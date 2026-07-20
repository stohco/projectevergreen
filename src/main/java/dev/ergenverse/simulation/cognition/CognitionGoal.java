package dev.ergenverse.simulation.cognition;

/**
 * CognitionGoal — a goal produced by the cognition pipeline.
 *
 * <p>Goals are generated from unsatisfied needs (via {@link GoalGenerator}),
 * weighted by Dao Identity (via {@link DaoIdentity#goalWeightModifier}),
 * and ranked by urgency × priority. The highest-ranked goal is passed to
 * the {@link Planner} to produce action options.
 *
 * <p>A goal carries a {@link Category} so the personality model can score
 * actions that pursue it. The PersonalityModel's scoreOption() function
 * is THE critical function in the cognition pipeline.
 */
public final class CognitionGoal {

    // ── Categories ─────────────────────────────────────────────────────

    /** Goal category — used for Dao Identity weighting + personality scoring. */
    public enum Category {
        SURVIVE,
        DEFEND,
        DEFEND_TERRITORY,
        GATHER_RESOURCE,
        KILL,
        FLEE,
        HIDE,
        SEEKING_DAO,
        BREAKTHROUGH,
        MEDITATE,
        STUDY,
        CRAFT,
        BREAK_FORMATION,
        INVESTIGATE,
        DECEIVE,
        CORRUPT,
        POLITICS,
        KEEP_PROMISE,
        FORGIVE,
        RESOLVE_DEBT,
        RESURRECT,
        SUBMIT,
        TRADE,
        OFFER_FAVOR,
        CALL_HELP,
        EXPLORE,
        WAIT,
        LEGACY,
        /** Desire-driven social goal (Art XXXI). NPC wants something from someone. */
        SOCIAL,
        OTHER
    }

    // ── Status ────────────────────────────────────────────────────────

    public enum Status {
        PENDING,
        ACTIVE,
        ACHIEVED,
        FAILED,
        ABANDONED
    }

    // ── Fields ────────────────────────────────────────────────────────

    public final Need sourceNeed;
    public final Category category;
    public final String description;
    public final double urgency;       // 0..1, time pressure
    public final double priority;      // 0..1, importance

    public Status status = Status.PENDING;

    public CognitionGoal(Need sourceNeed, Category category, String description,
                         double urgency, double priority) {
        this.sourceNeed = sourceNeed;
        this.category = category;
        this.description = description;
        this.urgency = clamp01(urgency);
        this.priority = clamp01(priority);
    }

    /** Effective weight = urgency × priority × Dao modifier. */
    public double effectiveWeight(double daoModifier) {
        return urgency * priority * daoModifier;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    @Override
    public String toString() {
        return "Goal[" + category + ": " + description + " u=" + urgency + " p=" + priority + "]";
    }
}
