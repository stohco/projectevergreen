package dev.ergenverse.npc.rumor;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RumorNetwork — the persistence layer and propagation engine for the
 * rumor system (PROJECT_MASTER.md 6.11).
 *
 * <p>This is the central class that:
 * <ul>
 *   <li>Stores all active rumors (persisted via SavedData)</li>
 *   <li>Tracks which NPCs know which rumors</li>
 *   <li>Advances propagation on each tick cycle</li>
 *   <li>Kills expired rumors</li>
 * </ul>
 *
 * <h2>Integration with WorldGraph</h2>
 * <p>Rumors are modeled as RUMOR nodes in the WorldGraph. When a rumor
 * is created, a node is added. When an NPC learns a rumor, a HEARD edge
 * (or MEMBER_OF / ALLY_OF traversal) links them. The graph's walk()
 * method is used to find reachable NPCs for propagation.
 *
 * <p>NOTE: Full WorldGraph integration is deferred — the initial version
 * uses a simple in-memory NPC proximity model. Phase B.7 (world-sim-tick)
 * will replace this with graph-based propagation.
 *
 * <h2>Performance</h2>
 * <p>Per 6.12, ordinary NPCs use lightweight deterministic propagation.
 * Only major NPCs (sect leaders, Wang Lin) run the full cognitive
 * simulation. This means most rumors just tick their cooldown and
 * advance one hop without any planning logic.
 *
 * <h2>Rumor lifecycle</h2>
 * <pre>
 *   Created → Active (propagating) → Exhausted (max hops or veracity=0) → Dead
 *                                              ↓
 *                              Disproven (NPC witnessed truth) → Dead
 * </pre>
 *
 * @see Rumor              the data model
 * @see RumorDistortion    per-hop content morphing
 * @see RumorEngineEvents  the Forge tick subscriber
 */
public class RumorNetwork extends SavedData {

    private static final String DATA_NAME = "ergenverse_rumor_network";

    // ─── NBT keys ────────────────────────────────────────────────
    private static final String TAG_RUMORS = "rumors";
    private static final String TAG_NPC_KNOWLEDGE = "nk";

    // ─── Configuration ───────────────────────────────────────────
    /** How often to tick propagation (in server ticks). 600 = 30 seconds. */
    public static final int PROPAGATION_INTERVAL = 600;

    /** Maximum active rumors in the network (prevent unbounded growth). */
    public static final int MAX_RUMORS = 200;

    /** Maximum age of a rumor in ticks before it dies (7 in-game days). */
    public static final long MAX_AGE_TICKS = 24000L * 7;

    /** How many rumors to propagate per tick cycle (spread the load). */
    public static final int RUMORS_PER_CYCLE = 3;

    // ─── State ───────────────────────────────────────────────────
    private final Map<String, Rumor> rumors = new ConcurrentHashMap<>();
    /** npcId → set of rumorIds the NPC has heard. */
    private final Map<String, Set<String>> npcKnowledge = new ConcurrentHashMap<>();
    /** Queue of rumor IDs pending propagation (FIFO). */
    private final Deque<String> propagationQueue = new ArrayDeque<>();

    // ─── Singleton access ────────────────────────────────────────

    public RumorNetwork() {}

