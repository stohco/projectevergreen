package dev.ergenverse.graph;

/**
 * Every type of directed relationship in the Ergenverse Graph.
 *
 * <p>Organized into 11 categories. Each category corresponds to a domain
 * of interaction in the Er Gen multiverse. Engines register their own
 * edge types here; the graph does not care which engine created an edge,
 * only what type it is.
 *
 * <p><b>Canon constraint:</b> Edge types are named after in-novel concepts.
 * "KARMIC_DEBT" is a literal law of cause-and-effect in Er Gen's universe,
 * not a metaphor. "JOSS_FLAME_FLOW" is the actual mechanism by which mortal
 * faith becomes cultivation energy for the world owner.
 *
 * @see Edge  the edge record that carries these types
 * @see WorldGraph  traversal and query methods
 */
public enum EdgeType {

    // ═══════════════════════════════════════════════════════════════════
    // 1. OWNERSHIP & POSSESSION
    // "Nothing drops. Everything is part of something."
    // ═══════════════════════════════════════════════════════════════════

    /** A owns B (Wang Lin OWNS Heaven-Defying Bead). */
    OWNS,
    /** A currently carries B in their inventory. */
    CARRIES,
    /** A has B equipped (wielding, wearing). */
    EQUIPPED_WITH,
    /** A is stored inside B (item in storage ring, NPC in cave). */
    STORED_IN,
    /** A physically wields B in combat. */
    WIELDING,
    /** B was forged/crafted by A. */
    FORGED_BY,
    /** B was harvested from A (beast part, herb part). */
    HARVESTED_FROM,
    /** B contains part A (composition relationship). */
    CONTAINS_PART,
    /** A was upgraded into B via evolution chain. */
    UPGRADED_TO,
    /** B is the base form that evolved into A. */
    UPGRADED_FROM,
    /** A and B are fused into one entity. */
    FUSED_WITH,
    /** B has a soul brand imprinted by A. */
    SOUL_BRANDED_BY,
    /** A cracked B's soul brand. */
    CRACKED_BY,
    /** A was gifted B by entity C (stored in edge properties). */
    GIFTED_BY,
    /** B was lost by A (dropped, stolen, destroyed). */
    LOST_BY,
    /** B was recovered/found by A. */
    RECOVERED_BY,
    /** B was traded from A to C (counterparty in properties). */
    TRADED_FOR,
    /** B is stored in A's treasury (sect treasury). */
    TREASURY_CONTAINS,

    // ═══════════════════════════════════════════════════════════════════
    // 2. CULTIVATION & COMPREHENSION
    // "Cultivation changes understanding, not existence."
    // ═══════════════════════════════════════════════════════════════════

    /** A comprehends Dao/Essence B. */
    COMPREHENDS,
    /** A practices/uses technique B. */
    PRACTICES,
    /** A teaches technique B to C. */
    TEACHES,
    /** A inherited technique/artifact/position from B. */
    INHERITED_FROM,
    /** A created technique/art/formation B. */
    CREATED_BY,
    /** A requires B to proceed (technique requirement). */
    REQUIRES,
    /** A counters B in combat (technique/dao interaction). */
    COUNTERS,
    /** A conflicts with B (dao friction, law clash). */
    CONFLICTS_WITH,
    /** A harmonizes with B (dao resonance, law alignment). */
    HARMONIZES_WITH,
    /** A is derived from B (technique evolution). */
    DERIVED_FROM,
    /** A represents the Essence of B. */
    ESSENCE_OF,
    /** A is the True Body for Essence B. */
    TRUE_BODY_OF,
    /** A crossed Bridge B (Heaven Trampling Bridge). */
    BRIDGE_CROSSED,
    /** A devoured/absorbed thunder B (Accompanying Thunder). */
    THUNDER_DEVoured,

    // ═══════════════════════════════════════════════════════════════════
    // 3. SOCIAL & RELATIONSHIP
    // "Every interaction creates karma."
    // ═══════════════════════════════════════════════════════════════════

