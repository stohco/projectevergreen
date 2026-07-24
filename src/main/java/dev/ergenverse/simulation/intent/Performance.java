package dev.ergenverse.simulation.intent;

/**
 * Performance — the Acting Layer.
 *
 * <h2>What this is (and is NOT)</h2>
 *
 * <p>This is the concrete realization of the user's 2026-07-26 design review:
 *
 * <blockquote>
 * The next evolution shouldn't be "better AI". It should be better acting.
 * Those aren't the same thing. Right now you're still treating animation as
 * "represent the current state." Instead think: "communicate the current
 * thought." Those are very different goals.
 *
 * Instead of [Commitment → POSE_OBSERVING], do
 *   Commitment → Internal State → Performance → Animation
 *
 * Performance might include: focus intensity, patience, confidence, urgency,
 * tension, fatigue, concealment. These aren't new AI. They're acting directions.
 * </blockquote>
 *
 * <p>A {@code Performance} is a bundle of seven acting-direction channels, each
 * a scalar in {@code [0,1]}. It is NOT cognition — it adds no intelligence. It
 * is the <b>translation layer</b> between a Commitment (what the NPC decided)
 * and the renderer (how the NPC's body communicates that decision to the
 * player). The same Commitment can produce different Performances depending on
 * context: observing wolves is calm vigilance; observing an unknown cultivator
 * is tense concealment. Same IntentNature. Different acting.
 *
 * <h2>The seven channels</h2>
 *
 * <p>Each channel is an independent acting direction the renderer consumes
 * separately. The user's directive: "instead of thinking in poses, think in
 * independent channels — Head, Torso, Shoulders, Hands, Feet, Eyes, Breathing,
 * Attention, Weight. Each channel updates independently. You suddenly get
 * hundreds of combinations instead of five fixed poses."
 *
 * <ul>
 *   <li><b>focus</b> (0–1) — how concentrated the NPC's attention is. High focus
 *       → slower, more deliberate head turns; smaller saccade amplitude; rarer
 *       glance-aways; near-motionless body. The wolf-observer is at 0.95.</li>
 *   <li><b>urgency</b> (0–1) — how time-pressured the NPC feels. High urgency →
 *       faster breathing; quicker head turns; more frequent surrounding checks;
 *       forward lean. The wolf-observer is at 0.15; the unknown-cultivator
 *       observer is at 0.85.</li>
 *   <li><b>confidence</b> (0–1) — how much the NPC trusts its read of the
 *       situation. High confidence → stable, settled body; smooth tracking.
 *       Low confidence → hesitant micro-movements; tension in the torso.</li>
 *   <li><b>concealment</b> (0–1) — how important it is to stay hidden. High
 *       concealment → minimal silhouette change; hand drifting nearer weapon;
 *       weight shifted back (ready to retreat); suppressed breathing amplitude.</li>
 *   <li><b>tension</b> (0–1) — physical readiness / coiled-spring state. High
 *       tension → torso rigidity; shoulders raised; breathing shallow and fast.</li>
 *   <li><b>patience</b> (0–1) — how long the NPC will hold still before
 *       shifting. High patience → planted feet; long holds between glances.
 *       Low patience → frequent weight shifts; fidget.</li>
 *   <li><b>fatigue</b> (0–1) — accumulated tiredness. High fatigue → slower
 *       movements; drooped posture; longer blinks (rendered as longer
 *       glance-downs). Grows slowly over a long commitment.</li>
 * </ul>
 *
 * <h2>Why channels, not poses</h2>
 *
 * <p>The user's argument: five fixed poses (IDLE, MEDITATING, CASTING,
 * OBSERVING, GUARDING, PURSUING, SOCIALIZING) cap the expressible states at
 * seven. But an NPC observing wolves (calm, high-confidence, low-urgency) and
 * an NPC observing an unknown cultivator (tense, low-confidence, high-urgency)
 * are BOTH in POSE_OBSERVING — yet they should look completely different.
 * Channels decouple the dimensions: the renderer combines seven independent
 * inputs into hundreds of emergent silhouettes. The pose system remains as a
 * coarse fallback (for activity-locked materializer poses), but the
 * Performance layer is what makes the NPC <i>act</i>.
 *
 * <p>Minecraft's low-poly models make tiny motion differences highly
 * noticeable. A five-degree head tilt is huge. A slight pause before turning
 * is huge. The channels exploit this: instead of demanding AAA animation
 * curves, they modulate timing and micro-motion — exactly the substrate where
 * low-poly models read best.
 *
 * <h2>Provenance</h2>
 *
 * <p><b>USER-DIRECTED.</b> The 2026-07-26 review explicitly named this layer
 * and its channels. The interpreter table below is INFERRED from the user's
 * two canonical examples (wolves vs unknown cultivator) — it will need
 * playtest tuning per the "First Convincing Observation" milestone.
 *
 * <p>This class adds NO cognition. It is pure translation. The Commitment
 * already decided what to do; the Performance decides how to show it.
 */
