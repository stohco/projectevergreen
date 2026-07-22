#!/usr/bin/env python3
"""AUTO-CANON-026: Add 1-2 new loot chests to 4 Wang Family Village NBTs that have
empty chests. Existing empty chests remain (realistic — not every chest has loot).
Reuses the proven patch_structure function from village_loot.py for ADDING blocks."""
import os, json, struct

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "wang_family_village")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

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
    """Add new chest blocks with loot. Reuses proven village_loot.py pattern."""
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

def ei(i, w, c=(1,1)):
    return {"type":"minecraft:item","name":i,"weight":w,"functions":[{"function":"minecraft:set_count","count":c}]}
def ee(w): return {"type":"minecraft:empty","weight":w}
def ep(e, r): return {"rolls":r,"entries":e}
def lt(p): return {"pools":p}

TABLES = {
    "wang_family_village_main": lt([ep([ei("minecraft:bread",15,(2,6)),ei("minecraft:wheat",10,(4,12)),ei("minecraft:iron_ingot",5,(1,2)),ei("minecraft:coal",8,(2,6)),ei("minecraft:emerald",3,(1,2)),ei("minecraft:oak_planks",10,(4,8)),ee(15)],{"rolls":3,"min_rolls":2,"max_rolls":4})]),
    "wang_family_village_mortal_quarter": lt([ep([ei("minecraft:bread",20,(1,3)),ei("minecraft:wheat",15,(3,8)),ei("minecraft:carrot",10,(2,5)),ei("minecraft:coal",8,(1,4)),ei("minecraft:string",5,(1,3)),ei("minecraft:leather",3,(1,2)),ee(20)],{"rolls":3,"min_rolls":1,"max_rolls":4})]),
    "wang_family_village_cultivator_quarter": lt([ep([ei("minecraft:iron_ingot",10,(1,3)),ei("minecraft:coal",12,(4,8)),ei("minecraft:paper",8,(1,3)),ei("minecraft:ink_sac",5,(1,2)),ei("minecraft:book",5),ei("minecraft:emerald",4,(1,2)),ei("minecraft:bread",10,(2,4)),ei("minecraft:glowstone_dust",3,(1,2)),ee(10)],{"rolls":3,"min_rolls":2,"max_rolls":4})]),
    "wang_family_village_smuggler_tunnels": lt([ep([ei("minecraft:emerald",15,(2,6)),ei("minecraft:iron_ingot",10,(2,5)),ei("minecraft:gold_nugget",8,(1,4)),ei("minecraft:string",8,(2,6)),ei("minecraft:glass_bottle",5,(1,3)),ei("minecraft:coal",5,(3,8)),ei("minecraft:arrow",5,(4,12)),ei("minecraft:iron_sword",2),ee(8)],{"rolls":3,"min_rolls":2,"max_rolls":5})]),
}
FILES = {"wang_family_village_main":"main.nbt","wang_family_village_mortal_quarter":"mortal_quarter.nbt","wang_family_village_cultivator_quarter":"cultivator_quarter.nbt","wang_family_village_smuggler_tunnels":"smuggler_tunnels.nbt"}
# Chest positions: chosen to be different from existing empty chests (y=1-3, varied x/z)
SPECS = {
    "main.nbt": [(4,1,5,"north"),(8,2,3,"south"),(3,1,2,"east")],
    "mortal_quarter.nbt": [(5,1,4,"west"),(7,1,6,"east")],
    "cultivator_quarter.nbt": [(6,1,4,"north"),(10,2,5,"south")],
    "smuggler_tunnels.nbt": [(3,1,3,"east"),(7,1,7,"west")],
}

def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    total = 0
    for lt_name, nbt_name in FILES.items():
        np_ = os.path.join(NBT_DIR, nbt_name)
        lp_ = os.path.join(LOOT_DIR, lt_name + ".json")
        with open(lp_, 'w') as f: json.dump(TABLES[lt_name], f, indent=2); f.write("\n")
        ref = f"ergenverse:chests/{lt_name}"
        specs = [(x,y,z,f,ref) for x,y,z,f in SPECS.get(nbt_name, [])]
        n = patch_structure(np_, specs)
        total += n
        print(f"  {nbt_name}: +{n} loot chests ({ref})")
    print(f"\nTotal: {total} new loot chests, {len(FILES)} loot tables")
    for lt_name, nbt_name in FILES.items():
        path = os.path.join(NBT_DIR, nbt_name)
        with open(path, 'rb') as f: d = f.read()
        refs = d.count(b'LootTable')
        print(f"  {nbt_name}: {refs} LootTable refs total")

if __name__ == "__main__":
    main()