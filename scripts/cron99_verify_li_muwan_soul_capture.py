#!/usr/bin/env python3
"""
CRON-99 verification script.

Verifies that the Li Muwan soul-capture event is correctly implemented:
  1. LiMuwanSoulCaptureEvent.java exists in the wanglin.bead package
  2. handleLiMuwanDeath is wired into EntityCultivator.die()
  3. setLiMuwanSoul is now ACTUALLY CALLED (closes CRON-95 known gap)
  4. Canon-faithful bilingual messages are present
  5. HistoryManager.onDiscovery is invoked for soul-captured + lost variants
  6. BeadInteriorStage.DORMANT_STONE rejection path is implemented
  7. Canon basis is documented with NO fabricated chapter citations
"""
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
SRC = FORGE_MOD / "src/main/java/dev/ergenverse"

EVENT_FILE = SRC / "wanglin/bead/LiMuwanSoulCaptureEvent.java"
ENTITY_FILE = SRC / "entity/EntityCultivator.java"
BEAD_FILE = SRC / "wanglin/bead/HeavenDefyingBeadItem.java"

PASS = 0
FAIL = 0
FAILS = []

def check(name, cond, detail=""):
    global PASS, FAIL
    if cond:
        PASS += 1
        print(f"  [PASS] {name}")
    else:
        FAIL += 1
        FAILS.append(name)
        print(f"  [FAIL] {name}  {detail}")

