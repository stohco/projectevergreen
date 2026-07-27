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
  // Tree geometry: thick trunk + FULL spherical foliage clusters.
  // NOT thin cones — actual 3D foliage that looks like a pine tree.
  const trunkGeo = new THREE.CylinderGeometry(0.25, 0.35, 4, 10)
  trunkGeo.translate(0, 2, 0)

  // Foliage: 3 overlapping SPHERES (not cones) for a full, bushy canopy.
  // Spheres look solid from every angle — no invisible thin-cone problem.
  const foliageGeo = new THREE.IcosahedronGeometry(2.2, 1) // low-poly sphere, looks organic
  foliageGeo.translate(0, 4.5, 0)
  const foliageGeo2 = new THREE.IcosahedronGeometry(1.8, 1)
  foliageGeo2.translate(0, 5.8, 0)
  const foliageGeo3 = new THREE.IcosahedronGeometry(1.3, 1)
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
  // Grass tuft geometry — a cluster of thin, flat blades that curve outward.
  // NOT crossed planes (those look like christmas trees). Each blade is a
  // thin triangle that curves, and we place 6-8 of them in a fan pattern.
  const tuftGeo = new THREE.BufferGeometry()
  const positions: number[] = []
  const normals: number[] = []
  const indices: number[] = []
  const numBlades = 7
  const bladeHeight = 0.18
  const bladeWidth = 0.04

  for (let i = 0; i < numBlades; i++) {
    const angle = (i / numBlades) * Math.PI * 2 + Math.random() * 0.3
    const cos = Math.cos(angle)
    const sin = Math.sin(angle)
    // Each blade: a thin triangle from base to tip, curving slightly.
    const baseIdx = i * 3 // 3 vertices per blade (1 triangle)
    // Base center
    positions.push(0, 0, 0)
    // Base left (offset perpendicular to blade direction)
    positions.push(-bladeWidth * sin, 0, bladeWidth * cos)
    // Tip (curved outward + up)
    const curve = 0.02 * (i % 2 === 0 ? 1 : -1)
    positions.push(cos * curve, bladeHeight, sin * curve)
    // Normal: pointing outward + up
    normals.push(cos * 0.5, 0.8, sin * 0.5)
    normals.push(cos * 0.5, 0.8, sin * 0.5)
    normals.push(cos * 0.5, 0.9, sin * 0.5)
    indices.push(baseIdx, baseIdx + 1, baseIdx + 2)
  }
  tuftGeo.setAttribute('position', new THREE.Float32BufferAttribute(positions, 3))
  tuftGeo.setAttribute('normal', new THREE.Float32BufferAttribute(normals, 3))
  tuftGeo.setIndex(indices)

  // Grass material — rich green, double-sided so blades are visible from all angles.
  const grassMat = new THREE.MeshStandardMaterial({
    color: 0x4a8a2a,
    roughness: 0.8,
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

  // Poisson-disk-like distribution.
  const minDist = 1.2 // spread out more — less dense, more natural
  const placedPositions: Array<{ x: number; z: number }> = []
  const maxCheck = 20

  while (placed < count && attempts < count * 4) {
    attempts++
    const x = centerX + (Math.random() - 0.5) * size
    const z = centerZ + (Math.random() - 0.5) * size
    const h = rbfTerrainHeight(x, z)
    if (h < 1 || h > 8) continue
    const dist = Math.sqrt(x * x + z * z)
    if (dist < 15) continue

    let tooClose = false
    const start = Math.max(0, placedPositions.length - maxCheck)
    for (let i = start; i < placedPositions.length; i++) {
      const p = placedPositions[i]
      const dx = p.x - x
      const dz = p.z - z
      if (dx * dx + dz * dz < minDist * minDist) { tooClose = true; break }
    }
    if (tooClose) continue
    placedPositions.push({ x, z })

    dummy.position.set(x, h, z)
    dummy.rotation.y = Math.random() * Math.PI * 2
    const scale = 0.8 + Math.random() * 0.6
    dummy.scale.set(scale, scale, scale)
    dummy.updateMatrix()
    instanced.setMatrixAt(placed, dummy.matrix)

    // Green spectrum variation.
    const v = 0.7 + Math.random() * 0.3
    color.setHSL(0.25 + Math.random() * 0.06, 0.55 * v, 0.30 * v)
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
