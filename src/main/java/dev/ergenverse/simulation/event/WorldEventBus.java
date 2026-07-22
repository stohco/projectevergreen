package dev.ergenverse.simulation.event;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.WorldHistory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * WorldEventBus — the nervous system of the simulation.
 *
 * <p>Per the ChatGPT architectural review (the #1 directive):
 * <pre>
 *   "build one enormous event network"
 *
 *   Spirit Vein weakened
 *     → Qi Density changed
 *     → Herbs grow slower
 *     → Beasts migrate
 *     → Sect notices
 *     → Outer disciples investigate
 *     → Bandits ambush disciples
 *     → Player discovers corpses
 *     → Soul cultivator harvests souls
 *     → Karma changes
 *     → Wang Lin notices
 *
 *   No quests. No scripts. Just consequences.
 * </pre>
 *
 * <p>The bus is the single point through which all world disturbances
 * flow. Any system can {@link #publish} an event; any system can
 * {@link #subscribe} to events by topic prefix. The bus handles:
 * <ul>
 *   <li><b>Routing</b> — delivers each event only to subscribers whose
 *       topicPrefix matches the event's topic.</li>
 *   <li><b>Write-through</b> — events with severity ≥
 *       {@link #LEDGER_SEVERITY_THRESHOLD} are written to
 *       {@link WorldHistory} so that "weeks later NPCs still reference
 *       the event" (ENDGAME PROOF link j).</li>
 *   <li><b>Thread safety</b> — subscribers are stored in concurrent
 *       collections. Dispatch is synchronous on the server thread.</li>
 * </ul>
 *
 * <h2>Topic naming convention</h2>
 * <pre>
 *   opportunity.spirit_fruit.ripe
 *   opportunity.restriction_cave.discovered
 *   opportunity.claimed
 *   npc.breakthrough
 *   npc.death
 *   npc.move
 *   beast.panic
 *   beast.migrate
 *   rumor.spread
 *   artifact.refined
 *   sect.founded
 *   sect.war_declared
 *   karma.shift
 *   spirit_vein.weakened
 * </pre>
 *
 * <p><b>Constitution compliance:</b>
 * <ul>
 *   <li>Art III — events represent world state changes, never player
 *       progression triggers.</li>
 *   <li>Art V — events fire regardless of player proximity. LOD is
 *       handled by subscribers (e.g. distant beasts use statistical
 *       response, nearby beasts use entity AI).</li>
 *   <li>Art XV — every event carries a canonSource for provenance.</li>
 *   <li>Art XVI — ONE bus, infinite subscribers. No per-system event
 *       channels.</li>
 *   <li>Art XVIII — the bus is one deep system, not many shallow ones.</li>
 * </ul>
 */
public final class WorldEventBus {

    /**
     * Events with severity ≥ this threshold are written to WorldHistory.
     * 0.45 = "historically notable". A spirit fruit ripening (0.6),
     * an NPC breakthrough (0.7), a sect war (0.9) all exceed this.
     * A beast wandering 10 blocks (0.05) does not.
     */
    public static final float LEDGER_SEVERITY_THRESHOLD = 0.45f;

    /** Subscribers keyed by topic prefix for O(1) prefix lookup. */
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<WorldEventSubscriber>>
            subscribersByPrefix = new ConcurrentHashMap<>();

    /** The current server level (set on first tick, cleared on world unload). */
    private static ServerLevel currentLevel;

    private WorldEventBus() {}

    // ─── Subscription ───

    /**
     * Register a subscriber. The subscriber will receive all events whose
     * topic starts with {@code subscriber.topicPrefix()}.
     *
     * <p>Safe to call at any time (during mod init, during server tick, etc.).
     * The subscriber is added to a copy-on-write list — existing dispatch
     * iterations are not affected.
     *
     * @param subscriber the subscriber to register
     */
    public static void subscribe(WorldEventSubscriber subscriber) {
        String prefix = subscriber.topicPrefix();
        subscribersByPrefix
                .computeIfAbsent(prefix, k -> new CopyOnWriteArrayList<>())
                .addIfAbsent(subscriber);
        Ergenverse.LOGGER.debug("[Ergenverse] EventBus: subscribed {} to prefix '{}'",
                subscriber.getClass().getSimpleName(), prefix);
    }

    /**
     * Unregister a subscriber.
     *
     * @param subscriber the subscriber to remove
     */
    public static void unsubscribe(WorldEventSubscriber subscriber) {
        String prefix = subscriber.topicPrefix();
        CopyOnWriteArrayList<WorldEventSubscriber> list = subscribersByPrefix.get(prefix);
        if (list != null) {
            list.remove(subscriber);
        }
    }

    // ─── Publication ───

    /**
     * Publish an event to all matching subscribers.
     *
     * <p>This is the SINGLE entry point through which all world disturbances
     * flow. Calling this method:
     * <ol>
     *   <li>Finds all subscribers whose topicPrefix matches the event's topic.</li>
     *   <li>Calls {@link WorldEventSubscriber#onEvent} on each, synchronously.</li>
     *   <li>If severity ≥ {@link #LEDGER_SEVERITY_THRESHOLD}, writes the event
     *       to {@link WorldHistory} (write-through).</li>
     * </ol>
     *
     * <p><b>Performance:</b> Dispatch is O(subscribers_matching_prefix).
     * For a typical event with 2-5 matching subscribers, this is sub-millisecond.
     * The write-through to WorldHistory is O(1) (append to ring buffer).
     *
     * @param event the event to publish
     */
    public static void dispatch(WorldEvent event) {
        if (event == null) return;

        // (1) Deliver to all matching subscribers.
        // We check every prefix group because a subscriber with prefix "opportunity."
        // should receive "opportunity.spirit_fruit.ripe", and a subscriber with
        // prefix "" (catch-all) should also receive it.
        for (var entry : subscribersByPrefix.entrySet()) {
            String prefix = entry.getKey();
            if (event.topic().startsWith(prefix)) {
                for (WorldEventSubscriber sub : entry.getValue()) {
                    try {
                        sub.onEvent(event);
                    } catch (Exception e) {
                        // A subscriber throwing MUST NOT break the bus or other subscribers.
                        Ergenverse.LOGGER.error(
                                "[Ergenverse] EventBus: subscriber {} threw on event {}",
                                sub.getClass().getSimpleName(), event.topic(), e);
                    }
                }
            }
        }

        // (2) Write-through to WorldHistory for historically notable events.
        if (event.severity() >= LEDGER_SEVERITY_THRESHOLD && currentLevel != null) {
            try {
                String historyType = mapTopicToHistoryType(event.topic());
                String regionId = event.isGlobal() ? "global" : ("r_" + event.pos().getX() / 256
                        + "_" + event.pos().getZ() / 256);
                WorldHistory.recordGlobalWithTopic(
                        currentLevel,
                        event.timestamp(),
                        historyType,
                        regionId,
                        event.description(),
                        event.canonSource(),
                        event.topic(),
                        event.isGlobal() ? WorldHistory.NO_POS : event.pos().getX(),
                        event.isGlobal() ? WorldHistory.NO_POS : event.pos().getZ()
                );
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] EventBus: WorldHistory write-through failed", e);
            }
        }
    }

    /**
     * Convenience: publish a positioned event.
     */
    public static void publish(String topic, EnergyType type, BlockPos pos,
                                float intensity, String desc, String canon, long tick) {
        dispatch(WorldEvent.of(topic, type, pos, intensity, desc, canon, tick));
    }

    /**
     * Convenience: publish a global (positionless) event.
     */
    public static void publishGlobal(String topic, EnergyType type,
                                      float intensity, String desc, String canon, long tick) {
        dispatch(WorldEvent.global(topic, type, intensity, desc, canon, tick));
    }

    // ─── Helpers ───

    /**
     * Map a bus topic to a WorldHistory event type string.
     * e.g. "opportunity.spirit_fruit.ripe" → "OPPORTUNITY_EVENT"
     *      "npc.breakthrough" → "NPC_EVENT"
     */
    private static String mapTopicToHistoryType(String topic) {
        if (topic.startsWith("opportunity.")) return "OPPORTUNITY_EVENT";
        if (topic.startsWith("player.")) return "PLAYER_ACTION";
        if (topic.startsWith("actor.")) return "ACTOR_ACTION";
        if (topic.startsWith("semantic.")) return "SEMANTIC_EVENT";
        if (topic.startsWith("npc.")) return "NPC_EVENT";
        if (topic.startsWith("beast.")) return "BEAST_EVENT";
        if (topic.startsWith("rumor.")) return "RUMOR_EVENT";
        if (topic.startsWith("artifact.")) return "ARTIFACT_EVENT";
        if (topic.startsWith("sect.")) return "SECT_EVENT";
        if (topic.startsWith("karma.")) return "KARMA_EVENT";
        if (topic.startsWith("spirit_vein.")) return "SPIRIT_VEIN_EVENT";
        if (topic.startsWith("collaboration.")) return "SOCIAL_EVENT";
        return "WORLD_EVENT";
    }

    // ─── Diagnostics ───

    /**
     * Get the total number of registered subscribers (all prefixes).
     */
    public static int subscriberCount() {
        int count = 0;
        for (var list : subscribersByPrefix.values()) {
            count += list.size();
        }
        return count;
    }

    /**
     * Get a diagnostic snapshot of all registered subscriber prefixes
     * and their counts. For the /ergen eventbus status command.
     */
    public static List<String> diagnosticSnapshot() {
        List<String> lines = new ArrayList<>();
        lines.add("WorldEventBus subscribers (" + subscriberCount() + " total):");
        for (var entry : subscribersByPrefix.entrySet()) {
            lines.add("  prefix='" + entry.getKey() + "' → " + entry.getValue().size() + " subscriber(s)");
            for (WorldEventSubscriber sub : entry.getValue()) {
                lines.add("    - " + sub.getClass().getSimpleName());
            }
        }
        return lines;
    }

    /**
     * Clear all subscribers. Called on world unload to prevent stale
     * references between server sessions.
     */
    public static void clearAll() {
        subscribersByPrefix.clear();
        currentLevel = null;
    }

    /** Set the current server level (for subscribers that need world access). */
    public static void setCurrentLevel(ServerLevel level) {
        currentLevel = level;
    }

    /** Get the current server level (may be null before first tick). */
    public static ServerLevel currentLevel() {
        return currentLevel;
    }
}
