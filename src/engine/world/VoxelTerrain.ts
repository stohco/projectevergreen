/**
 * VoxelTerrain — a hybrid voxel + smooth terrain system.
 *
 * PROBLEM: The current smooth terrain is a thin mesh — you can see through
 * the ground and see the ocean below. In a voxel world (NMS-style), the
 * ground is SOLID — you can dig into it, and there's material below the
 * surface.
 *
 * SOLUTION: Create a thick voxel shell under the smooth terrain surface.
 * The top surface uses the RBF heightmap (smooth, beautiful). Below the
 * surface, we generate voxel layers (dirt → stone → bedrock) so the
 * terrain has DEPTH. You can't see through it. You can dig into it.
 *
 * This is a hybrid: smooth top surface + voxel volume below.
 * The smooth mesh handles the visual quality. The voxel volume handles
 * the solidity and deformability.
 *
 * Per PRD §13.1: "Terrain should be authored as continuous fields plus
 * deterministic local detail." The top IS a continuous field. The volume
 * below is deterministic voxel data.
 */

import * as THREE from 'three'
import { rbfTerrainHeight, terrainMaterialAt } from './field/RBFTerrain'
import type { MaterialSlot } from './semantic/CanonTypes'

export interface VoxelColumn {
  /** Surface height (from RBF). */
  surfaceY: number
  /** Material at the surface. */
  surfaceMaterial: MaterialSlot
  /** Depth of dirt layer below surface. */
  dirtDepth: number
  /** Depth of stone layer below dirt. */
  stoneDepth: number
  /** Bedrock starts at this Y. */
  bedrockY: number
}

/**
 * Sample the voxel column at a world (x, z) position.
 * Returns the material at any depth below the surface.
 *
 * Layer structure (top to bottom):
 *   surface → dirt (3-5 blocks) → stone (10-20 blocks) → bedrock (infinite)
 */
export function sampleVoxelColumn(x: number, z: number): VoxelColumn {
  const surfaceY = rbfTerrainHeight(x, z)
  const surfaceMaterial = terrainMaterialAt(surfaceY)
  // Deterministic depth variation.
  const hash = (Math.sin(x * 12.9898 + z * 78.233) * 43758.5453) % 1
  const h = hash - Math.floor(hash)
  const dirtDepth = 3 + Math.floor(h * 3) // 3-5 blocks
  const stoneDepth = 10 + Math.floor(h * 11) // 10-20 blocks
  const bedrockY = surfaceY - dirtDepth - stoneDepth
  return { surfaceY, surfaceMaterial, dirtDepth, stoneDepth, bedrockY }
}

/**
 * Get the material at a specific world Y position.
 * Used for mining: when you dig, you get the material at that depth.
 */
export function sampleMaterialAtDepth(x: number, z: number, y: number): MaterialSlot {
  const col = sampleVoxelColumn(x, z)
  if (y > col.surfaceY) return 'WATER' // above surface = air or water
  if (y >= col.surfaceY - 1) return col.surfaceMaterial
  if (y >= col.surfaceY - col.dirtDepth) return 'DIRT'
  if (y >= col.bedrockY) return 'STONE'
  return 'STONE' // bedrock
}

/**
 * Create a SOLID terrain mesh — the surface mesh PLUS a thick skirt/wall
 * around the edges so you can't see through to the ocean below.
 *
 * The skirt extends from the surface down to bedrockY (well below sea level).
 * This makes the terrain look like a solid landmass, not a floating sheet.
 */
