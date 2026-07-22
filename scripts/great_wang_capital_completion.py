#!/usr/bin/env python3
"""AUTO-CANON-032: Great Wang Capital — 5 missing loot tables + 15 chest injections + 7 INFERRED NPCs.

Great Wang Capital is the capital of Zhao Country. In RI, it is the seat of
mortal governance — less powerful than the Teng family, but still the
administrative center. The city is larger and more formal than Teng Family City.

Canon basis: Zhao Country's capital exists in RI as the political center
where Wang Lin's family once had standing. The governor (Zhao Ming) reports
to the Teng family. The city would have markets, temples, docks, warehouses,
residential areas, and — given its political nature — smuggler tunnels for
the governor's own secrets.

Gaps to fill:
- 5 NBTs have 0 loot refs: market_district, mortal_quarter, residential_district,
  smuggler_tunnels, warehouse_district
- 5 NPCs exist but 11 districts need more
- Wealth gradient: governor_mansion > residential > market > warehouse > temple > port > city_gate > smuggler > mortal

Zero Java changes. Article XXII, XXIII, XXVI.
"""
import json, os, struct, glob, copy

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
BASE = os.path.join(os.path.dirname(SCRIPT_DIR),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "great_wang_capital")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ── NBT binary helpers (from teng_city_loot.py, handles BE+LE) ──
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
    # Market District — the capital's trade hub, more formal than Teng's
    "great_wang_capital_market_district": {
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
    # Mortal Quarter — the capital's poor, slightly better than Teng's
    "great_wang_capital_mortal_quarter": {
        "rolls": {"min": 1, "max": 3},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:bread","weight":18,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:wheat","weight":14,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:stick","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":3,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:string","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":2,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":1}}]},
            {"type":"minecraft:empty","weight":18},
        ]
    },
    # Residential District — government officials, moderate wealth
    "great_wang_capital_residential_district": {
        "rolls": {"min": 2, "max": 4},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":4}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:book","weight":6,"functions":[]},
            {"type":"minecraft:item","name":"minecraft:paper","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":14},
        ]
    },
    # Smuggler Tunnels — governor's dark secrets, moderate contraband
    "great_wang_capital_smuggler_tunnels": {
        "rolls": {"min": 2, "max": 5},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:emerald","weight":16,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":8}}]},
            {"type":"minecraft:item","name":"minecraft:gold_nugget","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:diamond","weight":2,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":1}}]},
            {"type":"minecraft:item","name":"minecraft:glass_bottle","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:item","name":"minecraft:arrow","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":12}}]},
            {"type":"minecraft:item","name":"minecraft:ink_sac","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":1,"max":3}}]},
            {"type":"minecraft:empty","weight":12},
        ]
    },
    # Warehouse District — capital's grain and material reserves
    "great_wang_capital_warehouse_district": {
        "rolls": {"min": 3, "max": 6},
        "entries": [
            {"type":"minecraft:item","name":"minecraft:wheat","weight":16,"functions":[{"function":"minecraft:set_count","count":{"min":8,"max":20}}]},
            {"type":"minecraft:item","name":"minecraft:coal","weight":12,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":12}}]},
            {"type":"minecraft:item","name":"minecraft:iron_ingot","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":6}}]},
            {"type":"minecraft:item","name":"minecraft:oak_planks","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":8,"max":16}}]},
            {"type":"minecraft:item","name":"minecraft:emerald","weight":6,"functions":[{"function":"minecraft:set_count","count":{"min":2,"max":5}}]},
            {"type":"minecraft:item","name":"minecraft:bread","weight":10,"functions":[{"function":"minecraft:set_count","count":{"min":4,"max":10}}]},
            {"type":"minecraft:item","name":"minecraft:stick","weight":8,"functions":[{"function":"minecraft:set_count","count":{"min":8,"max":16}}]},
            {"type":"minecraft:empty","weight":10},
        ]
    },
}

print("=== PART 1: Creating 5 loot tables ===")
for name, spec in LOOT.items():
    table = {"type": "minecraft:chest", "pools": [{"rolls": spec["rolls"], "entries": spec["entries"]}]}
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, 'w') as f:
        json.dump(table, f, indent=2); f.write("\n")
    print(f"  {name}: {len(spec['entries'])} entries")

