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
 * ActorRelationshipStore — multi-axis NPC-to-NPC relationship storage.
 *
 * <p>Per Constitution Articles XXXIV and XLI §4: a relationship is not a
 * number. A relationship is a graph of distinct, independently-variable
 * dimensions. "Friendship > 50" is forbidden. NPCs maintain structured
 * relationship state.
 *
 * <h2>Multi-axis relationship dimensions</h2>
 * <ul>
 *   <li>{@code trust} (0–100) — does the NPC believe the target's words?</li>
 *   <li>{@code respect} (0–100) — does the NPC esteem the target's capability?</li>
 *   <li>{@code fear} (0–100) — does the NPC fear the target?</li>
 *   <li>{@code familiarity} (0–100) — repeated exposure; not friendship,
 *       not trust. Simply: "I know who this person is."</li>
 *   <li>{@code debt} (signed int) — outstanding favors owed.
 *       Positive = NPC owes target, negative = target owes NPC.</li>
 *   <li>{@code grievance} (0–100) — accumulated resentment from wrongs.</li>
 * </ul>
 *
 * <p>Additionally, a legacy {@code affinity} field is maintained for backward
 * compatibility with existing call sites. The affinity is a computed value:
 * {@code affinity = (trust * 0.4 + respect * 0.2 - fear * 0.3 - grievance * 0.5 + debt * 0.1) * sign}.
 *
 * <h2>Storage model</h2>
 * <p>Key: {@code "actorA|actorB"} (always lexicographically ordered).
 * Value: A {@code CompoundTag} containing all six axis values plus metadata.
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
 *   <li><b>Article XXXIV:</b> Trust, Respect, Fear, Familiarity, Debt,
 *       and Grievance are independently-variable dimensions. Two NPCs
 *       can have completely different graphs for the same player.</li>
 *   <li><b>Article XLI §4:</b> Familiarity is a required dimension —
 *       "I know who this person is" from repeated exposure, separate
 *       from trust or respect.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> This is a data store, not an
 *       event-processing system. The RelationshipEngine and
 *       NpcSemanticRelationshipSubscriber write to it; other systems
 *       read from it.</li>
 * </ul>
 */
public class ActorRelationshipStore extends SavedData {

    private static final String DATA_NAME = "ergenverse_actor_relationships";
    private static final int CURRENT_VERSION = 2;

    /** All relationships: "actorA|actorB" → CompoundTag(trust, respect, ...). */
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

    // ─── Multi-Axis Public API ─────────────────────────────────────────

