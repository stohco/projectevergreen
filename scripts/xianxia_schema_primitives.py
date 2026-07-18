#!/usr/bin/env python3
"""
Xianxia Schema Primitives — Upgrades existing JSON data files with
xianxia-specific schema primitives: bloodline_tier, inner_dan_properties,
hive_mind_anchor, combat_ai (species), spiritual_vein_grade,
heritage_treasures, relationship_metadata (factions), karmic_weight,
consequences (opportunities).

Idempotent: safe to re-run. Preserves all existing fields.
"""

import json
import os
import glob
import re
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent / "src" / "main" / "resources" / "data" / "ergenverse"

# ──────────────────────────────────────────────────────
# 1. SPECIES UPGRADES
# ──────────────────────────────────────────────────────

# 9 species known to have condensed inner cores
INNER_DAN_CORE_SPECIES = {
    "mosquito_beast": {
        "has_core": True,
        "element": "blood",
        "grade": "mid",
        "alchemy_use": "Mosquito Beast Queen core can refine blood-attribute pills; swarm cores used in mass-poison alchemy",
        "canon_note": "Wang Lin's mosquito swarm cores are collected after battles; the Queen's core is unique"
    },
    "thunder_toad": {
        "has_core": True,
        "element": "thunder",
        "grade": "high",
        "alchemy_use": "Thunder Toad core refines Thunder Tribulation Pills; enhances thunder-attribute techniques",
        "canon_note": "Wang Lin killed the Thunder Toad in Fire Burn Country and harvested its core"
    },
    "lei_ji_thunder_beast": {
        "has_core": True,
        "element": "thunder",
        "grade": "high",
        "alchemy_use": "Lei Ji core refines Celestial Thunder Pills; key ingredient for thunder-attribute breakthrough pills",
        "canon_note": "Lei Ji Thunder Beasts roam the Thunder Celestial Realm; their cores are prized by thunder cultivators"
    },
    "nether_beast": {
        "has_core": True,
        "element": "dark",
        "grade": "high",
        "alchemy_use": "Nether core refines Soul Refining materials; used in dark-attribute restriction creation",
        "canon_note": "Nether Beasts inhabit the Sea of Devils; their cores are used by Soul Refining Sect"
    },
    "flame_dragon": {
        "has_core": True,
        "element": "fire",
        "grade": "extreme",
        "alchemy_use": "Flame Dragon core refines Fire Origin Pills; supreme material for fire-attribute breakthrough pills",
        "canon_note": "The Fire Burn Country's tributed Flame Dragon; its core is a national treasure"
    },
    "cloud_whale": {
        "has_core": True,
        "element": "wind",
        "grade": "high",
        "alchemy_use": "Cloud Whale essence refines Wind Spirit Pills; enhances movement-speed pills and flight elixirs",
        "canon_note": "Cloud Whales swim through the cloud layer; their essence condensation trails are harvestable"
    },
    "thunder_celestial_beast": {
        "has_core": True,
        "element": "thunder",
        "grade": "extreme",
        "alchemy_use": "Thunder Celestial Beast core refines Nine-Tribulation Thunder Pills; ingredient for Immortal breakthrough",
        "canon_note": "Born inside heavenly tribulations; their cores embody tribulation lightning itself"
    },
    "brilliant_void": {
        "has_core": True,
        "element": "void",
        "grade": "extreme",
        "alchemy_use": "Brilliant Void core refines Spatial Rift Pills; enables void-attribute breakthrough materials",
        "canon_note": "Wang Lin's bonded void beast; its core bridges reality and void"
    },
    "golden_exalt_sea_dragon": {
        "has_core": True,
        "element": "water",
        "grade": "extreme",
        "alchemy_use": "Golden Sea Dragon core refines Ocean Sovereign Pills; supreme water-attribute alchemy material",
        "canon_note": "Wang Lin's bonded sea dragon companion; its core commands oceanic qi"
    },
}

# Lesser beast species — no condensed inner core
LESSER_BEAST_PATTERNS = [
    "spirit_rabbit", "spirit_fox", "spirit_deer", "spirit_horse",
    "spirit_fish", "spirit_rat", "spirit_bat", "spirit_toad",
    "zhao_spirit_horse", "wang_family_hunting_dog", "mountain_lion",
    "iron_bear", "spirit_bear", "web_weaver_spider", "sand_scorpion",
    "meat_jelly", "xiao_bai", "xiao_bai_beast",
]

