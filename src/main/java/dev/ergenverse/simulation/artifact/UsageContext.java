package dev.ergenverse.simulation.artifact;

import dev.ergenverse.cultivation.RealmId;

/**
 * The context in which a wielder uses a treasure.
 *
 * <p>This is the input to {@link ArtifactUsageEngine#calculateUsage}.
 * It captures everything about the wielder and the action they're attempting.
 * The engine combines this with the treasure's {@link ArtifactUsageProfile}
 * to produce an {@link ArtifactOutput}.
 *
 * <p>Canon basis: Wang Lin's effectiveness with a treasure depends on his
 * qi reserves, divine sense strength, blood-refinement bond with the item,
 * karmic compatibility, and what action he's attempting — not on a single
 * "is your realm high enough?" check.
 */
public record UsageContext(
        /** The wielder's current cultivation realm. */
        RealmId userRealm,

        /** The realm the treasure was designed/forged for (from its profile). */
        RealmId artifactRealm,

        /**
         * Current qi the wielder can supply, normalized 0..1 relative to
         * what the artifact demands at full expression.
         * 1.0 = exactly enough. >1.0 = surplus. <1.0 = deficit.
         */
        double qiAvailable,

        /**
         * Divine sense throughput, normalized 0..1 relative to what the
         * artifact requires for full control.
         */
        double spiritualSenseStrength,

        /**
         * How deeply the wielder has blood-refined this specific treasure.
         * 0.0 = no bond. 1.0 = fully refined (Wang Lin and his sword).
         * Blood refinement is per-item, not per-player-general.
         */
        double bloodRefinementDepth,

        /**
         * Dao affinity / karmic fit between wielder and treasure.
         * 0.0 = no affinity (wrong Dao, incompatible bloodline).
         * 1.0 = perfect affinity (same Dao, karmic bond).
         * Canon: the Heaven-Defying Bead recognizes Wang Lin through
         * destiny — this is the extreme end of compatibility.
         */
        double compatibility,

        /**
         * Total energy reserve the wielder can burn through.
         * Spiritual stones, pills, stored qi. Normalized 0..1.
         * Overdrawing beyond this causes QI_DEPLETION or worse.
         */
        double energyReserve,

        /** What the wielder is trying to do with the treasure. */
        UsageAction action,

        /**
         * The treasure spirit's recognition level toward THIS wielder.
         * Null if the treasure has no spirit.
         */
        TreasureSpirit.Recognition spiritRecognition
) {
    public UsageContext {
        qiAvailable = clamp01(qiAvailable);
        spiritualSenseStrength = clamp01(spiritualSenseStrength);
        bloodRefinementDepth = clamp01(bloodRefinementDepth);
        compatibility = clamp01(compatibility);
        energyReserve = clamp01(energyReserve);
        if (action == null) action = UsageAction.HOLDING;
    }

    /** Convenience: a context for a mortal holding something. */
    public static UsageContext mortalHolding(RealmId artifactRealm) {
        return new UsageContext(RealmId.MORTAL, artifactRealm,
                0.0, 0.0, 0.0, 0.5, 0.0,
                UsageAction.HOLDING, null);
    }

    /** Convenience: a context for a specific realm doing a specific action. */
    public static UsageContext of(RealmId user, RealmId artifact, UsageAction action,
                                  double qi, double sense, double blood, double compat) {
        return new UsageContext(user, artifact, qi, sense, blood, compat, 0.5,
                action, null);
    }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }

    /**
     * The raw realm gap: positive means the wielder is BELOW the artifact's
     * intended level. Negative means above (overqualified — no backlash from
     * realm gap, but might be underutilizing).
     */
    public int realmGap() {
        return artifactRealm.order - userRealm.order;
    }
}