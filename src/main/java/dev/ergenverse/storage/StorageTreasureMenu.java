package dev.ergenverse.storage;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleContainer;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Menu for storage treasures (\u50a8\u7269\u6cd5\u5b9d). Displays extra
 * inventory slots stored in the item's NBT, plus the player's own inventory.
 *
 * <p>Uses a single shared MenuType since the slot count is dynamic.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class StorageTreasureMenu extends AbstractContainerMenu {

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<StorageTreasureMenu>> TYPE =
            MENU_REGISTER.register("storage_treasure", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        int slotCount = data.readVarInt();
                        int handId = data.readVarInt();
                        // Find the storage treasure item in the player's hand
                        var hand = handId == 0
                                ? net.minecraft.world.InteractionHand.MAIN_HAND
                                : net.minecraft.world.InteractionHand.OFF_HAND;
                        ItemStack stack = playerInv.player.getItemInHand(hand);
                        if (stack.getItem() instanceof StorageTreasureItem storageItem) {
                            SimpleContainer inv = StorageTreasureItem.getInventory(stack, slotCount);
                            return new StorageTreasureMenu(containerId, playerInv, inv, slotCount, stack);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Storage Treasure menu type registered.");
    }

    private final SimpleContainer storageContainer;
    private final int storageSlotCount;
    private final ItemStack sourceItem;
    private final int playerInvStart;

    public StorageTreasureMenu(int containerId, Inventory playerInv,
                                SimpleContainer storageContainer, int slotCount,
                                ItemStack sourceItem) {
        super(TYPE.get(), containerId);
        this.storageContainer = storageContainer;
        this.storageSlotCount = slotCount;
        this.sourceItem = sourceItem;
        this.playerInvStart = slotCount;

        // Storage slots — layout depends on slot count
        int rows = (slotCount + 8) / 9;
        for (int i = 0; i < slotCount; i++) {
            int row = i / 9;
            int col = i % 9;
            int x = 8 + col * 18;
            int y = 18 + row * 18;
            this.addSlot(new Slot(storageContainer, i, x, y));
        }

        // Player inventory
        int playerY = 18 + rows * 18 + 10;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9,
                        8 + col * 18, playerY + row * 18));
            }
        }
        // Player hotbar
        int hotbarY = playerY + 58;
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, hotbarY));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();
            int totalSlots = playerInvStart + 36;

            if (index < playerInvStart) {
                // Storage → player inv
                if (!this.moveItemStackTo(original, playerInvStart, totalSlots, true))
                    return ItemStack.EMPTY;
            } else {
                // Player inv → storage
                if (!this.moveItemStackTo(original, 0, playerInvStart, false))
                    return ItemStack.EMPTY;
            }
            if (original.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        // Valid as long as the player has the storage treasure
        return true;
    }

    /**
     * Called when the container is closed to save the inventory back
     * to the item's NBT.
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        // Save inventory back to the item
        if (!player.level().isClientSide() && sourceItem.getItem() instanceof StorageTreasureItem) {
            StorageTreasureItem.saveInventory(sourceItem, storageContainer);
        }
    }

    public int getStorageSlotCount() { return storageSlotCount; }

    /** Get the Y offset for the player inventory section. */
    public int getPlayerInvYOffset() {
        int rows = (storageSlotCount + 8) / 9;
        return 18 + rows * 18 + 10;
    }
}