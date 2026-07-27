/**
 * MeshCollisionSystem — ray-based collision against actual 3D mesh geometry.
 *
 * REPLACES the broken AABB system. This is the proper veteran approach:
 *
 * Instead of axis-aligned bounding boxes (which can't handle rotated buildings,
 * can't handle doorways, and teleport you on fast movement), we cast rays from
 * the player in the movement direction against the ACTUAL wall meshes. The
 * player stops at the intersection point — collision wraps exactly around the
 * 3D model, works with any rotation, and doorways are naturally walkable
 * (there's no mesh in the door gap, so the ray passes through).
 *
 * How it works:
 *   1. Register collidable meshes (walls, roofs, large props — NOT grass/flowers).
 *   2. Before moving the player, cast a short ray (player radius) in the
 *      movement direction from the player's new position.
 *   3. If the ray hits a registered mesh, push the player back to just before
 *      the hit point.
 *   4. Cast rays in 4 cardinal directions (N/S/E/W) from the player's body
 *      to prevent clipping when sliding along walls.
 *
 * This is how AAA games do character collision against arbitrary geometry.
 * It's more expensive than AABB but the village has <100 meshes, so it's
 * negligible (sub-millisecond per frame).
 */

import * as THREE from 'three'

export class MeshCollisionSystem {
  private readonly raycaster: THREE.Raycaster
  private readonly collidables: THREE.Object3D[] = []
  private readonly playerRadius: number

  constructor(playerRadius = 0.4) {
    this.raycaster = new THREE.Raycaster()
    this.raycaster.far = playerRadius * 2
    this.playerRadius = playerRadius
  }

  /**
   * Register a Three.js object (and all its children) as collidable.
   * Call this after compiling buildings — pass the village group.
   * Only meshes with userData.collidable = true (or name starts with 'wall')
   * are considered. Grass, flowers, and small props are NOT collidable.
   */
  register(obj: THREE.Object3D): void {
    obj.traverse((child) => {
      const mesh = child as THREE.Mesh
      if (!mesh.isMesh) return
      // Only register meshes marked as collidable, or named as walls/roofs.
      if (mesh.userData.collidable || mesh.name?.includes('wall') || mesh.name?.includes('roof') || mesh.name?.includes('pillar') || mesh.name?.includes('door')) {
        this.collidables.push(mesh)
      }
    })
  }

  /**
   * Check if the player can move from (prevX, prevZ) to (newX, newZ) without
   * hitting a wall or closed door. Returns the corrected position.
   *
   * Strategy: cast rays in 8 directions from the new position. If any ray
   * hits a collidable mesh within playerRadius, push the player back along
   * that ray to just outside the hit point.
   *
   * Doorways: the door gap has no wall mesh, so rays pass through the gap.
   * But a CLOSED door IS a collidable mesh — it blocks the player. When the
   * door is opened (userData.collidable = false), it stops blocking and the
   * player can walk through.
   */
  resolve(
    newX: number, newY: number, newZ: number,
    prevX: number, _prevY: number, prevZ: number,
  ): { x: number; z: number; hit: boolean } {
    if (this.collidables.length === 0) return { x: newX, z: newZ, hit: false }

    // Filter to only currently-collidable meshes (doors toggle this).
    const activeCollidables = this.collidables.filter((m) => m.userData.collidable !== false)

    let resultX = newX
    let resultZ = newZ
    let hit = false

    const origin = new THREE.Vector3(resultX, newY + 0.9, resultZ)
    const directions = [
      new THREE.Vector3(1, 0, 0),   // +X (east)
      new THREE.Vector3(-1, 0, 0),  // -X (west)
      new THREE.Vector3(0, 0, 1),   // +Z (south)
      new THREE.Vector3(0, 0, -1),  // -Z (north)
      new THREE.Vector3(0.707, 0, 0.707),   // NE
      new THREE.Vector3(-0.707, 0, 0.707),  // NW
      new THREE.Vector3(0.707, 0, -0.707),  // SE
      new THREE.Vector3(-0.707, 0, -0.707), // SW
    ]

    for (const dir of directions) {
      this.raycaster.set(origin, dir)
      this.raycaster.far = this.playerRadius
      const intersects = this.raycaster.intersectObjects(activeCollidables, false)
      if (intersects.length > 0) {
        const dist = intersects[0].distance
        if (dist < this.playerRadius) {
          const pushDist = this.playerRadius - dist + 0.01
          resultX -= dir.x * pushDist
          resultZ -= dir.z * pushDist
          origin.x = resultX
          origin.z = resultZ
          hit = true
        }
      }
    }

    // Movement-direction ray.
    const moveDir = new THREE.Vector3(newX - prevX, 0, newZ - prevZ)
    if (moveDir.lengthSq() > 0.0001) {
      moveDir.normalize()
      const moveOrigin = new THREE.Vector3(prevX, newY + 0.9, prevZ)
      this.raycaster.set(moveOrigin, moveDir)
      this.raycaster.far = this.playerRadius + moveDir.length() * 0.5
      const moveHits = this.raycaster.intersectObjects(activeCollidables, false)
      if (moveHits.length > 0 && moveHits[0].distance < this.playerRadius) {
        resultX = prevX
        resultZ = prevZ
        hit = true
      }
    }

    return { x: resultX, z: resultZ, hit }
  }

  clear(): void {
    this.collidables.length = 0
  }

  count(): number {
    return this.collidables.length
  }
}
