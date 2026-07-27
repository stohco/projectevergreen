/**
 * VoxelRaycaster — Amanatides-Woo voxel ray traversal.
 *
 * Given a ray origin and direction, walks voxel boundaries in O(N) where
 * N = number of voxels the ray passes through (≈ ray length in blocks).
 * Returns the first solid block hit, the face normal (entry face), and
 * the world-space hit point. Used for player block placement / removal
 * and target selection.
 *
 * Reference: Amanatides & Woo, "A Fast Voxel Traversal Algorithm for
 * Ray Tracing" (Eurographics 1987).
 *
 * NO canon chapter citations.
 */

import { BlockId, isOpaque } from './BlockRegistry'

export interface RaycastResult {
  /** Hit block world coords. */
  block: { x: number; y: number; z: number }
  /** Block id at the hit. */
  blockId: number
  /** Face normal of the entry face (unit vector). */
  normal: { x: number; y: number; z: number }
  /** World-space hit point (on the entry face). */
  point: { x: number; y: number; z: number }
  /** Distance from ray origin to hit point. */
  distance: number
}

export type WorldGetBlock = (wx: number, wy: number, wz: number) => number

/**
 * March a ray through the voxel grid. Returns null if no solid block is
 * hit within maxDist.
 *
 * `solid` defaults to "any non-air block"; pass a custom predicate to
 * e.g. ignore water or treat leaves as non-targetable.
 */
export function raycastVoxels(
  origin: { x: number; y: number; z: number },
  direction: { x: number; y: number; z: number },
  maxDist: number,
  worldGet: WorldGetBlock,
  isSolid: (id: number) => boolean = (id) => id !== BlockId.AIR,
): RaycastResult | null {
  // Normalize direction.
  const dlen = Math.hypot(direction.x, direction.y, direction.z)
  if (dlen === 0) return null
  const dx = direction.x / dlen
  const dy = direction.y / dlen
  const dz = direction.z / dlen

  // Current voxel (floor of origin).
  let ix = Math.floor(origin.x)
  let iy = Math.floor(origin.y)
  let iz = Math.floor(origin.z)

  // Step direction per axis (-1, 0, or +1).
  const stepX = dx > 0 ? 1 : dx < 0 ? -1 : 0
  const stepY = dy > 0 ? 1 : dy < 0 ? -1 : 0
  const stepZ = dz > 0 ? 1 : dz < 0 ? -1 : 0

  // tMax: distance along ray to the next voxel boundary on each axis.
  // tDelta: distance along ray between voxel boundaries on each axis.
  const tDeltaX = stepX !== 0 ? Math.abs(1 / dx) : Infinity
  const tDeltaY = stepY !== 0 ? Math.abs(1 / dy) : Infinity
  const tDeltaZ = stepZ !== 0 ? Math.abs(1 / dz) : Infinity

  // Initial boundary distance: distance from origin to the next voxel edge.
  const voxelBoundaryX = stepX > 0 ? ix + 1 : ix
  const voxelBoundaryY = stepY > 0 ? iy + 1 : iy
  const voxelBoundaryZ = stepZ > 0 ? iz + 1 : iz

  let tMaxX = stepX !== 0 ? (voxelBoundaryX - origin.x) / dx : Infinity
  let tMaxY = stepY !== 0 ? (voxelBoundaryY - origin.y) / dy : Infinity
  let tMaxZ = stepZ !== 0 ? (voxelBoundaryZ - origin.z) / dz : Infinity

  if (!isFinite(tMaxX) || tMaxX < 0) tMaxX = Infinity
  if (!isFinite(tMaxY) || tMaxY < 0) tMaxY = Infinity
  if (!isFinite(tMaxZ) || tMaxZ < 0) tMaxZ = Infinity

  // The normal of the face we entered the current voxel through.
  let normalX = 0
  let normalY = 0
  let normalZ = 0

  // Check the starting voxel first.
  let id = worldGet(ix, iy, iz)
  if (isSolid(id)) {
    return {
      block: { x: ix, y: iy, z: iz },
      blockId: id,
      normal: { x: 0, y: 0, z: 0 },
      point: { x: origin.x, y: origin.y, z: origin.z },
      distance: 0,
    }
  }

  let traveled = 0
  // Hard cap to prevent infinite loops on edge cases.
  const maxSteps = Math.ceil(maxDist) + 4

  for (let step = 0; step < maxSteps; step++) {
    if (tMaxX < tMaxY) {
      if (tMaxX < tMaxZ) {
        ix += stepX
        traveled = tMaxX
        tMaxX += tDeltaX
        normalX = -stepX
        normalY = 0
        normalZ = 0
      } else {
        iz += stepZ
        traveled = tMaxZ
        tMaxZ += tDeltaZ
        normalX = 0
        normalY = 0
        normalZ = -stepZ
      }
    } else {
      if (tMaxY < tMaxZ) {
        iy += stepY
        traveled = tMaxY
        tMaxY += tDeltaY
        normalX = 0
        normalY = -stepY
        normalZ = 0
      } else {
        iz += stepZ
        traveled = tMaxZ
        tMaxZ += tDeltaZ
        normalX = 0
        normalY = 0
        normalZ = -stepZ
      }
    }

    if (traveled > maxDist) break

    id = worldGet(ix, iy, iz)
    if (isSolid(id)) {
      const px = origin.x + dx * traveled
      const py = origin.y + dy * traveled
      const pz = origin.z + dz * traveled
      return {
        block: { x: ix, y: iy, z: iz },
        blockId: id,
        normal: { x: normalX, y: normalY, z: normalZ },
        point: { x: px, y: py, z: pz },
        distance: traveled,
      }
    }
  }

  return null
}

/**
 * Convenience: raycast that treats only opaque blocks as solid.
 * (Transparent blocks like water, leaves, ice are skipped.)
 */
export function raycastOpaque(
  origin: { x: number; y: number; z: number },
  direction: { x: number; y: number; z: number },
  maxDist: number,
  worldGet: WorldGetBlock,
): RaycastResult | null {
  return raycastVoxels(origin, direction, maxDist, worldGet, isOpaque)
}
