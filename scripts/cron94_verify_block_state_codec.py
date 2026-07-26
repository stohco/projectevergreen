#!/usr/bin/env python3
"""
CRON-94 verification script — checks that property-aware block state parsing
is correctly integrated into the WorldDeltaStore journal pipeline.

Verifies:
1. BlockStateCodec.java — serialize/parse methods, format handling, defensive error handling
2. PlayerBlockDeltaTracker.onBlockPlace — captures full BlockState string (not just block id)
3. WorldFacade.resolveBlockState — delegates to BlockStateCodec.parse (not defaultBlockState)
4. BlueprintChunkGenerator.resolveBlockState — delegates to BlockStateCodec.parse
5. BlockPlacementEngine — serializes full BlockState (not just block id)
6. ErgenDebugCommand — both sim-write and player-write validate via BlockStateCodec.parse
7. NO remaining defaultBlockState() calls in resolveBlockState paths
8. Backward compatibility — bare ids still work (no regression for old journal entries)
"""

import re
import sys
from pathlib import Path

FORGE_ROOT = Path("/home/z/my-project/forge-mod")
SRC_ROOT = FORGE_ROOT / "src/main/java/dev/ergenverse"

PASS = 0
FAIL = 0
FAILS = []

def check(name: str, condition: bool, detail: str = ""):
    global PASS, FAIL
    if condition:
        PASS += 1
    else:
        FAIL += 1
        FAILS.append(f"  FAIL  {name} — {detail}")
        print(f"  FAIL  {name} — {detail}")

def read(path: Path) -> str:
    return path.read_text()

def strip_comments(text: str) -> str:
    """Strip // line comments and /* */ block comments."""
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"//[^\n]*", "", text)
    return text

# ════════════════════════════════════════════════════════════════════
# CATEGORY 1: BlockStateCodec.java
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 1] BlockStateCodec.java")

codec_path = SRC_ROOT / "runtime/delta/BlockStateCodec.java"
check("BlockStateCodec.java exists", codec_path.exists())

if codec_path.exists():
    text = read(codec_path)
    code = strip_comments(text)

    # Class structure
    check("Is final class", "public final class BlockStateCodec" in text)
    check("Private constructor (utility class)", "private BlockStateCodec() {}" in code)

    # serialize method
    check("Has serialize(BlockState)", "public static String serialize(BlockState state)" in code)
    check("serialize delegates to toString()", "return state.toString();" in code)

    # parse method
    check("Has parse(String)", "public static BlockState parse(String blockStateString)" in code)
    check("parse handles null input", 'blockStateString == null' in code and "return null" in code)
    check("parse handles empty input", "blockStateString.isEmpty()" in code)
    check("parse splits on '['", "indexOf('[')" in code)
    check("parse splits properties on ','", 'split(",")' in code)
    check("parse splits key=value on '='", "indexOf('=')" in code)
    check("parse uses StateDefinition.getProperty", "definition.getProperty(propName)" in code)
    check("parse uses Property.getValue", "prop.getValue(valueName)" in code)
    check("parse calls setValue", "state.setValue(typedProp, typedValue)" in code)

    # Defensive error handling
    check("parse has try/catch (never throws)", "catch (Throwable t)" in code)
    check("parse logs unknown block", 'unknown block' in text)
    check("parse logs unknown property", "unknown property" in text)
    check("parse logs invalid value", "invalid value" in text)
    check("parse logs malformed property", "malformed property" in text)

    # Type-safe helper
    check("Has applyPropertyValue helper", "private static <T extends Comparable<T>> BlockState applyPropertyValue" in code)
    check("applyPropertyValue has @SuppressWarnings", "@SuppressWarnings(\"unchecked\")" in code)

    # CRON-94 documentation
    check("Mentions CRON-94", "CRON-COMPLETIONIST-94" in text)
    check("Documents format spec", "blockStateString := blockId" in text)
    check("Documents backward compat", "Backward compatibility" in text or "Backward compatible" in text)
    check("Mentions chest[facing=north] example", "minecraft:chest[facing=north" in text)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 2: PlayerBlockDeltaTracker.onBlockPlace
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 2] PlayerBlockDeltaTracker.onBlockPlace")

tracker_path = SRC_ROOT / "runtime/PlayerBlockDeltaTracker.java"
check("PlayerBlockDeltaTracker.java exists", tracker_path.exists())

