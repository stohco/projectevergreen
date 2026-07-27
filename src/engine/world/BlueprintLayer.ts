/**
 * BlueprintLayer — port of dev.ergenverse.runtime.layer.BlueprintLayer
 *
 * The blueprint is a DESCRIPTION, not a per-block truth. getBlock always
 * returns null — it answers higher-level queries (queryStructures,
 * queryActors, querySpiritVeins, queryLandmarks).
 *
 * Point 8 of the CRON-69 ten-point architecture: "The blueprint never
 * answers getBlock — it answers higher-level queries."
 *
 * The blueprint is NEVER modified. Every new save starts from identical
 * canon; divergence lives in the WorldDeltaStore journal.
 */
import type { WorldLayer, BlockQueryResult, StructureBox } from './WorldLayer'
import { Provenance } from './Provenance'
import type { CanonActor, SpiritVein, Landmark } from '../canon/types'

export class BlueprintLayer implements WorldLayer {
  readonly provenance = Provenance.CANON

  private readonly structures: StructureBox[] = []
  private readonly actors: CanonActor[] = []
  private readonly spiritVeins: SpiritVein[] = []
  private readonly landmarks: Landmark[] = []

  registerStructure(s: StructureBox): void {
    this.structures.push(s)
  }
  registerActor(a: CanonActor): void {
    this.actors.push(a)
  }
  registerSpiritVein(v: SpiritVein): void {
    this.spiritVeins.push(v)
  }
  registerLandmark(l: Landmark): void {
    this.landmarks.push(l)
  }

  /** ALWAYS null. Blueprint never answers per-block. */
  getBlock(_x: number, _y: number, _z: number): BlockQueryResult {
    return null
  }

  queryStructures(chunkX: number, chunkZ: number): StructureBox[] {
    const minCx = chunkX * 16
    const minCz = chunkZ * 16
    const maxCx = minCx + 15
    const maxCz = minCz + 15
    return this.structures.filter(
      (s) =>
        s.maxX >= minCx &&
        s.minX <= maxCx &&
        s.maxZ >= minCz &&
        s.minZ <= maxCz,
    )
  }

  queryActors(chunkX: number, chunkZ: number): CanonActor[] {
    const minCx = chunkX * 16
    const minCz = chunkZ * 16
    const maxCx = minCx + 15
    const maxCz = minCz + 15
    return this.actors.filter((a) => {
      const x = a.position[0]
      const z = a.position[2]
      return x >= minCx && x <= maxCx && z >= minCz && z <= maxCz
    })
  }

  querySpiritVeins(chunkX: number, chunkZ: number): SpiritVein[] {
    const minCx = chunkX * 16
    const minCz = chunkZ * 16
    const maxCx = minCx + 15
    const maxCz = minCz + 15
    return this.spiritVeins.filter((v) => {
      const x = v.position[0]
      const z = v.position[2]
      return x >= minCx && x <= maxCx && z >= minCz && z <= maxCz
    })
  }

  queryLandmarks(chunkX: number, chunkZ: number): Landmark[] {
    const minCx = chunkX * 16
    const minCz = chunkZ * 16
    const maxCx = minCx + 15
    const maxCz = minCz + 15
    return this.landmarks.filter((l) => {
      const x = l.position[0]
      const z = l.position[2]
      return x >= minCx && x <= maxCx && z >= minCz && z <= maxCz
    })
  }

  allStructures(): StructureBox[] {
    return [...this.structures]
  }
  allActors(): CanonActor[] {
    return [...this.actors]
  }
  allSpiritVeins(): SpiritVein[] {
    return [...this.spiritVeins]
  }
  allLandmarks(): Landmark[] {
    return [...this.landmarks]
  }
}
