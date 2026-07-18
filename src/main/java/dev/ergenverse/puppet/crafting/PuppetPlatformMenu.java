package dev.ergenverse.puppet.crafting;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * Minimal PuppetPlatformMenu — 3 input slots + 1 output.
 */
public class PuppetPlatformMenu extends AbstractContainerMenu {
    private final ContainerData data;

    public PuppetPlatformMenu(int id, Inventory playerInv, IItemHandler handler, ContainerData data) {
        super(net.minecraft.world.inventory.MenuType.ANVIL, id);
        this.data = data;
        for (int i = 0; i < 3; i++) this.addSlot(new SlotItemHandler(handler, i, 30 + i * 22, 35));
        this.addSlot(new SlotItemHandler(handler, 3, 134, 35) { @Override public boolean mayPlace(ItemStack s) { return false; } });
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) this.addSlot(new Slot(playerInv, c + r * 9 + 9, 8 + c * 18, 84 + r * 18));
        for (int c = 0; c < 9; c++) this.addSlot(new Slot(playerInv, c, 8 + c * 18, 142));
        addDataSlots(data);
    }

    public PuppetPlatformMenu(int id, Inventory playerInv, PuppetPlatformBlockEntity entity) {
        this(id, playerInv, entity != null ? entity.getInventory() : new net.minecraftforge.items.ItemStackHandler(4), entity != null ? entity.getData() : new net.minecraft.world.inventory.SimpleContainerData(1));
    }

    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
