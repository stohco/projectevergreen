#!/usr/bin/env python3
"""
CRON-93 batch updater: replace canonSurfaceHeight(x, z) with surfaceHeightFor(level, x, z)
in all 12 callers (11 structure builders + SpawnEventHandler).

Pattern: BlueprintChunkGenerator.canonSurfaceHeight(A, B)
       → BlueprintChunkGenerator.surfaceHeightFor(<level>, A, B)

Where <level> is the variable name already in scope at the call site.
For the 11 builders, this is always `level` (their getSectCenter takes ServerLevel level).
For SpawnEventHandler, it's `suzakuLevel` (the variable holding the Suzaku level).

This script does NOT touch:
- BlueprintChunkGenerator.java itself (legacy static method is RETAINED as fallback)
- CanonGeographyPlacer.java (only mentions canonSurfaceHeight in a comment, no call)
- Any other file (grep confirmed only 14 callers total)
"""

import re
import sys
from pathlib import Path

# (file, level_var_name) — the level variable in scope at the canonSurfaceHeight call site
TARGETS = [
    ("src/main/java/dev/ergenverse/spawn/LuoHeSectBuilder.java",               "level"),
    ("src/main/java/dev/ergenverse/spawn/SoulRefiningSectBuilder.java",        "level"),
    ("src/main/java/dev/ergenverse/spawn/VermilionBirdImperialCityBuilder.java","level"),
    ("src/main/java/dev/ergenverse/spawn/TianShuiCityBuilder.java",            "level"),
    ("src/main/java/dev/ergenverse/spawn/HengYueSectBuilder.java",             "level"),
    ("src/main/java/dev/ergenverse/spawn/WangFamilyVillageBuilder.java",       "level"),
    ("src/main/java/dev/ergenverse/spawn/XuanDaoSectBuilder.java",             "level"),
    ("src/main/java/dev/ergenverse/spawn/NanDouCityBuilder.java",              "level"),
    ("src/main/java/dev/ergenverse/spawn/SnowDomainCapitalBuilder.java",       "level"),
    ("src/main/java/dev/ergenverse/spawn/QilinCityBuilder.java",               "level"),
    ("src/main/java/dev/ergenverse/spawn/TengFamilyCityBuilder.java",          "level"),
    ("src/main/java/dev/ergenverse/spawn/SpawnEventHandler.java",              "suzakuLevel"),
]

# Pattern matches: BlueprintChunkGenerator.canonSurfaceHeight(EXPR, EXPR)
# Where EXPR is a Java identifier (constant or variable).
# Captures the two argument expressions.
PATTERN = re.compile(
    r"(BlueprintChunkGenerator\.canonSurfaceHeight\()(\s*)([A-Za-z_][A-Za-z0-9_]*)(\s*,\s*)([A-Za-z_][A-Za-z0-9_]*)(\s*\))"
)

def transform(text: str, level_var: str) -> tuple[str, int]:
    """Replace canonSurfaceHeight(a, b) → surfaceHeightFor(level_var, a, b). Return (new_text, count)."""
    def repl(m):
        return f"{m.group(1).replace('canonSurfaceHeight', 'surfaceHeightFor')}{m.group(2)}{level_var}{m.group(4)}{m.group(3)},{m.group(4)}{m.group(5)}{m.group(6)}"
    # Simpler: build replacement directly
    def repl2(m):
        prefix = m.group(1).replace("canonSurfaceHeight", "surfaceHeightFor")
        ws1 = m.group(2)
        arg1 = m.group(3)
        comma_ws = m.group(4)
        arg2 = m.group(5)
        close = m.group(6)
        return f"{prefix}{ws1}{level_var}{comma_ws}{arg1}{comma_ws}{arg2}{close}"
    new_text, count = PATTERN.subn(repl2, text)
    return new_text, count


def main():
    forge_root = Path("/home/z/my-project/forge-mod")
    total_changes = 0
    failures = []
    for rel_path, level_var in TARGETS:
        path = forge_root / rel_path
        if not path.exists():
            failures.append(f"MISSING: {path}")
            continue
        text = path.read_text()
        new_text, count = transform(text, level_var)
        if count == 0:
            failures.append(f"NO MATCH: {rel_path} (expected 1 canonSurfaceHeight call)")
            continue
        if count > 1:
            failures.append(f"MULTIPLE MATCHES ({count}): {rel_path} — manual review needed")
            continue
        path.write_text(new_text)
        print(f"  UPDATED: {rel_path} ({count} call → surfaceHeightFor({level_var}, ...))")
        total_changes += 1
    print(f"\nTotal files updated: {total_changes}/{len(TARGETS)}")
    if failures:
        print("\nFAILURES:")
        for f in failures:
            print(f"  - {f}")
        sys.exit(1)
    print("\nAll callers migrated to surfaceHeightFor(level, x, z).")


if __name__ == "__main__":
    main()
