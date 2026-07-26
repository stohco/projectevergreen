#!/usr/bin/env python3
"""
CRON-124 verification script.

Asserts that the Wang Ping mortal-life arc (二次化凡) is correctly wired:
  1. HeavenDefyingBeadItem NBT_WANG_PING_SOUL + NBT_QING_YI_SOUL flags
  2. HeavenDefyingBeadItem hasWangPingSoul / setWangPingSoul accessors
  3. HeavenDefyingBeadItem hasQingYiSoul / setQingYiSoul accessors
  4. WangPingMortalArcEvent.java exists with 5 stages + 2 entry points
  5. HistoryEvents.java dispatches to WangPingMortalArcEvent for both
     wang_ping and qing_yi characterIds
  6. Canon citations are present (Ch 693, Ch 700, Baidu Baike 王平, Baidu
     Baike 青宜, Baidu Baike 仙逆编年史, Zhihu timeline, Sohu deal)

Run: python3 /home/z/my-project/forge-mod/scripts/cron124_verify_wang_ping_arc.py
"""

import re
import sys
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod")
BEAD = ROOT / "src/main/java/dev/ergenverse/wanglin/bead"
HISTORY = ROOT / "src/main/java/dev/ergenverse/history"
RUNTIME = ROOT / "src/main/java/dev/ergenverse/runtime"

bead_path = BEAD / "HeavenDefyingBeadItem.java"
event_path = BEAD / "WangPingMortalArcEvent.java"
history_events_path = HISTORY / "HistoryEvents.java"

bead_src = bead_path.read_text(encoding="utf-8")
event_src = event_path.read_text(encoding="utf-8")
history_src = history_events_path.read_text(encoding="utf-8")

failures = []
passes = []


def check(name, cond, detail=""):
    if cond:
        passes.append(name)
    else:
        failures.append(f"{name} — {detail}")


# ─── 1. HeavenDefyingBeadItem NBT constants ──────────────────────────

check("BEAD: NBT_WANG_PING_SOUL constant declared",
      'public static final String NBT_WANG_PING_SOUL = "Ergen.Bead.WangPingSoul";' in bead_src,
      "NBT_WANG_PING_SOUL constant not found")

check("BEAD: NBT_QING_YI_SOUL constant declared",
      'public static final String NBT_QING_YI_SOUL = "Ergen.Bead.QingYiSoul";' in bead_src,
      "NBT_QING_YI_SOUL constant not found")

check("BEAD: NBT_WANG_PING_SOUL has CRON-124 Javadoc",
      "CRON-COMPLETIONIST-124" in bead_src and
      "NBT_WANG_PING_SOUL" in bead_src and
      "残魂" in bead_src,
      "CRON-124 Javadoc on NBT_WANG_PING_SOUL missing 残魂 citation")

check("BEAD: NBT_QING_YI_SOUL has CRON-124 Javadoc",
      "CRON-COMPLETIONIST-124" in bead_src and
      "NBT_QING_YI_SOUL" in bead_src and
      "殉情而亡" in bead_src,
      "CRON-124 Javadoc on NBT_QING_YI_SOUL missing 殉情而亡 citation")

check("BEAD: NBT_WANG_PING_SOUL cites Ch 700",
      "Ch 700" in bead_src and "《惊变》" in bead_src,
      "Ch 700 《惊变》 citation missing on NBT_WANG_PING_SOUL")

check("BEAD: NBT_WANG_PING_SOUL cites Baidu Baike 王平",
      "https://baike.baidu.com/item/王平/62563845" in bead_src,
      "Baidu Baike 王平 URL missing")

check("BEAD: NBT_QING_YI_SOUL cites Baidu Baike 青宜",
      "https://baike.baidu.com/item/青宜/637430" in bead_src,
      "Baidu Baike 青宜 URL missing")


# ─── 2. HeavenDefyingBeadItem accessor methods ───────────────────────

check("BEAD: hasWangPingSoul method declared",
      bool(re.search(r"public boolean hasWangPingSoul\s*\(\s*ItemStack\s+\w+\s*\)", bead_src)),
      "hasWangPingSoul(ItemStack) method not found")

