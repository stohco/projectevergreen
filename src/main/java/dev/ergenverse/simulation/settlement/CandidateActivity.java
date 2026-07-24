package dev.ergenverse.simulation.settlement;

import java.util.Collections;
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
 * <p>CRON-COMPLETIONIST-34: scoreFor() now accepts optional belief and memory
 * modifiers. These are temporary motivation weight adjustments computed by
 * the CultivatorMind from its BeliefRegistry and MemoryGraph. The modifiers
 * allow the same base motivation weights to produce DIFFERENT scores for the
 * same candidate when the actor's lived experience changes.
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
 * The total score = Σ ((base_weight + belief_mod + memory_mod) × score).
 * The candidate with the highest total wins.
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
     * Score this candidate against an actor's motivation weights (no modifiers).
     * Retained for backward compatibility.
     *
     * @param mind the actor's mind (carries the motivation weights)
     * @return the total weighted score
     */
    public double scoreFor(CultivatorMind mind) {
        return scoreFor(mind, Collections.emptyMap(), Collections.emptyMap());
    }

    /**
     * CRON-COMPLETIONIST-34: Score this candidate with belief and memory modifiers.
     *
     * <p>The effective weight for each motivation is:
     * {@code effective = mind.weightOf(m) + beliefModifier.getOrDefault(m, 0f) + memoryModifier.getOrDefault(m, 0f)}
     *
     * <p>This means beliefs about the current threat can temporarily amplify
     * or suppress certain motivations. For example, if the mind believes the
     * threat is "dangerous" (SURVIVAL +0.15), the FLEEING_HOME candidate's
     * SURVIVAL score (0.8) is multiplied by (base_survival + 0.15) instead of
     * just base_survival. This makes flee more attractive when the actor
     * has reason to fear.
     *
     * @param mind            the actor's mind (carries the motivation weights)
     * @param beliefModifiers temporary motivation weight adjustments from beliefs
     * @param memoryModifiers temporary motivation weight adjustments from memories
     * @return the total weighted score
     */
    public double scoreFor(CultivatorMind mind,
                           Map<Motivation, Float> beliefModifiers,
                           Map<Motivation, Float> memoryModifiers) {
        double total = 0.0;
        for (Map.Entry<Motivation, Float> e : scores.entrySet()) {
            float baseWeight = mind.weightOf(e.getKey());
            float beliefMod = beliefModifiers.getOrDefault(e.getKey(), 0.0f);
            float memoryMod = memoryModifiers.getOrDefault(e.getKey(), 0.0f);
            // Effective weight clamped to [0, 1] — motivation can't go negative.
            float effectiveWeight = Math.max(0f, Math.min(1f, baseWeight + beliefMod + memoryMod));
            total += effectiveWeight * e.getValue();
        }
        return total;
    }
}
