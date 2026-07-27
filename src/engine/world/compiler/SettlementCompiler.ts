/**
 * SettlementCompiler — compiles a CanonSettlement into Three.js meshes.
 *
 * This is the spec's "Voxel/Scene Compiler" layer (§4.4). It takes
 * semantic objects (settlement → buildings → rooms → furniture) and
 * produces renderable Three.js geometry using the template library.
 *
 * The compiler does NOT know about gameplay logic. It only knows:
 *   - semantic structure (from CanonTypes)
 *   - templates (from TemplateLibrary)
 *   - Three.js mesh construction
 *
 * Output: a THREE.Group containing all building meshes, positioned and
 * rotated per the semantic data. The caller adds this group to the scene.
 */

import * as THREE from 'three'
import type { CanonSettlement, CanonBuilding, CanonFurniture } from '../semantic/CanonTypes'
import { getMaterial, FURNITURE_TEMPLATES, type MeshPlacement } from '../template/TemplateLibrary'

/**
 * Compile a settlement into a Three.js group.
 */
export function compileSettlement(settlement: CanonSettlement): THREE.Group {
  const group = new THREE.Group()
  group.name = `settlement:${settlement.id}`
  group.position.set(settlement.position[0], settlement.position[1], settlement.position[2])

  // Compile each building.
  for (const building of settlement.buildings) {
    const buildingGroup = compileBuilding(building)
    group.add(buildingGroup)
  }

  // Compile roads.
  for (const road of settlement.roads) {
    const roadMesh = compileRoad(road)
    group.add(roadMesh)
  }

  return group
}

/**
 * Compile a single building into a Three.js group.
 */
function compileBuilding(building: CanonBuilding): THREE.Group {
  const group = new THREE.Group()
  group.name = building.id
  group.position.set(building.position[0], building.position[1], building.position[2])
  group.rotation.y = (building.rotation * Math.PI) / 180

  const [w, h, d] = building.size

  switch (building.purpose) {
    case 'well':
      compileWell(group, building)
      break
    case 'gate':
      compileGate(group, building)
      break
    case 'fence':
      compileFence(group, building)
      break
    default:
      compileHut(group, building)
      break
  }

  // Compile furniture in each room.
  for (const room of building.rooms) {
    for (const furn of room.furniture) {
      const furnMesh = compileFurniture(furn, building.shellTheme)
      furnMesh.position.set(
        room.position[0] + furn.position[0],
        room.position[1] + furn.position[1],
        room.position[2] + furn.position[2],
      )
      if (furn.rotation) {
        furnMesh.rotation.y = (furn.rotation * Math.PI) / 180
      }
      group.add(furnMesh)
    }
  }

  return group
}

/**
 * Compile a hut: walls, roof, door, floor.
 * A poor village hut has:
 *   - 4 walls (with a door opening on the front)
 *   - a pitched thatch roof
 *   - a packed-earth floor
 *   - optional pillars
 */
