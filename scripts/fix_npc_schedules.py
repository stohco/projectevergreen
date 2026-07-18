#!/usr/bin/env python3
"""AUTO-CANON-014: Fix Heng Yue NPC schedule diversity, soul_state bugs, and night_lines.

Problems found in canon audit:
1. soul_state "nascent_soul" on 8 NPCs who are NOT Ancient God cultivators (should be "none")
2. 6 NPCs share IDENTICAL schedules (Article XXI violation)
3. 6 non-patrol NPCs patrol at 23000-24000 (only Zhang Tian + Sun Zhenwei should)
4. Sun Zhenwei patrols at night but has no night_lines

Fixes:
- Unique personality-driven schedules for each NPC
- soul_state: "none" for all non-Ancient-God NPCs
- night_lines added to Sun Zhenwei
- No more random disciples on night patrol
"""

import json
import os

NPCS_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

# MC day: 0=dawn, 6000=noon, 12000=dusk, 18000=midnight, 24000=dawn

SCHEDULES = {
    # Qian Kun - practical, resourceful, trader personality
    # Currently has NO schedule. Adding one.
    "npc_qian_kun": [
        {"t0": 0, "t1": 2000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "studying", "dir": "west", "dist": 20},    # Library - research trade routes
        {"t0": 5000, "t1": 7000, "act": "wandering", "dir": "south", "dist": 25},   # Check market/herb sources
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 10000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "east", "dist": 20},  # Afternoon trade business
        {"t0": 12000, "t1": 13000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 13000, "t1": 15000, "act": "studying", "dir": "west", "dist": 20},   # Evening study
        {"t0": 15000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Wang Zhuo - proud aristocratic clan descendant, rival
    # Should NOT patrol. Sleeps in slightly, focuses on cultivation and showing off.
    "npc_wang_zhuo": [
        {"t0": 0, "t1": 1000, "act": "cultivating", "dir": "north", "dist": 25},   # Late night cultivation
        {"t0": 1000, "t1": 3000, "act": "sleeping", "dir": "south", "dist": 10},   # Sleeps in
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 6000, "act": "sparring", "dir": "east", "dist": 30},    # Morning sparring - show off
        {"t0": 6000, "t1": 8000, "act": "cultivating", "dir": "north", "dist": 25}, # Cultivate at peak
        {"t0": 8000, "t1": 10000, "act": "studying", "dir": "west", "dist": 20},   # Library - clan techniques
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 15000, "act": "cultivating", "dir": "north", "dist": 20}, # Afternoon cultivation
        {"t0": 15000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Yun Fei - quiet, studious female disciple
    # Should NOT patrol. Studies at library, attends lectures, cultivates.
    "npc_yun_fei": [
        {"t0": 0, "t1": 2000, "act": "studying", "dir": "west", "dist": 20},      # Library at dawn (quietest)
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "cultivating", "dir": "north", "dist": 15}, # Morning cultivation
        {"t0": 5000, "t1": 7000, "act": "studying", "dir": "west", "dist": 20},    # More study
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 10000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 14000, "act": "studying", "dir": "west", "dist": 20},   # Evening study
        {"t0": 14000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Zhou Lin - measured, mentions sect trial and spirit veins
    # Should NOT patrol. Cultivates at peak (strongest veins), trains at trial grounds.
    "npc_zhou_lin": [
        {"t0": 0, "t1": 3000, "act": "cultivating", "dir": "north", "dist": 25},   # Dawn cultivation (spirit veins strongest)
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "sparring", "dir": "south", "dist": 30},   # Train at trial grounds
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 10000, "act": "cultivating", "dir": "north", "dist": 25},
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 14000, "act": "wandering", "dir": "east", "dist": 20},  # Walk the sect, observe
        {"t0": 14000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Sun Zhenwei - disciplinarian, patrols
    # KEEP patrol role. ADD night_lines. Make schedule patrol-focused.
    "npc_sun_zhenwei": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "sparring", "dir": "east", "dist": 25},    # Morning drill
        {"t0": 5000, "t1": 7000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 10000, "act": "wandering", "dir": "south", "dist": 20}, # Check on disciples
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 14000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 14000, "t1": 16000, "act": "sleeping", "dir": None, "dist": 0},      # Brief rest before patrol
        {"t0": 16000, "t1": 24000, "act": "patrolling", "dir": "east", "dist": 25}, # Long night patrol
    ],

    # Zhang Hu - bold, tiger-like, mentions beasts and food
    # Should NOT patrol. Spars aggressively, tends beast pens.
    "npc_zhang_hu": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},          # Eats early
        {"t0": 3000, "t1": 6000, "act": "sparring", "dir": "east", "dist": 30},    # Long morning sparring
        {"t0": 6000, "t1": 8000, "act": "wandering", "dir": "south", "dist": 25},  # Check beast pens
        {"t0": 8000, "t1": 10000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 15000, "act": "sparring", "dir": "east", "dist": 30},   # Afternoon sparring
        {"t0": 15000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Wang Hao - boisterous, friendly, mentions food hall
    # Should NOT patrol. Social, eats early, wanders, spars casually.
    "npc_wang_hao": [
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 2000, "act": "eating", "dir": None, "dist": 0},         # RUSH to food hall at dawn
        {"t0": 2000, "t1": 4000, "act": "sparring", "dir": "east", "dist": 25},    # Morning spar
        {"t0": 4000, "t1": 6000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 6000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 25},  # Social wandering, chatting
        {"t0": 12000, "t1": 14000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 14000, "t1": 16000, "act": "cultivating", "dir": "north", "dist": 15},
        {"t0": 16000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Ye Zi - elder, formal, mentions restriction formations
    # Should NOT patrol. Cultivates, inspects formations, holds audience.
    "npc_ye_zi": [
        {"t0": 0, "t1": 3000, "act": "cultivating", "dir": "north", "dist": 20},   # Deep night cultivation
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "wandering", "dir": "east", "dist": 30},   # Inspect formations
        {"t0": 7000, "t1": 9000, "act": "cultivating", "dir": "north", "dist": 20},
        {"t0": 9000, "t1": 10000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 10000, "t1": 13000, "act": "wandering", "dir": "south", "dist": 25}, # Hold audience / inspect
        {"t0": 13000, "t1": 15000, "act": "cultivating", "dir": "north", "dist": 20},
        {"t0": 15000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10},
    ],

    # Sun Dazhu - bullying senior disciple
    # Should NOT patrol. Sleeps in, claims training area, bullies, minimal cultivation.
    "npc_sun_dazhu": [
        {"t0": 0, "t1": 3000, "act": "sleeping", "dir": None, "dist": 0},           # Sleeps in late
        {"t0": 3000, "t1": 4000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 4000, "t1": 7000, "act": "sparring", "dir": "east", "dist": 25},    # Claims training area
        {"t0": 7000, "t1": 9000, "act": "wandering", "dir": "south", "dist": 20},  # Look for juniors to bully
        {"t0": 9000, "t1": 10000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 10000, "t1": 13000, "act": "cultivating", "dir": "north", "dist": 15}, # Lazy cultivation
        {"t0": 13000, "t1": 14000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 14000, "t1": 24000, "act": "sleeping", "dir": "south", "dist": 10}, # Sleeps early
    ],
}

