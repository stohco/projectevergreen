package dev.ergenverse.simulation.intent;

/**
 * CompletionPredicate — a functional predicate evaluated against a
 * {@link CommitmentContext} to decide whether a commitment's success
 * or abandon condition is met.
 *
 * <p>Predicates are the <b>world's voice</b> in commitment lifecycle. A
 * commitment ends when the WORLD says it ends (the threat is gone, the
 * herb is harvested, the family is safe, the prey escaped, danger
 * exceeded tolerance), not when a timer fires.
 *
 * <p>Per the user's design review:
 * <blockquote>
 * The world should decide when a commitment ends. Not a timer. The
 * timer is merely insurance against bugs.
 * </blockquote>
 *
 * <p>A single {@link Commitment} carries two predicate lists:
 * <ul>
 *   <li><b>successConditions</b> — if any returns true, the commitment
 *       is {@link Commitment.Status#COMPLETED}. The actor achieved what
 *       it committed to. (e.g. "I observed the wolves long enough to
 *       understand their hunting pattern.")</li>
 *   <li><b>abandonConditions</b> — if any returns true, the commitment
 *       is {@link Commitment.Status#ABANDONED}. The world changed in a
 *       way that makes continuing pointless or unsafe. (e.g. "danger
 *       exceeded my tolerance," "family needs intervention," "the prey
 *       escaped.")</li>
 * </ul>
 *
 * <p>The distinction between COMPLETED and ABANDONED matters for memory:
 * a completed commitment becomes a positive memory ("I learned the
 * wolves' pattern"), an abandoned one becomes a negative or neutral
 * memory ("I had to give up — family needed me"). This feeds back into
 * the {@link dev.ergenverse.simulation.cognition.MemoryGraph} and
 * shapes future decisions.
 *
 * <p><b>Null-safety contract:</b> predicates MUST null-check any field
 * of {@link CommitmentContext} before dereferencing. If a predicate's
 * required input is null (e.g. the situation hasn't been computed), it
 * MUST return {@code false} — the commitment continues. A missing world
 * state should never silently end a commitment.
 */
@FunctionalInterface
public interface CompletionPredicate {
    /**
     * @param ctx the current world state (fields may be null)
     * @return true if this condition is met (commitment should end)
     */
    boolean test(CommitmentContext ctx);
}
