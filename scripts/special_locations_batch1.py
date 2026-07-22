#!/usr/bin/env python3
"""
AUTO-CANON-044: Special locations batch 1 — top 4 canon-significance single-structure locations.
All 14 special locations are single-jigsaw (1 structure, 1 template). Much smaller than sects.
This cycle completes the top 4 by canon significance:
  1. immortal_emperor_cave_mansion — 1 new loot + 3 NPCs (Wang Lin's major discovery)
  2. soul_refining_ancestral — 0 new loot (orphan exists) + 3 NPCs (Soul Banner origin)
  3. thunder_celestial_temple — 1 new loot + 3 NPCs (thunder cultivation hub)
  4. vermilion_bird_capital — 1 new loot + 3 NPCs (Planet Suzaku namesake capital)
Total: 3 new loot tables + 12 INFERRED NPCs.
"""
import json
import os
import hashlib

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse"
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
NPC_DIR = os.path.join(BASE, "npcs")

def make_entry(name, weight, min_c=1, max_c=1):
    e = {"type": "minecraft:item", "weight": weight, "name": name}
    if min_c != 1 or max_c != 1:
        e["functions"] = [{"function": "minecraft:set_count", "count": {"min": min_c, "max": max_c}}]
    return e

def make_pool(rolls_min, rolls_max, entries):
    return {"rolls": {"min": rolls_min, "max": rolls_max}, "entries": entries}

def make_npc(npc_id, name, nameCn, faction, location, cultivation,
             personality, speech, relationship, canon_confidence,
             initiation_lines, daily_schedule, sect_tasks, npc_type="sect_disciple"):
    salt = hashlib.sha256(npc_id.encode()).hexdigest()[:16]
    return {
        "npc_id": npc_id, "name": name, "nameCn": nameCn, "canon_id": "",
        "type": npc_type, "faction": faction, "location": location,
        "cultivation": cultivation, "personality": personality, "speech": speech,
        "relationship_to_wanglin": relationship,
        "dialogue_available": True, "quest_available": True,
        "trade_available": True, "teaching_available": True,
        "canon_confidence": canon_confidence, "derivation_type": "I", "salt": salt,
        "initiation_lines": initiation_lines, "daily_schedule": daily_schedule,
        "sect_tasks": sect_tasks, "_xianxia_schema": 1
    }

# ============================================================
# 1. IMMORTAL EMPEROR CAVE MANSION
# Theme: Ancient immortal legacy, spatial distortion, forgotten power.
# In RI: Wang Lin discovers an Immortal Emperor's hidden cave mansion,
# one of the most pivotal early-story events. Contains ancient formations,
# traps, treasures, and the Emperor's residual knowledge.
# ============================================================

def write_immortal_emperor_loot():
    """MAX-tier loot. The Immortal Emperor's treasure vault."""
    table = {
        "pools": [
            # Pool 1: Ancient immortal materials (4-6 rolls)
            make_pool(4, 6, [
                make_entry("minecraft:netherite_ingot", 8, 1, 3),       # immortal metal
                make_entry("minecraft:ender_pearl", 7, 1, 4),           # spatial distortion
                make_entry("minecraft:diamond", 7, 2, 5),              # immortal wealth
                make_entry("minecraft:obsidian", 6, 3, 8),             # formation material
                make_entry("minecraft:gold_ingot", 5, 2, 6),           # ancient currency
                make_entry("minecraft:nether_star", 2),                 # immortal essence
                make_entry("minecraft:bone", 4, 2, 5),                 # ancient remains
            ]),
            # Pool 2: Knowledge and power (2-4 rolls)
            make_pool(2, 4, [
                make_entry("ergenverse:jade_slip", 8),                 # ancient knowledge
                make_entry("ergenverse:spirit_stone", 7, 2, 5),       # cultivation resource
                make_entry("minecraft:enchanted_book", 5),
                make_entry("minecraft:book", 6, 1, 3),
                make_entry("minecraft:experience_bottle", 5, 1, 3),
                make_entry("minecraft:ender_pearl", 4, 1, 2),
                make_entry("minecraft:emerald", 3, 2, 6),
            ]),
            # Pool 3: Legendary (1-2 rolls)
            make_pool(1, 2, [
                make_entry("ergenverse:dao_fragment", 5, 1, 2),        # ultimate knowledge
                make_entry("ergenverse:spirit_stone", 4, 2, 4),
                make_entry("minecraft:nether_star", 3),
                make_entry("minecraft:diamond", 4, 2, 4),
                make_entry("minecraft:netherite_ingot", 3, 1, 2),
            ]),
        ]
    }
    path = os.path.join(LOOT_DIR, "immortal_emperor_cave_mansion.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(table, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {path}")


