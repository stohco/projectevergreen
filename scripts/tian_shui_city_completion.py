#!/usr/bin/env python3
"""AUTO-CANON-033: Tian Shui City — 11 loot tables + chest injections + 10 INFERRED NPCs.

Tian Shui City is a location in Zhao Country. In RI, Tian Shui (天水) is referenced
as one of the cities in Zhao Country's mortal world. Gao Qiming (N87, canon) is
a mysterious diviner/fortune teller there. The city follows the same 11-district
template as other cities.

Current state: 11 NBTs with 0 loot refs, 2 orphan loot tables (wrong naming),
1 canon NPC (Gao Qiming) with no schedule/tasks. The city is completely dead.

This script creates proper loot tables, injects chests into all 11 NBTs,
and adds 10 INFERRED NPCs covering all districts. Tian Shui is known for
its divination culture (Gao Qiming), so the NPCs reflect a city where fate
and fortune-telling influence daily life.

Zero Java changes. Article XXII, XXIII, XXVI.
"""
import json, os, struct, glob

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.join(os.path.dirname(SCRIPT_DIR),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "tian_shui_city")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ── NBT binary helpers (handles BE+LE) ──
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
# PART 1: 11 LOOT TABLES
# ═══════════════════════════════════════════════════════════════════
# Tian Shui is known for divination — temple has fate-related items.
# Wealth gradient: governor > smuggler > residential > cultivator > market > warehouse > temple > port > city_gate > mortal

LOOT = {
    "tian_shui_city_governor_mansion": {
        "rolls": {"min": 4, "max": 7},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:gold_ingot","weight":5,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":5,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:diamond","weight":2,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:golden_apple","weight":2,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:enchanted_book","weight":3,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:iron_chestplate","weight":2,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:book","weight":4,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:empty","weight":3},
        ]
    },
    "tian_shui_city_smuggler_tunnels": {
        "rolls": {"min": 2, "max": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":16,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":10}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:diamond","weight":2,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":1}}]},
            {"type":"minecraft:item","name":"minecraft:glass_bottle","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:arrow","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":12}}]},
            {"type":"minecraft:item","name":"minecraft:blaze_powder","weight":3,"functions":[]},
            {"type":"minecraft:empty","weight":10},
        ]
    },
    "tian_shui_city_residential_district": {
        "rolls": {"min": 2, "max": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":6,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":14},
        ]
    },
    "tian_shui_city_cultivator_quarter": {
        "rolls": {"min": 2, "max": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:experience_bottle","weight":4,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":12},
        ]
    },
    "tian_shui_city_market_district": {
        "rolls": {"min": 2, "max": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":14,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":5,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":12},
        ]
    },
    "tian_shui_city_warehouse_district": {
        "rolls": {"min": 3, "max": 6},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:wheat","weight":15,"functions":[{"function":"minecraft:set_count","count":{"min":8,"max":20}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":12}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:oak_planks","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":8,"max":16}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":10}}]},
            {"type":"minecraft:empty","weight":10},
        ]
    },
    "tian_shui_city_temple_district": {
        "rolls": {"min": 2, "max": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:ink_sac","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":14},
        ]
    },
    "tian_shui_city_port_docks": {
        "rolls": {"min": 2, "max": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:oak_planks","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":10}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:wheat","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":10}}]},
            {"type":"minecraft:empty","weight":14},
        ]
    },
    "tian_shui_city_city_gate": {
        "rolls": {"min": 2, "max": 3},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:arrow","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":12}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:leather","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:empty","weight":16},
        ]
    },
    "tian_shui_city_mortal_quarter": {
        "rolls": {"min": 1, "max": 3},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:bread","weight":18,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:wheat","weight":14,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:stick","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:string","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":20},
        ]
    },
    "tian_shui_city_tavern_district": {
        "rolls": {"min": 2, "max": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":2}}]},
            {"type":"minecraft:empty","weight":14},
        ]
    },
}

