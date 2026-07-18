#!/usr/bin/env python3
"""
Update template_pool JSONs to reference custom .nbt structures
instead of vanilla village placeholders.

Maps each sect/city's start_pool to use the new custom .nbt files,
with proper jigsaw element chaining for multi-piece structures.
"""
import os
import json
import glob

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
TEMPLATE_POOL_DIR = os.path.join(BASE, "worldgen", "template_pool")

# Mapping: location_key -> list of (nbt_path, weight) for the start_pool
# Each nbt_path is relative to data/ergenverse/structure/ (no .nbt extension)
CANON_STRUCTURE_MAP = {
    # ── Heng Yue Sect (恒岳宗) — 3 pieces: gate → main_hall → herb_garden ──
    "heng_yue_sect": [
        ("ergenverse:heng_yue_sect/main_hall", 5),
        ("ergenverse:heng_yue_sect/outer_gate", 3),
        ("ergenverse:heng_yue_sect/herb_garden", 2),
    ],
    "heng_yue_sect_main_hall": [
        ("ergenverse:heng_yue_sect/main_hall", 3),
    ],
    "heng_yue_sect_outer_gate": [
        ("ergenverse:heng_yue_sect/outer_gate", 3),
    ],
    "heng_yue_sect_herb_garden": [
        ("ergenverse:heng_yue_sect/herb_garden", 3),
    ],
    "heng_yue_sect_disciple_dormitories": [
        ("ergenverse:heng_yue_sect/herb_garden", 2),  # reuse garden as dorm proxy
        ("ergenverse:tian_shui_city/merchant_house", 1),
    ],

    # ── Tian Shui City (天水城) ──
    "tian_shui_city": [
        ("ergenverse:tian_shui_city/city_gate", 4),
        ("ergenverse:tian_shui_city/market_stall", 3),
        ("ergenverse:tian_shui_city/merchant_house", 2),
    ],
    "tian_shui_city_city_gate": [
        ("ergenverse:tian_shui_city/city_gate", 3),
    ],
    "tian_shui_city_market_district": [
        ("ergenverse:tian_shui_city/market_stall", 5),
        ("ergenverse:tian_shui_city/merchant_house", 2),
    ],
    "tian_shui_city_merchant_quarter": [
        ("ergenverse:tian_shui_city/merchant_house", 5),
    ],
    "tian_shui_city_governor_mansion": [
        ("ergenverse:teng_family_city/keep", 2),  # fortress-like mansion
        ("ergenverse:tian_shui_city/merchant_house", 1),
    ],

    # ── Teng Family City (腾家城) ──
    "teng_family_city": [
        ("ergenverse:teng_family_city/keep", 5),
    ],
    "teng_family_city_keep": [
        ("ergenverse:teng_family_city/keep", 3),
    ],
    "teng_family_city_inner_keep": [
        ("ergenverse:teng_family_city/keep", 3),
    ],
    "teng_family_city_outer_wall": [
        ("ergenverse:tian_shui_city/city_gate", 2),  # wall segments
    ],
    "teng_family_city_family_altar": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 2),  # altar-like
    ],

    # ── Soul Refining Sect (炼魂宗) ──
    "soul_refining_sect": [
        ("ergenverse:soul_refining_sect/furnace_hall", 5),
    ],
    "soul_refining_sect_furnace_hall": [
        ("ergenverse:soul_refining_sect/furnace_hall", 3),
    ],
    "soul_refining_sect_soul_pit": [
        ("ergenverse:soul_refining_sect/furnace_hall", 2),
    ],
    "soul_refining_sect_refining_chamber": [
        ("ergenverse:soul_refining_sect/furnace_hall", 3),
    ],

    # ── Corpse Yin Sect (尸阴宗) ──
    "corpse_yin_sect": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 5),
    ],
    "corpse_yin_sect_ancestor_hall": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 3),
    ],
    "corpse_yin_sect_corpse_pit": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 2),
    ],
    "corpse_yin_sect_yin_array": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 2),
    ],

    # ── Heavenly Fate Sect (天运宗) ──
    "heavenly_fate_sect": [
        ("ergenverse:heavenly_fate_sect/star_tower", 5),
    ],
    "heavenly_fate_sect_star_tower": [
        ("ergenverse:heavenly_fate_sect/star_tower", 3),
    ],
    "heavenly_fate_sect_fate_hall": [
        ("ergenverse:heavenly_fate_sect/star_tower", 2),
        ("ergenverse:heng_yue_sect/main_hall", 1),
    ],
    "heavenly_fate_sect_star_observatory": [
        ("ergenverse:heavenly_fate_sect/star_tower", 3),
    ],

    # ── Wandering Cultivator Camp ──
    "wandering_cultivator_camp": [
        ("ergenverse:wandering_camp/tent", 5),
        ("ergenverse:tian_shui_city/market_stall", 1),
    ],

    # ── Cloud Sky Sect (凌霄宗) — cloud/celestial theme ──
    "cloud_sky_sect": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 5),
    ],
    "cloud_sky_sect_main_plaza": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 3),
    ],
    "cloud_sky_sect_outer_gate": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 2),
        ("ergenverse:heng_yue_sect/outer_gate", 1),
    ],
    "cloud_sky_sect_sword_peak": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 2),
        ("ergenverse:heavenly_fate_sect/star_tower", 1),
    ],
    "cloud_sky_sect_spirit_herb_garden": [
        ("ergenverse:heng_yue_sect/herb_garden", 3),
    ],
    "cloud_sky_sect_library": [
        ("ergenverse:heavenly_fate_sect/star_tower", 2),
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 1),
    ],
    "cloud_sky_sect_inner_sect": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 3),
    ],
    "cloud_sky_sect_hidden_treasury": [
        ("ergenverse:immortal_cave/dwelling", 3),
    ],

    # ── Ancient Demon City (古魔城) ──
    "ancient_demon_city": [
        ("ergenverse:ancient_demon_city/demon_gate", 5),
    ],
    "ancient_demon_city_city_gate": [
        ("ergenverse:ancient_demon_city/demon_gate", 4),
        ("ergenverse:tian_shui_city/city_gate", 1),
    ],
    "ancient_demon_city_temple_district": [
        ("ergenverse:corpse_yin_sect/ancestor_hall", 3),
        ("ergenverse:soul_refining_sect/furnace_hall", 2),
    ],
    "ancient_demon_city_governor_mansion": [
        ("ergenverse:teng_family_city/keep", 3),
    ],
    "ancient_demon_city_market_district": [
        ("ergenverse:tian_shui_city/market_stall", 4),
    ],
    "ancient_demon_city_smuggler_tunnels": [
        ("ergenverse:immortal_cave/dwelling", 3),
    ],
    "ancient_demon_city_warehouse_district": [
        ("ergenverse:tian_shui_city/merchant_house", 2),
    ],

    # ── Luo He Sect, Fighting Evil Sect, Xuan Dao Sect — reuse existing ──
    "luo_he_sect": [
        ("ergenverse:heng_yue_sect/main_hall", 3),
        ("ergenverse:heng_yue_sect/herb_garden", 1),
    ],
    "fighting_evil_sect": [
        ("ergenverse:soul_refining_sect/furnace_hall", 3),
        ("ergenverse:teng_family_city/keep", 1),
    ],
    "xuan_dao_sect": [
        ("ergenverse:cloud_sky_sect/cloud_pavilion", 3),
        ("ergenverse:heavenly_fate_sect/star_tower", 1),
    ],

    # ── Nan Dou City, Qilin City, Great Wang Capital — reuse city pieces ──
    "nan_dou_city": [
        ("ergenverse:tian_shui_city/city_gate", 3),
        ("ergenverse:tian_shui_city/market_stall", 2),
    ],
    "qilin_city": [
        ("ergenverse:tian_shui_city/city_gate", 3),
        ("ergenverse:teng_family_city/keep", 1),
    ],
    "great_wang_capital": [
        ("ergenverse:teng_family_city/keep", 3),
        ("ergenverse:tian_shui_city/merchant_house", 2),
    ],

    # ── Immortal Cave (wandering cultivator dwellings) ──
    "ancient_god_cave": [
        ("ergenverse:immortal_cave/dwelling", 5),
    ],
}

