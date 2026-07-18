#!/usr/bin/env python3
"""
Generate Minecraft 1.20.1 structure NBT files for Heng Yue Sect buildings.

Minecraft structure .nbt format:
  Root: unnamed TAG_Compound
    "size": TAG_Int_Array [x, y, z]
    "palette": TAG_List of TAG_Compound [{"Name": "block_id"}, ...]
    "blocks": TAG_List of TAG_Compound [{"pos": [x,y,z], "state": palette_index}, ...]

Per Constitution Article XXII: canon entries must become gameplay.
Per Article XXIII: vertical slice completion.
Per Article XXVI: build content, not infrastructure.

Blocks used (Minecraft 1.20.1 block IDs):
  Stone bricks for walls/floors (Chinese temple aesthetic)
  Lanterns for lighting
  Bookshelves for library
  Cauldrons/crafting tables for alchemy
  Spruce/dark oak wood for beams and details
"""

import struct
import os
import sys

# ═══════════════════════════════════════════════════════════
#  NBT Binary Writer
# ═══════════════════════════════════════════════════════════

# NBT tag type IDs
TAG_END = 0
TAG_BYTE = 1
TAG_SHORT = 2
TAG_INT = 3
TAG_LONG = 4
TAG_FLOAT = 5
TAG_DOUBLE = 6
TAG_BYTE_ARRAY = 7
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10
TAG_INT_ARRAY = 11
TAG_LONG_ARRAY = 12


def write_nbt_string(name, value):
    """Write a TAG_String with name."""
    data = struct.pack('>b', TAG_STRING)
    if name is not None:
        encoded = name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
    encoded_val = value.encode('utf-8')
    data += struct.pack('>H', len(encoded_val)) + encoded_val
    return data


def write_nbt_int(name, value):
    """Write a TAG_Int with name."""
    data = struct.pack('>b', TAG_INT)
    if name is not None:
        encoded = name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
    data += struct.pack('>i', value)
    return data


def write_nbt_int_array(name, values):
    """Write a TAG_Int_Array with name."""
    data = struct.pack('>b', TAG_INT_ARRAY)
    if name is not None:
        encoded = name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
    data += struct.pack('>i', len(values))
    for v in values:
        data += struct.pack('>i', v)
    return data


def write_nbt_compound_start(name=None):
    """Write TAG_Compound header."""
    data = struct.pack('>b', TAG_COMPOUND)
    if name is not None:
        encoded = name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
    return data


def write_nbt_list_start(name, payload_type, length):
    """Write TAG_List header (elements follow)."""
    data = struct.pack('>b', TAG_LIST)
    if name is not None:
        encoded = name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
    data += struct.pack('>b', payload_type)
    data += struct.pack('>i', length)
    return data


def write_nbt_compound_element(name, payload):
    """Write a named TAG_Compound element (for palette entries)."""
    data = write_nbt_compound_start(name)
    data += payload
    data += struct.pack('>b', TAG_END)
    return data


def write_block_state_compound(block_id):
    """
    Write a palette entry compound for a block state string.
    MC 1.20.1 format: {Name: "block_id", Properties: {key: "val", ...}}
    If the block has no properties (no '['), just writes {Name: "block_id"}.
    """
    data = b''
    if '[' in block_id:
        name, props_str = block_id.split('[', 1)
        props_str = props_str.rstrip(']')
        # Write Name
        data += write_nbt_string("Name", name)
        # Write Properties compound
        data += struct.pack('>b', TAG_COMPOUND)
        prop_name = "Properties"
        encoded = prop_name.encode('utf-8')
        data += struct.pack('>H', len(encoded)) + encoded
        # Parse "key1=val1,key2=val2"
        for prop in props_str.split(','):
            if '=' in prop:
                k, v = prop.split('=', 1)
                data += write_nbt_string(k, v)
        data += struct.pack('>b', TAG_END)  # End Properties
    else:
        data += write_nbt_string("Name", block_id)
    return data


# ═══════════════════════════════════════════════════════════
#  Structure Builder
# ═══════════════════════════════════════════════════════════

