#!/usr/bin/env python3
"""
AUTO-CANON-039: Heavenly Fate Sect (天星宗) completion.
21 component loot tables + 12 INFERRED NPCs with schedules + tasks.

Heavenly Fate Sect theme: Fate manipulation, star observation, divination, destiny reading.
The sect reads the stars to predict and manipulate fate. Liu Mei (canon) originates here.
Loot features compass, clock, map (navigation/fate tools), glowstone_dust (starlight),
ender_pearl (spatial manipulation), redstone (fate threads). Mod items scale by tier.

Existing orphan: heavenly_fate_star_tower.json (spirit_stone, dao_fragment, ji_realm,
starry_sky_token, heaven_fan, karma_whip, nether_star). Keep as-is.

Wealth gradient: LOW (outer_gate, main_plaza, dormitories, herb_garden, spring, trials, outpost)
-> MED (library, alchemy, sword_peak, beast_pens, array_hall, puppet, cave)
-> HIGH (underground, secret_pavilion, inner_sect, hidden_treasury, sword_tomb)
-> MAX (ancestor_hall, core_formation_hall).
"""

import json
import os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables/chests")
NPC_DIR = os.path.join(BASE, "npcs")

COMPONENTS = [
    "heavenly_fate_sect_outer_gate",
    "heavenly_fate_sect_main_plaza",
    "heavenly_fate_sect_disciple_dormitories",
    "heavenly_fate_sect_spirit_herb_garden",
    "heavenly_fate_sect_spirit_spring",
    "heavenly_fate_sect_trial_grounds",
    "heavenly_fate_outpost",
    "heavenly_fate_sect_library",
    "heavenly_fate_sect_alchemy_courtyard",
    "heavenly_fate_sect_sword_peak",
    "heavenly_fate_sect_spirit_beast_pens",
    "heavenly_fate_sect_array_hall",
    "heavenly_fate_sect_puppet_workshop",
    "heavenly_fate_sect_mountain_cave",
    "heavenly_fate_sect_underground_passage",
    "heavenly_fate_sect_secret_pavilion",
    "heavenly_fate_sect_inner_sect",
    "heavenly_fate_sect_hidden_treasury",
    "heavenly_fate_sect_sword_tomb",
    "heavenly_fate_sect_ancestor_hall",
    "heavenly_fate_sect_core_formation_hall",
]

def item(weight, name, cmin=1, cmax=1):
    funcs = []
    if cmin != cmax or cmin != 1:
        funcs.append({"function": "minecraft:set_count", "count": {"min": cmin, "max": cmax}})
    return {"type": "minecraft:item", "weight": weight, "name": name, "functions": funcs}

def empty(weight):
    return {"type": "minecraft:empty", "weight": weight}

def make_loot(comment, pools):
    return {"_comment": comment, "type": "minecraft:chest", "pools": [
        {"rolls": {"min": p[0], "max": p[1]}, "entries": p[2]} for p in pools
    ]}

