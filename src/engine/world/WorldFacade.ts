/**
 * WorldFacade — port of dev.ergenverse.runtime.WorldFacade
 *
 * The invisible manager. Gameplay writes runtime.world().setPlayerBlock /
 * setSimulationBlock, NEVER touches the store or layers directly (point 5).
 *
 *   setPlayerBlock(x,y,z,type)        — record a PLAYER delta
 *   setSimulationBlock(x,y,z,type)    — record a SIMULATION delta
 *   getBlock(x,y,z)                   — composite query through all layers
 *
 * The player is a first-class actor: player actions flow through the
 * facade → journal → replay on reload.
 */
import { CompositeWorldLayer } from './WorldLayer'
import { WorldDeltaStore } from './WorldDeltaStore'
import { Provenance } from './Provenance'
import { makeDelta, type WorldDelta } from './WorldDelta'

export class WorldFacade {
  private readonly store: WorldDeltaStore
  private readonly composite: CompositeWorldLayer

  constructor(store: WorldDeltaStore, composite: CompositeWorldLayer) {
    this.store = store
    this.composite = composite
  }

  setPlayerBlock(x: number, y: number, z: number, type: string, data?: Record<string, unknown>): WorldDelta {
    const delta = makeDelta(x, y, z, type, Provenance.PLAYER, data)
    this.store.record(delta)
    return delta
  }

  setSimulationBlock(x: number, y: number, z: number, type: string, data?: Record<string, unknown>): WorldDelta {
    const delta = makeDelta(x, y, z, type, Provenance.SIMULATION, data)
    this.store.record(delta)
    return delta
  }

  /** Remove a player-placed block (record an AIR delta). */
  removePlayerBlock(x: number, y: number, z: number): WorldDelta {
    return this.setPlayerBlock(x, y, z, 'minecraft:air')
  }

  removeSimulationBlock(x: number, y: number, z: number): WorldDelta {
    return this.setSimulationBlock(x, y, z, 'minecraft:air')
  }

  /** Composite getBlock through all layers. */
  getBlock(x: number, y: number, z: number) {
    return this.composite.getBlock(x, y, z)
  }

  storeRef(): WorldDeltaStore {
    return this.store
  }

  compositeRef(): CompositeWorldLayer {
    return this.composite
  }

  /** Persist the journal (browser analog of SavedData). */
  save(): void {
    this.store.saveToLocalStorage()
  }

  /** Load the journal; returns true if anything was loaded. */
  load(): boolean {
    return this.store.loadFromLocalStorage()
  }

  /** New game: nuke the journal. The blueprint is NEVER modified. */
  reset(): void {
    this.store.clear()
    this.store.clearLocalStorage()
  }
}
