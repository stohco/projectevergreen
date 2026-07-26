#!/usr/bin/env python3
"""
CRON-90 Fix Script: Redraw country polygons + fix country assignments

After CRON-88 synced JSON settlement coordinates to Java PlanetSuzakuBlueprint
coordinates, 10 of 16 settlements fell OUTSIDE their assigned country polygons.
This script:

1. Adds 2 new countries:
   - sea_of_devils_region (修魔海 beast-cities: qilin_city, nan_dou_city)
   - four_sects_alliance (independent 四级修真国)

2. Redraws ALL country polygons to contain the canon-correct settlement
   coordinates. Polygons are drawn as bounding regions around the settlements
   that belong to each country, with appropriate margin.

3. Fixes country assignments for 5 settlements:
   - soul_refining_sect: zhao → pilu (canon: 毗卢国 three sects)
   - luo_he_sect: zhao → fire_burn (canon: 火焚国洛河门)
   - qilin_city: zhao → sea_of_devils_region (canon: 修魔海 beast-city)
   - nan_dou_city: nan_dou → sea_of_devils_region (canon: 修魔海 beast-city)
   - four_sects_alliance: vermilion_bird → four_sects_alliance (canon: independent)

4. Fixes canon name errors in settlement descriptions:
   - luo_he_sect: "Zhao State" → "Fire Burn Country (火焚国)"
   - qilin_city: "southern Zhao Country" → "修魔海 (Sea of Devils) region"
   - soul_refining_sect description verified canon-accurate

5. Recomputes stale distance_from_wang_village fields (CRON-89 next priority b).

Canon verification (web search 2026-07-26):
- 毗卢国 (Pilu) is where 炼魂宗 is located (Baidu Baike: "朱雀大陆毗卢国的三大宗派之一")
- 火焚国 (Fire Burn) is where 洛河门 is located (李慕婉 = 火焚国洛河门弟子)
- 修魔海 (Sea of Devils) is where 麒麟城 and 南斗城 beast-cities are located
  (Java PlanetSuzakuBlueprint: "RI 修魔海 arc — 麒麟兽城", "RI 修魔海 arc — beast-city")
- 四派联盟 (Four Sects Alliance) is an independent 四级修真国, NOT part of 朱雀国
- 玄道宗 IS in 赵国 (朴南子 is "赵国第一人", leads 赵国玄道宗)
"""
import json
import math
import sys

BLUEPRINT_PATH = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/worldgen/blueprint/planet_suzaku.json"
WANG_VILLAGE = (3842, -1184)

def distance(x1, z1, x2, z2):
    return math.sqrt((x2 - x1) ** 2 + (z2 - z1) ** 2)

