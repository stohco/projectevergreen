/**
 * VoxelChunk — 16x256x16 voxel storage.
 *
 * Each voxel is a uint16 block id (see BlockRegistry). The chunk is the unit
 * of greedy meshing and LOD streaming. Dirty flag drives re-mesh.
 *
 * Coordinate convention:
 *   - chunkX / chunkZ are world-chunk coords (block coord >> 4)
 *   - local x,y,z are 0..15 / 0..255 / 0..15
 *   - world coord = (chunkX * 16 + x, y, chunkZ * 16 + z)
 *
 * Storage: a Uint16Array of length CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z
 * = 65536 voxels = 128 KB per chunk. 17x17 chunks = ~37 MB; well within budget.
 *
 * NO canon chapter citations.
 */

export const CHUNK_SIZE_X = 16
export const CHUNK_SIZE_Y = 256
export const CHUNK_SIZE_Z = 16
export const CHUNK_AREA_XZ = CHUNK_SIZE_X * CHUNK_SIZE_Z
export const CHUNK_VOLUME = CHUNK_SIZE_X * CHUNK_SIZE_Y * CHUNK_SIZE_Z

export const SEA_LEVEL = 56

export class VoxelChunk {
  readonly chunkX: number
  readonly chunkZ: number
  /** Origin block coords (chunkX*16, 0, chunkZ*16). */
  readonly originX: number
  readonly originZ: number

  /** Packed voxel storage. 0 = AIR. */
  readonly voxels: Uint16Array

  /** Set true by any set(); cleared by the mesher after re-mesh. */
  dirty: boolean = true

  /** Set true if the chunk has been generated (terrain filled in). */
  generated: boolean = false

  /** Optional cached biome tag for HUD display. */
  biomeTag: string = 'plains'

  constructor(chunkX: number, chunkZ: number) {
    this.chunkX = chunkX
    this.chunkZ = chunkZ
    this.originX = chunkX * CHUNK_SIZE_X
    this.originZ = chunkZ * CHUNK_SIZE_Z
    this.voxels = new Uint16Array(CHUNK_VOLUME)
  }

  /** Local coord → flat index. Out-of-range returns -1. */
  private idx(x: number, y: number, z: number): number {
    if (x < 0 || x >= CHUNK_SIZE_X) return -1
    if (z < 0 || z >= CHUNK_SIZE_Z) return -1
    if (y < 0 || y >= CHUNK_SIZE_Y) return -1
    return (y * CHUNK_SIZE_Z + z) * CHUNK_SIZE_X + x
  }

  /** O(1) get. Returns 0 (AIR) for out-of-range. */
  get(x: number, y: number, z: number): number {
    const i = this.idx(x, y, z)
    return i < 0 ? 0 : this.voxels[i]
  }

  /** O(1) set. Marks dirty. */
  set(x: number, y: number, z: number, id: number): void {
    const i = this.idx(x, y, z)
    if (i < 0) return
    if (this.voxels[i] !== id) {
      this.voxels[i] = id
      this.dirty = true
    }
  }

  /** Bulk fill from a typed array (same length). Marks dirty. */
  fill(src: Uint16Array): void {
    if (src.length !== CHUNK_VOLUME) {
      throw new Error(`fill: expected ${CHUNK_VOLUME} voxels, got ${src.length}`)
    }
    this.voxels.set(src)
    this.dirty = true
  }

  /** Convenience: world block coord → chunk + local coords. */
  static worldToChunk(wx: number, wz: number): [number, number] {
    return [Math.floor(wx / CHUNK_SIZE_X), Math.floor(wz / CHUNK_SIZE_Z)]
  }

  /** Convenience: world block coord → local coord inside this chunk (or null). */
  worldToLocal(wx: number, wy: number, wz: number): [number, number, number] | null {
    const lx = wx - this.originX
    const lz = wz - this.originZ
    if (lx < 0 || lx >= CHUNK_SIZE_X) return null
    if (lz < 0 || lz >= CHUNK_SIZE_Z) return null
    if (wy < 0 || wy >= CHUNK_SIZE_Y) return null
    return [lx, wy, lz]
  }

  /** Iterate non-air voxels in y-sorted order. Used by the mesher. */
  forEachSolid(fn: (x: number, y: number, z: number, id: number) => void): void {
    const v = this.voxels
    for (let y = 0; y < CHUNK_SIZE_Y; y++) {
      const yBase = y * CHUNK_AREA_XZ
      for (let z = 0; z < CHUNK_SIZE_Z; z++) {
        const zBase = yBase + z * CHUNK_SIZE_X
        for (let x = 0; x < CHUNK_SIZE_X; x++) {
          const id = v[zBase + x]
          if (id !== 0) fn(x, y, z, id)
        }
      }
    }
  }

  /** Highest non-air y at local (x, z), or -1 if column is empty. */
  highestY(x: number, z: number): number {
    for (let y = CHUNK_SIZE_Y - 1; y >= 0; y--) {
      if (this.get(x, y, z) !== 0) return y
    }
    return -1
  }
}