def write_immortal_emperor_npcs():
    """3 INFERRED NPCs for the Immortal Emperor's Cave Mansion."""
    npcs = [
        make_npc(
            npc_id="npc_iem_array_puppet_guardian",
            name="Array Puppet Guardian",
            nameCn="阵法傀儡守卫",
            faction="Immortal Emperor Legacy",
            location="immortal_emperor_cave_mansion",
            cultivation="NASCENT_SOUL_PEAK",
            personality="mechanical, loyal to dead master, attacks intruders then offers tests",
            speech="stilted, ancient formal, pauses between phrases as if processing",
            relationship="hostile until proven worthy — tests all who enter",
            canon_confidence=2,
            initiation_lines=[
                "This mansion belongs to the Immortal Emperor. Your presence is... unauthorized. However.",
                "Three hundred cultivators have entered. Two hundred ninety-seven died. You may be the three hundredth.",
                "The Emperor left puzzles, not walls. Solve them and you earn the right to breathe his air.",
                "Bring me obsidian and ender pearls. The formations are weakening. I require materials to maintain them."
            ],
            daily_schedule=[
                {"time": "00:00", "action": "Patrol mansion perimeter", "location": "immortal_emperor_cave_mansion", "duration": 360},
                {"time": "06:00", "action": "Formation maintenance", "location": "immortal_emperor_cave_mansion", "duration": 180},
                {"time": "09:00", "action": "Test intruders", "location": "immortal_emperor_cave_mansion", "duration": 240},
                {"time": "13:00", "action": "Formation maintenance", "location": "immortal_emperor_cave_mansion", "duration": 180},
                {"time": "16:00", "action": "Record visitor attempts", "location": "immortal_emperor_cave_mansion", "duration": 120},
                {"time": "19:00", "action": "Deep patrol", "location": "immortal_emperor_cave_mansion", "duration": 300},
                {"time": "23:00", "action": "Dormant mode", "location": "immortal_emperor_cave_mansion", "duration": 60},
            ],
            sect_tasks=[
                {
                    "task_id": "iem_formation_repair",
                    "name": "Formation Repair Materials",
                    "description": "The puppet guardian needs obsidian and ender pearls to repair the weakening spatial formations.",
                    "required_items": {"minecraft:obsidian": 8, "minecraft:ender_pearl": 3},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 3, "experience": 40}
                },
                {
                    "task_id": "iem_puppet_maintenance",
                    "name": "Puppet Self-Maintenance",
                    "description": "The ancient puppet needs gold and iron to repair its deteriorating frame.",
                    "required_items": {"minecraft:gold_ingot": 5, "minecraft:iron_ingot": 8},
                    "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 10, "experience": 30}
                },
            ]
        ),
        make_npc(
            npc_id="npc_iem_trapped_explorer_lin",
            name="Trapped Explorer Lin",
            nameCn="被困探索者林",
            faction="none (rogue cultivator)",
            location="immortal_emperor_cave_mansion",
            cultivation="FOUNDATION_LATE",
            personality="desperate, knowledgeable about the mansion's traps, secretly hoarding treasures",
            speech="nervous, rapid, glances over shoulder, whispers",
            relationship="cautiously friendly — needs help escaping",
            canon_confidence=2,
            initiation_lines=[
                "You are not a puppet. Thank the heavens. I have been here for six months.",
                "The formations shift every three days. I have mapped forty percent of the patterns. Want to trade?",
                "I came from the Heng Yue Sect. They said I was foolish to come alone. They were correct.",
                "If you can bring me ender pearls and books, I can translate the Emperor's wall inscriptions."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Study wall inscriptions", "location": "immortal_emperor_cave_mansion", "duration": 240},
                {"time": "09:00", "action": "Probe formation boundaries", "location": "immortal_emperor_cave_mansion", "duration": 180},
                {"time": "12:00", "action": "Rationed meal", "location": "immortal_emperor_cave_mansion", "duration": 60},
                {"time": "13:00", "action": "Map formation patterns", "location": "immortal_emperor_cave_mansion", "duration": 240},
                {"time": "17:00", "action": "Avoid puppet patrol", "location": "immortal_emperor_cave_mansion", "duration": 120},
                {"time": "19:00", "action": "Rest in safe corner", "location": "immortal_emperor_cave_mansion", "duration": 600},
            ],
            sect_tasks=[
                {
                    "task_id": "iem_inscription_translation",
                    "name": "Emperor's Inscription Translation",
                    "description": "Explorer Lin needs ender pearls and books to translate ancient wall inscriptions about formation patterns.",
                    "required_items": {"minecraft:ender_pearl": 4, "minecraft:book": 3},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "experience": 35}
                },
                {
                    "task_id": "iem_escape_planning",
                    "name": "Escape Route Planning",
                    "description": "Lin needs diamonds and obsidian to construct a formation breaker for escaping the mansion.",
                    "required_items": {"minecraft:diamond": 4, "minecraft:obsidian": 6},
                    "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 12, "experience": 40}
                },
            ]
        ),
        make_npc(
            npc_id="npc_iem_residual_spirit_wang",
            name="Residual Spirit of the Emperor's Servant",
            nameCn="仙帝侍者残灵",
            faction="Immortal Emperor Legacy",
            location="immortal_emperor_cave_mansion",
            cultivation="SOUL_FORMATION_EARLY",
            personality="faded, speaks in riddles, sometimes forgets it is dead",
            speech="echoing, fragmented sentences, ancient diction",
            relationship="neutral — offers cryptic guidance to those who prove respectful",
            canon_confidence=2,
            initiation_lines=[
                "The Emperor... departed. Long ago. I remain to guard. Or perhaps I forgot to leave.",
                "This mansion exists in folded space. Time moves differently here. An hour outside is a day within.",
                "The Soul Refining Sect sought this place once. Their elder could not break the first gate.",
                "Netherite. The Emperor called it 'immortal bone.' Bring it to me and I will share what the walls remember."
            ],
            daily_schedule=[
                {"time": "00:00", "action": "Wander mansion corridors", "location": "immortal_emperor_cave_mansion", "duration": 480},
                {"time": "08:00", "action": "Fade into walls", "location": "immortal_emperor_cave_mansion", "duration": 240},
                {"time": "12:00", "action": "Manifest near treasure vault", "location": "immortal_emperor_cave_mansion", "duration": 120},
                {"time": "14:00", "action": "Wander mansion corridors", "location": "immortal_emperor_cave_mansion", "duration": 360},
                {"time": "20:00", "action": "Speak to walls", "location": "immortal_emperor_cave_mansion", "duration": 180},
                {"time": "23:00", "action": "Dissipate briefly", "location": "immortal_emperor_cave_mansion", "duration": 60},
            ],
            sect_tasks=[
                {
                    "task_id": "iem_immortal_bone_offering",
                    "name": "Immortal Bone Offering",
                    "description": "The residual spirit requires netherite ('immortal bone') and diamonds to strengthen its fading consciousness.",
                    "required_items": {"minecraft:netherite_ingot": 3, "minecraft:diamond": 5},
                    "rewards": {"ergenverse:dao_fragment": 1, "ergenverse:spirit_stone": 4, "experience": 50}
                },
                {
                    "task_id": "iem_wall_knowledge",
                    "name": "Wall Memory Recovery",
                    "description": "The spirit needs experience bottles and emeralds to recover fragmented memories from the mansion's walls.",
                    "required_items": {"minecraft:experience_bottle": 4, "minecraft:emerald": 8},
                    "rewards": {"ergenverse:jade_slip": 3, "ergenverse:spirit_stone": 2, "experience": 45}
                },
            ]
        ),
    ]
    for npc in npcs:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {len(npcs)} immortal_emperor NPCs")


