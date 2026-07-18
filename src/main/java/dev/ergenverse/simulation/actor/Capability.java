package dev.ergenverse.simulation.actor;

/**
 * Capability — 25 capability types that an actor may possess.
 *
 * <p>Capabilities are the things an actor can DO in the simulation: meditate,
 * fight, fly, soul-search, refine pills, inscribe formations, tame beasts,
 * teleport, disguise, devour souls, endure tribulation, and so on. An actor
 * that lacks a capability cannot pursue goals that require it (the Planner
 * filters such actions out).
 *
 * <p>Canon audit: 25 capability types.
 */
public enum Capability {
    // ── Combat (4) ──
    MELEE_COMBAT,
    RANGED_COMBAT,
    SPELL_COMBAT,
    FORMATION_COMBAT,

    // ── Cultivation (5) ──
    MEDITATE,
    BREAKTHROUGH,
    ENDURE_TRIBULATION,
    QI_GATHERING,
    BLOODLINE_REFINEMENT,

    // ── Soul / spirit (4) ──
    DIVINE_SENSE,
    SOUL_SEARCH,
    SOUL_DEVOUR,
    REINCARNATION_RECALL,

    // ── Movement (3) ──
    FLY,
    TELEPORT,
    WANDER,

    // ── Crafting (4) ──
    ALCHEMY,
    ARTIFACT_REFINING,
    FORMATION_INSCRIBE,
    TALISMAN_CRAFT,

    // ── Social (3) ──
    TAME_BEAST,
    ACCEPT_DISCIPLE,
    FOUND_SECT,

    // ── Stealth / disguise (1) ──
    DISGUISE,

    // ── Special (1, canon-RI) ──
    KARMA_MANIPULATION;

    /** Canonical count — canon audit requires 25. */
    public static final int CANON_COUNT = 25;
}
