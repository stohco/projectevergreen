#!/usr/bin/env python3
"""
AUTO-CANON-046: Final 6 special locations — closes out ALL P1 special locations.
- daoist_peak_retreat (MEDIUM — secluded Daoist cultivation)
- wandering_cultivator_camp (MEDIUM — preserve orphan loot_table/wandering_cultivator.json)
- mortal_dust_outpost (LOW — edge of cultivation territory)
- foreign_war_monument (LOW — ancient war memorial, late game)
- foreign_void_rift (LOW — tear in reality, late game)
- foreign_soul_crystal_deposit (LOW — soul crystal formation, late game)

Produces: 5 new loot tables (1 orphan preserved) + 18 INFERRED NPCs.
Article XXI, XXII, XXIII, XXIV, P1.
"""

import json
import os
import hashlib
import random

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ─── Loot Table Definitions ─────────────────────────────────────────────────
# Each: {filename, pools: [{entries: [{name, weight, count?}]}]}

LOOT_TABLES = {
    # 1. Daoist Peak Retreat — MEDIUM (meditation, herbs, scrolls, tea, incense)
    "daoist_peak_retreat": {
        "pools": [
            # Pool 1: Common — meditation/herb supplies
            {"rolls": (3, 5), "entries": [
                ("minecraft:paper", 8, (2, 6)),
                ("minecraft:ink_sac", 7, (1, 3)),
                ("minecraft:book", 7, (1, 2)),
                ("minecraft:wheat", 6, (4, 10)),  # tea
                ("minecraft:coal", 6, (2, 6)),     # incense fuel
                ("minecraft:spruce_planks", 5, (3, 8)),  # construction
                ("minecraft:glowstone", 4, (1, 3)),  # spiritual light
                ("minecraft:emerald", 5, (1, 3)),
            ]},
            # Pool 2: Rewards — cultivation knowledge
            {"rolls": (1, 3), "entries": [
                ("ergenverse:jade_slip", 7),
                ("ergenverse:spirit_stone", 6, (1, 2)),
                ("minecraft:enchanted_book", 3),
                ("minecraft:experience_bottle", 5, (1, 2)),
                ("minecraft:emerald", 4, (2, 5)),
            ]},
            # Pool 3: Rare — Daoist secrets
            {"rolls": (0, 1), "entries": [
                ("ergenverse:spirit_stone", 4, (2, 4)),
                ("ergenverse:dao_fragment", 2),
                ("minecraft:golden_carrot", 3),  # spiritual sustenance
            ]},
        ]
    },
    # wandering_cultivator_camp — ORPHAN PRESERVED (loot_tables/chests/wandering_cultivator.json)
    # No new loot table created.
    # 2. Mortal Dust Outpost — LOW (basic frontier supplies)
    "mortal_dust_outpost": {
        "pools": [
            # Pool 1: Common — frontier survival
            {"rolls": (3, 5), "entries": [
                ("minecraft:bread", 8, (3, 8)),
                ("minecraft:wheat", 7, (4, 10)),
                ("minecraft:coal", 6, (2, 5)),
                ("minecraft:iron_ingot", 5, (1, 3)),
                ("minecraft:oak_planks", 6, (3, 8)),
                ("minecraft:stick", 7, (4, 12)),
                ("minecraft:emerald", 4, (1, 2)),
            ]},
            # Pool 2: Minor — basic cultivation supplies
            {"rolls": (1, 2), "entries": [
                ("minecraft:book", 4),
                ("minecraft:experience_bottle", 3, (1, 2)),
                ("minecraft:emerald", 5, (1, 3)),
                ("minecraft:iron_ingot", 4, (1, 2)),
            ]},
        ]
    },
    # 3. Foreign War Monument — LOW-MEDIUM (ancient war remnants)
    "foreign_war_monument": {
        "pools": [
            # Pool 1: Common — war debris
            {"rolls": (2, 4), "entries": [
                ("minecraft:iron_ingot", 8, (2, 5)),    # broken weapons
                ("minecraft:bone", 7, (3, 8)),           # fallen warriors
                ("minecraft:arrow", 6, (4, 12)),
                ("minecraft:stone_sword", 3),            # memorial weapons
                ("minecraft:shield", 3),
                ("minecraft:chainmail_helmet", 2),
                ("minecraft:coal", 5, (2, 5)),
                ("minecraft:cobblestone", 6, (3, 8)),
            ]},
            # Pool 2: Rewards — historical artifacts
            {"rolls": (1, 2), "entries": [
                ("minecraft:book", 5, (1, 2)),
                ("minecraft:emerald", 5, (2, 4)),
                ("ergenverse:jade_slip", 4),
                ("minecraft:enchanted_book", 2),
                ("minecraft:experience_bottle", 4, (1, 2)),
            ]},
            # Pool 3: Rare — ancient power remnants
            {"rolls": (0, 1), "entries": [
                ("ergenverse:spirit_stone", 3, (1, 2)),
                ("minecraft:iron_sword", 3),
                ("minecraft:diamond", 2, (1, 2)),
            ]},
        ]
    },
    # 4. Foreign Void Rift — LOW (dangerous, sparse, otherworldly)
    "foreign_void_rift": {
        "pools": [
            # Pool 1: Common — void materials
            {"rolls": (1, 3), "entries": [
                ("minecraft:obsidian", 8, (2, 6)),
                ("minecraft:ender_pearl", 5, (1, 2)),
                ("minecraft:coal", 4, (1, 4)),
                ("minecraft:cobblestone", 5, (2, 6)),
                ("minecraft:iron_ingot", 3, (1, 2)),
                ("minecraft:emerald", 3, (1, 2)),
            ]},
            # Pool 2: Minor — otherworldly traces
            {"rolls": (0, 1), "entries": [
                ("minecraft:ender_pearl", 4),
                ("minecraft:book", 3),
                ("minecraft:experience_bottle", 3),
                ("minecraft:emerald", 4, (1, 3)),
                ("minecraft:empty", 6),
            ]},
        ]
    },
    # 5. Foreign Soul Crystal Deposit — LOW-MEDIUM (crystal formations)
    "foreign_soul_crystal_deposit": {
        "pools": [
            # Pool 1: Common — crystal/mineral materials
            {"rolls": (2, 4), "entries": [
                ("minecraft:amethyst_shard", 8, (2, 6)),
                ("minecraft:quartz", 7, (3, 8)),
                ("minecraft:prismarine_shard", 5, (1, 4)),
                ("minecraft:coal", 5, (2, 5)),
                ("minecraft:iron_ingot", 4, (1, 3)),
                ("minecraft:emerald", 4, (1, 3)),
                ("minecraft:glowstone", 4, (1, 3)),
                ("minecraft:obsidian", 3, (1, 2)),
            ]},
            # Pool 2: Rewards — soul-related finds
            {"rolls": (1, 2), "entries": [
                ("minecraft:book", 4),
                ("minecraft:emerald", 5, (2, 4)),
                ("ergenverse:jade_slip", 3),
                ("minecraft:experience_bottle", 4, (1, 2)),
                ("minecraft:amethyst_shard", 5, (2, 4)),
            ]},
            # Pool 3: Rare — soul crystal power
            {"rolls": (0, 1), "entries": [
                ("ergenverse:spirit_stone", 3, (1, 2)),
                ("minecraft:amethyst_block", 2),
                ("ergenverse:soul_fragment", 2),
            ]},
        ]
    },
}

