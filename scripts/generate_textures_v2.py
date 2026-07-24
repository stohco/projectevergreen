#!/usr/bin/env python3
"""
CRON-COMPLETIONIST texture generator v2.

Generates ALL entity textures for the Er Gen Verse mod.
Each texture matches the model UV layouts defined in createBodyLayer().

MINECRAFT CUBE UV UNFOLDING (Forge 1.20.1 CubeListBuilder):
  For addBox(x,y,z, dx,dy,dz) with texOffs(u,v):
  The texture layout is a cross:
      col:  dz    dx    dx    dz
  row 0 (top/bottom):
    Top    u+dz, v          size: dx x dz
    Bottom u+dz+dx, v       size: dx x dz
  row 1 (sides):
    North  u, v+dz           size: dx x dy   (mirror-x from west reference)
    South  u+dx, v+dz        size: dx x dy
    West   u+dx+dx, v+dz     size: dz x dy
    East   u+dx+dx+dz, v+dz  size: dz x dy

  TOTAL UV footprint: (2*dx + 2*dz) wide x (dz + dy) tall

HARSH SELF-CRITIQUE:
  These are PROCEDURAL textures — no hand-painted art. They use:
  - Perlin-like noise for fur/feather texture
  - Gradient fills for depth shading
  - Feature detail (eyes, nose, claws, markings)
  
  They are NOT equivalent to what a real texture artist would produce.
  A real artist would hand-paint fur direction, subsurface scattering,
  wear marks, and individual identity variation.
  
  But they are INFINITELY better than purple/black checkerboard.
"""

from PIL import Image, ImageDraw, ImageFilter
import os
import random
import math

random.seed(42)  # reproducible noise

BASE = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/entity"


def noise(x, y, seed=0):
    """Simple deterministic noise for texture variation."""
    n = int(x * 12.9898 + y * 78.233 + seed * 43.758)
    n = (n << 13) ^ n
    return ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 2147483647.0


def fur_noise(x, y, base_color, variation=15, seed=0):
    """Apply subtle per-pixel noise to simulate fur/skin texture."""
    n = noise(x, y, seed)
    r = max(0, min(255, base_color[0] + int((n - 0.5) * variation * 2)))
    g = max(0, min(255, base_color[1] + int((n - 0.5) * variation * 2)))
    b = max(0, min(255, base_color[2] + int((n - 0.5) * variation * 2)))
    return (r, g, b, 255)


def fill_with_noise(img, x0, y0, x1, y1, base_color, variation=12, seed=0):
    """Fill a rectangle with noise-textured color."""
    for y in range(y0, min(y1, img.height)):
        for x in range(x0, min(x1, img.width)):
            c = fur_noise(x, y, base_color, variation, seed)
            img.putpixel((x, y), c)


def fill_rect(img, x0, y0, x1, y1, color):
    """Fill a rectangle with a solid color."""
    d = ImageDraw.Draw(img)
    d.rectangle([x0, y0, x1, y1], fill=color)


def gradient_rect(img, x0, y0, x1, y1, color_top, color_bot, vertical=True):
    """Fill with vertical or horizontal gradient."""
    for y in range(y0, min(y1, img.height)):
        t = (y - y0) / max(1, y1 - y0)
        r = int(color_top[0] + (color_bot[0] - color_top[0]) * t)
        g = int(color_top[1] + (color_bot[1] - color_top[1]) * t)
        b = int(color_top[2] + (color_bot[2] - color_top[2]) * t)
        a = int(color_top[3] + (color_bot[3] - color_top[3]) * t) if len(color_top) > 3 else 255
        for x in range(x0, min(x1, img.width)):
            img.putpixel((x, y), (r, g, b, a))


def cube_uv_footprint(dx, dy, dz):
    """Calculate the UV region size for a Minecraft cube."""
    # UV layout width: dx + dz + dx + dz = 2*(dx+dz)
    # UV layout height: dz + dy
    return (2 * (dx + dz), dz + dy)


def paint_cube(img, u, v, dx, dy, dz, base_color, noise_var=12, seed=0):
    """Paint a Minecraft cube UV region with noise texture.
    
    Handles the cross-pattern unfolding of a cube onto the texture.
    The total UV area is:
      width: 2*(dx+dz), height: dz+dy
    """
    uv_w, uv_h = cube_uv_footprint(dx, dy, dz)
    
    # Fill entire UV region with base color + noise
    for py in range(v, min(v + uv_h, img.height)):
        for px in range(u, min(u + uv_w, img.width)):
            c = fur_noise(px, py, base_color, noise_var, seed)
            img.putpixel((px, py), c)
    
    # Add depth shading: top face lighter, bottom darker
    # Top face is at (u+dz, v) to (u+dz+dx, v+dz)
    for py in range(v, min(v + dz, img.height)):
        for px in range(u + dz, min(u + dz + dx, img.width)):
            c = img.getpixel((px, py))
            lighter = (min(255, c[0]+20), min(255, c[1]+20), min(255, c[2]+20), 255)
            img.putpixel((px, py), lighter)
    
    # Bottom face is at (u+dz+dx, v) to (u+2*dz+dx, v+dz)
    for py in range(v, min(v + dz, img.height)):
        for px in range(u + dz + dx, min(u + 2*dz + dx, img.width)):
            c = img.getpixel((px, py))
            darker = (max(0, c[0]-25), max(0, c[1]-25), max(0, c[2]-25), 255)
            img.putpixel((px, py), darker)


