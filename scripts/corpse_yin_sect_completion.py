#!/usr/bin/env python3
"""
AUTO-CANON-038: Corpse Yin Sect (尸阴宗) completion.
20 district loot tables + 12 INFERRED NPCs with schedules + tasks.

Corpse Yin Sect theme: Corpse refinement (炼尸), bone manipulation, undead servants,
dark yin energy. They refine corpses of cultivators and beasts into powerful
corpse soldiers. Feared across the cultivation world.

Wealth gradient: LOW (outer_gate, main_plaza, dormitories, herb_garden, spirit_spring,
trial_grounds) -> MED (library, alchemy, sword_peak, beast_pens, array_hall,
puppet_workshop, mountain_cave) -> HIGH (underground_passage, secret_pavilion,
inner_sect, hidden_treasury, sword_tomb) -> MAX (ancestor_hall, core_formation_hall).

Existing orphan: corpse_yin_ancestor.json (powerful mod items: spirit_stone, soul_fragment,
wither_skeleton_skull, soul_nourishing_lotus, ghast_tear, realm_sealing_flag, blood_essence,
nether_core, jade_slip, eighteen_hell_stamp). Keep as-is.

Loot theme: bone and ink_sac appear in EVERY table (corpse refinement materials).
rotten_flesh, ghast_tear, wither_skeleton_skull in higher tiers.
Mod items (corpse_yin_corpse_soldier, blood_essence, soul_fragment, realm_sealing_flag)
scale by wealth tier.
"""

import json
import os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables/chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ── Corpse Yin Sect structure list (20 components, no parent) ──
STRUCTURES = [
    "corpse_yin_sect_outer_gate",
    "corpse_yin_sect_main_plaza",
    "corpse_yin_sect_disciple_dormitories",
    "corpse_yin_sect_spirit_herb_garden",
    "corpse_yin_sect_spirit_spring",
    "corpse_yin_sect_trial_grounds",
    "corpse_yin_sect_library",
    "corpse_yin_sect_alchemy_courtyard",
    "corpse_yin_sect_sword_peak",
    "corpse_yin_sect_spirit_beast_pens",
    "corpse_yin_sect_array_hall",
    "corpse_yin_sect_puppet_workshop",
    "corpse_yin_sect_mountain_cave",
    "corpse_yin_sect_underground_passage",
    "corpse_yin_sect_secret_pavilion",
    "corpse_yin_sect_inner_sect",
    "corpse_yin_sect_hidden_treasury",
    "corpse_yin_sect_sword_tomb",
    "corpse_yin_sect_ancestor_hall",
    "corpse_yin_sect_core_formation_hall",
]

# ── Loot table definitions ──
# Each: (filename, comment, pools)
# pool = (rolls_min, rolls_max, entries[])
# entry = (weight, item_name, count_min, count_max) or (weight, "empty")

def item(weight, name, cmin=1, cmax=1):
    return {"type": "minecraft:item", "weight": weight, "name": name,
            "functions": [{"function": "minecraft:set_count",
                           "count": {"min": cmin, "max": cmax}}] if cmin != cmax or cmin != 1 else []}

def empty(weight):
    return {"type": "minecraft:empty", "weight": weight}

def make_loot(comment, pools):
    return {"_comment": comment, "type": "minecraft:chest", "pools": [
        {"rolls": {"min": p[0], "max": p[1]}, "entries": p[2]} for p in pools
    ]}

