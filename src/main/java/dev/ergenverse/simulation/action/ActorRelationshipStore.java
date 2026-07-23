package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ActorRelationshipStore — persistent NPC-to-NPC relationship storage.
 *
 * <p>Per the user's directive (2026-07-23, option f):
 * <pre>
 *   "Create a general ActorRelationshipStore for NPC-to-NPC relationships.
 *    This unblocks Wang Lin → Li Muwan, elder → disciple, etc."
 * </pre>
 *
 * <p>Before this store, the simulation could only persist player-centric
 * relationships (via {@code RelationshipHistory} which requires a ServerPlayer).
 * NPC-to-NPC relationships (Wang Lin ↔ Li Muwan, elder ↔ disciple, rival ↔
 * rival) were logged at DEBUG level but never persisted. They vanished on
 * server restart.
 *
 * <p>This store persists ALL actor-to-actor relationships as a SavedData
 * instance. It stores an affinity score and a list of relationship events
 * (reason + tick) for each actor pair.
 *
 * <h2>Storage model</h2>
 * <p>Key: {@code "actorA|actorB"} (always lexicographically ordered).
 * Value: A {@code CompoundTag} containing:
 * <ul>
 *   <li>{@code affinity} (int) — the cumulative relationship score.
 *       Positive = friendly/loyal. Negative = hostile/distrustful.</li>
 *   <li>{@code lastEvent} (String) — the most recent event that changed
 *       the relationship.</li>
 *   <li>{@code lastEventTick} (long) — when the last event occurred.</li>
 *   <li>{@code eventCount} (int) — total number of events recorded.</li>
 * </ul>
 *
 * <h2>Thread safety</h2>
 * <p>Uses ConcurrentHashMap. All getters return copies or primitives.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article XLIII §2:</b> Permanence is absolute. An NPC-to-NPC
 *       relationship that forms persists across server restart.</li>
 *   <li><b>Article V:</b> Everything exists without the player. Wang Lin
 *       and Li Muwan's relationship deepens even if the player never
 *       observes it.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> This is a data store, not an
 *       event-processing system. The RelationshipEngine writes to it;
 *       other systems read from it.</li>
 * </ul>
 */
public class ActorRelationshipStore extends SavedData {

    private static final String DATA_NAME = "ergenverse_actor_relationships";
    private static final int CURRENT_VERSION = 1;

    /** All relationships: "actorA|actorB" → CompoundTag(affinity, lastEvent, ...). */
    private final ConcurrentHashMap<String, CompoundTag> relationships = new ConcurrentHashMap<>();

    // ─── Singleton access ──────────────────────────────────────────────

    private ActorRelationshipStore() {}

