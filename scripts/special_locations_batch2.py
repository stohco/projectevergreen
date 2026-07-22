#!/usr/bin/env python3
"""
AUTO-CANON-045: Special locations batch 2 — next 4 canon-significance single-structure locations.
  1. snow_country_capital — 1 loot + 3 NPCs (Wang Lin's next major arc)
  2. ancient_god_cave — 1 loot + 3 NPCs (ancient divine power)
  3. immortal_court_palace — 1 loot + 3 NPCs (Immortal Court governance)
  4. karma_crystal_formation — 1 loot + 3 NPCs (karma theme — central to RI)
Total: 4 new loot tables + 12 INFERRED NPCs.
"""
import json, os, hashlib

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

def me(name, weight, min_c=1, max_c=1):
    e = {"type": "minecraft:item", "weight": weight, "name": name}
    if min_c != 1 or max_c != 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"min": min_c, "max": max_c}}]
    return e

def mp(rmin, rmax, entries):
    return {"rolls": {"min": rmin, "max": rmax}, "entries": entries}

def npc(npc_id, name, nameCn, faction, location, cultivation,
        personality, speech, relationship, cc, inits, sched, tasks, ntype="sect_disciple"):
    salt = hashlib.sha256(npc_id.encode()).hexdigest()[:16]
    return {
        "npc_id": npc_id, "name": name, "nameCn": nameCn, "canon_id": "",
        "type": ntype, "faction": faction, "location": location,
        "cultivation": cultivation, "personality": personality, "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True, "quest_available": True,
        "trade_available": True, "teaching_available": True,
        "canon_confidence": cc, "derivation_type": "I", "salt": salt,
        "initiation_lines": inits, "daily_schedule": sched,
        "sect_tasks": tasks, "_xianxia_schema": 1
    }

def write_loot(name, pools):
    path = os.path.join(LOOT_DIR, name)
    with open(path, "w", encoding="utf-8") as f:
        json.dump({"pools": pools}, f, indent=2, ensure_ascii=False)
    print(f"  LOOT {name}")

