package dev.ergenverse.runtime;

/**
 * EventRuntime — WorldEventBus, the simulation tick's nervous system.
 *
 * <p><b>Contract (2026-07-23 directive):</b> The player is a first-class
 * actor. All player actions flow through SimulationActions → WorldEventBus →
 * all subscribers. Never write to siloed stores directly. Every state change
 * is an event; every event is observed by all relevant subscribers.
 *
 * <p>The EventRuntime owns:
 * <ul>
 *   <li>The WorldEventBus (publish/subscribe)</li>
 *   <li>The event history (for memory, karma, and chronicle)</li>
 *   <li>The semantic tag system (act_of_mercy, cultivation_revealed, promise_broken)</li>
 *   <li>The opportunity carrier (assigns opportunity.*.emerged events to nearby NPCs)</li>
 * </ul>
 */
public final class EventRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean started = false;

    EventRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Start the event bus. Called on WorldRuntime.initialize(). */
    void start() {
        // TODO: Wire all subscribers to the WorldEventBus.
        // WangLinReasoningEngine subscribes to: act_of_mercy, cultivation_revealed, promise_broken
        // ActorRelationshipStore subscribes to: relationship_change, betrayal, alliance_formed
        // OpportunityCarrierSubscriber subscribes to: opportunity.*.emerged
        // CanonDivergenceRecorder subscribes to: ALL (records any canon divergence)
        // WorldChronicle subscribes to: ALL (the persistent history)
        started = true;
    }

    public boolean isStarted() { return started; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
