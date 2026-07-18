package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalTitles — Wang Lin's titles & honorifics.
 *
 * <p>Each title reflects a stage of Wang Lin's life. None can be transferred;
 * they are recognitions of his accomplishments.
 */
public final class CanonicalTitles extends AbstractSubRegistry {

    public CanonicalTitles() {
        super("CanonicalTitles");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "great_enlightened_one", "Great Enlightened One", "大觉者",
                CanonicalCategory.KNOWLEDGE,
                Provenance.explicit("Renegade Immortal", List.of("Tu Si inheritance"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Granted by Tu Si's inheritance — a recognition, not a transferable title."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Title granted by Tu Si when Wang Lin received the Ancient God 'knowledge' inheritance. (Tuo Sen received the 'power' inheritance.)",
                List.of("Granted by Tu Si's knowledge inheritance",
                        "Paired with Tuo Sen receiving the 'power' inheritance"),
                List.of("title", "tu_si", "ancient_god", "inheritance", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "6th_gen_vermilion_bird_emperor", "6th-Generation Vermilion Bird Divine Emperor", "朱雀六代神皇",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Vermilion Bird Divine Sect era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Inherited title of the Vermilion Bird Divine Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Became the 6th-Generation Vermilion Bird Divine Emperor through Situ Nan's lineage.",
                List.of("6th-Generation Vermilion Bird Divine Emperor",
                        "Inherited via Situ Nan's lineage"),
                List.of("title", "vermilion_bird", "situ_nan", "inheritance", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "lord_of_sealed_realm", "Lord of the Sealed Realm", "封界之主",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("end of Sealed Realm arc"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Became Lord of the Sealed Realm; reset the Realm-Sealing Grand Array."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Became Lord of the Sealed Realm and reset the Realm-Sealing Grand Array at end of arc.",
                List.of("Became Lord of the Sealed Realm",
                        "Reset the Realm-Sealing Grand Array"),
                List.of("title", "sealed_realm", "realm_sealing_grand_array", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "cave_world_owner", "Cave World Owner", "洞天之主",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("after killing Seven-Colored Daoist"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Killed the Seven-Colored Daoist to become the new world-owner."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Killed the Seven-Colored Daoist, became the new owner of the Cave World. Renamed it 'Wang Lin's Cave World'.",
                List.of("Killed the Seven-Colored Daoist",
                        "Renamed the Cave World to 'Wang Lin's Cave World'"),
                List.of("title", "cave_world", "seven_colored_daoist", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "tenth_sun_iac", "Tenth Sun of the Immortal Astral Continent", "仙罔大陆第十日",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("after slaying Gu Dao"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Achieved after slaying Gu Dao; second strongest by sun size."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Achieved after slaying Gu Dao to claim #1. Second strongest of the 10 suns by sun size. Condensed via the tricolor (black/white/gold) Great Heavenly Venerable Sun.",
                List.of("Became the 10th Sun of the IAC",
                        "Second strongest by sun size",
                        "Achieved by slaying Gu Dao"),
                List.of("title", "iac", "gu_dao", "grand_empyrean", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "celestial_ancestor_fourth_child", "Ancient Ancestor's Fourth Child", "古祖第四子",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ancient Race Heavenly Blood Calamity"), 5,
                        "Wang Lin's soul transformed into an Ancient Order Soul close to the Ancient Ancestor."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Soul transformed to Ancient Order Soul — considered the Ancient Ancestor's Fourth Child."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Soul-Devourer nature obtained Soul Blood from the Ancient Race Heavenly Blood Calamity, transforming his soul into an Ancient Order Soul close to the Ancient Ancestor — considered the Ancient Ancestor's Fourth Child.",
                List.of("Soul transformed to Ancient Order Soul",
                        "Considered the Ancient Ancestor's Fourth Child"),
                List.of("title", "ancient_clan", "ancient_ancestor", "soul_devourer", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "wang_lin_cave_world_renamer", "Renamer of the Cave World", "王林洞天",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("after becoming Cave World owner"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The renaming is Wang Lin's act as new owner."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Renamed the Cave World to 'Wang Lin's Cave World' after becoming its owner.",
                List.of("Renamed the Cave World to 'Wang Lin's Cave World'"),
                List.of("title", "cave_world", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "paragon_wang", "Paragon Wang (AWWP cross-novel title)", "王尊",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("A World Worth Protecting", List.of("AWWP Ch.60s & Ch.69"), 5,
                        "Cross-novel — Wang Lin as AWWP protagonist's mentor."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Cross-novel title; Wang Lin is the mentor of AWWP's Wang Baole (his son-in-law)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mentored/protected Wang Baole (AWWP protagonist; Wang Yiyi's husband) under the title 'Paragon Wang'.",
                List.of("Cross-novel mentor title",
                        "Mentored Wang Baole in AWWP"),
                List.of("title", "cross_novel", "awwp", "wang_baole", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "immortal_lord_title", "Immortal Lord Title (Huangtian Realm)", "仙尊",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Three Inches of Paradise cross-novel"), 5,
                        "Cross-novel — at the end of Three Inches of Paradise (Huangtian Realm)."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Cross-novel resonance title."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Per Baidu: at the end of Three Inches of Paradise (Huangtian Realm) — can break through the Star Ring's obstacles and ascend to Huangtian (corresponds to Immortal Lord Realm of the Huangtian Star Ring).",
                List.of("Cross-novel Huangtian Realm resonance",
                        "Immortal Lord Realm correspondence"),
                List.of("title", "cross_novel", "huangtian", "wang_lin_unique")
        ));
    }
}
