#!/usr/bin/env python3
"""
Generate Artifact Forge textures (16x16 block top/side/bottom + 176x166 GUI).

Design:
  - Block: dark iron anvils with mystical runes, slot for fuel + small spirit-stone altar
    top:    anvil-like dark iron surface with central glowing rune (cyan/teal spirit flame)
    side:   anvil body with vertical channel, faintly glowing cracks, hammer mark
    bottom: plain dark iron plate
  - GUI:   dark iron frame, 5 forge slots (blank/material/catalyst/fuel + output),
           a fire-pit progress bar area, player inv grid.
"""
import os
from PIL import Image, ImageDraw

ROOT = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse"

# ─── palette ─────────────────────────────────────────────────────────
IRON_DEEP    = (38, 38, 48, 255)
IRON_MID     = (62, 62, 76, 255)
IRON_LIGHT   = (94, 94, 110, 255)
IRON_EDGE    = (24, 24, 32, 255)
RUNE_GLOW    = (96, 220, 220, 255)   # cyan spirit flame
RUNE_HOT     = (220, 250, 255, 255)  # white-hot core
EMBER        = (220, 110, 40, 255)
EMBER_DARK   = (130, 50, 18, 255)
STONE_DARK   = (54, 50, 50, 255)
STONE_LIGHT  = (88, 80, 76, 255)
SLOT_VOID    = (16, 16, 22, 255)
GUI_FRAME    = (76, 76, 92, 255)
GUI_BG       = (44, 44, 56, 255)
TEXT_DARK    = (40, 40, 40, 255)


# ─── block top (16x16) ────────────────────────────────────────────────
def gen_top():
    img = Image.new("RGBA", (16, 16), IRON_DEEP)
    d = ImageDraw.Draw(img)
    # base iron plate
    d.rectangle((0, 0, 15, 15), fill=IRON_DEEP)
    # bevel highlights (top/left light, bottom/right dark)
    for i in range(16):
        img.putpixel((i, 0), IRON_LIGHT)
        img.putpixel((i, 15), IRON_EDGE)
        img.putpixel((0, i), IRON_LIGHT)
        img.putpixel((15, i), IRON_EDGE)
    # mid plate (slightly raised)
    d.rectangle((2, 2, 13, 13), fill=IRON_MID)
    for i in range(2, 14):
        img.putpixel((i, 2), IRON_LIGHT)
        img.putpixel((i, 13), IRON_EDGE)
        img.putpixel((2, i), IRON_LIGHT)
        img.putpixel((13, i), IRON_EDGE)
    # central rune circle (5x5)
    cx, cy = 8, 8
    # outer ring
    for dx in (-2, -1, 0, 1, 2):
        for dy in (-2, -1, 0, 1, 2):
            if abs(dx) + abs(dy) == 3 or (abs(dx) == 2 and abs(dy) == 2):
                img.putpixel((cx + dx, cy + dy), RUNE_GLOW)
            elif abs(dx) + abs(dy) <= 1:
                img.putpixel((cx + dx, cy + dy), RUNE_HOT)
    # crosshair marks at cardinal points (forge alignment)
    img.putpixel((cx, cy - 3), RUNE_GLOW)
    img.putpixel((cx, cy + 3), RUNE_GLOW)
    img.putpixel((cx - 3, cy), RUNE_GLOW)
    img.putpixel((cx + 3, cy), RUNE_GLOW)
    # corner rivets
    for (px, py) in [(3, 3), (12, 3), (3, 12), (12, 12)]:
        img.putpixel((px, py), IRON_EDGE)
        img.putpixel((px - 1, py), IRON_LIGHT)
        img.putpixel((px, py - 1), IRON_LIGHT)
    img.save(f"{ROOT}/textures/block/artifact_forge_top.png")