class StructureBuilder:
    """Builds a Minecraft structure NBT file block by block."""

    def __init__(self, name):
        self.name = name
        self.palette = []  # list of block_id strings
        self.palette_index = {}  # block_id -> index
        self.blocks = []  # list of (x, y, z, palette_index)

    def _get_palette_index(self, block_id):
        """Get or create palette index for a block ID."""
        if block_id not in self.palette_index:
            self.palette_index[block_id] = len(self.palette)
            self.palette.append(block_id)
        return self.palette_index[block_id]

    def set_block(self, x, y, z, block_id):
        """Set a block at the given position."""
        idx = self._get_palette_index(block_id)
        self.blocks.append((x, y, z, idx))

    def fill_rect(self, x1, y1, z1, x2, y2, z2, block_id):
        """Fill a rectangular region."""
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for y in range(min(y1, y2), max(y1, y2) + 1):
                for z in range(min(z1, z2), max(z1, z2) + 1):
                    self.set_block(x, y, z, block_id)

    def floor_rect(self, x1, y1, z1, x2, y2, z2, block_id):
        """Place a flat rectangle at a single y level (y1 should equal y2)."""
        y = y1
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                self.set_block(x, y, z, block_id)

    def walls_rect(self, x1, y1, z1, x2, y2, z2, block_id, thickness=1):
        """Place walls (hollow box)."""
        for x in range(min(x1, x2), max(x1, x2) + 1):
            for z in range(min(z1, z2), max(z1, z2) + 1):
                for dy in range(y2 - y1 + 1):
                    # Only place on perimeter
                    on_x_edge = (x == min(x1, x2) or x == max(x1, x2))
                    on_z_edge = (z == min(z1, z2) or z == max(z1, z2))
                    if on_x_edge or on_z_edge:
                        self.set_block(x, y1 + dy, z, block_id)

    def column(self, x, y1, z, y2, block_id):
        """Place a vertical column."""
        for y in range(min(y1, y2), max(y1, y2) + 1):
            self.set_block(x, y, z, block_id)

    def remove_block(self, x, y, z):
        """Remove a block (set to air)."""
        # Find and remove the block at this position
        self.blocks = [(bx, by, bz, bi) for bx, by, bz, bi in self.blocks
                       if not (bx == x and by == y and bz == z)]

    def render(self):
        """Render the structure to NBT binary data."""
        if not self.blocks:
            return b''

        # Compute size
        max_x = max(b[0] for b in self.blocks)
        max_y = max(b[1] for b in self.blocks)
        max_z = max(b[2] for b in self.blocks)
        size_x = max_x + 1
        size_y = max_y + 1
        size_z = max_z + 1

        # Ensure air is in palette (palette index 0 should be air for unused spaces)
        air_idx = self._get_palette_index("minecraft:air")

        # Start building NBT
        nbt = b''
        nbt += write_nbt_compound_start(None)  # Root compound, no name

        # "size" tag
        nbt += write_nbt_int_array("size", [size_x, size_y, size_z])

        # "palette" tag — TAG_List of TAG_Compound
        nbt += write_nbt_list_start("palette", TAG_COMPOUND, len(self.palette))
        for block_id in self.palette:
            nbt += write_block_state_compound(block_id)
            nbt += struct.pack('>b', TAG_END)  # End of this palette compound
        # No TAG_End for the list itself — list length is known

        # "blocks" tag — TAG_List of TAG_Compound
        nbt += write_nbt_list_start("blocks", TAG_COMPOUND, len(self.blocks))
        for x, y, z, state_idx in self.blocks:
            nbt += write_nbt_int_array("pos", [x, y, z])
            nbt += write_nbt_int("state", state_idx)
            nbt += struct.pack('>b', TAG_END)  # End of this block compound
        # No TAG_End for the list

        # End of root compound
        nbt += struct.pack('>b', TAG_END)

        return nbt


# ═══════════════════════════════════════════════════════════
#  Heng Yue Sect Buildings
# ═══════════════════════════════════════════════════════════

