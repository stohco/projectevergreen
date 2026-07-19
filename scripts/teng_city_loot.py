#!/usr/bin/env python3
"""AUTO-CANON-030: Teng Family City — 5 missing loot tables + 5 NBT injections.

Teng Family City: the dominant power in Zhao Country's mortal politics.
The Teng family oppressed the Wang family and other villages. Teng Huayuan
is a major antagonist in early RI. The city is wealthy, corrupt, and
politically powerful.

Missing: 5 NBTs have zero loot tables + zero LootTable refs.
Also: teng_family_keep loot table exists but has no NBT to inject into
(keep structure may not have been generated yet — skip).

Canon basis: Teng Family City is the seat of Teng Huayuan's power in
Zhao Country. The family controls trade, taxation, and politics. Their
warehouses overflow with extorted wealth. Smuggler tunnels hide their
worst secrets. The market is the economic heart.

Zero Java changes. Article XXII, XXIII, XXVI.
"""
import json, os, struct, glob

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.join(os.path.dirname(SCRIPT_DIR),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "teng_family_city")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ── NBT binary helpers (from hengyue_inject_chests.py, handles BE+LE) ──
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
        name, props = block_id.split('[', 1); props = props.rstrip(']')
        d += le_str("Name", name); d += le_compound("Properties")
        for prop in props.split(','):
            if '=' in prop: k, v = prop.split('=', 1); d += le_str(k, v)
        d += le_end()
    else:
        d += le_str("Name", block_id)
    d += le_end()
    return d
def le_block_entry(x, y, z, state_idx, nbt_bytes=None):
    d = le_compound(); d += le_int_arr("pos", [x, y, z]); d += le_int("state", state_idx)
    if nbt_bytes: d += le_compound("nbt") + nbt_bytes + le_end()
    d += le_end()
    return d
def le_chest_nbt(loot_path):
    return le_str("id","minecraft:chest") + le_str("LootTable",loot_path) + le_long("LootTableSeed",0)

def find_list_field(data, keyword):
    kw = keyword.encode('utf-8'); klen = len(kw); pos = 0
    while True:
        idx = data.find(kw, pos)
        if idx < 0: return None
        for nls, fmt, off in [(2,'<H',1),(2,'>H',1),(4,'<i',1),(4,'>i',1)]:
            tp = idx - nls - off
            if tp < 0 or data[tp] != 0x09: continue
            nl = struct.unpack_from(fmt, data, tp + off)[0]
            if nl != klen: continue
            co = idx + klen + 1
            if co + 4 > len(data): continue
            c = struct.unpack_from('<i', data, co)[0]
            if c < 0 or c > 100000: c = struct.unpack_from('>i', data, co)[0]
            return (tp, idx, data[idx+klen], c, co + 4)
        pos = idx + 1

def find_blocks_end(data, bds):
    ei = data.find(b'entities', bds)
    if ei > 0:
        tp = ei - 3
        if tp >= 0 and data[tp] == 0x09:
            for fmt in ['<H', '>H']:
                nl = struct.unpack_from(fmt, data, tp+1)[0]
                if nl == 8: return tp
    return len(data) - 1

def detect_endianness(data, kw_idx, klen):
    co = kw_idx + klen + 1
    if co + 4 > len(data): return '<'
    le = struct.unpack_from('<i', data, co)[0]
    return '<' if 0 < le < 100000 else '>'

def patch_structure(nbt_path, chest_specs):
    with open(nbt_path, 'rb') as f: data = bytearray(f.read())
    pi = find_list_field(data, "palette"); bi = find_list_field(data, "blocks")
    if not bi or not pi: print(f"  WARNING: could not parse {os.path.basename(nbt_path)}"); return 0
    bt,_,_,bc,bds = bi; _,_,_,pc,pds = pi; be = find_blocks_end(data, bds)
    endian = detect_endianness(data, bi[1], len(b"blocks")); ifmt = endian + 'i'
    new_pal = []; smap = {}
    for _,_,_,facing,_ in chest_specs:
        if facing not in smap:
            smap[facing] = pc + len(new_pal)
            new_pal.append(le_palette_entry(f"minecraft:chest[facing={facing},type=single,waterlogged=false]"))
    new_blks = []
    for cx,cy,cz,facing,lp in chest_specs:
        new_blks.append(le_block_entry(cx,cy,cz,smap[facing],le_chest_nbt(lp)))
    hdr = bytearray(data[bt:bds])
    struct.pack_into(ifmt, hdr, bds-bt-4, bc+len(chest_specs))
    patched = bytearray(data[:bt]) + b''.join(new_pal) + bytes(hdr) + bytes(data[bds:be]) + b''.join(new_blks) + bytes(data[be:])
    struct.pack_into(ifmt, patched, pi[1]+len(b'palette')+1, pc+len(new_pal))
    with open(nbt_path, 'wb') as f: f.write(patched)
    return len(chest_specs)

