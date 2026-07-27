/**
 * VoxelMesher — AAA greedy mesher with ambient occlusion.
 *
 * For each chunk we produce THREE geometries (one per material class):
 *   opaque      — solid blocks (stone, dirt, grass, ...)
 *   transparent — water, lava, ice, qi crystal (rendered back-to-front)
 *   cutout      — leaves (alpha-tested, no blending)
 *
 * Greedy meshing merges coplanar same-block faces into the largest
 * rectangles possible. For a 16x256x16 chunk this typically produces
 * 5-10x fewer triangles than naive per-cube meshing.
 *
 * Per-vertex ambient occlusion is computed using the classic 3-voxel
 * sample (side1, side2, corner) in the outside block's plane. The AO
 * value (0..3) is baked into the vertex color attribute so the
 * standard MeshStandardMaterial shader picks it up automatically
 * (vertexColors = true).
 *
 * Atlas UVs are looked up from BlockRegistry.tileUV(tileIndex). Each
 * block face picks top / bottom / side based on the face's normal.
 * UVs are scaled by the merged quad size so a 4x1 quad tiles the
 * texture 4 times horizontally (seamless because the atlas is tileable).
 *
 * The mesher takes a worldGet callback for cross-chunk neighbor lookups
 * (chunk boundary faces + AO samples that fall in neighbor chunks).
 *
 * NO canon chapter citations.
 */

import {
  BlockId,
  faceVisible,
  getBlock,
  isOpaque,
  tileUV,
} from './BlockRegistry'
import {
  CHUNK_SIZE_X,
  CHUNK_SIZE_Y,
  CHUNK_SIZE_Z,
  VoxelChunk,
} from './VoxelChunk'

export type WorldGetBlock = (wx: number, wy: number, wz: number) => number

export interface MeshData {
  positions: number[]
  normals: number[]
  uvs: number[]
  colors: number[]
  indices: number[]
}

export interface ChunkMeshResult {
  opaque: MeshData | null
  transparent: MeshData | null
  cutout: MeshData | null
}

// ---- Face configuration ----------------------------------------------------

interface FaceConfig {
  axis: 0 | 1 | 2 // normal axis (0=X, 1=Y, 2=Z)
  sign: 1 | -1
  uAxis: 0 | 1 | 2
  vAxis: 0 | 1 | 2
  // Winding order: indices into the 4 corners (du,dv) ∈ {0,1}² such that
  // the resulting triangles are CCW when viewed from outside.
  // corners[0] = (du=0,dv=0), [1] = (du=1,dv=0), [2] = (du=1,dv=1), [3] = (du=0,dv=1)
  winding: [0, 1, 2, 3] | [0, 3, 2, 1]
  // Which tile of the block to use: 'top' | 'bottom' | 'side'
  tile: 'top' | 'bottom' | 'side'
}

const FACES: FaceConfig[] = [
  // +X — u=Z, v=Y — natural winding reversed (CCW from +X)
  { axis: 0, sign: 1, uAxis: 2, vAxis: 1, winding: [0, 3, 2, 1], tile: 'side' },
  // -X — u=Z, v=Y — natural winding
  { axis: 0, sign: -1, uAxis: 2, vAxis: 1, winding: [0, 1, 2, 3], tile: 'side' },
  // +Y — u=X, v=Z — reversed winding
  { axis: 1, sign: 1, uAxis: 0, vAxis: 2, winding: [0, 3, 2, 1], tile: 'top' },
  // -Y — u=X, v=Z — natural winding
  { axis: 1, sign: -1, uAxis: 0, vAxis: 2, winding: [0, 1, 2, 3], tile: 'bottom' },
  // +Z — u=X, v=Y — reversed winding
  { axis: 2, sign: 1, uAxis: 0, vAxis: 1, winding: [0, 3, 2, 1], tile: 'side' },
  // -Z — u=X, v=Y — natural winding
  { axis: 2, sign: -1, uAxis: 0, vAxis: 1, winding: [0, 1, 2, 3], tile: 'side' },
]

const SIZES = [CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z]

/** Empty mesh data accumulator. */
function newMesh(): MeshData {
  return { positions: [], normals: [], uvs: [], colors: [], indices: [] }
}

/** Fetch a voxel from the local chunk or, if out-of-range, the world callback. */
function getAt(
  chunk: VoxelChunk,
  worldGet: WorldGetBlock,
  lx: number,
  ly: number,
  lz: number,
): number {
  if (
    lx >= 0 &&
    lx < CHUNK_SIZE_X &&
    ly >= 0 &&
    ly < CHUNK_SIZE_Y &&
    lz >= 0 &&
    lz < CHUNK_SIZE_Z
  ) {
    return chunk.get(lx, ly, lz)
  }
  return worldGet(chunk.originX + lx, ly, chunk.originZ + lz)
}