    /** A loves B (romantic bond). */
    LOVES,
    /** A hates B (deep enmity). */
    HATES,
    /** A fears B (power differential). */
    FEARS,
    /** A respects B (acknowledgment of strength/character). */
    RESPECTS,
    /** A is rivals with B (competitive equals). */
    RIVALS,
    /** A serves B (vassal, servant, subordinate). */
    SERVES,
    /** A was mentored by B (teacher-student). */
    MENTORED_BY,
    /** A betrayed B (broken trust). */
    BETRAYED,
    /** A is bonded with B as a dao companion. */
    DAO_COMPANION,
    /** A and B are sworn siblings (blood oath). */
    SWORN_SIBLING,
    /** A is family of B (blood or adopted). */
    FAMILY_OF,
    /** A is a disciple of B. */
    DISCIPLE_OF,
    /** A is an ally of B. */
    ALLY_OF,
    /** A is an enemy of B. */
    ENEMY_OF,
    /** A saved B's life. */
    SAVED,

    // ═══════════════════════════════════════════════════════════════════
    // 4. COSMOLOGICAL & SPATIAL
    // "The universe is nested. Lower tiers are contained by higher."
    // ═══════════════════════════════════════════════════════════════════

    /** A is physically located inside B. */
    LOCATED_IN,
    /** A is the parent cosmological node of B. */
    PARENT_OF,
    /** A sealed B (restricted a location, imprisoned an entity). */
    SEALED_BY,
    /** A is contained within sealed space B. */
    SEALED_WITHIN,
    /** A exited B via a specific method (method in properties). */
    EXITED_VIA,
    /** A traveled to B. */
    TRAVELED_TO,
    /** A was transferred to B (e.g., reincarnated to IAC). */
    TRANSFERRED_TO,
    /** A is the cosmological layer containing B. */
    COSMOLOGY_CONTAINS,

    // ═══════════════════════════════════════════════════════════════════
    // 5. KARMA & CAUSALITY
    // "Karma is a physical law, not a metaphor."
    // ═══════════════════════════════════════════════════════════════════

    /** A owes karmic debt to B (cause-and-effect obligation). */
    KARMIC_DEBT,
    /** A holds a grudge against B (unresolved negative karma). */
    GRUDGE,
    /** A feels gratitude toward B (positive karma). */
    GRATITUDE,
    /** A is karmically connected to B (visible at Nascent Soul+). */
    KARMIC_THREAD,
    /** A has a vengeance obligation toward B (binding oath). */
    VENGEANCE_OBLIGATION,
    /** A shares a bloodline with B (Ancient God/Devil/Demon). */
    BLOODLINE_OF,
    /** A is the reincarnation of B (past-life soul root). */
    REINCARNATION_OF,
    /** A is the soul root connecting to mortal avatar B (Samsara). */
    SOUL_ROOT_OF,
    /** A severed the karmic connection with B. */
    KARMA_SEVERED,
    /** A is hunting B (karmic pursuit, lightning hunters). */
    HUNTING,
    /** A has incurred heavenly enmity (weight in properties). */
    HEAVENLY_ENMITY,

    // ═══════════════════════════════════════════════════════════════════
    // 6. TEMPORAL & HISTORICAL
    // "History is the primary progression mechanism, not XP."
    // ═══════════════════════════════════════════════════════════════════

    /** Event A precedes event B in time. */
    PRECEDES,
    /** Event A was caused by event/entity B. */
    CAUSED_BY,
    /** A witnessed event B. */
    WITNESSED,
    /** A participated in event B. */
    PARTICIPATED_IN,
    /** A died in event/location B. */
    DIED_IN,
    /** A broke through to a new realm in event B. */
    BROKE_THROUGH_IN,
    /** A was born at location/time B. */
    BORN_IN,
    /** A's mortal transformation happened in B (Samsara). */
    MORTAL_TRANSFORMATION_IN,

    // ═══════════════════════════════════════════════════════════════════
    // 7. ECONOMY & TRIBUTE
    // "Joss Flame is the concrete implementation of Qiyun."
    // ═══════════════════════════════════════════════════════════════════

    /** Settlement A pays Joss Flame tribute to world owner B. */
    TRIBUTE_TO,
    /** A flows to B (Joss Flame, spiritual energy, qi). */
    ENERGY_FLOW,
    /** A trades with B (commercial relationship). */
    TRADES_WITH,
    /** A has a treaty with B (properties contain treaty terms). */
    TREATY_WITH,

    // ═══════════════════════════════════════════════════════════════════
    // 8. ECOLOGICAL
    // "The cultivation causal chain: Qi → veins → herbs → beasts → sects."
    // ═══════════════════════════════════════════════════════════════════

    /** Beast A preys on beast B (food web). */
    PREYS_ON,
    /** Species A's habitat is location B. */
    HABITAT_IN,
    /** A guards location/inheritance B. */
    GUARDS,
    /** Beast A was tamed by cultivator B. */
    TAMED_BY,
    /** A is bonded with beast B (cultivation companion). */
    BONDED_WITH_BEAST,
    /** Plant A grows in location B. */
    GROWS_IN,
    /** A pollinates/disperses B (ecological relationship). */
    POLLINATES,

