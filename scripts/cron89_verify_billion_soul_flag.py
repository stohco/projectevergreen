#!/usr/bin/env python3
"""
CRON-89 Verification Script — Billion Soul Flag (十亿魂幡)

Verifies that the Billion Soul Flag implementation is complete and correct.
Checks:
1. BillionSoulFlagItem.java exists and has the required mechanics
2. BillionSoulFlagEventHandler.java exists and hooks LivingDeathEvent
3. Item is registered in ErgenverseItems.java
4. Item is in the ERGENVERSE_ITEMS_OWNED_NAMES skip-set in WangLinItems.java
5. Canon fidelity: name, provenance, tier progression
6. NBT keys and constants are consistent
7. Creative tab includes the item
8. Model JSON exists and references the correct texture
"""
import re
import sys
import os
import json

MOD_ROOT = "/home/z/my-project/forge-mod/src/main/java/dev/ergenverse"
RES_ROOT = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse"

errors = []
checks_passed = 0
checks_total = 0

def check(condition, msg):
    global checks_passed, checks_total
    checks_total += 1
    if condition:
        checks_passed += 1
        print(f"  PASS: {msg}")
    else:
        errors.append(msg)
        print(f"  FAIL: {msg}")

def read_file(path):
    with open(path, 'r', encoding='utf-8') as f:
        return f.read()

print("=" * 70)
print("CRON-89 Verification: Billion Soul Flag (十亿魂幡)")
print("=" * 70)

# ── CHECK 1: BillionSoulFlagItem.java exists and has required mechanics ──
print("\n[1] BillionSoulFlagItem.java — mechanics check")
item_path = f"{MOD_ROOT}/item/BillionSoulFlagItem.java"
check(os.path.exists(item_path), "BillionSoulFlagItem.java exists")

if os.path.exists(item_path):
    src = read_file(item_path)
    
    # Required mechanics
    check("class BillionSoulFlagItem extends Item" in src,
          "Class declaration: extends Item")
    check("MAX_ORDINARY_SOULS = 100_000" in src,
          "Ordinary soul cap: 100,000 (gameplay concession vs canon's 1 billion)")
    check("MAX_MAIN_SOULS = 37" in src,
          "Main soul cap: 37 (canon-exact — 37 main souls with consciousness)")
    check("STORM_RADIUS = 16" in src, "Soul Storm radius: 16 blocks")
    check("SENSE_RADIUS = 32" in src, "Soul Sense radius: 32 blocks")
    check("BASE_ABSORPTION_CHANCE = 0.50" in src,
          "Base absorption chance: 50% (balance — canon implies 100%)")
    
    # Methods
    check("public InteractionResultHolder<ItemStack> use(" in src,
          "use() method (right-click handler)")
    check("private InteractionResultHolder<ItemStack> soulStorm(" in src,
          "soulStorm() method (right-click: AoE damage)")
    check("private InteractionResultHolder<ItemStack> soulSense(" in src,
          "soulSense() method (shift+right-click: reveal entities)")
    check("public boolean absorbSoul(" in src,
          "absorbSoul() method (called by event handler)")
    
    # NBT keys
    check('NBT_ORDINARY_SOULS = "OrdinarySouls"' in src,
          "NBT key: OrdinarySouls")
    check('NBT_MAIN_SOULS = "MainSouls"' in src,
          "NBT key: MainSouls")
    check('NBT_TOTAL_POWER = "TotalPower"' in src,
          "NBT key: TotalPower")
    check('NBT_SOUL_LOG = "SoulLog"' in src,
          "NBT key: SoulLog (last 5 absorbed souls)")
    
    # Tier progression (canon names)
    check('Empty Soul Flag' in src, "Tier: Empty Soul Flag")
    check('Soul Flag' in src and '魂幡' in src, "Tier: Soul Flag (魂幡)")
    check('Hundred Soul Flag' in src and '百魂幡' in src, "Tier: Hundred Soul Flag (百魂幡)")
    check('Thousand Soul Flag' in src and '千魂幡' in src, "Tier: Thousand Soul Flag (千魂幡)")
    check('Myriad Soul Flag' in src and '万魂幡' in src, "Tier: Myriad Soul Flag (万魂幡)")
    check('Billion Soul Flag' in src and '十亿魂幡' in src, "Tier: Billion Soul Flag (十亿魂幡)")
    
    # Damage formula
    check("5.0F + (float) Math.sqrt(totalPower) * 0.5F" in src,
          "Damage formula: 5.0 + sqrt(power) * 0.5")
    
    # Durability + repair
    check("isValidRepairItem" in src, "Repairable (isValidRepairItem override)")
    check("BEAST_CORE" in src, "Repair material: beast_core (canon: repaired via Gate of Emptiness)")
    
    # Foil (shimmer) only when souls > 0
    check("isFoil" in src, "isFoil() override — shimmer only when souls present")
    
    # Event bus integration (SimulationActions)
    check("SimulationActions.spellCast" in src,
          "Wired to SimulationActions.spellCast (event bus integration)")
    
    # Canon documentation
    check("十亿魂幡" in src, "Canon name: 十亿魂幡")
    check("Soul Refining Sect" in src, "Canon: Soul Refining Sect guardian treasure")
    check("Dun Tian" in src, "Canon: Gifted by Dun Tian (Soul Refining Sect predecessor)")
    check("37 main souls" in src, "Canon: 37 main souls with consciousness")
    check("1 billion ordinary souls" in src, "Canon: 1 billion ordinary souls")
    check("Tuo Sen" in src, "Canon: self-destructed vs Tuo Sen")
    check("Gate of Emptiness" in src, "Canon: repaired via Gate of Emptiness")

