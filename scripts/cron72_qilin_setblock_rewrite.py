#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-72: QilinCityBuilder setBlock -> set() rewrite.

QilinCityBuilder has 239 raw `level.setBlock(...)` calls scattered through
its 1195-line body. Unlike the other 3 ServerLevel-only builders, it does
NOT have a set() helper — every call is inline. Migrating each call by
hand is error-prone and tedious, so this script does it mechanically.

Strategy:
  1. Add a `set(level, BlockPos, BlockState, int)` overload that delegates
     to `sb(level, ...)` (the chunk-scoped + provenance-aware helper).
  2. Add a `set(level, x, y, z, BlockState, int)` overload that wraps
     new BlockPos(x,y,z) and delegates to sb.
  3. Replace every `level.setBlock(` with `set(level, ` everywhere.

This is a single-pass sed-style replacement, safe because:
  - `level.setBlock(` is always called with the signature
    (BlockPos, BlockState, int). The new set() helper takes the same
    signature, so no argument reshuffling is needed.
  - There are no calls like `level.setBlockAndUpdate(` (which would also
    match `level.setBlock(`) — verified via grep before running.

After running, the only `level.setBlock` calls left in the file should be:
  - The single call inside `sb()` itself (the actual placement call).
"""

import re
import sys
from pathlib import Path

SRC = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse/spawn/QilinCityBuilder.java")

def main():
    text = SRC.read_text()
    original_text = text

    # Safety check: verify no setBlockAndUpdate calls (which would be missed).
    if "level.setBlockAndUpdate(" in text:
        print("ERROR: found level.setBlockAndUpdate( — needs separate handling", file=sys.stderr)
        sys.exit(1)

    # Count before
    before = text.count("level.setBlock(")
    print(f"Before: {before} `level.setBlock(` occurrences")

    # Replacement: turn every `level.setBlock(` into `set(level, `
    # This is safe because:
    #  - We're going to add a `set(ServerLevel, BlockPos, BlockState, int)` overload
    #    that takes the same argument shape.
    #  - After this replacement, the ONLY level.setBlock call left is inside sb()
    #    itself (which we'll add).
    text = text.replace("level.setBlock(", "set(level, ")

    after = text.count("level.setBlock(")
    print(f"After:  {after} `level.setBlock(` occurrences (should be 0)")

    if text == original_text:
        print("ERROR: no changes made", file=sys.stderr)
        sys.exit(1)

    SRC.write_text(text)
    print(f"Wrote: {SRC}")

if __name__ == "__main__":
    main()
