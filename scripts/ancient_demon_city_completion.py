#!/usr/bin/env python3
"""
AUTO-CANON-036: Ancient Demon City completion.
Creates 10 district loot tables + 10 INFERRED NPCs with schedules + tasks.
Ancient Demon City (古魔城)  -  last Zhao backdrop city.
Theme: dark, ominous, feared by other cities, associated with demonic cultivators.
Existing: 1 orphan loot table (ancient_demon_city.json  -  powerful mod items, kept as-is),
1 canon NPC (npc_taga). Creating 10 district-specific loot tables + 10 INFERRED NPCs.
"""
import json, os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ============================================================
# PART 1: LOOT TABLES (10 districts  -  skip governor_mansion, use existing orphan for it)
# Theme: dark, bones, obsidian, nether materials, low gold, mysterious items
# ============================================================

loot_tables = {
    "ancient_demon_city_smuggler_tunnels": {
        "comment": "Ancient Demon City  -  Smuggler Tunnels. Dark artifacts and contraband. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 2, 6),
            ("minecraft:iron_ingot", 8, 1, 3),
            ("minecraft:bone", 12, 6, 16),
            ("minecraft:coal", 6, 2, 6),
            ("minecraft:ink_sac", 8, 2, 5),
            ("minecraft:obsidian", 6, 2, 4),
        ]
    },
    "ancient_demon_city_cultivator_quarter": {
        "comment": "Ancient Demon City  -  Cultivator Quarter. Demonic cultivation supplies. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 2, 5),
            ("minecraft:book", 10, 1, 2),
            ("minecraft:bone", 10, 4, 12),
            ("minecraft:ink_sac", 8, 2, 4),
            ("minecraft:coal", 8, 2, 8),
            ("minecraft:obsidian", 4, 1, 2),
        ]
    },
    "ancient_demon_city_residential_district": {
        "comment": "Ancient Demon City  -  Residential District. Sparse, fearful residents. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:emerald", 6, 1, 3),
            ("minecraft:bone", 10, 4, 10),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:bread", 8, 2, 4),
            ("minecraft:iron_ingot", 4, 1, 2),
        ]
    },
    "ancient_demon_city_market_district": {
        "comment": "Ancient Demon City  -  Market District. Dark goods, bone tools, rare herbs. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 1, 4),
            ("minecraft:bone", 12, 4, 12),
            ("minecraft:iron_ingot", 8, 1, 3),
            ("minecraft:coal", 8, 2, 6),
            ("minecraft:ink_sac", 6, 1, 3),
            ("minecraft:obsidian", 4, 1, 2),
        ]
    },
    "ancient_demon_city_tavern_district": {
        "comment": "Ancient Demon City  -  Tavern District. Dark rumors, suspicious patrons. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:emerald", 8, 1, 3),
            ("minecraft:bread", 10, 2, 6),
            ("minecraft:bone", 8, 2, 6),
            ("minecraft:coal", 6, 2, 4),
            ("minecraft:glass_bottle", 4, 1, 3),
            ("minecraft:ink_sac", 4, 1, 2),
        ]
    },
    "ancient_demon_city_temple_district": {
        "comment": "Ancient Demon City  -  Temple District. Ancestral demon worship. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 4),
            ("minecraft:bone", 12, 6, 16),
            ("minecraft:book", 8, 1, 2),
            ("minecraft:ink_sac", 8, 2, 5),
            ("minecraft:coal", 8, 4, 10),
            ("minecraft:obsidian", 4, 1, 2),
        ]
    },
    "ancient_demon_city_port_docks": {
        "comment": "Ancient Demon City  -  Port Docks. Dark wood, bone cargo. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 3),
            ("minecraft:iron_ingot", 8, 1, 4),
            ("minecraft:coal", 8, 4, 10),
            ("minecraft:planks", 8, 6, 12),
            ("minecraft:bone", 8, 4, 10),
            ("minecraft:stick", 4, 4, 8),
        ]
    },
    "ancient_demon_city_warehouse_district": {
        "comment": "Ancient Demon City  -  Warehouse District. Bone storage, dark materials. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:bone", 14, 10, 24),
            ("minecraft:coal", 8, 4, 10),
            ("minecraft:planks", 6, 4, 10),
            ("minecraft:iron_ingot", 4, 1, 3),
            ("minecraft:emerald", 4, 1, 3),
            ("minecraft:wheat", 4, 4, 10),
        ]
    },
    "ancient_demon_city_city_gate": {
        "comment": "Ancient Demon City  -  City Gate. Ominous, few visitors. INFERRED.",
        "rolls": (1, 2),
        "entries": [
            ("minecraft:iron_ingot", 8, 1, 2),
            ("minecraft:bone", 8, 2, 6),
            ("minecraft:arrow", 8, 4, 8),
            ("minecraft:coal", 6, 2, 4),
            ("minecraft:emerald", 2, 1, 2),
        ]
    },
    "ancient_demon_city_mortal_quarter": {
        "comment": "Ancient Demon City  -  Mortal Quarter. Poorest, fearful, bones everywhere. INFERRED.",
        "rolls": (1, 2),
        "entries": [
            ("minecraft:bread", 10, 2, 4),
            ("minecraft:bone", 10, 4, 10),
            ("minecraft:coal", 6, 2, 4),
            ("minecraft:stick", 4, 2, 6),
            ("minecraft:wheat", 4, 4, 8),
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
    entries.append({"type": "minecraft:empty", "weight": 8})
    table = {
        "type": "minecraft:chest",
        "pools": [{"rolls": {"min": cfg["rolls"][0], "max": cfg["rolls"][1]}, "entries": entries}]
    }
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, "w") as f:
        json.dump(table, f, indent=2)
    print(f"  Loot: {name}.json")

print(f"Created {len(loot_tables)} loot tables.")

# ============================================================
# PART 2: NPC DEFINITIONS (10 INFERRED NPCs)
# Ancient Demon City theme: dark, feared, demonic cultivation reputation
# ============================================================

npcs = [
    {
        "npc_id": "npc_ancient_demon_guard",
        "name": "Guard Captain Hei",
        "nameCn": "黑守卫长",
        "canon_id": "I-npc_ancient_demon_guard",
        "type": "guard",
        "faction": "Ancient Demon City Watch",
        "location": "ancient_demon_city",
        "cultivation": "Foundation Establishment early stage",
        "personality": "silent, intimidating, cultivates dark techniques",
        "speech": "barely speaks, menacing",
        "salt": 8234567890,
        "initiation_lines": [
            "Few people enter Ancient Demon City by choice. Those who do either seek power or have nowhere else to go.",
            "The governor does not care who enters. The city feeds on the desperate. I keep the desperate from killing each other.",
            "If you are here to trade, the market is east. If you are here to cultivate, the temple has... methods. If you are here to cause trouble, leave.",
            "Other cities call us demon worshippers. They are not entirely wrong. The temple's practices are... effective, if distasteful."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "north", "dist": 5},
            {"t0": 3000, "t1": 6000, "act": "wandering", "dir": "east", "dist": 12},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "south", "dist": 10},
            {"t0": 10000, "t1": 12000, "act": "wandering", "dir": "west", "dist": 8},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {"id": "watch_supplies", "description": "Hei: 'The watch needs arrows and coal for the signal fires. The bone piles near the gate attract creatures at night.'", "requires": [{"item": "minecraft:arrow", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "tunnel_activity", "description": "Hei: 'Something moves in the tunnels. Not beasts  -  cultivators. Bring me bones and iron and I will investigate.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:iron_ingot", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 6},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_merchant",
        "name": "Bone Merchant Gui",
        "nameCn": "骨商鬼",
        "canon_id": "I-npc_ancient_demon_merchant",
        "type": "merchant",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "Qi Condensation 9th Layer",
        "personality": "sinister, trades in bone tools and dark materials",
        "speech": "whispered, transactional",
        "salt": 4567890123,
        "initiation_lines": [
            "Welcome to the only market in Zhao where bones are worth more than gold. Spirit beast bones, ancient creature remains  -  I deal in them all.",
            "The merchants in Qilin City sell live beasts. I sell what remains after. There is more profit in death than life, sadly.",
            "Obsidian, ink, bones  -  bring me dark materials and I will pay fairly. The temple always needs supplies for their... rituals.",
            "A cultivator from the Corpse Yin Sect visited last month. Bought twelve bags of bone powder. I did not ask what it was for."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "east", "dist": 5},
            {"t0": 3000, "t1": 8000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 12000, "act": "wandering", "dir": "north", "dest": 10},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {"id": "bone_trade", "description": "Gui: 'I process bones into tools and powder. Bring me raw bones and ink  -  the temple pays well for bone powder.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:ink_sac", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 8},
            {"id": "dark_materials", "description": "Gui: 'Obsidian is rare and valuable. If you find any in the wild, bring it to me. Coal for the forge too.'", "requires": [{"item": "minecraft:obsidian", "count": 4}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 7},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_dock",
        "name": "Dock Master Leng",
        "nameCn": "码头冷",
        "canon_id": "I-npc_ancient_demon_dock",
        "type": "laborer",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "none",
        "personality": "cold, efficient, has seen too much",
        "speech": "flat, emotionless",
        "salt": 7890123456,
        "initiation_lines": [
            "Ships arrive. Ships leave. The cargo is always the same  -  bones, obsidian, coal, and things I do not catalogue.",
            "Unlike Qilin City where ships carry live beasts, our ships carry dead ones. The governor exports bone meal to the Teng family. Profitable but grim.",
            "I need planks and coal. The bone shipments are heavy and the dock cranes break constantly.",
            "A ship arrived from Nan Dou City last week. The captain refused to dock overnight. Said the city 'felt wrong.' He left at dawn."
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
            {"id": "dock_repairs", "description": "Leng: 'Planks and coal. The bone shipments warp the docks. Bring materials and I will pay.'", "requires": [{"item": "minecraft:planks", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "unmarked_cargo", "description": "Leng: 'Sometimes crates arrive with no manifest. The warehouse boss stores them. If you find anything unusual near the warehouse, I want to know.'", "requires": [{"item": "minecraft:iron_ingot", "count": 4}, {"item": "minecraft:coal", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 5},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_priest",
        "name": "Demon Priest Mo",
        "nameCn": "魔祭司莫",
        "canon_id": "I-npc_ancient_demon_priest",
        "type": "cultivator",
        "faction": "Ancient Demon Temple",
        "location": "ancient_demon_city",
        "cultivation": "Foundation Establishment late stage",
        "personality": "fanatical, believes dark cultivation is misunderstood",
        "speech": "zealous, lecturing",
        "salt": 3456789012,
        "initiation_lines": [
            "The temple of the Ancient Demon is older than Zhao Country itself. Before the governor, before the sects  -  this temple stood. It remembers what others have forgotten.",
            "Other cities worship ancestors or stars. We worship power. Not evil  -  power. There is a difference the Heng Yue Sect refuses to understand.",
            "I need bones for the temple furnaces and ink for the ritual scrolls. Bring me supplies and I will share knowledge they forbid in other sects.",
            "Taga dwells in the governor's mansion. He is the strongest cultivator in the city. Even he comes to the temple on dark moons."
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
            {"id": "temple_rituals", "description": "Mo: 'The temple furnaces burn bone and coal. Bring me materials and I will teach you a technique the orthodox sects suppress.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:coal", "count": 12}], "reward_item": "minecraft:book", "reward_count": 2},
            {"id": "ritual_scrolls", "description": "Mo: 'I record forbidden techniques. Paper and ink  -  bring them and I will share what I know about the Ancient Demon's original cultivation path.'", "requires": [{"item": "minecraft:paper", "count": 8}, {"item": "minecraft:ink_sac", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 6},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_tavern",
        "name": "Tavern Keeper Si",
        "nameCn": "客栈司",
        "canon_id": "I-npc_ancient_demon_tavern",
        "type": "service",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "none",
        "personality": "nervous, overhears dark secrets, survives by being invisible",
        "speech": "quiet, careful",
        "salt": 6789012345,
        "initiation_lines": [
            "Sit in the back. Do not draw attention. The cultivators here are not the friendly type.",
            "A man from the Soul Refining Sect came in last month. He asked about the temple's soul techniques. I pretended not to hear. I value my life.",
            "If you need food or rumors, bring wheat and coal. I keep my head down and my ears open. It has kept me alive in this city.",
            "The governor and the temple priest argue constantly. The priest wants to perform a Grand Ritual. The governor says it will attract attention from the Teng family. I say both of them are right."
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
            {"id": "quiet_rumors", "description": "Si whispers: 'Bring wheat and coal. I will tell you what I heard about the governor's dispute with the temple  -  and what the Soul Refining Sect man wanted.'", "requires": [{"item": "minecraft:wheat", "count": 12}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "written_intel", "description": "Si: 'Paper and ink. I will write down the temple priest's ritual schedule and guard patrol routes. Dangerous knowledge in this city.'", "requires": [{"item": "minecraft:paper", "count": 4}, {"item": "minecraft:ink_sac", "count": 2}], "reward_item": "minecraft:emerald", "reward_count": 4},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_cultivator",
        "name": "Dark Cultivator Yan",
        "nameCn": "暗修士燕",
        "canon_id": "I-npc_ancient_demon_cultivator",
        "type": "cultivator",
        "faction": "Rogue Cultivator",
        "location": "ancient_demon_city",
        "cultivation": "Foundation Establishment peak",
        "personality": "obsessed with power, experiments with bone refinement",
        "speech": "intense, single-minded",
        "salt": 9012345678,
        "initiation_lines": [
            "The temple teaches bone refinement  -  a forbidden technique. I have been practicing it for six years. My progress is... visible.",
            "Do not confuse bone refinement with the Corpse Yin Sect's corpse techniques. Bone refinement uses the essence left in bones, not the corpses themselves. There is a moral difference. A thin one.",
            "If you bring me bones and coal, I will trade cultivation knowledge. My methods are unorthodox but effective. The Heng Yue Sect would expel me. I do not care.",
            "The smuggler tunnels connect to an underground chamber beneath the temple. The priest does not know I have been exploring it. There are... things down there."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 4000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 4000, "t1": 8000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 8000, "t1": 9000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 9000, "t1": 11000, "act": "wandering", "dir": "north", "dist": 6},
            {"t0": 11000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {"id": "bone_refinement", "description": "Yan: 'I need bones and coal for my refinement experiments. Bring me materials and I will share what I have learned about bone essence extraction.'", "requires": [{"item": "minecraft:bone", "count": 20}, {"item": "minecraft:coal", "count": 12}], "reward_item": "minecraft:book", "reward_count": 2},
            {"id": "underground_chamber", "description": "Yan: 'The chamber beneath the temple has strange inscriptions. Bring me ink and paper and glass bottles  -  I need to document them before the priest finds out.'", "requires": [{"item": "minecraft:ink_sac", "count": 4}, {"item": "minecraft:paper", "count": 4}, {"item": "minecraft:glass_bottle", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 8},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_smuggler",
        "name": "Shadow Smuggler Ye",
        "nameCn": "影走私者叶",
        "canon_id": "I-npc_ancient_demon_smuggler",
        "type": "rogue",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "Foundation Establishment mid stage",
        "personality": "paranoid, moves artifacts through tunnels, trusts no one",
        "speech": "barely audible",
        "salt": 2345678901,
        "initiation_lines": [
            "You found me. That means the tavern keeper talked, or the hunter saw you following my trail. Neither would surprise me.",
            "I move things the governor does not want traced. Artifacts, texts, bones with residual soul essence. The temple priest pays well for certain... materials.",
            "Gold and emeralds  -  I can convert them through the tunnels to buyers in Nan Dou and Qilin. The profit is worth the risk. Mostly.",
            "The dark cultivator Yan knows too much. He explores the underground chamber. If he finds what I think is down there, the priest and I will have a problem."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 3000, "t0": 6000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t1": 10000, "act": "wandering", "dir": "north", "dist": 12},
            {"t0": 10000, "t0": 12000, "act": "wandering", "dir": "west", "dist": 6},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {"id": "artifact_smuggling", "description": "Ye: 'Gold and emeralds. I move them through the tunnels to external buyers. Bring materials.'", "requires": [{"item": "minecraft:gold_ingot", "count": 4}, {"item": "minecraft:emerald", "count": 6}], "reward_item": "minecraft:emerald", "reward_count": 14},
            {"id": "tunnel_reinforcement", "description": "Ye: 'The tunnels are collapsing near the temple. Iron and cobblestone  -  if they fall, the artifacts are lost forever.'", "requires": [{"item": "minecraft:iron_ingot", "count": 6}, {"item": "minecraft:cobblestone", "count": 32}], "reward_item": "minecraft:emerald", "reward_count": 9},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_elder",
        "name": "Elder Song",
        "nameCn": "宋长者",
        "canon_id": "I-npc_ancient_demon_elder",
        "type": "mortal",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "none",
        "personality": "haunted, last sane person in mortal quarter, knows city history",
        "speech": "trembling, urgent",
        "salt": 1234567890,
        "initiation_lines": [
            "You should not be here. No one should be here. This city is built on something ancient and it is waking up.",
            "I have lived here for seventy years. When I was young, the temple rituals were just ceremonies. Now they actually work. The bones glow at night. The governor pretends not to notice.",
            "If you are hungry, take this bread. The mortal quarter used to have farms. Now the bone dust from the warehouse poisons the soil. Nothing grows.",
            "Wang Lin passed through the valley near here once, decades ago. He was just a boy then. Even then, the spirits whispered about him. I did not understand what they meant until now."
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
            {"id": "mortal_survival", "description": "Song: 'The mortal quarter is dying. Bread and wheat for the families. I cannot pay, but I know what lies beneath this city.'", "requires": [{"item": "minecraft:bread", "count": 8}, {"item": "minecraft:wheat", "count": 16}], "reward_item": "minecraft:emerald", "reward_count": 3},
            {"id": "ancient_knowledge", "description": "Song: 'The temple was not always dark. Before the governor, it was a place of learning. Bring me coal and ink and I will write down what I remember of the old inscriptions.'", "requires": [{"item": "minecraft:coal", "count": 8}, {"item": "minecraft:ink_sac", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 4},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_warehouse",
        "name": "Warehouse Keeper An",
        "nameCn": "库房安",
        "canon_id": "I-npc_ancient_demon_warehouse",
        "type": "service",
        "faction": "Ancient Demon City Government",
        "location": "ancient_demon_city",
        "cultivation": "none",
        "personality": "fearful, processes bones all day, has seen things",
        "speech": "hushed, anxious",
        "salt": 5678901234,
        "initiation_lines": [
            "The warehouse stores bones. Hundreds of crates of bones. The smell... you get used to it. Eventually.",
            "The governor exports bone meal to the Teng family. Officially it is 'fertilizer.' Unofficially I do not want to know what it fertilizes.",
            "If you need supplies, bring paper and planks. Storage space is cheap here  -  no one wants to rent near the bone piles.",
            "The smuggler uses the tunnels to move things the governor does not want catalogued. I see the crates going in and out. I say nothing. Survival in this city means silence."
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
            {"id": "warehouse_supplies", "description": "An: 'Paper for manifests, planks for bone crates. The next shipment to the Teng family is due.'", "requires": [{"item": "minecraft:paper", "count": 6}, {"item": "minecraft:planks", "count": 12}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "bone_inventory", "description": "An: 'Bone inventory is... inconsistent. Some crates are lighter than they should be. If you find bone fragments or coal near the tunnels, bring them  -  I need to balance the books.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 4},
        ],
    },
    {
        "npc_id": "npc_ancient_demon_hunter",
        "name": "Bone Hunter You",
        "nameCn": "猎骨者尤",
        "canon_id": "I-npc_ancient_demon_hunter",
        "type": "mortal",
        "faction": "none",
        "location": "ancient_demon_city",
        "cultivation": "none",
        "personality": "brave, hunts bone creatures in the wild, knows the surrounding terrain",
        "speech": "direct, practical",
        "salt": 8901234567,
        "initiation_lines": [
            "I hunt bone creatures  -  skeletal beasts that roam the hills near the city. Their bones sell for good prices to the bone merchant.",
            "The bone creatures appeared about ten years ago. Before that, this was just a poor city with a dark reputation. Now it has a dark reality to match.",
            "If you need supplies for the wild, bring me sticks and coal. I always need fire and shelter materials.",
            "The smuggler tunnels connect to a cave system outside the city. I have seen cultivators entering at night with green light around their hands. The cave system goes deep  -  much deeper than the mortal quarter."
        ],
        "daily_schedule": [
            {"t0": 0, "t1": 2000, "act": "sleeping", "dir": None, "dist": 0},
            {"t0": 2000, "t1": 3000, "act": "wandering", "dir": "south", "dist": 8},
            {"t0": 3000, "t0": 6000, "act": "wandering", "dir": "east", "dist": 10},
            {"t0": 6000, "t1": 7000, "act": "eating", "dir": None, "dist": 0},
            {"t0": 7000, "t0": 9000, "act": "wandering", "dir": "west", "dist": 6},
            {"t0": 9000, "t0": 12000, "act": "wandering", "dir": "north", "dist": 8},
            {"t0": 12000, "t1": 24000, "act": "sleeping", "dir": None, "dist": 0},
        ],
        "sect_tasks": [
            {"id": "hunting_supplies", "description": "You: 'I need sticks for traps and coal for my campfire. The bone creatures are more active at night.'", "requires": [{"item": "minecraft:stick", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "cave_intel", "description": "You: 'The cave system outside the city connects to the tunnels. I have mapped part of it. Bring me bones for arrowheads and I will show you the entrance.'", "requires": [{"item": "minecraft:bone", "count": 12}, {"item": "minecraft:arrow", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 6},
        ],
    },
]

for npc_data in npcs:
    npc = {
        "_comment": f"NPC: {npc_data['name']} ({npc_data['nameCn']}). INFERRED: Ancient Demon City. Derivation: I = INFERRED.",
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
            "mortal": f"A {npc_data['type']} of Ancient Demon City",
            "qi_condensation": f"{npc_data['name']}, Ancient Demon {npc_data['type']}"
        },
        "canon_confidence": 1,
        "note": f"INFERRED: Ancient Demon City {npc_data['type']}. {npc_data['personality']}.",
        "derivation_type": "I",
        "salt": npc_data["salt"],
        "dao_heart": {"stability": 50, "cracks": [], "last_tested_tick": None, "note": "0-100."},
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