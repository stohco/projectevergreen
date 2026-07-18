package dev.ergenverse.wanglin.bead;

/**
 * How a player accesses the Heaven-Defying Bead.
 *
 * <p>The user's directive: the bead must support BOTH:
 * <ul>
 *   <li><b>Divine-sense access</b> — open the menu, grab items, no
 *       dimension entry needed. This is the "surface interface."</li>
 *   <li><b>Physical entry</b> — actually enter the bead's interior
 *       as a Minecraft dimension. This gives access to cultivation,
 *       time acceleration, growing resources, hidden secrets.</li>
 * </ul>
 * The inventory is just the surface; the dimension is the depth.
 */
public enum BeadAccessMode {
    /**
     * Surface access via divine sense. Opens the storage menu.
     * Available at CRACK_OPENED stage and above.
     */
    DIVINE_SENSE("Divine Sense", "Open the bead's storage menu."),

    /**
     * Deep access — physically enter the bead's interior dimension.
     * Available at VALLEY stage and above.
     */
    PHYSICAL_ENTRY("Physical Entry", "Step into the bead's interior world.");

    public final String label;
    public final String description;

    BeadAccessMode(String label, String description) {
        this.label = label;
        this.description = description;
    }
}