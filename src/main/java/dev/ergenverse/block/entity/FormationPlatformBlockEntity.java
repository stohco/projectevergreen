package dev.ergenverse.block.entity;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.screen.FormationPlatformMenu;
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
 * FormationPlatformBlockEntity — 9 slots in a 3x3 grid for formation components.
 * Craft time: 120 ticks (6 seconds).
 */
public class FormationPlatformBlockEntity extends BlockEntity implements MenuProvider {

    public static final int TOTAL_SLOTS = 9;
    public static final int CRAFT_TIME = 120; // 6 seconds

    private final ItemStackHandler itemHandler = new ItemStackHandler(TOTAL_SLOTS);
    private LazyOptional<IItemHandler> handler = LazyOptional.of(() -> itemHandler);
    private int craftProgress = 0;

    protected final ContainerData data = new ContainerData() {
        @Override public int get(int index) { return index == 0 ? craftProgress : 0; }
        @Override public void set(int index, int value) { if (index == 0) craftProgress = value; }
        @Override public int getCount() { return 1; }
    };

    public static net.minecraftforge.registries.RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<FormationPlatformBlockEntity>> TYPE;

    public FormationPlatformBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FormationPlatformBlockEntity entity) {
        entity.tick();
    }

    private void tick() {
        boolean hasComponents = false;
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) { hasComponents = true; break; }
        }
        if (hasComponents) {
            craftProgress++;
            if (craftProgress >= CRAFT_TIME) {
                craftProgress = 0;
                // TODO: resolve formation from component properties
                // Consume components
                for (int i = 0; i < TOTAL_SLOTS; i++) {
                    itemHandler.getStackInSlot(i).shrink(1);
                }
                setChanged();
            }
        } else if (craftProgress > 0) {
            craftProgress = 0;
        }
    }

    @Override
    public Component getDisplayName() { return Component.literal("Formation Platform"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new FormationPlatformMenu(id, inv, this.itemHandler, this.data);
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
