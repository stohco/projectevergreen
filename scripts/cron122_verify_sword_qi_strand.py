#!/usr/bin/env python3
"""
CRON-122 verification script.

Verifies the canon-faithful strand-type differentiation of the Sword Qi Strand
item (FLESH vs SOUL_GUARD), implemented per the Baidu Baike 王平 entry:
  "一道化作王平的血肉之躯，另一道守护其魂魄"

Checks:
  1. SwordQiStrandItem.java — StrandType enum, NBT tags, tooltip, Javadoc
  2. ErgenverseItems.java — stacksTo(1) (prevents same-type stacking bypass)
  3. LingTianhouSwordQiGrantEvent.java — grants 1 FLESH + 1 SOUL_GUARD
  4. CultivationPlanetCrystalBlock.java — hasOneFleshAndOneSoulGuardStrand
     + consumeOneFleshAndOneSoulGuardStrand (replaces count>=2 + consume(2))
  5. WangPingRedemptionEvent.java — Javadoc references the new prerequisite

Exit code 0 = all checks pass; non-zero = some check failed.
"""
import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
SRC = ROOT / "src/main/java/dev/ergenverse"

PASS = 0
FAIL = 0
CHECKS = []


def check(name: str, condition: bool, detail: str = "") -> None:
    global PASS, FAIL
    if condition:
        PASS += 1
        CHECKS.append(f"  PASS  {name}")
    else:
        FAIL += 1
        CHECKS.append(f"  FAIL  {name}  {detail}")


def read(p: Path) -> str:
    return p.read_text(encoding="utf-8")


# ──────────────────────────────────────────────────────────────────────────
# 1. SwordQiStrandItem.java
# ──────────────────────────────────────────────────────────────────────────
sqi_path = SRC / "item/SwordQiStrandItem.java"
sqi = read(sqi_path)

check("1.1 SwordQiStrandItem.java exists", sqi_path.exists())
check("1.2 StrandType enum declared", "public enum StrandType" in sqi,
      "StrandType enum not found")
check("1.3 FLESH variant declared", "FLESH(" in sqi, "FLESH variant missing")
check("1.4 SOUL_GUARD variant declared", "SOUL_GUARD(" in sqi, "SOUL_GUARD variant missing")
check("1.5 NBT_STRAND_TYPE constant declared",
      'public static final String NBT_STRAND_TYPE = "Ergen.SwordQi.StrandType";' in sqi,
      "NBT_STRAND_TYPE constant missing")
check("1.6 setStrandType method declared",
      "public void setStrandType(ItemStack stack, StrandType type)" in sqi,
      "setStrandType method missing")
check("1.7 getStrandType method declared",
      "public StrandType getStrandType(ItemStack stack)" in sqi,
      "getStrandType method missing")
check("1.8 FLESH tooltip cites canon '血肉之躯'",
      "血肉之躯" in sqi, "FLESH tooltip missing canon citation")
check("1.9 SOUL_GUARD tooltip cites canon '守护其魂魄'",
      "守护其魂魄" in sqi, "SOUL_GUARD tooltip missing canon citation")
check("1.10 Baidu Baike 王平 source cited",
      "https://baike.baidu.com/item/王平/62563845" in sqi,
      "Baidu Baike 王平 source not cited")
check("1.11 CRON-122 reference in Javadoc",
      "CRON-122" in sqi, "CRON-122 not referenced in Javadoc")
check("1.12 Old 'UNVERIFIED' claim retracted",
      # The word 'UNVERIFIED' may appear in the historical context describing the prior state.
      # We just need to verify the file no longer CLAIMS the differentiation is unverified.
      'is UNVERIFIED' not in sqi and 'marked the differentiation as UNVERIFIED' in sqi,
      "Stale 'UNVERIFIED' claim still present without historical retraction context")
check("1.13 Canon differentiation documented in class Javadoc",
      "StrandType#FLESH" in sqi and "StrandType#SOUL_GUARD" in sqi,
      "StrandType Javadoc links missing")
check("1.14 getStrandType falls back to FLESH on missing tag",
      "return StrandType.FLESH;" in sqi,
      "Defensive fallback to FLESH missing")
check("1.15 Tooltip displays strand type via type.tooltipLineCn()",
      "type.tooltipLineCn()" in sqi and "type.tooltipLineEn()" in sqi,
      "Tooltip doesn't display strand type")

# ──────────────────────────────────────────────────────────────────────────
# 2. ErgenverseItems.java — stacksTo(1)
# ──────────────────────────────────────────────────────────────────────────
items_path = SRC / "item/ErgenverseItems.java"
items = read(items_path)