function compileHut(group: THREE.Group, building: CanonBuilding): void {
  const [w, h, d] = building.size
  const theme = building.shellTheme
  const wallMat = getMaterial('WALL', theme)
  const roofMat = getMaterial('ROOF', theme)
  const floorMat = getMaterial('FLOOR', theme)
  const doorMat = getMaterial('DOOR', theme)
  const pillarMat = getMaterial('PILLAR', theme)

  /** Helper: tag a mesh as collidable for the ray-based collision system. */
  const collidable = (mesh: THREE.Mesh, name: string): THREE.Mesh => {
    mesh.userData.collidable = true
    mesh.name = name
    return mesh
  }

  // Floor
  const floor = collidable(new THREE.Mesh(new THREE.BoxGeometry(w, 0.1, d), floorMat), 'floor')
  floor.position.set(0, 0.05, 0)
  floor.receiveShadow = true
  group.add(floor)

  // Walls (4 walls with door opening on +Z face)
  const wallThickness = 0.15
  // Back wall (-Z)
  const backWall = collidable(new THREE.Mesh(new THREE.BoxGeometry(w, h, wallThickness), wallMat), 'wall_back')
  backWall.position.set(0, h / 2, -d / 2)
  backWall.castShadow = true
  backWall.receiveShadow = true
  group.add(backWall)
  // Front wall (+Z) with door gap — split into left + right + top
  const doorWidth = 1.0
  const doorHeight = 1.8
  const sideWidth = (w - doorWidth) / 2
  if (sideWidth > 0) {
    const leftFront = collidable(new THREE.Mesh(new THREE.BoxGeometry(sideWidth, h, wallThickness), wallMat), 'wall_front_left')
    leftFront.position.set(-(doorWidth / 2 + sideWidth / 2), h / 2, d / 2)
    leftFront.castShadow = true
    group.add(leftFront)
    const rightFront = collidable(new THREE.Mesh(new THREE.BoxGeometry(sideWidth, h, wallThickness), wallMat), 'wall_front_right')
    rightFront.position.set(doorWidth / 2 + sideWidth / 2, h / 2, d / 2)
    rightFront.castShadow = true
    group.add(rightFront)
  }
  // Above door
  const aboveDoor = collidable(new THREE.Mesh(new THREE.BoxGeometry(doorWidth, h - doorHeight, wallThickness), wallMat), 'wall_above_door')
  aboveDoor.position.set(0, doorHeight + (h - doorHeight) / 2, d / 2)
  group.add(aboveDoor)

  // Left wall (-X)
  const leftWall = collidable(new THREE.Mesh(new THREE.BoxGeometry(wallThickness, h, d), wallMat), 'wall_left')
  leftWall.position.set(-w / 2, h / 2, 0)
  leftWall.castShadow = true
  group.add(leftWall)
  // Right wall (+X)
  const rightWall = collidable(new THREE.Mesh(new THREE.BoxGeometry(wallThickness, h, d), wallMat), 'wall_right')
  rightWall.position.set(w / 2, h / 2, 0)
  rightWall.castShadow = true
  group.add(rightWall)

  // Door (closed by default, openable via E key interaction).
  // The door pivots on its left edge (hinge) to swing open.
  // When closed, the door is collidable (blocks the player). When open,
  // it's non-collidable (player can walk through the doorway).
  const door = new THREE.Mesh(
    new THREE.BoxGeometry(doorWidth * 0.9, doorHeight * 0.95, 0.05),
    doorMat,
  )
  // Offset the door geometry so its left edge is at x=0 (the hinge).
  door.geometry.translate(doorWidth * 0.45, 0, 0)
  door.position.set(-doorWidth * 0.45, doorHeight / 2, d / 2 + 0.05)
  door.userData.isDoor = true
  door.userData.isOpen = false
  door.userData.collidable = true // closed door blocks movement
  door.userData.buildingId = building.id
  door.userData.openAngle = -Math.PI / 2.5 // swing inward
  door.name = 'door'
  door.castShadow = true
  group.add(door)

  // Pitched roof — two slanted planes forming a triangle prism.
  const roofHeight = h * 0.5
  const roofGeo = new THREE.ConeGeometry(Math.max(w, d) * 0.75, roofHeight, 4)
  const roof = new THREE.Mesh(roofGeo, roofMat)
  roof.position.set(0, h + roofHeight / 2, 0)
  roof.rotation.y = Math.PI / 4
  roof.castShadow = true
  roof.receiveShadow = true
  group.add(roof)

  // Corner pillars (decorative structural)
  const pillarSize = 0.2
  for (const [px, pz] of [[-w / 2, -d / 2], [w / 2, -d / 2], [-w / 2, d / 2], [w / 2, d / 2]] as const) {
    const pillar = new THREE.Mesh(
      new THREE.BoxGeometry(pillarSize, h, pillarSize),
      pillarMat,
    )
    pillar.position.set(px, h / 2, pz)
    pillar.castShadow = true
    group.add(pillar)
  }
}

/**
 * Compile a stone well.
 */
function compileWell(group: THREE.Group, building: CanonBuilding): void {
  const wellMat = getMaterial('WALL', building.shellTheme)
  const waterMat = getMaterial('WATER', building.shellTheme)
  const woodMat = getMaterial('PILLAR', building.shellTheme)

  // Well wall (hollow cylinder)
  const wall = new THREE.Mesh(
    new THREE.CylinderGeometry(0.8, 0.9, 1.0, 16, 1, true),
    wellMat,
  )
  wall.position.set(0, 0.5, 0)
  wall.castShadow = true
  wall.receiveShadow = true
  group.add(wall)

  // Water surface
  const water = new THREE.Mesh(
    new THREE.CircleGeometry(0.7, 16),
    waterMat,
  )
  water.rotation.x = -Math.PI / 2
  water.position.set(0, 0.3, 0)
  group.add(water)

  // Wooden frame (2 posts + crossbar)
  for (const px of [-0.8, 0.8]) {
    const post = new THREE.Mesh(
      new THREE.BoxGeometry(0.1, 2.0, 0.1),
      woodMat,
    )
    post.position.set(px, 1.0, 0)
    post.castShadow = true
    group.add(post)
  }
  const crossbar = new THREE.Mesh(
    new THREE.BoxGeometry(1.8, 0.1, 0.1),
    woodMat,
  )
  crossbar.position.set(0, 2.0, 0)
  group.add(crossbar)

  // Small roof on top
  const roof = new THREE.Mesh(
    new THREE.ConeGeometry(1.2, 0.6, 4),
    getMaterial('ROOF', building.shellTheme),
  )
  roof.position.set(0, 2.35, 0)
  roof.rotation.y = Math.PI / 4
  group.add(roof)
}

