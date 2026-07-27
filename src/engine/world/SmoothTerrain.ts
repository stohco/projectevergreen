/**
 * SmoothTerrain — low-poly heightmap terrain mesh.
 *
 * Replaces the blocky voxel terrain with a SMOOTH mesh like No Mortal Space.
 * Uses simplex noise for rolling hills. The mesh is a PlaneGeometry with
 * vertex displacement. Face normals are computed for smooth shading.
 *
 * This is NOT voxel — it's a single continuous mesh. Player edits (mining/
 * building) will use a separate voxel overlay system in the future. For now,
 * the terrain is the visual foundation: smooth, painterly, xianxia.
 *
 * Deterministic: same seed = same terrain. The CANON_SEED is fixed.
 */

import * as THREE from 'three'
import { createNoise2D } from 'simplex-noise'

const CANON_SEED = 1337

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

const noise2D = createNoise2D(mulberry32(CANON_SEED))
const noise2D_detail = createNoise2D(mulberry32(CANON_SEED + 1))

/** Terrain height at a world (x, z) position. */
export function terrainHeight(x: number, z: number): number {
  // Base rolling hills — gentle, not jagged.
  const base = noise2D(x * 0.01, z * 0.01) * 8
  // Medium detail.
  const medium = noise2D(x * 0.03, z * 0.03) * 3
  // Fine detail.
  const fine = noise2D_detail(x * 0.08, z * 0.08) * 1
  // Flatten near spawn (village area) — 40-block radius.
  const dist = Math.sqrt(x * x + z * z)
  const flattenRadius = 40
  let height = base + medium + fine
  if (dist < flattenRadius) {
    const t = dist / flattenRadius
    const flatten = 1 - (1 - t) * (1 - t) // quadratic
    const targetHeight = 2 // gentle village height
    height = height * flatten + targetHeight * (1 - flatten)
  }
  return height
}

/**
 * Create a smooth terrain mesh covering `size` x `size` blocks centered
 * at (centerX, centerZ). The mesh has `segments` x `segments` vertices.
 */
export function createSmoothTerrain(
  centerX: number,
  centerZ: number,
  size: number,
  segments: number,
): THREE.Mesh {
  const geo = new THREE.PlaneGeometry(size, size, segments, segments)
  geo.rotateX(-Math.PI / 2) // make it horizontal (XZ plane, Y up)

  // Displace vertices by terrain height.
  const positions = geo.attributes.position
  for (let i = 0; i < positions.count; i++) {
    const x = positions.getX(i) + centerX
    const z = positions.getZ(i) + centerZ
    const y = terrainHeight(x, z)
    positions.setY(i, y)
  }
  positions.needsUpdate = true

  // Compute vertex normals for smooth shading.
  geo.computeVertexNormals()

  // Vertex colors based on height (grass low, stone mid, snow high).
  const colors = new Float32Array(positions.count * 3)
  const color = new THREE.Color()
  for (let i = 0; i < positions.count; i++) {
    const y = positions.getY(i)
    if (y < 0) {
      color.setRGB(0.15, 0.35, 0.55) // underwater — deep blue
    } else if (y < 1) {
      color.setRGB(0.75, 0.65, 0.45) // beach sand
    } else if (y < 6) {
      color.setRGB(0.35, 0.55, 0.25) // grass green
    } else if (y < 10) {
      color.setRGB(0.45, 0.50, 0.30) // dry grass / rock transition
    } else if (y < 14) {
      color.setRGB(0.50, 0.48, 0.42) // rock
    } else {
      color.setRGB(0.90, 0.92, 0.95) // snow cap
    }
    // Add slight noise variation for painterly feel.
    const variation = noise2D_detail(x_var(positions.getX(i)), z_var(positions.getZ(i))) * 0.08
    colors[i * 3] = Math.max(0, color.r + variation)
    colors[i * 3 + 1] = Math.max(0, color.g + variation)
    colors[i * 3 + 2] = Math.max(0, color.b + variation)
  }
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  const mat = new THREE.MeshStandardMaterial({
    vertexColors: true,
    roughness: 0.85,
    metalness: 0.0,
    flatShading: false, // smooth shading
  })

  const mesh = new THREE.Mesh(geo, mat)
  mesh.receiveShadow = true
  mesh.castShadow = false
  mesh.name = 'terrain'
  return mesh
}

// Helpers for vertex color variation.
function x_var(x: number): number { return x * 0.5 }
function z_var(z: number): number { return z * 0.5 }

/**
 * Create instanced spirit-pine trees scattered on the terrain.
 * Returns a THREE.InstancedMesh.
 */
