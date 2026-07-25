#!/usr/bin/env python3
"""
generate_beast_textures_v4.py — CRON-COMPLETIONIST-77

Regenerate the 3 worst beast textures to reach 1500+ unique opaque colors:
  - spirit_deer.png: 168 colors → target 1800+ (red-brown coat, cream belly, bone antlers)
  - soul_fish.png:   367 colors → target 1500+ (deep blue-violet, cyan bioluminescence)
  - spirit_tiger.png: 573 colors → target 1800+ (orange + black stripes, amber eyes)

Key improvement over v3: 6-octave FBM (was 3), per-species feature painting
(antlers, fish scales, tiger whorls), bilateral asymmetry, and much higher
color diversity via continuous value noise + domain warping.
"""

import os
import math
import random
import struct
import zlib

# ─── Output directory ─────────────────────────────────────────────
OUT_DIR = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/entity/beast/"

# ─── Noise utilities ─────────────────────────────────────────────

def hash2d(ix, iy, seed=0):
    """Integer hash for 2D coordinates."""
    n = ix * 374761393 + iy * 668265263 + seed * 1013904223
    n = ((n >> 13) ^ n) & 0xFFFFFFFF
    n = (n * (n * n * 60493 + 19990303) + 1376312589) & 0x7FFFFFFF
    return n / 0x7FFFFFFF  # [0, 1]

def smooth_noise(x, y, seed=0):
    """Value noise with bilinear interpolation."""
    ix, iy = int(math.floor(x)), int(math.floor(y))
    fx, fy = x - ix, y - iy
    # Smoothstep
    fx = fx * fx * (3 - 2 * fx)
    fy = fy * fy * (3 - 2 * fy)
    v00 = hash2d(ix, iy, seed)
    v10 = hash2d(ix + 1, iy, seed)
    v01 = hash2d(ix, iy + 1, seed)
    v11 = hash2d(ix + 1, iy + 1, seed)
    return v00 * (1-fx) * (1-fy) + v10 * fx * (1-fy) + v01 * (1-fx) * fy + v11 * fx * fy

def fbm(x, y, octaves=6, lacunarity=2.0, gain=0.5, seed=0):
    """Fractal Brownian Motion — multi-octave value noise."""
    value = 0.0
    amplitude = 1.0
    frequency = 1.0
    for i in range(octaves):
        value += amplitude * smooth_noise(x * frequency, y * frequency, seed + i * 1000)
        amplitude *= gain
        frequency *= lacunarity
    return value

def domain_warped_fbm(x, y, octaves=6, seed=0):
    """FBM with domain warping for organic patterns."""
    wx = fbm(x + 5.2, y + 1.3, octaves=3, seed=seed + 100)
    wy = fbm(x + 8.7, y + 3.8, octaves=3, seed=seed + 200)
    return fbm(x + 3.0 * wx, y + 3.0 * wy, octaves=octaves, seed=seed)

def clamp(v, lo=0, hi=255):
    return max(lo, min(hi, int(v)))

def lerp(a, b, t):
    return a + (b - a) * t

def color_distance(r1, g1, b1, r2, g2, b2):
    return abs(r1-r2) + abs(g1-g2) + abs(b1-b2)

# ─── PNG writer (minimal, no dependencies) ─────────────────────────

def write_png(filename, width, height, pixels):
    """Write RGBA pixels to a PNG file. pixels is [y][x] = (r,g,b,a)."""
    def make_chunk(chunk_type, data):
        chunk = chunk_type + data
        crc = struct.pack('>I', zlib.crc32(chunk) & 0xFFFFFFFF)
        return struct.pack('>I', len(data)) + chunk + crc

    raw = b''
    for y in range(height):
        raw += b'\x00'  # filter: none
        for x in range(width):
            r, g, b, a = pixels[y][x]
            raw += bytes([r, g, b, a])

    sig = b'\x89PNG\r\n\x1a\n'
    ihdr = struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0)  # 8bit RGBA
    compressed = zlib.compress(raw)

    with open(filename, 'wb') as f:
        f.write(sig)
        f.write(make_chunk(b'IHDR', ihdr))
        f.write(make_chunk(b'IDAT', compressed))
        f.write(make_chunk(b'IEND', b''))

