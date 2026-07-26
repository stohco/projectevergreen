#!/usr/bin/env python3
"""
CRON-75: Generate mysterious_stone.png texture.

Canon: The Heaven-Defying Bead (天逆珠) was found by Wang Lin inside an
ordinary-looking stone on Heng Yue mountain (RI Ch. 8). The stone should
look unremarkable from a distance but reveal subtle iridescence up close —
hinting at the treasure within.

Design:
- 16x16 pixel texture (Minecraft block standard)
- Base: dark stone-grey with amethyst tint
- Cracks: thin violet/white veins suggesting hidden power
- Iridescence: scattered cyan/magenta pixels for otherworldly shimmer
- Output: src/main/resources/assets/ergenverse/textures/block/mysterious_stone.png
"""
import struct
import zlib
import random
import os

# Deterministic seed for reproducibility
random.seed(7575)

WIDTH = 16
HEIGHT = 16

# Color palette (RGBA)
def rgba(r, g, b, a=255):
    return bytes([r, g, b, a])

# Base stone colors (dark grey with amethyst tint)
STONE_DARK = (52, 48, 60)      # deep amethyst-grey
STONE_MID = (78, 70, 92)       # mid amethyst
STONE_LIGHT = (108, 96, 124)   # light amethyst highlight
CRACK_VIOLET = (180, 140, 220) # glowing violet crack
CRACK_WHITE = (220, 200, 240)  # bright crack center
SHIMMER_CYAN = (140, 220, 240) # cyan iridescence
SHIMMER_MAGENTA = (220, 140, 200) # magenta iridescence
SHADOW = (28, 24, 36)          # deep shadow

# Generate the pixel grid
pixels = [[STONE_MID for _ in range(WIDTH)] for _ in range(HEIGHT)]

# Step 1: Base stone texture — noise-based variation
for y in range(HEIGHT):
    for x in range(WIDTH):
        n = random.random()
        if n < 0.25:
            pixels[y][x] = STONE_DARK
        elif n < 0.55:
            pixels[y][x] = STONE_MID
        elif n < 0.85:
            pixels[y][x] = STONE_LIGHT
        else:
            pixels[y][x] = SHADOW

# Step 2: Cracks — generate 2-3 thin veins across the stone
def draw_crack(start_x, start_y, length, direction):
    """Draw a crack vein from a starting point."""
    x, y = start_x, start_y
    for i in range(length):
        if 0 <= x < WIDTH and 0 <= y < HEIGHT:
            # Crack center is bright white-violet
            pixels[y][x] = CRACK_WHITE
            # Surrounding pixels are dimmer violet
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                nx, ny = x + dx, y + dy
                if 0 <= nx < WIDTH and 0 <= ny < HEIGHT:
                    if pixels[ny][nx] != CRACK_WHITE:
                        pixels[ny][nx] = CRACK_VIOLET
            # Move in direction with some randomness
            if direction == 'horizontal':
                x += 1
                if random.random() < 0.4:
                    y += random.choice([-1, 0, 1])
            elif direction == 'vertical':
                y += 1
                if random.random() < 0.4:
                    x += random.choice([-1, 0, 1])
            elif direction == 'diagonal':
                x += 1
                y += 1
                if random.random() < 0.3:
                    x += random.choice([-1, 0])
                    y += random.choice([-1, 0])

# Draw 3 cracks radiating from center-ish (suggesting the bead's containment)
draw_crack(3, 8, 6, 'horizontal')
draw_crack(8, 3, 7, 'vertical')
draw_crack(5, 5, 5, 'diagonal')

# Step 3: Iridescent shimmer — scattered bright pixels
shimmer_count = 8
placed = 0
attempts = 0
while placed < shimmer_count and attempts < 100:
    sx = random.randint(1, WIDTH - 2)
    sy = random.randint(1, HEIGHT - 2)
    # Only place shimmer on non-crack pixels
    if pixels[sy][sx] not in (CRACK_WHITE, CRACK_VIOLET):
        color = SHIMMER_CYAN if random.random() < 0.5 else SHIMMER_MAGENTA
        pixels[sy][sx] = color
        placed += 1
    attempts += 1

# Step 4: Border darkening (subtle vignette for depth)
for y in range(HEIGHT):
    for x in range(WIDTH):
        if x == 0 or x == WIDTH - 1 or y == 0 or y == HEIGHT - 1:
            r, g, b = pixels[y][x]
            pixels[y][x] = (max(0, r - 20), max(0, g - 20), max(0, b - 20))

# Flatten to RGBA bytes
raw_data = bytearray()
for y in range(HEIGHT):
    for x in range(WIDTH):
        r, g, b = pixels[y][x]
        raw_data.extend([r, g, b, 255])

# Encode as PNG
def write_png(filename, width, height, rgba_bytes):
    """Minimal PNG encoder (no external deps)."""
    def chunk(chunk_type, data):
        c = chunk_type + data
        crc = zlib.crc32(c) & 0xffffffff
        return struct.pack('>I', len(data)) + c + struct.pack('>I', crc)

    # PNG signature
    sig = b'\x89PNG\r\n\x1a\n'
    # IHDR
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)  # 8-bit, RGBA
    # IDAT — add filter byte (0) at start of each row
    raw = bytearray()
    for y in range(height):
        raw.append(0)  # filter type: None
        raw.extend(rgba_bytes[y * width * 4 : (y + 1) * width * 4])
    idat = zlib.compress(bytes(raw))
    # IEND
    return sig + chunk(b'IHDR', ihdr) + chunk(b'IDAT', idat) + chunk(b'IEND', b'')

output_path = '/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/block/mysterious_stone.png'
os.makedirs(os.path.dirname(output_path), exist_ok=True)
png_data = write_png(output_path, WIDTH, HEIGHT, raw_data)
with open(output_path, 'wb') as f:
    f.write(png_data)

print(f"Generated {output_path}")
print(f"  Size: {WIDTH}x{HEIGHT} pixels")
print(f"  File size: {len(png_data)} bytes")
print(f"  Design: dark amethyst stone with violet cracks + cyan/magenta iridescence")
