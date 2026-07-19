#!/usr/bin/env python3
"""AUTO-CANON-028: Heng Yue Sect — 8 missing loot tables + 9 NPC tasks.

Gaps found:
  - 8 structures have NO loot tables (out of 20 total): alchemy_courtyard,
    disciple_dormitories, hidden_treasury, main_plaza, outer_gate,
    spirit_herb_garden, spirit_spring, trial_grounds.
  - 9/12 NPCs have 0 sect_tasks (only Qiu Siping, Sun Dazhu, Wang Zhuo
    had tasks).

Canon basis (RI Ch.3-8):
  Heng Yue Sect is where Wang Lin spends his formative years. The sect
  has distinct functional areas: alchemy courtyard (pill refinement),
  spirit herb garden (cultivation herbs), spirit spring (meditation),
  hidden treasury (sect wealth, restricted access), trial grounds
  (disciple testing), disciple dormitories (living quarters).
  Elders give lectures and assign tasks. Disciples spar, cultivate,
  study, and patrol.

Zero Java changes. Article XXII (canon→gameplay), XXIII (vertical slice),
XXVI (build content, not infrastructure).
"""

import json
import os

BASE = os.path.join(os.path.dirname(__file__), "..",
    "src", "main", "resources", "data", "ergenverse")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ═══════════════════════════════════════════════════════════════════
# PART 1: 8 LOOT TABLES
# ═══════════════════════════════════════════════════════════════════

LOOT_TABLES = {
    # 1. Alchemy Courtyard — reagents for pill refinement
    "heng_yue_sect_alchemy_courtyard": {
        "rolls": {"rolls": 3, "min_rolls": 2, "max_rolls": 5},
        "entries": [
            ("minecraft:paper", 15, [2, 6]),
            ("minecraft:coal", 12, [3, 8]),
            ("minecraft:glass_bottle", 10, [1, 3]),
            ("minecraft:glowstone_dust", 8, [1, 3]),
            ("minecraft:book", 5, [1, 1]),
            ("minecraft:emerald", 4, [1, 2]),
            ("minecraft:blaze_powder", 3, [1, 2]),  # INFERRED: alchemy catalyst
            ("minecraft:empty", 10),
        ]
    },
    # 2. Disciple Dormitories — basic outer disciple supplies
    "heng_yue_sect_disciple_dormitories": {
        "rolls": {"rolls": 2, "min_rolls": 1, "max_rolls": 3},
        "entries": [
            ("minecraft:bread", 15, [2, 5]),
            ("minecraft:coal", 10, [2, 6]),
            ("minecraft:stick", 8, [4, 12]),
            ("minecraft:iron_ingot", 5, [1, 2]),
            ("minecraft:book", 4, [1, 1]),
            ("minecraft:emerald", 3, [1, 1]),
            ("minecraft:empty", 15),
        ]
    },
    # 3. Hidden Treasury — sect's most valuable resources (HIGHEST VALUE)
    "heng_yue_sect_hidden_treasury": {
        "rolls": {"rolls": 4, "min_rolls": 3, "max_rolls": 6},
        "entries": [
            ("minecraft:emerald", 20, [2, 8]),
            ("minecraft:gold_nugget", 15, [3, 9]),
            ("minecraft:iron_ingot", 10, [2, 5]),
            ("minecraft:diamond", 3, [1, 1]),         # rare high-value
            ("minecraft:glowstone_dust", 12, [2, 6]), # INFERRED: spirit stones
            ("minecraft:book", 8, [1, 2]),            # INFERRED: techniques/jade slips
            ("minecraft:paper", 6, [2, 5]),           # INFERRED: talismans
            ("minecraft:gold_ingot", 5, [1, 2]),
            ("minecraft:empty", 5),
        ]
    },
    # 4. Main Plaza — open gathering area, minimal loot
    "heng_yue_sect_main_plaza": {
        "rolls": {"rolls": 1, "min_rolls": 1, "max_rolls": 2},
        "entries": [
            ("minecraft:bread", 12, [1, 3]),
            ("minecraft:coal", 8, [1, 3]),
            ("minecraft:emerald", 3, [1, 1]),
            ("minecraft:empty", 30),
        ]
    },
    # 5. Outer Gate — guard supplies
    "heng_yue_sect_outer_gate": {
        "rolls": {"rolls": 2, "min_rolls": 1, "max_rolls": 3},
        "entries": [
            ("minecraft:arrow", 15, [4, 12]),
            ("minecraft:iron_ingot", 10, [1, 3]),
            ("minecraft:coal", 8, [2, 5]),
            ("minecraft:bread", 8, [1, 3]),
            ("minecraft:emerald", 3, [1, 1]),
            ("minecraft:empty", 20),
        ]
    },
    # 6. Spirit Herb Garden — cultivation herbs (INFERRED: wheat=herbs, glowstone=spirit herbs)
    "heng_yue_sect_spirit_herb_garden": {
        "rolls": {"rolls": 3, "min_rolls": 2, "max_rolls": 4},
        "entries": [
            ("minecraft:wheat", 18, [3, 10]),          # INFERRED: common herbs
            ("minecraft:glowstone_dust", 12, [1, 4]),  # INFERRED: spirit-grade herbs
            ("minecraft:bone_meal", 8, [2, 6]),        # INFERRED: fertilizer
            ("minecraft:book", 5, [1, 1]),              # INFERRED: herb identification guides
            ("minecraft:coal", 8, [2, 5]),             # INFERRED: harvesting tools fuel
            ("minecraft:emerald", 3, [1, 2]),
            ("minecraft:empty", 10),
        ]
    },
    # 7. Spirit Spring — meditation spot with spirit items (INFERRED)
    "heng_yue_sect_spirit_spring": {
        "rolls": {"rolls": 2, "min_rolls": 1, "max_rolls": 3},
        "entries": [
            ("minecraft:glowstone_dust", 15, [2, 5]),  # INFERRED: condensed spirit essence
            ("minecraft:glass_bottle", 10, [1, 3]),    # INFERRED: spirit water collection
            ("minecraft:emerald", 8, [1, 2]),           # INFERRED: offerings
            ("minecraft:coal", 6, [1, 3]),
            ("minecraft:book", 4, [1, 1]),
            ("minecraft:empty", 20),
        ]
    },
    # 8. Trial Grounds — training supplies and trial rewards (INFERRED)
    "heng_yue_sect_trial_grounds": {
        "rolls": {"rolls": 3, "min_rolls": 2, "max_rolls": 4},
        "entries": [
            ("minecraft:iron_ingot", 12, [1, 3]),
            ("minecraft:arrow", 10, [4, 10]),
            ("minecraft:coal", 8, [2, 6]),
            ("minecraft:emerald", 6, [1, 3]),
            ("minecraft:bread", 10, [2, 5]),
            ("minecraft:leather", 8, [1, 4]),
            ("minecraft:book", 3, [1, 1]),
            ("minecraft:empty", 12),
        ]
    },
}