# ── Pattern-based pool mapping for city sub-districts ──
# Maps the suffix of a pool name to a list of (nbt_path, weight) entries.
# This covers all 11 sub-districts of each city (city_gate, market_district, etc.)
CITY_PATTERN_MAP = {
    "city_gate": [("ergenverse:tian_shui_city/city_gate", 4), ("ergenverse:ancient_demon_city/demon_gate", 1)],
    "governor_mansion": [("ergenverse:teng_family_city/keep", 3), ("ergenverse:tian_shui_city/merchant_house", 1)],
    "market_district": [("ergenverse:tian_shui_city/market_stall", 5), ("ergenverse:tian_shui_city/merchant_house", 1)],
    "temple_district": [("ergenverse:corpse_yin_sect/ancestor_hall", 2), ("ergenverse:soul_refining_sect/furnace_hall", 2), ("ergenverse:heavenly_fate_sect/star_tower", 1)],
    "cultivator_quarter": [("ergenverse:tian_shui_city/merchant_house", 3), ("ergenverse:immortal_cave/dwelling", 1)],
    "residential_district": [("ergenverse:tian_shui_city/merchant_house", 4)],
    "mortal_quarter": [("ergenverse:tian_shui_city/market_stall", 3), ("ergenverse:tian_shui_city/merchant_house", 2)],
    "tavern_district": [("ergenverse:tian_shui_city/market_stall", 4), ("ergenverse:wandering_camp/tent", 2)],
    "warehouse_district": [("ergenverse:tian_shui_city/merchant_house", 3)],
    "port_docks": [("ergenverse:tian_shui_city/market_stall", 3), ("ergenverse:wandering_camp/tent", 1)],
    "smuggler_tunnels": [("ergenverse:immortal_cave/dwelling", 4)],
}

