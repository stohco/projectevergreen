#!/usr/bin/env python3
"""
NBT Structure Builder for Ergenverse Forge Mod
Generates custom .nbt structure files for canon locations on Planet Suzaku.

MC 1.20.1 structure NBT format:
  {
    "DataVersion": 3465,
    "size": [x, y, z],
    "palette": [{"Name": "minecraft:air"}, ...],
    "entities": [],
    "blocks": [{"pos": [x,y,z], "state": <palette_index>, "nbt": {...}}, ...]
  }

Usage:
  python3 nbt_structure_builder.py
  -> writes .nbt files to src/main/resources/data/ergenverse/structure/
"""
import os
import sys
import gzip
import io
import json
from collections import OrderedDict

try:
    import nbtlib
    from nbtlib import Compound, List, Int, IntArray, String, Byte
except ImportError:
    print("ERROR: nbtlib not installed. Run: pip3 install nbtlib", file=sys.stderr)
    sys.exit(1)

# ── constants ──────────────────────────────────────────────────────────
DATA_VERSION = 3465  # MC 1.20.1
STRUCTURE_DIR = os.path.join(
    os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
    "src", "main", "resources", "data", "ergenverse", "structure"
)

# ── block state helpers ────────────────────────────────────────────────
def bs(name, **props):
    """Build a block-state string like 'minecraft:oak_log[axis=y]'."""
    if not props:
        return name
    parts = [f"{k}={v}" for k, v in sorted(props.items())]
    return f"{name}[{','.join(parts)}]"

