#!/usr/bin/env python3
"""AUTO-CANON-027: Add loot chests to the last 3 Wang Family Village NBTs that
have ZERO chests: memorial_shrine, temple_district, port_docks.
Also adds a loot table to city_gate (small guard post supplies).
Reuses the proven patch_structure function from patch_village_empty_chests.py."""
import os, json, struct

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "wang_family_village")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

# ── NBT binary helpers (proven from patch_village_empty_chests.py) ──
def le_str(n, v):
    return struct.pack('<b',8) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<H',len(ve:=v.encode())) + ve

def le_int(n, v):
    return struct.pack('<b',3) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<i',v)

def le_int_arr(n, vals):
    d = struct.pack('<b',11) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<i',len(vals))
    for v in vals: d += struct.pack('<i',v)
    return d

def le_long(n, v):
    return struct.pack('<b',4) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<q',v)

def le_compound(n=None):
    d = struct.pack('<b',10)
    if n: d += struct.pack('<H',len(e:=n.encode())) + e
    return d

def le_end(): return struct.pack('<b',0)

def le_palette_entry(block_id):
    d = le_compound()
    if '[' in block_id:
        name, props = block_id.split('[', 1)
        props = props.rstrip(']')
        d += le_str("Name", name)
        d += le_compound("Properties")
        for prop in props.split(','):
            if '=' in prop: k, v = prop.split('=', 1); d += le_str(k, v)
        d += le_end()
    else:
        d += le_str("Name", block_id)
    d += le_end()
    return d

def le_block_entry(x, y, z, state_idx, nbt_bytes=None):
    d = le_compound()
    d += le_int_arr("pos", [x, y, z])
    d += le_int("state", state_idx)
    if nbt_bytes: d += le_compound("nbt") + nbt_bytes + le_end()
    d += le_end()
    return d

def le_chest_nbt(loot_path):
    return le_str("id","minecraft:chest") + le_str("LootTable",loot_path) + le_long("LootTableSeed",0)

def find_list_field(data, keyword):
    kw = keyword.encode('utf-8')
    pos = 0
    while True:
        idx = data.find(kw, pos)
        if idx < 0: return None
        tag_pos = idx - 3
        if tag_pos < 0 or data[tag_pos] != 0x09: pos = idx + 1; continue
        name_len = struct.unpack_from('<H', data, tag_pos + 1)[0]
        if name_len != len(kw): pos = idx + 1; continue
        elem_type = data[idx + len(kw)]
        count = struct.unpack_from('<i', data, idx + len(kw) + 1)[0]
        data_start = idx + len(kw) + 5
        return (tag_pos, idx, elem_type, count, data_start)

def find_blocks_end(data, blocks_data_start):
    ent_idx = data.find(b'entities', blocks_data_start)
    if ent_idx > 0:
        tp = ent_idx - 3
        if tp >= 0 and data[tp] == 0x09:
            nl = struct.unpack_from('<H', data, tp + 1)[0]
            if nl == 8: return tp
    return len(data) - 1

def patch_structure(nbt_path, chest_specs):
    with open(nbt_path, 'rb') as f:
        data = bytearray(f.read())
    pi = find_list_field(data, "palette")
    bi = find_list_field(data, "blocks")
    if not bi or not pi: return 0
    bt, _, _, bc, bds = bi
    _, _, _, pc, pds = pi
    be = find_blocks_end(data, bds)
    new_pal = []
    smap = {}
    for _, _, _, facing, _ in chest_specs:
        if facing not in smap:
            smap[facing] = pc + len(new_pal)
            new_pal.append(le_palette_entry(f"minecraft:chest[facing={facing},type=single,waterlogged=false]"))
    new_blks = []
    for cx, cy, cz, facing, lp in chest_specs:
        nb = le_chest_nbt(lp)
        new_blks.append(le_block_entry(cx, cy, cz, smap[facing], nb))
    partA = bytes(data[:bt])
    partB = b''.join(new_pal)
    hdr = bytearray(data[bt:bds])
    struct.pack_into('<i', hdr, bds - bt - 4, bc + len(chest_specs))
    partC = bytes(hdr)
    partD = bytes(data[bds:be])
    partE = b''.join(new_blks)
    partF = bytes(data[be:])
    patched = bytearray(partA + partB + partC + partD + partE + partF)
    pco = pc + len(new_pal)
    struct.pack_into('<i', patched, pi[1] + len(b'palette') + 1, pco)
    with open(nbt_path, 'wb') as f:
        f.write(patched)
    return len(chest_specs)

# ── Loot table helpers ──
def ei(i, w, c=(1,1)):
    return {"type":"minecraft:item","name":i,"weight":w,"functions":[{"function":"minecraft:set_count","count":c}]}
def ee(w): return {"type":"minecraft:empty","weight":w}
def ep(e, r): return {"rolls":r,"entries":e}
def lt(p): return {"pools":p}

# ── Canon-faithful loot tables ──
#
# memorial_shrine: Wang family ancestral memorial shrine.
# Canon: Villagers leave offerings for ancestors. Paper money, incense (coal proxy),
# gold nuggets as symbolic wealth. Very somber, low value — this is devotion, not commerce.
# INFERRED: canon mentions Wang Lin visiting the ancestral hall but not specific shrine items.
#
# temple_district: Village temple to local deities / heavenly spirits.
# Canon: Mortal villages have temples where villagers pray for good harvests and safety.
# Incense (coal), paper for talismans, glowstone dust as qi-lamp oil, gold as offerings.
# A book represents a basic meditation text the temple keeper maintains.
# INFERRED: no specific temple contents described in canon.
#
# port_docks: Village docks on a nearby river.
# Canon: Wang Family Village is in Zhao Country near Heng Yue Mountain. River trade
# would bring basic supplies. Fishing (string, bones), trade goods, rope.
# INFERRED: no docks described in canon, but a village of this size needs trade access.
# Lower value than smuggler tunnels — legitimate trade, not contraband.
#
# city_gate: Guard post at village entrance.
# Canon: Villages have some security. Guards would have basic supplies.
# INFERRED: minimal supplies — this is just a guard post, not an armory.

