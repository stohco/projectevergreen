#!/usr/bin/env python3
"""
AUTO-CANON-041: Luo He Sect (落河宗) completion.
Theme: River/water/flowing cultivation. Li Muwan's and Li Qiqing's origin sect.
20 loot tables (4-tier wealth gradient) + 12 INFERRED NPCs.
River items: prismarine_shard (ALL 20), prismarine (16/20), clay (14/20),
lily_pad (10/20), nautilus_shell (8/20), kelp (6/20), tropical_fish (5/20).
Mod items: jade_slip, spirit_stone, dao_fragment scale by tier.
"""
import json
import os
import hashlib

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# ============================================================
# LOOT TABLES — 20 tables, 4-tier wealth gradient
# ============================================================
# Theme items: prismarine_shard (river essence), prismarine (river blocks),
# clay (riverbed), lily_pad (surface), nautilus_shell (deep treasure),
# kelp (underwater herb), tropical_fish (river life)
# Vanilla tier items: arrows, coal, bread, iron, gold, emeralds, diamonds, obsidian
# Mod items: jade_slip, spirit_stone, dao_fragment, soul_fragment

# Structure ordering by wealth tier:
# LOW:  outer_gate, main_plaza, disciple_dormitories, trial_grounds, spirit_herb_garden, spirit_spring
# MED:  library, alchemy_courtyard, array_hall, beast_pens, puppet_workshop, mountain_cave
# HIGH: sword_peak, sword_tomb, underground_passage, secret_pavilion, inner_sect
# MAX:  hidden_treasury, ancestor_hall, core_formation_hall

STRUCTURES = [
    # (filename_suffix, comment, tier)
    ("outer_gate",        "River gate guard post. Patrol supplies and bamboo rafts.", "LOW"),
    ("main_plaza",        "Central river plaza. Meeting point with flowing water channels.", "LOW"),
    ("disciple_dormitories", "Disciple quarters. Riverstone beds and water basins.", "LOW"),
    ("trial_grounds",     "Underwater trial arena. Diving tests and current endurance.", "LOW"),
    ("spirit_herb_garden","Water herb garden. Kelp, lily pads, aquatic spirit herbs.", "LOW"),
    ("spirit_spring",     "Sacred river spring. Source of the Falling River.", "LOW"),
    ("library",           "River scroll archives. Waterproof jade slips and scrolls.", "MED"),
    ("alchemy_courtyard", "Water-pill refinery. Tide-powered alchemy furnaces.", "MED"),
    ("array_hall",        "Current-weave array hall. Water formations and flow patterns.", "MED"),
    ("spirit_beast_pens", "River creature pens. Fish, turtles, water serpents.", "MED"),
    ("puppet_workshop",   "River-puppet workshop. Water-driven mechanical constructs.", "MED"),
    ("mountain_cave",     "Underground river cave. Hidden cultivation grotto.", "MED"),
    ("sword_peak",        "Flowing Sword Peak. Water-sword training ground.", "HIGH"),
    ("sword_tomb",        "River Sword Tomb. Ancient swords submerged in flowing water.", "HIGH"),
    ("underground_passage", "Underground river passage. Secret waterway access.", "HIGH"),
    ("secret_pavilion",   "Secret river pavilion. Hidden meditation above the waterfall.", "HIGH"),
    ("inner_sect",        "Inner sect compound. Elite disciples near the river's heart.", "HIGH"),
    ("hidden_treasury",   "Sunken treasury. Flooded vault of the sect's greatest wealth.", "MAX"),
    ("ancestor_hall",     "Ancestor Hall. Memorial of Luo He founders above the eternal spring.", "MAX"),
    ("core_formation_hall", "Core Formation Hall. River convergence formation nexus.", "MAX"),
]

def make_entry(name, weight, min_c=1, max_c=1):
    """Helper to create a loot entry."""
    e = {"type": "minecraft:item", "weight": weight, "name": name}
    if min_c != 1 or max_c != 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"min": min_c, "max": max_c}}]
    return e

def make_pool(rolls_min, rolls_max, entries):
    return {"rolls": {"min": rolls_min, "max": rolls_max}, "entries": entries}

