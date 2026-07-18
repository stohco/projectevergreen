#!/usr/bin/env python3
"""
Xianxia Schema Refinement — adds the xianxia-specific primitives identified in
the architectural review to the existing canon data, idempotently.

Adds:
  Species (93 files):
    - bloodline_tier        : ordinary | spiritual | desolate | ancient_celestial | origin_true_body | chaos
    - inner_dan_properties  : { element, purity_tier, dao_resonance, side_effects } | null  (null is canon info for swarm-types)
    - hive_mind_anchor      : { swarm_id, queen_id, role_in_swarm, distance_from_queen } | null

  Civilizations (30 files):
    - spiritual_vein_grade  : 1-9 | immortal | chaos
    - heritage_treasures    : [ item_id, ... ]  (pointers into provenance / canon artifacts)

  NPCs (151 files):
    - dao_heart             : { stability: 0-100, cracks: [], last_tested_tick }
    - soul_state            : nascent_soul | soul_formation | primordial_spirit | origin | none
    - tribulation_debt      : int  (accumulated heavenly resentment, 0 = none)

  NEW subsystem data/ergenverse/karma/ (Karmic Nexus graph):
    - ~16 canon karmic nodes, each with:
        karmic_weight       : signed int  (+ = debt owed TO entity, - = grudge held BY entity)
        consequences[]      : typed pointers { type, target_id, magnitude, resolved_at_tick | null }
        unresolved_until    : tick | condition | "never"  (Prime Directive: persists until paid)

Prime Directive: reality is objective. These fields describe what IS, not what the
player perceives. Perception is a separate layer.

Idempotent: skips any file that already carries the `_xianxia_schema` marker.
"""
from __future__ import annotations
import json, os, hashlib, sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DATA = ROOT / "src" / "main" / "resources" / "data" / "ergenverse"
CANON_DB = ROOT / "ri_canon_database.json"
LANG_FILE = ROOT / "src" / "main" / "resources" / "assets" / "ergenverse" / "lang" / "en_us.json"
SCHEMA_MARKER = "_xianxia_schema"
SCHEMA_VERSION = 1

# ---------------------------------------------------------------------------
# Canon lookup tables (derived from RI canon, not invented)
# ---------------------------------------------------------------------------

# bloodline_tier by species_id. Anything not listed defaults to "spiritual".
BLOODLINE_TIERS = {
    # origin true bodies
    "restriction_origin_true_body": "origin_true_body",
    "taichu_origin_true_body": "origin_true_body",
    "thunder_origin_true_body": "origin_true_body",
    "five_elements_origin_true_body": "origin_true_body",
    "miemie_origin_true_body": "origin_true_body",
    "slaughter_true_body": "origin_true_body",
    "immortal_celestial_body": "origin_true_body",
    "undying_ancient_body": "origin_true_body",
    "dao_gu_indestructible_body": "origin_true_body",
    # ancient celestial beasts
    "azure_dragon": "ancient_celestial",
    "black_tortoise": "ancient_celestial",
    "vermilion_bird": "ancient_celestial",
    "white_tiger": "ancient_celestial",
    "golden_exalt_sea_dragon": "ancient_celestial",
    "brilliant_void": "ancient_celestial",
    "ancient_dream_beast": "ancient_celestial",
    "nether_beast": "ancient_celestial",
    "thunder_celestial_beast": "ancient_celestial",
    "blood_ancestor_beast": "ancient_celestial",
    "black_fiend_devil_saint": "ancient_celestial",
    # desolate beasts (great predators)
    "ji_qiong": "desolate",
    "earth_fire_dragon": "desolate",
    "green_devil_scorpion": "desolate",
    "berserk_beast": "desolate",
    "wind_demon_beast": "desolate",
    "heavenly_bull_soul_beast": "desolate",
    # swarm-type (special: no traditional inner dan)
    "mosquito_beast": "spiritual",
    # lesser miscellaneous (ordinary mutated beasts)
    "spirit_rabbit": "ordinary",
    "spirit_fox": "ordinary",
    "spirit_deer": "ordinary",
    "spirit_eagle": "ordinary",
    "spirit_tortoise": "ordinary",
    "spirit_bear": "ordinary",
    "spirit_snake": "ordinary",
    "spirit_bat": "ordinary",
    "spirit_fish": "ordinary",
    "spirit_toad": "ordinary",
    "spirit_wolf_pack_leader": "ordinary",
    "spirit_ape": "ordinary",
}

