package dev.ergenverse.npc.memory;

import dev.ergenverse.core.Ergenverse;
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

    /**
     * Last recording time per actor+topicPrefix. Prevents memory spam.
     * Key: "actorId|topicPrefix" → last tick recorded.
     */
    private final java.util.Map<String, Long> lastRecorded = new java.util.concurrent.ConcurrentHashMap<>();

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