print("=== PART 1: Creating 11 loot tables ===")
for name, spec in LOOT.items():
    table = {"type": "minecraft:chest", "pools": [{"rolls": spec["rolls"], "entries": spec["entries"]}]}
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, 'w') as f:
        json.dump(table, f, indent=2); f.write("\n")
    print(f"  {name}: {len(spec['entries'])} entries")

# ═══════════════════════════════════════════════════════════════════
# PART 2: INJECT CHESTS INTO ALL 11 NBTs
# ═══════════════════════════════════════════════════════════════════

LP = "ergenverse:chests/tian_shui_city"

CHESTS = {
    "city_gate.nbt": [
        (4, 1, 5, "north", f"{LP}/tian_shui_city_city_gate"),
        (8, 2, 3, "east",  f"{LP}/tian_shui_city_city_gate"),
        (6, 1, 7, "south", f"{LP}/tian_shui_city_city_gate"),
    ],
    "governor_mansion.nbt": [
        (5, 2, 5, "north", f"{LP}/tian_shui_city_governor_mansion"),
        (8, 2, 8, "east",  f"{LP}/tian_shui_city_governor_mansion"),
        (3, 2, 3, "west",  f"{LP}/tian_shui_city_governor_mansion"),
    ],
    "market_district.nbt": [
        (4, 1, 5, "north", f"{LP}/tian_shui_city_market_district"),
        (8, 2, 3, "east",  f"{LP}/tian_shui_city_market_district"),
        (6, 1, 7, "south", f"{LP}/tian_shui_city_market_district"),
    ],
    "mortal_quarter.nbt": [
        (5, 1, 4, "south", f"{LP}/tian_shui_city_mortal_quarter"),
        (7, 1, 6, "north", f"{LP}/tian_shui_city_mortal_quarter"),
        (3, 2, 3, "east",  f"{LP}/tian_shui_city_mortal_quarter"),
    ],
    "port_docks.nbt": [
        (4, 1, 5, "south", f"{LP}/tian_shui_city_port_docks"),
        (8, 1, 8, "east",  f"{LP}/tian_shui_city_port_docks"),
        (6, 2, 3, "west",  f"{LP}/tian_shui_city_port_docks"),
    ],
    "residential_district.nbt": [
        (4, 1, 5, "east",  f"{LP}/tian_shui_city_residential_district"),
        (8, 2, 3, "west",  f"{LP}/tian_shui_city_residential_district"),
        (6, 1, 7, "north", f"{LP}/tian_shui_city_residential_district"),
        (10, 1, 5, "south", f"{LP}/tian_shui_city_residential_district"),
    ],
    "cultivator_quarter.nbt": [
        (5, 2, 5, "north", f"{LP}/tian_shui_city_cultivator_quarter"),
        (8, 2, 8, "east",  f"{LP}/tian_shui_city_cultivator_quarter"),
        (3, 2, 3, "west",  f"{LP}/tian_shui_city_cultivator_quarter"),
    ],
    "smuggler_tunnels.nbt": [
        (3, 1, 3, "east",  f"{LP}/tian_shui_city_smuggler_tunnels"),
        (7, 1, 7, "south", f"{LP}/tian_shui_city_smuggler_tunnels"),
        (5, 2, 5, "north", f"{LP}/tian_shui_city_smuggler_tunnels"),
    ],
    "temple_district.nbt": [
        (5, 1, 5, "north", f"{LP}/tian_shui_city_temple_district"),
        (8, 2, 8, "east",  f"{LP}/tian_shui_city_temple_district"),
        (3, 1, 3, "west",  f"{LP}/tian_shui_city_temple_district"),
    ],
    "tavern_district.nbt": [
        (4, 1, 5, "north", f"{LP}/tian_shui_city_tavern_district"),
        (8, 2, 3, "east",  f"{LP}/tian_shui_city_tavern_district"),
        (6, 1, 7, "south", f"{LP}/tian_shui_city_tavern_district"),
    ],
    "warehouse_district.nbt": [
        (4, 1, 5, "south", f"{LP}/tian_shui_city_warehouse_district"),
        (8, 1, 8, "east",  f"{LP}/tian_shui_city_warehouse_district"),
        (6, 2, 3, "west",  f"{LP}/tian_shui_city_warehouse_district"),
        (10, 1, 6, "north", f"{LP}/tian_shui_city_warehouse_district"),
    ],
}

