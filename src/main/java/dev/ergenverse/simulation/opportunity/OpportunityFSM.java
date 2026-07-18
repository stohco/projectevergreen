package dev.ergenverse.simulation.opportunity;

/**
 * OpportunityFSM — the finite-state-machine driver for opportunity lifecycles.
 *
 * <p>This is the PURE transition engine. It knows the rules
 * ({@link OpportunityLifecycle#canTransitionTo}) and the gates
 * ({@link WorldSimContext}). It does NOT know:
 * <ul>
 *   <li>What kind of opportunity it is (spirit fruit? inheritance? treasure birth?).</li>
 *   <li>How to spawn entities (that's the driver's job, e.g.
 *       {@link SpiritFruitTimeline}).</li>
 *   <li>How to inform the rumor system (Phase B's job).</li>
 * </ul>
 *
 * <h2>The tick contract</h2>
 * <p>Each call to {@link #tickOpportunity} advances the state by AT MOST ONE
 * transition. This is strict FSM semantics: a state either fires its
 * transition this tick (and settles into the new state) or it stays put.
 * There is no "skip-ahead" — opportunities become, they do not appear.
 *
 * <h2>Transition rules (PROJECT_MASTER.md §5.4.1)</h2>
 * <pre>
 *   DORMANT   ──► FORMING    when ctx.isPerceptible(opportunityId) returns true
 *   FORMING   ──► CONTESTED  when ctx.getFirstCompetitor(opportunityId) returns non-null
 *   CONTESTED ──► RESOLVED   when ctx.getResolution(opportunityId) returns non-null
 *   RESOLVED  ──► HISTORICAL after RESOLVED_COOLDOWN_TICKS ticks in RESOLVED
 *   HISTORICAL (terminal)
 * </pre>
 *
 * <h2>Perception scaling</h2>
 * <p>{@link #getPlayerObservableInfo} returns what a player of given
 * cultivation realm can perceive about an opportunity. Mortals perceive
 * almost nothing; Ascendant+ perceive everything. See
 * {@link PlayerObserverRealm} for the seven perception tiers.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Players who arrive late find <i>consequences</i>, not the opportunity
 *   itself. They find a beast's corpse (winner ate the fruit), a battle scar
 *   (two sects fought), a wandering cultivator's journal. The world records
 *   what happened — even if the player never saw it.
 * </blockquote>
 */
public final class OpportunityFSM {

    private OpportunityFSM() {}

    /**
     * The cooldown (in world ticks) between RESOLVED and HISTORICAL.
     * <p>Default 6000 ticks = 5 minutes at 20 TPS. This is a TESTING value
     * — production would be MUCH longer (months or years of in-world time)
     * so that recent events stay in the "live world" before becoming history.
     * <p>This is a tunable: future config may override it.
     */
    public static final long RESOLVED_COOLDOWN_TICKS = 6000L;

    /**
     * The i18n key prefix for opportunity state display names.
     * <p>Full keys look like {@code "ergenverse.opportunity.state.dormant"}.
     */
    public static final String I18N_PREFIX = "ergenverse.opportunity.state.";