# ─── NPC Definitions ────────────────────────────────────────────────────────
# Each NPC: npc_id, name, nameCn, type, faction, location, cultivation, personality,
#           speech, relationship_to_wanglin, dialogue/quest/trade/teaching available,
#           canon_confidence, derivation_type, salt, initiation_lines (4),
#           daily_schedule (6-8 entries), sect_tasks (2 tasks)

NPCS = [
    # ── Daoist Peak Retreat (3 NPCs) ──
    {
        "npc_id": "npc_dpr_hermit_wei",
        "name": "Hermit Wei",
        "nameCn": "隐士魏",
        "type": "rogue_cultivator",
        "faction": "Daoist Peak Retreat",
        "location": "daoist_peak_retreat",
        "cultivation": "NASCENT_SOUL_MID",
        "personality": "serene, philosophical, speaks in metaphors about the Dao, occasionally cryptic",
        "speech": "measured, slow, frequently pauses mid-sentence to observe nature",
        "relationship_to_wanglin": "neutral-respectful — recognizes potential in those who climb the peak",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": True,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "You climbed. Most do not. The Daoist Peak does not call to those who seek power. It calls to those who seek silence.",
            "A Heng Yue elder visited once. He sat for three days, then left in frustration. He could not accept that the Dao has no curriculum.",
            "I have been here for forty years. The peak has not changed. I have not changed. That is the teaching.",
            "Ink and paper. If you wish to understand the Dao through words, I require these for my calligraphy."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn meditation", "location": "daoist_peak_retreat", "duration": 180},
            {"time": "07:00", "action": "Morning tea ceremony", "location": "daoist_peak_retreat", "duration": 90},
            {"time": "08:30", "action": "Calligraphy practice", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "10:30", "action": "Herb garden tending", "location": "daoist_peak_retreat", "duration": 150},
            {"time": "13:00", "action": "Midday lecture to acolytes", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "15:00", "action": "Meditation on the cliff edge", "location": "daoist_peak_retreat", "duration": 180},
            {"time": "19:00", "action": "Evening incense burning", "location": "daoist_peak_retreat", "duration": 60},
            {"time": "20:00", "action": "Night meditation", "location": "daoist_peak_retreat", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "dpr_calligraphy_supplies",
                "name": "Calligraphy Materials",
                "description": "Hermit Wei needs ink and paper for his Daoist calligraphy practice.",
                "required_items": {"minecraft:ink_sac": 8, "minecraft:paper": 16},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 8, "experience": 25}
            },
            {
                "task_id": "dpr_incense_fuel",
                "name": "Meditation Incense Fuel",
                "description": "The meditation hall requires coal and glowstone to maintain its spiritual atmosphere.",
                "required_items": {"minecraft:coal": 12, "minecraft:glowstone": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
        ]
    },
    {
        "npc_id": "npc_dpr_acolyte_chen",
        "name": "Acolyte Chen",
        "nameCn": "道童陈",
        "type": "sect_disciple",
        "faction": "Daoist Peak Retreat",
        "location": "daoist_peak_retreat",
        "cultivation": "CONDENSATION_LATE",
        "personality": "earnest, slightly frustrated with the slow pace, questions whether solitude is the path",
        "speech": "polite but with an undercurrent of doubt, asks indirect questions about the outside world",
        "relationship_to_wanglin": "friendly-curious — seeks news of the world beyond the peak",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "A visitor! The last one was a wandering cultivator from the Luo He Sect. He said the rivers down there have spirit fish. I have never seen a river.",
            "Hermit Wei says the Dao is found in stillness. I agree. But I also wonder if the Dao can be found in motion. He says I am not ready for that question.",
            "We grow wheat for tea and spruce for repairs. If you bring supplies, I can trade. The peak is self-sufficient, but not by much.",
            "A merchant from Tian Shui City used to come every month. He stopped three months ago. I do not know why."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Morning exercises", "location": "daoist_peak_retreat", "duration": 90},
            {"time": "06:30", "action": "Prepare tea ceremony", "location": "daoist_peak_retreat", "duration": 60},
            {"time": "07:30", "action": "Attend Hermit Wei's calligraphy", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "09:30", "action": "Herb garden maintenance", "location": "daoist_peak_retreat", "duration": 180},
            {"time": "13:00", "action": "Attend midday lecture", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "15:00", "action": "Gather spruce wood", "location": "daoist_peak_retreat", "duration": 150},
            {"time": "18:00", "action": "Cook evening meal", "location": "daoist_peak_retreat", "duration": 60},
            {"time": "19:30", "action": "Evening cultivation", "location": "daoist_peak_retreat", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "dpr_tea_herbs",
                "name": "Tea Herb Supplies",
                "description": "Acolyte Chen needs wheat and coal to maintain the peak's tea ceremony tradition.",
                "required_items": {"minecraft:wheat": 16, "minecraft:coal": 8},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 6, "experience": 20}
            },
            {
                "task_id": "dpr_wood_repair",
                "name": "Retreat Wood Supplies",
                "description": "The retreat buildings need constant repair from mountain weather. Spruce planks are essential.",
                "required_items": {"minecraft:spruce_planks": 20, "minecraft:stick": 16},
                "rewards": {"minecraft:emerald": 10, "minecraft:experience_bottle": 2, "experience": 15}
            },
        ]
    },
    {
        "npc_id": "npc_dpr_pilgrim_lin",
        "name": "Pilgrim Lin",
        "nameCn": "朝圣者林",
        "type": "rogue_cultivator",
        "faction": "None — traveling seeker",
        "location": "daoist_peak_retreat",
        "cultivation": "FOUNDATION_LATE",
        "personality": "worldly, observant, contrasts the peak with everywhere else, slightly irreverent",
        "speech": "animated, uses comparisons to other locations, asks direct questions the acolytes will not",
        "relationship_to_wanglin": "neutral — sees a fellow traveler",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "I have visited eleven peaks on this planet. This one is the quietest. The Cloud Sky Sect's peak has wind. This one has nothing. That is its value.",
            "The Snow Country Capital has ice guards who would freeze you for staring. Here, Hermit Wei would not notice if you stole everything. He does not own anything.",
            "I came from the Heavenly Fate Sect's observatory. They mapped my fate and told me to come here. I suspect they just wanted me off their mountain.",
            "Books and experience. If you have traveled, I will trade. I collect perspectives, not spirit stones."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Morning walk around the peak", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "08:00", "action": "Breakfast with acolytes", "location": "daoist_peak_retreat", "duration": 60},
            {"time": "09:00", "action": "Observe Hermit Wei's calligraphy", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "11:00", "action": "Journal writing", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "13:00", "action": "Attend lecture (as observer)", "location": "daoist_peak_retreat", "duration": 120},
            {"time": "16:00", "action": "Meditation (attempting peak style)", "location": "daoist_peak_retreat", "duration": 180},
            {"time": "20:00", "action": "Stargazing from cliff", "location": "daoist_peak_retreat", "duration": 180},
            {"time": "23:00", "action": "Sleep", "location": "daoist_peak_retreat", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "dpr_travel_knowledge",
                "name": "Travel Knowledge Exchange",
                "description": "Pilgrim Lin seeks books and experience to record in his travel journal.",
                "required_items": {"minecraft:book": 4, "minecraft:experience_bottle": 3},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
            {
                "task_id": "dpr_spirit_light",
                "name": "Spiritual Light for Meditation",
                "description": "The peak's meditation chamber needs glowstone and emeralds to sustain its spiritual illumination.",
                "required_items": {"minecraft:glowstone": 6, "minecraft:emerald": 8},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 1, "experience": 20}
            },
        ]
    },
    # ── Wandering Cultivator Camp (3 NPCs) ──
    {
        "npc_id": "npc_wcc_camp_leader_zhao",
        "name": "Camp Leader Zhao",
        "nameCn": "营地首领赵",
        "type": "rogue_cultivator",
        "faction": "Wandering Cultivator Camp",
        "location": "wandering_cultivator_camp",
        "cultivation": "FOUNDATION_MID",
        "personality": "pragmatic, survivor mentality, protective of the camp, distrustful of sects",
        "speech": "direct, no-nonsense, references survival frequently",
        "relationship_to_wanglin": "neutral-wary — sect cultivators are not trusted",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "This camp exists because the sects will not take us and the cities will not feed us. We are not failures. We are the unchosen.",
            "A Heng Yue Sect recruiter came once. He offered outer disciple positions. We told him to leave. A dog does not beg for scraps from the table it was chased from.",
            "We need bread, iron, and coal. Always. The cold does not care about cultivation bases.",
            "The Corpse Yin Sect buys bones from wandering cultivators. We do not trade with them. Some camps do. Ours does not."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Camp perimeter check", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "07:00", "action": "Morning assembly", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "08:00", "action": "Resource gathering", "location": "wandering_cultivator_camp", "duration": 240},
            {"time": "12:00", "action": "Meal distribution", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "13:00", "action": "Trade negotiations with travelers", "location": "wandering_cultivator_camp", "duration": 180},
            {"time": "16:00", "action": "Camp defense drills", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "19:00", "action": "Evening meal", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "20:00", "action": "Night watch rotation", "location": "wandering_cultivator_camp", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "wcc_camp_food",
                "name": "Camp Food Supplies",
                "description": "The camp needs bread and wheat to feed its residents through the coming weeks.",
                "required_items": {"minecraft:bread": 12, "minecraft:wheat": 16},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 6, "experience": 20}
            },
            {
                "task_id": "wcc_camp_tools",
                "name": "Camp Tool Repair",
                "description": "Camp Leader Zhao needs iron and coal to repair the camp's worn tools and weapons.",
                "required_items": {"minecraft:iron_ingot": 8, "minecraft:coal": 10},
                "rewards": {"ergenverse:spirit_stone": 1, "minecraft:emerald": 7, "experience": 20}
            },
        ]
    },
    {
        "npc_id": "npc_wcc_herb_gatherer_su",
        "name": "Herb Gatherer Su",
        "nameCn": "采药人苏",
        "type": "rogue_cultivator",
        "faction": "Wandering Cultivator Camp",
        "location": "wandering_cultivator_camp",
        "cultivation": "CONDENSATION_PEAK",
        "personality": "knowledgeable about herbs, quiet, speaks only when she has something worth saying",
        "speech": "soft-spoken, precise, often interrupts herself to correct herb names",
        "relationship_to_wanglin": "neutral-interested — seeks knowledge exchange",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": True,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "I gather qi_gathering_grass. The sects use it for foundation pills. We use it for tea. The same herb, different purposes, same result.",
            "A Soul Refining Sect disciple once tried to buy my entire stock. I refused. He said the sect always gets what it wants. I moved the camp. He did not follow.",
            "Spirit stones and bones. I trade herbs for cultivation resources. The sects have gardens. We have the wild.",
            "A monk from the Xuan Dao Sect taught me to identify thirty-seven herb species by root texture alone. He said the eyes deceive. The roots do not."
        ],
        "daily_schedule": [
            {"time": "05:30", "action": "Pre-dawn herb foraging", "location": "wandering_cultivator_camp", "duration": 180},
            {"time": "08:30", "action": "Herb processing and drying", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "10:30", "action": "Herb classification", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "13:00", "action": "Trade with other wanderers", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "15:00", "action": "Cultivation with herbs", "location": "wandering_cultivator_camp", "duration": 180},
            {"time": "18:30", "action": "Prepare herbal medicine", "location": "wandering_cultivator_camp", "duration": 90},
            {"time": "20:00", "action": "Record herb observations", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "22:00", "action": "Sleep", "location": "wandering_cultivator_camp", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "wcc_herbs_for_stones",
                "name": "Herb to Spirit Stone Trade",
                "description": "Herb Gatherer Su needs spirit stones and bones to maintain her cultivation while gathering.",
                "required_items": {"ergenverse:spirit_stone": 2, "minecraft:bone": 6},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 2, "experience": 25}
            },
            {
                "task_id": "wcc_herb_processing",
                "name": "Herb Processing Supplies",
                "description": "Su needs emeralds and books to trade for rare seeds and record her findings.",
                "required_items": {"minecraft:emerald": 10, "minecraft:book": 3},
                "rewards": {"ergenverse:qi_gathering_grass": 4, "minecraft:emerald": 6, "experience": 20}
            },
        ]
    },
    {
        "npc_id": "npc_wcc_craftsman_liu",
        "name": "Craftsman Liu",
        "nameCn": "工匠刘",
        "type": "mortal",
        "faction": "Wandering Cultivator Camp",
        "location": "wandering_cultivator_camp",
        "cultivation": "MORTAL",
        "personality": "practical, unimpressed by cultivation, takes pride in mundane skills",
        "speech": "blunt, uses metaphors from blacksmithing and woodworking",
        "relationship_to_wanglin": "neutral-friendly — judges by actions, not cultivation level",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "I am mortal. I forge tools. The cultivators here need storage pouches, iron tools, and repair work. I provide. They pay in emeralds and spirit stones I cannot use.",
            "A merchant from Great Wang Capital once visited. He looked at my work and said it was 'adequate for a mortal.' I charged him double.",
            "Iron and emeralds. If you have them, I can repair anything. If you do not, go find a cultivator. They can break things but not fix them.",
            "The Wang Family Village had a blacksmith better than me. I trained him. He left for the city. Cities take everything good."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Forge setup", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "07:00", "action": "Tool forging and repair", "location": "wandering_cultivator_camp", "duration": 240},
            {"time": "11:00", "action": "Breakfast", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "12:00", "action": "Repair camp equipment", "location": "wandering_cultivator_camp", "duration": 180},
            {"time": "15:00", "action": "Trade finished goods", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "17:00", "action": "Gather materials", "location": "wandering_cultivator_camp", "duration": 120},
            {"time": "19:00", "action": "Evening meal", "location": "wandering_cultivator_camp", "duration": 60},
            {"time": "20:00", "action": "Sleep", "location": "wandering_cultivator_camp", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "wcc_forge_supplies",
                "name": "Forge Material Supply",
                "description": "Craftsman Liu needs iron and emeralds to keep the camp's forge running.",
                "required_items": {"minecraft:iron_ingot": 10, "minecraft:emerald": 12},
                "rewards": {"ergenverse:storage_pouch": 1, "minecraft:emerald": 8, "experience": 15}
            },
            {
                "task_id": "wcc_tool_trade",
                "name": "Tool Manufacturing Trade",
                "description": "Liu can craft tools in exchange for experience bottles and storage pouches.",
                "required_items": {"minecraft:experience_bottle": 3, "ergenverse:storage_pouch": 1},
                "rewards": {"minecraft:iron_ingot": 8, "minecraft:emerald": 10, "experience": 20}
            },
        ]
    },
    # ── Mortal Dust Outpost (3 NPCs) ──
    {
        "npc_id": "npc_mdo_outpost_head_qian",
        "name": "Outpost Head Qian",
        "nameCn": "哨所所长钱",
        "type": "mortal",
        "faction": "Mortal Dust Outpost",
        "location": "mortal_dust_outpost",
        "cultivation": "MORTAL",
        "personality": "exhausted, duty-bound, sees the outpost as both refuge and prison",
        "speech": "tired, matter-of-fact, occasionally bitter about the outpost's isolation",
        "relationship_to_wanglin": "neutral-hopeful — any cultivator who arrives might bring news",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Welcome to the Mortal Dust Outpost. The name is accurate. We are at the edge of everything. Cultivators pass through. Mortals stay.",
            "A Nan Dou City merchant brings supplies once a month. If he is late, we eat less. Last month he was late. We ate less.",
            "Bread, coal, iron. The outpost survives on these three things. If you have them, you are useful. If you do not, you are another mouth.",
            "The Teng family collects taxes from us. Cultivators do not pay taxes. We pay for them."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Dawn inspection", "location": "mortal_dust_outpost", "duration": 90},
            {"time": "06:30", "action": "Supply inventory", "location": "mortal_dust_outpost", "duration": 60},
            {"time": "08:00", "action": "Supervise outpost repairs", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "11:00", "action": "Meet with travelers", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "13:00", "action": "Meal and rest", "location": "mortal_dust_outpost", "duration": 90},
            {"time": "15:00", "action": "Patrol perimeter", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "18:00", "action": "Evening report", "location": "mortal_dust_outpost", "duration": 60},
            {"time": "20:00", "action": "Sleep", "location": "mortal_dust_outpost", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "mdo_food_supply",
                "name": "Outpost Food Reserves",
                "description": "Outpost Head Qian needs bread and wheat to sustain the outpost through the next supply cycle.",
                "required_items": {"minecraft:bread": 16, "minecraft:wheat": 20},
                "rewards": {"minecraft:emerald": 10, "minecraft:book": 1, "experience": 15}
            },
            {
                "task_id": "mdo_repair_materials",
                "name": "Outpost Structure Repair",
                "description": "The outpost walls and buildings need constant repair. Planks and sticks are essential.",
                "required_items": {"minecraft:oak_planks": 24, "minecraft:stick": 20},
                "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 1, "experience": 10}
            },
        ]
    },
    {
        "npc_id": "npc_mdo_traveling_merchant_he",
        "name": "Traveling Merchant He",
        "nameCn": "行商何",
        "type": "mortal",
        "faction": "Nan Dou City Merchant Guild",
        "location": "mortal_dust_outpost",
        "cultivation": "MORTAL",
        "personality": "opportunistic but fair, knows every trade route on the planet, enjoys gossip",
        "speech": "fast-talking, friendly, always steering conversation toward trade",
        "relationship_to_wanglin": "neutral-friendly — a customer is a customer",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Merchant He, at your service. I supply this outpost from Nan Dou City every month. The road is dangerous, the pay is mediocre, and the company is... sparse.",
            "I have traded in every city on this planet. Tian Shui has the best fish. Qilin has the strongest beasts. This outpost has the most desperate buyers.",
            "Iron and emeralds move fastest here. The outpost always needs tools, and they pay premium because they have no other choice.",
            "A Soul Refining Sect envoy passed through last year. He bought all my coal. He did not say why. I did not ask."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Unload cargo", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "08:00", "action": "Trade with outpost residents", "location": "mortal_dust_outpost", "duration": 240},
            {"time": "12:00", "action": "Meal", "location": "mortal_dust_outpost", "duration": 60},
            {"time": "13:00", "action": "Negotiate supply contracts", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "16:00", "action": "Pack remaining goods", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "18:00", "action": "Evening meal and gossip", "location": "mortal_dust_outpost", "duration": 90},
            {"time": "20:00", "action": "Inventory accounting", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "22:00", "action": "Sleep", "location": "mortal_dust_outpost", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "mdo_merchant_iron",
                "name": "Merchant Iron Trade",
                "description": "Merchant He needs iron and emeralds to trade with the outpost and other settlements.",
                "required_items": {"minecraft:iron_ingot": 8, "minecraft:emerald": 10},
                "rewards": {"minecraft:emerald": 14, "minecraft:book": 2, "experience": 15}
            },
            {
                "task_id": "mdo_exotic_goods",
                "name": "Exotic Goods Acquisition",
                "description": "He seeks coal and iron to fill his cargo for the return trip to Nan Dou City.",
                "required_items": {"minecraft:coal": 16, "minecraft:iron_ingot": 6},
                "rewards": {"minecraft:emerald": 16, "minecraft:experience_bottle": 2, "experience": 20}
            },
        ]
    },
    {
        "npc_id": "npc_mdo_retired_guard_fang",
        "name": "Retired Guard Fang",
        "nameCn": "退役卫兵方",
        "type": "mortal",
        "faction": "Mortal Dust Outpost",
        "location": "mortal_dust_outpost",
        "cultivation": "MORTAL",
        "personality": "grizzled, nostalgic, haunted by one battle he will not describe",
        "speech": "slow, pauses often, references 'the old days' with a mixture of pride and regret",
        "relationship_to_wanglin": "neutral-respectful — respects anyone who can fight",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "I guarded the Great Wang Capital for thirty years. Now I guard a wooden wall at the edge of nowhere. The pay is worse. The quiet is better.",
            "The Teng family's guard captain was a boy when I started. Now he commands three hundred men. I command a gate that nobody attacks.",
            "Arrows and coal. If you have them, I will tell you what I know about the roads between here and the capital. The knowledge is worth more than the arrows.",
            "A Fighting Evil Sect patrol came through once. They said there was corruption in the hills nearby. They burned something. I did not ask what."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Dawn watch", "location": "mortal_dust_outpost", "duration": 120},
            {"time": "07:00", "action": "Weapon maintenance", "location": "mortal_dust_outpost", "duration": 90},
            {"time": "09:00", "action": "Perimeter patrol", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "12:00", "action": "Meal", "location": "mortal_dust_outpost", "duration": 60},
            {"time": "13:00", "action": "Train outpost militia", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "16:00", "action": "Rest and tell stories", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "19:00", "action": "Evening watch", "location": "mortal_dust_outpost", "duration": 180},
            {"time": "22:00", "action": "Sleep", "location": "mortal_dust_outpost", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "mdo_guard_arrows",
                "name": "Guard Arrow Supply",
                "description": "Retired Guard Fang needs arrows and coal to maintain the outpost's defenses.",
                "required_items": {"minecraft:arrow": 32, "minecraft:coal": 8},
                "rewards": {"minecraft:emerald": 8, "minecraft:book": 1, "experience": 15}
            },
            {
                "task_id": "mdo_road_knowledge",
                "name": "Road Intelligence",
                "description": "Fang will share knowledge of the surrounding roads in exchange for iron and books.",
                "required_items": {"minecraft:iron_ingot": 6, "minecraft:book": 2},
                "rewards": {"minecraft:emerald": 12, "minecraft:experience_bottle": 1, "experience": 20}
            },
        ]
    },
    # ── Foreign War Monument (3 NPCs) ──
    {
        "npc_id": "npc_fwm_monument_keeper_sen",
        "name": "Monument Keeper Sen",
        "nameCn": "纪念碑守卫森",
        "type": "rogue_cultivator",
        "faction": "None — self-appointed guardian",
        "location": "foreign_war_monument",
        "cultivation": "NASCENT_SOUL_EARLY",
        "personality": "solemn, obsessive about preservation, treats the monument as a living thing",
        "speech": "formal, ceremonial, speaks as though reciting a liturgy",
        "relationship_to_wanglin": "neutral-assessing — evaluates whether the visitor is worthy of the monument",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "You stand before the Monument of the Foreign War. One million cultivators died here. The monument remembers them. I remember the monument.",
            "The Immortal Court Palace registered this site as a memorial. They sent one envoy. He left a plaque. He did not stay. The dead do not pay taxes.",
            "Bones and iron. These are what remain. If you bring bones, I will inter them properly. If you bring iron, I will repair the monument.",
            "A Vermilion Bird Capital historian visited once. She counted the names on the monument for three days. On the fourth day, she wept. She said she could not read the last ten thousand names. They had been worn away by time."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn monument inspection", "location": "foreign_war_monument", "duration": 120},
            {"time": "06:00", "action": "Clean monument surfaces", "location": "foreign_war_monument", "duration": 180},
            {"time": "09:00", "action": "Collect scattered bones", "location": "foreign_war_monument", "duration": 180},
            {"time": "12:00", "action": "Proper burial ceremony", "location": "foreign_war_monument", "duration": 120},
            {"time": "14:00", "action": "Repair monument structure", "location": "foreign_war_monument", "duration": 180},
            {"time": "17:00", "action": "Record weathering damage", "location": "foreign_war_monument", "duration": 120},
            {"time": "19:00", "action": "Evening vigil", "location": "foreign_war_monument", "duration": 180},
            {"time": "22:00", "action": "Night meditation among the dead", "location": "foreign_war_monument", "duration": 360},
        ],
        "sect_tasks": [
            {
                "task_id": "fwm_bone_burial",
                "name": "Bone Interment",
                "description": "Keeper Sen needs bones and cobblestone to properly inter remains found near the monument.",
                "required_items": {"minecraft:bone": 16, "minecraft:cobblestone": 20},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 8, "experience": 25}
            },
            {
                "task_id": "fwm_monument_repair",
                "name": "Monument Restoration",
                "description": "The monument needs iron and coal to repair damage from centuries of weathering.",
                "required_items": {"minecraft:iron_ingot": 10, "minecraft:coal": 12},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 1, "experience": 30}
            },
        ]
    },
    {
        "npc_id": "npc_fwm_battle_scholar_yun",
        "name": "Battle Scholar Yun",
        "nameCn": "战史学者云",
        "type": "rogue_cultivator",
        "faction": "Vermilion Bird Academy (visiting)",
        "location": "foreign_war_monument",
        "cultivation": "FOUNDATION_PEAK",
        "personality": "academic, obsessive about military history, treats war as a puzzle to solve",
        "speech": "analytical, quotes casualty figures, draws battle diagrams in the dirt",
        "relationship_to_wanglin": "neutral-curious — interested in any firsthand combat experience",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "The Foreign War. Forty-seven years. Twenty-three nations. One million confirmed dead. The numbers are debated. The dead are not.",
            "I was sent by the Vermilion Bird Academy to study this monument. Specifically, to determine why the Heavenly Fate Sect's predictions failed to foresee the invasion.",
            "Books and emeralds. I need materials for my research. In return, I can teach you what the monument's inscriptions reveal about ancient formations.",
            "A Fighting Evil Sect historian comes every year. He studies the weapons. He says the war was won by iron, not cultivation. I say it was won by those who had no cultivation left to lose."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Morning study of inscriptions", "location": "foreign_war_monument", "duration": 180},
            {"time": "09:00", "action": "Transcribe monument text", "location": "foreign_war_monument", "duration": 180},
            {"time": "12:00", "action": "Meal and analysis", "location": "foreign_war_monument", "duration": 60},
            {"time": "13:00", "action": "Cross-reference with archives", "location": "foreign_war_monument", "duration": 180},
            {"time": "16:00", "action": "Interview Keeper Sen", "location": "foreign_war_monument", "duration": 120},
            {"time": "18:00", "action": "Draw battle diagrams", "location": "foreign_war_monument", "duration": 120},
            {"time": "20:00", "action": "Evening reading", "location": "foreign_war_monument", "duration": 180},
            {"time": "23:00", "action": "Sleep", "location": "foreign_war_monument", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "fwm_research_materials",
                "name": "Battle Research Materials",
                "description": "Scholar Yun needs books and emeralds to continue his research on the Foreign War.",
                "required_items": {"minecraft:book": 6, "minecraft:emerald": 12},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
            {
                "task_id": "fwm_formation_study",
                "name": "Ancient Formation Analysis",
                "description": "Yun needs emeralds and experience bottles to test formation theories derived from the monument.",
                "required_items": {"minecraft:emerald": 10, "minecraft:experience_bottle": 3},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 8, "experience": 25}
            },
        ]
    },
    {
        "npc_id": "npc_fwm_scavenger_gai",
        "name": "Scavenger Gai",
        "nameCn": "拾荒者盖",
        "type": "mortal",
        "faction": "None — independent",
        "location": "foreign_war_monument",
        "cultivation": "MORTAL",
        "personality": "superficially cheerful, deeply superstitious, treats the battlefield as a workplace",
        "speech": "casual, uses dark humor, whistles to keep spirits up",
        "relationship_to_wanglin": "neutral-friendly — anyone who is not dead is good company",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Do not tell the keeper, but I salvage from the monument. Not the monument itself. The ground around it. Swords, armor, arrowheads. The war left plenty.",
            "The Ancient God Cave is three days north. Scavengers who go there do not come back. This is safer. The dead here are already dead.",
            "Iron and arrows. If you find any, I will buy them. The keeper buries bones. I sell iron. We have an understanding.",
            "A Corpse Yin Sect merchant visited once. He wanted to buy bones. The keeper refused. The merchant offered me triple rate. I refused too. Some money is not worth touching."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Breakfast and prepare tools", "location": "foreign_war_monument", "duration": 60},
            {"time": "07:00", "action": "Scavenge battlefield perimeter", "location": "foreign_war_monument", "duration": 240},
            {"time": "11:00", "action": "Sort and clean finds", "location": "foreign_war_monument", "duration": 120},
            {"time": "13:00", "action": "Meal", "location": "foreign_war_monument", "duration": 60},
            {"time": "14:00", "action": "Trade with passing travelers", "location": "foreign_war_monument", "duration": 120},
            {"time": "16:00", "action": "Deep scavenging (careful zones)", "location": "foreign_war_monument", "duration": 180},
            {"time": "19:00", "action": "Evening count and inventory", "location": "foreign_war_monument", "duration": 60},
            {"time": "20:00", "action": "Sleep", "location": "foreign_war_monument", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "fwm_salvage_trade",
                "name": "Salvage Trade",
                "description": "Gai trades salvaged iron for emeralds and coal. Bring iron ingots and arrows.",
                "required_items": {"minecraft:iron_ingot": 6, "minecraft:arrow": 24},
                "rewards": {"minecraft:emerald": 12, "minecraft:coal": 8, "experience": 15}
            },
            {
                "task_id": "fwm_safe_scavenging",
                "name": "Safe Zone Scavenging Supplies",
                "description": "Gai needs coal and cobblestone to safely excavate items from deeper zones.",
                "required_items": {"minecraft:coal": 10, "minecraft:cobblestone": 16},
                "rewards": {"minecraft:iron_ingot": 6, "minecraft:emerald": 8, "experience": 15}
            },
        ]
    },
    # ── Foreign Void Rift (3 NPCs) ──
    {
        "npc_id": "npc_fvr_rift_watcher_mo",
        "name": "Rift Watcher Mo",
        "nameCn": "裂隙守望者莫",
        "type": "rogue_cultivator",
        "faction": "Immortal Court (assigned)",
        "location": "foreign_void_rift",
        "cultivation": "NASCENT_SOUL_LATE",
        "personality": "haunted, speaks in spatial metaphors, has seen things through the rift that changed him",
        "speech": "distant, sometimes pauses mid-sentence as if listening to something from the other side",
        "relationship_to_wanglin": "neutral-wary — the rift shows him things about people he does not want to see",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "The Immortal Court assigned me to watch the rift. That was forty years ago. The rift does not close. I do not leave. This is the assignment.",
            "The Karma Crystal Formation Observer came here once. He looked into the rift for ten seconds. He left without speaking. I have looked into it every day for forty years. I still cannot describe what I see.",
            "Obsidian and ender pearls. The rift consumes both. If you bring them, I will explain what happens when something falls through.",
            "The Soul Refining Sect offered to send a soul through. The Immortal Court refused. I think the Court was right. I think the Sect was right too. Both can be right when the question has no good answer."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Rift observation (pre-dawn is calmest)", "location": "foreign_void_rift", "duration": 240},
            {"time": "07:00", "action": "Record rift fluctuations", "location": "foreign_void_rift", "duration": 120},
            {"time": "09:00", "action": "Maintain containment arrays", "location": "foreign_void_rift", "duration": 180},
            {"time": "12:00", "action": "Meal", "location": "foreign_void_rift", "duration": 60},
            {"time": "13:00", "action": "Analyze rift edge samples", "location": "foreign_void_rift", "duration": 180},
            {"time": "16:00", "action": "Second observation period", "location": "foreign_void_rift", "duration": 180},
            {"time": "20:00", "action": "Night observation", "location": "foreign_void_rift", "duration": 300},
            {"time": "01:00", "action": "Brief rest", "location": "foreign_void_rift", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "fvr_containment_supplies",
                "name": "Containment Array Fuel",
                "description": "Watcher Mo needs obsidian and ender pearls to maintain the rift's containment arrays.",
                "required_items": {"minecraft:obsidian": 12, "minecraft:ender_pearl": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
            {
                "task_id": "fvr_rift_analysis",
                "name": "Rift Edge Analysis",
                "description": "Mo needs emeralds and cobblestone to construct analysis equipment for rift samples.",
                "required_items": {"minecraft:emerald": 10, "minecraft:cobblestone": 16},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:ender_pearl": 2, "experience": 25}
            },
        ]
    },
    {
        "npc_id": "npc_fvr_void_touched_girl_xiao",
        "name": "Void-Touched Girl Xiao",
        "nameCn": "虚空之女萧",
        "type": "rogue_cultivator",
        "faction": "None",
        "location": "foreign_void_rift",
        "cultivation": "FOUNDATION_MID",
        "personality": "dreamy, partially detached from reality, sees things others cannot",
        "speech": "soft, often describes things that are not there, occasionally terrifyingly lucid",
        "relationship_to_wanglin": "neutral-fascinated — sees something in the player she cannot articulate",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "I fell into the rift when I was seven. I was pulled out three days later. I was still seven. I am still seven inside. The rift does not let you age past the moment you enter.",
            "The Heavenly Fate Sect tested me. They said my fate was 'undefined.' They asked me to leave. The Xuan Dao Sect tested me too. They said my Dao was 'fractured.' They asked me to stay. I chose the rift.",
            "Coal and emeralds. The watcher says I need to eat. I do not feel hunger. But he insists. So I trade what the rift gives me for things I do not need.",
            "There is a city on the other side. I have seen it. The buildings are made of light. The people have no faces. They wave at me. I do not wave back."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Sit near the rift edge", "location": "foreign_void_rift", "duration": 300},
            {"time": "10:00", "action": "Draw what she sees", "location": "foreign_void_rift", "duration": 180},
            {"time": "13:00", "action": "Eat (watcher insists)", "location": "foreign_void_rift", "duration": 60},
            {"time": "14:00", "action": "Collect rift residue", "location": "foreign_void_rift", "duration": 180},
            {"time": "17:00", "action": "Talk to the watcher", "location": "foreign_void_rift", "duration": 120},
            {"time": "19:00", "action": "Watch the rift darken", "location": "foreign_void_rift", "duration": 180},
            {"time": "22:00", "action": "Sleep near the rift", "location": "foreign_void_rift", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "fvr_rift_residue",
                "name": "Rift Residue Collection",
                "description": "Xiao collects materials that seep through the rift. She needs coal and emeralds for the watcher's insistence on meals.",
                "required_items": {"minecraft:coal": 8, "minecraft:emerald": 6},
                "rewards": {"minecraft:ender_pearl": 2, "minecraft:book": 1, "experience": 20}
            },
            {
                "task_id": "fvr_void_trade",
                "name": "Void Material Trade",
                "description": "Xiao trades obsidian and cobblestone for items she collects from the rift's edge.",
                "required_items": {"minecraft:obsidian": 8, "minecraft:cobblestone": 12},
                "rewards": {"minecraft:ender_pearl": 3, "minecraft:emerald": 8, "experience": 20}
            },
        ]
    },
    {
        "npc_id": "npc_fvr_court_envoy_tang",
        "name": "Court Envoy Tang",
        "nameCn": "仙廷使臣唐",
        "type": "sect_disciple",
        "faction": "Immortal Court",
        "location": "foreign_void_rift",
        "cultivation": "SOUL_TRANSFORMATION_EARLY",
        "personality": "bureaucratic, views the rift as a security problem, uncomfortable with the watcher's attachment",
        "speech": "precise, uses legal and administrative language, avoids emotional topics",
        "relationship_to_wanglin": "neutral-official — registers all visitors per Immortal Court protocol",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Immortal Court Envoy Tang. This rift is classified as a Tier-3 spatial hazard. You are within the exclusion zone. Your presence is noted.",
            "The Vermilion Bird Capital ordered this rift sealed fourteen hundred years ago. The seal fails every century. We replace it. The rift does not care about seals.",
            "I need obsidian and iron for the next seal reinforcement. The Court provides funds. The watcher provides knowledge. You can provide labor.",
            "A Snow Country military unit attempted to use the rift as a weapon. They sent soldiers through. None returned. The Snow Country does not attempt that anymore."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Inspect seal integrity", "location": "foreign_void_rift", "duration": 120},
            {"time": "08:00", "action": "Administrative reports", "location": "foreign_void_rift", "duration": 120},
            {"time": "10:00", "action": "Supervise seal maintenance", "location": "foreign_void_rift", "duration": 180},
            {"time": "13:00", "action": "Meal", "location": "foreign_void_rift", "duration": 60},
            {"time": "14:00", "action": "Interview the watcher", "location": "foreign_void_rift", "duration": 120},
            {"time": "16:00", "action": "Prepare seal materials", "location": "foreign_void_rift", "duration": 180},
            {"time": "19:00", "action": "Evening inspection", "location": "foreign_void_rift", "duration": 60},
            {"time": "20:00", "action": "Cultivation", "location": "foreign_void_rift", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "fvr_seal_materials",
                "name": "Seal Reinforcement Materials",
                "description": "Envoy Tang needs obsidian and iron for the next century's seal reinforcement cycle.",
                "required_items": {"minecraft:obsidian": 16, "minecraft:iron_ingot": 8},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
            {
                "task_id": "fvr_court_reports",
                "name": "Court Documentation",
                "description": "Tang needs books and emeralds to prepare the annual Immortal Court report on rift status.",
                "required_items": {"minecraft:book": 4, "minecraft:emerald": 10},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
        ]
    },
    # ── Foreign Soul Crystal Deposit (3 NPCs) ──
    {
        "npc_id": "npc_fsc_crystal_miner_pang",
        "name": "Crystal Miner Pang",
        "nameCn": "晶矿工庞",
        "type": "rogue_cultivator",
        "faction": "Independent mining collective",
        "location": "foreign_soul_crystal_deposit",
        "cultivation": "FOUNDATION_LATE",
        "personality": "gruff, possessive of his mining claims, knows the deposit better than anyone",
        "speech": "terse, uses mining terminology, suspicious of outsiders who want 'free crystals'",
        "relationship_to_wanglin": "neutral-suspicious — another claim jumper until proven otherwise",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "This deposit was found thirty years ago by a Tian Shui City prospector. He died on the second day. The crystals are not dangerous. The things that guard them are.",
            "Amethyst and quartz. That is what we mine. The Soul Refining Sect wants the crystals for soul work. The Xuan Dao Sect wants them for formations. I want emeralds. Everyone wants something.",
            "Iron and coal. Mining tools wear out. If you have them, I will pay. If you do not, stay out of my tunnels.",
            "A Corpse Yin Sect envoy offered to animate the dead miners. We refused. The dead stay dead here. That is the rule."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn mine inspection", "location": "foreign_soul_crystal_deposit", "duration": 90},
            {"time": "06:00", "action": "Mining operations", "location": "foreign_soul_crystal_deposit", "duration": 300},
            {"time": "11:00", "action": "Ore processing", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "13:00", "action": "Meal and rest", "location": "foreign_soul_crystal_deposit", "duration": 90},
            {"time": "14:30", "action": "Trade with buyers", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "17:00", "action": "Deep mine exploration", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "20:00", "action": "Evening count and security", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "22:00", "action": "Sleep", "location": "foreign_soul_crystal_deposit", "duration": 360},
        ],
        "sect_tasks": [
            {
                "task_id": "fsc_mining_tools",
                "name": "Mining Tool Supply",
                "description": "Miner Pang needs iron and coal to replace worn mining equipment.",
                "required_items": {"minecraft:iron_ingot": 10, "minecraft:coal": 12},
                "rewards": {"minecraft:amethyst_shard": 8, "minecraft:emerald": 6, "experience": 20}
            },
            {
                "task_id": "fsc_crystal_trade",
                "name": "Crystal Exchange",
                "description": "Pang trades amethyst and quartz for emeralds and iron.",
                "required_items": {"minecraft:emerald": 10, "minecraft:iron_ingot": 6},
                "rewards": {"minecraft:amethyst_shard": 12, "minecraft:quartz": 10, "experience": 20}
            },
        ]
    },
    {
        "npc_id": "npc_fsc_soul_researcher_yan",
        "name": "Soul Researcher Yan",
        "nameCn": "魂魄研究者燕",
        "type": "rogue_cultivator",
        "faction": "None — independent scholar",
        "location": "foreign_soul_crystal_deposit",
        "cultivation": "NASCENT_SOUL_EARLY",
        "personality": "intense, speaks rapidly about soul-crystal resonance, loses track of time",
        "speech": "academic, fills silence with theories, rarely waits for responses",
        "relationship_to_wanglin": "neutral-intrigued — seeks test subjects for resonance experiments",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": True,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Soul crystals are not ordinary crystals. They resonate at the frequency of spiritual consciousness. The Soul Refining Sect knows this. They mine them in bulk. They are wrong. Bulk mining destroys the resonance pattern.",
            "I was expelled from the Xuan Dao Sect for proposing that soul crystals predate the formation. They said I lacked evidence. I said they lacked imagination.",
            "Amethyst, quartz, and glowstone. These are what I need for my experiments. If you bring them, I will share what the resonance patterns reveal.",
            "The Heavenly Fate Sect predicts the future with stars. I predict it with crystals. Both are imprecise. Crystals are faster."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Crystal resonance measurement", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "08:00", "action": "Experiment documentation", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "10:00", "action": "Field sampling", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "13:00", "action": "Meal (often skipped)", "location": "foreign_soul_crystal_deposit", "duration": 30},
            {"time": "13:30", "action": "Resonance experiments", "location": "foreign_soul_crystal_deposit", "duration": 240},
            {"time": "18:00", "action": "Compare findings with prior data", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "20:00", "action": "Evening meditation with crystals", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "23:00", "action": "Sleep", "location": "foreign_soul_crystal_deposit", "duration": 360},
        ],
        "sect_tasks": [
            {
                "task_id": "fsc_resonance_materials",
                "name": "Resonance Experiment Materials",
                "description": "Researcher Yan needs amethyst, quartz, and glowstone for soul crystal resonance experiments.",
                "required_items": {"minecraft:amethyst_shard": 10, "minecraft:quartz": 8, "minecraft:glowstone": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:soul_fragment": 1, "experience": 30}
            },
            {
                "task_id": "fsc_theory_trade",
                "name": "Crystal Theory Exchange",
                "description": "Yan trades knowledge for emeralds and books needed for further research.",
                "required_items": {"minecraft:emerald": 10, "minecraft:book": 3},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
        ]
    },
    {
        "npc_id": "npc_fsc_deposit_guard_sun",
        "name": "Deposit Guard Sun",
        "nameCn": "矿场守卫孙",
        "type": "rogue_cultivator",
        "faction": "Independent mining collective",
        "location": "foreign_soul_crystal_deposit",
        "cultivation": "CONDENSATION_PEAK",
        "personality": "alert, professional, treats the deposit as a military installation",
        "speech": "military-style, gives orders, reports status concisely",
        "relationship_to_wanglin": "neutral-assessing — classifies visitors by threat level",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 1,
        "derivation_type": "I",
        "initiation_lines": [
            "Halt. This is a restricted mining zone. State your business or leave. The crystals attract things. Not all of them are friendly.",
            "I trained under the Fighting Evil Sect for six years. They taught me to identify threats. This deposit has three: cave-ins, crystal guardians, and other miners.",
            "Iron and obsidian. Guard equipment needs constant replacement. The crystal guardians are not aggressive, but they are persistent.",
            "A Luo He Sect merchant tried to buy the deposit's mining rights. The miners refused. He said water always finds a way. We said crystal is harder than water."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn perimeter sweep", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "06:00", "action": "Guard post duty", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "09:00", "action": "Escort miners to deep tunnels", "location": "foreign_soul_crystal_deposit", "duration": 180},
            {"time": "12:00", "action": "Meal", "location": "foreign_soul_crystal_deposit", "duration": 60},
            {"time": "13:00", "action": "Guard crystal processing area", "location": "foreign_soul_crystal_deposit", "duration": 240},
            {"time": "17:00", "action": "Evening perimeter check", "location": "foreign_soul_crystal_deposit", "duration": 120},
            {"time": "19:00", "action": "Night watch briefing", "location": "foreign_soul_crystal_deposit", "duration": 60},
            {"time": "20:00", "action": "Night patrol", "location": "foreign_soul_crystal_deposit", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "fsc_guard_equipment",
                "name": "Guard Equipment Supply",
                "description": "Guard Sun needs iron and obsidian to maintain guard equipment and tunnel defenses.",
                "required_items": {"minecraft:iron_ingot": 8, "minecraft:obsidian": 10},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 8, "experience": 25}
            },
            {
                "task_id": "fsc_tunnel_security",
                "name": "Tunnel Security Materials",
                "description": "Sun needs coal and emeralds for tunnel lighting and security infrastructure.",
                "required_items": {"minecraft:coal": 12, "minecraft:emerald": 8},
                "rewards": {"minecraft:amethyst_shard": 6, "minecraft:emerald": 6, "experience": 20}
            },
        ]
    },
]

