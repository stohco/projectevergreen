#!/usr/bin/env python3
"""
CRON-132: Paint a metallic sword blade gradient into the ride_sword UV region
of every cultivator texture.

The ride_sword ModelPart (added in CRON-132) samples from the 64x64 cultivator
texture at UV region (32,56)-(54,60). Prior to this script, that region
contained arbitrary texture data (not transparent, not a sword). This script
paints a proper metallic blade gradient so the ride_sword renders as an
intentional silver sword, not random noise.

UV layout for the ride_sword boxes (Minecraft box UV mapping):
  - Blade (1x1x20 box, texOffs(32,56)):
      The box UV layout for a 1x1x20 box occupies a 22x4 pixel region:
        Front/Back faces (1x1): cols 32-33, rows 56-57
        Right/Left faces (20x1): cols 33-53, row 56 + cols 33-53, row 57
        Top/Bottom faces (20x1): cols 33-53, row 57 + cols 33-53, row 58
      Simplified: the blade faces are thin strips at rows 56-59, cols 32-54.
  - Guard (5x1x1 box, texOffs(32,60)):
      Occupies a 7x3 region at (32,60)-(39,63).
  - Pommel (1x1x2 box, texOffs(40,60)):
      Occupies a 4x3 region at (40,60)-(44,63).

Painting strategy:
  - Blade region (32,56)-(54,60): metallic gradient — dark edge (50,50,55),
    mid-blade (180,180,190), bright highlight (240,240,250). The gradient
    runs along the blade length (Z axis) to simulate a polished edge.
  - Guard region (32,60)-(39,63): golden guard — (180,140,40) base with a
    brighter (220,180,60) center band.
  - Pommel region (40,60)-(44,63): dark pommel — (60,45,20) bronze.

All colors are RGB; alpha is 255 (fully opaque). The script is idempotent —
running it twice produces the same result.
"""

import os
import sys
from pathlib import Path
from PIL import Image

# Ride sword UV regions in the 64x64 cultivator texture
BLADE_REGION = (32, 56, 54, 60)   # 22 wide x 4 tall
GUARD_REGION = (32, 60, 39, 63)   # 7 wide x 3 tall
POMMEL_REGION = (40, 60, 44, 63)  # 4 wide x 3 tall

# Metallic blade colors
BLADE_DARK = (50, 50, 55, 255)      # dark edge
BLADE_MID = (180, 180, 190, 255)    # mid-blade silver
BLADE_BRIGHT = (240, 240, 250, 255) # bright highlight

# Guard colors (golden)
GUARD_BASE = (180, 140, 40, 255)
GUARD_HIGHLIGHT = (220, 180, 60, 255)

# Pommel colors (dark bronze)
POMMEL_DARK = (60, 45, 20, 255)


def paint_blade(img):
    """Paint a metallic gradient into the blade UV region."""
    x0, y0, x1, y1 = BLADE_REGION
    width = x1 - x0
    height = y1 - y0
    for y in range(y0, y1):
        for x in range(x0, x1):
            # Gradient along the blade length (x axis in UV space)
            # Dark at edges, bright in the center (simulates a polished blade)
            t = (x - x0) / max(width - 1, 1)
            # Center-bright gradient: peak at t=0.5
            brightness = 1.0 - abs(2.0 * t - 1.0)  # 0 at edges, 1 at center
            if brightness > 0.8:
                color = BLADE_BRIGHT
            elif brightness > 0.3:
                color = BLADE_MID
            else:
                color = BLADE_DARK
            img.putpixel((x, y), color)


def paint_guard(img):
    """Paint a golden guard."""
    x0, y0, x1, y1 = GUARD_REGION
    for y in range(y0, y1):
        for x in range(x0, x1):
            # Center row is brighter (highlight)
            if y == y0 + 1:
                img.putpixel((x, y), GUARD_HIGHLIGHT)
            else:
                img.putpixel((x, y), GUARD_BASE)


def paint_pommel(img):
    """Paint a dark bronze pommel."""
    x0, y0, x1, y1 = POMMEL_REGION
    for y in range(y0, y1):
        for x in range(x0, x1):
            img.putpixel((x, y), POMMEL_DARK)


def process_texture(path):
    """Paint the ride_sword UV region in a single texture."""
    img = Image.open(path).convert("RGBA")
    if img.size != (64, 64):
        print(f"  SKIP (size {img.size} != 64x64): {path}")
        return False
    paint_blade(img)
    paint_guard(img)
    paint_pommel(img)
    img.save(path)
    return True


def main():
    tex_dir = Path("src/main/resources/assets/ergenverse/textures/entity/cultivator")
    if not tex_dir.exists():
        print(f"ERROR: texture directory not found: {tex_dir}")
        sys.exit(1)

    textures = sorted(tex_dir.glob("*.png"))
    print(f"Found {len(textures)} cultivator textures in {tex_dir}")

    painted = 0
    skipped = 0
    for tex in textures:
        if process_texture(tex):
            print(f"  PAINTED: {tex.name}")
            painted += 1
        else:
            skipped += 1

    print(f"\nDone: {painted} painted, {skipped} skipped.")


if __name__ == "__main__":
    main()
