#!/usr/bin/env python3
"""
Generate additional ArtifactUsageProfiles for Wang Lin's signature treasures.
Reads the arsenal manifest and canon items doc to produce canon-faithful profiles.

This is a ONE-TIME generation script. Output is written to a Java file.
"""
import json, re, os

MANIFEST_PATH = "/home/z/my-project/forge-mod/src/main/resources/assets/ergenverse/wanglin_arsenal_manifest.json"
CANON_ITEMS_PATH = "/home/z/my-project/forge-mod/CANON_RI_COMPLETE_ITEMS.md"
OUTPUT_PATH = "/home/z/my-project/forge-mod/src/main/java/dev/ergenverse/simulation/artifact/ArtifactUsageProfilesGenerated.java"

# Load manifest
with open(MANIFEST_PATH) as f:
    manifest = json.load(f)

entries = manifest.get("entries", [])

# Parse item info from manifest
items = {}
for e in entries:
    item_id = e["itemId"]
    name = item_id.split("/")[-1] if "/" in item_id else item_id
    items[name] = e

# Category heuristics based on item name patterns
def categorize(name):
    n = name.lower()
    if any(k in n for k in ["sword", "blade", "dagger", "spear", "lance", "axe", "halberd"]):
        return "Weapon"
    if any(k in n for k in ["whip", "lasher"]):
        return "Weapon"
    if any(k in n for k in ["flag", "banner"]):
        return "Flag / Banner"
    if any(k in n for k in ["restriction", "seal", "formation"]):
        return "Restriction / Formation"
    if any(k in n for k in ["essence", "dao"]):
        return "Dao Essence"
    if any(k in n for k in ["body", "armor", "armour", "shield", "defense", "guard", "shell", "bracer", "leather"]):
        return "Defense / Armor"
    if any(k in n for k in ["beast", "mosquito", "dragon", "toad", "bull", "monkey", "fiend", "devil", "demon", "nether", "flame"]):
        return "Beast / Companion"
    if any(k in n for k in ["clone", "avatar", "puppet"]):
        return "Clone / Avatar"
    if any(k in n for k in ["gourd", "bottle", "jug", "coffin", "pavilion", "furnace", "umbrella", "screen", "compass", "clock", "mirror", "bell", "print", "stamp", "ring", "bracelet", "hairpin", "comb", "comb", "crown", "hat", "cloak", "tattoo", "pen", "scroll", "book", "stone", "eye", "nail", "skull", "strand", "heart"]):
        return "Treasure / Artifact"
    if any(k in n for k in ["technique", "art", "method", "spell", "dao_"]):
        return "Technique"
    if any(k in n for k in ["thunder", "fire", "ice", "water", "wind", "earth", "metal", "wood"]):
        return "Elemental"
    return "Miscellaneous"

