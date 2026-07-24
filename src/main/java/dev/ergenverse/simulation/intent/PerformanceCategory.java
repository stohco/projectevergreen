package dev.ergenverse.simulation.intent;

/**
 * PerformanceCategory — the five hierarchies that govern the seven channels.
 *
 * <p>This is the user's 2026-07-26 structural directive:
 *
 * <blockquote>
 * I wouldn't let them stay hardcoded forever. Instead I'd classify them.
 * For example: Attention, Emotion, Energy, Confidence, Control. Then derive
 * individual channels from those. […] Otherwise you'll eventually have thirty
 * unrelated sliders. Hierarchies scale better.
 * </blockquote>
 *
 * <p>The seven Performance channels ({@link Performance}) are leaves; the five
 * categories are the branches. A category is a <b>dimension of inner state</b>,
 * and the channels that hang off it are different bodily manifestations of
 * that one dimension. This lets the interpreter (and eventually a designer)
 * reason at the category level ("raise Attention") rather than juggling seven
 * independent sliders.
 *
 * <h2>The mapping</h2>
 *
 * <pre>
 *   ATTENTION  → focus                          (where the mind is aimed)
 *   EMOTION    → urgency                        (the felt valence / pressure)
 *   ENERGY     → fatigue                        (the body's reserve)
 *   CONFIDENCE → confidence                     (trust in one's read)
 *   CONTROL    → tension, patience, concealment (regulation of reveal &amp; readiness)
 * </pre>
 *
 * <p>CONTROL is the richest category because cultivators live by concealment
 * and restraint — three of seven channels are about <i>not</i> showing what
 * you are. That asymmetry is intentional and canon-faithful: Wang Lin's
 * entire early arc is Control.
 *
 * <h2>Why categories, not just channels</h2>
 *
 * <p>Channels are the renderer's raw input. Categories are the interpreter's
 * reasoning substrate. The {@link PerformanceInterpreter} reads categories to
 * decide which {@link AnimationDirective}s to emit; the renderer never sees
 * categories or channels — only directives. This is the user's "the renderer
 * eventually shouldn't know what 'focus' means" principle made concrete.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named these five
 * categories and the principle that hierarchies scale better than flat lists.
 */
public enum PerformanceCategory {
    /** Where the mind is aimed. Drives: focus. Manifests as head tracking, saccade, glance cadence. */
    ATTENTION,
    /** The felt valence / pressure of the moment. Drives: urgency. Manifests as breath speed, lean, turn rate. */
    EMOTION,
    /** The body's reserve. Drives: fatigue. Manifests as droop, sag, lag. */
    ENERGY,
    /** Trust in one's read of the situation. Drives: confidence. Manifests as settle, smoothness, shoulder drop. */
    CONFIDENCE,
    /** Regulation of reveal &amp; readiness. Drives: tension, patience, concealment. The cultivator's core discipline. */
    CONTROL;

    /**
     * Classify a channel name (matching the {@link Performance} field names)
     * into its governing category.
     *
     * @param channel one of: focus, urgency, confidence, concealment, tension, patience, fatigue
     * @return the governing category; never null
     */
    public static PerformanceCategory of(String channel) {
        return switch (channel) {
            case "focus"      -> ATTENTION;
            case "urgency"    -> EMOTION;
            case "fatigue"    -> ENERGY;
            case "confidence" -> CONFIDENCE;
            case "tension", "patience", "concealment" -> CONTROL;
            default -> throw new IllegalArgumentException("unknown channel: " + channel);
        };
    }
}