# ── New country polygon definitions ──
# Each polygon is drawn to contain the canon-correct settlement coordinates
# with appropriate margin (300-500 blocks buffer around settlements).
NEW_COUNTRIES = {
    "zhao": {
        "polygon": [
            [-3000, -2400], [5000, -2400], [5400, -1600], [5200, -200],
            [5000, 600], [3800, 800], [2600, 800], [2400, 1600],
            [-1800, 1600], [-3000, 1600], [-3000, 0]
        ],
        # Contains: wang_family_village(3842,-1184), heng_yue_sect(4200,-1400),
        # teng_family_city(3500,-900), tian_shui_city(2600,-2000),
        # zhao_capital(4400,100), jue_ming_valley(4500,-500),
        # xuan_dao_sect(-2400,1400)
        # CRON-90 fix: extended left edge to z=1600 to contain xuan_dao_sect(-2400,1400)
    },
    "chu": {
        # Chu Country is east of Zhao — keep existing polygon (no settlements to validate)
        "polygon": [
            [7600, -2800], [11000, -2800], [11200, -800], [10800, 1200],
            [8200, 1800], [7600, 600], [7400, -200], [7600, -1600]
        ]
    },
    "vermilion_bird": {
        "polygon": [
            [-800, -800], [2800, -800], [3200, 600], [2800, 2400],
            [2400, 4200], [-200, 4200], [-800, 2400]
        ],
        # Contains: vermilion_bird_capital(0,0), suzaku_tomb(0,0)
    },
    "snow_domain": {
        "polygon": [
            [800, 2400], [3200, 2400], [3400, 4200], [2800, 5400],
            [1200, 5400], [600, 4000]
        ],
        # Contains: snow_domain_capital(2000,3200)
    },
    "fire_burn": {
        "polygon": [
            [2400, 1800], [3800, 1800], [4000, 3200], [3400, 4000],
            [2400, 4000], [2200, 2600]
        ],
        # Contains: luo_he_sect(3000,2400)
    },
    "sky_demon": {
        # Keep existing polygon (no settlements to validate)
        "polygon": [
            [9000, -5200], [13000, -5200], [13000, -2800], [11200, -2800],
            [9200, -3600]
        ]
    },
    "fire_demon": {
        # Keep existing polygon (no settlements to validate)
        "polygon": [
            [10600, 3600], [13000, 3600], [13000, 600], [11200, -800],
            [10800, 1200]
        ]
    },
    "pilu": {
        "polygon": [
            [-3200, -2400], [-1000, -2400], [-400, -1200], [-600, 0],
            [-1400, 800], [-2800, 800], [-3400, -400]
        ],
        # Contains: soul_refining_sect(-1600,-1800)
    },
    "xuan_wu": {
        # Keep existing polygon (no settlements to validate)
        "polygon": [
            [-500, -7000], [2800, -7000], [3200, -3800], [-500, -3200]
        ]
    },
    "qing_shui": {
        # Keep existing polygon (no settlements to validate)
        "polygon": [
            [-500, -1200], [2600, 600], [2400, 2400], [-200, 2600], [-600, 400]
        ]
    },
    # NEW country: sea_of_devils_region (修魔海 beast-cities)
    "sea_of_devils_region": {
        "name": "Sea of Devils Region",
        "canon_name": "Xiu Mo Hai / 修魔海",
        "canon_confidence": "EXPLICIT — vast demon-cultivation sea spanning half of Suzaku Star",
        "polygon": [
            [1200, -3200], [5400, -3200], [6400, -2400], [6200, -800],
            [5000, -400], [3600, -600], [2400, -800], [1400, -1600]
        ],
        # Contains: qilin_city(1800,-2600), nan_dou_city(4400,-2400),
        # sea_of_devils(6000,-1184) center point
        "capital": None,
        "biome_rule": "ergenverse:sea_of_devils",
        "terrain_description": "A vast perilous region of demon-cultivation seas and beast-cities. Spans nearly half of Suzaku Star. Home to ancient beast-cities like Qilin City and Nan Dou City. The highest peak, Po Tian (破天), is the gateway to the Foreign Battleground (域外战场).",
        "political_tier": "wilderness_region",
        "cultivation_power": "high — demonic cultivators and ancient beasts"
    },
    # NEW country: four_sects_alliance (四派联盟)
    "four_sects_alliance": {
        "name": "Four Sects Alliance",
        "canon_name": "Si Pai Meng / 四派联盟",
        "canon_confidence": "EXPLICIT — independent 四级修真国, Wang Lin's 化凡 arc region",
        "polygon": [
            [400, 1200], [1800, 1200], [2000, 2200], [1600, 2400],
            [600, 2400], [200, 1800]
        ],
        # Contains: four_sects_alliance(1000,1600)
        "capital": None,
        "biome_rule": "ergenverse:four_sects_alliance_region",
        "terrain_description": "An independent four-tier cultivation kingdom formed by an alliance of four sects: Eastern White Cloud Sect (东方白云宗), Southern Ink Sect (南方水墨门), Western Green Wood Cliff (西方青木崖), and Northern Black Soul Sect (北方黑魂派). Home of 曾大牛 (Zeng Da Niu). Wang Lin lives his 化凡 (mortal-life) arc here.",
        "political_tier": "minor_cultivation_kingdom",
        "cultivation_power": "moderate — four allied sects"
    },
}

# ── Settlement country assignment fixes ──
SETTLEMENT_COUNTRY_FIXES = {
    "soul_refining_sect": {
        "old_country": "zhao",
        "new_country": "pilu",
        "description_fix": "A cultivation sect in Pilu Kingdom (毗卢国), one of the three great sects. The patriarch Dun Tian (遁天) gifted Wang Lin the Billion Soul Flag (十亿尊魂幡). Canon: 炼魂宗 is one of the three great sects of 毗卢国 (Pilu Kingdom).",
    },
    "luo_he_sect": {
        "old_country": "zhao",
        "new_country": "fire_burn",
        "description_fix": "A cultivation sect in Fire Burn Country (火焚国), Li Muwan's original sect. Practices water/river cultivation arts. Canon: 李慕婉 is 火焚国洛河门弟子. The sect was nearly destroyed in the war between 火焚国 and 宣武国.",
    },
    "qilin_city": {
        "old_country": "zhao",
        "new_country": "sea_of_devils_region",
        "description_fix": "A major trading beast-city in the Sea of Devils (修魔海) region, sacred to divine Qilin beasts. Known for its wealth from qilin-related trade. Canon: RI 修魔海 arc — 麒麟兽城 (Qilin Beast City).",
    },
    "nan_dou_city": {
        "old_country": "nan_dou",
        "new_country": "sea_of_devils_region",
        "description_fix": "A beast-city in the Sea of Devils (修魔海) region, built on the back of an ancient beast. Canon: RI 修魔海 arc — beast-city (南斗城).",
    },
    "four_sects_alliance": {
        "old_country": "vermilion_bird",
        "new_country": "four_sects_alliance",
        "description_fix": "The Four Sects Alliance region where Wang Lin lives his 化凡 (mortal-life) arc. An independent 四级修真国 (four-tier cultivation kingdom) formed by four allied sects. Home of 曾大牛 (Zeng Da Niu). Canon: NOT part of 朱雀国 — it is an independent kingdom.",
    },
}