/** AO sample: returns 1 if the block is opaque (occluding), 0 otherwise. */
function aoOccludes(chunk: VoxelChunk, worldGet: WorldGetBlock, lx: number, ly: number, lz: number): number {
  const id = getAt(chunk, worldGet, lx, ly, lz)
  return isOpaque(id) ? 1 : 0
}

/**
 * Compute the AO level (0..3) for a corner given 3 occlusion samples.
 * Classic formula: if both sides occlude → 0; else 3 - (side1 + side2 + corner).
 */
function aoLevel(side1: number, side2: number, corner: number): number {
  if (side1 && side2) return 0
  return 3 - (side1 + side2 + corner)
}

/** Map AO level 0..3 → brightness 0..1 (smoothstep-ish). */
const AO_BRIGHTNESS = [0.72, 0.85, 0.93, 1.0]

/**
 * Build the three material-class meshes for a chunk.
 * The worldGet callback is used for any voxel lookup that falls outside
 * the chunk's local storage (neighbor chunks + chunk-boundary AO samples).
 */
export function meshChunk(chunk: VoxelChunk, worldGet: WorldGetBlock): ChunkMeshResult {
  const opaque = newMesh()
  const transparent = newMesh()
  const cutout = newMesh()

  for (const face of FACES) {
    meshFace(chunk, worldGet, face, opaque, transparent, cutout)
  }

  return {
    opaque: opaque.positions.length > 0 ? opaque : null,
    transparent: transparent.positions.length > 0 ? transparent : null,
    cutout: cutout.positions.length > 0 ? cutout : null,
  }
}

function meshFace(
  chunk: VoxelChunk,
  worldGet: WorldGetBlock,
  face: FaceConfig,
  opaque: MeshData,
  transparent: MeshData,
  cutout: MeshData,
): void {
  const { axis, sign, uAxis, vAxis, winding, tile: tileSide } = face
  const sSize = SIZES[axis]
  const uSize = SIZES[uAxis]
  const vSize = SIZES[vAxis]

  // Normal direction unit vector
  const nVec: [number, number, number] = [0, 0, 0]
  nVec[axis] = sign

  // Mask buffer: stores blockId of the visible face at each (u, v) in this slice.
  const mask = new Int32Array(vSize * uSize)
  // Visited buffer for greedy merge
  const visited = new Uint8Array(vSize * uSize)

  for (let s = 0; s < sSize; s++) {
    // Reset mask
    mask.fill(0)

    // Build mask: for each (u, v), determine if a face is visible here.
    for (let v = 0; v < vSize; v++) {
      for (let u = 0; u < uSize; u++) {
        // Inside block local coord
        const inside = sliceToLocal(axis, uAxis, vAxis, s, u, v)
        const insideId = chunk.get(inside[0], inside[1], inside[2])
        if (insideId === BlockId.AIR) continue

        // Outside block local coord (across the face)
        const outside: [number, number, number] = [
          inside[0] + nVec[0],
          inside[1] + nVec[1],
          inside[2] + nVec[2],
        ]
        const outsideId = getAt(chunk, worldGet, outside[0], outside[1], outside[2])
        if (faceVisible(insideId, outsideId)) {
          mask[v * uSize + u] = insideId
        }
      }
    }

    // Greedy merge: find maximal rectangles of same blockId.
    visited.fill(0)
    for (let v0 = 0; v0 < vSize; v0++) {
      for (let u0 = 0; u0 < uSize; u0++) {
        if (visited[v0 * uSize + u0]) continue
        const id = mask[v0 * uSize + u0]
        if (id === 0) continue

        // Find max u extent
        let u1 = u0 + 1
        while (u1 < uSize && !visited[v0 * uSize + u1] && mask[v0 * uSize + u1] === id) {
          u1++
        }
        // Find max v extent
        let v1 = v0 + 1
        v1Loop: while (v1 < vSize) {
          for (let u = u0; u < u1; u++) {
            if (visited[v1 * uSize + u] || mask[v1 * uSize + u] !== id) break v1Loop
          }
          v1++
        }

        // Mark visited
        for (let v = v0; v < v1; v++) {
          for (let u = u0; u < u1; u++) {
            visited[v * uSize + u] = 1
          }
        }

        emitQuad(
          chunk,
          worldGet,
          face,
          s,
          u0,
          v0,
          u1,
          v1,
          id,
          opaque,
          transparent,
          cutout,
        )
      }
    }
  }
}

/** Map (axis, uAxis, vAxis, s, u, v) → local (x, y, z). */
function sliceToLocal(
  axis: 0 | 1 | 2,
  uAxis: 0 | 1 | 2,
  vAxis: 0 | 1 | 2,
  s: number,
  u: number,
  v: number,
): [number, number, number] {
  const out: [number, number, number] = [0, 0, 0]
  out[axis] = s
  out[uAxis] = u
  out[vAxis] = v
  return out
}

