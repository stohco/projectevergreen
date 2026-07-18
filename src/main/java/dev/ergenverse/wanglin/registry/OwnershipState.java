package dev.ergenverse.wanglin.registry;

/**
 * OwnershipState — the canonical ownership state of an entity relative to Wang Lin.
 *
 * <p>Every {@link CanonicalEntry} carries exactly one OwnershipState. This is
 * the foundational question the TeachingResolver asks before any other gate:
 * <i>"did Wang Lin ever actually own, encounter, or witness this?"</i>
 *
 * <h2>States</h2>
 * <ul>
 *   <li>{@link #WANG_LIN_OWNED} — Wang Lin possessed/possesses this. Eligible
 *       for teaching (subject to other gates).</li>
 *   <li>{@link #FORMERLY_OWNED} — Wang Lin owned this in the past but lost,
 *       sold, gave away, fused, or destroyed it (per CANON_RI_COMPLETE_ITEMS.md
 *       status field). Still eligible for teaching.</li>
 *   <li>{@link #ENCOUNTERED} — Wang Lin encountered this in the world (saw,
 *       touched, fought against) but never owned it. Teaching limited to
 *       witness testimony.</li>
 *   <li>{@link #WITNESSED_IN_USE} — Wang Lin saw someone else use this. He
 *       can describe the demonstrated behavior but cannot teach the
 *       activation method.</li>
 *   <li>{@link #HEARD_OF_ONLY} — Wang Lin heard of this through rumor,
 *       inheritance, or lore. He can attest to its existence and reputation
 *       but cannot demonstrate.</li>
 *   <li>{@link #INHERITED_THEN_LOST} — Wang Lin inherited this (Tu Si, Situ
 *       Nan, Bai Fan, Dun Tian inheritances) but the inheritance was
 *       consumed, fused, or lost. Still eligible for teaching of the
 *       comprehension gained.</li>
 * </ul>
 */
public enum OwnershipState {
    WANG_LIN_OWNED("Wang Lin owned/owns this", true),
    FORMERLY_OWNED("Wang Lin formerly owned this (lost / sold / given / fused / destroyed)", true),
    ENCOUNTERED("Wang Lin encountered this in the world", false),
    WITNESSED_IN_USE("Wang Lin witnessed another cultivator using this", false),
    HEARD_OF_ONLY("Wang Lin heard of this through rumor / inheritance / lore", false),
    INHERITED_THEN_LOST("Wang Lin inherited this, then consumed/fused/lost it", true);

    public final String description;
    /** Whether this state permits teaching (subject to other gates). */
    public final boolean teachable;

    OwnershipState(String description, boolean teachable) {
        this.description = description;
        this.teachable = teachable;
    }

    /** Whether this state lets the TeachingResolver proceed past Gate 1. */
    public boolean passesOwnershipGate() {
        return teachable;
    }
}
