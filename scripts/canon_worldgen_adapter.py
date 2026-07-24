#!/usr/bin/env python3
"""
Canon Worldgen Adapter — Layer 3 of the Ergenverse architecture.

Reads ri_canon_database.json (Layer 1) and generates worldgen JSONs (Layer 4 input)
at scale. This is the bridge between canon data and Minecraft worldgen.

Architecture:
  Canon DB → Simulation Engine → [THIS ADAPTER] → Worldgen JSONs → Forge

Generation passes:
  1. Sub-region biomes (every country → 30-50+ sub-regions)
  2. Structure decomposition (every sect/city → 20-60+ components)
  3. Spirit vein systems (procedural vein variants)
  4. Herb ecological variants (growth stages × element types)
  5. Ocean layers (surface → abyss)
  6. Lang entries (auto-generated for all new content)

This script is IDEMPOTENT — re-running produces the same output unless the canon DB changed.
"""

import json
import os
import hashlib

# ============================================================
# PATHS
# ============================================================
FORGE = "/home/z/my-project/forge-mod"
CANON_DB = f"{FORGE}/ri_canon_database.json"
DATA = f"{FORGE}/src/main/resources/data/ergenverse"
BIOME_DIR = f"{DATA}/worldgen/biome"
CF_DIR = f"{DATA}/worldgen/configured_feature"
PF_DIR = f"{DATA}/worldgen/placed_feature"
STRUCT_DIR = f"{DATA}/worldgen/structure"
SS_DIR = f"{DATA}/worldgen/structure_set"
TP_DIR = f"{DATA}/worldgen/template_pool"
DT_DIR = f"{DATA}/dimension_type"
LANG_FILE = f"{FORGE}/src/main/resources/assets/ergenverse/lang/en_us.json"

# ============================================================
# CANON DATABASE LOADING
# ============================================================
def load_canon():
    with open(CANON_DB) as f:
        return json.load(f)

# ============================================================
# GENERATION TRACKING
# ============================================================
class GenerationReport:
    def __init__(self):
        self.created = {"biomes": 0, "configured_features": 0, "placed_features": 0,
                        "structures": 0, "structure_sets": 0, "template_pools": 0,
                        "lang_entries": 0}
        self.files = []
        self.provenance = {}  # filename -> canon_source

    def record(self, category, filepath, canon_source=None):
        self.created[category] = self.created.get(category, 0) + 1
        self.files.append(filepath)
        if canon_source:
            self.provenance[filepath] = canon_source

    def summary(self):
        total = sum(self.created.values())
        lines = [f"Canon Worldgen Adapter — Generation Report"]
        lines.append(f"Total new assets: {total}")
        for k, v in self.created.items():
            lines.append(f"  {k}: {v}")
        return "\n".join(lines)


# ============================================================
# HELPERS
# ============================================================
def write_json(filepath, data):
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

# ============================================================
# SCHEMA VALIDATORS — MC 1.20.1 format compliance
# These are the ROOT CAUSE fix. The generator previously
# produced invalid JSON that Forge silently rejected.
# ============================================================

def validate_biome(biome, name):
    """Validate a biome JSON against MC 1.20.1 schema requirements."""
    errors = []

    # Check carvers: MC 1.20.1 biome carvers are LISTS OF RESOURCE-LOCATION STRINGS
    # (references to configured carvers), NOT objects. Object form {"type":...}
    # is for ConfiguredCarver definitions, not biome references.
    # Correct:   "carvers": {"air": ["minecraft:cave", "minecraft:canyon"], "liquid": []}
    # WRONG:      "carvers": {"air": [{"type": "minecraft:cave"}], "liquid": []}
    for slot in ("air", "liquid"):
        entries = biome.get("carvers", {}).get(slot, [])
        for i, entry in enumerate(entries):
            if isinstance(entry, dict):
                errors.append(f"carvers.{slot}[{i}] is object {entry}, "
                              f"must be bare resource-location string (e.g. 'minecraft:cave')")
            elif not isinstance(entry, str):
                errors.append(f"carvers.{slot}[{i}] is {type(entry).__name__}, must be string")

    # Check features: must be 11-element list of lists
    features = biome.get("features", [])
    if len(features) != 11:
        errors.append(f"features has {len(features)} steps, must be exactly 11")
    for i, step in enumerate(features):
        if not isinstance(step, list):
            errors.append(f"features[{i}] is {type(step).__name__}, must be list")

    # Check spawners: each entry must have {type, weight, minSize, maxSize}
    for category, entries in biome.get("spawners", {}).items():
        for i, entry in enumerate(entries):
            if isinstance(entry, dict):
                for key in ("type", "weight", "minSize", "maxSize"):
                    if key not in entry:
                        errors.append(f"spawners.{category}[{i}] missing '{key}'")
            elif isinstance(entry, str):
                errors.append(f"spawners.{category}[{i}] is bare string '{entry}'")

    # Check required top-level keys
    for key in ("temperature", "downfall", "has_precipitation", "effects",
                "spawners", "spawn_costs", "carvers", "features"):
        if key not in biome:
            errors.append(f"missing required key '{key}'")

    if errors:
        msg = f"VALIDATION FAILED for biome '{name}':\n  " + "\n  ".join(errors)
        raise ValueError(msg)


def validate_structure(struct, name):
    """Validate a structure JSON against MC 1.20.1 schema requirements."""
    errors = []

    if struct.get("type") != "minecraft:jigsaw":
        errors.append(f"type is '{struct.get('type')}', expected 'minecraft:jigsaw'")

    # spawn_overrides: must be structured, not empty {}
    # MC 1.20.1 requires at least monster/creature/ambient/axolotls/water entries
    so = struct.get("spawn_overrides", {})
    if not so:
        # Generate proper empty spawn overrides
        struct["spawn_overrides"] = {
            "monster": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "creature": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "ambient": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "axolotls": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "underground_water_creature": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "water_ambient": {"bounding_box": "full", "max_count": 0, "spawns": []},
            "water_creature": {"bounding_box": "full", "max_count": 0, "spawns": []},
        }
    else:
        for key in ("monster", "creature", "ambient"):
            if key not in so:
                errors.append(f"spawn_overrides missing '{key}'")

    for key in ("start_pool", "size", "start_height", "project_start_to_heightmap"):
        if key not in struct:
            errors.append(f"missing required key '{key}'")

    # start_height.value must be a VerticalAnchor object: {"absolute":N} / {"above_bottom":N} / {"below_top":N}
    # NOT a raw integer. Raw int causes "Not a JSON object: 0" at registry load.
    sh = struct.get("start_height")
    if isinstance(sh, dict) and "value" in sh:
        v = sh["value"]
        if not isinstance(v, dict):
            errors.append(f"start_height.value is {type(v).__name__} ({v!r}), "
                          f"must be VerticalAnchor object e.g. {{'absolute': 0}}")
        elif not any(k in v for k in ("absolute", "above_bottom", "below_top")):
            errors.append(f"start_height.value {v} missing absolute/above_bottom/below_top key")

    if errors:
        msg = f"VALIDATION FAILED for structure '{name}':\n  " + "\n  ".join(errors)
        raise ValueError(msg)