# Swarm-type species (no traditional inner dan — null is canon information)
SWARM_SPECIES = {"mosquito_beast"}

# inner_dan element by species (canon). null = swarm-type.
INNER_DAN = {
    "azure_dragon":           {"element": "wood",      "purity_tier": 9, "dao_resonance": "celestial_sovereignty", "side_effects": ["body_refinement_overflow"]},
    "black_tortoise":         {"element": "water",     "purity_tier": 9, "dao_resonance": "primordial_water",     "side_effects": ["spiritual_seal"]},
    "vermilion_bird":         {"element": "fire",      "purity_tier": 9, "dao_resonance": "rebirth_samsara",      "side_effects": ["rebirth_ignition"]},
    "white_tiger":            {"element": "metal",     "purity_tier": 9, "dao_resonance": "slaughter_intent",     "side_effects": ["killing_intent_overflow"]},
    "golden_exalt_sea_dragon":{"element": "water",     "purity_tier": 8, "dao_resonance": "dragon_sovereignty",   "side_effects": ["dragon_pressure"]},
    "earth_fire_dragon":      {"element": "fire",      "purity_tier": 6, "dao_resonance": "earth_flame",          "side_effects": ["volcanic_corruption"]},
    "green_devil_scorpion":   {"element": "poison",    "purity_tier": 7, "dao_resonance": "green_devil_venom",    "side_effects": ["poison_accumulation"]},
    "ji_qiong":               {"element": "chaos",     "purity_tier": 8, "dao_resonance": "ancient_devouring",    "side_effects": ["soul_devour"]},
    "thunder_celestial_beast":{"element": "thunder",   "purity_tier": 8, "dao_resonance": "celestial_thunder",    "side_effects": ["thunder_tempering"]},
    "nether_beast":           {"element": "yin",       "purity_tier": 8, "dao_resonance": "nether_realm",         "side_effects": ["soul_corruption"]},
    "brilliant_void":         {"element": "void",      "purity_tier": 9, "dao_resonance": "void_dissolution",     "side_effects": ["existence_erasure"]},
    "blood_ancestor_beast":   {"element": "blood",     "purity_tier": 9, "dao_resonance": "bloodline_sovereignty","side_effects": ["blood_frenzy"]},
    "black_fiend_devil_saint":{"element": "fiend",     "purity_tier": 9, "dao_resonance": "fiend_sainthood",      "side_effects": ["fiend_corruption"]},
    "ancient_dream_beast":    {"element": "illusion",  "purity_tier": 7, "dao_resonance": "dream_realm",          "side_effects": ["dream_trance"]},
    "wind_demon_beast":       {"element": "wind",      "purity_tier": 5, "dao_resonance": "demon_wind",           "side_effects": ["wind_poisoning"]},
    "heavenly_bull_soul_beast":{"element":"soul",      "purity_tier": 7, "dao_resonance": "heavenly_bull",        "side_effects": ["soul_pull"]},
    "berserk_beast":          {"element": "earth",     "purity_tier": 5, "dao_resonance": "berserk_fury",         "side_effects": ["rage_overflow"]},
    "spirit_fox":             {"element": "illusion",  "purity_tier": 2, "dao_resonance": "fox_charm",            "side_effects": []},
    "spirit_snake":           {"element": "poison",    "purity_tier": 2, "dao_resonance": "serpent_venom",        "side_effects": []},
    "spirit_bear":            {"element": "earth",     "purity_tier": 2, "dao_resonance": "mountain_strength",     "side_effects": []},
    "spirit_eagle":           {"element": "wind",      "purity_tier": 2, "dao_resonance": "sky_keenness",         "side_effects": []},
    "spirit_toad":            {"element": "poison",    "purity_tier": 3, "dao_resonance": "miasma",               "side_effects": []},
    "spirit_tortoise":        {"element": "water",     "purity_tier": 3, "dao_resonance": "longevity",            "side_effects": []},
    "spirit_wolf_pack_leader":{"element": "metal",     "purity_tier": 3, "dao_resonance": "pack_sovereignty",     "side_effects": []},
    "spirit_ape":             {"element": "wood",      "purity_tier": 3, "dao_resonance": "wild_strength",        "side_effects": []},
    "spirit_rabbit":          {"element": "wood",      "purity_tier": 1, "dao_resonance": "fleetness",            "side_effects": []},
    "spirit_deer":            {"element": "wood",      "purity_tier": 1, "dao_resonance": "gentle_qi",            "side_effects": []},
    "spirit_bat":             {"element": "yin",       "purity_tier": 1, "dao_resonance": "night_sense",          "side_effects": []},
    "spirit_fish":            {"element": "water",     "purity_tier": 1, "dao_resonance": "flow",                 "side_effects": []},
}