check("BEAD: setWangPingSoul method declared",
      bool(re.search(r"public void setWangPingSoul\s*\(\s*ItemStack\s+\w+\s*,\s*boolean\s+\w+\s*\)", bead_src)),
      "setWangPingSoul(ItemStack, boolean) method not found")

check("BEAD: hasQingYiSoul method declared",
      bool(re.search(r"public boolean hasQingYiSoul\s*\(\s*ItemStack\s+\w+\s*\)", bead_src)),
      "hasQingYiSoul(ItemStack) method not found")

check("BEAD: setQingYiSoul method declared",
      bool(re.search(r"public void setQingYiSoul\s*\(\s*ItemStack\s+\w+\s*,\s*boolean\s+\w+\s*\)", bead_src)),
      "setQingYiSoul(ItemStack, boolean) method not found")

check("BEAD: setWangPingSoul calls getOrCreateTag().putBoolean(NBT_WANG_PING_SOUL)",
      "setWangPingSoul" in bead_src and
      "NBT_WANG_PING_SOUL" in bead_src and
      "putBoolean" in bead_src,
      "setWangPingSoul does not write NBT_WANG_PING_SOUL via putBoolean")

check("BEAD: setQingYiSoul calls getOrCreateTag().putBoolean(NBT_QING_YI_SOUL)",
      "setQingYiSoul" in bead_src and
      "NBT_QING_YI_SOUL" in bead_src and
      "putBoolean" in bead_src,
      "setQingYiSoul does not write NBT_QING_YI_SOUL via putBoolean")


# ─── 3. WangPingMortalArcEvent.java structure ────────────────────────

check("EVENT: file exists", event_path.exists(),
      f"file not found at {event_path}")

check("EVENT: package is dev.ergenverse.wanglin.bead",
      event_src.startswith("package dev.ergenverse.wanglin.bead;"),
      "wrong package declaration")

check("EVENT: class is public final",
      "public final class WangPingMortalArcEvent" in event_src,
      "class declaration not 'public final class WangPingMortalArcEvent'")

check("EVENT: CRON-124 Javadoc reference",
      "CRON-COMPLETIONIST-124" in event_src,
      "CRON-124 reference missing in Javadoc")

check("EVENT: WANG_PING_CHARACTER_ID = \"wang_ping\"",
      'public static final String WANG_PING_CHARACTER_ID = "wang_ping";' in event_src,
      "WANG_PING_CHARACTER_ID constant wrong/missing")

check("EVENT: QING_YI_CHARACTER_ID = \"qing_yi\"",
      'public static final String QING_YI_CHARACTER_ID = "qing_yi";' in event_src,
      "QING_YI_CHARACTER_ID constant wrong/missing")

check("EVENT: STATE_KEY_MORTAL_ARC_STAGE constant",
      'public static final String STATE_KEY_MORTAL_ARC_STAGE = "mortal_arc_stage";' in event_src,
      "STATE_KEY_MORTAL_ARC_STAGE constant missing")

check("EVENT: STATE_KEY_MARRIED constant",
      'public static final String STATE_KEY_MARRIED = "married_to_wang_ping";' in event_src,
      "STATE_KEY_MARRIED constant missing")

check("EVENT: STATE_KEY_IS_EMPRESS constant",
      'public static final String STATE_KEY_IS_EMPRESS = "is_empress";' in event_src,
      "STATE_KEY_IS_EMPRESS constant missing")

# 5 stage subjects
for stage, label in [
    ("SUBJECT_STAGE1_APPRENTICESHIP", "wang_ping_arc_stage1_apprenticeship"),
    ("SUBJECT_STAGE2_MARRIAGE", "wang_ping_arc_stage2_marriage"),
    ("SUBJECT_STAGE3_WAR", "wang_ping_arc_stage3_war"),
    ("SUBJECT_STAGE4_EMPEROR", "wang_ping_arc_stage4_emperor"),
    ("SUBJECT_STAGE5_DISPERSAL", "wang_ping_arc_stage5_dispersal"),
]:
    check(f"EVENT: {stage} subject declared",
          f'public static final String {stage} =' in event_src,
          f"{stage} subject constant missing")
    check(f"EVENT: {stage} subject value is '{label}'",
          f'"{label}"' in event_src,
          f"{stage} subject value not '{label}'")


# ─── 4. Entry points (handleWangPingInteract + handleQingYiInteract) ─

