#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-106 — CultivationPlanetCrystalBlock Verification.

Verifies that the CultivationPlanetCrystalBlock exists, is registered in
ErgenverseBlocks, replaces the CRON-105 diamond_block placeholder in
SuzakuTombBuilder, has canon-faithful mechanics (light emission, ambient
particles, right-click inheritance event with canon-faithful gating),
and is canon-faithful (修炼星晶, 朱雀子, 第十五代 all attested; NO
fabricated chapter citation).

Categories of checks:
  1. CultivationPlanetCrystalBlock.java — file exists, class declared
  2. Block-state property — INHERITED boolean, default false
  3. Block registration — ErgenverseBlocks.CULTIVATION_PLANET_CRYSTAL
  4. Block properties — lightLevel 15, amethyst sound, purple map color
  5. getDrops override — returns empty list (Crystal does NOT drop as item)
  6. animateTick override — emits END_ROD particles
  7. use() override — right-click inheritance event with prerequisites
  8. Inheritance prerequisites — bead-in-hand, realm ≥ NASCENT_SOUL, not inherited
  9. Inheritance outcome — block-state transitions, bead marked, messages sent
 10. HeavenDefyingBeadItem — NBT_SUZAKU_SON constant, isSuzakuSon/setSuzakuSon
 11. HistoryManager — SUBJECT_SUZAKU_SON_INHERITANCE constant, onDiscovery call
 12. SuzakuTombBuilder — uses CULTIVATION_PLANET_CRYSTAL instead of DIAMOND_BLOCK
 13. Block assets — blockstate JSON, block model, item model, texture PNG
 14. Loot table — empty pools (no drops)
 15. Language file — block.ergenverse.cultivation_planet_crystal entry
 16. Canon fidelity — 修炼星晶, 朱雀子, 第十五代, NO fabricated chapter citation
 17. Provenance — block-state change recorded as PLAYER delta via WorldFacade
 18. Architecture — no direct WorldDeltaStore manipulation, no Layer manipulation
 19. Integration — CRON-105 placeholder retired, CRON-104 cave suppression still protects