# hive_mind_anchor: only swarm species get a real anchor.
HIVE_MIND = {
    "mosquito_beast": {"swarm_id": "mosquito_valley_swarm", "queen_id": "mosquito_valley_ancient_queen",
                       "role_in_swarm": "collective", "distance_from_queen": "variable"},
}

# spiritual_vein_grade by civilization_id (canon-derived).
SPIRITUAL_VEIN_GRADE = {
    "heng_yue_sect":              4,
    "cloud_sky_sect":             5,
    "da_lou_sword_sect":          6,
    "soul_refining_sect":         7,
    "heavenly_fate_sect":         7,
    "vermilion_bird_divine_sect": "immortal",
    "great_soul_sect":            "immortal",
    "dark_scorpion_clan":         6,
    "ancient_clan":               "immortal",
    "dao_devil_sect":             7,
    "seven_colored_sect":         8,
    "wang_family_village":        1,
    "teng_family_city":           2,
    "cloud_sea_star_system":      8,
    "allheaven_star_system":      "immortal",
    "ancient_demon_city_court":   "chaos",
}

# heritage_treasures by civilization_id (pointers into canon artifact IDs).
HERITAGE_TREASURES = {
    "heng_yue_sect":              ["I03", "I07"],          # Restriction flag, flying swords
    "cloud_sky_sect":             ["I05"],                 # Cloud-sky inheritance
    "da_lou_sword_sect":          ["I09"],                 # Da Lou sword canon
    "soul_refining_sect":         ["I13"],                 # 18-Hell Celestial Sealing Stamp line
    "heavenly_fate_sect":         ["I02"],                 # Heaven-Fate inheritance
    "vermilion_bird_divine_sect": ["I01", "I04"],          # Suzaku/Vermilion inheritance
    "great_soul_sect":            ["I01"],                 # Heaven-Defying Bead ties
    "dark_scorpion_clan":         ["I06"],                 # Dark scorpion heritage
    "ancient_clan":               ["I08"],                 # Ancient clan god-bones
    "dao_devil_sect":             ["I10"],                 # Dao Devil heritage
    "seven_colored_sect":         ["I01"],                 # Seven-Colored ties to the bead
    "teng_family_city":           ["I11"],                 # Teng family heirloom
    "ancient_demon_city_court":   ["I12"],                 # Ancient demon seal
}

# soul_state by cultivation tier (used to seed NPC soul_state).
def soul_state_for(cultivation: str) -> str:
    if not cultivation:
        return "none"
    c = cultivation.lower()
    if "heaven trampling" in c or "transcend" in c or "origin" in c and "sect" not in c:
        return "origin"
    if "spirit transformation" in c or "soul formation" in c or "primordial" in c:
        return "primordial_spirit"
    if "nascent" in c or "yuan ying" in c:
        return "nascent_soul"
    if "foundation" in c:
        return "nascent_soul"  # foundation = forming nascent soul foundation
    return "none"

# dao_heart seeds for notable characters (canon-derived). Default 50.
DAO_HEART = {
    "wang_lin":          98,  # famously unbreakable after Teng massacre
    "situ_nan":          85,
    "qing_shui":         95,
    "seven_colored_daoist": 90,
    "blood_ancestor":    80,
    "teng_huayuan":      35,  # broke under pressure
    "qiu_siping":        30,  # craven
    "li_muwan":          70,
}

