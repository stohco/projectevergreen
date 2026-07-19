#!/usr/bin/env python3
"""
AUTO-CANON-021: Add loot chests to 6 Teng Family City structures lacking them.
Binary NBT surgery + canon-faithful loot tables.

Canon context (RI Ch.1-10):
- Teng Family City is the Teng clan seat — a wealthy, powerful cultivator city.
- Governor mansion: patriarch's treasury, iron/gold/diamonds/enchanted gear.
- Cultivator quarter: training supplies, talisman materials, qi resources.
- Temple district: spiritual offerings, gold, soul items.
- Tavern: food, drinks, information hub.
- City gate: guard supplies, weapons.
- Port docks: trade goods, import/export.
"""
import os, json, struct

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "teng_family_city")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

# ── LE NBT helpers (from village_loot.py) ──

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

LOOT_TABLES = {
    # Governor mansion (25x12x19): Teng Huayuan's seat. Wealthy, powerful clan.
    # Gold, diamonds, iron armor, enchanted books, emeralds. A clan that ruled Zhao Country.
    "teng_family_city_governor_mansion": lt([
        ep([
            ei("minecraft:gold_ingot", 6, (2, 8)),           # clan wealth
            ei("minecraft:emerald", 5, (2, 6)),              # trade dominance
            ei("minecraft:iron_ingot", 7, (3, 8)),
            ei("minecraft:diamond", 2, (1, 3)),              # rare high-value
            ei("minecraft:iron_chestplate", 3),
            ei("minecraft:iron_leggings", 3),
            ei("minecraft:iron_sword", 3),
            ei("minecraft:golden_apple", 2),
            ei("minecraft:enchanted_book", 1),               # cultivation manuals (INFERRED)
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:paper", 5, (2, 6)),                # clan records, edicts
            ei("minecraft:book", 4, (1, 3)),                  # ledgers, cultivation texts
            ei("minecraft:ink_sac", 4, (1, 3)),
            ei("minecraft:gold_nugget", 5, (4, 10)),
            ei("minecraft:arrow", 4, (8, 16)),
            ei("minecraft:bow", 2),
            ei("minecraft:shield", 2),
            ei("minecraft:iron_helmet", 2),
            ee(3),
        ], {"min": 3, "max": 5}),
    ]),

    # Cultivator quarter (21x9x21): Training grounds for Teng family cultivators.
    # Iron, coal, paper (talismans), glowstone (qi stones INFERRED), arrows.
    "teng_family_city_cultivator_quarter": lt([
        ep([
            ei("minecraft:iron_ingot", 7, (2, 6)),          # forging supplies
            ei("minecraft:coal", 8, (4, 12)),               # forge fuel
            ei("minecraft:paper", 5, (2, 5)),                # talisman paper (INFERRED)
            ei("minecraft:ink_sac", 4, (1, 3)),              # talisman ink (INFERRED)
            ei("minecraft:arrow", 5, (8, 16)),
            ei("minecraft:bow", 3),
            ei("minecraft:iron_sword", 2),
            ei("minecraft:flint", 4, (2, 6)),
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:glowstone_dust", 2, (1, 3)),      # qi stones (INFERRED)
            ei("minecraft:redstone", 3, (2, 5)),             # formation materials (INFERRED)
            ei("minecraft:book", 4, (1, 2)),                  # technique manuals (INFERRED)
            ei("minecraft:experience_bottle", 1),            # cultivation aid (INFERRED)
            ei("minecraft:stick", 6, (4, 12)),
            ei("minecraft:leather", 3, (1, 3)),
            ee(5),
        ], {"min": 2, "max": 3}),
    ]),

    # Temple district (21x10x21): Ancestral worship + spiritual cultivation.
    # Gold offerings, soul lanterns, books, candles, paper prayers.
    "teng_family_city_temple_district": lt([
        ep([
            ei("minecraft:gold_nugget", 7, (3, 8)),         # temple offerings
            ei("minecraft:gold_ingot", 4, (1, 3)),
            ei("minecraft:soul_lantern", 3),                 # spirit tablets
            ei("minecraft:torch", 6, (2, 6)),               # memorial candles (INFERRED)
            ei("minecraft:paper", 5, (2, 5)),                # prayer papers (INFERRED)
            ei("minecraft:book", 4, (1, 3)),                  # scriptures
            ee(3),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:emerald", 2, (1, 3)),              # rare donations
            ei("minecraft:glowstone_dust", 2, (1, 2)),      # spiritual light (INFERRED)
            ei("minecraft:ink_sac", 3, (1, 2)),
            ei("minecraft:iron_ingot", 3, (1, 3)),
            ee(7),
        ], {"min": 1, "max": 2}),
    ]),

    # Tavern district (17x7x13): Information hub, slightly better than village tavern.
    "teng_family_city_tavern_district": lt([
        ep([
            ei("minecraft:cooked_beef", 6, (1, 4)),         # city tavern fare (INFERRED)
            ei("minecraft:cooked_porkchop", 5, (1, 3)),
            ei("minecraft:bread", 7, (3, 8)),
            ei("minecraft:glass_bottle", 5, (2, 5)),        # drinks (INFERRED)
            ei("minecraft:emerald", 4, (1, 4)),
            ei("minecraft:coal", 5, (2, 6)),               # hearth
            ei("minecraft:iron_nugget", 4, (2, 5)),
            ee(4),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:wooden_sword", 2),                  # brawler's weapon (INFERRED)
            ei("minecraft:flint_and_steel", 1),               # hearth
            ei("minecraft:bowl", 4, (1, 3)),
            ei("minecraft:paper", 3, (1, 3)),                # messages/rumors (INFERRED)
            ee(6),
        ], {"min": 1, "max": 2}),
    ]),

    # City gate (17x10x9): Guard post. Military supplies.
    "teng_family_city_city_gate": lt([
        ep([
            ei("minecraft:iron_sword", 5),                   # guard weapons
            ei("minecraft:arrow", 7, (8, 16)),
            ei("minecraft:bow", 3),
            ei("minecraft:iron_ingot", 5, (1, 4)),
            ei("minecraft:leather", 4, (1, 3)),
            ei("minecraft:chainmail_chestplate", 2),         # guard armor
            ei("minecraft:bread", 6, (2, 5)),
            ei("minecraft:coal", 5, (2, 6)),
            ee(3),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:shield", 2),
            ei("minecraft:flint", 4, (2, 5)),
            ei("minecraft:stick", 5, (4, 10)),
            ee(7),
        ], {"min": 1, "max": 2}),
    ]),

    # Port docks (25x6x15): Trade goods, import/export.
    "teng_family_city_port_docks": lt([
        ep([
            ei("minecraft:emerald", 6, (2, 6)),             # trade wealth
            ei("minecraft:iron_ingot", 5, (2, 5)),
            ei("minecraft:coal", 6, (4, 10)),               # ship fuel
            ei("minecraft:string", 5, (3, 8)),               # rope
            ei("minecraft:barrel", 4, (1, 3)),
            ei("minecraft:wheat", 5, (4, 12)),               # grain imports
            ei("minecraft:bucket", 2),
            ee(3),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:lead", 2),                          # dock beasts (INFERRED)
            ei("minecraft:fishing_rod", 2),                   # dock workers (INFERRED)
            ei("minecraft:stone", 4, (4, 10)),
            ei("minecraft:oak_planks", 4, (4, 10)),          # ship repair
            ei("minecraft:cobblestone", 3, (4, 8)),
            ee(5),
        ], {"min": 2, "max": 3}),
    ]),
}

