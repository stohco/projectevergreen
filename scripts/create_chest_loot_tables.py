#!/usr/bin/env python3
"""
Create loot table JSONs for chests in custom canon structures.
Each loot table reflects the canon-appropriate treasures of that location.

References:
- ergenverse:chests/heng_yue_sect_hall          — sword manuals, spirit stones, herbs
- ergenverse:chests/tian_shui_market            — trade goods, coins, maps
- ergenverse:chests/tian_shui_merchant          — valuables, rare materials
- ergenverse:chests/teng_family_keep            — weapons, armor, family heirlooms
- ergenverse:chests/soul_refining_furnace       — soul fragments, dark techniques
- ergenverse:chests/corpse_yin_ancestor         — yin items, corpse pills
- ergenverse:chests/heavenly_fate_star_tower    — divination charts, fate items
- ergenverse:chests/wandering_cultivator        — mixed low-grade loot
"""
import os
import json

BASE = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "data", "ergenverse")
LOOT_DIR = os.path.join(BASE, "loot_tables", "chests")

def entry_item(item, weight=1, count=(1, 1), functions=None):
    """Create a loot pool entry for a single item."""
    e = {
        "type": "minecraft:item",
        "weight": weight,
        "name": item,
        "functions": functions or []
    }
    if count != (1, 1):
        e["functions"].append({
            "function": "minecraft:set_count",
            "count": {"min": count[0], "max": count[1]}
        })
    return e

def entry_empty(weight=1):
    return {"type": "minecraft:empty", "weight": weight}

def pool(entries, rolls=(2, 4)):
    return {
        "rolls": {"min": rolls[0], "max": rolls[1]},
        "entries": entries
    }

def loot_table(pools):
    return {"type": "minecraft:chest", "pools": pools}

def save(name, data):
    os.makedirs(LOOT_DIR, exist_ok=True)
    path = os.path.join(LOOT_DIR, f"{name}.json")
    with open(path, "w") as f:
        json.dump(data, f, indent=2)
        f.write("\n")
    print(f"  ✓ chests/{name}.json")

# Common ergenverse items
def eg(item):
    return f"ergenverse:{item}"

