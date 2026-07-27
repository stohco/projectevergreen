/**
 * ChunkManager — LOD chunk streaming around the player.
 *
 * Loads/unloads chunks based on distance. Render distance: 8 chunks near
 * (full LOD), 16 chunks far (lower detail — for now, just culled). Chunk
 * generation is queued and processed N per frame to avoid hitches.
 *
 * Each loaded chunk has:
 *   - VoxelChunk (data)
 *   - THREE.Mesh (rendered geometry from the greedy mesher)
 *
 * Chunks are keyed by `${chunkX},${chunkZ}` for O(1) lookup.
 */
import * as THREE from 'three'
import { VoxelChunk, CHUNK_SIZE_X, CHUNK_SIZE_Y, CHUNK_SIZE_Z } from './VoxelChunk'
import { meshChunk, type MeshData } from './VoxelMesher'
import { generateChunk } from '../terrain/DeterministicTerrainGenerator'
import { getAlbedoAtlas, getNormalAtlas } from '../render/ProceduralTextures'

const NEAR_RENDER_DISTANCE = 8 // chunks
const FAR_RENDER_DISTANCE = 16 // chunks (just terrain, no deco skew)
const MAX_GEN_PER_FRAME = 2
const MAX_MESH_PER_FRAME = 2

interface LoadedChunk {
  chunk: VoxelChunk
  meshOpaque: THREE.Mesh | null
  meshTransparent: THREE.Mesh | null
  meshCutout: THREE.Mesh | null
  lastUsed: number
}

export class ChunkManager {
  private readonly scene: THREE.Scene
  private readonly loaded: Map<string, LoadedChunk> = new Map()
  private readonly genQueue: Array<{ cx: number; cz: number; dist: number }> = []
  private readonly meshQueue: Array<string> = []
  private readonly material: THREE.Material
  private readonly transparentMaterial: THREE.Material
  private readonly cutoutMaterial: THREE.Material
  private cameraX = 0
  private cameraZ = 0
  private frame = 0

  constructor(scene: THREE.Scene) {
    this.scene = scene
    const albedo = getAlbedoAtlas()
    const normal = getNormalAtlas()
    const makeMat = (opts: THREE.MeshStandardMaterialParameters) =>
      new THREE.MeshStandardMaterial({
        map: albedo,
        normalMap: normal,
        normalScale: new THREE.Vector2(1.0, 1.0),
        roughness: 0.95,
        metalness: 0.0,
        vertexColors: true,
        ...opts,
      })
    this.material = makeMat({})
    this.transparentMaterial = makeMat({
      transparent: true,
      opacity: 0.78,
      depthWrite: false,
      side: THREE.DoubleSide,
    })
    this.cutoutMaterial = makeMat({
      transparent: true,
      alphaTest: 0.5,
      side: THREE.DoubleSide,
    })
  }

  /** Update camera position (world block coords). Triggers stream updates. */
  updateCamera(wx: number, wz: number): void {
    this.cameraX = wx
    this.cameraZ = wz
  }

  /** Per-frame update: process queues, unload distant chunks. */
  update(_dt: number): void {
    this.frame++
    // 1. Rebuild gen queue if we're at the start of a stream cycle.
    if (this.genQueue.length === 0) {
      this.refillGenQueue()
    }
    // 2. Process up to MAX_GEN_PER_FRAME generations.
    let gen = 0
    while (gen < MAX_GEN_PER_FRAME && this.genQueue.length > 0) {
      const job = this.genQueue.shift()!
      const key = `${job.cx},${job.cz}`
      if (this.loaded.has(key)) continue
      const chunk = new VoxelChunk(job.cx, job.cz)
      generateChunk(chunk, job.cx, job.cz)
      this.loaded.set(key, {
        chunk,
        meshOpaque: null,
        meshTransparent: null,
        meshCutout: null,
        lastUsed: this.frame,
      })
      this.meshQueue.push(key)
      gen++
    }
    // 3. Process up to MAX_MESH_PER_FRAME mesh builds.
    let mesh = 0
    while (mesh < MAX_MESH_PER_FRAME && this.meshQueue.length > 0) {
      const key = this.meshQueue.shift()!
      const lc = this.loaded.get(key)
      if (!lc) continue
      this.buildMesh(lc)
      mesh++
    }
    // 4. Unload distant chunks.
    this.unloadDistant()
  }

  private refillGenQueue(): void {
    const ccx = Math.floor(this.cameraX / CHUNK_SIZE_X)
    const ccz = Math.floor(this.cameraZ / CHUNK_SIZE_Z)
    const jobs: Array<{ cx: number; cz: number; dist: number }> = []
    for (let dz = -NEAR_RENDER_DISTANCE; dz <= NEAR_RENDER_DISTANCE; dz++) {
      for (let dx = -NEAR_RENDER_DISTANCE; dx <= NEAR_RENDER_DISTANCE; dx++) {
        const dist = Math.sqrt(dx * dx + dz * dz)
        if (dist > NEAR_RENDER_DISTANCE) continue
        const cx = ccx + dx
        const cz = ccz + dz
        const key = `${cx},${cz}`
        if (this.loaded.has(key)) continue
        jobs.push({ cx, cz, dist })
      }
    }
    // Sort nearest-first.
    jobs.sort((a, b) => a.dist - b.dist)
    this.genQueue.push(...jobs)
  }

