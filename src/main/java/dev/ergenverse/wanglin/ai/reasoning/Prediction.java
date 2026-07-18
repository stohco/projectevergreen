package dev.ergenverse.wanglin.ai.reasoning;

import net.minecraft.nbt.CompoundTag;

/**
 * Prediction — a single uncertain prediction Wang Lin holds about the player,
 * per PROJECT_MASTER.md §6.13.1.
 *
 * <p>Predictions are NOT certainties. Every prediction carries a confidence
 * percentage (0.0–1.0). Wang Lin's internal monologue might read:
 * <pre>
 *   Pursuing Restriction Dao                     82%
 *   Will challenge a stronger cultivator soon    61%
 *   Can survive Mosquito Valley                   34%
 *   Likely to break through within 30 days       47%
 * </pre>
 *
 * <p>Per §6.13.1, this matters because it lets NPCs make MISTAKES:
 * <ul>
 *   <li>Wang Lin might predict (82%) the player is pursuing Restriction Dao
 *       and leave a jade slip — but the player was actually researching
 *       formations to find a specific seal. The jade slip is a wasted
 *       gesture; Wang Lin updates his model.</li>
 *   <li>Wang Lin might predict (34%) the player can survive Mosquito Valley
 *       and offer it as a challenge — but the player dies. Wang Lin's
 *       prediction was wrong. He grieves (in his own way) and
 *       recalibrates.</li>
 * </ul>
 *
 * <p><b>Confidence decays with time</b> (see {@link ExpectationModel#decayPredictions}):
 * 1% per 24000 ticks (1 in-game day). Old predictions fade; new evidence
 * shifts the percentages. This makes the Expectation Model feel like a
 * living, learning mind rather than a static rules table.
 *
 * <h2>Hoarding correction</h2>
 * <p>The "hoarding_path" prediction is a SPECIAL prediction: it informs WHAT
 * the player needs most (a hoarding player may need a breakthrough catalyst
 * more than another treasure), but it NEVER triggers punitive withholding.
 * The JUDGMENT factor in {@link WangLinReasoningEngine} reads this prediction
 * to find the player's true need, not to lower the score.
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li>{@code id} — stable identifier (e.g. "pursuing_restriction_dao",
 *       "will_challenge_stronger_cultivator", "can_survive_mosquito_valley",
 *       "hoarding_path").</li>
 *   <li>{@code description} — human-readable statement of the prediction.</li>
 *   <li>{@code confidence} — 0.0 to 1.0. Clamped on construction.</li>
 *   <li>{@code lastUpdatedTick} — the world tick this prediction was last
 *       updated (for decay calculation).</li>
 *   <li>{@code evidenceSummary} — what observations led to this prediction
 *       (e.g. "Player right-clicked 12 restriction blocks in 6 in-game
 *       hours").</li>
 * </ul>
 */
public final class Prediction {

    private static final int NBT_VERSION = 1;

    public final String id;
    public final String description;
    public final double confidence;
    public final long lastUpdatedTick;
    public final String evidenceSummary;

    public Prediction(String id, String description, double confidence,
                      long lastUpdatedTick, String evidenceSummary) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Prediction requires a non-blank id");
        }
        this.id = id;
        this.description = description == null ? "" : description;
        // Clamp confidence to [0.0, 1.0]
        if (confidence < 0.0) confidence = 0.0;
        if (confidence > 1.0) confidence = 1.0;
        this.confidence = confidence;
        this.lastUpdatedTick = lastUpdatedTick;
        this.evidenceSummary = evidenceSummary == null ? "" : evidenceSummary;
    }

    /** Confidence as a percentage string (e.g. "82%") — for debug/UI display. */
    public String confidencePercent() {
        return Math.round(confidence * 100) + "%";
    }

    /**
     * Return a copy of this prediction with the confidence decayed by the
     * given amount. Does not modify this instance.
     *
     * @param decayAmount the amount to subtract from confidence (clamped at 0).
     * @return a new Prediction with reduced confidence.
     */
    public Prediction withDecayedConfidence(double decayAmount) {
        if (decayAmount <= 0.0) return this;
        double newConf = this.confidence - decayAmount;
        if (newConf < 0.0) newConf = 0.0;
        return new Prediction(this.id, this.description, newConf,
                this.lastUpdatedTick, this.evidenceSummary);
    }

    /**
     * Return a copy of this prediction with updated confidence, tick, and
     * evidence. Used by {@link ExpectationModel#updatePrediction}.
     */
    public Prediction withUpdate(double newConfidence, long newTick, String newEvidence) {
        return new Prediction(this.id, this.description, newConfidence, newTick, newEvidence);
    }

    // ─── NBT Serialization ─────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", NBT_VERSION);
        tag.putString("id", id);
        tag.putString("description", description);
        tag.putDouble("confidence", confidence);
        tag.putLong("lastUpdatedTick", lastUpdatedTick);
        tag.putString("evidenceSummary", evidenceSummary);
        return tag;
    }

    public static Prediction deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            return new Prediction("unknown", "", 0.0, 0L, "");
        }
        String id = tag.contains("id") ? tag.getString("id") : "unknown";
        String description = tag.contains("description") ? tag.getString("description") : "";
        double confidence = tag.contains("confidence") ? tag.getDouble("confidence") : 0.0;
        long lastUpdatedTick = tag.contains("lastUpdatedTick") ? tag.getLong("lastUpdatedTick") : 0L;
        String evidenceSummary = tag.contains("evidenceSummary") ? tag.getString("evidenceSummary") : "";
        return new Prediction(id, description, confidence, lastUpdatedTick, evidenceSummary);
    }

    @Override
    public String toString() {
        return String.format("Prediction[%s %.0f%%] %s", id, confidence * 100, description);
    }
}
