#!/usr/bin/env python3
"""
Downscale AI-generated 1024x1024 textures to 32x32 Minecraft pixel art.

Recovered from chat transcript (Gemini U158) — lost in sandbox reset on July 14.
Uses the IMPROVED pipeline (Gemini's revised version) with:
  1. Hybrid resize: LANCZOS to 128x128 (smooth), then NEAREST to 32x32 (crisp)
  2. Contrast / color / brightness enhancement for small-screen vibrancy
  3. 32-color FASTOCTREE quantization with no dithering (clean edges)
  4. Flood-fill background removal from corner (0,0) — protects internal dark pixels
     (the original global-threshold approach punched holes through shadowed item interiors)

Usage:
    python3 scripts/downscale_textures.py

Reads from:  src/main/resources/assets/ergenverse/textures/item/_ai_raw/*.png
Writes to:   src/main/resources/assets/ergenverse/textures/item/<name>.png
"""

from PIL import Image, ImageEnhance
import os

RAW_DIR = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/item/_ai_raw"
TEX_DIR = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/textures/item"


def downscale_to_pixelart(raw_path, out_path, size=32):
    """Downscale to pixel art with crisp edges (Gemini-improved version)."""
    img = Image.open(raw_path).convert('RGBA')

    # 1. Hybrid Resize: Smooth reduction first, then hard pixel lock
    img_mid = img.resize((size * 4, size * 4), Image.LANCZOS)
    img_small = img_mid.resize((size, size), Image.NEAREST)

    # 2. Punch up contrast/color for small screens
    img_small = ImageEnhance.Contrast(img_small).enhance(1.3)
    img_small = ImageEnhance.Color(img_small).enhance(1.2)
    img_small = ImageEnhance.Brightness(img_small).enhance(1.05)

    # 3. Quantize with clean edge preservation
    alpha = img_small.split()[3]
    img_rgb = img_small.convert('RGB')
    img_rgb = img_rgb.quantize(colors=32, method=Image.Quantize.FASTOCTREE, dither=Image.Dither.NONE)
    img_small = img_rgb.convert('RGBA')
    img_small.putalpha(alpha)

    # 4. Flood-fill background removal (protects internal dark pixels)
    # Assumes background touches the top-left corner (0,0)
    bg_color = img_small.getpixel((0, 0))
    # Threshold check to allow slight variation in background color
    mask = Image.new('L', (size, size), 0)
    pixels = img_small.load()

    for y in range(size):
        for x in range(size):
            r, g, b, a = pixels[x, y]
            # Check deviation from top-left pixel color
            if abs(r - bg_color[0]) < 25 and abs(g - bg_color[1]) < 25 and abs(b - bg_color[2]) < 25:
                mask.putpixel((x, y), 255)

    # Apply transparency only to the isolated background mask
    for y in range(size):
        for x in range(size):
            if mask.getpixel((x, y)) == 255:
                pixels[x, y] = (0, 0, 0, 0)

    img_small.save(out_path, 'PNG')
    return os.path.getsize(out_path)


# Process all AI raw textures
raws = sorted([f for f in os.listdir(RAW_DIR) if f.lower().endswith('.png') or f.lower().endswith('.jpg') or f.lower().endswith('.jpeg')])
print(f"Processing {len(raws)} AI textures -> 32x32 pixel art...")
print()

for raw_name in raws:
    # Normalize the output name (strip any extension)
    item_name = os.path.splitext(raw_name)[0]
    raw_path = os.path.join(RAW_DIR, raw_name)
    out_path = os.path.join(TEX_DIR, f"{item_name}.png")

    raw_size = os.path.getsize(raw_path)
    out_size = downscale_to_pixelart(raw_path, out_path)
    print(f"  {item_name:40s}  {raw_size//1024:4d}KB -> {out_size:5d}B")

print()
print(f"Done! {len(raws)} textures downscaled to 32x32 pixel art.")

# Verify the results
print("\n=== Final texture sizes ===")
for raw_name in raws:
    item_name = os.path.splitext(raw_name)[0]
    out_path = os.path.join(TEX_DIR, f"{item_name}.png")
    size = os.path.getsize(out_path)
    img = Image.open(out_path)
    print(f"  {item_name:40s}  {size:5d}B  {img.size}")
