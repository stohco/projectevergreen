#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-101 verification script.

Verifies the World Origin Essence (一界本源) item and its integration
with RevivalAttemptService. Checks across 10 categories:

1. WorldOriginEssenceItem.java — file, package, class, javadoc, fields
2. Item properties — EPIC rarity, stacksTo(1), fireResistant
3. Canon basis — 一界本源 mentioned, no fabricated chapter citation
4. Tooltip — bilingual header, source world, canon significance
5. Static helpers — createFromWorld, createDefault, findInInventory
6. ErgenverseItems registration — WORLD_ORIGIN_ESSENCE constant
7. Creative tab — World Origin Essence added
8. Item model + texture — JSON + PNG exist, correct dimensions
9. RevivalAttemptService — 6th gate added, essence consumed, HistoryManager
10. /ergenverse give_essence command — registered, optional world_name arg
"""
import sys
import os
import json

FORGE = "/home/z/my-project/forge-mod"
SRC = f"{FORGE}/src/main/java"
RES = f"{FORGE}/src/main/resources"

failures = []
checks = 0

def check(cond, name):
    global checks
    checks += 1
    if cond:
        print(f"  ✓ {name}")
    else:
        print(f"  ✗ FAIL: {name}")
        failures.append(name)

def read(path):
    try:
        with open(path, "r", encoding="utf-8") as f:
            return f.read()
    except Exception as e:
        return ""

# ─── 1. WorldOriginEssenceItem.java ─────────────────────────────────
print("\n[1] WorldOriginEssenceItem.java — file & class")
item_path = f"{SRC}/dev/ergenverse/item/WorldOriginEssenceItem.java"
item_src = read(item_path)
check("package dev.ergenverse.item;" in item_src, "package is dev.ergenverse.item")
check("public class WorldOriginEssenceItem extends Item" in item_src,
      "class declaration: extends Item")
check("CRON-COMPLETIONIST-101" in item_src, "CRON-101 marker in javadoc")
check("一界本源" in item_src, "canon term 一界本源 in javadoc")
check("World Origin Essence" in item_src, "English name in javadoc")
check("王林踏入第四步" in item_src or "踏入第四步" in item_src,
      "canon quote 王林踏入第四步 in javadoc")
check("逆尘界" in item_src, "canon source world 逆尘界 (Ni Chen Realm) mentioned")
check("戮默" in item_src, "canon detail 戮默 (Lu Mo) mentioned — Wang Lin's avatar")
check("NO fabricated chapter citation" in item_src,
      "canon honesty: NO fabricated chapter citation")

# ─── 2. Item properties ─────────────────────────────────────────────
print("\n[2] Item properties — EPIC rarity, stacksTo(1), fireResistant")
check("Rarity.EPIC" in item_src, "rarity EPIC")
check(".stacksTo(1)" in item_src, "stacksTo(1) — unique artifact")
check(".fireResistant()" in item_src, "fireResistant — world-origin cannot be destroyed")

# ─── 3. Canon basis & NBT fields ────────────────────────────────────
print("\n[3] Canon basis & NBT fields")
check("NBT_SOURCE_WORLD" in item_src, "NBT_SOURCE_WORLD constant defined")
check('"Ergenverse.WorldOriginEssence.SourceWorld"' in item_src,
      "NBT tag string is Ergenverse.WorldOriginEssence.SourceWorld")
check("DEFAULT_SOURCE_WORLD" in item_src, "DEFAULT_SOURCE_WORLD constant defined")
check('"未明之界"' in item_src, "default world name is 未明之界 (Unnamed World)")

# ─── 4. Tooltip ─────────────────────────────────────────────────────
print("\n[4] Tooltip — bilingual header, source world, canon significance")
check("appendHoverText" in item_src, "appendHoverText override exists")
check("ChatFormatting.LIGHT_PURPLE" in item_src, "LIGHT_PURPLE formatting")
check("ChatFormatting.DARK_PURPLE" in item_src, "DARK_PURPLE formatting")
check("ChatFormatting.GOLD" in item_src, "GOLD formatting")
check("ChatFormatting.GRAY" in item_src, "GRAY formatting")
check("World Origin Essence" in item_src, "English name in tooltip")
check("一界本源" in item_src, "Chinese name in tooltip")
check("Source World:" in item_src, "Source World display in tooltip")
check("Wang Lin uses 一界本源 to revive" in item_src,
      "canon reference in tooltip")
check("Required for the final successful revival" in item_src,
      "usage hint in tooltip")
check("TRANSCENDENCE" in item_src, "TRANSCENDENCE realm mentioned in tooltip")
check("137 failed revival attempts" in item_src,
      "137 attempts mentioned in tooltip")

# ─── 5. Static helpers ──────────────────────────────────────────────
print("\n[5] Static helpers — createFromWorld, createDefault, findInInventory")
check("public static ItemStack createFromWorld(String sourceWorldName)" in item_src,
      "createFromWorld(String) signature")
check("public static ItemStack createDefault()" in item_src,
      "createDefault() signature")
check("public static ItemStack findInInventory(net.minecraft.server.level.ServerPlayer player)" in item_src,
      "findInInventory(ServerPlayer) signature")
check("getMainHandItem" in item_src, "findInInventory scans main hand")
check("getOffhandItem" in item_src, "findInInventory scans off hand")
check("getInventory().items" in item_src, "findInInventory scans main inventory")
check("instanceof WorldOriginEssenceItem" in item_src,
      "findInInventory uses instanceof check")
check("return ItemStack.EMPTY" in item_src or "return net.minecraft.world.item.ItemStack.EMPTY" in item_src,
      "findInInventory returns EMPTY when not found")

# ─── 6. ErgenverseItems registration ────────────────────────────────
print("\n[6] ErgenverseItems registration")
items_path = f"{SRC}/dev/ergenverse/item/ErgenverseItems.java"
items_src = read(items_path)
check("WORLD_ORIGIN_ESSENCE" in items_src, "WORLD_ORIGIN_ESSENCE constant defined")
check('ITEMS.register("world_origin_essence"' in items_src,
      'registered as "world_origin_essence"')
check("WorldOriginEssenceItem" in items_src, "uses WorldOriginEssenceItem class")
check("CRON-COMPLETIONIST-101" in items_src, "CRON-101 marker in registration comment")
check("一界本源" in items_src, "canon term in registration comment")

# ─── 7. Creative tab ────────────────────────────────────────────────
print("\n[7] Creative tab — World Origin Essence added")
check("WORLD_ORIGIN_ESSENCE.get()" in items_src,
      "WORLD_ORIGIN_ESSENCE.get() in creative tab output.accept")

# ─── 8. Item model + texture ────────────────────────────────────────
print("\n[8] Item model + texture")
model_path = f"{RES}/assets/ergenverse/models/item/world_origin_essence.json"
texture_path = f"{RES}/assets/ergenverse/textures/item/world_origin_essence.png"
check(os.path.exists(model_path), "model JSON file exists")
if os.path.exists(model_path):
    model = json.load(open(model_path))
    check(model.get("parent") == "minecraft:item/generated",
          "model parent is minecraft:item/generated")
    tex = model.get("textures", {}).get("layer0", "")
    check(tex == "ergenverse:item/world_origin_essence",
          f"layer0 texture is ergenverse:item/world_origin_essence (got: {tex})")
check(os.path.exists(texture_path), "texture PNG file exists")
if os.path.exists(texture_path):
    size = os.path.getsize(texture_path)
    check(100 < size < 5000, f"texture PNG size reasonable (got {size} bytes)")
    # Verify it's a 16x16 PNG by reading the header
    with open(texture_path, "rb") as f:
        data = f.read(33)
        # PNG IHDR: bytes 16-24 are width/height (4 bytes each, big-endian)
        if len(data) >= 24 and data[:8] == b'\x89PNG\r\n\x1a\n':
            width = int.from_bytes(data[16:20], 'big')
            height = int.from_bytes(data[20:24], 'big')
            check(width == 16, f"texture width is 16 (got {width})")
            check(height == 16, f"texture height is 16 (got {height})")

# ─── 9. RevivalAttemptService — 6th gate + consumption ─────────────
print("\n[9] RevivalAttemptService — 6th essence gate + consumption")
rev_path = f"{SRC}/dev/ergenverse/wanglin/bead/RevivalAttemptService.java"
rev_src = read(rev_path)
check("import dev.ergenverse.item.WorldOriginEssenceItem;" in rev_src,
      "import WorldOriginEssenceItem")
check("SUBJECT_REVIVAL_ESSENCE_CONSUMED" in rev_src,
      "SUBJECT_REVIVAL_ESSENCE_CONSUMED constant defined")
check('"li_muwan_revival_essence_consumed"' in rev_src,
      "subject string is li_muwan_revival_essence_consumed")
check("Gate 6: Essence gate" in rev_src, "Gate 6: Essence gate comment")
check("CRON-COMPLETIONIST-101" in rev_src, "CRON-101 marker in service")
check("WorldOriginEssenceItem.findInInventory(player)" in rev_src,
      "calls WorldOriginEssenceItem.findInInventory(player)")
check("essenceStack.isEmpty()" in rev_src,
      "checks essenceStack.isEmpty() — gate rejection")
check("return doSuccessfulRevival(player, beadItem, stack, currentTick, essenceStack)" in rev_src,
      "passes essenceStack to doSuccessfulRevival")
check("private static boolean doSuccessfulRevival(ServerPlayer player,\n"
      "                                                HeavenDefyingBeadItem beadItem,\n"
      "                                                ItemStack stack,\n"
      "                                                long currentTick,\n"
      "                                                ItemStack essenceStack)" in rev_src
      or "doSuccessfulRevival(ServerPlayer player, HeavenDefyingBeadItem beadItem, ItemStack stack, long currentTick, ItemStack essenceStack)" in rev_src,
      "doSuccessfulRevival takes essenceStack parameter")
check("essenceStack.shrink(1)" in rev_src, "essenceStack.shrink(1) — consumes the item")
check("essenceStack.setCount(0)" in rev_src, "defensive setCount(0) after shrink")
check("SUBJECT_REVIVAL_ESSENCE_CONSUMED" in rev_src,
      "HistoryManager records essence-consumed subject")
# Check that the essence gate is in the SUCCESS path (after TRANSCENDENCE check)
trans_idx = rev_src.find("realm.isAtLeast(RealmId.TRANSCENDENCE)")
essence_gate_idx = rev_src.find("Gate 6: Essence gate")
check(trans_idx >= 0, "TRANSCENDENCE check found")
check(essence_gate_idx >= 0, "Essence gate found")
check(trans_idx < essence_gate_idx, "Essence gate is AFTER TRANSCENDENCE check (success path only)")
# Check essence gate is NOT in the failed-attempt path
failed_idx = rev_src.find("doFailedRevival")
check(essence_gate_idx < failed_idx or failed_idx < 0,
      "Essence gate is BEFORE doFailedRevival call (only in success path)")
# Canon-faithful message checks
check("你已踏入第四步" in rev_src, "essence-gate rejection has bilingual message (你已踏入第四步)")
check("一界本源" in rev_src, "essence-gate rejection mentions 一界本源")
check("Acquire World Origin Essence" in rev_src,
      "essence-gate rejection gives next-step hint")
# Success message checks
check("「" in rev_src and "」" in rev_src, "success message uses Chinese「」 brackets for world name")
check("A world has perished to restore her" in rev_src,
      "success message acknowledges world sacrifice")

# ─── 10. /ergenverse give_essence command ──────────────────────────
print("\n[10] /ergenverse give_essence command")
cmd_path = f"{SRC}/dev/ergenverse/spawn/ErgenverseCommand.java"
cmd_src = read(cmd_path)
check('Commands.literal("give_essence")' in cmd_src,
      "give_essence literal registered")
check("giveEssence(ctx.getSource(), null)" in cmd_src,
      "default giveEssence call (no world name)")
check("MessageArgument.message()" in cmd_src,
      "optional world_name argument via MessageArgument")
check("private static int giveEssence(CommandSourceStack src, String worldName)" in cmd_src,
      "giveEssence method signature")
check("WorldOriginEssenceItem.createDefault()" in cmd_src,
      "calls createDefault() when no world name")
check("WorldOriginEssenceItem.createFromWorld(worldName)" in cmd_src,
      "calls createFromWorld(worldName) when world name given")
check("player.getInventory().add(essenceStack)" in cmd_src,
      "adds essence to player inventory")
check("player.drop(essenceStack, false)" in cmd_src,
      "drops essence at feet if inventory full")
check("CRON-101" in cmd_src, "CRON-101 marker in command")
check("give_essence" in cmd_src, "give_essence in registration log")

# ─── 11. HeavenDefyingBeadItem javadoc updated ─────────────────────
print("\n[11] HeavenDefyingBeadItem javadoc mentions essence gate")
bead_path = f"{SRC}/dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.java"
bead_src = read(bead_path)
# Collapse whitespace AND javadoc line-prefix asterisks so wrapped phrases match
import re
bead_flat = re.sub(r'\s*\*\s*', ' ', bead_src)
bead_flat = re.sub(r'\s+', ' ', bead_flat)
check("World Origin Essence" in bead_flat,
      "HeavenDefyingBeadItem javadoc mentions World Origin Essence")
check("一界本源" in bead_flat, "HeavenDefyingBeadItem javadoc mentions 一界本源")
check("CRON-COMPLETIONIST-101" in bead_flat,
      "HeavenDefyingBeadItem javadoc has CRON-101 marker")
check("Origin Essence is consumed on success" in bead_flat,
      "HeavenDefyingBeadItem javadoc notes consumption on success")

# ─── Final summary ──────────────────────────────────────────────────
print("\n" + "=" * 60)
if not failures:
    print(f"✅ ALL {checks} CHECKS PASSED.")
    sys.exit(0)
else:
    print(f"❌ {len(failures)} FAILURES out of {checks} checks:")
    for f in failures:
        print(f"   - {f}")
    sys.exit(1)
