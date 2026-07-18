package dev.ergenverse.wanglin.ai.reasoning;

/**
 * GiftingFactor — the six factors Wang Lin evaluates when the player requests
 * an item, per PROJECT_MASTER.md §4.4.
 *
 * <p>Each factor is a question Wang Lin asks himself before responding. The
 * factors are scored (0.0–1.0) by the {@link WangLinReasoningEngine} and
 * aggregated into a single {@code aggregateScore} that determines the
 * {@link GiftingOutcome}.
 *
 * <h2>The Six Factors</h2>
 * <ol>
 *   <li><b>NECESSITY</b> — Does the player actually need this right now?</li>
 *   <li><b>SAFETY</b> — Will giving this item get the player killed? (too much
 *       power too fast)</li>
 *   <li><b>USEFULNESS</b> — Can the player actually use this at their current
 *       level?</li>
 *   <li><b>UNIQUENESS</b> — Is this item irreplaceable? Does the true body
 *       only have one?</li>
 *   <li><b>CURRENT_NEED</b> — Does Wang Lin himself need this item right
 *       now?</li>
 *   <li><b>JUDGMENT</b> — Based on his Expectation Model of the player
 *       (see §6.13), is this the right time?</li>
 * </ol>
 *
 * <h2>Weights</h2>
 * <p>The default weight is 1.0 for all factors. {@link #JUDGMENT} carries a
 * weight of 1.5 because it integrates the Expectation Model — the holistic
 * read of the player's path — and should outweigh any single isolated factor.
 * This is a Simulation-layer design choice derived from §6.13's intent: "Wang
 * Lin acts as a mentor who reads the player's behavior and responds with
 * judgment, not a vending machine that dispenses items at affinity
 * thresholds."
 *
 * <h2>Hoarding correction (§6.13, §4.4)</h2>
 * <p>Hoarding behavior does NOT lower the JUDGMENT score. It updates Wang
 * Lin's prediction of the player's path (the "hoarding_path" prediction in
 * the Expectation Model), which in turn informs WHAT is most needed. A
 * hoarding player may need a breakthrough catalyst more than another
 * treasure — JUDGMENT scores that necessity higher, not the hoarding lower.
 * Hoarding is never punished; only genuine need is answered.
 */
public enum GiftingFactor {
    NECESSITY(
            "Necessity",
            "Does the player actually need this right now?",
            1.0
    ),
    SAFETY(
            "Safety",
            "Will giving this item get the player killed? (too much power too fast)",
            1.0
    ),
    USEFULNESS(
            "Usefulness",
            "Can the player actually use this at their current level?",
            1.0
    ),
    UNIQUENESS(
            "Uniqueness",
            "Is this item irreplaceable? Does the true body only have one?",
            1.0
    ),
    CURRENT_NEED(
            "Current Need",
            "Does Wang Lin himself need this item right now?",
            1.0
    ),
    /** The holistic factor — integrates the Expectation Model. Weighted 1.5×. */
    JUDGMENT(
            "Judgment",
            "Based on his Expectation Model of the player (§6.13), is this the right time?",
            1.5
    );

    /** Human-readable display name (e.g. "Necessity"). */
    public final String displayName;

    /** The question Wang Lin asks himself for this factor. */
    public final String description;

    /** Aggregation weight. Default 1.0; JUDGMENT is 1.5 (integrates Expectation Model). */
    public final double weight;

    GiftingFactor(String displayName, String description, double weight) {
        this.displayName = displayName;
        this.description = description;
        this.weight = weight;
    }

    /** The canonical evaluation order (matches §4.4's factor table). */
    public static GiftingFactor[] canonicalOrder() {
        return new GiftingFactor[] {
                NECESSITY, SAFETY, USEFULNESS, UNIQUENESS, CURRENT_NEED, JUDGMENT
        };
    }
}