# ═══════════════════════════════════════════════════════════════════
# PART 1: 5 LOOT TABLES
# ═══════════════════════════════════════════════════════════════════

LOOT = {
    # Market District — trade hub, wealthy, diverse goods
    "teng_family_city_market_district": {
        "rolls": {"rolls": 3, "min_rolls": 2, "max_rolls": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":15,"functions":[{"function":"minecraft:set_count","count":[2,6]}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":12,"functions":[{"function":"minecraft:set_count","count":[2,5]}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":8,"functions":[{"function":"minecraft:set_count","count":[2,6]}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":8,"functions":[{"function":"minecraft:set_count","count":[1,4]}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":4,"functions":[{"function":"minecraft:set_count","count":[1,1]}]},
            {"type":"minecraft:item","name":"minecraft:oak_planks","weight":6,"functions":[{"function":"minecraft:set_count","count":[4,10]}]},
            {"type":"minecraft:empty","weight":12},
        ]
    },
    # Mortal Quarter — poor residents, Teng oppression
    "teng_family_city_mortal_quarter": {
        "rolls": {"rolls": 2, "min_rolls": 1, "max_rolls": 3},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:bread","weight":18,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:item","name":"minecraft:wheat","weight":15,"functions":[{"function":"minecraft:set_count","count":[2,6]}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":[1,4]}]},
            {"type":"minecraft:item","name":"minecraft:stick","weight":8,"functions":[{"function":"minecraft:set_count","count":[3,8]}]},
            {"type":"minecraft:item","name":"minecraft:string","weight":6,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:empty","weight":20},
        ]
    },
    # Residential District — Teng family members, moderate wealth
    "teng_family_city_residential_district": {
        "rolls": {"rolls": 2, "min_rolls": 2, "max_rolls": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":12,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":[2,5]}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":12,"functions":[{"function":"minecraft:set_count","count":[2,4]}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":6,"functions":[{"function":"minecraft:set_count","count":[1,2]}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":5,"functions":[{"function":"minecraft:set_count","count":[1,1]}]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":8,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:empty","weight":15},
        ]
    },
    # Smuggler Tunnels — HIGH VALUE contraband, Teng family dark secrets
    "teng_family_city_smuggler_tunnels": {
        "rolls": {"rolls": 3, "min_rolls": 2, "max_rolls": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":18,"functions":[{"function":"minecraft:set_count","count":[3,10]}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":12,"functions":[{"function":"minecraft:set_count","count":[2,8]}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":[2,5]}]},
            {"type":"minecraft:item","name":"minecraft:diamond","weight":2,"functions":[{"function":"minecraft:set_count","count":[1,1]}]},
            {"type":"minecraft:item","name":"minecraft:gold_ingot","weight":6,"functions":[{"function":"minecraft:set_count","count":[1,2]}]},
            {"type":"minecraft:item","name":"minecraft:glass_bottle","weight":8,"functions":[{"function":"minecraft:set_count","count":[1,3]}]},
            {"type":"minecraft:item","name":"minecraft:arrow","weight":8,"functions":[{"function":"minecraft:set_count","count":[4,12]}]},
            {"type":"minecraft:item","name":"minecraft:blaze_powder","weight":3,"functions":[{"function":"minecraft:set_count","count":[1,1]}]},
            {"type":"minecraft:empty","weight":10},
        ]
    },
    # Warehouse District — Teng family wealth storage, bulk goods
    "teng_family_city_warehouse_district": {
        "rolls": {"rolls": 4, "min_rolls": 3, "max_rolls": 6},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:wheat","weight":15,"functions":[{"function":"minecraft:set_count","count":[8,20]}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":12,"functions":[{"function":"minecraft:set_count","count":[4,12]}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":[2,6]}]},
            {"type":"minecraft:item","name":"minecraft:oak_planks","weight":10,"functions":[{"function":"minecraft:set_count","count":[8,20]}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":8,"functions":[{"function":"minecraft:set_count","count":[2,5]}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":[4,10]}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":5,"functions":[{"function":"minecraft:set_count","count":[1,4]}]},
            {"type":"minecraft:empty","weight":8},
        ]
    },
}

for name, spec in LOOT.items():
    table = {"pools": [{"rolls": spec["rolls"], "entries": spec["entries"]}]}
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, 'w') as f:
        json.dump(table, f, indent=2); f.write("\n")
    print(f"[LOOT] {name}: {len(spec['entries'])} entries")

# ═══════════════════════════════════════════════════════════════════
# PART 2: INJECT CHESTS INTO 5 NBTs
# ═══════════════════════════════════════════════════════════════════

CHESTS = {
    "market_district.nbt": [
        (4, 1, 5, "north", "ergenverse:chests/teng_family_city_market_district"),
        (8, 2, 3, "east",  "ergenverse:chests/teng_family_city_market_district"),
        (6, 1, 7, "south", "ergenverse:chests/teng_family_city_market_district"),
    ],
    "mortal_quarter.nbt": [
        (5, 1, 4, "south", "ergenverse:chests/teng_family_city_mortal_quarter"),
        (7, 1, 6, "north", "ergenverse:chests/teng_family_city_mortal_quarter"),
    ],
    "residential_district.nbt": [
        (4, 1, 5, "east",  "ergenverse:chests/teng_family_city_residential_district"),
        (8, 2, 3, "west",  "ergenverse:chests/teng_family_city_residential_district"),
        (6, 1, 7, "north", "ergenverse:chests/teng_family_city_residential_district"),
    ],
    "smuggler_tunnels.nbt": [
        (3, 1, 3, "east",  "ergenverse:chests/teng_family_city_smuggler_tunnels"),
        (7, 1, 7, "south", "ergenverse:chests/teng_family_city_smuggler_tunnels"),
        (5, 2, 5, "north", "ergenverse:chests/teng_family_city_smuggler_tunnels"),
    ],
    "warehouse_district.nbt": [
        (4, 1, 5, "south", "ergenverse:chests/teng_family_city_warehouse_district"),
        (8, 1, 8, "east",  "ergenverse:chests/teng_family_city_warehouse_district"),
        (6, 2, 3, "west",  "ergenverse:chests/teng_family_city_warehouse_district"),
        (10, 1, 6, "north", "ergenverse:chests/teng_family_city_warehouse_district"),
    ],
}

print("\n--- Chest injection ---")
total = 0
for nbt_name, specs in CHESTS.items():
    nbt_path = os.path.join(NBT_DIR, nbt_name)
    n = patch_structure(nbt_path, specs)
    total += n
    lt = specs[0][4].split("/")[-1]
    print(f"  {nbt_name}: +{n} chests ({lt})")
print(f"\nTotal: {total} new chests in 5 NBTs")

# ═══════════════════════════════════════════════════════════════════
# VERIFICATION
# ═══════════════════════════════════════════════════════════════════
print("\n=== Verification: all 11 Teng Family City NBTs ===")
all_nbts = sorted(glob.glob(os.path.join(NBT_DIR, "*.nbt")))
ok = 0; total_refs = 0
for p in all_nbts:
    with open(p, 'rb') as f: d = f.read()
    refs = d.count(b'LootTable'); total_refs += refs; fn = os.path.basename(p)
    if refs > 0:
        ok += 1
        print(f"  {fn}: {refs} refs [OK]")
    else:
        print(f"  {fn}: 0 refs [EMPTY]")
print(f"\nResult: {ok}/11 NBTs have loot, {total_refs} total refs")