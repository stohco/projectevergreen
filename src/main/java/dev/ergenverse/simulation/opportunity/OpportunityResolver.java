package dev.ergenverse.simulation.opportunity;

import dev.ergenverse.core.Ergenverse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpportunityResolver — the Layer 2 engine that resolves how the player can
 * obtain something related to a canon opportunity.
 *
 * <p>This is the Simulation-layer counterpart to the Canon Opportunity Registry.
 * It holds the {@link OpportunityClassification} for each canon opportunity and
 * provides the query API: "Given this opportunity, this protagonist, and the
 * player's current state (trust, world conditions), what can the player obtain?"
 *
 * <h2>The fundamental split (enforced here)</h2>
 * <ul>
 *   <li><b>Canon Opportunity Registry</b> (Layer 1): records original owner, canon
 *       event, original fate, uniqueness. Immutable.</li>
 *   <li><b>Simulation Opportunity Resolver</b> (Layer 2, this class): records
 *       possible acquisition routes, relationship requirements, world conditions,
 *       player eligibility. Design, not canon.</li>
 * </ul>
 * <p>Never mix them. This class never modifies a {@link CanonOpportunityRecord};
 * it only reads them and applies classifications.
 *
 * <h2>Resolution flow</h2>
 * <pre>
 *   1. Look up the OpportunityClassification for the opportunityId
 *   2. Check the OpportunityAcquisitionPolicy (hard gate — never rewrite canon)
 *   3. Check the ProtagonistSharingPhilosophy (would this protagonist share it?)
 *   4. Check world conditions (is the world state right?)
 *   5. Check player eligibility (cultivation tier, prerequisites)
 *   6. Return a ResolutionResult: what the player can obtain, via what route
 * </pre>
 *
 * <h2>Status</h2>
 * <p>The classification registry is being populated incrementally. Initially, a
 * default classification is inferred from the canon record's fate + tags. As
 * specific opportunities are classified by hand, they override the defaults.
 */
public final class OpportunityResolver {

    private static final Map<String, OpportunityClassification> CLASSIFICATIONS = new HashMap<>();
    private static final Map<String, CanonOpportunityRecord> CANON_RECORDS = new HashMap<>();
    private static boolean bootstrapped = false;

    private OpportunityResolver() {}

    /** Register a canon opportunity record (Layer 1 data). */
    public static void registerCanonRecord(CanonOpportunityRecord record) {
        CANON_RECORDS.put(record.opportunityId(), record);
    }

    /** Register a classification (Layer 2 data) for a canon opportunity. */
    public static void registerClassification(OpportunityClassification classification) {
        CLASSIFICATIONS.put(classification.opportunityId(), classification);
    }

    /** Get the classification for an opportunity. Returns null if not classified. */
    public static OpportunityClassification getClassification(String opportunityId) {
        if (!bootstrapped) bootstrap();
        return CLASSIFICATIONS.get(opportunityId);
    }

    /** Get the canon record for an opportunity. Returns null if not registered. */
    public static CanonOpportunityRecord getCanonRecord(String opportunityId) {
        if (!bootstrapped) bootstrap();
        return CANON_RECORDS.get(opportunityId);
    }

    /**
     * Resolve what the player can obtain for a specific opportunity, given the
     * current game state.
     *
     * @param opportunityId the canon opportunity ID
     * @param protagonistId the protagonist whose relationship is being evaluated (e.g. "wanglin")
     * @param currentTrust the player's current trust/affinity with that protagonist (0-100)
     * @param worldConditionsMet whether the required world conditions are met
     * @return a ResolutionResult describing what the player can obtain and how
     */
    public static ResolutionResult resolve(String opportunityId, String protagonistId,
                                           int currentTrust, boolean worldConditionsMet) {
        if (!bootstrapped) bootstrap();

        OpportunityClassification classification = CLASSIFICATIONS.get(opportunityId);
        if (classification == null) {
            return ResolutionResult.unclassified(opportunityId);
        }

        // Step 1: Check the policy (hard gate)
        if (!OpportunityAcquisitionPolicy.canObtainOriginal(classification)) {
            // The player cannot obtain the original. What CAN they obtain?
            String derivative = deriveWhatPlayerObtains(classification);
            if (!OpportunityAcquisitionPolicy.validateAcquisition(classification, derivative)) {
                return ResolutionResult.policyViolation(opportunityId, derivative);
            }
            // Policy allows the derivative — continue with the derivative
            return resolveDerivative(opportunityId, protagonistId, currentTrust,
                    worldConditionsMet, classification, derivative);
        }

        // Step 2: Check the protagonist sharing philosophy (if relationship required)
        if (classification.requiresRelationship()) {
            ProtagonistSharingPhilosophy philosophy = ProtagonistSharingPhilosophy.byId(protagonistId);
            if (philosophy == null) {
                return ResolutionResult.unknownProtagonist(opportunityId, protagonistId);
            }
            ProtagonistSharingPhilosophy.ShareDecision decision =
                philosophy.evaluateShare(classification, currentTrust);
            if (!decision.isPositive()) {
                return ResolutionResult.refusedByProtagonist(opportunityId, protagonistId, decision);
            }
        }

        // Step 3: Check world conditions
        if (!worldConditionsMet) {
            return ResolutionResult.worldConditionsNotMet(opportunityId, classification.worldConditions());
        }

        // Step 4: All checks passed — the player can obtain it
        return ResolutionResult.success(opportunityId, classification, classification.playerObtains());
    }

