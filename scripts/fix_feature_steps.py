#!/usr/bin/env python3
"""Fix feature step misplacements in biome JSONs.

Misplacements found by audit:
- forest_rocks in step 2 or 9 instead of 7 (UNDERGROUND_DECORATION)
- fossil_underground in step 3 or 9 instead of 7 (UNDERGROUND_DECORATION)
- nether_fossil in step 4 (SURFACE_STRUCTURES) instead of 9 (VEGETAL_DECORATION)
- freeze_top_layer in step 2 instead of 10 (TOP_LAYER_MODIFICATION)
- patch_snow/patch_ice/patch_blue_ice in step 2 instead of 10
- ice_spike in step 2 instead of 9 (VEGETAL_DECORATION)
"""

import json
import os

BASE = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/biome"

# Define move rules: (feature_name_fragment, from_step, to_step)
MOVE_RULES = [
    ("forest_rocks", None, 7),       # always goes to step 7
    ("fossil_underground", None, 7),  # always goes to step 7
    ("nether_fossil", None, 9),       # always goes to step 9
    ("freeze_top_layer", None, 10),   # always goes to step 10
    ("patch_snow", None, 10),         # always goes to step 10
    ("patch_ice", None, 10),          # always goes to step 10
    ("patch_blue_ice", None, 10),     # always goes to step 10
    ("ice_spike", None, 9),           # always goes to step 9
]

step_names = [
    "RAW_GEN", "LAKES", "LOCAL_MOD", "UG_STRUCT", "SURF_STRUCT",
    "STRONGHOLD", "UG_ORES", "UG_DECOR", "FLUID_SPRING", "VEG_DECOR", "TOP_LAYER"
]

fixed_count = 0
for fname in sorted(os.listdir(BASE)):
    if not fname.endswith(".json"):
        continue
    filepath = os.path.join(BASE, fname)
    with open(filepath, "r") as f:
        data = json.load(f)

    features = data.get("features", [])
    if not features or not isinstance(features[0], list):
        continue

    changed = False
    moves = []  # (feat, from_step, to_step)

    # First pass: find all misplacements
    for i, step in enumerate(features):
        feats_to_move = []
        for feat in step:
            name = feat.split(":")[-1]
            for rule_name, _, to_step in MOVE_RULES:
                if name == rule_name and i != to_step:
                    feats_to_move.append((feat, to_step))
                    break
        for feat, to_step in feats_to_move:
            features[i].remove(feat)
            features[to_step].append(feat)
            moves.append((feat, i, to_step))
            changed = True

    if changed:
        fixed_count += 1
        print(f"{fname}:")
        for feat, from_s, to_s in moves:
            print(f"  {feat}: step {from_s} ({step_names[from_s]}) -> step {to_s} ({step_names[to_s]})")
        with open(filepath, "w") as f:
            json.dump(data, f, indent=2)
        print(f"  SAVED")
    else:
        pass  # OK, no output needed

print(f"\nFixed {fixed_count} biome files")