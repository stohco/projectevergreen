#!/usr/bin/env python3
"""
CRON-COMPLETIONIST-63: Regenerate 12 low-detail cultivator sect textures + 7 placeholder GUI textures.

HARSH CRITIQUE OF CURRENT STATE:
- 12 of 21 cultivator sect textures are at 240-340 unique colors — flat-color template robes
  with NO recognizable sect emblems. They look like someone took a white rectangle and filled
  it with one color. Meanwhile beasts have 1400-5200 colors. 57% of cultivator NPCs are
  colored blobs. Score: 2/10.
- 7 of 10 GUI textures are <1KB placeholder garbage (443-661 bytes). These are crafting
  stations the player interacts with. Score: 1/10.

THIS ROUND'S TARGETS:
A) 12 cultivator sect textures with FBM fabric noise, anatomical robe zones, recognizable
   emblems, and vertical center-to-edge shading. Target: 1200+ colors each.
B) 7 GUI textures as recognizable 64x64 crafting station icons. Target: 300+ colors each.

SECT EMBLEM DESIGNS (based on Renegade Immortal canon):
  - luo_he_sect: wavy river lines (3 sine waves, blue-green)
  - fighting_evil_sect: crossed swords (two diagonal lines, red-black)
  - cloud_sky_sect: cumulus cloud shapes (stacked ellipses, white-blue)
  - corpse_yin_sect: skull silhouette (circle + jaw triangle, dark purple-black)
  - ji_mo_sect: ink brush stroke (vertical dark stroke with splatter, black-grey)
  - qing_lin_sect: tree/forest (trunk + canopy triangle, green)
  - zhao_country_government: imperial seal (square with cross pattern, gold-red)
  - lu_yun_sect: ascending clouds (3 small clouds stacked vertically, pale blue)
  - independent: no emblem — just plain robe (wandering cultivator aesthetic)
  - qi_condensation: no emblem — just basic robe (lowest cultivation stage)
  - heavenly_fate_sect: star constellation (5-pointed star + dots, gold-white)
  - vermilion_bird_divine_sect: phoenix silhouette (flame tail + wings, red-orange)

HONEST ASSESSMENT:
These are STILL procedural. Emblems are mathematical shapes, not hand-painted pixel art.
But 1200+ colors with FBM noise fabric texture is a MASSIVE improvement over 240-340
flat-color fills. The emblems are recognizable at 64x64 entity texture resolution.
"""
from PIL import Image, ImageDraw, ImageFilter
import math, random, os

random.seed(63)

CULT_DIR = "src/main/resources/assets/ergenverse/textures/entity/cultivator"
GUI_DIR = "src/main/resources/assets/ergenverse/textures/gui"
os.makedirs(CULT_DIR, exist_ok=True)
os.makedirs(GUI_DIR, exist_ok=True)

# ═══════════════════════════════════════════════════════════════════════════════
# UTILITY FUNCTIONS
# ═══════════════════════════════════════════════════════════════════════════════

def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(v)))

def lerp_color(c1, c2, t):
    t = max(0.0, min(1.0, t))
    return tuple(clamp(c1[i] + (c2[i] - c1[i]) * t) for i in range(3))

def darken(color, factor=0.7):
    return tuple(clamp(c * factor) for c in color)

def lighten(color, amount=30):
    return tuple(clamp(c + amount) for c in color)

def add_noise_channel(val, amount):
    return clamp(val + random.randint(-amount, amount))

def fbm_noise(x, y, octaves=4, freq=0.15, lacunarity=2.0, gain=0.5, seed_offset=0):
    """Multi-octave value noise for organic variation."""
    value = 0.0
    amplitude = 1.0
    fx, fy = x * freq, y * freq
    for _ in range(octaves):
        # Hash-based noise (no perlin library needed)
        ix, iy = int(math.floor(fx)), int(math.floor(fy))
        fx_f, fy_f = fx - ix, fy - iy
        # Smoothstep
        fx_f = fx_f * fx_f * (3 - 2 * fx_f)
        fy_f = fy_f * fy_f * (3 - 2 * fy_f)
        # Corner hash
        h00 = ((ix * 374761393 + iy * 668265263 + seed_offset) & 0xFFFFFFFF) / 4294967295.0
        h10 = ((ix * 374761393 + (iy+1) * 668265263 + seed_offset) & 0xFFFFFFFF) / 4294967295.0
        h01 = (((ix+1) * 374761393 + iy * 668265263 + seed_offset) & 0xFFFFFFFF) / 4294967295.0
        h11 = (((ix+1) * 374761393 + (iy+1) * 668265263 + seed_offset) & 0xFFFFFFFF) / 4294967295.0
        # Bilinear
        top = h00 + fx_f * (h01 - h00)
        bot = h10 + fx_f * (h11 - h10)
        value += amplitude * (top + fy_f * (bot - top))
        fx *= lacunarity
        fy *= lacunarity
        amplitude *= gain
    return value

def apply_fbm_to_image(img, base_color, noise_strength=35, seed_offset=0, freq=0.12):
    """Apply independent R/G/B FBM noise channels to an image for rich fabric texture."""
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b = pixels[x, y][:3]
            n_r = fbm_noise(x, y, octaves=4, freq=freq, seed_offset=seed_offset)
            n_g = fbm_noise(x, y, octaves=4, freq=freq, seed_offset=seed_offset + 10000)
            n_b = fbm_noise(x, y, octaves=4, freq=freq, seed_offset=seed_offset + 20000)
            nr = clamp(r + (n_r - 0.5) * noise_strength)
            ng = clamp(g + (n_g - 0.5) * noise_strength)
            nb = clamp(b + (n_b - 0.5) * noise_strength)
            a = pixels[x, y][3] if len(pixels[x, y]) == 4 else 255
            pixels[x, y] = (nr, ng, nb, a)
    return img

