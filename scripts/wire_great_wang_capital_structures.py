#!/usr/bin/env python3
"""AUTO-CANON-022: Wire Great Wang Capital template pools to custom NBTs.

Problem: All 11 template pools for Great Wang Capital point to vanilla Minecraft
NBTs (plains houses, terminators) despite 11 custom NBTs (529 KB) existing in
structures/great_wang_capital/. Player exploring great_wang_dynasty biome finds
vanilla plains houses instead of the Zhao Country capital.

Canon: Great Wang Capital is the political center of Zhao Country in Renegade
Immortal. The Wang family (Wang Lin's clan) has historical ties here. This is
a major Zhao Country location that must be reachable and explorable.

Fix: Update all 11 start_pool.json files to reference ergenverse:great_wang_capital/<name>.
Pattern identical to AUTO-CANON-019 (Teng Family City wiring).
"""

import json
import os

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
POOL_DIR = os.path.join(BASE, "src/main/resources/data/ergenverse/worldgen/template_pool")

# Mapping: pool directory name -> NBT file name (without .nbt)
DISTRICT_MAP = {
    "great_wang_capital_city_gate": "city_gate",
    "great_wang_capital_cultivator_quarter": "cultivator_quarter",
    "great_wang_capital_governor_mansion": "governor_mansion",
    "great_wang_capital_market_district": "market_district",
    "great_wang_capital_mortal_quarter": "mortal_quarter",
    "great_wang_capital_port_docks": "port_docks",
    "great_wang_capital_residential_district": "residential_district",
    "great_wang_capital_smuggler_tunnels": "smuggler_tunnels",
    "great_wang_capital_tavern_district": "tavern_district",
    "great_wang_capital_temple_district": "temple_district",
    "great_wang_capital_warehouse_district": "warehouse_district",
}

def fix_pool(pool_dir_name, nbt_suffix):
    pool_path = os.path.join(POOL_DIR, pool_dir_name, "start_pool.json")
    if not os.path.exists(pool_path):
        print(f"  SKIP (no file): {pool_path}")
        return False

    with open(pool_path, "r") as f:
        data = json.load(f)

    elem = data["elements"][0]
    # Handle both nested format (main pool) and flat format (district pools)
    if "element" in elem:
        actual = elem["element"]
    else:
        actual = elem

    old_loc = actual.get("location", "N/A")
    new_loc = f"ergenverse:great_wang_capital/{nbt_suffix}"

    if old_loc == new_loc:
        print(f"  OK (already correct): {pool_dir_name} -> {new_loc}")
        return False

    print(f"  FIX: {pool_dir_name}")
    print(f"    OLD: {old_loc}")
    print(f"    NEW: {new_loc}")

    actual["location"] = new_loc

    with open(pool_path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")

    return True

def main():
    print("=== Wiring Great Wang Capital template pools to custom NBTs ===")
    fixed = 0
    for pool_name, nbt_suffix in DISTRICT_MAP.items():
        if fix_pool(pool_name, nbt_suffix):
            fixed += 1
    print(f"\nFixed {fixed}/{len(DISTRICT_MAP)} pools")

    # Verify
    print("\n=== Verification ===")
    all_ok = True
    for pool_name in DISTRICT_MAP:
        pool_path = os.path.join(POOL_DIR, pool_name, "start_pool.json")
        if not os.path.exists(pool_path):
            print(f"  MISSING: {pool_path}")
            all_ok = False
            continue
        with open(pool_path, "r") as f:
            data = json.load(f)
        elem = data["elements"][0]
        actual = elem.get("element", elem) if isinstance(elem.get("element"), dict) else elem
        loc = actual.get("location", "N/A")
        if loc.startswith("ergenverse:"):
            print(f"  OK: {pool_name} -> {loc}")
        else:
            print(f"  STILL VANILLA: {pool_name} -> {loc}")
            all_ok = False

    if all_ok:
        print("\nAll 11 pools verified: pointing to ergenverse custom NBTs.")
    else:
        print("\nWARNING: Some pools still point to vanilla NBTs!")

if __name__ == "__main__":
    main()