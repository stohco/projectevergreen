package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalTechniques — every technique Wang Lin learned, created, or inherited.
 *
 * <p>Sourced from CANON_RI_COMPLETE_TECHNIQUES.md (1,794 lines, ~120 techniques).
 * This registry surfaces the signature subset that demonstrates Wang Lin's
 * technique-comprehension arc: the Underworld Ascension Method, Vermilion Bird
 * Burning Heaven Art, Ancient God Tactic, Ji Realm Divine Sense, Restriction
 * Flags Refining Method, the 7 Original Spells (Sundered Night, Flowing Time,
 * Dream Dao, etc.), Soul Refining method, Slash Luo Art, and the four-finger
 * trio (Death / Demonic / Underworld / Yellow Springs).
 */
public final class CanonicalTechniques extends AbstractSubRegistry {

    public CanonicalTechniques() {
        super("CanonicalTechniques");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.withSpec(
                "underworld_ascension_method", "Underworld Ascension Method / Yellow Springs Ascension Art", "黄泉升窍诀",
                CanonicalCategory.UNDERWORLD_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 86 (Donghua S1 EP8)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taught by Situ Nan (the remnant soul inside the Heaven-Defying Pearl). Requires Situ Nan's explicit permission to redistribute (canon gate)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Divided into 9 layers. Uses places of extreme Yin as cultivation sites. The cultivator enters a near-death state, absorbs extreme-Yin spiritual power, and forms a Cold Dan (cold core) that dramatically boosts the success rate of forming a Golden Core. Also catalyst for reaching the Ji Realm.",
                List.of("Near-death cultivation state",
                        "Forms a Cold Dan (boosts Golden Core success)",
                        "Catalyst for the Ji Realm",
                        "Produces cold-attribute Blue Flames (Ch. 121)",
                        "Pairs with Yin Energy Detection Technique"),
                List.of("cultivation_art", "y_path", "cold_dan", "ji_realm", "situ_nan", "blue_flames", "underworld"),
                "situ_nan_inheritance"
        ));

        register(CanonicalEntry.of(
                "vermilion_bird_burning_heaven_art", "Vermilion Bird Burning Heaven Art", "朱雀焚天功",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("implied early"), 4,
                        "Named in situ_nan_baidu as one of the arts Situ Nan imparted."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("2nd-Gen Vermilion Bird lineage (Situ Nan transmitted). Requires the Vermilion Bird Sequence to fully unlock."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Foundational Vermilion Bird cultivation art that opens the path to the Vermilion Bird Mark awakenings (1st–4th). Feeds into the Vermilion Bird Mark awakenings, Ethereal Fire, and ultimately the Fire Essence / Fire Essence True Body.",
                List.of("Opens the Vermilion Bird Mark awakening path (1st–4th)",
                        "Feeds into Ethereal Fire and Fire Essence"),
                List.of("cultivation_art", "vermilion_bird", "fire", "situ_nan", "ethereal_fire")
        ));

        register(CanonicalEntry.withSpec(
                "ancient_god_tactic", "Ancient God Tactic", "古神诀",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Tu Si's memory legacy. The essence is one word: plunder."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Reconstructs the cultivator's body as an Ancient God. Outer-body cultivation track (main body sinks into the earth to absorb spiritual energy while the Avatar cultivates normally). Allows absorbing spiritual energy, pills, and inheritance fragments as stars. Progression: 1★ (Ch.199) → 27★ (Ch.2003) → Half-Heaven-Trampling (Ch.2062) → Heaven-Trampling (Ch.2087).",
                List.of("Main-body sinks into earth; Avatar cultivates normally",
                        "Absorbs spiritual energy, pills, inheritance fragments as stars",
                        "Hair turns white after absorbing Tu Si's legacy",
                        "Drives the 1★→27★ Ancient God progression"),
                List.of("cultivation_art", "ancient_god", "tu_si", "body_cultivation", "plunder", "inheritance", "star"),
                "wang_lin_ancient_god_powers"
        ));

        register(CanonicalEntry.of(
                "ji_realm_divine_sense", "Ji Realm / Extreme Realm Divine Sense", "极境神识",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 127"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Awakened through trauma (Wang Clan annihilation) + Underworld Ascension Method. The Ji Realm is a unique cultivator-state; not transferable."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A killing divine sense that lets Wang Lin kill any cultivator below Core Formation at FE stage, below Nascent Soul at Core Formation, below Soul Formation at Nascent Soul Peak. Qing Shui used the Ji Realm to kill Russell for offending Wang Lin. Eventually absorbed into Slaughter and into Ji Thunder (Ch. 1368 — one of his 9 accompanying thunders).",
                List.of("Killing divine sense — realm-suppressing",
                        "Awakened via trauma + Underworld Ascension Method",
                        "Conflicts with breakthroughs (curse)",
                        "Eventually absorbed into Ji Thunder (7th accompanying thunder, Ch. 1368)"),
                List.of("divine_sense", "killing", "trauma", "underworld_ascension", "ji_thunder", "wang_lin_unique")
        ));

        register(CanonicalEntry.withSpec(
                "restriction_flags_refining_method", "Restriction Flags Refining Method", "禁旗炼制法",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~180 (S1 EP32)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Inherited from Tu Si after passing the Restriction Mountain trial (4th person ever). Each flag requires 3 Ink Stones + 99,999 restrictions — duplicable in principle."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Explains the flag material made with inkstones; put 99,999 restrictions to complete. Each Restriction Flag is a portable formation-array. Wang Lin made three flags: (1) intentionally left incomplete to summon divine tribulation, (2) mixed restrictions, (3) pure attack restriction flag.",
                List.of("99,999 restrictions to complete a flag",
                        "Requires 3 Ink Stones per flag",
                        "Wang Lin made 3 distinct flags"),
                List.of("restriction", "tu_si", "inkstone", "forge", "wang_lin_created", "ancient_god"),
                "tu_si_remnants"
        ));

        register(CanonicalEntry.of(
                "ancient_restrictions", "Ancient Restrictions", "古禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~179-180 (S1 EP31)"), 5,
                        "Wang Lin spent 7 years studying at the Land of the Ancient God Restrictions Mountain Trial."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-taught via the trial — 7 years of study."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Foundation for all Wang Lin's restriction work. Wang Lin spent seven years studying in the Land of the Ancient God Restrictions Mountain Trial — he became the 4th person ever to complete the trial.",
                List.of("7-year study at Restriction Mountain",
                        "4th person ever to complete the trial",
                        "Foundation for all restriction work"),
                List.of("restriction", "ancient_god", "tu_si", "restriction_mountain", "seven_years")
        ));

        register(CanonicalEntry.of(
                "illusionary_circle", "Illusionary Circle", "幻阵",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 180 (S1 EP32)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-developed after 7 years of research."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wave-based restriction analysis tool — lets Wang Lin 'read' any restriction's structure and rules without seeing it. He didn't even need to look with his eyes; by examining the wave produced by the illusionary circle, he could understand the structure and rules of any restriction.",
                List.of("Wave-based restriction analysis",
                        "Reads restriction structure without visual sight"),
                List.of("restriction", "analysis", "wang_lin_created", "wave")
        ));

