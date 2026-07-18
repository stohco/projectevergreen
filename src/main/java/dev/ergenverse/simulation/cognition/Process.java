package dev.ergenverse.simulation.cognition;

/**
 * Process — 54 process types that can act on an actor.
 *
 * <p>A Process is a long-running transformation on an actor's state:
 * cultivation breakthrough, body refinement, bloodline awakening, heart-demon
 * corruption, seclusion, inheritance, disguise, blood-binding, soul-search,
 * sealing, and so on. The {@link Plan} references processes by id.
 *
 * <p>RI-specific entries: HEART_DEMON_CORRUPT, QI_DEVIATION, SOUL_DEVOUR,
 * FLESH_DEVOUR, BIRTH_SPIRIT, SOUL_IMPRINT, SECLUDE, INHERIT_DAO,
 * INHERIT_SECT, INHERIT_BLOODLINE, DISGUISE, BLOOD_BIND, SOUL_SEARCH,
 * SEAL_ENTITY.
 */
public enum Process {
    // ── Cultivation processes (8) ──
    QI_GATHERING,
    QI_CIRCULATION,
    FOUNDATION_BUILDING,
    CORE_FORMATION,
    NASCENT_SOUL_FORMATION,
    SOUL_FORMATION,
    REALM_BREAKTHROUGH,
    TRIBULATION_ENDURE,

    // ── Body / physical processes (6) ──
    BODY_REFINEMENT,
    BLOODLINE_AWAKENING,
    FLESH_DEVOUR,            // RI — flesh-devour cultivation path
    BONE_FORGING,
    MARROW_CLEANSING,
    LIFESPAN_EXTENSION,

    // ── Soul / spirit processes (6) ──
    SOUL_DEVOUR,             // RI — absorb another's soul
    SOUL_IMPRINT,            // RI — imprint soul into object/puppet
    SOUL_REFINEMENT,
    DIVINE_SENSE_EXPANSION,
    HEART_DEMON_TRIAL,
    HEART_DEMON_CORRUPT,     // RI — corruption by heart-demon

    // ── Dao processes (5) ──
    SEEKING_DAO,
    COMPREHEND_LAW,
    INHERIT_DAO,             // RI — receive Dao lineage from predecessor
    DAO_FUSION,
    DAO_HEART_TEMPERING,

    // ── Heart / mind risk processes (4) ──
    QI_DEVIATION,            // RI — qi goes wrong
    INNER_DEMON_INVASION,
    KARMA_ACCUMULATION,
    KARMA_RESOLUTION,

    // ── Inheritance / succession processes (4) ──
    INHERIT_SECT,            // RI — succeed sect leadership
    INHERIT_BLOODLINE,       // RI — bloodline inheritance memory
    INHERITANCE_MEMORY,
    LEGACY_TRANSMIT,

    // ── Seclusion / cultivation modes (3) ──
    SECLUDE,                 // RI — closed-door cultivation
    MEDITATE,
    WANDER,

    // ── Disguise / transformation processes (3) ──
    DISGUISE,                // RI — change appearance / identity
    SHAPE_SHIFT,
    POSSESS_BODY,

    // ── Binding / subordination processes (4) ──
    BLOOD_BIND,              // RI — blood-binding oath / contract
    SOUL_BIND,
    CONTRACT_FORMATION,
    MASTER_DISCIPLE_BOND,

    // ── Combat / contest processes (4) ──
    DUEL,
    AMBUSH,
    SOUL_SEARCH,             // RI — forcible soul-search technique
    KARMIC_WHIP,

    // ── Sealing / restriction processes (3) ──
    SEAL_ENTITY,             // RI — seal an entity
    FORMATION_INSCRIBE,
    RESTRICTION_INSCRIBE,

    // ── Birth / death processes (3) ──
    BIRTH_SPIRIT,            // RI — spirit birth
    REINCARNATION,
    DEATH_TRANSITION,

    // ── World-pressure response processes (1) ──
    ADAPT_TO_ENVIRONMENT;

    /**
     * Total canonical process count. Updated when new processes are added;
     * the canon audit requires 54.
     */
    public static final int CANON_COUNT = 54;
}
