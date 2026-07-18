package dev.ergenverse.simulation.opportunity;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonOpportunityRecord — Layer 1 record of canon facts about an opportunity.
 *
 * <p><b>This is CANON data (Layer 1). It records only what Er Gen wrote.</b>
 * It does NOT contain any classification — that is the Simulation layer's job
 * ({@link OpportunityClassification}).
 *
 * <h2>What this records</h2>
 * <ul>
 *   <li><b>opportunityId</b> — stable identifier (e.g. "wanglin/heaven_defying_bead")</li>
 *   <li><b>originalOwner</b> — who canonically obtained/owned it (e.g. "Wang Lin")</li>
 *   <li><b>canonEvent</b> — the narrative event when it was obtained (e.g. "Found in Heng Yue Sect stone bead, Ch. 8")</li>
 *   <li><b>originalFate</b> — what happened to it (e.g. "retained to end", "destroyed Ch. 1273", "fused into Karma Whip")</li>
 *   <li><b>provenance</b> — source novel, chapters, EXPLICIT/INFERRED, confidence, ambiguities</li>
 *   <li><b>canonDescription</b> — what the opportunity IS (the canon summary)</li>
 * </ul>
 *
 * <h2>What this does NOT record</h2>
 * <ul>
 *   <li>How the player can obtain it (that is {@link OpportunityClassification}, Layer 2).</li>
 *   <li>Acquisition routes, relationship requirements, world conditions (all Layer 2).</li>
 *   <li>Any classification category (Layer 2).</li>
 * </ul>
 *
 * <h2>The fundamental split</h2>
 * <blockquote>
 *   Canon records: "Wang Lin obtained X."<br>
 *   Simulation decides: "How can another protagonist obtain something related to X?"
 * </blockquote>
 * <p>These two concerns are NEVER mixed. This class is the canon concern.
 * {@link OpportunityClassification} is the simulation concern.
 *
 * <h2>Immutability</h2>
 * <p>This record is immutable. Once a canon opportunity is recorded, it is NEVER
 * modified. The {@link OpportunityAcquisitionPolicy} enforces: "The simulation may
 * never rewrite canonical ownership."
 */
public record CanonOpportunityRecord(
        String opportunityId,
        String originalOwner,
        String canonEvent,
        String originalFate,
        Provenance provenance,
        String canonDescription,
        List<String> tags         // e.g. ["technique", "inheritance", "ancient_god"]
) {
    public CanonOpportunityRecord {
        if (opportunityId == null || opportunityId.isBlank()) {
            throw new IllegalArgumentException("CanonOpportunityRecord requires an opportunityId");
        }
        if (originalOwner == null || originalOwner.isBlank()) {
            throw new IllegalArgumentException("CanonOpportunityRecord requires an originalOwner");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("CanonOpportunityRecord requires a Provenance");
        }
        if (canonEvent == null) canonEvent = "";
        if (originalFate == null) originalFate = "";
        if (canonDescription == null) canonDescription = "";
        if (tags == null) tags = List.of();
    }

    /** Whether the original was retained by its owner to the end of canon. */
    public boolean wasRetained() {
        return originalFate.toLowerCase().contains("retain")
            || originalFate.toLowerCase().contains("kept")
            || originalFate.toLowerCase().contains("fused with primordial");
    }

    /** Whether the original was destroyed, consumed, or lost. */
    public boolean wasDestroyed() {
        return originalFate.toLowerCase().contains("destroy")
            || originalFate.toLowerCase().contains("consumed")
            || originalFate.toLowerCase().contains("lost")
            || originalFate.toLowerCase().contains("fused into");
    }

    /** Whether the original was given away or sold. */
    public boolean wasGivenAway() {
        return originalFate.toLowerCase().contains("given")
            || originalFate.toLowerCase().contains("sold")
            || originalFate.toLowerCase().contains("gifted");
    }
}