TABLES = {
    "wang_family_village_memorial_shrine": lt([
        ep([ei("minecraft:paper",15,(2,5)),       # Paper money for ancestors
           ei("minecraft:coal",12,(2,4)),         # Incense
           ei("minecraft:gold_nugget",8,(1,3)),   # Symbolic offerings
           ei("minecraft:wheat",10,(3,6)),        # Food offerings
           ei("minecraft:torch",5,(1,2)),         # Temple candles
           ee(25)], {"rolls":2,"min_rolls":1,"max_rolls":3})
    ]),
    "wang_family_village_temple_district": lt([
        ep([ei("minecraft:coal",15,(3,8)),         # Incense
           ei("minecraft:paper",10,(2,5)),         # Talisman paper / prayers
           ei("minecraft:gold_nugget",8,(1,3)),    # Offerings
           ei("minecraft:glowstone_dust",5,(1,2)), # Qi-lamp oil (INFERRED)
           ei("minecraft:book",5),                  # Basic meditation text
           ei("minecraft:bread",8,(1,3)),           # Temple keeper's food
           ei("minecraft:emerald",3,(1,2)),         # Donations
           ee(15)], {"rolls":3,"min_rolls":2,"max_rolls":4})
    ]),
    "wang_family_village_port_docks": lt([
        ep([ei("minecraft:string",12,(2,6)),       # Rope / fishing line
           ei("minecraft:coal",8,(2,5)),           # Trade fuel / lamp oil
           ei("minecraft:bread",10,(2,5)),         # Sailor provisions
           ei("minecraft:iron_ingot",6,(1,3)),     # Trade goods
           ei("minecraft:oak_planks",10,(4,10)),   # Repair materials
           ei("minecraft:emerald",4,(1,2)),        # Trade currency
           ei("minecraft:bone",8,(2,6)),           # Fish bones (INFERRED)
           ee(15)], {"rolls":3,"min_rolls":2,"max_rolls":4})
    ]),
    "wang_family_village_city_gate": lt([
        ep([ei("minecraft:bread",15,(1,3)),        # Guard rations
           ei("minecraft:coal",10,(2,4)),          # Torch fuel
           ei("minecraft:arrow",10,(4,8)),         # Guard supplies
           ei("minecraft:iron_ingot",5,(1,2)),     # Basic equipment
           ei("minecraft:string",5,(1,3)),         # Signal cord
           ee(25)], {"rolls":2,"min_rolls":1,"max_rolls":3})
    ]),
}

# Chest positions: (x, y, z, facing)
# Positions chosen to be interior, low (y=1-3), varied x/z
SPECS = {
    "memorial_shrine.nbt": [(3,1,4,"north"), (5,1,3,"south")],
    "temple_district.nbt": [(4,1,5,"east"), (8,1,6,"west"), (6,2,3,"north")],
    "port_docks.nbt": [(5,1,4,"south"), (9,1,7,"north"), (3,1,8,"east")],
    "city_gate.nbt": [(2,1,3,"east")],  # Small — just 1 chest in the guard post
}

FILES = {
    "wang_family_village_memorial_shrine": "memorial_shrine.nbt",
    "wang_family_village_temple_district": "temple_district.nbt",
    "wang_family_village_port_docks": "port_docks.nbt",
    "wang_family_village_city_gate": "city_gate.nbt",
}

def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    total = 0
    for lt_name, nbt_name in FILES.items():
        nbt_path = os.path.join(NBT_DIR, nbt_name)
        loot_path = os.path.join(LOOT_DIR, lt_name + ".json")

        # Write loot table
        with open(loot_path, 'w') as f:
            json.dump(TABLES[lt_name], f, indent=2)
            f.write("\n")

        ref = f"ergenverse:chests/{lt_name}"
        specs = [(x, y, z, facing, ref) for x, y, z, facing in SPECS.get(nbt_name, [])]

        n = patch_structure(nbt_path, specs)
        total += n
        print(f"  {nbt_name}: +{n} loot chests ({ref})")

    print(f"\nTotal: {total} new loot chests, {len(FILES)} loot tables")

    # Verify all 15 NBTs now have LootTable refs
    print("\n=== Final verification: all 15 Wang Family Village NBTs ===")
    import glob
    all_nbts = sorted(glob.glob(os.path.join(NBT_DIR, "*.nbt")))
    all_with = 0
    all_without = []
    for p in all_nbts:
        with open(p, 'rb') as f:
            d = f.read()
        refs = d.count(b'LootTable')
        fname = os.path.basename(p)
        if refs > 0:
            all_with += 1
            print(f"  {fname}: {refs} LootTable refs [OK]")
        else:
            all_without.append(fname)
            print(f"  {fname}: 0 LootTable refs [STILL EMPTY]")

    print(f"\nResult: {all_with}/15 NBTs have loot tables")
    if all_without:
        print(f"REMAINING: {all_without}")
    else:
        print("ALL VILLAGE STRUCTURES HAVE LOOT. Village loot vertical slice COMPLETE.")

if __name__ == "__main__":
    main()