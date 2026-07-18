package dev.ergenverse.forge;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.network.ERNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Artifact Forge (炼器炉).
 *
 * <p>Holds a 5-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Blank item input (sword blank, armor blank, etc.)</li>
 *   <li>Slot 1: Material input (spirit iron ingot, beast bone, etc.)</li>
 *   <li>Slot 2: Catalyst input (special material, ink-stone, space-stone)</li>
 *   <li>Slot 3: Fuel (spirit stones — consumed per craft)</li>
 *   <li>Slot 4: Output (forged artifact)</li>
 * </ul>
 *
 * <p>Tracks: forge mode, craft timer (5-8s), current recipe.
 * Per DESIGN_UNIFIED_CRAFTING.md §3.2 §13: duration 5-8s per craft.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class ArtifactForgeBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 5;
    public static final int SLOT_BLANK = 0;
    public static final int SLOT_MATERIAL = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_FUEL = 3;
    public static final int SLOT_OUTPUT = 4;

    private static final int BASE_CRAFT_TICKS = 100; // 5s
    private static final int HIGH_TIER_CRAFT_TICKS = 160; // 8s

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int craftTimer;
    private int craftDuration;
    private boolean crafting;
    @Nullable private String currentRecipeId;
    @Nullable private String currentMode;
    private boolean incomplete;

    public ArtifactForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ErgenverseBlocks.ARTIFACT_FORGE_ENTITY.get(), pos, state);
    }

    // ─── NBT ──────────────────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("CraftTimer", craftTimer);
        tag.putInt("CraftDuration", craftDuration);
        tag.putBoolean("Incomplete", incomplete);
        tag.putBoolean("Crafting", crafting);
        tag.putString("CurrentRecipeId", currentRecipeId != null ? currentRecipeId : "");
        tag.putString("CurrentMode", currentMode != null ? currentMode : "");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) inventory.deserializeNBT(tag.getCompound("Inventory"));
        craftTimer = tag.getInt("CraftTimer");
        craftDuration = tag.getInt("CraftDuration");
        incomplete = tag.getBoolean("Incomplete");
        crafting = tag.getBoolean("Crafting");
        String rid = tag.getString("CurrentRecipeId");
        currentRecipeId = rid.isEmpty() ? null : rid;
        String mid = tag.getString("CurrentMode");
        currentMode = mid.isEmpty() ? null : mid;
    }

    // ─── Crafting ────────────────────────────────────────────────────────

    public void startCraft(String recipeId, String mode, int duration) {
        this.currentRecipeId = recipeId;
        this.currentMode = mode;
        this.craftDuration = duration;
        this.craftTimer = 0;
        this.crafting = true;
        this.incomplete = false;
        setChanged();
    }

    public void tickCraft(Level level) {
        if (!crafting || currentRecipeId == null) return;
        craftTimer++;

        if (craftTimer >= MIN_CRAFT_TICKS) {
            if (inventory.getStackInSlot(SLOT_FUEL).isEmpty()) {
                incomplete = true;
                produceIncomplete();
                resetCraft();
                syncProgress();
                return;
            }
        }

        if (craftTimer >= craftDuration) {
            rollResult(level);
            resetCraft();
        }
        setChanged();
        syncProgress();
    }

    private static final int MIN_CRAFT_TICKS = 20;

    private void rollResult(Level level) {
        double successRate = 0.60;
        // TODO: compute from divine sense + fire tier + realm + mode
        RandomSource rng = level != null ? level.getRandom() : RandomSource.create();

        ItemStack input1 = inventory.getStackInSlot(SLOT_BLANK);
        ItemStack input2 = inventory.getStackInSlot(SLOT_MATERIAL);
        ItemStack input3 = inventory.getStackInSlot(SLOT_CATALYST);
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);

        if (rng.nextDouble() < successRate) {
            ItemStack output = createOutput();
            if (!output.isEmpty()) inventory.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            inventory.setStackInSlot(SLOT_OUTPUT, createWasteItem());
        }

        if (!fuel.isEmpty()) { fuel.shrink(1); inventory.setStackInSlot(SLOT_FUEL, fuel); }
        if (!input1.isEmpty()) input1.shrink(1);
        if (!input2.isEmpty()) input2.shrink(1);
        if (!input3.isEmpty()) input3.shrink(1);
    }

    private ItemStack createOutput() {
        if (currentRecipeId == null) return ItemStack.EMPTY;
        Item outItem = ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, currentRecipeId));
        if (outItem == null) outItem = Items.IRON_SWORD;
        ItemStack out = new ItemStack(outItem, 1);
        CompoundTag tag = new CompoundTag();
        tag.putString("forge_recipe", currentRecipeId);
        tag.putString("forge_mode", currentMode != null ? currentMode : "");
        tag.putBoolean("incomplete", false);
        out.setTag(tag);
        return out;
    }

    private ItemStack createWasteItem() {
        ItemStack out = new ItemStack(Items.IRON_INGOT, 1);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("forge_waste", true);
        tag.putString("forge_recipe", currentRecipeId != null ? currentRecipeId : "unknown");
        out.setTag(tag);
        return out;
    }

    private void produceIncomplete() {
        ItemStack output = createOutput();
        if (!output.isEmpty()) {
            CompoundTag tag = output.getOrCreateTag();
            tag.putBoolean("incomplete", true);
            output.setTag(tag);
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
        incomplete = true;
    }

    private void resetCraft() {
        craftTimer = 0;
        crafting = false;
        incomplete = false;
        currentRecipeId = null;
        currentMode = null;
        craftDuration = BASE_CRAFT_TICKS;
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = craftDuration > 0
                ? Math.min(1.0f, (float) craftTimer / (float) craftDuration) : 0.0f;
        // TODO: re-enable when ArtifactForgeSyncS2CPacket is fixed
        // TODO: sync disabled
        // for (ServerPlayer viewer : getViewerPlayers()) {
        //     ERNetwork.getChannel().send(PacketDistributor.PLAYER.with(() -> viewer), pkt);
        // }
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof ArtifactForgeMenu menu
                    && menu.getForge() == this) {
                viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ───────────────────────────────────────────────────────

    public float getCraftProgress() {
        return craftDuration > 0 ? Math.min(1.0f, (float) craftTimer / (float) craftDuration) : 0.0f;
    }
    public boolean isCrafting() { return crafting; }
    public boolean isIncomplete() { return incomplete; }
    @Nullable public String getCurrentRecipeId() { return currentRecipeId; }
    @Nullable public String getCurrentMode() { return currentMode; }
    public ItemStackHandler getInventory() { return inventory; }

    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // ── Container contract ───────────────────────────────────────────

    @Override public int getContainerSize() { return CONTAINER_SIZE; }
    @Override public boolean isEmpty() { for (int i = 0; i < CONTAINER_SIZE; i++) if (!inventory.getStackInSlot(i).isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return inventory.getStackInSlot(slot); }
    @Override public ItemStack removeItem(int slot, int amount) { ItemStack s = inventory.getStackInSlot(slot); if (s.isEmpty()) return ItemStack.EMPTY; ItemStack e = s.split(amount); inventory.setStackInSlot(slot, s); setChanged(); return e; }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack s = inventory.getStackInSlot(slot); inventory.setStackInSlot(slot, ItemStack.EMPTY); return s; }
    @Override public void setItem(int slot, ItemStack stack) { inventory.setStackInSlot(slot, stack); setChanged(); }
    @Override public void clearContent() { for (int i = 0; i < CONTAINER_SIZE; i++) inventory.setStackInSlot(i, ItemStack.EMPTY); }
    @Override public boolean stillValid(Player player) { if (level == null) return false; if (level.getBlockEntity(worldPosition) != this) return false; return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0; }

    // ── MenuProvider contract ──────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ergenverse.artifact_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new ArtifactForgeMenu(containerId, playerInv, this);
    }
}