check("EVENT: handleWangPingInteract method declared",
      bool(re.search(r"public static void handleWangPingInteract\s*\(\s*ServerPlayer\s+\w+\s*,\s*EntityCultivator\s+\w+\s*\)", event_src)),
      "handleWangPingInteract(ServerPlayer, EntityCultivator) method not found")

check("EVENT: handleQingYiInteract method declared",
      bool(re.search(r"public static void handleQingYiInteract\s*\(\s*ServerPlayer\s+\w+\s*,\s*EntityCultivator\s+\w+\s*\)", event_src)),
      "handleQingYiInteract(ServerPlayer, EntityCultivator) method not found")


# ─── 5. 5 stage advancement methods ──────────────────────────────────

for stage_num in range(1, 6):
    check(f"EVENT: advanceToStage{stage_num} method declared",
          f"advanceToStage{stage_num}(" in event_src,
          f"advanceToStage{stage_num} method not found")


# ─── 6. Stage 5 prerequisites + side effects ─────────────────────────

check("EVENT: stage 5 checks bead in main hand",
      "getItemInHand(InteractionHand.MAIN_HAND)" in event_src and
      "HeavenDefyingBeadItem" in event_src,
      "stage 5 does not check for bead in main hand")

check("EVENT: stage 5 checks bead not dormant",
      "DORMANT_STONE" in event_src,
      "stage 5 does not check for DORMANT_STONE bead stage")

check("EVENT: stage 5 marks Wang Ping deadUntilRevived",
      "WANG_PING" in event_src and
      "deadUntilRevived = true" in event_src,
      "stage 5 does not mark Wang Ping deadUntilRevived=true")

check("EVENT: stage 5 marks 青宜 deadUntilRevived (殉情而亡)",
      "QING_YI" in event_src and
      "deadUntilRevived = true" in event_src and
      "殉情而亡" in event_src,
      "stage 5 does not mark 青宜 deadUntilRevived=true (殉情而亡)")

check("EVENT: stage 5 dematerializes Wang Ping",
      "dematerializeActor(CanonUUID.WANG_PING" in event_src,
      "stage 5 does not dematerialize Wang Ping")

check("EVENT: stage 5 dematerializes 青宜",
      "dematerializeActor(CanonUUID.QING_YI" in event_src,
      "stage 5 does not dematerialize 青宜")

check("EVENT: stage 5 sets NBT_WANG_PING_SOUL on bead",
      "setWangPingSoul(" in event_src and
      "true" in event_src,
      "stage 5 does not call setWangPingSoul(true)")

check("EVENT: stage 5 sets NBT_QING_YI_SOUL on bead",
      "setQingYiSoul(" in event_src,
      "stage 5 does not call setQingYiSoul(true)")

check("EVENT: stage 5 records in HistoryManager",
      "HistoryManager.onDiscovery(" in event_src and
      "SUBJECT_STAGE5_DISPERSAL" in event_src,
      "stage 5 does not record via HistoryManager with SUBJECT_STAGE5_DISPERSAL")


# ─── 7. Canon citations in event Javadoc ─────────────────────────────

canon_citations = [
    ("Baidu Baike 王平", "https://baike.baidu.com/item/王平/62563845"),
    ("Baidu Baike 青宜", "https://baike.baidu.com/item/青宜/637430"),
    ("Baidu Baike 仙逆编年史", "https://baike.baidu.com/item/仙逆编年史/9845998"),
    ("Zhihu timeline", "https://zhuanlan.zhihu.com/p/713215901"),
    ("Sohu deal", "https://www.sohu.com/a/1021093654_121458245"),
    ("QQ News", "https://view.inews.qq.com/a/20260507A07ELJ00"),
]

for label, url in canon_citations:
    check(f"EVENT: cites {label}", url in event_src,
          f"{label} URL ({url}) missing from event Javadoc")


# ─── 8. Chapter citations (NO fabricated chapters) ───────────────────

check("EVENT: cites Vol 7 Ch 693 《青宜》",
      "Ch 693" in event_src and "《青宜》" in event_src,
      "Ch 693 《青宜》 citation missing")

check("EVENT: cites Vol 7 Ch 700 《惊变》",
      "Ch 700" in event_src and "《惊变》" in event_src,
      "Ch 700 《惊变》 citation missing")

