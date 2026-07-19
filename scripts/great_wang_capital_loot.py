#!/usr/bin/env python3
"""
AUTO-CANON-023: Add loot chests to 6 Great Wang Capital structures lacking them.
Binary NBT surgery + canon-faithful loot tables.

Canon context (RI Ch.1-10):
- Great Wang Capital is the political center of Zhao Country.
- Larger and wealthier than Teng Family City (a subordinate clan seat).
- The capital houses the country's administration, royal guards, major temple,
  trade port, and cultivator quarter for the country's elite.
- Teng Huayuan operates here politically but his seat is Teng Family City.
- The capital represents the peak of mortal/cultivator civilization in Zhao Country.
- INFERRED: The governor mansion holds the country treasury and administrative records.
- INFERRED: The temple is larger and more important than Teng city's temple.
- INFERRED: The cultivator quarter serves the capital's elite guards and visiting cultivators.

Dimensions (same as Teng city NBTs from same generator):
  city_gate: 17x10x9, cultivator_quarter: 21x9x21, governor_mansion: 25x12x19,
  port_docks: 25x6x15, tavern_district: 17x7x13, temple_district: 21x10x21
"""
import os, json, struct

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "great_wang_capital")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

# ── LE NBT helpers (from patch_structures_with_loot.py / teng_city_loot.py) ──

def le_str(name, val):
    d = struct.pack('<b', 8)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    venc = val.encode('utf-8')
    d += struct.pack('<H', len(venc)) + venc
    return d

def le_int(name, val):
    d = struct.pack('<b', 3)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', val)
    return d

def le_int_arr(name, vals):
    d = struct.pack('<b', 11)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', len(vals))
    for v in vals:
        d += struct.pack('<i', v)
    return d

def le_long(name, val):
    d = struct.pack('<b', 4)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<q', val)
    return d

def le_compound(name=None):
    d = struct.pack('<b', 10)
    if name is not None:
        enc = name.encode('utf-8')
        d += struct.pack('<H', len(enc)) + enc
    return d

def le_end():
    return struct.pack('<b', 0)

def le_palette_entry(block_id):
    d = le_compound()
    if '[' in block_id:
        name, props_str = block_id.split('[', 1)
        props_str = props_str.rstrip(']')
        d += le_str("Name", name)
        d += le_compound("Properties")
        for prop in props_str.split(','):
            if '=' in prop:
                k, v = prop.split('=', 1)
                d += le_str(k, v)
        d += le_end()
    else:
        d += le_str("Name", block_id)
    d += le_end()
    return d

def le_block_entry(x, y, z, state_idx, nbt_bytes=None):
    d = le_compound()
    d += le_int_arr("pos", [x, y, z])
    d += le_int("state", state_idx)
    if nbt_bytes:
        d += le_compound("nbt")
        d += nbt_bytes
        d += le_end()
    d += le_end()
    return d

def le_chest_nbt(loot_path):
    d = le_str("id", "minecraft:chest")
    d += le_str("LootTable", loot_path)
    d += le_long("LootTableSeed", 0)
    return d

def find_list_field(data, keyword):
    kw = keyword.encode('utf-8')
    pos = 0
    while True:
        idx = data.find(kw, pos)
        if idx < 0:
            return None
        tag_pos = idx - 3
        if tag_pos < 0 or data[tag_pos] != 0x09:
            pos = idx + 1
            continue
        name_len = struct.unpack_from('<H', data, tag_pos + 1)[0]
        if name_len != len(kw):
            pos = idx + 1
            continue
        elem_type = data[idx + len(kw)]
        count = struct.unpack_from('<i', data, idx + len(kw) + 1)[0]
        data_start = idx + len(kw) + 5
        return (tag_pos, idx, elem_type, count, data_start)

def find_blocks_end(data, blocks_data_start):
    ent_kw = b'entities'
    ent_idx = data.find(ent_kw, blocks_data_start)
    if ent_idx > 0:
        tag_pos = ent_idx - 3
        if tag_pos >= 0 and data[tag_pos] == 0x09:
            name_len = struct.unpack_from('<H', data, tag_pos + 1)[0]
            if name_len == len(ent_kw):
                return tag_pos
    return len(data) - 1

