/**
 * RICanonicalDatabase loader — fetches the canon JSON snapshot at engine boot
 * and exposes typed accessors. Faithful to 仙逆; mod-original placements
 * (positions on the Planet Suzaku map) are flagged as such.
 *
 * The canon JSON ships at /public/ri_canon_database.json (a snapshot of the
 * Renegade Immortal research docs). Canon confidence levels (1-5) are
 * preserved exactly as attested.
 */
import type {
  CanonCharacter,
  CanonLocation,
  CanonArtifact,
  CanonTechnique,
  CanonFaction,
  CanonBeast,
  CanonHerb,
  SpiritVein,
  Landmark,
  RICanonicalDatabaseShape,
} from './types'

let _db: RICanonicalDatabaseShape | null = null
let _loadingPromise: Promise<RICanonicalDatabaseShape> | null = null

const CANON_JSON_URL = '/ri_canon_database.json'

/**
 * Load the canon database (idempotent; returns the cached promise on repeat
 * calls so we never fetch twice).
 */
export function loadCanonDatabase(): Promise<RICanonicalDatabaseShape> {
  if (_db) return Promise.resolve(_db)
  if (_loadingPromise) return _loadingPromise

  _loadingPromise = (async () => {
    const res = await fetch(CANON_JSON_URL)
    if (!res.ok) throw new Error(`Canon fetch failed: ${res.status}`)
    const raw = await res.json()
    _db = {
      characters: (raw.characters ?? []) as CanonCharacter[],
      locations: (raw.locations ?? []) as CanonLocation[],
      artifacts: (raw.artifacts ?? []) as CanonArtifact[],
      techniques: (raw.techniques ?? []) as CanonTechnique[],
      factions: (raw.factions ?? []) as CanonFaction[],
      beasts: (raw.beasts ?? []) as CanonBeast[],
      herbs: (raw.herbs ?? []) as CanonHerb[],
      spiritVeins: (raw.spiritVeins ?? []) as SpiritVein[],
      landmarks: (raw.landmarks ?? []) as Landmark[],
    }
    return _db
  })()

  return _loadingPromise
}

export function getCanonDatabase(): RICanonicalDatabaseShape | null {
  return _db
}

/** Look up a character by canon id (e.g. 'N01' = Wang Lin). */
export function getCharacter(id: string): CanonCharacter | undefined {
  return _db?.characters.find((c) => c.id === id)
}

/** Look up a location by canon id (e.g. 'L04' = Cave World). */
export function getLocation(id: string): CanonLocation | undefined {
  return _db?.locations.find((l) => l.id === id)
}

/** Find a character by English or Chinese name (case-insensitive). */
export function findCharacterByName(name: string): CanonCharacter | undefined {
  const lower = name.toLowerCase()
  return _db?.characters.find(
    (c) => c.name.toLowerCase() === lower || (c.nameCn && c.nameCn === name),
  )
}

/** Find a location by English or Chinese name (case-insensitive). */
export function findLocationByName(name: string): CanonLocation | undefined {
  const lower = name.toLowerCase()
  return _db?.locations.find(
    (l) => l.name.toLowerCase() === lower || (l.nameCn && l.nameCn === name),
  )
}
