#!/usr/bin/env python3
"""
AUTO-CANON-017: Add loot chests to Wang Family Village structures.
Binary NBT surgery to inject chest blocks + create canon-faithful mortal loot tables.

Canon context (RI Ch.1-8):
- Wang Family Village is a MORTAL village. No cultivation items in most chests.
- Governor mansion has the best loot (iron, gold, emeralds).
- Childhood home has humble family savings.
- Ancestral hall has incense offerings.
- Warehouse stores bulk goods.
- Market has trade goods.
- Residential has daily necessities.
"""
import os, json, struct, sys

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "wang_family_village")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

# ── LE NBT helpers (from patch_structures_with_loot.py) ──

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
    """Inject chests into NBT. chest_specs: list of (x, y, z, facing, loot_table_path)"""
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

    # Collect unique (facing, loot_table) combos for palette
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

    # Assemble
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

    # Update palette count
    palette_count_offset = palette_kw_pos + len(b"palette") + 1
    struct.pack_into('<i', patched, palette_count_offset,
                     palette_count + len(new_palette_entries))

    with open(nbt_path, 'wb') as f:
        f.write(patched)

    print(f"  {os.path.basename(nbt_path)}: +{len(chest_specs)} chests "
          f"(palette {palette_count}->{palette_count + len(new_palette_entries)}, "
          f"blocks {blocks_count}->{blocks_count + len(chest_specs)})")
    return len(chest_specs)


# ── Loot table builders ──

def lt(pools):
    return {"type": "minecraft:chest", "pools": pools}

def ep(entries, rolls):
    return {"rolls": rolls, "entries": entries}

def ei(item, weight, count=None):
    """Item entry. Use full 'minecraft:' namespace for vanilla items."""
    entry = {"type": "minecraft:item", "weight": weight, "name": item, "functions": []}
    if count:
        entry["functions"].append({
            "function": "minecraft:set_count",
            "count": {"min": count[0], "max": count[1]}
        })
    return entry

def ee(weight):
    """Empty entry."""
    return {"type": "minecraft:empty", "weight": weight}


