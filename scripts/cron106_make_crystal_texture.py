#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-106 — Cultivation Planet Crystal texture generator.

Generates a 16x16 PNG texture for the CultivationPlanetCrystalBlock:
  - Deep purple core (the spiritual crystal)
  - Gold rim (sacred metal framework)
  - Light purple swirl (Qi emanation)
  - White center seed (the planet's sealed origin)

The texture is visually appropriate for the "sealed core of Planet Suzaku"
motif — glowing purple crystal with gold framing.

Run: python3 /home/z/my-project/scripts/cron106_make_crystal_texture.py
Output: /home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/block/cultivation_planet_crystal.png
"""

from pathlib import Path
import struct
import zlib

# ── 16x16 pixel art ────────────────────────────────────────────────────────
# Color palette (R, G, B, A):
#   DEEP_PURPLE = (90, 30, 130, 255)    — the crystal body
#   MID_PURPLE  = (140, 70, 180, 255)   — the inner glow
#   GOLD        = (210, 165, 70, 255)   — the rim/frame
#   BRIGHT_GOLD = (255, 215, 100, 255)  — highlights
#   WHITE_SEED  = (255, 245, 230, 255)  — the center seed
#   TRANSPARENT = (0, 0, 0, 0)          — corners (so it looks like a floating crystal)

DEEP_PURPLE  = (90, 30, 130, 255)
MID_PURPLE   = (140, 70, 180, 255)
GOLD         = (210, 165, 70, 255)
BRIGHT_GOLD  = (255, 215, 100, 255)
WHITE_SEED   = (255, 245, 230, 255)
TRANSPARENT  = (0, 0, 0, 0)

# 16x16 grid. Each row is 16 pixels.
# Layout:
#   - Outer 1px border on top/left/right/bottom: gold frame (with corner cuts)
#   - Inner 2px ring: deep purple
#   - Inner 4px diamond: mid purple
#   - Center 2x2: white seed (the planet's sealed origin)
#   - Corner pixels: transparent (so it looks like a beveled crystal)

PIXELS = [
    # y=0..15 (top to bottom)
    # Row 0: gold corners + top edge
    [TRANSPARENT, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, TRANSPARENT],
    # Row 1: gold edges + deep purple top
    [GOLD, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, GOLD],
    # Row 2: gold edges + deep purple + mid purple top
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 3: gold edges + deep + mid + bright gold accent
    [GOLD, DEEP_PURPLE, MID_PURPLE, BRIGHT_GOLD, BRIGHT_GOLD, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, BRIGHT_GOLD, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 4: gold edges + deep + mid (wider) + white seed corners
    [GOLD, DEEP_PURPLE, MID_PURPLE, BRIGHT_GOLD, WHITE_SEED, WHITE_SEED, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, WHITE_SEED, WHITE_SEED, BRIGHT_GOLD, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 5: gold edges + deep + mid + white seed row
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 6: gold edges + deep + mid (full mid ring)
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 7: gold edges + deep + mid (center horizontal — the brightest row)
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, BRIGHT_GOLD, BRIGHT_GOLD, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 8: gold edges + deep + mid (center horizontal — the brightest row)
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, BRIGHT_GOLD, BRIGHT_GOLD, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 9: gold edges + deep + mid (full mid ring)
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 10: gold edges + deep + mid + white seed row
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, WHITE_SEED, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 11: gold edges + deep + mid + white seed corners
    [GOLD, DEEP_PURPLE, MID_PURPLE, BRIGHT_GOLD, WHITE_SEED, WHITE_SEED, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, WHITE_SEED, WHITE_SEED, BRIGHT_GOLD, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 12: gold edges + deep + mid + bright gold accent
    [GOLD, DEEP_PURPLE, MID_PURPLE, BRIGHT_GOLD, BRIGHT_GOLD, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, BRIGHT_GOLD, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 13: gold edges + deep + mid bottom
    [GOLD, DEEP_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, MID_PURPLE, DEEP_PURPLE, GOLD],
    # Row 14: gold edges + deep bottom
    [GOLD, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, DEEP_PURPLE, GOLD],
    # Row 15: gold corners + bottom edge
    [TRANSPARENT, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, GOLD, TRANSPARENT],
]

assert len(PIXELS) == 16 and all(len(row) == 16 for row in PIXELS), "Must be 16x16"

# ── PNG encoder (no PIL dependency) ────────────────────────────────────────
# PNG format:
#   8-byte signature
#   IHDR chunk (width, height, bit_depth=8, color_type=6 (RGBA), compression=0, filter=0, interlace=0)
#   IDAT chunk (zlib-compressed scanlines, each prefixed with a filter byte 0)
#   IEND chunk

def make_chunk(chunk_type: bytes, data: bytes) -> bytes:
    """Build a PNG chunk: length (4 bytes BE) + type + data + CRC32 (4 bytes BE)."""
    return struct.pack(">I", len(data)) + chunk_type + data + struct.pack(">I", zlib.crc32(chunk_type + data) & 0xffffffff)


def encode_png(width: int, height: int, pixels: list) -> bytes:
    """Encode RGBA pixels to a PNG byte string."""
    # PNG signature
    sig = b"\x89PNG\r\n\x1a\n"
    # IHDR
    ihdr_data = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    ihdr = make_chunk(b"IHDR", ihdr_data)
    # IDAT — each scanline prefixed with filter byte 0
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # filter byte 0 (None)
        for x in range(width):
            r, g, b, a = pixels[y][x]
            raw.extend([r, g, b, a])
    idat = make_chunk(b"IDAT", zlib.compress(bytes(raw), 9))
    # IEND
    iend = make_chunk(b"IEND", b"")
    return sig + ihdr + idat + iend


def main() -> None:
    out_path = Path("/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/block/cultivation_planet_crystal.png")
    out_path.parent.mkdir(parents=True, exist_ok=True)
    png_bytes = encode_png(16, 16, PIXELS)
    out_path.write_bytes(png_bytes)
    print(f"Wrote {out_path} ({len(png_bytes)} bytes)")
    # Sanity check: read back
    re_read = out_path.read_bytes()
    assert re_read == png_bytes, "Read-back mismatch"
    assert re_read[:8] == b"\x89PNG\r\n\x1a\n", "PNG signature missing"
    print("PNG signature OK; file is a valid PNG.")


if __name__ == "__main__":
    main()