def make_salt(npc_id):
    """Deterministic salt from NPC ID."""
    return hashlib.md5(npc_id.encode()).hexdigest()[:16]

def build_loot_table(name, data):
    """Build a Minecraft loot table JSON."""
    pools = []
    for pool_data in data["pools"]:
        entries = []
        for entry in pool_data["entries"]:
            if isinstance(entry, tuple):
                item_name = entry[0]
                weight = entry[1]
                count = entry[2] if len(entry) > 2 else None
                e = {"type": "minecraft:item", "weight": weight, "name": item_name}
                if count:
                    e["functions"] = [{"function": "minecraft:set_count", "count": {"min": count[0], "max": count[1]}}]
                else:
                    e["functions"] = []
                entries.append(e)
            else:
                # string entry (empty)
                entries.append({"type": entry, "weight": 1})
        pool = {
            "rolls": {"min": pool_data["rolls"][0], "max": pool_data["rolls"][1]},
            "entries": entries
        }
        pools.append(pool)
    return {"pools": pools}

def build_npc(npc_data):
    """Build a complete NPC JSON."""
    salt = make_salt(npc_data["npc_id"])
    npc = {
        "npc_id": npc_data["npc_id"],
        "name": npc_data["name"],
        "nameCn": npc_data["nameCn"],
        "canon_id": "",
        "type": npc_data["type"],
        "faction": npc_data["faction"],
        "location": npc_data["location"],
        "cultivation": npc_data["cultivation"],
        "personality": npc_data["personality"],
        "speech": npc_data["speech"],
        "relationship_to_wanglin": npc_data["relationship_to_wanglin"],
        "dialogue_available": npc_data["dialogue_available"],
        "quest_available": npc_data["quest_available"],
        "trade_available": npc_data["trade_available"],
        "teaching_available": npc_data["teaching_available"],
        "canon_confidence": npc_data["canon_confidence"],
        "derivation_type": npc_data["derivation_type"],
        "salt": salt,
        "initiation_lines": npc_data["initiation_lines"],
        "daily_schedule": npc_data["daily_schedule"],
        "sect_tasks": npc_data["sect_tasks"],
        "_xianxia_schema": 1
    }
    return npc

