"""
Generate the World Origin Essence (一界本源) item texture — a 16x16 PNG.

Canon theme: the condensed origin of an entire world. Visually:
- Deep purple/navy core (the world's essence)
- Gold rim (the world's boundary)
- Light-purple swirl (the rules integrating into the world)
- Single bright pixel in the center (the world's'seed' / origin point)

This is a hand-crafted pixel-art texture, not algorithmic noise.
"""
from PIL import Image
import os

OUT = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/item/world_origin_essence.png"

# 16x16 RGBA
img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
px = img.load()

# Color palette
TRANSPARENT = (0, 0, 0, 0)
DEEP_VOID = (12, 8, 28, 255)        # outermost aura — void black-purple
DARK_PURPLE = (40, 20, 70, 255)     # outer ring — concentrated world-essence
MID_PURPLE = (90, 45, 140, 255)     # mid ring — flowing world-rules
LIGHT_PURPLE = (180, 130, 230, 255) # inner swirl — rules taking shape
GOLD = (255, 200, 80, 255)          # rim — the world's boundary
BRIGHT_GOLD = (255, 230, 140, 255)  # rim highlight
WHITE_CORE = (255, 255, 240, 255)   # center seed — the world's origin
GRAY_DUST = (140, 110, 170, 180)    # outer dust motes

# Pattern (16x16). Legend:
#   '.' = transparent
#   ' ' = void
#   'D' = dark purple
#   'M' = mid purple
#   'L' = light purple
#   'g' = gold
#   'G' = bright gold
#   'W' = white core (center)
#   ',' = gray dust
PATTERN = [
    ".....gggggg.....",
    "...ggDDDDDDgg...",
    "..gDDMMMMMMDDg..",
    ".gDDMLLLLLMMDDg.",
    ".gDMLLWLLWLLMDg.",
    "gDDMLLW,,WLLMDDg",
    "gDMLLW,WW,WLLMDg",
    "gDMLLW,WW,WLLMDg",
    "gDDMLLW,,WLLMDDg",
    ".gDMLLWLLWLLMDg.",
    ".gDDMLLLLLMDDg.",
    "..gDDMMMMMMDDg..",
    "...ggDDDDDDgg...",
    ".....ggGGgg.....",
    "......,gg,......",
    "................",
]

color_map = {
    '.': TRANSPARENT,
    ' ': DEEP_VOID,
    'D': DARK_PURPLE,
    'M': MID_PURPLE,
    'L': LIGHT_PURPLE,
    'g': GOLD,
    'G': BRIGHT_GOLD,
    'W': WHITE_CORE,
    ',': GRAY_DUST,
}

for y, row in enumerate(PATTERN):
    for x, ch in enumerate(row):
        if ch in color_map:
            px[x, y] = color_map[ch]

os.makedirs(os.path.dirname(OUT), exist_ok=True)
img.save(OUT)
print(f"Wrote {OUT} ({os.path.getsize(OUT)} bytes)")
print(f"Size: {img.size}, Mode: {img.mode}")
