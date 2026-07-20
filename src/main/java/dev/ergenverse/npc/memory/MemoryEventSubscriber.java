package dev.ergenverse.npc.memory;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.rumor.Rumor;
import dev.ergenverse.npc.rumor.RumorEngineEvents;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import dev.ergenverse.simulation.intent.ActorEntityLink;

/**
 * MemoryEventSubscriber — bridges WorldEventBus events into NPC cognitive
 * memories. When the world happens, NPCs remember.
 *
 * <p>Per Article XXXI.5 (Memory Metric): "Chapter complete only when THE
 * WORLD REMEMBERS." Before this subscriber, NpcCognitiveMemory had full
 * 3-tier persistence and decay but ZERO callers to its recording API.
 * Events fired, activities ran, but nothing was ever remembered. This
 * subscriber closes that gap.
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>A beast event fires: topic="beast.wolf_pack.stalking" near the village.</li>
 *   <li>This subscriber finds all linked actors within 64 blocks.</li>
 *   <li>Each nearby actor records an OBSERVATION or WORLD_EVENT memory.</li>
 *   <li>Memory tier is determined by event severity: high-severity events
 *       become LONG_TERM, moderate become MEDIUM_TERM, low become SHORT_TERM.</li>
 *   <li>Over time, NpcMemoryTickHandler decays these: MEDIUM→SHORT→forgotten.
 *       LONG_TERM persists forever.</li>
 * </ol>
 *
 * <p><b>The Memory Metric test (Art XXXI.5):</b>
 * <pre>
 *   Week 1: Old Chen owns a dog. (LONG_TERM SOCIAL memory)
 *   Week 3: Wolf attack — dog dies. (LONG_TERM WORLD_EVENT memory)
 *   Week 5: Old Chen says "Used to have one." (dialogue references memory)
 *   Months later: A child tells the story. (RUMOR memory from retelling)
 * </pre>
 * This subscriber handles step 2 (recording the wolf attack as memory).
 * Steps 1, 3, 4 require additional wiring (activity→memory, memory→dialogue,
 * memory→rumor) in future cycles.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a WorldEventSubscriber —
 * the same pattern as ActivityInterruptionSubscriber, QiDisturbanceSubscriber,
 * and BirdFlightSubscriber. It uses the existing WorldEventBus and
 * NpcCognitiveMemory. No new bus, no new engine.
 *
 * <p><b>Provenance: INFERRED.</b> The event→memory pattern is standard
 * cognitive simulation. Canon basis: in RI, Wang Lin remembers the wolf
 * attacks on the village, the elder's warnings, the Teng family's
 * oppression. These memories are not scripted — they are the natural
 * consequence of a world that remembers.
 */
public final class MemoryEventSubscriber implements WorldEventSubscriber {

    /** Maximum distance (blocks) at which an NPC can perceive and remember an event. */
    private static final int MAX_PERCEPTION_DISTANCE = 64;

    /** Minimum severity for an event to be remembered at all. */
    private static final float MIN_REMEMBER_SEVERITY = 0.15f;

    /** Severity threshold for LONG_TERM memory. Life-changing events. */
    private static final float LONG_TERM_SEVERITY = 0.7f;

    /** Severity threshold for MEDIUM_TERM memory. Notable events. */
    private static final float MEDIUM_TERM_SEVERITY = 0.4f;

    /** Cooldown: same actor + same topic prefix (e.g. "beast.wolf_pack") 
     *  won't record another memory within this many ticks.
     *  6000 ticks = 5 minutes. Prevents memory spam from repeated howls. */
    private static final long TOPIC_COOLDOWN_TICKS = 6000L;

    /** Severity threshold for rumor creation. Only notable events seed rumors. */
    private static final float RUMOR_SEVERITY_THRESHOLD = 0.5f;

    /** Cooldown: same topic prefix won't seed a new rumor within this many ticks.
     *  12000 ticks = 10 minutes. Prevents duplicate rumors from repeated events. */
    private static final long RUMOR_TOPIC_COOLDOWN_TICKS = 12000L;

    /**
     * Last recording time per actor+topicPrefix. Prevents memory spam.
     * Key: "actorId|topicPrefix" → last tick recorded.
     */
    private final java.util.Map<String, Long> lastRecorded = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * Last rumor creation time per topicPrefix. Prevents duplicate rumors.
     * Key: "topicPrefix" → last tick a rumor was created.
     */
    private final java.util.Map<String, Long> lastRumorCreated = new java.util.concurrent.ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        // Subscribe to ALL events — same as ActivityInterruptionSubscriber.
        // We filter by severity inside onEvent.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event.severity() < MIN_REMEMBER_SEVERITY) return;
        if (event.timestamp() <= 0) return;

