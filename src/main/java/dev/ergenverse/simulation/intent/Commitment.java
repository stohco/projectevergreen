package dev.ergenverse.simulation.intent;

import dev.ergenverse.simulation.cognition.CognitionGoal;

/**
 * Commitment — a decision that persists across ticks.
 *
 * <p>This is the missing layer the user identified:
 * <blockquote>
 * Mind → Reasoning → Decision → <b>Commitment</b> → Execution.
 *
 * The "goal" isn't really the interesting part anymore.
 * The interesting part is the commitment.
 *
 * Once Wang Lin decides "I'm going to investigate those wolves,"
 * that decision should persist. He shouldn't rethink it every tick.
 * That's closer to human behavior.
 * </blockquote>
 *
 * <h2>The difference between Intent and Commitment</h2>
 *
 * <p>An {@link Intent} is the immediate strategic framing of behavior.
 * It is re-evaluated frequently (seconds). It flickers as the world
 * flickers. If Wang Lin sees a wolf, his Intent becomes OBSERVE_WOLF.
 * If the wolf moves behind a tree, his Intent may flicker to
 * APPROACH. If a player walks near, his Intent flickers to
 * AVOID_REVEALING_STRENGTH. The Intent is reactive.
 *
 * <p>A <b>Commitment</b> is the decision those Intents serve. It is
 * re-evaluated rarely (minutes to hours). It does not flicker. When
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
 * per-tick re-evaluation: "That's closer to human behavior" — humans
 * decide and then <i>do</i>.
 *
 * <p>With Commitment, the NPC holds its course. Wang Lin walks to the
 * ridge. He stays there. He watches. He comes back at dawn. The
 * player watching from below sees someone with a purpose, not someone
 * glitching between nav points.
 *
 * <h2>Lifecycle</h2>
 *
 * <ol>
 *   <li><b>Formed</b> — by the ReasoningEngine when a pressure crosses
 *       a threshold AND the actor's motivations favor responding to it.
 *       The Commitment records: which pressure triggered it, which
 *       motivations it serves, the target, and the persistence
 *       duration.</li>
 *   <li><b>Active</b> — the actor is executing the commitment. The
 *       CognitionDrivenGoal decomposes it into Intents and Tasks, but
 *       does NOT re-evaluate the commitment each tick. It only
 *       re-evaluates when:
 *       <ul>
 *         <li>the persistence duration elapses ({@link #isExpired}),</li>
 *         <li>the triggering pressure is gone ({@link #shouldAbandon}), or</li>
 *         <li>the commitment's success condition is met
 *             ({@link #isFulfilled}).</li>
 *       </ul></li>
 *   <li><b>Paused</b> — a higher-priority Intent (e.g. flee a player,
 *       eat, sleep) temporarily overrides the commitment. The
 *       commitment is NOT abandoned; it resumes when the override
 *       clears. Pauses do not reset the persistence clock.</li>
 *   <li><b>Completed</b> — the success condition was met, OR the
 *       persistence elapsed and the actor chose not to renew.</li>
 *   <li><b>Abandoned</b> — the triggering pressure disappeared (wolves
 *       left, the herb was harvested by someone else, the elder died).
 *       The actor re-evaluates from scratch.</li>
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
 *   Commitment: OBSERVE_WOLF_ACTIVITY, target=western_ridge,
 *               persistenceDuration=12000t (10 min real-time),
 *               formedAtTick=131000, status=ACTIVE
 *   Execution:  CognitionDrivenGoal decomposes → MOVE_TO ridge →
 *               HOLD_POSITION+FACE wolves → WAIT 2000t → return.
 *               Per-tick Intent may flicker (AVOID_PLAYER when player
 *               approaches) but the Commitment holds.
 * </pre>
 *
 * <p><b>Provenance: INFERRED.</b> The user's design review named this
 * layer explicitly. The fields and lifecycle are distilled from that
 * review and from Article XLV §3 (pressures → priorities → commitment
 * → execution). The class is the concrete realization of the
 * architecture the user endorsed.
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
        /** Success condition met, or persistence elapsed without renewal. */
        COMPLETED,
        /** Triggering pressure disappeared; actor must re-evaluate from scratch. */
        ABANDONED
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

    /** Ticks this commitment should persist before re-evaluation.
     * Commitments are stickier than Intents (minutes/hours, not seconds). */
    public final long persistenceDurationTicks;

    /** Tick when the commitment was formed. */
    public final long formedAtTick;

    /** Tick when the commitment was last reaffirmed (after a pause). */
    public long lastReaffirmedTick;

    /** Current lifecycle status. */
    public Status status;

    /** Optional success condition description (for future isFulfilled() hooks). */
    public final String successCondition;

    /**
     * Construct a new commitment with FORMED status.
     *
     * @param intentNature the Intent nature to produce when active
     * @param targetId what the commitment is directed at
     * @param sourceGoal the long-term cognition goal this serves
     * @param reason why this commitment was formed (for logging/dialogue)
     * @param persistenceDurationTicks how long it persists before re-evaluation
     * @param currentTick the current server tick
     * @param successCondition optional human-readable success condition
     */
    public Commitment(IntentNature intentNature,
                      String targetId,
                      CognitionGoal sourceGoal,
                      String reason,
                      long persistenceDurationTicks,
                      long currentTick,
                      String successCondition) {
        this.intentNature = intentNature;
        this.targetId = targetId;
        this.sourceGoal = sourceGoal;
        this.reason = reason;
        this.persistenceDurationTicks = persistenceDurationTicks;
        this.formedAtTick = currentTick;
        this.lastReaffirmedTick = currentTick;
        this.status = Status.FORMED;
        this.successCondition = successCondition;
    }

    /**
     * Has this commitment lived past its persistence duration?
     *
     * <p>Expiration does NOT mean the commitment is abandoned — it means
     * the actor should re-evaluate. The actor may renew the commitment
     * (extend persistence) if the pressure is still present.
     *
     * @param currentTick the current server tick
     * @return true if the commitment has lived past its persistence duration
     */
    public boolean isExpired(long currentTick) {
        return currentTick >= lastReaffirmedTick + persistenceDurationTicks;
    }

    /**
     * Should this commitment be abandoned because the triggering
     * pressure is gone?
     *
     * <p>This is a hook for future pressure-monitoring logic. For now it
     * returns false — abandonment is decided by the ReasoningEngine
     * which has access to the current WorldSituation. The method exists
     * so the CognitionDrivenGoal can ask the question without coupling
     * to the ReasoningEngine.
     *
     * @param currentTick the current server tick
     * @return true if the commitment should be abandoned
     */
    public boolean shouldAbandon(long currentTick) {
        // Future: check if the triggering pressure is still present.
        // For now, abandonment is the ReasoningEngine's call.
        return false;
    }

    /**
     * Has this commitment been fulfilled?
     *
     * <p>Future: check the success condition against world state.
     * For now, fulfillment is the ReasoningEngine's call (it can
     * observe, e.g., that the wolves have left, or that the herb
     * has been harvested).
     *
     * @param currentTick the current server tick
     * @return true if the commitment's success condition is met
     */
    public boolean isFulfilled(long currentTick) {
        // Future: check success condition against world state.
        return false;
    }

    /**
     * Is this commitment currently actionable (ACTIVE or FORMED)?
     */
    public boolean isActionable() {
        return status == Status.ACTIVE || status == Status.FORMED;
    }

    /**
     * Reaffirm the commitment after a pause — reset the persistence
     * clock so the actor has a full duration to continue.
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
     * expectedDurationTicks is set to the commitment's remaining
     * persistence, so the Intent won't expire before the commitment
     * does.
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
                + " persist=" + persistenceDurationTicks + "t]";
    }
}
