package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalHistoricalEvents — Wang Lin's life events (the timeline).
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.RITimelineEngine} (the 108-event
 * spine of RI history). This registry surfaces the subset of events that
 * directly involve Wang Lin's personal arc.
 */
public final class CanonicalHistoricalEvents extends AbstractSubRegistry {

    public CanonicalHistoricalEvents() {
        super("CanonicalHistoricalEvents");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "event_bead_discovery", "Heaven-Defying Bead Discovery", "得逆天珠",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 8"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining childhood event."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Wang Lin discovered the Heaven-Defying Bead as a youth inside the Heng Yue Sect stone bead (found in a dead bird under a cliff). The bead's true origin: sent back through time by his future clone Lu Mo via Dream Dao (originally bestowed by Seven-Colored Immortal Venerable to Realm-Sealing Supreme).",
                List.of("Ch. 8 acquisition",
                        "Found inside the Heng Yue Sect stone bead",
                        "True origin: Lu Mo sent it back through time via Dream Dao"),
                List.of("event", "bead", "heng_yue_sect", "lu_mo", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "event_heng_yue_discipleship", "Heng Yue Sect Discipleship", "入恒岳派",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1-7"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's sect entry."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Wang Lin's fourth uncle Wang Tianshan risked his sect position to get Wang Lin into Heng Yue Sect. Wang Lin chose the Wealth flying sword from the armory as a disciple. Sect rule: 'whoever chooses this sword must treat it well — if it breaks, repair immediately, if sold, exiled from sect.'",
                List.of("Entered via Wang Tianshan's risk",
                        "Chose the Wealth flying sword",
                        "Sect-rule soul-binding contract"),
                List.of("event", "heng_yue_sect", "wang_tianshan", "wealth_flying_sword", "discipleship")
        ));

        register(CanonicalEntry.of(
                "event_ji_realm_awakening", "Ji Realm Awakening", "极境觉醒",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 127"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's first killing divine sense."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Awakened through the trauma of the Wang Clan's annihilation + the Underworld Ascension Method's catalyst. The 'Extreme Realm' is a unique cultivator-state where Spiritual Energy refines into Divine Sense. Conflicts with breakthroughs (the Ji Realm curse).",
                List.of("Awakened via trauma + Underworld Ascension Method",
                        "Spiritual Energy → Divine Sense",
                        "Conflicts with breakthroughs (the curse)"),
                List.of("event", "ji_realm", "trauma", "wang_clan_annihilation", "underworld_ascension")
        ));

        register(CanonicalEntry.of(
                "event_ancient_god_inheritance", "Tu Si Ancient God Inheritance", "涂司古神传承",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si's knowledge inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin received Tu Si's 'knowledge' inheritance (Tuo Sen received the 'power' inheritance). Granted the 'Great Enlightened One' title. Triggered the 1★→27★ Ancient God body reconstruction progression. Hair turned white after absorbing Tu Si's legacy.",
                List.of("Granted 'Great Enlightened One' title",
                        "Triggered 1★→27★ Ancient God progression",
                        "Hair turned white"),
                List.of("event", "tu_si", "ancient_god", "inheritance", "great_enlightened_one")
        ));

        register(CanonicalEntry.of(
                "event_qing_shui_brotherhood", "Sworn Brotherhood with Qing Shui", "与清水结拜",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's sworn brotherhood with Qing Shui."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Wang Lin and Qing Shui became sworn brothers. Qing Shui used the Ji Realm to kill Russell for offending Wang Lin. Qing Shui later bequeathed the Slaughter Sword to Wang Lin — fused with the Slaughter Crystal to complete Wang Lin's Slaughter Essence.",
                List.of("Sworn brotherhood",
                        "Qing Shui killed Russell for offending Wang Lin",
                        "Slaughter Sword bequeathed"),
                List.of("event", "qing_shui", "sworn_brother", "ji_realm", "slaughter_sword")
        ));

        register(CanonicalEntry.of(
                "event_soul_refining_sect_inheritance", "Soul Refining Sect Inheritance", "炼魂宗传承",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Dun Tian gave Wang Lin the Ten Billion Soul Banner and sect inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dun Tian (Soul Refining Sect ancestor) gave Wang Lin the Ten Billion Soul Banner and the Soul Flag Production Method (Soul Refining, Soul Extracting, Soul Sealing). Dun Tian erased his own consciousness to become a soul within the Soul Banner.",
                List.of("Received Ten Billion Soul Banner",
                        "Received Soul Flag Production Method (3 parts)",
                        "Dun Tian erased consciousness to become a soul"),
                List.of("event", "soul_refining_sect", "dun_tian", "billion_soul_flag", "inheritance")
        ));

