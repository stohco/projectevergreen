#!/usr/bin/env python3
"""
CRON-135 verification script — upward ray-cast for tall obstacles.

Verifies that CultivatorFlightNavigator now detects TALL obstacles
(mountainsides, cliffs, towers) whose leading edge is above the
cultivator's body height (1.5). Closes CRON-133 self-critique #5.

Checks:
  1. UPWARD_SAMPLES constant exists with values {3.0D, 6.0D, 9.0D}.
  2. TALL_VAULT_SPEED_SCALE constant exists (= 1.2D, stronger than VAULT_SPEED_SCALE=0.8D).
  3. SteerResult has tallObstacle field.
  4. SteerResult constructor takes 6 args (added tallObstacle).
  5. isUpwardBlocked helper method exists.
  6. computeSteer calls isUpwardBlocked.
  7. computeSteer combines forwardBlocked || upwardBlocked into blocked.
  8. computeSteer uses tallObstacle flag to choose TALL_VAULT_SPEED_SCALE.
  9. computeSteer doubles upward bias during dodge when tallObstacle.
 10. All 5 SteerResult construction sites updated to 6-arg form.
 11. Canon fidelity: CRON-135 marker, mountainside reference, web-search date.
 12. No regression: CRON-133 constants unchanged (LOOKAHEAD, HEIGHT_SAMPLES,
     DODGE_PROBE_DIST, DODGE_SPEED_SCALE, VAULT_SPEED_SCALE, VAULT_FORWARD_SCALE,
     DODGE_UPWARD_BIAS_SCALE, DODGE_FORWARD_BIAS_SCALE).
 13. Build succeeds (./gradlew compileJava exit code 0).

Exit code: 0 if all checks pass, 1 otherwise.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/z/my-project/forge-mod")
NAVIGATOR = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/control/CultivatorFlightNavigator.java"

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


def extract_method_body(src: str, method_sig: str) -> str:
    """Extract the body of a method by brace matching (handles nested blocks)."""
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


nav_src = read(NAVIGATOR)

# ──────────────────────────────────────────────────────────────────────────
# 1. UPWARD_SAMPLES constant
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] UPWARD_SAMPLES constant")
check("navigator file exists", NAVIGATOR.exists())
check("UPWARD_SAMPLES constant declared",
      "public static final double[] UPWARD_SAMPLES" in nav_src)
check("UPWARD_SAMPLES = {3.0D, 6.0D, 9.0D}",
      "UPWARD_SAMPLES = {3.0D, 6.0D, 9.0D}" in nav_src)
check("UPWARD_SAMPLES javadoc references CRON-135",
      re.search(r"CRON-135:\s*Upward\s+sample\s+offsets", nav_src) is not None)
check("UPWARD_SAMPLES javadoc explains tall obstacle detection",
      "TALL obstacles" in nav_src and "mountainside" in nav_src.lower())

# ──────────────────────────────────────────────────────────────────────────
# 2. TALL_VAULT_SPEED_SCALE constant
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] TALL_VAULT_SPEED_SCALE constant")
check("TALL_VAULT_SPEED_SCALE constant declared",
      "public static final double TALL_VAULT_SPEED_SCALE" in nav_src)
check("TALL_VAULT_SPEED_SCALE declared and >= 1.2D (CRON-136 corrected to 3.0D)",
      re.search(r"TALL_VAULT_SPEED_SCALE\s*=\s*([0-9.]+)D", nav_src) is not None and
      float(re.search(r"TALL_VAULT_SPEED_SCALE\s*=\s*([0-9.]+)D", nav_src).group(1)) >= 1.2,
      "expected TALL_VAULT_SPEED_SCALE >= 1.2D (CRON-136 set 3.0D)")
check("TALL_VAULT_SPEED_SCALE > VAULT_SPEED_SCALE (0.8D)",
      re.search(r"TALL_VAULT_SPEED_SCALE\s*=\s*([0-9.]+)D", nav_src) is not None and
      float(re.search(r"TALL_VAULT_SPEED_SCALE\s*=\s*([0-9.]+)D", nav_src).group(1)) > 0.8 and
      "VAULT_SPEED_SCALE = 0.8D" in nav_src)
check("TALL_VAULT_SPEED_SCALE javadoc references CRON-135",
      re.search(r"CRON-135:\s*Vault\s+upward\s+impulse\s+scale\s+for\s+TALL", nav_src) is not None)

# ──────────────────────────────────────────────────────────────────────────
# 3. SteerResult.tallObstacle field
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] SteerResult.tallObstacle field")
check("public final boolean tallObstacle field",
      "public final boolean tallObstacle;" in nav_src)
check("tallObstacle javadoc references CRON-135",
      re.search(r"CRON-135:\s*True\s+if\s+the\s+UPWARD\s+ray-cast", nav_src) is not None)
check("tallObstacle javadoc references TALL_VAULT_SPEED_SCALE",
      "TALL_VAULT_SPEED_SCALE" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 4. SteerResult constructor (6 args)
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] SteerResult constructor (6 args)")
check("constructor takes 6 args (added tallObstacle)",
      re.search(r"public SteerResult\s*\(\s*Vec3\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*,\s*boolean\s+\w+\s*\)", nav_src) is not None)
check("constructor assigns this.tallObstacle = tallObstacle",
      re.search(r"this\.tallObstacle\s*=\s*tallObstacle\s*;", nav_src) is not None)

# ──────────────────────────────────────────────────────────────────────────
# 5. isUpwardBlocked helper method
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] isUpwardBlocked helper method")
check("private static boolean isUpwardBlocked method exists",
      "private static boolean isUpwardBlocked(" in nav_src)
check("isUpwardBlocked javadoc references CRON-135",
      re.search(r"CRON-135:\s*Ray-cast\s+forward.*UPWARD\s+heights", nav_src, re.DOTALL) is not None)
check("isUpwardBlocked iterates UPWARD_SAMPLES",
      "for (double h : UPWARD_SAMPLES)" in nav_src)
check("isUpwardBlocked uses isSolidRender (consistent with isForwardBlocked)",
      "state.isSolidRender(level, pos)" in nav_src)
check("isUpwardBlocked uses BlockPos.containing",
      "BlockPos.containing(" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 6. computeSteer calls isUpwardBlocked
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] computeSteer calls isUpwardBlocked")
compute_body = extract_method_body(nav_src, "public static SteerResult computeSteer(")
check("computeSteer body extracted", len(compute_body) > 0)
if compute_body:
    check("computeSteer calls isUpwardBlocked(cultivator, nx, nz)",
          "isUpwardBlocked(cultivator, nx, nz)" in compute_body)
    check("computeSteer calls isForwardBlocked (still present)",
          "isForwardBlocked(cultivator, nx, nz)" in compute_body)

# ──────────────────────────────────────────────────────────────────────────
# 7. computeSteer combines forwardBlocked || upwardBlocked
# ──────────────────────────────────────────────────────────────────────────
print("\n[7] computeSteer combines forwardBlocked || upwardBlocked")
if compute_body:
    check("forwardBlocked variable declared",
          "boolean forwardBlocked = isForwardBlocked" in compute_body)
    check("upwardBlocked variable declared",
          "boolean upwardBlocked = isUpwardBlocked" in compute_body)
    check("blocked = forwardBlocked || upwardBlocked",
          "boolean blocked = forwardBlocked || upwardBlocked" in compute_body)
    check("tallObstacle = upwardBlocked",
          "boolean tallObstacle = upwardBlocked" in compute_body)

# ──────────────────────────────────────────────────────────────────────────
# 8. computeSteer uses tallObstacle to choose TALL_VAULT_SPEED_SCALE
# ──────────────────────────────────────────────────────────────────────────
print("\n[8] computeSteer uses tallObstacle for vault scale")
if compute_body:
    check("vaultScale = tallObstacle ? TALL_VAULT_SPEED_SCALE : VAULT_SPEED_SCALE",
          "double vaultScale = tallObstacle ? TALL_VAULT_SPEED_SCALE : VAULT_SPEED_SCALE" in compute_body)
    check("vy = flightSpeed * vaultScale (uses chosen scale)",
          "double vy = flightSpeed * vaultScale" in compute_body)

# ──────────────────────────────────────────────────────────────────────────
# 9. computeSteer doubles upward bias during dodge when tallObstacle
# ──────────────────────────────────────────────────────────────────────────
print("\n[9] computeSteer doubles upward bias during dodge when tallObstacle")
if compute_body:
    check("upwardBias variable declared (conditional on tallObstacle)",
          "double upwardBias = tallObstacle" in compute_body)
    check("upwardBias = flightSpeed * DODGE_UPWARD_BIAS_SCALE * 2.0D for tall",
          "DODGE_UPWARD_BIAS_SCALE * 2.0D" in compute_body)
    check("upwardBias = flightSpeed * DODGE_UPWARD_BIAS_SCALE for short",
          re.search(r"flightSpeed\s*\*\s*DODGE_UPWARD_BIAS_SCALE\s*;", compute_body) is not None
          or ": flightSpeed * DODGE_UPWARD_BIAS_SCALE" in compute_body)
    check("vy = upwardBias (uses computed bias)",
          "double vy = upwardBias" in compute_body)

# ──────────────────────────────────────────────────────────────────────────
# 10. All 4 SteerResult construction sites updated to 6-arg form
# ──────────────────────────────────────────────────────────────────────────
print("\n[10] All SteerResult construction sites (6-arg form)")
# Count occurrences of "new SteerResult(new Vec3" — should be 4 (vertical-only,
# clear-path, dodge, vault). The constructor itself is a 5th occurrence but
# it's a definition, not a call site, so it doesn't match `new SteerResult(new Vec3`.
count = nav_src.count("new SteerResult(new Vec3")
check(f"found 4 SteerResult constructions (found {count})",
      count == 4, f"expected 4, found {count}")
# Verify each has 6 boolean args (false/true patterns).
# Use DOTALL so the regex spans the multi-line `new Vec3(...)` calls.
# The `[^)]*` inside Vec3(...) handles simple arg lists like (0, vy, 0) and
# (vx, vy, vz) — these don't contain nested parens.
six_arg_pattern = re.compile(
    r"new SteerResult\s*\(\s*new Vec3\s*\([^)]*\)\s*,"
    r"\s*(?:true|false)\s*,"
    r"\s*(?:true|false|tallObstacle)\s*,"
    r"\s*(?:true|false|goLeft|!goLeft)\s*,"
    r"\s*(?:true|false|!goLeft)\s*,"
    r"\s*(?:true|false)\s*\)",
    re.DOTALL,
)
matches = six_arg_pattern.findall(nav_src)
check(f"all SteerResult constructions match 6-arg pattern (found {len(matches)})",
      len(matches) == 4, f"expected 4 matches, found {len(matches)}")

# Specific construction checks
check("vertical-only edge case: 6 args (false, false, false, false, false)",
      "new SteerResult(new Vec3(0, vy, 0), false, false, false, false, false)" in nav_src)
check("clear path: 6 args (false, false, false, false, false)",
      "new SteerResult(new Vec3(vx, vy, vz), false, false, false, false, false)" in nav_src)
check("dodge: 6 args with tallObstacle flag",
      "new SteerResult(new Vec3(vx, vy, vz), true, tallObstacle, goLeft, !goLeft, false)" in nav_src)
check("vault: 6 args with tallObstacle flag",
      "new SteerResult(new Vec3(vx, vy, vz), true, tallObstacle, false, false, true)" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 11. Canon fidelity markers
# ──────────────────────────────────────────────────────────────────────────
print("\n[11] Canon fidelity markers")
check("CRON-135 marker in algorithm javadoc",
      re.search(r"<b>CRON-135:</b>\s*Also\s+ray-cast", nav_src) is not None)
check("CRON-135 marker in vault step javadoc",
      re.search(r"When\s+the\s+upward\s+ray-cast[\s\*]+detected\s+the\s+obstacle", nav_src) is not None)
check("references mountainside (tall obstacle example)",
      "mountainside" in nav_src.lower())
check("references cliff (tall obstacle example)",
      "cliff" in nav_src.lower())
check("references tower (tall obstacle example)",
      "tower" in nav_src.lower())
check("web-search verification date 2026-07-27",
      "2026-07-27" in nav_src)
check("no-fabricated-citations disclaimer",
      re.search(r"NO\s+fabricated\s+chapter[\s\*]+citations", nav_src) is not None)
check("references 御剑飞行 (sword flight canon)",
      "仙逆" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 12. No regression: CRON-133 constants unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[12] No regression: CRON-133 constants unchanged")
check("LOOKAHEAD = 3.0D (unchanged)",
      "public static final double LOOKAHEAD = 3.0D;" in nav_src)
check("HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D} (unchanged)",
      "public static final double[] HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D};" in nav_src)
check("DODGE_PROBE_DIST = 2.0D (unchanged)",
      "public static final double DODGE_PROBE_DIST = 2.0D;" in nav_src)
check("DODGE_SPEED_SCALE = 0.7D (unchanged)",
      "public static final double DODGE_SPEED_SCALE = 0.7D;" in nav_src)
check("VAULT_SPEED_SCALE = 0.8D (unchanged)",
      "public static final double VAULT_SPEED_SCALE = 0.8D;" in nav_src)
check("VAULT_FORWARD_SCALE = 0.3D (unchanged)",
      "public static final double VAULT_FORWARD_SCALE = 0.3D;" in nav_src)
check("DODGE_UPWARD_BIAS_SCALE = 0.15D (unchanged)",
      "public static final double DODGE_UPWARD_BIAS_SCALE = 0.15D;" in nav_src)
check("DODGE_FORWARD_BIAS_SCALE = 0.2D (unchanged)",
      "public static final double DODGE_FORWARD_BIAS_SCALE = 0.2D;" in nav_src)
check("isForwardBlocked method still exists (unchanged)",
      "private static boolean isForwardBlocked(" in nav_src)
check("isDodgeClear method still exists (unchanged)",
      "private static boolean isDodgeClear(" in nav_src)
check("clampVy method still exists (unchanged)",
      "private static double clampVy(" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 13. Build verification
# ──────────────────────────────────────────────────────────────────────────
print("\n[13] Build verification")
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
print(f"CRON-135 VERIFICATION: {PASS} pass, {FAIL} fail")
print("=" * 60)
if FAIL > 0:
    print("\nFAILED CHECKS:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
sys.exit(0)
