#!/usr/bin/env python3
"""
AUTO-CANON-047: P2 — Add initiation_lines, daily_schedule, and sect_tasks
to 8 critical canon characters that currently lack them.

Characters (all canon, derivation_type A/B):
1. Li Muwan (N17) → heng_yue_sect — Wang Lin's love interest, alchemist
2. Zhou Ru (N10) → heng_yue_sect — Wang Lin's disciple
3. Qing Shui (N30) → heng_yue_sect — mentor, Third Step
4. Situ Nan (N20) → vermilion_bird_divine_sect — sworn brother, Divine Emperor
5. Daoist Water (N102) → heng_yue_sect — elder/antagonist
6. Xu Liguo (N62) → heng_yue_sect — sword spirit servant
7. Gao Qiming (N87) → tian_shui_city — needs initiation_lines only (has schedule+tasks)
8. Ling'er (N162) → heng_yue_sect — child cultivator

Preserves all existing fields. Adds: initiation_lines, daily_schedule, sect_tasks.
Updates location from variable/unknown to canon-accurate Zhao Country locations.
"""

import json
import os

NPC_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

# ── Initiation lines, schedules, tasks for each character ──

UPDATES = {
    "npc_li_muwan.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "You must be new to the alchemy hall. I am Li Muwan. If you bring me herbs and reagents, I can refine pills. The Heng Yue Sect's alchemy has declined in recent years. I intend to restore it.",
            "Wang Lin once sat where you stand. He was not the most talented disciple. He was the most determined. That matters more than talent.",
            "The Soul Refining Sect's alchemists use souls as reagents. I use spirit herbs. The difference is not efficiency. It is what you are willing to sacrifice.",
            "Bread and wheat for sustenance, or coal and bottles if you seek refined pills. Alchemy requires both patience and materials."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Pre-dawn herb preparation", "location": "heng_yue_sect", "duration": 120},
            {"time": "07:00", "action": "Alchemy lecture for junior disciples", "location": "heng_yue_sect", "duration": 120},
            {"time": "09:00", "action": "Pill refinement", "location": "heng_yue_sect", "duration": 240},
            {"time": "13:00", "action": "Meal and herb cataloging", "location": "heng_yue_sect", "duration": 90},
            {"time": "14:30", "action": "Advanced alchemy research", "location": "heng_yue_sect", "duration": 180},
            {"time": "17:30", "action": "Herb garden inspection", "location": "heng_yue_sect", "duration": 120},
            {"time": "20:00", "action": "Evening cultivation", "location": "heng_yue_sect", "duration": 300},
            {"time": "01:00", "action": "Rest", "location": "heng_yue_sect", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "lmw_herb_refinement",
                "name": "Herb Pill Refinement",
                "description": "Li Muwan needs bread and wheat to sustain herself during long alchemy sessions, and coal and bottles for the refinement furnace.",
                "required_items": {"minecraft:bread": 8, "minecraft:wheat": 12, "minecraft:coal": 8, "minecraft:glass_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 3, "experience": 30}
            },
            {
                "task_id": "lmw_reagent_collection",
                "name": "Alchemy Reagent Supply",
                "description": "Li Muwan needs paper and ink to record alchemy formulas, and emeralds to purchase rare reagents from traveling merchants.",
                "required_items": {"minecraft:paper": 16, "minecraft:ink_sac": 8, "minecraft:emerald": 10},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
        ]
    },
    "npc_zhou_ru.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "I am Zhou Ru. My master is away. He left instructions that I am to train the junior disciples in his absence. Do not underestimate me because of my appearance.",
            "The Xuan Dao Sect once sent an elder to recruit me. I declined. My master's teachings are not transferable.",
            "Spirit stones and books. If you bring them, I will share what my master taught me about the path of cultivation.",
            "The Heng Yue Sect's elders worry about my cultivation speed. They should worry about their own."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn cultivation", "location": "heng_yue_sect", "duration": 180},
            {"time": "07:00", "action": "Sword practice", "location": "heng_yue_sect", "duration": 120},
            {"time": "09:00", "action": "Train junior disciples", "location": "heng_yue_sect", "duration": 180},
            {"time": "12:00", "action": "Meal", "location": "heng_yue_sect", "duration": 60},
            {"time": "13:00", "action": "Study ancient texts", "location": "heng_yue_sect", "duration": 180},
            {"time": "16:00", "action": "Solo cultivation", "location": "heng_yue_sect", "duration": 240},
            {"time": "20:00", "action": "Evening meditation", "location": "heng_yue_sect", "duration": 300},
            {"time": "01:00", "action": "Rest", "location": "heng_yue_sect", "duration": 180},
        ],
        "sect_tasks": [
            {
                "task_id": "zr_disciple_training",
                "name": "Disciple Training Materials",
                "description": "Zhou Ru needs spirit stones and books to train junior disciples in her master's absence.",
                "required_items": {"ergenverse:spirit_stone": 2, "minecraft:book": 4},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 2, "experience": 30}
            },
            {
                "task_id": "zr_cultivation_aid",
                "name": "Cultivation Support",
                "description": "Zhou Ru needs emeralds and experience bottles to accelerate her cultivation while her master is away.",
                "required_items": {"minecraft:emerald": 12, "minecraft:experience_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 1, "experience": 25}
            },
        ]
    },
    "npc_qing_shui.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "The water does not resist. It does not fight. It simply flows around the obstacle and continues. This is the Dao I teach. Most disciples do not understand it for decades.",
            "Wang Lin understood it in hours. That is why I gave him the Slaughter Sword. Not because he was strong. Because he understood that strength and water are not opposites.",
            "The Corpse Yin Sect cultivates death. The Soul Refining Sect cultivates souls. I cultivate understanding. All three paths lead to the same mountain. The view is different from each.",
            "Books and experience. If you wish to learn, bring these. I will teach you what water has taught me."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Deep meditation by the spirit spring", "location": "heng_yue_sect", "duration": 240},
            {"time": "07:00", "action": "Morning lecture on the Dao", "location": "heng_yue_sect", "duration": 120},
            {"time": "09:00", "action": "Observe sect training", "location": "heng_yue_sect", "duration": 180},
            {"time": "12:00", "action": "Meal with elders", "location": "heng_yue_sect", "duration": 60},
            {"time": "13:00", "action": "Private cultivation", "location": "heng_yue_sect", "duration": 300},
            {"time": "18:00", "action": "Walk the mountain paths", "location": "heng_yue_sect", "duration": 120},
            {"time": "20:00", "action": "Night meditation under stars", "location": "heng_yue_sect", "duration": 300},
            {"time": "01:00", "action": "Rest", "location": "heng_yue_sect", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "qs_dao_teaching",
                "name": "Dao Instruction Materials",
                "description": "Qing Shui needs books and experience bottles to prepare Dao instruction for promising disciples.",
                "required_items": {"minecraft:book": 6, "minecraft:experience_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 35}
            },
            {
                "task_id": "qs_spring_maintenance",
                "name": "Spirit Spring Tending",
                "description": "The spirit spring requires coal and glowstone to maintain its spiritual purity. Qing Shui entrusts this to capable disciples.",
                "required_items": {"minecraft:coal": 16, "minecraft:glowstone": 8},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:dao_fragment": 1, "experience": 40}
            },
        ]
    },
    "npc_situ_nan.json": {
        "initiation_lines": [
            "Ha! Another cultivator! You look like you can fight. Good. I like people who can fight. Come, sit. Tell me your name. I will tell you whether it is a good name for surviving.",
            "I have lived longer than this sect. I have died and come back. Death is overrated. What matters is having a good friend to pull you back. Wang Lin is mine.",
            "The Vermilion Bird Divine Sect thinks it owns me. The Heavenly Fate Sect thinks it can predict me. The Soul Refining Sect thinks it can capture me. They are all wrong. I am Situ Nan.",
            "Spirit stones and jade slips. If you have them, I will share stories of battles that shook the planet. If you do not, I will share them anyway. I am generous like that."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Wake and stretch cultivation", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "08:00", "action": "Patrol sect grounds (loudly)", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "11:00", "action": "Train in combat forms", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "13:00", "action": "Meal and boast about past battles", "location": "vermilion_bird_divine_sect", "duration": 90},
            {"time": "14:30", "action": "Cultivation and recovery", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "17:30", "action": "Evening storytelling for younger disciples", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "20:00", "action": "Night meditation", "location": "vermilion_bird_divine_sect", "duration": 300},
            {"time": "01:00", "action": "Rest", "location": "vermilion_bird_divine_sect", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "sn_battle_stories",
                "name": "Battle Knowledge Exchange",
                "description": "Situ Nan trades battle experience for spirit stones and jade slips. His knowledge spans millennia.",
                "required_items": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 1},
                "rewards": {"minecraft:enchanted_book": 1, "minecraft:experience_bottle": 3, "experience": 40}
            },
            {
                "task_id": "sn_combat_training",
                "name": "Combat Training Request",
                "description": "Situ Nan offers combat guidance in exchange for emeralds and experience. His training is legendary.",
                "required_items": {"minecraft:emerald": 16, "minecraft:experience_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2, "experience": 45}
            },
        ]
    },
    "npc_daoist_water.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "Water finds the lowest point. Power finds the highest. I have chosen water. Most cultivators choose power. They are wrong, but they do not know it yet.",
            "The Heng Yue Sect's elders fear me. Good. Fear is a form of respect. They should also fear the river. It does not fear them.",
            "The Karma Crystal Formation and I share an understanding. Cause and effect are like water. They flow. They erode. They drown. Bring me what I need, and I will teach you to read the flow.",
            "Books and glowstone. If you want to understand the water Dao, these are the beginning. The end is a long way from here."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn water cultivation", "location": "heng_yue_sect", "duration": 180},
            {"time": "07:00", "action": "Observe disciples' training", "location": "heng_yue_sect", "duration": 120},
            {"time": "09:00", "action": "Deep cultivation in meditation chamber", "location": "heng_yue_sect", "duration": 300},
            {"time": "14:00", "action": "Walk the mountain streams", "location": "heng_yue_sect", "duration": 120},
            {"time": "16:00", "action": "Teach advanced disciples (reluctantly)", "location": "heng_yue_sect", "duration": 120},
            {"time": "18:00", "action": "Evening cultivation by water", "location": "heng_yue_sect", "duration": 180},
            {"time": "21:00", "action": "Night meditation", "location": "heng_yue_sect", "duration": 300},
            {"time": "02:00", "action": "Rest", "location": "heng_yue_sect", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "dw_water_dao_teaching",
                "name": "Water Dao Instruction",
                "description": "Daoist Water requires books and glowstone to teach the water Dao to worthy disciples.",
                "required_items": {"minecraft:book": 6, "minecraft:glowstone": 10},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 35}
            },
            {
                "task_id": "dw_formation_study",
                "name": "Formation Analysis",
                "description": "Daoist Water studies formations from the Xuan Dao Sect and Karma Crystal. He needs emeralds and ink.",
                "required_items": {"minecraft:emerald": 14, "minecraft:ink_sac": 8},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:dao_fragment": 1, "experience": 40}
            },
        ]
    },
    "npc_xu_liguo.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "Master! I mean — you are not my master. My master is Wang Lin. But you have a similar... presence. Are you here to claim the sword? It is not ready. I am not ready. We are both working on it.",
            "I was once a cultivator of the Soul Refining Sect. Then I was a sword. Then I was a sword spirit. The Soul Refining Sect makes many things. Swords. Spirits. Mistakes. I was all three.",
            "Iron and coal. If you bring them, I will maintain the sword in my master's absence. He left instructions. I follow them. That is what sword spirits do.",
            "The Corpse Yin Sect's envoy asked about me once. He wanted to know if a sword spirit could be extracted. I told him to try. He did not."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Sword maintenance and polishing", "location": "heng_yue_sect", "duration": 180},
            {"time": "08:00", "action": "Patrol master's quarters", "location": "heng_yue_sect", "duration": 120},
            {"time": "10:00", "action": "Cultivation within the sword", "location": "heng_yue_sect", "duration": 240},
            {"time": "14:00", "action": "Gather intelligence on sect affairs", "location": "heng_yue_sect", "duration": 120},
            {"time": "16:00", "action": "Sword training exercises", "location": "heng_yue_sect", "duration": 180},
            {"time": "19:00", "action": "Report to master's quarters", "location": "heng_yue_sect", "duration": 60},
            {"time": "20:00", "action": "Guard duty", "location": "heng_yue_sect", "duration": 360},
            {"time": "02:00", "action": "Dormant state", "location": "heng_yue_sect", "duration": 180},
        ],
        "sect_tasks": [
            {
                "task_id": "xlg_sword_maintenance",
                "name": "Sword Maintenance Supplies",
                "description": "Xu Liguo needs iron and coal to maintain his master's weapons in his absence.",
                "required_items": {"minecraft:iron_ingot": 12, "minecraft:coal": 10},
                "rewards": {"minecraft:emerald": 10, "minecraft:experience_bottle": 2, "experience": 20}
            },
            {
                "task_id": "xlg_intelligence_gathering",
                "name": "Sect Intelligence",
                "description": "Xu Liguo trades information for emeralds and books. He knows the sect's secrets.",
                "required_items": {"minecraft:emerald": 10, "minecraft:book": 3},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 8, "experience": 25}
            },
        ]
    },
    "npc_gao_qiming.json": {
        # Only add initiation_lines — already has schedule + tasks
        "initiation_lines": [
            "Sit. I have been expecting someone. The stars told me. They always tell me. The question is whether I should tell you what they said.",
            "The Heavenly Fate Sect claims a monopoly on divination. They are wrong. My methods are older. Their methods are louder. Louder is not better.",
            "Dark omens gather over Tian Shui City. A Soul Refining Sect envoy passed through last month. He did not like what I told him. The feeling was mutual.",
            "Emeralds and paper, or coal and ink. Bring them, and I will read what the heavens permit."
        ],
    },
    "npc_ling_er.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "Hello! Are you a disciple too? I am still learning. My cultivation is not very high yet, but Teacher says I have potential. Teacher says many things. I try to remember all of them.",
            "Wang Lin found me when I was very small. I do not remember my parents. I remember the mountains. The mountains were safe. Wang Lin made them safe.",
            "Bread and wheat! I get hungry when I cultivate. Teacher says cultivation burns energy. I believe her. I am always hungry.",
            "The Heng Yue Sect elders are very serious. I try not to make them more serious. It does not always work."
        ],
        "daily_schedule": [
            {"time": "06:00", "action": "Morning cultivation practice", "location": "heng_yue_sect", "duration": 120},
            {"time": "08:00", "action": "Attend elder's lecture", "location": "heng_yue_sect", "duration": 120},
            {"time": "10:00", "action": "Play in the sect gardens", "location": "heng_yue_sect", "duration": 180},
            {"time": "13:00", "action": "Lunch", "location": "heng_yue_sect", "duration": 60},
            {"time": "14:00", "action": "Study with teacher", "location": "heng_yue_sect", "duration": 180},
            {"time": "17:00", "action": "Explore sect grounds", "location": "heng_yue_sect", "duration": 120},
            {"time": "19:00", "action": "Dinner and stories", "location": "heng_yue_sect", "duration": 90},
            {"time": "21:00", "action": "Sleep", "location": "heng_yue_sect", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "le_cultivation_snacks",
                "name": "Cultivation Energy Snacks",
                "description": "Ling'er gets hungry during cultivation. She needs bread and wheat for energy.",
                "required_items": {"minecraft:bread": 12, "minecraft:wheat": 16},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1, "experience": 10}
            },
            {
                "task_id": "le_study_materials",
                "name": "Study Materials",
                "description": "Ling'er needs books and emeralds for her studies with her teacher.",
                "required_items": {"minecraft:book": 4, "minecraft:emerald": 8},
                "rewards": {"minecraft:emerald": 6, "minecraft:book": 2, "experience": 15}
            },
        ]
    },
}