# Heavenly Fate signature items: compass, clock, glowstone_dust (starlight), map
LOOT_TABLES = {
    # ── TIER 1: LOW ──
    "heavenly_fate_sect_outer_gate": make_loot(
        "Heavenly Fate Sect \u2014 outer_gate. Guard supplies. Fate-inscribed arrows.",
        [(2, 4, [item(10, "minecraft:arrow", 4, 12), item(8, "minecraft:glowstone_dust", 2, 5),
                 item(6, "minecraft:coal", 2, 5), item(5, "minecraft:bread", 1, 3),
                 item(4, "minecraft:redstone", 2, 4), item(3, "minecraft:iron_ingot", 1, 2)]),
         (1, 2, [item(5, "minecraft:paper", 1, 3), item(3, "minecraft:compass"),
                 item(2, "minecraft:stick", 4, 8)])]),
    "heavenly_fate_sect_main_plaza": make_loot(
        "Heavenly Fate Sect \u2014 main_plaza. Open star observation area.",
        [(1, 2, [item(8, "minecraft:glowstone_dust", 2, 5), item(6, "minecraft:bread", 1, 3),
                 item(5, "minecraft:coal", 2, 4), item(4, "minecraft:redstone", 1, 3),
                 item(3, "minecraft:compass")]),
         (1, 1, [item(3, "minecraft:clock"), item(2, "minecraft:paper", 2, 4)])]),
    "heavenly_fate_sect_disciple_dormitories": make_loot(
        "Heavenly Fate Sect \u2014 disciple_dormitories. Basic disciple supplies. Star charts.",
        [(1, 3, [item(8, "minecraft:glowstone_dust", 2, 6), item(6, "minecraft:bread", 1, 4),
                 item(5, "minecraft:coal", 2, 5), item(4, "minecraft:redstone", 1, 3),
                 item(3, "minecraft:iron_ingot", 1, 2), item(2, "minecraft:paper", 2, 4)]),
         ]),
    "heavenly_fate_sect_spirit_herb_garden": make_loot(
        "Heavenly Fate Sect \u2014 spirit_herb_garden. Star-nourished herbs. INFERRED.",
        [(2, 4, [item(8, "minecraft:glowstone_dust", 2, 5), item(6, "minecraft:wheat", 2, 6),
                 item(5, "minecraft:bone_meal", 2, 5), item(4, "minecraft:redstone", 1, 3),
                 item(3, "minecraft:glass_bottle", 1, 3), item(2, "minecraft:coal", 2, 4)]),
         ]),
    "heavenly_fate_sect_spirit_spring": make_loot(
        "Heavenly Fate Sect \u2014 spirit_spring. Starlight-infused spring. INFERRED.",
        [(1, 3, [item(8, "minecraft:glowstone_dust", 2, 6), item(6, "minecraft:glass_bottle", 1, 3),
                 item(5, "minecraft:ender_pearl", 1, 1), item(4, "minecraft:redstone", 1, 3),
                 item(2, "minecraft:compass")]),
         ]),
    "heavenly_fate_sect_trial_grounds": make_loot(
        "Heavenly Fate Sect \u2014 trial_grounds. Fate-reading trials. INFERRED.",
        [(2, 4, [item(8, "minecraft:glowstone_dust", 3, 8), item(6, "minecraft:arrow", 4, 10),
                 item(5, "minecraft:redstone", 2, 5), item(4, "minecraft:leather", 1, 3),
                 item(3, "minecraft:iron_ingot", 1, 2)]),
         (1, 2, [item(4, "minecraft:emerald", 1, 3), item(3, "minecraft:compass"),
                 item(2, "minecraft:coal", 2, 4)])]),
    "heavenly_fate_outpost": make_loot(
        "Heavenly Fate Sect \u2014 outpost. Remote observation post. Star monitoring.",
        [(2, 4, [item(8, "minecraft:glowstone_dust", 3, 7), item(6, "minecraft:arrow", 4, 8),
                 item(5, "minecraft:redstone", 2, 5), item(4, "minecraft:coal", 2, 4),
                 item(3, "minecraft:bread", 1, 3), item(3, "minecraft:iron_ingot", 1, 2)]),
         (1, 2, [item(4, "minecraft:compass"), item(3, "minecraft:clock"),
                 item(2, "minecraft:map"), item(2, "minecraft:emerald", 1, 2)])]),

    # ── TIER 2: MED ──
    "heavenly_fate_sect_library": make_loot(
        "Heavenly Fate Sect \u2014 library. Fate divination texts and star charts.",
        [(2, 4, [item(7, "minecraft:glowstone_dust", 2, 5), item(7, "minecraft:paper", 2, 5),
                 item(6, "minecraft:ink_sac", 2, 4), item(5, "minecraft:book", 1, 3),
                 item(4, "minecraft:compass"), item(3, "minecraft:redstone", 2, 4)]),
         (1, 2, [item(4, "minecraft:emerald", 1, 3), item(3, "minecraft:enchanted_book"),
                 item(2, "minecraft:clock"), item(2, "minecraft:map")])]),
    "heavenly_fate_sect_alchemy_courtyard": make_loot(
        "Heavenly Fate Sect \u2014 alchemy_courtyard. Fate pills and star-essence elixirs.",
        [(2, 4, [item(7, "minecraft:glowstone_dust", 3, 6), item(6, "minecraft:glass_bottle", 2, 5),
                 item(5, "minecraft:coal", 2, 5), item(4, "minecraft:redstone", 2, 4),
                 item(3, "minecraft:blaze_powder", 1, 2), item(2, "minecraft:ender_pearl", 1, 1)]),
         (1, 2, [item(4, "minecraft:emerald", 1, 3), item(2, "minecraft:ghast_tear"),
                 item(1, "ergenverse:spirit_stone", 1, 1)])]),
    "heavenly_fate_sect_sword_peak": make_loot(
        "Heavenly Fate Sect \u2014 sword_peak. Fate-attributed sword cultivation. INFERRED.",
        [(2, 4, [item(7, "minecraft:glowstone_dust", 2, 5), item(6, "minecraft:iron_ingot", 2, 5),
                 item(5, "minecraft:redstone", 2, 4), item(4, "minecraft:coal", 2, 4),
                 item(3, "minecraft:ender_pearl", 1, 1), item(2, "minecraft:iron_sword")]),
         (1, 2, [item(4, "minecraft:emerald", 2, 4), item(2, "minecraft:enchanted_book"),
                 item(1, "minecraft:book")])]),
    "heavenly_fate_sect_spirit_beast_pens": make_loot(
        "Heavenly Fate Sect \u2014 spirit_beast_pens. Fate-reading beasts. INFERRED.",
        [(2, 4, [item(7, "minecraft:glowstone_dust", 3, 7), item(6, "minecraft:leather", 2, 5),
                 item(5, "minecraft:redstone", 2, 4), item(4, "minecraft:iron_ingot", 1, 3),
                 item(3, "minecraft:lead", 1, 3)]),
         (1, 2, [item(4, "minecraft:emerald", 1, 3), item(2, "minecraft:compass"),
                 item(1, "minecraft:string", 2, 6)])]),
    "heavenly_fate_sect_array_hall": make_loot(
        "Heavenly Fate Sect \u2014 array_hall. Fate-thread restriction arrays. INFERRED.",
        [(2, 4, [item(7, "minecraft:redstone", 3, 7), item(6, "minecraft:ender_pearl", 1, 2),
                 item(5, "minecraft:glowstone_dust", 2, 5), item(4, "minecraft:obsidian", 1, 3),
                 item(3, "minecraft:compass")]),
         (1, 2, [item(4, "minecraft:emerald", 2, 5), item(2, "ergenverse:jade_slip"),
                 item(1, "minecraft:enchanted_book")])]),
    "heavenly_fate_sect_puppet_workshop": make_loot(
        "Heavenly Fate Sect \u2014 puppet_workshop. Fate-predictive construct puppets. INFERRED.",
        [(2, 4, [item(7, "minecraft:redstone", 3, 7), item(6, "minecraft:glowstone_dust", 2, 5),
                 item(5, "minecraft:iron_ingot", 2, 4), item(4, "minecraft:leather", 2, 4),
                 item(3, "minecraft:string", 2, 6)]),
         (1, 2, [item(4, "minecraft:emerald", 2, 4), item(2, "minecraft:clock"),
                 item(1, "minecraft:redstone", 4, 8)])]),
    "heavenly_fate_sect_mountain_cave": make_loot(
        "Heavenly Fate Sect \u2014 mountain_cave. Hidden star observation point. INFERRED.",
        [(2, 4, [item(8, "minecraft:glowstone_dust", 4, 10), item(6, "minecraft:redstone", 2, 5),
                 item(5, "minecraft:ender_pearl", 1, 2), item(4, "minecraft:coal", 2, 5),
                 item(3, "minecraft:obsidian", 1, 2)]),
         (1, 2, [item(3, "minecraft:emerald", 2, 5), item(2, "minecraft:compass"),
                 item(1, "minecraft:ender_pearl", 1, 2)])]),

    # ── TIER 3: HIGH ──
    "heavenly_fate_sect_underground_passage": make_loot(
        "Heavenly Fate Sect \u2014 underground_passage. Hidden fate archives.",
        [(3, 5, [item(7, "minecraft:redstone", 5, 12), item(6, "minecraft:glowstone_dust", 4, 8),
                 item(5, "minecraft:ender_pearl", 1, 2), item(4, "minecraft:obsidian", 2, 4),
                 item(3, "minecraft:emerald", 2, 5), item(3, "minecraft:iron_ingot", 2, 4)]),
         (1, 3, [item(4, "ergenverse:jade_slip", 1, 1), item(3, "minecraft:enchanted_book"),
                 item(2, "ergenverse:spirit_stone", 1, 1), item(1, "minecraft:ender_pearl", 1, 2)])]),
    "heavenly_fate_sect_secret_pavilion": make_loot(
        "Heavenly Fate Sect \u2014 secret_pavilion. Hidden fate manipulation techniques.",
        [(3, 5, [item(6, "minecraft:redstone", 4, 8), item(6, "minecraft:glowstone_dust", 3, 6),
                 item(5, "minecraft:book", 1, 3), item(4, "minecraft:obsidian", 2, 4),
                 item(3, "minecraft:emerald", 2, 5)]),
         (1, 3, [item(4, "ergenverse:jade_slip", 1, 1), item(3, "minecraft:enchanted_book"),
                 item(2, "ergenverse:spirit_stone", 1, 1), item(1, "ergenverse:dao_fragment")])]),
    "heavenly_fate_sect_inner_sect": make_loot(
        "Heavenly Fate Sect \u2014 inner_sect. Elder quarters. Advanced fate reading.",
        [(3, 5, [item(6, "minecraft:redstone", 4, 8), item(5, "minecraft:glowstone_dust", 3, 6),
                 item(5, "minecraft:ender_pearl", 1, 3), item(4, "minecraft:emerald", 3, 7),
                 item(3, "minecraft:obsidian", 2, 4), item(2, "minecraft:gold_ingot", 1, 3)]),
         (1, 3, [item(4, "ergenverse:jade_slip", 1, 2), item(3, "ergenverse:spirit_stone", 1, 1),
                 item(2, "minecraft:enchanted_book"), item(1, "ergenverse:dao_fragment")])]),
    "heavenly_fate_sect_hidden_treasury": make_loot(
        "Heavenly Fate Sect \u2014 hidden_treasury. Sect wealth. Fate-sealed vault.",
        [(3, 6, [item(5, "minecraft:emerald", 3, 8), item(5, "minecraft:glowstone_dust", 5, 10),
                 item(4, "minecraft:gold_ingot", 2, 5), item(4, "minecraft:redstone", 3, 6),
                 item(3, "minecraft:obsidian", 2, 4), item(2, "minecraft:diamond", 1, 1)]),
         (2, 3, [item(4, "ergenverse:spirit_stone", 1, 2), item(3, "ergenverse:jade_slip", 1, 1),
                 item(2, "minecraft:enchanted_book"), item(1, "ergenverse:dao_fragment"),
                 item(1, "ergenverse:spirit_stone_mid", 1, 1)])]),
    "heavenly_fate_sect_sword_tomb": make_loot(
        "Heavenly Fate Sect \u2014 sword_tomb. Weapons of fated cultivators. INFERRED.",
        [(3, 5, [item(6, "minecraft:glowstone_dust", 5, 10), item(5, "minecraft:iron_ingot", 3, 6),
                 item(5, "minecraft:redstone", 2, 5), item(4, "minecraft:emerald", 2, 5),
                 item(3, "minecraft:obsidian", 2, 4)]),
         (1, 3, [item(3, "ergenverse:jade_slip", 1, 1), item(2, "minecraft:enchanted_book"),
                 item(2, "minecraft:ender_pearl", 1, 2), item(1, "ergenverse:spirit_stone", 1, 1)])]),

    # ── TIER 4: MAX ──
    "heavenly_fate_sect_ancestor_hall": make_loot(
        "Heavenly Fate Sect \u2014 ancestor_hall. Heavenly Fate Ancestor's shrine. Fate convergence point.",
        [(3, 6, [item(5, "minecraft:emerald", 4, 10), item(5, "minecraft:glowstone_dust", 6, 12),
                 item(4, "minecraft:obsidian", 3, 6), item(4, "minecraft:redstone", 3, 6),
                 item(3, "minecraft:gold_ingot", 2, 5), item(2, "minecraft:diamond", 1, 2)]),
         (2, 3, [item(4, "ergenverse:spirit_stone", 1, 3), item(3, "ergenverse:jade_slip", 1, 2),
                 item(2, "ergenverse:dao_fragment"), item(2, "minecraft:enchanted_book"),
                 item(1, "ergenverse:spirit_stone_mid", 1, 2), item(1, "minecraft:nether_star")]),
         (0, 2, [item(2, "ergenverse:starry_sky_token"), item(1, "ergenverse:spirit_stone_high", 1, 2),
                 item(1, "ergenverse:immortal_stone"), empty(5)]),
         ]),
    "heavenly_fate_sect_core_formation_hall": make_loot(
        "Heavenly Fate Sect \u2014 core_formation_hall. Elder core formation. Advanced fate manipulation.",
        [(3, 6, [item(5, "minecraft:emerald", 4, 10), item(5, "minecraft:glowstone_dust", 5, 10),
                 item(4, "minecraft:obsidian", 3, 6), item(4, "minecraft:redstone", 3, 6),
                 item(3, "minecraft:gold_ingot", 2, 5), item(2, "minecraft:diamond", 1, 2)]),
         (2, 3, [item(4, "ergenverse:spirit_stone", 2, 4), item(3, "ergenverse:jade_slip", 1, 2),
                 item(2, "ergenverse:dao_fragment"), item(2, "minecraft:enchanted_book"),
                 item(1, "ergenverse:spirit_stone_mid", 1, 2)]),
         (0, 2, [item(2, "ergenverse:starry_sky_token"), item(1, "ergenverse:spirit_stone_high", 1, 2),
                 empty(6)]),
         ]),
}

