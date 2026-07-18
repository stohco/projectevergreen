package dev.ergenverse.cultivation;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.perception.PerceptionTier;

/**
 * The unified cultivation realm ladder of the Er Gen multiverse.
 *
 * <p>17 stages across the First, Transitional, Second, and Immortal+ Steps.
 * Each realm has: a lifespan, a perception tier, a canon confidence, and
 * an absolute tier (used in suppression calculations).
 *
 * <p>Cultivators don't level via XP. They have a Cultivation State that
 * changes through events (breakthroughs, setbacks, insights, heart demons).
 */
public enum RealmId {
    // ── First Step (mortal → nascent_soul) ──
    MORTAL           (0, 0, "Mortal",              "凡人",       100,         PerceptionTier.MORTAL,         CanonEngine.Confidence.NOVEL_STATEMENT),
    QI_CONDENSATION  (1, 0, "Qi Condensation",     "练气",       200,         PerceptionTier.QI_CONDENSATION, CanonEngine.Confidence.NOVEL_STATEMENT),
    FOUNDATION       (2, 0, "Foundation",          "筑基",       500,         PerceptionTier.FOUNDATION,      CanonEngine.Confidence.NOVEL_STATEMENT),
    CORE_FORMATION   (3, 0, "Core Formation",      "结丹",       1000,        PerceptionTier.FOUNDATION,      CanonEngine.Confidence.NOVEL_STATEMENT),
    NASCENT_SOUL     (4, 0, "Nascent Soul",        "元婴",       2000,        PerceptionTier.NASCENT_SOUL,    CanonEngine.Confidence.NOVEL_STATEMENT),
    SOUL_FORMATION   (5, 0, "Soul Formation",      "化神",       3000,        PerceptionTier.SOUL_FORMATION,  CanonEngine.Confidence.NOVEL_STATEMENT),
    SOUL_TRANSFORMATION(6, 0, "Soul Transformation","炼虚",      5000,        PerceptionTier.SOUL_FORMATION,  CanonEngine.Confidence.NOVEL_STATEMENT),
    ASCENDANT        (7, 0, "Ascendant",           "合体",       10000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Transitional Step ──
    ILLUSORY_YIN     (8, 1, "Illusory Yin",        "婴变",       15000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    CORPOREAL_YANG   (9, 1, "Corporeal Yang",      "洞玄",       20000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Second Step (Nirvana) ──
    NIRVANA_SCRYER   (10, 2, "Nirvana Scryer",     "窥涅",       50000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    NIRVANA_CLEANSER (11, 2, "Nirvana Cleanser",   "净涅",       100000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    NIRVANA_FRUIT    (12, 2, "Nirvana Fruit",      "碎涅",       200000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    SPIRIT_SEIZER    (13, 2, "Spirit Seizer",      "夺舍",       300000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Immortal+ Step ──
    TRUE_IMMORTAL    (14, 3, "True Immortal",      "真仙",       1000000,     PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    ANCIENT          (15, 3, "Ancient",             "古境",       5000000,     PerceptionTier.TRANSCENDENCE,  CanonEngine.Confidence.NOVEL_STATEMENT),
    PARAGON          (16, 3, "Paragon",             "大天尊",     10000000,    PerceptionTier.TRANSCENDENCE,  CanonEngine.Confidence.NOVEL_STATEMENT),
    TRANSCENDENCE    (17, 4, "Transcendence",       "超脱",       Long.MAX_VALUE, PerceptionTier.TRANSCENDENCE, CanonEngine.Confidence.NOVEL_STATEMENT);

    public final int order;
    public final int step; // 0=First, 1=Transitional, 2=Second, 3=Immortal, 4=4th Step
    public final String name;
    public final String nameCn;
    public final long lifespan;
    public final PerceptionTier perceptionTier;
    public final CanonEngine.Confidence canonConfidence;

    RealmId(int order, int step, String name, String nameCn, long lifespan,
            PerceptionTier perceptionTier, CanonEngine.Confidence canonConfidence) {
        this.order = order;
        this.step = step;
        this.name = name;
        this.nameCn = nameCn;
        this.lifespan = lifespan;
        this.perceptionTier = perceptionTier;
        this.canonConfidence = canonConfidence;
    }

    /** Is this realm at least as high as {@code other}? */
    public boolean isAtLeast(RealmId other) {
        return this.order >= other.order;
    }

    /** Get the next realm in the ladder, or null if at the top. */
    public RealmId next() {
        if (this == TRANSCENDENCE) return null;
        return values()[this.ordinal() + 1];
    }

    /** Get the realm by order index. */
    public static RealmId byOrder(int order) {
        for (RealmId r : values()) if (r.order == order) return r;
        return order >= 17 ? TRANSCENDENCE : MORTAL;
    }
}
