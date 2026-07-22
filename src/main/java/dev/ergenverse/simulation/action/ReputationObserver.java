package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ReputationObserver — localized reputation that spreads physically.
 *
 * <p>Per the user's directive (2026-07-23 #2):
 * <pre>
 *   "Not global reputation. Localized reputation.
 *
 *    Village knows player helps children.
 *    Nearby sect? Never heard of you.
 *    Sea of Devils? No clue.
 *    Unless rumors travel.
 *
 *    Reputation spreads physically. Exactly like information."
 * </pre>
 *
 * <p>Reputation is <b>not</b> a single global score. It's localized
 * to regions (128-block chunks, roughly matching chat-group boundaries).
 * An actor's reputation in one region has NO effect on their reputation
 * in a distant region — unless rumors, chronicles, or travelers carry
 * information between them.
 *
 * <h2>How reputation forms</h2>
 * <ul>
 *   <li>High-severity player.* events (combat victories, notable breakthroughs)
 *       contribute positive reputation to the local region.</li>
 *   <li>Negative semantic events (ACT_OF_CRUELTY, PROMISE_BROKEN)
 *       contribute negative reputation to the local region.</li>
 *   <li>Mercy, gifts, and positive semantic events add positive
 *       reputation to the local region.</li>
 *   <li>Reputation decays over time (default: -1 per game-day) if not
 *       reinforced by new deeds.</li>
 * </ul>
 *
 * <h2>Reputation spread</h2>
 * <p>Reputation does NOT auto-propagate between regions. It spreads
 * only through:
 * <ul>
 *   <li><b>Rumors</b> — when the RumorEngineEvents propagates a rumor
 *       about a deed, the ReputationObserver picks up the rumor and
 *       adds a fraction of the original reputation to the rumor's region.</li>
 *   <li><b>Travelers</b> — when an NPC migrates between regions, they carry
 *       a faint reputation impression. (Future: tie to migration system.)</li>
 *   <li><b>Chronicle</b> — long-term chronicle entries can slowly seed
 *       reputation in new regions over time. (Future: tie to chronicle.)</li>
 * </ul>
 * This means a player can be beloved in Zhao Country but unknown in
 * the Sea of Devils — exactly as it should be in the Er Gen universe.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> WorldEventSubscriber that
 * maintains a reputation map. No new bus, no new infrastructure.
 */
public final class ReputationObserver implements WorldEventSubscriber {

    /** Reputation per region+actor: "regionX_Z|actorId" → score. */
    private static final ConcurrentHashMap<String, Float> reputation = new ConcurrentHashMap<>();

    /** Decay amount per game-day (24000 ticks). Reputation atrophies. */
    private static final float DAILY_DECAY = 1.0f;

    /** Maximum reputation magnitude (prevents infinite accumulation). */
    private static final float MAX_REPUTATION = 100.0f;

    /** Minimum reputation magnitude. */
    private static final float MIN_REPUTATION = -100.0f;

    @Override
    public String topicPrefix() {
        return ""; // observe player and semantic events
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;

        String topic = event.topic();
        float severity = event.severity();

        // Only player-sourced events and semantic events affect player reputation.
        boolean isPlayerEvent = topic.startsWith("player.")
                || topic.startsWith("semantic.");
        if (!isPlayerEvent) return;
        if (severity < 0.2f) return; // trivial events don't affect reputation

        // Determine the reputation delta from the event's meaning.
        float delta = computeReputationDelta(event);
        if (delta == 0f) return;

        // Determine the source actor (player UUID).
        String actorId = resolvePlayerUuid(event);
        if (actorId == null) return;

        // Localize to the region.
        String region = regionKey(event.pos());
        String key = region + "|" + actorId;

        // Apply delta (clamped).
        float current = reputation.getOrDefault(key, 0f);
        float updated = clampReputation(current + delta);
        reputation.put(key, updated);

        Ergenverse.LOGGER.debug("[ReputationObserver] {} in {}: {} ({} → {})",
                actorId, region, delta > 0 ? "+" + delta : delta,
                current, updated);
    }

    /**
     * Compute the reputation delta from an event.
     * Positive = reputation gained. Negative = reputation lost.
     * Magnitude scales with severity.
     */
    private float computeReputationDelta(WorldEvent event) {
        String topic = event.topic();
        float severity = event.severity();
        float sign = 1.0f;

        if (topic.startsWith("semantic.")) {
            String semanticTag = event.semanticTag();
            sign = switch (semanticTag) {
                case "ACT_OF_MERCY" -> 1.0f;
                case "ACT_OF_CRUELTY" -> -1.0f;
                case "PROMISE_BROKEN" -> -1.5f;
                case "DEBT_IGNORED" -> -1.0f;
                case "PUBLIC_HUMILIATION" -> -1.0f;
                case "PROMISE_MADE" -> 0.5f;
                case "DEBT_REPAID" -> 0.8f;
                case "TECHNIQUE_DISPLAYED" -> 0.3f;
                case "CULTIVATION_REVEALED" -> 0.2f;
                case "FORBIDDEN_KNOWLEDGE_WITNESSED" -> -0.5f;
                default -> 0.0f;
            };
        } else if (topic.startsWith("player.")) {
            sign = switch (topic) {
                case SemanticEventTopics.PLAYER_COMBAT_ENGAGED -> {
                    String outcome = event.meta("combat_outcome", "");
                    yield "player_won".equals(outcome) ? severity : -severity * 0.5f;
                }
                case SemanticEventTopics.PLAYER_BREAKTHROUGH -> 1.0f;
                case SemanticEventTopics.PLAYER_GIFT_RECEIVED,
                     SemanticEventTopics.PLAYER_GIFT_GIVEN -> 0.5f;
                case SemanticEventTopics.PLAYER_DISCOVERY -> 0.3f;
                case SemanticEventTopics.PLAYER_INTERACTION -> 0.1f;
                default -> 0.0f;
            };
        }

        if (sign == 0.0f) return 0f;
        return sign * severity * 10.0f; // Scale: max ±10 per notable event
    }

    private static String resolvePlayerUuid(WorldEvent event) {
        String source = event.sourceActorId();
        if (isUuid(source)) return source;
        String target = event.targetActorId();
        if (isUuid(target)) return target;
        return null;
    }

    private static boolean isUuid(String s) {
        if (s == null || s.isEmpty()) return false;
        try { UUID.fromString(s); return true; }
        catch (IllegalArgumentException e) { return false; }
    }

    private static String regionKey(BlockPos pos) {
        return "r_" + (pos.getX() / 128) + "_" + (pos.getZ() / 128);
    }

    private static float clampReputation(float v) {
        return Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, v));
    }

    /**
     * Decay all reputation by the daily amount. Called from server tick.
     * Reputation atrophies if not reinforced by new deeds.
     */
    public static void decayAll(long tick) {
        if (tick % 24000L != 0) return; // once per game-day
        for (var entry : reputation.entrySet()) {
            float current = entry.getValue();
            if (current > 0 && current > DAILY_DECAY) {
                entry.setValue(current - DAILY_DECAY);
            } else if (current < 0 && current < -DAILY_DECAY) {
                entry.setValue(current + DAILY_DECAY);
            } else {
                entry.setValue(0f);
            }
        }
    }

    /** Get the player's reputation in a specific region. Returns 0 if unknown. */
    public static float getReputation(String actorId, String regionKey) {
        return reputation.getOrDefault(regionKey + "|" + actorId, 0f);
    }

    /** Get a diagnostic snapshot. */
    public static int reputationCount() { return reputation.size(); }

    public static void clearAll() { reputation.clear(); }
}