    /** Derive what the player can obtain when they cannot get the original. */
    private static String deriveWhatPlayerObtains(OpportunityClassification classification) {
        return switch (classification.category()) {
            case ABSOLUTE_UNIQUE -> "derivative_understanding";
            case SUCCESSOR -> "successor_artifact";
            case PARALLEL -> "parallel_opportunity";
            default -> classification.playerObtains();
        };
    }

    /** Resolve a derivative acquisition (for ABSOLUTE_UNIQUE, SUCCESSOR, PARALLEL). */
    private static ResolutionResult resolveDerivative(String opportunityId, String protagonistId,
                                                       int currentTrust, boolean worldConditionsMet,
                                                       OpportunityClassification classification,
                                                       String derivative) {
        // For derivatives, the relationship requirement may still apply (e.g.
        // Wang Lin must trust you to share derivative understanding of the Bead)
        if (classification.requiresRelationship() && currentTrust < classification.relationshipRequirement()) {
            return ResolutionResult.refusedByProtagonist(opportunityId, protagonistId,
                ProtagonistSharingPhilosophy.ShareDecision.REFUSES);
        }
        if (!worldConditionsMet) {
            return ResolutionResult.worldConditionsNotMet(opportunityId, classification.worldConditions());
        }
        return ResolutionResult.success(opportunityId, classification, derivative);
    }

    // ── Bootstrap: register canon records + classifications ───────────

    /**
     * Bootstrap the resolver with canon records and their classifications.
     *
     * <p>Each entry below pairs a {@link CanonOpportunityRecord} (Layer 1: canon
     * facts) with an {@link OpportunityClassification} (Layer 2: how the player
     * can obtain something related). The two are registered separately and never
     * mixed in the same data structure.
     */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        Ergenverse.LOGGER.info("[Opportunity] Bootstrapping Opportunity Classification system...");

        // ════════════════════════════════════════════════════════════════
        // SIGNATURE OPPORTUNITIES — the most iconic canon opportunities
        // ════════════════════════════════════════════════════════════════

        registerOpportunity(
            "wanglin/heaven_defying_bead",
            "Wang Lin",
            "Found in Heng Yue Sect stone bead (Ch. 8); recognized later as the Heaven-Defying Bead",
            "retained — fused with Wang Lin's primordial spirit",
            "The Heaven-Defying Bead: a transcendent artifact with the Five Elements pattern, interior spirit space with 10× time dilation, sentient and destiny-bound.",
            List.of("treasure", "absolute_unique", "transcendent", "wanglin_core"),
            OpportunityCategory.ABSOLUTE_UNIQUE,
            List.of("Derivative: Wang Lin's manifestation shares understanding of the bead's nature at deep trust",
                    "Successor: a player-refined bead-analog (if the player discovers the underlying principle)",
                    "Partial: temporary manifestation-sharing of the bead's perception during a shared trial"),
            95,  // requires extraordinary trust
            List.of("Player must have reached at least Soul Formation",
                    "Wang Lin's manifestation must be present"),
            "derivative_understanding",
            "The original Bead is fused with Wang Lin's primordial spirit — it cannot be separated or duplicated. Only derivatives are possible."
        );

        registerOpportunity(
            "wanglin/restriction_flag_technique",
            "Wang Lin",
            "Wang Lin learned restriction dao over the full narrative, reaching 99,999 restrictions mastery",
            "retained — Wang Lin's signature restriction mastery",
            "The Restriction Flag technique: Wang Lin's path to 99,999 restrictions, the peak of restriction dao.",
            List.of("technique", "transferable", "dao_mastery"),
            OpportunityCategory.TRANSFERABLE,
            List.of("Teaching: Wang Lin's manifestation teaches the restriction dao at high trust",
                    "Insight: Wang Lin shares comprehension fragments during shared meditation"),
            70,  // Wang Lin is cautious but will teach survival/dao knowledge
            List.of("Player must have reached at least Nascent Soul",
                    "Player must have demonstrated restriction affinity"),
            "taught_copy",
            "The restriction dao is knowledge — Wang Lin can teach it. The player learns their own path to 99,999."
        );