public final class Performance {

    // ── The seven acting-direction channels (each [0,1]) ──────────────

    /** Concentration of attention. High = deliberate, slow, near-motionless. */
    public final float focus;
    /** Time pressure felt. High = faster breathing, quicker turns, forward lean. */
    public final float urgency;
    /** Trust in one's read of the situation. High = stable, settled. Low = hesitant. */
    public final float confidence;
    /** Importance of staying hidden. High = minimal silhouette, hand near weapon, weight back. */
    public final float concealment;
    /** Physical readiness / coiled-spring state. High = rigid torso, raised shoulders. */
    public final float tension;
    /** Willingness to hold still. High = planted feet, long holds. Low = fidget. */
    public final float patience;
    /** Accumulated tiredness. High = slower movement, drooped posture. */
    public final float fatigue;

    /** Sentinel "no performance" — all channels NaN. Used when no Commitment is active. */
    public static final Performance NONE = new Performance(
            Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);

    public Performance(float focus, float urgency, float confidence,
                       float concealment, float tension, float patience, float fatigue) {
        this.focus = clamp01(focus);
        this.urgency = clamp01(urgency);
        this.confidence = clamp01(confidence);
        this.concealment = clamp01(concealment);
        this.tension = clamp01(tension);
        this.patience = clamp01(patience);
        this.fatigue = clamp01(fatigue);
    }

    /** True if this Performance is the NONE sentinel (no active commitment). */
    public boolean isNone() {
        return Float.isNaN(focus);
    }

    private static float clamp01(float v) {
        if (Float.isNaN(v)) return v; // preserve NaN for NONE sentinel
        return Math.max(0.0f, Math.min(1.0f, v));
    }

