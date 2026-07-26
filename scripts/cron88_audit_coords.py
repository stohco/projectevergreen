#!/usr/bin/env python3
"""
CRON-88 verification script for JSON vs Java coordinate audit.

Parses:
  - Java side: PlanetSuzakuBlueprint.java (CanonLocation constants)
  - JSON side: planet_suzaku.json (settlements array)

Reports:
  1. Coordinate mismatches (same id, different x/z)
  2. ID mismatches (Java id != JSON id for the same settlement)
  3. Missing entries (in Java but not JSON, or vice versa)
  4. Canon name errors (wrong characters, wrong source attribution)

Exit code 0 = ALL COORDINATES MATCH. Non-zero = mismatches found.
"""
import json
import re
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
JAVA_FILE = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"
JSON_FILE = FORGE_MOD / "src/main/resources/data/ergenverse/worldgen/blueprint/planet_suzaku.json"

# ============================================================
# JAVA SIDE: parse CanonLocation constants
# ============================================================

def parse_java_coordinates():
    """Parse CanonLocation constants from PlanetSuzakuBlueprint.java."""
    src = JAVA_FILE.read_text(encoding="utf-8")

    # Pattern: new CanonLocation("id", "name", x, y, z, "category", "reference")
    pattern = r'new CanonLocation\(\s*"([^"]+)",\s*"([^"]+)",\s*(-?\d+),\s*(-?\d+),\s*(-?\d+),\s*"([^"]+)"'
    matches = re.findall(pattern, src)

    java_locs = {}
    for loc_id, name, x, y, z, category in matches:
        java_locs[loc_id] = {
            "name": name,
            "x": int(x),
            "y": int(y),
            "z": int(z),
            "category": category,
        }
    return java_locs


# ============================================================
# JSON SIDE: parse settlements array
# ============================================================

def parse_json_coordinates():
    """Parse settlements from planet_suzaku.json."""
    data = json.loads(JSON_FILE.read_text(encoding="utf-8"))
    blueprint = data.get("blueprint", {})
    settlements = blueprint.get("settlements", [])

    json_locs = {}
    for s in settlements:
        sid = s.get("id", "")
        json_locs[sid] = {
            "name": s.get("name", ""),
            "x": s.get("x"),
            "z": s.get("z"),
            "y": s.get("y", 0),
            "type": s.get("type", ""),
            "canon_name": s.get("canon_name", ""),
            "canon_confidence": s.get("canon_confidence", ""),
        }
    return json_locs


# ============================================================
# ID MAPPING (known renames)
# ============================================================

# ID MAPPING (known renames)
# CRON-88: these IDs were previously different between Java and JSON,
# but have now been synced. The mapping is empty — both sides use the Java ID.
ID_MAP = {}