# Cities that should use the demon_gate variant for city_gate (demonic cities)
DEMON_CITIES = {"ancient_demon_city"}
# Cities that should use keep-heavy mapping (fortress cities)
FORTRESS_CITIES = {"great_wang_capital", "qilin_city", "teng_family_city"}

# Structures that should use blank/empty (just terrain) — leave village fallback
SKIP_KEYS = set()

def make_pool_element(nbt_location, weight, pool_name):
    """Create a single_pool_element referencing a custom .nbt."""
    return {
        "weight": weight,
        "element": {
            "element_type": "minecraft:single_pool_element",
            "location": nbt_location,
            "projection": "rigid",
            "processors": "ergenverse:structure_degradation"
        }
    }

def make_pool_element_legacy(nbt_location, weight):
    """Create a legacy_single_pool_element (for village-style jigsaw)."""
    return {
        "weight": weight,
        "element": {
            "element_type": "minecraft:legacy_single_pool_element",
            "location": nbt_location,
            "processors": "minecraft:empty"
        }
    }

def update_pool(filepath, canon_entries, pool_name):
    """Rewrite a template_pool JSON to use custom .nbt structures."""
    elements = []
    for (nbt_loc, weight) in canon_entries:
        elements.append(make_pool_element(nbt_loc, weight, pool_name))

    # Read original to preserve name/fallback
    try:
        with open(filepath) as f:
            original = json.load(f)
        fallback = original.get("fallback", "minecraft:village/plains/terminators")
        original_name = original.get("name", pool_name)
    except Exception:
        fallback = "minecraft:village/plains/terminators"
        original_name = pool_name

    new_pool = {
        "name": original_name,
        "fallback": fallback,
        "elements": elements
    }

    with open(filepath, "w") as f:
        json.dump(new_pool, f, indent=2)
        f.write("\n")

