/**
 * Semantic world model — pure data types for the meaningful world.
 *
 * Port of the formal spec §5.1. These types describe WHAT exists in the
 * world (a settlement, a building, a room, a bed) without saying HOW it
 * renders. The template library + compiler turn these into geometry.
 *
 * NO Three.js imports. NO block coordinates. NO meshes. Pure semantics.
 * This is the layer that survives engine changes, art direction changes,
 * and rendering technology changes.
 *
 * Canon fidelity: settlements come from the canon database (赵国偏僻山村 =
 * Wang Lin's birthplace). Buildings are mod-original reconstructions
 * (REASONABLE_RECONSTRUCTION, conf 3) — the novel attests Wang Lin grew
 * up in a remote mountain village but does not specify floor plans.
 */

/** Stable identifier for any canon object. */
export type CanonId = string

/** Material slot — symbolic, resolved by theme. */
export type MaterialSlot =
  | 'WALL'
  | 'FLOOR'
  | 'ROOF'
  | 'PILLAR'
  | 'DOOR'
  | 'BED'
  | 'MAT'
  | 'LAMP'
  | 'SPIRIT_STONE'
  | 'WOOD'
  | 'STONE'
  | 'JADE'
  | 'PAPER'
  | 'THATCH'
  | 'COBBLE'
  | 'GRASS'
  | 'DIRT'
  | 'WATER'

/** Provenance — where a piece of state came from. */
export type ProvenanceTag = 'CANON' | 'SIMULATION' | 'PLAYER'

// ---- Settlement ----------------------------------------------------------

export interface CanonSettlement {
  id: CanonId
  name: string
  nameCn?: string
  /** Parent location (e.g. 'Zhao Country'). */
  parentLocationId?: CanonId
  /** Region identifier. */
  regionId: CanonId
  /** World position [x, y, z] — mod-original placement. */
  position: [number, number, number]
  /** Approximate radius. */
  radius: number
  /** Canon status. */
  canonStatus: 'canon' | 'mod_original' | 'unverified'
  buildings: CanonBuilding[]
  /** Roads connecting buildings within the settlement. */
  roads: CanonRoad[]
  /** Spirit veins near the settlement. */
  spiritVeins: CanonSpiritVein[]
}

// ---- Building ------------------------------------------------------------

export interface CanonBuilding {
  id: CanonId
  name: string
  nameCn?: string
  ownerId?: CanonId
  purpose: BuildingPurpose
  rooms: CanonRoom[]
  /** Theme key — resolved by the template library. */
  shellTheme: string
  /** World position [x, y, z] relative to settlement origin. */
  position: [number, number, number]
  /** Y-axis rotation in degrees. */
  rotation: number
  /** Building footprint [width, depth, height] in meters. */
  size: [number, number, number]
}

export type BuildingPurpose =
  | 'home'
  | 'alchemy'
  | 'storage'
  | 'training'
  | 'administration'
  | 'shrine'
  | 'well'
  | 'gate'
  | 'fence'
  | 'other'

// ---- Room ----------------------------------------------------------------

export interface CanonRoom {
  id: CanonId
  name: string
  function: RoomFunction
  ownerId?: CanonId
  furniture: CanonFurniture[]
  anchors: RoomAnchor[]
  /** Position within the building [x, y, z]. */
  position: [number, number, number]
  /** Size [width, height, depth]. */
  size: [number, number, number]
}

export type RoomFunction =
  | 'bedroom'
  | 'kitchen'
  | 'alchemy_lab'
  | 'courtyard'
  | 'storage'
  | 'hall'
  | 'meditation'
  | 'other'

// ---- Furniture -----------------------------------------------------------

export interface CanonFurniture {
  id: CanonId
  kind: FurnitureKind
  tags: string[]
  /** Position within the room [x, y, z]. */
  position: [number, number, number]
  /** Y-axis rotation in degrees. */
  rotation?: number
}

export type FurnitureKind =
  | 'BED'
  | 'MEDITATION_MAT'
  | 'BOOKSHELF'
  | 'HIDDEN_STORAGE'
  | 'ALCHEMY_FURNACE'
  | 'SPIRIT_WELL'
  | 'TABLE'
  | 'LAMP'
  | 'CHEST'
  | 'ALTAR'
  | 'INCENSE_BURNER'
  | 'SWORD_RACK'

// ---- Anchors (AI navigation targets) ------------------------------------

export interface RoomAnchor {
  id: string
  role: AnchorRole
  /** Local position within the room [x, y, z]. */
  localPos: [number, number, number]
}

export type AnchorRole =
  | 'bed'
  | 'meditation'
  | 'storage'
  | 'window'
  | 'door'
  | 'desk'
  | 'furnace'
  | 'altar'
  | 'well'

// ---- Roads + Spirit Veins ------------------------------------------------

export interface CanonRoad {
  id: CanonId
  /** Polyline of world positions. */
  points: [number, number, number][]
  width: number
  material: MaterialSlot
}

export interface CanonSpiritVein {
  id: CanonId
  name: string
  position: [number, number, number]
  /** Quality 1-10 (higher = richer qi). */
  quality: number
  element: 'fire' | 'water' | 'wood' | 'metal' | 'earth' | 'lightning' | 'void'
}

// ---- Compiler output -----------------------------------------------------

export interface VoxelInstruction {
  worldX: number
  worldY: number
  worldZ: number
  slot: MaterialSlot
  provenance: ProvenanceTag
  priority: number
  opacity?: number
}

/** A compiled mesh instruction (higher-level than voxels — for smooth meshes). */
export interface MeshInstruction {
  kind: 'box' | 'roof' | 'cylinder' | 'plane' | 'sphere' | 'custom'
  position: [number, number, number]
  size: [number, number, number]
  rotation: number
  slot: MaterialSlot
  provenance: ProvenanceTag
  priority: number
  opacity?: number
}
