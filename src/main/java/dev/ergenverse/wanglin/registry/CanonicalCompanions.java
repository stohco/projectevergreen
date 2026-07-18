package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalCompanions — Wang Lin's contracted companions (devils, life-bound).
 *
 * <p>Distinct from {@link CanonicalPets} (tamed beasts). Companions are
 * humanoids / devils / contracted entities — Xu Liguo (devil head sword-spirit),
 * the souls-tornado leader devil, etc.
 */
public final class CanonicalCompanions extends AbstractSubRegistry {

    public CanonicalCompanions() {
        super("CanonicalCompanions");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "companion_xu_liguo", "Xu Liguo (1st Devil → Rain Celestial Sword Spirit)", "徐立果",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~580"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Refined/captured 1st devil (Ch. ~580). Later became the sword-spirit of the Rain Celestial Sword."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "1st devil Wang Lin refined/captured (Ch. ~580). Extremely cunning and gifted in possession. Later became the sword-spirit of Wang Lin's Rain Celestial Sword (sword-spirit named Jufu / 巨斧).",
                List.of("1st devil refined/captured (Ch. ~580)",
                        "Cunning and gifted in possession",
                        "Became the sword-spirit of the Rain Celestial Sword (Jufu / 巨斧)"),
                List.of("companion", "devil", "xu_liguo", "rain_celestial_sword", "sword_spirit", "possession")
        ));

        register(CanonicalEntry.of(
                "companion_souls_tornado_leader", "Souls Tornado Leader (2nd Devil)", "魂龙首领",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~590"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("2nd devil captured."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "2nd devil Wang Lin refined/captured (Ch. ~590) — the leader of a souls tornado. Devil-tier companion. Cunning and gifted in possession.",
                List.of("2nd devil captured",
                        "Leader of a souls tornado",
                        "Cunning and gifted in possession"),
                List.of("companion", "devil", "souls_tornado", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "companion_devil_sky_cloud_monkey", "Devil Sky Cloud Monkey (3rd Devil)", "魔天云猴",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~620"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("3rd devil captured at Sky Cloud Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "3rd devil Wang Lin refined/captured (Ch. ~620). Sky Cloud Sect monkey devil-tier companion.",
                List.of("3rd devil captured at Sky Cloud Sect",
                        "Devil-tier companion",
                        "Cunning and gifted in possession"),
                List.of("companion", "devil", "monkey", "sky_cloud_sect", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "companion_thirteen", "Thirteen (Disciple)", "十三",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Wang Lin's disciple."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's disciple Thirteen — a contracted humanoid companion.",
                List.of("Wang Lin's disciple",
                        "Contracted humanoid companion"),
                List.of("companion", "disciple", "thirteen", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "companion_xie_qing", "Xie Qing (Disciple)", "谢青",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Wang Lin's disciple."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's disciple Xie Qing — a contracted humanoid companion.",
                List.of("Wang Lin's disciple",
                        "Contracted humanoid companion"),
                List.of("companion", "disciple", "xie_qing", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "companion_xi_zifeng", "Xi Zifeng (Disciple)", "溪紫凤",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~871"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Wang Lin's disciple. Gifted Wang Lin the Jade (Thunder Defense) (Ch. 871)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's disciple Xi Zifeng. Gifted Wang Lin the Jade (Thunder Defense) (Ch. 871) — an important self-defense treasure she possessed.",
                List.of("Wang Lin's disciple",
                        "Gifted Wang Lin the Jade (Thunder Defense)"),
                List.of("companion", "disciple", "xi_zifeng", "jade_thunder_defense")
        ));

        register(CanonicalEntry.of(
                "companion_lian_daofei", "Lian Daofei (Madman)", "炼道疯",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Nether Beast era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Met Wang Lin inside the Nether Beast. Transmitted Indestructible Immortal Body."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Madman, brother of Immortal Emperor Lian Daozhen. Met Wang Lin inside the Nether Beast. Fused his supreme Immortal bloodline into Wang Lin and imparted the Indestructible Immortal Body art + numerous divine abilities. Later used as guide for the 4th Heaven Trampling Bridge crossing.",
                List.of("Brother of Immortal Emperor Lian Daozhen",
                        "Met Wang Lin inside the Nether Beast",
                        "Fused supreme Immortal bloodline into Wang Lin",
                        "Used as guide for 4th Heaven Trampling Bridge"),
                List.of("companion", "madman", "lian_daofei", "indestructible_immortal_body", "nether_beast", "lian_daozhen")
        ));

        register(CanonicalEntry.withSpec(
                "companion_ling_dong", "Ling Dong (Ancient Slave)", "灵东·古奴",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1450"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Half-step Third-Step Esteemed Ling Dong refined into ancient slave using Emperor Furnace — bound to Wang Lin."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Half-step Third-Step cultivator Esteemed Ling Dong was refined into an ancient slave using the Emperor Furnace. Functions as a contracted puppet-companion.",
                List.of("Half-step Third-Step refined into ancient slave",
                        "Refined using Emperor Furnace",
                        "Functions as puppet-companion"),
                List.of("companion", "ancient_slave", "ling_dong", "emperor_furnace", "wang_lin_owned"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.withSpec(
                "companion_silver_poison_female_corpse", "Silver Poison Female Corpse", "银毒女尸",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 930"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Got during Alliance–Allheaven war. Originally the maid of the Seven-Coloured Celestial Sovereign."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Got during the war between the Alliance and Allheaven. Strength at the early stage of Nirvana Cleanser. Later revealed she was the maid of the Seven-Coloured Celestial Sovereign and she still has her memories. She can think and do stuff on her own.",
                List.of("Early-stage Nirvana Cleanser strength",
                        "Originally maid of the Seven-Coloured Celestial Sovereign",
                        "Memories intact; autonomous"),
                List.of("companion", "corpse", "silver_poison", "seven_colored", "autonomous"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.withSpec(
                "companion_yi_si_puppet", "Yi Si Puppet", "忆思傀儡",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1774"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Got from the Ancient Tomb's second floor."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Got from the Ancient Tomb's second floor. Ancient-tier puppet-companion.",
                List.of("From Ancient Tomb (2nd floor)",
                        "Ancient-tier puppet"),
                List.of("companion", "puppet", "yi_si", "ancient_tomb", "wang_lin_owned"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.withSpec(
                "companion_celestial_guard_copper_du_jian", "Celestial Guard — Copper Rank (Du Jian)", "铜阶仙卫·杜坚",
                CanonicalCategory.PUPPETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 653", "Ch. 762 (exploded)"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Refined from Du Jian's body. Method from Huang Yu."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Puppets that copy the Ancient Gods and fight only with their body and some spells specially created for them. Wang Lin added a heavenly ghost, making it comparable to an Illusionary-Yin cultivator. Exploded Ch. 762.",
                List.of("Copper Rank (Illusionary-Yin strength)",
                        "Heavenly-ghost-augmented",
                        "Copies Ancient Gods",
                        "Exploded Ch. 762"),
                List.of("companion", "puppet", "celestial_guard", "du_jian", "huang_yu", "copper_rank"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.withSpec(
                "companion_celestial_guard_silver_thunder_daoist", "Celestial Guard — Silver Rank (Thunder Daoist)", "银阶仙卫·雷道人",
                CanonicalCategory.PUPPETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 707", "Ch. 717 (shattered)"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Refined Thunder Daoist (real messenger of the Thunder Celestial Temple)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin refined Thunder Daoist (a real messenger of the Thunder Celestial Temple) into a celestial guard. Shattered Ch. 717 (Thunder Prison).",
                List.of("Silver Rank",
                        "Source: Thunder Celestial Temple messenger",
                        "Thunder-element",
                        "Shattered Ch. 717"),
                List.of("companion", "puppet", "celestial_guard", "thunder_daoist", "thunder_celestial_temple", "silver_rank"),
                "wang_lin_puppets"
        ));

        register(CanonicalEntry.withSpec(
                "companion_celestial_guard_silver_ta_shan", "Celestial Guard — Silver Rank (Ta Shan)", "银阶仙卫·塔山",
                CanonicalCategory.PUPPETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 815", "Ch. 1025 (freed)"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Refined from dying Ta Shan (with Ta Shan's consent)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ta Shan, a member of the Chosen Immortal Clan, was dying. Wang Lin proposed refining him into a celestial guard in hopes of him surviving. Freed Ch. 1025 — the celestial-guard seal was forcefully removed by the Tattoo Clan's Ancestor.",
                List.of("Silver Rank",
                        "Source: Chosen Immortal Clan (Ta Shan, with consent)",
                        "Freed Ch. 1025 (seal removed by Tattoo Clan Ancestor)"),
                List.of("companion", "puppet", "celestial_guard", "ta_shan", "chosen_immortal_clan", "silver_rank"),
                "wang_lin_puppets"
        ));

        // ── Additional puppets / companions (per canon database I114–I115) ──
        register(CanonicalEntry.of(
                "companion_zhou_jin", "Zhou Jin (captured then freed)", "周谨",
                CanonicalCategory.PUPPETS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1470 (captured)"), 5,
                        "Cross-refs RICanonicalDatabase I115."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Temporary puppet — captured and held (Ch. 1470). Freed when Wang Lin was injured."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Zhou Jin. Temporary puppet — captured and held by Wang Lin (Ch. 1470). Freed when Wang Lin was injured.",
                List.of("Temporary puppet",
                        "Captured and held (Ch. 1470)",
                        "Freed when Wang Lin was injured"),
                List.of("companion", "puppet", "zhou_jin", "temporary")
        ));

        register(CanonicalEntry.of(
                "companion_xu_liguo_first", "Xu Liguo (1st Devil, alternative name)", "徐立果",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~580"), 5,
                        "Cross-refs RICanonicalDatabase I123. Alias entry to ensure manifest-id searchability."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Refined/captured 1st devil (Ch. ~580)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Alias entry for Xu Liguo (companion_xu_liguo) to ensure the manifest id 'devil_xu_liguo_first' is searchable. Same entity as companion_xu_liguo.",
                List.of("Alias for companion_xu_liguo",
                        "1st devil refined/captured (Ch. ~580)",
                        "Same entity — alternative id"),
                List.of("companion", "devil", "xu_liguo", "alias", "manifest_id")
        ));

        register(CanonicalEntry.of(
                "companion_devil_soul_tornado_leader", "Devil Soul Tornado Leader (2nd Devil, alternative name)", "魂龙首领",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~590"), 5,
                        "Cross-refs RICanonicalDatabase I123."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("2nd devil captured."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Alias entry for the Souls Tornado Leader devil to ensure manifest id 'devil_soul_tornado_leader' is searchable. Same entity as companion_souls_tornado_leader.",
                List.of("Alias for companion_souls_tornado_leader",
                        "2nd devil captured (Ch. ~590)",
                        "Same entity — alternative id"),
                List.of("companion", "devil", "souls_tornado", "alias", "manifest_id")
        ));

        register(CanonicalEntry.of(
                "companion_devil_sky_cloud_monkey_v2", "Devil Sky Cloud Monkey (3rd Devil, alternative name)", "魔天云猴",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~620"), 5,
                        "Cross-refs RICanonicalDatabase I123."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("3rd devil captured at Sky Cloud Sect."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Alias entry for the Devil Sky Cloud Monkey to ensure manifest id 'devil_sky_cloud_monkey' is searchable. Same entity as companion_devil_sky_cloud_monkey.",
                List.of("Alias for companion_devil_sky_cloud_monkey",
                        "3rd devil captured at Sky Cloud Sect (Ch. ~620)",
                        "Same entity — alternative id"),
                List.of("companion", "devil", "monkey", "sky_cloud_sect", "alias", "manifest_id")
        ));

        // ── Wang Lin's Puppets (wang_lin_puppets spec group) ──
        register(CanonicalEntry.of(
                "puppet_du_jian", "Du Jian (Puppet)", "杜建（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of Wang Lin's refined corpse puppets. A combat puppet with significant fighting power.",
                List.of("Combat puppet", "Refined corpse", "Soul Refining Sect technique"),
                List.of("puppet", "companion", "combat", "refining", "soul")
        ));
        register(CanonicalEntry.of(
                "puppet_thunder_daoist", "Thunder Daoist (Puppet)", "雷修（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A puppet refined from a Thunder-attributed cultivator. Retains thunder-based combat abilities.",
                List.of("Thunder-attribute combat", "Retained original abilities"),
                List.of("puppet", "companion", "thunder", "combat")
        ));
        register(CanonicalEntry.of(
                "puppet_ta_shan", "Ta Shan (Puppet)", "泰山（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A powerful puppet refined by Wang Lin. Known for physical strength.",
                List.of("Physical combat specialty", "High durability"),
                List.of("puppet", "companion", "physical", "strength")
        ));
        register(CanonicalEntry.of(
                "puppet_silver_poison_female_corpse", "Silver Poison Female Corpse (Puppet)", "银毒女尸（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A female corpse puppet with silver poison abilities. Used for assassinations and poison-based combat.",
                List.of("Poison-based combat", "Assassination capability", "Female corpse refinement"),
                List.of("puppet", "companion", "poison", "silver", "assassination")
        ));
        register(CanonicalEntry.of(
                "puppet_yi_si", "Yi Si (Puppet)", "易思（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A refined puppet. One of Wang Lin's loyal guard corps.",
                List.of("Guard duty", "Loyal to Wang Lin"),
                List.of("puppet", "companion", "guard")
        ));
        register(CanonicalEntry.of(
                "puppet_ling_dong", "Ling Dong (Puppet)", "凌动（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A refined puppet. One of Wang Lin's guard corps members.",
                List.of("Guard duty", "Combat support"),
                List.of("puppet", "companion", "guard", "combat")
        ));
        register(CanonicalEntry.of(
                "puppet_zhou_jin", "Zhou Jin (Puppet)", "周金（傀儡）",
                CanonicalCategory.COMPANIONS,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Refined by Wang Lin via soul refining."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A refined puppet. One of Wang Lin's guard corps members.",
                List.of("Guard duty", "Combat support"),
                List.of("puppet", "companion", "guard", "combat")
        ));
    }
}
