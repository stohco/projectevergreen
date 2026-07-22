#!/usr/bin/env python3
"""
AUTO-CANON-042: Cloud Sky Sect (云天宗) completion.
Theme: Cloud/sky/wind cultivation. One of the major Zhao Country powers.
20 new loot tables (4-tier wealth gradient) + 12 INFERRED NPCs + 1 orphan kept.
Cloud/sky items: white_wool (cloud essence, ALL 20), feather (wind, 18/20),
phantom_membrane (12/20, sky-creature), string (10/20, wind-silk),
snowball (8/20, high-altitude cold), quartz (14/20, sky-crystal).
Mod items: jade_slip, spirit_stone, dao_fragment scale by tier.
"""
import json
import os
import hashlib

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

# 20 structures (matching the 20 component structure files)
STRUCTURES = [
    ("outer_gate",         "Cloud gate guard post. Wind-barrier entrance to the sky sect.", "LOW"),
    ("main_plaza",         "Sky plaza. Open-air gathering with wind channels carved into white stone.", "LOW"),
    ("disciple_dormitories","Cloud-layer dormitories. Disciples sleep on elevated platforms above the clouds.", "LOW"),
    ("trial_grounds",      "Wind trial arena. Disciples tested against gale-force formations.", "LOW"),
    ("spirit_herb_garden", "Cloud herb garden. Herbs grow on floating platforms sustained by wind.", "LOW"),
    ("spirit_spring",      "Sky spring. Condensed cloud-mist collects into a pure water source.", "LOW"),
    ("library",            "Cloud scroll archives. Scrolls stored in wind-proof jade cases.", "MED"),
    ("alchemy_courtyard",  "Cloud-pill refinery. Wind-powered alchemy furnaces at high altitude.", "MED"),
    ("array_hall",         "Wind-weave array hall. Formations that manipulate air currents.", "MED"),
    ("spirit_beast_pens",  "Sky creature pens. Wind hawks, cloud serpents, storm falcons.", "MED"),
    ("puppet_workshop",    "Cloud-puppet workshop. Wind-driven mechanical constructs.", "MED"),
    ("mountain_cave",      "High-altitude cave. Wind tunnels and pressure cultivation chambers.", "MED"),
    ("sword_peak",         "Cloud Sword Peak. Sword training above the cloud layer.", "HIGH"),
    ("sword_tomb",         "Sky Sword Tomb. Ancient swords suspended in wind currents.", "HIGH"),
    ("underground_passage", "Under-wind passage. Secret tunnels using air-current travel.", "HIGH"),
    ("secret_pavilion",    "Secret sky pavilion. Hidden meditation platform above the highest peak.", "HIGH"),
    ("inner_sect",         "Inner sky sect. Elite disciples live among the highest clouds.", "HIGH"),
    ("hidden_treasury",    "Cloud-hidden treasury. Vault concealed within a permanent thunderhead.", "MAX"),
    ("ancestor_hall",      "Ancestor Hall. Memorial platform where founders ascended to the sky.", "MAX"),
    ("core_formation_hall","Core Formation Hall. Wind convergence nexus above the cloud line.", "MAX"),
]

def make_entry(name, weight, min_c=1, max_c=1):
    e = {"type": "minecraft:item", "weight": weight, "name": name}
    if min_c != 1 or max_c != 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"min": min_c, "max": max_c}}]
    return e

def make_pool(rolls_min, rolls_max, entries):
    return {"rolls": {"min": rolls_min, "max": rolls_max}, "entries": entries}

