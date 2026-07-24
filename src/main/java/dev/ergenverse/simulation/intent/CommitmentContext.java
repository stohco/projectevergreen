package dev.ergenverse.simulation.intent;

import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;
import dev.ergenverse.simulation.settlement.WorldSituation;

/**
 * CommitmentContext — the world state a {@link Commitment}'s completion
 * and abandon predicates evaluate against.
 *
 * <p>Per the user's design review (the cycle after CRON-COMPLETIONIST-11):
 * <blockquote>
 * Commitments should not simply expire. They should also be completed
 * by conditions. Instead of "Observe wolves — 12000 ticks — Done" think
 * "Observe wolves — Until: ✓ understand hunting pattern OR ✓ danger
 * exceeds tolerance OR ✓ family needs intervention OR ✓ prey escapes."
 * The world should decide when a commitment ends. Not a timer. The timer
 * is merely insurance against bugs.
 * </blockquote>
 *
 * <p>The context is intentionally minimal — current tick, the actor, the
 * shared world situation (if available), and the actor's last filtered
 * perception. Predicates read what they need and ignore the rest. This
 * keeps the predicate API stable as the world grows richer: a new
 * "danger level" field on the situation doesn't break old predicates,
 * it lets new ones be written.
 *
 * <p><b>Nullability:</b> every field except {@code currentTick} may be
 * null. Predicates MUST null-check before dereferencing. A predicate
 * that cannot evaluate (because its required input is null) should
 * return {@code false} — the commitment continues. This is the safe
 * default: a missing world state should never end a commitment.
 */
public record CommitmentContext(
        long currentTick,
        Actor actor,
        WorldSituation situation,
        PerceptionSnapshot perception
) {
    /**
     * A minimal context carrying only the tick. Used for safety-net
     * timer checks when no richer world state is available (e.g. during
     * unit tests or before the situation has been computed for this
     * scan). Predicates that need more than the tick will return false.
     */
    public static CommitmentContext minimal(long tick) {
        return new CommitmentContext(tick, null, null, null);
    }
}
