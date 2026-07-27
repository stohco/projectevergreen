/**
 * CollisionSystem — AABB collision for buildings, props, and terrain.
 *
 * The player (and NPCs) cannot walk through walls, buildings, or large props.
 * This uses axis-aligned bounding boxes (AABB) for simplicity and performance.
 * Each building registers its AABB; the player movement code checks against
 * all registered boxes before moving.
 *
 * Terrain collision is handled separately (the ground-clamp in WorldCanvas)
 * — this system is for OBJECTS on top of terrain.
 *
 * Per the DESIGN_HITBOXES_AND_FORMATIONS.md: every object has physical
 * properties (mass, material, world law resistance). Collision is the
 * physical manifestation of those properties — you can't walk through a
 * wall because the wall has mass and the world's laws resist changing it.
 */

export interface AABB {
  minX: number
  maxX: number
  minY: number
  maxY: number
  minZ: number
  maxZ: number
}

export interface CollisionEntity {
  id: string
  box: AABB
  /** If true, entities are pushed out. If false, entity is a trigger (no push). */
  solid: boolean
}

export class CollisionSystem {
  private readonly boxes: CollisionEntity[] = []

  register(entity: CollisionEntity): void {
    this.boxes.push(entity)
  }

  registerBox(id: string, box: AABB, solid = true): void {
    this.boxes.push({ id, box, solid })
  }

  /**
   * Check if a point (x, z) at height y is inside any solid AABB.
   * Returns the colliding entity, or null if no collision.
   */
  checkPoint(x: number, y: number, z: number): CollisionEntity | null {
    for (const e of this.boxes) {
      if (!e.solid) continue
      const b = e.box
      if (x >= b.minX && x <= b.maxX && y >= b.minY && y <= b.maxY && z >= b.minZ && z <= b.maxZ) {
        return e
      }
    }
    return null
  }

  /**
   * Check if a cylinder (player body) at (x, z) with radius r and height
   * range [y, y+height] intersects any solid AABB.
   * Returns the colliding entity, or null.
   */
  checkCylinder(x: number, y: number, z: number, radius: number, height: number): CollisionEntity | null {
    for (const e of this.boxes) {
      if (!e.solid) continue
      const b = e.box
      // Cylinder vs AABB: check if the cylinder's XZ circle intersects the
      // AABB's XZ rectangle, AND the cylinder's Y range overlaps the AABB's Y range.
      const closestX = Math.max(b.minX, Math.min(x, b.maxX))
      const closestZ = Math.max(b.minZ, Math.min(z, b.maxZ))
      const dx = x - closestX
      const dz = z - closestZ
      const distSq = dx * dx + dz * dz
      if (distSq > radius * radius) continue // no XZ overlap
      // Y overlap check.
      if (y + height < b.minY || y > b.maxY) continue // no Y overlap
      return e
    }
    return null
  }

  /**
   * Resolve collision: given a desired position (x, y, z) and the previous
   * position (prevX, prevY, prevZ), push the entity out of any solid AABB
   * it's intersecting. Returns the corrected position.
   *
   * This is a simple push-out: for each axis, if the entity is inside a box,
   * push it back to the nearest edge.
   */
  resolve(
    x: number, y: number, z: number,
    prevX: number, prevY: number, prevZ: number,
    radius: number, height: number,
  ): { x: number; y: number; z: number } {
    let result = { x, y, z }
    // Check up to 3 times (for corner cases where pushing out of one box
    // pushes into another).
    for (let iter = 0; iter < 3; iter++) {
      const hit = this.checkCylinder(result.x, result.y, result.z, radius, height)
      if (!hit) break
      const b = hit.box
      // Push out along the axis of least penetration.
      const penX = result.x < (b.minX + b.maxX) / 2 ? result.x - b.minX : result.x - b.maxX
      const penZ = result.z < (b.minZ + b.maxZ) / 2 ? result.z - b.minZ : result.z - b.maxZ
      const absPenX = Math.abs(penX) + radius
      const absPenZ = Math.abs(penZ) + radius
      if (absPenX < absPenZ) {
        // Push along X.
        result.x = penX < 0 ? b.minX - radius : b.maxX + radius
      } else {
        // Push along Z.
        result.z = penZ < 0 ? b.minZ - radius : b.maxZ + radius
      }
    }
    return result
  }

  clear(): void {
    this.boxes.length = 0
  }

  count(): number {
    return this.boxes.length
  }
}
