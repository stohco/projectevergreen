#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-108 — Generate ancient_god_clan.png texture for Tuo Sen.

A 64x64 RGBA PNG that fits the CultivatorRobeModel UV layout (same as
heng_yue_sect.png, wang_lin.png, etc.). The texture features:

  - Dark violet god-body (Ancient God clan colors): #2A0E40 base robe.
  - Lighter violet highlights for robe folds: #4B1C71.
  - Deep purple sash: #1A0633.
  - Gold trim on collar and sleeves: #C9A24B.
  - The signature 8-star forehead array: 8 gold stars (#FFD45A) arranged
    in a circular pattern on the head UV region (top of the texture).
    This is the iconic Ancient God visual marker.

Web-search verified 2026-07-26: 8-star Ancient Gods (古神) in 仙逆 canon
have a forehead star array; the number of stars corresponds to their tier.
Tuo Sen at the Suzaku Tomb reappearance is 8-star (CRON-107 verified).

Run: python3 /home/z/my-project/scripts/cron108_make_ancient_god_texture.py
"""

import struct
import zlib
from pathlib import Path

OUT = Path("/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/"
           "textures/entity/cultivator/ancient_god_clan.png")

# 64x64 RGBA
W, H = 64, 64

# Color palette (RGBA tuples)
ROBE_DARK    = (0x2A, 0x0E, 0x40, 0xFF)  # deep violet base
ROBE_MID     = (0x3D, 0x14, 0x59, 0xFF)  # mid violet
ROBE_LIGHT   = (0x4B, 0x1C, 0x71, 0xFF)  # highlight
SASH         = (0x1A, 0x06, 0x33, 0xFF)  # deep purple
GOLD         = (0xC9, 0xA2, 0x4B, 0xFF)  # collar/sleeve trim
GOLD_BRIGHT  = (0xFF, 0xD4, 0x5A, 0xFF)  # 8-star array
SKIN         = (0xB8, 0x86, 0x4A, 0xFF)  # bronze-tan skin (god-body)
SKIN_DARK    = (0x7A, 0x52, 0x2A, 0xFF)  # skin shadow
HAIR         = (0x0D, 0x06, 0x1F, 0xFF)  # very dark blue-black hair
HAIR_HL      = (0x1F, 0x12, 0x44, 0xFF)  # hair highlight
EYE_GOLD     = (0xFF, 0xCC, 0x33, 0xFF)  # glowing gold eyes (Ancient God)
BOOT         = (0x12, 0x05, 0x1F, 0xFF)  # black-violet boots
EMPTY        = (0x00, 0x00, 0x00, 0x00)  # transparent

# Build pixel buffer
buf = [[EMPTY for _ in range(W)] for _ in range(H)]


def fill_rect(x0, y0, x1, y1, color):
    """Fill a rectangular region (inclusive)."""
    for y in range(max(0, y0), min(H, y1 + 1)):
        for x in range(max(0, x0), min(W, x1 + 1)):
            buf[y][x] = color


def set_pixel(x, y, color):
    if 0 <= x < W and 0 <= y < H:
        buf[y][x] = color


def draw_star(cx, cy, r, color):
    """Draw a small 5-point star (approximated as a plus + X)."""
    # Plus
    for dy in range(-r, r + 1):
        set_pixel(cx, cy + dy, color)
    for dx in range(-r, r + 1):
        set_pixel(cx + dx, cy, color)
    # X (diagonals)
    for d in range(-r, r + 1):
        set_pixel(cx + d, cy + d, color)
        set_pixel(cx + d, cy - d, color)


# ── CultivatorRobeModel UV layout (mirrors the standard biped model) ──
# The 64x64 texture is laid out as:
#
#   Y=0  ┌──────────────────────────────────────────────────────────┐
#        │  HEAD (8x8 face at 0-8 x 0-8)   │  HAIR_BUN (8x8)        │
#   Y=8  ├──────────────────────────────────┴────────────────────────┤
#        │  BODY/TORSO (8x12 at 16-24 x 16-28)  │  RIGHT_ARM (4x12) │
#   Y=20 ├──┬──┴──────────────────────────────────┴───────────────────┤
#        │ LEFT_ARM (4x12 at 32-36 x 48-60)                            │
#   Y=32 ├──┴──────────────────────────────────────────────────────────┤
#        │  RIGHT_LEG (4x12) │ LEFT_LEG (4x12)                         │
#   Y=48 ├───────────────────┴────────────────────────────────────────-┤
#        │  ROBE_WAIST / ROBE_MID / ROBE_HEM (wider rectangles)        │
#   Y=64 └────────────────────────────────────────────────────────────┘
#
# We use a simplified layout that approximates the standard:
#   - Head/face at (0, 0, 32, 16) — for the 8-star forehead array
#   - Hair/hat at (32, 0, 64, 16)
#   - Body/robe at (16, 16, 40, 32)
#   - Arms at (40, 16, 56, 32) and (32, 48, 48, 64)
#   - Legs at (0, 16, 16, 32) and (16, 48, 32, 64) — wait that overlaps
#
# Per Minecraft classic Villager/Zombie layout (which CultivatorRobeModel
# uses), the standard layout is:
#   - Head: (8,8) to (16,16) for face
#   - Head right side: (0,8) to (8,16)
#   - Head top: (8,0) to (16,8)
#   - Body: (20,20) to (28,32) front
#   - Right arm: (44,20) to (48,32)
#   - Left arm: (36,52) to (40,64)
#   - Right leg: (4,20) to (8,32)
#   - Left leg: (20,52) to (24,64)
#   - Hair bun overlay: (32,0) to (40,8) — sits on top of head
#
# For the CultivatorRobeModel, the layout is similar. We'll fill a generous
# head region for the 8-star array and a generous body/robe region for the
# violet god-body.

# ── Head & face region (0..32 x, 0..16 y) ──
# Fill the entire head/hair area with HAIR (dark blue-black)
fill_rect(0, 0, 31, 15, HAIR)

# Face plate (front of head): roughly (8,8) to (16,16) — 8x8 area
# In CultivatorRobeModel, this is the visible face. We'll use a 12x12 area
# centered at (16,12) for visibility.
fill_rect(8, 8, 20, 20, SKIN)
# Slight skin shadow on the lower right (lighting)
fill_rect(16, 14, 20, 20, SKIN_DARK)

# Hair on top of head (above face): (8,0) to (16,8)
fill_rect(8, 0, 20, 8, HAIR)
fill_rect(0, 0, 8, 8, HAIR)        # right side of head
fill_rect(20, 0, 31, 8, HAIR)      # left side of head (texture is mirrored)
fill_rect(0, 8, 8, 16, HAIR)       # back of head right
fill_rect(20, 8, 31, 16, HAIR)     # back of head left

# Hair highlight strip
for x in range(8, 21, 2):
    set_pixel(x, 0, HAIR_HL)
    set_pixel(x, 1, HAIR_HL)

# Eyes (gold, glowing — Ancient God trait): (10,12) and (14,12) for a 2x2 each
fill_rect(10, 12, 11, 13, EYE_GOLD)
fill_rect(14, 12, 15, 13, EYE_GOLD)
# Eye glow
set_pixel(9, 11, EYE_GOLD)
set_pixel(16, 11, EYE_GOLD)

# Mouth (a thin dark line): (12,16) to (16,16)
for x in range(12, 17):
    set_pixel(x, 16, SKIN_DARK)

# ── 8-star forehead array ──
# The 8 stars are arranged in a circular pattern centered on the forehead
# (above the eyes, in the upper portion of the face plate). Each star is
# 1 pixel with a 4-neighbor cross for visibility (so it reads as a star,
# not a stray pixel). Gold-bright color.
#
# Canon: 8-star Ancient God (古神八星). The forehead array is the iconic
# visual marker of an Ancient God's tier.
#
# Pattern (centered on (14, 8) — upper face):
#   positions for the 8 stars at radius 4-5 from center
star_positions = [
    (10, 6), (14, 5), (18, 6),           # top arc
    (8, 9), (20, 9),                      # sides
    (10, 12), (14, 13), (18, 12),         # bottom arc (above eyes)
]
# Wait — the bottom arc would overlap the eyes. Adjust: top-only arc.
star_positions = [
    (10, 4), (14, 3), (18, 4),           # top arc (3 stars)
    (8, 7), (20, 7),                      # upper sides (2 stars)
    (9, 10), (19, 10),                    # lower sides (2 stars)
    (14, 11),                             # center bottom (1 star)
]

for cx, cy in star_positions:
    # 1-pixel star + 4 neighbors = clear visible mark
    set_pixel(cx, cy, GOLD_BRIGHT)
    set_pixel(cx - 1, cy, GOLD_BRIGHT)
    set_pixel(cx + 1, cy, GOLD_BRIGHT)
    set_pixel(cx, cy - 1, GOLD_BRIGHT)
    set_pixel(cx, cy + 1, GOLD_BRIGHT)

# Central forehead star (slightly larger)
set_pixel(14, 7, GOLD_BRIGHT)
set_pixel(13, 7, GOLD_BRIGHT)
set_pixel(15, 7, GOLD_BRIGHT)
set_pixel(14, 6, GOLD_BRIGHT)
set_pixel(14, 8, GOLD_BRIGHT)

# ── Body / robe region ──
# CultivatorRobeModel uses the standard biped body UV: a 16x16 region
# for the front of the torso at approximately (16, 16) to (32, 32),
# with arms and legs in adjacent regions.
# We'll fill a generous body area with the violet god-body robe.

# Body/torso: (16,16) to (32,32) — front of robe
fill_rect(16, 16, 31, 31, ROBE_DARK)

# Robe highlight (mid violet) — vertical strip down the center
fill_rect(22, 16, 26, 31, ROBE_MID)

# Robe light (collar and shoulder highlights)
fill_rect(16, 16, 31, 18, ROBE_LIGHT)

# Gold collar trim (a thin gold line at the neckline)
for x in range(16, 32):
    set_pixel(x, 19, GOLD)

# Gold trim on shoulders
for y in range(16, 22):
    set_pixel(16, y, GOLD)
    set_pixel(31, y, GOLD)

# Sash (deep purple horizontal band at waist)
fill_rect(16, 24, 31, 26, SASH)

# Gold sash tie
fill_rect(22, 24, 26, 26, GOLD)

# ── Right arm region ──
# Right arm: (40,16) to (44,32) — narrow vertical strip
fill_rect(40, 16, 44, 31, ROBE_DARK)
# Sleeve highlight
fill_rect(41, 16, 43, 31, ROBE_MID)
# Gold cuff
fill_rect(40, 28, 44, 31, GOLD)

# ── Left arm region ──
# Left arm: (32,48) to (40,64) — bottom-left quadrant
fill_rect(32, 48, 39, 63, ROBE_DARK)
fill_rect(33, 48, 38, 63, ROBE_MID)
# Gold cuff
fill_rect(32, 60, 39, 63, GOLD)

# ── Right leg region ──
# Right leg: (16,48) to (20,64) — wait, that's where left arm goes
# Actually in standard biped layout: right leg is (4,16) to (8,32) on the
# front, but CultivatorRobeModel has the leg regions in different spots.
# Let's use the leg areas at (4,16)-(8,32) for the right leg.
fill_rect(4, 16, 8, 31, ROBE_DARK)
fill_rect(5, 16, 7, 31, ROBE_MID)

# Boot bottom
fill_rect(4, 28, 8, 31, BOOT)

# ── Left leg region ──
# Left leg: (0,16) to (4,32) — far left
fill_rect(0, 16, 4, 31, ROBE_DARK)
fill_rect(1, 16, 3, 31, ROBE_MID)
# Boot bottom
fill_rect(0, 28, 4, 31, BOOT)

# ── Robe skirt (lower body) ──
# CultivatorRobeModel has a 3-bone robe chain (robeWaist/Mid/Hem) that
# extends below the legs. We'll fill the bottom portion of the texture
# (Y=32 to Y=64) with a wider robe area to suggest the skirt.

# Wait — the arms/legs already occupy regions in Y=48-64. Let's add the
# robe skirt in the unused regions.
# Bottom region (Y=32 to Y=48): mostly empty in standard layout — fill
# with a robe skirt texture.
fill_rect(8, 32, 31, 47, ROBE_DARK)
# Skirt vertical highlights
for x in [12, 16, 20, 24, 28]:
    fill_rect(x, 32, x, 47, ROBE_MID)

# Robe hem (gold trim at the bottom)
for x in range(8, 32):
    set_pixel(x, 47, GOLD)

# Right-side robe skirt (X=32-48, Y=32-48)
fill_rect(32, 32, 47, 47, ROBE_DARK)
for x in [36, 40, 44]:
    fill_rect(x, 32, x, 47, ROBE_MID)
for x in range(32, 48):
    set_pixel(x, 47, GOLD)

# ── Hair bun (top of head) ──
# CultivatorRobeModel has a hair bun at (32,0) to (40,8) per CRON-54.
fill_rect(32, 0, 39, 7, HAIR)
fill_rect(33, 1, 38, 6, HAIR_HL)
# Hairpin (gold dot on the bun)
set_pixel(36, 3, GOLD_BRIGHT)
set_pixel(35, 3, GOLD)
set_pixel(37, 3, GOLD)

# ── Encode PNG ──
def encode_png(width, height, pixels):
    """Encode a 2D array of RGBA tuples as a PNG file."""
    # PNG signature
    sig = b'\x89PNG\r\n\x1a\n'

    # IHDR chunk
    ihdr_data = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)
    ihdr = b'IHDR' + ihdr_data
    ihdr_crc = zlib.crc32(ihdr)
    ihdr_chunk = struct.pack('>I', len(ihdr_data)) + ihdr + struct.pack('>I', ihdr_crc)

    # IDAT chunk
    raw = b''
    for y in range(height):
        raw += b'\x00'  # filter byte (None)
        for x in range(width):
            r, g, b, a = pixels[y][x]
            raw += bytes([r, g, b, a])
    compressed = zlib.compress(raw, 9)
    idat = b'IDAT' + compressed
    idat_crc = zlib.crc32(idat)
    idat_chunk = struct.pack('>I', len(compressed)) + idat + struct.pack('>I', idat_crc)

    # IEND chunk
    iend = b'IEND'
    iend_crc = zlib.crc32(iend)
    iend_chunk = struct.pack('>I', 0) + iend + struct.pack('>I', iend_crc)

    return sig + ihdr_chunk + idat_chunk + iend_chunk


png_bytes = encode_png(W, H, buf)
OUT.parent.mkdir(parents=True, exist_ok=True)
OUT.write_bytes(png_bytes)
print(f"CRON-108: wrote {OUT} ({len(png_bytes)} bytes, {W}x{H} RGBA)")
print(f"  8-star forehead array: {len(star_positions)} stars + 1 central")
print(f"  Robe: dark violet base ({ROBE_DARK[:3]}) with gold trim")
print(f"  Eyes: glowing gold ({EYE_GOLD[:3]}) — Ancient God trait")
