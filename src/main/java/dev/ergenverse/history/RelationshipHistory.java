package dev.ergenverse.history;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RelationshipHistory — the affinity/relationship timeline between the player and
 * each canon protagonist manifestation (Layer 3).
 *
 * <p>Tracks trust earned, gifts given/received, shared experiences. This is the
 * runtime state that the
 * {@link dev.ergenverse.simulation.affinity.ManifestationGiftSystem}
 * queries when deciding whether a manifestation will grant an item or insight.
 *
 * <h2>Relationship to the Affinity System (Layer 2)</h2>
 * <p>The Affinity System defines the RULES (affinity is a prerequisite, not a
 * currency; the manifestation decides via a four-question evaluation). This class
 * records the STATE (how much affinity has been earned, what gifts have been
 * exchanged). Together they determine what the player can access.
 *
 * <h2>Affinity types</h2>
 * <ul>
 *   <li><b>AFFINITY_EARNED</b> — general trust gained through interactions.</li>
 *   <li><b>GIFT_RECEIVED</b> — the player received a gift from the manifestation.</li>
 *   <li><b>GIFT_GIVEN</b> — the player gave a gift to the manifestation.</li>
 *   <li><b>SHARED_EXPERIENCE</b> — fought together, explored together, etc.</li>
 *   <li><b>QUEST_COMPLETED</b> — the player completed a task for the manifestation.</li>
 *   <li><b>TRUST_BROKEN</b> — the player betrayed the manifestation's trust.</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * <p>Stored in {@link dev.ergenverse.simulation.WorldRuntimeState} under
 * the player's UUID key ({@code player_mutations} → {@code relationships}).
 */
public final class RelationshipHistory {

    // ─── NBT keys ─────────────────────────────────────────────────────
    private static final String TAG_RELATIONSHIPS = "relationships";
    private static final String TAG_PROTAGONIST = "protagonist_id";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_EVENT_TYPE = "event_type";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_AFFINITY_DELTA = "affinity_delta";
    private static final String TAG_AFFINITY_SCORES = "affinity_scores";

    /** Maximum events per protagonist. */
    private static final int MAX_EVENTS_PER_PROTAGONIST = 100;

    /**
     * A single relationship event.
     * Compact record — no defensive copies in constructors.
     */
    public record RelationshipEvent(
            long timestamp,
            String protagonistCanonId,
            String eventType,
            String description,
            int affinityDelta
    ) {
        public RelationshipEvent {
            if (protagonistCanonId == null) throw new IllegalArgumentException("protagonistCanonId cannot be null");
            if (eventType == null) throw new IllegalArgumentException("eventType cannot be null");
            if (description == null) throw new IllegalArgumentException("description cannot be null");
        }

        /** Factory that applies defaults for nullable fields. */
        public static RelationshipEvent withDefaults(long timestamp, String protagonistCanonId,
                                                      String eventType, String description, int affinityDelta) {
            return new RelationshipEvent(timestamp,
                    protagonistCanonId != null ? protagonistCanonId : "",
                    eventType != null ? eventType : "AFFINITY_EARNED",
                    description != null ? description : "",
                    affinityDelta);
        }
    }

    // ─── Instance fields ────────────────────────────────────────────
    private final String playerUuid;
    private final Map<String, List<RelationshipEvent>> relationships = new HashMap<>();

