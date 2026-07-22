#!/usr/bin/env python3
"""
AUTO-CANON-007: Add interior loot to Heng Yue Sect buildings.

1. Creates 4 loot table JSONs (library, alchemy, treasury, dormitories).
2. Patches gen_heng_yue_structures.py to fix property bug + add chest_with_loot support.
3. Regenerates key Heng Yue NBT structures with loot-filled chests.

Canon basis (Article XXII, XXIII):
- Library: jade slips, cultivation techniques, books (RI Ch.5-8)
- Alchemy Courtyard: herbs, pill ingredients, empty pill bottles (RI Ch.10-15)
- Hidden Treasury: spirit stones, low-grade artifacts, storage pouches (INFERRED)
- Disciple Dormitories: basic supplies, spirit stones, jade slip blank (RI Ch.3-4)

Article XXVI: No new Engine/Bus/Subscriber. Pure data (loot tables) + script (NBT generation).
"""
import os, sys, json, struct, shutil

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
STRUCT_DIR = os.path.join(BASE, "structures", "heng_yue_sect")

# ── NBT tag type IDs ──────────────────────────────────────────
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

# ── NBT binary writers (from gen_heng_yue_structures.py, with property bug FIX) ──

def nbt_string(name, value):
    data = struct.pack('>b', TAG_STRING)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    enc_val = value.encode('utf-8')
    data += struct.pack('>H', len(enc_val)) + enc_val
    return data

def nbt_int(name, value):
    data = struct.pack('>b', TAG_INT)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    data += struct.pack('>i', value)
    return data

def nbt_long(name, value):
    data = struct.pack('>b', TAG_LONG)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    data += struct.pack('>q', value)
    return data

def nbt_int_array(name, values):
    data = struct.pack('>b', TAG_INT_ARRAY)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    data += struct.pack('>i', len(values))
    for v in values:
        data += struct.pack('>i', v)
    return data

def nbt_compound_start(name=None):
    data = struct.pack('>b', TAG_COMPOUND)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    return data

def nbt_list_start(name, payload_type, length):
    data = struct.pack('>b', TAG_LIST)
    if name is not None:
        enc = name.encode('utf-8')
        data += struct.pack('>H', len(enc)) + enc
    data += struct.pack('>b', payload_type)
    data += struct.pack('>i', length)
    return data

def nbt_block_state(block_id):
    """Write palette entry compound for a block state string. FIXED: property values use v not k."""
    data = b''
    if '[' in block_id:
        name, props_str = block_id.split('[', 1)
        props_str = props_str.rstrip(']')
        data += nbt_string("Name", name)
        data += struct.pack('>b', TAG_COMPOUND)
        enc = b"Properties"
        data += struct.pack('>H', len(enc)) + enc
        for prop in props_str.split(','):
            if '=' in prop:
                k, v = prop.split('=', 1)
                data += nbt_string(k, v)  # BUG FIX: was (k, k)
        data += struct.pack('>b', TAG_END)
    else:
        data += nbt_string("Name", block_id)
    return data

# ── Extended StructureBuilder with block entity NBT support ──