# ─── Spirit Deer Texture ──────────────────────────────────────────

def generate_spirit_deer():
    """Reddish-brown coat with cream belly, bone-white antlers, dark ears."""
    W, H = 64, 64
    random.seed(42)

    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Base coat palette
    coat_dark = (140, 80, 40)    # dark reddish-brown (back, sides)
    coat_mid = (170, 105, 55)    # medium brown
    coat_light = (195, 140, 80)  # light brown (shoulder, upper legs)
    belly = (220, 195, 160)      # cream belly/chest
    muzzle = (180, 155, 130)     # tan muzzle
    nose = (60, 40, 30)          # dark nose
    eye_color = (50, 35, 20)     # dark brown eye
    antler_base = (230, 215, 190)  # bone white
    antler_dark = (180, 160, 130)   # darker bone tips
    ear_inner = (170, 120, 100)   # pinkish inner ear
    hoof = (90, 70, 50)           # dark hooves

    for y in range(H):
        for x in range(W):
            # 6-octave FBM for continuous fur variation
            n = domain_warped_fbm(x * 0.08, y * 0.08, octaves=6, seed=42)
            n2 = domain_warped_fbm(x * 0.15 + 100, y * 0.15 + 100, octaves=6, seed=43)
            n3 = fbm(x * 0.25, y * 0.25, octaves=4, seed=44)

            # Vertical gradient: back (top of UV) is darker, belly (bottom) is lighter
            vgrad = y / H  # 0=top, 1=bottom
            belly_factor = max(0, (vgrad - 0.45) * 3.0)  # belly zone

            # Stripe-like banding (subtle, like real deer coat)
            stripe = math.sin(y * 0.6 + n2 * 3.0) * 0.15

            # Blend coat colors based on gradient + noise
            if belly_factor > 0.3:
                # Belly/chest region
                t = min(1.0, (belly_factor - 0.3) * 2.0)
                r = lerp(coat_mid[0], belly[0], t) + (n - 0.5) * 25 + stripe * 15
                g = lerp(coat_mid[1], belly[1], t) + (n - 0.5) * 20 + stripe * 10
                b = lerp(coat_mid[2], belly[2], t) + (n - 0.5) * 15 + stripe * 8
            else:
                # Main coat
                blend = n * 0.6 + stripe
                r = lerp(coat_dark[0], coat_light[0], blend) + (n2 - 0.5) * 30
                g = lerp(coat_dark[1], coat_light[1], blend) + (n2 - 0.5) * 22
                b = lerp(coat_dark[2], coat_light[2], blend) + (n2 - 0.5) * 15

            # Fine grain noise for fur texture
            r += (n3 - 0.5) * 18
            g += (n3 - 0.5) * 14
            b += (n3 - 0.5) * 10

            # Antler region (top-center of UV, y < 12 approximately)
            # Antlers are at higher UV rows
            if y < 14 and 8 < x < 56:
                antler_n = fbm(x * 0.2, y * 0.3, octaves=4, seed=55)
                antler_t = antler_n * 0.7 + 0.15
                r = lerp(r, antler_base[0], antler_t)
                g = lerp(g, antler_base[1], antler_t)
                b = lerp(b, antler_base[2], antler_t)
                # Growth ring patterns on antlers
                ring = math.sin(y * 1.5 + x * 0.1) * 0.1
                r += ring * 20
                g += ring * 15
                b += ring * 10

            a = 255
            pixels[y][x] = (clamp(r), clamp(g), clamp(b), a)

    # Paint features
    # Eyes at approximately (20, 16) and (44, 16) — deer head region
    for ey_pos in [(20, 16), (44, 16)]:
        ex, ey = ey_pos
        for dy in range(-1, 2):
            for dx in range(-1, 2):
                if 0 <= ey+dy < H and 0 <= ex+dx < W:
                    dist = abs(dx) + abs(dy)
                    if dist <= 1:
                        t = 1.0 - dist * 0.3
                        r, g, b, _ = pixels[ey+dy][ex+dx]
                        pixels[ey+dy][ex+dx] = (
                            clamp(lerp(r, eye_color[0], t)),
                            clamp(lerp(g, eye_color[1], t)),
                            clamp(lerp(b, eye_color[2], t)),
                            255
                        )
        # Eye highlight
        if 0 <= ey-1 < H and 0 <= ex-1 < W:
            r, g, b, _ = pixels[ey-1][ex-1]
            pixels[ey-1][ex-1] = (clamp(r + 60), clamp(g + 60), clamp(b + 50), 255)

    # Nose at (32, 18)
    for dy in range(-1, 2):
        for dx in range(-2, 3):
            ny, nx = 18 + dy, 32 + dx
            if 0 <= ny < H and 0 <= nx < W:
                dist = abs(dx) * 0.5 + abs(dy)
                if dist < 1.5:
                    t = 1.0 - dist * 0.5
                    r, g, b, _ = pixels[ny][nx]
                    pixels[ny][nx] = (
                        clamp(lerp(r, nose[0], t)),
                        clamp(lerp(g, nose[1], t)),
                        clamp(lerp(b, nose[2], t)),
                        255
                    )

    # Ears (dark tips at edges)
    for ear_x_range in [(4, 14), (50, 60)]:
        for ex in range(ear_x_range[0], ear_x_range[1]):
            ey = 12
            if 0 <= ey < H and 0 <= ex < W:
                ear_t = fbm(ex * 0.3, ey * 0.3, octaves=3, seed=60)
                r, g, b, _ = pixels[ey][ex]
                pixels[ey][ex] = (
                    clamp(r - 40 + ear_t * 20),
                    clamp(g - 35 + ear_t * 15),
                    clamp(b - 25 + ear_t * 10),
                    255
                )

    # Hooves at bottom rows (y > 55)
    for y in range(56, H):
        for x in range(W):
            hoof_n = fbm(x * 0.3, y * 0.5, octaves=3, seed=65)
            t = min(1.0, (y - 56) / 6.0) * 0.7
            r, g, b, _ = pixels[y][x]
            pixels[y][x] = (
                clamp(lerp(r, hoof[0], t) + hoof_n * 15),
                clamp(lerp(g, hoof[1], t) + hoof_n * 12),
                clamp(lerp(b, hoof[2], t) + hoof_n * 8),
                255
            )

    out_path = os.path.join(OUT_DIR, "spirit_deer.png")
    write_png(out_path, W, H, pixels)
    return count_colors(pixels), out_path