        register(CanonicalEntry.of(
                "event_li_muwan_nascent_soul_storage", "Storing Li Muwan's Nascent Soul", "存李慕婉元神",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("E28"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The defining grief-driven act."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "After Li Muwan's body perished, Wang Lin stored her Nascent Soul inside the Heaven-Defying Bead to preserve her until he could rebuild her a body. This is the entire motivation for Wang Lin's late-game actions (becoming the Cave World owner, crossing the Heaven Trampling Bridges).",
                List.of("Stored her Nascent Soul in the Heaven-Defying Bead",
                        "Drove the entire late-game arc",
                        "Stored for 700 years"),
                List.of("event", "li_muwan", "heaven_defying_bead", "wang_lin_unique", "motivation")
        ));

        register(CanonicalEntry.of(
                "event_killing_all_seer", "Killing the All-Seer", "杀全知者",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid-late era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Defining revenge."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin turned the trap in the Celestial Slaughter Art back on the All-Seer and killed him. The All-Seer had plotted to absorb source origins of Wang Lin, Ling Tianhou, and Blood Ancestor.",
                List.of("Killed the false mentor",
                        "Trap turned back"),
                List.of("event", "all_seer", "celestial_slaughter_art", "wang_lin_killed", "revenge")
        ));

        register(CanonicalEntry.of(
                "event_killing_seven_colored_daoist", "Killing the Seven-Colored Daoist", "杀七彩道人",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("end of Cave World arc"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Defining cosmic act — became Cave World owner."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin killed the Seven-Colored Daoist (the Cave World's owner-farmer). Became the new owner of the Cave World. Renamed it 'Wang Lin's Cave World'.",
                List.of("Became new Cave World owner",
                        "Renamed to 'Wang Lin's Cave World'"),
                List.of("event", "seven_colored_daoist", "cave_world", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "event_4th_bridge_crossing", "Crossing the 4th Heaven Trampling Bridge", "踏天桥第四",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("4th bridge event"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining 4th-bridge crossing."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 4th bridge turned into countless specks of light that devoured Wang Lin — he woke up. Crossing granted newfound cultivation; removed Ancestral curse from Celestial Ancestor's Head; absorbed the head into the sun using Lian Daozhan's soul as guide. This crossing produced the tricolor (black/white/gold) Great Heavenly Venerable Sun.",
                List.of("Bridge turned to specks of light",
                        "Removed Ancestral curse from Celestial Ancestor's Head",
                        "Used Lian Daozhan's soul as guide",
                        "Produced tricolor Great Heavenly Venerable Sun"),
                List.of("event", "heaven_trampling", "4th_bridge", "celestial_ancestor", "grand_empyrean_sun")
        ));

        register(CanonicalEntry.of(
                "event_reincarnation_essence_completion", "Reincarnation Essence Completion", "轮回源成",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2087"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The defining final act — achieved Heaven Trampling."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "After 13 years of meditation at the Reincarnation Pool, Wang Lin fully comprehended the Reincarnation Essence (his 14th). The moment he completed it, he achieved Heaven Trampling — bypassing the 9th bridge entirely. Transcends with Li Muwan.",
                List.of("13-year meditation completed",
                        "Achieved Heaven Trampling (Ch. 2087)",
                        "Bypassed the 9th bridge",
                        "Transcends with Li Muwan"),
                List.of("event", "reincarnation_essence", "heaven_trampling", "wang_lin_unique", "transcendence")
        ));

        register(CanonicalEntry.of(
                "event_becoming_tenth_sun", "Becoming the Tenth Sun of IAC", "成仙罔第十日",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining IAC act."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin slew Gu Dao (the strongest sun by size) to claim the #1 position. Became the Tenth Sun of the Immortal Astral Continent, second strongest by sun size. The tricolor (black/white/gold) Great Heavenly Venerable Sun was the marker.",
                List.of("Slew Gu Dao",
                        "Became Tenth Sun of IAC",
                        "Second strongest by sun size"),
                List.of("event", "iac", "gu_dao", "tenth_sun", "grand_empyrean")
        ));

        register(CanonicalEntry.of(
                "event_qi_xi_spell_attempt", "Qi Xi Spell Resurrection Attempt", "乞夕术复活",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("implicit (Baidu)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Failed resurrection attempt — drove later arcs."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin used the Qi Xi Spell on himself, sacrificing 50% of his life force to resurrect Li Muwan and form flesh out of her Nascent Soul. Failed to fully recover Li Muwan — didn't pass the 4th day out of 7. This failure drove Wang Lin to seek the 4th Step / Heaven Trampling as the only path to revive her properly.",
                List.of("Sacrificed 50% of life force",
                        "Failed (Li Muwan didn't pass 4th day of 7)",
                        "Drove Wang Lin to seek Heaven Trampling"),
                List.of("event", "qi_xi_spell", "li_muwan_revival", "failure", "heaven_trampling_motivation")
        ));

        register(CanonicalEntry.of(
                "event_dream_dao_bead_send_back", "Lu Mo Sends Bead Back via Dream Dao", "路摩梦道送珠",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1295+ (Dream Dao)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Time-loop bootstrap event."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Lu Mo (Wang Lin's Slaughter True Body, who achieved the Fourth Step) borrowed the Realm-Defining Compass from Old Man Miēshēng and blasted the Heaven-Defying Pearl open via Dream Dao, sending it back through time to the main body (Wang Lin as a youth). Lu Mo also left information in the White Hair Strand and Infant Skull to resolve the main body's confusion about reincarnation.",
                List.of("Lu Mo borrowed Realm-Defining Compass from Old Man Miēshēng",
                        "Blasted the bead open via Dream Dao",
                        "Sent it back through time",
                        "Left info in White Hair Strand and Infant Skull"),
                List.of("event", "lu_mo", "dream_dao", "time_travel", "miēshēng", "wang_lin_unique")
        ));
    }
}