    public static RumorNetwork load(CompoundTag tag) {
        RumorNetwork network = new RumorNetwork();
        if (tag.contains(TAG_RUMORS, Tag.TAG_LIST)) {
            ListTag rumorList = tag.getList(TAG_RUMORS, Tag.TAG_COMPOUND);
            for (int i = 0; i < rumorList.size(); i++) {
                Rumor rumor = Rumor.load(rumorList.getCompound(i));
                network.rumors.put(rumor.getRumorId(), rumor);
                if (rumor.canPropagate()) {
                    network.propagationQueue.add(rumor.getRumorId());
                }
            }
        }
        if (tag.contains(TAG_NPC_KNOWLEDGE, Tag.TAG_LIST)) {
            ListTag nkList = tag.getList(TAG_NPC_KNOWLEDGE, Tag.TAG_COMPOUND);
            for (int i = 0; i < nkList.size(); i++) {
                CompoundTag nkTag = nkList.getCompound(i);
                String npcId = nkTag.getString("npc");
                Set<String> known = ConcurrentHashMap.newKeySet();
                if (nkTag.contains("r", Tag.TAG_LIST)) {
                    ListTag rList = nkTag.getList("r", Tag.TAG_STRING);
                    for (int j = 0; j < rList.size(); j++) {
                        known.add(rList.getString(j));
                    }
                }
                network.npcKnowledge.put(npcId, known);
            }
        }
        return network;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag rumorList = new ListTag();
        for (Rumor rumor : rumors.values()) {
            rumorList.add(rumor.save());
        }
        tag.put(TAG_RUMORS, rumorList);

        ListTag nkList = new ListTag();
        for (Map.Entry<String, Set<String>> entry : npcKnowledge.entrySet()) {
            CompoundTag nkTag = new CompoundTag();
            nkTag.putString("npc", entry.getKey());
            ListTag rList = new ListTag();
            for (String rId : entry.getValue()) {
                rList.add(net.minecraft.nbt.StringTag.valueOf(rId));
            }
            nkTag.put("r", rList);
            nkList.add(nkTag);
        }
        tag.put(TAG_NPC_KNOWLEDGE, nkList);
        return tag;
    }

