#!/usr/bin/env python3
"""
AUTO-CANON-016: Wire Wang Family Village jigsaw pools + create 3 missing NBTs + 3 missing structure_sets.

Problem: 11 of 14 template pools point to vanilla NBTs instead of custom ones.
3 canon-critical NBTs (ancestral_hall, memorial_shrine, wang_lin_childhood_home) don't exist.
3 structure_set files are missing for those 3 structures.

Solution:
1. Fix 11 template pool JSONs to reference ergenverse:wang_family_village/<name> NBTs.
2. Create 3 small NBT files using binary NBT construction (mortal village buildings).
3. Create 3 structure_set JSONs.
"""
import os, json, struct, glob

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
POOL_DIR = os.path.join(BASE, "worldgen", "template_pool")
STRUCT_DIR = os.path.join(BASE, "worldgen", "structure")
STRUCT_SET_DIR = os.path.join(BASE, "worldgen", "structure_set")
NBT_DIR = os.path.join(BASE, "structures", "wang_family_village")

# ── LE NBT primitive writers ──

def le_str(name, val):
    d = struct.pack('<b', 8)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    venc = val.encode('utf-8')
    d += struct.pack('<H', len(venc)) + venc
    return d

def le_int(name, val):
    d = struct.pack('<b', 3)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', val)
    return d

def le_int_arr(name, vals):
    d = struct.pack('<b', 11)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', len(vals))
    for v in vals:
        d += struct.pack('<i', v)
    return d

def le_compound(name=None):
    d = struct.pack('<b', 10)
    if name is not None:
        enc = name.encode('utf-8')
        d += struct.pack('<H', len(enc)) + enc
    return d

def le_end():
    return struct.pack('<b', 0)

def le_list_start(name, elem_type):
    d = struct.pack('<b', 9)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<b', elem_type)
    return d

def le_list_empty(name, elem_type):
    """TAG_List with 0 elements."""
    return le_list_start(name, elem_type) + struct.pack('<i', 0)

def le_block_entry(pos, palette_idx, nbt_data=None):
    """One block entry: TAG_Compound { pos: [x,y,z], state: N, nbt?: {...} }."""
    d = le_compound()
    d += le_int_arr("pos", pos)
    d += le_int("state", palette_idx)
    if nbt_data:
        d += nbt_data
    d += le_end()
    return d

def build_nbt(palette, blocks, size, entities=None):
    """Build a complete MC 1.20.1 structure NBT file.
    palette: list of block names (strings)
    blocks: list of (pos, palette_idx, optional_nbt_bytes)
    size: [x, y, z]
    entities: list of entity compound bytes (optional)
    """
    data = le_compound("")  # root
    data += le_int("DataVersion", 3465)  # MC 1.20.1
    data += le_int_arr("size", size)

    # Build palette
    data += le_list_start("palette", 10)  # TAG_Compound = type 10
    data += struct.pack('<i', len(palette))
    for block_name in palette:
        data += le_compound()
        data += le_str("Name", block_name)
        data += le_end()
    # (no Properties for vanilla blocks without states)

    # Build blocks
    data += le_list_start("blocks", 10)  # TAG_Compound = type 10
    data += struct.pack('<i', len(blocks))
    for entry in blocks:
        pos = entry[0]
        pidx = entry[1]
        nbt = entry[2] if len(entry) > 2 else None
        data += le_block_entry(pos, pidx, nbt)

    # Entities (empty)
    if entities is None:
        data += le_list_empty("entities", 10)
    else:
        data += le_list_start("entities", 10)
        data += struct.pack('<i', len(entities))
        for ent in entities:
            data += ent

    data += le_end()  # close root compound
    return data

# ── Palette helpers ──

def make_palette(block_names):
    """Simple palette — one entry per unique block name."""
    return block_names

# ── Structure builders ──

