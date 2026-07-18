package dev.ergenverse.perception.observation;

import dev.ergenverse.perception.observation.ObservationPhenomenon;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * PlayerObservationState — per-player record of noticed phenomena, acquired
 * knowledge, and completed chains.
 *
 * <p>Persisted in player persistent NBT under key {@code ergenverse_observation}
 * (same pattern as {@link dev.ergenverse.origin.ExplorationShortcutSystem}).
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Noticing a phenomenon is not a "trigger." It is the player's perception
 *   engaging with the world. The state records what they have noticed so
 *   chains can form organically — but the player is NEVER told "you noticed
 *   X." They simply see the world respond to their attention.
 * </blockquote>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class PlayerObservationState {

    private static final String NBT_ROOT = "ergenverse_observation";
    private static final String NBT_NOTICED = "noticed";        // phenomenon IDs
    private static final String NBT_KINDS = "kinds";            // noticed Kind IDs
    private static final String NBT_TAGS = "tags";              // KnowledgeTag IDs
    private static final String NBT_CHAINS = "chains";          // completed chain IDs
    private static final String NBT_LAST_TICK = "last_tick";

    /** Max noticed phenomena IDs to retain (older ones pruned). */
    private static final int MAX_NOTICED = 200;

    private final Set<String> noticedPhenomenaIds;      // dedup by phenomenon ID
    private final Set<ObservationPhenomenon.Kind> noticedKinds;                // for chain evaluation
    private final Set<KnowledgeTag> acquiredTags;
    private final Set<String> completedChainIds;
    private long lastObservationTick;

    public PlayerObservationState() {
        this.noticedPhenomenaIds = new HashSet<>();
        this.noticedKinds = new HashSet<>();
        this.acquiredTags = new HashSet<>();
        this.completedChainIds = new HashSet<>();
        this.lastObservationTick = 0;
    }

    // ─── Mutation ──────────────────────────────────────────────────────

    /**
     * Record that the player noticed a phenomenon. Returns true if this is a
     * NEW notice (not previously seen).
     */
    public boolean notice(String phenomenonId, ObservationPhenomenon.Kind kind, long tick) {
        boolean isNew = noticedPhenomenaIds.add(phenomenonId);
        noticedKinds.add(kind);
        lastObservationTick = tick;
        // Prune if too large (keep most recent — simple cap)
        if (noticedPhenomenaIds.size() > MAX_NOTICED) {
            // Simple prune: clear half. Future: LRU by tick.
            int target = MAX_NOTICED / 2;
            java.util.List<String> all = new java.util.ArrayList<>(noticedPhenomenaIds);
            for (int i = 0; i < all.size() - target; i++) {
                noticedPhenomenaIds.remove(all.get(i));
            }
        }
        return isNew;
    }

    /** Grant a knowledge tag. Returns true if newly granted. */
    public boolean grantKnowledge(KnowledgeTag tag) {
        return acquiredTags.add(tag);
    }

    /** Mark a chain as completed. Returns true if newly completed. */
    public boolean completeChain(String chainId) {
        return completedChainIds.add(chainId);
    }

    // ─── Queries ───────────────────────────────────────────────────────

    public boolean hasNoticed(String phenomenonId) {
        return noticedPhenomenaIds.contains(phenomenonId);
    }

    public boolean hasNoticedKind(ObservationPhenomenon.Kind kind) {
        return noticedKinds.contains(kind);
    }

    public boolean hasKnowledge(KnowledgeTag tag) {
        return acquiredTags.contains(tag);
    }

    public boolean hasCompletedChain(String chainId) {
        return completedChainIds.contains(chainId);
    }

    public Set<ObservationPhenomenon.Kind> getNoticedKinds() {
        return Collections.unmodifiableSet(noticedKinds);
    }

    public Set<KnowledgeTag> getAcquiredTags() {
        return Collections.unmodifiableSet(acquiredTags);
    }

    public Set<String> getCompletedChainIds() {
        return Collections.unmodifiableSet(completedChainIds);
    }

    public long getLastObservationTick() {
        return lastObservationTick;
    }

    /**
     * Evaluate all predefined chains. Returns the list of chains that are now
     * complete AND were not previously completed.
     */
    public java.util.List<ObservationChain> evaluateNewlyCompletedChains() {
        java.util.List<ObservationChain> newlyCompleted = new java.util.ArrayList<>();
        for (ObservationChain chain : ObservationChain.ALL_CHAINS) {
            if (completedChainIds.contains(chain.chainId)) continue;
            if (chain.isComplete(noticedKinds)) {
                completeChain(chain.chainId);
                newlyCompleted.add(chain);
            }
        }
        return newlyCompleted;
    }

    // ─── Persistence (player NBT) ──────────────────────────────────────

    /** Save this state into the player's persistent NBT. */
    public void saveToPlayer(ServerPlayer player) {
        CompoundTag root = new CompoundTag();
        ListTag noticedList = new ListTag();
        for (String id : noticedPhenomenaIds) noticedList.add(StringTag.valueOf(id));
        root.put(NBT_NOTICED, noticedList);

        ListTag kindsList = new ListTag();
        for (ObservationPhenomenon.Kind k : noticedKinds) kindsList.add(StringTag.valueOf(k.id));
        root.put(NBT_KINDS, kindsList);

        ListTag tagsList = new ListTag();
        for (KnowledgeTag t : acquiredTags) tagsList.add(StringTag.valueOf(t.id));
        root.put(NBT_TAGS, tagsList);

        ListTag chainsList = new ListTag();
        for (String c : completedChainIds) chainsList.add(StringTag.valueOf(c));
        root.put(NBT_CHAINS, chainsList);

        root.putLong(NBT_LAST_TICK, lastObservationTick);

        player.getPersistentData().put(NBT_ROOT, root);
    }

    /** Load state from the player's persistent NBT. Returns a fresh state. */
    public static PlayerObservationState loadFromPlayer(ServerPlayer player) {
        PlayerObservationState state = new PlayerObservationState();
        CompoundTag root = player.getPersistentData().getCompound(NBT_ROOT);
        if (root.isEmpty()) return state;

        ListTag noticedList = root.getList(NBT_NOTICED, Tag.TAG_STRING);
        for (int i = 0; i < noticedList.size(); i++) {
            state.noticedPhenomenaIds.add(noticedList.getString(i));
        }

        ListTag kindsList = root.getList(NBT_KINDS, Tag.TAG_STRING);
        for (int i = 0; i < kindsList.size(); i++) {
            ObservationPhenomenon.Kind k = ObservationPhenomenon.Kind.fromId(kindsList.getString(i));
            if (k != null) state.noticedKinds.add(k);
        }

        ListTag tagsList = root.getList(NBT_TAGS, Tag.TAG_STRING);
        for (int i = 0; i < tagsList.size(); i++) {
            KnowledgeTag t = KnowledgeTag.fromId(tagsList.getString(i));
            if (t != null) state.acquiredTags.add(t);
        }

        ListTag chainsList = root.getList(NBT_CHAINS, Tag.TAG_STRING);
        for (int i = 0; i < chainsList.size(); i++) {
            state.completedChainIds.add(chainsList.getString(i));
        }

        state.lastObservationTick = root.getLong(NBT_LAST_TICK);
        return state;
    }
}
