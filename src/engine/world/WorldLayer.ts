/**
 * WorldLayer + CompositeWorldLayer — port of dev.ergenverse.runtime.layer
 *
 * Composable layers, NO hardcoded priority — insert a QuestLayer later.
 * Each layer is a stateless view over a backing store of WorldDeltas.
 *
 *   getBlock(x,y,z)         — answers per-block "what does this layer say?"
 *   queryStructures(chunkX, chunkZ) — structure-granularity answers
 *
 * The BlueprintLayer is special: getBlock always returns null (blueprint is a
 * DESCRIPTION, not a per-block truth). It answers higher-level queries.
 */
import { Provenance, PROVENANCE_PRIORITY } from './Provenance'
import { packPos, unpackPos, type WorldDelta } from './WorldDelta'

export type BlockQueryResult = { type: string; data?: Record<string, unknown> } | null

export interface StructureBox {
  minX: number
  minY: number
  minZ: number
  maxX: number
  maxY: number
  maxZ: number
  kind: string
  name: string
  canonId?: string
}

export interface WorldLayer {
  readonly provenance: Provenance
  /** Per-block truth from this layer (or null = "this layer has nothing to say here"). */
  getBlock(x: number, y: number, z: number): BlockQueryResult
  /** What structures does this layer contribute to this chunk? */
  queryStructures(chunkX: number, chunkZ: number): StructureBox[]
}

/**
 * A stateless view over a delta-store: it indexes WorldDeltas by packed-pos
 * for O(1) getBlock. Used by PlayerLayer and SimulationLayer.
 */
export class DeltaViewLayer implements WorldLayer {
  readonly provenance: Provenance
  private readonly blocks: Map<string, WorldDelta> = new Map()

  constructor(provenance: Provenance) {
    this.provenance = provenance
  }

  ingest(delta: WorldDelta): void {
    if (delta.provenance !== this.provenance) {
      throw new Error(
        `Provenance mismatch: layer ${this.provenance} got delta ${delta.provenance}`,
      )
    }
    this.blocks.set(delta.id, delta)
  }

  /** A delta of { type: 'minecraft:air' } means 'remove the block here'. */
  remove(id: string): void {
    this.blocks.delete(id)
  }

  getBlock(x: number, y: number, z: number): BlockQueryResult {
    const d = this.blocks.get(packPos(x, y, z))
    if (!d) return null
    if (d.type === 'minecraft:air' || d.type === 'ergenverse:air') return null
    return { type: d.type, data: d.data }
  }

  queryStructures(_chunkX: number, _chunkZ: number): StructureBox[] {
    // Player/Sim layers are per-block, they don't define structures.
    return []
  }

  /** Snapshot for serialization. */
  serialize(): WorldDelta[] {
    return Array.from(this.blocks.values())
  }

  /** Restore from a serialized snapshot. */
  deserialize(deltas: WorldDelta[]): void {
    this.blocks.clear()
    for (const d of deltas) this.blocks.set(d.id, d)
  }
}

/**
 * CompositeWorldLayer — composes multiple layers in priority order.
 * Default order: CANON (blueprint terrain) → SIMULATION → PLAYER.
 * The composition is: walk layers in priority order, first layer that
 * has a non-null answer wins. (Player overrides Sim overrides Canon.)
 */
export class CompositeWorldLayer {
  private readonly layers: WorldLayer[] = []

  addLayer(layer: WorldLayer): void {
    this.layers.push(layer)
    // Stable sort by priority so insertion order is preserved within a tier.
    this.layers.sort((a, b) => PROVENANCE_PRIORITY[a.provenance] - PROVENANCE_PRIORITY[b.provenance])
  }

  getBlock(x: number, y: number, z: number): BlockQueryResult {
    for (const layer of this.layers) {
      const r = layer.getBlock(x, y, z)
      if (r !== null) return r
    }
    return null
  }

  /** All structures any layer wants to contribute to this chunk. */
  queryStructures(chunkX: number, chunkZ: number): StructureBox[] {
    const out: StructureBox[] = []
    for (const layer of this.layers) {
      out.push(...layer.queryStructures(chunkX, chunkZ))
    }
    return out
  }

  /** Layers in priority order (for materializer). */
  ordered(): WorldLayer[] {
    return [...this.layers]
  }
}

/** Pack a BlockPos for stable keying in any Map<string, _>. */
export { packPos, unpackPos }
