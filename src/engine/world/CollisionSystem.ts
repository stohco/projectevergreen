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
   * Resolve collision using the PREVIOUS position to determine which side
   * the entity came from. This prevents the teleport-through-walls bug
   * where a fast-moving player gets pushed to the wrong side.
   *
   * Strategy: for each colliding box, determine which face the entity
   * entered from (based on prevPos relative to box center), then push
   * back to that face. This ensures the entity is pushed BACK to where
   * it came from, not through to the other side.
   */
  resolve(
    x: number, y: number, z: number,
    prevX: number, prevY: number, prevZ: number,
    radius: number, height: number,
  ): { x: number; y: number; z: number } {
    let result = { x, y, z }
    for (let iter = 0; iter < 4; iter++) {
      const hit = this.checkCylinder(result.x, result.y, result.z, radius, height)
      if (!hit) break
      const b = hit.box
      const centerX = (b.minX + b.maxX) / 2
      const centerZ = (b.minZ + b.maxZ) / 2

      // Determine which face the entity entered from using the PREVIOUS position.
      // If prevX was outside the box on the -X side, push to -X face.
      // If prevX was outside on +X side, push to +X face.
      // If prevZ was outside on -Z side, push to -Z face.
      // If prevZ was outside on +Z side, push to +Z face.
      // If both were outside (corner entry), push along the axis of greater movement.

      const wasOutsideMinX = prevX + radius <= b.minX
      const wasOutsideMaxX = prevX - radius >= b.maxX
      const wasOutsideMinZ = prevZ + radius <= b.minZ
      const wasOutsideMaxZ = prevZ - radius >= b.maxZ

      const movedX = Math.abs(x - prevX)
      const movedZ = Math.abs(z - prevZ)

      if (wasOutsideMinX) {
        // Came from -X side — push back to -X face.
        result.x = b.minX - radius
      } else if (wasOutsideMaxX) {
        // Came from +X side — push back to +X face.
        result.x = b.maxX + radius
      } else if (wasOutsideMinZ) {
        // Came from -Z side.
        result.z = b.minZ - radius
      } else if (wasOutsideMaxZ) {
        // Came from +Z side.
        result.z = b.maxZ + radius
      } else {
        // Entity was already inside the box (shouldn't happen normally).
        // Fall back to nearest-face push based on current position.
        const distMinX = Math.abs(result.x - b.minX)
        const distMaxX = Math.abs(result.x - b.maxX)
        const distMinZ = Math.abs(result.z - b.minZ)
        const distMaxZ = Math.abs(result.z - b.maxZ)
        const minDist = Math.min(distMinX, distMaxX, distMinZ, distMaxZ)
        if (minDist === distMinX) result.x = b.minX - radius
        else if (minDist === distMaxX) result.x = b.maxX + radius
        else if (minDist === distMinZ) result.z = b.minZ - radius
        else result.z = b.maxZ + radius
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