# tribulation_debt seeds (canon — Wang Lin's is famously astronomical).
TRIBULATION_DEBT = {
    "wang_lin":          9999,
    "seven_colored_daoist": 800,
    "blood_ancestor":    1200,
    "situ_nan":          600,
}

# ---------------------------------------------------------------------------
# Karmic Nexus nodes (new subsystem)
# ---------------------------------------------------------------------------
KARMA_NODES = [
    {
        "karma_id": "teng_family_massacre",
        "name": "Teng Family Massacre",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 1825,
        "karmic_weight": -9000,
        "bearer": "N01",
        "debtor": "teng_family",
        "description": "Wang Lin annihilates the Teng family to avenge his parents. The single most defining karmic act of his early cultivation — locks his Dao toward Slaughter.",
        "consequences": [
            {"type": "personality_shift", "target_id": "N01", "magnitude": 95, "resolved_at_tick": 1825, "note": "dao_affinity locked toward Slaughter; mercy threshold permanently lowered"},
            {"type": "faction_destroyed", "target_id": "teng_family", "magnitude": 100, "resolved_at_tick": 1825, "note": "Teng family lineage extinguished"},
            {"type": "technique_unlocked", "target_id": "T_slaughter_sword", "magnitude": 80, "resolved_at_tick": 2400, "note": "Dao of Slaughter comprehension accelerated"},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 7000, "resolved_at_tick": None, "note": "heavenly resentment accumulated; manifests across later tribulations"}
        ],
        "unresolved_until": "never",
        "prime_directive_note": "This karma persists objectively; no amount of merit erases it. It can only be paid in kind."
    },
    {
        "karma_id": "heaven_defying_bead_discovery",
        "name": "Heaven-Defying Bead Discovery",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 6570,
        "karmic_weight": 5000,
        "bearer": "N01",
        "debtor": "heaven",
        "description": "Wang Lin finds the Heaven-Defying Bead in a dead bird as a child. The bead is destiny-bound; this act entangles him with the cosmic cause-and-effect of the entire Ergenverse.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "I01", "magnitude": 100, "resolved_at_tick": 6570},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 2999, "resolved_at_tick": None, "note": "the bead attracts enemies across the cosmos"}
        ],
        "unresolved_until": "transcendence"
    },
    {
        "karma_id": "ancient_god_inheritance",
        "name": "Ancient God Tu Si Inheritance",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 12000,
        "karmic_weight": 3000,
        "bearer": "N01",
        "debtor": "tu_si_ancient_god",
        "description": "Wang Lin inherits the Ancient God knowledge of Tu Si, gaining the Ji Realm and the path of the Ancient God.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_ancient_god_ji_realm", "magnitude": 100, "resolved_at_tick": 12000},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 1500, "resolved_at_tick": None}
        ],
        "unresolved_until": "wang_lin_becomes_ancient_god"
    },
    {
        "karma_id": "suzaku_inheritance",
        "name": "Suzaku Divine Emperor Inheritance",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 21000,
        "karmic_weight": 2500,
        "bearer": "N01",
        "debtor": "suzaku_divine_emperor",
        "description": "Wang Lin inherits the Suzaku divine emperor position and the Vermilion Bird inheritance.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "I04", "magnitude": 100, "resolved_at_tick": 21000},
            {"type": "faction_destroyed", "target_id": "heavenly_fate_sect_leadership", "magnitude": 60, "resolved_at_tick": 21000}
        ],
        "unresolved_until": "vermilion_bird_recognition"
    },
    {
        "karma_id": "seven_colored_killing",
        "name": "Killing of the Seven-Colored Daoist",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 35400,
        "karmic_weight": -4500,
        "bearer": "N01",
        "debtor": "seven_colored_daoist",
        "description": "Wang Lin slays the Seven-Colored Daoist and becomes owner of the Cave World. Resolves the long-standing enmity but inherits the Cave World's karmic burden.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "cave_world", "magnitude": 100, "resolved_at_tick": 35400},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 3000, "resolved_at_tick": None, "note": "ownership of a world draws heavenly attention"}
        ],
        "unresolved_until": "never"
    },
    {
        "karma_id": "blood_ancestor_battle",
        "name": "Blood Ancestor Confrontation",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 18900,
        "karmic_weight": -2000,
        "bearer": "N01",
        "debtor": "blood_ancestor",
        "description": "Wang Lin battles the Blood Ancestor, a remnant of the ancient era. Establishes Wang Lin as a power capable of contesting ancient existences.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_blood_sutra", "magnitude": 50, "resolved_at_tick": 18900}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "mosquito_valley_taming",
        "name": "Taming the Mosquito Beast Swarm",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 5475,
        "karmic_weight": 800,
        "bearer": "N01",
        "debtor": "mosquito_valley_swarm",
        "description": "Wang Lin tames the mosquito beast swarm in Mosquito Valley. The swarm becomes his signature weapon.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "mosquito_beast", "magnitude": 100, "resolved_at_tick": 5475}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "situ_nan_friendship",
        "name": "Situ Nan Mentorship",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 7300,
        "karmic_weight": 1200,
        "bearer": "N01",
        "debtor": "situ_nan",
        "description": "Situ Nan takes Wang Lin as a disciple, granting him the path of restriction and profound guidance.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_restriction_canon", "magnitude": 90, "resolved_at_tick": 7300}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "qing_shui_mentorship",
        "name": "Qing Shui Mentorship",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 25550,
        "karmic_weight": 1500,
        "bearer": "N01",
        "debtor": "qing_shui",
        "description": "Qing Shui teaches Wang Lin the Heaven-Trampling Dao, the capstone of his cultivation.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_heaven_trampling_bridge", "magnitude": 100, "resolved_at_tick": 25550}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "soul_refining_inheritance",
        "name": "Soul Refining Sect Inheritance",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 14600,
        "karmic_weight": -1500,
        "bearer": "N01",
        "debtor": "soul_refining_sect",
        "description": "Wang Lin inherits the Soul Refining Sect's soul-manipulation legacy. Heavy yin karma.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_soul_refining", "magnitude": 80, "resolved_at_tick": 14600},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 800, "resolved_at_tick": None}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "foreign_battleground_participation",
        "name": "Foreign Battleground Service",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 16700,
        "karmic_weight": -1800,
        "bearer": "N01",
        "debtor": "planet_suzaku",
        "description": "Wang Lin fights in the Foreign Battleground for Planet Suzaku. Earns merit but accumulates war karma.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "foreign_battleground_merit", "magnitude": 70, "resolved_at_tick": 16700}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "cave_world_ownership",
        "name": "Cave World Ownership",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 35400,
        "karmic_weight": 4000,
        "bearer": "N01",
        "debtor": "cave_world",
        "description": "Wang Lin becomes the owner of the Cave World after slaying the Seven-Colored Daoist. A world's worth of beings now fall under his karma.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "cave_world", "magnitude": 100, "resolved_at_tick": 35400},
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 2000, "resolved_at_tick": None}
        ],
        "unresolved_until": "never"
    },
    {
        "karma_id": "heaven_trampling_transcendence",
        "name": "Heaven-Trampling Transcendence",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 46500,
        "karmic_weight": 10000,
        "bearer": "N01",
        "debtor": "heaven",
        "description": "Wang Lin crosses the nine Heaven-Trampling Bridges and transcends with Li Muwan. Resolves the majority of his accumulated karma.",
        "consequences": [
            {"type": "technique_unlocked", "target_id": "T_transcendence", "magnitude": 100, "resolved_at_tick": 46500}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "immortal_astral_arrival",
        "name": "Immortal Astral Continent Arrival",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 40000,
        "karmic_weight": 2000,
        "bearer": "N01",
        "debtor": "immortal_astral_continent",
        "description": "Wang Lin arrives at the Immortal Astral Continent, the apex realm of the Ergenverse.",
        "consequences": [
            {"type": "tribulation_earned", "target_id": "N01", "magnitude": 1500, "resolved_at_tick": None}
        ],
        "unresolved_until": "transcendence"
    },
    {
        "karma_id": "heng_yue_sect_destruction",
        "name": "Heng Yue Sect Decline",
        "novel": "Renegade Immortal",
        "canon_confidence": 4,
        "derivation_type": "A",
        "event_tick": 20000,
        "karmic_weight": -600,
        "bearer": "N01",
        "debtor": "heng_yue_sect",
        "description": "Wang Lin's departure and rise causes the decline of his former sect. Indirect karmic consequence.",
        "consequences": [
            {"type": "faction_destroyed", "target_id": "heng_yue_sect", "magnitude": 70, "resolved_at_tick": 20000, "note": "decline, not total destruction"}
        ],
        "unresolved_until": "resolved"
    },
    {
        "karma_id": "realm_sealing_grand_array",
        "name": "Realm-Sealing Grand Array",
        "novel": "Renegade Immortal",
        "canon_confidence": 5,
        "derivation_type": "A",
        "event_tick": 29200,
        "karmic_weight": 1500,
        "bearer": "N01",
        "debtor": "sealed_realm",
        "description": "Wang Lin becomes Lord of the Sealed Realm, inheriting its karma and protection.",
        "consequences": [
            {"type": "inheritance_claimed", "target_id": "sealed_realm", "magnitude": 100, "resolved_at_tick": 29200}
        ],
        "unresolved_until": "never"
    },
]

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def salt_for(name: str) -> int:
    return int(hashlib.md5(name.encode("utf-8")).hexdigest(), 16) % (2**31)

