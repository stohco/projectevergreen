package dev.ergenverse.wanglin.registry;

/**
 * Demonstrability — what Wang Lin can do with this canonical entity, beyond mere ownership.
 *
 * <p>Ownership says "Wang Lin had it". Demonstrability says "and he can
 * <i>show, explain, or recreate</i> it". The distinction matters: Wang Lin
 * owned the Heaven-Defying Bead (OWNED) but cannot demonstrate its core
 * mysteries (UNIQUELY_BOUND); he can demonstrate Restriction Flag construction
 * (CAN_DEMONSTRATE_FULLY) because he personally forged three.
 *
 * <p>Per the Prime Directive: where canon is silent on whether Wang Lin
 * demonstrated an ability, mark {@link #UNKNOWN} — never invent.
 */
public enum Demonstrability {
    /** Wang Lin canonically demonstrated this ability in full (chapter-cited). */
    CAN_DEMONSTRATE_FULLY("Wang Lin canonically demonstrated this in full"),
    /** Wang Lin demonstrated a partial / derived form (e.g. only one of three variations). */
    CAN_DEMONSTRATE_PARTIAL("Wang Lin demonstrated a partial / derived form"),
    /** Wang Lin explained or attested to it but did not demonstrate on-screen. */
    CAN_EXPLAIN_BUT_NOT_DEMONSTRATE("Wang Lin explained it but did not demonstrate on-screen"),
    /** Wang Lin can attest to its existence but cannot explain mechanics. */
    ATTEST_ONLY("Wang Lin attests to its existence; mechanics unknown to him"),
    /** Bound to a unique artifact / lineage; cannot be demonstrated even by Wang Lin. */
    UNIQUELY_BOUND("Bound to a unique artifact/lineage; cannot be demonstrated"),
    /** Canon is silent on whether Wang Lin can demonstrate this. */
    UNKNOWN("Canon does not record whether Wang Lin demonstrated this");

    public final String description;

    Demonstrability(String description) {
        this.description = description;
    }
}
