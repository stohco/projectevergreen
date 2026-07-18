package dev.ergenverse.alchemy;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

/**
 * Server-side menu for the Pill Furnace (丹炉).
 *
 * <p>Layout:
 * <ul>
 *   <li>Slot 0: Primary herb input</li>
 *   <li>Slot 1: Secondary input (catalyst/core)</li>
 *   <li>Slot 2: Fuel (spirit stone, consumed)</li>
 *   <li>Slot 3: Output (pill or waste pill)</li>
 * </ul>
 *
 * <p>The player opens this via right-click on the Pill Furnace block.
 * A "Craft" button in the client screen sends a C2S packet that triggers
 * the actual crafting logic on the server.
 *
 * <p>Per DESIGN_UNIFIED_CRAFTING.md §13.1: duration 4-6s per craft.
 * The client displays a progress bar that fills over this time.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public class PillFurnaceMenu extends AbstractContainerMenu {

    // ── Menu Type Registration ─────────────────────────────────────

    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(net.minecraft.core.registries.Registries.MENU, Ergenverse.MOD_ID);

    public static final RegistryObject<MenuType<PillFurnaceMenu>> TYPE =
            MENU_REGISTER.register("pill_furnace", () -> IForgeMenuType.create(
                    (containerId, playerInv, data) -> {
                        // Client-side construction: read BlockPos from buffer,
                        // fetch the BE, build the menu.
                        BlockPos pos = data.readBlockPos();
                        BlockEntity be = playerInv.player.level().getBlockEntity(pos);
                        if (be instanceof PillFurnaceBlockEntity furnace) {
                            return new PillFurnaceMenu(containerId, playerInv, furnace);
                        }
                        return null;
                    }));

    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Pill Furnace menu type registered.");
    }

    // ── Layout Constants ───────────────────────────────────────────

    /** Inventory slot indices inside the menu (furnace slots come first). */
    public static final int FURNACE_SLOT_COUNT = 4;
    public static final int SLOT_INPUT_PRIMARY = 0;
    public static final int SLOT_INPUT_CATALYST = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_OUTPUT = 3;

    private static final int PLAYER_INVENTORY_START = FURNACE_SLOT_COUNT;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;
    private static final int TOTAL_SLOTS = PLAYER_HOTBAR_START + 9;

    // ── State ──────────────────────────────────────────────────────

    private final PillFurnaceBlockEntity furnace;
    private final ContainerLevelAccess access;

    // ── Constructors ───────────────────────────────────────────────

    /**
     * Server-side constructor. Called from {@link PillFurnaceBlock#use}
     * via the MenuProvider.
     */
    public PillFurnaceMenu(int containerId, Inventory playerInv, PillFurnaceBlockEntity furnace) {
        super(TYPE.get(), containerId);
        this.furnace = furnace;
        this.access = ContainerLevelAccess.create(furnace.getLevel(), furnace.getBlockPos());

        ItemStackHandler inv = furnace.getInventory();

        // Furnace slots (top-left area, 4 in a row at y=36)
        this.addSlot(new SlotItemHandler(inv, SLOT_INPUT_PRIMARY, 44, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_INPUT_CATALYST, 66, 36));
        this.addSlot(new SlotItemHandler(inv, SLOT_FUEL, 88, 36));
        // Output slot — mayPlace = false
        this.addSlot(new SlotItemHandler(inv, SLOT_OUTPUT, 134, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) { return false; }
        });

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // Player hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    /** Get the furnace backing this menu (server-side). */
    public PillFurnaceBlockEntity getFurnace() { return furnace; }

    // ── Slot Behavior ───────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();

            if (index < FURNACE_SLOT_COUNT) {
                // Moving from furnace → player inventory
                if (!this.moveItemStackTo(original, PLAYER_INVENTORY_START, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory → furnace input/fuel slots (NOT output)
                if (!this.moveItemStackTo(original, SLOT_INPUT_PRIMARY, SLOT_OUTPUT, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (original.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) ->
                level.getBlockState(pos).getBlock() instanceof PillFurnaceBlock
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }

    /**
     * Start a craft on the server side. Called when the player clicks the
     * "Craft" button. Validates that inputs + fuel are present before
     * starting.
     *
     * @param recipeId the recipe to craft
     * @return true if the craft was started
     */
    public boolean tryStartCraft(String recipeId) {
        if (furnace.isCrafting()) return false;
        dev.ergenverse.alchemy.AlchemyCraftingLogic.PillRecipe recipe =
                dev.ergenverse.alchemy.AlchemyCraftingLogic.getRecipe(recipeId);
        if (recipe == null) return false;
        int duration = recipe.craftDurationTicks;
        furnace.startCraft(recipeId, duration);
        return true;
    }
}
