#!/usr/bin/env python3
"""Fix biome format bugs and other data-driven worldgen issues.

Bug 1: 5 biomes have flat features array instead of 11-step list-of-lists.
  - ancient_god_level1_hurricane.json
  - ancient_god_level2_restriction.json
  - ancient_god_level3_annihilation.json
  - immortal_graveyard.json
  - suzaku_tomb.json

Bug 2: zhao_mountains.json references minecraft:emerald_ore (should be minecraft:ore_emerald)

Bug 3: spirit_vein_ore.json has malformed target predicates (predicate_type at wrong nesting level).
  Fix: rewrite with correct nested format to match spirit_vein_quartz_ore.json pattern.
  Alternatively delete it since it's orphaned (no placed_feature or biome references it).
  DECISION: Delete it — it's a duplicate of spirit_vein_quartz_ore.json with wrong block (nether_quartz_ore
  doesn't generate in overworld). spirit_vein_quartz_ore.json is the correct, referenced one.
"""

import json
import os
import shutil

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/biome"

# Generation step classification for vanilla MC 1.20.1 features:
# Step 0: RAW_GENERATION
# Step 1: LAKES
# Step 2: LOCAL_MODIFICATIONS
# Step 3: UNDERGROUND_STRUCTURES
# Step 4: SURFACE_STRUCTURES
# Step 5: STRONGHOLDS
# Step 6: UNDERGROUND_ORES
# Step 7: UNDERGROUND_DECORATION
# Step 8: FLUID_SPRINGS
# Step 9: VEGETAL_DECORATION
# Step 10: TOP_LAYER_MODIFICATION

# Feature classification map — which vanilla features go in which step
ORE_FEATURES = {
    "ore_dirt", "ore_gravel", "ore_granite_upper", "ore_granite_lower",
    "ore_diorite_upper", "ore_diorite_lower", "ore_andesite_upper",
    "ore_andesite_lower", "ore_coal_upper", "ore_coal_lower",
    "ore_iron_upper", "ore_iron_middle", "ore_iron_small",
    "ore_gold_upper", "ore_gold_lower", "ore_gold_extra",
    "ore_copper", "ore_redstone", "ore_lapis", "ore_diamond",
    "ore_emerald", "ore_quartz", "ore_blackstone", "ore_magma",
    "ore_debris_small", "ore_debris_large",
}

UNDERGROUND_DECORATION = {
    "fossil_underground", "forest_rocks",
}

VEGETAL_DECORATION = {
    "patch_dead_bush", "patch_taiga_grass", "patch_grass_badlands",
    "patch_grass_normal", "patch_grass_forest", "patch_grass_jungle",
    "patch_grass_plain", "patch_grass_savanna", "patch_grass_taiga",
    "patch_tall_grass", "brown_mushroom_normal", "red_mushroom_normal",
    "forest_flowers", "patch_sugar_cane", "patch_pumpkin",
    "flower_default", "flower_forest", "flower_meadow",
    "flower_cherry", "flower_plain", "flower_sunflower",
    "patch_waterlily", "patch_large_fern",
    "nether_fossil", "nether_sprouts",
    "basalt_pillar", "large_basalt_columns", "delta",
    "patch_fire", "glowstone_extra", "vine",
    "patch_coral", "sea_pickle", "kelp_cold", "kelp_warm",
    "ice_spike", "patch_ice", "patch_blue_ice", "patch_snow",
    "freeze_top_layer",
}

SURFACE_STRUCTURES = {
    "swamp_hut", "village_plains", "village_desert", "village_savanna",
    "village_taiga", "village_snowy", "pillager_outpost",
    "desert_pyramid", "jungle_pyramid", "igloo", "mansion",
    "ocean_ruin_cold", "ocean_ruin_warm", "shipwreck_beached",
    "shipwreck", "monument", "end_city", "fortress",
}

RAW_GENERATION = {
    "lake_lava_underground", "lake_lava_surface", "lake_water",
    "end_spike", "end_gateway_delayed", "end_gateway_return",
}

LOCAL_MODIFICATIONS = {
    "amethyst_geode",
}

FLUID_SPRINGS = {
    "spring_water", "spring_lava", "spring_closed",
}

TOP_LAYER = {
    "freeze_top_layer",
    "patch_snow", "patch_ice", "patch_blue_ice",
}

STRONGHOLDS = {
    "stronghold", "fortress",
}

UNDERGROUND_STRUCTURES = {
    "mineshaft", "mineshaft_mesa", "ancient_city",
    "ruined_portal_standard", "ruined_portal_desert",
    "ruined_portal_jungle", "ruined_portal_swamp",
    "ruined_portal_mountain", "ruined_portal_ocean",
    "ruined_portal_nether",
    "nether_fossil_ruined_portal",
    "end_city",
}