        register(CanonicalEntry.of(
                "annihilation_restriction", "Annihilation Restriction (1st Great Restriction)", "寂灭禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 754"), 5,
                        "Inherited half of restrictions heart from Li Yuan."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Obtained after inheriting half of restrictions heart from Li Yuan."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the four great restrictions. Annihilation-attribute; destroy-the-target restriction. Source of the 18 Plum Restriction. Combined with Heart Compass for full activation.",
                List.of("One of the Four Great Restrictions",
                        "Annihilation-attribute",
                        "Source of the 18 Plum Restriction"),
                List.of("restriction", "great_restriction", "annihilation", "li_yuan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "time_restriction", "Time Restriction (2nd Great Restriction)", "时禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1223"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the four great restrictions. Time-bending restriction.",
                List.of("One of the Four Great Restrictions",
                        "Time-bending"),
                List.of("restriction", "great_restriction", "time", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "life_death_restriction", "Life and Death Restriction (3rd Great Restriction)", "生死禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1229"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the four great restrictions. Life-death-attribute.",
                List.of("One of the Four Great Restrictions",
                        "Life-death-attribute"),
                List.of("restriction", "great_restriction", "life_death", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "ancient_soul_restriction", "Ancient Soul Restriction (4th Great Restriction)", "古魂禁",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1697"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended / inherited."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the four great restrictions. Ancient-soul-attribute.",
                List.of("One of the Four Great Restrictions",
                        "Ancient-soul-attribute"),
                List.of("restriction", "great_restriction", "ancient_soul", "wang_lin_owned")
        ));

        register(CanonicalEntry.withSpec(
                "soul_flag_production_method", "Soul Flag Production Method", "魂旗炼制法",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance (Dun Tian gift). Splits into Soul Refining, Soul Extracting, Soul Sealing."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Main cultivation method of the Soul Refining Sect — splits into three parts: Soul Refining, Soul Extracting, and Soul Sealing. Lets the user refine souls into a Soul Flag. Powers the One Billion Soul Banner.",
                List.of("Three-part method: Refine, Extract, Seal",
                        "Powers the One Billion Soul Banner",
                        "Inherited from Dun Tian (Soul Refining Sect)"),
                List.of("soul", "soul_refining_sect", "dun_tian", "inheritance", "soul_flag"),
                "soul_refining"
        ));

        register(CanonicalEntry.withSpec(
                "soul_refining", "Soul Refining", "炼魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Part of the Soul Flag Production Method (Soul Refining Sect)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Part of the Soul Flag Production Method (Soul Refining Sect). Refines captured souls.",
                List.of("Refines captured souls",
                        "Part of the Soul Refining Sect method"),
                List.of("soul", "soul_refining_sect", "dun_tian", "inheritance"),
                "soul_refining"
        ));

        register(CanonicalEntry.of(
                "soul_extracting", "Soul Extracting", "抽魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Part of the Soul Flag Production Method (Soul Refining Sect)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Part of the Soul Flag Production Method (Soul Refining Sect). Extracts souls from bodies.",
                List.of("Extracts souls from bodies",
                        "Part of the Soul Refining Sect method"),
                List.of("soul", "soul_refining_sect", "dun_tian", "inheritance")
        ));

        register(CanonicalEntry.of(
                "soul_sealing", "Soul Sealing", "封魂",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Part of the Soul Flag Production Method (Soul Refining Sect)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Part of the Soul Flag Production Method (Soul Refining Sect). Seals refined souls into the Soul Flag.",
                List.of("Seals refined souls into the Soul Flag",
                        "Part of the Soul Refining Sect method"),
                List.of("soul", "soul_refining_sect", "dun_tian", "inheritance", "soul_flag")
        ));

        register(CanonicalEntry.of(
                "slash_luo_art", "Slash Luo Art", "斩罗剑术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 567 (wiki)"), 5,
                        "Sword-technique contained in the Rain Celestial Sword."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Contained in the Rain Celestial Sword (Zhou Yi's gift, Ch. 717)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Sword-technique contained in the Rain Celestial Sword (second-generation Rain Immortal Sword).",
                List.of("Contained in the Rain Celestial Sword",
                        "Sword-technique"),
                List.of("sword", "celestial", "rain_celestial_sword", "zhou_yi", "inheritance")
        ));

        register(CanonicalEntry.of(
                "sundered_night", "Sundered Night (1st Original Spell)", "残夜",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 988"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created. Dao of Beginning and End / Creation and Extermination."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's first self-created divine ability. Contains the rules of beginning. Initially cultivated via insights into the Beginning Realm on Water Spirit Star, uses the Taichu sun to tear through the night. Used to kill Xu Kongzi and heavily wound Tian Yunzi. After reaching Kong-Jie, evolved into a Belief Art. Upon comprehending the Taichu Origin, its power became even greater.",
                List.of("First self-created Original Spell",
                        "Rules of Beginning",
                        "Killed Xu Kongzi; heavily wounded Tian Yunzi",
                        "Evolved into a Belief Art",
                        "Power grew after Taichu Origin comprehension"),
                List.of("original_spell", "wang_lin_created", "taichu", "beginning", "belief_art")
        ));

        register(CanonicalEntry.of(
                "flowing_time", "Flowing Time / Flowing Moon (2nd Original Spell)", "流月",
                CanonicalCategory.TIME,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1245"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created. Dao of Time."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Comprehended at the gate of the Ancient Immortal Domain in the Wind Immortal World. Can reverse time. Wang Lin used this to send Lu Mo (Slaughter True Body) back to the past to search for a method to resurrect Li Muwan.",
                List.of("Second Original Spell",
                        "Time-reversal",
                        "Used to send Lu Mo back to the past"),
                List.of("original_spell", "wang_lin_created", "time", "lu_mo", "li_muwan_revival")
        ));

        register(CanonicalEntry.of(
                "dream_dao", "Dream Dao (3rd Original Spell)", "梦道",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1295"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created with Sealer of Realms' help."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "With the help of the Sealer of Realms, Wang Lin entered the Dao Realm and gained insight into his first personal Dao art. Entering the Dream Dao allows one to probe reincarnation and can be used for enslavement. Used to kill his shadow to pass the Self Tribulation during the Three-Step Tribulation. Used to learn of Tian Yunzi's 99 cycles of reincarnation. Lu Mo used Dream Dao to deduce the past, leading the Immortal Ancestor, Ancient Ancestor, and Dao Gu Imperial Preceptor to believe that the Xiangang Continent was merely a dream of Wang Lin.",
                List.of("Third Original Spell",
                        "Probes reincarnation; usable for enslavement",
                        "Killed his shadow to pass Self Tribulation",
                        "Lu Mo used Dream Dao to send the bead back through time"),
                List.of("original_spell", "wang_lin_created", "reincarnation", "lu_mo", "dream", "enslavement")
        ));

        register(CanonicalEntry.of(
                "celestial_slaughter_art", "Celestial Slaughter Art", "屠仙术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 504 (S1 EP107)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_PARTIAL,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Learned from Grey-robed All-Seer via Tianyunzi's clone. The All-Seer embedded a trap inside. Wang Lin cannot in good conscience propagate a trapped technique."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial Spell Wang Lin obtained from Grey-robed All-Seer (the All-Seer) via Tianyunzi's clone. The All-Seer embedded a trap inside; Wang Lin later turned the trap back on the All-Seer and killed him.",
                List.of("Life-seal technique",
                        "Embedded with the All-Seer's trap",
                        "Wang Lin turned the trap back on the All-Seer"),
                List.of("celestial", "all_seer", "tianyunzi", "trap", "life_seal")
        ));

        register(CanonicalEntry.of(
                "finger_of_death", "Finger of Death / Extinction Finger", "寂灭指",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 482 (S1 EP103)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taught by Situ Nan (incomplete low-quality celestial spell)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Death-attribute finger strike. Wang Lin's main offensive technique during his Yang-Soul-Transformation period. Originally performed with the Extinction Twin Fingers treasure; Situ Nan later integrated it with his laws to cast bare-handed.",
                List.of("Death-attribute finger strike",
                        "Main offensive technique during Yang-Soul-Transformation period",
                        "Has variants: Demonic Finger (Ch. 483), Underworld Finger (Ch. 500)"),
                List.of("celestial", "situ_nan", "finger", "death", "incomplete")
        ));

        register(CanonicalEntry.of(
                "demonic_finger", "Demonic Finger", "魔指",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 483"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taught by Situ Nan."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Demonic-attribute finger strike; part of Situ Nan's trio of celestial-finger spells (Death/Demonic/Underworld).",
                List.of("Demonic-attribute finger strike",
                        "Sister-technique to Finger of Death"),
                List.of("celestial", "situ_nan", "finger", "demonic", "incomplete")
        ));

        register(CanonicalEntry.of(
                "underworld_finger", "Underworld Finger", "冥指",
                CanonicalCategory.UNDERWORLD_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 500 (S1 EP103)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taught by Situ Nan."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Underworld-attribute finger strike. Third sister-spell to Death/Demonic Fingers.",
                List.of("Underworld-attribute finger strike",
                        "Third sister-spell of the Situ Nan finger trio"),
                List.of("celestial", "situ_nan", "finger", "underworld", "incomplete")
        ));

        register(CanonicalEntry.of(
                "yellow_springs_finger", "Yellow Springs Finger", "黄泉指",
                CanonicalCategory.UNDERWORLD_TECHNIQUES,
                Provenance.inferred("Renegade Immortal", List.of("implicit (Baidu/Situ Nan lineage)"), 4,
                        "Used by Situ Nan to heavily injure Tuo Sen."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan lineage — Wang Lin learns it second-hand."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Yellow-springs-attribute finger strike. Situ Nan used it to heavily injure Tuo Sen.",
                List.of("Yellow-springs-attribute finger strike",
                        "Situ Nan used it to heavily injure Tuo Sen"),
                List.of("celestial", "situ_nan", "finger", "yellow_springs")
        ));

        register(CanonicalEntry.of(
                "nine_cycle_celestial_refining", "Nine Cycle Celestial Refining Tactic", "九转炼仙诀",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 493 (S1 EP106)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Acquired from the All-Seer / Tianyunzi. The All-Seer is a known betrayer; Wang Lin does not redistribute his arts."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An imitation of a low-quality celestial spell created by the All-Seer. Causes immortal power inside the body to slowly rotate in a peculiar spiral shape before release — equivalent to the power of a Golden Immortal. Can produce up to 9 revolutions. The longer the power is held in the body before release, the greater the output.",
                List.of("Immortal-power spiral rotation",
                        "Up to 9 revolutions",
                        "Time-delay tradeoff: more revolutions = more power but longer charge"),
                List.of("celestial", "all_seer", "tianyunzi", "celestial_induction", "immortal_power")
        ));

        register(CanonicalEntry.of(
                "ancient_thunder_dragon_form", "Ancient Thunder Dragon Form", "古龙雷形",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 650"), 5,
                        "Devoured Greed's Second Origin Soul (Ancient Thunder Dragon)."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Devoured Greed's Ancient Thunder Dragon — Soul-Devourer absorption."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's Soul-Devourer nature absorbed the Ancient Thunder Dragon (Greed's Second Origin Soul). Granted thunder-attribute soul-form.",
                List.of("Absorbed Ancient Thunder Dragon (Greed's Second Origin Soul)",
                        "Thunder-attribute soul-form"),
                List.of("soul_devourer", "thunder", "ancient_thunder_dragon", "greed", "absorption")
        ));

        register(CanonicalEntry.of(
                "soul_searching", "Soul Searching", "搜魂",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired soul-technique."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Forcibly reads a captured soul's memories. Damages or destroys the soul in the process.",
                List.of("Forcibly reads memories from a captured soul",
                        "Damages / destroys the soul"),
                List.of("soul", "divine_sense", "wang_lin_created", "interrogation")
        ));

        register(CanonicalEntry.of(
                "soul_branding", "Soul Branding / Slave Seal", "灵魂烙印",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("early; used on Big Head Cultivator"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Brands a target's soul as a slave seal. Used on the Big Head Cultivator and others.",
                List.of("Brands soul as slave-seal",
                        "Used on the Big Head Cultivator"),
                List.of("soul", "slave_seal", "wang_lin_created", "branding")
        ));

        register(CanonicalEntry.of(
                "nether_guide", "Nether Guide", "冥引",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 949"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Spell taught by Qing Shui (Wang Lin's senior brother)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Condenses all memories of the persons the user killed recently to open a portal that leads to where those persons had ever been before.",
                List.of("Condenses slain-enemy memories into a portal",
                        "Portal leads to where the slain had been"),
                List.of("soul", "movement", "portal", "qing_shui", "inheritance")
        ));

        register(CanonicalEntry.of(
                "qi_xi_spell", "Qi Xi Spell (Life-Force Exchange Resurrection)", "乞夕术",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("implicit (Baidu)"), 5,
                        "Zhan Li Yunzi referred Wang Lin to his ancestor who could use the Qi Xi spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Learned from Zhan Li Yunzi's ancestor."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin used it on himself, sacrificing his vitality (lifespan) to resurrect Li Muwan and form flesh out of her Nascent Soul. Ancestor demanded 50% of Wang Lin's life force. Failed to fully recover Li Muwan — didn't pass the 4th day out of 7.",
                List.of("Life-force sacrifice for resurrection",
                        "Cost: 50% of Wang Lin's life force",
                        "Failed to fully recover Li Muwan (4 of 7 days)"),
                List.of("resurrection", "life_force", "li_muwan_revival", "wang_lin_owned", "sacrifice")
        ));

        register(CanonicalEntry.of(
                "magic_arsenal", "Magic Arsenal (Bai Fan Six Paths Triple Arts, 3rd)", "魔库术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 919"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan lineage (3rd of the Six Paths Triple Arts)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Spell that stores all souls of enemies the user has killed through their lifetime. Wang Lin used his Life and Death domain Underworld River as a catalyst to bind the souls into Celestial Sealing Stamp to form 18 Layers of Hell Reincarnation Realm.",
                List.of("Stores souls of all slain enemies",
                        "Combined with Underworld River + Celestial Sealing Stamp → 18 Layers of Hell Reincarnation Realm"),
                List.of("soul", "bai_fan", "celestial", "arsenal", "inheritance", "underworld_river")
        ));

        register(CanonicalEntry.of(
                "mountain_crumble_spell", "Mountain Crumble (Bai Fan Six Paths Triple Arts)", "山河崩",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Bai Fan inheritance"), 4,
                        "Wang Lin inherited Bai Fan's Mountain Crumble spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Bai Fan's Mountain Crumble spell — one of the Six Paths Triple Arts Wang Lin inherited from the Thunder Immortal World.",
                List.of("Inherited from Bai Fan's Collection Pavilion in Thunder Immortal World"),
                List.of("celestial", "bai_fan", "inheritance", "thunder_immortal_world")
        ));

        register(CanonicalEntry.of(
                "body_fixation_art", "Body Fixation Art", "定身术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Freezes a target's body in place via divine sense.",
                List.of("Freezes target's body",
                        "Divine-sense-based"),
                List.of("celestial", "divine_sense", "wang_lin_created", "control")
        ));

        register(CanonicalEntry.of(
                "earth_escape_technique", "Earth Escape Technique", "土遁",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 134"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired survival skill (Ch. 134, Sea of Devils)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The basic earth-burrowing escape technique Wang Lin developed in the Sea of Devils (Ch. 134). A survival skill — it lets the cultivator melt into earth and flee. Wang Lin considers this a defensive art worth sharing: a living cultivator can cultivate; a dead one cannot.",
                List.of("Melts cultivator into earth",
                        "Survival / escape art"),
                List.of("movement", "escape", "wang_lin_created", "survival", "sea_of_devils")
        ));

        register(CanonicalEntry.of(
                "teleportation_restriction", "Teleportation Restriction", "传送禁",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 493"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Restriction-based teleportation — Wang Lin's signature movement restriction.",
                List.of("Restriction-based teleportation",
                        "Movement + restriction hybrid"),
                List.of("movement", "restriction", "wang_lin_created", "space")
        ));

        register(CanonicalEntry.of(
                "soul_piercing_eyes", "Soul Piercing Eyes / Divine Sense Eyes", "神识之眼",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 179"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Obtained once someone has reached a certain level of mastery in restrictions."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Divine sense eyes that are only obtained once someone has reached a certain level of mastery in restrictions. Pierces illusions and restrictions; reads structure of restrictions.",
                List.of("Pierces illusions",
                        "Reads restriction structure",
                        "Restriction-mastery-gated"),
                List.of("divine_sense", "eye", "restriction", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "eyes_suppressing_the_world", "Eyes Suppressing the World", "镇压天下之眼",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1896"), 5,
                        "From the fragment of the sword the green-robed old man (Ji Si) had fused into his eyes."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ji Si implanted a fragment of the Celestial Ancestor's Immortal Absolute Sword into Wang Lin's eyes."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "His gaze can suppress another cultivator. Directly tied to Metal Essence — third fragment of the Celestial Ancestor's Sword.",
                List.of("Gaze suppresses another cultivator",
                        "Tied to Metal Essence completion (Ch. 1997)"),
                List.of("divine_sense", "eye", "ji_si", "celestial_ancestor", "metal_essence", "inheritance")
        ));

        register(CanonicalEntry.of(
                "light_shadow_shield", "Light and Shadow Shield", "光影盾",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1321"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Dao Spell of Dao Master Blue Dream (Li Qianmei's father)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Defensive spell that creates a light made of every vitality in the world. Fuses all surrounding light into one's body, forming an illusory light shield that blocks AND reflects enemy attacks.",
                List.of("Fuses surrounding light into the body",
                        "Blocks AND reflects enemy attacks"),
                List.of("celestial", "blue_dream", "shield", "reflective", "light", "inheritance")
        ));

        register(CanonicalEntry.of(
                "dao_fusion", "Dao Fusion", "道融",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1322"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Dao Spell of Dao Master Blue Dream."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Not an attack or defense — a Dao Spell that fuses your spells with your essence in a special way. If learned, lets the user fuse all spells into tens of millions of different ones, or all into one. The mechanical basis for Wang Lin's later Essence fusion breakthroughs.",
                List.of("Fuses spells with essence",
                        "Tens of millions of fusion variants",
                        "Basis for later Essence fusion breakthroughs"),
                List.of("dao_spell", "blue_dream", "fusion", "inheritance", "essence_fusion")
        ));

        register(CanonicalEntry.of(
                "rapid_spell_art", "Rapid Spell Art", "速咒术",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1867"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Got after killing Xu Decai of the Green Devil Continent. Extreme path; opening nine spell veins is fatal on failure — Wang Lin does not propagate paths that kill the unworthy."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Walks an extreme path that lets the caster cast spells at terrifying speed. The key is opening nine spell veins in the body, each increasing casting speed greatly. Spell veins are a special method that uses sealed time-energy to accelerate casting. Each spell vein requires sealing a portion of cultivation/energy; extreme path means failure during opening is fatal.",
                List.of("Nine spell veins",
                        "Each vein requires sealing cultivation/energy",
                        "Failure during opening is fatal",
                        "Reproduces Empyrean Exalt fusing-Dao effect"),
                List.of("cultivation_art", "xu_decai", "spell_vein", "extreme_path", "void_tribulant")
        ));

        // ── Heaven Technique (T05) ───────────────────────────────────
        register(CanonicalEntry.of(
                "heaven_technique_ancient_god", "Heaven Technique (Ancient God)", "通天诀",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 192"), 5,
                        "Cross-refs RICanonicalDatabase T05. Tu Si legacy."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tu Si legacy (Ch. 192)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heaven Technique of the Ancient God lineage. Tu Si legacy (Ch. 192). Complementary to the Ancient God Tactic — opens the heaven-channeling pathway of the Ancient God body.",
                List.of("Ancient God lineage heaven-technique",
                        "Tu Si legacy (Ch. 192)",
                        "Complementary to Ancient God Tactic"),
                List.of("ancient_god", "tu_si", "heaven_technique", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T13_heavenly_chop", "Heavenly Chop", "天斩",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 685"), 4,
                        "Cross-refs RICanonicalDatabase T13."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired sword divine ability."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Sword divine ability. Celestial-tier chopping sword-technique.",
                List.of("Sword divine ability",
                        "Celestial-tier chopping sword-technique"),
                List.of("sword", "celestial", "chop", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "T14_ten_million_swords", "10 Million Swords", "千万剑",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Cross-refs RICanonicalDatabase T14."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A technique that summons 10 million flying swords. Wiki-attested.",
                List.of("Summons 10 million flying swords",
                        "Wiki-attested"),
                List.of("sword", "celestial", "summon", "ten_million", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "T17_falling_star", "Falling Star", "陨星",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1119"), 4,
                        "Cross-refs RICanonicalDatabase T17. Self-acquired celestial spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired celestial spell (Ch. 1119)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial-tier self-acquired spell. Calls down a falling star on the target.",
                List.of("Self-acquired celestial spell",
                        "Calls down a falling star",
                        "Celestial-tier"),
                List.of("celestial", "falling_star", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T18_heaven_extinction", "Heaven Extinction", "灭天",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1307"), 5,
                        "Cross-refs RICanonicalDatabase T18."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 1307)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Self-acquired celestial spell (Ch. 1307). Heaven-extinction attribute.",
                List.of("Self-acquired celestial spell",
                        "Heaven-extinction attribute",
                        "Celestial-tier"),
                List.of("celestial", "heaven_extinction", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T19_heaven_reversal_stamp", "Heaven Reversal Stamp", "翻天印",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1320"), 4,
                        "Cross-refs RICanonicalDatabase T19."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created celestial spell (Ch. 1320)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Self-created celestial spell (Ch. 1320). Reverses the heavens — stamp-form attack.",
                List.of("Self-created celestial spell",
                        "Reverses the heavens",
                        "Stamp-form attack"),
                List.of("celestial", "heaven_reversal", "stamp", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T23_dao_of_power", "Dao of Power / Rebound Force", "力之反",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1403"), 4,
                        "Cross-refs RICanonicalDatabase T23. 2nd-Gen Vermilion Bird / Black Tortoise lineage."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("2nd-Gen Vermilion Bird / Black Tortoise lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dao of Power / Rebound Force. Reflects the force of attacks back at the attacker. 2nd-Gen Vermilion Bird / Black Tortoise lineage.",
                List.of("Reflects force of attacks back",
                        "2nd-Gen Vermilion Bird / Black Tortoise lineage"),
                List.of("celestial", "power", "rebound", "vermilion_bird", "black_tortoise")
        ));

        register(CanonicalEntry.of(
                "T27_heavenly_fate_finger", "Heavenly Fate Finger", "天命指",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1156"), 4,
                        "Cross-refs RICanonicalDatabase T27. Bai Fan lineage."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heavenly Fate Finger. Bai Fan lineage celestial spell.",
                List.of("Bai Fan lineage",
                        "Celestial-tier finger spell"),
                List.of("celestial", "finger", "bai_fan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T28_lands_collapse", "Lands Collapse", "土崩",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1582"), 4,
                        "Cross-refs RICanonicalDatabase T28. Bai Fan spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Lands Collapse. Bai Fan spell — earth-collapse attack.",
                List.of("Bai Fan spell",
                        "Earth-collapse attack"),
                List.of("celestial", "earth_collapse", "bai_fan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T29_dark_moon_clear_skies", "Dark Moon, Clear Skies", "暗月晴空",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1582"), 4,
                        "Cross-refs RICanonicalDatabase T29."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dark Moon, Clear Skies. Bai Fan spell — yin-yang transition attack.",
                List.of("Bai Fan spell",
                        "Yin-yang transition attack"),
                List.of("celestial", "dark_moon", "bai_fan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T30_god_demon_devil_ancient_dao_no_celestial", "God, Demon, Devil, Ancient Dao, No Celestial!", "神魔妖古道无仙",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1778"), 5,
                        "Cross-refs RICanonicalDatabase T30. Three-spell combo."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Three-spell combo (Ch. 1778)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three-spell combo: God-Tremble Army Formation + Demon Spell Wind-Fire Mountain + Devil Dao Life-Death Reverse. The combo's name declares 'God, Demon, Devil, Ancient Dao — No Celestial!' — i.e. the four ancient lineages united; no celestial-tier cultivator can stand against it.",
                List.of("Three-spell combo (Ch. 1778)",
                        "God-Tremble Army Formation + Demon Spell Wind-Fire Mountain + Devil Dao Life-Death Reverse",
                        "Four ancient lineages united",
                        "No celestial-tier cultivator can stand against it"),
                List.of("celestial", "combo", "god_tremble", "wind_fire_mountain", "life_death_reverse", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T31_god_punch", "God Punch", "神拳",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1312"), 5,
                        "Cross-refs RICanonicalDatabase T31. Ancient Order spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "God Punch. Ancient Order spell. God-tier punch that channels Ancient God power.",
                List.of("Ancient Order spell",
                        "Channels Ancient God power",
                        "God-tier punch"),
                List.of("ancient_god", "punch", "ancient_order", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T33_immortal_dream", "Immortal Dream", "仙梦",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1675"), 5,
                        "Cross-refs RICanonicalDatabase T33. Fusion of Dream of Ancient Times + Dream Dao."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 1675). Fusion of Dream of Ancient Times + Dream Dao."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Immortal Dream. Fusion of Dream of Ancient Times + Dream Dao (Ch. 1675). Dream-tier attack that traps target in a dream-realm.",
                List.of("Fusion of Dream of Ancient Times + Dream Dao",
                        "Dream-tier attack",
                        "Traps target in a dream-realm"),
                List.of("celestial", "dream", "fusion", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T34_heaven_ripping", "Heaven-Ripping", "撕天",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1691"), 5,
                        "Cross-refs RICanonicalDatabase T34. Ancient Order spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heaven-Ripping. Ancient Order spell (Ch. 1691). Rips open the heavens.",
                List.of("Ancient Order spell",
                        "Rips open the heavens"),
                List.of("ancient_god", "ancient_order", "heaven_ripping", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T37_heavenly_devil_sound", "Heavenly Devil Sound", "天魔音",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 619", "Ch. 625"), 5,
                        "Cross-refs RICanonicalDatabase T37."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("From Qing Lin's false cave in Demon Spirit Land (Ch. 619/625)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heavenly Devil Sound. Sonic attack that disrupts the target's primordial spirit. Acquired from Qing Lin's false cave.",
                List.of("Sonic attack",
                        "Disrupts the primordial spirit",
                        "Acquired from Qing Lin's false cave"),
                List.of("celestial", "sound", "qing_lin", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T38_spirit_transformation", "Spirit Transformation", "灵变",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 765"), 5,
                        "Cross-refs RICanonicalDatabase T38. Ancient Order spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Spirit Transformation. Ancient Order spell (Ch. 765). Transforms the spirit into another form.",
                List.of("Ancient Order spell",
                        "Transforms the spirit into another form"),
                List.of("ancient_god", "spirit_transformation", "ancient_order", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T39_merit_spirit", "Merit Spirit", "功德灵",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1017"), 5,
                        "Cross-refs RICanonicalDatabase T39. Ancient God supreme spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient God supreme spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Merit Spirit. Ancient God supreme spell (Ch. 1017). Channels merit-spirit power.",
                List.of("Ancient God supreme spell",
                        "Channels merit-spirit power"),
                List.of("ancient_god", "merit_spirit", "supreme_spell", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T41_void_8_star_spell", "Void (8-star Ancient God spell)", "虚",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1314"), 5,
                        "Cross-refs RICanonicalDatabase T41. 8-star Ancient God spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("8-star Ancient God spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Void. 8-star Ancient God spell (Ch. 1314). Channels void-elemental power.",
                List.of("8-star Ancient God spell",
                        "Channels void-elemental power"),
                List.of("ancient_god", "void", "8_star", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T42_mysterious_god_star", "Mysterious God Star", "玄神星",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1315"), 4,
                        "Cross-refs RICanonicalDatabase T42."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mysterious God Star. Ancient Order spell (Ch. 1315). Summons a mysterious god-star.",
                List.of("Ancient Order spell",
                        "Summons a mysterious god-star"),
                List.of("ancient_god", "mysterious_god_star", "ancient_order", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T43_god_tremble_army_formation_spell", "God-Tremble Army Formation (spell)", "神震军阵",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1469"), 5,
                        "Cross-refs RICanonicalDatabase T43. Ancient Order combo part 1."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order combo part 1."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "God-Tremble Army Formation (Ch. 1469). Ancient Order combo part 1 of the God-Demon-Devil-Ancient Dao-No-Celestial three-spell combo.",
                List.of("Ancient Order combo part 1",
                        "Army-formation shockwave",
                        "Part of the three-spell combo"),
                List.of("ancient_god", "god_tremble", "army_formation", "ancient_order", "combo")
        ));

        register(CanonicalEntry.of(
                "T44_demon_spell_wind_fire_mountain", "Demon Spell, Wind and Fire Mountain", "魔咒风水山",
                CanonicalCategory.ANCIENT_DEMON,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1469"), 5,
                        "Cross-refs RICanonicalDatabase T44. Ancient Order combo part 2."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order combo part 2."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Demon Spell, Wind and Fire Mountain (Ch. 1469). Ancient Order combo part 2 of the three-spell combo. Demon-element wind-fire mountain attack.",
                List.of("Ancient Order combo part 2",
                        "Demon-element wind-fire mountain attack",
                        "Part of the three-spell combo"),
                List.of("ancient_demon", "wind_fire_mountain", "ancient_order", "combo")
        ));

        register(CanonicalEntry.of(
                "T45_devil_dao_life_death_reverse", "Devil Dao, Life and Death Reverse", "魔道生死逆",
                CanonicalCategory.ANCIENT_DEVIL,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1476"), 5,
                        "Cross-refs RICanonicalDatabase T45. Ancient Order combo part 3."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order combo part 3."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Devil Dao, Life and Death Reverse (Ch. 1476). Ancient Order combo part 3. Reverses life and death for the target.",
                List.of("Ancient Order combo part 3",
                        "Reverses life and death",
                        "Part of the three-spell combo"),
                List.of("ancient_devil", "life_death_reverse", "ancient_order", "combo")
        ));

        register(CanonicalEntry.of(
                "T46_ancient_order_life_transformation_spell", "Ancient Order, Life Transformation Spell", "古序命转术",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1515"), 5,
                        "Cross-refs RICanonicalDatabase T46. 3 uses per lifetime."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell. Limited to 3 uses per lifetime."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient Order, Life Transformation Spell (Ch. 1515). Transforms the user's life — limited to 3 uses per lifetime.",
                List.of("Ancient Order spell",
                        "Transforms the user's life",
                        "Limited to 3 uses per lifetime"),
                List.of("ancient_god", "life_transformation", "ancient_order", "limited_use", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T47_burning_realm_ancient_umbrella", "Burning Realm Ancient Umbrella", "焚界古伞",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1427", "Ch. 1543 (upgraded)"), 5,
                        "Cross-refs RICanonicalDatabase T47. Vermilion Bird Divine Ability."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Ability."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Burning Realm Ancient Umbrella (Ch. 1427; upgraded Ch. 1543). Vermilion Bird Divine Ability — umbrella that burns an entire realm.",
                List.of("Vermilion Bird Divine Ability",
                        "Umbrella that burns an entire realm",
                        "Upgraded Ch. 1543"),
                List.of("celestial", "vermilion_bird", "umbrella", "burning_realm", "divine_ability")
        ));

        register(CanonicalEntry.of(
                "T48_third_eye_spell", "Third Eye Spell", "天眼术",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 734"), 5,
                        "Cross-refs RICanonicalDatabase T48. Luotian Star Domain ancestor."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Luotian Star Domain ancestor inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Third Eye Spell (Ch. 734). Opens the third eye for divine-sense perception. Luotian Star Domain ancestor inheritance.",
                List.of("Opens the third eye",
                        "Divine-sense perception boost",
                        "Luotian Star Domain ancestor"),
                List.of("divine_sense", "third_eye", "luotian", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T52_ancient_god_blood", "Ancient God's Blood", "古神之血",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("implicit"), 5,
                        "Cross-refs RICanonicalDatabase T52. Killed Giant Demon Ancestor."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("From the Giant Demon Ancestor Wang Lin killed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient God's Blood. Power source / attack — collected from the Giant Demon Ancestor Wang Lin killed.",
                List.of("Power source / attack",
                        "Collected from the Giant Demon Ancestor"),
                List.of("ancient_god", "blood", "giant_demon_ancestor")
        ));

        register(CanonicalEntry.of(
                "T53_dao_transformation_yellow_springs", "Dao Transformation Yellow Springs", "道化黄泉",
                CanonicalCategory.UNDERWORLD_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("implicit"), 5,
                        "Cross-refs RICanonicalDatabase T53. Self-created."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dao Transformation Yellow Springs. Self-created. Transforms Dao power into Yellow Springs (underworld river) energy.",
                List.of("Self-created",
                        "Transforms Dao power into Yellow Springs energy"),
                List.of("underworld", "yellow_springs", "wang_lin_created", "dao_transformation")
        ));

        register(CanonicalEntry.of(
                "T55_three_life_spell", "Three Life Spell", "三生术",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1713"), 4,
                        "Cross-refs RICanonicalDatabase T55. Splits soul/origin soul/body."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 1713)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three Life Spell (Ch. 1713). Splits soul / origin soul / body into three lives. Samsara-tier spell.",
                List.of("Splits soul / origin soul / body into three lives",
                        "Samsara-tier spell"),
                List.of("samsara", "three_life", "soul_split", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T57_war_spirit_print", "War Spirit Print", "战神印",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 299", "Ch. 1542 (upgraded)"), 5,
                        "Cross-refs RICanonicalDatabase T57. Rain Celestial Realm."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Rain Celestial Realm inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "War Spirit Print (Ch. 299; upgraded Ch. 1542). Rain Celestial Realm inheritance. Summons war spirit to fight.",
                List.of("Rain Celestial Realm inheritance",
                        "Summons war spirit to fight",
                        "Upgraded Ch. 1542"),
                List.of("celestial", "war_spirit", "rain_celestial_realm", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T58_void_stop_spell", "Void Stop Spell", "空停术",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 627"), 5,
                        "Cross-refs RICanonicalDatabase T58."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 627)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Void Stop Spell (Ch. 627). Stops void movement — locks the target in space.",
                List.of("Stops void movement",
                        "Locks the target in space"),
                List.of("space", "void_stop", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T59_thunder_origin_spell", "Thunder Origin Spell", "雷源术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 707"), 5,
                        "Cross-refs RICanonicalDatabase T59. Low-rank celestial fragment."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Low-rank celestial fragment."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder Origin Spell (Ch. 707). Low-rank celestial fragment thunder spell.",
                List.of("Low-rank celestial fragment",
                        "Thunder-element"),
                List.of("celestial", "thunder_origin", "celestial_fragment", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T60_call_wind", "Call the Wind (Bai Fan Six Paths #1)", "唤风术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 783"), 5,
                        "Cross-refs RICanonicalDatabase T60. Bai Fan Six Paths Triple Arts #1."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan Six Paths Triple Arts #1."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Call the Wind (Ch. 783). Bai Fan Six Paths Triple Arts #1. Summons wind-elemental power.",
                List.of("Bai Fan Six Paths Triple Arts #1",
                        "Summons wind-elemental power"),
                List.of("celestial", "wind", "bai_fan", "six_paths", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T61_summon_rain", "Summon the Rain (Bai Fan Six Paths #2)", "唤雨术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 914"), 5,
                        "Cross-refs RICanonicalDatabase T61. Bai Fan Six Paths Triple Arts #2."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bai Fan Six Paths Triple Arts #2."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Summon the Rain (Ch. 914). Bai Fan Six Paths Triple Arts #2. Summons rain-elemental power. Pairs with Call the Wind.",
                List.of("Bai Fan Six Paths Triple Arts #2",
                        "Summons rain-elemental power",
                        "Pairs with Call the Wind"),
                List.of("celestial", "rain", "bai_fan", "six_paths", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T62_body_formation_spell", "Body Formation", "体化术",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1125"), 5,
                        "Cross-refs RICanonicalDatabase T62."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 1125)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Body Formation (Ch. 1125). Rock avatar + origin soul formation — fuses body with rock for defense.",
                List.of("Rock avatar + origin soul formation",
                        "Fuses body with rock for defense"),
                List.of("celestial", "body_formation", "rock_avatar", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T63_heart_pounding_thunder", "Heart-Pounding Thunder", "心悸雷",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1777"), 5,
                        "Cross-refs RICanonicalDatabase T63. Fire+Thunder essence fusion."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 1777)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heart-Pounding Thunder (Ch. 1777). Fusion of fire and thunder essence spell. Comprehended in the last layer of the Ancient Graveyard. Composite elemental power.",
                List.of("Fusion of fire and thunder essence",
                        "Comprehended in the last layer of the Ancient Graveyard",
                        "Composite elemental power"),
                List.of("celestial", "heart_pounding_thunder", "fire_thunder_fusion", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T64_soul_eye_dao", "Soul Eye Dao", "魂眼道",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1831"), 4,
                        "Cross-refs RICanonicalDatabase T64. Great Soul Sect divination."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Great Soul Sect divination."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Eye Dao (Ch. 1831). Great Soul Sect divination technique. Eye-based soul perception.",
                List.of("Great Soul Sect divination technique",
                        "Eye-based soul perception"),
                List.of("divine_sense", "soul_eye", "great_soul_sect", "divination", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T65_soul_fantasy_origin", "Soul Fantasy Origin", "魂幻源",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1837"), 4,
                        "Cross-refs RICanonicalDatabase T65. Great Soul Sect."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Great Soul Sect inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Fantasy Origin (Ch. 1837). Great Soul Sect inheritance. Origin of soul-fantasy / illusion power.",
                List.of("Great Soul Sect inheritance",
                        "Origin of soul-fantasy / illusion power"),
                List.of("divine_sense", "soul_fantasy", "great_soul_sect", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T66_multi_layered_illusion_spell", "Multi-Layered Illusion Spell", "多层幻术",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1853"), 5,
                        "Cross-refs RICanonicalDatabase T66. Nine levels."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Great Soul Sect inheritance (nine levels)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Multi-Layered Illusion Spell (Ch. 1853). Nine levels. Each level deepens the illusion. Great Soul Sect inheritance.",
                List.of("Nine levels of illusion",
                        "Each level deepens the illusion",
                        "Great Soul Sect inheritance"),
                List.of("divine_sense", "illusion", "multi_layered", "great_soul_sect", "inheritance")
        ));

        // ── Eight Extreme Daos (T67–T76) ─────────────────────────────
        register(CanonicalEntry.of(
                "T67_extreme_fire_dao", "Extreme Fire Dao", "极火道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1826"), 5,
                        "Cross-refs RICanonicalDatabase T67. 1st of 8 Extreme Daos."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended (Ch. 1826). 1st of 8 Extreme Daos."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Fire Dao. 1st of the 8 Extreme Daos (Ch. 1826). Fire-element Third-Step-tier Dao.",
                List.of("1st of the 8 Extreme Daos",
                        "Fire-element Third-Step-tier Dao",
                        "Self-comprehended Ch. 1826"),
                List.of("extreme_dao", "fire", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T68_extreme_water_dao", "Extreme Water Dao", "极水道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1958"), 5,
                        "Cross-refs RICanonicalDatabase T68. 2nd of 8 Extreme Daos."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended (Ch. 1958)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Water Dao. 2nd of the 8 Extreme Daos (Ch. 1958). Water-element Third-Step-tier Dao.",
                List.of("2nd of the 8 Extreme Daos",
                        "Water-element Third-Step-tier Dao",
                        "Self-comprehended Ch. 1958"),
                List.of("extreme_dao", "water", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T69_extreme_metal_dao", "Extreme Metal Dao", "极金道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2034"), 5,
                        "Cross-refs RICanonicalDatabase T69."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended (Ch. 2034)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Metal Dao. 3rd of the 8 Extreme Daos (Ch. 2034). Metal-element Third-Step-tier Dao.",
                List.of("3rd of the 8 Extreme Daos",
                        "Metal-element Third-Step-tier Dao",
                        "Self-comprehended Ch. 2034"),
                List.of("extreme_dao", "metal", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T70_extreme_wood_dao", "Extreme Wood Dao", "极木道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Cross-refs RICanonicalDatabase T70."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Wood Dao. 4th of the 8 Extreme Daos. Wood-element Third-Step-tier Dao.",
                List.of("4th of the 8 Extreme Daos",
                        "Wood-element Third-Step-tier Dao"),
                List.of("extreme_dao", "wood", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T71_extreme_earth_dao", "Extreme Earth Dao", "极土道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Cross-refs RICanonicalDatabase T71."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Earth Dao. 5th of the 8 Extreme Daos. Earth-element Third-Step-tier Dao.",
                List.of("5th of the 8 Extreme Daos",
                        "Earth-element Third-Step-tier Dao"),
                List.of("extreme_dao", "earth", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T72_extreme_life_death_dao", "Extreme Life and Death Dao", "极生死道",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Cross-refs RICanonicalDatabase T72."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Life and Death Dao. 6th of the 8 Extreme Daos. Life-death-element Third-Step-tier Dao.",
                List.of("6th of the 8 Extreme Daos",
                        "Life-death-element Third-Step-tier Dao"),
                List.of("extreme_dao", "life_death", "third_step", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T73_extreme_land_dao", "Extreme Land Dao", "极地道",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2063"), 5,
                        "Cross-refs RICanonicalDatabase T73. 5th Heaven-Trampling bridge."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended on the 5th Heaven-Trampling bridge (Ch. 2063)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Land Dao. 7th of the 8 Extreme Daos. Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063).",
                List.of("7th of the 8 Extreme Daos",
                        "Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063)"),
                List.of("extreme_dao", "land", "fourth_step", "wang_lin_created", "heaven_trampling_bridge_5")
        ));

        register(CanonicalEntry.of(
                "T74_extreme_sky_dao", "Extreme Sky Dao", "极天道",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2063"), 5,
                        "Cross-refs RICanonicalDatabase T74."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended on the 5th Heaven-Trampling bridge (Ch. 2063)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Sky Dao. The mutual-opposing pair with Extreme Land Dao. Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063).",
                List.of("Mutual-opposing pair with Extreme Land Dao",
                        "Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063)"),
                List.of("extreme_dao", "sky", "fourth_step", "wang_lin_created", "heaven_trampling_bridge_5")
        ));

        register(CanonicalEntry.of(
                "T75_extreme_life_dao", "Extreme Life Dao", "极命道",
                CanonicalCategory.FOURTH_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2063"), 5,
                        "Cross-refs RICanonicalDatabase T75."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended on the 5th Heaven-Trampling bridge (Ch. 2063). The 8th life-bound Dao."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Extreme Life Dao. The 8th of the 8 Extreme Daos — the life-bound Dao. Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063).",
                List.of("8th of the 8 Extreme Daos",
                        "The life-bound Dao",
                        "Comprehended on the 5th Heaven-Trampling bridge (Ch. 2063)"),
                List.of("extreme_dao", "life", "fourth_step", "wang_lin_created", "heaven_trampling_bridge_5")
        ));

        register(CanonicalEntry.of(
                "T77_ancient_ancestors_finger_attack", "Ancient Ancestor's Finger Attack", "古祖之指",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2033"), 4,
                        "Cross-refs RICanonicalDatabase T77. Ancient Order lineage."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient Ancestor's Finger Attack (Ch. 2033). Ancient Order lineage spell. Finger-attack channeling ancient-ancestor power.",
                List.of("Ancient Order lineage spell",
                        "Channels ancient-ancestor power",
                        "Finger-attack"),
                List.of("ancient_god", "ancient_ancestor", "finger", "ancient_order", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T80_heart_of_slaughter", "Heart of Slaughter", "杀戮之心",
                CanonicalCategory.SLAUGHTER,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 574"), 5,
                        "Cross-refs RICanonicalDatabase T80."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired from accumulated slaughter (Ch. 574)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heart of Slaughter (Ch. 574). Cultivated through accumulated slaughter. Foundation of the Slaughter Dao.",
                List.of("Cultivated through accumulated slaughter",
                        "Foundation of the Slaughter Dao",
                        "Ch. 574"),
                List.of("slaughter", "heart_of_slaughter", "wang_lin_created", "slaughter_dao")
        ));

        register(CanonicalEntry.of(
                "T83_samsara_eye", "Samsara Eye / Reincarnation Eye", "轮回眼",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("implicit"), 4,
                        "Cross-refs RICanonicalDatabase T83. Situ Nan technique."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan technique."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Samsara Eye / Reincarnation Eye. Situ Nan technique. Allows perception of samsara / reincarnation cycle.",
                List.of("Situ Nan technique",
                        "Allows perception of samsara / reincarnation cycle"),
                List.of("samsara", "samsara_eye", "reincarnation_eye", "situ_nan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T84_heavenly_eye", "Heavenly Eye", "天眼",
                CanonicalCategory.DIVINE_SENSE,
                Provenance.explicit("Renegade Immortal", List.of("implicit"), 4,
                        "Cross-refs RICanonicalDatabase T84. Situ Nan awakened after reincarnation."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan awakened after reincarnation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heavenly Eye. Situ Nan awakened this after reincarnation. Divine-sense perception boost.",
                List.of("Situ Nan awakened after reincarnation",
                        "Divine-sense perception boost"),
                List.of("divine_sense", "heavenly_eye", "situ_nan", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T85_blue_flames", "Blue Flames", "蓝焰",
                CanonicalCategory.UNDERWORLD_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 121"), 5,
                        "Cross-refs RICanonicalDatabase T85. Cold-attribute flame."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Produced by the Underworld Ascension Method (Ch. 121)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blue Flames (Ch. 121). Cold-attribute flame produced by the Underworld Ascension Method. Counter to normal fire.",
                List.of("Cold-attribute flame",
                        "Produced by the Underworld Ascension Method",
                        "Counter to normal fire"),
                List.of("underworld", "blue_flames", "cold_attribute", "underworld_ascension")
        ));

        register(CanonicalEntry.of(
                "T86_star_rotation", "Star Rotation", "星辰流转",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 475"), 4,
                        "Cross-refs RICanonicalDatabase T86. Self-acquired defensive."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired defensive (Ch. 475)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Star Rotation (Ch. 475). Self-acquired defensive technique. Rotates star-energy around the user.",
                List.of("Self-acquired defensive technique",
                        "Rotates star-energy around the user"),
                List.of("celestial", "star_rotation", "defensive", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T87_spatial_bending", "Spatial Bending", "空间弯曲",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 770"), 4,
                        "Cross-refs RICanonicalDatabase T87."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired movement / divine-sense (Ch. 770)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Spatial Bending (Ch. 770). Self-acquired movement / divine-sense technique. Bends space for teleportation or perception.",
                List.of("Self-acquired movement / divine-sense",
                        "Bends space for teleportation or perception"),
                List.of("space", "spatial_bending", "movement", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T88_planet_soul_extraction", "Planet Soul Extraction", "星魂抽取",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 903"), 4,
                        "Cross-refs RICanonicalDatabase T88."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired soul-technique (Ch. 903)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Planet Soul Extraction (Ch. 903). Self-acquired soul-technique. Extracts the soul of an entire planet.",
                List.of("Self-acquired soul-technique",
                        "Extracts the soul of an entire planet"),
                List.of("soul", "planet_soul_extraction", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T110_soul_devourer_nature", "Soul-Devourer Soul Nature", "噬魂者本性",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 119", "Ch. 193 (Soul Gem)"), 5,
                        "Cross-refs RICanonicalDatabase T110. Innate."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Innate soul nature — Wang Lin's Soul-Devourer trait. Cannot be transferred."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul-Devourer Soul Nature (Ch. 119; Soul Gem Ch. 193). Innate. Wang Lin's soul has the Soul-Devourer nature — very strong souls obey him as subjects obey a king.",
                List.of("Innate soul nature",
                        "Soul-Devourer trait",
                        "Strong souls obey as subjects obey a king",
                        "Soul Gem at Ch. 193"),
                List.of("soul", "soul_devourer", "innate", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "T111_soul_branding", "Soul Branding / Slave Seal", "灵魂烙印",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("early"), 4,
                        "Cross-refs RICanonicalDatabase T111."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Branding / Slave Seal. Brands a soul-seal onto a target, enslaving them. Used by Wang Lin to bind devils and cultivators.",
                List.of("Brands a soul-seal onto a target",
                        "Enslaves the target",
                        "Used to bind devils and cultivators"),
                List.of("soul", "branding", "slave_seal", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T116_qi_xi_spell_v2", "Qi Xi Spell (life-force sacrifice resurrection)", "乞夕术",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("implicit"), 5,
                        "Cross-refs RICanonicalDatabase T116. Life-force sacrifice resurrection."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Qi Xi Spell. Life-force sacrifice resurrection technique. Sacrifices own life-force to resurrect another.",
                List.of("Life-force sacrifice resurrection",
                        "Sacrifices own life-force",
                        "Resurrects another"),
                List.of("samsara", "qi_xi_spell", "resurrection", "life_force_sacrifice", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T118_heavenly_flame", "Heavenly Flame", "天火",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1021"), 5,
                        "Cross-refs RICanonicalDatabase T118. Absorbed from Ming Hai."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Absorbed from Ming Hai (Ch. 1021)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heavenly Flame (Ch. 1021). Absorbed from Ming Hai. Heavenly-tier flame; celestial-tier attack power.",
                List.of("Absorbed from Ming Hai (Ch. 1021)",
                        "Heavenly-tier flame",
                        "Celestial-tier attack power"),
                List.of("celestial", "heavenly_flame", "ming_hai", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T120_disguising_technique", "Disguising Technique", "易容术",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 33"), 5,
                        "Cross-refs RICanonicalDatabase T120."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Basic immortal technique."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Disguising Technique (Ch. 33). Basic immortal technique. Changes the user's appearance.",
                List.of("Basic immortal technique",
                        "Changes appearance"),
                List.of("cultivation_art", "disguise", "basic_immortal")
        ));

        register(CanonicalEntry.of(
                "T121_memory_erasing_technique", "Memory Erasing Technique", "忘情术",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 78"), 5,
                        "Cross-refs RICanonicalDatabase T121."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 78)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Memory Erasing Technique (Ch. 78). Self-acquired. Erases memories from a target's soul.",
                List.of("Self-acquired (Ch. 78)",
                        "Erases memories from a target's soul"),
                List.of("soul", "memory_erasing", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T122_attractive_force_art", "Attractive Force Technique", "引力术",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 25"), 5,
                        "Cross-refs RICanonicalDatabase T122. Three Stages of Qi Condensation."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Basic Three Stages of Qi Condensation technique."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Attractive Force Technique (Ch. 25). Basic Three Stages of Qi Condensation technique. Generates attractive force on a target.",
                List.of("Basic Three Stages of Qi Condensation technique",
                        "Generates attractive force on a target"),
                List.of("cultivation_art", "attractive_force", "basic", "qi_condensation")
        ));

        register(CanonicalEntry.of(
                "T125_teleportation_restriction_spell", "Teleportation Restriction (spell form)", "瞬移禁制",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 493"), 5,
                        "Cross-refs RICanonicalDatabase T125."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created hybrid (Ch. 493)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Teleportation Restriction (Ch. 493). Self-created hybrid movement + restriction spell.",
                List.of("Self-created hybrid",
                        "Movement + restriction",
                        "Ch. 493"),
                List.of("space", "teleportation_restriction", "movement", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T129_divine_path", "Divine Path", "神道诀",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 129 (S1 EP26)"), 5,
                        "Cross-refs RICanonicalDatabase T129. War God Shrine."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("War God Shrine transmission."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Divine Path (神道诀, Ch. 129). War God Shrine transmission. Used to create Wang Lin's first Cultivator Clone.",
                List.of("War God Shrine transmission",
                        "Creates a Cultivator Clone",
                        "Ch. 129"),
                List.of("cultivation_art", "divine_path", "war_god_shrine", "clone_creation", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T132_foundation_stealing_technique", "Foundation Stealing Technique", "夺基术",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 84"), 5,
                        "Cross-refs RICanonicalDatabase T132. Used on Teng Li Ch. 86."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan helped create."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Foundation Stealing Technique (Ch. 84). Steals another cultivator's cultivation foundation. Used on Teng Li (Ch. 86).",
                List.of("Steals another cultivator's foundation",
                        "Used on Teng Li (Ch. 86)",
                        "Situ Nan helped create"),
                List.of("cultivation_art", "foundation_stealing", "situ_nan", "teng_li")
        ));

        register(CanonicalEntry.of(
                "T133_blood_refining_technique", "Blood Refining Technique", "血炼术",
                CanonicalCategory.REFINING,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 80"), 5,
                        "Cross-refs RICanonicalDatabase T133."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan helped create."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blood Refining Technique (Ch. 80). Refines treasures using blood. Situ Nan helped create.",
                List.of("Refines treasures using blood",
                        "Situ Nan helped create",
                        "Ch. 80"),
                List.of("refining", "blood_refining", "situ_nan")
        ));

        register(CanonicalEntry.of(
                "T160_nine_songs_three_signs", "Nine Songs and Three Signs", "九歌三符",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2065"), 5,
                        "Cross-refs RICanonicalDatabase T160. Gu Dao battle."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-acquired (Ch. 2065)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Nine Songs and Three Signs (Ch. 2065). Used in the Gu Dao battle. Composite celestial-tier spell.",
                List.of("Used in the Gu Dao battle",
                        "Composite celestial-tier spell",
                        "Ch. 2065"),
                List.of("celestial", "nine_songs", "three_signs", "gu_dao", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T163_grand_empyrean_sun", "Grand Empyrean Sun", "大天尊之阳",
                CanonicalCategory.THIRD_STEP,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2050"), 5,
                        "Cross-refs RICanonicalDatabase T163. 10th Sun of IAC."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 2050). Makes Wang Lin the 10th Sun of the IAC."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Grand Empyrean Sun (Ch. 2050). Self-condensed by Wang Lin's will. Makes Wang Lin the 10th Sun of the Immortal Astral Continent (IAC).",
                List.of("Self-condensed by Wang Lin's will",
                        "Makes Wang Lin the 10th Sun of the IAC",
                        "Ch. 2050"),
                List.of("third_step", "grand_empyrean_sun", "iac", "tenth_sun", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T164_ethereal_fire", "Ethereal Fire", "虚无之火",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1412 (4th VB Awakening)"), 5,
                        "Cross-refs RICanonicalDatabase T164."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Absorbed with Vermilion Bird awakening (~Ch. 1068; 4th Awakening Ch. 1412)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ethereal Fire (Ch. 1412). One of Wang Lin's accompanying fires. Similar in nature to the Seven-Colored Lance. Absorbed with Vermilion Bird awakening.",
                List.of("One of Wang Lin's accompanying fires",
                        "Similar in nature to the Seven-Colored Lance",
                        "Absorbed with Vermilion Bird awakening (Ch. 1412)"),
                List.of("celestial", "ethereal_fire", "vermilion_bird", "accompanying_fire", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T159_nine_tribulation_karma_fires", "Nine-Tribulation Karma Fires", "九劫业火",
                CanonicalCategory.KARMA,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase T159. Self-condensed."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (mid era)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Nine-Tribulation Karma Fires. Karmic-element accompanying fire. Self-condensed mid-era.",
                List.of("Karmic-element accompanying fire",
                        "Self-condensed",
                        "One of Wang Lin's accompanying fires"),
                List.of("karma", "nine_tribulation_karma_fires", "accompanying_fire", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T168_fire_burn_nine_mysterious_1st", "Fire Burn (Nine Mysterious 1st Transformation)", "焚天九变·第一变",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1089"), 5,
                        "Cross-refs RICanonicalDatabase T168. VB Divine Ability."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Ability."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fire Burn — 1st of the Nine Mysterious Transformations (Ch. 1089). Vermilion Bird Divine Ability. Aligns with the 1st Vermilion Bird Awakening.",
                List.of("1st of the Nine Mysterious Transformations",
                        "Vermilion Bird Divine Ability",
                        "Aligns with the 1st Vermilion Bird Awakening"),
                List.of("celestial", "fire_burn", "nine_mysterious", "vermilion_bird", "divine_ability")
        ));

        register(CanonicalEntry.of(
                "T169_rise_three_realm_flame_origin", "Rise Three Realm Flame Origin", "三界焰源",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1091"), 5,
                        "Cross-refs RICanonicalDatabase T169. VB Divine Ability."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Ability."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Rise Three Realm Flame Origin (Ch. 1091). Vermilion Bird Divine Ability. Three-realm flame-origin attack.",
                List.of("Vermilion Bird Divine Ability",
                        "Three-realm flame-origin attack",
                        "Ch. 1091"),
                List.of("celestial", "three_realm_flame_origin", "vermilion_bird", "divine_ability")
        ));

        register(CanonicalEntry.of(
                "T170_nine_mysterious_fire_escape", "Nine Mysterious Fire Escape", "九秘火遁",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1127"), 5,
                        "Cross-refs RICanonicalDatabase T170. Stolen from Azure Dragon."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Stolen from Azure Dragon lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Nine Mysterious Fire Escape (Ch. 1127). Stolen from Azure Dragon lineage. Fire-element escape technique.",
                List.of("Stolen from Azure Dragon lineage",
                        "Fire-element escape technique",
                        "Ch. 1127"),
                List.of("celestial", "nine_mysterious_fire_escape", "azure_dragon", "escape", "inheritance")
        ));

        // ── Accompanying Thunders (AT01–AT09) ────────────────────────
        register(CanonicalEntry.of(
                "AT01_1st_accompanying_thunder", "1st Accompanying Thunder", "第一道伴雷",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1368"), 4,
                        "Cross-refs RICanonicalDatabase AT01."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1368)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "1st of Wang Lin's nine accompanying thunders (Ch. 1368).",
                List.of("1st of 9 accompanying thunders",
                        "Self-condensed Ch. 1368"),
                List.of("ji_realm", "accompanying_thunder", "1st", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "AT07_store_all_ji_thunder", "Store All Ji Thunder (7th accompanying thunder)", "收万极雷",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1368"), 5,
                        "Cross-refs RICanonicalDatabase AT07. Absorbed the Ji Realm."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1368). 7th accompanying thunder."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Store All Ji Thunder (储极雷). 7th accompanying thunder (Ch. 1368). Wang Lin's own 7th type of accompanying thunder. Absorbed the Ji Realm.",
                List.of("7th of 9 accompanying thunders",
                        "Absorbed the Ji Realm",
                        "Self-condensed Ch. 1368"),
                List.of("ji_realm", "accompanying_thunder", "7th", "ji_thunder", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "AT08_bloodline_thunder", "Bloodline Thunder (8th accompanying thunder)", "血脉雷",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1368"), 5,
                        "Cross-refs RICanonicalDatabase AT08."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1368). 8th accompanying thunder."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Bloodline Thunder (血脉雷). 8th accompanying thunder (Ch. 1368).",
                List.of("8th of 9 accompanying thunders",
                        "Self-condensed Ch. 1368"),
                List.of("ji_realm", "accompanying_thunder", "8th", "bloodline_thunder", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "AT09_defying_thunder", "Defying Thunder (9th accompanying thunder)", "逆天雷",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1368"), 5,
                        "Cross-refs RICanonicalDatabase AT09."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed via defying will (Ch. 1368). 9th accompanying thunder."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Defying Thunder (逆天雷). 9th accompanying thunder (Ch. 1368). Never appeared since the beginning of time. Formed via defying will. Unprecedented in history.",
                List.of("9th of 9 accompanying thunders",
                        "Never appeared since the beginning of time",
                        "Formed via defying will",
                        "Unprecedented in history"),
                List.of("ji_realm", "accompanying_thunder", "9th", "defying_thunder", "wang_lin_unique", "wang_lin_created")
        ));

        // ── Vermilion Bird Awakenings (VA01–VA04) ────────────────────
        register(CanonicalEntry.of(
                "VA01_1st_vermilion_bird_awakening", "1st Vermilion Bird Awakening", "朱雀一觉",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1089"), 5,
                        "Cross-refs RICanonicalDatabase VA01."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan / Vermilion Bird lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "1st Vermilion Bird Mark awakening (Ch. 1089).",
                List.of("1st Vermilion Bird Mark awakening",
                        "Ch. 1089"),
                List.of("celestial", "vermilion_bird", "awakening", "1st", "situ_nan")
        ));

        register(CanonicalEntry.of(
                "VA02_2nd_vermilion_bird_awakening", "2nd Vermilion Bird Awakening", "朱雀二觉",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1200"), 5,
                        "Cross-refs RICanonicalDatabase VA02."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan / Vermilion Bird lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "2nd Vermilion Bird Mark awakening (Ch. ~1200).",
                List.of("2nd Vermilion Bird Mark awakening",
                        "Ch. ~1200"),
                List.of("celestial", "vermilion_bird", "awakening", "2nd", "situ_nan")
        ));

        register(CanonicalEntry.of(
                "VA03_3rd_vermilion_bird_awakening", "3rd Vermilion Bird Awakening", "朱雀三觉",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1400"), 5,
                        "Cross-refs RICanonicalDatabase VA03."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan / Vermilion Bird lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "3rd Vermilion Bird Mark awakening (Ch. ~1400).",
                List.of("3rd Vermilion Bird Mark awakening",
                        "Ch. ~1400"),
                List.of("celestial", "vermilion_bird", "awakening", "3rd", "situ_nan")
        ));

        register(CanonicalEntry.of(
                "VA04_4th_vermilion_bird_awakening", "4th Vermilion Bird Awakening", "朱雀四觉",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1412"), 5,
                        "Cross-refs RICanonicalDatabase VA04. 9 colors fuse into 1."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan / Vermilion Bird lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "4th Vermilion Bird Mark awakening (Ch. 1412). 9 colors fuse into 1. Fuels Ethereal Fire.",
                List.of("4th Vermilion Bird Mark awakening",
                        "9 colors fuse into 1",
                        "Fuels Ethereal Fire"),
                List.of("celestial", "vermilion_bird", "awakening", "4th", "ethereal_fire", "situ_nan")
        ));

        // ── Ethereal Essences (T165) ─────────────────────────────────
        register(CanonicalEntry.of(
                "T165_ethereal_essences", "Ethereal Essences (14 Origins)", "虚源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1943+"), 5,
                        "Cross-refs RICanonicalDatabase T165."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ethereal Essences. The 14 Origins total (Wang Lin's Samsara Dao system). 6 substantial + 4 virtual + 4 special.",
                List.of("14 Origins total",
                        "6 substantial + 4 virtual + 4 special",
                        "Wang Lin's Samsara Dao system"),
                List.of("essence", "ethereal_essences", "samsara_dao", "14_origins", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "T167_brilliant_void_star_system_crossing", "Brilliant Void Star System Crossing", "虚空星系穿越",
                CanonicalCategory.SPACE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1314"), 5,
                        "Cross-refs RICanonicalDatabase T167. 8-star Ancient God spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("8-star Ancient God spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Brilliant Void Star System Crossing (Ch. 1314). 8-star Ancient God spell. Allows cross-star-system void travel.",
                List.of("8-star Ancient God spell",
                        "Cross-star-system void travel",
                        "Ch. 1314"),
                List.of("space", "void_star_system", "8_star_ancient_god", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T40_100_avatars", "100 Avatars", "百化身",
                CanonicalCategory.CLONES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1271"), 5,
                        "Cross-refs RICanonicalDatabase T40. Ancient Order spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Order spell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "100 Avatars (Ch. 1271). Ancient Order spell. Splits the user into 100 avatars.",
                List.of("Ancient Order spell",
                        "Splits the user into 100 avatars",
                        "Ch. 1271"),
                List.of("clones", "100_avatars", "ancient_order", "inheritance")
        ));

        register(CanonicalEntry.of(
                "T01_devouring_technique", "Devouring Technique", "吞噬之术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("early RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A foundational absorption technique. Wang Lin used it to absorb spiritual energy, pills, and items.",
                List.of(
                        "Absorbs spiritual energy and items",
                        "Foundational absorption art"
                ),
                List.of("devouring", "absorption", "foundational")
        ));


        register(CanonicalEntry.of(
                "T03_fiend_transformation_art", "Fiend Transformation Art", "化魔之术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Transforms the user into a fiend/demon form. Increases combat power at the cost of cultivation stability.",
                List.of(
                        "Transforms into fiend form",
                        "Power boost with risk",
                        "Used by Wang Lin in desperate battles"
                ),
                List.of("transformation", "fiend", "demon", "combat")
        ));


        register(CanonicalEntry.of(
                "T09_yin_energy_detection", "Yin Energy Detection Technique", "阴气探测术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("early RI"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Detects Yin energy in the environment. Essential for finding cultivation sites for the Underworld Ascension Method.",
                List.of(
                        "Detects Yin energy sources",
                        "Pairs with Underworld Ascension Method"
                ),
                List.of("detection", "yin", "sensing", "underworld")
        ));


        register(CanonicalEntry.of(
                "T16_mountains_crumble", "Mountains Crumble", "山崩",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Qi Condensation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An earth-type attack technique that causes the ground to collapse and crumble.",
                List.of(
                        "Earth-type attack",
                        "Ground collapse"
                ),
                List.of("earth", "attack", "destruction")
        ));


        register(CanonicalEntry.of(
                "T21_fog_transformation_thunder", "Fog Transformation Thunder Spell", "雾化雷术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Foundation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Transforms fog into thunder/lightning. An offensive spell combining mist and lightning elements.",
                List.of(
                        "Fog-to-thunder conversion",
                        "Combined mist + lightning offense"
                ),
                List.of("fog", "thunder", "transformation", "offense")
        ));


        register(CanonicalEntry.of(
                "T22_giant_thunder_stamp", "Giant Thunder Stamp", "大雷印",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A large-scale thunder seal technique. Projects a giant stamp of thunder energy.",
                List.of(
                        "Large-scale thunder attack",
                        "Seal-type technique"
                ),
                List.of("thunder", "seal", "large_scale", "attack")
        ));


        register(CanonicalEntry.of(
                "T24_open_ancient_thunder_realm", "Open the Ancient Thunder Realm", "开古雷界",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Opens a passage to the Ancient Thunder Realm. Used for accessing thunder-type inheritances.",
                List.of(
                        "Opens Ancient Thunder Realm passage",
                        "Dimensional access technique"
                ),
                List.of("thunder", "realm", "dimension", "ancient")
        ));


        register(CanonicalEntry.of(
                "T31_rapid_spell_art_xu_decai", "Rapid Spell Art (Xu Decai)", "徐才快速法术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Foundation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A rapid-casting technique learned from Xu Decai. Enables near-instant spell incantations.",
                List.of(
                        "Rapid spell casting",
                        "Reduced incantation time",
                        "Learned from Xu Decai"
                ),
                List.of("speed", "casting", "spell", "xu_decai")
        ));


        register(CanonicalEntry.of(
                "T33_rapid_spell_technique", "Rapid Spell Technique", "快速施法术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's own improved rapid-casting technique. Builds on the Xu Decai method.",
                List.of(
                        "Improved rapid casting",
                        "Wang Lin's refinement"
                ),
                List.of("speed", "casting", "improvement")
        ));


        register(CanonicalEntry.of(
                "T34_body_fixation_art_xiangang", "Body Fixation Art (Xiang'ang)", "香冈定身术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Foundation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A body-fixation/immobilization technique. Learned or adapted from Xiang'ang.",
                List.of(
                        "Immobilizes targets",
                        "Body fixation"
                ),
                List.of("fixation", "immobilize", "control")
        ));


        register(CanonicalEntry.of(
                "T38_life_transformation_spell", "Life Transformation Spell", "化命术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A spell that transforms life force. Can convert life energy between forms.",
                List.of(
                        "Transforms life force",
                        "Life energy conversion"
                ),
                List.of("life", "transformation", "energy")
        ));


        register(CanonicalEntry.of(
                "T41_slaughter_immortal_art", "Slaughter Immortal Art", "杀仙术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A killing art specialized against immortals/cultivators. Part of Wang Lin's offensive arsenal.",
                List.of(
                        "Anti-cultivator killing art",
                        "Slaughter-focused"
                ),
                List.of("slaughter", "killing", "immortal", "offense")
        ));


        register(CanonicalEntry.of(
                "T44_god_slaying_seal_origin", "God-Slaying Seal Origin", "诛神印本源",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The origin/essence of the God-Slaying Seal technique. The fundamental principle behind the seal.",
                List.of(
                        "Origin technique of God-Slaying Seal",
                        "Fundamental seal principle"
                ),
                List.of("god_slaying", "seal", "origin", "fundamental")
        ));


        register(CanonicalEntry.of(
                "T50_life_seizing_hex", "Life-Seizing Hex", "夺命咒",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A hex that seizes the target's life force. Used in combat to weaken or kill.",
                List.of(
                        "Seizes target's life force",
                        "Hex-type technique"
                ),
                List.of("hex", "life", "seizing", "combat")
        ));


        register(CanonicalEntry.of(
                "T55_nine_mysterious_transformations", "Nine Mysterious Transformations", "九玄变化",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A transformation art with 9 forms. Allows the user to assume different shapes and properties.",
                List.of(
                        "9 transformation forms",
                        "Shape-shifting art"
                ),
                List.of("transformation", "nine", "mysterious", "shape")
        ));


        register(CanonicalEntry.of(
                "T90_ji_realm_execution", "Ji Realm Execution", "极境处决",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An execution technique powered by the Ji Realm divine sense. Instantly kills targets below a certain realm.",
                List.of(
                        "Ji Realm-powered instant kill",
                        "Realm-gated execution"
                ),
                List.of("ji_realm", "execution", "killing", "divine_sense")
        ));


        register(CanonicalEntry.of(
                "T96_eyes_suppressing_world", "Eyes Suppressing World", "眼压天地",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A gaze-based technique that suppresses everything in the user's field of vision. Uses divine sense projection.",
                List.of(
                        "Gaze-based suppression",
                        "Divine sense projection"
                ),
                List.of("gaze", "suppression", "divine_sense", "domain")
        ));


        register(CanonicalEntry.of(
                "T100_ice_imitation_dongling_pool", "Ice Imitation (Dongling Pool)", "冰之模仿（东灵池）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Foundation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An ice-type technique imitated from the Dongling Pool. Creates and manipulates ice.",
                List.of(
                        "Ice creation and manipulation",
                        "Imitated technique"
                ),
                List.of("ice", "imitation", "dongling", "elemental")
        ));


        register(CanonicalEntry.of(
                "T105_nine_drops_poison_miao_yin", "Nine Drops Poison (Miao Yin)", "九滴毒（妙音）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A poison technique from Miao Yin. Uses nine drops of concentrated poison.",
                List.of(
                        "Poison attack",
                        "Nine-drop concentration",
                        "Miao Yin origin"
                ),
                List.of("poison", "miao_yin", "nine_drops")
        ));


        register(CanonicalEntry.of(
                "T110_three_life_spell", "Three-Life Spell", "三世咒",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A spell that spans three lives/incarnations. Related to the Samsara Dao.",
                List.of(
                        "Spans three incarnations",
                        "Samsara-linked"
                ),
                List.of("samsara", "three_lives", "reincarnation", "spell")
        ));


        register(CanonicalEntry.of(
                "T135_defying_thunder", "Defying Thunder", "逆雷",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A heaven-defying thunder technique. Goes beyond normal thunder cultivation limits.",
                List.of(
                        "Beyond normal thunder limits",
                        "Heaven-defying property"
                ),
                List.of("thunder", "defying", "heaven_defying")
        ));


        register(CanonicalEntry.of(
                "T140_nine_revolutions_refining", "Nine Revolutions Refining Immortal", "九转炼仙",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A nine-revolution refining technique for immortal-grade items. Part of Wang Lin's crafting arts.",
                List.of(
                        "Nine-revolution refining process",
                        "Immortal-grade crafting"
                ),
                List.of("refining", "crafting", "nine_revolutions", "immortal")
        ));


        register(CanonicalEntry.of(
                "T145_extreme_fire_dao_imitation", "Extreme Fire Dao Imitation", "极火道模仿",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An imitation of the Extreme Fire Dao. A fire-type technique模仿 from observation.",
                List.of(
                        "Fire Dao imitation",
                        "Learned through observation"
                ),
                List.of("fire", "dao", "imitation", "extreme")
        ));


        register(CanonicalEntry.of(
                "AT07_store_all_ji_thunder", "Store All Ji Thunder", "储极雷",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1400"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 7th accompanying thunder — stores all Ji Realm thunder energy. The culmination of the 9-thunder series.",
                List.of(
                        "7th of 9 accompanying thunders",
                        "Stores all Ji thunder energy",
                        "Culmination of thunder series"
                ),
                List.of("thunder", "accompanying", "ji_realm", "storage")
        ));


        register(CanonicalEntry.of(
                "OS04_karma_print", "Karma Print — 4th Original Spell", "因果印·第四元神术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 4th of Wang Lin's Original Spells. A karma-based sealing print that binds cause and effect.",
                List.of(
                        "4th Original Spell",
                        "Karma-based sealing",
                        "Binds cause and effect"
                ),
                List.of("original_spell", "karma", "seal", "fourth")
        ));


        register(CanonicalEntry.of(
                "OS05_life_death_seal", "Life and Death Seal — 5th Original Spell", "生死印·第五元神术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 5th of Wang Lin's Original Spells. Controls life and death of targets within range.",
                List.of(
                        "5th Original Spell",
                        "Life-death control",
                        "Domain-type authority"
                ),
                List.of("original_spell", "life_death", "seal", "fifth")
        ));


        register(CanonicalEntry.of(
                "OS06_true_false_eternal_seal", "True and False Eternal Seal — 6th Original Spell", "真假永恒印·第六元神术",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 6th of Wang Lin's Original Spells. Distinguishes truth from falsehood; creates an eternal seal of distinction.",
                List.of(
                        "6th Original Spell",
                        "Truth-falsehood distinction",
                        "Eternal sealing"
                ),
                List.of("original_spell", "true_false", "eternal", "seal", "sixth")
        ));


        register(CanonicalEntry.of(
                "I138_life_death_domain", "Life and Death Domain", "生死域",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A domain that controls life and death within its area. One of Wang Lin's powerful domain-type abilities.",
                List.of(
                        "Life-death control within domain",
                        "Area-of-effect authority"
                ),
                List.of("domain", "life_death", "area", "authority")
        ));


        register(CanonicalEntry.of(
                "I153_blood_lines_rules", "Blood Lines Rules / Restriction Essence", "血脉规则·禁制本源",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The essence of restriction art combined with bloodline rules. Represents the ultimate fusion of restriction + bloodline.",
                List.of(
                        "Fusion of restriction + bloodline",
                        "Ultimate restriction art form"
                ),
                List.of("restriction", "bloodline", "essence", "fusion")
        ));


        register(CanonicalEntry.of(
                "T136_bloodline_thunder", "Bloodline Thunder", "血脉之雷",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder technique powered by bloodline energy. Combines Ancient God bloodline with thunder cultivation.",
                List.of(
                        "Bloodline-powered thunder",
                        "Ancient God + thunder fusion"
                ),
                List.of("thunder", "bloodline", "ancient_god", "fusion")
        ));


        register(CanonicalEntry.of(
                "T95_flame_dragon", "Flame Dragon", "火龙",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Summons or transforms into a flame dragon. A fire-type offensive technique.",
                List.of(
                        "Flame dragon summon/transformation",
                        "Fire-type offense"
                ),
                List.of("fire", "dragon", "summon", "offense")
        ));


        register(CanonicalEntry.of(
                "T150_nine_star_ancient_god", "Nine-Star Ancient God Power", "九星古神之力",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The power of the nine-star Ancient God. Represents peak Ancient God Tactic cultivation.",
                List.of(
                        "Nine-star Ancient God power",
                        "Peak Ancient God Tactic"
                ),
                List.of("ancient_god", "nine_star", "power", "peak")
        ));


        register(CanonicalEntry.of(
                "T155_extreme_land_sky_life", "Extreme Land Sky Life Dao", "极地天生命道",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An extreme Dao combining land, sky, and life concepts. One of Wang Lin's late-stage Dao comprehensions.",
                List.of(
                        "Combines land + sky + life",
                        "Late-stage Dao comprehension"
                ),
                List.of("dao", "extreme", "land", "sky", "life")
        ));


        register(CanonicalEntry.of(
                "T160_mother_child_dao", "Mother-Child Dao (Withered)", "母子道（枯萎）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A Mother-Child Dao technique in a withered/state. Represents a Dao that has been diminished.",
                List.of(
                        "Mother-Child Dao variant",
                        "Withered/diminished state"
                ),
                List.of("dao", "mother_child", "withered")
        ));


        register(CanonicalEntry.of(
                "T165_wither_dao_pair", "Wither Dao Pair", "枯道对",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A pair of wither-type Dao techniques. Used together for enhanced effect.",
                List.of(
                        "Paired wither Dao techniques",
                        "Enhanced when combined"
                ),
                List.of("dao", "wither", "pair", "combined")
        ));


        register(CanonicalEntry.of(
                "E08_taichu_essence", "Taichu Essence", "太初本源",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Taichu (Primordial Beginning) essence. One of the origin essences Wang Lin comprehended.",
                List.of(
                        "Origin essence comprehension",
                        "Taichu/Primordial concept"
                ),
                List.of("essence", "taichu", "origin", "primordial")
        ));


        register(CanonicalEntry.of(
                "E09_miemie_essence", "Miemie Essence", "灭灭本源",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Miemie (Annihilation-Extinction) essence. One of the origin essences Wang Lin comprehended.",
                List.of(
                        "Origin essence comprehension",
                        "Annihilation-Extinction concept"
                ),
                List.of("essence", "miemie", "annihilation", "extinction")
        ));


        register(CanonicalEntry.of(
                "T170_cultivator_dao_avatar", "Cultivator Dao Avatar", "修仙分身道",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The technique of creating a cultivator clone/avatar that can cultivate independently.",
                List.of(
                        "Clone/avatar creation technique",
                        "Independent cultivation by avatar"
                ),
                List.of("clone", "avatar", "cultivation", "separation")
        ));


        register(CanonicalEntry.of(
                "B01_ancient_god_body", "Ancient God Body", "古神之体",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The physical body transformation into an Ancient God form via the Ancient God Tactic.",
                List.of(
                        "Ancient God body transformation",
                        "Outer-body cultivation result"
                ),
                List.of("ancient_god", "body", "transformation")
        ));


        register(CanonicalEntry.of(
                "B02_undying_ancient_body", "Undying Ancient Body", "不古道身",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Undying Ancient God body form. A higher evolution of the Ancient God Body.",
                List.of(
                        "Undying Ancient God form",
                        "Higher body evolution"
                ),
                List.of("ancient_god", "undying", "body", "evolution")
        ));


        register(CanonicalEntry.of(
                "companion_xu_liguo_devil", "Xu Liguo (Devil Form)", "许立国（魔头形态）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("early RI"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Xu Liguo's devil form — one of Wang Lin's primary companions. A sentient devil head with personality.",
                List.of(
                        "Sentient devil companion",
                        "Comic relief + combat support",
                        "Loyal but cowardly personality"
                ),
                List.of("companion", "devil", "xu_liguo", "sentient")
        ));


        register(CanonicalEntry.of(
                "companion_xu_liguo_first", "Xu Liguo (First Form)", "许立国（初始形态）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("early RI"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Xu Liguo's first/devouring form before full awakening.",
                List.of(
                        "Initial devil form",
                        "Devouring capability"
                ),
                List.of("companion", "devil", "xu_liguo", "initial")
        ));


        register(CanonicalEntry.of(
                "companion_xie_qing_guard", "Xie Qing (Immortal Guard)", "谢青（仙卫）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Xie Qing, one of Wang Lin's immortal guards. A powerful combat companion.",
                List.of(
                        "Immortal guard companion",
                        "Combat specialist"
                ),
                List.of("companion", "guard", "xie_qing", "combat")
        ));


        register(CanonicalEntry.of(
                "I130_tattoo_talisman_speed", "Tattoo Talisman (Speed Boost)", "刺青符（加速）",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A tattoo talisman that boosts movement speed. Applied to the body.",
                List.of(
                        "Movement speed boost",
                        "Body-applied talisman"
                ),
                List.of("talisman", "tattoo", "speed", "movement")
        ));


        register(CanonicalEntry.of(
                "I20_short_sword_seals_shadows", "Short Sword (Seals Shadows)", "短剑·封影",
                CanonicalCategory.TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Foundation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin developed or obtained this technique during his cultivation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A short sword that can seal shadows. One of Wang Lin's early weapons.",
                List.of(
                        "Shadow-sealing capability",
                        "Early weapon"
                ),
                List.of("sword", "shadow", "seal", "weapon")
        ));

    }
}