if tracker_path.exists():
    text = read(tracker_path)
    code = strip_comments(text)

    # CRON-94 capture
    check("Uses BlockStateCodec.serialize", "BlockStateCodec.serialize(state)" in code)
    check("Passes stateString to setPlayerBlock (not blockId.toString())",
          "setPlayerBlock(pos.getX(), pos.getY(), pos.getZ(), stateString)" in code,
          "should pass stateString, not blockId.toString()")

    # No longer passes bare blockId
    check("Does NOT pass blockId.toString() to setPlayerBlock",
          "setPlayerBlock(pos.getX(), pos.getY(), pos.getZ(), blockId.toString())" not in code,
          "found old pattern: setPlayerBlock(..., blockId.toString())")

    # CRON-94 javadoc
    check("Has CRON-94 javadoc", "CRON-COMPLETIONIST-94" in text)
    check("Javadoc mentions chest[facing=north]", "minecraft:chest[facing=north" in text)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 3: WorldFacade.resolveBlockState
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 3] WorldFacade.resolveBlockState")

facade_path = SRC_ROOT / "runtime/layer/WorldFacade.java"
check("WorldFacade.java exists", facade_path.exists())

if facade_path.exists():
    text = read(facade_path)
    code = strip_comments(text)

    # Delegates to BlockStateCodec.parse
    check("resolveBlockState delegates to BlockStateCodec.parse",
          "BlockStateCodec.parse(blockId)" in code,
          "should call BlockStateCodec.parse(blockId)")

    # No longer calls defaultBlockState
    check("resolveBlockState does NOT call defaultBlockState()",
          "defaultBlockState()" not in code or
          # Allow defaultBlockState in other contexts (e.g., Blocks.STONE.defaultBlockState())
          # but not in the resolveBlockState method body
          "return block.defaultBlockState()" not in code,
          "found old pattern: return block.defaultBlockState()")

    # No longer has the old try/catch with ResourceLocation
    check("resolveBlockState does NOT have old ResourceLocation pattern",
          "new ResourceLocation(blockId)" not in code or
          "ForgeRegistries.BLOCKS.getValue(rl)" not in code,
          "found old ResourceLocation + ForgeRegistries pattern")

    # CRON-94 javadoc
    check("Has CRON-94 javadoc", "CRON-COMPLETIONIST-94" in text)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 4: BlueprintChunkGenerator.resolveBlockState
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 4] BlueprintChunkGenerator.resolveBlockState")

bcg_path = SRC_ROOT / "runtime/worldgen/BlueprintChunkGenerator.java"
check("BlueprintChunkGenerator.java exists", bcg_path.exists())

if bcg_path.exists():
    text = read(bcg_path)
    code = strip_comments(text)

    # Delegates to BlockStateCodec.parse
    check("resolveBlockState delegates to BlockStateCodec.parse",
          "BlockStateCodec.parse(blockId)" in code,
          "should call BlockStateCodec.parse(blockId)")

    # No longer calls defaultBlockState in resolveBlockState
    # (note: defaultBlockState may appear in fillFromNoise for stone/water/bedrock — those are fine)
    check("resolveBlockState does NOT return block.defaultBlockState()",
          "return block.defaultBlockState()" not in code,
          "found old pattern: return block.defaultBlockState()")

    # CRON-94 javadoc
    check("Has CRON-94 javadoc in resolveBlockState", "CRON-COMPLETIONIST-94" in text)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 5: BlockPlacementEngine
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 5] BlockPlacementEngine")

bpe_path = SRC_ROOT / "simulation/residence/BlockPlacementEngine.java"
check("BlockPlacementEngine.java exists", bpe_path.exists())

if bpe_path.exists():
    text = read(bpe_path)
    code = strip_comments(text)

    # Serializes full BlockState
    check("Uses BlockStateCodec.serialize",
          "BlockStateCodec.serialize(state)" in code,
          "should call BlockStateCodec.serialize(state)")

    # No longer passes rl.toString() to setSimulationBlock
    check("Does NOT pass rl.toString() to setSimulationBlock",
          "setSimulationBlock(\n                    pos.getX(), pos.getY(), pos.getZ(), rl.toString())" not in code
          and "pos.getZ(), rl.toString())" not in code,
          "found old pattern: setSimulationBlock(..., rl.toString())")

    # CRON-94 javadoc
    check("Has CRON-94 javadoc", "CRON-COMPLETIONIST-94" in text)

