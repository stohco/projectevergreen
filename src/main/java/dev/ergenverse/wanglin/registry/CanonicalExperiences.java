package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalExperiences — Wang Lin's defining life experiences.
 *
 * <p>Distinct from techniques / items / relationships. These are the events
 * that SHAPED Wang Lin: the Teng Clan annihilation, 7 years at Restriction
 * Mountain, the Reincarnation Pool meditation, the killing of Daoist Water,
 * storing Li Muwan's Nascent Soul, etc. Each experience drives a personality
 * trait and a teaching gate.
 */
public final class CanonicalExperiences extends AbstractSubRegistry {

    public CanonicalExperiences() {
        super("CanonicalExperiences");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "exp_teng_clan_annihilation", "Teng Clan Annihilation", "灭门之祸",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("E08-E14 (mortal origin → Teng Clan trauma)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining trauma. The root of his caution and his resolve."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Teng Huayuan annihilated the Wang Clan to seize Wang Lin's soul for divination. Wang Lin's father (Wang Tianshui) and mother (Zhou Tingsu) were killed; their souls were trapped in Teng's soul flag. Wang Lin's cousin Wang Zhuo killed his forced wife and himself rather than betray the clan. This trauma forged Wang Lin's caution, his hatred of being used, and his protective instinct toward mortals.",
                List.of("Forged Wang Lin's caution",
                        "Killed his parents (Teng Huayuan)",
                        "Wang Zhuo's suicide-by-betrayal",
                        "Situ Nan rescued the parents' souls into the Heaven-Defying Bead"),
                List.of("experience", "trauma", "teng_huayuan", "wang_clan", "caution", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_restriction_mountain_seven_years", "7 Years at Restriction Mountain", "古禁山七年",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~179-180"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining patience trial. The basis of his Restriction teaching gate."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin spent seven years studying in the Land of the Ancient God Restrictions Mountain Trial — he became the 4th person ever to complete the trial. This is the canon basis for his teaching gate: anyone learning restriction from him must demonstrate equivalent patience.",
                List.of("7-year patience trial",
                        "4th person ever to complete",
                        "Triggered the Tu Si Restriction Flag inheritance",
                        "Canon basis for the Restriction teaching gate"),
                List.of("experience", "patience_trial", "tu_si", "ancient_god", "restriction_mountain", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_li_muwan_death", "Death of Li Muwan", "李慕婉之死",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("E28", "Ch. ~500 Nascent Soul fail"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining grief. The entire late-game motivation."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Li Muwan failed Nascent Soul formation multiple times; died at 500 years old. Wang Lin stored her Nascent Soul in the Heaven-Defying Bead for 700 years. He attempted the Qi Xi Spell (sacrificing 50% of his life force) — it failed (didn't pass the 4th day out of 7). This grief drove his entire late-game arc: becoming the Cave World owner, crossing the Heaven Trampling Bridges, transcending with her.",
                List.of("Stored her Nascent Soul in the Heaven-Defying Bead 700 years",
                        "Qi Xi Spell failed (sacrificed 50% life force)",
                        "Drove the entire late-game arc"),
                List.of("experience", "grief", "li_muwan", "qi_xi_spell", "heaven_defying_bead", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_reincarnation_pool_thirteen_years", "13 Years at the Reincarnation Pool", "轮回池十三年",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1943 (potential)", "Ch. 2087 (complete)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The 14th Essence comprehension — achieved by 13 years of meditation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin meditated 13 years at the Reincarnation Pool to comprehend the Reincarnation Essence (his 14th). Completing it = Heaven Trampling achieved without crossing the 9th bridge.",
                List.of("13-year meditation",
                        "Achieved the 14th Essence (Reincarnation)",
                        "Heaven Trampling achieved (Ch. 2087)"),
                List.of("experience", "reincarnation_pool", "essence", "wang_lin_unique", "heaven_trampling")
        ));

        register(CanonicalEntry.of(
                "exp_killing_daoist_water", "Killing Daoist Water", "杀水道人",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Cloud Sea Star System"), 5,
                        "Completed Slaughter Essence here."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining victory; completed Slaughter Essence here."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin slew Daoist Water in the Cloud Sea Star System. The site where Wang Lin completed his Slaughter Essence. Daoist Water had destroyed many of Wang Lin's treasures (God-Slaying Sword Ch. 1273, Ancient God Trident Ch. 1277, Seven-Colored Nail Ch. 1082) — all later restored (Ch. 1626 via Void Gate power).",
                List.of("Slew Daoist Water in Cloud Sea Star System",
                        "Completed Slaughter Essence here",
                        "Restored destroyed treasures via Void Gate (Ch. 1626)"),
                List.of("experience", "slaughter_essence", "daoist_water", "cloud_sea", "void_gate")
        ));

        register(CanonicalEntry.of(
                "exp_killing_all_seer", "Killing the All-Seer", "杀全知者",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid-late era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining revenge against the false mentor."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The All-Seer was Wang Lin's false mentor — patriarch of the Heavenly Fate Sect, plotted to absorb source origins of Wang Lin, Ling Tianhou, and Blood Ancestor. Taught Wang Lin the Celestial Slaughter Art with a trap inside. Wang Lin turned the trap back on him and killed him.",
                List.of("Killed the false mentor",
                        "Turned the Celestial Slaughter Art trap back on him"),
                List.of("experience", "revenge", "all_seer", "heavenly_fate_sect", "celestial_slaughter_art")
        ));

        register(CanonicalEntry.of(
                "exp_killing_seven_colored_daoist", "Killing the Seven-Colored Daoist", "杀七彩道人",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("end of Cave World arc"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining cosmic act — became the new Cave World owner."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin killed the Seven-Colored Daoist (the Cave World's owner-farmer). Became the new owner of the Cave World. Renamed it 'Wang Lin's Cave World'. The defining act that ended his Cave World arc.",
                List.of("Killed the Cave World owner",
                        "Became the new Cave World owner",
                        "Renamed to 'Wang Lin's Cave World'"),
                List.of("experience", "cave_world_owner", "seven_colored_daoist", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_killing_gu_dao", "Slaying Gu Dao", "杀古道",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining IAC victory — became the Tenth Sun."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin slew Gu Dao (the strongest sun by size) to claim the #1 position. Became the Tenth Sun of the Immortal Astral Continent, second strongest by sun size.",
                List.of("Slew Gu Dao (strongest by size)",
                        "Became the Tenth Sun of the IAC",
                        "Second strongest by sun size"),
                List.of("experience", "iac", "gu_dao", "tenth_sun", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_soul_devourer_awakening", "Soul-Devourer Nature Awakening", "噬魂者觉醒",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 119"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Innate soul-nature — not transferable."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Soul contains the Soul-Devourer nature (Ch. 119). Manifests as a Soul Gem. Soul-devourer with a soul gem could create wandering souls. Evolution: Soul Gem (Ch. 193) → devours Ancient Thunder Dragon (Greed's Second Origin Soul) at Ch. 650 → absorbs Heavenly Flame (Ch. 1021) → fuses with Devilish Flames, Fire Dragon, and Flame Dragon (Ch. 1068) → obtains Soul Blood from Ancient Race Heavenly Blood Calamity, transforming his soul into an Ancient Order Soul close to the Ancient Ancestor — considered the Ancient Ancestor's Fourth Child.",
                List.of("Soul-Devourer nature (innate)",
                        "Manifests as Soul Gem (Ch. 193)",
                        "Devoured Ancient Thunder Dragon (Ch. 650)",
                        "Absorbed Heavenly Flame (Ch. 1021)",
                        "Transformed to Ancient Order Soul"),
                List.of("experience", "soul_devourer", "soul_gem", "wang_lin_unique", "ancient_ancestor")
        ));

        register(CanonicalEntry.of(
                "exp_mortal_lifetime", "The Mortal Lifetime (Wang Lin's Domain)", "凡人一世",
                CanonicalCategory.LIFE_AND_DEATH,
                Provenance.explicit("Renegade Immortal", List.of("Ascendant era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining Dao-comprehension experience."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Wang Lin lived as a mortal among mortals to comprehend the Life-Death Essence. This lifetime of mortality was the basis for his Life-Death Domain and Life-Death Essence comprehension.",
                List.of("Lived as a mortal among mortals",
                        "Comprehended Life-Death Essence",
                        "Basis for Life-Death Domain"),
                List.of("experience", "mortal_lifetime", "life_death_essence", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "exp_situ_nan_mentorship", "Situ Nan Mentorship (Bead Spirit)", "司徒南师承",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 8+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan was the bead's spirit and Wang Lin's first mentor."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Situ Nan (the remnant soul of the 2nd-Generation Vermilion Bird) lived inside the Heaven-Defying Pearl. He became Wang Lin's first mentor — transmitted the Underworld Ascension Method + Vermilion Bird Burning Heaven Art. Sacrificed his remaining power to save Wang Lin; later reincarnated on IAC as 'Si Nan' Grand Marshal of Wu Xuan Country.",
                List.of("Bead-spirit mentor",
                        "Transmitted Underworld Ascension Method + Vermilion Bird Burning Heaven Art",
                        "Sacrificed remaining power to save Wang Lin",
                        "Reincarnated on IAC as 'Si Nan'"),
                List.of("experience", "situ_nan", "vermilion_bird", "underworld_ascension", "mentor")
        ));

        register(CanonicalEntry.of(
                "exp_tu_si_inheritance", "Tu Si Ancient God Inheritance", "涂司古神传承",
                CanonicalCategory.HISTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si inheritance — knowledge portion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tu Si (Ancient God 8-Star) — his body became the Land of the Ancient God (3-level Chaotic Broken Stars realm). Granted Wang Lin the 'Great Enlightened One' title and 'knowledge' inheritance. Tuo Sen inherited his 'power' inheritance (born from Tu Si's failed Ink Flow Split Soul Technique).",
                List.of("Tu Si = Ancient God 8-Star",
                        "Body became the Land of the Ancient God",
                        "Granted 'Great Enlightened One' title",
                        "Tuo Sen inherited 'power' portion"),
                List.of("experience", "tu_si", "ancient_god", "inheritance", "great_enlightened_one", "tuo_sen")
        ));
    }
}