def main():
    print("=" * 70)
    print("Updating template_pools to use custom .nbt structures")
    print("=" * 70)

    updated = 0
    skipped = 0
    pattern_matched = 0

    # Walk all template_pool directories
    for pool_dir in sorted(os.listdir(TEMPLATE_POOL_DIR)):
        pool_path = os.path.join(TEMPLATE_POOL_DIR, pool_dir)
        if not os.path.isdir(pool_path):
            continue

        # Check if this directory matches a canon key (exact match)
        canon_entries = CANON_STRUCTURE_MAP.get(pool_dir)

        # If no exact match, try pattern-based matching for city sub-districts
        if not canon_entries:
            for suffix, pattern_entries in CITY_PATTERN_MAP.items():
                if pool_dir.endswith("_" + suffix):
                    # Special handling for demon cities (use demon_gate)
                    city_prefix = pool_dir[:-(len(suffix) + 1)]
                    if suffix == "city_gate" and city_prefix in DEMON_CITIES:
                        canon_entries = [("ergenverse:ancient_demon_city/demon_gate", 5)]
                    elif suffix == "governor_mansion" and city_prefix in FORTRESS_CITIES:
                        canon_entries = [("ergenverse:teng_family_city/keep", 5)]
                    elif suffix == "city_gate" and city_prefix in FORTRESS_CITIES:
                        canon_entries = [("ergenverse:teng_family_city/keep", 2),
                                         ("ergenverse:tian_shui_city/city_gate", 3)]
                    else:
                        canon_entries = pattern_entries
                    break

        if not canon_entries:
            skipped += 1
            continue

        # Update start_pool.json in this directory
        start_pool = os.path.join(pool_path, "start_pool.json")
        if os.path.exists(start_pool):
            update_pool(start_pool, canon_entries, f"ergenverse:{pool_dir}/start_pool")
            source = "canon" if pool_dir in CANON_STRUCTURE_MAP else "pattern"
            print(f"  ✓ {pool_dir}/start_pool.json [{source}] → {len(canon_entries)} pieces")
            updated += 1
            if source == "pattern":
                pattern_matched += 1

    # Also check top-level pool files (not in subdirs)
    for pool_file in sorted(glob.glob(os.path.join(TEMPLATE_POOL_DIR, "*.json"))):
        pool_name = os.path.splitext(os.path.basename(pool_file))[0]
        canon_entries = CANON_STRUCTURE_MAP.get(pool_name)
        if canon_entries:
            update_pool(pool_file, canon_entries, f"ergenverse:{pool_name}")
            print(f"  ✓ {pool_name}.json → {len(canon_entries)} custom pieces")
            updated += 1

    print(f"\n{'=' * 70}")
    print(f"Updated: {updated} template pools ({pattern_matched} via pattern matching)")
    print(f"Skipped (no canon mapping): {skipped} directories")
    print(f"{'=' * 70}")

    # Also create a degradation processor for structures (mossy/cracked variation)
    create_degradation_processor()

def create_degradation_processor():
    """Create a processor that adds weathering to structures (canon: ancient ruins)."""
    proc_dir = os.path.join(BASE, "processor")
    os.makedirs(proc_dir, exist_ok=True)
    proc_path = os.path.join(proc_dir, "structure_degradation.json")

    processor = {
        "processor_type": "minecraft:rule",
        "rules": [
            # 10% chance: stone_bricks → mossy_stone_bricks (ancient weathering)
            {
                "position_predicate": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:stone_bricks", "probability": 0.08},
                "input_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:stone_bricks"},
                "location_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:mossy_stone_bricks"}
            },
            # 5% chance: stone_bricks → cracked_stone_bricks
            {
                "position_predicate": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:stone_bricks", "probability": 0.05},
                "input_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:stone_bricks"},
                "location_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:cracked_stone_bricks"}
            },
            # 8% chance: cobblestone → mossy_cobblestone
            {
                "position_predicate": {"predicate_type": "minecraft:random_block_match", "block": "minecraft:cobblestone", "probability": 0.10},
                "input_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:cobblestone"},
                "location_predicate": {"predicate_type": "minecraft:block_match", "block": "minecraft:mossy_cobblestone"}
            },
        ]
    }

    with open(proc_path, "w") as f:
        json.dump(processor, f, indent=2)
        f.write("\n")
    print(f"\n  ✓ processor/structure_degradation.json (weathering rules)")

if __name__ == "__main__":
    main()
