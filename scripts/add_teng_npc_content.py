#!/usr/bin/env python3
"""AUTO-CANON-020: Add initiation_lines, daily_schedule, and sect_tasks to 3 Teng Family City NPCs.

Canon context (RI Ch.1-10):
- Teng Huayuan (N83): Half-Step Deity Transformation patriarch, destroyed Wang family,
  cold/commanding, most powerful being in Zhao Country. Rules from governor mansion.
- Teng Li (N84): Late Foundation Establishment, arrogant young master, bullying,
  sneering. Early antagonist Wang Lin defeats. Patrols/train in cultivator quarter.
- Teng Xiuxiu (N15): Low-tier cultivator, conflicted loyalties, guarded speech.
  Not hostile despite enemy family. INFERRED from canon context.

Per Article XXIV: NPCs must initiate gameplay. Currently all 3 are silent mannequins.
Per Article XXI: World is main character — NPCs need schedules to feel alive.
Per Article XXVI: Use existing systems (NpcSectMissionGoal for tasks, NpcScheduleGoal for schedules).
"""

import json
import os

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                     "src/main/resources/data/ergenverse/npcs")

# --- Teng Huayuan: Patriarch, Half-Step Deity Transformation ---
# Cold, commanding, few words. References Teng family dominance, contempt for weak.
# Spends time in governor mansion, occasionally inspects city.
teng_huayuan = {
    "initiation_lines": [
        "The Teng family has ruled Zhao Country for three generations. Do not mistake our patience for weakness.",
        "The villages send their tribute on time. If they do not... I make an example.",
        "Heng Yue Sect? A minor sect clinging to the mountains. Their elders know better than to cross my path.",
        "You carry the stench of a weak cultivator. State your business before I lose interest."
    ],
    "daily_schedule": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 10},
        {"t0": 4000, "t1": 5000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 5000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 15},
        {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 12},
        {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    "sect_tasks": [
        {
            "id": "tribute_collection",
            "description": "Teng Huayuan's cold gaze falls on you: 'The outer villages are late with their grain tribute. Go to the warehouse and bring me proof of collection — 16 wheat bundles. You will be compensated.'",
            "requires": [{"item": "minecraft:wheat", "count": 16}],
            "reward_item": "minecraft:emerald",
            "reward_count": 5
        },
        {
            "id": "family_weaponry",
            "description": "Teng Huayuan waves dismissively: 'The family guard needs new equipment. Bring iron ingots and arrows. Do not waste my time.'",
            "requires": [
                {"item": "minecraft:iron_ingot", "count": 8},
                {"item": "minecraft:arrow", "count": 16}
            ],
            "reward_item": "minecraft:gold_ingot",
            "reward_count": 3
        }
    ]
}

# --- Teng Li: Arrogant young master, Late Foundation Establishment ---
# Sneering, haughty, references his cultivation as superior, bullies weaker beings.
# Trains in cultivator quarter, patrols city looking for targets of amusement.
teng_li = {
    "initiation_lines": [
        "Foundation Establishment. Something you will never achieve, standing there gaping like a fish.",
        "The Teng family does not speak to peasants. But since you are here — make yourself useful or leave.",
        "My cousin speaks softly to everyone. I prefer honesty. You are weak. Accept it.",
        "There is a village nearby where the mortals think they can hide their grain from us. Amusing, isn't it?"
    ],
    "daily_schedule": [
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 20},
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 6000, "act": "wandering", "dir": "south", "dist": 25},
        {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 7000, "t1": 9000, "act": "wandering", "dir": "west", "dist": 20},
        {"t0": 9000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    "sect_tasks": [
        {
            "id": "training_supplies",
            "description": "Teng Li smirks: 'I am refining my Foundation Establishment. Bring me coal for the forge and iron for talismans. Do it quickly.'",
            "requires": [
                {"item": "minecraft:coal", "count": 16},
                {"item": "minecraft:iron_ingot", "count": 4}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 4
        },
        {
            "id": "humiliation_collection",
            "description": "Teng Li laughs cruelly: 'Those villages think they can resist. Bring me their 'protection payment' — emeralds and gold. Show them what the Teng family means.'",
            "requires": [
                {"item": "minecraft:emerald", "count": 3},
                {"item": "minecraft:gold_ingot", "count": 2}
            ],
            "reward_item": "minecraft:iron_sword",
            "reward_count": 1
        }
    ]
}

# --- Teng Xiuxiu: Conflicted Teng family member, low-tier cultivator ---
# Guarded, speaks carefully, hints at discomfort with family's cruelty.
# INFERRED: spends time in temple district (spiritual contemplation) and market.
teng_xiuxiu = {
    "initiation_lines": [
        "...You are not from the city. Be careful here. Not everyone in the Teng family speaks before they act.",
        "The temple district is quiet at this hour. I go there to think. The ancestors... they were not always like this.",
        "My cousin Teng Li enjoys his power too much. But the patriarch's word is absolute. I cannot interfere.",
        "If you need supplies, the market district is to the south. The merchants there do not ask questions."
    ],
    "daily_schedule": [
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 18},
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "wandering", "dir": "north", "dist": 15},
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "west", "dist": 20},
        {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0}
    ],
    "sect_tasks": [
        {
            "id": "herb_request",
            "description": "Teng Xiuxiu lowers her voice: 'I need herbs for a poultice. Not for the family — for the mortal quarter. They have no healer. Bring me wheat and carrots and I will make it worth your while.'",
            "requires": [
                {"item": "minecraft:wheat", "count": 8},
                {"item": "minecraft:carrot", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 3
        },
        {
            "id": "information_trade",
            "description": "Teng Xiuxiu glances around nervously: 'If you bring me paper and ink, I can write down something useful — the patrol schedule of the city guard. Some people would pay good emeralds for that.'",
            "requires": [
                {"item": "minecraft:paper", "count": 4},
                {"item": "minecraft:ink_sac", "count": 2}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 6
        }
    ]
}

def update_npc(filename, updates):
    path = os.path.join(BASE, filename)
    with open(path, "r") as f:
        data = json.load(f)
    
    changed = []
    for key, value in updates.items():
        if key in data:
            print(f"  WARN: {filename} already has '{key}', overwriting")
        data[key] = value
        changed.append(key)
    
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    
    print(f"  Updated {filename}: added {', '.join(changed)}")

def main():
    print("=== AUTO-CANON-020: Adding lines, schedules, tasks to Teng Family City NPCs ===\n")
    
    update_npc("npc_teng_huayuan.json", teng_huayuan)
    update_npc("npc_teng_li.json", teng_li)
    update_npc("npc_teng_xiuxiu.json", teng_xiuxiu)
    
    # Verify
    print("\n=== Verification ===")
    for fname in ["npc_teng_huayuan.json", "npc_teng_li.json", "npc_teng_xiuxiu.json"]:
        path = os.path.join(BASE, fname)
        with open(path, "r") as f:
            data = json.load(f)
        lines = len(data.get("initiation_lines", []))
        schedule = len(data.get("daily_schedule", []))
        tasks = len(data.get("sect_tasks", []))
        print(f"  {data['name']}: {lines} lines, {schedule} schedule entries, {tasks} tasks")
    
    total_lines = sum(len(v["initiation_lines"]) for v in [teng_huayuan, teng_li, teng_xiuxiu])
    total_tasks = sum(len(v["sect_tasks"]) for v in [teng_huayuan, teng_li, teng_xiuxiu])
    print(f"\nTotal: {total_lines} initiation lines, {total_tasks} tasks across 3 NPCs")

if __name__ == "__main__":
    main()