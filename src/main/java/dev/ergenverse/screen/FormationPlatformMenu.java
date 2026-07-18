package dev.ergenverse.screen;

import dev.ergenverse.block.entity.FormationPlatformBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * FormationPlatformMenu — 9 slots in a 3x3 grid (like crafting table).
 */
public class FormationPlatformMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public FormationPlatformMenu(int id, Inventory playerInventory, IItemHandler handler, ContainerData data) {
        super(ErgenverseMenus.FORMATION_PLATFORM.get(), id);
        this.data = data;

        // 3x3 grid centered at x=30, y=17
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new SlotItemHandler(handler, row * 3 + col, 30 + col * 18, 17 + row * 18));
            }
        }

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();
        if (index >= 0 && index < 9) {
            if (!this.moveItemStackTo(original, 9, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(original, 0, 9, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public int getCraftProgress() { return data.get(0); }
    public int getProgressScaled() {
        return FormationPlatformBlockEntity.CRAFT_TIME > 0 ? getCraftProgress() * 24 / FormationPlatformBlockEntity.CRAFT_TIME : 0;
    }
}
