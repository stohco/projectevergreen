#!/usr/bin/env python3
"""Generate P6 Restriction Altar resource files."""
import json, os, struct, zlib

BASE = "/home/z/my-project/forge-mod/src/main/resources"
ASSETS = f"{BASE}/assets/ergenverse"
DATA = f"{BASE}/data/ergenverse"

# ── Blockstate ──────────────────────────────────────────────────
os.makedirs(f"{ASSETS}/blockstates", exist_ok=True)
with open(f"{ASSETS}/blockstates/restriction_altar.json", "w") as f:
    json.dump({"variants": {
        "": { "model": "ergenverse:block/restriction_altar" }
    }}, f, indent=2)

# ── Block model (cube_column) ───────────────────────────────────
os.makedirs(f"{ASSETS}/models/block", exist_ok=True)
with open(f"{ASSETS}/models/block/restriction_altar.json", "w") as f:
    json.dump({
        "parent": "minecraft:block/cube_column",
        "textures": {
            "end": "ergenverse:block/restriction_altar_top",
            "side": "ergenverse:block/restriction_altar_side"
        }
    }, f, indent=2)

# ── Block item model ────────────────────────────────────────────
os.makedirs(f"{ASSETS}/models/item", exist_ok=True)
with open(f"{ASSETS}/models/item/restriction_altar.json", "w") as f:
    json.dump({
        "parent": "ergenverse:block/restriction_altar"
    }, f, indent=2)

# ── Procedural textures ────────────────────────────────────────
def make_png(w, h, pixels):
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)
    raw = b""
    for y in range(h):
        raw += b"\x00"
        for x in range(w):
            raw += bytes(pixels[y * w + x])
    sig = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    return sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b"")

os.makedirs(f"{ASSETS}/textures/block", exist_ok=True)
os.makedirs(f"{ASSETS}/textures/gui", exist_ok=True)

# Block side texture (16x16) — dark metal with glowing runes
w, h = 16, 16
pixels = []
for y in range(h):
    for x in range(w):
        # Dark obsidian-like base
        base_r, base_g, base_b = 30, 20, 40
        # Glowing purple rune lines
        if (x == 4 or x == 11) and 2 <= y <= 13:
            pixels.append((160, 80, 200, 255))
        elif y in (3, 7, 11) and 4 <= x <= 11:
            pixels.append((140, 60, 180, 255))
        elif (x == 7 or x == 8) and y in (3, 7, 11):
            pixels.append((180, 100, 220, 255))
        elif x == 0 or x == 15 or y == 0 or y == 15:
            pixels.append((15, 10, 20, 255))
        else:
            pixels.append((base_r, base_g, base_b, 255))
with open(f"{ASSETS}/textures/block/restriction_altar_side.png", "wb") as f:
    f.write(make_png(w, h, pixels))

# Block top texture (16x16) — ornate circle with 9-dot pattern
pixels = []
for y in range(h):
    for x in range(w):
        cx, cy = 7.5, 7.5
        dist = ((x-cx)**2 + (y-cy)**2)**0.5
        if dist < 1.5:
            pixels.append((200, 120, 255, 255))  # center glow
        elif dist < 3.0:
            pixels.append((120, 60, 180, 255))
        elif dist < 6.5:
            # 9 dots around the circle (3x3)
            angle = (x - cx) * 0.5
            if (x % 3 == 1 and y % 3 == 1) and dist < 6.0:
                pixels.append((160, 80, 220, 255))
            else:
                pixels.append((40, 25, 55, 255))
        elif x == 0 or x == 15 or y == 0 or y == 15:
            pixels.append((15, 10, 20, 255))
        else:
            pixels.append((35, 22, 48, 255))
with open(f"{ASSETS}/textures/block/restriction_altar_top.png", "wb") as f:
    f.write(make_png(w, h, pixels))

# GUI texture (176x166 + session progress bar + layer progress bar)
w, h = 200, 100
pixels = []
for y in range(h):
    for x in range(w):
        if x < 176 and y < 166:
            if x == 0 or x == 175 or y == 0 or y == 165:
                pixels.append((80, 40, 120, 255))
            elif x < 7 or x > 168 or y < 7 or y > 158:
                pixels.append((60, 30, 90, 255))
            else:
                pixels.append((25, 15, 35, 200))
        elif x < 228 and y < 17:
            # Session progress bar overlay (purple)
            pixels.append((140, 60, 200, 255))
        elif x < 138 and y < 25 and y >= 17:
            # Layer progress bar overlay (blue-purple)
            pixels.append((100, 50, 160, 255))
        else:
            pixels.append((0, 0, 0, 0))
with open(f"{ASSETS}/textures/gui/restriction_altar.png", "wb") as f:
    f.write(make_png(w, h, pixels))

# ── Loot table ──────────────────────────────────────────────────
os.makedirs(f"{DATA}/loot_tables/blocks", exist_ok=True)
with open(f"{DATA}/loot_tables/blocks/restriction_altar.json", "w") as f:
    json.dump({
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": "ergenverse:restriction_altar"
            }],
            "conditions": [{
                "condition": "minecraft:survives_explosion"
            }]
        }]
    }, f, indent=2)

# ── Crafting recipe (end-game: nether star + obsidian + gold) ────
os.makedirs(f"{DATA}/recipes/crafting", exist_ok=True)
with open(f"{DATA}/recipes/crafting/restriction_altar.json", "w") as f:
    json.dump({
        "type": "minecraft:crafting_shaped",
        "pattern": ["OOO", "OPO", "GGG"],
        "key": {
            "O": { "item": "minecraft:obsidian" },
            "P": { "item": "minecraft:nether_star" },
            "G": { "item": "minecraft:gold_block" }
        },
        "result": { "item": "ergenverse:restriction_altar", "count": 1 }
    }, f, indent=2)

# ── Lang entries ────────────────────────────────────────────────
LANG_FILE = f"{ASSETS}/lang/en_us.json"
with open(LANG_FILE, "r") as f:
    lang = json.load(f)

new_entries = {
    "block.ergenverse.restriction_altar": "Restriction Altar (\u7981\u5236\u53f0)",
    "container.ergenverse.restriction_altar": "Restriction Altar",
    "container.ergenverse.restriction_altar.inscribe": "Inscribe",
}

lang.update(new_entries)
with open(LANG_FILE, "w") as f:
    json.dump(lang, f, indent=2, ensure_ascii=False)

print("P6 resource files generated successfully.")
print(f"  Blockstate: 1, Block model: 1, Item model: 1")
print(f"  Block textures: 2 (side + top with glowing runes)")
print(f"  GUI texture: 1 (200x100 with dual progress bars)")
print(f"  Loot table: 1, Crafting recipe: 1 (nether star)")
print(f"  Lang entries: 3")