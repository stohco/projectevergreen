/**
 * SmoothTerrain — low-poly heightmap terrain mesh, RBF-driven.
 *
 * Uses the RBF terrain height function (canon control points + Wendland RBF
 * + micro detail noise) from RBFTerrain.ts. Mountains sit where canon says
 * they sit. The village area is flattened. This is NOT blocky voxels — it's
 * a single continuous smooth mesh.
 *
 * The renderer samples the field: terrain = T(blueprint, simDelta, playerDelta).
 */

import * as THREE from 'three'
import { rbfTerrainHeight, terrainMaterialAt, SEA_LEVEL } from './field/RBFTerrain'

// Re-export for backward compatibility.
export { rbfTerrainHeight as terrainHeight } from './field/RBFTerrain'

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

  // Displace vertices by RBF terrain height (canon-authored).
  const positions = geo.attributes.position
  for (let i = 0; i < positions.count; i++) {
    const x = positions.getX(i) + centerX
    const z = positions.getZ(i) + centerZ
    const y = rbfTerrainHeight(x, z)
    positions.setY(i, y)
  }
  positions.needsUpdate = true

  // Compute vertex normals for smooth shading.
  geo.computeVertexNormals()

  // Vertex colors based on height + material (canon-driven).
  const colors = new Float32Array(positions.count * 3)
  const color = new THREE.Color()
  for (let i = 0; i < positions.count; i++) {
    const x = positions.getX(i) + centerX
    const z = positions.getZ(i) + centerZ
    const y = positions.getY(i)
    const mat = terrainMaterialAt(y)
    switch (mat) {
      case 'WATER':
        color.setRGB(0.15, 0.35, 0.55); break
      case 'SAND':
        color.setRGB(0.75, 0.65, 0.45); break
      case 'GRASS':
        // Grass gets slight noise variation for painterly feel.
        color.setRGB(0.30, 0.52, 0.22); break
      case 'STONE':
        if (y > 18) {
          color.setRGB(0.92, 0.94, 0.96) // snow cap
        } else {
          color.setRGB(0.48, 0.46, 0.42) // rock
        }
        break
      default:
        color.setRGB(0.40, 0.50, 0.30)
    }
    // Add slight noise variation for painterly feel.
    const v = (Math.sin(x * 12.9898 + z * 78.233) * 43758.5453) % 1
    const variation = (v - Math.floor(v) - 0.5) * 0.1
    colors[i * 3] = Math.max(0, Math.min(1, color.r + variation))
    colors[i * 3 + 1] = Math.max(0, Math.min(1, color.g + variation))
    colors[i * 3 + 2] = Math.max(0, Math.min(1, color.b + variation))
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
    const h = rbfTerrainHeight(x, z)
    if (h < 1 || h > 12) continue
    // Deterministic placement via a simple hash.
    const hash = (Math.sin(x * 12.9898 + z * 78.233) * 43758.5453)
    const noiseVal = (hash - Math.floor(hash)) * 2 - 1 // -1..1
    if (noiseVal < -0.3) continue
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

/**
 * Create instanced grass CLUSTERS — NMS-style dense vegetation.
 *
 * Instead of sparse individual tall blades, this creates dense clusters of
 * short grass tufts. Each cluster is a small patch of 5-8 crossed planes
 * forming a bushy tuft. Distribution uses Poisson-disk-like sampling so
 * clusters form organic patches with natural gaps (not a uniform grid).
 *
 * The result looks like ground cover, not a hair forest.
 *
 * Returns an InstancedMesh of grass tuft clusters.
 */
export function createGrassTufts(
  centerX: number,
  centerZ: number,
  size: number,
  count: number,
): THREE.InstancedMesh {
  // Grass tuft geometry — a small bushy cluster of 4 crossed planes.
  // Each plane is short (0.25m) and wide (0.3m), forming a dense clump.
  const tuftGeo = new THREE.BufferGeometry()
  const positions: number[] = []
  const normals: number[] = []
  const uvs: number[] = []
  const indices: number[] = []
  // 4 crossed planes, each 0.3m wide, 0.25m tall.
  const bladeW = 0.15
  const bladeH = 0.25
  for (let i = 0; i < 4; i++) {
    const angle = (i / 4) * Math.PI
    const cos = Math.cos(angle)
    const sin = Math.sin(angle)
    const baseIdx = i * 4
    // 4 vertices per plane (2 triangles).
    positions.push(
      -bladeW * cos, 0, -bladeW * sin, // bottom-left
      bladeW * cos, 0, bladeW * sin,   // bottom-right
      bladeW * cos * 0.2, bladeH, bladeW * sin * 0.2,  // top-right (tapered)
      -bladeW * cos * 0.2, bladeH, -bladeW * sin * 0.2, // top-left (tapered)
    )
    // Normal pointing outward from the plane.
    normals.push(sin, 0.5, -cos, sin, 0.5, -cos, sin, 0.8, -cos, sin, 0.8, -cos)
    uvs.push(0, 0, 1, 0, 1, 1, 0, 1)
    indices.push(baseIdx, baseIdx + 1, baseIdx + 2, baseIdx, baseIdx + 2, baseIdx + 3)
  }
  tuftGeo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  tuftGeo.setAttribute('normal', new THREE.Float32BufferAttribute(normals, 3))
  tuftGeo.setAttribute('uv', new THREE.Float32BufferAttribute(uvs, 2))
  tuftGeo.setIndex(indices)

  // Grass material — rich green, alpha-tested for soft edges.
  const grassMat = new THREE.MeshStandardMaterial({
    color: 0x5a9a3a,
    roughness: 0.85,
    metalness: 0.0,
    side: THREE.DoubleSide,
    transparent: false,
  })

  const instanced = new THREE.InstancedMesh(tuftGeo, grassMat, count)
  instanced.castShadow = false
  instanced.receiveShadow = false
  instanced.frustumCulled = false

  const dummy = new THREE.Object3D()
  const color = new THREE.Color()
  let placed = 0
  let attempts = 0

  // Poisson-disk-like distribution: reject points too close to previous ones.
  // This creates organic patches with natural gaps, not a uniform scatter.
  const minDist = 0.8 // minimum distance between tufts
  const placedPositions: Array<{ x: number; z: number }> = []
  const maxCheck = Math.min(20, placedPositions.length) // only check recent

  while (placed < count && attempts < count * 4) {
    attempts++
    const x = centerX + (Math.random() - 0.5) * size
    const z = centerZ + (Math.random() - 0.5) * size
    const h = rbfTerrainHeight(x, z)
    // Grass only on grass terrain (not water, not snow, not high rock).
    if (h < 1 || h > 8) continue
    // No grass in the village plaza (15-block radius).
    const dist = Math.sqrt(x * x + z * z)
    if (dist < 15) continue

    // Poisson-disk check: reject if too close to recent placements.
    let tooClose = false
    const start = Math.max(0, placedPositions.length - maxCheck)
    for (let i = start; i < placedPositions.length; i++) {
      const p = placedPositions[i]
      const dx = p.x - x
      const dz = p.z - z
      if (dx * dx + dz * dz < minDist * minDist) {
        tooClose = true
        break
      }
    }
    if (tooClose) continue

    placedPositions.push({ x, z })

    dummy.position.set(x, h, z)
    dummy.rotation.y = Math.random() * Math.PI * 2
    // Vary scale: some tufts small, some larger — natural variation.
    const scale = 0.6 + Math.random() * 0.8
    dummy.scale.set(scale, scale, scale)
    dummy.updateMatrix()
    instanced.setMatrixAt(placed, dummy.matrix)

    // Per-instance color: rich green spectrum (emerald to lime).
    const v = 0.7 + Math.random() * 0.3
    const hue = 0.25 + Math.random() * 0.08 // green range
    color.setHSL(hue, 0.5 * v, 0.35 * v)
    instanced.setColorAt(placed, color)

    placed++
  }
  instanced.count = placed
  instanced.instanceMatrix.needsUpdate = true
  if (instanced.instanceColor) instanced.instanceColor.needsUpdate = true
  return instanced
}

/**
 * Create instanced rocks scattered on the terrain.
 * Adds geological detail + visual interest.
 */
export function createRocks(
  centerX: number,
  centerZ: number,
  size: number,
  count: number,
): THREE.InstancedMesh {
  // Rock geometry — a low-poly icosahedron (jagged, natural).
  const rockGeo = new THREE.IcosahedronGeometry(1, 0)
  // Randomize vertices for natural variation.
  const pos = rockGeo.attributes.position
  for (let i = 0; i < pos.count; i++) {
    const v = 0.7 + Math.random() * 0.6
    pos.setX(i, pos.getX(i) * v)
    pos.setY(i, pos.getY(i) * v)
    pos.setZ(i, pos.getZ(i) * v)
  }
  pos.needsUpdate = true
  rockGeo.computeVertexNormals()

  const rockMat = new THREE.MeshStandardMaterial({
    color: 0x6a6a6e,
    roughness: 0.85,
    metalness: 0.0,
    flatShading: true, // jagged look
  })

  const instanced = new THREE.InstancedMesh(rockGeo, rockMat, count)
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
    if (h < 1 || h > 16) continue
    // Fewer rocks in the village plaza.
    const dist = Math.sqrt(x * x + z * z)
    if (dist < 12 && Math.random() > 0.3) continue

    dummy.position.set(x, h - 0.3, z)
    dummy.rotation.set(Math.random() * Math.PI, Math.random() * Math.PI, Math.random() * Math.PI)
    const scale = 0.3 + Math.random() * 0.8
    dummy.scale.set(scale, scale * 0.8, scale)
    dummy.updateMatrix()
    instanced.setMatrixAt(placed, dummy.matrix)

    // Color variation (grey to brown-grey).
    const v = 0.6 + Math.random() * 0.4
    color.setRGB(0.42 * v, 0.40 * v, 0.38 * v)
    instanced.setColorAt(placed, color)

    placed++
  }
  instanced.count = placed
  instanced.instanceMatrix.needsUpdate = true
  if (instanced.instanceColor) instanced.instanceColor.needsUpdate = true
  return instanced
}

/**
 * Create instanced spirit flowers (small glowing blossoms).
 * Adds qi-infused color to the landscape — xianxia atmosphere.
 */
export function createSpiritFlowers(
  centerX: number,
  centerZ: number,
  size: number,
  count: number,
): THREE.InstancedMesh {
  // Flower geometry — a small cross of planes.
  const flowerGeo = new THREE.IcosahedronGeometry(0.15, 0)
  flowerGeo.translate(0, 0.15, 0)

  const flowerMat = new THREE.MeshStandardMaterial({
    color: 0x9be15d,
    roughness: 0.3,
    metalness: 0.1,
    emissive: 0x9be15d,
    emissiveIntensity: 0.5,
  })

  const instanced = new THREE.InstancedMesh(flowerGeo, flowerMat, count)
  instanced.castShadow = false

  const dummy = new THREE.Object3D()
  let placed = 0
  let attempts = 0
  while (placed < count && attempts < count * 5) {
    attempts++
    const x = centerX + (Math.random() - 0.5) * size
    const z = centerZ + (Math.random() - 0.5) * size
    const h = rbfTerrainHeight(x, z)
    if (h < 2 || h > 8) continue
    const dist = Math.sqrt(x * x + z * z)
    if (dist < 15) continue

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