# Combat AI mappings — species_id → combat_ai
COMBAT_AI_MAP = {
    "mosquito_beast": "swarm_aggressive",
    "mosquito_elite_gold_carapace": "swarm_aggressive",
    "mosquito_valley_ecosystem": "swarm_aggressive",
    "spirit_wolf": "pack_hunter",
    "spirit_wolf_pack_leader": "pack_hunter",
    "wind_wolf": "pack_hunter",
    "snow_domain_ice_wolf": "pack_hunter",
    "spirit_ape": "pack_hunter",
    "giant_savage_ape": "pack_hunter",
    "thunder_ape": "pack_hunter",
    "dragon_ape": "pack_hunter",
    "spirit_snake": "solo_ambush",
    "giant_python": "solo_ambush",
    "nine_headed_snake": "solo_ambush",
    "qiu_serpent": "solo_ambush",
    "chu_water_serpent": "solo_ambush",
    "southern_winged_snake": "solo_ambush",
    "ink_bat": "solo_ambush",
    "spirit_bat": "solo_ambush",
    "web_weaver_spider": "solo_ambush",
    "sun_moon_spider": "solo_ambush",
    "green_devil_scorpion": "solo_ambush",
    "pilu_poison_scorpion": "solo_ambush",
    "sand_scorpion": "solo_ambush",
    "giant_centipede": "solo_ambush",
    "corpse_yin_beast": "solo_ambush",
    "ghost_face_beast": "solo_ambush",
    "ghost_spirit_beast": "solo_ambush",
    "spirit_tortoise": "territorial_defender",
    "black_tortoise": "territorial_defender",
    "iron_bear": "territorial_defender",
    "spirit_bear": "territorial_defender",
    "teng_family_bodyguard_beast": "territorial_defender",
    "heavenly_fate_guardian_lion": "territorial_defender",
    "celestial_guardian_beast": "territorial_defender",
    "celestial_guard_beast": "territorial_defender",
    "suzaku_guardian_beast": "territorial_defender",
    "cloud_whale": "migration_passive",
    "cosmic_whale": "migration_passive",
    "spirit_crane": "migration_passive",
    "spirit_horse": "migration_passive",
    "zhao_spirit_horse": "migration_passive",
    "spirit_deer": "migration_passive",
    "seven_colored_deer": "migration_passive",
    "spirit_rabbit": "migration_passive",
    "spirit_fish": "migration_passive",
    "flame_dragon": "deity_level",
    "vermilion_bird": "deity_level",
    "azure_dragon": "deity_level",
    "white_tiger": "deity_level",
    "white_tiger_general": "deity_level",
    "black_tortoise": "deity_level",
    "qilin": "deity_level",
    "ice_phoenix": "deity_level",
    "nether_beast": "hive_coordinated",
    "ant_beast": "hive_coordinated",
    "soul_beast": "hive_coordinated",
    "devils_soul_beast": "hive_coordinated",
    "sea_of_devils_soul_beast": "hive_coordinated",
    "soul_lasher_beast": "hive_coordinated",
    "blood_beast": "hive_coordinated",
    "blood_ancestor_beast": "deity_level",
    "heaven_devouring_beast": "deity_level",
    "taichu_origin_beast": "deity_level",
    "miemie_origin_beast": "deity_level",
    "brilliant_void_beast": "deity_level",
    "brilliant_void": "deity_level",
    "heaven_defying_beast": "deity_level",
    "star_eating_beast": "deity_level",
    "thunder_celestial_beast": "deity_level",
    "golden_exalt_sea_dragon": "deity_level",
    "restriction_beast": "solo_ambush",
    "restriction_origin_beast": "deity_level",
    "thunder_origin_beast": "deity_level",
    "timescape_beast": "deity_level",
    "void_beast": "solo_ambush",
    "void_fissure_beast": "solo_ambush",
    "light_beast": "solo_ambush",
    "thunder_toad": "territorial_defender",
    "lei_ji_thunder_beast": "solo_ambush",
    "lei_ji": "solo_ambush",
    "ink_dragon": "deity_level",
    "cold_dragon": "deity_level",
    "earth_fire_dragon": "deity_level",
    "heavenly_bull_soul_beast": "deity_level",
    "ancient_dream_beast": "deity_level",
    "destiny_beast": "deity_level",
    "fire_burn_wild_boar": "territorial_defender",
    "tian_shui_spirit_fox": "solo_ambush",
    "spirit_fox": "solo_ambush",
    "puppet_beast": "territorial_defender",
    "giant_head_ancient_clansman": "deity_level",
    "foreign_battlefield_slaughter_beast": "swarm_aggressive",
    "slaughter_true_body_beast": "deity_level",
    "undying_ancient_body_beast": "deity_level",
    "immortal_celestial_body_beast": "deity_level",
    "dao_gu_indestructible_body_beast": "deity_level",
    "ancient_demon_clone": "deity_level",
    "ancient_demon_clone_beast": "deity_level",
    "ancient_devil_clone_beast": "deity_level",
    "black_fiend_devil_saint": "deity_level",
    "six_desire_devil": "deity_level",
    "berserk_beast": "swarm_aggressive",
    "wind_demon_beast": "solo_ambush",
    "devil_soul_beast": "hive_coordinated",
    "ji_qiong": "deity_level",
    "nine_colored_peacock": "deity_level",
    "five_elements_beast": "deity_level",
    "eternal_silence_beast": "deity_level",
    "wang_family_hunting_dog": "pack_hunter",
    "meat_jelly": "migration_passive",
    "xiao_bai": "pack_hunter",
    "xiao_bai_beast": "pack_hunter",
}


