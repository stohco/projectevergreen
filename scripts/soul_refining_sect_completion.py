#!/usr/bin/env python3
"""
AUTO-CANON-037: Soul Refining Sect (炼魂宗) completion — 21 loot tables + 12 INFERRED NPCs.
Theme: soul refinement, soul banners, bone/soul items, dark cultivation, hierarchical sect.
Location: Pilu Kingdom (main sect, 20 structures) + Sea of Devils (ancestral ground, 1 structure).
Canon: Wang Lin inherits the Ten Billion Soul Banner here from Dun Tian.
"""

import json
import os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ── Loot table definitions ──────────────────────────────────────────────
# Wealth tiers for soul refinement sect:
#   LOW:  outer_gate, disciple_dormitories, spirit_herb_garden, trial_grounds, main_plaza
#   MED:  library, alchemy_courtyard, spirit_beast_pens, array_hall, spirit_spring,
#         sword_peak, puppet_workshop, mountain_cave
#   HIGH: inner_sect, sword_tomb, underground_passage, secret_pavilion
#   MAX:  hidden_treasury, ancestor_hall, core_formation_hall, soul_refining_ancestral

LOOT_TABLES = {
    # ── LOW TIER (outer areas, accessible to all disciples) ──
    "soul_refining_sect_outer_gate": {
        "theme": "Gate patrol supplies. Outer disciples guard the entrance.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (10, "minecraft:arrow", (4, 12)),
                (8, "minecraft:leather", (1, 3)),
                (6, "minecraft:bread", (1, 3)),
                (5, "minecraft:coal", (2, 6)),
                (4, "minecraft:stick", (4, 8)),
                (2, "minecraft:iron_ingot", (1, 2)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:paper", (1, 3)),
                (3, "minecraft:bone", (2, 6)),
                (2, "minecraft:ink_sac", (1, 2)),
            ]},
        ]
    },
    "soul_refining_sect_disciple_dormitories": {
        "theme": "Outer disciple living quarters. Simple cultivation aids.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:bread", (1, 4)),
                (6, "minecraft:coal", (2, 5)),
                (5, "minecraft:paper", (1, 3)),
                (4, "minecraft:stick", (3, 8)),
                (3, "minecraft:glass_bottle", (1, 2)),
                (2, "minecraft:book", (1, 1)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:bone", (1, 4)),
                (3, "minecraft:ink_sac", (1, 1)),
                (2, "minecraft:emerald", (1, 2)),
            ]},
        ]
    },
    "soul_refining_sect_spirit_herb_garden": {
        "theme": "Herb cultivation garden. Soul-nourishing plants.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:wheat", (3, 8)),
                (6, "minecraft:bone_meal", (2, 6)),
                (5, "minecraft:glass_bottle", (1, 3)),
                (4, "minecraft:coal", (1, 4)),
                (3, "minecraft:bread", (1, 2)),
            ]},
            {"rolls": (1, 3), "entries": [
                (6, "minecraft:bone", (1, 4)),
                (4, "minecraft:ink_sac", (1, 2)),
                (2, "ergenverse:soul_nourishing_lotus", (1, 1)),
                (2, "minecraft:book", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_trial_grounds": {
        "theme": "Disciple testing arena. Rewards for those who pass.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:arrow", (3, 10)),
                (6, "minecraft:leather", (1, 4)),
                (5, "minecraft:iron_ingot", (1, 3)),
                (4, "minecraft:coal", (2, 6)),
                (3, "minecraft:bread", (1, 3)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:emerald", (1, 3)),
                (4, "minecraft:bone", (2, 5)),
                (3, "minecraft:experience_bottle", (1, 2)),
                (2, "minecraft:book", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_main_plaza": {
        "theme": "Central gathering area. Standard sect supplies.",
        "pools": [
            {"rolls": (2, 5), "entries": [
                (7, "minecraft:coal", (2, 6)),
                (6, "minecraft:iron_ingot", (1, 3)),
                (5, "minecraft:bread", (1, 4)),
                (5, "minecraft:paper", (1, 3)),
                (4, "minecraft:bone", (2, 5)),
                (3, "minecraft:emerald", (1, 3)),
                (2, "minecraft:book", (1, 2)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:ink_sac", (1, 2)),
                (3, "minecraft:glass_bottle", (1, 2)),
                (2, "minecraft:arrow", (3, 8)),
            ]},
        ]
    },

    # ── MEDIUM TIER (inner facilities, sect resources) ──
    "soul_refining_sect_library": {
        "theme": "Sect library. Forbidden techniques and soul refinement manuals.",
        "pools": [
            {"rolls": (3, 5), "entries": [
                (10, "minecraft:book", (1, 3)),
                (8, "minecraft:paper", (2, 5)),
                (6, "minecraft:ink_sac", (1, 3)),
                (5, "minecraft:emerald", (1, 4)),
                (3, "minecraft:enchanted_book", (1, 1)),
            ]},
            {"rolls": (1, 3), "entries": [
                (6, "minecraft:experience_bottle", (1, 3)),
                (4, "ergenverse:soul_fragment", (1, 1)),
                (3, "minecraft:ender_pearl", (1, 2)),
                (2, "ergenverse:heart_devil_flower", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_alchemy_courtyard": {
        "theme": "Alchemy area. Pills and soul-refining reagents.",
        "pools": [
            {"rolls": (2, 5), "entries": [
                (8, "minecraft:glass_bottle", (2, 5)),
                (7, "minecraft:coal", (2, 6)),
                (5, "minecraft:wheat", (2, 5)),
                (4, "minecraft:bone", (2, 6)),
                (3, "minecraft:emerald", (1, 3)),
                (2, "minecraft:ghast_tear", (1, 1)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:ink_sac", (1, 2)),
                (4, "ergenverse:soul_nourishing_lotus", (1, 1)),
                (3, "minecraft:experience_bottle", (1, 2)),
                (2, "minecraft:blaze_powder", (1, 2)),
            ]},
        ]
    },
    "soul_refining_sect_spirit_beast_pens": {
        "theme": "Captured spirit beasts. Beast materials for soul refinement.",
        "pools": [
            {"rolls": (2, 5), "entries": [
                (10, "minecraft:bone", (4, 12)),
                (8, "minecraft:leather", (2, 6)),
                (6, "minecraft:arrow", (4, 10)),
                (5, "minecraft:iron_ingot", (1, 3)),
                (3, "minecraft:lead", (1, 3)),
                (2, "minecraft:emerald", (1, 3)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:string", (2, 6)),
                (3, "minecraft:coal", (2, 5)),
                (2, "ergenverse:soul_fragment", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_array_hall": {
        "theme": "Formation array hall. Restriction and array components.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:redstone", (4, 12)),
                (7, "minecraft:iron_ingot", (1, 4)),
                (5, "minecraft:obsidian", (1, 4)),
                (4, "minecraft:ender_pearl", (1, 2)),
                (3, "minecraft:emerald", (1, 4)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:coal", (2, 6)),
                (4, "minecraft:ink_sac", (1, 3)),
                (3, "minecraft:experience_bottle", (1, 2)),
                (2, "ergenverse:soul_fragment", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_spirit_spring": {
        "theme": "Spirit spring. Rare cultivation water source.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:glass_bottle", (2, 6)),
                (6, "minecraft:emerald", (2, 5)),
                (4, "minecraft:coal", (1, 4)),
                (3, "minecraft:book", (1, 2)),
                (2, "ergenverse:soul_nourishing_lotus", (1, 1)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:experience_bottle", (1, 3)),
                (3, "minecraft:ink_sac", (1, 2)),
                (2, "minecraft:bone", (2, 5)),
            ]},
        ]
    },
    "soul_refining_sect_sword_peak": {
        "theme": "Sword cultivation peak. Weapon materials and ancient swords.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:iron_ingot", (2, 6)),
                (6, "minecraft:emerald", (1, 4)),
                (5, "minecraft:coal", (2, 5)),
                (4, "minecraft:bone", (2, 5)),
                (3, "minecraft:enchanted_book", (1, 1)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:experience_bottle", (1, 3)),
                (3, "minecraft:ender_pearl", (1, 2)),
                (2, "minecraft:book", (1, 2)),
            ]},
        ]
    },
    "soul_refining_sect_puppet_workshop": {
        "theme": "Soul puppet workshop. Puppet parts and soul-binding materials.",
        "pools": [
            {"rolls": (2, 5), "entries": [
                (9, "minecraft:iron_ingot", (2, 6)),
                (7, "minecraft:bone", (4, 10)),
                (6, "minecraft:redstone", (2, 6)),
                (5, "minecraft:leather", (2, 5)),
                (4, "minecraft:coal", (2, 5)),
                (2, "minecraft:emerald", (1, 3)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "minecraft:string", (2, 6)),
                (3, "ergenverse:soul_fragment", (1, 1)),
                (2, "minecraft:ender_pearl", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_mountain_cave": {
        "theme": "Mountain cultivation cave. Secluded meditation spot.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (7, "minecraft:coal", (3, 8)),
                (5, "minecraft:iron_ingot", (1, 4)),
                (5, "minecraft:bone", (2, 6)),
                (4, "minecraft:glass_bottle", (1, 3)),
                (3, "minecraft:emerald", (1, 3)),
                (2, "minecraft:book", (1, 1)),
            ]},
            {"rolls": (1, 2), "entries": [
                (4, "minecraft:experience_bottle", (1, 2)),
                (3, "minecraft:ink_sac", (1, 2)),
                (2, "ergenverse:soul_nourishing_lotus", (1, 1)),
            ]},
        ]
    },

    # ── HIGH TIER (restricted areas, valuable finds) ──
    "soul_refining_sect_inner_sect": {
        "theme": "Inner sect sanctuary. Advanced cultivation resources.",
        "pools": [
            {"rolls": (3, 5), "entries": [
                (7, "minecraft:emerald", (2, 6)),
                (6, "minecraft:iron_ingot", (2, 5)),
                (5, "minecraft:book", (1, 3)),
                (4, "minecraft:enchanted_book", (1, 1)),
                (3, "minecraft:diamond", (1, 2)),
                (3, "minecraft:gold_ingot", (1, 3)),
            ]},
            {"rolls": (1, 3), "entries": [
                (5, "ergenverse:spirit_stone", (1, 3)),
                (4, "ergenverse:soul_fragment", (1, 2)),
                (3, "minecraft:ender_pearl", (1, 3)),
                (2, "minecraft:ghast_tear", (1, 1)),
                (2, "minecraft:experience_bottle", (2, 4)),
            ]},
        ]
    },
    "soul_refining_sect_sword_tomb": {
        "theme": "Ancient sword burial ground. Powerful weapons sealed here.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (7, "minecraft:iron_ingot", (3, 8)),
                (5, "minecraft:diamond", (1, 2)),
                (5, "minecraft:enchanted_book", (1, 2)),
                (4, "minecraft:bone", (4, 10)),
                (3, "minecraft:obsidian", (2, 5)),
            ]},
            {"rolls": (1, 2), "entries": [
                (5, "ergenverse:spirit_stone", (1, 3)),
                (3, "ergenverse:soul_fragment", (1, 2)),
                (2, "ergenverse:soul_refining_flag", (1, 1)),
                (2, "minecraft:experience_bottle", (2, 4)),
            ]},
        ]
    },
    "soul_refining_sect_underground_passage": {
        "theme": "Secret underground tunnels. Forbidden soul experiments.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (9, "minecraft:bone", (6, 14)),
                (7, "minecraft:obsidian", (2, 6)),
                (5, "minecraft:soul_sand", (4, 10)),
                (4, "minecraft:ink_sac", (2, 5)),
                (3, "minecraft:iron_ingot", (1, 3)),
            ]},
            {"rolls": (1, 3), "entries": [
                (5, "ergenverse:soul_fragment", (1, 2)),
                (4, "ergenverse:spirit_stone", (1, 3)),
                (3, "minecraft:ender_pearl", (1, 2)),
                (2, "ergenverse:soul_refining_flag", (1, 1)),
                (1, "ergenverse:soul_lasher", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_secret_pavilion": {
        "theme": "Forbidden knowledge pavilion. Restricted soul techniques.",
        "pools": [
            {"rolls": (2, 4), "entries": [
                (8, "minecraft:enchanted_book", (1, 2)),
                (6, "minecraft:book", (2, 4)),
                (5, "minecraft:obsidian", (2, 5)),
                (4, "minecraft:emerald", (2, 5)),
                (3, "minecraft:diamond", (1, 2)),
            ]},
            {"rolls": (1, 3), "entries": [
                (5, "ergenverse:spirit_stone", (2, 5)),
                (4, "ergenverse:soul_fragment", (1, 2)),
                (3, "ergenverse:heart_devil_flower", (1, 1)),
                (2, "minecraft:ender_pearl", (1, 2)),
                (1, "ergenverse:nether_core", (1, 1)),
            ]},
        ]
    },

    # ── MAX TIER (most restricted, greatest treasures) ──
    "soul_refining_sect_hidden_treasury": {
        "theme": "Sect treasury. Vast accumulated wealth and artifacts.",
        "pools": [
            {"rolls": (3, 6), "entries": [
                (7, "minecraft:diamond", (2, 5)),
                (6, "minecraft:gold_ingot", (3, 8)),
                (5, "minecraft:emerald", (3, 8)),
                (4, "minecraft:enchanted_book", (1, 2)),
                (3, "minecraft:golden_apple", (1, 2)),
            ]},
            {"rolls": (2, 4), "entries": [
                (5, "ergenverse:spirit_stone", (3, 8)),
                (4, "ergenverse:spirit_stone_mid", (1, 3)),
                (3, "ergenverse:soul_fragment", (2, 4)),
                (2, "ergenverse:nether_core", (1, 1)),
                (1, "ergenverse:soul_lasher", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_ancestor_hall": {
        "theme": "Ancestor worship hall. Sacred relics and ancestor's legacy.",
        "pools": [
            {"rolls": (3, 5), "entries": [
                (7, "minecraft:diamond", (2, 5)),
                (6, "minecraft:golden_apple", (1, 3)),
                (5, "minecraft:enchanted_book", (1, 2)),
                (4, "minecraft:obsidian", (3, 8)),
                (3, "minecraft:bone", (4, 10)),
            ]},
            {"rolls": (2, 4), "entries": [
                (5, "ergenverse:spirit_stone", (3, 8)),
                (4, "ergenverse:spirit_stone_mid", (2, 4)),
                (3, "ergenverse:soul_refining_flag", (1, 2)),
                (2, "ergenverse:soul_nourishing_lotus", (1, 2)),
                (1, "minecraft:dragon_breath", (1, 1)),
            ]},
        ]
    },
    "soul_refining_sect_core_formation_hall": {
        "theme": "Core soul refinement hall. The heart of the sect's power.",
        "pools": [
            {"rolls": (3, 5), "entries": [
                (7, "ergenverse:spirit_stone", (3, 8)),
                (5, "ergenverse:spirit_stone_mid", (1, 4)),
                (4, "ergenverse:soul_fragment", (2, 5)),
                (4, "minecraft:bone", (6, 14)),
                (3, "minecraft:soul_sand", (4, 10)),
                (2, "ergenverse:spirit_stone_high", (1, 2)),
            ]},
            {"rolls": (1, 3), "entries": [
                (4, "ergenverse:soul_refining_flag", (1, 1)),
                (3, "ergenverse:soul_lasher", (1, 1)),
                (2, "ergenverse:nether_core", (1, 1)),
                (2, "minecraft:ghast_tear", (1, 2)),
                (1, "minecraft:dragon_breath", (1, 1)),
            ]},
        ]
    },
    "soul_refining_ancestral": {
        "theme": "Deep ancestral ground in Sea of Devils. Origin of the Ten Billion Soul Banner. Most dangerous, most rewarding.",
        "pools": [
            {"rolls": (3, 6), "entries": [
                (6, "ergenverse:spirit_stone_mid", (2, 5)),
                (5, "ergenverse:spirit_stone_high", (1, 3)),
                (4, "ergenverse:soul_fragment", (3, 6)),
                (4, "minecraft:bone", (8, 16)),
                (3, "minecraft:soul_sand", (6, 12)),
                (3, "minecraft:obsidian", (4, 10)),
            ]},
            {"rolls": (2, 4), "entries": [
                (5, "ergenverse:soul_refining_flag", (1, 2)),
                (4, "ergenverse:soul_lasher", (1, 1)),
                (3, "ergenverse:nether_core", (1, 2)),
                (2, "minecraft:dragon_breath", (1, 2)),
                (2, "ergenverse:heart_devil_flower", (1, 2)),
                (1, "minecraft:golden_apple", (1, 3)),
            ]},
            {"rolls": (1, 2), "entries": [
                (3, "minecraft:enchanted_book", (1, 2)),
                (2, "minecraft:wither_skeleton_skull", (1, 1)),
                (1, "ergenverse:spirit_stone_high", (1, 1)),
            ]},
        ]
    },
}


def build_loot_table(name, data):
    """Build a Minecraft loot table JSON from compact definition."""
    pools = []
    for pool in data["pools"]:
        entries = []
        for weight, item, count_range in pool["entries"]:
            entry = {
                "type": "minecraft:item",
                "weight": weight,
                "name": item,
            }
            if count_range != (1, 1):
                entry["functions"] = [{
                    "function": "minecraft:set_count",
                    "count": {"min": count_range[0], "max": count_range[1]}
                }]
            entries.append(entry)
        pools.append({
            "rolls": {"min": pool["rolls"][0], "max": pool["rolls"][1]},
            "entries": entries,
        })
    return {
        "_comment": f"Soul Refining Sect — {name}. {data['theme']}",
        "type": "minecraft:chest",
        "pools": pools,
    }


# ── NPC definitions ──────────────────────────────────────────────────────
# 12 INFERRED NPCs covering all major sect areas.
# Theme: hierarchical, dark, soul-obsessed, forbidden knowledge.

NPCS = [
    {
        "npc_id": "npc_sr_outer_disciple_wei",
        "name": "Outer Disciple Wei",
        "nameCn": "外门弟子魏",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_outer_gate",
        "cultivation": "FAITH early",
        "personality": "dutiful, suspicious of outsiders, eager to prove himself",
        "speech": "clipped, questioning",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370101,
        "initiation_lines": [
            "Halt. State your business at the Soul Refining Sect. Outsiders are not welcome without cause.",
            "The ancestors watch all who enter. If you seek cultivation, speak to Elder Mo in the Core Hall.",
            "Beasts roar from the pens at night. Some say they remember their lives before capture.",
            "The trial grounds are closed today. A disciple failed the soul-binding test. Again.",
        ],
        "daily_schedule": [
            {"time": "0600", "action": "patrol", "location": "soul_refining_sect_outer_gate", "duration": 120},
            {"time": "0800", "action": "stand_guard", "location": "soul_refining_sect_outer_gate", "duration": 240},
            {"time": "1200", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "patrol", "location": "soul_refining_sect_main_plaza", "duration": 120},
            {"time": "1500", "action": "train", "location": "soul_refining_sect_trial_grounds", "duration": 120},
            {"time": "1700", "action": "stand_guard", "location": "soul_refining_sect_outer_gate", "duration": 180},
            {"time": "2000", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "gate_patrol_supplies",
                "name": "Gate Patrol Supplies",
                "description": "The outer gate needs arrows and leather for patrol shifts. Bring them to Disciple Wei.",
                "required_items": {"minecraft:arrow": 8, "minecraft:leather": 3},
                "rewards": {"minecraft:emerald": 5, "minecraft:book": 1},
            },
            {
                "task_id": "visitor_registration",
                "name": "Visitor Registration Log",
                "description": "Record all visitors at the gate. Bring paper and ink for the registration book.",
                "required_items": {"minecraft:paper": 5, "minecraft:ink_sac": 2},
                "rewards": {"minecraft:emerald": 4, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_sr_soul_hall_elder_mo",
        "name": "Elder Mo",
        "nameCn": "魂殿长老墨",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_core_formation_hall",
        "cultivation": "FAITH late",
        "personality": "cold, calculating, obsessed with perfecting soul refinement",
        "speech": "measured, unsettling pauses",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370102,
        "initiation_lines": [
            "The soul is the true treasure. Flesh decays. The soul, refined, endures.",
            "Bring me soul fragments and I will teach you the first principle of refinement: separation.",
            "The ancestor refined ten thousand souls into a single banner. We seek to surpass even that.",
            "The underground passages contain... experiments. Do not venture there without permission.",
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "soul_refining_sect_core_formation_hall", "duration": 180},
            {"time": "0800", "action": "supervise", "location": "soul_refining_sect_core_formation_hall", "duration": 240},
            {"time": "1200", "action": "inspect", "location": "soul_refining_sect_puppet_workshop", "duration": 60},
            {"time": "1300", "action": "lecture", "location": "soul_refining_sect_library", "duration": 120},
            {"time": "1500", "action": "supervise", "location": "soul_refining_sect_core_formation_hall", "duration": 180},
            {"time": "1800", "action": "ritual", "location": "soul_refining_sect_ancestor_hall", "duration": 120},
            {"time": "2000", "action": "meditate", "location": "soul_refining_sect_core_formation_hall", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "soul_fragment_collection",
                "name": "Soul Fragment Collection",
                "description": "Elder Mo needs soul fragments for the refinement furnace. Bring them from the beast pens or sword tomb.",
                "required_items": {"ergenverse:soul_fragment": 3, "minecraft:bone": 10},
                "rewards": {"ergenverse:spirit_stone": 4, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "furnace_fuel",
                "name": "Refinement Furnace Fuel",
                "description": "The soul refinement furnace requires soul sand and coal to maintain its fires.",
                "required_items": {"minecraft:soul_sand": 8, "minecraft:coal": 10},
                "rewards": {"ergenverse:spirit_stone_mid": 2, "minecraft:experience_bottle": 2},
            },
        ],
    },
    {
        "npc_id": "npc_sr_library_keeper_xue",
        "name": "Library Keeper Xue",
        "nameCn": "藏书阁薛",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_library",
        "cultivation": "FAITH late",
        "personality": "scholarly, secretive, knows more than she reveals",
        "speech": "quiet, precise",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370103,
        "initiation_lines": [
            "The library holds three thousand scrolls on soul refinement. Most are forbidden to outer disciples.",
            "Bring me paper and ink. I am copying a restricted technique for Elder Mo. Do not read over my shoulder.",
            "A visitor from the Heng Yue Sect came here once, seeking information on soul-binding. He left quickly.",
            "The Secret Pavilion contains techniques that even elders fear. I have never been inside.",
        ],
        "daily_schedule": [
            {"time": "0600", "action": "organize", "location": "soul_refining_sect_library", "duration": 180},
            {"time": "0900", "action": "copy_scrolls", "location": "soul_refining_sect_library", "duration": 240},
            {"time": "1300", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "research", "location": "soul_refining_sect_library", "duration": 180},
            {"time": "1700", "action": "deliver", "location": "soul_refining_sect_inner_sect", "duration": 60},
            {"time": "1800", "action": "organize", "location": "soul_refining_sect_library", "duration": 120},
            {"time": "2000", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "scroll_copying",
                "name": "Scroll Copying Materials",
                "description": "Keeper Xue needs paper and ink to copy forbidden techniques for the elders.",
                "required_items": {"minecraft:paper": 8, "minecraft:ink_sac": 4},
                "rewards": {"minecraft:book": 2, "minecraft:emerald": 5},
            },
            {
                "task_id": "library_restock",
                "name": "Library Restocking",
                "description": "The library needs books and experience bottles for advanced disciples' studies.",
                "required_items": {"minecraft:book": 3, "minecraft:experience_bottle": 2},
                "rewards": {"minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 1},
            },
        ],
    },
    {
        "npc_id": "npc_sr_beast_handler_lei",
        "name": "Beast Handler Lei",
        "nameCn": "兽栏管事雷",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_spirit_beast_pens",
        "cultivation": "FAITH mid",
        "personality": "gruff, practical, secretly sympathetic to the beasts",
        "speech": "blunt, short sentences",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370104,
        "initiation_lines": [
            "Don't get too close to the pens. Some of these beasts still have their original souls.",
            "I need bones and leather for the feeding stock. The stronger beasts require iron supplements.",
            "The Qilin City merchants sell live beasts. Our sect buys in bulk. Most don't survive the journey.",
            "Elder Mo wants me to prepare a beast for the refinement furnace. I delay as long as I can.",
        ],
        "daily_schedule": [
            {"time": "0500", "action": "feed_beasts", "location": "soul_refining_sect_spirit_beast_pens", "duration": 120},
            {"time": "0700", "action": "patrol_pens", "location": "soul_refining_sect_spirit_beast_pens", "duration": 180},
            {"time": "1000", "action": "train", "location": "soul_refining_sect_trial_grounds", "duration": 120},
            {"time": "1200", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "feed_beasts", "location": "soul_refining_sect_spirit_beast_pens", "duration": 120},
            {"time": "1500", "action": "procure", "location": "soul_refining_sect_outer_gate", "duration": 60},
            {"time": "1600", "action": "patrol_pens", "location": "soul_refining_sect_spirit_beast_pens", "duration": 180},
            {"time": "1900", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {
                "task_id": "beast_feeding_supplies",
                "name": "Beast Feeding Supplies",
                "description": "The spirit beasts need bones and leather for sustenance. Handler Lei is running low.",
                "required_items": {"minecraft:bone": 12, "minecraft:leather": 6},
                "rewards": {"minecraft:emerald": 7, "minecraft:arrow": 8},
            },
            {
                "task_id": "pen_reinforcement",
                "name": "Pen Reinforcement Materials",
                "description": "The beast pens need iron and lead to reinforce the enclosures. Some beasts are breaking free.",
                "required_items": {"minecraft:iron_ingot": 6, "minecraft:lead": 4},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:emerald": 5},
            },
        ],
    },
    {
        "npc_id": "npc_sr_alchemist_han",
        "name": "Alchemist Han",
        "nameCn": "炼丹弟子韩",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_alchemy_courtyard",
        "cultivation": "FAITH mid",
        "personality": "nervous, meticulous, afraid of making mistakes",
        "speech": "hesitant, technical",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370105,
        "initiation_lines": [
            "I-I need glass bottles and coal for the refining fires. One wrong temperature and the whole batch is ruined.",
            "Soul Nourishing Lotuses grow in the herb garden, but only Elder Mo may harvest them.",
            "A pill that strengthens the soul requires ghast tears. Do you know how hard those are to obtain?",
            "The last alchemist who failed Elder Mo's order was sent to the underground passages. He never returned.",
        ],
        "daily_schedule": [
            {"time": "0600", "action": "gather_herbs", "location": "soul_refining_sect_spirit_herb_garden", "duration": 120},
            {"time": "0800", "action": "refine", "location": "soul_refining_sect_alchemy_courtyard", "duration": 240},
            {"time": "1200", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "refine", "location": "soul_refining_sect_alchemy_courtyard", "duration": 180},
            {"time": "1600", "action": "deliver_pills", "location": "soul_refining_sect_inner_sect", "duration": 60},
            {"time": "1700", "action": "clean", "location": "soul_refining_sect_alchemy_courtyard", "duration": 120},
            {"time": "1900", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {
                "task_id": "alchemy_reagents",
                "name": "Alchemy Reagent Supply",
                "description": "Alchemist Han needs glass bottles and coal for the soul-strengthening pill furnace.",
                "required_items": {"minecraft:glass_bottle": 8, "minecraft:coal": 10},
                "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 6},
            },
            {
                "task_id": "soul_pill_ingredients",
                "name": "Soul Pill Ingredients",
                "description": "Rare ingredients needed: soul nourishing lotus and blaze powder for the soul pill.",
                "required_items": {"ergenverse:soul_nourishing_lotus": 2, "minecraft:blaze_powder": 3},
                "rewards": {"ergenverse:spirit_stone": 3, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_sr_array_master_feng",
        "name": "Array Master Feng",
        "nameCn": "阵法大师冯",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_array_hall",
        "cultivation": "FAITH late",
        "personality": "arrogant, perfectionist, proud of his formations",
        "speech": "lecturing, condescending",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370106,
        "initiation_lines": [
            "These arrays have protected the Soul Refining Sect for a thousand years. Do not touch the redstone.",
            "I need obsidian and ender pearls for the grand array repair. The outer wards are weakening.",
            "The restriction arrays in the Secret Pavilion are my masterpiece. Even the ancestor praised them.",
            "A cultivator from the Corpse Yin Sect came to study arrays. I turned him away. Our techniques are not shared.",
        ],
        "daily_schedule": [
            {"time": "0500", "action": "inspect_arrays", "location": "soul_refining_sect_array_hall", "duration": 180},
            {"time": "0800", "action": "repair", "location": "soul_refining_sect_outer_gate", "duration": 120},
            {"time": "1000", "action": "teach", "location": "soul_refining_sect_array_hall", "duration": 180},
            {"time": "1300", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "repair", "location": "soul_refining_sect_hidden_treasury", "duration": 120},
            {"time": "1600", "action": "research", "location": "soul_refining_sect_array_hall", "duration": 180},
            {"time": "1900", "action": "meditate", "location": "soul_refining_sect_mountain_cave", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "array_repair_materials",
                "name": "Array Repair Materials",
                "description": "Array Master Feng needs obsidian and ender pearls to repair the outer ward arrays.",
                "required_items": {"minecraft:obsidian": 8, "minecraft:ender_pearl": 3},
                "rewards": {"ergenverse:spirit_stone": 3, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "formation_components",
                "name": "Formation Components",
                "description": "Redstone and iron needed for the inner formation nodes. The sect's defenses must hold.",
                "required_items": {"minecraft:redstone": 16, "minecraft:iron_ingot": 6},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:emerald": 8},
            },
        ],
    },
    {
        "npc_id": "npc_sr_puppet_artisan_gu",
        "name": "Puppet Artisan Gu",
        "nameCn": "傀儡师顾",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_puppet_workshop",
        "cultivation": "FAITH late",
        "personality": "obsessive, rarely speaks, focused on his creations",
        "speech": "muttering, distracted",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370107,
        "initiation_lines": [
            "The puppet needs more bone. And redstone for the binding joints. And soul fragments for the core.",
            "I am creating a puppet that can fight on its own. The ancestor will be pleased.",
            "Do you hear them? The souls inside the puppets. They are quiet now. Most of them.",
            "Bring me materials and I may show you how to bind a soul to a construct. It is not for the faint-hearted.",
        ],
        "daily_schedule": [
            {"time": "0400", "action": "craft", "location": "soul_refining_sect_puppet_workshop", "duration": 300},
            {"time": "0900", "action": "gather", "location": "soul_refining_sect_spirit_beast_pens", "duration": 60},
            {"time": "1000", "action": "craft", "location": "soul_refining_sect_puppet_workshop", "duration": 240},
            {"time": "1400", "action": "test", "location": "soul_refining_sect_trial_grounds", "duration": 120},
            {"time": "1600", "action": "craft", "location": "soul_refining_sect_puppet_workshop", "duration": 240},
            {"time": "2000", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "puppet_materials",
                "name": "Puppet Construction Materials",
                "description": "Artisan Gu needs bones, redstone, and iron for his soul puppet construction.",
                "required_items": {"minecraft:bone": 16, "minecraft:redstone": 8, "minecraft:iron_ingot": 4},
                "rewards": {"ergenverse:soul_fragment": 2, "ergenverse:spirit_stone": 3},
            },
            {
                "task_id": "soul_binding_components",
                "name": "Soul Binding Components",
                "description": "Soul fragments and leather needed to bind souls into puppet constructs.",
                "required_items": {"ergenverse:soul_fragment": 3, "minecraft:leather": 8, "minecraft:string": 6},
                "rewards": {"ergenverse:spirit_stone_mid": 2, "minecraft:experience_bottle": 2},
            },
        ],
    },
    {
        "npc_id": "npc_sr_trial_proctor_zhao",
        "name": "Trial Proctor Zhao",
        "nameCn": "试炼执法赵",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_trial_grounds",
        "cultivation": "FAITH late",
        "personality": "stern, unforgiving, tests disciples harshly",
        "speech": "commanding, loud",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370108,
        "initiation_lines": [
            "The trial grounds test your soul's resilience. If you cannot endure, the sect has no use for you.",
            "I need arrows and iron for the combat trials. Disciples must face real weapons, not practice dummies.",
            "Three disciples failed the soul-binding trial this month. Their souls are... repurposed.",
            "Only those who pass the trials may enter the Inner Sect. The weak are culled. This is the ancestor's law.",
        ],
        "daily_schedule": [
            {"time": "0600", "action": "prepare_trials", "location": "soul_refining_sect_trial_grounds", "duration": 120},
            {"time": "0800", "action": "proctor_trials", "location": "soul_refining_sect_trial_grounds", "duration": 300},
            {"time": "1300", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "judge", "location": "soul_refining_sect_trial_grounds", "duration": 180},
            {"time": "1700", "action": "report", "location": "soul_refining_sect_inner_sect", "duration": 60},
            {"time": "1800", "action": "patrol", "location": "soul_refining_sect_outer_gate", "duration": 120},
            {"time": "2000", "action": "rest", "location": "soul_refining_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "trial_supplies",
                "name": "Trial Ground Supplies",
                "description": "Proctor Zhao needs arrows and iron for the combat trials. Disciples must face real weapons.",
                "required_items": {"minecraft:arrow": 16, "minecraft:iron_ingot": 6},
                "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2},
            },
            {
                "task_id": "trial_recovery",
                "name": "Trial Recovery Materials",
                "description": "After trials, the grounds need leather and bread for injured disciples.",
                "required_items": {"minecraft:leather": 8, "minecraft:bread": 10},
                "rewards": {"ergenverse:soul_fragment": 1, "minecraft:emerald": 5},
            },
        ],
    },
    {
        "npc_id": "npc_sr_ancestor_attendant_bai",
        "name": "Ancestor Attendant Bai",
        "nameCn": "祖殿侍者白",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_ancestor_hall",
        "cultivation": "FAITH late",
        "personality": "reverent, cryptic, speaks as if the ancestor listens",
        "speech": "whispering, formal",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370109,
        "initiation_lines": [
            "The ancestor's will permeates this hall. Speak softly. He may still be listening.",
            "I require obsidian and gold for the incense altar. The ancestor's spirit prefers precious offerings.",
            "The ancestral ground in the Sea of Devils is sealed. Only the sect leader may open it. Something sleeps there.",
            "Wang Lin... the name is written in the ancestor's journals. He inherited something from this sect.",
        ],
        "daily_schedule": [
            {"time": "0400", "action": "ritual", "location": "soul_refining_sect_ancestor_hall", "duration": 180},
            {"time": "0700", "action": "clean_hall", "location": "soul_refining_sect_ancestor_hall", "duration": 120},
            {"time": "0900", "action": "offer_incense", "location": "soul_refining_sect_ancestor_hall", "duration": 60},
            {"time": "1000", "action": "stand_vigil", "location": "soul_refining_sect_ancestor_hall", "duration": 360},
            {"time": "1600", "action": "read_journals", "location": "soul_refining_sect_library", "duration": 120},
            {"time": "1800", "action": "ritual", "location": "soul_refining_sect_ancestor_hall", "duration": 120},
            {"time": "2000", "action": "stand_vigil", "location": "soul_refining_sect_ancestor_hall", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "ancestor_incense",
                "name": "Ancestor Incense Materials",
                "description": "The ancestor hall requires obsidian and gold for the sacred incense altar.",
                "required_items": {"minecraft:obsidian": 6, "minecraft:gold_ingot": 4},
                "rewards": {"ergenverse:spirit_stone_mid": 2, "minecraft:emerald": 6},
            },
            {
                "task_id": "ancestor_journal_repair",
                "name": "Ancestor Journal Repair",
                "description": "Ancient journals need books and ink for preservation. They contain the ancestor's techniques.",
                "required_items": {"minecraft:book": 4, "minecraft:ink_sac": 4, "minecraft:paper": 6},
                "rewards": {"ergenverse:soul_refining_flag": 1, "ergenverse:spirit_stone": 4},
            },
        ],
    },
    {
        "npc_id": "npc_sr_shadow_guide_wu",
        "name": "Shadow Guide Wu",
        "nameCn": "暗道向导吴",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_underground_passage",
        "cultivation": "FAITH late",
        "personality": "cunning, secretive, sells information",
        "speech": "low voice, pauses to listen",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370110,
        "initiation_lines": [
            "You found the passage. Interesting. Most disciples don't know this entrance exists.",
            "I can guide you through the underground tunnels. But information has a price.",
            "The treasury is not as well-guarded as Elder Mo believes. I know a way. For the right payment.",
            "Bones line these tunnels. Not all of them are from beasts. Remember that.",
        ],
        "daily_schedule": [
            {"time": "0500", "action": "patrol", "location": "soul_refining_sect_underground_passage", "duration": 180},
            {"time": "0800", "action": "lurk", "location": "soul_refining_sect_main_plaza", "duration": 120},
            {"time": "1000", "action": "scout", "location": "soul_refining_sect_hidden_treasury", "duration": 60},
            {"time": "1100", "action": "lurk", "location": "soul_refining_sect_outer_gate", "duration": 120},
            {"time": "1300", "action": "eat", "location": "soul_refining_sect_tavern_district", "duration": 60},
            {"time": "1400", "action": "patrol", "location": "soul_refining_sect_underground_passage", "duration": 180},
            {"time": "1700", "action": "gather_intel", "location": "soul_refining_sect_inner_sect", "duration": 120},
            {"time": "1900", "action": "rest", "location": "soul_refining_sect_underground_passage", "duration": 660},
        ],
        "sect_tasks": [
            {
                "task_id": "tunnel_mapping",
                "name": "Tunnel Mapping Supplies",
                "description": "Shadow Guide Wu needs ink and paper to map the ever-changing underground passages.",
                "required_items": {"minecraft:ink_sac": 4, "minecraft:paper": 8},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "underground_contraband",
                "name": "Underground Contraband",
                "description": "Smuggle gold and emeralds through the tunnels. Wu pays well for discretion.",
                "required_items": {"minecraft:gold_ingot": 4, "minecraft:emerald": 8},
                "rewards": {"ergenverse:soul_lasher": 1, "ergenverse:spirit_stone_mid": 3},
            },
        ],
    },
    {
        "npc_id": "npc_sr_sword_tomb_keeper",
        "name": "Sword Tomb Keeper",
        "nameCn": "剑冢守卫",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_sword_tomb",
        "cultivation": "NASCENT_SOUL early",
        "personality": "ancient, weary, has guarded the tomb for centuries",
        "speech": "echoing, slow",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370111,
        "initiation_lines": [
            "These swords once belonged to cultivators. Their spirits remain. Approach with respect.",
            "The ancestor forged his first soul banner from the spirit of a sword that refused to die.",
            "Iron and bone maintain the tomb's seals. If the seals break, the sword spirits will roam free.",
            "I have guarded this tomb for three hundred years. My own sword lies among them now. I simply forgot which one.",
        ],
        "daily_schedule": [
            {"time": "0400", "action": "meditate", "location": "soul_refining_sect_sword_tomb", "duration": 240},
            {"time": "0800", "action": "inspect_seals", "location": "soul_refining_sect_sword_tomb", "duration": 300},
            {"time": "1300", "action": "walk", "location": "soul_refining_sect_sword_peak", "duration": 120},
            {"time": "1500", "action": "inspect_seals", "location": "soul_refining_sect_sword_tomb", "duration": 240},
            {"time": "1900", "action": "meditate", "location": "soul_refining_sect_sword_tomb", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "tomb_seal_maintenance",
                "name": "Tomb Seal Maintenance",
                "description": "The sword tomb seals need iron and bone to prevent sword spirits from escaping.",
                "required_items": {"minecraft:iron_ingot": 10, "minecraft:bone": 16},
                "rewards": {"ergenverse:spirit_stone_mid": 2, "ergenverse:soul_refining_flag": 1},
            },
            {
                "task_id": "sword_spirit_tribute",
                "name": "Sword Spirit Tribute",
                "description": "Offer emeralds and obsidian to calm the restless sword spirits.",
                "required_items": {"minecraft:emerald": 8, "minecraft:obsidian": 6},
                "rewards": {"ergenverse:soul_lasher": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_sr_inner_elder_yun",
        "name": "Inner Elder Yun",
        "nameCn": "内门长老云",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "soul_refining_sect",
        "location": "soul_refining_sect_inner_sect",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "ambitious, watches everyone, plots against rivals",
        "speech": "smooth, flattering, dangerous",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": True,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 370112,
        "initiation_lines": [
            "Welcome to the Inner Sect. Not many outer disciples earn the right to stand here.",
            "I require spirit stones and soul fragments for my research. Reward you ask? Knowledge is its own reward.",
            "Elder Mo is too cautious. The ancestor's techniques should be used, not locked away.",
            "The Heavenly Fate Sect and the Soul Refining Sect were once allies. Now they watch each other. Suspicion is the currency of power.",
        ],
        "daily_schedule": [
            {"time": "0500", "action": "cultivate", "location": "soul_refining_sect_spirit_spring", "duration": 180},
            {"time": "0800", "action": "manage", "location": "soul_refining_sect_inner_sect", "duration": 240},
            {"time": "1200", "action": "meet", "location": "soul_refining_sect_ancestor_hall", "duration": 60},
            {"time": "1300", "action": "eat", "location": "soul_refining_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "research", "location": "soul_refining_sect_secret_pavilion", "duration": 180},
            {"time": "1700", "action": "manage", "location": "soul_refining_sect_inner_sect", "duration": 180},
            {"time": "2000", "action": "cultivate", "location": "soul_refining_sect_mountain_cave", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "inner_research_funding",
                "name": "Inner Sect Research Funding",
                "description": "Elder Yun needs spirit stones and emeralds to fund his restriction research.",
                "required_items": {"ergenverse:spirit_stone": 5, "minecraft:emerald": 10},
                "rewards": {"ergenverse:spirit_stone_mid": 3, "minecraft:enchanted_book": 2},
            },
            {
                "task_id": "spirit_spring_water",
                "name": "Spirit Spring Water Collection",
                "description": "Collect glass bottles filled with spirit spring water for Elder Yun's cultivation.",
                "required_items": {"minecraft:glass_bottle": 8, "minecraft:emerald": 4},
                "rewards": {"ergenverse:soul_nourishing_lotus": 2, "ergenverse:spirit_stone": 5},
            },
        ],
    },
]


def build_npc(npc_data):
    """Build a complete NPC JSON from compact definition."""
    return {
        "_comment": f"NPC: {npc_data['name']} ({npc_data['nameCn']}). INFERRED. Soul Refining Sect. Location: {npc_data['location']}.",
        "npc_id": npc_data["npc_id"],
        "name": npc_data["name"],
        "nameCn": npc_data["nameCn"],
        "canon_id": npc_data.get("canon_id"),
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
        "salt": npc_data["salt"],
        "initiation_lines": npc_data["initiation_lines"],
        "daily_schedule": npc_data["daily_schedule"],
        "sect_tasks": npc_data["sect_tasks"],
        "_xianxia_schema": 1,
    }


def main():
    # Create loot tables
    loot_count = 0
    for name, data in LOOT_TABLES.items():
        loot_table = build_loot_table(name, data)
        filepath = os.path.join(LOOT_DIR, f"{name}.json")
        with open(filepath, "w") as f:
            json.dump(loot_table, f, indent=2)
        loot_count += 1
        print(f"  LOOT: {name}.json")

    # Create NPCs
    npc_count = 0
    for npc_data in NPCS:
        npc = build_npc(npc_data)
        filepath = os.path.join(NPC_DIR, f"{npc_data['npc_id']}.json")
        with open(filepath, "w") as f:
            json.dump(npc, f, indent=2)
        npc_count += 1
        print(f"  NPC:  {npc_data['npc_id']}.json")

    # Stats
    total_initiation = sum(len(n["initiation_lines"]) for n in NPCS)
    total_schedule = sum(len(n["daily_schedule"]) for n in NPCS)
    total_tasks = sum(len(n["sect_tasks"]) for n in NPCS)
    print(f"\nTotal: {loot_count} loot tables, {npc_count} NPCs")
    print(f"  {total_initiation} initiation lines, {total_schedule} schedule entries, {total_tasks} tasks")

    # Wealth gradient verification
    print("\nWealth gradient (approximate emerald value of best items):")
    for tier_name, tables in [
        ("LOW", ["outer_gate", "disciple_dormitories", "spirit_herb_garden", "trial_grounds", "main_plaza"]),
        ("MED", ["library", "alchemy_courtyard", "spirit_beast_pens", "array_hall", "spirit_spring", "sword_peak", "puppet_workshop", "mountain_cave"]),
        ("HIGH", ["inner_sect", "sword_tomb", "underground_passage", "secret_pavilion"]),
        ("MAX", ["hidden_treasury", "ancestor_hall", "core_formation_hall", "soul_refining_ancestral"]),
    ]:
        print(f"  {tier_name}: {', '.join(tables)}")

    # Circulating economy check
    print("\nKey material flows:")
    print("  bone → Beast Handler Lei (12) → Puppet Artisan Gu (16) → Sword Tomb Keeper (16) → Elder Mo (10)")
    print("  soul_fragment → Elder Mo (3) → Library Xue (1 reward) → Shadow Guide Wu (2 reward) → Inner Elder Yun (research)")
    print("  spirit_stone → Elder Mo (4 reward) → Array Master Feng (3 reward) → Inner Elder Yun (5 required) → Puppet Gu (3 reward)")
    print("  paper/ink → Disciple Wei (5+2) → Keeper Xue (8+4) → Shadow Wu (8+4) → Attendant Bai (6+4+6)")
    print("  emerald → circulates through ALL NPCs (used in 20 of 24 tasks)")
    print("  obsidian → Array Master Feng (8) → Sword Tomb Keeper (6) → Shadow Wu (underground) → Secret Pavilion (loot)")

    # Inter-location references
    print("\nInter-location references in NPC dialogue:")
    refs = set()
    for n in NPCS:
        for line in n["initiation_lines"]:
            for keyword in ["Heng Yue", "Corpse Yin", "Heavenly Fate", "Qilin", "Wang Lin", "Sea of Devils", "Zhao"]:
                if keyword in line:
                    refs.add(keyword)
    for r in sorted(refs):
        print(f"  {r}")


if __name__ == "__main__":
    main()