function emitQuad(
  chunk: VoxelChunk,
  worldGet: WorldGetBlock,
  face: FaceConfig,
  s: number,
  u0: number,
  v0: number,
  u1: number,
  v1: number,
  blockId: number,
  opaque: MeshData,
  transparent: MeshData,
  cutout: MeshData,
): void {
  const { axis, sign, uAxis, vAxis, winding, tile: tileSide } = face
  const def = getBlock(blockId)
  const target = def.material === 'opaque' ? opaque : def.material === 'transparent' ? transparent : cutout

  // Tile UVs from atlas
  const tileIndex = def.tiles[tileSide]
  const [tu0, tv0, tu1, tv1] = tileUV(tileIndex)
  const tuSpan = tu1 - tu0
  const tvSpan = tv1 - tv0
  const uSpan = u1 - u0
  const vSpan = v1 - v0

  // Normal vector (for the normal attribute)
  const nrm: [number, number, number] = [0, 0, 0]
  nrm[axis] = sign

  // For each of the 4 corners (du, dv) ∈ {0,1}²:
  //   - World position (corner of the face)
  //   - AO level (0..3) from 3 neighbors in the outside plane
  const positions: Array<[number, number, number]> = []
  const aos: number[] = []
  const uvs: Array<[number, number]> = []

  for (let dv = 0; dv < 2; dv++) {
    for (let du = 0; du < 2; du++) {
      // Corner world position
      const uCoord = du === 0 ? u0 : u1
      const vCoord = dv === 0 ? v0 : v1
      const nCoord = s + (sign === 1 ? 1 : 0) // face plane position along normalAxis
      const local: [number, number, number] = [0, 0, 0]
      local[axis] = nCoord
      local[uAxis] = uCoord
      local[vAxis] = vCoord
      const wx = chunk.originX + local[0]
      const wy = local[1]
      const wz = chunk.originZ + local[2]
      positions.push([wx, wy, wz])

      // AO samples — outside block at (s+sign, u_out, v_out)
      const uOut = du === 0 ? u0 : u1 - 1
      const vOut = dv === 0 ? v0 : v1 - 1
      const nOut = s + sign
      const outsideLocal: [number, number, number] = [0, 0, 0]
      outsideLocal[axis] = nOut
      outsideLocal[uAxis] = uOut
      outsideLocal[vAxis] = vOut

      const duSign = du * 2 - 1 // -1 or +1
      const dvSign = dv * 2 - 1

      const side1Local: [number, number, number] = [...outsideLocal]
      side1Local[uAxis] += duSign
      const side2Local: [number, number, number] = [...outsideLocal]
      side2Local[vAxis] += dvSign
      const cornerLocal: [number, number, number] = [...outsideLocal]
      cornerLocal[uAxis] += duSign
      cornerLocal[vAxis] += dvSign

      const s1 = aoOccludes(chunk, worldGet, side1Local[0], side1Local[1], side1Local[2])
      const s2 = aoOccludes(chunk, worldGet, side2Local[0], side2Local[1], side2Local[2])
      const cc = aoOccludes(chunk, worldGet, cornerLocal[0], cornerLocal[1], cornerLocal[2])
      const ao = aoLevel(s1, s2, cc)
      aos.push(ao)

      // UV: scale tile UV by quad span (texture tiles per block)
      const uu = du === 0 ? tu0 : tu0 + uSpan * tuSpan
      const vv = dv === 0 ? tv0 : tv0 + vSpan * tvSpan
      uvs.push([uu, vv])
    }
  }

  // Apply winding: corners are stored as [(0,0), (1,0), (1,1), (0,1)] indexed 0..3.
  // winding[] reorders them so the two triangles are CCW from outside.
  const w0 = winding[0]
  const w1 = winding[1]
  const w2 = winding[2]
  const w3 = winding[3]

  const baseIndex = target.positions.length / 3

  // Push 4 vertices
  for (let i = 0; i < 4; i++) {
    const p = positions[i]
    target.positions.push(p[0], p[1], p[2])
    target.normals.push(nrm[0], nrm[1], nrm[2])
    target.uvs.push(uvs[i][0], uvs[i][1])
    const b = AO_BRIGHTNESS[aos[i]]
    // Bake AO into vertex color. Slight per-block emissive lift so glow blocks
    // don't get crushed by AO.
    const lift = def.emissive * 0.5
    const c = Math.min(1, b + lift)
    target.colors.push(c, c, c)
  }

  // Two triangles
  target.indices.push(baseIndex + w0, baseIndex + w1, baseIndex + w2)
  target.indices.push(baseIndex + w0, baseIndex + w2, baseIndex + w3)
}
