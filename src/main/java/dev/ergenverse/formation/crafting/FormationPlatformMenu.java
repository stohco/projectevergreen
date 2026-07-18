package dev.ergenverse.formation.crafting;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
public class FormationPlatformMenu extends AbstractContainerMenu {
    public FormationPlatformMenu(int id, Inventory inv, FormationPlatformBlockEntity entity) {
        super(net.minecraft.world.inventory.MenuType.CRAFTING, id);
        for (int r = 0; r < 3; r++) for (int c = 0; c < 3; c++)
            this.addSlot(new SlotItemHandler(entity.getInventory(), r*3+c, 30+c*18, 17+r*18));
        for (int r = 0; r < 3; r++) for (int c = 0; c < 9; c++) this.addSlot(new Slot(inv, c+r*9+9, 8+c*18, 84+r*18));
        for (int c = 0; c < 9; c++) this.addSlot(new Slot(inv, c, 8+c*18, 142));
    }
    @Override public ItemStack quickMoveStack(Player p, int i) { return ItemStack.EMPTY; }
    @Override public boolean stillValid(Player p) { return true; }
}
