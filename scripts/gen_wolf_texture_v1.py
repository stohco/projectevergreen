#!/usr/bin/env python3
"""
gen_wolf_texture_v1.py — CRON-COMPLETIONIST-82
Generate spirit_wolf.png with intentional anatomical patterns.

Spirit wolf color palette (Renegade Immortal canon):
- Dorsal surface: blue-gray spirit fur (supernatural blue tint distinguishes
  spirit beasts from mortal wolves — qi-infused coat)
- Ventral surface: cream/off-white (lighter belly, classic countershading)
- Shoulders: darker gray-blue guard hair band (visible muscle definition)
- Ear tips: dark navy (almost black, like real wolf ear tipping)
- Chest blaze: white/cream diamond (spirit marking)
- Eye region: amber-gold glow (spirit eyes, rendered via emissive)
- Ruff/neck: thicker lighter fur ring (mane-like neck ruff)
- Legs: darker distal coloring (legs darken toward paws)
- Tail tip: dark (like real wolves)

Uses FBM noise for organic variation WITH intentional anatomical zones.
"""
import numpy as np
from PIL import Image
import sys, os

# ── Noise functions ──

def hash2d(x, y, seed=0):
    """Integer hash to [0,1] for deterministic noise."""
    n = (x * 374761393 + y * 668265263 + seed * 1274126177) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return (n & 0x7FFFFFFF) / 0x7FFFFFFF

def smooth_noise(x, y, seed=0):
    """Smoothed 2D value noise."""
    ix, iy = int(np.floor(x)), int(np.floor(y))
    fx, fy = x - ix, y - iy
    # Smoothstep
    fx = fx * fx * (3.0 - 2.0 * fx)
    fy = fy * fy * (3.0 - 2.0 * fy)
    v00 = hash2d(ix, iy, seed)
    v10 = hash2d(ix+1, iy, seed)
    v01 = hash2d(ix, iy+1, seed)
    v11 = hash2d(ix+1, iy+1, seed)
    return (v00*(1-fx)*(1-fy) + v10*fx*(1-fy) + v01*(1-fx)*fy + v11*fx*fy)

def fbm(x, y, octaves=6, seed=0, lacunarity=2.0, gain=0.5):
    """Fractal Brownian Motion — layered noise for organic variation."""
    value = 0.0
    amplitude = 1.0
    frequency = 1.0
    for _ in range(octaves):
        value += amplitude * smooth_noise(x * frequency, y * frequency, seed)
        amplitude *= gain
        frequency *= lacunarity
    return value

# ── Color palette ──

# Spirit wolf has a supernatural blue-gray tint from qi infusion.
# This distinguishes it from mortal wolves (which would be brown/gray).

PALETTE = {
    'dorsal':    np.array([95, 110, 140]),   # Blue-gray (spirit qi tint)
    'ventral':   np.array([210, 205, 195]),  # Cream/off-white
    'guard':     np.array([60, 70, 100]),     # Dark blue-gray (shoulder guard hair)
    'ear_tip':   np.array([30, 35, 55]),      # Very dark navy
    'chest':     np.array([225, 220, 210]),   # Light cream (chest blaze)
    'ruff':      np.array([140, 145, 160]),   # Lighter neck ruff
    'eye_ring':  np.array([180, 140, 50]),    # Amber (spirit eye glow)
    'leg_dark':  np.array([55, 60, 85]),      # Darker distal legs
    'tail_tip':  np.array([40, 45, 70]),      # Dark tail tip
    'nose':      np.array([25, 25, 30]),      # Near-black nose
    'fang':      np.array([240, 238, 230]),   # White fangs
}

def lerp_color(c1, c2, t):
    """Linear interpolation between two colors."""
    t = np.clip(t, 0, 1)
    return c1 * (1 - t) + c2 * t

