#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-105 — SuzakuTombBuilder Verification.

Verifies that the SuzakuTombBuilder exists, is chunk-scoped (following the
WangFamilyVillageBuilder CRON-62 pattern), is registered in
StructureBuilderRegistry, has a loot table JSON that drops World Origin
Essence, and is canon-faithful (朱雀墓, 朱雀子, 修炼星晶, 一界本源 all
attested; NO fabricated chapter citation).

Categories of checks:
  1. SuzakuTombBuilder.java — file exists, class declared, chunk-scoped pattern
  2. Chunk-scoped sb() helper — ThreadLocal CURRENT_BOUNDS, chunk-filter, provenance-guard
  3. buildForChunk / build / isAlreadyBuilt — public API
  4. buildInternal — calls all 5 sub-builders
  5. Canon constants — TOMB_X/Z from blueprint, HALF=10, FLOOR_Y, CEILING_Y
  6. Sub-builders — buildChamberShell, buildPedestalAndCrystal, buildSpiritVeinConduits, buildSealingFormation, placeInheritanceChest
  7. Canon fidelity — 朱雀墓, 朱雀子, 修炼星晶, 一界本源, 拓森, NO fabricated chapter citation
  8. Loot table JSON — exists, valid JSON, drops world_origin_essence, 3 pools
  9. StructureBuilderRegistry — SuzakuTombBuilder imported + registered for SUZAKU_TOMB.id
 10. Integration — PlanetSuzakuBlueprint.SUZAKU_TOMB exists at (0, -60, 0)
 11. CRON-104 interaction — cave suppression protects the tomb (150-block radius)

Run: python3 scripts/cron105_verify_suzaku_tomb_builder.py
Exit code: 0 if all checks pass, 1 otherwise.
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "forge-mod"
BUILDER = ROOT / "src/main/java/dev/ergenverse/spawn/SuzakuTombBuilder.java"
REGISTRY = ROOT / "src/main/java/dev/ergenverse/runtime/materialize/StructureBuilderRegistry.java"
BLUEPRINT = ROOT / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"
LOOT_TABLE = ROOT / "src/main/resources/data/ergenverse/loot_tables/chests/suzaku_tomb_inheritance_chamber.json"
CHUNK_GEN = ROOT / "src/main/java/dev/ergenverse/runtime/worldgen/BlueprintChunkGenerator.java"

PASS = 0
FAIL = 0
FAILS = []


def check(cond: bool, label: str) -> None:
    global PASS, FAIL
    if cond:
        PASS += 1
    else:
        FAIL += 1
        FAILS.append(label)
        print(f"  FAIL: {label}")


def section(name: str) -> None:
    print(f"\n=== {name} ===")


# ─────────────────────────────────────────────────────────────────────────────
# Load sources
# ─────────────────────────────────────────────────────────────────────────────

builder_text = BUILDER.read_text(encoding="utf-8")
registry_text = REGISTRY.read_text(encoding="utf-8")
blueprint_text = BLUEPRINT.read_text(encoding="utf-8")
chunk_gen_text = CHUNK_GEN.read_text(encoding="utf-8")
loot_text = LOOT_TABLE.read_text(encoding="utf-8")

# ─────────────────────────────────────────────────────────────────────────────
# 1. SuzakuTombBuilder.java — file exists, class declared
# ─────────────────────────────────────────────────────────────────────────────
section("1. SuzakuTombBuilder.java — file & class")

check(BUILDER.exists(), "SuzakuTombBuilder.java file exists")
check("package dev.ergenverse.spawn;" in builder_text, "package dev.ergenverse.spawn")
check("public final class SuzakuTombBuilder" in builder_text, "public final class SuzakuTombBuilder")
check("private SuzakuTombBuilder() {}" in builder_text, "private constructor (utility class)")
check("CRON-COMPLETIONIST-105" in builder_text, "class javadoc mentions CRON-COMPLETIONIST-105")

