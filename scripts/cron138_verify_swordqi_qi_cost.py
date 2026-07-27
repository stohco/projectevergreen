#!/usr/bin/env python3
"""
CRON-138 verification script — qi cost for CultivatorSwordQiGoal.

Closes CRON-134 self-critique #8: 'No qi cost for OTHER cultivator abilities.
Sword flight now costs qi, but other qi-consuming abilities
(CultivatorSwordQiGoal ranged attack, casting, meditation) do NOT cost qi.'

CRON-138 adds:
  1. SWORD_QI_QI_COST = 5.0D constant in CultivatorSwordQiGoal.
  2. hasEnoughQiForSwordQi() helper in EntityCultivator (5% of maxQi gate).
  3. canUse() qi activation gate (refuses below 5% of maxQi).
  4. fireSwordQi() qi consumption at fire time (authoritative check).
  5. Abort-on-insufficient: no damage, no particle, no sound if qi insufficient.

Checks:
  1. SWORD_QI_QI_COST constant declared with correct value + javadoc.
  2. EntityCultivator.hasEnoughQiForSwordQi() method added.
  3. canUse() qi gate present and correctly placed.
  4. fireSwordQi() consumes qi + aborts on insufficient.
  5. start()/stop() reset qiGatePassedAtActivation.
  6. Canon fidelity: CRON-138 marker, 仙逆 reference, mod-original disclaimer,
     no fabricated citations, CRON-134 self-critique #8 reference.
  7. No regression: CRON-134 qi API unchanged (getQi, consumeQi, drainAllQi,
     hasEnoughQiForFlightActivation, hasEnoughQiForFlightTick, tickQi).
  8. No regression: existing sword-qi mechanics unchanged (damage scale 0.7,
     cooldown table, distance range 5-18, LOS check, particle trail).
  9. Build succeeds (./gradlew compileJava exit code 0).

Exit code: 0 if all checks pass, 1 otherwise.
"""

import os
import re
import subprocess
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/z/my-project/forge-mod")
GOAL = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/ai/CultivatorSwordQiGoal.java"
ENTITY = PROJECT_ROOT / "src/main/java/dev/ergenverse/entity/EntityCultivator.java"

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


goal_src = read(GOAL)
entity_src = read(ENTITY)

# ──────────────────────────────────────────────────────────────────────────
# 1. SWORD_QI_QI_COST constant
# ──────────────────────────────────────────────────────────────────────────
print("\n[1] SWORD_QI_QI_COST constant")
check("goal file exists", GOAL.exists())
check("SWORD_QI_QI_COST constant declared",
      "public static final double SWORD_QI_QI_COST" in goal_src)
check("SWORD_QI_QI_COST = 5.0D",
      "SWORD_QI_QI_COST = 5.0D" in goal_src,
      "expected 'SWORD_QI_QI_COST = 5.0D'")
check("SWORD_QI_QI_COST javadoc references CRON-138",
      re.search(r"CRON-138:\s*Qi\s+cost\s+per\s+sword-qi\s+projection", goal_src) is not None,
      "expected 'CRON-138: Qi cost per sword-qi projection' in javadoc")
check("javadoc lists Foundation (maxQi=100) → 20 shots",
      re.search(r"Foundation\s*\(maxQi=100\):\s*20\s+shots", goal_src) is not None,
      "expected Foundation shots calibration")
check("javadoc lists Core (maxQi=500) → 100 shots",
      re.search(r"Core\s*\(maxQi=500\):\s*100\s+shots", goal_src) is not None,
      "expected Core shots calibration")
check("javadoc lists Nascent Soul (maxQi=2000) → 400 shots",
      re.search(r"Nascent\s+Soul\s*\(maxQi=2000\):\s*400\s+shots", goal_src) is not None,
      "expected Nascent Soul shots calibration")
check("javadoc lists Soul+ (maxQi=10000) → 2000 shots",
      re.search(r"Soul\+\s*\(maxQi=10000\):\s*2000\s+shots", goal_src) is not None,
      "expected Soul+ shots calibration")
check("javadoc notes 2.5s flight equivalent",
      "2.5s of flight equivalent" in goal_src or "2.5s of flight" in goal_src,
      "expected flight-equivalent calibration note")

# ──────────────────────────────────────────────────────────────────────────
# 2. EntityCultivator.hasEnoughQiForSwordQi() method
# ──────────────────────────────────────────────────────────────────────────
print("\n[2] EntityCultivator.hasEnoughQiForSwordQi()")
check("entity file exists", ENTITY.exists())
check("hasEnoughQiForSwordQi method declared",
      "public boolean hasEnoughQiForSwordQi()" in entity_src,
      "expected 'public boolean hasEnoughQiForSwordQi()' method")
check("hasEnoughQiForSwordQi javadoc references CRON-138",
      re.search(r"CRON-138:\s*Returns\s+true\s+if\s+the\s+cultivator\s+has\s+enough\s+qi\s+to\s+PROJECT", entity_src) is not None,
      "expected 'CRON-138: Returns true if the cultivator has enough qi to PROJECT' in javadoc")
