#!/usr/bin/env python3
"""
CRON-90 Final Verification Script

Comprehensive verification of the country polygon containment fix.
Checks:
1. All settlements with country assignments are inside their polygons
2. Country assignments are canon-correct
3. distance_from_wang_village fields are recomputed correctly
4. New countries (sea_of_devils_region, four_sects_alliance) are properly defined
5. No duplicate country IDs
6. JSON is valid
"""
import json
import math
import sys
import os

BLUEPRINT_PATH = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/blueprint/planet_suzaku.json"
WANG_VILLAGE = (3842, -1184)

errors = []
checks_passed = 0
checks_total = 0

def check(condition, msg):
    global checks_passed, checks_total
    checks_total += 1
    if condition:
        checks_passed += 1
        print(f"  PASS: {msg}")
    else:
        errors.append(msg)
        print(f"  FAIL: {msg}")

def point_in_polygon(x, z, polygon):
    n = len(polygon)
    if n < 3:
        return False
    inside = False
    j = n - 1
    for i in range(n):
        xi, zi = polygon[i][0], polygon[i][1]
        xj, zj = polygon[j][0], polygon[j][1]
        if ((zi > z) != (zj > z)) and \
           (x < (xj - xi) * (z - zi) / (zj - zi + 1e-12) + xi):
            inside = not inside
        j = i
    return inside

def expected_distance(x, z):
    return int(round(math.sqrt((x - WANG_VILLAGE[0])**2 + (z - WANG_VILLAGE[1])**2)))

print("=" * 72)
print("CRON-90 Final Verification: Country Polygon Containment Fix")
print("=" * 72)

# Load JSON
with open(BLUEPRINT_PATH) as f:
    data = json.load(f)
bp = data["blueprint"]

# ── CHECK 1: JSON valid + structure ──
print("\n[1] JSON structure check")
check(isinstance(bp, dict), "Blueprint is a JSON object")
check("countries" in bp, "Has 'countries' array")
check("settlements" in bp, "Has 'settlements' array")
check(len(bp["countries"]) == 12, f"12 countries (got {len(bp['countries'])})")
check(len(bp["settlements"]) == 16, f"16 settlements (got {len(bp['settlements'])})")

# ── CHECK 2: No duplicate country IDs ──
print("\n[2] Country ID uniqueness check")
country_ids = [c["id"] for c in bp["countries"]]
check(len(country_ids) == len(set(country_ids)), "No duplicate country IDs")
print(f"  Countries: {sorted(country_ids)}")

# ── CHECK 3: New countries exist ──
print("\n[3] New countries check")
countries_by_id = {c["id"]: c for c in bp["countries"]}
check("sea_of_devils_region" in countries_by_id, "sea_of_devils_region country exists")
check("four_sects_alliance" in countries_by_id, "four_sects_alliance country exists")

if "sea_of_devils_region" in countries_by_id:
    sdr = countries_by_id["sea_of_devils_region"]
    check(sdr.get("canon_name") == "Xiu Mo Hai / 修魔海", "sea_of_devils_region canon_name correct")
    check(len(sdr.get("polygon", [])) >= 6, "sea_of_devils_region has polygon with ≥6 vertices")

if "four_sects_alliance" in countries_by_id:
    fsa = countries_by_id["four_sects_alliance"]
    check(fsa.get("canon_name") == "Si Pai Meng / 四派联盟", "four_sects_alliance canon_name correct")
    check(len(fsa.get("polygon", [])) >= 6, "four_sects_alliance has polygon with ≥6 vertices")

# ── CHECK 4: Polygon containment ──
print("\n[4] Settlement polygon containment check")
violations = []
for s in bp["settlements"]:
    sid = s["id"]
    x = s.get("x")
    z = s.get("z")
    country_id = s.get("country")
    
    if x is None or z is None:
        continue
    if not country_id:
        # sea_of_devils is wilderness — correct
        if sid == "sea_of_devils":
            print(f"  OK    {sid} ({x},{z}): wilderness (geographic feature, correct)")
        else:
            violations.append(f"{sid}: unexpectedly has no country")
        continue
    if country_id not in countries_by_id:
        violations.append(f"{sid}: country '{country_id}' not defined")
        continue
    
    poly = countries_by_id[country_id].get("polygon", [])
    if point_in_polygon(x, z, poly):
        print(f"  OK    {sid} ({x},{z}) -> {country_id}")
    else:
        violations.append(f"{sid} ({x},{z}) -> {country_id}: OUTSIDE polygon")

