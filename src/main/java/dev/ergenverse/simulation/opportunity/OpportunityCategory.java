package dev.ergenverse.simulation.opportunity;

/**
 * OpportunityCategory — the six availability classifications for canon opportunities.
 *
 * <p>This is the core of the Opportunity Classification system. Every canon
 * opportunity (inheritance, treasure, technique, beast, etc.) receives exactly
 * one of these classifications. The classification determines HOW (or whether)
 * the player can obtain something related to that opportunity.
 *
 * <h2>The Six Categories</h2>
 * <p>See the {@link dev.ergenverse.simulation.opportunity package documentation}
 * for the full philosophy. Summary:
 * <ol>
 *   <li>{@link #TRANSFERABLE} — teachable / giftable / shareable</li>
 *   <li>{@link #REPLICABLE} — the principle can happen again (recipe + materials)</li>
 *   <li>{@link #SUCCESSOR} — the legacy continues (not the original, the continuation)</li>
 *   <li>{@link #PARALLEL} — a new, distinct opportunity of similar significance</li>
 *   <li>{@link #RELATIONSHIP_EXCLUSIVE} — comes from bonds, not exploration</li>
 *   <li>{@link #ABSOLUTE_UNIQUE} — truly singular; only derivatives possible</li>
 * </ol>
 *
 * <h2>Classification is Layer 2, not Layer 1</h2>
 * <p>Canon records: "Wang Lin obtained X." The Simulation decides: "How can another
 * protagonist obtain something related to X?" These concerns are NEVER mixed. The
 * classification lives here (Layer 2), not in the canon record (Layer 1).
 */
public enum OpportunityCategory {
    /**
     * Category 1 — Transferable.
     * <p>The original owner can teach, gift, or share it. Examples: techniques,
     * cultivation methods, insights, spells, crafting knowledge, formations.
     * <p>Mechanism: Relationship → Teaching → Player learns.
     * <p>This is the easiest category.
     */
    TRANSFERABLE(
        "Transferable",
        "The original owner can teach, gift, or share it.",
        "Relationship → Teaching → Player learns",
        true   // player can obtain the original (a taught copy)
    ),

    /**
     * Category 2 — Replicable.
     * <p>The original event happened once, but the underlying principle can happen
     * again. Example: a rare pill recipe. The first pill was unique because Wang Lin
     * found the ingredients, but the recipe exists, the materials exist, the refining
     * method exists. Another person can create another one.
     * <p>Mechanism: Knowledge + Materials + Conditions = New instance.
     */
    REPLICABLE(
        "Replicable",
        "The underlying principle can happen again (recipe + materials + conditions).",
        "Knowledge + Materials + Conditions = New instance",
        true   // player can obtain a new instance
    ),

    /**
     * Category 3 — Successor.
     * <p>The original object is unique, but its legacy can continue. Examples: a
     * destroyed weapon's forging method, an inheritance left by an ancient cultivator,
     * a bloodline technique. The player does not obtain THE original; they obtain
     * THE CONTINUATION. This fits cultivation extremely well — cultivators are
     * obsessed with passing down legacies.
     */
    SUCCESSOR(
        "Successor",
        "The original is unique, but its legacy can continue.",
        "Player obtains the continuation, not the original",
        false  // player obtains a successor, not the original
    ),

    /**
     * Category 4 — Parallel Opportunity.
     * <p>The most important category for the protagonist fantasy. The universe is
     * enormous; the novels only document one person's encounters. If Wang Lin found
     * Ancient God inheritance A, that does not prove it was the only possible
     * inheritance in existence. The player may discover Ancient God inheritance B —
     * with similar significance, NOT a copy, a NEW legendary opportunity.
     */
    PARALLEL(
        "Parallel Opportunity",
        "A new, distinct opportunity of similar significance exists in the unexplored world.",
        "Exploration → Discovery of a parallel opportunity",
        false  // player obtains a different opportunity, not the original
    ),

    /**
     * Category 5 — Relationship Exclusive.
     * <p>Some things are impossible to acquire through exploration. They come from
     * bonds. Wang Lin trusts you and says: "This technique helped me survive
     * countless calamities. I will pass it to you." The opportunity exists because
     * of the relationship, not because the world respawned it.
     */
    RELATIONSHIP_EXCLUSIVE(
        "Relationship Exclusive",
        "Comes from bonds, not exploration. Exists because of the relationship.",
        "Trust threshold → Protagonist offers the opportunity",
        true   // player can obtain it (via the relationship)
    ),

    /**
     * Category 6 — Absolute Unique.
     * <p>Some things should remain truly singular, otherwise the universe loses
     * meaning. Examples: the original Heaven-Defying Bead, a specific cosmic
     * authority, something tied directly to a person's Dao. The player may receive:
     * understanding, derivative techniques, partial usage, a successor artifact —
     * but NOT literally replace the original.
     */
    ABSOLUTE_UNIQUE(
        "Absolute Unique",
        "Truly singular. Only derivatives (understanding, successor artifacts) are possible.",
        "Player obtains derivatives/understanding, never the original",
        false  // player can NEVER obtain the original itself
    );

    public final String label;
    public final String description;
    public final String mechanism;
    public final boolean playerCanObtainOriginal;

    OpportunityCategory(String label, String description, String mechanism,
                        boolean playerCanObtainOriginal) {
        this.label = label;
        this.description = description;
        this.mechanism = mechanism;
        this.playerCanObtainOriginal = playerCanObtainOriginal;
    }

    /**
     * Whether this category allows the player to obtain something (original,
     * copy, successor, or parallel). Only ABSOLUTE_UNIQUE blocks all direct
     * acquisition (though derivatives are still possible).
     */
    public boolean isAcquirable() {
        return this != ABSOLUTE_UNIQUE;
    }

    /**
     * Whether acquiring this requires a protagonist relationship (TRANSFERABLE,
     * RELATIONSHIP_EXCLUSIVE) vs. world exploration (REPLICABLE, SUCCESSOR, PARALLEL).
     */
    public boolean requiresRelationship() {
        return this == TRANSFERABLE || this == RELATIONSHIP_EXCLUSIVE;
    }

    /**
     * Whether acquiring this requires world exploration / discovery.
     */
    public boolean requiresExploration() {
        return this == REPLICABLE || this == SUCCESSOR || this == PARALLEL;
    }
}