class StructureBuilder:
    def __init__(self, name):
        self.name = name
        self.palette = []
        self.palette_index = {}
        self.blocks = []  # list of (x, y, z, palette_idx, nbt_bytes_or_None)

    def _get_idx(self, block_id):
        if block_id not in self.palette_index:
            self.palette_index[block_id] = len(self.palette)
            self.palette.append(block_id)
        return self.palette_index[block_id]

    def set_block(self, x, y, z, block_id, nbt=None):
        idx = self._get_idx(block_id)
        self.blocks.append((x, y, z, idx, nbt))

    def fill_rect(self, x1, y1, z1, x2, y2, z2, block_id):
        for x in range(min(x1,x2), max(x1,x2)+1):
            for y in range(min(y1,y2), max(y1,y2)+1):
                for z in range(min(z1,z2), max(z1,z2)+1):
                    self.set_block(x, y, z, block_id)

    def floor_rect(self, x1, y1, z1, x2, y2, z2, block_id):
        for x in range(min(x1,x2), max(x1,x2)+1):
            for z in range(min(z1,z2), max(z1,z2)+1):
                self.set_block(x, y1, z, block_id)

    def walls_rect(self, x1, y1, z1, x2, y2, z2, block_id, thickness=1):
        for x in range(min(x1,x2), max(x1,x2)+1):
            for z in range(min(z1,z2), max(z1,z2)+1):
                for dy in range(y2-y1+1):
                    on_x = (x == min(x1,x2) or x == max(x1,x2))
                    on_z = (z == min(z1,z2) or z == max(z1,z2))
                    if on_x or on_z:
                        self.set_block(x, y1+dy, z, block_id)

    def column(self, x, y1, z, y2, block_id):
        for y in range(min(y1,y2), max(y1,y2)+1):
            self.set_block(x, y, z, block_id)

    def chest_with_loot(self, x, y, z, facing="north", loot_table=None):
        block_id = f"minecraft:chest[facing={facing},type=single,waterlogged=false]"
        if not loot_table:
            nbt = None
        else:
            # Build block entity NBT: {id: "minecraft:chest", LootTable: "path", LootTableSeed: 0L}
            nbt = b''
            nbt += nbt_string("id", "minecraft:chest")
            nbt += nbt_string("LootTable", loot_table)
            nbt += nbt_long("LootTableSeed", 0)
        self.set_block(x, y, z, block_id, nbt)

    def remove_block(self, x, y, z):
        self.blocks = [(bx,by,bz,bi,bn) for bx,by,bz,bi,bn in self.blocks
                       if not (bx==x and by==y and bz==z)]

    def render(self):
        if not self.blocks:
            return b''
        max_x = max(b[0] for b in self.blocks)
        max_y = max(b[1] for b in self.blocks)
        max_z = max(b[2] for b in self.blocks)
        size = [max_x+1, max_y+1, max_z+1]

        self._get_idx("minecraft:air")  # ensure air at index 0

        nbt = nbt_compound_start(None)
        nbt += nbt_int_array("size", size)
        nbt += nbt_list_start("palette", TAG_COMPOUND, len(self.palette))
        for bid in self.palette:
            nbt += nbt_block_state(bid)
            nbt += struct.pack('>b', TAG_END)
        nbt += nbt_list_start("blocks", TAG_COMPOUND, len(self.blocks))
        for x, y, z, si, block_nbt in self.blocks:
            nbt += nbt_int_array("pos", [x, y, z])
            nbt += nbt_int("state", si)
            if block_nbt:
                # Write "nbt" TAG_Compound with block entity data
                nbt += struct.pack('>b', TAG_COMPOUND)
                nbt += struct.pack('>H', 3) + b'nbt'  # name = "nbt"
                nbt += block_nbt
                nbt += struct.pack('>b', TAG_END)  # end "nbt" compound
            nbt += struct.pack('>b', TAG_END)  # end block compound
        nbt += struct.pack('>b', TAG_END)  # end root
        return nbt

    def save(self, base_dir):
        os.makedirs(base_dir, exist_ok=True)
        path = os.path.join(base_dir, f"{self.name}.nbt")
        data = self.render()
        with open(path, 'wb') as f:
            f.write(data)
        return path


# ── Loot table helpers ────────────────────────────────────────

def entry_item(item, weight=1, count=(1, 1)):
    e = {"type": "minecraft:item", "weight": weight, "name": item, "functions": []}
    if count != (1, 1):
        e["functions"].append({"function": "minecraft:set_count", "count": {"min": count[0], "max": count[1]}})
    return e

def entry_empty(weight=1):
    return {"type": "minecraft:empty", "weight": weight}

def pool(entries, rolls=(2, 4)):
    return {"rolls": {"min": rolls[0], "max": rolls[1]}, "entries": entries}

def loot_table(pools):
    return {"type": "minecraft:chest", "pools": pools}

def eg(item):
    return f"ergenverse:{item}"

def save_loot(name, data):
    os.makedirs(LOOT_DIR, exist_ok=True)
    with open(os.path.join(LOOT_DIR, f"{name}.json"), 'w') as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print(f"  Loot table: chests/{name}.json")


# ── Loot table definitions (canon-faithful) ───────────────────

