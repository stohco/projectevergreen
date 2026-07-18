package dev.ergenverse.wanglin.registry;

/**
 * Transferability — the canon-grounded verdict on what Wang Lin can do with this
 * entity <i>relative to a player</i>.
 *
 * <p>This is the central record the user demanded: every canonical item,
 * technique, beast, companion, avatar, inheritance, and treasure must be
 * decomposed into <i>can he teach it, demonstrate it, explain it, transfer it,
 * gift an equivalent, or create a new one?</i> — each with a canon-basis citing
 * the chapter(s) that justify the verdict.
 *
 * <p>Per the Prime Directive and the user's "exact copy" directive (worklog
 * Task RI-BIBLE-wiki-research + exact-copy-fix): the canonical original is
 * never removed from Wang Lin; an exact copy MAY be granted where canon
 * supports it. The {@link #canTransfer} and {@link #canGiftEquivalent} fields
 * reflect this.
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li>{@link #canTeach} — WILL_TEACH / REFUSES / OFFERS_PARTIAL / TESTS_FIRST /
 *       CANNOT_TEACH. Delegates to the existing {@code WangLinTeachingPolicy}
 *       engine when the entry corresponds to a known TeachingOffering.</li>
 *   <li>{@link #canDemonstrate} — {@link Demonstrability} enum.</li>
 *   <li>{@link #canExplain} — whether Wang Lin can put the mechanics into words.</li>
 *   <li>{@link #canTransfer} — YES / NO / PARTIAL / REQUIRES_RITUAL / UNIQUELY_BOUND.</li>
 *   <li>{@link #canGiftEquivalent} — YES / NO / AT_AFFINITY_LEVEL_X. The "exact
 *       copy" directive lives here: where canon supports an exact copy,
 *       this is YES.</li>
 *   <li>{@link #canCreateNew} — YES / NO / UNKNOWN. Whether Wang Lin can make
 *       another (e.g. he made 3 Restriction Flags; he cannot make another
 *       Heaven-Defying Bead).</li>
 *   <li>{@link #canonBasis} — the chapter(s) / wiki pages that justify the
 *       verdicts above. Never empty.</li>
 * </ul>
 *
 * @param canTeach           teaching verdict
 * @param canDemonstrate     demonstration verdict
 * @param canExplain         explanation verdict
 * @param canTransfer        transfer verdict
 * @param canGiftEquivalent  gift-equivalent verdict (the "exact copy" directive)
 * @param canCreateNew       creation verdict
 * @param canonBasis         chapter citations / wiki references — never null
 */
public record Transferability(
        CanTeach canTeach,
        Demonstrability canDemonstrate,
        CanExplain canExplain,
        CanTransfer canTransfer,
        CanGiftEquivalent canGiftEquivalent,
        CanCreateNew canCreateNew,
        String canonBasis
) {
    /** Whether Wang Lin will teach this — the four canon gates plus CANNOT. */
    public enum CanTeach {
        WILL_TEACH, REFUSES, OFFERS_PARTIAL, TESTS_FIRST, CANNOT_TEACH
    }

    /** Whether Wang Lin can verbally explain this. */
    public enum CanExplain {
        YES_FULLY, YES_PARTIAL, NO_BOUND_BY_OATH, NO_LACKS_UNDERSTANDING, UNKNOWN
    }

    /** Whether Wang Lin can transfer the canonical original (always NO per user directive). */
    public enum CanTransfer {
        YES, NO, PARTIAL, REQUIRES_RITUAL, UNIQUELY_BOUND
    }

    /** Whether Wang Lin can gift an exact copy or equivalent. */
    public enum CanGiftEquivalent {
        YES_EXACT_COPY, YES_DERIVED_EQUIVALENT, NO, AT_AFFINITY_LEVEL_X, UNIQUELY_BOUND
    }

    /** Whether Wang Lin can create a new instance of this. */
    public enum CanCreateNew {
        YES, NO, REQUIRES_MATERIALS, REQUIRES_RITUAL, UNKNOWN
    }

    public Transferability {
        if (canTeach == null) canTeach = CanTeach.CANNOT_TEACH;
        if (canDemonstrate == null) canDemonstrate = Demonstrability.UNKNOWN;
        if (canExplain == null) canExplain = CanExplain.UNKNOWN;
        if (canTransfer == null) canTransfer = CanTransfer.UNIQUELY_BOUND;
        if (canGiftEquivalent == null) canGiftEquivalent = CanGiftEquivalent.UNIQUELY_BOUND;
        if (canCreateNew == null) canCreateNew = CanCreateNew.UNKNOWN;
        if (canonBasis == null || canonBasis.isBlank()) {
            canonBasis = "Canon basis not recorded — gap flagged for review.";
        }
    }

    // ── Convenience factories ─────────────────────────────────────────

    /** A fully transferable, teachable, copyable entity (rare — e.g. life-lesson wisdom). */
    public static Transferability freelyTeachable(String canonBasis) {
        return new Transferability(
                CanTeach.WILL_TEACH, Demonstrability.CAN_DEMONSTRATE_FULLY,
                CanExplain.YES_FULLY, CanTransfer.YES,
                CanGiftEquivalent.YES_DERIVED_EQUIVALENT, CanCreateNew.YES,
                canonBasis);
    }

    /** A uniquely-bound entity — the Heaven-Defying Bead, Lu Mo, Slaughter True Body.
     *  Cannot be transferred, gifted, or duplicated. */
    public static Transferability uniquelyBound(String canonBasis) {
        return new Transferability(
                CanTeach.REFUSES, Demonstrability.UNIQUELY_BOUND,
                CanExplain.YES_PARTIAL, CanTransfer.UNIQUELY_BOUND,
                CanGiftEquivalent.UNIQUELY_BOUND, CanCreateNew.NO,
                canonBasis);
    }

    /** A self-forged treasure Wang Lin can make more of (Restriction Flag, soul flag). */
    public static Transferability selfForged(String canonBasis) {
        return new Transferability(
                CanTeach.TESTS_FIRST, Demonstrability.CAN_DEMONSTRATE_FULLY,
                CanExplain.YES_FULLY, CanTransfer.PARTIAL,
                CanGiftEquivalent.YES_EXACT_COPY, CanCreateNew.REQUIRES_MATERIALS,
                canonBasis);
    }

    /** A contracted inheritance Wang Lin can teach only with the original giver's permission. */
    public static Transferability contractedInheritance(String canonBasis) {
        return new Transferability(
                CanTeach.TESTS_FIRST, Demonstrability.CAN_DEMONSTRATE_FULLY,
                CanExplain.YES_FULLY, CanTransfer.REQUIRES_RITUAL,
                CanGiftEquivalent.YES_DERIVED_EQUIVALENT, CanCreateNew.REQUIRES_RITUAL,
                canonBasis);
    }

    /** A technique Wang Lin personally invented — teachable but not giftable as a "treasure". */
    public static Transferability selfCreated(String canonBasis) {
        return new Transferability(
                CanTeach.WILL_TEACH, Demonstrability.CAN_DEMONSTRATE_FULLY,
                CanExplain.YES_FULLY, CanTransfer.PARTIAL,
                CanGiftEquivalent.YES_DERIVED_EQUIVALENT, CanCreateNew.YES,
                canonBasis);
    }
}