export function createSpiritPines(
  centerX: number,
  centerZ: number,
  size: number,
  count: number,
): THREE.InstancedMesh {
  // Tree geometry: a trunk (cylinder) + foliage (cone) — fuller, NMS-style.
  const trunkGeo = new THREE.CylinderGeometry(0.2, 0.3, 3, 8)
  trunkGeo.translate(0, 1.5, 0)
  // Triple-layer foliage for a fuller canopy.
  const foliageGeo = new THREE.ConeGeometry(2.0, 4, 8)
  foliageGeo.translate(0, 4.5, 0)
  const foliageGeo2 = new THREE.ConeGeometry(1.6, 3, 8)
  foliageGeo2.translate(0, 5.8, 0)
  const foliageGeo3 = new THREE.ConeGeometry(1.1, 2.2, 8)
  foliageGeo3.translate(0, 7.0, 0)
  // Merge into a single geometry.
  const trunkPos = trunkGeo.attributes.position
  const foliagePos = foliageGeo.attributes.position
  const foliagePos2 = foliageGeo2.attributes.position
  const foliagePos3 = foliageGeo3.attributes.position
  const merged = new THREE.BufferGeometry()
  const positions: number[] = []
  const normals: number[] = []
  for (let i = 0; i < trunkPos.count; i++) {
    positions.push(trunkPos.getX(i), trunkPos.getY(i), trunkPos.getZ(i))
    const n = trunkGeo.attributes.normal
    normals.push(n.getX(i), n.getY(i), n.getZ(i))
  }
  for (let i = 0; i < foliagePos.count; i++) {
    positions.push(foliagePos.getX(i), foliagePos.getY(i), foliagePos.getZ(i))
    const n = foliageGeo.attributes.normal
    normals.push(n.getX(i), n.getY(i), n.getZ(i))
  }
  for (let i = 0; i < foliagePos2.count; i++) {
    positions.push(foliagePos2.getX(i), foliagePos2.getY(i), foliagePos2.getZ(i))
    const n = foliageGeo2.attributes.normal
    normals.push(n.getX(i), n.getY(i), n.getZ(i))
  }
  for (let i = 0; i < foliagePos3.count; i++) {
    positions.push(foliagePos3.getX(i), foliagePos3.getY(i), foliagePos3.getZ(i))
    const n = foliageGeo3.attributes.normal
    normals.push(n.getX(i), n.getY(i), n.getZ(i))
  }
  merged.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  merged.setAttribute('normal', new THREE.Float32BufferAttribute(normals, 3))

  // Vertex colors: trunk brown, foliage green (3 layers, slightly different shades).
  const colors: number[] = []
  for (let i = 0; i < trunkPos.count; i++) {
    colors.push(0.35, 0.25, 0.15) // trunk brown
  }
  for (let i = 0; i < foliagePos.count; i++) {
    colors.push(0.18, 0.38, 0.16) // dark green
  }
  for (let i = 0; i < foliagePos2.count; i++) {
    colors.push(0.22, 0.42, 0.18) // medium green
  }
  for (let i = 0; i < foliagePos3.count; i++) {
    colors.push(0.26, 0.46, 0.20) // light green top
  }
  merged.setAttribute('color', new THREE.Float32BufferAttribute(colors, 3))

  const mat = new THREE.MeshStandardMaterial({
    vertexColors: true,
    roughness: 0.8,
    metalness: 0.0,
  })

  const instanced = new THREE.InstancedMesh(merged, mat, count)
  instanced.castShadow = true
  instanced.receiveShadow = true

  const dummy = new THREE.Object3D()
  let placed = 0
  let attempts = 0
  while (placed < count && attempts < count * 5) {
    attempts++
    const x = centerX + (Math.random() - 0.5) * size
    const z = centerZ + (Math.random() - 0.5) * size
    const dist = Math.sqrt(x * x + z * z)
    // No trees in the village plaza (20-block radius).
    if (dist < 20) continue
    // No trees on water or snow.
    const h = terrainHeight(x, z)
    if (h < 1 || h > 12) continue
    // Deterministic placement via noise.
    if (noise2D(x * 0.3, z * 0.3) < -0.3) continue
    dummy.position.set(x, h, z)
    dummy.rotation.y = Math.random() * Math.PI * 2
    const scale = 0.8 + Math.random() * 0.6
    dummy.scale.set(scale, scale, scale)
    dummy.updateMatrix()
    instanced.setMatrixAt(placed, dummy.matrix)
    placed++
  }
  instanced.count = placed
  instanced.instanceMatrix.needsUpdate = true
  return instanced
}
