package dev.ergenverse.block.entity;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * ErgenverseBlockEntities — DeferredRegister for all Ergenverse block entity
 * types.
 *
 * <p>Currently registers:
 * <ul>
 *   <li>{@code alchemy_furnace} — the {@link AlchemyFurnaceBlockEntity},
 *       attached to the {@code alchemy_furnace} block.</li>
 * </ul>
 *
 * <p>Wire via {@link #register(IEventBus)} from the {@link Ergenverse}
 * constructor. Block-entity types must be registered AFTER their owning
 * blocks (the {@link BlockEntityType.Builder#of} factory references the
 * block), so the call site in the mod constructor must come after
 * {@link ErgenverseBlocks#register}.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ErgenverseBlockEntities {

    private ErgenverseBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Ergenverse.MOD_ID);

    /**
     * The Alchemy Furnace block entity type. Bound to the Alchemy Furnace
     * block. The supplier is a BlockEntitySupplier (pos, state) -&gt; BE.
     */
    public static final RegistryObject<BlockEntityType<AlchemyFurnaceBlockEntity>> ALCHEMY_FURNACE =
            BLOCK_ENTITIES.register("alchemy_furnace", () ->
                    BlockEntityType.Builder.of(
                            AlchemyFurnaceBlockEntity::new,
                            ErgenverseBlocks.ALCHEMY_FURNACE.get())
                            .build(null));

    /** Wire the deferred register to the mod event bus. */
    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseBlockEntities: registered alchemy_furnace BE type.");
    }
}