# Night lines for Sun Zhenwei (disciplinarian who patrols)
NIGHT_LINES = {
    "npc_sun_zhenwei": [
        "Halt. Identify yourself. The sect perimeter is not a place for midnight strolls.",
        "You are out past curfew. I will not ask twice — return to the disciple dormitories.",
        "If I find you wandering near the restricted buildings again, you will answer to Elder Zhang Tian.",
        "The night patrol reports directly to the Sect Master. Move along, disciple.",
    ],
}

# NPCs whose soul_state should be "none" (not Ancient God cultivators)
SOUL_STATE_FIXES = [
    "npc_wang_zhuo",
    "npc_zhang_hu",
    "npc_sun_dazhu",
    "npc_sun_zhenwei",
    "npc_qiu_siping",
    "npc_chen_bailiang",
    "npc_ye_zi",
    "npc_wu_yu",
]

# Wu Yu and Chen Bailiang: remove their 23000-24000 patrol block (elders shouldn't patrol)
# They have unique schedules otherwise - just trim the patrol
PATROL_REMOVAL = [
    "npc_chen_bailiang",  # Remove last block (23000-24000 patrol)
    "npc_wu_yu",          # Remove last block (23000-24000 patrol)
]


def main():
    changed_files = []

    for npc_id, schedule in SCHEDULES.items():
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        if not os.path.exists(path):
            print(f"WARNING: {path} not found, skipping")
            continue
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        data["daily_schedule"] = schedule
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')
        changed_files.append(npc_id)
        print(f"  Schedule updated: {npc_id} ({len(schedule)} blocks)")

    for npc_id, lines in NIGHT_LINES.items():
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        data["night_lines"] = lines
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')
        changed_files.append(npc_id)
        print(f"  Night lines added: {npc_id} ({len(lines)} lines)")

    for npc_id in SOUL_STATE_FIXES:
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        old = data.get("soul_state", "?")
        if old != "none":
            data["soul_state"] = "none"
            with open(path, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write('\n')
            changed_files.append(npc_id)
            print(f"  Soul state fixed: {npc_id} ({old} -> none)")
        else:
            print(f"  Soul state OK: {npc_id} (already none)")

    for npc_id in PATROL_REMOVAL:
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        sched = data.get("daily_schedule", [])
        if sched and sched[-1].get("act") == "patrolling":
            removed = sched.pop()
            data["daily_schedule"] = sched
            with open(path, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
                f.write('\n')
            changed_files.append(npc_id)
            print(f"  Patrol removed: {npc_id} (was {removed['t0']}-{removed['t1']} {removed['dir']})")
        else:
            print(f"  No patrol to remove: {npc_id}")

    unique_files = set(changed_files)
    print(f"\nTotal files changed: {len(unique_files)}")
    for f in sorted(unique_files):
        print(f"  - {f}")


if __name__ == "__main__":
    main()