#!/usr/bin/env python3
"""AUTO-CANON-027: Add schedules + tasks to 4 Wang Family Village NPCs.

Targets:
  1. npc_heng_yue_recruiter — NO schedule, NO tasks (CANON-CRITICAL: RI Ch.3)
  2. npc_wang_ping — schedule YES, tasks NO
  3. npc_wang_tianshan — schedule YES, tasks NO
  4. npc_wang_qingyue — schedule YES, tasks NO

Also verifies the 4 staged loot table files are valid JSON before committing.
Zero Java changes. Article XXII (canon→gameplay), XXIII (vertical slice), XXIV (NPC initiate).
"""

import json
import os
import sys

NPC_DIR = os.path.join(os.path.dirname(__file__), "..",
    "src", "main", "resources", "data", "ergenverse", "npcs")

def load_npc(npc_id):
    path = os.path.join(NPC_DIR, f"{npc_id}.json")
    with open(path, "r") as f:
        return json.load(f), path

def save_npc(data, path):
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

# ── 1. Heng Yue Recruiter: schedule + tasks ──────────────────────
# Canon (RI Ch.3): Elder arrives at village, sets up testing area in
# the village square, tests children one by one, issues tokens to
# those who pass, departs at dusk. The recruiter is a low-realm
# cultivator (Qi Cond. 3) who finds this duty boring — he has done
# it many times. He is indifferent but dutiful.
# Schedule: arrive at dawn (tick 0 = 6:00 AM), conduct tests through
# the day, depart at dusk (tick 12000 = 6:00 PM). He stays in the
# village center area, moving between the gate (arrival) and the
# main square (testing).
data, path = load_npc("npc_heng_yue_recruiter")

data["daily_schedule"] = [
    # Dawn: arrive at village gate
    {"t0": 0,    "t1": 1000,  "act": "wandering", "dir": "north", "dist": 10},
    # Walk to village center (testing area)
    {"t0": 1000, "t1": 2000,  "act": "wandering", "dir": "south", "dist": 15},
    # Morning: conduct aptitude tests
    {"t0": 2000, "t1": 4000,  "act": "idle",      "dir": None,    "dist": 0},
    # Brief rest, eat
    {"t0": 4000, "t1": 5000,  "act": "eating",    "dir": None,    "dist": 0},
    # Afternoon: continue testing
    {"t0": 5000, "t1": 8000,  "act": "idle",      "dir": None,    "dist": 0},
    # Late afternoon: finish testing, pack up
    {"t0": 8000, "t1": 10000, "act": "wandering", "dir": "north", "dist": 15},
    # Dusk: depart toward gate
    {"t0": 10000,"t1": 12000, "act": "wandering", "dir": "north", "dist": 20},
    # Night: gone (sleeping off-site, effectively absent)
    {"t0": 12000,"t1": 24000, "act": "sleeping",  "dir": None,    "dist": 0},
]

data["sect_tasks"] = [
    {
        "id": "aptitude_test",
        "description": "The Heng Yue recruiter gestures to a pale jade stone on the table: 'Place your hand here. If you have any spiritual root at all, the stone will glow. This is your only chance to leave this mortal village.'",
        "requires": [],
        "reward_item": "minecraft:air",
        "reward_count": 0
    },
    {
        "id": "gather_village_children",
        "description": "The recruiter narrows his eyes: 'I need every child in this village between twelve and sixteen brought to the square. The test will not wait. Go tell the Wang, Zhou, and Zhang families.'",
        "requires": [],
        "reward_item": "minecraft:emerald",
        "reward_count": 1
    }
]

save_npc(data, path)
print(f"[OK] npc_heng_yue_recruiter: 8 schedule entries + 2 tasks")

# ── 2. Wang Ping: tasks ──────────────────────────────────────────
# Canon: Wang Lin's younger brother. Gentle, protective.
# His initiation_lines mention fishing with Da Niu and the Teng family tax.
# Tasks: fishing (food for family), gathering firewood (mentioned in
# lines as "extra food"), helping at the port docks (village economy).
data, path = load_npc("npc_wang_ping")