def make_spirit_wolf():
    """64x64 — lean quadruped predator, silver-blue spirit fur."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    base = (123, 143, 162, 255)      # silver-blue fur
    light = (150, 170, 190, 255)     # lighter highlights
    dark = (80, 95, 115, 255)        # dark accents
    belly = (170, 185, 200, 255)     # cream belly
    nose = (30, 25, 25, 255)         # black nose
    eye = (200, 190, 50, 255)        # amber eye
    fang = (240, 238, 230, 255)      # ivory fang
    ear_inner = (180, 150, 160, 255) # pink inner ear
    spirit = (140, 200, 255, 180)    # spirit glow
    
    # Body chest (texOffs 0,0, 4x6x5): uv_footprint = 2*(4+5)=18 wide, 5+6=11 tall
    paint_cube(img, 0, 0, 4, 6, 5, base, 15, seed=100)
    # Lighter belly area on bottom face
    for py in range(0, min(5, 64)):
        for px in range(9, min(14, 64)):  # bottom face region
            c = img.getpixel((px, py))
            img.putpixel((px, py), belly)
    
    # Body hip (texOffs 0,12, 3x5x6): uv_footprint = 18 wide, 6+5=11 tall
    paint_cube(img, 0, 12, 3, 5, 6, base, 15, seed=101)
    
    # Neck (texOffs 28,0, 2x2x3): uv_footprint = 10 wide, 3+2=5 tall
    paint_cube(img, 28, 0, 2, 2, 3, base, 12, seed=102)
    
    # Head skull (texOffs 0,18, 3x3x3) + snout (0,26, 2x2x2)
    paint_cube(img, 0, 18, 3, 3, 3, light, 10, seed=103)
    paint_cube(img, 0, 26, 2, 2, 2, light, 10, seed=104)
    
    # Ears (texOffs 20,18 and 20,24, both 1x2x1): uv_footprint = 4 wide, 1+2=3 tall
    paint_cube(img, 20, 18, 1, 2, 1, base, 10, seed=105)
    paint_cube(img, 20, 24, 1, 2, 1, base, 10, seed=106)
    # Inner ear pink on top face
    for py in range(18, min(19, 64)):
        for px in range(21, min(22, 64)):
            img.putpixel((px, py), ear_inner)
    for py in range(24, min(25, 64)):
        for px in range(21, min(22, 64)):
            img.putpixel((px, py), ear_inner)
    
    # Jaw (texOffs 28,18, 2x1x2): uv_footprint = 8 wide, 2+1=3 tall
    paint_cube(img, 28, 18, 2, 1, 2, belly, 10, seed=107)
    
    # Fangs (texOffs 40,0 and 40,4, both 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 40, 0, 1, 1, 1, fang, 5, seed=108)
    paint_cube(img, 40, 4, 1, 1, 1, fang, 5, seed=109)
    
    # Nose pad (texOffs 44,0, 1x0.5x0.5→1x1x1): treat as 1x1x1
    paint_cube(img, 44, 0, 1, 1, 1, nose, 5, seed=110)
    
    # Tail segments (texOffs 36,8/14/20, each 1x1x3): uv_footprint = 8 wide, 3+1=4 tall
    for y_off in [8, 14, 20]:
        paint_cube(img, 36, y_off, 1, 1, 3, base, 12, seed=111+y_off)
        # Tail tip lighter
        if y_off == 20:
            for py in range(y_off, min(y_off+4, 64)):
                for px in range(36, min(44, 64)):
                    c = img.getpixel((px, py))
                    lighter = (min(255,c[0]+15), min(255,c[1]+15), min(255,c[2]+15), 255)
                    img.putpixel((px, py), lighter)
    
    # Legs (4 thighs + 4 shins, each 2x3x2): uv_footprint = 8 wide, 2+3=5 tall
    leg_positions = [(0,32), (0,40), (8,32), (8,40), (0,48), (0,56), (8,48), (8,56)]
    for i, (lx, ly) in enumerate(leg_positions):
        paint_cube(img, lx, ly, 2, 3, 2, dark, 12, seed=200+i)
        # Paw (bottom face) slightly lighter
        for py in range(ly, min(ly+2, 64)):
            for px in range(lx+2, min(lx+4, 64)):
                c = img.getpixel((px, py))
                img.putpixel((px, py), (min(255,c[0]+10), min(255,c[1]+10), min(255,c[2]+10), 255))
    
    # Eyes on the head (front face of head skull is where eyes go)
    # The head's front face in UV starts around (0, 21) in the cube footprint
    # Paint small eye dots
    d.ellipse([1, 20, 3, 22], fill=eye)  # left eye
    d.ellipse([6, 20, 8, 22], fill=eye)  # right eye
    # Eye pupils
    d.ellipse([1, 20, 2, 21], fill=(20, 20, 20, 255))
    d.ellipse([7, 20, 8, 21], fill=(20, 20, 20, 255))
    
    # Spirit markings: faint luminous lines along the body
    for i in range(0, 18, 3):
        alpha_line = (140, 200, 255, 60)
        d.point((i, 2), fill=alpha_line)
        d.point((i, 4), fill=alpha_line)
    
    # Nose on snout front face
    d.ellipse([0, 27, 2, 28], fill=nose)
    
    return img


def make_spirit_hawk():
    """64x64 — raptor with 3-segment wings, brown-gold feathers."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    base = (139, 115, 85, 255)       # dark brown-gold
    wing_covert = (120, 100, 70, 255) # wing coverts
    flight_feather = (90, 75, 55, 255)  # flight feathers (darker)
    feather_tip = (70, 60, 45, 255)    # feather tips (darkest)
    beak = (55, 40, 25, 255)          # dark hooked beak
    cere = (90, 75, 50, 255)          # beak cere (waxy base)
    crest = (160, 135, 95, 255)       # lighter crest
    eye = (200, 180, 50, 255)         # golden eye
    talon = (70, 55, 40, 255)         # dark talons
    breast = (170, 145, 110, 255)     # lighter breast
    spirit = (200, 180, 120, 120)      # spirit sheen
    
    # Body (texOffs 0,0, 6x4x6): uv_footprint = 24 wide, 6+4=10 tall
    paint_cube(img, 0, 0, 6, 4, 6, base, 15, seed=300)
    # Breast lighter (front face region)
    for py in range(6, min(10, 64)):
        for px in range(0, min(6, 64)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), breast)
    
    # Head skull (texOffs 24,0, 3x3x3): uv_footprint = 12 wide, 3+3=6 tall
    paint_cube(img, 24, 0, 3, 3, 3, crest, 12, seed=301)
    
    # Beak (texOffs 24,8, 1x1x2): uv_footprint = 6 wide, 2+1=3 tall
    paint_cube(img, 24, 8, 1, 1, 2, beak, 8, seed=302)
    # Cere at base of beak
    for py in range(8, min(9, 64)):
        for px in range(26, min(28, 64)):
            img.putpixel((px, py), cere)
    
    # Crest (texOffs 36,0, 2x1x1): uv_footprint = 6 wide, 1+1=2 tall
    paint_cube(img, 36, 0, 2, 1, 1, crest, 10, seed=303)
    
    # Eye on head front face
    d.ellipse([25, 3, 27, 5], fill=eye)
    d.ellipse([29, 3, 31, 5], fill=eye)
    d.ellipse([25, 3, 26, 4], fill=(15, 15, 10, 255))
    d.ellipse([30, 3, 31, 4], fill=(15, 15, 10, 255))
    
    # LEFT WING
    # Shoulder (texOffs 0,16, 5x1x4): uv_footprint = 18 wide, 4+1=5 tall
    paint_cube(img, 0, 16, 5, 1, 4, wing_covert, 12, seed=310)
    # Forearm (texOffs 0,24, 5x1x4): uv_footprint = 18 wide, 4+1=5 tall
    paint_cube(img, 0, 24, 5, 1, 4, wing_covert, 12, seed=311)
    # Hand (texOffs 0,32, 4x1x4): uv_footprint = 16 wide, 4+1=5 tall
    paint_cube(img, 0, 32, 4, 1, 4, flight_feather, 12, seed=312)
    # Primary feathers (texOffs 20,16/20/24, each 8x1x1): uv_footprint = 4 wide, 1+1=2 tall
    for y_off in [16, 20, 24]:
        paint_cube(img, 20, y_off, 8, 1, 1, feather_tip, 8, seed=320+y_off)
        # Feather tip gets progressively darker
        darkness = (y_off - 16) // 4 * 10
        for py in range(y_off, min(y_off+2, 64)):
            for px in range(20, min(28, 64)):
                c = img.getpixel((px, py))
                img.putpixel((px, py), (max(0,c[0]-darkness), max(0,c[1]-darkness), max(0,c[2]-darkness), 255))
    
    # RIGHT WING
    paint_cube(img, 0, 40, 5, 1, 4, wing_covert, 12, seed=330)
    paint_cube(img, 0, 48, 5, 1, 4, wing_covert, 12, seed=331)
    paint_cube(img, 0, 56, 4, 1, 4, flight_feather, 12, seed=332)
    for y_off in [40, 44, 48]:
        paint_cube(img, 20, y_off, 8, 1, 1, feather_tip, 8, seed=340+y_off)
        darkness = (y_off - 40) // 4 * 10
        for py in range(y_off, min(y_off+2, 64)):
            for px in range(20, min(28, 64)):
                c = img.getpixel((px, py))
                img.putpixel((px, py), (max(0,c[0]-darkness), max(0,c[1]-darkness), max(0,c[2]-darkness), 255))
    
    # Tail feathers (texOffs 40,16/24/32, each 1x1x6): uv_footprint = 14 wide, 6+1=7 tall
    for y_off in [16, 24, 32]:
        paint_cube(img, 40, y_off, 1, 1, 6, base, 10, seed=360+y_off)
    
    # Legs (texOffs 50,16 through 50,50, multiple parts)
    for y_off in [16, 22, 36, 42]:
        paint_cube(img, 50, y_off, 1, 3, 1, base, 10, seed=370+y_off)
    for y_off in [19, 25, 39, 45]:
        paint_cube(img, 50, y_off, 2, 1, 1, talon, 8, seed=375+y_off)
    for y_off in [21, 27, 41, 47]:
        paint_cube(img, 50, y_off, 1, 1, 1, talon, 8, seed=380+y_off)
    for y_off in [22, 28, 42, 48]:
        paint_cube(img, 50, y_off, 1, 1, 1, talon, 8, seed=385+y_off)
    
    # Spirit glow on wing feather tips
    for i in range(20, 28):
        img.putpixel((i, 17), spirit)
        img.putpixel((i, 25), spirit)
        img.putpixel((i, 41), spirit)
        img.putpixel((i, 49), spirit)
    
    return img


