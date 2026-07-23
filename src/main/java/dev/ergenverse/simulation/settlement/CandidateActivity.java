package dev.ergenverse.simulation.settlement;

import java.util.Map;

/**
 * CandidateActivity — one possible activity an actor could choose, plus the
 * per-motivation scores that explain WHY it would be chosen.
 *
 * <p>Per the user's directive:
 * <blockquote>
 * Wang Lin: Observe +30 curiosity +40 concealment +20 cultivation opportunity
 * -10 danger = 80. Fight +30 remove danger -80 reveal strength = -50.
 * Highest score wins.
 * </blockquote>
 *
 * <p>A {@code CandidateActivity} is the scoring unit. The {@link CultivatorMind}
 * generates candidates appropriate to the {@link WorldSituation}, scores each
 * against the actor's motivation weights, and selects the highest. The score
 * breakdown is retained for audit — when the verifier asks "why did Wang Lin
 * observe?", the answer is the breakdown, not a hardcoded rule.
 *
 * <h2>Score semantics</h2>
 * <p>Each score is in roughly [-1.0, +1.0]:
 * <ul>
 *   <li>+1.0 — this activity strongly serves this motivation.</li>
 *   <li>+0.3 — this activity somewhat serves this motivation.</li>
 *   <li>0.0 — this activity is neutral to this motivation.</li>
 *   <li>-0.3 — this activity somewhat harms this motivation.</li>
 *   <li>-1.0 — this activity strongly harms this motivation.</li>
 * </ul>
 * The total score = Σ (motivation weight × score). The candidate with the
 * highest total wins.
 *
 * <p>This is the architectural shift the user demanded: <b>nobody ever wrote
 * "Wang Lin observes wolves."</b> Instead, the OBSERVE candidate scores highest
 * for Wang Lin because his CONCEAL_STRENGTH and CURIOSITY weights are high and
 * those motivations are strongly served by observing. A mortal — whose SURVIVAL
 * weight dominates and whose CONCEAL_STRENGTH is ~0 — scores FLEEING_HOME
 * highest. Same candidates, different weights, different winner. That is
 * emergence from the actor, not scripting by the engine.
 */
public final class CandidateActivity {

    /** The activity type this candidate represents. */
    public final Activity.Type type;

    /** Settlement-local X offset where this activity would place the actor. */
    public final int offsetX;

    /** Settlement-local Z offset where this activity would place the actor. */
    public final int offsetZ;

    /**
     * Per-motivation scores explaining how this activity serves each drive.
     * Keys are {@link Motivation}s; values are in [-1.0, +1.0]. Missing keys
     * default to 0.0 (neutral).
     */
    public final Map<Motivation, Float> scores;

    /** A canon-faithful one-line reason this candidate is being considered. */
    public final String reason;

    public CandidateActivity(Activity.Type type, int offsetX, int offsetZ,
                             Map<Motivation, Float> scores, String reason) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.scores = scores;
        this.reason = reason;
    }

    /**
     * Score this candidate against an actor's motivation weights.
     * @param mind the actor's mind (carries the motivation weights)
     * @return the total weighted score
     */
    public double scoreFor(CultivatorMind mind) {
        double total = 0.0;
        for (Map.Entry<Motivation, Float> e : scores.entrySet()) {
            total += mind.weightOf(e.getKey()) * e.getValue();
        }
        return total;
    }
}