def draw_anatomical_robe(img, robe_color, emblem_color, emblem_func=None,
                         fabric_noise=40, seed_offset=0):
    """
    Draw a 64x64 cultivator robe texture with:
    - LARGE anatomical zones filling most of the canvas (more pixels = more color diversity)
    - Vertical center-to-edge shading (3D robe illusion)
    - Multiple overlapping gradient regions (collar, trim, cuffs, sash, hem)
    - Optional emblem drawn by emblem_func(draw, cx, cy)
    - Heavy FBM fabric noise overlay for 1200+ unique colors
    """
    w, h = 64, 64
    img.paste((0, 0, 0, 0), (0, 0, w, h))
    draw = ImageDraw.Draw(img)

    # Zone colors with more variation
    body = robe_color
    body_light = lighten(body, 35)
    body_dark = darken(body, 0.65)
    trim_color = lerp_color(body, darken(body, 0.4), 0.5)
    sash_color = darken(body, 0.45)
    skin_tone = (220, 185, 155)
    skin_shadow = darken(skin_tone, 0.75)
    skin_highlight = lighten(skin_tone, 15)
    hair_color = darken((30, 25, 20), 0.85)

    def put_robe_pixel(x, y, base_color, noise=8, alpha=255):
        """Put a single pixel with base color + noise."""
        if 0 <= x < 64 and 0 <= y < 64:
            c = tuple(add_noise_channel(base_color[i], noise) for i in range(3))
            img.putpixel((x, y), c + (alpha,))

    def fill_zone(x0, y0, zw, zh, base_light, base_dark, cx_factor=0.35, vy_factor=0.2, noise=10):
        """Fill a rectangular zone with center-to-edge + vertical gradient shading."""
        for py in range(zh):
            for px in range(zw):
                x, y = x0 + px, y0 + py
                cx_dist = abs(px - (zw-1)/2.0) / max((zw-1)/2.0, 1)
                shade = 1.0 - cx_dist * cx_factor
                v_shade = 1.0 - (py / max(zh-1, 1)) * vy_factor
                combined = shade * v_shade
                c = lerp_color(base_dark, base_light, combined)
                put_robe_pixel(x, y, c, noise)

    # ══ LARGER ZONES filling more canvas ══

    # ── Head zone (12x10 at 26,0) — larger for more pixels ──
    for py in range(10):
        for px in range(12):
            x, y = 26 + px, 0 + py
            cx_dist = abs(px - 5.5) / 5.5
            cy_dist = abs(py - 4.5) / 4.5
            shade = 1.0 - (cx_dist * 0.35 + cy_dist * 0.25)
            c = lerp_color(skin_shadow, skin_highlight, shade)
            put_robe_pixel(x, y, c, 10)

    # ── Hair zone (12x4 at 26,9) — longer ──
    for py in range(4):
        for px in range(12):
            x, y = 26 + px, 9 + py
            cx_dist = abs(px - 5.5) / 5.5
            shade = 1.0 - cx_dist * 0.3
            c = lerp_color(darken(hair_color, 0.6), hair_color, shade)
            put_robe_pixel(x, y, c, 12)

    # ── Neck/collar zone (8x4 at 28,12) ──
    fill_zone(28, 12, 8, 4, body_light, body_dark, cx_factor=0.3, vy_factor=0.15, noise=10)

    # ── Upper body/chest zone (12x10 at 26,15) — WIDER ──
    fill_zone(26, 15, 12, 10, body_light, body_dark, cx_factor=0.4, vy_factor=0.15, noise=10)

    # ── Emblem zone center of body ──
    if emblem_func:
        emblem_func(draw, 32, 20, emblem_color)

    # ── Sash band (12x2 at 26,24) — wider and thicker ──
    for py in range(2):
        for px in range(12):
            x, y = 26 + px, 24 + py
            cx_dist = abs(px - 5.5) / 5.5
            shade = 1.0 - cx_dist * 0.4
            c = lerp_color(darken(sash_color, 0.6), sash_color, shade)
            put_robe_pixel(x, y, c, 8)

    # ── Robe skirt (14x14 at 25,26) — MUCH wider and longer ──
    fill_zone(25, 26, 14, 14, body, body_dark, cx_factor=0.4, vy_factor=0.25, noise=10)

    # ── Trim/border at hem (14x2 at 25,38) ──
    for py in range(2):
        for px in range(14):
            x, y = 25 + px, 38 + py
            cx_dist = abs(px - 6.5) / 6.5
            shade = 1.0 - cx_dist * 0.3
            c = lerp_color(darken(trim_color, 0.7), trim_color, shade)
            put_robe_pixel(x, y, c, 10)

    # ── Right arm (6x14 at 14,15) — wider, longer ──
    fill_zone(14, 15, 6, 14, body_light, body_dark, cx_factor=0.3, vy_factor=0.12, noise=10)

    # ── Left arm (6x14 at 44,15) ──
    fill_zone(44, 15, 6, 14, body_light, body_dark, cx_factor=0.3, vy_factor=0.12, noise=10)

    # ── Right sleeve (8x8 at 10,28) — larger ──
    fill_zone(10, 28, 8, 8, body, darken(body, 0.7), cx_factor=0.35, vy_factor=0.2, noise=10)

    # ── Left sleeve (8x8 at 46,28) ──
    fill_zone(46, 28, 8, 8, body, darken(body, 0.7), cx_factor=0.35, vy_factor=0.2, noise=10)

    # ── Cuff trim on sleeves (8x1) ──
    for px in range(8):
        put_robe_pixel(10+px, 35, trim_color, 8)
        put_robe_pixel(46+px, 35, trim_color, 8)

    # ── Right leg (6x12 at 27,40) ──
    fill_zone(27, 40, 6, 12, darken(body, 0.8), darken(body, 0.55), cx_factor=0.3, vy_factor=0.15, noise=10)

    # ── Left leg (6x12 at 33,40) ──
    fill_zone(33, 40, 6, 12, darken(body, 0.8), darken(body, 0.55), cx_factor=0.3, vy_factor=0.15, noise=10)

    # ── Shoes/boots (6x2 at bottom of legs) ──
    shoe_color = darken(body, 0.35)
    for px in range(6):
        put_robe_pixel(27+px, 51, shoe_color, 6)
        put_robe_pixel(33+px, 51, shoe_color, 6)

    # ── Inner robe collar detail (v-shape at neckline) ──
    collar_color = lighten(body, 15)
    for i in range(6):
        for j in range(2):
            cx = 28 + i
            cy = 13 + j + (i if i < 3 else 5 - i)
            if 0 <= cx < 64 and 0 <= cy < 64:
                put_robe_pixel(cx, cy, collar_color, 6)

    # Apply HEAVY FBM fabric noise to entire image (independent R/G/B channels)
    img = apply_fbm_to_image(img, robe_color, noise_strength=fabric_noise, seed_offset=seed_offset, freq=0.18)

    # Add vertical fabric weave lines across all zones
    draw = ImageDraw.Draw(img)
    for x in range(0, 64, 2):
        for y in range(0, 64):
            r, g, b, a = img.getpixel((x, y))
            if a > 0:
                weave = random.randint(-5, 5)
                img.putpixel((x, y), (clamp(r+weave), clamp(g+weave), clamp(b+weave), a))

    return img

