#!/usr/bin/env python3
"""
Generate the Alchemy Furnace textures (GUI + block faces).

Outputs:
  assets/ergenverse/textures/gui/alchemy_furnace.png        (200x190)
  assets/ergenverse/textures/block/alchemy_furnace_top.png  (16x16)
  assets/ergenverse/textures/block/alchemy_furnace_front.png(16x16)
  assets/ergenverse/textures/block/alchemy_furnace_side.png (16x16)

Design notes:
  - GUI background is dark-stone (60,60,60) with subtle horizontal seams,
    matching vanilla furnace's GUI dark area.
  - Slot borders: outer dark (37,37,37), inner light (139,139,139) —
    the standard vanilla slot inset.
  - Flame icon: 14x14 orange-yellow gradient.
  - Arrow icon: 24x17 white arrow on transparent background.
  - Block textures: dark metal (50,50,55) with rivets; front has a glowing
    fire opening (orange-to-yellow); top has a circular cauldron mouth.
"""
from PIL import Image, ImageDraw
import os

ASSETS = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse"
GUI_DIR = os.path.join(ASSETS, "textures", "gui")
BLOCK_DIR = os.path.join(ASSETS, "textures", "block")
os.makedirs(GUI_DIR, exist_ok=True)
os.makedirs(BLOCK_DIR, exist_ok=True)


# ── Palette ──────────────────────────────────────────────────────────────
DARK_STONE   = (60, 60, 60, 255)
SEAM_DARK    = (45, 45, 45, 255)
SEAM_LIGHT   = (75, 75, 75, 255)
SLOT_OUTER   = (37, 37, 37, 255)   # darker outer border
SLOT_INNER   = (139, 139, 139, 255)  # lighter inner highlight
SLOT_BG      = (75, 75, 75, 255)
FLAME_CORE   = (255, 220, 80, 255)
FLAME_MID    = (255, 160, 30, 255)
FLAME_EDGE   = (200, 80, 20, 255)
ARROW_COLOR  = (220, 220, 220, 255)
ARROW_BORDER = (160, 160, 160, 255)

BLOCK_METAL_DARK  = (40, 40, 45, 255)
BLOCK_METAL_MID   = (55, 55, 60, 255)
BLOCK_METAL_LIGHT = (75, 75, 80, 255)
BLOCK_RIVET       = (90, 90, 95, 255)
BLOCK_FIRE_CORE   = (255, 220, 80, 255)
BLOCK_FIRE_MID    = (255, 140, 30, 255)
BLOCK_FIRE_EDGE   = (180, 60, 20, 255)
BLOCK_CAULDRON_DARK = (28, 28, 32, 255)
BLOCK_CAULDRON_LIGHT = (55, 55, 60, 255)


def fill_box(img, x, y, w, h, color):
    """Fill a (x, y, w, h) box on img with color (RGBA tuple)."""
    d = ImageDraw.Draw(img)
    d.rectangle([x, y, x + w - 1, y + h - 1], fill=color)


def draw_slot(img, x, y):
    """Draw a vanilla-style 18x18 slot at (x, y) (slot interior is 16x16)."""
    # Outer 1px dark border (18x18 total)
    fill_box(img, x, y, 18, 18, SLOT_OUTER)
    # Inner light 1px highlight (16x16)
    fill_box(img, x + 1, y + 1, 16, 16, SLOT_INNER)
    # Interior 14x14 darker
    fill_box(img, x + 2, y + 2, 14, 14, SLOT_BG)


