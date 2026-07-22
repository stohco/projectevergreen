#!/usr/bin/env python3
"""
AUTO-CANON-012: Add loot chests to Heng Yue Sect structures.
Binary surgery on LE NBT files. Inserts palette entries + block entries.

Structure of MC 1.20.1 .nbt (LE format used by this project):
  TAG_Compound "" {
    TAG_Int "DataVersion"
    TAG_Int_Array "size" [x, y, z]
    TAG_List "palette" [TAG_Compound { "Name": ..., "Properties": {...} }, ...]
    TAG_List "blocks" [TAG_Compound { "pos": [x,y,z], "state": N, "nbt": {...} }, ...]
    TAG_List "entities" [...]
  }

Binary layout for a TAG_List field (e.g. "blocks"):
  [0x09] [name_len:u16] [name:bytes] [elem_type:u8] [count:i32] [entries...]
  Example: 09 04 00 62 6c 6f 63 6b 73 0A [count:4bytes] [compound entries]
  "blocks" = 62 6c 6f 63 6b 73

Offset math from keyword position K (where "blocks" starts):
  TAG_List byte:  K - 3
  Name length:    K - 2 (2 bytes)
  Keyword:        K (4 bytes)
  Element type:   K + 4 (1 byte)
  Count:          K + 5 (4 bytes LE)
  Data starts:    K + 9
"""
import os, json, struct, sys

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")
STRUCT_DIR = os.path.join(BASE, "structures", "heng_yue_sect")

# ── LE NBT primitive writers ──

def le_str(name, val):
    """TAG_String with name."""
    d = struct.pack('<b', 8)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    venc = val.encode('utf-8')
    d += struct.pack('<H', len(venc)) + venc
    return d

def le_int(name, val):
    """TAG_Int with name."""
    d = struct.pack('<b', 3)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', val)
    return d

def le_int_arr(name, vals):
    """TAG_Int_Array with name."""
    d = struct.pack('<b', 11)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<i', len(vals))
    for v in vals:
        d += struct.pack('<i', v)
    return d

def le_long(name, val):
    """TAG_Long with name."""
    d = struct.pack('<b', 4)
    enc = name.encode('utf-8')
    d += struct.pack('<H', len(enc)) + enc
    d += struct.pack('<q', val)
    return d

def le_compound(name=None):
    """TAG_Compound opener (no name for list entries)."""
    d = struct.pack('<b', 10)
    if name is not None:
        enc = name.encode('utf-8')
        d += struct.pack('<H', len(enc)) + enc
    return d

def le_end():
    """TAG_End."""
    return struct.pack('<b', 0)

def le_palette_entry(block_id):
    """One palette entry: TAG_Compound(no name) { Name, Properties }."""
    d = le_compound()  # no name in list context
    if '[' in block_id:
        name, props_str = block_id.split('[', 1)
        props_str = props_str.rstrip(']')
        d += le_str("Name", name)
        d += le_compound("Properties")
        for prop in props_str.split(','):
            if '=' in prop:
                k, v = prop.split('=', 1)
                d += le_str(k, v)
        d += le_end()
    else:
        d += le_str("Name", block_id)
    d += le_end()
    return d

def le_block_entry(x, y, z, state_idx, nbt_bytes=None):
    """One block entry: TAG_Compound(no name) { pos, state, nbt? }."""
    d = le_compound()  # no name in list context
    d += le_int_arr("pos", [x, y, z])
    d += le_int("state", state_idx)
    if nbt_bytes:
        d += le_compound("nbt")
        d += nbt_bytes
        d += le_end()
    d += le_end()
    return d

def le_chest_nbt(loot_path):
    """Block entity NBT for a chest with loot table."""
    d = le_str("id", "minecraft:chest")
    d += le_str("LootTable", loot_path)
    d += le_long("LootTableSeed", 0)
    return d


def find_list_field(data, keyword):
    """Find a TAG_List field by keyword in LE NBT binary.
    Returns (tag_start, keyword_pos, elem_type, count, data_start) or None.
    """
    kw = keyword.encode('utf-8')
    pos = 0
    while True:
        idx = data.find(kw, pos)
        if idx < 0:
            return None
        # Check: 3 bytes before keyword should be TAG_List (0x09) + name_len (0x04 0x00 for 4-char names)
        tag_pos = idx - 3
        if tag_pos < 0 or data[tag_pos] != 0x09:
            pos = idx + 1
            continue
        name_len = struct.unpack_from('<H', data, tag_pos + 1)[0]
        if name_len != len(kw):
            pos = idx + 1
            continue
        elem_type = data[idx + len(kw)]
        count = struct.unpack_from('<i', data, idx + len(kw) + 1)[0]
        data_start = idx + len(kw) + 5
        return (tag_pos, idx, elem_type, count, data_start)


