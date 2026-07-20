package dev.ergenverse.simulation.cognition;

import java.util.List;

/**
 * DecisionEngine — the full cognition pipeline.
 *
 * <p>Pipeline (one decision tick):
 * <ol>
 *   <li>{@link GoalGenerator#generate} — needs → ranked goal list (Dao-weighted).</li>
 *   <li>Pick the highest-ranked goal.</li>
 *   <li>{@link Planner#plan} — goal → action options.</li>
 *   <li>{@link PersonalityModel#scoreOption} — score each option in context.</li>
 *   <li>Select the highest-scoring option as the winner.</li>
 * </ol>
 *
 * <p>The DecisionEngine returns a {@link Decision} record holding the chosen
 * goal, the chosen option, and the runner-up (for replay/audit).
 */
public final class DecisionEngine {

    private DecisionEngine() {}

    /** Result of a single decision tick. */
    public static final class Decision {
        public final CognitionGoal goal;
        public final ActionOption chosen;
        public final ActionOption runnerUp;
        public final double chosenScore;

        public Decision(CognitionGoal goal, ActionOption chosen, ActionOption runnerUp, double chosenScore) {
            this.goal = goal;
            this.chosen = chosen;
            this.runnerUp = runnerUp;
            this.chosenScore = chosenScore;
        }

        @Override
        public String toString() {
            return "Decision[goal=" + (goal == null ? "none" : goal.category)
                    + " action=" + (chosen == null ? "none" : chosen.label)
                    + " score=" + Math.round(chosenScore * 100) + "]";
        }
    }

    /**
     * Run one decision tick for an actor.
     *
     * @param dao             the actor's primary Dao identity
     * @param needIntensities the actor's current need intensities (0..1)
     * @param physical        the actor's physical state
     * @param cultivation     the actor's cultivation state
     * @param social          the actor's social state
     * @param personality     the actor's personality model
     * @param context         the current decision context string
     *                        (e.g. "confront_stranger", "sect_meeting", "wilderness")
     * @param desires         the actor's active desires (Art XXXI). May be null.
     *                        Produces SOCIAL goals in GoalGenerator.
     * @return the decision (goal + chosen action), or null if no goal passed threshold.
     */
    public static Decision decide(
            DaoIdentity dao,
            java.util.Map<Need, Double> needIntensities,
            PhysicalState physical,
            CultivationState cultivation,
            SocialState social,
            PersonalityModel personality,
            String context,
            java.util.List<DesireState> desires) {

        // (1) Generate goals (needs + desires)
        List<CognitionGoal> goals = GoalGenerator.generate(
                dao, needIntensities, physical, cultivation, social, desires);
        if (goals.isEmpty()) return new Decision(null, null, null, 0.0);

        // (2) Pick highest-ranked goal
        CognitionGoal topGoal = goals.get(0);

        // (3) Plan options
        List<ActionOption> options = Planner.plan(topGoal, physical, cultivation, social);
        if (options.isEmpty()) return new Decision(topGoal, null, null, 0.0);

        // (4) Score each option
        ActionOption winner = null;
        ActionOption runnerUp = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        double runnerUpScore = Double.NEGATIVE_INFINITY;
        for (ActionOption opt : options) {
            double s = personality.scoreOption(opt, context);
            if (s > bestScore) {
                runnerUp = winner;
                runnerUpScore = bestScore;
                winner = opt;
                bestScore = s;
            } else if (s > runnerUpScore) {
                runnerUp = opt;
                runnerUpScore = s;
            }
        }

        // (5) Return the decision
        return new Decision(topGoal, winner, runnerUp, bestScore);
    }
}
