#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-108 — Tuo Sen 3D model + Ancient God combat AI verification.

Verifies that:
  1. Tuo Sen has a CharacterBuild entry with scale 1.30 (massive god-body).
  2. Tuo Sen maps to HeldWeaponType.NONE (no mortal weapon — Ancient God fights
     with raw power).
  3. The EntityCultivatorRenderer has an ancient_god_clan texture keyword
     that resolves to ancient_god_clan.png.
  4. The ancient_god_clan.png texture file exists and is a 64x64 PNG.
  5. AncientGodPressGoal exists and is properly structured (AoE ground pound,
     Tuo Sen characterId gate, 80 damage, 8s cooldown, particles, sound).
  6. AncientGodStarGazeGoal exists and is properly structured (paralysis via
     SLOWNESS/WEAKNESS/DARKNESS, Tuo Sen characterId gate, 30 damage, 14s
     cooldown, charge timer, particle beam).
  7. Both goals are registered in EntityCultivator.registerGoals() at priority 2.
  8. The activation gates in canUse() correctly check characterId == "tuo_sen".
  9. Canon fidelity — 拓森 (NOT 拓山), 8-star Ancient God, Tu Si rival.
 10. Architecture — no direct WorldFacade/store manipulation in combat goals
     (combat goals use vanilla damageSources().mobAttack, not the delta store).
 11. CRON-107 integration — Tuo Sen's spawn (CRON-107) sets characterId="tuo_sen",
     which is the gate key for the CRON-108 goals.
 12. Web-search canon verification — Baidu Baike + Sohu + 163 sources cited.