        // Extract a topic prefix for cooldown deduplication.
        // "beast.wolf_pack.stalking" → "beast.wolf_pack"
        // "npc.breakthrough" → "npc.breakthrough"
        // "opportunity.spirit_fruit.ripe" → "opportunity.spirit_fruit"
        String topicPrefix = extractTopicPrefix(event.topic());

        for (Actor a : ActorRegistry.all()) {
            // Only record for linked actors (they have entities in the world).
            if (!ActorEntityLink.isLinked(a.id)) continue;

            // Distance check.
            double distSq = Math.pow(a.blockX - event.pos().getX(), 2)
                          + Math.pow(a.blockZ - event.pos().getZ(), 2);
            if (distSq > (double) MAX_PERCEPTION_DISTANCE * MAX_PERCEPTION_DISTANCE) continue;

            // Cooldown check: don't spam memories for the same event type.
            String cooldownKey = a.id + "|" + topicPrefix;
            Long lastTick = lastRecorded.get(cooldownKey);
            if (lastTick != null && (event.timestamp() - lastTick) < TOPIC_COOLDOWN_TICKS) {
                continue;
            }
            lastRecorded.put(cooldownKey, event.timestamp());

            // Determine memory category from event topic.
            NpcCognitiveMemory.MemoryCategory category = mapTopicToCategory(event.topic());

            // Determine memory tier from event severity.
            NpcCognitiveMemory.MemoryTier tier = mapSeverityToTier(event.severity());

            // Build description from event.
            String description = buildDescription(event, a.id);

            // Record the memory!
            NpcMemoryTickHandler.recordShortTerm(
                    a.id,
                    category,
                    description,
                    event.topic(),
                    event.timestamp()
            );

            // If this should be a higher tier, promote it.
            if (tier == NpcCognitiveMemory.MemoryTier.MEDIUM_TERM) {
                // Re-record as medium-term. The short-term entry will eventually decay.
                NpcMemoryTickHandler.recordMediumTerm(
                        a.id,
                        category,
                        description,
                        event.topic(),
                        event.timestamp()
                );
            } else if (tier == NpcCognitiveMemory.MemoryTier.LONG_TERM) {
                NpcMemoryTickHandler.recordLongTerm(
                        a.id,
                        category,
                        description,
                        event.topic()
                );
            }

            Ergenverse.LOGGER.debug("[NpcMemory] {} remembered event '{}' as {} ({})",
                    a.id, event.topic(), tier, category);
        }