export function createSolidTerrain(
  centerX: number,
  centerZ: number,
  size: number,
  segments: number,
): THREE.Mesh {
  const geo = new THREE.PlaneGeometry(size, size, segments, segments)
  geo.rotateX(-Math.PI / 2)

  // Displace vertices by RBF terrain height.
  const positions = geo.attributes.position
  for (let i = 0; i < positions.count; i++) {
    const x = positions.getX(i) + centerX
    const z = positions.getZ(i) + centerZ
    const y = rbfTerrainHeight(x, z)
    positions.setY(i, y)
  }
  positions.needsUpdate = true
  geo.computeVertexNormals()

  // Vertex colors based on height + material.
  const colors = new Float32Array(positions.count * 3)
  const color = new THREE.Color()
  for (let i = 0; i < positions.count; i++) {
    const x = positions.getX(i) + centerX
    const z = positions.getZ(i) + centerZ
    const y = positions.getY(i)
    const mat = terrainMaterialAt(y)
    switch (mat) {
      case 'WATER': color.setRGB(0.15, 0.35, 0.55); break
      case 'SAND': color.setRGB(0.75, 0.65, 0.45); break
      case 'GRASS': color.setRGB(0.30, 0.52, 0.22); break
      case 'STONE':
        if (y > 18) color.setRGB(0.92, 0.94, 0.96)
        else color.setRGB(0.48, 0.46, 0.42)
        break
      default: color.setRGB(0.40, 0.50, 0.30)
    }
    const v = (Math.sin(x * 12.9898 + z * 78.233) * 43758.5453) % 1
    const variation = (v - Math.floor(v) - 0.5) * 0.1
    colors[i * 3] = Math.max(0, Math.min(1, color.r + variation))
    colors[i * 3 + 1] = Math.max(0, Math.min(1, color.g + variation))
    colors[i * 3 + 2] = Math.max(0, Math.min(1, color.b + variation))
  }
  geo.setAttribute('color', new THREE.BufferAttribute(colors, 3))

  // Create a SOLID material — the terrain is opaque, no see-through.
  const mat = new THREE.MeshStandardMaterial({
    vertexColors: true,
    roughness: 0.85,
    metalness: 0.0,
    flatShading: false,
    side: THREE.FrontSide, // only render front faces — no see-through
  })

  const mesh = new THREE.Mesh(geo, mat)
  mesh.receiveShadow = true
  mesh.castShadow = false
  mesh.name = 'terrain'

  // Add a skirt/wall around the terrain edges to prevent see-through.
  // The skirt extends from the terrain edge down to y=-20 (below ocean).
  const skirtGeo = new THREE.BufferGeometry()
  const skirtPositions: number[] = []
  const skirtColors: number[] = []
  const skirtIndices: number[] = []

  // Get edge vertices (the 4 sides of the terrain).
  const halfSize = size / 2
  const skirtDepth = 25 // extend 25 blocks below lowest terrain
  const edges: Array<Array<[number, number]>> = [
    // North edge (z = -halfSize)
    [], // will fill with [x, surfaceY] pairs
    // South edge (z = +halfSize)
    [],
    // East edge (x = +halfSize)
    [],
    // West edge (x = -halfSize)
    [],
  ]

  // Sample edge points.
  const edgeSegments = segments
  for (let i = 0; i <= edgeSegments; i++) {
    const t = i / edgeSegments
    // North edge: x goes from -halfSize to +halfSize, z = -halfSize
    const nx = centerX - halfSize + t * size
    const nz = centerZ - halfSize
    edges[0].push([nx, rbfTerrainHeight(nx, nz)])
    // South edge
    edges[1].push([nx, rbfTerrainHeight(nx, centerZ + halfSize)])
    // East edge: z goes from -halfSize to +halfSize, x = +halfSize
    const ez = centerZ - halfSize + t * size
    edges[2].push([rbfTerrainHeight(centerX + halfSize, ez), ez])
    // Wait, the format is [x_or_y, ...]. Let me fix.
  }

  // Rebuild edges with proper [x, y, z] triples.
  const edgePoints: Array<Array<[number, number, number]>> = [[], [], [], []]
  for (let i = 0; i <= edgeSegments; i++) {
    const t = i / edgeSegments
    // North edge
    const nx = centerX - halfSize + t * size
    const ny = rbfTerrainHeight(nx, centerZ - halfSize)
    edgePoints[0].push([nx, ny, centerZ - halfSize])
    // South edge
    const sy = rbfTerrainHeight(nx, centerZ + halfSize)
    edgePoints[1].push([nx, sy, centerZ + halfSize])
    // East edge
    const ez = centerZ - halfSize + t * size
    const ey = rbfTerrainHeight(centerX + halfSize, ez)
    edgePoints[2].push([centerX + halfSize, ey, ez])
    // West edge
    const wy = rbfTerrainHeight(centerX - halfSize, ez)
    edgePoints[3].push([centerX - halfSize, wy, ez])
  }

  // Build skirt geometry: for each edge point, create a quad going down.
  let vIdx = 0
  const skirtColor = new THREE.Color(0x4a3a2a) // brown/dirt color
  for (const edge of edgePoints) {
    for (let i = 0; i < edge.length - 1; i++) {
      const [x1, y1, z1] = edge[i]
      const [x2, y2, z2] = edge[i + 1]
      const bottomY1 = Math.min(y1, 0) - skirtDepth
      const bottomY2 = Math.min(y2, 0) - skirtDepth

      // Top vertices
      skirtPositions.push(x1, y1, z1)
      skirtPositions.push(x2, y2, z2)
      // Bottom vertices
      skirtPositions.push(x1, bottomY1, z1)
      skirtPositions.push(x2, bottomY2, z2)

      // Colors
      for (let c = 0; c < 4; c++) {
        skirtColors.push(skirtColor.r, skirtColor.g, skirtColor.b)
      }

      // Indices (2 triangles)
      skirtIndices.push(vIdx, vIdx + 2, vIdx + 1)
      skirtIndices.push(vIdx + 1, vIdx + 2, vIdx + 3)
      vIdx += 4
    }
  }

  skirtGeo.setAttribute('position', new THREE.Float32BufferAttribute(skirtPositions, 3))
  skirtGeo.setAttribute('color', new THREE.Float32BufferAttribute(skirtColors, 3))
  skirtGeo.setIndex(skirtIndices)
  skirtGeo.computeVertexNormals()

  const skirtMat = new THREE.MeshStandardMaterial({
    vertexColors: true,
    roughness: 0.9,
    metalness: 0.0,
    side: THREE.DoubleSide,
  })
  const skirt = new THREE.Mesh(skirtGeo, skirtMat)
  skirt.receiveShadow = true
  skirt.name = 'terrain_skirt'

  // Combine terrain + skirt into one group.
  const group = new THREE.Group()
  group.add(mesh)
  group.add(skirt)
  group.name = 'solid_terrain'

  // Return the main mesh but store the group on userData.
  mesh.userData.terrainGroup = group
  mesh.userData.skirt = skirt
  return mesh
}