# ── CHECK 2: BillionSoulFlagEventHandler.java ──
print("\n[2] BillionSoulFlagEventHandler.java — event handler check")
handler_path = f"{MOD_ROOT}/item/BillionSoulFlagEventHandler.java"
check(os.path.exists(handler_path), "BillionSoulFlagEventHandler.java exists")

if os.path.exists(handler_path):
    src = read_file(handler_path)
    
    check("@Mod.EventBusSubscriber" in src, "Has @Mod.EventBusSubscriber annotation")
    check("Bus.FORGE" in src, "Registered on FORGE event bus")
    check("@SubscribeEvent" in src, "Has @SubscribeEvent method")
    check("LivingDeathEvent" in src, "Hooks LivingDeathEvent")
    check("EventPriority.NORMAL" in src,
          "Priority: NORMAL (lower than PlayerCombatBridge's HIGHEST)")
    
    # Both hands check
    check("getMainHandItem" in src, "Checks main hand")
    check("getOffhandItem" in src, "Checks offhand")
    check("mainIsFlag" in src and "offIsFlag" in src,
          "Checks both hands for flag")
    
    # Guard clauses
    check("isClientSide" in src, "Client-side guard")
    check("ServerPlayer" in src, "Player-sourced kills only")
    check("victim instanceof Player" in src, "Skips player deaths (no soul from self)")
    
    # Canon documentation
    check("passive" in src.lower(), "Documents passive absorption mechanic")
    check("sentient" in src.lower() or "sentient" in src,
          "References flag's sentience (canon)")

# ── CHECK 3: Registration in ErgenverseItems.java ──
print("\n[3] ErgenverseItems.java — registration check")
ergen_items_path = f"{MOD_ROOT}/item/ErgenverseItems.java"
src = read_file(ergen_items_path)

check('BILLION_SOUL_FLAG' in src, "BILLION_SOUL_FLAG RegistryObject defined")
check('ITEMS.register("billion_soul_flag"' in src,
      'Registered as "billion_soul_flag"')
check('new dev.ergenverse.item.BillionSoulFlagItem(' in src,
      "Uses BillionSoulFlagItem class (not generic Item)")
