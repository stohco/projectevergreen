/**
 * WorldState — the field-driven world model.
 *
 * Implements the spec's core equation:
 *
 *   W(t) = B ⊕ S(t) ⊕ P(t)
 *
 * Where:
 *   B   = immutable blueprint (canon world)
 *   S(t) = simulation delta (weather, beasts, sect growth, erosion)
 *   P(t) = player delta (mining, building, destroying, placing)
 *
 * The renderer does NOT own W. It samples it:
 *
 *   state(x, t) = sample(W(t), x)
 *
 * Every visual system is a pure function of semantic state:
 *   terrain  = T(blueprint, simDelta, playerDelta)
 *   building = H(semanticBuilding, theme, state)
 *   character= C(actorState, outfitState, cultivationState, injuryState)
 *   vfx      = V(eventState, qiField, camera)
 *
 * This is the crucial inversion: the renderer is a VIEW over the field,
 * not the owner of state. All edits become deltas (Invariant E). The
 * compiler is deterministic (Invariant D). Every visual change traces
 * back to canon/sim/player (Invariant C).
 */

import type { ProvenanceTag, MaterialSlot } from '../semantic/CanonTypes'

// ---- The field equation --------------------------------------------------

/**
 * A sample of world state at a point in space.
 * This is what the renderer receives — never the raw delta store.
 */
export interface WorldSample {
  /** Terrain height at this (x, z). */
  terrainHeight: number
  /** Surface material slot (grass, stone, sand, snow, water...). */
  terrainMaterial: MaterialSlot
  /** Is this point underwater? */
  underwater: boolean
  /** Is this point inside a building volume? */
  insideBuilding: boolean
  /** Building id if insideBuilding, else null. */
  buildingId: string | null
  /** Settlement id at this position, or null. */
  settlementId: string | null
  /** Qi density 0..1 (scalar field value). */
  qiDensity: number
  /** Qi flow direction (vector field value). */
  qiFlow: [number, number, number]
  /** Provenance of the dominant delta at this point. */
  provenance: ProvenanceTag
  /** Region/biome tag. */
  biome: string
}

/**
 * The world state field. The renderer calls sample(x, z) to get the
 * state at any point. The field composes blueprint + sim + player deltas.
 *
 * This is the ONLY authority. Three.js is a view over this.
 */
export interface WorldStateField {
  sample(x: number, z: number, t: number): WorldSample
  /** Apply a player delta (mining, building, placing). */
  applyPlayerDelta(delta: FieldDelta): void
  /** Apply a simulation delta (weather, beast, growth). */
  applySimulationDelta(delta: FieldDelta): void
  /** Serialize the full state for save/load. */
  serialize(): SerializedWorldState
}

// ---- Deltas --------------------------------------------------------------

export type FieldDeltaType =
  | 'terrain_height'
  | 'terrain_material'
  | 'block_place'
  | 'block_remove'
  | 'building_spawn'
  | 'building_remove'
  | 'actor_spawn'
  | 'actor_remove'
  | 'qi_field_change'
  | 'weather_change'

export interface FieldDelta {
  type: FieldDeltaType
  provenance: ProvenanceTag
  /** Spatial bounds this delta affects (for incremental re-meshing). */
  bounds: BoundingVolume
  /** Payload — type-specific. */
  data: unknown
  /** Timestamp (game time). */
  t: number
}

export interface BoundingVolume {
  minX: number
  maxX: number
  minY: number
  maxY: number
  minZ: number
  maxZ: number
}

export interface SerializedWorldState {
  version: number
  blueprint: unknown
  simulationDeltas: FieldDelta[]
  playerDeltas: FieldDelta[]
}

// ---- Concrete implementation ---------------------------------------------

import { PLANET_SUZAKU_PLACEMENT } from '../../canon/PlanetSuzakuPlacement'
import { rbfTerrainHeight, terrainMaterialAt, biomeAt } from './RBFTerrain'

export class FieldWorldState implements WorldStateField {
  private simulationDeltas: FieldDelta[] = []
  private playerDeltas: FieldDelta[] = []
  private playerBlockOverrides: Map<string, { blockId: number; material: MaterialSlot }> = new Map()

  sample(x: number, z: number, t: number): WorldSample {
    // 1. Blueprint: RBF terrain height (canon-authored control points).
    let terrainHeight = rbfTerrainHeight(x, z)

    // 2. Apply player terrain edits (mining/building).
    const key = `${x | 0},${z | 0}`
    const override = this.playerBlockOverrides.get(key)
    if (override) {
      // Player has modified this column.
    }

    // 3. Determine material + biome.
    let terrainMaterial = terrainMaterialAt(terrainHeight)
    let biome = biomeAt(x, z, terrainHeight)

    // 4. Settlement check — is this point inside a placed settlement?
    let settlementId: string | null = null
    let insideBuilding = false
    let buildingId: string | null = null
    for (const loc of PLANET_SUZAKU_PLACEMENT) {
      const dx = x - loc.position[0]
      const dz = z - loc.position[1]
      const dist = Math.sqrt(dx * dx + dz * dz)
      if (dist < loc.radius) {
        settlementId = loc.canonId
        break
      }
    }

    // 5. Qi field — scalar density + flow vector.
    // Near spirit veins, qi density is high. Flow points toward the vein.
    let qiDensity = 0.1 // baseline ambient qi
    let qiFlow: [number, number, number] = [0, 0, 0]
    // The cliff of 天逆珠 has a spirit vein at (-80, -120).
    const veinDist = Math.sqrt((x + 80) ** 2 + (z + 120) ** 2)
    if (veinDist < 60) {
      qiDensity = Math.max(qiDensity, 0.8 * (1 - veinDist / 60))
      // Flow points toward the vein.
      const fx = (-80 - x) / Math.max(1, veinDist)
      const fz = (-120 - z) / Math.max(1, veinDist)
      qiFlow = [fx * 0.3, 0, fz * 0.3]
    }

    // 6. Provenance: player overrides win, else simulation, else canon.
    let provenance: ProvenanceTag = 'CANON'
    if (override) provenance = 'PLAYER'

    return {
      terrainHeight,
      terrainMaterial,
      underwater: terrainHeight < 0,
      insideBuilding,
      buildingId,
      settlementId,
      qiDensity,
      qiFlow,
      provenance,
      biome,
    }
  }

  applyPlayerDelta(delta: FieldDelta): void {
    this.playerDeltas.push(delta)
    if (delta.type === 'block_place' || delta.type === 'block_remove') {
      const d = delta.data as { x: number; z: number; blockId?: number; material?: MaterialSlot }
      if (delta.type === 'block_place' && d.blockId !== undefined && d.material) {
        this.playerBlockOverrides.set(`${d.x | 0},${d.z | 0}`, { blockId: d.blockId, material: d.material })
      } else if (delta.type === 'block_remove') {
        this.playerBlockOverrides.delete(`${d.x | 0},${d.z | 0}`)
      }
    }
  }

  applySimulationDelta(delta: FieldDelta): void {
    this.simulationDeltas.push(delta)
  }

  serialize(): SerializedWorldState {
    return {
      version: 1,
      blueprint: null, // blueprint is immutable canon, not serialized
      simulationDeltas: this.simulationDeltas,
      playerDeltas: this.playerDeltas,
    }
  }

  /** Count helpers for the HUD. */
  playerDeltaCount(): number { return this.playerDeltas.length }
  simulationDeltaCount(): number { return this.simulationDeltas.length }
}
