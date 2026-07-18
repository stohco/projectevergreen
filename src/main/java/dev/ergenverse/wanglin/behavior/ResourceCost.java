package dev.ergenverse.wanglin.behavior;

/**
 * ResourceCost — what the cultivator pays to use the item / technique.
 *
 * <p>All amounts are non-negative; -1 means "unknown" / "not applicable".
 * Per the Prime Directive: where canon is silent, leave UNKNOWN.
 *
 * @param qiCost             spiritual-energy cost (units undefined; relative scale)
 * @param soulCost           soul-force / origin-soul cost
 * @param divineSenseCost    divine-sense cost
 * @param jossFlameCost      joss-flame (faith-energy) cost
 * @param lifeForceCost      life-force (lifespan) cost — for Qi Xi Spell etc.
 * @param karmaCost          karmic-debt cost
 * @param cooldownTicks      cooldown in Minecraft ticks (20 ticks = 1 sec); -1 = none
 * @param chargeupTicks      charge-up time in ticks; 0 = instant
 * @param notes              free-text canon notes
 */
public record ResourceCost(
        int qiCost,
        int soulCost,
        int divineSenseCost,
        int jossFlameCost,
        int lifeForceCost,
        int karmaCost,
        int cooldownTicks,
        int chargeupTicks,
        String notes
) {
    public static final ResourceCost UNKNOWN = new ResourceCost(
            -1, -1, -1, -1, -1, -1, -1, -1,
            "UNKNOWN — canon does not specify cost mechanics.");

    public ResourceCost {
        if (notes == null) notes = "";
    }

    // ── Convenience factories ─────────────────────────────────────────

    public static ResourceCost qi(int qi, String notes) {
        return new ResourceCost(qi, 0, 0, 0, 0, 0, 0, 0, notes);
    }

    public static ResourceCost divineSense(int ds, int cooldown, String notes) {
        return new ResourceCost(0, 0, ds, 0, 0, 0, cooldown, 0, notes);
    }

    public static ResourceCost soulAndDivineSense(int soul, int ds, int cooldown, String notes) {
        return new ResourceCost(0, soul, ds, 0, 0, 0, cooldown, 0, notes);
    }

    public static ResourceCost lifeForceSacrifice(int lifePct, String notes) {
        return new ResourceCost(0, 0, 0, 0, lifePct, 0, 0, 0, notes);
    }
}