def compute_bloodline_tier(species_id, canonical):
    """Compute bloodline_tier from canonical fields."""
    cultivation = canonical.get("cultivation", "")
    bloodline = canonical.get("bloodline", "")
    mutations = canonical.get("mutations", [])
    intelligence = canonical.get("intelligence", 0)

    cultivation_str = str(cultivation).lower()
    bloodline_str = str(bloodline).lower()
    mutations_str = " ".join(str(m).lower() for m in mutations)

    # Check cultivation text for tier indicators
    if "origin" in cultivation_str and ("true body" in cultivation_str or "origin" in species_id.lower()):
        return "origin_true_body"
    if "ancient celestial" in cultivation_str or "vermilion bird" in cultivation_str or "divine beast" in cultivation_str:
        return "ancient_celestial_beast"
    if "desolate" in cultivation_str:
        return "desolate_beast"
    if ("spirit" in bloodline_str and intelligence >= 6) or "spiritual" in bloodline_str:
        return "spiritual_beast"
    if "mutated" in mutations_str:
        return "mutated_ordinary"
    # Also check cultivation range for high-tier indicators
    if any(t in cultivation_str for t in ["immortal", "ascendant", "heaven", "celestial"]):
        return "spiritual_beast"
    return "ordinary_beast"


def compute_inner_dan(species_id, canonical):
    """Compute inner_dan_properties from species_id and canonical fields."""
    sid = species_id.lower()

    # Origin True Body species → void element
    bloodline_tier = compute_bloodline_tier(species_id, canonical)
    if bloodline_tier == "origin_true_body":
        return {
            "has_core": True,
            "element": "void",
            "grade": "supreme",
            "alchemy_use": "Origin True Body core is beyond mortal alchemy; usable only by Origin-level cultivators",
            "canon_note": f"{species_id} — Origin True Body; its core contains primordial origin essence"
        }

    # Check the 9 special species
    if sid in INNER_DAN_CORE_SPECIES:
        return dict(INNER_DAN_CORE_SPECIES[sid])

    # Check lesser beast patterns
    for pattern in LESSER_BEAST_PATTERNS:
        if pattern in sid:
            return {
                "has_core": False,
                "element": None,
                "grade": None,
                "alchemy_use": "No alchemy value — common beast with no condensed inner core",
                "canon_note": f"{species_id} — common beast, no condensed inner core"
            }

    # Default for all other species
    return {
        "has_core": False,
        "element": None,
        "grade": None,
        "alchemy_use": None,
        "canon_note": f"{species_id} — no specific inner dan documented in canon"
    }


