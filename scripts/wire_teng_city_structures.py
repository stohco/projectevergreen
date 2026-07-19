#!/usr/bin/env python3
"""AUTO-CANON-019: Wire Teng Family City template pools to custom NBTs.

Problem: All 12 template pools for Teng Family City point to vanilla Minecraft
NBTs (taiga/plains houses) despite 11 custom NBTs existing in structures/teng_family_city/.
Player exploring zhao_mountains biome finds vanilla houses instead of the Teng clan seat.

Fix: Update all 12 start_pool.json files to reference ergenverse:teng_family_city/<name> NBTs.
Main pool → city_gate (entry point). District pools → their matching NBT.
"""

import json
import os
import glob

BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
POOL_DIR = os.path.join(BASE, "src/main/resources/data/ergenverse/worldgen/template_pool")

# Mapping: pool directory name suffix -> NBT location suffix
# Main pool (teng_family_city) -> city_gate (the city entry point)
DISTRICT_MAP = {
    "teng_family_city": "city_gate",
    "teng_family_city_city_gate": "city_gate",
    "teng_family_city_cultivator_quarter": "cultivator_quarter",
    "teng_family_city_governor_mansion": "governor_mansion",
    "teng_family_city_market_district": "market_district",
    "teng_family_city_mortal_quarter": "mortal_quarter",
    "teng_family_city_port_docks": "port_docks",
    "teng_family_city_residential_district": "residential_district",
    "teng_family_city_smuggler_tunnels": "smuggler_tunnels",
    "teng_family_city_tavern_district": "tavern_district",
    "teng_family_city_temple_district": "temple_district",
    "teng_family_city_warehouse_district": "warehouse_district",
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
    new_loc = f"ergenverse:teng_family_city/{nbt_suffix}"

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
    print("=== Wiring Teng Family City template pools to custom NBTs ===")
    fixed = 0
    for pool_name, nbt_suffix in DISTRICT_MAP.items():
        if fix_pool(pool_name, nbt_suffix):
            fixed += 1
    print(f"\nFixed {fixed}/{len(DISTRICT_MAP)} pools")

    # Verify: check all pools now point to ergenverse:
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
        print("\nAll pools verified: pointing to ergenverse custom NBTs.")
    else:
        print("\nWARNING: Some pools still point to vanilla NBTs!")

if __name__ == "__main__":
    main()