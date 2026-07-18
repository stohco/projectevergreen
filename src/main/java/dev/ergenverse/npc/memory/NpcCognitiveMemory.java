package dev.ergenverse.npc.memory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcCognitiveMemory — per-NPC 3-tier memory system (PROJECT_MASTER.md 6.5).
 *
 * <p>Unlike {@code dev.ergenverse.history.NpcMemory} (which is player-centric:
 * "what the player remembers about NPCs"), this system is NPC-centric:
 * "what each NPC remembers about the world, other NPCs, and the player."
 *
 * <h2>Three tiers with decay (6.5)</h2>
 * <table>
 *   <tr><th>Tier</th><th>Content</th><th>Duration</th><th>Max memories</th></tr>
 *   <tr><td>LONG_TERM</td><td>Killed my son, saved my life, stole my treasure,
 *       joined sect, married disciple</td><td>Forever (no decay)</td><td>50</td></tr>
 *   <tr><td>MEDIUM_TERM</td><td>Talked yesterday, borrowed sword, lost duel,
 *       witnessed spirit fruit ripening</td><td>Weeks (decays to short)</td><td>30</td></tr>
 *   <tr><td>SHORT_TERM</td><td>Passed by, asked directions, bought pills,
 *       heard a rumor</td><td>Hours-days (decays to forgotten)</td><td>20</td></tr>
 *   <tr><td>FORGOTTEN</td><td>— (pruned from storage)</td><td>Removed</td><td>0</td></tr>
 * </table>
 *
 * <h2>Memory categories</h2>
 * <p>Each memory has a category that influences how it's used by other
 * Phase B systems (priority queue, internal monologue, dialogue generation):
 * <ul>
 *   <li><b>COMBAT</b> — fought, killed, wounded, was wounded</li>
 *   <li><b>SOCIAL</b> — talked, traded, gifted, received gift, favor</li>
 *   <li><b>OBSERVATION</b> — saw something, noticed something, overheard</li>
 *   <li><b>WORLD_EVENT</b> — spirit fruit ripened, formation activated, beast migration</li>
 *   <li><b>PLAYER_ACTION</b> — player did something notable near this NPC</li>
 *   <li><b>RUMOR</b> — heard a rumor (links to RumorNetwork by rumorId)</li>
 *   <li><b>CULTIVATION</b> — breakthrough, tribulation, technique learned</li>
 *   <li><b>EMOTIONAL</b> — felt fear, joy, anger, grief (drives mood)</li>
 * </ul>
 *
 * <h2>Integration points</h2>
 * <ul>
 *   <li><b>Priority Queue (B.1)</b>: memories influence goal priorities.
 *       A COMBAT memory of being attacked raises DEFEND priority.</li>
 *   <li><b>Internal Monologue (B.5)</b>: recent short-term memories feed
 *       the "Problem" and "Concern" fields.</li>
 *   <li><b>Dialogue Generation (B.7)</b>: long-term memories of the player
 *       influence greeting style and trust.</li>
 *   <li><b>Rumor System (B.3)</b>: RUMOR category memories link to
 *       {@code RumorNetwork} for full distortion chain.</li>
 *   <li><b>Expectation Model (B.6)</b>: OBSERVATION and PLAYER_ACTION
 *       memories feed NPC predictions about the player.</li>
 * </ul>
 *
 * <h2>Performance (6.12)</h2>
 * <p>Per 6.12, ordinary NPCs get lightweight deterministic memory.
 * Only major NPCs run the full cognitive simulation. Memory limits
 * (50/30/20) prevent unbounded growth.
 *
 * @see NpcMemoryTickHandler  the Forge tick subscriber that drives decay
 */
public class NpcCognitiveMemory extends SavedData {

    private static final String DATA_NAME = "ergenverse_npc_cognitive_memory";

    // ─── NBT keys ────────────────────────────────────────────────
    private static final String TAG_NPCS = "npcs";
    private static final String TAG_NPC_ID = "id";
    private static final String TAG_LONG = "l";
    private static final String TAG_MEDIUM = "m";
    private static final String TAG_SHORT = "s";

    // Per-memory NBT keys
    private static final String TAG_MEM_TS = "t";
    private static final String TAG_MEM_CAT = "c";
    private static final String TAG_MEM_DESC = "d";
    private static final String TAG_MEM_TARGET = "tg";
    private static final String TAG_MEM_RUMOR = "r";
    private static final String TAG_MEM_EMOTIONAL_WEIGHT = "ew";

