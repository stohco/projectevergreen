#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-96: Distribute the 7 missing bead-progression essences
across canon-appropriate loot tables.

CONTEXT (from CRON-95):
  The Heaven-Defying Bead's active progression requires the player to absorb
  8 essences (5 Elements + 3 Dao) to align the 9 Parts. CRON-95 shipped the
  absorption mechanic but the essences were NOT distributed in any loot
  tables — making the bead progression UNREACHABLE in survival mode.

  dao_fragment was already in 74 loot tables (maps to DEEP_MYSTERY_1).
  The other 7 essences (metal/wood/water/fire/earth_essence + dao_karma +
  dao_life_death) were in ZERO loot tables.

CANON-APPROPRIATE DISTRIBUTION:
  Each essence goes to locations that thematically match its attribute:

  metal_essence  (金源精魄) → sword_peak, sword_tomb, puppet_workshop,
                               mountain_cave, hidden_treasury
  wood_essence   (木源精魄) → spirit_herb_garden, alchemy_courtyard,
                               underground_passage, ancestor_hall
  water_essence  (水源精魄) → spirit_spring, port_docks, spirit_beast_pens
  fire_essence   (火源精魄) → alchemy_courtyard, core_formation_hall,
                               trial_grounds, fighting_evil_sect_*
  earth_essence  (土源精魄) → mountain_cave, underground_passage,
                               hidden_treasury, corpse_yin_sect_*
  dao_karma      (业力之道) → karma_crystal_formation, ancestor_hall,
                               hidden_treasury, heavenly_fate_star_tower,
                               immortal_emperor_cave_mansion (HIGH-TIER)
  dao_life_death (生死之道) → immortal_emperor_cave_mansion, ancient_god_cave,
                               soul_refining_furnace, thunder_celestial_temple,
                               sword_tomb (HIGHEST-TIER)

WEIGHTS:
  Element essences: weight 3, count 1-2 (rare but findable)
  Dao items:        weight 1, count 1   (very rare — end-game items)

IDEMPOTENT: skips loot tables that already contain the essence.

Run: python3 /home/z/my-project/scripts/cron96_distribute_essences.py
"""

import json
import os
import re
import sys
from pathlib import Path

LOOT_DIR = Path("/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/loot_tables/chests")

# ── Essence distribution rules ────────────────────────────────────────
# Each rule: (essence_id, list of filename substrings to match, weight, count_min, count_max, tier_label)

ESSENCE_RULES = [
    # ── Five Elements (canon: Wang Lin aligns the Five Elements) ──
    {
        "essence": "ergenverse:metal_essence",
        "name_cn": "金源精魄",
        "part": "METAL",
        "weight": 3,
        "count_min": 1,
        "count_max": 2,
        "tier": "ELEMENT",
        # Metal-attribute locations: swords (metal), puppets (metal constructs),
        # mountain caves (ore deposits), hidden treasuries (precious metals)
        "matches": ["sword_peak", "sword_tomb", "puppet_workshop",
                    "mountain_cave", "hidden_treasury"],
    },
    {
        "essence": "ergenverse:wood_essence",
        "name_cn": "木源精魄",
        "part": "WOOD",
        "weight": 3,
        "count_min": 1,
        "count_max": 2,
        "tier": "ELEMENT",
        # Wood-attribute locations: herb gardens (living plants),
        # alchemy courtyards (herb processing), underground passages (ancient roots),
        # ancestor halls (old wooden beams)
        "matches": ["spirit_herb_garden", "alchemy_courtyard",
                    "underground_passage", "ancestor_hall"],
    },
    {
        "essence": "ergenverse:water_essence",
        "name_cn": "水源精魄",
        "part": "WATER",
        "weight": 3,
        "count_min": 1,
        "count_max": 2,
        "tier": "ELEMENT",
        # Water-attribute locations: spirit springs (water sources),
        # port docks (water-adjacent), spirit beast pens (watering holes)
        "matches": ["spirit_spring", "port_docks", "spirit_beast_pens"],
    },
    {
        "essence": "ergenverse:fire_essence",
        "name_cn": "火源精魄",
        "part": "FIRE",
        "weight": 3,
        "count_min": 1,
        "count_max": 2,
        "tier": "ELEMENT",
        # Fire-attribute locations: alchemy courtyards (furnaces),
        # core formation halls (fire tribulation), trial grounds (fire trials),
        # Fighting Evil Sect (fire-attribute sect in canon)
        "matches": ["alchemy_courtyard", "core_formation_hall",
                    "trial_grounds", "fighting_evil_sect_"],
    },
    {
        "essence": "ergenverse:earth_essence",
        "name_cn": "土源精魄",
        "part": "EARTH",
        "weight": 3,
        "count_min": 1,
        "count_max": 2,
        "tier": "ELEMENT",
        # Earth-attribute locations: mountain caves (earth),
        # underground passages (earth), hidden treasuries (underground),
        # Corpse Yin Sect (earth/burial attribute — canon: they work with corpses)
        "matches": ["mountain_cave", "underground_passage",
                    "hidden_treasury", "corpse_yin_sect_"],
    },
    # ── Three Hidden Fragments (mod-original — see CRON-95 javadoc) ──
    {
        "essence": "ergenverse:dao_karma",
        "name_cn": "业力之道",
        "part": "DEEP_MYSTERY_2",
        "weight": 1,
        "count_min": 1,
        "count_max": 1,
        "tier": "DAO",
        # Karmic locations: karma crystal formation (directly karmic),
        # ancestor halls (karmic connection to ancestors),
        # hidden treasuries (high-tier), heavenly fate star tower (fate/karma),
        # immortal emperor cave mansion (end-game karmic)
        "matches": ["karma_crystal_formation", "ancestor_hall",
                    "hidden_treasury", "heavenly_fate_star_tower",
                    "immortal_emperor_cave_mansion"],
    },
    {
        "essence": "ergenverse:dao_life_death",
        "name_cn": "生死之道",
        "part": "DEEP_MYSTERY_3",
        "weight": 1,
        "count_min": 1,
        "count_max": 1,
        "tier": "DAO",
        # Life/Death locations (HIGHEST-TIER): immortal emperor cave mansion (end-game),
        # ancient god cave (end-game), soul refining furnace (life/death of souls),
        # thunder celestial temple (tribulation = life/death trial),
        # sword tombs (death)
        "matches": ["immortal_emperor_cave_mansion", "ancient_god_cave",
                    "soul_refining_furnace", "thunder_celestial_temple",
                    "sword_tomb"],
    },
]


def matches_loot_table(filename, match_substrings):
    """Check if a loot table filename matches any of the canon-appropriate substrings."""
    for sub in match_substrings:
        if sub in filename:
            return True
    return False


def essence_already_in_table(loot_data, essence_id):
    """Check if the essence is already in any pool of this loot table."""
    for pool in loot_data.get("pools", []):
        for entry in pool.get("entries", []):
            if entry.get("name") == essence_id:
                return True
    return False


def make_essence_entry(essence_id, weight, count_min, count_max):
    """Create a loot table entry for an essence item."""
    entry = {
        "type": "minecraft:item",
        "name": essence_id,
        "weight": weight,
    }
    if count_min != 1 or count_max != 1:
        entry["functions"] = [
            {
                "function": "minecraft:set_count",
                "count": {"min": count_min, "max": count_max}
            }
        ]
    return entry


def process_loot_table(filepath, stats):
    """Process a single loot table JSON file. Add appropriate essences."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            loot_data = json.load(f)
    except (json.JSONDecodeError, IOError) as e:
        stats["errors"].append(f"{filepath.name}: {e}")
        return

    filename = filepath.name
    modified = False
    essences_added = []

    for rule in ESSENCE_RULES:
        if not matches_loot_table(filename, rule["matches"]):
            continue
        if essence_already_in_table(loot_data, rule["essence"]):
            stats["skipped_duplicate"] += 1
            continue

        # Add to the first pool (all these loot tables have at least one pool)
        if not loot_data.get("pools"):
            stats["errors"].append(f"{filename}: no pools found")
            continue

        entry = make_essence_entry(
            rule["essence"], rule["weight"],
            rule["count_min"], rule["count_max"]
        )
        loot_data["pools"][0]["entries"].append(entry)
        essences_added.append(rule)
        modified = True

    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            json.dump(loot_data, f, indent=2, ensure_ascii=False)
            f.write("\n")  # trailing newline
        stats["files_modified"] += 1
        for r in essences_added:
            tier_label = r["tier"]
            stats[f"added_{tier_label}"] += 1
            stats["details"].append(
                f"  {filename}: +{r['essence'].split(':')[1]} "
                f"({r['name_cn']}, Part {r['part']}, w={r['weight']})"
            )
    else:
        stats["files_unchanged"] += 1


