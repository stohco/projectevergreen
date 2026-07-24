package dev.ergenverse.simulation.intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PerformanceInterpreter — translates a {@link Performance} into a list of
 * {@link AnimationDirective}s.
 *
 * <p>This is the user's 2026-07-26 architectural pivot, made concrete:
 *
 * <blockquote>
 * I'd eventually introduce something like
 *   Performance → Performance Interpreter → Animation Directives → Renderer
 * Now the renderer doesn't know psychology. It only knows directives.
 * That keeps rendering completely independent from cognition.
 * </blockquote>
 *
 * <h2>What this layer IS</h2>
 *
 * <p>The interpreter is the only place in the codebase that reads Performance
 * channels and decides what they <b>mean</b> for the body. It answers the
 * question "what should the player understand about what the NPC is thinking?"
 * and emits directives that make that understanding visible. The renderer then
 * obeys the directives without knowing why they were emitted.
 *
 * <p>This is the boundary between meaning and motion. Crossing it in either
 * direction is a bug:
 * <ul>
 *   <li>The interpreter must not touch ModelPart rotations.</li>
 *   <li>The renderer must not read Performance channels.</li>
 * </ul>
 *
 * <h2>What this layer IS NOT</h2>
 *
 * <p>It is NOT cognition. It adds no intelligence. The Commitment already
 * decided what to do; the Performance already encoded the acting directions;
 * the interpreter merely groups those directions into named behavioral
 * instructions the renderer can obey blindly. It is a translator, not a thinker.
 *
 * <h2>The mapping — categories to directives</h2>
 *
 * <p>The interpreter reasons in {@link PerformanceCategory}s (the user's five
 * hierarchies), not in raw channels:
 *
 * <pre>
 *   ATTENTION  (focus)      → LOCK_ATTENTION, ANTICIPATE_TARGET
 *   EMOTION    (urgency)    → SCAN_URGENT
 *   ENERGY     (fatigue)    → SAG_FATIGUE
 *   CONFIDENCE (confidence) → SETTLE
 *   CONTROL    (tension)    → BRACE
 *   CONTROL    (patience)   → HOLD_GROUND  (high) / FIDGET (low)
 *   CONTROL    (concealment)→ CONCEAL_WEAPON_HAND (when urgency also high)
 * </pre>
 *
 * <p>Each directive is emitted with an intensity derived from its source
 * channel(s). The CONCEAL_WEAPON_HAND directive is special: it fires only when
 * <b>both</b> concealment and urgency are high (the product), because a calm
 * concealed NPC keeps hands relaxed and a panicked un-concealed NPC flails —
 * only the tense-hidden case draws the hand toward the weapon. This is the
 * user's canonical "unknown cultivator" tell.
 *
 * <h2>Why a list, not a set</h2>
 *
 * <p>Directives can coexist and even tension against each other: LOCK_ATTENTION
 * (slow head) can coexist with SCAN_URGENT (quick checks) — the net effect is
 * a head that mostly holds still but periodically snaps to scan. The renderer
 * resolves these by applying them additively in a fixed order. A set would lose
 * the intensity nuance; a list preserves it.
 *
 * <p>USER-DIRECTED. The 2026-07-26 review explicitly named this layer and gave
 * MaintainEyeContact / HoldBreath / ReduceIdleMotion as examples. The mapping
 * below is the project's concrete instantiation of that principle.
 */
public final class PerformanceInterpreter {

    private PerformanceInterpreter() {}

