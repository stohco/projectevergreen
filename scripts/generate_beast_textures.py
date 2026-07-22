#!/usr/bin/env python3
"""
Generate beast textures matching the custom model UV layouts.

Each model's createBodyLayer() defines texOffs() calls that map specific
pixel regions of the texture to specific model parts. This script generates
a texture with:
  - Base fur/skin color per beast type
  - Subtle color variation (darker belly, lighter highlights)
  - Spirit markings (faint luminous lines) on a separate alpha channel region
  - Part delineation via slight color differences at UV region boundaries

HARSH SELF-CRITIQUE (pre-emptive):
  - These are procedurally generated flat-color textures. Real creature textures
    have fur direction, subsurface scattering, wet/dry variation, wear marks,
    scar tissue, and individual identity. Mine are uniform flat fills.
  - "Spirit markings" are just brighter colored rectangles — they should be
    glowing rune-like patterns, not brighter boxes.
  - No normal map, no specular map, no detail texture. Minecraft's default
    lighting is the only shading these will ever get.
  - Each beast species should have color morphs (young vs. old, male vs. female,
    region variants). This script generates ONE texture per species.
  - The fire beast should have an emissive texture map for the flame mane.
    This script just paints orange boxes. Not even close to fire.
  - The stone back boar needs a stone plate texture that looks geological —
    cracks, moss, lichen. Mine is a gray box with slightly different gray lines.

This is the MINIMUM VIABLE TEXTURE so the models don't render as purple/black
missing-texture boxes. A real artist should replace every pixel.
"""

from PIL import Image, ImageDraw
import os
import random

BASE = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/entity"

def make_spirit_wolf():
    """64x64 — lean quadruped predator, silver-blue fur, spirit glow."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Base fur: silver-blue (#7B8FA2)
    base = (123, 143, 162, 255)
    belly = (170, 185, 200, 255)  # lighter underside
    dark = (80, 95, 110, 255)     # darker accents
    spirit = (140, 200, 255, 200) # spirit glow lines
    # Body (texOffs 0,0 4x6x10)
    d.rectangle([0, 0, 31, 15], fill=base)
    # Neck (texOffs 28,0 2x2x3)
    d.rectangle([28, 0, 39, 5], fill=base)
    # Head skull (texOffs 0,18 3x3x3) — slightly lighter face
    d.rectangle([0, 18, 11, 27], fill=belly)
    # Snout (texOffs 0,26 2x2x2) — dark nose
    d.rectangle([0, 26, 7, 31], fill=dark)
    # Ears (texOffs 20,18 / 20,24) — inner ear pink
    d.rectangle([20, 18, 27, 23], fill=base)
    d.rectangle([20, 18, 23, 21], fill=(180, 150, 160, 255))  # inner ear
    # Jaw (texOffs 28,18)
    d.rectangle([28, 18, 39, 21], fill=belly)
    # Fangs (texOffs 40,0 / 40,4) — white
    d.rectangle([40, 0, 47, 3], fill=(240, 240, 235, 255))
    d.rectangle([40, 4, 47, 7], fill=(240, 240, 235, 255))
    # Tail segments (texOffs 36,8/14/20)
    for y in [8, 14, 20]:
        d.rectangle([36, y, 43, y+5], fill=base)
    # Legs — 4 thighs + 4 shins, 8 texOffs regions
    for x_off, y_off in [(0,32),(0,40),(8,32),(8,40),(0,48),(0,56),(8,48),(8,56)]:
        d.rectangle([x_off, y_off, x_off+7, y_off+7], fill=dark)
    # Spirit markings: faint blue lines on body
    for i in range(0, 30, 4):
        d.line([(i, 0), (i, 15)], fill=spirit, width=1)
    return img

def make_spirit_hawk():
    """64x64 — raptor, brown-gold feathers, 3-segment wings."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Base: dark brown-gold (#8B7355)
    base = (139, 115, 85, 255)
    wing = (120, 100, 70, 255)
    feather = (100, 85, 60, 255)
    beak = (60, 40, 20, 255)      # dark hooked beak
    crest = (160, 130, 90, 255)    # lighter crest
    eye = (200, 180, 50, 255)     # golden eye
    talon = (80, 60, 40, 255)
    spirit = (180, 160, 100, 160)
    # Body (texOffs 0,0 6x4x6)
    d.rectangle([0, 0, 23, 15], fill=base)
    # Head (texOffs 24,0 3x3x3) + beak (24,8 1x1x2) + crest (36,0 2x1x1)
    d.rectangle([24, 0, 35, 11], fill=crest)
    d.rectangle([24, 8, 31, 13], fill=beak)
    d.rectangle([36, 0, 43, 3], fill=crest)
    # Left wing: shoulder(0,16) forearm(0,24) hand(0,32) feathers(20,16/20/24)
    d.rectangle([0, 16, 19, 19], fill=wing)
    d.rectangle([0, 24, 19, 27], fill=wing)
    d.rectangle([0, 32, 19, 35], fill=wing)
    for y in [16, 20, 24]:
        d.rectangle([20, y, 51, y+3], fill=feather)
    # Right wing: shoulder(0,40) forearm(0,48) hand(0,56) feathers(20,40/44/48)
    d.rectangle([0, 40, 19, 43], fill=wing)
    d.rectangle([0, 48, 19, 51], fill=wing)
    d.rectangle([0, 56, 19, 59], fill=wing)
    for y in [40, 44, 48]:
        d.rectangle([20, y, 51, y+3], fill=feather)
    # Tail feathers (40,16/24/32)
    for y in [16, 24, 32]:
        d.rectangle([40, y, 47, y+5], fill=base)
    # Legs + talons (50,16 through 50,50)
    for y in range(16, 50, 6):
        d.rectangle([50, y, 59, y+5], fill=base)
        d.rectangle([50, y+3, 59, y+5], fill=talon)
    # Spirit glow on wing edges
    for i in range(20, 52, 2):
        d.line([(i, 16), (i, 19)], fill=spirit, width=1)
    return img

