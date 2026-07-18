package dev.ergenverse.block.entity;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.screen.TalismanDeskMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TalismanDeskBlockEntity — 4 slots: paper, ink, intent, output.
 * Craft time: 40 ticks (2 seconds).
 */
public class TalismanDeskBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_PAPER = 0;
    public static final int SLOT_INK = 1;
    public static final int SLOT_INTENT = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int TOTAL_SLOTS = 4;
    public static final int CRAFT_TIME = 40; // 2 seconds

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS);
    private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private int craftProgress = 0;

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? craftProgress : 0; }
        @Override public void set(int index, int value) { if (index == 0) craftProgress = value; }
        @Override public int getCount() { return 1; }
    };

    public static net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<TalismanDeskBlockEntity>> TYPE;

    public TalismanDeskBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TalismanDeskBlockEntity entity) {
        entity.tick();
    }

    private void tick() {
        boolean hasInputs = !itemHandler.getStackInSlot(SLOT_PAPER).isEmpty()
                && !itemHandler.getStackInSlot(SLOT_INK).isEmpty()
                && itemHandler.getStackInSlot(SLOT_OUTPUT).isEmpty();

        if (hasInputs) {
            craftProgress++;
            if (craftProgress >= CRAFT_TIME) {
                craftProgress = 0;
                performCraft();
                setChanged();
            }
        } else if (craftProgress > 0) {
            craftProgress = 0;
        }
    }

    private void performCraft() {
        // TODO: property-based talisman resolution using MaterialProperties
        // For now: consume paper + ink, produce a basic talisman
        itemHandler.getStackInSlot(SLOT_PAPER).shrink(1);
        itemHandler.getStackInSlot(SLOT_INK).shrink(1);
        if (!itemHandler.getStackInSlot(SLOT_INTENT).isEmpty()) {
            itemHandler.getStackInSlot(SLOT_INTENT).shrink(1);
        }
        // Output: basic talisman (placeholder)
        itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(net.minecraft.world.item.Items.PAPER));
    }

    @Override
    public Component getDisplayName() { return Component.literal("Talisman Desk"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new TalismanDeskMenu(id, inv, this.itemHandler, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("craftProgress", craftProgress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("inventory")) itemHandler.deserializeNBT(tag.getCompound("inventory"));
        craftProgress = tag.getInt("craftProgress");
    }

    @Override
    public void invalidateCaps() { super.invalidateCaps(); handler.invalidate(); }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull net.minecraftforge.common.capabilities.Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return handler.cast();
        return super.getCapability(cap, side);
    }
}