def generate_wolf_texture(width=64, height=64, seed=42):
    """Generate the wolf texture with anatomical zones + FBM noise."""
    img = np.zeros((height, width, 3), dtype=np.uint8)
    
    for y in range(height):
        for x in range(width):
            # Normalized coordinates [0, 1]
            nx = x / width
            ny = y / height
            
            # FBM noise for organic variation (3 different scales)
            n1 = fbm(nx * 4.0, ny * 4.0, octaves=5, seed=seed)
            n2 = fbm(nx * 8.0, ny * 8.0, octaves=4, seed=seed+100)
            n3 = fbm(nx * 16.0, ny * 16.0, octaves=3, seed=seed+200)
            
            # Fine detail noise for fur texture
            fur_noise = fbm(nx * 32.0, ny * 32.0, octaves=2, seed=seed+300) * 0.15
            
            # ── Anatomical zone determination ──
            # The texture is mapped to the wolf model parts:
            # - Top half (y < 32): body_chest, body_hip, neck
            # - Bottom half (y >= 32): legs, tail, head details
            # But the UV mapping is complex, so we use position-based zones.
            
            # Dorsal-ventral gradient: top=darker (back), bottom=lighter (belly)
            # For a quadruped: y maps roughly to dorsal(0)-ventral(1) on the sides
            dorsal_weight = 1.0 - ny  # top of texture = dorsal
            ventral_weight = ny       # bottom of texture = ventral
            
            # Base color from dorsal-ventral gradient
            base_color = lerp_color(PALETTE['dorsal'], PALETTE['ventral'], ventral_weight ** 0.7)
            
            # ── Shoulder guard hair band (y ≈ 0.15-0.25, x ≈ 0.3-0.7) ──
            # Darker band across the upper-middle representing thick guard hair
            guard_dist = abs(ny - 0.18) / 0.08
            if guard_dist < 1.0 and 0.2 < nx < 0.8:
                guard_factor = (1.0 - guard_dist) * 0.6
                base_color = lerp_color(base_color, PALETTE['guard'], guard_factor)
            
            # ── Neck ruff (y ≈ 0.25-0.35, x ≈ 0.35-0.65) ──
            # Lighter fur around the neck area
            ruff_dist = abs(ny - 0.30) / 0.06
            if ruff_dist < 1.0 and 0.30 < nx < 0.70:
                ruff_factor = (1.0 - ruff_dist) * 0.5
                base_color = lerp_color(base_color, PALETTE['ruff'], ruff_factor)
            
            # ── Chest blaze (y ≈ 0.35-0.50, x ≈ 0.40-0.60) ──
            # White/cream diamond marking on chest (spirit marking)
            chest_cx = (nx - 0.50) / 0.12
            chest_cy = (ny - 0.42) / 0.08
            chest_dist = abs(chest_cx) + abs(chest_cy)  # diamond shape
            if chest_dist < 1.0:
                chest_factor = (1.0 - chest_dist) * 0.7
                base_color = lerp_color(base_color, PALETTE['chest'], chest_factor)
            
            # ── Leg darkening (lower portion of texture, y > 0.7) ──
            if ny > 0.7:
                leg_factor = (ny - 0.7) / 0.3
                base_color = lerp_color(base_color, PALETTE['leg_dark'], leg_factor * 0.4)
            
            # ── Tail tip darkening (right edge, y ≈ 0.3-0.4) ──
            if nx > 0.9 and 0.25 < ny < 0.45:
                tail_factor = (nx - 0.9) / 0.1
                base_color = lerp_color(base_color, PALETTE['tail_tip'], tail_factor * 0.5)
            
            # ── Ear tip darkening (top corners) ──
            if ny < 0.1:
                ear_factor = (1.0 - ny / 0.1)
                # Left ear region
                if nx < 0.15:
                    base_color = lerp_color(base_color, PALETTE['ear_tip'], ear_factor * 0.8)
                # Right ear region
                if nx > 0.85:
                    base_color = lerp_color(base_color, PALETTE['ear_tip'], ear_factor * 0.8)
            
            # ── Eye region glow (y ≈ 0.4-0.5, x ≈ 0.42-0.48 and 0.52-0.58) ──
            # Amber glow around eye positions (enhanced by emissive renderer)
            for eye_cx in [0.45, 0.55]:
                eye_dist = ((nx - eye_cx) / 0.04) ** 2 + ((ny - 0.44) / 0.03) ** 2
                if eye_dist < 1.0:
                    eye_factor = (1.0 - eye_dist) * 0.4
                    base_color = lerp_color(base_color, PALETTE['eye_ring'], eye_factor)
            
            # ── Nose (bottom-center, y ≈ 0.48-0.52, x ≈ 0.47-0.53) ──
            nose_dist = ((nx - 0.50) / 0.03) ** 2 + ((ny - 0.50) / 0.02) ** 2
            if nose_dist < 1.0:
                nose_factor = (1.0 - nose_dist) * 0.9
                base_color = lerp_color(base_color, PALETTE['nose'], nose_factor)
            
            # ── Apply FBM noise variation ──
            # Independent R/G/B noise channels for rich color variation
            # (prevents the "everything shifts equally" flat look)
            n_r = fbm(nx * 5.0 + 0.1, ny * 5.0, octaves=5, seed=seed+500) 
            n_g = fbm(nx * 5.0 + 0.3, ny * 5.0, octaves=5, seed=seed+600)
            n_b = fbm(nx * 5.0 + 0.5, ny * 5.0, octaves=5, seed=seed+700)
            
            # Fine grain noise per channel
            fn_r = fbm(nx * 24.0, ny * 24.0, octaves=2, seed=seed+800) * 0.3
            fn_g = fbm(nx * 24.0 + 7.0, ny * 24.0, octaves=2, seed=seed+900) * 0.3
            fn_b = fbm(nx * 24.0 + 14.0, ny * 24.0, octaves=2, seed=seed+1000) * 0.3
            
            # Directional fur streaks (vertical streaks simulating guard hair)
            fur_streak = fbm(nx * 3.0, ny * 25.0, octaves=2, seed=seed+400) * 0.08
            
            # Per-channel noise offsets create richer color space
            r_var = (n_r * 0.2 + fn_r + fur_streak - 0.18) * 70
            g_var = (n_g * 0.18 + fn_g + fur_streak * 0.8 - 0.16) * 65
            b_var = (n_b * 0.15 + fn_b + fur_streak * 0.6 - 0.14) * 60
            
            # Apply noise as per-channel color shift
            img[y, x, 0] = np.clip(base_color[0] + r_var, 0, 255)
            img[y, x, 1] = np.clip(base_color[1] + g_var, 0, 255)
            img[y, x, 2] = np.clip(base_color[2] + b_var, 0, 255)
    
    return img

