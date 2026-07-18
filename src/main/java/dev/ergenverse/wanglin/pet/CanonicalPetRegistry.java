package dev.ergenverse.wanglin.pet;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CanonicalPetRegistry — populates all of Wang Lin's canon pets with the full
 * pet-specific data model (species, bloodline, cultivation, personality, growth
 * stages, known abilities, evolution history, relationship, contract /
 * reproduce / mentor / gift-offspring flags, canon summary, canon-demonstrated
 * behaviors, provenance).
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.registry.CanonicalPets} (which
 * surfaces the same pets as generic {@code CanonicalEntry} records for the
 * knowledge-graph / TeachingResolver). The {@code id} of each CanonicalPet
 * matches the {@code id} of the corresponding CanonicalEntry for bidirectional
 * lookup.
 *
 * <p>Sources: CANON_RI_COMPLETE_ITEMS.md §10 (Mounts & Companions),
 * CANON_RI_ECOLOGY.md, ri_canon_database.json. Chapter citations preserved
 * verbatim where the canon docs provide them; UNKNOWN where canon is silent.
 *
 * <h2>Prime Directive</h2>
 * <p>Where canon is silent, mark UNKNOWN. Never invent.
 */
public final class CanonicalPetRegistry {

    private CanonicalPetRegistry() {}

    private static final Map<String, CanonicalPet> PETS = new LinkedHashMap<>();
    private static volatile boolean bootstrapped = false;

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        Ergenverse.LOGGER.info("[CanonicalPetRegistry] ════════════════════════════════════════════");
        Ergenverse.LOGGER.info("[CanonicalPetRegistry]  Bootstrapping canonical pet data model...");
        Ergenverse.LOGGER.info("[CanonicalPetRegistry]  (Species / bloodline / growth / evolution / 4 flags)");
        Ergenverse.LOGGER.info("[CanonicalPetRegistry] ════════════════════════════════════════════");

        register(mosquitoBeast());
        register(mosquitoSwarm10000());
        register(leiJiThunderBeast());
        register(netherBeast());
        register(thunderToad());
        register(thunderCelestialBeast());
        register(brilliantVoidSeaDragon());
        register(ancientSoulRestrictionTortoise());
        register(devilSkyCloudMonkey());

