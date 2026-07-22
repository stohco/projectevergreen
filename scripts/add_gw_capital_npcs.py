#!/usr/bin/env python3
"""AUTO-CANON-024: Create 5 INFERRED NPCs for Great Wang Capital (Zhao Country capital).

Canon context (RI Ch.1-10):
- Great Wang Capital is the political center of Zhao Country.
- The Teng family exerts control over Zhao Country from Teng Family City.
- The capital has its own governor, guards, merchants, temple priests.
- No named capital inhabitants appear in the novel — all NPCs are INFERRED
  from the existence of 11 city districts (governor mansion, cultivator quarter,
  temple, market, tavern, port, etc.) and the principle that a capital city
  must have inhabitants (Article II: "Reality First", Article V: "Everything
  Exists Without The Player").

NPCs created (all INFERRED, canon_confidence=2):
1. Governor Zhao Ming — Foundation Establishment, political governor, puppet of Teng family
2. Guard Captain Lin — Qi Condensation 9, head of city guard, military
3. Priestess Yun — Qi Condensation 6, temple district spiritual figure
4. Merchant Hong — Mortal, market district trader
5. Tavern Keeper Old Zhou — Mortal, tavern information hub

Per Article XXIV: NPCs must initiate gameplay (initiation_lines + sect_tasks).
Per Article XXI: World is main character (daily_schedule for all).
Per Article XXVI: Use existing systems (NpcSectMissionGoal, NpcScheduleGoal). Zero Java changes.
"""

import json
import os

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                     "src/main/resources/data/ergenverse/npcs")

def make_npc(npc_id, name, name_cn, canon_id, npc_type, faction, location, cultivation,
             personality, speech, relationship, lines, schedule, tasks,
             quest_available=True, trade_available=False, teaching_available=False,
             max_hp=None):
    """Create a complete NPC JSON dict."""
    d = {
        "_comment": f"NPC: {name} ({name_cn}). INFERRED — Great Wang Capital inhabitant. "
                   f"Canon ID: {canon_id}. Per Article II (Reality First): the Zhao Country "
                   f"capital must have inhabitants. These NPCs are inferred from the city's "
                   f"district structure (governor mansion, temple, market, tavern, port). "
                   f"Prime Directive: these beings exist independently of the player's perception.",
        "npc_id": npc_id,
        "name": name,
        "nameCn": name_cn,
        "canon_id": canon_id,
        "type": npc_type,
        "faction": faction,
        "location": location,
        "cultivation": cultivation,
        "personality": personality,
        "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True,
        "quest_available": quest_available,
        "trade_available": trade_available,
        "teaching_available": teaching_available,
        "perception_tiers": {
            "mortal": f"A {npc_type.replace('_', ' ')} of the capital",
            "qi_condensation": f"{name} — {cultivation}",
            "foundation": f"{name} — {cultivation}. INFERRED capital resident.",
            "nascent_soul": f"{name} — minor figure in the capital's power structure"
        },
        "canon_confidence": 2,
        "note": f"INFERRED from Great Wang Capital district structure. {personality}",
        "derivation_type": "I",
        "salt": hash(name) & 0x7FFFFFFF,
        "dao_heart": {
            "stability": 40,
            "cracks": [],
            "last_tested_tick": None,
            "note": "0-100. A cracked Dao heart blocks breakthrough regardless of Qi."
        },
        "soul_state": "none",
        "tribulation_debt": 0,
        "_xianxia_schema": 1,
        "initiation_lines": lines,
        "daily_schedule": schedule,
        "sect_tasks": tasks
    }
    if max_hp is not None:
        d["max_hp"] = max_hp
    return d

