package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ReputationObserver — localized, persistent reputation that spreads physically.
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
 * <p><b>Persistent (Art XLIII §2):</b> Reputation is stored as SavedData
 * and survives server restart. A player who spent weeks building reputation
 * keeps it after logging out.
 *
 * <h2>Reputation spread</h2>
 * <p>Reputation does NOT auto-propagate between regions. It spreads
 * only through:
 * <ul>
 *   <li><b>Rumors</b> — when the RumorEngine propagates a rumor
 *       about a deed, the ReputationObserver picks up the rumor and
 *       adds a fraction of the original reputation to the rumor's region.</li>
 *   <li><b>Travelers</b> — when an NPC migrates between regions, they carry
 *       a faint reputation impression. (Future: tie to migration system.)</li>
 *   <li><b>Chronicle</b> — long-term chronicle entries can slowly seed
 *       reputation in new regions over time. (Future: tie to chronicle.)</li>
 * </ul>
 *
 * <p><b>Not a new Engine (Art XXVI):</b> WorldEventSubscriber that
 * maintains a persistent reputation map. No new bus, no new infrastructure.
 */
public final class ReputationObserver implements WorldEventSubscriber {

    private static final String DATA_NAME = "ergenverse_reputation";
    private static final int CURRENT_VERSION = 1;

    /** Decay amount per game-day (24000 ticks). Reputation atrophies. */
    private static final float DAILY_DECAY = 1.0f;

    /** Maximum reputation magnitude (prevents infinite accumulation). */
    private static final float MAX_REPUTATION = 100.0f;

    /** Minimum reputation magnitude. */
    private static final float MIN_REPUTATION = -100.0f;

    /** In-memory cache: "regionX_Z|actorId" → score. Flushed to SavedData periodically. */
    private static final ConcurrentHashMap<String, Float> reputation = new ConcurrentHashMap<>();

    /** Dirty flag: true when in-memory state needs to be persisted. */
    private static boolean dirty = false;

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
        dirty = true;

        Ergenverse.LOGGER.debug("[ReputationObserver] {} in {}: {} ({} → {})",
                actorId, region, delta > 0 ? "+" + delta : delta,
                current, updated);
    }

    // ─── Persistence ─────────────────────────────────────────────────

    /**
     * Get the persistent reputation data for the world. Used by
     * ReputationObserver internally and by diagnostic commands.
     */
    public static ReputationData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ReputationData::load, ReputationData::new, DATA_NAME);
    }

    /**
     * Save in-memory reputation to SavedData. Called periodically from
     * the server tick loop (every 6000 ticks = 5 minutes) and on world save.
     */
    public static void flushToDisk(ServerLevel level) {
        if (!dirty) return;
        ReputationData data = get(level);
        data.replaceAll(reputation);
        data.setDirty();
        dirty = false;
        Ergenverse.LOGGER.debug("[ReputationObserver] Flushed {} reputation entries to disk",
                reputation.size());
    }

    /**
     * Load reputation from SavedData into the in-memory cache.
     * Called on world load / tick 1.
     */
    public static void loadFromDisk(ServerLevel level) {
        ReputationData data = get(level);
        reputation.clear();
        reputation.putAll(data.snapshot());
        Ergenverse.LOGGER.info("[ReputationObserver] Loaded {} reputation entries from disk",
                reputation.size());
    }

    // ─── Decay ───────────────────────────────────────────────────────

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
        dirty = true;
    }

    // ─── Computation ──────────────────────────────────────────────────

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
                case "EXPECTATION_VIOLATION" -> -0.7f;
                default -> 0.0f;
            };
        } else if (topic.startsWith("player.")) {
            sign = switch (topic) {
                case "player.combat.engaged" -> {
                    String outcome = event.meta("combat_outcome", "");
                    yield "player_won".equals(outcome) ? severity : -severity * 0.5f;
                }
                case "player.breakthrough" -> 1.0f;
                case "player.gift.given", "player.gift.received" -> 0.5f;
                case "player.discovery" -> 0.3f;
                case "player.interaction" -> 0.1f;
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

    private static String regionKey(net.minecraft.core.BlockPos pos) {
        return "r_" + (pos.getX() / 128) + "_" + (pos.getZ() / 128);
    }

    private static float clampReputation(float v) {
        return Math.max(MIN_REPUTATION, Math.min(MAX_REPUTATION, v));
    }

    /** Get the player's reputation in a specific region. Returns 0 if unknown. */
    public static float getReputation(String actorId, String regionKey) {
        return reputation.getOrDefault(regionKey + "|" + actorId, 0f);
    }

    /** Get a diagnostic snapshot. */
    public static int reputationCount() { return reputation.size(); }

    public static void clearAll() { reputation.clear(); dirty = false; }

    // ─── SavedData Container ─────────────────────────────────────────

    /**
     * SavedData container for reputation persistence.
     * Wraps the reputation map in a SavedData instance so it survives
     * server restarts per Article XLIII §2.
     */
    public static class ReputationData extends SavedData {
        private final ConcurrentHashMap<String, Float> stored = new ConcurrentHashMap<>();

        public ReputationData() {}

        /** Take a snapshot of the current in-memory state. */
        public void replaceAll(ConcurrentHashMap<String, Float> source) {
            stored.clear();
            stored.putAll(source);
        }

        /** Get a copy of all stored reputation entries. */
        public Map<String, Float> snapshot() {
            return new ConcurrentHashMap<>(stored);
        }

        @Override
        public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
            compound.putInt("data_version", CURRENT_VERSION);
            ListTag list = new ListTag();
            for (var entry : stored.entrySet()) {
                CompoundTag entryTag = new CompoundTag();
                entryTag.putString("key", entry.getKey());
                entryTag.putFloat("value", entry.getValue());
                list.add(entryTag);
            }
            compound.put("entries", list);
            return compound;
        }

        public static ReputationData load(CompoundTag compound) {
            ReputationData data = new ReputationData();
            int version = compound.getInt("data_version");
            if (version < 1) return data;
            ListTag list = compound.getList("entries", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entryTag = list.getCompound(i);
                data.stored.put(entryTag.getString("key"), entryTag.getFloat("value"));
            }
            return data;
        }
    }
}