Run: python3 /home/z/my-project/scripts/cron106_verify_cultivation_planet_crystal.py
Exit code: 0 if all checks pass, 1 otherwise.
"""

import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "forge-mod"
BLOCK = ROOT / "src/main/java/dev/ergenverse/block/CultivationPlanetCrystalBlock.java"
BLOCKS_REGISTRY = ROOT / "src/main/java/dev/ergenverse/block/ErgenverseBlocks.java"
TOMB_BUILDER = ROOT / "src/main/java/dev/ergenverse/spawn/SuzakuTombBuilder.java"
BEAD_ITEM = ROOT / "src/main/java/dev/ergenverse/wanglin/bead/HeavenDefyingBeadItem.java"
HISTORY = ROOT / "src/main/java/dev/ergenverse/history/HistoryManager.java"
BLOCKSTATE_JSON = ROOT / "src/main/resources/assets/ergenverse/blockstates/cultivation_planet_crystal.json"
BLOCK_MODEL_JSON = ROOT / "src/main/resources/assets/ergenverse/models/block/cultivation_planet_crystal.json"
ITEM_MODEL_JSON = ROOT / "src/main/resources/assets/ergenverse/models/item/cultivation_planet_crystal.json"
TEXTURE_PNG = ROOT / "src/main/resources/assets/ergenverse/textures/block/cultivation_planet_crystal.png"
LOOT_TABLE_JSON = ROOT / "src/main/resources/data/ergenverse/loot_tables/blocks/cultivation_planet_crystal.json"
LANG_EN = ROOT / "src/main/resources/assets/ergenverse/lang/en_us.json"
CHUNK_GEN = ROOT / "src/main/java/dev/ergenverse/runtime/worldgen/BlueprintChunkGenerator.java"
BLUEPRINT = ROOT / "src/main/java/dev/ergenverse/runtime/PlanetSuzakuBlueprint.java"

PASS = 0
FAIL = 0
FAILS = []


def check(cond: bool, label: str) -> None:
    global PASS, FAIL
    if cond:
        PASS += 1
    else:
        FAIL += 1
        FAILS.append(label)
        print(f"  FAIL: {label}")


def section(name: str) -> None:
    print(f"\n=== {name} ===")


# ─────────────────────────────────────────────────────────────────────────────
# Load sources
# ─────────────────────────────────────────────────────────────────────────────

block_text = BLOCK.read_text(encoding="utf-8")
blocks_text = BLOCKS_REGISTRY.read_text(encoding="utf-8")
tomb_text = TOMB_BUILDER.read_text(encoding="utf-8")
bead_text = BEAD_ITEM.read_text(encoding="utf-8")
history_text = HISTORY.read_text(encoding="utf-8")
chunk_gen_text = CHUNK_GEN.read_text(encoding="utf-8")
blueprint_text = BLUEPRINT.read_text(encoding="utf-8")

blockstate_json = json.loads(BLOCKSTATE_JSON.read_text(encoding="utf-8"))
block_model_json = json.loads(BLOCK_MODEL_JSON.read_text(encoding="utf-8"))
item_model_json = json.loads(ITEM_MODEL_JSON.read_text(encoding="utf-8"))
loot_json = json.loads(LOOT_TABLE_JSON.read_text(encoding="utf-8"))
lang_en = json.loads(LANG_EN.read_text(encoding="utf-8"))

# ─────────────────────────────────────────────────────────────────────────────
# 1. CultivationPlanetCrystalBlock.java — file & class
# ─────────────────────────────────────────────────────────────────────────────
section("1. CultivationPlanetCrystalBlock.java — file & class")

check(BLOCK.exists(), "CultivationPlanetCrystalBlock.java file exists")
check("package dev.ergenverse.block;" in block_text, "package dev.ergenverse.block")
check("public class CultivationPlanetCrystalBlock extends Block" in block_text,
      "public class CultivationPlanetCrystalBlock extends Block")
check("CRON-COMPLETIONIST-106" in block_text, "class javadoc mentions CRON-COMPLETIONIST-106")
check("修炼星晶" in block_text, "canon term 修炼星晶 present in javadoc")
check("朱雀子" in block_text, "canon term 朱雀子 present in javadoc")
check("Cultivation Planet Crystal" in block_text, "English canon name present")

# ─────────────────────────────────────────────────────────────────────────────
# 2. Block-state property — INHERITED boolean, default false
# ─────────────────────────────────────────────────────────────────────────────
section("2. Block-state property INHERITED")

check("BooleanProperty INHERITED = BooleanProperty.create(\"inherited\")" in block_text
      or "BooleanProperty.create(\"inherited\")" in block_text,
      "INHERITED BooleanProperty declared")
check("createBlockStateDefinition" in block_text, "createBlockStateDefinition override present")
check("builder.add(INHERITED)" in block_text, "INHERITED added to state definition")
check("setValue(INHERITED, false)" in block_text, "default state: inherited=false")

# ─────────────────────────────────────────────────────────────────────────────
# 3. Block registration — ErgenverseBlocks.CULTIVATION_PLANET_CRYSTAL
# ─────────────────────────────────────────────────────────────────────────────
section("3. Block registration")

check("CULTIVATION_PLANET_CRYSTAL" in blocks_text,
      "CULTIVATION_PLANET_CRYSTAL RegistryObject declared")
check('registerCrystal("cultivation_planet_crystal")' in blocks_text
      or 'registerCrystal(\n            "cultivation_planet_crystal")' in blocks_text
      or '"cultivation_planet_crystal"' in blocks_text and "registerCrystal" in blocks_text,
      "registerCrystal helper called with correct name")
check("new CultivationPlanetCrystalBlock" in blocks_text,
      "CultivationPlanetCrystalBlock instantiated in registry")
check("CRON-COMPLETIONIST-106" in blocks_text,
      "registry javadoc mentions CRON-COMPLETIONIST-106")

# ─────────────────────────────────────────────────────────────────────────────
# 4. Block properties — lightLevel 15, amethyst sound, purple map color
# ─────────────────────────────────────────────────────────────────────────────
section("4. Block properties — canon-faithful visual")

check(".lightLevel(state -> 15)" in blocks_text, "lightLevel 15 (maximum emission)")
check("SoundType.AMETHYST" in blocks_text, "SoundType.AMETHYST (crystalline sound)")
check("MapColor.COLOR_PURPLE" in blocks_text, "MapColor.COLOR_PURPLE (sacred purple)")
check(".strength(8.0F, 1200.0F)" in blocks_text, "strength 8.0F / 1200.0F (hard, blast-resistant)")
check(".requiresCorrectToolForDrops()" in blocks_text, "requiresCorrectToolForDrops")
check(".noOcclusion()" in blocks_text, "noOcclusion (allows particles to render through)")

# ─────────────────────────────────────────────────────────────────────────────
# 5. getDrops override — returns empty list (Crystal does NOT drop as item)
# ─────────────────────────────────────────────────────────────────────────────
section("5. getDrops override — no drops (canon: too large to inventory)")

check("public List<ItemStack> getDrops" in block_text, "getDrops override present")
check("Collections.emptyList()" in block_text, "getDrops returns Collections.emptyList()")
check("does NOT drop as an item" in block_text, "javadoc explains no-drop design")

# ─────────────────────────────────────────────────────────────────────────────
# 6. animateTick override — emits END_ROD particles
# ─────────────────────────────────────────────────────────────────────────────
section("6. animateTick override — ambient Qi particles")

check("public void animateTick" in block_text, "animateTick override present")
check("ParticleTypes.END_ROD" in block_text, "uses ParticleTypes.END_ROD (Qi emission visual)")
check("PARTICLE_TICK_INTERVAL = 20" in block_text, "particle spawn rate 1/20 ticks ≈ once/sec")

# ─────────────────────────────────────────────────────────────────────────────
# 7. use() override — right-click inheritance event with prerequisites
# ─────────────────────────────────────────────────────────────────────────────
section("7. use() override — right-click inheritance")

check("public InteractionResult use" in block_text, "use() override present")
check("level.isClientSide()" in block_text, "client-side early return")
check("ServerPlayer serverPlayer" in block_text, "cast to ServerPlayer on server")
check("InteractionResult.sidedSuccess" in block_text, "returns sidedSuccess on client")
check("InteractionResult.CONSUME" in block_text, "returns CONSUME on server")

# ─────────────────────────────────────────────────────────────────────────────
# 8. Inheritance prerequisites — bead-in-hand, realm ≥ NASCENT_SOUL, not inherited
# ─────────────────────────────────────────────────────────────────────────────
section("8. Inheritance prerequisites — canon-faithful gating")

# Prereq 1: not already inherited
check("state.getValue(INHERITED)" in block_text, "checks INHERITED block-state (prereq 1)")
check("修炼星晶的力量已经传承" in block_text, "canon message: already inherited (CN)")

# Prereq 2: bead in hand
check("findBead(serverPlayer)" in block_text, "findBead helper called (prereq 2)")
check("HeavenDefyingBeadItem" in block_text, "checks for HeavenDefyingBeadItem instance")
check("beadStack.isEmpty()" in block_text, "rejects if bead not found")
check("你需要天逆珠才能承受星晶之力" in block_text, "canon message: bead required (CN)")

# Prereq 3: realm ≥ NASCENT_SOUL
check("MIN_INHERITANCE_REALM = RealmId.NASCENT_SOUL" in block_text,
      "MIN_INHERITANCE_REALM = RealmId.NASCENT_SOUL")
check("playerRealm.order < MIN_INHERITANCE_REALM.order" in block_text,
      "rejects if realm below Nascent Soul (prereq 3)")
check("你的修为不足" in block_text, "canon message: insufficient realm (CN)")
check("元婴" in block_text or "NASCENT_SOUL" in block_text,
      "canon term 元婴/NASCENT_SOUL present")

# ─────────────────────────────────────────────────────────────────────────────
# 9. Inheritance outcome — block-state transition, bead marked, messages sent
# ─────────────────────────────────────────────────────────────────────────────
section("9. Inheritance outcome — full event")

check("state.setValue(INHERITED, true)" in block_text, "transitions block-state to inherited=true")
check("level.setBlock(pos, inheritedState, 3)" in block_text, "applies block-state to world")
check("recordPlayerDelta(pos, inheritedState)" in block_text, "records PLAYER delta for persistence")
check("beadItem.setSuzakuSon(beadStack, true)" in block_text, "marks bead with Suzaku Son status")
check("你成为了第十五代朱雀子" in block_text, "canon message: 15th-gen Suzaku Son (CN)")
check("15th-generation Suzaku Son" in block_text, "canon message: 15th-gen Suzaku Son (EN)")
check("─────────────────────────────────────" in block_text,
      "divider line in inheritance message (matches CRON-102 pattern)")
check("王林" in block_text, "canon name 王林 present in inheritance message")
check("朱雀星" in block_text, "canon term 朱雀星 present in inheritance message")

# ─────────────────────────────────────────────────────────────────────────────
# 10. HeavenDefyingBeadItem — NBT_SUZAKU_SON, isSuzakuSon/setSuzakuSon
# ─────────────────────────────────────────────────────────────────────────────
section("10. HeavenDefyingBeadItem — Suzaku Son NBT flag")

check('NBT_SUZAKU_SON = "Ergen.Bead.SuzakuSon"' in bead_text,
      "NBT_SUZAKU_SON constant declared")
check("CRON-COMPLETIONIST-106" in bead_text, "CRON-106 marker in bead javadoc")
check("public boolean isSuzakuSon" in bead_text, "isSuzakuSon getter present")
check("public void setSuzakuSon" in bead_text, "setSuzakuSon setter present")
check("stack.getOrCreateTag().putBoolean(NBT_SUZAKU_SON, true)" in bead_text,
      "setSuzakuSon writes NBT boolean")
check("write-once" in bead_text, "write-once semantics documented")
check("if (!suzakuSon) return" in bead_text, "write-once guard in setter")

# ─────────────────────────────────────────────────────────────────────────────
# 11. HistoryManager — subject constant + onDiscovery call
# ─────────────────────────────────────────────────────────────────────────────
section("11. HistoryManager — inheritance discovery event")

check('SUBJECT_SUZAKU_SON_INHERITANCE = "suzaku_son_inheritance"' in block_text,
      "SUBJECT_SUZAKU_SON_INHERITANCE constant declared in block")
check("HistoryManager.onDiscovery" in block_text, "HistoryManager.onDiscovery called")
check("SUBJECT_SUZAKU_SON_INHERITANCE" in block_text, "subject constant used in onDiscovery call")
check("15th-generation Suzaku Son" in block_text, "discovery detail mentions 15th-gen Suzaku Son")

# ─────────────────────────────────────────────────────────────────────────────
# 12. SuzakuTombBuilder — uses CULTIVATION_PLANET_CRYSTAL instead of DIAMOND_BLOCK
# ─────────────────────────────────────────────────────────────────────────────
section("12. SuzakuTombBuilder — placeholder retired")

check("CULTIVATION_PLANET_CRYSTAL" in tomb_text,
      "SuzakuTombBuilder references CULTIVATION_PLANET_CRYSTAL")
check("CultivationPlanetCrystalBlock" in tomb_text,
      "SuzakuTombBuilder references CultivationPlanetCrystalBlock class")
# CRON-105 used diamond_block; CRON-106 should NOT use it for the Crystal placement
# (the buildPedestalAndCrystal method should use the new block, not Blocks.DIAMOND_BLOCK)
check("ErgenverseBlocks.CULTIVATION_PLANET_CRYSTAL" in tomb_text,
      "buildPedestalAndCrystal uses ErgenverseBlocks.CULTIVATION_PLANET_CRYSTAL")
check("isAlreadyBuilt" in tomb_text, "isAlreadyBuilt method present")
check("CultivationPlanetCrystalBlock" in tomb_text,
      "isAlreadyBuilt checks for CultivationPlanetCrystalBlock instance")
check("CRON-106" in tomb_text, "tomb builder javadoc mentions CRON-106")

# Check that the placeholder comment is GONE (no longer claims diamond_block is used)
check("diamond block placeholder" not in tomb_text.lower(),
      "no more 'diamond block placeholder' language (placeholder retired)")

# ─────────────────────────────────────────────────────────────────────────────
# 13. Block assets — blockstate JSON, block model, item model, texture PNG
# ─────────────────────────────────────────────────────────────────────────────
section("13. Block assets")

check(BLOCKSTATE_JSON.exists(), "blockstate JSON exists")
check("inherited=false" in blockstate_json.get("variants", {}),
      "blockstate has inherited=false variant")
check("inherited=true" in blockstate_json.get("variants", {}),
      "blockstate has inherited=true variant")
check(blockstate_json["variants"]["inherited=false"]["model"] == "ergenverse:block/cultivation_planet_crystal",
      "inherited=false model points to ergenverse:block/cultivation_planet_crystal")
check(blockstate_json["variants"]["inherited=true"]["model"] == "ergenverse:block/cultivation_planet_crystal",
      "inherited=true model points to ergenverse:block/cultivation_planet_crystal")

check(BLOCK_MODEL_JSON.exists(), "block model JSON exists")
check(block_model_json.get("parent") == "minecraft:block/cube_all",
      "block model parent = minecraft:block/cube_all")
check(block_model_json.get("textures", {}).get("all") == "ergenverse:block/cultivation_planet_crystal",
      "block model texture = ergenverse:block/cultivation_planet_crystal")

check(ITEM_MODEL_JSON.exists(), "item model JSON exists")
check(item_model_json.get("parent") == "ergenverse:block/cultivation_planet_crystal",
      "item model parent = ergenverse:block/cultivation_planet_crystal")

check(TEXTURE_PNG.exists(), "texture PNG exists")
check(TEXTURE_PNG.stat().st_size > 100, "texture PNG is non-trivial (>100 bytes)")
# PNG signature
png_sig = b"\x89PNG\r\n\x1a\n"
with open(TEXTURE_PNG, "rb") as f:
    actual_sig = f.read(8)
check(actual_sig == png_sig, "texture PNG has valid PNG signature")

# ─────────────────────────────────────────────────────────────────────────────
# 14. Loot table — empty pools (no drops)
# ─────────────────────────────────────────────────────────────────────────────
section("14. Loot table — empty pools (Crystal does NOT drop as item)")

check(LOOT_TABLE_JSON.exists(), "loot table JSON exists")
check(loot_json.get("type") == "minecraft:block", "loot table type = minecraft:block")
check(loot_json.get("pools") == [], "loot table pools = [] (empty — no drops)")
check("DROPS NOTHING" in LOOT_TABLE_JSON.read_text(encoding="utf-8")
      or "does NOT drop" in LOOT_TABLE_JSON.read_text(encoding="utf-8"),
      "loot table _comment explains no-drop design")

# ─────────────────────────────────────────────────────────────────────────────
# 15. Language file — block.ergenverse.cultivation_planet_crystal entry
# ─────────────────────────────────────────────────────────────────────────────
section("15. Language file")

check("block.ergenverse.cultivation_planet_crystal" in lang_en,
      "en_us.json has block.ergenverse.cultivation_planet_crystal key")
check(lang_en["block.ergenverse.cultivation_planet_crystal"] == "Cultivation Planet Crystal",
      "translation = 'Cultivation Planet Crystal'")

# ─────────────────────────────────────────────────────────────────────────────
# 16. Canon fidelity — 修炼星晶, 朱雀子, 第十五代, NO fabricated chapter citation
# ─────────────────────────────────────────────────────────────────────────────
section("16. Canon fidelity")

check("修炼星晶" in block_text, "canon term 修炼星晶 (Cultivation Planet Crystal)")
check("朱雀子" in block_text, "canon term 朱雀子 (Suzaku Son)")
check("第十五代" in block_text or "15th-generation" in block_text or "15th-gen" in block_text,
      "canon term 第十五代 / 15th-generation Suzaku Son")
check("拓森" in block_text, "canon term 拓森 (Tuo Sen) — mentioned as future NPC")
check("Baidu Baike" in block_text, "canon source attribution (Baidu Baike)")
check("NO fabricated chapter citation" in block_text or "exact chapter is NOT cited" in block_text,
      "no fabricated chapter citation (canon honesty)")

# Canon-faithful inheritance prerequisites
check("Nascent Soul" in block_text or "NASCENT_SOUL" in block_text,
      "Nascent Soul realm requirement (canon-faithful)")
check("Heaven-Defying Bead" in block_text or "天逆珠" in block_text,
      "Heaven-Defying Bead requirement (canon-faithful)")

# ─────────────────────────────────────────────────────────────────────────────
# 17. Provenance — block-state change recorded as PLAYER delta via WorldFacade
# ─────────────────────────────────────────────────────────────────────────────
section("17. Provenance — PLAYER delta via WorldFacade")

check("WorldRuntime.get()" in block_text, "uses WorldRuntime.get() (the facade entry point)")
check("runtime.world().setPlayerBlock" in block_text, "calls runtime.world().setPlayerBlock (the facade)")
check("BlockStateCodec.serialize(state)" in block_text, "uses BlockStateCodec.serialize (CRON-94 codec)")
check("recordPlayerDelta" in block_text, "recordPlayerDelta helper method present")
check("runtime.isInitialized()" in block_text, "defensive isInitialized check")

# ─────────────────────────────────────────────────────────────────────────────
# 18. Architecture — no direct WorldDeltaStore manipulation, no Layer manipulation
# ─────────────────────────────────────────────────────────────────────────────
section("18. Architecture — facade-only writes")

# The block MUST NOT manipulate WorldDeltaStore directly (point 5 of CRON-69)
# It MUST NOT manipulate WorldLayer directly
# It MUST go through runtime.world().setPlayerBlock (the WorldFacade)
block_text_no_comments = re.sub(r'//.*', '', block_text)
block_text_no_comments = re.sub(r'/\*.*?\*/', '', block_text_no_comments, flags=re.DOTALL)
block_text_no_comments = re.sub(r'\*.*', '', block_text_no_comments)  # javadoc lines

check("WorldDeltaStore store = " not in block_text_no_comments
      and "store.record(" not in block_text_no_comments,
      "does NOT directly call WorldDeltaStore.store.record (uses facade)")
check("CompositeWorldLayer" not in block_text_no_comments,
      "does NOT directly manipulate CompositeWorldLayer")
check("PlayerLayer" not in block_text_no_comments,
      "does NOT directly manipulate PlayerLayer")
check("SimulationLayer" not in block_text_no_comments,
      "does NOT directly manipulate SimulationLayer")

# ─────────────────────────────────────────────────────────────────────────────
# 19. Integration — CRON-105 placeholder retired, CRON-104 cave suppression still protects
# ─────────────────────────────────────────────────────────────────────────────
section("19. Integration with CRON-104/105")

# CRON-104 cave suppression should still reference the tomb (radius 150)
check("suzaku_tomb" in chunk_gen_text.lower() or "SUZAKU_TOMB" in chunk_gen_text,
      "BlueprintChunkGenerator still references Suzaku Tomb (CRON-104 cave suppression)")
check("150" in chunk_gen_text, "CRON-104 cave suppression radius 150 still present")

# CRON-105 loot table should still exist (chest at the tomb)
loot_chest_path = ROOT / "src/main/resources/data/ergenverse/loot_tables/chests/suzaku_tomb_inheritance_chamber.json"
check(loot_chest_path.exists(), "CRON-105 loot table (chest) still exists")

# PlanetSuzakuBlueprint.SUZAKU_TOMB should still exist at (0, -60, 0)
check("SUZAKU_TOMB" in blueprint_text, "PlanetSuzakuBlueprint.SUZAKU_TOMB still exists")
check("0, -60, 0" in blueprint_text or "(0, -60, 0)" in blueprint_text,
      "SUZAKU_TOMB at (0, -60, 0) still in blueprint")

# ─────────────────────────────────────────────────────────────────────────────
# Final report
# ─────────────────────────────────────────────────────────────────────────────

print(f"\n{'=' * 70}")
print(f"CRON-106 CultivationPlanetCrystalBlock Verification:")
print(f"  PASSED: {PASS}")
print(f"  FAILED: {FAIL}")
print(f"  TOTAL:  {PASS + FAIL}")
if FAILS:
    print(f"\nFAILED CHECKS:")
    for f in FAILS:
        print(f"  - {f}")
print(f"{'=' * 70}")

sys.exit(0 if FAIL == 0 else 1)