def find_blocks_end(data, blocks_data_start, blocks_count, elem_type):
    """Find the byte offset where the blocks list data ends.
    We search for the "entities" keyword or the final TAG_End.
    """
    # Strategy: find "entities" keyword after blocks_data_start
    ent_kw = b'entities'
    ent_idx = data.find(ent_kw, blocks_data_start)
    if ent_idx > 0:
        # Verify it's a real TAG_List field
        tag_pos = ent_idx - 3
        if tag_pos >= 0 and data[tag_pos] == 0x09:
            name_len = struct.unpack_from('<H', data, tag_pos + 1)[0]
            if name_len == len(ent_kw):
                return tag_pos  # blocks list ends just before "entities" TAG_List

    # Fallback: find the root TAG_End (last 0x00 byte)
    # The root compound's TAG_End is the very last byte
    return len(data) - 1


def patch_structure(struct_name, chest_specs, loot_table_path):
    """Patch an NBT structure file to add loot chests.
    
    Args:
        struct_name: filename without .nbt
        chest_specs: list of (x, y, z, facing) tuples
        loot_table_path: e.g. "ergenverse:chests/heng_yue_sect_sword_peak"
    
    Returns:
        number of chests added, or 0 on error
    """
    nbt_path = os.path.join(STRUCT_DIR, f"{struct_name}.nbt")
    if not os.path.exists(nbt_path):
        print(f"  SKIP: {struct_name}.nbt not found")
        return 0

    with open(nbt_path, 'rb') as f:
        data = bytearray(f.read())

    # Find palette and blocks fields
    palette_info = find_list_field(data, "palette")
    blocks_info = find_list_field(data, "blocks")

    if not blocks_info:
        print(f"  ERROR: 'blocks' field not found in {struct_name}")
        return 0
    if not palette_info:
        print(f"  ERROR: 'palette' field not found in {struct_name}")
        return 0

    blocks_tag_start, blocks_kw_pos, blocks_elem_type, blocks_count, blocks_data_start = blocks_info
    palette_tag_start, palette_kw_pos, palette_elem_type, palette_count, palette_data_start = palette_info

    # Find where blocks list ends
    blocks_end = find_blocks_end(data, blocks_data_start, blocks_count, blocks_elem_type)

    # Build new palette entries (one per unique facing direction)
    new_palette_entries = []
    chest_state_indices = {}
    for _, _, _, facing in chest_specs:
        if facing not in chest_state_indices:
            idx = palette_count + len(new_palette_entries)
            chest_state_indices[facing] = idx
            new_palette_entries.append(
                le_palette_entry(f"minecraft:chest[facing={facing},type=single,waterlogged=false]"))

    # Build new block entries
    new_block_entries = []
    for cx, cy, cz, facing in chest_specs:
        nbt_data = le_chest_nbt(loot_table_path)
        new_block_entries.append(le_block_entry(cx, cy, cz, chest_state_indices[facing], nbt_data))

    new_chest_count = len(chest_specs)

    # ── Assemble patched file ──
    # Part A: everything before blocks TAG_List (includes palette — we update count there)
    # Part B: new palette entries (inserted between palette end and blocks start)
    # Part C: blocks TAG_List header (TAG_List + name_len + name + elem_type + count)
    # Part D: existing block entries
    # Part E: new block entries
    # Part F: everything after blocks list (entities, root TAG_End)

    # Insert palette entries just before blocks TAG_List
    partA = bytes(data[:blocks_tag_start])
    partB = b''.join(new_palette_entries)
    partC_header = bytearray(data[blocks_tag_start:blocks_data_start])
    # Update blocks count in header
    struct.pack_into('<i', partC_header, blocks_data_start - blocks_tag_start - 4, blocks_count + new_chest_count)
    partC = bytes(partC_header)
    partD = bytes(data[blocks_data_start:blocks_end])
    partE = b''.join(new_block_entries)
    partF = bytes(data[blocks_end:])

    patched = partA + partB + partC + partD + partE + partF

    # Update palette count in partA
    patched_ba = bytearray(patched)
    palette_count_offset = palette_kw_pos + len(b"palette") + 1  # +1 for elem_type byte
    struct.pack_into('<i', patched_ba, palette_count_offset, palette_count + len(new_palette_entries))

    with open(nbt_path, 'wb') as f:
        f.write(patched_ba)

    print(f"  {struct_name}: +{new_chest_count} chests "
          f"(palette {palette_count}->{palette_count + len(new_palette_entries)}, "
          f"blocks {blocks_count}->{blocks_count + new_chest_count})")
    return new_chest_count


