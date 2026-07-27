/**
 * BlockRegistry — canonical block id → properties map.
 *
 * Each voxel stores a uint16 block id. This registry maps ids to:
 *   - name (namespaced "ergenverse:...")
 *   - texture tiles (top / bottom / side) — indices into the ProceduralTextures atlas
 *   - material class ('opaque' | 'transparent' | 'cutout')
 *   - hardness (mining time, 0 = unbreakable)
 *   - solidity (collider)
 *   - emissive intensity (for qi crystal / spirit-vein glow)
 *   - smooth shading (grass tops = smooth, stone = flat)
 *
 * Id 0 is reserved for AIR (the absence of a block).
 *
 * All block names are mod-original interpretations grounded in xianxia
 * genre conventions and the 仙逆 canon setting (jade, spirit veins,
 * formation stone, paper walls, tiled roofs). NO fabricated chapter
 * citations — block types reflect material culture attested in the
 * novel's world (jade as a cultivation medium, paper-walled sects,
 * tiled-roof cities).
 */

export type MaterialClass = 'opaque' | 'transparent' | 'cutout'

export interface BlockDef {
  /** Stable string id ("ergenverse:stone"). */
  readonly id: string
  /** Numeric block id (stored in voxel storage). */
  readonly numericId: number
  /** Display label. */
  readonly label: string
  /** Texture tile indices into the ProceduralTextures atlas. */
  readonly tiles: { readonly top: number; readonly bottom: number; readonly side: number }
  /** Material class controls render order / shader. */
  readonly material: MaterialClass
  /** Mining hardness in seconds (0 = unbreakable bedrock). */
  readonly hardness: number
  /** True if the block is collidable (most solids are; water/leaves-cutout are not). */
  readonly solid: boolean
  /** Emissive intensity 0..1 for self-lit blocks (qi crystal, spirit-vein glow). */
  readonly emissive: number
  /** Smooth shading: true = average vertex normals across faces (grass, snow). */
  readonly smooth: boolean
  /** Base color (debug / minimap tint). */
  readonly color: number
}

// ---- Atlas tile indices (must match ProceduralTextures TILE_KEYS order) ----
const TILE = {
  STONE: 0,
  MARBLE: 1,
  JADE_STONE: 2,
  SPIRIT_VEIN_ORE: 3,
  DIRT: 4,
  GRASS_TOP: 5,
  GRASS_SIDE: 6,
  SAND: 7,
  SNOW: 8,
  SWAMP_MUD: 9,
  PINE_WOOD_SIDE: 10,
  PINE_WOOD_TOP: 11,
  WILLOW_WOOD_SIDE: 12,
  WILLOW_WOOD_TOP: 13,
  BAMBOO_SIDE: 14,
  BAMBOO_TOP: 15,
  JADE_WOOD_SIDE: 16,
  JADE_WOOD_TOP: 17,
  PINE_LEAVES: 18,
  WILLOW_LEAVES: 19,
  BAMBOO_LEAVES: 20,
  JADE_WOOD_LEAVES: 21,
  STONE_BRICKS: 22,
  JADE_BRICKS: 23,
  PLANKS: 24,
  PAPER_WALL: 25,
  TILED_ROOF: 26,
  WATER: 27,
  DEEP_WATER: 28,
  LAVA: 29,
  QI_CRYSTAL: 30,
  SPIRIT_VEIN_GLOW: 31,
  FORMATION_STONE: 32,
  COBBLESTONE: 33,
  MOSSY_STONE: 34,
  ICE: 35,
  VOLCANIC_ROCK: 36,
  ASH: 37,
  RED_SAND: 38,
  SNOW_CAP: 39,
  PINE_BARK: 40,
  JADE_ORE: 41,
  GOLD_ORE: 42,
  IRON_ORE: 43,
  CRYSTAL_FLOOR: 44,
  BEDROCK: 45,
  // 46..63 reserved
} as const

/**
 * Canonical numeric ids. The order is intentional: AIR=0, then stone-family,
 * then soil-family, then wood/leaves, then construction, then liquids, then
 * special, then ores. New blocks must append; never reorder (would break saves).
 */