    // ─── Decay configuration (in game ticks) ─────────────────────
    /**
     * Medium-term memories older than this decay to short-term.
     * 14 in-game days = 14 * 24000 = 336,000 ticks.
     */
    public static final long MEDIUM_DECAY_TICKS = 24000L * 14;

    /**
     * Short-term memories older than this are forgotten (removed).
     * 3 in-game days = 72,000 ticks.
     */
    public static final long SHORT_DECAY_TICKS = 24000L * 3;

    /**
     * How often to run the decay tick (in server ticks).
     * 1200 ticks = 1 minute. Decaying every minute is cheap.
     */
    public static final int DECAY_INTERVAL = 1200;

    // ─── Memory capacity per NPC ────────────────────────────────
    public static final int MAX_LONG_TERM = 50;
    public static final int MAX_MEDIUM_TERM = 30;
    public static final int MAX_SHORT_TERM = 20;

    /**
     * Maximum distinct NPCs tracked (prevents unbounded world growth).
     * In practice, only NPCs that have been "activated" (near a player
     * or involved in an event) get memory entries.
     */
    public static final int MAX_TRACKED_NPCS = 500;

    // ─── Memory tier enum ────────────────────────────────────────

    public enum MemoryTier {
        /** Permanent — killed my son, saved my life, stole my treasure. */
        LONG_TERM,
        /** Weeks — talked yesterday, borrowed sword, lost duel. */
        MEDIUM_TERM,
        /** Hours-days — passed by, asked directions, bought pills. */
        SHORT_TERM
    }

    /**
     * Memory category — what kind of thing the NPC remembers.
     */
    public enum MemoryCategory {
        /** Fought, killed, wounded, was wounded. */
        COMBAT,
        /** Talked, traded, gifted, received gift, favor. */
        SOCIAL,
        /** Saw something, noticed something, overheard. */
        OBSERVATION,
        /** Spirit fruit ripened, formation activated, beast migration. */
        WORLD_EVENT,
        /** Player did something notable near this NPC. */
        PLAYER_ACTION,
        /** Heard a rumor (links to RumorNetwork). */
        RUMOR,
        /** Breakthrough, tribulation, technique learned. */
        CULTIVATION,
        /** Felt fear, joy, anger, grief. */
        EMOTIONAL
    }

    // ─── Memory entry (immutable value object) ───────────────────

    /**
     * A single memory entry. Immutable — to modify, create a new instance.
     */
    public static final class MemoryEntry {
        public final long timestamp;
        public final MemoryCategory category;
        public final String description;
        public final String targetId;      // who/what this memory is about (npc id, player uuid, item id, or empty)
        public final String linkedRumorId; // non-empty if category == RUMOR
        public final double emotionalWeight; // -1.0 to +1.0 (only meaningful for EMOTIONAL category)

        public MemoryEntry(long timestamp, MemoryCategory category, String description,
                           String targetId, String linkedRumorId, double emotionalWeight) {
            this.timestamp = timestamp;
            this.category = category;
            this.description = description;
            this.targetId = targetId != null ? targetId : "";
            this.linkedRumorId = linkedRumorId != null ? linkedRumorId : "";
            this.emotionalWeight = emotionalWeight;
        }

        // ─── NBT ──────────────────────────────────────────────

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putLong(TAG_MEM_TS, timestamp);
            tag.putString(TAG_MEM_CAT, category.name());
            tag.putString(TAG_MEM_DESC, description);
            tag.putString(TAG_MEM_TARGET, targetId);
            tag.putString(TAG_MEM_RUMOR, linkedRumorId);
            tag.putDouble(TAG_MEM_EMOTIONAL_WEIGHT, emotionalWeight);
            return tag;
        }