# Common block states (saves typing)
AIR = "minecraft:air"
STONE = "minecraft:stone"
COBBLE = "minecraft:cobblestone"
STONE_BRICKS = "minecraft:stone_bricks"
MOSSY_STONE_BRICKS = "minecraft:mossy_stone_bricks"
CRACKED_STONE_BRICKS = "minecraft:cracked_stone_bricks"
DEEPSLATE_BRICKS = "minecraft:deepslate_bricks"
POLISHED_ANDESITE = "minecraft:polished_andesite"
SMOOTH_STONE = "minecraft:smooth_stone"
QUARTZ_BRICKS = "minecraft:quartz_bricks"
QUARTZ_PILLAR = bs("minecraft:quartz_pillar", axis="y")
CHISELED_QUARTZ = "minecraft:chiseled_quartz_block"
OAK_LOG = bs("minecraft:oak_log", axis="y")
OAK_LOG_X = bs("minecraft:oak_log", axis="x")
OAK_LOG_Z = bs("minecraft:oak_log", axis="z")
OAK_PLANKS = "minecraft:oak_planks"
OAK_SLAB = bs("minecraft:oak_slab", type="bottom")
OAK_SLAB_TOP = bs("minecraft:oak_slab", type="top")
OAK_STAIRS_N = bs("minecraft:oak_stairs", facing="north", half="bottom")
OAK_STAIRS_S = bs("minecraft:oak_stairs", facing="south", half="bottom")
OAK_STAIRS_E = bs("minecraft:oak_stairs", facing="east", half="bottom")
OAK_STAIRS_W = bs("minecraft:oak_stairs", facing="west", half="bottom")
SPRUCE_PLANKS = "minecraft:spruce_planks"
SPRUCE_LOG = bs("minecraft:spruce_log", axis="y")
DARK_OAK_PLANKS = "minecraft:dark_oak_planks"
DARK_OAK_LOG = bs("minecraft:dark_oak_log", axis="y")
BIRCH_PLANKS = "minecraft:birch_planks"
SPRUCE_STAIRS_N = bs("minecraft:spruce_stairs", facing="north", half="bottom")
SPRUCE_STAIRS_S = bs("minecraft:spruce_stairs", facing="south", half="bottom")
SPRUCE_STAIRS_E = bs("minecraft:spruce_stairs", facing="east", half="bottom")
SPRUCE_STAIRS_W = bs("minecraft:spruce_stairs", facing="west", half="bottom")
DARK_OAK_STAIRS_N = bs("minecraft:dark_oak_stairs", facing="north", half="bottom")
DARK_OAK_STAIRS_S = bs("minecraft:dark_oak_stairs", facing="south", half="bottom")
DARK_OAK_STAIRS_E = bs("minecraft:dark_oak_stairs", facing="east", half="bottom")
DARK_OAK_STAIRS_W = bs("minecraft:dark_oak_stairs", facing="west", half="bottom")
GLASS_PANE = "minecraft:glass_pane"
OAK_FENCE = "minecraft:oak_fence"
OAK_DOOR_L = bs("minecraft:oak_door", facing="south", half="lower", hinge="left", open="false")
OAK_DOOR_U = bs("minecraft:oak_door", half="upper", hinge="left", open="false")
IRON_BARS = "minecraft:iron_bars"
CHAIN = "minecraft:chain"
LANTERN = bs("minecraft:lantern", hanging="true")
SOUL_LANTERN = bs("minecraft:soul_lantern", hanging="true")
REDSTONE_LAMP = bs("minecraft:redstone_lamp", lit="true")
GLOWSTONE = "minecraft:glowstone"
SEA_LANTERN = "minecraft:sea_lantern"
END_ROD = "minecraft:end_rod"
CAMPFIRE = bs("minecraft:campfire", lit="true")
SOUL_CAMPFIRE = bs("minecraft:soul_campfire", lit="true")
WATER = "minecraft:water"
LILY_PAD = "minecraft:lily_pad"
GRASS = "minecraft:grass"
TALL_GRASS = bs("minecraft:tall_grass", half="lower")
FLOWER_RED = "minecraft:poppy"
FLOWER_YELLOW = "minecraft:dandelion"
AZURE_BLUET = "minecraft:azure_bluet"
OAK_LEAVES = "minecraft:oak_leaves"
SPRUCE_LEAVES = "minecraft:spruce_leaves"
DARK_OAK_LEAVES = "minecraft:dark_oak_leaves"
PODZOL = "minecraft:podzol"
COARSE_DIRT = "minecraft:coarse_dirt"
DIRT_PATH = "minecraft:dirt_path"
SAND = "minecraft:sand"
RED_SAND = "minecraft:red_sand"
GRAVEL = "minecraft:gravel"
CLAY = "minecraft:clay"
OBSIDIAN = "minecraft:obsidian"
CRYING_OBSIDIAN = "minecraft:crying_obsidian"
NETHER_BRICKS = "minecraft:nether_bricks"
RED_NETHER_BRICKS = "minecraft:red_nether_bricks"
BLACKSTONE = "minecraft:blackstone"
BASALT = bs("minecraft:basalt", axis="y")
SOUL_SOIL = "minecraft:soul_soil"
SOUL_SAND = "minecraft:soul_sand"
MAGMA = "minecraft:magma_block"
LAVA = "minecraft:lava"
BLUE_ICE = "minecraft:blue_ice"
PACKED_ICE = "minecraft:packed_ice"
SNOW_BLOCK = "minecraft:snow_block"
PRISMARINE = "minecraft:prismarine"
PRISMARINE_BRICKS = "minecraft:prismarine_bricks"
DARK_PRISMARINE = "minecraft:dark_prismarine"
GOLD_BLOCK = "minecraft:gold_block"
IRON_BLOCK = "minecraft:iron_block"
DIAMOND_BLOCK = "minecraft:diamond_block"
EMERALD_BLOCK = "minecraft:emerald_block"
LAPIS_BLOCK = "minecraft:lapis_block"
REDSTONE_BLOCK = "minecraft:redstone_block"
NETHERITE_BLOCK = "minecraft:netherite_block"
CHEST = "minecraft:chest"
ENDER_CHEST = "minecraft:ender_chest"
BOOKSHELF = "minecraft:bookshelf"
LECTERN = bs("minecraft:lectern", facing="south")
CRAFTING_TABLE = "minecraft:crafting_table"
FLETCHING_TABLE = "minecraft:fletching_table"
SMITHING_TABLE = "minecraft:smithing_table"
CAULDRON = "minecraft:cauldron"
BREWING_STAND = "minecraft:brewing_stand"
ANVIL = "minecraft:anvil"
CHIPPED_ANVIL = "minecraft:chipped_anvil"
BELL = bs("minecraft:bell", attachment="floor", facing="south")
FLOWER_POT = "minecraft:flower_pot"
ARMOR_STAND = "minecraft:armor_stand"
ITEM_FRAME = bs("minecraft:item_frame", facing="south")
PAINTING = "minecraft:painting"
WHITE_CARPET = "minecraft:white_carpet"
RED_CARPET = "minecraft:red_carpet"
YELLOW_CARPET = "minecraft:yellow_carpet"
BLUE_CARPET = "minecraft:blue_carpet"
GREEN_CARPET = "minecraft:green_carpet"
BLACK_CARPET = "minecraft:black_carpet"
WHITE_WOOL = "minecraft:white_wool"
RED_WOOL = "minecraft:red_wool"
BLACK_WOOL = "minecraft:black_wool"
GOLD_WOOL = "minecraft:yellow_wool"
WHITE_CONCRETE = "minecraft:white_concrete"
BLACK_CONCRETE = "minecraft:black_concrete"
RED_CONCRETE = "minecraft:red_concrete"
TERRACOTTA = "minecraft:terracotta"
WHITE_TERRACOTTA = "minecraft:white_terracotta"
BLACK_TERRACOTTA = "minecraft:black_terracotta"
CHISELED_STONE_BRICKS = "minecraft:chiseled_stone_bricks"
STONE_BRICK_STAIRS_N = bs("minecraft:stone_brick_stairs", facing="north", half="bottom")
STONE_BRICK_STAIRS_S = bs("minecraft:stone_brick_stairs", facing="south", half="bottom")
STONE_BRICK_STAIRS_E = bs("minecraft:stone_brick_stairs", facing="east", half="bottom")
STONE_BRICK_STAIRS_W = bs("minecraft:stone_brick_stairs", facing="west", half="bottom")
STONE_BRICK_SLAB = bs("minecraft:stone_brick_slab", type="bottom")
COBBLE_WALL = "minecraft:cobblestone_wall"
STONE_BRICK_WALL = "minecraft:stone_brick_wall"
MOSSY_STONE_BRICK_WALL = "minecraft:mossy_stone_brick_wall"
POLISHED_BLACKSTONE_BRICKS = "minecraft:polished_blackstone_bricks"
POLISHED_BLACKSTONE_BRICK_WALL = "minecraft:polished_blackstone_brick_wall"
GILDED_BLACKSTONE = "minecraft:gilded_blackstone"
LODESTONE = "minecraft:lodestone"
RESPAWN_ANCHOR = bs("minecraft:respawn_anchor", charges="4")