    /**
     * Interpret a Performance into the directives that communicate it.
     *
     * @param p the active Performance; if {@link Performance#isNone()}, returns empty
     * @return an unmodifiable list of directives (possibly empty, never null)
     */
    public static List<AnimationDirective> interpret(Performance p) {
        if (p.isNone()) return Collections.emptyList();

        List<AnimationDirective> out = new ArrayList<>(9);

        // ── ATTENTION (focus) ──────────────────────────────────────────
        // High focus → LOCK_ATTENTION (slow deliberate tracking, tiny saccade,
        // rare glance-away). The wolf-observer at focus=0.95 locks hard.
        // ANTICIPATE_TARGET also scales with focus — concentrated eyes lead
        // the head by a few degrees (the "track slightly ahead" directive).
        if (p.focus > 0.5f) {
            out.add(new AnimationDirective(AnimationDirective.Name.LOCK_ATTENTION, p.focus));
            out.add(new AnimationDirective(AnimationDirective.Name.ANTICIPATE_TARGET,
                    (p.focus - 0.5f) * 2.0f)); // remap 0.5→1.0 into 0→1
        }

        // ── EMOTION (urgency) ──────────────────────────────────────────
        // High urgency → SCAN_URGENT (quick head turns, forward lean, fast
        // surrounding checks). The unknown-cultivator observer at urgency=0.85
        // scans hard. Low urgency emits nothing (calm is the absence of scan,
        // not a separate directive).
        if (p.urgency > 0.35f) {
            out.add(new AnimationDirective(AnimationDirective.Name.SCAN_URGENT, p.urgency));
        }

        // ── ENERGY (fatigue) ───────────────────────────────────────────
        // High fatigue → SAG_FATIGUE (head droops, shoulders round, eyes lag).
        // Only fires past 0.3 — short commitments show no sag.
        if (p.fatigue > 0.3f) {
            out.add(new AnimationDirective(AnimationDirective.Name.SAG_FATIGUE, p.fatigue));
        }

        // ── CONFIDENCE (confidence) ────────────────────────────────────
        // High confidence → SETTLE (smooth stable tracking, shoulders dropped,
        // at-ease arms). Only fires past 0.7 — below that, confidence is
        // neutral (neither settled nor visibly unsettled; BRACE handles the
        // visibly unsettled case via tension).
        if (p.confidence > 0.7f) {
            out.add(new AnimationDirective(AnimationDirective.Name.SETTLE,
                    (p.confidence - 0.7f) * (1.0f / 0.3f))); // remap 0.7→1.0 into 0→1
        }

        // ── CONTROL (tension) ──────────────────────────────────────────
        // High tension → BRACE (shoulders raised, torso rigid, shallow breath).
        // Fires past 0.4 — below that the body is relaxed.
        if (p.tension > 0.4f) {
            out.add(new AnimationDirective(AnimationDirective.Name.BRACE,
                    (p.tension - 0.4f) * (1.0f / 0.6f))); // remap 0.4→1.0 into 0→1
        }

        // ── CONTROL (patience) ─────────────────────────────────────────
        // High patience → HOLD_GROUND (feet planted, weight still, long glance
        // holds). Low patience → FIDGET (feet shuffle, weight shifts, short
        // holds). These are mutually exclusive — an NPC is either planted or
        // fidgeting, not both. The split point is 0.5.
        if (p.patience >= 0.5f) {
            out.add(new AnimationDirective(AnimationDirective.Name.HOLD_GROUND,
                    (p.patience - 0.5f) * 2.0f)); // remap 0.5→1.0 into 0→1
        } else {
            out.add(new AnimationDirective(AnimationDirective.Name.FIDGET,
                    (0.5f - p.patience) * 2.0f)); // remap 0.5→0.0 into 0→1
        }

        // ── CONTROL (concealment × urgency) ────────────────────────────
        // CONCEAL_WEAPON_HAND fires only when BOTH concealment and urgency are
        // high. The product is high only in the tense-hidden case: calm-hidden
        // (low urgency) keeps hands relaxed; panicked-exposed (low concealment)
        // flails. Only the unknown-cultivator case (concealment≈0.95,
        // urgency≈0.85) draws the hand to the weapon. This is the user's
        // canonical "hand nearer weapon" tell.
        float weaponReadiness = p.concealment * p.urgency;
        if (weaponReadiness > 0.2f) {
            out.add(new AnimationDirective(AnimationDirective.Name.CONCEAL_WEAPON_HAND,
                    Math.min(1.0f, weaponReadiness * 1.5f))); // scale up so 0.66 product → 1.0
        }

        return Collections.unmodifiableList(out);
    }
}