def patch_structure(nbt_path, chest_specs):
    with open(nbt_path, 'rb') as f:
        data = bytearray(f.read())

    palette_info = find_list_field(data, "palette")
    blocks_info = find_list_field(data, "blocks")
    if not blocks_info or not palette_info:
        print(f"  ERROR: missing palette/blocks in {os.path.basename(nbt_path)}")
        return 0

    blocks_tag_start, blocks_kw_pos, blocks_elem_type, blocks_count, blocks_data_start = blocks_info
    palette_tag_start, palette_kw_pos, palette_elem_type, palette_count, palette_data_start = palette_info
    blocks_end = find_blocks_end(data, blocks_data_start)

    new_palette_entries = []
    state_map = {}
    for _, _, _, facing, _ in chest_specs:
        key = facing
        if key not in state_map:
            idx = palette_count + len(new_palette_entries)
            state_map[key] = idx
            new_palette_entries.append(
                le_palette_entry(f"minecraft:chest[facing={facing},type=single,waterlogged=false]"))

    new_block_entries = []
    for cx, cy, cz, facing, loot_path in chest_specs:
        nbt_data = le_chest_nbt(loot_path)
        new_block_entries.append(le_block_entry(cx, cy, cz, state_map[facing], nbt_data))

    partA = bytes(data[:blocks_tag_start])
    partB = b''.join(new_palette_entries)
    partC_header = bytearray(data[blocks_tag_start:blocks_data_start])
    struct.pack_into('<i', partC_header, blocks_data_start - blocks_tag_start - 4,
                     blocks_count + len(chest_specs))
    partC = bytes(partC_header)
    partD = bytes(data[blocks_data_start:blocks_end])
    partE = b''.join(new_block_entries)
    partF = bytes(data[blocks_end:])

    patched = bytearray(partA + partB + partC + partD + partE + partF)

    palette_count_offset = palette_kw_pos + len(b"palette") + 1
    struct.pack_into('<i', patched, palette_count_offset,
                     palette_count + len(new_palette_entries))

    with open(nbt_path, 'wb') as f:
        f.write(patched)

    print(f"  {os.path.basename(nbt_path)}: +{len(chest_specs)} chests")
    return len(chest_specs)


# ── Loot table builders ──

def lt(pools):
    return {"type": "minecraft:chest", "pools": pools}

def ep(entries, rolls):
    return {"rolls": rolls, "entries": entries}

def ei(item, weight, count=None):
    entry = {"type": "minecraft:item", "weight": weight, "name": item, "functions": []}
    if count:
        entry["functions"].append({
            "function": "minecraft:set_count",
            "count": {"min": count[0], "max": count[1]}
        })
    return entry

def ee(weight):
    return {"type": "minecraft:empty", "weight": weight}

# ── Canon-faithful loot tables ──
# Great Wang Capital is the Zhao Country CAPITAL — wealthier than Teng Family City.
# The capital has the country treasury, royal guards, major temple, elite cultivator quarter.

