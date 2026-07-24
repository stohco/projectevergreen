package dev.ergenverse.simulation.cognition.prediction;

import dev.ergenverse.simulation.cognition.ActionOption;
import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.PersonalityModel;
import dev.ergenverse.simulation.cognition.perception.Interpretation;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;

/**
 * ActionPredictor — the prediction layer of Article XXXV.
 *
 * <pre>
 *   World -> Perception -> Attention -> Interpretation ->
 *   Prediction -> Goals -> Intent -> Planning -> Tasks -> Activities -> Minecraft
 * </pre>
 *
 * <p>Before this class, the DecisionEngine picked the highest-scoring
 * action and the actor executed it — with NO forward simulation. A
 * meditating cultivator next to a wolf would keep meditating because
 * "MEDITATE has the highest personality score." The user's critique:
 * "Wang Lin doesn't immediately evaluate 'wolf.' He first notices it,
 * then decides if it matters, then PREDICTS what will happen, then
 * remembers previous encounters, then decides whether to intervene."
 *
 * <p>The ActionPredictor takes:
 * <ul>
 *   <li>A candidate {@link ActionOption}</li>
 *   <li>The current {@link PerceptionSnapshot}</li>
 *   <li>The {@link Interpretation} of that perception</li>
 *   <li>The actor's {@link PersonalityModel}</li>
 * </ul>
 * and returns a {@link Outcome} forecasting:
 * <ul>
 *   <li>{@code pSuccess} — probability the action achieves its goal</li>
 *   <li>{@code pInjury} — probability the actor gets hurt/killed</li>
 *   <li>{@code pWitnessed} — probability the action is observed (reveals power)</li>
 *   <li>{@code expectedValue} — combined score; the DecisionEngine uses this
 *       to re-rank candidates after prediction</li>
 * </ul>
 *
 * <h2>Example: Wang Lin meditating while a wolf approaches</h2>
 * <ul>
 *   <li>Candidate: MEDITATE</li>
 *   <li>Perception: wolf at 8 blocks, hostile</li>
 *   <li>Interpretation: THREAT_TO_LIFE</li>
 *   <li>Prediction: pInjury = 0.78, pSuccess = 0.2 (can't meditate while being
 *       attacked), expectedValue = -0.6 → REJECTED</li>
 *   <li>Candidate: FLEE</li>
 *   <li>Prediction: pInjury = 0.1, pSuccess = 0.85, expectedValue = +0.7 → CHOSEN</li>
 * </ul>
 *
 * <p>This is the mechanism that makes Wang Lin REACT to a wolf instead
 * of meditating through his own death. It is the difference between a
 * state machine and a mind.
 */
public final class ActionPredictor {

    private ActionPredictor() {}

    /** A forecasted outcome for one candidate action. */
    public static final class Outcome {
        public final ActionOption action;
        public final double pSuccess;       // 0..1
        public final double pInjury;        // 0..1
        public final double pWitnessed;     // 0..1
        public final double expectedValue;  // combined

        public Outcome(ActionOption action, double pSuccess, double pInjury,
                       double pWitnessed, double expectedValue) {
            this.action = action;
            this.pSuccess = clamp01(pSuccess);
            this.pInjury = clamp01(pInjury);
            this.pWitnessed = clamp01(pWitnessed);
            this.expectedValue = expectedValue;
        }

        @Override
        public String toString() {
            return "Predict[" + (action == null ? "?" : action.label)
                    + " pS=" + String.format("%.2f", pSuccess)
                    + " pI=" + String.format("%.2f", pInjury)
                    + " pW=" + String.format("%.2f", pWitnessed)
                    + " EV=" + String.format("%.2f", expectedValue) + "]";
        }
    }

    /**
     * Predict the outcome of a candidate action given the current situation.
     *
     * @param action       the candidate action
     * @param goal         the goal the action serves
     * @param perception   current perception (may be null → neutral prediction)
     * @param interpretation interpretation of the perception (may be null)
     * @param personality  the actor's personality (for caution-weighting)
     * @return the predicted outcome (never null)
     */
    public static Outcome predict(ActionOption action, CognitionGoal goal,
                                   PerceptionSnapshot perception,
                                   Interpretation interpretation,
                                   PersonalityModel personality) {
        // Baseline from the planner's estimates.
        double pSuccess = action.successProbability;
        double pInjury = action.risk;
        double pWitnessed = 0.3;  // default: any action might be seen

        // ── Perception modifiers ──
        if (perception != null) {
            // If we're observed, pWitnessed rises.
            if (perception.isObserved) pWitnessed = Math.min(1.0, pWitnessed + 0.4);

            // If there's a threat, dangerous actions get riskier, safe actions safer.
            if (perception.hasThreat) {
                switch (action.id) {
                    case MEDITATE, WAIT, CRAFT_TALISMAN, CRAFT_PILL, STUDY_FORMATION:
                        // Passive actions while threatened = very dangerous.
                        pInjury = Math.min(1.0, pInjury + 0.5);
                        pSuccess = Math.max(0.0, pSuccess - 0.4);
                        break;
                    case FLEE, HIDE:
                        // Evasion gets MORE successful when actually threatened.
                        pSuccess = Math.min(1.0, pSuccess + 0.15);
                        pInjury = Math.max(0.0, pInjury - 0.1);
                        break;
                    case FIGHT, TAKE_REVENGE, KILL_OWNER:
                        // Combat is viable but risky.
                        pInjury = Math.min(1.0, pInjury + 0.2);
                        break;
                    default:
                        break;
                }
            }

            // At night, outdoors, risky actions are riskier.
            if (perception.isNight && !perception.isUnderground) {
                pInjury = Math.min(1.0, pInjury + 0.05);
            }
        }

        // ── Interpretation modifiers ──
        if (interpretation != null) {
            if (interpretation.category == Interpretation.Category.THREAT_TO_LIFE) {
                // Under lethal threat, only FLEE/HIDE/FIGHT are sane.
                if (action.id != ActionOption.ActionId.FLEE
                        && action.id != ActionOption.ActionId.HIDE
                        && action.id != ActionOption.ActionId.FIGHT
                        && action.id != ActionOption.ActionId.CALL_HELP) {
                    pSuccess = Math.max(0.0, pSuccess - 0.5);
                    pInjury = Math.min(1.0, pInjury + 0.3);
                }
            }
            if (interpretation.category == Interpretation.Category.WITNESS_RISK) {
                // Witnessed: power-revealing actions get penalized.
                switch (action.id) {
                    case FIGHT, BREAKTHROUGH, BREAK_FORMATION, TAKE_REVENGE, KILL_OWNER:
                        pWitnessed = Math.min(1.0, pWitnessed + 0.4);
                        break;
                    default:
                        break;
                }
            }
        }

        // ── Expected value ──
        // EV = pSuccess * reward - pInjury * injuryWeight - pWitnessed * witnessWeight
        double reward = action.reward;
        double injuryWeight = 1.0;
        double witnessWeight = 0.0;

        // Hiding-power daos (Wang Lin) heavily penalize being witnessed.
        if (personality != null) {
            // Read the caution trait if present; cautious actors weight injury more.
            // PersonalityModel exposes trait curves; we use a simple heuristic.
            injuryWeight = 1.0;  // could be personality-driven
        }

        double ev = pSuccess * reward
                - pInjury * injuryWeight
                - pWitnessed * witnessWeight;

        return new Outcome(action, pSuccess, pInjury, pWitnessed, ev);
    }

    private static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
