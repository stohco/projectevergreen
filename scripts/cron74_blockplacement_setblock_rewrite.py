#!/usr/bin/env python3
"""
CRON-74: Migrate BlockPlacementEngine's 23 direct level.setBlock(...) calls
to use the existing setBlock(level, pos, state) helper, which routes through
WorldRuntime.get().world().setSimulationBlock(...) when the runtime is
initialized and the level is Planet Suzaku.

The ONE fallback call inside the setBlock helper itself (level.setBlock(pos, state, 3))
is PROTECTED and left as-is — it's the direct-write fallback for non-Suzaku levels.

Strategy: parser-based replacement that tracks paren depth to correctly handle
nested parens in pos expressions (e.g., origin.offset(2, 0, 1)) and multi-line
state expressions (e.g., Blocks.IRON_DOOR.defaultBlockState().setValue(...)).
"""
import sys

FILEPATH = "/home/z/my-project/forge-mod/src/main/java/dev/ergenverse/simulation/residence/BlockPlacementEngine.java"

FALLBACK_LINE = "level.setBlock(pos, state, 3);"
PLACEHOLDER = "__CRON74_FALLBACK_PROTECTED__"


def split_top_level_commas(s):
    """Split a string at top-level commas (depth 0), respecting (), [], {}."""
    parts = []
    depth = 0
    current = []
    for ch in s:
        if ch in '([{':
            depth += 1
            current.append(ch)
        elif ch in ')]}':
            depth -= 1
            current.append(ch)
        elif ch == ',' and depth == 0:
            parts.append(''.join(current))
            current = []
        else:
            current.append(ch)
    if current:
        parts.append(''.join(current))
    return parts


def migrate_setblock_calls(text):
    """Find each level.setBlock(...) call and migrate to setBlock(level, pos, state)."""
    result = []
    i = 0
    call_count = 0
    skipped = 0
    while i < len(text):
        idx = text.find("level.setBlock(", i)
        if idx == -1:
            result.append(text[i:])
            break
        # Append text up to the match
        result.append(text[i:idx])
        # Start of args (after "level.setBlock(")
        args_start = idx + len("level.setBlock(")
        # Find matching ")" tracking paren depth
        depth = 1
        j = args_start
        while j < len(text) and depth > 0:
            ch = text[j]
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            j += 1
        # j is now just past the matching ")"
        args_str = text[args_start:j - 1]  # exclude the ")"
        # Split args at top-level commas
        parts = split_top_level_commas(args_str)
        # Expect 3 parts: pos, state, flags
        if len(parts) == 3 and parts[2].strip() == "3":
            pos = parts[0].strip()
            state = parts[1].strip()
            # For multi-line state expressions, preserve newlines
            if '\n' in args_str:
                # Multi-line: format nicely
                result.append(f"setBlock(level, {pos},\n                {state})")
            else:
                result.append(f"setBlock(level, {pos}, {state})")
            call_count += 1
        else:
            # Not a 3-arg call with flags=3; leave as-is
            result.append(text[idx:j])
            skipped += 1
        i = j
    print(f"  Migrated {call_count} calls, skipped {skipped} non-matching calls")
    return ''.join(result)


def main():
    with open(FILEPATH, 'r') as f:
        content = f.read()

    # Step 1: Count before
    before = content.count("level.setBlock(")
    print(f"Before: {before} 'level.setBlock(' occurrences")

    # Step 2: Protect the fallback line inside the setBlock helper
    if FALLBACK_LINE not in content:
        print("ERROR: Fallback line not found! Aborting.")
        sys.exit(1)
    content = content.replace(FALLBACK_LINE, PLACEHOLDER, 1)
    protected_count = content.count("level.setBlock(")
    print(f"After protecting fallback: {protected_count} 'level.setBlock(' occurrences")

    # Step 3: Migrate all remaining level.setBlock calls
    content = migrate_setblock_calls(content)

    # Step 4: Restore the fallback
    content = content.replace(PLACEHOLDER, FALLBACK_LINE)

    # Step 5: Count after
    after = content.count("level.setBlock(")
    print(f"After: {after} 'level.setBlock(' occurrences (should be 1 — the fallback)")

    # Step 6: Write
    with open(FILEPATH, 'w') as f:
        f.write(content)
    print("Migration complete.")


if __name__ == "__main__":
    main()
