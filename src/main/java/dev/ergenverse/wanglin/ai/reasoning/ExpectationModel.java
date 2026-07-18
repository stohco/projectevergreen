package dev.ergenverse.wanglin.ai.reasoning;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ExpectationModel — Wang Lin's living model of the player, per
 * PROJECT_MASTER.md §6.13 and §6.13.1.
 *
 * <p>Every important NPC doesn't just react — they PREDICT the player and act
 * on predictions. For Wang Lin specifically:
 *
 * <blockquote>
 *   "Wang Lin acts as a mentor who reads the player's behavior and responds
 *   with judgment, not a vending machine that dispenses items at affinity
 *   thresholds." — §6.13
 * </blockquote>
 *
 * <p>The model holds a map of {@link Prediction}s keyed by prediction ID.
 * Each prediction has a confidence percentage that DECAYS over time (1% per
 * 24000 ticks = 1 in-game day) and UPDATES with new observations. Old
 * predictions fade; new evidence shifts the percentages. This makes the
 * Expectation Model feel like a living, learning mind.
 *
 * <h2>Canonical prediction IDs</h2>
 * <ul>
 *   <li><b>pursuing_restriction_dao</b> — player studies restrictions
 *       (right-clicks formation/restriction blocks). Updated by
 *       {@link ExpectationModelObserver}.</li>
 *   <li><b>will_challenge_stronger_cultivator</b> — player engages entities
 *       of higher cultivation realm. Updated on LivingHurtEvent.</li>
 *   <li><b>can_survive_X</b> — player can survive a specific challenge
 *       (e.g. "can_survive_mosquito_valley"). Reduced on respawn events.</li>
 *   <li><b>hoarding_path</b> — player hoards resources (inventory &gt; 80%
 *       full of ingots/gems). <b>HOARDING CORRECTION:</b> this prediction
 *       NEVER triggers punitive withholding. It INFORMS what the player
 *       needs most (a hoarding player may need a breakthrough catalyst more
 *       than another treasure). See {@link WangLinReasoningEngine}'s JUDGMENT
 *       factor.</li>
 * </ul>
 *
 * <h2>Persistence</h2>
 * <p>The model is per-player. {@link ExpectationModelObserver} stores and
 * loads it via the player's persistent NBT (under the key
 * {@value #PERSISTENT_NBT_KEY}). Round-trip stable through save/load.
 *
 * <h2>Thread-safety</h2>
 * <p>This class is NOT thread-safe. It is intended for server-side use only,
 * accessed from the server thread (Forge event handlers).
 */
public final class ExpectationModel {

    private static final int NBT_VERSION = 1;

    /** The player-persistent-NBT key under which the serialized model is stored. */
    public static final String PERSISTENT_NBT_KEY = "ergenverse_expectation_model";

    /**
     * Decay rate: 1% confidence reduction per 24000 ticks (1 in-game day).
     * Per §6.13.1: "Confidence decays with time and updates with new
     * observations. Old predictions fade."
     */
    public static final long DECAY_INTERVAL_TICKS = 24000L;
    public static final double DECAY_PER_INTERVAL = 0.01; // 1%

    /** Maximum confidence a prediction can reach via observation (never 100% — §6.13.1 uncertainty). */
    public static final double CONFIDENCE_CAP = 0.95;

    /** Predictions below this confidence are pruned on decay (treat as faded). */
    public static final double PRUNE_THRESHOLD = 0.02;

    private final Map<String, Prediction> activePredictions = new LinkedHashMap<>();
    private final Map<String, Long> predictionLastUpdatedTick = new LinkedHashMap<>();

    /** Construct an empty ExpectationModel. */
    public ExpectationModel() {}

    // ─── Public API ────────────────────────────────────────────────────

    /**
     * Create or update a prediction. If the prediction already exists, its
     * confidence, evidence, and tick are updated; the description is replaced
     * only if the new description is non-blank (otherwise the existing one
     * is preserved).
     *
     * @param id          the prediction ID
     * @param description human-readable statement (replaces existing if non-blank)
     * @param confidence  the new confidence value (clamped to [0, CONFIDENCE_CAP])
     * @param worldTick   the current world tick
     * @param evidence    what observations led to this update
     */
    public void updatePrediction(String id, String description, double confidence,
                                  long worldTick, String evidence) {
        if (id == null || id.isBlank()) return;
        // Clamp to [0, CONFIDENCE_CAP] — predictions are never 100% certain.
        if (confidence < 0.0) confidence = 0.0;
        if (confidence > CONFIDENCE_CAP) confidence = CONFIDENCE_CAP;

        Prediction existing = activePredictions.get(id);
        String finalDescription = (description == null || description.isBlank()) && existing != null
                ? existing.description
                : (description == null ? "" : description);
        String finalEvidence = evidence == null ? "" : evidence;

        Prediction updated = new Prediction(id, finalDescription, confidence, worldTick, finalEvidence);
        activePredictions.put(id, updated);
        predictionLastUpdatedTick.put(id, worldTick);
    }

    /**
     * Apply time-based decay to all predictions. Per §6.13.1: confidence
     * reduces by 1% per 24000 ticks (1 in-game day) since the last update.
     * Predictions below {@link #PRUNE_THRESHOLD} are pruned (treated as
     * fully faded).
     *
     * @param currentTick the current world tick
     */
    public void decayPredictions(long currentTick) {
        List<String> toPrune = new ArrayList<>();
        for (Map.Entry<String, Prediction> entry : activePredictions.entrySet()) {
            Prediction p = entry.getValue();
            long deltaTicks = currentTick - p.lastUpdatedTick;
            if (deltaTicks <= 0) continue;
            long intervals = deltaTicks / DECAY_INTERVAL_TICKS;
            if (intervals <= 0) continue;
            double decay = intervals * DECAY_PER_INTERVAL;
            Prediction decayed = p.withDecayedConfidence(decay);
            if (decayed.confidence <= PRUNE_THRESHOLD) {
                toPrune.add(entry.getKey());
            } else {
                activePredictions.put(entry.getKey(), decayed);
            }
        }
        for (String key : toPrune) {
            activePredictions.remove(key);
            predictionLastUpdatedTick.remove(key);
        }
    }

    /** Get a prediction by ID (or null if absent). */
    public Prediction getPrediction(String id) {
        if (id == null) return null;
        return activePredictions.get(id);
    }

    /** Convenience: confidence of a prediction, or 0.0 if absent. */
    public double confidenceOf(String id) {
        Prediction p = getPrediction(id);
        return p == null ? 0.0 : p.confidence;
    }

    /** Whether a prediction with the given ID exists at confidence ≥ 0.3. */
    public boolean hasStrongPrediction(String id) {
        return confidenceOf(id) >= 0.3;
    }

    /**
     * Get the top N predictions by confidence, descending.
     *
     * @param n the maximum number of predictions to return
     * @return an unmodifiable list of predictions, highest confidence first.
     */
    public List<Prediction> getTopPredictions(int n) {
        if (n <= 0) return List.of();
        return activePredictions.values().stream()
                .sorted((a, b) -> Double.compare(b.confidence, a.confidence))
                .limit(n)
                .collect(Collectors.toUnmodifiableList());
    }

    /** Immutable view of all active predictions (not sorted). */
    public List<Prediction> allPredictions() {
        return List.copyOf(activePredictions.values());
    }

    /** Number of active predictions. */
    public int size() {
        return activePredictions.size();
    }

    /** Whether the model is empty. */
    public boolean isEmpty() {
        return activePredictions.isEmpty();
    }

    // ─── NBT Serialization ─────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", NBT_VERSION);
        CompoundTag predictions = new CompoundTag();
        for (Prediction p : activePredictions.values()) {
            predictions.put(p.id, p.serializeNBT());
        }
        tag.put("predictions", predictions);
        return tag;
    }

    public static ExpectationModel deserializeNBT(CompoundTag tag) {
        ExpectationModel model = new ExpectationModel();
        if (tag == null) return model;
        if (tag.contains("predictions")) {
            CompoundTag predictions = tag.getCompound("predictions");
            for (String key : predictions.getAllKeys()) {
                Prediction p = Prediction.deserializeNBT(predictions.getCompound(key));
                model.activePredictions.put(p.id, p);
                model.predictionLastUpdatedTick.put(p.id, p.lastUpdatedTick);
            }
        }
        return model;
    }

    /** Convenience: return an unmodifiable view of the underlying map (for debug/inspection). */
    public Map<String, Prediction> snapshotMap() {
        return Collections.unmodifiableMap(activePredictions);
    }

    @Override
    public String toString() {
        if (activePredictions.isEmpty()) return "ExpectationModel{empty}";
        StringBuilder sb = new StringBuilder("ExpectationModel{\n");
        for (Prediction p : getTopPredictions(5)) {
            sb.append("  ").append(p).append("\n");
        }
        sb.append("} (").append(activePredictions.size()).append(" total)");
        return sb.toString();
    }
}