def compute_hive_mind(species_id):
    """Compute hive_mind_anchor from species_id."""
    sid = species_id.lower()
    if sid == "mosquito_beast":
        return {
            "is_hive": True,
            "queen_id": "mosquito_beast_queen",
            "swarm_id": "mosquito_valley_swarm",
            "coordination": "telepathic",
            "canon_note": "Mosquito Beast swarm is telepathically linked to the Queen; Wang Lin controls via Dream Dao"
        }
    return {
        "is_hive": False,
        "queen_id": None,
        "swarm_id": None,
        "coordination": None,
        "canon_note": f"{species_id} — not a hive-mind species"
    }


def compute_combat_ai(species_id):
    """Determine combat AI behavior."""
    return COMBAT_AI_MAP.get(species_id.lower(), "solo_ambush")


def upgrade_species(filepath):
    """Upgrade a single species JSON file. Returns True if modified."""
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False
    species_id = data.get("species_id", Path(filepath).stem)

    # Only add to canonical if it exists
    if "canonical" in data and isinstance(data["canonical"], dict):
        canonical = data["canonical"]

        # bloodline_tier (inside canonical)
        if "bloodline_tier" not in canonical:
            canonical["bloodline_tier"] = compute_bloodline_tier(species_id, canonical)
            modified = True
        # else: idempotent — already set, skip

        # inner_dan_properties (inside canonical)
        if "inner_dan_properties" not in canonical:
            canonical["inner_dan_properties"] = compute_inner_dan(species_id, canonical)
            modified = True

        # hive_mind_anchor (inside canonical)
        if "hive_mind_anchor" not in canonical:
            canonical["hive_mind_anchor"] = compute_hive_mind(species_id)
            modified = True

        # combat_ai (inside canonical)
        if "combat_ai" not in canonical:
            canonical["combat_ai"] = compute_combat_ai(species_id)
            modified = True

    # Mark schema version
    if "_xianxia_schema" not in data:
        data["_xianxia_schema"] = 2
        modified = True
    elif data.get("_xianxia_schema", 1) < 2:
        data["_xianxia_schema"] = 2
        modified = True

    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    return modified


# ──────────────────────────────────────────────────────
# 2. FACTION RELATIONSHIP UPGRADES
# ──────────────────────────────────────────────────────

# Canon-specific overrides for key factions
FACTION_SPIRITUAL_VEIN = {
    "heng_yue_sect": {
        "grade": "mid",
        "element": "wood",
        "qi_modifier": "Outer disciples cultivate 1.5x faster",
        "canon_note": "Heng Yue Sect sits on a mid-grade wood-aspected spiritual vein in the Zhao mountains"
    },
    "soul_refining_sect": {
        "grade": "high",
        "element": "dark",
        "qi_modifier": "Soul Refining techniques 2x effective within sect grounds",
        "canon_note": "Soul Refining Sect's high-grade dark vein amplifies soul manipulation techniques"
    },
    "teng_family": {
        "grade": "mid",
        "element": "metal",
        "qi_modifier": "Metal-attribute techniques enhanced within Teng Family compounds",
        "canon_note": "Teng Family's mid-grade metal vein supported their sword-cultivation traditions"
    },
    "heavenly_fate_sect": {
        "grade": "high",
        "element": None,
        "qi_modifier": "Divination accuracy enhanced; all-affinity cultivation",
        "canon_note": "Heavenly Fate Sect's high-grade null-element vein supports divination and all-element cultivation"
    },
    "vermilion_bird_divine_sect": {
        "grade": "supreme",
        "element": "fire",
        "qi_modifier": "All fire-attribute cultivators gain 3x comprehension speed; Vermilion Bird bloodline resonance",
        "canon_note": "Vermilion Bird Divine Sect sits on the apex fire-aspected spiritual vein of the star system"
    },
}

