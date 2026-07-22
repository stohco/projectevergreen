#!/usr/bin/env python3
"""AUTO-CANON-029: Inject loot chests into 8 Heng Yue Sect NBTs.

The 8 loot tables created in AUTO-CANON-028 are JSON files with NO chest
blocks referencing them. Article XXII violation: data-only, not experience.

This script uses the proven patch_structure function from
patch_village_final_loot.py (AUTO-CANON-027) to inject new chest blocks
with LootTable NBT tags into all 8 NBT files.

State before:
  alchemy_courtyard.nbt: 18KB, 3 refs (OLD tables)
  disciple_dormitories.nbt: 19KB, 3 refs (OLD tables)
  hidden_treasury.nbt: 12KB, 5 refs (OLD tables)
  main_plaza.nbt: 33KB, 0 refs
  outer_gate.nbt: 24KB, 0 refs
  spirit_herb_garden.nbt: 33KB, 0 refs
  spirit_spring.nbt: 13KB, 0 refs
  trial_grounds.nbt: 87KB, 0 refs

Zero Java changes. Article XXII (data→experience), XXVI (no new systems).
"""
import os, struct

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
NBT_DIR = os.path.join(BASE, "structures", "heng_yue_sect")

# ── NBT binary helpers (proven from patch_village_final_loot.py) ──
def le_str(n, v):
    return struct.pack('<b',8) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<H',len(ve:=v.encode())) + ve

def le_int(n, v):
    return struct.pack('<b',3) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<i',v)

def le_int_arr(n, vals):
    d = struct.pack('<b',11) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<i',len(vals))
    for v in vals: d += struct.pack('<i',v)
    return d

def le_long(n, v):
    return struct.pack('<b',4) + struct.pack('<H',len(e:=n.encode())) + e + struct.pack('<q',v)

def le_compound(n=None):
    d = struct.pack('<b',10)
    if n: d += struct.pack('<H',len(e:=n.encode())) + e
    return d

def le_end(): return struct.pack('<b',0)

def le_palette_entry(block_id):
    d = le_compound()
    if '[' in block_id:
        name, props = block_id.split('[', 1)
        props = props.rstrip(']')
        d += le_str("Name", name)
        d += le_compound("Properties")
        for prop in props.split(','):
            if '=' in prop: k, v = prop.split('=', 1); d += le_str(k, v)
        d += le_end()
    else:
        d += le_str("Name", block_id)
    d += le_end()
    return d

def le_block_entry(x, y, z, state_idx, nbt_bytes=None):
    d = le_compound()
    d += le_int_arr("pos", [x, y, z])
    d += le_int("state", state_idx)
    if nbt_bytes: d += le_compound("nbt") + nbt_bytes + le_end()
    d += le_end()
    return d

def le_chest_nbt(loot_path):
    return le_str("id","minecraft:chest") + le_str("LootTable",loot_path) + le_long("LootTableSeed",0)

def find_list_field(data, keyword):
    """Find a TAG_List field by keyword, handling both LE and BE name lengths.

    Some structure NBTs (e.g. from older generators) encode the string
    name length in big-endian instead of standard little-endian NBT.
    This function tries both, plus 4-byte int lengths.
    """
    kw = keyword.encode('utf-8')
    klen = len(kw)
    pos = 0
    while True:
        idx = data.find(kw, pos)
        if idx < 0: return None
        # Try multiple name-length encodings before the keyword
        for name_len_size, fmt, offset in [(2, '<H', 1), (2, '>H', 1), (4, '<i', 1), (4, '>i', 1)]:
            tag_pos = idx - name_len_size - offset
            if tag_pos < 0: continue
            if data[tag_pos] != 0x09: continue
            name_len = struct.unpack_from(fmt, data, tag_pos + offset)[0]
            if name_len != klen: continue
            # Found a match — determine payload offset
            # After keyword: 1 byte element_type + 4 bytes count
            elem_type = data[idx + klen]
            # Try LE int for count (standard NBT), fall back to BE
            count_offset = idx + klen + 1
            if count_offset + 4 <= len(data):
                count = struct.unpack_from('<i', data, count_offset)[0]
                if count < 0 or count > 100000:
                    count = struct.unpack_from('>i', data, count_offset)[0]
            else:
                count = 0
            data_start = count_offset + 4
            return (tag_pos, idx, elem_type, count, data_start)
        pos = idx + 1

