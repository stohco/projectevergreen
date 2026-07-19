#!/usr/bin/env python3
"""
AUTO-CANON-034: Nan Dou City completion.
Creates 11 loot tables + 10 INFERRED NPCs with schedules + tasks.
Nan Dou City (南斗城) is a Zhao Country backdrop city.
All NPCs are INFERRED (derivation_type="I").
City theme: northern trade hub with celestial navigation/astronomy flavor
(南斗 = Southern Dipper constellation).
"""
import json, os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ============================================================
# PART 1: LOOT TABLES (11 districts, wealth gradient)
# ============================================================
# Wealth gradient: governor_mansion > smuggler_tunnels > cultivator_quarter
#   > residential > market > warehouse > tavern > temple > port > city_gate > mortal_quarter

loot_tables = {
    "nan_dou_city_governor_mansion": {
        "comment": "Nan Dou City — Governor's Mansion. Highest wealth. The governor collects taxes for the Teng family.",
        "rolls": (3, 6),
        "entries": [
            ("minecraft:emerald", 15, 3, 8),
            ("minecraft:golden_apple", 3, 1, 1),
            ("minecraft:gold_ingot", 10, 2, 5),
            ("minecraft:iron_ingot", 8, 2, 4),
            ("minecraft:book", 6, 1, 2),
            ("minecraft:diamond", 2, 1, 2),
            ("minecraft:paper", 8, 2, 5),
            ("minecraft:ink_sac", 5, 1, 3),
        ]
    },
    "nan_dou_city_smuggler_tunnels": {
        "comment": "Nan Dou City — Smuggler Tunnels. High value contraband. INFERRED: smuggling route under the city.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 14, 3, 10),
            ("minecraft:gold_ingot", 12, 2, 6),
            ("minecraft:iron_ingot", 8, 1, 4),
            ("minecraft:diamond", 3, 1, 2),
            ("minecraft:coal", 6, 4, 12),
            ("minecraft:ink_sac", 8, 2, 5),
            ("minecraft:glass_bottle", 6, 2, 6),
        ]
    },
    "nan_dou_city_cultivator_quarter": {
        "comment": "Nan Dou City — Cultivator Quarter. Cultivation supplies and books. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 12, 2, 6),
            ("minecraft:book", 10, 1, 3),
            ("minecraft:paper", 10, 2, 5),
            ("minecraft:ink_sac", 8, 1, 3),
            ("minecraft:iron_ingot", 6, 1, 3),
            ("minecraft:coal", 6, 2, 8),
            ("minecraft:glass_bottle", 5, 1, 3),
        ]
    },
    "nan_dou_city_residential_district": {
        "comment": "Nan Dou City — Residential District. Middle-class homes. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 1, 4),
            ("minecraft:iron_ingot", 8, 1, 3),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:bread", 10, 2, 5),
            ("minecraft:book", 5, 1, 1),
            ("minecraft:wheat", 6, 4, 12),
        ]
    },
    "nan_dou_city_market_district": {
        "comment": "Nan Dou City — Market District. Trade goods. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 12, 2, 5),
            ("minecraft:iron_ingot", 10, 1, 3),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:bread", 10, 2, 4),
            ("minecraft:paper", 6, 1, 3),
            ("minecraft:gold_nugget", 8, 1, 4),
            ("minecraft:book", 4, 1, 1),
        ]
    },
    "nan_dou_city_tavern_district": {
        "comment": "Nan Dou City — Tavern District. Food, drink, rumors. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 1, 4),
            ("minecraft:bread", 12, 3, 8),
            ("minecraft:wheat", 8, 4, 12),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:iron_ingot", 6, 1, 2),
            ("minecraft:glass_bottle", 6, 2, 5),
            ("minecraft:paper", 4, 1, 2),
        ]
    },
    "nan_dou_city_temple_district": {
        "comment": "Nan Dou City — Temple District. Incense, books, offerings. INFERRED: temple to the Southern Dipper constellation.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 4),
            ("minecraft:book", 10, 1, 3),
            ("minecraft:paper", 10, 2, 6),
            ("minecraft:ink_sac", 8, 1, 4),
            ("minecraft:coal", 8, 3, 8),
            ("minecraft:glass_bottle", 4, 1, 2),
        ]
    },
    "nan_dou_city_port_docks": {
        "comment": "Nan Dou City — Port Docks. Trade goods, fishing supplies. INFERRED: northern port on Zhao's waterway.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 3),
            ("minecraft:iron_ingot", 10, 2, 5),
            ("minecraft:coal", 10, 4, 10),
            ("minecraft:planks", 8, 8, 16),
            ("minecraft:sticks", 6, 4, 12),
            ("minecraft:bread", 8, 2, 6),
            ("minecraft:wheat", 6, 4, 10),
        ]
    },
    "nan_dou_city_warehouse_district": {
        "comment": "Nan Dou City — Warehouse District. Bulk storage. INFERRED.",
        "rolls": (3, 6),
        "entries": [
            ("minecraft:wheat", 12, 8, 20),
            ("minecraft:coal", 10, 4, 12),
            ("minecraft:planks", 10, 8, 16),
            ("minecraft:iron_ingot", 6, 1, 4),
            ("minecraft:emerald", 6, 1, 4),
            ("minecraft:bread", 8, 4, 10),
            ("minecraft:sticks", 6, 4, 12),
        ]
    },
    "nan_dou_city_city_gate": {
        "comment": "Nan Dou City — City Gate. Guard supplies. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:iron_ingot", 10, 1, 3),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:arrow", 10, 4, 12),
            ("minecraft:emerald", 4, 1, 2),
            ("minecraft:bread", 6, 2, 4),
            ("minecraft:planks", 4, 4, 8),
        ]
    },
    "nan_dou_city_mortal_quarter": {
        "comment": "Nan Dou City — Mortal Quarter. Poorest district. Basic survival goods. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:bread", 12, 2, 6),
            ("minecraft:wheat", 10, 4, 12),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:stick", 6, 4, 10),
            ("minecraft:planks", 4, 2, 6),
        ]
    },
}

