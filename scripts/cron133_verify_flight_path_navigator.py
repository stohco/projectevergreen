#!/usr/bin/env python3
"""
CRON-133 verification script — CultivatorFlightNavigator + CultivatorFlightGoal wiring.

Verifies:
  1. CultivatorFlightNavigator.java exists with the expected public API
     (constants, SteerResult inner class, computeSteer static method).
  2. CultivatorFlightNavigator.java has the canon-faithful algorithm
     (ray-cast forward at 3 heights, perpendicular dodge probes, vault upward).
  3. CultivatorFlightGoal.java imports and calls the navigator.
  4. CultivatorFlightGoal.java tracks consecutiveBlockedTicks and aborts on stuck.
  5. CultivatorFlightGoal.java applies the navigator's velocity via setDeltaMovement.
  6. Yaw is computed from POST-NAVIGATOR velocity (so dodging cultivators face
     their dodge direction, not the original target direction).
  7. CRON-133 javadoc markers are present in both files.
  8. Canon fidelity: javadoc references 御剑飞行, no fabricated chapter citations.
  9. No regression: CRON-130 constants (FLIGHT_SPEED, MAX_FLIGHT_TICKS,
     ACTIVATE_DIST, YIELD_DIST) are unchanged.
 10. Build succeeds (./gradlew compileJava exit code 0).

Exit code: 0 if all checks pass, 1 otherwise.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/z/my-project/forge-mod")
NAVIGATOR = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/control/CultivatorFlightNavigator.java"
GOAL = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/ai/CultivatorFlightGoal.java"

PASS = 0
FAIL = 0
ERRORS = []


def check(name: str, condition: bool, detail: str = "") -> None:
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  PASS  {name}")
    else:
        FAIL += 1
        ERRORS.append(f"{name}: {detail}")
        print(f"  FAIL  {name}  {detail}")


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


# ──────────────────────────────────────────────────────────────────────────
# 1. CultivatorFlightNavigator.java — file exists + package + class header
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] CultivatorFlightNavigator.java — file structure")
nav_src = read(NAVIGATOR)
check("navigator file exists", NAVIGATOR.exists())
check("package dev.ergenverse.entity.control",
      "package dev.ergenverse.entity.control;" in nav_src)
check("public final class CultivatorFlightNavigator",
      "public final class CultivatorFlightNavigator" in nav_src)
check("private constructor (no instances)",
      "private CultivatorFlightNavigator()" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 2. Navigator constants
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] Navigator constants")
check("LOOKAHEAD = 3.0D", "public static final double LOOKAHEAD = 3.0D;" in nav_src)
check("HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D}",
      "public static final double[] HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D};" in nav_src)
check("DODGE_PROBE_DIST = 2.0D",
      "public static final double DODGE_PROBE_DIST = 2.0D;" in nav_src)
check("DODGE_SPEED_SCALE = 0.7D",
      "public static final double DODGE_SPEED_SCALE = 0.7D;" in nav_src)
check("VAULT_SPEED_SCALE = 0.8D",
      "public static final double VAULT_SPEED_SCALE = 0.8D;" in nav_src)
check("VAULT_FORWARD_SCALE = 0.3D",
      "public static final double VAULT_FORWARD_SCALE = 0.3D;" in nav_src)
check("DODGE_UPWARD_BIAS_SCALE = 0.15D",
      "public static final double DODGE_UPWARD_BIAS_SCALE = 0.15D;" in nav_src)
check("DODGE_FORWARD_BIAS_SCALE = 0.2D",
      "public static final double DODGE_FORWARD_BIAS_SCALE = 0.2D;" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 3. SteerResult inner class
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] SteerResult inner class")
check("public static final class SteerResult",
      "public static final class SteerResult" in nav_src)
check("field: Vec3 velocity",
      "public final Vec3 velocity;" in nav_src)
check("field: boolean blocked",
      "public final boolean blocked;" in nav_src)
check("field: boolean dodgedLeft",
      "public final boolean dodgedLeft;" in nav_src)
check("field: boolean dodgedRight",
      "public final boolean dodgedRight;" in nav_src)
check("field: boolean vaulted",
      "public final boolean vaulted;" in nav_src)
# CRON-135 expanded SteerResult from 5 args to 6 args (added tallObstacle).
# Accept BOTH forms — the CRON-133 invariant is 'constructor exists with the
# expected arg types', not a specific arg count.
check("constructor (5 or 6 args — CRON-135 expanded to 6)",
      re.search(r"public SteerResult\s*\(\s*Vec3\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*(?:,\s*boolean\s+\w+\s*)?\)", nav_src) is not None)

# ──────────────────────────────────────────────────────────────────────────
# 4. computeSteer method
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] computeSteer static method")
check("public static SteerResult computeSteer",
      re.search(r"public static SteerResult computeSteer\s*\(", nav_src) is not None)
check("params: EntityCultivator, Vec3, double, double",
      re.search(r"computeSteer\s*\(\s*EntityCultivator\s+\w+\s*,\s*Vec3\s+\w+\s*,\s*double\s+\w+\s*,\s*double\s+\w+\s*\)", nav_src) is not None)
# CRON-135 changed clear-path return from 5 args to 6 args (added tallObstacle=false).
# Accept BOTH forms.
check("returns new SteerResult on clear path",
      "return new SteerResult(new Vec3(vx, vy, vz), false, false, false, false, false)" in nav_src
      or "return new SteerResult(new Vec3(vx, vy, vz), false, false, false, false);" in nav_src)
# CRON-135 changed dodge return from 5 args to 6 args (added tallObstacle flag).
# Accept BOTH forms — old form has 'true, goLeft, !goLeft, false' (4 bools after Vec3),
# new form has 'true, tallObstacle, goLeft, !goLeft, false' (5 bools after Vec3).
check("returns dodge SteerResult",
      re.search(r"return new SteerResult\s*\(\s*new Vec3\s*\(\s*vx\s*,\s*vy\s*,\s*vz\s*\)\s*,\s*true\s*,\s*(?:tallObstacle\s*,\s*)?goLeft\s*,\s*!goLeft\s*,\s*false\s*\)", nav_src) is not None)
# CRON-135 changed vault return from 5 args to 6 args (added tallObstacle flag).
# Accept BOTH forms — old form has 'true, false, false, true' (4 bools),
# new form has 'true, tallObstacle, false, false, true' (5 bools).
check("returns vault SteerResult",
      re.search(r"return new SteerResult\s*\(\s*new Vec3\s*\(\s*vx\s*,\s*vy\s*,\s*vz\s*\)\s*,\s*true\s*,\s*(?:tallObstacle\s*,\s*)?false\s*,\s*false\s*,\s*true\s*\)", nav_src) is not None)

# ──────────────────────────────────────────────────────────────────────────
# 5. Algorithm — ray-cast + dodge + vault
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] Algorithm — ray-cast lookahead + perpendicular dodge + vault")
check("isForwardBlocked method present",
      "private static boolean isForwardBlocked(" in nav_src)
check("isDodgeClear method present",
      "private static boolean isDodgeClear(" in nav_src)
check("clampVy method present",
      "private static double clampVy(" in nav_src)
check("uses BlockState.isSolidRender (consistent with FlightMoveControl)",
      "state.isSolidRender(level, pos)" in nav_src)
check("uses BlockPos.containing",
      "BlockPos.containing(" in nav_src)
check("deterministic per-cultivator dodge bias (entity ID parity)",
      "cultivator.getId() & 1" in nav_src)
# CRON-135 changed vault to use a conditional vaultScale (tallObstacle ? TALL_VAULT_SPEED_SCALE : VAULT_SPEED_SCALE).
# The CRON-133 invariant is 'VAULT_SPEED_SCALE is used for vault impulse' — still true
# (it's the short-obstacle branch of the conditional). Accept EITHER the direct
# use (CRON-133 form) or the conditional use (CRON-135 form).
check("vault upward impulse when no dodge available",
      "vy = flightSpeed * VAULT_SPEED_SCALE" in nav_src
      or "double vaultScale = tallObstacle ? TALL_VAULT_SPEED_SCALE : VAULT_SPEED_SCALE" in nav_src)
check("dodge has forward bias",
      "DODGE_FORWARD_BIAS_SCALE" in nav_src)
check("dodge has upward bias",
      "DODGE_UPWARD_BIAS_SCALE" in nav_src)
check("vertical-only edge case (directly above/below target)",
      "Math.signum(toTarget.y) * flightSpeed * 0.5D" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 6. Canon fidelity in navigator javadoc
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] Canon fidelity in navigator javadoc")
check("references 御剑飞行 (sword flight)",
      "御剑飞行" in nav_src)
check("references 仙逆",
      "仙逆" in nav_src)
check("references Foundation Establishment 筑基",
      "筑基" in nav_src)
check("references CRON-133 marker",
      "CRON-133" in nav_src)
check("references CRON-130 self-critique #2",
      "CRON-130 self-critique #2" in nav_src)
check("references CRON-132 self-critique #6",
      "CRON-132 self-critique #6" in nav_src)
check("explicit no-fabricated-citations disclaimer",
      re.search(r"NO\s+fabricated\s+chapter[\s\*]+citations", nav_src) is not None)
check("web-search verification date 2026-07-27",
      "2026-07-27" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 7. CultivatorFlightGoal.java — import + state
# ──────────────────────────────────────────────────────────────────────────
print("\n[7] CultivatorFlightGoal.java — import + state")
goal_src = read(GOAL)
check("imports CultivatorFlightNavigator",
      "import dev.ergenverse.entity.control.CultivatorFlightNavigator;" in goal_src)
check("MAX_BLOCKED_TICKS constant defined",
      "private static final int MAX_BLOCKED_TICKS" in goal_src)
check("MAX_BLOCKED_TICKS = 100 (5 seconds)",
      "MAX_BLOCKED_TICKS = 100" in goal_src)
check("consecutiveBlockedTicks field declared",
      "private int consecutiveBlockedTicks;" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 8. start() and stop() reset state
# ──────────────────────────────────────────────────────────────────────────
print("\n[8] start() and stop() reset consecutiveBlockedTicks")
# Use brace-matching extraction (regex can't handle nested {} blocks,
# which became necessary in CRON-134 when stop() gained an if/else block).
def _extract_method_body_133(src: str, method_sig: str) -> str:
    sig_idx = src.find(method_sig)
    if sig_idx == -1:
        return ""
    brace_idx = src.find("{", sig_idx)
    if brace_idx == -1:
        return ""
    depth = 1
    i = brace_idx + 1
    while i < len(src) and depth > 0:
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
        i += 1
    return src[brace_idx + 1 : i - 1]

start_body = _extract_method_body_133(goal_src, "public void start()")
check("start() method exists", len(start_body) > 0)
if start_body:
    check("start() resets consecutiveBlockedTicks = 0",
          "consecutiveBlockedTicks = 0" in start_body)

stop_body = _extract_method_body_133(goal_src, "public void stop()")
check("stop() method exists", len(stop_body) > 0)
if stop_body:
    check("stop() resets consecutiveBlockedTicks = 0",
          "consecutiveBlockedTicks = 0" in stop_body)

# ──────────────────────────────────────────────────────────────────────────
# 9. tick() uses navigator
# ──────────────────────────────────────────────────────────────────────────
print("\n[9] tick() delegates to navigator + stuck detection")
# Use a brace-matching approach instead of regex (regex can't handle nested {}).
def extract_method_body(src: str, method_sig: str) -> str:
    """Extract the body of a method by brace matching (handles nested blocks)."""
    sig_idx = src.find(method_sig)
    if sig_idx == -1:
        return ""
    # Find the opening brace after the signature.
    brace_idx = src.find("{", sig_idx)
    if brace_idx == -1:
        return ""
    depth = 1
    i = brace_idx + 1
    while i < len(src) and depth > 0:
        if src[i] == "{":
            depth += 1
        elif src[i] == "}":
            depth -= 1
        i += 1
    return src[brace_idx + 1 : i - 1]

tick_body = extract_method_body(goal_src, "public void tick()")
check("tick() method exists", len(tick_body) > 0)
if tick_body:
    check("tick() calls CultivatorFlightNavigator.computeSteer",
          "CultivatorFlightNavigator.computeSteer(" in tick_body)
    check("tick() applies steer.velocity via setDeltaMovement",
          "cultivator.setDeltaMovement(steer.velocity)" in tick_body)
    check("tick() has stuck-detection (consecutiveBlockedTicks++)",
          "consecutiveBlockedTicks++" in tick_body)
    check("tick() aborts on MAX_BLOCKED_TICKS",
          "flightTicks = MAX_FLIGHT_TICKS" in tick_body)
    check("tick() logs blockage warnings",
          "Ergenverse.LOGGER.warn" in tick_body)
    check("tick() logs recovery from blockage",
          "cleared obstacle" in tick_body)
    check("tick() computes yaw from POST-NAVIGATOR velocity",
          "Vec3 velocity = steer.velocity" in tick_body)
    check("tick() yaw uses velocity.x and velocity.z (not target dx/dz)",
          "Math.atan2(velocity.z, velocity.x)" in tick_body)

# ──────────────────────────────────────────────────────────────────────────
# 10. CRON-133 javadoc markers in goal
# ──────────────────────────────────────────────────────────────────────────
print("\n[10] CRON-133 javadoc markers in goal")
check("goal javadoc references CRON-133",
      "CRON-133" in goal_src)
check("goal javadoc references CultivatorFlightNavigator",
      "{@link CultivatorFlightNavigator}" in goal_src)
check("goal javadoc references MAX_BLOCKED_TICKS",
      "{@value #MAX_BLOCKED_TICKS}" in goal_src)
check("goal javadoc explains stuck detection",
      "Stuck detection" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 11. No regression: CRON-130 constants unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[11] No regression: CRON-130 constants unchanged")
check("ACTIVATE_DIST = 18.0D (unchanged)",
      "private static final double ACTIVATE_DIST = 18.0D;" in goal_src)
check("YIELD_DIST = 8.0D (unchanged)",
      "private static final double YIELD_DIST = 8.0D;" in goal_src)
check("MAX_FLIGHT_TICKS = 600 (unchanged)",
      "private static final int MAX_FLIGHT_TICKS = 600;" in goal_src)
check("FLIGHT_SPEED = 0.40D (unchanged)",
      "private static final double FLIGHT_SPEED = 0.40D;" in goal_src)
check("CRUISE_ALTITUDE = 4.0D (unchanged)",
      "private static final double CRUISE_ALTITUDE = 4.0D;" in goal_src)
check("resolveFlightTarget still exists",
      "private Vec3 resolveFlightTarget()" in goal_src)
check("computeCruiseAltitude still exists",
      "private double computeCruiseAltitude(" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 12. Build verification — ./gradlew compileJava
# ──────────────────────────────────────────────────────────────────────────
print("\n[12] Build verification")
env = os.environ.copy()
env["JAVA_HOME"] = "/tmp/my-project/.jdks/jdk-17.0.13+11"
try:
    result = subprocess.run(
        ["./gradlew", "compileJava"],
        cwd=str(PROJECT_ROOT),
        env=env,
        capture_output=True,
        text=True,
        timeout=180,
    )
    check("./gradlew compileJava exit code 0",
          result.returncode == 0,
          f"exit={result.returncode}, stderr tail: {result.stderr[-300:]}")
    check("BUILD SUCCESSFUL in output",
          "BUILD SUCCESSFUL" in result.stdout,
          f"stdout tail: {result.stdout[-300:]}")
    check("no 'error:' in output",
          "error:" not in result.stdout and "error:" not in result.stderr)
except subprocess.TimeoutExpired:
    check("./gradlew compileJava exit code 0", False, "TIMEOUT after 180s")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print("\n" + "=" * 60)
print(f"CRON-133 VERIFICATION: {PASS} pass, {FAIL} fail")
print("=" * 60)
if FAIL > 0:
    print("\nFAILED CHECKS:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
sys.exit(0)
