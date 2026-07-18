package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalKnowledge — abstract lore: Dao truths, cosmology, world mechanics.
 *
 * <p>Distinct from items / techniques / pets / companions / experiences. This
 * registry captures the conceptual knowledge Wang Lin attests to — the
 * cosmological structure, the cultivation ladder, the Prime Directive, etc.
 * When the player asks Wang Lin "how does the world work?", these are the
 * subjects he can explain.
 */
public final class CanonicalKnowledge extends AbstractSubRegistry {

    public CanonicalKnowledge() {
        super("CanonicalKnowledge");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "knowledge_nested_sealed_cosmology", "Nested-Sealed Cosmology (the world-as-farm truth)", "嵌套封印宇宙",
                CanonicalCategory.KNOWLEDGE,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's lived understanding — he killed the Seven-Colored Daoist to escape this truth."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Every 'world' is a sealed farm owned by a higher-tier cultivator. The Cave World is sealed by the Seven-Colored Daoist; the Sealed Realm is sealed AGAIN by the Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe) to prevent Third-Step cultivators from rising. Wang Lin's final act is to kill the Seven-Colored Daoist, become the new world-owner, and Transcend.",
                List.of("7 nested-sealed layers",
                        "Each layer (except Root Dao) can be owned by a higher-tier cultivator",
                        "Owner imposes a cultivation ceiling",
                        "To escape, the owned must kill the owner"),
                List.of("knowledge", "cosmology", "nested_sealed", "joss_flame", "cave_world", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_joss_flame_economy", "Joss Flame Economy (the harvest loop)", "香火经济",
                CanonicalCategory.KNOWLEDGE,
                Provenance.explicit("Renegade Immortal", List.of("Cave World era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's lived understanding — he became the new Cave World owner."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Mortal faith produces Joss Flames. The world-owner harvests 30% of all Joss Flames produced within the world. The remaining 70% sustains the cultivators inside. This is the harvest loop: mortals believe → Joss Flames produced → owner takes 30% → cultivators inside take 70% → cultivators die → souls recycled → mortals persist. Killing the owner = stopping the harvest.",
                List.of("Mortal faith → Joss Flames",
                        "Owner harvests 30%",
                        "Cultivators take 70%",
                        "Killing the owner stops the harvest"),
                List.of("knowledge", "joss_flame", "economy", "cave_world", "harvest_loop")
        ));

        register(CanonicalEntry.of(
                "knowledge_cultivation_ladder", "The 17-Stage Cultivation Ladder", "修炼阶梯",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin walked every step of this ladder."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "First Step (1–7): Qi Condensation → Foundation → Core Formation → Nascent Soul → Soul Formation → Soul Transformation → Ascendant. Transitional (8–9): Illusory Yin → Corporeal Yang. Second Step (10–13): Nirvana Scryer → Nirvana Cleanser → Nirvana Fruit → Spirit Seizer (Dao Realm). Third Step+ (14–17): True Immortal → Ancient → Paragon → Heaven-Trampling/Transcendence.",
                List.of("17 stages across 4 steps",
                        "Each realm CHANGES REALITY — unlocks new capability classes",
                        "Not just stats — perception shifts"),
                List.of("knowledge", "cultivation_ladder", "first_step", "second_step", "third_step", "fourth_step")
        ));

        register(CanonicalEntry.of(
                "knowledge_realm_sealing_grand_array", "Realm-Sealing Grand Array (the seal)", "封界大阵",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin reset this array as Lord of the Sealed Realm."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The seal that suppresses Third-Step cultivators in the Sealed Realm. Its spirit is the Heaven-Splitting Axe. The biggest flaw: it also restricts the entrance of Joss Flames, weakening the world's cultivators. Wang Lin's Unnamed Wheel Formation is the replacement that stops Outer-Realm cultivators from entering while NOT restricting Joss Flames.",
                List.of("Suppresses Third-Step cultivators in the Sealed Realm",
                        "Spirit: Heaven-Splitting Axe",
                        "Flaw: restricts Joss Flame entrance",
                        "Replaced by Wang Lin's Unnamed Wheel Formation"),
                List.of("knowledge", "restriction", "sealed_realm", "heaven_splitting_axe", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_14_essences_truth", "The 14 Essences of Samsara Dao", "十四本源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 127 — Ch. 2087"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's signature Dao — he is the only known cultivator to walk all 14."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 14 Essences: 6 substantial (Metal, Wood, Water, Fire, Earth, Thunder — each forms a True Body, fuse into Five Elements True Body), 4 virtual (Life-Death, Karma, True-False, Reincarnation — conceptual laws, no True Body), 4 special (Primordial/Taichu, Silent Extinction/Miemie, Restriction, Slaughter — each has own True Body). Completing all 14 (esp. the 14th, Reincarnation) = Heaven Trampling.",
                List.of("6 substantial + 4 virtual + 4 special",
                        "Each substantial/special essence forms a True Body",
                        "Completing all 14 = Heaven Trampling"),
                List.of("knowledge", "essences", "samsara_dao", "wang_lin_signature", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_samsara_incarnation_technique", "One Billion Samsara Incarnations", "一亿分身",
                CanonicalCategory.CLONES,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's signature incarnation technique — requires the Heaven-Defying Bead or equivalent soul-vessel artifact."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Each incarnation is a fragment of Wang Lin's soul that lives a complete life in a different context (different world, identity, circumstances). When the incarnation dies, its experiences return to Wang Lin as Dao insight. Each incarnation is REAL — they live, suffer, love, die. When Wang Lin reabsorbs them, he gains their lifetime of insight. This is why Samsara Dao is the path to Reincarnation Essence.",
                List.of("1 billion soul-fragment incarnations",
                        "Each incarnation lives a complete life",
                        "Reabsorption = Dao insight gain",
                        "Requires soul-vessel artifact (the bead)"),
                List.of("knowledge", "samsara", "incarnation", "wang_lin_signature", "wang_lin_unique", "heaven_defying_bead")
        ));

        register(CanonicalEntry.of(
                "knowledge_ancient_clan_truth", "Ancient Clan (the Ancient One fusion)", "古族真相",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1539+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's lived Ancient Clan fusion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Ancient Clan is a fusion of three lineages: Ancient God, Ancient Demon, Ancient Devil. Wang Lin's three clones (Ancient God main body, Ancient Demon statue clone, Ancient Devil corpse clone) fused back into a single Ancient One. Star progression: 13★ (7G/6D) → 20★ (7G/7M/6D) → 24★ (8G/8M/8D) → 27★ (9G/8M/9D) peak.",
                List.of("Three-way fusion: God + Demon + Devil",
                        "Wang Lin fused his three clones into an Ancient One",
                        "Star progression: 13★ → 27★"),
                List.of("knowledge", "ancient_clan", "ancient_god", "ancient_demon", "ancient_devil", "fusion")
        ));

        register(CanonicalEntry.of(
                "knowledge_prime_directive", "The Prime Directive (Reality is Objective)", "客观真理",
                CanonicalCategory.KNOWLEDGE,
                Provenance.inferred("Renegade Immortal", List.of("throughout"), 5,
                        "Cross-cutting theme — Wang Lin's perspective on reality."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's lived Dao — reality is objective; cultivation changes understanding, not existence."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Never hide or reveal objects because of the player's level. Hide or reveal interactions according to the laws of the world. The world is objective and exists independently of the player. Cultivation increases a character's ability to perceive, understand, and interact with deeper layers of reality — it does not create or replace reality. A mortal on Planet Suzaku does not know they live inside a sealed farm; that ignorance does not unmake the farm. Cultivation lets you PERCEIVE the seal, then BREAK it.",
                List.of("Reality is objective — exists independently of perception",
                        "Cultivation changes understanding, not existence",
                        "Mortal ignorance does not unmake the farm",
                        "Cultivation lets you perceive, then break"),
                List.of("knowledge", "prime_directive", "objective_reality", "wang_lin_unique", "philosophy")
        ));

        register(CanonicalEntry.of(
                "knowledge_dao_comprehension_track", "Dao Comprehension Track (separate from realm)", "悟道之路",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's lived experience."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Dao comprehension is a separate track from realm progression. A cultivator can be at Paragon realm with 0 Essences, or at Nirvana Scryer with 14. Essences do not 'unlock at realm X' (per user correction #5) — they are comprehended through insight, life experience, and Dao reflection. Wang Lin's completion order tracks his life events (Slaughter after killing Qing Shui's father; Reincarnation after the Reincarnation Pool).",
                List.of("Dao comprehension is a separate track from realm",
                        "Essences do NOT unlock at realm X (correction #5)",
                        "Comprehended via insight + life experience",
                        "Wang Lin's completion order tracks his life events"),
                List.of("knowledge", "dao_comprehension", "essences", "correction_5", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_reincarnation_truth", "The Reincarnation Truth (bead sent back through time)", "轮回真相",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1295+ Dream Dao", "Ch. 1943+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's defining time-loop truth."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "The Heaven-Defying Bead was sent back through time by Wang Lin's future clone Lu Mo via Dream Dao. Lu Mo borrowed the Realm-Defining Compass from Old Man Miēshēng to blast the bead open. Lu Mo also left information in the White Hair Strand and Infant Skull to resolve the main body's confusion about reincarnation. The Ji Qiong skull had a divine sense left by Slaughter telling how to revive Li Muwan. The Dao Country Imperial Teacher is the missing bead portion (the Boundary Compass Treasure Spirit).",
                List.of("Bead was sent back through time by Lu Mo via Dream Dao",
                        "Lu Mo borrowed Realm-Defining Compass from Old Man Miēshēng",
                        "Ji Qiong skull contained Slaughter's divine sense on revival",
                        "Dao Country Imperial Teacher = Boundary Compass Treasure Spirit"),
                List.of("knowledge", "reincarnation", "lu_mo", "dream_dao", "time_travel", "wang_lin_unique", "li_muwan_revival")
        ));

        // ── Inheritance sources (per user priority list) ─────────────
        register(CanonicalEntry.withSpec(
                "knowledge_situ_nan_inheritance", "Situ Nan's Inheritance (Ancient God remnant soul in the Bead)", "司徒南传承",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 8 (awakened)", "Ch. 78 onward (transmission)", "Ch. 441 (mosquito beast given)"), 5,
                        "Situ Nan is the remnant soul inside the Heaven-Defying Bead; transmitted Underworld Ascension Method, Vermilion Bird Burning Heaven Art, Finger of Death trio."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan — the remnant soul inside the Heaven-Defying Bead — transmitted multiple arts. Per Wang Lin's contract, Situ Nan's permission is required for redistribution. Situ Nan himself eventually reawakened his body via the Mosquito Beast (Ch. 441)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Situ Nan is the remnant soul sealed inside the Heaven-Defying Bead. He is the source of Wang Lin's earliest cultivation transmissions: the Underworld Ascension Method (Ch. 86), the Vermilion Bird Burning Heaven Art, the four-finger trio (Finger of Death / Demonic / Underworld / Yellow Springs), and the Vermilion Bird Sequence. Situ Nan is the 2nd-Generation Vermilion Bird Divine Emperor; Wang Lin inherits his lineage as 6th-Generation. Situ Nan eventually reawakened (reformed a physical body via the Mosquito Beast pair Wang Lin gifted him, Ch. 441) and walks his own path.",
                List.of("Remnant soul inside the Heaven-Defying Bead",
                        "Transmitted Underworld Ascension Method (Ch. 86)",
                        "Transmitted Vermilion Bird Burning Heaven Art",
                        "Transmitted the 4-finger trio (Death / Demonic / Underworld / Yellow Springs)",
                        "2nd-Gen Vermilion Bird Divine Emperor — Wang Lin's lineage predecessor",
                        "Reawakened via Mosquito Beast gift (Ch. 441)"),
                List.of("knowledge", "inheritance", "situ_nan", "ancient_god", "vermilion_bird", "underworld_ascension", "wang_lin_unique"),
                "situ_nan_inheritance"
        ));

        register(CanonicalEntry.withSpec(
                "knowledge_tu_si_remnants", "Tu Si Remnants (Ancient God Tu Si's memory legacy)", "涂司遗泽",
                CanonicalCategory.ANCIENT_GOD,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 180 (Restriction Flag method)", "Ch. 190 (Ancient God Tactic)", "Ch. 941 (God Slaying Spear)", "Ch. 1082 (Trident)", "Ch. 758 (Leather Armour)", "Ch. 980 (Azure Shield)"), 5,
                        "Tu Si is the Ancient God whose memory legacy Wang Lin inherited after passing the Restrictions Mountain Trial."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Tu Si's memory legacy after Wang Lin became the 4th person to complete the Land of the Ancient God Restrictions Mountain Trial (Ch. ~180)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tu Si (涂司) is the Ancient God whose memory-fragment legacy Wang Lin inherited after completing the Land of the Ancient God Restrictions Mountain Trial (4th person ever). Tu Si transmitted: the Ancient God Tactic (Ch. 190 — essence: plunder), the Restriction Flags Refining Method (Ch. ~180), the Ancient Restrictions foundation, and a portfolio of Ancient God treasures that Wang Lin would later claim (God Slaying Spear illusory Ch. 941, Ancient God Trident Ch. 1082, Ancient God Leather Armour Ch. 758, Azure Ancient God Shield Ch. 980, Ancient God Furnace Ch. 838, Ancient God Bracer). The Ancient God Tactic drives Wang Lin's 1★→27★ Ancient God progression. Wang Lin's hair turns white after absorbing the full legacy.",
                List.of("Ancient God memory-fragment legacy",
                        "Transmitted Ancient God Tactic (Ch. 190) — essence: plunder",
                        "Transmitted Restriction Flags Refining Method (Ch. ~180)",
                        "Transmitted Ancient Restrictions foundation",
                        "Source of Ancient God treasure portfolio (Spear, Trident, Armour, Shield, Furnace, Bracer)",
                        "Drives 1★→27★ Ancient God progression",
                        "Hair turns white after absorbing the full legacy"),
                List.of("knowledge", "inheritance", "tu_si", "ancient_god", "ancient_god_tactic", "restriction_flag", "wang_lin_unique"),
                "tu_si_remnants"
        ));

        register(CanonicalEntry.withSpec(
                "knowledge_qing_lin_spell", "Qing Lin's Spell (Immortal Emperor Qing Lin's transmission)", "青霖传承",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~625 (Divine Soul Brush acquired)", "Ch. 619 (Heavenly Devil Sound)"), 5,
                        "Qing Lin is Immortal Emperor Qing Lin; his cave in Demon Spirit Land contained celestial wine, the Divine Soul Brush, and the Heavenly Devil Sound art. Supplementary details (xian-ni.fandom.com/wiki/Qing_Lin, xian-ni.fandom.com/wiki/Wang_Lin): 'Qing Lin was the strongest Celestial Emperor and the strongest person inside the Sealed Realm during the reign of the Celestial Domain.' 'In the 70 years during which Wang Lin was missing, Qing Lin used a life-threatening spell to seal the Inner Realm for 30 years.'"),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Immortal Emperor Qing Lin's false cave in the Demon Spirit Land. The Divine Soul Brush was originally Qing Lin's gift to his daughter Qing Shuang."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Qing Lin (青霖) is the Immortal Emperor whose false cave in the Demon Spirit Land Wang Lin plundered (~Ch. 625). The cave contained celestial wine (Ch. ~625), the Divine Soul Brush / Golden Celestial Brush (点仙笔 — created by Qing Lin as a gift to his daughter Qing Shuang), and the Heavenly Devil Sound art (Ch. 619/625). The Divine Soul Brush was later given to Li Qianmei (Ch. 1178). Qing Lin's transmission contributed to Wang Lin's celestial-tier spell portfolio.",
                List.of("Immortal Emperor Qing Lin's false cave in Demon Spirit Land",
                        "Plundered celestial wine (Ch. ~625)",
                        "Acquired Divine Soul Brush (created for Qing Shuang)",
                        "Acquired Heavenly Devil Sound (Ch. 619/625)",
                        "Divine Soul Brush later given to Li Qianmei (Ch. 1178)",
                        "Strongest Celestial Emperor inside Sealed Realm during Celestial Domain reign",
                        "Used a life-threatening spell to seal the Inner Realm for 30 years during Wang Lin's 70-year absence"),
                List.of("knowledge", "inheritance", "qing_lin", "celestial", "divine_soul_brush", "heavenly_devil_sound", "wang_lin_unique"),
                "qing_lin_inheritance"
        ));

        register(CanonicalEntry.of(
                "knowledge_bai_fan_inheritance", "Bai Fan's Six Paths Triple Arts Inheritance", "白发传承",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 783 (Call the Wind)", "Ch. 914 (Summon the Rain)", "Ch. 919 (Magic Arsenal)", "Ch. 1105 (Mountains Crumble)", "Ch. 1119 (Falling Star)", "Ch. 1156 (Heavenly Fate Finger)", "Ch. 1582 (Lands Collapse)", "Ch. 1582 (Dark Moon Clear Skies)"), 5,
                        "Bai Fan (the White-Haired Cultivator) taught Wang Lin a portfolio of celestial-tier spells; Mountains Crumble is taught by Qing Shui on behalf of Bai Fan per Baidu."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Bai Fan — the White-Haired Cultivator who taught Wang Lin celestial-tier arts including Call the Wind, Summon the Rain, Magic Arsenal, Mountains Crumble (via Qing Shui), Falling Star, Heavenly Fate Finger, Lands Collapse, Dark Moon Clear Skies."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Bai Fan (白发) is the White-Haired Cultivator who transmitted to Wang Lin a portfolio of celestial-tier arts collectively known as the Six Paths Triple Arts. The transmission includes: Call the Wind (Ch. 783), Summon the Rain (Ch. 914), Magic Arsenal (Ch. 919), Falling Star (Ch. 1119), Heavenly Fate Finger (Ch. 1156), Lands Collapse (Ch. 1582), Dark Moon Clear Skies (Ch. 1582). Mountains Crumble (Ch. 1105) is taught by Qing Shui on behalf of Bai Fan. The Six Paths Triple Arts form a key pillar of Wang Lin's celestial-tier combat arts.",
                List.of("White-Haired Cultivator transmission",
                        "Six Paths Triple Arts portfolio",
                        "Call the Wind (Ch. 783)",
                        "Summon the Rain (Ch. 914)",
                        "Magic Arsenal (Ch. 919)",
                        "Mountains Crumble via Qing Shui (Ch. 1105)",
                        "Falling Star (Ch. 1119)",
                        "Heavenly Fate Finger (Ch. 1156)",
                        "Lands Collapse + Dark Moon Clear Skies (Ch. 1582)"),
                List.of("knowledge", "inheritance", "bai_fan", "celestial", "six_paths_triple_arts", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_dun_tian_inheritance", "Dun Tian's Soul Refining Sect Inheritance", "敦天传承",
                CanonicalCategory.SOUL_CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 384 (Soul Flag Production Method)"), 5,
                        "Dun Tian is the predecessor of the Soul Refining Sect who gifted Wang Lin the Soul Flag Production Method + Billion Soul Flag."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Dun Tian — predecessor of the Soul Refining Sect — as a mid-Nascent-Soul era gift."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dun Tian (敦天) is the predecessor of the Soul Refining Sect who gifted Wang Lin the Soul Flag Production Method (Ch. 384) and the Billion Soul Flag / Ten-Billion Soul Banner. The Soul Flag Production Method splits into three parts: Soul Refining, Soul Extracting, Soul Sealing. The Billion Soul Flag contains 37 main souls + 1 billion ordinary souls, plus a 4th mysterious soul that can battle Ascension cultivators. This transmission is the foundation of Wang Lin's soul-cultivation track.",
                List.of("Soul Refining Sect predecessor transmission",
                        "Soul Flag Production Method (Ch. 384) — three-part: Refine, Extract, Seal",
                        "Billion Soul Flag / Ten-Billion Soul Banner",
                        "37 main souls + 1 billion ordinary souls + 4th mysterious soul",
                        "Foundation of Wang Lin's soul-cultivation track"),
                List.of("knowledge", "inheritance", "dun_tian", "soul_refining_sect", "soul_flag", "billion_soul_flag", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_qing_shui_inheritance", "Immortal Lord Qing Shui's Inheritance", "清水传承",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 949 (Nether Guide)", "Ch. 1105 (Mountains Crumble transmitted on Bai Fan's behalf)", "Ch. 1509 (Slaughter Dao initial)", "Ch. 1561 (Slaughter Sword)"), 5,
                        "Qing Shui is Wang Lin's senior brother; transmitted Nether Guide, Mountains Crumble, and the Slaughter Sword."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Immortal Lord Qing Shui (王林's senior brother)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Immortal Lord Qing Shui (清水) is Wang Lin's senior brother in the Vermilion Bird Divine Sect. He transmitted the Nether Guide (Ch. 949), Mountains Crumble (Ch. 1105, on Bai Fan's behalf), and the Slaughter Sword (Ch. 1561) which fuses with Wang Lin's Slaughter Crystal to complete the Slaughter Essence. Qing Shui used the Ji Realm to kill Russell for offending Wang Lin. The transmission contributed to Wang Lin's Slaughter Dao (initial: Ch. 1509).",
                List.of("Wang Lin's senior brother transmission",
                        "Nether Guide (Ch. 949)",
                        "Mountains Crumble (Ch. 1105, on Bai Fan's behalf)",
                        "Slaughter Sword (Ch. 1561) — fuses with Slaughter Crystal → Slaughter Essence",
                        "Contributed to Slaughter Dao (initial: Ch. 1509)"),
                List.of("knowledge", "inheritance", "qing_shui", "celestial", "slaughter_dao", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_lian_daofei_transmission", "Lian Daofei's Madman Transmission", "炼道疯传承",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Nether Beast era", "Ch. 1533 (Li Guang Bow Dao)", "Ch. 1543 (Seven-Colored Lance)", "Ch. 1509+ (Indestructible Immortal Body)"), 5,
                        "Lian Daofei is the Madman; brother of Immortal Emperor Lian Daozhen; met Wang Lin inside the Nether Beast."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Transmitted by Lian Daofei (madman, brother of Immortal Emperor Lian Daozhen) inside the Nether Beast."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Lian Daofei (炼道疯) — the Madman — is the brother of Immortal Emperor Lian Daozhen. Met Wang Lin inside the Nether Beast (where he was imprisoned). Fused his supreme Immortal bloodline into Wang Lin and imparted: Indestructible Immortal Body (initially rejected by Wang Lin's Dao-Ancient bloodline; later inspired Undying Ancient Finger + Undying Ancient Body), Li Guang's Heaven-Shattering Bow Dao (Ch. 1533), Seven-Colored Lance (Ch. 1543), and numerous other divine abilities. Later guided Wang Lin across the 4th Heaven-Trampling Bridge.",
                List.of("Brother of Immortal Emperor Lian Daozhen",
                        "Encountered inside the Nether Beast",
                        "Fused supreme Immortal bloodline into Wang Lin",
                        "Transmitted Indestructible Immortal Body",
                        "Transmitted Li Guang's Heaven-Shattering Bow Dao (Ch. 1533)",
                        "Transmitted Seven-Colored Lance (Ch. 1543)",
                        "Guide for the 4th Heaven-Trampling Bridge"),
                List.of("knowledge", "inheritance", "lian_daofei", "indestructible_immortal_body", "li_guang_bow", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_dao_master_blue_dream", "Dao Master Blue Dream's Transmission", "蓝梦道主传承",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1321 (Light and Shadow Shield)", "Ch. 1322 (Dao Fusion)"), 5,
                        "Dao Master Blue Dream transmitted Dao Fusion + Light and Shadow Shield."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Dao Master Blue Dream."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dao Master Blue Dream (蓝梦道主) transmitted to Wang Lin: Dao Fusion (Ch. 1322 — the fusion of Call the Wind + Summon the Rain + Celestial Sealing Stamp into Rain and Wind World), and Light and Shadow Shield (Ch. 1321 — creates a shield from all vitality/light in the world; blocks AND reflects enemy attacks). Dao Master Blue Dream is a key celestial-tier teacher in Wang Lin's mid-to-late game.",
                List.of("Transmitted Dao Fusion (Ch. 1322)",
                        "Transmitted Light and Shadow Shield (Ch. 1321)",
                        "Source of Wang Lin's Rain and Wind World composite"),
                List.of("knowledge", "inheritance", "dao_master_blue_dream", "celestial", "dao_fusion", "light_shadow_shield", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_ye_mo_inheritance", "Ye Mo's Inheritance (Three Palaces: Heaven/Earth/Human)", "夜魔传承",
                CanonicalCategory.ANCIENT_DEVIL,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1478 (Earth Palace)", "Ch. 1768 (Ye Mo's left arm in Dong Ling Pool ice)"), 5,
                        "Ye Mo is the ancient devil whose inheritance comprises three palaces: Heaven, Earth, Human."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Ye Mo — the ancient devil. Three-palace inheritance: Heaven, Earth, Human."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ye Mo (夜魔) is the ancient devil whose inheritance comprises three palaces (天宫/地宫/人宫 — Heaven, Earth, Human). Wang Lin acquired the Earth Palace (Ch. 1478) and a palm-sized piece of ice from an imitation Dong Ling Pool containing Ye Mo's left arm (Ch. 1768). The Earth Palace is one of Wang Lin's storage-tier treasures. The Ye Mo inheritance is a major late-game Ancient Devil lineage transmission.",
                List.of("Ancient devil three-palace inheritance (Heaven / Earth / Human)",
                        "Earth Palace acquired (Ch. 1478)",
                        "Ye Mo's left arm in Dong Ling Pool ice (Ch. 1768)",
                        "Late-game Ancient Devil lineage transmission"),
                List.of("knowledge", "inheritance", "ye_mo", "ancient_devil", "earth_palace", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_great_soul_sect_gifts", "Great Soul Sect Founder's Three Promised Gifts", "大魂宗三礼",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1838 (Space Stone)", "Ch. ~1840 (Soul Eye Jade)", "Ch. 1843 (Water Essence Drop)"), 5,
                        "Three promised gifts from the founder of the Great Soul Sect."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Three promised gifts from the founder of the Great Soul Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The founder of the Great Soul Sect promised Wang Lin three quasi-Third-Step-tier gifts: (1) Space Stone (Ch. 1838 — extract one item from storage without damage; nurtures a Heavenly Dao), (2) Jade (Soul Eye Dao) (Ch. ~1840 — see changes in Immortal Astral Continent ONCE; divinate ONE thing; formed by founder's lifetime Soul Eye Dao cultivation), (3) Drop of Crystal Clear Water / Water Essence Drop (Ch. 1843 — refined from 99 rivers with multiple water-essence cultivators; helped complete Water Essence).",
                List.of("Three promised gifts from Great Soul Sect founder",
                        "1st: Space Stone (Ch. 1838)",
                        "2nd: Jade (Soul Eye Dao) (Ch. ~1840) — single-use divination",
                        "3rd: Water Essence Drop (Ch. 1843) — refined from 99 rivers",
                        "All quasi-Third-Step-tier"),
                List.of("knowledge", "inheritance", "great_soul_sect", "space_stone", "soul_eye_jade", "water_essence_drop", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_war_god_shrine_inheritance", "War God Shrine's Divine Path Inheritance", "战神殿神道",
                CanonicalCategory.CULTIVATION,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 129 (Divine Path)", "Ch. 211 (Cultivator Clone created)"), 5,
                        "The War God Shrine transmitted Divine Path; used by Wang Lin to create his first clone."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from the War God Shrine — Divine Path technique used to create Wang Lin's first Cultivator Clone."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The War God Shrine (战神殿) transmitted to Wang Lin the Divine Path (神道诀, Ch. 129) — the technique he used to create his first Cultivator Clone (Ch. 211). The clone has no cultivation and originally a 30-year lifespan, but helps breakthrough to Nascent Soul. Wang Lin later fused the clone back into the Main Body, removing the lifespan drawback via the Ji Realm conflict. The War God Shrine also transmitted a refining technique.",
                List.of("War God Shrine transmission",
                        "Divine Path (Ch. 129) — creates Cultivator Clone",
                        "Clone has no cultivation + 30-year lifespan (originally)",
                        "Helps breakthrough to Nascent Soul",
                        "Lifespan drawback removed via Ji Realm conflict"),
                List.of("knowledge", "inheritance", "war_god_shrine", "divine_path", "cultivator_clone", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_immortal_emperor_qing_lin_cave", "Immortal Emperor Qing Lin's False Cave (Demon Spirit Land)", "青霖假洞府",
                CanonicalCategory.KNOWLEDGE,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~625"), 5,
                        "The false cave in Demon Spirit Land left by Immortal Emperor Qing Lin."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Plundered by Wang Lin in the Demon Spirit Land (~Ch. 625)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The false cave (假洞府) left by Immortal Emperor Qing Lin in the Demon Spirit Land. Wang Lin plundered it (~Ch. 625), acquiring: Celestial Wine (in a jug — drinking or spilling grants temporary celestial-spirit-energy boost), the Divine Soul Brush / Golden Celestial Brush (originally created by Qing Lin as a gift to his daughter Qing Shuang), and the Heavenly Devil Sound art. The brush was later given to Li Qianmei (Ch. 1178).",
                List.of("False cave in Demon Spirit Land",
                        "Plundered by Wang Lin (~Ch. 625)",
                        "Yielded: Celestial Wine (jug)",
                        "Yielded: Divine Soul Brush (created for Qing Shuang)",
                        "Yielded: Heavenly Devil Sound art"),
                List.of("knowledge", "cave", "qing_lin", "demon_spirit_land", "celestial_wine", "divine_soul_brush", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_venerates_ancient_clan_truth", "The Three-Way Ancient Clan Fusion Truth (God+Demon+Devil)", "三族合一",
                CanonicalCategory.ANCIENT_CLAN,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1539 (Ancient Clan awakening)", "Ch. 1705 (24-star)", "Ch. 2003 (27-star peak)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's lived Ancient One fusion — unique to his three-clone setup."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Ancient Clan is not a single lineage but a fusion of three: Ancient God (Wang Lin's main body, Tu Si inheritance), Ancient Demon (statue clone from Land of Demonic Spirits, Ch. 1002), Ancient Devil (corpse clone from Daogu Yemo's tomb, Ch. ~1000). When all three fuse back into the main body, Wang Lin becomes an Ancient One. Star progression: 13★ (7G/6D, Ch. 1539) → 24★ (8G/8M/8D, Ch. 1705) → 27★ peak (9G/8M/9D, Ch. 2003). Each star represents a God/Demon/Devil soul-star absorbed via the Ancient God Tactic.",
                List.of("Three-way fusion: God + Demon + Devil",
                        "Ancient God (main body, Tu Si)",
                        "Ancient Demon (statue clone, Land of Demonic Spirits)",
                        "Ancient Devil (corpse clone, Daogu Yemo's tomb)",
                        "13★ → 24★ → 27★ star progression",
                        "Each star = one God/Demon/Devil soul-star"),
                List.of("knowledge", "ancient_clan", "fusion", "ancient_god", "ancient_demon", "ancient_devil", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_accompanying_thunders_truth", "The Nine Accompanying Thunders", "九道伴雷",
                CanonicalCategory.JI_REALM,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1368"), 5,
                        "Cross-refs RICanonicalDatabase AT01–AT09."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed; Wang Lin's 9 accompanying thunders are a unique signature."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's nine accompanying thunders, condensed at Ch. 1368. 1st–6th: standard accompanying thunders (AT01–AT06). 7th: Store All Ji Thunder (储极雷, AT07). 8th: Bloodline Thunder (血脉雷, AT08). 9th: Defying Thunder (逆天雷, AT09) — never appeared since the beginning of time; formed via defying will, unprecedented in history. The Ji Realm was eventually absorbed into the 7th accompanying thunder (Store All Ji Thunder).",
                List.of("9 accompanying thunders (condensed Ch. 1368)",
                        "1st–6th: standard accompanying thunders",
                        "7th: Store All Ji Thunder (absorbed the Ji Realm)",
                        "8th: Bloodline Thunder",
                        "9th: Defying Thunder — unprecedented in history"),
                List.of("knowledge", "thunders", "accompanying", "ji_realm", "ji_thunder", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_vermilion_bird_awakenings", "The Four Vermilion Bird Awakenings", "朱雀四觉",
                CanonicalCategory.CELESTIAL_TECHNIQUES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1089 (1st)", "Ch. ~1200 (2nd)", "Ch. ~1400 (3rd)", "Ch. 1412 (4th)"), 5,
                        "Cross-refs RICanonicalDatabase VA01–VA04."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Situ Nan / Vermilion Bird lineage inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The four Vermilion Bird Mark awakenings. 1st (Ch. 1089): VA01 — first mark. 2nd (Ch. ~1200): VA02 — second mark. 3rd (Ch. ~1400): VA03 — third mark. 4th (Ch. 1412): VA04 — fourth and final mark; the 9 colors fuse into 1. The awakenings fuel the Ethereal Fire (虚火, Ch. 1412) and ultimately the Fire Essence / Fire Essence True Body. Feeds back into the Vermilion Bird Burning Heaven Art.",
                List.of("4 Vermilion Bird Mark awakenings",
                        "1st Awakening (Ch. 1089)",
                        "2nd Awakening (Ch. ~1200)",
                        "3rd Awakening (Ch. ~1400)",
                        "4th Awakening (Ch. 1412) — 9 colors fuse into 1",
                        "Fuels Ethereal Fire and Fire Essence"),
                List.of("knowledge", "vermilion_bird", "awakening", "ethereal_fire", "fire_essence", "situ_nan", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "knowledge_origin_swords_truth", "The Six Origin Swords", "六本源剑",
                CanonicalCategory.ORIGIN_ENERGY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1561 (Slaughter)", "Ch. 1625 (Thunder)", "Ch. 1715 (Six Origin Swords complete)"), 5,
                        "Cross-refs RICanonicalDatabase I32 / T166."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed from void-gate vortex power; each embodies one Origin."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Six Origin Swords (六本源剑) are self-condensed from the strange power within the vortex formed by the collapse of the void gate. Each embodies one Origin: Fire, Life-Death, Karma, True-False, Slaughter, Restriction. Set completes power when all six are condensed. Anchors Wang Lin's 14-Essence Dao as physical weapon-portals. The Slaughter Origin Sword is originally Qing Shui's (Ch. 1561); the Thunder Origin Sword is self-condensed (Ch. 1625); all six complete at Ch. 1715.",
                List.of("6 Origin Swords (Fire, Life-Death, Karma, True-False, Slaughter, Restriction)",
                        "Self-condensed from void-gate vortex power",
                        "Each embodies one Origin",
                        "Slaughter Origin Sword originally Qing Shui's (Ch. 1561)",
                        "Thunder Origin Sword self-condensed (Ch. 1625)",
                        "All six complete (Ch. 1715)"),
                List.of("knowledge", "origin_swords", "six_origin_swords", "void_gate", "wang_lin_created", "wang_lin_unique")
        ));
    }
}