# Ergenverse custom blocks (must exist in mod)
def eg(block):
    return f"ergenverse:{block}"

SPIRIT_STONE_ORE = eg("spirit_stone_ore")
REALM_SEALING_FLAG = eg("realm_sealing_flag")
QI_GATHERING_GRASS = eg("qi_gathering_grass")
SNOW_HEART_HERB = eg("snow_heart_herb")
SOUL_NOURISHING_LOTUS = eg("soul_nourishing_lotus")
FOUNDATION_ROOT_VINE = eg("foundation_root_vine")
SWORD_EDGE_MOSS = eg("sword_edge_moss")
NINE_LEAF_CLOVER = eg("nine_leaf_clover")
# jade_bed and cultivation_mat not yet registered — use vanilla proxies
JADE_BED = "minecraft:emerald_block"  # jade = green gemstone proxy
CULTIVATION_MAT = "minecraft:white_carpet"  # meditation mat proxy

# ── StructureBuilder ───────────────────────────────────────────────────
class StructureBuilder:
    """3D block grid that exports to MC 1.20.1 .nbt structure format."""

    def __init__(self, width, height, length):
        self.w = width
        self.h = height
        self.l = length
        # blocks: dict (x,y,z) -> (blockstate_str, nbt_dict_or_None)
        self.blocks = {}

    def _check(self, x, y, z):
        if not (0 <= x < self.w and 0 <= y < self.h and 0 <= z < self.l):
            raise IndexError(f"Block ({x},{y},{z}) out of bounds {self.w}x{self.h}x{self.l}")

    def set(self, x, y, z, blockstate, nbt=None):
        self._check(x, y, z)
        self.blocks[(x, y, z)] = (blockstate, nbt)

    def fill(self, x0, y0, z0, x1, y1, z1, blockstate, nbt=None):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            for y in range(min(y0, y1), max(y0, y1) + 1):
                for z in range(min(z0, z1), max(z0, z1) + 1):
                    self.set(x, y, z, blockstate, nbt)

    def clear(self, x0, y0, z0, x1, y1, z1):
        self.fill(x0, y0, z0, x1, y1, z1, AIR)

    def hollow_box(self, x0, y0, z0, x1, y1, z1, blockstate):
        """Fill only the shell of a box (walls + floor + ceiling), interior = air."""
        self.fill(x0, y0, z0, x1, y0, z1, blockstate)  # floor
        self.fill(x0, y1, z0, x1, y1, z1, blockstate)  # ceiling
        self.fill(x0, y0, z0, x0, y1, z1, blockstate)  # west wall
        self.fill(x1, y0, z0, x1, y1, z1, blockstate)  # east wall
        self.fill(x0, y0, z0, x1, y1, z0, blockstate)  # north wall
        self.fill(x0, y0, z1, x1, y1, z1, blockstate)  # south wall

    def walls(self, x0, y0, z0, x1, y1, z1, blockstate):
        """Fill only the 4 walls (no floor/ceiling)."""
        self.fill(x0, y0, z0, x0, y1, z1, blockstate)
        self.fill(x1, y0, z0, x1, y1, z1, blockstate)
        self.fill(x0, y0, z0, x1, y1, z0, blockstate)
        self.fill(x0, y0, z1, x1, y1, z1, blockstate)

    def floor(self, x0, z0, x1, z1, y, blockstate):
        self.fill(x0, y, z0, x1, y, z1, blockstate)

    def ceiling(self, x0, z0, x1, z1, y, blockstate):
        self.fill(x0, y, z0, x1, y, z1, blockstate)

    def line_x(self, x0, x1, y, z, blockstate):
        for x in range(min(x0, x1), max(x0, x1) + 1):
            self.set(x, y, z, blockstate)

    def line_z(self, z0, z1, x, y, blockstate):
        for z in range(min(z0, z1), max(z0, z1) + 1):
            self.set(x, y, z, blockstate)

    def staircase_s(self, x, y0, z0, steps, blockstate, facing="south"):
        """Staircase going south, ascending."""
        stair = {
            "north": STONE_BRICK_STAIRS_N, "south": STONE_BRICK_STAIRS_S,
            "east": STONE_BRICK_STAIRS_E, "west": STONE_BRICK_STAIRS_W
        }[facing]
        for i in range(steps):
            self.set(x, y0 + i, z0 + i, stair)

    def export_nbt(self, filepath):
        """Export to gzipped NBT structure file."""
        os.makedirs(os.path.dirname(filepath), exist_ok=True)

        def to_nbt(obj):
            """Recursively convert Python objects to nbtlib tags."""
            if isinstance(obj, bool):
                return Byte(1 if obj else 0)
            elif isinstance(obj, int):
                return Int(obj)
            elif isinstance(obj, float):
                from nbtlib import Float as NbtFloat
                return NbtFloat(obj)
            elif isinstance(obj, str):
                return String(obj)
            elif isinstance(obj, dict):
                return Compound({k: to_nbt(v) for k, v in obj.items()})
            elif isinstance(obj, (list, tuple)):
                # Infer element type from first element, default to Compound
                if obj and isinstance(obj[0], dict):
                    return List[Compound]([to_nbt(x) for x in obj])
                elif obj and isinstance(obj[0], int):
                    return List[Int]([to_nbt(x) for x in obj])
                else:
                    return List[String]([to_nbt(x) for x in obj])
            elif isinstance(obj, (Compound, List, Int, String, Byte)):
                return obj
            return String(str(obj))

        # Build palette (unique blockstates)
        palette_list = []
        palette_idx = {}
        for (bs_str, _) in self.blocks.values():
            if bs_str not in palette_idx:
                palette_idx[bs_str] = len(palette_list)
                palette_list.append(bs_str)

        # Build blocks list (sorted by pos for deterministic output)
        blocks_list = []
        for (x, y, z) in sorted(self.blocks.keys()):
            bs_str, nbt_data = self.blocks[(x, y, z)]
            idx = palette_idx[bs_str]
            entry = {"pos": [x, y, z], "state": idx}
            if nbt_data:
                entry["nbt"] = nbt_data
            blocks_list.append(entry)

        # Parse blockstate strings into palette compounds
        palette_compounds = []
        for bs_str in palette_list:
            if "[" in bs_str:
                name_part = bs_str[:bs_str.index("[")]
                props_part = bs_str[bs_str.index("[") + 1:bs_str.index("]")]
                props = {}
                for kv in props_part.split(","):
                    k, v = kv.split("=")
                    props[k] = String(v)
                palette_compounds.append(Compound({
                    "Name": String(name_part),
                    "Properties": Compound(props)
                }))
            else:
                palette_compounds.append(Compound({"Name": String(bs_str)}))

        # Build blocks compounds with proper NBT conversion
        blocks_compounds = []
        for b in blocks_list:
            entry = {
                "pos": List[Int]([Int(b["pos"][0]), Int(b["pos"][1]), Int(b["pos"][2])]),
                "state": Int(b["state"])
            }
            if "nbt" in b:
                entry["nbt"] = to_nbt(b["nbt"])
            blocks_compounds.append(Compound(entry))

        # Build NBT compound
        root = Compound({
            "DataVersion": Int(DATA_VERSION),
            "size": List[Int]([Int(self.w), Int(self.h), Int(self.l)]),
            "palette": List[Compound](palette_compounds),
            "entities": List[Compound]([]),
            "blocks": List[Compound](blocks_compounds)
        })

        # Write as gzipped NBT
        nbt_file = nbtlib.File(root, gzipped=True)
        nbt_file.save(filepath)
        return filepath

    def stats(self):
        non_air = sum(1 for (bs_str, _) in self.blocks.values() if bs_str != AIR)
        return f"{self.w}x{self.h}x{self.l} = {self.w*self.h*self.l} cells, {non_air} non-air blocks"


