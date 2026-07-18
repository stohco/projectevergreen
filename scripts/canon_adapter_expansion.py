#!/usr/bin/env python3
"""
Canon Worldgen Adapter — Expansion Passes 7-10.

Extends the core adapter (canon_worldgen_adapter.py) with:
  Pass 7: Sky Realm dimension + sky-layer biomes
  Pass 8: Underground civilization biomes
  Pass 9: Spirit beast species definitions (data/ergenverse/species/)
  Pass 10: Loot provenance metadata for all artifacts (data/ergenverse/provenance/)

Architecture: Canon DB → [Core Adapter + THIS Expansion] → Worldgen + Data JSONs → Forge
"""

import json
import os
import hashlib

FORGE = "/home/z/my-project/forge-mod"
CANON_DB = f"{FORGE}/ri_canon_database.json"
DATA = f"{FORGE}/src/main/resources/data/ergenverse"
BIOME_DIR = f"{DATA}/worldgen/biome"
CF_DIR = f"{DATA}/worldgen/configured_feature"
PF_DIR = f"{DATA}/worldgen/placed_feature"
DIM_DIR = f"{DATA}/dimension"
DT_DIR = f"{DATA}/dimension_type"
LANG_FILE = f"{FORGE}/src/main/resources/assets/ergenverse/lang/en_us.json"

# New data directories for non-worldgen content
SPECIES_DIR = f"{DATA}/species"
PROVENANCE_DIR = f"{DATA}/provenance"
ECOLOGY_DIR = f"{DATA}/ecology"


def load_canon():
    with open(CANON_DB) as f:
        return json.load(f)

def write_json(filepath, data):
    os.makedirs(os.path.dirname(filepath), exist_ok=True)
    with open(filepath, "w") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

def make_11_step_features(step_map):
    steps = [[] for _ in range(11)]
    for idx, feats in step_map.items():
        if isinstance(feats, list):
            steps[idx] = feats
        else:
            steps[idx] = [feats]
    return steps

def salt_for(name, seed=0):
    h = hashlib.md5(f"{name}{seed}".encode()).hexdigest()
    return int(h[:8], 16) % 100000000


# ============================================================
# PASS 7: SKY REALM DIMENSION + SKY-LAYER BIOMES
# ============================================================
# Canon: The sky above cultivation worlds is not empty.
# Cloud beasts, flying sects, sword cultivators, spirit cranes, thunder birds,
# floating mountains, cloud whales, flying islands, ancient sky battlefields,
# meteor streams, spatial cracks.