    /**
     * Get the world-global relationship store. Attached to the overworld's
     * DimensionDataStorage so it persists across server restarts.
     * Lazy-creates on first access.
     */
    public static ActorRelationshipStore get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(ActorRelationshipStore::load, ActorRelationshipStore::new, DATA_NAME);
    }

    // ─── Public API ───────────────────────────────────────────────────

    /**
     * Record a relationship change between two actors.
     *
     * @param actorA      first actor ID (canon ID like "wang_lin" or player UUID)
     * @param actorB      second actor ID
     * @param affinityDelta  the change amount (+5, -15, etc.)
     * @param reason      human-readable description of why the change occurred
     * @param tick        the server tick when this event occurred
     */
    public void recordRelationship(String actorA, String actorB,
                                    int affinityDelta, String reason, long tick) {
        if (actorA == null || actorB == null || actorA.isEmpty() || actorB.isEmpty()) return;
        if (affinityDelta == 0) return;

        String key = normalizeKey(actorA, actorB);
        CompoundTag existing = relationships.get(key);

        if (existing == null) {
            existing = new CompoundTag();
            existing.putInt("affinity", 0);
            existing.putString("lastEvent", "");
            existing.putLong("lastEventTick", 0);
            existing.putInt("eventCount", 0);
        }

        // Copy on write (defensive).
        CompoundTag updated = existing.copy();
        int currentAffinity = updated.getInt("affinity");
        updated.putInt("affinity", currentAffinity + affinityDelta);
        updated.putString("lastEvent", reason);
        updated.putLong("lastEventTick", tick);
        updated.putInt("eventCount", updated.getInt("eventCount") + 1);

        relationships.put(key, updated);
        this.setDirty();

        Ergenverse.LOGGER.debug("[ActorRelStore] {} ↔ {}: {} (total={})",
                actorA, actorB,
                affinityDelta > 0 ? "+" + affinityDelta : affinityDelta,
                currentAffinity + affinityDelta);
    }

    /**
     * Get the cumulative affinity between two actors.
     * Returns 0 if no relationship exists.
     */
    public int getAffinity(String actorA, String actorB) {
        if (actorA == null || actorB == null) return 0;
        String key = normalizeKey(actorA, actorB);
        CompoundTag data = relationships.get(key);
        return data != null ? data.getInt("affinity") : 0;
    }

    /**
     * Get the relationship data for two actors, or null if none exists.
     * Returns a defensive copy.
     */
    @Nullable
    public CompoundTag getRelationshipData(String actorA, String actorB) {
        if (actorA == null || actorB == null) return null;
        String key = normalizeKey(actorA, actorB);
        CompoundTag data = relationships.get(key);
        return data != null ? data.copy() : null;
    }

    /**
     * Check whether any relationship exists between two actors.
     */
    public boolean hasRelationship(String actorA, String actorB) {
        if (actorA == null || actorB == null) return false;
        return relationships.containsKey(normalizeKey(actorA, actorB));
    }

    /** Total number of stored relationships. */
    public int relationshipCount() {
        return relationships.size();
    }

    /**
     * Get all relationships involving a specific actor.
     * Returns a map of otherActorId → affinity.
     */
    public Map<String, Integer> getRelationshipsForActor(String actorId) {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        if (actorId == null || actorId.isEmpty()) return result;
        String suffix = "|" + actorId;
        String prefix = actorId + "|";
        for (var entry : relationships.entrySet()) {
            String key = entry.getKey();
            String otherId = null;
            if (key.startsWith(prefix)) {
                otherId = key.substring(prefix.length());
            } else if (key.endsWith(suffix)) {
                otherId = key.substring(0, key.length() - suffix.length());
            }
            if (otherId != null && !otherId.isEmpty()) {
                result.put(otherId, entry.getValue().getInt("affinity"));
            }
        }
        return result;
    }

    // ─── NBT Persistence ──────────────────────────────────────────────

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        compound.putInt("data_version", CURRENT_VERSION);
        ListTag list = new ListTag();
        for (var entry : relationships.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("key", entry.getKey());
            entryTag.put("data", entry.getValue().copy());
            list.add(entryTag);
        }
        compound.put("relationships", list);
        return compound;
    }

    public static ActorRelationshipStore load(CompoundTag compound) {
        ActorRelationshipStore store = new ActorRelationshipStore();

        int version = compound.getInt("data_version");
        if (version < 1) {
            // v0 = no data — empty store.
            return store;
        }

        ListTag list = compound.getList("relationships", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String key = entryTag.getString("key");
            CompoundTag data = entryTag.getCompound("data");
            store.relationships.put(key, data);
        }

        Ergenverse.LOGGER.info("[ActorRelStore] Loaded {} NPC-to-NPC relationships",
                store.relationships.size());
        return store;
    }

    // ─── Key normalization ────────────────────────────────────────────

    /**
     * Normalize a pair key so that "A|B" and "B|A" map to the same entry.
     * Uses lexicographic ordering.
     */
    private static String normalizeKey(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    // ─── Diagnostics ──────────────────────────────────────────────────

    public String getStatusReport() {
        StringBuilder sb = new StringBuilder("ActorRelationshipStore: ");
        sb.append(relationships.size()).append(" relationships\n");
        for (var entry : relationships.entrySet()) {
            CompoundTag data = entry.getValue();
            sb.append("  ").append(entry.getKey())
                    .append(": affinity=").append(data.getInt("affinity"))
                    .append(", events=").append(data.getInt("eventCount"))
                    .append(", last=").append(data.contains("lastEvent") ? data.getString("lastEvent") : "none")
                    .append("\n");
        }
        return sb.toString();
    }
}
