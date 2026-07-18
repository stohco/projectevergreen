package dev.ergenverse.screen;

import dev.ergenverse.block.entity.AlchemyFurnaceBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * AlchemyFurnaceMenu — the player-facing container for the Alchemy Furnace.
 *
 * <h2>Slot layout (176×166 GUI)</h2>
 * <pre>
 *   Index  Slot          Pixel (x, y)
 *   ─────  ───────────   ────────────
 *   0      Ingredient 1  (44, 17)
 *   1      Ingredient 2  (62, 17)
 *   2      Ingredient 3  (80, 17)
 *   3      Ingredient 4  (53, 35)
 *   4      Ingredient 5  (71, 35)
 *   5      Fuel          (8, 35)
 *   6      Output        (134, 35)
 *   7..33  Player inv    (8 + col*18, 84 + row*18)
 *   34..42 Player hotbar (8 + col*18, 142)
 * </pre>
 *
 * <p>The five ingredient slots form a 3-2 grid (a stylized plus/pentagon
 * shape) — this is the canonical xianxia alchemy layout (three heavens above,
 * two earths below). The fuel slot sits to the lower-left of the grid (with
 * the flame icon between it and the grid); the output slot sits to the
 * lower-right (with the progress arrow between the grid and the output).
 *
 * <h2>Synchronization</h2>
 * <p>A {@link ContainerData} of size 3 is attached:
 * <ul>
 *   <li>0 — craft progress (0..{@link AlchemyFurnaceBlockEntity#CRAFT_TIME})</li>
 *   <li>1 — fuel remaining ticks</li>
 *   <li>2 — fuel max ticks (for the flame-height rendering)</li>
 * </ul>
 *
 * <h2>Shift-click transfer</h2>
 * <ul>
 *   <li>From an ingredient or fuel slot → player inventory (hotbar last).</li>
 *   <li>From the output slot → player inventory (hotbar last).</li>
 *   <li>From the player inventory → ingredient slots first, then fuel.</li>
 *   <li>From the hotbar → player inventory first, then ingredients/fuel.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class AlchemyFurnaceMenu extends AbstractContainerMenu {

    // ── Slot index constants (menu-side, NOT BE-side) ───────────────────
    /** Number of furnace-owned slots (5 ingredients + 1 fuel + 1 output). */
    public static final int FURNACE_SLOTS = 7;

    /** First player-inventory slot index in the menu. */
    public static final int PLAYER_INV_START = FURNACE_SLOTS;            // 7
    /** First hotbar slot index in the menu. */
    public static final int HOTBAR_START = PLAYER_INV_START + 27;        // 34
    /** One-past-last slot index in the menu. */
    public static final int TOTAL_SLOTS = HOTBAR_START + 9;              // 43

    // ── Pixel coordinates (must match the GUI texture) ──────────────────
    private static final int INGREDIENT_X_TOP    = 44; // top row, left
    private static final int INGREDIENT_X_MID    = 62; // top row, middle
    private static final int INGREDIENT_X_RIGHT  = 80; // top row, right
    private static final int INGREDIENT_X_BOT_L  = 53; // bottom row, left
    private static final int INGREDIENT_X_BOT_R  = 71; // bottom row, right
    private static final int INGREDIENT_Y_TOP    = 17;
    private static final int INGREDIENT_Y_BOTTOM = 35;

    private static final int FUEL_X = 8;
    private static final int FUEL_Y = 35;

    private static final int OUTPUT_X = 134;
    private static final int OUTPUT_Y = 35;

    private static final int PLAYER_INV_X = 8;
    private static final int PLAYER_INV_Y = 84;
    private static final int HOTBAR_Y     = 142;

    // ── State ───────────────────────────────────────────────────────────
    /** The furnace block entity backing this menu (server side). */
    private final AlchemyFurnaceBlockEntity blockEntity;
    /** The furnace inventory (the BE itself is a Container). */
    private final Container furnaceContainer;
    /** Synchronized craft/fuel data. */
    private final ContainerData data;

    // ── Constructor (server-side) ───────────────────────────────────────
    public AlchemyFurnaceMenu(int containerId, Inventory playerInv,
                              AlchemyFurnaceBlockEntity blockEntity,
                              ContainerData data) {
        super(ErgenverseMenus.ALCHEMY_FURNACE.get(), containerId);
        this.blockEntity = blockEntity;
        this.furnaceContainer = blockEntity;
        this.data = data;
        checkContainerSize(blockEntity, FURNACE_SLOTS);
        checkContainerDataCount(data, 3);

        // Capture the player reference up-front — ResultSlot needs it.
        Player player = playerInv.player;

        // ── Furnace slots ──────────────────────────────────────────────
        // Five ingredient slots — 3-2 grid.
        this.addSlot(new Slot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_INGREDIENT_1,
                INGREDIENT_X_TOP, INGREDIENT_Y_TOP));
        this.addSlot(new Slot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_INGREDIENT_2,
                INGREDIENT_X_MID, INGREDIENT_Y_TOP));
        this.addSlot(new Slot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_INGREDIENT_3,
                INGREDIENT_X_RIGHT, INGREDIENT_Y_TOP));
        this.addSlot(new Slot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_INGREDIENT_4,
                INGREDIENT_X_BOT_L, INGREDIENT_Y_BOTTOM));
        this.addSlot(new Slot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_INGREDIENT_5,
                INGREDIENT_X_BOT_R, INGREDIENT_Y_BOTTOM));

        // Fuel slot — accepts only fuel items.
        this.addSlot(new FuelSlot(this.furnaceContainer,
                AlchemyFurnaceBlockEntity.SLOT_FUEL, FUEL_X, FUEL_Y));

        // Output slot — ResultSlot prevents item placement and handles
        // the standard "shift-click into player inventory" behavior.
        this.addSlot(new Slot(furnaceContainer, AlchemyFurnaceBlockEntity.SLOT_OUTPUT, OUTPUT_X, OUTPUT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });

        // ── Player inventory (3 rows of 9) ─────────────────────────────
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv,
                        col + row * 9 + 9,
                        PLAYER_INV_X + col * 18,
                        PLAYER_INV_Y + row * 18));
            }
        }
        // ── Player hotbar (1 row of 9) ─────────────────────────────────
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col,
                    PLAYER_INV_X + col * 18, HOTBAR_Y));
        }

        // ── Synchronized data (craft/fuel) ─────────────────────────────
        addDataSlots(this.data);
    }

    // ── Shift-click transfer ────────────────────────────────────────────
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (index < FURNACE_SLOTS) {
            // From a furnace slot (ingredient/fuel/output) → player inventory.
            if (!this.moveItemStackTo(original, PLAYER_INV_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player inventory/hotbar.
            // First try the ingredient slots (0..4) — most permissive.
            if (!this.moveItemStackTo(original, 0, 5, false)) {
                // Then try the fuel slot (5).
                if (!this.moveItemStackTo(original, 5, 6, false)) {
                    // Then rearrange between player inv and hotbar.
                    if (index < HOTBAR_START) {
                        if (!this.moveItemStackTo(original, HOTBAR_START, TOTAL_SLOTS, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(original, PLAYER_INV_START, HOTBAR_START, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    // ── Validity check ──────────────────────────────────────────────────
    @Override
    public boolean stillValid(Player player) {
        return this.furnaceContainer.stillValid(player);
    }

    // ── Accessors used by the screen ────────────────────────────────────
    /** Current craft progress (0..CRAFT_TIME). */
    public int getCraftProgress() {
        return this.data.get(0);
    }

    /** Remaining fuel ticks. */
    public int getFuelRemaining() {
        return this.data.get(1);
    }

    /** Max fuel ticks for the currently burning fuel (for flame height). */
    public int getFuelMax() {
        return this.data.get(2);
    }

    /** Convenience: scaled craft progress for the arrow (0..pixelWidth). */
    public int getScaledCraftProgress(int pixelWidth) {
        int progress = getCraftProgress();
        int total = AlchemyFurnaceBlockEntity.CRAFT_TIME;
        return total == 0 || progress == 0 ? 0 : progress * pixelWidth / total;
    }

    /** Convenience: scaled fuel remaining for the flame (0..pixelHeight). */
    public int getScaledFuelRemaining(int pixelHeight) {
        int max = getFuelMax();
        if (max == 0) return 0;
        return getFuelRemaining() * pixelHeight / max;
    }

    /** Expose the BE for any menu-driven lookups (currently unused). */
    public AlchemyFurnaceBlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    // ── Custom fuel slot ────────────────────────────────────────────────
    /**
     * A fuel-only slot. Rejects non-fuel items via {@link #mayPlace}.
     * Uses {@link ItemStack#getBurnTime} against the vanilla smelting
     * recipe type, which matches coal, charcoal, lava buckets, blaze rods,
     * etc. — anything vanilla considers furnace fuel.
     */
    private static class FuelSlot extends Slot {
        FuelSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return stack.getBurnTime(RecipeType.SMELTING) > 0;
        }
    }
}