FACTION_HERITAGE_TREASURES = {
    "heng_yue_sect": [
        {
            "item_id": "flying_sword_hengyue",
            "name": "Heng Yue Flying Swords (set)",
            "status": "vaulted",
            "location_within_faction": "Sword Pavilion inner vault",
            "canon_note": None
        },
        {
            "item_id": "spirit_gathering_array",
            "name": "Spirit Gathering Array",
            "status": "active",
            "location_within_faction": "Sect center formation room",
            "canon_note": None
        }
    ],
    "soul_refining_sect": [
        {
            "item_id": "soul_refining_flag",
            "name": "Soul Refining Flag",
            "status": "sealed",
            "location_within_faction": "Forbidden chamber beneath sect",
            "canon_note": None
        },
        {
            "item_id": "restriction_flag",
            "name": "Restriction Flag set",
            "status": "active",
            "location_within_faction": "Xu Liqing's personal vault",
            "canon_note": None
        }
    ],
    "teng_family": [
        {
            "item_id": "teng_family_spirit_sword",
            "name": "Teng Family Spirit Sword Collection",
            "status": "destroyed",
            "location_within_faction": "Destroyed during Teng Family massacre",
            "canon_note": "Lost when Wang Lin destroyed the Teng Family"
        }
    ],
    "heavenly_fate_sect": [
        {
            "item_id": "heavenly_fate_seal",
            "name": "Heavenly Fate Seal",
            "status": "active",
            "location_within_faction": "Sect Leader's hall",
            "canon_note": None
        }
    ],
    "vermilion_bird_divine_sect": [
        {
            "item_id": "vermilion_bird_feather",
            "name": "Vermilion Bird Feather",
            "status": "sealed",
            "location_within_faction": "Inner sanctuary",
            "canon_note": None
        },
        {
            "item_id": "vermilion_emperor_seal",
            "name": "Vermilion Emperor Seal",
            "status": "active",
            "location_within_faction": "Emperor's throne room",
            "canon_note": None
        }
    ],
}

# Enriched relationship metadata for key factions
FACTION_RELATIONSHIP_METADATA = {
    "heng_yue_sect": {
        "allies": [
            {"id": "heavenly_fate_sect", "strength": "moderate", "since": "Pre-canon", "reason": "Mutual defense pact in Zhao region"}
        ],
        "enemies": [
            {"id": "teng_family", "strength": "sect_war", "since": "Pre-canon", "reason": "Teng Family expansion threatened Heng Yue territory"}
        ],
        "vassals": [],
        "rivals": [],
        "neutral": []
    },
    "soul_refining_sect": {
        "allies": [],
        "enemies": [
            {"id": "heng_yue_sect", "strength": "ancient", "since": "Pre-canon", "reason": "Xu Liqing's grudge against Wang Lin's origin sect"},
            {"id": "all_sect_war", "strength": "moderate", "since": "Pre-canon", "reason": "Orthodox sects oppose Soul Refining practices"}
        ],
        "vassals": [],
        "rivals": [],
        "neutral": []
    },
    "teng_family": {
        "allies": [],
        "enemies": [
            {"id": "wang_lin", "strength": "mortal_feud", "since": "Year 1 of Wang Lin's cultivation", "reason": "Teng Huayuan's assassination attempt; Wang Lin's massacre retaliation"}
        ],
        "vassals": [],
        "rivals": [],
        "neutral": []
    },
}

DEFAULT_RELATIONSHIP_METADATA = {
    "allies": [],
    "enemies": [],
    "vassals": [],
    "rivals": [],
    "neutral": []
}


def convert_relationship_list(data, key, template):
    """Convert flat ID list to enriched objects using a field template.
    
    template is a dict of field_name: default_value, e.g.:
      {"strength": None, "since": None, "reason": None}  # for allies/enemies
      {"obligation": None, "since": None}  # for vassals
    """
    flat = data.get(key, [])
    if not isinstance(flat, list):
        flat = []
    enriched = []
    for entry in flat:
        if isinstance(entry, str):
            obj = {"id": entry}
            obj.update(template)
            enriched.append(obj)
        elif isinstance(entry, dict) and "id" in entry:
            enriched.append(entry)
    return enriched