# Find the SWORD_QI_STRAND registration block — the regex must allow
# nested parentheses inside the lambda.
m = re.search(
    r'RegistryObject<SwordQiStrandItem> SWORD_QI_STRAND\s*=\s*ITEMS\.register\([\s\S]*?stacksTo\((\d+)\)',
    items)
check("2.1 SWORD_QI_STRAND registration found", m is not None,
      "SWORD_QI_STRAND registration block not found")
if m:
    stack_size = int(m.group(1))
    check("2.2 stacksTo(1) (lowered from 2)", stack_size == 1,
          f"stacksTo({stack_size}) — expected 1")
check("2.3 CRON-122 reference in items comment",
      "CRON-122" in items, "CRON-122 not referenced in items comment")
check("2.4 stacksTo rationale documented",
      "prevents a creative-mode player" in items or "stack-count checks" in items,
      "stacksTo(1) rationale not documented")

# ──────────────────────────────────────────────────────────────────────────
# 3. LingTianhouSwordQiGrantEvent.java — grants 1 FLESH + 1 SOUL_GUARD
# ──────────────────────────────────────────────────────────────────────────
grant_path = SRC / "wanglin/bead/LingTianhouSwordQiGrantEvent.java"
grant = read(grant_path)

check("3.1 Grant event file exists", grant_path.exists())
check("3.2 setStrandType called in grant loop",
      "setStrandType" in grant, "setStrandType not called in grant loop")
check("3.3 Strand 1 → FLESH pairing",
      "StrandType.FLESH" in grant, "FLESH strand type not assigned")
check("3.4 Strand 2 → SOUL_GUARD pairing",
      "StrandType.SOUL_GUARD" in grant, "SOUL_GUARD strand type not assigned")
check("3.5 Conditional strand-type assignment",
      re.search(r"i == 1\s*\)?\s*\n\s*\?\s*(?:SwordQiStrandItem\.)?StrandType\.FLESH\s*\n\s*:\s*(?:SwordQiStrandItem\.)?StrandType\.SOUL_GUARD", grant) is not None
      or re.search(r"i == 1\s*\?\s*(?:SwordQiStrandItem\.)?StrandType\.FLESH\s*:\s*(?:SwordQiStrandItem\.)?StrandType\.SOUL_GUARD", grant) is not None,
      "Conditional (i==1 ? FLESH : SOUL_GUARD) assignment not found")
check("3.6 CRON-122 reference in grant Javadoc",
      "CRON-122" in grant, "CRON-122 not referenced in grant Javadoc")
check("3.7 Grant state transition diagram updated",
      "+1 FLESH strand + 1 SOUL_GUARD strand" in grant,
      "State transition diagram not updated")
check("3.8 Grant inventory list shows strand types",
      "strand_type=FLESH" in grant and "strand_type=SOUL_GUARD" in grant,
      "Grant inventory list doesn't show strand types")

# ──────────────────────────────────────────────────────────────────────────
# 4. CultivationPlanetCrystalBlock.java — prerequisite + consumer
# ──────────────────────────────────────────────────────────────────────────
crystal_path = SRC / "block/CultivationPlanetCrystalBlock.java"
crystal = read(crystal_path)

check("4.1 Crystal block file exists", crystal_path.exists())
check("4.2 hasOneFleshAndOneSoulGuardStrand method declared",
      "private static boolean hasOneFleshAndOneSoulGuardStrand" in crystal,
      "hasOneFleshAndOneSoulGuardStrand method missing")
check("4.3 consumeOneFleshAndOneSoulGuardStrand method declared",
      "private static void consumeOneFleshAndOneSoulGuardStrand" in crystal,
      "consumeOneFleshAndOneSoulGuardStrand method missing")
check("4.4 New prerequisite check used in use() method",
      "hasOneFleshAndOneSoulGuardStrand(serverPlayer)" in crystal,
      "New prerequisite check not used in use()")
check("4.5 New consumer called in use() method",
      "consumeOneFleshAndOneSoulGuardStrand(serverPlayer)" in crystal,
      "New consumer not called in use()")
check("4.6 Old 'countSwordQiStrands >= 2' check removed from use()",
      "countSwordQiStrands(serverPlayer) >= 2" not in crystal
      or "swordQiCount >= 2" not in crystal,
      "Old 'count >= 2' check still present in use()")
