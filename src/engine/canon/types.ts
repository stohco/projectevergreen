/**
 * Canon data types — ported from dev.ergenverse.canon.* Java package.
 * Faithful to the 仙逆 novel; NO invented chapter citations.
 */

export type Vec3 = [number, number, number]

export type CanonConfidence = 1 | 2 | 3 | 4 | 5
// 5 = CANON_CONCRETE (explicitly attested)
// 4 = CANON_IMPLIED (strongly implied)
// 3 = REASONABLE_RECONSTRUCTION (canon silent; logically necessary)
// 1-2 = SPECULATION (only when needed; never overwrites canon)

export interface CanonCharacter {
  id: string
  name: string
  nameCn?: string
  type: string // protagonist | antagonist | ally | sect_leader | mortal | beast_companion
  peakRealm: string
  affiliation: string
  status: string
  canonConfidence: CanonConfidence
  location: string
  knownFacts: string[]
  relationships: Array<{ target: string; relation: string }>
  /** Mod-original placement on Planet Suzaku (mod-original; flagged). */
  position?: Vec3
  /** Cultivation realm at simulation start. */
  startingRealm?: string
}

export interface CanonLocation {
  id: string
  name: string
  nameCn?: string
  type: string // settlement | sect | region | country | planet | cave | valley | mountain | lake
  parent: string
  description: string
  canonConfidence: CanonConfidence
  /** Mod-original placement on the Planet Suzaku map. */
  position?: Vec3
  /** Approximate radius/bounds for the location. */
  radius?: number
  /** Spiritual qi density (1-10). */
  qiDensity?: number
}

export interface CanonArtifact {
  id: string
  name: string
  nameCn?: string
  type: string // flying_sword | bead | talisman | flag | scroll | banner | pill | formation
  owner: string
  description: string
  canonConfidence: CanonConfidence
  knownFacts: string[]
}

export interface CanonTechnique {
  id: string
  name: string
  nameCn?: string
  type: string // cultivation_method | spell | secret_art | body_refinement | divine_ability
  practitioner: string
  description: string
  canonConfidence: CanonConfidence
  knownFacts: string[]
}

export interface CanonActor {
  /** Stable UUID; equals the persistence UUID of the materialized entity. */
  id: string
  /** Display name (English). */
  name: string
  nameCn?: string
  /** Position on Planet Suzaku. */
  position: Vec3
  /** Canon character id (e.g. N01) — for graph linkage. */
  canonId?: string
  /** Starting cultivation realm. */
  realm: string
  /** Affiliation string. */
  faction: string
  /** Hostility 0-100 toward player at sim start. */
  hostility: number
  /** Optional model key for the renderer. */
  modelKey?: string
}

export interface SpiritVein {
  id: string
  name: string
  position: Vec3
  /** Quality 1-10 (higher = richer qi). */
  quality: number
  /** Element affinity (fire, water, wood, metal, earth, lightning, etc.). */
  element: string
  /** Faction that controls this vein, if any. */
  controller?: string
}

export interface Landmark {
  id: string
  name: string
  position: Vec3
  kind: string // cliff | cave | waterfall | shrine | battlefield | ancient_ruin
  description: string
  canonConfidence: CanonConfidence
}

export interface CanonFaction {
  id: string
  name: string
  nameCn?: string
  type: string // sect | clan | country | alliance | merchant_house
  leader: string
  alignment: string // righteous | demonic | neutral | beast
  description: string
  canonConfidence: CanonConfidence
  headquarters: string
}

export interface CanonBeast {
  id: string
  name: string
  nameCn?: string
  rank: string // rank_1 .. rank_9 (corresponds to cultivation realm equivalents)
  element: string
  habitat: string
  description: string
  canonConfidence: CanonConfidence
  /** Aggression 0-100. */
  aggression: number
  /** Preferred model key. */
  modelKey?: string
}

export interface CanonHerb {
  id: string
  name: string
  nameCn?: string
  rank: string
  element: string
  growth: string
  description: string
  canonConfidence: CanonConfidence
}

/**
 * RICanonicalDatabase — total in-memory canon store.
 * Filled at engine boot from a JSON snapshot of the canon research docs.
 */
export interface RICanonicalDatabaseShape {
  characters: CanonCharacter[]
  locations: CanonLocation[]
  artifacts: CanonArtifact[]
  techniques: CanonTechnique[]
  factions: CanonFaction[]
  beasts: CanonBeast[]
  herbs: CanonHerb[]
  spiritVeins: SpiritVein[]
  landmarks: Landmark[]
}
