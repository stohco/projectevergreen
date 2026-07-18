package dev.ergenverse.history;

import dev.ergenverse.core.Ergenverse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WorldHistory — emergent changes to the world caused by the player AND by canon consequences (Layer 3).
 *
 * <p>The living world is the player's opportunity engine. Wang Lin's canon actions
 * created consequences — abandoned sects, scattered treasures, fractured factions,
 * wandering beasts. These consequences are NEW opportunities that only exist
 * because canon happened. This class tracks them, plus any world changes the
 * player causes.
 *
 * <h2>Examples of world-history events</h2>
 * <ul>
 *   <li><b>CANON_CONSEQUENCE</b>: "Heng Yue Sect destroyed by Teng Huayuan" →
 *       abandoned library, fractured formation, scattered disciples now exist
 *       as explorable opportunities.</li>
 *   <li><b>PLAYER_ACTION</b>: "Player cleared the abandoned library" →
 *       the opportunity is consumed; the world state updates.</li>
 *   <li><b>ECOLOGY_SHIFT</b>: "Player exterminated herbivores in region X" →
 *       predators starve, herbs spread, nearby sects notice.</li>
 *   <li><b>FACTION_CHANGE</b>: "Sect elder killed in player conflict" →
 *       succession crisis, power vacuum.</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * <p>World history is GLOBAL (not per-player). Stored in
 * {@link dev.ergenverse.simulation.WorldRuntimeState} under a reserved key
 * ({@code "_world_history"} in the player_mutations map — using a reserved
 * key that can never be a real UUID). This is loaded on demand.
 *
 * <p>At server start, canon-consequence events are seeded from
 * {@link dev.ergenverse.cultivation.RITimelineEngine} to reflect the
 * novel's timeline state.
 */
public final class WorldHistory {

    // ─── NBT keys ─────────────────────────────────────────────────────
    private static final String TAG_WORLD_HISTORY = "_world_history";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_EVENT_TYPE = "event_type";
    private static final String TAG_REGION_ID = "region_id";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_CANON_SOURCE = "canon_source";
    private static final String TAG_TOPIC = "topic";        // v2: bus topic for routing
    private static final String TAG_POS_X = "pos_x";        // v2: event X coord
    private static final String TAG_POS_Z = "pos_z";        // v2: event Z coord

    /** Maximum global events (prevents unbounded growth across all players). */
    private static final int MAX_GLOBAL_EVENTS = 2000;
    /** Maximum events per region query. */
    private static final int MAX_REGION_EVENTS = 200;

    /** Sentinel for "no topic" (pre-v2 events or events without a bus topic). */
    public static final String NO_TOPIC = "";
    /** Sentinel for "no position" (global events or pre-v2 events). */
    public static final int NO_POS = Integer.MIN_VALUE;

    /** A single world-history event. */
    public record WorldEvent(
            long timestamp,
            String eventType,    // "CANON_CONSEQUENCE", "PLAYER_ACTION", "ECOLOGY_SHIFT", "FACTION_CHANGE", etc.
            String regionId,     // the world region affected (e.g. "zhao_country", "heng_yue_sect")
            String description,
            String canonSource,  // if CANON_CONSEQUENCE, the canon event that caused it
            String topic,        // v2: the WorldEventBus topic (e.g. "opportunity.spirit_fruit.ripe")
            int posX,            // v2: event X block coord (NO_POS if unknown)
            int posZ             // v2: event Z block coord (NO_POS if unknown)
    ) {
        public WorldEvent {
            // Validate / default nulls without reassigning (records auto-assign)
            if (eventType == null) throw new IllegalArgumentException("eventType cannot be null");
            if (regionId == null) throw new IllegalArgumentException("regionId cannot be null");
            if (description == null) throw new IllegalArgumentException("description cannot be null");
            if (canonSource == null) throw new IllegalArgumentException("canonSource cannot be null");
            if (topic == null) topic = NO_TOPIC;
        }

        /** Factory that applies defaults for nullable fields (v1 compat — no topic/pos). */
        public static WorldEvent withDefaults(long timestamp, String eventType, String regionId,
                                               String description, String canonSource) {
            return new WorldEvent(timestamp,
                    eventType != null ? eventType : "PLAYER_ACTION",
                    regionId != null ? regionId : "",
                    description != null ? description : "",
                    canonSource != null ? canonSource : "",
                    NO_TOPIC, NO_POS, NO_POS);
        }

        /** Factory with topic and position (v2 — for WorldEventBus write-through). */
        public static WorldEvent withTopic(long timestamp, String eventType, String regionId,
                                            String description, String canonSource,
                                            String topic, int posX, int posZ) {
            return new WorldEvent(timestamp,
                    eventType != null ? eventType : "PLAYER_ACTION",
                    regionId != null ? regionId : "",
                    description != null ? description : "",
                    canonSource != null ? canonSource : "",
                    topic != null ? topic : NO_TOPIC,
                    posX, posZ);
        }

        /** Whether this event has a bus topic. */
        public boolean hasTopic() { return !topic.equals(NO_TOPIC); }
        /** Whether this event has a known position. */
        public boolean hasPosition() { return posX != NO_POS && posZ != NO_POS; }
    }