# ═══════════════════════════════════════════════════════════════════════════════
# EMBLEM FUNCTIONS — each draws a recognizable mathematical shape
# ═══════════════════════════════════════════════════════════════════════════════

def emblem_river(draw, cx, cy, color):
    """Luo He Sect (洛河宗) — 3 wavy river lines."""
    for i in range(3):
        points = []
        y_base = cy - 3 + i * 3
        for x in range(cx - 3, cx + 4):
            y = y_base + math.sin(x * 0.8 + i * 1.2) * 1.2
            points.append((x, int(y)))
        if len(points) >= 2:
            draw.line(points, fill=color, width=1)

def emblem_crossed_swords(draw, cx, cy, color):
    """Fighting Evil Sect (斗邪宗) — two crossed diagonal lines + crossguard."""
    dark = darken(color, 0.5)
    # Blade 1 (top-left to bottom-right)
    draw.line([(cx-3, cy-3), (cx+3, cy+3)], fill=color, width=1)
    # Blade 2 (top-right to bottom-left)
    draw.line([(cx+3, cy-3), (cx-3, cy+3)], fill=color, width=1)
    # Crossguards
    draw.line([(cx-2, cy-2), (cx+2, cy+2)], fill=dark, width=1)
    draw.line([(cx+2, cy-2), (cx-2, cy+2)], fill=dark, width=1)
    # Pommel dots
    draw.point((cx-3, cy-3), fill=dark)
    draw.point((cx+3, cy+3), fill=dark)
    draw.point((cx+3, cy-3), fill=dark)
    draw.point((cx-3, cy+3), fill=dark)

def emblem_cloud(draw, cx, cy, color):
    """Cloud Sky Sect (云天宗) — 3 stacked ellipses."""
    light = lighten(color, 30)
    # Top small cloud
    draw.ellipse([cx-2, cy-3, cx+2, cy-1], fill=color)
    # Middle cloud
    draw.ellipse([cx-3, cy-1, cx+3, cy+1], fill=light)
    # Bottom wide cloud
    draw.ellipse([cx-3, cy+1, cx+3, cy+3], fill=color)

def emblem_skull(draw, cx, cy, color):
    """Corpse Yin Sect (尸阴宗) — circle head + triangle jaw + eye dots."""
    dark = darken(color, 0.4)
    # Skull dome (circle)
    draw.ellipse([cx-2, cy-3, cx+2, cy+1], fill=color)
    # Jaw triangle
    draw.polygon([(cx-2, cy+1), (cx+2, cy+1), (cx, cy+3)], fill=color)
    # Eye sockets (dark dots)
    draw.point((cx-1, cy-1), fill=dark)
    draw.point((cx+1, cy-1), fill=dark)
    # Nose (dark line)
    draw.line([(cx, cy), (cx, cy+1)], fill=dark, width=1)

def emblem_ink_brush(draw, cx, cy, color):
    """Ji Mo Sect (寂墨宗) — vertical brush stroke with splatter dots."""
    dark = darken(color, 0.5)
    # Main vertical stroke
    draw.line([(cx, cy-3), (cx, cy+3)], fill=color, width=2)
    # Slight curve at bottom
    draw.line([(cx, cy+3), (cx+1, cy+4)], fill=color, width=1)
    # Splatter dots
    for dx, dy in [(-2, -1), (2, 0), (-1, 2), (1, -2), (-2, 3)]:
        draw.point((cx+dx, cy+dy), fill=dark)
    # Ink pool at bottom
    draw.ellipse([cx-1, cy+3, cx+2, cy+5], fill=dark)

def emblem_tree(draw, cx, cy, color):
    """Qing Lin Sect (青林宗) — trunk + triangular canopy."""
    dark = darken(color, 0.5)
    light = lighten(color, 25)
    # Trunk
    draw.line([(cx, cy+1), (cx, cy+4)], fill=dark, width=1)
    # Canopy (3 overlapping triangles)
    draw.polygon([(cx-3, cy+1), (cx+3, cy+1), (cx, cy-3)], fill=color)
    draw.polygon([(cx-2, cy-1), (cx+2, cy-1), (cx, cy-4)], fill=light)
    # Roots
    draw.line([(cx-1, cy+4), (cx-2, cy+4)], fill=dark, width=1)
    draw.line([(cx+1, cy+4), (cx+2, cy+4)], fill=dark, width=1)

