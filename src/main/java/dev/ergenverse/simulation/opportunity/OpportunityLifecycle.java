package dev.ergenverse.simulation.opportunity;

/**
 * OpportunityLifecycle — the five states of an opportunity's life cycle.
 *
 * <p>Per PROJECT_MASTER.md §5.4.1, <b>every opportunity is a finite-state
 * machine</b>. Nothing should instantly appear. Everything should
 * <i>become</i>. This enum encodes the five canon-faithful states and the
 * allowed transition graph between them.
 *
 * <h2>The Five States</h2>
 * <ol>
 *   <li>{@link #DORMANT} — exists in potential, not yet perceptible. A spirit
 *       fruit is growing but unripe; a vein is stable but weakening.</li>
 *   <li>{@link #FORMING} — now perceptible to the right observer. Spiritual
 *       fluctuations begin. Beasts nearby grow restless. Mortals hear
 *       strange sounds.</li>
 *   <li>{@link #CONTESTED} — multiple actors are aware and competing. Beasts
 *       fighting cultivators; two sects dispatching scouts; a wandering
 *       cultivator arrives.</li>
 *   <li>{@link #RESOLVED} — the opportunity has concluded. Someone got it.
 *       The fruit was eaten. The vein was harvested. A beast advanced.</li>
 *   <li>{@link #HISTORICAL} — part of the world's memory. NPCs reference it:
 *       "Three years ago, a spirit fruit ripened in Mosquito Valley — many
 *       died for it." Informs rumor propagation and the Expectation Model.</li>
 * </ol>
 *
 * <h2>Transition graph</h2>
 * <pre>
 *   DORMANT   ──► FORMING    (world simulation says: perceptible)
 *   FORMING   ──► CONTESTED  (first competitor arrives)
 *   CONTESTED ──► RESOLVED   (one actor claims it / destroyed / time expires)
 *   RESOLVED  ──► HISTORICAL (after a configurable cooldown)
 *   HISTORICAL (terminal — persists in world memory forever)
 * </pre>
 *
 * <h2>Design rule (PROJECT_MASTER.md §5.4.1)</h2>
 * <blockquote>
 *   Players who arrive late find <i>consequences</i>, not the opportunity
 *   itself. They find a beast's corpse (winner ate the fruit), a battle scar
 *   (two sects fought), a wandering cultivator's journal. The world records
 *   what happened — even if the player never saw it.
 * </blockquote>
 *
 * <h2>Why a state enum (not a free-form string)</h2>
 * <p>Using an enum makes invalid states unrepresentable. {@link #canTransitionTo}
 * enforces the canon-faithful transition graph at compile time. Any code that
 * attempts an illegal transition is rejected by the FSM driver before any
 * state mutation occurs.
 */
public enum OpportunityLifecycle {

    /**
     * State 1 — Dormant.
     * <p>The opportunity exists in potential but has not yet become
     * perceptible. A spirit fruit is growing but unripe; a vein is stable
     * but weakening.
     * <p>Transitions to {@link #FORMING} when the world simulation says the
     * opportunity is now perceptible (e.g. spirit fruit ripens, vein
     * destabilizes).
     */
    DORMANT(
        "Dormant",
        "The opportunity exists in potential but has not yet become perceptible. " +
        "A spirit fruit is growing but unripe; a vein is stable but weakening."
    ),

    /**
     * State 2 — Forming.
     * <p>The opportunity is now perceptible to the right observer. Spiritual
     * fluctuations begin. Beasts nearby grow restless. Mortals hear strange
     * sounds.
     * <p>Transitions to {@link #CONTESTED} when the first competitor arrives
     * (beast, cultivator, scout).
     */
    FORMING(
        "Forming",
        "The opportunity is now perceptible to the right observer. " +
        "Spiritual fluctuations begin. Beasts nearby grow restless. " +
        "Mortals hear strange sounds."
    ),

    /**
     * State 3 — Contested.
     * <p>Multiple actors are aware and competing. Beasts fighting
     * cultivators; two sects dispatching scouts; a wandering cultivator
     * arrives.
     * <p>Transitions to {@link #RESOLVED} when one actor claims it, OR it is
     * destroyed, OR time expires.
     */
    CONTESTED(
        "Contested",
        "Multiple actors are aware and competing. Beasts fighting cultivators; " +
        "two sects dispatching scouts; a wandering cultivator arrives."
    ),

    /**
     * State 4 — Resolved.
     * <p>The opportunity has concluded. Someone got it. The fruit was eaten.
     * The vein was harvested. A beast advanced.
     * <p>Transitions to {@link #HISTORICAL} after a configurable cooldown
     * (default 6000 ticks = 5 minutes for testing; production would be much
     * longer).
     */
    RESOLVED(
        "Resolved",
        "The opportunity has concluded. Someone got it. The fruit was eaten. " +
        "The vein was harvested. A beast advanced."
    ),

    /**
     * State 5 — Historical (terminal).
     * <p>The opportunity is part of the world's memory. NPCs reference it:
     * "Three years ago, a spirit fruit ripened in Mosquito Valley — many
     * died for it." Informs rumor propagation and the Expectation Model.
     * <p>This is a terminal state — it persists in world memory forever.
     */
    HISTORICAL(
        "Historical",
        "The opportunity is part of the world's memory. NPCs reference it: " +
        "'Three years ago, a spirit fruit ripened in Mosquito Valley — many died " +
        "for it.' Informs rumor propagation and the Expectation Model."
    );

    /** Human-readable label (English; the i18n key is in {@link OpportunityFSM}). */
    public final String label;
    /** Longer description, used in dev/debug output. */
    public final String description;

    OpportunityLifecycle(String label, String description) {
        this.label = label;
        this.description = description;
    }

    /**
     * Whether this state is terminal (no further transitions possible).
     * <p>Only {@link #HISTORICAL} is terminal — it persists forever as part
     * of the world's memory.
     */
    public boolean isTerminal() {
        return this == HISTORICAL;
    }

    /**
     * Whether the canon-faithful transition graph allows moving from this
     * state to {@code next}.
     * <p>The allowed transitions form a strict linear chain:
     * {@code DORMANT → FORMING → CONTESTED → RESOLVED → HISTORICAL}. No
     * skipping, no backtracking, no branching. This matches the canon rule:
     * "Nothing should instantly appear. Everything should become."
     * <p>Self-transitions (this == next) are NOT allowed — they would imply
     * a no-op transition, which the FSM never invokes. The FSM only fires a
     * transition when the state actually changes.
     *
     * @param next the proposed next state
     * @return true if the transition is canon-faithful, false otherwise
     */
    public boolean canTransitionTo(OpportunityLifecycle next) {
        if (next == null || next == this) return false;
        return switch (this) {
            case DORMANT    -> next == FORMING;
            case FORMING    -> next == CONTESTED;
            case CONTESTED  -> next == RESOLVED;
            case RESOLVED   -> next == HISTORICAL;
            case HISTORICAL -> false;  // terminal — no transitions
        };
    }

    /**
     * The next state in the canonical chain, or {@code null} if this is
     * terminal. Convenience method for FSM drivers that want to "advance
     * one step".
     */
    public OpportunityLifecycle nextInChain() {
        return switch (this) {
            case DORMANT    -> FORMING;
            case FORMING    -> CONTESTED;
            case CONTESTED  -> RESOLVED;
            case RESOLVED   -> HISTORICAL;
            case HISTORICAL -> null;
        };
    }
}