# ─── block side (16x16) ───────────────────────────────────────────────
def gen_side():
    img = Image.new("RGBA", (16, 16), IRON_DEEP)
    d = ImageDraw.Draw(img)
    # body
    d.rectangle((0, 0, 15, 15), fill=IRON_DEEP)
    # bevel
    for i in range(16):
        img.putpixel((i, 0), IRON_LIGHT)
        img.putpixel((0, i), IRON_LIGHT)
        img.putpixel((15, i), IRON_EDGE)
        img.putpixel((i, 15), IRON_EDGE)
    # anvil-like top platform (rows 1-3, inset)
    d.rectangle((2, 1, 13, 3), fill=IRON_MID)
    for i in range(2, 14):
        img.putpixel((i, 1), IRON_LIGHT)
    # narrow waist (rows 4-9, columns 4-11)
    d.rectangle((4, 4, 11, 9), fill=IRON_DEEP)
    for i in range(4, 12):
        img.putpixel((i, 4), IRON_EDGE)
        img.putpixel((i, 9), IRON_EDGE)
    # glowing forge door (rows 6-8, cols 6-9) — the fire opening
    d.rectangle((6, 6, 9, 8), fill=EMBER_DARK)
    img.putpixel((7, 7), EMBER)
    img.putpixel((8, 7), EMBER)
    img.putpixel((7, 6), RUNE_GLOW)
    img.putpixel((8, 6), RUNE_GLOW)
    # base plate (rows 10-14)
    d.rectangle((1, 10, 14, 14), fill=IRON_MID)
    for i in range(1, 15):
        img.putpixel((i, 10), IRON_LIGHT)
        img.putpixel((i, 14), IRON_EDGE)
    # small spirit-stone altar slot on the base (cols 7-8, row 12)
    img.putpixel((7, 12), SLOT_VOID)
    img.putpixel((8, 12), SLOT_VOID)
    img.putpixel((7, 13), IRON_EDGE)
    img.putpixel((8, 13), IRON_EDGE)
    # hammer mark on the platform (row 2)
    img.putpixel((4, 2), RUNE_GLOW)
    img.putpixel((5, 2), RUNE_GLOW)
    img.putpixel((10, 2), RUNE_GLOW)
    img.putpixel((11, 2), RUNE_GLOW)
    img.save(f"{ROOT}/textures/block/artifact_forge_side.png")


# ─── block bottom (16x16) ─────────────────────────────────────────────
def gen_bottom():
    img = Image.new("RGBA", (16, 16), IRON_DEEP)
    d = ImageDraw.Draw(img)
    d.rectangle((0, 0, 15, 15), fill=IRON_DEEP)
    # bevel
    for i in range(16):
        img.putpixel((i, 0), IRON_EDGE)
        img.putpixel((i, 15), IRON_EDGE)
        img.putpixel((0, i), IRON_EDGE)
        img.putpixel((15, i), IRON_EDGE)
    # large center plate
    d.rectangle((1, 1, 14, 14), fill=IRON_MID)
    # four rivets at corners
    for (px, py) in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        img.putpixel((px, py), IRON_EDGE)
        img.putpixel((px - 1, py), IRON_LIGHT)
        img.putpixel((px, py - 1), IRON_LIGHT)
        img.putpixel((px + 1, py), IRON_EDGE)
        img.putpixel((px, py + 1), IRON_EDGE)
    # cross braces
    for i in range(3, 13):
        img.putpixel((i, i), IRON_EDGE)
        img.putpixel((i, 15 - i), IRON_EDGE)
    img.save(f"{ROOT}/textures/block/artifact_forge_bottom.png")


