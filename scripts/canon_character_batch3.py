#!/usr/bin/env python3
"""
AUTO-CANON-049: P2 batch 3 — 8 canon characters across Sea of Devils, Soul Refining, and Vermilion Bird arcs.
Punnan Zi (N88), Old Man Ji Mo (N86), Lin Yi (N89), Xu Liqing/Six-Desire Devil (N92),
Qian Pinghai (N80), Ye Wuyou (N79), 14th-Gen Vermilion Bird (N82), 3rd-Gen Vermilion Bird (N81).

All have location, cultivation, personality, but NO initiation_lines,
daily_schedule, or sect_tasks. This script extends existing JSON files.
"""
import json, os

NPC_DIR = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/npcs"

UPDATES = {
    "npc_punnan_zi.json": {
        "location": "sea_of_devils",
        "initiation_lines": [
            "You are bold to enter my domain. The Sea of Devils has consumed cultivators far stronger than you. I am Punnan Zi. Remember that name. It will be the last thing you learn.",
            "Lou Hou. That is what the ancients called me. Punnan Zi is what I call myself now. The difference between those two names is the difference between who I was and what the Sea of Devils made me.",
            "Wang Lin. Yes, I know him. He passed through these waters once. He left pieces of himself behind — not intentionally. The Sea of Devils takes tolls from everyone. Even him.",
            "If you seek the Heart Devil Flower that grows in the deepest currents, you will need guidance. I have guided many cultivators to it. None have returned. Bring me soul fragments and I will consider adding you to that count."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Ancient meditation on Sea of Devils currents", "location": "sea_of_devils", "duration": 240},
            {"time": "07:00", "action": "Survey chaotic current boundaries", "location": "sea_of_devils", "duration": 180},
            {"time": "10:00", "action": "Collect Heart Devil Flowers from deep currents", "location": "sea_of_devils", "duration": 180},
            {"time": "13:00", "action": "Consume spiritual energy from sea currents", "location": "sea_of_devils", "duration": 90},
            {"time": "14:30", "action": "Test intruders who enter domain", "location": "sea_of_devils", "duration": 150},
            {"time": "17:00", "action": "Cultivation at peak Third Step", "location": "sea_of_devils", "duration": 240},
            {"time": "21:00", "action": "Patrol outer ring perimeter", "location": "sea_of_devils", "duration": 180},
            {"time": "00:00", "action": "Rest in deep sea hollow", "location": "sea_of_devils", "duration": 180},
        ],
        "sect_tasks": [
            {
                "task_id": "pz_heart_devil_flower",
                "name": "Heart Devil Flower Retrieval",
                "description": "Punnan Zi sends cultivators to retrieve Heart Devil Flowers from the chaotic currents of the Sea of Devils. Few return.",
                "required_items": {"ergenverse:soul_fragment": 5, "minecraft:blaze_rod": 2},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 3, "minecraft:experience_bottle": 4, "experience": 50}
            },
            {
                "task_id": "pz_sea_knowledge",
                "name": "Sea of Devils Navigation Lore",
                "description": "Punnan Zi trades knowledge of safe paths through the Sea of Devils in exchange for jade slips documenting other regions.",
                "required_items": {"ergenverse:jade_slip": 3, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 2, "experience": 45}
            },
        ]
    },
    "npc_old_man_ji_mo.json": {
        "initiation_lines": [
            "Hehehe! Another one wanders into my waters! The Ji Mo Sect has ruled these currents for three hundred years. You think you can challenge that? Amusing. Very amusing.",
            "Wang Lin destroyed my avatar once. Just an avatar, mind you. My true body slumbers deeper than any cultivator has ever reached. When I wake, the Sea of Devils will tremble.",
            "The demonic path is not evil. It is efficient. Good cultivators waste centuries on morality. I spent those centuries becoming powerful. Tell me which approach the world rewards.",
            "Spirit stones and netherwart. The Ji Mo Sect refines them into demonic pills. Bring materials and I may let you watch. Watching is all you will do unless you prove yourself useful."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Demonic cultivation in underwater cavern", "location": "sea_of_devils", "duration": 180},
            {"time": "07:00", "action": "Inspect Ji Mo Sect disciples", "location": "sea_of_devils", "duration": 120},
            {"time": "09:00", "action": "Refine demonic pills", "location": "sea_of_devils", "duration": 240},
            {"time": "13:00", "action": "Consume pills and cultivate", "location": "sea_of_devils", "duration": 120},
            {"time": "15:00", "action": "Send disciples to raid passing cultivators", "location": "sea_of_devils", "duration": 180},
            {"time": "18:00", "action": "Study soul refinement techniques stolen from Soul Refining Sect", "location": "sea_of_devils", "duration": 150},
            {"time": "20:30", "action": "Patrol sea of devils inner ring", "location": "sea_of_devils", "duration": 180},
            {"time": "23:30", "action": "Rest in avatar chamber", "location": "sea_of_devils", "duration": 270},
        ],
        "sect_tasks": [
            {
                "task_id": "jm_demonic_pill_materials",
                "name": "Demonic Pill Reagents",
                "description": "Old Man Ji Mo needs netherwart and spirit stones to refine his signature demonic cultivation pills.",
                "required_items": {"minecraft:nether_wart": 16, "ergenverse:spirit_stone": 4},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 3, "ergenverse:soul_fragment": 1, "experience": 35}
            },
            {
                "task_id": "jm_raiding_intelligence",
                "name": "Cultivator Movement Reports",
                "description": "Ji Mo Sect needs intelligence on cultivator movements through the Sea of Devils. Bring jade slips with navigational data.",
                "required_items": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8},
                "rewards": {"minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 30}
            },
        ]
    },
    "npc_lin_yi.json": {
        "location": "soul_refining_sect",
        "initiation_lines": [
            "I do not engage in small talk. The Soul Refining Sect does not reward sociability. It rewards results. If you have results, speak. Otherwise, leave.",
            "Wang Lin was an anomaly. The Soul Refining Sect produces tools. He became something else. I study anomalies. Not because I admire them, but because they represent failure in our system.",
            "The inner sect has been watching you. Your cultivation path is... unconventional. Unconventional paths either produce brilliance or destruction. The Soul Refining Sect accommodates both.",
            "Bones. Soul fragments. Glowstone for the refinement arrays. If you can procure these without drawing attention from the outer sect elders, I will compensate you discreetly."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Soul analysis meditation", "location": "soul_refining_sect", "duration": 180},
            {"time": "08:00", "action": "Monitor inner sect disciples", "location": "soul_refining_sect", "duration": 120},
            {"time": "10:00", "action": "Study anomaly cultivation paths", "location": "soul_refining_sect", "duration": 180},
            {"time": "13:00", "action": "Meal in silent chamber", "location": "soul_refining_sect", "duration": 60},
            {"time": "14:00", "action": "Report findings to Duanmu Ji", "location": "soul_refining_sect", "duration": 120},
            {"time": "16:00", "action": "Refinement array maintenance", "location": "soul_refining_sect", "duration": 150},
            {"time": "18:30", "action": "Evening cultivation", "location": "soul_refining_sect", "duration": 240},
            {"time": "23:00", "action": "Rest", "location": "soul_refining_sect", "duration": 300},
        ],
        "sect_tasks": [
            {
                "task_id": "ly_refinement_supplies",
                "name": "Discreet Refinement Materials",
                "description": "Lin Yi needs bones, soul fragments, and glowstone for refinement arrays. She prefers discretion over volume.",
                "required_items": {"minecraft:bone": 12, "ergenverse:soul_fragment": 3, "minecraft:glowstone": 8},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 2, "ergenverse:spirit_stone": 1, "experience": 30}
            },
            {
                "task_id": "ly_anomaly_report",
                "name": "Anomaly Observation Report",
                "description": "Lin Yi collects information on unusual cultivation phenomena. Bring jade slips and books documenting strange encounters.",
                "required_items": {"ergenverse:jade_slip": 1, "minecraft:book": 3},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "experience": 25}
            },
        ]
    },
    "npc_xu_liqing.json": {
        "initiation_lines": [
            "Desire is not weakness. It is the most fundamental force in cultivation. The Six Desires — sight, sound, smell, taste, touch, thought — each is a door. I have walked through all six.",
            "The Soul Refining Sect thinks it controls me. They use my techniques. They fear my methods. Control and fear are two sides of the same coin, and I am the coin itself.",
            "Wang Lin resisted my Six Desires technique once. Only once. Most cultivators surrender to their own cravings before I even activate the array. He stared into the heart of his deepest desire and chose to walk away. That makes him dangerous.",
            "Redstone and amethyst. These channel desire energy. Bring them and I will show you the first Desire Door. What lies beyond it depends entirely on what you want most."
        ],
        "daily_schedule": [
            {"time": "03:00", "action": "Six Desires deep meditation", "location": "soul_refining_sect", "duration": 240},
            {"time": "07:00", "action": "Test desire arrays on captured souls", "location": "soul_refining_sect", "duration": 180},
            {"time": "10:00", "action": "Refine desire-channeling materials", "location": "soul_refining_sect", "duration": 180},
            {"time": "13:00", "action": "Observe outer sect disciples for desire weaknesses", "location": "soul_refining_sect", "duration": 120},
            {"time": "15:00", "action": "Lecture on desire manipulation theory", "location": "soul_refining_sect", "duration": 120},
            {"time": "17:00", "action": "Cultivation at Soul Transformation stage", "location": "soul_refining_sect", "duration": 180},
            {"time": "20:00", "action": "Secret meetings with faction allies", "location": "soul_refining_sect", "duration": 150},
            {"time": "23:00", "action": "Rest in desire-saturated chamber", "location": "soul_refining_sect", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "xlq_desire_materials",
                "name": "Desire Channeling Components",
                "description": "Xu Liqing needs redstone and amethyst to construct desire-channeling arrays for his Six Desires technique research.",
                "required_items": {"minecraft:redstone": 24, "minecraft:amethyst_shard": 8},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:soul_fragment": 1, "experience": 45}
            },
            {
                "task_id": "xlq_soul_observation",
                "name": "Soul Desire Analysis",
                "description": "Xu Liqing seeks soul fragments to study how desire persists after death. Trade soul fragments for forbidden knowledge.",
                "required_items": {"ergenverse:soul_fragment": 6, "ergenverse:spirit_stone": 2},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:experience_bottle": 4, "experience": 50}
            },
        ]
    },
    "npc_qian_pinghai.json": {
        "initiation_lines": [
            "You stand before the 13th Generation Vermilion Bird Divine Emperor. Show proper respect. This sect has endured for generations because we understand order, duty, and the weight of fire.",
            "The Heavenly Fate Sect reads stars. The Soul Refining Sect refines souls. The Corpse Yin Sect animates corpses. We burn. It is simpler. It is older. It is more effective than any of them care to admit.",
            "Wang Lin carries the Vermilion Bird legacy in ways he does not yet understand. I chose my successor carefully. The 14th Generation carries my teachings. Whether they carry my wisdom remains to be seen.",
            "Blaze rods and gold. The imperial forge requires these to maintain the sect's sacred flames. Contribute, and you will be remembered in the Vermilion Bird annals."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Imperial flame meditation", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "07:00", "action": "Hold morning court with sect elders", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "09:00", "action": "Inspect sacred flame vaults", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "12:00", "action": "Formal meal with inner disciples", "location": "vermilion_bird_divine_sect", "duration": 90},
            {"time": "13:30", "action": "Receive reports from Vermilion Bird outposts", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "15:30", "action": "Cultivation at Divine Emperor level", "location": "vermilion_bird_divine_sect", "duration": 240},
            {"time": "20:00", "action": "Audit sect defense formations", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "22:30", "action": "Rest in imperial chamber", "location": "vermilion_bird_divine_sect", "duration": 330},
        ],
        "sect_tasks": [
            {
                "task_id": "qp_sacred_flame_materials",
                "name": "Sacred Flame Maintenance",
                "description": "The 13th Generation Divine Emperor needs blaze rods and gold to maintain the Vermilion Bird sect's sacred flames.",
                "required_items": {"minecraft:blaze_rod": 8, "minecraft:gold_ingot": 12},
                "rewards": {"ergenverse:jade_slip": 1, "ergenverse:spirit_stone": 3, "minecraft:enchanted_book": 1, "experience": 45}
            },
            {
                "task_id": "qp_outpost_intelligence",
                "name": "Continental Intelligence Report",
                "description": "The Divine Emperor seeks jade slips containing intelligence from other sects and regions across Planet Suzaku.",
                "required_items": {"ergenverse:jade_slip": 2, "minecraft:emerald": 10},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2, "minecraft:experience_bottle": 2, "experience": 40}
            },
        ]
    },
    "npc_ye_wuyou.json": {
        "initiation_lines": [
            "I was the first. Before the Heavenly Fate Sect mapped the stars, before the Soul Refining Sect learned to harvest souls, before the Heng Yue Sect raised its first sword — I held the Vermilion Bird flame. That was eons ago.",
            "Sorrow is not weakness for one who has lived as long as I have. It is the only honest emotion. Joy fades. Anger burns out. Ambition collapses. Sorrow remains because loss is the one thing time never reverses.",
            "Wang Lin reminds me of the second generation. Same quiet determination. Same refusal to accept that the world dictates his path. I have seen many like him. Most died. Some transcended. He will do one or the other.",
            "Nether stars and ancient tomes. I study what was before the Vermilion Bird sect. If you find artifacts from the ancient era, bring them. I will trade knowledge that no living cultivator possesses."
        ],
        "daily_schedule": [
            {"time": "02:00", "action": "Ancient era recollection meditation", "location": "vermilion_bird_divine_sect", "duration": 300},
            {"time": "07:00", "action": "Walk the ancestral flame corridors", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "09:00", "action": "Study pre-sect cultivation records", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "12:00", "action": "Silent meal alone", "location": "vermilion_bird_divine_sect", "duration": 60},
            {"time": "13:00", "action": "Advise current Divine Emperor when asked", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "15:00", "action": "Cultivation at First Generation level", "location": "vermilion_bird_divine_sect", "duration": 300},
            {"time": "20:30", "action": "Observe night sky for celestial changes", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "00:00", "action": "Rest in ancestral crypt", "location": "vermilion_bird_divine_sect", "duration": 120},
        ],
        "sect_tasks": [
            {
                "task_id": "yw_ancient_artifacts",
                "name": "Ancient Era Artifacts",
                "description": "Ye Wuyou seeks nether stars and ancient tomes from before the Vermilion Bird sect's founding. He trades unique knowledge in return.",
                "required_items": {"minecraft:nether_star": 1, "minecraft:book": 5},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 2, "ergenverse:dao_fragment": 1, "experience": 60}
            },
            {
                "task_id": "yw_ancestral_memories",
                "name": "Ancestral Flame Communion",
                "description": "Ye Wuyou needs spirit stones to power the ancestral flame communion ritual, which reveals fragments of ancient history.",
                "required_items": {"ergenverse:spirit_stone": 5, "ergenverse:jade_slip": 1},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 50}
            },
        ]
    },
    "npc_14th_gen_vermilion.json": {
        "initiation_lines": [
            "The 14th Generation speaks. You listen. That is the natural order. The Vermilion Bird Divine Sect did not survive fourteen generations by entertaining democratic discourse.",
            "My predecessor, the 13th Generation, was soft. Diplomatic. He believed the sect could coexist with the Heavenly Fate Sect and the Soul Refining Sect. I believe the sect can consume them.",
            "Wang Lin's connection to the Vermilion Bird flame is an asset. When the time comes, that asset will be claimed. The flame does not choose its bearer. The bearer is chosen.",
            "Iron and blaze powder. The 14th Generation's military forge runs continuously. If you can supply materials, you will earn the right to train in the inner flame chambers."
        ],
        "daily_schedule": [
            {"time": "05:00", "action": "Military strategy meditation", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "07:00", "action": "War council with generals", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "10:00", "action": "Inspect military forge operations", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "13:00", "action": "Combat training with elite disciples", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "16:00", "action": "Review expansion plans for sect territory", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "18:00", "action": "Cultivation at 14th Generation power level", "location": "vermilion_bird_divine_sect", "duration": 240},
            {"time": "22:00", "action": "Patrol sect borders personally", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "01:00", "action": "Rest in war chamber", "location": "vermilion_bird_divine_sect", "duration": 240},
        ],
        "sect_tasks": [
            {
                "task_id": "v14_military_forge",
                "name": "Military Forge Supplies",
                "description": "The 14th Generation needs iron and blaze powder for the military forge that equips the sect's expansion forces.",
                "required_items": {"minecraft:iron_ingot": 24, "minecraft:blaze_powder": 12},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 40}
            },
            {
                "task_id": "v14_intel_gathering",
                "name": "Rival Sect Intelligence",
                "description": "The 14th Generation seeks intelligence on rival sect military capabilities. Bring jade slips with tactical information.",
                "required_items": {"ergenverse:jade_slip": 2, "minecraft:emerald": 12},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 3, "ergenverse:spirit_stone": 3, "experience": 45}
            },
        ]
    },
    "npc_3rd_gen_vermilion.json": {
        "initiation_lines": [
            "Three generations after the founding, the Vermilion Bird sect faced its first true crisis. I led us through it. The current generation has forgotten that crisis. I have not.",
            "Ye Wuyou founded this sect on fire and sorrow. I shaped it into a weapon. The generations between us softened it. I do not intend to let that happen again.",
            "Wang Lin walks a path I recognize. It is the path of one who burns everything to reach the top. I walked that path. The top is lonelier than anyone describes.",
            "Obsidian and redstone. The 3rd Generation's defense arrays still protect the sect's foundations. They require maintenance that only I understand. Help me, and I will teach you the Vermilion Bird's original defensive art."
        ],
        "daily_schedule": [
            {"time": "04:00", "action": "Foundation array meditation", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "07:00", "action": "Inspect ancient defense arrays", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "10:00", "action": "Repair structural damage to sect foundations", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "13:00", "action": "Advise on historical precedent", "location": "vermilion_bird_divine_sect", "duration": 120},
            {"time": "15:00", "action": "Teach original Vermilion Bird arts to worthy disciples", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "18:00", "action": "Cultivation at 3rd Generation power", "location": "vermilion_bird_divine_sect", "duration": 240},
            {"time": "22:00", "action": "Night patrol of foundation perimeter", "location": "vermilion_bird_divine_sect", "duration": 180},
            {"time": "01:00", "action": "Rest in foundation chamber", "location": "vermilion_bird_divine_sect", "duration": 180},
        ],
        "sect_tasks": [
            {
                "task_id": "v3_foundation_maintenance",
                "name": "Foundation Array Repair",
                "description": "The 3rd Generation Vermilion Bird Master needs obsidian and redstone to maintain the sect's ancient defense arrays.",
                "required_items": {"minecraft:obsidian": 16, "minecraft:redstone": 20},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:spirit_stone": 2, "experience": 45}
            },
            {
                "task_id": "v3_historical_research",
                "name": "Sect Historical Records",
                "description": "The 3rd Generation collects historical records and books to preserve the sect's early history. Trade knowledge for ancient arts.",
                "required_items": {"minecraft:book": 4, "ergenverse:jade_slip": 1},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1, "ergenverse:dao_fragment": 1, "experience": 40}
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