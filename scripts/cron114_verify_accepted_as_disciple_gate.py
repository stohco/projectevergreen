#!/usr/bin/env python3
"""
CRON-114 Verification Script — accepted_as_disciple gate on CRON-111.

Verifies that CRON-114 correctly added the `accepted_as_disciple` gate
to ZhouRuCultivationGrowthService.tickImpl, enforcing strict narrative
order: transfer (CRON-110) → depart (CRON-112) → accept (CRON-113) →
cultivate (CRON-111).

Categories:
  1. File existence + build artifact
  2. Gate 3 presence (the new CRON-114 gate)
  3. Gate ordering (gate 3 comes AFTER gate 2, BEFORE gate 4)
  4. Gate 3 logic (checks accepted_as_disciple flag, no-ops if false)
  5. Gate renumbering (1-12 sequential, no duplicates, no gaps)
  6. Javadoc Mechanism <ol> updated (CRON-114 gate documented)
  7. State transition diagram updated (includes CRON-112, CRON-113)
  8. @see references updated (ZhouRuKunxuDepartureEvent, MuBingmeiAcceptanceEvent)
  9. No regression to existing CRON-111 behavior (other gates intact)
 10. Architecture compliance (no direct store manipulation)
 11. Canon fidelity (strict narrative order documented)
 12. Build artifact (compiled .class updated)
"""

import re
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")
SRC_FILE = FORGE_MOD / "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.java"
CLASS_FILE = FORGE_MOD / "build/classes/java/main/dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.class"
CRON_113_FILE = FORGE_MOD / "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java"

PASS = 0
FAIL = 0
FAILURES = []


def check(name, condition, detail=""):
    global PASS, FAIL
    if condition:
        PASS += 1
        print(f"  [PASS] {name}")
    else:
        FAIL += 1
        FAILURES.append((name, detail))
        print(f"  [FAIL] {name} — {detail}")


def strip_java_comments(text):
    """Remove // line comments and /* */ block comments from Java source."""
    text = re.sub(r'/\*[\s\S]*?\*/', '', text)
    text = re.sub(r'//[^\n]*', '', text)
    return text