    /**
     * Record a multi-axis relationship change between two actors.
     * Each delta is independently applied. Zero or null values are ignored.
     *
     * @param actorA            first actor ID (canon ID like "wang_lin" or player UUID)
     * @param actorB            second actor ID
     * @param trustDelta         change to trust (0-100, clamped)
     * @param respectDelta       change to respect (0-100, clamped)
     * @param fearDelta          change to fear (0-100, clamped)
     * @param familiarityDelta   change to familiarity (0-100, clamped)
     * @param debtDelta          change to debt (signed, positive = NPC A owes B)
     * @param grievanceDelta     change to grievance (0-100, clamped)
     * @param reason             human-readable description
     * @param tick               the server tick
     */
    public void recordMultiAxis(String actorA, String actorB,
                                  @Nullable Integer trustDelta,
                                  @Nullable Integer respectDelta,
                                  @Nullable Integer fearDelta,
                                  @Nullable Integer familiarityDelta,
                                  @Nullable Integer debtDelta,
                                  @Nullable Integer grievanceDelta,
                                  String reason, long tick) {
        if (actorA == null || actorB == null || actorA.isEmpty() || actorB.isEmpty()) return;
        boolean anyChange = trustDelta != null && trustDelta != 0
                || respectDelta != null && respectDelta != 0
                || fearDelta != null && fearDelta != 0
                || familiarityDelta != null && familiarityDelta != 0
                || debtDelta != null && debtDelta != 0
                || grievanceDelta != null && grievanceDelta != 0;
        if (!anyChange) return;

        String key = normalizeKey(actorA, actorB);
        CompoundTag existing = relationships.get(key);

        if (existing == null) {
            existing = new CompoundTag();
            existing.putInt("trust", 0);
            existing.putInt("respect", 0);
            existing.putInt("fear", 0);
            existing.putInt("familiarity", 0);
            existing.putInt("debt", 0);
            existing.putInt("grievance", 0);
            existing.putInt("affinity", 0);
            existing.putString("lastEvent", "");
            existing.putLong("lastEventTick", 0);
            existing.putInt("eventCount", 0);
        }

        // Copy on write (defensive).
        CompoundTag updated = existing.copy();

        if (trustDelta != null && trustDelta != 0)
            updated.putInt("trust", clampAxis(updated.getInt("trust") + trustDelta));
        if (respectDelta != null && respectDelta != 0)
            updated.putInt("respect", clampAxis(updated.getInt("respect") + respectDelta));
        if (fearDelta != null && fearDelta != 0)
            updated.putInt("fear", clampAxis(updated.getInt("fear") + fearDelta));
        if (familiarityDelta != null && familiarityDelta != 0)
            updated.putInt("familiarity", clampAxis(updated.getInt("familiarity") + familiarityDelta));
        if (debtDelta != null && debtDelta != 0)
            updated.putInt("debt", updated.getInt("debt") + debtDelta);
        if (grievanceDelta != null && grievanceDelta != 0)
            updated.putInt("grievance", clampAxis(updated.getInt("grievance") + grievanceDelta));

        // Recompute legacy affinity from multi-axis values.
        updated.putInt("affinity", computeAffinity(updated));

        updated.putString("lastEvent", reason);
        updated.putLong("lastEventTick", tick);
        updated.putInt("eventCount", updated.getInt("eventCount") + 1);

        relationships.put(key, updated);
        this.setDirty();

        Ergenverse.LOGGER.debug("[ActorRelStore] {} ↔ {}: trust={}/respect={}/fear={}/fam={}/debt={}/griev={}",
                actorA, actorB,
                updated.getInt("trust"), updated.getInt("respect"),
                updated.getInt("fear"), updated.getInt("familiarity"),
                updated.getInt("debt"), updated.getInt("grievance"));
    }

    /**
     * Legacy API: record a simple affinity change.
     * Maps affinity delta to trust delta for backward compatibility.
     */
    public void recordRelationship(String actorA, String actorB,
                                    int affinityDelta, String reason, long tick) {
        // Map legacy affinity to multi-axis: positive affinity → trust boost,
        // negative affinity → grievance boost. This preserves the spirit of
        // the old system while using the new multi-axis structure.
        int trustDelta = affinityDelta > 0 ? Math.min(affinityDelta, 5) : 0;
        int grievanceDelta = affinityDelta < 0 ? Math.min(-affinityDelta, 5) : 0;
        int familiarityDelta = Math.abs(affinityDelta) > 0 ? 1 : 0;
        recordMultiAxis(actorA, actorB, trustDelta, 0, 0, familiarityDelta,
                0, grievanceDelta, reason, tick);
    }

    // ─── Multi-Axis Getters ───────────────────────────────────────────

    /** Get the trust value (0-100) between two actors. Returns 0 if no relationship. */
    public int getTrust(String actorA, String actorB) {
        return getAxis(actorA, actorB, "trust");
    }

    /** Get the respect value (0-100) between two actors. Returns 0 if no relationship. */
    public int getRespect(String actorA, String actorB) {
        return getAxis(actorA, actorB, "respect");
    }

    /** Get the fear value (0-100) between two actors. Returns 0 if no relationship. */
    public int getFear(String actorA, String actorB) {
        return getAxis(actorA, actorB, "fear");
    }

    /** Get the familiarity value (0-100) between two actors. Returns 0 if no relationship. */
    public int getFamiliarity(String actorA, String actorB) {
        return getAxis(actorA, actorB, "familiarity");
    }

    /** Get the debt value (signed) between two actors. Returns 0 if no relationship. */
    public int getDebt(String actorA, String actorB) {
        return getAxis(actorA, actorB, "debt");
    }

    /** Get the grievance value (0-100) between two actors. Returns 0 if no relationship. */
    public int getGrievance(String actorA, String actorB) {
        return getAxis(actorA, actorB, "grievance");
    }