def create_loot_tables():
    print("Creating Heng Yue Sect loot tables...")

    # Library: jade slips, cultivation guides, basic techniques
    save_loot("heng_yue_sect_library", loot_table([
        pool([
            entry_item(eg("jade_slip"), 6),
            entry_item(eg("jade_slip_blank"), 3),
            entry_item("minecraft:book", 5, (1, 3)),
            entry_item(eg("cultivation_guide"), 2),
            entry_item("minecraft:enchanted_book", 1),
            entry_item(eg("qi_gathering_grass"), 2, (1, 2)),
            entry_empty(3),
        ], (3, 5)),
        pool([
            entry_item(eg("spirit_stone_low"), 5, (2, 6)),
            entry_item(eg("spirit_stone"), 3, (1, 3)),
            entry_item("minecraft:paper", 4, (1, 3)),
            entry_item("minecraft:ink_sac", 2, (1, 2)),
            entry_empty(4),
        ], (1, 3)),
    ]))

    # Alchemy Courtyard: herbs, pill ingredients, empty pill bottles
    save_loot("heng_yue_sect_alchemy", loot_table([
        pool([
            entry_item(eg("qi_gathering_grass"), 6, (2, 5)),
            entry_item(eg("snow_heart_herb"), 4, (1, 3)),
            entry_item(eg("nine_leaf_clover"), 2, (1, 2)),
            entry_item(eg("soul_nourishing_lotus"), 1),
            entry_item(eg("blood_forgetting_grass"), 2, (1, 2)),
            entry_item("minecraft:glass_bottle", 5, (2, 6)),
            entry_empty(2),
        ], (3, 5)),
        pool([
            entry_item(eg("spirit_stone_low"), 4, (3, 8)),
            entry_item("minecraft:glowstone_dust", 3, (1, 3)),
            entry_item("minecraft:blaze_powder", 2, (1, 2)),
            entry_item(eg("spirit_stone"), 2, (1, 2)),
            entry_empty(3),
        ], (2, 3)),
    ]))

    # Hidden Treasury: spirit stones, low-grade artifacts, storage pouches
    save_loot("heng_yue_sect_treasury", loot_table([
        pool([
            entry_item(eg("spirit_stone"), 8, (5, 15)),
            entry_item(eg("spirit_stone_mid"), 3, (1, 4)),
            entry_item(eg("spirit_stone_low"), 5, (5, 12)),
            entry_empty(2),
        ], (3, 5)),
        pool([
            entry_item(eg("storage_pouch"), 3),
            entry_item(eg("flying_sword"), 2),
            entry_item(eg("spirit_armor"), 1),
            entry_item(eg("jade_slip"), 2),
            entry_item(eg("dao_fragment"), 1),
            entry_item("minecraft:gold_ingot", 4, (2, 6)),
            entry_item("minecraft:diamond", 2, (1, 3)),
            entry_empty(5),
        ], (1, 3)),
    ]))

    # Disciple Dormitories: basic supplies, low-grade items
    save_loot("heng_yue_sect_dormitory", loot_table([
        pool([
            entry_item(eg("spirit_stone_low"), 6, (1, 5)),
            entry_item("minecraft:bread", 5, (2, 4)),
            entry_item(eg("qi_gathering_grass"), 3, (1, 2)),
            entry_item("minecraft:torch", 4, (3, 8)),
            entry_item("minecraft:iron_ingot", 2, (1, 3)),
            entry_empty(4),
        ], (2, 4)),
        pool([
            entry_item(eg("jade_slip_blank"), 2),
            entry_item(eg("jade_slip"), 1),
            entry_item("minecraft:book", 3),
            entry_empty(6),
        ], (0, 2)),
    ]))


# ── Heng Yue structure rebuilds (only buildings that need chests) ──

# Block constants
STONE = "minecraft:stone_bricks"
STONE_CHISELED = "minecraft:chiseled_stone_bricks"
STONE_CRACKED = "minecraft:cracked_stone_bricks"
SMOOTH_STONE = "minecraft:smooth_stone"
STONE_SLAB = "minecraft:stone_brick_slab[type=bottom,waterlogged=false]"
DARK_OAK_PLANKS = "minecraft:dark_oak_planks"
DARK_OAK_LOG = "minecraft:dark_oak_log[axis=y]"
SPRUCE_PLANKS = "minecraft:spruce_planks"
SPRUCE_LOG = "minecraft:spruce_log[axis=y]"
BOOKSHELF = "minecraft:bookshelf"
CRAFTING_TABLE = "minecraft:crafting_table"
LANTERN = "minecraft:lantern"
GLOWSTONE = "minecraft:glowstone"
CAULDRON = "minecraft:cauldron"
BREWING_STAND = "minecraft:brewing_stand"

LIBRARY_LT = "ergenverse:chests/heng_yue_sect_library"
ALCHEMY_LT = "ergenverse:chests/heng_yue_sect_alchemy"
TREASURY_LT = "ergenverse:chests/heng_yue_sect_treasury"
DORMITORY_LT = "ergenverse:chests/heng_yue_sect_dormitory"