LOOT_TABLES = {
    # ── TIER 1: LOW (outer areas, basic supplies + bone) ──
    "corpse_yin_sect_outer_gate": make_loot(
        "Corpse Yin Sect \u2014 outer_gate. Guard supplies. Corpse soldiers stationed here.",
        [
            (2, 4, [item(10, "minecraft:arrow", 4, 12), item(8, "minecraft:bone", 2, 6),
                    item(6, "minecraft:coal", 2, 5), item(5, "minecraft:bread", 1, 3),
                    item(4, "minecraft:ink_sac", 1, 2), item(3, "minecraft:iron_ingot", 1, 2)]),
            (1, 2, [item(5, "minecraft:paper", 1, 3), item(4, "minecraft:rotten_flesh", 1, 3),
                    item(2, "minecraft:leather", 1, 2)]),
        ]),
    "corpse_yin_sect_main_plaza": make_loot(
        "Corpse Yin Sect \u2014 main_plaza. Open gathering area. Minimal loot.",
        [
            (1, 2, [item(8, "minecraft:bone", 2, 5), item(6, "minecraft:bread", 1, 3),
                    item(5, "minecraft:coal", 2, 4), item(3, "minecraft:ink_sac", 1, 2)]),
            (1, 1, [item(4, "minecraft:stick", 3, 6), item(2, "minecraft:rotten_flesh", 1, 2)]),
        ]),
    "corpse_yin_sect_disciple_dormitories": make_loot(
        "Corpse Yin Sect \u2014 disciple_dormitories. Basic disciple life. Corpse preservation materials.",
        [
            (1, 3, [item(8, "minecraft:bone", 2, 6), item(6, "minecraft:bread", 1, 4),
                    item(5, "minecraft:coal", 2, 5), item(4, "minecraft:rotten_flesh", 1, 3),
                    item(3, "minecraft:ink_sac", 1, 2), item(2, "minecraft:iron_ingot", 1, 2)]),
        ]),
    "corpse_yin_sect_spirit_herb_garden": make_loot(
        "Corpse Yin Sect \u2014 spirit_herb_garden. Yin-attributed herbs. INFERRED: corpse preservation herbs.",
        [
            (2, 4, [item(8, "minecraft:bone", 2, 5), item(6, "minecraft:wheat", 2, 6),
                    item(5, "minecraft:ink_sac", 1, 3), item(4, "minecraft:bone_meal", 2, 5),
                    item(3, "minecraft:rotten_flesh", 1, 3), item(2, "minecraft:glowstone_dust", 1, 2)]),
        ]),
    "corpse_yin_sect_spirit_spring": make_loot(
        "Corpse Yin Sect \u2014 spirit_spring. Yin energy spring. INFERRED: corpse nourishment.",
        [
            (1, 3, [item(8, "minecraft:bone", 2, 5), item(6, "minecraft:glass_bottle", 1, 3),
                    item(5, "minecraft:glowstone_dust", 1, 3), item(4, "minecraft:ink_sac", 1, 2),
                    item(2, "minecraft:rotten_flesh", 1, 2)]),
        ]),
    "corpse_yin_sect_trial_grounds": make_loot(
        "Corpse Yin Sect \u2014 trial_grounds. Disciple tests involving corpse control. INFERRED.",
        [
            (2, 4, [item(8, "minecraft:bone", 3, 8), item(6, "minecraft:arrow", 4, 10),
                    item(5, "minecraft:rotten_flesh", 2, 5), item(4, "minecraft:leather", 1, 3),
                    item(3, "minecraft:iron_ingot", 1, 2), item(2, "minecraft:ink_sac", 1, 3)]),
            (1, 2, [item(4, "minecraft:emerald", 1, 3), item(3, "minecraft:coal", 2, 4)]),
        ]),

    # ── TIER 2: MED (functional areas, some mod items) ──
    "corpse_yin_sect_library": make_loot(
        "Corpse Yin Sect \u2014 library. Corpse refinement texts and forbidden knowledge.",
        [
            (2, 4, [item(8, "minecraft:bone", 2, 5), item(7, "minecraft:paper", 2, 5),
                    item(6, "minecraft:ink_sac", 2, 4), item(5, "minecraft:book", 1, 3),
                    item(4, "minecraft:coal", 2, 4), item(3, "minecraft:rotten_flesh", 1, 3)]),
            (1, 2, [item(4, "minecraft:emerald", 1, 3), item(3, "minecraft:enchanted_book"),
                    item(2, "minecraft:writable_book")]),
        ]),
    "corpse_yin_sect_alchemy_courtyard": make_loot(
        "Corpse Yin Sect \u2014 alchemy_courtyard. Yin-attribute alchemy. Corpse pills.",
        [
            (2, 4, [item(7, "minecraft:bone", 3, 6), item(6, "minecraft:glass_bottle", 2, 5),
                    item(5, "minecraft:coal", 2, 5), item(4, "minecraft:ink_sac", 1, 3),
                    item(4, "minecraft:rotten_flesh", 2, 4), item(3, "minecraft:blaze_powder", 1, 2)]),
            (1, 2, [item(4, "minecraft:emerald", 1, 3), item(2, "minecraft:ghast_tear"),
                    item(1, "ergenverse:blood_essence", 1, 1)]),
        ]),
    "corpse_yin_sect_sword_peak": make_loot(
        "Corpse Yin Sect \u2014 sword_peak. Yin-attributed sword cultivation. INFERRED.",
        [
            (2, 4, [item(7, "minecraft:bone", 2, 5), item(6, "minecraft:iron_ingot", 2, 5),
                    item(5, "minecraft:coal", 2, 4), item(4, "minecraft:ink_sac", 1, 3),
                    item(3, "minecraft:rotten_flesh", 1, 3), item(2, "minecraft:iron_sword")]),
            (1, 2, [item(4, "minecraft:emerald", 2, 4), item(2, "minecraft:enchanted_book"),
                    item(1, "minecraft:book")]),
        ]),
    "corpse_yin_sect_spirit_beast_pens": make_loot(
        "Corpse Yin Sect \u2014 spirit_beast_pens. Corpses of beasts for refinement. INFERRED.",
        [
            (2, 4, [item(8, "minecraft:bone", 4, 10), item(6, "minecraft:rotten_flesh", 3, 8),
                    item(5, "minecraft:leather", 2, 5), item(4, "minecraft:ink_sac", 1, 3),
                    item(3, "minecraft:iron_ingot", 1, 3), item(2, "minecraft:lead", 1, 3)]),
            (1, 2, [item(4, "minecraft:emerald", 1, 3), item(2, "minecraft:bone", 4, 8),
                    item(1, "minecraft:string", 2, 6)]),
        ]),
    "corpse_yin_sect_array_hall": make_loot(
        "Corpse Yin Sect \u2014 array_hall. Yin-necrotic restriction arrays. INFERRED.",
        [
            (2, 4, [item(7, "minecraft:bone", 3, 6), item(6, "minecraft:obsidian", 1, 3),
                    item(5, "minecraft:ink_sac", 2, 4), item(4, "minecraft:redstone", 2, 5),
                    item(3, "minecraft:rotten_flesh", 1, 3), item(2, "minecraft:ender_pearl", 1, 1)]),
            (1, 2, [item(4, "minecraft:emerald", 2, 5), item(2, "ergenverse:soul_fragment", 1, 1),
                    item(1, "minecraft:enchanted_book")]),
        ]),
    "corpse_yin_sect_puppet_workshop": make_loot(
        "Corpse Yin Sect \u2014 puppet_workshop. Corpse soldier construction. INFERRED.",
        [
            (2, 4, [item(8, "minecraft:bone", 4, 8), item(6, "minecraft:rotten_flesh", 2, 5),
                    item(5, "minecraft:ink_sac", 2, 4), item(4, "minecraft:leather", 2, 4),
                    item(3, "minecraft:iron_ingot", 1, 3), item(3, "minecraft:string", 2, 6)]),
            (1, 2, [item(4, "minecraft:emerald", 2, 4), item(2, "ergenverse:soul_fragment", 1, 1),
                    item(1, "minecraft:redstone", 2, 5)]),
        ]),
    "corpse_yin_sect_mountain_cave": make_loot(
        "Corpse Yin Sect \u2014 mountain_cave. Hidden corpse storage. INFERRED.",
        [
            (2, 4, [item(8, "minecraft:bone", 4, 10), item(6, "minecraft:rotten_flesh", 3, 7),
                    item(5, "minecraft:ink_sac", 2, 4), item(4, "minecraft:coal", 2, 5),
                    item(3, "minecraft:obsidian", 1, 2)]),
            (1, 2, [item(3, "minecraft:emerald", 2, 5), item(2, "ergenverse:blood_essence", 1, 1),
                    item(1, "minecraft:ghast_tear")]),
        ]),

    # ── TIER 3: HIGH (restricted areas, significant mod items) ──
    "corpse_yin_sect_underground_passage": make_loot(
        "Corpse Yin Sect \u2014 underground_passage. Smuggling route for corpses. Dark, dangerous.",
        [
            (3, 5, [item(7, "minecraft:bone", 5, 12), item(6, "minecraft:rotten_flesh", 4, 8),
                    item(5, "minecraft:ink_sac", 2, 5), item(4, "minecraft:obsidian", 2, 4),
                    item(3, "minecraft:emerald", 2, 5), item(3, "minecraft:iron_ingot", 2, 4)]),
            (1, 3, [item(4, "ergenverse:soul_fragment", 1, 2), item(3, "minecraft:ghast_tear", 1, 1),
                    item(2, "ergenverse:blood_essence", 1, 1), item(1, "minecraft:wither_skeleton_skull", 1, 1)]),
        ]),
    "corpse_yin_sect_secret_pavilion": make_loot(
        "Corpse Yin Sect \u2014 secret_pavilion. Hidden forbidden techniques. INFERRED.",
        [
            (3, 5, [item(6, "minecraft:bone", 4, 8), item(6, "minecraft:ink_sac", 3, 6),
                    item(5, "minecraft:book", 1, 3), item(4, "minecraft:obsidian", 2, 4),
                    item(3, "minecraft:emerald", 2, 5)]),
            (1, 3, [item(4, "ergenverse:soul_fragment", 1, 2), item(3, "minecraft:enchanted_book"),
                    item(2, "ergenverse:realm_sealing_flag"), item(1, "ergenverse:blood_essence", 1, 2)]),
        ]),
    "corpse_yin_sect_inner_sect": make_loot(
        "Corpse Yin Sect \u2014 inner_sect. Elder quarters, refined corpse soldiers.",
        [
            (3, 5, [item(6, "minecraft:bone", 4, 8), item(5, "minecraft:rotten_flesh", 3, 6),
                    item(5, "minecraft:ink_sac", 2, 5), item(4, "minecraft:emerald", 3, 7),
                    item(3, "minecraft:obsidian", 2, 4), item(2, "minecraft:gold_ingot", 1, 3)]),
            (1, 3, [item(4, "ergenverse:soul_fragment", 1, 2), item(3, "ergenverse:blood_essence", 1, 2),
                    item(2, "minecraft:enchanted_book"), item(1, "ergenverse:realm_sealing_flag")]),
        ]),
    "corpse_yin_sect_hidden_treasury": make_loot(
        "Corpse Yin Sect \u2014 hidden_treasury. Sect wealth storage. Corpses of powerful cultivators.",
        [
            (3, 6, [item(5, "minecraft:emerald", 3, 8), item(5, "minecraft:bone", 5, 10),
                    item(4, "minecraft:gold_ingot", 2, 5), item(4, "minecraft:ink_sac", 3, 6),
                    item(3, "minecraft:obsidian", 2, 4), item(2, "minecraft:diamond", 1, 1)]),
            (2, 3, [item(4, "ergenverse:soul_fragment", 1, 3), item(3, "ergenverse:blood_essence", 1, 2),
                    item(2, "minecraft:enchanted_book"), item(1, "ergenverse:realm_sealing_flag"),
                    item(1, "minecraft:wither_skeleton_skull", 1, 1)]),
        ]),
    "corpse_yin_sect_sword_tomb": make_loot(
        "Corpse Yin Sect \u2014 sword_tomb. Weapons of fallen cultivators. INFERRED.",
        [
            (3, 5, [item(6, "minecraft:bone", 5, 10), item(5, "minecraft:iron_ingot", 3, 6),
                    item(5, "minecraft:ink_sac", 2, 5), item(4, "minecraft:emerald", 2, 5),
                    item(3, "minecraft:obsidian", 2, 4), item(2, "minecraft:rotten_flesh", 3, 6)]),
            (1, 3, [item(3, "ergenverse:soul_fragment", 1, 2), item(2, "minecraft:enchanted_book"),
                    item(2, "minecraft:ghast_tear", 1, 1), item(1, "minecraft:wither_skeleton_skull", 1, 1)]),
        ]),

    # ── TIER 4: MAX (inner sanctum, ancestor-level) ──
    "corpse_yin_sect_ancestor_hall": make_loot(
        "Corpse Yin Sect \u2014 ancestor_hall. Corpse Yin Ancestor's shrine. Highest yin concentration.",
        [
            (3, 6, [item(5, "minecraft:emerald", 4, 10), item(5, "minecraft:bone", 6, 12),
                    item(4, "minecraft:obsidian", 3, 6), item(4, "minecraft:ink_sac", 3, 6),
                    item(3, "minecraft:gold_ingot", 2, 5), item(2, "minecraft:diamond", 1, 2)]),
            (2, 3, [item(4, "ergenverse:soul_fragment", 1, 3), item(3, "ergenverse:blood_essence", 1, 2),
                    item(2, "ergenverse:realm_sealing_flag"), item(2, "minecraft:enchanted_book"),
                    item(1, "minecraft:wither_skeleton_skull", 1, 2), item(1, "minecraft:crying_obsidian", 1, 2)]),
            (0, 2, [item(2, "ergenverse:nether_core"), item(1, "ergenverse:spirit_stone_high", 1, 2),
                    item(2, "ergenverse:jade_slip"), empty(5)]),
        ]),
    "corpse_yin_sect_core_formation_hall": make_loot(
        "Corpse Yin Sect \u2014 core_formation_hall. Elder core formation. Advanced corpse refinement.",
        [
            (3, 6, [item(5, "minecraft:emerald", 4, 10), item(5, "minecraft:bone", 5, 10),
                    item(4, "minecraft:obsidian", 3, 6), item(4, "minecraft:ink_sac", 3, 6),
                    item(3, "minecraft:gold_ingot", 2, 5), item(2, "minecraft:diamond", 1, 2)]),
            (2, 3, [item(4, "ergenverse:soul_fragment", 2, 4), item(3, "ergenverse:blood_essence", 1, 3),
                    item(2, "ergenverse:realm_sealing_flag"), item(2, "minecraft:enchanted_book"),
                    item(1, "minecraft:wither_skeleton_skull", 1, 1)]),
            (0, 2, [item(2, "ergenverse:nether_core"), item(1, "ergenverse:spirit_stone_high", 1, 2),
                    item(1, "ergenverse:eighteen_hell_stamp"), empty(6)]),
        ]),
}