def load_json(p: Path):
    with open(p, "r", encoding="utf-8") as f:
        return json.load(f)

def write_json(p: Path, data):
    with open(p, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        f.write("\n")

def needs_refinement(data) -> bool:
    return data.get(SCHEMA_MARKER) != SCHEMA_VERSION

# ---------------------------------------------------------------------------
# Pass 1: species refinement
# ---------------------------------------------------------------------------
def refine_species():
    sp_dir = DATA / "species"
    if not sp_dir.exists():
        return 0
    n = 0
    for f in sorted(sp_dir.glob("*.json")):
        d = load_json(f)
        if not needs_refinement(d):
            continue
        sid = d.get("species_id", f.stem)
        d["bloodline_tier"] = BLOODLINE_TIERS.get(sid, "spiritual")
        if sid in SWARM_SPECIES:
            d["inner_dan_properties"] = None
            d["_inner_dan_note"] = "Swarm-type beast — no traditional inner dan. The swarm itself is the 'core.' Canon information, not a gap."
        else:
            d["inner_dan_properties"] = INNER_DAN.get(sid, {
                "element": "neutral", "purity_tier": 3, "dao_resonance": "minor", "side_effects": []
            })
        d["hive_mind_anchor"] = HIVE_MIND.get(sid, None)
        d[SCHEMA_MARKER] = SCHEMA_VERSION
        write_json(f, d)
        n += 1
    return n

# ---------------------------------------------------------------------------
# Pass 2: civilization refinement
# ---------------------------------------------------------------------------
def refine_civilizations():
    civ_dir = DATA / "civilizations"
    if not civ_dir.exists():
        return 0
    n = 0
    for f in sorted(civ_dir.glob("*.json")):
        d = load_json(f)
        if not needs_refinement(d):
            continue
        cid = d.get("civilization_id", f.stem)
        d["spiritual_vein_grade"] = SPIRITUAL_VEIN_GRADE.get(cid, 3)
        d["heritage_treasures"] = HERITAGE_TREASURES.get(cid, [])
        d[SCHEMA_MARKER] = SCHEMA_VERSION
        write_json(f, d)
        n += 1
    return n

# ---------------------------------------------------------------------------
# Pass 3: NPC refinement (dao_heart, soul_state, tribulation_debt)
# ---------------------------------------------------------------------------
def refine_npcs():
    npc_dir = DATA / "npcs"
    if not npc_dir.exists():
        return 0
    n = 0
    for f in sorted(npc_dir.glob("*.json")):
        d = load_json(f)
        if not needs_refinement(d):
            continue
        nid = d.get("npc_id") or d.get("canon_id") or f.stem
        key = nid.lower().replace("-", "_")
        cult = d.get("cultivation", "")
        if isinstance(cult, dict):
            cult = cult.get("peak", "") or cult.get("current", "") or ""
        d["dao_heart"] = {
            "stability": DAO_HEART.get(key, 50),
            "cracks": [],
            "last_tested_tick": None,
            "note": "0-100. A cracked Dao heart blocks breakthrough regardless of Qi. Per canon: Wang Lin's Dao heart is famously tested at every major realm."
        }
        d["soul_state"] = soul_state_for(str(cult))
        d["tribulation_debt"] = TRIBULATION_DEBT.get(key, 0)
        d[SCHEMA_MARKER] = SCHEMA_VERSION
        write_json(f, d)
        n += 1
    return n

# ---------------------------------------------------------------------------
# Pass 4: Karmic Nexus subsystem (NEW)
# ---------------------------------------------------------------------------
def write_karma_subsystem():
    karma_dir = DATA / "karma"
    karma_dir.mkdir(parents=True, exist_ok=True)
    n = 0
    for node in KARMA_NODES:
        p = karma_dir / f"{node['karma_id']}.json"
        node2 = dict(node)
        node2["_comment"] = (
            f"Karmic Nexus node: {node['name']}. Per architectural review: RI is a novel about karma. "
            "karmic_weight is signed (+ = debt owed TO bearer, - = grudge held BY bearer). "
            "Prime Directive: unresolved karma persists objectively until paid."
        )
        node2["salt"] = salt_for(node["karma_id"])
        write_json(p, node2)
        n += 1
    # registry
    reg = {
        "_comment": "Karmic Nexus registry. Lists all canon karmic nodes. Consumed by WorldStateEngine.queryWhoKnows() and queryWhyUntaken().",
        "subsystem": "karma",
        "node_count": len(KARMA_NODES),
        "nodes": [n["karma_id"] for n in KARMA_NODES],
        "schema": {
            "karmic_weight": "signed int; + = debt owed TO bearer, - = grudge held BY bearer",
            "consequences": "typed pointers to downstream effects; each has type/target_id/magnitude/resolved_at_tick",
            "unresolved_until": "tick | condition | 'never' | 'resolved'; Prime Directive demands persistence"
        }
    }
    write_json(karma_dir / "_registry.json", reg)
    return n

# ---------------------------------------------------------------------------
# Pass 5: lang entries for new schema fields
# ---------------------------------------------------------------------------
def refine_lang():
    if not LANG_FILE.exists():
        return 0
    lang = load_json(LANG_FILE)
    added = 0
    def add(k, v):
        if k not in lang:
            lang[k] = v
            return 1
        return 0
    # field labels
    added += add("ergenverse.schema.bloodline_tier", "Bloodline Tier")
    added += add("ergenverse.schema.inner_dan", "Inner Dan")
    added += add("ergenverse.schema.hive_mind", "Hive Mind")
    added += add("ergenverse.schema.spiritual_vein_grade", "Spiritual Vein Grade")
    added += add("ergenverse.schema.heritage_treasures", "Heritage Treasures")
    added += add("ergenverse.schema.dao_heart", "Dao Heart")
    added += add("ergenverse.schema.soul_state", "Soul State")
    added += add("ergenverse.schema.tribulation_debt", "Tribulation Debt")
    added += add("ergenverse.schema.karma", "Karmic Nexus")
    added += add("ergenverse.schema.karmic_weight", "Karmic Weight")
    # bloodline tier values
    for t in ["ordinary", "spiritual", "desolate", "ancient_celestial", "origin_true_body", "chaos"]:
        added += add(f"ergenverse.bloodline.{t}", t.replace("_", " ").title())
    # soul states
    for s in ["none", "nascent_soul", "soul_formation", "primordial_spirit", "origin"]:
        added += add(f"ergenverse.soul_state.{s}", s.replace("_", " ").title())
    # karma node names
    for node in KARMA_NODES:
        added += add(f"ergenverse.karma.{node['karma_id']}", node["name"])
    write_json(LANG_FILE, lang)
    return added

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
def main():
    print("=== Xianxia Schema Refinement ===")
    print(f"Data root: {DATA}")
    s = refine_species()
    print(f"Species refined:        {s}")
    c = refine_civilizations()
    print(f"Civilizations refined:  {c}")
    npc = refine_npcs()
    print(f"NPCs refined:           {npc}")
    k = write_karma_subsystem()
    print(f"Karma nodes written:    {k}")
    l = refine_lang()
    print(f"Lang entries added:     {l}")
    print("DONE.")

if __name__ == "__main__":
    main()
