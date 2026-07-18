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
 * NpcMemory — what each NPC remembers about the player (Layer 3).
 *
 * <p>Canon NPCs have their canon memories (Layer 1 — immutable). New memories
 * formed through interaction with the player are recorded here. An NPC's
 * behavior toward the player is governed by the combination of their canon
 * personality (Layer 1) and their emergent memory of the player (Layer 3).
 *
 * <h2>Memory types</h2>
 * <ul>
 *   <li><b>INTERACTION</b> — the player talked to or right-clicked the NPC.</li>
 *   <li><b>COMBAT</b> — the player attacked the NPC (or vice versa).</li>
 *   <li><b>TRADE</b> — items exchanged between player and NPC.</li>
 *   <li><b>GIFT_GRANTED</b> — the NPC gave something to the player.</li>
 *   <li><b>GIFT_RECEIVED</b> — the NPC received something from the player.</li>
 *   <li><b>FAVOR_GRANTED</b> — the NPC did a favor for the player.</li>
 * </ul>
 *
 * <h2>Trust scoring</h2>
 * <p>Trust is the sum of all trustDeltas for an NPC. It ranges from
 * negative (hostile) to positive (friendly). The ManifestationGiftSystem
 * and future NPC dialogue systems query this.
 *
 * <p>Trust decays: each time the player interacts with an NPC, older memories
 * lose 10% effectiveness. This is computed lazily in {@link #trustScore(String)}
 * via the DECAY_FACTOR on older intervals.
 */
public final class NpcMemory {

    // ─── NBT keys (short for NBT efficiency) ─────────────
    private static final String TAG_NM = "nm";
    private static final String TAG_TS = "ts";
    private static final String TAG_MYT = "myt";
    private static final String TAG_D = "d";
    private static final String TAG_TR = "tr";
    private static final String TAG_NPC_MEMORIES = "npc_mem";
    private static final String TAG_NPC_TRUST = "npc_trust";

    /** Maximum memories per NPC (prevents unbounded growth). */
    private static final int MAX_MEMORIES_PER_NPC = 50;
    /** Maximum distinct NPCs tracked per player. */
    private static final int MAX_NPCS = 200;
    /** Per-interaction trust decay: older memories lose this fraction. */
    private static final double DECAY_FACTOR = 0.9;
    /** Ticks between decay intervals (60000 ticks = 50 real minutes). */
    private static final long TRUST_DECAY_TICKS = 60000L;

    /**
     * A single memory of a player-NPC interaction.
     */
    public static final class Memory {
        public final long timestamp;
        public final String npcCanonId;
        public final String memoryType;
        public final String description;
        public final int trustDelta;

        public Memory(long timestamp, String npcCanonId, String memoryType,
                     String description, int trustDelta) {
            this.timestamp = timestamp;
            this.npcCanonId = npcCanonId;
            this.memoryType = memoryType;
            this.description = description;
            this.trustDelta = trustDelta;
        }
    }

    // ─── Instance fields ────────────────────────────────────────────
    private final String playerUuid;
    private final Map<String, List<Memory>> memories = new HashMap<>();

    private NpcMemory(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    // ─── Access ──────────────────────────────────────────────────────

    /** Get all memories for a specific NPC (newest first). */
    public List<Memory> forNpc(String npcCanonId) {
        return Collections.unmodifiableList(
                memories.getOrDefault(npcCanonId, Collections.emptyList()));
    }

    /**
     * Get the trust score for an NPC with time-based decay.
     *
     * <p>Recent memories (within TRUST_DECAY_TICKS of latest interaction)
     * count at full weight. Older memories are multiplied by DECAY_FACTOR
     * for each TRUST_DECAY_TICKS interval they predate the latest.
     */
    public int trustScore(String npcCanonId) {
        List<Memory> npcMemories = memories.get(npcCanonId);
        if (npcMemories == null || npcMemories.isEmpty()) return 0;

        long latestTimestamp = 0;
        for (Memory m : npcMemories) {
            if (m.timestamp > latestTimestamp) latestTimestamp = m.timestamp;
        }

        double score = 0;
        for (Memory m : npcMemories) {
            int intervals = 0;
            if (latestTimestamp > m.timestamp) {
                intervals = (int) ((latestTimestamp - m.timestamp) / TRUST_DECAY_TICKS);
            }
            double weight = Math.pow(DECAY_FACTOR, intervals);
            score += m.trustDelta * weight;
        }
        return (int) Math.round(score);
    }

    /** Get the NPC IDs that have memories (all NPCs the player has interacted with). */
    public List<String> knownNpcs() {
        return List.copyOf(memories.keySet());
    }

    // ─── Recording ───────────────────────────────────────────

    /** Record a new memory. Trims to MAX_MEMORIES_PER_NPC. */
    public void record(long timestamp, String npcCanonId, String memoryType,
                       String description, int trustDelta) {
        List<Memory> list = memories.computeIfAbsent(npcCanonId,
                k -> new ArrayList<>());
        list.add(new Memory(timestamp, npcCanonId, memoryType, description, trustDelta));
        while (list.size() > MAX_MEMORIES_PER_NPC) {
            list.remove(0);
        }

        // Cap total distinct NPCs
        while (memories.size() > MAX_NPCS) {
            String oldest = memories.keySet().iterator().next();
            memories.remove(oldest);
        }
    }

    // ─── Serialization ───────────────────────────────────────────────

    /**
     * Serialize all NPC memories into a CompoundTag.
     * Structure: npc_memories → [ {npc_id, memories: [entry, ...]}, ... ]
     */
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // Save per-NPC trust scores for quick access
        CompoundTag trustTag = new CompoundTag();
        for (Map.Entry<String, List<Memory>> entry : memories.entrySet()) {
            trustTag.putInt(entry.getKey(), trustScore(entry.getKey()));
        }
        tag.put(TAG_NPC_TRUST, trustTag);

        ListTag npcList = new ListTag();
        for (Map.Entry<String, List<Memory>> entry : memories.entrySet()) {
            CompoundTag npcTag = new CompoundTag();
            npcTag.putString(TAG_NM, entry.getKey());

            ListTag memList = new ListTag();
            for (Memory m : entry.getValue()) {
                CompoundTag mTag = new CompoundTag();
                mTag.putLong(TAG_TS, m.timestamp);
                mTag.putString(TAG_MYT, m.memoryType);
                mTag.putString(TAG_D, m.description);
                mTag.putInt(TAG_TR, m.trustDelta);
                memList.add(mTag);
            }
            npcTag.put(TAG_D, memList);
            npcList.add(npcTag);
        }
        tag.put(TAG_NPC_MEMORIES, npcList);
        return tag;
    }

    /**
     * Deserialize from a CompoundTag.
     */
    public static NpcMemory load(String playerUuid, CompoundTag tag) {
        NpcMemory memory = new NpcMemory(playerUuid);

        if (tag.contains(TAG_NPC_MEMORIES, Tag.TAG_LIST)) {
            ListTag npcList = tag.getList(TAG_NPC_MEMORIES, Tag.TAG_COMPOUND);
            for (int i = 0; i < npcList.size(); i++) {
                CompoundTag npcTag = npcList.getCompound(i);
                String npcId = npcTag.getString(TAG_NM);
                List<Memory> mems = new ArrayList<>();
                for (int j = 0; j < npcTag.getList(TAG_D, Tag.TAG_COMPOUND).size(); j++) {
                    CompoundTag mTag = npcTag.getList(TAG_D, Tag.TAG_COMPOUND).getCompound(j);
                    mems.add(new Memory(
                            mTag.getLong(TAG_TS),
                            npcId,
                            mTag.getString(TAG_MYT),
                            mTag.getString(TAG_D),
                            mTag.getInt(TAG_TR)
                    ));
                }
                memory.memories.put(npcId, mems);
            }
        }
        return memory;
    }

    // ─── Static convenience methods ──────────────────────────────────────

    public static void recordInteraction(ServerPlayer player, String npcCanonId,
                                        String detail, long worldTick) {
        NpcMemory m = get(player);
        m.record(worldTick, npcCanonId, "INTERACTION",
                detail, 1);
        persist(player, m);
    }

    public static void recordCombat(ServerPlayer player, String npcCanonId,
                                     String outcome, boolean playerWon, long worldTick) {
        NpcMemory m = get(player);
        m.record(worldTick, npcCanonId, "COMBAT",
                outcome, playerWon ? 5 : -10);
        persist(player, m);
    }

    public static void recordFavorBroken(ServerPlayer player, String npcCanonId,
                                           String detail, long worldTick) {
        NpcMemory m = get(player);
        m.record(worldTick, npcCanonId, "FAVOR_BROKEN",
                detail, -15);
        persist(player, m);
    }

    // ─── Internal: get/persist via WorldRuntimeState ─────────────────

    public static NpcMemory get(ServerPlayer player) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);

        if (playerTag.contains(TAG_NPC_MEMORIES, Tag.TAG_LIST)) {
            return load(uuid, playerTag);
        }
        return new NpcMemory(uuid);
    }

    private static void persist(ServerPlayer player, NpcMemory memory) {
        var runtimeState = dev.ergenverse.simulation.WorldRuntimeState.get(player.serverLevel());
        String uuid = player.getStringUUID();
        CompoundTag playerTag = runtimeState.getPlayerMutation(uuid);
        CompoundTag savedMem = memory.save();
        playerTag.put(TAG_NPC_MEMORIES, savedMem.getList(TAG_NPC_MEMORIES, Tag.TAG_LIST));
        runtimeState.updatePlayerMutation(uuid, playerTag);
    }
}