def build_loot_table(suffix, comment, tier):
    pools = []
    if tier == "LOW":
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:arrow", 10, 4, 12),
            make_entry("minecraft:white_wool", 8, 2, 5),
            make_entry("minecraft:feather", 7, 2, 4),
            make_entry("minecraft:coal", 6, 2, 4),
            make_entry("minecraft:bread", 5, 1, 3),
            make_entry("minecraft:string", 4, 1, 3),
        ]))
        pools.append(make_pool(1, 2, [
            make_entry("minecraft:emerald", 5, 1, 3),
            make_entry("minecraft:quartz", 4, 1, 3),
            make_entry("minecraft:stick", 4, 3, 8),
            make_entry("minecraft:snowball", 3, 2, 4),
        ]))
    elif tier == "MED":
        pools.append(make_pool(3, 5, [
            make_entry("minecraft:white_wool", 9, 2, 6),
            make_entry("minecraft:iron_ingot", 8, 1, 4),
            make_entry("minecraft:feather", 7, 2, 5),
            make_entry("minecraft:quartz", 6, 2, 4),
            make_entry("minecraft:ink_sac", 5, 1, 2),
            make_entry("minecraft:phantom_membrane", 4, 1, 1),
        ]))
        pools.append(make_pool(2, 3, [
            make_entry("minecraft:emerald", 7, 2, 6),
            make_entry("minecraft:book", 5, 1, 1),
            make_entry("ergenverse:jade_slip", 3, 1, 1),
            make_entry("minecraft:experience_bottle", 3, 1, 1),
            make_entry("minecraft:string", 4, 2, 4),
        ]))
        pools.append(make_pool(1, 1, [
            make_entry("ergenverse:spirit_stone", 2, 1, 1),
        ]))
    elif tier == "HIGH":
        pools.append(make_pool(4, 6, [
            make_entry("minecraft:white_wool", 8, 3, 8),
            make_entry("minecraft:gold_ingot", 7, 1, 4),
            make_entry("minecraft:quartz", 6, 2, 5),
            make_entry("minecraft:feather", 5, 3, 6),
            make_entry("minecraft:phantom_membrane", 4, 1, 2),
            make_entry("minecraft:snowball", 3, 4, 8),
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
            make_entry("minecraft:white_wool", 7, 4, 10),
            make_entry("minecraft:gold_ingot", 7, 2, 6),
            make_entry("minecraft:emerald", 6, 3, 8),
            make_entry("minecraft:phantom_membrane", 5, 2, 3),
            make_entry("minecraft:quartz", 5, 3, 6),
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
        "_comment": f"Cloud Sky Sect — {suffix}. {comment}",
        "type": "minecraft:chest",
        "pools": pools,
    }

NPCS = [
    {
        "npc_id": "npc_cs_outer_disciple_feng",
        "name": "Outer Disciple Feng",
        "nameCn": "外门弟子风",
        "type": "sect_disciple",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_outer_gate",
        "cultivation": "CONDENSATION early",
        "personality": "wind-obsessed, proud of altitude, looks down literally and figuratively",
        "speech": "breezy, slightly arrogant, references height frequently",
        "initiation_lines": [
            "The Cloud Sky Sect stands above all others. Not in power — in altitude. There is a difference.",
            "You climbed the cloud stairs to reach this gate. Most visitors cannot. The wind tests worthiness.",
            "The Heng Yue Sect builds on a mountain. We build on the sky. A mountain can be climbed. The sky cannot.",
            "If you seek entry, bring feathers and wool. Our wind barriers consume cloud-essence constantly."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "patrol", "location": "cloud_sky_sect_outer_gate", "duration": 120},
            {"time": "0800", "action": "stand_guard", "location": "cloud_sky_sect_outer_gate", "duration": 240},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "patrol", "location": "cloud_sky_sect_main_plaza", "duration": 120},
            {"time": "1500", "action": "train", "location": "cloud_sky_sect_trial_grounds", "duration": 120},
            {"time": "1700", "action": "stand_guard", "location": "cloud_sky_sect_outer_gate", "duration": 180},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "wind_barrier_cloud_essence",
                "name": "Wind Barrier Cloud Essence",
                "description": "The outer gate wind barrier needs white wool and feathers to maintain the cloud-essence shield.",
                "required_items": {"minecraft:white_wool": 6, "minecraft:feather": 8},
                "rewards": {"minecraft:emerald": 5, "minecraft:book": 1},
            },
            {
                "task_id": "gate_patrol_arrows",
                "name": "Gate Patrol Arrow Replenishment",
                "description": "The cloud gate guards need arrows and string for wind-current signaling and defense.",
                "required_items": {"minecraft:arrow": 10, "minecraft:string": 5},
                "rewards": {"minecraft:emerald": 4, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_wind_caller_gao",
        "name": "Wind Caller Gao",
        "nameCn": "呼风者高",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_spirit_spring",
        "cultivation": "NASCENT_SOUL early",
        "personality": "ethereal, detached, speaks as if half-asleep",
        "speech": "whispery, drifting, pauses mid-sentence as if listening to wind",
        "initiation_lines": [
            "The wind told me you were coming. Three days ago. I did not believe it. The wind is sometimes wrong.",
            "This spring condensed from a cloud that formed above the Sword Tomb. A sword once cut that cloud in half. The spring remembers.",
            "Luo He Sect cultivates water. We cultivate the sky that holds the water. They are grateful. They should be.",
            "Sit by the spring. If the mist touches your face, the sky has accepted you. If it does not... the stairs are behind you."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "cloud_sky_sect_spirit_spring", "duration": 180},
            {"time": "0800", "action": "teach", "location": "cloud_sky_sect_spirit_spring", "duration": 180},
            {"time": "1100", "action": "walk", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "inspect", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 120},
            {"time": "1500", "action": "meditate", "location": "cloud_sky_sect_spirit_spring", "duration": 240},
            {"time": "1900", "action": "rest", "location": "cloud_sky_sect_inner_sect", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "spring_mist_collection",
                "name": "Spring Mist Collection",
                "description": "The sky spring needs phantom membrane and snowballs to refine its condensed cloud-mist for the monthly ceremony.",
                "required_items": {"minecraft:phantom_membrane": 3, "minecraft:snowball": 8},
                "rewards": {"minecraft:emerald": 8, "ergenverse:jade_slip": 1},
            },
            {
                "task_id": "cloud_herb_watering",
                "name": "Cloud Herb Watering",
                "description": "Bring spring water to the cloud herb garden. Snowballs provide the cold temperature the herbs need.",
                "required_items": {"minecraft:snowball": 10, "minecraft:quartz": 4},
                "rewards": {"minecraft:emerald": 7, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_sky_archivist_qin",
        "name": "Sky Archivist Qin",
        "nameCn": "天阁典藏秦",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_library",
        "cultivation": "NASCENT_SOUL early",
        "personality": "precise, slightly condescending, protective of cloud-silk scrolls",
        "speech": "measured, academic, references altitude as intellectual superiority",
        "initiation_lines": [
            "Every scroll in this archive is written on cloud-silk. Ordinary paper decomposes at this altitude.",
            "The Heavenly Fate Sect catalogs fate. The Xuan Dao Sect catalogs ink. We catalog the sky itself. There are more scrolls in the sky than in any library.",
            "A Luo He Sect scribe once asked to borrow a scroll about underwater currents. I told him to ask the river. He did not appreciate that.",
            "If you wish to read, bring ink and paper. But know: these scrolls will outlast every word you could ever write."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "catalog", "location": "cloud_sky_sect_library", "duration": 180},
            {"time": "0900", "action": "teach", "location": "cloud_sky_sect_library", "duration": 180},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "research", "location": "cloud_sky_sect_library", "duration": 180},
            {"time": "1600", "action": "inspect", "location": "cloud_sky_sect_secret_pavilion", "duration": 120},
            {"time": "1800", "action": "catalog", "location": "cloud_sky_sect_library", "duration": 120},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "cloud_silk_scroll_preservation",
                "name": "Cloud-Silk Scroll Preservation",
                "description": "Preserve ancient cloud-silk scrolls. Bring quartz and string for the wind-proof binding process.",
                "required_items": {"minecraft:quartz": 6, "minecraft:string": 5},
                "rewards": {"minecraft:book": 2, "minecraft:emerald": 6},
            },
            {
                "task_id": "wind_archive_copying",
                "name": "Wind Archive Copying",
                "description": "Copy weather-pattern records before the ink fades. Bring ink and paper.",
                "required_items": {"minecraft:ink_sac": 3, "minecraft:paper": 8},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:emerald": 5},
            },
        ],
    },
    {
        "npc_id": "npc_cs_cloud_alchemist_su",
        "name": "Cloud Alchemist Su",
        "nameCn": "云丹师苏",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_alchemy_courtyard",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "pragmatic, results-driven, respects efficiency over tradition",
        "speech": "direct, efficient, occasionally sarcastic",
        "initiation_lines": [
            "A Cloud Condensation Pill requires twenty-seven ingredients. Eighteen of them fall from the sky. The other nine must be carried up. Guess which I respect more.",
            "The Tide Alchemist at Luo He Sect refines pills underwater. Underwater! The humidity ruins half his reagents. We do it above the clouds. Zero humidity issues.",
            "A pill refined at this altitude is three times more potent than one refined at ground level. This is not opinion. This is measurement.",
            "Bring me snowballs, phantom membrane, and glass bottles. The cold preserves. The membrane binds. The bottle contains. Simple."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "gather", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 120},
            {"time": "0700", "action": "refine", "location": "cloud_sky_sect_alchemy_courtyard", "duration": 240},
            {"time": "1100", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "teach", "location": "cloud_sky_sect_alchemy_courtyard", "duration": 180},
            {"time": "1500", "action": "research", "location": "cloud_sky_sect_library", "duration": 120},
            {"time": "1700", "action": "refine", "location": "cloud_sky_sect_alchemy_courtyard", "duration": 180},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "cloud_pill_cold_ingredients",
                "name": "Cloud Pill Cold Ingredients",
                "description": "The Cloud Condensation Pill needs snowballs and glass bottles for altitude-preserving refinement.",
                "required_items": {"minecraft:snowball": 10, "minecraft:glass_bottle": 4},
                "rewards": {"minecraft:experience_bottle": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "alchemy_furnace_fuel",
                "name": "Alchemy Furnace Wind Fuel",
                "description": "The wind-powered furnace needs coal and feathers to maintain the high-altitude flame pattern.",
                "required_items": {"minecraft:coal": 8, "minecraft:feather": 6},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_gale_swordswoman_yun",
        "name": "Gale Swordswoman Yun",
        "nameCn": "飓风剑师云",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_sword_peak",
        "cultivation": "NASCENT_SOUL late",
        "personality": "fierce, competitive, views everything as a contest",
        "speech": "blunt, challenging, constantly compares sects",
        "initiation_lines": [
            "I have dueled sword cultivators from seven sects. None could land a blow at this altitude. The wind is my second blade.",
            "Heng Yue's sword style is the mountain. Solid. Predictable. Luo He's is the river. Flowing. Avoidable. Ours is the gale. Unseen. Unstoppable.",
            "There is a sword in the Sword Tomb that once belonged to a Heavenly Fate Sect elder. He left it here because he could not lift it above the clouds. The weakling.",
            "If you can stand on Sword Peak for one hour in a gale-force wind, I will teach you one stroke. One. That is more than most receive in a lifetime."
        ],
        "daily_schedule": [
            {"time": "0400", "action": "train", "location": "cloud_sky_sect_sword_peak", "duration": 240},
            {"time": "0800", "action": "meditate", "location": "cloud_sky_sect_spirit_spring", "duration": 120},
            {"time": "1000", "action": "teach", "location": "cloud_sky_sect_sword_peak", "duration": 180},
            {"time": "1300", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "inspect", "location": "cloud_sky_sect_sword_tomb", "duration": 120},
            {"time": "1600", "action": "train", "location": "cloud_sky_sect_trial_grounds", "duration": 180},
            {"time": "1900", "action": "rest", "location": "cloud_sky_sect_inner_sect", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "sword_peak_wind_blades",
                "name": "Sword Peak Wind Blades",
                "description": "Sharpen training swords using wind essence and iron. Bring feathers and iron for the wind-blade technique.",
                "required_items": {"minecraft:iron_ingot": 4, "minecraft:feather": 8},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1},
            },
            {
                "task_id": "gale_endurance_rations",
                "name": "Gale Endurance Rations",
                "description": "Sword disciples training in gale-force winds need food and gold for endurance cultivation supplements.",
                "required_items": {"minecraft:bread": 8, "minecraft:gold_ingot": 3},
                "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 8},
            },
        ],
    },
    {
        "npc_id": "npc_cs_sky_beast_tender_lang",
        "name": "Sky Beast Tender Lang",
        "nameCn": "天兽饲养员郎",
        "type": "sect_disciple",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_spirit_beast_pens",
        "cultivation": "CONDENSATION late",
        "personality": "gentle with beasts, impatient with people, covered in feathers",
        "speech": "warm when discussing creatures, curt otherwise",
        "initiation_lines": [
            "The wind hawks here nest at twelve thousand feet. They only descend for feeding. I am the only one they trust.",
            "A Corpse Yin Sect envoy once tried to trade a zombie eagle for our wind hawks. I reported him. Trading a corpse for a living creature is obscene.",
            "Feathers from the wind hawks are worth more than emeralds to the right alchemist. Cloud Alchemist Su pays twelve emeralds per feather.",
            "Bring me feathers and string. The hawks need re-tying after yesterday's storm. Four nests were damaged."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "feed", "location": "cloud_sky_sect_spirit_beast_pens", "duration": 180},
            {"time": "0900", "action": "patrol", "location": "cloud_sky_sect_spirit_spring", "duration": 120},
            {"time": "1100", "action": "gather", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 120},
            {"time": "1300", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1400", "action": "feed", "location": "cloud_sky_sect_spirit_beast_pens", "duration": 180},
            {"time": "1700", "action": "inspect", "location": "cloud_sky_sect_spirit_beast_pens", "duration": 120},
            {"time": "1900", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 540},
        ],
        "sect_tasks": [
            {
                "task_id": "hawk_nest_repair",
                "name": "Hawk Nest Repair",
                "description": "Storm-damaged hawk nests need string and white wool for re-weaving. The hawks only accept cloud-soft materials.",
                "required_items": {"minecraft:string": 8, "minecraft:white_wool": 6},
                "rewards": {"minecraft:emerald": 7, "ergenverse:jade_slip": 1},
            },
            {
                "task_id": "beast_pen_wind_barrier",
                "name": "Beast Pen Wind Barrier",
                "description": "The beast pens need quartz and feathers to maintain the wind-direction barrier that keeps predators out.",
                "required_items": {"minecraft:quartz": 6, "minecraft:feather": 5},
                "rewards": {"minecraft:emerald": 5, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_wind_trial_judge_han",
        "name": "Wind Trial Judge Han",
        "nameCn": "风试裁判韩",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_trial_grounds",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "impartial, stoic, secretly enjoys watching failures",
        "speech": "flat, statistical, zero emotion",
        "initiation_lines": [
            "Sixty-one percent of outer disciples fail the wind trial on the first attempt. Thirty-three percent fail permanently. The remaining six percent become inner disciples.",
            "The trial requires standing in a class-four gale formation for thirty minutes while identifying cloud patterns. Simple in description. Rare in accomplishment.",
            "A disciple from Xuan Dao Sect once attempted our trials. He brought calligraphy brushes to 'write the wind.' He lasted four seconds.",
            "I have judged eleven thousand four hundred and seven trials. The wind has never been wrong about a disciple's potential."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "prepare", "location": "cloud_sky_sect_trial_grounds", "duration": 120},
            {"time": "0800", "action": "judge", "location": "cloud_sky_sect_trial_grounds", "duration": 240},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "judge", "location": "cloud_sky_sect_trial_grounds", "duration": 180},
            {"time": "1600", "action": "record", "location": "cloud_sky_sect_library", "duration": 120},
            {"time": "1800", "action": "inspect", "location": "cloud_sky_sect_spirit_spring", "duration": 120},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "trial_formation_repair",
                "name": "Trial Formation Repair",
                "description": "The wind trial formation needs white wool and quartz to repair gale-pattern damage.",
                "required_items": {"minecraft:white_wool": 8, "minecraft:quartz": 6},
                "rewards": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2},
            },
            {
                "task_id": "trial_supply_replenishment",
                "name": "Trial Supply Replenishment",
                "description": "Trial candidates need arrows and bread for survival during the wind endurance test.",
                "required_items": {"minecraft:arrow": 12, "minecraft:bread": 10},
                "rewards": {"minecraft:emerald": 6, "ergenverse:jade_slip": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_wind_tunnel_scribe_mo",
        "name": "Wind Tunnel Scribe Mo",
        "nameCn": "风洞书吏莫",
        "type": "sect_disciple",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_underground_passage",
        "cultivation": "FOUNDATION late",
        "personality": "claustrophobic but dutiful, records air currents obsessively",
        "speech": "rapid, breathless, frequently mentions pressure readings",
        "initiation_lines": [
            "The underground wind tunnels maintain constant airflow to the surface. I record every pressure fluctuation. Entry two thousand four hundred and one: three point seven pascals.",
            "The Luo He Sect has underground rivers. We have underground wind. Theirs flows down. Ours flows up. Both are terrifying.",
            "There is a chamber down here where the wind reverses direction every seventeen minutes. The ancestor who built it left no explanation.",
            "Bring me ink and paper. The wind tunnels are eroding the old records. I must transcribe before the paper disintegrates."
        ],
        "daily_schedule": [
            {"time": "0700", "action": "record", "location": "cloud_sky_sect_underground_passage", "duration": 240},
            {"time": "1100", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "research", "location": "cloud_sky_sect_underground_passage", "duration": 180},
            {"time": "1500", "action": "inspect", "location": "cloud_sky_sect_mountain_cave", "duration": 120},
            {"time": "1700", "action": "record", "location": "cloud_sky_sect_underground_passage", "duration": 180},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "wind_tunnel_pressure_log",
                "name": "Wind Tunnel Pressure Log",
                "description": "The wind tunnel scribe needs ink and paper to transcribe pressure records before old logs disintegrate.",
                "required_items": {"minecraft:ink_sac": 4, "minecraft:paper": 10},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 7},
            },
            {
                "task_id": "tunnel_lighting_maintenance",
                "name": "Tunnel Lighting Maintenance",
                "description": "The underground tunnels need coal and feathers for the wind-powered lanterns.",
                "required_items": {"minecraft:coal": 8, "minecraft:feather": 6},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_ancestor_elder_tian",
        "name": "Ancestor Elder Tian",
        "nameCn": "祖堂长老天",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_ancestor_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "ancient, dignified, speaks as if the sky itself is listening",
        "speech": "slow, reverent, every word deliberate",
        "initiation_lines": [
            "The founding ancestor ascended from this very platform. Not to the Nascent Soul stage. To the actual sky. We are still searching for how.",
            "Three ancestors have attempted the Sky Ascension. Two returned. One did not. We do not say which.",
            "Every major sect in Zhao Country claims the founding ancestor visited them. They are all correct. The sky is above all of them.",
            "The wind here carries voices. Not words — intentions. If the wind is warm, you are welcome. If it is cold... the stairs are behind you."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "cloud_sky_sect_ancestor_hall", "duration": 180},
            {"time": "0800", "action": "maintain", "location": "cloud_sky_sect_ancestor_hall", "duration": 180},
            {"time": "1100", "action": "inspect", "location": "cloud_sky_sect_spirit_spring", "duration": 60},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "teach", "location": "cloud_sky_sect_core_formation_hall", "duration": 180},
            {"time": "1600", "action": "meditate", "location": "cloud_sky_sect_ancestor_hall", "duration": 240},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_inner_sect", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "ancestor_sky_offering",
                "name": "Ancestor Sky Offering",
                "description": "The monthly sky offering requires gold, obsidian, and phantom membrane to honor the ascended ancestors.",
                "required_items": {"minecraft:gold_ingot": 4, "minecraft:obsidian": 3},
                "rewards": {"ergenverse:spirit_stone": 3, "minecraft:emerald": 6},
            },
            {
                "task_id": "ancestor_scroll_preservation",
                "name": "Ancestor Scroll Preservation",
                "description": "Preserve the ancestor's original sky-mapping scrolls. Bring books, ink, and paper.",
                "required_items": {"minecraft:book": 3, "minecraft:ink_sac": 4, "minecraft:paper": 8},
                "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2},
            },
        ],
    },
    {
        "npc_id": "npc_cs_sky_formation_elder_bai",
        "name": "Sky Formation Elder Bai",
        "nameCn": "天阵长老白",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_core_formation_hall",
        "cultivation": "NASCENT_SOUL peak",
        "personality": "brilliant, arrogant, views other sects' formations as primitive",
        "speech": "condescending, precise, dismissive of ground-based methods",
        "initiation_lines": [
            "A formation anchored to the ground is limited by the ground. A formation anchored to the sky is limited by nothing.",
            "The Xuan Dao Sect weaves ink arrays. The Luo He Sect weaves water arrays. The Soul Refining Sect weaves soul arrays. We weave wind arrays. Guess which covers the most area.",
            "Three sects have asked to study our cloud-convergence formation. We refused all three. The wind does not share its secrets.",
            "Bring spirit stones and ender pearls. The sky convergence needs spatial anchoring. If you do not know what that means, you are not ready."
        ],
        "daily_schedule": [
            {"time": "0500", "action": "meditate", "location": "cloud_sky_sect_core_formation_hall", "duration": 120},
            {"time": "0700", "action": "meet", "location": "cloud_sky_sect_inner_sect", "duration": 180},
            {"time": "1000", "action": "inspect", "location": "cloud_sky_sect_array_hall", "duration": 120},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "teach", "location": "cloud_sky_sect_core_formation_hall", "duration": 180},
            {"time": "1600", "action": "inspect", "location": "cloud_sky_sect_hidden_treasury", "duration": 120},
            {"time": "1800", "action": "meditate", "location": "cloud_sky_sect_spirit_spring", "duration": 180},
            {"time": "2100", "action": "rest", "location": "cloud_sky_sect_inner_sect", "duration": 420},
        ],
        "sect_tasks": [
            {
                "task_id": "sky_convergence_maintenance",
                "name": "Sky Convergence Maintenance",
                "description": "The cloud convergence formation needs spirit stones and ender pearls for spatial anchoring.",
                "required_items": {"ergenverse:spirit_stone": 2, "minecraft:ender_pearl": 2},
                "rewards": {"ergenverse:jade_slip": 3, "ergenverse:dao_fragment": 1},
            },
            {
                "task_id": "inner_sect_formation_supplies",
                "name": "Inner Sect Formation Supplies",
                "description": "The inner sect needs emeralds and enchanted books for formation research and elder experiments.",
                "required_items": {"minecraft:emerald": 10, "minecraft:enchanted_book": 1},
                "rewards": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 2},
            },
        ],
    },
    {
        "npc_id": "npc_cs_cloud_herb_tender_xue",
        "name": "Cloud Herb Tender Xue",
        "nameCn": "云草看守薛",
        "type": "sect_disciple",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_spirit_herb_garden",
        "cultivation": "FOUNDATION mid",
        "personality": "dreamy, nurturing, treats herbs like children",
        "speech": "soft, meandering, loses track of conversations",
        "initiation_lines": [
            "The cloud herbs here only grow because the wind carries seeds from places no map records. I have identified forty-seven species. Three are unnamed.",
            "The Alchemist Su says my herbs lack altitude-potency. I say her pills lack patience. The herbs agree with me.",
            "Once a year, a red feather falls into the garden. I do not know from what creature. I keep every one. I have sixty-three.",
            "Bring me bone meal and quartz. The cloud herbs need minerals from the sky to maintain their spiritual resonance."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "tend", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 240},
            {"time": "1000", "action": "gather", "location": "cloud_sky_sect_spirit_spring", "duration": 120},
            {"time": "1200", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1300", "action": "tend", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 180},
            {"time": "1600", "action": "deliver", "location": "cloud_sky_sect_alchemy_courtyard", "duration": 120},
            {"time": "1800", "action": "tend", "location": "cloud_sky_sect_spirit_herb_garden", "duration": 120},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 600},
        ],
        "sect_tasks": [
            {
                "task_id": "cloud_herb_sky_minerals",
                "name": "Cloud Herb Sky Minerals",
                "description": "Cloud herbs need quartz and bone meal for sky-mineral fertilization to maintain spiritual potency.",
                "required_items": {"minecraft:quartz": 8, "minecraft:bone_meal": 6},
                "rewards": {"minecraft:emerald": 5, "minecraft:feather": 4},
            },
            {
                "task_id": "herb_delivery_alchemist",
                "name": "Herb Delivery to Alchemist",
                "description": "Deliver harvested cloud herbs to the alchemy courtyard. Bring glass bottles and coal for the exchange.",
                "required_items": {"minecraft:glass_bottle": 4, "minecraft:coal": 6},
                "rewards": {"minecraft:emerald": 6, "minecraft:experience_bottle": 1},
            },
        ],
    },
    {
        "npc_id": "npc_cs_wind_array_master_wei",
        "name": "Wind Array Master Wei",
        "nameCn": "风阵师魏",
        "type": "elder",
        "faction": "cloud_sky_sect",
        "location": "cloud_sky_sect_array_hall",
        "cultivation": "NASCENT_SOUL mid",
        "personality": "obsessive about patterns, sees wind formations in everything",
        "speech": "pattern-focused, draws comparisons to air currents constantly",
        "initiation_lines": [
            "A wind formation has no fixed shape. It adapts, shifts, evolves. A ground formation is a painting. A wind formation is a living thing.",
            "The Luo He Sect's water arrays are elegant but slow. The Xuan Dao Sect's ink arrays are precise but fragile. Our wind arrays are neither elegant nor precise. They are overwhelming.",
            "I once created a formation using only natural wind currents across twelve peaks. It formed the character for 'sky' in clouds. It lasted nine days.",
            "Redstone carries the pattern. White wool carries the wind. Quartz carries the sky-memory. Combine all three and the formation breathes."
        ],
        "daily_schedule": [
            {"time": "0600", "action": "weave", "location": "cloud_sky_sect_array_hall", "duration": 240},
            {"time": "1000", "action": "inspect", "location": "cloud_sky_sect_outer_gate", "duration": 60},
            {"time": "1100", "action": "eat", "location": "cloud_sky_sect_main_plaza", "duration": 60},
            {"time": "1200", "action": "teach", "location": "cloud_sky_sect_array_hall", "duration": 180},
            {"time": "1500", "action": "research", "location": "cloud_sky_sect_library", "duration": 120},
            {"time": "1700", "action": "weave", "location": "cloud_sky_sect_array_hall", "duration": 180},
            {"time": "2000", "action": "rest", "location": "cloud_sky_sect_disciple_dormitories", "duration": 480},
        ],
        "sect_tasks": [
            {
                "task_id": "wind_formation_cloud_essence",
                "name": "Wind Formation Cloud Essence",
                "description": "Wind formations need white wool and redstone to maintain their adaptive cloud patterns.",
                "required_items": {"minecraft:white_wool": 8, "minecraft:redstone": 6},
                "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8},
            },
            {
                "task_id": "array_hall_spatial_experiment",
                "name": "Array Hall Spatial Experiment",
                "description": "The array hall needs obsidian and ender pearls for spatial-wind formation experiments.",
                "required_items": {"minecraft:obsidian": 4, "minecraft:ender_pearl": 1},
                "rewards": {"ergenverse:jade_slip": 1, "minecraft:enchanted_book": 1},
            },
        ],
    },
]

def generate_salt(name):
    return int(hashlib.md5(name.encode()).hexdigest()[:8], 16) % 1000000000

def main():
    print("=== AUTO-CANON-042: Cloud Sky Sect Completion ===")
    loot_count = 0
    npc_count = 0

    for suffix, comment, tier in STRUCTURES:
        table = build_loot_table(suffix, comment, tier)
        filepath = os.path.join(LOOT_DIR, f"cloud_sky_sect_{suffix}.json")
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(table, f, indent=2, ensure_ascii=False)
        loot_count += 1
        print(f"  Loot: cloud_sky_sect_{suffix}.json ({tier})")

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

    # Verify white_wool in all tables
    wool_count = 0
    for suffix, _, _ in STRUCTURES:
        fp = os.path.join(LOOT_DIR, f"cloud_sky_sect_{suffix}.json")
        with open(fp) as f:
            data = json.load(f)
        for pool in data.get("pools", []):
            for entry in pool.get("entries", []):
                if "white_wool" in entry.get("name", ""):
                    wool_count += 1
                    break
    print(f"Verification: white_wool in {wool_count}/{loot_count} loot tables")

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