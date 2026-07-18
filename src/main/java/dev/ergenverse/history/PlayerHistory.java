package dev.ergenverse.history;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PlayerHistory — the emergent history of the player's actions (Layer 3).
 *
 * <p>Records everything the player does that creates NEW history: items refined,
 * techniques invented, beasts tamed, breakthroughs achieved, gifts received from
 * manifestations, NPCs killed, sects visited. Each entry carries its own
 * provenance (timestamp, cause, canon-vs-emergent marker).
 *
 * <h2>The two-timeline principle</h2>
 * <p>If the player refines a copy of Wang Lin's sword (received via the Affinity
 * System), that refinement is recorded here. The sword's CANON history (Layer 1)
 * is unchanged — Er Gen wrote it one way, and that's immutable. The player's
 * copy now has its OWN history, tracked here.
 *
 * <h2>Persistence</h2>
 * <p>History is stored in {@link dev.ergenverse.simulation.WorldRuntimeState}
 * under the player's UUID key ({@code player_mutations} → {@code playerHistory}).
 * The player mutation tag is written by {@link #save(CompoundTag)} and loaded by
 * {@link #load(CompoundTag)}. All mutations go through WorldRuntimeState's
 * copy-on-write system.
 *
 * <h2>Auto-recording hooks</h2>
 * <p>Callers (CultivationEvents, ManifestationGiftHandler, combat events) invoke
 * static convenience methods which delegate to the player's instance:
 * <ul>
 *   <li>{@link #recordBreakthrough(ServerPlayer, String, String)}</li>
 *   <li>{@link #recordGiftReceived(ServerPlayer, String, String)}</li>
 *   <li>{@link #recordKill(ServerPlayer, String, String)}</li>
 *   <li>{@link #recordDiscovery(ServerPlayer, String, String)}</li>
 *   <li>{@link #recordTechniqueLearned(ServerPlayer, String)}</li>
 *   <li>{@link #recordKarmaAction(ServerPlayer, String, int)}</li>
 * </ul>
 *
 * <h2>Integration with WorldRuntimeState</h2>
 * <p>The per-player mutation tag in WorldRuntimeState now contains a
 * {@code "player_history"} ListTag. This is the authoritative persistence
 * layer. The in-memory {@link PlayerHistory} instances are reconstructed
 * from NBT on demand via {@link #get(ServerPlayer)}.
 */
public final class PlayerHistory {

    // ─── NBT keys (short for NBT efficiency) ─────────────
    private static final String TAG_PH = "ph"; // ListTag of entry CompoundTags
    private static final String TAG_TS = "t";    // long
    private static final String TAG_ET = "e";    // String
    private static final String TAG_S = "s";    // String (subject)
    private static final String TAG_D = "d";    // String (description)
    private static final String TAG_C = "c";    // String (cause)

    /** Maximum entries per player (prevents unbounded memory growth). */
    private static final int MAX_ENTRIES = 500;

    // ─── Instance fields ────────────────────────────────────────────
    private final String playerUuid;
    private final List<HistoryEntry> entries = new ArrayList<>();

    private PlayerHistory(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    /**
     * A single emergent-history event. Standard canonical record.
     */
    public static final class HistoryEntry {
        public final long timestamp;
        public final String eventType;
        public final String subject;
        public final String description;
        public final String cause;

        public HistoryEntry(long timestamp, String eventType, String subject,
                        String description, String cause) {
            this.timestamp = timestamp;
            this.eventType = eventType;
            this.subject = subject;
            this.description = description;
            this.cause = cause;
        }
    }

    // ─── Access ──────────────────────────────────────────────

    /** Get all history entries (oldest first). */
    public List<HistoryEntry> all() {
        return Collections.unmodifiableList(entries);
    }

    /** Get history entries for a specific subject. */
    public List<HistoryEntry> forSubject(String subject) {
        if (subject == null || subject.isEmpty()) return List.of();
        List<HistoryEntry> result = new ArrayList<>();
        for (HistoryEntry e : entries) {
            if (subject.equals(e.subject)) result.add(e);
        }
        return result;
    }

    /** Get history entries of a specific type. */
    public List<HistoryEntry> forType(String eventType) {
        if (eventType == null || eventType.isEmpty()) return List.of();
        List<HistoryEntry> result = new ArrayList<>();
        for (HistoryEntry e : entries) {
            if (eventType.equals(e.eventType)) result.add(e);
        }
        return result;
    }

    /** Total number of recorded events. */
    public int size() {
        return entries.size();
    }

    /** The most recent entry, or null if empty. */
    public HistoryEntry latest() {
        return entries.isEmpty() ? null : entries.get(entries.size() - 1);
    }

    // ─── Recording ───────────────────────────────────────────

    /** Record a new emergent-history event. Trims to MAX_ENTRIES if exceeded. */
    public void record(long timestamp, String eventType, String subject,
                       String description, String cause) {
        entries.add(new HistoryEntry(timestamp, eventType, subject, description, cause));
        while (entries.size() > MAX_ENTRIES) {
            entries.remove(0);
        }
    }

    // ─── Serialization ───────────────────────────────────────────────

    /**
     * Serialize this history into a CompoundTag suitable for storage
     * in WorldRuntimeState's player_mutations map.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (HistoryEntry e : entries) {
            CompoundTag entry = new CompoundTag();
            entry.putLong(TAG_TS, e.timestamp);
            entry.putString(TAG_ET, e.eventType);
            entry.putString(TAG_S, e.subject);
            entry.putString(TAG_D, e.description);
            entry.putString(TAG_C, e.cause);
            list.add(entry);
        }
        tag.put(TAG_PH, list);
        return tag;
    }

    /**
     * Deserialize from a CompoundTag (from WorldRuntimeState).
     */
    public static PlayerHistory load(String playerUuid, CompoundTag tag) {
        PlayerHistory history = new PlayerHistory(playerUuid);
        ListTag list = tag.getList(TAG_PH, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            history.entries.add(new HistoryEntry(
                    entry.getLong(TAG_TS),
                    entry.getString(TAG_ET),
                    entry.getString(TAG_S),
                    entry.getString(TAG_D),
                    entry.getString(TAG_C)
            ));
        }
        return history;
    }

    // ─── Static convenience methods ──────────────────────────────────────

    /**
     * Record a cultivation breakthrough.
     */
    public static void recordBreakthrough(ServerPlayer player, String fromRealm,
                                          String toRealm, long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "BREAKTHROUGH", toRealm,
                "Broke through from " + fromRealm + " to " + toRealm + ".",
                "PLAYER_ACTION");
        persist(player, h);
    }

    /**
     * Record a gift received from a manifestation NPC.
     */
    public static void recordGiftReceived(ServerPlayer player, String itemName,
                                           String protagonistId, long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "GIFT_RECEIVED", itemName,
                "Received " + itemName + " from " + protagonistId + " manifestation.",
                "AFFINITY_GRANT");
        persist(player, h);
    }

    /**
     * Record killing an NPC or creature.
     */
    public static void recordKill(ServerPlayer player, String targetName,
                                   String method, long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "KILL", targetName,
                "Killed " + targetName + " via " + method + ".",
                "PLAYER_ACTION");
        persist(player, h);
    }

    /**
     * Record discovering something (location, item, secret).
     */
    public static void recordDiscovery(ServerPlayer player, String subject,
                                        String detail, long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "DISCOVERY", subject,
                "Discovered: " + detail, "PLAYER_ACTION");
        persist(player, h);
    }

    /**
     * Record a technique being learned.
     */
    public static void recordTechniqueLearned(ServerPlayer player, String techniqueName,
                                                 long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "TECHNIQUE_LEARNED", techniqueName,
                "Learned technique: " + techniqueName + ".", "PLAYER_ACTION");
        persist(player, h);
    }

    /**
     * Record a karma-changing action (for future karmic consequences).
     */
    public static void recordKarmaAction(ServerPlayer player, String action,
                                           int karmaDelta, long worldTick) {
        PlayerHistory h = get(player);
        h.record(worldTick, "KARMA_ACTION", action,
                "Karmic action: " + action + " (karma " + (karmaDelta >= 0 ? "+" : "") + karmaDelta + ").",
                "PLAYER_ACTION");
        persist(player, h);
    }

    // ─── Internal: get/persist via WorldRuntimeState ─────────────────

    /**
     * Get (or create) the PlayerHistory for a given player.
     *
     * <p>Reads from WorldRuntimeState's player_mutations tag. If no
     * history exists yet, returns an empty one.
     */
    public static PlayerHistory get(ServerPlayer player) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);

        if (playerTag.contains(TAG_PH, Tag.TAG_LIST)) {
            return load(uuid, playerTag);
        }
        return new PlayerHistory(uuid);
    }

    /**
     * Persist the PlayerHistory back into WorldRuntimeState.
     */
    private static void persist(ServerPlayer player, PlayerHistory history) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();

        // Read existing mutation tag, layer the history into it
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);
        CompoundTag savedHistory = history.save();
        playerTag.put(TAG_PH, savedHistory.getList(TAG_PH, Tag.TAG_LIST));
        runtimeState.updatePlayerMutation(uuid, playerTag);
    }
}