def fix_mountain_cave_chest():
    """Fix the dead chest in mountain_cave.nbt (has chest block but no LootTable tag).
    Adds LootTable NBT to the existing chest block entity.
    """
    nbt_path = os.path.join(STRUCT_DIR, "mountain_cave.nbt")
    with open(nbt_path, 'rb') as f:
        data = f.read()
    
    # The chest in mountain_cave has block entity with "id": "minecraft:chest" but no LootTable
    # Find the "id" string followed by "minecraft:chest" in the blocks area
    # Strategy: find all occurrences of "minecraft:chest" and check if preceded by LootTable
    chest_id = b'minecraft:chest'
    loot_kw = b'LootTable'
    
    if loot_kw in data:
        print(f"  mountain_cave: already has LootTable, skipping")
        return 0
    
    # Find the chest's "id" field in block entity NBT
    # The pattern is: TAG_String "id" "minecraft:chest" ... TAG_End (end of nbt compound)
    # We need to insert LootTable + LootTableSeed before the TAG_End
    
    idx = 0
    fixed = 0
    while True:
        pos = data.find(chest_id, idx)
        if pos < 0:
            break
        # Check if this is a TAG_String value (preceded by length bytes)
        # In LE NBT, before "minecraft:chest" there should be the string length (0x10 0x00 = 16)
        if pos >= 2:
            str_len = struct.unpack_from('<H', data, pos - 2)[0]
            if str_len == len(chest_id):
                # This is a TAG_String value for "id". The block entity compound continues after.
                # Find the TAG_End that closes this compound
                scan = pos + len(chest_id)
                # After the string value, there might be more fields or TAG_End
                depth = 0
                while scan < len(data):
                    if data[scan] == 0x00:  # TAG_End
                        if depth == 0:
                            # Insert LootTable before this TAG_End
                            loot_path = b"ergenverse:chests/heng_yue_sect_mountain_cave"
                            insert_data = le_str("LootTable", loot_path.decode('utf-8'))
                            insert_data += le_long("LootTableSeed", 0)
                            data = data[:scan] + insert_data + data[scan:]
                            fixed += 1
                            idx = scan + len(insert_data)
                            break
                        else:
                            depth -= 1
                            scan += 1
                    elif data[scan] == 0x0A:  # TAG_Compound
                        depth += 1
                        # Skip name
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl
                    elif data[scan] == 0x08:  # TAG_String
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl
                        vl = struct.unpack_from('<H', data, scan)[0]
                        scan += 2 + vl
                    elif data[scan] == 0x09:  # TAG_List
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl + 5  # name + type + length
                    elif data[scan] == 0x03:  # TAG_Int
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl + 4
                    elif data[scan] == 0x04:  # TAG_Long
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl + 8
                    elif data[scan] == 0x0B:  # TAG_Int_Array
                        nl = struct.unpack_from('<H', data, scan + 1)[0]
                        scan += 3 + nl
                        al = struct.unpack_from('<i', data, scan)[0]
                        scan += 4 + al * 4
                    else:
                        scan += 1
                else:
                    idx = pos + 1
        else:
            idx = pos + 1
    
    if fixed > 0:
        with open(nbt_path, 'wb') as f:
            f.write(data)
        print(f"  mountain_cave: fixed {fixed} dead chest(s) with LootTable")
    return fixed


# ── Loot table helpers ──

def ei(item, w=1, c=(1,1)):
    e = {"type":"minecraft:item","weight":w,"name":item,"functions":[]}
    if c != (1,1):
        e["functions"].append({"function":"minecraft:set_count","count":{"min":c[0],"max":c[1]}})
    return e

def ee(w=1):
    return {"type":"minecraft:empty","weight":w}

def ep(entries, rolls=(2,4)):
    return {"rolls":{"min":rolls[0],"max":rolls[1]},"entries":entries}

def lt(pools):
    return {"type":"minecraft:chest","pools":pools}

def eg(i):
    return f"ergenverse:{i}"

def save_loot(name, data):
    os.makedirs(LOOT_DIR, exist_ok=True)
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, 'w') as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print(f"  Loot: {name}.json")


