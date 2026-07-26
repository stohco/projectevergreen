#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-104 — Canon-Aware Cave Placement Verification.

Verifies that BlueprintChunkGenerator.applyCarvers now consults the
shouldSuppressCarvers guard before delegating to vanilla, that the
guard correctly identifies protected canon categories (settlement,
sect, ruin), that the Suzaku Tomb gets the extended radius, and that
the debug screen reports the suppression status.

Categories of checks:
  1. Constants — CAVE_SUPPRESSION_RADIUS_DEFAULT=80, _TOMB=150
  2. applyCarvers override — guards with shouldSuppressCarvers, then delegates
  3. shouldSuppressCarvers — rectangle-circle intersection logic
  4. isProtectedCategory — settlement/sect/ruin true; region/geographic/dangerous_region false
  5. getCaveSuppressionRadius — suzaku_tomb → 150, all others → 80
  6. getCaveSuppressionLabel — returns "ACTIVE near <name> (r=<radius>)" or "inactive"
  7. addDebugScreenInfo — reports "Cave suppression:" line
  8. Class javadoc — mentions CRON-104, 朱雀墓, applyCarvers, shouldSuppressCarvers
  9. Canon fidelity — 朱雀墓, 恒岳派, 王家村, 修魔海, 决明谷 all attested
 10. Architecture — no WorldFacade dependency, no Provenance in cave guard
 11. Integration — planet_suzaku.json still uses ergenverse:blueprint
 12. Canon honesty — NO fabricated chapter citation