# ── Chest placement specs: (x, y, z, facing, loot_table_path) ──
# NBT dimensions: city_gate 17x10x9, cultivator_quarter 21x9x21,
#   governor_mansion 25x12x19, port_docks 25x6x15, tavern 17x7x13, temple 21x10x21

CHEST_SPECS = {
    # Governor mansion (25x12x19): place in center, side rooms, upper level
    "governor_mansion": [
        (12, 1, 9, "south", "ergenverse:chests/teng_family_city_governor_mansion"),   # main hall
        (5, 1, 5, "east", "ergenverse:chests/teng_family_city_governor_mansion"),     # west wing
        (19, 1, 14, "west", "ergenverse:chests/teng_family_city_governor_mansion"),   # east wing
        (12, 5, 9, "south", "ergenverse:chests/teng_family_city_governor_mansion"),   # upper treasury
    ],
    # Cultivator quarter (21x9x21): training hall, supply room, meditation area
    "cultivator_quarter": [
        (10, 1, 10, "south", "ergenverse:chests/teng_family_city_cultivator_quarter"), # central training
        (5, 1, 5, "east", "ergenverse:chests/teng_family_city_cultivator_quarter"),   # supply room
        (16, 1, 16, "west", "ergenverse:chests/teng_family_city_cultivator_quarter"), # far training
        (10, 1, 3, "north", "ergenverse:chests/teng_family_city_cultivator_quarter"), # front area
    ],
    # Temple district (21x10x21): offering altar, side chambers, meditation
    "temple_district": [
        (10, 1, 10, "south", "ergenverse:chests/teng_family_city_temple_district"),   # main altar
        (5, 1, 5, "east", "ergenverse:chests/teng_family_city_temple_district"),      # east shrine
        (16, 1, 16, "west", "ergenverse:chests/teng_family_city_temple_district"),    # west shrine
    ],
    # Tavern (17x7x13): bar counter, back room, upstairs
    "tavern_district": [
        (8, 1, 6, "south", "ergenverse:chests/teng_family_city_tavern_district"),     # bar area
        (4, 1, 3, "east", "ergenverse:chests/teng_family_city_tavern_district"),      # back room
        (12, 1, 10, "west", "ergenverse:chests/teng_family_city_tavern_district"),    # private room
    ],
    # City gate (17x10x9): guard room, armory, gatehouse
    "city_gate": [
        (8, 1, 4, "south", "ergenverse:chests/teng_family_city_city_gate"),           # guard room
        (4, 1, 2, "east", "ergenverse:chests/teng_family_city_city_gate"),            # armory
        (13, 1, 7, "west", "ergenverse:chests/teng_family_city_city_gate"),           # gatehouse
    ],
    # Port docks (25x6x15): warehouse, dock office, ship storage
    "port_docks": [
        (12, 1, 7, "south", "ergenverse:chests/teng_family_city_port_docks"),          # dock office
        (5, 1, 4, "east", "ergenverse:chests/teng_family_city_port_docks"),           # warehouse
        (20, 1, 12, "west", "ergenverse:chests/teng_family_city_port_docks"),         # far storage
    ],
}


if __name__ == "__main__":
    print("=== AUTO-CANON-021: Teng Family City Loot Chests ===\n")

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