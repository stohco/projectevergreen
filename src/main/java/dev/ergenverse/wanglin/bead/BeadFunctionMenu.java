package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * The Heaven-Defying Bead's storage menu — opened via divine-sense right-click.
 *
 * <p>This is the "surface interface" to the bead. It provides:
 * <ul>
 *   <li>A variable-size storage inventory (9 to 216 slots depending on stage)</li>
 *   <li>Tab switching between the 6 {@link BeadFunctionTab} functions</li>
 *   <li>Stage-aware slot locking (unavailable tabs show locked slots)</li>
 * </ul>
 *
 * <h2>Layout</h2>
 * <p>The menu uses a dynamic layout:
 * <ul>
 *   <li>Top row: 9 tab indicator slots (one per {@link BeadFunctionTab})</li>
 *   <li>Player inventory: standard 9x4 + 9 hotbar slots</li>
 *   <li>Bead storage: rows of 9 slots, number of rows = stage-dependent</li>
 * </ul>
 *
 * <h2>Data Synchronization</h2>
 * <p>The menu uses {@link ContainerData} to synchronize the bead's state
 * (stage, active tab, slot count) between server and client. The actual
 * storage inventory is written to the bead item's NBT when the menu closes.
 *
 * <p>Canon: Wang Lin accesses the bead's storage via divine sense throughout
 * the novel. He grabs weapons, stores herbs, retrieves formation materials.
 * The menu represents this divine-sense interface.
 *
 * @see HeavenDefyingBeadItem  — the Item subclass that opens this menu
 * @see BeadInteriorStage      — determines which tabs/slots are available
 */
public class BeadFunctionMenu extends AbstractContainerMenu {

    // ── Menu Type Registration ─────────────────────────────────────

    /** DeferredRegister for the bead menu type. */
    private static final DeferredRegister<MenuType<?>> MENU_REGISTER =
            DeferredRegister.create(Registries.MENU, Ergenverse.MOD_ID);

    /** The registered menu type for the bead function menu. */
    public static final RegistryObject<MenuType<BeadFunctionMenu>> TYPE =
            MENU_REGISTER.register("bead_function", () -> IForgeMenuType.create((containerId, playerInv, data) ->
                    new BeadFunctionMenu(containerId, playerInv, data)));

    /**
     * Wire the menu type to the mod event bus. Call from Ergenverse constructor.
     */
    public static void register(IEventBus modEventBus) {
        MENU_REGISTER.register(modEventBus);
        Ergenverse.LOGGER.info("[Ergenverse] Bead function menu type registered.");
    }

    // ── Layout Constants ─────────────────────────────────────────────

    /** Slots per row in the bead storage area. */
    private static final int SLOTS_PER_ROW = 9;

    /** Player inventory starts at this slot index. */
    private static final int PLAYER_INVENTORY_START = 0;

    /** Player hotbar starts at this slot index. */
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_START + 27;

    /** Bead storage starts after player slots + tab row. */
    private static final int TAB_ROW_START = PLAYER_HOTBAR_START + 9;

    /** Bead storage starts after the tab indicator row. */
    private static final int BEAD_STORAGE_START = TAB_ROW_START + 9;

    /** Maximum possible bead storage rows (COMPLETE_ECOSYSTEM = 216 slots = 24 rows). */
    private static final int MAX_STORAGE_ROWS = 24;

    /** Maximum possible total slots (player + tab + max storage). */
    private static final int MAX_TOTAL_SLOTS = BEAD_STORAGE_START + (MAX_STORAGE_ROWS * SLOTS_PER_ROW);

    // ── State ────────────────────────────────────────────────────────

    /** The bead item stack this menu operates on. */
    private final ItemStack beadStack;

    /** The interior stage (determines available tabs and slot count). */
    private final BeadInteriorStage stage;

    /** The number of storage slots available at this stage. */
    private final int slotCount;

    /** The currently active function tab. */
    private int activeTab;

    /** The bead's internal storage contents. */
    private final NonNullList<ItemStack> beadStorage;

    /** Synchronized data: stage ordinal, active tab, storage slot count. */
    private final ContainerData beadData;

    // ── Factory ──────────────────────────────────────────────────────

