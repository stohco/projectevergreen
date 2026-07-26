#!/usr/bin/env python3
"""
CRON-92 verification script — confirms the three simulation-writer fallback
leak paths have been eliminated and all writes route through WorldFacade.

The three target files:
  1. SpiritBeastFeedGoal.java       — beast herb-harvest writer
  2. BlockPlacementEngine.java      — sect residence wall-placement writer
  3. WangLinHomeBuilder.java         — sect residence marker-placement writer

For each file, verifies:
  - No direct level.setBlock(...) calls remain in the simulation write path
  - The method calls rt.world().setSimulationBlock(...) (the WorldFacade)
  - CRON-92 javadoc is present explaining the fix
  - Early-return + warn-log pattern matches WeatherDamageSubscriber (the clean reference)

Also verifies:
  - WeatherDamageSubscriber.java still has the clean pattern (no regression)
  - No new direct level.setBlock calls were introduced anywhere in the simulation paths
  - The CANON one-shot builders (which legitimately use level.setBlock) are NOT touched

Run: python3 /home/z/my-project/forge-mod/scripts/cron92_verify_simulation_writers.py
"""

import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
JAVA_ROOT = ROOT / "src/main/java/dev/ergenverse"

passed = 0
failed = 0
checks = []


def check(name, ok, detail=""):
    global passed, failed
    checks.append((name, ok, detail))
    if ok:
        passed += 1
        print(f"  PASS  {name}")
    else:
        failed += 1
        print(f"  FAIL  {name}  {detail}")


def strip_comments(src):
    """Remove block comments and line comments from Java source."""
    src = re.sub(r"/\*\*.*?\*/", "", src, flags=re.DOTALL)
    src = re.sub(r"//[^\n]*", "", src)
    return src


# ──────────────────────────────────────────────────────────────────────────
# 1. SpiritBeastFeedGoal.java — fallback leak eliminated
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] SpiritBeastFeedGoal.java — beast herb-harvest writer")

feed_path = JAVA_ROOT / "entity/ai/SpiritBeastFeedGoal.java"
feed_src = feed_path.read_text(encoding="utf-8")
feed_code = strip_comments(feed_src)

check(
    "CRON-92 javadoc present in SpiritBeastFeedGoal",
    "CRON-COMPLETIONIST-92" in feed_src and "ELIMINATED FALLBACK LEAK" in feed_src,
)

check(
    "simulationSetBlock method still present",
    "private void simulationSetBlock(BlockPos pos, String blockId)" in feed_src,
)

check(
    "Calls rt.world().setSimulationBlock(...)",
    "rt.world().setSimulationBlock(" in feed_src,
)

check(
    "NO direct level.setBlock in simulationSetBlock (code only, comments stripped)",
    "beast.level().setBlock(" not in feed_code
    and "level().setBlock(pos" not in feed_code,
)

check(
    "Early-return + warn-log when runtime not initialized",
    'if (!rt.isInitialized())' in feed_src
    and "Ergenverse.LOGGER.warn" in feed_src
    and "WorldRuntime not initialized" in feed_src,
)

check(
    "Catch block logs warn (not debug) and does NOT fall back to direct write",
    "Ergenverse.LOGGER.warn" in feed_src
    and "no fallback write" in feed_src.lower(),
)

# ──────────────────────────────────────────────────────────────────────────
# 2. BlockPlacementEngine.java — fallback leak eliminated
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] BlockPlacementEngine.java — sect residence wall-placement writer")

engine_path = JAVA_ROOT / "simulation/residence/BlockPlacementEngine.java"
engine_src = engine_path.read_text(encoding="utf-8")
engine_code = strip_comments(engine_src)

check(
    "CRON-92 javadoc present in BlockPlacementEngine",
    "CRON-COMPLETIONIST-92" in engine_src and "ELIMINATED FALLBACK LEAK" in engine_src,
)

check(
    "setBlock method still present",
    "private static void setBlock(ServerLevel level, BlockPos pos, BlockState state)" in engine_src,
)

check(
    "Calls rt.world().setSimulationBlock(...)",
    "rt.world().setSimulationBlock(" in engine_src,
)

check(
    "NO direct level.setBlock fallback in setBlock method (code only)",
    "level.setBlock(pos, state, 3)" not in engine_code,
)

check(
    "Early-return + warn-log when runtime not initialized",
    'if (!rt.isInitialized())' in engine_src
    and "WorldRuntime not initialized" in engine_src,
)

check(
    "Early-return + warn-log when level is not Suzaku",
    "rt.suzakuLevel() != level" in engine_src
    and "not Planet Suzaku" in engine_src,
)

check(
    "Catch block logs warn and does NOT fall back to direct write",
    "no fallback write" in engine_src.lower(),
)

# ──────────────────────────────────────────────────────────────────────────
# 3. WangLinHomeBuilder.java — fallback leak eliminated
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] WangLinHomeBuilder.java — sect residence marker-placement writer")

home_path = JAVA_ROOT / "simulation/residence/WangLinHomeBuilder.java"
home_src = home_path.read_text(encoding="utf-8")
home_code = strip_comments(home_src)