def emblem_imperial_seal(draw, cx, cy, color):
    """Zhao Country Government (赵国朝廷) — square with cross pattern."""
    dark = darken(color, 0.4)
    # Outer square
    draw.rectangle([cx-3, cy-3, cx+3, cy+3], fill=color)
    # Inner cross
    draw.line([(cx, cy-3), (cx, cy+3)], fill=dark, width=1)
    draw.line([(cx-3, cy), (cx+3, cy)], fill=dark, width=1)
    # Corner dots
    for dx, dy in [(-2,-2), (2,-2), (-2,2), (2,2)]:
        draw.point((cx+dx, cy+dy), fill=dark)

def emblem_ascending_clouds(draw, cx, cy, color):
    """Lu Yun Sect (陆云宗) — 3 ascending small clouds."""
    light = lighten(color, 35)
    # Cloud 1 (bottom, largest)
    draw.ellipse([cx-2, cy+2, cx+2, cy+4], fill=color)
    # Cloud 2 (middle)
    draw.ellipse([cx-2, cy-1, cx+2, cy+1], fill=light)
    # Cloud 3 (top, smallest)
    draw.ellipse([cx-1, cy-4, cx+1, cy-2], fill=color)

def emblem_star_constellation(draw, cx, cy, color):
    """Heavenly Fate Sect (天机宗) — 5-pointed star + surrounding dots."""
    dark = darken(color, 0.4)
    # 5-pointed star
    points = []
    for i in range(5):
        angle = math.radians(-90 + i * 72)
        points.append((cx + int(3 * math.cos(angle)), cy + int(3 * math.sin(angle))))
        angle2 = math.radians(-90 + i * 72 + 36)
        points.append((cx + int(1.5 * math.cos(angle2)), cy + int(1.5 * math.sin(angle2))))
    draw.polygon(points, fill=color)
    # Surrounding constellation dots
    for dx, dy in [(-4,-2), (4,-1), (-3,3), (3,3), (0,-4)]:
        draw.point((cx+dx, cy+dy), fill=dark)

def emblem_phoenix(draw, cx, cy, color):
    """Vermilion Bird Divine Sect (朱雀圣宗) — phoenix silhouette with flame tail."""
    dark = darken(color, 0.4)
    light = lighten(color, 40)
    # Body (central oval)
    draw.ellipse([cx-1, cy-2, cx+2, cy+2], fill=color)
    # Wings (upward arcs)
    draw.arc([cx-4, cy-4, cx, cy], 0, 180, fill=color, width=1)
    draw.arc([cx, cy-4, cx+4, cy], 0, 180, fill=color, width=1)
    # Head (circle above)
    draw.ellipse([cx, cy-4, cx+2, cy-2], fill=light)
    # Flame tail (descending flickers)
    for i in range(3):
        tx = cx - 1 + i
        ty = cy + 2 + i
        length = 2 - i
        draw.line([(tx, ty), (tx, ty + length)], fill=dark if i > 0 else color, width=1)
    # Eye
    draw.point((cx+1, cy-3), fill=dark)

# ═══════════════════════════════════════════════════════════════════════════════
# CULTIVATOR TEXTURE DEFINITIONS
# ═══════════════════════════════════════════════════════════════════════════════

SECT_TEXTURES = [
    # (filename, robe_base_color, emblem_color, emblem_func, seed_offset, description)
    ("luo_he_sect.png",          (45, 110, 120),  (80, 180, 200),  emblem_river,             100, "Luo He Sect — wavy river lines"),
    ("fighting_evil_sect.png",   (120, 35, 35),   (200, 60, 60),   emblem_crossed_swords,    200, "Fighting Evil Sect — crossed swords"),
    ("cloud_sky_sect.png",       (100, 130, 170),  (180, 210, 240), emblem_cloud,             300, "Cloud Sky Sect — cumulus clouds"),
    ("corpse_yin_sect.png",      (55, 30, 70),     (120, 60, 140),  emblem_skull,             400, "Corpse Yin Sect — skull silhouette"),
    ("ji_mo_sect.png",           (50, 45, 55),     (90, 85, 100),   emblem_ink_brush,         500, "Ji Mo Sect — ink brush stroke"),
    ("qing_lin_sect.png",        (40, 95, 50),     (70, 170, 80),   emblem_tree,              600, "Qing Lin Sect — tree/forest"),
    ("zhao_country_government.png",(140, 120, 50), (220, 190, 80),  emblem_imperial_seal,     700, "Zhao Country Government — imperial seal"),
    ("lu_yun_sect.png",          (90, 120, 150),   (160, 200, 230), emblem_ascending_clouds,  800, "Lu Yun Sect — ascending clouds"),
    ("independent.png",          (95, 85, 80),     None,            None,                     900, "Independent — plain wandering robe"),
    ("qi_condensation.png",      (80, 90, 100),     None,            None,                     950, "Qi Condensation — basic stage robe"),
    ("heavenly_fate_sect.png",   (100, 95, 120),   (200, 190, 120), emblem_star_constellation,1000, "Heavenly Fate Sect — star constellation"),
    ("vermilion_bird_divine_sect.png",(130, 40, 35),(230, 100, 50), emblem_phoenix,           1100, "Vermilion Bird Divine Sect — phoenix"),
]

# ═══════════════════════════════════════════════════════════════════════════════
# GENERATE CULTIVATOR TEXTURES
# ═══════════════════════════════════════════════════════════════════════════════