SPIRIT_STONE = eg("spirit_stone")
LOW_GRADE_SPIRIT_STONE = eg("spirit_stone_low")
MID_GRADE_SPIRIT_STONE = eg("spirit_stone_mid")
HIGH_GRADE_SPIRIT_STONE = eg("spirit_stone_high")
IMMORTAL_STONE = eg("immortal_stone")
QI_GATHERING_GRASS = eg("qi_gathering_grass")
SNOW_HEART_HERB = eg("snow_heart_herb")
SOUL_NOURISHING_LOTUS = eg("soul_nourishing_lotus")
NINE_LEAF_CLOVER = eg("nine_leaf_clover")
FIVE_COLOR_GINSENG = eg("five_color_ginseng")
BLOOD_FORGETTING_GRASS = eg("blood_forgetting_grass")
HEART_DEVIL_FLOWER = eg("heart_devil_flower")
VOID_NETHER_GRASS = eg("void_nether_grass")
REINCARNATION_LILY = eg("reincarnation_lily")
VERMILION_BLOOD_GINSENG = eg("vermilion_blood_ginseng")
REALM_SEALING_FLAG = eg("realm_sealing_flag")
STAR_SEALING_FLAG = eg("star_sealing_flag")
SOUL_REFINING_FLAG = eg("soul_refining_flag")
JADE_SLIP = eg("jade_slip")
STARRY_SKY_TOKEN = eg("starry_sky_token")
# Canon treasures
FLYING_SWORD = eg("flying_sword")
GOD_SLAYING_SWORD = eg("god_slaying_sword")
SPIRIT_ARMOR = eg("spirit_armor")
STORAGE_POUCH = eg("storage_pouch")
DAO_FRAGMENT = eg("dao_fragment")
SOUL_FRAGMENT = eg("soul_fragment")
BLOOD_ESSENCE = eg("blood_essence")
VOID_ESSENCE = eg("void_essence")
SPIRIT_VEIN_ESSENCE = eg("spirit_vein_essence")
HEAVEN_DEFYING_BEAD = eg("heaven_defying_bead")
SOUL_LASHER = eg("soul_lasher")
HEAVEN_FAN = eg("heaven_fan")
KARMA_WHIP = eg("karma_whip")
EIGHTEEN_HELL_STAMP = eg("eighteen_hell_stamp")
VERMILION_EMPEROR_SEAL = eg("vermilion_emperor_seal")
VERMILION_BIRD_FEATHER = eg("vermilion_bird_feather")
JI_REALM = eg("ji_realm")
NINE_COLOR_FLAME = eg("nine_color_flame")
TRIBULATION_FRAGMENT = eg("tribulation_fragment")
CAVE_WORLD_KEY = eg("cave_world_key")
# Spirit beast cores
THUNDER_TOAD_CORE = eg("thunder_toad_core")
MOSQUITO_CORE = eg("mosquito_core")
LEI_JI_CORE = eg("lei_ji_core")
FLAME_DRAGON_CORE = eg("flame_dragon_core")
VERMILION_BIRD_CORE = eg("vermilion_bird_core")
AZURE_DRAGON_CORE = eg("azure_dragon_core")
WHITE_TIGER_CORE = eg("white_tiger_core")
BLACK_TORTOISE_CORE = eg("black_tortoise_core")
QILIN_CORE = eg("qilin_core")
CLOUD_WHALE_CORE = eg("cloud_whale_core")
NETHER_CORE = eg("nether_core")
# Ancient god relics
ANCIENT_GOD_BONE = eg("ancient_god_bone")
ANCIENT_GOD_CORE = eg("ancient_god_core")
ANCIENT_GOD_CROWN = eg("ancient_god_crown")
DRAGON_SCALE = eg("dragon_scale")

# Vanilla fallbacks (for items that may not exist yet)
BONE = "minecraft:bone"
BOOK = "minecraft:book"
ENCHANTED_BOOK = "minecraft:enchanted_book"
EMERALD = "minecraft:emerald"
GOLD_INGOT = "minecraft:gold_ingot"
IRON_INGOT = "minecraft:iron_ingot"
DIAMOND = "minecraft:diamond"
ENDER_PEARL = "minecraft:ender_pearl"
NETHER_STAR = "minecraft:nether_star"
GOLDEN_APPLE = "minecraft:golden_apple"
ENCHANTED_GOLDEN_APPLE = "minecraft:enchanted_golden_apple"
MAP = "minecraft:map"
COMPASS = "minecraft:compass"
CLOCK = "minecraft:clock"
SADDLE = "minecraft:saddle"
NAME_TAG = "minecraft:name_tag"
IRON_SWORD = "minecraft:iron_sword"
GOLDEN_SWORD = "minecraft:golden_sword"
DIAMOND_SWORD = "minecraft:diamond_sword"
CHAINMAIL_HELMET = "minecraft:chainmail_helmet"
CHAINMAIL_CHESTPLATE = "minecraft:chainmail_chestplate"
TOTEM = "minecraft:totem_of_undying"
EXPERIENCE_BOTTLE = "minecraft:experience_bottle"
WITHER_SKELETON_SKULL = "minecraft:wither_skeleton_skull"
DRAGON_BREATH = "minecraft:dragon_breath"
GHAST_TEAR = "minecraft:ghast_tear"
NAUTILUS_SHELL = "minecraft:nautilus_shell"
HEART_OF_THE_SEA = "minecraft:heart_of_the_sea"

