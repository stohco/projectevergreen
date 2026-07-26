#!/usr/bin/env python3
"""
CRON-100 verification script.

Verifies that the 137-revival-attempt counter and mechanic is correctly
implemented:
  1. HeavenDefyingBeadItem has NBT_REVIVAL_ATTEMPTS + NBT_LAST_REVIVAL_TICK
  2. CANON_REVIVAL_ATTEMPT_CAP = 137 (canon-attested)
  3. Accessor methods (get/setRevivalAttempts, get/setLastRevivalAttemptTick)
  4. Tooltip displays "Revival Attempts: X / 137" when hasLiMuwanSoul
  5. RevivalAttemptService exists with 5 canon gates + 4 outcomes
  6. /ergenverse bead revive command is registered
  7. Canon-faithful bilingual messages for 137th failure + success
  8. No fabricated chapter citations
"""
import sys
import subprocess
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
SRC = FORGE_MOD / "src/main/java/dev/ergenverse"

BEAD_FILE = SRC / "wanglin/bead/HeavenDefyingBeadItem.java"
SERVICE_FILE = SRC / "wanglin/bead/RevivalAttemptService.java"
COMMAND_FILE = SRC / "spawn/ErgenverseCommand.java"

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
    print("=== CRON-100: 137 Revival Attempts Counter — Verification ===\n")

    bead_text = BEAD_FILE.read_text(encoding="utf-8")
    service_text = SERVICE_FILE.read_text(encoding="utf-8")
    command_text = COMMAND_FILE.read_text(encoding="utf-8")

    # ── Category 1: NBT fields in HeavenDefyingBeadItem ──
    print("1. NBT fields in HeavenDefyingBeadItem:")
    check("NBT_REVIVAL_ATTEMPTS constant defined",
          'public static final String NBT_REVIVAL_ATTEMPTS = "Ergen.Bead.RevivalAttempts";' in bead_text)
    check("NBT_LAST_REVIVAL_TICK constant defined",
          'public static final String NBT_LAST_REVIVAL_TICK = "Ergen.Bead.LastRevivalTick";' in bead_text)
    check("CANON_REVIVAL_ATTEMPT_CAP = 137",
          "public static final int CANON_REVIVAL_ATTEMPT_CAP = 137;" in bead_text)
    check("CRON-100 javadoc reference",
          "CRON-COMPLETIONIST-100" in bead_text)
    check("Canon basis: 137 attempts referenced",
          "137 times" in bead_text)
    check("Canon basis: 137th attempt narrative quote",
          "血色残阳笼罩着朱雀墓" in bead_text)
    check("Canon basis: Fourth Step requirement",
          "Fourth Step" in bead_text and "第四步" in bead_text)
    check("Canon basis: 一界本源 (origin of a world)",
          "一界本源" in bead_text)
    check("No fabricated chapter citation",
          "Chapter " not in bead_text.split("CRON-COMPLETIONIST-100")[1].split("*/")[0]
          if "CRON-COMPLETIONIST-100" in bead_text else False,
          "CRON-100 javadoc should not contain literal 'Chapter N' citations")

    # ── Category 2: Accessor methods ──
    print("\n2. Accessor methods:")
    check("getRevivalAttempts method",
          "public int getRevivalAttempts(ItemStack stack)" in bead_text)
    check("setRevivalAttempts method",
          "public void setRevivalAttempts(ItemStack stack, int count)" in bead_text)
    check("getLastRevivalAttemptTick method",
          "public long getLastRevivalAttemptTick(ItemStack stack)" in bead_text)
    check("setLastRevivalAttemptTick method",
          "public void setLastRevivalAttemptTick(ItemStack stack, long tick)" in bead_text)
    check("getRevivalAttempts clamps to CANON_REVIVAL_ATTEMPT_CAP",
          "Math.min(CANON_REVIVAL_ATTEMPT_CAP" in bead_text)
    check("setRevivalAttempts clamps to CANON_REVIVAL_ATTEMPT_CAP",
          "Math.min(CANON_REVIVAL_ATTEMPT_CAP, Math.max(0, count))" in bead_text)
    check("setRevivalAttempts does NOT call recalculateStage (quest tracker, not progression)",
          "recalculateStage" not in bead_text.split("public void setRevivalAttempts")[1].split("/**")[0][:500]
          if "public void setRevivalAttempts" in bead_text else False)

    # ── Category 3: Tooltip display ──
    print("\n3. Tooltip display:")
    check("Tooltip shows 'Revival Attempts: X / 137'",
          "Revival Attempts: " in bead_text and "CANON_REVIVAL_ATTEMPT_CAP" in bead_text)
    check("Tooltip only shows when hasLiMuwanSoul is true",
          bead_text.index("Revival Attempts: ") > bead_text.index("if (hasLiMuwanSoul(stack))"))
    check("Tooltip uses GOLD formatting (Wang Lin's central quest)",
          "ChatFormatting.GOLD" in bead_text)

    # ── Category 4: RevivalAttemptService class ──
    print("\n4. RevivalAttemptService class:")
    check("File exists", SERVICE_FILE.exists())
    check("Package declaration correct",
          "package dev.ergenverse.wanglin.bead;" in service_text)
    check("Public final class",
          "public final class RevivalAttemptService" in service_text)
    check("Private constructor (utility class)",
          "private RevivalAttemptService() {}" in service_text)
    check("REVIVAL_COOLDOWN_TICKS constant",
          "public static final int REVIVAL_COOLDOWN_TICKS" in service_text)
    check("Cooldown = 6000 ticks (5 minutes)",
          "= 6000" in service_text)
    check("attemptRevival method (sole entry point)",
          "public static boolean attemptRevival(ServerPlayer player, ItemStack stack)" in service_text)
    check("Three HistoryManager subject constants",
          'SUBJECT_REVIVAL_FAILED = "li_muwan_revival_failed"' in service_text)
    check("SUBJECT_REVIVAL_137TH constant",
          'SUBJECT_REVIVAL_137TH = "li_muwan_revival_137th_failure"' in service_text)
    check("SUBJECT_REVIVAL_SUCCEEDED constant",
          'SUBJECT_REVIVAL_SUCCEEDED = "li_muwan_revival_succeeded"' in service_text)

    # ── Category 5: Canon gates ──
    print("\n5. Canon gates (5 total):")
    check("Gate 1: Soul gate (hasLiMuwanSoul)",
          "beadItem.hasLiMuwanSoul(stack)" in service_text)
    check("Gate 2: Stage gate (hasSpecialFunctions)",
          "stage.hasSpecialFunctions" in service_text)
    check("Gate 3: Realm gate (SOUL_FORMATION)",
          "RealmId.SOUL_FORMATION" in service_text)
    check("Gate 4: Cooldown gate (REVIVAL_COOLDOWN_TICKS)",
          "REVIVAL_COOLDOWN_TICKS" in service_text)
    check("Gate 5: Cap gate (CANON_REVIVAL_ATTEMPT_CAP)",
          "HeavenDefyingBeadItem.CANON_REVIVAL_ATTEMPT_CAP" in service_text)
    check("TRANSCENDENCE realm for success",
          "RealmId.TRANSCENDENCE" in service_text)

    # ── Category 6: Outcomes ──
    print("\n6. Outcomes (4 total):")
    check("Outcome 1: doFailedRevival method (normal failure)",
          "private static boolean doFailedRevival" in service_text)
    check("Outcome 2: 137th failure special message",
          "is137th" in service_text)
    check("Outcome 3: doSuccessfulRevival method",
          "private static boolean doSuccessfulRevival" in service_text)
    check("Outcome 4: Rejected (gate failure) — multiple rejection messages",
          service_text.count("return false;") >= 5)  # 5 gates

    # ── Category 7: Canon-faithful messages ──
    print("\n7. Canon-faithful bilingual messages:")
    check("137th failure: 血色残阳笼罩着朱雀墓 (blood-red sun over Vermilion Bird Tomb)",
          "血色残阳笼罩着朱雀墓。" in service_text)
    check("137th failure: 王林怀中抱着生机尽散的李慕婉",
          "王林怀中抱着生机尽散的李慕婉" in service_text)
    check("137th failure: 这是他第137次尝试复活失败",
          "这是他第137次尝试复活失败。" in service_text)
    check("137th failure: English translation present",
          "blood-red sun sets over the Vermilion Bird Tomb" in service_text)
    check("137th failure: 'Only the Fourth Step can save her now'",
          "Only the Fourth Step" in service_text)
    check("Normal failure: bilingual message",
          "次尝试复活失败" in service_text)
    check("Success: 王林踏入第四步 (Wang Lin enters Fourth Step)",
          "王林踏入第四步" in service_text)
    check("Success: 以一界本源 (using the origin of a world)",
          "以一界本源" in service_text)
    check("Success: 逆天复活李慕婉 (defy heaven and revive Li Muwan)",
          "逆天复活李慕婉" in service_text)
    check("Success: 两人踏天同行 (together they transcend)",
          "两人踏天同行" in service_text)
    check("Success: English translation",
          "Wang Lin enters the Fourth Step" in service_text)

    # ── Category 8: HistoryManager integration ──
    print("\n8. HistoryManager integration:")
    check("137th failure recorded in HistoryManager",
          "SUBJECT_REVIVAL_137TH" in service_text)
    check("Normal failure recorded in HistoryManager",
          "SUBJECT_REVIVAL_FAILED" in service_text)
    check("Success recorded in HistoryManager",
          "SUBJECT_REVIVAL_SUCCEEDED" in service_text)
    check("HistoryManager.onDiscovery called 3 times (one per outcome)",
          service_text.count("HistoryManager.onDiscovery(player") == 3)

    # ── Category 9: Command registration ──
    print("\n9. /ergenverse bead revive command:")
    check("Command literal 'bead' registered",
          'Commands.literal("bead")' in command_text)
    check("Subcommand 'revive' registered",
          'Commands.literal("revive")' in command_text)
    check("beadRevive method exists",
          "private static int beadRevive(CommandSourceStack src)" in command_text)
    check("Command delegates to RevivalAttemptService",
          "RevivalAttemptService" in command_text)
    check("Command calls attemptRevival",
          ".attemptRevival(player, beadStack)" in command_text)
    check("findBead helper in command (main-hand → off-hand → inventory)",
          "private static net.minecraft.world.item.ItemStack findBead(ServerPlayer player)" in command_text)
    check("Command registration log includes 'bead revive'",
          "bead revive" in command_text)

    # ── Category 10: Architecture respect ──
    print("\n10. Architecture respect (CRON-69 ten-point refactor):")
    check("Service is in wanglin.bead package (item-NBT, not world-state)",
          "package dev.ergenverse.wanglin.bead;" in service_text)
    check("Service does NOT import WorldFacade",
          "import dev.ergenverse.runtime.layer.WorldFacade" not in service_text)
    check("Service does NOT import WorldDeltaStore",
          "import dev.ergenverse.runtime.delta.WorldDeltaStore" not in service_text)
    check("Service does NOT import Provenance",
          "Provenance" not in service_text)
    check("Service does NOT touch the blueprint",
          "Blueprint" not in service_text)
    check("Single-Player Maximalism Article XLIII referenced",
          "Article XLIII" in service_text)
    check("CRON-99 soul-capture referenced (Li Muwan soul in bead)",
          "CRON-99" in service_text)
    check("CRON-95 findBead pattern referenced (consistency)",
          "CRON-95" in service_text or "BeadProgressionService" in service_text
          or "findBead" in service_text)

    # ── Summary ──
    print(f"\n=== Summary: {PASS} passed, {FAIL} failed ===")
    if FAILS:
        print(f"FAILURES: {FAILS}")
    return 1 if FAIL > 0 else 0

if __name__ == "__main__":
    sys.exit(main())