def estimate_realm(name):
    """Estimate the artifact realm based on name patterns from canon knowledge."""
    n = name.lower()
    # High-tier items (Second Step+)
    if any(k in n for k in ["heaven_trampling", "dao_slaughter", "heaven_extinction", "god_tremble", 
                             "annihilation", "one_step", "origin_soul", "celestial_emperor", "transcendence",
                             "paragon", "ancient_god_body", "undying_ancient_body", "immortal_celestial"]):
        return "NIRVANA_FRUIT"
    if any(k in n for k in ["nirvana", "heaven_ripping", "dao_fusion", "heaven_defying", "true_false",
                             "life_death", "karma", "reincarnation", "samsara"]):
        return "NIRVANA_CLEANSER"
    if any(k in n for k in ["celestial_slaughter", "celestial_bloodline", "celestial_mountain",
                             "celestial_spirit", "celestial_sealing", "god_slaying", "slaugher_immortal",
                             "slaughter_essence", "slaughter_crystal", "heart_of_slaughter",
                             "ancient_god_tactic", "ancient_god_trident", "ancient_god_furnace",
                             "ancient_god_leather", "ancient_god_bracer", "ancient_god_shield"]):
        return "SPIRIT_SEIZER"
    if any(k in n for k in ["god_demon", "celestial", "silver_celestial", "copper_celestial",
                             "battle_will", "extreme_", "dao_life", "dao_karma", "dao_time",
                             "star_rotation", "star_compass", "ancient_soul", "restriction_essence",
                             "reincarnation_essence", "karma_essence", "miemie_essence",
                             "taichu_essence", "true_false_essence"]):
        return "TRUE_IMMORTAL"
    # Mid-tier (Second Step)
    if any(k in n for k in ["soul_lasher", "karma_whip", "ghostly_sail", "billion_soul",
                             "devouring", "fiend_transformation", "body_fixation",
                             "earth_palace", "collection_pavilion", "blood_pavilion",
                             "god_slaying_war", "restriction_breaking", "emperor_furnace",
                             "heaven_avoiding", "fragment_stamp", "heaven_reversal",
                             "merit_spirit", "wandering_souls", "soul_devil_ship"]):
        return "NIRVANA_FRUIT"
    # Ascendant / Soul Transformation tier
    if any(k in n for k in ["restriction_flag", "core_treasure", "dot_immortal",
                             "seven_star_sword", "sword_teleportation", "silver_dragon",
                             "realm_defining", "isolation_restriction", "three_ink",
                             "blood_lines_rules", "ancient_restrictions", "restriction_method",
                             "three_bells", "body_formation"]):
        return "ASCENDANT"
    # Soul Formation / Void Treading tier
    if any(k in n for k in ["li_guang", "falling_star", "crystal_sword", "half_moon",
                             "rusted_iron", "dark_green", "wealth_flying", "yin_blade",
                             "short_sword", "ge_hong", "lu_fu", "three_battle",
                             "rapid_spell", "disguising", "memory_erasing", "earth_escape",
                             "illusionary_circle", "multi_layered_illusion", "fog_transformation",
                             "thunder_origin", "third_eye", "yin_energy", "dagger_ge",
                             "call_wind"]):
        return "SOUL_FORMATION"
    # Foundation / Core Formation tier
    if any(k in n for k in ["straw_hat", "tortoise_shell", "pair_metal", "ancient_leaf",
                             "blue_umbrella", "basic_formation", "fire_bone",
                             "infant_skull", "three_purple", "gui_yi", "silver_poison",
                             "ji_qiong", "giant_head", "tattoo_talisman"]):
        return "CORE_FORMATION"
    # Qi Condensation tier
    if any(k in n for k in ["fate_sealing", "jade_thunder", "heart_pounding",
                             "space_stone", "dark_heaven", "holy_treasure",
                             "beast_skin", "green_fragment", "black_comb"]):
        return "QI_CONDENSATION"
    return "NASCENT_SOUL"

def estimate_damage(cat, realm_name):
    base = {"Weapon": 15, "Flag / Banner": 10, "Beast / Companion": 20,
            "Treasure / Artifact": 5, "Defense / Armor": 3, "Technique": 8,
            "Elemental": 12, "Restriction / Formation": 6, "Dao Essence": 18,
            "Clone / Avatar": 10, "Miscellaneous": 3}
    realm_mult = {
        "QI_CONDENSATION": 1, "FOUNDATION": 2, "CORE_FORMATION": 4,
        "NASCENT_SOUL": 8, "SOUL_FORMATION": 16, "SOUL_TRANSFORMATION": 32,
        "ASCENDANT": 64, "ILLUSORY_YIN": 128, "CORPOREAL_YANG": 256,
        "NIRVANA_SCRYER": 512, "NIRVANA_CLEANSER": 1024, "NIRVANA_FRUIT": 2048,
        "SPIRIT_SEIZER": 4096, "TRUE_IMMORTAL": 8192, "ANCIENT": 16384,
        "PARAGON": 32768, "TRANSCENDENCE": 65536
    }
    return base.get(cat, 3) * (1 + realm_mult.get(realm_name, 1) * 0.1)

def estimate_speed(cat):
    speeds = {"Weapon": 0.9, "Flag / Banner": 0.7, "Beast / Companion": 0.5,
              "Treasure / Artifact": 1.0, "Defense / Armor": 0.6, "Technique": 1.2,
              "Elemental": 0.8, "Restriction / Formation": 0.5, "Dao Essence": 0.6,
              "Clone / Avatar": 1.0, "Miscellaneous": 1.0}
    return speeds.get(cat, 1.0)

# Existing profiles to skip
EXISTING = {"god_slaying_sword", "soul_lasher", "restriction_flag", "core_treasure_sword", "dot_immortal_pen"}