def detect_count_endianness(data, kw_idx, klen):
    """Detect LE vs BE for the int count after keyword+elem_type."""
    count_offset = kw_idx + klen + 1
    if count_offset + 4 > len(data):
        return '<'
    le_val = struct.unpack_from('<i', data, count_offset)[0]
    if 0 < le_val < 100000:
        return '<'
    return '>'

def find_blocks_end(data, blocks_data_start):
    ent_idx = data.find(b'entities', blocks_data_start)
    if ent_idx > 0:
        tp = ent_idx - 3
        if tp >= 0 and data[tp] == 0x09:
            nl = struct.unpack_from('<H', data, tp + 1)[0]
            if nl == 8: return tp
        # Try BE
        tp2 = ent_idx - 3
        if tp2 >= 0 and data[tp2] == 0x09:
            nl2 = struct.unpack_from('>H', data, tp2 + 1)[0]
            if nl2 == 8: return tp2
    return len(data) - 1

def patch_structure(nbt_path, chest_specs):
    with open(nbt_path, 'rb') as f:
        data = bytearray(f.read())
    pi = find_list_field(data, "palette")
    bi = find_list_field(data, "blocks")
    if not bi or not pi:
        print(f"  WARNING: could not parse {os.path.basename(nbt_path)}, skipping")
        return 0
    bt, _, _, bc, bds = bi
    _, _, _, pc, pds = pi
    be = find_blocks_end(data, bds)
    # Detect endianness for count writes
    kw_len_blocks = len(b"blocks")
    endian = detect_count_endianness(data, bi[1], kw_len_blocks)
    ifmt = endian + 'i'
    new_pal = []
    smap = {}
    for _, _, _, facing, _ in chest_specs:
        if facing not in smap:
            smap[facing] = pc + len(new_pal)
            new_pal.append(le_palette_entry(f"minecraft:chest[facing={facing},type=single,waterlogged=false]"))
    new_blks = []
    for cx, cy, cz, facing, lp in chest_specs:
        nb = le_chest_nbt(lp)
        new_blks.append(le_block_entry(cx, cy, cz, smap[facing], nb))
    partA = bytes(data[:bt])
    partB = b''.join(new_pal)
    hdr = bytearray(data[bt:bds])
    struct.pack_into(ifmt, hdr, bds - bt - 4, bc + len(chest_specs))
    partC = bytes(hdr)
    partD = bytes(data[bds:be])
    partE = b''.join(new_blks)
    partF = bytes(data[be:])
    patched = bytearray(partA + partB + partC + partD + partE + partF)
    pco = pc + len(new_pal)
    struct.pack_into(ifmt, patched, pi[1] + len(b'palette') + 1, pco)
    with open(nbt_path, 'wb') as f:
        f.write(patched)
    return len(chest_specs)

# ── Chest specifications for each NBT ──
# Format: (x, y, z, facing, loot_table_ref)
# Positions chosen to be interior, varied, low y (1-3)