# ─────────────────────────────────────────────────────────────────────────────
# 2. Chunk-scoped sb() helper (CRON-62/63 pattern)
# ─────────────────────────────────────────────────────────────────────────────
section("2. Chunk-scoped sb() helper")

check(
    "ThreadLocal<ChunkBounds> CURRENT_BOUNDS" in builder_text,
    "ThreadLocal<ChunkBounds> CURRENT_BOUNDS declared",
)
check(
    re.search(r"private\s+static\s+void\s+sb\(\s*ServerLevel\s+\w+\s*,\s*BlockPos\s+\w+\s*,\s*BlockState\s+\w+\s*,\s*int\s+\w+\s*\)", builder_text)
    is not None,
    "sb() method declared with (ServerLevel, BlockPos, BlockState, int) signature",
)
check(
    "b.contains(pos.getX(), pos.getZ())" in builder_text,
    "sb() applies chunk filter via b.contains(pos.getX(), pos.getZ())",
)
check(
    "hasPlayerOrSimulationDelta(pos)" in builder_text,
    "sb() applies provenance guard via hasPlayerOrSimulationDelta(pos)",
)
check(
    "store.hasBlock(x, y, z, Provenance.PLAYER)" in builder_text,
    "hasPlayerOrSimulationDelta checks Provenance.PLAYER",
)
check(
    "store.hasBlock(x, y, z, Provenance.SIMULATION)" in builder_text,
    "hasPlayerOrSimulationDelta checks Provenance.SIMULATION",
)
check(
    "level.setBlock(pos, state, flags)" in builder_text,
    "sb() calls level.setBlock(pos, state, flags) when guards pass",
)

# ─────────────────────────────────────────────────────────────────────────────
# 3. buildForChunk / build / isAlreadyBuilt — public API
# ─────────────────────────────────────────────────────────────────────────────
section("3. Public API")

check(
    "public static void buildForChunk(ServerLevel level, @Nullable ChunkBounds bounds)" in builder_text,
    "buildForChunk(ServerLevel, @Nullable ChunkBounds) declared",
)
check(
    "@Nullable" in builder_text,
    "@Nullable annotation used",
)
check(
    "public static void build(ServerLevel level)" in builder_text,
    "build(ServerLevel) full-build path declared",
)
check(
    "isAlreadyBuilt(level)" in builder_text,
    "build() calls isAlreadyBuilt(level) guard",
)
check(
    "private static boolean isAlreadyBuilt(ServerLevel level)" in builder_text,
    "isAlreadyBuilt declared as private static boolean",
)
check(
    "Blocks.DIAMOND_BLOCK" in builder_text,
    "isAlreadyBuilt checks for DIAMOND_BLOCK (Crystal placeholder)",
)

# ─────────────────────────────────────────────────────────────────────────────
# 4. buildInternal — calls all 5 sub-builders
# ─────────────────────────────────────────────────────────────────────────────
section("4. buildInternal sub-builders")

check(
    "private static void buildInternal(ServerLevel level)" in builder_text,
    "buildInternal(ServerLevel) declared",
)
check(
    "buildChamberShell(level)" in builder_text,
    "buildInternal calls buildChamberShell",
)
check(
    "buildPedestalAndCrystal(level)" in builder_text,
    "buildInternal calls buildPedestalAndCrystal",
)
check(
    "buildSpiritVeinConduits(level)" in builder_text,
    "buildInternal calls buildSpiritVeinConduits",
)
check(
    "buildSealingFormation(level)" in builder_text,
    "buildInternal calls buildSealingFormation",
)
check(
    "placeInheritanceChest(level)" in builder_text,
    "buildInternal calls placeInheritanceChest",
)

# ─────────────────────────────────────────────────────────────────────────────
# 5. Canon constants
# ─────────────────────────────────────────────────────────────────────────────
section("5. Canon constants")

