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
 * <p>An {@code Activity} is a transient decision produced by the actor's
 * {@link CultivatorMind} in response to a {@link WorldSituation}. It carries:
 * <ul>
 *   <li><b>type</b> — what the actor is doing (OBSERVING, FLEEING, GUARDING...).</li>
 *   <li><b>location</b> — where the actor is doing it (settlement-local offset).</li>
 *   <li><b>expiryTick</b> — when the activity naturally ends.</li>
 *   <li><b>reason</b> — a canon-faithful one-line explanation of WHY this actor
 *       chose this activity (recorded to settlement memory).</li>
 * </ul>
 *
 * <p><b>Per the user's directive (this cycle):</b> Activity no longer carries a
 * <b>poseTag</b>. The renderer decides animation, not the activity. The same
 * "OBSERVING_THREAT" activity produces a crouch for a cultivator and a
 * stand-still for a mortal — the renderer reads (activity.type + actor.role)
 * and picks the animation. This is the Activity → Intent → Animation Selection
 * → Renderer pipeline the user asked for.
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
 *   WorldSituation → CultivatorMind.evaluate() [scores candidates]
 *     → Activity [Wang Lin: OBSERVING; patriarch: GUARDING; others: FLEEING]
 *       → EntityCultivator at activity.location, pose derived by renderer
 * </pre>
 * The same wolf event produces different minds reaching different conclusions
 * — and the decision <b>emerges</b> from the mind's motivation weights, not
 * from a switch(profile). Nobody wrote "if Wang Lin." That is simulation.
 *
 * <h2>Null vs. non-null Activity</h2>
 * <p>When the reasoning engine returns {@code null}, the actor falls back to
 * their peaceful daily rhythm (the time-of-day weighted presence pick from
 * {@link ActorPresence}). A non-null Activity <b>overrides</b> the daily rhythm
 * for its duration. This means the peaceful daily-rhythm code is untouched —
 * reasoning layers on top, it does not replace it.
 */
public final class Activity {

    /** The activity type. Drives location semantics; the renderer derives animation from (type + actor role). */
    public enum Type {
        // ── Peaceful activities (future: produced by daily-rhythm reasoning) ──
        /** Sitting in cultivation. */
        MEDITATING,
        /** Harvesting crops or herbs. */
        HARVESTING,
        /** Studying a text or restriction. */
        STUDYING,
        /** Cooking a meal. */
        COOKING,
        /** Teaching a student. */
        TEACHING,

        // ── Threat-response activities (produced by CultivatorMind.evaluate) ──
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
         * Location: a vantage point at the settlement edge toward the threat.
         */
        INVESTIGATING,
    PURSUING_OPPORTUNITY,
    SOCIALIZING,
    OBSERVING_THREAT,
        /**
         * Standing guard at the settlement perimeter or a valued asset.
         * The defender's response: weapon ready, position between threat and
         * what they protect.
         * Location: the settlement edge toward the threat.
         */
        GUARDING,
        /**
         * Securing livestock or goods before fleeing. The responsible-party
         * response: the laborer runs to the livestock pen, the merchant packs
         * valuables — then both flee.
         * Location: the asset being secured.
         */
        SECURING_ASSETS
    }

    /** The activity type. */
    public final Type type;

    /** Settlement-local X offset where the actor performs this activity. */
    public final int offsetX;

    /** Settlement-local Z offset where the actor performs this activity. */
    public final int offsetZ;

    /** The game tick when this activity expires (threat ends, task done). */
    public final long expiryTick;

    /** A canon-faithful one-line reason this actor chose this activity. */
    public final String reason;

    public Activity(Type type, int offsetX, int offsetZ,
                    long expiryTick, String reason) {
        this.type = type;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.expiryTick = expiryTick;
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "Activity[" + type + " @(" + offsetX + "," + offsetZ + ")"
                + " until tick " + expiryTick + " — " + reason + "]";
    }
}
