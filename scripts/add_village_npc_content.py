#!/usr/bin/env python3
"""AUTO-CANON-015: Add initiation_lines and mortal schedules to 7 Wang Family Village NPCs.

Canon basis: RI Ch.1-3. Wang Lin grows up in a small village (~100 families).
His family is poor but respected. Father Tianshui is stern but loving.
Mother Tingsu is gentle and worried. Brother Ping is protective.
Da Niu is a simple, strong childhood friend. Zhou Rui is a kind neighbor.
Wang Qingyue and Wang Tianshan are extended family.

These are MORTALS. Their schedules reflect village life: farm work, meals, market, sleep.
No cultivation, no sparring, no patrolling.
"""

import json, os

NPCS_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

# Canon-faithful initiation_lines for each NPC
# Lines should feel like mortals in a xianxia village — concerned with harvest,
# family, the upcoming aptitude test, and daily village life.
INITIATION_LINES = {
    "npc_wang_tianshui": [
        "Lin'er, your mother prepared extra food today. Eat before you go wandering.",
        "The Teng family's servants were in the market again, buying up grain. Mark my words — they are squeezing us out.",
        "I have heard the Heng Yue Sect elder will arrive soon for the aptitude test. Every family in the village is nervous.",
    ],
    "npc_zhou_tingsu": [
        "Wang Lin, take this bread. You always forget to eat when you are out exploring the hills.",
        "Be careful near the river. Old Zhou's boy nearly drowned there last spring. These waters are not forgiving.",
        "I had a dream last night. A man in blue robes descended from the sky. Perhaps the cultivators are coming soon.",
    ],
    "npc_wang_ping": [
        "Brother, Da Niu wants to go fishing at the south bend. Will you come? We could use the extra food.",
        "Father has been quiet lately. The Teng family's tax collectors came again yesterday. We gave them half our rice.",
        "If the Heng Yue elder tests us, I hope you pass. I will stay and take care of Mother and Father, but you... you could become someone.",
    ],
    "npc_da_niu": [
        "Eh heh! Wang Lin! Come help me carry these logs. You are smarter than me, but I am stronger — we make a good team!",
        "I heard there are spirit beasts in the mountains east of here. My grandfather said he saw one forty years ago. Want to go look?",
        "The village elder says we should stay close to home before the aptitude test. But I am not afraid. What is the worst that could happen?",
    ],
    "npc_zhou_rui": [
        "Good morning, Wang Lin. Your father's carpentry work is the finest in the village. He made my new shelves last week.",
        "The harvest was poor this season. If the Teng family demands more grain, I do not know what we will do.",
        "I have some spare thread and needles. If your mother needs them for mending, send Wang Ping to my house.",
    ],
    "npc_wang_qingyue": [
        "Cousin Wang Lin, the ancestral hall needs sweeping before the test ceremony. Would you help?",
        "Our family has lived in this village for five generations. We may be poor, but we have our dignity.",
        "I heard the cultivators can fly. Can you imagine? Looking down at our little village from the clouds...",
    ],
    "npc_wang_tianshan": [
        "Young one, respect your elders. The Wang family's reputation was built on discipline, not luck.",
        "When I was your age, the Teng family was just a merchant clan. Now they act like nobility. Times change.",
        "The memorial shrine must be maintained. Our ancestors watch over us. Do not forget that.",
    ],
}

# Mortal daily schedules — farming village life
# MC day: 0=dawn, 6000=noon, 12000=dusk, 18000=midnight
MORTAL_SCHEDULES = {
    # Wang Tianshui - stern father, carpenter, works hard
    "npc_wang_tianshui": [
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 2000, "act": "wandering", "dir": "south", "dist": 15},   # Early morning check on farm
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 7000, "act": "wandering", "dir": "east", "dist": 20},   # Carpentry work
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "south", "dist": 20}, # Afternoon farm work
        {"t0": 11000, "t1": 13000, "act": "wandering", "dir": "west", "dist": 15},  # Evening market/errands
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Zhou Tingsu - gentle mother, household work
    "npc_zhou_tingsu": [
        {"t0": 0, "t1": 1000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 1000, "t1": 2000, "act": "wandering", "dir": "south", "dist": 10},   # Morning chores
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "wandering", "dir": "west", "dist": 12},    # Laundry/washing at river
        {"t0": 5000, "t1": 6000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 6000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},    # Tend garden
        {"t0": 8000, "t1": 10000, "act": "wandering", "dir": "south", "dist": 12},  # Visit neighbors / collect water
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Wang Ping - protective younger brother, helps father
    "npc_wang_ping": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "east", "dist": 20},    # Help father carpentry
        {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 7000, "t1": 9000, "act": "wandering", "dir": "south", "dist": 25},   # Farm work
        {"t0": 9000, "t1": 10000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 10000, "t1": 13000, "act": "wandering", "dir": "west", "dist": 20},  # Free time / fishing with Da Niu
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Da Niu - simple strong farmhand
    "npc_da_niu": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 7000, "act": "wandering", "dir": "south", "dist": 25},   # Heavy farm work
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 10000, "act": "wandering", "dir": "south", "dist": 20},  # More farm work
        {"t0": 10000, "t1": 12000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 12000, "t1": 14000, "act": "wandering", "dir": "west", "dist": 15},  # Hang out / fishing
        {"t0": 14000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Zhou Rui - kind neighbor, has shop/trade
    "npc_zhou_rui": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "west", "dist": 15},    # Tend own farm
        {"t0": 6000, "t1": 8000, "act": "wandering", "dir": "north", "dist": 20},   # Market stall / trade
        {"t0": 8000, "t1": 10000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "east", "dist": 15},  # Visit neighbors
        {"t0": 12000, "t1": 13000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Wang Qingyue - cousin, helps with village duties
    "npc_wang_qingyue": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "wandering", "dir": "north", "dist": 15},   # Sweep ancestral hall
        {"t0": 5000, "t1": 7000, "act": "wandering", "dir": "south", "dist": 15},   # Village errands
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "east", "dist": 15},    # Help with harvest
        {"t0": 11000, "t1": 13000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
    # Wang Tianshan - family elder, inspects memorial shrine
    "npc_wang_tianshan": [
        {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
        {"t0": 2000, "t1": 3000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 3000, "t1": 5000, "act": "wandering", "dir": "north", "dist": 20},   # Inspect memorial shrine
        {"t0": 5000, "t1": 7000, "act": "wandering", "dir": "east", "dist": 15},    # Walk village, check on families
        {"t0": 7000, "t1": 8000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 8000, "t1": 11000, "act": "wandering", "dir": "south", "dist": 15},  # Afternoon rest / tea
        {"t0": 11000, "t1": 13000, "act": "eating", "dir": None, "dist": 0},
        {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
    ],
}


def main():
    changed = []
    for npc_id, lines in INITIATION_LINES.items():
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        if not os.path.exists(path):
            print(f"WARNING: {path} not found")
            continue
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        data["initiation_lines"] = lines
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')
        changed.append(npc_id)
        print(f"  initiation_lines: {npc_id} ({len(lines)} lines)")

    for npc_id, schedule in MORTAL_SCHEDULES.items():
        path = os.path.join(NPCS_DIR, f"{npc_id}.json")
        with open(path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        data["daily_schedule"] = schedule
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write('\n')
        changed.append(npc_id)
        print(f"  schedule: {npc_id} ({len(schedule)} blocks)")

    print(f"\nTotal unique files changed: {len(set(changed))}")
    for f in sorted(set(changed)):
        print(f"  - {f}")


if __name__ == "__main__":
    main()