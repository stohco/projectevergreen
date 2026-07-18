package dev.ergenverse.npc.expectation;

import dev.ergenverse.wanglin.ai.reasoning.Prediction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcExpectationNetwork — generalized NPC expectation model (PROJECT_MASTER.md 6.13).
 *
 * <p>Extends the concept from {@code dev.ergenverse.wanglin.ai.reasoning.ExpectationModel}
 * (which is Wang-Lin-specific and per-player) to ALL important NPCs forming predictions
 * about ANY target (player, other NPC, faction, or abstract entity).
 *
 * <h2>Core philosophy: Prediction not Reaction (6.8)</h2>
 * <p>"NPCs don't react to the player. They predict the player."
 * A sect elder doesn't wait for the player to attack — she predicts the player
 * is a spy (90% confidence) and assigns watchers. When the prediction proves
 * wrong, she updates her model. This makes NPCs feel alive because they can
 * make MISTAKES.
 *
 * <h2>Data model</h2>
 * <p>Each NPC has a set of {@link TargetExpectation}s — one per target they
 * are tracking. Each target expectation holds a map of {@link Prediction}s.
 * <ul>
 *   <li>NPC "sect_elder_zhang" tracks player UUID "abc-123" with predictions
 *       about their cultivation path, combat ability, and trustworthiness.</li>
 *   <li>NPC "wang_lin" tracks "player_abc" with predictions about dao pursuit,
 *       survival, and hoarding behavior (via the specialized Wang Lin system).</li>
 *   <li>NPC "merchant_liu" tracks "wandering_cultivator_42" with predictions
 *       about their purchasing patterns.</li>
 * </ul>
 *
 * <h2>Memory-driven inference (6.13)</h2>
 * <p>Predictions are not set manually (except for canonical seeds). They are
 * INFERRED from {@code NpcCognitiveMemory} observations:
 * <ul>
 *   <li>OBSERVATION memories about a target → update relevant predictions</li>
 *   <li>PLAYER_ACTION memories → update behavioral predictions</li>
 *   <li>COMBAT memories → update combat-ability predictions</li>
 *   <li>SOCIAL memories → update trust/relationship predictions</li>
 * </ul>
 * The inference is run periodically by {@link NpcExpectationTickHandler}.
 *
 * <h2>Realm-scaled belief (6.6)</h2>
 * <p>Higher-realm NPCs are more resistant to forming new predictions from
 * sparse evidence (they've seen centuries of deception). The
 * {@code NpcRealmCognition.getRumorBelievability()} value modulates the
 * confidence increment from each observation. A mortal merchant might form
 * a prediction at +0.08 per observation; an ancient Soul Formation elder
 * might only shift by +0.02.
 *
 * <h2>Prediction decay (6.13.1)</h2>
 * <p>Predictions decay at 1% per 24000 ticks (1 in-game day), same as the
 * Wang Lin system. Predictions below 2% confidence are pruned.
 *
 * <h2>Canonical prediction IDs</h2>
 * <p>These are stable identifiers used across all NPCs:
 * <ul>
 *   <li><b>combat_able</b> — target can hold their own in combat</li>
 *   <li><b>cultivation_talent_X</b> — target has talent in dao X (restriction, sword, alchemy, etc.)</li>
 *   <li><b>trustworthy</b> — target can be trusted</li>
 *   <li><b>hostile_intent</b> — target harbors hostile intent toward this NPC</li>
 *   <li><b>seeking_power</b> — target is actively seeking power/resources</li>
 *   <li><b>will_betray</b> — target is likely to betray alliances</li>
 *   <li><b>valuable_ally</b> — target would be a valuable ally</li>
 *   <li><b>dangerous_if_provoked</b> — target is dangerous if provoked</li>
 * </ul>
 *
 * <h2>Performance (6.12)</h2>
 * <p>Maximum 200 tracked NPC-target pairs. Each pair holds at most 12 predictions.
 * Only NPCs with entries in {@code NpcGoalQueue} (i.e., activated NPCs) get
 * expectation stores. Ordinary villagers do NOT run the expectation model.
 *
 * <h2>Relationship to Wang Lin's system</h2>
 * <p>The Wang Lin-specific system ({@code wanglin.ai.reasoning.ExpectationModel})
 * is per-player, stored on player persistent NBT, and has specialized hooks
 * (restriction-dao detection, hoarding detection, challenge survival). This
 * generic system is per-NPC-per-target, stored as world SavedData, and has
 * generalized inference from memory categories. Wang Lin's system is NOT
 * replaced — it continues to drive his specialized gifting reasoning. This
 * system provides the generic infrastructure for ALL other NPCs.
 *
 * @see Prediction  reused immutable value object from the Wang Lin reasoning package
 * @see NpcExpectationTickHandler  the Forge tick subscriber that drives inference and decay
 */
public class NpcExpectationNetwork extends SavedData {

    private static final String DATA_NAME = "ergenverse_npc_expectation_network";

    // ─── NBT keys ───────────────────────────────────────────────────
    private static final String TAG_PAIRS = "pairs";
    private static final String TAG_NPC_ID = "nid";
    private static final String TAG_TARGET_ID = "tid";
    private static final String TAG_PREDICTIONS = "preds";

    // ─── Limits ─────────────────────────────────────────────────────
    /** Maximum NPC-target pairs tracked (prevents unbounded growth). */
    public static final int MAX_PAIRS = 200;

    /** Maximum predictions per NPC-target pair. */
    public static final int MAX_PREDICTIONS_PER_PAIR = 12;

    // ─── Decay (same as Wang Lin system, 6.13.1) ────────────────────
    public static final long DECAY_INTERVAL_TICKS = 24000L;
    public static final double DECAY_PER_INTERVAL = 0.01;
    public static final double CONFIDENCE_CAP = 0.95;
    public static final double PRUNE_THRESHOLD = 0.02;

    /** How often to run inference (server ticks). 600 ticks = 30 seconds. */
    public static final int INFERENCE_INTERVAL_TICKS = 600;

    // ─── Canonical prediction IDs ───────────────────────────────────
    public static final String PRED_COMBAT_ABLE = "combat_able";
    public static final String PRED_CULTIVATION_TALENT = "cultivation_talent";
    public static final String PRED_TRUSTWORTHY = "trustworthy";
    public static final String PRED_HOSTILE_INTENT = "hostile_intent";
    public static final String PRED_SEEKING_POWER = "seeking_power";
    public static final String PRED_WILL_BETRAY = "will_betray";
    public static final String PRED_VALUABLE_ALLY = "valuable_ally";
    public static final String PRED_DANGEROUS_IF_PROVOKED = "dangerous_if_provoked";

    // ─── Data store ─────────────────────────────────────────────────

    /**
     * The complete set of predictions one NPC holds about one target.
     * Immutable value object — to modify, create a new instance.
     */
    public static final class TargetExpectation {
        public final String npcId;
        public final String targetId;
        private final Map<String, Prediction> predictions;

        public TargetExpectation(String npcId, String targetId) {
            this.npcId = npcId;
            this.targetId = targetId;
            this.predictions = new LinkedHashMap<>();
        }

        /** Private copy constructor for withPrediction. */
        private TargetExpectation(String npcId, String targetId,
                                  Map<String, Prediction> predictions) {
            this.npcId = npcId;
            this.targetId = targetId;
            this.predictions = new LinkedHashMap<>(predictions);
        }

        /** Get a prediction by ID, or null. */
        @Nullable
        public Prediction getPrediction(String id) {
            return predictions.get(id);
        }

        /** Confidence of a prediction, or 0.0. */
        public double confidenceOf(String id) {
            Prediction p = predictions.get(id);
            return p == null ? 0.0 : p.confidence;
        }

        /** Whether a prediction exists at confidence >= threshold. */
        public boolean hasStrongPrediction(String id, double threshold) {
            return confidenceOf(id) >= threshold;
        }

        /** All predictions, unmodifiable. */
        public Collection<Prediction> allPredictions() {
            return Collections.unmodifiableCollection(predictions.values());
        }

        /** Number of active predictions. */
        public int size() {
            return predictions.size();
        }

        /** Return a new TargetExpectation with the prediction added/updated. */
        public TargetExpectation withPrediction(Prediction pred) {
            if (predictions.size() >= MAX_PREDICTIONS_PER_PAIR
                    && !predictions.containsKey(pred.id)) {
                // Full — prune the lowest-confidence prediction to make room.
                String lowestKey = null;
                double lowestConf = Double.MAX_VALUE;
                for (Map.Entry<String, Prediction> e : predictions.entrySet()) {
                    if (e.getValue().confidence < lowestConf) {
                        lowestConf = e.getValue().confidence;
                        lowestKey = e.getKey();
                    }
                }
                if (lowestKey != null) {
                    predictions.remove(lowestKey);
                }
            }
            TargetExpectation copy = new TargetExpectation(npcId, targetId, predictions);
            copy.predictions.put(pred.id, pred);
            return copy;
        }

        /** Return a new TargetExpectation with decayed predictions. */
        public TargetExpectation withDecay(long currentTick) {
            Map<String, Prediction> decayed = new LinkedHashMap<>();
            for (Map.Entry<String, Prediction> e : predictions.entrySet()) {
                Prediction p = e.getValue();
                long delta = currentTick - p.lastUpdatedTick;
                if (delta <= 0) {
                    decayed.put(e.getKey(), p);
                    continue;
                }
                long intervals = delta / DECAY_INTERVAL_TICKS;
                if (intervals <= 0) {
                    decayed.put(e.getKey(), p);
                    continue;
                }
                double decayAmount = intervals * DECAY_PER_INTERVAL;
                Prediction dp = p.withDecayedConfidence(decayAmount);
                if (dp.confidence > PRUNE_THRESHOLD) {
                    decayed.put(e.getKey(), dp);
                }
                // Below threshold → pruned (not added to decayed map)
            }
            return new TargetExpectation(npcId, targetId, decayed);
        }

        /** Top N predictions by confidence, descending. */
        public List<Prediction> topPredictions(int n) {
            return predictions.values().stream()
                    .sorted((a, b) -> Double.compare(b.confidence, a.confidence))
                    .limit(n)
                    .toList();
        }

        // ─── NBT ──────────────────────────────────────────────────

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_NPC_ID, npcId);
            tag.putString(TAG_TARGET_ID, targetId);
            ListTag predList = new ListTag();
            for (Prediction p : predictions.values()) {
                predList.add(p.serializeNBT());
            }
            tag.put(TAG_PREDICTIONS, predList);
            return tag;
        }

        public static TargetExpectation load(CompoundTag tag) {
            String npcId = tag.getString(TAG_NPC_ID);
            String targetId = tag.getString(TAG_TARGET_ID);
            TargetExpectation te = new TargetExpectation(npcId, targetId);
            ListTag predList = tag.getList(TAG_PREDICTIONS, Tag.TAG_COMPOUND);
            for (int i = 0; i < predList.size(); i++) {
                Prediction p = Prediction.deserializeNBT(predList.getCompound(i));
                te.predictions.put(p.id, p);
            }
            return te;
        }
    }

    // ─── Main store ─────────────────────────────────────────────────

    /**
     * Nested map: npcId -> targetId -> TargetExpectation.
     * Using ConcurrentHashMap for thread safety during iteration.
     */
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, TargetExpectation>> network = new ConcurrentHashMap<>();

    public NpcExpectationNetwork() {}

    // ─── Static SavedData access ────────────────────────────────────

    private static NpcExpectationNetwork cached;

    public static NpcExpectationNetwork get(ServerLevel level) {
        if (cached != null && !cached.isDirty()) return cached;
        cached = level.getDataStorage()
                .computeIfAbsent(NpcExpectationNetwork::load, NpcExpectationNetwork::new, DATA_NAME);
        return cached;
    }

    public static NpcExpectationNetwork load(CompoundTag tag) {
        NpcExpectationNetwork net = new NpcExpectationNetwork();
        ListTag pairs = tag.getList(TAG_PAIRS, Tag.TAG_COMPOUND);
        for (int i = 0; i < pairs.size(); i++) {
            CompoundTag pairTag = pairs.getCompound(i);
            TargetExpectation te = TargetExpectation.load(pairTag);
            net.network.computeIfAbsent(te.npcId, k -> new ConcurrentHashMap<>())
                    .put(te.targetId, te);
        }
        return net;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag pairs = new ListTag();
        for (Map<String, TargetExpectation> targetMap : network.values()) {
            for (TargetExpectation te : targetMap.values()) {
                pairs.add(te.save());
            }
        }
        tag.put(TAG_PAIRS, pairs);
        return tag;
    }

    // ─── Public query API ───────────────────────────────────────────

    /** Get the TargetExpectation for an NPC about a target, or null. */
    @Nullable
    public TargetExpectation getExpectation(String npcId, String targetId) {
        Map<String, TargetExpectation> targets = network.get(npcId);
        if (targets == null) return null;
        return targets.get(targetId);
    }

    /** Confidence of a specific prediction an NPC holds about a target. */
    public double confidenceOf(String npcId, String targetId, String predictionId) {
        TargetExpectation te = getExpectation(npcId, targetId);
        if (te == null) return 0.0;
        return te.confidenceOf(predictionId);
    }

    /** Whether an NPC has a strong prediction (>= threshold) about a target. */
    public boolean hasStrongPrediction(String npcId, String targetId,
                                       String predictionId, double threshold) {
        return confidenceOf(npcId, targetId, predictionId) >= threshold;
    }

    /** Get top N predictions an NPC holds about a target. */
    public List<Prediction> getTopPredictions(String npcId, String targetId, int n) {
        TargetExpectation te = getExpectation(npcId, targetId);
        if (te == null) return List.of();
        return te.topPredictions(n);
    }

    /** Get all targets an NPC is tracking. */
    public Set<String> getTrackedTargets(String npcId) {
        Map<String, TargetExpectation> targets = network.get(npcId);
        if (targets == null) return Set.of();
        return Collections.unmodifiableSet(targets.keySet());
    }

    /** Get all NPCs that are tracking a specific target. */
    public Set<String> getObserversOf(String targetId) {
        Set<String> observers = new HashSet<>();
        for (Map.Entry<String, ConcurrentHashMap<String, TargetExpectation>> entry : network.entrySet()) {
            if (entry.getValue().containsKey(targetId)) {
                observers.add(entry.getKey());
            }
        }
        return observers;
    }

    /** Total number of tracked NPC-target pairs. */
    public int totalPairs() {
        int count = 0;
        for (Map<String, TargetExpectation> targets : network.values()) {
            count += targets.size();
        }
        return count;
    }

    /** Set of all NPC IDs with any expectations. */
    public Set<String> allNpcIds() {
        return Collections.unmodifiableSet(network.keySet());
    }

    // ─── Mutation API (called by tick handler) ──────────────────────

    /**
     * Update or create a prediction for an NPC about a target.
     * If the NPC-target pair doesn't exist, it is created (subject to MAX_PAIRS).
     *
     * @param npcId       the predicting NPC's character ID
     * @param targetId    the target being predicted (player UUID, NPC ID, etc.)
     * @param id          prediction ID (canonical or custom)
     * @param description human-readable statement
     * @param confidence  new confidence (clamped to [0, CONFIDENCE_CAP])
     * @param worldTick   current game tick
     * @param evidence    what observations led to this
     * @return true if the prediction was stored, false if at capacity
     */
    public boolean updatePrediction(String npcId, String targetId, String id,
                                    String description, double confidence,
                                    long worldTick, String evidence) {
        if (npcId == null || targetId == null || id == null || id.isBlank()) return false;
        // Clamp confidence
        if (confidence < 0.0) confidence = 0.0;
        if (confidence > CONFIDENCE_CAP) confidence = CONFIDENCE_CAP;

        ConcurrentHashMap<String, TargetExpectation> targets = network.get(npcId);
        if (targets == null) {
            // Check capacity before creating a new NPC entry
            if (totalPairs() >= MAX_PAIRS) return false;
            targets = new ConcurrentHashMap<>();
            network.put(npcId, targets);
        }

        TargetExpectation existing = targets.get(targetId);
        if (existing == null) {
            if (totalPairs() >= MAX_PAIRS) return false;
            existing = new TargetExpectation(npcId, targetId);
        }

        // Preserve existing description if new one is blank
        String finalDesc = description;
        if ((finalDesc == null || finalDesc.isBlank()) && existing.getPrediction(id) != null) {
            finalDesc = existing.getPrediction(id).description;
        }
        if (finalDesc == null) finalDesc = "";

        Prediction pred = new Prediction(id, finalDesc, confidence, worldTick,
                evidence == null ? "" : evidence);
        TargetExpectation updated = existing.withPrediction(pred);
        targets.put(targetId, updated);
        setDirty();
        return true;
    }

    /**
     * Adjust a prediction's confidence by a delta. Useful for inference:
     * "saw the player study formations → combat_able += 0.05".
     * Clamps to [0, CONFIDENCE_CAP].
     *
     * @return the new confidence after adjustment, or 0.0 if at capacity
     */
    public double adjustConfidence(String npcId, String targetId, String id,
                                   double delta, long worldTick, String evidence) {
        TargetExpectation te = getExpectation(npcId, targetId);
        double current = (te != null) ? te.confidenceOf(id) : 0.0;
        double updated = current + delta;
        if (updated < 0.0) updated = 0.0;
        if (updated > CONFIDENCE_CAP) updated = CONFIDENCE_CAP;
        boolean stored = updatePrediction(npcId, targetId, id, null, updated, worldTick, evidence);
        return stored ? updated : current;
    }

    /**
     * Run one decay cycle across all NPC-target pairs.
     * Removes predictions below PRUNE_THRESHOLD. Removes empty pairs.
     *
     * @param currentTick the current game tick
     * @return number of predictions pruned
     */
    public int decayTick(long currentTick) {
        int pruned = 0;
        Iterator<Map.Entry<String, ConcurrentHashMap<String, TargetExpectation>>> npcIt =
                network.entrySet().iterator();
        while (npcIt.hasNext()) {
            Map.Entry<String, ConcurrentHashMap<String, TargetExpectation>> npcEntry = npcIt.next();
            ConcurrentHashMap<String, TargetExpectation> targets = npcEntry.getValue();
            Iterator<Map.Entry<String, TargetExpectation>> targetIt = targets.entrySet().iterator();
            while (targetIt.hasNext()) {
                Map.Entry<String, TargetExpectation> targetEntry = targetIt.next();
                TargetExpectation te = targetEntry.getValue();
                int beforeSize = te.size();
                TargetExpectation decayed = te.withDecay(currentTick);
                int afterSize = decayed.size();
                pruned += (beforeSize - afterSize);
                if (afterSize == 0) {
                    targetIt.remove();
                } else {
                    targetEntry.setValue(decayed);
                }
            }
            if (targets.isEmpty()) {
                npcIt.remove();
            }
        }
        if (pruned > 0) setDirty();
        return pruned;
    }

    /**
     * Seed a canonical prediction for an NPC about a target.
     * Only creates the prediction if it doesn't already exist (doesn't
     * overwrite existing observations). Useful for initializing NPCs
     * with baseline expectations from their canon profiles.
     *
     * @return true if the prediction was seeded (didn't exist before)
     */
    public boolean seedPrediction(String npcId, String targetId, String id,
                                  String description, double initialConfidence,
                                  long worldTick, String evidence) {
        TargetExpectation te = getExpectation(npcId, targetId);
        if (te != null && te.getPrediction(id) != null) return false; // already exists
        return updatePrediction(npcId, targetId, id, description,
                initialConfidence, worldTick, evidence);
    }

    /**
     * Get or create a TargetExpectation for an NPC about a target.
     * Used by the tick handler for batch inference.
     */
    @Nullable
    public TargetExpectation getOrCreateExpectation(String npcId, String targetId) {
        if (totalPairs() >= MAX_PAIRS && getExpectation(npcId, targetId) == null) {
            return null; // at capacity
        }
        ConcurrentHashMap<String, TargetExpectation> targets =
                network.computeIfAbsent(npcId, k -> new ConcurrentHashMap<>());
        return targets.computeIfAbsent(targetId,
                tid -> new TargetExpectation(npcId, tid));
    }
}