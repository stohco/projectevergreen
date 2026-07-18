package dev.ergenverse.wanglin.bead;

/**
 * The six tabs of the Heaven-Defying Bead's storage menu.
 *
 * <p>When the player right-clicks the bead (divine-sense access), they
 * see a menu with these tabs. Each tab becomes available as the bead's
 * interior grows (see {@link BeadInteriorStage}).
 *
 * <p>Canon basis: the bead's functions are distributed across its
 * internal structure. Storage is the surface; time dilation is the
 * chamber; cultivation areas are the valley; special functions are
 * the deepest secrets. The menu organizes these canon-attested
 * functions into tabs.
 */
public enum BeadFunctionTab {
    /** The inventory — chests, racks, warehouses. Available earliest. */
    STORAGE("Storage", "Access the bead's storage vaults.", 0),

    /** The time-dilation chamber. 1 hour inside = 10 hours outside. */
    TIME_DOMAIN("Time Domain", "Enter the time-accelerated chamber.", 1),

    /** The button to physically enter the bead's dimension. */
    WORLD_SPACE("World Space", "Enter the bead's interior world.", 2),

    /** Cultivation areas — herb gardens, meditation spots. */
    CULTIVATION("Cultivation", "Access cultivation areas and herb gardens.", 3),

    /** Beast habitats — manage the Mosquito Beast and others. */
    BEAST_MANAGEMENT("Beast Management", "Manage spirit beasts housed in the bead.", 4),

    /** Deepest secrets — Li Muwan's Nascent Soul, Samsara, divine abilities. */
    SPECIAL_FUNCTIONS("Special Functions", "Access the bead's deepest secrets.", 5);

    public final String label;
    public final String description;
    /** The minimum interior stage at which this tab appears. */
    public final int minStage;

    BeadFunctionTab(String label, String description, int minStage) {
        this.label = label;
        this.description = description;
        this.minStage = minStage;
    }
}