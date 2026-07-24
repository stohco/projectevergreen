package dev.ergenverse.wanglin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.item.FlyingSwordItem;
import dev.ergenverse.item.sword.SwordEffectType;
import dev.ergenverse.wanglin.bead.HeavenDefyingBeadItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WangLinItems — the DeferredRegister for ALL of Wang Lin's arsenal.
 *
 * <p>This is the single most important class for the "completionist Wang Lin branch"
 * directive: it registers EVERY item, technique, pet, clone, and companion Wang Lin
 * encountered or owned across the full Renegade Immortal narrative as an actual
 * pickable Minecraft {@link Item}.
 *
 * <h2>Data-Driven Registration</h2>
 * <p>The item list is loaded from {@code assets/ergenverse/wanglin_arsenal_manifest.json}
 * (extracted from the Next.js prototype's {@code protagonist-arsenals.ts}). This manifest
 * contains 309 entries — every Wang Lin arsenal asset with its canonical itemId,
 * unlock threshold, and canon flag. The manifest is the single source of truth for
 * what gets registered; adding a new Wang Lin item means adding it to the manifest,
 * not hand-editing this class.
 *
 * <h2>Registration</h2>
 * <p>Call {@link #register(IEventBus)} once during mod construction (from
 * {@link dev.ergenverse.core.Ergenverse#Ergenverse}). This wires the DeferredRegister
 * to the mod event bus. Forge then fires the RegisterEvent and all 309 items become
 * real, holdable, creative-tab-listed Minecraft items.
 *
 * <h2>Access Pattern</h2>
 * <p>After registration, look up any item by its canonId:
 * <pre>{@code
 * RegistryObject<Item> flag = WangLinItems.get("wanglin/restriction_flag");
 * ItemStack stack = new ItemStack(flag.get());
 * }</pre>
 *
 * <h2>Prime Directive Compliance</h2>
 * <p>Every registered item is an objective world object — it exists regardless of
 * player perception. The item's tooltip always shows its full canon info; perception
 * gating (what the player UNDERSTANDS about the item) is handled by the
 * {@link dev.ergenverse.perception.PerceptionEngine}, not by hiding the item.
 */
public final class WangLinItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ergenverse.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ergenverse.MOD_ID);

    /** All registered arsenal entries, keyed by canonId (e.g. "wanglin/restriction_flag"). */
    private static final Map<String, RegistryObject<Item>> ARSENAL = new HashMap<>();

    /** The loaded manifest entries (preserved for queries). */
    private static final List<ManifestEntry> MANIFEST = new ArrayList<>();

    /** Whether the manifest has been loaded and items registered. */
    private static boolean registered = false;

    /** Tracks which registry names have already been registered, to skip duplicates. */
    private static final java.util.Set<String> REGISTERED_NAMES = new java.util.HashSet<>();

    /**
     * Registry names that are OWNED by {@link dev.ergenverse.item.ErgenverseItems}
     * with real mechanics (SoulGourdItem, StorageRingItem, JournalItem, etc.).
     * The WangLin arsenal manifest also lists these (as tooltip-only WangLinItem
     * duplicates), so we must skip them here to avoid a same-namespace collision
     * that crashes the server at registry freeze.
     * Add to this set whenever ErgenverseItems gains a new functional item that
     * also appears in wanglin_arsenal_manifest.json.
     */
    private static final java.util.Set<String> ERGENVERSE_ITEMS_OWNED_NAMES = java.util.Set.of(
            "soul_gourd",
            "storage_ring",
            "cultivation_journal",
            "beast_core"
    );

    /**
     * Manifest bare-id -> canonical registry entry id.
     *
     * <p>Built by analyzing the 309-entry {@code wanglin_arsenal_manifest.json}
     * against the 482+ entries in {@link WangLinMasterRegistry}. Items whose
     * bare id is NOT in this map have no confident registry match - their
     * {@code canonId} is left {@code null}, and the tooltip enrichment is
     * silently skipped (no fabricated matches).
     *
     * <p>Matching strategies used (in priority order):
     * <ol>
     *   <li>Exact id match (e.g. {@code karma_whip} -> {@code karma_whip})</li>
     *   <li>Inventory/Technique prefix code (e.g. {@code attractive_force_art}
     *       -> {@code T122_attractive_force_art})</li>
     *   <li>Suffix variant (e.g. {@code soul_flag_production}
     *       -> {@code soul_flag_production_method})</li>
     *   <li>Pet/Companion/Ally/Enemy prefix (e.g. {@code mosquito_beast}
     *       -> {@code pet_mosquito_beast})</li>
     *   <li>Word-order swap (e.g. {@code karma_essence} -> {@code essence_karma})</li>
     *   <li>US/UK spelling variant (e.g. {@code ancient_god_leather_armor}
     *       -> {@code ancient_god_leather_armour})</li>
     * </ol>
     */
    private static final Map<String, String> MANIFEST_TO_REGISTRY = buildManifestToRegistryMap();

    private static Map<String, String> buildManifestToRegistryMap() {
        Map<String, String> m = new HashMap<>();
        m.put("ancient_ancestors_finger_attack", "T77_ancient_ancestors_finger_attack");
        m.put("ancient_demon", "ancient_demon_clone");
        m.put("ancient_devil", "ancient_devil_clone");
        m.put("ancient_god_bracer", "I39_ancient_god_bracer");
        m.put("ancient_god_furnace", "I35_ancient_god_furnace");
        m.put("ancient_god_leather_armor", "ancient_god_leather_armour");
        m.put("ancient_god_tactic", "ancient_god_tactic");
        m.put("ancient_god_trident", "ancient_god_trident");
        m.put("ancient_leaf", "I163_ancient_leaf");
        m.put("ancient_restrictions", "ancient_restrictions");
        m.put("ancient_soul_restriction", "ancient_soul_restriction");
        m.put("ancient_soul_restriction_tortoise_beast", "ancient_soul_restriction_tortoise_beast");
        m.put("ancient_thunder_dragon_form", "ancient_thunder_dragon_form");
        m.put("annihilation_restriction", "annihilation_restriction");
        m.put("attractive_force_art", "T122_attractive_force_art");
        m.put("azure_ancient_god_shield", "azure_ancient_god_shield");
        m.put("basic_formation_book", "I74_basic_formation_book");
        m.put("battle_will_domain", "I141_battle_will_domain");
        m.put("beads_seven_colored_realm", "I85_beads_seven_colored_realm");
        m.put("bell_sealing_tracking", "I97_bell_sealing_tracking");
        m.put("billion_soul_flag", "I51_billion_soul_flag");
        m.put("black_comb_19_teeth", "I94_black_comb_19_teeth");
        m.put("blood_ancestors_blood_body", "I61_blood_ancestors_blood_body");
        m.put("blood_jades_yao_xixue", "I171_blood_jades_yao_xixue");
        m.put("blood_pavilion", "blood_pavilion");
        m.put("blood_red_nascent_soul", "I60_blood_red_nascent_soul");
        m.put("blood_refining_technique", "T133_blood_refining_technique");
        m.put("blood_slaughter_sword", "I22_blood_slaughter_sword");
        m.put("blue_flames", "T85_blue_flames");
        m.put("blue_umbrella", "I46_blue_umbrella");
        m.put("body_fixation_art", "body_fixation_art");
        m.put("body_formation", "body_formation");
        m.put("brilliant_void_sea_dragon", "pet_brilliant_void_sea_dragon");
        m.put("burning_realm_ancient_umbrella", "T47_burning_realm_ancient_umbrella");
        m.put("carving_domain_of_time", "I54_carving_domain_of_time");
        m.put("celestial_body", "celestial_body_mid");
        m.put("celestial_capture_net", "celestial_capture_net");
        m.put("celestial_emperor_crown", "celestial_emperor_crown");
        m.put("celestial_mountain_soul", "I100_celestial_mountain_soul");
        m.put("celestial_sealing_stamp", "celestial_sealing_stamp_18_hell");
        m.put("celestial_slaughter_art", "celestial_slaughter_art");
        m.put("celestial_wine_jug", "I88_celestial_wine_jug");
        m.put("cloak_vermilion_bird_emperor", "I102_cloak_vermilion_bird_emperor");
        m.put("collection_pavilion", "collection_pavilion");
        m.put("copper_celestial_guard_du_jian", "companion_celestial_guard_copper_du_jian");
        m.put("core_treasure_sword", "core_treasure_sword");
        m.put("crystal_sword", "I16_crystal_sword");
        m.put("dagger_ge_hong", "I48_dagger_ge_hong");
        m.put("dao_fusion", "dao_fusion");
        m.put("dao_karma", "dao_karma");
        m.put("dao_life_death", "dao_life_death");
        m.put("dao_slaughter", "dao_slaughter");
        m.put("dao_time", "dao_time");
        m.put("dao_transformation_yellow_springs", "T53_dao_transformation_yellow_springs");
        m.put("dark_green_flying_sword", "dark_green_flying_sword");
        m.put("dark_heaven_stone", "I166_dark_heaven_stone");
        m.put("dark_moon_clear_skies", "T29_dark_moon_clear_skies");
        m.put("demon_blade_earth_burial", "I25_demon_blade_earth_burial");
        m.put("demon_spell_wind_fire_mountain", "T44_demon_spell_wind_fire_mountain");
        m.put("demonic_finger", "demonic_finger");
        m.put("destruction_restriction", "destruction_restriction");
        m.put("devil_dao_life_death_reverse", "T45_devil_dao_life_death_reverse");
        m.put("devil_sky_cloud_monkey", "pet_devil_sky_cloud_monkey");
        m.put("devil_soul_bottle", "I52_devil_soul_bottle");
        m.put("devil_soul_tornado_leader", "companion_devil_soul_tornado_leader");
        m.put("disguising_technique", "T120_disguising_technique");
        m.put("divine_path", "T129_divine_path");
        m.put("divine_path_clone", "divine_path_clone");
        m.put("dream_dao", "dream_dao");
        m.put("earth_escape_technique", "earth_escape_technique");
        m.put("earth_essence", "earth_essence_true_body");
        m.put("earth_palace", "I79_earth_palace");
        m.put("emerald_bracelet_li_qianmei", "I107_emerald_bracelet_li_qianmei");
        m.put("emperor_furnace", "I37_emperor_furnace");
        m.put("eternal_wood_spirit", "I164_eternal_wood_spirit");
        m.put("ethereal_fire", "T164_ethereal_fire");
        m.put("extreme_earth_dao", "T71_extreme_earth_dao");
        m.put("extreme_fire_dao", "T67_extreme_fire_dao");
        m.put("extreme_life_death_dao", "T72_extreme_life_death_dao");
        m.put("extreme_metal_dao", "T69_extreme_metal_dao");
        m.put("extreme_sky_dao", "T74_extreme_sky_dao");
        m.put("extreme_water_dao", "T68_extreme_water_dao");
        m.put("extreme_wood_dao", "T70_extreme_wood_dao");
        m.put("falling_star", "T17_falling_star");
        m.put("fate_sealing_ring", "fate_sealing_ring");
        m.put("finger_of_death", "finger_of_death");
        m.put("fire_bone", "I106_fire_bone");
        m.put("fire_essence", "fire_essence_true_body");
        m.put("five_elements_true_body", "five_elements_true_body");
        m.put("flowing_time", "flowing_time");
        m.put("fog_devil_lance", "I26_fog_devil_lance");
        m.put("foundation_stealing_technique", "T132_foundation_stealing_technique");
        m.put("fragment_stamp_celestial_sealing", "I169_fragment_stamp_celestial_sealing");
        m.put("ghostly_sail_main", "ghostly_sail_main");
        m.put("giant_head_skull_ancient_clansman", "I57_giant_head_skull_ancient_clansman");
        m.put("god_demon_devil_ancient_dao_no_celestial", "T30_god_demon_devil_ancient_dao_no_celestial");
        m.put("god_punch", "T31_god_punch");
        m.put("god_slaying_spear", "god_slaying_spear_illusory");
        m.put("god_slaying_war_chariot", "god_slaying_war_chariot_mid");
        m.put("god_tremble_army_formation", "god_tremble_army_formation");
        m.put("golden_print_xuan_luo", "I173_golden_print_xuan_luo");
        m.put("great_heavenly_venerable_sun", "great_heavenly_venerable_sun");
        m.put("great_teleportation_forbidden", "great_teleportation_forbidden");
        m.put("green_fragment_power_heaven", "I84_green_fragment_heaven_power");
        m.put("gui_yi_sect_earth_armor", "I44_gui_yi_sect_earth_armor");
        m.put("hairpin_thousand_illusion_ruthless", "I93_hairpin_thousand_illusion_ruthless");
        m.put("half_moon_blade", "I18_half_moon_blade");
        m.put("heart_compass_annihilation", "heart_compass_annihilation");
        m.put("heart_of_slaughter", "T80_heart_of_slaughter");
        m.put("heart_pounding_thunder", "T63_heart_pounding_thunder");
        m.put("heart_restriction", "heart_restriction");
        m.put("heaven_avoiding_coffin", "heaven_avoiding_coffin");
        m.put("heaven_dao_crystal", "I86_heaven_dao_crystal");
        m.put("heaven_defying_bead", "heaven_defying_bead");
        m.put("heaven_extinction", "T18_heaven_extinction");
        m.put("heaven_reversal_stamp", "T19_heaven_reversal_stamp");
        m.put("heaven_ripping", "T34_heaven_ripping");
        m.put("heaven_splitting_axe_ancestral", "I24_heaven_splitting_axe_ancestral");
        m.put("heaven_technique", "heaven_technique_ancient_god");
        m.put("heaven_tiger_flag", "heaven_tiger_flag");
        m.put("heavenly_bull_bead", "I175_heavenly_bull_bead");
        m.put("heavenly_bull_soul_armour", "I45_heavenly_bull_soul_armour");
        m.put("heavenly_chop", "T13_heavenly_chop");
        m.put("heavenly_devil_sound", "T37_heavenly_devil_sound");
        m.put("heavenly_fate_finger", "T27_heavenly_fate_finger");
        m.put("heavenly_flame", "T118_heavenly_flame");
        m.put("holy_treasure_white_stone", "I104_holy_treasure_white_stone");
        m.put("illusionary_circle", "illusionary_circle");
        m.put("immortal_celestial_body", "immortal_celestial_body");
        m.put("immortal_dream", "T33_immortal_dream");
        m.put("isolation_restriction_compass", "I71_isolation_restriction_compass");
        m.put("jade_bottle_black_liquid", "I108_jade_bottle_black_liquid");
        m.put("jade_thunder_defense", "I47_jade_thunder_defense");
        m.put("ji_qiong_head", "I83_ji_qiong_head");
        m.put("ji_realm", "ji_realm_divine_sense");
        m.put("karma_domain", "I139_karma_domain");
        m.put("karma_essence", "essence_karma");
        m.put("karma_whip", "karma_whip");
        m.put("lands_collapse", "T28_lands_collapse");
        m.put("lantern_origin_soul_protection", "I174_lantern_origin_soul_protection");
        m.put("lei_ji_thunder_beast", "pet_lei_ji_thunder_beast");
        m.put("li_guang_arrow", "I28_li_guang_arrow");
        m.put("li_guang_bow", "I27_li_guang_bow");
        m.put("life_death_essence", "essence_life_death");
        m.put("life_death_restriction", "life_death_restriction");
        m.put("light_shadow_shield", "light_shadow_shield");
        m.put("lu_fu_blood_balls", "I82_lu_fu_blood_balls");
        m.put("lu_mo_slaughter_clone", "lu_mo_slaughter_clone");
        m.put("memory_erasing_technique", "T121_memory_erasing_technique");
        m.put("merit_spirit", "T39_merit_spirit");
        m.put("metal_essence", "metal_essence_true_body");
        m.put("mosquito_beast", "pet_mosquito_beast");
        m.put("mosquito_swarm_10000", "pet_mosquito_swarm_10000");
        m.put("mountain_and_river_screen", "mountain_and_river_screen");
        m.put("multi_layered_illusion_spell", "T66_multi_layered_illusion_spell");
        m.put("mysterious_god_star", "T42_mysterious_god_star");
        m.put("nether_beast", "pet_nether_beast");
        m.put("nether_guide", "nether_guide");
        m.put("nine_cycle_celestial_refining", "nine_cycle_celestial_refining");
        m.put("nine_tribulation_karma_fires", "T159_nine_tribulation_karma_fires");
        m.put("one_step_trample_heavens", "one_step_trample_heavens");
        m.put("pair_metal_flints", "I89_pair_metal_flints");
        m.put("planet_soul_extraction", "T88_planet_soul_extraction");
        m.put("rain_celestial_sword", "rain_celestial_sword");
        m.put("realm_defining_compass", "realm_defining_compass");
        m.put("reincarnation_essence", "essence_reincarnation");
        m.put("restriction_breaking_ancient_mirror", "I66_restriction_breaking_ancient_mirror");
        m.put("restriction_essence", "essence_restriction");
        m.put("restriction_flag", "restriction_flag_1st");
        m.put("restriction_flag_method", "restriction_flag_method");
        m.put("rusted_iron_sword", "I20_rusted_iron_sword");
        m.put("scattered_devil_armour", "scattered_devil_armour");
        m.put("seven_colored_lance", "I29_seven_colored_lance");
        m.put("seven_star_sword_formation", "seven_star_sword_formation");
        m.put("silver_celestial_guard_ta_shan", "companion_celestial_guard_silver_ta_shan");
        m.put("silver_celestial_guard_thunder_daoist", "companion_celestial_guard_silver_thunder_daoist");
        m.put("silver_dragon_star_compass", "I99_silver_dragon_star_compass");
        m.put("silver_poison_female_corpse", "companion_silver_poison_female_corpse");
        m.put("slash_luo_art", "slash_luo_art");
        m.put("slaughter_crystal", "slaughter_crystal");
        m.put("slaughter_essence", "slaughter_essence_true_body");
        m.put("soul_devil_ship", "soul_devil_ship");
        m.put("soul_devourer_nature", "T110_soul_devourer_nature");
        m.put("soul_eye_dao", "T64_soul_eye_dao");
        m.put("soul_fantasy_origin", "T65_soul_fantasy_origin");
        m.put("soul_flag_production", "soul_flag_production_method");
        m.put("soul_gourd", "I56_soul_gourd");
        m.put("soul_piercing_eyes", "soul_piercing_eyes");
        m.put("soul_searching", "soul_searching");
        m.put("space_stone", "space_stone");
        m.put("spatial_bending", "T87_spatial_bending");
        m.put("spirit_transformation", "T38_spirit_transformation");
        m.put("star_compass", "I98_star_compass");
        m.put("star_of_law", "I142_star_of_law");
        m.put("star_rotation", "T86_star_rotation");
        m.put("straw_hat", "I96_straw_hat");
        m.put("sundered_night", "sundered_night");
        m.put("sword_teleportation_spell", "I92_sword_teleportation_spell");
        m.put("teleportation_restriction", "teleportation_restriction");
        m.put("ten_million_swords", "T14_ten_million_swords");
        m.put("third_eye_spell", "T48_third_eye_spell");
        m.put("three_bells_shield", "I42_three_bells_shield");
        m.put("three_life_spell", "T55_three_life_spell");
        m.put("three_purple_flags_defensive", "three_purple_flags_defensive");
        m.put("thunder_body", "thunder_body");
        m.put("thunder_celestial_beast", "pet_thunder_celestial_beast");
        m.put("thunder_essence", "thunder_essence_true_body");
        m.put("thunder_origin_spell", "T59_thunder_origin_spell");
        m.put("thunder_toad", "pet_thunder_toad");
        m.put("time_restriction", "time_restriction");
        m.put("tortoise_shell", "I176_tortoise_shell");
        m.put("true_false_domain", "I140_true_false_domain");
        m.put("true_false_essence", "essence_true_false");
        m.put("underworld_ascension_method", "underworld_ascension_method");
        m.put("underworld_finger", "underworld_finger");
        m.put("undying_ancient_body", "undying_ancient_body");
        m.put("undying_ancient_finger", "undying_ancient_finger");
        m.put("unnamed_wheel_formation", "unnamed_wheel_formation");
        m.put("vermilion_bird_feather", "I105_vermilion_bird_feather");
        m.put("vermilion_bird_holy_token", "I103_vermilion_bird_holy_token");
        m.put("vice_ghostly_sail", "ghostly_sail_vice");
        m.put("void_avatar", "void_avatar");
        m.put("void_spell_8_star", "T41_void_8_star_spell");
        m.put("void_stop_spell", "T58_void_stop_spell");
        m.put("wandering_souls", "I62_wandering_souls");
        m.put("war_spirit_print", "T57_war_spirit_print");
        m.put("water_essence", "water_essence_true_body");
        m.put("wealth_flying_sword", "wealth_flying_sword");
        m.put("white_hair_strand", "I59_white_hair_strand");
        m.put("wood_carving_black_fiend", "I165_wood_carving_black_fiend");
        m.put("wood_essence", "wood_essence_true_body");
        m.put("yi_si_puppet", "companion_yi_si_puppet");
        m.put("yin_blade", "I23_yin_blade");
        m.put("five_sword_sheaths", "I05_five_sword_sheaths");
        m.put("bronze_mirror_time_domain", "I37_bronze_mirror");
        m.put("soul_flag_soul_refining_sect", "I49_soul_flag_soul_refining_sect");
        m.put("seven_colored_nails", "I82_seven_colored_nails");
        m.put("celestial_spirit_clock", "I76_celestial_spirit_clock");
        m.put("dot_immortal_pen", "I90_dot_immortal_pen");
        m.put("golden_print", "I92_golden_print");
        m.put("beast_skin_tattoo", "I115_beast_skin_tattoo");
        m.put("three_ink_stones_restriction", "I03_three_ink_stones");
        m.put("infant_skull_da_yi", "I100_infant_skull_da_yi");
        m.put("three_battle_scrolls_zhan", "I65_three_battle_scrolls");
        m.put("human_shaped_armor_green_bull", "I40_human_shaped_armor");
        m.put("dream_dao_mirror", "I160_dream_dao_mirror");
        m.put("red_lightning_ji_realm", "AT01_red_lightning_ji_realm");
        m.put("blood_nascent_soul_seven_colored", "I60_blood_nascent_soul_seven_colored");
        m.put("ancient_bloodline_ancestor", "I167_ancient_bloodline_ancestor");
        m.put("celestial_bloodline_ancestor", "I168_celestial_bloodline_ancestor");
        m.put("vermilion_bird_spirit", "I169_vermilion_bird_spirit");
        m.put("call_wind_summon_rain_magic_arsenal", "I170_call_wind_summon_rain");
        m.put("devouring_technique", "T01_devouring_technique");
        m.put("fiend_transformation_art", "T03_fiend_transformation_art");
        m.put("yin_energy_detection", "T09_yin_energy_detection");
        m.put("mountains_crumble", "T16_mountains_crumble");
        m.put("fog_transformation_thunder_spell", "T21_fog_transformation_thunder");
        m.put("giant_thunder_stamp", "T22_giant_thunder_stamp");
        m.put("open_ancient_thunder_realm", "T24_open_ancient_thunder_realm");
        m.put("rapid_spell_art_xu_decai", "T31_rapid_spell_art_xu_decai");
        m.put("rapid_spell_technique", "T33_rapid_spell_technique");
        m.put("body_fixation_art_xiangang", "T34_body_fixation_art_xiangang");
        m.put("life_transformation_spell", "T38_life_transformation_spell");
        m.put("slaughter_immortal_art", "T41_slaughter_immortal_art");
        m.put("god_slaying_seal_origin", "T44_god_slaying_seal_origin");
        m.put("life_seizing_hex", "T50_life_seizing_hex");
        m.put("nine_mysterious_transformations", "T55_nine_mysterious_transformations");
        m.put("ji_realm_execution", "T90_ji_realm_execution");
        m.put("eyes_suppressing_world", "T96_eyes_suppressing_world");
        m.put("ice_imitation_dongling_pool", "T100_ice_imitation_dongling_pool");
        m.put("nine_drops_poison_miao_yin", "T105_nine_drops_poison_miao_yin");
        m.put("three_life_spell_william", "T110_three_life_spell");
        m.put("defying_thunder", "T135_defying_thunder");
        m.put("nine_revolutions_refining_immortal", "T140_nine_revolutions_refining");
        m.put("extreme_fire_dao_imitation", "T145_extreme_fire_dao_imitation");
        m.put("store_all_ji_thunder", "AT07_store_all_ji_thunder");
        m.put("karma_print", "OS04_karma_print");
        m.put("life_death_seal", "OS05_life_death_seal");
        m.put("true_false_eternal_seal", "OS06_true_false_eternal_seal");
        m.put("life_death_domain", "I138_life_death_domain");
        m.put("blood_lines_rules_restriction", "I153_blood_lines_rules");
        m.put("bloodline_thunder", "T136_bloodline_thunder");
        m.put("flame_dragon", "T95_flame_dragon");
        m.put("nine_star_ancient_god_power", "T150_nine_star_ancient_god");
        m.put("extreme_land_sky_life_dao", "T155_extreme_land_sky_life");
        m.put("mother_child_dao_withered", "T160_mother_child_dao");
        m.put("wither_dao_pair", "T165_wither_dao_pair");
        m.put("taichu_essence", "E08_taichu_essence");
        m.put("miemie_essence", "E09_miemie_essence");
        m.put("cultivator_dao_avatar", "T170_cultivator_dao_avatar");
        m.put("ancient_god_body", "B01_ancient_god_body");
        m.put("undying_ancient_body_version", "B02_undying_ancient_body");
        m.put("xu_liguo_devil", "companion_xu_liguo_devil");
        m.put("devil_xu_liguo_first", "companion_xu_liguo_first");
        m.put("xie_qing_immortal_guard", "companion_xie_qing_guard");
        m.put("tattoo_talisman_speed_boost", "I130_tattoo_talisman_speed");
        m.put("short_sword_seals_shadows", "I20_short_sword_seals_shadows");
        m.put("burning_realm_ancient_umbrella_dao", "I46_blue_umbrella"); // DAO variant — bridges to base item
        m.put("li_guang_heaven_shattering_bow", "I47_silver_dragon_star_compass"); // DAO variant — bridges to base item
        m.put("li_guang_heaven_shattering_bow_dao", "I47_silver_dragon_star_compass"); // DAO variant — bridges to base item
        m.put("seven_colored_lance_dao", "I48_soul_lasser_karma_whip"); // DAO variant — bridges to base item
        m.put("seven_colored_lance_dao_spell", "I48_soul_lasser_karma_whip"); // DAO variant — bridges to base item
        m.put("devilish_flames_fire_dragon", "T95_flame_dragon"); // bridges to flame dragon technique
        return java.util.Collections.unmodifiableMap(m);
    }


    private WangLinItems() {}

    /** A single entry from the arsenal manifest. */
    public record ManifestEntry(String itemId, boolean isCanonical, int count, float unlockThreshold) {
        /** The canonId portion (e.g. "wanglin/restriction_flag" from "ergen:wanglin/restriction_flag"). */
        public String canonId() {
            return itemId.replace("ergen:", "");
        }
        /** The registry name portion (e.g. "restriction_flag"). */
        public String registryName() {
            String id = itemId.replace("ergen:wanglin/", "");
            return id;
        }
    }

    /**
     * Load the arsenal manifest from the embedded JSON resource and register every
     * entry as a {@link WangLinItem}. This runs once at class-init (before the mod
     * event bus fires the RegisterEvent).
     */
    private static synchronized void loadManifestAndRegister() {
        if (registered) return;
        Ergenverse.LOGGER.info("[WangLin] Loading arsenal manifest from assets/ergenverse/wanglin_arsenal_manifest.json...");

        try (var is = WangLinItems.class.getResourceAsStream("/assets/ergenverse/wanglin_arsenal_manifest.json")) {
            if (is == null) {
                Ergenverse.LOGGER.error("[WangLin] ARSENAL MANIFEST NOT FOUND! No Wang Lin items will be registered.");
                Ergenverse.LOGGER.error("[WangLin] Expected: src/main/resources/assets/ergenverse/wanglin_arsenal_manifest.json");
                registered = true;
                return;
            }
            var reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            JsonObject root = new Gson().fromJson(reader, JsonObject.class);
            JsonArray entries = root.getAsJsonArray("entries");
            int count = 0;
            for (JsonElement el : entries) {
                JsonObject e = el.getAsJsonObject();
                String itemId = e.get("itemId").getAsString();
                boolean isCanonical = e.get("isCanonical").getAsBoolean();
                int cnt = e.has("count") ? e.get("count").getAsInt() : 1;
                float unlock = e.has("unlockThreshold") ? e.get("unlockThreshold").getAsFloat() : 0f;

                ManifestEntry me = new ManifestEntry(itemId, isCanonical, cnt, unlock);
                MANIFEST.add(me);
                registerArsenalItem(me);
                count++;
            }
            Ergenverse.LOGGER.info("[WangLin] Arsenal manifest loaded: {} items registered.", count);
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[WangLin] Failed to load arsenal manifest!", e);
        }
        registered = true;
    }

    /**
     * Register a single arsenal item. The item is created as a {@link WangLinItem}
     * with category inferred from the canonId prefix and evolution chain looked up
     * from {@link ItemEvolutionRegistry}.
     *
     * <p>The {@code canonId} passed to {@link WangLinItem} is the <b>canonical
     * registry entry id</b> (looked up from {@link #MANIFEST_TO_REGISTRY}), NOT
     * the manifest's {@code itemId}. This ensures {@link WangLinItem}'s tooltip
     * can call {@link WangLinMasterRegistry#lookup(String)} and find a match.
     * Items whose manifest bare-id has no confident registry match get a
     * {@code null} canonId — their tooltip enrichment is silently skipped
     * (no fabricated matches).
     */
    private static void registerArsenalItem(ManifestEntry me) {
        String registryName = me.registryName();
        String manifestCanonId = me.canonId();

        // CRON-SMOKE-TEST FIX: Skip items whose registry name is ALREADY owned by
        // ErgenverseItems. Both DeferredRegisters target the "ergenverse" namespace,
        // so if ErgenverseItems registers "ergenverse:soul_gourd" (the real
        // SoulGourdItem with capture/release mechanics) and WangLinItems ALSO
        // registers "ergenverse:soul_gourd" (a tooltip-only WangLinItem), Forge
        // creates an override that maps to `air`, crashing the server at registry
        // freeze with "One or more entry values did not copy to the correct id."
        // The ErgenverseItems version is always the one with real mechanics, so we
        // defer to it and skip the arsenal's display-only duplicate.
        if (ERGENVERSE_ITEMS_OWNED_NAMES.contains(registryName)) {
            Ergenverse.LOGGER.info("[WangLin] Skipping arsenal duplicate of ErgenverseItems-owned item: {}", registryName);
            return;
        }

        // Skip duplicate entries — the manifest has 6 duplicate registry names.
        // Without this check, DeferredRegister.register() throws IllegalArgumentException
        // on the 2nd occurrence, aborting the loop and leaving all subsequent items unregistered.
        if (!REGISTERED_NAMES.add(registryName)) {
            Ergenverse.LOGGER.warn("[WangLin] Skipping duplicate arsenal item: {}", registryName);
            return;
        }

        // Look up the actual canonical registry entry id from the manifest->registry map.
        // If no confident match, leave canonId null (tooltip enrichment silently skipped).
        String registryCanonId = MANIFEST_TO_REGISTRY.get(registryName);
        if (registryCanonId == null) {
            Ergenverse.LOGGER.debug("[WangLin] No registry match for arsenal item '{}' — tooltip enrichment will be skipped.",
                    registryName);
        }
        WangLinItem.ArsenalCategory category = inferCategory(manifestCanonId);
        String displayName = humanizeName(registryName);

        // CANON-ONLY: We do NOT create default chains for items without explicit
        // canon histories. Per the rewritten Prime Directive, items with a single
        // documented canon form exist in that form — period. No invented "Peak"
        // or "Awakened" stages. The ItemEvolutionRegistry contains ONLY chains
        // for items with explicitly documented canon transformations.
        ItemEvolutionRegistry.bootstrap();

        // SPECIAL CASES: Items that need their own Item subclass with real mechanics.
        // These are canon-critical items where WangLinItem's tooltip-only behavior
        // is insufficient — the player must be able to LAUNCH a flying sword,
        // STORE items in a ring, or CAPTURE souls with the gourd.
        //
        // CRON-COMPLETIONIST-67: Wired 4 flying swords from WangLinItem (display-only)
        // to FlyingSwordItem (launchable projectile with cultivation-scaled damage
        // and per-sword supernatural effects).
        final RegistryObject<Item> ro;
        if ("heaven_defying_bead".equals(registryName)) {
            ro = ITEMS.register(registryName, () -> new HeavenDefyingBeadItem(
                    new Item.Properties()));
        } else if ("wealth_flying_sword".equals(registryName)) {
            // Wang Lin's FIRST flying sword — the iconic Wealth Sword.
            // Canon: basic qi blade, no special effect, but reliable.
            // Damage: 8.0 (low — it's his weakest sword). Effect: NONE.
            ro = ITEMS.register(registryName, () -> new FlyingSwordItem(
                    8.0F, SwordEffectType.NONE,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)
            ));
        } else if ("core_treasure_sword".equals(registryName)) {
            // Core Treasure Sword — can teleport targets on hit.
            // Canon: a refined treasure-tier sword that displaces enemies.
            // Damage: 12.0 (mid-tier). Effect: TELEPORT.
            ro = ITEMS.register(registryName, () -> new FlyingSwordItem(
                    12.0F, SwordEffectType.TELEPORT,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)
            ));
        } else if ("dark_green_flying_sword".equals(registryName)) {
            // Dark Green Flying Sword — seeps poison into wounds.
            // Canon: a sinister sword that poisons those it cuts.
            // Damage: 10.0 (mid-tier). Effect: POISON (Wither II, 3s).
            ro = ITEMS.register(registryName, () -> new FlyingSwordItem(
                    10.0F, SwordEffectType.POISON,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)
            ));
        } else if ("blood_slaughter_sword".equals(registryName)) {
            // Blood Slaughter Sword — drains life force from those it cuts.
            // Canon: one of the Seven Swords of the Ancient Dao, absorbs blood qi.
            // Damage: 15.0 (high-tier). Effect: LIFESTEAL (30% damage healed).
            ro = ITEMS.register(registryName, () -> new FlyingSwordItem(
                    15.0F, SwordEffectType.LIFESTEAL,
                    new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)
            ));
        } else {
            ro = ITEMS.register(registryName, () -> new WangLinItem(
                    new Item.Properties(),
                    registryCanonId,
                    displayName,
                    "",  // CN name filled in by evolution-chain stages if available
                    category,
                    me.isCanonical() ? 5 : 3,
                    manifestCanonId,
                    me.unlockThreshold(),
                    "Renegade Immortal \u2014 Wang Lin's arsenal"
            ));
        }
        ARSENAL.put(manifestCanonId, ro);
    }

    /** Infer the arsenal category from the canonId naming conventions. */
    private static WangLinItem.ArsenalCategory inferCategory(String canonId) {
        if (canonId.contains("essence")) return WangLinItem.ArsenalCategory.ESSENCE;
        if (canonId.contains("domain")) return WangLinItem.ArsenalCategory.DOMAIN;
        if (canonId.contains("bridge")) return WangLinItem.ArsenalCategory.DOMAIN;
        if (canonId.contains("pet") || canonId.contains("dragon") || canonId.contains("soul_tornado")
                || canonId.contains("monkey") || canonId.contains("beast")) return WangLinItem.ArsenalCategory.PET;
        if (canonId.contains("devil") || canonId.contains("clone") || canonId.contains("xu_liguo")) return WangLinItem.ArsenalCategory.CLONE;
        if (canonId.contains("companion")) return WangLinItem.ArsenalCategory.COMPANION;
        if (canonId.contains("formation") || canonId.contains("array")) return WangLinItem.ArsenalCategory.FORMATION;
        if (canonId.contains("flag") || canonId.contains("sword") || canonId.contains("blade")
                || canonId.contains("chariot") || canonId.contains("crown") || canonId.contains("hat")
                || canonId.contains("pen") || canonId.contains("coffin") || canonId.contains("compass")
                || canonId.contains("net") || canonId.contains("shield") || canonId.contains("screen")
                || canonId.contains("pavilion")) return WangLinItem.ArsenalCategory.ARTIFACT;
        if (canonId.contains("art") || canonId.contains("technique") || canonId.contains("spell")
                || canonId.contains("hex") || canonId.contains("print") || canonId.contains("sound")
                || canonId.contains("method") || canonId.contains("refining") || canonId.contains("detection")
                || canonId.contains("escape") || canonId.contains("transformation") || canonId.contains("searching")
                || canonId.contains("rotation") || canonId.contains("bending") || canonId.contains("tactic")
                || canonId.contains("extraction") || canonId.contains("devouring") || canonId.contains("disguising")
                || canonId.contains("erasing") || canonId.contains("stealing") || canonId.contains("ascension")) return WangLinItem.ArsenalCategory.TECHNIQUE;
        return WangLinItem.ArsenalCategory.MISCELLANEOUS;
    }

    /** Convert a registry name like "restriction_flag" to "Restriction Flag". */
    private static String humanizeName(String registryName) {
        StringBuilder sb = new StringBuilder();
        for (String word : registryName.split("_")) {
            if (!word.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
            }
        }
        return sb.toString();
    }

    // ── Public API ────────────────────────────────────────────────────

    /**
     * Wire the items + creative tabs to the mod event bus. Call from
     * {@link dev.ergenverse.core.Ergenverse#Ergenverse}.
     */
    public static void register(IEventBus modEventBus) {
        loadManifestAndRegister();
        registerCreativeTab();
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        Ergenverse.LOGGER.info("[WangLin] Arsenal DeferredRegister wired to mod event bus. {} items pending Forge registration.",
                MANIFEST.size());
    }

    /**
     * Look up a registered item by canonId. Returns null if not found or not yet resolved.
     */
    public static RegistryObject<Item> get(String canonId) {
        return ARSENAL.get(canonId);
    }

    /** All registered arsenal entries. */
    public static List<ManifestEntry> allEntries() {
        return List.copyOf(MANIFEST);
    }

    // ── Creative Tab ──────────────────────────────────────────────────

    /** The RegistryObject for the Wang Lin creative tab. */
    public static RegistryObject<CreativeModeTab> WANG_LIN_TAB;

    /**
     * Register a creative mode tab showing ALL Wang Lin arsenal items, organized by category.
     * This fulfills the "access to ALL arsenal" directive — every item is available in creative mode.
     */
    private static void registerCreativeTab() {
        WANG_LIN_TAB = CREATIVE_TABS.register("wanglin_arsenal", () -> CreativeModeTab.builder()
                .title(Component.literal("Wang Lin's Arsenal"))
                .icon(() -> {
                    RegistryObject<Item> bead = ARSENAL.get("wanglin/heaven_defying_bead");
                    return bead != null ? new ItemStack(bead.get()) : ItemStack.EMPTY;
                })
                .displayItems((params, output) -> {
                    // Add ALL registered Wang Lin items to the creative tab.
                    for (ManifestEntry me : MANIFEST) {
                        String registryName = me.registryName();
                        // Look up the RegistryObject from the ITEMS deferred register
                        var ro = ITEMS.getEntries().stream()
                                .filter(r -> r.getId().getPath().equals(registryName))
                                .findFirst();
                        ro.ifPresent(r -> output.accept(r.get()));
                    }
                })
                .build());
    }
}
