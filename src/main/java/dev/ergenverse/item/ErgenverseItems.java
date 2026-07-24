package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * ErgenverseItems — DeferredRegister for utility items that don't belong to
 * Wang Lin's personal arsenal (which is in {@link dev.ergenverse.wanglin.WangLinItems}).
 *
 * <p>Currently registers:
 * <ul>
 *   <li><b>jade_slip</b> — a cultivator's jade slip. Used to channel Qi from
 *       spirit-vein and spirit-herb blocks. Canon: jade slips are the standard
 *       storage and Qi-channeling medium in xianxia; Wang Lin uses one for
 *       soul-searching and qi-gathering throughout the series.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ErgenverseItems {

    private ErgenverseItems() {}

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ergenverse.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ergenverse.MOD_ID);

    /**
     * Jade Slip — a Qi-channeling medium.
     * Right-click on a spirit-vein or spirit-herb block to gather Qi.
     * One-time use per block per MC day (recharges over time).
     */
    public static final RegistryObject<Item> JADE_SLIP = ITEMS.register("jade_slip",
            () -> new Item(new Item.Properties()
                    .durability(64)  // 64 uses before repair needed
                    .rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    /** Ergenverse utility items creative tab. */
    public static RegistryObject<CreativeModeTab> ERGENVERSE_ITEMS_TAB;


    // ── Crafting Materials ─────────────────────────────────────────────
    // NOTE: spirit_stone item is provided by ErgenverseBlocks.registerSimple("spirit_stone")
    // which auto-creates a block item. Do NOT register a duplicate here.
    public static final RegistryObject<Item> SPIRIT_STONE_FRAGMENT = ITEMS.register("spirit_stone_fragment",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> IRON_SAND = ITEMS.register("iron_sand",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> COLD_IRON_INGOT = ITEMS.register("cold_iron_ingot",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_IRON_INGOT = ITEMS.register("spirit_iron_ingot",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BEAST_BONE = ITEMS.register("beast_bone",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> BEAST_CORE = ITEMS.register("beast_core",
            () -> new BeastCoreItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> WOLF_CORE = ITEMS.register("wolf_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> RABBIT_BLOOD_ESSENCE = ITEMS.register("rabbit_blood_essence",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_HERB_SEED = ITEMS.register("spirit_herb_seed",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_INK = ITEMS.register("spirit_ink",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FORMATION_FLAG_BLANK = ITEMS.register("formation_flag_blank",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> TALISMAN_PAPER_BLANK = ITEMS.register("talisman_paper_blank",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MEDITATION_MAT = ITEMS.register("meditation_mat",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CAVE_DWELLING_CORE = ITEMS.register("cave_dwelling_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    // ── Pills ──────────────────────────────────────────────────────────
    // CRON-COMPLETIONIST-57: Replaced generic Item pills with SpiritPillItem (real mechanics).
    // Constitution Article III: "Never design progression. Design reality."
    // A pill is not a generic item — it is a substance with specific pharmacological effects.
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> QI_GATHERING_PILL =
            ITEMS.register("qi_gathering_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.QI_GATHERING,
                    new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> FOUNDATION_PILL =
            ITEMS.register("foundation_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.FOUNDATION,
                    new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> PURIFICATION_PILL =
            ITEMS.register("purification_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.PURIFICATION,
                    new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> SOUL_MENDING_PILL =
            ITEMS.register("soul_mending_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.SOUL_MENDING,
                    new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    // CRON-COMPLETIONIST-57: WASTE_PILL — failed alchemy product (Nausea + Poison)
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> WASTE_PILL =
            ITEMS.register("waste_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.WASTE_PILL,
                    new Item.Properties().stacksTo(16)));


    // ── Blank Jade Slip (for inscription) ──────────────────────────────
    public static final RegistryObject<Item> JADE_SLIP_BLANK = ITEMS.register("jade_slip_blank",
            () -> new Item(new Item.Properties()));

    // ── Flying Swords (CRON-COMPLETIONIST-55/57) ───────────────────────────
    // NOTE: wealth_flying_sword, core_treasure_sword, blood_slaughter_sword, and
    // dark_green_flying_sword are registered by WangLinItems from the arsenal manifest.
    // Do NOT register them here to avoid duplicate item name crashes.
    // Only god_slaying_sword is exclusive to ErgenverseItems (not in the manifest).
    public static final RegistryObject<dev.ergenverse.item.FlyingSwordItem> GOD_SLAYING_SWORD =
            ITEMS.register("god_slaying_sword", () -> new dev.ergenverse.item.FlyingSwordItem(28.0F,
                    dev.ergenverse.item.sword.SwordEffectType.RESTRICTION,
                    new Item.Properties().durability(3000).rarity(net.minecraft.world.item.Rarity.EPIC)));

    // Talismans: single-use, right-click deploys the effect, consumed on use.
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> FIREBALL_TALISMAN =
            ITEMS.register("fireball_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.FIREBALL, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> BARRIER_TALISMAN =
            ITEMS.register("barrier_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.BARRIER, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> LIGHTNING_TALISMAN =
            ITEMS.register("lightning_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.LIGHTNING, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> SHIELD_TALISMAN =
            ITEMS.register("shield_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.SHIELD, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> SWORD_QI_TALISMAN =
            ITEMS.register("sword_qi_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.SWORD_QI, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> SPEED_BOOST_TALISMAN =
            ITEMS.register("speed_boost_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.SPEED_BOOST, new Item.Properties().stacksTo(16)));
    public static final RegistryObject<dev.ergenverse.item.TalismanItem> LIGHT_TALISMAN =
            ITEMS.register("light_talisman", () -> new dev.ergenverse.item.TalismanItem(
                    dev.ergenverse.item.TalismanType.LIGHT, new Item.Properties().stacksTo(16)));

    // Spirit pills: consumed via eat animation, apply realm-appropriate buffs.
    // CRON-COMPLETIONIST-57: _REAL suffix pills now alias the base names.
    // Kept as separate registrations for backward compatibility with saved-item NBT.
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> QI_GATHERING_PILL_REAL =
            ITEMS.register("qi_gathering_pill_real", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.QI_GATHERING, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> FOUNDATION_PILL_REAL =
            ITEMS.register("foundation_pill_real", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.FOUNDATION, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> PURIFICATION_PILL_REAL =
            ITEMS.register("purification_pill_real", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.PURIFICATION, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> SOUL_MENDING_PILL_REAL =
            ITEMS.register("soul_mending_pill_real", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.SOUL_MENDING, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> BLOOD_SOUL_PILL =
            ITEMS.register("blood_soul_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.BLOOD_SOUL, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.SpiritPillItem> MINOR_HEALING_PILL =
            ITEMS.register("minor_healing_pill", () -> new dev.ergenverse.item.SpiritPillItem(
                    dev.ergenverse.item.PillType.MINOR_HEALING, new Item.Properties().stacksTo(16)));


    // ── CRON-COMPLETIONIST-67: Canon Storage Ring & Soul Gourd ─────────────────
    // Storage Ring: 9-slot pocket-dimension inventory, persists across death.
    // Canon: THE standard storage tool for every cultivator above Qi Condensation.
    public static final RegistryObject<Item> STORAGE_RING = ITEMS.register("storage_ring",
            () -> new StorageRingItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    // Soul Gourd: captures souls from slain entities, releases as area attack.
    // Canon: feared soul-capturing treasure, prevents reincarnation.
    public static final RegistryObject<Item> SOUL_GOURD = ITEMS.register("soul_gourd",
            () -> new SoulGourdItem(new Item.Properties().durability(500).rarity(net.minecraft.world.item.Rarity.RARE)));
    // Cultivation Journal: records observations, persists across death.
    // Canon: cultivators keep detailed journals of insights.
    public static final RegistryObject<Item> CULTIVATION_JOURNAL = ITEMS.register("cultivation_journal",
            () -> new JournalItem(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));

    // ── CRON-72: Spirit Stone Currency (loot table items) ──────────────
    // Canon: spirit stones are the universal currency of the cultivation world.
    // Low/Mid/High tiers correspond to Qi Condensation/Foundation/Core Formation value ranges.
    // NOTE: spirit_stone block item is registered by ErgenverseBlocks.registerSimple("spirit_stone").
    // Do NOT register a duplicate here — would crash at registry freeze.
    public static final RegistryObject<Item> SPIRIT_STONE_LOW = ITEMS.register("spirit_stone_low",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_STONE_MID = ITEMS.register("spirit_stone_mid",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPIRIT_STONE_HIGH = ITEMS.register("spirit_stone_high",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> IMMORTAL_STONE = ITEMS.register("immortal_stone",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));

    // ── CRON-72: Beast Cores & Essences (mob drops) ────────────────────
    // Canon: cores harvested from spirit beasts are used for beast taming,
    // alchemy, and restriction artifacts.
    public static final RegistryObject<Item> ANCIENT_GOD_CORE = ITEMS.register("ancient_god_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> ANCIENT_GOD_BONE = ITEMS.register("ancient_god_bone",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> AZURE_DRAGON_CORE = ITEMS.register("azure_dragon_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> CLOUD_WHALE_CORE = ITEMS.register("cloud_whale_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> LEI_JI_CORE = ITEMS.register("lei_ji_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> NETHER_CORE = ITEMS.register("nether_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> THUNDER_TOAD_CORE = ITEMS.register("thunder_toad_core",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SOUL_FRAGMENT = ITEMS.register("soul_fragment",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SOUL_LASHER = ITEMS.register("soul_lasher",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> SPIRIT_VEIN_ESSENCE = ITEMS.register("spirit_vein_essence",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> BLOOD_ESSENCE = ITEMS.register("blood_essence",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> DRAGON_SCALE = ITEMS.register("dragon_scale",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    // vermilion_bird_feather: registered by WangLinItems arsenal (do NOT duplicate)
    public static final RegistryObject<Item> NINE_COLOR_FLAME = ITEMS.register("nine_color_flame",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> TRIBULATION_FRAGMENT = ITEMS.register("tribulation_fragment",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> DAO_FRAGMENT = ITEMS.register("dao_fragment",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    // ji_realm: registered by WangLinItems arsenal (do NOT duplicate)

    // ── CRON-72: Equipment & Artifacts (treasure items) ──────────────────
    public static final RegistryObject<Item> SPIRIT_ARMOR = ITEMS.register("spirit_armor",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    // karma_whip: registered by WangLinItems arsenal (do NOT duplicate)
    public static final RegistryObject<Item> HEAVEN_FAN = ITEMS.register("heaven_fan",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    // heaven_defying_bead: registered by WangLinItems arsenal (do NOT duplicate)
    public static final RegistryObject<Item> STAR_SEALING_FLAG = ITEMS.register("star_sealing_flag",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> SOUL_REFINING_FLAG = ITEMS.register("soul_refining_flag",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> CAVE_WORLD_KEY = ITEMS.register("cave_world_key",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> STARSKY_TOKEN = ITEMS.register("starry_sky_token",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<Item> EIGHTEEN_HELL_STAMP = ITEMS.register("eighteen_hell_stamp",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> VERMILION_EMPEROR_SEAL = ITEMS.register("vermilion_emperor_seal",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.EPIC)));
    public static final RegistryObject<Item> STORAGE_POUCH = ITEMS.register("storage_pouch",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> CULTIVATION_MAT = ITEMS.register("cultivation_mat",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    // Generic flying sword for loot tables (non-Wang-Lin-specific)
    public static final RegistryObject<Item> FLYING_SWORD = ITEMS.register("flying_sword",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    // ── Spawn Eggs ────────────────────────────────────────────────────
    // Uses DeferredSpawnEggItem because SpawnEggItem requires EntityType at
    // construction time, but ITEM registry fires BEFORE ENTITY_TYPE registry.
    public static final RegistryObject<Item> SPIRIT_RABBIT_SPAWN_EGG = ITEMS.register("spirit_rabbit_spawn_egg",
            () -> new dev.ergenverse.item.DeferredSpawnEggItem(
                    () -> (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>) dev.ergenverse.entity.EREntityTypes.SPIRIT_RABBIT.get(),
                    0xFFFFFF, 0xC0C0C0,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_WOLF_SPAWN_EGG = ITEMS.register("spirit_wolf_spawn_egg",
            () -> new dev.ergenverse.item.DeferredSpawnEggItem(
                    () -> (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>) dev.ergenverse.entity.EREntityTypes.SPIRIT_WOLF.get(),
                    0x5A6E82, 0x00C8DC,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_DEER_SPAWN_EGG = ITEMS.register("spirit_deer_spawn_egg",
            () -> new dev.ergenverse.item.DeferredSpawnEggItem(
                    () -> (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>) dev.ergenverse.entity.EREntityTypes.SPIRIT_DEER.get(),
                    0x8C643C, 0xFFC832,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_HAWK_SPAWN_EGG = ITEMS.register("spirit_hawk_spawn_egg",
            () -> new dev.ergenverse.item.DeferredSpawnEggItem(
                    () -> (net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>) dev.ergenverse.entity.EREntityTypes.SPIRIT_HAWK.get(),
                    0x785032, 0x00C8DC,
                    new Item.Properties()));

    // ── CRON-COMPLETIONIST-40: Canon Technique Scrolls ─────────────────────
    // Right-click to study, consumes scroll, grants temporary cultivation buff.
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_QI_GATHERING =
            ITEMS.register("scroll_qi_gathering", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.QI_GATHERING, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_SWORD_TECHNIQUE =
            ITEMS.register("scroll_sword_technique", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.SWORD_TECHNIQUE, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_BODY_REFINEMENT =
            ITEMS.register("scroll_body_refinement", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.BODY_REFINEMENT, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_FIRE_CONTROL =
            ITEMS.register("scroll_fire_control", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.FIRE_CONTROL, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_SPIRITUAL_SENSE =
            ITEMS.register("scroll_spiritual_sense", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.SPIRITUAL_SENSE, new Item.Properties().stacksTo(16).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.TechniqueScrollItem> SCROLL_RESTRICTION_ART =
            ITEMS.register("scroll_restriction_art", () -> new dev.ergenverse.item.TechniqueScrollItem(
                    dev.ergenverse.item.ScrollType.RESTRICTION_ART, new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC)));

    // ── CRON-COMPLETIONIST-40: Canon Sect Banners ─────────────────────────
    // Right-click to plant, 16-block aura, buffs allies, consumed.
    public static final RegistryObject<dev.ergenverse.item.SectBannerItem> BANNER_HENG_YUE =
            ITEMS.register("banner_heng_yue", () -> new dev.ergenverse.item.SectBannerItem(
                    dev.ergenverse.item.BannerType.HENG_YUE, new Item.Properties().stacksTo(8).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.SectBannerItem> BANNER_TENG_FAMILY =
            ITEMS.register("banner_teng_family", () -> new dev.ergenverse.item.SectBannerItem(
                    dev.ergenverse.item.BannerType.TENG_FAMILY, new Item.Properties().stacksTo(8).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.SectBannerItem> BANNER_TIAN_SHUI =
            ITEMS.register("banner_tian_shui", () -> new dev.ergenverse.item.SectBannerItem(
                    dev.ergenverse.item.BannerType.TIAN_SHUI, new Item.Properties().stacksTo(8).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.SectBannerItem> BANNER_SOUL_REFINING =
            ITEMS.register("banner_soul_refining", () -> new dev.ergenverse.item.SectBannerItem(
                    dev.ergenverse.item.BannerType.SOUL_REFINING, new Item.Properties().stacksTo(8).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.SectBannerItem> BANNER_XUAN_DAO =
            ITEMS.register("banner_xuan_dao", () -> new dev.ergenverse.item.SectBannerItem(
                    dev.ergenverse.item.BannerType.XUAN_DAO, new Item.Properties().stacksTo(8).rarity(net.minecraft.world.item.Rarity.RARE)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ERGENVERSE_ITEMS_TAB = CREATIVE_TABS.register("ergenverse_items", () ->
                CreativeModeTab.builder()
                        .title(Component.literal("Ergenverse Items"))
                        .icon(() -> new ItemStack(JADE_SLIP.get()))
                        .displayItems((params, output) -> {
                            // CRON-COMPLETIONIST-57: Populate creative tab with ALL items
                            output.accept(JADE_SLIP.get());
                            output.accept(JADE_SLIP_BLANK.get());
                            output.accept(MEDITATION_MAT.get());
                            // Crafting materials
                            output.accept(dev.ergenverse.block.ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().asItem());
                            output.accept(SPIRIT_STONE_FRAGMENT.get());
                            output.accept(IRON_SAND.get());
                            output.accept(COLD_IRON_INGOT.get());
                            output.accept(SPIRIT_IRON_INGOT.get());
                            output.accept(BEAST_BONE.get());
                            output.accept(BEAST_CORE.get());
                            output.accept(WOLF_CORE.get());
                            output.accept(RABBIT_BLOOD_ESSENCE.get());
                            output.accept(SPIRIT_HERB_SEED.get());
                            output.accept(SPIRIT_INK.get());
                            output.accept(FORMATION_FLAG_BLANK.get());
                            output.accept(TALISMAN_PAPER_BLANK.get());
                            output.accept(CAVE_DWELLING_CORE.get());
                            // Pills
                            output.accept(QI_GATHERING_PILL.get());
                            output.accept(FOUNDATION_PILL.get());
                            output.accept(PURIFICATION_PILL.get());
                            output.accept(SOUL_MENDING_PILL.get());
                            output.accept(BLOOD_SOUL_PILL.get());
                            output.accept(MINOR_HEALING_PILL.get());
                            output.accept(WASTE_PILL.get());
                            // Flying swords (registered by WangLinItems, not here)
                            output.accept(GOD_SLAYING_SWORD.get());
                            // Talismans
                            output.accept(FIREBALL_TALISMAN.get());
                            output.accept(BARRIER_TALISMAN.get());
                            output.accept(LIGHTNING_TALISMAN.get());
                            output.accept(SHIELD_TALISMAN.get());
                            output.accept(SWORD_QI_TALISMAN.get());
                            output.accept(SPEED_BOOST_TALISMAN.get());
                            output.accept(LIGHT_TALISMAN.get());
                            // Scrolls
                            output.accept(SCROLL_QI_GATHERING.get());
                            output.accept(SCROLL_SWORD_TECHNIQUE.get());
                            output.accept(SCROLL_BODY_REFINEMENT.get());
                            output.accept(SCROLL_FIRE_CONTROL.get());
                            output.accept(SCROLL_SPIRITUAL_SENSE.get());
                            output.accept(SCROLL_RESTRICTION_ART.get());
                            // Banners
                            output.accept(BANNER_HENG_YUE.get());
                            output.accept(BANNER_TENG_FAMILY.get());
                            output.accept(BANNER_TIAN_SHUI.get());
                            output.accept(BANNER_SOUL_REFINING.get());
                            output.accept(BANNER_XUAN_DAO.get());
                            // CRON-COMPLETIONIST-67: Canon items
                            output.accept(STORAGE_RING.get());
                            output.accept(SOUL_GOURD.get());
                            output.accept(CULTIVATION_JOURNAL.get());
                        })
                        .build());
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseItems: registered jade_slip utility item.");
    }
}
