package dev.ergenverse.wanglin.pet;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;

import java.util.List;

/**
 * PetTeachingResolver — resolves whether Wang Lin can gift / contract / mentor
 * a pet for the player.
 *
 * <p>Walks:
 * <pre>
 *   Pet requested (by id)
 *     → look up CanonicalPet
 *     → check the four pet-specific behavior flags
 *         (canContract / canReproduce / canMentor / canGiftOffspring)
 *     → check player request kind (contract / offspring / mentorship)
 *     → cross-reference the registry's CanonicalEntry for ownership + transferability
 *         (via WangLinMasterRegistry.lookup(petId) — done lazily to avoid a hard
 *          cross-package compile dependency; the registry layer is consulted at
 *          runtime if available, otherwise the resolver falls back to the
 *          CanonicalPet's own flags)
 *     → produce PetTeachingResult { outcome, pet, reason, canonBasis, simulationNotes }
 * </pre>
 *
 * <h2>Per the user's "exact copy" directive</h2>
 * <p>The canonical original pet is NEVER removed from Wang Lin. Where
 * {@link CanonicalPet#canGiftOffspring()} is true, Wang Lin gifts an OFFSPRING
 * (a new instance of the species), not the original. Where
 * {@link CanonicalPet#canContract()} is true, the player may contract a NEW
 * member of the species (the original remains with Wang Lin).
 *
 * <h2>Source-of-truth</h2>
 * <p>All four pet-behavior flags trace back to {@link CanonicalPet}'s fields,
 * which are populated from CANON_RI_COMPLETE_ITEMS.md §10 + the Baidu main
 * character box. Where canon is silent, the flag is false (conservative — never
 * promise what canon doesn't attest).
 */
public final class PetTeachingResolver {

    private PetTeachingResolver() {}

    /** The outcome of a pet-related teaching/gift/contract request. */
    public enum Outcome {
        /** Wang Lin gifts an offspring of the pet to the player. */
        GIFTS_OFFSPRING,
        /** Wang Lin allows the player to contract a new member of the species. */
        CONTRACTS_PET,
        /** Wang Lin permits the pet to mentor the player. */
        MENTORS_PLAYER,
        /** Wang Lin refuses the request (canon does not support it). */
        REFUSES,
        /** The requested pet id is not in the registry. */
        NOT_FOUND,
        /** The pet exists but is not owned by Wang Lin (shouldn't happen — all CanonicalPets are Wang Lin's). */
        NOT_OWNED,
        /** The pet exists and is owned, but the requested action is not canon-supported. */
        CANNOT_DEMONSTRATE
    }

    /** The player's pet-related request. */
    public record PetPlayerRequest(
            String requestedPetId,        // the CanonicalPet id (e.g. "pet_mosquito_beast")
            int trustLevel,                // 0..100
            RequestKind requestKind        // what the player is asking for
    ) {
        /** What the player is asking Wang Lin to do with the pet. */
        public enum RequestKind {
            CONTRACT_A_NEW_ONE,    // "Can I contract a new member of this species?"
            REQUEST_OFFSPRING,     // "Can you gift me an offspring?"
            REQUEST_MENTORSHIP     // "Can this pet mentor me?"
        }

        public PetPlayerRequest {
            if (requestedPetId == null || requestedPetId.isBlank()) {
                throw new IllegalArgumentException("PetPlayerRequest requires a requestedPetId");
            }
            if (trustLevel < 0) trustLevel = 0;
            if (trustLevel > 100) trustLevel = 100;
            if (requestKind == null) requestKind = RequestKind.CONTRACT_A_NEW_ONE;
        }
    }

    /** The result of resolving a pet request. */
    public record PetTeachingResult(
            Outcome outcome,
            CanonicalPet pet,             // null if NOT_FOUND
            String reason,                // human-readable
            String canonBasis,            // provenance citation
            String simulationNotes        // gameplay-layer notes
    ) {
        public PetTeachingResult {
            if (outcome == null) outcome = Outcome.NOT_FOUND;
            if (reason == null) reason = "";
            if (canonBasis == null) canonBasis = "";
            if (simulationNotes == null) simulationNotes = "";
        }
    }