def upgrade_faction(filepath):
    """Upgrade a single faction_relationships JSON file. Returns True if modified."""
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False
    faction_id = data.get("faction_id", Path(filepath).stem)

    # spiritual_vein_grade
    if "spiritual_vein_grade" not in data:
        if faction_id in FACTION_SPIRITUAL_VEIN:
            data["spiritual_vein_grade"] = FACTION_SPIRITUAL_VEIN[faction_id]
        else:
            data["spiritual_vein_grade"] = {
                "grade": None,
                "element": None,
                "qi_modifier": None,
                "canon_note": f"{faction_id} — spiritual vein grade not specified in canon"
            }
        modified = True

    # heritage_treasures
    if "heritage_treasures" not in data:
        if faction_id in FACTION_HERITAGE_TREASURES:
            data["heritage_treasures"] = FACTION_HERITAGE_TREASURES[faction_id]
        else:
            data["heritage_treasures"] = []
        modified = True

    # relationship_metadata
    if "relationship_metadata" not in data:
        if faction_id in FACTION_RELATIONSHIP_METADATA:
            # Use the pre-defined canon metadata
            meta = FACTION_RELATIONSHIP_METADATA[faction_id]
        else:
            # Convert existing flat arrays
            meta = {
                "allies": convert_relationship_list(data, "allies", {"strength": None, "since": None, "reason": None}),
                "enemies": convert_relationship_list(data, "enemies", {"strength": None, "since": None, "reason": None}),
                "vassals": convert_relationship_list(data, "vassals", {"obligation": None, "since": None}),
                "rivals": convert_relationship_list(data, "rivals", {"competition_over": None, "intensity": None}),
                "neutral": convert_relationship_list(data, "neutral", {"context": None}),
            }
        data["relationship_metadata"] = meta
        modified = True

    # Mark schema version
    if "_xianxia_schema" not in data:
        data["_xianxia_schema"] = 2
        modified = True
    elif data.get("_xianxia_schema", 1) < 2:
        data["_xianxia_schema"] = 2
        modified = True

    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    return modified


# ──────────────────────────────────────────────────────
# 3. OPPORTUNITY (KARMIC NEXUS) UPGRADES
# ──────────────────────────────────────────────────────

OPPORTUNITY_KARMIC_WEIGHT = {
    "ri_heaven_defying_bead_discovery": {
        "value": 100,
        "type": "fate",
        "propagation": "global",
        "description": "The Heaven-Defying Bead is the single most karma-laden object in Wang Lin's journey. Its discovery triggers the entire chain of events."
    },
    "ri_heng_yue_sect_destruction": {
        "value": 80,
        "type": "grudge",
        "propagation": "regional",
        "description": "Wang Lin's massacre of the Teng Family creates a blood-karma that follows him through the Sea of Devils"
    },
    "ri_mosquito_valley_taming": {
        "value": 70,
        "type": "bond",
        "propagation": "continental",
        "description": "Taming the Mosquito Beast swarm creates a permanent soul-bond"
    },
    "ri_ancient_god_inheritance": {
        "value": 95,
        "type": "fate",
        "propagation": "global",
        "description": "Tu Si's Ancient God inheritance is the heaviest karmic event — it makes Wang Lin a target for every power in the Cultivation Planet"
    },
}

OPPORTUNITY_CONSEQUENCES = {
    "ri_heaven_defying_bead_discovery": [
        {
            "event_id": "wang_lin_cultivation_acceleration",
            "type": "realm_change",
            "target": "wang_lin",
            "description": "Wang Lin's cultivation speed increases dramatically",
            "delay": "immediate"
        },
        {
            "event_id": "teng_family_massacre",
            "type": "faction_change",
            "target": "teng_family",
            "description": "Teng Family attempts to steal the bead, leading to their destruction",
            "delay": "years"
        }
    ],
    "ri_heng_yue_sect_destruction": [
        {
            "event_id": "wang_lin_ruthless_personality",
            "type": "personality_shift",
            "target": "wang_lin",
            "description": "Wang Lin's personality locks to Ruthless/Dao of Slaughter",
            "delay": "immediate"
        },
        {
            "event_id": "soul_refining_sect_recruitment",
            "type": "faction_change",
            "target": "wang_lin",
            "description": "Xu Liqing notices Wang Lin's ruthlessness and recruits him",
            "delay": "years"
        }
    ],
    "ri_mosquito_valley_taming": [
        {
            "event_id": "mosquito_beast_companion",
            "type": "birth",
            "target": "mosquito_beast_queen",
            "description": "Mosquito Beast Queen becomes Wang Lin's companion",
            "delay": "immediate"
        },
        {
            "event_id": "swarm_combat_capability",
            "type": "realm_change",
            "target": "wang_lin",
            "description": "Wang Lin gains continent-scale destructive capability",
            "delay": "immediate"
        }
    ],
    "ri_ancient_god_inheritance": [
        {
            "event_id": "ancient_god_body_transformation",
            "type": "realm_change",
            "target": "wang_lin",
            "description": "Wang Lin gains the Ancient God's physical body and restriction powers",
            "delay": "immediate"
        },
        {
            "event_id": "suzaku_inheritance_conflict",
            "type": "faction_change",
            "target": "vermilion_bird_divine_sect",
            "description": "Vermilion Bird Divine Sect views Wang Lin as both asset and threat",
            "delay": "decades"
        }
    ],
}