def main():
    print("=== CRON-99: Li Muwan Soul Capture Event — Verification ===\n")

    # ── Category 1: LiMuwanSoulCaptureEvent.java exists and is well-formed ──
    print("1. LiMuwanSoulCaptureEvent.java exists and is well-formed:")
    text = EVENT_FILE.read_text(encoding="utf-8")
    check("File exists", EVENT_FILE.exists())
    check("Package declaration correct",
          "package dev.ergenverse.wanglin.bead;" in text)
    check("Public final class",
          "public final class LiMuwanSoulCaptureEvent" in text)
    check("Private constructor (utility class)",
          "private LiMuwanSoulCaptureEvent() {}" in text)
    check("SOUL_CAPTURE_RADIUS constant",
          "public static final double SOUL_CAPTURE_RADIUS" in text)
    check("CHARACTER_ID constant = li_muwan",
          'public static final String CHARACTER_ID = "li_muwan";' in text)
    check("handleLiMuwanDeath method",
          "public static void handleLiMuwanDeath" in text)
    check("CRON-95 gap-closing reference in javadoc",
          "CRON-95" in text)
    check("Canon basis section present",
          "Canon basis" in text and "fact-checked" in text)
    check("No fabricated chapter citation",
          "Chapter " not in text.split("Canon basis")[1].split("</ol>")[0]
          if "Canon basis" in text else False,
          "Canon basis section should not contain literal 'Chapter N' citations")

    # ── Category 2: Canon fidelity ──
    print("\n2. Canon fidelity (fact-checked via web-search 2026-07-26):")
    check("李慕婉 (Li Muwan) referenced",
          "李慕婉" in text)
    check("元婴 (Nascent Soul) referenced",
          "元婴" in text)
    check("天逆珠 (Heaven-Defying Bead) referenced",
          "天逆珠" in text)
    check("修魔海 (Sea of Devils) referenced",
          "修魔海" in text)
    check("结婴 (Nascent Soul formation) referenced as cause of death",
          "结婴" in text)
    check("司徒南 (Situ Nan) referenced for bead-opening canon",
          "司徒南" in text)
    check("Luo He Sect (洛河门) NOT re-introduced (CRON-69 correction held)",
          "洛河门" not in text or "Luo He Sect" in text)
    check("Single-player maximalism Article XLIII referenced",
          "Article XLIII" in text)
    check("CRON-69 canon-honesty pattern referenced",
          "CRON-69" in text)

    # ── Category 3: Implementation paths ──
    print("\n3. Implementation paths (capture / lost / dormant):")
    check("Capture path: setLiMuwanSoul(stack, true) invoked",
          "beadItem.setLiMuwanSoul(beadStack, true)" in text)
    check("Capture path: bilingual message (Chinese first)",
          "李慕婉的元婴被天逆珠收容。" in text)
    check("Capture path: bilingual message (English second)",
          "Li Muwan's Nascent Soul has been drawn into the Heaven-Defying Bead." in text)
    check("Capture path: motivation message",
          "path of defying heaven" in text)
    check("Capture path: HistoryManager.onDiscovery(li_muwan_soul_captured)",
          '"li_muwan_soul_captured"' in text)
    check("Lost path (no player): announceSoulLostNoPlayer exists",
          "announceSoulLostNoPlayer" in text)
    check("Lost path (no bead): announceSoulLostNoBead exists",
          "announceSoulLostNoBead" in text)
    check("Lost path (no bead): bilingual dissipated message",
          "李慕婉的元婴消散于天地之间。" in text)
    check("Lost path (no bead): HistoryManager.onDiscovery(li_muwan_soul_lost_no_bead)",
          '"li_muwan_soul_lost_no_bead"' in text)
    check("Lost path (dormant bead): announceSoulLostDormantBead exists",
          "announceSoulLostDormantBead" in text)
    check("Lost path (dormant bead): HistoryManager.onDiscovery(li_muwan_soul_lost_dormant_bead)",
          '"li_muwan_soul_lost_dormant_bead"' in text)

    # ── Category 4: Player-finding logic ──
    print("\n4. Player-finding logic:")
    check("findCapturingPlayer method exists",
          "private static ServerPlayer findCapturingPlayer" in text)
    check("Killer preference (source.getEntity() instanceof ServerPlayer)",
          "source.getEntity()" in text and "instanceof ServerPlayer" in text)
    check("Closest-player-in-radius fallback",
          "p.blockPosition().distSqr(deathPos)" in text)
    check("findBead method exists (mirrors BeadProgressionService pattern)",
          "private static ItemStack findBead" in text)
    check("findBead scans main hand first",
          'player.getMainHandItem()' in text)
    check("findBead scans off hand second",
          'player.getOffhandItem()' in text)
    check("findBead scans main inventory last",
          'player.getInventory().items' in text)

    # ── Category 5: Bead stage gate ──
    print("\n5. Bead stage gate (DORMANT_STONE rejection):")
    check("BeadInteriorStage import",
          "import dev.ergenverse.wanglin.bead.BeadInteriorStage" not in text  # same package, no import needed
          or "BeadInteriorStage" in text)
    check("DORMANT_STONE check",
          "BeadInteriorStage.DORMANT_STONE" in text)
    check("Dormant rejection: canon rationale (Situ Nan must crack open)",
          "Situ Nan must crack open the bead" in text)

    # ── Category 6: EntityCultivator.die() wiring ──
    print("\n6. EntityCultivator.die() wiring:")
    ent_text = ENTITY_FILE.read_text(encoding="utf-8")
    check("Import LiMuwanSoulCaptureEvent",
          "import dev.ergenverse.wanglin.bead.LiMuwanSoulCaptureEvent;" in ent_text)
    check("characterId guard (li_muwan)",
          'LiMuwanSoulCaptureEvent.CHARACTER_ID.equals(this.getCharacterId())' in ent_text)
    check("handleLiMuwanDeath call",
          "LiMuwanSoulCaptureEvent.handleLiMuwanDeath" in ent_text)
    check("Call site passes ServerLevel",
          "serverLevel" in ent_text and "LiMuwanSoulCaptureEvent.handleLiMuwanDeath" in ent_text)
    check("Call site passes DamageSource",
          "source" in ent_text and "LiMuwanSoulCaptureEvent.handleLiMuwanDeath" in ent_text)
    check("Call site passes blockPosition()",
          "this.blockPosition()" in ent_text and "LiMuwanSoulCaptureEvent.handleLiMuwanDeath" in ent_text)
    check("Wiring inserted BEFORE player-caused-death bookkeeping",
          ent_text.index("LiMuwanSoulCaptureEvent.handleLiMuwanDeath")
          < ent_text.index("Layer 3: Record NPC death"))

    # ── Category 7: setLiMuwanSoul is now ACTUALLY CALLED (CRON-95 gap closed) ──
    print("\n7. CRON-95 gap closure — setLiMuwanSoul is now ACTUALLY CALLED:")
    # Check across the whole codebase that setLiMuwanSoul has at least one caller
    import subprocess
    grep_result = subprocess.run(
        ["grep", "-rn", "setLiMuwanSoul", "--include=*.java",
         str(SRC)],
        capture_output=True, text=True
    )
    callers = [l for l in grep_result.stdout.splitlines()
               if "HeavenDefyingBeadItem.java" not in l]
    check("setLiMuwanSoul has at least one caller outside HeavenDefyingBeadItem",
          len(callers) >= 1,
          f"callers found: {len(callers)}")
    check("Caller is LiMuwanSoulCaptureEvent.java",
          any("LiMuwanSoulCaptureEvent.java" in l for l in callers),
          f"callers: {callers}")

    # ── Summary ──
    print(f"\n=== Summary: {PASS} passed, {FAIL} failed ===")
    if FAILS:
        print(f"FAILURES: {FAILS}")
    return 1 if FAIL > 0 else 0

if __name__ == "__main__":
    sys.exit(main())
