package dev.ergenverse.simulation.cognition;

/**
 * Need — 28 need types in the unified cognition model.
 *
 * <p>Needs drive goal generation. Each need has an intensity (0-1) and a
 * satiation rate. When intensity exceeds threshold, the GoalGenerator
 * converts it to a goal.
 *
 * <p>RI-specific needs: HEART_DEMON, KARMIC_DEBT, DAO_HEART, TRIBULATION_DEBT,
 * FILIAL_PIETY, SWORN_BROTHERHOOD.
 *
 * <p>Per canon audit: NO BOREDOM (mortals don't get bored in xianxia worlds —
 * they're too busy surviving), NO PRIDE (merged into FACE — face captures
 * the social dimension that pride alone misses), NO SEEKING_INSIGHT (merged
 * into SEEKING_DAO — insight is a sub-act of seeking the Dao).
 */
public enum Need {
    // ── Survival needs (Maslow-style base) ──
    FOOD            ("Food",              0.9, 0.3),
    WATER           ("Water",             0.9, 0.3),
    REST            ("Rest",              0.7, 0.4),
    SAFETY          ("Safety",            0.8, 0.2),
    SHELTER         ("Shelter",           0.5, 0.2),

    // ── Cultivation needs ──
    QI              ("Qi",                0.85, 0.25),
    SEEKING_DAO     ("Seeking Dao",       0.6, 0.05),   // merged: SEEKING_INSIGHT folded here
    DAO_HEART       ("Dao Heart",         0.7, 0.05),
    BREAKTHROUGH    ("Breakthrough",      0.5, 0.0),
    TRIBULATION_DEBT("Tribulation Debt",  0.4, 0.0),
    RESOURCE        ("Cultivation Resource", 0.7, 0.2),

    // ── Social needs ──
    AFFECTION       ("Affection",         0.5, 0.15),
    BELONGING       ("Belonging",         0.6, 0.1),
    FACE            ("Face",              0.65, 0.1),    // merged: PRIDE folded here
    REPUTATION      ("Reputation",        0.55, 0.1),
    STATUS          ("Status",            0.55, 0.1),

    // ── Karmic / moral needs (RI-specific) ──
    KARMIC_DEBT     ("Karmic Debt",       0.5, 0.05),
    HEART_DEMON     ("Heart Demon",       0.6, 0.0),
    FILIAL_PIETY    ("Filial Piety",      0.5, 0.05),
    SWORN_BROTHERHOOD("Sworn Brotherhood", 0.55, 0.05),
    GRATITUDE       ("Gratitude",         0.55, 0.05),
    REVENGE         ("Revenge",           0.7, 0.0),

    // ── Information / mastery needs ──
    KNOWLEDGE       ("Knowledge",         0.5, 0.1),
    CRAFT           ("Crafting Mastery",  0.4, 0.1),

    // ── Autonomy / freedom needs ──
    FREEDOM         ("Freedom",           0.5, 0.1),
    CURIOSITY       ("Curiosity",         0.4, 0.15),

    // ── Self-actualization / transcendence ──
    LEGACY          ("Legacy",            0.4, 0.0),
    TRANSCENDENCE   ("Transcendence",     0.3, 0.0);

    public final String label;
    /** Threshold above which this need becomes a goal. 0..1. */
    public final double threshold;
    /** Per-tick natural decay (satiation) of intensity. */
    public final double decay;

    Need(String label, double threshold, double decay) {
        this.label = label;
        this.threshold = threshold;
        this.decay = decay;
    }
}
