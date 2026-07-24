package dev.ergenverse.simulation.intent;

/**
 * AnimationDirective — a semantic instruction the renderer can obey without
 * knowing psychology.
 *
 * <p>This is the user's 2026-07-26 architectural pivot, stated verbatim:
 *
 * <blockquote>
 * I'd eventually introduce something like
 *   Performance → Performance Interpreter → Animation Directives → Renderer
 * Example:
 *   Performance → MaintainEyeContact, HoldBreath, ReduceIdleMotion → Renderer
 * Now the renderer doesn't know psychology. It only knows directives.
 * That keeps rendering completely independent from cognition.
 * </blockquote>
 *
 * <p>A directive is a named behavioral instruction (e.g. {@link Name#LOCK_ATTENTION})
 * paired with an intensity in {@code [0,1]}. The {@link PerformanceInterpreter}
 * produces a list of these from a {@link Performance}; the renderer's
 * {@code applyDirectives} consumes them. The renderer implements each directive
 * as a concrete body-language effect (head lerp speed, saccade amplitude, breath
 * rate, etc.) and never reads the underlying focus/urgency/confidence values.
 *
 * <p>This is the boundary between "what should the player understand about what
 * the NPC is thinking" (the interpreter's job) and "how does the body move"
 * (the renderer's job). Crossing this boundary in either direction is a bug:
 * the interpreter must not set ModelPart rotations; the renderer must not read
 * Performance channels.
 *
 * <h2>The directive vocabulary</h2>
 *
 * <p>Nine directives, one per body channel the user named (Head, Torso,
 * Shoulders, Hands, Feet, Eyes, Breathing, Weight, Fatigue-sag). Each is
 * <b>additive</b> — directives layer on top of the pose, they never override
 * it. This preserves CRON-21/22's guarantee that Performance modulates rather
 * than replaces the materializer pose.
 *
 * <ul>
 *   <li>{@link Name#LOCK_ATTENTION} — head tracks slow, tiny saccade, rare glance-away</li>
 *   <li>{@link Name#SCAN_URGENT} — head turns quick, frequent surrounding checks, forward lean</li>
 *   <li>{@link Name#SETTLE} — smooth stable tracking, shoulders dropped, at-ease arms</li>
 *   <li>{@link Name#CONCEAL_WEAPON_HAND} — hand drifts toward weapon, weight shifts back, breath suppressed</li>
 *   <li>{@link Name#BRACE} — shoulders raised, torso rigid, shallow fast breath</li>
 *   <li>{@link Name#HOLD_GROUND} — feet planted, weight still, long glance holds</li>
 *   <li>{@link Name#FIDGET} — feet shuffle, weight shifts, short glance holds</li>
 *   <li>{@link Name#SAG_FATIGUE} — head droops, shoulders round, eyes lag downward</li>
 *   <li>{@link Name#ANTICIPATE_TARGET} — eyes lead head by a few degrees (anticipatory drift)</li>
 * </ul>
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named the interpreter
 * layer and gave MaintainEyeContact / HoldBreath / ReduceIdleMotion as examples.
 * The nine names here are the project's concrete vocabulary.
 */
public final class AnimationDirective {

    /** The named vocabulary of directives the renderer knows how to obey. */
    public enum Name {
        LOCK_ATTENTION,
        SCAN_URGENT,
        SETTLE,
        CONCEAL_WEAPON_HAND,
        BRACE,
        HOLD_GROUND,
        FIDGET,
        SAG_FATIGUE,
        ANTICIPATE_TARGET
    }

    /** The directive's name. */
    public final Name name;
    /** The directive's intensity in {@code [0,1]}. 0 means "do nothing"; 1 means "full effect." */
    public final float intensity;

    public AnimationDirective(Name name, float intensity) {
        this.name = name;
        this.intensity = Math.max(0.0f, Math.min(1.0f, intensity));
    }

    @Override
    public String toString() {
        return name + "(" + String.format("%.2f", intensity) + ")";
    }
}
