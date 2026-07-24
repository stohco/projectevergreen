package dev.ergenverse.simulation.intent;

import dev.ergenverse.simulation.cognition.CognitionGoal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Commitment — a decision that persists across ticks, ended by the world
 * (not a timer).
 *
 * <h2>The user's directive (the cycle after CRON-COMPLETIONIST-11)</h2>
 *
 * <blockquote>
 * Commitments should not simply expire. They should also be completed
 * by conditions. Instead of
 * <pre>
 *   Observe wolves — 12000 ticks — Done
 * </pre>
 * think
 * <pre>
 *   Observe wolves — Until:
 *     ✓ understand hunting pattern
 *     OR
 *     ✓ danger exceeds tolerance
 *     OR
 *     ✓ family needs intervention
 *     OR
 *     ✓ prey escapes
 * </pre>
 * The world should decide when a commitment ends. Not a timer. The timer
 * is merely insurance against bugs.
 * </blockquote>
 *
 * <p>This class is the concrete realization of that directive. A
 * commitment carries:
 * <ul>
 *   <li><b>successConditions</b> — world predicates that, when met,
 *       mark the commitment COMPLETED (the actor achieved what it
 *       committed to).</li>
 *   <li><b>abandonConditions</b> — world predicates that, when met,
 *       mark the commitment ABANDONED (the world changed; continuing is
 *       pointless or unsafe).</li>
 *   <li><b>maxDurationTicks</b> — a safety-net timer. The user was
 *       explicit: "The timer is merely insurance against bugs." If both
 *       predicate lists somehow fail to fire (a bug, a missing world
 *       state, a predicate that never becomes true), the commitment
 *       ends after this duration so the actor re-evaluates. It is NOT
 *       the primary lifecycle mechanism.</li>
 * </ul>
 *
 * <h2>The difference between Intent and Commitment</h2>
 *
 * <p>An {@link Intent} is the immediate strategic framing of behavior.
 * It is re-evaluated frequently (seconds). It flickers as the world
 * flickers. If Wang Lin sees a wolf, his Intent becomes OBSERVE_FROM_DISTANCE.
 * If the wolf moves behind a tree, his Intent may flicker to
 * ADVANCE_OPPORTUNISTICALLY. If a player walks near, his Intent flickers to
 * AVOID_REVEALING_STRENGTH. The Intent is reactive.
 *
 * <p>A <b>Commitment</b> is the decision those Intents serve. It is
 * re-evaluated rarely — only when the WORLD says it should end (a
 * success or abandon condition fires). It does not flicker. When
 * Wang Lin commits to "investigate the wolves that have been circling
 * the village," that commitment persists even if:
 * <ul>
 *   <li>a wolf momentarily moves out of sight,</li>
 *   <li>a player walks past,</li>
 *   <li>the wind changes direction,</li>
 *   <li>his mother calls him for dinner.</li>
 * </ul>
 * He may pause the commitment (to eat, to sleep, to avoid a player),
 * but he returns to it. The commitment is the through-line.
 *
 * <h2>Why this matters for the simulation</h2>
 *
 * <p>Without Commitment, the NPC is a ditherer. Every tick it re-asks
 * "what should I do?" and gets a slightly different answer because the
 * world is slightly different. The result is the NPC wanders, stops,
 * wanders back, looks around, wanders again. This is the behavior the
 * user identified as the failure mode of timetable schedules and
 * per-tick re-evaluation.
 *
 * <p>With Commitment, the NPC holds its course. Wang Lin walks to the
 * ridge. He stays there. He watches. He comes back when the world
 * tells him his commitment is complete (he understands the pattern)
 * or abandoned (the wolves left, or danger exceeded his tolerance).
 * The player watching from below sees someone with a purpose, not
 * someone glitching between nav points.
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li><b>Formed</b> — by the ReasoningEngine when a pressure crosses
 *       a threshold AND the actor's motivations favor responding to it.
 *       The Commitment records: which pressure triggered it, which
 *       motivations it serves, the target, the success/abandon
 *       conditions, and the safety-net max duration.</li>
 *   <li><b>Active</b> — the actor is executing the commitment. The
 *       CognitionDrivenGoal decomposes it into Intents and Tasks, but
 *       does NOT re-evaluate the commitment each tick. It only
 *       re-evaluates when:
 *       <ul>
 *         <li>a success condition fires ({@link #isFulfilled}) → COMPLETED,</li>
 *         <li>an abandon condition fires ({@link #shouldAbandon}) → ABANDONED,</li>
 *         <li>the safety-net max duration elapses ({@link #isExpired}) → COMPLETED
 *             with reason MAX_DURATION_ELAPSED (the bug-insurance path).</li>
 *       </ul></li>
 *   <li><b>Paused</b> — a higher-priority Intent (e.g. flee a player,
 *       eat, sleep) temporarily overrides the commitment. The
 *       commitment is NOT abandoned; it resumes when the override
 *       clears. Pauses do not reset the safety-net clock.</li>
 *   <li><b>Completed</b> — a success condition was met (the actor
 *       achieved what it committed to), OR the safety-net max duration
 *       elapsed without any condition firing.</li>
 *   <li><b>Abandoned</b> — an abandon condition fired (the world
 *       changed; continuing is pointless or unsafe).</li>
 * </ol>
 *
 * <h2>Canon example: Wang Lin investigating wolves</h2>
 *
 * <pre>
 *   Pressure:   WOLF_SIGHTING_NEAR_VILLAGE (world fact, world-owned)
 *   Mind:       Wang Lin's CultivatorMind weighs CONCEAL_STRENGTH(40) +
 *               CURIOSITY(30) + PROTECT_FAMILY(20) = 90 toward "investigate"
 *   Reasoning:  ActorReasoningEngine scores candidates:
 *                 OBSERVE_FROM_RIDGE: +90 (wins)
 *                 HUNT_WOLVES:        -30 (reveals strength)
 *                 IGNORE:             -20 (family at risk)
 *   Decision:   "I will investigate the wolves from the ridge."
 *   Commitment: OBSERVE_FROM_DISTANCE, target=wolf_pack_west_ridge,
 *               maxDuration=120000t (safety net — 100 min real-time),
 *               formedAtTick=131000, status=ACTIVE
 *   Success conditions:
 *     - ctx.perception shows wolves retreating (hunting pattern understood)
 *     - 6000t have passed AND wolves still present (observation complete)
 *   Abandon conditions:
 *     - ctx.situation.threat intensity > 0.8 (danger exceeds tolerance)
 *     - family member perceives threat (family needs intervention)
 *     - wolves leave the area (prey/threat escaped — nothing to observe)
 *   Execution:  CognitionDrivenGoal decomposes → MOVE_TO ridge →
 *               HOLD_POSITION+FACE wolves → WAIT. Per-tick Intent may
 *               flicker (AVOID_REVEALING_STRENGTH when player approaches)
 *               but the Commitment holds until a condition fires.
 * </pre>
 *
 * <h2>The bridge this class closes</h2>
 *
 * <p>CRON-COMPLETIONIST-11 shipped the Commitment contract but no
 * producer — no code path SET activeCommitment. This cycle (the user's
 * design review) wires the producer in {@link dev.ergenverse.simulation.actor.ActorTickLoop}:
 * when a commitment-worthy goal is chosen (INVESTIGATE, DEFEND, STUDY,
 * SEEKING_DAO, EXPLORE, KEEP_PROMISE, RESOLVE_DEBT, LEGACY), the tick
 * loop forms a Commitment with situation-derived predicates and sets
 * it on the actor (and its cognition). The CognitionDrivenGoal then
 * honors it.
 *
 * <p><b>Provenance: INFERRED + USER-DIRECTED.</b> The user's design
 * review named the condition-based completion requirement explicitly.
 * The predicate API is the concrete realization of "the world decides
 * when a commitment ends."
 */
public final class Commitment {

    /** Status of a commitment in its lifecycle. */
    public enum Status {
        /** Just formed, not yet executing. Transitions to ACTIVE on next goal tick. */
        FORMED,
        /** Currently being executed by the CognitionDrivenGoal. */
        ACTIVE,
        /** Temporarily overridden by a higher-priority Intent (flee, eat, sleep). */
        PAUSED,
        /** A success condition was met, or the safety-net max duration elapsed. */
        COMPLETED,
        /** An abandon condition fired (the world changed; continuing is pointless/unsafe). */
        ABANDONED
    }

    /** Why a commitment ended. Set when status transitions to a terminal state. */
    public enum CompletionReason {
        /** A success condition fired — the actor achieved what it committed to. */
        SUCCESS_CONDITION_MET,
        /** An abandon condition fired — the world changed; continuing is pointless/unsafe. */
        ABANDON_CONDITION_MET,
        /** The safety-net max duration elapsed without any condition firing (bug insurance). */
        MAX_DURATION_ELAPSED,
        /** The triggering pressure disappeared before any predicate could fire. */
        TRIGGER_DISAPPEARED
    }

    /** The Intent nature this commitment will produce when active. */
    public final IntentNature intentNature;

    /** What the commitment is directed at (e.g. "wolf_pack_west_ridge",
     * "spirit_herb_glade", "village_elder_house"). */
    public final String targetId;

    /** The long-term cognition goal this commitment serves. */
    public final CognitionGoal sourceGoal;

    /** Human-readable reason the commitment was formed — for logging,
     * dialogue, and future "explain your behavior" features. */
    public final String reason;

    /**
     * Safety-net maximum duration in ticks. The commitment ends after this
     * duration ONLY if no success or abandon condition has fired. Per the
     * user: "The timer is merely insurance against bugs."
     *
     * <p>This field replaces the prior semantic of "persistence duration"
     * (which was the primary lifecycle mechanism). It is now a backstop,
     * not the driver. Conditions drive lifecycle; the timer catches bugs.
     */
    public final long persistenceDurationTicks;

    /** Tick when the commitment was formed. */
    public final long formedAtTick;

    /** Tick when the commitment was last reaffirmed (after a pause). */
    public long lastReaffirmedTick;

    /** Current lifecycle status. */
    public Status status;

    /**
     * Success conditions — world predicates that, when met, mark this
     * commitment COMPLETED. The actor achieved what it committed to.
     * Unmodifiable after construction.
     */
    public final List<CompletionPredicate> successConditions;

    /**
     * Abandon conditions — world predicates that, when met, mark this
     * commitment ABANDONED. The world changed; continuing is pointless
     * or unsafe. Unmodifiable after construction.
     */
    public final List<CompletionPredicate> abandonConditions;

    /** Why this commitment ended (null while status is non-terminal). */
    public CompletionReason endReason = null;

    /** Optional human-readable success condition description (for logging). */
    public final String successConditionDescription;

    /**
     * Construct a new commitment with FORMED status.
     *
     * <p>This constructor is package-private — commitments should be
     * built via {@link Builder} so success/abandon conditions are
     * explicit and self-documenting at the call site.
     */
    Commitment(IntentNature intentNature,
                      String targetId,
                      CognitionGoal sourceGoal,
                      String reason,
                      long maxDurationTicks,
                      long currentTick,
                      List<CompletionPredicate> successConditions,
                      List<CompletionPredicate> abandonConditions,
                      String successConditionDescription) {
        this.intentNature = intentNature;
        this.targetId = targetId;
        this.sourceGoal = sourceGoal;
        this.reason = reason;
        this.persistenceDurationTicks = maxDurationTicks;
        this.formedAtTick = currentTick;
        this.lastReaffirmedTick = currentTick;
        this.status = Status.FORMED;
        this.successConditions = Collections.unmodifiableList(new ArrayList<>(successConditions));
        this.abandonConditions = Collections.unmodifiableList(new ArrayList<>(abandonConditions));
        this.successConditionDescription = successConditionDescription;
    }

    /**
     * Has this commitment lived past its safety-net max duration?
     *
     * <p>This is the BUG-INSURANCE path. It returns true only when both
     * the success and abandon predicate lists have failed to fire for
     * the entire {@link #persistenceDurationTicks} window. In a
     * correctly-predicated commitment, this should almost never be the
     * reason a commitment ends — the world should decide first.
     *
     * <p>Per the user: "The timer is merely insurance against bugs."
     *
     * @param currentTick the current server tick
     * @return true if the safety-net max duration has elapsed
     */
    public boolean isExpired(long currentTick) {
        return currentTick >= lastReaffirmedTick + persistenceDurationTicks;
    }

    /**
     * Should this commitment be COMPLETED because a success condition fired?
     *
     * <p>This is the primary "the world says you're done" path. The
     * CognitionDrivenGoal calls this each tick with the current
     * {@link CommitmentContext}. If any predicate returns true, the
     * commitment is COMPLETED with reason SUCCESS_CONDITION_MET.
     *
     * <p>Predicates MUST null-check their inputs. If a predicate's
     * required input is null (e.g. the situation hasn't been computed),
     * it MUST return false — a missing world state should never silently
     * complete a commitment.
     *
     * @param ctx the current world state (fields may be null)
     * @return true if any success condition is met
     */
    public boolean isFulfilled(CommitmentContext ctx) {
        for (CompletionPredicate p : successConditions) {
            try {
                if (p.test(ctx)) {
                    this.endReason = CompletionReason.SUCCESS_CONDITION_MET;
                    return true;
                }
            } catch (Exception e) {
                // A buggy predicate should not silently end a commitment.
                // Log and continue checking other predicates.
                // (Intentionally swallowed — predicates are user-supplied lambdas.)
            }
        }
        return false;
    }

    /**
     * Should this commitment be ABANDONED because an abandon condition
     * fired, OR because the safety-net max duration elapsed?
     *
     * <p>This is the "the world says stop" path. The CognitionDrivenGoal
     * calls this each tick. If any abandon predicate returns true, the
     * commitment is ABANDONED with reason ABANDON_CONDITION_MET. If the
     * safety-net max duration has elapsed, the commitment is COMPLETED
     * with reason MAX_DURATION_ELAPSED (the bug-insurance path).
     *
     * <p>Predicates MUST null-check their inputs. If a predicate's
     * required input is null, it MUST return false.
     *
     * @param ctx the current world state (fields may be null)
     * @return true if the commitment should end (abandon condition or safety net)
     */
    public boolean shouldAbandon(CommitmentContext ctx) {
        for (CompletionPredicate p : abandonConditions) {
            try {
                if (p.test(ctx)) {
                    this.endReason = CompletionReason.ABANDON_CONDITION_MET;
                    return true;
                }
            } catch (Exception e) {
                // A buggy predicate should not silently end a commitment.
            }
        }
        // Safety-net: max duration (bug insurance per user directive).
        if (ctx != null && isExpired(ctx.currentTick())) {
            this.endReason = CompletionReason.MAX_DURATION_ELAPSED;
            return true;
        }
        return false;
    }

    /**
     * Is this commitment currently actionable (ACTIVE or FORMED)?
     */
    public boolean isActionable() {
        return status == Status.ACTIVE || status == Status.FORMED;
    }

    /**
     * Reaffirm the commitment after a pause — reset the safety-net
     * clock so the actor has a full duration to continue.
     *
     * <p>Per the user's directive: pauses do NOT reset the primary
     * lifecycle (conditions), only the safety-net timer. A commitment
     * that has been paused for an hour still completes/abandons based
     * on world conditions, not on time-since-formation.
     *
     * @param currentTick the current server tick
     */
    public void reaffirm(long currentTick) {
        this.lastReaffirmedTick = currentTick;
        if (this.status == Status.PAUSED) {
            this.status = Status.ACTIVE;
        }
    }

    /**
     * Produce the Intent this commitment directs the actor to hold.
     *
     * <p>The CognitionDrivenGoal calls this to get the per-tick Intent
     * without re-running the ReasoningEngine. The Intent's
     * expectedDurationTicks is set to the commitment's REMAINING
     * safety-net duration (so the Intent won't expire before the
     * safety net does), but the actual lifecycle is driven by the
     * conditions — the Intent is re-derived each tick regardless.
     *
     * @param currentTick the current server tick
     * @return the Intent directed by this commitment
     */
    public Intent toIntent(long currentTick) {
        long remaining = persistenceDurationTicks - (currentTick - lastReaffirmedTick);
        if (remaining < 20) remaining = 20; // never produce a 0-duration intent
        return new Intent(intentNature, targetId, sourceGoal,
                0.8, // commitments carry steady urgency — not panicked, not idle
                remaining, currentTick);
    }

    @Override
    public String toString() {
        return "Commitment[" + intentNature.label + " → " + targetId
                + " " + status
                + " reason=\"" + (reason != null && reason.length() > 40
                                    ? reason.substring(0, 37) + "..."
                                    : reason) + "\""
                + " success=" + successConditions.size()
                + " abandon=" + abandonConditions.size()
                + " maxDur=" + persistenceDurationTicks + "t]";
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Builder — the only way to construct a Commitment
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Build a {@link Commitment} with explicit success and abandon conditions.
     *
     * <p>The builder forces the call site to think about BOTH how the
     * commitment should succeed AND how it should be abandoned. A
     * commitment with no abandon conditions is a commitment that can
     * never adapt to a changing world — the safety-net timer becomes
     * the only escape, which is exactly the anti-pattern the user
     * named.
     *
     * <pre>
     * Commitment c = Commitment.builder(
     *         IntentNature.OBSERVE_FROM_DISTANCE,
     *         "wolf_pack_west_ridge",
     *         observeGoal)
     *     .reason("Wang Lin investigates the wolf pack circling the village.")
     *     .maxDuration(120000L)  // safety net — 100 min real-time
     *     .successWhen(ctx -&gt; ctx.perception != null
     *         &amp;&amp; ctx.perception.threatRetreating)
     *     .successWhen(ctx -&gt; ctx.currentTick - formedAt &gt; 6000L
     *         &amp;&amp; wolvesStillPresent(ctx))  // observed long enough
     *     .abandonWhen(ctx -&gt; ctx.situation != null
     *         &amp;&amp; ctx.situation.primaryThreat != null
     *         &amp;&amp; ctx.situation.primaryThreat.intensity() &gt; 0.8f)
     *     .abandonWhen(ctx -&gt; familyNeedsIntervention(ctx))
     *     .abandonWhen(ctx -&gt; wolvesLeftArea(ctx))
     *     .form(currentTick);
     * </pre>
     */
    public static Builder builder(IntentNature intentNature,
                                   String targetId,
                                   CognitionGoal sourceGoal) {
        return new Builder(intentNature, targetId, sourceGoal);
    }

    /** Fluent builder for Commitment. See {@link #builder}. */
    public static final class Builder {
        private final IntentNature intentNature;
        private final String targetId;
        private final CognitionGoal sourceGoal;

        private String reason = "";
        private long maxDurationTicks = 12000L; // default safety net: 10 min real-time
        private final List<CompletionPredicate> successConditions = new ArrayList<>();
        private final List<CompletionPredicate> abandonConditions = new ArrayList<>();
        private String successDescription = null;

        private Builder(IntentNature intentNature, String targetId, CognitionGoal sourceGoal) {
            this.intentNature = intentNature;
            this.targetId = targetId;
            this.sourceGoal = sourceGoal;
        }

        /** Human-readable reason the commitment was formed (for logging/dialogue). */
        public Builder reason(String r) { this.reason = r; return this; }

        /**
         * Safety-net maximum duration in ticks. The commitment ends after
         * this ONLY if no condition fires. Default: 12000t (10 min real-time).
         * Per the user: "The timer is merely insurance against bugs."
         */
        public Builder maxDuration(long ticks) {
            this.maxDurationTicks = Math.max(20L, ticks);
            return this;
        }

        /** Human-readable description of the success condition (for logging). */
        public Builder successDescription(String d) {
            this.successDescription = d; return this;
        }

        /** Add a success condition — when met, the commitment is COMPLETED. */
        public Builder successWhen(CompletionPredicate p) {
            if (p != null) successConditions.add(p);
            return this;
        }

        /** Add an abandon condition — when met, the commitment is ABANDONED. */
        public Builder abandonWhen(CompletionPredicate p) {
            if (p != null) abandonConditions.add(p);
            return this;
        }

        /** Build the commitment with FORMED status at the given tick. */
        public Commitment form(long currentTick) {
            return new Commitment(intentNature, targetId, sourceGoal, reason,
                    maxDurationTicks, currentTick,
                    successConditions, abandonConditions, successDescription);
        }
    }
}