    public static RumorNetwork get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(RumorNetwork::load, RumorNetwork::new, DATA_NAME);
    }

    // ═══════════════════════════════════════════════════════════════
    //  Rumor Creation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Create a new rumor from a source event.
     *
     * @param content      the original (true) content of the rumor
     * @param sourceNpcId  the NPC that witnessed the event (can be empty for world events)
     * @param originType   what kind of event spawned this rumor
     * @param locationHint specific location (e.g. "Mosquito Valley")
     * @param currentTick  the current game time in ticks
     * @return the created rumor, or null if the network is full
     */
    @Nullable
    public Rumor createRumor(String content, String sourceNpcId,
                              Rumor.OriginType originType, String locationHint,
                              long currentTick) {
        if (rumors.size() >= MAX_RUMORS) {
            // Kill the oldest rumor to make room
            killOldestRumor();
        }

        Rumor rumor = Rumor.builder()
                .sourceNpcId(sourceNpcId)
                .currentContent(content)
                .originalContent(content)
                .bornTick(currentTick)
                .lastPropagatedTick(currentTick)
                .veracity(1.0)
                .locationHint(locationHint)
                .originType(originType)
                .chainEntry(new Rumor.ChainEntry(content,
                        sourceNpcId.isEmpty()
                                ? Rumor.ActorClass.MORTAL
                                : Rumor.ActorClass.SECT_SCOUT,
                        0, 1.0))
                .build();

        rumors.put(rumor.getRumorId(), rumor);
        propagationQueue.add(rumor.getRumorId());

        // Source NPC knows the rumor at full veracity
        if (!sourceNpcId.isEmpty()) {
            npcKnowledge.computeIfAbsent(sourceNpcId, k -> ConcurrentHashMap.newKeySet())
                    .add(rumor.getRumorId());
        }

        setDirty();
        Ergenverse.LOGGER.debug("[RumorNetwork] Created rumor {}: '{}' (source={}, type={})",
                rumor.getRumorId(), content, sourceNpcId, originType);
        return rumor;
    }

    // ═══════════════════════════════════════════════════════════════
    //  Propagation Tick
    // ═══════════════════════════════════════════════════════════════

    /**
     * Advance the rumor network by one tick cycle.
     * Propagates up to RUMORS_PER_CYCLE rumors and kills expired ones.
     *
     * @param level the server level (for random source)
     * @param tick  the current game time
     */
    public void tick(ServerLevel level, long tick) {
        java.util.Random random = new java.util.Random(level.getRandom().nextLong());

        // 1. Kill expired rumors
        killExpiredRumors(tick);

        // 2. Propagate pending rumors
        int propagated = 0;
        Iterator<String> it = propagationQueue.iterator();
        while (it.hasNext() && propagated < RUMORS_PER_CYCLE) {
            String rumorId = it.next();
            Rumor rumor = rumors.get(rumorId);
            if (rumor == null || !rumor.canPropagate()) {
                it.remove();
                continue;
            }

            // Check if enough time has passed for propagation
            // Use a simplified delay: base 1200 ticks (1 minute) per hop for now
            // Real delay is per-actor-class and computed in RumorDistortion
            long minDelay = 1200L + rumor.getHopCount() * 600L;
            if (tick - rumor.getLastPropagatedTick() < minDelay) {
                continue; // Not ready yet, keep in queue
            }

            // Pick a random actor class for this propagation hop
            Rumor.ActorClass actor = pickRandomActor(random);

            // Apply distortion
            Rumor distorted = RumorDistortion.applyHop(rumor, actor, random);
            // Fix the lastPropagatedTick to the real current tick
            distorted = distorted.withHop(
                    distorted.getCurrentContent(),
                    distorted.getVeracity(),
                    distorted.getChain().get(distorted.getChain().size() - 1).actor,
                    distorted.getLocationHint(),
                    tick);

            // Update the rumor in the network
            rumors.put(rumorId, distorted);

            // NPC knowledge: pick 1-3 random NPCs that "hear" this hop
            int spreadCount = RumorDistortion.getSpreadCount(actor, random);
            for (int i = 0; i < spreadCount; i++) {
                // For now, generate a synthetic NPC ID based on actor class
                // In Phase B.8 (world-sim-tick), this will use real NPC positions
                String npcId = "npc_" + actor.name().toLowerCase() + "_" +
                        random.nextInt(100);
                npcKnowledge.computeIfAbsent(npcId, k -> ConcurrentHashMap.newKeySet())
                        .add(rumorId);
            }

            propagated++;

            if (distorted.canPropagate()) {
                // Re-queue for next hop
                it.remove();
                propagationQueue.addLast(rumorId);
            } else {
                it.remove();
                if (distorted.getVeracity() <= 0.01 || distorted.getHopCount() >= Rumor.MAX_HOPS) {
                    // Mark as dead and store
                    rumors.put(rumorId, distorted.markDead());
                    Ergenverse.LOGGER.debug("[RumorNetwork] Rumor {} exhausted: hops={}, veracity={:.2f}",
                            rumorId, distorted.getHopCount(), distorted.getVeracity());
                }
            }

            setDirty();
        }

        if (propagated > 0) {
            Ergenverse.LOGGER.debug("[RumorNetwork] Propagated {} rumors this cycle. Active: {}, Dead: {}",
                    propagated, getActiveCount(), getDeadCount());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Queries
    // ═══════════════════════════════════════════════════════════════

    /** Get a rumor by ID, or null. */
    @Nullable
    public Rumor getRumor(String rumorId) {
        return rumors.get(rumorId);
    }

    /** Get all rumors an NPC knows about. */
    public List<Rumor> getRumorsKnownBy(String npcId) {
        Set<String> known = npcKnowledge.get(npcId);
        if (known == null || known.isEmpty()) return List.of();
        List<Rumor> result = new ArrayList<>();
        for (String id : known) {
            Rumor r = rumors.get(id);
            if (r != null) result.add(r);
        }
        return result;
    }

    /** Get the most distorted (lowest veracity) version of a rumor. */
    @Nullable
    public Rumor getMostDistortedVersion(String originalContent) {
        Rumor best = null;
        for (Rumor r : rumors.values()) {
            if (r.getOriginalContent().equals(originalContent)) {
                if (best == null || r.getVeracity() < best.getVeracity()) {
                    best = r;
                }
            }
        }
        return best;
    }

    /** Count of active (non-dead) rumors. */
    public int getActiveCount() {
        int count = 0;
        for (Rumor r : rumors.values()) {
            if (!r.isDead()) count++;
        }
        return count;
    }

    /** Count of dead rumors. */
    public int getDeadCount() {
        int count = 0;
        for (Rumor r : rumors.values()) {
            if (r.isDead()) count++;
        }
        return count;
    }

    /** Total rumors (active + dead). */
    public int getTotalCount() {
        return rumors.size();
    }

    /** Count of NPCs who know at least one rumor. */
    public int getInformedNpcCount() {
        int count = 0;
        for (Set<String> s : npcKnowledge.values()) {
            if (!s.isEmpty()) count++;
        }
        return count;
    }

    /** Get a status report for logging. */
    public String getStatusReport() {
        return String.format("%d active, %d dead, %d NPCs informed, %d in queue",
                getActiveCount(), getDeadCount(), getInformedNpcCount(), propagationQueue.size());
    }

    // ═══════════════════════════════════════════════════════════════
    //  Internal helpers
    // ═══════════════════════════════════════════════════════════════

    private void killExpiredRumors(long tick) {
        boolean anyKilled = false;
        for (Map.Entry<String, Rumor> entry : rumors.entrySet()) {
            Rumor r = entry.getValue();
            if (!r.isDead() && (tick - r.getBornTick()) > MAX_AGE_TICKS) {
                entry.setValue(r.markDead());
                propagationQueue.remove(r.getRumorId());
                anyKilled = true;
            }
        }
        // Prune fully dead rumors that are very old (keep last 50 dead for history)
        if (rumors.size() > MAX_RUMORS + 50) {
            pruneDeadRumors(50);
        }
        if (anyKilled) setDirty();
    }

    private void killOldestRumor() {
        String oldestId = null;
        long oldestTick = Long.MAX_VALUE;
        for (Map.Entry<String, Rumor> entry : rumors.entrySet()) {
            if (!entry.getValue().isDead() && entry.getValue().getBornTick() < oldestTick) {
                oldestTick = entry.getValue().getBornTick();
                oldestId = entry.getKey();
            }
        }
        if (oldestId != null) {
            Rumor r = rumors.get(oldestId);
            rumors.put(oldestId, r.markDead());
            propagationQueue.remove(oldestId);
            setDirty();
        }
    }

    private void pruneDeadRumors(int keepCount) {
        List<Map.Entry<String, Rumor>> dead = new ArrayList<>();
        for (Map.Entry<String, Rumor> e : rumors.entrySet()) {
            if (e.getValue().isDead()) dead.add(e);
        }
        // Sort by born tick descending (newest first)
        dead.sort((a, b) -> Long.compare(b.getValue().getBornTick(), a.getValue().getBornTick()));
        // Remove all but the newest `keepCount` dead rumors
        for (int i = keepCount; i < dead.size(); i++) {
            String id = dead.get(i).getKey();
            rumors.remove(id);
            // Also clean NPC knowledge
            for (Set<String> known : npcKnowledge.values()) {
                known.remove(id);
            }
        }
        setDirty();
    }

    /**
     * Pick a random actor class weighted by what would realistically carry a rumor.
     * Mortals and wandering cultivators are most common; elders are rare.
     */
    private static Rumor.ActorClass pickRandomActor(java.util.Random random) {
        double roll = random.nextDouble();
        if (roll < 0.35) return Rumor.ActorClass.MORTAL;
        if (roll < 0.55) return Rumor.ActorClass.WANDERING_CULTIVATOR;
        if (roll < 0.70) return Rumor.ActorClass.MERCHANT;
        if (roll < 0.85) return Rumor.ActorClass.SECT_SCOUT;
        if (roll < 0.95) return Rumor.ActorClass.SECT_ELDER;
        return Rumor.ActorClass.MORTAL; // fallback
    }
}