def classify_feature(feat_name):
    """Classify a feature string into its generation step (0-10)."""
    # Strip namespace
    name = feat_name.split(":")[-1] if ":" in feat_name else feat_name

    if name in ORE_FEATURES:
        return 6  # UNDERGROUND_ORES
    if name in UNDERGROUND_DECORATION:
        return 7  # UNDERGROUND_DECORATION
    if name in VEGETAL_DECORATION:
        return 9  # VEGETAL_DECORATION
    if name in SURFACE_STRUCTURES:
        return 4  # SURFACE_STRUCTURES
    if name in RAW_GENERATION:
        return 0  # RAW_GENERATION
    if name in LOCAL_MODIFICATIONS:
        return 2  # LOCAL_MODIFICATIONS
    if name in FLUID_SPRINGS:
        return 8  # FLUID_SPRINGS
    if name in TOP_LAYER:
        return 10  # TOP_LAYER_MODIFICATION
    if name in STRONGHOLDS:
        return 5  # STRONGHOLDS
    if name in UNDERGROUND_STRUCTURES:
        return 3  # UNDERGROUND_STRUCTURES

    # Default: if it starts with "ore_", put in ores step
    if name.startswith("ore_"):
        return 6

    # ergenverse features
    if "spirit_vein" in name:
        return 6  # ores
    # Other ergenverse features are typically vegetation
    return 9  # default to VEGETAL_DECORATION


def convert_flat_to_list_of_lists(features_flat):
    """Convert a flat feature list to the 11-step list-of-lists format."""
    steps = [[] for _ in range(11)]
    for feat in features_flat:
        step = classify_feature(feat)
        steps[step].append(feat)
    return steps


def fix_biome(filepath):
    """Read a biome JSON, fix features format, write back."""
    with open(filepath, 'r') as f:
        data = json.load(f)

    features = data.get("features", [])
    if not features:
        print(f"  SKIP {filepath}: no features")
        return

    # Check if already in list-of-lists format
    if isinstance(features[0], list):
        print(f"  OK {filepath}: already list-of-lists ({len(features)} steps)")
        return

    # Convert flat list to list-of-lists
    print(f"  FIX {filepath}: converting {len(features)} flat features to 11-step format")
    steps = convert_flat_to_list_of_lists(features)
    data["features"] = steps

    # Print classification summary
    for i, step_features in enumerate(steps):
        if step_features:
            step_names = [
                "RAW_GEN", "LAKES", "LOCAL_MOD", "UG_STRUCT",
                "SURF_STRUCT", "STRONGHOLD", "UG_ORES", "UG_DECOR",
                "FLUID_SPRING", "VEG_DECOR", "TOP_LAYER"
            ]
            print(f"    Step {i} ({step_names[i]}): {step_features}")

    with open(filepath, 'w') as f:
        json.dump(data, f, indent=2)
    print(f"  SAVED {filepath}")


def fix_emerald_typo(filepath):
    """Fix minecraft:emerald_ore -> minecraft:ore_emerald in zhao_mountains."""
    with open(filepath, 'r') as f:
        content = f.read()

    if "minecraft:emerald_ore" in content:
        content = content.replace('"minecraft:emerald_ore"', '"minecraft:ore_emerald"')
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"  FIXED emerald_ore typo in {filepath}")
    else:
        print(f"  OK no emerald_ore typo in {filepath}")


def delete_orphaned_file(filepath):
    """Delete the orphaned spirit_vein_ore.json."""
    if os.path.exists(filepath):
        os.remove(filepath)
        print(f"  DELETED orphaned {filepath}")
    else:
        print(f"  SKIP {filepath}: not found")


if __name__ == "__main__":
    print("=== Fixing 5 flat-list biomes ===")
    flat_biomes = [
        "ancient_god_level1_hurricane.json",
        "ancient_god_level2_restriction.json",
        "ancient_god_level3_annihilation.json",
        "immortal_graveyard.json",
        "suzaku_tomb.json",
    ]
    for biome in flat_biomes:
        fix_biome(os.path.join(BASE, biome))

    print("\n=== Fixing emerald_ore typo in zhao_mountains ===")
    fix_emerald_typo(os.path.join(BASE, "zhao_mountains.json"))

    print("\n=== Deleting orphaned spirit_vein_ore.json ===")
    delete_orphaned_file(
        "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/configured_feature/spirit_vein_ore.json"
    )

    print("\n=== Done ===")