export const enum BlockId {
  AIR = 0,
  STONE = 1,
  MARBLE = 2,
  JADE_STONE = 3,
  SPIRIT_VEIN_ORE = 4,
  DIRT = 5,
  GRASS = 6,
  SAND = 7,
  SNOW = 8,
  SWAMP_MUD = 9,
  PINE_WOOD = 10,
  WILLOW_WOOD = 11,
  BAMBOO = 12,
  JADE_WOOD = 13,
  PINE_LEAVES = 14,
  WILLOW_LEAVES = 15,
  BAMBOO_LEAVES = 16,
  JADE_WOOD_LEAVES = 17,
  STONE_BRICKS = 18,
  JADE_BRICKS = 19,
  PLANKS = 20,
  PAPER_WALL = 21,
  TILED_ROOF = 22,
  WATER = 23,
  DEEP_WATER = 24,
  LAVA = 25,
  QI_CRYSTAL = 26,
  SPIRIT_VEIN_GLOW = 27,
  FORMATION_STONE = 28,
  COBBLESTONE = 29,
  MOSSY_STONE = 30,
  ICE = 31,
  VOLCANIC_ROCK = 32,
  ASH = 33,
  RED_SAND = 34,
  SNOW_CAP = 35,
  PINE_BARK = 36,
  JADE_ORE = 37,
  GOLD_ORE = 38,
  IRON_ORE = 39,
  CRYSTAL_FLOOR = 40,
  BEDROCK = 41,
}

const DEFS: BlockDef[] = []

function def(d: BlockDef): void {
  DEFS[d.numericId] = d
}

def({
  id: 'ergenverse:air',
  numericId: BlockId.AIR,
  label: 'Air',
  tiles: { top: 0, bottom: 0, side: 0 },
  material: 'transparent',
  hardness: 0,
  solid: false,
  emissive: 0,
  smooth: false,
  color: 0x000000,
})

// ---- Stone family ----------------------------------------------------------
def({ id: 'ergenverse:stone', numericId: BlockId.STONE, label: 'Granite Stone',
  tiles: { top: TILE.STONE, bottom: TILE.STONE, side: TILE.STONE },
  material: 'opaque', hardness: 1.5, solid: true, emissive: 0, smooth: false, color: 0x6b6f76 })
def({ id: 'ergenverse:marble', numericId: BlockId.MARBLE, label: 'White Marble',
  tiles: { top: TILE.MARBLE, bottom: TILE.MARBLE, side: TILE.MARBLE },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 0, smooth: true, color: 0xe8e6e0 })
def({ id: 'ergenverse:jade_stone', numericId: BlockId.JADE_STONE, label: 'Jade Stone',
  tiles: { top: TILE.JADE_STONE, bottom: TILE.JADE_STONE, side: TILE.JADE_STONE },
  material: 'opaque', hardness: 2.5, solid: true, emissive: 0.1, smooth: true, color: 0x4e8a72 })
def({ id: 'ergenverse:spirit_vein_ore', numericId: BlockId.SPIRIT_VEIN_ORE, label: 'Spirit Vein Ore',
  tiles: { top: TILE.SPIRIT_VEIN_ORE, bottom: TILE.SPIRIT_VEIN_ORE, side: TILE.SPIRIT_VEIN_ORE },
  material: 'opaque', hardness: 3.0, solid: true, emissive: 0.5, smooth: false, color: 0x9be15d })
def({ id: 'ergenverse:cobblestone', numericId: BlockId.COBBLESTONE, label: 'Cobblestone',
  tiles: { top: TILE.COBBLESTONE, bottom: TILE.COBBLESTONE, side: TILE.COBBLESTONE },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 0, smooth: false, color: 0x7a7670 })
def({ id: 'ergenverse:mossy_stone', numericId: BlockId.MOSSY_STONE, label: 'Mossy Stone',
  tiles: { top: TILE.MOSSY_STONE, bottom: TILE.MOSSY_STONE, side: TILE.MOSSY_STONE },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 0, smooth: false, color: 0x5a7050 })
def({ id: 'ergenverse:volcanic_rock', numericId: BlockId.VOLCANIC_ROCK, label: 'Volcanic Rock',
  tiles: { top: TILE.VOLCANIC_ROCK, bottom: TILE.VOLCANIC_ROCK, side: TILE.VOLCANIC_ROCK },
  material: 'opaque', hardness: 3.0, solid: true, emissive: 0.2, smooth: false, color: 0x2a1d20 })
def({ id: 'ergenverse:ice', numericId: BlockId.ICE, label: 'Ice',
  tiles: { top: TILE.ICE, bottom: TILE.ICE, side: TILE.ICE },
  material: 'transparent', hardness: 0.5, solid: true, emissive: 0, smooth: true, color: 0xbfe6ff })