    // ═══════════════════════════════════════════════════════════════════
    //  Tick — the FSM driver
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Advance one opportunity's state by at most one transition.
     *
     * <p>The transition rules (PROJECT_MASTER.md §5.4.1):
     * <ul>
     *   <li><b>DORMANT → FORMING</b>: when the world simulation says the
     *       opportunity is perceptible (e.g. spirit fruit ripens, vein
     *       destabilizes). Gated by {@link WorldSimContext#isPerceptible}.</li>
     *   <li><b>FORMING → CONTESTED</b>: when the first competitor arrives
     *       (beast, cultivator, scout). Gated by
     *       {@link WorldSimContext#getFirstCompetitor}.</li>
     *   <li><b>CONTESTED → RESOLVED</b>: when one actor claims it, OR it is
     *       destroyed, OR time expires. Gated by
     *       {@link WorldSimContext#getResolution}.</li>
     *   <li><b>RESOLVED → HISTORICAL</b>: after a configurable cooldown
     *       (default 6000 ticks = 5 minutes for testing). Production would
     *       be much longer.</li>
     * </ul>
     *
     * <p>If the state is already HISTORICAL, this is a no-op. If the state
     * is DORMANT and the world simulation says it's not yet perceptible,
     * this is also a no-op (the state stays DORMANT).
     *
     * @param state     the opportunity state to advance (mutated in place)
     * @param worldTick the current world tick
     * @param ctx       the world-simulation context (provides transition gates)
     * @return true if a transition was fired this tick, false otherwise
     */
    public static boolean tickOpportunity(OpportunityState state, long worldTick, WorldSimContext ctx) {
        if (state == null || ctx == null) return false;

        // Always update the timestamp.
        state.tick(worldTick);

        OpportunityLifecycle lifecycle = state.getLifecycle();
        if (lifecycle == null || lifecycle.isTerminal()) return false;

        switch (lifecycle) {
            case DORMANT: {
                // DORMANT → FORMING: when the world says it's perceptible.
                if (ctx.isPerceptible(state.getOpportunityId())) {
                    state.transitionTo(OpportunityLifecycle.FORMING, worldTick);
                    return true;
                }
                return false;
            }
            case FORMING: {
                // FORMING → CONTESTED: when the first competitor arrives.
                String competitor = ctx.getFirstCompetitor(state.getOpportunityId());
                if (competitor != null && !competitor.isBlank()) {
                    state.addActor(competitor);
                    state.transitionTo(OpportunityLifecycle.CONTESTED, worldTick);
                    return true;
                }
                return false;
            }
            case CONTESTED: {
                // CONTESTED → RESOLVED: when an actor claims it / destroyed / expired.
                String resolution = ctx.getResolution(state.getOpportunityId());
                if (resolution != null && !resolution.isBlank()) {
                    state.resolve(resolution, worldTick);
                    return true;
                }
                return false;
            }
            case RESOLVED: {
                // RESOLVED → HISTORICAL: after the cooldown.
                long resolvedAt = state.getWorldTickResolved();
                if (resolvedAt > 0 && (worldTick - resolvedAt) >= RESOLVED_COOLDOWN_TICKS) {
                    state.toHistorical(worldTick);
                    return true;
                }
                return false;
            }
            case HISTORICAL:
            default:
                return false;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Display name (i18n)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * The i18n key for the display name of a lifecycle state.
     * <p>Full keys look like {@code "ergenverse.opportunity.state.dormant"}.
     * <p>The lang files (a future subagent's responsibility) will provide
     * translations. Until then, callers can fall back to
     * {@link OpportunityLifecycle#label}.
     */
    public static String getDisplayNameForState(OpportunityLifecycle lifecycle) {
        if (lifecycle == null) return I18N_PREFIX + "unknown";
        return I18N_PREFIX + lifecycle.name().toLowerCase();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Player perception
    // ═══════════════════════════════════════════════════════════════════

    /**
     * What can a player of the given cultivation realm perceive about this
     * opportunity?
     *
     * <p>Per PROJECT_MASTER.md §5.5 (Divine Sense Atlas), perception scales
     * fundamentally with cultivation realm:
     * <ul>
     *   <li><b>Mortals</b> perceive almost nothing — at most, vague unease,
     *       strange sounds, deer acting oddly.</li>
     *   <li><b>Qi Condensation+</b> perceive spiritual fluctuations, beast
     *       unrest.</li>
     *   <li><b>Foundation+</b> perceive resolved outcomes.</li>
     *   <li><b>Nascent Soul+</b> perceive the actors contesting.</li>
     *   <li><b>Soul Formation+</b> perceive dormant potential.</li>
     *   <li><b>Ascendant+</b> perceive EVERYTHING — full actor list,
     *       distortion hops, the underlying driver state.</li>
     * </ul>
     *
     * <p>This method does NOT mutate the state. It is a read-only query that
     * the future Divine Sense Atlas UI (M-key) will use to render the
     * player's perception of nearby opportunities.
     *
     * @param state the opportunity state to perceive
     * @param realm the player's perception tier
     * @return a human-readable description of what the player perceives
     */
    public static String getPlayerObservableInfo(OpportunityState state, PlayerObserverRealm realm) {
        if (state == null) return "";
        if (realm == null) realm = PlayerObserverRealm.MORTAL;

        return switch (state.getLifecycle()) {
            case DORMANT -> {
                if (realm.isAtLeast(PlayerObserverRealm.SOUL_FORMATION)) {
                    yield "You sense a dormant potential here — something is growing, not yet ripe.";
                }
                if (realm.isAtLeast(PlayerObserverRealm.QI_CONDENSATION)) {
                    yield "You feel a faint spiritual undercurrent, but its source eludes you.";
                }
                yield "You perceive nothing of note here.";  // mortal
            }
            case FORMING -> {
                if (realm.isOmniscient()) {
                    yield "Opportunity forming: " + state.getOpportunityId()
                        + " — " + state.getCurrentActors().size() + " actor(s) aware.";
                }
                if (realm.isAtLeast(PlayerObserverRealm.NASCENT_SOUL)) {
                    yield "Spiritual fluctuations gather here — something is becoming. Beasts nearby grow restless.";
                }
                if (realm.isAtLeast(PlayerObserverRealm.QI_CONDENSATION)) {
                    yield "You sense spiritual fluctuations in the air — something is forming.";
                }
                yield "You hear strange sounds from this direction.";  // mortal
            }
            case CONTESTED -> {
                if (realm.isOmniscient()) {
                    yield "Opportunity contested: " + state.getOpportunityId()
                        + " — actors: " + state.getCurrentActors()
                        + " (rumor distortion: " + state.getDistortionHops() + " hops).";
                }
                if (realm.isAtLeast(PlayerObserverRealm.NASCENT_SOUL)) {
                    yield "Multiple actors are contesting an opportunity here: "
                        + state.getCurrentActors() + ".";
                }
                if (realm.isAtLeast(PlayerObserverRealm.QI_CONDENSATION)) {
                    yield "Spiritual fluctuations are intense here — something significant is unfolding.";
                }
                yield "You hear distant sounds of conflict from this direction.";  // mortal
            }
            case RESOLVED -> {
                String outcome = state.getResolutionOutcome();
                if (outcome == null) outcome = "unknown fate";
                if (realm.isAtLeast(PlayerObserverRealm.FOUNDATION)) {
                    yield "An opportunity here has concluded: " + humanizeOutcome(outcome) + ".";
                }
                if (realm.isAtLeast(PlayerObserverRealm.QI_CONDENSATION)) {
                    yield "The spiritual fluctuations here have settled — whatever was happening is over.";
                }
                yield "Whatever was happening here is over.";  // mortal
            }
            case HISTORICAL -> {
                String summary = state.getHistoricalSummary();
                if (summary == null) summary = "An event here is part of history.";
                if (realm.isAtLeast(PlayerObserverRealm.QI_CONDENSATION)) {
                    yield summary;
                }
                yield "Old stories cling to this place.";  // mortal
            }
        };
    }

    /**
     * Humanize a resolution outcome string for display.
     * <p>{@code "claimed_by_wolf_spirit_beast"} → "claimed by wolf spirit beast".
     */
    private static String humanizeOutcome(String outcome) {
        if (outcome == null) return "unknown fate";
        if (outcome.startsWith("claimed_by_")) {
            String actor = outcome.substring("claimed_by_".length());
            return "claimed by " + actor.replace('_', ' ');
        }
        return switch (outcome) {
            case "destroyed" -> "destroyed in conflict";
            case "expired"   -> "expired, unclaimed";
            case "rotted"    -> "rotted, untended";
            default          -> outcome;
        };
    }
}