print("\n=== PART 2: Injecting chests into 11 NBTs ===")
total_chests = 0
for nbt_name, specs in CHESTS.items():
    nbt_path = os.path.join(NBT_DIR, nbt_name)
    n = patch_structure(nbt_path, specs)
    total_chests += n
    print(f"  {nbt_name}: +{n} chests")
print(f"  Total: {total_chests} new chests")


# ═══════════════════════════════════════════════════════════════════
# PART 3: 10 INFERRED NPCs
# ═══════════════════════════════════════════════════════════════════

def make_npc(npc_id, name, nameCn, npc_type, faction, cultivation,
             personality, speech, canon_note, initiation_lines,
             daily_schedule, sect_tasks, canon_confidence=1):
    return {
        "_comment": f"NPC: {name} ({nameCn}). INFERRED: {canon_note}. Derivation: I = INFERRED.",
        "npc_id": npc_id, "name": name, "nameCn": nameCn,
        "canon_id": f"I-{npc_id}", "type": npc_type, "faction": faction,
        "location": "tian_shui_city", "cultivation": cultivation,
        "personality": personality, "speech": speech,
        "relationship_to_wanglin": "INFERRED",
        "dialogue_available": True, "quest_available": len(sect_tasks) > 0,
        "trade_available": False, "teaching_available": False,
        "perception_tiers": {"mortal": f"A {personality.split(',')[0]} {npc_type}", "qi_condensation": f"{name}, {faction} {npc_type}"},
        "canon_confidence": canon_confidence, "note": f"INFERRED: {canon_note}",
        "derivation_type": "I", "salt": hash(npc_id) % 2147483647,
        "dao_heart": {"stability": 50, "cracks": [], "last_tested_tick": None, "note": "0-100."},
        "soul_state": "none", "tribulation_debt": 0, "_xianxia_schema": 1,
        "initiation_lines": initiation_lines,
        "daily_schedule": daily_schedule,
        "sect_tasks": sect_tasks
    }