def create_new_loot_tables():
    """Create loot tables for buildings that don't have them yet."""
    # mountain_cave — the hidden cave where Wang Lin finds the Heaven Defying Bead
    # Canon: cave with spiritual energy, hidden location
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_mountain_cave.json")):
        save_loot("heng_yue_sect_mountain_cave", lt([
            ep([ei(eg("spirit_stone"),5,(2,6)), ei(eg("spirit_stone_fragment"),4,(3,8)),
                ei(eg("jade_slip"),3), ei(eg("qi_gathering_pill"),3,(1,2)), ee(3)], (2,4)),
            ep([ei(eg("spirit_herb_seed"),4,(1,4)), ei(eg("cold_iron_ingot"),2,(1,2)),
                ei("minecraft:glowstone_dust",2,(1,3)), ee(5)], (1,2)),
        ]))

    # core_formation_hall — where Core Formation breakthrough happens
    # Canon: Wang Lin breaks through to Core Formation. Pills, stones, formation materials.
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_core_formation_hall.json")):
        save_loot("heng_yue_sect_core_formation_hall", lt([
            ep([ei(eg("spirit_stone"),7,(5,12)), ei(eg("foundation_pill"),2),
                ei(eg("qi_gathering_pill"),4,(1,3)), ei(eg("jade_slip"),3), ee(2)], (3,5)),
            ep([ei(eg("formation_flag_blank"),4,(2,5)), ei(eg("spirit_ink"),3,(1,3)),
                ei(eg("spirit_herb_seed"),3,(2,4)), ei(eg("cold_iron_ingot"),2), ee(4)], (1,3)),
        ]))

    # secret_pavilion — hidden knowledge pavilion
    # Canon: secret locations contain rare techniques and pills (INFERRED)
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_secret_pavilion.json")):
        save_loot("heng_yue_sect_secret_pavilion", lt([
            ep([ei(eg("jade_slip"),6), ei(eg("foundation_pill"),2),
                ei(eg("purification_pill"),3), ei(eg("spirit_stone"),5,(3,8)), ee(2)], (2,4)),
            ep([ei(eg("spirit_iron_ingot"),2,(1,2)), ei(eg("meditation_mat"),2),
                ei(eg("qi_gathering_pill"),3,(1,2)), ee(5)], (1,2)),
        ]))

    # sword_tomb — ancient sword burial site
    # Canon: sword tombs contain ancient weapons and crafting materials (INFERRED from RI)
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_sword_tomb.json")):
        save_loot("heng_yue_sect_sword_tomb", lt([
            ep([ei(eg("cold_iron_ingot"),7,(3,8)), ei(eg("spirit_iron_ingot"),3,(1,3)),
                ei(eg("beast_bone"),5,(2,5)), ei(eg("spirit_ink"),2), ee(3)], (3,6)),
            ep([ei(eg("spirit_stone_fragment"),5,(3,8)), ei(eg("spirit_stone"),3,(1,3)),
                ei("minecraft:iron_sword",1), ei(eg("jade_slip_blank"),2,(1,2)), ee(4)], (1,3)),
        ]))

    # underground_passage — hidden tunnel connecting sect areas
    # Canon: secret passages (INFERRED), basic supplies for those who know
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_underground_passage.json")):
        save_loot("heng_yue_sect_underground_passage", lt([
            ep([ei(eg("spirit_stone_fragment"),6,(3,8)), ei(eg("spirit_stone"),3,(1,3)),
                ei(eg("jade_slip"),2), ei(eg("qi_gathering_pill"),2), ee(5)], (2,3)),
            ep([ei("minecraft:torch",3,(2,6)), ei("minecraft:bread",4,(1,3)),
                ei(eg("beast_bone"),2), ee(5)], (1,2)),
        ]))

    # puppet_workshop — crafting area for puppets/constructs
    # Canon: puppet workshops have crafting materials (INFERRED)
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_puppet_workshop.json")):
        save_loot("heng_yue_sect_puppet_workshop", lt([
            ep([ei(eg("cold_iron_ingot"),6,(2,5)), ei(eg("beast_bone"),5,(2,4)),
                ei(eg("spirit_ink"),4,(1,3)), ei(eg("formation_flag_blank"),3,(1,3)), ee(2)], (3,5)),
            ep([ei(eg("spirit_stone_fragment"),4,(3,8)), ei(eg("spirit_herb_seed"),3,(1,3)),
                ei("minecraft:iron_ingot",2,(1,3)), ee(5)], (1,3)),
        ]))

    # spirit_beast_pens — animal pens
    # Canon: beast pens have feed and taming supplies (INFERRED)
    if not os.path.exists(os.path.join(LOOT_DIR, "heng_yue_sect_spirit_beast_pens.json")):
        save_loot("heng_yue_sect_spirit_beast_pens", lt([
            ep([ei(eg("beast_bone"),6,(2,5)), ei(eg("spirit_herb_seed"),4,(1,4)),
                ei("minecraft:bone",3,(1,3)), ee(5)], (2,4)),
            ep([ei(eg("spirit_stone_fragment"),3,(2,5)), ei("minecraft:lead",1),
                ee(8)], (1,2)),
        ]))


