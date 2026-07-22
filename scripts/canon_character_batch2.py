#!/usr/bin/env python3
"""
AUTO-CANON-048: P2 batch 2 — 8 mid-Zhao-arc canon characters.
Duanmu Ji (N90), Gun Lan (N93), Hong Die (N57), Yun Quezi (N59),
Master Cloud Soul (N105), Hunchback Meng (N91), Zhao Xingsha (N140),
Xuan Luo (N28).

All have location, cultivation, personality, but NO initiation_lines,
daily_schedule, or sect_tasks. This script extends existing JSON files.
"""
import json, os

NPC_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

UPDATES = {
    "npc_duanmu_ji.json": {
        "initiation_lines": [
            "You do not belong here. The Soul Refining Sect does not welcome outsiders. We do not welcome insiders either, but at least they know what they are being used for.",
            "Wang Lin was supposed to be a soul refinement ingredient. He became something else entirely. The sect still studies what went wrong. I study it too. From the other side.",
            "Bones and soul fragments. These are the currency of the Soul Refining Sect. If you have them, I may allow you to observe our methods. Observe only. Touch nothing.",
            "The Corpse Yin Sect refines corpses. We refine souls. They say we are different. We are not. We both take what is not ours."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Soul refinement meditation", "location": "soul_refining_sect", "duration": 180},
            {"time": "08:00", "action": "Oversee soul extraction rituals", "location": "soul_refining_sect", "duration": 180},
            {"time": "11:00", "action": "Study forbidden techniques", "location": "soul_refining_sect", "duration": 120},
            {"time": "13:00", "action": "Meal", "location": "soul_refining_sect", "duration": 60},
            {"time": "14:00", "action": "Interrogate captured souls", "location": "soul_refining_sect", "duration": 180},
            {"time": "17:00", "action": "Report to inner sect elders", "location": "soul_refining_sect", "duration": 120},
            {"time": "19:00", "action": "Cultivation", "location": "soul_refining_sect", "duration": 300},
            {"time": "00:00", "action": "Rest", "location": "soul_refining_sect", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "dj_soul_materials",
                "name": "Soul Refinement Materials",
                "description": "Duanmu Ji needs bones and soul fragments for soul refinement experiments.",
                "required_items": {"minecraft:bone": 16, "ergenverse:soul_fragment": 4},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
            {
                "task_id": "dj_forbidden_knowledge",
                "name": "Forbidden Technique Exchange",
                "description": "Duanmu Ji trades soul refinement knowledge for jade slips and spirit stones.",
                "required_items": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 3},
                "rewards": {"minecraft:enchanted_book": 1, "minecraft:experience_bottle": 3, "experience": 35}
            },
        ]
    },
    "npc_gun_lan.json": {
        "initiation_lines": [
            "Ambition is not a sin in the Soul Refining Sect. It is a requirement. Without ambition, you cannot justify what we do. I have no trouble justifying it.",
            "I knew Wang Lin before he became a problem. He was quiet. Unremarkable. That is always the dangerous kind.",
            "The Soul Refining Ancestral Ground is where our power originates. I have been there once. The things I saw... I am still processing them.",
            "Spirit stones and bones. Bring them, and I will teach you the first principle of soul refinement: everything has a soul, and every soul has a price."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn soul cultivation", "location": "soul_refining_sect", "duration": 180},
            {"time": "07:00", "action": "Compete with rival disciples", "location": "soul_refining_sect", "duration": 120},
            {"time": "09:00", "action": "Soul banner crafting", "location": "soul_refining_sect", "duration": 240},
            {"time": "13:00", "action": "Meal and networking", "location": "soul_refining_sect", "duration": 90},
            {"time": "14:30", "action": "Political maneuvering", "location": "soul_refining_sect", "duration": 180},
            {"time": "17:30", "action": "Advanced soul techniques", "location": "soul_refining_sect", "duration": 150},
            {"time": "20:00", "action": "Evening cultivation", "location": "soul_refining_sect", "duration": 300},
            {"time": "01:00", "action": "Rest", "location": "soul_refining_sect", "duration": 180},
        ],
        "sect_tasks": [
            {
                "task_id": "gl_banner_materials",
                "name": "Soul Banner Materials",
                "description": "Gun Lan needs spirit stones and bones to craft soul banners for sect use.",
                "required_items": {"ergenverse:spirit_stone": 3, "minecraft:bone": 12},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:soul_fragment": 2, "experience": 30}
            },
            {
                "task_id": "gl_political_favor",
                "name": "Political Favor Exchange",
                "description": "Gun Lan trades influence and knowledge for emeralds and jade slips.",
                "required_items": {"minecraft:emerald": 14, "ergenverse:jade_slip": 1},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
        ]
    },
    "npc_hong_die.json": {
        "initiation_lines": [
            "The Heavenly Fate Sect sees destiny in stars. I see it in fire. We disagree on methodology. We agree on results. My results are usually more convincing.",
            "Wang Lin and I fought once. He won. I do not admit that to my sect. But I admit it to myself. A cultivator who cannot admit defeat cannot improve.",
            "The Fighting Evil Sect calls us fortune tellers. They are wrong. We do not tell fortunes. We determine them. There is a difference.",
            "Blaze rods and redstone. If you bring them, I will read your fate. Not the fate the stars show. The fate the fire shows. It is less accurate but more honest."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Fire meditation", "location": "heavenly_fate_sect", "duration": 120},
            {"time": "07:00", "action": "Morning star observation", "location": "heavenly_fate_sect", "duration": 90},
            {"time": "09:00", "action": "Fate calculation practice", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "12:00", "action": "Meal", "location": "heavenly_fate_sect", "duration": 60},
            {"time": "13:00", "action": "Teach junior disciples", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "16:00", "action": "Combat training", "location": "heavenly_fate_sect", "duration": 150},
            {"time": "18:30", "action": "Evening star reading", "location": "heavenly_fate_sect", "duration": 120},
            {"time": "21:00", "action": "Night cultivation", "location": "heavenly_fate_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "hd_fate_reading",
                "name": "Fire Fate Reading",
                "description": "Hong Die reads your fate using fire for blaze rods and redstone.",
                "required_items": {"minecraft:blaze_rod": 4, "minecraft:redstone": 16},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 3, "experience": 30}
            },
            {
                "task_id": "hd_combat_knowledge",
                "name": "Combat Experience Exchange",
                "description": "Hong Die trades her combat experience for jade slips and spirit stones.",
                "required_items": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2},
                "rewards": {"minecraft:enchanted_book": 1, "minecraft:experience_bottle": 2, "experience": 30}
            },
        ]
    },
    "npc_yun_quezi.json": {
        "initiation_lines": [
            "The stars do not lie. They are simply complex. Most disciples cannot read them accurately. I can. That is why I am an elder and they are not.",
            "Wang Lin's fate was recorded in our star charts seventeen years before he arrived. The charts said 'anomaly.' They were correct. He is the most disruptive force this planet has seen in millennia.",
            "The Karma Crystal Formation Observer claims karma cannot be predicted by stars. He is partially right. Stars predict the shape of karma. The formation measures its weight. Both are incomplete without the other.",
            "Books and emeralds. The sect's star charts require constant updating. If you provide materials, I will share what the stars reveal about your path."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn star observation", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "07:00", "action": "Senior disciple lecture", "location": "heavenly_fate_sect", "duration": 120},
            {"time": "09:00", "action": "Star chart maintenance", "location": "heavenly_fate_sect", "duration": 240},
            {"time": "13:00", "action": "Meal and study", "location": "heavenly_fate_sect", "duration": 90},
            {"time": "14:30", "action": "Fate calculation for sect matters", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "17:30", "action": "Advise sect leadership", "location": "heavenly_fate_sect", "duration": 120},
            {"time": "20:00", "action": "Night observation", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "23:00", "action": "Rest", "location": "heavenly_fate_sect", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "yq_star_chart_update",
                "name": "Star Chart Materials",
                "description": "Yun Quezi needs books and emeralds to maintain the Heavenly Fate Sect's star charts.",
                "required_items": {"minecraft:book": 8, "minecraft:emerald": 12},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
            {
                "task_id": "yq_fate_calculation",
                "name": "Fate Calculation Request",
                "description": "Yun Quezi performs a fate calculation in exchange for jade slips and experience.",
                "required_items": {"ergenverse:jade_slip": 2, "minecraft:experience_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 35}
            },
        ]
    },
    "npc_master_cloud_soul.json": {
        "location": "soul_refining_sect",
        "initiation_lines": [
            "I am what happens when a soul cultivator reaches the Third Step. I am not a person anymore. I am a phenomenon. The sect does not command me. It asks. Sometimes I answer.",
            "Wang Lin absorbed the Ten Billion Soul Banner. I created it. I do not resent him for taking it. I resent him for understanding it faster than I did.",
            "The Soul Refining Ancestral Ground contains what remains of my humanity. I visit it once per century. Each time, there is less to find.",
            "Spirit stones and soul fragments. If you bring them, I will share knowledge that would drive lesser cultivators mad. Whether you are lesser is for you to discover."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Deep soul meditation", "location": "soul_refining_sect", "duration": 300},
            {"time": "08:00", "action": "Observe sect affairs from seclusion", "location": "soul_refining_sect", "duration": 120},
            {"time": "10:00", "action": "Receive sect reports", "location": "soul_refining_sect", "duration": 60},
            {"time": "11:00", "action": "Soul cloud cultivation", "location": "soul_refining_sect", "duration": 300},
            {"time": "16:00", "action": "Brief audience with elders", "location": "soul_refining_sect", "duration": 60},
            {"time": "17:00", "action": "Walk the ancestral halls", "location": "soul_refining_sect", "duration": 120},
            {"time": "20:00", "action": "Night meditation among soul clouds", "location": "soul_refining_sect", "duration": 360},
            {"time": "02:00", "action": "Rest (minimal)", "location": "soul_refining_sect", "duration": 60},
        ],
        "sect_tasks": [
            {
                "task_id": "mcs_soul_knowledge",
                "name": "Soul Cloud Knowledge",
                "description": "Master Cloud Soul shares Third Step soul cultivation insights for spirit stones and soul fragments.",
                "required_items": {"ergenverse:spirit_stone": 4, "ergenverse:soul_fragment": 4},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:dao_fragment": 1, "experience": 45}
            },
            {
                "task_id": "mcs_ancestral_guidance",
                "name": "Ancestral Ground Guidance",
                "description": "Master Cloud Soul provides guidance for the Soul Refining Ancestral Ground in exchange for jade slips.",
                "required_items": {"ergenverse:jade_slip": 3, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:dao_fragment": 1, "minecraft:experience_bottle": 4, "experience": 40}
            },
        ]
    },
    "npc_hunchback_meng.json": {
        "location": "soul_refining_ancestral",
        "initiation_lines": [
            "Do not look at my back. Look at what I can do. What I can do is far more interesting than my appearance.",
            "I have been inside the Soul Refining Ancestral Ground more times than anyone alive. Sixty-three times. Each time, the ancestors tried to keep me. I am still here. They are not.",
            "Wang Lin entered the Ancestral Ground once. He left with the Ten Billion Soul Banner. I have entered sixty-three times and left with scars. The difference is not talent. It is ruthlessness.",
            "Obsidian and bones. The Ancestral Ground demands payment. If you wish to enter, I can tell you what to bring. Whether you leave is not my concern."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Inspect ancestral ground entrance", "location": "soul_refining_ancestral", "duration": 120},
            {"time": "07:00", "action": "Trade with passing cultivators", "location": "soul_refining_ancestral", "duration": 120},
            {"time": "09:00", "action": "Scavenge ancestral ground perimeter", "location": "soul_refining_ancestral", "duration": 240},
            {"time": "13:00", "action": "Meal and rest", "location": "soul_refining_ancestral", "duration": 90},
            {"time": "14:30", "action": "Study ancestral formations", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "17:30", "action": "Brief ancestral ground entry (shallow)", "location": "soul_refining_ancestral", "duration": 120},
            {"time": "20:00", "action": "Night watch", "location": "soul_refining_ancestral", "duration": 300},
            {"time": "01:00", "action": "Sleep", "location": "soul_refining_ancestral", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "hm_ancestral_supplies",
                "name": "Ancestral Ground Supplies",
                "description": "Hunchback Meng needs obsidian and bones to prepare for his next Ancestral Ground expedition.",
                "required_items": {"minecraft:obsidian": 16, "minecraft:bone": 20},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:soul_fragment": 2, "experience": 30}
            },
            {
                "task_id": "hm_formation_knowledge",
                "name": "Ancestral Formation Knowledge",
                "description": "Meng trades knowledge of Ancestral Ground formations for jade slips and experience.",
                "required_items": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 3},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
        ]
    },
    "npc_zhao_xingsha.json": {
        "initiation_lines": [
            "The Heavenly Fate Sect predicted I would become an elder by forty. I became one by thirty-five. The stars were wrong. I was faster.",
            "Wang Lin's fate thread crossed mine once. The Heavenly Fate Sect recorded it as 'convergence imminent.' It was not imminent. It was a warning. We did not listen.",
            "The Xuan Dao Sect studies the Dao. The Heavenly Fate Sect studies fate. I study power. Fate and Dao are academic. Power is practical.",
            "Spirit stones and jade slips. I am accelerating my cultivation. If you provide materials, I will share what the stars have shown me about the path ahead."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Pre-dawn fate cultivation", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "07:00", "action": "Compete with rival disciples", "location": "heavenly_fate_sect", "duration": 120},
            {"time": "09:00", "action": "Advanced fate calculations", "location": "heavenly_fate_sect", "duration": 240},
            {"time": "13:00", "action": "Meal and political meetings", "location": "heavenly_fate_sect", "duration": 90},
            {"time": "14:30", "action": "Cultivation breakthrough attempts", "location": "heavenly_fate_sect", "duration": 180},
            {"time": "17:30", "action": "Report to Yun Quezi", "location": "heavenly_fate_sect", "duration": 60},
            {"time": "18:30", "action": "Combat training", "location": "heavenly_fate_sect", "duration": 150},
            {"time": "21:00", "action": "Night cultivation", "location": "heavenly_fate_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "zxs_cultivation_acceleration",
                "name": "Cultivation Materials",
                "description": "Zhao Xingsha needs spirit stones and jade slips to accelerate her breakthrough attempts.",
                "required_items": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 2},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 35}
            },
            {
                "task_id": "zxs_fate_intelligence",
                "name": "Fate Intelligence Trade",
                "description": "Zhao Xingsha trades fate calculations for emeralds and experience bottles.",
                "required_items": {"minecraft:emerald": 12, "minecraft:experience_bottle": 3},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
        ]
    },
    "npc_xuan_luo.json": {
        "location": "heng_yue_sect",
        "initiation_lines": [
            "I am Xuan Luo. You may have heard my name. You may not. Both are acceptable. A master's reputation should be earned, not inherited.",
            "Wang Lin was my disciple. I taught him the foundations. He exceeded them. A teacher's greatest success is a student who surpasses them. A teacher's greatest fear is the same thing.",
            "The Heng Yue Sect has forgotten what it means to cultivate the Dao. They cultivate power. They cultivate politics. They cultivate survival. These are not the Dao. The Dao is understanding.",
            "Books and experience. If you seek instruction, bring these. I will teach you what the Heng Yue elders have forgotten: that cultivation is not accumulation. It is revelation."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Deep Dao meditation", "location": "heng_yue_sect", "duration": 240},
            {"time": "07:00", "action": "Morning lecture to core disciples", "location": "heng_yue_sect", "duration": 120},
            {"time": "09:00", "action": "Dao comprehension cultivation", "location": "heng_yue_sect", "duration": 240},
            {"time": "13:00", "action": "Meal with elders", "location": "heng_yue_sect", "duration": 60},
            {"time": "14:00", "action": "Review sect affairs", "location": "heng_yue_sect", "duration": 120},
            {"time": "16:00", "action": "Teach advanced disciples privately", "location": "heng_yue_sect", "duration": 180},
            {"time": "19:00", "action": "Walk the mountain paths", "location": "heng_yue_sect", "duration": 120},
            {"time": "21:00", "action": "Night cultivation", "location": "heng_yue_sect", "duration": 360},
        ],
        "sect_tasks": [
            {
                "task_id": "xl_dao_instruction",
                "name": "Dao Comprehension Teaching",
                "description": "Xuan Luo teaches Dao comprehension to disciples who bring books and experience.",
                "required_items": {"minecraft:book": 6, "minecraft:experience_bottle": 4},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2, "experience": 40}
            },
            {
                "task_id": "xl_elder_guidance",
                "name": "Sect Guidance Request",
                "description": "Xuan Luo provides guidance on sect affairs and cultivation for jade slips and spirit stones.",
                "required_items": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:dao_fragment": 1, "minecraft:experience_bottle": 3, "experience": 40}
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

        changes = []

        if "location" in update_data:
            old = data.get("location", "")
            data["location"] = update_data["location"]
            if old != update_data["location"]:
                changes.append(f"loc: {old} -> {update_data['location']}")

        if "initiation_lines" in update_data and not data.get("initiation_lines"):
            data["initiation_lines"] = update_data["initiation_lines"]
            changes.append(f"+{len(update_data['initiation_lines'])} init")

        if "daily_schedule" in update_data and not data.get("daily_schedule"):
            data["daily_schedule"] = update_data["daily_schedule"]
            changes.append(f"+{len(update_data['daily_schedule'])} sched")

        if "sect_tasks" in update_data and not data.get("sect_tasks"):
            data["sect_tasks"] = update_data["sect_tasks"]
            changes.append(f"+{len(update_data['sect_tasks'])} tasks")

        if changes:
            with open(filepath, 'w', encoding='utf-8') as f:
                json.dump(data, f, indent=2, ensure_ascii=False)
            print(f"  {data.get('name',filename)}: {'; '.join(changes)}")
            updated += 1
        else:
            print(f"  {data.get('name',filename)}: no changes")

    print(f"\nUpdated: {updated}/{len(UPDATES)}")


if __name__ == "__main__":
    main()