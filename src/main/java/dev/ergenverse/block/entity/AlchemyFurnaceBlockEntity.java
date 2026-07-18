package dev.ergenverse.block.entity;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.screen.AlchemyFurnaceMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * AlchemyFurnaceBlockEntity — the server-side tick logic for the Alchemy Furnace.
 *
 * <h2>Inventory</h2>
 * <ul>
 *   <li>Slots 0..4 — five ingredient slots (drawn as a 3-2 grid in the menu)</li>
 *   <li>Slot 5     — fuel slot</li>
 *   <li>Slot 6     — output slot</li>
 * </ul>
 *
 * <h2>Timings</h2>
 * <ul>
 *   <li>{@link #CRAFT_TIME} = 80 ticks (4 seconds at 20 TPS) — one craft cycle</li>
 *   <li>{@link #FUEL_BURN_TIME} = 200 ticks (10 seconds) — one fuel item's burn</li>
 * </ul>
 *
 * <h2>Tick Loop</h2>
 * <p>Every server tick ({@link #serverTick()}):
 * <ol>
 *   <li>Decrement {@link #fuelRemaining} if burning.</li>
 *   <li>If not burning and the fuel slot has fuel, ignite it (consume one fuel
 *       item, set {@link #fuelRemaining} = {@link #FUEL_BURN_TIME}).</li>
 *   <li>If burning AND {@link #canCraft()} returns true, advance
 *       {@link #craftProgress}. When it reaches {@link #CRAFT_TIME}, consume
 *       ingredients and emit the {@link #craftItem()} result into the output.</li>
 *   <li>If not burning or {@code canCraft()} returns false, reset progress to 0.</li>
 * </ol>
 *
 * <h2>Craft Resolution (placeholder)</h2>
 * <p>The current {@link #canCraft()} / {@link #craftItem()} logic is a
 * <b>property-based placeholder</b>: when at least one ingredient slot is
 * non-empty AND the output slot is empty or matches the placeholder "basic
 * pill" item (sugar, for now), the furnace crafts. A future iteration will
 * resolve against a recipe registry keyed on ingredient-item properties
 * (Element, Aspect, Tier, etc.).
 *
 * <h2>Synchronization</h2>
 * <p>{@link #data} is a {@link ContainerData} of size 3:
 * <ul>
 *   <li>index 0 — {@link #craftProgress}</li>
 *   <li>index 1 — {@link #fuelRemaining}</li>
 *   <li>index 2 — {@link #fuelMax}</li>
 * </ul>
 * The menu reads it via {@link #getData()} to draw the flame and progress arrow.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class AlchemyFurnaceBlockEntity extends BaseContainerBlockEntity {

    // ── Slot indices (exported so Menu/Screen can reference them) ───────
    public static final int SLOT_INGREDIENT_1 = 0;
    public static final int SLOT_INGREDIENT_2 = 1;
    public static final int SLOT_INGREDIENT_3 = 2;
    public static final int SLOT_INGREDIENT_4 = 3;
    public static final int SLOT_INGREDIENT_5 = 4;
    public static final int SLOT_FUEL = 5;
    public static final int SLOT_OUTPUT = 6;

    /** Total inventory size: 5 ingredients + 1 fuel + 1 output. */
    public static final int NUM_SLOTS = 7;

    /** One craft cycle takes 80 ticks (4 seconds at 20 TPS). */
    public static final int CRAFT_TIME = 80;

    /** One fuel item burns for 200 ticks (10 seconds). */
    public static final int FUEL_BURN_TIME = 200;

    // ── State ───────────────────────────────────────────────────────────
    /** Inventory contents, in slot-index order. */
    protected NonNullList<ItemStack> items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);

    /** Current craft progress in ticks (0..CRAFT_TIME). */
    int craftProgress = 0;

    /** Remaining fuel ticks (decrements each tick while burning). */
    int fuelRemaining = 0;

    /** The maximum fuel ticks for the currently-burning fuel item. Used by
     *  the screen to draw the flame's height proportionally. */
    int fuelMax = 0;

    /** Synchronized data exposed to the menu. */
    private net.minecraft.world.entity.player.Player lastPlayer = null;
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> AlchemyFurnaceBlockEntity.this.craftProgress;
                case 1 -> AlchemyFurnaceBlockEntity.this.fuelRemaining;
                case 2 -> AlchemyFurnaceBlockEntity.this.fuelMax;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> AlchemyFurnaceBlockEntity.this.craftProgress = value;
                case 1 -> AlchemyFurnaceBlockEntity.this.fuelRemaining = value;
                case 2 -> AlchemyFurnaceBlockEntity.this.fuelMax = value;
                default -> { /* no-op */ }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    // ── Construction ────────────────────────────────────────────────────
    public AlchemyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ErgenverseBlockEntities.ALCHEMY_FURNACE.get(), pos, state);
    }

    // ── MenuProvider contract ───────────────────────────────────────────
    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.ergenverse.alchemy_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInv) {
        return new AlchemyFurnaceMenu(containerId, playerInv, this, this.data);
    }

    /** Expose the sync data to the menu (constructor uses it directly). */
    public ContainerData getData() {
        return this.data;
    }

    // ── Container contract ──────────────────────────────────────────────
    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(this.items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return true; // simplified — TODO: proper distance check
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    // ── NBT save/load ───────────────────────────────────────────────────
    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.items = NonNullList.withSize(NUM_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.craftProgress = tag.getInt("CraftProgress");
        this.fuelRemaining = tag.getInt("FuelRemaining");
        this.fuelMax = tag.getInt("FuelMax");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("CraftProgress", this.craftProgress);
        tag.putInt("FuelRemaining", this.fuelRemaining);
        tag.putInt("FuelMax", this.fuelMax);
    }

    // ── Tick logic ──────────────────────────────────────────────────────

    /**
     * Per-server-tick update. Drives the fuel and craft loops.
     *
     * <p>Called from {@link dev.ergenverse.block.AlchemyFurnaceBlock#getTicker}
     * — only runs server-side.
     */
    public void serverTick() {
        boolean wasBurning = isBurning();
        boolean changed = false;

        // 1. Decrement fuel if currently burning.
        if (this.fuelRemaining > 0) {
            this.fuelRemaining--;
            changed = true;
        }

        // 2. If not burning and we have fuel, ignite one item.
        if (this.fuelRemaining <= 0 && canIgnite()) {
            ItemStack fuelStack = this.items.get(SLOT_FUEL);
            this.fuelMax = FUEL_BURN_TIME;
            this.fuelRemaining = FUEL_BURN_TIME;
            Item remaining = fuelStack.getCraftingRemainingItem().getItem();
            fuelStack.shrink(1);
            if (fuelStack.isEmpty()) {
                this.items.set(SLOT_FUEL, new ItemStack(remaining));
            }
            changed = true;
        } else if (this.fuelRemaining <= 0 && !canIgnite()) {
            // Ran out of fuel and no fuel to consume — zero out max for the
            // flame icon so it disappears.
            if (this.fuelMax != 0) {
                this.fuelMax = 0;
                changed = true;
            }
        }

        // 3. Craft step: only advance if burning AND a recipe is possible.
        if (this.fuelRemaining > 0 && canCraft()) {
            this.craftProgress++;
            if (this.craftProgress >= CRAFT_TIME) {
                this.craftProgress = 0;
                craftItem();
                changed = true;
            }
        } else if (this.craftProgress > 0) {
            // Reset progress if we cannot craft (no recipe / no fuel).
            this.craftProgress = 0;
            changed = true;
        }

        if (wasBurning != isBurning() || changed) {
            this.setChanged();
            // Push state to clients via the chunk.
            if (this.level != null && !this.level.isClientSide()) {
                BlockState st = this.level.getBlockState(this.worldPosition);
                this.level.sendBlockUpdated(this.worldPosition, st, st, 3);
            }
        }
    }

    /** True if at least one fuel-tick remains. */
    public boolean isBurning() {
        return this.fuelRemaining > 0;
    }

    /** True if the fuel slot has any fuel item. */
    private boolean canIgnite() {
        return !this.items.get(SLOT_FUEL).isEmpty();
    }

    // ── Craft resolution (placeholder) ──────────────────────────────────

    /**
     * Placeholder craft check. Returns true if the furnace should advance
     * the craft timer.
     *
     * <p>Current rule: at least one ingredient slot must be non-empty, AND
     * the output slot must be either empty or already contain the placeholder
     * "basic pill" item (sugar) with room for one more.
     *
     * <p>Future: replace with a property-based recipe resolver keyed on
     * ingredient Element/Aspect/Tier.
     */
    private boolean canCraft() {
        boolean anyIngredient = false;
        for (int i = SLOT_INGREDIENT_1; i <= SLOT_INGREDIENT_5; i++) {
            if (!this.items.get(i).isEmpty()) {
                anyIngredient = true;
                break;
            }
        }
        if (!anyIngredient) return false;

        ItemStack out = this.items.get(SLOT_OUTPUT);
        if (out.isEmpty()) return true;
        // TODO: check if output matches the recipe result
        return out.getCount() + 1 <= out.getMaxStackSize();
    }

    /**
     * Consume one of each non-empty ingredient and emit the placeholder
     * "basic pill" into the output slot.
     */
    private void craftItem() {
        // CONSTITUTION Article VIII: "All systems read from the same ontology."
        // CONSTITUTION Article I: Canon recipes are deterministic.
        
        // 1. Collect ingredient IDs
        java.util.List<String> ingredientIds = new java.util.ArrayList<>();
        java.util.List<net.minecraft.resources.ResourceLocation> ingredientRLs = new java.util.ArrayList<>();
        for (int i = SLOT_INGREDIENT_1; i <= SLOT_INGREDIENT_5; i++) {
            ItemStack stack = this.items.get(i);
            if (!stack.isEmpty()) {
                net.minecraft.resources.ResourceLocation rl = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (rl != null) {
                    ingredientIds.add(rl.toString());
                    ingredientRLs.add(rl);
                }
            }
        }
        
        // 2. Match canon recipe (Constitution Article I: canon is deterministic)
        dev.ergenverse.alchemy.CanonPillRecipes.PillRecipe recipe = dev.ergenverse.alchemy.CanonPillRecipes.matchRecipe(ingredientIds);
        
        ItemStack result;
        if (recipe != null) {
            // CONSTITUTION Article VII: "The player does not unlock recipes. The player acquires knowledge."
            // Check if the player knows this recipe.
            // If no player is tracking this furnace, allow the craft (automation).
            // If a player IS tracking, they must know the recipe to brew it.
            boolean playerKnowsRecipe = true; // Default: allow (for hopper automation)
            if (lastPlayer != null) {
                // TODO: Wire to player's KnowledgeEngine capability
                // For now: check if player's cultivation realm meets the recipe's minimum
                try {
                    var capOpt = dev.ergenverse.cultivation.CultivationCapability.get(lastPlayer);
                    if (!capOpt.isPresent()) { result = ItemStack.EMPTY; }
                    else { var cultState = capOpt.resolve().get();
                    int playerRealm = cultState.getCurrentRealm().ordinal();
                    if (playerRealm < recipe.minRealm()) {
                        // Player's cultivation is too low to brew this pill
                        // Constitution Article I: canon requires minimum realm for certain pills
                        result = ItemStack.EMPTY;
                    } else {
                        // CANON MATCH — deterministic result (Constitution Article I)
                        net.minecraft.resources.ResourceLocation resultRL = new net.minecraft.resources.ResourceLocation(recipe.resultItemId());
                        Item resultItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resultRL);
                        result = resultItem != null ? new ItemStack(resultItem) : new ItemStack(Items.SUGAR);
                    } }
                } catch (Exception e) {
                    // No cultivation capability — treat as mortal
                    if (recipe.minRealm() > 0) {
                        result = ItemStack.EMPTY; // Can't brew above-mortal pills without cultivation
                    } else {
                        net.minecraft.resources.ResourceLocation resultRL = new net.minecraft.resources.ResourceLocation(recipe.resultItemId());
                        Item resultItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resultRL);
                        result = resultItem != null ? new ItemStack(resultItem) : new ItemStack(Items.SUGAR);
                    }
                }
            } else {
                // No player — automation. Allow canon recipe.
                net.minecraft.resources.ResourceLocation resultRL = new net.minecraft.resources.ResourceLocation(recipe.resultItemId());
                Item resultItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(resultRL);
                result = resultItem != null ? new ItemStack(resultItem) : new ItemStack(Items.SUGAR);
            }
        } else {
            // EXPERIMENTAL — read MaterialProperties (Constitution Article VIII)
            boolean hasQi = false;
            for (net.minecraft.resources.ResourceLocation rl : ingredientRLs) {
                dev.ergenverse.material.MaterialProperties props = dev.ergenverse.material.MaterialRegistry.get(rl);
                if (props.qiConductivity() > 0.3) { hasQi = true; break; }
            }
            result = hasQi ? new ItemStack(dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get()) : ItemStack.EMPTY;
        }
        
        // 3. Consume ingredients
        for (int i = SLOT_INGREDIENT_1; i <= SLOT_INGREDIENT_5; i++) {
            ItemStack ing = this.items.get(i);
            if (!ing.isEmpty()) ing.shrink(1);
        }
        
        // 4. Output
        if (!result.isEmpty()) {
            ItemStack out = this.items.get(SLOT_OUTPUT);
            if (out.isEmpty()) this.items.set(SLOT_OUTPUT, result);
            else out.grow(1);
        }
    }
}
