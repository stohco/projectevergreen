/**
 * RBFTerrain — Radial Basis Function terrain with canon control points.
 *
 * Implements the spec §2.1:
 *
 *   h(x, z) = Σᵢ aᵢ · φ(‖(x,z) − cᵢ‖) + r(x,z)
 *
 * Where:
 *   φ is a Wendland RBF (compact support, C² continuous)
 *   cᵢ are authored control points from the canon placement map
 *   aᵢ is the amplitude (mountain height) at each control point
 *   r(x,z) is deterministic micro-detail noise
 *
 * This means mountains sit WHERE CANON SAYS they sit:
 *   - Heng Yue Sect (640, -480) → jagged mountains
 *   - Snow Domain (0, -2400) → high snowy peaks
 *   - Sea of Devils (0, 1800) → below sea level (ocean)
 *   - Wang Lin's village (0, 0) → gentle plains (flattened)
 *
 * This is fundamentally different from pure simplex noise: the macro
 * landforms are AUTHORED, the micro detail is procedural.
 */

import { createNoise2D } from 'simplex-noise'
import { PLANET_SUZAKU_PLACEMENT } from '../../canon/PlanetSuzakuPlacement'
import type { MaterialSlot } from '../semantic/CanonTypes'

// Deterministic seed for micro-detail noise.
function mulberry32(seed: number): () => number {
  let a = seed >>> 0
  return () => {
    a = (a + 0x6d2b79f5) >>> 0
    let t = a
    t = Math.imul(t ^ (t >>> 15), t | 1)
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61)
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}

const CANON_SEED = 1337
const noiseDetail = createNoise2D(mulberry32(CANON_SEED + 99))
const noiseMedium = createNoise2D(mulberry32(CANON_SEED + 7))

// ---- Canon control points ------------------------------------------------

interface ControlPoint {
  x: number
  z: number
  amplitude: number // height contribution
  radius: number // support radius
}

/**
 * Build control points from the canon placement map.
 * Each placed location becomes an RBF control point with amplitude
 * determined by its biome:
 *   mountains → high amplitude (peaks)
 *   snow → high amplitude (peaks)
 *   sea → negative amplitude (ocean basin)
 *   swamp → low amplitude (flat)
 *   plains → near-zero amplitude (flat)
 *   desert → low-medium amplitude (dunes)
 */
function buildControlPoints(): ControlPoint[] {
  const cps: ControlPoint[] = []
  for (const loc of PLANET_SUZAKU_PLACEMENT) {
    let amplitude = 0
    let radius = loc.radius * 3
    switch (loc.biome) {
      case 'mountains': amplitude = 40; radius = loc.radius * 4; break
      case 'snow': amplitude = 50; radius = loc.radius * 3; break
      case 'sea': amplitude = -20; radius = loc.radius * 2; break
      case 'swamp': amplitude = -2; radius = loc.radius * 2; break
      case 'plains': amplitude = 0; radius = loc.radius; break
      case 'desert': amplitude = 5; radius = loc.radius * 2; break
      case 'volcanic': amplitude = 25; radius = loc.radius * 3; break
      case 'forest': amplitude = 8; radius = loc.radius * 2; break
      case 'coast': amplitude = -5; radius = loc.radius * 2; break
    }
    cps.push({
      x: loc.position[0],
      z: loc.position[1],
      amplitude,
      radius,
    })
  }
  return cps
}

const CONTROL_POINTS = buildControlPoints()

// ---- Wendland RBF --------------------------------------------------------

/**
 * Wendland C² RBF — compact support, smooth, C² continuous.
 * φ(r) = (1 - r/R)⁴ · (4r/R + 1) for r < R, else 0.
 * This is the standard choice for scattered data interpolation with
 * compact support — zero outside the radius, smooth inside.
 */
function wendlandC2(r: number, R: number): number {
  if (r >= R) return 0
  const t = 1 - r / R
  return t * t * t * t * (4 * (r / R) + 1)
}

