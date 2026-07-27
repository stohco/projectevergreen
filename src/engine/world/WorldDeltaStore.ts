/**
 * WorldDeltaStore — port of dev.ergenverse.runtime.delta.WorldDeltaStore
 *
 * The journal: per-provenance packed-pos index + flat list.
 * serialize/deserialize to JSON (replaces NBT in the Java version).
 *
 * Gameplay NEVER touches the store directly — it writes through
 * WorldFacade.setPlayerBlock / setSimulationBlock (point 5).
 */
import { Provenance } from './Provenance'
import { packPos, type WorldDelta } from './WorldDelta'

export class WorldDeltaStore {
  private readonly byPos: Map<string, WorldDelta> = new Map()
  private readonly byProvenance: Map<Provenance, Map<string, WorldDelta>> = new Map([
    [Provenance.CANON, new Map()],
    [Provenance.SIMULATION, new Map()],
    [Provenance.PLAYER, new Map()],
  ])

  /** Returns true if a NEW delta was recorded (or an existing one updated). */
  record(delta: WorldDelta): boolean {
    const existing = this.byPos.get(delta.id)
    if (existing && existing.provenance === delta.provenance && existing.type === delta.type) {
      // Idempotent: same delta already in journal.
      return false
    }
    if (existing) {
      // A new provenance is overriding this position. Remove the old
      // provenance index entry so the journal stays consistent.
      this.byProvenance.get(existing.provenance)!.delete(delta.id)
    }
    this.byPos.set(delta.id, delta)
    this.byProvenance.get(delta.provenance)!.set(delta.id, delta)
    return true
  }

  get(id: string): WorldDelta | undefined {
    return this.byPos.get(id)
  }

  getByProvenance(p: Provenance): WorldDelta[] {
    return Array.from(this.byProvenance.get(p)!.values())
  }

  /** All deltas — for serialization. */
  all(): WorldDelta[] {
    return Array.from(this.byPos.values())
  }

  size(): number {
    return this.byPos.size
  }

  clear(): void {
    this.byPos.clear()
    this.byProvenance.forEach((m) => m.clear())
  }

  serialize(): string {
    return JSON.stringify({
      version: 1,
      deltas: this.all(),
    })
  }

  deserialize(s: string): void {
    this.clear()
    const parsed = JSON.parse(s) as { version: number; deltas: WorldDelta[] }
    for (const d of parsed.deltas) {
      this.record(d)
    }
  }

  /** LocalStorage-backed persistence (browser analog of SavedData). */
  saveToLocalStorage(key = 'ergenverse.worldstore'): void {
    try {
      localStorage.setItem(key, this.serialize())
    } catch (e) {
      console.warn('[WorldDeltaStore] save failed', e)
    }
  }

  loadFromLocalStorage(key = 'ergenverse.worldstore'): boolean {
    try {
      const s = localStorage.getItem(key)
      if (!s) return false
      this.deserialize(s)
      return true
    } catch (e) {
      console.warn('[WorldDeltaStore] load failed', e)
      return false
    }
  }

  clearLocalStorage(key = 'ergenverse.worldstore'): void {
    try {
      localStorage.removeItem(key)
    } catch {
      /* ignore */
    }
  }
}

export { packPos }