# ════════════════════════════════════════════════════════════════════
# CATEGORY 6: ErgenDebugCommand
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 6] ErgenDebugCommand")

cmd_path = SRC_ROOT / "command/ErgenDebugCommand.java"
check("ErgenDebugCommand.java exists", cmd_path.exists())

if cmd_path.exists():
    text = read(cmd_path)
    code = strip_comments(text)

    # Both sim-write and player-write validate via BlockStateCodec.parse
    parse_count = code.count("BlockStateCodec.parse(blockId)")
    check("Has 2 BlockStateCodec.parse(blockId) calls (sim-write + player-write)",
          parse_count == 2,
          f"found {parse_count} calls, expected 2")

    # No longer uses old ResourceLocation validation pattern
    check("Does NOT use new ResourceLocation(blockId) for validation",
          "new net.minecraft.resources.ResourceLocation(blockId)" not in code,
          "found old validation pattern")

    # CRON-94 javadoc in both commands
    cron94_count = text.count("CRON-COMPLETIONIST-94")
    check("Has 2 CRON-94 javadoc comments (sim-write + player-write)",
          cron94_count >= 2,
          f"found {cron94_count} CRON-94 mentions, expected >= 2")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 7: Global audit — no remaining defaultBlockState() in resolveBlockState paths
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 7] Global audit — resolveBlockState purity")

# Check that no resolveBlockState method still calls defaultBlockState
for java_file in SRC_ROOT.rglob("*.java"):
    text = read(java_file)
    code = strip_comments(text)
    # Find resolveBlockState method bodies
    # Simple heuristic: if a file has "resolveBlockState" AND "defaultBlockState" in code,
    # check if they're in the same method
    if "resolveBlockState" in code and "return block.defaultBlockState()" in code:
        rel = java_file.relative_to(SRC_ROOT)
        check(f"{rel}: resolveBlockState does NOT return block.defaultBlockState()",
              False,
              "found 'return block.defaultBlockState()' in a file that also has resolveBlockState")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 8: Backward compatibility checks
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 8] Backward compatibility")

if codec_path.exists():
    text = read(codec_path)

    # Bare ids (no properties) should still work — the parser must handle
    # the case where there's no '[' in the string
    check("Parser handles bare ids (no '[' in string)",
          "int bracketIdx = blockStateString.indexOf('[')" in text and
          "idPart = blockStateString" in text,
          "parser must set idPart = full string when no '[' present")

    # Empty properties "minecraft:chest[]" should also work
    check("Parser handles empty properties '[]'",
          "closeIdx > bracketIdx + 1" in text,
          "parser must skip empty properties when closeIdx == bracketIdx + 1")

    # Old journal entries (bare ids) will still resolve to default state
    check("Backward compat documented",
          "old journal entries" in text or "existing saves" in text,
          "should document that old bare-id journal entries still work")

# ════════════════════════════════════════════════════════════════════
# CATEGORY 9: BlockChangeDelta javadoc updated
# ════════════════════════════════════════════════════════════════════
print("\n[CATEGORY 9] BlockChangeDelta javadoc")

bcd_path = SRC_ROOT / "runtime/delta/BlockChangeDelta.java"
check("BlockChangeDelta.java exists", bcd_path.exists())

if bcd_path.exists():
    text = read(bcd_path)
    # Javadoc should mention full state strings
    check("Javadoc mentions full state strings",
          "minecraft:chest[facing=north" in text,
          "should mention 'minecraft:chest[facing=north,...]' as a valid blockState value")
    check("Javadoc mentions CRON-94",
          "CRON-94" in text or "CRON-COMPLETIONIST-94" in text,
          "should mention CRON-94 in the javadoc")

# ════════════════════════════════════════════════════════════════════
# SUMMARY
# ════════════════════════════════════════════════════════════════════
print(f"\n{'='*70}")
print(f"RESULTS: {PASS} passed, {FAIL} failed, {PASS + FAIL} total")
if FAILS:
    print(f"\nFAILURES:")
    for f in FAILS:
        print(f)
    sys.exit(1)
else:
    print("\nALL CHECKS PASSED.")
    sys.exit(0)