def validate_structure_set(ss, name):
    """Validate a structure_set JSON."""
    errors = []
    if "structures" not in ss:
        errors.append("missing 'structures'")
    if "placement" not in ss:
        errors.append("missing 'placement'")
    else:
        p = ss["placement"]
        if p.get("type") == "minecraft:random_spread":
            for key in ("spacing", "separation", "salt"):
                if key not in p:
                    errors.append(f"placement missing '{key}'")
    if errors:
        msg = f"VALIDATION FAILED for structure_set '{name}':\n  " + "\n  ".join(errors)
        raise ValueError(msg)


def validate_template_pool(tp, name):
    """Validate a template_pool JSON.

    MC 1.20.1 requires each element to have the structure:
        {"weight": N, "element": {"element_type": "...", "location": "...", ...}}
    NOT the flat form where element_type/location are at the top level.
    """
    errors = []
    if "name" not in tp:
        errors.append("missing 'name'")
    if "fallback" not in tp:
        errors.append("missing 'fallback'")
    if "elements" not in tp:
        errors.append("missing 'elements'")
    else:
        for i, el in enumerate(tp["elements"]):
            if not isinstance(el, dict):
                errors.append(f"elements[{i}] is not an object")
                continue
            if "weight" not in el:
                errors.append(f"elements[{i}] missing 'weight'")
            if "element" not in el:
                errors.append(f"elements[{i}] missing 'element' wrapper "
                              f"(MC 1.20.1 requires {{weight, element: {{element_type, ...}}}})")
            else:
                inner = el["element"]
                if not isinstance(inner, dict):
                    errors.append(f"elements[{i}].element is not an object")
                elif "element_type" not in inner:
                    errors.append(f"elements[{i}].element missing 'element_type'")
    if errors:
        msg = f"VALIDATION FAILED for template_pool '{name}':\n  " + "\n  ".join(errors)
        raise ValueError(msg)


def validate_dimension_type(dt, name):
    """Validate a dimension_type JSON for MC 1.20.1 schema compliance.

    Catches the 'value_in_clamped_range' bug: MC 1.20.1 expects
    monster_spawn_light_level to be an IntProvider:
        {"type":"minecraft:uniform","value":{"min_inclusive":N,"max_inclusive":M}}
    NOT a flat object with 'value_in_clamped_range' + top-level min/max.
    """
    errors = []
    required = ["ultrawarm", "natural", "piglin_safe", "respawn_anchor_works",
                "bed_works", "has_raids", "has_skylight", "has_ceiling",
                "coordinate_scale", "ambient_light", "logical_height",
                "min_y", "height", "infiniburn", "effects"]
    for key in required:
        if key not in dt:
            errors.append(f"missing '{key}'")

    msl = dt.get("monster_spawn_light_level")
    if msl is not None:
        if isinstance(msl, dict):
            if "value_in_clamped_range" in msl:
                errors.append(
                    "monster_spawn_light_level uses deprecated 'value_in_clamped_range' format. "
                    "MC 1.20.1 requires {\"type\":\"minecraft:uniform\",\"value\":{\"min_inclusive\":N,\"max_inclusive\":M}}"
                )
            elif "type" in msl:
                if "value" not in msl:
                    errors.append("monster_spawn_light_level has 'type' but no 'value' IntProvider")
                else:
                    v = msl["value"]
                    if not isinstance(v, dict) or "min_inclusive" not in v or "max_inclusive" not in v:
                        errors.append("monster_spawn_light_level.value must have min_inclusive + max_inclusive")
        # integer form is also valid (literal light level) — no check needed.

    if "monster_spawn_block_light_limit" not in dt:
        errors.append("missing 'monster_spawn_block_light_limit' (required in 1.20.1)")

    if errors:
        msg = f"VALIDATION FAILED for dimension_type '{name}':\n  " + "\n  ".join(errors)
        raise ValueError(msg)


def make_11_step_features(step_map):
    """Build 11-step list-of-lists from {step_idx: [features]}."""
    steps = [[] for _ in range(11)]
    for idx, feats in step_map.items():
        if isinstance(feats, list):
            steps[idx] = feats
        else:
            steps[idx] = [feats]
    return steps

def salt_for(name, seed=0):
    """Generate a deterministic unique salt from a name."""
    h = hashlib.md5(f"{name}{seed}".encode()).hexdigest()
    return int(h[:8], 16) % 100000000

# Standard ore sets
ORES_STD = [
    "minecraft:ore_dirt", "minecraft:ore_gravel",
    "minecraft:ore_granite_upper", "minecraft:ore_granite_lower",
    "minecraft:ore_diorite_upper", "minecraft:ore_diorite_lower",
    "minecraft:ore_andesite_upper", "minecraft:ore_andesite_lower",
    "minecraft:ore_coal_upper", "minecraft:ore_coal_lower",
    "minecraft:ore_iron_upper", "minecraft:ore_iron_middle",
    "minecraft:ore_iron_small", "minecraft:ore_gold_upper",
    "minecraft:ore_gold_lower", "minecraft:ore_copper",
    "ergenverse:spirit_vein_quartz_ore",
]

ORES_RICH = ORES_STD + [
    "minecraft:ore_redstone", "minecraft:ore_lapis",
    "minecraft:ore_diamond", "minecraft:ore_emerald",
]

VEG_STD = [
    "minecraft:patch_taiga_grass", "minecraft:forest_flowers",
    "minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal",
    "minecraft:patch_sugar_cane", "minecraft:patch_pumpkin",
    "ergenverse:qi_gathering_grass",
]


# ============================================================
# PASS 1: SUB-REGION BIOMES
# ============================================================
# Every country expands into geographic + cultivation + historical sub-regions.

