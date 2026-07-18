package dev.ergenverse.beast.crafting;

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
 * Server-side menu for the Beast Pact Altar (御兽台).
 *
 * <p>3 slots: Beast Core (0), Pact Medium (1), Output (2).
 * Plus 36 player inventory slots.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class BeastPactAltarMenu extends AbstractContainerMenu {

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<BeastPactAltarMenu>> TYPE =
            MENU_REGISTER.register("beast_pact_altar", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof BeastPactAltarBlockEntity altar) {
                            return new BeastPactAltarMenu(containerId, playerInv, altar);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Beast Pact Altar menu type registered.");
    }

    public static final int ALTAR_SLOT_COUNT = 3;
    public static final int SLOT_BEAST_CORE = 0;
    public static final int SLOT_PACT_MEDIUM = 1;
    public static final int SLOT_OUTPUT = 2;
    private static final int PLAYER_INV_START = ALTAR_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INV_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START + 9;

    private final BeastPactAltarBlockEntity altar;
    private final ContainerLevelAccess access;

    public BeastPactAltarMenu(int containerId, Inventory playerInv, BeastPactAltarBlockEntity altar) {
        super(TYPE.get(), containerId);
        this.altar = altar;
        this.access = ContainerLevelAccess.create(altar.getLevel(), altar.getBlockPos());

        ItemStackHandler inv = altar.getInventory();
        this.addSlot(new SlotItemHandler(inv, SLOT_BEAST_CORE, 62, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_PACT_MEDIUM, 98, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_OUTPUT, 134, 36) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public BeastPactAltarBlockEntity getAltar() { return altar; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();
            if (index < ALTAR_SLOT_COUNT) {
                if (!this.moveItemStackTo(original, PLAYER_INV_START, TOTAL_SLOTS, true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(original, SLOT_BEAST_CORE, SLOT_PACT_MEDIUM + 1, false)) return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((lvl, pos) ->
                lvl.getBlockState(pos).getBlock() instanceof BeastPactAltarBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }
}