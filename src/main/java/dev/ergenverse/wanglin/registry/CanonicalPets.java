package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalPets — Wang Lin's tamed beasts (signature subset).
 *
 * <p>Delegates to {@code dev.ergenverse.wanglin.pet.CanonicalPetRegistry} for
 * the full pet data model (species, bloodline, growth stages, etc.). The
 * entries here surface the canonical-ownership + transferability verdict for
 * each pet.
 *
 * <p>Per the user's task brief: extract Wang Lin's canon pets — the mosquito
 * beast, the thunder toad, the snow-hair beast, the little turtle, etc.
 */
public final class CanonicalPets extends AbstractSubRegistry {

    public CanonicalPets() {
        super("CanonicalPets");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "pet_mosquito_beast", "Mosquito Beast (Lil Mosqi)", "蚊兽 / 小蚊",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era", "Sea of Devils"), 5,
                        "xian-ni.fandom.com/wiki/Lil_Mosqi: 'Lil Mosqi is Wang Lin's pet. Lil Mosqi was part of a herd of Mosquito Beasts on Planet Suzaku before he met Wang Lin.' The named individual mosquito beast is 'Lil Mosqi' (小蚊)."),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.TESTS_FIRST,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.PARTIAL,
                        Transferability.CanGiftEquivalent.YES_EXACT_COPY,
                        Transferability.CanCreateNew.YES,
                        "Tamed in the Sea of Devils; multiplies into a swarm of 10,000. 1st given to Situ Nan (Ch. 441); 2nd & 3rd pairs destroyed Ch. 1276 vs Daoist Water → restored Ch. 1626. Wang Lin can breed more."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's signature companion. The named individual is 'Lil Mosqi' (小蚊) — originally part of a herd of Mosquito Beasts on Planet Suzaku before Wang Lin tamed him. Rank-9 Void Mosquito. Multiplies later into a swarm of 10,000. 1st given to Situ Nan Ch. 441. 2nd & 3rd pairs destroyed Ch. 1276 vs Daoist Water → restored Ch. 1626 (Void Gate power). Purple / Gold variants exist.",
                List.of("Named individual: 'Lil Mosqi' (小蚊) — from a Planet Suzaku herd",
                        "Rank-9 Void Mosquito",
                        "Multiplies into a swarm of 10,000",
                        "1st pair given to Situ Nan (Ch. 441)",
                        "2nd & 3rd pairs destroyed by Daoist Water (Ch. 1276) → restored (Ch. 1626)"),
                List.of("pet", "beast", "void_mosquito", "lil_mosqi", "sea_of_devils", "swarm", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "pet_mosquito_swarm_10000", "Mosquito Swarm ×10,000", "蚊群",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("later era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.WILL_TEACH,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.YES,
                        Transferability.CanGiftEquivalent.YES_EXACT_COPY,
                        Transferability.CanCreateNew.YES,
                        "Bred from the original Mosquito Beast — Wang Lin can breed more."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Descendants / swarm of the original Mosquito Beast — battlefield swarm weapon. Rank-9 Void Mosquito swarm.",
                List.of("Battlefield swarm weapon",
                        "Descendants of the original Mosquito Beast",
                        "Rank-9 Void Mosquito swarm"),
                List.of("pet", "beast", "void_mosquito", "swarm", "battlefield", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "pet_lei_ji_thunder_beast", "Lei Ji / Thunder Beast (Silver-Horned)", "雷极",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1080"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Beast-soul of the Mid Quality God-Slaying War Chariot; unsealed to become a Silver-Horned Thunder Beast. Bound to the chariot — cannot be duplicated."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Originally the beast-soul of the Mid Quality God-Slaying War Chariot; when unsealed, became a Silver-Horned Thunder Beast that fights alongside Wang Lin. Strength comparable to late Soul-Ascension. Per Baidu: Mount(s) = Mosquito Beast, Lei Ji.",
                List.of("Originally the beast-soul of the Mid-Quality God-Slaying Chariot",
                        "Became a Silver-Horned Thunder Beast when unsealed",
                        "Strength comparable to late Soul-Ascension"),
                List.of("pet", "beast", "thunder", "silver_horned", "war_chariot", "chariot_beast_soul")
        ));

        register(CanonicalEntry.of(
                "pet_nether_beast", "Nether Beast (Life-Bound)", "冥兽",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Nether Beast era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Life-bound beast — formed in the world of the Nether Beast."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Per Baidu main character box: 'Life-bound Beast: Nether Beast'. A being so vast that civilizations build on its back (gastroRealm archetype). Inside the Nether Beast, Wang Lin encountered 'Madman' Lian Daofei.",
                List.of("Life-bound beast",
                        "Vast being — civilizations on its back",
                        "Inside it: Wang Lin met Lian Daofei"),
                List.of("pet", "beast", "life_bound", "gastroRealm", "nether_beast", "lian_daofei")
        ));

        register(CanonicalEntry.of(
                "pet_thunder_toad", "Thunder Toad", "雷蟾",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Celestial-era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Thunder-element spirit beast; early acquisition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder-toad spirit beast, thunder-element companion.",
                List.of("Thunder-element companion",
                        "Spirit beast"),
                List.of("pet", "beast", "thunder", "toad", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "pet_thunder_celestial_beast", "Thunder Celestial Beast", "雷仙兽",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Celestial era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Celestial-tier thunder beast; early-Celestial-era acquisition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A thunder-element celestial beast companion.",
                List.of("Thunder-element celestial beast"),
                List.of("pet", "beast", "thunder", "celestial", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "pet_brilliant_void_sea_dragon", "Brilliant Void Sea Dragon", "耀虚海龙",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Void-tier sea dragon companion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Void-sea dragon companion. Void-element.",
                List.of("Void-tier sea dragon",
                        "Void-element"),
                List.of("pet", "beast", "void", "sea_dragon", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "pet_ancient_soul_restriction_tortoise", "Ancient Soul Restriction Tortoise Beast", "古魂禁龟兽",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1426"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by the old Vermilion Bird in the Fallen Land."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Given by the old Vermilion Bird in the Fallen Land. A living tortoise-beast with ancient-soul-restriction properties. Functions as a living formation-component.",
                List.of("Living tortoise-beast",
                        "Ancient-soul-restriction properties",
                        "Living formation-component"),
                List.of("pet", "beast", "tortoise", "restriction", "vermilion_bird", "fallen_land")
        ));

        register(CanonicalEntry.of(
                "pet_devil_sky_cloud_monkey", "Devil Sky Cloud Monkey", "魔天云猴",
                CanonicalCategory.PETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~620"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("3rd devil captured at Sky Cloud Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "3rd devil Wang Lin refined/captured (Ch. ~620). Devil-tier companion — Sky Cloud Sect monkey.",
                List.of("3rd devil captured at Sky Cloud Sect",
                        "Devil-tier companion"),
                List.of("pet", "devil", "monkey", "sky_cloud_sect", "wang_lin_owned")
        ));
    }
}