  private buildMesh(lc: LoadedChunk): void {
    // Dispose any existing meshes.
    this.disposeMeshes(lc)
    const worldGet = (wx: number, wy: number, wz: number) => this.getBlock(wx, wy, wz)
    const { opaque, transparent, cutout } = meshChunk(lc.chunk, worldGet)
    if (opaque) {
      const geo = meshDataToGeometry(opaque)
      const m = new THREE.Mesh(geo, this.material)
      m.position.set(0, 0, 0) // positions are already world-space
      m.castShadow = true
      m.receiveShadow = true
      m.frustumCulled = true
      this.scene.add(m)
      lc.meshOpaque = m
    }
    if (transparent) {
      const geo = meshDataToGeometry(transparent)
      const m = new THREE.Mesh(geo, this.transparentMaterial)
      m.position.set(0, 0, 0)
      m.receiveShadow = true
      m.frustumCulled = true
      this.scene.add(m)
      lc.meshTransparent = m
    }
    if (cutout) {
      const geo = meshDataToGeometry(cutout)
      const m = new THREE.Mesh(geo, this.cutoutMaterial)
      m.position.set(0, 0, 0)
      m.castShadow = true
      m.receiveShadow = true
      m.frustumCulled = true
      this.scene.add(m)
      lc.meshCutout = m
    }
    lc.chunk.dirty = false
  }

  private disposeMeshes(lc: LoadedChunk): void {
    for (const key of ['meshOpaque', 'meshTransparent', 'meshCutout'] as const) {
      const m = lc[key]
      if (m) {
        this.scene.remove(m)
        m.geometry.dispose()
        lc[key] = null
      }
    }
  }

  private unloadDistant(): void {
    const ccx = Math.floor(this.cameraX / CHUNK_SIZE_X)
    const ccz = Math.floor(this.cameraZ / CHUNK_SIZE_Z)
    const maxDist = FAR_RENDER_DISTANCE
    for (const [key, lc] of this.loaded) {
      const dx = lc.chunk.chunkX - ccx
      const dz = lc.chunk.chunkZ - ccz
      const dist = Math.sqrt(dx * dx + dz * dz)
      if (dist > maxDist) {
        this.disposeMeshes(lc)
        this.loaded.delete(key)
      }
    }
  }

  /** Get a loaded chunk by world block coords (or null). */
  getChunkAt(wx: number, wz: number): VoxelChunk | null {
    const cx = Math.floor(wx / CHUNK_SIZE_X)
    const cz = Math.floor(wz / CHUNK_SIZE_Z)
    return this.loaded.get(`${cx},${cz}`)?.chunk ?? null
  }

  /** Get a loaded chunk by chunk coords (or null). */
  getChunk(cx: number, cz: number): VoxelChunk | null {
    return this.loaded.get(`${cx},${cz}`)?.chunk ?? null
  }

  /** Read a block from world coords (0 = air if chunk not loaded). */
  getBlock(wx: number, wy: number, wz: number): number {
    if (wy < 0 || wy >= CHUNK_SIZE_Y) return 0
    const chunk = this.getChunkAt(wx, wz)
    if (!chunk) return 0
    const local = chunk.worldToLocal(wx, wy, wz)
    if (!local) return 0
    return chunk.get(local[0], local[1], local[2])
  }

  /** Set a block at world coords (records a player delta; chunk re-meshes). */
  setBlock(wx: number, wy: number, wz: number, id: number): void {
    if (wy < 0 || wy >= CHUNK_SIZE_Y) return
    const chunk = this.getChunkAt(wx, wz)
    if (!chunk) return
    const local = chunk.worldToLocal(wx, wy, wz)
    if (!local) return
    chunk.set(local[0], local[1], local[2], id)
    // Queue re-mesh.
    const key = `${chunk.chunkX},${chunk.chunkZ}`
    if (!this.meshQueue.includes(key)) this.meshQueue.unshift(key)
  }

  loadedCount(): number {
    return this.loaded.size
  }

  /** Total triangle count across all loaded chunk meshes (for HUD). */
  triangleCount(): number {
    let n = 0
    for (const lc of this.loaded.values()) {
      for (const m of [lc.meshOpaque, lc.meshTransparent, lc.meshCutout]) {
        if (m && m.geometry.index) n += m.geometry.index.count / 3
        else if (m && m.geometry.attributes.position) n += m.geometry.attributes.position.count / 3
      }
    }
    return Math.floor(n)
  }
}

/** Convert mesher MeshData (flat arrays) to a Three.js BufferGeometry. */
function meshDataToGeometry(data: MeshData): THREE.BufferGeometry {
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.Float32BufferAttribute(data.positions, 3))
  geo.setAttribute('normal', new THREE.Float32BufferAttribute(data.normals, 3))
  geo.setAttribute('uv', new THREE.Float32BufferAttribute(data.uvs, 2))
  geo.setAttribute('color', new THREE.Float32BufferAttribute(data.colors, 3))
  geo.setIndex(data.indices)
  geo.computeBoundingSphere()
  return geo
}