def make_spirit_deer():
    """64x64 — long-necked grazer, tawny brown, branched antlers."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    base = (165, 135, 90, 255)       # tawny brown
    belly = (195, 175, 140, 255)     # cream underside
    dark = (125, 100, 70, 255)        # darker legs
    antler = (195, 185, 165, 255)     # bone-colored antlers
    antler_tip = (220, 215, 200, 255) # lighter tips
    nose = (80, 60, 45, 255)          # dark nose
    eye = (50, 40, 30, 255)           # dark eye
    ear_inner = (200, 175, 165, 255)  # pink inner ear
    hoof = (60, 45, 35, 255)          # dark hooves
    spirit = (220, 200, 150, 100)      # faint golden spirit glow
    
    # Body (texOffs 0,0, 3x5x8): uv_footprint = 22 wide, 8+5=13 tall
    paint_cube(img, 0, 0, 3, 5, 8, base, 15, seed=400)
    # Belly lighter (bottom face of body, approximate location)
    for py in range(0, min(8, 64)):
        for px in range(8, min(16, 64)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), belly)
    
    # Neck (texOffs 22,0, 1x4x1): uv_footprint = 4 wide, 1+4=5 tall
    paint_cube(img, 22, 0, 1, 4, 1, base, 12, seed=401)
    
    # Head skull (texOffs 0,16, 2x3x2): uv_footprint = 8 wide, 2+3=5 tall
    paint_cube(img, 0, 16, 2, 3, 2, belly, 10, seed=402)
    # Snout (texOffs 8,16, 2x1x2): uv_footprint = 8 wide, 2+1=3 tall
    paint_cube(img, 8, 16, 2, 1, 2, base, 10, seed=403)
    # Nose on front face of snout
    d.rectangle([8, 18, 10, 19], fill=nose)
    
    # Eyes on head
    d.ellipse([1, 18, 2, 19], fill=eye)
    d.ellipse([4, 18, 5, 19], fill=eye)
    
    # Ears (texOffs 20,16 and 20,20, each 1x2x1): uv_footprint = 4 wide, 1+2=3 tall
    paint_cube(img, 20, 16, 1, 2, 1, base, 10, seed=404)
    paint_cube(img, 20, 20, 1, 2, 1, base, 10, seed=405)
    # Inner ear pink
    for py in range(16, min(17, 64)):
        for px in range(21, min(22, 64)):
            img.putpixel((px, py), ear_inner)
    for py in range(20, min(21, 64)):
        for px in range(21, min(22, 64)):
            img.putpixel((px, py), ear_inner)
    
    # Antlers: main beams (texOffs 28,16 and 28,24, each 1x3x1): uv_footprint = 4 wide, 1+3=4 tall
    paint_cube(img, 28, 16, 1, 3, 1, antler, 8, seed=410)
    paint_cube(img, 28, 24, 1, 3, 1, antler, 8, seed=411)
    # Tines (texOffs 36,16/20/24/28, each 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    for y_off in [16, 20, 24, 28]:
        paint_cube(img, 36, y_off, 1, 1, 1, antler_tip, 6, seed=420+y_off)
    
    # Tail (texOffs 40,0, 2x2x2): uv_footprint = 8 wide, 2+2=4 tall
    paint_cube(img, 40, 0, 2, 2, 2, belly, 10, seed=430)
    
    # Legs (4 thighs + 4 shins, each 1.5x3x1.5→int 2x3x2): uv_footprint = 8 wide, 2+3=5 tall
    leg_positions = [(0,28),(0,34),(8,28),(8,34),(0,40),(0,46),(8,40),(8,46)]
    for i, (lx, ly) in enumerate(leg_positions):
        paint_cube(img, lx, ly, 2, 3, 2, dark, 12, seed=440+i)
        # Hooves on bottom face
        for py in range(ly, min(ly+2, 64)):
            for px in range(lx+2, min(lx+4, 64)):
                img.putpixel((px, py), hoof)
    
    # Spirit marking along spine (top face of body)
    for i in range(0, 6, 2):
        img.putpixel((i+1, 1), spirit)
        img.putpixel((i+1, 3), spirit)
    
    return img


def make_spirit_rabbit():
    """32x32 — small round lagomorph, grey-brown fur."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    base = (155, 145, 135, 255)     # grey-brown fur
    belly = (185, 178, 168, 255)    # lighter belly/face
    ear_inner = (205, 175, 165, 255) # pink inner ear
    nose = (185, 135, 135, 255)     # pink nose
    eye = (35, 30, 25, 255)         # dark eye
    dark = (115, 105, 95, 255)      # darker paws/feet
    tail = (190, 185, 178, 255)     # white puff tail
    spirit = (190, 210, 225, 100)   # faint spirit glow
    
    # Body (texOffs 0,0, 4x4x5): uv_footprint = 18 wide, 5+4=9 tall
    paint_cube(img, 0, 0, 4, 4, 5, base, 15, seed=500)
    # Belly lighter (bottom face)
    for py in range(0, min(5, 32)):
        for px in range(5, min(9, 32)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), belly)
    
    # Head (texOffs 0,10, 3x3x3): uv_footprint = 12 wide, 3+3=6 tall
    paint_cube(img, 0, 10, 3, 3, 3, belly, 12, seed=501)
    
    # Nose (texOffs 14,10, 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 14, 10, 1, 1, 1, nose, 8, seed=502)
    
    # Eyes on head front face
    d.ellipse([1, 12, 2, 13], fill=eye)
    d.ellipse([4, 12, 5, 13], fill=eye)
    
    # Whisker dots (just hints)
    d.point((0, 14), fill=(50, 50, 50, 255))
    d.point((6, 14), fill=(50, 50, 50, 255))
    
    # Ears (texOffs 14,14 and 20,14, each 1x4x1): uv_footprint = 4 wide, 1+4=5 tall
    paint_cube(img, 14, 14, 1, 4, 1, base, 10, seed=503)
    paint_cube(img, 20, 14, 1, 4, 1, base, 10, seed=504)
    # Inner ear pink (top face = inner ear for tall ear boxes)
    for py in range(14, min(15, 32)):
        for px in range(15, min(16, 32)):
            img.putpixel((px, py), ear_inner)
    for py in range(14, min(15, 32)):
        for px in range(21, min(22, 32)):
            img.putpixel((px, py), ear_inner)
    
    # Legs (texOffs 0,17 1x2x1; 5,17 1x2x1; 10,17 2x2x2; 20,17 2x2x2)
    paint_cube(img, 0, 17, 1, 2, 1, dark, 10, seed=510)
    paint_cube(img, 5, 17, 1, 2, 1, dark, 10, seed=511)
    paint_cube(img, 10, 17, 2, 2, 2, dark, 10, seed=512)
    paint_cube(img, 20, 17, 2, 2, 2, dark, 10, seed=513)
    
    # Tail (texOffs 28,10, 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 28, 10, 1, 1, 1, tail, 8, seed=520)
    
    # Spirit glow on body top
    for i in range(1, 5):
        img.putpixel((i, 1), spirit)
    
    return img