NPCS = [
    {
        "npc_id": "npc_hf_outer_disciple_bai",
        "name": "Outer Disciple Bai",
        "nameCn": "外门弟子白",
        "canon_id": None, "type": "sect_disciple", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_outer_gate", "cultivation": "CONDENSATION early",
        "personality": "star-struck, romantic about fate, optimistic",
        "speech": "wondering, starry-eyed, occasionally philosophical",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390101,
        "initiation_lines": [
            "Welcome to the Heavenly Fate Sect. The stars foretold your arrival. Or perhaps they foretold someone more interesting.",
            "Every cultivator who passes through this gate has a fate-thread. I can see them, faintly, like spider silk in moonlight.",
            "The Star Tower glows at night. Elder Tian says it is the ancestor's will. I say it is beautiful.",
            "Do not linger at the gate. The fate currents here are chaotic. You might trip."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "patrol", "location": "heavenly_fate_sect_outer_gate", "duration": 120},
            {"time": "0800", "action": "stand_guard", "location": "heavenly_fate_sect_outer_gate", "duration": 240},
            {"time": "1200", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "patrol", "location": "heavenly_fate_sect_main_plaza", "duration": 120},
            {"time": "1500", "action": "train", "location": "heavenly_fate_sect_trial_grounds", "duration": 120},
            {"time": "1700", "action": "stand_guard", "location": "heavenly_fate_sect_outer_gate", "duration": 180},
            {"time": "2000", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {"task_id": "gate_star_dust", "name": "Gate Star Dust Supply",
             "description": "The gate formations need glowstone dust to maintain the fate-thread detection array.",
             "required_items": {"minecraft:arrow": 8, "minecraft:glowstone_dust": 6},
             "rewards": {"minecraft:emerald": 5, "minecraft:book": 1}},
            {"task_id": "visitor_fate_record", "name": "Visitor Fate Record",
             "description": "Record every visitor's fate-thread reading. Bring paper and ink for the log.",
             "required_items": {"minecraft:paper": 5, "minecraft:ink_sac": 2},
             "rewards": {"minecraft:emerald": 4, "minecraft:experience_bottle": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_star_reader_tian",
        "name": "Star Reader Tian",
        "nameCn": "观星师天",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_spirit_spring", "cultivation": "NASCENT_SOUL early",
        "personality": "distracted, gazes upward, speaks in riddles about stars",
        "speech": "cryptic, poetic, references star positions",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390102,
        "initiation_lines": [
            "The Southern Dipper shifted three degrees last night. Someone in Zhao Country will die before dawn.",
            "I do not predict the future. I read what the stars have already written. The difference is everything.",
            "The spring here reflects starlight even at noon. Drink from it and you will see your fate-thread for one heartbeat.",
            "Liu Mei left this sect three years ago. Her fate-thread was the brightest I have ever seen. Then it vanished."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "observe", "location": "heavenly_fate_sect_spirit_spring", "duration": 180},
            {"time": "0800", "action": "read_stars", "location": "heavenly_fate_sect_main_plaza", "duration": 180},
            {"time": "1100", "action": "meditate", "location": "heavenly_fate_sect_spirit_spring", "duration": 120},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "teach", "location": "heavenly_fate_sect_main_plaza", "duration": 120},
            {"time": "1600", "action": "observe", "location": "heavenly_fate_sect_mountain_cave", "duration": 180},
            {"time": "1900", "action": "read_stars", "location": "heavenly_fate_sect_main_plaza", "duration": 300},
            {"time": "2400", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 360},
        ],
        "sect_tasks": [
            {"task_id": "star_observation_supplies", "name": "Star Observation Supplies",
             "description": "I need glowstone dust and ender pearls for the star-reflection array at the spring.",
             "required_items": {"minecraft:glowstone_dust": 8, "minecraft:ender_pearl": 2},
             "rewards": {"minecraft:emerald": 8, "minecraft:enchanted_book": 1}},
            {"task_id": "fate_reading_tools", "name": "Fate Reading Tools",
             "description": "Bring compasses and clocks. I need to calibrate the fate-thread detection instruments.",
             "required_items": {"minecraft:compass": 3, "minecraft:clock": 2},
             "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_library_keeper_xing",
        "name": "Library Keeper Xing",
        "nameCn": "藏经阁主星",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_library", "cultivation": "NASCENT_SOUL early",
        "personality": "precise, organized, treats fate-records as sacred",
        "speech": "exact, references specific scrolls, mildly condescending",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390103,
        "initiation_lines": [
            "The library contains four thousand scrolls on fate manipulation. Section C-7 is restricted. Do not ask why.",
            "Every cultivator's fate-thread is recorded here. Yours too, if you stay long enough.",
            "Yun Quezi's star charts are in section A-3. Hong Die's fate-reading notes are in B-9. Both left decades ago.",
            "The Corpse Yin Sect requested access to our fate archives. We refused. Their fate-threads are abhorrent."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "organize", "location": "heavenly_fate_sect_library", "duration": 240},
            {"time": "1000", "action": "study", "location": "heavenly_fate_sect_library", "duration": 180},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "teach", "location": "heavenly_fate_sect_main_plaza", "duration": 120},
            {"time": "1600", "action": "study", "location": "heavenly_fate_sect_library", "duration": 240},
            {"time": "2000", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {"task_id": "scroll_copying_fate", "name": "Scroll Copying: Fate Arts",
             "description": "Copy the fate manipulation scrolls for the inner elders. The ink must be precise.",
             "required_items": {"minecraft:paper": 6, "minecraft:ink_sac": 4},
             "rewards": {"minecraft:book": 2, "minecraft:emerald": 5}},
            {"task_id": "restricted_section_retrieval", "name": "Restricted Section Retrieval",
             "description": "A scroll was borrowed from section C-7 without authorization. Return it.",
             "required_items": {"minecraft:book": 1, "minecraft:emerald": 3},
             "rewards": {"minecraft:enchanted_book": 1, "ergenverse:jade_slip": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_fate_alchemist_yue",
        "name": "Fate Alchemist Yue",
        "nameCn": "命丹师月",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_alchemy_courtyard", "cultivation": "NASCENT_SOUL mid",
        "personality": "dreamy, impractical, believes alchemy is just applied fate",
        "speech": "meandering, references fate in everything, gentle",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390104,
        "initiation_lines": [
            "Every pill is a small fate. The ingredients choose each other. I merely facilitate the meeting.",
            "The Fate Pill requires starlight essence. I collect it in bottles from the spring at midnight.",
            "The Soul Refining Sect's alchemist visited once. He wanted our Fate Pill formula. Fate said no.",
            "Bring me bottles and coal. The furnace burns with starlight, not flame. It is very temperamental."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "gather", "location": "heavenly_fate_sect_spirit_herb_garden", "duration": 120},
            {"time": "0700", "action": "work", "location": "heavenly_fate_sect_alchemy_courtyard", "duration": 300},
            {"time": "1200", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "work", "location": "heavenly_fate_sect_alchemy_courtyard", "duration": 300},
            {"time": "1800", "action": "collect_starlight", "location": "heavenly_fate_sect_spirit_spring", "duration": 120},
            {"time": "2000", "action": "work", "location": "heavenly_fate_sect_alchemy_courtyard", "duration": 180},
            {"time": "2300", "action": "rest", "location": "heavenly_fate_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {"task_id": "starlight_fuel", "name": "Starlight Furnace Fuel",
             "description": "The starlight furnace needs glass bottles and coal to maintain the star-fire.",
             "required_items": {"minecraft:glass_bottle": 4, "minecraft:coal": 6},
             "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 6}},
            {"task_id": "fate_pill_ingredients", "name": "Fate Pill Ingredients",
             "description": "I need spirit stones and blaze powder for the Fate Pill refinement cycle.",
             "required_items": {"ergenverse:spirit_stone": 1, "minecraft:blaze_powder": 2},
             "rewards": {"minecraft:enchanted_book": 1, "ergenverse:jade_slip": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_array_weaver_chen",
        "name": "Array Weaver Chen",
        "nameCn": "阵法师陈",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_array_hall", "cultivation": "NASCENT_SOUL late",
        "personality": "meticulous, paranoid about fate-disruption, checks threads constantly",
        "speech": "precise, references measurements and fate-thread tensions",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390105,
        "initiation_lines": [
            "The fate-thread array at the eastern perimeter has a tension imbalance of 0.3 degrees. Unacceptable.",
            "Other sects use redstone for power. We use it to weave fate-threads. There is a difference.",
            "The Xuan Dao Sect sent an array student to study with us. I sent him back. His threads were too rigid.",
            "If you stand on the center inscription at midnight, you will see your fate-thread for three heartbeats. Do not. It changes you."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "inspect", "location": "heavenly_fate_sect_array_hall", "duration": 180},
            {"time": "0800", "action": "repair", "location": "heavenly_fate_sect_outer_gate", "duration": 120},
            {"time": "1000", "action": "work", "location": "heavenly_fate_sect_array_hall", "duration": 240},
            {"time": "1400", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1500", "action": "inspect", "location": "heavenly_fate_sect_underground_passage", "duration": 120},
            {"time": "1700", "action": "work", "location": "heavenly_fate_sect_array_hall", "duration": 180},
            {"time": "2000", "action": "rest", "location": "heavenly_fate_sect_inner_sect", "duration": 600},
        ],
        "sect_tasks": [
            {"task_id": "fate_array_redstone", "name": "Fate Array Redstone Supply",
             "description": "The perimeter fate arrays need redstone and ender pearls for thread-weaving maintenance.",
             "required_items": {"minecraft:redstone": 10, "minecraft:ender_pearl": 2},
             "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1}},
            {"task_id": "obsidian_fate_conduit", "name": "Obsidian Fate Conduit",
             "description": "The inner array needs obsidian conduits to redirect fate-thread flow.",
             "required_items": {"minecraft:obsidian": 6, "minecraft:iron_ingot": 4},
             "rewards": {"minecraft:emerald": 8, "ergenverse:jade_slip": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_fate_beast_tender_lin",
        "name": "Fate Beast Tender Lin",
        "nameCn": "命兽管事林",
        "canon_id": None, "type": "sect_disciple", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_spirit_beast_pens", "cultivation": "CONDENSATION late",
        "personality": "gentle with beasts, impatient with people, hums fate-melodies",
        "speech": "soft when speaking of beasts, curt with humans",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390106,
        "initiation_lines": [
            "These beasts can see fate-threads more clearly than most elders. We do not cage them. We negotiate.",
            "The star-gazing hawks in the eastern pen are from Qilin City. Their previous owner could not read their fate-calls.",
            "A beast that has seen its own fate-thread goes quiet. Permanently. I do not let visitors near the western pen.",
            "Bring me redstone and leather. The fate-sensing collars need constant adjustment."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "work", "location": "heavenly_fate_sect_spirit_beast_pens", "duration": 300},
            {"time": "1100", "action": "inspect", "location": "heavenly_fate_sect_mountain_cave", "duration": 120},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "work", "location": "heavenly_fate_sect_spirit_beast_pens", "duration": 240},
            {"time": "1800", "action": "deliver", "location": "heavenly_fate_sect_alchemy_courtyard", "duration": 60},
            {"time": "1900", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {"task_id": "fate_collar_repair", "name": "Fate-Sensing Collar Repair",
             "description": "The beast collars need redstone and leather to maintain fate-thread detection.",
             "required_items": {"minecraft:redstone": 8, "minecraft:leather": 4},
             "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1}},
            {"task_id": "beast_herbs", "name": "Beast Herb Delivery",
             "description": "Bring bone meal and wheat for the fate-sensitive beasts from the garden.",
             "required_items": {"minecraft:bone_meal": 6, "minecraft:wheat": 8},
             "rewards": {"minecraft:emerald": 5, "minecraft:glowstone_dust": 4}},
        ],
    },
    {
        "npc_id": "npc_hf_trial_judge_han",
        "name": "Trial Judge Han",
        "nameCn": "试炼官韩",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_trial_grounds", "cultivation": "NASCENT_SOUL mid",
        "personality": "impartial, reads fate-threads during trials, unsettlingly calm",
        "speech": "measured, references fate-percentages, never surprised",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390107,
        "initiation_lines": [
            "Eighty-two percent of disciples pass the fate-reading trial. Only twelve percent pass the fate-acceptance trial.",
            "The trial is simple: read your own fate-thread. Those who see death weep. Those who accept it, advance.",
            "We do not test talent. We test whether a disciple can bear knowing their own destiny.",
            "The records show that disciples from the Zhao Country region have the highest fate-acceptance rate. Curious."
        ],
        "daily_schedule": [
            {"time": "0700", "action": "oversee", "location": "heavenly_fate_sect_trial_grounds", "duration": 240},
            {"time": "1100", "action": "evaluate", "location": "heavenly_fate_sect_main_plaza", "duration": 120},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "oversee", "location": "heavenly_fate_sect_trial_grounds", "duration": 240},
            {"time": "1800", "action": "report", "location": "heavenly_fate_sect_inner_sect", "duration": 120},
            {"time": "2000", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {"task_id": "trial_supplies", "name": "Trial Grounds Supplies",
             "description": "The trials need glowstone dust for fate-thread illumination and arrows for the defense portion.",
             "required_items": {"minecraft:glowstone_dust": 8, "minecraft:arrow": 12},
             "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2}},
            {"task_id": "fate_record_compilation", "name": "Fate Record Compilation",
             "description": "Compile the trial results. Bring redstone for the recording array and paper for the logs.",
             "required_items": {"minecraft:redstone": 6, "minecraft:paper": 8},
             "rewards": {"minecraft:emerald": 6, "minecraft:enchanted_book": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_sword_elder_feng",
        "name": "Sword Elder Feng",
        "nameCn": "剑峰长老风",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_sword_tomb", "cultivation": "NASCENT_SOUL late",
        "personality": "melancholic, traces sword-fate-threads, mourns fallen cultivators",
        "speech": "soft, reverent, speaks of swords as if they are people",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390108,
        "initiation_lines": [
            "Every sword here has a fate-thread. Some are still attached to their owners. The threads glow blue at midnight.",
            "The sword in the center has no thread. It was cut. By its owner. I do not know why.",
            "Zhao Xingsha left his sword here before departing. Its thread still points south. Toward the Sea of Devils.",
            "Bring me iron and glowstone. The fate-thread preservation ritual requires both."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "heavenly_fate_sect_sword_tomb", "duration": 180},
            {"time": "0800", "action": "maintain", "location": "heavenly_fate_sect_sword_tomb", "duration": 300},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "maintain", "location": "heavenly_fate_sect_sword_tomb", "duration": 240},
            {"time": "1800", "action": "patrol", "location": "heavenly_fate_sect_sword_peak", "duration": 120},
            {"time": "2000", "action": "meditate", "location": "heavenly_fate_sect_sword_tomb", "duration": 180},
            {"time": "2300", "action": "rest", "location": "heavenly_fate_sect_sword_tomb", "duration": 420},
        ],
        "sect_tasks": [
            {"task_id": "sword_fate_preservation", "name": "Sword Fate Preservation",
             "description": "The sword fate-threads need iron and glowstone for the preservation ritual.",
             "required_items": {"minecraft:iron_ingot": 6, "minecraft:glowstone_dust": 8},
             "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1}},
            {"task_id": "xingsha_sword_retrieval", "name": "Zhao Xingsha's Sword",
             "description": "Retrieve items from the tomb for the elder council. Gold and obsidian as tribute.",
             "required_items": {"minecraft:gold_ingot": 4, "minecraft:obsidian": 4},
             "rewards": {"ergenverse:spirit_stone": 2, "minecraft:enchanted_book": 1}},
        ],
    },
    {
        "npc_id": "npc_hf_underground_archivist_meng",
        "name": "Underground Archivist Meng",
        "nameCn": "地下档案官孟",
        "canon_id": None, "type": "sect_disciple", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_underground_passage", "cultivation": "FOUNDATION_ESTABLISHMENT late",
        "personality": "nervous, knows secrets that weigh on him, fidgets",
        "speech": "whispered, checks over shoulder, drops hints about hidden knowledge",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390109,
        "initiation_lines": [
            "You found the passage. The fate-threads here are... tangled. Be careful where you step.",
            "The underground archives contain every cultivator's fate prediction since the sect's founding. Some predictions came true. Some did not. The ancestors do not like to discuss the latter.",
            "I am not supposed to be here. But the fate-threads in the archives called to me. They whisper names.",
            "Bring me paper and redstone. I am... compiling something. For personal research. Not for the elders."
        ],
        "daily_schedule": [
            {"time": "0800", "action": "idle", "location": "heavenly_fate_sect_underground_passage", "duration": 180},
            {"time": "1100", "action": "research", "location": "heavenly_fate_sect_underground_passage", "duration": 120},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "idle", "location": "heavenly_fate_sect_underground_passage", "duration": 180},
            {"time": "1700", "action": "research", "location": "heavenly_fate_sect_underground_passage", "duration": 120},
            {"time": "1900", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {"task_id": "archive_compilation", "name": "Archive Compilation",
             "description": "I need paper and redstone to compile the fate-thread analysis. Do not tell the elders.",
             "required_items": {"minecraft:paper": 8, "minecraft:redstone": 6},
             "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 7}},
            {"task_id": "hidden_archive_access", "name": "Hidden Archive Access",
             "description": "I can show you the forbidden predictions. The price is emeralds and gold.",
             "required_items": {"minecraft:gold_ingot": 3, "minecraft:emerald": 8},
             "rewards": {"ergenverse:spirit_stone": 2, "ergenverse:jade_slip": 2}},
        ],
    },
    {
        "npc_id": "npc_hf_ancestor_elder_yuan",
        "name": "Ancestor Elder Yuan",
        "nameCn": "祖师长老元",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_ancestor_hall", "cultivation": "NASCENT_SOUL peak",
        "personality": "serene, speaks as if channeling the ancestor, unfathomable",
        "speech": "slow, prophetic, references fate as if reading it in real-time",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390110,
        "initiation_lines": [
            "The ancestor's fate-thread spans the entire sky. To look upon it is to see the shape of time.",
            "I have read every fate-thread in this hall. Yours is... interesting. It branches in seven directions.",
            "The Heavenly Fate Ancestor predicted the fall of three dynasties. Two have fallen. The third approaches.",
            "Bring gold and obsidian. The ancestor's shrine needs devotion. Fate rewards the faithful. Eventually."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "heavenly_fate_sect_ancestor_hall", "duration": 240},
            {"time": "0900", "action": "ritual", "location": "heavenly_fate_sect_ancestor_hall", "duration": 180},
            {"time": "1200", "action": "eat", "location": "heavenly_fate_sect_inner_sect", "duration": 60},
            {"time": "1300", "action": "maintain", "location": "heavenly_fate_sect_ancestor_hall", "duration": 240},
            {"time": "1700", "action": "ritual", "location": "heavenly_fate_sect_ancestor_hall", "duration": 180},
            {"time": "2000", "action": "meditate", "location": "heavenly_fate_sect_ancestor_hall", "duration": 300},
            {"time": "0100", "action": "rest", "location": "heavenly_fate_sect_ancestor_hall", "duration": 300},
        ],
        "sect_tasks": [
            {"task_id": "ancestor_devotion", "name": "Ancestor Devotion Offering",
             "description": "The ancestor's shrine requires gold and obsidian for the weekly fate-reading ritual.",
             "required_items": {"minecraft:gold_ingot": 4, "minecraft:obsidian": 6},
             "rewards": {"ergenverse:spirit_stone": 3, "minecraft:emerald": 6}},
            {"task_id": "ancestor_fate_record", "name": "Ancestor Fate Record",
             "description": "Transcribe the ancestor's fate-predictions. The ink must be fresh and the paper pure.",
             "required_items": {"minecraft:paper": 8, "minecraft:ink_sac": 5, "minecraft:book": 2},
             "rewards": {"ergenverse:starry_sky_token": 1, "ergenverse:jade_slip": 2}},
        ],
    },
    {
        "npc_id": "npc_hf_inner_elder_shao",
        "name": "Inner Elder Shao",
        "nameCn": "内门长老邵",
        "canon_id": None, "type": "sect_elder", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_core_formation_hall", "cultivation": "NASCENT_SOUL peak",
        "personality": "calculating, views everything through fate-lens, manipulative but not evil",
        "speech": "strategic, always assessing fate-probabilities, veiled threats",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390111,
        "initiation_lines": [
            "There is a sixty-three percent chance you will survive the next year. I can improve those odds. For a price.",
            "The Corpse Yin Sect proposed an alliance. The fate-threads showed a seventy-eight percent chance of betrayal. We declined.",
            "Liu Mei's departure was not a loss. Her fate-thread was always destined to leave. We merely accelerated the schedule.",
            "Bring me spirit stones and emeralds. I am researching a technique to read fate-threads across distances."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "research", "location": "heavenly_fate_sect_core_formation_hall", "duration": 300},
            {"time": "1000", "action": "inspect", "location": "heavenly_fate_sect_inner_sect", "duration": 120},
            {"time": "1200", "action": "meet", "location": "heavenly_fate_sect_secret_pavilion", "duration": 120},
            {"time": "1400", "action": "research", "location": "heavenly_fate_sect_core_formation_hall", "duration": 240},
            {"time": "1800", "action": "inspect", "location": "heavenly_fate_sect_hidden_treasury", "duration": 120},
            {"time": "2000", "action": "research", "location": "heavenly_fate_sect_core_formation_hall", "duration": 180},
            {"time": "2300", "action": "rest", "location": "heavenly_fate_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {"task_id": "fate_research_materials", "name": "Fate Research Materials",
             "description": "I need spirit stones and emeralds to power the long-range fate-thread array.",
             "required_items": {"ergenverse:spirit_stone": 3, "minecraft:emerald": 8},
             "rewards": {"minecraft:enchanted_book": 2, "ergenverse:jade_slip": 2}},
            {"task_id": "outpost_relay_supply", "name": "Outpost Relay Supply",
             "description": "The remote observation outpost needs supplies for the star-monitoring relay.",
             "required_items": {"minecraft:ender_pearl": 4, "minecraft:obsidian": 8, "minecraft:compass": 3},
             "rewards": {"ergenverse:starry_sky_token": 1, "ergenverse:spirit_stone": 3}},
        ],
    },
    {
        "npc_id": "npc_hf_herb_tender_xiao",
        "name": "Herb Tender Xiao",
        "nameCn": "药园管事萧",
        "canon_id": None, "type": "sect_disciple", "faction": "heavenly_fate_sect",
        "location": "heavenly_fate_sect_spirit_herb_garden", "cultivation": "FOUNDATION_ESTABLISHMENT mid",
        "personality": "patient, sings to plants, believes herbs have fate-threads too",
        "speech": "gentle, botanical, occasionally mystical about plant fate",
        "relationship_to_wanglin": "neutral",
        "dialogue_available": True, "quest_available": True, "trade_available": True,
        "teaching_available": False, "canon_confidence": 0, "derivation_type": "I", "salt": 390112,
        "initiation_lines": [
            "Every herb has a fate-thread. This one is destined to become a pill. That one is destined to wither. I still water it.",
            "The star-nourished herbs grow only in glowstone-rich soil. Without starlight, they are just weeds.",
            "The Alchemist needs the fate-lotus from the spring. I grow it here. It only blooms when a cultivator with a bright fate-thread walks past.",
            "Bring me bone meal and wheat. The herbs need feeding, and the starlight alone is not enough."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "tend", "location": "heavenly_fate_sect_spirit_herb_garden", "duration": 300},
            {"time": "1100", "action": "gather", "location": "heavenly_fate_sect_spirit_spring", "duration": 120},
            {"time": "1300", "action": "eat", "location": "heavenly_fate_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "tend", "location": "heavenly_fate_sect_spirit_herb_garden", "duration": 240},
            {"time": "1800", "action": "deliver", "location": "heavenly_fate_sect_alchemy_courtyard", "duration": 60},
            {"time": "1900", "action": "rest", "location": "heavenly_fate_sect_disciple_dormitories", "duration": 660},
        ],
        "sect_tasks": [
            {"task_id": "garden_starlight_soil", "name": "Garden Starlight Soil",
             "description": "The herbs need bone meal and wheat to enrich the glowstone-starlight soil.",
             "required_items": {"minecraft:bone_meal": 6, "minecraft:wheat": 8},
             "rewards": {"minecraft:emerald": 5, "minecraft:glowstone_dust": 3}},
            {"task_id": "herb_delivery_alchemist", "name": "Herb Delivery to Alchemist",
             "description": "The Alchemist needs fresh herbs and bottles from the garden.",
             "required_items": {"minecraft:glass_bottle": 3, "minecraft:bone_meal": 4},
             "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1}},
        ],
    },
]


def main():
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)

    loot_count = 0
    for name, data in LOOT_TABLES.items():
        fp = os.path.join(LOOT_DIR, name.replace("heavenly_fate_sect_", "heavenly_fate_sect_") + ".json")
        with open(fp, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
        loot_count += 1
        print(f"  [LOOT] {os.path.basename(fp)}")

    npc_count = 0
    for npc in NPCS:
        fp = os.path.join(NPC_DIR, npc["npc_id"] + ".json")
        npc["_xianxia_schema"] = 1
        with open(fp, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
        npc_count += 1
        print(f"  [NPC]  {os.path.basename(fp)}")

    print(f"\nDone: {loot_count} loot tables + {npc_count} NPCs for Heavenly Fate Sect.")
    print(f"Existing orphan kept: heavenly_fate_star_tower.json")
    print(f"Total: {loot_count + 1} loot tables (21 new + 1 orphan)")


if __name__ == "__main__":
    main()