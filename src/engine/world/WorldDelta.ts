/**
 * WorldDelta — port of dev.ergenverse.runtime.delta.WorldDelta
 *
 * One language for ALL changes. The journal never lies; every block change
 * in the world flows through this interface, tagged with its Provenance.
 *
 *   id          — stable packed position key (string form, e.g. "3842,72,-1184")
 *   type        — block id (string namespaced like "ergenverse:stone_bricks")
 *   provenance  — where this change came from
 *   apply(layer) — write this delta into a packed-pos map
 *   serialize()  — convert to JSON for save (replaces NBT in the Java version)
 */
import { Provenance } from './Provenance'

export interface WorldDelta {
  /** Stable position key — packed "x,y,z" string */
  readonly id: string
  /** Block id, namespaced "ergenverse:..." */
  readonly type: string
  /** Origin of this change */
  readonly provenance: Provenance
  /** Optional metadata (orientation, tile-entity state, etc.) */
  readonly data?: Record<string, unknown>
}

export interface PackedPos {
  x: number
  y: number
  z: number
}

export function packPos(x: number, y: number, z: number): string {
  return `${x | 0},${y | 0},${z | 0}`
}

export function unpackPos(id: string): PackedPos {
  const [x, y, z] = id.split(',').map((n) => parseInt(n, 10))
  return { x, y, z }
}

export function makeDelta(
  x: number,
  y: number,
  z: number,
  type: string,
  provenance: Provenance,
  data?: Record<string, unknown>,
): WorldDelta {
  return { id: packPos(x, y, z), type, provenance, data }
}

export function serializeDelta(d: WorldDelta): string {
  return JSON.stringify(d)
}

export function deserializeDelta(s: string): WorldDelta {
  return JSON.parse(s) as WorldDelta
}