        registerOpportunity(
            "wanglin/tu_si_ancient_god_inheritance",
            "Wang Lin",
            "Wang Lin inherited Ancient God Tu Si's legacy (Ch. 941+)",
            "consumed — Wang Lin absorbed the inheritance",
            "Tu Si's Ancient God inheritance: the legacy of a dead Ancient God, including the God-Slaying Spear (illusory) and Ancient God Trident.",
            List.of("inheritance", "ancient_god", "consumed"),
            OpportunityCategory.PARALLEL,
            List.of("Parallel: the player may discover a DIFFERENT Ancient God inheritance in an unexplored cave",
                    "Successor: Tu Si's forging method (if recorded) could be rediscovered",
                    "The consumed original is gone — that is canon, immutable"),
            0,  // no relationship required for a parallel discovery
            List.of("Player must explore an unexplored Ancient God ruin",
                    "The ruin must NOT be Tu Si's (that was consumed by Wang Lin)"),
            "parallel_opportunity",
            "Wang Lin's inheritance is consumed. The player discovers their OWN Ancient God inheritance — a new, distinct opportunity."
        );

        registerOpportunity(
            "wanglin/karma_whip_technique",
            "Wang Lin",
            "Wang Lin fused Soul Lasher + Karma Domain into the Karma Whip (Ch. 731)",
            "retained — the Karma Whip is Wang Lin's signature weapon",
            "The Karma Whip: weaponizes karmic cause-effect. Cleaved 7 million worlds with a single strike.",
            List.of("treasure", "weapon", "relationship_exclusive"),
            OpportunityCategory.RELATIONSHIP_EXCLUSIVE,
            List.of("Exact copy: Wang Lin's manifestation creates an exact duplicate at extraordinary trust",
                    "The original remains Wang Lin's; the player receives an identical copy"),
            95,
            List.of("Player must have comprehended Karma Domain",
                    "Wang Lin's manifestation must be present"),
            "exact_copy",
            "The Karma Whip is tied to Wang Lin's Karma Domain. Through extraordinary trust, he can duplicate it for the player."
        );

        registerOpportunity(
            "wanglin/soul_lasher_pre_fusion",
            "Red Butterfly (won by Wang Lin)",
            "Red Butterfly wielded the Soul Lasher; Wang Lin won it after her death",
            "fused into Karma Whip (Ch. 731) — the Soul Lasher no longer exists as a separate item",
            "The Soul Lasher: a whip that attacks the origin soul at warp speed. Originally Red Butterfly's, then Wang Lin's, then fused into the Karma Whip.",
            List.of("treasure", "weapon", "fused", "successor"),
            OpportunityCategory.SUCCESSOR,
            List.of("Successor: the player may forge their OWN soul-whip through a parallel path",
                    "The original is gone — fused into the Karma Whip. That is canon."),
            0,
            List.of("Player must acquire soul-forging knowledge",
                    "Player must obtain whip-forging materials"),
            "successor_artifact",
            "The original Soul Lasher is fused into the Karma Whip. The player forges their own successor."
        );

        registerOpportunity(
            "wanglin/qing_shui_slaughter_technique",
            "Immortal Lord Qing Shui",
            "Qing Shui shared the Slaughter Sword technique with Wang Lin via the Sky-Gate vortex",
            "passed to Wang Lin — part of Wang Lin's Slaughter Origin",
            "Qing Shui's Slaughter Sword technique: the dao of slaughter, shared with Wang Lin.",
            List.of("technique", "slaughter", "relationship_exclusive"),
            OpportunityCategory.TRANSFERABLE,
            List.of("Teaching: Wang Lin's manifestation teaches the Slaughter Sword comprehension at high trust",
                    "Wang Lin received it from Qing Shui; he can pass it on"),
            80,
            List.of("Player must have reached at least Soul Transformation",
                    "Player must have demonstrated slaughter dao affinity"),
            "taught_copy",
            "The Slaughter technique is knowledge that was shared with Wang Lin. He can share it further."
        );

        registerOpportunity(
            "wanglin/god_slaying_sword_original",
            "Wang Lin",
            "Wang Lin's primary celestial sword",
            "destroyed Ch. 1273 (first Daoist Water fight) → restored later",
            "The God-Slaying Sword: Wang Lin's primary celestial weapon, destroyed by Daoist Water and later restored.",
            List.of("treasure", "weapon", "destroyed_restored"),
            OpportunityCategory.PARALLEL,
            List.of("Parallel: the player may discover their OWN celestial sword through exploration",
                    "The restored original remains Wang Lin's; the player finds their own"),
            0,
            List.of("Player must explore a celestial ruin or inheritance cave"),
            "parallel_opportunity",
            "The original was destroyed and restored — that history is Wang Lin's. The player finds their own sword."
        );

