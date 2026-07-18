/**
 * <h1>dev.ergenverse.wanglin.pet — Canonical Pet Data Model</h1>
 *
 * <p>This package contains the full canonical pet data model per the user's
 * directive (Task RI-FORGE-BEHAVIOR-DEPTH): species, cultivation, bloodline,
 * personality, growth stages, known abilities, evolution history, relationship
 * to Wang Lin, the contract/reproduction/mentorship/gift-offspring flags, a
 * canon summary of what the novels say it IS, and a list of canon-demonstrated
 * behaviors (what the novels show it DOING).
 *
 * <h2>Distinction from {@code dev.ergenverse.wanglin.registry.CanonicalPets}</h2>
 * <p>{@code CanonicalPets} is a {@code CanonicalSubRegistry} of generic
 * {@code CanonicalEntry} records — it surfaces canonical-ownership +
 * transferability verdicts (canTeach / canTransfer / canGiftEquivalent, etc.)
 * for the 30-top-level-node knowledge graph. THIS package
 * ({@code dev.ergenverse.wanglin.pet}) holds the <b>pet-specific</b> data model
 * with rich fields that don't fit the generic CanonicalEntry shape
 * (species, bloodline, growth stages, evolution history, the four pet-behavior
 * flags canContract / canReproduce / canMentor / canGiftOffspring).
 *
 * <p>The two layers are cross-referenced by ID: every CanonicalPet's
 * {@code id} matches the corresponding {@code CanonicalEntry}'s {@code id} in
 * {@code CanonicalPets}. This enables bidirectional lookups:
 * <ul>
 *   <li>pet → entry: {@code CanonicalPetRegistry.get("pet_mosquito_beast").id()}
 *       → {@code CanonicalPets.get("pet_mosquito_beast")} (transferability verdict)</li>
 *   <li>entry → pet: {@code CanonicalPets.get("pet_mosquito_beast").id()}
 *       → {@code CanonicalPetRegistry.get("pet_mosquito_beast")} (rich pet fields)</li>
 * </ul>
 *
 * <h2>Pets registered</h2>
 * <ul>
 *   <li>Mosquito Beast (蚊兽) — Rank-9 Void Mosquito; Wang Lin's signature companion</li>
 *   <li>Mosquito Swarm ×10,000 (蚊群) — Descendants of the original Mosquito Beast</li>
 *   <li>Lei Ji / Thunder Beast (雷极) — Silver-Horned Thunder Beast; from the God-Slaying War Chariot</li>
 *   <li>Nether Beast (冥兽) — Life-bound beast; civilizations on its back</li>
 *   <li>Thunder Toad (雷蟾) — Thunder-element spirit beast</li>
 *   <li>Thunder Celestial Beast (雷仙兽) — Thunder-element celestial beast</li>
 *   <li>Brilliant Void Sea Dragon (耀虚海龙) — Void-tier sea dragon</li>
 *   <li>Ancient Soul Restriction Tortoise Beast (古魂禁龟兽) — Living formation-component</li>
 *   <li>Devil Sky Cloud Monkey (魔天云猴) — Devil-tier monkey (3rd devil)</li>
 * </ul>
 *
 * <h2>PetTeachingResolver</h2>
 * <p>{@link dev.ergenverse.wanglin.pet.PetTeachingResolver} resolves whether
 * Wang Lin can gift/contract/mentor a pet for the player. The walk:
 * pet → ownership → transferability → relationship → result. This mirrors the
 * registry's {@code TeachingResolver} but is pet-specific (handles the four
 * pet-behavior flags plus the canonical transferability verdict).
 */
package dev.ergenverse.wanglin.pet;
