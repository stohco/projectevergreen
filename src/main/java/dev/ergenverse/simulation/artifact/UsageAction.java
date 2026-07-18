package dev.ergenverse.simulation.artifact;

/**
 * The specific action a wielder is attempting with a treasure.
 *
 * <p>Backlash is contextual — it depends on WHAT the wielder is trying to do,
 * not merely on holding the item. Swinging a divine sword never backlashes.
 * Forcing open a sealed restriction is the most dangerous action possible.
 *
 * <p>Canon basis: Wang Lin forcing the Heaven-Defying Bead's sealed layers
 * causes strain but not destruction. Forcing Situ Nan's remnant soul to obey
 * before recognition causes retaliation. Simply holding an over-leveled
 * treasure is always safe.
 */
public enum UsageAction {

    /** Just carrying the item in inventory or holding it in hand. Never backlashes. */
    HOLDING("Holding", 0.0, false),

    /** Physical attack — swinging, stabbing. Only fatigues if the item is extremely heavy. */
    SWINGING("Swinging", 0.05, false),

    /** Enjoying passive benefits (self-repair, aura suppression). Never backlashes. */
    PASSIVE_BENEFIT("Passive Benefit", 0.0, false),

    /** Supplying qi / divine sense to express an ability. May strain if above capacity. */
    ACTIVATING("Activating Ability", 0.3, true),

    /** Riding a flying sword / treasure as a mount. Sustained sense required. */
    FLYING("Flying Mount", 0.35, true),

    /** Commanding a bound treasure spirit. High backlash if spirit doesn't recognize wielder. */
    COMMANDING_SPIRIT("Commanding Spirit", 0.6, true),

    /** Prying open a sealed layer or restriction inside the treasure. Highest backlash risk. */
    FORCING_RESTRICTION("Forcing Sealed Restriction", 0.8, true),

    /** Demanding more energy than the wielder can supply. Qi depletion, regression. */
    OVERDRAWING("Overdrawing Energy", 0.5, true),

    /** Attempting blood refinement to bond with the treasure. One-time, risky. */
    BLOOD_REFINING("Blood Refinement", 0.4, true),

    /** Entering a treasure's interior space (e.g., Heaven-Defying Bead chamber). */
    ENTERING_INTERIOR("Entering Interior", 0.2, true);

    /** Human-readable name for log messages. */
    public final String label;

    /**
     * Base backlash multiplier for this action. The engine scales this by
     * the gap between user and treasure. HOLDING/SWINGING/PASSIVE_BENEFIT
     * have near-zero multipliers because these actions are inherently safe.
     */
    public final double baseBacklashMultiplier;

    /** Whether this action requires any energy (qi, sense, etc.) at all. */
    public final boolean requiresEnergy;

    UsageAction(String label, double baseBacklashMultiplier, boolean requiresEnergy) {
        this.label = label;
        this.baseBacklashMultiplier = baseBacklashMultiplier;
        this.requiresEnergy = requiresEnergy;
    }
}