def main():
    if not LOOT_DIR.exists():
        print(f"ERROR: loot table directory not found: {LOOT_DIR}")
        sys.exit(1)

    stats = {
        "files_scanned": 0,
        "files_modified": 0,
        "files_unchanged": 0,
        "skipped_duplicate": 0,
        "added_ELEMENT": 0,
        "added_DAO": 0,
        "errors": [],
        "details": [],
    }

    print("=" * 70)
    print("CRON-COMPLETIONIST-96: Distribute bead-progression essences")
    print("  to canon-appropriate loot tables")
    print("=" * 70)

    # Process all JSON files in the chests/ directory
    for filepath in sorted(LOOT_DIR.glob("*.json")):
        stats["files_scanned"] += 1
        process_loot_table(filepath, stats)

    # Print summary
    print(f"\n── Summary ──")
    print(f"  Files scanned:      {stats['files_scanned']}")
    print(f"  Files modified:     {stats['files_modified']}")
    print(f"  Files unchanged:    {stats['files_unchanged']}")
    print(f"  Duplicates skipped: {stats['skipped_duplicate']}")
    print(f"  Element essences added: {stats['added_ELEMENT']}")
    print(f"  Dao items added:        {stats['added_DAO']}")

    if stats["errors"]:
        print(f"\n  ERRORS ({len(stats['errors'])}):")
        for e in stats["errors"]:
            print(f"    {e}")

    if stats["details"]:
        print(f"\n── Details ({len(stats['details'])} additions) ──")
        for d in stats["details"]:
            print(d)

    # Verification: count essences in loot tables after the run
    print(f"\n── Post-run verification ──")
    for essence_id in [r["essence"] for r in ESSENCE_RULES]:
        essence_name = essence_id.split(":")[1]
        count = 0
        for filepath in LOOT_DIR.glob("*.json"):
            try:
                with open(filepath, 'r') as f:
                    content = f.read()
                if essence_id in content:
                    count += 1
            except:
                pass
        status = "OK" if count > 0 else "MISSING"
        print(f"  {essence_name:20s} in {count:3d} loot tables  [{status}]")

    print("\n" + "=" * 70)
    if stats["errors"]:
        print(f"RESULT: COMPLETED WITH {len(stats['errors'])} ERRORS")
        sys.exit(1)
    else:
        print(f"RESULT: SUCCESS — {stats['added_ELEMENT']} element + "
              f"{stats['added_DAO']} dao essences added to "
              f"{stats['files_modified']} loot tables")
        sys.exit(0)


if __name__ == "__main__":
    main()