// ---- Soils -----------------------------------------------------------------
def({ id: 'ergenverse:dirt', numericId: BlockId.DIRT, label: 'Dirt',
  tiles: { top: TILE.DIRT, bottom: TILE.DIRT, side: TILE.DIRT },
  material: 'opaque', hardness: 0.5, solid: true, emissive: 0, smooth: false, color: 0x6b4a2b })
def({ id: 'ergenverse:grass', numericId: BlockId.GRASS, label: 'Grass Block',
  tiles: { top: TILE.GRASS_TOP, bottom: TILE.DIRT, side: TILE.GRASS_SIDE },
  material: 'opaque', hardness: 0.5, solid: true, emissive: 0, smooth: true, color: 0x5a8a3c })
def({ id: 'ergenverse:sand', numericId: BlockId.SAND, label: 'Sand',
  tiles: { top: TILE.SAND, bottom: TILE.SAND, side: TILE.SAND },
  material: 'opaque', hardness: 0.3, solid: true, emissive: 0, smooth: true, color: 0xd9c89a })
def({ id: 'ergenverse:red_sand', numericId: BlockId.RED_SAND, label: 'Red Sand',
  tiles: { top: TILE.RED_SAND, bottom: TILE.RED_SAND, side: TILE.RED_SAND },
  material: 'opaque', hardness: 0.3, solid: true, emissive: 0, smooth: true, color: 0xb07a4a })
def({ id: 'ergenverse:snow', numericId: BlockId.SNOW, label: 'Snow',
  tiles: { top: TILE.SNOW, bottom: TILE.SNOW, side: TILE.SNOW },
  material: 'opaque', hardness: 0.2, solid: true, emissive: 0, smooth: true, color: 0xf4f8ff })
def({ id: 'ergenverse:snow_cap', numericId: BlockId.SNOW_CAP, label: 'Snow Cap',
  tiles: { top: TILE.SNOW_CAP, bottom: TILE.SNOW, side: TILE.SNOW_CAP },
  material: 'opaque', hardness: 0.3, solid: true, emissive: 0, smooth: true, color: 0xf0f5ff })
def({ id: 'ergenverse:swamp_mud', numericId: BlockId.SWAMP_MUD, label: 'Swamp Mud',
  tiles: { top: TILE.SWAMP_MUD, bottom: TILE.SWAMP_MUD, side: TILE.SWAMP_MUD },
  material: 'opaque', hardness: 0.4, solid: true, emissive: 0, smooth: false, color: 0x3a3825 })
def({ id: 'ergenverse:ash', numericId: BlockId.ASH, label: 'Volcanic Ash',
  tiles: { top: TILE.ASH, bottom: TILE.ASH, side: TILE.ASH },
  material: 'opaque', hardness: 0.2, solid: true, emissive: 0, smooth: false, color: 0x2a2724 })

// ---- Woods -----------------------------------------------------------------
def({ id: 'ergenverse:pine_wood', numericId: BlockId.PINE_WOOD, label: 'Pine Wood',
  tiles: { top: TILE.PINE_WOOD_TOP, bottom: TILE.PINE_WOOD_TOP, side: TILE.PINE_WOOD_SIDE },
  material: 'opaque', hardness: 1.0, solid: true, emissive: 0, smooth: false, color: 0x4a3520 })
def({ id: 'ergenverse:willow_wood', numericId: BlockId.WILLOW_WOOD, label: 'Willow Wood',
  tiles: { top: TILE.WILLOW_WOOD_TOP, bottom: TILE.WILLOW_WOOD_TOP, side: TILE.WILLOW_WOOD_SIDE },
  material: 'opaque', hardness: 1.0, solid: true, emissive: 0, smooth: false, color: 0x6a5a3a })
def({ id: 'ergenverse:bamboo', numericId: BlockId.BAMBOO, label: 'Bamboo',
  tiles: { top: TILE.BAMBOO_TOP, bottom: TILE.BAMBOO_TOP, side: TILE.BAMBOO_SIDE },
  material: 'opaque', hardness: 0.8, solid: true, emissive: 0, smooth: false, color: 0x6e8a3a })
