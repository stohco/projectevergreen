#!/usr/bin/env python3
"""
CRON-134 verification script — Qi expenditure for cultivator sword-flight.

Verifies:
  1. EntityCultivator has the qi field + maxQi field + qiInitialized field.
  2. EntityCultivator has qi accessors: getQi, getMaxQi, getQiFraction, setQi,
     consumeQi, drainAllQi, hasEnoughQiForFlightActivation, hasEnoughQiForFlightTick.
  3. EntityCultivator has initializeQiForRealm + tickQi methods.
  4. initializeQiForRealm sets maxQi per-realm: mortal=0, foundation=100,
     core=500, nascent=2000, soul+=10000.
  5. tickQi regenerates 1.0/sec when not flying, no regen when flying.
  6. Qi is persisted to NBT (Qi, MaxQi, QiInitialized keys).
  7. tickQi is called from aiStep() (active branch, not hibernate branch).
  8. CultivatorFlightGoal has FLIGHT_QI_COST_PER_TICK = 0.2D.
  9. CultivatorFlightGoal.canUse() checks hasEnoughQiForFlightActivation.
 10. CultivatorFlightGoal.canContinueToUse() checks hasEnoughQiForFlightTick
     and forces landing on qi exhaustion.
 11. CultivatorFlightGoal.tick() consumes qi each tick and drains on
     insufficient qi.
 12. CultivatorFlightGoal.start() and stop() reset qiConsumedThisFlight.
 13. Canon fidelity markers in javadoc (web-search date, no fabricated
     citations, CRON-134 marker).
 14. No regression: CRON-130/133 constants unchanged.
 15. Build succeeds (./gradlew compileJava exit code 0).

Exit code: 0 if all checks pass, 1 otherwise.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/z/my-project/forge-mod")
ENTITY = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/EntityCultivator.java"
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


# ──────────────────────────────────────────────────────────────────────────
# 1. EntityCultivator — qi fields
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] EntityCultivator — qi fields")
entity_src = read(ENTITY)
check("file exists", ENTITY.exists())
check("private double qi field",
      re.search(r"private\s+double\s+qi\s*=\s*0\.0\s*;", entity_src) is not None)
check("private double maxQi field",
      re.search(r"private\s+double\s+maxQi\s*=\s*0\.0\s*;", entity_src) is not None)
check("private int qiTickCounter field",
      re.search(r"private\s+int\s+qiTickCounter\s*=\s*0\s*;", entity_src) is not None)
check("private boolean qiInitialized field",
      re.search(r"private\s+boolean\s+qiInitialized\s*=\s*false\s*;", entity_src) is not None)
check("CRON-134 marker in field comment block",
      "CRON-134: Qi (灵气) reserves for cultivator NPCs" in entity_src)

# ──────────────────────────────────────────────────────────────────────────
# 2. EntityCultivator — qi accessors
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] EntityCultivator — qi accessors")
check("public double getQi()", "public double getQi()" in entity_src)
check("public double getMaxQi()", "public double getMaxQi()" in entity_src)
check("public double getQiFraction()", "public double getQiFraction()" in entity_src)
check("public void setQi(double)", "public void setQi(double amount)" in entity_src)
check("public boolean consumeQi(double)", "public boolean consumeQi(double absoluteAmount)" in entity_src)
check("public void drainAllQi()", "public void drainAllQi()" in entity_src)
check("public boolean hasEnoughQiForFlightActivation()",
      "public boolean hasEnoughQiForFlightActivation()" in entity_src)
check("public boolean hasEnoughQiForFlightTick()",
      "public boolean hasEnoughQiForFlightTick()" in entity_src)

# ──────────────────────────────────────────────────────────────────────────
# 3. EntityCultivator — initializeQiForRealm + tickQi
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] EntityCultivator — initializeQiForRealm + tickQi")
check("private void initializeQiForRealm()", "private void initializeQiForRealm()" in entity_src)
check("public void tickQi()", "public void tickQi()" in entity_src)

# ──────────────────────────────────────────────────────────────────────────
# 4. initializeQiForRealm — maxQi per realm
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] initializeQiForRealm — maxQi per realm")
init_body = extract_method_body(entity_src, "private void initializeQiForRealm()")
check("initializeQiForRealm body extracted", len(init_body) > 0)
if init_body:
    check("mortal/qi_condensation → maxQi = 0.0",
          "this.maxQi = 0.0" in init_body)
    check("foundation → maxQi = 100.0",
          re.search(r'foundation.*?this\.maxQi\s*=\s*100\.0', init_body, re.DOTALL) is not None
          or re.search(r'this\.maxQi\s*=\s*100\.0.*?foundation', init_body, re.DOTALL) is not None
          or "this.maxQi = 100.0" in init_body)
    check("core → maxQi = 500.0",
          "this.maxQi = 500.0" in init_body)
    check("nascent → maxQi = 2000.0",
          "this.maxQi = 2000.0" in init_body)
    check("soul+ → maxQi = 10000.0",
          "this.maxQi = 10000.0" in init_body)
    check("spawns at full qi (qi = maxQi)",
          "this.qi = this.maxQi" in init_body)
    check("sets qiInitialized = true",
          "this.qiInitialized = true" in init_body)

# ──────────────────────────────────────────────────────────────────────────
# 5. tickQi — regen logic
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] tickQi — regen logic")
tick_qi_body = extract_method_body(entity_src, "public void tickQi()")
check("tickQi body extracted", len(tick_qi_body) > 0)
if tick_qi_body:
    check("client-side guard", "isClientSide" in tick_qi_body)
    check("lazy init guard", "if (!this.qiInitialized)" in tick_qi_body)
    check("mortal/qi-condensation early return", "this.maxQi <= 0.0" in tick_qi_body)
    check("no regen during flight", "isFlying()" in tick_qi_body)
    check("20-tick throttle (qiTickCounter < 20)",
          "qiTickCounter < 20" in tick_qi_body)
    check("regen rate = 1.0/sec", "this.qi + 1.0" in tick_qi_body)
    check("clamp to maxQi", "Math.min(this.maxQi" in tick_qi_body)

# ──────────────────────────────────────────────────────────────────────────
# 6. NBT persistence
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] NBT persistence")
save_body = extract_method_body(entity_src, "public void addAdditionalSaveData(CompoundTag compound)")
check("addAdditionalSaveData body extracted", len(save_body) > 0)
if save_body:
    check("saves Qi to NBT", 'compound.putDouble("Qi"' in save_body)
    check("saves MaxQi to NBT", 'compound.putDouble("MaxQi"' in save_body)
    check("saves QiInitialized to NBT", 'compound.putBoolean("QiInitialized"' in save_body)

load_body = extract_method_body(entity_src, "public void readAdditionalSaveData(CompoundTag compound)")
check("readAdditionalSaveData body extracted", len(load_body) > 0)
if load_body:
    check("loads Qi from NBT (backward-compat contains check)",
          'compound.contains("Qi")' in load_body)
    check("loads MaxQi from NBT", 'compound.contains("MaxQi")' in load_body)
    check("loads QiInitialized from NBT", 'compound.contains("QiInitialized")' in load_body)

# ──────────────────────────────────────────────────────────────────────────
# 7. tickQi called from aiStep (active branch)
# ──────────────────────────────────────────────────────────────────────────
print("\n[7] aiStep wiring")
aistep_body = extract_method_body(entity_src, "public void aiStep()")
check("aiStep body extracted", len(aistep_body) > 0)
if aistep_body:
    check("aiStep calls tickQi()", "tickQi()" in aistep_body)
    check("tickQi call is AFTER hibernate-branch return (not in hibernate branch)",
          # The hibernate branch returns early; tickQi() should be after that.
          aistep_body.find("tickQi()") > aistep_body.find("return;"))

# ──────────────────────────────────────────────────────────────────────────
# 8. CultivatorFlightGoal — FLIGHT_QI_COST_PER_TICK
# ──────────────────────────────────────────────────────────────────────────
print("\n[8] CultivatorFlightGoal — FLIGHT_QI_COST_PER_TICK")
goal_src = read(GOAL)
check("FLIGHT_QI_COST_PER_TICK constant declared",
      "private static final double FLIGHT_QI_COST_PER_TICK" in goal_src)
check("FLIGHT_QI_COST_PER_TICK = 0.2D",
      "FLIGHT_QI_COST_PER_TICK = 0.2D" in goal_src)
check("qiConsumedThisFlight field declared",
      "private double qiConsumedThisFlight" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 9. canUse() — qi activation gate
# ──────────────────────────────────────────────────────────────────────────
print("\n[9] canUse() — qi activation gate")
canuse_body = extract_method_body(goal_src, "public boolean canUse()")
check("canUse body extracted", len(canuse_body) > 0)
if canuse_body:
    check("canUse calls hasEnoughQiForFlightActivation()",
          "hasEnoughQiForFlightActivation()" in canuse_body)
    check("canUse returns false on insufficient qi",
          re.search(r"if\s*\(\s*!cultivator\.hasEnoughQiForFlightActivation\s*\(\s*\)\s*\)\s*return\s+false\s*;", canuse_body) is not None)
    check("qi check is BEFORE higher-priority pose check (realm first, then qi)",
          # Realm check comes before qi check (per source order)
          canuse_body.find("isFoundationOrHigher") < canuse_body.find("hasEnoughQiForFlightActivation"))

# ──────────────────────────────────────────────────────────────────────────
# 10. canContinueToUse() — qi abort
# ──────────────────────────────────────────────────────────────────────────
print("\n[10] canContinueToUse() — qi abort")
cancont_body = extract_method_body(goal_src, "public boolean canContinueToUse()")
check("canContinueToUse body extracted", len(cancont_body) > 0)
if cancont_body:
    check("canContinueToUse calls hasEnoughQiForFlightTick()",
          "hasEnoughQiForFlightTick()" in cancont_body)
    check("canContinueToUse returns false on qi exhaustion",
          re.search(r"if\s*\(\s*!cultivator\.hasEnoughQiForFlightTick\s*\(\s*\)\s*\)\s*\{.*?return\s+false\s*;", cancont_body, re.DOTALL) is not None)
    check("canContinueToUse logs qi-exhaustion warning",
          "qi exhausted" in cancont_body and "LOGGER.warn" in cancont_body)

# ──────────────────────────────────────────────────────────────────────────
# 11. tick() — qi consumption
# ──────────────────────────────────────────────────────────────────────────
print("\n[11] tick() — qi consumption")
tick_body = extract_method_body(goal_src, "public void tick()")
check("tick body extracted", len(tick_body) > 0)
if tick_body:
    check("tick calls consumeQi(FLIGHT_QI_COST_PER_TICK)",
          "cultivator.consumeQi(FLIGHT_QI_COST_PER_TICK)" in tick_body)
    check("tick accumulates qiConsumedThisFlight on success",
          "qiConsumedThisFlight += FLIGHT_QI_COST_PER_TICK" in tick_body)
    check("tick drains remaining qi on insufficient (drainAllQi)",
          "cultivator.drainAllQi()" in tick_body)
    check("tick logs insufficient-qi warning",
          "qi insufficient" in tick_body and "LOGGER.warn" in tick_body)
    check("qi consumption is FIRST in tick (before navigation)",
          tick_body.find("consumeQi") < tick_body.find("computeSteer"))

# ──────────────────────────────────────────────────────────────────────────
# 12. start() and stop() reset qiConsumedThisFlight
# ──────────────────────────────────────────────────────────────────────────
print("\n[12] start() and stop() reset qiConsumedThisFlight")
start_body = extract_method_body(goal_src, "public void start()")
check("start body extracted", len(start_body) > 0)
if start_body:
    check("start resets qiConsumedThisFlight = 0.0",
          "qiConsumedThisFlight = 0.0" in start_body)
    check("start logs qi at takeoff (getQi, getMaxQi, getQiFraction)",
          "cultivator.getQi()" in start_body and "cultivator.getMaxQi()" in start_body
          and "cultivator.getQiFraction()" in start_body)

stop_body = extract_method_body(goal_src, "public void stop()")
check("stop body extracted", len(stop_body) > 0)
if stop_body:
    check("stop resets qiConsumedThisFlight = 0.0",
          "qiConsumedThisFlight = 0.0" in stop_body)
    check("stop logs qi consumed on landing (qiConsumedThisFlight > 0 check)",
          "qiConsumedThisFlight > 0.0" in stop_body)

# ──────────────────────────────────────────────────────────────────────────
# 13. Canon fidelity markers
# ──────────────────────────────────────────────────────────────────────────
print("\n[13] Canon fidelity markers")
check("EntityCultivator CRON-134 marker",
      "CRON-134: Qi (灵气) reserves" in entity_src)
check("EntityCultivator web-search date 2026-07-27",
      "2026-07-27" in entity_src)
check("EntityCultivator mod-original disclaimer (genre convention)",
      "mod-original" in entity_src and "genre convention" in entity_src)
check("EntityCultivator no-fabricated-citations disclaimer",
      re.search(r"NO\s+explicit\s+仙逆\s+chapter\s+citation", entity_src) is not None)
check("Goal CRON-134 marker in javadoc",
      "CRON-134 — Qi expenditure is no longer future work" in goal_src)
check("Goal mod-original disclaimer",
      "mod-original interpretation grounded in genre convention" in goal_src)
check("Goal references 御剑飞行 canon",
      "御剑飞行" in goal_src or "xianxia genre convention" in goal_src)
check("Goal references 4-round carried-over self-critique",
      "CRON-130 #5" in goal_src and "CRON-132 #5" in goal_src and "CRON-133 #7" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 14. No regression: CRON-130/133 constants unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[14] No regression: CRON-130/133 constants unchanged")
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
check("MAX_BLOCKED_TICKS = 100 (CRON-133 unchanged)",
      "private static final int MAX_BLOCKED_TICKS = 100;" in goal_src)
check("CultivatorFlightNavigator import unchanged (CRON-133)",
      "import dev.ergenverse.entity.control.CultivatorFlightNavigator;" in goal_src)
check("Navigator.computeSteer call still in tick (CRON-133)",
      "CultivatorFlightNavigator.computeSteer(" in goal_src)

# ──────────────────────────────────────────────────────────────────────────
# 15. Build verification
# ──────────────────────────────────────────────────────────────────────────
print("\n[15] Build verification")
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
print(f"CRON-134 VERIFICATION: {PASS} pass, {FAIL} fail")
print("=" * 60)
if FAIL > 0:
    print("\nFAILED CHECKS:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
sys.exit(0)