LOOT_TABLES = {
    # Governor mansion (25x12x19): The country governor's palace.
    # Country treasury — richer than Teng clan's private treasury.
    # Gold, diamonds, emeralds, enchanted books, iron/diamond armor, golden apples.
    "great_wang_capital_governor_mansion": lt([
        ep([
            ei("minecraft:gold_ingot", 5, (3, 10)),          # country treasury wealth
            ei("minecraft:emerald", 5, (3, 8)),               # trade capital
            ei("minecraft:diamond", 3, (1, 4)),               # royal reserves
            ei("minecraft:iron_ingot", 6, (3, 8)),
            ei("minecraft:golden_apple", 3),                   # royal provisions
            ei("minecraft:diamond_sword", 1),                  # rare royal weapon
            ei("minecraft:enchanted_book", 2),                 # cultivation manuals
            ei("minecraft:iron_chestplate", 2),
            ee(2),
        ], {"min": 4, "max": 7}),
        ep([
            ei("minecraft:paper", 5, (3, 8)),                 # country edicts, records
            ei("minecraft:book", 5, (2, 4)),                   # law codices, ledgers
            ei("minecraft:ink_sac", 4, (2, 4)),
            ei("minecraft:gold_nugget", 5, (5, 12)),
            ei("minecraft:arrow", 4, (8, 16)),
            ei("minecraft:shield", 2),
            ei("minecraft:iron_helmet", 2),
            ei("minecraft:map", 2),                             # territory maps (INFERRED)
            ee(2),
        ], {"min": 3, "max": 5}),
    ]),

    # Cultivator quarter (21x9x21): Elite cultivator training for the capital.
    # Better resources than Teng city's cultivator quarter.
    "great_wang_capital_cultivator_quarter": lt([
        ep([
            ei("minecraft:iron_ingot", 6, (3, 8)),            # forging
            ei("minecraft:coal", 7, (4, 12)),                  # forge fuel
            ei("minecraft:paper", 5, (3, 6)),                  # talisman paper (INFERRED)
            ei("minecraft:ink_sac", 4, (1, 3)),
            ei("minecraft:arrow", 5, (8, 16)),
            ei("minecraft:bow", 3),
            ei("minecraft:iron_sword", 3),
            ei("minecraft:iron_chestplate", 2),                 # guard equipment
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:glowstone_dust", 3, (1, 4)),       # qi stones (INFERRED)
            ei("minecraft:redstone", 4, (3, 6)),                # formation materials (INFERRED)
            ei("minecraft:book", 4, (1, 3)),                   # technique scrolls (INFERRED)
            ei("minecraft:experience_bottle", 2),               # cultivation aid (INFERRED)
            ei("minecraft:emerald", 3, (1, 4)),                # cultivator stipend
            ei("minecraft:flint", 3, (2, 6)),
            ee(3),
        ], {"min": 2, "max": 4}),
    ]),

    # Temple district (21x10x21): Major spiritual temple — the country's primary temple.
    # Richer offerings than Teng city temple. More spiritual items.
    "great_wang_capital_temple_district": lt([
        ep([
            ei("minecraft:gold_nugget", 6, (4, 10)),         # abundant temple offerings
            ei("minecraft:gold_ingot", 5, (1, 4)),
            ei("minecraft:soul_lantern", 4),                    # spirit tablets
            ei("minecraft:torch", 5, (3, 8)),                  # memorial candles (INFERRED)
            ei("minecraft:paper", 5, (3, 6)),                  # prayer papers (INFERRED)
            ei("minecraft:book", 5, (2, 4)),                    # scriptures
            ei("minecraft:emerald", 3, (1, 3)),                 # noble donations
            ee(2),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:glowstone_dust", 3, (1, 3)),       # spiritual light (INFERRED)
            ei("minecraft:ink_sac", 3, (1, 3)),
            ei("minecraft:iron_ingot", 3, (1, 4)),
            ei("minecraft:ender_pearl", 1),                     # rare spiritual item (INFERRED)
            ee(6),
        ], {"min": 1, "max": 3}),
    ]),

    # Tavern district (17x7x13): Capital tavern — more diverse than Teng city.
    # Merchants, officials, traveling cultivators. Better food, more information.
    "great_wang_capital_tavern_district": lt([
        ep([
            ei("minecraft:cooked_beef", 6, (2, 5)),           # capital tavern fare
            ei("minecraft:cooked_porkchop", 5, (2, 4)),
            ei("minecraft:bread", 6, (3, 8)),
            ei("minecraft:glass_bottle", 5, (3, 6)),           # drinks (INFERRED)
            ei("minecraft:emerald", 5, (2, 5)),
            ei("minecraft:coal", 4, (2, 6)),
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:paper", 4, (2, 5)),                  # messages/rumors (INFERRED)
            ei("minecraft:book", 3, (1, 2)),                    # traveling literature
            ei("minecraft:flint_and_steel", 1),
            ei("minecraft:bowl", 3, (1, 3)),
            ei("minecraft:wooden_sword", 1),                    # brawlers (INFERRED)
            ee(5),
        ], {"min": 2, "max": 3}),
    ]),

    # City gate (17x10x9): Capital city gate — royal guards, better equipment.
    "great_wang_capital_city_gate": lt([
        ep([
            ei("minecraft:iron_sword", 5),                      # guard weapons
            ei("minecraft:iron_axe", 3),
            ei("minecraft:arrow", 7, (8, 16)),
            ei("minecraft:bow", 4),
            ei("minecraft:iron_ingot", 5, (2, 5)),
            ei("minecraft:chainmail_chestplate", 3),             # guard armor
            ei("minecraft:iron_chestplate", 2),                  # officer armor
            ei("minecraft:bread", 5, (3, 6)),
            ei("minecraft:coal", 4, (2, 6)),
            ee(2),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:shield", 3),
            ei("minecraft:flint", 4, (2, 5)),
            ei("minecraft:stick", 4, (4, 10)),
            ei("minecraft:leather", 3, (1, 3)),
            ee(5),
        ], {"min": 1, "max": 2}),
    ]),

    # Port docks (25x6x15): Capital trade port — major import/export hub.
    # More diverse goods than Teng city docks.
    "great_wang_capital_port_docks": lt([
        ep([
            ei("minecraft:emerald", 6, (3, 8)),                # trade wealth
            ei("minecraft:iron_ingot", 5, (3, 6)),
            ei("minecraft:coal", 5, (4, 10)),
            ei("minecraft:string", 5, (3, 8)),
            ei("minecraft:barrel", 4, (1, 3)),
            ei("minecraft:wheat", 5, (4, 12)),                  # grain imports
            ei("minecraft:bucket", 2),
            ei("minecraft:gold_nugget", 3, (2, 5)),             # luxury imports
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:fishing_rod", 2),
            ei("minecraft:oak_planks", 4, (4, 10)),
            ei("minecraft:cobblestone", 3, (4, 8)),
            ei("minecraft:stone", 3, (4, 10)),
            ei("minecraft:lead", 1),                              # dock beasts (INFERRED)
            ee(5),
        ], {"min": 2, "max": 3}),
    ]),
}