LOOT_TABLES = {
    "wang_family_village_childhood_home": lt([
        ep([
            ei("minecraft:bread", 8, (2, 5)),
            ei("minecraft:wheat", 6, (3, 8)),
            ei("minecraft:carrot", 5, (1, 4)),
            ei("minecraft:wooden_axe", 4),
            ei("minecraft:oak_planks", 5, (2, 6)),  # carpentry materials
            ei("minecraft:stick", 7, (4, 12)),       # carpentry materials
            ei("minecraft:string", 3, (1, 3)),        # sewing/mending
            ei("minecraft:leather", 2, (1, 2)),       # simple goods
            ei("minecraft:written_book", 1),           # family notes (INFERRED)
            ee(5),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:iron_ingot", 3, (1, 2)),    # family savings (small)
            ei("minecraft:copper_ingot", 4, (1, 3)),
            ei("minecraft:emerald", 1),                # rare trade gem
            ei("minecraft:flint", 5, (1, 3)),
            ei("minecraft:coal", 6, (2, 5)),
            ee(6),
        ], {"min": 1, "max": 2}),
    ]),

    "wang_family_village_ancestral_hall": lt([
        ep([
            ei("minecraft:stick", 8, (1, 4)),         # incense sticks (INFERRED)
            ei("minecraft:torch", 5, (1, 3)),         # memorial candles (INFERRED)
            ei("minecraft:soul_lantern", 2),           # spirit tablet lanterns
            ei("minecraft:paper", 4, (1, 3)),         # prayer papers (INFERRED)
            ei("minecraft:gold_nugget", 3, (1, 3)),    # modest offerings
            ee(5),
        ], {"min": 2, "max": 4}),
        ep([
            ei("ergenverse:spirit_stone_fragment", 1, (1, 2)),  # rare: offering from Wang Lin's return
            ei("minecraft:iron_ingot", 2, (1, 2)),
            ee(8),
        ], {"min": 0, "max": 1}),
    ]),

    "wang_family_village_governor_mansion": lt([
        ep([
            ei("minecraft:iron_ingot", 6, (2, 6)),
            ei("minecraft:gold_ingot", 3, (1, 3)),
            ei("minecraft:emerald", 4, (1, 4)),       # trade wealth
            ei("minecraft:iron_sword", 3),
            ei("minecraft:iron_chestplate", 2),
            ei("minecraft:iron_helmet", 2),
            ei("minecraft:shield", 2),
            ei("minecraft:golden_apple", 1),           # governor's luxury
            ee(3),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:paper", 5, (1, 4)),         # tax records (INFERRED)
            ei("minecraft:book", 4, (1, 2)),           # ledgers
            ei("minecraft:ink_sac", 3, (1, 2)),
            ei("minecraft:gold_nugget", 4, (2, 6)),
            ei("minecraft:arrow", 3, (4, 12)),
            ei("minecraft:bow", 2),
            ee(3),
        ], {"min": 2, "max": 4}),
    ]),

    "wang_family_village_residential": lt([
        ep([
            ei("minecraft:bread", 7, (2, 6)),
            ei("minecraft:wheat", 6, (3, 8)),
            ei("minecraft:carrot", 5, (2, 5)),
            ei("minecraft:potato", 5, (2, 5)),
            ei("minecraft:wooden_pickaxe", 3),
            ei("minecraft:wooden_axe", 3),
            ei("minecraft:wooden_hoe", 3),
            ei("minecraft:bowl", 4, (1, 3)),
            ei("minecraft:leather", 2, (1, 2)),
            ee(5),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:coal", 5, (2, 6)),
            ei("minecraft:stick", 6, (3, 8)),
            ei("minecraft:flint", 4, (1, 3)),
            ei("minecraft:cobblestone", 3, (2, 5)),
            ee(6),
        ], {"min": 1, "max": 3}),
    ]),

    "wang_family_village_market": lt([
        ep([
            ei("minecraft:emerald", 6, (1, 4)),       # trade currency
            ei("minecraft:bread", 5, (2, 5)),
            ei("minecraft:iron_ingot", 4, (1, 3)),
            ei("minecraft:coal", 5, (2, 6)),
            ei("minecraft:string", 4, (1, 4)),        # rope/goods
            ei("minecraft:leather", 3, (1, 3)),
            ei("minecraft:glass_bottle", 2, (1, 3)),  # medicine bottles (INFERRED)
            ei("minecraft:bowl", 3, (1, 3)),
            ei("minecraft:wooden_sword", 2),           # cheap weapon for sale
            ee(4),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:wheat", 5, (4, 10)),        # grain trade
            ei("minecraft:paper", 3, (1, 3)),
            ei("minecraft:arrow", 3, (4, 8)),
            ei("minecraft:flint", 3, (2, 4)),
            ee(6),
        ], {"min": 1, "max": 3}),
    ]),

    "wang_family_village_warehouse": lt([
        ep([
            ei("minecraft:wheat", 8, (8, 16)),         # bulk grain storage
            ei("minecraft:barrel", 5, (1, 3)),         # empty storage barrels
            ei("minecraft:coal", 6, (4, 10)),
            ei("minecraft:iron_ingot", 3, (2, 5)),
            ei("minecraft:stone", 4, (4, 10)),        # building materials
            ei("minecraft:oak_planks", 4, (4, 10)),
            ei("minecraft:cobblestone", 5, (4, 10)),
            ei("minecraft:chest", 1),                   # empty chest for storage
            ee(3),
        ], {"min": 4, "max": 6}),
        ep([
            ei("minecraft:stick", 7, (8, 16)),         # bulk building supplies
            ei("minecraft:string", 5, (3, 8)),
            ei("minecraft:lead", 2),                   # for beasts (INFERRED)
            ei("minecraft:bucket", 2),
            ee(5),
        ], {"min": 2, "max": 3}),
    ]),

    "wang_family_village_tavern": lt([
        ep([
            ei("minecraft:bread", 7, (3, 8)),
            ei("minecraft:cooked_beef", 4, (1, 3)),   # tavern food (INFERRED)
            ei("minecraft:cooked_porkchop", 4, (1, 3)),
            ei("minecraft:glass_bottle", 5, (1, 4)),  # drinks (INFERRED)
            ei("minecraft:bowl", 3, (1, 3)),
            ei("minecraft:wooden_sword", 2),           # drunk's dropped weapon (INFERRED)
            ei("minecraft:emerald", 3, (1, 3)),
            ee(4),
        ], {"min": 3, "max": 5}),
        ep([
            ei("minecraft:coal", 5, (2, 6)),           # tavern hearth fuel
            ei("minecraft:stick", 5, (2, 6)),
            ei("minecraft:flint_and_steel", 1),        # hearth lighting
            ei("minecraft:iron_nugget", 4, (1, 3)),
            ee(6),
        ], {"min": 1, "max": 2}),
    ]),
}

