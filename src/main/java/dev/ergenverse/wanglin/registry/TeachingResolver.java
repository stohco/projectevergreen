package dev.ergenverse.wanglin.registry;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.ai.WangLinPersonality;
import dev.ergenverse.wanglin.ai.WangLinRelationships;
import dev.ergenverse.wanglin.ai.WangLinTeachingPolicy;

import java.util.List;
import java.util.Set;

/**
 * TeachingResolver — the pipeline the user demanded.
 *
 * <p>Walks:
 * <pre>
 *   Technique requested
 *     → look up CanonicalEntry
 *     → check OwnershipState (must be WANG_LIN_OWNED or INHERITED_THEN_LOST)
 *     → check Transferability.canTeach
 *     → evaluate WangLinPersonality (mood, current goal — via existing WangLinTeachingPolicy)
 *     → evaluate WangLinRelationships (player affinity, trust level — via existing PlayerRequest)
 *     → produce TeachingResult { outcome, reason, canonBasis, simulationNotes }
 * </pre>
 *
 * <h2>Delegation</h2>
 * <p>The resolver delegates canon-grounded teaching decisions to the existing
 * {@link WangLinTeachingPolicy#canTeach} engine, which already implements the
 * four canon gates:
 * <ul>
 *   <li>Gate 1 — Heaven-Defying Bead's core mysteries (UNIQUELY_BOUND, always REFUSES)</li>
 *   <li>Gate 2 — Situ Nan's contract (Underworld Ascension Method)</li>
 *   <li>Gate 3 — Restriction Mountain patience trial (Restriction Flag art)</li>
 *   <li>Gate 4 — "reminds him of his younger self" open door (life-lesson teaching)</li>
 *   <li>Gate 5 — Trust + virtue gate (default)</li>
 * </ul>
 * The resolver wraps the policy's result with the {@link CanonicalEntry}'s
 * provenance + simulation notes.
 *
 * <h2>Per the user's "exact copy" directive</h2>
 * <p>If the player wants an exact copy of a treasure (not just teaching),
 * the resolver checks {@link Transferability#canGiftEquivalent()}. If it's
 * {@code YES_EXACT_COPY}, the resolver returns a {@link TeachingResult} with
 * outcome {@code GRANTS_EXACT_COPY}. The canonical original is never removed
 * from Wang Lin (per worklog Task RI-BIBLE-wiki-research + exact-copy-fix).
 */
public final class TeachingResolver {

    /** The outcome of a teaching/copy request. */
    public enum Outcome {
        WILL_TEACH,
        REFUSES,
        OFFERS_PARTIAL,
        TESTS_FIRST,
        GRANTS_EXACT_COPY,
        GRANTS_DERIVED_EQUIVALENT,
        NOT_FOUND,
        NOT_OWNED,
        CANNOT_DEMONSTRATE
    }

    /** The player's request — what they want and who they are to Wang Lin. */
    public record PlayerRequest(
            String requestedId,                                  // the CanonicalEntry id
            int trustLevel,                                       // 0..100
            Set<WangLinTeachingPolicy.RecognizedVirtue> virtues,
            boolean hasSituNanPermission,
            boolean remindsHimOfYoungerSelf,
            boolean hasPassedRestrictionTrial,
            boolean wantsExactCopy                                // true: requesting a copy of a treasure; false: requesting teaching of a technique
    ) {
        public PlayerRequest {
            if (trustLevel < 0) trustLevel = 0;
            if (trustLevel > 100) trustLevel = 100;
            if (virtues == null) virtues = Set.of();
        }

        /** Convert to the WangLinTeachingPolicy.PlayerRequest format. */
        public WangLinTeachingPolicy.PlayerRequest toPolicyRequest() {
            return new WangLinTeachingPolicy.PlayerRequest(
                    trustLevel, virtues, hasSituNanPermission,
                    remindsHimOfYoungerSelf, hasPassedRestrictionTrial);
        }
    }

    /** The result of resolving a request. */
    public record TeachingResult(
            Outcome outcome,
            CanonicalEntry entry,                                 // null if NOT_FOUND
            String reason,                                        // human-readable
            String canonBasis,                                    // provenance citation
            String simulationNotes                                // gameplay-layer notes
    ) {
        public TeachingResult {
            if (outcome == null) outcome = Outcome.NOT_FOUND;
            if (reason == null) reason = "";
            if (canonBasis == null) canonBasis = "";
            if (simulationNotes == null) simulationNotes = "";
        }
    }