def make_spirit_deer():
    """64x64 — long-necked grazer, tawny brown, branched antlers."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    base = (160, 130, 90, 255)      # tawny brown
    belly = (190, 170, 130, 255)    # cream underside
    dark = (120, 95, 65, 255)       # darker legs/points
    antler = (180, 170, 150, 255)   # bone-colored antlers
    nose = (70, 50, 35, 255)
    spirit = (200, 180, 130, 140)
    # Body (texOffs 0,0 3x5x8)
    d.rectangle([0, 0, 15, 15], fill=base)
    # Neck (texOffs 22,0 1x4x1)
    d.rectangle([22, 0, 27, 7], fill=base)
    # Head (texOffs 0,16 2x3x2) + snout (8,16 2x1x2)
    d.rectangle([0, 16, 7, 23], fill=belly)
    d.rectangle([8, 16, 15, 19], fill=nose)
    # Ears (texOffs 20,16 / 20,20) — pink inner
    d.rectangle([20, 16, 27, 19], fill=base)
    d.rectangle([20, 16, 23, 18], fill=(200, 170, 160, 255))
    d.rectangle([20, 20, 27, 23], fill=base)
    # Antlers: main beams (28,16/24) + tines (36,16/20/24/28)
    for y in [16, 24]:
        d.rectangle([28, y, 35, y+7], fill=antler)
    for y in [16, 20, 24, 28]:
        d.rectangle([36, y, 43, y+3], fill=antler)
    # Tail (texOffs 40,0)
    d.rectangle([40, 0, 47, 3], fill=belly)
    # Legs — 4 thighs + 4 shins (y=28 to y=55)
    for x_off in [0, 8]:
        for y_off in [28, 34, 40, 46]:
            d.rectangle([x_off, y_off, x_off+7, y_off+5], fill=dark)
    # Spirit marking: faint golden line along spine
    d.line([(2, 2), (13, 2)], fill=spirit, width=1)
    d.line([(2, 4), (13, 4)], fill=spirit, width=1)
    return img

def make_spirit_rabbit():
    """32x32 — small round lagomorph, grey-brown fur."""
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    base = (150, 140, 130, 255)    # grey-brown
    belly = (180, 175, 165, 255)   # lighter belly
    ear_inner = (200, 170, 160, 255)
    nose = (180, 130, 130, 255)
    dark = (110, 100, 90, 255)
    spirit = (180, 200, 220, 140)
    # Body (texOffs 0,0 4x4x5)
    d.rectangle([0, 0, 15, 9], fill=base)
    # Head (texOffs 0,10 3x3x3) + nose (14,10 1x1x1)
    d.rectangle([0, 10, 11, 21], fill=belly)
    d.rectangle([14, 10, 19, 13], fill=nose)
    # Ears (texOffs 14,14 / 20,14)
    d.rectangle([14, 14, 19, 25], fill=base)
    d.rectangle([14, 14, 16, 20], fill=ear_inner)  # inner ear
    d.rectangle([20, 14, 25, 25], fill=base)
    d.rectangle([20, 14, 22, 20], fill=ear_inner)
    # Legs: front (0,17 / 5,17) back (10,17 / 20,17)
    for x in [0, 5, 10, 20]:
        d.rectangle([x, 17, x+3, 21], fill=dark)
    # Tail (texOffs 28,10)
    d.rectangle([28, 10, 31, 13], fill=belly)
    # Spirit glow
    d.line([(1, 1), (14, 1)], fill=spirit, width=1)
    return img

def make_fire_beast():
    """64x64 — wolf-like predator wreathed in flame, charcoal-red body, orange mane."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    body = (60, 25, 20, 255)       # dark charcoal-red
    belly = (80, 35, 25, 255)
    flame = (255, 120, 20, 255)    # orange fire
    flame_hot = (255, 200, 50, 255)  # yellow-hot fire
    ember_eye = (255, 220, 80, 255)  # bright ember
    horn = (40, 20, 15, 255)       # dark horn
    jaw = (50, 20, 15, 255)
    dark = (35, 15, 10, 255)
    # Body (texOffs 0,0 5x6x10)
    d.rectangle([0, 0, 39, 15], fill=body)
    # Flame mane segments (texOffs 40,0+i*6 for i=0..4)
    for i in range(5):
        y = i * 6
        d.rectangle([40, y, 47, y+5], fill=flame)
        d.rectangle([42, y, 45, y+3], fill=flame_hot)  # hotter center
    # Neck (texOffs 28,0 3x3x3)
    d.rectangle([28, 0, 39, 11], fill=body)
    # Head skull (texOffs 0,18 3x3x3) + upper jaw (0,26 3x1.5x2)
    d.rectangle([0, 18, 11, 31], fill=body)
    d.rectangle([0, 26, 11, 31], fill=jaw)
    # Lower jaw (texOffs 28,18)
    d.rectangle([28, 18, 39, 23], fill=jaw)
    # Eyes (texOffs 44,32 / 48,32) — bright ember
    d.rectangle([44, 32, 47, 35], fill=ember_eye)
    d.rectangle([48, 32, 51, 35], fill=ember_eye)
    # Horns (texOffs 52,0 / 52,4)
    d.rectangle([52, 0, 59, 7], fill=horn)
    d.rectangle([52, 4, 59, 11], fill=horn)
    # Tail base (texOffs 36,8) + flame tip (40,36/44,36/48,36)
    d.rectangle([36, 8, 43, 15], fill=body)
    d.rectangle([40, 36, 47, 43], fill=flame)
    d.rectangle([44, 36, 51, 43], fill=flame_hot)
    d.rectangle([48, 36, 55, 43], fill=flame)
    # Legs — 4 thighs + 4 shins
    for x_off in [0, 8]:
        for y_off in [32, 40, 48, 56]:
            d.rectangle([x_off, y_off, x_off+7, y_off+7], fill=dark)
    # Embers along spine
    for i in range(0, 38, 3):
        d.point((i, 0), fill=ember_eye)
    return img

