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
 * <p>An event has:
 * <ul>
 *   <li><b>topic</b> — a dot-separated topic string for routing
 *       (e.g. "opportunity.spirit_fruit.ripe", "npc.breakthrough",
 *       "beast.migrate", "rumor.spread"). Subscribers subscribe by
 *       topic prefix.</li>
 *   <li><b>energyType</b> — determines propagation radius and which
 *       subscribers are sensitive to it.</li>
 *   <li><b>pos</b> — the world position where the event originated.
 *       May be {@link BlockPos#ZERO} for global events.</li>
 *   <li><b>intensity</b> — 0.0 to 1.0. How strong the disturbance is.
 *       A ripening spirit fruit ramps from 0.1 to 1.0 over hours.</li>
 *   <li><b>severity</b> — 0.0 to 1.0. How historically significant.
 *       Events with severity ≥ {@link WorldEventBus#LEDGER_SEVERITY_THRESHOLD}
 *       are written to {@link dev.ergenverse.history.WorldHistory}.</li>
 *   <li><b>description</b> — human-readable canon-faithful description.</li>
 *   <li><b>canonSource</b> — provenance citation (Art XV). e.g.
 *       "RI Ch.12 §3" or "INFERRED from RI Ch.7".</li>
 *   <li><b>timestamp</b> — the server tick when the event was published.</li>
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
        long timestamp
) {
    /**
     * Compact factory for the common case: a positioned event with
     * default severity (derived from intensity).
     */
    public static WorldEvent of(String topic, EnergyType type, BlockPos pos,
                                 float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, pos, intensity, intensity, desc, canon, tick);
    }

    /**
     * Factory for a global (positionless) event. Uses BlockPos.ZERO as
     * the origin — subscribers treat this as "everywhere".
     */
    public static WorldEvent global(String topic, EnergyType type,
                                     float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, BlockPos.ZERO, intensity, intensity, desc, canon, tick);
    }

    /** Whether this event is global (no specific position). */
    public boolean isGlobal() {
        return pos.equals(BlockPos.ZERO);
    }

    @Override
    public String toString() {
        return "WorldEvent[" + topic + " @" + timestamp
                + " " + energyType + " i=" + Math.round(intensity * 100)
                + "% s=" + Math.round(severity * 100) + "%]";
    }
}
