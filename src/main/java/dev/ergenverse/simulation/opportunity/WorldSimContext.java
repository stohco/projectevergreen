package dev.ergenverse.simulation.opportunity;

import java.util.Random;

/**
 * WorldSimContext — the world-simulation bridge that drives opportunity
 * state transitions.
 *
 * <p>The {@link OpportunityFSM} is a pure state-machine driver: it knows the
 * transition <i>rules</i>, but it does NOT know <i>when</i> a transition
 * should fire. That decision belongs to the world simulation. This interface
 * is the contract between the two.
 *
 * <h2>The three transition queries</h2>
 * <p>Each FSM transition (DORMANT→FORMING, FORMING→CONTESTED,
 * CONTESTED→RESOLVED) is gated by one query:
 * <ul>
 *   <li>{@link #isPerceptible(String)} — DORMANT→FORMING: "has the
 *       opportunity become perceptible?" (e.g. spirit fruit ripened, vein
 *       destabilized)</li>
 *   <li>{@link #getFirstCompetitor(String)} — FORMING→CONTESTED: "who is the
 *       first competitor to arrive?" Returns the actor ID, or {@code null}
 *       if no competitor has arrived yet.</li>
 *   <li>{@link #getResolution(String)} — CONTESTED→RESOLVED: "what was the
 *       resolution?" Returns one of:
 *       <ul>
 *         <li>{@code "claimed_by_<actorId>"} — an actor claimed it</li>
 *         <li>{@code "destroyed"} — the opportunity was destroyed</li>
 *         <li>{@code "expired"} — time expired with no claimant</li>
 *         <li>{@code "rotted"} — the opportunity decayed (e.g. fruit rotted)</li>
 *         <li>{@code null} — still contested</li>
 *       </ul>
 *   </li>
 * </ul>
 *
 * <h2>Why an interface (not a concrete class)?</h2>
 * <p>The real world simulation is being built incrementally across multiple
 * phases (see PROJECT_MASTER.md §8 Phase A.3 → Phase B). This interface lets
 * us plug in:
 * <ul>
 *   <li>{@link NoOpWorldSimContext} — for testing. Always returns sensible
 *       defaults so opportunities progress through the FSM without any real
 *       world simulation.</li>
 *   <li>A future {@code ErgenverseWorldSimContext} — the real implementation
 *       that consults the living world (beast AI, cultivator AI, spirit vein
 *       stability, time of day, weather, karma, etc.).</li>
 *   <li>Per-opportunity-type contexts (e.g.
 *       {@code SpiritFruitTimeline.SpiritFruitCtx}) that drive a specific
 *       opportunity's lifecycle from its own internal timeline.</li>
 * </ul>
 *
 * <h2>Random source</h2>
 * <p>{@link #random()} provides a {@link Random} for any stochastic decisions
 * the FSM or context might need (e.g. picking between equally-strong
 * competitors). In production this should be a {@code RandomSource} derived
 * from the level's seed; for the interface contract we keep it simple to
 * avoid coupling to Minecraft's {@code RandomSource} API.
 */
public interface WorldSimContext {

    /**
     * Has the opportunity become perceptible?
     * <p>Used by the FSM to gate the DORMANT→FORMING transition. Returns
     * {@code true} once the world simulation says the opportunity is now
     * perceptible (e.g. a spirit fruit has ripened, a spirit vein has
     * destabilized).
     *
     * @param opportunityId the opportunity's stable ID
     * @return true if perceptible, false if still dormant
     */
    boolean isPerceptible(String opportunityId);

