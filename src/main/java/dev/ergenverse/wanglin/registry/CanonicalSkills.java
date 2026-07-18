package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalSkills — discrete skills Wang Lin demonstrated.
 *
 * <p>Distinct from techniques (which are codified cultivation arts). Skills are
 * the practical crafts Wang Lin learned: Soul Refining, Alchemy, Refining
 * (treasure-forging), Formations, Restriction-Reading, Sword-Refinement, etc.
 */
public final class CanonicalSkills extends AbstractSubRegistry {

    public CanonicalSkills() {
        super("CanonicalSkills");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "skill_restriction_reading", "Restriction Reading", "读禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~179-180"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-taught via 7-year Restriction Mountain study."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "After 7 years of research and improvement, Wang Lin could read any restriction's structure and rules without looking with his eyes — by examining the wave produced by the illusionary circle.",
                List.of("Reads restriction structure without visual sight",
                        "Wave-based reading"),
                List.of("skill", "restriction", "wang_lin_created", "wave")
        ));

        register(CanonicalEntry.of(
                "skill_restriction_forging", "Restriction Forging", "禁器",
                CanonicalCategory.REFINING,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~180"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Self-forging skill; Tu Si inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can forge restriction treasures (Restriction Flags, Heart Compass, Soul Devil Ship, Ghostly Sail). Requires 3 Ink Stones per flag and 99,999 restrictions woven.",
                List.of("Forges Restriction Flags, Heart Compass, Soul Devil Ship",
                        "99,999 restrictions per flag",
                        "Inkstones required"),
                List.of("skill", "refining", "tu_si", "wang_lin_created", "inkstone")
        ));

        register(CanonicalEntry.of(
                "skill_soul_refining", "Soul Refining", "炼魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance (Dun Tian)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can refine captured souls into soul-flags and soul-banners. Powers the One Billion Soul Banner.",
                List.of("Refines captured souls into soul-flags",
                        "Powers the One Billion Soul Banner"),
                List.of("skill", "soul", "soul_refining_sect", "dun_tian", "inheritance")
        ));

        register(CanonicalEntry.of(
                "skill_soul_extracting", "Soul Extracting", "抽魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can extract souls from bodies (corpse-soul extraction).",
                List.of("Extracts souls from bodies"),
                List.of("skill", "soul", "soul_refining_sect", "inheritance")
        ));

        register(CanonicalEntry.of(
                "skill_soul_sealing", "Soul Sealing", "封魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can seal refined souls into the Soul Flag / Soul Banner.",
                List.of("Seals refined souls into the Soul Flag"),
                List.of("skill", "soul", "soul_refining_sect", "inheritance", "soul_flag")
        ));

        register(CanonicalEntry.of(
                "skill_treasure_refining", "Treasure Refining", "炼器",
                CanonicalCategory.REFINING,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's treasure-refining skill — forges and refines magical treasures. Includes blood-refinement (Core-Treasure Sword → Dark Green Flying Sword), divine-tribulation refinement (Fragment Stamp → 18-Hell Celestial Sealing Stamp), and inheritance-treasure refinement (Heaven Avoiding Coffin, Fate Sealing Ring).",
                List.of("Blood-refinement (Core-Treasure Sword → Dark Green Flying Sword)",
                        "Divine-tribulation refinement (Fragment Stamp → 18-Hell Celestial Sealing Stamp)",
                        "Inheritance-treasure refinement (Heaven Avoiding Coffin)"),
                List.of("skill", "refining", "wang_lin_created", "blood_refined", "divine_tribulation")
        ));

        register(CanonicalEntry.withSpec(
                "skill_puppet_refining", "Puppet Refining (Celestial Guard Method)", "炼傀",
                CanonicalCategory.PUPPETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 653"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Huang Yu gave him the method to create a celestial guard."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can refine cultivator bodies into Celestial Guards (copper/silver rank). The method came from Huang Yu. Adds a heavenly ghost to make it Illusionary-Yin-strength.",
                List.of("Refines cultivators into Celestial Guards",
                        "Heavenly-ghost augmentation → Illusionary-Yin strength",
                        "Huang Yu transmitted the method"),
                List.of("skill", "puppet", "celestial_guard", "huang_yu", "inheritance"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.of(
                "skill_alchemy", "Alchemy (basic)", "炼丹",
                CanonicalCategory.ALCHEMY,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Not Wang Lin's primary focus; basic competency."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired basic competency."),
                Demonstrability.CAN_DEMONSTRATE_PARTIAL,
                "Wang Lin learned the basics of alchemy but it is not his primary focus. He prefers to find pills rather than refine them.",
                List.of("Basic alchemy competency",
                        "Not his primary focus"),
                List.of("skill", "alchemy", "wang_lin_created", "basic")
        ));

        register(CanonicalEntry.of(
                "skill_formation_arraying", "Formation Arraying", "布阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can erect formation-arrays on the battlefield. Includes self-erected formations (Unnamed Wheel Formation, Soul Devil Ship, Nine Deaths Perish Formation, Body Formation, God-Tremble Army Formation).",
                List.of("Erects formation-arrays on the battlefield",
                        "Self-erected formations include Unnamed Wheel Formation"),
                List.of("skill", "formation", "wang_lin_created", "array")
        ));

        register(CanonicalEntry.of(
                "skill_sword_refining", "Sword Refining", "炼剑",
                CanonicalCategory.REFINING,
                Provenance.explicit("Renegade Immortal", List.of("Core-Treasure Sword era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can refine flying swords — blood-refinement, poison-attribute refinement, thunder-attribute refinement. Condensed the 6 Origin Swords via the void-gate collapse vortex.",
                List.of("Blood-refines flying swords",
                        "Condensed the 6 Origin Swords via void-gate vortex"),
                List.of("skill", "refining", "sword", "wang_lin_created", "origin_sword")
        ));

        register(CanonicalEntry.of(
                "skill_disguise", "Disguising Technique", "易容",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("early era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's disguise skill — conceals his realm and appearance. Used to hide from Teng Huayuan and many other enemies.",
                List.of("Conceals realm and appearance",
                        "Used to hide from Teng Huayuan"),
                List.of("skill", "disguise", "wang_lin_created", "concealment")
        ));

        register(CanonicalEntry.of(
                "skill_memory_erasing", "Memory Erasing", "抹忆",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin can erase memories from mortals and low-realm cultivators who saw too much.",
                List.of("Erases memories from mortals / low-realm cultivators"),
                List.of("skill", "divine_sense", "wang_lin_created", "concealment")
        ));
    }
}
