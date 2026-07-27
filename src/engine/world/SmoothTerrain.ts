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
