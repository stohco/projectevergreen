package dev.ergenverse.simulation.opportunity;

/**
 * PlayerObserverRealm — the cultivation-realm lens through which a player
 * perceives an opportunity.
 *
 * <p>This is a SIMULATION-LAYER enum (NOT the canonical {@code RealmId}). It
 * exists in this package so the {@link OpportunityFSM} can answer the question
 * "what does THIS player perceive about THIS opportunity?" without depending
 * on the cultivation subsystem. The mapping from canonical {@code RealmId} to
 * {@code PlayerObserverRealm} is the integration layer's job (Phase B).
 *
 * <p>Per PROJECT_MASTER.md §5.5 (Divine Sense Atlas), perception scales
 * fundamentally with cultivation realm:
 * <ul>
 *   <li>{@link #MORTAL} — perceives almost nothing (vague unease, strange
 *       sounds, deer acting oddly). The world is large and mysterious.</li>
 *   <li>{@link #QI_CONDENSATION} — perceives spiritual fluctuations, basic
 *       beast territories, herb locations.</li>
 *   <li>{@link #FOUNDATION} — perceives spirit veins, hidden caves, qi density.</li>
 *   <li>{@link #CORE_FORMATION} — perceives formation nodes, restriction
 *       signatures, mid-tier beast territories.</li>
 *   <li>{@link #NASCENT_SOUL} — perceives karmic nodes, full spirit-beast
 *       territories, faction scout movements.</li>
 *   <li>{@link #SOUL_FORMATION} — perceives ancient battlefields, sealed
 *       realms, hidden inheritances, dormant opportunities.</li>
 *   <li>{@link #ASCENDANT_PLUS} — perceives EVERYTHING. Space cracks, law
 *       distortions, void paths. The full picture.</li>
 * </ul>
 *
 * <h2>Why this is a separate enum from {@code RealmId}</h2>
 * <p>The simulation/opportunity package must remain decoupled from the
 * cultivation subsystem. Future phases may swap the cultivation ladder (e.g.
 * for ISSTH or AWWP protagonists whose realms differ from Renegade Immortal's
 * Wang Lin). This enum stays stable across such swaps. The integration layer
 * (a future Forge event subscriber in another package) is responsible for
 * translating whatever the player's actual cultivation state is into one of
 * these seven perception tiers.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Mortals perceive almost nothing. Ascendant+ perceives everything.<br>
 *   The tier SCALING is canon-faithful — not "more is better", but
 *   "fundamentally different perception of reality".
 * </blockquote>
 */
public enum PlayerObserverRealm {
    /** Mortal — perceives almost nothing. The world is large and mysterious. */
    MORTAL(0, "Mortal"),
    /** Qi Condensation — perceives spiritual fluctuations, basic beast territories. */
    QI_CONDENSATION(1, "Qi Condensation"),
    /** Foundation — perceives spirit veins, hidden caves, qi density. */
    FOUNDATION(2, "Foundation"),
    /** Core Formation — perceives formation nodes, restriction signatures. */
    CORE_FORMATION(3, "Core Formation"),
    /** Nascent Soul — perceives karmic nodes, full territories, scout movements. */
    NASCENT_SOUL(4, "Nascent Soul"),
    /** Soul Formation — perceives ancient battlefields, sealed realms, dormant opportunities. */
    SOUL_FORMATION(5, "Soul Formation"),
    /** Ascendant and above — perceives EVERYTHING. Space cracks, void paths. */
    ASCENDANT_PLUS(6, "Ascendant+");

    public final int order;
    public final String label;

    PlayerObserverRealm(int order, String label) {
        this.order = order;
        this.label = label;
    }

    /** Is this realm at least as advanced as {@code other}? */
    public boolean isAtLeast(PlayerObserverRealm other) {
        return this.order >= other.order;
    }

    /** Whether this realm perceives EVERYTHING (used as a fast-path check). */
    public boolean isOmniscient() {
        return this == ASCENDANT_PLUS;
    }

    /** Whether this realm perceives almost nothing (used as a fast-path check). */
    public boolean isBlind() {
        return this == MORTAL;
    }
}