def main():
    with open(BLUEPRINT_PATH) as f:
        data = json.load(f)

    bp = data["blueprint"]

    # ── STEP 1: Update country polygons ──
    print("[1] Updating country polygons...")
    for country in bp["countries"]:
        cid = country["id"]
        if cid in NEW_COUNTRIES:
            new_data = NEW_COUNTRIES[cid]
            old_poly = country.get("polygon", [])
            country["polygon"] = new_data["polygon"]
            print(f"  {cid}: polygon redrawn ({len(old_poly)} → {len(new_data['polygon'])} vertices)")

    # ── STEP 2: Add new countries ──
    print("\n[2] Adding new countries...")
    existing_ids = {c["id"] for c in bp["countries"]}
    for cid, cdata in NEW_COUNTRIES.items():
        if cid not in existing_ids:
            # New country — add full definition
            new_country = {
                "id": cid,
                "name": cdata["name"],
                "canon_name": cdata["canon_name"],
                "canon_confidence": cdata["canon_confidence"],
                "polygon": cdata["polygon"],
                "capital": cdata["capital"],
                "biome_rule": cdata["biome_rule"],
                "terrain_description": cdata["terrain_description"],
                "political_tier": cdata["political_tier"],
                "cultivation_power": cdata["cultivation_power"]
            }
            bp["countries"].append(new_country)
            print(f"  {cid}: ADDED ({len(cdata['polygon'])} vertices)")

    # ── STEP 3: Fix settlement country assignments + descriptions ──
    print("\n[3] Fixing settlement country assignments...")
    for settlement in bp["settlements"]:
        sid = settlement["id"]
        if sid in SETTLEMENT_COUNTRY_FIXES:
            fix = SETTLEMENT_COUNTRY_FIXES[sid]
            old = settlement.get("country")
            new = fix["new_country"]
            settlement["country"] = new
            # Update description
            if "description" in settlement:
                settlement["description"] = fix["description_fix"]
            print(f"  {sid}: {old} → {new}")

    # ── STEP 4: Recompute stale distance_from_wang_village ──
    print("\n[4] Recomputing distance_from_wang_village fields...")
    wx, wz = WANG_VILLAGE
    for settlement in bp["settlements"]:
        if "distance_from_wang_village" in settlement:
            sx = settlement.get("x")
            sz = settlement.get("z")
            if sx is not None and sz is not None:
                old_dist = settlement["distance_from_wang_village"]
                new_dist = int(round(distance(wx, wz, sx, sz)))
                settlement["distance_from_wang_village"] = new_dist
                print(f"  {settlement['id']}: {old_dist} → {new_dist}")

    # ── STEP 5: Add CRON-90 comment to blueprint ──
    if "_comment" in data:
        existing_comment = data["_comment"]
        cron90_note = (
            "\n\nCRON-90: Country polygons redrawn to contain canon-correct "
            "settlement coordinates (fixes CRON-88 regression). 5 settlement "
            "country assignments corrected per canon (soul_refining_sect→pilu, "
            "luo_he_sect→fire_burn, qilin_city→sea_of_devils_region, "
            "nan_dou_city→sea_of_devils_region, four_sects_alliance→four_sects_alliance). "
            "2 new countries added (sea_of_devils_region, four_sects_alliance). "
            "Canon verification: 毗卢国 for 炼魂宗 (Baidu Baike), 火焚国 for 洛河门 "
            "(李慕婉=火焚国洛河门弟子), 修魔海 for 麒麟城+南斗城 (Java blueprint: "
            "RI 修魔海 arc — beast-cities), 四派联盟 independent (web search 2026-07-26)."
        )
        data["_comment"] = existing_comment + cron90_note

    # ── Write back ──
    with open(BLUEPRINT_PATH, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")

    print("\n[5] Blueprint written to", BLUEPRINT_PATH)
    print("    Run cron90_audit_polygon_containment.py to verify all settlements are now inside their polygons.")

if __name__ == "__main__":
    main()
