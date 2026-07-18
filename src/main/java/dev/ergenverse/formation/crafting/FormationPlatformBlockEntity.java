package dev.ergenverse.formation.crafting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
public class FormationPlatformBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(9);
    public FormationPlatformBlockEntity(BlockPos pos, BlockState state) { super(null, pos, state); }
    public ItemStackHandler getInventory() { return inventory; }
    public ContainerData getData() { return new SimpleContainerData(1); }
    @Override public Component getDisplayName() { return Component.literal("Formation Platform"); }
    @Override public AbstractContainerMenu createMenu(int id, Inventory inv, Player p) { return new FormationPlatformMenu(id, inv, this); }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); tag.put("inv", inventory.serializeNBT()); }
    @Override public void load(CompoundTag tag) { super.load(tag); if (tag.contains("inv")) inventory.deserializeNBT(tag.getCompound("inv")); }
}
