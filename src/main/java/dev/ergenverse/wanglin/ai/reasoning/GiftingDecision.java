package dev.ergenverse.wanglin.ai.reasoning;

import net.minecraft.nbt.CompoundTag;

/**
 * GiftingDecision — Wang Lin's evaluation of ONE factor for ONE gift request.
 *
 * <p>A complete {@link GiftingResponse} contains six {@code GiftingDecision}s
 * — one per {@link GiftingFactor}, in canonical order — produced by the
 * {@link WangLinReasoningEngine}.
 *
 * <p>Each decision captures:
 * <ul>
 *   <li><b>factor</b> — which of the six factors was evaluated.</li>
 *   <li><b>score</b> — 0.0 (the factor strongly disfavors giving) to 1.0 (the
 *       factor strongly favors giving). The score is a Simulation-layer
 *       design choice; per §6.13.1, the underlying read of the player is
 *       uncertain, so the score should be interpreted as Wang Lin's
 *       confidence-weighted estimate.</li>
 *   <li><b>reasoning</b> — Wang Lin's internal monologue about this factor,
 *       in his voice (image-rich, terse per {@link dev.ergenverse.wanglin.ai
 *       .WangLinSpeechPatterns}). E.g. for SAFETY on a low-realm player
 *       requesting a high-realm treasure: "Player has Qi Condensation 3 —
 *       Restriction Jade Slip is safely below their threshold." This is the
 *       in-character narration the dialogue engine surfaces.</li>
 * </ul>
 *
 * <h2>Serialization</h2>
 * <p>Serializes to a {@link CompoundTag} under keys "factor", "score",
 * "reasoning". Round-trip stable via {@link #serializeNBT()} /
 * {@link #deserializeNBT(CompoundTag)}.
 *
 * <p>This class is immutable after construction. NBT deserialization produces
 * a fresh instance.
 */
public final class GiftingDecision {

    /** NBT version tag — bump if the schema changes. */
    private static final int NBT_VERSION = 1;

    public final GiftingFactor factor;
    /** 0.0 (disfavor) to 1.0 (favor). Clamped on construction. */
    public final double score;
    /** Wang Lin's internal monologue for this factor. Never null (may be empty). */
    public final String reasoning;

    public GiftingDecision(GiftingFactor factor, double score, String reasoning) {
        if (factor == null) {
            throw new IllegalArgumentException("GiftingDecision requires a non-null factor");
        }
        this.factor = factor;
        // Clamp score to [0.0, 1.0]
        if (score < 0.0) score = 0.0;
        if (score > 1.0) score = 1.0;
        this.score = score;
        this.reasoning = reasoning == null ? "" : reasoning;
    }

    /** Convenience accessor — was this factor strongly in favor (score ≥ 0.7)? */
    public boolean isStronglyFavoring() {
        return score >= 0.7;
    }

    /** Convenience accessor — was this factor strongly against (score ≤ 0.3)? */
    public boolean isStronglyAgainst() {
        return score <= 0.3;
    }

    /** Weighted score contribution to the aggregate (score × factor weight). */
    public double weightedContribution() {
        return score * factor.weight;
    }

    // ─── NBT Serialization ─────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", NBT_VERSION);
        tag.putString("factor", factor.name());
        tag.putDouble("score", score);
        tag.putString("reasoning", reasoning);
        return tag;
    }

    public static GiftingDecision deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            return new GiftingDecision(GiftingFactor.JUDGMENT, 0.0, "");
        }
        GiftingFactor factor;
        try {
            factor = GiftingFactor.valueOf(tag.getString("factor"));
        } catch (IllegalArgumentException ex) {
            // Unknown factor name (e.g. from a future version) — fall back to JUDGMENT.
            factor = GiftingFactor.JUDGMENT;
        }
        double score = tag.contains("score") ? tag.getDouble("score") : 0.0;
        String reasoning = tag.contains("reasoning") ? tag.getString("reasoning") : "";
        return new GiftingDecision(factor, score, reasoning);
    }

    @Override
    public String toString() {
        return String.format("GiftingDecision[%s=%.2f] %s", factor.displayName, score, reasoning);
    }
}
