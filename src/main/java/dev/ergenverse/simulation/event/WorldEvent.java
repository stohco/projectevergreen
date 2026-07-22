package dev.ergenverse.simulation.event;

import net.minecraft.core.BlockPos;

/**
 * WorldEvent — an immutable record describing one disturbance in the world.
 *
 * <p>Per the ChatGPT architectural review: "No quests. No scripts. Just
 * consequences." Every WorldEvent is a consequence — something that
 * objectively happened in the world, now propagating to any system that
 * cares.
 *
 * <p><b>Event-Sourced Architecture (2026-07-23 pivot):</b>
 * The user's core directive: "The world is no longer driven by methods.
 * It's driven by facts." A WorldEvent is now a rich <b>fact</b> — it
 * carries not just what happened and where, but <b>who</b> did it and
 * <b>who</b> was affected. This makes the player a first-class actor in
 * the simulation: a player action publishes a WorldEvent exactly like
 * a spirit fruit ripening or a sect declaring war, and every subscriber
 * (memory, rumor, chronicle, relationships, opportunities, dialogue)
 * derives its own consequences from that one fact.
 *
 * <p>An event has:
 * <ul>
 *   <li><b>topic</b> — a dot-separated topic string for routing
 *       (e.g. "player.gift.given", "actor.combat.engaged",
 *       "semantic.act_of_mercy", "beast.migrate", "rumor.spread").
 *       Subscribers subscribe by topic prefix.</li>
 *   <li><b>energyType</b> — determines propagation radius and which
 *       subscribers are sensitive to it.</li>
 *   <li><b>pos</b> — the world position where the event originated.
 *       May be {@link BlockPos#ZERO} for global events.</li>
 *   <li><b>intensity</b> — 0.0 to 1.0. How strong the disturbance is.</li>
 *   <li><b>severity</b> — 0.0 to 1.0. How historically significant.
 *       Events with severity ≥ {@link WorldEventBus#LEDGER_SEVERITY_THRESHOLD}
 *       are written to {@link dev.ergenverse.history.WorldHistory}.</li>
 *   <li><b>description</b> — human-readable canon-faithful description.</li>
 *   <li><b>canonSource</b> — provenance citation (Art XV). e.g.
 *       "RI Ch.12 §3" or "INFERRED from RI Ch.7" or "SIMULATION".</li>
 *   <li><b>timestamp</b> — the server tick when the event was published.</li>
 *   <li><b>sourceActorId</b> — who performed the action. A player UUID
 *       or an NPC canon ID (e.g. "wang_lin", "old_chen"). Empty string
 *       for environmental events (spirit fruit ripening, beast migration).</li>
 *   <li><b>targetActorId</b> — who was affected. A player UUID or NPC
 *       canon ID. Empty string if the event has no specific target
 *       (e.g. a breakthrough affects the breaker, not a separate target).</li>
 *   <li><b>semanticTag</b> — a {@link SemanticTag} name (e.g. "GIFT_GIVEN",
 *       "ACT_OF_MERCY", "CULTIVATION_REVEALED"). Empty string for
 *       events that carry no semantic classification. This is the
 *       "meaning" layer: NPCs don't just react to "player interacted" —
 *       they react to "player revealed their cultivation publicly."</li>
 * </ul>
 *
 * <p><b>Immutability:</b> WorldEvent is a record — all fields are final.
 * This allows safe concurrent dispatch to multiple subscribers.
 */
public record WorldEvent(
        String topic,
        EnergyType energyType,
        BlockPos pos,
        float intensity,
        float severity,
        String description,
        String canonSource,
        long timestamp,
        String sourceActorId,
        String targetActorId,
        String semanticTag
) {
    /**
     * Compact factory for the common case: a positioned event with
     * default severity (derived from intensity) and no actor metadata.
     * Used by environmental events (spirit fruit, beast migration, etc.).
     */
    public static WorldEvent of(String topic, EnergyType type, BlockPos pos,
                                 float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, pos, intensity, intensity, desc, canon, tick,
                "", "", "");
    }

    /**
     * Factory for a global (positionless) event. Uses BlockPos.ZERO as
     * the origin — subscribers treat this as "everywhere".
     */
    public static WorldEvent global(String topic, EnergyType type,
                                     float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, BlockPos.ZERO, intensity, intensity, desc, canon, tick,
                "", "", "");
    }

    /**
     * Factory for a <b>simulation event</b> — a rich fact carrying actor
     * metadata and a semantic classification. This is the factory used by
     * {@link dev.ergenverse.simulation.action.SimulationActions} when the
     * player or an NPC performs a meaningful action.
     *
     * <p>Unlike {@link #of}, this lets the caller set severity independently
     * of intensity (a gift might have low intensity but high severity if
     * it's a life-saving artifact), and carries the source/target actor IDs
     * and semantic tag that downstream subscribers (RelationshipEngine,
     * OpportunityGenerator, MemoryEventSubscriber) need.
     *
     * @param topic          the routing topic (e.g. "player.gift.given")
     * @param type           the energy type (determines propagation radius)
     * @param pos            the world position where the action occurred
     * @param intensity      0.0–1.0, how strong the disturbance is
     * @param severity       0.0–1.0, how historically significant
     * @param desc           human-readable description
     * @param canon          provenance citation
     * @param tick           the server tick
     * @param sourceActorId  who performed the action (player UUID or NPC canon ID)
     * @param targetActorId  who was affected (player UUID or NPC canon ID, "" if none)
     * @param semanticTag    a {@link SemanticTag} name, or "" for no classification
     */
    public static WorldEvent simulation(String topic, EnergyType type, BlockPos pos,
                                         float intensity, float severity,
                                         String desc, String canon, long tick,
                                         String sourceActorId, String targetActorId,
                                         String semanticTag) {
        return new WorldEvent(topic, type, pos, intensity, severity, desc, canon, tick,
                sourceActorId != null ? sourceActorId : "",
                targetActorId != null ? targetActorId : "",
                semanticTag != null ? semanticTag : "");
    }

    /** Whether this event is global (no specific position). */
    public boolean isGlobal() {
        return pos.equals(BlockPos.ZERO);
    }

    /** Whether this event carries actor metadata (is a simulation action). */
    public boolean hasActors() {
        return !sourceActorId.isEmpty();
    }

    /** Whether this event carries a semantic classification. */
    public boolean hasSemanticTag() {
        return !semanticTag.isEmpty();
    }

    @Override
    public String toString() {
        return "WorldEvent[" + topic + " @" + timestamp
                + " " + energyType + " i=" + Math.round(intensity * 100)
                + "% s=" + Math.round(severity * 100) + "%"
                + (hasActors() ? " src=" + sourceActorId : "")
                + (!targetActorId.isEmpty() ? " tgt=" + targetActorId : "")
                + (hasSemanticTag() ? " sem=" + semanticTag : "")
                + "]";
    }
}