def main():
    global PASS, FAIL

    print("=" * 72)
    print("CRON-114 Verification: accepted_as_disciple gate on CRON-111")
    print("=" * 72)

    # ── 1. File existence + build artifact ──
    print("\n[1] File existence + build artifact")
    check("ZhouRuCultivationGrowthService.java exists",
          SRC_FILE.exists(),
          f"expected at {SRC_FILE}")
    check("MuBingmeiAcceptanceEvent.java exists (CRON-113 prerequisite)",
          CRON_113_FILE.exists(),
          f"expected at {CRON_113_FILE}")
    check("ZhouRuCultivationGrowthService.class compiled",
          CLASS_FILE.exists(),
          f"expected at {CLASS_FILE}")

    if not SRC_FILE.exists():
        print("\nFATAL: source file missing; aborting.")
        sys.exit(1)

    src_raw = SRC_FILE.read_text(encoding="utf-8")
    src = strip_java_comments(src_raw)

    # ── 2. Gate 3 presence (the new CRON-114 gate) ──
    print("\n[2] Gate 3 presence (the new CRON-114 gate)")
    check("gate 3 comment exists",
          "── 3. CRON-114 gate" in src_raw
          or "── 3. CRON-114 gate" in src)
    check("gate 3 references accepted_as_disciple",
          '"accepted_as_disciple"' in src
          and 'getBoolean("accepted_as_disciple")' in src)
    check("gate 3 no-ops if flag is false",
          'if (!zhouRuState.getBoolean("accepted_as_disciple"))' in src)
    check("gate 3 has return statement (no-op)",
          re.search(r'getBoolean\("accepted_as_disciple"\)\)\s*\{[^}]*return;', src, re.DOTALL) is not None)
    check("gate 3 references CRON-113 in comment",
          "CRON-113" in src_raw)
    check("gate 3 references CRON-114 in comment",
          "CRON-114" in src_raw)

    # ── 3. Gate ordering (gate 3 comes AFTER gate 2, BEFORE gate 4) ──
    print("\n[3] Gate ordering (gate 3 AFTER gate 2, BEFORE gate 4)")
    # Use raw text to preserve comment line numbers
    gate_lines = {}
    for match in re.finditer(r'── (\d+)\.\s', src_raw):
        gate_num = int(match.group(1))
        gate_lines[gate_num] = match.start()
    check("gate 1 exists", 1 in gate_lines)
    check("gate 2 exists", 2 in gate_lines)
    check("gate 3 exists", 3 in gate_lines)
    check("gate 4 exists", 4 in gate_lines)
    check("gate 5 exists", 5 in gate_lines)
    check("gate 2 (pregnant_with_li_muwan_soul) comes before gate 3 (accepted_as_disciple)",
          gate_lines.get(2, -1) < gate_lines.get(3, -1))
    check("gate 3 (accepted_as_disciple) comes before gate 4 (find Mu Bingmei)",
          gate_lines.get(3, -1) < gate_lines.get(4, -1))
    check("gate 3 (accepted_as_disciple) comes before gate 5 (proximity)",
          gate_lines.get(3, -1) < gate_lines.get(5, -1))

    # ── 4. Gate 3 logic (checks accepted_as_disciple flag, no-ops if false) ──
    print("\n[4] Gate 3 logic")
    # Extract the gate 3 block (from "── 3." to "── 4.")
    gate3_start = src_raw.find("── 3. CRON-114 gate")
    gate4_start = src_raw.find("── 4.", gate3_start)
    gate3_block = src_raw[gate3_start:gate4_start] if gate3_start >= 0 and gate4_start >= 0 else ""
    check("gate 3 block extracted", gate3_block != "", "could not extract gate 3 block")
    if gate3_block:
        check("gate 3 block references accepted_as_disciple",
              "accepted_as_disciple" in gate3_block)
        check("gate 3 block has the if condition",
              'if (!zhouRuState.getBoolean("accepted_as_disciple"))' in gate3_block)
        check("gate 3 block has return statement",
              "return;" in gate3_block)
        check("gate 3 block references CRON-113 (the setter)",
              "CRON-113" in gate3_block)
        check("gate 3 block references CRON-114 (this round)",
              "CRON-114" in gate3_block)
        check("gate 3 block documents strict narrative order",
              "transfer (CRON-110)" in gate3_block
              and "depart (CRON-112)" in gate3_block
              and "accept (CRON-113)" in gate3_block
              and "cultivate (CRON-111)" in gate3_block)
        check("gate 3 block documents the throttle differential rationale",
              "24000" in gate3_block and "200" in gate3_block)

    # ── 5. Gate renumbering (1-12 sequential, no duplicates, no gaps) ──
    print("\n[5] Gate renumbering (sequential 1-12)")
    gate_nums = sorted(gate_lines.keys())
    check("gates 1 through 12 all present",
          gate_nums == list(range(1, 13)),
          f"found gates: {gate_nums}")
    # Check for duplicates by counting occurrences of each gate marker
    gate_pattern = re.compile(r'── (\d+)\.\s')
    gate_counts = {}
    for m in gate_pattern.finditer(src_raw):
        n = int(m.group(1))
        gate_counts[n] = gate_counts.get(n, 0) + 1
    duplicates = [n for n, c in gate_counts.items() if c > 1]
    check("no duplicate gate numbers", not duplicates,
          f"duplicates: {duplicates}")

    # ── 6. Javadoc Mechanism <ol> updated ──
    print("\n[6] Javadoc Mechanism <ol> updated")
    # The javadoc is in a block comment; use raw text.
    # Find the Mechanism section.
    mech_start = src_raw.find("<h2>Mechanism</h2>")
    mech_end = src_raw.find("</ol>", mech_start)
    mech_section = src_raw[mech_start:mech_end] if mech_start >= 0 and mech_end >= 0 else ""
    check("Mechanism section found", mech_section != "")
    if mech_section:
        check("Mechanism mentions CRON-114 gate",
              "CRON-114 gate" in mech_section)
        check("Mechanism mentions accepted_as_disciple flag",
              "accepted_as_disciple" in mech_section)
        check("Mechanism mentions CRON-113's MuBingmeiAcceptanceEvent",
              "MuBingmeiAcceptanceEvent" in mech_section
              and "CRON-113" in mech_section)
        check("Mechanism documents strict narrative order",
              "transfer (CRON-110)" in mech_section
              and "depart (CRON-112)" in mech_section
              and "accept" in mech_section
              and "(CRON-113)" in mech_section
              and "cultivate (CRON-111)" in mech_section)
        check("Mechanism documents the pre-CRON-114 gap",
              "Before CRON-114" in mech_section
              and "missing" in mech_section)

    # ── 7. State transition diagram updated ──
    print("\n[7] State transition diagram updated")
    std_start = src_raw.find("State Transition Diagram")
    std_end = src_raw.find("</pre>", std_start)
    std_section = src_raw[std_start:std_end] if std_start >= 0 and std_end >= 0 else ""
    check("State Transition Diagram section found", std_section != "")
    if std_section:
        check("diagram includes CRON-99", "CRON-99" in std_section)
        check("diagram includes CRON-110", "CRON-110" in std_section)
        check("diagram includes CRON-112 (NEW)",
              "CRON-112" in std_section)
        check("diagram includes CRON-113 (NEW)",
              "CRON-113" in std_section)
        check("diagram includes CRON-111 with CRON-114 note",
              "CRON-111" in std_section
              and "CRON-114" in std_section)
        check("diagram includes CRON-100", "CRON-100" in std_section)
        check("diagram includes CRON-102", "CRON-102" in std_section)
        check("diagram shows CRON-110 → CRON-112 → CRON-113 → CRON-111 order",
              std_section.find("CRON-110") < std_section.find("CRON-112")
              < std_section.find("CRON-113") < std_section.find("CRON-111"))
        check("diagram mentions sent_to_kunxu (CRON-112 flag)",
              "sent_to_kunxu" in std_section)
        check("diagram mentions accepted_as_disciple (CRON-113 flag)",
              "accepted_as_disciple" in std_section)

    # ── 8. @see references updated ──
    print("\n[8] @see references updated")
    see_start = src_raw.find("@see ZhouRuSoulTransferEvent")
    see_section = src_raw[see_start:see_start + 800] if see_start >= 0 else ""
    check("@see section found", see_section != "")
    if see_section:
        check("@see ZhouRuSoulTransferEvent (CRON-110)",
              "@see ZhouRuSoulTransferEvent" in see_section
              and "CRON-110" in see_section)
        check("@see ZhouRuKunxuDepartureEvent (CRON-112, NEW)",
              "@see ZhouRuKunxuDepartureEvent" in see_section
              and "CRON-112" in see_section)
        check("@see MuBingmeiAcceptanceEvent (CRON-113, NEW)",
              "@see MuBingmeiAcceptanceEvent" in see_section
              and "CRON-113" in see_section
              and "accepted_as_disciple gate" in see_section)

    # ── 9. No regression to existing CRON-111 behavior ──
    print("\n[9] No regression to existing CRON-111 behavior")
    check("gate 2 still checks pregnant_with_li_muwan_soul",
          'getBoolean("pregnant_with_li_muwan_soul")' in src)
    check("gate 2 still no-ops if flag absent",
          re.search(r'if\s*\(\s*zhouRuState\s*==\s*null\s*\|\|\s*!zhouRuState\.getBoolean\("pregnant_with_li_muwan_soul"\)\s*\)\s*\{[^}]*return;', src, re.DOTALL) is not None)
    check("gate 1 still finds Zhou Ru",
          'findCultivatorByCharacterId(level, ZHOU_RU_CHARACTER_ID)' in src)
    check("gate 4 still finds Mu Bingmei",
          'findCultivatorByCharacterId(level, MU_BINGMEI_CHARACTER_ID)' in src)
    check("gate 5 still checks proximity",
          "distanceToSqr(muBingmei)" in src
          and "PROXIMITY_RADIUS_SQ" in src)
    check("gate 6 still checks current realm",
          "getCultivationRealm()" in src
          and "parseRealmId" in src)
    check("gate 7 still checks canon cap",
          "CANON_CAP.order" in src)
    check("gate 8 still advances realm",
          "currentRealm.next()" in src
          and "setCultivationRealm" in src)
    check("gate 9 still updates runtime state",
          'putInt("cultivation_realm_order"' in src
          and 'runtime.updateNpcState(ZHOU_RU_CHARACTER_ID, zhouRuState)' in src)
    check("gate 10 still spawns breakthrough effects",
          "spawnBreakthroughEffects" in src)
    check("gate 11 still displays bilingual message",
          "announceBreakthrough" in src
          and "findNearbyPlayer" in src)
    check("gate 12 still records in HistoryManager",
          "HistoryManager.onDiscovery" in src
          and '"zhou_ru_cultivation_breakthrough"' in src)
    check("tick() entry point unchanged",
          "public static void tick(ServerLevel level, long currentTick)" in src)
    check("tick() throttle unchanged",
          "currentTick % GROWTH_INTERVAL_TICKS != 0" in src)
    check("tick() defensive try/catch unchanged",
          "tickImpl(level, currentTick)" in src
          and "catch (Throwable t)" in src)

    # ── 10. Architecture compliance ──
    print("\n[10] Architecture compliance (CRON-69 point 5)")
    check("no direct WorldDeltaStore manipulation",
          "WorldDeltaStore" not in src)
    check("no direct WorldLayer manipulation",
          "WorldLayer" not in src)
    check("no level.setBlock call",
          ".setBlock(" not in src)
    check("uses runtime.updateNpcState (the facade)",
          "runtime.updateNpcState" in src)
    check("uses vanilla particle API (level.sendParticles)",
          "level.sendParticles" in src)
    check("uses vanilla sound API (level.playSound)",
          "level.playSound" in src)

    # ── 11. Canon fidelity ──
    print("\n[11] Canon fidelity (strict narrative order)")
    check("canon basis N10 (Zhou Ru) still cited",
          "N10" in src_raw)
    check("canon basis N19 (Mu Bingmei) still cited",
          "N19" in src_raw)
    check("canon basis L74 (Kunxu Realm) still cited",
          "L74" in src_raw)
    check("strict narrative order documented in gate 3 comment",
          "transfer (CRON-110) → depart (CRON-112) → accept (CRON-113) → cultivate (CRON-111)" in src_raw)
    check("the gate enforces canon: cultivation only after acceptance",
          "cultivation growth only begins AFTER the disciple-master bond" in src_raw
          and "canonically established (CRON-113)" in src_raw)

    # ── 12. Build artifact freshness ──
    print("\n[12] Build artifact freshness")
    if CLASS_FILE.exists():
        import os
        class_mtime = CLASS_FILE.stat().st_mtime
        src_mtime = SRC_FILE.stat().st_mtime
        check("class file is newer than source (recompiled)",
              class_mtime >= src_mtime,
              f"class mtime={class_mtime}, src mtime={src_mtime}")
    else:
        check("class file exists", False, "class file missing")

    # ── Summary ──
    print("\n" + "=" * 72)
    print(f"SUMMARY: {PASS} passed, {FAIL} failed (total {PASS + FAIL})")
    print("=" * 72)
    if FAIL > 0:
        print("\nFAILURES:")
        for name, detail in FAILURES:
            print(f"  - {name}: {detail}")
        sys.exit(1)
    else:
        print("\nALL CHECKS PASSED.")
        sys.exit(0)


if __name__ == "__main__":
    main()