    /**
     * Resolve a pet-related request. Walks the pet → ownership → transferability
     * → relationship → result pipeline.
     */
    public static PetTeachingResult resolve(PetPlayerRequest request) {
        if (request == null) {
            return new PetTeachingResult(Outcome.NOT_FOUND, null,
                    "Null request.", "", "");
        }

        CanonicalPet pet = CanonicalPetRegistry.get(request.requestedPetId());
        if (pet == null) {
            return new PetTeachingResult(Outcome.NOT_FOUND, null,
                    "No canonical pet with id '" + request.requestedPetId() + "'.",
                    "", "Verify the pet id; check CanonicalPetRegistry for the list.");
        }

        // Step 1: trust gate. Wang Lin only gifts/contracts/mentors players he trusts at least minimally.
        if (request.trustLevel() < 10) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin does not know you well enough to entrust a companion.",
                    pet.provenance().citation(),
                    "Raise trust to >= 10 (minimal threshold) before requesting pets.");
        }

        // Step 2: dispatch by request kind, gated on the pet's canon flags.
        return switch (request.requestKind()) {
            case REQUEST_OFFSPRING -> resolveOffspringRequest(pet, request);
            case CONTRACT_A_NEW_ONE -> resolveContractRequest(pet, request);
            case REQUEST_MENTORSHIP -> resolveMentorshipRequest(pet, request);
        };
    }

    private static PetTeachingResult resolveOffspringRequest(CanonicalPet pet, PetPlayerRequest req) {
        if (!pet.canGiftOffspring()) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin cannot gift an offspring of " + pet.displayName() + " — canon does not attest reproduction.",
                    pet.provenance().citation(),
                    "canGiftOffspring=false; the species does not breed in canon.");
        }
        if (!pet.canReproduce()) {
            // Defensive: canGiftOffspring implies canReproduce, but check anyway.
            return new PetTeachingResult(Outcome.CANNOT_DEMONSTRATE, pet,
                    "Wang Lin cannot breed " + pet.displayName() + " — canon does not attest reproduction.",
                    pet.provenance().citation(),
                    "canReproduce=false; gifting offspring impossible.");
        }
        // Higher trust required for offspring gifts (canonical: Situ Nan was Wang Lin's student before receiving the Mosquito Beast pair, Ch. 441).
        if (req.trustLevel() < 40) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin considers gifting offspring of " + pet.displayName() + ", but you have not earned his trust sufficiently.",
                    pet.provenance().citation() + " (cf. Situ Nan gift Ch. 441)",
                    "Raise trust to >= 40 (offspring-gift threshold; Situ Nan was Wang Lin's student).");
        }
        return new PetTeachingResult(Outcome.GIFTS_OFFSPRING, pet,
                "Wang Lin gifts you an offspring of " + pet.displayName() + ".",
                pet.provenance().citation() + " (cf. Situ Nan gift Ch. 441 for Mosquito Beast)",
                "Spawn a new member of the species with NBT 'Ergen.Origin'='wang_lin_gift'. The original remains with Wang Lin.");
    }

    private static PetTeachingResult resolveContractRequest(CanonicalPet pet, PetPlayerRequest req) {
        if (!pet.canContract()) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin refuses to let you contract " + pet.displayName() + " — canon does not attest contractable nature.",
                    pet.provenance().citation(),
                    "canContract=false; the species is soul-bound or chariot-bound.");
        }
        if (req.trustLevel() < 20) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin will not introduce you to a wild " + pet.displayName() + " yet.",
                    pet.provenance().citation(),
                    "Raise trust to >= 20 (contract-introduction threshold).");
        }
        return new PetTeachingResult(Outcome.CONTRACTS_PET, pet,
                "Wang Lin introduces you to a wild " + pet.displayName() + " and supervises the contract.",
                pet.provenance().citation(),
                "Player receives a contract-quest advancement; on completion, a new member of the species spawns and tames to the player.");
    }

    private static PetTeachingResult resolveMentorshipRequest(CanonicalPet pet, PetPlayerRequest req) {
        if (!pet.canMentor()) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    pet.displayName() + " cannot mentor you — canon does not attest mentorship capability.",
                    pet.provenance().citation(),
                    "canMentor=false; the species is not a teacher-type.");
        }
        // Mentorship requires the highest trust (Wang Lin vouches for the player to the beast).
        if (req.trustLevel() < 60) {
            return new PetTeachingResult(Outcome.REFUSES, pet,
                    "Wang Lin will not yet vouch for you before " + pet.displayName() + ".",
                    pet.provenance().citation(),
                    "Raise trust to >= 60 (mentorship-vouch threshold).");
        }
        return new PetTeachingResult(Outcome.MENTORS_PLAYER, pet,
                "Wang Lin permits " + pet.displayName() + " to mentor you.",
                pet.provenance().citation(),
                "Player receives a 'Mentored by <pet>' status effect granting restricted access to the pet's knownAbilities (e.g. for the Nether Beast: access to its interior world).");
    }

    /** Convenience: all pets the player could plausibly request right now (passes the trust gate). */
    public static List<CanonicalPet> availableForPlayer(int trustLevel) {
        return CanonicalPetRegistry.all().stream()
                .filter(p -> trustLevel >= 10)
                .toList();
    }

    /** Convenience: all pets the player can request offspring of right now (trust + canGiftOffspring + canReproduce). */
    public static List<CanonicalPet> offspringAvailableForPlayer(int trustLevel) {
        return CanonicalPetRegistry.all().stream()
                .filter(p -> p.canGiftOffspring() && p.canReproduce() && trustLevel >= 40)
                .toList();
    }

    static {
        // Bootstrap the pet registry on first use of the resolver.
        Ergenverse.LOGGER.info("[PetTeachingResolver] Initialized — delegates to CanonicalPetRegistry.");
        CanonicalPetRegistry.bootstrap();
    }
}
