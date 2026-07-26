#!/usr/bin/env python3
"""
CRON-115: Canon correction — 慕冰媚 → 木冰眉.

Verified via Baidu Baike (https://baike.baidu.com/item/木冰眉/8802287):
  Mu Bingmei's correct Chinese name is 木冰眉 (wood-ice-brow), NOT 慕冰媚
  (admire-ice-charm). The mod has been using the wrong characters since
  CRON-111 — a canon-fidelity violation per the same standard as CRON-69's
  藤厉 not "Teng Lijun" correction.

This script:
  1. Replaces 慕冰媚 → 木冰眉 in all mod-loaded Java + JSON files.
  2. Updates the "true_identity" note in npc_mu_bingmei.json to reflect
     that 柳眉 is Mu Bingmei's NINTH AVATAR (第九分身), not just "a mask
     she wore in the mortal world".
  3. Updates the "sources" field to cite the Baidu Baike page for 木冰眉.
  4. Does NOT touch:
     - worklog.md (historical record)
     - Root-level research files (ri_canon_database.json, ri_npcs_extracted.json,
       ri_canon_missing_npcs.json, CANON_RI_COMPLETE_WORLD.md) — these are
       extraction artifacts, not mod-loaded resources.
     - Backup files (*.bak)

Reports the per-file change count.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

FORGE_MOD = Path("/home/z/my-project/forge-mod")

# Mod-loaded Java source files (10)
JAVA_FILES = [
    "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java",
    "src/main/java/dev/ergenverse/runtime/CanonUUID.java",
    "src/main/java/dev/ergenverse/runtime/NPCRuntime.java",
    "src/main/java/dev/ergenverse/runtime/materialize/CanonActorMaterializer.java",
    "src/main/java/dev/ergenverse/wanglin/RICanonicalDatabase.java",
    "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuKunxuDepartureEvent.java",
    "src/main/java/dev/ergenverse/wanglin/bead/MuBingmeiAcceptanceEvent.java",
    "src/main/java/dev/ergenverse/wanglin/bead/ZhouRuCultivationGrowthService.java",
    "src/main/java/dev/ergenverse/wanglin/registry/CanonicalAllies.java",
]

# Mod-loaded JSON resource files (2)
JSON_FILES = [
    "src/main/resources/data/ergenverse/ri_canon_database.json",
    "src/main/resources/data/ergenverse/npcs/npc_mu_bingmei.json",
]

WRONG = "慕冰媚"
RIGHT = "木冰眉"

# The "true_identity" note in npc_mu_bingmei.json previously said:
#   "Mu Bingmei (慕冰媚) is Liu Mei's (柳眉) true form — they are the same
#    person. Liu Mei was the public identity; Mu Bingmei is the true
#    cultivation name."
# Canon-accurate version (per Baidu Baike):
#   "Mu Bingmei (木冰眉) is Liu Mei's (柳眉) true form — they are the same
#    person. Liu Mei is Mu Bingmei's ninth avatar (第九分身); Mu Bingmei is
#    the 7th-generation Saintess of the Kunxu Realm (昆虚之境)."
OLD_TRUE_IDENTITY = (
    "Mu Bingmei (慕冰媚) is Liu Mei's (柳眉) true form — they are the same person. "
    "Liu Mei was the public identity; Mu Bingmei is the true cultivation name."
)
NEW_TRUE_IDENTITY = (
    "Mu Bingmei (木冰眉) is Liu Mei's (柳眉) true form — they are the same person. "
    "Liu Mei is Mu Bingmei's ninth avatar (第九分身); Mu Bingmei is the 7th-generation "
    "Saintess of the Kunxu Realm (昆虚之境)."
)

# The "sources" field previously cited "Baidu Baike (Liu Mei = Mu Bingmei)".
# Canon-accurate citation: Baidu Baike page for 木冰眉.
OLD_SOURCES_FRAGMENT = "Baidu Baike (Liu Mei = Mu Bingmei)"
NEW_SOURCES_FRAGMENT = "Baidu Baike (木冰眉 — https://baike.baidu.com/item/木冰眉/8802287)"


def fix_file(path: Path) -> tuple[int, int, int]:
    """Returns (char_count, true_identity_count, sources_count)."""
    text = path.read_text(encoding="utf-8")
    char_count = text.count(WRONG)
    new_text = text.replace(WRONG, RIGHT)

    true_identity_count = new_text.count(OLD_TRUE_IDENTITY)
    new_text = new_text.replace(OLD_TRUE_IDENTITY, NEW_TRUE_IDENTITY)

    sources_count = new_text.count(OLD_SOURCES_FRAGMENT)
    new_text = new_text.replace(OLD_SOURCES_FRAGMENT, NEW_SOURCES_FRAGMENT)

    if new_text != text:
        path.write_text(new_text, encoding="utf-8")

    return (char_count, true_identity_count, sources_count)


def main() -> int:
    print(f"CRON-115: Canon correction {WRONG} → {RIGHT}")
    print(f"  Verified via Baidu Baike: https://baike.baidu.com/item/木冰眉/8802287")
    print(f"  Liu Mei = Mu Bingmei's 9th avatar (第九分身), NOT a 'mask'")
    print()

    total_char = 0
    total_ti = 0
    total_src = 0

    print("Java files:")
    for rel in JAVA_FILES:
        path = FORGE_MOD / rel
        if not path.exists():
            print(f"  MISSING: {rel}")
            continue
        c, ti, src = fix_file(path)
        total_char += c
        total_ti += ti
        total_src += src
        print(f"  {rel}: {c} char, {ti} true_identity, {src} sources")

    print("\nJSON files:")
    for rel in JSON_FILES:
        path = FORGE_MOD / rel
        if not path.exists():
            print(f"  MISSING: {rel}")
            continue
        c, ti, src = fix_file(path)
        total_char += c
        total_ti += ti
        total_src += src
        print(f"  {rel}: {c} char, {ti} true_identity, {src} sources")

    print()
    print(f"TOTAL: {total_char} char replacements, {total_ti} true_identity updates, {total_src} sources updates")

    # Verify no remaining instances in the targeted files
    print("\nVerification — remaining instances of wrong character:")
    remaining = 0
    for rel in JAVA_FILES + JSON_FILES:
        path = FORGE_MOD / rel
        if not path.exists():
            continue
        text = path.read_text(encoding="utf-8")
        count = text.count(WRONG)
        if count > 0:
            print(f"  {rel}: {count} REMAINING")
            remaining += count
    if remaining == 0:
        print("  None — all targeted files clean.")
    else:
        print(f"  TOTAL REMAINING: {remaining}")
        return 1

    return 0


if __name__ == "__main__":
    sys.exit(main())