# ============================================================
# 2. SOUL REFINING ANCESTRAL
# Theme: Soul fragments, dark spiritual energy, origin of Ten Billion Soul Banner.
# In RI: The ancestral ground of the Soul Refining Sect, deep in the Sea of Devils.
# Contains massive soul energy and is the origin of the sect's ultimate artifact.
# Has 1 pre-existing MAX-tier orphan loot table — keep it, add NPCs only.
# ============================================================

def write_soul_refining_ancestral_npcs():
    """3 INFERRED NPCs for the Soul Refining Ancestral Ground."""
    npcs = [
        make_npc(
            npc_id="npc_sra_soul_warden_qiu",
            name="Soul Warden Qiu",
            nameCn="魂卫邱",
            faction="Soul Refining Sect (Ancestral)",
            location="soul_refining_ancestral",
            cultivation="NASCENT_SOUL_PEAK",
            personality="obsessed with soul collection, views all living things as potential soul material",
            speech="whispery, occasionally speaks in plural 'we'",
            relationship="neutral-dangerous — evaluates visitors as soul sources",
            canon_confidence=2,
            initiation_lines=[
                "The souls here number in the billions. Each one has a name. I have forgotten most of them.",
                "The Soul Refining Sect sends one elder per decade to maintain the ancestral ground. I am the sixty-third.",
                "Corpse Yin Sect elders visit sometimes. They say our souls are 'improperly stored.' We say their corpses are 'poorly organized.'",
                "Spirit stones and bone. The currency of this place is not emeralds. It is memory."
            ],
            daily_schedule=[
                {"time": "04:00", "action": "Soul census", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "07:00", "action": "Inspect soul banners", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "10:00", "action": "Collect stray soul fragments", "location": "soul_refining_ancestral", "duration": 150},
                {"time": "13:00", "action": "Meal (alone)", "location": "soul_refining_ancestral", "duration": 60},
                {"time": "14:00", "action": "Meditate with soul energy", "location": "soul_refining_ancestral", "duration": 240},
                {"time": "18:00", "action": "Record soul fluctuations", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "22:00", "action": "Night vigil", "location": "soul_refining_ancestral", "duration": 360},
            ],
            sect_tasks=[
                {
                    "task_id": "sra_soul_banner_repair",
                    "name": "Soul Banner Repair",
                    "description": "Warden Qiu needs soul fragments and spirit stones to repair deteriorating soul banners.",
                    "required_items": {"ergenverse:soul_fragment": 6, "ergenverse:spirit_stone": 4},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 5, "experience": 45}
                },
                {
                    "task_id": "sra_bone_ritual_supply",
                    "name": "Soul Binding Ritual",
                    "description": "Bone and ink are needed for the soul binding ritual that maintains the ancestral ward.",
                    "required_items": {"minecraft:bone": 12, "minecraft:ink_sac": 6},
                    "rewards": {"ergenverse:jade_slip": 1, "ergenverse:soul_fragment": 4, "experience": 35}
                },
            ]
        ),
        make_npc(
            npc_id="npc_sra_sea_devils_guide_ma",
            name="Sea of Devils Guide Ma",
            nameCn="魔渊海向导马",
            faction="none (independent guide)",
            location="soul_refining_ancestral",
            cultivation="CONDENSATION_LATE",
            personality="pragmatic, fearless of souls, charges exorbitant fees",
            speech="blunt, transactional, references danger casually",
            relationship="neutral-commercial — guides for pay",
            canon_confidence=2,
            initiation_lines=[
                "You want to reach the Soul Refining Ancestral Ground? That will cost you. The Sea of Devils eats the unprepared.",
                "I have guided forty-seven expeditions. Thirty-one returned. The others are part of this place now.",
                "The Fighting Evil Sect sent a squad once. Brave. All of them. Dead within the hour.",
                "Emeralds and iron. I need supplies for the return trip. This place does not give anything for free."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Check route markers", "location": "soul_refining_ancestral", "duration": 120},
                {"time": "07:00", "action": "Guide morning expedition", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "10:00", "action": "Rest and resupply", "location": "soul_refining_ancestral", "duration": 120},
                {"time": "12:00", "action": "Meal", "location": "soul_refining_ancestral", "duration": 60},
                {"time": "13:00", "action": "Guide afternoon expedition", "location": "soul_refining_ancestral", "duration": 240},
                {"time": "18:00", "action": "Emergency route check", "location": "soul_refining_ancestral", "duration": 120},
                {"time": "21:00", "action": "Sleep with one eye open", "location": "soul_refining_ancestral", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "sra_expedition_supplies",
                    "name": "Expedition Resupply",
                    "description": "Guide Ma needs emeralds and iron to fund and equip return expeditions through the Sea of Devils.",
                    "required_items": {"minecraft:emerald": 10, "minecraft:iron_ingot": 6},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:soul_fragment": 3, "experience": 30}
                },
                {
                    "task_id": "sra_route_marking",
                    "name": "Route Marker Maintenance",
                    "description": "Coal and string are needed to maintain the dangerous route markers through the Sea of Devils.",
                    "required_items": {"minecraft:coal": 10, "minecraft:string": 8},
                    "rewards": {"ergenverse:spirit_stone": 2, "minecraft:emerald": 8, "experience": 25}
                },
            ]
        ),
        make_npc(
            npc_id="npc_sra_ancestor_echo_gu",
            name="Ancestor Echo Gu",
            nameCn="祖灵回响古",
            faction="Soul Refining Sect (Ancestral Spirit)",
            location="soul_refining_ancestral",
            cultivation="SOUL_TRANSFORMATION_EARLY",
            personality="ancient, fragmented consciousness, speaks in third person about the founding",
            speech="echoing, overlapping phrases, ancient dialect",
            relationship="neutral-reverent — recognizes sect descendants",
            canon_confidence=2,
            initiation_lines=[
                "Gu founded this ground. Gu refined the first ten thousand souls. Gu does not remember why.",
                "The Ten Billion Soul Banner... was not always a banner. It was a promise. Gu made it to someone. Who?",
                "Heng Yue builds mountains. Luo He cultivates rivers. We... we collect what remains when mountains and rivers fail.",
                "Spirit stones and obsidian. The anchors of this place. Without them, the souls scatter. Bring them."
            ],
            daily_schedule=[
                {"time": "00:00", "action": "Drift through ancestral ground", "location": "soul_refining_ancestral", "duration": 420},
                {"time": "07:00", "action": "Resonate with soul banners", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "10:00", "action": "Flicker between visibility", "location": "soul_refining_ancestral", "duration": 180},
                {"time": "13:00", "action": "Ancient memory access", "location": "soul_refining_ancestral", "duration": 240},
                {"time": "17:00", "action": "Strengthen ward boundaries", "location": "soul_refining_ancestral", "duration": 120},
                {"time": "20:00", "action": "Commune with scattered souls", "location": "soul_refining_ancestral", "duration": 240},
                {"time": "23:00", "action": "Near-dissipation", "location": "soul_refining_ancestral", "duration": 60},
            ],
            sect_tasks=[
                {
                    "task_id": "sra_ward_anchor_repair",
                    "name": "Ancestral Ward Anchors",
                    "description": "Ancestor Echo Gu needs spirit stones and obsidian to anchor the soul ward boundaries.",
                    "required_items": {"ergenverse:spirit_stone": 6, "minecraft:obsidian": 8},
                    "rewards": {"ergenverse:dao_fragment": 1, "ergenverse:jade_slip": 3, "experience": 50}
                },
                {
                    "task_id": "sra_memory_recovery",
                    "name": "Ancestral Memory Recovery",
                    "description": "Books and experience bottles help the ancient echo recover fragmented memories of the founding.",
                    "required_items": {"minecraft:book": 5, "minecraft:experience_bottle": 4},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:soul_fragment": 5, "experience": 45}
                },
            ]
        ),
    ]
    for npc in npcs:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {len(npcs)} soul_refining_ancestral NPCs")