# ═══════════════════════════════════════════════════════════════════
# PART 2: INJECT CHESTS INTO 5 NBTs
# ═══════════════════════════════════════════════════════════════════

LP = "ergenverse:chests/great_wang_capital"

CHESTS = {
    "market_district.nbt": [
        (4, 1, 5, "north", f"{LP}/great_wang_capital_market_district"),
        (8, 2, 3, "east",  f"{LP}/great_wang_capital_market_district"),
        (6, 1, 7, "south", f"{LP}/great_wang_capital_market_district"),
    ],
    "mortal_quarter.nbt": [
        (5, 1, 4, "south", f"{LP}/great_wang_capital_mortal_quarter"),
        (7, 1, 6, "north", f"{LP}/great_wang_capital_mortal_quarter"),
        (3, 2, 3, "east",  f"{LP}/great_wang_capital_mortal_quarter"),
    ],
    "residential_district.nbt": [
        (4, 1, 5, "east",  f"{LP}/great_wang_capital_residential_district"),
        (8, 2, 3, "west",  f"{LP}/great_wang_capital_residential_district"),
        (6, 1, 7, "north", f"{LP}/great_wang_capital_residential_district"),
        (10, 1, 5, "south", f"{LP}/great_wang_capital_residential_district"),
    ],
    "smuggler_tunnels.nbt": [
        (3, 1, 3, "east",  f"{LP}/great_wang_capital_smuggler_tunnels"),
        (7, 1, 7, "south", f"{LP}/great_wang_capital_smuggler_tunnels"),
        (5, 2, 5, "north", f"{LP}/great_wang_capital_smuggler_tunnels"),
    ],
    "warehouse_district.nbt": [
        (4, 1, 5, "south", f"{LP}/great_wang_capital_warehouse_district"),
        (8, 1, 8, "east",  f"{LP}/great_wang_capital_warehouse_district"),
        (6, 2, 3, "west",  f"{LP}/great_wang_capital_warehouse_district"),
        (10, 1, 6, "north", f"{LP}/great_wang_capital_warehouse_district"),
    ],
}

print("\n=== PART 2: Injecting chests into 5 NBTs ===")
total_chests = 0
for nbt_name, specs in CHESTS.items():
    nbt_path = os.path.join(NBT_DIR, nbt_name)
    n = patch_structure(nbt_path, specs)
    total_chests += n
    print(f"  {nbt_name}: +{n} chests")
print(f"  Total: {total_chests} new chests")


# ═══════════════════════════════════════════════════════════════════
# PART 3: 7 INFERRED NPCs
# ═══════════════════════════════════════════════════════════════════

def make_npc(npc_id, name, nameCn, npc_type, faction, cultivation,
             personality, speech, canon_note, initiation_lines,
             daily_schedule, sect_tasks, canon_confidence=1, relationship="INFERRED"):
    return {
        "_comment": f"NPC: {name} ({nameCn}). INFERRED from canon context: {canon_note}. Derivation: I = INFERRED. Per Article XV, this NPC fills a role implied by canon but not explicitly named.",
        "npc_id": npc_id,
        "name": name,
        "nameCn": nameCn,
        "canon_id": f"I-{npc_id}",
        "type": npc_type,
        "faction": faction,
        "location": "great_wang_capital",
        "cultivation": cultivation,
        "personality": personality,
        "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True,
        "quest_available": len(sect_tasks) > 0,
        "trade_available": False,
        "teaching_available": False,
        "perception_tiers": {
            "mortal": f"A {personality.split(',')[0]} {npc_type}",
            "qi_condensation": f"{name}, {faction} {npc_type}"
        },
        "canon_confidence": canon_confidence,
        "note": f"INFERRED: {canon_note}",
        "derivation_type": "I",
        "salt": hash(npc_id) % 2147483647,
        "dao_heart": {"stability": 50, "cracks": [], "last_tested_tick": None, "note": "0-100."},
        "soul_state": "none",
        "tribulation_debt": 0,
        "_xianxia_schema": 1,
        "initiation_lines": initiation_lines,
        "daily_schedule": daily_schedule,
        "sect_tasks": sect_tasks
    }

