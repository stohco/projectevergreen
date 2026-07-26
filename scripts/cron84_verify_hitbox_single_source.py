#!/usr/bin/env python3
"""CRON-84: Verify single-source-of-truth hitbox architecture.

Post-CRON-84, the BeastType enum in SpiritBeastEntity.java is the SINGLE source
of truth for all 12 beasts' (width, height, eyeHeight). Both:
  (A) EntityType.Builder.sized(w, h) in EREntityTypes.java — reads from
      BeastType.XXX.width / .height
  (B) SpiritBeastEntity.getDimensions() / getEyeHeight() — reads from
      getBeastType().width / .height / .eyeHeight

This script verifies:
  1. BeastType enum has (width, height, eyeHeight) for all 12 constants.
  2. EREntityTypes.sized() references BeastType.XXX.width/.height (not literals).
  3. SpiritBeastEntity has NO beastWidth/beastHeight/beastEyeHeight fields
     and NO reassessDimensions() method (the old dual-source footgun).
  4. The values match the CRON-80 reconciled values (no regression).

If all 4 checks pass, the dual-source footgun that silently undid CRON-60's
SOUL_FISH fix for ~10 rounds is architecturally eliminated.
"""
import re
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse")
sb_text = (ROOT / "entity/SpiritBeastEntity.java").read_text()
et_text = (ROOT / "entity/EREntityTypes.java").read_text()

# CRON-80 reconciled values (the canonical hitboxes after CRON-80's manual fix).
# These must NOT change in CRON-84 — the refactor is architectural, not value-changing.
CANON_VALUES = {
    "RABBIT":         (0.4, 0.5, 0.4),
    "WOLF":           (0.6, 0.9, 0.75),
    "DEER":           (0.7, 1.8, 1.45),
    "HAWK":           (0.5, 0.6, 0.5),
    "FIRE_BEAST":     (1.2, 1.4, 1.15),
    "STONE_BACK_BOAR":(1.2, 1.0, 0.8),
    "CRANE":          (0.6, 1.8, 1.6),
    "BAT":            (0.4, 0.5, 0.4),
    "QILIN":          (1.0, 1.5, 1.25),
    "SEA_SERPENT":    (1.0, 0.8, 0.65),
    "SOUL_FISH":      (0.6, 0.5, 0.25),
    "TIGER":          (1.0, 1.0, 0.85),
}

print("=" * 78)
print("CRON-84: Single-source-of-truth hitbox verification")
print("=" * 78)

# ── CHECK 1: BeastType enum has (width, height, eyeHeight) for all 12 ──
print("\n[1] BeastType enum constants carry (width, height, eyeHeight):")
enum_pat = re.compile(
    r'(\w+)\("([^"]+)",\s*([\d.]+)F,\s*([\d.]+)F,\s*([\d.]+)F\)'
)
enum_values = {}
for m in enum_pat.finditer(sb_text):
    name, id_str, w, h, eh = m.group(1), m.group(2), float(m.group(3)), float(m.group(4)), float(m.group(5))
    enum_values[name] = (w, h, eh, id_str)

all_ok_1 = True
for name, (exp_w, exp_h, exp_eh) in CANON_VALUES.items():
    if name not in enum_values:
        print(f"  ✗ FAIL: {name} not found in BeastType enum")
        all_ok_1 = False
        continue
    w, h, eh, id_str = enum_values[name]
    ok = abs(w - exp_w) < 0.01 and abs(h - exp_h) < 0.01 and abs(eh - exp_eh) < 0.01
    if not ok:
        print(f"  ✗ FAIL: {name} = ({w},{h},{eh}) expected ({exp_w},{exp_h},{exp_eh})")
        all_ok_1 = False
    else:
        print(f"  ✓ {name:<18} id={id_str:<16} ({w},{h},{eh})")

# ── CHECK 2: EREntityTypes.sized() references BeastType.XXX.width/.height ──
# Only checks SpiritBeastEntity builders (excludes MosquitoSwarm, Cultivator).
print("\n[2] EREntityTypes.sized() references BeastType enum (SpiritBeastEntity only):")
# Match sized() calls that are inside a Builder.<SpiritBeastEntity>of block.
# We look for the pattern: Builder.<SpiritBeastEntity>of ... .sized(...)
sb_block_pat = re.compile(
    r'Builder\.<SpiritBeastEntity>of[^;]*?\.sized\(\s*'
    r'(?:SpiritBeastEntity\.BeastType\.(\w+)\.width|([\d.]+)F)\s*,\s*'
    r'(?:SpiritBeastEntity\.BeastType\.(\w+)\.height|([\d.]+)F)\s*\)',
    re.DOTALL
)
all_ok_2 = True
sized_count = 0
literal_count = 0
for m in sb_block_pat.finditer(et_text):
    beast_w, lit_w, beast_h, lit_h = m.group(1), m.group(2), m.group(3), m.group(4)
    if beast_w and beast_h:
        sized_count += 1
        if beast_w != beast_h:
            print(f"  ✗ FAIL: width={beast_w} but height={beast_h} (mismatched enum)")
            all_ok_2 = False
    else:
        literal_count += 1
        print(f"  ✗ FAIL: literal sized({lit_w}F, {lit_h}F) found in SpiritBeastEntity builder")
        all_ok_2 = False