for name, cfg in loot_tables.items():
    entries = []
    for item_data in cfg["entries"]:
        name_item, weight = item_data[0], item_data[1]
        entry = {"type": "minecraft:item", "name": name_item, "weight": weight, "functions": []}
        if len(item_data) > 2:
            entry["functions"].append({
                "function": "minecraft:set_count",
                "count": {"min": item_data[2], "max": item_data[3]}
            })
        entries.append(entry)
    # Add empty slot
    entries.append({"type": "minecraft:empty", "weight": 10})

    table = {
        "type": "minecraft:chest",
        "pools": [{
            "rolls": {"min": cfg["rolls"][0], "max": cfg["rolls"][1]},
            "entries": entries
        }]
    }
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, "w") as f:
        json.dump(table, f, indent=2)
    print(f"  Loot: {name}.json")

print(f"Created {len(loot_tables)} loot tables.")

# ============================================================
# PART 2: NPC DEFINITIONS (10 INFERRED NPCs)
# ============================================================
# Nan Dou City theme: northern trade hub, celestial navigation (Southern Dipper)
# All INFERRED. Each has 4 initiation_lines, 7-8 schedule entries, 2 sect_tasks.

npcs = [
    {
        "npc_id": "npc_nandou_guard_captain",
        "name": "Guard Captain Liang",
        "nameCn": "梁守卫长",
        "canon_id": "I-npc_nandou_guard_captain",
        "type": "guard",
        "faction": "Zhao Country Military",
        "location": "nan_dou_city",
        "cultivation": "Qi Condensation 5th Layer",
        "personality": "strict, watches the northern border, mistrusts strangers",
        "speech": "military, clipped",
        "salt": 3782110547,
        "initiation_lines": [
            "Halt. Nan Dou City is on high alert. State your name, affiliation, and business.",
            "The Southern Dipper hangs low tonight. The elders say it means trouble at the border. I say it means more patrols.",
            "If you see anything suspicious near the port — crates that don't match the manifests, ships that dock too quietly — report to me.",
            "The Teng family governor rarely leaves the mansion. But his tax collectors are everywhere. Watch yourself."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "north", "dist": 5},
            {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "south", "dist": 12},
            {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 8},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "border_patrol_supplies",
                "description": "Captain Liang: 'My guards need arrows and leather for repairs. The border raids have been frequent. Can you help?'",
                "requires": [
                    {"item": "minecraft:arrow", "count": 16},
                    {"item": "minecraft:leather", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 5,
            },
            {
                "id": "contraband_report",
                "description": "Liang lowers his voice: 'I suspect smugglers are using the tunnels. If you find iron and cobblestone being moved in bulk at night, bring me proof.'",
                "requires": [
                    {"item": "minecraft:iron_ingot", "count": 8},
                    {"item": "minecraft:cobblestone", "count": 32},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 7,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_merchant_fang",
        "name": "Merchant Fang",
        "nameCn": "商贩方",
        "canon_id": "I-npc_nandou_merchant_fang",
        "type": "merchant",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "chatty, knows trade routes, complains about taxes",
        "speech": "salesman-like, gossipy",
        "salt": 5519283746,
        "initiation_lines": [
            "Welcome to Nan Dou! Finest goods north of the capital. Iron from the mountains, wheat from the plains, fish from the port.",
            "Trade has been slow since the Teng family increased tariffs. I used to ship goods to Tian Shui City — now the diviners there can't afford my paper.",
            "If you're heading south, be careful on the road. Bandits have been targeting cultivators near the Wang Family Village border.",
            "The portmaster and I have an arrangement — I sell what arrives, he looks the other way on certain crates. Perfectly legal, of course."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 5},
            {"t0": 3000, "t1": 8000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "north", "dist": 10},
            {"t0": 12000, "t1": 13000, "act": "wandering", "dir": "west", "dist": 5},
            {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "trade_resupply",
                "description": "Fang: 'I need supplies for my next caravan. Wheat, coal, and iron — the three pillars of commerce. Bring them and I will pay well!'",
                "requires": [
                    {"item": "minecraft:wheat", "count": 16},
                    {"item": "minecraft:coal", "count": 8},
                    {"item": "minecraft:iron_ingot", "count": 4},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 8,
            },
            {
                "id": "caravan_paper_trade",
                "description": "Fang whispers: 'The temple needs paper and ink for their star charts. I buy low, they pay high. Bring me raw materials.'",
                "requires": [
                    {"item": "minecraft:paper", "count": 8},
                    {"item": "minecraft:ink_sac", "count": 4},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 7,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_dock_master",
        "name": "Dock Master Zhou",
        "nameCn": "码头周",
        "canon_id": "I-npc_nandou_dock_master",
        "type": "laborer",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "gruff, pragmatic, knows the harbor secrets",
        "speech": "blunt, nautical",
        "salt": 8823456102,
        "initiation_lines": [
            "Ships come and go. Some carry wheat. Some carry questions I don't ask. That's the port.",
            "The Northern Dipper constellation guides our ships at night. The temple priests chart the routes, but I've been sailing these waters since before they could read a star map.",
            "Last week a ship arrived from Great Wang Capital. The cargo was marked 'tribute' but the crates were too light. Someone is skimming.",
            "If you need work, the docks always need hands. Planks, sticks, coal — we burn through materials faster than the merchant can resupply."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 6},
            {"t0": 3000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 8},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "dock_repairs",
                "description": "Zhou: 'Storms damaged the pier. I need planks and sticks for repairs. Coal for the work lamps too.'",
                "requires": [
                    {"item": "minecraft:planks", "count": 16},
                    {"item": "minecraft:stick", "count": 16},
                    {"item": "minecraft:coal", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 6,
            },
            {
                "id": "unusual_cargo",
                "description": "Zhou looks around: 'A ship came in with cargo that doesn't match the manifest. Iron ingots that were too light. If you find anything strange in the warehouse, bring it to me.'",
                "requires": [
                    {"item": "minecraft:iron_ingot", "count": 4},
                    {"item": "minecraft:coal", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 7,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_temple_astrologer",
        "name": "Astrologer Chen",
        "nameCn": "星象师陈",
        "canon_id": "I-npc_nandou_temple_astrologer",
        "type": "cultivator",
        "faction": "Zhao Country Temple",
        "location": "nan_dou_city",
        "cultivation": "Qi Condensation 8th Layer",
        "personality": "scholarly, obsessed with stars, mildly eccentric",
        "speech": "measured, references constellations",
        "salt": 2293847561,
        "initiation_lines": [
            "The Southern Dipper governs fate. Nan Dou City was built beneath its light. You stand in a city that reads the sky.",
            "Unlike those fortune-tellers in Tian Shui City who read palms, we read the stars. The difference? Stars do not lie. Palms do.",
            "I need paper and ink for my star charts. If you bring me materials, I will share what the heavens reveal about your path.",
            "Last month the Dipper shifted. I reported it to the governor. He laughed. The old fool does not understand that stars are more powerful than armies."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 6},
            {"t0": 4000, "t1": 8000, "act": "wandering", "dir": "west", "dist": 8},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 11000, "t1": 12000, "act": "wandering", "dir": "south", "dist": 5},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "star_chart_materials",
                "description": "Astrologer Chen: 'I require paper and ink to chart the Dipper's movement. The heavens wait for no one — bring me supplies and I shall read your fortune.'",
                "requires": [
                    {"item": "minecraft:paper", "count": 8},
                    {"item": "minecraft:ink_sac", "count": 4},
                ],
                "reward_item": "minecraft:book",
                "reward_count": 2,
            },
            {
                "id": "temple_incense",
                "description": "Chen: 'The temple incense is running low. Coal for the braziers, planks for the offering tables. The Southern Dipper must be honored.'",
                "requires": [
                    {"item": "minecraft:coal", "count": 12},
                    {"item": "minecraft:planks", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 5,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_tavern_keeper",
        "name": "Tavern Keeper Sun",
        "nameCn": "客栈孙",
        "canon_id": "I-npc_nandou_tavern_keeper",
        "type": "service",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "friendly, hears everything, trades information for drinks",
        "speech": "warm, gossipy, slightly drunk",
        "salt": 6677823491,
        "initiation_lines": [
            "First drink is free. After that, you pay — or you tell me something interesting. I collect stories like others collect emeralds.",
            "A cultivator from the Corpse Yin Sect passed through last month. Ordered nothing, stared at the wall for three hours, then left. Strange folk.",
            "The dock workers say the warehouse district has been busy at night. More activity than the manifests would suggest. But what do I know — I just pour drinks.",
            "If you have paper and ink, I can write down the rumors I've heard. Some of them are even true."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 4},
            {"t0": 3000, "t1": 10000, "act": "wandering", "dir": "north", "dist": 6},
            {"t0": 10000, "t1": 11000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 11000, "t1": 13000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 13000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "tavern_rumors",
                "description": "Sun slides you a drink: 'Information costs. Bring me wheat for bread, coal for the hearth, and I will tell you what I have heard about the governor's late-night visitors.'",
                "requires": [
                    {"item": "minecraft:wheat", "count": 12},
                    {"item": "minecraft:coal", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 6,
            },
            {
                "id": "information_trade",
                "description": "Sun leans in: 'A dock worker told me something about the warehouse. Paper and ink — I will write it down for you. The captain would pay good money for this.'",
                "requires": [
                    {"item": "minecraft:paper", "count": 4},
                    {"item": "minecraft:ink_sac", "count": 2},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 5,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_cultivator_park",
        "name": "Cultivator Park",
        "nameCn": "修士朴克",
        "canon_id": "I-npc_nandou_cultivator_park",
        "type": "cultivator",
        "faction": "Rogue Cultivator",
        "location": "nan_dou_city",
        "cultivation": "Foundation Establishment early stage",
        "personality": "arrogant but insecure, came to Nan Dou to avoid sect politics",
        "speech": "formal, slightly condescending",
        "salt": 4489102763,
        "initiation_lines": [
            "I left the Xuan Dao Sect to cultivate in peace. Nan Dou is far enough from the sect wars — or so I thought.",
            "The cultivator quarter here is a joke. No proper spirit stones, no formation arrays, just a handful of rogues pretending they have a path.",
            "If you have iron and coal, I can trade for books from my personal collection. Real cultivation knowledge, not the garbage the temple astrologer reads.",
            "The governor has been trying to recruit cultivators for his personal guard. The pay is good but the company is worse."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "north", "dist": 8},
            {"t0": 4000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "south", "dist": 6},
            {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "cultivation_materials",
                "description": "Park: 'My cultivation requires basic materials. Iron for formations, coal for alchemy fires. Bring them and I will share knowledge.'",
                "requires": [
                    {"item": "minecraft:iron_ingot", "count": 6},
                    {"item": "minecraft:coal", "count": 12},
                    {"item": "minecraft:emerald", "count": 4},
                ],
                "reward_item": "minecraft:book",
                "reward_count": 2,
            },
            {
                "id": "rogue_intelligence",
                "description": "Park lowers his voice: 'I have been watching the smuggler tunnels. Something is being moved that is not on any manifest. Bring me glass bottles — I need them for an observation array.'",
                "requires": [
                    {"item": "minecraft:glass_bottle", "count": 8},
                    {"item": "minecraft:coal", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 8,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_smuggler_qin",
        "name": "Smuggler Qin",
        "nameCn": "走私者秦",
        "canon_id": "I-npc_nandou_smuggler_qin",
        "type": "rogue",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "Qi Condensation 6th Layer",
        "personality": "nervous, greedy, knows the tunnels better than anyone",
        "speech": "whispered, paranoid",
        "salt": 9134567820,
        "initiation_lines": [
            "You found the tunnels. Not many people know about these. The governor doesn't know. The guard captain doesn't know. Keep it that way.",
            "I move goods that the Teng family would rather handle themselves. Gold, emeralds, the occasional spirit stone. Nothing illegal — just... untaxed.",
            "If you have gold and emeralds, I can double them. The tunnels connect to the warehouse district — I know every route.",
            "The dock master suspects something. The tavern keeper knows too much. I may need to relocate soon."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "north", "dist": 12},
            {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 6},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "smuggler_goods",
                "description": "Qin: 'Gold and emeralds — I can move them through the tunnels and triple the value. Bring me materials and I will give you a cut.'",
                "requires": [
                    {"item": "minecraft:gold_ingot", "count": 4},
                    {"item": "minecraft:emerald", "count": 6},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 14,
            },
            {
                "id": "tunnel_maintenance",
                "description": "Qin: 'The tunnels need repairs. Iron for supports, cobblestone for walls. If the guard captain finds a collapse, my operation is finished.'",
                "requires": [
                    {"item": "minecraft:iron_ingot", "count": 6},
                    {"item": "minecraft:cobblestone", "count": 32},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 9,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_elder_wu",
        "name": "Elder Wu",
        "nameCn": "吴长者",
        "canon_id": "I-npc_nandou_elder_wu",
        "type": "mortal",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "kind, remembers old times, gives context about the city",
        "speech": "slow, nostalgic, wise",
        "salt": 7712345890,
        "initiation_lines": [
            "I have lived in Nan Dou for sixty years. Before the Teng family, before the Zhao Country governor. This city was built by star-worshippers.",
            "The temple used to be the heart of the city. Now the governor's mansion is. That tells you everything about what this place has become.",
            "If you are hungry, take this bread. Cultivators, mortals — hunger does not care about realm. I remember when Wang Lin's grandfather traded in this very market.",
            "The mortal quarter used to stretch twice as far. The governor expanded the warehouse district. Storage for tribute, they said. Storage for greed, I say."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "east", "dist": 4},
            {"t0": 4000, "t1": 8000, "act": "wandering", "dir": "south", "dist": 6},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "north", "dist": 5},
            {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "elder_humanitarian",
                "description": "Elder Wu: 'The mortal quarter is struggling. Bread and wheat would help the families here. I cannot pay much, but I can share what I know about this city's history.'",
                "requires": [
                    {"item": "minecraft:bread", "count": 8},
                    {"item": "minecraft:wheat", "count": 16},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 3,
            },
            {
                "id": "temple_glass",
                "description": "Wu: 'The temple's star-gazing instruments need glass. If you bring glass bottles, I will tell you a secret about the smuggler tunnels that even the guard captain does not know.'",
                "requires": [
                    {"item": "minecraft:glass_bottle", "count": 6},
                    {"item": "minecraft:planks", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 4,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_warehouse_clerk",
        "name": "Warehouse Clerk Hao",
        "nameCn": "库房吏郝",
        "canon_id": "I-npc_nandou_warehouse_clerk",
        "type": "service",
        "faction": "Zhao Country Government",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "nervous, follows orders, notices discrepancies",
        "speech": "clipped, bureaucratic",
        "salt": 3345678129,
        "initiation_lines": [
            "All goods entering the warehouse must be logged. I log them. That is my job. I do not ask questions about what is missing.",
            "The manifests say twenty crates of wheat arrived last night. I counted fifteen. The other five... I did not count the other five.",
            "If you need to store goods, there is a fee. Planks for crates, paper for manifests. Standard procedure.",
            "The dock master and the smuggler have an arrangement. I do not know what it is. I do not want to know what it is."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 5},
            {"t0": 3000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 8},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "north", "dist": 6},
            {"t0": 11000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 4},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "warehouse_manifest",
                "description": "Hao: 'I need paper and ink for manifests, planks for crate repairs. The tribute shipment is due next week and everything must be in order.'",
                "requires": [
                    {"item": "minecraft:paper", "count": 6},
                    {"item": "minecraft:ink_sac", "count": 3},
                    {"item": "minecraft:planks", "count": 12},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 6,
            },
            {
                "id": "discrepancy_investigation",
                "description": "Hao looks nervous: 'Goods keep disappearing from the warehouse. If you find iron and cobblestone being moved through the tunnels at night, report to the guard captain — not to me.'",
                "requires": [
                    {"item": "minecraft:iron_ingot", "count": 4},
                    {"item": "minecraft:cobblestone", "count": 16},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 5,
            },
        ],
    },
    {
        "npc_id": "npc_nandou_fisherman_mo",
        "name": "Fisherman Mo",
        "nameCn": "渔夫莫",
        "canon_id": "I-npc_nandou_fisherman_mo",
        "type": "mortal",
        "faction": "none",
        "location": "nan_dou_city",
        "cultivation": "none",
        "personality": "quiet, observant, knows the waterways",
        "speech": "simple, direct, occasionally insightful",
        "salt": 1192345678,
        "initiation_lines": [
            "The river tells you things if you listen. Last night it carried the sound of hammering from beneath the warehouse. Tunnels, maybe.",
            "I fish at dawn and dusk. The stars are clearest then. The temple astrologer says the Southern Dipper is my patron. I say it just means I wake up early.",
            "Coal for my cooking fire, planks for my boat repairs. If you have spare materials, I will trade fish — or information about what floats downriver.",
            "A body washed up last month. A cultivator, from the look of the robes. The guard captain said it was a river accident. I am not so sure."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 9000, "act": "wandering", "dir": "west", "dist": 6},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "north", "dist": 8},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {
                "id": "fishing_supplies",
                "description": "Mo: 'I need coal for my fire and planks for my boat. The river fish are biting well — I can pay in emeralds from my catch.'",
                "requires": [
                    {"item": "minecraft:coal", "count": 8},
                    {"item": "minecraft:planks", "count": 12},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 5,
            },
            {
                "id": "river_intelligence",
                "description": "Mo looks at the water: 'I see things on the river at night. Lights under the warehouse. Boats that carry no fishing nets. Bring me sticks and coal for a signal fire and I will tell you the schedule.'",
                "requires": [
                    {"item": "minecraft:stick", "count": 16},
                    {"item": "minecraft:coal", "count": 8},
                ],
                "reward_item": "minecraft:emerald",
                "reward_count": 6,
            },
        ],
    },
]

# Write NPCs
for npc_data in npcs:
    npc = {
        "_comment": f"NPC: {npc_data['name']} ({npc_data['nameCn']}). INFERRED: Nan Dou City. Derivation: I = INFERRED.",
        "npc_id": npc_data["npc_id"],
        "name": npc_data["name"],
        "nameCn": npc_data["nameCn"],
        "canon_id": npc_data["canon_id"],
        "type": npc_data["type"],
        "faction": npc_data["faction"],
        "location": npc_data["location"],
        "cultivation": npc_data["cultivation"],
        "personality": npc_data["personality"],
        "speech": npc_data["speech"],
        "relationship_to_wanglin": "INFERRED",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": False,
        "perception_tiers": {
            "mortal": f"A {npc_data['type']} of Nan Dou City",
            "qi_condensation": f"{npc_data['name']}, Nan Dou {npc_data['type']}"
        },
        "canon_confidence": 1,
        "note": f"INFERRED: Nan Dou City {npc_data['type']}. {npc_data['personality']}.",
        "derivation_type": "I",
        "salt": npc_data["salt"],
        "dao_heart": {
            "stability": 50,
            "cracks": [],
            "last_tested_tick": None,
            "note": "0-100."
        },
        "soul_state": "none",
        "tribulation_debt": 0,
        "_xianxia_schema": 1,
        "initiation_lines": npc_data["initiation_lines"],
        "daily_schedule": npc_data["daily_schedule"],
        "sect_tasks": npc_data["sect_tasks"],
    }
    path = os.path.join(NPC_DIR, f"{npc_data['npc_id']}.json")
    with open(path, "w") as f:
        json.dump(npc, f, indent=2)
    print(f"  NPC: {npc_data['npc_id']}.json")

print(f"\nCreated {len(npcs)} NPCs.")
print(f"\nTotals: {len(loot_tables)} loot tables, {len(npcs)} NPCs.")
print(f"  Initiation lines: {sum(len(n['initiation_lines']) for n in npcs)}")
print(f"  Schedule entries: {sum(len(n['daily_schedule']) for n in npcs)}")
print(f"  Tasks: {sum(len(n['sect_tasks']) for n in npcs)}")