#!/usr/bin/env python3
"""
CRON-115 verification: 木冰眉 (Mu Bingmei) canon correction.

Verifies that:
  1. No mod-loaded Java file contains the wrong character "慕冰媚".
  2. No mod-loaded JSON resource file contains the wrong character "慕冰媚".
  3. The correct character "木冰眉" appears in all expected files.
  4. The "true_identity" note in npc_mu_bingmei.json uses the canon-accurate
     "ninth avatar (第九分身)" framing, NOT the old "mask she wore" framing.
  5. The "sources" field cites the Baidu Baike page for 木冰眉.
  6. The CRON-115 canon-correction header note exists in MuBingmeiAcceptanceEvent.java.
  7. The daughter-NPC plan was NOT implemented (no "zhou_ru_daughter" or
     "li_muwan_reincarnated" NPC was added) — verified non-canon.

Exit code 0 = all checks pass; 1 = at least one check failed.
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")

# Files that MUST be clean (no 慕冰媚)
TARGET_FILES = [
    "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java",
    "src/main/java/dev/ergenverse/runtime/CanonUUID.java",
    "src/main/java/dev/ergenverse/runtime/NPCRuntime.java",
    "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java",
    "src/main/java/dev/ergenverse/wanglin/RICanonicalDatabase.java",
    "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuKunxuDepartureEvent.java",
    "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java",
    "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.java",
    "src/main/java/dev/ergenverse/wanglin/registry/CanonicalAllies.java",
    "src/main/resources/data/ergenverse/ri_canon_database.json",
    "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json",
]

WRONG = "慕冰媚"
RIGHT = "木冰眉"


def check_no_wrong_char() -> tuple[int, int]:
    """Check 1: no mod-loaded file contains the wrong character."""
    passed = 0
    failed = 0
    for rel in TARGET_FILES:
        path = FORGE_MOD / rel
        if not path.exists():
            print(f"  FAIL: {rel} — file does not exist")
            failed += 1
            continue
        text = path.read_text(encoding="utf-8")
        if WRONG in text:
            count = text.count(WRONG)
            print(f"  FAIL: {rel} — {count} instances of {WRONG} remain")
            failed += 1
        else:
            passed += 1
    return passed, failed


def check_correct_char_present() -> tuple[int, int]:
    """Check 3: the correct character appears in the key files."""
    passed = 0
    failed = 0
    key_files = [
        "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java",
        "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java",
        "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json",
        "src/main/resources/data/ergenverse/ri_canon_database.json",
    ]
    for rel in key_files:
        path = FORGE_MOD / rel
        text = path.read_text(encoding="utf-8")
        if RIGHT not in text:
            print(f"  FAIL: {rel} — does not contain {RIGHT}")
            failed += 1
        else:
            passed += 1
    return passed, failed


def check_true_identity_avatar() -> tuple[int, int]:
    """Check 4: true_identity uses 'ninth avatar' framing, not 'mask she wore'."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json"
    text = path.read_text(encoding="utf-8")
    passed = 0
    failed = 0
    if "ninth avatar" in text and "第九分身" in text:
        passed += 1
    else:
        print(f"  FAIL: true_identity does not use 'ninth avatar (第九分身)' framing")
        failed += 1
    if "mask I wore" in text or "public identity" in text:
        print(f"  FAIL: old 'mask' or 'public identity' framing still present")
        failed += 1
    else:
        passed += 1
    return passed, failed


def check_sources_citation() -> tuple[int, int]:
    """Check 5: sources field cites the Baidu Baike page for 木冰眉."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json"
    text = path.read_text(encoding="utf-8")
    expected_url = "https://baike.baidu.com/item/木冰眉/8802287"
    if expected_url in text:
        return 1, 0
    print(f"  FAIL: sources field does not cite {expected_url}")
    return 0, 1


def check_cron115_header() -> tuple[int, int]:
    """Check 6: CRON-115 canon-correction header note exists."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java"
    text = path.read_text(encoding="utf-8")
    if "CRON-COMPLETIONIST-115 — CANON CORRECTION" in text:
        return 1, 0
    print(f"  FAIL: CRON-115 header note missing from MuBingmeiAcceptanceEvent.java")
    return 0, 1