    /**
     * Resolve a player's request.
     *
     * @param request the player's request
     * @return the resolved result
     */
    public TeachingResult resolve(PlayerRequest request) {
        if (request == null || request.requestedId() == null) {
            return new TeachingResult(Outcome.NOT_FOUND, null,
                    "No request made.", "", "");
        }

        // Step 1: Look up CanonicalEntry across all sub-registries
        CanonicalEntry entry = WangLinMasterRegistry.lookup(request.requestedId());
        if (entry == null) {
            return new TeachingResult(Outcome.NOT_FOUND, null,
                    "Wang Lin does not know of a canonical entity called '"
                            + request.requestedId() + "'.", "", "");
        }

        // Step 2: Ownership gate — must be OWNED, FORMERLY_OWNED, or INHERITED_THEN_LOST
        if (!entry.passesOwnershipGate()) {
            return new TeachingResult(Outcome.NOT_OWNED, entry,
                    "Wang Lin never owned '" + entry.displayName() + "'. He "
                            + entry.ownership().description.toLowerCase() + ".",
                    entry.provenance().citation(),
                    "Ownership state: " + entry.ownership() + " — ineligible for teaching.");
        }

        // Step 3: Transferability.canTeach gate
        Transferability.CanTeach canTeach = entry.transferability().canTeach();
        if (canTeach == Transferability.CanTeach.CANNOT_TEACH) {
            return new TeachingResult(Outcome.CANNOT_DEMONSTRATE, entry,
                    "Wang Lin cannot demonstrate '" + entry.displayName() + "'. "
                            + entry.demonstrability().description + ".",
                    entry.provenance().citation(),
                    "Demonstrability: " + entry.demonstrability());
        }

        // Step 4: If the player wants an exact copy, check canGiftEquivalent
        if (request.wantsExactCopy()) {
            Transferability.CanGiftEquivalent gift = entry.transferability().canGiftEquivalent();
            switch (gift) {
                case YES_EXACT_COPY -> {
                    return new TeachingResult(Outcome.GRANTS_EXACT_COPY, entry,
                            "Wang Lin nods. He places an exact copy of '"
                                    + entry.displayName() + "' in your hands. His own remains.",
                            entry.provenance().citation(),
                            "Exact-copy directive: canonical original preserved; player receives duplicate.");
                }
                case YES_DERIVED_EQUIVALENT -> {
                    return new TeachingResult(Outcome.GRANTS_DERIVED_EQUIVALENT, entry,
                            "Wang Lin cannot give you the original '" + entry.displayName()
                                    + "', but he forges you a derived equivalent.",
                            entry.provenance().citation(),
                            "Derived-equivalent path — original preserved; player receives functional copy.");
                }
                case UNIQUELY_BOUND, NO, AT_AFFINITY_LEVEL_X -> {
                    return new TeachingResult(Outcome.REFUSES, entry,
                            "Wang Lin's '" + entry.displayName() + "' cannot be duplicated. "
                                    + entry.transferability().canGiftEquivalent(),
                            entry.provenance().citation(),
                            "Gift-verdict: " + gift + " — exact-copy directive honored by refusal.");
                }
            }
        }

        // Step 5: Delegate to WangLinTeachingPolicy for the canon-grounded decision
        // The policy walks: bead-binding → Situ Nan contract → Restriction trial → open door → trust+virtue
        WangLinTeachingPolicy.TeachingResult policyResult = WangLinTeachingPolicy.canTeach(
                request.toPolicyRequest(), request.requestedId());

        // Step 6: Translate the policy's decision to our Outcome, attach canon basis + simulation notes
        return switch (policyResult.decision()) {
            case WILL_TEACH -> new TeachingResult(Outcome.WILL_TEACH, entry,
                    policyResult.reason(),
                    entry.provenance().citation() + " | " + policyResult.offering().provenance().citation(),
                    "All four canon gates passed. Player may learn " + entry.displayName() + ".");
            case REFUSES -> new TeachingResult(Outcome.REFUSES, entry,
                    policyResult.reason(),
                    entry.provenance().citation(),
                    "Refusal is canon-faithful. Original remains with Wang Lin.");
            case OFFERS_PARTIAL -> new TeachingResult(Outcome.OFFERS_PARTIAL, entry,
                    policyResult.reason() + (policyResult.partialNote().isBlank() ? "" : " " + policyResult.partialNote()),
                    entry.provenance().citation(),
                    "Partial form offered — full subject remains beyond reach.");
            case TESTS_FIRST -> new TeachingResult(Outcome.TESTS_FIRST, entry,
                    policyResult.reason(),
                    entry.provenance().citation(),
                    "Trial gate active — player must pass a Restriction-Mountain-style patience trial.");
        };
    }

    /**
     * Convenience: resolve by id with default player request (trust 0, no virtues).
     * Used for debug / admin queries.
     */
    public TeachingResult resolve(String id) {
        return resolve(new PlayerRequest(id, 0, Set.of(), false, false, false, false));
    }

    /**
     * List all entries Wang Lin is willing to teach the given player.
     * Useful for UI: "What can Wang Lin teach me right now?"
     */
    public List<CanonicalEntry> teachableNow(PlayerRequest baseRequest) {
        return WangLinMasterRegistry.INVENTORY.all().stream()
                .filter(e -> resolve(new PlayerRequest(e.id(), baseRequest.trustLevel(),
                        baseRequest.virtues(), baseRequest.hasSituNanPermission(),
                        baseRequest.remindsHimOfYoungerSelf(), baseRequest.hasPassedRestrictionTrial(),
                        false)).outcome() == Outcome.WILL_TEACH)
                .toList();
    }

    /** Static convenience — bootstrap-checked access. */
    public static TeachingResolver instance() {
        if (WangLinMasterRegistry.TEACHING_RESOLVER == null) {
            WangLinMasterRegistry.bootstrap();
        }
        return WangLinMasterRegistry.TEACHING_RESOLVER;
    }

    /** One-time static initializer hook (no-op if already bootstrapped). */
    public static void ensureBootstrapped() {
        WangLinMasterRegistry.bootstrap();
        // Touch WangLinPersonality to ensure it's also bootstrapped (for mood queries)
        WangLinPersonality.bootstrap();
        Ergenverse.LOGGER.info("[TeachingResolver] Ready. {} relationships available.",
                WangLinRelationships.ALL_RELATIONSHIPS.size());
    }
}
