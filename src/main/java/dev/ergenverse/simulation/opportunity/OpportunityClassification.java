package dev.ergenverse.simulation.opportunity;

import java.util.List;

/**
 * OpportunityClassification — the Layer 2 simulation classification for a canon opportunity.
 *
 * <p><b>This is SIMULATION data (Layer 2). It decides how the player can obtain
 * something related to a canon opportunity.</b> It references a
 * {@link CanonOpportunityRecord} (Layer 1) but lives in a separate conceptual
 * space — the two are never mixed.
 *
 * <h2>What this records</h2>
 * <ul>
 *   <li><b>opportunityId</b> — links to the {@link CanonOpportunityRecord}</li>
 *   <li><b>category</b> — one of the six {@link OpportunityCategory} values</li>
 *   <li><b>acquisitionRoutes</b> — the possible ways the player can obtain something
 *       related to this opportunity (e.g. "Teaching from Wang Lin at trust 70+",
 *       "Discover parallel inheritance in unexplored Ancient God cave")</li>
 *   <li><b>relationshipRequirement</b> — the minimum affinity/trust required (if any)</li>
 *   <li><b>worldConditions</b> — world-state conditions that must be true (e.g.
 *       "Player must have reached Core Formation", "Corpse Yin Sect must be destroyed")</li>
 *   <li><b>playerObtains</b> — what the player actually gets: "original", "taught_copy",
 *       "successor_artifact", "parallel_opportunity", "derivative_understanding", "nothing"</li>
 *   <li><b>notes</b> — design notes / caveats</li>
 * </ul>
 *
 * <h2>Classification is a design decision, not a canon fact</h2>
 * <p>The user's directive: "Every inheritance/treasure/opportunity should receive a
 * simulation classification." The classification is the game's DESIGN for how the
 * player accesses this content. It is NOT canon — Er Gen never wrote about
 * "opportunity categories." But it must be consistent with canon (the
 * {@link OpportunityAcquisitionPolicy} enforces this).
 *
 * <h2>Example classifications</h2>
 * <ul>
 *   <li><b>Heaven-Defying Bead</b> → {@link OpportunityCategory#ABSOLUTE_UNIQUE}.
 *       The original is Wang Lin's, fused with his primordial spirit. The player
 *       can obtain: derivative understanding, a successor artifact, partial usage
 *       via manifestation-sharing — but NOT the original bead itself.</li>
 *   <li><b>Restriction Flag technique</b> → {@link OpportunityCategory#TRANSFERABLE}.
 *       Wang Lin can teach the restriction dao. The player learns it through
 *       relationship + teaching.</li>
 *   <li><b>Tu Si Ancient God inheritance</b> → {@link OpportunityCategory#PARALLEL}.
 *       Wang Lin obtained inheritance A. The player may discover inheritance B in
 *       an unexplored Ancient God cave — a new, distinct opportunity of similar
 *       significance.</li>
 *   <li><b>Soul Lasher (pre-fusion whip)</b> → {@link OpportunityCategory#SUCCESSOR}.
 *       The original was fused into the Karma Whip. The player may obtain a
 *       successor soul-whip through their own forging path.</li>
 *   <li><b>Qing Shui's Slaughter Sword technique</b> →
 *       {@link OpportunityCategory#RELATIONSHIP_EXCLUSIVE}. Only obtainable if Wang
 *       Lin's manifestation chooses to pass it on at extraordinary trust.</li>
 * </ul>
 */
public record OpportunityClassification(
        String opportunityId,
        OpportunityCategory category,
        List<String> acquisitionRoutes,
        int relationshipRequirement,      // 0-100 affinity/trust, 0 = no relationship needed
        List<String> worldConditions,
        String playerObtains,             // "original", "taught_copy", "successor_artifact", etc.
        String notes
) {
    public OpportunityClassification {
        if (opportunityId == null || opportunityId.isBlank()) {
            throw new IllegalArgumentException("OpportunityClassification requires an opportunityId");
        }
        if (category == null) {
            throw new IllegalArgumentException("OpportunityClassification requires a category");
        }
        if (acquisitionRoutes == null || acquisitionRoutes.isEmpty()) {
            acquisitionRoutes = List.of(category.mechanism);
        }
        if (worldConditions == null) worldConditions = List.of();
        if (playerObtains == null || playerObtains.isBlank()) {
            playerObtains = category.playerCanObtainOriginal ? "original" : "derivative";
        }
        if (notes == null) notes = "";
        relationshipRequirement = Math.max(0, Math.min(100, relationshipRequirement));
    }

    /** Whether this classification requires a protagonist relationship. */
    public boolean requiresRelationship() {
        return category.requiresRelationship() || relationshipRequirement > 0;
    }

    /** Whether the player can obtain the original (or a copy/teaching of it). */
    public boolean playerObtainsOriginal() {
        return category.playerCanObtainOriginal;
    }
}