check(
    "PlanetSuzakuBlueprint.SUZAKU_TOMB.x" in builder_text,
    "TOMB_X sourced from PlanetSuzakuBlueprint.SUZAKU_TOMB.x",
)
check(
    "PlanetSuzakuBlueprint.SUZAKU_TOMB.y" in builder_text,
    "TOMB_Y sourced from PlanetSuzakuBlueprint.SUZAKU_TOMB.y",
)
check(
    "PlanetSuzakuBlueprint.SUZAKU_TOMB.z" in builder_text,
    "TOMB_Z sourced from PlanetSuzakuBlueprint.SUZAKU_TOMB.z",
)
check(
    "HALF = 10" in builder_text,
    "HALF = 10 (20x20 chamber)",
)
check(
    "FLOOR_Y = TOMB_Y - 3" in builder_text,
    "FLOOR_Y = TOMB_Y - 3 (3 blocks below center)",
)
check(
    "CEILING_Y = TOMB_Y + 3" in builder_text,
    "CEILING_Y = TOMB_Y + 3 (3 blocks above center)",
)

# ─────────────────────────────────────────────────────────────────────────────
# 6. Sub-builder details
# ─────────────────────────────────────────────────────────────────────────────
section("6. Sub-builder details")

# Chamber shell
check(
    "Blocks.DEEPSLATE_BRICKS" in builder_text,
    "buildChamberShell uses DEEPSLATE_BRICKS (canon-appropriate for underground)",
)
check(
    "Blocks.DEEPSLATE" in builder_text,
    "buildChamberShell uses DEEPSLATE (floor)",
)
check(
    "Blocks.AIR" in builder_text,
    "buildChamberShell clears interior to AIR",
)

# Pedestal and Crystal
check(
    "ErgenverseBlocks.SPIRIT_STONE_BLOCK" in builder_text,
    "buildPedestalAndCrystal uses SPIRIT_STONE_BLOCK for pedestal",
)
check(
    "Blocks.DIAMOND_BLOCK" in builder_text,
    "buildPedestalAndCrystal uses DIAMOND_BLOCK as Cultivation Planet Crystal placeholder",
)
check(
    "Mod-original placeholder" in builder_text,
    "javadoc honestly flags diamond block as mod-original placeholder",
)

# Spirit vein conduits
check(
    "ErgenverseBlocks.SPIRIT_VEIN_STONE" in builder_text,
    "buildSpiritVeinConduits uses SPIRIT_VEIN_STONE",
)
check(
    "TOMB_X - HALF + 2" in builder_text,
    "buildSpiritVeinConduits places pillars at inset corners",
)

# Sealing formation
check(
    "ErgenverseBlocks.FORMATION_CORE_STONE" in builder_text,
    "buildSealingFormation uses FORMATION_CORE_STONE",
)

# Inheritance chest
check(
    "ChestHelper.placeChestWithLoot" in builder_text,
    "placeInheritanceChest uses ChestHelper.placeChestWithLoot",
)
check(
    "chests/suzaku_tomb_inheritance_chamber" in builder_text,
    "placeInheritanceChest references chests/suzaku_tomb_inheritance_chamber loot table",
)
check(
    "(lvl, p) -> sb(lvl, p, Blocks.CHEST.defaultBlockState(), 2)" in builder_text,
    "placeInheritanceChest passes sb()-delegating placer (CRON-71 pattern)",
)

# ─────────────────────────────────────────────────────────────────────────────
# 7. Canon fidelity
# ─────────────────────────────────────────────────────────────────────────────
section("7. Canon fidelity")

check("朱雀墓" in builder_text, "javadoc mentions 朱雀墓 (Suzaku Tomb)")
check("朱雀子" in builder_text, "javadoc mentions 朱雀子 (Suzaku Son lineage)")
check("修炼星晶" in builder_text, "javadoc mentions 修炼星晶 (Cultivation Planet Crystal)")
check("一界本源" in builder_text, "javadoc mentions 一界本源 (World Origin Essence)")
check("拓森" in builder_text, "javadoc mentions 拓森 (Tuo Sen)")
check(
    "NO fabricated chapter citation" in builder_text,
    "javadoc explicitly states 'NO fabricated chapter citation'",
)
check(
    "Baidu Baike" in builder_text,
    "javadoc references Baidu Baike as web-search source",
)
check(
    "Mod-original placeholder" in builder_text,
    "diamond block Crystal honestly flagged as mod-original placeholder",
)
check(
    "mod-inferred" in builder_text and "acquisition path" in builder_text,
    "World Origin Essence drop honestly flagged as mod-inferred acquisition path",
)

