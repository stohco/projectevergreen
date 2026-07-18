package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalBodies — Wang Lin's body-cultivation states & True Bodies.
 *
 * <p>Focuses on the Ancient God star-tier progression (1★→27★→Half-Heaven-Trampling→Heaven-Trampling)
 * and the Undying Ancient Body / Undying Ancient Finger derived from Lian Daofei's
 * Indestructible Immortal Body transmission.
 */
public final class CanonicalBodies extends AbstractSubRegistry {

    public CanonicalBodies() {
        super("CanonicalBodies");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.withSpec(
                "ancient_god_1_star", "1-Star Ancient God", "古神一星",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 199"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("First Ancient God reconstruction; Tu Si inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's first Ancient God star. True body reconstruction achieved via the Ancient God Tactic.",
                List.of("First Ancient God star (Ch. 199)",
                        "Achieved via Ancient God Tactic"),
                List.of("body", "ancient_god", "tu_si", "wang_lin_owned", "1_star"),
                "wang_lin_ancient_god_powers"
        ));

        register(CanonicalEntry.of(
                "ancient_god_3_star", "3-Star Ancient God", "古神三星",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 391"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si inheritance progression."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 3-Star Ancient God state (Ch. 391).",
                List.of("3-Star Ancient God"),
                List.of("body", "ancient_god", "wang_lin_owned", "3_star")
        ));

        register(CanonicalEntry.of(
                "ancient_god_7_star", "7-Star Ancient God", "古神七星",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1290 (pseudo)", "Ch. 1472", "Ch. 1538 (golden)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si inheritance progression."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 7-Star Ancient God state (pseudo Ch. 1290; confirmed Ch. 1472; golden Ch. 1538).",
                List.of("7-Star Ancient God",
                        "Pseudo → confirmed → golden progression"),
                List.of("body", "ancient_god", "wang_lin_owned", "7_star")
        ));

        register(CanonicalEntry.of(
                "ancient_clan_13_star", "13-Star Ancient Clan (7G/6D)", "古族十三星",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1539"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Clan ascension."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's first Ancient Clan state — 13 stars (7 Ancient God / 6 Ancient Demon) (Ch. 1539).",
                List.of("13-Star Ancient Clan",
                        "7 God / 6 Demon stars"),
                List.of("body", "ancient_clan", "ancient_god", "ancient_demon", "wang_lin_owned", "13_star")
        ));

        register(CanonicalEntry.of(
                "ancient_clan_24_star", "24-Star Ancient Clan (8G/8M/8D)", "古族二十四星",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1705"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Clan ascension."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 24-star Ancient Clan state — 8 God / 8 Demon / 8 Devil (Ch. 1705).",
                List.of("24-Star Ancient Clan",
                        "8 God / 8 Demon / 8 Devil stars"),
                List.of("body", "ancient_clan", "ancient_god", "ancient_demon", "ancient_devil", "wang_lin_owned", "24_star")
        ));

        register(CanonicalEntry.of(
                "ancient_clan_27_star", "27-Star Ancient Clan", "古族二十七星",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2003"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Peak Ancient Clan state (Ch. 2003)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's peak Ancient Clan state — 27 stars (9 God / 8 Demon / 9 Devil) (Ch. 2003).",
                List.of("Peak 27-Star Ancient Clan",
                        "9 God / 8 Demon / 9 Devil stars"),
                List.of("body", "ancient_clan", "wang_lin_owned", "27_star", "peak")
        ));

        register(CanonicalEntry.of(
                "undying_ancient_body", "Undying Ancient Body", "不灭古体",
                CanonicalCategory.BODY_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1728"), 5,
                        "Inspired by Lian Daofei's Indestructible Immortal Body transmission."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created, inspired by Lian Daofei's transmission."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's self-created undying body — inspired by Lian Daofei's Indestructible Immortal Body transmission (Ch. 1728).",
                List.of("Self-created undying body",
                        "Inspired by Lian Daofei's transmission"),
                List.of("body", "undying", "wang_lin_created", "lian_daofei", "immortal_body")
        ));

        register(CanonicalEntry.of(
                "undying_ancient_finger", "Undying Ancient Finger", "不灭古指",
                CanonicalCategory.BODY_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1639"), 5,
                        "Inspired by Lian Daofei's transmission."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created, inspired by Lian Daofei's transmission."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's self-created undying-finger divine ability (Ch. 1639). Inspired by Lian Daofei's Indestructible Immortal Body.",
                List.of("Self-created undying-finger divine ability",
                        "Inspired by Lian Daofei's transmission"),
                List.of("body", "undying", "finger", "wang_lin_created", "lian_daofei")
        ));

        register(CanonicalEntry.of(
                "indestructible_immortal_body", "Indestructible Immortal Body", "不灭仙体",
                CanonicalCategory.BODY_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("implied ~Ch. 1509+"), 4,
                        "Transmitted by Lian Daofei (madman, brother of Immortal Emperor Lian Daozhen) inside the Nether Beast."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Transmitted by Lian Daofei. Initially rejected by Wang Lin's Dao-Ancient bloodline."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Forges an undying physical body; later inspires Wang Lin's self-created Undying Ancient Finger (Ch. 1639) and Undying Ancient Body (Ch. 1728). Initially rejected by Wang Lin's Dao-Ancient bloodline. After awakening, Lian Daofei imparts numerous divine abilities alongside.",
                List.of("Forges an undying physical body",
                        "Initially rejected by Dao-Ancient bloodline",
                        "Inspired Undying Ancient Finger + Undying Ancient Body"),
                List.of("body", "undying", "lian_daofei", "inheritance", "immortal_body")
        ));
    }
}