check("4.7 Old 'consumeSwordQiStrands(serverPlayer, 2)' call removed from use()",
      "consumeSwordQiStrands(serverPlayer, 2)" not in crystal,
      "Old 'consumeSwordQiStrands(player, 2)' call still present in use()")
check("4.8 SwordQiStrandItem imported",
      "import dev.ergenverse.item.SwordQiStrandItem;" in crystal,
      "SwordQiStrandItem import missing")
check("4.9 CRON-122 reference in crystal block",
      "CRON-122" in crystal, "CRON-122 not referenced in crystal block")
check("4.10 Canon citation in prerequisite Javadoc",
      "一道化作王平的血肉之躯" in crystal,
      "Canon citation missing in prerequisite Javadoc")
check("4.11 Both StrandType.FLESH and SOUL_GUARD checked in hasOneFlesh...",
      crystal.count("StrandType.FLESH") >= 3 and crystal.count("StrandType.SOUL_GUARD") >= 3,
      "StrandType constants not used in hasOneFlesh/consumeOne methods")
check("4.12 Old countSwordQiStrands method retained (for debug command)",
      "private static int countSwordQiStrands" in crystal,
      "Old countSwordQiStrands method removed (should be retained for debug)")
check("4.13 Old countSwordQiStrands Javadoc notes CRON-122 retention",
      "CRON-122" in crystal and "retained" in crystal,
      "Old method Javadoc doesn't note CRON-122 retention reason")
check("4.14 Defensive warning log in consumeOneFlesh...",
      "could not consume both strand types" in crystal,
      "Defensive warning log missing in consumeOneFleshAndOneSoulGuardStrand")

# ──────────────────────────────────────────────────────────────────────────
# 5. WangPingRedemptionEvent.java — Javadoc accuracy
# ──────────────────────────────────────────────────────────────────────────
redeem_path = SRC / "wanglin/bead/WangPingRedemptionEvent.java"
redeem = read(redeem_path)

check("5.1 WangPingRedemptionEvent file exists", redeem_path.exists())
check("5.2 Javadoc references '1 FLESH + 1 SOUL_GUARD'",
      "1 FLESH + 1 SOUL_GUARD" in redeem or "1 FLESH strand AND 1 SOUL_GUARD strand" in redeem,
      "Javadoc doesn't reference 1 FLESH + 1 SOUL_GUARD prerequisite")
check("5.3 CRON-122 reference in redemption Javadoc",
      "CRON-122" in redeem, "CRON-122 not referenced in redemption Javadoc")
check("5.4 Baidu Baike 王平 citation in redemption Javadoc",
      "https://baike.baidu.com/item/王平/62563845" in redeem,
      "Baidu Baike 王平 citation missing in redemption Javadoc")
check("5.5 Stale '合体' realm reference removed",
      "ASCENDANT maps to 合体" not in redeem,
      "Stale 'ASCENDANT maps to 合体' reference still present")
check("5.6 CRON-119 realm correction referenced",
      "CRON-119" in redeem and "问鼎" in redeem,
      "CRON-119 问鼎 realm correction not referenced")
# The phrase '≥2 sword qi strands' may appear in the CRON-122 historical context
# ("the prior CRON-118 check was '≥2 sword qi strands of any type'. This incorrectly...").
# That's correct context. We just need to verify the CURRENT prerequisite description
# doesn't use the old phrasing as the current requirement.
check("5.7 Old '≥2 sword qi strands' phrasing removed from current prerequisite",
      # The current prerequisite must be '1 FLESH + 1 SOUL_GUARD', not '≥2 sword qi strands'.
      "Player has exactly 1 FLESH strand AND 1 SOUL_GUARD strand" in redeem,
      "Current prerequisite doesn't use '1 FLESH + 1 SOUL_GUARD' phrasing")
check("5.8 Old 'life-saving treasure given to disciple' UNVERIFIED claim retracted",
      "UNVERIFIED claim from a research subagent and is now retracted" in redeem,
      "Old UNVERIFIED claim not explicitly retracted")

# ──────────────────────────────────────────────────────────────────────────
# Summary
# ──────────────────────────────────────────────────────────────────────────
print()
print("=" * 70)
print("CRON-122 verification — Sword Qi Strand FLESH vs SOUL_GUARD")
print("=" * 70)
for c in CHECKS:
    print(c)
print()
print(f"Total: {PASS} pass, {FAIL} fail (out of {PASS + FAIL})")
print()
if FAIL == 0:
    print("RESULT: ALL CHECKS PASS")
    sys.exit(0)
else:
    print(f"RESULT: {FAIL} CHECK(S) FAILED")
    sys.exit(1)