def main():
    updated = 0
    for filename, update_data in UPDATES.items():
        filepath = os.path.join(NPC_DIR, filename)
        if not os.path.exists(filepath):
            print(f"  MISSING: {filename}")
            continue

        with open(filepath, 'r', encoding='utf-8') as f:
            data = json.load(f)

        # Track what changed
        changes = []

        # Update location if specified
        if "location" in update_data:
            old_loc = data.get("location", "")
            data["location"] = update_data["location"]
            if old_loc != update_data["location"]:
                changes.append(f"location: {old_loc} → {update_data['location']}")

        # Add initiation_lines
        if "initiation_lines" in update_data and not data.get("initiation_lines"):
            data["initiation_lines"] = update_data["initiation_lines"]
            changes.append("added 4 initiation_lines")

        # Add daily_schedule
        if "daily_schedule" in update_data and not data.get("daily_schedule"):
            data["daily_schedule"] = update_data["daily_schedule"]
            changes.append(f"added {len(update_data['daily_schedule'])} schedule entries")

        # Add sect_tasks (only if not already present with proper format)
        if "sect_tasks" in update_data and not data.get("sect_tasks"):
            data["sect_tasks"] = update_data["sect_tasks"]
            changes.append(f"added {len(update_data['sect_tasks'])} tasks")
        elif "sect_tasks" in update_data and data.get("sect_tasks"):
            # Gao Qiming has tasks but in old format — add initiation_lines only
            pass

        if changes:
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            name = data.get("name", filename)
            print(f"  Updated {name} ({filename}): {'; '.join(changes)}")
            updated += 1
        else:
            name = data.get("name", filename)
            print(f"  No changes needed: {name} ({filename})")

    print(f"\nTotal NPCs updated: {updated}/{len(UPDATES)}")


if __name__ == "__main__":
    main()