    // ─── Instance fields ────────────────────────────────────────────
    private final List<WorldEvent> events = new ArrayList<>();

    private WorldHistory() {}

    // ─── Access ──────────────────────────────────────────────────────

    /** Get all events (oldest first). */
    public List<WorldEvent> all() {
        return Collections.unmodifiableList(events);
    }

    /** Get events for a specific region. */
    public List<WorldEvent> forRegion(String regionId) {
        if (regionId == null || regionId.isEmpty()) return List.of();
        List<WorldEvent> result = new ArrayList<>();
        for (WorldEvent e : events) {
            if (regionId.equals(e.regionId)) result.add(e);
        }
        // Limit results
        if (result.size() > MAX_REGION_EVENTS) {
            result = result.subList(result.size() - MAX_REGION_EVENTS, result.size());
        }
        return result;
    }

    /** Get events of a specific type. */
    public List<WorldEvent> forType(String eventType) {
        if (eventType == null || eventType.isEmpty()) return List.of();
        List<WorldEvent> result = new ArrayList<>();
        for (WorldEvent e : events) {
            if (eventType.equals(e.eventType)) result.add(e);
        }
        return result;
    }

    /** Get only canon-consequence events. */
    public List<WorldEvent> canonConsequences() {
        return forType("CANON_CONSEQUENCE");
    }

    /** Get only player-caused events. */
    public List<WorldEvent> playerActions() {
        return forType("PLAYER_ACTION");
    }

    /** Total number of recorded events. */
    public int size() {
        return events.size();
    }

    // ─── Recording ───────────────────────────────────────────────────

    /** Record a world event. Trims to MAX_GLOBAL_EVENTS if exceeded. */
    public void record(long timestamp, String eventType, String regionId,
                       String description, String canonSource) {
        events.add(WorldEvent.withDefaults(timestamp, eventType, regionId,
                description, canonSource));
        while (events.size() > MAX_GLOBAL_EVENTS) {
            events.remove(0);
        }
    }

    /** Record a world event with topic and position (v2). */
    public void recordWithTopic(long timestamp, String eventType, String regionId,
                                 String description, String canonSource,
                                 String topic, int posX, int posZ) {
        events.add(WorldEvent.withTopic(timestamp, eventType, regionId,
                description, canonSource, topic, posX, posZ));
        while (events.size() > MAX_GLOBAL_EVENTS) {
            events.remove(0);
        }
    }

    // ─── v2 Query API ─────────────────────────────────────────────────