    /** Get the legacy affinity (computed from multi-axis). Returns 0 if no relationship. */
    public int getAffinity(String actorA, String actorB) {
        if (actorA == null || actorB == null) return 0;
        String key = normalizeKey(actorA, actorB);
        CompoundTag data = relationships.get(key);
        if (data == null) return 0;
        // If legacy data has affinity but no multi-axis fields, return the stored value.
        if (!data.contains("trust") && data.contains("affinity")) return data.getInt("affinity");
        return computeAffinity(data);
    }

    /**
     * Get the full relationship data for two actors, or null if none exists.
     * Returns a defensive copy.
     */
    @Nullable
    public CompoundTag getRelationshipData(String actorA, String actorB) {
        if (actorA == null || actorB == null) return null;
        String key = normalizeKey(actorA, actorB);
        CompoundTag data = relationships.get(key);
        return data != null ? data.copy() : null;
    }

    /** Check whether any relationship exists between two actors. */
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
     * Returns a map of otherActorId → CompoundTag (defensive copies).
     */
    public Map<String, CompoundTag> getRelationshipsForActor(String actorId) {
        Map<String, CompoundTag> result = new ConcurrentHashMap<>();
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
                result.put(otherId, entry.getValue().copy());
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
            return store;
        }

        ListTag list = compound.getList("relationships", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entryTag = list.getCompound(i);
            String key = entryTag.getString("key");
            CompoundTag data = entryTag.getCompound("data");

            // Migrate v1 (affinity-only) to v2 (multi-axis).
            if (version == 1) {
                if (!data.contains("trust")) data.putInt("trust", 0);
                if (!data.contains("respect")) data.putInt("respect", 0);
                if (!data.contains("fear")) data.putInt("fear", 0);
                if (!data.contains("familiarity")) data.putInt("familiarity", 0);
                if (!data.contains("debt")) data.putInt("debt", 0);
                if (!data.contains("grievance")) data.putInt("grievance", 0);
                // Recompute affinity from original value if multi-axis is all zero.
                int oldAffinity = data.getInt("affinity");
                if (oldAffinity != 0 && data.getInt("trust") == 0) {
                    if (oldAffinity > 0) data.putInt("trust", Math.min(oldAffinity * 2, 100));
                    else data.putInt("grievance", Math.min(-oldAffinity * 2, 100));
                    data.putInt("familiarity", Math.min(Math.abs(oldAffinity), 50));
                    data.putInt("affinity", store.computeAffinity(data));
                }
            }

            store.relationships.put(key, data);
        }

        Ergenverse.LOGGER.info("[ActorRelStore] Loaded {} NPC-to-NPC relationships (v{})",
                store.relationships.size(), version);
        return store;
    }

    // ─── Internal helpers ─────────────────────────────────────────────

    private int getAxis(String actorA, String actorB, String axis) {
        if (actorA == null || actorB == null) return 0;
        String key = normalizeKey(actorA, actorB);
        CompoundTag data = relationships.get(key);
        return data != null ? data.getInt(axis) : 0;
    }

    /**
     * Compute a legacy affinity score from multi-axis values.
     * Formula: (trust * 0.4 + respect * 0.2 - fear * 0.3 - grievance * 0.5 + debt * 0.1) * scale
     * This gives a value roughly in the -50 to +50 range, matching the old affinity scale.
     */
    private int computeAffinity(CompoundTag data) {
        int trust = data.getInt("trust");
        int respect = data.getInt("respect");
        int fear = data.getInt("fear");
        int grievance = data.getInt("grievance");
        int debt = data.getInt("debt");
        double score = (trust * 0.4 + respect * 0.2 - fear * 0.3 - grievance * 0.5 + Math.min(debt, 10) * 0.1);
        return (int) Math.round(score);
    }

    /** Clamp an axis value to [0, 100]. */
    private static int clampAxis(int value) {
        return Math.max(0, Math.min(100, value));
    }

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
                    .append(": T=").append(data.getInt("trust"))
                    .append(" R=").append(data.getInt("respect"))
                    .append(" F=").append(data.getInt("fear"))
                    .append(" Fam=").append(data.getInt("familiarity"))
                    .append(" D=").append(data.getInt("debt"))
                    .append(" G=").append(data.getInt("grievance"))
                    .append(", affinity=").append(data.getInt("affinity"))
                    .append(", events=").append(data.getInt("eventCount"))
                    .append("\n");
        }
        return sb.toString();
    }
}