# ── helper builders ────────────────────────────────────────────────────
def roof_pyramidal(b: StructureBuilder, x0, y0, z0, x1, y1, z1, height, blockstate, edge_block=None):
    """Pyramidal roof (like East Asian pagoda). Each layer shrinks by 1 on each side.
    y0 is the starting Y (base of roof). height layers rise from y0 upward."""
    edge = edge_block or blockstate
    for layer in range(height):
        cx0, cz0 = x0 + layer, z0 + layer
        cx1, cz1 = x1 - layer, z1 - layer
        if cx0 > cx1 or cz0 > cz1:
            break
        y = y0 + layer
        # Fill ring (edges use edge_block, interior uses blockstate)
        for x in range(cx0, cx1 + 1):
            b.set(x, y, cz0, edge)
            b.set(x, y, cz1, edge)
        for z in range(cz0, cz1 + 1):
            b.set(cx0, y, z, edge)
            b.set(cx1, y, z, edge)
        # Fill interior
        if cx0 < cx1 and cz0 < cz1:
            b.fill(cx0 + 1, y, cz0 + 1, cx1 - 1, y, cz1 - 1, blockstate)

def roof_gabled(b: StructureBuilder, x0, y0, z0, x1, y1, z1, height, blockstate, ridge_block=None):
    """Gabled roof with a ridge beam along the Z axis.
    y0 is the starting Y (base of roof). height layers rise from y0 upward."""
    ridge = ridge_block or blockstate
    half = (x1 - x0) // 2
    for layer in range(height):
        y = y0 + layer
        offset = layer
        # Roof slopes inward as it rises
        if offset > half:
            break
        lx0, lx1 = x0 + offset, x1 - offset
        if lx0 <= lx1:
            b.fill(lx0, y, z0, lx1, y, z1, blockstate)
    # Ridge beam at top
    top_y = y0 + min(height - 1, half)
    mid_x = (x0 + x1) // 2
    b.fill(mid_x, top_y + 1, z0, mid_x, top_y + 1, z1, ridge)