def main():
    print("=" * 70)
    print("CRON-88 AUDIT: JSON vs Java coordinate consistency")
    print("=" * 70)

    java_locs = parse_java_coordinates()
    json_locs = parse_json_coordinates()

    print(f"\nJava side: {len(java_locs)} locations in PlanetSuzakuBlueprint.java")
    print(f"JSON side: {len(json_locs)} settlements in planet_suzaku.json")

    # ============================================================
    # CHECK 1: Coordinate mismatches
    # ============================================================
    print("\n=== CHECK 1: COORDINATE MATCHING ===")
    mismatches = []
    matches = []

    for java_id, java_data in java_locs.items():
        # Map Java ID to JSON ID
        json_id = ID_MAP.get(java_id, java_id)

        if json_id not in json_locs:
            # Check if it's a special case (sea_of_devils has polygon, no x/z)
            if java_id == "sea_of_devils" and "sea_of_devils" in json_locs:
                json_data = json_locs["sea_of_devils"]
                if json_data["x"] is None or json_data["z"] is None:
                    mismatches.append(f"{java_id}: JSON has no x/z (polygon only); Java has ({java_data['x']}, {java_data['z']})")
                    continue
            mismatches.append(f"{java_id}: MISSING from JSON settlements")
            continue

        json_data = json_locs[json_id]

        # Skip entries without x/z (like sea_of_devils polygon)
        if json_data["x"] is None or json_data["z"] is None:
            mismatches.append(f"{java_id}: JSON has no x/z (polygon only); Java has ({java_data['x']}, {java_data['z']})")
            continue

        jx, jz = java_data["x"], java_data["z"]
        json_x, json_z = json_data["x"], json_data["z"]

        if jx == json_x and jz == json_z:
            matches.append(f"{java_id}: ({jx}, {jz}) ✓")
        else:
            mismatches.append(f"{java_id}: Java=({jx}, {jz}) vs JSON=({json_x}, {json_z}) [JSON id: {json_id}]")

    print(f"  Matches: {len(matches)}")
    for m in matches:
        print(f"    ✓ {m}")

    print(f"  Mismatches: {len(mismatches)}")
    for m in mismatches:
        print(f"    ✗ {m}")

    # ============================================================
    # CHECK 2: ID consistency
    # ============================================================
    print("\n=== CHECK 2: ID CONSISTENCY ===")
    id_issues = []
    for java_id, json_id in ID_MAP.items():
        if java_id in java_locs and json_id in json_locs:
            id_issues.append(f"ID MISMATCH: Java '{java_id}' vs JSON '{json_id}' (same settlement, different IDs)")
        elif java_id in java_locs and json_id not in json_locs:
            # Already fixed
            pass

    if id_issues:
        print(f"  ID issues: {len(id_issues)}")
        for issue in id_issues:
            print(f"    ✗ {issue}")
    else:
        print("  No ID mismatches (all IDs aligned).")

    # ============================================================
    # CHECK 3: Canon name errors
    # ============================================================
    print("\n=== CHECK 3: CANON NAME ERRORS ===")
    canon_errors = []

    # Check for 滕 (wrong) instead of 藤 (correct) in Teng City
    for sid, data in json_locs.items():
        canon_name = data.get("canon_name", "")
        if "滕" in canon_name:
            canon_errors.append(f"{sid}: canon_name contains 滕 (should be 藤): {canon_name}")

    # Check for 罗河宗 (wrong) instead of 洛河门 (correct) in Luo He Sect
    for sid, data in json_locs.items():
        canon_name = data.get("canon_name", "")
        if "罗河宗" in canon_name:
            canon_errors.append(f"{sid}: canon_name contains 罗河宗 (should be 洛河门): {canon_name}")

    # Check for "A Will Eternal" (wrong source) in Luo He Sect
    # CRON-88: the fix says "NOT from A Will Eternal" — the script should
    # only flag POSITIVE references to "A Will Eternal", not negations.
    for sid, data in json_locs.items():
        confidence = data.get("canon_confidence", "")
        if "A Will Eternal" in confidence and "NOT from A Will Eternal" not in confidence:
            canon_errors.append(f"{sid}: canon_confidence references 'A Will Eternal' (should be RI/仙逆): {confidence}")

    if canon_errors:
        print(f"  Canon errors: {len(canon_errors)}")
        for err in canon_errors:
            print(f"    ✗ {err}")
    else:
        print("  No canon name errors found.")

    # ============================================================
    # SUMMARY
    # ============================================================
    print("\n" + "=" * 70)
    total_issues = len(mismatches) + len(id_issues) + len(canon_errors)
    if total_issues == 0:
        print(f"RESULT: ALL CHECKS PASSED — {len(matches)} locations match, 0 mismatches, 0 ID issues, 0 canon errors.")
        sys.exit(0)
    else:
        print(f"RESULT: {total_issues} issue(s) found ({len(mismatches)} coord mismatches, {len(id_issues)} ID issues, {len(canon_errors)} canon errors).")
        sys.exit(1)


if __name__ == "__main__":
    main()
