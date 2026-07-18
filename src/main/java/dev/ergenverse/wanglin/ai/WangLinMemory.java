package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

/**
 * WangLinMemory — a single defining memory that drives Wang Lin's behaviour.
 *
 * <p>Each instance is one canon-attested event from Wang Lin's life, encoded
 * with an emotional weight (how positive or negative it was for him), the
 * behaviour it drives, and a {@link Provenance} citing the chapter(s) or
 * timeline event where the memory is attested.
 *
 * <h2>Why memories, not traits?</h2>
 * <p>Traits are patterns; memories are causes. Wang Lin's <i>caution</i> is a
 * trait — but the <i>Teng Clan massacre of his family</i> (timeline event E13)
 * is the memory that <b>caused</b> the caution. An AI that only knows "Wang
 * Lin is cautious" can imitate the pattern; an AI that knows the memory can
 * explain WHY and react appropriately when the player encounters something
 * reminiscent (e.g. entering a Zhao-Country-style village, or meeting a
 * cultivator who slaughtered an innocent family).
 *
 * <h2>Emotional weight scale</h2>
 * <ul>
 *   <li><b>+1.0</b> — supreme joy / fulfilment (e.g. Li Muwan's resurrection
 *       and Transcendence with him, E36).</li>
 *   <li><b>+0.5 to +0.8</b> — strong positive (inheritance received, sworn
 *       brotherhood with Situ Nan, daughter's birth).</li>
 *   <li><b>+0.1 to +0.4</b> — mild positive (sect promotion, technique
 *       mastered).</li>
 *   <li><b>0.0</b> — neutral (birth, childhood before cultivation).</li>
 *   <li><b>-0.3 to -0.6</b> — strong negative (loss, betrayal, defeat in
 *       battle).</li>
 *   <li><b>-0.8 to -1.0</b> — defining trauma (Teng Clan massacre E13,
 *       Li Muwan's death E28, Qi Xi Spell failure E88).</li>
 * </ul>
 * <p>The scale is a Simulation-layer design choice — Er Gen never wrote
 * numerical emotional weights. The SIGN and relative MAGNITUDE are derived
 * from canon (e.g. Li Muwan's death is unambiguously his lowest point; the
 * Transcendence with her is unambiguously his highest). The exact number is
 * design. This is recorded as INFERRED with confidence 3 in the
 * emotionalWeight's interpretation; the underlying MEMORY itself is canon.
 *
 * <h2>Canon sourcing</h2>
 * <p>The {@code provenance} field cites the novel and the specific chapter(s)
 * or timeline event(s). Where a memory spans a long arc (e.g. "the Restriction
 * Mountain trial" took 7 years), the most attested single chapter is cited
 * plus an ambiguity note explaining the span.
 *
 * @param memoryId            a stable identifier (e.g. {@code "E13_teng_massacre"})
 * @param description         a one-sentence summary of the event
 * @param emotionalWeight     -1.0 (defining trauma) to +1.0 (supreme fulfilment)
 * @param whatBehaviorItDrives the trait or habit this memory caused/reinforced
 *                            (free-text, references a trait/habit by name)
 * @param provenance          source novel, chapters/timeline events, attestation,
 *                            confidence, ambiguities
 */
public record WangLinMemory(
        String memoryId,
        String description,
        double emotionalWeight,
        String whatBehaviorItDrives,
        Provenance provenance
) {
    public WangLinMemory {
        if (memoryId == null || memoryId.isBlank()) {
            throw new IllegalArgumentException("WangLinMemory requires a memoryId");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("WangLinMemory '" + memoryId + "' requires a description");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("WangLinMemory '" + memoryId + "' requires a Provenance");
        }
        if (whatBehaviorItDrives == null) whatBehaviorItDrives = "";
        // Clamp emotional weight to [-1.0, +1.0]
        if (emotionalWeight < -1.0) emotionalWeight = -1.0;
        if (emotionalWeight > 1.0) emotionalWeight = 1.0;
    }

    /** Whether this memory is one of Wang Lin's defining traumas (weight ≤ -0.8). */
    public boolean isDefiningTrauma() {
        return emotionalWeight <= -0.8;
    }

    /** Whether this memory is one of Wang Lin's supreme fulfilments (weight ≥ +0.8). */
    public boolean isSupremeFulfilment() {
        return emotionalWeight >= 0.8;
    }

    /** A compact one-line summary for dialogue tooltips. */
    public String summary() {
        String sign = emotionalWeight >= 0 ? "+" : "";
        return String.format("[%s%.2f] %s — %s",
                sign, emotionalWeight, description, whatBehaviorItDrives);
    }
}
