package dev.ergenverse.simulation.cognition;

/**
 * DaoIdentity — the 30 Dao types a cultivator can follow in the Er Gen multiverse.
 *
 * <p>Each Dao colors goal selection: a SLAUGHTER cultivator weights killing
 * goals higher; a KARMA cultivator weighs debt-resolution higher; a SECT_LEADER
 * weighs sect-preservation higher. The {@link #goalWeightModifier(CognitionGoal.Category)}
 * function is the heart of the Dao Identity system.
 *
 * <p>RI-specific entries: DEFIANCE, SLAUGHTER, KARMA, LIFE_DEATH, TRUE_FALSE,
 * RESTRICTION, ROGUE_CULTIVATOR, SECT_LEADER, BEAST_KING, AVENGER, etc.
 *
 * <p>These map roughly to canonical Dao paths walked by characters in
 * Renegade Immortal, I Shall Seal the Heavens, A Will Eternal,
 * Pursuit of Truth, A World Worth Protecting, and Outside of Time.
 */
public enum DaoIdentity {
    // ── Classic elemental Daos ──
    FIRE            ("Fire Dao",         1.0),
    WATER           ("Water Dao",        1.0),
    EARTH           ("Earth Dao",        1.0),
    WIND            ("Wind Dao",         1.0),
    METAL           ("Metal Dao",        1.0),
    WOOD            ("Wood Dao",         1.0),
    LIGHTNING       ("Lightning Dao",    1.0),

    // ── Cosmic Daos ──
    KARMA           ("Karma Dao",        1.0),
    LIFE_DEATH      ("Life & Death Dao", 1.0),
    SAMSARA         ("Samsara Dao",      1.0),
    TIME            ("Time Dao",         1.0),
    SPACE           ("Space Dao",        1.0),
    DREAM           ("Dream Dao",        1.0),

    // ── Wang Lin's Dao (Renegade Immortal) ──
    DEFIANCE        ("Defiance Dao",     1.0),
    SLAUGHTER       ("Slaughter Dao",    1.0),
    RESTRICTION     ("Restriction Dao",  1.0),
    TRUE_FALSE      ("True-False Dao",   1.0),

    // ── Meng Hao / Bai Xiaochun line Daos ──
    SEEKING_DAO     ("Seeking Dao",      1.0),
    SECT_LEADER     ("Sect Leader Dao",  1.0),
    ROGUE_CULTIVATOR("Rogue Cultivator Dao", 1.0),

    // ── Beast / nature Daos ──
    BEAST_KING      ("Beast King Dao",   1.0),
    NATURE          ("Nature Dao",       1.0),
    BLOODLINE       ("Bloodline Dao",    1.0),

    // ── Dao of War / revenge / honor ──
    AVENGER         ("Avenger Dao",      1.0),
    WAR             ("War Dao",          1.0),
    HONOR           ("Honor Dao",        1.0),

    // ── Heart / mind Daos ──
    HEART_DEMON     ("Heart Demon Dao",  1.0),
    INTEGRITY       ("Integrity Dao",    1.0),

    // ── Sword / spell Daos ──
    SWORD           ("Sword Dao",        1.0),
    SEAL            ("Seal Dao",         1.0);

    public final String label;
    public final double baseGoalWeight;

    DaoIdentity(String label, double baseGoalWeight) {
        this.label = label;
        this.baseGoalWeight = baseGoalWeight;
    }

