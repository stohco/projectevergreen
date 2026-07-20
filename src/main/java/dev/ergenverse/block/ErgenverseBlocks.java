package dev.ergenverse.block;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

/**
 * ErgenverseBlocks — DeferredRegister for all custom blocks.
 *
 * <p>Registers:
 * <ul>
 *   <li>3 Realm-Sealing Grand Array anchor blocks (realm_sealing_flag,
 *       heaven_splitting_axe_pedestal, dao_binding_stone)</li>
 *   <li>14 canon spirit herb blocks (replacing vanilla stand-ins in worldgen)</li>
 * </ul>
 *
 * <p>Per the Prime Directive: these blocks exist objectively in the world.
 * A mortal sees "a strange glowing flower" — the PerceptionEngine handles
 * what the player UNDERSTANDS, not whether the block exists.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ErgenverseBlocks {

    private ErgenverseBlocks() {}

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Ergenverse.MOD_ID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Ergenverse.MOD_ID);

    public static final net.minecraftforge.registries.DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITIES =
            net.minecraftforge.registries.DeferredRegister.create(net.minecraft.core.registries.Registries.BLOCK_ENTITY_TYPE, Ergenverse.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Ergenverse.MOD_ID);

    /** Ergenverse Blocks creative tab. */
    public static RegistryObject<CreativeModeTab> ERGENVERSE_BLOCKS_TAB;

    // ── Workstation Blocks ──────────────────────────────────────────────

    /**
     * Alchemy Furnace — the first Minecraft crafting block in the Ergenverse.
     *
     * <p>A directional, horizontal-facing block. Right-clicking opens the
     * Alchemy Furnace menu (5 ingredient slots + 1 fuel + 1 output). The
     * block entity ticks on the server, consuming fuel and producing a
     * placeholder "basic pill" output (sugar, until a proper pill item is
     * registered).
     *
     * <p>Strength/sound matches vanilla furnace (3.5 hardness, 1500 explosion
     * resistance, metal sound, requires correct tool for drops).
     */
    public static final RegistryObject<Block> ALCHEMY_FURNACE = registerWorkstation(
            "alchemy_furnace",
            BlockBehaviour.Properties.of()
                    .strength(3.5f, 1500.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops(),
            AlchemyFurnaceBlock::new);

    // ── Anchor Blocks (Realm-Sealing Grand Array) ──────────────────────

    /** Realm Sealing Flag — primary anchor for the Realm-Sealing Grand Array. */
    public static final RegistryObject<Block> REALM_SEALING_FLAG = registerAnchor(
            "realm_sealing_flag",
            BlockBehaviour.Properties.of()
                    .strength(5.0f, 1200.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops());
    

    /** Heaven-Splitting Axe Pedestal — the seat of the array's spirit. */
    public static final RegistryObject<Block> HEAVEN_SPLITTING_AXE_PEDESTAL = registerAnchor(
            "heaven_splitting_axe_pedestal",
            BlockBehaviour.Properties.of()
                    .strength(8.0f, 2000.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    

    /** Dao Binding Stone — inscribed with the Seven-Colored Daoist's dao. */
    public static final RegistryObject<Block> DAO_BINDING_STONE = registerAnchor(
            "dao_binding_stone",
            BlockBehaviour.Properties.of()
                    .strength(6.0f, 1500.0f)
                    .sound(SoundType.STONE)
                    .requiresCorrectToolForDrops());
    

    // ── Spirit Herb Blocks ──────────────────────────────────────────────
    // These replace the vanilla stand-ins (lily_of_the_valley, sea_pickle,
    // glow_berries, cyan_terracotta, orange_tulip, brown_mushroom, red_mushroom)
    // used in configured_feature JSONs.

    /** Snow-Heart Herb — ice-aspect spirit herb from Snow Domain Country. */
    public static final RegistryObject<Block> SNOW_HEART_HERB = registerHerb(
            "snow_heart_herb", MobEffects.MOVEMENT_SLOWDOWN, 8);

    /** Soul Nourishing Lotus — water-aspect herb, nourishes soul power. */
    public static final RegistryObject<Block> SOUL_NOURISHING_LOTUS = registerHerb(
            "soul_nourishing_lotus", MobEffects.REGENERATION, 6);

    /** Fire Bloom Lotus — fire-aspect herb from Fire Demon Country. */
    public static final RegistryObject<Block> FIRE_BLOOM_LOTUS = registerHerb(
            "fire_bloom_lotus", MobEffects.FIRE_RESISTANCE, 6);

    /** Nine-Leaf Clover — rare lucky herb, canon-attested in Zhao Kingdom. */
    public static final RegistryObject<Block> NINE_LEAF_CLOVER = registerHerb(
            "nine_leaf_clover", MobEffects.LUCK, 100);

    /** Qi Gathering Grass — base meditation herb, found across all countries. */
    public static final RegistryObject<Block> QI_GATHERING_GRASS = registerHerb(
            "qi_gathering_grass", null, 00);

    /** Blood Forgetting Grass — memory-affecting herb from Sea of Devils. */
    public static final RegistryObject<Block> BLOOD_FORGETTING_GRASS = registerHerb(
            "blood_forgetting_grass", MobEffects.CONFUSION, 6);

    /** Void Nether Grass — void-aspect herb from border zones. */
    public static final RegistryObject<Block> VOID_NETHER_GRASS = registerHerb(
            "void_nether_grass", MobEffects.WEAKNESS, 6);

    /** Dao Trace Vine — vine that bears the imprint of a cultivation dao. */
    public static final RegistryObject<Block> DAO_TRACE_VINE = registerHerb(
            "dao_trace_vine", null, 00);

    /** Sword Edge Moss — moss growing on ancient sword-edge terrain. */
    public static final RegistryObject<Block> SWORD_EDGE_MOSS = registerHerb(
            "sword_edge_moss", MobEffects.DAMAGE_BOOST, 40);

    /** Five-Color Ginseng — multi-aspect ginseng, high-value herb. */
    public static final RegistryObject<Block> FIVE_COLOR_GINSENG = registerHerb(
            "five_color_ginseng", MobEffects.REGENERATION, 100);

    /** Foundation Root Vine — root vine used in foundation-establishing pills. */
    public static final RegistryObject<Block> FOUNDATION_ROOT_VINE = registerHerb(
            "foundation_root_vine", null, 00);

    /** Reincarnation Lily — reincarnation-aspect herb. */
    public static final RegistryObject<Block> REINCARNATION_LILY = registerHerb(
            "reincarnation_lily", MobEffects.DAMAGE_RESISTANCE, 80);

    /** Heart Devil Flower — induces heart-devil tribulation. */
    public static final RegistryObject<Block> HEART_DEVIL_FLOWER = registerHerb(
            "heart_devil_flower", MobEffects.BLINDNESS, 6);

    /** Demon Corpse Mushroom — grows on ancient battlefield remains. */
    public static final RegistryObject<Block> DEMON_CORPSE_MUSHROOM = registerHerb(
            "demon_corpse_mushroom", MobEffects.WITHER, 80);

    /** Vermilion Blood Ginseng — fire-blood ginseng from Vermilion Bird Country. */
    public static final RegistryObject<Block> VERMILION_BLOOD_GINSENG = registerHerb(
            "vermilion_blood_ginseng", MobEffects.ABSORPTION, 100);

    // ── Helpers ─────────────────────────────────────────────────────────

    private static RegistryObject<Block> registerAnchor(String name, BlockBehaviour.Properties props) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new Block(props));
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
        return block;
    }

    /**
     * Register a workstation block + its BlockItem. Uses a custom Block
     * supplier (e.g., {@link AlchemyFurnaceBlock::new}) so directional /
     * block-entity-bearing blocks can be instantiated with the properties.
     */
    private static RegistryObject<Block> registerWorkstation(String name,
                                                              BlockBehaviour.Properties props,
                                                              java.util.function.Function<BlockBehaviour.Properties, Block> factory) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> factory.apply(props));
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
        return block;
    }

    private static RegistryObject<Block> registerHerb(String name,
                                                      @javax.annotation.Nullable net.minecraft.world.effect.MobEffect stewEffect,
                                                      int effectDuration) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new FlowerBlock(
                stewEffect != null ? () -> stewEffect : null, effectDuration,
                BlockBehaviour.Properties.of()
                        .noCollission()
                        .instabreak()
                        .sound(SoundType.GRASS)
                        .offsetType(BlockBehaviour.OffsetType.XZ)));
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(),
                new Item.Properties()));
        return block;
    }

    /** Convenience: get the ResourceLocation for a registered block. */
    public static ResourceLocation getBlockId(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block);
    }


    // ── Spirit Ore Blocks ──────────────────────────────────────────────
    public static final RegistryObject<Block> SPIRIT_IRON_ORE = registerSimple("spirit_iron_ore", 3.0F, 3.0F);
    public static final RegistryObject<Block> COLD_IRON_ORE = registerSimple("cold_iron_ore", 3.0F, 3.0F);
    public static final RegistryObject<Block> SPIRIT_STONE_ORE = registerSimple("spirit_stone_ore", 3.0F, 3.0F);
    public static final RegistryObject<Block> THUNDER_IRON_ORE = registerSimple("thunder_iron_ore", 3.5F, 3.5F);
    public static final RegistryObject<Block> FIRE_CRYSTAL_ORE = registerSimple("fire_crystal_ore", 3.0F, 3.0F);
    public static final RegistryObject<Block> NETHER_IRON_ORE = registerSimple("nether_iron_ore", 4.0F, 4.0F);

    // ── Spirit Wood Blocks ─────────────────────────────────────────────
    public static final RegistryObject<Block> SPIRIT_WOOD_LOG = registerSimple("spirit_wood_log", 2.0F, 2.0F);
    public static final RegistryObject<Block> SPIRIT_WOOD_PLANKS = registerSimple("spirit_wood_planks", 2.0F, 2.0F);
    public static final RegistryObject<Block> SPIRIT_WOOD_LEAVES = registerSimple("spirit_wood_leaves", 0.2F, 0.2F);
    public static final RegistryObject<Block> ANCIENT_SPIRIT_LOG = registerSimple("ancient_spirit_log", 2.5F, 2.5F);
    public static final RegistryObject<Block> ANCIENT_SPIRIT_LEAVES = registerSimple("ancient_spirit_leaves", 0.2F, 0.2F);

    // ── Formation Blocks ───────────────────────────────────────────────
    public static final RegistryObject<Block> FORMATION_CORE_STONE = registerSimple("formation_core_stone", 3.5F, 3.5F);
    public static final RegistryObject<Block> FORMATION_FLAG_BASE = registerSimple("formation_flag_base", 2.0F, 2.0F);
    public static final RegistryObject<Block> RESTRICTION_STONE = registerSimple("restriction_stone", 4.0F, 4.0F);
    public static final RegistryObject<Block> SPIRIT_VEIN_STONE = registerSimple("spirit_vein_stone", 3.0F, 3.0F);

    // ── Terrain Blocks ─────────────────────────────────────────────────
    public static final RegistryObject<Block> SPIRIT_DIRT = registerSimple("spirit_dirt", 0.5F, 0.5F);
    public static final RegistryObject<Block> SPIRIT_GRASS = registerSimple("spirit_grass", 0.6F, 0.6F);
    public static final RegistryObject<Block> SPIRIT_STONE_BLOCK = registerSimple("spirit_stone", 1.5F, 6.0F);
    public static final RegistryObject<Block> JADE_STONE = registerSimple("jade_stone", 2.0F, 6.0F);
    public static final RegistryObject<Block> BLOOD_STONE = registerSimple("blood_stone", 2.0F, 6.0F);
    public static final RegistryObject<Block> SCORCHED_STONE = registerSimple("scorched_stone", 1.8F, 6.0F);
    public static final RegistryObject<Block> SPIRIT_SAND = registerSimple("spirit_sand", 0.5F, 0.5F);
    public static final RegistryObject<Block> SPIRIT_SANDSTONE = registerSimple("spirit_sandstone", 0.8F, 4.0F);


    private static RegistryObject<Block> registerSimple(String name, float hardness, float resistance) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new Block(
                BlockBehaviour.Properties.of()
                        .strength(hardness, resistance)
                        .sound(SoundType.STONE)));
        BLOCK_ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
        return block;
    }


    // ── Talisman Desk Block ────────────────────────────────────────────
    public static final RegistryObject<Block> TALISMAN_DESK = registerSimple("talisman_desk", 2.0F, 2.0F);

    // ── Formation Platform Block ──────────────────────────────────────
    public static final RegistryObject<Block> FORMATION_PLATFORM = registerSimple("formation_platform", 3.5F, 3.5F);


    // ── Artifact Forge Block ───────────────────────────────────────────
    public static final RegistryObject<Block> ARTIFACT_FORGE = registerSimple("artifact_forge", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.forge.ArtifactForgeBlockEntity>> ARTIFACT_FORGE_ENTITY =
            BLOCK_ENTITIES.register("artifact_forge", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.forge.ArtifactForgeBlockEntity::new, ARTIFACT_FORGE.get())
                    .build(null));


    // ── Pill Furnace Block (alchemy workstation) ──────────────────────
    public static final RegistryObject<Block> PILL_FURNACE = registerSimple("pill_furnace", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.alchemy.PillFurnaceBlockEntity>> PILL_FURNACE_ENTITY =
            BLOCK_ENTITIES.register("pill_furnace", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.alchemy.PillFurnaceBlockEntity::new, PILL_FURNACE.get())
                    .build(null));


    public static final RegistryObject<Block> TALISMAN_DESK_CRAFT = registerSimple("talisman_desk_craft", 2.0F, 2.0F);

    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.talisman.crafting.TalismanDeskBlockEntity>> TALISMAN_DESK_ENTITY =
            BLOCK_ENTITIES.register("talisman_desk_craft", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.talisman.crafting.TalismanDeskBlockEntity::new, TALISMAN_DESK_CRAFT.get())
                    .build(null));


    // ── RestrictionAltarBlock ──────────────────────────────────────────────────
    public static final RegistryObject<Block> RESTRICTION_ALTAR = registerSimple("restriction_altar", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.restriction.crafting.RestrictionAltarBlockEntity>> RESTRICTION_ALTAR_ENTITY =
            BLOCK_ENTITIES.register("restriction_altar", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.restriction.crafting.RestrictionAltarBlockEntity::new, RESTRICTION_ALTAR.get())
                    .build(null));

    // ── SoulRefiningCauldronBlock ──────────────────────────────────────────────────
    public static final RegistryObject<Block> SOUL_REFINING_CAULDRON = registerSimple("soul_refining_cauldron", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.soul.crafting.SoulRefiningCauldronBlockEntity>> SOUL_REFINING_CAULDRON_ENTITY =
            BLOCK_ENTITIES.register("soul_refining_cauldron", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.soul.crafting.SoulRefiningCauldronBlockEntity::new, SOUL_REFINING_CAULDRON.get())
                    .build(null));

    // ── RefiningPoolBlock ──────────────────────────────────────────────────
    public static final RegistryObject<Block> REFINING_POOL = registerSimple("refining_pool", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.body.crafting.RefiningPoolBlockEntity>> REFINING_POOL_ENTITY =
            BLOCK_ENTITIES.register("refining_pool", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.body.crafting.RefiningPoolBlockEntity::new, REFINING_POOL.get())
                    .build(null));

    // ── PuppetPlatformBlock ──────────────────────────────────────────────────
    public static final RegistryObject<Block> PUPPET_PLATFORM = registerSimple("puppet_platform", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.puppet.crafting.PuppetPlatformBlockEntity>> PUPPET_PLATFORM_ENTITY =
            BLOCK_ENTITIES.register("puppet_platform", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.puppet.crafting.PuppetPlatformBlockEntity::new, PUPPET_PLATFORM.get())
                    .build(null));

    // ── BeastPactAltarBlock ──────────────────────────────────────────────────
    public static final RegistryObject<Block> BEAST_PACT_ALTAR = registerSimple("beast_pact_altar", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.beast.crafting.BeastPactAltarBlockEntity>> BEAST_PACT_ALTAR_ENTITY =
            BLOCK_ENTITIES.register("beast_pact_altar", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.beast.crafting.BeastPactAltarBlockEntity::new, BEAST_PACT_ALTAR.get())
                    .build(null));
    public static final RegistryObject<Block> FORMATION_PLATFORM_CRAFT = registerSimple("formation_platform_craft", 3.5F, 3.5F);
    public static final net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<dev.ergenverse.formation.crafting.FormationPlatformBlockEntity>> FORMATION_PLATFORM_ENTITY =
            BLOCK_ENTITIES.register("formation_platform_craft", () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder
                    .of(dev.ergenverse.formation.crafting.FormationPlatformBlockEntity::new, FORMATION_PLATFORM_CRAFT.get())
                    .build(null));

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        BLOCK_ENTITIES.register(modEventBus);
        // NOTE: FormationPlatformBlockEntity.TYPE and TalismanDeskBlockEntity.TYPE
        // are already registered via BLOCK_ENTITIES above (FORMATION_PLATFORM_ENTITY
        // at line 326 with name "formation_platform_craft", and TALISMAN_DESK_ENTITY
        // at line 285 with name "talisman_desk_craft"). The previous inline
        // DeferredRegister.create() calls here were NEVER wired to the modEventBus,
        // so those RegistryObjects would never resolve. Removed to avoid confusion.
        BLOCK_ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        ERGENVERSE_BLOCKS_TAB = CREATIVE_TABS.register("ergenverse_blocks", () ->
                CreativeModeTab.builder()
                        .title(Component.literal("Ergenverse Blocks"))
                        .icon(() -> new ItemStack(REALM_SEALING_FLAG.get()))
                        .displayItems((params, output) -> {
                            // Workstation blocks first (explicit)
                            output.accept(ALCHEMY_FURNACE.get().asItem());
                            // Anchor blocks next (explicit)
                            output.accept(REALM_SEALING_FLAG.get().asItem());
                            output.accept(HEAVEN_SPLITTING_AXE_PEDESTAL.get().asItem());
                            output.accept(DAO_BINDING_STONE.get().asItem());
                            // Then all herb block items (skip explicit entries to avoid duplicates)
                            for (var entry : BLOCK_ITEMS.getEntries()) {
                                Item it = entry.get();
                                if (it == ALCHEMY_FURNACE.get().asItem()) continue;
                                if (it == REALM_SEALING_FLAG.get().asItem()) continue;
                                if (it == HEAVEN_SPLITTING_AXE_PEDESTAL.get().asItem()) continue;
                                if (it == DAO_BINDING_STONE.get().asItem()) continue;
                                output.accept(it);
                            }
                        })
                        .build());
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseBlocks: registered 1 workstation + 3 anchor blocks + 14 spirit herb blocks.");
    }
}