check(len(violations) == 0, f"All settlements inside their polygons ({len(violations)} violations)")
if violations:
    for v in violations:
        print(f"    VIOLATION: {v}")

# ── CHECK 5: Canon-correct country assignments ──
print("\n[5] Canon country assignment check")
EXPECTED_COUNTRIES = {
    "wang_family_village": "zhao",
    "heng_yue_sect": "zhao",
    "teng_family_city": "zhao",
    "tian_shui_city": "zhao",
    "zhao_capital": "zhao",
    "jue_ming_valley": "zhao",
    "xuan_dao_sect": "zhao",        # 朴南子 is 赵国第一人
    "soul_refining_sect": "pilu",    # 毗卢国 three sects
    "luo_he_sect": "fire_burn",      # 火焚国洛河门
    "qilin_city": "sea_of_devils_region",  # 修魔海 beast-city
    "nan_dou_city": "sea_of_devils_region", # 修魔海 beast-city
    "snow_domain_capital": "snow_domain",
    "vermilion_bird_capital": "vermilion_bird",
    "suzaku_tomb": "vermilion_bird",
    "four_sects_alliance": "four_sects_alliance",  # independent
}

settlements_by_id = {s["id"]: s for s in bp["settlements"]}
for sid, expected_country in EXPECTED_COUNTRIES.items():
    if sid not in settlements_by_id:
        check(False, f"{sid}: settlement not found in JSON")
        continue
    actual = settlements_by_id[sid].get("country")
    check(actual == expected_country, f"{sid}: country={actual} (expected {expected_country})")

# ── CHECK 6: distance_from_wang_village recomputed ──
print("\n[6] distance_from_wang_village recomputation check")
distance_errors = []
for s in bp["settlements"]:
    if "distance_from_wang_village" in s:
        sid = s["id"]
        x = s.get("x")
        z = s.get("z")
        if x is None or z is None:
            continue
        actual = s["distance_from_wang_village"]
        expected = expected_distance(x, z)
        if actual != expected:
            distance_errors.append(f"{sid}: {actual} (expected {expected})")
        else:
            print(f"  OK    {sid}: {actual} blocks")

check(len(distance_errors) == 0, f"All distance_from_wang_village correct ({len(distance_errors)} errors)")
if distance_errors:
    for e in distance_errors:
        print(f"    ERROR: {e}")

# ── CHECK 7: sea_of_devils settlement has no country (wilderness) ──
print("\n[7] Sea of Devils wilderness check")
sea = settlements_by_id.get("sea_of_devils")
if sea:
    check(sea.get("country") is None or sea.get("country") == "",
          "sea_of_devils has no country (wilderness, correct — it's a geographic feature spanning multiple regions)")
    check(sea.get("x") == 6000 and sea.get("z") == -1184,
          "sea_of_devils coordinates (6000,-1184) match Java PlanetSuzakuBlueprint")

# ── CHECK 8: Canon name errors corrected in descriptions ──
print("\n[8] Description canon check")
soul_desc = settlements_by_id.get("soul_refining_sect", {}).get("description", "")
check("Pilu Kingdom" in soul_desc or "毗卢国" in soul_desc,
      "soul_refining_sect description mentions Pilu Kingdom (毗卢国)")

luo_desc = settlements_by_id.get("luo_he_sect", {}).get("description", "")
check("Fire Burn Country" in luo_desc or "火焚国" in luo_desc,
      "luo_he_sect description mentions Fire Burn Country (火焚国)")

qilin_desc = settlements_by_id.get("qilin_city", {}).get("description", "")
check("Sea of Devils" in qilin_desc or "修魔海" in qilin_desc,
      "qilin_city description mentions Sea of Devils (修魔海)")

# ── SUMMARY ──
print("\n" + "=" * 72)
print(f"RESULTS: {checks_passed}/{checks_total} checks passed")
if errors:
    print(f"FAILURES: {len(errors)}")
    for e in errors:
        print(f"  - {e}")
    sys.exit(1)
else:
    print("ALL CHECKS PASSED")
    sys.exit(0)