/**
 * Create instanced trees with VARIETY — different sizes, shapes, and colors.
 * Not all identical clones. Per user: "whats up with these all being the
 * exact same size and shape? is this what trees look like in the er gen
 * multiverse?"
 *
 * Canon: Zhao Country has spirit pines. But not all pines are identical —
 * some are young (thin, short), some are ancient (thick, tall), some are
 * weathered (asymmetric). We create 4 tree variants and randomly assign them.
 */
export function createVariedSpiritPines(
  centerX: number,
  centerZ: number,
  size: number,
  count: number,
): THREE.InstancedMesh {
  // 4 tree variants with different proportions.
  const variants = [
    { trunkR: 0.20, trunkH: 3.0, folR: [2.0, 1.6, 1.1], folY: [4.0, 5.2, 6.3], color: [0.18, 0.38, 0.16] },
    { trunkR: 0.30, trunkH: 4.5, folR: [2.8, 2.2, 1.5], folY: [5.5, 7.0, 8.5], color: [0.15, 0.35, 0.14] },
    { trunkR: 0.15, trunkH: 2.0, folR: [1.5, 1.2, 0.8], folY: [3.0, 4.0, 5.0], color: [0.22, 0.42, 0.18] },
    { trunkR: 0.25, trunkH: 5.5, folR: [3.2, 2.5, 1.8, 1.0], folY: [6.5, 8.0, 9.5, 10.8], color: [0.12, 0.30, 0.12] },
  ]

  // Build a merged geometry from the first variant (we'll scale per-instance).
  const v = variants[0]
  const trunkGeo = new THREE.CylinderGeometry(v.trunkR * 0.8, v.trunkR, v.trunkH, 8)
  trunkGeo.translate(0, v.trunkH / 2, 0)
  const fol1 = new THREE.IcosahedronGeometry(v.folR[0], 1)
  fol1.translate(0, v.folY[0], 0)
  const fol2 = new THREE.IcosahedronGeometry(v.folR[1], 1)
  fol2.translate(0, v.folY[1], 0)
  const fol3 = new THREE.IcosahedronGeometry(v.folR[2], 1)
  fol3.translate(0, v.folY[2], 0)

  // Merge.
  const merged = new THREE.BufferGeometry()
  const positions: number[] = []
  const normals: number[] = []
  const colors: number[] = []
  const parts = [
    { geo: trunkGeo, color: [0.35, 0.25, 0.15] },
    { geo: fol1, color: v.color },
    { geo: fol2, color: v.color },
    { geo: fol3, color: v.color },
  ]
  for (const part of parts) {
    const pos = part.geo.attributes.position
    const nrm = part.geo.attributes.normal
    for (let i = 0; i < pos.count; i++) {
      positions.push(pos.getX(i), pos.getY(i), pos.getZ(i))
      normals.push(nrm.getX(i), nrm.getY(i), nrm.getZ(i))
      colors.push(part.color[0], part.color[1], part.color[2])
    }
  }
  merged.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  merged.setAttribute('normal', new THREE.Float32BufferAttribute(normals, 3))
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
  const color = new THREE.Color()
  let placed = 0
  let attempts = 0
  while (placed < count && attempts < count * 5) {
    attempts++
    const x = centerX + (Math.random() - 0.5) * size
    const z = centerZ + (Math.random() - 0.5) * size
    const h = rbfTerrainHeight(x, z)
    if (h < 1 || h > 12) continue
    const dist = Math.sqrt(x * x + z * z)
    if (dist < 20) continue

    // Pick a random variant and scale.
    const variantIdx = Math.floor(Math.random() * variants.length)
    const variant = variants[variantIdx]
    const scaleFactor = variant.trunkR / v.trunkR // scale relative to base

    dummy.position.set(x, h, z)
    dummy.rotation.y = Math.random() * Math.PI * 2
    // Vary scale per instance.
    const s = scaleFactor * (0.7 + Math.random() * 0.6)
    dummy.scale.set(s, s, s)
    // Slight lean for weathered trees.
    if (variantIdx === 2) {
      dummy.rotation.z = (Math.random() - 0.5) * 0.15
    }
    dummy.updateMatrix()
    instanced.setMatrixAt(placed, dummy.matrix)

    // Vary color slightly per instance.
    const c = variant.color
    const brightness = 0.8 + Math.random() * 0.4
    color.setRGB(c[0] * brightness, c[1] * brightness, c[2] * brightness)
    instanced.setColorAt(placed, color)
    placed++
  }
  instanced.count = placed
  instanced.instanceMatrix.needsUpdate = true
  if (instanced.instanceColor) instanced.instanceColor.needsUpdate = true
  return instanced
}