def main():
    print("=" * 60)
    print("AUTO-CANON-012: Add loot chests to Heng Yue structures")
    print("=" * 60)

    # Create any missing loot tables
    print("\nCreating new loot tables...")
    create_new_loot_tables()

    # Fix mountain_cave's dead chest first
    print("\nFixing dead chests...")
    total_fixed = fix_mountain_cave_chest()

    # Patch structures that already have loot tables but no chests in NBT
    print("\nPatching structures...")
    total = 0

    # ancestor_hall: 19x12x15 — memorial hall with offering niches along walls
    total += patch_structure("ancestor_hall",
        [(5,1,1,"south"),(9,1,1,"south"),(7,1,2,"south"),(3,1,7,"east"),(11,1,7,"east")],
        "ergenverse:chests/heng_yue_sect_ancestor_hall")

    # sword_peak: 15x20x15 — mountain-top training ground
    total += patch_structure("sword_peak",
        [(5,2,1,"south"),(8,2,1,"south"),(7,2,3,"south")],
        "ergenverse:chests/heng_yue_sect_sword_peak")

    # inner_sect: 21x14x15 — inner sect quarters (restricted area, better loot)
    total += patch_structure("inner_sect",
        [(2,1,2,"south"),(12,1,2,"south"),(2,1,12,"north"),(12,1,12,"north"),(7,1,7,"south")],
        "ergenverse:chests/heng_yue_sect_inner_sect")

    # array_hall: 17x10x17 — formation hall with formation materials
    total += patch_structure("array_hall",
        [(2,1,2,"south"),(12,1,2,"south"),(7,1,1,"south"),(4,1,12,"north"),(10,1,12,"north")],
        "ergenverse:chests/heng_yue_sect_array_hall")

    # core_formation_hall: 19x12x19 — Core Formation breakthrough hall
    total += patch_structure("core_formation_hall",
        [(5,1,2,"south"),(9,1,2,"south"),(7,1,2,"south")],
        "ergenverse:chests/heng_yue_sect_core_formation_hall")

    # secret_pavilion: 11x8x11 — hidden pavilion (small, rare loot)
    total += patch_structure("secret_pavilion",
        [(3,1,3,"south"),(7,1,7,"south")],
        "ergenverse:chests/heng_yue_sect_secret_pavilion")

    # sword_tomb: 13x10x13 — ancient sword tomb
    total += patch_structure("sword_tomb",
        [(3,1,3,"south"),(9,1,3,"south"),(6,1,6,"south")],
        "ergenverse:chests/heng_yue_sect_sword_tomb")

    # underground_passage: 7x5x25 — long narrow tunnel
    total += patch_structure("underground_passage",
        [(2,1,2,"south"),(5,1,22,"south")],
        "ergenverse:chests/heng_yue_sect_underground_passage")

    # puppet_workshop: 17x8x13 — crafting workshop
    total += patch_structure("puppet_workshop",
        [(2,1,2,"south"),(8,1,2,"south"),(14,1,2,"south")],
        "ergenverse:chests/heng_yue_sect_puppet_workshop")

    # spirit_beast_pens: 21x6x15 — animal pens
    total += patch_structure("spirit_beast_pens",
        [(3,1,3,"south"),(17,1,3,"south")],
        "ergenverse:chests/heng_yue_sect_spirit_beast_pens")

    # ── Verify all ──
    print("\nVerifying all Heng Yue structures...")
    all_structs = sorted(os.listdir(STRUCT_DIR))
    grand_chest = 0
    grand_loot = 0
    for f in all_structs:
        if f.endswith('.nbt'):
            path = os.path.join(STRUCT_DIR, f)
            with open(path, 'rb') as fp:
                d = fp.read()
            has_chest = d.count(b'chest')
            has_loot = d.count(b'LootTable')
            grand_chest += has_chest
            grand_loot += has_loot
            status = "LOOT" if has_loot > 0 else "EMPTY"
            print(f"  {f}: chest_blocks={has_chest}, loot_tables={has_loot} [{status}]")

    print(f"\nTotal: {grand_loot} loot chests across {len(all_structs)} structures")
    print(f"New this run: {total} chests patched + {total_fixed} dead chests fixed")


if __name__ == "__main__":
    main()