# ============================================================
# 3. THUNDER CELESTIAL TEMPLE
# Theme: Lightning, thunder cultivation, celestial power.
# In RI: Thunder is central to Wang Lin's cultivation. The temple is
# a site where celestial thunder energy gathers, crucial for thunder-based
# techniques. Already has 2 beast NPCs (thunder_celestial_beast, thunder_toad).
# ============================================================

def write_thunder_celestial_loot():
    """HIGH-tier loot. Thunder celestial treasures."""
    table = {
        "pools": [
            # Pool 1: Thunder-themed materials (3-5 rolls)
            make_pool(3, 5, [
                make_entry("minecraft:copper_ingot", 10, 2, 6),        # conductor metal
                make_entry("minecraft:lightning_rod", 7, 1, 2),        # thunder attractor
                make_entry("minecraft:redstone", 7, 2, 5),            # energy current
                make_entry("minecraft:glowstone", 5, 1, 3),           # celestial light
                make_entry("minecraft:gold_ingot", 5, 1, 3),          # celestial metal
                make_entry("minecraft:blaze_rod", 4, 1, 2),           # fire/lightning fusion
                make_entry("minecraft:iron_ingot", 6, 2, 4),
            ]),
            # Pool 2: Cultivation rewards (2-3 rolls)
            make_pool(2, 3, [
                make_entry("ergenverse:jade_slip", 7),
                make_entry("ergenverse:spirit_stone", 6, 1, 3),
                make_entry("minecraft:experience_bottle", 5, 1, 2),
                make_entry("minecraft:enchanted_book", 4),
                make_entry("minecraft:emerald", 5, 2, 5),
                make_entry("minecraft:book", 4),
            ]),
            # Pool 3: Rare celestial items (1 roll)
            make_pool(1, 1, [
                make_entry("ergenverse:spirit_stone", 5, 2, 4),
                make_entry("ergenverse:dao_fragment", 2),
                make_entry("minecraft:diamond", 3, 1, 2),
                make_entry("minecraft:nether_star", 1),
            ]),
        ]
    }
    path = os.path.join(LOOT_DIR, "thunder_celestial_temple.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(table, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {path}")