def main():
    output_dir = os.path.join(os.path.dirname(__file__), '..', 
                              'src', 'main', 'resources', 'assets', 'ergenverse',
                              'textures', 'entity', 'beast')
    output_path = os.path.join(output_dir, 'spirit_wolf.png')
    
    # Generate texture
    print("Generating spirit_wolf.png (64x64)...")
    img_array = generate_wolf_texture(64, 64, seed=42)
    
    # Save
    img = Image.fromarray(img_array, 'RGB')
    img.save(output_path)
    
    # Stats
    colors = len(img.getcolors(maxcolors=999999) or [])
    print(f"Saved to {output_path}")
    print(f"Size: {img.size}")
    print(f"Unique colors: {colors}")
    
    # Compare with original
    orig_path = os.path.join(output_dir, 'spirit_wolf.png')
    if os.path.exists(orig_path):
        # Backup original
        backup_path = os.path.join(output_dir, 'spirit_wolf_original_v81.png')
        if not os.path.exists(backup_path):
            try:
                orig_img = Image.open(orig_path)
                orig_img.save(backup_path)
                orig_colors = len(orig_img.getcolors(maxcolors=999999) or [])
                print(f"Backed up original ({orig_colors} colors) to {os.path.basename(backup_path)}")
            except Exception as e:
                print(f"Backup skipped: {e}")

if __name__ == '__main__':
    main()
