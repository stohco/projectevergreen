#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-96 verification script.

Verifies that all 7 bead-progression essences are now distributed in
canon-appropriate loot tables, closing the CRON-95 playability gap.

Checks:
  1. All 7 essences appear in at least 5 loot tables (accessibility)
  2. Each essence appears in canon-appropriate locations (thematic match)
  3. No essence appears in inappropriate locations (anti-canonical)
  4. Element essences have weight 3 (rare but findable)
  5. Dao items have weight 1 (very rare — end-game)
  6. All loot table JSONs are valid
  7. dao_fragment (pre-existing) is still in its 74 loot tables (no regression)
"""

import json
import os
import sys
from pathlib import Path

LOOT_DIR = Path("/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/loot_tables/chests")

PASS = 0
FAIL = 0
FAILS = []


def check(label, condition, detail=""):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  [PASS] {label}")
    else:
        FAIL += 1
        FAILS.append(f"{label}: {detail}")
        print(f"  [FAIL] {label}  {detail}")


def count_essence_in_loot(essence_name):
    """Count how many loot tables contain the given essence."""
    count = 0
    files = []
    for filepath in LOOT_DIR.glob("*.json"):
        try:
            with open(filepath, 'r') as f:
                content = f.read()
            if f'ergenverse:{essence_name}' in content:
                count += 1
                files.append(filepath.name)
        except:
            pass
    return count, files


def get_essence_weight(filepath, essence_name):
    """Get the weight of an essence in a specific loot table. Returns None if not found."""
    try:
        with open(filepath, 'r') as f:
            data = json.load(f)
        for pool in data.get("pools", []):
            for entry in pool.get("entries", []):
                if entry.get("name") == f"ergenverse:{essence_name}":
                    return entry.get("weight", 1)
    except:
        pass
    return None


print("=" * 70)
print("CRON-COMPLETIONIST-96: Essence Distribution Verification")
print("=" * 70)

# ── Section 1: All 7 essences are distributed ─────────────────────────
print("\n── Section 1: All 7 essences are distributed ──")

essences = [
    ("metal_essence", "金源精魄", "METAL"),
    ("wood_essence", "木源精魄", "WOOD"),
    ("water_essence", "水源精魄", "WATER"),
    ("fire_essence", "火源精魄", "FIRE"),
    ("earth_essence", "土源精魄", "EARTH"),
    ("dao_karma", "业力之道", "DEEP_MYSTERY_2"),
    ("dao_life_death", "生死之道", "DEEP_MYSTERY_3"),
]

essence_files = {}
for name, cn, part in essences:
    count, files = count_essence_in_loot(name)
    essence_files[name] = files
    check(f"{name} ({cn}, Part {part}) in >=5 loot tables",
          count >= 5, f"only {count} tables")

# ── Section 2: Canon-appropriate locations ────────────────────────────
print("\n── Section 2: Canon-appropriate locations ──")

# metal_essence should be in sword_peak, sword_tomb, puppet_workshop, mountain_cave, hidden_treasury
metal_files = essence_files["metal_essence"]
check("metal_essence in sword_peak locations",
      any("sword_peak" in f for f in metal_files))
check("metal_essence in sword_tomb locations",
      any("sword_tomb" in f for f in metal_files))
check("metal_essence in puppet_workshop locations",
      any("puppet_workshop" in f for f in metal_files))
check("metal_essence in mountain_cave locations",
      any("mountain_cave" in f for f in metal_files))
check("metal_essence in hidden_treasury locations",
      any("hidden_treasury" in f for f in metal_files))

# wood_essence should be in spirit_herb_garden, alchemy_courtyard, underground_passage, ancestor_hall
wood_files = essence_files["wood_essence"]
check("wood_essence in spirit_herb_garden locations",
      any("spirit_herb_garden" in f for f in wood_files))
check("wood_essence in alchemy_courtyard locations",
      any("alchemy_courtyard" in f for f in wood_files))
check("wood_essence in underground_passage locations",
      any("underground_passage" in f for f in wood_files))
check("wood_essence in ancestor_hall locations",
      any("ancestor_hall" in f for f in wood_files))

# water_essence should be in spirit_spring, port_docks, spirit_beast_pens
water_files = essence_files["water_essence"]
check("water_essence in spirit_spring locations",
      any("spirit_spring" in f for f in water_files))
check("water_essence in port_docks locations",
      any("port_docks" in f for f in water_files))
check("water_essence in spirit_beast_pens locations",
      any("spirit_beast_pens" in f for f in water_files))

# fire_essence should be in alchemy_courtyard, core_formation_hall, trial_grounds, fighting_evil_sect_
fire_files = essence_files["fire_essence"]
check("fire_essence in alchemy_courtyard locations",
      any("alchemy_courtyard" in f for f in fire_files))
check("fire_essence in core_formation_hall locations",
      any("core_formation_hall" in f for f in fire_files))
check("fire_essence in trial_grounds locations",
      any("trial_grounds" in f for f in fire_files))
check("fire_essence in fighting_evil_sect locations",
      any("fighting_evil_sect_" in f for f in fire_files))

# earth_essence should be in mountain_cave, underground_passage, hidden_treasury, corpse_yin_sect_
earth_files = essence_files["earth_essence"]
check("earth_essence in mountain_cave locations",
      any("mountain_cave" in f for f in earth_files))
check("earth_essence in underground_passage locations",
      any("underground_passage" in f for f in earth_files))
check("earth_essence in hidden_treasury locations",
      any("hidden_treasury" in f for f in earth_files))
check("earth_essence in corpse_yin_sect locations",
      any("corpse_yin_sect_" in f for f in earth_files))

# dao_karma should be in karma_crystal_formation, ancestor_hall, hidden_treasury, heavenly_fate_star_tower
karma_files = essence_files["dao_karma"]
check("dao_karma in karma_crystal_formation",
      any("karma_crystal_formation" in f for f in karma_files))
check("dao_karma in ancestor_hall locations",
      any("ancestor_hall" in f for f in karma_files))
check("dao_karma in hidden_treasury locations",
      any("hidden_treasury" in f for f in karma_files))
check("dao_karma in heavenly_fate_star_tower",
      any("heavenly_fate_star_tower" in f for f in karma_files))

# dao_life_death should be in immortal_emperor_cave_mansion, ancient_god_cave, soul_refining_furnace, thunder_celestial_temple, sword_tomb
life_death_files = essence_files["dao_life_death"]
check("dao_life_death in immortal_emperor_cave_mansion",
      any("immortal_emperor_cave_mansion" in f for f in life_death_files))
check("dao_life_death in ancient_god_cave",
      any("ancient_god_cave" in f for f in life_death_files))
check("dao_life_death in soul_refining_furnace",
      any("soul_refining_furnace" in f for f in life_death_files))
check("dao_life_death in thunder_celestial_temple",
      any("thunder_celestial_temple" in f for f in life_death_files))
check("dao_life_death in sword_tomb locations",
      any("sword_tomb" in f for f in life_death_files))

# ── Section 3: Weights are correct ────────────────────────────────────
print("\n── Section 3: Weights are correct (element=3, dao=1) ──")

# Check a sample element essence for weight 3
sample_metal_file = None
for f in metal_files:
    if "sword_peak" in f:
        sample_metal_file = LOOT_DIR / f
        break
if sample_metal_file:
    w = get_essence_weight(sample_metal_file, "metal_essence")
    check("metal_essence has weight 3 (element tier)",
          w == 3, f"got weight {w}")

# Check a sample dao item for weight 1
sample_karma_file = None
for f in karma_files:
    if "karma_crystal_formation" in f:
        sample_karma_file = LOOT_DIR / f
        break
if sample_karma_file:
    w = get_essence_weight(sample_karma_file, "dao_karma")
    check("dao_karma has weight 1 (dao tier)",
          w == 1, f"got weight {w}")

# Check dao_life_death for weight 1
sample_ld_file = None
for f in life_death_files:
    if "immortal_emperor" in f or "ancient_god" in f:
        sample_ld_file = LOOT_DIR / f
        break
if sample_ld_file:
    w = get_essence_weight(sample_ld_file, "dao_life_death")
    check("dao_life_death has weight 1 (dao tier)",
          w == 1, f"got weight {w}")

# ── Section 4: No regression on dao_fragment ──────────────────────────
print("\n── Section 4: No regression on dao_fragment (pre-existing) ──")
df_count, _ = count_essence_in_loot("dao_fragment")
check("dao_fragment still in >=74 loot tables (no regression)",
      df_count >= 74, f"only {df_count} tables")

# ── Section 5: All JSON files are valid ───────────────────────────────
print("\n── Section 5: All loot table JSONs are valid ──")
json_errors = 0
total_files = 0
for filepath in LOOT_DIR.glob("*.json"):
    total_files += 1
    try:
        with open(filepath, 'r') as f:
            json.load(f)
    except json.JSONDecodeError as e:
        json_errors += 1
        print(f"  [FAIL] Invalid JSON: {filepath.name}: {e}")
check(f"All {total_files} loot table JSONs are valid",
      json_errors == 0, f"{json_errors} invalid JSONs")

# ── Section 6: Anti-canonical checks (essences NOT in wrong places) ──
print("\n── Section 6: Anti-canonical placement checks ──")

# Element essences should NOT be in end-game locations only
# (they should be in early/mid-game locations too)
check("metal_essence in at least one Heng Yue Sect location (early game)",
      any("heng_yue_sect" in f for f in metal_files),
      "Heng Yue Sect is Wang Lin's first sect — metal should be findable there")

check("wood_essence in at least one Heng Yue Sect location (early game)",
      any("heng_yue_sect" in f for f in wood_files),
      "Heng Yue Sect has a spirit herb garden — wood should be findable there")

# Dao items should NOT be in early-game/wang_family_village locations (too rare)
check("dao_life_death NOT in wang_family_village (too early-game)",
      not any("wang_family_village" in f for f in life_death_files),
      "dao_life_death is end-game only; Wang Family Village is the starting area")

check("dao_karma NOT in wang_family_village (too early-game)",
      not any("wang_family_village" in f for f in karma_files),
      "dao_karma is high-tier; Wang Family Village is the starting area")

# ── Summary ──────────────────────────────────────────────────────────
print("\n" + "=" * 70)
print(f"RESULT: {PASS} passed, {FAIL} failed")
print("=" * 70)

if FAILS:
    print("\nFailures:")
    for f in FAILS:
        print(f"  - {f}")
sys.exit(0 if FAIL == 0 else 1)