// ---- The terrain height function -----------------------------------------

/** Base sea level — terrain below this is underwater. */
export const SEA_LEVEL = 0

/**
 * The RBF terrain height function:
 *
 *   h(x, z) = Σᵢ aᵢ · φ(‖(x,z) − cᵢ‖) + r(x,z)
 *
 * Control points come from canon placements. Micro detail from noise.
 * The village area (near 0,0) is flattened for walkable spawn.
 */
export function rbfTerrainHeight(x: number, z: number): number {
  // 1. Sum RBF contributions from all canon control points.
  let h = 0
  for (const cp of CONTROL_POINTS) {
    const dx = x - cp.x
    const dz = z - cp.z
    const r = Math.sqrt(dx * dx + dz * dz)
    h += cp.amplitude * wendlandC2(r, cp.radius)
  }

  // 2. Base elevation — gentle rolling baseline.
  h += noiseMedium(x * 0.008, z * 0.008) * 4

  // 3. Micro detail — small bumps for visual texture.
  h += noiseDetail(x * 0.05, z * 0.05) * 0.8

  // 4. Flatten near spawn (village area) — 60-block radius for the full village.
  // Canon: Wang Family Village is a small mortal village on flat ground.
  // The village needs flat terrain for all 25 buildings.
  const distFromSpawn = Math.sqrt(x * x + z * z)
  const flattenRadius = 60
  if (distFromSpawn < flattenRadius) {
    const t = distFromSpawn / flattenRadius
    const flatten = t * t // quadratic ramp from 0 at center to 1 at edge
    const targetHeight = 2 // gentle village height above sea level
    h = h * flatten + targetHeight * (1 - flatten)
  }

  return h
}

/**
 * Determine surface material based on height + biome.
 * Used by the renderer for vertex coloring / material assignment.
 */
export function terrainMaterialAt(height: number): MaterialSlot {
  if (height < SEA_LEVEL - 2) return 'WATER'
  if (height < SEA_LEVEL + 1) return 'SAND'
  if (height < 8) return 'GRASS'
  if (height < 14) return 'STONE'
  return 'STONE' // high peaks — snow is a separate overlay
}

/**
 * Determine biome tag for a position. Combines height + canon placement.
 */
export function biomeAt(x: number, z: number, height: number): string {
  // Check canon placement first.
  for (const loc of PLANET_SUZAKU_PLACEMENT) {
    const dx = x - loc.position[0]
    const dz = z - loc.position[1]
    const dist = Math.sqrt(dx * dx + dz * dz)
    if (dist < loc.radius * 1.5) {
      return loc.biome
    }
  }
  // Fallback: height-based.
  if (height < SEA_LEVEL) return 'sea'
  if (height > 15) return 'mountains'
  return 'plains'
}

/**
 * SDF for the cliff of 天逆珠 (where Wang Lin found the bead as a child).
 * Returns signed distance: negative = inside cliff, positive = outside.
 * Used by the compiler to carve a cliff feature into the terrain.
 */
export function cliffSDF(x: number, z: number): number {
  // Cliff is at (-80, -120) with radius ~25.
  const dx = x + 80
  const dz = z + 120
  return Math.sqrt(dx * dx + dz * dz) - 25
}

/**
 * SDF for rivers. Returns signed distance to the nearest river path.
 * For now, one river flows from the mountains to the sea near the village.
 */
export function riverSDF(x: number, z: number): number {
  // Simple river: a band from (50, -100) to (-30, 200).
  // Distance from point to line segment.
  const ax = 50, az = -100
  const bx = -30, bz = 200
  const dx = bx - ax
  const dz = bz - az
  const len2 = dx * dx + dz * dz
  let t = ((x - ax) * dx + (z - az) * dz) / len2
  t = Math.max(0, Math.min(1, t))
  const px = ax + t * dx
  const pz = az + t * dz
  const d = Math.sqrt((x - px) ** 2 + (z - pz) ** 2)
  return d - 3 // river is 6 blocks wide (radius 3)
}