NPCS = [
    make_npc("npc_tianshui_guard_zhao", "Guard Zhao", "守卫赵", "guard", "tian_shui_government",
             "late Qi Condensation", "alert, superstitious, respects omens",
             "formal, cautious",
             "City gate guard. Tian Shui people believe in fate/omens, reflected in guard behavior.",
             ["You approach Tian Shui City. The diviners say today is an auspicious day for travel — or perhaps not. Let me see your face before you enter.",
              "Gao Qiming read my fortune once. Said I would live to see three hundred moons. I count every one.",
              "The governor asks that all armed visitors register at the cultivator quarter. Fate favors the prepared.",
              "A strange omen last night — the temple bells rang without wind. The priests are concerned."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":4000,"act":"wandering","dir":"north","dist":8},{"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},{"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":12000,"act":"wandering","dir":"south","dist":10},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"gate_supplies","description":"Guard Zhao checks his spear: 'The gate watch needs leather for armor and arrows for the towers. Bring me supplies and I will pay from the city fund.'","requires":[{"item":"minecraft:leather","count":8},{"item":"minecraft:arrow","count":16}],"reward_item":"minecraft:emerald","reward_count":4},
              {"id":"omen_report","description":"Guard Zhao lowers his voice: 'Strange occurrences at night. I need torches and coal to light the watch route. If you see anything unusual, report it.'","requires":[{"item":"minecraft:coal","count":16},{"item":"minecraft:torch","count":8}],"reward_item":"minecraft:emerald","reward_count":3}]),

    make_npc("npc_tianshui_governor_bai", "Governor Bai", "白太守", "governor", "tian_shui_government",
             "Foundation Establishment (early)", "calculating, believes in fate, delegates to diviners",
             "measured, oblique",
             "Tian Shui's governor. The city's divination culture means the governor consults fortune tellers like Gao Qiming for political decisions.",
             ["Welcome to Tian Shui City, where heaven and water meet. I am Governor Bai. I trust your arrival was foretold.",
              "Gao Qiming predicted a cultivator of unusual destiny would visit this month. I wonder if that is you.",
              "The diviners advise caution in our dealings with the Teng family. Heaven's will is not always clear.",
              "The governor mansion's treasury funds the temple district. Faith and governance are intertwined here."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":4000,"act":"wandering","dir":"north","dist":8},{"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},{"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":10},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":12000,"act":"wandering","dir":"south","dist":12},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"tribute_collection","description":"Governor Bai strokes his beard: 'The seasonal tribute from the outer villages is due. Bring me wheat as proof of collection. The diviners say a bountiful harvest this year.'","requires":[{"item":"minecraft:wheat","count":16}],"reward_item":"minecraft:emerald","reward_count":6},
              {"id":"temple_donation","description":"Governor Bai nods: 'The temple requires emeralds for the fortune-telling ceremony. Bring me gold and I will convert it to the temple's offering. Heaven rewards generosity.'","requires":[{"item":"minecraft:gold_ingot","count":3},{"item":"minecraft:emerald","count":2}],"reward_item":"minecraft:emerald","reward_count":5}]),

    make_npc("npc_tianshui_merchant_liu", "Merchant Liu", "商贩刘", "merchant", "none",
             "none", "shrewd, sells fortune-related trinkets alongside goods",
             "salesman-like, mystical undertone",
             "Tian Shui market merchant. The city's divination culture means merchants sell fortune-related items alongside normal goods.",
             ["Fortune scrolls, protective talismans, iron tools, fresh bread — I sell what the people need. And what they fear they need.",
              "Business was better before the Teng family raised taxes. Now even fate cannot predict my profits.",
              "I heard Gao Qiming told a merchant that his ship would arrive safely. It did. That merchant now buys everything from me.",
              "If you need paper and ink, I have the best in the city. The diviners use my supplies for their scrolls."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":3000,"act":"wandering","dir":"south","dist":5},{"t0":3000,"t1":8000,"act":"wandering","dir":"east","dist":8},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":11000,"act":"wandering","dir":"west","dist":10},{"t0":11000,"t1":12000,"act":"wandering","dir":"north","dist":6},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"merchant_resupply","description":"Merchant Liu smiles: 'I need wheat for bread, coal for warmth, and iron for tools. The diviners say today is a good day for trade. Bring me supplies!'","requires":[{"item":"minecraft:wheat","count":16},{"item":"minecraft:coal","count":8},{"item":"minecraft:iron_ingot","count":4}],"reward_item":"minecraft:emerald","reward_count":8},
              {"id":"fortune_trinkets","description":"Liu leans in: 'I make fortune trinkets for the temple. Paper, ink, and glass bottles. Bring me the materials and I will pay — and give you a free reading.'","requires":[{"item":"minecraft:paper","count":4},{"item":"minecraft:ink_sac","count":2},{"item":"minecraft:glass_bottle","count":4}],"reward_item":"minecraft:emerald","reward_count":7}]),

    make_npc("npc_tianshui_beggar_sun", "Beggar Sun", "乞丐孙", "mortal", "none",
             "none", "cunning, claims to see fate, actually just observant",
             "rambling, pseudo-mystical",
             "Tian Shui's mortal quarter beggar. In a city of diviners, even beggars claim mystical insight.",
             ["Spare a coin? Or spare a moment? The stars told me you would come. They tell me many things. Most of them involve food.",
              "Gao Qiming? He is the real thing. Me? I just watch people. You learn more from watching than from any crystal ball.",
              "The temple priests say the smuggler tunnels are cursed. I say they are just dark. But cursed sounds better for donations.",
              "I know every crack in this city. Every hidden door. Not from divination — from hunger. Hungry people go everywhere."],
             [{"t0":0,"t1":3000,"act":"sleeping","dir":None,"dist":0},{"t0":3000,"t1":6000,"act":"wandering","dir":"north","dist":6},{"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},{"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":8},{"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},{"t0":11000,"t1":13000,"act":"wandering","dir":"south","dist":5},{"t0":13000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"beggar_food","description":"Beggar Sun grins: 'The stars say you will feed me. Or maybe that is just my stomach talking. Bring bread and I will tell you where the night watch is weakest.'","requires":[{"item":"minecraft:bread","count":8}],"reward_item":"minecraft:emerald","reward_count":2},
              {"id":"beggar_info","description":"Sun whispers: 'Coal for warmth, bread for my friends. In return — I saw the governor's men carrying something into the smuggler tunnels at midnight. Heavy crates. They were frightened.'","requires":[{"item":"minecraft:coal","count":8},{"item":"minecraft:bread","count":4}],"reward_item":"minecraft:emerald","reward_count":3}]),

    make_npc("npc_tianshui_dock_zhou", "Dock Master Zhou", "码头周", "laborer", "none",
             "none", "gruff, practical, dismissive of fortune-telling",
             "direct, no-nonsense",
             "Tian Shui dock master. In a city obsessed with fate, he is the pragmatist who actually moves the goods.",
             ["Another pair of hands? Good. The barge will not unload itself, no matter what the diviners say about favorable winds.",
              "This city talks too much about fate and not enough about cargo manifests. Three crates are missing again.",
              "A merchant from Heng Yue Sect came through last month. Bought a fortune reading and ten crates of iron. Make of that what you will.",
              "The warehouse is that way. Tell Manager Feng that Zhou sent you. He might give you better rates."],
             [{"t0":0,"t1":1000,"act":"sleeping","dir":None,"dist":0},{"t0":1000,"t1":4000,"act":"wandering","dir":"south","dist":10},{"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},{"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":11000,"act":"wandering","dir":"north","dist":8},{"t0":11000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"barge_unloading","description":"Dock Master Zhou wipes his brow: 'Barge needs unloading. Bring planks for pallets and sticks for bracing. Heavy work, fair pay. No fortune-telling required.'","requires":[{"item":"minecraft:oak_planks","count":12},{"item":"minecraft:stick","count":24}],"reward_item":"minecraft:emerald","reward_count":5},
              {"id":"dock_supplies","description":"Zhou points at the moorings: 'Storm season. Need coal for lanterns and iron for reinforcements. The diviners say storms are coming. For once, I believe them.'","requires":[{"item":"minecraft:coal","count":12},{"item":"minecraft:iron_ingot","count":6}],"reward_item":"minecraft:emerald","reward_count":7}]),

    make_npc("npc_tianshui_temple_feng", "Temple Priest Feng", "庙祝冯", "priest", "tian_shui_temple",
             "none", "devout, genuinely believes in fate, provides divination services",
             "measured, prophetic",
             "Tian Shui temple priest. The city is known for divination — the temple is its spiritual heart, more important than in other cities.",
             ["Welcome to the Temple of Heavenly Water. The diviners say your arrival was written in the stars long ago. Come — let me read your fortune.",
              "Gao Qiming is our most gifted diviner, but he keeps to himself. If you seek a reading, I can perform a lesser one for a small donation.",
              "The temple records every fortune told. Centuries of fate, catalogued in these books. The patterns... they repeat.",
              "Bring me coal for the incense braziers and paper for the fortune scrolls. The ancestors must not be forgotten."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":5000,"act":"wandering","dir":"north","dist":6},{"t0":5000,"t1":6000,"act":"eating","dir":None,"dist":0},{"t0":6000,"t1":9000,"act":"wandering","dir":"west","dist":8},{"t0":9000,"t1":10000,"act":"eating","dir":None,"dist":0},{"t0":10000,"t1":12000,"act":"wandering","dir":"south","dist":10},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"temple_incense","description":"Priest Feng bows: 'The incense braziers need coal. The fortune scrolls need paper. Heaven speaks through smoke and ink. Bring me the materials.'","requires":[{"item":"minecraft:coal","count":8},{"item":"minecraft:paper","count":8}],"reward_item":"minecraft:emerald","reward_count":5},
              {"id":"temple_repair","description":"Feng sighs: 'The temple roof leaks. Rain damages the fortune archives. Bring planks and glass and I will reward you — and read your future.'","requires":[{"item":"minecraft:oak_planks","count":12},{"item":"minecraft:glass","count":4}],"reward_item":"minecraft:book","reward_count":2}]),

    make_npc("npc_tianshui_tavern_keeper", "Tavern Keeper Qin", "酒馆老板秦", "tavern_keeper", "none",
             "none", "warm, listens more than talks, hears all rumors",
             "friendly, knowing",
             "Tavern keeper in a divination city — people drink and talk about their fortunes, making the tavern an information hub.",
             ["Sit down. You look like someone with a story. In Tian Shui, everyone has a story — and everyone wants to know how it ends.",
              "The diviners drink here after hours. When they drink, they talk. I have heard things about the governor that would make your hair stand on end.",
              "A cultivator from the Teng family came in last week. Demanded to know his fortune. Gao Qiming refused. The man left in a fury.",
              "If you bring me paper and ink, I can write down what I have heard. Some information is worth more than gold."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":3000,"act":"wandering","dir":"east","dist":5},{"t0":3000,"t1":8000,"act":"wandering","dir":"south","dist":8},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":12000,"act":"wandering","dir":"west","dist":10},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"tavern_supplies","description":"Tavern Keeper Qin smiles: 'Running low on wheat for bread and coal for the hearth. Bring supplies and I will share what the diviners said last night.'","requires":[{"item":"minecraft:wheat","count":16},{"item":"minecraft:coal","count":8}],"reward_item":"minecraft:emerald","reward_count":4},
              {"id":"tavern_intel","description":"Qin leans close: 'I know things. Guard rotations, merchant secrets, what the governor fears. Paper and ink to write it down. What I know is worth far more than what it costs.'","requires":[{"item":"minecraft:paper","count":4},{"item":"minecraft:ink_sac","count":2}],"reward_item":"minecraft:emerald","reward_count":8}]),

    make_npc("npc_tianshui_warehouse_feng", "Warehouse Manager Feng", "库管冯", "official", "tian_shui_government",
             "none", "meticulous, anxious, consults diviners for logistics decisions",
             "clipped, slightly nervous",
             "Warehouse manager in a city where even logistics is influenced by fortune-telling.",
             ["You are not authorized in the warehouse. All goods are property of Tian Shui City. The diviners said today would bring unexpected visitors — I suppose that is you.",
              "The dock shipments were short again. I asked Gao Qiming if the missing goods were stolen or lost at sea. He just smiled.",
              "We need more storage. The tribute keeps growing but the warehouse walls stay the same. Some things even fate cannot fix.",
              "Bring me paper and ink for the inventory records. The old ones were damaged. Water damage. The diviners said it was a bad omen."],
             [{"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},{"t0":2000,"t1":3000,"act":"wandering","dir":"south","dist":8},{"t0":3000,"t1":5000,"act":"wandering","dir":"east","dist":10},{"t0":5000,"t1":6000,"act":"eating","dir":None,"dist":0},{"t0":6000,"t1":10000,"act":"wandering","dir":"north","dist":12},{"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},{"t0":11000,"t1":12000,"act":"wandering","dir":"west","dist":6},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"inventory_audit","description":"Manager Feng checks his ledger: 'The diviners say the records must be rewritten. Bad energy clings to the old ones. Bring me paper and ink.'","requires":[{"item":"minecraft:paper","count":8},{"item":"minecraft:ink_sac","count":4}],"reward_item":"minecraft:emerald","reward_count":5},
              {"id":"storage_repair","description":"Feng wrings his hands: 'Moisture ruined storage bins. The diviners warned us. Planks and cobblestone for repairs. Heaven help us if the tribute is damaged.'","requires":[{"item":"minecraft:oak_planks","count":16},{"item":"minecraft:cobblestone","count":24}],"reward_item":"minecraft:emerald","reward_count":6}]),

    make_npc("npc_tianshui_smuggler_hei", "Black Market Hei", "黑市黑", "rogue", "none",
             "mid Qi Condensation", "paranoid, cynical, mocks the divination culture",
             "whispered, sarcastic",
             "Smuggler who cynically exploits the city's fortune-telling culture. Sells 'fate-changing' items at premium prices.",
             ["You found the tunnels. The diviners say there are no tunnels. Funny how heaven's will aligns with the governor's convenience.",
              "I sell things the market cannot. Rare materials, forbidden knowledge. The diviners call it 'interfering with fate.' I call it business.",
              "Gao Qiming is the only real seer in this city. Everyone else is selling snake oil. Including me. But at least I am honest about it.",
              "Stay off the main tunnel past midnight. That is when the governor's cultivators make their deposits. Fate will not save you if they catch you."],
             [{"t0":0,"t1":4000,"act":"sleeping","dir":None,"dist":0},{"t0":4000,"t1":6000,"act":"wandering","dir":"north","dist":8},{"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},{"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":10},{"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},{"t0":11000,"t1":14000,"act":"wandering","dir":"south","dist":6},{"t0":14000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"smuggled_goods","description":"Hei grins: 'Gold and emeralds. I have buyers who pay premium for fate-altering materials. Bring me goods and I will give you a cut — and a map of places the diviners cannot see.'","requires":[{"item":"minecraft:gold_ingot","count":4},{"item":"minecraft:emerald","count":6}],"reward_item":"minecraft:emerald","reward_count":12},
              {"id":"tunnel_expansion","description":"Hei whispers: 'Extending toward the warehouse. Iron and cobblestone. Discreetly. The governor's guards cannot know — fate has not foretold this.'","requires":[{"item":"minecraft:iron_ingot","count":8},{"item":"minecraft:cobblestone","count":32}],"reward_item":"minecraft:emerald","reward_count":8}]),

    make_npc("npc_tianshui_cultivator_ye", "Cultivator Ye", "修士叶", "cultivator", "tian_shui_government",
             "Foundation Establishment (early)", "skeptical of divination, relies on cultivation",
             "dry, slightly dismissive",
             "Tian Shui cultivator who serves the government but is privately skeptical of the city's obsession with fortune-telling.",
             ["The cultivator quarter is for those who rely on their own power, not the stars. I have no patience for fortune-telling.",
              "The governor consults Gao Qiming for every decision. Every. Single. One. It is exhausting. Cultivation requires action, not divination.",
              "I heard Heng Yue Sect has a promising disciple. Wang something. Perhaps he will visit Tian Shui someday.",
              "If you want to train, bring me cultivation materials. Coal, iron, emeralds for purchasing actual spirit stones. I will share what I know."],
             [{"t0":0,"t1":1000,"act":"sleeping","dir":None,"dist":0},{"t0":1000,"t1":4000,"act":"wandering","dir":"north","dist":15},{"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},{"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},{"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},{"t0":9000,"t1":12000,"act":"wandering","dir":"west","dist":10},{"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"cultivation_aid","description":"Cultivator Ye scowls: 'My breakthrough requires resources. Iron, coal, emeralds. I will not consult a diviner — I will make my own fate. Help me and I share a technique scroll.'","requires":[{"item":"minecraft:iron_ingot","count":10},{"item":"minecraft:coal","count":16},{"item":"minecraft:emerald","count":4}],"reward_item":"minecraft:book","reward_count":2},
              {"id":"patrol_duty","description":"Ye sighs: 'The governor wants districts patrolled. The diviners said danger approaches. Bring arrows and coal for signal fires.'","requires":[{"item":"minecraft:arrow","count":24},{"item":"minecraft:coal","count":8}],"reward_item":"minecraft:emerald","reward_count":6}]),

    make_npc("npc_tianshui_mortal_granny", "Granny Chen", "陈婆婆", "elder", "none",
             "none", "wise, remembers old Tian Shui, dismissive of new fortune-fad",
             "slow, nostalgic",
             "Elderly mortal who remembers Tian Shui before it became obsessed with divination. Provides historical perspective.",
             ["Young one. Sit. You look tired. In my day, people did not need diviners to tell them when to eat or sleep. They just ate and slept.",
              "Tian Shui was a simple fishing city before the temple grew powerful. Now every decision requires a fortune. It is exhausting.",
              "The Teng family changed everything. Before them, we governed ourselves. Now the governor asks crystal balls for permission to breathe.",
              "If you have bread to share, I will tell you about the old days. The days when fate was something you made, not something you read."],
             [{"t0":0,"t1":3000,"act":"sleeping","dir":None,"dist":0},{"t0":3000,"t1":6000,"act":"wandering","dir":"north","dist":6},{"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},{"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":8},{"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},{"t0":11000,"t1":13000,"act":"wandering","dir":"south","dist":5},{"t0":13000,"t1":24000,"act":"sleeping","dir":None,"dist":0}],
             [{"id":"elder_food","description":"Granny Chen pats your hand: 'The mortal quarter needs food. The governor takes too much in tax. Bring bread and wheat and I will see it distributed. No fortune required — just kindness.'","requires":[{"item":"minecraft:bread","count":12},{"item":"minecraft:wheat","count":16}],"reward_item":"minecraft:emerald","reward_count":3},
              {"id":"elder_shelter","description":"Granny Chen shakes her head: 'Winter comes. The shelters need glass for windows, planks for walls. In the old days, the community built them together. Now we need strangers' help.'","requires":[{"item":"minecraft:glass","count":8},{"item":"minecraft:oak_planks","count":16}],"reward_item":"minecraft:emerald","reward_count":4}]),
]