check("hasEnoughQiForSwordQi references CRON-134 self-critique #8",
      re.search(r"CRON-134\s+self-critique\s+#8", entity_src) is not None,
      "expected 'CRON-134 self-critique #8' reference")
check("hasEnoughQiForSwordQi uses 5% threshold",
      re.search(r"this\.qi\s*>=\s*this\.maxQi\s*\*\s*0\.05", entity_src) is not None,
      "expected 5% threshold check")
check("hasEnoughQiForSwordQi returns false when maxQi <= 0",
      re.search(r"if\s*\(this\.maxQi\s*<=\s*0\.0\)\s*return\s+false;", entity_src) is not None,
      "expected maxQi <= 0 guard")
check("hasEnoughQiForSwordQi references CultivatorSwordQiGoal",
      "CultivatorSwordQiGoal" in entity_src,
      "expected reference to CultivatorSwordQiGoal in javadoc")

# ──────────────────────────────────────────────────────────────────────────
# 3. canUse() qi gate
# ──────────────────────────────────────────────────────────────────────────
print("\n[3] canUse() qi gate")
canuse_body = extract_method_body(goal_src, "public boolean canUse()")
check("canUse method exists",
      "public boolean canUse()" in goal_src)
check("canUse checks hasEnoughQiForSwordQi",
      "hasEnoughQiForSwordQi()" in canuse_body,
      "expected 'hasEnoughQiForSwordQi()' call in canUse body")
check("canUse returns false when qi insufficient",
      re.search(r"if\s*\(!ec\.hasEnoughQiForSwordQi\(\)\)\s*\{[^}]*return\s+false;", canuse_body, re.DOTALL) is not None,
      "expected 'return false' when hasEnoughQiForSwordQi returns false")
check("canUse qi gate is instance-of EntityCultivator",
      "mob instanceof EntityCultivator ec" in canuse_body,
      "expected 'mob instanceof EntityCultivator ec' pattern")
check("canUse qi gate after realm check",
      canuse_body.find("realm < 1") < canuse_body.find("hasEnoughQiForSwordQi"),
      "expected qi gate AFTER realm check (realm first, then qi)")

# ──────────────────────────────────────────────────────────────────────────
# 4. fireSwordQi() qi consumption
# ──────────────────────────────────────────────────────────────────────────
print("\n[4] fireSwordQi() qi consumption")
fire_body = extract_method_body(goal_src, "private void fireSwordQi")
check("fireSwordQi method exists",
      "private void fireSwordQi" in goal_src)
check("fireSwordQi calls consumeQi(SWORD_QI_QI_COST)",
      "consumeQi(SWORD_QI_QI_COST)" in fire_body,
      "expected 'consumeQi(SWORD_QI_QI_COST)' call")
check("fireSwordQi aborts on insufficient qi (return before damage)",
      "if (!consumed)" in fire_body and fire_body.find("if (!consumed)") < fire_body.find("mobAttack"),
      "expected 'if (!consumed)' block with return before mobAttack damage")
check("fireSwordQi logs warn on insufficient qi",
      re.search(r"LOGGER\.warn.*qi\s+insufficient\s+at\s+fire\s+time", fire_body, re.DOTALL) is not None,
      "expected LOGGER.warn about qi insufficient at fire time")
check("fireSwordQi logs debug on successful consumption",
      re.search(r"LOGGER\.debug.*fired\s+sword-qi.*consumed", fire_body, re.DOTALL) is not None,
      "expected LOGGER.debug about successful consumption")
check("fireSwordQi uses String.format with Locale.ROOT for qi values",
      "String.format(java.util.Locale.ROOT" in fire_body,
      "expected String.format with Locale.ROOT (SLF4J doesn't support format specs)")
check("fireSwordQi consumption happens BEFORE damage application",
      fire_body.find("consumeQi") < fire_body.find("mobAttack"),
      "expected qi consumption BEFORE damage application")
check("fireSwordQi consumption happens BEFORE particle spawn",
      fire_body.find("consumeQi") < fire_body.find("sendParticles"),
      "expected qi consumption BEFORE particle spawn")
check("fireSwordQi consumption happens BEFORE sound",
      fire_body.find("consumeQi") < fire_body.find("playSound"),
      "expected qi consumption BEFORE sound")

# ──────────────────────────────────────────────────────────────────────────
# 5. start()/stop() reset qiGatePassedAtActivation
# ──────────────────────────────────────────────────────────────────────────
print("\n[5] start()/stop() qiGatePassedAtActivation")
check("qiGatePassedAtActivation field declared",
      "private boolean qiGatePassedAtActivation" in goal_src,
      "expected 'private boolean qiGatePassedAtActivation' field")
check("start() sets qiGatePassedAtActivation = true",
      "qiGatePassedAtActivation = true" in goal_src,
      "expected 'qiGatePassedAtActivation = true' in start()")
check("stop() sets qiGatePassedAtActivation = false",
      "qiGatePassedAtActivation = false" in goal_src,
      "expected 'qiGatePassedAtActivation = false' in stop()")