def main():
    print("=" * 70)
    print("Creating loot tables for canon structure chests")
    print("=" * 70)

    # ── Heng Yue Sect Hall — sword sect treasures ──
    save("heng_yue_sect_hall", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 8, (3, 8)),
            entry_item(LOW_GRADE_SPIRIT_STONE, 6, (5, 12)),
            entry_item(MID_GRADE_SPIRIT_STONE, 2, (1, 3)),
            entry_empty(3),
        ], (2, 4)),
        pool([
            entry_item(QI_GATHERING_GRASS, 4, (1, 3)),
            entry_item(SNOW_HEART_HERB, 2, (1, 2)),
            entry_item(NINE_LEAF_CLOVER, 1, (1, 1)),
            entry_item(BOOK, 5, (1, 2)),
            entry_item(ENCHANTED_BOOK, 2, (1, 1)),
            entry_item(JADE_SLIP, 3, (1, 1)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(FLYING_SWORD, 2, (1, 1)),
            entry_item(IRON_SWORD, 3, (1, 1)),
            entry_item(GOLDEN_SWORD, 1, (1, 1)),
            entry_item(STORAGE_POUCH, 2, (1, 1)),
            entry_item(SPIRIT_ARMOR, 1, (1, 1)),
            entry_item(EMERALD, 3, (1, 4)),
            entry_empty(4),
        ], (1, 2)),
    ]))

    # ── Tian Shui Market — trade goods ──
    save("tian_shui_market", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 6, (2, 6)),
            entry_item(LOW_GRADE_SPIRIT_STONE, 8, (4, 10)),
            entry_item(EMERALD, 5, (2, 5)),
            entry_item(GOLD_INGOT, 3, (1, 3)),
            entry_empty(3),
        ], (2, 5)),
        pool([
            entry_item(MAP, 3, (1, 1)),
            entry_item(COMPASS, 2, (1, 1)),
            entry_item(CLOCK, 2, (1, 1)),
            entry_item(NAME_TAG, 2, (1, 1)),
            entry_item(SADDLE, 1, (1, 1)),
            entry_item(BOOK, 4, (1, 2)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(BONE, 3, (2, 6)),
            entry_item(IRON_INGOT, 4, (1, 4)),
            entry_empty(5),
        ], (1, 2)),
    ]))

    # ── Tian Shui Merchant — valuables ──
    save("tian_shui_merchant", loot_table([
        pool([
            entry_item(MID_GRADE_SPIRIT_STONE, 4, (1, 4)),
            entry_item(SPIRIT_STONE, 6, (5, 12)),
            entry_item(EMERALD, 5, (3, 8)),
            entry_item(DIAMOND, 2, (1, 2)),
            entry_item(GOLD_INGOT, 4, (2, 5)),
            entry_empty(2),
        ], (3, 5)),
        pool([
            entry_item(ENCHANTED_BOOK, 3, (1, 1)),
            entry_item(GOLDEN_APPLE, 2, (1, 2)),
            entry_item(ENCHANTED_GOLDEN_APPLE, 1, (1, 1)),
            entry_item(TOTEM, 1, (1, 1)),
            entry_empty(5),
        ], (1, 2)),
        pool([
            entry_item(DIAMOND_SWORD, 1, (1, 1)),
            entry_item(CHAINMAIL_CHESTPLATE, 2, (1, 1)),
            entry_item(IRON_SWORD, 3, (1, 1)),
            entry_empty(6),
        ], (0, 1)),
    ]))

    # ── Teng Family Keep — weapons & heirlooms ──
    save("teng_family_keep", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 6, (4, 10)),
            entry_item(MID_GRADE_SPIRIT_STONE, 3, (1, 3)),
            entry_item(IRON_INGOT, 5, (3, 8)),
            entry_item(GOLD_INGOT, 3, (2, 5)),
            entry_empty(2),
        ], (2, 4)),
        pool([
            entry_item(FLYING_SWORD, 3, (1, 1)),
            entry_item(IRON_SWORD, 5, (1, 1)),
            entry_item(DIAMOND_SWORD, 2, (1, 1)),
            entry_item(SPIRIT_ARMOR, 2, (1, 1)),
            entry_item(CHAINMAIL_HELMET, 3, (1, 1)),
            entry_item(CHAINMAIL_CHESTPLATE, 3, (1, 1)),
            entry_item(GOLDEN_APPLE, 2, (1, 2)),
            entry_item(STORAGE_POUCH, 2, (1, 1)),
            entry_empty(3),
        ], (1, 3)),
        pool([
            entry_item(ENCHANTED_BOOK, 4, (1, 1)),
            entry_item(REALM_SEALING_FLAG, 1, (1, 1)),
            entry_item(STAR_SEALING_FLAG, 1, (1, 1)),
            entry_item(JADE_SLIP, 2, (1, 1)),
            entry_item(EXPERIENCE_BOTTLE, 3, (2, 5)),
            entry_item(THUNDER_TOAD_CORE, 1, (1, 1)),
            entry_item(LEI_JI_CORE, 1, (1, 1)),
            entry_empty(4),
        ], (1, 2)),
    ]))

    # ── Soul Refining Furnace — dark/soul items ──
    save("soul_refining_furnace", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 5, (3, 8)),
            entry_item(MID_GRADE_SPIRIT_STONE, 3, (1, 4)),
            entry_item(HIGH_GRADE_SPIRIT_STONE, 1, (1, 2)),
            entry_empty(3),
        ], (2, 4)),
        pool([
            entry_item(BONE, 8, (4, 12)),
            entry_item(WITHER_SKELETON_SKULL, 2, (1, 1)),
            entry_item(SOUL_NOURISHING_LOTUS, 3, (1, 2)),
            entry_item(SOUL_FRAGMENT, 3, (1, 2)),
            entry_item(SOUL_REFINING_FLAG, 2, (1, 1)),
            entry_item(GHAST_TEAR, 2, (1, 2)),
            entry_item(SOUL_LASHER, 1, (1, 1)),
            entry_item(NETHER_CORE, 1, (1, 1)),
            entry_empty(3),
        ], (1, 3)),
        pool([
            entry_item(ENCHANTED_BOOK, 4, (1, 1)),
            entry_item(ENDER_PEARL, 3, (1, 3)),
            entry_item(EXPERIENCE_BOTTLE, 4, (3, 6)),
            entry_item(DRAGON_BREATH, 1, (1, 2)),
            entry_item(HEART_DEVIL_FLOWER, 2, (1, 1)),
            entry_empty(4),
        ], (1, 2)),
    ]))

    # ── Corpse Yin Ancestor Hall — yin/death items ──
    save("corpse_yin_ancestor", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 4, (2, 6)),
            entry_item(BONE, 6, (3, 8)),
            entry_item(WITHER_SKELETON_SKULL, 3, (1, 2)),
            entry_item(SOUL_FRAGMENT, 3, (1, 2)),
            entry_empty(4),
        ], (2, 4)),
        pool([
            entry_item(SOUL_NOURISHING_LOTUS, 3, (1, 2)),
            entry_item(GHAST_TEAR, 2, (1, 2)),
            entry_item(ENCHANTED_BOOK, 3, (1, 1)),
            entry_item(ENDER_PEARL, 2, (1, 2)),
            entry_item("minecraft:crying_obsidian", 3, (1, 3)),
            entry_item(REALM_SEALING_FLAG, 2, (1, 1)),
            entry_item(BLOOD_ESSENCE, 2, (1, 2)),
            entry_item(NETHER_CORE, 1, (1, 1)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(HIGH_GRADE_SPIRIT_STONE, 1, (1, 2)),
            entry_item(REALM_SEALING_FLAG, 1, (1, 1)),
            entry_item(JADE_SLIP, 2, (1, 1)),
            entry_item(TOTEM, 1, (1, 1)),
            entry_item(EIGHTEEN_HELL_STAMP, 1, (1, 1)),
            entry_empty(6),
        ], (0, 2)),
    ]))

    # ── Heavenly Fate Star Tower — divination/fate items ──
    save("heavenly_fate_star_tower", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 5, (3, 8)),
            entry_item(MID_GRADE_SPIRIT_STONE, 4, (2, 5)),
            entry_item(HIGH_GRADE_SPIRIT_STONE, 2, (1, 3)),
            entry_item(IMMORTAL_STONE, 1, (1, 1)),
            entry_item(EMERALD, 3, (2, 6)),
            entry_empty(2),
        ], (2, 4)),
        pool([
            entry_item(COMPASS, 4, (1, 1)),
            entry_item(CLOCK, 4, (1, 1)),
            entry_item(MAP, 3, (1, 1)),
            entry_item(ENCHANTED_BOOK, 5, (1, 1)),
            entry_item(JADE_SLIP, 4, (1, 1)),
            entry_item(EXPERIENCE_BOTTLE, 4, (3, 6)),
            entry_item(DAO_FRAGMENT, 2, (1, 1)),
            entry_item(JI_REALM, 1, (1, 1)),
            entry_empty(2),
        ], (2, 4)),
        pool([
            entry_item(ENDER_PEARL, 3, (1, 3)),
            entry_item(NAUTILUS_SHELL, 2, (1, 2)),
            entry_item(HEART_OF_THE_SEA, 1, (1, 1)),
            entry_item(STARRY_SKY_TOKEN, 1, (1, 1)),
            entry_item(NETHER_STAR, 1, (1, 1)),
            entry_item(HEAVEN_FAN, 1, (1, 1)),
            entry_item(KARMA_WHIP, 1, (1, 1)),
            entry_empty(5),
        ], (0, 1)),
    ]))

    # ── Wandering Cultivator — mixed low-grade ──
    save("wandering_cultivator", loot_table([
        pool([
            entry_item(LOW_GRADE_SPIRIT_STONE, 8, (2, 5)),
            entry_item(SPIRIT_STONE, 4, (1, 3)),
            entry_item(BONE, 3, (1, 4)),
            entry_item(BOOK, 3, (1, 1)),
            entry_item(BREAD_ITEM := "minecraft:bread", 4, (1, 3)),
            entry_empty(3),
        ], (1, 3)),
        pool([
            entry_item(QI_GATHERING_GRASS, 3, (1, 2)),
            entry_item(IRON_INGOT, 2, (1, 2)),
            entry_item(EMERALD, 2, (1, 2)),
            entry_item(EXPERIENCE_BOTTLE, 2, (1, 2)),
            entry_item(STORAGE_POUCH, 1, (1, 1)),
            entry_empty(5),
        ], (0, 2)),
    ]))

    # ── Cloud Sky Pavilion — celestial/sky treasures ──
    save("cloud_sky_pavilion", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 5, (3, 8)),
            entry_item(MID_GRADE_SPIRIT_STONE, 3, (1, 4)),
            entry_item(HIGH_GRADE_SPIRIT_STONE, 2, (1, 2)),
            entry_item(IMMORTAL_STONE, 1, (1, 1)),
            entry_empty(3),
        ], (2, 4)),
        pool([
            entry_item(SOUL_NOURISHING_LOTUS, 3, (1, 2)),
            entry_item(NINE_LEAF_CLOVER, 2, (1, 1)),
            entry_item(FIVE_COLOR_GINSENG, 1, (1, 1)),
            entry_item(ENCHANTED_BOOK, 4, (1, 1)),
            entry_item(JADE_SLIP, 3, (1, 1)),
            entry_item(EXPERIENCE_BOTTLE, 4, (2, 5)),
            entry_empty(3),
        ], (1, 3)),
        pool([
            entry_item(FLYING_SWORD, 2, (1, 1)),
            entry_item(SPIRIT_ARMOR, 1, (1, 1)),
            entry_item(STORAGE_POUCH, 2, (1, 1)),
            entry_item(CLOUD_WHALE_CORE, 1, (1, 1)),
            entry_item(AZURE_DRAGON_CORE, 1, (1, 1)),
            entry_item(VERMILION_BIRD_FEATHER, 1, (1, 1)),
            entry_empty(5),
        ], (0, 2)),
    ]))

    # ── Ancient Demon City — demonic/dark treasures ──
    save("ancient_demon_city", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 5, (3, 8)),
            entry_item(MID_GRADE_SPIRIT_STONE, 3, (1, 3)),
            entry_item(BONE, 6, (3, 8)),
            entry_item(WITHER_SKELETON_SKULL, 2, (1, 1)),
            entry_empty(3),
        ], (2, 4)),
        pool([
            entry_item(SOUL_FRAGMENT, 3, (1, 2)),
            entry_item(BLOOD_ESSENCE, 3, (1, 2)),
            entry_item(NETHER_CORE, 2, (1, 1)),
            entry_item(HEART_DEVIL_FLOWER, 2, (1, 1)),
            entry_item(VOID_NETHER_GRASS, 2, (1, 1)),
            entry_item(ENDER_PEARL, 2, (1, 2)),
            entry_item(DRAGON_BREATH, 1, (1, 2)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(ENCHANTED_BOOK, 4, (1, 1)),
            entry_item(GOD_SLAYING_SWORD, 1, (1, 1)),
            entry_item(EIGHTEEN_HELL_STAMP, 1, (1, 1)),
            entry_item(KARMA_WHIP, 1, (1, 1)),
            entry_item(ANCIENT_GOD_BONE, 1, (1, 1)),
            entry_item(DRAGON_SCALE, 1, (1, 1)),
            entry_item(EXPERIENCE_BOTTLE, 3, (3, 5)),
            entry_empty(5),
        ], (0, 2)),
    ]))

    # ── Immortal Cave — cultivator's personal hoard ──
    save("immortal_cave", loot_table([
        pool([
            entry_item(SPIRIT_STONE, 6, (4, 10)),
            entry_item(MID_GRADE_SPIRIT_STONE, 4, (2, 5)),
            entry_item(HIGH_GRADE_SPIRIT_STONE, 2, (1, 3)),
            entry_item(IMMORTAL_STONE, 1, (1, 1)),
            entry_empty(2),
        ], (2, 5)),
        pool([
            entry_item(QI_GATHERING_GRASS, 3, (1, 3)),
            entry_item(SNOW_HEART_HERB, 2, (1, 2)),
            entry_item(SOUL_NOURISHING_LOTUS, 2, (1, 1)),
            entry_item(NINE_LEAF_CLOVER, 1, (1, 1)),
            entry_item(VERMILION_BLOOD_GINSENG, 1, (1, 1)),
            entry_item(REINCARNATION_LILY, 1, (1, 1)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(ENCHANTED_BOOK, 5, (1, 1)),
            entry_item(JADE_SLIP, 4, (1, 1)),
            entry_item(STORAGE_POUCH, 3, (1, 1)),
            entry_item(FLYING_SWORD, 2, (1, 1)),
            entry_item(SPIRIT_ARMOR, 2, (1, 1)),
            entry_item(DAO_FRAGMENT, 2, (1, 1)),
            entry_item(SPIRIT_VEIN_ESSENCE, 1, (1, 1)),
            entry_item(HEAVEN_DEFYING_BEAD, 1, (1, 1)),
            entry_item(CAVE_WORLD_KEY, 1, (1, 1)),
            entry_empty(4),
        ], (1, 3)),
        pool([
            entry_item(STARRY_SKY_TOKEN, 1, (1, 1)),
            entry_item(TRIBULATION_FRAGMENT, 1, (1, 1)),
            entry_item(VERMILION_EMPEROR_SEAL, 1, (1, 1)),
            entry_item(ANCIENT_GOD_CORE, 1, (1, 1)),
            entry_item(NINE_COLOR_FLAME, 1, (1, 1)),
            entry_empty(7),
        ], (0, 1)),
    ]))

    print(f"\n{'=' * 70}")
    print("DONE — 11 loot tables created.")
    print(f"Output: {LOOT_DIR}")
    print(f"{'=' * 70}")

if __name__ == "__main__":
    main()