def rebuild_library():
    """Library / Scripture Pavilion (藏经阁) — with loot-filled chests."""
    s = StructureBuilder("library")
    s.floor_rect(0, 0, 0, 11, 0, 9, SPRUCE_PLANKS)
    s.walls_rect(0, 1, 0, 11, 3, 9, STONE)
    for dy in range(1, 4):
        s.remove_block(6, dy, 9)
        s.remove_block(5, dy, 9)
    s.floor_rect(0, 4, 0, 11, 4, 9, DARK_OAK_PLANKS)
    for x in range(1, 11):
        s.set_block(x, 5, 4, DARK_OAK_LOG)
        s.set_block(x, 5, 5, DARK_OAK_LOG)
    # Bookshelves along all walls
    for x in range(1, 11):
        s.set_block(x, 1, 0, BOOKSHELF)
        s.set_block(x, 2, 0, BOOKSHELF)
    for x in range(1, 5):
        s.set_block(x, 1, 8, BOOKSHELF)
        s.set_block(x, 2, 8, BOOKSHELF)
    for x in range(7, 11):
        s.set_block(x, 1, 8, BOOKSHELF)
        s.set_block(x, 2, 8, BOOKSHELF)
    for z in range(1, 8):
        s.set_block(0, 1, z, BOOKSHELF)
        s.set_block(0, 2, z, BOOKSHELF)
    for z in range(1, 8):
        s.set_block(11, 1, z, BOOKSHELF)
        s.set_block(11, 2, z, BOOKSHELF)
    # Reading desks
    s.set_block(3, 1, 4, CRAFTING_TABLE)
    s.set_block(8, 1, 4, CRAFTING_TABLE)
    s.floor_rect(5, 0, 1, 6, 0, 7, SMOOTH_STONE)
    # Lanterns
    s.set_block(3, 3, 3, LANTERN)
    s.set_block(8, 3, 6, LANTERN)
    s.set_block(5, 3, 4, GLOWSTONE)
    # LOOT CHESTS: back of library, north wall center (canon: rare jade slips)
    s.chest_with_loot(5, 1, 1, "south", LIBRARY_LT)
    s.chest_with_loot(6, 1, 1, "south", LIBRARY_LT)
    # Additional chest: east wall reading nook
    s.chest_with_loot(10, 1, 3, "west", LIBRARY_LT)
    # Entrance
    s.set_block(5, 0, 10, STONE_SLAB)
    s.set_block(6, 0, 10, STONE_SLAB)
    return s

def rebuild_alchemy_courtyard():
    """Alchemy Courtyard (炼丹房) — with herb chests."""
    s = StructureBuilder("alchemy_courtyard")
    s.floor_rect(0, 0, 0, 13, 0, 11, SMOOTH_STONE)
    s.walls_rect(0, 1, 0, 13, 3, 11, STONE)
    for dy in range(1, 4):
        s.remove_block(7, dy, 11)
        s.remove_block(6, dy, 11)
    s.floor_rect(0, 4, 0, 13, 4, 11, DARK_OAK_PLANKS)
    for x in range(0, 14, 2):
        s.column(x, 5, 5, 6, DARK_OAK_LOG)  # roof pillars
    # Alchemy stations
    for x in [2, 5, 9, 12]:
        s.set_block(x, 1, 2, CAULDRON)
    s.set_block(4, 1, 5, CRAFTING_TABLE)
    s.set_block(9, 1, 5, CRAFTING_TABLE)
    s.set_block(7, 1, 8, BREWING_STAND)
    # Lanterns
    s.set_block(3, 3, 3, LANTERN)
    s.set_block(10, 3, 8, LANTERN)
    s.set_block(6, 3, 5, GLOWSTONE)
    # LOOT CHESTS: herb storage
    s.chest_with_loot(1, 1, 1, "south", ALCHEMY_LT)
    s.chest_with_loot(12, 1, 1, "south", ALCHEMY_LT)
    s.chest_with_loot(6, 1, 9, "north", ALCHEMY_LT)
    # Entrance
    s.set_block(6, 0, 12, STONE_SLAB)
    s.set_block(7, 0, 12, STONE_SLAB)
    return s