# Block palette for Chinese-cultivation-sect aesthetic
STONE = "minecraft:stone_bricks"
STONE_CHISELED = "minecraft:chiseled_stone_bricks"
STONE_CRACKED = "minecraft:cracked_stone_bricks"
STONE_SLAB = "minecraft:stone_brick_slab[type=bottom,waterlogged=false]"
STONE_STAIRS = "minecraft:stone_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]"
SMOOTH_STONE = "minecraft:smooth_stone"
DARK_OAK_PLANKS = "minecraft:dark_oak_planks"
SPRUCE_PLANKS = "minecraft:spruce_planks"
SPRUCE_FENCE = "minecraft:spruce_fence"
LANTERN = "minecraft:lantern"
BOOKSHELF = "minecraft:bookshelf"
CAULDRON = "minecraft:cauldron"
CRAFTING_TABLE = "minecraft:crafting_table"
GLASS_PANE = "minecraft:glass_pane"
DARK_OAK_LOG = "minecraft:dark_oak_log[axis=y]"
SPRUCE_LOG = "minecraft:spruce_log[axis=y]"
SPRUCE_DOOR = "minecraft:spruce_door[facing=east,half=lower,hinge=left,open=false,powered=false]"
GLOWSTONE = "minecraft:glowstone"
AIR = "minecraft:air"
COBBLESTONE = "minecraft:cobblestone"
MOSSY_COBBLESTONE = "minecraft:mossy_cobblestone"
GRAVEL = "minecraft:gravel"
WATER = "minecraft:water[level=0]"
IRON_BARS = "minecraft:iron_bars"
CHEST = "minecraft:chest[facing=north,type=single,waterlogged=false]"
BREWING_STAND = "minecraft:brewing_stand[has_bottle_0=false,has_bottle_1=false,has_bottle_2=false]"
FURNACE = "minecraft:furnace[facing=north,lit=false]"
SOUL_SAND = "minecraft:soul_sand"


def build_sword_peak():
    """
    Sword Peak (剑峰) — the most iconic Heng Yue location.
    Canon: A flat mountain-top training ground where disciples practice
    sword arts. Features a stone arena, training dummies (stone pillars),
    a small shelter, and a weapon rack area.
    """
    s = StructureBuilder("sword_peak")

    # Foundation platform (15x1x15 stone bricks)
    s.floor_rect(0, 0, 0, 14, 0, 14, STONE)

    # Stone pillar training dummies (4 pillars at cardinal points of arena)
    for px, pz in [(3, 3), (11, 3), (3, 11), (11, 11)]:
        s.column(px, 1, pz, 4, STONE)
        s.set_block(px, 5, pz, STONE_CHISELED)  # Chiseled cap

    # Central sword-fighting arena (flat area with smooth stone)
    s.floor_rect(5, 0, 5, 9, 0, 9, SMOOTH_STONE)

    # Small shelter on the north side (7x4x5)
    sx, sz = 4, 0
    # Floor
    s.floor_rect(sx, 1, sz, sx + 6, 1, sz + 4, SPRUCE_PLANKS)
    # Walls
    s.walls_rect(sx, 2, sz, sx + 6, 4, sz + 4, STONE, thickness=1)
    # Roof (dark oak planks)
    s.floor_rect(sx, 5, sz, sx + 6, 5, sz + 4, DARK_OAK_PLANKS)
    # Door opening (remove blocks)
    for dy in range(1, 5):
        s.remove_block(sx + 3, dy, sz + 4)
    # Lantern inside shelter
    s.set_block(sx + 3, 2, sz + 2, LANTERN)

    # Steps up to shelter
    for i in range(1, 3):
        s.set_block(sx + 3, i, sz + 5, STONE_SLAB.replace("bottom", "top"))

    # Fence perimeter around training area
    for x in range(1, 14):
        s.set_block(x, 1, 0, SPRUCE_FENCE)
        s.set_block(x, 1, 14, SPRUCE_FENCE)
    for z in range(1, 14):
        s.set_block(0, 1, z, SPRUCE_FENCE)
        s.set_block(14, 1, z, SPRUCE_FENCE)

    # Corner pillars for fence
    for cx, cz in [(0, 0), (14, 0), (0, 14), (14, 14)]:
        s.column(cx, 1, cz, 2, STONE)

    return s