def make_gui_texture():
    """Build the 200x190 GUI texture: dark bg + 7 slots + flame + arrow."""
    W, H = 200, 190
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))

    # 1. Main 176x166 dark-stone background.
    fill_box(img, 0, 0, 176, 166, DARK_STONE)

    # Subtle horizontal seams (every 4px) for "stone" texture.
    for y in range(4, 166, 8):
        for x in range(0, 176, 1):
            # Alternate light/dark based on x parity and y position
            if (x + y) % 11 == 0:
                img.putpixel((x, y), SEAM_LIGHT)
            elif (x * y) % 7 == 0:
                img.putpixel((x, y), SEAM_DARK)

    # 2. Player inventory slot borders (drawn as a 9x3 grid).
    PX, PY = 8, 84  # player inventory top-left
    for row in range(3):
        for col in range(9):
            draw_slot(img, PX + col * 18, PY + row * 18)

    # 3. Player hotbar slots (9x1 grid).
    HY = 142
    for col in range(9):
        draw_slot(img, PX + col * 18, HY)

    # 4. Furnace slots:
    # 5 ingredients — 3 top, 2 bottom (offset half a slot)
    draw_slot(img, 44, 17)  # ing 1
    draw_slot(img, 62, 17)  # ing 2
    draw_slot(img, 80, 17)  # ing 3
    draw_slot(img, 53, 35)  # ing 4
    draw_slot(img, 71, 35)  # ing 5
    # Fuel slot
    draw_slot(img, 8, 35)
    # Output slot
    draw_slot(img, 134, 35)

    # 5. Flame icon (14x14 at x=176, y=0)
    # Draw an upside-down teardrop: orange outside, yellow inside.
    fx, fy, fw, fh = 176, 0, 14, 14
    for py in range(fh):
        for px in range(fw):
            # Distance from center (7, 9) (slightly lower for teardrop)
            cx, cy = 7, 9
            dx = (px - cx) / 6.0
            dy = (py - cy) / 7.0
            # Teardrop: narrower at top, wider at bottom (flame flicker)
            # Add a slight sine curve for flame shape
            dist_sq = dx * dx + (dy + (1 - py / fh) * 0.5) ** 2
            if dist_sq > 1.0:
                continue  # transparent outside flame
            # Color: inner yellow, outer orange, edge dark-orange
            if dist_sq < 0.35:
                img.putpixel((fx + px, fy + py), FLAME_CORE)
            elif dist_sq < 0.65:
                img.putpixel((fx + px, fy + py), FLAME_MID)
            else:
                img.putpixel((fx + px, fy + py), FLAME_EDGE)

    # 6. Progress arrow (24x17 at x=176, y=14)
    # Draw a horizontal right-pointing arrow: rectangle base + triangle tip.
    ax, ay, aw, ah = 176, 14, 24, 17
    # Base rectangle: x=[0..13], y=[4..12] (8x9 base)
    for py in range(ah):
        for px in range(aw):
            # Right-pointing arrow with triangular tip
            in_base = (4 <= py <= 12) and (0 <= px <= 13)
            # Triangle tip: from px=14 to px=23, narrowing toward tip
            # Tip at (23, 8), base at (14, 0..16) — width 17 narrowing to 0
            if 14 <= px <= 23:
                # width at this x = (24 - px) * 17 / 10  (linear taper)
                taper = (24 - px) / 10.0
                half_width = taper * 8.5  # max half-width at x=14
                if abs(py - 8) <= half_width:
                    in_base = True
            if in_base:
                # Outline (border) on the very edge of the arrow shape
                edge = False
                # Check 4-neighborhood — if any is NOT part of the arrow, mark as edge
                for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                    nx, ny = px + dx, py + dy
                    if not (4 <= ny <= 12 and 0 <= nx <= 13):
                        if not (14 <= nx <= 23):
                            edge = True
                            break
                        # Check triangle
                        if 14 <= nx <= 23:
                            taper = (24 - nx) / 10.0
                            hw = taper * 8.5
                            if abs(ny - 8) > hw:
                                edge = True
                                break
                img.putpixel((ax + px, ay + py),
                             ARROW_BORDER if edge else ARROW_COLOR)

    # Save
    out_path = os.path.join(GUI_DIR, "alchemy_furnace.png")
    img.save(out_path)
    print(f"Wrote {out_path} ({W}x{H})")