NPCS = [
    # 1. Capital Guard — patrols city gate, enforces governor's law
    make_npc(
        npc_id="npc_capital_guard_chen",
        name="Capital Guard Chen",
        nameCn="京卫兵陈",
        npc_type="guard",
        faction="zhao_country_government",
        cultivation="late Qi Condensation",
        personality="disciplined, watchful, respects authority",
        speech="formal, direct",
        canon_note="The capital of Zhao Country requires a garrison. The city gate structure implies guards who check visitors, enforce curfews, and maintain order under the governor's authority.",
        initiation_lines=[
            "Halt. You stand before the Great Wang Capital. State your name, origin, and business.",
            "The governor's peace is maintained here. Weapons must be sheathed within the market district. Cultivation disputes are handled at the cultivator quarter.",
            "The Teng family's envoys passed through last week. Heavy guard. Whatever they carried, it was important.",
            "If you seek lodging, the residential district is to the west. If you seek trouble, the city gate is behind you.",
        ],
        daily_schedule=[
            {"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},
            {"t0":2000,"t1":4000,"act":"wandering","dir":"north","dist":8},
            {"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},
            {"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},
            {"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},
            {"t0":9000,"t1":12000,"act":"wandering","dir":"south","dist":10},
            {"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"patrol_equipment","description":"Guard Chen nods: 'The night patrol needs leather for armor patches and arrows for the watchtowers. Bring me the materials and I will pay from the garrison fund.'","requires":[{"item":"minecraft:leather","count":8},{"item":"minecraft:arrow","count":16}],"reward_item":"minecraft:emerald","reward_count":4},
            {"id":"gate_repairs","description":"Guard Chen gestures at the gate: 'The gate took damage during the last merchant caravan incident. Bring planks and iron to repair it. The governor will compensate you.'","requires":[{"item":"minecraft:oak_planks","count":12},{"item":"minecraft:iron_ingot","count":4}],"reward_item":"minecraft:emerald","reward_count":5},
        ],
    ),
    # 2. Warehouse Manager — warehouse district
    make_npc(
        npc_id="npc_warehouse_manager_sun",
        name="Warehouse Manager Sun",
        nameCn="库管孙",
        npc_type="official",
        faction="zhao_country_government",
        cultivation="none",
        personality="meticulous, stressed, loyal bureaucrat",
        speech="clipped, efficient",
        canon_note="A capital city stores tribute and supplies in warehouses. The warehouse district implies a manager who tracks inventory, receives shipments from the docks, and distributes to other districts.",
        initiation_lines=[
            "You are not authorized to enter the warehouse. All goods here are property of the Zhao Country government.",
            "The dock shipments were short again. Three crates of iron missing. I have filed a report with the governor, but... well, the Teng family gets priority.",
            "If you have goods to declare, see the tax office at the city gate. Everything that enters the capital is counted.",
            "We need more storage space. The tribute from the outer provinces keeps growing, but the warehouse walls do not.",
        ],
        daily_schedule=[
            {"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},
            {"t0":2000,"t1":3000,"act":"wandering","dir":"south","dist":8},
            {"t0":3000,"t1":5000,"act":"wandering","dir":"east","dist":10},
            {"t0":5000,"t1":6000,"act":"eating","dir":None,"dist":0},
            {"t0":6000,"t1":10000,"act":"wandering","dir":"north","dist":12},
            {"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},
            {"t0":11000,"t1":12000,"act":"wandering","dir":"west","dist":6},
            {"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"inventory_audit","description":"Manager Sun checks his ledger: 'The governor demands an audit. I need paper and ink to rewrite the inventory records. The old ones were... damaged in a flood. Bring me materials.'","requires":[{"item":"minecraft:paper","count":8},{"item":"minecraft:ink_sac","count":4}],"reward_item":"minecraft:emerald","reward_count":5},
            {"id":"storage_repair","description":"Manager Sun sighs: 'Moisture ruined a section of the warehouse. We need planks and cobblestone to rebuild the storage bins. The tribute must be protected.'","requires":[{"item":"minecraft:oak_planks","count":16},{"item":"minecraft:cobblestone","count":24}],"reward_item":"minecraft:emerald","reward_count":6},
        ],
    ),
    # 3. Mortal Elder — mortal quarter, community figure
    make_npc(
        npc_id="npc_mortal_elder_wu",
        name="Elder Wu",
        nameCn="吴长老",
        npc_type="elder",
        faction="none",
        cultivation="none",
        personality="wise, tired, protective of his people",
        speech="slow, story-telling",
        canon_note="The mortal quarter of a capital city would have community elders who mediate disputes, remember history, and care for the poor. Elder Wu provides the player with perspective on how mortals live under cultivator rule.",
        initiation_lines=[
            "Young one. You carry the air of a cultivator. In the mortal quarter, that means either help or trouble. I hope it is the former.",
            "This quarter has stood for four generations. Before the Teng family rose. Before the governor built his mansion. We remember what it was like.",
            "The temple district used to be open to everyone. Now the priests charge for blessings. Everything has a price in the capital.",
            "If you have bread to spare, share it. The children here have not eaten well since the tax increased.",
        ],
        daily_schedule=[
            {"t0":0,"t1":3000,"act":"sleeping","dir":None,"dist":0},
            {"t0":3000,"t1":6000,"act":"wandering","dir":"north","dist":6},
            {"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},
            {"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":8},
            {"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},
            {"t0":11000,"t1":13000,"act":"wandering","dir":"south","dist":5},
            {"t0":13000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"quarter_food","description":"Elder Wu's eyes soften: 'The quarter is running low on food. The governor's tax took most of our harvest. If you could bring bread and wheat, I will see it distributed fairly.'","requires":[{"item":"minecraft:bread","count":12},{"item":"minecraft:wheat","count":16}],"reward_item":"minecraft:emerald","reward_count":3},
            {"id":"repair_shelter","description":"Elder Wu points to a crumbling building: 'Winter is coming. The shelters need repairs. Glass for windows, planks for walls. The young ones cannot survive another cold season like the last.'","requires":[{"item":"minecraft:glass","count":8},{"item":"minecraft:oak_planks","count":16}],"reward_item":"minecraft:emerald","reward_count":4},
        ],
    ),
    # 4. Capital Cultivator — cultivator quarter
    make_npc(
        npc_id="npc_capital_cultivator_feng",
        name="Cultivator Feng",
        nameCn="修士冯",
        npc_type="cultivator",
        faction="zhao_country_government",
        cultivation="Foundation Establishment (early)",
        personality="ambitious, slightly cynical, politically aware",
        speech="measured, slightly condescending",
        canon_note="The capital's cultivator quarter would house cultivators who serve the government — enforcers, advisors, or those seeking patronage. In RI, cultivators in mortal cities often serve political roles.",
        initiation_lines=[
            "The cultivator quarter is not for idlers. We serve the governor and, through him, the Teng family. Our cultivation is the price of protection.",
            "I have heard of Heng Yue Sect. A minor sect in the mountains. They produce decent disciples, but nothing compared to the major sects of the central regions.",
            "The governor has a hidden collection of cultivation resources in the mansion. Officially, they are 'for the defense of the capital.' Unofficially... well.",
            "If you seek cultivation guidance, I am not a teacher. But I might know where to find one — for the right price.",
        ],
        daily_schedule=[
            {"t0":0,"t1":1000,"act":"sleeping","dir":None,"dist":0},
            {"t0":1000,"t1":4000,"act":"wandering","dir":"north","dist":15},
            {"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},
            {"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},
            {"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},
            {"t0":9000,"t1":12000,"act":"wandering","dir":"west","dist":10},
            {"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"cultivation_aid","description":"Cultivator Feng raises an eyebrow: 'My breakthrough requires resources. Iron for formations, coal for furnaces, emeralds to purchase spirit stones. Help me and I will share a technique scroll.'","requires":[{"item":"minecraft:iron_ingot","count":10},{"item":"minecraft:coal","count":16},{"item":"minecraft:emerald","count":4}],"reward_item":"minecraft:book","reward_count":2},
            {"id":"patrol_duty","description":"Cultivator Feng smirks: 'The governor wants the outer districts patrolled. Bring arrows and coal for signal fires. Consider it... community service.'","requires":[{"item":"minecraft:arrow","count":24},{"item":"minecraft:coal","count":8}],"reward_item":"minecraft:emerald","reward_count":6},
        ],
    ),
    # 5. Dock Worker — port docks
    make_npc(
        npc_id="npc_dock_worker_li",
        name="Dock Worker Li",
        nameCn="码头工李",
        npc_type="laborer",
        faction="none",
        cultivation="none",
        personality="strong, honest, overworked",
        speech="blunt, friendly",
        canon_note="A capital city on a river/coast needs docks. Dock workers handle shipments, hear rumors from traveling merchants, and provide the player with information about trade routes and incoming goods.",
        initiation_lines=[
            "Another pair of hands? Good. The barge from the southern provinces just arrived and the foreman is screaming about delays.",
            "Most of what comes through these docks goes to the Teng family warehouses first. The capital gets the leftovers. That is just how it is.",
            "A merchant came through last week asking about Wang Family Village. Unusual — nobody asks about that place. He was in a hurry.",
            "If you need work, the warehouse manager is always short-staffed. Tell him Li sent you. Might get you better pay.",
        ],
        daily_schedule=[
            {"t0":0,"t1":1000,"act":"sleeping","dir":None,"dist":0},
            {"t0":1000,"t1":4000,"act":"wandering","dir":"south","dist":10},
            {"t0":4000,"t1":5000,"act":"eating","dir":None,"dist":0},
            {"t0":5000,"t1":8000,"act":"wandering","dir":"east","dist":12},
            {"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},
            {"t0":9000,"t1":11000,"act":"wandering","dir":"north","dist":8},
            {"t0":11000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"barge_unloading","description":"Dock Worker Li wipes sweat: 'The barge needs unloading. Bring planks for pallets and sticks for bracing. Heavy crates of iron and coal. Hard work, fair pay.'","requires":[{"item":"minecraft:oak_planks","count":12},{"item":"minecraft:stick","count":24}],"reward_item":"minecraft:emerald","reward_count":5},
            {"id":"dock_supplies","description":"Li squints at the sky: 'Storm season is coming. We need coal for the dock lanterns and iron to reinforce the moorings. Help me out and I will share what the merchants pay me.'","requires":[{"item":"minecraft:coal","count":12},{"item":"minecraft:iron_ingot","count":6}],"reward_item":"minecraft:emerald","reward_count":7},
        ],
    ),
    # 6. Street Vendor — market district
    make_npc(
        npc_id="npc_street_vendor_ma",
        name="Street Vendor Ma",
        nameCn="摊贩马",
        npc_type="merchant",
        faction="none",
        cultivation="none",
        personality="cheerful, shrewd, gossipy",
        speech="loud, friendly, sales-oriented",
        canon_note="A capital market needs vendors. Unlike the formal Merchant Hong, a street vendor operates on a smaller scale, hears street rumors, and provides the player with affordable goods and gossip.",
        initiation_lines=[
            "Fresh bread! Iron tools! Coal for the winter! Best prices in the capital — I guarantee it, or my name is not Ma!",
            "Business has been slow since the Teng family raised the market tax. Three emeralds per stall, per month. How is a simple vendor supposed to survive?",
            "You look like someone who travels. I heard a rumor that a cultivator from Heng Yue Sect passed through the capital last month. Looking for something — or someone.",
            "The tavern keeper buys bread from me every morning. We have an arrangement. If you need information, he is the one to talk to.",
        ],
        daily_schedule=[
            {"t0":0,"t1":2000,"act":"sleeping","dir":None,"dist":0},
            {"t0":2000,"t1":3000,"act":"wandering","dir":"south","dist":5},
            {"t0":3000,"t1":8000,"act":"wandering","dir":"east","dist":8},
            {"t0":8000,"t1":9000,"act":"eating","dir":None,"dist":0},
            {"t0":9000,"t1":11000,"act":"wandering","dir":"west","dist":10},
            {"t0":11000,"t1":12000,"act":"wandering","dir":"north","dist":6},
            {"t0":12000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"vendor_resupply","description":"Vendor Ma grins: 'I am running low on stock! Bring me wheat for bread, coal for the forge, and iron for tools. I will pay you in emeralds — fair price, as always!'","requires":[{"item":"minecraft:wheat","count":16},{"item":"minecraft:coal","count":8},{"item":"minecraft:iron_ingot","count":4}],"reward_item":"minecraft:emerald","reward_count":8},
            {"id":"market_intel","description":"Ma leans in conspiratorially: 'For paper and ink, I can write down what I have heard about the Teng family's next tax increase. Information like that is worth gold to the right people.'","requires":[{"item":"minecraft:paper","count":4},{"item":"minecraft:ink_sac","count":2}],"reward_item":"minecraft:emerald","reward_count":6},
        ],
    ),
    # 7. Underground Informant — smuggler tunnels
    make_npc(
        npc_id="npc_underground_qian",
        name="Underground Qian",
        nameCn="地下钱",
        npc_type="rogue",
        faction="none",
        cultivation="mid Qi Condensation",
        personality="nervous, opportunistic, well-informed",
        speech="whispered, coded",
        canon_note="A capital city's smuggler tunnels imply a network of informants and black-market dealers. Qian provides the player with access to illegal information and goods, and hints at the governor's corruption.",
        initiation_lines=[
            "You found this place. Either you are very clever or very foolish. Let us hope it is the former. What do you need?",
            "The governor's smuggler tunnels are not for the Teng family — they are his own. He skims from the tribute before reporting to Teng Huayuan. If the patriarch knew...",
            "I can get you things that the market district cannot. Rare materials, forbidden texts, information about who really controls Zhao Country. But everything has a price.",
            "Stay off the main tunnel after the sixth hour past midnight. That is when the governor's personal guard makes their deposits. You do not want to be seen.",
        ],
        daily_schedule=[
            {"t0":0,"t1":4000,"act":"sleeping","dir":None,"dist":0},
            {"t0":4000,"t1":6000,"act":"wandering","dir":"north","dist":8},
            {"t0":6000,"t1":7000,"act":"eating","dir":None,"dist":0},
            {"t0":7000,"t1":10000,"act":"wandering","dir":"east","dist":10},
            {"t0":10000,"t1":11000,"act":"eating","dir":None,"dist":0},
            {"t0":11000,"t1":14000,"act":"wandering","dir":"south","dist":6},
            {"t0":14000,"t1":24000,"act":"sleeping","dir":None,"dist":0},
        ],
        sect_tasks=[
            {"id":"smuggled_goods","description":"Qian glances around: 'I have a buyer for gold and emeralds. Bring me the goods and I will give you a cut — and a map. A map of places the governor does not want found.'","requires":[{"item":"minecraft:gold_ingot","count":4},{"item":"minecraft:emerald","count":6}],"reward_item":"minecraft:emerald","reward_count":12},
            {"id":"tunnel_supplies","description":"Qian whispers: 'We are extending the tunnel toward the warehouse. Need iron and cobblestone — quietly. The governor's guards cannot know.'","requires":[{"item":"minecraft:iron_ingot","count":8},{"item":"minecraft:cobblestone","count":32}],"reward_item":"minecraft:emerald","reward_count":8},
        ],
    ),
]

print("\n=== PART 3: Creating 7 INFERRED NPCs ===")
written = 0
for npc in NPCS:
    path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(npc, f, indent=2, ensure_ascii=False)
    written += 1
    print(f"  {npc['npc_id']}: {npc['name']} ({len(npc['initiation_lines'])} lines, {len(npc['daily_schedule'])} sched, {len(npc['sect_tasks'])} tasks)")

# ═══════════════════════════════════════════════════════════════════
# VERIFICATION
# ═══════════════════════════════════════════════════════════════════

print("\n=== VERIFICATION: All 11 Great Wang Capital NBTs ===")
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
print(f"Loot tables: 5 new (total 11 for Great Wang Capital)")
print(f"Chest injections: {total_chests} new chests in 5 NBTs")
print(f"NPCs: {written} new INFERRED (total 12 for Great Wang Capital)")
print(f"Grand total: {5 + total_chests + written} new content pieces")