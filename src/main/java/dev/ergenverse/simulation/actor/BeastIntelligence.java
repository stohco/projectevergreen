package dev.ergenverse.simulation.actor;

/**
 * BeastIntelligence — 7 tiers of spirit-beast intelligence.
 *
 * <p>Per canon, spirit beasts have intelligence tiers that determine whether
 * they can be reasoned with, tamed, or whether they are essentially geographic
 * features. The {@link Actor} framework consults this tier when the
 * {@link dev.ergenverse.simulation.cognition.DecisionEngine} decides whether
 * a beast gets cognition.
 *
 * <p>The 7 tiers (canon audit, RI-canon):
 * <ol>
 *   <li>{@code INSTINCT} — pure instinct. Mortal rabbit-tier. No cognition.</li>
 *   <li>{@code AWARE} — knows it's a spirit beast. Avoids cultivators. Minimal cognition.</li>
 *   <li>{@code CUNNING} — can set traps, flee intelligently. Predator-tier.</li>
 *   <li>{@code SPIRIT} — full spirit intelligence. Can form Dao. Active-actor cognition.</li>
 *   <li>{@code DEMON} — humanoid intelligence. Can speak, scheme, sect-found.</li>
 *   <li>{@code ANCIENT} — ancient beast. Geographic-scale cognition. Slow but vast.</li>
 *   <li>{@code OLD_MONSTER} — true immortal-tier beast. Equal to a peak cultivator.</li>
 * </ol>
 */
public enum BeastIntelligence {
    INSTINCT     ("Instinct",       0),
    AWARE        ("Aware",          1),
    CUNNING      ("Cunning",        2),
    SPIRIT       ("Spirit",         3),
    DEMON        ("Demon",          4),
    ANCIENT      ("Ancient",        5),
    OLD_MONSTER  ("Old Monster",    6);

    public final String label;
    public final int order;

    BeastIntelligence(String label, int order) {
        this.label = label;
        this.order = order;
    }

    /** Returns true if this beast tier warrants cognition (SPIRIT or higher). */
    public boolean warrantsCognition() {
        return order >= SPIRIT.order;
    }

    /** Returns true if this beast tier warrants full cognition (DEMON or higher). */
    public boolean warrantsFullCognition() {
        return order >= DEMON.order;
    }

    /** Canonical count — canon audit requires 7. */
    public static final int CANON_COUNT = 7;
}