def make_stone_back_boar():
    """64x64 — stocky boar with stone plate, dark brown, gray stone."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    fur = (90, 70, 50, 255)        # dark brown bristles
    belly = (120, 95, 70, 255)     # lighter underside
    stone = (130, 130, 125, 255)   # gray stone
    stone_dark = (90, 90, 85, 255)  # stone cracks
    stone_moss = (100, 120, 90, 255)  # mossy seams
    tusk = (220, 210, 180, 255)   # yellowed ivory
    snout = (100, 75, 55, 255)
    dark = (60, 45, 30, 255)
    # Body (texOffs 0,0 5x5x10)
    d.rectangle([0, 0, 39, 15], fill=fur)
    # Stone plate (texOffs 40,0 6x1x8) — gray with crack lines
    d.rectangle([40, 0, 63, 7], fill=stone)
    # crack lines on stone
    for i in range(42, 62, 4):
        d.line([(i, 0), (i+2, 7)], fill=stone_dark, width=1)
    d.line([(40, 3), (63, 4)], fill=stone_moss, width=1)
    # Head (texOffs 0,16 3x3x3) + snout (0,24 3x2x3)
    d.rectangle([0, 16, 11, 27], fill=fur)
    d.rectangle([0, 24, 11, 31], fill=snout)
    # Snout disc (texOffs 24,16 3x1.5x1)
    d.rectangle([24, 16, 35, 21], fill=snout)
    # Ears (texOffs 20,16 / 20,20)
    d.rectangle([20, 16, 23, 19], fill=fur)
    d.rectangle([20, 20, 23, 23], fill=fur)
    # Tusks (texOffs 36,16 / 40,16 / 36,20 / 40,20) — ivory
    for x, y in [(36,16),(40,16),(36,20),(40,20)]:
        d.rectangle([x, y, x+3, y+3], fill=tusk)
    # Tail (texOffs 40,24)
    d.rectangle([40, 24, 47, 29], fill=fur)
    # Legs — 4 thighs + 4 shins (y=32 to y=55)
    for x_off in [0, 8]:
        for y_off in [32, 38, 44, 50]:
            d.rectangle([x_off, y_off, x_off+7, y_off+5], fill=dark)
    return img

def make_cultivator_default():
    """64x64 — cultivator robe texture, extends vanilla player-skin UV layout."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    # Colors
    robe = (50, 60, 90, 255)       # dark blue-gray robe
    robe_light = (70, 80, 110, 255)
    skin = (210, 180, 150, 255)    # skin tone
    hair = (30, 25, 20, 255)       # black hair
    sash = (120, 80, 60, 255)      # brown sash/belt
    # Head (vanilla UV: 0,0 8x8x8) — skin
    d.rectangle([0, 0, 7, 7], fill=skin)
    d.rectangle([8, 0, 15, 7], fill=skin)  # hat overlay
    # Body (vanilla UV: 16,16 8x12x4) — robe
    d.rectangle([16, 16, 31, 27], fill=robe)
    # Robe decoration: sash at waist
    d.rectangle([16, 24, 31, 26], fill=sash)
    # Arms (vanilla UV: 40,16 / 32,48) — robe sleeves
    d.rectangle([40, 16, 47, 31], fill=robe)
    d.rectangle([32, 48, 39, 63], fill=robe)
    # Legs (vanilla UV: 0,16 / 16,48) — dark pants
    d.rectangle([0, 16, 7, 31], fill=(40, 40, 50, 255))
    d.rectangle([16, 48, 31, 63], fill=(40, 40, 50, 255))
    # Robe skirt (our custom: texOffs 16,32 9x8x6) — matches robe color
    d.rectangle([16, 32, 43, 47], fill=robe)
    # Robe pattern: subtle vertical lines
    for i in range(18, 42, 3):
        d.line([(i, 32), (i, 47)], fill=robe_light, width=1)
    # Hair bun (our custom: texOffs 0,32 4x2x4) — black
    d.rectangle([0, 32, 11, 39], fill=hair)
    # Sleeves (our custom: texOffs 40,32 / 40,48) — robe
    d.rectangle([40, 32, 55, 47], fill=robe)
    d.rectangle([40, 48, 55, 63], fill=robe)
    return img

# Generate all textures
textures = {
    "beast/spirit_wolf": (make_spirit_wolf, (64, 64)),
    "beast/spirit_hawk": (make_spirit_hawk, (64, 64)),
    "beast/spirit_deer": (make_spirit_deer, (64, 64)),
    "beast/spirit_rabbit": (make_spirit_rabbit, (32, 32)),
    "beast/fire_beast": (make_fire_beast, (64, 64)),
    "beast/stone_back_boar": (make_stone_back_boar, (64, 64)),
    "cultivator/default": (make_cultivator_default, (64, 64)),
}

for name, (gen_func, expected_size) in textures.items():
    out_dir = os.path.join(BASE, os.path.dirname(name))
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, os.path.basename(name) + ".png")
    img = gen_func()
    assert img.size == expected_size, f"{name}: expected {expected_size}, got {img.size}"
    img.save(out_path)
    print(f"Generated {out_path} ({img.size[0]}x{img.size[1]})")

print("All beast + cultivator textures generated.")