# ── Chest placement specs: (x, y, z, facing, loot_table_path) ──
# Coordinates must be within NBT bounds. Facing: north/south/east/west.

CHEST_SPECS = {
    # Childhood home (7x4x6): crafting table at (5,1,4), bed at (1,1,1-2)
    # Hidden compartment: floor level at (4,0,3) was dirt — replace with chest
    "wang_lin_childhood_home": [
        (4, 0, 3, "north", "ergenverse:chests/wang_family_village_childhood_home"),  # hidden compartment
        (3, 1, 5, "east", "ergenverse:chests/wang_family_village_childhood_home"),    # wall shelf
    ],
    # Ancestral hall (9x6x7): altar at (3-5,1,2-4), soul lantern at (4,2,3)
    "ancestral_hall": [
        (4, 1, 3, "south", "ergenverse:chests/wang_family_village_ancestral_hall"),   # on altar
        (7, 1, 3, "west", "ergenverse:chests/wang_family_village_ancestral_hall"),    # side offering
    ],
    # Governor mansion (15x8x13)
    "governor_mansion": [
        (7, 1, 6, "south", "ergenverse:chests/wang_family_village_governor_mansion"),  # center
        (3, 1, 3, "east", "ergenverse:chests/wang_family_village_governor_mansion"),   # side room
        (11, 1, 10, "west", "ergenverse:chests/wang_family_village_governor_mansion"), # far room
    ],
    # Residential district (15x7x15)
    "residential_district": [
        (3, 1, 3, "south", "ergenverse:chests/wang_family_village_residential"),
        (7, 1, 7, "north", "ergenverse:chests/wang_family_village_residential"),
        (11, 1, 11, "west", "ergenverse:chests/wang_family_village_residential"),
        (7, 1, 3, "east", "ergenverse:chests/wang_family_village_residential"),
    ],
    # Market district (15x6x15)
    "market_district": [
        (3, 1, 3, "south", "ergenverse:chests/wang_family_village_market"),
        (7, 1, 7, "east", "ergenverse:chests/wang_family_village_market"),
        (11, 1, 11, "north", "ergenverse:chests/wang_family_village_market"),
        (7, 1, 11, "west", "ergenverse:chests/wang_family_village_market"),
    ],
    # Warehouse district (15x8x11)
    "warehouse_district": [
        (3, 1, 3, "south", "ergenverse:chests/wang_family_village_warehouse"),
        (7, 1, 5, "east", "ergenverse:chests/wang_family_village_warehouse"),
        (11, 1, 3, "west", "ergenverse:chests/wang_family_village_warehouse"),
        (7, 1, 9, "north", "ergenverse:chests/wang_family_village_warehouse"),
    ],
    # Tavern district (15x8x11)
    "tavern_district": [
        (7, 1, 5, "south", "ergenverse:chests/wang_family_village_tavern"),
        (4, 1, 3, "east", "ergenverse:chests/wang_family_village_tavern"),
        (10, 1, 8, "west", "ergenverse:chests/wang_family_village_tavern"),
    ],
}


if __name__ == "__main__":
    print("=== AUTO-CANON-017: Village Loot Chests ===\n")

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