def make_fire_beast():
    """64x64 — wolf-like predator wreathed in flame, charcoal body, orange mane."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    body = (55, 25, 20, 255)        # dark charcoal-red
    belly = (75, 35, 25, 255)       # slightly lighter
    flame_core = (255, 180, 40, 255) # bright yellow-orange core
    flame_outer = (255, 100, 15, 255) # orange-red outer flame
    flame_tip = (255, 60, 10, 255)   # red flame tips
    ember_eye = (255, 230, 80, 255)  # blazing ember eye
    horn = (45, 22, 18, 255)         # dark horn
    jaw = (50, 22, 18, 255)          # dark jaw
    dark = (35, 18, 12, 255)         # darkest legs
    ash = (40, 35, 30, 255)          # ash/charcoal
    
    # Body (texOffs 0,0, 5x6x10): uv_footprint = 30 wide, 10+6=16 tall
    paint_cube(img, 0, 0, 5, 6, 10, body, 12, seed=600)
    # Belly lighter
    for py in range(0, min(10, 64)):
        for px in range(10, min(20, 64)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), belly)
    
    # Head skull (texOffs 0,18, 3x3x3): uv_footprint = 12 wide, 3+3=6 tall
    paint_cube(img, 0, 18, 3, 3, 3, body, 10, seed=601)
    # Upper jaw (texOffs 0,26, 3x1.5x2→3x2x2): uv_footprint = 10 wide, 2+2=4 tall
    paint_cube(img, 0, 26, 3, 2, 2, jaw, 10, seed=602)
    # Lower jaw (texOffs 28,18, 3x1x2): uv_footprint = 10 wide, 2+1=3 tall
    paint_cube(img, 28, 18, 3, 1, 2, jaw, 10, seed=603)
    
    # Eyes (texOffs 44,32 and 48,32, each 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 44, 32, 1, 1, 1, ember_eye, 5, seed=610)
    paint_cube(img, 48, 32, 1, 1, 1, ember_eye, 5, seed=611)
    # Extra bright center
    d.point((44, 32), fill=(255, 255, 200, 255))
    d.point((48, 32), fill=(255, 255, 200, 255))
    
    # Horns (texOffs 52,0 and 52,4, each 1x2x1): uv_footprint = 4 wide, 1+2=3 tall
    paint_cube(img, 52, 0, 1, 2, 1, horn, 8, seed=612)
    paint_cube(img, 52, 4, 1, 2, 1, horn, 8, seed=613)
    
    # Flame mane segments (texOffs 40, 0+i*6 for i=0..4, each 1x3x1): uv_footprint = 4 wide, 1+3=4 tall
    for i in range(5):
        y_off = i * 6
        paint_cube(img, 40, y_off, 1, 3, 1, flame_outer, 10, seed=620+i)
        # Hot core at center
        for py in range(y_off+1, min(y_off+3, 64)):
            for px in range(41, min(42, 64)):
                c = img.getpixel((px, py))
                img.putpixel((px, py), flame_core)
    
    # Tail base (texOffs 36,8, 1x1x3): uv_footprint = 8 wide, 3+1=4 tall
    paint_cube(img, 36, 8, 1, 1, 3, body, 10, seed=630)
    
    # Flame tip parts (texOffs 40,36/44,36/48,36): uv_footprint = 4 wide, 1+2=3 tall for 1x2x1
    paint_cube(img, 40, 36, 1, 3, 1, flame_core, 10, seed=631)
    paint_cube(img, 44, 36, 1, 2, 1, flame_outer, 10, seed=632)
    paint_cube(img, 48, 36, 1, 2, 1, flame_tip, 10, seed=633)
    
    # Legs (4 thighs + 4 shins, each 2x3x2): uv_footprint = 8 wide, 2+3=5 tall
    leg_positions = [(0,32),(0,40),(8,32),(8,40),(0,48),(0,56),(8,48),(8,56)]
    for i, (lx, ly) in enumerate(leg_positions):
        paint_cube(img, lx, ly, 2, 3, 2, dark, 10, seed=640+i)
    
    # Embers/ash scattered along spine
    for i in range(0, 30, 5):
        n = random.random()
        if n > 0.5:
            img.putpixel((i, 0), (255, 150 + random.randint(0, 80), 20, 200))
    
    # Teeth/fangs hint on jaw front face
    for px in range(29, 32):
        img.putpixel((px, 18), (240, 230, 220, 255))
    
    return img


def make_stone_back_boar():
    """64x64 — stocky boar with stone plate, dark brown fur, gray stone."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    fur = (95, 75, 55, 255)          # dark brown bristles
    belly = (125, 100, 75, 255)      # lighter underside
    stone_base = (135, 135, 130, 255) # gray stone
    stone_light = (160, 158, 150, 255) # lighter stone facet
    stone_dark = (95, 95, 90, 255)    # stone crack/shadow
    stone_moss = (105, 125, 95, 255)  # mossy seams
    tusk = (225, 215, 185, 255)      # yellowed ivory
    snout = (105, 80, 60, 255)        # pinkish-brown snout
    dark = (65, 50, 35, 255)          # darkest legs
    hoof = (50, 40, 30, 255)          # dark hooves
    eye = (30, 25, 20, 255)           # dark eye
    bristle = (75, 60, 45, 255)      # bristle ridge
    
    # Body (texOffs 0,0, 5x5x10): uv_footprint = 30 wide, 10+5=15 tall
    paint_cube(img, 0, 0, 5, 5, 10, fur, 15, seed=700)
    # Belly lighter
    for py in range(0, min(10, 64)):
        for px in range(10, min(20, 64)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), belly)
    
    # Stone plate (texOffs 40,0, 6x1x8): uv_footprint = 28 wide, 8+1=9 tall
    paint_cube(img, 40, 0, 6, 1, 8, stone_base, 15, seed=710)
    # Stone detail: cracks, moss, facets
    for i in range(42, 66, 3):
        y_start = random.randint(0, 8)
        for j in range(y_start, min(y_start + 2, 9)):
            if 0 <= j < 64:
                img.putpixel((i, j), stone_dark)
    # Moss line
    for i in range(42, 68):
        if i < 64:
            img.putpixel((i, 4), stone_moss)
    # Light facet highlights
    for i in range(42, 66, 5):
        for j in range(0, min(3, 64)):
            if i < 64:
                c = img.getpixel((i, j))
                img.putpixel((i, j), stone_light)
    
    # Head (texOffs 0,16, 3x3x3): uv_footprint = 12 wide, 3+3=6 tall
    paint_cube(img, 0, 16, 3, 3, 3, fur, 12, seed=701)
    # Snout (texOffs 0,24, 3x2x3): uv_footprint = 12 wide, 3+2=5 tall
    paint_cube(img, 0, 24, 3, 2, 3, snout, 12, seed=702)
    # Nostrils
    d.point((1, 25), fill=(40, 30, 25, 255))
    d.point((3, 25), fill=(40, 30, 25, 255))
    
    # Eye on head
    d.ellipse([1, 18, 2, 19], fill=eye)
    d.ellipse([4, 18, 5, 19], fill=eye)
    
    # Snout disc (texOffs 24,16, 3x1.5x1→3x2x1): uv_footprint = 8 wide, 1+2=3 tall
    paint_cube(img, 24, 16, 3, 2, 1, snout, 10, seed=703)
    # Nostrils on disc
    d.point((25, 16), fill=(40, 30, 25, 255))
    d.point((27, 16), fill=(40, 30, 25, 255))
    
    # Ears (texOffs 20,16 and 20,20, each 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 20, 16, 1, 1, 1, fur, 10, seed=704)
    paint_cube(img, 20, 20, 1, 1, 1, fur, 10, seed=705)
    
    # Tusks: base (texOffs 36,16 and 36,20, each 1x1.5x1→1x2x1): uv_footprint = 4 wide, 1+2=3 tall
    # tip (texOffs 40,16 and 40,20, each 1x1x1): uv_footprint = 4 wide, 1+1=2 tall
    paint_cube(img, 36, 16, 1, 2, 1, tusk, 5, seed=706)
    paint_cube(img, 40, 16, 1, 1, 1, tusk, 5, seed=707)
    paint_cube(img, 36, 20, 1, 2, 1, tusk, 5, seed=708)
    paint_cube(img, 40, 20, 1, 1, 1, tusk, 5, seed=709)
    
    # Tail (texOffs 40,24, 1x1x2): uv_footprint = 6 wide, 2+1=3 tall
    paint_cube(img, 40, 24, 1, 1, 2, bristle, 10, seed=730)
    
    # Legs (4 thighs + 4 shins, each 1.5x3x1.5→2x3x2): uv_footprint = 8 wide, 2+3=5 tall
    leg_positions = [(0,32),(0,38),(8,32),(8,38),(0,44),(0,50),(8,44),(8,50)]
    for i, (lx, ly) in enumerate(leg_positions):
        paint_cube(img, lx, ly, 2, 3, 2, dark, 12, seed=740+i)
        # Hooves on bottom
        for py in range(ly, min(ly+2, 64)):
            for px in range(lx+2, min(lx+4, 64)):
                img.putpixel((px, py), hoof)
    
    # Bristle ridge along spine (top face of body)
    for i in range(0, 10, 2):
        img.putpixel((i+1, 1), bristle)
    
    return img