# ── Chest placement specs: (x, y, z, facing, loot_table_path) ──
# Dimensions: city_gate 17x10x9, cultivator_quarter 21x9x21,
#   governor_mansion 25x12x19, port_docks 25x6x15, tavern 17x7x13, temple 21x10x21

CHEST_SPECS = {
    # Governor mansion (25x12x19): country treasury, admin offices, upper level
    "governor_mansion": [
        (12, 1, 9, "south", "ergenverse:chests/great_wang_capital_governor_mansion"),   # main hall
        (5, 1, 5, "east", "ergenverse:chests/great_wang_capital_governor_mansion"),      # west wing (records)
        (19, 1, 14, "west", "ergenverse:chests/great_wang_capital_governor_mansion"),    # east wing (records)
        (12, 5, 9, "south", "ergenverse:chests/great_wang_capital_governor_mansion"),    # upper treasury
    ],
    # Cultivator quarter (21x9x21): training hall, supply room, meditation
    "cultivator_quarter": [
        (10, 1, 10, "south", "ergenverse:chests/great_wang_capital_cultivator_quarter"), # central training
        (5, 1, 5, "east", "ergenverse:chests/great_wang_capital_cultivator_quarter"),    # supply room
        (16, 1, 16, "west", "ergenverse:chests/great_wang_capital_cultivator_quarter"),  # far training
        (10, 1, 3, "north", "ergenverse:chests/great_wang_capital_cultivator_quarter"),  # front area
    ],
    # Temple district (21x10x21): main altar, side chambers
    "temple_district": [
        (10, 1, 10, "south", "ergenverse:chests/great_wang_capital_temple_district"),    # main altar
        (5, 1, 5, "east", "ergenverse:chests/great_wang_capital_temple_district"),       # east shrine
        (16, 1, 16, "west", "ergenverse:chests/great_wang_capital_temple_district"),     # west shrine
    ],
    # Tavern (17x7x13): bar, back room, private room
    "tavern_district": [
        (8, 1, 6, "south", "ergenverse:chests/great_wang_capital_tavern_district"),      # bar area
        (4, 1, 3, "east", "ergenverse:chests/great_wang_capital_tavern_district"),       # back room
        (12, 1, 10, "west", "ergenverse:chests/great_wang_capital_tavern_district"),     # private room
    ],
    # City gate (17x10x9): guard room, armory, gatehouse
    "city_gate": [
        (8, 1, 4, "south", "ergenverse:chests/great_wang_capital_city_gate"),            # guard room
        (4, 1, 2, "east", "ergenverse:chests/great_wang_capital_city_gate"),             # armory
        (13, 1, 7, "west", "ergenverse:chests/great_wang_capital_city_gate"),            # gatehouse
    ],
    # Port docks (25x6x15): dock office, warehouse, far storage
    "port_docks": [
        (12, 1, 7, "south", "ergenverse:chests/great_wang_capital_port_docks"),          # dock office
        (5, 1, 4, "east", "ergenverse:chests/great_wang_capital_port_docks"),           # warehouse
        (20, 1, 12, "west", "ergenverse:chests/great_wang_capital_port_docks"),         # far storage
    ],
}


if __name__ == "__main__":
    print("=== AUTO-CANON-023: Great Wang Capital Loot Chests ===\n")

    print("--- Step 1: Create loot tables ---")
    os.makedirs(LOOT_DIR, exist_ok=True)
    lt_created = 0
    for name, table in LOOT_TABLES.items():
        path = os.path.join(LOOT_DIR, f"{name}.json")
        with open(path, 'w') as f:
            json.dump(table, f, indent=2)
            f.write("\n")
        print(f"  CREATE: {name}.json")
        lt_created += 1

    print(f"\n--- Step 2: Inject chests into NBT structures ---")
    total_chests = 0
    for struct_name, specs in CHEST_SPECS.items():
        nbt_path = os.path.join(NBT_DIR, f"{struct_name}.nbt")
        if not os.path.exists(nbt_path):
            print(f"  SKIP: {struct_name}.nbt not found")
            continue
        count = patch_structure(nbt_path, specs)
        total_chests += count

    print(f"\n=== DONE: {lt_created} loot tables, {total_chests} chests injected ===")