        registerOpportunity(
            "wanglin/mosquito_beast",
            "Wang Lin",
            "Wang Lin tamed the mosquito beast in the Sea of Devils",
            "retained — Wang Lin's signature flying mount (2nd & 3rd pairs destroyed Ch. 1276, restored Ch. 1626)",
            "The Mosquito Beast: Wang Lin's iconic flying mount, tamed from the Sea of Devils.",
            List.of("pet", "beast", "transferable"),
            OpportunityCategory.REPLICABLE,
            List.of("Same species: the player may find and tame their OWN mosquito beast in the Sea of Devils",
                    "Descendant: a descendant of Wang Lin's mosquito beast may be obtainable at high trust",
                    "Contract: through relationship, Wang Lin may facilitate a contract with a wild mosquito beast"),
            30,  // Wang Lin will help with beast contracting at moderate trust
            List.of("Player must travel to the Sea of Devils (or a parallel devil-sea region)"),
            "parallel_opportunity",
            "The original mosquito beast is Wang Lin's. The player tames their own — a new beast with its own history."
        );

        Ergenverse.LOGGER.info("[Opportunity] Bootstrapped: {} canon records, {} classifications.",
            CANON_RECORDS.size(), CLASSIFICATIONS.size());
        Ergenverse.LOGGER.info("[Opportunity] {}", OpportunityAcquisitionPolicy.assertionLine());
        bootstrapped = true;
    }

    /** Helper: register a canon record + its classification together. */
    private static void registerOpportunity(String id, String owner, String event, String fate,
                                            String description, List<String> tags,
                                            OpportunityCategory category,
                                            List<String> routes, int trustReq,
                                            List<String> conditions, String playerObtains,
                                            String notes) {
        CanonOpportunityRecord canon = new CanonOpportunityRecord(id, owner, event, fate,
            dev.ergenverse.canon.Provenance.explicit("Renegade Immortal", List.of("various"), 5,
                "Classified per the Opportunity Classification system (Layer 2 simulation design)."),
            description, tags);
        registerCanonRecord(canon);

        OpportunityClassification classification = new OpportunityClassification(id, category,
            routes, trustReq, conditions, playerObtains, notes);
        registerClassification(classification);
    }

    // ── ResolutionResult ──────────────────────────────────────────────

    /**
     * The result of an opportunity resolution. Describes what the player can
     * obtain, via what route, and why (or why not).
     */
    public record ResolutionResult(
            String opportunityId,
            Status status,
            String playerObtains,
            String route,
            String reason
    ) {
        public enum Status {
            SUCCESS("The player can obtain this opportunity."),
            UNCLASSIFIED("This opportunity has not been classified yet."),
            POLICY_VIOLATION("The proposed acquisition violates the Opportunity Acquisition Policy."),
            REFUSED_BY_PROTAGONIST("The protagonist refuses to share this (trust too low or item is never-shared)."),
            WORLD_CONDITIONS_NOT_MET("The required world conditions are not met."),
            UNKNOWN_PROTAGONIST("The specified protagonist has no sharing philosophy registered.");

            public final String description;
            Status(String description) { this.description = description; }

            public boolean isSuccess() { return this == SUCCESS; }
        }

        public static ResolutionResult success(String id, OpportunityClassification c, String obtains) {
            return new ResolutionResult(id, Status.SUCCESS, obtains,
                c.acquisitionRoutes().isEmpty() ? c.category().mechanism : c.acquisitionRoutes().get(0),
                c.category().description);
        }
        public static ResolutionResult unclassified(String id) {
            return new ResolutionResult(id, Status.UNCLASSIFIED, null, null, "No classification registered.");
        }
        public static ResolutionResult policyViolation(String id, String attempted) {
            return new ResolutionResult(id, Status.POLICY_VIOLATION, null, null,
                "Acquiring '" + attempted + "' would violate the policy: never rewrite canonical ownership.");
        }
        public static ResolutionResult refusedByProtagonist(String id, String prot,
                                                            ProtagonistSharingPhilosophy.ShareDecision decision) {
            return new ResolutionResult(id, Status.REFUSED_BY_PROTAGONIST, null, null,
                prot + " refuses: " + decision.description);
        }
        public static ResolutionResult worldConditionsNotMet(String id, List<String> conditions) {
            return new ResolutionResult(id, Status.WORLD_CONDITIONS_NOT_MET, null, null,
                "Required conditions not met: " + String.join("; ", conditions));
        }
        public static ResolutionResult unknownProtagonist(String id, String prot) {
            return new ResolutionResult(id, Status.UNKNOWN_PROTAGONIST, null, null,
                "No sharing philosophy registered for: " + prot);
        }
    }
}