def make_cultivator_default():
    """64x64 — cultivator NPC texture, extends vanilla player-skin UV layout."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    
    robe = (45, 55, 85, 255)        # dark blue-gray robe
    robe_pattern = (55, 65, 95, 255) # slightly lighter pattern lines
    robe_dark = (35, 42, 65, 255)    # darker robe folds
    skin = (215, 185, 155, 255)      # skin tone
    skin_shadow = (195, 165, 135, 255) # skin shadow
    hair = (30, 25, 20, 255)         # black hair
    sash = (130, 90, 65, 255)        # brown sash
    collar = (60, 70, 100, 255)      # collar trim
    white_inner = (200, 200, 210, 255) # white inner robe visible at collar
    
    # === VANILLA HUMANOID UV REGIONS ===
    
    # Head (vanilla: 0,0 8x8x8): uv_footprint = 32 wide, 8+8=16 tall
    # Fill head region with skin tone
    paint_cube(img, 0, 0, 8, 8, 8, skin, 8, seed=800)
    # Hair on top (top face of head cube = hair)
    for py in range(0, min(8, 64)):
        for px in range(8, min(24, 64)):
            img.putpixel((px, py), hair)
    # Hat overlay region (8,0 to 15,7)
    for py in range(0, min(8, 64)):
        for px in range(8, min(16, 64)):
            img.putpixel((px, py), hair)
    # Face features on front face (0,8 to 7,16)
    # Eyes
    d.ellipse([2, 10, 3, 11], fill=(30, 30, 30, 255))
    d.ellipse([5, 10, 6, 11], fill=(30, 30, 30, 255))
    # Eyebrows
    d.line([(1, 9), (4, 9)], fill=(20, 18, 15, 255), width=1)
    d.line([(4, 9), (7, 9)], fill=(20, 18, 15, 255), width=1)
    # Mouth
    d.line([(3, 13), (5, 13)], fill=(170, 130, 120, 255), width=1)
    # Nose hint
    d.point((4, 11), fill=skin_shadow)
    
    # Body (vanilla: 16,16 8x12x4): uv_footprint = 24 wide, 4+12=16 tall
    paint_cube(img, 16, 16, 8, 12, 4, robe, 12, seed=801)
    # Robe collar (top of body = white inner robe visible)
    for py in range(16, min(20, 64)):
        for px in range(20, min(28, 64)):
            img.putpixel((px, py), white_inner)
    # Collar trim
    for py in range(16, min(18, 64)):
        for px in range(16, min(20, 64)):
            img.putpixel((px, py), collar)
    # Sash at waist level (front face, waist = y offset ~28)
    for py in range(28, min(30, 64)):
        for px in range(16, min(24, 64)):
            img.putpixel((px, py), sash)
    # Robe vertical pattern lines
    for i in range(18, 24, 3):
        for py in range(20, min(32, 64)):
            if py < 64:
                img.putpixel((i, py), robe_pattern)
    
    # Arms (vanilla: 40,16 4x12x4 right arm; 32,48 4x12x4 left arm)
    paint_cube(img, 40, 16, 4, 12, 4, robe, 12, seed=802)
    paint_cube(img, 32, 48, 4, 12, 4, robe, 12, seed=803)
    # Arm cuff trim at wrist
    for py in range(26, min(28, 64)):
        for px in range(40, min(48, 64)):
            img.putpixel((px, py), sash)
    for py in range(58, min(60, 64)):
        for px in range(32, min(40, 64)):
            img.putpixel((px, py), sash)
    # Hands (bottom of arm = skin)
    for py in range(16, min(20, 64)):
        for px in range(44, min(48, 64)):
            img.putpixel((px, py), skin)
    for py in range(48, min(52, 64)):
        for px in range(36, min(40, 64)):
            img.putpixel((px, py), skin)
    
    # Legs (vanilla: 0,16 4x12x4; 16,48 8x12x4)
    paint_cube(img, 0, 16, 4, 12, 4, robe_dark, 10, seed=804)
    paint_cube(img, 16, 48, 8, 12, 4, robe_dark, 10, seed=805)
    # Boots at bottom of legs
    for py in range(16, min(20, 64)):
        for px in range(4, min(8, 64)):
            img.putpixel((px, py), (30, 25, 20, 255))
    for py in range(56, min(60, 64)):
        for px in range(24, min(32, 64)):
            img.putpixel((px, py), (30, 25, 20, 255))
    
    # === CUSTOM PARTS ===
    
    # Robe skirt (texOffs 16,32, 9x8x6): uv_footprint = 30 wide, 6+8=14 tall
    paint_cube(img, 16, 32, 9, 8, 6, robe, 15, seed=810)
    # Skirt bottom hem slightly darker
    for py in range(42, min(46, 64)):
        for px in range(16, min(46, 64)):
            c = img.getpixel((px, py))
            img.putpixel((px, py), robe_dark)
    # Skirt pattern: subtle vertical cloud/dao pattern
    for i in range(20, 44, 4):
        for py in range(38, min(46, 64)):
            if i < 64:
                img.putpixel((i, py), robe_pattern)
                if i+1 < 64:
                    img.putpixel((i+1, py), robe_pattern)
    
    # Hair bun (texOffs 0,32, 4x2x4): uv_footprint = 16 wide, 4+2=6 tall
    paint_cube(img, 0, 32, 4, 2, 4, hair, 8, seed=811)
    # Hairpin hint on top face
    for px in range(4, min(8, 64)):
        if 32 < 64:
            img.putpixel((px, 32), (100, 80, 40, 255))  # jade hairpin
    
    # Sleeves (texOffs 40,32 and 40,48, each 4x12x4): uv_footprint = 16 wide, 4+12=16 tall
    paint_cube(img, 40, 32, 4, 12, 4, robe, 12, seed=812)
    paint_cube(img, 40, 48, 4, 12, 4, robe, 12, seed=813)
    # Sleeve cuffs
    for py in range(42, min(44, 64)):
        for px in range(40, min(56, 64)):
            img.putpixel((px, py), sash)
    for py in range(58, min(60, 64)):
        for px in range(40, min(56, 64)):
            img.putpixel((px, py), sash)
    
    return img


# ==================== GENERATE ALL TEXTURES ====================

textures = {
    "beast/spirit_wolf": (make_spirit_wolf, 64),
    "beast/spirit_hawk": (make_spirit_hawk, 64),
    "beast/spirit_deer": (make_spirit_deer, 64),
    "beast/spirit_rabbit": (make_spirit_rabbit, 32),
    "beast/fire_beast": (make_fire_beast, 64),
    "beast/stone_back_boar": (make_stone_back_boar, 64),
    "cultivator/default": (make_cultivator_default, 64),
}

for name, (gen_func, expected_size) in textures.items():
    out_dir = os.path.join(BASE, os.path.dirname(name))
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, os.path.basename(name) + ".png")
    img = gen_func()
    assert img.size == (expected_size, expected_size), f"{name}: expected {expected_size}x{expected_size}, got {img.size}"
    img.save(out_path)
    print(f"Generated {out_path} ({img.size[0]}x{img.size[1]})")

print("\nAll 7 entity textures generated successfully.")