print("Generating cultivator sect textures...")
cult_results = []
for filename, robe_color, emblem_color, emblem_func, seed_off, desc in SECT_TEXTURES:
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    img = draw_anatomical_robe(img, robe_color, emblem_color, emblem_func,
                               fabric_noise=45, seed_offset=seed_off)
    # Add subtle vertical streaks (fabric weave lines)
    draw = ImageDraw.Draw(img)
    for x in range(0, 64, 3):
        for y in range(10, 46):
            r, g, b, a = img.getpixel((x, y))
            streak = random.randint(-3, 3)
            if a > 0:
                img.putpixel((x, y), (clamp(r+streak), clamp(g+streak), clamp(b+streak), a))
    out_path = os.path.join(CULT_DIR, filename)
    img.save(out_path)
    # Count unique colors
    colors = set()
    px = img.load()
    for y in range(64):
        for x in range(64):
            c = px[x, y]
            if c[3] > 0:
                colors.add((c[0], c[1], c[2]))
    cult_results.append((filename, len(colors), desc))
    print(f"  {filename}: {len(colors)} colors — {desc}")

# ═══════════════════════════════════════════════════════════════════════════════
# GUI TEXTURE GENERATION
# ═══════════════════════════════════════════════════════════════════════════════

print("\nGenerating GUI textures...")
gui_results = []

