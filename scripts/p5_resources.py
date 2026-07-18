#!/usr/bin/env python3
"""Generate P5 Talisman Desk resource files: blockstate, models, textures, loot, recipe, lang."""
import json, os, struct, zlib

BASE = "/home/z/my-project/forge-mod/src/main/resources"
ASSETS = f"{BASE}/assets/ergenverse"
DATA = f"{BASE}/data/ergenverse"

# ── Blockstate ──────────────────────────────────────────────────
os.makedirs(f"{ASSETS}/blockstates", exist_ok=True)
with open(f"{ASSETS}/blockstates/talisman_desk.json", "w") as f:
    json.dump({"variants": {
        "": { "model": "ergenverse:block/talisman_desk" }
    }}, f, indent=2)

# ── Block model (cube_column) ───────────────────────────────────
os.makedirs(f"{ASSETS}/models/block", exist_ok=True)
with open(f"{ASSETS}/models/block/talisman_desk.json", "w") as f:
    json.dump({
        "parent": "minecraft:block/cube_column",
        "textures": {
            "end": "ergenverse:block/talisman_desk_top",
            "side": "ergenverse:block/talisman_desk_side"
        }
    }, f, indent=2)

# ── Block item model ────────────────────────────────────────────
os.makedirs(f"{ASSETS}/models/item", exist_ok=True)
with open(f"{ASSETS}/models/item/talisman_desk.json", "w") as f:
    json.dump({
        "parent": "ergenverse:block/talisman_desk"
    }, f, indent=2)

# ── Item models for 17 output/input items ──────────────────────
ITEM_NAMES = [
    "blank_talisman_paper", "blank_jade_slip", "spirit_ink",
    "fireball_talisman", "lightning_talisman", "sword_qi_talisman",
    "shield_talisman", "barrier_talisman", "teleport_talisman",
    "invisibility_talisman", "aura_suppression_talisman",
    "light_talisman", "water_breathing_talisman",
    "technique_jade_slip", "map_jade_slip", "message_jade_slip",
    "inheritance_jade_slip"
]

for name in ITEM_NAMES:
    with open(f"{ASSETS}/models/item/{name}.json", "w") as f:
        json.dump({
            "parent": "minecraft:item/generated",
            "textures": { "layer0": f"ergenverse:item/{name}" }
        }, f, indent=2)

# ── Procedural textures ────────────────────────────────────────
def make_png(w, h, pixels):
    """Create a minimal PNG from raw RGBA pixels."""
    def chunk(ctype, data):
        c = ctype + data
        return struct.pack(">I", len(data)) + c + struct.pack(">I", zlib.crc32(c) & 0xFFFFFFFF)
    
    raw = b""
    for y in range(h):
        raw += b"\x00"  # filter none
        for x in range(w):
            raw += bytes(pixels[y * w + x])
    
    sig = b"\x89PNG\r\n\x1a\n"
    ihdr = struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0)
    return sig + chunk(b"IHDR", ihdr) + chunk(b"IDAT", zlib.compress(raw)) + chunk(b"IEND", b"")

# Color palette for talismans
TALISMAN_COLORS = {
    "fireball_talisman": (255, 80, 40),
    "lightning_talisman": (255, 255, 80),
    "sword_qi_talisman": (180, 200, 255),
    "shield_talisman": (80, 160, 255),
    "barrier_talisman": (60, 100, 200),
    "teleport_talisman": (120, 80, 255),
    "invisibility_talisman": (200, 200, 220),
    "aura_suppression_talisman": (140, 180, 140),
    "light_talisman": (255, 255, 200),
    "water_breathing_talisman": (80, 180, 255),
}

os.makedirs(f"{ASSETS}/textures/block", exist_ok=True)
os.makedirs(f"{ASSETS}/textures/item", exist_ok=True)
os.makedirs(f"{ASSETS}/textures/gui", exist_ok=True)

