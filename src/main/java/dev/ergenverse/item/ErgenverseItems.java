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
    public static final RegistryObject<Item> QI_GATHERING_PILL = ITEMS.register("qi_gathering_pill",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> FOUNDATION_PILL = ITEMS.register("foundation_pill",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> PURIFICATION_PILL = ITEMS.register("purification_pill",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SOUL_MENDING_PILL = ITEMS.register("soul_mending_pill",
            () -> new Item(new Item.Properties().rarity(net.minecraft.world.item.Rarity.RARE)));

    // ── Blank Jade Slip (for inscription) ──────────────────────────────
    public static final RegistryObject<Item> JADE_SLIP_BLANK = ITEMS.register("jade_slip_blank",
            () -> new Item(new Item.Properties()));


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

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ERGENVERSE_ITEMS_TAB = CREATIVE_TABS.register("ergenverse_items", () ->
                CreativeModeTab.builder()
                        .title(Component.literal("Ergenverse Items"))
                        .icon(() -> new ItemStack(JADE_SLIP.get()))
                        .displayItems((params, output) -> {
                            output.accept(JADE_SLIP.get());
                        })
                        .build());
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseItems: registered jade_slip utility item.");
    }
}