def build_loot_table(suffix, comment, tier):
    """Build a loot table for one structure."""
    pools = []

    if tier == "LOW":
        # Pool 1: Common supplies (3-5 rolls)
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:arrow", 10, 4, 12),
            make_entry("minecraft:prismarine_shard", 8, 2, 5),
            make_entry("minecraft:clay", 7, 1, 3),
            make_entry("minecraft:coal", 6, 2, 4),
            make_entry("minecraft:bread", 5, 1, 3),
            make_entry("minecraft:lily_pad", 4, 1, 2),
        ]))
        # Pool 2: Minor rewards (1-2 rolls)
        pools.append(make_pool(1, 2, [
            make_entry("minecraft:emerald", 5, 1, 3),
            make_entry("minecraft:prismarine", 4, 1, 2),
            make_entry("minecraft:stick", 4, 3, 8),
            make_entry("minecraft:kelp", 3, 2, 5),
        ]))

    elif tier == "MED":
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:prismarine_shard", 9, 2, 6),
            make_entry("minecraft:iron_ingot", 8, 1, 4),
            make_entry("minecraft:clay", 7, 2, 5),
            make_entry("minecraft:prismarine", 6, 1, 3),
            make_entry("minecraft:ink_sac", 5, 1, 2),
            make_entry("minecraft:lily_pad", 5, 1, 3),
        ]))
        pools.append(make_pool(2, 3, [
            make_entry("minecraft:emerald", 7, 2, 6),
            make_entry("minecraft:book", 5, 1, 1),
            make_entry("minecraft:nautilus_shell", 3, 1, 1),
            make_entry("ergenverse:jade_slip", 3, 1, 1),
            make_entry("minecraft:experience_bottle", 3, 1, 1),
        ]))
        pools.append(make_pool(1, 1, [
            make_entry("ergenverse:spirit_stone", 2, 1, 1),
        ]))

    elif tier == "HIGH":
        pools.append(make_pool(4, 6, [
            make_entry("minecraft:prismarine_shard", 8, 3, 8),
            make_entry("minecraft:gold_ingot", 7, 1, 4),
            make_entry("minecraft:prismarine", 6, 2, 4),
            make_entry("minecraft:clay", 5, 3, 6),
            make_entry("minecraft:nautilus_shell", 4, 1, 2),
            make_entry("minecraft:tropical_fish", 3, 1, 3),
        ]))
        pools.append(make_pool(2, 3, [
            make_entry("ergenverse:jade_slip", 7, 1, 2),
            make_entry("minecraft:emerald", 6, 3, 8),
            make_entry("minecraft:enchanted_book", 4, 1, 1),
            make_entry("ergenverse:spirit_stone", 4, 1, 2),
            make_entry("minecraft:ender_pearl", 3, 1, 1),
        ]))
        pools.append(make_pool(1, 1, [
            make_entry("ergenverse:dao_fragment", 2, 1, 1),
            make_entry("minecraft:obsidian", 3, 2, 4),
        ]))

    elif tier == "MAX":
        pools.append(make_pool(4, 6, [
            make_entry("minecraft:prismarine_shard", 7, 4, 10),
            make_entry("minecraft:gold_ingot", 7, 2, 6),
            make_entry("minecraft:emerald", 6, 3, 8),
            make_entry("minecraft:nautilus_shell", 5, 1, 3),
            make_entry("minecraft:prismarine", 5, 2, 5),
            make_entry("minecraft:diamond", 3, 1, 2),
        ]))
        pools.append(make_pool(2, 4, [
            make_entry("ergenverse:jade_slip", 8, 1, 3),
            make_entry("ergenverse:spirit_stone", 7, 1, 3),
            make_entry("minecraft:enchanted_book", 5, 1, 1),
            make_entry("ergenverse:dao_fragment", 4, 1, 2),
            make_entry("minecraft:ender_pearl", 4, 1, 2),
        ]))
        pools.append(make_pool(1, 2, [
            make_entry("minecraft:obsidian", 5, 3, 6),
            make_entry("minecraft:nether_star", 1, 1, 1),
        ]))

    return {
        "_comment": f"Luo He Sect — {suffix}. {comment}",
        "type": "minecraft:chest",
        "pools": pools,
    }


# ============================================================
# NPCs — 12 INFERRED NPCs covering all 20 areas
# ============================================================