CHESTS = {
    # 1. Alchemy Courtyard (18KB) — reagent storage shelves
    "alchemy_courtyard.nbt": [
        (4, 1, 5, "north", "ergenverse:chests/heng_yue_sect_alchemy_courtyard"),
        (7, 2, 3, "east",  "ergenverse:chests/heng_yue_sect_alchemy_courtyard"),
    ],
    # 2. Disciple Dormitories (19KB) — disciple bunk supplies
    "disciple_dormitories.nbt": [
        (3, 1, 4, "south", "ergenverse:chests/heng_yue_sect_disciple_dormitories"),
        (8, 1, 6, "west",  "ergenverse:chests/heng_yue_sect_disciple_dormitories"),
        (5, 2, 3, "north", "ergenverse:chests/heng_yue_sect_disciple_dormitories"),
    ],
    # 3. Hidden Treasury (12KB) — MOST VALUABLE, more chests
    "hidden_treasury.nbt": [
        (3, 1, 4, "east",  "ergenverse:chests/heng_yue_sect_hidden_treasury"),
        (6, 1, 5, "north", "ergenverse:chests/heng_yue_sect_hidden_treasury"),
        (4, 2, 3, "south", "ergenverse:chests/heng_yue_sect_hidden_treasury"),
    ],
    # 4. Main Plaza (33KB) — open area, minimal chests (storage nooks)
    "main_plaza.nbt": [
        (5, 1, 5, "north", "ergenverse:chests/heng_yue_sect_main_plaza"),
        (10, 1, 8, "east",  "ergenverse:chests/heng_yue_sect_main_plaza"),
    ],
    # 5. Outer Gate (24KB) — guard post supplies
    "outer_gate.nbt": [
        (3, 1, 4, "south", "ergenverse:chests/heng_yue_sect_outer_gate"),
        (7, 1, 6, "north", "ergenverse:chests/heng_yue_sect_outer_gate"),
    ],
    # 6. Spirit Herb Garden (33KB) — herb storage + gathering supplies
    "spirit_herb_garden.nbt": [
        (4, 1, 5, "east",  "ergenverse:chests/heng_yue_sect_spirit_herb_garden"),
        (9, 1, 7, "south", "ergenverse:chests/heng_yue_sect_spirit_herb_garden"),
        (6, 2, 3, "west",  "ergenverse:chests/heng_yue_sect_spirit_herb_garden"),
    ],
    # 7. Spirit Spring (13KB) — small, meditation supplies
    "spirit_spring.nbt": [
        (3, 1, 4, "north", "ergenverse:chests/heng_yue_sect_spirit_spring"),
        (6, 1, 5, "east",  "ergenverse:chests/heng_yue_sect_spirit_spring"),
    ],
    # 8. Trial Grounds (87KB) — LARGEST, most chests
    "trial_grounds.nbt": [
        (4, 1, 5, "south", "ergenverse:chests/heng_yue_sect_trial_grounds"),
        (10, 1, 8, "north", "ergenverse:chests/heng_yue_sect_trial_grounds"),
        (7, 2, 3, "east",  "ergenverse:chests/heng_yue_sect_trial_grounds"),
        (12, 1, 6, "west",  "ergenverse:chests/heng_yue_sect_trial_grounds"),
    ],
}

def main():
    total = 0
    for nbt_name, specs in CHESTS.items():
        nbt_path = os.path.join(NBT_DIR, nbt_name)
        n = patch_structure(nbt_path, specs)
        total += n
        loot_refs = set(s[4] for s in specs)
        print(f"  {nbt_name}: +{n} chests -> {loot_refs.pop()}")

    print(f"\nTotal: {total} new loot chests in 8 NBTs")

    # Verification: count ALL LootTable refs in ALL Heng Yue NBTs
    print("\n=== Verification: all 20 Heng Yue NBTs ===")
    import glob
    all_nbts = sorted(glob.glob(os.path.join(NBT_DIR, "*.nbt")))
    with_refs = 0
    total_refs = 0
    without = []
    for p in all_nbts:
        with open(p, 'rb') as f:
            d = f.read()
        refs = d.count(b'LootTable')
        fname = os.path.basename(p)
        total_refs += refs
        if refs > 0:
            with_refs += 1
            print(f"  {fname}: {refs} LootTable refs [OK]")
        else:
            without.append(fname)
            print(f"  {fname}: 0 LootTable refs [EMPTY]")

    print(f"\nResult: {with_refs}/20 NBTs have loot, {total_refs} total refs")
    if without:
        print(f"STILL EMPTY: {without}")
    else:
        print("ALL HENG YUE STRUCTURES HAVE LOOT. Sect loot vertical slice COMPLETE.")

if __name__ == "__main__":
    main()