    /** Find events whose topic starts with the given prefix (newest first). */
    public List<WorldEvent> findByTopicPrefix(String prefix, int maxResults) {
        List<WorldEvent> result = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0 && result.size() < maxResults; i--) {
            WorldEvent e = events.get(i);
            if (e.hasTopic() && e.topic().startsWith(prefix)) {
                result.add(e);
            }
        }
        return result;
    }

    /** Find the N most recent events (newest first). */
    public List<WorldEvent> findRecent(int maxResults) {
        List<WorldEvent> result = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0 && result.size() < maxResults; i--) {
            result.add(events.get(i));
        }
        return result;
    }

    /** Find events near a position within radius (newest first). */
    public List<WorldEvent> findNearby(int x, int z, int radius, int maxResults) {
        long rSq = (long) radius * radius;
        List<WorldEvent> result = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0 && result.size() < maxResults; i--) {
            WorldEvent e = events.get(i);
            if (!e.hasPosition()) continue;
            long dx = e.posX() - x;
            long dz = e.posZ() - z;
            if (dx * dx + dz * dz <= rSq) {
                result.add(e);
            }
        }
        return result;
    }

    /** Find recent events matching a specific topic (newest first). */
    public List<WorldEvent> findRecentByTopic(String topic, long minTickAge, int maxResults) {
        List<WorldEvent> result = new ArrayList<>();
        for (int i = events.size() - 1; i >= 0 && result.size() < maxResults; i--) {
            WorldEvent e = events.get(i);
            if (e.hasTopic() && e.topic().equals(topic) && e.timestamp() <= minTickAge) {
                result.add(e);
            }
        }
        return result;
    }

    // ─── Serialization ───────────────────────────────────────────────

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (WorldEvent e : events) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_TIMESTAMP, e.timestamp);
            entry.putString(TAG_EVENT_TYPE, e.eventType);
            entry.putString(TAG_REGION_ID, e.regionId);
            entry.putString(TAG_DESCRIPTION, e.description);
            entry.putString(TAG_CANON_SOURCE, e.canonSource);
            // v2 fields (backward-compatible: old saves just lack these keys)
            if (e.hasTopic()) entry.putString(TAG_TOPIC, e.topic);
            if (e.hasPosition()) {
                entry.putInt(TAG_POS_X, e.posX);
                entry.putInt(TAG_POS_Z, e.posZ);
            }
            list.add(entry);
        }
        tag.put(TAG_EVENTS, list);
        return tag;
    }

    public static WorldHistory load(CompoundTag tag) {
        WorldHistory wh = new WorldHistory();
        if (tag.contains(TAG_EVENTS, Tag.TAG_LIST)) {
            ListTag list = tag.getList(TAG_EVENTS, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                // v2 backward-compatible load: old saves lack topic/pos keys,
                // getString returns "" → NO_TOPIC, getInt returns 0 → translate to NO_POS
                String topic = entry.contains(TAG_TOPIC) ? entry.getString(TAG_TOPIC) : NO_TOPIC;
                int posX = entry.contains(TAG_POS_X) ? entry.getInt(TAG_POS_X) : NO_POS;
                int posZ = entry.contains(TAG_POS_Z) ? entry.getInt(TAG_POS_Z) : NO_POS;
                // Translate legacy 0,0 to NO_POS (0,0 is a valid position but legacy
                // events didn't track position, so 0 means "unknown")
                if (posX == 0 && posZ == 0 && !entry.contains(TAG_POS_X)) {
                    posX = NO_POS;
                    posZ = NO_POS;
                }
                wh.events.add(new WorldEvent(
                        entry.getLong(TAG_TIMESTAMP),
                        entry.getString(TAG_EVENT_TYPE),
                        entry.getString(TAG_REGION_ID),
                        entry.getString(TAG_DESCRIPTION),
                        entry.getString(TAG_CANON_SOURCE),
                        topic, posX, posZ
                ));
            }
        }
        return wh;
    }

    // ─── Global singleton via WorldRuntimeState ────────────────────────

    /**
     * Cached singleton — invalidated on server stop so a fresh instance
     * is loaded from disk on next server start. Without this, a stopped
     * and restarted server would keep stale in-memory data that diverges
     * from the SavedData on disk.
     */
    private static WorldHistory instance = null;

    /**
     * Get the global WorldHistory instance. Loads from WorldRuntimeState
     * on first access. Persists on every recordGlobal().
     *
     * <p>Uses a reserved key ({@code _world_history}) in the player_mutations
     * map of WorldRuntimeState. The UUID "_world_history" can never be a real
     * player UUID (UUIDs don't start with underscore).
     *
     * <p>Thread safety: called from server tick thread only. No synchronization
     * needed — Forge runs all tick events on the server thread.
     */
    public static WorldHistory get(ServerLevel level) {
        if (instance != null) return instance;
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(level);
        CompoundTag reserved = runtimeState.getPlayerMutation(TAG_WORLD_HISTORY);

        if (reserved.contains(TAG_EVENTS, Tag.TAG_LIST)) {
            instance = load(reserved);
            Ergenverse.LOGGER.info("[WorldHistory] Loaded {} events from SavedData.",
                    instance.events.size());
        } else {
            instance = new WorldHistory();
            // Seed initial canon-consequence events
            seedCanonConsequences(instance);
            // Persist the seeded data immediately
            persist(level);
            Ergenverse.LOGGER.info("[WorldHistory] Seeded {} canon-consequence events " +
                    "and persisted.", instance.events.size());
        }
        return instance;
    }

    /**
     * Invalidate the cached singleton. Called when the server stops
     * so the next get() reloads from SavedData.
     */
    public static void invalidate() {
        if (instance != null) {
            Ergenverse.LOGGER.debug("[WorldHistory] Singleton invalidated (server stopping).");
        }
        instance = null;
    }

    private static void persist(ServerLevel level) {
        if (instance == null) return;
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(level);
        CompoundTag saved = instance.save();
        CompoundTag tag = runtimeState.getPlayerMutation(TAG_WORLD_HISTORY);
        tag.put(TAG_EVENTS, saved.getList(TAG_EVENTS, Tag.TAG_LIST));
        runtimeState.updatePlayerMutation(TAG_WORLD_HISTORY, tag);
    }

    /**
     * Record a world event and persist it.
     */
    public static void recordGlobal(ServerLevel level, long timestamp,
                                       String eventType, String regionId,
                                       String description, String canonSource) {
        WorldHistory wh = get(level);
        wh.record(timestamp, eventType, regionId, description, canonSource);
        persist(level);
    }

    /** Record a world event with topic and position, then persist (v2). */
    public static void recordGlobalWithTopic(ServerLevel level, long timestamp,
                                               String eventType, String regionId,
                                               String description, String canonSource,
                                               String topic, int posX, int posZ) {
        WorldHistory wh = get(level);
        wh.recordWithTopic(timestamp, eventType, regionId, description, canonSource, topic, posX, posZ);
        persist(level);
    }

    /**
     * Tick the world history — advances canon-consequence timelines.
     *
     * <p>Called from Ergenverse.onServerTick every 24000 ticks (1 MC day).
     * This is where the world's emergent timeline advances: faction shifts,
     * ecology changes, NPC migrations. Currently a v1 stub that persists
     * periodic checkpoints. v2 will advance RITimelineEngine events and
     * trigger world mutations.
     *
     * @param level the server overworld level
     * @param currentTick the current game time in ticks
     */
    public static void tickWorldHistory(ServerLevel level, long currentTick) {
        if (currentTick % 24000 != 0) return;

        WorldHistory wh = get(level);
        int dayNumber = (int) (currentTick / 24000);

        // v1: periodic persistence checkpoint (data is also persisted on every recordGlobal)
        // This ensures the seed data is written if no player events have occurred
        if (dayNumber % 7 == 0) {
            persist(level);
            Ergenverse.LOGGER.debug("[WorldHistory] Weekly checkpoint at day {}. " +
                    "Global events: {}", dayNumber, wh.size());
        }

        // v2: advance RITimelineEngine events, check for scheduled world mutations,
        // trigger ecology shifts from CausalEcology, process faction succession crises.
        // For now, the world is seeded with t0 canon consequences and only changes
        // when the player acts (recordGlobal calls from HistoryManager).
    }

    // ─── Canon-consequence seeding ────────────────────────────────────
    //
    // On first load, seed the world history with key canon events from
    // Renegade Immortal that set the t₀ state of Planet Suzaku.

    private static void seedCanonConsequences(WorldHistory wh) {
        long tick0 = 0;

        // Zhao Country — Wang Lin's starting region
        wh.record(tick0, "CANON_CONSEQUENCE", "zhao_country",
                "Zhao Country under King Zhao's rule. Heng Yue Sect and Heavenly Fate Sect operate in the mountains. The region is stable but Qi-poor for cultivation compared to other countries.",
                "ri:zhao_country");

        // Heng Yue Sect — Wang Lin's sect
        wh.record(tick0, "CANON_CONSEQUENCE", "heng_yue_sect",
                "Heng Yue Sect established in the Zhao Mountains. Head Elder: Elder Liu. Outer/inner/core mountain structure. Accepts disciples via entrance exam.",
                "ri:hang_yue_sect");

        // Heavenly Fate Sect — rival sect
        wh.record(tick0, "CANON_CONSEQUENCE", "heavenly_fate_sect",
                "Heavenly Fate Sect operates in the Zhao Plains. A major power in the region. Teng Family provides secular backing.",
                "ri:heavenly_fate_sect");

        // Soul Refining Sect — Wang Lin's second sect
        wh.record(tick0, "CANON_CONSEQUENCE", "pilu_kingdom",
                "Soul Refining Sect operates in Pilu Kingdom. Known for soul-based cultivation arts and restriction techniques.",
                "ri:soul_refining_sect");

        // Corpse Yin Sect — antagonists
        wh.record(tick0, "CANON_CONSEQUENCE", "sea_of_devils",
                "Corpse Yin Sect lurks in the Sea of Devils. A dark-arts sect practicing forbidden soul manipulation.",
                "ri:corpse_yin_sect");

        // Teng Family — secular noble power
        wh.record(tick0, "CANON_CONSEQUENCE", "zhao_plains",
                "Teng Family holds significant secular power in Zhao Country. Teng Huayuan is a Core Formation cultivator with political ambitions.",
                "ri:teng_family");

        // Wang Family Village — Wang Lin's home
        wh.record(tick0, "CANON_CONSEQUENCE", "zhao_plains",
                "Wang Family Village — a small village in Zhao Country. Wang Tiangui (mortal) and Wang Lin's family live here.",
                "ri:wang_family_village");

        // Tian Shui City — major city
        wh.record(tick0, "CANON_CONSEQUENCE", "zhao_plains",
                "Tian Shui City — the largest city in Zhao Country. Hub for trade, information, and sect recruitment.",
                "ri:tian_shui_city");

        // Suzaku — the planet itself
        wh.record(tick0, "CANON_CONSEQUENCE", "planet_suzaku",
                "Planet Suzaku — one of the Four Cultivation Planets. 14 major countries/regions. Cultivation ceiling: Soul Transformation (first step). The Suzaku Tomb holds the Cultivation Planet Crystal.",
                "ri:planet_suzaku");

        Ergenverse.LOGGER.info("[WorldHistory] Seeded {} canon-consequence events into world history.",
                wh.events.size());
    }
}