def({ id: 'ergenverse:jade_wood', numericId: BlockId.JADE_WOOD, label: 'Jade Wood',
  tiles: { top: TILE.JADE_WOOD_TOP, bottom: TILE.JADE_WOOD_TOP, side: TILE.JADE_WOOD_SIDE },
  material: 'opaque', hardness: 1.5, solid: true, emissive: 0.1, smooth: false, color: 0x3a6a52 })
def({ id: 'ergenverse:pine_bark', numericId: BlockId.PINE_BARK, label: 'Pine Bark',
  tiles: { top: TILE.PINE_BARK, bottom: TILE.PINE_BARK, side: TILE.PINE_BARK },
  material: 'opaque', hardness: 1.0, solid: true, emissive: 0, smooth: false, color: 0x3a2515 })

// ---- Leaves (cutout for crisp edges) --------------------------------------
def({ id: 'ergenverse:pine_leaves', numericId: BlockId.PINE_LEAVES, label: 'Pine Leaves',
  tiles: { top: TILE.PINE_LEAVES, bottom: TILE.PINE_LEAVES, side: TILE.PINE_LEAVES },
  material: 'cutout', hardness: 0.2, solid: false, emissive: 0, smooth: true, color: 0x2a4a22 })
def({ id: 'ergenverse:willow_leaves', numericId: BlockId.WILLOW_LEAVES, label: 'Willow Leaves',
  tiles: { top: TILE.WILLOW_LEAVES, bottom: TILE.WILLOW_LEAVES, side: TILE.WILLOW_LEAVES },
  material: 'cutout', hardness: 0.2, solid: false, emissive: 0, smooth: true, color: 0x5a7a3a })
def({ id: 'ergenverse:bamboo_leaves', numericId: BlockId.BAMBOO_LEAVES, label: 'Bamboo Leaves',
  tiles: { top: TILE.BAMBOO_LEAVES, bottom: TILE.BAMBOO_LEAVES, side: TILE.BAMBOO_LEAVES },
  material: 'cutout', hardness: 0.2, solid: false, emissive: 0, smooth: true, color: 0x6a8a3a })
def({ id: 'ergenverse:jade_wood_leaves', numericId: BlockId.JADE_WOOD_LEAVES, label: 'Jade Wood Leaves',
  tiles: { top: TILE.JADE_WOOD_LEAVES, bottom: TILE.JADE_WOOD_LEAVES, side: TILE.JADE_WOOD_LEAVES },
  material: 'cutout', hardness: 0.2, solid: false, emissive: 0.05, smooth: true, color: 0x3a6a52 })

// ---- Construction ----------------------------------------------------------
def({ id: 'ergenverse:stone_bricks', numericId: BlockId.STONE_BRICKS, label: 'Stone Bricks',
  tiles: { top: TILE.STONE_BRICKS, bottom: TILE.STONE_BRICKS, side: TILE.STONE_BRICKS },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 0, smooth: false, color: 0x7a7a7a })
def({ id: 'ergenverse:jade_bricks', numericId: BlockId.JADE_BRICKS, label: 'Jade Bricks',
  tiles: { top: TILE.JADE_BRICKS, bottom: TILE.JADE_BRICKS, side: TILE.JADE_BRICKS },
  material: 'opaque', hardness: 2.5, solid: true, emissive: 0.15, smooth: true, color: 0x4e8a72 })
def({ id: 'ergenverse:planks', numericId: BlockId.PLANKS, label: 'Wooden Planks',
  tiles: { top: TILE.PLANKS, bottom: TILE.PLANKS, side: TILE.PLANKS },
  material: 'opaque', hardness: 1.0, solid: true, emissive: 0, smooth: false, color: 0x9a7240 })
def({ id: 'ergenverse:paper_wall', numericId: BlockId.PAPER_WALL, label: 'Paper Wall',
  tiles: { top: TILE.PAPER_WALL, bottom: TILE.PAPER_WALL, side: TILE.PAPER_WALL },
  material: 'opaque', hardness: 0.1, solid: true, emissive: 0, smooth: true, color: 0xf0e6c8 })
def({ id: 'ergenverse:tiled_roof', numericId: BlockId.TILED_ROOF, label: 'Tiled Roof',
  tiles: { top: TILE.TILED_ROOF, bottom: TILE.TILED_ROOF, side: TILE.TILED_ROOF },
  material: 'opaque', hardness: 1.5, solid: true, emissive: 0, smooth: false, color: 0x6a3a2a })