def make_loot_table(name, spec):
    """Build a vanilla loot table JSON from a compact spec."""
    entries = []
    for e in spec["entries"]:
        if e[0] == "minecraft:empty":
            entries.append({"type": "minecraft:empty", "weight": e[1]})
        else:
            entry = {
                "type": "minecraft:item",
                "name": e[0],
                "weight": e[1],
            }
            if len(e) > 2 and e[2] != [1, 1]:
                entry["functions"] = [{
                    "function": "minecraft:set_count",
                    "count": e[2]
                }]
            entries.append(entry)
    return {
        "pools": [{
            "rolls": spec["rolls"],
            "entries": entries
        }]
    }

for name, spec in LOOT_TABLES.items():
    table = make_loot_table(name, spec)
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, "w") as f:
        json.dump(table, f, indent=2)
        f.write("\n")
    print(f"[LOOT] {name}: {len(spec['entries'])} entry types, rolls={spec['rolls']}")

# ═══════════════════════════════════════════════════════════════════
# PART 2: 9 NPC TASKS
# ═══════════════════════════════════════════════════════════════════

NPC_TASKS = {
    "npc_chen_bailiang": [
        {
            "id": "gather_alchemy_reagents",
            "description": "Elder Chen Bailiang adjusts his spectacles: 'I need glass bottles and fuel for today's pill refinement lecture. Bring me those materials and I will share a cultivation technique from my personal notes.'",
            "requires": [
                {"item": "minecraft:glass_bottle", "count": 3},
                {"item": "minecraft:coal", "count": 8}
            ],
            "reward_item": "minecraft:book",
            "reward_count": 1
        },
        {
            "id": "herb_garden_harvest",
            "description": "Chen Bailiang examines a withered herb: 'The Spirit Herb Garden needs harvesting before the herbs lose potency. Gather wheat from the eastern valley — I use it as a base reagent for Qi Gathering Pills.'",
            "requires": [
                {"item": "minecraft:wheat", "count": 12}
            ],
            "reward_item": "minecraft:glowstone_dust",
            "reward_count": 3
        }
    ],
    "npc_wu_yu": [
        {
            "id": "lecture_hall_preparation",
            "description": "Elder Wu Yu frowns at the empty lecture podium: 'My lecture on Qi foundations requires writing materials for the disciples. Bring paper and writing coal. Punctuality in preparation reflects punctuality in cultivation.'",
            "requires": [
                {"item": "minecraft:paper", "count": 8},
                {"item": "minecraft:coal", "count": 4}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        },
        {
            "id": "ancestor_hall_incense",
            "description": "Wu Yu gestures toward the Ancestor Hall: 'The memorial tablets of our founders require fresh incense. Bring coal for the braziers. Respect for the past strengthens resolve for the future.'",
            "requires": [
                {"item": "minecraft:coal", "count": 12}
            ],
            "reward_item": "minecraft:book",
            "reward_count": 1
        }
    ],
    "npc_ye_zi": [
        {
            "id": "formation_maintenance",
            "description": "Elder Ye Zi traces a glowing line in the air: 'The restriction formations around the Ancestor Hall are weakening. I need iron and coal to reinforce the array nodes. Do not touch the formations yourself — even Nascent Soul cultivators have been trapped.'",
            "requires": [
                {"item": "minecraft:iron_ingot", "count": 4},
                {"item": "minecraft:coal", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 3
        },
        {
            "id": "sect_patrol_report",
            "description": "Ye Zi narrows his eyes: 'I need a written patrol report from the outer perimeter. Bring me paper — you will serve as my scribe while I dictate observations on the eastern approach.'",
            "requires": [
                {"item": "minecraft:paper", "count": 5}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        }
    ],
    "npc_qian_kun": [
        {
            "id": "trade_herb_supplies",
            "description": "Qian Kun leans in conspiratorially: 'I know a disciple who trades rare herbs from the eastern valleys. But he wants payment in gold and emeralds. If you supply the currency, I will split the herbs with you — fair deal.'",
            "requires": [
                {"item": "minecraft:gold_nugget", "count": 6},
                {"item": "minecraft:emerald", "count": 2}
            ],
            "reward_item": "minecraft:glowstone_dust",
            "reward_count": 4
        },
        {
            "id": "practical_alchemy_ingredients",
            "description": "Qian Kun shrugs: 'Elder Chen needs herbs for his next lecture. I volunteered to gather them, but I am behind on my own cultivation. If you bring wheat and glowstone dust, I will give you my share of the lecture rewards.'",
            "requires": [
                {"item": "minecraft:wheat", "count": 8},
                {"item": "minecraft:glowstone_dust", "count": 2}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        }
    ],
    "npc_wang_hao": [
        {
            "id": "sparring_equipment",
            "description": "Wang Hao grins and flexes: 'The morning sparring session wiped out my arrows and leather armor patches. If you bring replacements, I will share some of the food I saved from the dining hall — the good portions, not the gruel.'",
            "requires": [
                {"item": "minecraft:arrow", "count": 16},
                {"item": "minecraft:leather", "count": 4}
            ],
            "reward_item": "minecraft:bread",
            "reward_count": 6
        },
        {
            "id": "dining_hall_food_run",
            "description": "Wang Hao whispers: 'I missed the dawn meal because Sun Dazhu blocked the door again. If you bring bread, I will pay you back with emeralds from my last mission reward. Please — I am starving.'",
            "requires": [
                {"item": "minecraft:bread", "count": 4}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 1
        }
    ],
    "npc_yun_fei": [
        {
            "id": "library_research_materials",
            "description": "Yun Fei looks up from a crumbling text: 'I am researching formation theory in the library, but several key scrolls are damaged. Bring me paper and a blank book — I will copy my notes for you. My research could accelerate your cultivation.'",
            "requires": [
                {"item": "minecraft:paper", "count": 10},
                {"item": "minecraft:book", "count": 1}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 3
        },
        {
            "id": "meditation_fuel",
            "description": "Yun Fei speaks softly: 'True advancement comes from stillness. I need coal for the meditation braziers near the peak and glowstone dust to enhance spiritual perception during deep meditation. Supply these and I will share a technique I discovered in the library.'",
            "requires": [
                {"item": "minecraft:coal", "count": 6},
                {"item": "minecraft:glowstone_dust", "count": 3}
            ],
            "reward_item": "minecraft:book",
            "reward_count": 1
        }
    ],
    "npc_sun_zhenwei": [
        {
            "id": "patrol_arrow_resupply",
            "description": "Sun Zhenwei checks his quiver with a scowl: 'The night patrol depletes arrows faster than the armory restocks them. I need more. Bring arrows and I will compensate you with iron from the sect stores.'",
            "requires": [
                {"item": "minecraft:arrow", "count": 24}
            ],
            "reward_item": "minecraft:iron_ingot",
            "reward_count": 3
        },
        {
            "id": "training_grounds_repair",
            "description": "Sun Zhenwei surveys the damaged training dummies: 'Sparring destroys the equipment faster than the carpenters can repair it. Bring iron and planks — I will reinforce the dummies so the next session is not cut short.'",
            "requires": [
                {"item": "minecraft:iron_ingot", "count": 3},
                {"item": "minecraft:oak_planks", "count": 16}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        }
    ],
    "npc_zhang_hu": [
        {
            "id": "beast_den_clearing",
            "description": "Zhang Hu cracks his knuckles: 'The spirit beasts near the herb garden have been aggressive lately. I need arrows and an iron sword to clear the den. Help me out and I will share whatever loot we find — those beasts sometimes carry spirit stones.'",
            "requires": [
                {"item": "minecraft:arrow", "count": 12},
                {"item": "minecraft:iron_sword", "count": 1}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 3
        },
        {
            "id": "sword_peak_path_repair",
            "description": "Zhang Hu points toward the peak: 'The Sword Peak formations are old and some paths are collapsing. I need coal for lantern fuel and iron to reinforce the railings. If you supply the materials, I will handle the dangerous climbing.'",
            "requires": [
                {"item": "minecraft:coal", "count": 10},
                {"item": "minecraft:iron_ingot", "count": 3}
            ],
            "reward_item": "minecraft:book",
            "reward_count": 1
        }
    ],
    "npc_zhou_lin": [
        {
            "id": "trial_preparation",
            "description": "Zhou Lin speaks with measured urgency: 'The sect trial approaches. I need supplies — bread for endurance, coal for the trial braziers. The elders test more than Qi; they test preparation. Help me and I will share my trial strategy notes.'",
            "requires": [
                {"item": "minecraft:bread", "count": 6},
                {"item": "minecraft:coal", "count": 8}
            ],
            "reward_item": "minecraft:emerald",
            "reward_count": 2
        },
        {
            "id": "dawn_meditation_supplies",
            "description": "Zhou Lin lowers his voice: 'The spirit veins near the peak are strongest at dawn. I plan to meditate there tonight. I need glowstone dust to amplify the vein's energy and coal for the meditation brazier. Supply these and I will share what I learn.'",
            "requires": [
                {"item": "minecraft:glowstone_dust", "count": 4},
                {"item": "minecraft:coal", "count": 6}
            ],
            "reward_item": "minecraft:book",
            "reward_count": 1
        }
    ],
}

for npc_id, tasks in NPC_TASKS.items():
    path = os.path.join(NPC_DIR, f"{npc_id}.json")
    with open(path, "r") as f:
        data = json.load(f)
    data["sect_tasks"] = tasks
    with open(path, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"[NPC] {npc_id}: {len(tasks)} tasks added")

# ═══════════════════════════════════════════════════════════════════
# VERIFICATION
# ═══════════════════════════════════════════════════════════════════
print("\n── Verification ──")

# Check all loot tables exist
import glob
hy_loot = sorted(glob.glob(os.path.join(LOOT_DIR, "heng_yue_sect_*.json")))
print(f"Heng Yue loot tables: {len(hy_loot)}")

# Check all NPCs have tasks
for npc_id in ["npc_chen_bailiang", "npc_qian_kun", "npc_qiu_siping",
               "npc_sun_dazhu", "npc_sun_zhenwei", "npc_wang_hao",
               "npc_wang_zhuo", "npc_wu_yu", "npc_ye_zi", "npc_yun_fei",
               "npc_zhang_hu", "npc_zhou_lin"]:
    with open(os.path.join(NPC_DIR, f"{npc_id}.json")) as f:
        d = json.load(f)
    t = len(d.get("sect_tasks", []))
    s = len(d.get("daily_schedule", []))
    l = len(d.get("initiation_lines", []))
    status = "OK" if t > 0 else "MISSING TASKS"
    print(f"  {npc_id}: {l} lines, {s} schedule, {t} tasks [{status}]")

print("\nDone. 8 loot tables created, 9 NPCs updated with tasks.")