# ── NPC definitions ──
# 12 INFERRED NPCs covering all major areas
NPCS = [
    {
        "npc_id": "npc_cy_outer_disciple_hei",
        "name": "Outer Disciple Hei",
        "nameCn": "外门弟子黑",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_outer_gate",
        "cultivation": "CONDENSATION early",
        "personality": "gaunt, nervous, whispers, avoids eye contact",
        "speech": "quiet, hesitant, fearful",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380101,
        "initiation_lines": [
            "The Corpse Yin Sect does not welcome visitors. State your purpose or leave.",
            "You can smell it, can you not? The yin energy seeps from the ground here.",
            "Do not wander. The corpse soldiers patrol at night and they do not distinguish friend from foe.",
            "The ancestors say a great one will rise from our sect. I pray I live to see it."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "patrol", "location": "corpse_yin_sect_outer_gate", "duration": 120},
            {"time": "0800", "action": "stand_guard", "location": "corpse_yin_sect_outer_gate", "duration": 240},
            {"time": "1200", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "patrol", "location": "corpse_yin_sect_main_plaza", "duration": 120},
            {"time": "1500", "action": "train", "location": "corpse_yin_sect_trial_grounds", "duration": 120},
            {"time": "1700", "action": "stand_guard", "location": "corpse_yin_sect_outer_gate", "duration": 180},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "gate_patrol_bones",
                "name": "Gate Patrol Bone Supply",
                "description": "The outer gate corpse soldiers need fresh bones for repair. Bring them.",
                "required_items": {"minecraft:arrow": 8, "minecraft:bone": 6},
                "rewards": {"minecraft:emerald": 5, "minecraft:book": 1},
            },
            {
                "task_id": "visitor_registry_ink",
                "name": "Visitor Registry Ink",
                "description": "We record all who enter the sect. The yin-infused ink preserves the names of the dead.",
                "required_items": {"minecraft:paper": 5, "minecraft:ink_sac": 3},
                "rewards": {"minecraft:emerald": 4, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cy_corpse_refiner_shen",
        "name": "Corpse Refiner Shen",
        "nameCn": "炼尸师沈",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_puppet_workshop",
        "cultivation": "NASCENT_SOUL early",
        "personality": "obsessive, meticulous, speaks to corpses as if alive",
        "speech": "clinical, detached, occasionally tender toward subjects",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380102,
        "initiation_lines": [
            "This one has good bone structure. A Condensation cultivator, perhaps. Yes, the marrow is still fresh.",
            "Do not call them corpses. They are preserved vessels awaiting purpose. There is a difference.",
            "The Soul Refining Sect thinks they understand souls. They play with wisps. We give the dead form.",
            "I need more bone and sinew. The corpse soldiers in the outer patrols are deteriorating."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "work", "location": "corpse_yin_sect_puppet_workshop", "duration": 300},
            {"time": "1000", "action": "inspect", "location": "corpse_yin_sect_spirit_beast_pens", "duration": 120},
            {"time": "1200", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "work", "location": "corpse_yin_sect_puppet_workshop", "duration": 240},
            {"time": "1700", "action": "study", "location": "corpse_yin_sect_library", "duration": 120},
            {"time": "1900", "action": "work", "location": "corpse_yin_sect_puppet_workshop", "duration": 180},
            {"time": "2200", "action": "rest", "location": "corpse_yin_sect_inner_sect", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "corpse_repair_materials",
                "name": "Corpse Repair Materials",
                "description": "The outer patrol soldiers need bone and sinew for emergency repairs. Do not ask what sinew is.",
                "required_items": {"minecraft:bone": 8, "minecraft:leather": 4, "minecraft:string": 6},
                "rewards": {"minecraft:emerald": 8, "ergenverse:soul_fragment": 1},
            },
            {
                "task_id": "preservation_ink_formula",
                "name": "Preservation Ink Formula",
                "description": "I need ink and blood to prepare the preservation formula for the new batch.",
                "required_items": {"minecraft:ink_sac": 4, "minecraft:glass_bottle": 3, "minecraft:rotten_flesh": 3},
                "rewards": {"minecraft:emerald": 7, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cy_library_curator_po",
        "name": "Library Curator Po",
        "nameCn": "藏经阁主薄",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_library",
        "cultivation": "NASCENT_SOUL early",
        "personality": "elegant, soft-spoken, unsettling calm",
        "speech": "measured, poetic, drops hints about forbidden knowledge",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380103,
        "initiation_lines": [
            "The library holds three thousand scrolls on the art of preservation. Some were written on the skin of the authors.",
            "Knowledge of death is the most valuable knowledge. All other cultivation paths end here eventually.",
            "The Array Master asked for scrolls on yin-necrotic formations. I refused. His techniques are too crude.",
            "Bring me paper and ink, and I will share what the dead have taught us."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "organize", "location": "corpse_yin_sect_library", "duration": 240},
            {"time": "1000", "action": "study", "location": "corpse_yin_sect_library", "duration": 180},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "teach", "location": "corpse_yin_sect_main_plaza", "duration": 120},
            {"time": "1600", "action": "study", "location": "corpse_yin_sect_library", "duration": 240},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "scroll_copying_preservation",
                "name": "Scroll Copying: Preservation Arts",
                "description": "I need materials to copy the forbidden preservation scrolls for the inner elders.",
                "required_items": {"minecraft:paper": 6, "minecraft:ink_sac": 4},
                "rewards": {"minecraft:book": 2, "minecraft:emerald": 5},
            },
            {
                "task_id": "forbidden_text_retrieval",
                "name": "Forbidden Text Retrieval",
                "description": "A scroll was taken from the restricted section. Return it and I will reward you with knowledge.",
                "required_items": {"minecraft:book": 1, "minecraft:emerald": 3},
                "rewards": {"minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cy_yin_alchemist_gui",
        "name": "Yin Alchemist Gui",
        "nameCn": "阴丹师桂",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_alchemy_courtyard",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "coughing, pale, surrounded by fumes",
        "speech": "wheezing, distracted, mutters formulae",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380104,
        "initiation_lines": [
            "Cough, cough. The Yin Corpse Pill requires fresh bone marrow and ghast tears. Do you have any?",
            "The last batch exploded. Three disciples were... repurposed. Progress demands sacrifice.",
            "The Soul Refining Sect's alchemist visited once. He vomited when he saw my ingredients.",
            "Bring me bottles and coal. The furnace must burn without flame — yin fire feeds on ash."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "gather", "location": "corpse_yin_sect_spirit_herb_garden", "duration": 120},
            {"time": "0700", "action": "work", "location": "corpse_yin_sect_alchemy_courtyard", "duration": 300},
            {"time": "1200", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "work", "location": "corpse_yin_sect_alchemy_courtyard", "duration": 300},
            {"time": "1800", "action": "study", "location": "corpse_yin_sect_library", "duration": 120},
            {"time": "2000", "action": "work", "location": "corpse_yin_sect_alchemy_courtyard", "duration": 180},
            {"time": "2300", "action": "rest", "location": "corpse_yin_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "yin_fuel_supplies",
                "name": "Yin Fire Fuel",
                "description": "The yin furnace requires specific fuel. No normal flame can refine corpse pills.",
                "required_items": {"minecraft:glass_bottle": 4, "minecraft:coal": 6},
                "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 6},
            },
            {
                "task_id": "corpse_pill_ingredients",
                "name": "Corpse Pill Ingredients",
                "description": "I need blood essence and blaze powder for the Yin Corpse Pill refinement.",
                "required_items": {"ergenverse:blood_essence": 1, "minecraft:blaze_powder": 2},
                "rewards": {"minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 2},
            },
        ],
    },
    {
        "npc_id": "npc_cy_array_master_luo",
        "name": "Array Master Luo",
        "nameCn": "阵法师罗",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_array_hall",
        "cultivation": "NASCENT_SOUL late",
        "personality": "paranoid, checks arrays constantly, trusts no one",
        "speech": "precise, clipped, references measurements",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380105,
        "initiation_lines": [
            "The yin-necrotic array at the eastern perimeter is three inches off alignment. Unacceptable.",
            "The Soul Refining Sect's array master came to study our formations. I showed him the outer ring. The inner ring would break his mind.",
            "Obsidian conducts yin energy better than any other material. That is why this hall is built from it.",
            "If you disturb any array inscriptions while walking, I will have you repurposed. Politely."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "inspect", "location": "corpse_yin_sect_array_hall", "duration": 180},
            {"time": "0800", "action": "repair", "location": "corpse_yin_sect_outer_gate", "duration": 120},
            {"time": "1000", "action": "work", "location": "corpse_yin_sect_array_hall", "duration": 240},
            {"time": "1400", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1500", "action": "inspect", "location": "corpse_yin_sect_underground_passage", "duration": 120},
            {"time": "1700", "action": "work", "location": "corpse_yin_sect_array_hall", "duration": 180},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_inner_sect", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "array_repair_obsidian",
                "name": "Array Repair: Obsidian Inscription",
                "description": "The eastern perimeter array needs obsidian and ender pearls for the yin conduit.",
                "required_items": {"minecraft:obsidian": 4, "minecraft:ender_pearl": 1},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "perimeter_redstone",
                "name": "Perimeter Redstone Supply",
                "description": "The corpse soldier patrol paths need redstone for their activation circuits.",
                "required_items": {"minecraft:redstone": 8, "minecraft:iron_ingot": 4},
                "rewards": {"minecraft:emerald": 8, "ergenverse:soul_fragment": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cy_beast_keeper_mang",
        "name": "Beast Keeper Mang",
        "nameCn": "灵兽管事莽",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_spirit_beast_pens",
        "cultivation": "CONDENSATION late",
        "personality": "brutal, efficient, treats beasts as raw materials",
        "speech": "gruff, dismissive, businesslike",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380106,
        "initiation_lines": [
            "The beasts in the eastern pen are ready for refinement. The ones in the western pen need another week.",
            "Qilin City merchants bring the best specimens. Their beast trade feeds our entire operation.",
            "A corpse soldier made from a Foundation Establishment beast is worth ten made from mortals.",
            "Do not feed them. They are not pets. They are materials awaiting transformation."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "work", "location": "corpse_yin_sect_spirit_beast_pens", "duration": 300},
            {"time": "1100", "action": "inspect", "location": "corpse_yin_sect_mountain_cave", "duration": 120},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "work", "location": "corpse_yin_sect_spirit_beast_pens", "duration": 240},
            {"time": "1800", "action": "deliver", "location": "corpse_yin_sect_puppet_workshop", "duration": 120},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "beast_pen_reinforcement",
                "name": "Beast Pen Reinforcement",
                "description": "The pens need lead bars and iron to prevent escapes. The last one killed two disciples.",
                "required_items": {"minecraft:iron_ingot": 4, "minecraft:lead": 3},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:emerald": 5},
            },
            {
                "task_id": "raw_material_delivery",
                "name": "Raw Material Delivery",
                "description": "Bring bone and leather from the latest beast haul to the Refiner.",
                "required_items": {"minecraft:bone": 10, "minecraft:leather": 6},
                "rewards": {"minecraft:emerald": 7, "minecraft:arrow": 12},
            },
        ],
    },
    {
        "npc_id": "npc_cy_trial_overseer_zha",
        "name": "Trial Overseer Zha",
        "nameCn": "试炼官扎",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_trial_grounds",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "cold, evaluative, records everything",
        "speech": "flat, administrative, references statistics",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380107,
        "initiation_lines": [
            "Seventy-three percent of disciples fail the corpse bonding trial on the first attempt. Thirty-one percent never attempt it again.",
            "The trial is simple: bond with a corpse soldier and command it to walk. Those who hesitate are removed from the program.",
            "We do not waste resources. Failed disciples are... reassigned to other duties within the sect.",
            "The records show that disciples from the Wang Country region have the highest success rate. Interesting."
        ],
        "daily_schedule": [
            {"time": "0700", "action": "oversee", "location": "corpse_yin_sect_trial_grounds", "duration": 240},
            {"time": "1100", "action": "evaluate", "location": "corpse_yin_sect_main_plaza", "duration": 120},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "oversee", "location": "corpse_yin_sect_trial_grounds", "duration": 240},
            {"time": "1800", "action": "report", "location": "corpse_yin_sect_inner_sect", "duration": 120},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "trial_equipment_supply",
                "name": "Trial Equipment Supply",
                "description": "The trial grounds need arrows for the combat portion and leather for the binding portion.",
                "required_items": {"minecraft:arrow": 12, "minecraft:leather": 5},
                "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2},
            },
            {
                "task_id": "failed_disciple_recovery",
                "name": "Failed Disciple Recovery",
                "description": "A disciple failed the trial. Recover the bone fragments and bring them to the puppet workshop.",
                "required_items": {"minecraft:bone": 8, "minecraft:rotten_flesh": 4},
                "rewards": {"ergenverse:soul_fragment": 1, "minecraft:emerald": 5},
            },
        ],
    },
    {
        "npc_id": "npc_cy_sword_caretaker_yan",
        "name": "Sword Caretaker Yan",
        "nameCn": "剑冢守燕",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_sword_tomb",
        "cultivation": "NASCENT_SOUL late",
        "personality": "melancholic, speaks to swords, grieving",
        "speech": "soft, reverent, tells stories of the fallen",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380108,
        "initiation_lines": [
            "Each sword here once belonged to a cultivator. Some were our enemies. Some were our own.",
            "The Ancestor says weapons retain a fragment of their wielder's soul. I believe him. I hear them at night.",
            "Do not touch the black iron sword in the center. It belongs to someone who is not yet dead.",
            "The Corpse Refiner wants to melt these swords for materials. I will not allow it. Not while I breathe."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "corpse_yin_sect_sword_tomb", "duration": 180},
            {"time": "0800", "action": "maintain", "location": "corpse_yin_sect_sword_tomb", "duration": 300},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "maintain", "location": "corpse_yin_sect_sword_tomb", "duration": 240},
            {"time": "1800", "action": "patrol", "location": "corpse_yin_sect_sword_peak", "duration": 120},
            {"time": "2000", "action": "meditate", "location": "corpse_yin_sect_sword_tomb", "duration": 180},
            {"time": "2300", "action": "rest", "location": "corpse_yin_sect_sword_tomb", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "sword_preservation",
                "name": "Sword Preservation Materials",
                "description": "The swords need oil and cloth to prevent yin corrosion. Bring iron and bone for the preservation ritual.",
                "required_items": {"minecraft:iron_ingot": 6, "minecraft:bone": 8},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "forbidden_sword_retrieval",
                "name": "Forbidden Sword Retrieval",
                "description": "A sword was removed from the tomb. Find it and return it before the Ancestor notices.",
                "required_items": {"minecraft:emerald": 6, "minecraft:obsidian": 4},
                "rewards": {"ergenverse:realm_sealing_flag": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cy_underground_guide_mo",
        "name": "Underground Guide Mo",
        "nameCn": "地下通道向导莫",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_underground_passage",
        "cultivation": "FOUNDATION_ESTABLISHMENT late",
        "personality": "shifty-eyed, greedy, knows too many secrets",
        "speech": "whispered, conspiratorial, always looking over shoulder",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380109,
        "initiation_lines": [
            "You found the passage. Not many do. The elders do not know I use it for... supplementary income.",
            "Bodies come through here. Not just beasts. Cultivators too, sometimes. I do not ask questions.",
            "The smuggling route goes to the Sea of Devils. The Soul Refining Sect pays well for fresh specimens.",
            "Bring me gold and emeralds. I can get you things the elders keep locked in the treasury."
        ],
        "daily_schedule": [
            {"time": "0800", "action": "idle", "location": "corpse_yin_sect_underground_passage", "duration": 180},
            {"time": "1100", "action": "smuggle", "location": "corpse_yin_sect_underground_passage", "duration": 120},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "idle", "location": "corpse_yin_sect_underground_passage", "duration": 180},
            {"time": "1700", "action": "smuggle", "location": "corpse_yin_sect_underground_passage", "duration": 120},
            {"time": "1900", "action": "deliver", "location": "corpse_yin_sect_hidden_treasury", "duration": 60},
            {"time": "2000", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "smuggling_route_supplies",
                "name": "Smuggling Route Supplies",
                "description": "The passage needs ink and paper to forge transit documents for the shipments.",
                "required_items": {"minecraft:ink_sac": 4, "minecraft:paper": 6},
                "rewards": {"ergenverse:soul_fragment": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "underground_treasure_access",
                "name": "Underground Treasury Access",
                "description": "I can get you into the treasury through the tunnels. The price is gold and emeralds.",
                "required_items": {"minecraft:gold_ingot": 3, "minecraft:emerald": 8},
                "rewards": {"ergenverse:blood_essence": 2, "ergenverse:soul_fragment": 2},
            },
        ],
    },
    {
        "npc_id": "npc_cy_ancestor_attendant_hui",
        "name": "Ancestor Attendant Hui",
        "nameCn": "祖师殿侍惠",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_ancestor_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "fanatical, eyes vacant, speaks as if possessed",
        "speech": "reverent, prophetic, third-person references to the Ancestor",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380110,
        "initiation_lines": [
            "The Ancestor's will permeates this hall. Can you feel it? The yin energy responds to his presence.",
            "He does not sleep. He waits. The Corpse Yin Ancestor has waited for three thousand years.",
            "The ancestor's journals mention a cultivator named Wang Lin. The name is circled. Three times.",
            "Bring offerings of gold and obsidian. The Ancestor appreciates devotion. He rewards it. In time."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "corpse_yin_sect_ancestor_hall", "duration": 240},
            {"time": "0900", "action": "ritual", "location": "corpse_yin_sect_ancestor_hall", "duration": 180},
            {"time": "1200", "action": "eat", "location": "corpse_yin_sect_inner_sect", "duration": 60},
            {"time": "1300", "action": "maintain", "location": "corpse_yin_sect_ancestor_hall", "duration": 240},
            {"time": "1700", "action": "ritual", "location": "corpse_yin_sect_ancestor_hall", "duration": 180},
            {"time": "2000", "action": "meditate", "location": "corpse_yin_sect_ancestor_hall", "duration": 300},
            {"time": "0100", "action": "rest", "location": "corpse_yin_sect_ancestor_hall", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "ancestor_offering_gold",
                "name": "Ancestor Offering: Gold and Obsidian",
                "description": "The ancestor's shrine requires gold and obsidian for the weekly offering ritual.",
                "required_items": {"minecraft:gold_ingot": 4, "minecraft:obsidian": 6},
                "rewards": {"ergenverse:soul_fragment": 3, "minecraft:emerald": 6},
            },
            {
                "task_id": "ancestor_journal_copying",
                "name": "Ancestor Journal Copying",
                "description": "Copy the ancestor's most recent revelations. The ink must be fresh and the paper pure.",
                "required_items": {"minecraft:paper": 8, "minecraft:ink_sac": 5, "minecraft:book": 2},
                "rewards": {"ergenverse:realm_sealing_flag": 1, "ergenverse:soul_fragment": 2},
            },
        ],
    },
    {
        "npc_id": "npc_cy_inner_elder_qiu",
        "name": "Inner Elder Qiu",
        "nameCn": "内门长老邱",
        "canon_id": None,
        "type": "sect_elder",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_core_formation_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "ambitious, calculating, watches everyone",
        "speech": "measured, strategic, drops veiled threats",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380111,
        "initiation_lines": [
            "The Corpse Yin Ancestor's techniques can raise the dead. But can they create life? That is the question that drives me.",
            "The Heavenly Fate Sect proposed an alliance last year. We refused. Their fate manipulation is child's play compared to our arts.",
            "The Sea of Devils holds secrets even the Ancestor has not fully explored. I intend to be the one who finds them.",
            "Bring me spirit stones and emeralds. I am conducting research that could change the balance of power."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "research", "location": "corpse_yin_sect_core_formation_hall", "duration": 300},
            {"time": "1000", "action": "inspect", "location": "corpse_yin_sect_inner_sect", "duration": 120},
            {"time": "1200", "action": "meet", "location": "corpse_yin_sect_secret_pavilion", "duration": 120},
            {"time": "1400", "action": "research", "location": "corpse_yin_sect_core_formation_hall", "duration": 240},
            {"time": "1800", "action": "inspect", "location": "corpse_yin_sect_hidden_treasury", "duration": 120},
            {"time": "2000", "action": "research", "location": "corpse_yin_sect_core_formation_hall", "duration": 180},
            {"time": "2300", "action": "rest", "location": "corpse_yin_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "core_formation_research",
                "name": "Core Formation Research Materials",
                "description": "I need spirit stones and emeralds to power the formation research array.",
                "required_items": {"ergenverse:soul_fragment": 3, "minecraft:emerald": 8},
                "rewards": {"minecraft:enchanted_book": 2, "ergenverse:blood_essence": 2},
            },
            {
                "task_id": "sea_of_devils_expedition",
                "name": "Sea of Devils Expedition Supplies",
                "description": "Prepare supplies for the expedition into the Sea of Devils. We search for the Ancestor's original tomb.",
                "required_items": {"minecraft:glass_bottle": 6, "minecraft:emerald": 10, "minecraft:obsidian": 8},
                "rewards": {"ergenverse:realm_sealing_flag": 1, "ergenverse:soul_fragment": 3},
            },
        ],
    },
    {
        "npc_id": "npc_cy_herb_gardener_sang",
        "name": "Herb Gardener Sang",
        "nameCn": "药园管事桑",
        "canon_id": None,
        "type": "sect_disciple",
        "faction": "corpse_yin_sect",
        "location": "corpse_yin_sect_spirit_herb_garden",
        "cultivation": "FOUNDATION_ESTABLISHMENT mid",
        "personality": "hunched, soil-stained, hums to the plants",
        "speech": "gentle, distracted, botanical",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": False,
        "canon_confidence": 0,
        "derivation_type": "I",
        "salt": 380112,
        "initiation_lines": [
            "The yin-soil here grows herbs that wither in sunlight. This garden has not seen the sun in four hundred years.",
            "Bone meal from Foundation Establishment beasts makes the best fertilizer. Condensation bones are too weak.",
            "The Alchemist needs the black lotus from the spring. I grow it here. The petals are the color of a bruise.",
            "Bring me wheat and bone meal. The corpse nourishment herbs need feeding, and I am running low."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "tend", "location": "corpse_yin_sect_spirit_herb_garden", "duration": 300},
            {"time": "1100", "action": "gather", "location": "corpse_yin_sect_spirit_spring", "duration": 120},
            {"time": "1300", "action": "eat", "location": "corpse_yin_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "tend", "location": "corpse_yin_sect_spirit_herb_garden", "duration": 240},
            {"time": "1800", "action": "deliver", "location": "corpse_yin_sect_alchemy_courtyard", "duration": 60},
            {"time": "1900", "action": "rest", "location": "corpse_yin_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {
                "task_id": "garden_fertilizer",
                "name": "Garden Fertilizer Supply",
                "description": "The corpse nourishment herbs need bone meal and wheat to grow in yin-soil.",
                "required_items": {"minecraft:bone": 6, "minecraft:wheat": 8},
                "rewards": {"minecraft:emerald": 5, "minecraft:glowstone_dust": 3},
            },
            {
                "task_id": "herb_delivery_alchemist",
                "name": "Herb Delivery to Alchemist",
                "description": "The Alchemist needs fresh herbs and bottles from the garden and spring.",
                "required_items": {"minecraft:glass_bottle": 3, "minecraft:bone_meal": 4},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1},
            },
        ],
    },
]


def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)

    # Write loot tables
    loot_count = 0
    for struct_name, loot_data in LOOT_TABLES.items():
        filename = struct_name.replace("corpse_yin_sect_", "corpse_yin_sect_") + ".json"
        filepath = os.path.join(LOOT_DIR, filename)
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(loot_data, f, indent=2, ensure_ascii=False)
        loot_count += 1
        print(f"  [LOOT] {filename}")

    # Write NPCs
    npc_count = 0
    for npc_data in NPCS:
        filename = npc_data["npc_id"] + ".json"
        filepath = os.path.join(NPC_DIR, filename)
        npc_data["_xianxia_schema"] = 1
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(npc_data, f, indent=2, ensure_ascii=False)
        npc_count += 1
        print(f"  [NPC]  {filename}")

    print(f"\nDone: {loot_count} loot tables + {npc_count} NPCs created for Corpse Yin Sect.")
    print(f"Existing orphan kept: corpse_yin_ancestor.json")
    print(f"Total loot tables: {loot_count + 1} (20 new + 1 orphan)")


if __name__ == "__main__":
    main()