def({ id: 'ergenverse:formation_stone', numericId: BlockId.FORMATION_STONE, label: 'Formation Stone',
  tiles: { top: TILE.FORMATION_STONE, bottom: TILE.FORMATION_STONE, side: TILE.FORMATION_STONE },
  material: 'opaque', hardness: 5.0, solid: true, emissive: 0.4, smooth: true, color: 0x4a5a8a })
def({ id: 'ergenverse:crystal_floor', numericId: BlockId.CRYSTAL_FLOOR, label: 'Crystal Floor',
  tiles: { top: TILE.CRYSTAL_FLOOR, bottom: TILE.CRYSTAL_FLOOR, side: TILE.CRYSTAL_FLOOR },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 0.3, smooth: true, color: 0x88c8e6 })
def({ id: 'ergenverse:bedrock', numericId: BlockId.BEDROCK, label: 'Bedrock',
  tiles: { top: TILE.BEDROCK, bottom: TILE.BEDROCK, side: TILE.BEDROCK },
  material: 'opaque', hardness: 0, solid: true, emissive: 0, smooth: false, color: 0x1a1a1a })

// ---- Liquids (transparent) -------------------------------------------------
def({ id: 'ergenverse:water', numericId: BlockId.WATER, label: 'Water',
  tiles: { top: TILE.WATER, bottom: TILE.WATER, side: TILE.WATER },
  material: 'transparent', hardness: 100, solid: false, emissive: 0, smooth: true, color: 0x2a6a9a })
def({ id: 'ergenverse:deep_water', numericId: BlockId.DEEP_WATER, label: 'Deep Water',
  tiles: { top: TILE.DEEP_WATER, bottom: TILE.DEEP_WATER, side: TILE.DEEP_WATER },
  material: 'transparent', hardness: 100, solid: false, emissive: 0, smooth: true, color: 0x153a5a })
def({ id: 'ergenverse:lava', numericId: BlockId.LAVA, label: 'Lava',
  tiles: { top: TILE.LAVA, bottom: TILE.LAVA, side: TILE.LAVA },
  material: 'transparent', hardness: 100, solid: false, emissive: 1.0, smooth: true, color: 0xd04a1a })

// ---- Special (qi crystal / spirit vein glow / ores) ------------------------
def({ id: 'ergenverse:qi_crystal', numericId: BlockId.QI_CRYSTAL, label: 'Qi Crystal',
  tiles: { top: TILE.QI_CRYSTAL, bottom: TILE.QI_CRYSTAL, side: TILE.QI_CRYSTAL },
  material: 'transparent', hardness: 3.0, solid: true, emissive: 0.8, smooth: true, color: 0x6affc8 })
def({ id: 'ergenverse:spirit_vein_glow', numericId: BlockId.SPIRIT_VEIN_GLOW, label: 'Spirit Vein Glow',
  tiles: { top: TILE.SPIRIT_VEIN_GLOW, bottom: TILE.SPIRIT_VEIN_GLOW, side: TILE.SPIRIT_VEIN_GLOW },
  material: 'opaque', hardness: 2.0, solid: true, emissive: 1.0, smooth: true, color: 0x9be15d })
def({ id: 'ergenverse:jade_ore', numericId: BlockId.JADE_ORE, label: 'Jade Ore',
  tiles: { top: TILE.JADE_ORE, bottom: TILE.JADE_ORE, side: TILE.JADE_ORE },
  material: 'opaque', hardness: 3.0, solid: true, emissive: 0.15, smooth: false, color: 0x4e8a72 })
def({ id: 'ergenverse:gold_ore', numericId: BlockId.GOLD_ORE, label: 'Gold Ore',
  tiles: { top: TILE.GOLD_ORE, bottom: TILE.GOLD_ORE, side: TILE.GOLD_ORE },
  material: 'opaque', hardness: 3.0, solid: true, emissive: 0.2, smooth: false, color: 0xd4a838 })
def({ id: 'ergenverse:iron_ore', numericId: BlockId.IRON_ORE, label: 'Iron Ore',
  tiles: { top: TILE.IRON_ORE, bottom: TILE.IRON_ORE, side: TILE.IRON_ORE },
  material: 'opaque', hardness: 3.0, solid: true, emissive: 0, smooth: false, color: 0x8a7a6a })

// ---- Registry accessors ----------------------------------------------------

/** All registered block definitions indexed by numeric id. */
export const BLOCKS: ReadonlyArray<BlockDef | undefined> = DEFS

