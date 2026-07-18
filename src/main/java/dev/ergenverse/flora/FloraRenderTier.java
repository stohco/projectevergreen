package dev.ergenverse.flora;

/**
 * FloraRenderTier — the 4 visual/behavioral tiers of flora, distributed
 * across the world in a 70/20/8/2 ratio.
 *
 * <p>This is a RENDERING and TICKING classification, not a power classification.
 * A CROSS_SPRITE herb can still be more pharmacologically potent than a TREE.
 * The tier determines:
 * <ul>
 *   <li>How the block is rendered (cross planes vs voxel model vs structure)</li>
 *   <li>Whether the block has collision (pass-through small herbs vs solid shrubs/trees)</li>
 *   <li>Whether the block's BlockEntity ticks (legendary flora have active behavior)</li>
 * </ul>
 *
 * <p>The distribution is objective — it shapes how the world looks and feels.
 * A mortal sees mostly cross-sprite herbs; legendary flora are rare ecosystem
 * anchors that reshape their surroundings regardless of who observes them.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public enum FloraRenderTier {
    /**
     * 70% — small herbs rendered as vanilla-style crossed planes
     * (FlowerBlock model). Cheap, dense, the visual baseline of the world.
     * No collision, no ticking required.
     */
    CROSS_SPRITE("Cross Sprite", false, false),

    /**
     * 20% — woody shrubs rendered as full voxel block models with
     * collision. Slightly more expensive but still common.
     */
    SHRUB("Shrub", true, false),

    /**
     * 8% — full trees with custom trunk + leaves. Worldgen-placed,
     * not block-placed. Ticking is optional (for seasonal leaf drop).
     */
    TREE("Tree", true, true),

    /**
     * 2% — unique ecosystem objects with active behavior (the
     * Heaven-Defying Tree, the Karma Bodhi, the Samsara Lotus Pond).
     * Structure-like, ticking, often quest anchors.
     */
    LEGENDARY("Legendary", true, true);

    public final String label;
    public final boolean hasCollision;
    public final boolean isTicking;

    FloraRenderTier(String label, boolean hasCollision, boolean isTicking) {
        this.label = label;
        this.hasCollision = hasCollision;
        this.isTicking = isTicking;
    }

    /** Case-insensitive lookup by name or label. Defaults to CROSS_SPRITE. */
    public static FloraRenderTier fromName(String name) {
        if (name == null) return CROSS_SPRITE;
        String upper = name.toUpperCase();
        try {
            return FloraRenderTier.valueOf(upper);
        } catch (IllegalArgumentException e) {
            for (FloraRenderTier t : values()) {
                if (t.label.equalsIgnoreCase(name)) return t;
            }
            return CROSS_SPRITE;
        }
    }
}