SUB_REGION_TEMPLATES = {
    # Geographic variants
    "farmland": {"temp_mod": 0.0, "downfall_mod": 0.1, "sky": 7907327, "veg": ["minecraft:patch_grass_normal", "minecraft:patch_pumpkin"]},
    "river_basin": {"temp_mod": -0.05, "downfall_mod": 0.2, "sky": 8355711, "veg": ["minecraft:patch_sugar_cane", "minecraft:patch_waterlily"]},
    "hills": {"temp_mod": -0.1, "downfall_mod": 0.0, "sky": 12694940, "veg": ["minecraft:patch_taiga_grass"]},
    "valley": {"temp_mod": 0.05, "downfall_mod": 0.1, "sky": 7907327, "veg": ["minecraft:forest_flowers"]},
    "forest": {"temp_mod": -0.05, "downfall_mod": 0.15, "sky": 8355711, "veg": ["minecraft:forest_flowers", "minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]},
    "marsh": {"temp_mod": 0.1, "downfall_mod": 0.3, "sky": 4210752, "veg": ["minecraft:patch_grass_normal", "minecraft:patch_waterlily", "minecraft:brown_mushroom_normal"]},
    "coast": {"temp_mod": 0.0, "downfall_mod": 0.2, "sky": 7907327, "veg": ["minecraft:patch_sugar_cane"]},
    "plateau": {"temp_mod": -0.15, "downfall_mod": -0.1, "sky": 12694940, "veg": ["minecraft:patch_taiga_grass"]},
    "canyon": {"temp_mod": 0.1, "downfall_mod": -0.1, "sky": 11184810, "veg": ["minecraft:patch_dead_bush"]},
    "mesa": {"temp_mod": 0.15, "downfall_mod": -0.15, "sky": 14118124, "veg": ["minecraft:patch_dead_bush"]},
    # Cultivation variants
    "spirit_herb_hills": {"temp_mod": -0.05, "downfall_mod": 0.1, "sky": 8355711, "veg": ["ergenverse:qi_gathering_grass", "ergenverse:foundation_root_vine"]},
    "broken_restriction_valley": {"temp_mod": 0.0, "downfall_mod": -0.1, "sky": 5916164, "veg": ["minecraft:patch_dead_bush"]},
    "cultivator_hunting_grounds": {"temp_mod": 0.05, "downfall_mod": 0.0, "sky": 7907327, "veg": ["minecraft:patch_grass_normal"]},
    "hidden_cave_network": {"temp_mod": -0.1, "downfall_mod": 0.0, "sky": 4210752, "veg": ["minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]},
    "collapsed_spirit_mine": {"temp_mod": 0.0, "downfall_mod": -0.1, "sky": 4210752, "veg": ["minecraft:patch_dead_bush"]},
    "minor_spirit_vein_region": {"temp_mod": -0.05, "downfall_mod": 0.05, "sky": 8355711, "veg": ["ergenverse:qi_gathering_grass"]},
    # Historical variants
    "ancient_battlefield": {"temp_mod": 0.0, "downfall_mod": -0.15, "sky": 4210752, "veg": ["minecraft:patch_dead_bush"]},
    "abandoned_battlefield": {"temp_mod": 0.05, "downfall_mod": -0.1, "sky": 5916164, "veg": ["minecraft:patch_dead_bush", "minecraft:brown_mushroom_normal"]},
    "old_caravan_road": {"temp_mod": 0.05, "downfall_mod": -0.05, "sky": 12694940, "veg": ["minecraft:patch_grass_normal"]},
    "ruined_outpost": {"temp_mod": 0.0, "downfall_mod": -0.1, "sky": 5916164, "veg": ["minecraft:patch_dead_bush"]},
    # Ecological variants
    "mortals_hunting_forest": {"temp_mod": -0.05, "downfall_mod": 0.15, "sky": 8355711, "veg": ["minecraft:forest_flowers", "minecraft:patch_taiga_grass"]},
    "fog_marsh": {"temp_mod": 0.1, "downfall_mod": 0.25, "sky": 4210752, "veg": ["minecraft:patch_waterlily", "minecraft:brown_mushroom_normal"]},
    "beast_territory": {"temp_mod": 0.0, "downfall_mod": 0.1, "sky": 7907327, "veg": ["minecraft:patch_grass_normal", "minecraft:patch_taiga_grass"]},
    "corrupted_zone": {"temp_mod": 0.15, "downfall_mod": -0.1, "sky": 4210752, "veg": ["minecraft:patch_dead_bush", "ergenverse:demon_corpse_mushroom"]},
}

COUNTRY_BASE_PARAMS = {
    "Country of Zhao": {"temp": 0.4, "downfall": 0.4, "tier": "fragile", "biome_prefix": "zhao"},
    "Chu Country": {"temp": 0.5, "downfall": 0.5, "tier": "fragile", "biome_prefix": "chu"},
    "Fire Burn Country": {"temp": 0.85, "downfall": 0.1, "tier": "fragile", "biome_prefix": "fire_burn"},
    "Sky Demon Country": {"temp": 0.7, "downfall": 0.2, "tier": "low", "biome_prefix": "sky_demon"},
    "Pilu Kingdom": {"temp": 0.6, "downfall": 0.15, "tier": "low", "biome_prefix": "pilu"},
    "Snow Domain Country": {"temp": -0.5, "downfall": 0.5, "tier": "fragile", "biome_prefix": "snow_domain"},
    "Xuan Wu": {"temp": 0.3, "downfall": 0.6, "tier": "fragile", "biome_prefix": "xuan_wu"},
    "Fire Demon Country": {"temp": 0.9, "downfall": 0.05, "tier": "low", "biome_prefix": "fire_demon"},
    "Vermilion Bird Country": {"temp": 0.65, "downfall": 0.35, "tier": "medium", "biome_prefix": "vermilion_bird"},
    "Great Wang Dynasty": {"temp": 0.6, "downfall": 0.4, "tier": "medium", "biome_prefix": "great_wang"},
    "Qing Shui Kingdom": {"temp": 0.4, "downfall": 0.3, "tier": "fragile", "biome_prefix": "qing_shui"},
}

def generate_sub_region_biomes(canon, report):
    """Pass 1: Expand every country into 24 sub-region biomes."""
    locations = canon["locations"]
    countries = [l for l in locations if l["type"] == "country" and l["name"] in COUNTRY_BASE_PARAMS]

    lang_entries = {}

    for country in countries:
        params = COUNTRY_BASE_PARAMS[country["name"]]
        prefix = params["biome_prefix"]
        base_temp = params["temp"]
        base_downfall = params["downfall"]
        tier = params["tier"]

        for region_key, template in SUB_REGION_TEMPLATES.items():
            biome_name = f"{prefix}_{region_key}"
            biome_id = f"ergenverse:{biome_name}"

            temp = max(-1.0, min(1.0, base_temp + template["temp_mod"]))
            downfall = max(0.0, min(1.0, base_downfall + template["downfall_mod"]))

            # Build features
            ores = list(ORES_STD)
            if tier in ("medium", "high"):
                ores = list(ORES_RICH)

            veg = list(template["veg"])
            # Add spirit vein to all sub-regions
            if "ergenverse:spirit_vein_quartz_ore" not in ores:
                ores.append("ergenverse:spirit_vein_quartz_ore")

            features = make_11_step_features({
                6: ores,
                7: ["minecraft:forest_rocks"],
                9: veg,
            })

            # Build spawners based on region type
            creatures = []
            monsters = []

            if region_key in ("farmland", "river_basin", "old_caravan_road"):
                creatures = [
                    {"type": "minecraft:sheep", "weight": 12, "minSize": 4, "maxSize": 4},
                    {"type": "minecraft:cow", "weight": 10, "minSize": 4, "maxSize": 4},
                    {"type": "minecraft:pig", "weight": 10, "minSize": 4, "maxSize": 4},
                ]
            elif region_key in ("forest", "mortals_hunting_forest", "beast_territory"):
                creatures = [
                    {"type": "minecraft:wolf", "weight": 10, "minSize": 4, "maxSize": 4},
                    {"type": "minecraft:fox", "weight": 8, "minSize": 2, "maxSize": 4},
                    {"type": "minecraft:rabbit", "weight": 10, "minSize": 2, "maxSize": 3},
                ]
            elif region_key == "coast":
                creatures = [{"type": "minecraft:turtle", "weight": 8, "minSize": 1, "maxSize": 1}]

            if region_key in ("ancient_battlefield", "abandoned_battlefield", "corrupted_zone", "collapsed_spirit_mine"):
                monsters = [
                    {"type": "minecraft:skeleton", "weight": 40, "minSize": 1, "maxSize": 3},
                    {"type": "minecraft:zombie", "weight": 30, "minSize": 2, "maxSize": 4},
                    {"type": "minecraft:creeper", "weight": 20, "minSize": 1, "maxSize": 1},
                ]
                if region_key == "corrupted_zone":
                    monsters.append({"type": "minecraft:witch", "weight": 20, "minSize": 1, "maxSize": 1})
            elif region_key in ("marsh", "fog_marsh"):
                monsters = [
                    {"type": "minecraft:slime", "weight": 30, "minSize": 1, "maxSize": 3},
                    {"type": "minecraft:witch", "weight": 15, "minSize": 1, "maxSize": 1},
                    {"type": "minecraft:spider", "weight": 25, "minSize": 1, "maxSize": 2},
                ]
            else:
                monsters = [
                    {"type": "minecraft:spider", "weight": 40, "minSize": 1, "maxSize": 2},
                    {"type": "minecraft:zombie", "weight": 30, "minSize": 2, "maxSize": 4},
                    {"type": "minecraft:skeleton", "weight": 25, "minSize": 1, "maxSize": 2},
                    {"type": "minecraft:creeper", "weight": 15, "minSize": 1, "maxSize": 1},
                ]

            # Carvers — MC 1.20.1 biome carvers are LISTS OF RESOURCE-LOCATION STRINGS
            # (references to configured carvers by ID), NOT objects.
            # Corrected from CRON-64 which wrongly used {"type":...} objects.
            carvers = ["minecraft:cave", "minecraft:cave_extra_underground"]
            if region_key in ("canyon", "hills", "plateau", "broken_restriction_valley", "hidden_cave_network"):
                carvers.append("minecraft:canyon")

            biome = {
                "_comment": f"{country['name']} — {region_key.replace('_', ' ').title()}. Auto-generated sub-region from canon location {country['id']} ({country['nameCn']}). Parent world-law tier: {tier}. Spirit veins: {country.get('spiritVeins', 'unknown')}. Prime Directive: this region exists objectively as part of {country['name']}; cultivation determines perception, not existence.",
                "temperature": temp,
                "downfall": downfall,
                "has_precipitation": downfall > 0.05,
                "effects": {
                    "sky_color": template["sky"],
                    "fog_color": template["sky"],
                    "water_color": 4159204,
                    "water_fog_color": 329011,
                    "grass_color_modifier": "none",
                },
                "spawners": {
                    "ambient": [{"type": "minecraft:bat", "weight": 10, "minSize": 8, "maxSize": 8}] if downfall > 0.1 else [],
                    "axolotls": [],
                    "creature": creatures,
                    "monster": monsters,
                    "underground_water_creature": [],
                    "water_ambient": [],
                    "water_creature": [],
                },
                "spawn_costs": {},
                "carvers": {"air": carvers, "liquid": []},
                "features": features,
            }

            # ── Schema validation: fail fast if format is wrong ──
            validate_biome(biome, biome_name)

            filepath = f"{BIOME_DIR}/{biome_name}.json"
            # Don't overwrite existing hand-tuned biomes
            if not os.path.exists(filepath):
                write_json(filepath, biome)
                report.record("biomes", filepath, country["id"])
                display_name = f"{country['name']} — {region_key.replace('_', ' ').title()}"
                lang_entries[f"biome.ergenverse.{biome_name}"] = display_name

    return lang_entries


# ============================================================
# PASS 2: STRUCTURE DECOMPOSITION
# ============================================================

SECT_COMPONENTS = [
    "outer_gate", "disciple_dormitories", "spirit_herb_garden", "sword_peak",
    "core_formation_hall", "library", "secret_pavilion", "spirit_beast_pens",
    "array_hall", "main_plaza", "inner_sect", "mountain_cave",
    "ancestor_hall", "hidden_treasury", "spirit_spring", "sword_tomb",
    "underground_passage", "trial_grounds", "puppet_workshop", "alchemy_courtyard",
]

CITY_COMPONENTS = [
    "city_gate", "market_district", "residential_district", "cultivator_quarter",
    "mortal_quarter", "port_docks", "governor_mansion", "tavern_district",
    "warehouse_district", "temple_district", "smuggler_tunnels",
]

# Sect name -> biome mapping (using existing biome names)
SECT_BIOME_MAP = {
    "Heng Yue Sect": "ergenverse:zhao_mountains",
    "Xuan Dao Sect": "ergenverse:chu_country",
    "Heavenly Fate Sect": "ergenverse:zhao_plains",
    "Soul Refining Sect": "ergenverse:pilu_kingdom",
    "Corpse Yin Sect": "ergenverse:sea_of_devils",
    "Fighting Evil Sect": "ergenverse:sea_of_devils",
    "Luo He Sect": "ergenverse:fire_burn_country",
    "Cloud Sky Sect": "ergenverse:chu_country",
}

CITY_BIOME_MAP = {
    "Tian Shui City": "ergenverse:zhao_plains",
    "Teng Family City": "ergenverse:zhao_mountains",
    "Wang Family Village": "ergenverse:zhao_plains",
    "Nan Dou City": "ergenverse:zhao_plains",
    "Qilin City": "ergenverse:zhao_plains",
    "Ancient Demon City": "ergenverse:sky_demon_country",
    "Great Wang Capital": "ergenverse:great_wang_dynasty",
}

POOL_TEMPLATES = {
    "plains": ("minecraft:village/plains/terminators", [
        "minecraft:village/plains/houses/plains_small_house_1",
        "minecraft:village/plains/houses/plains_medium_house_1",
        "minecraft:village/plains/houses/plains_large_house_1",
        "minecraft:village/plains/houses/plains_butcher_shop_1",
        "minecraft:village/plains/houses/plains_fletcher_house_1",
        "minecraft:village/plains/houses/plains_masons_house_1",
        "minecraft:village/plains/houses/plains_shepherds_house_1",
        "minecraft:village/plains/houses/plains_armorer_house_1",
        "minecraft:village/plains/houses/plains_cartographer_house_1",
        "minecraft:village/plains/houses/plains_library_1",
    ]),
    "desert": ("minecraft:village/desert/terminators", [
        "minecraft:village/desert/houses/desert_small_house_1",
        "minecraft:village/desert/houses/desert_medium_house_1",
        "minecraft:village/desert/houses/desert_large_house_1",
        "minecraft:village/desert/houses/desert_butcher_shop_1",
        "minecraft:village/desert/houses/desert_fletcher_house_1",
        "minecraft:village/desert/houses/desert_masons_house_1",
    ]),
    "taiga": ("minecraft:village/taiga/terminators", [
        "minecraft:village/taiga/houses/taiga_small_house_1",
        "minecraft:village/taiga/houses/taiga_medium_house_1",
        "minecraft:village/taiga/houses/taiga_large_house_1",
        "minecraft:village/taiga/houses/taiga_butcher_shop_1",
        "minecraft:village/taiga/houses/taiga_armorer_house_1",
    ]),
    "nether": ("minecraft:empty", [
        "minecraft:nether_fortress/corridor_1",
        "minecraft:nether_fortress/corridor_2",
        "minecraft:nether_fortress/corridor_3",
        "minecraft:nether_fortress/corridor_4",
        "minecraft:nether_fortress/bridge_end_right",
        "minecraft:nether_fortress/bridge_end_left",
        "minecraft:nether_fortress/bridge_crossing",
        "minecraft:nether_fortress/entrance_1",
    ]),
    "woodland": ("minecraft:empty", [
        "minecraft:woodland_mansion/entrance_01",
        "minecraft:woodland_mansion/entrance_02",
        "minecraft:woodland_mansion/1x1_a1",
        "minecraft:woodland_mansion/1x1_a2",
        "minecraft:woodland_mansion/1x1_b1",
        "minecraft:woodland_mansion/1x1_b2",
        "minecraft:woodland_mansion/1x2_a1",
        "minecraft:woodland_mansion/1x2_a2",
        "minecraft:woodland_mansion/1x2_b1",
        "minecraft:woodland_mansion/1x2_b2",
        "minecraft:woodland_mansion/2x2_a1",
        "minecraft:woodland_mansion/2x2_a2",
        "minecraft:woodland_mansion/2x2_b1",
        "minecraft:woodland_mansion/2x2_b2",
    ]),
}

def pick_pool_type(biome):
    """Pick a vanilla pool template based on biome."""
    if "mountain" in biome or "peak" in biome:
        return "taiga"
    if "fire" in biome or "demon" in biome or "devil" in biome:
        return "nether"
    if "snow" in biome:
        return "taiga"
    return "plains"

def generate_structure_components(canon, report):
    """Pass 2: Decompose sects and cities into individual structure components."""
    lang_entries = {}

    # Process sects from the SECT_BIOME_MAP
    for sect_name, biome in SECT_BIOME_MAP.items():
        # Find canon location if it exists
        canon_loc = None
        for loc in canon["locations"]:
            if sect_name.lower() in loc["name"].lower():
                canon_loc = loc
                break

        canon_id = canon_loc["id"] if canon_loc else "inferred"
        canon_cn = canon_loc.get("nameCn", "") if canon_loc else ""

        # Generate a slug from sect name
        slug = sect_name.lower().replace(" ", "_").replace("'", "")

        pool_type = pick_pool_type(biome)
        fallback, elements = POOL_TEMPLATES[pool_type]

        for i, component in enumerate(SECT_COMPONENTS):
            struct_name = f"{slug}_{component}"
            element = elements[i % len(elements)]

            # Structure JSON
            struct = {
                "_comment": f"{sect_name} — {component.replace('_', ' ').title()}. Auto-generated structure component from canon {'location ' + canon_id if canon_loc else 'faction reference'}. Part of {sect_name} ({canon_cn}). Prime Directive: this structure exists as part of the sect's physical grounds.",
                "type": "minecraft:jigsaw",
                "biomes": biome,
                "step": "surface_structures",
                "spawn_overrides": {},
                "start_pool": f"ergenverse:{struct_name}/start_pool",
                "size": 4,
                "start_height": {"type": "minecraft:constant", "value": {"absolute": 0}},
                "project_start_to_heightmap": "WORLD_SURFACE_WG",
                "max_distance_from_center": 80,
                "use_expansion_hack": False,
            }
            # ── Validate structure before writing ──
            validate_structure(struct, struct_name)

            struct_path = f"{STRUCT_DIR}/{struct_name}.json"
            if not os.path.exists(struct_path):
                write_json(struct_path, struct)
                report.record("structures", struct_path, canon_id)

            # Structure Set JSON
            ss = {
                "structures": [{"structure": f"ergenverse:{struct_name}", "weight": 1}],
                "placement": {
                    "type": "minecraft:random_spread",
                    "spacing": 20 + (i * 2),
                    "separation": 8,
                    "salt": salt_for(struct_name),
                },
            }
            validate_structure_set(ss, struct_name)
            ss_path = f"{SS_DIR}/{struct_name}.json"
            if not os.path.exists(ss_path):
                write_json(ss_path, ss)
                report.record("structure_sets", ss_path, canon_id)

            # Template Pool JSON
            tp = {
                "name": f"ergenverse:{struct_name}/start_pool",
                "fallback": fallback,
                "elements": [{
                    "weight": 1,
                    "element": {
                        "element_type": "minecraft:single_pool_element",
                        "location": element,
                        "projection": "rigid",
                        "processors": "minecraft:empty",
                    },
                }],
            }
            validate_template_pool(tp, struct_name)
            tp_path = f"{TP_DIR}/{struct_name}/start_pool.json"
            if not os.path.exists(tp_path):
                write_json(tp_path, tp)
                report.record("template_pools", tp_path, canon_id)

            lang_entries[f"structure.ergenverse.{struct_name}"] = f"{sect_name} — {component.replace('_', ' ').title()}"

    # Process cities
    for city_name, biome in CITY_BIOME_MAP.items():
        canon_loc = None
        for loc in canon["locations"]:
            if city_name.lower() in loc["name"].lower():
                canon_loc = loc
                break

        canon_id = canon_loc["id"] if canon_loc else "inferred"
        canon_cn = canon_loc.get("nameCn", "") if canon_loc else ""
        slug = city_name.lower().replace(" ", "_").replace("'", "")

        pool_type = pick_pool_type(biome)
        fallback, elements = POOL_TEMPLATES[pool_type]

        for i, component in enumerate(CITY_COMPONENTS):
            struct_name = f"{slug}_{component}"
            element = elements[i % len(elements)]

            struct = {
                "_comment": f"{city_name} — {component.replace('_', ' ').title()}. Auto-generated city component from canon location {canon_id} ({canon_cn}). Prime Directive: this district exists as part of the city's physical layout.",
                "type": "minecraft:jigsaw",
                "biomes": biome,
                "step": "surface_structures",
                "spawn_overrides": {},
                "start_pool": f"ergenverse:{struct_name}/start_pool",
                "size": 5,
                "start_height": {"type": "minecraft:constant", "value": {"absolute": 0}},
                "project_start_to_heightmap": "WORLD_SURFACE_WG",
                "max_distance_from_center": 80,
                "use_expansion_hack": False,
            }
            validate_structure(struct, struct_name)
            struct_path = f"{STRUCT_DIR}/{struct_name}.json"
            if not os.path.exists(struct_path):
                write_json(struct_path, struct)
                report.record("structures", struct_path, canon_id)

            ss = {
                "structures": [{"structure": f"ergenverse:{struct_name}", "weight": 1}],
                "placement": {
                    "type": "minecraft:random_spread",
                    "spacing": 16 + (i * 2),
                    "separation": 6,
                    "salt": salt_for(struct_name),
                },
            }
            validate_structure_set(ss, struct_name)
            ss_path = f"{SS_DIR}/{struct_name}.json"
            if not os.path.exists(ss_path):
                write_json(ss_path, ss)
                report.record("structure_sets", ss_path, canon_id)

            tp = {
                "name": f"ergenverse:{struct_name}/start_pool",
                "fallback": fallback,
                "elements": [{
                    "weight": 1,
                    "element": {
                        "element_type": "minecraft:single_pool_element",
                        "location": element,
                        "projection": "rigid",
                        "processors": "minecraft:empty",
                    },
                }],
            }
            validate_template_pool(tp, struct_name)
            tp_path = f"{TP_DIR}/{struct_name}/start_pool.json"
            if not os.path.exists(tp_path):
                write_json(tp_path, tp)
                report.record("template_pools", tp_path, canon_id)

            lang_entries[f"structure.ergenverse.{struct_name}"] = f"{city_name} — {component.replace('_', ' ').title()}"

    return lang_entries


# ============================================================
# PASS 3: SPIRIT VEIN SYSTEMS
# ============================================================

VEIN_TIERS = {
    "minor": {"size": 3, "count": 4, "rarity": None, "block": "minecraft:quartz_ore", "comment": "Minor spirit vein — thin Qi flow, common in low-tier countries."},
    "major": {"size": 8, "count": 2, "rarity": None, "block": "minecraft:quartz_ore", "comment": "Major spirit vein — dense Qi flow, rare, found in medium+ tier regions."},
    "branch": {"size": 2, "count": 6, "rarity": None, "block": "minecraft:quartz_ore", "comment": "Branch vein — offshoot of a major vein, thin but widely spread."},
    "dead": {"size": 4, "count": 2, "rarity": None, "block": "minecraft:dead_tube_coral_block", "comment": "Dead spirit vein — Qi has dried up, leaving inert stone."},
    "contaminated": {"size": 5, "count": 2, "rarity": None, "block": "minecraft:gray_concrete", "comment": "Contaminated spirit vein — corrupted by demonic energy."},
}

VEIN_ELEMENTS = {
    "wood": {"block": "minecraft:oak_log", "comment": "Wood-element spirit vein — found in forested regions."},
    "fire": {"block": "minecraft:magma_block", "comment": "Fire-element spirit vein — found in volcanic regions."},
    "earth": {"block": "minecraft:terracotta", "comment": "Earth-element spirit vein — found in mountainous regions."},
    "metal": {"block": "minecraft:iron_block", "comment": "Metal-element spirit vein — found in ore-rich regions."},
    "water": {"block": "minecraft:sea_lantern", "comment": "Water-element spirit vein — found near water bodies."},
    "heavenly": {"block": "minecraft:diamond_block", "comment": "Heavenly spirit vein — transcendent tier, extremely rare."},
    "ancient": {"block": "minecraft:lodestone", "comment": "Ancient spirit vein — predates current civilization."},
    "broken": {"block": "minecraft:cobblestone", "comment": "Broken spirit vein — damaged by ancient conflict."},
    "concealed": {"block": "minecraft:obsidian", "comment": "Concealed spirit vein — hidden by ancient restrictions."},
    "beast": {"block": "minecraft:bone_block", "comment": "Beast spirit vein — formed around a spirit beast's corpse."},
    "dragon": {"block": "minecraft:gold_block", "comment": "Dragon spirit vein — the rarest, formed by dragon Qi."},
}

def generate_spirit_vein_systems(canon, report):
    """Pass 3: Generate spirit vein variants (tiers × elements)."""
    lang_entries = {}

    # Tier variants
    for tier_name, tier_data in VEIN_TIERS.items():
        vein_name = f"spirit_vein_{tier_name}"

        # Configured feature
        cf = {
            "_comment": f"{tier_data['comment']} Auto-generated by Canon Worldgen Adapter. Prime Directive: spirit veins are objective geological features.",
            "type": "minecraft:ore",
            "config": {
                "size": tier_data["size"],
                "discard_chance_on_air_exposure": 0.3 if tier_name != "dead" else 0.0,
                "targets": [{
                    "target": {
                        "predicate": {"type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"},
                        "state": {"Name": tier_data["block"]}
                    },
                    "size": tier_data["size"]
                }, {
                    "target": {
                        "predicate": {"type": "minecraft:tag_match", "tag": "minecraft:deepslate_ore_replaceables"},
                        "state": {"Name": tier_data["block"]}
                    },
                    "size": tier_data["size"]
                }]
            }
        }
        cf_path = f"{CF_DIR}/{vein_name}.json"
        if not os.path.exists(cf_path):
            write_json(cf_path, cf)
            report.record("configured_features", cf_path, "ecological")

        # Placed feature
        pf = {
            "feature": f"ergenverse:{vein_name}",
            "placement": [
                {"type": "minecraft:count", "count": tier_data["count"]},
                {"type": "minecraft:in_square"},
                {"type": "minecraft:height_range",
                 "min": {"absolute": -64},
                 "max": {"absolute": 16}},
                {"type": "minecraft:biome"}
            ]
        }
        pf_path = f"{PF_DIR}/{vein_name}.json"
        if not os.path.exists(pf_path):
            write_json(pf_path, pf)
            report.record("placed_features", pf_path, "ecological")

    # Elemental variants
    for elem_name, elem_data in VEIN_ELEMENTS.items():
        vein_name = f"spirit_vein_{elem_name}"

        cf = {
            "_comment": f"{elem_data['comment']} Auto-generated elemental spirit vein variant. Prime Directive: the element is an objective property of the vein's Qi.",
            "type": "minecraft:ore",
            "config": {
                "size": 5,
                "discard_chance_on_air_exposure": 0.2,
                "targets": [{
                    "target": {
                        "predicate": {"type": "minecraft:tag_match", "tag": "minecraft:stone_ore_replaceables"},
                        "state": {"Name": elem_data["block"]}
                    },
                    "size": 3
                }]
            }
        }
        cf_path = f"{CF_DIR}/{vein_name}.json"
        if not os.path.exists(cf_path):
            write_json(cf_path, cf)
            report.record("configured_features", cf_path, "ecological")

        pf = {
            "feature": f"ergenverse:{vein_name}",
            "placement": [
                {"type": "minecraft:rarity_filter", "chance": 20 if elem_name not in ("heavenly", "dragon") else 80},
                {"type": "minecraft:in_square"},
                {"type": "minecraft:height_range",
                 "min": {"absolute": -48},
                 "max": {"absolute": 32}},
                {"type": "minecraft:biome"}
            ]
        }
        pf_path = f"{PF_DIR}/{vein_name}.json"
        if not os.path.exists(pf_path):
            write_json(pf_path, pf)
            report.record("placed_features", pf_path, "ecological")

    return lang_entries


# ============================================================
# PASS 4: HERB ECOLOGICAL VARIANTS
# ============================================================

BASE_HERBS = {
    "qi_gathering_grass": {"block": "minecraft:fern", "base_rarity": 4},
    "snow_heart_herb": {"block": "minecraft:lily_of_the_valley", "base_rarity": 8},
    "fire_bloom_lotus": {"block": "minecraft:orange_tulip", "base_rarity": 10},
    "vermilion_blood_ginseng": {"block": "minecraft:wither_rose", "base_rarity": 5},
    "sword_edge_moss": {"block": "minecraft:small_dripleaf", "base_rarity": 7},
    "demon_corpse_mushroom": {"block": "minecraft:brown_mushroom", "base_rarity": 6},
    "foundation_root_vine": {"block": "minecraft:spore_blossom", "base_rarity": 8},
    "nine_leaf_clover": {"block": "minecraft:oxeye_daisy", "base_rarity": 24},
    "soul_nourishing_lotus": {"block": "minecraft:lily_of_the_valley", "base_rarity": 32},
    "five_color_ginseng": {"block": "minecraft:allium", "base_rarity": 40},
    "blood_forgetting_grass": {"block": "minecraft:cornflower", "base_rarity": 30},
    "dao_trace_vine": {"block": "minecraft:lilac", "base_rarity": 48},
    "heart_devil_flower": {"block": "minecraft:wither_rose", "base_rarity": 50},
    "reincarnation_lily": {"block": "minecraft:white_tulip", "base_rarity": 80},
    "void_nether_grass": {"block": "minecraft:spore_blossom", "base_rarity": 60},
}

HERB_VARIANTS = {
    "seedling": {"rarity_mult": 2.0, "block_override": None, "comment": "Early growth stage — common, low potency."},
    "young": {"rarity_mult": 1.5, "block_override": None, "comment": "Young plant — moderate rarity."},
    "mature": {"rarity_mult": 1.0, "block_override": None, "comment": "Mature plant — standard rarity, full potency."},
    "ancient": {"rarity_mult": 0.3, "block_override": None, "comment": "Ancient specimen — extremely rare, peak potency."},
    "spirit": {"rarity_mult": 0.5, "block_override": None, "comment": "Spirit variant — infused with concentrated Qi."},
    "tribulation": {"rarity_mult": 0.1, "block_override": None, "comment": "Tribulation variant — survived heavenly tribulation, transcendent."},
    "corrupted": {"rarity_mult": 0.8, "block_override": "minecraft:wither_rose", "comment": "Corrupted variant — tainted by demonic energy."},
    "dormant": {"rarity_mult": 1.2, "block_override": "minecraft:dead_bush", "comment": "Dormant variant — appears dead, will revive under right conditions."},
}

def generate_herb_variants(canon, report):
    """Pass 4: Generate ecological variants of every herb."""
    lang_entries = {}

    for herb_name, herb_data in BASE_HERBS.items():
        for variant_name, variant_data in HERB_VARIANTS.items():
            full_name = f"{herb_name}_{variant_name}"
            block = variant_data["block_override"] or herb_data["block"]
            rarity = max(1, int(herb_data["base_rarity"] / variant_data["rarity_mult"]))

            # Configured feature
            cf = {
                "_comment": f"{herb_name} — {variant_name} variant. {variant_data['comment']} Auto-generated ecological variant. Prime Directive: the variant is an objective growth state of the herb.",
                "type": "minecraft:flower",
                "config": {
                    "features": [{"feature": "minecraft:simple_block", "placement": [
                        {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:all_of", "predicates": [
                            {"type": "minecraft:matching_blocks", "offset": [0, -1, 0], "blocks": ["minecraft:grass_block", "minecraft:dirt", "minecraft:podzol", "minecraft:coarse_dirt", "minecraft:moss_block"]}
                        ]}}
                    ]}],
                    "tries": 1,
                    "xz_spread": 16,
                    "y_spread": 2,
                    "need_red_to_survive": False,
                }
            }
            cf_path = f"{CF_DIR}/{full_name}.json"
            if not os.path.exists(cf_path):
                write_json(cf_path, cf)
                report.record("configured_features", cf_path, "ecological")

            # Placed feature
            pf = {
                "feature": f"ergenverse:{full_name}",
                "placement": [
                    {"type": "minecraft:rarity_filter", "chance": rarity},
                    {"type": "minecraft:in_square"},
                    {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
                    {"type": "minecraft:biome"}
                ]
            }
            pf_path = f"{PF_DIR}/{full_name}.json"
            if not os.path.exists(pf_path):
                write_json(pf_path, pf)
                report.record("placed_features", pf_path, "ecological")

    return lang_entries


# ============================================================
# PASS 5: OCEAN LAYERS
# ============================================================

OCEAN_LAYERS = {
    "ocean_surface": {"temp": 0.7, "depth": "surface", "water_color": 4159204, "monsters": ["minecraft:drowned", "minecraft:cod"], "comment": "Ocean surface layer — sunlit, calm, common sea life."},
    "ocean_shallow": {"temp": 0.6, "depth": "shallow", "water_color": 6388580, "monsters": ["minecraft:drowned", "minecraft:cod", "minecraft:salmon"], "comment": "Shallow ocean — continental shelf, kelp forests."},
    "ocean_open": {"temp": 0.5, "depth": "open", "water_color": 4159204, "monsters": ["minecraft:drowned", "minecraft:guardian"], "comment": "Open ocean — deep water, guardians patrol."},
    "ocean_sunlight": {"temp": 0.4, "depth": "sunlight", "water_color": 329011, "monsters": ["minecraft:guardian", "minecraft:squid"], "comment": "Sunlight zone — upper photic, last light reaches here."},
    "ocean_twilight": {"temp": 0.2, "depth": "twilight", "water_color": 270131, "monsters": ["minecraft:guardian", "minecraft:glow_squid"], "comment": "Twilight zone — dim, bioluminescent creatures."},
    "ocean_dark": {"temp": 0.0, "depth": "dark", "water_color": 13108, "monsters": ["minecraft:elder_guardian", "minecraft:glow_squid"], "comment": "Dark zone — no sunlight, high pressure."},
    "ocean_abyss": {"temp": -0.3, "depth": "abyss", "water_color": 65793, "monsters": ["minecraft:elder_guardian", "minecraft:wither_skeleton"], "comment": "Abyss — crushing pressure, ancient beasts."},
    "ocean_trench": {"temp": -0.5, "depth": "trench", "water_color": 25600, "monsters": ["minecraft:elder_guardian", "minecraft:wither_skeleton", "minecraft:blaze"], "comment": "Trench — deepest ocean, volcanic vents."},
    "ocean_spirit_abyss": {"temp": -0.7, "depth": "spirit_abyss", "water_color": 13107, "monsters": ["minecraft:elder_guardian", "minecraft:wither_skeleton", "minecraft:blaze", "minecraft:ghast"], "comment": "Spirit abyss — where ancient spirit beasts sleep. Canon: huge beasts exist here, not as boss arenas but as ecology."},
    "ocean_ancient_abyss": {"temp": -0.9, "depth": "ancient_abyss", "water_color": 256, "monsters": ["minecraft:elder_guardian", "minecraft:wither_skeleton", "minecraft:blaze", "minecraft:ghast", "minecraft:warden"], "comment": "Ancient abyss — pre-civilization depths. Things live here that predate the Sealed Realm."},
}

def generate_ocean_layers(canon, report):
    """Pass 5: Generate layered ocean biomes."""
    lang_entries = {}

    for layer_name, layer_data in OCEAN_LAYERS.items():
        # Build monster spawners
        monsters = []
        weights = {"minecraft:drowned": 40, "minecraft:cod": 20, "minecraft:salmon": 15,
                   "minecraft:guardian": 25, "minecraft:squid": 15, "minecraft:glow_squid": 20,
                   "minecraft:elder_guardian": 5, "minecraft:wither_skeleton": 15,
                   "minecraft:blaze": 10, "minecraft:ghast": 8, "minecraft:warden": 1}
        for m in layer_data["monsters"]:
            monsters.append({"type": m, "weight": weights.get(m, 10), "minSize": 1, "maxSize": 2})

        biome = {
            "_comment": f"{layer_data['comment']} Auto-generated ocean layer. Prime Directive: the ocean layers exist objectively — the beasts that live in the deep are real, not spawned for the player.",
            "temperature": layer_data["temp"],
            "downfall": 0.5,
            "has_precipitation": True,
            "effects": {
                "sky_color": 8355711 if layer_data["temp"] > 0 else 4210752,
                "fog_color": 4210752 if layer_data["temp"] <= 0 else 12694940,
                "water_color": layer_data["water_color"],
                "water_fog_color": 270131 if layer_data["temp"] < 0 else 329011,
                "grass_color_modifier": "none",
            },
            "spawners": {
                "ambient": [{"type": "minecraft:bat", "weight": 10, "minSize": 8, "maxSize": 8}],
                "axolotls": [],
                "creature": [],
                "monster": monsters,
                "underground_water_creature": [{"type": "minecraft:glow_squid", "weight": 10, "minSize": 3, "maxSize": 5}] if layer_data["temp"] < 0.3 else [],
                "water_ambient": [{"type": "minecraft:cod", "weight": 10, "minSize": 3, "maxSize": 6}] if layer_data["temp"] > 0.3 else [],
                "water_creature": [{"type": "minecraft:squid", "weight": 10, "minSize": 1, "maxSize": 4}] if layer_data["temp"] > 0.3 else [],
            },
            "spawn_costs": {},
            # Carvers — MC 1.20.1: bare resource-location strings
            "carvers": {"air": ["minecraft:cave"], "liquid": []},
            "features": make_11_step_features({
                6: ["minecraft:ore_dirt", "minecraft:ore_gravel", "minecraft:ore_coal_upper",
                    "minecraft:ore_iron_upper", "minecraft:ore_iron_middle",
                    "minecraft:ore_gold_upper", "minecraft:ore_copper",
                    "ergenverse:spirit_vein_quartz_ore"],
                9: ["minecraft:patch_seagrass", "minecraft:patch_kelp"] if layer_data["temp"] > 0.4 else ["minecraft:patch_dead_bush"],
            }),
        }

        biome_path = f"{BIOME_DIR}/{layer_name}.json"
        if not os.path.exists(biome_path):
            write_json(biome_path, biome)
            report.record("biomes", biome_path, "ecological")
            lang_entries[f"biome.ergenverse.{layer_name}"] = layer_name.replace("_", " ").title()

    return lang_entries


# ============================================================
# PASS 6: LANG FILE UPDATE
# ============================================================
def update_lang_file(new_entries, report):
    """Merge new lang entries into en_us.json."""
    with open(LANG_FILE) as f:
        lang = json.load(f)

    added = 0
    for key, value in new_entries.items():
        if key not in lang:
            lang[key] = value
            added += 1

    # Sort: item.*, biome.*, dimension.*, structure.*
    def sort_key(kv):
        k = kv[0]
        if k.startswith("itemGroup."): return (0, k)
        if k.startswith("item."): return (1, k)
        if k.startswith("biome."): return (2, k)
        if k.startswith("dimension."): return (3, k)
        if k.startswith("structure."): return (4, k)
        return (5, k)

    sorted_lang = dict(sorted(lang.items(), key=sort_key))
    with open(LANG_FILE, "w") as f:
        json.dump(sorted_lang, f, indent=2, ensure_ascii=False)
        f.write("\n")

    report.created["lang_entries"] = added
    return added


# ============================================================
# MAIN
# ============================================================
def main():
    print("=" * 60)
    print("Canon Worldgen Adapter — Layer 3 Generation")
    print("=" * 60)

    canon = load_canon()
    report = GenerationReport()
    all_lang = {}

    print("\n--- Pass 1: Sub-Region Biomes ---")
    lang = generate_sub_region_biomes(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.created['biomes']} biome JSONs")

    print("\n--- Pass 2: Structure Decomposition ---")
    before_struct = report.created["structures"]
    lang = generate_structure_components(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.created['structures'] - before_struct} structure JSONs")
    print(f"  Generated {report.created['structure_sets'] - before_struct} structure_set JSONs")
    print(f"  Generated {report.created['template_pools'] - before_struct} template_pool JSONs")

    print("\n--- Pass 3: Spirit Vein Systems ---")
    before_cf = report.created["configured_features"]
    lang = generate_spirit_vein_systems(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.created['configured_features'] - before_cf} configured_feature JSONs")
    print(f"  Generated {report.created['placed_features'] - before_cf} placed_feature JSONs")

    print("\n--- Pass 4: Herb Ecological Variants ---")
    before_cf = report.created["configured_features"]
    lang = generate_herb_variants(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.created['configured_features'] - before_cf} configured_feature JSONs")
    print(f"  Generated {report.created['placed_features'] - before_cf} placed_feature JSONs")

    print("\n--- Pass 5: Ocean Layers ---")
    before_biome = report.created["biomes"]
    lang = generate_ocean_layers(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.created['biomes'] - before_biome} ocean biome JSONs")

    print("\n--- Pass 6: Lang File Update ---")
    added = update_lang_file(all_lang, report)
    print(f"  Added {added} new lang entries")

    print("\n" + "=" * 60)
    print(report.summary())
    print("=" * 60)

    # ── POST-GENERATION VALIDATION: scan all output ──
    print("\n--- Post-Generation Validation Sweep ---")
    validate_existing_files(report)
    print("  All existing files pass schema validation.")

    # Write provenance report
    prov_path = f"{FORGE}/scripts/_adapter_provenance.json"
    with open(prov_path, "w") as f:
        json.dump(report.provenance, f, indent=2)
    print(f"\nProvenance report: {prov_path}")


def validate_existing_files(report):
    """Scan all existing worldgen JSONs and validate them.
    Catches bugs from prior runs where no validation existed."""
    import glob

    errors_found = 0
    files_checked = 0

    # Validate all biomes
    for filepath in sorted(glob.glob(f"{BIOME_DIR}/*.json")):
        files_checked += 1
        try:
            with open(filepath) as f:
                biome = json.load(f)
            name = os.path.basename(filepath)[:-5]
            validate_biome(biome, name)
        except Exception as e:
            errors_found += 1
            print(f"  ERROR: {filepath}: {e}")

    # Validate all structures
    for filepath in sorted(glob.glob(f"{STRUCT_DIR}/*.json")):
        files_checked += 1
        try:
            with open(filepath) as f:
                struct = json.load(f)
            name = os.path.basename(filepath)[:-5]
            validate_structure(struct, name)
        except Exception as e:
            errors_found += 1
            print(f"  ERROR: {filepath}: {e}")

    # Validate all structure_sets
    for filepath in sorted(glob.glob(f"{SS_DIR}/*.json")):
        files_checked += 1
        try:
            with open(filepath) as f:
                ss = json.load(f)
            name = os.path.basename(filepath)[:-5]
            validate_structure_set(ss, name)
        except Exception as e:
            errors_found += 1
            print(f"  ERROR: {filepath}: {e}")

    # Validate all template_pools
    for filepath in sorted(glob.glob(f"{TP_DIR}/*/start_pool.json")):
        files_checked += 1
        try:
            with open(filepath) as f:
                tp = json.load(f)
            name = os.path.basename(os.path.dirname(filepath))
            validate_template_pool(tp, name)
        except Exception as e:
            errors_found += 1
            print(f"  ERROR: {filepath}: {e}")

    # Validate all dimension_types (catches the monster_spawn_light_level bug)
    for filepath in sorted(glob.glob(f"{DT_DIR}/*.json")):
        files_checked += 1
        try:
            with open(filepath) as f:
                dt = json.load(f)
            name = os.path.basename(filepath)[:-5]
            validate_dimension_type(dt, name)
        except Exception as e:
            errors_found += 1
            print(f"  ERROR: {filepath}: {e}")

    print(f"  Checked {files_checked} files, found {errors_found} errors.")
    if errors_found > 0:
        raise RuntimeError(
            f"{errors_found} files failed validation. Fix the generator, "
            f"not the files. The generator must produce valid MC 1.20.1 JSON."
        )

if __name__ == "__main__":
    main()