SKY_BIOMES = {
    "cloud_sea_low": {
        "comment": "Low Cloud Sea — the layer of clouds just above normal terrain. Spirit cranes nest here. Sword cultivators practice flying sword arts through the clouds. Prime Directive: the cloud layer is an objective atmospheric phenomenon.",
        "temp": 0.3, "downfall": 0.8, "precip": True,
        "sky": 16777215, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 15, 1, 2), ("minecraft:bat", 20, 4, 8)],
        "creatures": [("minecraft:parrot", 10, 2, 4), ("minecraft:allay", 5, 1, 2)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_grass_normal"]},
    },
    "floating_island_low": {
        "comment": "Low Floating Islands — small floating landmasses at mid-altitude. Ancient cultivators carved these from mountains using restrictions. Home to minor spirit beasts and hermit cultivators. Prime Directive: the islands were lifted by ancient array formations — they are objective geological features held aloft by active restrictions.",
        "temp": 0.4, "downfall": 0.5, "precip": True,
        "sky": 12694940, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 20, 1, 3), ("minecraft:vex", 10, 1, 2)],
        "creatures": [("minecraft:parrot", 8, 2, 4), ("minecraft:horse", 5, 2, 4)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_coal_upper", "minecraft:ore_iron_upper", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: ["minecraft:patch_taiga_grass", "minecraft:forest_flowers"]},
    },
    "floating_island_high": {
        "comment": "High Floating Islands — large floating continents at extreme altitude. Ancient sect headquarters that ascended. Dense spirit energy. Only accessible to Foundation Establishment+ cultivators who can fly. Prime Directive: the islands exist because ancient arrays still function — the arrays are objective technology.",
        "temp": 0.2, "downfall": 0.6, "precip": True,
        "sky": 7907327, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 10, 1, 2), ("minecraft:vex", 15, 1, 3)],
        "creatures": [("minecraft:allay", 10, 1, 3), ("minecraft:parrot", 5, 2, 3)],
        "carvers": ["minecraft:cave", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_gold_upper", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: ["minecraft:patch_taiga_grass", "minecraft:forest_flowers", "minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]},
    },
    "sword_cultivator_airspace": {
        "comment": "Sword Cultivator Airspace — a sky region frequently patrolled by flying sword cultivators. Residual sword Qi saturates the air. Spirit cranes avoid this region. Prime Directive: the sword Qi is objective residual energy from centuries of cultivator traffic.",
        "temp": 0.35, "downfall": 0.3, "precip": False,
        "sky": 8947848, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:vex", 25, 1, 3), ("minecraft:phantom", 10, 1, 2)],
        "creatures": [],
        "carvers": ["minecraft:cave"],
        "features": {6: ["ergenverse:spirit_vein_quartz_ore"], 9: []},
    },
    "cloud_whale_territory": {
        "comment": "Cloud Whale Territory — migration route of the legendary Cloud Whales. These massive spirit beasts swim through the cloud layer. Their passage leaves spirit energy condensation trails. Prime Directive: cloud whales are objective ecological fauna — their migration routes are as real as any terrestrial animal's.",
        "temp": 0.25, "downfall": 0.7, "precip": True,
        "sky": 16777215, "fog": 16777215, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 5, 1, 1)],
        "creatures": [("minecraft:allay", 15, 1, 3), ("minecraft:parrot", 5, 1, 2)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_grass_normal"]},
    },
    "thunder_bird_nesting": {
        "comment": "Thunder Bird Nesting Peaks — high mountain peaks where thunder birds nest. The air is perpetually charged. Lightning strikes are constant. Thunder essence can be harvested here. Prime Directive: the thunder birds and their habitat are objective ecology.",
        "temp": 0.15, "downfall": 0.4, "precip": True,
        "sky": 3355443, "fog": 5916164, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 15, 1, 3), ("minecraft:vex", 20, 1, 2)],
        "creatures": [("minecraft:parrot", 8, 1, 3)],
        "carvers": ["minecraft:cave", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_gold_upper", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_taiga_grass"]},
    },
    "ancient_sky_battlefield": {
        "comment": "Ancient Sky Battlefield — ruins of a floating fortress from an ancient war. Shattered platforms drift through the clouds. Residual killing intent saturates the air. Prime Directive: the battlefield is an objective historical site — the war happened, the fortress was destroyed, the ruins drift.",
        "temp": 0.3, "downfall": 0.1, "precip": False,
        "sky": 4210752, "fog": 4210752, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:skeleton", 30, 1, 3), ("minecraft:wither_skeleton", 20, 1, 2), ("minecraft:vex", 15, 1, 2), ("minecraft:phantom", 10, 1, 2)],
        "creatures": [],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_gold_upper", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_dead_bush"]},
    },
    "meteor_stream_zone": {
        "comment": "Meteor Stream Zone — a sky region where meteor streams pass through periodically. Meteorites carry rare ores from other star systems. Cultivators harvest meteoric iron here. Prime Directive: the meteor stream is an objective astronomical phenomenon.",
        "temp": 0.1, "downfall": 0.0, "precip": False,
        "sky": 2039583, "fog": 2039583, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:enderman", 20, 1, 2), ("minecraft:phantom", 10, 1, 1)],
        "creatures": [],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_gold_upper", "minecraft:ore_diamond", "minecraft:ore_debris_small", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_dead_bush"]},
    },
    "spatial_crack_region": {
        "comment": "Spatial Crack Region — a sky zone where space itself is fractured. Spatial tears drift through the air. Cultivators harvest spatial fragments here, but the tears are deadly. Prime Directive: the spatial fractures are objective damage to the fabric of space — caused by ancient spatial-tier battles.",
        "temp": 0.2, "downfall": 0.0, "precip": False,
        "sky": 13107, "fog": 13107, "water": 13107, "water_fog": 256,
        "monsters": [("minecraft:enderman", 40, 1, 3), ("minecraft:vex", 20, 1, 2), ("minecraft:phantom", 15, 1, 2)],
        "creatures": [],
        "carvers": ["minecraft:cave", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_debris_small", "minecraft:ore_debris_large", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_dead_bush"]},
    },
    "celestial_observation_peak": {
        "comment": "Celestial Observation Peak — the highest points in the world, above all clouds. Cultivators come here to observe the stars and comprehend celestial Dao. Spirit energy is thin but pure. Prime Directive: the peaks are objective geography — their height makes them suitable for observation.",
        "temp": -0.3, "downfall": 0.3, "precip": True,
        "sky": 7907327, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:stray", 15, 1, 2), ("minecraft:skeleton", 10, 1, 1)],
        "creatures": [("minecraft:goat", 10, 2, 4)],
        "carvers": ["minecraft:cave", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_gold_upper", "minecraft:ore_diamond", "minecraft:ore_emerald", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: ["minecraft:patch_taiga_grass", "minecraft:patch_snow"]},
    },
    "spirit_crane_migration": {
        "comment": "Spirit Crane Migration Route — a sky corridor used by spirit cranes during seasonal migration. The cranes leave spirit energy trails that condense into rare cloud-deposits. Prime Directive: the migration route is objective ecological behavior.",
        "temp": 0.4, "downfall": 0.5, "precip": True,
        "sky": 16773915, "fog": 16777215, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:phantom", 5, 1, 1)],
        "creatures": [("minecraft:parrot", 25, 2, 5), ("minecraft:allay", 8, 1, 2)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_grass_normal"]},
    },
    "flying_sect_airspace": {
        "comment": "Flying Sect Airspace — territory claimed by a flying cultivation sect. The sect's floating headquarters drifts through this region. No-fly zone for unauthorized cultivators. Prime Directive: the sect's territorial claim is enforced by their array network — an objective defense system.",
        "temp": 0.3, "downfall": 0.4, "precip": True,
        "sky": 7907327, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:vex", 15, 1, 2), ("minecraft:witch", 10, 1, 1)],
        "creatures": [("minecraft:allay", 10, 1, 3), ("minecraft:horse", 5, 2, 3)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_gold_upper", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: ["minecraft:patch_taiga_grass", "minecraft:forest_flowers"]},
    },
}


def generate_sky_biomes(canon, report):
    """Pass 7: Generate sky-layer biomes + sky realm dimension."""
    lang_entries = {}

    for name, data in SKY_BIOMES.items():
        biome = {
            "_comment": data["comment"],
            "temperature": data["temp"],
            "downfall": data["downfall"],
            "has_precipitation": data["precip"],
            "effects": {
                "sky_color": data["sky"],
                "fog_color": data["fog"],
                "water_color": data["water"],
                "water_fog_color": data["water_fog"],
                "grass_color_modifier": "none",
            },
            "spawners": {
                "ambient": [{"type": "minecraft:bat", "weight": 10, "minCount": 8, "maxCount": 8}] if data["downfall"] > 0.1 else [],
                "axolotls": [],
                "creature": [{"type": c[0], "weight": c[1], "minCount": c[2], "maxCount": c[3]} for c in data.get("creatures", [])],
                "monster": [{"type": m[0], "weight": m[1], "minCount": m[2], "maxCount": m[3]} for m in data.get("monsters", [])],
                "underground_water_creature": [],
                "water_ambient": [],
                "water_creature": [],
            },
            "spawn_costs": {},
            "carvers": {"air": data.get("carvers", ["minecraft:cave"]), "liquid": []},
            "features": make_11_step_features(data.get("features", {})),
        }

        biome_path = f"{BIOME_DIR}/{name}.json"
        if not os.path.exists(biome_path):
            write_json(biome_path, biome)
            report["biomes"] = report.get("biomes", 0) + 1
            lang_entries[f"biome.ergenverse.{name}"] = name.replace("_", " ").title()

    # Sky Realm dimension type
    sky_dt = {
        "_comment": "Sky Realm — the aerial dimension above cultivation worlds. Contains floating islands, cloud beasts, flying sects, and ancient sky battlefields. Accessed via flying sword, teleportation array, or spatial rift. Prime Directive: the sky realm exists objectively — it is not a perception effect.",
        "ultrawarm": False,
        "natural": False,
        "piglin_safe": False,
        "respawn_anchor_works": False,
        "bed_works": True,
        "has_raids": False,
        "has_skylight": True,
        "has_ceiling": False,
        "coordinate_scale": 1.0,
        "ambient_light": 0.5,
        "logical_height": 384,
        "min_y": -64,
        "height": 448,
        "infiniburn": "#minecraft:infiniburn_overworld",
        "effects": "minecraft:overworld",
    }
    dt_path = f"{DT_DIR}/sky_realm_type.json"
    if not os.path.exists(dt_path):
        write_json(dt_path, sky_dt)
        report["dimension_types"] = report.get("dimension_types", 0) + 1
        lang_entries["dimension.ergenverse.sky_realm"] = "Sky Realm"

    # Sky Realm dimension — uses end noise settings for floating islands
    sky_dim = {
        "_comment": "Sky Realm dimension — floating islands, cloud layer, sky ecology. Uses end noise settings for floating-island terrain generation. 12 sky biomes mapped by temperature/depth axes.",
        "type": "ergenverse:sky_realm_type",
        "generator": {
            "type": "minecraft:noise",
            "settings": "minecraft:end",
            "biome_source": {
                "type": "minecraft:multi_noise",
                "biomes": [
                    {"biome": "ergenverse:cloud_sea_low", "parameters": {"temperature": [0.2, 0.5], "humidity": [0.5, 1.0], "continentalness": [-1.0, 0.0], "erosion": [-1.0, 1.0], "weirdness": [-1.0, 1.0], "depth": [-1.0, 0.0], "offset": 0.0}},
                    {"biome": "ergenverse:floating_island_low", "parameters": {"temperature": [0.3, 0.6], "humidity": [0.3, 0.7], "continentalness": [0.0, 0.5], "erosion": [-1.0, 1.0], "weirdness": [-1.0, 1.0], "depth": [0.0, 0.5], "offset": 0.0}},
                    {"biome": "ergenverse:floating_island_high", "parameters": {"temperature": [0.1, 0.4], "humidity": [0.4, 0.8], "continentalness": [0.5, 1.0], "erosion": [-1.0, 1.0], "weirdness": [-1.0, 1.0], "depth": [0.5, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:sword_cultivator_airspace", "parameters": {"temperature": [0.2, 0.5], "humidity": [0.2, 0.5], "continentalness": [-0.5, 0.3], "erosion": [-1.0, 1.0], "weirdness": [0.3, 1.0], "depth": [-1.0, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:cloud_whale_territory", "parameters": {"temperature": [0.1, 0.4], "humidity": [0.5, 1.0], "continentalness": [-1.0, -0.3], "erosion": [-1.0, 1.0], "weirdness": [-1.0, 0.0], "depth": [0.0, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:thunder_bird_nesting", "parameters": {"temperature": [-0.2, 0.2], "humidity": [0.3, 0.6], "continentalness": [0.3, 1.0], "erosion": [-1.0, 0.0], "weirdness": [0.0, 1.0], "depth": [0.5, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:ancient_sky_battlefield", "parameters": {"temperature": [0.2, 0.5], "humidity": [-1.0, 0.0], "continentalness": [0.0, 0.6], "erosion": [0.5, 1.0], "weirdness": [0.5, 1.0], "depth": [0.0, 0.7], "offset": 0.0}},
                    {"biome": "ergenverse:meteor_stream_zone", "parameters": {"temperature": [-0.3, 0.1], "humidity": [-1.0, -0.3], "continentalness": [-0.5, 0.5], "erosion": [-1.0, 1.0], "weirdness": [-1.0, -0.3], "depth": [0.3, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:spatial_crack_region", "parameters": {"temperature": [0.0, 0.3], "humidity": [-1.0, 0.0], "continentalness": [-0.3, 0.3], "erosion": [-1.0, 1.0], "weirdness": [0.7, 1.0], "depth": [-1.0, 0.0], "offset": 0.0}},
                    {"biome": "ergenverse:celestial_observation_peak", "parameters": {"temperature": [-0.6, -0.2], "humidity": [0.2, 0.5], "continentalness": [0.5, 1.0], "erosion": [-1.0, 0.0], "weirdness": [-1.0, 0.0], "depth": [0.7, 1.0], "offset": 0.0}},
                    {"biome": "ergenverse:spirit_crane_migration", "parameters": {"temperature": [0.3, 0.6], "humidity": [0.4, 0.8], "continentalness": [-0.5, 0.0], "erosion": [0.0, 1.0], "weirdness": [-0.3, 0.3], "depth": [0.0, 0.5], "offset": 0.0}},
                    {"biome": "ergenverse:flying_sect_airspace", "parameters": {"temperature": [0.2, 0.5], "humidity": [0.3, 0.6], "continentalness": [0.2, 0.7], "erosion": [-0.5, 0.5], "weirdness": [-0.3, 0.3], "depth": [0.3, 0.8], "offset": 0.0}},
                ]
            }
        }
    }
    dim_path = f"{DIM_DIR}/sky_realm.json"
    if not os.path.exists(dim_path):
        write_json(dim_path, sky_dim)
        report["dimensions"] = report.get("dimensions", 0) + 1

    return lang_entries


# ============================================================
# PASS 8: UNDERGROUND CIVILIZATION BIOMES
# ============================================================

UNDERGROUND_BIOMES = {
    "spirit_mine_shaft": {
        "comment": "Spirit Mine Shaft — abandoned mining tunnels from ancient cultivation eras. Spirit veins were extracted here, leaving residual Qi deposits. Supports are crumbling. Prime Directive: the mines are objective historical infrastructure.",
        "temp": 0.2, "downfall": 0.0, "precip": False,
        "sky": 4210752, "fog": 4210752, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:zombie", 30, 2, 4), ("minecraft:skeleton", 25, 1, 2), ("minecraft:cave_spider", 20, 1, 3), ("minecraft:creeper", 15, 1, 1)],
        "creatures": [("minecraft:bat", 20, 4, 8)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_coal_upper", "minecraft:ore_coal_lower", "minecraft:ore_iron_upper", "minecraft:ore_iron_middle", "minecraft:ore_iron_small", "minecraft:ore_gold_upper", "minecraft:ore_gold_lower", "minecraft:ore_copper", "minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: ["minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]},
    },
    "crystal_forest_cavern": {
        "comment": "Crystal Forest Cavern — vast underground cavern filled with glowing crystal formations. The crystals are fossilized spirit energy from ancient eras. Light sources are the crystals themselves. Prime Directive: the crystal forest is a natural geological formation infused with ancient Qi.",
        "temp": 0.3, "downfall": 0.2, "precip": False,
        "sky": 16777215, "fog": 13421772, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:skeleton", 20, 1, 2), ("minecraft:creeper", 15, 1, 1), ("minecraft:witch", 10, 1, 1)],
        "creatures": [("minecraft:bat", 15, 4, 8), ("minecraft:glow_squid", 10, 2, 4)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground"],
        "features": {6: ["minecraft:ore_diamond", "minecraft:ore_emerald", "minecraft:ore_lapis", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:amethyst_geode"], 9: ["minecraft:glow_lichen"]},
    },
    "ancient_ruin_chamber": {
        "comment": "Ancient Ruin Chamber — forgotten cultivation chambers from before the Sealed Realm was sealed. Contains broken formations and decayed treasure. Prime Directive: the ruins are objective historical sites — they were built by cultivators who lived before the current era.",
        "temp": 0.15, "downfall": 0.0, "precip": False,
        "sky": 4210752, "fog": 4210752, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:skeleton", 35, 1, 3), ("minecraft:zombie", 25, 2, 4), ("minecraft:creeper", 15, 1, 1), ("minecraft:witch", 10, 1, 1)],
        "creatures": [("minecraft:bat", 20, 4, 8)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground"],
        "features": {6: ["minecraft:ore_iron_upper", "minecraft:ore_iron_middle", "minecraft:ore_gold_lower", "minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks", "minecraft:fossil_underground"], 9: ["minecraft:brown_mushroom_normal", "minecraft:red_mushroom_normal"]},
    },
    "forgotten_formation_hall": {
        "comment": "Forgotten Formation Hall — underground chamber with an ancient formation array still active after millennia. The formation emits residual energy. Cultivators study it to learn ancient formation arts. Prime Directive: the formation is objective technology — it was built by ancient cultivators and still functions.",
        "temp": 0.25, "downfall": 0.0, "precip": False,
        "sky": 5916164, "fog": 5916164, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:vex", 25, 1, 3), ("minecraft:skeleton", 20, 1, 2), ("minecraft:witch", 15, 1, 1)],
        "creatures": [("minecraft:bat", 15, 4, 8)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:forest_rocks"], 9: []},
    },
    "dead_beast_crypt": {
        "comment": "Dead Beast Crypt — underground crypt containing the skeleton of a massive spirit beast that died here millennia ago. The bones still radiate residual beast Qi. Scavenger beasts feed on the remaining energy. Prime Directive: the skeleton is objective biological remains — the beast lived and died here.",
        "temp": 0.1, "downfall": 0.0, "precip": False,
        "sky": 4210752, "fog": 4210752, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:skeleton", 30, 1, 3), ("minecraft:cave_spider", 20, 1, 3), ("minecraft:zombie", 15, 2, 3)],
        "creatures": [("minecraft:bat", 20, 4, 8)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground"],
        "features": {6: ["minecraft:ore_iron_middle", "minecraft:ore_gold_lower", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 7: ["minecraft:fossil_underground", "minecraft:bone_block"], 9: ["minecraft:brown_mushroom_normal"]},
    },
    "sealed_demon_prison": {
        "comment": "Sealed Demon Prison — underground chamber where a powerful demon was sealed by ancient cultivators. The seals are weakening. Demonic energy leaks through. Prime Directive: the demon and its prison are objective — the sealing happened, the demon exists, the seals are physical formations.",
        "temp": 0.8, "downfall": 0.0, "precip": False,
        "sky": 1310720, "fog": 1310720, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:wither_skeleton", 30, 1, 2), ("minecraft:blaze", 20, 1, 2), ("minecraft:zombie", 20, 2, 4), ("minecraft:witch", 15, 1, 1)],
        "creatures": [],
        "carvers": ["minecraft:cave", "minecraft:nether_cave"],
        "features": {6: ["minecraft:ore_quartz", "minecraft:ore_gold_extra", "minecraft:ore_magma", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_fire"]},
    },
    "underground_spirit_river": {
        "comment": "Underground Spirit River — a subterranean river infused with spirit energy. The water glows faintly. Spirit fish live here. Cultivators harvest the water for alchemy. Prime Directive: the river is an objective geological feature — its spirit energy comes from the spirit vein it flows through.",
        "temp": 0.35, "downfall": 0.5, "precip": False,
        "sky": 4159204, "fog": 4159204, "water": 9961472, "water_fog": 329011,
        "monsters": [("minecraft:drowned", 25, 1, 2), ("minecraft:skeleton", 15, 1, 1)],
        "creatures": [("minecraft:bat", 15, 4, 8), ("minecraft:glow_squid", 20, 2, 4), ("minecraft:axolotl", 10, 1, 2)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground"],
        "features": {6: ["minecraft:ore_iron_middle", "minecraft:ore_gold_lower", "minecraft:ore_copper", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_seagrass", "minecraft:glow_lichen"]},
    },
    "elemental_fire_cavern": {
        "comment": "Elemental Fire Cavern — underground cavern saturated with fire-aspect Qi. Magma pools and fire crystals abound. Fire-aspect spirit beasts nest here. Prime Directive: the fire saturation is an objective geological phenomenon — the cavern sits above a fire spirit vein.",
        "temp": 1.0, "downfall": 0.0, "precip": False,
        "sky": 16711680, "fog": 16711680, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:blaze", 30, 1, 2), ("minecraft:magma_cube", 25, 1, 3), ("minecraft:wither_skeleton", 15, 1, 1)],
        "creatures": [],
        "carvers": ["minecraft:nether_cave"],
        "features": {6: ["minecraft:ore_magma", "minecraft:ore_quartz", "minecraft:ore_gold_extra", "ergenverse:spirit_vein_quartz_ore", "ergenverse:spirit_vein_fire"], 9: ["minecraft:patch_fire"]},
    },
    "elemental_ice_cavern": {
        "comment": "Elemental Ice Cavern — underground cavern saturated with ice-aspect Qi. Perpetually frozen. Ice crystals contain frozen spirit energy. Ice-aspect spirit beasts hibernate here. Prime Directive: the ice saturation is objective — the cavern sits above an ice spirit vein.",
        "temp": -0.8, "downfall": 0.5, "precip": False,
        "sky": 16777215, "fog": 16777215, "water": 4159204, "water_fog": 329011,
        "monsters": [("minecraft:stray", 30, 1, 2), ("minecraft:skeleton", 15, 1, 1)],
        "creatures": [("minecraft:bat", 5, 4, 8)],
        "carvers": ["minecraft:cave"],
        "features": {6: ["minecraft:ore_iron_middle", "minecraft:ore_gold_lower", "minecraft:ore_diamond", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_ice", "minecraft:patch_blue_ice"]},
    },
    "ancient_god_bone_chamber": {
        "comment": "Ancient God Bone Chamber — underground chamber containing bone fragments of an Ancient God. The bones are mountain-sized. Residual divine power saturates the chamber. Prime Directive: the bones are objective remains — an Ancient God died here in the primordial era.",
        "temp": 0.4, "downfall": 0.0, "precip": False,
        "sky": 1118481, "fog": 1118481, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:wither_skeleton", 35, 1, 3), ("minecraft:skeleton", 25, 1, 2), ("minecraft:enderman", 15, 1, 2)],
        "creatures": [],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground"],
        "features": {6: ["minecraft:ore_diamond", "minecraft:ore_debris_small", "minecraft:ore_debris_large", "ergenverse:spirit_vein_quartz_ore", "ergenverse:spirit_vein_ancient"], 7: ["minecraft:fossil_underground", "minecraft:bone_block"], 9: []},
    },
    "ore_kingdom_deep": {
        "comment": "Ore Kingdom — deep underground civilization of ore-dwelling creatures. Vast chambers filled with every ore type. The creatures here cultivate by absorbing mineral Qi. Prime Directive: the ore kingdom is an objective civilization — its inhabitants evolved to live in mineral-rich deep strata.",
        "temp": 0.3, "downfall": 0.0, "precip": False,
        "sky": 4210752, "fog": 4210752, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:skeleton", 20, 1, 2), ("minecraft:zombie", 15, 2, 3), ("minecraft:creeper", 10, 1, 1)],
        "creatures": [("minecraft:bat", 15, 4, 8)],
        "carvers": ["minecraft:cave", "minecraft:cave_extra_underground", "minecraft:canyon"],
        "features": {6: ["minecraft:ore_dirt", "minecraft:ore_gravel", "minecraft:ore_granite_upper", "minecraft:ore_granite_lower", "minecraft:ore_diorite_upper", "minecraft:ore_diorite_lower", "minecraft:ore_andesite_upper", "minecraft:ore_andesite_lower", "minecraft:ore_coal_upper", "minecraft:ore_coal_lower", "minecraft:ore_iron_upper", "minecraft:ore_iron_middle", "minecraft:ore_iron_small", "minecraft:ore_gold_upper", "minecraft:ore_gold_lower", "minecraft:ore_copper", "minecraft:ore_redstone", "minecraft:ore_lapis", "minecraft:ore_diamond", "minecraft:ore_emerald", "ergenverse:spirit_vein_quartz_ore", "ergenverse:spirit_vein_metal"]},
    },
    "nether_mining_colony": {
        "comment": "Nether Mining Colony — a deep cultivation outpost established to mine nether-tier materials. The colonists have gone feral after millennia of isolation. Prime Directive: the colony is objective history — it was established, then abandoned, and the inhabitants devolved.",
        "temp": 0.7, "downfall": 0.0, "precip": False,
        "sky": 2039583, "fog": 2039583, "water": 3145784, "water_fog": 270131,
        "monsters": [("minecraft:wither_skeleton", 25, 1, 2), ("minecraft:blaze", 20, 1, 2), ("minecraft:zombie", 20, 2, 4), ("minecraft:skeleton", 15, 1, 2)],
        "creatures": [],
        "carvers": ["minecraft:cave", "minecraft:nether_cave"],
        "features": {6: ["minecraft:ore_quartz", "minecraft:ore_gold_extra", "minecraft:ore_magma", "minecraft:ore_debris_small", "minecraft:ore_debris_large", "ergenverse:spirit_vein_quartz_ore"], 9: ["minecraft:patch_fire"]},
    },
}


def generate_underground_biomes(canon, report):
    """Pass 8: Generate underground civilization biomes."""
    lang_entries = {}

    for name, data in UNDERGROUND_BIOMES.items():
        biome = {
            "_comment": data["comment"],
            "temperature": data["temp"],
            "downfall": data["downfall"],
            "has_precipitation": data["precip"],
            "effects": {
                "sky_color": data["sky"],
                "fog_color": data["fog"],
                "water_color": data["water"],
                "water_fog_color": data["water_fog"],
                "grass_color_modifier": "none",
            },
            "spawners": {
                "ambient": [{"type": "minecraft:bat", "weight": 10, "minCount": 8, "maxCount": 8}],
                "axolotls": [],
                "creature": [{"type": c[0], "weight": c[1], "minCount": c[2], "maxCount": c[3]} for c in data.get("creatures", [])],
                "monster": [{"type": m[0], "weight": m[1], "minCount": m[2], "maxCount": m[3]} for m in data.get("monsters", [])],
                "underground_water_creature": [{"type": "minecraft:glow_squid", "weight": 10, "minCount": 3, "maxCount": 5}] if "glow_squid" in str(data.get("creatures", [])) else [],
                "water_ambient": [],
                "water_creature": [],
            },
            "spawn_costs": {},
            "carvers": {"air": data.get("carvers", ["minecraft:cave"]), "liquid": []},
            "features": make_11_step_features(data.get("features", {})),
        }

        biome_path = f"{BIOME_DIR}/{name}.json"
        if not os.path.exists(biome_path):
            write_json(biome_path, biome)
            report["biomes"] = report.get("biomes", 0) + 1
            lang_entries[f"biome.ergenverse.{name}"] = name.replace("_", " ").title()

    return lang_entries


# ============================================================
# PASS 9: SPIRIT BEAST SPECIES DEFINITIONS
# ============================================================
# Custom data files in data/ergenverse/species/ — read by the simulation engine.
# Every species has: taxonomy, temperament, territory, diet, migration, sleep cycle,
# cultivation, breeding, mutations, leader hierarchy, predators, prey, territorial radius,
# nest generation, treasure preferences, relationship with cultivators, fear response,
# intelligence, elemental affinity, law affinity.

SPECIES_DEFINITIONS = {
    "mosquito_beast": {
        "comment": "Mosquito Beast (蚊子兽) — Wang Lin's iconic spirit beast. Canon: evolved from a swarm of blood-drinking mosquitoes that absorbed spirit beast blood. Can drain Qi from any living thing. The Mosquito Valley ecosystem contains millions of larvae, adult swarms, queens, egg chambers, blood pools, spirit trees, nest tunnels, mutated elites, and ancient queens.",
        "taxonomy": {"kingdom": "spirit_insect", "phylum": "qi_absorber", "class": "blood_drinker", "order": "mosquito", "family": "spirit_mosquito", "genus": "aether_culex", "species": "mosquito_beast"},
        "temperament": "aggressive_swarm",
        "territory": "Mosquito Valley and surrounding blood-soaked regions",
        "territorial_radius": 500,
        "diet": "blood_and_qi",
        "prey": ["spirit_beasts", "cultivators", "mortals", "wild_animals"],
        "predators": ["thunder_birds", "high_tier_cultivators"],
        "migration": "seasonal — follows spirit beast herds",
        "sleep_cycle": "diurnal_hunter — active during day, nests at night",
        "cultivation": {"base_tier": "qi_condensation", "max_tier": "soul_formation", "growth_method": "blood_absorption"},
        "breeding": {"method": "queen_lays_eggs", "egg_count": 10000, "gestation": 30, "maturity_age": 90},
        "mutations": ["winged_elite", "armored_queen", "blood_essence_ancient", "tribulation_survivor"],
        "leader_hierarchy": "queen_dominated — one ancient queen controls the entire swarm",
        "nest_generation": "underground_tunnel_network_with_blood_pools",
        "treasure_preferences": ["blood_essence", "spirit_blood", "corpse_blood"],
        "relationship_with_cultivators": "hostile — will attack any cultivator below Soul Formation; Wang Lin tamed one through Dream Dao",
        "fear_response": "swarm_retreat_to_queen_when_threatened",
        "intelligence": 3,
        "elemental_affinity": "blood",
        "law_affinity": "death",
        "canon_source": "I093 — Mosquito Beast; Wang Lin's mount/companion",
        "ecological_variants": ["larval", "adult_worker", "adult_soldier", "elite", "queen", "ancient_queen"],
    },
    "thunder_toad": {
        "comment": "Thunder Toad (雷蟾) — a thunder-aspect spirit beast. Canon: found in thunder-saturated regions. Its croak summons lightning. Cultivators hunt it for thunder essence.",
        "taxonomy": {"kingdom": "spirit_beast", "phylum": "elemental", "class": "amphibian", "order": "toad", "family": "thunder_toad", "genus": "fulmen_bufo", "species": "thunder_toad"},
        "temperament": "territorial_passive",
        "territory": "thunder-saturated wetlands and mountain peaks",
        "territorial_radius": 200,
        "diet": "thunder_essence_and_insects",
        "prey": ["insects", "small_spirit_beasts", "thunder_essence_particles"],
        "predators": ["thunder_birds", "high_tier_cultivators"],
        "migration": "stationary — tied to thunder spirit veins",
        "sleep_cycle": "nocturnal — absorbs thunder Qi during storms",
        "cultivation": {"base_tier": "foundation_establishment", "max_tier": "nascent_soul", "growth_method": "thunder_absorption"},
        "breeding": {"method": "egg_laying", "egg_count": 50, "gestation": 60, "maturity_age": 180},
        "mutations": ["golden_thunder", "tribulation_survivor", "ancient_thunder_dragon_form"],
        "leader_hierarchy": "solitary — meet only to breed",
        "nest_generation": "underground_burrow_near_thunder_vein",
        "treasure_preferences": ["thunder_essence", "thunder_crystals", "lightning_struck_trees"],
        "relationship_with_cultivators": "neutral — will defend territory but not hunt cultivators",
        "fear_response": "release_thunder_burst_and_flee",
        "intelligence": 5,
        "elemental_affinity": "thunder",
        "law_affinity": "yang",
        "canon_source": "I094 — Thunder Toad",
        "ecological_variants": ["juvenile", "adult", "ancient", "tribulation_survivor"],
    },
    "lei_ji_thunder_beast": {
        "comment": "Lei Ji Thunder Beast (雷击兽) — a massive thunder-aspect beast. Canon: Wang Lin encountered these in the Thunder Celestial Realm. They embody thunder itself.",
        "taxonomy": {"kingdom": "spirit_beast", "phylum": "elemental", "class": "quadruped", "order": "thunder_beast", "family": "lei_ji", "genus": "tonitrus_bestia", "species": "lei_ji"},
        "temperament": "aggressive_solitary",
        "territory": "Thunder Celestial Realm and thunder-saturated dimensions",
        "territorial_radius": 1000,
        "diet": "thunder_essence_and_spirit_beast_flesh",
        "prey": ["thunder_toads", "weaker_spirit_beasts", "unwary_cultivators"],
        "predators": ["none_below_celestial_tier"],
        "migration": "follows_thunder_storms_across_realms",
        "sleep_cycle": "does_not_sleep — perpetually charged with thunder Qi",
        "cultivation": {"base_tier": "nascent_soul", "max_tier": "celestial", "growth_method": "thunder_tribulation_absorption"},
        "breeding": {"method": "thunder_fusion", "egg_count": 1, "gestation": 365, "maturity_age": 1000},
        "mutations": ["tribulation_survivor", "ancient_thunder_dragon", "celestial_thunder_form"],
        "leader_hierarchy": "solitary_apex — no hierarchy, each is a lone apex predator",
        "nest_generation": "thunder_charged_mountain_peak",
        "treasure_preferences": ["thunder_essence", "celestial_thunder_crystals", "tribulation_lightning"],
        "relationship_with_cultivators": "hostile — will attack any cultivator below Soul Transformation",
        "fear_response": "none — fights_to_death",
        "intelligence": 7,
        "elemental_affinity": "thunder",
        "law_affinity": "heavenly_tribulation",
        "canon_source": "I095 — Lei Ji Thunder Beast",
        "ecological_variants": ["juvenile", "adult", "ancient", "celestial"],
    },
    "nether_beast": {
        "comment": "Nether Beast (幽冥兽) — a beast from the nether/boundary between life and death. Canon: feeds on souls. Found in places where the boundary is thin — Jue Ming Valley, Sea of Devils, Immortal Graveyard.",
        "taxonomy": {"kingdom": "spirit_beast", "phylum": "nether", "class": "soul_drainer", "order": "nether", "family": "nether_beast", "genus": "umbra_bestia", "species": "nether_beast"},
        "temperament": "cunning_ambush_predator",
        "territory": "nether_boundary_regions — places where life/death barrier is thin",
        "territorial_radius": 300,
        "diet": "souls_and_spirit_energy",
        "prey": ["mortals", "low_tier_cultivators", "weaker_spirit_beasts"],
        "predators": ["soul_formation_cultivators", "ancient_gods"],
        "migration": "follows_soul_density_gradients",
        "sleep_cycle": "nocturnal — most active when the boundary is weakest (midnight)",
        "cultivation": {"base_tier": "core_formation", "max_tier": "soul_formation", "growth_method": "soul_absorption"},
        "breeding": {"method": "soul_fission", "egg_count": 3, "gestation": 180, "maturity_age": 500},
        "mutations": ["soul_devourer", "nether_lord", "ancient_reaper"],
        "leader_hierarchy": "solitary — territorial against other nether beasts",
        "nest_generation": "crack_in_the_nether_boundary",
        "treasure_preferences": ["soul_essence", "nether_crystals", "death_aura_items"],
        "relationship_with_cultivators": "hostile — hunts cultivators for their souls",
        "fear_response": "phase_into_nether_boundary_and_flee",
        "intelligence": 8,
        "elemental_affinity": "nether",
        "law_affinity": "death",
        "canon_source": "I096 — Nether Beast",
        "ecological_variants": ["juvenile", "adult", "ancient", "nether_lord"],
    },
    "flame_dragon": {
        "comment": "Flame Dragon (火龙) — a fire-aspect dragon-type spirit beast. Canon: rare and powerful. Found in fire-saturated regions. Sought after for their dragon cores.",
        "taxonomy": {"kingdom": "spirit_beast", "phylum": "dragon", "class": "elemental_dragon", "order": "fire_dragon", "family": "flame_dragon", "genus": "ignis_draconus", "species": "flame_dragon"},
        "temperament": "proud_territorial",
        "territory": "volcanic_regions and fire_spirit_vein_areas",
        "territorial_radius": 2000,
        "diet": "fire_essence_and_minerals",
        "prey": ["fire_aspect_beasts", "ore_deposit_creatures"],
        "predators": ["none_below_ancient_god_tier"],
        "migration": "follows_volcanic_activity",
        "sleep_cycle": "decades_long_hibernation_between_active_periods",
        "cultivation": {"base_tier": "nascent_soul", "max_tier": "celestial", "growth_method": "fire_essence_absorption"},
        "breeding": {"method": "egg_laying", "egg_count": 1, "gestation": 1000, "maturity_age": 10000},
        "mutations": ["ancient_flame_dragon", "celestial_flame_dragon", "primordial_fire_dragon"],
        "leader_hierarchy": "solitary_apex — dragons do not form hierarchies",
        "nest_generation": "volcanic_caldera_with_ward_arrays",
        "treasure_preferences": ["fire_essence", "dragon_core_materials", "volcanic_minerals"],
        "relationship_with_cultivators": "neutral — will ignore cultivators who don't trespass; destroy those who do",
        "fear_response": "none — dragon_pride_prevents_flight",
        "intelligence": 12,
        "elemental_affinity": "fire",
        "law_affinity": "yang",
        "canon_source": "I097 — Flame Dragon",
        "ecological_variants": ["juvenile", "adult", "ancient", "primordial"],
    },
    "cloud_whale": {
        "comment": "Cloud Whale (云鲸) — massive sky-dwelling spirit beast. Canon: swims through the cloud layer. Their passage leaves spirit energy condensation trails. Migration routes are stable across millennia.",
        "taxonomy": {"kingdom": "spirit_beast", "phylum": "sky_fauna", "class": "cetacean", "order": "cloud_whale", "family": "aether_cetus", "genus": "nimbus_balaena", "species": "cloud_whale"},
        "temperament": "gentle_giant",
        "territory": "cloud_whale_territory sky biome — migration corridor",
        "territorial_radius": 5000,
        "diet": "cloud_spirit_energy_and_atmospheric_qi",
        "prey": ["none — filter_feeds_on_atmospheric_qi"],
        "predators": ["none — too_large_to_be_predated"],
        "migration": "seasonal — follows cloud spirit density across the sky realm",
        "sleep_cycle": "does_not_sleep — perpetually_swimming",
        "cultivation": {"base_tier": "soul_formation", "max_tier": "celestial", "growth_method": "cloud_qi_absorption"},
        "breeding": {"method": "cloud_birth", "egg_count": 1, "gestation": 100, "maturity_age": 5000},
        "mutations": ["ancient_cloud_whale", "storm_whale", "celestial_sky_whale"],
        "leader_hierarchy": "pod_structure — eldest_female_leads",
        "nest_generation": "cloud_condensation_nest — temporary_structures_in_the_cloud_sea",
        "treasure_preferences": ["cloud_essence", "sky_spirit_crystals", "condensation_dew"],
        "relationship_with_cultivators": "friendly — will allow cultivators to ride them if treated with respect",
        "fear_response": "ascend_to_higher_altitude — never_fights",
        "intelligence": 15,
        "elemental_affinity": "cloud",
        "law_affinity": "freedom",
        "canon_source": "L56 — Cloud Sea Star System fauna",
        "ecological_variants": ["juvenile", "adult", "elder", "ancient"],
    },
    "thunder_celestial_beast": {
        "comment": "Thunder Celestial Beast (雷仙兽) — a celestial-tier thunder beast from the Thunder Celestial Realm. Canon: embodies the thunder law. Only Thunder Celestials can command them.",
        "taxonomy": {"kingdom": "celestial_beast", "phylum": "law_embodiment", "class": "thunder", "order": "celestial", "family": "thunder_celestial", "genus": "celestial_tonitrus", "species": "thunder_celestial_beast"},
        "temperament": "imperious_law_embodiment",
        "territory": "Thunder Celestial Realm",
        "territorial_radius": 10000,
        "diet": "thunder_law_energy",
        "prey": ["any_creature_that_enters_their_realm"],
        "predators": ["none — apex_of_the_thunder_realm"],
        "migration": "does_not_migrate — guards_the_thunder_realm_core",
        "sleep_cycle": "does_not_sleep — perpetually_embodies_thunder",
        "cultivation": {"base_tier": "celestial", "max_tier": "celestial_lord", "growth_method": "thunder_law_comprehension"},
        "breeding": {"method": "law_fission", "egg_count": 1, "gestation": 10000, "maturity_age": 100000},
        "mutations": ["none — already_at_celestial_tier"],
        "leader_hierarchy": "solitary — each_is_a_law_embodiment",
        "nest_generation": "thunder_realm_core",
        "treasure_preferences": ["thunder_law_fragments", "celestial_thunder_crystals"],
        "relationship_with_cultivators": "neutral_to_thunder_cultivators — hostile_to_all_others",
        "fear_response": "none — embodies_eternal_thunder",
        "intelligence": 20,
        "elemental_affinity": "celestial_thunder",
        "law_affinity": "thunder_law",
        "canon_source": "I098 — Thunder Celestial Beast",
        "ecological_variants": ["celestial", "ancient_celestial"],
    },
    "mosquito_valley_ecosystem": {
        "comment": "Mosquito Valley Ecosystem — a complete ecological system centered on the Mosquito Beast species. Canon: Wang Lin's Mosquito Beast came from here. The valley contains millions of larvae, adult swarms, queens, egg chambers, blood pools, spirit trees, nest tunnels, mutated elites, and ancient queens. This is not a single mob — it's an entire ecosystem.",
        "ecosystem_type": "swarm_predator_ecology",
        "keystone_species": "mosquito_beast",
        "trophic_layers": [
            {"layer": "producer", "species": ["blood_pool_spirit_algae", "corpse_fungus"], "description": "Spirit algae that grows in blood pools; fungus that breaks down corpses"},
            {"layer": "primary_consumer", "species": ["mosquito_larva", "blood_worm"], "description": "Larval mosquitoes and blood worms that feed on the algae and dissolved blood"},
            {"layer": "secondary_consumer", "species": ["mosquito_worker", "mosquito_soldier"], "description": "Adult mosquitoes that hunt wild animals and weak cultivators"},
            {"layer": "tertiary_consumer", "species": ["mosquito_elite", "mosquito_queen"], "description": "Elite mosquitoes that can drain Qi from Foundation Establishment cultivators; queens that command swarms"},
            {"layer": "apex", "species": ["ancient_mosquito_queen"], "description": "The ancient queen — a Soul Formation tier beast that has lived for millennia, absorbing the blood of millions"},
        ],
        "ecosystem_features": [
            "blood_pools — standing water infused with spirit beast blood; breeding ground for larvae",
            "egg_chambers — underground tunnels where queens lay thousands of eggs",
            "nest_tunnels — extensive underground network connecting blood pools and egg chambers",
            "spirit_trees — trees that grow from blood-infused soil; their fruit attracts prey animals",
            "corpse_middens — piles of drained corpses near the nest entrances",
            "queen_chamber — the deepest chamber where the ancient queen resides",
        ],
        "population_dynamics": {
            "larvae_per_valley": 10000000,
            "adult_workers_per_valley": 1000000,
            "adult_soldiers_per_valley": 100000,
            "elites_per_valley": 100,
            "queens_per_valley": 10,
            "ancient_queens_per_valley": 1,
        },
        "seasonal_cycle": "population_booms_during_spirit_beast_migration_season — crashes_during_winter_when_prey_is_scarce",
        "canon_source": "I093 — Mosquito Beast; derived ecology from novel description",
    },
}


def generate_species_definitions(canon, report):
    """Pass 9: Generate spirit beast species definition JSONs."""
    lang_entries = {}

    for name, data in SPECIES_DEFINITIONS.items():
        filepath = f"{SPECIES_DIR}/{name}.json"
        if not os.path.exists(filepath):
            write_json(filepath, data)
            report["species"] = report.get("species", 0) + 1

    return lang_entries


# ============================================================
# PASS 10: LOOT PROVENANCE METADATA
# ============================================================
# Every artifact gets a provenance JSON with: origin, maker, age, why made,
# current owner, known history, restrictions, blood-bound status.

def generate_loot_provenance(canon, report):
    """Pass 10: Generate provenance JSONs for all artifacts in the canon database."""
    lang_entries = {}

    for artifact in canon["artifacts"]:
        artifact_id = artifact["id"]
        name = artifact["name"]
        name_cn = artifact.get("nameCn", "")
        slug = name.lower().replace(" ", "_").replace("'", "").replace("/", "_").replace("(", "").replace(")", "").replace("—", "_").replace("-", "_")[:60]

        provenance = {
            "_comment": f"Provenance metadata for {name} ({name_cn}). Auto-generated from canon database. Every item answers: Where did this originate? Who made it? When? Why? How old? What cultivation? Current owner? Known history?",
            "artifact_id": artifact_id,
            "name": name,
            "nameCn": name_cn,
            "type": artifact.get("type", "unknown"),
            "category": artifact.get("category", "unknown"),
            "origin": artifact.get("origin", "unknown"),
            "current_owner": artifact.get("currentOwner", "unknown"),
            "abilities": artifact.get("abilities", []),
            "canon_confidence": artifact.get("canonConfidence", 3),
            "known_facts": artifact.get("knownFacts", []),
            "source": artifact.get("source", "canon"),
            "provenance_metadata": {
                "derivation_type": "A" if artifact.get("canonConfidence", 3) >= 4 else "B",
                "blood_bound": "unknown",
                "has_restrictions": "unknown",
                "repair_count": 0,
                "known_owners": [],
                "historical_events": [],
                "estimated_age_years": "unknown",
                "creation_circumstances": artifact.get("origin", "unknown"),
            },
        }

        filepath = f"{PROVENANCE_DIR}/{slug}.json"
        if not os.path.exists(filepath):
            write_json(filepath, provenance)
            report["provenance"] = report.get("provenance", 0) + 1

    return lang_entries


# ============================================================
# MAIN
# ============================================================
def main():
    print("=" * 60)
    print("Canon Worldgen Adapter — Expansion Passes 7-10")
    print("=" * 60)

    canon = load_canon()
    report = {}
    all_lang = {}

    print("\n--- Pass 7: Sky Realm + Sky Biomes ---")
    lang = generate_sky_biomes(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.get('biomes', 0)} sky biomes")
    print(f"  Generated {report.get('dimensions', 0)} sky dimension")
    print(f"  Generated {report.get('dimension_types', 0)} sky dimension type")

    print("\n--- Pass 8: Underground Civilization Biomes ---")
    before = report.get("biomes", 0)
    lang = generate_underground_biomes(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.get('biomes', 0) - before} underground biomes")

    print("\n--- Pass 9: Spirit Beast Species Definitions ---")
    lang = generate_species_definitions(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.get('species', 0)} species definition JSONs")

    print("\n--- Pass 10: Loot Provenance Metadata ---")
    lang = generate_loot_provenance(canon, report)
    all_lang.update(lang)
    print(f"  Generated {report.get('provenance', 0)} provenance JSONs")

    # Update lang file
    print("\n--- Updating lang file ---")
    with open(LANG_FILE) as f:
        lang = json.load(f)
    added = 0
    for key, value in all_lang.items():
        if key not in lang:
            lang[key] = value
            added += 1
    if added > 0:
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
    print(f"  Added {added} new lang entries")

    print("\n" + "=" * 60)
    total = sum(report.values())
    print(f"Total new assets: {total}")
    for k, v in report.items():
        print(f"  {k}: {v}")
    print("=" * 60)

if __name__ == "__main__":
    main()