/**
 * Compile a village gate.
 */
function compileGate(group: THREE.Group, building: CanonBuilding): void {
  const [w, h, d] = building.size
  const woodMat = getMaterial('PILLAR', building.shellTheme)
  const roofMat = getMaterial('ROOF', building.shellTheme)

  // Two posts
  for (const px of [-w / 3, w / 3]) {
    const post = new THREE.Mesh(
      new THREE.BoxGeometry(0.4, h, 0.4),
      woodMat,
    )
    post.position.set(px, h / 2, 0)
    post.castShadow = true
    group.add(post)
  }
  // Crossbeam
  const beam = new THREE.Mesh(
    new THREE.BoxGeometry(w, 0.3, 0.4),
    woodMat,
  )
  beam.position.set(0, h, 0)
  group.add(beam)
  // Roof
  const roof = new THREE.Mesh(
    new THREE.ConeGeometry(w * 0.6, 0.8, 4),
    roofMat,
  )
  roof.position.set(0, h + 0.4, 0)
  roof.rotation.y = Math.PI / 4
  group.add(roof)
}

/**
 * Compile a fence segment.
 */
function compileFence(group: THREE.Group, building: CanonBuilding): void {
  const [w, h, d] = building.size
  const woodMat = getMaterial('PILLAR', building.shellTheme)
  const fence = new THREE.Mesh(
    new THREE.BoxGeometry(w, h, d),
    woodMat,
  )
  fence.position.set(0, h / 2, 0)
  fence.castShadow = true
  group.add(fence)
}

/**
 * Compile a road as a flat plane.
 */
function compileRoad(road: { points: [number, number, number][]; width: number; material: string }): THREE.Mesh {
  const points = road.points.map((p) => new THREE.Vector3(p[0], p[1] + 0.02, p[2]))
  if (points.length < 2) return new THREE.Mesh()
  const curve = new THREE.CatmullRomCurve3(points)
  const geo = new THREE.TubeGeometry(curve, points.length * 4, road.width / 2, 8, false)
  const mat = getMaterial(road.material as 'DIRT', 'poor_village_wood')
  mat.transparent = true
  mat.opacity = 0.8
  const mesh = new THREE.Mesh(geo, mat)
  mesh.receiveShadow = true
  return mesh
}

/**
 * Compile furniture using the template library.
 */
function compileFurniture(furn: CanonFurniture, themeId: string): THREE.Group {
  const group = new THREE.Group()
  group.name = `furniture:${furn.id}`
  const template = FURNITURE_TEMPLATES[furn.kind]
  if (!template) return group

  for (const placement of template.placements) {
    const mesh = compilePlacement(placement, themeId)
    group.add(mesh)
  }
  return group
}

function compilePlacement(placement: MeshPlacement, themeId: string): THREE.Mesh {
  let geo: THREE.BufferGeometry
  switch (placement.kind) {
    case 'box':
      geo = new THREE.BoxGeometry(placement.size[0], placement.size[1], placement.size[2])
      break
    case 'cylinder':
      geo = new THREE.CylinderGeometry(placement.size[0], placement.size[0], placement.size[1], 12)
      break
    case 'plane':
      geo = new THREE.PlaneGeometry(placement.size[0], placement.size[1])
      break
    case 'sphere':
      geo = new THREE.SphereGeometry(placement.size[0], 12, 8)
      break
    default:
      geo = new THREE.BoxGeometry(placement.size[0], placement.size[1], placement.size[2])
  }
  const mat = getMaterial(placement.slot, themeId)
  const mesh = new THREE.Mesh(geo, mat)
  mesh.position.set(placement.position[0], placement.position[1], placement.position[2])
  if (placement.rotation) {
    mesh.rotation.y = (placement.rotation * Math.PI) / 180
  }
  mesh.castShadow = true
  mesh.receiveShadow = true
  return mesh
}
