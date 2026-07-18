package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalAllies — Wang Lin's mentors, friends, and life-companions.
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.ai.WangLinRelationships} (the 17
 * canonical relationships). The entries here surface the canon-demonstrated
 * behavior of each ally and how they relate to Wang Lin's teaching gates.
 */
public final class CanonicalAllies extends AbstractSubRegistry {

    public CanonicalAllies() {
        super("CanonicalAllies");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "ally_li_muwan", "Li Muwan (Wife)", "李慕婉",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Heng Yue Sect era → end of novel"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's wife; his entire late-game motivation."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Wang Lin's wife. Met Wang Lin escaping a Fire Beast; drained life force to refine Azure Dragon Jade Slip for him. Failed Nascent Soul formation multiple times; died at 500 years old. Soul preserved in Heaven Defying Bead 700 years; resurrected by Wang Lin at 4th Step; transcends with him.",
                List.of("Drained life force to refine Azure Dragon Jade Slip for Wang Lin",
                        "Failed Nascent Soul formation",
                        "Soul preserved in Heaven-Defying Bead 700 years",
                        "Resurrected at 4th Step",
                        "Transcends with Wang Lin"),
                List.of("ally", "spouse", "li_muwan", "wang_lin_unique", "azure_dragon_jade_slip")
        ));

        register(CanonicalEntry.of(
                "ally_situ_nan", "Situ Nan (Mentor / Bead Spirit)", "司徒南",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 8+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Originally the Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird. Fled into the Heaven-Defying Pearl."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Originally the Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird. Fled into the Heaven Defying Pearl; met Wang Lin and became his first mentor. Sacrificed his remaining power to save Wang Lin; reincarnated on IAC as 'Si Nan' Grand Marshal of Wu Xuan Country. Transmitted: Underworld Ascension Method, Vermilion Bird Burning Heaven Art, Finger of Death / Demonic / Underworld trio, Yellow Springs Finger, Samsara Eye, Heavenly Eye.",
                List.of("Bead-spirit mentor",
                        "Sacrificed remaining power to save Wang Lin",
                        "Reincarnated on IAC as 'Si Nan'",
                        "Transmitted the Underworld Ascension Method + Vermilion Bird Burning Heaven Art"),
                List.of("ally", "mentor", "situ_nan", "vermilion_bird", "heaven_defying_bead", "contract")
        ));

        register(CanonicalEntry.of(
                "ally_tu_si", "Tu Si (Ancient God Inheritance)", "涂司",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190+"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Ancient God 8-Star. Granted Wang Lin the 'knowledge' inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient God 8-Star. His body became the Land of the Ancient God (3-level Chaotic Broken Stars realm). Granted Wang Lin the 'Great Enlightened One' title and 'knowledge' inheritance. Tuo Sen inherited his 'power' inheritance (born from Tu Si's failed Ink Flow Split Soul Technique). Gave Wang Lin: Ancient God Tactic, Heaven Technique (movement-inside-body), Restriction Flags Refining Method, Ancient God Leather Armour, Azure Ancient God Shield, Ancient God Trident, Ancient God Furnace, Ancient God Bracer, God-Slaying Spear (illusory), Protection Bone Tablets.",
                List.of("Ancient God 8-Star",
                        "Body became the Land of the Ancient God",
                        "Granted 'Great Enlightened One' title",
                        "Source of most Ancient God treasures"),
                List.of("ally", "mentor", "tu_si", "ancient_god", "inheritance", "great_enlightened_one")
        ));

        register(CanonicalEntry.of(
                "ally_bai_fan", "Bai Fan (Six Paths Triple Arts Inheritor)", "白凡",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Thunder Immortal World era"), 4),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Immortal Emperor (Third Step+); bequeathed the six spells and the Bead's recognition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin inherited Bai Fan's Mountain Crumble spell and Six Paths Triple Techniques. Found Bai Fan's Collection Pavilion in the Thunder Immortal World. Long dead; inheritance passed to Wang Lin.",
                List.of("Immortal Emperor (Third Step+)",
                        "Inherited Mountain Crumble spell",
                        "Inherited Six Paths Triple Techniques",
                        "Found Bai Fan's Collection Pavilion"),
                List.of("ally", "mentor", "bai_fan", "thunder_immortal_world", "inheritance", "six_paths")
        ));

        register(CanonicalEntry.of(
                "ally_qing_shui", "Qing Shui (Sworn Brother)", "清水",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Wang Lin's sworn brother; the Slaughter Dao inheritor."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's sworn brother; the Slaughter Dao inheritor. Used the Ji Realm to kill Russell for offending Wang Lin. Gave Wang Lin the Nether Guide spell. His Slaughter Sword was later given to Wang Lin — fused with the Slaughter Crystal to complete Wang Lin's Slaughter Essence.",
                List.of("Sworn brother",
                        "Used Ji Realm to kill Russell",
                        "Taught Wang Lin the Nether Guide spell",
                        "His Slaughter Sword completed Wang Lin's Slaughter Essence"),
                List.of("ally", "sworn_brother", "qing_shui", "slaughter_dao", "nether_guide", "ji_realm")
        ));

        register(CanonicalEntry.of(
                "ally_dun_tian", "Dun Tian (Soul Refining Sect Ancestor)", "顿天",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Pilu Kingdom era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Soul Refining Sect ancestor. Gave Wang Lin the Ten Billion Soul Banner and sect inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Refining Sect ancestor; gave Wang Lin the Ten Billion Soul Banner and sect inheritance. Helped Wang Lin's clone reach Nascent Soul peak and true body reach 3-Star Ancient God. Erased his own consciousness to become a soul within the Soul Banner.",
                List.of("Soul Refining Sect ancestor",
                        "Gave Wang Lin the Ten Billion Soul Banner",
                        "Helped Wang Lin's clone reach Nascent Soul peak",
                        "Erased his own consciousness to become a soul in the Banner"),
                List.of("ally", "mentor", "dun_tian", "soul_refining_sect", "billion_soul_flag", "sacrifice")
        ));

        register(CanonicalEntry.of(
                "ally_zhou_yi", "Zhou Yi", "周毅",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Gifted Wang Lin the Celestial Sword → Rain Celestial Sword lineage (Ch. 717)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Gifted Wang Lin the Celestial Sword (Ch. 717) under the condition Wang Lin protect the celestial corpse in the pagoda. The sword became the seed of the Rain Celestial Sword inheritance.",
                List.of("Gifted the Celestial Sword (Ch. 717)",
                        "Conditional gift (protect celestial corpse)",
                        "Seed of the Rain Celestial Sword inheritance"),
                List.of("ally", "zhou_yi", "celestial_sword", "rain_celestial_sword", "conditional_gift")
        ));

        register(CanonicalEntry.of(
                "ally_dao_master_blue_dream", "Dao Master Blue Dreams", "蓝梦道主",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Blue Silk Clan era"), 4),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Li Qianmei's father; taught Wang Lin Light Shadow Shield and Dao Art Fusion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Li Qianmei's father; taught Wang Lin Light Shadow Shield and Dao Art Fusion. Healed Li Qianmei at the cost of her memories. Wang Lin injured his palm at one point.",
                List.of("Li Qianmei's father",
                        "Taught Light Shadow Shield + Dao Fusion",
                        "Healed Li Qianmei at cost of her memories"),
                List.of("ally", "mentor", "blue_dream", "li_qianmei", "light_shadow_shield", "dao_fusion")
        ));

        register(CanonicalEntry.of(
                "ally_lian_daofei", "Lian Daofei (Madman)", "炼道疯",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Nether Beast era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.contractedInheritance("Brother of Immortal Emperor Lian Daozhen. Transmitted Indestructible Immortal Body + numerous divine abilities."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Madman, brother of Immortal Emperor Lian Daozhen. Met Wang Lin inside the Nether Beast. Fused his supreme Immortal bloodline into Wang Lin and imparted the Indestructible Immortal Body art. After Wang Lin's bloodline accepted it, Lian Daofei imparted numerous divine abilities alongside. Also used as guide for the 4th Heaven Trampling Bridge crossing.",
                List.of("Brother of Immortal Emperor Lian Daozhen",
                        "Fused supreme Immortal bloodline into Wang Lin",
                        "Transmitted Indestructible Immortal Body",
                        "Used as guide for 4th Heaven Trampling Bridge"),
                List.of("ally", "mentor", "lian_daofei", "indestructible_immortal_body", "nether_beast", "lian_daozhen")
        ));

        register(CanonicalEntry.of(
                "ally_li_qianmei", "Li Qianmei (Second Wife)", "李千媚",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Origin Sect era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's second wife; Dao Master Blue Dream's daughter."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Daughter of Dao Master Blue Dream; Wang Lin's second wife. Smeared blood on Wang Lin's stone-petrified body for 10 years to save him from Daoist Water. Healed by her father at the cost of losing most memories of Wang Lin.",
                List.of("Smeared blood on Wang Lin's petrified body 10 years",
                        "Saved Wang Lin from Daoist Water",
                        "Lost most memories of Wang Lin (healing cost)"),
                List.of("ally", "spouse", "li_qianmei", "blue_dream", "daoist_water", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "ally_mu_bingmei", "Mu Bingmei / Liu Mei (Third Wife)", "慕冰媚 / 柳眉",
                CanonicalCategory.ALLIES,
                Provenance.explicit("Renegade Immortal", List.of("Planet Suzaku era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's third wife; Wang Ping's mother."),
                Demonstrability.CAN_EXPLAIN_BUT_NOT_DEMONSTRATE,
                "Liu Mei's true form; Wang Lin's third wife. Had a son with Wang Lin (Wang Ping) whom she refined into a resentful spirit out of hatred. Wang Lin severed karmic ties with her via the Dream Dao; one of his clones accompanies her.",
                List.of("Wang Ping's mother",
                        "Refined Wang Ping into a resentful spirit",
                        "Wang Lin severed karmic ties via Dream Dao"),
                List.of("ally", "spouse", "mu_bingmei", "wang_ping", "dream_dao", "karma_severance")
        ));
    }
}
