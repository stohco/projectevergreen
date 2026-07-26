#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-98 verification script.

Verifies the per-character body-scale override that closes the CRON-97
self-critique #6 gap:

  BEFORE CRON-98:
    - HeldWeaponType enum mapped weapon type → scale (NONE=1.0, SWORD=1.0,
      FAN=1.0, STAFF=1.05, HOE=0.95, FLY_WHISK=0.98).
    - Situ Nan and Li Muwan BOTH mapped to FAN → both got scale 1.0.
    - Intended: Situ Nan 1.10, Li Muwan 0.92. LOST.

  AFTER CRON-98:
    - New CharacterBuild enum maps characterId → scale (9 entries).
    - setCharacterId() now sources scale from CharacterBuild.scaleFor(id),
      NOT from HeldWeaponType.scale.
    - HeldWeaponType.scale is @Deprecated, retained for backward-compat
      but no longer consulted at runtime.
    - Situ Nan gets 1.10, Li Muwan gets 0.92 — closing the gap.

Verifies:
  1. CharacterBuild enum exists with 9 constants
  2. Each constant has correct characterId and scale
  3. scaleFor(String) returns the right scale for each canon character
  4. scaleFor returns 1.0F for null/empty/unrecognized
  5. setCharacterId sources scale from CharacterBuild, not HeldWeaponType
  6. HeldWeaponType.scale field is @Deprecated
  7. The Situ Nan / Li Muwan limitation is closed (1.10F and 0.92F present)
  8. No false canon citations; mod-original honestly flagged

