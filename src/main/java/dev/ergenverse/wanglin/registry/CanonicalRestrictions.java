package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalRestrictions — Wang Lin's restriction system (4 Great + Restriction Flag + Restriction Essence).
 *
 * <p>Cross-refs CANON_RI_COMPLETE_TECHNIQUES.md §D (Restriction Arts) and §E (4 Great Restrictions).
 */
public final class CanonicalRestrictions extends AbstractSubRegistry {

    public CanonicalRestrictions() {
        super("CanonicalRestrictions");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "ancient_restrictions_art", "Ancient Restrictions (foundational art)", "古禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~179-180 (S1 EP31)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-taught via the trial — 7 years of study."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Foundation for all Wang Lin's restriction work. Wang Lin spent seven years studying in the Land of the Ancient God Restrictions Mountain Trial — he became the 4th person ever to complete the trial.",
                List.of("7-year study at Restriction Mountain",
                        "4th person ever to complete the trial"),
                List.of("restriction", "ancient_god", "tu_si", "restriction_mountain", "foundational")
        ));

        register(CanonicalEntry.withSpec(
                "restriction_flag_method", "Restriction Flags Refining Method", "禁旗炼制法",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~180 (S1 EP32)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Inherited from Tu Si after passing the trial. Each flag requires 3 Ink Stones + 99,999 restrictions."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Explains the flag material made with inkstones; put 99,999 restrictions to complete. Each Restriction Flag is a portable formation-array.",
                List.of("99,999 restrictions per flag",
                        "Requires 3 Ink Stones per flag",
                        "Each flag is a portable formation-array"),
                List.of("restriction", "tu_si", "inkstone", "forge", "restriction_flag", "inheritance"),
                "restriction_flag"
        ));

        register(CanonicalEntry.of(
                "illusionary_circle_art", "Illusionary Circle", "幻阵",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 180"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-developed after 7 years of research."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wave-based restriction analysis tool — lets Wang Lin 'read' any restriction's structure and rules without seeing it.",
                List.of("Wave-based restriction analysis",
                        "Reads restriction structure without visual sight"),
                List.of("restriction", "analysis", "wang_lin_created", "wave")
        ));

        register(CanonicalEntry.of(
                "annihilation_restriction_art", "Annihilation Restriction (1st Great Restriction)", "寂灭禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 754"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited half of restrictions heart from Li Yuan."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the four great restrictions. Annihilation-attribute; destroy-the-target restriction. Source of the 18 Plum Restriction. Combined with Heart Compass for full activation.",
                List.of("1st of the Four Great Restrictions",
                        "Annihilation-attribute",
                        "Source of 18 Plum Restriction"),
                List.of("restriction", "great_restriction", "annihilation", "li_yuan", "inheritance", "heart_compass")
        ));

        register(CanonicalEntry.of(
                "time_restriction_art", "Time Restriction (2nd Great Restriction)", "时禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1223"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "2nd of the four great restrictions. Time-bending restriction.",
                List.of("2nd of the Four Great Restrictions",
                        "Time-bending"),
                List.of("restriction", "great_restriction", "time", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "life_death_restriction_art", "Life and Death Restriction (3rd Great Restriction)", "生死禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1229"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "3rd of the four great restrictions. Life-death-attribute.",
                List.of("3rd of the Four Great Restrictions",
                        "Life-death-attribute"),
                List.of("restriction", "great_restriction", "life_death", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "ancient_soul_restriction_art", "Ancient Soul Restriction (4th Great Restriction)", "古魂禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1697"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "4th of the four great restrictions. Ancient-soul-attribute.",
                List.of("4th of the Four Great Restrictions",
                        "Ancient-soul-attribute"),
                List.of("restriction", "great_restriction", "ancient_soul", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "18_plum_restriction", "18 Plum Restriction / Plum Blossom Eighteen Restriction", "梅花十八禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 752"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-derived from the Annihilation Restriction."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mysterious restriction art derived from the ancient Destruction Restriction. Unpredictable, almost supernatural. Once set, operates autonomously. Form resembles a blooming plum blossom. The 18 sets of restrictions transform into statues. Derived from Annihilation Restriction → can be deployed as a treasure/compass (Heart Compass).",
                List.of("Derived from Annihilation Restriction",
                        "Form resembles a blooming plum blossom",
                        "18 sets transform into statues",
                        "Operates autonomously once set"),
                List.of("restriction", "annihilation", "plum_blossom", "autonomous", "wang_lin_created", "heart_compass")
        ));

        register(CanonicalEntry.of(
                "heart_restriction", "Heart Restriction", "心禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 858"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's specialized restriction — paired with Heart Compass."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's specialized restriction — paired with Heart Compass (item) and the Restriction Heart inheritance. Restricts the heart/mind; works on the inner demons of self and enemies.",
                List.of("Restricts heart/mind",
                        "Works on inner demons of self and enemies",
                        "Paired with Heart Compass"),
                List.of("restriction", "heart", "wang_lin_created", "heart_compass", "inner_demon")
        ));

        register(CanonicalEntry.of(
                "destruction_restriction", "Destruction Restriction", "破禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Heart Compass era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Pairs with Heart Compass (Annihilation Restriction inheritance)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Pairs with Heart Compass to seal all things (immortals, demons, mortal beasts).",
                List.of("Pairs with Heart Compass",
                        "Seals immortals / demons / mortal beasts"),
                List.of("restriction", "destruction", "heart_compass", "sealing", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "teleportation_restriction_art", "Teleportation Restriction", "传送禁",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 493"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired movement + restriction hybrid."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Restriction-based teleportation — Wang Lin's signature movement restriction.",
                List.of("Restriction-based teleportation",
                        "Movement + restriction hybrid"),
                List.of("restriction", "movement", "teleportation", "wang_lin_created", "space")
        ));

        register(CanonicalEntry.of(
                "great_teleportation_forbidden", "Great Teleportation Forbidden", "大传送禁",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Void Treading era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A higher-tier teleportation restriction — Wang Lin's void-traversal restriction.",
                List.of("Higher-tier teleportation restriction",
                        "Void-traversal"),
                List.of("restriction", "movement", "teleportation", "void", "wang_lin_created")
        ));
    }
}