# ─────────────────────────────────────────────────────────────────────────────
# 8. Loot table JSON
# ─────────────────────────────────────────────────────────────────────────────
section("8. Loot table JSON")

check(LOOT_TABLE.exists(), "suzaku_tomb_inheritance_chamber.json loot table exists")

try:
    loot = json.loads(loot_text)
    check(True, "loot table is valid JSON")
except json.JSONDecodeError as e:
    check(False, f"loot table is valid JSON: {e}")
    loot = {}

check(loot.get("type") == "minecraft:chest", "loot table type is minecraft:chest")
pools = loot.get("pools", [])
check(len(pools) == 3, f"loot table has 3 pools (got {len(pools)})")

# Check that World Origin Essence appears in the loot table
all_entries = []
for pool in pools:
    for entry in pool.get("entries", []):
        all_entries.append(entry.get("name", ""))
check(
    "ergenverse:world_origin_essence" in all_entries,
    "loot table drops ergenverse:world_origin_essence",
)

# Check World Origin Essence is rare (weight 1)
woe_weight = None
for pool in pools:
    for entry in pool.get("entries", []):
        if entry.get("name") == "ergenverse:world_origin_essence":
            woe_weight = entry.get("weight")
check(
    woe_weight == 1,
    f"World Origin Essence has weight 1 (very rare) — got weight {woe_weight}",
)

# Check other canon-flavored loot items
check(
    "ergenverse:spirit_stone" in all_entries,
    "loot table drops spirit_stone (common cultivation currency)",
)
check(
    "ergenverse:spirit_stone_high" in all_entries,
    "loot table drops spirit_stone_high (rare high-tier)",
)
check(
    "ergenverse:jade_slip" in all_entries,
    "loot table drops jade_slip (canon: cultivators record techniques on jade slips)",
)
check(
    "ergenverse:dao_fragment" in all_entries,
    "loot table drops dao_fragment (canon inheritance item)",
)

# Check the _comment field for canon honesty
check(
    "CRON-COMPLETIONIST-105" in loot_text,
    "loot table _comment mentions CRON-COMPLETIONIST-105",
)
check(
    "NO fabricated chapter citation" in loot_text,
    "loot table _comment states 'NO fabricated chapter citation'",
)
check(
    "acquisition path" in loot_text and ("mod-inferred" in loot_text or "Mod-original" in loot_text or "mod-original" in loot_text),
    "loot table _comment honestly flags World Origin Essence drop as mod-inferred/mod-original",
)

# ─────────────────────────────────────────────────────────────────────────────
# 9. StructureBuilderRegistry — import + register
# ─────────────────────────────────────────────────────────────────────────────
section("9. StructureBuilderRegistry registration")

check(
    "import dev.ergenverse.spawn.SuzakuTombBuilder;" in registry_text,
    "StructureBuilderRegistry imports SuzakuTombBuilder",
)
check(
    'PlanetSuzakuBlueprint.SUZAKU_TOMB.id' in registry_text,
    "registry references PlanetSuzakuBlueprint.SUZAKU_TOMB.id",
)
check(
    "SuzakuTombBuilder.buildForChunk(l, b)" in registry_text,
    "registry registers SuzakuTombBuilder.buildForChunk",
)
check(
    "CRON-COMPLETIONIST-105" in registry_text,
    "registry javadoc mentions CRON-COMPLETIONIST-105",
)