/** Get a block definition by numeric id. Returns the AIR def for unknown ids. */
export function getBlock(numericId: number): BlockDef {
  return DEFS[numericId] ?? DEFS[BlockId.AIR]!
}

/** True if the block id is air or out of range. */
export function isAir(numericId: number): boolean {
  return numericId === BlockId.AIR || DEFS[numericId] === undefined
}

/** True if the block should render in the opaque pass. */
export function isOpaque(numericId: number): boolean {
  const b = DEFS[numericId]
  return !!b && b.material === 'opaque'
}

/** True if the block should render in the transparent pass. */
export function isTransparent(numericId: number): boolean {
  const b = DEFS[numericId]
  return !!b && b.material === 'transparent'
}

/** True if the block should render in the cutout pass (alpha-tested). */
export function isCutout(numericId: number): boolean {
  const b = DEFS[numericId]
  return !!b && b.material === 'cutout'
}

/** True if a face between `here` and `neighbor` should be drawn. */
export function faceVisible(hereId: number, neighborId: number): boolean {
  if (hereId === BlockId.AIR) return false
  const here = DEFS[hereId]
  const neighbor = DEFS[neighborId]
  if (!here) return false
  if (!neighbor || neighborId === BlockId.AIR) return true
  // Transparent/cutout blocks reveal faces of opaque neighbors.
  if (here.material === 'opaque' && neighbor.material !== 'opaque') return true
  // Same-id transparent neighbors (water-water) don't draw interior faces.
  if (hereId === neighborId) return false
  // Transparent neighbor of opaque → opaque face shows.
  if (here.material !== 'opaque' && neighbor.material === 'opaque') return false
  return true
}

/** Count of registered block types (highest numeric id + 1). */
export const BLOCK_COUNT: number = DEFS.length

/** Namespaced id → numeric id lookup. */
const NAME_TO_ID: Map<string, number> = new Map()
for (const d of DEFS) {
  if (d) NAME_TO_ID.set(d.id, d.numericId)
}

export function numericIdFor(blockId: string): number {
  return NAME_TO_ID.get(blockId) ?? BlockId.AIR
}

/** Atlas descriptor — must match ProceduralTextures. */
export const ATLAS_TILES_PER_ROW = 8
export const ATLAS_TILE_PIXELS = 256
export const ATLAS_PIXELS = ATLAS_TILES_PER_ROW * ATLAS_TILE_PIXELS // 2048

/** Convert a tile index to [u0, v0, u1, v1] in the atlas, with edge padding. */
export function tileUV(tileIndex: number): [number, number, number, number] {
  const col = tileIndex % ATLAS_TILES_PER_ROW
  const row = Math.floor(tileIndex / ATLAS_TILES_PER_ROW)
  const pad = 0.5 / ATLAS_PIXELS // half-texel padding to prevent bleed
  const u0 = col / ATLAS_TILES_PER_ROW + pad
  const v1 = 1 - row / ATLAS_TILES_PER_ROW - pad
  const u1 = (col + 1) / ATLAS_TILES_PER_ROW - pad
  const v0 = 1 - (row + 1) / ATLAS_TILES_PER_ROW + pad
  return [u0, v0, u1, v1]
}

/** Ordered list of texture keys for the ProceduralTextures atlas generator. */
export const TILE_KEYS: readonly string[] = [
  'stone', 'marble', 'jade_stone', 'spirit_vein_ore',
  'dirt', 'grass_top', 'grass_side', 'sand',
  'snow', 'swamp_mud',
  'pine_wood_side', 'pine_wood_top',
  'willow_wood_side', 'willow_wood_top',
  'bamboo_side', 'bamboo_top',
  'jade_wood_side', 'jade_wood_top',
  'pine_leaves', 'willow_leaves', 'bamboo_leaves', 'jade_wood_leaves',
  'stone_bricks', 'jade_bricks', 'planks', 'paper_wall', 'tiled_roof',
  'water', 'deep_water', 'lava',
  'qi_crystal', 'spirit_vein_glow', 'formation_stone',
  'cobblestone', 'mossy_stone', 'ice', 'volcanic_rock', 'ash',
  'red_sand', 'snow_cap', 'pine_bark',
  'jade_ore', 'gold_ore', 'iron_ore', 'crystal_floor', 'bedrock',
] as const

export function tileIndexForKey(key: string): number {
  const i = TILE_KEYS.indexOf(key)
  return i < 0 ? 0 : i
}