DEFAULT_KARMIC_WEIGHT = {
    "value": 0,
    "type": None,
    "propagation": "none",
    "description": "No specific karmic consequence recorded in canon"
}


def upgrade_opportunity(filepath):
    """Upgrade a single opportunity JSON file. Returns True if modified."""
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False
    opp_id = data.get("opportunity_id", Path(filepath).stem)

    # karmic_weight
    if "karmic_weight" not in data:
        if opp_id in OPPORTUNITY_KARMIC_WEIGHT:
            data["karmic_weight"] = OPPORTUNITY_KARMIC_WEIGHT[opp_id]
        else:
            data["karmic_weight"] = dict(DEFAULT_KARMIC_WEIGHT)
        modified = True

    # consequences
    if "consequences" not in data:
        if opp_id in OPPORTUNITY_CONSEQUENCES:
            data["consequences"] = OPPORTUNITY_CONSEQUENCES[opp_id]
        else:
            data["consequences"] = []
        modified = True

    # Mark schema version
    if "_xianxia_schema" not in data:
        data["_xianxia_schema"] = 2
        modified = True
    elif data.get("_xianxia_schema", 1) < 2:
        data["_xianxia_schema"] = 2
        modified = True

    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    return modified


# ──────────────────────────────────────────────────────
# 4. WORLD STATE ENGINE SCHEMA UPGRADE
# ──────────────────────────────────────────────────────