def write_thunder_celestial_npcs():
    """3 INFERRED NPCs for the Thunder Celestial Temple."""
    npcs = [
        make_npc(
            npc_id="npc_tct_thunder_keeper_dian",
            name="Thunder Keeper Dian",
            nameCn="雷霆守者电",
            faction="Thunder Celestial Temple",
            location="thunder_celestial_temple",
            cultivation="NASCENT_SOUL_MID",
            personality="electric temperament, impatient, speaks in short bursts",
            speech="rapid, crackling energy in voice, interrupts self",
            relationship="neutral — tests visitors with thunder tribulation",
            canon_confidence=2,
            initiation_lines=[
                "Thunder does not wait. Neither do I. State your purpose in three words or leave.",
                "The Celestial Beast outside is a juvenile. The adult could level this temple. Respect it.",
                "Cloud Sky Sect cultivators came for our thunder techniques. Their wind could not carry lightning. They left empty.",
                "Copper and redstone — the body and blood of any thunder formation. Bring them or stop wasting time."
            ],
            daily_schedule=[
                {"time": "04:00", "action": "Absorb dawn thunder", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "06:00", "action": "Inspect lightning rods", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "08:00", "action": "Teach thunder cultivation", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "11:00", "action": "Combat practice", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "13:00", "action": "Meal", "location": "thunder_celestial_temple", "duration": 60},
                {"time": "14:00", "action": "Formation maintenance", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "18:00", "action": "Evening thunder absorption", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "21:00", "action": "Rest", "location": "thunder_celestial_temple", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "tct_thunder_formation_repair",
                    "name": "Thunder Formation Repair",
                    "description": "Keeper Dian needs copper and redstone to repair the temple's thunder-gathering formations.",
                    "required_items": {"minecraft:copper_ingot": 8, "minecraft:redstone": 6},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8, "experience": 30}
                },
                {
                    "task_id": "tct_lightning_rod_upgrade",
                    "name": "Lightning Rod Upgrade",
                    "description": "Iron and glowstone are needed to upgrade the lightning rod array for stronger celestial thunder absorption.",
                    "required_items": {"minecraft:iron_ingot": 6, "minecraft:glowstone": 4},
                    "rewards": {"ergenverse:spirit_stone": 2, "minecraft:experience_bottle": 2, "experience": 25}
                },
            ]
        ),
        make_npc(
            npc_id="npc_tct_tribulation_scholar_lei",
            name="Tribulation Scholar Lei",
            nameCn="天劫学者雷",
            faction="Thunder Celestial Temple",
            location="thunder_celestial_temple",
            cultivation="NASCENT_SOUL_LATE",
            personality="obsessive documenter of thunder tribulations, treats lightning as a science",
            speech="academic, precise, cites specific tribulation events",
            relationship="neutral — interested in anyone who has survived tribulation",
            canon_confidence=2,
            initiation_lines=[
                "I have documented four hundred and twelve thunder tribulations. Each one is unique. Each one lies.",
                "The Heavenly Fate Sect claims stars predict tribulation timing. They do not. Thunder is not astrological.",
                "A cultivator from the Heng Yue Sect survived a minor tribulation here last year. His sword conducted the lightning. He survived. The sword did not.",
                "Books and experience bottles. I need to record. Bring them and I will share my tribulation catalog."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Morning tribulation observation", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "08:00", "action": "Catalog previous tribulations", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "11:00", "action": "Study ancient thunder texts", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "13:00", "action": "Meal", "location": "thunder_celestial_temple", "duration": 60},
                {"time": "14:00", "action": "Teach tribulation survival", "location": "thunder_celestial_temple", "duration": 150},
                {"time": "17:00", "action": "Observe evening storms", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "21:00", "action": "Write observations", "location": "thunder_celestial_temple", "duration": 300},
            ],
            sect_tasks=[
                {
                    "task_id": "tct_tribulation_documentation",
                    "name": "Tribulation Documentation",
                    "description": "Scholar Lei needs books and experience bottles to document thunder tribulation patterns.",
                    "required_items": {"minecraft:book": 4, "minecraft:experience_bottle": 3},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "experience": 35}
                },
                {
                    "task_id": "tth_storm_attractor",
                    "name": "Storm Attractor Construction",
                    "description": "Gold and blaze rods are needed to construct a storm attractor for controlled tribulation study.",
                    "required_items": {"minecraft:gold_ingot": 6, "minecraft:blaze_rod": 3},
                    "rewards": {"ergenverse:spirit_stone": 2, "ergenverse:jade_slip": 1, "experience": 30}
                },
            ]
        ),
        make_npc(
            npc_id="npc_tct_celestial_pilgrim_yun",
            name="Celestial Pilgrim Yun",
            nameCn="天朝朝圣者云",
            faction="none (pilgrim from distant land)",
            location="thunder_celestial_temple",
            cultivation="FOUNDATION_PEAK",
            personality="reverent, seeking enlightenment through thunder, deeply afraid but determined",
            speech="soft, awed, frequently pauses to listen to distant thunder",
            relationship="friendly — fellow traveler seeking power",
            canon_confidence=2,
            initiation_lines=[
                "I traveled from the Vermilion Bird Capital to reach this temple. Forty days through the mountains.",
                "They say if you meditate under the celestial thunder for seven days, your cultivation base shatters and reforms. I am on day three.",
                "A Luo He Sect disciple was here before me. She said the river sects cannot understand thunder. Water and lightning are enemies.",
                "Bread and coal. A pilgrim still needs to eat and stay warm, even in a temple of celestial power."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Pre-dawn meditation under sky", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "08:00", "action": "Morning meal", "location": "thunder_celestial_temple", "duration": 60},
                {"time": "09:00", "action": "Thunder meditation", "location": "thunder_celestial_temple", "duration": 240},
                {"time": "13:00", "action": "Meal", "location": "thunder_celestial_temple", "duration": 60},
                {"time": "14:00", "action": "Study temple inscriptions", "location": "thunder_celestial_temple", "duration": 120},
                {"time": "17:00", "action": "Evening thunder meditation", "location": "thunder_celestial_temple", "duration": 180},
                {"time": "21:00", "action": "Sleep near lightning rods", "location": "thunder_celestial_temple", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "tct_pilgrim_provisions",
                    "name": "Pilgrim Provisions",
                    "description": "Pilgrim Yun needs bread and coal to sustain her seven-day thunder meditation.",
                    "required_items": {"minecraft:bread": 8, "minecraft:coal": 6},
                    "rewards": {"minecraft:emerald": 6, "ergenverse:jade_slip": 1, "experience": 20}
                },
                {
                    "task_id": "tct_meditation_supplies",
                    "name": "Thunder Meditation Supplies",
                    "description": "Yun needs glowstone and emeralds to create meditation markers for her thunder cultivation.",
                    "required_items": {"minecraft:glowstone": 4, "minecraft:emerald": 6},
                    "rewards": {"ergenverse:jade_slip": 1, "minecraft:experience_bottle": 2, "experience": 25}
                },
            ]
        ),
    ]
    for npc in npcs:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {len(npcs)} thunder_celestial_temple NPCs")