# ──────────────────────────────────────────────────────────────────────────
# 6. Canon fidelity markers
# ──────────────────────────────────────────────────────────────────────────
print("\n[6] Canon fidelity markers")
check("CRON-138 marker present in goal",
      "CRON-138" in goal_src,
      "expected CRON-138 marker in CultivatorSwordQiGoal")
check("CRON-138 marker present in entity",
      "CRON-138" in entity_src,
      "expected CRON-138 marker in EntityCultivator")
check("仙逆 reference present",
      "仙逆" in goal_src or "仙逆" in entity_src,
      "expected 仙逆 canon reference")
check("mod-original disclaimer present",
      "mod-original" in goal_src.lower(),
      "expected mod-original disclaimer")
check("no fabricated citations disclaimer",
      "NO explicit" in goal_src or "NO canon citation" in goal_src or "NO fabricated" in goal_src,
      "expected no-fabricated-citations disclaimer")
check("CRON-134 self-critique #8 reference in goal javadoc",
      re.search(r"CRON-134\s+self-critique\s+#8", goal_src) is not None,
      "expected CRON-134 self-critique #8 reference in goal")
check("剑气 (sword-qi) reference present",
      "剑气" in goal_src or "剑气" in entity_src,
      "expected 剑气 (sword-qi) canon reference")
check("真元 (zhenyuan) reference present",
      "真元" in goal_src,
      "expected 真元 (true essence) reference")

# ──────────────────────────────────────────────────────────────────────────
# 7. No regression — CRON-134 qi API unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[7] No regression — CRON-134 qi API unchanged")
check("getQi() method present",
      "public double getQi()" in entity_src)
check("getMaxQi() method present",
      "public double getMaxQi()" in entity_src)
check("getQiFraction() method present",
      "public double getQiFraction()" in entity_src)
check("setQi(double) method present",
      "public void setQi(double amount)" in entity_src)
check("consumeQi(double) method present",
      "public boolean consumeQi(double absoluteAmount)" in entity_src)
check("drainAllQi() method present",
      "public void drainAllQi()" in entity_src)
check("hasEnoughQiForFlightActivation() method present",
      "public boolean hasEnoughQiForFlightActivation()" in entity_src)
check("hasEnoughQiForFlightTick() method present",
      "public boolean hasEnoughQiForFlightTick()" in entity_src)
check("tickQi() method present",
      "public void tickQi()" in entity_src)
check("initializeQiForRealm() method present",
      "private void initializeQiForRealm()" in entity_src)

# ──────────────────────────────────────────────────────────────────────────
# 8. No regression — existing sword-qi mechanics unchanged
# ──────────────────────────────────────────────────────────────────────────
print("\n[8] No regression — existing sword-qi mechanics")
check("damage multiplier 0.7F unchanged",
      "* 0.7F" in goal_src,
      "expected ranged damage = 70% melee")
check("distance range 5-18 blocks unchanged",
      "distSq < 25.0D || distSq > 324.0D" in goal_src,
      "expected distance range 5-18 blocks (25-324 sq)")
check("charge timer 10 ticks unchanged",
      "chargeTimer = 10" in goal_src,
      "expected 10-tick charge timer")
check("cooldown table unchanged",
      "getCooldownForRealm" in goal_src and "realmOrdinal <= 1" in goal_src,
      "expected cooldown table")
check("LOS check via ClipContext unchanged",
      "ClipContext" in goal_src and "HitResult.Type.MISS" in goal_src,
      "expected LOS check via ClipContext")
check("particle trail (SWEEP_ATTACK + CRIT) unchanged",
      "ParticleTypes.SWEEP_ATTACK" in goal_src and "ParticleTypes.CRIT" in goal_src,
      "expected SWEEP_ATTACK + CRIT particle trail")
check("sword-swish sound unchanged",
      "PLAYER_ATTACK_SWEEP" in goal_src,
      "expected PLAYER_ATTACK_SWEEP sound")
check("realm ordinal via RealmId.valueOf unchanged",
      "RealmId.valueOf" in goal_src,
      "expected RealmId.valueOf parsing")
check("knockback on hit unchanged",
      "target.push" in goal_src,
      "expected target.push knockback")

# ──────────────────────────────────────────────────────────────────────────
# 9. Build verification
# ──────────────────────────────────────────────────────────────────────────
print("\n[9] Build verification")
env = os.environ.copy()
env["JAVA_HOME"] = "/tmp/my-project/.jdks/jdk-17.0.13+11/"
try:
    result = subprocess.run(
        ["./gradlew", "compileJava"],
        cwd=str(PROJECT_ROOT),
        capture_output=True,
        text=True,
        env=env,
        timeout=180,
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
    check("gradlew compileJava exit code 0", False, "timed out after 180s")
    check("BUILD SUCCESSFUL in output", False, "timed out")
    check("no 'error:' in build output", False, "timed out")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print(f"\n{'='*60}")
print(f"CRON-138 verification: {PASS} pass, {FAIL} fail")
print(f"{'='*60}")
if FAIL > 0:
    print("\nFAILURES:")
    for e in ERRORS:
        print(f"  - {e}")
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED")
    sys.exit(0)