        // Art XXXI.5: Seed a rumor from high-severity events.
        // This closes G1: events now birth rumors that propagate through NPCs.
        // Only create one rumor per event (not per-actor), with topic cooldown.
        if (event.severity() >= RUMOR_SEVERITY_THRESHOLD) {
            String rumorCooldownKey = topicPrefix;
            Long lastRumorTick = lastRumorCreated.get(rumorCooldownKey);
            if (lastRumorTick == null
                    || (event.timestamp() - lastRumorTick) >= RUMOR_TOPIC_COOLDOWN_TICKS) {
                lastRumorCreated.put(rumorCooldownKey, event.timestamp());
                seedRumorFromEvent(event);
            }
        }
    }

    /**
     * Create a rumor from a high-severity world event.
     * Maps the event topic to a Rumor.OriginType and calls the
     * appropriate RumorEngineEvents creation method.
     *
     * <p>Not a new engine (Art XXVI): calls existing RumorNetwork
     * creation API through RumorEngineEvents static methods.
     */
    private void seedRumorFromEvent(WorldEvent event) {
        String locationHint = "x=" + (int) event.pos().getX()
                + ",z=" + (int) event.pos().getZ();
        Rumor.OriginType originType = mapTopicToRumorOrigin(event.topic());

        try {
            switch (originType) {
                case COMBAT_EVENT ->
                    RumorEngineEvents.createCombatRumor(
                            event.description(), "", locationHint, event.timestamp());
                case SPIRIT_EVENT ->
                    RumorEngineEvents.createOpportunityRumor(
                            event.description(), locationHint, event.timestamp());
                case PLAYER_ACTION ->
                    RumorEngineEvents.createPlayerActionRumor(
                            event.description(), "", locationHint, event.timestamp());
                default ->
                    RumorEngineEvents.createEnvironmentalRumor(
                            event.description(), locationHint, event.timestamp());
            }
            Ergenverse.LOGGER.debug("[NpcMemory] Seeded rumor from event '{}'",
                    event.topic());
        } catch (Exception e) {
            // Rumor creation is best-effort — don't let it break memory recording.
            Ergenverse.LOGGER.debug("[NpcMemory] Failed to seed rumor from '{}': {}",
                    event.topic(), e.getMessage());
        }
    }

    /**
     * Map event topic to a Rumor.OriginType for rumor creation.
     */
    private static Rumor.OriginType mapTopicToRumorOrigin(String topic) {
        if (topic.startsWith("beast.")) return Rumor.OriginType.ENVIRONMENTAL;
        if (topic.contains("death") || topic.contains("combat")
                || topic.contains("attack"))
            return Rumor.OriginType.COMBAT_EVENT;
        if (topic.startsWith("opportunity.")) return Rumor.OriginType.SPIRIT_EVENT;
        if (topic.startsWith("player.")) return Rumor.OriginType.PLAYER_ACTION;
        if (topic.startsWith("sect.")) return Rumor.OriginType.FACTION_EVENT;
        return Rumor.OriginType.OTHER;
    }

    /**
     * Extract a 2-segment topic prefix for cooldown deduplication.
     * "beast.wolf_pack.stalking" → "beast.wolf_pack"
     * "npc.breakthrough" → "npc.breakthrough" (no third segment)
     */
    private static String extractTopicPrefix(String topic) {
        int first = topic.indexOf('.');
        if (first < 0) return topic;
        int second = topic.indexOf('.', first + 1);
        if (second < 0) return topic;
        return topic.substring(0, second);
    }

    /**
     * Map event topic to memory category.
     */
    private static NpcCognitiveMemory.MemoryCategory mapTopicToCategory(String topic) {
        if (topic.startsWith("beast.")) return NpcCognitiveMemory.MemoryCategory.WORLD_EVENT;
        if (topic.startsWith("opportunity.")) return NpcCognitiveMemory.MemoryCategory.OBSERVATION;
        if (topic.startsWith("npc.")) {
            if (topic.contains("death") || topic.contains("combat") || topic.contains("attack"))
                return NpcCognitiveMemory.MemoryCategory.COMBAT;
            if (topic.contains("breakthrough") || topic.contains("cultivation"))
                return NpcCognitiveMemory.MemoryCategory.CULTIVATION;
            return NpcCognitiveMemory.MemoryCategory.SOCIAL;
        }
        if (topic.startsWith("rumor.")) return NpcCognitiveMemory.MemoryCategory.RUMOR;
        if (topic.startsWith("sect.")) return NpcCognitiveMemory.MemoryCategory.WORLD_EVENT;
        if (topic.startsWith("karma.")) return NpcCognitiveMemory.MemoryCategory.OBSERVATION;
        if (topic.startsWith("spirit_vein.")) return NpcCognitiveMemory.MemoryCategory.OBSERVATION;
        if (topic.startsWith("collaboration.") || topic.startsWith("social."))
            return NpcCognitiveMemory.MemoryCategory.SOCIAL;
        return NpcCognitiveMemory.MemoryCategory.OBSERVATION;
    }

    /**
     * Map event severity to memory tier.
     */
    private static NpcCognitiveMemory.MemoryTier mapSeverityToTier(float severity) {
        if (severity >= LONG_TERM_SEVERITY) return NpcCognitiveMemory.MemoryTier.LONG_TERM;
        if (severity >= MEDIUM_TERM_SEVERITY) return NpcCognitiveMemory.MemoryTier.MEDIUM_TERM;
        return NpcCognitiveMemory.MemoryTier.SHORT_TERM;
    }

    /**
     * Build a human-readable memory description from an event.
     * Format: "Witnessed [description]" for OBSERVATION,
     *         "[description]" for WORLD_EVENT (more impactful).
     */
    private static String buildDescription(WorldEvent event, String actorId) {
        NpcCognitiveMemory.MemoryCategory cat = mapTopicToCategory(event.topic());
        if (cat == NpcCognitiveMemory.MemoryCategory.OBSERVATION) {
            return "Witnessed: " + event.description();
        }
        return event.description();
    }

    /**
     * Clear cooldown state (called on world unload to prevent stale keys).
     */
    public void clearCooldowns() {
        lastRecorded.clear();
    }
}