def write_npcs(npcs):
    for n in npcs:
        path = os.path.join(NPC_DIR, f"{n['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(n, f, indent=2, ensure_ascii=False)
    print(f"  {len(npcs)} NPCs")

# ============================================================
# 1. SNOW COUNTRY CAPITAL (雪域国都)
# Theme: Ice, cold, isolation, northern power.
# In RI: Snow Country is Wang Lin's next major destination after Zhao Country.
# A cold, harsh nation with its own cultivation traditions and political intrigue.
# ============================================================
print("=== 1. Snow Country Capital ===")
write_loot("snow_country_capital.json", [
    mp(3, 5, [
        me("minecraft:packed_ice", 10, 2, 6),       # frozen capital
        me("minecraft:snowball", 8, 4, 12),          # perpetual snow
        me("minecraft:iron_ingot", 7, 2, 5),
        me("minecraft:coal", 6, 2, 6),               # heating fuel
        me("minecraft:emerald", 5, 1, 4),
        me("minecraft:blue_ice", 4, 1, 3),           # imperial ice
        me("minecraft:spruce_planks", 5, 2, 6),      # northern timber
    ]),
    mp(2, 3, [
        me("ergenverse:jade_slip", 7),
        me("ergenverse:spirit_stone", 6, 1, 3),
        me("minecraft:book", 5, 1, 2),
        me("minecraft:enchanted_book", 4),
        me("minecraft:experience_bottle", 4, 1, 2),
        me("minecraft:emerald", 4, 2, 5),
    ]),
    mp(1, 1, [
        me("ergenverse:spirit_stone", 5, 2, 4),
        me("ergenverse:dao_fragment", 2),
        me("minecraft:diamond", 3, 1, 2),
    ]),
])
write_npcs([
    npc("npc_scc_ice_guard_captain_xue", "Ice Guard Captain Xue", "冰卫队长雪",
        "Snow Country Military", "snow_country_capital", "NASCENT_SOUL_LATE",
        "stern, cold demeanor, loyal to Snow Country, views southerners as soft",
        "clipped, formal, references temperature frequently",
        "neutral-suspicious — southerners require observation",
        2,
        ["The Snow Country Capital does not welcome visitors from the south. You will find no warmth here — in any sense.",
         "Zhao Country cultivators complain about the cold. Good. Cold builds discipline. Their sects are weak because they are comfortable.",
         "A disciple from the Heavenly Fate Sect came once to read our 'fate in the stars.' He left within the hour. The cold cracked his star charts.",
         "Iron and coal. The garrison needs constant fuel and weapons. The cold degrades everything."],
        [{"time":"05:00","action":"Dawn patrol","location":"snow_country_capital","duration":150},
         {"time":"07:30","action":"Guard assembly","location":"snow_country_capital","duration":90},
         {"time":"09:00","action":"Inspect city walls","location":"snow_country_capital","duration":180},
         {"time":"12:00","action":"Meal","location":"snow_country_capital","duration":60},
         {"time":"13:00","action":"Train recruits in cold","location":"snow_country_capital","duration":180},
         {"time":"16:30","action":"Evening patrol","location":"snow_country_capital","duration":150},
         {"time":"20:00","action":"Night watch briefing","location":"snow_country_capital","duration":60},
         {"time":"21:00","action":"Night patrol","location":"snow_country_capital","duration":420}],
        [{"task_id":"scc_garrison_fuel","name":"Garrison Heating Fuel","description":"Captain Xue needs coal and iron to maintain the garrison in the perpetual cold.","required_items":{"minecraft:coal":10,"minecraft:iron_ingot":6},"rewards":{"ergenverse:jade_slip":1,"minecraft:emerald":8,"experience":25}},
         {"task_id":"scc_wall_repair","name":"City Wall Ice Repair","description":"Packed ice and spruce planks are needed to repair ice-damaged city wall sections.","required_items":{"minecraft:packed_ice":8,"minecraft:spruce_planks":10},"rewards":{"ergenverse:spirit_stone":2,"minecraft:emerald":10,"experience":30}}]),

    npc("npc_scc_frost_alchemist_han", "Frost Alchemist Han", "霜炼丹师韩",
        "Snow Country Alchemy Hall", "snow_country_capital", "NASCENT_SOUL_MID",
        "precise, believes cold enhances pill quality, dismisses southern alchemy as 'sauna brewing'",
        "precise, cold humor, compares everything to temperature",
        "neutral — interested in southern reagents she has never seen",
        2,
        ["A pill refined at sub-zero temperature is four times more potent. This is not theory. This is measurement.",
         "A Luo He Sect alchemist visited once. He tried to refine underwater. The cold here is better. Water freezes. Pills crystallize.",
         "The Vermilion Bird Capital sends us emeralds for our frost pills. They use them to cool temples. We use them to survive.",
         "Snowballs and glass bottles — the cold here is a reagent, not a hazard. Bring them and I will show you."],
        [{"time":"04:00","action":"Collect morning frost","location":"snow_country_capital","duration":120},
         {"time":"06:00","action":"Pill refinement","location":"snow_country_capital","duration":180},
         {"time":"09:00","action":"Analyze frost crystals","location":"snow_country_capital","duration":120},
         {"time":"12:00","action":"Meal","location":"snow_country_capital","duration":60},
         {"time":"13:00","action":"Afternoon pill refinement","location":"snow_country_capital","duration":180},
         {"time":"16:30","action":"Herb storage inspection","location":"snow_country_capital","duration":90},
         {"time":"19:00","action":"Record pill formulas","location":"snow_country_capital","duration":120},
         {"time":"22:00","action":"Rest","location":"snow_country_capital","duration":420}],
        [{"task_id":"scc_frost_pill_reagents","name":"Frost Pill Reagents","description":"Alchemist Han needs snowballs and glass bottles to refine frost-resistance pills.","required_items":{"minecraft:snowball":16,"minecraft:glass_bottle":5},"rewards":{"ergenverse:jade_slip":2,"minecraft:experience_bottle":2,"experience":30}},
         {"task_id":"scc_cold_furnace_fuel","name":"Cold Furnace Fuel","description":"Coal and blue ice are needed to maintain the sub-zero alchemical furnace.","required_items":{"minecraft:coal":8,"minecraft:blue_ice":4},"rewards":{"ergenverse:spirit_stone":2,"ergenverse:jade_slip":1,"experience":25}}]),

    npc("npc_scc_exiled_scholar_feng", "Exiled Scholar Feng", "流放学者冯",
        "none (exiled from Zhao Country)", "snow_country_capital", "FOUNDATION_PEAK",
        "bitter, intellectual, exiled for forbidden research, secretly content in the cold",
        "literary, sardonic, quotes banned texts",
        "neutral-sympathetic — recognizes fellow outcasts",
        2,
        ["I was exiled from Zhao Country for asking the wrong questions about karma. The Snow Country does not care about my questions. They care about survival. I prefer it.",
         "The Heng Yue Sect library had seven thousand scrolls. I read them all. Then I read the forbidden ones. That is why I am here.",
         "An elder from the Fighting Evil Sect visited the capital last year. He said the cold was 'a different kind of evil.' He was not wrong.",
         "Ink and paper. Even in exile, a scholar writes. Bring them and I will share what Zhao Country banned."],
        [{"time":"06:00","action":"Morning writing","location":"snow_country_capital","duration":180},
         {"time":"09:00","action":"Teach local children","location":"snow_country_capital","duration":120},
         {"time":"11:00","action":"Research in snow archives","location":"snow_country_capital","duration":150},
         {"time":"13:30","action":"Meal","location":"snow_country_capital","duration":60},
         {"time":"14:30","action":"Consult with frost alchemist","location":"snow_country_capital","duration":90},
         {"time":"16:30","action":"Evening lecture","location":"snow_country_capital","duration":120},
         {"time":"19:00","action":"Write by cold lamplight","location":"snow_country_capital","duration":180},
         {"time":"23:00","action":"Sleep","location":"snow_country_capital","duration":420}],
        [{"task_id":"scc_exiled_writing","name":"Exiled Scholar's Writing Materials","description":"Scholar Feng needs ink and paper to continue his forbidden research and record Snow Country's history.","required_items":{"minecraft:ink_sac":6,"minecraft:paper":10},"rewards":{"ergenverse:jade_slip":2,"minecraft:emerald":8,"experience":25}},
         {"task_id":"scc_forbidden_knowledge","name":"Forbidden Knowledge Exchange","description":"Feng trades banned Zhao Country knowledge for books and experience bottles.","required_items":{"minecraft:book":4,"minecraft:experience_bottle":3},"rewards":{"ergenverse:jade_slip":1,"minecraft:enchanted_book":1,"minecraft:emerald":6,"experience":30}}]),
])

# ============================================================
# 2. ANCIENT GOD CAVE (古神洞)
# Theme: Primordial power, ancient divinity, raw creation energy.
# In RI: A cave containing remnants of ancient god-level power.
# Extremely dangerous, extremely rewarding. A place where the
# fundamental laws of reality are thin.
# ============================================================
print("=== 2. Ancient God Cave ===")
write_loot("ancient_god_cave.json", [
    mp(4, 6, [
        me("minecraft:obsidian", 10, 3, 8),          # primordial stone
        me("minecraft:ender_pearl", 8, 1, 4),        # reality distortion
        me("minecraft:ancient_debris", 6, 1, 2),     # ancient material (1.16+)
        me("minecraft:netherrack", 7, 2, 5),         # primordial rock
        me("minecraft:gold_ingot", 5, 1, 3),         # divine currency
        me("minecraft:glowstone", 6, 2, 4),          # divine light
        me("minecraft:bone", 5, 2, 5),               # ancient remains
    ]),
    mp(2, 4, [
        me("ergenverse:jade_slip", 8),                # ancient knowledge
        me("ergenverse:spirit_stone", 7, 2, 5),      # divine energy
        me("ergenverse:dao_fragment", 3),             # primordial truth
        me("minecraft:enchanted_book", 5),
        me("minecraft:experience_bottle", 5, 1, 3),
        me("minecraft:diamond", 4, 1, 3),
    ]),
    mp(1, 2, [
        me("ergenverse:dao_fragment", 6, 1, 2),      # higher than normal
        me("ergenverse:spirit_stone", 5, 3, 6),
        me("minecraft:nether_star", 2),
        me("minecraft:ender_pearl", 4, 1, 3),
    ]),
])
write_npcs([
    npc("npc_agc_primordial_guardian", "Primordial Guardian", "原始守卫",
        "Ancient God Remnant", "ancient_god_cave", "SOUL_TRANSFORMATION_EARLY",
        "ageless, speaks in geological time, barely recognizes living beings as significant",
        "slow, resonant, sentences span centuries",
        "neutral-indifferent — all living things are brief",
        2,
        ["This cave existed before the mountain. The mountain existed before the sects. The sects believe they are old.",
         "The last being I spoke to was a Soul Refining elder. He sought to capture the god's residual soul. I did not permit it. He did not survive the refusal.",
         "The Immortal Emperor's Cave Mansion is young. Three thousand years. This cave is older than the concept of 'emperor.'",
         "Obsidian and ender pearls. The cave's reality-thin walls require constant reinforcement. Bring them or watch existence fray."],
        [{"time":"00:00","action":"Patrol cave boundaries","location":"ancient_god_cave","duration":360},
         {"time":"06:00","action":"Reality reinforcement","location":"ancient_god_cave","duration":180},
         {"time":"09:00","action":"Commune with cave","location":"ancient_god_cave","duration":240},
         {"time":"14:00","action":"Inspect god remnants","location":"ancient_god_cave","duration":180},
         {"time":"18:00","action":"Evening vigil","location":"ancient_god_cave","duration":240},
         {"time":"23:00","action":"Deep meditation","location":"ancient_god_cave","duration":60}],
        [{"task_id":"agc_reality_reinforcement","name":"Reality Wall Reinforcement","description":"The Primordial Guardian needs obsidian and ender pearls to reinforce the cave's thinning reality walls.","required_items":{"minecraft:obsidian":10,"minecraft:ender_pearl":4},"rewards":{"ergenverse:dao_fragment":2,"ergenverse:spirit_stone":5,"experience":50}},
         {"task_id":"agc_god_remnant_preservation","name":"God Remnant Preservation","description":"Glowstone and gold are needed to preserve the ancient god's residual energy patterns.","required_items":{"minecraft:glowstone":8,"minecraft:gold_ingot":6},"rewards":{"ergenverse:jade_slip":3,"ergenverse:spirit_stone":4,"experience":40}}]),

    npc("npc_agc_researcher_bai", "Cave Researcher Bai", "洞府研究者白",
        "Vermilion Bird Imperial Academy", "ancient_god_cave", "NASCENT_SOUL_MID",
        "obsessive, reckless, willing to die for knowledge, treats danger as data",
        "rapid, excited, takes notes mid-sentence",
        "neutral-friendly — happy to share findings with anyone interested",
        2,
        ["The Vermilion Bird Capital sent me to study this cave. They said 'be careful.' I have been careful. I have also been nearly killed fourteen times.",
         "The god who died here did not 'die' in any sense we understand. Its body became the cave. Its breath became the distortion fields.",
         "A Xuan Dao Sect philosopher came once. He said the cave proved Dao is emergent from chaos. The Primordial Guardian said nothing. The philosopher left. The Guardian continued saying nothing.",
         "Netherrack and bone — the cave's building materials. Bring them and I will share my fourteen near-death findings."],
        [{"time":"05:00","action":"Morning cave survey","location":"ancient_god_cave","duration":150},
         {"time":"07:30","action":"Collect samples","location":"ancient_god_cave","duration":180},
         {"time":"10:30","action":"Analyze distortion fields","location":"ancient_god_cave","duration":150},
         {"time":"13:00","action":"Meal (dried rations)","location":"ancient_god_cave","duration":60},
         {"time":"14:00","action":"Record findings","location":"ancient_god_cave","duration":180},
         {"time":"17:30","action":"Probe deep cave","location":"ancient_god_cave","duration":150},
         {"time":"21:00","action":"Night analysis","location":"ancient_god_cave","duration":180},
         {"time":"00:00","action":"Brief sleep","location":"ancient_god_cave","duration":300}],
        [{"task_id":"agc_sample_collection","name":"Cave Sample Collection","description":"Researcher Bai needs netherrack and bone to continue her analysis of the ancient god's remains.","required_items":{"minecraft:netherrack":10,"minecraft:bone":8},"rewards":{"ergenverse:jade_slip":2,"minecraft:enchanted_book":1,"experience":30}},
         {"task_id":"agc_distortion_measurement","name":"Distortion Field Measurement","description":"Ender pearls and experience bottles are needed to measure and record the cave's reality distortion fields.","required_items":{"minecraft:ender_pearl":4,"minecraft:experience_bottle":3},"rewards":{"ergenverse:jade_slip":1,"ergenverse:spirit_stone":2,"minecraft:emerald":8,"experience":35}}]),

    npc("npc_agc_pilgrim_shen", "God-Cave Pilgrim Shen", "古神洞朝圣者沈",
        "none (independent seeker)", "ancient_god_cave", "CONDENSATION_LATE",
        "reverent, seeks enlightenment from the ancient god's remnants, peaceful",
        "soft, contemplative, pauses to listen to the cave",
        "neutral-friendly — fellow seeker",
        2,
        ["I have traveled from the Corpse Yin Sect's territory to reach this cave. They said a dead god has no wisdom. They are wrong.",
         "The Heng Yue Sect meditates on mountains. The Luo He Sect meditates on rivers. I meditate on what existed before mountains and rivers.",
         "A Cultivator from the Thunder Celestial Temple came seeking lightning in the god's remnants. He found silence. He said it was the loudest silence he had ever heard.",
         "Coal and books. Even a pilgrim must eat and read. The cave provides silence. It does not provide lunch."],
        [{"time":"04:00","action":"Pre-dawn meditation","location":"ancient_god_cave","duration":180},
         {"time":"07:00","action":"Morning pilgrimage circuit","location":"ancient_god_cave","duration":180},
         {"time":"10:00","action":"Contemplate god remnants","location":"ancient_god_cave","duration":180},
         {"time":":13:00","action":"Meal","location":"ancient_god_cave","duration":60},
         {"time":"14:00","action":"Study cave inscriptions","location":"ancient_god_cave","duration":150},
         {"time":"17:00","action":"Evening meditation","location":"ancient_god_cave","duration":180},
         {"time":"21:00","action":"Sleep near god remnants","location":"ancient_god_cave","duration":420}],
        [{"task_id":"agc_pilgrim_supplies","name":"Pilgrim Provisions","description":"Pilgrim Shen needs coal and books to sustain his meditation and study in the cave.","required_items":{"minecraft:coal":8,"minecraft:book":3},"rewards":{"ergenverse:jade_slip":1,"minecraft:emerald":6,"experience":20}},
         {"task_id":"agc_inscription_study","name":"Incription Rubbing Materials","description":"Paper and ink are needed to create rubbings of the ancient god's cave wall inscriptions.","required_items":{"minecraft:paper":8,"minecraft:ink_sac":4},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":1,"minecraft:experience_bottle":2,"experience":25}}]),
])

# ============================================================
# 3. IMMORTAL COURT PALACE (仙朝宫殿)
# Theme: Bureaucratic celestial governance, ancient law, records.
# In RI: The Immortal Court (仙朝) is the governing body above sects.
# The palace handles planetary-scale administration and ancient laws.
# ============================================================
print("=== 3. Immortal Court Palace ===")
write_loot("immortal_court_palace.json", [
    mp(3, 5, [
        me("minecraft:gold_ingot", 10, 2, 6),        # imperial gold
        me("minecraft:paper", 8, 3, 8),               # court documents
        me("minecraft:ink_sac", 6, 1, 3),             # court records
        me("minecraft:emerald", 7, 2, 5),             # court treasury
        me("minecraft:book", 7, 1, 3),
        me("minecraft:iron_ingot", 5, 2, 4),
        me("minecraft:quartz", 5, 2, 5),              # celestial stone
    ]),
    mp(2, 3, [
        me("ergenverse:jade_slip", 8),
        me("ergenverse:spirit_stone", 6, 1, 3),
        me("minecraft:enchanted_book", 5),
        me("minecraft:experience_bottle", 4, 1, 2),
        me("minecraft:diamond", 3, 1, 2),
    ]),
    mp(1, 1, [
        me("ergenverse:spirit_stone", 5, 2, 4),
        me("ergenverse:dao_fragment", 3),
        me("minecraft:nether_star", 1),
    ]),
])
write_npcs([
    npc("npc_icp_court_magistrate_zhou", "Court Magistrate Zhou", "仙朝判官周",
        "Immortal Court", "immortal_court_palace", "NASCENT_SOUL_PEAK",
        "imposing, bureaucratic, believes the Court is the only legitimate authority",
        "formal, legalistic, cites court precedents",
        "neutral-authoritative — all are subject to Immortal Court jurisdiction",
        2,
        ["You stand in the Immortal Court Palace. All cultivator disputes on Planet Suzaku fall under our jurisdiction. All of them.",
         "The sects believe they are sovereign. They are not. They are registered organizations subject to Court law.",
         "The Vermilion Bird Capital and the Immortal Court have coexisted for four thousand years. The Bird provides power. The Court provides order.",
         "Paper and ink. The Court runs on documentation, not cultivation. Bring them."],
        [{"time":"05:00","action":"Review overnight petitions","location":"immortal_court_palace","duration":120},
         {"time":"07:00","action":"Morning court session","location":"immortal_court_palace","duration":180},
         {"time":"10:00","action":"Hear sect disputes","location":"immortal_court_palace","duration":180},
         {"time":"13:00","action":"Meal","location":"immortal_court_palace","duration":60},
         {"time":"14:00","action":"Review provincial reports","location":"immortal_court_palace","duration":150},
         {"time":"17:00","action":"Issue court rulings","location":"immortal_court_palace","duration":120},
         {"time":"20:00","action":"Evening court session","location":"immortal_court_palace","duration":120},
         {"time":"23:00","action":"Rest","location":"immortal_court_palace","duration":360}],
        [{"task_id":"icp_court_documentation","name":"Court Documentation Supply","description":"Magistrate Zhou needs paper and ink to process the Court's backlog of planetary disputes.","required_items":{"minecraft:paper":12,"minecraft:ink_sac":6},"rewards":{"ergenverse:jade_slip":2,"minecraft:emerald":10,"experience":25}},
         {"task_id":"icp_provincial_records","name":"Provincial Record Compilation","description":"Books and quartz are needed to compile provincial cultivation records for Court review.","required_items":{"minecraft:book":5,"minecraft:quartz":8},"rewards":{"ergenverse:spirit_stone":2,"ergenverse:jade_slip":1,"minecraft:emerald":8,"experience":30}}]),

    npc("npc_icp_record_keeper_sun", "Record Keeper Sun", "档案保管者孙",
        "Immortal Court Archives", "immortal_court_palace", "NASCENT_SOUL_EARLY",
        "meticulous, secretly knows where all the bodies are buried (metaphorically), values records above all",
        "precise, references specific case numbers, slightly conspiratorial",
        "neutral-talkative — trades information freely",
        2,
        ["I have read every court record for the past six hundred years. There are things in these archives that would topple three sects.",
         "The Soul Refining Sect's charter permits 'soul acquisition from hostile entities.' The Court has never defined 'hostile.' I find this interesting.",
         "A Heng Yue Sect elder once filed a petition against the Teng family. The case lasted forty years. He died. The Teng family won. The records are in aisle seven.",
         "Gold and emeralds. The archives accept the Court's standard currency. Knowledge costs extra."],
        [{"time":"06:00","action":"Archive organization","location":"immortal_court_palace","duration":150},
         {"time":"08:30","action":"Retrieve records for court","location":"immortal_court_palace","duration":150},
         {"time":"11:00","action":"File new records","location":"immortal_court_palace","duration":120},
         {"time":"13:00","action":"Meal","location":"immortal_court_palace","duration":60},
         {"time":"14:00","action":"Secret reading","location":"immortal_court_palace","duration":180},
         {"time":"17:30","action":"Archive maintenance","location":"immortal_court_palace","duration":120},
         {"time":"20:00","action":"Index new entries","location":"immortal_court_palace","duration":150},
         {"time":":23:00","action":"Rest","location":"immortal_court_palace","duration":420}],
        [{"task_id":"icp_archive_maintenance","name":"Archive Maintenance","description":"Keeper Sun needs gold and emeralds to maintain the ancient archive preservation formations.","required_items":{"minecraft:gold_ingot":8,"minecraft:emerald":10},"rewards":{"ergenverse:jade_slip":2,"minecraft:book":3,"experience":25}},
         {"task_id":"icp_sensitive_records","name":"Sensitive Record Access","description":"For gold and books, Sun will provide access to restricted court records containing sect secrets.","required_items":{"minecraft:gold_ingot":6,"minecraft:book":4},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":1,"minecraft:enchanted_book":1,"experience":35}}]),

    npc("npc_icp_provincial_envoy_li", "Provincial Envoy Li", "省域使者李",
        "Immortal Court (Zhao Province)", "immortal_court_palace", "NASCENT_SOUL_MID",
        "diplomatic, weary from travel, knows every province's politics",
        "measured, diplomatic, avoids committing to positions",
        "neutral-friendly — useful contact for provincial information",
        2,
        ["I am the Court's envoy to Zhao Province. It is the most troublesome province on the planet. And I have been to all twenty-three.",
         "The Corpse Yin Sect operates in a legal gray area. The Court has ruled three times on their practices. They comply with the rulings they agree with.",
         "The Fighting Evil Sect requested Court recognition as a 'planetary defense force.' The request is in committee. It has been in committee for sixty years.",
         "Emeralds and iron — the currency of diplomacy. I need supplies for my next provincial circuit."],
        [{"time":"05:00","action":"Prepare dispatches","location":"immortal_court_palace","duration":120},
         {"time":"07:00","action":"Attend morning court","location":"immortal_court_palace","duration":120},
         {"time":"09:00","action":"Meet with provincial representatives","location":"immortal_court_palace","duration":180},
         {"time":"12:00","action":"Diplomatic lunch","location":"immortal_court_palace","duration":90},
         {"time":"14:00","action":"Draft provincial reports","location":"immortal_court_palace","duration":180},
         {"time":"17:00","action":"Evening audience with magistrate","location":"immortal_court_palace","duration":90},
         {"time":"19:00","action":"Social gathering","location":"immortal_court_palace","duration":120},
         {"time":"22:00","action":"Rest","location":"immortal_court_palace","duration":360}],
        [{"task_id":"icp_diplomatic_supplies","name":"Diplomatic Circuit Supplies","description":"Envoy Li needs emeralds and iron for his next provincial circuit across Planet Suzaku.","required_items":{"minecraft:emerald":12,"minecraft:iron_ingot":8},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":2,"experience":30}},
         {"task_id":"icp_intelligence_trade","name":"Provincial Intelligence Trade","description":"Li trades provincial secrets for emeralds and books — useful for navigating sect politics.","required_items":{"minecraft:emerald":10,"minecraft:book":3},"rewards":{"ergenverse:jade_slip":1,"minecraft:enchanted_book":1,"minecraft:emerald":8,"experience":25}}]),
])

# ============================================================
# 4. KARMA CRYSTAL FORMATION (因果晶阵)
# Theme: Karma, cause and effect, crystallized fate.
# In RI: Karma is one of the deepest themes. The karma crystal formation
# is where cause-and-effect becomes visible and manipulable.
# ============================================================
print("=== 4. Karma Crystal Formation ===")
write_loot("karma_crystal_formation.json", [
    mp(3, 5, [
        me("minecraft:amethyst_shard", 10, 2, 6),   # karma crystal
        me("minecraft:quartz", 9, 2, 5),              # crystallized cause
        me("minecraft:ender_pearl", 7, 1, 3),         # karmic entanglement
        me("minecraft:prismarine_shard", 6, 1, 3),    # karmic residue
        me("minecraft:emerald", 5, 1, 4),
        me("minecraft:glowstone", 5, 1, 3),           # karmic illumination
        me("minecraft:iron_ingot", 4, 1, 3),
    ]),
    mp(2, 3, [
        me("ergenverse:jade_slip", 8),
        me("ergenverse:spirit_stone", 6, 1, 3),
        me("minecraft:book", 5, 1, 2),
        me("minecraft:enchanted_book", 4),
        me("minecraft:experience_bottle", 5, 1, 2),
    ]),
    mp(1, 1, [
        me("ergenverse:dao_fragment", 5),
        me("ergenverse:spirit_stone", 4, 2, 4),
        me("minecraft:nether_star", 1),
    ]),
])
write_npcs([
    npc("npc_kcf_karma_observer_chen", "Karma Observer Chen", "因果观察者陈",
        "none (independent karma scholar)", "karma_crystal_formation", "NASCENT_SOUL_PEAK",
        "serene, detached, sees all actions as threads in a web",
        "calm, philosophical, speaks in cause-and-effect chains",
        "neutral-observant — watches all visitors' karmic threads",
        2,
        ["Every action you have taken led you here. Every action you take here will lead somewhere else. This is not philosophy. It is visible.",
         "The Heavenly Fate Sect reads stars to predict karma. They are looking at the reflection. We are looking at the water.",
         "The Xuan Dao Sect's founder understood karma better than anyone. He chose not to build here. He said 'some things should not be crystallized.'",
         "Amethyst shards and quartz — the physical material of karma. Bring them and I will show you your thread."],
        [{"time":"04:00","action":"Observe karmic threads","location":"karma_crystal_formation","duration":240},
         {"time":"08:00","action":"Meditate among crystals","location":"karma_crystal_formation","duration":180},
         {"time":"11:00","action":"Teach karma reading","location":"karma_crystal_formation","duration":120},
         {"time":"13:00","action":"Meal (minimal)","location":"karma_crystal_formation","duration":60},
         {"time":"14:00","action":"Record karmic observations","location":"karma_crystal_formation","duration":180},
         {"time":"17:30","action":"Crystal maintenance","location":"karma_crystal_formation","duration":120},
         {"time":"20:00","action":"Night karmic observation","location":"karma_crystal_formation","duration":240},
         {"time":":23:00","action":"Rest","location":"karma_crystal_formation","duration":360}],
        [{"task_id":"kcf_crystal_maintenance","name":"Karma Crystal Maintenance","description":"Observer Chen needs amethyst shards and quartz to maintain the karma crystal formation.","required_items":{"minecraft:amethyst_shard":10,"minecraft:quartz":8},"rewards":{"ergenverse:dao_fragment":1,"ergenverse:jade_slip":2,"experience":40}},
         {"task_id":"kcf_thread_reading","name":"Karmic Thread Reading","description":"Ender pearls and glowstone are needed to illuminate and read visitors' karmic threads.","required_items":{"minecraft:ender_pearl":4,"minecraft:glowstone":6},"rewards":{"ergenverse:jade_slip":2,"ergenverse:spirit_stone":2,"minecraft:experience_bottle":2,"experience":35}}]),

    npc("npc_kcf_crystal_miner_zhao", "Crystal Miner Zhao", "晶矿工赵",
        "none (independent miner)", "karma_crystal_formation", "CONDENSATION_PEAK",
        "pragmatic, has learned to sense karma through mining, surprisingly wise",
        "blunt, practical, occasionally accidentally profound",
        "neutral-friendly — trades crystals for supplies",
        2,
        ["I mine karma crystals. Twenty years. I have learned more about cause and effect from a pickaxe than most scholars learn from books.",
         "The crystals grow when someone nearby makes a significant choice. I cannot see the choice. I see the crystal grow. It is enough.",
         "A merchant from Tian Shui City came to buy crystals for divination. I sold him twelve. He said they were 'the most accurate he had ever used.' I do not know what that means.",
         "Iron and emeralds. Mining tools and payment. I am simple. The crystals are not."],
        [{"time":"05:00","action":"Begin mining shift","location":"karma_crystal_formation","duration":240},
         {"time":"09:00","action":"Sort crystals","location":"karma_crystal_formation","duration":120},
         {"time":"11:00","action":"Trade with visitors","location":"karma_crystal_formation","duration":120},
         {"time":"13:00","action":"Meal","location":"karma_crystal_formation","duration":60},
         {"time":"14:00","action":"Afternoon mining","location":"karma_crystal_formation","duration":180},
         {"time":"18:00","action":"Clean and count crystals","location":"karma_crystal_formation","duration":120},
         {"time":"21:00","action":"Rest","location":"karma_crystal_formation","duration":420}],
        [{"task_id":"kcf_mining_supplies","name":"Mining Tool Supply","description":"Miner Zhao needs iron and emeralds to maintain mining equipment and purchase crystal rights.","required_items":{"minecraft:iron_ingot":8,"minecraft:emerald":8},"rewards":{"ergenverse:jade_slip":1,"minecraft:amethyst_shard":12,"experience":20}},
         {"task_id":"kcf_crystal_trade","name":"Crystal Trade","description":"Zhao trades karma crystals for prismarine shards and emeralds — raw karmic materials.","required_items":{"minecraft:prismarine_shard":8,"minecraft:emerald":6},"rewards":{"ergenverse:jade_slip":2,"minecraft:quartz":10,"experience":25}}]),

    npc("npc_kcf_redemption_seeker_liu", "Redemption Seeker Liu", "赎罪者刘",
        "none (seeking karmic redemption)", "karma_crystal_formation", "FOUNDATION_LATE",
        "tormented, seeks to understand and atone for past sins through karma study",
        "quiet, intense, speaks about guilt and causation",
        "neutral-desperate — seeks understanding of own karma",
        2,
        ["I came here because I heard the crystals can show you what you have done. I need to see it.",
         "The Corpse Yin Sect trades in bodies. We trade in consequences. Both are currencies of death. Ours is more honest.",
         "A cultivator from the Heng Yue Sect came seeking redemption for killing a fellow disciple. The crystals showed him nothing. He left relieved. I think the crystals were showing him that nothing needed forgiveness.",
         "Books and experience bottles. I study karma theory. Perhaps understanding will bring peace."],
        [{"time":"05:00","action":"Meditate near crystals","location":"karma_crystal_formation","duration":180},
         {"time":"08:00","action":"Study karma texts","location":"karma_crystal_formation","duration":180},
         {"time":"11:00","action":"Attempt to read own karma","location":"karma_crystal_formation","duration":120},
         {"time":"13:00","action":"Meal (barely eats)","location":"karma_crystal_formation","duration":60},
         {"time":"14:00","action":"Help miner with crystals","location":"karma_crystal_formation","duration":150},
         {"time":"17:00","action":"Evening meditation","location":"karma_crystal_formation","duration":180},
         {"time":"21:00","action":"Journal writing","location":"karma_crystal_formation","duration":120},
         {"time":"24:00","action":"Rest","location":"karma_crystal_formation","duration":300}],
        [{"task_id":"kcf_karma_study","name":"Karma Theory Study","description":"Liu needs books and experience bottles to study karma theory and seek understanding.","required_items":{"minecraft:book":4,"minecraft:experience_bottle":3},"rewards":{"ergenverse:jade_slip":1,"minecraft:emerald":6,"experience":20}},
         {"task_id":"kcf_crystal_meditation","name":"Crystal Meditation Aid","description":"Glowstone and emeralds help Liu maintain focus during long crystal meditation sessions.","required_items":{"minecraft:glowstone":6,"minecraft:emerald":8},"rewards":{"ergenverse:jade_slip":2,"minecraft:amethyst_shard":8,"experience":25}}]),
])

print("\nAUTO-CANON-045: 4 special locations batch 2 complete.")
print(f"  4 new loot tables, 12 new NPCs")