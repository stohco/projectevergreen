#!/usr/bin/env python3
"""
AUTO-CANON-050: P2 batch 4 — 8 A-tier canon characters.
2nd Gen Vermilion Bird (N52), Zhou Wutai (N58), Nian Tian (N161),
Master Void (N97), Blood Ancestor (N98), Yao Xixue (N99),
Ta Shan (N44), Lei Ji (N46).

Special cases:
- Nian Tian: deceased, remnant soul at soul_refining_ancestral. Interaction flags flipped.
- Ta Shan: minimal speech (silent guardian). Pinned to soul_refining_ancestral.
- Lei Ji: beast, non-verbal. Location pinned to soul_refining_sect. Beast-appropriate tasks.
- Master Void: pinned from unknown to foreign_void_rift.
"""
import json, os

NPC_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

UPDATES = {
    "npc_2nd_gen_vermilion.json": {
        "initiation_lines": [
            "You have entered the Fallen Land. Few survive the trial. Fewer still earn my attention. You have earned it. I am the Second Generation Vermilion Bird Divine Emperor. This trial will test not your strength, but your resolve.",
            "Wang Lin passed this trial. He did not pass because he was the strongest. He passed because he refused to stop. Strength without resolve is fire without fuel. I have seen both fail.",
            "I gave Wang Lin dragon blood and taught him the Dao of Strength. He asked me to look after Zhong Dahong. A small request from a man who had just proven he deserved anything he asked for.",
            "Blaze rods and dragon breath. If you wish to attempt the Young Emperor trial, you will need fire-aspected materials to fuel the array. Bring them and I will assess your worthiness."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Void Flame meditation", "location": "fallen_land", "duration": 300},
            {"time": "08:00", "action": "Assess trial candidates", "location": "fallen_land", "duration": 180},
            {"time": "11:00", "action": "Maintain trial arrays", "location": "fallen_land", "duration": 180},
            {"time": "14:00", "action": "Cultivation at Divine Emperor level", "location": "fallen_land", "duration": 240},
            {"time": "18:00", "action": "Review trial records of past candidates", "location": "fallen_land", "duration": 120},
            {"time": "20:00", "action": "Commune with the Vermilion Bird flame remnant", "location": "fallen_land", "duration": 180},
            {"time": "23:00", "action": "Patrol Fallen Land perimeter", "location": "fallen_land", "duration": 180},
            {"time": "02:00", "action": "Rest in imperial chamber", "location": "fallen_land", "duration": 60},
        ],
        "sect_tasks": [
            {
                "task_id": "v2_trial_materials",
                "name": "Young Emperor Trial Preparation",
                "description": "The 2nd Generation Divine Emperor needs blaze rods and experience bottles to prepare the Young Emperor trial array for the next candidate.",
                "required_items": {"minecraft:blaze_rod": 12, "minecraft:experience_bottle": 6},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 3, "ergenverse:dao_fragment": 1, "experience": 55}
            },
            {
                "task_id": "v2_dao_strength",
                "name": "Dao of Strength Instruction",
                "description": "The 2nd Generation teaches fragments of the Dao of Strength to worthy cultivators. Bring iron and books to demonstrate your foundation.",
                "required_items": {"minecraft:iron_ingot": 20, "minecraft:book": 4},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 50}
            },
        ]
    },
    "npc_zhou_wutai.json": {
        "initiation_lines": [
            "The burden of the Divine Emperor is not power. It is watching everyone you protect grow old and die while you remain. I carried that burden before Wang Lin. He carries it now. The weight does not decrease with generations.",
            "The Trial of Heaven is not a test of cultivation. It is a test of whether the universe accepts you. I stood before it once. It accepted me. When Wang Lin stood before it, it hesitated. That hesitation should tell you everything about who he is.",
            "The Heavenly Fate Sect believes destiny is written in stars. The Soul Refining Sect believes power comes from suffering. The Vermilion Bird Divine Sect believes that fire purifies all things. We are all partially right. None of us are fully right.",
            "Gold and blaze powder. The Divine Emperor's seal requires constant energy to maintain. If you contribute, you earn the right to observe the Trial of Heaven preparation. Whether you survive the observation is another matter."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Imperial seal meditation", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "07:00", "action": "Receive reports from all sect branches", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "10:00", "action": "Cultivation at Wending realm", "location": "vermilion_bird_divine_sect", "duration": 240},
            {"time": "14:00", "action": "Advise inner disciples on cultivation", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "17:00", "action": "Prepare Trial of Heaven logistics", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "19:00", "action": "Diplomatic correspondence with other sects", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "21:00", "action": "Night cultivation and reflection", "location": "vermilion_bird_divine_sect", "duration": 300},
            {"time": "02:00", "action": "Rest", "location": "vermilion_bird_divine_sect", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "zw_imperial_seal",
                "name": "Imperial Seal Maintenance",
                "description": "Zhou Wutai needs gold and blaze powder to maintain the Divine Emperor's seal that protects the Vermilion Bird Divine Sect.",
                "required_items": {"minecraft:gold_ingot": 16, "minecraft:blaze_powder": 10},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 4, "minecraft:enchanted_book": 1, "experience": 50}
            },
            {
                "task_id": "zw_trial_records",
                "name": "Trial of Heaven Records",
                "description": "Zhou Wutai collects historical records of past Trial of Heaven attempts. Bring jade slips and books documenting cultivation breakthroughs.",
                "required_items": {"ergenverse:jade_slip": 2, "minecraft:book": 5, "minecraft:emerald": 8},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 45}
            },
        ]
    },
    "npc_nian_tian.json": {
        "location": "soul_refining_ancestral",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": True,
        "teaching_available": True,
        "initiation_lines": [
            "You can see me? My body died protecting Du Tian. My soul... fragments of it remain here, bound to the Ancestral Ground. I am a memory that refuses to fade.",
            "Du Tian was my sworn brother. We shared the Ancient Clan bloodline. When the Soul Refining Sect was destroyed, I stood between him and death. Death took me instead. I do not regret it. A brother's duty is not negotiable.",
            "Wang Lin inherited Du Tian's legacy. He carries the Ten Billion Soul Banner now. If you serve him, you serve the continuation of what Du Tian and I built. Remember that loyalty is not given. It is earned through sacrifice.",
            "My soul fragments retain some knowledge. If you bring me soul fragments and spirit stones, I can share what I remember of the Ancient Clan bloodline arts. It is not much. But it is all I have left to give."
        ],
        "daily_schedule": [
            {"time": "00:00", "action": "Drift through Ancestral Ground as remnant soul", "location": "soul_refining_ancestral", "duration": 480},
            {"time": "08:00", "action": "Linger near Du Tian's memorial", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "11:00", "action": "Resonate with Ten Billion Soul Banner fragments", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "14:00", "action": "Faintly manifest to speak with visitors", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "17:00", "action": "Drift back toward deepest chamber", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "20:00", "action": "Flicker between consciousness and void", "location": "soul_refining_ancestral", "duration": 360},
            {"time": "02:00", "action": "Brief moment of clarity near ancient altar", "location": "soul_refining_ancestral", "duration": 120},
            {"time": "04:00", "action": "Dissolve into scattered fragments", "location": "soul_refining_ancestral", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "nt_soul_fragments",
                "name": "Remnant Soul Stabilization",
                "description": "Nian Tian's remnant soul needs soul fragments and spirit stones to maintain coherence. In return, he shares Ancient Clan bloodline knowledge.",
                "required_items": {"ergenverse:soul_fragment": 8, "ergenverse:spirit_stone": 4},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "ergenverse:dao_fragment": 1, "experience": 60}
            },
            {
                "task_id": "nt_ancient_memory",
                "name": "Ancient Clan Memory Recovery",
                "description": "Nian Tian attempts to recover fragmented memories of the Ancient Clan. Bring books and jade slips to help reconstruct his knowledge.",
                "required_items": {"minecraft:book": 6, "ergenverse:jade_slip": 2},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 2, "experience": 50}
            },
        ]
    },
    "npc_master_void.json": {
        "location": "foreign_void_rift",
        "initiation_lines": [
            "You stand at the edge of the void. Most who reach this point fall in. You have not. That is either wisdom or ignorance. I have not yet decided which.",
            "The void does not destroy. It removes. Everything that enters ceases to exist not because it is broken, but because it was never there. I learned this truth and became its master. Or perhaps its servant. The distinction is unclear.",
            "Wang Lin once stood where you stand. He did not fall in either. He reached into the void and pulled something out. That something should not have existed. The void has not forgiven him for that theft.",
            "Endstone and obsidian. These are the only materials that anchor existence near the void. Bring them and I will teach you the first principle of void manipulation: to exist in nothing, you must first become nothing."
        ],
        "daily_schedule": [
            {"time": "02:00", "action": "Void meditation at the rift's edge", "location": "foreign_void_rift", "duration": 300},
            {"time": "07:00", "action": "Study rift fluctuations", "location": "foreign_void_rift", "duration": 180},
            {"time": "10:00", "action": "Practice void manipulation techniques", "location": "foreign_void_rift", "duration": 240},
            {"time": "14:00", "action": "Seal expanding void tears", "location": "foreign_void_rift", "duration": 180},
            {"time": "17:00", "action": "Cultivation at Nirvana Shatterer level", "location": "foreign_void_rift", "duration": 180},
            {"time": "20:00", "action": "Observe entities that emerge from the void", "location": "foreign_void_rift", "duration": 180},
            {"time": "23:00", "action": "Deep void communion", "location": "foreign_void_rift", "duration": 180},
            {"time": "02:00", "action": "Rest in void-anchored chamber", "location": "foreign_void_rift", "duration": 0},
        ],
        "sect_tasks": [
            {
                "task_id": "mv_void_anchoring",
                "name": "Void Rift Anchoring Materials",
                "description": "Master Void needs endstone and obsidian to anchor the void rift and prevent it from expanding into surrounding territory.",
                "required_items": {"minecraft:end_stone": 20, "minecraft:obsidian": 16},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 3, "experience": 50}
            },
            {
                "task_id": "mv_void_knowledge",
                "name": "Void Manipulation Exchange",
                "description": "Master Void trades void manipulation knowledge for jade slips containing information about other realms and dimensions.",
                "required_items": {"ergenverse:jade_slip": 3, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "ergenverse:dao_fragment": 1, "experience": 55}
            },
        ]
    },
    "npc_blood_ancestor.json": {
        "initiation_lines": [
            "Blood is the original cultivation resource. Before spirit stones, before herbs, before jade slips — there was blood. The Yao Family remembers this. The Yao Family practices this. The Yao Family IS this.",
            "Wang Lin killed me. Or rather, he killed this body. The Blood Sea reforms. The Blood Ancestor returns. Death is a temporary inconvenience for one who has mastered the art of blood reincarnation.",
            "The Soul Refining Sect refines souls. The Corpse Yin Sect animates corpses. Both are crude imitations of what the Blood Sea accomplishes naturally. Blood carries memory, power, lineage. Everything else is a shadow of blood's truth.",
            "Redstone and bone meal. The Blood Sea requires these to sustain its territorial expansion. Bring them, and I will teach you the first form of Blood Reincarnation. What you do with that knowledge is your own affair."
        ],
        "daily_schedule": [
            {"time": "01:00", "action": "Blood Sea meditation", "location": "blood_sea", "duration": 300},
            {"time": "06:00", "action": "Absorb blood energy from the sea", "location": "blood_sea", "duration": 180},
            {"time": "09:00", "action": "Direct Yao Family operations", "location": "blood_sea", "duration": 240},
            {"time": "13:00", "action": "Cultivation at peak Third Step", "location": "blood_sea", "duration": 180},
            {"time": "16:00", "action": "Expand Blood Sea territory", "location": "blood_sea", "duration": 180},
            {"time": "19:00", "action": "Receive reports from Yao Xixue", "location": "blood_sea", "duration": 120},
            {"time": "21:00", "action": "Blood reincarnation body preparation", "location": "blood_sea", "duration": 180},
            {"time": "00:00", "action": "Rest in blood cocoon", "location": "blood_sea", "duration": 60},
        ],
        "sect_tasks": [
            {
                "task_id": "ba_blood_sea_materials",
                "name": "Blood Sea Expansion Materials",
                "description": "The Blood Ancestor needs redstone and bone meal to sustain and expand the Blood Sea's territorial boundary.",
                "required_items": {"minecraft:redstone": 32, "minecraft:bone_meal": 24},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 2, "ergenverse:spirit_stone": 3, "experience": 55}
            },
            {
                "task_id": "ba_blood_reincarnation",
                "name": "Blood Reincarnation Knowledge",
                "description": "The Blood Ancestor trades forbidden Blood Reincarnation arts in exchange for soul fragments and jade slips documenting other cultivation methods.",
                "required_items": {"ergenverse:soul_fragment": 6, "ergenverse:jade_slip": 3},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 2, "ergenverse:dao_fragment": 1, "experience": 60}
            },
        ]
    },
    "npc_yao_xixue.json": {
        "initiation_lines": [
            "The Yao Family does not negotiate from weakness. We negotiate from the Blood Sea, which stretches further than your eye can see. If you want something from us, first understand that you are standing on our territory.",
            "Wang Lin is a problem that the Blood Ancestor will solve. Personally, I find him fascinating. A cultivator who kills a Third Step being and simply walks away as if it were Tuesday. That is either supreme confidence or supreme ignorance.",
            "The Blood Ancestor trusts me with the family's external affairs. I handle diplomacy, intelligence, and when diplomacy fails, other methods. The Soul Refining Sect calls us monsters. We call them amateurs.",
            "Emeralds and gold. The Yao Family's external operations require funding. In exchange, I can provide intelligence on any faction on Planet Suzaku. Information is the one currency that appreciates in war."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Morning cultivation", "location": "yao_family_territory", "duration": 180},
            {"time": "08:00", "action": "Review intelligence reports from agents", "location": "yao_family_territory", "duration": 180},
            {"time": "11:00", "action": "Manage Yao Family external affairs", "location": "yao_family_territory", "duration": 180},
            {"time": "14:00", "action": "Report to Blood Ancestor", "location": "blood_sea", "duration": 120},
            {"time": "16:00", "action": "Diplomatic meetings with other factions", "location": "yao_family_territory", "duration": 180},
            {"time": "19:00", "action": "Cultivation at Infant Transformation stage", "location": "yao_family_territory", "duration": 180},
            {"time": "22:00", "action": "Coordinate agent network", "location": "yao_family_territory", "duration": 120},
            {"time": "00:00", "action": "Rest", "location": "yao_family_territory", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "yx_external_funding",
                "name": "Yao Family Intelligence Operations",
                "description": "Yao Xixue needs emeralds and gold to fund the Yao Family's intelligence network across Planet Suzaku.",
                "required_items": {"minecraft:emerald": 16, "minecraft:gold_ingot": 10},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 3, "minecraft:enchanted_book": 1, "experience": 45}
            },
            {
                "task_id": "yx_faction_intelligence",
                "name": "Cross-Faction Intelligence Trade",
                "description": "Yao Xixue trades detailed intelligence on other factions in exchange for jade slips containing cultivation secrets.",
                "required_items": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "minecraft:emerald": 8, "experience": 40}
            },
        ]
    },
    "npc_ta_shan.json": {
        "location": "soul_refining_ancestral",
        "initiation_lines": [
            "...",
            "Master requires protection. I protect. That is sufficient.",
            "I am Ta Shan. Celestial Guard. Refined for loyalty. Refined for silence. If Master's enemy approaches, they will face me first. They will not face anyone after.",
            "...Materials for armor maintenance. Iron. Obsidian. Bring them. I will continue standing."
        ],
        "daily_schedule": [
            {"time": "00:00", "action": "Stand guard at Soul Refining Ancestral entrance", "location": "soul_refining_ancestral", "duration": 480},
            {"time": "08:00", "action": "Patrol Ancestral Ground perimeter", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "11:00", "action": "Armor maintenance and repair", "location": "soul_refining_ancestral", "duration": 120},
            {"time": "13:00", "action": "Stand guard at inner chamber", "location": "soul_refining_ancestral", "duration": 300},
            {"time": "18:00", "action": "Patrol outer Ancestral Ground", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "21:00", "action": "Stand guard at Ten Billion Soul Banner chamber", "location": "soul_refining_ancestral", "duration": 360},
            {"time": "03:00", "action": "Brief cultivation cycle", "location": "soul_refining_ancestral", "duration": 180},
            {"time": "06:00", "action": "Resume entrance guard duty", "location": "soul_refining_ancestral", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "ts_armor_maintenance",
                "name": "Celestial Guard Armor Repair",
                "description": "Ta Shan needs iron and obsidian to maintain his Celestial Guard armor. He offers protection knowledge in return.",
                "required_items": {"minecraft:iron_ingot": 20, "minecraft:obsidian": 12},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 35}
            },
            {
                "task_id": "ts_patrol_report",
                "name": "Ancestral Ground Patrol Report",
                "description": "Ta Shan accepts reports of unusual activity near the Soul Refining Ancestral Ground. Bring jade slips with observations.",
                "required_items": {"ergenverse:jade_slip": 1, "minecraft:emerald": 6},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "experience": 25}
            },
        ]
    },
    "npc_lei_ji.json": {
        "location": "soul_refining_sect",
        "dialogue_available": True,
        "quest_available": True,
        "trade_available": False,
        "teaching_available": False,
        "initiation_lines": [
            "*A deep thunderous rumble shakes the ground. Lei Ji's eyes crackle with blue lightning. The bonded thunder beast lowers its massive head, sniffing you with electric curiosity.*",
            "*Lei Ji circles once, leaving scorch marks on the stone. A low growl vibrates through your chest. This Ascendant-tier thunder beast is clearly assessing whether you are a threat to its master.*",
            "*Lei Ji's fur bristles with static charge. It seems to understand you are not an enemy, but its vigilance never wavers. It is bonded to Wang Lin. Its loyalty is absolute.*",
            "*Lei Ji nudges a pile of blaze rods with its snout and looks at you expectantly. The thunder beast apparently wants something, though communicating with a beast requires patience and redstone.*"
        ],
        "daily_schedule": [
            {"time": "00:00", "action": "Sleep curled around Soul Refining Sect courtyard", "location": "soul_refining_sect", "duration": 360},
            {"time": "06:00", "action": "Thunder-charged morning patrol", "location": "soul_refining_sect", "duration": 180},
            {"time": "09:00", "action": "Hunt for lightning-aspected prey in territory", "location": "soul_refining_sect", "duration": 240},
            {"time": "13:00", "action": "Rest and discharge stored lightning", "location": "soul_refining_sect", "duration": 120},
            {"time": "15:00", "action": "Cultivation — absorb ambient thunder energy", "location": "soul_refining_sect", "duration": 180},
            {"time": "18:00", "action": "Evening perimeter patrol", "location": "soul_refining_sect", "duration": 180},
            {"time": "21:00", "action": "Guard Wang Lin's quarters", "location": "soul_refining_sect", "duration": 300},
            {"time": "02:00", "action": "Sleep in guard position", "location": "soul_refining_sect", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "lj_thunder_materials",
                "name": "Thunder Beast Sustenance",
                "description": "Lei Ji needs blaze rods and redstone to sustain its Ascendant-tier thunder energy. The bonded beast rewards those who care for it.",
                "required_items": {"minecraft:blaze_rod": 6, "minecraft:redstone": 16},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 2, "minecraft:experience_bottle": 3, "experience": 35}
            },
            {
                "task_id": "lj_lightning_core",
                "name": "Lightning Core Collection",
                "description": "Lei Ji occasionally sheds charged lightning cores during thunder discharge. Bring copper and glowstone to help collect them safely.",
                "required_items": {"minecraft:copper_ingot": 8, "minecraft:glowstone": 6},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 1, "experience": 30}
            },
        ]
    },
}

def main():
    updated = 0
    for filename, fields in UPDATES.items():
        filepath = os.path.join(NPC_DIR, filename)
        if not os.path.exists(filepath):
            print(f"WARN: {filepath} not found, skipping")
            continue
        with open(filepath) as f:
            data = json.load(f)
        # Handle list vs dict
        if isinstance(data, list):
            item = data[0]
        else:
            item = data
        # Apply updates
        for k, v in fields.items():
            item[k] = v
        with open(filepath, 'w') as f:
            json.dump(data if isinstance(data, list) else item, f, indent=2, ensure_ascii=False)
        updated += 1
        print(f"OK: {filename}")

    print(f"\nUpdated {updated}/{len(UPDATES)} NPC files.")

if __name__ == "__main__":
    main()