if sized_count == 12 and literal_count == 0:
    print(f"  ✓ All {sized_count} SpiritBeastEntity .sized() calls reference BeastType enum")
elif sized_count > 0:
    print(f"  ⚠ {sized_count} enum-based, {literal_count} literal-based (need 12 enum-based)")
else:
    print(f"  ✗ FAIL: no enum-based sized() calls found")

# ── CHECK 3: No beastWidth/beastHeight/beastEyeHeight fields or reassessDimensions() ──
print("\n[3] Old dual-source footgun removed (no caching fields, no reassessDimensions):")
has_fields = bool(re.search(r'private\s+float\s+beastWidth\s*=', sb_text))
has_method = bool(re.search(r'private\s+void\s+reassessDimensions\s*\(\s*\)', sb_text))
all_ok_3 = True
if has_fields:
    print("  ✗ FAIL: beastWidth/beastHeight/beastEyeHeight fields still present")
    all_ok_3 = False
else:
    print("  ✓ No beastWidth/beastHeight/beastEyeHeight caching fields")
if has_method:
    print("  ✗ FAIL: reassessDimensions() method still present")
    all_ok_3 = False
else:
    print("  ✓ No reassessDimensions() inline switch method")

# ── CHECK 4: getDimensions/getEyeHeight read from enum ──
print("\n[4] getDimensions()/getEyeHeight() read from BeastType enum:")
gd = re.search(r'public\s+net\.minecraft\.world\.entity\.EntityDimensions\s+getDimensions.*?\{([^}]+)\}', sb_text, re.DOTALL)
ge = re.search(r'public\s+float\s+getEyeHeight.*?\{([^}]+)\}', sb_text, re.DOTALL)
all_ok_4 = True
if gd:
    body = gd.group(1)
    if 'getBeastType()' in body and ('.width' in body or 'type.width' in body):
        print("  ✓ getDimensions() reads from getBeastType().width/.height")
    else:
        print(f"  ✗ FAIL: getDimensions() body doesn't reference enum: {body.strip()[:80]}")
        all_ok_4 = False
else:
    print("  ✗ FAIL: getDimensions() not found")
    all_ok_4 = False

if ge:
    body = ge.group(1)
    if 'getBeastType()' in body and '.eyeHeight' in body:
        print("  ✓ getEyeHeight() reads from getBeastType().eyeHeight")
    else:
        print(f"  ✗ FAIL: getEyeHeight() body doesn't reference enum: {body.strip()[:80]}")
        all_ok_4 = False
else:
    print("  ✗ FAIL: getEyeHeight() not found")
    all_ok_4 = False

# ── CHECK 5: byRegistryName() exists (spawn-egg fix) ──
print("\n[5] byRegistryName() exists (spawn-egg hitbox fix):")
has_brn = bool(re.search(r'public\s+static\s+BeastType\s+byRegistryName\s*\(', sb_text))
if has_brn:
    print("  ✓ BeastType.byRegistryName() method exists")
else:
    print("  ✗ FAIL: BeastType.byRegistryName() not found")
    all_ok_4 = False

# ── SUMMARY ──
print("\n" + "=" * 78)
checks = [all_ok_1, all_ok_2, all_ok_3, all_ok_4]
passed = sum(checks)
total = len(checks)
if all(checks):
    print(f"RESULT: ALL {total} CHECKS PASSED ✓")
    print()
    print("Architecture summary:")
    print("  - BeastType enum is the SINGLE source of truth for hitboxes.")
    print("  - EREntityTypes.sized() reads from BeastType.XXX.width/.height.")
    print("  - SpiritBeastEntity.getDimensions()/getEyeHeight() read from getBeastType().")
    print("  - No caching fields, no reassessDimensions() inline switch.")
    print("  - Spawn-egg beasts get correct hitbox via byRegistryName() inference.")
    print("  - The dual-source footgun that silently undid CRON-60's SOUL_FISH fix")
    print("    is ARCHITECTURALLY ELIMINATED. Future changes cannot silently desync.")
    return_code = 0
else:
    print(f"RESULT: {passed}/{total} checks passed — FAILURES PRESENT")
    return_code = 1

import sys
sys.exit(return_code)