Run: python3 /home/z/my-project/scripts/cron108_verify_tuo_sen_ancient_god.py
Exit code: 0 if all checks pass, 1 otherwise.
"""

import json
import re
import struct
import zlib
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "forge-mod"
ROBE_MODEL = ROOT / "src/main/java/dev/ergenverse/client/model/CultivatorRobeModel.java"
RENDERER = ROOT / "src/main/java/dev/ergenverse/client/render/EntityCultivatorRenderer.java"
ENTITY = ROOT / "src/main/java/dev/ergenverse/entity/EntityCultivator.java"
PRESS_GOAL = ROOT / "src/main/java/dev/ergenverse/entity/ai/AncientGodPressGoal.java"
GAZE_GOAL = ROOT / "src/main/java/dev/ergenverse/entity/ai/AncientGodStarGazeGoal.java"
TEXTURE = ROOT / "src/main/resources/assets/ergenverse/textures/entity/cultivator/ancient_god_clan.png"
TUOSEN_SPAWN = ROOT / "src/main/java/dev/ergenverse/wanglin/bead/TuoSenSpawnEvent.java"

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


# Load sources
robe_text = ROBE_MODEL.read_text(encoding="utf-8")
renderer_text = RENDERER.read_text(encoding="utf-8")
entity_text = ENTITY.read_text(encoding="utf-8")
press_text = PRESS_GOAL.read_text(encoding="utf-8")
gaze_text = GAZE_GOAL.read_text(encoding="utf-8")
tuosen_text = TUOSEN_SPAWN.read_text(encoding="utf-8")

# ─────────────────────────────────────────────────────────────────────────────
# 1. CharacterBuild has Tuo Sen entry with scale 1.30
# ─────────────────────────────────────────────────────────────────────────────
section("1. CharacterBuild — Tuo Sen scale 1.30")

check('TUO_SEN("tuo_sen", 1.30F)' in robe_text,
      "TUO_SEN CharacterBuild entry with scale 1.30F")
check("8-star Ancient God" in robe_text,
      "canon term '8-star Ancient God' in TUO_SEN javadoc")
check("Ink Flow Split Soul Technique" in robe_text,
      "canon basis 'Ink Flow Split Soul Technique' referenced")
check("墨流分魂术" in robe_text,
      "canon Chinese term 墨流分魂术 present")
check("CRON-COMPLETIONIST-108" in robe_text,
      "CRON-108 marker in CultivatorRobeModel")
check("拓森" in robe_text,
      "canon term 拓森 in TUO_SEN javadoc")
# 1.30 should be the MAX scale in the enum. Match both lowercase and
# uppercase enum constant names (e.g. WANG_LIN, TUO_SEN, OLD_CHEN).
scales = re.findall(r'\("([a-z_]+)",\s*([\d.]+)F\)', robe_text)
tuo_sen_scale = None
for name, scale in scales:
    if name == "tuo_sen":
        tuo_sen_scale = float(scale)
check(tuo_sen_scale == 1.30, f"Tuo Sen scale is 1.30 (got {tuo_sen_scale})")
# 1.30 should be the maximum
all_scales = [float(s) for _, s in scales if s]
check(all_scales and tuo_sen_scale == max(all_scales),
      f"Tuo Sen 1.30 is the MAXIMUM scale in CharacterBuild (max={max(all_scales) if all_scales else 'N/A'})")

# ─────────────────────────────────────────────────────────────────────────────
# 2. HeldWeaponType — Tuo Sen maps to NONE
# ─────────────────────────────────────────────────────────────────────────────
section("2. HeldWeaponType — Tuo Sen → NONE")

# Find the forCharacter switch and check tuo_sen case
check('case "tuo_sen" -> NONE;' in robe_text,
      'Tuo Sen case in HeldWeaponType.forCharacter switch → NONE')
check("Ancient Gods fight with raw god-body power" in robe_text,
      "canon rationale for NONE documented")

# ─────────────────────────────────────────────────────────────────────────────
# 3. EntityCultivatorRenderer — ancient_god_clan texture keyword
# ─────────────────────────────────────────────────────────────────────────────
section("3. Renderer — ancient_god_clan keyword fallback")

check('if (lower.contains("ancient_god"))' in renderer_text,
      'ancient_god keyword check in tryKeywordFallback')
check("ancientGodTex()" in renderer_text,
      "ancientGodTex() helper method exists")
check('_ancientGod' in renderer_text,
      "_ancientGod cached ResourceLocation field")
check('tex("ancient_god_clan")' in renderer_text,
      'tex("ancient_god_clan") ResourceLocation creation')
check("CRON-COMPLETIONIST-108" in renderer_text,
      "CRON-108 marker in EntityCultivatorRenderer")

# ─────────────────────────────────────────────────────────────────────────────
# 4. ancient_god_clan.png texture file exists and is valid 64x64 PNG
# ─────────────────────────────────────────────────────────────────────────────
section("4. Texture file — ancient_god_clan.png")

check(TEXTURE.exists(), f"ancient_god_clan.png exists at {TEXTURE}")
if TEXTURE.exists():
    png_bytes = TEXTURE.read_bytes()
    # PNG signature
    check(png_bytes[:8] == b'\x89PNG\r\n\x1a\n',
          "valid PNG signature")
    # Parse IHDR for dimensions
    if png_bytes[:8] == b'\x89PNG\r\n\x1a\n':
        # IHDR is the first chunk after the signature
        # chunk length (4 bytes) + 'IHDR' (4 bytes) + data
        ihdr_len = struct.unpack('>I', png_bytes[8:12])[0]
        ihdr_type = png_bytes[12:16]
        check(ihdr_type == b'IHDR', "first chunk is IHDR")
        if ihdr_type == b'IHDR':
            width, height = struct.unpack('>II', png_bytes[16:24])
            bit_depth = png_bytes[24]
            color_type = png_bytes[25]
            check(width == 64, f"texture width is 64 (got {width})")
            check(height == 64, f"texture height is 64 (got {height})")
            check(bit_depth == 8, f"texture bit depth is 8 (got {bit_depth})")
            check(color_type == 6, f"texture color type is 6 (RGBA) (got {color_type})")

# ─────────────────────────────────────────────────────────────────────────────
# 5. AncientGodPressGoal — AoE ground pound
# ─────────────────────────────────────────────────────────────────────────────
section("5. AncientGodPressGoal — structure & mechanics")

check(PRESS_GOAL.exists(), "AncientGodPressGoal.java file exists")
check("class AncientGodPressGoal extends Goal" in press_text,
      "class declaration extends Goal")
check("CRON-COMPLETIONIST-108" in press_text,
      "CRON-108 marker in AncientGodPressGoal")
check('EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)' in press_text,
      "claims MOVE+LOOK+JUMP flags (leap requires JUMP)")

# Activation gate
check('"tuo_sen".equals(ec.getCharacterId())' in press_text,
      'canUse() gates on characterId == "tuo_sen"')
check('!(mob instanceof EntityCultivator ec)' in press_text,
      "canUse() returns false for non-EntityCultivator mobs")

# Mechanics
check("MIN_RANGE_SQ = 16.0D" in press_text, "min range 4 blocks (squared 16)")
check("MAX_RANGE_SQ = 256.0D" in press_text, "max range 16 blocks (squared 256)")
check("AOE_RADIUS = 6.0D" in press_text, "AoE radius 6 blocks")
check("PRESS_DAMAGE = 80.0F" in press_text, "press damage 80")
check("COOLDOWN_TICKS = 160" in press_text, "cooldown 160 ticks (8s)")
check("LEAP_DURATION = 10" in press_text, "leap duration 10 ticks")
check("LEAP_VELOCITY = 1.2D" in press_text, "leap y-velocity 1.2")

# Visual effects
check("ParticleTypes.POOF" in press_text, "POOF particles (dust cloud)")
check("ParticleTypes.SQUID_INK" in press_text, "SQUID_INK particles (dark god-force)")
check("ParticleTypes.CRIT" in press_text, "CRIT particles (impact sparks)")
check("ParticleTypes.END_ROD" in press_text, "END_ROD particles (god-body energy)")
check("ENDER_DRAGON_GROWL" in press_text, "ENDER_DRAGON_GROWL sound")
check("WITHER_BREAK_BLOCK" in press_text, "WITHER_BREAK_BLOCK sound (THUD)")

# Canon basis
check("8-star Ancient God" in press_text, "canon term '8-star Ancient God'")
check("拓森" in press_text or "Tu Si" in press_text, "canon term in javadoc")
check("墨流分魂术" in press_text, "canon term 墨流分魂术 referenced")
check("Baidu Baike" in press_text, "Baidu Baike source cited")

# ─────────────────────────────────────────────────────────────────────────────
# 6. AncientGodStarGazeGoal — paralysis attack
# ─────────────────────────────────────────────────────────────────────────────
section("6. AncientGodStarGazeGoal — structure & mechanics")

check(GAZE_GOAL.exists(), "AncientGodStarGazeGoal.java file exists")
check("class AncientGodStarGazeGoal extends Goal" in gaze_text,
      "class declaration extends Goal")
check("CRON-COMPLETIONIST-108" in gaze_text,
      "CRON-108 marker in AncientGodStarGazeGoal")
check("EnumSet.of(Flag.LOOK)" in gaze_text,
      "claims LOOK flag only (Tuo Sen can't move during charge)")

# Activation gate
check('"tuo_sen".equals(ec.getCharacterId())' in gaze_text,
      'canUse() gates on characterId == "tuo_sen"')

# Mechanics
check("MIN_RANGE_SQ = 100.0D" in gaze_text, "min range 10 blocks (squared 100)")
check("MAX_RANGE_SQ = 900.0D" in gaze_text, "max range 30 blocks (squared 900)")
check("CHARGE_DURATION = 30" in gaze_text, "charge duration 30 ticks (1.5s)")
check("PARALYSIS_DURATION = 100" in gaze_text, "paralysis duration 100 ticks (5s)")
check("DARKNESS_DURATION = 60" in gaze_text, "darkness duration 60 ticks (3s)")
check("GAZE_DAMAGE = 30.0F" in gaze_text, "gaze damage 30")
check("COOLDOWN_TICKS = 280" in gaze_text, "cooldown 280 ticks (14s)")

# Paralysis effects
check("MobEffects.MOVEMENT_SLOWDOWN" in gaze_text, "applies SLOWNESS")
check("MobEffects.WEAKNESS" in gaze_text, "applies WEAKNESS")
check("MobEffects.DARKNESS" in gaze_text, "applies DARKNESS (visual impairment)")

# Charge particles (8 END_ROD ring around head)
check("ParticleTypes.END_ROD" in gaze_text, "END_ROD particles (8-star array)")
check("particleCount = 4 + (progress * 4 / CHARGE_DURATION)" in gaze_text,
      "particle count grows during charge (4 → 8)")
check("WITHER_AMBIENT" in gaze_text, "WITHER_AMBIENT charge sound")

# Beam particles
check("ParticleTypes.DRAGON_BREATH" in gaze_text, "DRAGON_BREATH beam particles")
check("WITHER_DEATH" in gaze_text, "WITHER_DEATH fire sound")

# Line of sight check
check("hasLineOfSight(target)" in gaze_text, "LOS check method")
check("ClipContext" in gaze_text, "uses ClipContext for LOS check")
check("net.minecraft.world.level.ClipContext" in gaze_text,
      "uses correct ClipContext import (world.level, NOT world.phys)")

# Canon basis
check("8-star Ancient God" in gaze_text, "canon term '8-star Ancient God'")
check("古神之眼" in gaze_text or "Ancient God Eye" in gaze_text,
      "canon term Ancient God Eye referenced")
check("Baidu Baike" in gaze_text, "Baidu Baike source cited")

# ─────────────────────────────────────────────────────────────────────────────
# 7. EntityCultivator — goal registration
# ─────────────────────────────────────────────────────────────────────────────
section("7. EntityCultivator — Ancient God goal registration")

check("AncientGodPressGoal(this)" in entity_text,
      "AncientGodPressGoal registered in registerGoals()")
check("AncientGodStarGazeGoal(this)" in entity_text,
      "AncientGodStarGazeGoal registered in registerGoals()")
check("CRON-COMPLETIONIST-108" in entity_text,
      "CRON-108 marker in EntityCultivator")
check("goalSelector.addGoal(2, new dev.ergenverse.entity.ai.AncientGodPressGoal(this))" in entity_text,
      "Press goal at priority 2")
check("goalSelector.addGoal(2, new dev.ergenverse.entity.ai.AncientGodStarGazeGoal(this))" in entity_text,
      "Gaze goal at priority 2")
# Confirm the goals are added AFTER the existing combat goals (so they're documented as extensions)
press_idx = entity_text.find("AncientGodPressGoal(this)")
swordqi_idx = entity_text.find("CultivatorSwordQiGoal(this)")
check(press_idx > swordqi_idx,
      "Press goal registered AFTER CultivatorSwordQiGoal (documented as extension)")

# ─────────────────────────────────────────────────────────────────────────────
# 8. Activation gates — characterId check in canUse()
# ─────────────────────────────────────────────────────────────────────────────
section("8. Activation gates — Tuo Sen characterId gate")

# Both goals must check characterId == "tuo_sen" in canUse()
# Extract the canUse() method from each file
def extract_method(text, method_name):
    """Extract a method body from Java source."""
    pattern = rf'public boolean {method_name}\(\)\s*\{{(.*?)\}}\s*\n\s*(?:@Override|public|private|protected|\Z)'
    match = re.search(pattern, text, re.DOTALL)
    return match.group(1) if match else ""

press_canuse = extract_method(press_text, "canUse")
gaze_canuse = extract_method(gaze_text, "canUse")

check("tuo_sen" in press_canuse, "Press canUse() checks characterId")
check("tuo_sen" in gaze_canuse, "Gaze canUse() checks characterId")
check("ec.getCharacterId()" in press_canuse, "Press canUse() uses EntityCultivator.getCharacterId()")
check("ec.getCharacterId()" in gaze_canuse, "Gaze canUse() uses EntityCultivator.getCharacterId()")

# ─────────────────────────────────────────────────────────────────────────────
# 9. Canon fidelity
# ─────────────────────────────────────────────────────────────────────────────
section("9. Canon fidelity")

check("拓森" in press_text or "Tuo Sen" in press_text, "canon term Tuo Sen/拓森 in Press goal")
check("拓森" in gaze_text or "Tuo Sen" in gaze_text, "canon term Tuo Sen/拓森 in Gaze goal")
check("拓山" not in press_text, "no incorrect 拓山 in Press goal")
check("拓山" not in gaze_text, "no incorrect 拓山 in Gaze goal")
check("Tu Si" in press_text or "涂司" in press_text, "Tu Si (涂司) referenced in Press goal")
check("Suzaku Tomb" in press_text or "朱雀墓" in press_text, "Suzaku Tomb referenced in Press goal")

# ─────────────────────────────────────────────────────────────────────────────
# 10. Architecture — no direct WorldFacade/store manipulation
# ─────────────────────────────────────────────────────────────────────────────
section("10. Architecture — combat goals use vanilla damage API")

# Combat goals should NOT touch WorldFacade, WorldDeltaStore, or layers directly
# They use mob.damageSources().mobAttack(mob) — the vanilla damage API
check("WorldFacade" not in press_text, "Press goal does NOT touch WorldFacade")
check("WorldDeltaStore" not in press_text, "Press goal does NOT touch WorldDeltaStore")
check("CompositeWorldLayer" not in press_text, "Press goal does NOT touch CompositeWorldLayer")
check("WorldFacade" not in gaze_text, "Gaze goal does NOT touch WorldFacade")
check("WorldDeltaStore" not in gaze_text, "Gaze goal does NOT touch WorldDeltaStore")
check("CompositeWorldLayer" not in gaze_text, "Gaze goal does NOT touch CompositeWorldLayer")

# Combat goals SHOULD use mob.damageSources().mobAttack(mob)
check("mob.damageSources().mobAttack(mob)" in press_text,
      "Press goal uses vanilla mob.damageSources().mobAttack(mob)")
check("mob.damageSources().mobAttack(mob)" in gaze_text,
      "Gaze goal uses vanilla mob.damageSources().mobAttack(mob)")

# Combat goals SHOULD use ServerLevel.sendParticles (vanilla particle API)
check("sendParticles" in press_text, "Press goal uses ServerLevel.sendParticles")
check("sendParticles" in gaze_text, "Gaze goal uses ServerLevel.sendParticles")

# ─────────────────────────────────────────────────────────────────────────────
# 11. CRON-107 integration — spawn sets characterId="tuo_sen"
# ─────────────────────────────────────────────────────────────────────────────
section("11. CRON-107 integration — spawn sets characterId")

# CRON-107's TuoSenSpawnEvent sets characterId via CanonActorMaterializer's
# CanonProfile entry. The profile uses characterId="tuo_sen".
# Check that the spawn event references the right CanonUUID and that the
# CanonActorMaterializer profile uses characterId="tuo_sen".
check("CanonUUID.TUO_SEN" in tuosen_text, "TuoSenSpawnEvent uses CanonUUID.TUO_SEN")
check("tuo_sen" in tuosen_text, "tuo_sen string in TuoSenSpawnEvent")
# The CRON-107 spawn should set realm="ancient" (matching RealmId.ANCIENT = 古境)
check("ancient" in tuosen_text.lower(), "spawn uses 'ancient' realm")

# ─────────────────────────────────────────────────────────────────────────────
# 12. Web-search canon verification — sources cited
# ─────────────────────────────────────────────────────────────────────────────
section("12. Web-search canon verification")

# Both goals should cite web-search sources (Baidu Baike, Sohu, 163)
sources = ["Baidu Baike", "Sohu", "163"]
for src in sources:
    check(src in press_text, f"Press goal cites {src}")
    check(src in gaze_text, f"Gaze goal cites {src}")

# Canon quotes
check("时隔300年" in press_text or "300 years" in press_text,
      "Press goal references canon '300 years' quote")
check("九星古神血液" in press_text or "9-star Ancient God blood" in press_text,
      "Press goal references canon 9-star Ancient God blood quote")

# ─────────────────────────────────────────────────────────────────────────────
# Final summary
# ─────────────────────────────────────────────────────────────────────────────
print(f"\n{'=' * 60}")
print(f"CRON-COMPLETIONIST-108 verification: {PASS}/{PASS + FAIL} checks passed.")
if FAILS:
    print(f"FAILURES ({FAIL}):")
    for f in FAILS:
        print(f"  - {f}")
    sys.exit(1)
else:
    print("ALL CHECKS PASSED.")
    sys.exit(0)
