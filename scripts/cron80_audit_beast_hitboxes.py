#!/usr/bin/env python3
"""CRON-80: Per-entity hitbox audit for SpiritBeastEntity. (DEPRECATED — see below)

DEPRECATED in CRON-84: This script audited the OLD dual-source architecture
where three sources of truth existed for each beast's hitbox:
  (A) EntityType.Builder.sized(w, h) in EREntityTypes.java
  (B) SpiritBeastEntity.reassessDimensions() — runtime override (WINS at runtime)
  (C) Model comment "Hitbox: ~W wide, ~H tall" in EREntityTypes (design intent)

CRON-84 eliminated the dual-source architecture: BeastType enum constants now
carry (width, height, eyeHeight), and both EREntityTypes.sized() and
SpiritBeastEntity.getDimensions()/getEyeHeight() read from those enum fields.
There is ONE source of truth — the BeastType enum.

This script's regexes no longer match the new architecture (sized() now
references BeastType.XXX.width instead of literals; reassessDimensions() is
gone; Hitbox comments are removed). It shows "—" for all sources, which is
technically correct (no mismatches because no data found) but misleading.

USE INSTEAD: /home/z/my-project/scripts/cron84_verify_hitbox_single_source.py
That script verifies the new single-source architecture: enum constants carry
the values, both EREntityTypes and SpiritBeastEntity reference them, no
caching fields, no reassessDimensions(), and byRegistryName() exists for the
spawn-egg hitbox fix.

This script is preserved for historical reference — it documents the
3-source problem that CRON-80 manually reconciled and CRON-84 architecturally
eliminated.
"""
import re
from pathlib import Path

ROOT = Path("/home/z/my-project/forge-mod/src/main/java/dev/ergenverse")
et_text = (ROOT / "entity/EREntityTypes.java").read_text()
sb_text = (ROOT / "entity/SpiritBeastEntity.java").read_text()

# (A) EntityType.sized(w, h)
block_pat = re.compile(
    r'RegistryObject<EntityType<SpiritBeastEntity>>\s+(\w+)\s*=\s*'
    r'ENTITY_TYPES\.register\([^;]*?\.sized\(([\d.]+)F,\s*([\d.]+)F\)',
    re.DOTALL
)
sources_A = {}
for m in block_pat.finditer(et_text):
    sources_A[m.group(1)] = (float(m.group(2)), float(m.group(3)))

# (B) reassessDimensions case -> {beastWidth=..; beastHeight=..; beastEyeHeight=..;}
case_pat = re.compile(
    r'case (\w+)\s*->\s*\{\s*beastWidth\s*=\s*([\d.]+)F;\s*beastHeight\s*=\s*([\d.]+)F;\s*beastEyeHeight\s*=\s*([\d.]+)F;\s*\}'
)
sources_B = {}
for m in case_pat.finditer(sb_text):
    sources_B[m.group(1)] = (float(m.group(2)), float(m.group(3)), float(m.group(4)))

# (C) Comment "Hitbox: ~W wide, ~H tall" in EREntityTypes (looking back from each register call)
sources_C = {}
for m in block_pat.finditer(et_text):
    name = m.group(1)
    snippet = et_text[max(0, m.start()-700):m.start()]
    cm = list(re.finditer(r'Hitbox:\s*~?([\d.]+)\s*wide,?\s*~?([\d.]+)\s*tall', snippet, re.IGNORECASE))
    if cm:
        sources_C[name] = (float(cm[-1].group(1)), float(cm[-1].group(2)))

# Map enum short-name -> registry name for cross-reference.
# E.g., case RABBIT corresponds to registry SPIRIT_RABBIT.
enum_to_registry = {
    "RABBIT": "SPIRIT_RABBIT",
    "WOLF": "SPIRIT_WOLF",
    "DEER": "SPIRIT_DEER",
    "HAWK": "SPIRIT_HAWK",
    "FIRE_BEAST": "FIRE_BEAST",
    "STONE_BACK_BOAR": "STONE_BACK_BOAR",
    "CRANE": "SPIRIT_CRANE",
    "BAT": "SPIRIT_BAT",
    "QILIN": "QILIN",
    "SEA_SERPENT": "SEA_SERPENT",
    "SOUL_FISH": "SOUL_FISH",
    "TIGER": "SPIRIT_TIGER",
}

print(f"{'Beast (reg)':<20} {'(A) ET.sized':<14} {'(B) Runtime':<22} {'(C) Comment':<14} {'Mismatch':<10}")
print("=" * 86)
for enum_name, reg_name in sorted(enum_to_registry.items()):
    a = sources_A.get(reg_name)
    b = sources_B.get(enum_name)
    c = sources_C.get(reg_name)
    a_str = f"{a[0]:.2f}x{a[1]:.2f}" if a else "—"
    b_str = f"{b[0]:.2f}x{b[1]:.2f} (e{b[2]:.2f})" if b else "—"
    c_str = f"{c[0]:.2f}x{c[1]:.2f}" if c else "—"
    flags = []
    if a and b and (abs(a[0]-b[0]) > 0.01 or abs(a[1]-b[1]) > 0.01):
        flags.append("A≠B")
    if b and c and (abs(b[0]-c[0]) > 0.01 or abs(b[1]-c[1]) > 0.01):
        flags.append("B≠C")
    if a and c and (abs(a[0]-c[0]) > 0.01 or abs(a[1]-c[1]) > 0.01):
        flags.append("A≠C")
    flag_str = " ".join(flags) if flags else "OK"
    print(f"{reg_name:<20} {a_str:<14} {b_str:<22} {c_str:<14} {flag_str:<10}")

print()
print("=== CRITICAL BUGS ===")
# A≠B is critical — runtime hitbox doesn't match declared hitbox
for enum_name, reg_name in enum_to_registry.items():
    a = sources_A.get(reg_name)
    b = sources_B.get(enum_name)
    if a and b and (abs(a[0]-b[0]) > 0.01 or abs(a[1]-b[1]) > 0.01):
        print(f"  {reg_name}: ET.sized={a}, runtime={b[:2]}  ← CRITICAL: A≠B")
