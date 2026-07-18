package dev.ergenverse.screen;

import dev.ergenverse.block.entity.AlchemyFurnaceBlockEntity;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * ErgenverseMenus — DeferredRegister for all Ergenverse menu types.
 *
 * <p>Currently registers:
 * <ul>
 *   <li>{@code alchemy_furnace} — the {@link AlchemyFurnaceMenu} type,
 *       opened by right-clicking the Alchemy Furnace block. Uses the
 *       Forge {@link IForgeMenuType#create} pattern so the client-side
 *       factory can receive a {@link BlockPos} buffer and look up the
 *       block entity on the client world.</li>
 * </ul>
 *
 * <p>Wire via {@link #register(IEventBus)} from the {@link Ergenverse}
 * constructor. Menu types must be registered AFTER their owning block
 * entities (the client factory references the BE class).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ErgenverseMenus {

    private ErgenverseMenus() {}

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Ergenverse.MOD_ID);

    /**
     * The Alchemy Furnace menu type. Forge-style: the client factory reads a
     * {@link BlockPos} from the packet buffer and resolves the block entity
     * on the client world. The BE provides its own {@code ContainerData} for
     * the flame/progress synchronization.
     */
    public static final RegistryObject<MenuType<FormationPlatformMenu>> FORMATION_PLATFORM =
            MENUS.register("formation_platform", () ->
                    IForgeMenuType.create((windowId, inv, data) ->
                            new FormationPlatformMenu(windowId, inv, null, new SimpleContainerData(1))));

    public static final RegistryObject<MenuType<TalismanDeskMenu>> TALISMAN_DESK =
            MENUS.register("talisman_desk", () ->
                    IForgeMenuType.create((windowId, inv, data) ->
                            new TalismanDeskMenu(windowId, inv, null, new SimpleContainerData(1))));

    public static final RegistryObject<MenuType<AlchemyFurnaceMenu>> ALCHEMY_FURNACE =
            MENUS.register("alchemy_furnace", () ->
                    IForgeMenuType.create((containerId, playerInv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof AlchemyFurnaceBlockEntity furnace) {
                            return new AlchemyFurnaceMenu(containerId, playerInv,
                                    furnace, furnace.getData());
                        }
                        return null;
                    }));

    /** Wire the deferred register to the mod event bus. */
    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] ErgenverseMenus: registered alchemy_furnace menu type.");
    }
}