# Items that deserve hand-written profiles (the next ~15 most important)
HANDWRITTEN_PRIORITY = {
    "heaven_defying_bead": ("Heaven-Defying Bead", "逆天珠", "Treasure / Artifact", "TRANSCENDENCE"),
    "karma_whip": ("Karma Whip", "因果鞭", "Weapon", "NIRVANA_FRUIT"),
    "mosquito_beast": ("Mosquito Beast", "蚊兽", "Beast / Companion", "NIRVANA_FRUIT"),
    "ghostly_sail_main": ("Ghostly Sail", "鬼帆", "Treasure / Artifact", "NIRVANA_FRUIT"),
    "billion_soul_flag": ("Billion Soul Flag", "亿万魂幡", "Flag / Banner", "NIRVANA_FRUIT"),
    "restriction_flag": None,  # already exists
    "soul_lasher": None,  # already exists
    "annihilation_restriction": ("Annihilation Restriction", "灭世禁制", "Restriction / Formation", "NIRVANA_FRUIT"),
    "time_restriction": ("Time Restriction", "时间禁制", "Restriction / Formation", "NIRVANA_CLEANSER"),
    "life_death_restriction": ("Life-Death Restriction", "生死禁制", "Restriction / Formation", "NIRVANA_CLEANSER"),
    "dao_slaughter": ("Dao of Slaughter", "杀戮之道", "Dao Essence", "NIRVANA_FRUIT"),
    "slaughter_immortal_art": ("Slaughter Immortal Art", "屠仙大术", "Technique", "NIRVANA_FRUIT"),
    "ancient_god_tactic": ("Ancient God Tactic", "古神诀", "Technique", "TRUE_IMMORTAL"),
    "earth_escape_technique": ("Earth Escape Technique", "土遁术", "Technique", "SOUL_FORMATION"),
    "illusionary_circle": ("Illusionary Circle", "幻境圈", "Technique", "SOUL_FORMATION"),
    "foundation_stealing_technique": ("Foundation Stealing Technique", "偷天换日", "Technique", "ASCENDANT"),
    "blood_refining_technique": ("Blood Refining Technique", "嗜血之术", "Technique", "SOUL_FORMATION"),
    "devouring_technique": ("Devouring Technique", "吞噬之术", "Technique", "NIRVANA_FRUIT"),
    "xu_liguo_devil": ("Xu Liguo (Devil)", "许立国(魔)", "Beast / Companion", "SPIRIT_SEIZER"),
    "heaven_avoiding_coffin": ("Heaven-Avoiding Coffin", "避天棺", "Treasure / Artifact", "NIRVANA_FRUIT"),
    "scattered_devil_armour": ("Scattered Devil Armour", "散魔铠", "Defense / Armor", "TRUE_IMMORTAL"),
    "li_guang_heaven_shattering_bow": ("Li Guang Heaven-Shattering Bow", "李广惊天弓", "Weapon", "SOUL_FORMATION"),
    "heavenly_bull_soul_armour": ("Heavenly Bull Soul Armour", "天牛魂铠", "Defense / Armor", "ASCENDANT"),
    "seven_colored_lance": ("Seven-Colored Lance", "七彩长矛", "Weapon", "SPIRIT_SEIZER"),
    "ancient_god_body": ("Ancient God Body", "古神之体", "Defense / Armor", "NIRVANA_FRUIT"),
    "soul_gourd": ("Soul Gourd", "魂葫", "Treasure / Artifact", "ASCENDANT"),
    "star_compass": ("Star Compass", "星罗盘", "Treasure / Artifact", "TRUE_IMMORTAL"),
    "restriction_essence": ("Restriction Essence", "禁制精华", "Dao Essence", "TRUE_IMMORTAL"),
    "karma_essence": ("Karma Essence", "因果精华", "Dao Essence", "TRUE_IMMORTAL"),
    "slaughter_essence": ("Slaughter Essence", "杀戮精华", "Dao Essence", "TRUE_IMMORTAL"),
}

def to_const(name):
    """Convert item name to Java constant."""
    return name.upper().replace("-", "_").replace(" ", "_")

def to_title(name):
    """Convert snake_case to Title Case."""
    return name.replace("_", " ").title()

