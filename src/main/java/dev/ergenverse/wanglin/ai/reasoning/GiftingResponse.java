package dev.ergenverse.wanglin.ai.reasoning;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GiftingResponse — Wang Lin's complete response to a single gift request.
 *
 * <p>Contains the {@link GiftingOutcome}, the per-factor {@link GiftingDecision}s
 * (one per factor in canonical order — see {@link GiftingFactor#canonicalOrder()}),
 * the aggregated score, the outcome-specific payload (challenge description /
 * granted item ID), and Wang Lin's actual dialogue line.
 *
 * <p>Produced by {@link WangLinReasoningEngine#evaluateRequest} and consumed by
 * {@link ReasoningIntegrationBridge#processGiftRequest} to drive the
 * player-facing interaction.
 *
 * <h2>Field semantics by outcome</h2>
 * <ul>
 *   <li><b>YES</b> — {@code itemGrantedId} is the Forge registry ID of the
 *       granted item; {@code challengeDescription} is null.</li>
 *   <li><b>NO_FOR_NOW</b> — both {@code itemGrantedId} and
 *       {@code challengeDescription} are null. The dialogue line carries the
 *       "what to improve" hint.</li>
 *   <li><b>CHALLENGE</b> — {@code challengeDescription} is the canon-flavored
 *       challenge text (e.g. "Survive three days in Mosquito Valley without
 *       any cultivation."); {@code itemGrantedId} is null (granted only on
 *       challenge completion by another system).</li>
 * </ul>
 *
 * <h2>Serialization</h2>
 * <p>Round-trip stable via {@link #serializeNBT()} /
 * {@link #deserializeNBT(CompoundTag)}. The decisions list preserves canonical
 * order across serialization.
 */
public final class GiftingResponse {

    private static final int NBT_VERSION = 1;

    public final GiftingOutcome outcome;
    /** One decision per factor, in {@link GiftingFactor#canonicalOrder()}. Never null/empty. */
    public final List<GiftingDecision> decisions;
    /** Weighted aggregate score across all six factors. Range [0.0, 1.0]. */
    public final double aggregateScore;
    /** Non-null iff outcome == CHALLENGE. The thematically-tied challenge text. */
    public final String challengeDescription;
    /** Non-null iff outcome == YES. The Forge registry ID of the granted item. */
    public final String itemGrantedId;
    /** The actual line Wang Lin says to the player (canon-flavored, terse). */
    public final String wangLinDialogue;

    public GiftingResponse(GiftingOutcome outcome,
                           List<GiftingDecision> decisions,
                           double aggregateScore,
                           String challengeDescription,
                           String itemGrantedId,
                           String wangLinDialogue) {
        if (outcome == null) {
            throw new IllegalArgumentException("GiftingResponse requires a non-null outcome");
        }
        if (decisions == null || decisions.isEmpty()) {
            throw new IllegalArgumentException("GiftingResponse requires at least one decision");
        }
        this.outcome = outcome;
        this.decisions = Collections.unmodifiableList(new ArrayList<>(decisions));
        // Clamp aggregate score
        if (aggregateScore < 0.0) aggregateScore = 0.0;
        if (aggregateScore > 1.0) aggregateScore = 1.0;
        this.aggregateScore = aggregateScore;
        // Per-outcome invariants
        if (outcome != GiftingOutcome.CHALLENGE) {
            challengeDescription = null;
        }
        if (outcome != GiftingOutcome.YES) {
            itemGrantedId = null;
        }
        this.challengeDescription = challengeDescription;
        this.itemGrantedId = itemGrantedId;
        this.wangLinDialogue = wangLinDialogue == null ? "" : wangLinDialogue;
    }

    /** Convenience: the dominant factor (highest deviation from 0.5 in either direction). */
    public GiftingDecision dominantDecision() {
        GiftingDecision dominant = decisions.get(0);
        double maxDeviation = Math.abs(dominant.score - 0.5);
        for (GiftingDecision d : decisions) {
            double dev = Math.abs(d.score - 0.5);
            if (dev > maxDeviation) {
                maxDeviation = dev;
                dominant = d;
            }
        }
        return dominant;
    }

    /** Convenience: the factor whose score most drove the outcome (used for dialogue flavor). */
    public GiftingFactor dominantFactor() {
        return dominantDecision().factor;
    }

    // ─── NBT Serialization ─────────────────────────────────────────────

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("version", NBT_VERSION);
        tag.putString("outcome", outcome.name());
        tag.putDouble("aggregateScore", aggregateScore);
        if (challengeDescription != null) {
            tag.putString("challengeDescription", challengeDescription);
        }
        if (itemGrantedId != null) {
            tag.putString("itemGrantedId", itemGrantedId);
        }
        tag.putString("wangLinDialogue", wangLinDialogue);
        ListTag list = new ListTag();
        for (GiftingDecision d : decisions) {
            list.add(d.serializeNBT());
        }
        tag.put("decisions", list);
        return tag;
    }

    public static GiftingResponse deserializeNBT(CompoundTag tag) {
        if (tag == null) {
            // Safe default: refusal with no decisions — but the constructor rejects empty,
            // so fabricate a single placeholder JUDGMENT decision.
            return new GiftingResponse(
                    GiftingOutcome.NO_FOR_NOW,
                    List.of(new GiftingDecision(GiftingFactor.JUDGMENT, 0.0, "")),
                    0.0, null, null, "");
        }
        GiftingOutcome outcome;
        try {
            outcome = GiftingOutcome.valueOf(tag.getString("outcome"));
        } catch (IllegalArgumentException ex) {
            outcome = GiftingOutcome.NO_FOR_NOW;
        }
        double aggregateScore = tag.contains("aggregateScore") ? tag.getDouble("aggregateScore") : 0.0;
        String challengeDescription = tag.contains("challengeDescription") ? tag.getString("challengeDescription") : null;
        String itemGrantedId = tag.contains("itemGrantedId") ? tag.getString("itemGrantedId") : null;
        String wangLinDialogue = tag.contains("wangLinDialogue") ? tag.getString("wangLinDialogue") : "";

        List<GiftingDecision> decisions = new ArrayList<>();
        if (tag.contains("decisions", Tag.TAG_LIST)) {
            ListTag list = tag.getList("decisions", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                decisions.add(GiftingDecision.deserializeNBT(list.getCompound(i)));
            }
        }
        if (decisions.isEmpty()) {
            decisions.add(new GiftingDecision(GiftingFactor.JUDGMENT, 0.0, ""));
        }
        return new GiftingResponse(outcome, decisions, aggregateScore,
                challengeDescription, itemGrantedId, wangLinDialogue);
    }

    @Override
    public String toString() {
        return String.format("GiftingResponse[outcome=%s, agg=%.2f, dominant=%s]",
                outcome.name(), aggregateScore, dominantFactor().displayName);
    }
}