def build_alchemy_courtyard():
    """
    Alchemy Courtyard (炼丹房) — where pills and elixirs are refined.
    Canon: Heng Yue's alchemy hall. Features cauldrons, furnaces,
    brewing stands, ingredient storage, and a chimney.
    """
    s = StructureBuilder("alchemy_courtyard")

    # Courtyard floor (16x1x16)
    s.floor_rect(0, 0, 0, 15, 0, 15, STONE)

    # Inner floor (smoother stone for work area)
    s.floor_rect(2, 0, 2, 13, 0, 13, SMOOTH_STONE)

    # Walls (perimeter, 4 blocks high)
    s.walls_rect(0, 1, 0, 15, 4, 15, STONE)

    # Door openings (south wall center, east wall center)
    for dy in range(1, 5):
        s.remove_block(8, dy, 15)  # South door
        s.remove_block(15, dy, 8)  # East door

    # Roof
    s.floor_rect(0, 5, 0, 15, 5, 15, DARK_OAK_PLANKS)

    # Alchemy stations (3 furnaces along west wall)
    for i, z in enumerate([4, 7, 10]):
        s.set_block(2, 1, z, FURNACE)
        s.set_block(2, 2, z, AIR)  # Keep above clear
        # Chimney above each furnace
        s.column(2, 6, z, 8, COBBLESTONE)
        # Lantern near each station
        s.set_block(3, 1, z, LANTERN)

    # Brewing area (east side, 3 brewing stands)
    for z in [4, 7, 10]:
        s.set_block(13, 1, z, BREWING_STAND)
        s.set_block(14, 1, z, CAULDRON)  # Water source cauldron nearby

    # Ingredient storage (south side, chests + bookshelves)
    for x in [4, 6, 8, 10, 12]:
        s.set_block(x, 1, 14, BOOKSHELF)
    s.set_block(7, 1, 13, CHEST)  # Main ingredient chest
    s.set_block(9, 1, 13, CHEST)  # Secondary chest

    # Central work table
    s.set_block(7, 1, 7, CRAFTING_TABLE)
    s.set_block(8, 1, 7, CRAFTING_TABLE)

    # Soul sand array (for advanced alchemy — INFERRED)
    s.floor_rect(5, 0, 2, 10, 0, 3, SOUL_SAND)

    # Lanterns for lighting
    for x, z in [(4, 4), (11, 4), (4, 11), (11, 11), (7, 7)]:
        s.set_block(x, 4, z, LANTERN)

    # Glowing interior
    s.set_block(7, 4, 9, GLOWSTONE)
    s.set_block(8, 4, 5, GLOWSTONE)

    return s


def build_library():
    """
    Library / Scripture Pavilion (藏经阁) — where cultivation techniques
    and jade slips are stored.
    Canon: Contains the sect's accumulated knowledge. Wang Lin would have
    studied here. Features bookshelves, reading desks, and a lectern.
    """
    s = StructureBuilder("library")

    # Floor (12x1x10)
    s.floor_rect(0, 0, 0, 11, 0, 9, SPRUCE_PLANKS)

    # Walls (3 blocks high)
    s.walls_rect(0, 1, 0, 11, 3, 9, STONE)

    # Door opening (south center)
    for dy in range(1, 4):
        s.remove_block(6, dy, 9)
        s.remove_block(5, dy, 9)

    # Roof
    s.floor_rect(0, 4, 0, 11, 4, 9, DARK_OAK_PLANKS)
    # Decorative roof ridge
    for x in range(1, 11):
        s.set_block(x, 5, 4, DARK_OAK_LOG)
        s.set_block(x, 5, 5, DARK_OAK_LOG)

    # Bookshelves along all walls (interior)
    # North wall
    for x in range(1, 11):
        s.set_block(x, 1, 0, BOOKSHELF)
        s.set_block(x, 2, 0, BOOKSHELF)
    # South wall (flanking door)
    for x in range(1, 5):
        s.set_block(x, 1, 8, BOOKSHELF)
        s.set_block(x, 2, 8, BOOKSHELF)
    for x in range(7, 11):
        s.set_block(x, 1, 8, BOOKSHELF)
        s.set_block(x, 2, 8, BOOKSHELF)
    # West wall
    for z in range(1, 8):
        s.set_block(0, 1, z, BOOKSHELF)
        s.set_block(0, 2, z, BOOKSHELF)
    # East wall
    for z in range(1, 8):
        s.set_block(11, 1, z, BOOKSHELF)
        s.set_block(11, 2, z, BOOKSHELF)

    # Reading desks (crafting tables as study desks)
    s.set_block(3, 1, 4, CRAFTING_TABLE)
    s.set_block(8, 1, 4, CRAFTING_TABLE)

    # Central aisle (smooth stone floor)
    s.floor_rect(5, 0, 1, 6, 0, 7, SMOOTH_STONE)

    # Lanterns
    s.set_block(3, 3, 3, LANTERN)
    s.set_block(8, 3, 6, LANTERN)
    s.set_block(5, 3, 4, GLOWSTONE)

    # Chest for rare jade slips (back of library, north wall center)
    s.set_block(5, 1, 1, CHEST)
    s.set_block(6, 1, 1, CHEST)

    # Entrance step
    s.set_block(5, 0, 10, STONE_SLAB)
    s.set_block(6, 0, 10, STONE_SLAB)

    return s