check(
    "CRON-92 javadoc present in WangLinHomeBuilder",
    "CRON-COMPLETIONIST-92" in home_src and "ELIMINATED FALLBACK LEAK" in home_src,
)

check(
    "placeMarker method still present",
    "private static void placeMarker(ServerLevel level, BlockPos origin)" in home_src,
)

check(
    "Calls rt.world().setSimulationBlock(...)",
    "rt.world().setSimulationBlock(" in home_src,
)

check(
    "NO direct level.setBlock fallback in placeMarker method (code only)",
    "level.setBlock(markerPos, MARKER, 3)" not in home_code,
)

check(
    "Early-return + warn-log when runtime not initialized",
    'if (!rt.isInitialized())' in home_src
    and "WorldRuntime not initialized" in home_src,
)

check(
    "Early-return + warn-log when level is not Suzaku",
    "rt.suzakuLevel() != level" in home_src
    and "not Planet Suzaku" in home_src,
)

check(
    "Catch block logs warn and does NOT fall back to direct write",
    "no fallback write" in home_src.lower(),
)

# ──────────────────────────────────────────────────────────────────────────
# 4. WeatherDamageSubscriber.java — clean pattern intact (no regression)
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] WeatherDamageSubscriber.java — clean reference pattern intact")

weather_path = JAVA_ROOT / "simulation/weather/WeatherDamageSubscriber.java"
weather_src = weather_path.read_text(encoding="utf-8")
weather_code = strip_comments(weather_src)

check(
    "WeatherDamageSubscriber still guards on !rt.isInitialized()",
    'if (!rt.isInitialized())' in weather_src or "if (!rt.isInitialized()) return;" in weather_src,
)

check(
    "WeatherDamageSubscriber has NO direct level.setBlock in scanWindowForDamage (code only)",
    "suzaku.setBlock(" not in weather_code and "level.setBlock(" not in weather_code,
)

check(
    "WeatherDamageSubscriber calls rt.world().setSimulationBlock",
    "rt.world().setSimulationBlock(" in weather_src or ".setSimulationBlock(" in weather_src,
)

# ──────────────────────────────────────────────────────────────────────────
# 5. Global audit — no new direct level.setBlock in simulation paths
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] Global audit — direct level.setBlock calls in simulation paths")

# The three target files should have ZERO direct level.setBlock in their
# simulation write methods. Other files (CANON builders, worldgen, etc.)
# legitimately use level.setBlock and should NOT be touched.
sim_files = {
    "entity/ai/SpiritBeastFeedGoal.java": "beast feed",
    "simulation/residence/BlockPlacementEngine.java": "residence engine",
    "simulation/residence/WangLinHomeBuilder.java": "residence top-level",
    "simulation/weather/WeatherDamageSubscriber.java": "weather damage",
}

for rel, label in sim_files.items():
    path = JAVA_ROOT / rel
    src = path.read_text(encoding="utf-8")
    code = strip_comments(src)
    has_direct = "level.setBlock(" in code or ".setBlock(pos," in code or ".setBlock(markerPos," in code
    check(
        f"{label}: NO direct level.setBlock in stripped code",
        not has_direct,
        f"found direct setBlock in {rel}" if has_direct else "",
    )

# ──────────────────────────────────────────────────────────────────────────
# 6. CANON builders NOT touched (legitimate level.setBlock with provenance guard)
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] CANON one-shot builders NOT touched (legitimate direct writes)")

canon_builders = [
    "spawn/HengYueSectBuilder.java",
    "spawn/SoulRefiningSectBuilder.java",
    "spawn/XuanDaoSectBuilder.java",
    "spawn/LuoHeSectBuilder.java",
    "spawn/WangFamilyVillageBuilder.java",
    "spawn/TengFamilyCityBuilder.java",
    "spawn/TianShuiCityBuilder.java",
    "spawn/NanDouCityBuilder.java",
    "spawn/SnowDomainCapitalBuilder.java",
    "spawn/QilinCityBuilder.java",
    "spawn/VermilionBirdImperialCityBuilder.java",
]

for rel in canon_builders:
    path = JAVA_ROOT / rel
    if not path.exists():
        check(f"{rel}: exists", False, "file not found")
        continue
    src = path.read_text(encoding="utf-8")
    # CANON builders SHOULD still have level.setBlock (they are the blueprint)
    has_setblock = "level.setBlock(" in src
    check(f"{rel}: still has level.setBlock (CANON writer, not touched)", has_setblock)
    # And should have the provenance guard (hasPlayerOrSimulationDelta)
    has_guard = "hasPlayerOrSimulationDelta" in src or "CURRENT_BOUNDS" in src
    check(f"{rel}: has provenance guard (chunk-bounds or delta check)", has_guard)

# ──────────────────────────────────────────────────────────────────────────
# 7. Summary
# ──────────────────────────────────────────────────────────────────────────
print("\n" + "=" * 60)
print(f"RESULT: {passed} passed, {failed} failed")
print("=" * 60)

if failed > 0:
    print("\nFAILED CHECKS:")
    for name, ok, detail in checks:
        if not ok:
            print(f"  - {name}  {detail}")
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED")
    sys.exit(0)