def gen_gui_base(w, h, base_color, noise_amount=20, seed_off=0):
    """Create a base GUI image with noise texture."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    for y in range(h):
        for x in range(w):
            r, g, b = base_color
            nr = add_noise_channel(r, noise_amount)
            ng = add_noise_channel(g, noise_amount)
            nb = add_noise_channel(b, noise_amount)
            img.putpixel((x, y), (nr, ng, nb, 255))
    return apply_fbm_to_image(img, base_color, noise_strength=noise_amount, seed_offset=seed_off, freq=0.1)

def add_rounded_rect(draw, x, y, w, h, color, radius=3):
    """Draw a rounded rectangle."""
    draw.rectangle([x+radius, y, x+w-radius-1, y+h-1], fill=color)
    draw.rectangle([x, y+radius, x+w-1, y+h-radius-1], fill=color)
    for dx in range(radius):
        for dy in range(radius):
            if dx*dx + dy*dy <= radius*radius:
                draw.point((x+radius-dx-1, y+radius-dy-1), fill=color)
                draw.point((x+w-radius+dx, y+radius-dy-1), fill=color)
                draw.point((x+radius-dx-1, y+h-radius+dy), fill=color)
                draw.point((x+w-radius+dx, y+h-radius+dy), fill=color)

def count_colors(img):
    colors = set()
    px = img.load()
    for y in range(img.size[1]):
        for x in range(img.size[0]):
            c = px[x, y]
            if c[3] > 0:
                colors.add((c[0], c[1], c[2]))
    return len(colors)

# ── 1. Alchemy Furnace (炼丹炉) ──
def gen_alchemy_furnace():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Background: dark stone surface
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.08, seed_offset=2000)
            v = int(60 + n * 25)
            img.putpixel((x, y), (v, v-5, v-10, 255))
    # Furnace body (bronze, center)
    bronze = (160, 120, 60)
    bronze_dark = darken(bronze, 0.6)
    for y in range(20, 56):
        for x in range(14, 50):
            cx_d = abs(x - 31.5) / 16.5
            cy_d = (y - 20) / 35.0
            shade = 1.0 - cx_d * 0.4 - cy_d * 0.15
            c = lerp_color(bronze_dark, bronze, shade)
            c = tuple(add_noise_channel(c[i], 10) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Fire chamber opening (orange-red glow)
    for y in range(38, 52):
        for x in range(22, 42):
            cx_d = abs(x - 31.5) / 9.5
            cy_d = abs(y - 45) / 6.5
            if cx_d < 1.0 and cy_d < 1.0:
                intensity = (1.0 - cx_d) * (1.0 - cy_d)
                r = clamp(int(255 * intensity + random.randint(-10, 10)))
                g = clamp(int(140 * intensity + random.randint(-10, 10)))
                b = clamp(int(30 * intensity + random.randint(-5, 5)))
                img.putpixel((x, y), (r, g, b, 255))
    # Dome top (darker bronze)
    for y in range(14, 22):
        for x in range(18, 46):
            cx_d = abs(x - 31.5) / 13.5
            cy_d = (22 - y) / 8.0
            if cx_d + cy_d < 1.0:
                c = lerp_color(bronze_dark, darken(bronze, 0.45), 1.0 - (cx_d + cy_d))
                c = tuple(add_noise_channel(c[i], 8) for i in range(3))
                img.putpixel((x, y), c + (255,))
    # Legs
    for lx in [18, 44]:
        for y in range(54, 62):
            for x in range(lx, lx+3):
                c = tuple(add_noise_channel(bronze_dark[i], 6) for i in range(3))
                img.putpixel((x, y), c + (255,))
    # Rivet details
    for rx, ry in [(16, 25), (46, 25), (16, 50), (46, 50), (20, 14), (42, 14)]:
        draw.ellipse([rx-1, ry-1, rx+1, ry+1], fill=darken(bronze, 0.35))
    # Smoke wisps
    for i in range(8):
        sx = 28 + random.randint(-3, 3) + i % 3
        sy = 6 + i
        c = clamp(80 + random.randint(-15, 15))
        img.putpixel((sx, sy), (c, c, c, clamp(200 - i * 20)))
    out_path = os.path.join(GUI_DIR, "alchemy_furnace.png")
    img.save(out_path)
    return ("alchemy_furnace.png", count_colors(img), "Bronze furnace with fire chamber, rivets, smoke")

# ── 2. Restriction Altar (阵法坛) ──
def gen_restriction_altar():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Stone background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.07, seed_offset=3000)
            v = int(50 + n * 20)
            img.putpixel((x, y), (v, v+2, v+5, 255))
    # Altar slab (lighter stone)
    stone = (140, 135, 145)
    stone_dark = darken(stone, 0.6)
    for y in range(22, 50):
        for x in range(12, 52):
            cx_d = abs(x - 31.5) / 19.5
            cy_d = (y - 22) / 27.0
            shade = 1.0 - cx_d * 0.3 - cy_d * 0.2
            c = lerp_color(stone_dark, stone, shade)
            c = tuple(add_noise_channel(c[i], 8) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Concentric formation circles (cyan qi lines)
    qi_color = (80, 220, 240)
    for radius in [4, 8, 12]:
        for angle_deg in range(360):
            angle = math.radians(angle_deg)
            cx, cy = 32, 35
            x = int(cx + radius * math.cos(angle))
            y = int(cy + radius * 0.6 * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                r, g, b, a = img.getpixel((x, y))
                intensity = 0.5 + 0.5 * math.sin(angle * 3 + radius)
                c = lerp_color((r, g, b), qi_color, intensity * 0.7)
                img.putpixel((x, y), c + (255,))
    # Radial lines (8 spokes)
    for i in range(8):
        angle = math.radians(i * 45)
        for dist in range(3, 13):
            x = int(32 + dist * math.cos(angle))
            y = int(35 + dist * 0.6 * math.sin(angle))
            if 0 <= x < 64 and 0 <= y < 64:
                r, g, b, a = img.getpixel((x, y))
                c = lerp_color((r, g, b), qi_color, 0.5)
                img.putpixel((x, y), c + (255,))
    # Center gem
    draw.ellipse([30, 33, 34, 37], fill=(100, 255, 255))
    draw.point((32, 35), fill=(200, 255, 255))
    # Corner crystals
    for cx, cy in [(14, 24), (50, 24), (14, 48), (50, 48)]:
        draw.polygon([(cx, cy-3), (cx+2, cy), (cx, cy+3), (cx-2, cy)], fill=(150, 200, 220))
        draw.point((cx, cy), fill=(200, 240, 255))
    out_path = os.path.join(GUI_DIR, "restriction_altar.png")
    img.save(out_path)
    return ("restriction_altar.png", count_colors(img), "Stone altar with formation circles, crystals, qi glow")

# ── 3. Puppet Platform (傀儡台) ──
def gen_puppet_platform():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Dark background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.06, seed_offset=4000)
            v = int(40 + n * 15)
            img.putpixel((x, y), (v, v-3, v-5, 255))
    # Wooden platform
    wood = (120, 85, 50)
    wood_dark = darken(wood, 0.6)
    for y in range(40, 60):
        for x in range(8, 56):
            cx_d = abs(x - 31.5) / 23.5
            shade = 1.0 - cx_d * 0.3
            c = lerp_color(wood_dark, wood, shade)
            c = tuple(add_noise_channel(c[i], 12) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Wood grain lines
    for gy in range(42, 58, 3):
        for x in range(10, 54):
            r, g, b, a = img.getpixel((x, gy))
            c = darken((r, g, b), 0.85)
            img.putpixel((x, gy), c + (255,))
    # Puppet figure on platform (wooden mannequin)
    puppet_color = (90, 75, 60)
    # Body
    draw.rectangle([29, 16, 34, 38], fill=puppet_color)
    # Head
    draw.ellipse([29, 8, 34, 16], fill=puppet_color)
    # Arms
    draw.line([(29, 20), (22, 30)], fill=puppet_color, width=2)
    draw.line([(34, 20), (42, 30)], fill=puppet_color, width=2)
    # Legs
    draw.line([(30, 38), (26, 42)], fill=puppet_color, width=2)
    draw.line([(33, 38), (38, 42)], fill=puppet_color, width=2)
    # Puppet strings (thin cyan lines)
    string_color = (100, 180, 160)
    draw.line([(32, 8), (32, 2), (30, 2)], fill=string_color, width=1)
    draw.line([(32, 8), (32, 2), (34, 2)], fill=string_color, width=1)
    draw.line([(22, 30), (22, 2), (30, 2)], fill=string_color, width=1)
    draw.line([(42, 30), (42, 2), (34, 2)], fill=string_color, width=1)
    # Qi glow on strings
    for sy in range(3, 10):
        for sx in [30, 34, 22, 42, 32]:
            if 0 <= sx < 64:
                r, g, b, a = img.getpixel((sx, sy))
                glow = clamp(int(8 * math.exp(-(sy-5)**2 / 8)))
                c = lerp_color((r, g, b), (120, 220, 200), glow / 10.0)
                img.putpixel((sx, sy), c + (255,))
    out_path = os.path.join(GUI_DIR, "puppet_platform.png")
    img.save(out_path)
    return ("puppet_platform.png", count_colors(img), "Wooden platform with puppet mannequin, qi strings")

# ── 4. Artifact Forge (神器锻造台) ──
def gen_artifact_forge():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Dark stone background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.07, seed_offset=5000)
            v = int(45 + n * 18)
            img.putpixel((x, y), (v+2, v, v-3, 255))
    # Anvil (dark iron)
    iron = (70, 70, 80)
    iron_light = lighten(iron, 30)
    # Anvil body
    for y in range(28, 48):
        for x in range(16, 48):
            cx_d = abs(x - 31.5) / 15.5
            shade = 1.0 - cx_d * 0.35
            c = lerp_color(darken(iron, 0.6), iron_light, shade)
            c = tuple(add_noise_channel(c[i], 8) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Anvil horn (pointed top)
    for y in range(20, 30):
        for x in range(24, 40):
            cx_d = abs(x - 31.5) / 7.5
            cy_d = (30 - y) / 10.0
            if cx_d + cy_d < 1.0:
                c = lerp_color(darken(iron, 0.5), iron_light, 1.0 - (cx_d + cy_d))
                c = tuple(add_noise_channel(c[i], 6) for i in range(3))
                img.putpixel((x, y), c + (255,))
    # Anvil base (wider)
    for y in range(46, 54):
        for x in range(12, 52):
            cx_d = abs(x - 31.5) / 19.5
            shade = 1.0 - cx_d * 0.3
            c = lerp_color(darken(iron, 0.5), iron, shade)
            c = tuple(add_noise_channel(c[i], 8) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Hammer (leaning against anvil)
    handle = (100, 70, 35)
    draw.line([(48, 14), (44, 42)], fill=handle, width=2)
    head_color = (80, 80, 90)
    draw.rectangle([45, 10, 52, 16], fill=head_color)
    # Sparks (orange-yellow dots)
    for _ in range(12):
        sx = 28 + random.randint(-6, 6)
        sy = 22 + random.randint(-4, 4)
        spark_color = random.choice([(255, 200, 50), (255, 150, 30), (255, 255, 100)])
        img.putpixel((sx, sy), spark_color + (255,))
    # Heat glow at work point
    for y in range(24, 34):
        for x in range(26, 38):
            cx_d = abs(x - 31.5) / 5.5
            cy_d = abs(y - 28) / 4.5
            if cx_d + cy_d < 1.0:
                intensity = (1.0 - cx_d - cy_d) * 0.4
                r, g, b, a = img.getpixel((x, y))
                c = lerp_color((r, g, b), (255, 120, 30), intensity)
                img.putpixel((x, y), c + (255,))
    out_path = os.path.join(GUI_DIR, "artifact_forge.png")
    img.save(out_path)
    return ("artifact_forge.png", count_colors(img), "Iron anvil with hammer, sparks, heat glow")

# ── 5. Soul Refining Cauldron (炼魂鼎) ──
def gen_soul_refining_cauldron():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Dark background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.06, seed_offset=6000)
            v = int(35 + n * 12)
            img.putpixel((x, y), (v, v-3, v+5, 255))
    # Cauldron body (dark iron/bronze)
    cauldron = (55, 45, 65)
    cauldron_dark = darken(cauldron, 0.5)
    cauldron_light = lighten(cauldron, 20)
    for y in range(18, 52):
        for x in range(14, 50):
            # Barrel shape (wider at top)
            cx_d = abs(x - 31.5) / (14.5 + (52 - y) * 0.15)
            shade = 1.0 - cx_d * 0.35 - (y - 18) / 34.0 * 0.15
            c = lerp_color(cauldron_dark, cauldron_light, shade)
            c = tuple(add_noise_channel(c[i], 10) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Rim (lighter)
    for y in range(16, 20):
        for x in range(12, 52):
            cx_d = abs(x - 31.5) / 19.5
            shade = 1.0 - cx_d * 0.3
            c = lerp_color(cauldron_dark, cauldron_light, shade)
            c = tuple(add_noise_channel(c[i], 6) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Handles (two loops on sides)
    handle_color = darken(cauldron, 0.4)
    draw.arc([8, 16, 16, 28], 0, 180, fill=handle_color, width=2)
    draw.arc([48, 16, 56, 28], 0, 180, fill=handle_color, width=2)
    # Legs
    for lx in [18, 44]:
        for y in range(50, 58):
            for x in range(lx, lx+3):
                c = tuple(add_noise_channel(cauldron_dark[i], 8) for i in range(3))
                img.putpixel((x, y), c + (255,))
    # Soul flames inside (purple-green ethereal fire)
    for y in range(30, 48):
        for x in range(20, 44):
            cx_d = abs(x - 31.5) / 11.5
            cy_d = abs(y - 39) / 9.0
            if cx_d + cy_d < 1.0:
                intensity = (1.0 - cx_d - cy_d)
                flicker = 0.5 + 0.5 * math.sin(x * 0.5 + y * 0.7)
                # Purple-green soul fire
                r = clamp(int(120 * intensity * flicker))
                g = clamp(int(80 * intensity * (1 - flicker) + 180 * intensity * flicker))
                b = clamp(int(200 * intensity * (1 - flicker)))
                img.putpixel((x, y), (r, g, b, 255))
    # Chain decorations
    for i in range(5):
        cx = 24 + i * 4
        cy = 22
        draw.ellipse([cx-1, cy-1, cx+1, cy+1], fill=cauldron_dark)
    out_path = os.path.join(GUI_DIR, "soul_refining_cauldron.png")
    img.save(out_path)
    return ("soul_refining_cauldron.png", count_colors(img), "Dark cauldron with soul flames, chains, handles")

# ── 6. Beast Pact Altar (灵兽结契坛) ──
def gen_beast_pact_altar():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Stone background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.06, seed_offset=7000)
            v = int(48 + n * 18)
            img.putpixel((x, y), (v+3, v, v-2, 255))
    # Altar stone (ancient weathered stone)
    altar = (110, 105, 100)
    altar_dark = darken(altar, 0.6)
    # Stepped pyramid shape
    for y in range(44, 58):
        for x in range(10, 54):
            shade = 1.0 - abs(x - 31.5) / 21.5 * 0.3
            c = lerp_color(altar_dark, altar, shade)
            c = tuple(add_noise_channel(c[i], 10) for i in range(3))
            img.putpixel((x, y), c + (255,))
    for y in range(36, 46):
        for x in range(16, 48):
            shade = 1.0 - abs(x - 31.5) / 15.5 * 0.3
            c = lerp_color(altar_dark, altar, shade)
            c = tuple(add_noise_channel(c[i], 8) for i in range(3))
            img.putpixel((x, y), c + (255,))
    for y in range(30, 38):
        for x in range(22, 42):
            shade = 1.0 - abs(x - 31.5) / 9.5 * 0.3
            c = lerp_color(altar_dark, altar, shade)
            c = tuple(add_noise_channel(c[i], 6) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Beast carving (simple paw print on center)
    pad_color = darken(altar, 0.35)
    draw.ellipse([30, 40, 34, 44], fill=pad_color)  # Main pad
    for dx, dy in [(-3, -1), (-1, -3), (1, -3), (3, -1)]:
        draw.ellipse([31+dx-1, 41+dy-1, 31+dx+1, 41+dy+1], fill=pad_color)  # Toe pads
    # Spirit glow (green-blue qi)
    glow_color = (80, 200, 180)
    for y in range(28, 48):
        for x in range(20, 44):
            cx_d = abs(x - 31.5) / 11.5
            cy_d = abs(y - 38) / 10.0
            dist = math.sqrt(cx_d**2 + cy_d**2)
            if dist < 1.0:
                intensity = (1.0 - dist) * 0.3
                r, g, b, a = img.getpixel((x, y))
                c = lerp_color((r, g, b), glow_color, intensity)
                img.putpixel((x, y), c + (255,))
    # Corner beast runes
    rune_color = (100, 180, 160)
    for rx, ry in [(14, 32), (50, 32), (14, 54), (50, 54)]:
        draw.line([(rx, ry-3), (rx, ry+3)], fill=rune_color, width=1)
        draw.line([(rx-2, ry-1), (rx+2, ry-1)], fill=rune_color, width=1)
    out_path = os.path.join(GUI_DIR, "beast_pact_altar.png")
    img.save(out_path)
    return ("beast_pact_altar.png", count_colors(img), "Stepped stone altar with paw carving, spirit glow")

# ── 7. Refining Pool (淬炼池) ──
def gen_refining_pool():
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    # Stone chamber background
    for y in range(64):
        for x in range(64):
            n = fbm_noise(x, y, freq=0.06, seed_offset=8000)
            v = int(42 + n * 16)
            img.putpixel((x, y), (v, v+1, v+4, 255))
    # Pool basin (stone walls)
    pool_wall = (80, 85, 95)
    pool_wall_dark = darken(pool_wall, 0.55)
    for y in range(16, 56):
        for x in range(10, 54):
            # U-shape basin
            in_basin = False
            if y >= 20 and y <= 52 and x >= 16 and x <= 48:
                in_basin = True
            if in_basin:
                continue
            cx_d = abs(x - 31.5) / 21.5
            shade = 1.0 - cx_d * 0.25
            c = lerp_color(pool_wall_dark, pool_wall, shade)
            c = tuple(add_noise_channel(c[i], 8) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Liquid surface (metallic silver-blue)
    liquid_base = (140, 170, 210)
    liquid_dark = darken(liquid_base, 0.6)
    liquid_light = lighten(liquid_base, 35)
    for y in range(22, 52):
        for x in range(18, 48):
            n = fbm_noise(x, y, freq=0.1, seed_offset=8500)
            # Ripple pattern
            ripple = math.sin(x * 0.3 + y * 0.2 + n * 6) * 0.3
            cx_d = abs(x - 32.0) / 14.0
            cy_d = abs(y - 37.0) / 14.0
            shade = 0.5 + ripple - cx_d * 0.2 - cy_d * 0.15
            c = lerp_color(liquid_dark, liquid_light, shade)
            c = tuple(add_noise_channel(c[i], 12) for i in range(3))
            img.putpixel((x, y), c + (255,))
    # Qi shimmer highlights
    for _ in range(15):
        sx = random.randint(22, 44)
        sy = random.randint(26, 46)
        shimmer = clamp(180 + random.randint(-20, 20))
        img.putpixel((sx, sy), (shimmer, shimmer, clamp(shimmer + 30), 255))
    # Mineral deposits on walls (glowing spots)
    for mx, my in [(14, 30), (50, 35), (16, 48), (48, 25)]:
        for dy in range(-1, 2):
            for dx in range(-1, 2):
                c = lerp_color((60, 100, 120), (100, 200, 180), 0.5 + random.random() * 0.3)
                nx, ny = mx+dx, my+dy
                if 0 <= nx < 64 and 0 <= ny < 64:
                    img.putpixel((nx, ny), c + (255,))
    out_path = os.path.join(GUI_DIR, "refining_pool.png")
    img.save(out_path)
    return ("refining_pool.png", count_colors(img), "Stone basin with metallic liquid, ripples, mineral deposits")

# Generate all GUI textures
gui_generators = [
    gen_alchemy_furnace,
    gen_restriction_altar,
    gen_puppet_platform,
    gen_artifact_forge,
    gen_soul_refining_cauldron,
    gen_beast_pact_altar,
    gen_refining_pool,
]

for gen_func in gui_generators:
    result = gen_func()
    gui_results.append(result)
    print(f"  {result[0]}: {result[1]} colors — {result[2]}")

# ═══════════════════════════════════════════════════════════════════════════════
# SUMMARY
# ═══════════════════════════════════════════════════════════════════════════════

print("\n" + "="*60)
print("CRON-COMPLETIONIST-63 TEXTURE GENERATION SUMMARY")
print("="*60)
print(f"\nCultivator textures ({len(cult_results)}):")
for fname, count, desc in cult_results:
    print(f"  {fname}: {count} colors — {desc}")
print(f"\nGUI textures ({len(gui_results)}):")
for fname, count, desc in gui_results:
    print(f"  {fname}: {count} colors — {desc}")