def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)

    # Write loot tables (skip wandering_cultivator_camp — orphan preserved)
    loot_count = 0
    for name, data in LOOT_TABLES.items():
        filepath = os.path.join(LOOT_DIR, f"{name}.json")
        loot = build_loot_table(name, data)
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(loot, f, indent=2, ensure_ascii=False)
        loot_count += 1
        print(f"  Loot: {name}.json")

    print(f"\nTotal loot tables written: {loot_count} (1 orphan preserved: wandering_cultivator.json)")

    # Write NPCs
    npc_count = 0
    for npc_data in NPCS:
        npc = build_npc(npc_data)
        filepath = os.path.join(NPC_DIR, f"{npc_data['npc_id']}.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
        npc_count += 1
        print(f"  NPC: {npc_data['npc_id']}.json")

    print(f"\nTotal NPCs written: {npc_count}")
    print(f"\nLocations completed this cycle:")
    print(f"  1. daoist_peak_retreat (MEDIUM) — 1 loot + 3 NPCs")
    print(f"  2. wandering_cultivator_camp (LOW) — 1 orphan loot (preserved) + 3 NPCs")
    print(f"  3. mortal_dust_outpost (LOW) — 1 loot + 3 NPCs")
    print(f"  4. foreign_war_monument (LOW-MED) — 1 loot + 3 NPCs")
    print(f"  5. foreign_void_rift (LOW) — 1 loot + 3 NPCs")
    print(f"  6. foreign_soul_crystal_deposit (LOW-MED) — 1 loot + 3 NPCs")
    print(f"\nALL 14 SPECIAL LOCATIONS NOW COMPLETE. P1 special locations 100%.")

if __name__ == "__main__":
    main()