    // ═══════════════════════════════════════════════════════════════════
    // 9. POSITION & AUTHORITY
    // "Certain positions are singular — only one entity can hold them."
    // ═══════════════════════════════════════════════════════════════════

    /** A holds cosmic/singular position B (e.g., Vermilion Bird Emperor). */
    HOLDS_POSITION,
    /** A competes with B for position C (stored in properties). */
    POSITION_COMPETITION,
    /** A is the owner of world/realm B. */
    OWNS_WORLD,
    /** A governs faction/location B. */
    GOVERNS,
    /** A is a member of faction B. */
    MEMBER_OF,

    // ═══════════════════════════════════════════════════════════════════
    // 10. WORLD LAW & BOUNDARY
    // "World laws are active forces that fight back."
    // ═══════════════════════════════════════════════════════════════════

    /** Location A's world law suppresses entity B's power. */
    LAW_SUPPRESSES,
    /** Location A's world law is actively hostile to entity B. */
    LAW_HOSTILE_TO,
    /** A attempted illegal exit from world B. */
    ILLEGAL_EXIT_ATTEMPT,
    /** A performed authorized exit from world B. */
    AUTHORIZED_EXIT,
    /** A is draining the dimensional mana of world B. */
    DRAINING,
    /** A is the Heaven-Will antagonist of world B. */
    HEAVEN_WILL_OF,

    // ═══════════════════════════════════════════════════════════════════
    // 11. PERCEPTION & CONCEALMENT
    // "A mortal and a Transcendent do not experience the same world."
    // ═══════════════════════════════════════════════════════════════════

    /** A perceived/concealed object B (divine sense interaction). */
    PERCEIVED,
    /** A conceals object B (spiritual camouflage). */
    CONCEALS,
    /** A is hidden from B by cultivation tier gap. */
    HIDDEN_FROM;

    /**
     * Check if this edge type represents a negative karmic relationship.
     * Used by the Karmic Thread Vector Solver to compute tribulation modifiers.
     */
    public boolean isNegativeKarma() {
        return this == GRUDGE || this == KARMIC_DEBT || this == HATES
                || this == ENEMY_OF || this == BETRAYED || this == VENGEANCE_OBLIGATION
                || this == HEAVENLY_ENMITY || this == HUNTING;
    }

    /**
     * Check if this edge type represents a positive karmic relationship.
     * Used by the Qiyun Flux Engine to compute luck modifiers.
     */
    public boolean isPositiveKarma() {
        return this == GRATITUDE || this == RESPECTS || this == SAVED
                || this == ALLY_OF || this == DAO_COMPANION || this == LOVES
                || this == FAMILY_OF || this == SWORN_SIBLING;
    }

    /**
     * Check if this edge type represents a karmic relationship (positive or negative).
     * Used by the Karmic Thread Vector Solver for graph traversal.
     */
    public boolean isKarmic() {
        return isNegativeKarma() || isPositiveKarma()
                || this == KARMIC_THREAD || this == KARMA_SEVERED
                || this == BLOODLINE_OF || this == REINCARNATION_OF
                || this == SOUL_ROOT_OF;
    }

    /**
     * Check if this edge type represents an ownership chain.
     * Used for provenance path queries (click artifact → see full lineage).
     */
    public boolean isOwnership() {
        return this == OWNS || this == CARRIES || this == EQUIPPED_WITH
                || this == FORGED_BY || this == HARVESTED_FROM || this == UPGRADED_TO
                || this == UPGRADED_FROM || this == FUSED_WITH || this == SOUL_BRANDED_BY
                || this == CRACKED_BY || this == GIFTED_BY || this == LOST_BY
                || this == RECOVERED_BY || this == TRADED_FOR || this == TREASURY_CONTAINS;
    }

    /**
     * Check if this edge type represents a cosmological boundary interaction.
     * Used by the Cosmological Boundary Transformer.
     */
    public boolean isBoundaryInteraction() {
        return this == LAW_SUPPRESSES || this == LAW_HOSTILE_TO
                || this == ILLEGAL_EXIT_ATTEMPT || this == AUTHORIZED_EXIT
                || this == DRAINING || this == EXITED_VIA || this == TRAVELED_TO
                || this == TRANSFERRED_TO;
    }
}