    /**
     * Who is the first competitor to arrive?
     * <p>Used by the FSM to gate the FORMING→CONTESTED transition. Returns
     * the actor ID (e.g. {@code "wandering_beast"},
     * {@code "scout_faction_A"}, {@code "wandering_qi_cultivator"}) of the
     * first competitor to arrive, or {@code null} if no competitor has
     * arrived yet.
     * <p>An "actor" is any being aware of the opportunity: a beast, a
     * cultivator, a scout NPC, a faction representative. The first
     * competitor marks the transition from "forming" (peaceful observation
     * possible) to "contested" (conflict possible).
     *
     * @param opportunityId the opportunity's stable ID
     * @return the first competitor's actor ID, or null
     */
    String getFirstCompetitor(String opportunityId);

    /**
     * What was the resolution of this opportunity?
     * <p>Used by the FSM to gate the CONTESTED→RESOLVED transition. Returns
     * one of:
     * <ul>
     *   <li>{@code "claimed_by_<actorId>"} — an actor claimed it</li>
     *   <li>{@code "destroyed"} — the opportunity was destroyed</li>
     *   <li>{@code "expired"} — time expired with no claimant</li>
     *   <li>{@code "rotted"} — the opportunity decayed</li>
     *   <li>{@code null} — still contested</li>
     * </ul>
     *
     * @param opportunityId the opportunity's stable ID
     * @return the resolution string, or null if still contested
     */
    String getResolution(String opportunityId);

    /**
     * The current world tick, as the world simulation sees it.
     * <p>Used for timestamp bookkeeping. In production this is
     * {@code ServerLevel#getGameTime()}.
     *
     * @return the current world tick
     */
    long currentTick();

    /**
     * A {@link Random} for stochastic decisions.
     * <p>In production, this should derive from the level's seed for
     * reproducibility. For testing, a plain {@code new Random()} suffices.
     *
     * @return a non-null Random instance
     */
    Random random();


    // ═══════════════════════════════════════════════════════════════════
    //  NoOpWorldSimContext — singleton for testing
    // ═══════════════════════════════════════════════════════════════════

    /**
     * A no-op {@link WorldSimContext} for testing.
     *
     * <p>Returns sensible defaults that drive opportunities through the FSM
     * without any real world simulation:
     * <ul>
     *   <li>{@link #isPerceptible(String)} — always {@code true} (the
     *       opportunity is perceptible from the start)</li>
     *   <li>{@link #getFirstCompetitor(String)} — always
     *       {@code "wandering_beast"} (a wandering beast is the first
     *       competitor)</li>
     *   <li>{@link #getResolution(String)} — always
     *       {@code "claimed_by_wandering_beast"} (the wandering beast
     *       claims it)</li>
     *   <li>{@link #currentTick()} — always 0 (the caller is responsible
     *       for passing the real world tick to the FSM)</li>
     *   <li>{@link #random()} — a shared {@link Random}</li>
     * </ul>
     *
     * <p>This lets the FSM be exercised end-to-end in unit tests without a
     * real Minecraft world. The RESOLVED→HISTORICAL cooldown (6000 ticks)
     * still gates the final transition; tests can either wait it out or
     * call {@link OpportunityState#toHistorical(long)} directly.
     *
     * <h2>Why a singleton?</h2>
     * <p>The {@link OpportunityRegistry}'s default tickAll path uses this
     * instance for opportunities without a specific driver. A singleton
     * avoids per-tick allocation pressure.
     */
    WorldSimContext NoOpWorldSimContext = new NoOpWorldSimContextImpl();

    /** Concrete no-op implementation. Held as a named class for clarity. */
    final class NoOpWorldSimContextImpl implements WorldSimContext {
        private final Random rng = new Random(42L);  // deterministic for tests

        private NoOpWorldSimContextImpl() {}

        @Override public boolean isPerceptible(String opportunityId) { return true; }
        @Override public String getFirstCompetitor(String opportunityId) { return "wandering_beast"; }
        @Override public String getResolution(String opportunityId) { return "claimed_by_wandering_beast"; }
        @Override public long currentTick() { return 0L; }
        @Override public Random random() { return rng; }

        @Override public String toString() { return "NoOpWorldSimContext{}"; }
    }
}