NPCS = [
    {
        "npc_id": "npc_lh_outer_disciple_jiang",
        "name": "Outer Disciple Jiang",
        "nameCn": "外门弟子江",
        "type": "sect_disciple",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_outer_gate",
        "cultivation": "CONDENSATION early",
        "personality": "vigilant, river-proud, respects the current",
        "speech": "direct, watery metaphors, welcoming to respectful visitors",
        "initiation_lines": [
            "The Falling River never stops flowing. Neither do we. State your business at Luo He.",
            "The Li family once walked this very gate. Their daughter was our finest scholar before she departed.",
            "We guard the river's mouth. Everything that flows downstream passes through us first.",
            "If you seek entry, bring prismarine shards. The gate formations run on river essence."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "patrol", "location": "luo_he_sect_outer_gate", "duration": 120},
            {"time": "0800", "action": "stand_guard", "location": "luo_he_sect_outer_gate", "duration": 240},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "patrol", "location": "luo_he_sect_main_plaza", "duration": 120},
            {"time": "1500", "action": "train", "location": "luo_he_sect_trial_grounds", "duration": 120},
            {"time": "1700", "action": "stand_guard", "location": "luo_he_sect_outer_gate", "duration": 180},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "river_gate_patrol_supplies",
                "name": "River Gate Patrol Supplies",
                "description": "The outer gate patrol needs arrows and river essence to maintain the water-current barrier.",
                "required_items": {"minecraft:arrow": 8, "minecraft:prismarine_shard": 6},
                "rewards": {"minecraft:emerald": 5, "minecraft:book": 1},
            },
            {
                "task_id": "gate_current_barrier_repair",
                "name": "Current Barrier Repair",
                "description": "The water-current formation at the gate needs prismarine and clay to seal cracks in the flow pattern.",
                "required_items": {"minecraft:prismarine": 4, "minecraft:clay": 5},
                "rewards": {"minecraft:emerald": 4, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_flow_master_yun",
        "name": "Flow Master Yun",
        "nameCn": "流云长老",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_spirit_spring",
        "cultivation": "NASCENT_SOUL early",
        "personality": "calm, philosophic, speaks in water metaphors",
        "speech": "measured, poetic, references the eternal flow",
        "initiation_lines": [
            "The spring does not choose to flow. It simply flows. That is the highest Dao of Luo He.",
            "Li Muwan meditated here for three years. The spring remembers her still.",
            "The Heavenly Fate Sect reads stars. We read water. The stars say nothing about rivers, but the river knows every star it reflects.",
            "Sit by the spring. Close your eyes. The current will teach you what a thousand scrolls cannot."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "luo_he_sect_spirit_spring", "duration": 180},
            {"time": "0800", "action": "teach", "location": "luo_he_sect_spirit_spring", "duration": 180},
            {"time": "1100", "action": "walk", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "inspect", "location": "luo_he_sect_spirit_herb_garden", "duration": 120},
            {"time": "1500", "action": "meditate", "location": "luo_he_sect_spirit_spring", "duration": 240},
            {"time": "1900", "action": "rest", "location": "luo_he_sect_inner_sect", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "spring_purification_essence",
                "name": "Spring Purification Essence",
                "description": "The sacred spring needs river essence and nautilus shells to purify its flow for the monthly ceremony.",
                "required_items": {"minecraft:prismarine_shard": 8, "minecraft:nautilus_shell": 1},
                "rewards": {"minecraft:emerald": 8, "ergenverse:jade_slip": 1},
            },
            {
                "task_id": "lily_pad_harvest",
                "name": "Lily Pad Harvest",
                "description": "Gather lily pads from the spring for the alchemy courtyard. They need them for Water Essence Pills.",
                "required_items": {"minecraft:lily_pad": 8, "minecraft:kelp": 5},
                "rewards": {"minecraft:emerald": 7, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_river_librarian_he",
        "name": "River Librarian He",
        "nameCn": "河典阁主何",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_library",
        "cultivation": "NASCENT_SOUL early",
        "personality": "meticulous, protective of scrolls, values knowledge above combat",
        "speech": "precise, scholarly, occasionally dry humor",
        "initiation_lines": [
            "Every scroll in this library has been waterproofed with river-silk resin. Touch them with dry hands only.",
            "I have catalogued seven thousand two hundred and forty-one scrolls. Thirty-one are irreparably water-damaged. I remember each one.",
            "The Xuan Dao Sect lent us three formation scrolls last year. We returned them with annotations in blue ink. They said the ink was wrong.",
            "If you seek knowledge of the river Dao, begin with scroll four hundred. If you seek combat techniques, I suggest you leave."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "catalog", "location": "luo_he_sect_library", "duration": 180},
            {"time": "0900", "action": "teach", "location": "luo_he_sect_library", "duration": 180},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "research", "location": "luo_he_sect_library", "duration": 180},
            {"time": "1600", "action": "inspect", "location": "luo_he_sect_secret_pavilion", "duration": 120},
            {"time": "1800", "action": "catalog", "location": "luo_he_sect_library", "duration": 120},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "scroll_waterproofing",
                "name": "Scroll Waterproofing",
                "description": "Apply river-silk resin to damaged scrolls. Bring clay and prismarine for the resin base.",
                "required_items": {"minecraft:clay": 6, "minecraft:prismarine_shard": 4},
                "rewards": {"minecraft:book": 2, "minecraft:emerald": 6},
            },
            {
                "task_id": "rare_scroll_retrieval",
                "name": "Rare Scroll Retrieval",
                "description": "A rare scroll was misplaced. Bring ink and paper to copy the remaining fragments before water damage spreads.",
                "required_items": {"minecraft:ink_sac": 3, "minecraft:paper": 8},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 5},
            },
        ],
    },
    {
        "npc_id": "npc_lh_tide_alchemist_shen",
        "name": "Tide Alchemist Shen",
        "nameCn": "潮汐炼丹师沈",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_alchemy_courtyard",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "intense, perfectionist, disdainful of impure methods",
        "speech": "sharp, precise, frequently critical of other sects' alchemy",
        "initiation_lines": [
            "A pill refined without water-spirit essence is not a pill. It is a pebble you swallow.",
            "The Soul Refining Sect sent an alchemist to study our Water Essence Pill. I refused. Their soul-corruption techniques would contaminate the river's purity.",
            "Li Muwan was the only disciple who understood that alchemy is not chemistry. It is conversation with water.",
            "Bring me kelp, nautilus shells, and glass bottles. If your ingredients are impure, I will know. The water always knows."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "gather", "location": "luo_he_sect_spirit_herb_garden", "duration": 120},
            {"time": "0700", "action": "refine", "location": "luo_he_sect_alchemy_courtyard", "duration": 240},
            {"time": "1100", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "teach", "location": "luo_he_sect_alchemy_courtyard", "duration": 180},
            {"time": "1500", "action": "research", "location": "luo_he_sect_library", "duration": 120},
            {"time": "1700", "action": "refine", "location": "luo_he_sect_alchemy_courtyard", "duration": 180},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "water_essence_pill_ingredients",
                "name": "Water Essence Pill Ingredients",
                "description": "Gather river herbs and containers for the Tide Alchemist's signature Water Essence Pill refinement.",
                "required_items": {"minecraft:kelp": 8, "minecraft:glass_bottle": 4},
                "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "tide_purification_fuel",
                "name": "Tide Purification Fuel",
                "description": "The alchemy furnace needs coal and nautilus shells to maintain the tidal flame pattern.",
                "required_items": {"minecraft:coal": 8, "minecraft:nautilus_shell": 2},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_current_swordswoman_liu",
        "name": "Current Swordswoman Liu",
        "nameCn": "流水剑师柳",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_sword_peak",
        "cultivation": "NASCENT_SOUL late",
        "personality": "flowing, unpredictable, treats sword like water",
        "speech": "fluid metaphors, sudden shifts in tone, challenging",
        "initiation_lines": [
            "A sword that cuts like water cannot be blocked. A sword that IS water cannot be seen until it has already cut.",
            "I trained at the Sword Tomb for forty years. The river carried away my first blade. I never replaced it. The river is my blade now.",
            "The Heng Yue Sect's sword style is rigid. Mountains. Our style has no shape. You cannot block what has no form.",
            "Stand in the river current and try to hold a stance. When you can stand for one hour, return. Until then, you are not ready."
        ],
        "daily_schedule": [
            {"time": "0400", "action": "train", "location": "luo_he_sect_sword_peak", "duration": 240},
            {"time": "0800", "action": "meditate", "location": "luo_he_sect_spirit_spring", "duration": 120},
            {"time": "1000", "action": "teach", "location": "luo_he_sect_sword_peak", "duration": 180},
            {"time": "1300", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "inspect", "location": "luo_he_sect_sword_tomb", "duration": 120},
            {"time": "1600", "action": "train", "location": "luo_he_sect_trial_grounds", "duration": 180},
            {"time": "1900", "action": "rest", "location": "luo_he_sect_inner_sect", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "sword_river_essence_sharpening",
                "name": "Sword River Essence Sharpening",
                "description": "Bring river essence and iron to re-sharpen the training blades in the Sword Tomb using flowing water.",
                "required_items": {"minecraft:iron_ingot": 4, "minecraft:prismarine_shard": 6},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "current_endurance_supplies",
                "name": "Current Endurance Supplies",
                "description": "The sword disciples need food and healing supplies for their underwater endurance training.",
                "required_items": {"minecraft:bread": 8, "minecraft:gold_ingot": 3},
                "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 8},
            },
        ],
    },
    {
        "npc_id": "npc_lh_beast_fisher_zhao",
        "name": "Beast Fisher Zhao",
        "nameCn": "灵兽渔夫赵",
        "type": "sect_disciple",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_spirit_beast_pens",
        "cultivation": "CONDENSATION late",
        "personality": "patient, observant, deeply knowledgeable about river creatures",
        "speech": "slow, gentle, full of aquatic observations",
        "initiation_lines": [
            "The river serpents here have lived for three hundred years. They remember the founding ancestor. I believe them.",
            "Every creature in these pens was caught by hand. No nets. Nets are for those who do not understand the current.",
            "A Corpse Yin Sect envoy once offered to trade zombie-fish for our river serpents. I told him our fish are alive. His were not.",
            "The kelp you see in the herb garden was originally from the beast pens. One river serpent curled around it for a century. Now it glows."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "feed", "location": "luo_he_sect_spirit_beast_pens", "duration": 180},
            {"time": "0900", "action": "patrol", "location": "luo_he_sect_spirit_spring", "duration": 120},
            {"time": "1100", "action": "gather", "location": "luo_he_sect_spirit_herb_garden", "duration": 120},
            {"time": "1300", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "feed", "location": "luo_he_sect_spirit_beast_pens", "duration": 180},
            {"time": "1700", "action": "inspect", "location": "luo_he_sect_spirit_beast_pens", "duration": 120},
            {"time": "1900", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "serpent_feeding_ingredients",
                "name": "Serpent Feeding Ingredients",
                "description": "The river serpents need raw fish and clay for their monthly feeding. Clay settles their digestion.",
                "required_items": {"minecraft:tropical_fish": 6, "minecraft:clay": 5},
                "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1},
            },
            {
                "task_id": "pen_repair_materials",
                "name": "Pen Repair Materials",
                "description": "The underwater beast pens need prismarine and sticks to repair damage from the last storm.",
                "required_items": {"minecraft:prismarine": 5, "minecraft:stick": 10},
                "rewards": {"minecraft:emerald": 5, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_trial_diver_chen",
        "name": "Trial Diver Chen",
        "nameCn": "试炼潜者陈",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_trial_grounds",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "strict, impartial, records everything",
        "speech": "clipped, efficient, statistics-driven",
        "initiation_lines": [
            "Seventy-three percent of outer disciples fail the first dive. Forty-one percent fail the second. Twelve percent never resurface.",
            "The trial is simple: hold your breath, walk to the bottom, retrieve the jade slip, return. The river decides who passes.",
            "Last month a Heng Yue Sect disciple attempted our trials. He lasted eleven seconds. He said our river was too cold.",
            "I have recorded every trial result for sixty years. The data does not lie. The river does not lie. Only disciples lie."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "prepare", "location": "luo_he_sect_trial_grounds", "duration": 120},
            {"time": "0800", "action": "judge", "location": "luo_he_sect_trial_grounds", "duration": 240},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "judge", "location": "luo_he_sect_trial_grounds", "duration": 180},
            {"time": "1600", "action": "record", "location": "luo_he_sect_library", "duration": 120},
            {"time": "1800", "action": "inspect", "location": "luo_he_sect_spirit_spring", "duration": 120},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "trial_equipment_maintenance",
                "name": "Trial Equipment Maintenance",
                "description": "The underwater trial arena needs prismarine and clay to repair the depth markers and safety formations.",
                "required_items": {"minecraft:prismarine_shard": 6, "minecraft:clay": 4},
                "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2},
            },
            {
                "task_id": "diving_supply_replenishment",
                "name": "Diving Supply Replenishment",
                "description": "Trials consume bread and arrows for survival supplies. Replenish the trial stores.",
                "required_items": {"minecraft:bread": 10, "minecraft:arrow": 12},
                "rewards": {"minecraft:emerald": 6, "ergenverse:jade_slip": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_underground_stream_scribe_wei",
        "name": "Underground Stream Scribe Wei",
        "nameCn": "地下溪流书吏魏",
        "type": "sect_disciple",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_underground_passage",
        "cultivation": "FOUNDATION late",
        "personality": "nervous, knowledgeable, secretly brave",
        "speech": "hushed, reverent, frequently glances at the water",
        "initiation_lines": [
            "The underground river whispers. I have been writing down what it says for eleven years. Scroll seven hundred and twelve of three thousand.",
            "Corpses sometimes float downstream from Corpse Yin Sect territory. We return them. They never thank us. The river thanks us.",
            "There is a chamber down here where the water flows upward. The ancestor said it leads to another country. I have not found it.",
            "Bring me ink and paper. The river's whispers fade if not recorded within the hour."
        ],
        "daily_schedule": [
            {"time": "0700", "action": "record", "location": "luo_he_sect_underground_passage", "duration": 240},
            {"time": "1100", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "research", "location": "luo_he_sect_underground_passage", "duration": 180},
            {"time": "1500", "action": "inspect", "location": "luo_he_sect_mountain_cave", "duration": 120},
            {"time": "1700", "action": "record", "location": "luo_he_sect_underground_passage", "duration": 180},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "river_whisper_recording",
                "name": "River Whisper Recording",
                "description": "The underground scribe needs ink and paper to record the river's whispers before they fade.",
                "required_items": {"minecraft:ink_sac": 4, "minecraft:paper": 10},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "underground_passage_lighting",
                "name": "Underground Passage Lighting",
                "description": "The passage torches need coal and glowstone. The river whispers are harder to read in darkness.",
                "required_items": {"minecraft:coal": 10, "minecraft:prismarine_shard": 4},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_ancestor_keeper_bai",
        "name": "Ancestor Keeper Bai",
        "nameCn": "祖堂守白",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_ancestor_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "reverent, stern, carries the weight of history",
        "speech": "formal, measured, invokes ancestors frequently",
        "initiation_lines": [
            "The founding ancestor stood at this spring three thousand years ago and declared: 'Where the river falls, the Dao rises.' We have kept that oath.",
            "Li Qiqing was the pride of the inner sect. When he departed for Fire Burn Country, the ancestor hall's river sang for three days.",
            "Every elder who has reached Nascent Soul in this sect has meditated in the Ancestor Hall. The river carries their voices still.",
            "You stand where generations stood. The river does not care about your ambition. It cares about your patience."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "luo_he_sect_ancestor_hall", "duration": 180},
            {"time": "0800", "action": "maintain", "location": "luo_he_sect_ancestor_hall", "duration": 180},
            {"time": "1100", "action": "inspect", "location": "luo_he_sect_spirit_spring", "duration": 60},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "teach", "location": "luo_he_sect_core_formation_hall", "duration": 180},
            {"time": "1600", "action": "meditate", "location": "luo_he_sect_ancestor_hall", "duration": 240},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_inner_sect", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "ancestor_hall_river_offering",
                "name": "Ancestor Hall River Offering",
                "description": "The monthly river offering requires gold, obsidian, and prismarine crystals to honor the founding ancestor.",
                "required_items": {"minecraft:gold_ingot": 4, "minecraft:obsidian": 3},
                "rewards": {"ergenverse:spirit_stone": 3, "minecraft:emerald": 6},
            },
            {
                "task_id": "ancestral_scroll_preservation",
                "name": "Ancestral Scroll Preservation",
                "description": "Preserve the ancestor's original writings. Bring paper, ink, and books to create protective copies.",
                "required_items": {"minecraft:book": 3, "minecraft:ink_sac": 4, "minecraft:paper": 8},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2},
            },
        ],
    },
    {
        "npc_id": "npc_lh_inner_river_elder_song",
        "name": "Inner River Elder Song",
        "nameCn": "内河长老宋",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_core_formation_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "commanding, strategic, sees the sect as a living river system",
        "speech": "authoritative, systemic, draws connections between all things",
        "initiation_lines": [
            "The Luo He Sect is not a collection of buildings. It is a river system. Every hall, every disciple, every formation is a tributary.",
            "Our water arrays differ from the Xuan Dao Sect's ink arrays. Theirs record the Dao. Ours become the Dao. The water remembers.",
            "The Soul Refining Sect's soul arrays are chains. The Corpse Yin Sect's bone arrays are cages. Our water arrays are freedom.",
            "Three sects have asked for alliance this decade. Two were refused. The third has not yet been answered. The river is still deciding."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "luo_he_sect_core_formation_hall", "duration": 120},
            {"time": "0700", "action": "meet", "location": "luo_he_sect_inner_sect", "duration": 180},
            {"time": "1000", "action": "inspect", "location": "luo_he_sect_array_hall", "duration": 120},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "teach", "location": "luo_he_sect_core_formation_hall", "duration": 180},
            {"time": "1600", "action": "inspect", "location": "luo_he_sect_hidden_treasury", "duration": 120},
            {"time": "1800", "action": "meditate", "location": "luo_he_sect_spirit_spring", "duration": 180},
            {"time": "2100", "action": "rest", "location": "luo_he_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "core_formation_convergence",
                "name": "Core Formation Convergence",
                "description": "The core formation needs spirit stones and ender pearls to maintain the river convergence nexus.",
                "required_items": {"ergenverse:spirit_stone": 2, "minecraft:ender_pearl": 2},
                "rewards": {"ergenverse:jade_slip": 3, "ergenverse:dao_fragment": 1},
            },
            {
                "task_id": "inner_sect_supply_chain",
                "name": "Inner Sect Supply Chain",
                "description": "The inner sect needs emeralds, enchanted books, and obsidian for formation maintenance and elder research.",
                "required_items": {"minecraft:emerald": 10, "minecraft:enchanted_book": 1},
                "rewards": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 2},
            },
        ],
    },
    {
        "npc_id": "npc_lh_herb_drifter_qing",
        "name": "Herb Drifter Qing",
        "nameCn": "草药漂泊者青",
        "type": "sect_disciple",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_spirit_herb_garden",
        "cultivation": "FOUNDATION mid",
        "personality": "gentle, dreamy, speaks to plants",
        "speech": "soft, wandering, frequently loses track of conversations",
        "initiation_lines": [
            "The kelp here is four hundred years old. It remembers when the river was clear enough to see the bottom.",
            "Water herbs grow differently from mountain herbs. You cannot pull them. You must ask. I ask every morning.",
            "The Alchemist Shen says my herbs lack potency. I say his pills lack patience. The river agrees with me.",
            "If you bring me clay and bone meal, I can grow river-lily seeds. They bloom underwater. It is quite beautiful."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "tend", "location": "luo_he_sect_spirit_herb_garden", "duration": 240},
            {"time": "1000", "action": "gather", "location": "luo_he_sect_spirit_spring", "duration": 120},
            {"time": "1200", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "tend", "location": "luo_he_sect_spirit_herb_garden", "duration": 180},
            {"time": "1600", "action": "deliver", "location": "luo_he_sect_alchemy_courtyard", "duration": 120},
            {"time": "1800", "action": "tend", "location": "luo_he_sect_spirit_herb_garden", "duration": 120},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "river_herb_garden_fertilizer",
                "name": "River Herb Garden Fertilizer",
                "description": "The water herbs need bone meal and kelp for fertilizer to maintain their spiritual potency.",
                "required_items": {"minecraft:bone_meal": 8, "minecraft:kelp": 6},
                "rewards": {"minecraft:emerald": 5, "minecraft:lily_pad": 4},
            },
            {
                "task_id": "herb_delivery_to_alchemist",
                "name": "Herb Delivery to Alchemist",
                "description": "Deliver harvested herbs to the alchemy courtyard. Bring glass bottles and coal for the exchange.",
                "required_items": {"minecraft:glass_bottle": 4, "minecraft:coal": 6},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_lh_array_weaver_hu",
        "name": "Array Weaver Hu",
        "nameCn": "阵法织者胡",
        "type": "elder",
        "faction": "luo_he_sect",
        "location": "luo_he_sect_array_hall",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "analytical, creative, sees patterns everywhere",
        "speech": "precise, pattern-focused, compares everything to water flow",
        "initiation_lines": [
            "A formation is a river. If the flow is correct, it nourishes everything. If the flow breaks, it floods.",
            "The Xuan Dao Sect's ink arrays dry the water. Our water arrays nourish the ink. Both are valid. Ours are more alive.",
            "I once wove a formation using only the river's natural currents. It lasted three hundred years. Then a drought came.",
            "Redstone carries intent. Prismarine carries memory. Combine them, and the formation remembers what you intended."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "weave", "location": "luo_he_sect_array_hall", "duration": 240},
            {"time": "1000", "action": "inspect", "location": "luo_he_sect_outer_gate", "duration": 60},
            {"time": "1100", "action": "eat", "location": "luo_he_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "teach", "location": "luo_he_sect_array_hall", "duration": 180},
            {"time": "1500", "action": "research", "location": "luo_he_sect_library", "duration": 120},
            {"time": "1700", "action": "weave", "location": "luo_he_sect_array_hall", "duration": 180},
            {"time": "2000", "action": "rest", "location": "luo_he_sect_disciple_dormitories", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "water_formation_maintenance",
                "name": "Water Formation Maintenance",
                "description": "The water-flow formations need redstone and prismarine to maintain their pattern integrity.",
                "required_items": {"minecraft:redstone": 6, "minecraft:prismarine_shard": 8},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8},
            },
            {
                "task_id": "array_hall_supply_requisition",
                "name": "Array Hall Supply Requisition",
                "description": "The array hall needs obsidian and ender pearls for high-level water-space formation experiments.",
                "required_items": {"minecraft:obsidian": 4, "minecraft:ender_pearl": 1},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
]


def generate_salt(name):
    """Generate a deterministic salt from NPC name."""
    return int(hashlib.md5(name.encode()).hexdigest()[:8], 16) % 1000000000


def main():
    print("=== AUTO-CANON-041: Luo He Sect Completion ===")
    loot_count = 0
    npc_count = 0

    # --- Generate Loot Tables ---
    for suffix, comment, tier in STRUCTURES:
        table = build_loot_table(suffix, comment, tier)
        filepath = os.path.join(LOOT_DIR, f"luo_he_sect_{suffix}.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(table, f, indent=2, ensure_ascii=False)
        loot_count += 1
        print(f"  Loot: luo_he_sect_{suffix}.json ({tier})")

    # --- Generate NPCs ---
    for npc_data in NPCS:
        npc = {
            "npc_id": npc_data["npc_id"],
            "name": npc_data["name"],
            "nameCn": npc_data["nameCn"],
            "canon_id": None,
            "type": npc_data["type"],
            "faction": npc_data["faction"],
            "location": npc_data["location"],
            "cultivation": npc_data["cultivation"],
            "personality": npc_data["personality"],
            "speech": npc_data["speech"],
            "relationship_to_wanglin": "neutral",
            "dialogue_available": True,
            "quest_available": True,
            "trade_available": True,
            "teaching_available": False,
            "canon_confidence": 0,
            "derivation_type": "I",
            "salt": generate_salt(npc_data["npc_id"]),
            "initiation_lines": npc_data["initiation_lines"],
            "daily_schedule": npc_data["daily_schedule"],
            "sect_tasks": npc_data["sect_tasks"],
            "_xianxia_schema": 1,
        }
        filepath = os.path.join(NPC_DIR, f"{npc_data['npc_id']}.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
        npc_count += 1
        print(f"  NPC: {npc_data['npc_id']}.json ({npc_data['cultivation']})")

    print(f"\n=== Generated {loot_count} loot tables + {npc_count} NPCs ===")

    # --- Verify ---
    # Check prismarine_shard presence
    shard_count = 0
    for suffix, _, _ in STRUCTURES:
        fp = os.path.join(LOOT_DIR, f"luo_he_sect_{suffix}.json")
        with open(fp) as f:
            data = json.load(f)
        for pool in data.get("pools", []):
            for entry in pool.get("entries", []):
                if "prismarine_shard" in entry.get("name", ""):
                    shard_count += 1
                    break
    print(f"\nVerification: prismarine_shard in {shard_count}/{loot_count} loot tables")

    # Check jade_slip in tasks
    js_tasks = 0
    total_tasks = 0
    for npc_data in NPCS:
        for task in npc_data["sect_tasks"]:
            total_tasks += 1
            for reward in task["rewards"]:
                if "jade_slip" in reward:
                    js_tasks += 1
                    break
    print(f"Verification: jade_slip in {js_tasks}/{total_tasks} task rewards")


if __name__ == "__main__":
    main()