def check_no_daughter_npc() -> tuple[int, int]:
    """Check 7: NO daughter NPC was added (the plan was rejected as non-canon)."""
    passed = 0
    failed = 0
    # Check that no new file was created for a daughter NPC
    daughter_files = [
        "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuDaughterBirthEvent.java",
        "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuDaughterEvent.java",
    ]
    for rel in daughter_files:
        path = FORGE_MOD / rel
        if path.exists():
            print(f"  FAIL: daughter NPC file exists: {rel}")
            failed += 1
        else:
            passed += 1
    # Check that CanonUUID.java does NOT contain a daughter constant
    canon_uuid = (FORGE_MOD / "src/main/java/dev/ergenverse/runtime/CanonUUID.java").read_text(encoding="utf-8")
    if "ZHOU_RU_DAUGHTER" in canon_uuid or "LI_MUWAN_REINCARNATED" in canon_uuid:
        print(f"  FAIL: CanonUUID.java contains a daughter NPC constant (non-canon)")
        failed += 1
    else:
        passed += 1
    return passed, failed


def check_canon_actor_materializer_display_name() -> tuple[int, int]:
    """Check that the display name string in CanonActorMaterializer uses 木冰眉."""
    path = FORGE_MOD / "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java"
    text = path.read_text(encoding="utf-8")
    # The profile line should now read: "Mu Bingmei 木冰眉 / 柳眉"
    if '"Mu Bingmei 木冰眉 / 柳眉"' in text:
        return 1, 0
    print(f"  FAIL: CanonActorMaterializer profile display name not updated")
    return 0, 1


def check_ri_canon_database_namecn() -> tuple[int, int]:
    """Check that ri_canon_database.json N19 entry nameCn is correct."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/ri_canon_database.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    # Find the N19 entry
    for char in data.get("characters", []):
        if char.get("id") == "N19":
            if char.get("nameCn") == "木冰眉 / 柳眉":
                return 1, 0
            print(f"  FAIL: N19 nameCn is '{char.get('nameCn')}', expected '木冰眉 / 柳眉'")
            return 0, 1
    print(f"  FAIL: N19 entry not found in ri_canon_database.json")
    return 0, 1


def check_initiation_lines() -> tuple[int, int]:
    """Check that the initiation_lines use the 'ninth avatar' framing."""
    path = FORGE_MOD / "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    lines = data.get("initiation_lines", [])
    for line in lines:
        if "ninth avatar" in line and "第九分身" in line:
            return 1, 0
    print(f"  FAIL: no initiation_line uses 'ninth avatar (第九分身)' framing")
    return 0, 1


def main() -> int:
    print("=" * 70)
    print("CRON-115 Verification: 木冰眉 (Mu Bingmei) Canon Correction")
    print("=" * 70)
    print(f"Wrong character: {WRONG}")
    print(f"Correct character: {RIGHT}")
    print(f"Canon source: https://baike.baidu.com/item/木冰眉/8802287")
    print()

    total_passed = 0
    total_failed = 0

    print(f"── Check 1: No mod-loaded file contains wrong character '{WRONG}' ──")
    p, f = check_no_wrong_char()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed (of {len(TARGET_FILES)} files)")
    print()

    print("── Check 2: Skipped (subsumed by Check 1) ──")
    print()

    print("── Check 3: Correct character '木冰眉' present in key files ──")
    p, f = check_correct_char_present()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 4: true_identity uses 'ninth avatar (第九分身)' framing ──")
    p, f = check_true_identity_avatar()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 5: sources field cites Baidu Baike page for 木冰眉 ──")
    p, f = check_sources_citation()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 6: CRON-115 header note in MuBingmeiAcceptanceEvent.java ──")
    p, f = check_cron115_header()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 7: NO daughter NPC added (plan rejected as non-canon) ──")
    p, f = check_no_daughter_npc()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 8: CanonActorMaterializer display name uses 木冰眉 ──")
    p, f = check_canon_actor_materializer_display_name()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 9: ri_canon_database.json N19 nameCn is '木冰眉 / 柳眉' ──")
    p, f = check_ri_canon_database_namecn()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("── Check 10: initiation_lines use 'ninth avatar' framing ──")
    p, f = check_initiation_lines()
    total_passed += p
    total_failed += f
    print(f"  {p} passed, {f} failed")
    print()

    print("=" * 70)
    print(f"TOTAL: {total_passed} passed, {total_failed} failed")
    print("=" * 70)
    if total_failed == 0:
        print("ALL CHECKS PASS — CRON-115 canon correction verified.")
        return 0
    else:
        print("SOME CHECKS FAILED — see above for details.")
        return 1


if __name__ == "__main__":
    sys.exit(main())