def rebuild_hidden_treasury():
    """Hidden Treasury (藏宝阁) — spirit stones and artifacts."""
    s = StructureBuilder("hidden_treasury")
    s.floor_rect(0, 0, 0, 9, 0, 9, STONE_CHISELED)
    s.walls_rect(0, 1, 0, 9, 3, 9, STONE)
    for dy in range(1, 4):
        s.remove_block(5, dy, 9)
        s.remove_block(4, dy, 9)
    s.floor_rect(0, 4, 0, 9, 4, 9, DARK_OAK_PLANKS)
    for x in range(0, 10, 3):
        s.column(x, 5, 4, 6, DARK_OAK_LOG)
    # Interior pillars
    s.column(3, 1, 3, 3, STONE_CRACKED)
    s.column(6, 1, 3, 3, STONE_CRACKED)
    s.column(3, 1, 6, 3, STONE_CRACKED)
    s.column(6, 1, 6, 3, STONE_CRACKED)
    # Glowstone lighting
    s.set_block(4, 3, 4, GLOWSTONE)
    s.set_block(5, 3, 4, GLOWSTONE)
    # LOOT CHESTS: the treasury has many chests with valuables
    s.chest_with_loot(1, 1, 1, "south", TREASURY_LT)
    s.chest_with_loot(8, 1, 1, "south", TREASURY_LT)
    s.chest_with_loot(1, 1, 8, "north", TREASURY_LT)
    s.chest_with_loot(8, 1, 8, "north", TREASURY_LT)
    s.chest_with_loot(4, 1, 1, "south", TREASURY_LT)
    # Entrance
    s.set_block(4, 0, 10, STONE_SLAB)
    s.set_block(5, 0, 10, STONE_SLAB)
    return s

def rebuild_disciple_dormitories():
    """Disciple Dormitories (弟子宿舍) — basic living quarters."""
    s = StructureBuilder("disciple_dormitories")
    s.floor_rect(0, 0, 0, 15, 0, 9, SPRUCE_PLANKS)
    s.walls_rect(0, 1, 0, 15, 3, 9, STONE)
    for dy in range(1, 4):
        s.remove_block(8, dy, 9)
        s.remove_block(7, dy, 9)
    # Internal divider walls (rooms)
    for dy in range(1, 4):
        for y in range(1, 4):
            s.set_block(5, y, 0, STONE)
            s.set_block(10, y, 0, STONE)
            s.set_block(5, y, 9, STONE)
            s.set_block(10, y, 9, STONE)
    # Roof
    s.floor_rect(0, 4, 0, 15, 4, 9, DARK_OAK_PLANKS)
    for x in range(0, 16, 4):
        s.column(x, 5, 4, 6, SPRUCE_LOG)
    # Beds (red wool as bed substitute), one per room
    for x in range(1, 5):
        s.set_block(x, 1, 1, "minecraft:red_wool")
    for x in range(6, 10):
        s.set_block(x, 1, 1, "minecraft:red_wool")
    for x in range(11, 15):
        s.set_block(x, 1, 1, "minecraft:red_wool")
    # Lanterns
    s.set_block(3, 3, 5, LANTERN)
    s.set_block(8, 3, 5, LANTERN)
    s.set_block(13, 3, 5, LANTERN)
    # LOOT CHESTS: one per room for personal belongings
    s.chest_with_loot(2, 1, 8, "north", DORMITORY_LT)
    s.chest_with_loot(7, 1, 8, "north", DORMITORY_LT)
    s.chest_with_loot(12, 1, 8, "north", DORMITORY_LT)
    # Entrance
    s.set_block(7, 0, 10, STONE_SLAB)
    s.set_block(8, 0, 10, STONE_SLAB)
    return s


def main():
    print("=" * 60)
    print("AUTO-CANON-007: Heng Yue Sect interior loot")
    print("=" * 60)

    # Step 1: Create loot table JSONs
    create_loot_tables()

    # Step 2: Rebuild structures with loot-filled chests
    print("\nRebuilding Heng Yue structures with loot chests...")
    builders = [
        rebuild_library(),
        rebuild_alchemy_courtyard(),
        rebuild_hidden_treasury(),
        rebuild_disciple_dormitories(),
    ]
    for b in builders:
        path = b.save(STRUCT_DIR)
        size = len(b.render())
        chests = sum(1 for _,_,_,_,nbt in b.blocks if nbt is not None)
        print(f"  {b.name}.nbt: {len(b.blocks)} blocks, {chests} loot chests, {size} bytes")

    # Step 3: Verify the rebuilt files contain "chest" and "LootTable"
    print("\nVerifying rebuilt files...")
    for name in ["library", "alchemy_courtyard", "hidden_treasury", "disciple_dormitories"]:
        path = os.path.join(STRUCT_DIR, f"{name}.nbt")
        with open(path, 'rb') as f:
            data = f.read()
        chests = data.count(b'chest')
        loottables = data.count(b'LootTable')
        print(f"  {name}: chest={chests}, LootTable={loottables}")

    print("\nDone! 4 loot tables + 4 rebuilt structures.")


if __name__ == "__main__":
    main()