def make_block_top():
    """16x16 top face: dark metal with a circular cauldron opening."""
    img = Image.new("RGBA", (16, 16), BLOCK_METAL_MID)
    d = ImageDraw.Draw(img)

    # 1. Subtle metal plate texture: 4x4 quadrants with slight variation
    for y in range(16):
        for x in range(16):
            base = BLOCK_METAL_MID
            # Add some noise based on position
            if (x + y) % 5 == 0:
                base = BLOCK_METAL_DARK
            elif (x * y) % 7 == 0:
                base = BLOCK_METAL_LIGHT
            img.putpixel((x, y), base)

    # 2. Rivets in 4 corners (2x2 darker squares)
    for cx, cy in [(2, 2), (12, 2), (2, 12), (12, 12)]:
        for dy in range(2):
            for dx in range(2):
                img.putpixel((cx + dx, cy + dy), BLOCK_RIVET)

    # 3. Cauldron opening: dark circle in the middle (radius ~5)
    cx, cy = 7.5, 7.5
    for y in range(16):
        for x in range(16):
            dx = x - cx
            dy = y - cy
            dist = (dx * dx + dy * dy) ** 0.5
            if dist <= 4.5:
                img.putpixel((x, y), BLOCK_CAULDRON_DARK)
            elif dist <= 5.5:
                img.putpixel((x, y), BLOCK_CAULDRON_LIGHT)

    out_path = os.path.join(BLOCK_DIR, "alchemy_furnace_top.png")
    img.save(out_path)
    print(f"Wrote {out_path} (16x16)")


def make_block_front():
    """16x16 front face: furnace front with fire glow (rectangular opening)."""
    img = Image.new("RGBA", (16, 16), BLOCK_METAL_MID)
    d = ImageDraw.Draw(img)

    # 1. Base metal texture
    for y in range(16):
        for x in range(16):
            base = BLOCK_METAL_MID
            if (x + y) % 5 == 0:
                base = BLOCK_METAL_DARK
            elif (x * y) % 7 == 0:
                base = BLOCK_METAL_LIGHT
            img.putpixel((x, y), base)

    # 2. Rivets in 4 corners
    for cx, cy in [(2, 2), (12, 2), (2, 12), (12, 12)]:
        for dy in range(2):
            for dx in range(2):
                img.putpixel((cx + dx, cy + dy), BLOCK_RIVET)

    # 3. Fire opening: rectangular window in the middle-bottom (4x4 at x=6..9, y=8..11)
    # Border (darker), interior (fire gradient)
    fx, fy, fw, fh = 5, 8, 6, 5
    # Border
    for py in range(fy, fy + fh):
        for px in range(fx, fx + fw):
            on_edge = (px == fx or px == fx + fw - 1
                       or py == fy or py == fy + fh - 1)
            if on_edge:
                img.putpixel((px, py), BLOCK_CAULDRON_DARK)
    # Fire interior — radial gradient from center (hot core) to edge (dark fire)
    fcx, fcy = fx + fw / 2.0, fy + fh / 2.0
    for py in range(fy + 1, fy + fh - 1):
        for px in range(fx + 1, fx + fw - 1):
            dx = px - fcx
            dy = py - fcy
            # Normalize by half-extents
            nx = dx / (fw / 2.0)
            ny = dy / (fh / 2.0)
            dist = (nx * nx + ny * ny) ** 0.5
            if dist < 0.4:
                img.putpixel((px, py), BLOCK_FIRE_CORE)
            elif dist < 0.8:
                img.putpixel((px, py), BLOCK_FIRE_MID)
            else:
                img.putpixel((px, py), BLOCK_FIRE_EDGE)

    out_path = os.path.join(BLOCK_DIR, "alchemy_furnace_front.png")
    img.save(out_path)
    print(f"Wrote {out_path} (16x16)")


def make_block_side():
    """16x16 side face: plain metal side with rivets."""
    img = Image.new("RGBA", (16, 16), BLOCK_METAL_MID)
    d = ImageDraw.Draw(img)

    # Base metal texture
    for y in range(16):
        for x in range(16):
            base = BLOCK_METAL_MID
            if (x + y) % 5 == 0:
                base = BLOCK_METAL_DARK
            elif (x * y) % 7 == 0:
                base = BLOCK_METAL_LIGHT
            img.putpixel((x, y), base)

    # Rivets in 4 corners
    for cx, cy in [(2, 2), (12, 2), (2, 12), (12, 12)]:
        for dy in range(2):
            for dx in range(2):
                img.putpixel((cx + dx, cy + dy), BLOCK_RIVET)

    # A vertical seam in the middle for visual interest
    for y in range(2, 14):
        img.putpixel((8, y), BLOCK_METAL_DARK)

    out_path = os.path.join(BLOCK_DIR, "alchemy_furnace_side.png")
    img.save(out_path)
    print(f"Wrote {out_path} (16x16)")


if __name__ == "__main__":
    make_gui_texture()
    make_block_top()
    make_block_front()
    make_block_side()
    print("All textures generated.")