check('.durability(2000)' in src, "Durability: 2000")
check('Rarity.EPIC' in src, "Rarity: EPIC")
check('output.accept(BILLION_SOUL_FLAG.get())' in src,
      "Added to creative tab")

# ── CHECK 4: Skip-set in WangLinItems.java ──
print("\n[4] WangLinItems.java — skip-set check")
wanglin_items_path = f"{MOD_ROOT}/wanglin/WangLinItems.java"
src = read_file(wanglin_items_path)

check('"billion_soul_flag"' in src and 'ERGENVERSE_ITEMS_OWNED_NAMES' in src,
      "billion_soul_flag in ERGENVERSE_ITEMS_OWNED_NAMES skip-set")
# Verify it appears in the Set.of(...) block, not elsewhere
skip_match = re.search(r'ERGENVERSE_ITEMS_OWNED_NAMES\s*=\s*java\.util\.Set\.of\((.*?)\);', src, re.DOTALL)
if skip_match:
    skip_content = skip_match.group(1)
    check('"billion_soul_flag"' in skip_content,
          "billion_soul_flag is inside the Set.of(...) literal")
else:
    errors.append("Could not find ERGENVERSE_ITEMS_OWNED_NAMES Set.of() block")
    print("  FAIL: Could not find ERGENVERSE_ITEMS_OWNED_NAMES Set.of() block")

# ── CHECK 5: Model JSON + texture ──
print("\n[5] Model JSON + texture check")
model_path = f"{RES_ROOT}/models/item/billion_soul_flag.json"
texture_path = f"{RES_ROOT}/textures/item/billion_soul_flag.png"

check(os.path.exists(model_path), "Model JSON exists")
check(os.path.exists(texture_path), "Texture PNG exists")

if os.path.exists(model_path):
    model = json.load(open(model_path))
    check(model.get("parent") == "minecraft:item/generated",
          "Model parent: minecraft:item/generated")
    textures = model.get("textures", {})
    check(textures.get("layer0") == "ergenverse:item/billion_soul_flag",
          "Texture reference: ergenverse:item/billion_soul_flag")

if os.path.exists(texture_path):
    size = os.path.getsize(texture_path)
    check(size > 100, f"Texture size > 100 bytes (got {size} bytes)")

# ── CHECK 6: Provenance data ──
print("\n[6] Provenance data check")
prov_path = "/home/z/my-project/forge-mod/src/main/resources/data/ergenverse/provenance/billion_soul_flag___ten_billion_soul_banner.json"
check(os.path.exists(prov_path), "Provenance JSON exists")

if os.path.exists(prov_path):
    prov = json.load(open(prov_path))
    check(prov.get("artifact_id") == "I51", "Artifact ID: I51")
    check(prov.get("nameCn") == "十亿魂幡", "Canon name: 十亿魂幡")
    check(prov.get("type") == "banner", "Type: banner")
    check("37 main souls" in str(prov.get("abilities", [])),
          "Canon: 37 main souls + 1 billion ordinary souls")
    check(prov.get("canon_confidence") == 5, "Canon confidence: 5 (highest)")

# ── CHECK 7: No duplicate registration ──
print("\n[7] No duplicate registration check")
# Verify WangLinItems does NOT register billion_soul_flag as WangLinItem
# (it should be skipped via ERGENVERSE_ITEMS_OWNED_NAMES)
wanglin_src = read_file(wanglin_items_path)
# The registerArsenalItem method should skip billion_soul_flag
skip_logic = "if (ERGENVERSE_ITEMS_OWNED_NAMES.contains(registryName))"
check(skip_logic in wanglin_src,
      "WangLinItems has skip-logic for ERGENVERSE_ITEMS_OWNED_NAMES")

# ── SUMMARY ──
print("\n" + "=" * 70)
print(f"RESULTS: {checks_passed}/{checks_total} checks passed")
if errors:
    print(f"FAILURES: {len(errors)}")
    for e in errors:
        print(f"  - {e}")
    sys.exit(1)
else:
    print("ALL CHECKS PASSED")
    sys.exit(0)
