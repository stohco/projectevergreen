#!/usr/bin/env python3
"""
Generate custom .nbt structure files for canon locations on Planet Suzaku.
Each structure is a hand-crafted building matching its canon description.

Structures generated:
  1. heng_yue_sect/main_hall       — grand Daoist main hall (Wang Lin's sect)
  2. heng_yue_sect/outer_gate      — sect entrance gate with spirit-stone pillars
  3. heng_yue_sect/herb_garden     — spirit herb cultivation pavilion + garden
  4. tian_shui_city/city_gate      — trade city south gate
  5. tian_shui_city/market_stall   — merchant stall
  6. tian_shui_city/merchant_house — wealthy merchant residence
  7. teng_family_city/keep         — Teng family fortress keep
  8. soul_refining_sect/furnace    — soul-refining furnace hall
  9. corpse_yin_sect/ancestor_hall — death sect ancestor hall
  10. heavenly_fate_sect/star_tower — star observatory tower
  11. wandering_camp/tent          — wandering cultivator tent
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from nbt_structure_builder import (
    StructureBuilder, bs,
    AIR, STONE, COBBLE, STONE_BRICKS, MOSSY_STONE_BRICKS, CRACKED_STONE_BRICKS,
    CHISELED_STONE_BRICKS, DEEPSLATE_BRICKS, POLISHED_ANDESITE, SMOOTH_STONE,
    QUARTZ_BRICKS, QUARTZ_PILLAR, CHISELED_QUARTZ,
    OAK_LOG, OAK_LOG_X, OAK_LOG_Z, OAK_PLANKS, OAK_SLAB, OAK_SLAB_TOP,
    OAK_STAIRS_N, OAK_STAIRS_S, OAK_STAIRS_E, OAK_STAIRS_W,
    SPRUCE_PLANKS, SPRUCE_LOG, SPRUCE_STAIRS_N, SPRUCE_STAIRS_S, SPRUCE_STAIRS_E, SPRUCE_STAIRS_W,
    DARK_OAK_PLANKS, DARK_OAK_LOG, BIRCH_PLANKS,
    DARK_OAK_STAIRS_N, DARK_OAK_STAIRS_S, DARK_OAK_STAIRS_E, DARK_OAK_STAIRS_W,
    GLASS_PANE, OAK_FENCE, IRON_BARS, CHAIN, LANTERN, SOUL_LANTERN,
    REDSTONE_LAMP, GLOWSTONE, SEA_LANTERN, END_ROD, CAMPFIRE, SOUL_CAMPFIRE,
    GRASS, TALL_GRASS, FLOWER_RED, FLOWER_YELLOW, AZURE_BLUET,
    OAK_LEAVES, SPRUCE_LEAVES, DARK_OAK_LEAVES,
    DIRT_PATH, COARSE_DIRT, PODZOL, SAND, RED_SAND, GRAVEL, CLAY,
    OBSIDIAN, CRYING_OBSIDIAN, NETHER_BRICKS, RED_NETHER_BRICKS,
    BLACKSTONE, BASALT, SOUL_SOIL, SOUL_SAND, MAGMA, LAVA,
    BLUE_ICE, PACKED_ICE, SNOW_BLOCK,
    PRISMARINE, PRISMARINE_BRICKS, DARK_PRISMARINE,
    GOLD_BLOCK, IRON_BLOCK, DIAMOND_BLOCK, EMERALD_BLOCK, LAPIS_BLOCK, REDSTONE_BLOCK,
    CHEST, ENDER_CHEST, BOOKSHELF, LECTERN, CRAFTING_TABLE, CAULDRON, BREWING_STAND,
    ANVIL, CHIPPED_ANVIL, BELL, FLOWER_POT,
    WHITE_CARPET, RED_CARPET, YELLOW_CARPET, BLUE_CARPET, GREEN_CARPET, BLACK_CARPET,
    WHITE_WOOL, RED_WOOL, BLACK_WOOL, GOLD_WOOL,
    WHITE_CONCRETE, BLACK_CONCRETE, RED_CONCRETE, TERRACOTTA,
    WHITE_TERRACOTTA, BLACK_TERRACOTTA,
    STONE_BRICK_STAIRS_N, STONE_BRICK_STAIRS_S, STONE_BRICK_STAIRS_E, STONE_BRICK_STAIRS_W,
    STONE_BRICK_SLAB, COBBLE_WALL, STONE_BRICK_WALL, MOSSY_STONE_BRICK_WALL,
    POLISHED_BLACKSTONE_BRICKS, POLISHED_BLACKSTONE_BRICK_WALL,
    GILDED_BLACKSTONE, LODESTONE,
    SPIRIT_STONE_ORE, REALM_SEALING_FLAG, JADE_BED, CULTIVATION_MAT,
    QI_GATHERING_GRASS, SNOW_HEART_HERB, SOUL_NOURISHING_LOTUS,
    FOUNDATION_ROOT_VINE, SWORD_EDGE_MOSS, NINE_LEAF_CLOVER,
    STRUCTURE_DIR, roof_pyramidal, roof_gabled, door_2high, window_cross,
    pillar, lantern_hang, chest_with_loot, sign_post,
)

def save(b, relpath):
    fp = os.path.join(STRUCTURE_DIR, relpath + ".nbt")
    b.export_nbt(fp)
    print(f"  ✓ {relpath}.nbt  ({b.stats()})")

# ════════════════════════════════════════════════════════════════════════
# 1. HENG YUE SECT — Main Hall (恒岳宗主殿)
#    Canon: Mountain Daoist sect. Grand hall for sect gatherings, sword
#    training. Vermilion pillars, curved roof, spirit vein beneath.
# ════════════════════════════════════════════════════════════════════════
def build_heng_yue_main_hall():
    print("\n[1/11] Heng Yue Sect — Main Hall")
    W, H, L = 21, 18, 25
    b = StructureBuilder(W, H, L)

    # Foundation platform (stone bricks, 2 high base)
    b.fill(0, 0, 0, W-1, 1, L-1, STONE_BRICKS)
    # Staircase entrance (south side, steps going up into hall)
    for i in range(3):
        for x in range(6, W-6):
            b.set(x, 1 + i, L-1-i, STONE_BRICK_STAIRS_N)

    # Main hall floor (polished andesite)
    b.fill(2, 2, 2, W-3, 2, L-3, POLISHED_ANDESITE)

    # 4 rows of vermillion (red concrete) pillars — 8 pillars total
    pillar_positions = [(4, 4), (4, 12), (4, 20), (W-5, 4), (W-5, 12), (W-5, 20),
                        (W//2, 4), (W//2, 20)]
    for (px, pz) in pillar_positions:
        pillar(b, px, 3, pz, 10, RED_CONCRETE)
        # Stone brick base
        b.set(px, 2, pz, STONE_BRICKS)
        # Gold cap
        b.set(px, 11, pz, GOLD_BLOCK)

    # Walls (stone brick) — back wall, side walls; front has large opening
    b.fill(2, 3, 2, 2, 10, L-3, STONE_BRICKS)       # west wall
    b.fill(W-3, 3, 2, W-3, 10, L-3, STONE_BRICKS)   # east wall
    b.fill(2, 3, 2, W-3, 10, 2, STONE_BRICKS)        # north wall (back)
    # South wall (front) — leave central opening for door
    b.fill(2, 3, L-3, 6, 10, L-3, STONE_BRICKS)
    b.fill(W-7, 3, L-3, W-3, 10, L-3, STONE_BRICKS)
    # Lintel above door
    b.fill(7, 8, L-3, W-8, 10, L-3, STONE_BRICKS)

    # Windows in side walls
    for z in [6, 10, 14, 18]:
        window_cross(b, 2, 6, z, STONE_BRICKS, GLASS_PANE)
        window_cross(b, W-3, 6, z, STONE_BRICKS, GLASS_PANE)

    # Altar platform at back (north) — 3 steps up
    for i in range(3):
        b.fill(6, 3+i, 4, W-7, 3+i, 6, STONE_BRICK_SLAB if i < 2 else POLISHED_ANDESITE)
    b.fill(6, 3, 4, W-7, 5, 6, POLISHED_ANDESITE)
    b.fill(6, 6, 4, W-7, 6, 6, STONE_BRICK_SLAB)

    # Altar: jade bed (cultivation seat) + incense cauldron
    b.set(W//2, 7, 5, JADE_BED)
    b.set(W//2 - 2, 7, 5, CAULDRON)
    b.set(W//2 + 2, 7, 5, CAULDRON)

    # Bells flanking the altar
    b.set(4, 7, 5, BELL)
    b.set(W-5, 7, 5, BELL)

    # Ceiling
    b.fill(2, 11, 2, W-3, 11, L-3, OAK_PLANKS)

    # Curved/pyramidal roof (3 layers of stone brick + gold trim)
    roof_pyramidal(b, 1, 1, 1, W-2, 11, L-2, 5, STONE_BRICKS, GOLD_BLOCK)
    # Roof apex — spirit gathering ornament
    b.set(W//2, 17, L//2, GLOWSTONE)
    b.set(W//2, 16, L//2, GOLD_BLOCK)

    # Eaves — overhanging dark oak slabs
    b.fill(0, 11, 0, W-1, 11, 0, DARK_OAK_PLANKS)
    b.fill(0, 11, L-1, W-1, 11, L-1, DARK_OAK_PLANKS)
    b.fill(0, 11, 0, 0, 11, L-1, DARK_OAK_PLANKS)
    b.fill(W-1, 11, 0, W-1, 11, L-1, DARK_OAK_PLANKS)

    # Lanterns hanging from ceiling
    for (lx, lz) in [(5, 8), (5, 16), (W-6, 8), (W-6, 16)]:
        lantern_hang(b, lx, 11, lz, LANTERN)

    # Red carpet runner down center aisle
    b.fill(W//2, 3, 8, W//2, 3, L-4, RED_CARPET)

    # Bookshelves along back wall
    for x in [4, 5, 6, W-7, W-6, W-5]:
        b.set(x, 3, 3, BOOKSHELF)
        b.set(x, 4, 3, BOOKSHELF)

    # Lectern for scripture reading
    b.set(W//2, 3, L-5, LECTERN)

    # Hidden spirit stone ore beneath altar (canon: spirit vein beneath hall)
    b.set(W//2, 0, 5, SPIRIT_STONE_ORE)
    b.set(W//2-1, 0, 5, SPIRIT_STONE_ORE)
    b.set(W//2+1, 0, 5, SPIRIT_STONE_ORE)

    # Chest with sect supplies
    chest_with_loot(b, 4, 3, 3, facing="west",
                    loot_table="ergenverse:chests/heng_yue_sect_hall")

    save(b, "heng_yue_sect/main_hall")

# ════════════════════════════════════════════════════════════════════════
# 2. HENG YUE SECT — Outer Gate (恒岳宗山门)
# ════════════════════════════════════════════════════════════════════════
def build_heng_yue_outer_gate():
    print("\n[2/11] Heng Yue Sect — Outer Gate")
    W, H, L = 13, 14, 8
    b = StructureBuilder(W, H, L)

    # Stone foundation
    b.fill(0, 0, 0, W-1, 1, L-1, STONE_BRICKS)
    b.fill(0, 2, 0, W-1, 2, L-1, POLISHED_ANDESITE)

    # Two large stone-brick pillars (gate posts)
    pillar(b, 1, 2, 3, 11, STONE_BRICKS)
    pillar(b, W-2, 2, 3, 11, STONE_BRICKS)
    pillar(b, 1, 2, L-4, 11, STONE_BRICKS)
    pillar(b, W-2, 2, L-4, 11, STONE_BRICKS)

    # Gold caps on pillars
    for (px, pz) in [(1, 3), (W-2, 3), (1, L-4), (W-2, L-4)]:
        b.set(px, 12, pz, GOLD_BLOCK)
        b.set(px, 2, pz, CHISELED_STONE_BRICKS)

    # Lintel (heavy beam across top)
    b.fill(1, 9, 3, W-2, 11, L-4, STONE_BRICKS)
    b.fill(1, 12, 3, W-2, 12, L-4, DARK_OAK_PLANKS)

    # Gate plaque (chiseled quartz) — "恒岳宗" carved
    b.fill(4, 10, 2, W-5, 10, 2, CHISELED_QUARTZ)
    b.fill(4, 10, L-3, W-5, 10, L-3, CHISELED_QUARTZ)

    # Curved roof on top
    roof_pyramidal(b, 0, 13, 1, W-1, 13, L-2, 1, STONE_BRICKS, GOLD_BLOCK)
    b.set(W//2, 13, L//2, GLOWSTONE)

    # Realm-sealing flags at gate corners (canon: sect protection)
    b.set(1, 6, 2, REALM_SEALING_FLAG)
    b.set(W-2, 6, 2, REALM_SEALING_FLAG)
    b.set(1, 6, L-3, REALM_SEALING_FLAG)
    b.set(W-2, 6, L-3, REALM_SEALING_FLAG)

    # Lanterns on posts
    b.set(1, 8, 3, LANTERN)
    b.set(W-2, 8, 3, LANTERN)
    b.set(1, 8, L-4, LANTERN)
    b.set(W-2, 8, L-4, LANTERN)

    # Stone path leading through gate
    b.fill(W//2, 2, 0, W//2, 2, L-1, STONE_BRICK_SLAB)

    save(b, "heng_yue_sect/outer_gate")

# ════════════════════════════════════════════════════════════════════════
# 3. HENG YUE SECT — Herb Garden Pavilion (药园)
# ════════════════════════════════════════════════════════════════════════
def build_heng_yue_herb_garden():
    print("\n[3/11] Heng Yue Sect — Herb Garden Pavilion")
    W, H, L = 15, 11, 15
    b = StructureBuilder(W, H, L)

    # Garden ground (dirt path + grass)
    b.fill(0, 0, 0, W-1, 0, L-1, GRASS)
    # Path cross
    b.fill(0, 0, L//2, W-1, 0, L//2, DIRT_PATH)
    b.fill(W//2, 0, 0, W//2, 0, L-1, DIRT_PATH)

    # Central pavilion (6x6, open-sided)
    pav_x0, pav_z0 = W//2 - 3, L//2 - 3
    pav_x1, pav_z1 = W//2 + 3, L//2 + 3
    # Floor
    b.fill(pav_x0, 0, pav_z0, pav_x1, 1, pav_z1, POLISHED_ANDESITE)
    # 4 corner pillars
    pillar(b, pav_x0, 2, pav_z0, 6, SPRUCE_LOG)
    pillar(b, pav_x1, 2, pav_z0, 6, SPRUCE_LOG)
    pillar(b, pav_x0, 2, pav_z1, 6, SPRUCE_LOG)
    pillar(b, pav_x1, 2, pav_z1, 6, SPRUCE_LOG)
    # Roof
    b.fill(pav_x0, 7, pav_z0, pav_x1, 7, pav_z1, SPRUCE_PLANKS)
    roof_pyramidal(b, pav_x0, 8, pav_z0, pav_x1, 8, pav_z1, 2, SPRUCE_PLANKS, GOLD_BLOCK)
    b.set(W//2, 10, L//2, GLOWSTONE)

    # Cultivation mat in pavilion center
    b.set(W//2, 2, L//2, CULTIVATION_MAT)

    # Herb plots (4 quadrants with real ergenverse spirit herbs)
    plots = [(1, 1, pav_x0-1, pav_z0-1),
             (pav_x1+1, 1, W-2, pav_z0-1),
             (1, pav_z1+1, pav_x0-1, L-2),
             (pav_x1+1, pav_z1+1, W-2, L-2)]
    flowers = [QI_GATHERING_GRASS, SNOW_HEART_HERB, SOUL_NOURISHING_LOTUS, NINE_LEAF_CLOVER]
    for i, (x0, z0, x1, z1) in enumerate(plots):
        b.fill(x0, 0, z0, x1, 1, z1, COARSE_DIRT)
        # Plant flowers in a grid
        for x in range(x0, x1+1, 2):
            for z in range(z0, z1+1, 2):
                b.set(x, 1, z, flowers[i])

    # Fence around garden perimeter (low fence)
    b.fill(0, 1, 0, W-1, 1, 0, OAK_FENCE)
    b.fill(0, 1, L-1, W-1, 1, L-1, OAK_FENCE)
    b.fill(0, 1, 0, 0, 1, L-1, OAK_FENCE)
    b.fill(W-1, 1, 0, W-1, 1, L-1, OAK_FENCE)
    # Gate opening (south center)
    b.set(W//2, 1, L-1, AIR)
    b.set(W//2-1, 1, L-1, AIR)

    # Water trough (prismarine) in one corner
    b.fill(1, 1, L-3, 2, 1, L-2, PRISMARINE_BRICKS)
    b.set(1, 1, L-3, "minecraft:water")
    b.set(2, 1, L-3, "minecraft:water")
    b.set(1, 1, L-2, "minecraft:water")
    b.set(2, 1, L-2, "minecraft:water")

    # Cauldron for herb processing
    b.set(W-2, 1, 1, CAULDRON)

    save(b, "heng_yue_sect/herb_garden")

# ════════════════════════════════════════════════════════════════════════
# 4. TIAN SHUI CITY — South Gate (天水城南门)
#    Canon: Prosperous trade city. Stone walls, bustling market.
# ════════════════════════════════════════════════════════════════════════
def build_tian_shui_city_gate():
    print("\n[4/11] Tian Shui City — South Gate")
    W, H, L = 17, 16, 10
    b = StructureBuilder(W, H, L)

    # Foundation
    b.fill(0, 0, 0, W-1, 1, L-1, STONE_BRICKS)

    # City wall segments (left and right of gate)
    b.fill(0, 2, 2, 5, 7, L-3, STONE_BRICKS)       # west wall segment
    b.fill(W-6, 2, 2, W-1, 7, L-3, STONE_BRICKS)   # east wall segment
    # Wall top walkway
    b.fill(0, 8, 2, 5, 8, L-3, STONE_BRICK_SLAB)
    b.fill(W-6, 8, 2, W-1, 8, L-3, STONE_BRICK_SLAB)
    # Battlements (crenellations)
    for x in range(0, 6, 2):
        b.set(x, 9, 2, STONE_BRICK_WALL)
        b.set(x, 9, L-3, STONE_BRICK_WALL)
    for x in range(W-6, W, 2):
        b.set(x, 9, 2, STONE_BRICK_WALL)
        b.set(x, 9, L-3, STONE_BRICK_WALL)

    # Gate towers (2 flanking towers)
    for tx in [0, W-5]:
        # Tower base (5x5)
        b.fill(tx, 2, 2, tx+4, 11, L-3, STONE_BRICKS)
        b.clear(tx+1, 3, 3, tx+3, 10, L-4)
        # Tower windows
        for ty in [5, 8]:
            b.set(tx+2, ty, 2, GLASS_PANE)
            b.set(tx+2, ty, L-3, GLASS_PANE)
        # Tower roof
        roof_pyramidal(b, tx, 12, 2, tx+4, 12, L-3, 3, STONE_BRICKS, GOLD_BLOCK)
        b.set(tx+2, 15, L//2, GLOWSTONE)
        # Tower flag
        b.set(tx+2, 11, 2, REALM_SEALING_FLAG)

    # Gate archway (central passage)
    # Pillars
    pillar(b, 6, 2, 2, 11, STONE_BRICKS)
    pillar(b, 6, 2, L-3, 11, STONE_BRICKS)
    pillar(b, W-7, 2, 2, 11, STONE_BRICKS)
    pillar(b, W-7, 2, L-3, 11, STONE_BRICKS)
    # Arch top
    b.fill(6, 8, 2, W-7, 11, L-3, STONE_BRICKS)
    # Gate plaque
    b.fill(7, 9, 2, W-8, 9, 2, CHISELED_STONE_BRICKS)
    b.fill(7, 9, L-3, W-8, 9, L-3, CHISELED_STONE_BRICKS)

    # Iron portcullis (iron bars) — raised position
    for y in range(3, 8):
        b.set(7, y, 2, IRON_BARS)
        b.set(8, y, 2, IRON_BARS)
        b.set(9, y, 2, IRON_BARS)
        b.set(7, y, L-3, IRON_BARS)
        b.set(8, y, L-3, IRON_BARS)
        b.set(9, y, L-3, IRON_BARS)

    # Gate passage floor (smooth stone)
    b.fill(6, 2, 2, W-7, 2, L-3, SMOOTH_STONE)

    # Lanterns flanking gate
    b.set(6, 7, 2, LANTERN)
    b.set(W-7, 7, 2, LANTERN)
    b.set(6, 7, L-3, LANTERN)
    b.set(W-7, 7, L-3, LANTERN)

    save(b, "tian_shui_city/city_gate")

# ════════════════════════════════════════════════════════════════════════
# 5. TIAN SHUI CITY — Market Stall (天水城商铺)
# ════════════════════════════════════════════════════════════════════════
def build_tian_shui_market_stall():
    print("\n[5/11] Tian Shui City — Market Stall")
    W, H, L = 7, 6, 7
    b = StructureBuilder(W, H, L)

    # Ground
    b.fill(0, 0, 0, W-1, 0, L-1, SMOOTH_STONE)

    # Counter (oak planks)
    b.fill(1, 1, 1, W-2, 1, W-2, OAK_PLANKS)
    b.fill(1, 2, 1, W-2, 2, 1, OAK_SLAB_TOP)  # counter front
    # Counter supports
    b.set(1, 1, 1, OAK_LOG)
    b.set(W-2, 1, 1, OAK_LOG)
    b.set(1, 1, L-2, OAK_LOG)
    b.set(W-2, 1, L-2, OAK_LOG)

    # Awning posts
    pillar(b, 0, 1, 0, 4, OAK_LOG)
    pillar(b, W-1, 1, 0, 4, OAK_LOG)
    # Awning (wool canopy)
    b.fill(0, 5, 0, W-1, 5, 0, RED_WOOL)
    b.fill(0, 5, 1, W-1, 5, 1, WHITE_WOOL)
    b.fill(0, 5, 2, W-1, 5, 2, RED_WOOL)

    # Goods on counter (chests + items)
    chest_with_loot(b, 2, 2, 2, facing="north",
                    loot_table="ergenverse:chests/tian_shui_market")
    chest_with_loot(b, 4, 2, 2, facing="north",
                    loot_table="ergenverse:chests/tian_shui_market")
    # Brewing stand (alchemy goods)
    b.set(3, 2, 3, BREWING_STAND)

    # Lantern
    b.set(3, 4, 1, LANTERN)

    # Sign
    sign_post(b, W//2, 1, 0, ["天水商行", "Tian Shui", "Trade House"])

    save(b, "tian_shui_city/market_stall")

# ════════════════════════════════════════════════════════════════════════
# 6. TIAN SHUI CITY — Merchant House (天水城商户宅)
# ════════════════════════════════════════════════════════════════════════
def build_tian_shui_merchant_house():
    print("\n[6/11] Tian Shui City — Merchant House")
    W, H, L = 13, 14, 13
    b = StructureBuilder(W, H, L)

    # Foundation
    b.fill(0, 0, 0, W-1, 0, L-1, STONE_BRICKS)
    # Floor
    b.fill(1, 1, 1, W-2, 1, L-2, OAK_PLANKS)

    # Outer walls (oak plank frame, stone brick base)
    b.fill(1, 2, 1, W-2, 3, 1, STONE_BRICKS)
    b.fill(1, 4, 1, W-2, 7, 1, OAK_PLANKS)
    b.fill(1, 2, L-2, W-2, 3, L-2, STONE_BRICKS)
    b.fill(1, 4, L-2, W-2, 7, L-2, OAK_PLANKS)
    b.fill(1, 2, 1, 1, 3, L-2, STONE_BRICKS)
    b.fill(1, 4, 1, 1, 7, L-2, OAK_PLANKS)
    b.fill(W-2, 2, 1, W-2, 3, L-2, STONE_BRICKS)
    b.fill(W-2, 4, 1, W-2, 7, L-2, OAK_PLANKS)

    # Door (south side)
    door_2high(b, W//2, 2, L-2, facing="south", wood="oak")
    # Windows
    for z in [3, 5, 7, 9]:
        b.set(1, 5, z, GLASS_PANE)
        b.set(W-2, 5, z, GLASS_PANE)
    b.set(W//2-2, 5, 1, GLASS_PANE)
    b.set(W//2+2, 5, 1, GLASS_PANE)

    # Interior partition (divides shop from living quarters)
    b.fill(W//2, 2, 2, W//2, 5, L-3, OAK_PLANKS)
    # Interior door
    b.set(W//2, 2, L//2, AIR)
    b.set(W//2, 3, L//2, AIR)

    # Shop side (front): counter + chests
    b.fill(W//2+1, 2, L-4, W//2+1, 2, L-3, OAK_SLAB_TOP)
    chest_with_loot(b, W//2+2, 2, L-3, facing="south",
                    loot_table="ergenverse:chests/tian_shui_merchant")
    chest_with_loot(b, W//2+3, 2, L-3, facing="south",
                    loot_table="ergenverse:chests/tian_shui_merchant")

    # Living side (back): bed, bookshelf, crafting table
    b.fill(2, 2, 2, 3, 2, 3, WHITE_WOOL)  # bed base
    b.set(2, 2, 2, RED_WOOL)  # pillow
    b.set(4, 2, 2, BOOKSHELF)
    b.set(4, 3, 2, BOOKSHELF)
    b.set(4, 2, 3, CRAFTING_TABLE)

    # Ceiling
    b.fill(1, 8, 1, W-2, 8, L-2, OAK_PLANKS)
    # Gabled roof
    roof_gabled(b, 0, 9, 0, W-1, 9, L-1, 4, OAK_PLANKS, DARK_OAK_LOG)

    # Lanterns
    b.set(W//2+2, 7, L//2, LANTERN)
    b.set(W//2-2, 7, L//2, LANTERN)

    save(b, "tian_shui_city/merchant_house")

# ════════════════════════════════════════════════════════════════════════
# 7. TENG FAMILY CITY — Keep (腾家城主堡)
#    Canon: Teng family fortress. Martial, fortified.
# ════════════════════════════════════════════════════════════════════════
def build_teng_family_keep():
    print("\n[7/11] Teng Family City — Keep")
    W, H, L = 17, 20, 17
    b = StructureBuilder(W, H, L)

    # Foundation (deep stone)
    b.fill(0, 0, 0, W-1, 2, L-1, DEEPSLATE_BRICKS)

    # Outer keep walls (4 thick stone brick)
    b.walls(1, 3, 1, W-2, 14, L-2, STONE_BRICKS)
    # Wall corners reinforced
    for (cx, cz) in [(1,1), (W-2,1), (1,L-2), (W-2,L-2)]:
        pillar(b, cx, 3, cz, 14, CHISELED_STONE_BRICKS)

    # Floor
    b.fill(2, 3, 2, W-3, 3, L-3, POLISHED_ANDESITE)

    # Gate (south, reinforced)
    b.fill(W//2-1, 3, L-2, W//2+1, 5, L-2, AIR)
    b.fill(W//2-1, 3, L-2, W//2+1, 3, L-2, bs("minecraft:iron_door", facing="north", half="lower", hinge="left", open="false"))
    b.fill(W//2-1, 4, L-2, W//2+1, 4, L-2, bs("minecraft:iron_door", half="upper", hinge="left", open="false"))

    # Windows (arrow slits)
    for y in [6, 9, 12]:
        b.set(1, y, L//2, IRON_BARS)
        b.set(W-2, y, L//2, IRON_BARS)
        b.set(W//2, y, 1, IRON_BARS)
        b.set(W//2, y, L-2, IRON_BARS)

    # Inner courtyard structure — ancestor altar
    b.fill(W//2-2, 4, 4, W//2+2, 5, 6, STONE_BRICKS)
    b.clear(W//2-1, 4, 5, W//2+1, 5, 5)
    # Altar
    b.set(W//2, 6, 5, GOLD_BLOCK)
    b.set(W//2, 7, 5, CHISELED_STONE_BRICKS)
    # Family banner
    b.set(W//2, 8, 5, REALM_SEALING_FLAG)

    # Corner watchtowers (4, taller than walls)
    for (tx, tz) in [(0,0), (W-1,0), (0,L-1), (W-1,L-1)]:
        b.fill(tx, 3, tz, tx, 18, tz, CHISELED_STONE_BRICKS)
        b.fill(tx-1 if tx>0 else tx+1, 3, tz, tx, 18, tz, STONE_BRICKS)

    # Upper floor (mezzanine)
    b.fill(2, 9, 2, W-3, 9, L-3, OAK_PLANKS)
    # Upper walls
    b.walls(1, 10, 1, W-2, 15, L-2, STONE_BRICKS)
    b.fill(2, 10, 2, W-3, 10, L-3, AIR)

    # Roof platform (battlements)
    b.fill(1, 15, 1, W-2, 15, L-2, STONE_BRICKS)
    b.fill(2, 15, 2, W-3, 15, L-3, AIR)  # walkable
    # Crenellations
    for x in range(1, W-1, 2):
        b.set(x, 16, 1, STONE_BRICK_WALL)
        b.set(x, 16, L-2, STONE_BRICK_WALL)
    for z in range(1, L-1, 2):
        b.set(1, 16, z, STONE_BRICK_WALL)
        b.set(W-2, 16, z, STONE_BRICK_WALL)

    # Central watchtower (tall, for signaling)
    pillar(b, W//2-1, 15, L//2-1, 19, CHISELED_STONE_BRICKS)
    pillar(b, W//2+1, 15, L//2-1, 19, CHISELED_STONE_BRICKS)
    pillar(b, W//2-1, 15, L//2+1, 19, CHISELED_STONE_BRICKS)
    pillar(b, W//2+1, 15, L//2+1, 19, CHISELED_STONE_BRICKS)
    b.fill(W//2-1, 19, L//2-1, W//2+1, 19, L//2+1, DARK_OAK_PLANKS)
    # Signal fire brazier
    b.set(W//2, 19, L//2, CAMPFIRE)

    # Iron golem guards (2, stationed at gate)
    # Use armor stands as placeholder sentinels
    b.set(W//2-2, 4, L-3, "minecraft:armor_stand")
    b.set(W//2+2, 4, L-3, "minecraft:armor_stand")

    # Chests in keep interior
    chest_with_loot(b, 3, 4, 3, facing="east",
                    loot_table="ergenverse:chests/teng_family_keep")
    chest_with_loot(b, W-4, 4, 3, facing="west",
                    loot_table="ergenverse:chests/teng_family_keep")

    # Anvil (forge)
    b.set(W-4, 4, L-4, ANVIL)
    b.set(W-5, 4, L-4, CHIPPED_ANVIL)

    save(b, "teng_family_city/keep")

# ════════════════════════════════════════════════════════════════════════
# 8. SOUL REFINING SECT — Furnace Hall (炼魂宗魂炉殿)
#    Canon: Dark sect. Refines souls. Eerie, chains, soul fire.
# ════════════════════════════════════════════════════════════════════════
def build_soul_refining_furnace():
    print("\n[8/11] Soul Refining Sect — Furnace Hall")
    W, H, L = 15, 16, 15
    b = StructureBuilder(W, H, L)

    # Foundation (soul soil + blackstone — dark)
    b.fill(0, 0, 0, W-1, 1, L-1, BLACKSTONE)
    b.fill(0, 2, 0, W-1, 2, L-1, POLISHED_BLACKSTONE_BRICKS)

    # Walls (blackstone, dark)
    b.walls(1, 3, 1, W-2, 10, L-2, POLISHED_BLACKSTONE_BRICKS)
    # Door (north, dark oak)
    door_2high(b, W//2, 3, 1, facing="north", wood="dark_oak")

    # Ceiling (nether bricks — oppressive)
    b.fill(1, 11, 1, W-2, 11, L-2, NETHER_BRICKS)

    # Central soul furnace (magma + soul fire)
    # Furnace base
    b.fill(W//2-2, 3, L//2-2, W//2+2, 4, L//2+2, NETHER_BRICKS)
    b.clear(W//2-1, 3, L//2-1, W//2+1, 3, L//2+1)
    # Magma core
    b.fill(W//2-1, 3, L//2-1, W//2+1, 3, L//2+1, MAGMA)
    # Soul campfires (soul fire)
    b.set(W//2, 4, L//2, SOUL_CAMPFIRE)
    b.set(W//2-1, 4, L//2, SOUL_CAMPFIRE)
    b.set(W//2+1, 4, L//2, SOUL_CAMPFIRE)
    # Furnace pillars (4 corners, crying obsidian)
    for (fx, fz) in [(W//2-2, L//2-2), (W//2+2, L//2-2), (W//2-2, L//2+2), (W//2+2, L//2+2)]:
        pillar(b, fx, 5, fz, 8, CRYING_OBSIDIAN)

    # Chain cages hanging from ceiling (soul prisons)
    for (cx, cz) in [(3, 3), (3, L-4), (W-4, 3), (W-4, L-4)]:
        b.set(cx, 10, cz, CHAIN)
        b.set(cx, 9, cz, CHAIN)
        b.set(cx, 8, cz, IRON_BARS)
        b.set(cx-1, 8, cz, IRON_BARS)
        b.set(cx+1, 8, cz, IRON_BARS)
        b.set(cx, 8, cz-1, IRON_BARS)
        b.set(cx, 8, cz+1, IRON_BARS)
        # Soul lantern inside cage
        b.set(cx, 7, cz, SOUL_LANTERN)

    # Soul lanterns along walls
    for z in [3, 6, 9, 12]:
        b.set(1, 6, z, SOUL_LANTERN)
        b.set(W-2, 6, z, SOUL_LANTERN)
    for x in [3, 6, 9, 12]:
        b.set(x, 6, 1, SOUL_LANTERN)
        b.set(x, 6, L-2, SOUL_LANTERN)

    # Altar with loot (dark)
    b.fill(2, 3, 2, 3, 3, 3, OBSIDIAN)
    chest_with_loot(b, 2, 4, 2, facing="south",
                    loot_table="ergenverse:chests/soul_refining_furnace")
    b.fill(W-4, 3, 2, W-3, 3, 3, OBSIDIAN)
    chest_with_loot(b, W-3, 4, 2, facing="south",
                    loot_table="ergenverse:chests/soul_refining_furnace")

    # Bone pillar decorations
    pillar(b, 2, 3, L//2, 7, QUARTZ_PILLAR)
    pillar(b, W-3, 3, L//2, 7, QUARTZ_PILLAR)

    # Roof (pyramidal, nether bricks)
    roof_pyramidal(b, 0, 12, 0, W-1, 12, L-1, 3, NETHER_BRICKS, RED_NETHER_BRICKS)
    b.set(W//2, 15, L//2, SOUL_LANTERN)

    save(b, "soul_refining_sect/furnace_hall")

# ════════════════════════════════════════════════════════════════════════
# 9. CORPSE YIN SECT — Ancestor Hall (尸阴宗祖师殿)
#    Canon: Death sect. Corpse pits, yin energy, ancestor veneration.
# ════════════════════════════════════════════════════════════════════════
def build_corpse_yin_ancestor_hall():
    print("\n[9/11] Corpse Yin Sect — Ancestor Hall")
    W, H, L = 15, 13, 15
    b = StructureBuilder(W, H, L)

    # Foundation (soul sand + blackstone)
    b.fill(0, 0, 0, W-1, 1, L-1, SOUL_SAND)
    b.fill(0, 2, 0, W-1, 2, L-1, POLISHED_BLACKSTONE_BRICKS)

    # Walls (dark, mossy)
    b.walls(1, 3, 1, W-2, 8, L-2, POLISHED_BLACKSTONE_BRICKS)
    # Door
    door_2high(b, W//2, 3, 1, facing="north", wood="dark_oak")

    # Ceiling
    b.fill(1, 9, 1, W-2, 9, L-2, NETHER_BRICKS)

    # Ancestor shrine (back wall)
    b.fill(W//2-3, 3, L-4, W//2+3, 5, L-3, POLISHED_BLACKSTONE_BRICKS)
    b.clear(W//2-2, 3, L-3, W//2+2, 5, L-3)
    # Shrine altar
    b.fill(W//2-2, 6, L-3, W//2+2, 6, L-3, CHISELED_STONE_BRICKS)
    # Ancestor tablets (chiseled quartz = tablets)
    b.set(W//2-2, 7, L-3, CHISELED_QUARTZ)
    b.set(W//2, 7, L-3, CHISELED_QUARTZ)
    b.set(W//2+2, 7, L-3, CHISELED_QUARTZ)
    # Soul lanterns above tablets
    b.set(W//2-2, 8, L-3, SOUL_LANTERN)
    b.set(W//2, 8, L-3, SOUL_LANTERN)
    b.set(W//2+2, 8, L-3, SOUL_LANTERN)

    # Corpse pits (2, on sides) — sunken areas with soulsand
    # West pit
    b.fill(2, 2, 4, 4, 2, L-5, SOUL_SAND)
    b.fill(2, 2, 4, 4, 2, L-5, AIR)
    b.fill(2, 2, 4, 2, 2, L-5, POLISHED_BLACKSTONE_BRICK_WALL)
    b.fill(4, 2, 4, 4, 2, L-5, POLISHED_BLACKSTONE_BRICK_WALL)
    # East pit
    b.fill(W-5, 2, 4, W-3, 2, L-5, SOUL_SAND)
    b.fill(W-5, 2, 4, W-3, 2, L-5, AIR)
    b.fill(W-5, 2, 4, W-5, 2, L-5, POLISHED_BLACKSTONE_BRICK_WALL)
    b.fill(W-3, 2, 4, W-3, 2, L-5, POLISHED_BLACKSTONE_BRICK_WALL)

    # Yin array (redstone pattern on floor — represents yin gathering)
    b.set(W//2, 3, L//2, REDSTONE_BLOCK)
    b.set(W//2-1, 3, L//2, REDSTONE_BLOCK)
    b.set(W//2+1, 3, L//2, REDSTONE_BLOCK)
    b.set(W//2, 3, L//2-1, REDSTONE_BLOCK)
    b.set(W//2, 3, L//2+1, REDSTONE_BLOCK)

    # Crying obsidian pillars (yin energy conduits)
    pillar(b, 2, 3, L-4, 8, CRYING_OBSIDIAN)
    pillar(b, W-3, 3, L-4, 8, CRYING_OBSIDIAN)
    pillar(b, 2, 3, 4, 8, CRYING_OBSIDIAN)
    pillar(b, W-3, 3, 4, 8, CRYING_OBSIDIAN)

    # Loot chests
    chest_with_loot(b, W//2-2, 3, 2, facing="north",
                    loot_table="ergenverse:chests/corpse_yin_ancestor")
    chest_with_loot(b, W//2+2, 3, 2, facing="north",
                    loot_table="ergenverse:chests/corpse_yin_ancestor")

    # Roof (flat, dark)
    b.fill(0, 10, 0, W-1, 10, L-1, NETHER_BRICKS)
    # Corner spires
    for (sx, sz) in [(0,0), (W-1,0), (0,L-1), (W-1,L-1)]:
        pillar(b, sx, 11, sz, 11, BLACKSTONE)
        b.set(sx, 12, sz, SOUL_LANTERN)

    save(b, "corpse_yin_sect/ancestor_hall")

# ════════════════════════════════════════════════════════════════════════
# 10. HEAVENLY FATE SECT — Star Tower (天运宗星象塔)
#     Canon: Divination sect. Observes stars, fate.
# ════════════════════════════════════════════════════════════════════════
def build_heavenly_fate_star_tower():
    print("\n[10/11] Heavenly Fate Sect — Star Tower")
    W, H, L = 11, 25, 11
    b = StructureBuilder(W, H, L)

    # Foundation
    b.fill(0, 0, 0, W-1, 2, L-1, QUARTZ_BRICKS)

    # Tower walls (quartz, white — pure/celestial)
    b.walls(1, 3, 1, W-2, 20, L-2, QUARTZ_BRICKS)
    # Door
    door_2high(b, W//2, 3, 1, facing="north", wood="birch")

    # Floor levels (mezzanines every 5 blocks)
    for fy in [8, 14]:
        b.fill(2, fy, 2, W-3, fy, L-3, BIRCH_PLANKS)
        # Spiral staircase access (leave hole)
        b.set(W//2, fy, W//2, AIR)

    # Staircase (spiral, stone brick)
    stair_centers = [(W//2, 3), (W//2+1, 4), (W//2+1, 5), (W//2, 6), (W//2-1, 7)]
    # Simple central pillar with stairs around
    pillar(b, W//2, 3, L//2, 20, QUARTZ_PILLAR)

    # Windows (star observation slits)
    for y in range(4, 20, 2):
        b.set(1, y, L//2, GLASS_PANE)
        b.set(W-2, y, L//2, GLASS_PANE)
        b.set(W//2, y, 1, GLASS_PANE)
        b.set(W//2, y, L-2, GLASS_PANE)

    # Star chart floor pattern (lapis = night sky, gold = stars)
    b.fill(2, 3, 2, W-3, 3, L-3, LAPIS_BLOCK)
    # Star points
    for (sx, sz) in [(3,3), (W-4,3), (3,L-4), (W-4,L-4), (W//2,3), (W//2,L-4)]:
        b.set(sx, 3, sz, GOLD_BLOCK)

    # Bookshelves (fate records)
    for x in [2, 3, W-4, W-3]:
        b.set(x, 4, 2, BOOKSHELF)
        b.set(x, 5, 2, BOOKSHELF)
        b.set(x, 4, L-3, BOOKSHELF)
        b.set(x, 5, L-3, BOOKSHELF)

    # Lecterns (divination charts)
    b.set(3, 4, L//2, LECTERN)
    b.set(W-4, 4, L//2, LECTERN)

    # Observatory deck (top, open)
    b.fill(1, 20, 1, W-2, 20, L-2, QUARTZ_BRICKS)
    b.fill(2, 20, 2, W-3, 20, L-3, AIR)
    # Railings
    b.fill(1, 21, 1, W-2, 21, 1, QUARTZ_BRICKS)
    b.fill(1, 21, L-2, W-2, 21, L-2, QUARTZ_BRICKS)
    b.fill(1, 21, 1, 1, 21, L-2, QUARTZ_BRICKS)
    b.fill(W-2, 21, 1, W-2, 21, L-2, QUARTZ_BRICKS)

    # Telescope (end rod pointing up + gold)
    b.set(W//2, 21, L//2, QUARTZ_PILLAR)
    b.set(W//2, 22, L//2, END_ROD)
    b.set(W//2, 23, L//2, END_ROD)

    # Roof dome (sea lantern = glowing celestial orb)
    roof_pyramidal(b, 1, 22, 1, W-2, 22, L-2, 2, QUARTZ_BRICKS, GOLD_BLOCK)
    b.set(W//2, 24, L//2, SEA_LANTERN)

    # Glowing stars on walls (end rods)
    b.set(W//2, 16, 1, END_ROD)
    b.set(W//2, 16, L-2, END_ROD)
    b.set(1, 16, L//2, END_ROD)
    b.set(W-2, 16, L//2, END_ROD)

    # Loot
    chest_with_loot(b, 3, 4, 3, facing="east",
                    loot_table="ergenverse:chests/heavenly_fate_star_tower")

    save(b, "heavenly_fate_sect/star_tower")

# ════════════════════════════════════════════════════════════════════════
# 11. WANDERING CULTIVATOR CAMP — Tent (散修帐篷)
# ════════════════════════════════════════════════════════════════════════
def build_wandering_camp_tent():
    print("\n[11/11] Wandering Cultivator Camp — Tent")
    W, H, L = 7, 5, 7
    b = StructureBuilder(W, H, L)

    # Ground (grass)
    b.fill(0, 0, 0, W-1, 0, L-1, GRASS)
    # Campfire center
    b.set(W//2, 1, L//2, CAMPFIRE)

    # Tent — wool canopy on log frame
    # Tent base (open front)
    # Corner posts
    pillar(b, 1, 1, 1, 3, OAK_LOG)
    pillar(b, W-2, 1, 1, 3, OAK_LOG)
    pillar(b, 1, 1, L-2, 3, OAK_LOG)
    pillar(b, W-2, 1, L-2, 3, OAK_LOG)
    # Ridge pole
    b.fill(1, 4, L//2, W-2, 4, L//2, OAK_LOG_X)
    # Tent roof (white wool slopes)
    for layer in range(3):
        y = 4 - layer
        offset = layer
        b.fill(1+offset, y, 1, W-2-offset, y, 1, WHITE_WOOL)
        b.fill(1+offset, y, L-2, W-2-offset, y, L-2, WHITE_WOOL)
    # Tent sides (closed back, open front)
    b.fill(1, 2, 1, W-2, 3, 1, WHITE_WOOL)
    b.fill(1, 2, L-2, 1, 3, L-2, WHITE_WOOL)
    b.fill(W-2, 2, L-2, W-2, 3, L-2, WHITE_WOOL)

    # Bedroll inside (wool)
    b.set(2, 1, L-3, RED_WOOL)
    b.set(3, 1, L-3, WHITE_WOOL)

    # Chest with wandering cultivator loot
    chest_with_loot(b, W-3, 1, L-3, facing="west",
                    loot_table="ergenverse:chests/wandering_cultivator")

    # Lantern at entrance
    b.set(W//2, 1, 1, LANTERN)

    # Sign
    sign_post(b, W//2, 1, 0, ["散修", "Wanderer", "Camp"])

    save(b, "wandering_camp/tent")

# ════════════════════════════════════════════════════════════════════════
# 12. CLOUD SKY SECT — Cloud Pavilion (凌霄宗云阁)
#     Canon: High-altitude sect on floating peaks. Cloud/mist aesthetic.
# ════════════════════════════════════════════════════════════════════════
def build_cloud_sky_pavilion():
    print("\n[12/14] Cloud Sky Sect — Cloud Pavilion")
    W, H, L = 13, 17, 13
    b = StructureBuilder(W, H, L)

    # Floating platform base (quartz — celestial)
    b.fill(0, 0, 0, W-1, 2, L-1, QUARTZ_BRICKS)
    # Underside taper (floating island look)
    b.fill(1, 0, 1, W-2, 0, L-2, QUARTZ_BRICKS)
    b.fill(2, 0, 2, W-3, 0, L-3, QUARTZ_BRICKS)

    # Floor
    b.fill(1, 3, 1, W-2, 3, L-2, SMOOTH_STONE)

    # 4 corner pillars (quartz pillars — elegant)
    pillar(b, 2, 4, 2, 10, QUARTZ_PILLAR)
    pillar(b, W-3, 4, 2, 10, QUARTZ_PILLAR)
    pillar(b, 2, 4, L-3, 10, QUARTZ_PILLAR)
    pillar(b, W-3, 4, L-3, 10, QUARTZ_PILLAR)

    # Walls (open-air pavilion — only low walls)
    b.fill(2, 4, 1, W-3, 4, 1, QUARTZ_BRICKS)       # north low wall
    b.fill(2, 4, L-2, W-3, 4, L-2, QUARTZ_BRICKS)   # south low wall
    b.fill(1, 4, 2, 1, 4, L-3, QUARTZ_BRICKS)        # west low wall
    b.fill(W-2, 4, 2, W-2, 4, L-3, QUARTZ_BRICKS)   # east low wall

    # Cloud decorations (white wool + light) at corners
    b.set(2, 11, 2, WHITE_WOOL)
    b.set(3, 11, 2, WHITE_WOOL)
    b.set(2, 11, 3, WHITE_WOOL)
    b.set(W-3, 11, 2, WHITE_WOOL)
    b.set(W-4, 11, 2, WHITE_WOOL)
    b.set(W-3, 11, 3, WHITE_WOOL)
    b.set(2, 11, L-3, WHITE_WOOL)
    b.set(W-3, 11, L-3, WHITE_WOOL)

    # Ceiling
    b.fill(1, 12, 1, W-2, 12, L-2, QUARTZ_BRICKS)

    # Multi-tiered roof (pagoda style — 3 layers)
    roof_pyramidal(b, 0, 13, 0, W-1, 13, L-1, 3, QUARTZ_BRICKS, GOLD_BLOCK)
    b.set(W//2, 16, L//2, SEA_LANTERN)  # celestial orb apex

    # Cloud mist (end rods glowing around platform)
    b.set(0, 3, 0, END_ROD)
    b.set(W-1, 3, 0, END_ROD)
    b.set(0, 3, L-1, END_ROD)
    b.set(W-1, 3, L-1, END_ROD)

    # Central altar (jade bed proxy + incense)
    b.set(W//2, 4, L//2, JADE_BED)
    b.set(W//2-2, 4, L//2, CAULDRON)
    b.set(W//2+2, 4, L//2, CAULDRON)

    # Spirit herbs around altar
    b.set(W//2, 4, L//2-2, SOUL_NOURISHING_LOTUS)
    b.set(W//2, 4, L//2+2, QI_GATHERING_GRASS)

    # Loot chest
    chest_with_loot(b, 2, 4, L-3, facing="east",
                    loot_table="ergenverse:chests/cloud_sky_pavilion")

    # Bookshelves (scripture storage)
    b.set(2, 4, 2, BOOKSHELF)
    b.set(2, 5, 2, BOOKSHELF)
    b.set(W-3, 4, 2, BOOKSHELF)
    b.set(W-3, 5, 2, BOOKSHELF)

    save(b, "cloud_sky_sect/cloud_pavilion")

# ════════════════════════════════════════════════════════════════════════
# 13. ANCIENT DEMON CITY — Gate (古魔城门)
#     Canon: Demon-influenced city. Dark, foreboding. Red nether motifs.
# ════════════════════════════════════════════════════════════════════════
def build_ancient_demon_city_gate():
    print("\n[13/14] Ancient Demon City — Gate")
    W, H, L = 15, 17, 10
    b = StructureBuilder(W, H, L)

    # Foundation (blackstone + red nether — demonic)
    b.fill(0, 0, 0, W-1, 1, L-1, BLACKSTONE)
    b.fill(0, 2, 0, W-1, 2, L-1, RED_NETHER_BRICKS)

    # Two massive gate towers
    for tx in [0, W-5]:
        b.fill(tx, 3, 2, tx+4, 12, L-3, RED_NETHER_BRICKS)
        b.clear(tx+1, 4, 3, tx+3, 11, L-4)
        # Demon-eye windows (glowing redstone)
        b.set(tx+2, 6, 2, REDSTONE_BLOCK)
        b.set(tx+2, 9, 2, REDSTONE_BLOCK)
        b.set(tx+2, 6, L-3, REDSTONE_BLOCK)
        b.set(tx+2, 9, L-3, REDSTONE_BLOCK)
        # Tower spires
        roof_pyramidal(b, tx, 13, 2, tx+4, 13, L-3, 3, RED_NETHER_BRICKS, OBSIDIAN)
        b.set(tx+2, 16, L//2, REDSTONE_BLOCK)

    # Gate archway (central)
    pillar(b, 5, 3, 2, 12, RED_NETHER_BRICKS)
    pillar(b, 5, 3, L-3, 12, RED_NETHER_BRICKS)
    pillar(b, W-6, 3, 2, 12, RED_NETHER_BRICKS)
    pillar(b, W-6, 3, L-3, 12, RED_NETHER_BRICKS)
    b.fill(5, 9, 2, W-6, 12, L-3, RED_NETHER_BRICKS)

    # Demon skull plaque (wither skull)
    b.set(W//2, 10, 2, "minecraft:wither_skeleton_skull")
    b.set(W//2, 10, L-3, "minecraft:wither_skeleton_skull")

    # Gate passage floor (basalt)
    b.fill(5, 3, 2, W-6, 3, L-3, BASALT)

    # Soul fire braziers (demonic flames)
    b.set(5, 4, 2, SOUL_CAMPFIRE)
    b.set(W-6, 4, 2, SOUL_CAMPFIRE)
    b.set(5, 4, L-3, SOUL_CAMPFIRE)
    b.set(W-6, 4, L-3, SOUL_CAMPFIRE)

    # Crying obsidian decorations (demonic tears)
    b.set(0, 12, 2, CRYING_OBSIDIAN)
    b.set(W-1, 12, 2, CRYING_OBSIDIAN)
    b.set(0, 12, L-3, CRYING_OBSIDIAN)
    b.set(W-1, 12, L-3, CRYING_OBSIDIAN)

    # Iron bar portcullis
    for y in range(4, 9):
        b.set(6, y, 2, IRON_BARS)
        b.set(7, y, 2, IRON_BARS)
        b.set(8, y, 2, IRON_BARS)
        b.set(6, y, L-3, IRON_BARS)
        b.set(7, y, L-3, IRON_BARS)
        b.set(8, y, L-3, IRON_BARS)

    save(b, "ancient_demon_city/demon_gate")

# ════════════════════════════════════════════════════════════════════════
# 14. IMMORTAL CAVE DWELLING (仙人洞府)
#     Canon: Cultivator's personal cave dwelling. Sealed, has cultivation
#     chamber, storage, spirit spring. Wang Lin's iconic home.
# ════════════════════════════════════════════════════════════════════════
def build_immortal_cave():
    print("\n[14/14] Immortal Cave Dwelling")
    W, H, L = 13, 8, 15
    b = StructureBuilder(W, H, L)

    # Cave interior (hollowed stone)
    b.fill(0, 0, 0, W-1, H-1, L-1, STONE)
    # Hollow out interior
    b.clear(1, 1, 1, W-2, H-2, L-2)
    # Floor (smooth stone)
    b.fill(1, 1, 1, W-2, 1, L-2, SMOOTH_STONE)

    # Entrance (north, z=0)
    b.clear(W//2-1, 1, 0, W//2+1, 3, 0)
    b.fill(W//2-1, 4, 0, W//2+1, 4, 0, STONE_BRICKS)

    # Cultivation chamber (back/south)
    b.fill(W//2-2, 1, L-5, W//2+2, 1, L-2, POLISHED_ANDESITE)
    # Cultivation mat (jade bed proxy)
    b.set(W//2, 2, L-3, JADE_BED)
    # Spirit gathering array (redstone pattern)
    b.set(W//2, 1, L-3, REDSTONE_BLOCK)
    b.set(W//2-1, 1, L-4, REDSTONE_BLOCK)
    b.set(W//2+1, 1, L-4, REDSTONE_BLOCK)
    b.set(W//2-1, 1, L-2, REDSTONE_BLOCK)
    b.set(W//2+1, 1, L-2, REDSTONE_BLOCK)

    # Spirit spring (water pool, west side)
    b.fill(2, 1, 4, 3, 1, 6, "minecraft:water")
    b.fill(2, 0, 4, 3, 0, 6, STONE_BRICKS)
    b.set(2, 2, 5, SEA_LANTERN)  # glowing spring

    # Storage area (east side)
    chest_with_loot(b, W-3, 2, 4, facing="west",
                    loot_table="ergenverse:chests/immortal_cave")
    chest_with_loot(b, W-3, 2, 5, facing="west",
                    loot_table="ergenverse:chests/immortal_cave")
    b.set(W-3, 2, 6, BOOKSHELF)
    b.set(W-3, 3, 6, BOOKSHELF)

    # Alchemy station
    b.set(W//2+2, 2, 4, BREWING_STAND)
    b.set(W//2+3, 2, 4, CAULDRON)

    # Spirit herbs (cultivation resources)
    b.set(3, 2, L-3, QI_GATHERING_GRASS)
    b.set(4, 2, L-3, SNOW_HEART_HERB)
    b.set(W-4, 2, L-3, SOUL_NOURISHING_LOTUS)

    # Lanterns (glowstone in ceiling)
    b.set(3, H-2, 4, GLOWSTONE)
    b.set(W-4, H-2, 4, GLOWSTONE)
    b.set(3, H-2, L-4, GLOWSTONE)
    b.set(W-4, H-2, L-4, GLOWSTONE)
    b.set(W//2, H-2, L//2, GLOWSTONE)

    # Realm sealing flag at entrance (canon: cave dwellings are sealed)
    b.set(W//2, 3, 1, REALM_SEALING_FLAG)

    # Natural stone pillars (cave supports)
    pillar(b, 2, 2, 2, H-2, COBBLE)
    pillar(b, W-3, 2, 2, H-2, COBBLE)
    pillar(b, 2, 2, L-3, H-2, COBBLE)
    pillar(b, W-3, 2, L-3, H-2, COBBLE)

    save(b, "immortal_cave/dwelling")

# ════════════════════════════════════════════════════════════════════════
# MAIN
# ════════════════════════════════════════════════════════════════════════
if __name__ == "__main__":
    print("=" * 70)
    print("Generating custom .nbt structures for Ergenverse — Planet Suzaku")
    print("=" * 70)

    build_heng_yue_main_hall()
    build_heng_yue_outer_gate()
    build_heng_yue_herb_garden()
    build_tian_shui_city_gate()
    build_tian_shui_market_stall()
    build_tian_shui_merchant_house()
    build_teng_family_keep()
    build_soul_refining_furnace()
    build_corpse_yin_ancestor_hall()
    build_heavenly_fate_star_tower()
    build_wandering_camp_tent()
    build_cloud_sky_pavilion()
    build_ancient_demon_city_gate()
    build_immortal_cave()

    print("\n" + "=" * 70)
    print("DONE — 14 custom .nbt structures generated.")
    print(f"Output: {STRUCTURE_DIR}")
    print("=" * 70)