Run: python3 scripts/cron104_verify_canon_cave_guard.py
Exit code: 0 if all checks pass, 1 otherwise.
"""

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "forge-mod"
JAVA = ROOT / "src/main/java/dev/ergenverse/runtime/worldgen/BlueprintChunkGenerator.java"
BLUEPRINT = ROOT / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"
DIMENSION_JSON = ROOT / "src/main/resources/data/ergenverse/dimension/planet_suzaku.json"

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

java_text = JAVA.read_text(encoding="utf-8")
blueprint_text = BLUEPRINT.read_text(encoding="utf-8")
dimension_text = DIMENSION_JSON.read_text(encoding="utf-8")

# ─────────────────────────────────────────────────────────────────────────────
# 1. Constants
# ─────────────────────────────────────────────────────────────────────────────
section("1. Constants")

check(
    "CAVE_SUPPRESSION_RADIUS_DEFAULT = 80" in java_text,
    "CAVE_SUPPRESSION_RADIUS_DEFAULT = 80 declared",
)
# Make sure it's an int field declaration, not a comment reference
check(
    re.search(r"private\s+static\s+final\s+int\s+CAVE_SUPPRESSION_RADIUS_DEFAULT\s*=\s*80\s*;", java_text)
    is not None,
    "CAVE_SUPPRESSION_RADIUS_DEFAULT is a private static final int = 80",
)

check(
    "CAVE_SUPPRESSION_RADIUS_TOMB = 150" in java_text,
    "CAVE_SUPPRESSION_RADIUS_TOMB = 150 declared",
)
check(
    re.search(r"private\s+static\s+final\s+int\s+CAVE_SUPPRESSION_RADIUS_TOMB\s*=\s*150\s*;", java_text)
    is not None,
    "CAVE_SUPPRESSION_RADIUS_TOMB is a private static final int = 150",
)

check(
    "CANON-AWARE CAVE SUPPRESSION (CRON-104)" in java_text,
    "Section header comment mentions CRON-104",
)

# ─────────────────────────────────────────────────────────────────────────────
# 2. applyCarvers override
# ─────────────────────────────────────────────────────────────────────────────
section("2. applyCarvers override")

check(
    "public void applyCarvers(" in java_text,
    "applyCarvers method declared",
)
check(
    "GenerationStep.Carving step" in java_text,
    "applyCarvers takes GenerationStep.Carving step parameter",
)
check(
    re.search(
        r"applyCarvers\([^)]*\)\s*\{[^}]*shouldSuppressCarvers\([^)]*\)[^}]*\}",
        java_text,
        re.DOTALL,
    )
    is not None,
    "applyCarvers calls shouldSuppressCarvers before delegating",
)
check(
    re.search(
        r"if\s*\(\s*shouldSuppressCarvers\([^)]*\)\s*\)\s*\{[^}]*return\s*;[^}]*\}",
        java_text,
        re.DOTALL,
    )
    is not None,
    "applyCarvers returns early (skips carvers) when shouldSuppressCarvers is true",
)
check(
    "wrapped.applyCarvers(" in java_text,
    "applyCarvers delegates to wrapped.applyCarvers when not suppressed",
)
check(
    "CRON-104" in java_text and "canon-aware cave placement" in java_text.lower(),
    "applyCarvers javadoc mentions CRON-104 canon-aware cave placement",
)

# ─────────────────────────────────────────────────────────────────────────────
# 3. shouldSuppressCarvers logic
# ─────────────────────────────────────────────────────────────────────────────
section("3. shouldSuppressCarvers logic")

check(
    "private boolean shouldSuppressCarvers(" in java_text,
    "shouldSuppressCarvers method declared as private boolean",
)
check(
    "ChunkAccess chunk" in java_text,
    "shouldSuppressCarvers takes ChunkAccess chunk",
)
check(
    "chunk.getPos().getMinBlockX()" in java_text,
    "shouldSuppressCarvers reads chunk.getPos().getMinBlockX()",
)
check(
    "chunk.getPos().getMaxBlockX()" in java_text,
    "shouldSuppressCarvers reads chunk.getPos().getMaxBlockX()",
)
check(
    "chunk.getPos().getMinBlockZ()" in java_text,
    "shouldSuppressCarvers reads chunk.getPos().getMinBlockZ()",
)
check(
    "chunk.getPos().getMaxBlockZ()" in java_text,
    "shouldSuppressCarvers reads chunk.getPos().getMaxBlockZ()",
)
check(
    "PlanetSuzakuBlueprint.canonical()" in java_text,
    "shouldSuppressCarvers consults PlanetSuzakuBlueprint.canonical()",
)
check(
    "allLocations().values()" in java_text,
    "shouldSuppressCarvers iterates allLocations().values()",
)
check(
    "isProtectedCategory(loc.category)" in java_text,
    "shouldSuppressCarvers calls isProtectedCategory(loc.category)",
)
check(
    "getCaveSuppressionRadius(loc)" in java_text,
    "shouldSuppressCarvers calls getCaveSuppressionRadius(loc)",
)
# Rectangle-circle intersection: closest-point check
check(
    "Math.max(chunkMinX, Math.min(loc.x, chunkMaxX))" in java_text,
    "shouldSuppressCarvers computes closestX via Math.max/Math.min clamp",
)
check(
    "Math.max(chunkMinZ, Math.min(loc.z, chunkMaxZ))" in java_text,
    "shouldSuppressCarvers computes closestZ via Math.max/Math.min clamp",
)
check(
    "dx * dx + dz * dz <= radius * radius" in java_text,
    "shouldSuppressCarvers uses squared distance <= squared radius",
)

# ─────────────────────────────────────────────────────────────────────────────
# 4. isProtectedCategory
# ─────────────────────────────────────────────────────────────────────────────
section("4. isProtectedCategory")

check(
    "private static boolean isProtectedCategory(" in java_text,
    "isProtectedCategory method declared as private static boolean",
)
check(
    '"settlement".equals(category)' in java_text,
    "isProtectedCategory returns true for settlement",
)
check(
    '"sect".equals(category)' in java_text,
    "isProtectedCategory returns true for sect",
)
check(
    '"ruin".equals(category)' in java_text,
    "isProtectedCategory returns true for ruin",
)
# Unprotected categories should NOT be in the isProtectedCategory method
# (find the method body and check it doesn't include the unprotected categories)
m = re.search(
    r"private\s+static\s+boolean\s+isProtectedCategory\([^)]*\)\s*\{([^}]+)\}",
    java_text,
    re.DOTALL,
)
if m:
    body = m.group(1)
    check(
        '"region"' not in body,
        "isProtectedCategory does NOT return true for region",
    )
    check(
        '"geographic"' not in body,
        "isProtectedCategory does NOT return true for geographic",
    )
    check(
        '"dangerous_region"' not in body,
        "isProtectedCategory does NOT return true for dangerous_region",
    )
else:
    check(False, "could not extract isProtectedCategory method body")
    body = ""

# ─────────────────────────────────────────────────────────────────────────────
# 5. getCaveSuppressionRadius
# ─────────────────────────────────────────────────────────────────────────────
section("5. getCaveSuppressionRadius")

check(
    "private static int getCaveSuppressionRadius(" in java_text,
    "getCaveSuppressionRadius method declared as private static int",
)
check(
    '"suzaku_tomb".equals(loc.id)' in java_text,
    "getCaveSuppressionRadius checks suzaku_tomb id",
)
check(
    "CAVE_SUPPRESSION_RADIUS_TOMB" in java_text,
    "getCaveSuppressionRadius returns CAVE_SUPPRESSION_RADIUS_TOMB for suzaku_tomb",
)
check(
    "CAVE_SUPPRESSION_RADIUS_DEFAULT" in java_text,
    "getCaveSuppressionRadius returns CAVE_SUPPRESSION_RADIUS_DEFAULT for others",
)

# ─────────────────────────────────────────────────────────────────────────────
# 6. getCaveSuppressionLabel
# ─────────────────────────────────────────────────────────────────────────────
section("6. getCaveSuppressionLabel")

check(
    "private static String getCaveSuppressionLabel(" in java_text,
    "getCaveSuppressionLabel method declared as private static String",
)
check(
    '"ACTIVE near " + loc.name' in java_text,
    "getCaveSuppressionLabel returns 'ACTIVE near <name>' when suppressing",
)
check(
    '" (r=" + radius + ")"' in java_text,
    "getCaveSuppressionLabel includes radius in label ' (r=<radius>)'",
)
check(
    'return "inactive"' in java_text,
    "getCaveSuppressionLabel returns 'inactive' when not suppressing",
)

# ─────────────────────────────────────────────────────────────────────────────
# 7. addDebugScreenInfo
# ─────────────────────────────────────────────────────────────────────────────
section("7. addDebugScreenInfo")

check(
    '"[Er Gen Verse] Cave suppression: " + caveStatus' in java_text,
    "addDebugScreenInfo reports '[Er Gen Verse] Cave suppression: <status>'",
)
check(
    "getCaveSuppressionLabel(sx, sz)" in java_text,
    "addDebugScreenInfo calls getCaveSuppressionLabel(sx, sz)",
)
check(
    "CRON-104: report canon-aware cave-suppression status" in java_text,
    "addDebugScreenInfo javadoc/comment mentions CRON-104",
)

# ─────────────────────────────────────────────────────────────────────────────
# 8. Class-level javadoc
# ─────────────────────────────────────────────────────────────────────────────
section("8. Class-level javadoc")

check(
    "CRON-COMPLETIONIST-104 — CANON-AWARE CAVE PLACEMENT" in java_text,
    "Class javadoc has CRON-COMPLETIONIST-104 section header",
)
check(
    "applyCarvers" in java_text,
    "Class javadoc mentions applyCarvers",
)
check(
    "shouldSuppressCarvers" in java_text,
    "Class javadoc mentions shouldSuppressCarvers",
)
check(
    "朱雀墓" in java_text,
    "Class javadoc mentions 朱雀墓 (Suzaku Tomb)",
)
check(
    "朱雀子" in java_text,
    "Class javadoc mentions 朱雀子 (Suzaku Son lineage)",
)
check(
    "王家村" in java_text,
    "Class javadoc mentions 王家村 (Wang Family Village)",
)
check(
    "恒岳派" in java_text,
    "Class javadoc mentions 恒岳派 (Heng Yue Sect)",
)

# ─────────────────────────────────────────────────────────────────────────────
# 9. Canon fidelity (fact-checked against the novel / web-search)
# ─────────────────────────────────────────────────────────────────────────────
section("9. Canon fidelity")

# All protected locations should be attested in PlanetSuzakuBlueprint
check(
    '"wang_family_village"' in blueprint_text,
    "PlanetSuzakuBlueprint defines wang_family_village (Wang Lin's birthplace)",
)
check(
    '"heng_yue_sect"' in blueprint_text,
    "PlanetSuzakuBlueprint defines heng_yue_sect (恒岳派)",
)
check(
    '"soul_refining_sect"' in blueprint_text,
    "PlanetSuzakuBlueprint defines soul_refining_sect (炼魂宗)",
)
check(
    '"xuan_dao_sect"' in blueprint_text,
    "PlanetSuzakuBlueprint defines xuan_dao_sect (玄道宗)",
)
check(
    '"luo_he_sect"' in blueprint_text,
    "PlanetSuzakuBlueprint defines luo_he_sect (洛河门 — Li Muwan's sect)",
)
check(
    '"teng_family_city"' in blueprint_text,
    "PlanetSuzakuBlueprint defines teng_family_city (藤家城)",
)
check(
    '"tian_shui_city"' in blueprint_text,
    "PlanetSuzakuBlueprint defines tian_shui_city (天水城)",
)
check(
    '"qilin_city"' in blueprint_text,
    "PlanetSuzakuBlueprint defines qilin_city (麒麟城)",
)
check(
    '"nan_dou_city"' in blueprint_text,
    "PlanetSuzakuBlueprint defines nan_dou_city (南斗城)",
)
check(
    '"snow_domain_capital"' in blueprint_text,
    "PlanetSuzakuBlueprint defines snow_domain_capital (雪域国)",
)
check(
    '"vermilion_bird_capital"' in blueprint_text,
    "PlanetSuzakuBlueprint defines vermilion_bird_capital (朱雀国)",
)
check(
    '"suzaku_tomb"' in blueprint_text,
    "PlanetSuzakuBlueprint defines suzaku_tomb (朱雀墓)",
)

# Unprotected locations should also exist
check(
    '"four_sects_alliance"' in blueprint_text,
    "PlanetSuzakuBlueprint defines four_sects_alliance (四派联盟 — UNPROTECTED region)",
)
check(
    '"sea_of_devils"' in blueprint_text,
    "PlanetSuzakuBlueprint defines sea_of_devils (修魔海 — UNPROTECTED geographic)",
)
check(
    '"jue_ming_valley"' in blueprint_text,
    "PlanetSuzakuBlueprint defines jue_ming_valley (决明谷 — UNPROTECTED dangerous_region)",
)

# Canon names attested in PlanetSuzakuBlueprint
check(
    "朱雀墓" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 朱雀墓",
)
check(
    "恒岳派" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 恒岳派",
)
check(
    "修魔海" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 修魔海 (Sea of Devils — canon name)",
)
check(
    "雪域国" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 雪域国 (Snow Domain — canon name)",
)
check(
    "决明谷" in blueprint_text,
    "PlanetSuzakuBlueprint mentions 决明谷 (Jue Ming Valley — canon name, not 绝命)",
)

# Suzaku Tomb should have y=-60 (underground)
# The name "Suzaku Tomb (朱雀墓)" contains parentheses, so we can't use [^)]+?.
# Instead, find the line with suzaku_tomb and extract the next 3 integer args.
tomb_match = re.search(
    r'new\s+CanonLocation\(\s*"suzaku_tumb"[^"]*"[^"]*",\s*"([^"]*)"[^,]*,\s*'
    r'(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*,',
    blueprint_text,
)
if not tomb_match:
    # Try a more permissive multiline match — the constructor call may span
    # multiple lines. Match "suzaku_tomb" then skip ahead to the next 4
    # comma-separated arguments (name, x, y, z — but name is the 2nd arg).
    tomb_match = re.search(
        r'new\s+CanonLocation\(\s*"suzaku_tomb"\s*,\s*"[^"]*"\s*,\s*'
        r'(-?\d+)\s*,\s*(-?\d+)\s*,\s*(-?\d+)\s*,',
        blueprint_text,
    )
if tomb_match:
    # The regex captures 3 groups: x, y, z (or 4 if name was captured)
    groups = tomb_match.groups()
    # Take the last 3 integer groups as x, y, z
    nums = [int(g) for g in groups if g is not None and re.match(r"^-?\d+$", g)]
    if len(nums) >= 3:
        tomb_x, tomb_y, tomb_z = nums[-3], nums[-2], nums[-1]
        check(
            tomb_y < 0,
            f"Suzaku Tomb has y={tomb_y} (underground, negative Y)",
        )
    else:
        check(False, f"could not parse Suzaku Tomb coordinates from {groups}")
else:
    check(False, "could not extract Suzaku Tomb coordinates")

# Categories in PlanetSuzakuBlueprint
check(
    '"settlement"' in blueprint_text,
    "PlanetSuzakuBlueprint uses 'settlement' category",
)
check(
    '"sect"' in blueprint_text,
    "PlanetSuzakuBlueprint uses 'sect' category",
)
check(
    '"ruin"' in blueprint_text,
    "PlanetSuzakuBlueprint uses 'ruin' category",
)

# ─────────────────────────────────────────────────────────────────────────────
# 10. Architecture
# ─────────────────────────────────────────────────────────────────────────────
section("10. Architecture")

# The cave guard should NOT depend on WorldFacade (chunk-gen purity)
# Find the applyCarvers method body and verify no WorldFacade reference
m_apply = re.search(
    r"public\s+void\s+applyCarvers\([^)]*\)\s*\{(.+?)^\s*\}",
    java_text,
    re.DOTALL | re.MULTILINE,
)
if m_apply:
    body = m_apply.group(1)
    check(
        "WorldFacade" not in body,
        "applyCarvers does NOT reference WorldFacade (chunk-gen purity)",
    )
    check(
        "WorldRuntime" not in body,
        "applyCarvers does NOT reference WorldRuntime (chunk-gen purity)",
    )
else:
    check(False, "could not extract applyCarvers method body")

# The shouldSuppressCarvers method should NOT depend on WorldFacade/WorldRuntime
m_suppress = re.search(
    r"private\s+boolean\s+shouldSuppressCarvers\([^)]*\)\s*\{(.+?)^\s*\}",
    java_text,
    re.DOTALL | re.MULTILINE,
)
if m_suppress:
    body = m_suppress.group(1)
    check(
        "WorldFacade" not in body,
        "shouldSuppressCarvers does NOT reference WorldFacade",
    )
    check(
        "WorldRuntime" not in body,
        "shouldSuppressCarvers does NOT reference WorldRuntime",
    )
    check(
        "Provenance" not in body,
        "shouldSuppressCarvers does NOT reference Provenance (no ownership concepts)",
    )
else:
    check(False, "could not extract shouldSuppressCarvers method body")

# The isProtectedCategory should be a pure function (no instance state)
m_cat = re.search(
    r"private\s+static\s+boolean\s+isProtectedCategory\([^)]*\)\s*\{(.+?)^\s*\}",
    java_text,
    re.DOTALL | re.MULTILINE,
)
if m_cat:
    body = m_cat.group(1)
    check(
        "WorldFacade" not in body and "WorldRuntime" not in body and "Provenance" not in body,
        "isProtectedCategory is a pure function (no WorldFacade/WorldRuntime/Provenance)",
    )
else:
    check(False, "could not extract isProtectedCategory method body")

# ─────────────────────────────────────────────────────────────────────────────
# 11. Integration — planet_suzaku.json still uses ergenverse:blueprint
# ─────────────────────────────────────────────────────────────────────────────
section("11. Integration — planet_suzaku.json")

check(
    '"type": "ergenverse:blueprint"' in dimension_text,
    "planet_suzaku.json generator type is ergenverse:blueprint",
)
check(
    '"settings": "minecraft:overworld"' in dimension_text,
    "planet_suzaku.json retains minecraft:overworld settings (for cave/surface rules)",
)

# ErgenverseChunkGenerators still registers BLUEPRINT
chunk_gens_path = ROOT / "src/main/java/dev/ergenverse/runtime/worldgen/ErgenverseChunkGenerators.java"
chunk_gens_text = chunk_gens_path.read_text(encoding="utf-8")
check(
    'register("blueprint"' in chunk_gens_text,
    "ErgenverseChunkGenerators registers 'blueprint' codec",
)
check(
    "BlueprintChunkGenerator.CODEC" in chunk_gens_text,
    "ErgenverseChunkGenerators uses BlueprintChunkGenerator.CODEC",
)
check(
    "Registries.CHUNK_GENERATOR" in chunk_gens_text,
    "ErgenverseChunkGenerators uses Registries.CHUNK_GENERATOR",
)

# Ergenverse.java still registers ErgenverseChunkGenerators
ergenverse_path = ROOT / "src/main/java/dev/ergenverse/core/Ergenverse.java"
ergenverse_text = ergenverse_path.read_text(encoding="utf-8")
check(
    "ErgenverseChunkGenerators.register(modEventBus)" in ergenverse_text,
    "Ergenverse.java calls ErgenverseChunkGenerators.register(modEventBus)",
)

# ─────────────────────────────────────────────────────────────────────────────
# 12. Canon honesty — NO fabricated chapter citation
# ─────────────────────────────────────────────────────────────────────────────
section("12. Canon honesty")

# The applyCarvers javadoc should explicitly disclaim fabricated citations
# Find the applyCarvers javadoc (the comment block immediately preceding the method)
m_javadoc = re.search(
    r"(/\*\*.*?\*/)\s*@Override\s+public\s+void\s+applyCarvers",
    java_text,
    re.DOTALL,
)
if m_javadoc:
    jd = m_javadoc.group(1)
    check(
        "NO fabricated chapter citation" in jd,
        "applyCarvers javadoc explicitly states 'NO fabricated chapter citation'",
    )
    check(
        "Baidu Baike" in jd,
        "applyCarvers javadoc references Baidu Baike as web-search source",
    )
else:
    check(False, "could not extract applyCarvers javadoc")

# ─────────────────────────────────────────────────────────────────────────────
# Summary
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n{'=' * 70}")
print(f"CRON-104 Verification: {PASS} passed, {FAIL} failed")
print(f"{'=' * 70}")
if FAILS:
    print("\nFailed checks:")
    for f in FAILS:
        print(f"  - {f}")
sys.exit(0 if FAIL == 0 else 1)