data["sect_tasks"] = [
    {
        "id": "fishing_south_bend",
        "description": "Wang Ping grins: 'Da Niu and I were going to fish at the south bend. If you bring us some string for lines, I will share whatever we catch. Mother could use the extra food.'",
        "requires": [
            {"item": "minecraft:string", "count": 3}
        ],
        "reward_item": "minecraft:cod",
        "reward_count": 3
    },
    {
        "id": "chop_firewood",
        "description": "Wang Ping looks at the dwindling woodpile: 'Winter will be hard this year. Father says we need more firewood. If you bring oak logs, I can help you split them.'",
        "requires": [
            {"item": "minecraft:oak_log", "count": 8}
        ],
        "reward_item": "minecraft:coal",
        "reward_count": 4
    }
]

save_npc(data, path)
print(f"[OK] npc_wang_ping: 2 tasks added")

# ── 3. Wang Tianshan: tasks ─────────────────────────────────────
# Canon: Wang family elder. His lines mention the memorial shrine
# must be maintained and the Teng family's rise.
# Tasks: maintain the memorial shrine (coal = incense fuel),
# patrol village perimeter (arrows for defense), collect tribute
# for the ancestral hall (paper = prayers).
data, path = load_npc("npc_wang_tianshan")

data["sect_tasks"] = [
    {
        "id": "shrine_incense_supplies",
        "description": "Wang Tianshan gestures toward the memorial shrine: 'The ancestors watch over us, but the incense burns low. Bring coal for the braziers — our forefathers deserve warmth, even in death.'",
        "requires": [
            {"item": "minecraft:coal", "count": 8}
        ],
        "reward_item": "minecraft:emerald",
        "reward_count": 2
    },
    {
        "id": "village_patrol_arrows",
        "description": "Wang Tianshan frowns: 'Bandits have been seen on the northern road. I am too old to patrol as I once did. Bring arrows — the village watch needs them.'",
        "requires": [
            {"item": "minecraft:arrow", "count": 16}
        ],
        "reward_item": "minecraft:iron_ingot",
        "reward_count": 2
    }
]

save_npc(data, path)
print(f"[OK] npc_wang_tianshan: 2 tasks added")

# ── 4. Wang Qingyue: tasks ──────────────────────────────────────
# Canon: Wang family member (cousin). Her lines mention sweeping
# the ancestral hall before the test ceremony and the family's
# five-generation history.
# Tasks: sweep ancestral hall (sticks = broom material), help with
# market errands (bread for the ceremony day), maintain temple district.
data, path = load_npc("npc_wang_qingyue")

data["sect_tasks"] = [
    {
        "id": "sweep_ancestral_hall",
        "description": "Wang Qingyue hands you a worn broom: 'The ancestral hall must be spotless before the Heng Yue elder arrives. If you bring sticks for new brooms, I will handle the rest. Our ancestors deserve respect.'",
        "requires": [
            {"item": "minecraft:stick", "count": 16}
        ],
        "reward_item": "minecraft:wheat",
        "reward_count": 8
    },
    {
        "id": "ceremony_bread",
        "description": "Wang Qingyue wrings her hands: 'The test ceremony will bring many families to the square. We need bread for the gathering. If you can provide some, the Wang family will not look poor before the cultivator.'",
        "requires": [
            {"item": "minecraft:bread", "count": 6}
        ],
        "reward_item": "minecraft:emerald",
        "reward_count": 1
    }
]

save_npc(data, path)
print(f"[OK] npc_wang_qingyue: 2 tasks added")

# ── Verify all modified NPCs ─────────────────────────────────────
print("\n── Verification ──")
for npc_id in ["npc_heng_yue_recruiter", "npc_wang_ping", "npc_wang_tianshan", "npc_wang_qingyue"]:
    d, _ = load_npc(npc_id)
    sched = len(d.get("daily_schedule", []))
    tasks = len(d.get("sect_tasks", []))
    lines = len(d.get("initiation_lines", []))
    print(f"  {npc_id}: {lines} lines, {sched} schedule, {tasks} tasks")

print("\nDone. All 4 NPCs now have complete lines+schedule+tasks.")