# ─── Soul Fish Texture ────────────────────────────────────────────

def generate_soul_fish():
    """Deep blue-violet body with cyan-teal bioluminescent spots."""
    W, H = 64, 64
    random.seed(77)

    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Body palette
    body_dark = (30, 20, 70)     # deep indigo back
    body_mid = (60, 40, 120)     # blue-violet body
    body_light = (90, 70, 160)   # lighter sides
    belly_color = (120, 110, 180) # pale belly
    fin_color = (70, 140, 180)    # translucent teal fins
    eye_color = (100, 240, 255)   # bright cyan eye (bioluminescent)
    glow_spot = (80, 220, 240)    # bioluminescent spot

    for y in range(H):
        for x in range(W):
            # 6-octave FBM for body color variation
            n = domain_warped_fbm(x * 0.1, y * 0.1, octaves=6, seed=77)
            n2 = domain_warped_fbm(x * 0.18 + 50, y * 0.18 + 50, octaves=6, seed=78)
            n3 = fbm(x * 0.3, y * 0.3, octaves=4, seed=79)
            n4 = fbm(x * 0.5 + 200, y * 0.5 + 200, octaves=3, seed=80)

            # Horizontal gradient: dorsal (top) dark, ventral (bottom) light
            hgrad = y / H

            # Scale pattern — overlapping arcs
            scale = math.sin(x * 0.8 + y * 0.3 + n2 * 2.0) * math.cos(y * 0.6 + n * 1.5) * 0.12

            # Body color
            if hgrad < 0.35:
                # Dorsal region (dark)
                t = hgrad / 0.35
                r = lerp(body_dark[0], body_mid[0], t) + (n - 0.5) * 20 + scale * 15
                g = lerp(body_dark[1], body_mid[1], t) + (n - 0.5) * 15 + scale * 12
                b = lerp(body_dark[2], body_mid[2], t) + (n - 0.5) * 25 + scale * 20
            elif hgrad < 0.7:
                # Mid body
                t = (hgrad - 0.35) / 0.35
                r = lerp(body_mid[0], body_light[0], t) + (n2 - 0.5) * 22 + scale * 18
                g = lerp(body_mid[1], body_light[1], t) + (n2 - 0.5) * 18 + scale * 14
                b = lerp(body_mid[2], body_light[2], t) + (n2 - 0.5) * 28 + scale * 22
            else:
                # Belly (pale)
                t = (hgrad - 0.7) / 0.3
                r = lerp(body_light[0], belly_color[0], t) + (n3 - 0.5) * 18
                g = lerp(body_light[1], belly_color[1], t) + (n3 - 0.5) * 15
                b = lerp(body_light[2], belly_color[2], t) + (n3 - 0.5) * 22

            # Fine shimmer (iridescent fish scales)
            shimmer = math.sin(x * 0.7 + y * 0.4 + n4 * 4.0) * 0.5 + 0.5
            r += shimmer * 12
            g += shimmer * 18
            b += shimmer * 25

            # Lateral line (along middle of body, ~y=30-34)
            if 28 < y < 36:
                lateral_dist = abs(y - 32) / 4.0
                if lateral_dist < 1.0:
                    lateral_t = (1.0 - lateral_dist) * 0.6
                    r = lerp(r, 140, lateral_t)
                    g = lerp(g, 180, lateral_t)
                    b = lerp(b, 220, lateral_t)

            # Bioluminescent spots — scattered cyan dots
            spot_n = fbm(x * 0.4 + 300, y * 0.4 + 300, octaves=3, seed=88)
            spot_n2 = fbm(x * 0.6 + 400, y * 0.6 + 400, octaves=3, seed=89)
            # Create discrete spots using threshold
            spot_val = max(spot_n, spot_n2)
            if spot_val > 0.72:
                intensity = (spot_val - 0.72) / 0.28  # 0 to 1
                intensity = intensity * intensity  # quadratic falloff
                r = lerp(r, glow_spot[0], intensity * 0.8)
                g = lerp(g, glow_spot[1], intensity * 0.8)
                b = lerp(b, glow_spot[2], intensity * 0.8)

            a = 255
            pixels[y][x] = (clamp(r), clamp(g), clamp(b), a)

    # Eyes (bioluminescent cyan)
    for eye_pos in [(22, 20), (42, 20)]:
        ex, ey = eye_pos
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                ny, nx = ey + dy, ex + dx
                if 0 <= ny < H and 0 <= nx < W:
                    dist = math.sqrt(dx*dx + dy*dy)
                    if dist < 2.5:
                        t = max(0, 1.0 - dist / 2.5)
                        t = t * t
                        r, g, b, _ = pixels[ny][nx]
                        pixels[ny][nx] = (
                            clamp(lerp(r, eye_color[0], t)),
                            clamp(lerp(g, eye_color[1], t)),
                            clamp(lerp(b, eye_color[2], t)),
                            255
                        )
        # Eye glow halo
        for dy in range(-3, 4):
            for dx in range(-3, 4):
                ny, nx = ey + dy, ex + dx
                if 0 <= ny < H and 0 <= nx < W:
                    dist = math.sqrt(dx*dx + dy*dy)
                    if 2.5 <= dist < 4.0:
                        t = (1.0 - (dist - 2.5) / 1.5) * 0.3
                        r, g, b, _ = pixels[ny][nx]
                        pixels[ny][nx] = (
                            clamp(r + glow_spot[0] * t * 0.3),
                            clamp(g + glow_spot[1] * t * 0.3),
                            clamp(b + glow_spot[2] * t * 0.3),
                            255
                        )

    # Dorsal fin (top region, y < 8) — darker with edge glow
    for y in range(0, 8):
        for x in range(15, 49):
            if 0 <= y < H and 0 <= x < W:
                fin_t = (1.0 - y / 8.0) * 0.5
                fin_n = fbm(x * 0.3, y * 0.4, octaves=3, seed=90)
                r, g, b, _ = pixels[y][x]
                pixels[y][x] = (
                    clamp(lerp(r, fin_color[0], fin_t) + fin_n * 15),
                    clamp(lerp(g, fin_color[1], fin_t) + fin_n * 20),
                    clamp(lerp(b, fin_color[2], fin_t) + fin_n * 25),
                    255
                )

    # Tail fan (right side, x > 52) — translucent with glow
    for y in range(20, 45):
        for x in range(52, W):
            if 0 <= y < H and 0 <= x < W:
                tail_t = ((x - 52) / (W - 52)) * 0.5
                tail_n = fbm(x * 0.4, y * 0.3, octaves=3, seed=91)
                glow = math.sin(y * 0.5 + tail_n * 3.0) * 0.5 + 0.5
                r, g, b, _ = pixels[y][x]
                pixels[y][x] = (
                    clamp(lerp(r, fin_color[0], tail_t) + glow * 20),
                    clamp(lerp(g, fin_color[1], tail_t) + glow * 30),
                    clamp(lerp(b, fin_color[2], tail_t) + glow * 35),
                    255
                )

    out_path = os.path.join(OUT_DIR, "soul_fish.png")
    write_png(out_path, W, H, pixels)
    return count_colors(pixels), out_path