    @Override
    public String toString() {
        if (isNone()) return "Performance[NONE]";
        return String.format(
                "Performance[focus=%.2f urgency=%.2f conf=%.2f conceal=%.2f tension=%.2f patience=%.2f fatigue=%.2f]",
                focus, urgency, confidence, concealment, tension, patience, fatigue);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Interpreter — Commitment → Performance
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Interpret a Commitment into a Performance.
     *
     * <p>This is the Acting Layer's entry point. The {@link CognitionDrivenGoal}
     * calls this when a Commitment becomes active, then syncs the resulting
     * channels to the client via SynchedEntityData. The renderer reads the
     * channels and drives the body independently per channel.
     *
     * <h3>The user's two canonical examples (verification targets)</h3>
     * <pre>
     *   Observe wolves:
     *     focus=0.95 urgency=0.15 confidence=0.92 concealment=0.85
     *     → almost motionless, very slow breathing, tiny eye movements,
     *       rare glances away, feet planted, head tracks smoothly
     *
     *   Observe unknown cultivator:
     *     focus=0.95 urgency=0.85 confidence=0.35 concealment=0.95
     *     → quicker head turns, more frequent checking surroundings,
     *       slight torso tension, subtle backward weight shift,
     *       hand nearer weapon
     * </pre>
     *
     * <p>Same IntentNature (OBSERVE_FROM_DISTANCE). Different Performance.
     * That difference is the entire point of the Acting Layer.
     *
     * <h3>How context modulates the base table</h3>
     *
     * <p>Each IntentNature has a base Performance (the {@link #baseTable}).
     * Three context signals modulate it:
     * <ol>
     *   <li><b>targetId keywords</b> — "wolf/beast/pack/animal" lowers urgency
     *       and raises confidence (animals follow patterns); "cultivator/
     *       stranger/unknown/human" raises urgency and lowers confidence
     *       (humans are unpredictable).</li>
     *   <li><b>threatIntensity</b> (0–1, from perception/situation) — raises
     *       urgency, tension, and lowers confidence.</li>
     *   <li><b>concealmentPressure</b> (0–1, from canon/motivations) — raises
     *       concealment and tension.</li>
     * </ol>
     *
     * @param nature the active Commitment's IntentNature
     * @param targetId the Commitment's target (e.g. "wolf_pack_west_ridge")
     * @param threatIntensity 0–1 scalar of how dangerous the current situation
     *        feels (pass 0 if unknown — defaults to neutral)
     * @param concealmentPressure 0–1 scalar of how important staying hidden is
     *        (pass 0 if unknown)
     * @return a Performance; never null
     */
    public static Performance interpret(IntentNature nature, String targetId,
                                        float threatIntensity, float concealmentPressure) {
        if (nature == null) {
            return new Performance(0.4f, 0.3f, 0.5f, 0.3f, 0.2f, 0.5f, 0.1f);
        }

        float[] base = baseTable(nature);
        float focus = base[0];
        float urgency = base[1];
        float confidence = base[2];
        float concealment = base[3];
        float tension = base[4];
        float patience = base[5];
        float fatigue = base[6];

        // ── Context modulation 1: target type ──
        // The user's examples hinge on this. Wolves are predictable (lower
        // urgency, higher confidence). Unknown humans are unpredictable
        // (higher urgency, lower confidence, higher concealment).
        TargetKind kind = classifyTarget(targetId);
        switch (kind) {
            case ANIMAL -> {
                urgency -= 0.10f;
                confidence += 0.20f;
            }
            case UNKNOWN_HUMAN -> {
                urgency += 0.50f;
                confidence -= 0.35f;
                concealment += 0.15f;
                tension += 0.10f;
            }
            case RESOURCE -> {
                urgency -= 0.05f;
                patience += 0.10f;
            }
            case NONE, OTHER -> { /* no modulation */ }
        }

        // ── Context modulation 2: threat intensity ──
        // A situation that feels dangerous raises urgency/tension and erodes
        // confidence. Pass 0 to skip (neutral).
        urgency += threatIntensity * 0.20f;
        tension += threatIntensity * 0.30f;
        confidence -= threatIntensity * 0.15f;

        // ── Context modulation 3: concealment pressure ──
        // If the NPC's motivations/canon demand staying hidden, raise the
        // concealment channel and add a touch of tension.
        concealment += concealmentPressure * 0.25f;
        tension += concealmentPressure * 0.10f;

        return new Performance(focus, urgency, confidence, concealment,
                tension, patience, fatigue);
    }

    /**
     * The base Performance table per IntentNature.
     *
     * <p>These are the "neutral context" values — before target-type and
     * threat modulation. They are programmer guesses inferred from the user's
     * two examples and from the canonical Wang Lin behavioral profile
     * (cautious, observant, concealment-first). Playtest tuning expected.
     *
     * <p>Order: [focus, urgency, confidence, concealment, tension, patience, fatigue]
     */
    private static float[] baseTable(IntentNature nature) {
        return switch (nature) {
            // ── Observation family: high focus, moderate concealment ──
            // The wolf-observer is the calibration target: focus≈0.95,
            // urgency≈0.15-0.25, confidence≈0.7-0.9, concealment≈0.7-0.85.
            case OBSERVE_FROM_DISTANCE -> new float[]{0.95f, 0.20f, 0.72f, 0.70f, 0.30f, 0.80f, 0.10f};
            case GATHER_INTEL          -> new float[]{0.88f, 0.30f, 0.60f, 0.80f, 0.40f, 0.70f, 0.15f};
            case EXPLORE_CAUTIOUSLY    -> new float[]{0.72f, 0.40f, 0.50f, 0.60f, 0.40f, 0.60f, 0.20f};
            case AVOID_REVEALING_STRENGTH -> new float[]{0.60f, 0.50f, 0.60f, 0.95f, 0.50f, 0.50f, 0.20f};

            // ── Defensive family: high urgency, high tension, low concealment ──
            case PROTECT_ASSET      -> new float[]{0.85f, 0.70f, 0.70f, 0.20f, 0.70f, 0.40f, 0.20f};
            case DEFEND_POSITION    -> new float[]{0.90f, 0.80f, 0.70f, 0.10f, 0.80f, 0.30f, 0.25f};
            case ESTABLISH_DOMINANCE-> new float[]{0.80f, 0.60f, 0.85f, 0.00f, 0.60f, 0.30f, 0.20f};
            case AMBUSH             -> new float[]{0.95f, 0.50f, 0.70f, 1.00f, 0.70f, 0.90f, 0.30f};
            case DECEIVE            -> new float[]{0.80f, 0.50f, 0.60f, 0.90f, 0.50f, 0.60f, 0.20f};
            case PROVOKE            -> new float[]{0.85f, 0.70f, 0.70f, 0.30f, 0.60f, 0.30f, 0.20f};

            // ── Movement family: moderate focus, forward lean ──
            case SEEK_OPPORTUNITY          -> new float[]{0.75f, 0.50f, 0.60f, 0.50f, 0.40f, 0.60f, 0.25f};
            case ADVANCE_OPPORTUNISTICALLY -> new float[]{0.80f, 0.60f, 0.65f, 0.40f, 0.50f, 0.50f, 0.30f};
            case RETREAT_TACTICALLY        -> new float[]{0.85f, 0.75f, 0.70f, 0.60f, 0.60f, 0.40f, 0.35f};

            // ── Social family: relaxed, low tension ──
            case NEGOTIATE      -> new float[]{0.80f, 0.40f, 0.65f, 0.30f, 0.30f, 0.70f, 0.15f};
            case TEST_JUDGMENT  -> new float[]{0.85f, 0.30f, 0.75f, 0.50f, 0.35f, 0.80f, 0.15f};
            case MAINTAIN_COVER -> new float[]{0.70f, 0.30f, 0.70f, 0.85f, 0.40f, 0.70f, 0.20f};

            // ── Cultivation: maximum focus, minimum urgency, high patience ──
            case CULTIVATE_SECRETLY -> new float[]{0.95f, 0.10f, 0.80f, 0.90f, 0.20f, 0.95f, 0.40f};
        };
    }

    /**
     * Classify a targetId string into a kind that drives Performance modulation.
     *
     * <p>This is keyword-based and deliberately coarse. The point is not
     * precision but capturing the user's distinction: "wolves" vs "unknown
     * cultivator" produce different acting from the same IntentNature.
     */
    private static TargetKind classifyTarget(String targetId) {
        if (targetId == null || targetId.isEmpty()) return TargetKind.NONE;
        String t = targetId.toLowerCase();
        // Animal / beast targets — predictable, patterned behavior
        if (t.contains("wolf") || t.contains("pack") || t.contains("beast")
                || t.contains("animal") || t.contains("spirit_beast")
                || t.contains("hawk") || t.contains("tiger") || t.contains("snake")) {
            return TargetKind.ANIMAL;
        }
        // Unknown / unpredictable human targets
        if (t.contains("cultivator") || t.contains("stranger") || t.contains("unknown")
                || t.contains("human") || t.contains("intruder") || t.contains("enemy")) {
            return TargetKind.UNKNOWN_HUMAN;
        }
        // Resource / opportunity targets — gatherable, non-threatening
        if (t.contains("herb") || t.contains("resource") || t.contains("opportunity")
                || t.contains("spirit_stone") || t.contains("mine") || t.contains("glade")) {
            return TargetKind.RESOURCE;
        }
        return TargetKind.OTHER;
    }

    private enum TargetKind { NONE, ANIMAL, UNKNOWN_HUMAN, RESOURCE, OTHER }
}