def build_mountain_cave():
    """
    Mountain Cave (密洞) — the cave where Wang Lin found the
    Heaven Defying Bead.
    Canon: A hidden cave behind the Heng Yue Sect's back mountain.
    Features a rough stone interior, a pool of water, and
    mysterious glow from spirit energy.
    """
    s = StructureBuilder("mountain_cave")

    # Cave floor (irregular, 12x1x12)
    s.floor_rect(0, 0, 0, 11, 0, 11, COBBLESTONE)

    # Cave walls (rough stone, varying height for natural feel)
    # North wall (5-6 high)
    for x in range(12):
        height = 5 + (x % 3)
        for y in range(1, height):
            s.set_block(x, y, 0, MOSSY_COBBLESTONE if (x + y) % 3 == 0 else STONE_CRACKED)

    # South wall (4-5 high, with entrance gap)
    for x in range(12):
        if 5 <= x <= 6:
            continue  # Entrance gap
        height = 4 + (x % 2)
        for y in range(1, height):
            s.set_block(x, y, 11, MOSSY_COBBLESTONE if (x + y) % 4 == 0 else COBBLESTONE)

    # West wall (5 high)
    for z in range(12):
        height = 5 + (z % 2)
        for y in range(1, height):
            s.set_block(0, y, z, MOSSY_COBBLESTONE if (z + y) % 3 == 0 else STONE_CRACKED)

    # East wall (5-6 high)
    for z in range(12):
        height = 5 + (z % 3)
        for y in range(1, height):
            s.set_block(11, y, z, MOSSY_COBBLESTONE if (z + y) % 4 == 0 else COBBLESTONE)

    # Ceiling (partial, gives a cavernous feel)
    for x in range(1, 11):
        for z in range(1, 11):
            if (x + z) % 3 != 0:  # Leave some gaps for rough look
                s.set_block(x, 5, z, STONE_CRACKED if (x * z) % 5 == 0 else COBBLESTONE)

    # Spirit pool (water in center of cave) — canon: pools of spiritual energy
    s.floor_rect(4, 0, 4, 7, 0, 7, WATER)

    # Glowstone beneath pool edges (spirit energy glow — INFERRED)
    for x, z in [(4, 4), (7, 4), (4, 7), (7, 7)]:
        # Place glowstone below water level (y=-1 doesn't render, so use y=1 at edges)
        s.set_block(x, 1, z, GLOWSTONE)

    # Lanterns around cave (spiritual atmosphere)
    s.set_block(1, 1, 1, LANTERN)
    s.set_block(10, 1, 10, LANTERN)
    s.set_block(1, 1, 10, LANTERN)
    s.set_block(10, 1, 1, LANTERN)

    # Gravel path from entrance to pool
    s.set_block(5, 0, 9, GRAVEL)
    s.set_block(6, 0, 9, GRAVEL)
    s.set_block(5, 0, 8, GRAVEL)
    s.set_block(6, 0, 8, GRAVEL)
    s.set_block(5, 0, 7, GRAVEL)
    s.set_block(6, 0, 7, GRAVEL)

    # Chest in the cave (where the Heaven Defying Bead might be found — INFERRED)
    s.set_block(9, 1, 9, CHEST)

    # Entrance steps going down into cave
    s.set_block(5, 0, 12, COBBLESTONE)
    s.set_block(6, 0, 12, COBBLESTONE)

    return s


