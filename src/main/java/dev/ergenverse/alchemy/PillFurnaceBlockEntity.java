package dev.ergenverse.alchemy;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
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
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Pill Furnace (丹炉).
 *
 * <p>Holds a 4-slot inventory via Forge's {@link ItemStackHandler}:
 * <ul>
 *   <li>Slot 0: Primary herb input (spirit herb powder, etc.)</li>
 *   <li>Slot 1: Secondary input (catalyst, beast core)</li>
 *   <li>Slot 2: Fuel (spirit stone — consumed per craft)</li>
 *   <li>Slot 3: Output (pill or waste pill)</li>
 * </ul>
 *
 * <p>The entity tracks: craft timer (4-6s), current recipe, and
 * incomplete-pill flag (crafting interrupted → weaker pill output).
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §3.1 §13:
 * Duration = 4s standard, 6s high-tier. No AFK, no real-time gates.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PillFurnaceBlockEntity extends BlockEntity implements MenuProvider, Container {

    /** Number of crafting slots (input x 2 + fuel + output). */
    public static final int CONTAINER_SIZE = 4;

    public static final int SLOT_INPUT_PRIMARY = 0;
    public static final int SLOT_INPUT_CATALYST = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;

    /** Standard craft duration in ticks (4 seconds = 80 ticks). */
    private static final int BASE_CRAFT_TICKS = 80;

    /** Minimum ticks before output can be taken. */
    private static final int MIN_CRAFT_TICKS = 20;

    /** The internal inventory — Forge's standard ItemStackHandler. */
    private final ItemStackHandler inventory = new ItemStackHandler(CONTAINER_SIZE) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    /** Crafting timer. When this reaches craftDuration, output is produced. */
    private int craftTimer;

    /** Total ticks needed for the current recipe. */
    private int craftDuration;

    /** Whether the craft produced an incomplete pill (interrupted). */
    private boolean incompletePill;

    /** Whether a craft is currently in progress. */
    private boolean crafting;

    /** The ID of the recipe being crafted (string key, e.g. "qi_gathering_pill"). */
    @Nullable
    private String currentRecipeId;

    public PillFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ErgenverseBlocks.PILL_FURNACE_ENTITY.get(), pos, state);
    }

    // ─── NBT Persistence ────────────────────────────────────────────

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("CraftTimer", craftTimer);
        tag.putInt("CraftDuration", craftDuration);
        tag.putBoolean("IncompletePill", incompletePill);
        tag.putBoolean("Crafting", crafting);
        tag.putString("CurrentRecipeId", currentRecipeId != null ? currentRecipeId : "");
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        craftTimer = tag.getInt("CraftTimer");
        craftDuration = tag.getInt("CraftDuration");
        incompletePill = tag.getBoolean("IncompletePill");
        crafting = tag.getBoolean("Crafting");
        String recipeId = tag.getString("CurrentRecipeId");
        currentRecipeId = recipeId.isEmpty() ? null : recipeId;
    }

    // ─── Crafting Logic ───────────────────────────────────────────

    /**
     * Start crafting a pill. Called from the server-side menu when the
     * player confirms.
     */
    public void startCraft(String recipeId, int duration) {
        this.currentRecipeId = recipeId;
        this.craftDuration = duration;
        this.craftTimer = 0;
        this.crafting = true;
        this.incompletePill = false;
        setChanged();
    }

    /**
     * Tick the crafting process. Called every server tick from the block
     * ticker. When the timer reaches the duration, produce output (or waste
     * pill if the craft fails).
     */
    public void tickCraft(Level level) {
        if (!crafting || currentRecipeId == null) return;

        craftTimer++;

        // Cancel if fuel was consumed (slot 2 empty = no fuel).
        // This handles the "furnace runs out of fuel" case from §3.1
        // (incomplete pill — canon: Wang Lin broke Core Formation on an
        // incomplete Distant Heaven Pill)
        if (craftTimer >= MIN_CRAFT_TICKS) {
            ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);
            if (fuel.isEmpty()) {
                incompletePill = true;
                produceIncompletePill();
                resetCraft();
                syncProgressToViewers();
                return;
            }
        }

        if (craftTimer >= craftDuration) {
            rollCraftResult();
            resetCraft();
        }

        setChanged();
        syncProgressToViewers();
    }

    /**
     * Roll the craft result. Success = produce the recipe's pill item.
     * Failure = produce a waste pill (废丹).
     */
    private void rollCraftResult() {
        double successRate = 0.70;

        ItemStack input1 = inventory.getStackInSlot(SLOT_INPUT_PRIMARY);
        ItemStack input2 = inventory.getStackInSlot(SLOT_INPUT_CATALYST);
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);

        RandomSource rng = level != null ? level.getRandom() : RandomSource.create();

        if (rng.nextDouble() < successRate) {
            ItemStack output = createPillItem(currentRecipeId);
            if (!output.isEmpty()) {
                inventory.setStackInSlot(SLOT_OUTPUT, output);
            }
        } else {
            inventory.setStackInSlot(SLOT_OUTPUT, createWastePill());
        }

        // Consume one spirit stone per craft (fuel)
        if (!fuel.isEmpty()) {
            fuel.shrink(1);
            inventory.setStackInSlot(SLOT_FUEL, fuel);
        }
        // Consume partial inputs (1 of each)
        if (!input1.isEmpty()) input1.shrink(1);
        if (!input2.isEmpty()) input2.shrink(1);
    }

    /**
     * Produce the output pill item for the given recipe. Returns empty
     * if the recipe ID is unrecognized.
     */
    private ItemStack createPillItem(String recipeId) {
        if (recipeId == null) return ItemStack.EMPTY;
        Item pillItem = getPillItemFromId(recipeId);
        if (pillItem == null) pillItem = getPillItemFromId("qi_gathering_pill");
        if (pillItem == null) return ItemStack.EMPTY;
        ItemStack output = new ItemStack(pillItem, 1);
        // Tag with recipe ID and incomplete status
        CompoundTag tag = new CompoundTag();
        tag.putString("pill_recipe", recipeId);
        tag.putBoolean("incomplete", false);
        output.setTag(tag);
        return output;
    }

    /**
     * Create a waste pill (废丹). These are items — not destroyed.
     * Canon: Wang Lin's entire early arc is built on waste pills.
     * They can be re-refined, Bead-purified, or fed to beasts.
     */
    private ItemStack createWastePill() {
        Item wasteItem = getPillItemFromId("waste_pill");
        if (wasteItem == null) {
            // Fallback: use a mundane item as placeholder
            wasteItem = Items.FERMENTED_SPIDER_EYE;
        }
        ItemStack output = new ItemStack(wasteItem, 1);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("waste_pill", true);
        tag.putString("pill_recipe", currentRecipeId != null ? currentRecipeId : "unknown");
        output.setTag(tag);
        return output;
    }

    /**
     * Produce an incomplete pill (crafting interrupted).
     * Weaker but usable. Canon: Wang Lin broke Core Formation on an
     * incomplete Distant Heaven Pill.
     */
    private void produceIncompletePill() {
        ItemStack output = createPillItem(currentRecipeId);
        if (!output.isEmpty()) {
            CompoundTag tag = output.getOrCreateTag();
            tag.putBoolean("incomplete", true);
            output.setTag(tag);
            inventory.setStackInSlot(SLOT_OUTPUT, output);
        }
        incompletePill = true;
    }

    /** Reset crafting state after output is produced. */
    private void resetCraft() {
        this.craftTimer = 0;
        this.crafting = false;
        this.incompletePill = false;
        this.currentRecipeId = null;
        this.craftDuration = BASE_CRAFT_TICKS;
    }

    /**
     * Push a progress sync packet to any player currently viewing this
     * furnace's menu. Called each craft tick.
     */
    private void syncProgressToViewers() {
        if (level == null || level.isClientSide()) return;
        float progress = craftDuration > 0
                ? Math.min(1.0f, (float) craftTimer / (float) craftDuration)
                : 0.0f;
        // TODO: sync packet disabled
    }

    /** Get the list of players viewing this BE's menu (server-side only). */
    private java.util.List<ServerPlayer> getViewerPlayers() {
        java.util.List<ServerPlayer> viewers = new java.util.ArrayList<>();
        if (level == null || level.isClientSide()) return viewers;
        for (Player p : level.players()) {
            if (p instanceof ServerPlayer sp && sp.containerMenu instanceof PillFurnaceMenu menu) {
                if (menu.getFurnace() == this) viewers.add(sp);
            }
        }
        return viewers;
    }

    // ─── Accessors ─────────────────────────────────────────────

    /** Get the current crafting progress (0.0 to 1.0). */
    public float getCraftProgress() {
        if (craftDuration <= 0) return 0.0f;
        return Math.min(1.0f, (float) craftTimer / (float) craftDuration);
    }

    /** Whether a craft is currently in progress. */
    public boolean isCrafting() { return crafting; }

    public boolean isIncompletePill() { return incompletePill; }

    @Nullable
    public String getCurrentRecipeId() { return currentRecipeId; }

    public int getCraftTimer() { return craftTimer; }
    public int getCraftDuration() { return craftDuration; }

    /** Exposed for the menu to read inputs when validating crafts. */
    public ItemStackHandler getInventory() { return inventory; }

    /**
     * Drop all inventory contents into the world (used when the block is
     * broken). Called from PillFurnaceBlock.onRemove.
     */
    public void dropContents(Level level, BlockPos pos) {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    // ─── Container contract (for slot access from the menu) ────

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = inventory.getStackInSlot(slot);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        ItemStack extracted = stack.split(amount);
        inventory.setStackInSlot(slot, stack);
        setChanged();
        return extracted;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = inventory.getStackInSlot(slot);
        inventory.setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.setStackInSlot(slot, stack);
        setChanged();
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null) return false;
        if (level.getBlockEntity(worldPosition) != this) return false;
        return player.distanceToSqr(
                worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    // ─── MenuProvider contract (for opening the GUI) ──────────

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ergenverse.pill_furnace");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInv, Player player) {
        return new PillFurnaceMenu(containerId, playerInv, this);
    }

    // ─── Helpers ──────────────────────────────────────────────

    @Nullable
    private static Item getPillItemFromId(String id) {
        return net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, id));
    }
}