# --- Governor Zhao Ming: Political governor, Foundation Establishment ---
# Formal, cautious, defers to Teng family. Manages administration from governor mansion.
# Canon: INFERRED. A country's capital must have a governor. The Teng family controls
# Zhao Country politically, so the governor would be their appointee or puppet.
gov_zhao_ming = make_npc(
    npc_id="npc_gov_zhao_ming",
    name="Governor Zhao Ming",
    name_cn="\u8d75\u660e\u592a\u5b88",
    canon_id="INF-GWC-01",
    npc_type="governor",
    faction="zhao_country_government",
    location="great_wang_capital",
    cultivation="Foundation Establishment (mid-stage)",
    personality="cautious, formal, politically astute; defers to Teng family authority",
    speech="measured, diplomatic",
    relationship="INFERRED — indirect; governor of the country where Wang Lin was born",
    lines=[
        "Welcome to the Great Wang Capital. I am Governor Zhao Ming. State your business — the capital has rules that even cultivators must follow.",
        "The Teng family's authority extends to every corner of Zhao Country. My role is to ensure order in their absence. I suggest you do not test that order.",
        "The governor mansion's treasury is well-stocked. Do not mistake my hospitality for weakness. The last thief lost more than his hands.",
        "Cultivators from the sects occasionally visit the capital on official business. If you are one of them, present your sect token. If not... the market district is that way."
    ],
    schedule=[
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 8},
        {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},
        {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 12},
        {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    tasks=[
        {
            "id": "tax_collection",
            "description": "Governor Zhao Ming adjusts his official robes: 'The outer provinces are due for their seasonal tax. Bring me 16 bundles of wheat as proof of collection. The capital's granaries must be filled before winter.'",
            "requires": [{"item": "minecraft:wheat", "count": 16}],
            "reward_item": "minecraft:emerald",
            "reward_count": 6
        },
        {
            "id": "intelligence_report",
            "description": "Governor Zhao Ming leans closer: 'I need a report compiled. Bring me paper and ink. The Teng family expects regular updates on the capital's affairs — and I intend to provide them.'",
            "requires": [
                {"item": "minecraft:paper", "count": 8},
                {"item": "minecraft:ink_sac", "count": 4}
            ],
            "reward_item": "minecraft:gold_ingot",
            "reward_count": 3
        }
    ],
    max_hp=200
)

# --- Guard Captain Lin: City guard commander, Qi Condensation 9 ---
# Military, alert, suspicious of strangers. Patrols from city gate through districts.
# Canon: INFERRED. A capital city must have a guard force. The city_gate district
# exists specifically for military defense.
guard_captain_lin = make_npc(
    npc_id="npc_guard_captain_lin",
    name="Guard Captain Lin",
    name_cn="\u6797\u961f\u957f",
    canon_id="INF-GWC-02",
    npc_type="guard_captain",
    faction="zhao_country_government",
    location="great_wang_capital",
    cultivation="Qi Condensation 9 (peak)",
    personality="alert, suspicious, dutiful; takes security seriously",
    speech="gruff, military",
    relationship="INFERRED — neutral; would treat Wang Lin as another traveler unless provoked",
    lines=[
        "Halt. The Great Wang Capital does not admit armed strangers without inspection. State your name and purpose.",
        "The smuggler tunnels beneath the city are a persistent problem. My guards find new entrances every month. If you see anything suspicious, report it immediately.",
        "The capital has not seen open conflict in years. But that is because the Teng family's reputation keeps the ambitious in check. Do not test that peace.",
        "My guards patrol from the city gate to the port docks. If you are caught stealing from the warehouse district, I will personally escort you to the governor's dungeon."
    ],
    schedule=[
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 20},
        {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 25},
        {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 20},
        {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    tasks=[
        {
            "id": "patrol_supplies",
            "description": "Guard Captain Lin barks: 'My men need supplies. Iron for weapons, arrows for the watchtowers. The capital's safety is not free — bring me materials and I will pay from the guard treasury.'",
            "requires": [
                {"item": "minecraft:iron_ingot", "count": 8},
                {"item": "minecraft:arrow", "count": 32}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 5
        },
        {
            "id": "smuggler_intel",
            "description": "Guard Captain Lin scowls: 'The smugglers are getting bolder. If you can gather information — glass bottles for evidence collection, string for binding suspects — I will reward you. The governor wants those tunnels sealed.'",
            "requires": [
                {"item": "minecraft:glass_bottle", "count": 4},
                {"item": "minecraft:string", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 4
        }
    ],
    max_hp=120
)

# --- Priestess Yun: Temple spiritual figure, Qi Condensation 6 ---
# Serene, spiritual, offers guidance. Stays in temple district mostly.
# Canon: INFERRED. The capital has a 52KB temple district — it must have a priest/priestess.
# Zhao Country is a cultivation nation; the temple would serve spiritual functions.
priestess_yun = make_npc(
    npc_id="npc_priestess_yun",
    name="Priestess Yun",
    name_cn="\u4e91\u796d\u53f8",
    canon_id="INF-GWC-03",
    npc_type="priestess",
    faction="great_wang_temple",
    location="great_wang_capital",
    cultivation="Qi Condensation 6 (mid-stage)",
    personality="serene, compassionate, spiritually devoted; offers counsel to those who seek it",
    speech="gentle, measured",
    relationship="INFERRED — benevolent; would offer guidance to any cultivator who seeks it",
    lines=[
        "Welcome, traveler. The temple district offers solace from the capital's noise. Light incense if you wish — the ancestors listen to those who are sincere.",
        "Cultivation is not merely about power. It is about understanding. The temple preserves ancient teachings that even the sects have forgotten.",
        "Many come to the capital seeking wealth or power. Few come seeking peace. I appreciate your visit. Stay as long as you like.",
        "The offerings here are modest, but the ancestors do not judge by gold. They judge by the heart. Bring wheat for the poor, and the temple will bless your journey."
    ],
    schedule=[
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 10},
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "wandering", "dir": "west", "dist": 8},
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "north", "dist": 12},
        {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    tasks=[
        {
            "id": "temple_offerings",
            "description": "Priestess Yun smiles gently: 'The temple needs offerings for the ancestral shrine. Gold nuggets for the altar, wheat for the poor. The ancestors will remember your generosity.'",
            "requires": [
                {"item": "minecraft:gold_nugget", "count": 6},
                {"item": "minecraft:wheat", "count": 16}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 5
        },
        {
            "id": "incense_materials",
            "description": "Priestess Yun bows: 'We are running low on incense materials. Coal for the braziers, paper for prayer slips. The temple's peace depends on these humble supplies.'",
            "requires": [
                {"item": "minecraft:coal", "count": 16},
                {"item": "minecraft:paper", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 4
        }
    ],
    max_hp=80
)

# --- Merchant Hong: Market district trader, mortal ---
# Shrewd, business-minded, knows trade routes. Moves between market and port.
# Canon: INFERRED. The capital has a market district and port — there must be merchants.
# In a cultivation world, even mortal merchants handle goods that cultivators need.
merchant_hong = make_npc(
    npc_id="npc_merchant_hong",
    name="Merchant Hong",
    name_cn="\u6d2a\u5546\u4eba",
    canon_id="INF-GWC-04",
    npc_type="merchant",
    faction="great_wang_merchants_guild",
    location="great_wang_capital",
    cultivation="Mortal (non-cultivator)",
    personality="shrewd, opportunistic, well-informed; knows every price in the capital",
    speech="fast, persuasive",
    relationship="INFERRED — neutral; sees everyone as a potential customer",
    lines=[
        "Ah, a customer! Welcome to the Great Wang Capital market. I have goods from every province — iron from the northern mines, wheat from the southern farmlands, and rare items from the port.",
        "The port docks see ships from distant lands. Well, distant provinces at least. If you need something specific, ask — I can get almost anything. For a price.",
        "The Teng family's merchants get the best stalls. Those of us without cultivation backing make do with what remains. But I have connections. Do not underestimate a mortal with a network.",
        "Gold and emeralds talk in this city. If you have materials to sell, I will give you fair prices. Fairer than the Teng family's buyers, at least."
    ],
    schedule=[
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 4000, "act": "wandering", "dir": "south", "dist": 18},
        {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 22},
        {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 15},
        {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    tasks=[
        {
            "id": "trade_goods",
            "description": "Merchant Hong rubs his hands: 'I have a buyer in the cultivator quarter who needs iron and emeralds. Bring me the materials and I will pay you in gold — minus my commission, of course.'",
            "requires": [
                {"item": "minecraft:iron_ingot", "count": 8},
                {"item": "minecraft:emerald", "count": 4}
            ],
            "reward_item": "minecraft:gold_ingot",
            "reward_count": 4
        },
        {
            "id": "rare_herbs",
            "description": "Merchant Hong whispers: 'A cultivator in the quarter is willing to pay well for mundane herbs with alchemical potential. Wheat, carrots, and coal for the furnace. Bring me these and I will handle the rest.'",
            "requires": [
                {"item": "minecraft:wheat", "count": 16},
                {"item": "minecraft:carrot", "count": 8},
                {"item": "minecraft:coal", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 6
        }
    ],
    max_hp=20
)

# --- Tavern Keeper Old Zhou: Information hub, mortal ---
# Gossipy, welcoming, knows rumors. Stays in tavern district mostly.
# Canon: INFERRED. Taverns in xianxia are information hubs (standard genre convention +
# the capital has a dedicated tavern district). Old Zhou is the eyes and ears.
tavern_keeper_zhou = make_npc(
    npc_id="npc_tavern_keeper_zhou",
    name="Tavern Keeper Old Zhou",
    name_cn="\u8001\u5468\u9152\u9986",
    canon_id="INF-GWC-05",
    npc_type="tavern_keeper",
    faction="none",
    location="great_wang_capital",
    cultivation="Mortal (non-cultivator, but well-informed)",
    personality="gossipy, welcoming, shrewd beneath cheerfulness; hears everything",
    speech="warm, chatty",
    relationship="INFERRED — friendly; information is his currency",
    lines=[
        "Come in, come in! Sit down. The capital's finest tavern — well, the only one that doesn't water the drinks. What'll you have?",
        "You hear things in a tavern. Merchants complain about taxes, guards grumble about patrols, cultivators whisper about sect politics. I remember it all. Information is more valuable than gold, you know.",
        "The smuggler tunnels? Ha! Everyone knows they exist. The guards pretend not to notice because half of them drink here. But I didn't tell you that.",
        "A piece of advice, traveler: do not anger the Teng family in this city. They control everything — the market, the port, the governor. Even the temple answers to them, indirectly. Keep your head down and your drink full."
    ],
    schedule=[
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "north", "dist": 8},
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "wandering", "dir": "south", "dist": 10},
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 13000, "act": "wandering", "dir": "east", "dist": 12},
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    tasks=[
        {
            "id": "food_supplies",
            "description": "Old Zhou wipes the bar: 'Business is good but the kitchen is running low. Bring me wheat for bread and beef for the stew. I will pay in emeralds — and a free meal.'",
            "requires": [
                {"item": "minecraft:wheat", "count": 16},
                {"item": "minecraft:cooked_beef", "count": 4}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 4
        },
        {
            "id": "rumor_network",
            "description": "Old Zhou leans in: 'I trade in information, not just drinks. Bring me paper for recording rumors and glass bottles for... shall we say, 'verification.' I will share what I hear about the capital's power struggles.'",
            "requires": [
                {"item": "minecraft:paper", "count": 8},
                {"item": "minecraft:glass_bottle", "count": 4}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 5
        }
    ],
    max_hp=20
)

ALL_NPCS = [gov_zhao_ming, guard_captain_lin, priestess_yun, merchant_hong, tavern_keeper_zhou]

def main():
    print("=== AUTO-CANON-024: Creating 5 INFERRED NPCs for Great Wang Capital ===\n")

    # Create NPC JSON files
    filenames = []
    for npc in ALL_NPCS:
        fname = npc["npc_id"] + ".json"
        fpath = os.path.join(BASE, fname)
        with open(fpath, "w") as f:
            json.dump(npc, f, indent=2)
            f.write("\n")
        filenames.append(fname)
        lines = len(npc["initiation_lines"])
        sched = len(npc["daily_schedule"])
        tasks = len(npc["sect_tasks"])
        print(f"  Created {fname}: {npc['name']} ({npc['cultivation']})")
        print(f"    {lines} initiation lines, {sched} schedule entries, {tasks} tasks")

    # Update _index.json
    index_path = os.path.join(BASE, "_index.json")
    with open(index_path, "r") as f:
        index = json.load(f)

    existing = set(index)
    added = []
    for fname in filenames:
        if fname not in existing:
            index.append(fname)
            added.append(fname)

    # Sort for cleanliness
    index.sort()
    with open(index_path, "w") as f:
        json.dump(index, f, indent=2)
        f.write("\n")

    print(f"\n  Updated _index.json: added {len(added)} entries")
    for a in added:
        print(f"    + {a}")

    # Verify
    print("\n=== Verification ===")
    with open(index_path, "r") as f:
        index_check = json.load(f)
    for fname in filenames:
        assert fname in index_check, f"MISSING from _index.json: {fname}"
        fpath = os.path.join(BASE, fname)
        with open(fpath, "r") as f:
            data = json.load(f)
        assert "initiation_lines" in data, f"{fname} missing initiation_lines"
        assert "daily_schedule" in data, f"{fname} missing daily_schedule"
        assert "sect_tasks" in data, f"{fname} missing sect_tasks"
        assert data["location"] == "great_wang_capital", f"{fname} wrong location"
        print(f"  OK: {data['name']} — {len(data['initiation_lines'])}L, "
              f"{len(data['daily_schedule'])}S, {len(data['sect_tasks'])}T, "
              f"loc={data['location']}")

    total_lines = sum(len(n["initiation_lines"]) for n in ALL_NPCS)
    total_sched = sum(len(n["daily_schedule"]) for n in ALL_NPCS)
    total_tasks = sum(len(n["sect_tasks"]) for n in ALL_NPCS)
    print(f"\nTotal: {total_lines} initiation lines, {total_sched} schedule entries, "
          f"{total_tasks} tasks across 5 NPCs")
    print("\n=== NpcSpawnRegistry.java must be updated manually ===")
    print("Add these 5 registrations under 'great_wang_dynasty':")
    for npc in ALL_NPCS:
        print(f'  register("great_wang_dynasty", "{npc["npc_id"]}");')

if __name__ == "__main__":
    main()