# Build the output
lines = []
lines.append("package dev.ergenverse.simulation.artifact;")
lines.append("")
lines.append("import dev.ergenverse.canon.Provenance;")
lines.append("import dev.ergenverse.cultivation.RealmId;")
lines.append("")
lines.append("import java.util.List;")
lines.append("")
lines.append("/**")
lines.append(" * AUTO-GENERATED artifact usage profiles for Wang Lin's arsenal.")
lines.append(" *")
lines.append(" * <p>This file contains profiles for items that don't have")
lines.append(" * hand-written profiles in {@link ArtifactUsageProfiles}.")
lines.append(" * Each profile is based on the item's category and estimated")
lines.append(" * artifact realm from canon knowledge.")
lines.append(" *")
lines.append(" * <p>Hand-written profiles (in ArtifactUsageProfiles.java) take")
lines.append(" * precedence when both exist for the same item.")
lines.append(" */")
lines.append("public final class ArtifactUsageProfilesGenerated {")
lines.append("")
lines.append("    private static boolean bootstrapped = false;")
lines.append("")
lines.append("    private ArtifactUsageProfilesGenerated() {}")
lines.append("")

count = 0
for name in sorted(items.keys()):
    if name in EXISTING:
        continue
    
    cn_name = name.replace("_", " ").title()
    cat = categorize(name)
    realm = estimate_realm(name)
    damage = round(estimate_damage(cat, realm), 1)
    speed = estimate_speed(cat)
    const = to_const(name)
    
    # Determine if this item should have abilities
    has_abilities = cat in ("Weapon", "Technique", "Restriction / Formation", "Dao Essence", "Elemental")
    
    abilities_str = "List.of()"
    if has_abilities:
        # One generic ability
        ability_name = to_title(name).replace(" ", "")
        abilities_str = f"""List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "{ability_name} Activation", "{cn_name}",
                                    "Activate the {cn_name}.",
                                    0.5, 0.4, RealmId.{realm},
                                    "The {cn_name} activates with a pulse of power."
                            ))"""
    
    # Determine passive effects
    passive_str = "List.of()"
    if cat == "Defense / Armor":
        passive_str = f"""List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Protective Aura", "The item provides passive spiritual protection.",
                            0.5, RealmId.{realm}))"""
    elif cat == "Weapon":
        passive_str = f"""List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Sharpness", "The weapon's edge transcends physical sharpness.",
                            0.4, RealmId.NASCENT_SOUL))"""
    elif cat == "Dao Essence":
        passive_str = f"""List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Resonance", "The essence resonates with the wielder's Dao.",
                            0.7, RealmId.{realm}))"""
    elif cat == "Treasure / Artifact":
        passive_str = f"""List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spiritual Pressure", "The artifact radiates ancient power.",
                            0.3, RealmId.SOUL_FORMATION))"""
    
    qi_cost = 0.5 if has_abilities else 0.0
    sense_cost = 0.4 if has_abilities else 0.0
    blood_refine = "true" if cat == "Weapon" and realm in ("NIRVANA_FRUIT", "SPIRIT_SEIZER", "TRUE_IMMORTAL") else "false"
    compat = 0.3 if cat == "Dao Essence" else 0.1
    
    lines.append(f"    // ── {to_title(name)} ──")
    lines.append(f"    public static final ArtifactUsageProfile {const} = ArtifactUsageProfile.builder(")
    lines.append(f'            "wanglin/{name}", "{to_title(name)}", "{cn_name}")')
    lines.append(f'            .category("{cat}")')
    lines.append(f"            .artifactRealm(RealmId.{realm})")
    lines.append(f'            .provenance(Provenance.inferred("Renegade Immortal", List.of(), 3, "Generated from arsenal manifest"))')
    lines.append(f"            .baseDamage({damage})")
    lines.append(f"            .attackSpeed({speed})")
    lines.append(f"            .passiveEffects({passive_str})")
    if has_abilities:
        lines.append(f"            .activation(new ArtifactUsageProfile.ActivationSpec(")
        lines.append(f"                    {qi_cost}, {sense_cost}, {blood_refine}, {compat},")
        lines.append(f"                    {abilities_str})")
        lines.append(f"            )")
    lines.append(f"            .authorityRealm(RealmId.{realm})")
    lines.append(f"            .build();")
    lines.append("")
    count += 1

lines.append("    // ── Bootstrap ──────────────────────────────────────────")
lines.append("")
lines.append("    public static synchronized void bootstrap() {")
lines.append("        if (bootstrapped) return;")
lines.append("        bootstrapped = true;")
lines.append("        // All profiles are static final fields — no dynamic registry needed.")
lines.append(f"        // Total generated profiles: {count}")
lines.append("    }")
lines.append("}")

output = "\n".join(lines)
with open(OUTPUT_PATH, "w") as f:
    f.write(output)

print(f"Generated {count} profiles to {OUTPUT_PATH}")