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
    public static final RegistryObject<Item> SPIRIT_STONE = ITEMS.register("spirit_stone",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
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
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
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

    // ── Real-mechanic items (Constitution: items must have real mechanics, not generic stubs) ──
    // Flying swords: right-click launches a homing qi-blade projectile. Left-click = melee.
    // CRON-COMPLETIONIST-55: each sword now carries a canon-faithful SwordEffectType.
    //   Wealth Flying Sword    — no effect (basic qi blade)
    //   Core Treasure Sword     — TELEPORT (displaces target on hit)
    //   Blood Slaughter Sword  — LIFESTEAL (heals attacker 30% of damage)
    public static final RegistryObject<dev.ergenverse.item.FlyingSwordItem> WEALTH_FLYING_SWORD =
            ITEMS.register("wealth_flying_sword", () -> new dev.ergenverse.item.FlyingSwordItem(8.0F,
                    dev.ergenverse.item.sword.SwordEffectType.NONE,
                    new Item.Properties().durability(500).rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<dev.ergenverse.item.FlyingSwordItem> CORE_TREASURE_SWORD =
            ITEMS.register("core_treasure_sword", () -> new dev.ergenverse.item.FlyingSwordItem(14.0F,
                    dev.ergenverse.item.sword.SwordEffectType.TELEPORT,
                    new Item.Properties().durability(1200).rarity(net.minecraft.world.item.Rarity.RARE)));
    public static final RegistryObject<dev.ergenverse.item.FlyingSwordItem> BLOOD_SLAUGHTER_SWORD =
            ITEMS.register("blood_slaughter_sword", () -> new dev.ergenverse.item.FlyingSwordItem(20.0F,
                    dev.ergenverse.item.sword.SwordEffectType.LIFESTEAL,
                    new Item.Properties().durability(2000).rarity(net.minecraft.world.item.Rarity.EPIC)));
    // CRON-COMPLETIONIST-57: Dark Green Flying Sword (墨绿飞剑) — POISON effect (Wither II 3s)
    // Canon: Wang Lin's fourth flying sword, corrupt energy, drains life force.
    public static final RegistryObject<dev.ergenverse.item.FlyingSwordItem> DARK_GREEN_FLYING_SWORD =
            ITEMS.register("dark_green_flying_sword", () -> new dev.ergenverse.item.FlyingSwordItem(17.0F,
                    dev.ergenverse.item.sword.SwordEffectType.POISON,
                    new Item.Properties().durability(1500).rarity(net.minecraft.world.item.Rarity.EPIC)));
    // CRON-COMPLETIONIST-57: God-Slaying Sword (诛仙剑) — RESTRICTION effect (armor-bypass magic damage)
    // Canon: one of the Seven Swords of Star Heaven, ignores defensive formations.
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


    // ── Spawn Eggs ────────────────────────────────────────────────────
    public static final RegistryObject<Item> SPIRIT_RABBIT_SPAWN_EGG = ITEMS.register("spirit_rabbit_spawn_egg",
            () -> new net.minecraft.world.item.SpawnEggItem(
                    dev.ergenverse.entity.EREntityTypes.SPIRIT_RABBIT.get(),
                    0xFFFFFF, 0xC0C0C0,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_WOLF_SPAWN_EGG = ITEMS.register("spirit_wolf_spawn_egg",
            () -> new net.minecraft.world.item.SpawnEggItem(
                    dev.ergenverse.entity.EREntityTypes.SPIRIT_WOLF.get(),
                    0x5A6E82, 0x00C8DC,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_DEER_SPAWN_EGG = ITEMS.register("spirit_deer_spawn_egg",
            () -> new net.minecraft.world.item.SpawnEggItem(
                    dev.ergenverse.entity.EREntityTypes.SPIRIT_DEER.get(),
                    0x8C643C, 0xFFC832,
                    new Item.Properties()));
    public static final RegistryObject<Item> SPIRIT_HAWK_SPAWN_EGG = ITEMS.register("spirit_hawk_spawn_egg",
            () -> new net.minecraft.world.item.SpawnEggItem(
                    dev.ergenverse.entity.EREntityTypes.SPIRIT_HAWK.get(),
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
                            output.accept(SPIRIT_STONE.get());
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
                            // Flying swords
                            output.accept(WEALTH_FLYING_SWORD.get());
                            output.accept(CORE_TREASURE_SWORD.get());
                            output.accept(BLOOD_SLAUGHTER_SWORD.get());
                            output.accept(DARK_GREEN_FLYING_SWORD.get());
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
                        })
                        .build());
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseItems: registered jade_slip utility item.");
    }
}