# ============================================================
# 4. VERMILION BIRD CAPITAL
# Theme: Imperial power, vermilion/fire, planet-level governance.
# In RI: The capital of the Vermilion Bird (朱雀), the divine beast
# that gives Planet Suzaku (朱雀星) its name. Seat of planetary power.
# ============================================================

def write_vermilion_bird_capital_loot():
    """HIGH-tier loot. Imperial vermilion treasures."""
    table = {
        "pools": [
            # Pool 1: Imperial materials (3-5 rolls)
            make_pool(3, 5, [
                make_entry("minecraft:gold_ingot", 10, 2, 6),         # imperial gold
                make_entry("minecraft:redstone", 8, 2, 5),            # vermilion essence
                make_entry("minecraft:emerald", 7, 2, 5),            # imperial treasury
                make_entry("minecraft:iron_ingot", 6, 2, 4),
                make_entry("minecraft:blaze_rod", 5, 1, 3),           # vermilion fire
                make_entry("minecraft:red_wool", 4, 1, 2),           # imperial banner cloth
                make_entry("minecraft:coal", 5, 2, 4),
            ]),
            # Pool 2: Governance rewards (2-3 rolls)
            make_pool(2, 3, [
                make_entry("ergenverse:jade_slip", 7),
                make_entry("ergenverse:spirit_stone", 6, 1, 3),
                make_entry("minecraft:enchanted_book", 5),
                make_entry("minecraft:book", 5, 1, 2),
                make_entry("minecraft:experience_bottle", 4, 1, 2),
                make_entry("minecraft:diamond", 3, 1, 2),
            ]),
            # Pool 3: Rare imperial items (1 roll)
            make_pool(1, 1, [
                make_entry("ergenverse:spirit_stone", 5, 2, 4),
                make_entry("ergenverse:dao_fragment", 3),
                make_entry("minecraft:nether_star", 1),
                make_entry("minecraft:diamond", 4, 1, 3),
            ]),
        ]
    }
    path = os.path.join(LOOT_DIR, "vermilion_bird_capital.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(table, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {path}")