def door_2high(b: StructureBuilder, x, y, z, facing="south", wood="oak"):
    """Place a 2-high door."""
    door_lower = bs(f"minecraft:{wood}_door", facing=facing, half="lower", hinge="left", open="false")
    door_upper = bs(f"minecraft:{wood}_door", half="upper", hinge="left", open="false")
    b.set(x, y, z, door_lower)
    b.set(x, y + 1, z, door_upper)

def window_cross(b: StructureBuilder, x, y, z, frame_block, pane=GLASS_PANE):
    """Cross-shaped window with frame."""
    b.set(x, y, z, pane)
    b.set(x - 1, y, z, frame_block)
    b.set(x + 1, y, z, frame_block)
    b.set(x, y - 1, z, frame_block)
    b.set(x, y + 1, z, frame_block)

def pillar(b: StructureBuilder, x, z, y0, y1, blockstate):
    """Vertical pillar from y0 to y1."""
    for y in range(y0, y1 + 1):
        b.set(x, y, z, blockstate)

def lantern_hang(b: StructureBuilder, x, y, z, lantern=LANTERN):
    """Hang a lantern from ceiling at (x,y,z) — y is ceiling block, lantern goes below."""
    b.set(x, y - 1, z, lantern)

def chest_with_loot(b: StructureBuilder, x, y, z, facing="south", loot_table=None):
    """Place a chest with optional loot table reference."""
    chest_state = bs("minecraft:chest", facing=facing, type="single", waterlogged="false")
    nbt = {"id": "minecraft:chest", "Items": []}
    if loot_table:
        nbt["LootTable"] = loot_table
        nbt["LootTableSeed"] = 0
    b.set(x, y, z, chest_state, nbt)

def sign_post(b: StructureBuilder, x, y, z, text_lines=None, wood="oak"):
    """Standing sign with text."""
    sign_state = bs(f"minecraft:{wood}_sign", rotation="0", waterlogged="false")
    nbt = {"id": f"minecraft:{wood}_sign", "Text1": '{"text":""}', "Text2": '{"text":""}',
           "Text3": '{"text":""}', "Text4": '{"text":""}'}
    if text_lines:
        for i, line in enumerate(text_lines[:4]):
            nbt[f"Text{i+1}"] = json.dumps({"text": line})
    b.set(x, y, z, sign_state, nbt)

# ── main ───────────────────────────────────────────────────────────────
if __name__ == "__main__":
    print("NBT Structure Builder library loaded.")
    print(f"Structure output dir: {STRUCTURE_DIR}")
    print(f"DataVersion: {DATA_VERSION} (MC 1.20.1)")
