#!/usr/bin/env python3
"""
AUTO-CANON-035: Qilin City completion.
Creates 11 loot tables + 10 INFERRED NPCs with schedules + tasks.
Qilin City (麒麟城) is a Zhao Country backdrop city.
Theme: spirit beast trading hub (麒麟 = mythical qilin/unicorn beast).
All NPCs are INFERRED (derivation_type="I").
"""
import json, os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ============================================================
# PART 1: LOOT TABLES (11 districts, wealth gradient)
# Theme: beast trading → more animal-related loot, leather, bones
# Wealth: governor > smuggler > cultivator > residential > market > tavern > temple > port > warehouse > city_gate > mortal
# ============================================================

loot_tables = {
    "qilin_city_governor_mansion": {
        "comment": "Qilin City — Governor's Mansion. Wealth from beast trade taxes.",
        "rolls": (3, 6),
        "entries": [
            ("minecraft:emerald", 15, 3, 8),
            ("minecraft:golden_apple", 3, 1, 1),
            ("minecraft:gold_ingot", 10, 2, 5),
            ("minecraft:iron_ingot", 8, 2, 4),
            ("minecraft:book", 6, 1, 2),
            ("minecraft:diamond", 2, 1, 2),
            ("minecraft:leather", 8, 2, 6),
            ("minecraft:bone", 6, 4, 12),
        ]
    },
    "qilin_city_smuggler_tunnels": {
        "comment": "Qilin City — Smuggler Tunnels. Illicit beast parts trade. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 14, 3, 10),
            ("minecraft:gold_ingot", 12, 2, 6),
            ("minecraft:iron_ingot", 8, 1, 4),
            ("minecraft:diamond", 3, 1, 2),
            ("minecraft:bone", 10, 6, 16),
            ("minecraft:leather", 10, 4, 10),
            ("minecraft:ink_sac", 6, 1, 3),
        ]
    },
    "qilin_city_cultivator_quarter": {
        "comment": "Qilin City — Cultivator Quarter. Beast taming supplies. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 12, 2, 6),
            ("minecraft:book", 10, 1, 3),
            ("minecraft:paper", 10, 2, 5),
            ("minecraft:iron_ingot", 6, 1, 3),
            ("minecraft:bone", 8, 4, 12),
            ("minecraft:leather", 8, 2, 6),
            ("minecraft:lead", 4, 2, 6),
        ]
    },
    "qilin_city_residential_district": {
        "comment": "Qilin City — Residential District. Middle-class beast handlers. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 1, 4),
            ("minecraft:iron_ingot", 8, 1, 3),
            ("minecraft:leather", 10, 2, 6),
            ("minecraft:bone", 8, 4, 10),
            ("minecraft:bread", 10, 2, 5),
            ("minecraft:book", 5, 1, 1),
            ("minecraft:wheat", 6, 4, 12),
        ]
    },
    "qilin_city_market_district": {
        "comment": "Qilin City — Market District. Beast trade goods. INFERRED.",
        "rolls": (2, 5),
        "entries": [
            ("minecraft:emerald", 12, 2, 5),
            ("minecraft:iron_ingot", 10, 1, 3),
            ("minecraft:leather", 12, 3, 8),
            ("minecraft:bone", 10, 4, 12),
            ("minecraft:lead", 6, 2, 6),
            ("minecraft:bread", 8, 2, 4),
            ("minecraft:gold_nugget", 6, 1, 4),
        ]
    },
    "qilin_city_tavern_district": {
        "comment": "Qilin City — Tavern District. Beast handlers drink here. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 10, 1, 4),
            ("minecraft:bread", 12, 3, 8),
            ("minecraft:wheat", 8, 4, 12),
            ("minecraft:leather", 8, 2, 6),
            ("minecraft:bone", 6, 2, 8),
            ("minecraft:iron_ingot", 6, 1, 2),
            ("minecraft:glass_bottle", 4, 1, 3),
        ]
    },
    "qilin_city_temple_district": {
        "comment": "Qilin City — Temple District. Temple to the Qilin beast spirit. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 4),
            ("minecraft:book", 10, 1, 3),
            ("minecraft:paper", 10, 2, 6),
            ("minecraft:ink_sac", 8, 1, 4),
            ("minecraft:bone", 8, 4, 12),
            ("minecraft:coal", 6, 2, 8),
        ]
    },
    "qilin_city_port_docks": {
        "comment": "Qilin City — Port Docks. Beast transport ships. INFERRED.",
        "rolls": (2, 4),
        "entries": [
            ("minecraft:emerald", 8, 1, 3),
            ("minecraft:iron_ingot", 10, 2, 5),
            ("minecraft:coal", 10, 4, 10),
            ("minecraft:planks", 8, 8, 16),
            ("minecraft:lead", 8, 4, 10),
            ("minecraft:leather", 6, 2, 6),
            ("minecraft:bread", 6, 2, 6),
        ]
    },
    "qilin_city_warehouse_district": {
        "comment": "Qilin City — Warehouse District. Bulk beast feed and cages. INFERRED.",
        "rolls": (3, 6),
        "entries": [
            ("minecraft:wheat", 12, 8, 20),
            ("minecraft:bone", 10, 8, 20),
            ("minecraft:lead", 8, 4, 10),
            ("minecraft:planks", 8, 8, 16),
            ("minecraft:iron_ingot", 4, 1, 3),
            ("minecraft:emerald", 4, 1, 3),
            ("minecraft:leather", 6, 2, 8),
        ]
    },
    "qilin_city_city_gate": {
        "comment": "Qilin City — City Gate. Beast inspection checkpoint. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:iron_ingot", 10, 1, 3),
            ("minecraft:lead", 8, 2, 6),
            ("minecraft:arrow", 10, 4, 12),
            ("minecraft:leather", 6, 2, 4),
            ("minecraft:emerald", 4, 1, 2),
            ("minecraft:bread", 6, 2, 4),
        ]
    },
    "qilin_city_mortal_quarter": {
        "comment": "Qilin City — Mortal Quarter. Poorest, beast fodder handlers. INFERRED.",
        "rolls": (1, 3),
        "entries": [
            ("minecraft:bread", 12, 2, 6),
            ("minecraft:wheat", 10, 4, 12),
            ("minecraft:bone", 8, 4, 12),
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
    entries.append({"type": "minecraft:empty", "weight": 10})
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
# Qilin City theme: spirit beast trading hub
# ============================================================

npcs = [
    {
        "npc_id": "npc_qilin_guard_captain",
        "name": "Guard Captain Xu",
        "nameCn": "徐守卫长",
        "canon_id": "I-npc_qilin_guard_captain",
        "type": "guard",
        "faction": "Zhao Country Military",
        "location": "qilin_city",
        "cultivation": "Qi Condensation 6th Layer",
        "personality": "gruff, inspects beast cargo, hates paperwork",
        "speech": "military, impatient",
        "salt": 5829173640,
        "initiation_lines": [
            "Halt. All beast cargo must be inspected before entering Qilin. The governor takes a cut of every trade — I just make sure nothing escapes the count.",
            "Last week someone tried to smuggle a spirit wolf pup past the gate. A pup! As if the city doesn't have enough wolves already.",
            "If you are not here to trade beasts, you are here to buy them. Either way, stay out of the warehouse district after dark.",
            "The beast riots last month were bad. Three handlers dead, two spirit deer escaped. The governor blamed me. I blame the smugglers who drugged the animals."
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
            {"id": "gate_inspection_supplies", "description": "Captain Xu: 'Inspection supplies are low. I need arrows for the guards and lead for the beast pens. Bring them and I will pay.'", "requires": [{"item": "minecraft:arrow", "count": 16}, {"item": "minecraft:lead", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 5},
            {"id": "contraband_beasts", "description": "Xu frowns: 'Beast parts are being moved through the tunnels at night. If you find bones and leather in bulk, report to me — this is illegal trade.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:leather", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 7},
        ],
    },
    {
        "npc_id": "npc_qilin_beast_merchant",
        "name": "Beast Merchant Zhao",
        "nameCn": "兽商赵",
        "canon_id": "I-npc_qilin_beast_merchant",
        "type": "merchant",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "shrewd, specializes in beast trades, has contacts across Zhao",
        "speech": "salesman-like, animal metaphors",
        "salt": 2938475610,
        "initiation_lines": [
            "Welcome to Qilin City, the beast trading capital of Zhao Country. Spirit wolves, cloud deer, iron-crystal boars — I can source anything with claws.",
            "Business is good. The Teng family buys war beasts. The Heng Yue Sect wants spirit herb gatherers. Everyone needs something from someone.",
            "Leather and bone are the currency of Qilin. Bring me materials and I will trade fairly — unlike the dock masters who skim off the top.",
            "A merchant from Nan Dou City was here last week. Said the star-readers there predict a beast plague. I told him the stars do not feed my animals — gold does."
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
            {"id": "beast_trade_materials", "description": "Zhao: 'I need leather and bone for my next shipment to the Teng family. Bring me materials and I will pay in emeralds — pure profit for you.'", "requires": [{"item": "minecraft:leather", "count": 12}, {"item": "minecraft:bone", "count": 16}], "reward_item": "minecraft:emerald", "reward_count": 9},
            {"id": "cage_construction", "description": "Zhao: 'My beast cages need repair. Iron for bars, planks for frames. The Teng family will not accept damaged goods.'", "requires": [{"item": "minecraft:iron_ingot", "count": 8}, {"item": "minecraft:planks", "count": 16}], "reward_item": "minecraft:emerald", "reward_count": 7},
        ],
    },
    {
        "npc_id": "npc_qilin_dock_handler",
        "name": "Dock Handler Li",
        "nameCn": "码头力李",
        "canon_id": "I-npc_qilin_dock_handler",
        "type": "laborer",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "strong, works with beast transport, knows which ships carry contraband",
        "speech": "blunt, nautical",
        "salt": 7145623890,
        "initiation_lines": [
            "The docks handle live cargo — mostly beasts, sometimes contraband. I load and unload. I do not ask what is in the unmarked crates.",
            "Spirit deer are the hardest to transport. They panic in enclosed spaces. Last week one kicked through a crate and swam to the other side of the river.",
            "If you need work, bring me planks and coal. The beast pens on the ships need constant repair. These animals are stronger than they look.",
            "A ship from Great Wang Capital arrived last night. The manifest said 'tribute grain' but the crates were too light and they smelled of leather."
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
            {"id": "pen_repairs", "description": "Li: 'Beast pens on the ships need planks and coal for the work lamps. These animals break everything — bring supplies.'", "requires": [{"item": "minecraft:planks", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 6},
            {"id": "suspicious_cargo", "description": "Li glances around: 'The Great Wang Capital ship last night — crates too light for grain. If you find anything strange near the warehouse, bring me proof.'", "requires": [{"item": "minecraft:iron_ingot", "count": 4}, {"item": "minecraft:leather", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 7},
        ],
    },
    {
        "npc_id": "npc_qilin_temple_keeper",
        "name": "Temple Keeper Bai",
        "nameCn": "庙祝白",
        "canon_id": "I-npc_qilin_temple_keeper",
        "type": "cultivator",
        "faction": "Qilin Beast Temple",
        "location": "qilin_city",
        "cultivation": "Qi Condensation 7th Layer",
        "personality": "spiritual, reveres the qilin beast, studies beast souls",
        "speech": "measured, reverent",
        "salt": 4561237890,
        "initiation_lines": [
            "The Qilin is the king of all beasts. This temple honors its spirit. Those who trade beasts without respect will find their fortunes turning.",
            "Unlike the astrologers in Nan Dou City who read stars, we read beasts. The eyes of a spirit wolf reveal more about the future than any constellation.",
            "I need paper and ink to record the temple's beast lineage records. The governor does not care about history, but the beasts remember.",
            "A cultivator from the Soul Refining Sect visited the temple last month. He asked about the Qilin's soul. I told him some things are not for sale."
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
            {"id": "beast_lineage_records", "description": "Bai: 'I record the lineage of every traded beast. Paper and ink — bring them and I will share knowledge about beast taming that even the Heng Yue Sect does not teach.'", "requires": [{"item": "minecraft:paper", "count": 8}, {"item": "minecraft:ink_sac", "count": 4}], "reward_item": "minecraft:book", "reward_count": 2},
            {"id": "temple_offerings", "description": "Bai: 'The temple offerings have dwindled. Bones for the beast spirits, coal for the incense braziers. The Qilin must be honored.'", "requires": [{"item": "minecraft:bone", "count": 16}, {"item": "minecraft:coal", "count": 12}], "reward_item": "minecraft:emerald", "reward_count": 5},
        ],
    },
    {
        "npc_id": "npc_qilin_tavern_keeper",
        "name": "Tavern Keeper Niu",
        "nameCn": "客栈牛",
        "canon_id": "I-npc_qilin_tavern_keeper",
        "type": "service",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "boisterous, loves beast-fighting stories, trades information",
        "speech": "loud, friendly, gossipy",
        "salt": 8923456178,
        "initiation_lines": [
            "Welcome to the Iron Fang Tavern! Best roast boar in Zhao Country — I get the meat fresh from the market, if you know what I mean.",
            "Beast handlers are the best customers. They work hard, they drink hard, and they talk too much. Perfect for a man who collects information.",
            "A cultivator wearing Corpse Yin Sect robes came in last week. Ordered nothing, watched a beast fight in the alley, then left. Creepy.",
            "If you want rumors, bring me wheat and coal. The tavern runs on bread and warmth, and my mouth runs on emeralds."
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
            {"id": "tavern_rumors", "description": "Niu: 'Information costs bread and coal. Bring supplies and I will tell you what I heard about the governor's secret beast auctions.'", "requires": [{"item": "minecraft:wheat", "count": 12}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 6},
            {"id": "beast_fight_intel", "description": "Niu: 'Paper and ink — I will write down what the beast handlers say about the smuggler tunnels. The captain would pay well for this.'", "requires": [{"item": "minecraft:paper", "count": 4}, {"item": "minecraft:ink_sac", "count": 2}], "reward_item": "minecraft:emerald", "reward_count": 5},
        ],
    },
    {
        "npc_id": "npc_qilin_beast_tamer",
        "name": "Beast Tamer Gao",
        "nameCn": "驯兽师高",
        "canon_id": "I-npc_qilin_beast_tamer",
        "type": "cultivator",
        "faction": "Rogue Cultivator",
        "location": "qilin_city",
        "cultivation": "Foundation Establishment mid stage",
        "personality": "quiet, bonds with beasts, distrusts city politics",
        "speech": "soft-spoken, direct",
        "salt": 3345678912,
        "initiation_lines": [
            "I tame beasts, not people. The cultivator quarter is too noisy for my work. I stay near the port where the animals are.",
            "The governor wants me to train war beasts for the Teng family. I refuse. My beasts are not weapons.",
            "If you bring me leather and lead, I can craft beast collars. In return, I will share what I know about restriction techniques — useful for trapping, not fighting.",
            "A spirit wolf pup followed me home last week. Its mother was sold to the Teng family. I am raising it in secret. If the governor finds out, he will take it."
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
            {"id": "beast_collars", "description": "Gao: 'I need leather for collars and lead for chains. My beasts need new restraints. Bring materials and I will share restriction knowledge.'", "requires": [{"item": "minecraft:leather", "count": 8}, {"item": "minecraft:lead", "count": 12}], "reward_item": "minecraft:book", "reward_count": 2},
            {"id": "secret_pup", "description": "Gao: 'The wolf pup needs wheat for food and coal for warmth. The governor's men are searching for it. Help me hide it.'", "requires": [{"item": "minecraft:wheat", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 8},
        ],
    },
    {
        "npc_id": "npc_qilin_smuggler",
        "name": "Smuggler Fang",
        "nameCn": "走私者方",
        "canon_id": "I-npc_qilin_smuggler",
        "type": "rogue",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "Qi Condensation 9th Layer",
        "personality": "cunning, moves live beasts through tunnels, sees himself as a freedom fighter",
        "speech": "whispered, self-justifying",
        "salt": 6578901234,
        "initiation_lines": [
            "You found the tunnels. I move beasts — rare ones, the kind the governor claims for 'tax purposes' and sells for profit.",
            "I am not a criminal. The governor taxes beast traders into poverty, then sells confiscated animals to the Teng family. I redistribute.",
            "Gold and emeralds — I can move anything through these tunnels. The passages connect to the warehouse district and the port.",
            "The beast tamer in the port knows about me. He does not approve, but he does not report me either. We have an understanding."
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
            {"id": "beast_smuggling", "description": "Fang: 'Gold and emeralds — I can triple their value moving rare beasts through the tunnels. Bring me materials for the operation.'", "requires": [{"item": "minecraft:gold_ingot", "count": 4}, {"item": "minecraft:emerald", "count": 6}], "reward_item": "minecraft:emerald", "reward_count": 14},
            {"id": "tunnel_supports", "description": "Fang: 'The tunnels are collapsing in places. Iron for supports, cobblestone for walls. If they collapse, the animals die.'", "requires": [{"item": "minecraft:iron_ingot", "count": 6}, {"item": "minecraft:cobblestone", "count": 32}], "reward_item": "minecraft:emerald", "reward_count": 9},
        ],
    },
    {
        "npc_id": "npc_qilin_elder_shen",
        "name": "Elder Shen",
        "nameCn": "沈长者",
        "canon_id": "I-npc_qilin_elder_shen",
        "type": "mortal",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "wise, former beast handler, mourns the old ways",
        "speech": "slow, nostalgic",
        "salt": 1287654390,
        "initiation_lines": [
            "I trained beasts for forty years before the governor turned Qilin into a factory. Animals used to be partners. Now they are products.",
            "The temple used to perform the Qilin Blessing every solstice. Now the governor sells the blessing ceremony tickets to merchants. Everything has a price.",
            "If you are hungry, take this bread. The mortal quarter used to have plenty — before the governor expanded the beast pens into our farmland.",
            "Wang Lin's father once came here to buy a spirit wolf for his son. The boy had no talent for cultivation, they said. How wrong they were."
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
            {"id": "mortal_aid", "description": "Shen: 'The mortal quarter families are starving. Bread and wheat would help. I cannot pay much, but I remember when this city was decent.'", "requires": [{"item": "minecraft:bread", "count": 8}, {"item": "minecraft:wheat", "count": 16}], "reward_item": "minecraft:emerald", "reward_count": 3},
            {"id": "old_tunnels", "description": "Shen: 'Before the governor, there were older tunnels — ventilation shafts for the beast pens. Bring me planks and I will show you where they connect.'", "requires": [{"item": "minecraft:planks", "count": 8}, {"item": "minecraft:coal", "count": 4}], "reward_item": "minecraft:emerald", "reward_count": 4},
        ],
    },
    {
        "npc_id": "npc_qilin_warehouse_boss",
        "name": "Warehouse Boss Deng",
        "nameCn": "库房邓",
        "canon_id": "I-npc_qilin_warehouse_boss",
        "type": "service",
        "faction": "Zhao Country Government",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "corrupt, skims from the governor's beast taxes",
        "speech": "bureaucratic, evasive",
        "salt": 9012345678,
        "initiation_lines": [
            "All beasts entering Qilin are logged, weighed, and taxed. I manage the records. The records are accurate. Mostly.",
            "The governor taxes twenty percent of all beast trades. I record twenty percent. What happens between the docks and the warehouse is... logistics.",
            "If you need to store goods, bring planks and paper. Storage is not free, even for the governor's men.",
            "The beast merchant Zhao and I have an understanding. His invoices match my records. The actual numbers are between us and the river."
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
            {"id": "warehouse_records", "description": "Deng: 'I need paper and ink for manifests, planks for beast crates. The next shipment is due and everything must look... correct.'", "requires": [{"item": "minecraft:paper", "count": 6}, {"item": "minecraft:ink_sac", "count": 3}, {"item": "minecraft:planks", "count": 12}], "reward_item": "minecraft:emerald", "reward_count": 6},
            {"id": "beast_inventory", "description": "Deng: 'Beast feed and supplies are running low. Wheat and bones for the pens. If the animals starve, the governor will have my head.'", "requires": [{"item": "minecraft:wheat", "count": 16}, {"item": "minecraft:bone", "count": 16}], "reward_item": "minecraft:emerald", "reward_count": 5},
        ],
    },
    {
        "npc_id": "npc_qilin_mortal_hunter",
        "name": "Mortal Hunter Mei",
        "nameCn": "猎人梅",
        "canon_id": "I-npc_qilin_mortal_hunter",
        "type": "mortal",
        "faction": "none",
        "location": "qilin_city",
        "cultivation": "none",
        "personality": "practical, knows the wilderness, supplements income with hunting",
        "speech": "simple, observant",
        "salt": 5678901234,
        "initiation_lines": [
            "I hunt beasts in the wild and sell them at the market. The beast merchants pay well for live captures, better for rare species.",
            "The woods north of Qilin used to be full of spirit deer. Now the merchants have hunted them nearly to extinction. Greed.",
            "If you need hunting supplies, I always need coal for my campfires and lead for sling shots. I can pay in emeralds from my catches.",
            "I saw lights in the smuggler tunnels last night. Not torches — something green. Cultivation light. Someone is doing more than moving beasts down there."
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
            {"id": "hunting_supplies", "description": "Mei: 'I need coal for my campfires and lead for sling shots. The winter hunt is coming and I need to be prepared.'", "requires": [{"item": "minecraft:coal", "count": 8}, {"item": "minecraft:lead", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 6},
            {"id": "tunnel_lights", "description": "Mei: 'Green lights in the smuggler tunnels. Not normal. Bring me sticks and coal for a signal fire and I will show you where I saw them from.'", "requires": [{"item": "minecraft:stick", "count": 16}, {"item": "minecraft:coal", "count": 8}], "reward_item": "minecraft:emerald", "reward_count": 6},
        ],
    },
]

for npc_data in npcs:
    npc = {
        "_comment": f"NPC: {npc_data['name']} ({npc_data['nameCn']}). INFERRED: Qilin City. Derivation: I = INFERRED.",
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
            "mortal": f"A {npc_data['type']} of Qilin City",
            "qi_condensation": f"{npc_data['name']}, Qilin {npc_data['type']}"
        },
        "canon_confidence": 1,
        "note": f"INFERRED: Qilin City {npc_data['type']}. {npc_data['personality']}.",
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