def build_disciple_dormitory():
    """
    Disciple Dormitories (弟子住处) — where Heng Yue outer disciples sleep.
    Canon: Simple living quarters. Bunk beds, chests, minimal furnishings.
    """
    s = StructureBuilder("disciple_dormitory")

    # Floor (10x1x12)
    s.floor_rect(0, 0, 0, 9, 0, 11, SPRUCE_PLANKS)

    # Walls (3 high)
    s.walls_rect(0, 1, 0, 9, 3, 11, STONE)

    # Door openings (south wall, two doors)
    for dy in range(1, 4):
        s.remove_block(3, dy, 11)
        s.remove_block(7, dy, 11)

    # Roof
    s.floor_rect(0, 4, 0, 9, 4, 11, DARK_OAK_PLANKS)

    # Bunk beds: each bed = dark_oak_planks frame + chest for storage
    # Room 1 (west half)
    for x in [1, 3, 5]:
        s.set_block(x, 1, 1, SPRUCE_PLANKS)  # Bed frame
        s.set_block(x, 1, 2, CHEST)  # Personal storage
    # Room 2 (east half)
    for x in [5, 7, 9]:
        s.set_block(x, 1, 9, SPRUCE_PLANKS)
        s.set_block(x, 1, 10, CHEST)

    # Central divider (fence posts)
    for z in range(1, 11):
        s.set_block(4, 1, z, SPRUCE_FENCE)
        s.set_block(5, 1, z, SPRUCE_FENCE)

    # Lanterns
    s.set_block(2, 3, 5, LANTERN)
    s.set_block(7, 3, 6, LANTERN)

    # Entrance steps
    for x in [3, 7]:
        s.set_block(x, 0, 12, STONE_SLAB)

    return s


def build_trial_grounds():
    """
    Trial Grounds (试炼场) — where Heng Yue disciples test their
    cultivation progress.
    Canon: An arena-like area where disciples compete and are evaluated.
    """
    s = StructureBuilder("trial_grounds")

    # Arena floor (18x1x18)
    s.floor_rect(0, 0, 0, 17, 0, 17, STONE)

    # Inner arena (smooth stone)
    s.floor_rect(2, 0, 2, 15, 0, 15, SMOOTH_STONE)

    # Raised spectator platform (north side)
    s.floor_rect(0, 1, 0, 17, 1, 2, STONE)
    s.set_block(0, 1, 2, STONE_SLAB)
    s.set_block(17, 1, 2, STONE_SLAB)

    # Steps up to spectator area
    for x in range(3, 16):
        s.set_block(x, 0, 1, STONE_STAIRS)

    # Arena pillars (8 pillars around the perimeter)
    for px, pz in [(2, 2), (15, 2), (2, 15), (15, 15),
                   (2, 8), (15, 8), (8, 2), (8, 15)]:
        s.column(px, 1, pz, 3, STONE)
        s.set_block(px, 4, pz, STONE_CHISELED)  # Cap

    # Iron bar fence around arena
    for x in range(3, 16):
        s.set_block(x, 1, 3, IRON_BARS)
        s.set_block(x, 1, 15, IRON_BARS)
    for z in range(3, 16):
        s.set_block(3, 1, z, IRON_BARS)
        s.set_block(15, 1, z, IRON_BARS)

    # Lanterns on each pillar
    for px, pz in [(2, 2), (15, 2), (2, 15), (15, 15),
                   (2, 8), (15, 8), (8, 2), (8, 15)]:
        s.set_block(px, 4, pz, LANTERN)

    # Judgment seat (north center, for elders to observe)
    s.set_block(8, 2, 0, DARK_OAK_PLANKS)
    s.set_block(9, 2, 0, DARK_OAK_PLANKS)
    s.set_block(8, 3, 0, DARK_OAK_PLANKS)
    s.set_block(9, 3, 0, DARK_OAK_PLANKS)

    # Center arena marker (glowstone)
    s.set_block(9, 0, 9, GLOWSTONE)

    return s