        Ergenverse.LOGGER.info("[CanonicalPetRegistry] ──────────────────────────────────────");
        Ergenverse.LOGGER.info("[CanonicalPetRegistry]  {} canonical pets loaded.", PETS.size());
        Ergenverse.LOGGER.info("[CanonicalPetRegistry]  Pets: {}", String.join(", ", PETS.keySet()));
        Ergenverse.LOGGER.info("[CanonicalPetRegistry] ════════════════════════════════════════════");
    }

    private static void register(CanonicalPet pet) {
        if (pet == null) return;
        if (PETS.containsKey(pet.id())) {
            throw new IllegalStateException("Duplicate CanonicalPet id '" + pet.id() + "'");
        }
        PETS.put(pet.id(), pet);
    }

    /** Look up a pet by id. Returns null if not present. */
    public static CanonicalPet get(String id) {
        if (!bootstrapped) bootstrap();
        return PETS.get(id);
    }

    /** All pets (immutable). */
    public static List<CanonicalPet> all() {
        if (!bootstrapped) bootstrap();
        return List.copyOf(PETS.values());
    }

    /** Count. */
    public static int size() {
        if (!bootstrapped) bootstrap();
        return PETS.size();
    }

    /** All pets that can be contracted by the player. */
    public static List<CanonicalPet> contractable() {
        if (!bootstrapped) bootstrap();
        return PETS.values().stream()
                .filter(CanonicalPet::canContract)
                .toList();
    }

    /** All pets Wang Lin can gift offspring of. */
    public static List<CanonicalPet> giftableOffspring() {
        if (!bootstrapped) bootstrap();
        return PETS.values().stream()
                .filter(CanonicalPet::canGiftOffspring)
                .toList();
    }

    // ── Pet records (canon-grounded) ──────────────────────────────────────

    /** Mosquito Beast (蚊兽) — Wang Lin's signature companion. Rank-9 Void Mosquito. The named individual is "Lil Mosqi" (小蚊). */
    private static CanonicalPet mosquitoBeast() {
        return new CanonicalPet(
                "pet_mosquito_beast",
                "Mosquito Beast (Lil Mosqi)", "蚊兽 / 小蚊",
                "Rank-9 Void Mosquito (named individual: Lil Mosqi)",
                "Void Mosquito bloodline (destinedCompanion archetype)",
                "Retained to end (late Soul Formation / Soul Transformation tier by end of canon)",
                "Fierce but loyal to Wang Lin; soul-bound; multiplies into a swarm of 10,000",
                List.of(
                        "Tamed in the Cultivation Devil Sea (early-mid era) — named individual 'Lil Mosqi' (小蚊)",
                        "Originally part of a herd of Mosquito Beasts on Planet Suzaku",
                        "Soul-bound to Wang Lin",
                        "Multiplies later into a swarm of 10,000"
                ),
                List.of(
                        "Void-element bite (pierces most defenses)",
                        "Flight (signature mount)",
                        "Swarm multiplication (10,000 descendants)",
                        "Purple / Gold variants (destinedCompanion)"
                ),
                List.of(
                        "Early-mid era — tamed in the Cultivation Devil Sea (destinedCompanion); named individual 'Lil Mosqi'",
                        "Mid era — 1st pair given to Situ Nan (Ch. 441)",
                        "Later era — multiplied into a swarm of 10,000",
                        "Ch. 1276 — 2nd & 3rd pairs destroyed by Daoist Water",
                        "Ch. 1626 — restored via Void Gate power"
                ),
                "Signature companion (soul-bound mount)",
                /* canContract   */ true,
                /* canReproduce  */ true,
                /* canMentor     */ false,
                /* canGiftOffspring */ true,
                "Wang Lin's signature companion. The named individual is 'Lil Mosqi' (小蚊) — originally part of a herd of Mosquito Beasts on Planet Suzaku before Wang Lin tamed him. Rank-9 Void Mosquito. Multiplies later into a swarm of 10,000. 1st pair given to Situ Nan Ch. 441. 2nd & 3rd pairs destroyed Ch. 1276 vs Daoist Water → restored Ch. 1626 (Void Gate power). Purple / Gold variants exist (destinedCompanion archetype).",
                List.of(
                        "Mount (signature flying mount) — RI throughout",
                        "Multiplies into a 10,000-strong battlefield swarm — RI later era",
                        "1st pair gifted to Situ Nan — Ch. 441",
                        "2nd & 3rd pairs destroyed by Daoist Water — Ch. 1276",
                        "Restored via Void Gate power — Ch. 1626"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("early-mid era (Cultivation Devil Sea)", "Ch. 441 (Situ Nan gift)", "Ch. 1276 (Daoist Water)", "Ch. 1626 (Void Gate restoration)"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Mounts & Companions); xian-ni.fandom.com/wiki/Lil_Mosqi ('Lil Mosqi is Wang Lin's pet. Lil Mosqi was part of a herd of Mosquito Beasts on Planet Suzaku before he met Wang Lin.'); Baidu main character box: Mount(s) = Mosquito Beast, Lei Ji")
        );
    }

    /** Mosquito Swarm ×10,000 (蚊群) — battlefield swarm weapon. */
    private static CanonicalPet mosquitoSwarm10000() {
        return new CanonicalPet(
                "pet_mosquito_swarm_10000",
                "Mosquito Swarm ×10,000", "蚊群",
                "Rank-9 Void Mosquito swarm",
                "Void Mosquito bloodline (descendants of the original Mosquito Beast)",
                "Retained (battlefield-tier by end of canon)",
                "Swarm-intelligence; coordinated battlefield deployment",
                List.of(
                        "Bred from the original Mosquito Beast (later era)",
                        "Deploys as a battlefield swarm weapon"
                ),
                List.of(
                        "Swarm tactics (overwhelming numbers)",
                        "Void-element bites (collective)",
                        "Battlefield coverage (10,000 entities)"
                ),
                List.of(
                        "Descended from the original Mosquito Beast",
                        "Bred progressively through the later era"
                ),
                "Battlefield swarm weapon (descendants of the signature companion)",
                /* canContract   */ false,
                /* canReproduce  */ true,
                /* canMentor     */ false,
                /* canGiftOffspring */ true,
                "Descendants / swarm of the original Mosquito Beast — battlefield swarm weapon. Rank-9 Void Mosquito swarm.",
                List.of(
                        "Battlefield deployment (RI later era)",
                        "Swarm tactics against large enemy formations"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("later era"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Mosquito Swarm ×10,000)")
        );
    }

    /** Lei Ji / Thunder Beast (雷极) — Silver-Horned Thunder Beast; from the Mid-Quality God-Slaying War Chariot. */
    private static CanonicalPet leiJiThunderBeast() {
        return new CanonicalPet(
                "pet_lei_ji_thunder_beast",
                "Lei Ji / Thunder Beast (Silver-Horned)", "雷极",
                "Silver-Horned Thunder Beast",
                "Beast-soul of the Mid-Quality God-Slaying War Chariot (chariot-beast-soul-origin)",
                "Late Soul-Ascension (comparable tier at unsealing Ch. ~1080)",
                "Loyal mount; bound to the chariot; thunder-element ferocity",
                List.of(
                        "Beast-soul sealed within the Mid-Quality God-Slaying War Chariot",
                        "Unsealed Ch. ~1080 — became a Silver-Horned Thunder Beast"
                ),
                List.of(
                        "Thunder-element charge (mount-tier)",
                        "Silver-horn gore attack",
                        "Chariot-pulling (when sealed state)"
                ),
                List.of(
                        "Ch. ~1080 — unsealed from the Mid-Quality God-Slaying War Chariot",
                        "Retained to end as Wang Lin's secondary mount (after Mosquito Beast)"
                ),
                "Mount (chariot-beast-soul-origin); bound to the God-Slaying War Chariot — cannot be duplicated",
                /* canContract   */ false,
                /* canReproduce  */ false,
                /* canMentor     */ false,
                /* canGiftOffspring */ false,
                "Originally the beast-soul of the Mid Quality God-Slaying War Chariot; when unsealed, became a Silver-Horned Thunder Beast that fights alongside Wang Lin. Strength comparable to late Soul-Ascension. Per Baidu: Mount(s) = Mosquito Beast, Lei Ji.",
                List.of(
                        "Mount (secondary, after Mosquito Beast) — RI Ch. ~1080+",
                        "Chariot-pulling (sealed state, pre-Ch. ~1080)",
                        "Battlefield thunder-element charge — RI mid-late era"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Ch. ~1080 (unsealed)"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Lei Ji); Baidu main character box: Mount(s) = Mosquito Beast, Lei Ji")
        );
    }

    /** Nether Beast (冥兽) — life-bound beast; civilizations on its back. */
    private static CanonicalPet netherBeast() {
        return new CanonicalPet(
                "pet_nether_beast",
                "Nether Beast (Life-Bound)", "冥兽",
                "Ancient-tier Nether Beast",
                "Life-bound beast bloodline (gastroRealm archetype — vast being with civilizations on its back)",
                "Ancient-tier (exact realm UNKNOWN)",
                "Vast, slow, contemplative; hosts civilizations on its back; met Wang Lin peacefully",
                List.of(
                        "Life-bound (formed in the world of the Nether Beast)",
                        "Encountered by Wang Lin (met Lian Daofei inside)"
                ),
                List.of(
                        "Hosts civilizations on its back (gastroRealm)",
                        "Interior world navigable",
                        "Life-bound (cannot be unsummoned)"
                ),
                List.of(
                        "Wang Lin entered the Nether Beast's interior world",
                        "Encountered 'Madman' Lian Daofei inside — Lian Daofei transmitted the Indestructible Immortal Body art"
                ),
                "Life-bound beast (gastroRealm interior)",
                /* canContract   */ false,
                /* canReproduce  */ false,
                /* canMentor     */ true,
                /* canGiftOffspring */ false,
                "Per Baidu main character box: 'Life-bound Beast: Nether Beast'. A being so vast that civilizations build on its back (gastroRealm archetype). Inside the Nether Beast, Wang Lin encountered 'Madman' Lian Daofei.",
                List.of(
                        "Hosts civilizations on its back — RI throughout",
                        "Wang Lin entered its interior — RI (Lian Daofei encounter)",
                        "Lian Daofei transmitted the Indestructible Immortal Body — RI Ch. ~1509+"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Nether Beast era"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Nether Beast); Baidu main character box: 'Life-bound Beast: Nether Beast'")
        );
    }

    /** Thunder Toad (雷蟾) — thunder-element spirit beast. */
    private static CanonicalPet thunderToad() {
        return new CanonicalPet(
                "pet_thunder_toad",
                "Thunder Toad", "雷蟾",
                "Thunder-toad spirit beast",
                "Thunder-element spirit beast bloodline",
                "Thunder-element (exact realm UNKNOWN; early-Celestial-era acquisition)",
                "UNKNOWN — canon silent on personality; presumed thunder-element temperament (volatile, electric)",
                List.of(
                        "Acquired (early acquisition, Celestial-era)"
                ),
                List.of(
                        "Thunder-element discharge",
                        "Spirit beast utility (companion-tier)"
                ),
                List.of(
                        "Celestial-era — acquired (early)",
                        "Retained through the Celestial arc"
                ),
                "Companion (early acquisition)",
                /* canContract   */ true,
                /* canReproduce  */ false,
                /* canMentor     */ false,
                /* canGiftOffspring */ false,
                "Thunder-toad spirit beast, thunder-element companion.",
                List.of(
                        "Thunder-element companion — RI Celestial era",
                        "Utility beast (spirit-tier)"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Celestial-era"),
                        4, "CANON_RI_COMPLETE_ITEMS.md §10 (Thunder Toad); canon confidence 4 — chapter unspecified in wiki")
        );
    }

    /** Thunder Celestial Beast (雷仙兽) — thunder-element celestial beast. */
    private static CanonicalPet thunderCelestialBeast() {
        return new CanonicalPet(
                "pet_thunder_celestial_beast",
                "Thunder Celestial Beast", "雷仙兽",
                "Thunder-element celestial beast",
                "Celestial-tier thunder beast bloodline",
                "Celestial-tier (early-Celestial-era acquisition)",
                "UNKNOWN — canon silent on personality; presumed celestial-tier thunder beast (aloof, powerful)",
                List.of(
                        "Acquired (early-Celestial-era)"
                ),
                List.of(
                        "Thunder-element celestial attacks",
                        "Celestial-tier companion utility"
                ),
                List.of(
                        "Early-Celestial-era — acquired",
                        "Retained through the Celestial arc"
                ),
                "Companion (celestial-tier)",
                /* canContract   */ true,
                /* canReproduce  */ false,
                /* canMentor     */ false,
                /* canGiftOffspring */ false,
                "A thunder-element celestial beast companion.",
                List.of(
                        "Celestial-tier thunder beast — RI Celestial era",
                        "Companion utility"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Celestial era"),
                        4, "CANON_RI_COMPLETE_ITEMS.md §10 (Thunder Celestial Beast); canon confidence 4 — chapter unspecified in wiki")
        );
    }

    /** Brilliant Void Sea Dragon (耀虚海龙) — Void-tier sea dragon. */
    private static CanonicalPet brilliantVoidSeaDragon() {
        return new CanonicalPet(
                "pet_brilliant_void_sea_dragon",
                "Brilliant Void Sea Dragon", "耀虚海龙",
                "Void-tier sea dragon",
                "Void-element sea dragon bloodline",
                "Void-tier (mid-era acquisition)",
                "UNKNOWN — canon silent on personality; presumed void-element sea-dragon (serene, vast)",
                List.of(
                        "Acquired (mid-era)"
                ),
                List.of(
                        "Void-element sea-dragon attacks",
                        "Aquatic / void traversal"
                ),
                List.of(
                        "Mid-era — acquired",
                        "Retained through the mid-late era"
                ),
                "Companion (void-tier)",
                /* canContract   */ true,
                /* canReproduce  */ false,
                /* canMentor     */ false,
                /* canGiftOffspring */ false,
                "Void-sea dragon companion. Void-element.",
                List.of(
                        "Void-sea dragon — RI mid era",
                        "Aquatic / void traversal"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("mid era"),
                        4, "CANON_RI_COMPLETE_ITEMS.md §10 (Brilliant Void Sea Dragon); canon confidence 4 — chapter unspecified in wiki")
        );
    }

    /** Ancient Soul Restriction Tortoise Beast (古魂禁龟兽) — living formation-component. */
    private static CanonicalPet ancientSoulRestrictionTortoise() {
        return new CanonicalPet(
                "pet_ancient_soul_restriction_tortoise",
                "Ancient Soul Restriction Tortoise Beast", "古魂禁龟兽",
                "Tortoise-beast (formation-component)",
                "Ancient-soul-restriction bloodline (gifted by the old Vermilion Bird in the Fallen Land)",
                "Restriction-tier (living formation-component)",
                "Slow, deliberate, restriction-attuned; carries ancient-soul-restriction properties",
                List.of(
                        "Gifted by the old Vermilion Bird in the Fallen Land (Ch. 1426)"
                ),
                List.of(
                        "Living formation-component (anchors restriction formations)",
                        "Ancient-soul-restriction properties",
                        "Tortoise-shell durability"
                ),
                List.of(
                        "Ch. 1426 — gifted by the old Vermilion Bird in the Fallen Land",
                        "Functions as a living formation-component thereafter"
                ),
                "Contracted inheritance (gifted by the old Vermilion Bird)",
                /* canContract   */ false,
                /* canReproduce  */ false,
                /* canMentor     */ true,
                /* canGiftOffspring */ false,
                "Given by the old Vermilion Bird in the Fallen Land. A living tortoise-beast with ancient-soul-restriction properties. Functions as a living formation-component.",
                List.of(
                        "Living formation-component — RI Ch. 1426+",
                        "Ancient-soul-restriction properties (active in formation work)",
                        "Gifted by the old Vermilion Bird — Ch. 1426"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Ch. 1426"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Ancient Soul Restriction Tortoise Beast); CANON_RI_COMPLETE_ITEMS.md §8 (formation-component)")
        );
    }

    /** Devil Sky Cloud Monkey (魔天云猴) — 3rd devil captured at Sky Cloud Sect. */
    private static CanonicalPet devilSkyCloudMonkey() {
        return new CanonicalPet(
                "pet_devil_sky_cloud_monkey",
                "Devil Sky Cloud Monkey", "魔天云猴",
                "Devil-tier monkey (companion devil)",
                "Devil bloodline (3rd devil refined/captured by Wang Lin)",
                "Devil-tier (Ch. ~620 acquisition)",
                "Cunning, possession-gifted, mischievous (per Baidu: devils are extremely cunning and gifted in possession)",
                List.of(
                        "Captured at Sky Cloud Sect (Ch. ~620)",
                        "Refined as the 3rd devil companion"
                ),
                List.of(
                        "Possession (devil-tier signature ability)",
                        "Cunning / trickery",
                        "Cloud-element movement (Sky Cloud Sect lineage)"
                ),
                List.of(
                        "Ch. ~620 — captured at Sky Cloud Sect (3rd devil)",
                        "Retained as devil-tier companion"
                ),
                "Contracted inheritance (captured devil); devil-tier companion",
                /* canContract   */ false,
                /* canReproduce  */ false,
                /* canMentor     */ false,
                /* canGiftOffspring */ false,
                "3rd devil Wang Lin refined/captured (Ch. ~620). Devil-tier companion — Sky Cloud Sect monkey. Devils are extremely cunning and gifted in possession.",
                List.of(
                        "Possession-gifted (devil-tier signature ability) — RI Ch. ~620+",
                        "3rd devil companion — RI Ch. ~620",
                        "Cunning / trickery in combat"
                ),
                Provenance.explicit("Renegade Immortal",
                        List.of("Ch. ~620"),
                        5, "CANON_RI_COMPLETE_ITEMS.md §10 (Devils); CANON_RI_COMPLETE_ITEMS.md §10 (Devil Sky Cloud Monkey)")
        );
    }
}
