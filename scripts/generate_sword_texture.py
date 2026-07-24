#!/usr/bin/env python3
"""Generate flying sword texture (32x32)."""
from PIL import Image, ImageDraw
import os, random
random.seed(42)

BASE = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/entity"

img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
d = ImageDraw.Draw(img)

# Steel blade (texOffs 0,0, 1.2x5x1.2): uv_footprint = 2*(1+1)=4 wide, 1+5=6 tall
steel = (180, 185, 195, 255)
steel_light = (210, 215, 225, 255)  # blade edge highlight
steel_dark = (140, 145, 155, 255)   # blade shadow
for y in range(0, 6):
    for x in range(0, 4):
        n = random.randint(-8, 8)
        c = (max(0,min(255,steel[0]+n)), max(0,min(255,steel[1]+n)), max(0,min(255,steel[2]+n)), 255)
        img.putpixel((x, y), c)
# Edge highlight (bright line along one side)
d.line([(1, 0), (1, 5)], fill=steel_light, width=1)
# Fuller (blood groove) — dark line down center
d.line([(2, 1), (2, 4)], fill=steel_dark, width=1)

# Guard (texOffs 8,0, 5x1x1): uv_footprint = 2*(5+1)=12 wide, 1+1=2 tall
guard = (140, 100, 50, 255)  # bronze/copper guard
guard_dark = (100, 70, 35, 255)
for y in range(0, 2):
    for x in range(8, 20):
        n = random.randint(-5, 5)
        c = (max(0,min(255,guard[0]+n)), max(0,min(255,guard[1]+n)), max(0,min(255,guard[2]+n)), 255)
        img.putpixel((x, y), c)
# Guard edge detail
d.line([(8, 0), (19, 0)], fill=guard_dark, width=1)
d.line([(8, 1), (19, 1)], fill=guard_dark, width=1)

# Handle (texOffs 0,8, 1x3x1): uv_footprint = 2*(1+1)=4 wide, 1+3=4 tall
handle_leather = (70, 45, 30, 255)    # dark leather
handle_wrap = (90, 60, 40, 255)       # lighter wrap highlight
for y in range(8, 12):
    for x in range(0, 4):
        c = handle_leather if (y % 2 == 0) else handle_wrap
        img.putpixel((x, y), c)

# Pommel (texOffs 0,16, 1.2x1x1.2): uv_footprint = 2*(1+1)=4 wide, 1+1=2 tall
pommel = (100, 80, 55, 255)  # bronze pommel
for y in range(16, 18):
    for x in range(0, 4):
        img.putpixel((x, y), pommel)
# Pommel gem highlight
d.point((1, 16), fill=(180, 140, 80, 255))

# Tassel (texOffs 4,16, 0.5x3x0.5): uv_footprint = 2*(1+1)=4 wide, 1+3=4 tall
tassel_red = (180, 40, 30, 255)  # Wang Lin's signature red tassel
tassel_dark = (140, 30, 20, 255)
for y in range(16, 20):
    for x in range(4, 8):
        c = tassel_red if (y % 2 == 0) else tassel_dark
        img.putpixel((x, y), c)

out_path = os.path.join(BASE, "flying_sword.png")
img.save(out_path)
print(f"Generated {out_path} (32x32)")