def build_ancestor_hall():
    """
    Ancestor Hall (祖师堂) — memorial hall for Heng Yue's founders.
    Canon: A solemn hall where the sect's ancestors are honored.
    Features memorial tablets (bookshelves as display), incense
    burners (lanterns), and a central altar.
    """
    s = StructureBuilder("ancestor_hall")

    # Floor (10x1x10)
    s.floor_rect(0, 0, 0, 9, 0, 9, SMOOTH_STONE)

    # Walls (4 high, more imposing)
    s.walls_rect(0, 1, 0, 9, 4, 9, STONE)

    # Door (south, double width)
    for dy in range(1, 5):
        s.remove_block(4, dy, 9)
        s.remove_block(5, dy, 9)

    # Roof
    s.floor_rect(0, 5, 0, 9, 5, 9, DARK_OAK_PLANKS)
    # Roof ridge
    for z in range(0, 10):
        s.set_block(4, 6, z, DARK_OAK_LOG)
        s.set_block(5, 6, z, DARK_OAK_LOG)

    # Memorial tablets along north wall (bookshelves as display niches)
    for x in range(1, 9):
        s.set_block(x, 1, 0, BOOKSHELF)
        s.set_block(x, 2, 0, BOOKSHELF)

    # Central altar (chiseled stone platform)
    s.floor_rect(4, 0, 4, 5, 0, 5, STONE_CHISELED)
    s.set_block(4, 1, 4, STONE_CHISELED)
    s.set_block(5, 1, 4, STONE_CHISELED)
    s.set_block(4, 1, 5, STONE_CHISELED)
    s.set_block(5, 1, 5, STONE_CHISELED)

    # Incense burners (lanterns flanking altar)
    s.set_block(3, 1, 4, LANTERN)
    s.set_block(6, 1, 4, LANTERN)
    s.set_block(3, 1, 5, LANTERN)
    s.set_block(6, 1, 5, LANTERN)

    # Side memorial displays (east and west walls)
    for z in range(1, 9):
        s.set_block(0, 1, z, BOOKSHELF)
        s.set_block(9, 1, z, BOOKSHELF)

    # Entrance steps
    s.set_block(4, 0, 10, STONE_SLAB)
    s.set_block(5, 0, 10, STONE_SLAB)

    # Pillars at entrance
    s.column(3, 1, 9, 4, STONE)
    s.column(6, 1, 9, 4, STONE)

    return s


# ═══════════════════════════════════════════════════════════
#  Main
# ═══════════════════════════════════════════════════════════

STRUCTURE_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/structure/heng_yue_sect"

BUILDERS = {
    "sword_peak": build_sword_peak,
    "alchemy_courtyard": build_alchemy_courtyard,
    "library": build_library,
    "mountain_cave": build_mountain_cave,
    "disciple_dormitory": build_disciple_dormitory,
    "trial_grounds": build_trial_grounds,
    "ancestor_hall": build_ancestor_hall,
}

# Map: template pool directory name → NBT filename
POOL_TO_NBT = {
    "heng_yue_sect_sword_peak": "sword_peak",
    "heng_yue_sect_alchemy_courtyard": "alchemy_courtyard",
    "heng_yue_sect_library": "library",
    "heng_yue_sect_mountain_cave": "mountain_cave",
    "heng_yue_sect_disciple_dormitories": "disciple_dormitory",
    "heng_yue_sect_trial_grounds": "trial_grounds",
    "heng_yue_sect_ancestor_hall": "ancestor_hall",
}

TEMPLATE_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/template_pool"


def main():
    os.makedirs(STRUCTURE_DIR, exist_ok=True)

    # Generate all NBT structure files
    for name, builder_fn in BUILDERS.items():
        print(f"Building {name}...")
        builder = builder_fn()
        nbt_data = builder.render()
        out_path = os.path.join(STRUCTURE_DIR, f"{name}.nbt")
        with open(out_path, 'wb') as f:
            f.write(nbt_data)
        size_kb = len(nbt_data) / 1024
        blocks = len(builder.blocks)
        palette = len(builder.palette)
        print(f"  → {out_path} ({size_kb:.1f} KB, {blocks} blocks, {palette} palette entries)")

    # Fix template pools to reference the new NBT files
    for pool_name, nbt_name in POOL_TO_NBT.items():
        pool_path = os.path.join(TEMPLATE_DIR, pool_name, "start_pool.json")
        if not os.path.exists(pool_path):
            os.makedirs(os.path.dirname(pool_path), exist_ok=True)

        template = {
            "_comment": f"Heng Yue Sect — {nbt_name.replace('_', ' ').title()}. "
                        f"Custom structure generated for canon fidelity (Article XXII). "
                        f"Replaces vanilla Minecraft placeholder.",
            "name": f"ergenverse:{pool_name}/start_pool",
            "fallback": "minecraft:village/plains/terminators",
            "elements": [
                {
                    "weight": 1,
                    "element": {
                        "element_type": "minecraft:single_pool_element",
                        "projection": "rigid",
                        "location": f"ergenverse:heng_yue_sect/{nbt_name}",
                        "processors": "minecraft:empty"
                    }
                }
            ]
        }
        import json
        with open(pool_path, 'w') as f:
            json.dump(template, f, indent=2)
        print(f"  → Updated template pool: {pool_name}/start_pool.json")

    print(f"\nDone. Generated {len(BUILDERS)} structures + updated {len(POOL_TO_NBT)} template pools.")


if __name__ == "__main__":
    main()