def upgrade_engine(filepath):
    """Add Xianxia Primitives section to engine.json. Returns True if modified."""
    with open(filepath, "r", encoding="utf-8") as f:
        data = json.load(f)

    modified = False

    if "xianxia_primitives" not in data:
        data["xianxia_primitives"] = {
            "version": 2,
            "description": "Xianxia-specific schema primitives added to species, faction_relationships, and opportunities",
            "species_primitives": {
                "bloodline_tier": {
                    "location": "canonical.bloodline_tier",
                    "type": "string",
                    "enum": ["ordinary_beast", "mutated_ordinary", "spiritual_beast", "desolate_beast", "ancient_celestial_beast", "origin_true_body"],
                    "description": "Classifies a beast's bloodline purity. Derived from cultivation range, bloodline field, mutations, and intelligence."
                },
                "inner_dan_properties": {
                    "location": "canonical.inner_dan_properties",
                    "type": "object",
                    "fields": {
                        "has_core": "boolean — whether the species condenses an inner core",
                        "element": "string — fire/water/earth/metal/wood/thunder/wind/ice/dark/light/void/null",
                        "grade": "string — null/low/mid/high/extreme/supreme",
                        "alchemy_use": "string — what the core can be used for in alchemy (or null)",
                        "canon_note": "string — canon source note"
                    },
                    "description": "Describes a beast's condensed inner core (dan) for alchemy and cultivation purposes."
                },
                "hive_mind_anchor": {
                    "location": "canonical.hive_mind_anchor",
                    "type": "object",
                    "fields": {
                        "is_hive": "boolean",
                        "queen_id": "string — species_id of the queen (or null)",
                        "swarm_id": "string — unique swarm identifier (or null)",
                        "coordination": "string — telepathic/pherochemical/visual/null",
                        "canon_note": "string"
                    },
                    "description": "Tracks hive-mind swarm linkage for species like the Mosquito Beast."
                },
                "combat_ai": {
                    "location": "canonical.combat_ai",
                    "type": "string",
                    "enum": ["swarm_aggressive", "pack_hunter", "solo_ambush", "territorial_defender", "migration_passive", "hive_coordinated", "deity_level"],
                    "description": "Determines the species' default combat behavior pattern."
                }
            },
            "faction_primitives": {
                "spiritual_vein_grade": {
                    "location": "spiritual_vein_grade",
                    "type": "object",
                    "fields": {
                        "grade": "string — null/low/mid/high/extreme/supreme",
                        "element": "string — fire/water/earth/metal/wood/null",
                        "qi_modifier": "string — description of passive cultivation bonus",
                        "canon_note": "string"
                    },
                    "description": "The spiritual vein that powers a faction's territory."
                },
                "heritage_treasures": {
                    "location": "heritage_treasures",
                    "type": "array of objects",
                    "fields": {
                        "item_id": "string — matches item_properties or arsenal ID",
                        "name": "string",
                        "status": "string — vaulted/active/lost/sealed/destroyed",
                        "location_within_faction": "string — where it's kept",
                        "canon_note": "string"
                    },
                    "description": "Heritage treasures owned by the faction."
                },
                "relationship_metadata": {
                    "location": "relationship_metadata",
                    "type": "object",
                    "fields": {
                        "allies": "array of {id, strength, since, reason}",
                        "enemies": "array of {id, strength, since, reason}",
                        "vassals": "array of {id, obligation, since}",
                        "rivals": "array of {id, competition_over, intensity}",
                        "neutral": "array of {id, context}"
                    },
                    "description": "Enriched faction relationship data with metadata."
                }
            },
            "opportunity_primitives": {
                "karmic_weight": {
                    "location": "karmic_weight",
                    "type": "object",
                    "fields": {
                        "value": "integer — karmic magnitude",
                        "type": "string — grudge/debt/blessing/fate/null",
                        "propagation": "string — none/individual/faction/regional/continental/global",
                        "description": "string — what karmic consequence this event creates"
                    },
                    "description": "Measures the karmic weight of an opportunity event."
                },
                "consequences": {
                    "location": "consequences",
                    "type": "array of objects",
                    "fields": {
                        "event_id": "string — ID of a related future event or state change",
                        "type": "string — personality_shift/faction_change/item_transfer/location_change/realm_change/death/birth",
                        "target": "string — who/what is affected",
                        "description": "string",
                        "delay": "string — immediate/years/decades/centuries"
                    },
                    "description": "Downstream consequences triggered by this opportunity."
                }
            }
        }
        modified = True

    if modified:
        with open(filepath, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    return modified


# ──────────────────────────────────────────────────────
# MAIN
# ──────────────────────────────────────────────────────

def main():
    species_dir = BASE / "species"
    faction_dir = BASE / "faction_relationships"
    opportunity_dir = BASE / "opportunities"
    engine_path = BASE / "world_state" / "engine.json"

    species_modified = []
    faction_modified = []
    opportunity_modified = []

    # 1. Upgrade species
    for fpath in sorted(species_dir.glob("*.json")):
        if upgrade_species(fpath):
            species_modified.append(str(fpath))

    # 2. Upgrade faction relationships
    for fpath in sorted(faction_dir.glob("*.json")):
        if upgrade_faction(fpath):
            faction_modified.append(str(fpath))

    # 3. Upgrade opportunities
    for fpath in sorted(opportunity_dir.glob("*.json")):
        if upgrade_opportunity(fpath):
            opportunity_modified.append(str(fpath))

    # 4. Upgrade engine schema
    engine_modified = False
    if engine_path.exists():
        if upgrade_engine(engine_path):
            engine_modified = True
            print(f"[ENGINE] Modified: {engine_path}")
    else:
        print(f"[ENGINE] WARNING: {engine_path} not found")

    # ── Report ──
    print("\n" + "=" * 60)
    print("XIANXIA SCHEMA PRIMITIVES — UPGRADE REPORT")
    print("=" * 60)
    print(f"\nSpecies updated:      {len(species_modified)} / {len(list(species_dir.glob('*.json')))}")
    print(f"Faction rels updated: {len(faction_modified)} / {len(list(faction_dir.glob('*.json')))}")
    print(f"Opportunities updated: {len(opportunity_modified)} / {len(list(opportunity_dir.glob('*.json')))}")
    print(f"Engine schema updated: {'yes' if engine_modified else 'no'}")

    if species_modified:
        print(f"\n--- Species files modified ({len(species_modified)}) ---")
        for p in species_modified:
            print(f"  {p}")

    if faction_modified:
        print(f"\n--- Faction relationship files modified ({len(faction_modified)}) ---")
        for p in faction_modified:
            print(f"  {p}")

    if opportunity_modified:
        print(f"\n--- Opportunity files modified ({len(opportunity_modified)}) ---")
        for p in opportunity_modified:
            print(f"  {p}")

    total = len(species_modified) + len(faction_modified) + len(opportunity_modified) + (1 if engine_modified else 0)
    print(f"\nTotal files modified: {total}")


if __name__ == "__main__":
    main()