print("\n=== PART 3: Creating 10 INFERRED NPCs ===")
written = 0
for npc in NPCS:
    path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(npc, f, indent=2, ensure_ascii=False)
    written += 1
    print(f"  {npc['npc_id']}: {npc['name']} ({len(npc['initiation_lines'])} lines, {len(npc['daily_schedule'])} sched, {len(npc['sect_tasks'])} tasks)")

# ═══════════════════════════════════════════════════════════════════
# Also add schedule+tasks to Gao Qiming (he has none)
# ═══════════════════════════════════════════════════════════════════
print("\n=== UPDATING Gao Qiming (adding schedule + tasks) ===")
gao_path = os.path.join(NPC_DIR, "npc_gao_qiming.json")
with open(gao_path) as f:
    gao = json.load(f)
gao["daily_schedule"] = [
    {"t0":0,"t1":3000,"act":"sleeping","dir":None,"dist":0},
    {"t0":3000,"t1":6000,"act":"wandering","dir":"south","dist":10},
    {"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},
    {"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":15},
    {"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},
    {"t0":11000,"t1":14000,"act":"wandering","dir":"north","dist":12},
    {"t0":14000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
]
gao["sect_tasks"] = [
    {"id":"fortune_reading","description":"Gao Qiming gazes into your eyes: 'You wish to know your fate? Bring me emeralds as an offering and paper to record the reading. Heaven reveals what it will.'","requires":[{"item":"minecraft:emerald","count":4},{"item":"minecraft:paper","count":4}],"reward_item":"minecraft:book","reward_count":2},
    {"id":"omen_investigation","description":"Gao Qiming frowns: 'Dark omens gather over the smuggler tunnels. I need coal for my divination fire and ink to sketch what I see. This concerns you more than you know.'","requires":[{"item":"minecraft:coal","count":8},{"item":"minecraft:ink_sac","count":4}],"reward_item":"minecraft:emerald","reward_count":6},
]
with open(gao_path, "w", encoding="utf-8") as f:
    json.dump(gao, f, indent=2, ensure_ascii=False)
print(f"  npc_gao_qiming: added {len(gao['daily_schedule'])} schedule entries, {len(gao['sect_tasks'])} tasks")

# ═══════════════════════════════════════════════════════════════════
# VERIFICATION
# ═══════════════════════════════════════════════════════════════════

print("\n=== VERIFICATION: All 11 Tian Shui City NBTs ===")
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

print(f"\n=== SUMMARY ===")
print(f"Loot tables: 11 new (total 13 for Tian Shui City, 2 orphan)")
print(f"Chest injections: {total_chests} new chests in 11 NBTs")
print(f"NPCs: {written} new INFERRED + 1 canon updated (Gao Qiming) = 11 total")
print(f"Grand total: {11 + total_chests + written + 1} new content pieces")