def write_vermilion_bird_capital_npcs():
    """3 INFERRED NPCs for the Vermilion Bird Capital."""
    npcs = [
        make_npc(
            npc_id="npc_vbc_imperial_guard_captain",
            name="Imperial Guard Captain Zhu",
            nameCn="禁卫队长朱",
            faction="Vermilion Bird Imperial Court",
            location="vermilion_bird_capital",
            cultivation="NASCENT_SOUL_LATE",
            personality="imperious, loyal to the Vermilion Bird, suspicious of all outsiders",
            speech="formal, commanding, uses imperial titles",
            relationship="neutral-suspicious — all visitors are potential threats to the Vermilion Bird",
            canon_confidence=2,
            initiation_lines=[
                "You stand in the Vermilion Bird Capital. The seat of Planet Suzaku's power. Conduct yourself accordingly.",
                "The Vermilion Bird has not manifested in four thousand years. The capital still waits. That is faith.",
                "Sects squabble over Zhao Country. They forget this capital exists. Their vision is small.",
                "Gold and iron. The imperial guard requires constant resupply. We protect the entire planet."
            ],
            daily_schedule=[
                {"time": "05:00", "action": "Dawn guard assembly", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "07:00", "action": "Patrol capital perimeter", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "10:00", "action": "Receive reports from provinces", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "12:00", "action": "Meal", "location": "vermilion_bird_capital", "duration": 60},
                {"time": "13:00", "action": "Inspect guard posts", "location": "vermilion_bird_capital", "duration": 150},
                {"time": "16:00", "action": "Train new recruits", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "20:00", "action": "Night watch briefing", "location": "vermilion_bird_capital", "duration": 60},
                {"time": "21:00", "action": "Night patrol", "location": "vermilion_bird_capital", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "vbc_guard_resupply",
                    "name": "Imperial Guard Resupply",
                    "description": "Captain Zhu needs gold and iron to equip the planetary imperial guard.",
                    "required_items": {"minecraft:gold_ingot": 8, "minecraft:iron_ingot": 10},
                    "rewards": {"ergenverse:jade_slip": 2, "ergenverse:spirit_stone": 2, "experience": 35}
                },
                {
                    "task_id": "vbc_banner_repair",
                    "name": "Imperial Banner Repair",
                    "description": "Red wool and redstone are needed to repair the imperial vermilion banners.",
                    "required_items": {"minecraft:red_wool": 8, "minecraft:redstone": 6},
                    "rewards": {"minecraft:emerald": 10, "ergenverse:jade_slip": 1, "experience": 25}
                },
            ]
        ),
        make_npc(
            npc_id="npc_vbc_imperial_scribe_bai",
            name="Imperial Scribe Bai",
            nameCn="皇室史官白",
            faction="Vermilion Bird Imperial Court",
            location="vermilion_bird_capital",
            cultivation="NASCENT_SOUL_EARLY",
            personality="meticulous, secretly aware of the empire's decline, records everything",
            speech="measured, literary, quotes imperial edicts",
            relationship="neutral-talkative — craves information from the provinces",
            canon_confidence=2,
            initiation_lines=[
                "I have recorded eight hundred years of this capital's history. The last two hundred are... repetitive.",
                "The Heavenly Fate Sect once sent astrologers to predict the Vermilion Bird's return. Their stars said 'soon.' That was six centuries ago.",
                "Zhao Country is one province of twenty-three. Its sects believe they are the world. They are a footnote.",
                "Ink, paper, and books. A scribe's needs are humble. An empire's history is not."
            ],
            daily_schedule=[
                {"time": "06:00", "action": "Review yesterday's records", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "08:00", "action": "Interview visitors", "location": "vermilion_bird_capital", "duration": 150},
                {"time": "10:30", "action": "Write imperial records", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "13:30", "action": "Meal", "location": "vermilion_bird_capital", "duration": 60},
                {"time": "14:30", "action": "Archive research", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "18:00", "action": "Evening court session", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "21:00", "action": "Write by candlelight", "location": "vermilion_bird_capital", "duration": 420},
            ],
            sect_tasks=[
                {
                    "task_id": "vbc_imperial_record_keeping",
                    "name": "Imperial Record Keeping",
                    "description": "Scribe Bai needs ink, paper, and books to maintain the imperial archives.",
                    "required_items": {"minecraft:ink_sac": 6, "minecraft:paper": 10, "minecraft:book": 3},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:emerald": 8, "experience": 25}
                },
                {
                    "task_id": "vbc_province_intelligence",
                    "name": "Province Intelligence Report",
                    "description": "Scribe Bai trades knowledge for emeralds and experience bottles to fund intelligence networks.",
                    "required_items": {"minecraft:emerald": 8, "minecraft:experience_bottle": 2},
                    "rewards": {"ergenverse:jade_slip": 2, "minecraft:enchanted_book": 1, "experience": 30}
                },
            ]
        ),
        make_npc(
            npc_id="npc_vbc_vermilion_priestess_hong",
            name="Vermilion Priestess Hong",
            nameCn="朱雀祭司红",
            faction="Vermilion Bird Temple",
            location="vermilion_bird_capital",
            cultivation="NASCENT_SOUL_PEAK",
            personality="devout, mystical, claims to communicate with the dormant Vermilion Bird",
            speech="ceremonial, prophetic, pauses as if listening",
            relationship="neutral-reverent — evaluates visitors' spiritual resonance",
            canon_confidence=2,
            initiation_lines=[
                "The Vermilion Bird sleeps beneath this capital. I have felt its dreams. They are fire and sorrow.",
                "Four thousand years ago, the Bird fought a war that cracked the planet. We maintain the temple. The war is not over.",
                "The Xuan Dao Sect understands Dao philosophy. The Soul Refining Sect understands souls. We understand fire. Not blaze-rod fire. Phoenix fire.",
                "Blaze rods and emeralds. The temple's eternal flame requires fuel. The Bird's dream requires tribute."
            ],
            daily_schedule=[
                {"time": "04:00", "action": "Pre-dawn fire meditation", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "07:00", "action": "Temple ceremony", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "09:00", "action": "Receive petitioners", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "12:00", "action": "Sacred meal", "location": "vermilion_bird_capital", "duration": 60},
                {"time": "13:00", "action": "Eternal flame maintenance", "location": "vermilion_bird_capital", "duration": 180},
                {"time": "16:00", "action": "Vermilion dream interpretation", "location": "vermilion_bird_capital", "duration": 150},
                {"time": "19:00", "action": "Evening fire ceremony", "location": "vermilion_bird_capital", "duration": 120},
                {"time": "22:00", "action": "Commune with Bird's dream", "location": "vermilion_bird_capital", "duration": 360},
            ],
            sect_tasks=[
                {
                    "task_id": "vbc_eternal_flame_fuel",
                    "name": "Eternal Flame Fuel",
                    "description": "Priestess Hong needs blaze rods and emeralds to maintain the temple's eternal flame.",
                    "required_items": {"minecraft:blaze_rod": 6, "minecraft:emerald": 8},
                    "rewards": {"ergenverse:spirit_stone": 3, "ergenverse:jade_slip": 2, "experience": 40}
                },
                {
                    "task_id": "vbc_dream_tribute",
                    "name": "Vermilion Dream Tribute",
                    "description": "Gold and redstone are needed as tribute for the Vermilion Bird's dream communion ritual.",
                    "required_items": {"minecraft:gold_ingot": 6, "minecraft:redstone": 8},
                    "rewards": {"ergenverse:dao_fragment": 1, "ergenverse:spirit_stone": 2, "experience": 45}
                },
            ]
        ),
    ]
    for npc in npcs:
        path = os.path.join(NPC_DIR, f"{npc['npc_id']}.json")
        with open(path, "w", encoding="utf-8") as f:
            json.dump(npc, f, indent=2, ensure_ascii=False)
    print(f"  Wrote {len(npcs)} vermilion_bird_capital NPCs")


# ============================================================
# MAIN
# ============================================================
if __name__ == "__main__":
    os.makedirs(LOOT_DIR, exist_ok=True)
    os.makedirs(NPC_DIR, exist_ok=True)

    print("=== 1. Immortal Emperor Cave Mansion ===")
    write_immortal_emperor_loot()
    write_immortal_emperor_npcs()

    print("=== 2. Soul Refining Ancestral (orphan loot kept) ===")
    write_soul_refining_ancestral_npcs()

    print("=== 3. Thunder Celestial Temple ===")
    write_thunder_celestial_loot()
    write_thunder_celestial_npcs()

    print("=== 4. Vermilion Bird Capital ===")
    write_vermilion_bird_capital_loot()
    write_vermilion_bird_capital_npcs()

    print("\nAUTO-CANON-044: 4 special locations complete.")
    print(f"  3 new loot tables in {LOOT_DIR}")
    print(f"  12 new NPCs in {NPC_DIR}")