# ─── GUI (176 x 166) ──────────────────────────────────────────────────
# Mirror of the pill furnace GUI: outer dark frame, slot grid, fire-pit,
# player inv (3x9) + hotbar (1x9). Add 5 forge slots + craft button area.
def gen_gui():
    W, H = 176, 166
    # Canvas is wider than the GUI (W + 24) to also hold the progress-bar overlay
    # sprite at columns [W .. W+24). The screen blits this region for the filled
    # portion of the progress bar.
    CANVAS_W = W + 24
    img = Image.new("RGBA", (CANVAS_W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # background
    d.rectangle((0, 0, W - 1, H - 1), fill=GUI_BG)
    # outer frame
    d.rectangle((0, 0, W - 1, H - 1), outline=GUI_FRAME)
    d.rectangle((1, 1, W - 2, H - 2), outline=IRON_DEEP)

    # title strip
    d.rectangle((3, 3, W - 4, 14), fill=IRON_MID)
    for i in range(3, W - 3):
        img.putpixel((i, 3), IRON_LIGHT)

    # ── forge slot row: blank/material/catalyst/fuel (4 input slots) ──
    # match ArtifactForgeMenu positions: x=44,66,88,110 / y=36
    slot_positions = [(44, 36), (66, 36), (88, 36), (110, 36)]
    for (sx, sy) in slot_positions:
        # slot bevel: dark inside, light top-left edge
        d.rectangle((sx - 1, sy - 1, sx + 16, sy + 16), fill=SLOT_VOID)
        for i in range(0, 18):
            img.putpixel((sx - 1 + i, sy - 1), IRON_LIGHT)
            img.putpixel((sx - 1 + i, sy + 16), IRON_EDGE)
            img.putpixel((sx - 1, sy - 1 + i), IRON_LIGHT)
            img.putpixel((sx + 16, sy - 1 + i), IRON_EDGE)

    # arrow from fuel → output area (around x=124-140, y=36)
    for i in range(124, 140):
        img.putpixel((i, 36), IRON_LIGHT)
        img.putpixel((i, 44), IRON_EDGE)
    # arrowhead
    for k in range(4):
        img.putpixel((140 + k, 36 + k), IRON_LIGHT)
        img.putpixel((140 + k, 44 - k), IRON_LIGHT)

    # output slot (x=156, y=36) — bigger highlight
    d.rectangle((155, 35, 172, 53), fill=SLOT_VOID)
    for i in range(0, 18):
        img.putpixel((155 + i, 35), RUNE_GLOW)
        img.putpixel((155 + i, 52), IRON_EDGE)
        img.putpixel((155, 35 + i), RUNE_GLOW)
        img.putpixel((171, 35 + i), IRON_EDGE)

    # craft button area (x=116-166, y=56-72) — placeholder for the button widget
    d.rectangle((116, 56, 165, 71), fill=IRON_DEEP)
    for i in range(116, 166):
        img.putpixel((i, 56), IRON_LIGHT)
        img.putpixel((i, 70), IRON_EDGE)

    # fire-pit indicator under the slots (centered)
    d.rectangle((80, 56, 96, 70), fill=EMBER_DARK)
    img.putpixel((84, 64), EMBER)
    img.putpixel((88, 64), EMBER)
    img.putpixel((86, 62), RUNE_GLOW)
    img.putpixel((90, 62), RUNE_GLOW)
    img.putpixel((82, 68), IRON_EDGE)
    img.putpixel((94, 68), IRON_EDGE)

    # progress bar slot (24 wide, 17 tall) — at (124, 37)
    # drawn empty by default; the screen overlays the filled portion
    d.rectangle((124, 37, 147, 53), fill=SLOT_VOID)

    # ── player inventory (3x9 starting at x=8, y=84) ─────────────────
    for row in range(3):
        for col in range(9):
            sx = 8 + col * 18
            sy = 84 + row * 18
            d.rectangle((sx - 1, sy - 1, sx + 16, sy + 16), fill=SLOT_VOID)
            for i in range(0, 18):
                img.putpixel((sx - 1 + i, sy - 1), IRON_LIGHT)
                img.putpixel((sx - 1 + i, sy + 16), IRON_EDGE)
                img.putpixel((sx - 1, sy - 1 + i), IRON_LIGHT)
                img.putpixel((sx + 16, sy - 1 + i), IRON_EDGE)

    # ── hotbar (1x9 at y=142) ─────────────────────────────────────────
    for col in range(9):
        sx = 8 + col * 18
        sy = 142
        d.rectangle((sx - 1, sy - 1, sx + 16, sy + 16), fill=SLOT_VOID)
        for i in range(0, 18):
            img.putpixel((sx - 1 + i, sy - 1), IRON_LIGHT)
            img.putpixel((sx - 1 + i, sy + 16), IRON_EDGE)
            img.putpixel((sx - 1, sy - 1 + i), IRON_LIGHT)
            img.putpixel((sx + 16, sy - 1 + i), IRON_EDGE)

    # ── progress bar overlay sprite (176+0..24, 0..17) — filled gradient ──
    # The screen blits from (176, 0) of this same texture for the progress fill.
    # Draw it just past the right edge of the GUI background.
    for x in range(24):
        for y in range(17):
            # gradient: ember-dark (left) → ember (mid) → rune-glow (right)
            t = x / 24.0
            if t < 0.5:
                r = int(EMBER_DARK[0] + (EMBER[0] - EMBER_DARK[0]) * (t * 2))
                g = int(EMBER_DARK[1] + (EMBER[1] - EMBER_DARK[1]) * (t * 2))
                b = int(EMBER_DARK[2] + (EMBER[2] - EMBER_DARK[2]) * (t * 2))
            else:
                t2 = (t - 0.5) * 2
                r = int(EMBER[0] + (RUNE_GLOW[0] - EMBER[0]) * t2)
                g = int(EMBER[1] + (RUNE_GLOW[1] - EMBER[1]) * t2)
                b = int(EMBER[2] + (RUNE_GLOW[2] - EMBER[2]) * t2)
            img.putpixel((W + x, y), (r, g, b, 255))

    # Extend canvas width to include the progress bar overlay (W=176+24=200)
    # Already created with the wider canvas, so just save.
    img.save(f"{ROOT}/textures/gui/artifact_forge.png")


if __name__ == "__main__":
    os.makedirs(f"{ROOT}/textures/block", exist_ok=True)
    os.makedirs(f"{ROOT}/textures/gui", exist_ok=True)
    gen_top()
    gen_side()
    gen_bottom()
    gen_gui()
    print("Generated 4 artifact_forge textures (top, side, bottom, gui).")
