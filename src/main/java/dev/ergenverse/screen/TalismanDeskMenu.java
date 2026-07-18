package dev.ergenverse.screen;

import dev.ergenverse.block.entity.TalismanDeskBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * TalismanDeskMenu — 4 slots: paper, ink, intent, output.
 * Layout: [Paper] [Ink] [Intent] → [Output]
 */
public class TalismanDeskMenu extends AbstractContainerMenu {

    private final ContainerData data;

    public TalismanDeskMenu(int id, Inventory playerInventory, IItemHandler handler, ContainerData data) {
        super(ErgenverseMenus.TALISMAN_DESK.get(), id);
        this.data = data;

        // Input slots: paper (30,20), ink (52,20), intent (74,20)
        this.addSlot(new SlotItemHandler(handler, TalismanDeskBlockEntity.SLOT_PAPER, 30, 35));
        this.addSlot(new SlotItemHandler(handler, TalismanDeskBlockEntity.SLOT_INK, 52, 35));
        this.addSlot(new SlotItemHandler(handler, TalismanDeskBlockEntity.SLOT_INTENT, 74, 35));

        // Output slot
        this.addSlot(new SlotItemHandler(handler, TalismanDeskBlockEntity.SLOT_OUTPUT, 134, 35) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

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
        if (index >= 0 && index <= 3) {
            if (!this.moveItemStackTo(original, 4, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!this.moveItemStackTo(original, 0, 3, false)) return ItemStack.EMPTY;
        }
        if (original.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) { return true; }

    public int getCraftProgress() { return data.get(0); }
    public int getProgressScaled() {
        int progress = getCraftProgress();
        return TalismanDeskBlockEntity.CRAFT_TIME > 0 ? progress * 24 / TalismanDeskBlockEntity.CRAFT_TIME : 0;
    }
}
