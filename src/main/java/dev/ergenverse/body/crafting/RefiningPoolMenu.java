package dev.ergenverse.body.crafting;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side menu for the Refining Pool (炼体池).
 *
 * <p>2 pool slots: Medium (0), Fuel (1).
 * Plus 36 player inventory slots.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class RefiningPoolMenu extends AbstractContainerMenu {

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<RefiningPoolMenu>> TYPE =
            MENU_REGISTER.register("refining_pool", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof RefiningPoolBlockEntity pool) {
                            return new RefiningPoolMenu(containerId, playerInv, pool);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Refining Pool menu type registered.");
    }

    public static final int POOL_SLOT_COUNT = 2;
    public static final int SLOT_MEDIUM = 0;
    public static final int SLOT_FUEL = 1;
    private static final int PLAYER_INV_START = POOL_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INV_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START + 9;

    private final RefiningPoolBlockEntity pool;
    private final ContainerLevelAccess access;

    public RefiningPoolMenu(int containerId, Inventory playerInv, RefiningPoolBlockEntity pool) {
        super(TYPE.get(), containerId);
        this.pool = pool;
        this.access = ContainerLevelAccess.create(pool.getLevel(), pool.getBlockPos());

        ItemStackHandler inv = pool.getInventory();
        this.addSlot(new SlotItemHandler(inv, SLOT_MEDIUM, 80, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_FUEL, 116, 36));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public RefiningPoolBlockEntity getPool() { return pool; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();
            if (index < POOL_SLOT_COUNT) {
                if (!this.moveItemStackTo(original, PLAYER_INV_START, TOTAL_SLOTS, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(original, SLOT_MEDIUM, SLOT_FUEL + 1, false)) return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((lvl, pos) ->
                lvl.getBlockState(pos).getBlock() instanceof RefiningPoolBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }
}