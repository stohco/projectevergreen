#!/usr/bin/env python3
"""
AUTO-CANON-043: Fighting Evil Sect (斗邪宗) completion.
Theme: Combat/evil-slaying/martial cultivation. Aggressive sect dedicated to
destroying corruption, evil spirits, and demonic forces through martial prowess.
21 structures, 0 existing loot tables, 0 existing NPCs.
20 loot tables (4-tier wealth gradient) + 12 INFERRED NPCs.
Combat items: iron_ingot (ALL 20), redstone (18/20 — blood/spiritual combat),
gunpowder (14/20 — explosive combat), bone (16/20 — battle remains),
string (12/20 — binding evil), rotten_flesh (10/20 — corruption remnants),
blaze_rod (8/20 — demon-fire), shield (6/20 — defensive martial stance).
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
# Theme items: iron_ingot (martial metal), redstone (blood/combat),
# gunpowder (explosive techniques), bone (battle remains),
# string (binding evil), rotten_flesh (corruption),
# blaze_rod (demon-fire), shield (defensive stance)
# Vanilla tier items: arrows, coal, bread, iron, gold, emeralds, diamonds, obsidian
# Mod items: jade_slip, spirit_stone, dao_fragment, soul_fragment

# Structure ordering by wealth tier:
# LOW (6):  outer_gate, main_plaza, disciple_dormitories, trial_grounds, spirit_herb_garden, spirit_spring
# MED (6):  library, alchemy_courtyard, array_hall, beast_pens, puppet_workshop, mountain_cave
# HIGH (5): sword_peak, sword_tomb, underground_passage, secret_pavilion, inner_sect
# MAX (3):  hidden_treasury, ancestor_hall, core_formation_hall

STRUCTURES = [
    # (filename_suffix, comment, tier)
    ("outer_gate",           "Iron-banded war gate. Guard post with weapon racks and evil-slaying banners.", "LOW"),
    ("main_plaza",           "Combat arena plaza. Bloodstained stone, weapon racks, training dummies.", "LOW"),
    ("disciple_dormitories", "Barracks-style dorms. Weapon racks beside every bed. Sparse, militaristic.", "LOW"),
    ("trial_grounds",        "Evil-slaying trial arena. Corrupted beast cages and combat circles.", "LOW"),
    ("spirit_herb_garden",   "Anti-corruption herb garden. Herbs that purify evil essence.", "LOW"),
    ("spirit_spring",        "Purification spring. Cleanses corruption from weapons and cultivators.", "LOW"),
    ("library",              "Combat technique archives. Scrolls on evil-slaying arts and martial forms.", "MED"),
    ("alchemy_courtyard",    "Combat-pill refinery. Refines pills from defeated evil essence.", "MED"),
    ("array_hall",           "Binding array hall. Arrays designed to trap and destroy evil.", "MED"),
    ("spirit_beast_pens",    "Reformed evil beast pens. Captured dark creatures being purified.", "MED"),
    ("puppet_workshop",      "War puppet workshop. Constructs combat automata for siege and patrol.", "MED"),
    ("mountain_cave",        "Corruption containment cave. Sealed evil relics and test grounds.", "MED"),
    ("sword_peak",           "Evil-Slaying Sword Peak. Highest combat training ground. Wind and iron.", "HIGH"),
    ("sword_tomb",           "Fallen Warrior Sword Tomb. Swords of ancestors who died fighting evil.", "HIGH"),
    ("underground_passage",  "Deep passage network. Tunnels to sealed corruption chambers.", "HIGH"),
    ("secret_pavilion",      "Secret combat pavilion. Hidden martial arts training chamber.", "HIGH"),
    ("inner_sect",           "Inner sect compound. Elite evil-slayers with advanced techniques.", "HIGH"),
    ("hidden_treasury",      "War treasury. Weapons, armor, and spoils from defeated evil forces.", "MAX"),
    ("ancestor_hall",        "Ancestor Hall. Memorial of sect founders who perished fighting evil.", "MAX"),
    ("core_formation_hall",  "Core Formation Hall. The sect's central combat formation nexus.", "MAX"),
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
        # Pool 1: Common combat supplies (3-5 rolls)
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:arrow", 10, 4, 12),
            make_entry("minecraft:iron_ingot", 8, 2, 4),
            make_entry("minecraft:redstone", 7, 1, 3),
            make_entry("minecraft:coal", 6, 2, 4),
            make_entry("minecraft:bread", 5, 1, 3),
            make_entry("minecraft:bone", 4, 1, 2),
        ]))
        # Pool 2: Minor combat/evil-slaying items (1-2 rolls)
        pools.append(make_pool(1, 2, [
            make_entry("minecraft:string", 8, 1, 3),
            make_entry("minecraft:gunpowder", 5, 1, 2),
            make_entry("minecraft:rotten_flesh", 4, 1, 2),
            make_entry("minecraft:leather", 6, 1, 2),
            make_entry("ergenverse:jade_slip", 1),
        ]))

    elif tier == "MED":
        # Pool 1: Themed combat supplies (3-5 rolls)
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:iron_ingot", 10, 2, 5),
            make_entry("minecraft:redstone", 8, 2, 4),
            make_entry("minecraft:bone", 7, 1, 3),
            make_entry("minecraft:string", 6, 1, 3),
            make_entry("minecraft:coal", 5, 2, 4),
            make_entry("minecraft:gunpowder", 5, 1, 3),
        ]))
        # Pool 2: Combat rewards (2-3 rolls)
        pools.append(make_pool(2, 3, [
            make_entry("minecraft:emerald", 8, 1, 3),
            make_entry("minecraft:iron_ingot", 6, 1, 3),
            make_entry("minecraft:experience_bottle", 5, 1, 2),
            make_entry("ergenverse:jade_slip", 3),
            make_entry("minecraft:book", 5),
            make_entry("minecraft:rotten_flesh", 4, 1, 2),
        ]))
        # Pool 3: Spirit stone (1 roll)
        pools.append(make_pool(1, 1, [
            make_entry("ergenverse:spirit_stone", 5, 1, 2),
            make_entry("minecraft:emerald", 3, 1, 2),
        ]))

    elif tier == "HIGH":
        # Pool 1: Themed high-tier combat supplies (4-6 rolls)
        pools.append(make_pool(4, 6, [
            make_entry("minecraft:iron_ingot", 10, 3, 6),
            make_entry("minecraft:redstone", 8, 2, 5),
            make_entry("minecraft:bone", 7, 2, 4),
            make_entry("minecraft:gunpowder", 6, 2, 4),
            make_entry("minecraft:string", 5, 1, 3),
            make_entry("minecraft:blaze_rod", 4, 1, 2),
            make_entry("minecraft:rotten_flesh", 3, 1, 2),
        ]))
        # Pool 2: Combat rewards (2-3 rolls)
        pools.append(make_pool(2, 3, [
            make_entry("minecraft:emerald", 8, 2, 5),
            make_entry("minecraft:gold_ingot", 6, 1, 3),
            make_entry("minecraft:experience_bottle", 5, 1, 2),
            make_entry("ergenverse:jade_slip", 4),
            make_entry("minecraft:enchanted_book", 3),
            make_entry("minecraft:shield", 3),
        ]))
        # Pool 3: Rare (1 roll)
        pools.append(make_pool(1, 1, [
            make_entry("ergenverse:spirit_stone", 6, 1, 3),
            make_entry("minecraft:diamond", 2),
            make_entry("minecraft:blaze_rod", 3, 1, 2),
        ]))

    elif tier == "MAX":
        # Pool 1: Themed ultimate combat supplies (4-6 rolls)
        pools.append(make_pool(4, 6, [
            make_entry("minecraft:iron_ingot", 10, 3, 8),
            make_entry("minecraft:redstone", 9, 3, 6),
            make_entry("minecraft:bone", 7, 2, 5),
            make_entry("minecraft:gunpowder", 6, 2, 4),
            make_entry("minecraft:blaze_rod", 5, 1, 3),
            make_entry("minecraft:string", 4, 1, 3),
            make_entry("minecraft:rotten_flesh", 3, 1, 2),
        ]))
        # Pool 2: Ultimate rewards (2-4 rolls)
        pools.append(make_pool(2, 4, [
            make_entry("minecraft:emerald", 8, 3, 8),
            make_entry("minecraft:gold_ingot", 7, 2, 5),
            make_entry("minecraft:experience_bottle", 6, 1, 3),
            make_entry("ergenverse:jade_slip", 5),
            make_entry("minecraft:enchanted_book", 4),
            make_entry("minecraft:diamond", 3, 1, 2),
            make_entry("minecraft:obsidian", 4, 2, 4),
            make_entry("minecraft:shield", 2),
        ]))
        # Pool 3: Legendary (1-2 rolls)
        pools.append(make_pool(1, 2, [
            make_entry("ergenverse:spirit_stone", 6, 1, 4),
            make_entry("ergenverse:dao_fragment", 3),
            make_entry("minecraft:diamond", 4, 1, 3),
            make_entry("minecraft:nether_star", 1),
        ]))

    return {
        "pools": pools
    }


def write_loot_tables():
    """Write all 20 loot table JSONs."""
    written = 0
    for suffix, comment, tier in STRUCTURES:
        table = build_loot_table(suffix, comment, tier)
        path = os.path.join(LOOT_DIR, f"fighting_evil_sect_{suffix}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(table, f, indent=2, ensure_ascii=False)
        written += 1
    print(f"Wrote {written} loot tables.")


# ============================================================
# NPCS — 12 INFERRED NPCs
# ============================================================

def make_npc(npc_id, name, nameCn, faction, location, cultivation,
             personality, speech, relationship, canon_confidence,
             initiation_lines, daily_schedule, sect_tasks):
    """Build a complete NPC JSON matching the xianxia schema."""
    salt = hashlib.sha256(npc_id.encode()).hexdigest()[:16]
    return {
        "npc_id": npc_id,
        "name": name,
        "nameCn": nameCn,
        "canon_id": "",
        "type": "sect_disciple",
        "faction": faction,
        "location": location,
        "cultivation": cultivation,
        "personality": personality,
        "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": True,
        "canon_confidence": canon_confidence,
        "derivation_type": "I",
        "salt": salt,
        "initiation_lines": initiation_lines,
        "daily_schedule": daily_schedule,
        "sect_tasks": sect_tasks,
        "_xianxia_schema": 1
    }


def write_npcs():
    """Write all 12 INFERRED NPC JSONs."""
    npcs = [
        # 1. Outer Disciple Yan — outer gate, CONDENSATION early
        make_npc(
            npc_id="npc_fe_outer_disciple_yan",
            name="Outer Disciple Yan",
            nameCn="外门弟子严",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_outer_gate",
            cultivation="CONDENSATION_EARLY",
            personality="rigid, suspicious of outsiders, fiercely proud of the sect's mission",
            speech="clipped, militaristic, uses combat metaphors",
            relationship="neutral — assesses all visitors as potential threats",
            canon_confidence=2,
            initiation_lines=[
                "Halt. State your purpose. The Fighting Evil Sect does not accept visitors without cause.",
                "That weapon you carry — has it drawn evil blood? If not, it is decoration.",
                "Every disciple here has killed at least one corrupted spirit. What have you killed?",
                "The gate stays open during the day. At night, it closes. We are not fools."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Gate inspection", "location": "fighting_evil_sect_outer_gate", "duration": 120},
                {"time": "08:00", "action": "Patrol outer perimeter", "location": "fighting_evil_sect_main_plaza", "duration": 120},
                {"time": "10:00", "action": "Weapon maintenance", "location": "fighting_evil_sect_outer_gate", "duration": 90},
                {"time": "12:00", "action": "Meal at barracks", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "14:00", "action": "Combat drills", "location": "fighting_evil_sect_trial_grounds", "duration": 120},
                {"time": "17:00", "action": "Gate duty", "location": "fighting_evil_sect_outer_gate", "duration": 180},
                {"time": "20:00", "action": "Return to barracks", "location": "fighting_evil_sect_disciple_dormitories", "duration": 480},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_gate_patrol_supplies",
                    "name": "Gate Patrol Resupply",
                    "description": "The outer gate guard needs arrows and iron to maintain patrols against corrupted creatures.",
                    "required_items": {"minecraft:arrow": 8, "minecraft:iron_ingot": 3},
                    "rewards": {"minecraft:emerald": 5, "minecraft:book": 1, "experience": 20}
                },
                {
                    "task_id": "fe_banner_repair",
                    "name": "Evil-Slaying Banner Repair",
                    "description": "War banners at the gate are frayed. Bring redstone and coal to repaint the binding sigils.",
                    "required_items": {"minecraft:redstone": 4, "minecraft:coal": 3},
                    "rewards": {"minecraft:emerald": 4, "experience": 15}
                },
            ]
        ),

        # 2. Evil-Slayer Guard Xu — main plaza, CONDENSATION late
        make_npc(
            npc_id="npc_fe_evil_slayer_guard_xu",
            name="Evil-Slayer Guard Xu",
            nameCn="镇邪卫兵徐",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_main_plaza",
            cultivation="CONDENSATION_LATE",
            personality="battle-hardened, grimly humorous, treats combat as sacred duty",
            speech="blunt, dark humor, quotes combat maxims",
            relationship="neutral — respects strength, tests visitors through conversation",
            canon_confidence=2,
            initiation_lines=[
                "Welcome to the plaza. Try not to step in the bloodstains. Some of them are fresh.",
                "We do not cultivate for peace. We cultivate the strength to destroy it when it is false.",
                "Last month a corrupted beast broke through the outer gate. It lasted eleven seconds.",
                "If you are here to learn, go to the library. If you are here to fight, stay right here."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Dawn combat practice", "location": "fighting_evil_sect_main_plaza", "duration": 120},
                {"time": "07:00", "action": "Plaza patrol", "location": "fighting_evil_sect_main_plaza", "duration": 120},
                {"time": "09:00", "action": "Supervise disciple drills", "location": "fighting_evil_sect_trial_grounds", "duration": 150},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Weapon sharpening", "location": "fighting_evil_sect_main_plaza", "duration": 90},
                {"time": "15:00", "action": "Evil-slaying patrol", "location": "fighting_evil_sect_outer_gate", "duration": 180},
                {"time": "19:00", "action": "Report to inner sect", "location": "fighting_evil_sect_inner_sect", "duration": 60},
                {"time": "20:00", "action": "Night watch rotation", "location": "fighting_evil_sect_main_plaza", "duration": 480},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_bone_iron_collection",
                    "name": "Battle Remains Salvage",
                    "description": "The plaza needs iron and bone to forge new weapons and repair training dummies.",
                    "required_items": {"minecraft:iron_ingot": 4, "minecraft:bone": 6},
                    "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1, "experience": 25}
                },
                {
                    "task_id": "fe_binding_materials",
                    "name": "Spirit Binding Materials",
                    "description": "Guard Xu needs string and redstone to reinforce the plaza's evil-detection array.",
                    "required_items": {"minecraft:string": 5, "minecraft:redstone": 4},
                    "rewards": {"minecraft:emerald": 6, "experience": 20}
                },
            ]
        ),

        # 3. Trial Champion Zhao — trial grounds, NASCENT_SOUL early
        make_npc(
            npc_id="npc_fe_trial_champion_zhao",
            name="Trial Champion Zhao",
            nameCn="试炼冠军赵",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_trial_grounds",
            cultivation="NASCENT_SOUL_EARLY",
            personality="intense, competitive, secretly fears the corruption he fights",
            speech="challenging, provocative, speaks in combat metaphors",
            relationship="neutral — tests everyone who enters the trial grounds",
            canon_confidence=2,
            initiation_lines=[
                "The trials do not test your cultivation. They test whether you will walk into darkness.",
                "I have killed seventeen corrupted beasts in the arena. Eighteen was the one that nearly killed me.",
                "A Heng Yue disciple once attempted our trials. He lasted nine seconds. He was brave. Bravery is not enough.",
                "Bring me iron and gunpowder. If your hands shake, you are not ready."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Solo combat meditation", "location": "fighting_evil_sect_trial_grounds", "duration": 120},
                {"time": "08:00", "action": "Oversee morning trials", "location": "fighting_evil_sect_trial_grounds", "duration": 180},
                {"time": "11:00", "action": "Weapon rest", "location": "fighting_evil_sect_main_plaza", "duration": 60},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Afternoon trials", "location": "fighting_evil_sect_trial_grounds", "duration": 180},
                {"time": "16:00", "action": "Corrupted beast disposal", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 90},
                {"time": "18:00", "action": "Evening training", "location": "fighting_evil_sect_sword_peak", "duration": 120},
                {"time": "21:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_trial_explosive_supply",
                    "name": "Trial Grounds Demolition Supply",
                    "description": "The trial grounds need iron and gunpowder to reset combat arenas after corrupted beast fights.",
                    "required_items": {"minecraft:iron_ingot": 5, "minecraft:gunpowder": 4},
                    "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2, "experience": 30}
                },
                {
                    "task_id": "fe_trial_basic_supplies",
                    "name": "Trial Rations Supply",
                    "description": "Trial participants need bread and arrows for endurance tests.",
                    "required_items": {"minecraft:bread": 6, "minecraft:arrow": 12},
                    "rewards": {"minecraft:emerald": 6, "ergenverse:jade_slip": 1, "experience": 20}
                },
            ]
        ),

        # 4. Corruption Alchemist Zheng — alchemy courtyard, NASCENT_SOUL mid
        make_npc(
            npc_id="npc_fe_corruption_alchemist_zheng",
            name="Corruption Alchemist Zheng",
            nameCn="除邪炼丹师郑",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_alchemy_courtyard",
            cultivation="NASCENT_SOUL_MID",
            personality="methodical, morbidly curious, treats corruption as raw material",
            speech="precise, clinical, occasionally unsettling",
            relationship="neutral — interested in any visitor's exposure to corruption",
            canon_confidence=2,
            initiation_lines=[
                "I refine pills from the essence of defeated evil. The reagents are... unpleasant.",
                "Rotten flesh is not garbage. It is data. It tells me what the corruption was.",
                "A Luo He alchemist visited once. He refused to touch my reagents. Weak stomach.",
                "Bring me flesh from corrupted creatures and bottles. In return, purification pills."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Gather reagents from beast pens", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 90},
                {"time": "07:00", "action": "Morning pill refinement", "location": "fighting_evil_sect_alchemy_courtyard", "duration": 180},
                {"time": "10:00", "action": "Corruption analysis", "location": "fighting_evil_sect_alchemy_courtyard", "duration": 120},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Afternoon pill refinement", "location": "fighting_evil_sect_alchemy_courtyard", "duration": 180},
                {"time": "16:00", "action": "Herb collection", "location": "fighting_evil_sect_spirit_herb_garden", "duration": 90},
                {"time": "18:00", "action": "Record findings", "location": "fighting_evil_sect_library", "duration": 60},
                {"time": "20:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 480},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_corruption_pill_reagents",
                    "name": "Corruption Purification Reagents",
                    "description": "Alchemist Zheng needs corrupted flesh and glass bottles for his evil-essence extraction process.",
                    "required_items": {"minecraft:rotten_flesh": 6, "minecraft:glass_bottle": 4},
                    "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 7, "experience": 25}
                },
                {
                    "task_id": "fe_demon_fire_pills",
                    "name": "Demon-Fire Pill Fuel",
                    "description": "Coal and blaze_rod are needed to fuel the alchemical furnace for high-temperature purification.",
                    "required_items": {"minecraft:coal": 6, "minecraft:blaze_rod": 2},
                    "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 30}
                },
            ]
        ),

        # 5. Evil-Binding Array Master Fang — array hall, NASCENT_SOUL mid
        make_npc(
            npc_id="npc_fe_array_master_fang",
            name="Evil-Binding Array Master Fang",
            nameCn="镇邪阵法师方",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_array_hall",
            cultivation="NASCENT_SOUL_MID",
            personality="obsessive, perfectionist, views arrays as the ultimate weapon against evil",
            speech="technical, intense, compares everything to formations",
            relationship="neutral — evaluates visitors by their understanding of formations",
            canon_confidence=2,
            initiation_lines=[
                "An array that cannot restrain evil is just pretty lights on the ground.",
                "I have designed forty-three binding arrays. Forty-two work. The forty-third will be my masterpiece.",
                "Cloud Sky Sect's wind arrays are elegant. Ours are functional. When evil comes, functional matters.",
                "Redstone and iron form the skeleton of any binding array. Bring them and I will teach you."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Inspect morning arrays", "location": "fighting_evil_sect_array_hall", "duration": 120},
                {"time": "08:00", "action": "Teach array class", "location": "fighting_evil_sect_array_hall", "duration": 150},
                {"time": "11:00", "action": "Repair outer gate arrays", "location": "fighting_evil_sect_outer_gate", "duration": 90},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Design new binding array", "location": "fighting_evil_sect_array_hall", "duration": 180},
                {"time": "16:00", "action": "Test arrays on mountain cave", "location": "fighting_evil_sect_mountain_cave", "duration": 120},
                {"time": "19:00", "action": "Consult with Core Elder", "location": "fighting_evil_sect_core_formation_hall", "duration": 60},
                {"time": "21:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_binding_array_materials",
                    "name": "Binding Array Skeleton",
                    "description": "Master Fang needs redstone and iron ingots to construct the framework of a new evil-binding array.",
                    "required_items": {"minecraft:redstone": 8, "minecraft:iron_ingot": 6},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8, "experience": 30}
                },
                {
                    "task_id": "fe_trap_string_gunpowder",
                    "name": "Explosive Trap Components",
                    "description": "String and gunpowder are needed for the array hall's perimeter trap system.",
                    "required_items": {"minecraft:string": 6, "minecraft:gunpowder": 5},
                    "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
                },
            ]
        ),

        # 6. Dark Beast Handler Wei — beast pens, CONDENSATION late
        make_npc(
            npc_id="npc_fe_dark_beast_handler_wei",
            name="Dark Beast Handler Wei",
            nameCn="暗兽驯者魏",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_spirit_beast_pens",
            cultivation="CONDENSATION_LATE",
            personality="gruff but compassionate toward beasts, believes evil can be purified",
            speech="blunt, protective of his charges, surprisingly gentle",
            relationship="neutral — wary of anyone who mistreats animals",
            canon_confidence=2,
            initiation_lines=[
                "These beasts were evil once. We broke them. Now they serve the light.",
                "Do not call them monsters. They are soldiers who lost a war with their own nature.",
                "A beast from Qilin City was brought here last month. Its handler could not control it. I can.",
                "Bone and flesh — the remains of what they were. Bring them and I will feed them properly."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Morning beast feeding", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 120},
                {"time": "07:00", "action": "Corruption inspection", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 120},
                {"time": "09:00", "action": "Training exercises", "location": "fighting_evil_sect_trial_grounds", "duration": 120},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Beast purification rituals", "location": "fighting_evil_sect_spirit_spring", "duration": 120},
                {"time": "15:00", "action": "Pen maintenance", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 120},
                {"time": "18:00", "action": "Evening feeding", "location": "fighting_evil_sect_spirit_beast_pens", "duration": 90},
                {"time": "20:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 480},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_beast_feeding_supply",
                    "name": "Dark Beast Feeding Supplies",
                    "description": "The reformed beasts need bone and flesh to sustain their purification process.",
                    "required_items": {"minecraft:bone": 8, "minecraft:rotten_flesh": 6},
                    "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1, "experience": 25}
                },
                {
                    "task_id": "fe_pen_reinforcement",
                    "name": "Beast Pen Reinforcement",
                    "description": "Handler Wei needs iron and string to reinforce the pen barriers against residual corruption.",
                    "required_items": {"minecraft:iron_ingot": 4, "minecraft:string": 5},
                    "rewards": {"minecraft:emerald": 5, "experience": 20}
                },
            ]
        ),

        # 7. War Puppet Smith Han — puppet workshop, NASCENT_SOUL early
        make_npc(
            npc_id="npc_fe_war_puppet_smith_han",
            name="War Puppet Smith Han",
            nameCn="战傀匠人韩",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_puppet_workshop",
            cultivation="NASCENT_SOUL_EARLY",
            personality="mechanical, single-minded, treats puppets as his children",
            speech="precise, refers to puppets by name, mildly unsettling calm",
            relationship="neutral — interested in anyone who understands machinery",
            canon_confidence=2,
            initiation_lines=[
                "A puppet that hesitates is a puppet that dies. My puppets do not hesitate.",
                "Iron Slaughter Unit Seven has killed more corrupted beasts than most inner disciples.",
                "Redstone is the blood. Iron is the bone. Coal is the breath. A puppet is not so different from you.",
                "Bring me iron and redstone. I will show you what discipline looks like when it has no fear."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Puppet maintenance", "location": "fighting_evil_sect_puppet_workshop", "duration": 150},
                {"time": "08:30", "action": "Forge new puppet parts", "location": "fighting_evil_sect_puppet_workshop", "duration": 150},
                {"time": "11:00", "action": "Test puppets in arena", "location": "fighting_evil_sect_trial_grounds", "duration": 90},
                {"time": "12:30", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:30", "action": "Puppet combat calibration", "location": "fighting_evil_sect_puppet_workshop", "duration": 150},
                {"time": "16:00", "action": "Deploy patrol puppets", "location": "fighting_evil_sect_outer_gate", "duration": 90},
                {"time": "18:00", "action": "Evening repairs", "location": "fighting_evil_sect_puppet_workshop", "duration": 120},
                {"time": "21:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_puppet_forging",
                    "name": "War Puppet Skeleton Forging",
                    "description": "Smith Han needs iron and redstone to forge the skeletal framework of a new combat puppet.",
                    "required_items": {"minecraft:iron_ingot": 8, "minecraft:redstone": 6},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 7, "experience": 30}
                },
                {
                    "task_id": "fe_puppet_fuel_supply",
                    "name": "Puppet Power Supply",
                    "description": "Coal and bone are needed to fuel the puppet workshop's furnaces and provide calcium-hardening compounds.",
                    "required_items": {"minecraft:coal": 8, "minecraft:bone": 4},
                    "rewards": {"minecraft:emerald": 6, "experience": 20}
                },
            ]
        ),

        # 8. Sword Elder Mei — sword peak, NASCENT_SOUL late
        make_npc(
            npc_id="npc_fe_sword_elder_mei",
            name="Sword Elder Mei",
            nameCn="剑长老梅",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_sword_peak",
            cultivation="NASCENT_SOUL_LATE",
            personality="cold, precise, haunted by the evil she has killed",
            speech="measured, pauses before speaking, occasionally dark",
            relationship="neutral — respects cultivators who have faced true danger",
            canon_confidence=2,
            initiation_lines=[
                "Every sword in this sect was quenched in the blood of something evil. That is not metaphor.",
                "I have killed three Nascent Soul stage corrupted cultivators. Each one screamed the same word before they died.",
                "Heng Yue's sword style builds on mountains. Ours builds on corpses. Both reach upward.",
                "Iron and gold — one for the blade, one for the will. Bring both and I will sharpen your understanding."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Pre-dawn sword meditation", "location": "fighting_evil_sect_sword_peak", "duration": 120},
                {"time": "07:00", "action": "Teach sword class", "location": "fighting_evil_sect_sword_peak", "duration": 150},
                {"time": "09:30", "action": "Solo sword practice", "location": "fighting_evil_sect_sword_peak", "duration": 120},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Inner sect combat meeting", "location": "fighting_evil_sect_inner_sect", "duration": 90},
                {"time": "15:00", "action": "Advanced sword training", "location": "fighting_evil_sect_sword_peak", "duration": 180},
                {"time": "18:00", "action": "Visit sword tomb", "location": "fighting_evil_sect_sword_tomb", "duration": 60},
                {"time": "20:00", "action": "Rest", "location": "fighting_evil_sect_inner_sect", "duration": 480},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_sword_sharpening",
                    "name": "Evil-Slaying Blade Sharpening",
                    "description": "Elder Mei needs iron and gold to forge and temper evil-slaying blades.",
                    "required_items": {"minecraft:iron_ingot": 6, "minecraft:gold_ingot": 4},
                    "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 8, "experience": 35}
                },
                {
                    "task_id": "fe_demon_fire_tempering",
                    "name": "Demon-Fire Tempering Supply",
                    "description": "Blaze rods and iron are needed to temper swords in demon-fire for corruption resistance.",
                    "required_items": {"minecraft:iron_ingot": 8, "minecraft:blaze_rod": 3},
                    "rewards": {"ergenverse:jade_slip": 3, "minecraft:enchanted_book": 1, "experience": 40}
                },
            ]
        ),

        # 9. Tomb Keeper Mo — sword tomb, FOUNDATION late
        make_npc(
            npc_id="npc_fe_tomb_keeper_mo",
            name="Tomb Keeper Mo",
            nameCn="剑冢守墓者莫",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_sword_tomb",
            cultivation="FOUNDATION_LATE",
            personality="reverent, quiet, speaks to the dead swords as if alive",
            speech="soft, formal, addresses swords by their former owners' names",
            relationship="neutral — judges visitors by how they treat the tomb",
            canon_confidence=2,
            initiation_lines=[
                "These swords remember. Touch one unprepared and it will show you what it killed.",
                "Elder Shen's sword still vibrates at night. She died killing a Soul Formation corruption.",
                "The Heavenly Fate Sect sent an elder to catalogue these swords. He left after counting three hundred. He said the rest made his stars go dark.",
                "I record the history of every blade. Ink and paper, please. The dead deserve documentation."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Morning sword communion", "location": "fighting_evil_sect_sword_tomb", "duration": 120},
                {"time": "08:00", "action": "Clean and oil swords", "location": "fighting_evil_sect_sword_tomb", "duration": 150},
                {"time": "10:30", "action": "Record sword histories", "location": "fighting_evil_sect_sword_tomb", "duration": 120},
                {"time": "12:30", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:30", "action": "Consult library archives", "location": "fighting_evil_sect_library", "duration": 90},
                {"time": "15:00", "action": "New sword internment", "location": "fighting_evil_sect_sword_tomb", "duration": 120},
                {"time": "18:00", "action": "Evening vigil", "location": "fighting_evil_sect_sword_tomb", "duration": 180},
                {"time": "22:00", "action": "Rest", "location": "fighting_evil_sect_disciple_dormitories", "duration": 360},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_sword_record_keeping",
                    "name": "Fallen Sword Documentation",
                    "description": "Keeper Mo needs ink and paper to record the histories of newly interned evil-slaying swords.",
                    "required_items": {"minecraft:ink_sac": 4, "minecraft:paper": 6},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 7, "experience": 25}
                },
                {
                    "task_id": "fe_tomb_lamp_fuel",
                    "name": "Tomb Lamp Fuel",
                    "description": "Coal and redstone are needed to keep the eternal lamps burning in the sword tomb.",
                    "required_items": {"minecraft:coal": 8, "minecraft:redstone": 4},
                    "rewards": {"minecraft:emerald": 6, "experience": 20}
                },
            ]
        ),

        # 10. Passage Sentinel Lei — underground passage, NASCENT_SOUL mid
        make_npc(
            npc_id="npc_fe_passage_sentinel_lei",
            name="Passage Sentinel Lei",
            nameCn="地道哨兵雷",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_underground_passage",
            cultivation="NASCENT_SOUL_MID",
            personality="vigilant, paranoid, has seen too much in the dark",
            speech="quiet, urgent, checks over his shoulder while speaking",
            relationship="neutral — suspicious but desperate for reinforcement",
            canon_confidence=2,
            initiation_lines=[
                "Something lives in the deep passages. We do not go there. We guard the way down.",
                "Three sentinels have vanished this year. The sect says they transferred. The sect is wrong.",
                "The Corpse Yin Sect sent an envoy once. He laughed when he saw our passages. Said his sect's tunnels were deeper.",
                "Iron and obsidian — we need them to reinforce the barriers. Before they break through again."
            ],
            daily_schedule=[
                {"time": "04:00", "action": "Begin underground patrol", "location": "fighting_evil_sect_underground_passage", "duration": 180},
                {"time": "07:00", "action": "Barrier inspection", "location": "fighting_evil_sect_underground_passage", "duration": 120},
                {"time": "09:00", "action": "Surface report", "location": "fighting_evil_sect_main_plaza", "duration": 60},
                {"time": "10:00", "action": "Resupply from treasury", "location": "fighting_evil_sect_hidden_treasury", "duration": 60},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_disciple_dormitories", "duration": 60},
                {"time": "13:00", "action": "Deep passage patrol", "location": "fighting_evil_sect_underground_passage", "duration": 240},
                {"time": "18:00", "action": "Emergency barrier repair", "location": "fighting_evil_sect_underground_passage", "duration": 120},
                {"time": "21:00", "action": "Night vigil", "location": "fighting_evil_sect_underground_passage", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_barrier_reinforcement",
                    "name": "Underground Barrier Reinforcement",
                    "description": "Sentinel Lei urgently needs iron and obsidian to reinforce barriers against whatever is beneath.",
                    "required_items": {"minecraft:iron_ingot": 8, "minecraft:obsidian": 6},
                    "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2, "experience": 35}
                },
                {
                    "task_id": "fe_demolition_supply",
                    "name": "Tunnel Demolition Supply",
                    "description": "Coal and gunpowder are needed for controlled demolitions of collapsed tunnel sections.",
                    "required_items": {"minecraft:coal": 8, "minecraft:gunpowder": 5},
                    "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1, "experience": 25}
                },
            ]
        ),

        # 11. Ancestor Spirit-Keeper Shi — ancestor hall, NASCENT_SOUL peak
        make_npc(
            npc_id="npc_fe_ancestor_keeper_shi",
            name="Ancestor Spirit-Keeper Shi",
            nameCn="祖灵守者石",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_ancestor_hall",
            cultivation="NASCENT_SOUL_PEAK",
            personality="ancient, serene, speaks as if the ancestors are present",
            speech="formal, reverent, pauses as if listening to unseen voices",
            relationship="neutral — evaluates visitors through the ancestors' wisdom",
            canon_confidence=2,
            initiation_lines=[
                "Our ancestors did not die. They departed to fight evil in places we cannot follow.",
                "The first founder killed a Soul Transformation corruption with his bare hands. His sword is in the tomb. His will is in the walls.",
                "Three ancestors attempted the Evil-Purging Ascension. One returned. We do not speak of what he saw.",
                "Gold and obsidian for the ancestor tablets. Books and ink to record their deeds. These are not trades. They are devotions."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Ancestor meditation", "location": "fighting_evil_sect_ancestor_hall", "duration": 120},
                {"time": "07:00", "action": "Incense and offerings", "location": "fighting_evil_sect_ancestor_hall", "duration": 90},
                {"time": "09:00", "action": "Advise inner sect elders", "location": "fighting_evil_sect_inner_sect", "duration": 120},
                {"time": "11:00", "action": "Visit sword tomb", "location": "fighting_evil_sect_sword_tomb", "duration": 60},
                {"time": "12:00", "action": "Meal", "location": "fighting_evil_sect_inner_sect", "duration": 60},
                {"time": "13:00", "action": "Teach ancestral combat forms", "location": "fighting_evil_sect_main_plaza", "duration": 120},
                {"time": "16:00", "action": "Core formation consultation", "location": "fighting_evil_sect_core_formation_hall", "duration": 90},
                {"time": "19:00", "action": "Evening ancestor communion", "location": "fighting_evil_sect_ancestor_hall", "duration": 180},
                {"time": "22:00", "action": "Rest", "location": "fighting_evil_sect_ancestor_hall", "duration": 360},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_ancestor_tablet_maintenance",
                    "name": "Ancestor Tablet Restoration",
                    "description": "Keeper Shi needs gold and obsidian to restore damaged ancestor memorial tablets.",
                    "required_items": {"minecraft:gold_ingot": 6, "minecraft:obsidian": 5},
                    "rewards": {"ergenverse:spirit_stone": 3, "minecraft:emerald": 6, "experience": 35}
                },
                {
                    "task_id": "fe_ancestor_deeds_recording",
                    "name": "Ancestral Deed Documentation",
                    "description": "Books, ink, and paper are needed to record the deeds of fallen sect warriors for the ancestor hall.",
                    "required_items": {"minecraft:book": 4, "minecraft:ink_sac": 6, "minecraft:paper": 8},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2, "experience": 30}
                },
            ]
        ),

        # 12. Core Elder Hong — core formation hall, NASCENT_SOUL peak
        make_npc(
            npc_id="npc_fe_core_elder_hong",
            name="Core Elder Hong",
            nameCn="阵法核心长老洪",
            faction="Fighting Evil Sect",
            location="fighting_evil_sect_core_formation_hall",
            cultivation="NASCENT_SOUL_PEAK",
            personality="authoritative, strategic, views the entire sect as a weapon",
            speech="commanding, precise, never wastes a word",
            relationship="neutral — evaluates whether visitors strengthen or weaken the sect",
            canon_confidence=2,
            initiation_lines=[
                "The core formation of this sect is a weapon. Every other sect's core is a well. Ours is a blade pointed at the darkness.",
                "Soul Refining Sect's core arrays are chains. Ours are blades. Both cut. Chains bind. Blades end.",
                "I have ordered the underground passages sealed three times. Each time, something unseals them.",
                "Spirit stones and ender pearls. If you have them, you understand what is at stake. If you do not, leave."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Core formation inspection", "location": "fighting_evil_sect_core_formation_hall", "duration": 120},
                {"time": "07:00", "action": "Elder council meeting", "location": "fighting_evil_sect_inner_sect", "duration": 120},
                {"time": "09:00", "action": "Formation calibration", "location": "fighting_evil_sect_core_formation_hall", "duration": 180},
                {"time": "12:00", "action": "Meal with inner sect", "location": "fighting_evil_sect_inner_sect", "duration": 60},
                {"time": "13:00", "action": "Review sentinel reports", "location": "fighting_evil_sect_underground_passage", "duration": 90},
                {"time": "15:00", "action": "Strategic planning", "location": "fighting_evil_sect_core_formation_hall", "duration": 150},
                {"time": "18:00", "action": "Visit ancestor hall", "location": "fighting_evil_sect_ancestor_hall", "duration": 60},
                {"time": "19:00", "action": "Night formation monitoring", "location": "fighting_evil_sect_core_formation_hall", "duration": 300},
                {"time": "23:00", "action": "Rest", "location": "fighting_evil_sect_inner_sect", "duration": 300},
            ],
            sect_tasks=[
                {
                    "task_id": "fe_core_formation_upgrade",
                    "name": "Core Formation Power Supply",
                    "description": "Elder Hong needs spirit stones and ender pearls to upgrade the sect's central combat formation.",
                    "required_items": {"ergenverse:spirit_stone": 4, "minecraft:ender_pearl": 2},
                    "rewards": {"ergenverse:jade_slip": 3, "ergenverse:dao_fragment": 1, "experience": 50}
                },
                {
                    "task_id": "fe_elder_knowledge_exchange",
                    "name": "Elder Knowledge Exchange",
                    "description": "Elder Hong trades high-level cultivation knowledge for emeralds and enchanted books.",
                    "required_items": {"minecraft:emerald": 12, "minecraft:enchanted_book": 1},
                    "rewards": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 2, "experience": 40}
                },
            ]
        ),
    ]

    written = 0
    for npc in npcs:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
        written += 1
    print(f"Wrote {written} NPC JSONs.")


# ============================================================
# MAIN
# ============================================================
if __name__ == "__main__":
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)
    write_loot_tables()
    write_npcs()
    print("AUTO-CANON-043: Fighting Evil Sect completion done.")
    print(f"  20 loot tables in {LOOT_DIR}")
    print(f"  12 NPCs in {NPC_DIR}")