def build_ancestral_hall():
    """Ancestral Hall (祠堂) — mossy stone brick building with soul_sand spirit tablets.
    Canon: Where village elders speak of the lost generation. Wang family lineage.
    Modest 9x6x7 building.
    """
    P = make_palette([
        "minecraft:mossy_stone_bricks",
        "minecraft:chiseled_stone_bricks",
        "minecraft:dark_oak_log",
        "minecraft:dark_oak_planks",
        "minecraft:soul_sand",
        "minecraft:soul_lantern",
        "minecraft:stone_brick_stairs",
        "minecraft:air",
        "minecraft:dark_oak_door",
        "minecraft:polished_andesite",
        "minecraft:dark_oak_fence",
    ])
    p = P.index  # shorthand

    blocks = []
    # Floor (y=0) — polished_andesite foundation
    for x in range(9):
        for z in range(7):
            blocks.append(([x, 0, z], p("minecraft:polished_andesite")))

    # Walls (y=1-4) — mossy_stone_bricks with dark_oak_log pillars at corners
    for y in range(1, 5):
        for x in range(9):
            for z in range(7):
                if x == 0 or x == 8 or z == 0 or z == 6:
                    # Corner pillars
                    if (x == 0 or x == 8) and (z == 0 or z == 6):
                        blocks.append(([x, y, z], p("minecraft:dark_oak_log")))
                    # Walls
                    else:
                        blocks.append(([x, y, z], p("minecraft:mossy_stone_bricks")))
                # Interior is air (y=1-3)
                elif y <= 3:
                    blocks.append(([x, y, z], p("minecraft:air")))

    # Roof (y=4-5) — dark_oak_planks
    for x in range(9):
        for z in range(7):
            blocks.append(([x, 4, z], p("minecraft:dark_oak_planks")))
    # Ridge
    for x in range(1, 8):
        blocks.append(([x, 5, 3], p("minecraft:dark_oak_planks")))

    # Door opening (y=1-2 at x=4, z=0) — air
    blocks.append(([4, 1, 0], p("minecraft:air")))
    blocks.append(([4, 2, 0], p("minecraft:air")))

    # Altar (y=1, center) — chiseled_stone_bricks
    for x in range(3, 6):
        for z in range(2, 5):
            blocks.append(([x, 1, z], p("minecraft:chiseled_stone_bricks")))

    # Soul lantern on altar
    blocks.append(([4, 2, 3], p("minecraft:soul_lantern")))

    # Spirit tablets (soul_sand) along back wall (z=5, y=1)
    for x in range(2, 7):
        blocks.append(([x, 1, 5], p("minecraft:soul_sand")))

    # Dark oak fence railings along sides
    for y_off in [1, 2]:
        for z in range(1, 6):
            blocks.append(([1, y_off, z], p("minecraft:dark_oak_fence")))
            blocks.append(([7, y_off, z], p("minecraft:dark_oak_fence")))

    # Front steps
    for z in [-1, -2]:
        for x in range(3, 6):
            blocks.append(([x, 0, z], p("minecraft:stone_brick_stairs")))

    return build_nbt(P, blocks, [9, 6, 7])


def build_memorial_shrine():
    """Memorial Shrine — small stone shrine at the massacre center.
    Canon: Wang Lin returns as Soul Formation to bury the dead (RI Ch. ~50).
    Small 5x4x5 shrine with a central memorial.
    """
    P = make_palette([
        "minecraft:stone_bricks",
        "minecraft:chiseled_stone_bricks",
        "minecraft:polished_blackstone",
        "minecraft:soul_sand",
        "minecraft:soul_lantern",
        "minecraft:air",
        "minecraft:dark_oak_fence",
        "minecraft:oxidized_copper",
    ])
    p = P.index

    blocks = []
    # Floor (y=0) — stone_bricks
    for x in range(5):
        for z in range(5):
            blocks.append(([x, 0, z], p("minecraft:stone_bricks")))

    # Walls (y=1-3)
    for y in range(1, 4):
        for x in range(5):
            for z in range(5):
                if x == 0 or x == 4 or z == 0 or z == 4:
                    blocks.append(([x, y, z], p("minecraft:stone_bricks")))
                elif y <= 2:
                    blocks.append(([x, y, z], p("minecraft:air")))

    # Roof (y=3)
    for x in range(5):
        for z in range(5):
            blocks.append(([x, 3, z], p("minecraft:polished_blackstone")))

    # Door opening
    blocks.append(([2, 1, 0], p("minecraft:air")))
    blocks.append(([2, 2, 0], p("minecraft:air")))

    # Central memorial (chiseled_stone_bricks)
    blocks.append(([2, 1, 2], p("minecraft:chiseled_stone_bricks")))
    # Soul sand at base
    blocks.append(([2, 0, 2], p("minecraft:soul_sand")))
    # Soul lantern above
    blocks.append(([2, 2, 2], p("minecraft:soul_lantern")))

    # Soul lanterns in corners
    blocks.append(([1, 1, 1], p("minecraft:soul_lantern")))
    blocks.append(([3, 1, 1], p("minecraft:soul_lantern")))
    blocks.append(([1, 1, 3], p("minecraft:soul_lantern")))
    blocks.append(([3, 1, 3], p("minecraft:soul_lantern")))

    # Fence around memorial
    for x in range(1, 4):
        blocks.append(([x, 1, 1], p("minecraft:dark_oak_fence")))
        blocks.append(([x, 1, 3], p("minecraft:dark_oak_fence")))
    for z in range(1, 4):
        blocks.append(([1, 1, z], p("minecraft:dark_oak_fence")))
        blocks.append(([3, 1, z], p("minecraft:dark_oak_fence")))

    # Oxidized copper accents on corners (aged, neglected)
    for pos in [(0, 1, 0), (4, 1, 0), (0, 1, 4), (4, 1, 4)]:
        blocks.append((pos, p("minecraft:oxidized_copper")))

    return build_nbt(P, blocks, [5, 4, 5])


