package dev.ergenverse.flora;

/**
 * BiologicalStage — the 8 developmental stages of a flora organism.
 *
 * <p>Plants are not static world-decor. They have a lifecycle: they
 * sprout, grow, mature, flower, fruit, go dormant, and eventually die.
 * Each stage determines harvestability and yield quality. A cultivator's
 * realm determines HOW they harvest (see {@link RealmHarvestBehavior}),
 * but the stage is objective truth stored on the {@link FloraBlockEntity}.
 *
 * <p>The Prime Directive: stage exists whether or not the player perceives
 * it. A mortal sees "a small weed"; a Nascent Soul cultivator sees
 * "a Frost Herb in the Flowering stage, 32 days old." The plant itself
 * is identical.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public enum BiologicalStage {
    SEED      (0, "seed",      false, 0.0f),
    SPROUT    (1, "sprout",    false, 0.0f),
    YOUNG     (2, "young",     true,  0.3f),
    MATURE    (3, "mature",    true,  1.0f),
    FLOWERING (4, "flowering", true,  1.2f),
    FRUITING  (5, "fruiting",  true,  1.5f),
    DORMANT   (6, "dormant",   true,  0.5f),
    DEAD      (7, "dead",      false, 0.0f);

    public final int order;
    public final String label;
    public final boolean canHarvest;
    public final float yieldMultiplier;

    BiologicalStage(int order, String label, boolean canHarvest, float yieldMultiplier) {
        this.order = order;
        this.label = label;
        this.canHarvest = canHarvest;
        this.yieldMultiplier = yieldMultiplier;
    }

    /**
     * Advance to the next stage. DEAD is terminal — calling {@code next()}
     * on DEAD returns DEAD. DORMANT cycles back to SEED (perennial cycle):
     * a dormant plant that survives winter re-sprouts.
     */
    public BiologicalStage next() {
        if (this == DEAD) return DEAD;
        if (this == DORMANT) return SEED; // perennial re-sprout
        return values()[this.ordinal() + 1];
    }

    /** True if this stage is terminal (DEAD). The plant cannot recover. */
    public boolean isTerminal() {
        return this == DEAD;
    }

    /** True if this stage is the fruiting stage (the only stage that yields fruit). */
    public boolean isFruiting() {
        return this == FRUITING;
    }

    /**
     * Look up a stage by its label name. Case-insensitive. Returns
     * {@code MATURE} as a safe default if the name is unrecognized
     * (mature is the most common stage for a placed plant).
     */
    public static BiologicalStage fromName(String name) {
        if (name == null) return MATURE;
        String lower = name.toLowerCase();
        for (BiologicalStage s : values()) {
            if (s.label.equals(lower) || s.name().toLowerCase().equals(lower)) return s;
        }
        return MATURE;
    }
}
