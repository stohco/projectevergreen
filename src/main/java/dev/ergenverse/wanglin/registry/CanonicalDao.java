package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalDao — Wang Lin's Dao comprehension track.
 *
 * <p>The umbrella nodes: Samsara Dao (the 14 Essences + 9 Heaven Trampling
 * Bridges), Heaven Trampling (the 4th Step), and the four Dao-spell pillars
 * (Sundered Night, Flowing Time, Dream Dao, Rain and Wind World).
 */
public final class CanonicalDao extends AbstractSubRegistry {

    public CanonicalDao() {
        super("CanonicalDao");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "samsara_dao", "Samsara Dao (Wang Lin's signature Dao)", "轮回道",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 127 (1st essence)", "Ch. 2087 (14th essence)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's signature Dao — the 14 Essences + 9 Heaven Trampling Bridges. The 14th Essence (Reincarnation) IS Wang Lin's path."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's signature Dao. The 14 Essences (6 substantial + 4 virtual + 4 special) + the 9 Heaven Trampling Bridges. Completing all 14 Essences (esp. the 14th, Reincarnation) = Heaven Trampling.",
                List.of("14 Essences + 9 Heaven Trampling Bridges",
                        "Completing all 14 Essences = Heaven Trampling",
                        "The 14th (Reincarnation) bypasses the 9th Bridge"),
                List.of("dao", "samsara", "essences", "heaven_trampling_bridges", "wang_lin_signature", "wang_lin_unique")
        ));

        register(CanonicalEntry.withSpec(
                "heaven_trampling_bridges", "9 Heaven Trampling Bridges", "踏天桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("throughout late-game"), 5,
                        "Per-bridge tribulation names attested by xian-ni.fandom.com/wiki/Half_Heaven_Trampling: 1st=Universal Law & Origin Soul Fusion; 2nd=Dao Heart Tribulation; 3rd=Inner Demon Tribulation; 4th=Unknown; 5th=Transcending Reincarnation; 6th=Unknown; 7th=Unknown; 8th=Unknown; 9th=Unknown (leads to Heaven Trampling realm)."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's bridge-crossing history; the 9th bridge is bypassed by Reincarnation Essence comprehension."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 9 Heaven Trampling Bridges (踏天桥): 1st tests sturdiness of heart; 2nd glimpse of Heaven Trampling power; 3rd close mind off inner demons (Wang Lin EMBRACED them instead); 4th bridge turned to specks of light that devoured him (woke up); 5th crossed via Heaven Trampling step vision; 6th & 7th crossed; 8th STOPPED; 9th NOT STEPPED ON — Wang Lin achieved Heaven Trampling without crossing, the moment he comprehended the Reincarnation Essence. Per-bridge canon tribulation names (xian-ni wiki): 1st=Universal Law & Origin Soul Fusion; 2nd=Dao Heart Tribulation; 3rd=Inner Demon Tribulation; 4th=Unknown; 5th=Transcending Reincarnation; 6th=Unknown; 7th=Unknown; 8th=Unknown; 9th=Unknown (leads to Heaven Trampling realm).",
                List.of("1st: tests sturdiness of heart (tribulation name: Universal Law & Origin Soul Fusion)",
                        "2nd: glimpse of Heaven Trampling power (tribulation name: Dao Heart Tribulation)",
                        "3rd: Wang Lin EMBRACED his inner demons (tribulation name: Inner Demon Tribulation)",
                        "4th: bridge devoured him (woke up) (tribulation name: UNKNOWN per xian-ni wiki)",
                        "5th: crossed via Heaven Trampling step vision (tribulation name: Transcending Reincarnation)",
                        "6th: crossed (tribulation name: UNKNOWN per xian-ni wiki)",
                        "7th: crossed (tribulation name: UNKNOWN per xian-ni wiki)",
                        "8th: STOPPED — couldn't cross (tribulation name: UNKNOWN per xian-ni wiki)",
                        "9th: NOT STEPPED ON — bypassed via Reincarnation Essence (tribulation name: UNKNOWN — bridge leads to Heaven Trampling realm)"),
                List.of("dao", "heaven_trampling", "bridges", "wang_lin_unique", "reincarnation_essence"),
                "heaven_trampling_bridges"
        ));

        register(CanonicalEntry.of(
                "heaven_trampling", "Heaven Trampling (Fourth Step)", "踏天境",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2087 (Revealed)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The Fourth Step — achieved by completing the Reincarnation Essence. Cannot be transferred."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin achieved Heaven Trampling without stepping on any more bridges. He reached this level the moment he fully comprehended the Reincarnation Essence. Through the conflict between soul, divine sense, and Dao charm, can open up a world within the illusory, forming a cycle of reincarnation within this world. Those who leap beyond this Dao realm, forging a new path, are the ones who Tread Heaven from the other shore. Walking a single Dao to its end and becoming its source is the Fourth Step.",
                List.of("Achieved via Reincarnation Essence completion (Ch. 2087)",
                        "Opens a world within the illusory",
                        "Forms a cycle of reincarnation within that world",
                        "Walking a single Dao to its end and becoming its source"),
                List.of("dao", "fourth_step", "transcendence", "wang_lin_unique", "reincarnation_essence")
        ));

        register(CanonicalEntry.of(
                "dao_karma", "Karma Dao (cause-effect)", "因果道",
                CanonicalCategory.KARMA,
                Provenance.explicit("Renegade Immortal", List.of("3rd essence era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Anchors Karma Domain + Karma Whip."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Karma Dao — the law of karmic consequence. Comprehension lets him see karmic threads and manipulate them. Anchors Karma Domain + Karma Whip.",
                List.of("See karmic threads",
                        "Anchors Karma Domain + Karma Whip"),
                List.of("dao", "karma", "wang_lin_created", "karma_domain", "karma_whip")
        ));

        register(CanonicalEntry.of(
                "dao_time", "Time Dao", "时道",
                CanonicalCategory.TIME,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1245 (Flowing Time)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Anchors Flowing Time + Time Restriction."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Time Dao. Comprehended at the gate of the Ancient Immortal Domain in the Wind Immortal World. Can reverse time.",
                List.of("Anchors Flowing Time + Time Restriction",
                        "Can reverse time"),
                List.of("dao", "time", "wang_lin_created", "flowing_time", "time_restriction")
        ));

        register(CanonicalEntry.of(
                "dao_life_death", "Life-Death Dao", "生死道",
                CanonicalCategory.LIFE_AND_DEATH,
                Provenance.explicit("Renegade Immortal", List.of("4th essence era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended via Wang Lin's Domain (living as a mortal among mortals)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Life-Death Dao. Comprehended via Wang Lin's Domain (living as a mortal among mortals). Anchors Life-Death Domain + Underworld River.",
                List.of("Anchors Life-Death Domain + Underworld River",
                        "Comprehended via living as a mortal"),
                List.of("dao", "life_death", "wang_lin_created", "underworld_river", "life_death_domain")
        ));

        register(CanonicalEntry.of(
                "dao_slaughter", "Slaughter Dao", "杀戮道",
                CanonicalCategory.SLAUGHTER,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1509 (initial)", "Ch. 1622 (complete)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Comprehended through a lifetime of killing."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's first major Dao. Comprehended through a lifetime of killing (initial: slaying Daoist Water; complete: consuming Qing Shui's Black Sword).",
                List.of("First major Dao",
                        "Initial: slaying Daoist Water",
                        "Complete: consuming Qing Shui's Black Sword"),
                List.of("dao", "slaughter", "wang_lin_created", "qing_shui", "daoist_water")
        ));

        // ── The 9 Heaven-Trampling Bridges (B01–B09) ─────────────────
        register(CanonicalEntry.of(
                "bridge_1st_heaven_trampling", "1st Heaven-Trampling Bridge", "踏天桥·第一桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 5,
                        "Cross-refs RICanonicalDatabase B01."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("1st of the 9 Heaven-Trampling Bridges; tests sturdiness of heart. The crossing is personal — no one can walk it for another."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "1st of the 9 Heaven-Trampling Bridges (踏天桥·第一桥). Tests the sturdiness of the cultivator's heart. Wang Lin crossed this bridge successfully.",
                List.of("Tribulation name: Universal Law & Origin Soul Fusion (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling)",
                        "1st of the 9 Heaven-Trampling Bridges",
                        "Tests sturdiness of heart",
                        "Wang Lin crossed successfully"),
                List.of("dao", "heaven_trampling", "bridge", "1st", "heart_test", "universal_law", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_2nd_heaven_trampling", "2nd Heaven-Trampling Bridge", "踏天桥·第二桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 5,
                        "Cross-refs RICanonicalDatabase B02."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("2nd of the 9 Heaven-Trampling Bridges; gives a glimpse of Heaven-Trampling power."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "2nd of the 9 Heaven-Trampling Bridges (踏天桥·第二桥). Gives the cultivator a glimpse of Heaven-Trampling power.",
                List.of("Tribulation name: Dao Heart Tribulation (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling)",
                        "2nd of the 9 Heaven-Trampling Bridges",
                        "Glimpse of Heaven-Trampling power",
                        "Wang Lin crossed successfully"),
                List.of("dao", "heaven_trampling", "bridge", "2nd", "dao_heart", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_3rd_heaven_trampling", "3rd Heaven-Trampling Bridge", "踏天桥·第三桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 5,
                        "Cross-refs RICanonicalDatabase B03."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("3rd of the 9 Heaven-Trampling Bridges. Wang Lin EMBRACED his inner demons instead of closing his mind to them — a unique crossing."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "3rd of the 9 Heaven-Trampling Bridges (踏天桥·第三桥). Tests the cultivator's reaction to inner demons. Standard method: close the mind off from them. Wang Lin's unique method: EMBRACED them instead.",
                List.of("Tribulation name: Inner Demon Tribulation (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling)",
                        "3rd of the 9 Heaven-Trampling Bridges",
                        "Tests reaction to inner demons",
                        "Standard: close mind off; Wang Lin: EMBRACED them"),
                List.of("dao", "heaven_trampling", "bridge", "3rd", "inner_demon", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_4th_heaven_trampling", "4th Heaven-Trampling Bridge", "踏天桥·第四桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 5,
                        "Cross-refs RICanonicalDatabase B04."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("4th of the 9 Heaven-Trampling Bridges. Lian Daofei guided Wang Lin across."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "4th of the 9 Heaven-Trampling Bridges (踏天桥·第四桥). The bridge turned to specks of light that devoured him; he woke up. Lian Daofei served as guide.",
                List.of("Tribulation name: UNKNOWN (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling — wiki lists 4th as 'Unknown')",
                        "4th of the 9 Heaven-Trampling Bridges",
                        "Bridge turned to specks of light that devoured him",
                        "Wang Lin woke up",
                        "Lian Daofei guided the crossing"),
                List.of("dao", "heaven_trampling", "bridge", "4th", "lian_daofei", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_5th_heaven_trampling", "5th Heaven-Trampling Bridge", "踏天桥·第五桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2063 (Extreme Land/Sky/Life Daos)", "Ch. 2064 (One Step)"), 5,
                        "Cross-refs RICanonicalDatabase B05."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("5th of the 9 Heaven-Trampling Bridges. Wang Lin crossed via Heaven-Trampling Step vision; comprehended Extreme Land/Sky/Life Dao and One Step to Trample the Heavens here."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "5th of the 9 Heaven-Trampling Bridges (踏天桥·第五桥). Wang Lin crossed via Heaven-Trampling Step vision. Comprehended the 6th, 7th, and 8th of the Eight Extreme Daos (Extreme Land Dao, Extreme Sky Dao, Extreme Life Dao) here, plus One Step to Trample the Heavens.",
                List.of("Tribulation name: Transcending Reincarnation (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling)",
                        "5th of the 9 Heaven-Trampling Bridges",
                        "Crossed via Heaven-Trampling Step vision",
                        "Comprehended Extreme Land / Sky / Life Daos here",
                        "Comprehended One Step to Trample the Heavens here (Ch. 2064)"),
                List.of("dao", "heaven_trampling", "bridge", "5th", "extreme_daos", "one_step_trample_heavens", "transcending_reincarnation", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_6th_heaven_trampling", "6th Heaven-Trampling Bridge", "踏天桥·第六桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 4,
                        "Cross-refs RICanonicalDatabase B06."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("6th of the 9 Heaven-Trampling Bridges. Wang Lin crossed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "6th of the 9 Heaven-Trampling Bridges (踏天桥·第六桥). Wang Lin crossed successfully.",
                List.of("Tribulation name: UNKNOWN (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling — wiki lists 6th as 'Unknown')",
                        "6th of the 9 Heaven-Trampling Bridges",
                        "Wang Lin crossed successfully"),
                List.of("dao", "heaven_trampling", "bridge", "6th", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_7th_heaven_trampling", "7th Heaven-Trampling Bridge", "踏天桥·第七桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 5,
                        "Cross-refs RICanonicalDatabase B07."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("7th of the 9 Heaven-Trampling Bridges. Wang Lin crossed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "7th of the 9 Heaven-Trampling Bridges (踏天桥·第七桥). Wang Lin crossed successfully.",
                List.of("Tribulation name: UNKNOWN (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling — wiki lists 7th as 'Unknown')",
                        "7th of the 9 Heaven-Trampling Bridges",
                        "Wang Lin crossed successfully"),
                List.of("dao", "heaven_trampling", "bridge", "7th", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_8th_heaven_trampling", "8th Heaven-Trampling Bridge", "踏天桥·第八桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("late-game bridge crossing"), 4,
                        "Cross-refs RICanonicalDatabase B08."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("8th of the 9 Heaven-Trampling Bridges. Wang Lin STOPPED — he could not cross it."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "8th of the 9 Heaven-Trampling Bridges (踏天桥·第八桥). Wang Lin STOPPED here — he could not cross. He achieved Heaven Trampling by another path: comprehending the Reincarnation Essence (bypassing the 9th Bridge entirely).",
                List.of("Tribulation name: UNKNOWN (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling — wiki lists 8th as 'Unknown'; Wang Lin stopped before the tribulation)",
                        "8th of the 9 Heaven-Trampling Bridges",
                        "Wang Lin STOPPED — could not cross",
                        "Achieved Heaven Trampling by another path (Reincarnation Essence)"),
                List.of("dao", "heaven_trampling", "bridge", "8th", "stopped", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "bridge_9th_heaven_trampling", "9th Heaven-Trampling Bridge", "踏天桥·第九桥",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2087"), 5,
                        "Cross-refs RICanonicalDatabase B09."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("9th of the 9 Heaven-Trampling Bridges. Wang Lin did NOT step on it — he bypassed it via the Reincarnation Essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "9th of the 9 Heaven-Trampling Bridges (踏天桥·第九桥). Wang Lin did NOT step on this bridge — the moment he fully comprehended the Reincarnation Essence (Ch. 2087), he achieved Heaven Trampling directly, bypassing the 9th bridge entirely.",
                List.of("Tribulation name: UNKNOWN (per xian-ni.fandom.com/wiki/Heaven_Trampling — wiki describes the 9th bridge as leading to Heaven Trampling; Wang Lin bypassed it via Reincarnation Essence)",
                        "9th of the 9 Heaven-Trampling Bridges",
                        "Wang Lin did NOT step on it",
                        "Bypassed via Reincarnation Essence comprehension (Ch. 2087)",
                        "Achieved Heaven Trampling directly"),
                List.of("dao", "heaven_trampling", "bridge", "9th", "bypassed", "reincarnation_essence", "wang_lin_unique")
        ));

        // ── Eight Extreme Daos (cross-ref I160) ──────────────────────
        register(CanonicalEntry.of(
                "eight_extreme_daos", "Eight Extreme Daos (Eight Extremities Dao)", "八极道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1826", "Ch. 1958", "Ch. 2034", "Ch. 2063"), 5,
                        "Cross-refs RICanonicalDatabase I160."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended: first 5 by Empyrean Trial; last 3 on the 5th Heaven-Trampling bridge. Later passed to Wang Baole in AWWP."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Set of 8 Daos: Extreme Water, Metal, Wood, Earth, Fire, Heaven, Land, Life. First 5 = Five Elements; 6th & 7th = mutually opposing pair (Heaven/Land); 8th = life-bound Dao (Life). Third-Step-tier Eight Extremities Dao. Comprehended across Ch. 1826 (Extreme Fire), Ch. 1958 (Extreme Water), Ch. 2034 (Extreme Metal), and Ch. 2063 (Extreme Land/Sky/Life) on the 5th Heaven-Trampling bridge.",
                List.of("8 Daos: Water, Metal, Wood, Earth, Fire, Heaven, Land, Life",
                        "First 5 = Five Elements",
                        "6th & 7th = mutually opposing pair",
                        "8th = life-bound Dao",
                        "Third-Step-tier Eight Extremities Dao"),
                List.of("dao", "extreme_daos", "third_step", "wang_lin_created", "five_elements", "heaven_trampling_bridge_5")
        ));

        // ── One Step to Trample the Heavens ──────────────────────────
        register(CanonicalEntry.of(
                "one_step_trample_heavens", "One Step to Trample the Heavens", "一步踏天",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2064"), 5,
                        "Cross-refs RICanonicalDatabase T76 / I161."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended on the 5th Heaven-Trampling bridge (Ch. 2064)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Heaven-Trampling Dao. Comprehended on the 5th Heaven-Trampling bridge. The Fourth-Step-tier capstone — walking a single Dao to its end and becoming its source.",
                List.of("The Heaven-Trampling Dao",
                        "Comprehended on the 5th Heaven-Trampling bridge (Ch. 2064)",
                        "Fourth-Step-tier capstone",
                        "Walking a single Dao to its end and becoming its source"),
                List.of("dao", "heaven_trampling", "one_step", "fourth_step", "wang_lin_created", "wang_lin_unique")
        ));
    }
}