def build_childhood_home():
    """Wang Lin's Childhood Home — modest ruined dwelling.
    Canon: Wang family carpentry home. Partially ruined (post-massacre).
    Simple 7x4x6 house with chest + hidden compartment.
    """
    P = make_palette([
        "minecraft:oak_planks",
        "minecraft:oak_log",
        "minecraft:cobblestone",
        "minecraft:air",
        "minecraft:oak_stairs",
        "minecraft:dirt",
        "minecraft:stone_bricks",
        "minecraft:dark_oak_planks",  # bed
        "minecraft:white_wool",  # bed
        "minecraft:oak_fence",
        "minecraft:glass_pane",
        "minecraft:crafting_table",
    ])
    p = P.index

    blocks = []
    # Floor (y=0) — cobblestone
    for x in range(7):
        for z in range(6):
            blocks.append(([x, 0, z], p("minecraft:cobblestone")))

    # Walls (y=1-3) — oak_planks, some gaps for "ruined" feel
    for y in range(1, 4):
        for x in range(7):
            for z in range(6):
                if x == 0 or x == 6 or z == 0 or z == 5:
                    # Corner logs
                    if (x == 0 or x == 6) and (z == 0 or z == 5):
                        blocks.append(([x, y, z], p("minecraft:oak_log")))
                    else:
                        blocks.append(([x, y, z], p("minecraft:oak_planks")))
                elif y <= 2:
                    blocks.append(([x, y, z], p("minecraft:air")))

    # Roof (y=3) — oak_planks with gaps (ruined)
    for x in range(7):
        for z in range(6):
            # Some roof tiles missing to show ruin
            if (x + z) % 3 != 0:
                blocks.append(([x, 3, z], p("minecraft:oak_planks")))
            else:
                blocks.append(([x, 3, z], p("minecraft:air")))

    # Door opening (x=3, z=0, y=1-2)
    blocks.append(([3, 1, 0], p("minecraft:air")))
    blocks.append(([3, 2, 0], p("minecraft:air")))

    # Bed (y=1, corner: x=1, z=1)
    blocks.append(([1, 1, 1], p("minecraft:dark_oak_planks")))  # bed foot
    blocks.append(([1, 1, 2], p("minecraft:white_wool")))  # bed head

    # Crafting table (carpentry family!)
    blocks.append(([5, 1, 4], p("minecraft:crafting_table")))

    # Window (y=2, z=0, x=1) — glass_pane
    blocks.append(([1, 2, 0], p("minecraft:glass_pane")))

    # Hidden compartment under floor (trapdoor area at x=4, z=3)
    # The dirt block that hides the compartment
    blocks.append(([4, 0, 3], p("minecraft:dirt")))

    # Front step
    blocks.append(([3, 0, -1], p("minecraft:oak_stairs")))

    # Oak fence (garden)
    for z in [-1]:
        for x in range(1, 6):
            if x != 3:
                blocks.append(([x, 1, z], p("minecraft:oak_fence")))

    return build_nbt(P, blocks, [7, 4, 6])


# ── Template pool fixer ──