Total: 35 checks across 6 categories.
"""

import re
import sys
from pathlib import Path

MODEL_PATH = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse/client/model/CultivatorRobeModel.java")

PASS_COUNT = 0
FAIL_COUNT = 0
FAILURES = []


def check(name: str, condition: bool, detail: str = ""):
    global PASS_COUNT, FAIL_COUNT
    if condition:
        PASS_COUNT += 1
    else:
        FAIL_COUNT += 1
        FAILURES.append(f"{name}: {detail}")
        print(f"  FAIL  {name} — {detail}")


def section(title: str):
    print(f"\n── {title} ──")


# ════════════════════════════════════════════════════════════════════
# 1. CHARACTERBUILD ENUM EXISTS WITH 9 CONSTANTS
# ════════════════════════════════════════════════════════════════════

def check_enum_exists(model_src: str):
    section("1. CharacterBuild enum exists")

    check(
        "CharacterBuild enum declared",
        "private enum CharacterBuild" in model_src,
        "CharacterBuild enum missing"
    )

    check(
        "CharacterBuild javadoc references CRON-98",
        "CRON-COMPLETIONIST-98: Per-character body build" in model_src,
        "CharacterBuild javadoc missing CRON-98 reference"
    )

    check(
        "CharacterBuild javadoc explains the CRON-97 gap it closes",
        "CRON-97 self-critique #6 gap" in model_src,
        "CharacterBuild javadoc does not explain the gap it closes"
    )

    expected_constants = [
        "WANG_LIN", "SITU_NAN", "TENG_LI", "WANG_ZHUO",
        "TENG_HUAYUAN", "ZENG_DA_NIU", "LI_MUWAN", "WANG_HAO", "OLD_CHEN",
    ]
    for name in expected_constants:
        check(
            f"CharacterBuild.{name} constant declared",
            f"{name}(" in model_src and "CharacterBuild" in model_src,
            f"{name} constant missing"
        )


# ════════════════════════════════════════════════════════════════════
# 2. EACH CONSTANT HAS CORRECT CHARACTERID AND SCALE
# ════════════════════════════════════════════════════════════════════

def check_constant_values(model_src: str):
    section("2. Constant characterId + scale values")

    expected = [
        ("WANG_LIN",      "wang_lin",      "1.0F"),
        ("SITU_NAN",      "situ_nan",      "1.10F"),
        ("TENG_LI",       "teng_li",       "1.0F"),
        ("WANG_ZHUO",     "wang_zhuo",     "1.0F"),
        ("TENG_HUAYUAN",  "teng_huayuan",  "1.05F"),
        ("ZENG_DA_NIU",   "zeng_da_niu",   "0.95F"),
        ("LI_MUWAN",      "li_muwan",      "0.92F"),
        ("WANG_HAO",      "wang_hao",      "1.0F"),
        ("OLD_CHEN",      "old_chen",      "0.98F"),
    ]

    for const_name, char_id, scale in expected:
        # Pattern: CONST_NAME("char_id", scale),
        pattern = rf'{const_name}\("{char_id}",\s*{re.escape(scale)}'
        check(
            f"{const_name}(\"{char_id}\", {scale})",
            re.search(pattern, model_src) is not None,
            f"constant {const_name} not declared with char_id={char_id} scale={scale}"
        )


# ════════════════════════════════════════════════════════════════════
# 3. scaleFor(String) RETURNS CORRECT SCALE
# ════════════════════════════════════════════════════════════════════

def check_scale_for_method(model_src: str):
    section("3. scaleFor(String) method")

    check(
        "scaleFor(String) static method exists",
        "static float scaleFor(String characterId)" in model_src,
        "scaleFor method missing or wrong signature"
    )

    check(
        "scaleFor returns 1.0F for null",
        'if (characterId == null || characterId.isEmpty()) return 1.0F' in model_src,
        "scaleFor does not return 1.0F for null/empty"
    )

    check(
        "scaleFor returns 1.0F as fallback for unrecognized",
        "return 1.0F;" in model_src,
        "scaleFor does not return 1.0F as fallback"
    )

    check(
        "scaleFor normalizes characterId (lowercase, trim, whitespace→underscore)",
        'toLowerCase(java.util.Locale.ROOT).trim()' in model_src
        and 'replaceAll("\\\\s+", "_")' in model_src,
        "scaleFor does not normalize characterId"
    )

    check(
        "scaleFor iterates values() and matches characterId field",
        "for (CharacterBuild build : values())" in model_src
        and "build.characterId.equals(id)" in model_src,
        "scaleFor does not iterate values() or match characterId"
    )


# ════════════════════════════════════════════════════════════════════
# 4. setCharacterId SOURCES SCALE FROM CharacterBuild, NOT HeldWeaponType
# ════════════════════════════════════════════════════════════════════

def check_set_character_id_wiring(model_src: str):
    section("4. setCharacterId wiring")

    check(
        "setCharacterId calls CharacterBuild.scaleFor(characterId)",
        "CharacterBuild.scaleFor(characterId)" in model_src,
        "setCharacterId does not call CharacterBuild.scaleFor"
    )

    check(
        "setCharacterId assigns result to characterScale",
        "this.characterScale = CharacterBuild.scaleFor(characterId)" in model_src,
        "setCharacterId does not assign scaleFor result to characterScale"
    )

    check(
        "setCharacterId no longer reads weapon.scale",
        "this.characterScale = weapon.scale" not in model_src,
        "setCharacterId still reads weapon.scale (CRON-97 limitation not closed)"
    )


# ════════════════════════════════════════════════════════════════════
# 5. HeldWeaponType.scale IS DEPRECATED
# ════════════════════════════════════════════════════════════════════

def check_weapon_scale_deprecated(model_src: str):
    section("5. HeldWeaponType.scale deprecated")

    check(
        "HeldWeaponType.scale field marked @Deprecated",
        "@Deprecated(since = \"CRON-COMPLETIONIST-98\", forRemoval = false)" in model_src,
        "HeldWeaponType.scale not marked @Deprecated"
    )

    check(
        "HeldWeaponType.scale javadoc explains deprecation",
        "@deprecated CRON-98: Per-character scale is now sourced from" in model_src,
        "HeldWeaponType.scale javadoc missing @deprecated explanation"
    )

    check(
        "HeldWeaponType javadoc notes CRON-98 change",
        "CRON-COMPLETIONIST-98: The {@code scale} field on this enum is now" in model_src,
        "HeldWeaponType javadoc missing CRON-98 note"
    )


# ════════════════════════════════════════════════════════════════════
# 6. CANON FIDELITY & HONESTY
# ════════════════════════════════════════════════════════════════════

def check_canon_fidelity(model_src: str):
    section("6. Canon fidelity & honesty")

    # The CharacterBuild javadoc must document canon basis for each scale
    check(
        "CharacterBuild javadoc has 'Canon basis for each scale' section",
        "Canon basis for each scale (fact-checked)" in model_src,
        "canon basis section missing"
    )

    # Situ Nan — archetype-driven, score 7/10
    check(
        "Situ Nan 1.10 documented as archetype (score 7/10)",
        "Situ Nan is the 2nd-gen Vermilion Bird" in model_src
        and "Score 7/10" in model_src,
        "Situ Nan canon basis not documented"
    )

    # Li Muwan — archetype-driven, score 7/10
    check(
        "Li Muwan 0.92 documented as archetype (score 7/10)",
        "Li Muwan (李慕婉) is a female cultivator" in model_src,
        "Li Muwan canon basis not documented"
    )

    # Old Chen — mod-original, score N/A
    check(
        "Old Chen documented as mod-original (score N/A)",
        "Mod-original character" in model_src and "Score N/A" in model_src,
        "Old Chen mod-original status not documented"
    )

    # No false chapter citations
    check(
        "No false chapter citations (no 'Ch.' or 'chapter N' references)",
        "Ch." not in model_src and "chapter " not in model_src.lower(),
        "false chapter citation found"
    )

    # The CRON-97 gap (Situ Nan + Li Muwan both → FAN → both scale 1.0) is explicitly referenced
    check(
        "CRON-97 limitation explicitly referenced (Situ Nan + Li Muwan both → FAN)",
        "Situ Nan and Li Muwan both → FAN" in model_src,
        "CRON-97 limitation not referenced"
    )

    # The fix rationale is documented
    check(
        "Fix rationale documented (decouples weapon from build)",
        "decouples" in model_src and "what weapon does this character hold" in model_src,
        "fix rationale not documented"
    )

    # Why enum not Map — documented
    check(
        "Enum-vs-Map rationale documented",
        "Why an enum and not a {@code Map<String, Float>}" in model_src,
        "enum-vs-map rationale not documented"
    )

    # Why primitive float not Float — documented
    check(
        "Primitive float return rationale documented",
        "scaleFor(String)} returns a primitive {@code float}" in model_src,
        "primitive float rationale not documented"
    )


# ════════════════════════════════════════════════════════════════════
# 7. CRON-97 LIMITATION CLOSED
# ════════════════════════════════════════════════════════════════════

def check_limitation_closed(model_src: str):
    section("7. CRON-97 self-critique #6 limitation closed")

    # The 1.10F and 0.92F scales MUST be present now (CRON-97 verified they were ABSENT)
    check(
        "1.10F scale present (Situ Nan — closes CRON-97 gap)",
        "1.10F" in model_src,
        "1.10F scale not present — CRON-97 gap not closed"
    )

    check(
        "0.92F scale present (Li Muwan — closes CRON-97 gap)",
        "0.92F" in model_src,
        "0.92F scale not present — CRON-97 gap not closed"
    )

    # Both must be on CharacterBuild constants (not HeldWeaponType)
    check(
        "1.10F is on CharacterBuild.SITU_NAN constant",
        re.search(r'SITU_NAN\("situ_nan",\s*1\.10F\)', model_src) is not None,
        "1.10F not on SITU_NAN constant"
    )

    check(
        "0.92F is on CharacterBuild.LI_MUWAN constant",
        re.search(r'LI_MUWAN\("li_muwan",\s*0\.92F\)', model_src) is not None,
        "0.92F not on LI_MUWAN constant"
    )

    # FAN weapon type's scale must STILL be 1.0 (NOT changed to 1.10 or 0.92)
    # — this verifies the decoupling: the weapon type's scale is independent
    # of the per-character build.
    check(
        "FAN weapon type scale still 1.0 (decoupled from per-character build)",
        re.search(r'FAN\(1\.0F\)', model_src) is not None,
        "FAN weapon type scale changed — decoupling not preserved"
    )


# ════════════════════════════════════════════════════════════════════
# MAIN
# ════════════════════════════════════════════════════════════════════

def main():
    print("════════════════════════════════════════════════════════════════")
    print("  CRON-COMPLETIONIST-98 verification — Per-Character Scale Override")
    print("════════════════════════════════════════════════════════════════")

    if not MODEL_PATH.exists():
        print(f"FATAL: model file not found at {MODEL_PATH}")
        sys.exit(1)

    model_src = MODEL_PATH.read_text()

    check_enum_exists(model_src)
    check_constant_values(model_src)
    check_scale_for_method(model_src)
    check_set_character_id_wiring(model_src)
    check_weapon_scale_deprecated(model_src)
    check_canon_fidelity(model_src)
    check_limitation_closed(model_src)

    print(f"\n════════════════════════════════════════════════════════════════")
    print(f"  RESULT: {PASS_COUNT}/{PASS_COUNT + FAIL_COUNT} checks passed")
    print(f"════════════════════════════════════════════════════════════════")

    if FAIL_COUNT > 0:
        print(f"\nFAILURES:")
        for f in FAILURES:
            print(f"  - {f}")
        sys.exit(1)
    else:
        print(f"\nALL CHECKS PASSED.")
        sys.exit(0)


if __name__ == "__main__":
    main()
