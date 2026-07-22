package dev.ergenverse.simulation.event;

import net.minecraft.core.BlockPos;

import java.util.Collections;
import java.util.Map;

/**
 * WorldEvent — an immutable record describing one disturbance in the world.
 *
 * <p><b>Event-Sourced Architecture (2026-07-23):</b>
 * "The world is no longer driven by methods. It's driven by facts."
 * A WorldEvent is a rich <b>fact</b> carrying what happened, where, who did it,
 * who was affected, what it meant, and structured metadata for downstream
 * subscribers. The player is a first-class actor — their actions publish
 * WorldEvents exactly like spirit fruit ripening or sect declarations.
 *
 * <p><b>Per user directive (2026-07-23 #2):</b>
 * <pre>
 *   "WorldEvent needs structured payloads.
 *
 *   metadata
 *     gift
 *       ↓ item id
 *       quality
 *       quantity
 *
 *   No string parsing ever again."
 * </pre>
 *
 * <p>An event has:
 * <ul>
 *   <li><b>topic</b> — dot-separated routing string (e.g. "player.gift.given").</li>
 *   <li><b>energyType</b> — propagation radius category (QI, SOCIAL, PHYSICAL, etc.).</li>
 *   <li><b>pos</b> — world position. BlockPos.ZERO for global events.</li>
 *   <li><b>intensity</b> — 0.0–1.0, how strong the disturbance is.</li>
 *   <li><b>severity</b> — 0.0–1.0, how historically significant.</li>
 *   <li><b>description</b> — human-readable canon-faithful description (for
 *       chronicle entries, dialogue, NPC memories that are narrative text).</li>
 *   <li><b>canonSource</b> — provenance citation (Art XV).</li>
 *   <li><b>timestamp</b> — server tick when published.</li>
 *   <li><b>sourceActorId</b> — who performed the action (player UUID or NPC canon ID).
 *       Empty for environmental events.</li>
 *   <li><b>targetActorId</b> — who was affected. Empty if no specific target.</li>
 *   <li><b>semanticTag</b> — the meaning classification (e.g. "ACT_OF_MERCY").
 *       Empty for events without semantic classification.</li>
 *   <li><b>metadata</b> — structured key-value pairs for machine consumption.
 *       Subscribers read this instead of parsing the description string.
 *       Examples: {"item_name": "Restriction Flag", "item_quality": "rare",
 *       "npc_name": "Wang Lin", "realm": "Nascent Soul",
 *       "combat_outcome": "player_won", "from_realm": "Core Formation"}.
 *       Empty for events without structured data.</li>
 * </ul>
 *
 * <p><b>Immutability:</b> record — all fields final. The metadata map is
 * unmodifiable (either empty or wrapped via Collections.unmodifiableMap).
 * Safe for concurrent dispatch.
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
        String semanticTag,
        Map<String, String> metadata
) {
    /**
     * Compact factory for environmental events — no actor metadata, no metadata.
     * Severity defaults to intensity.
     */
    public static WorldEvent of(String topic, EnergyType type, BlockPos pos,
                                 float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, pos, intensity, intensity, desc, canon, tick,
                "", "", "", Map.of());
    }

    /**
     * Factory for global (positionless) environmental events.
     */
    public static WorldEvent global(String topic, EnergyType type,
                                     float intensity, String desc, String canon, long tick) {
        return new WorldEvent(topic, type, BlockPos.ZERO, intensity, intensity, desc, canon, tick,
                "", "", "", Map.of());
    }

    /**
     * Factory for a simulation event with actor metadata and semantic tag,
     * but no structured metadata payload. Used for backward compatibility
     * with callers that haven't migrated to metadata yet.
     */
    public static WorldEvent simulation(String topic, EnergyType type, BlockPos pos,
                                         float intensity, float severity,
                                         String desc, String canon, long tick,
                                         String sourceActorId, String targetActorId,
                                         String semanticTag) {
        return new WorldEvent(topic, type, pos, intensity, severity, desc, canon, tick,
                sourceActorId != null ? sourceActorId : "",
                targetActorId != null ? targetActorId : "",
                semanticTag != null ? semanticTag : "",
                Map.of());
    }

    /**
     * Factory for a simulation event with structured metadata.
     *
     * <p>This is the preferred factory for gameplay code. It carries
     * structured data that subscribers read directly instead of
     * parsing the description string. Example:
     * <pre>
     *   WorldEvent.of("player.gift.given", EnergyType.SOCIAL, pos,
     *       0.4f, 0.4f, "Player gave Restriction Flag to Wang Lin.",
     *       "SIMULATION", tick,
     *       player.getUUID(), "wang_lin", "GIFT_GIVEN",
     *       Map.of("item_name", "Restriction Flag",
     *             "item_quality", "rare"));
     * </pre>
     *
     * @param topic          routing topic
     * @param type           energy type
     * @param pos            position (or BlockPos.ZERO for global)
     * @param intensity      0.0–1.0
     * @param severity       0.0–1.0
     * @param desc           human-readable description
     * @param canon          provenance citation
     * @param tick           server tick
     * @param sourceActorId  who performed the action
     * @param targetActorId  who was affected
     * @param semanticTag    meaning classification
     * @param metadata       structured key-value pairs (machine-readable)
     */
    public static WorldEvent of(String topic, EnergyType type, BlockPos pos,
                                 float intensity, float severity,
                                 String desc, String canon, long tick,
                                 String sourceActorId, String targetActorId,
                                 String semanticTag,
                                 Map<String, String> metadata) {
        return new WorldEvent(topic, type, pos, intensity, severity, desc, canon, tick,
                sourceActorId != null ? sourceActorId : "",
                targetActorId != null ? targetActorId : "",
                semanticTag != null ? semanticTag : "",
                metadata != null && !metadata.isEmpty()
                        ? Collections.unmodifiableMap(metadata)
                        : Map.of());
    }

    /**
     * Convenience: get a metadata value, or fallback.
     * Returns the fallback if the key is missing.
     */
    public String meta(String key, String fallback) {
        return metadata.getOrDefault(key, fallback);
    }

    /** Whether this event is global (no specific position). */
    public boolean isGlobal() {
        return pos.equals(BlockPos.ZERO);
    }

    /** Whether this event carries actor metadata. */
    public boolean hasActors() {
        return !sourceActorId.isEmpty();
    }

    /** Whether this event has a semantic classification. */
    public boolean hasSemanticTag() {
        return !semanticTag.isEmpty();
    }

    /** Whether this event has structured metadata. */
    public boolean hasMetadata() {
        return !metadata.isEmpty();
    }

    @Override
    public String toString() {
        return "WorldEvent[" + topic + " @" + timestamp
                + " " + energyType + " i=" + Math.round(intensity * 100)
                + "% s=" + Math.round(severity * 100) + "%"
                + (hasActors() ? " src=" + sourceActorId : "")
                + (!targetActorId.isEmpty() ? " tgt=" + targetActorId : "")
                + (hasSemanticTag() ? " sem=" + semanticTag : "")
                + (hasMetadata() ? " meta=" + metadata.keySet() : "")
                + "]";
    }
}
