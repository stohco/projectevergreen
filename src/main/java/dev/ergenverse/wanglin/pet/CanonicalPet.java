package dev.ergenverse.wanglin.pet;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalPet — the full canonical record for each of Wang Lin's pets.
 *
 * <p>Per the user's directive (Task RI-FORGE-BEHAVIOR-DEPTH): every pet must be
 * decomposed into species, cultivation, bloodline, personality, growth stages,
 * known abilities, evolution history, relationship to Wang Lin, and the
 * contract/reproduction/mentorship/gift-offspring flags. Plus a canon summary
 * of what the novels say it IS and a list of canon-demonstrated behaviors
 * (what the novels show it DOING).
 *
 * <p>This record is the <b>pet-specific data model</b>. It is distinct from
 * {@code dev.ergenverse.wanglin.registry.CanonicalPets} (which is a
 * {@code CanonicalSubRegistry} of generic {@code CanonicalEntry} records). The
 * registry surfaces canonical-ownership + transferability verdicts; THIS record
 * holds rich pet-specific fields (species, bloodline, growth stages, etc.).
 *
 * @param id                        stable lowercase-kebab identifier
 * @param displayName               human-readable EN name
 * @param displayNameCn             human-readable CN name
 * @param species                   the species (e.g. "Rank-9 Void Mosquito")
 * @param bloodline                 bloodline (e.g. "Ancient God bloodline", "Beast king lineage")
 * @param cultivationRealm          current cultivation at edge of canon
 * @param personality               the beast's canon-attested personality
 * @param growthStages              documented growth stages (e.g. "Tamed → Restored (Ch. 1626)")
 * @param knownAbilities            canon-demonstrated abilities
 * @param evolutionHistory          how the pet grew/changed over the story (list of milestones)
 * @param relationshipToWangLin     relationship type (e.g. "signature companion", "life-bound")
 * @param canContract               whether the player can contract this species
 * @param canReproduce              whether the species can reproduce (swarm-breeding)
 * @param canMentor                 whether the beast can mentor the player
 * @param canGiftOffspring          whether Wang Lin can gift an offspring to the player
 * @param canonSummary              what the novels say it IS
 * @param canonDemonstratedBehaviors what the novels show it DOING (chapter-cited list)
 * @param provenance                source novel, chapters, attestation, confidence, ambiguities
 */
public record CanonicalPet(
        String id,
        String displayName,
        String displayNameCn,
        String species,
        String bloodline,
        String cultivationRealm,
        String personality,
        List<String> growthStages,
        List<String> knownAbilities,
        List<String> evolutionHistory,
        String relationshipToWangLin,
        boolean canContract,
        boolean canReproduce,
        boolean canMentor,
        boolean canGiftOffspring,
        String canonSummary,
        List<String> canonDemonstratedBehaviors,
        Provenance provenance
) {
    public CanonicalPet {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("CanonicalPet requires an id");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("CanonicalPet '" + id + "' requires a displayName");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("CanonicalPet '" + id + "' requires a Provenance");
        }
        if (displayNameCn == null) displayNameCn = "";
        if (species == null) species = "UNKNOWN";
        if (bloodline == null) bloodline = "UNKNOWN";
        if (cultivationRealm == null) cultivationRealm = "UNKNOWN";
        if (personality == null) personality = "UNKNOWN";
        if (growthStages == null) growthStages = List.of();
        if (knownAbilities == null) knownAbilities = List.of();
        if (evolutionHistory == null) evolutionHistory = List.of();
        if (relationshipToWangLin == null) relationshipToWangLin = "UNKNOWN";
        if (canonSummary == null) canonSummary = "";
        if (canonDemonstratedBehaviors == null) canonDemonstratedBehaviors = List.of();
    }
}