# Map: pool directory name -> correct NBT location
POOL_TO_NBT = {
    "wang_family_village": "ergenverse:wang_family_village/main",
    "wang_family_village_city_gate": "ergenverse:wang_family_village/city_gate",
    "wang_family_village_port_docks": "ergenverse:wang_family_village/port_docks",
    "wang_family_village_smuggler_tunnels": "ergenverse:wang_family_village/smuggler_tunnels",
    "wang_family_village_tavern_district": "ergenverse:wang_family_village/tavern_district",
    "wang_family_village_governor_mansion": "ergenverse:wang_family_village/governor_mansion",
    "wang_family_village_residential_district": "ergenverse:wang_family_village/residential_district",
    "wang_family_village_temple_district": "ergenverse:wang_family_village/temple_district",
    "wang_family_village_cultivator_quarter": "ergenverse:wang_family_village/cultivator_quarter",
    "wang_family_village_mortal_quarter": "ergenverse:wang_family_village/mortal_quarter",
    "wang_family_village_market_district": "ergenverse:wang_family_village/market_district",
    "wang_family_village_warehouse_district": "ergenverse:wang_family_village/warehouse_district",
}

def fix_template_pools():
    """Rewrite 11 template pools to point to correct custom NBTs."""
    fixed = 0
    for pool_name, nbt_location in POOL_TO_NBT.items():
        pool_path = os.path.join(POOL_DIR, pool_name, "start_pool.json")
        if not os.path.exists(pool_path):
            print(f"  SKIP (not found): {pool_path}")
            continue

        with open(pool_path, 'r') as f:
            data = json.load(f)

        elem = data["elements"][0]
        # Handle both nested and flat element formats
        old_location = elem.get("element", elem).get("location")
        if old_location == nbt_location:
            print(f"  OK (already correct): {pool_name}")
            continue

        print(f"  FIX: {pool_name}")
        print(f"    OLD: {old_location}")
        print(f"    NEW: {nbt_location}")

        elem = data["elements"][0]
        if "element" in elem:
            elem["element"]["location"] = nbt_location
        else:
            elem["location"] = nbt_location

        with open(pool_path, 'w') as f:
            json.dump(data, f, indent=2)
            f.write("\n")
        fixed += 1

    return fixed


def create_structure_sets():
    """Create 3 missing structure_set files."""
    sets_to_create = [
        ("wang_family_village_ancestral_hall", "ergenverse:wang_family_village_ancestral_hall", 42, 10, 88231),
        ("wang_family_village_memorial_shrine", "ergenverse:wang_family_village_memorial_shrine", 38, 8, 55277),
        ("wang_family_village_wang_lin_childhood_home", "ergenverse:wang_family_village_wang_lin_childhood_home", 35, 8, 77314),
    ]

    created = 0
    for filename, structure_ref, spacing, separation, salt in sets_to_create:
        filepath = os.path.join(STRUCT_SET_DIR, f"{filename}.json")
        if os.path.exists(filepath):
            print(f"  SKIP (exists): {filepath}")
            continue

        data = {
            "structures": [
                {"structure": structure_ref, "weight": 1}
            ],
            "placement": {
                "type": "minecraft:random_spread",
                "spacing": spacing,
                "separation": separation,
                "salt": salt
            }
        }

        with open(filepath, 'w') as f:
            json.dump(data, f, indent=2)
            f.write("\n")
        print(f"  CREATE: {filepath}")
        created += 1

    return created


def create_nbt_files():
    """Create the 3 missing NBT files."""
    builders = [
        ("ancestral_hall.nbt", build_ancestral_hall),
        ("memorial_shrine.nbt", build_memorial_shrine),
        ("wang_lin_childhood_home.nbt", build_childhood_home),
    ]

    created = 0
    for filename, builder in builders:
        filepath = os.path.join(NBT_DIR, filename)
        if os.path.exists(filepath):
            print(f"  SKIP (exists): {filepath}")
            continue

        nbt_data = builder()
        with open(filepath, 'wb') as f:
            f.write(nbt_data)
        size_kb = len(nbt_data) / 1024
        print(f"  CREATE: {filepath} ({size_kb:.1f} KB)")
        created += 1

    return created


if __name__ == "__main__":
    print("=== AUTO-CANON-016: Wire Village Structures ===\n")

    print("--- Step 1: Fix template pools ---")
    pools_fixed = fix_template_pools()

    print("\n--- Step 2: Create missing NBT files ---")
    nbt_created = create_nbt_files()

    print("\n--- Step 3: Create missing structure_set files ---")
    sets_created = create_structure_sets()

    print(f"\n=== DONE: {pools_fixed} pools fixed, {nbt_created} NBTs created, {sets_created} structure_sets created ===")