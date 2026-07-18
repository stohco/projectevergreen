package dev.ergenverse.wanglin.bead;

/**
 * How the bead's interior grows over time.
 *
 * <p>Canon basis: the bead starts as a stone (Ch. 8). After Situ Nan blasts
 * it open, it reveals a small interior chamber. As Wang Lin aligns the Five
 * Elements, the interior grows. By end-game it contains an entire world.
 *
 * <p>This progression is NOT driven by XP or levels. It is driven by the
 * same kind of events that drive it in canon: aligning element fragments,
 * gaining the spirit's recognition, fusing with the primordial spirit.
 *
 * <p>Each stage determines the base storage capacity, whether the player
 * can physically enter the dimension, and what functions are available.
 */
public enum BeadInteriorStage {

    /** A stone. No interior. Mortal sees nothing. */
    DORMANT_STONE(0, 0, "A stone bead. Cold and lifeless.",
            false, false, false, false, false, false),

    /** Situ Nan blasted it open. A single small chamber. */
    CRACK_OPENED(1, 9, "A small chamber opens within the bead.",
            true, false, false, false, false, false),

    /** A room-sized space. Basic storage. */
    SMALL_SPACE(2, 27, "The interior has grown into a room.",
            true, false, false, false, false, false),

    /** A valley. Herb gardens become possible. */
    VALLEY(3, 54, "A valley stretches within the bead.",
            true, true, false, true, false, false),

    /** A self-contained world. Ecosystems form. */
    SMALL_WORLD(4, 108, "An entire world exists inside the bead.",
            true, true, true, true, true, false),

    /** The ultimate: a complete ecosystem. The bead IS a universe. */
    COMPLETE_ECOSYSTEM(5, 216, "The bead contains a living universe.",
            true, true, true, true, true, true);

    /** The stage's ordinal. */
    public final int stage;

    /** Base storage slots at this stage. */
    public final int baseSlots;

    /** A narrative description of what the interior looks like. */
    public final String description;

    /** Whether the storage menu (divine-sense access) is available. */
    public final boolean hasStorageAccess;

    /** Whether the player can physically ENTER the bead dimension. */
    public final boolean hasPhysicalEntry;

    /** Whether time dilation is active (10x inside). */
    public final boolean hasTimeDilation;

    /** Whether herb gardens / resource growing is possible. */
    public final boolean hasCultivationAreas;

    /** Whether beast habitats are available. */
    public final boolean hasBeastManagement;

    /** Whether the deepest secrets (Li Muwan, Samsara, divine abilities) are accessible. */
    public final boolean hasSpecialFunctions;

    BeadInteriorStage(int stage, int baseSlots, String description,
                       boolean hasStorageAccess, boolean hasPhysicalEntry,
                       boolean hasTimeDilation, boolean hasCultivationAreas,
                       boolean hasBeastManagement, boolean hasSpecialFunctions) {
        this.stage = stage;
        this.baseSlots = baseSlots;
        this.description = description;
        this.hasStorageAccess = hasStorageAccess;
        this.hasPhysicalEntry = hasPhysicalEntry;
        this.hasTimeDilation = hasTimeDilation;
        this.hasCultivationAreas = hasCultivationAreas;
        this.hasBeastManagement = hasBeastManagement;
        this.hasSpecialFunctions = hasSpecialFunctions;
    }

    /** Which tabs are available at this stage. */
    public boolean tabAvailable(BeadFunctionTab tab) {
        return switch (tab) {
            case STORAGE -> hasStorageAccess;
            case TIME_DOMAIN -> hasTimeDilation;
            case WORLD_SPACE -> hasPhysicalEntry;
            case CULTIVATION -> hasCultivationAreas;
            case BEAST_MANAGEMENT -> hasBeastManagement;
            case SPECIAL_FUNCTIONS -> hasSpecialFunctions;
        };
    }
}