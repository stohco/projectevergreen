package dev.ergenverse.simulation.settlement;

/**
 * Activity — the thing an actor is currently <b>doing</b>.
 *
 * <p>Per the user's architectural directive (the cycle after SettlementThreatIndex):
 * <blockquote>
 * Presence should eventually disappear too. Instead I'd like Actors to own
 * Activities. Current Activity: Meditating, Walking Home, Teaching, Studying,
 * Cooking, Harvesting, Observing Wolves, Repairing Roof, Searching Cave.
 * <br><br>
 * Activity → Animation → Location → Renderer
 * <br><br>
 * Instead of "Presence: Home", imagine "Activity: Harvesting Herbs" — that
 * activity itself defines where, how fast, with whom, animation, interruptions,
 * completion, memory. Then the renderer simply follows.
 * </blockquote>
 *
 * <p>An {@code Activity} is a transient decision produced by the
 * {@link ActorReasoningEngine} in response to a {@link WorldSituation}. It
 * carries everything the renderer needs to portray the actor's current behavior:
 * <ul>
 *   <li><b>type</b> — what the actor is doing (drives animation/pose).</li>
 *   <li><b>location</b> — where the actor is doing it (settlement-local offset).</li>
 *   <li><b>poseTag</b> — the renderer pose hint (idle, meditating, observing, guarding).</li>
 *   <li><b>expiryTick</b> — when the activity naturally ends (threat expires,
 *       task completes). Until then, the actor holds this activity.</li>
 *   <li><b>reason</b> — a canon-faithful one-line explanation of WHY this actor
 *       chose this activity. Recorded to settlement memory so the village
 *       remembers who did what.</li>
 * </ul>
 *
 * <h2>The inversion this completes</h2>
 * <p>Before this cycle:
 * <pre>
 *   ThreatContext → ActorPresence.computePosition() [hardcoded: if threat, everyone home]
 *     → EntityCultivator at home position
 * </pre>
 * Everyone reacted identically to the same wolf — a switch statement, not
 * simulation.
 *
 * <p>After this cycle:
 * <pre>
 *   WorldSituation → ActorReasoningEngine.reason(actor, situation)
 *     → Activity [Wang Lin: OBSERVING; patriarch: GUARDING; others: FLEEING]
 *       → EntityCultivator at activity.location, holding activity.poseTag
 * </pre>
 * The same wolf event produces different minds reaching different conclusions.
 * That is simulation.
 *
 * <h2>Null vs. non-null Activity</h2>
 * <p>When the reasoning engine returns {@code null}, the actor falls back to
 * their peaceful daily rhythm (the time-of-day weighted presence pick from
 * {@link ActorPresence}). A non-null Activity <b>overrides</b> the daily rhythm
 * for its duration. This means the peaceful daily-rhythm code is untouched —
 * reasoning layers on top, it does not replace it.
 */
public final class Activity {

    /** What the actor is doing. Drives pose + location semantics. */
    public enum Type {
        // ── Peaceful activities (future: produced by daily-rhythm reasoning) ──
        /** Sitting in cultivation. Pose: POSE_MEDITATING. */
        MEDITATING,
        /** Harvesting crops or herbs. */
        HARVESTING,
        /** Studying a text or restriction. */
        STUDYING,
        /** Cooking a meal. */
        COOKING,
        /** Teaching a student. */
        TEACHING,

        // ── Threat-response activities (produced by ActorReasoningEngine) ──
        /**
         * Fled home and barred the door. The mortal's response to danger:
         * protect family, hide, wait it out.
         * Location: actor's home residence center.
         */
        FLEEING_HOME,
        /**
         * Observing the threat from cover without revealing capability.
         * The hidden cultivator's response: assess, don't expose, intervene
         * only if family is truly endangered.
         * Location: between home and the threat — a vantage point at the
         * settlement edge toward the threat direction.
         */
        OBSERVING_THREAT,
        /**
         * Standing guard at the settlement perimeter or a valued asset.
         * The defender's response: weapon ready, position between threat and
         * what they protect (family, livestock, sect gate).
         * Location: the settlement edge toward the threat, or the asset.
         */
        GUARDING,
        /**
         * Securing livestock or goods before fleeing. The responsible-party
         * response: the laborer runs to the livestock pen, the merchant packs
         * valuables — then both flee.
         * Location: the asset being secured (livestock pen, market stall).
         */
        SECURING_ASSETS
    }

    /** The activity type. */
    public final Type type;

    /** Settlement-local X offset where the actor performs this activity. */
    public final int offsetX;

    /** Settlement-local Z offset where the actor performs this activity. */
    public final int offsetZ;

    /**
     * Renderer pose hint. Maps to {@code EntityCultivator.POSE_*} constants:
     * <ul>
     *   <li>"idle" → {@code POSE_IDLE}</li>
     *   <li>"meditating" → {@code POSE_MEDITATING}</li>
     *   <li>"observing" → {@code POSE_OBSERVING} (crouched, watchful)</li>
     *   <li>"guarding" → {@code POSE_GUARDING} (combat stance)</li>
     *   <li>"casting" → {@code POSE_CASTING}</li>
     * </ul>
     */
    public final String poseTag;

    /** The game tick when this activity expires (threat ends, task done). */
    public final long expiryTick;

    /** A canon-faithful one-line reason this actor chose this activity. */
    public final String reason;

    public Activity(Type type, int offsetX, int offsetZ, String poseTag,
                    long expiryTick, String reason) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.poseTag = poseTag;
        this.expiryTick = expiryTick;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Activity[" + type + " @(" + offsetX + "," + offsetZ + ") "
                + poseTag + " until tick " + expiryTick
                + " — " + reason + "]";
    }
}