    private RelationshipHistory(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    // ─── Access ──────────────────────────────────────────────────────

    /** Get all events for a protagonist (oldest first). */
    public List<RelationshipEvent> forProtagonist(String protagonistCanonId) {
        return Collections.unmodifiableList(
                relationships.getOrDefault(protagonistCanonId, Collections.emptyList()));
    }

    /**
     * Get the affinity score for a protagonist (sum of all affinity deltas).
     * This is what ManifestationGiftSystem.PlayerStateSnapshot.affinity
     * should read.
     */
    public int affinityScore(String protagonistCanonId) {
        List<RelationshipEvent> events = relationships.get(protagonistCanonId);
        if (events == null || events.isEmpty()) return 0;
        return events.stream().mapToInt(RelationshipEvent::affinityDelta).sum();
    }

    /** Get all protagonist IDs that have any relationship events. */
    public List<String> knownProtagonists() {
        return List.copyOf(relationships.keySet());
    }

    // ─── Recording ───────────────────────────────────────────────────

    /** Record a relationship event. Trims to MAX_EVENTS_PER_PROTAGONIST. */
    public void record(long timestamp, String protagonistCanonId, String eventType,
                         String description, int affinityDelta) {
        List<RelationshipEvent> list = relationships.computeIfAbsent(protagonistCanonId,
                k -> new ArrayList<>());
        list.add(new RelationshipEvent(timestamp, protagonistCanonId, eventType,
                description, affinityDelta));
        while (list.size() > MAX_EVENTS_PER_PROTAGONIST) {
            list.remove(0);
        }
    }

    // ─── Serialization ───────────────────────────────────────────────

    /**
     * Serialize relationships into a CompoundTag.
     * Structure: relationships → [ {protagonist_id, events: [entry, ...]}, ... ]
     * Also writes a flat affinity_scores tag for O(1) reads.
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Flat affinity scores for quick reads
        CompoundTag scoresTag = new CompoundTag();
        for (Map.Entry<String, List<RelationshipEvent>> entry : relationships.entrySet()) {
            scoresTag.putInt(entry.getKey(), affinityScore(entry.getKey()));
        }
        tag.put(TAG_AFFINITY_SCORES, scoresTag);

        ListTag relList = new ListTag();
        for (Map.Entry<String, List<RelationshipEvent>> entry : relationships.entrySet()) {
            CompoundTag relTag = new CompoundTag();
            relTag.putString(TAG_PROTAGONIST, entry.getKey());

            ListTag evtList = new ListTag();
            for (RelationshipEvent e : entry.getValue()) {
                CompoundTag eTag = new CompoundTag();
                eTag.putLong(TAG_TIMESTAMP, e.timestamp);
                eTag.putString(TAG_EVENT_TYPE, e.eventType);
                eTag.putString(TAG_DESCRIPTION, e.description);
                eTag.putInt(TAG_AFFINITY_DELTA, e.affinityDelta);
                evtList.add(eTag);
            }
            relTag.put(TAG_EVENTS, evtList);
            relList.add(relTag);
        }
        tag.put(TAG_RELATIONSHIPS, relList);
        return tag;
    }

    /**
     * Deserialize from a CompoundTag.
     */
    public static RelationshipHistory load(String playerUuid, CompoundTag tag) {
        RelationshipHistory rh = new RelationshipHistory(playerUuid);

        if (tag.contains(TAG_RELATIONSHIPS, Tag.TAG_LIST)) {
            ListTag relList = tag.getList(TAG_RELATIONSHIPS, Tag.TAG_COMPOUND);
            for (int i = 0; i < relList.size(); i++) {
                CompoundTag relTag = relList.getCompound(i);
                String protagId = relTag.getString(TAG_PROTAGONIST);

                ListTag evtList = relTag.getList(TAG_EVENTS, Tag.TAG_COMPOUND);
                List<RelationshipEvent> events = new ArrayList<>();
                for (int j = 0; j < evtList.size(); j++) {
                    CompoundTag eTag = evtList.getCompound(j);
                    events.add(new RelationshipEvent(
                            eTag.getLong(TAG_TIMESTAMP),
                            protagId,
                            eTag.getString(TAG_EVENT_TYPE),
                            eTag.getString(TAG_DESCRIPTION),
                            eTag.getInt(TAG_AFFINITY_DELTA)
                    ));
                }
                rh.relationships.put(protagId, events);
            }
        }
        return rh;
    }

    // ─── Static convenience methods ──────────────────────────────────

    public static void recordAffinityEarned(ServerPlayer player, String protagonistId,
                                              String reason, int delta, long worldTick) {
        RelationshipHistory rh = get(player);
        rh.record(worldTick, protagonistId, "AFFINITY_EARNED",
                reason, delta);
        persist(player, rh);
    }

    public static void recordGiftReceived(ServerPlayer player, String protagonistId,
                                             String itemName, int delta, long worldTick) {
        RelationshipHistory rh = get(player);
        rh.record(worldTick, protagonistId, "GIFT_RECEIVED",
                "Received " + itemName + ".", delta);
        persist(player, rh);
    }

    public static void recordSharedExperience(ServerPlayer player, String protagonistId,
                                                  String experience, int delta, long worldTick) {
        RelationshipHistory rh = get(player);
        rh.record(worldTick, protagonistId, "SHARED_EXPERIENCE",
                experience, delta);
        persist(player, rh);
    }

    public static void recordQuestCompleted(ServerPlayer player, String protagonistId,
                                                String quest, int delta, long worldTick) {
        RelationshipHistory rh = get(player);
        rh.record(worldTick, protagonistId, "QUEST_COMPLETED",
                "Completed: " + quest, delta);
        persist(player, rh);
    }

    public static void recordTrustBroken(ServerPlayer player, String protagonistId,
                                           String reason, long worldTick) {
        RelationshipHistory rh = get(player);
        rh.record(worldTick, protagonistId, "TRUST_BROKEN",
                reason, -20);
        persist(player, rh);
    }

    // ─── Internal: get/persist via WorldRuntimeState ─────────────────

    public static RelationshipHistory get(ServerPlayer player) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);

        if (playerTag.contains(TAG_RELATIONSHIPS, Tag.TAG_LIST)) {
            return load(uuid, playerTag);
        }
        return new RelationshipHistory(uuid);
    }

    private static void persist(ServerPlayer player, RelationshipHistory rh) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);
        CompoundTag saved = rh.save();
        playerTag.put(TAG_RELATIONSHIPS, saved.getList(TAG_RELATIONSHIPS, Tag.TAG_LIST));
        runtimeState.updatePlayerMutation(uuid, playerTag);
    }
}