package dev.ergenverse.talisman.crafting;

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
 * Block entity for the Talisman Desk (\u7b26\u53f0) — shared workstation
 * for both talisman crafting (\u00a73.3) and jade slip inscription (\u00a73.8).
 *
 * <p>5-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Primary input (blank paper / blank jade slip)</li>
 *   <li>Slot 1: Secondary input (catalyst: glowstone, redstone, etc.)</li>
 *   <li>Slot 2: Spirit ink (required for all recipes)</li>
 *   <li>Slot 3: Extra material slot</li>
 *   <li>Slot 4: Output (inscribed talisman / jade slip)</li>
 * </ul>
 *
 * <p>Per \u00a713: talisman 2s (40t) / 3s (60t) / 5s (100t);
 * jade slip 3s (60t) / 4s (80t).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class TalismanDeskBlockEntity extends BlockEntity implements MenuProvider, Container {

    public static final int CONTAINER_SIZE = 5;
    public static final int SLOT_PRIMARY = 0;
    public static final int SLOT_SECONDARY = 1;
    public static final int SLOT_INK = 2;
    public static final int SLOT_EXTRA = 3;
    public static final int SLOT_OUTPUT = 4;

    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) { setChanged(); }
    };

    private int craftTimer;
    private int craftDuration;
    private boolean crafting;
    @Nullable private String currentRecipeId;
    @Nullable private String currentCategory;
    @Nullable private String currentMode;
    private boolean incomplete;

    public TalismanDeskBlockEntity(BlockPos pos, BlockState state) {
        super(dev.ergenverse.block.ErgenverseBlocks.TALISMAN_DESK_ENTITY.get(), pos, state);
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
        tag.putString("CurrentCategory", currentCategory != null ? currentCategory : "");
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
        String cat = tag.getString("CurrentCategory");
        currentCategory = cat.isEmpty() ? null : cat;
        String mode = tag.getString("CurrentMode");
        currentMode = mode.isEmpty() ? null : mode;
    }

    // ─── Crafting ────────────────────────────────────────────────────────

    public void startCraft(String recipeId, String category, String mode, int duration) {
        this.currentRecipeId = recipeId;
        this.currentCategory = category;
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

        // Check ink at the halfway point — if empty, produce incomplete
        if (craftTimer >= 20) {
            if (inventory.getStackInSlot(SLOT_INK).isEmpty()) {
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

    private void rollResult(Level level) {
        double successRate = 0.60;
        TalismanCraftingLogic.TalismanRecipe recipe =
                TalismanCraftingLogic.getRecipe(currentRecipeId);
        if (recipe != null) successRate = recipe.successRate();

        RandomSource rng = level != null ? level.getRandom() : RandomSource.create();

        ItemStack primary = inventory.getStackInSlot(SLOT_PRIMARY);
        ItemStack secondary = inventory.getStackInSlot(SLOT_SECONDARY);
        ItemStack ink = inventory.getStackInSlot(SLOT_INK);

        if (rng.nextDouble() < successRate) {
            ItemStack output = createOutput();
            if (!output.isEmpty()) inventory.setStackInSlot(SLOT_OUTPUT, output);
        } else {
            // Failed inscription — consumed paper, no output
            inventory.setStackInSlot(SLOT_OUTPUT, createWasteItem());
        }

        // Consume inputs
        if (!ink.isEmpty()) { ink.shrink(1); inventory.setStackInSlot(SLOT_INK, ink); }
        if (!primary.isEmpty()) primary.shrink(1);
        if (!secondary.isEmpty()) secondary.shrink(1);
    }

    private ItemStack createOutput() {
        if (currentRecipeId == null) return ItemStack.EMPTY;
        Item outItem = ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, currentRecipeId));
        if (outItem == null) {
            // Fallback: paper for talismans, emerald for jade slips
            if (currentMode != null && currentMode.equals(TalismanCraftingLogic.MODE_JADE_SLIP)) {
                outItem = Items.EMERALD;
            } else {
                outItem = Items.PAPER;
            }
        }
        ItemStack out = new ItemStack(outItem, 1);
        CompoundTag tag = new CompoundTag();
        tag.putString("talisman_recipe", currentRecipeId);
        tag.putString("talisman_category", currentCategory != null ? currentCategory : "");
        tag.putString("talisman_mode", currentMode != null ? currentMode : "");
        tag.putBoolean("incomplete", false);
        out.setTag(tag);
        return out;
    }

    private ItemStack createWasteItem() {
        ItemStack out = new ItemStack(Items.PAPER, 1);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("talisman_waste", true);
        tag.putString("talisman_recipe", currentRecipeId != null ? currentRecipeId : "unknown");
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
        currentCategory = null;
        currentMode = null;
        craftDuration = 40;
    }

    private void syncProgress() {
        if (level == null || level.isClientSide()) return;
        float progress = craftDuration > 0
                ? Math.min(1.0f, (float) craftTimer / (float) craftDuration) : 0.0f;
        // TODO: sync disabled
    }

    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp
                    && sp.containerMenu instanceof TalismanDeskMenu menu
                    && menu.getDesk() == this) {
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
    @Nullable public String getCurrentCategory() { return currentCategory; }
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
        return Component.translatable("container.ergenverse.talisman_desk");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new TalismanDeskMenu(containerId, playerInv, this);
    }
}