# ─── Spirit Tiger Texture ─────────────────────────────────────────

def generate_spirit_tiger():
    """Orange coat with black stripes, white belly/chest, amber slit-pupil eyes."""
    W, H = 64, 64
    random.seed(99)

    pixels = [[(0, 0, 0, 0) for _ in range(W)] for _ in range(H)]

    # Tiger palette
    orange_base = (210, 130, 50)   # base orange
    orange_dark = (180, 105, 35)   # darker orange (shadows)
    orange_light = (235, 175, 80)  # light orange (highlights)
    stripe_black = (25, 18, 12)     # near-black stripes
    white_belly = (240, 230, 215)  # cream-white belly
    white_chest = (235, 220, 200)  # cream chest
    nose_pink = (200, 150, 140)    # pink nose pad
    eye_amber = (220, 170, 40)     # amber iris
    eye_slit = (15, 10, 5)         # black slit pupil
    paw_pad = (160, 130, 110)      # paw pad pink
    whisker_white = (240, 235, 225)

    for y in range(H):
        for x in range(W):
            # 6-octave domain-warped FBM for rich fur variation
            n = domain_warped_fbm(x * 0.09, y * 0.09, octaves=6, seed=99)
            n2 = domain_warped_fbm(x * 0.16 + 150, y * 0.16 + 150, octaves=6, seed=100)
            n3 = fbm(x * 0.3 + 250, y * 0.3 + 250, octaves=4, seed=101)
            n4 = fbm(x * 0.5 + 350, y * 0.5 + 350, octaves=3, seed=102)

            # Vertical gradient: dorsal (top) darker, ventral (bottom) lighter
            vgrad = y / H
            belly_factor = max(0, (vgrad - 0.5) * 2.5)

            # ── STRIPE PATTERN ──
            # Real tiger stripes are NOT parallel bands. They're curved,
            # branching, asymmetric whorls that follow body contour lines.
            # We simulate this with domain-warped threshold noise.
            stripe_field = domain_warped_fbm(
                x * 0.06 + 500, y * 0.12 + 500,
                octaves=5, seed=103
            )
            # Stripe threshold — creates organic branching pattern
            is_stripe = stripe_field > 0.55

            # Stripe width modulation (thinner at belly, wider on back)
            stripe_width_mod = 1.0 + (1.0 - vgrad) * 0.15
            is_stripe = is_stripe and (stripe_field - 0.55) < 0.25 * stripe_width_mod

            # Reduce stripe density on belly
            if belly_factor > 0.5:
                is_stripe = is_stripe and stripe_field > 0.65

            # Base fur color with noise variation
            if belly_factor > 0.3:
                # White/cream belly region
                t = min(1.0, (belly_factor - 0.3) / 0.7)
                r = lerp(orange_light[0], white_belly[0], t) + (n - 0.5) * 20
                g = lerp(orange_light[1], white_belly[1], t) + (n - 0.5) * 15
                b = lerp(orange_light[2], white_belly[2], t) + (n - 0.5) * 10
                # Belly stripes are very faint
                if is_stripe and stripe_field > 0.7:
                    r = lerp(r, stripe_black[0], 0.15)
                    g = lerp(g, stripe_black[1], 0.15)
                    b = lerp(b, stripe_black[2], 0.15)
            else:
                # Main body — orange with black stripes
                fur_blend = n * 0.5 + n2 * 0.3 + 0.2
                r = lerp(orange_dark[0], orange_light[0], fur_blend) + (n3 - 0.5) * 18
                g = lerp(orange_dark[1], orange_light[1], fur_blend) + (n3 - 0.5) * 14
                b = lerp(orange_dark[2], orange_light[2], fur_blend) + (n3 - 0.5) * 10

                if is_stripe:
                    # Stripe edge softness
                    edge = (stripe_field - 0.55) / 0.15 if stripe_field < 0.70 else 1.0
                    edge = min(1.0, max(0.0, edge))
                    # Stripe has its own noise for fur texture within the stripe
                    stripe_n = n4 * 0.3
                    sr = lerp(stripe_black[0], stripe_black[0] + 25, stripe_n)
                    sg = lerp(stripe_black[1], stripe_black[1] + 18, stripe_n)
                    sb = lerp(stripe_black[2], stripe_black[2] + 12, stripe_n)
                    r = lerp(r, sr, edge * 0.92)
                    g = lerp(g, sg, edge * 0.92)
                    b = lerp(b, sb, edge * 0.92)

            # Very fine grain for individual fur hairs
            r += (n4 - 0.5) * 12
            g += (n4 - 0.5) * 10
            b += (n4 - 0.5) * 8

            a = 255
            pixels[y][x] = (clamp(r), clamp(g), clamp(b), a)

    # ── FEATURES ──

    # Eyes at (20, 14) and (44, 14) — amber with slit pupil
    for eye_pos in [(20, 14), (44, 14)]:
        ex, ey = eye_pos
        for dy in range(-2, 3):
            for dx in range(-2, 3):
                ny, nx = ey + dy, ex + dx
                if 0 <= ny < H and 0 <= nx < W:
                    dist = math.sqrt(dx*dx + dy*dy)
                    if dist < 2.5:
                        t = max(0, 1.0 - dist / 2.5)
                        # Amber iris
                        r, g, b, _ = pixels[ny][nx]
                        pixels[ny][nx] = (
                            clamp(lerp(r, eye_amber[0], t)),
                            clamp(lerp(g, eye_amber[1], t)),
                            clamp(lerp(b, eye_amber[2], t)),
                            255
                        )
        # Slit pupil (vertical line)
        for dy in range(-2, 3):
            ny = ey + dy
            if 0 <= ny < H and 0 <= ex < W:
                t = max(0, 1.0 - abs(dy) / 2.5)
                r, g, b, _ = pixels[ny][ex]
                pixels[ny][ex] = (
                    clamp(lerp(r, eye_slit[0], t)),
                    clamp(lerp(g, eye_slit[1], t)),
                    clamp(lerp(b, eye_slit[2], t)),
                    255
                )
        # Eye highlight
        if 0 <= ey-1 < H and 0 <= ex-1 < W:
            r, g, b, _ = pixels[ey-1][ex-1]
            pixels[ey-1][ex-1] = (clamp(r + 80), clamp(g + 70), clamp(b + 50), 255)

    # Nose at (32, 16)
    for dy in range(-1, 2):
        for dx in range(-2, 3):
            ny, nx = 16 + dy, 32 + dx
            if 0 <= ny < H and 0 <= nx < W:
                dist = abs(dx) * 0.6 + abs(dy)
                if dist < 1.5:
                    t = 1.0 - dist * 0.4
                    r, g, b, _ = pixels[ny][nx]
                    pixels[ny][nx] = (
                        clamp(lerp(r, nose_pink[0], t)),
                        clamp(lerp(g, nose_pink[1], t)),
                        clamp(lerp(b, nose_pink[2], t)),
                        255
                    )

    # Whisker dots (3 per side of muzzle)
    for whisker_pos in [(26, 17), (27, 18), (38, 17), (37, 18)]:
        wx, wy = whisker_pos
        if 0 <= wy < H and 0 <= wx < W:
            r, g, b, _ = pixels[wy][wx]
            pixels[wy][wx] = (clamp(r - 30), clamp(g - 25), clamp(b - 20), 255)

    # Paw pads at bottom rows (y > 56)
    for y in range(57, H):
        for x in range(W):
            paw_n = fbm(x * 0.35, y * 0.5, octaves=3, seed=110)
            t = min(1.0, (y - 57) / 5.0) * 0.4
            r, g, b, _ = pixels[y][x]
            pixels[y][x] = (
                clamp(lerp(r, paw_pad[0], t) + paw_n * 12),
                clamp(lerp(g, paw_pad[1], t) + paw_n * 10),
                clamp(lerp(b, paw_pad[2], t) + paw_n * 8),
                255
            )

    # Chest white patch (around x=24-40, y=36-44) — tiger ruff
    for y in range(36, 45):
        for x in range(22, 42):
            cx = (x - 32) / 10.0
            cy = (y - 40) / 5.0
            ruff_dist = math.sqrt(cx*cx + cy*cy)
            if ruff_dist < 1.2:
                t = (1.2 - ruff_dist) / 1.2 * 0.5
                r, g, b, _ = pixels[y][x]
                pixels[y][x] = (
                    clamp(lerp(r, white_chest[0], t)),
                    clamp(lerp(g, white_chest[1], t)),
                    clamp(lerp(b, white_chest[2], t)),
                    255
                )

    out_path = os.path.join(OUT_DIR, "spirit_tiger.png")
    write_png(out_path, W, H, pixels)
    return count_colors(pixels), out_path

# ─── Color counting ───────────────────────────────────────────────

def count_colors(pixels):
    """Count unique opaque colors (alpha > 8)."""
    colors = set()
    for row in pixels:
        for r, g, b, a in row:
            if a > 8:
                colors.add((r, g, b))
    return len(colors)

# ─── Main ─────────────────────────────────────────────────────────

if __name__ == "__main__":
    os.makedirs(OUT_DIR, exist_ok=True)

    print("=== CRON-COMPLETIONIST-77: Beast Texture Regeneration v4 ===\n")

    print("Generating spirit_deer.png...")
    deer_count, deer_path = generate_spirit_deer()
    print(f"  → {deer_count} unique opaque colors (was 168) → {deer_path}")

    print("Generating soul_fish.png...")
    fish_count, fish_path = generate_soul_fish()
    print(f"  → {fish_count} unique opaque colors (was 367) → {fish_path}")

    print("Generating spirit_tiger.png...")
    tiger_count, tiger_path = generate_spirit_tiger()
    print(f"  → {tiger_count} unique opaque colors (was 573) → {tiger_path}")

    print(f"\nDone. All 3 textures regenerated with 6-octave domain-warped FBM.")