        public static MemoryEntry load(CompoundTag tag) {
            return new MemoryEntry(
                    tag.getLong(TAG_MEM_TS),
                    MemoryCategory.valueOf(tag.getString(TAG_MEM_CAT)),
                    tag.getString(TAG_MEM_DESC),
                    tag.getString(TAG_MEM_TARGET),
                    tag.getString(TAG_MEM_RUMOR),
                    tag.getDouble(TAG_MEM_EMOTIONAL_WEIGHT)
            );
        }
    }

    // ─── Per-NPC memory store ────────────────────────────────────

    /**
     * The complete memory state for one NPC. Contains three tiers
     * that decay over time.
     */
    public static final class NpcMemoryStore {
        private final String npcId;
        private final List<MemoryEntry> longTerm = new ArrayList<>();
        private final List<MemoryEntry> mediumTerm = new ArrayList<>();
        private final List<MemoryEntry> shortTerm = new ArrayList<>();

        public NpcMemoryStore(String npcId) {
            this.npcId = npcId;
        }

        public String getNpcId() { return npcId; }
        public List<MemoryEntry> getLongTerm() { return Collections.unmodifiableList(longTerm); }
        public List<MemoryEntry> getMediumTerm() { return Collections.unmodifiableList(mediumTerm); }
        public List<MemoryEntry> getShortTerm() { return Collections.unmodifiableList(shortTerm); }

        /**
         * Record a new memory at the specified tier.
         * If the tier is full, the oldest memory is removed.
         */
        public void record(MemoryEntry entry, MemoryTier tier) {
            List<MemoryEntry> list = listForTier(tier);
            list.add(entry);
            trimTier(list, maxSizeForTier(tier));
        }

        /**
         * Promote a short-term memory to long-term (e.g., a seemingly
         * trivial observation turns out to be life-changing).
         */
        public void promoteToLongTerm(MemoryEntry entry) {
            shortTerm.remove(entry);
            mediumTerm.remove(entry);
            longTerm.add(entry);
            trimTier(longTerm, MAX_LONG_TERM);
        }

        /**
         * Get all memories across all tiers (for queries that don't
         * care about tier — e.g., "find all COMBAT memories").
         */
        public List<MemoryEntry> allMemories() {
            List<MemoryEntry> all = new ArrayList<>(longTerm.size() + mediumTerm.size() + shortTerm.size());
            all.addAll(longTerm);
            all.addAll(mediumTerm);
            all.addAll(shortTerm);
            return all;
        }

        /**
         * Get all memories of a specific category across all tiers.
         */
        public List<MemoryEntry> byCategory(MemoryCategory cat) {
            List<MemoryEntry> result = new ArrayList<>();
            for (MemoryEntry e : longTerm) { if (e.category == cat) result.add(e); }
            for (MemoryEntry e : mediumTerm) { if (e.category == cat) result.add(e); }
            for (MemoryEntry e : shortTerm) { if (e.category == cat) result.add(e); }
            return result;
        }

        /**
         * Get memories about a specific target (NPC, player, item) across all tiers.
         */
        public List<MemoryEntry> aboutTarget(String targetId) {
            List<MemoryEntry> result = new ArrayList<>();
            for (MemoryEntry e : allMemories()) {
                if (e.targetId.equals(targetId)) result.add(e);
            }
            return result;
        }

        /**
         * Compute emotional state from EMOTIONAL memories.
         * Returns -1.0 (grieving/raging) to +1.0 (joyful/grateful).
         * More recent memories weigh more. Non-EMOTIONAL memories ignored.
         */
        public double computeEmotionalState(long currentTick) {
            double sum = 0;
            double totalWeight = 0;
            for (MemoryEntry e : allMemories()) {
                if (e.category != MemoryCategory.EMOTIONAL) continue;
                // Recency weight: 1.0 for same-tick, decaying for older
                double age = Math.max(0, currentTick - e.timestamp);
                double recency = 1.0 / (1.0 + age / 24000.0); // 1-day half-life
                sum += e.emotionalWeight * recency;
                totalWeight += recency;
            }
            if (totalWeight == 0) return 0.0;
            double raw = sum / totalWeight;
            return Math.max(-1.0, Math.min(1.0, raw));
        }

        /** Count total memories across all tiers. */
        public int totalMemoryCount() {
            return longTerm.size() + mediumTerm.size() + shortTerm.size();
        }

        // ─── NBT ──────────────────────────────────────────────

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_NPC_ID, npcId);
            tag.put(TAG_LONG, saveList(longTerm));
            tag.put(TAG_MEDIUM, saveList(mediumTerm));
            tag.put(TAG_SHORT, saveList(shortTerm));
            return tag;
        }

        public static NpcMemoryStore load(CompoundTag tag) {
            String npcId = tag.getString(TAG_NPC_ID);
            NpcMemoryStore store = new NpcMemoryStore(npcId);
            loadList(tag.getList(TAG_LONG, Tag.TAG_COMPOUND), store.longTerm);
            loadList(tag.getList(TAG_MEDIUM, Tag.TAG_COMPOUND), store.mediumTerm);
            loadList(tag.getList(TAG_SHORT, Tag.TAG_COMPOUND), store.shortTerm);
            return store;
        }

        private static ListTag saveList(List<MemoryEntry> list) {
            ListTag tag = new ListTag();
            for (MemoryEntry e : list) tag.add(e.save());
            return tag;
        }

        private static void loadList(ListTag tag, List<MemoryEntry> out) {
            for (int i = 0; i < tag.size(); i++) {
                out.add(MemoryEntry.load(tag.getCompound(i)));
            }
        }

        private List<MemoryEntry> listForTier(MemoryTier tier) {
            return switch (tier) {
                case LONG_TERM -> longTerm;
                case MEDIUM_TERM -> mediumTerm;
                case SHORT_TERM -> shortTerm;
            };
        }

        private static int maxSizeForTier(MemoryTier tier) {
            return switch (tier) {
                case LONG_TERM -> MAX_LONG_TERM;
                case MEDIUM_TERM -> MAX_MEDIUM_TERM;
                case SHORT_TERM -> MAX_SHORT_TERM;
            };
        }

        private static void trimTier(List<MemoryEntry> list, int max) {
            while (list.size() > max) {
                list.remove(0); // remove oldest
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  NpcCognitiveMemory — the world-level SavedData container
    // ═══════════════════════════════════════════════════════════════

    private final Map<String, NpcMemoryStore> npcStores = new ConcurrentHashMap<>();

    public NpcCognitiveMemory() {}

    public static NpcCognitiveMemory load(CompoundTag tag) {
        NpcCognitiveMemory memory = new NpcCognitiveMemory();
        if (tag.contains(TAG_NPCS, Tag.TAG_LIST)) {
            ListTag npcList = tag.getList(TAG_NPCS, Tag.TAG_COMPOUND);
            for (int i = 0; i < npcList.size(); i++) {
                NpcMemoryStore store = NpcMemoryStore.load(npcList.getCompound(i));
                memory.npcStores.put(store.getNpcId(), store);
            }
        }
        return memory;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag npcList = new ListTag();
        for (NpcMemoryStore store : npcStores.values()) {
            npcList.add(store.save());
        }
        tag.put(TAG_NPCS, npcList);
        return tag;
    }

    public static NpcCognitiveMemory get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(NpcCognitiveMemory::load, NpcCognitiveMemory::new, DATA_NAME);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Public API — memory recording
    // ═══════════════════════════════════════════════════════════════

    /**
     * Record a memory for an NPC.
     *
     * @param npcId      the NPC's canon or graph ID
     * @param entry      the memory entry
     * @param tier       which memory tier to place it in
     */
    public void record(String npcId, MemoryEntry entry, MemoryTier tier) {
        NpcMemoryStore store = getOrCreateStore(npcId);
        store.record(entry, tier);
        setDirty();
    }

    /**
     * Convenience: record a long-term memory (permanent — life events).
     */
    public void recordLongTerm(String npcId, MemoryCategory category,
                                String description, String targetId) {
        record(npcId, new MemoryEntry(0, category, description, targetId, "", 0.0),
                MemoryTier.LONG_TERM);
    }

    /**
     * Convenience: record a medium-term memory (weeks — recent interactions).
     */
    public void recordMediumTerm(String npcId, MemoryCategory category,
                                  String description, String targetId, long tick) {
        record(npcId, new MemoryEntry(tick, category, description, targetId, "", 0.0),
                MemoryTier.MEDIUM_TERM);
    }

    /**
     * Convenience: record a short-term memory (hours-days — fleeting observations).
     */
    public void recordShortTerm(String npcId, MemoryCategory category,
                                 String description, String targetId, long tick) {
        record(npcId, new MemoryEntry(tick, category, description, targetId, "", 0.0),
                MemoryTier.SHORT_TERM);
    }

    /**
     * Convenience: record a rumor memory (linked to RumorNetwork).
     */
    public void recordRumorHeard(String npcId, String rumorId,
                                  String description, long tick) {
        record(npcId, new MemoryEntry(tick, MemoryCategory.RUMOR, description,
                "", rumorId, 0.0), MemoryTier.SHORT_TERM);
    }

    /**
     * Convenience: record an emotional memory.
     */
    public void recordEmotional(String npcId, String description,
                                double emotionalWeight, long tick) {
        record(npcId, new MemoryEntry(tick, MemoryCategory.EMOTIONAL, description,
                "", "", emotionalWeight), MemoryTier.SHORT_TERM);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Public API — queries
    // ═══════════════════════════════════════════════════════════════

    /** Get the memory store for an NPC, or null if none exists. */
    @Nullable
    public NpcMemoryStore getStore(String npcId) {
        return npcStores.get(npcId);
    }

    /** Get or create the memory store for an NPC. */
    public NpcMemoryStore getOrCreateStore(String npcId) {
        return npcStores.computeIfAbsent(npcId, NpcMemoryStore::new);
    }

    /** Get all memories about a specific target for an NPC. */
    public List<MemoryEntry> getMemoriesAbout(String npcId, String targetId) {
        NpcMemoryStore store = npcStores.get(npcId);
        if (store == null) return List.of();
        return store.aboutTarget(targetId);
    }

    /** Get emotional state for an NPC (-1.0 to +1.0). */
    public double getEmotionalState(String npcId, long currentTick) {
        NpcMemoryStore store = npcStores.get(npcId);
        if (store == null) return 0.0;
        return store.computeEmotionalState(currentTick);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Decay tick — called by NpcMemoryTickHandler
    // ═══════════════════════════════════════════════════════════════

    /**
     * Run one decay cycle. Moves medium→short and removes expired short.
     *
     * @param currentTick the current game time
     * @return the number of memories that were decayed or removed
     */
    public int decayTick(long currentTick) {
        int changes = 0;
        Iterator<Map.Entry<String, NpcMemoryStore>> it = npcStores.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, NpcMemoryStore> entry = it.next();
            NpcMemoryStore store = entry.getValue();

            // 1. Medium-term → Short-term (if older than MEDIUM_DECAY_TICKS)
            Iterator<MemoryEntry> medIt = store.mediumTerm.iterator();
            while (medIt.hasNext()) {
                MemoryEntry e = medIt.next();
                if ((currentTick - e.timestamp) > MEDIUM_DECAY_TICKS) {
                    medIt.remove();
                    store.shortTerm.add(e);
                    trimList(store.shortTerm, MAX_SHORT_TERM);
                    changes++;
                }
            }

            // 2. Short-term → Forgotten (if older than SHORT_DECAY_TICKS)
            Iterator<MemoryEntry> shortIt = store.shortTerm.iterator();
            while (shortIt.hasNext()) {
                MemoryEntry e = shortIt.next();
                if ((currentTick - e.timestamp) > SHORT_DECAY_TICKS) {
                    shortIt.remove();
                    changes++;
                }
            }

            // 3. Remove empty NPC stores (cleanup)
            if (store.totalMemoryCount() == 0) {
                it.remove();
            }
        }

        // 4. Cap total tracked NPCs
        while (npcStores.size() > MAX_TRACKED_NPCS) {
            // Remove the NPC with the fewest total memories
            String minId = null;
            int minCount = Integer.MAX_VALUE;
            for (Map.Entry<String, NpcMemoryStore> e : npcStores.entrySet()) {
                int c = e.getValue().totalMemoryCount();
                if (c < minCount) {
                    minCount = c;
                    minId = e.getKey();
                }
            }
            if (minId != null) npcStores.remove(minId);
        }

        if (changes > 0) setDirty();
        return changes;
    }

    /** Get a status report for logging. */
    public String getStatusReport() {
        int totalLong = 0, totalMed = 0, totalShort = 0;
        for (NpcMemoryStore s : npcStores.values()) {
            totalLong += s.getLongTerm().size();
            totalMed += s.getMediumTerm().size();
            totalShort += s.getShortTerm().size();
        }
        return String.format("%d NPCs tracked, %d long / %d medium / %d short (%d total)",
                npcStores.size(), totalLong, totalMed, totalShort,
                totalLong + totalMed + totalShort);
    }

    // ─── Internal ───────────────────────────────────────────────

    private static void trimList(List<MemoryEntry> list, int max) {
        while (list.size() > max) list.remove(0);
    }
}