# Block textures (16x16)
def make_block_texture(r, g, b, filename):
    w, h = 16, 16
    pixels = []
    for y in range(h):
        for x in range(w):
            if filename.endswith("_top"):
                # Top: ornate grid pattern
                if (x % 8 == 0 or y % 8 == 0):
                    pixels.append((r//2, g//2, b//2, 255))
                elif (x == 7 or x == 8) and (y >= 4 and y <= 11):
                    pixels.append((min(255,r+60), min(255,g+60), min(255,b+60), 255))
                else:
                    pixels.append((r, g, b, 255))
            else:
                # Side: wood-like with inscription lines
                if y < 2 or y > 13:
                    pixels.append((r//3, g//3, b//3, 255))
                elif y in (5, 10):
                    pixels.append((min(255,r+40), min(255,g+40), min(255,b+40), 255))
                else:
                    pixels.append((r, g, b, 255))
    with open(filename, "wb") as f:
        f.write(make_png(w, h, pixels))

make_block_texture(100, 70, 40, f"{ASSETS}/textures/block/talisman_desk_side.png")
make_block_texture(130, 90, 50, f"{ASSETS}/textures/block/talisman_desk_top.png")

# Item textures (16x16)
def make_item_texture(r, g, b, filename, pattern="talisman"):
    w, h = 16, 16
    pixels = []
    for y in range(h):
        for x in range(w):
            # Diamond/rune shape on transparent background
            cx, cy = 7.5, 7.5
            dx, dy = abs(x - cx), abs(y - cy)
            if pattern == "paper":
                # Paper: rectangle with fold
                if 2 <= x <= 13 and 3 <= y <= 12:
                    pixels.append((240, 230, 210, 255))
                elif x == 2 and 3 <= y <= 12:
                    pixels.append((200, 190, 170, 255))
                else:
                    pixels.append((0, 0, 0, 0))
            elif pattern == "ink":
                # Ink bottle: small filled circle
                dist = ((x-7.5)**2 + (y-9)**2)**0.5
                if dist < 3.5:
                    pixels.append((r, g, b, 255))
                elif 4 <= x <= 11 and y >= 10:
                    pixels.append((r//2, g//2, b//2, 255))
                else:
                    pixels.append((0, 0, 0, 0))
            elif pattern == "jade":
                # Jade slip: tall rectangle with green tint
                if 4 <= x <= 11 and 1 <= y <= 14:
                    edge = (x == 4 or x == 11)
                    if edge:
                        pixels.append((r//2, g//2, b//2, 255))
                    elif y in (1, 14):
                        pixels.append((min(255,r+30), min(255,g+30), min(255,b+30), 255))
                    else:
                        pixels.append((r, g, b, 255))
                else:
                    pixels.append((0, 0, 0, 0))
            else:
                # Talisman: diamond shape
                if dx + dy < 6.5:
                    # Inner rune lines
                    if (y == 7 and 4 <= x <= 11):
                        pixels.append((min(255,r+80), min(255,g+80), min(255,b+80), 255))
                    else:
                        pixels.append((r, g, b, 255))
                elif dx + dy < 7.5:
                    pixels.append((r//2, g//2, b//2, 255))
                else:
                    pixels.append((0, 0, 0, 0))
    with open(filename, "wb") as f:
        f.write(make_png(w, h, pixels))

# Input textures
make_item_texture(240, 230, 210, f"{ASSETS}/textures/item/blank_talisman_paper.png", "paper")
make_item_texture(100, 200, 120, f"{ASSETS}/textures/item/blank_jade_slip.png", "jade")
make_item_texture(20, 10, 30, f"{ASSETS}/textures/item/spirit_ink.png", "ink")

# Talisman textures
for name, color in TALISMAN_COLORS.items():
    make_item_texture(*color, f"{ASSETS}/textures/item/{name}.png", "talisman")

# Jade slip textures (varying green/blue/purple)
JADE_COLORS = {
    "technique_jade_slip": (80, 180, 120),
    "map_jade_slip": (100, 160, 140),
    "message_jade_slip": (120, 140, 180),
    "inheritance_jade_slip": (160, 100, 200),
}
for name, color in JADE_COLORS.items():
    make_item_texture(*color, f"{ASSETS}/textures/item/{name}.png", "jade")

# ── GUI texture (176x166 + progress bar overlay) ─────────────────
def make_gui_texture(filename):
    w, h = 200, 166
    pixels = []
    for y in range(h):
        for x in range(w):
            if x < 176 and y < 166:
                # Background: warm parchment
                if x == 0 or x == 175 or y == 0 or y == 165:
                    pixels.append((60, 40, 20, 255))
                elif x < 7 or x > 168 or y < 7 or y > 158:
                    pixels.append((140, 110, 70, 255))
                else:
                    # Subtle grid
                    if (x % 18 == 8 or y % 18 == 8) and x < 170 and y < 154:
                        pixels.append((100, 80, 50, 180))
                    else:
                        pixels.append((180, 150, 100, 230))
            else:
                # Progress bar overlay area (x=176..199, y=0..16)
                if x >= 176 and y < 17:
                    pixels.append((200, 100, 50, 255))  # orange for talisman
                else:
                    pixels.append((0, 0, 0, 0))
    with open(filename, "wb") as f:
        f.write(make_png(w, h, pixels))

make_gui_texture(f"{ASSETS}/textures/gui/talisman_desk.png")

# ── Loot table ──────────────────────────────────────────────────
os.makedirs(f"{DATA}/loot_tables/blocks", exist_ok=True)
with open(f"{DATA}/loot_tables/blocks/talisman_desk.json", "w") as f:
    json.dump({
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{
                "type": "minecraft:item",
                "name": "ergenverse:talisman_desk"
            }],
            "conditions": [{
                "condition": "minecraft:survives_explosion"
            }]
        }]
    }, f, indent=2)

# ── Crafting recipe (vanilla grid: planks + ink + gold) ─────────
os.makedirs(f"{DATA}/recipes/crafting", exist_ok=True)
with open(f"{DATA}/recipes/crafting/talisman_desk.json", "w") as f:
    json.dump({
        "type": "minecraft:crafting_shaped",
        "pattern": ["WWW", "IGI", "PPP"],
        "key": {
            "W": { "item": "minecraft:oxidized_copper" },
            "I": { "item": "minecraft:ink_sac" },
            "G": { "item": "minecraft:gold_ingot" },
            "P": { "item": "minecraft:dark_oak_planks" }
        },
        "result": { "item": "ergenverse:talisman_desk", "count": 1 }
    }, f, indent=2)

# Also add crafting recipes for the input materials
with open(f"{DATA}/recipes/crafting/blank_talisman_paper.json", "w") as f:
    json.dump({
        "type": "minecraft:crafting_shaped",
        "pattern": ["PP", "PP"],
        "key": { "P": { "item": "minecraft:paper" } },
        "result": { "item": "ergenverse:blank_talisman_paper", "count": 4 }
    }, f, indent=2)

with open(f"{DATA}/recipes/crafting/blank_jade_slip.json", "w") as f:
    json.dump({
        "type": "minecraft:crafting_shaped",
        "pattern": [" G ", "GJG", " G "],
        "key": {
            "G": { "item": "minecraft:gold_nugget" },
            "J": { "item": "minecraft:emerald" }
        },
        "result": { "item": "ergenverse:blank_jade_slip", "count": 1 }
    }, f, indent=2)

with open(f"{DATA}/recipes/crafting/spirit_ink.json", "w") as f:
    json.dump({
        "type": "minecraft:crafting_shaped",
        "pattern": [" B ", "BIB", " B "],
        "key": {
            "B": { "item": "minecraft:black_dye" },
            "I": { "item": "minecraft:ink_sac" }
        },
        "result": { "item": "ergenverse:spirit_ink", "count": 2 }
    }, f, indent=2)

# ── Lang entries ────────────────────────────────────────────────
LANG_FILE = f"{ASSETS}/lang/en_us.json"
with open(LANG_FILE, "r") as f:
    lang = json.load(f)

new_entries = {
    "block.ergenverse.talisman_desk": "Talisman Desk (\u7b26\u53f0)",
    "container.ergenverse.talisman_desk": "Talisman Desk",
    "container.ergenverse.talisman_desk.craft": "Inscribe",
    
    # Input materials
    "item.ergenverse.blank_talisman_paper": "Blank Talisman Paper (\u7b26\u7eb8)",
    "item.ergenverse.blank_jade_slip": "Blank Jade Slip (\u7a7a\u7389\u7b80)",
    "item.ergenverse.spirit_ink": "Spirit Ink (\u7075\u58a8)",
    
    # Talismans
    "item.ergenverse.fireball_talisman": "Fireball Talisman (\u706b\u7403\u7b26)",
    "item.ergenverse.lightning_talisman": "Lightning Talisman (\u5f15\u96f7\u7b26)",
    "item.ergenverse.sword_qi_talisman": "Sword-Qi Talisman (\u5251\u6c14\u7b26)",
    "item.ergenverse.shield_talisman": "Shield Talisman (\u62a4\u76fe\u7b26)",
    "item.ergenverse.barrier_talisman": "Barrier Talisman (\u7ed3\u754c\u7b26)",
    "item.ergenverse.teleport_talisman": "Teleport Talisman (\u4f20\u9001\u7b26)",
    "item.ergenverse.invisibility_talisman": "Invisibility Talisman (\u9690\u8eab\u7b26)",
    "item.ergenverse.aura_suppression_talisman": "Aura-Suppression Talisman (\u655b\u606f\u7b26)",
    "item.ergenverse.light_talisman": "Light Talisman (\u7167\u660e\u7b26)",
    "item.ergenverse.water_breathing_talisman": "Water-Breathing Talisman (\u907f\u6c34\u7b26)",
    
    # Jade Slips
    "item.ergenverse.technique_jade_slip": "Technique Jade Slip (\u529f\u6cd5\u7389\u7b80)",
    "item.ergenverse.map_jade_slip": "Map Jade Slip (\u5730\u56fe\u7389\u7b80)",
    "item.ergenverse.message_jade_slip": "Message Jade Slip (\u4f20\u8baf\u7389\u7b80)",
    "item.ergenverse.inheritance_jade_slip": "Inheritance Jade Slip (\u4f20\u627f\u7389\u7b80)",
}

lang.update(new_entries)
with open(LANG_FILE, "w") as f:
    json.dump(lang, f, indent=2, ensure_ascii=False)

print("P5 resource files generated successfully.")
print(f"  Blockstate: 1")
print(f"  Block models: 1")
print(f"  Item models: 18")
print(f"  Block textures: 2")
print(f"  Item textures: 17")
print(f"  GUI texture: 1 (200x166)")
print(f"  Loot tables: 1")
print(f"  Crafting recipes: 4 (desk + 3 inputs)")
print(f"  Lang entries: 18")