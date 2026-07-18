package dev.ergenverse.simulation.opportunity;

/**
 * OpportunityAcquisitionPolicy — the non-negotiable rule governing opportunity acquisition.
 *
 * <p>This is the single rule that makes the entire Opportunity Classification system
 * canon-faithful. It is enforced at every acquisition check.
 *
 * <h2>The Policy (user's exact words)</h2>
 * <blockquote>
 *   <b>Every canonical opportunity must retain its original history.</b><br><br>
 *   The player may obtain:
 *   <ul>
 *     <li>the original,</li>
 *     <li>a teaching,</li>
 *     <li>a successor,</li>
 *     <li>a replica,</li>
 *     <li>a parallel opportunity,</li>
 *     <li>a manifestation-sharing event,</li>
 *   </ul>
 *   depending on the nature of the opportunity.<br><br>
 *   <b>The simulation may never rewrite canonical ownership.</b>
 * </blockquote>
 *
 * <h2>What "never rewrite canonical ownership" means</h2>
 * <ul>
 *   <li>Wang Lin's Heaven-Defying Bead remains Wang Lin's. The player never
 *       "takes" it from him. If the player obtains a derivative, it is clearly
 *       a NEW object with its own history (recorded in Layer 3).</li>
 *   <li>If a canon treasure was destroyed (e.g. God-Slaying Sword, Ch. 1273),
 *       the simulation does not "respawn" the destroyed original. The player
 *       may obtain a successor or a parallel opportunity, but the original's
 *       destruction remains historical truth.</li>
 *   <li>If a canon inheritance was consumed (e.g. Tu Si's Ancient God inheritance),
 *       the simulation does not "reset" it. The player may discover a PARALLEL
 *       inheritance (Category 4), but the consumed original is gone — that is
 *       canon, immutable.</li>
 * </ul>
 *
 * <h2>Enforcement</h2>
 * <p>The {@link OpportunityResolver} calls {@link #validateAcquisition} before
 * any acquisition is granted. If the validation fails, the acquisition is refused
 * and a canon-violation warning is logged. This is a hard gate — there is no
 * override, no admin bypass, no creative-mode skip. The policy is non-negotiable.
 */
public final class OpportunityAcquisitionPolicy {

    private OpportunityAcquisitionPolicy() {}

    /** The policy statement, enshrined in code. */
    public static final String POLICY_STATEMENT =
        "Every canonical opportunity must retain its original history. " +
        "The player may obtain: the original, a teaching, a successor, a replica, " +
        "a parallel opportunity, or a manifestation-sharing event — depending on " +
        "the nature of the opportunity. The simulation may never rewrite canonical ownership.";

    /**
     * Validate that a proposed acquisition does NOT violate the policy.
     *
     * @param classification the opportunity's classification
     * @param playerObtainsWhat what the player would obtain ("original", "taught_copy",
     *                          "successor_artifact", "parallel_opportunity",
     *                          "derivative_understanding", "replica")
     * @return true if the acquisition is policy-compliant, false if it violates canon
     */
    public static boolean validateAcquisition(OpportunityClassification classification,
                                               String playerObtainsWhat) {
        if (classification == null || playerObtainsWhat == null) return false;

        OpportunityCategory category = classification.category();
        String what = playerObtainsWhat.toLowerCase();

        // ABSOLUTE_UNIQUE: the player can NEVER obtain the original.
        // Only derivatives ("derivative_understanding", "successor_artifact") are allowed.
        if (category == OpportunityCategory.ABSOLUTE_UNIQUE) {
            return what.contains("derivative")
                || what.contains("successor")
                || what.contains("understanding");
        }

        // SUCCESSOR: the player obtains a continuation, NOT the original.
        // "original" is forbidden; "successor_artifact" is correct.
        if (category == OpportunityCategory.SUCCESSOR) {
            return !what.equals("original");
        }

        // PARALLEL: the player obtains a NEW opportunity, NOT the canon one.
        // "original" is forbidden; "parallel_opportunity" is correct.
        if (category == OpportunityCategory.PARALLEL) {
            return !what.equals("original");
        }

        // TRANSFERABLE, REPLICABLE, RELATIONSHIP_EXCLUSIVE: the player can obtain
        // the original (or a copy/teaching of it), since the protagonist retains theirs.
        // All acquisition types are allowed.
        return true;
    }

    /**
     * Check whether the policy allows the player to obtain the ORIGINAL of a
     * specific opportunity. This is the strictest check.
     */
    public static boolean canObtainOriginal(OpportunityClassification classification) {
        if (classification == null) return false;
        return validateAcquisition(classification, "original");
    }

    /**
     * The rule, as a one-line assertion for logging.
     */
    public static String assertionLine() {
        return "[OpportunityAcquisitionPolicy] The simulation may never rewrite canonical ownership.";
    }
}
