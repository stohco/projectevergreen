package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalFormations — Wang Lin's formation-array treasures & self-erected formations.
 *
 * <p>Includes the Soul Devil Ship (composite of the 4 Great Restrictions),
 * the Unnamed Wheel Formation (Wang Lin's replacement for the Realm-Sealing
 * Formation), 9 Deaths Perish Formation, and the Soul-Refining Sect's
 * Blood Pavilion / Formation manuals.
 */
public final class CanonicalFormations extends AbstractSubRegistry {

    public CanonicalFormations() {
        super("CanonicalFormations");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "soul_devil_ship", "Soul Devil Ship", "魂魔船",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1789"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Self-forged — a ship made up of the four great restrictions and many other restrictions."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Composite formation treasure — ship made up of the four great restrictions and many other restrictions. Pairs with Ghostly Sail. Used by Fan Shanmeng to cast the multi-layered illusion spell on Wang Lin and her sister Fan Shanlu.",
                List.of("Composite of the 4 Great Restrictions",
                        "Pairs with Ghostly Sail",
                        "Used to cast multi-layered illusion spell"),
                List.of("formation", "ship", "four_great_restrictions", "ghostly_sail", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "ghostly_sail_main", "Ghostly Sail (main)", "鬼帆",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1699", "Ch. 1854 (Wang Lin refines his own)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("1st: refined by Wang Lin himself (Ch. 1854)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Main sail for the Soul Devil Ship. Not simply a ghostly face — contains many restrictions. Everything about the Soul Devil Ship is encoded in the sail.",
                List.of("Main sail of the Soul Devil Ship",
                        "Encodes the entire Soul Devil Ship"),
                List.of("formation", "sail", "soul_devil_ship", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "ghostly_sail_vice", "Ghostly Sail (vice)", "副鬼帆",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("later era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Used as sect-protection formation of the Great Soul Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The vice Ghostly Sail of the primary Ghostly Sail, used as sect-protection formation of the Great Soul Sect.",
                List.of("Vice sail of the primary Ghostly Sail",
                        "Used as sect-protection formation of the Great Soul Sect"),
                List.of("formation", "sail", "great_soul_sect", "sect_protection")
        ));

        register(CanonicalEntry.of(
                "unnamed_wheel_formation", "Unnamed Wheel Formation", "无名轮阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1667"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-erected. Upgraded version of his Life-Death / Karma / True-False Wheel Formation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's replacement for the Realm-Sealing Formation. Third-Step-tier formation. Stops all Outer-Realm cultivators from entering the Inner Realm while NOT restricting the entrance of Joss Flames (the Realm-Sealing Formation's biggest flaw). Calls treasure spirits of all treasures destroyed in the past 100 years of war → fuse with formation → become eternal. Calls souls of all cultivation planets destroyed in the past 100 years → fuse with formation → become eternal. Gathers soul fragments of countless Outer-Realm cultivators who died in the Inner Realm → forces them to push the wheel forever. Beseeches resting souls of all Inner-Realm cultivators who died in previous + current wars → become the formation's immortal formation spirit, worshipped by generations.",
                List.of("Replaces the Realm-Sealing Formation",
                        "Stops Outer-Realm cultivators from entering Inner Realm",
                        "Does NOT restrict Joss Flame entrance (unlike Realm-Sealing)",
                        "Calls treasure spirits, planet souls, soul fragments",
                        "Active eternal formation"),
                List.of("formation", "wheel", "wang_lin_created", "realm_sealing_replacement", "eternal", "soul_fueled", "joss_flame")
        ));

        register(CanonicalEntry.of(
                "nine_deaths_perish_formation", "Nine Deaths Perish Formation", "九死灭阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 829"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-constructed formation from Annihilation-lineage restrictions."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient deadly formation. Self-constructed from Annihilation-lineage restrictions.",
                List.of("Ancient deadly formation",
                        "Annihilation-lineage"),
                List.of("formation", "trap", "annihilation", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "seven_star_sword_formation", "Seven Star Sword Formation", "七星剑阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-erected sword formation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Seven-star sword formation — sword-array that aligns with the seven stars for offense and defense.",
                List.of("Seven-star sword array"),
                List.of("formation", "sword", "seven_star", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "blood_pavilion", "Blood Pavilion", "血阁",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Refining Sect era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance (Dun Tian gift)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A pavilion-formation treasure of the Soul Refining Sect — refines captured souls in bulk.",
                List.of("Pavilion-formation",
                        "Soul Refining Sect inheritance"),
                List.of("formation", "pavilion", "soul_refining_sect", "dun_tian", "inheritance")
        ));

        register(CanonicalEntry.of(
                "three_purple_flags_defensive", "Three Purple Flags (defensive)", "三紫旗",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfForged("Set of three purple flags — defensive."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Defensive treasure (set of three purple flags).",
                List.of("Set of three purple flags",
                        "Defensive"),
                List.of("formation", "defensive", "flags", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "heaven_tiger_flag", "Heaven Tiger Flag / Banner", "天虎旗",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 879"), 5,
                        "Awarded at Luo Tian Immortal Sealing ceremony per Baidu."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Awarded at Luo Tian Immortal Sealing ceremony."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Big Boi technique — materializes a Heavenly Tiger (天虎) projection to fight for the wielder.",
                List.of("Materializes a Heavenly Tiger projection",
                        "Awarded at Luo Tian Immortal Sealing"),
                List.of("formation", "defensive", "summon", "heavenly_tiger", "luo_tian_ceremony")
        ));

        register(CanonicalEntry.of(
                "ancient_restrictions_mountain_trial", "Ancient Restrictions Mountain Trial (location)", "古禁山试炼",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~179-180 (S1 EP31)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si's trial — Wang Lin is the 4th person ever to complete it."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Land of the Ancient God Restrictions Mountain Trial. Wang Lin spent 7 years studying here; he became the 4th person ever to complete the trial, after which Tu Si gave him the Restriction Flag refining method.",
                List.of("7-year study site",
                        "4th person ever to complete the trial",
                        "Triggered the Restriction Flag inheritance"),
                List.of("formation", "trial", "tu_si", "ancient_god", "restriction_mountain", "seven_years")
        ));

        register(CanonicalEntry.of(
                "god_tremble_army_formation", "God-Tremble Army Formation", "神震军阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ancient God era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-erected army formation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Army-scale formation that channels the Ancient God's awe — suppresses enemy cultivators by shockwave.",
                List.of("Army-scale formation",
                        "Suppresses enemy cultivators by shockwave"),
                List.of("formation", "army", "ancient_god", "wang_lin_created", "shockwave")
        ));

        register(CanonicalEntry.of(
                "body_formation", "Body Formation", "身阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Transformation era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-erected body-protecting formation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Body-worn formation that protects the cultivator from divine-sense attacks and ambushes.",
                List.of("Body-worn protective formation",
                        "Defends against divine-sense attacks"),
                List.of("formation", "defensive", "body", "wang_lin_created")
        ));

        // ── Additional formations ────────────────────────────────────
        register(CanonicalEntry.of(
                "formation_heaven_devouring_demon", "Heaven Devouring Demon Formation", "吞天魔阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 139"), 5,
                        "Cross-refs RICanonicalDatabase T131."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired vicious formation (Ch. 139)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heaven Devouring Demon Formation. Self-acquired vicious formation (Ch. 139). Devours the heavens and the target's energy.",
                List.of("Self-acquired vicious formation",
                        "Devours the heavens and the target's energy",
                        "Ch. 139"),
                List.of("formation", "vicious", "heaven_devouring", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "formation_isolation_restriction_compass_v2", "Isolation Restriction Compass (formation)", "隔离禁盘",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1850", "Ch. 1864 (discarded)"), 5,
                        "Cross-refs RICanonicalDatabase I71."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Took from Green Devil Continent woman at pill sea (Ch. 1850)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Isolation Restriction Compass. Contains Devil Restriction Sect's Devil Isolation Restriction. Tracking-vulnerable — discarded Ch. 1864.",
                List.of("Contains Devil Restriction Sect's Devil Isolation Restriction",
                        "Tracking-vulnerable",
                        "Discarded Ch. 1864"),
                List.of("formation", "isolation", "devil_restriction_sect", "discarded")
        ));

        register(CanonicalEntry.of(
                "formation_realm_sealing_grand_array_v2", "Realm-Sealing Grand Array (formation)", "封界大阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5,
                        "Wang Lin reset this array as Lord of the Sealed Realm."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Spirit: Heaven-Splitting Axe. Wang Lin reset this as Lord of the Sealed Realm."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Realm-Sealing Grand Array. The seal that suppresses Third-Step cultivators in the Sealed Realm. Spirit: Heaven-Splitting Axe. Replaced by Wang Lin's Unnamed Wheel Formation.",
                List.of("Suppresses Third-Step cultivators in the Sealed Realm",
                        "Spirit: Heaven-Splitting Axe",
                        "Replaced by Unnamed Wheel Formation"),
                List.of("formation", "realm_sealing", "heaven_splitting_axe", "sealed_realm")
        ));
    }
}