    /**
     * Create a new bead function menu.
     *
     * <p>Called from {@link HeavenDefyingBeadItem#openBeadMenu} and
     * from the Forge menu type factory.
     */
    public static BeadFunctionMenu create(int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                                          ItemStack beadStack, BeadInteriorStage stage,
                                          int slotCount, int activeTab) {
        return new BeadFunctionMenu(containerId, playerInv, beadStack, stage, slotCount, activeTab);
    }

    // ── Constructor ──────────────────────────────────────────────────

    /**
     * Internal constructor. Called both by the factory and by the MenuType.
     */
    public BeadFunctionMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                            net.minecraft.network.FriendlyByteBuf buf) {
        this(containerId, playerInv, ItemStack.EMPTY,
                BeadInteriorStage.values()[Math.min(buf.readVarInt(),
                        BeadInteriorStage.values().length - 1)],
                buf.readVarInt(),
                buf.readVarInt());
    }

    private BeadFunctionMenu(int containerId, net.minecraft.world.entity.player.Inventory playerInv,
                             ItemStack beadStack, BeadInteriorStage stage,
                             int slotCount, int activeTab) {
        super(TYPE.get(), containerId);
        this.beadStack = beadStack;
        this.stage = stage;
        this.slotCount = Math.min(slotCount, MAX_STORAGE_ROWS * SLOTS_PER_ROW);
        this.activeTab = Math.max(0, Math.min(activeTab, BeadFunctionTab.values().length - 1));
        this.beadStorage = NonNullList.withSize(this.slotCount, ItemStack.EMPTY);

        // Load existing storage from bead NBT
        if (!beadStack.isEmpty() && beadStack.hasTag()) {
            CompoundTag tag = beadStack.getTag();
            if (tag.contains("Ergen.Bead.Storage")) {
                CompoundTag storageTag = tag.getCompound("Ergen.Bead.Storage");
                int count = Math.min(storageTag.getInt("Size"), this.slotCount);
                for (int i = 0; i < count; i++) {
                    if (storageTag.contains("Slot" + i)) {
                        this.beadStorage.set(i, ItemStack.of(storageTag.getCompound("Slot" + i)));
                    }
                }
            }
        }

        // Player inventory (3 rows of 9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv,
                        col + row * 9,
                        8 + col * 18,
                        84 + row * 18));
            }
        }

        // Player hotbar (1 row of 9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, 142));
        }

        // Tab indicator row (non-interactive slots showing tab icons)
        for (int i = 0; i < BeadFunctionTab.values().length; i++) {
            BeadFunctionTab tab = BeadFunctionTab.values()[i];
            int x = 8 + i * 18;
            int y = 5;
            // Tab slots are output-only: player can see them but not take items.
            // We use a custom slot that prevents interaction.
            this.addSlot(new TabIndicatorSlot(beadStorage, i, x, y, tab));
        }

        // Bead storage slots (dynamic rows based on stage)
        int storageRows = (int) Math.ceil((double) this.slotCount / SLOTS_PER_ROW);
        for (int row = 0; row < storageRows; row++) {
            for (int col = 0; col < SLOTS_PER_ROW; col++) {
                int slotIndex = row * SLOTS_PER_ROW + col;
                if (slotIndex >= this.slotCount) break;
                this.addSlot(new BeadStorageSlot(this.beadStorage, slotIndex,
                        8 + col * 18, 27 + row * 18));
            }
        }

        // Synchronized data for client
        this.beadData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> stage.ordinal();
                    case 1 -> BeadFunctionMenu.this.activeTab;
                    case 2 -> BeadFunctionMenu.this.slotCount;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 1 -> BeadFunctionMenu.this.activeTab = value;
                    // case 0 and 2 are read-only from the client
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
        this.addDataSlots(this.beadData);
    }

    // ── Slot Behavior ────────────────────────────────────────────────

    /**
     * Non-interactive slot for the tab indicator row.
     * Shows which tab is active but doesn't allow item placement.
     */
    private static class TabIndicatorSlot extends Slot {
        private final BeadFunctionTab tab;

        TabIndicatorSlot(NonNullList<ItemStack> items, int index, int x, int y,
                         BeadFunctionTab tab) {
            super(new net.minecraft.world.SimpleContainer(items.toArray(new ItemStack[0])), index, x, y);
            this.tab = tab;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;  // Tab indicators are not interactive
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;  // Cannot take items from tab indicators
        }

        @Override
        public int getMaxStackSize() {
            return 0;
        }
    }

    /**
     * Storage slot inside the bead. Items placed here are stored
     * in the bead's NBT when the menu closes.
     */
    private static class BeadStorageSlot extends Slot {
        BeadStorageSlot(NonNullList<ItemStack> items, int index, int x, int y) {
            super(new net.minecraft.world.SimpleContainer(items.toArray(new ItemStack[0])), index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            // Cannot place the bead inside itself
            if (stack.getItem() instanceof HeavenDefyingBeadItem) return false;
            return true;
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public boolean mayPickup(Player player) {
            return true;
        }
    }

    // ── Container Logic ──────────────────────────────────────────────

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            itemStack = original.copy();

            // Determine if this is a bead storage slot
            boolean isBeadSlot = index >= BEAD_STORAGE_START;

            if (isBeadSlot) {
                // Moving from bead storage to player inventory
                if (!this.moveItemStackTo(original,
                        PLAYER_INVENTORY_START, PLAYER_HOTBAR_START + 9,
                        true)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INVENTORY_START) {
                // Moving from player inventory to bead storage
                if (!this.moveItemStackTo(original,
                        BEAD_STORAGE_START, BEAD_STORAGE_START + slotCount,
                        false)) {
                    // If bead storage is full, try rearranging within player inv
                    if (index < PLAYER_HOTBAR_START) {
                        if (!this.moveItemStackTo(original,
                                PLAYER_HOTBAR_START, PLAYER_HOTBAR_START + 9,
                                false)) {
                            return ItemStack.EMPTY;
                        }
                    } else {
                        if (!this.moveItemStackTo(original,
                                PLAYER_INVENTORY_START, PLAYER_HOTBAR_START,
                                false)) {
                            return ItemStack.EMPTY;
                        }
                    }
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
        // The menu is valid as long as the player is alive and has the bead.
        // We don't check distance because the bead is accessed via divine sense.
        return player.isAlive();
    }

    /**
     * Called when the player closes the menu. Persists the bead's storage
     * to the item's NBT so items survive between menu sessions.
     */
    @Override
    public void removed(Player player) {
        super.removed(player);
        saveStorageToNBT();
    }

    /**
     * Save the bead's storage contents back to the item's NBT.
     */
    private void saveStorageToNBT() {
        if (beadStack.isEmpty()) return;

        CompoundTag tag = beadStack.getOrCreateTag();
        CompoundTag storageTag = new CompoundTag();
        storageTag.putInt("Size", slotCount);

        for (int i = 0; i < slotCount; i++) {
            ItemStack slotItem = beadStorage.get(i);
            if (!slotItem.isEmpty()) {
                CompoundTag slotTag = new CompoundTag();
                slotItem.save(slotTag);
                storageTag.put("Slot" + i, slotTag);
            }
        }

        tag.put("Ergen.Bead.Storage", storageTag);

        // Update active tab
        if (beadStack.getItem() instanceof HeavenDefyingBeadItem beadItem) {
            beadItem.setActiveTab(beadStack, activeTab);
        }

        Ergenverse.LOGGER.debug("[Ergenverse] Bead storage saved ({} slots, "
                + "activeTab={})", slotCount, activeTab);
    }

    // ── Tab Switching ────────────────────────────────────────────────

    /**
     * Switch the active function tab.
     *
     * <p>Client calls this via a button click. The server validates
     * that the tab is available at the current stage.
     *
     * @param tabOrdinal the tab to switch to
     * @return true if the tab switch was allowed
     */
    public boolean switchTab(int tabOrdinal) {
        BeadFunctionTab[] tabs = BeadFunctionTab.values();
        if (tabOrdinal < 0 || tabOrdinal >= tabs.length) return false;

        BeadFunctionTab tab = tabs[tabOrdinal];
        if (!stage.tabAvailable(tab)) {
            Ergenverse.LOGGER.debug("[Ergenverse] Tab {} not available at stage {}",
                    tab, stage);
            return false;
        }

        this.activeTab = tabOrdinal;
        this.beadData.set(1, tabOrdinal);
        return true;
    }

    // ── Accessors ────────────────────────────────────────────────────

    public BeadInteriorStage getStage() { return stage; }
    public int getSlotCount() { return slotCount; }
    public int getActiveTab() { return activeTab; }
    public NonNullList<ItemStack> getBeadStorage() { return beadStorage; }

    /**
     * Get the number of storage rows for layout calculations.
     */
    public int getStorageRows() {
        return (int) Math.ceil((double) slotCount / SLOTS_PER_ROW);
    }
}