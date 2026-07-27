#!/usr/bin/env python3
"""
CRON-136 verification script — tall-obstacle correctness fixes.

Closes two CRON-135 self-critique items:
  #2: TALL_VAULT_SPEED_SCALE=1.2D is math-insufficient for +9 obstacles.
      At cruise speed 0.4 and LOOKAHEAD=3.0, the warning window is 7.5 ticks.
      1.2D produces 0.48 blocks/tick = 3.6 blocks in 7.5 ticks (crashes into +9).
      CRON-136 corrects to 3.0D: 1.2 blocks/tick = 9.0 blocks in 7.5 ticks (clears +9).
  #4: isDodgeClear only probed at chest height (Y+1.0). A cultivator dodging
      left to avoid a forward obstacle, where the left path has a mountainside
      curving left, would see 'clear' at chest height but crash into the
      mountainside. CRON-136 adds upward probes (+3.0, +6.0) to isDodgeClear.

Checks:
  1. TALL_VAULT_SPEED_SCALE corrected to 3.0D with math derivation in javadoc.
  2. DODGE_HEIGHT_SAMPLES constant = {1.0D, 3.0D, 6.0D}.
  3. isDodgeClear refactored to iterate DODGE_HEIGHT_SAMPLES (no Y+1.0 hardcode).
  4. Class-level algorithm javadoc step 4 mentions CRON-136 + three heights.
  5. No regression: CRON-135 constants unchanged (UPWARD_SAMPLES, HEIGHT_SAMPLES,
     LOOKAHEAD, DODGE_PROBE_DIST, DODGE_SPEED_SCALE, VAULT_SPEED_SCALE,
     VAULT_FORWARD_SCALE, DODGE_UPWARD_BIAS_SCALE, DODGE_FORWARD_BIAS_SCALE).
  6. No regression: SteerResult still 6-arg, tallObstacle field present,
     isUpwardBlocked + isForwardBlocked still present.
  7. Canon fidelity: CRON-136 marker, 仙逆 reference, no fabricated citations.
  8. Build succeeds (./gradlew compileJava exit code 0).

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
# 1. TALL_VAULT_SPEED_SCALE corrected to 3.0D with math derivation
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] TALL_VAULT_SPEED_SCALE corrected to 3.0D")
check("navigator file exists", NAVIGATOR.exists())
check("TALL_VAULT_SPEED_SCALE constant declared",
      "public static final double TALL_VAULT_SPEED_SCALE" in nav_src)
check("TALL_VAULT_SPEED_SCALE = 3.0D (corrected from 1.2D)",
      "TALL_VAULT_SPEED_SCALE = 3.0D" in nav_src,
      "expected 'TALL_VAULT_SPEED_SCALE = 3.0D'")
check("old value 1.2D no longer the declaration",
      "TALL_VAULT_SPEED_SCALE = 1.2D" not in nav_src,
      "found stale 'TALL_VAULT_SPEED_SCALE = 1.2D' declaration")
check("TALL_VAULT_SPEED_SCALE > VAULT_SPEED_SCALE (0.8D)",
      "TALL_VAULT_SPEED_SCALE = 3.0D" in nav_src and "VAULT_SPEED_SCALE = 0.8D" in nav_src)
check("javadoc references CRON-136",
      re.search(r"CRON-136\s*[—-]\s*math-derived\s+value", nav_src) is not None,
      "expected 'CRON-136 — math-derived value' in javadoc")
check("javadoc references CRON-135 self-critique #2",
      re.search(r"CRON-135\s+self-critique\s+#2", nav_src) is not None,
      "expected 'CRON-135 self-critique #2' in javadoc")
check("javadoc contains warning-window math (LOOKAHEAD / cruiseSpeed = 7.5)",
      re.search(r"LOOKAHEAD\s*/\s*cruiseSpeed.*7\.5", nav_src, re.DOTALL) is not None,
      "expected 'LOOKAHEAD / cruiseSpeed ... 7.5' math derivation")
check("javadoc contains 9 blocks / 7.5 ticks = 1.2 blocks/tick",
      re.search(r"9\s+blocks.*7\.5\s+ticks.*1\.2\s+blocks/tick", nav_src, re.DOTALL) is not None,
      "expected '9 blocks ... 7.5 ticks ... 1.2 blocks/tick' math")
check("javadoc contains scale derivation 1.2 / 0.4 = 3.0",
      re.search(r"1\.2\s*/\s*0\.4.*3\.0", nav_src, re.DOTALL) is not None,
      "expected '1.2 / 0.4 ... 3.0' scale derivation")
check("javadoc notes old 1.2D was insufficient (0.48 blocks/tick)",
      "0.48" in nav_src and "1.2D" in nav_src,
      "expected reference to old 1.2D producing 0.48 blocks/tick")
check("javadoc notes 3.0D produces 1.2 blocks/tick (9.0 blocks in 7.5 ticks)",
      "9.0 blocks in 7.5 ticks" in nav_src,
      "expected '9.0 blocks in 7.5 ticks'")

# ──────────────────────────────────────────────────────────────────────────
# 2. DODGE_HEIGHT_SAMPLES constant
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] DODGE_HEIGHT_SAMPLES constant")
check("DODGE_HEIGHT_SAMPLES constant declared",
      "public static final double[] DODGE_HEIGHT_SAMPLES" in nav_src)
check("DODGE_HEIGHT_SAMPLES = {1.0D, 3.0D, 6.0D}",
      "DODGE_HEIGHT_SAMPLES = {1.0D, 3.0D, 6.0D}" in nav_src,
      "expected 'DODGE_HEIGHT_SAMPLES = {1.0D, 3.0D, 6.0D}'")
check("DODGE_HEIGHT_SAMPLES javadoc references CRON-136",
      re.search(r"CRON-136:\s*Height\s+samples.*dodge-path\s+probing", nav_src, re.DOTALL) is not None,
      "expected 'CRON-136: Height samples ... dodge-path probing' in javadoc")
check("DODGE_HEIGHT_SAMPLES javadoc references CRON-135 self-critique #4",
      re.search(r"CRON-135\s+self-critique\s+#4", nav_src) is not None,
      "expected 'CRON-135 self-critique #4' in javadoc")
check("DODGE_HEIGHT_SAMPLES javadoc lists three samples",
      "+1.0" in nav_src and "+3.0" in nav_src and "+6.0" in nav_src,
      "expected +1.0, +3.0, +6.0 sample descriptions")
check("DODGE_HEIGHT_SAMPLES javadoc explains +9 omission",
      "+9 is omitted" in nav_src,
      "expected explanation of why +9 is omitted")

# ──────────────────────────────────────────────────────────────────────────
# 3. isDodgeClear refactored to iterate DODGE_HEIGHT_SAMPLES
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] isDodgeClear refactored")
dodge_body = extract_method_body(nav_src, "private static boolean isDodgeClear")
check("isDodgeClear method exists",
      "private static boolean isDodgeClear" in nav_src)
check("isDodgeClear javadoc references CRON-136",
      re.search(r"CRON-136:\s*(?:</b>)?\s*Previously\s+probed\s+only\s+at\s+chest\s+height", nav_src) is not None,
      "expected 'CRON-136: Previously probed only at chest height' in javadoc")
check("isDodgeClear iterates DODGE_HEIGHT_SAMPLES",
      "for (double h : DODGE_HEIGHT_SAMPLES)" in dodge_body,
      "expected 'for (double h : DODGE_HEIGHT_SAMPLES)' in method body")
check("isDodgeClear no longer hardcodes Y+1.0 chest height",
      "+ 1.0D;  // chest height" not in dodge_body,
      "found stale 'Y+1.0D chest height' hardcode")
check("isDodgeClear returns false on first blocked height",
      "return false" in dodge_body and "isSolidRender" in dodge_body,
      "expected 'return false' after isSolidRender check")
check("isDodgeClear returns true at end (all clear)",
      "return true" in dodge_body and "all heights clear" in dodge_body,
      "expected 'return true' with 'all heights clear' comment")
check("isDodgeClear uses BlockPos.containing",
      "BlockPos.containing" in dodge_body,
      "expected BlockPos.containing in method body")
check("isDodgeClear uses isSolidRender",
      "isSolidRender" in dodge_body,
      "expected isSolidRender in method body")

# ──────────────────────────────────────────────────────────────────────────
# 4. Class-level algorithm javadoc step 4 mentions CRON-136
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] Class-level algorithm javadoc updated")
check("algorithm step 4 references CRON-136",
      re.search(r"CRON-136:\s*(?:</b>)?[\s*]*Each\s+dodge\s+direction\s+is\s+now\s+probed\s+at\s+THREE\s+heights", nav_src) is not None,
      "expected 'CRON-136: Each dodge direction is now probed at THREE heights'")
check("algorithm step 4 lists +1.0, +3.0, +6.0",
      re.search(r"chest\s+\+1\.0.*\+3\.0.*\+6\.0", nav_src, re.DOTALL) is not None,
      "expected 'chest +1.0 ... +3.0 ... +6.0' in step 4")
check("algorithm step 4 mentions dodge-INTO-tall-obstacle prevention",
      "prevent dodging" in nav_src and "tall obstacle" in nav_src,
      "expected 'prevent dodging ... tall obstacle' in step 4")
check("algorithm step 4 mentions 'clear at all three heights'",
      "clear at all three heights" in nav_src or "all three heights" in nav_src,
      "expected 'all three heights' in step 4")

# ──────────────────────────────────────────────────────────────────────────
# 5. No regression — CRON-135 constants unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] No regression — CRON-135 constants unchanged")
check("LOOKAHEAD = 3.0D unchanged",
      "LOOKAHEAD = 3.0D" in nav_src)
check("HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D} unchanged",
      "HEIGHT_SAMPLES = {0.5D, 1.0D, 1.5D}" in nav_src)
check("UPWARD_SAMPLES = {3.0D, 6.0D, 9.0D} unchanged",
      "UPWARD_SAMPLES = {3.0D, 6.0D, 9.0D}" in nav_src)
check("DODGE_PROBE_DIST = 2.0D unchanged",
      "DODGE_PROBE_DIST = 2.0D" in nav_src)
check("DODGE_SPEED_SCALE = 0.7D unchanged",
      "DODGE_SPEED_SCALE = 0.7D" in nav_src)
check("VAULT_SPEED_SCALE = 0.8D unchanged",
      "VAULT_SPEED_SCALE = 0.8D" in nav_src)
check("VAULT_FORWARD_SCALE = 0.3D unchanged",
      "VAULT_FORWARD_SCALE = 0.3D" in nav_src)
check("DODGE_UPWARD_BIAS_SCALE = 0.15D unchanged",
      "DODGE_UPWARD_BIAS_SCALE = 0.15D" in nav_src)
check("DODGE_FORWARD_BIAS_SCALE = 0.2D unchanged",
      "DODGE_FORWARD_BIAS_SCALE = 0.2D" in nav_src)

# ──────────────────────────────────────────────────────────────────────────
# 6. No regression — SteerResult API + helper methods
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] No regression — SteerResult API + helpers")
check("SteerResult.tallObstacle field present",
      "public final boolean tallObstacle;" in nav_src)
check("SteerResult 6-arg constructor present",
      re.search(r"public SteerResult\(Vec3 velocity, boolean blocked, boolean tallObstacle", nav_src) is not None)
check("isUpwardBlocked method present",
      "private static boolean isUpwardBlocked" in nav_src)
check("isForwardBlocked method present",
      "private static boolean isForwardBlocked" in nav_src)
check("computeSteer uses vaultScale = tallObstacle ? TALL : STANDARD",
      re.search(r"vaultScale\s*=\s*tallObstacle\s*\?\s*TALL_VAULT_SPEED_SCALE\s*:\s*VAULT_SPEED_SCALE", nav_src) is not None,
      "expected vaultScale ternary choosing TALL vs STANDARD")
check("computeSteer doubles upward bias when tallObstacle",
      re.search(r"tallObstacle\s*\?\s*flightSpeed\s*\*\s*DODGE_UPWARD_BIAS_SCALE\s*\*\s*2\.0D", nav_src) is not None,
      "expected tallObstacle ? flightSpeed * DODGE_UPWARD_BIAS_SCALE * 2.0D")

# ──────────────────────────────────────────────────────────────────────────
# 7. Canon fidelity markers
# ──────────────────────────────────────────────────────────────────────────
print("\n[7] Canon fidelity markers")
check("CRON-136 marker present in file",
      "CRON-136" in nav_src,
      "expected at least one CRON-136 reference")
check("仙逆 reference present",
      "仙逆" in nav_src,
      "expected 仙逆 canon reference")
check("no fabricated citations disclaimer present",
      "NO fabricated chapter citations" in nav_src or "NO fabricated" in nav_src,
      "expected no-fabricated-citations disclaimer")
check("web-search verification date 2026-07-27 present",
      "2026-07-27" in nav_src,
      "expected web-search verification date 2026-07-27")
check("mountainside reference present (canon case)",
      "mountainside" in nav_src.lower(),
      "expected 'mountainside' canon reference")

# ──────────────────────────────────────────────────────────────────────────
# 8. Build verification
# ──────────────────────────────────────────────────────────────────────────
print("\n[8] Build verification")
env = os.environ.copy()
env["JAVA_HOME"] = "/tmp/my-project/.jdks/jdk-17.0.13+11/"
try:
    result = subprocess.run(
        ["./gradlew", "compileJava"],
        cwd=str(PROJECT_ROOT),
        capture_output=True,
        text=True,
        env=env,
        timeout=120,
    )
    build_output = result.stdout + result.stderr
    check("gradlew compileJava exit code 0",
          result.returncode == 0,
          f"exit code {result.returncode}")
    check("BUILD SUCCESSFUL in output",
          "BUILD SUCCESSFUL" in build_output,
          "expected 'BUILD SUCCESSFUL' in gradle output")
    check("no 'error:' in build output",
          "error:" not in build_output,
          "found compilation error in build output")
except subprocess.TimeoutExpired:
    check("gradlew compileJava exit code 0", False, "timed out after 120s")
    check("BUILD SUCCESSFUL in output", False, "timed out")
    check("no 'error:' in build output", False, "timed out")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"CRON-136 verification: {PASS} pass, {FAIL} fail")
print(f"{'='*60}")
if FAIL > 0:
    print("\nFAILURES:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED")
    sys.exit(0)