    /**
     * Returns a multiplier (0.0 to 3.0) applied to goals of the given category
     * when this Dao is the actor's primary identity.
     *
     * <p>A multiplier &gt; 1.0 means the actor pursues that category more eagerly;
     * &lt; 1.0 means they avoid it; 0.0 means they will not pursue it at all.
     *
     * <p>This is the single most important function in the cognition system —
     * it is how Dao Identity shapes behavior without writing if/else trees.
     */
    public double goalWeightModifier(CognitionGoal.Category category) {
        switch (this) {
            case SLAUGHTER:
                if (category == CognitionGoal.Category.KILL)        return 3.0;
                if (category == CognitionGoal.Category.DEFEND)      return 1.2;
                if (category == CognitionGoal.Category.MEDITATE)    return 0.6;
                return 1.0;
            case KARMA:
                if (category == CognitionGoal.Category.RESOLVE_DEBT)   return 3.0;
                if (category == CognitionGoal.Category.SEEKING_DAO)    return 1.4;
                if (category == CognitionGoal.Category.KILL)           return 0.5;
                return 1.0;
            case LIFE_DEATH:
                if (category == CognitionGoal.Category.SEEKING_DAO)    return 2.2;
                if (category == CognitionGoal.Category.RESURRECT)      return 3.0;
                if (category == CognitionGoal.Category.SURVIVE)        return 1.5;
                return 1.0;
            case TRUE_FALSE:
                if (category == CognitionGoal.Category.SEEKING_DAO)    return 2.0;
                if (category == CognitionGoal.Category.INVESTIGATE)    return 2.5;
                if (category == CognitionGoal.Category.DECEIVE)        return 1.8;
                return 1.0;
            case DEFIANCE:
                if (category == CognitionGoal.Category.SEEKING_DAO)    return 2.5;
                if (category == CognitionGoal.Category.BREAKTHROUGH)   return 2.5;
                if (category == CognitionGoal.Category.SUBMIT)         return 0.1;
                if (category == CognitionGoal.Category.FLEE)           return 0.4;
                return 1.0;
            case RESTRICTION:
                if (category == CognitionGoal.Category.STUDY)          return 2.5;
                if (category == CognitionGoal.Category.BREAK_FORMATION) return 2.0;
                if (category == CognitionGoal.Category.CRAFT)          return 1.5;
                return 1.0;
            case SECT_LEADER:
                if (category == CognitionGoal.Category.DEFEND)         return 2.0;
                if (category == CognitionGoal.Category.POLITICS)       return 2.5;
                if (category == CognitionGoal.Category.GATHER_RESOURCE) return 1.5;
                return 1.0;
            case ROGUE_CULTIVATOR:
                if (category == CognitionGoal.Category.GATHER_RESOURCE) return 1.8;
                if (category == CognitionGoal.Category.FLEE)           return 1.8;
                if (category == CognitionGoal.Category.SEEKING_DAO)    return 1.5;
                if (category == CognitionGoal.Category.POLITICS)       return 0.3;
                return 1.0;
            case BEAST_KING:
                if (category == CognitionGoal.Category.DEFEND_TERRITORY) return 3.0;
                if (category == CognitionGoal.Category.KILL)            return 1.5;
                if (category == CognitionGoal.Category.GATHER_RESOURCE) return 1.4;
                return 1.0;
            case AVENGER:
                if (category == CognitionGoal.Category.KILL)         return 2.5;
                if (category == CognitionGoal.Category.POLITICS)     return 1.5;
                if (category == CognitionGoal.Category.FORGIVE)      return 0.05;
                return 1.0;
            case HEART_DEMON:
                if (category == CognitionGoal.Category.CORRUPT)      return 3.0;
                if (category == CognitionGoal.Category.DECEIVE)      return 2.0;
                if (category == CognitionGoal.Category.MEDITATE)     return 0.4;
                return 1.0;
            case HONOR:
                if (category == CognitionGoal.Category.DEFEND)       return 1.8;
                if (category == CognitionGoal.Category.KEEP_PROMISE) return 3.0;
                if (category == CognitionGoal.Category.DECEIVE)      return 0.2;
                return 1.0;
            case SWORD:
                if (category == CognitionGoal.Category.KILL)         return 1.5;
                if (category == CognitionGoal.Category.MEDITATE)     return 1.6;
                if (category == CognitionGoal.Category.SEEKING_DAO)  return 1.3;
                return 1.0;
            default:
                return baseGoalWeight;
        }
    }
}