# ─────────────────────────────────────────────────────────────────────────────
# 10. Integration — PlanetSuzakuBlueprint.SUZAKU_TOMB
# ─────────────────────────────────────────────────────────────────────────────
section("10. PlanetSuzakuBlueprint.SUZAKU_TOMB integration")

check(
    '"suzaku_tomb"' in blueprint_text,
    "PlanetSuzakuBlueprint defines suzaku_tomb location id",
)
check(
    "SUZAKU_TOMB" in blueprint_text,
    "PlanetSuzakuBlueprint defines SUZAKU_TOMB constant",
)
check(
    "朱雀墓" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 朱雀墓",
)
check(
    "朱雀子" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 朱雀子",
)
check(
    "拓森" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 拓森 (Tuo Sen)",
)

# Extract Suzaku Tomb coordinates from blueprint
tomb_match = re.search(
    r'new\s+CanonLocation\(\s*"suzaku_tomb"\s*,\s*"[^"]*"\s*,\s*'
    r'(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*,',
    blueprint_text,
)
if tomb_match:
    tomb_x, tomb_y, tomb_z = int(tomb_match.group(1)), int(tomb_match.group(2)), int(tomb_match.group(3))
    check(tomb_x == 0, f"Suzaku Tomb x=0 (under Vermilion Bird Capital) — got {tomb_x}")
    check(tomb_y == -60, f"Suzaku Tomb y=-60 (underground) — got {tomb_y}")
    check(tomb_z == 0, f"Suzaku Tomb z=0 — got {tomb_z}")
else:
    check(False, "could not extract Suzaku Tomb coordinates")

# Check SUZAKU_TOMB category is "ruin" — the name "Suzaku Tomb (朱雀墓)"
# contains parentheses, so we use a multiline dot-matching approach.
tomb_cat_match = re.search(
    r'new\s+CanonLocation\(\s*"suzaku_tomb".*?"ruin"',
    blueprint_text,
    re.DOTALL,
)
if tomb_cat_match:
    check(True, "Suzaku Tomb category='ruin'")
else:
    # Fallback: check if "ruin" appears on the same line as suzaku_tomb
    # or the line after
    tomb_lines = blueprint_text.split("\n")
    for i, line in enumerate(tomb_lines):
        if '"suzaku_tomb"' in line:
            # Check this line and the next 2 lines for "ruin"
            context = "\n".join(tomb_lines[i:i+3])
            check(
                '"ruin"' in context,
                "Suzaku Tomb category='ruin' (found in context lines)",
            )
            break
    else:
        check(False, "could not find suzaku_tomb in blueprint")

# ─────────────────────────────────────────────────────────────────────────────
# 11. CRON-104 interaction — cave suppression protects the tomb
# ─────────────────────────────────────────────────────────────────────────────
section("11. CRON-104 cave suppression interaction")

check(
    "CAVE_SUPPRESSION_RADIUS_TOMB = 150" in chunk_gen_text,
    "BlueprintChunkGenerator has CAVE_SUPPRESSION_RADIUS_TOMB = 150 (CRON-104)",
)
check(
    '"suzaku_tomb".equals(loc.id)' in chunk_gen_text,
    "BlueprintChunkGenerator.getCaveSuppressionRadius checks suzaku_tomb id (CRON-104)",
)
check(
    "isProtectedCategory" in chunk_gen_text and '"ruin"' in chunk_gen_text,
    "BlueprintChunkGenerator.isProtectedCategory includes 'ruin' category (CRON-104)",
)

# Verify the tomb is in the allLocations() map
check(
    'map.put(SUZAKU_TOMB.id, SUZAKU_TOMB)' in blueprint_text,
    "PlanetSuzakuBlueprint.allLocations() includes SUZAKU_TOMB",
)

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n{'=' * 70}")
print(f"CRON-105 Verification: {PASS} passed, {FAIL} failed")
print(f"{'=' * 70}")
if FAILS:
    print("\nFailed checks:")
    for f in FAILS:
        print(f"  - {f}")
sys.exit(0 if FAIL == 0 else 1)