check("EVENT: ARC_TOTAL_YEARS = 72",
      "ARC_TOTAL_YEARS = 72" in event_src,
      "ARC_TOTAL_YEARS = 72 constant missing")

check("EVENT: 19+8+25+10+10=72 timeline documented",
      "19+8+25+10+10=72" in event_src,
      "Zhihu 5-stage timeline (19+8+25+10+10=72) missing")

check("EVENT: DISPERSAL_CHAPTER constant",
      'public static final String DISPERSAL_CHAPTER' in event_src,
      "DISPERSAL_CHAPTER constant missing")


# ─── 9. Particle/sound effects (stage 5 dispersal) ───────────────────

check("EVENT: spawnDispersalEffects method",
      "private static void spawnDispersalEffects" in event_src,
      "spawnDispersalEffects method missing")

check("EVENT: END_ROD ascending spiral",
      "ParticleTypes.END_ROD" in event_src,
      "END_ROD particles missing")

check("EVENT: SOUL particles (souls flowing)",
      "ParticleTypes.SOUL" in event_src,
      "SOUL particles missing")

check("EVENT: FIREWORK central flash",
      "ParticleTypes.FIREWORK" in event_src,
      "FIREWORK particles missing")

check("EVENT: AMETHYST_BLOCK_CHIME sound",
      "SoundEvents.AMETHYST_BLOCK_CHIME" in event_src,
      "AMETHYST_BLOCK_CHIME sound missing")

check("EVENT: WITHER_DEATH sound",
      "SoundEvents.WITHER_DEATH" in event_src,
      "WITHER_DEATH sound missing")

check("EVENT: BELL_BLOCK sound",
      "SoundEvents.BELL_BLOCK" in event_src,
      "BELL_BLOCK sound missing")


# ─── 10. HistoryEvents dispatch wiring ───────────────────────────────

check("HISTORY: imports WangPingMortalArcEvent",
      "import dev.ergenverse.wanglin.bead.WangPingMortalArcEvent;" in history_src,
      "WangPingMortalArcEvent import missing in HistoryEvents.java")

check("HISTORY: CRON-124 dispatch comment",
      "CRON-124" in history_src,
      "CRON-124 dispatch comment missing in HistoryEvents.java")

check("HISTORY: dispatches Wang Ping interaction",
      "WangPingMortalArcEvent.WANG_PING_CHARACTER_ID.equals(cultivator.getCharacterId())" in history_src,
      "Wang Ping dispatch missing")

check("HISTORY: dispatches 青宜 interaction",
      "WangPingMortalArcEvent.QING_YI_CHARACTER_ID.equals(cultivator.getCharacterId())" in history_src,
      "Qing Yi dispatch missing")

check("HISTORY: calls handleWangPingInteract",
      "WangPingMortalArcEvent.handleWangPingInteract(serverPlayer, cultivator);" in history_src,
      "handleWangPingInteract call missing")

check("HISTORY: calls handleQingYiInteract",
      "WangPingMortalArcEvent.handleQingYiInteract(serverPlayer, cultivator);" in history_src,
      "handleQingYiInteract call missing")


# ─── 11. No typo (WangPingMolarArcEvent) ─────────────────────────────

check("HISTORY: no 'WangPingMolarArcEvent' typo",
      "WangPingMolarArcEvent" not in history_src,
      "typo 'WangPingMolarArcEvent' present in HistoryEvents.java")

check("EVENT: no 'WangPingMolarArcEvent' typo",
      "WangPingMolarArcEvent" not in event_src,
      "typo 'WangPingMolarArcEvent' present in WangPingMortalArcEvent.java")

check("BEAD: no 'WangPingMolarArcEvent' typo",
      "WangPingMolarArcEvent" not in bead_src,
      "typo 'WangPingMolarArcEvent' present in HeavenDefyingBeadItem.java")


# ─── Report ──────────────────────────────────────────────────────────

print("=" * 72)
print(f"CRON-124 Wang Ping Mortal-Life Arc Verification")
print("=" * 72)
print(f"PASS: {len(passes)}")
print(f"FAIL: {len(failures)}")
if failures:
    print("\nFAILURES:")
    for f in failures:
        print(f"  - {f}")
print("=" * 72)

sys.exit(0 if not failures else 1)
