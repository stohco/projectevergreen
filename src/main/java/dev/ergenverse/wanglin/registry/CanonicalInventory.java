package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalInventory — Wang Lin's owned items (signature subset; cross-refs RICanonicalDatabase).
 *
 * <p>Each entry cites its source from CANON_RI_COMPLETE_ITEMS.md. The 175-item
 * wiki items catalog and the 178-artifact RICanonicalDatabase together
 * contain every Wang Lin–owned item; this registry surfaces the signature
 * subset the user named in the task brief: Restriction Flag, Heaven-Defying
 * Bead, Karma Whip, 18-Hell Celestial Sealing Stamp, Sword Sheaths ×5,
 * Storage treasures, Ancient God Leather Armour, God-Slaying War Chariot, etc.
 *
 * <p>The full 309-item arsenal (per the wanglin_arsenal_manifest.json) is
 * registered as Minecraft items via {@code WangLinItems}; this registry is
 * the canon-metadata layer that those items pull from.
 */
public final class CanonicalInventory extends AbstractSubRegistry {

    public CanonicalInventory() {
        super("CanonicalInventory");
    }

    @Override
    protected void doBootstrap() {
        // ── Core treasures ────────────────────────────────────────────
        register(CanonicalEntry.withSpec(
                "heaven_defying_bead", "Heaven-Defying Bead", "逆天珠",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 8"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("RI Ch. 8 acquisition; wiki items table; cross-novel artifact (Su Ming/Xuan Zang). The bead's mysteries are bound to the bead's existence — Wang Lin can share principles, not the bead itself."),
                Demonstrability.UNIQUELY_BOUND,
                "Bead with the Five Elements pattern. Originally 9 parts; the bead is its core. Interior door leads to a chamber where time runs 10× outside. Stores Li Muwan's Nascent Soul. Reputed to contain Third-Step divine abilities inside. Sentient / destiny-bound / fused with primordial spirit at Heaven Trampling.",
                List.of(
                        "Recognizes master after Lu Mo (clone) blasts it open via Dream Dao",
                        "Once Five Elements perfected, the master truly owns it",
                        "Stores Li Muwan's Nascent Soul after her body perishes",
                        "Time dilation: 1 hour inside = 10 hours outside",
                        "Heaven-defying: allows ONE tier above the world's cultivation ceiling",
                        "Fuses with Wang Lin's primordial spirit at Heaven Trampling"
                ),
                List.of("core", "five_elements", "time", "space", "samsara", "li_muwan", "lu_mo", "heaven_defying"),
                "heaven_defying_bead"
        ));

        register(CanonicalEntry.withSpec(
                "restriction_flag_1st", "Restriction Flag (1st)", "禁旗第一面",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent-Soul era", "Restriction Mountain Trial"), 5,
                        "Wiki items table; CANON_RI_COMPLETE_TECHNIQUES.md §Restriction Flags Refining Method"),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Self-forged after Tu Si gave the refining method (Ch. ~180). Three flags made; each requires 3 Ink Stones + 99,999 restrictions. Forgeable = duplicable in principle."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Made from inkstones with 99,999 restrictions to complete. 1st flag intentionally left incomplete so Wang Lin could summon divine tribulation in danger. Self-forged (Tu Si gave the refining method after Wang Lin became the 4th person to complete the Land of the Ancient God Restrictions Mountain trial).",
                List.of(
                        "Summons divine tribulation even in incomplete state",
                        "Carries a portable formation-array",
                        "Each flag is a portable restriction formation"
                ),
                List.of("restriction", "formation", "tu_si", "ancient_god", "inkstone", "divine_tribulation"),
                "restriction_flag"
        ));

        register(CanonicalEntry.withSpec(
                "restriction_flag_2nd", "Restriction Flag (2nd, mixed)", "禁旗第二面",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent-Soul era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Self-forged; same method as 1st flag."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mixture of different restrictions. Second of Wang Lin's three Restriction Flags.",
                List.of("Mixed restriction set",
                        "Functions as portable formation-array"),
                List.of("restriction", "formation", "tu_si", "ancient_god"),
                "restriction_flag"
        ));

        register(CanonicalEntry.withSpec(
                "restriction_flag_3rd", "Restriction Flag (3rd, pure attack)", "禁旗第三面",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent-Soul era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfForged("Self-forged; pure attack restriction flag."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Pure attack restriction flag. Third of Wang Lin's three Restriction Flags.",
                List.of("Pure attack restriction set",
                        "Functions as portable formation-array"),
                List.of("restriction", "formation", "attack", "tu_si", "ancient_god"),
                "restriction_flag"
        ));

        register(CanonicalEntry.withSpec(
                "karma_whip", "Karma Whip", "因果鞭",
                CanonicalCategory.FLYING_SWORDS,  // weapons category; the user enumerated "flying swords" — whips are melee weapons
                Provenance.explicit("Renegade Immortal", List.of("Ch. 731"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Fused from Soul Lasher + Karma Domain (Ch. 731). Wang Lin can recreate the fusion given the inputs but cannot duplicate the original whip."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Created when Wang Lin fused the Soul Lasher (originally Red Butterfly's whip — attacks origin soul at warp speed) with his Karma Domain. Weaponizes karmic cause-effect. In the Outer Realm, Wang Lin once used this to cleave open 7 million worlds with a single whip-strike.",
                List.of("Strikes at the origin soul at warp speed",
                        "Weaponizes karmic cause-effect",
                        "Cleaved open 7 million worlds with a single whip-strike (Outer Realm)"
                ),
                List.of("karma", "weapon", "whip", "soul", "dao_domain", "fusion", "wang_lin_created"),
                "karma_whip"
        ));

        register(CanonicalEntry.of(
                "celestial_sealing_stamp_18_hell", "18-Hell Celestial Sealing Stamp", "十八地狱封天印",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 915", "Ch. 769 (Fragment Stamp)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-forged from Fragment Stamp via divine tribulation (Ch. 915). Composite of Magic-Arsenal Spell + Celestial Sealing Stamp."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Forms the 18-Layers-of-Hell-Reincarnation-Realm with Underworld River (Life-Death Dao). Originally a fragment refined by divine tribulation when Wang Lin broke into Illusionary-Yin/Corporeal-Yang. Stores all souls of enemies Wang Lin has killed. Refined into its own pocket-realm (Incense Offering Realm per Baidu).",
                List.of("Forms a pocket-realm (18 Layers of Hell Reincarnation Realm)",
                        "Stores all souls of slain enemies",
                        "Anchors the Scatter Beans to Form Soldiers divine ability"
                ),
                List.of("soul", "reincarnation", "life_death", "underworld_river", "magic_arsenal", "celestial", "stamp", "wang_lin_created")
        ));

        register(CanonicalEntry.withSpec(
                "god_slaying_sword", "God-Slaying Sword", "杀神剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Soul Transformation era", "Ch. 1273 (destroyed)"), 5),
                OwnershipState.FORMERLY_OWNED,
                new Transferability(
                        Transferability.CanTeach.WILL_TEACH,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.NO,
                        Transferability.CanGiftEquivalent.YES_DERIVED_EQUIVALENT,
                        Transferability.CanCreateNew.UNKNOWN,
                        "RI Soul Transformation era acquisition; destroyed Ch. 1273 (first Daoist Water fight) then restored later."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's primary celestial-tier sword. Destroyed in the first Daoist Water fight (Ch. 1273), restored later. The original was lost and re-forged; the restoration was a separate event.",
                List.of("Primary sword during Soul Transformation era",
                        "Destroyed by Daoist Water and restored"),
                List.of("sword", "celestial", "weapon", "daoist_water"),
                "wang_lin_flying_swords"
        ));

        register(CanonicalEntry.withSpec(
                "rain_celestial_sword", "Rain Celestial Sword (Mid Quality)", "雨仙剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by Zhou Yi (Ch. 717) under condition Wang Lin protect the celestial corpse in the pagoda. Sword-spirit later passed to Xu Liguo (devil head)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A sword Zhou Yi separated out from the celestial corpse swords so Wang Lin could inherit it for eternity. The sword-spirit (Jufu / 巨斧) was later passed to Xu Liguo (devil head) to wield. Contains the Slash Luo Art sword-technique.",
                List.of("Inherited conditionally from Zhou Yi",
                        "Sword-spirit bound to Xu Liguo",
                        "Contains the Slash Luo Art"),
                List.of("sword", "celestial", "zhou_yi", "xu_liguo", "slash_luo", "inheritance"),
                "wang_lin_flying_swords"
        ));

        register(CanonicalEntry.of(
                "celestial_sword_lineage", "Celestial Sword (→ Rain Celestial Sword lineage)", "仙剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Conditional gift from Zhou Yi (Ch. 717). Evolved into Rain Celestial Sword lineage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Zhou Yi gave one to Wang Lin under the condition Wang Lin keeps protecting the celestial corpse inside the pagoda. Became the seed of the Rain Celestial Sword inheritance.",
                List.of("Seed of the Rain Celestial Sword inheritance",
                        "Conditional gift"),
                List.of("sword", "celestial", "zhou_yi", "inheritance", "conditional_gift")
        ));

        register(CanonicalEntry.of(
                "core_treasure_sword", "Core-Treasure Sword (Teleportation)", "核心宝剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid-Foundation era"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfForged("Blood-refined by Wang Lin. Evolves into Dark Green Flying Sword (poison-attribute)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Has a teleportation effect. Blood-refined by Wang Lin; took countless lives for him. Eventually refined further into Dark Green Flying Sword (Poison).",
                List.of("Teleportation effect on strike",
                        "Blood-refined",
                        "Evolved into Dark Green Flying Sword"),
                List.of("sword", "blood_refined", "teleportation", "wang_lin_created")
        ));

        register(CanonicalEntry.withSpec(
                "dark_green_flying_sword", "Dark Green Flying Sword (Poison)", "墨绿飞剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("post-Core-Treasure"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfForged("Blood-refinement evolution of Core-Treasure Sword. Retired (replaced by later swords)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Successor to Core-Treasure Sword; carries poison attribute. Blood-refined and battle-hardened.",
                List.of("Carries poison attribute",
                        "Blood-refined and battle-hardened",
                        "Retired (replaced by later swords)"),
                List.of("sword", "poison", "blood_refined", "wang_lin_created"),
                "wang_lin_flying_swords"
        ));

        register(CanonicalEntry.withSpec(
                "wealth_flying_sword", "Wealth (First Flying Sword)", "财富",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Heng Yue Sect era", "Ch. ~110 (destroyed)"), 5),
                OwnershipState.FORMERLY_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.ATTEST_ONLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "RI Heng Yue Sect era. Soul-binding contract with sect: 'if it breaks, repair immediately; if sold, exiled from sect'. Destroyed by Teng Huayuan."
                ),
                Demonstrability.ATTEST_ONLY,
                "Wang Lin's first flying sword. Made by a Heng Yue Sect elder; sect rule: 'whoever chooses this sword must treat it well — if it breaks, repair immediately, if sold, exiled from sect.' Revealed later to have a much more mysterious origin.",
                List.of("Soul-binding contract with the Heng Yue Sect",
                        "Broken/reforged many times",
                        "Destroyed by Teng Huayuan (~Ch. 110 era)"),
                List.of("sword", "heng_yue_sect", "teng_huayuan", "first_sword", "soul_bound"),
                "wang_lin_flying_swords"
        ));

        register(CanonicalEntry.of(
                "god_slaying_spear_illusory", "God Slaying Spear (Illusory)", "杀神矛",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 941"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited from Ancient God Tu Si's legacy (Ch. 941). Illusory copy of Tu Si's life-treasure."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tu Si's life treasure; illusion-projected from inheritance. Functions as the Ancient God's Weapon — bypasses physical defenses by striking at the origin soul.",
                List.of("Bypasses physical defenses",
                        "Strikes at the origin soul",
                        "Illusion-projected from Tu Si's life-treasure"),
                List.of("spear", "ancient_god", "tu_si", "inheritance", "soul")
        ));

        register(CanonicalEntry.of(
                "ancient_god_trident", "Ancient God Trident", "古神三叉戟",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1082", "Ch. 1277 (destroyed)"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Tu Si-refined; inherited. Destroyed by Daoist Water (Ch. 1277 Rebirth)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Weapon Tu Si was refining; planned to finish after the spell that led to his death. Can absorb spells cast at the wielder.",
                List.of("Absorbs spells cast at the wielder",
                        "Tu Si-refined",
                        "Destroyed by Daoist Water"),
                List.of("trident", "ancient_god", "tu_si", "spell_absorb", "inheritance")
        ));

        register(CanonicalEntry.withSpec(
                "ancient_god_leather_armour", "Ancient God Leather Armour", "古神皮甲",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 758"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient God inheritance (Ch. 758). Made from the skin of an 8-star Ancient God — cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Origin-Soul-defensive treasure made from the skin of an 8-star Ancient God.",
                List.of("Origin-Soul defensive treasure",
                        "Made from 8-star Ancient God skin"),
                List.of("armor", "ancient_god", "tu_si", "origin_soul_defense", "inheritance"),
                "devil_armor"
        ));

        register(CanonicalEntry.withSpec(
                "scattered_devil_armour", "Scattered Devil Armour / Divine Devil Armour", "散魔甲",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 610", "Ch. 1178 (sold)"), 5),
                OwnershipState.FORMERLY_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.NO,
                        Transferability.CanGiftEquivalent.NO,
                        Transferability.CanCreateNew.NO,
                        "RI Ch. 610 acquisition (took from a Scattered Devil in the Demon Spirit Land, with help of Ancient Demon Bei Lou); sold Ch. 1178."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Pitch-black armor with a demonic feel. Took from a Scattered Devil while in the Demon Spirit Land (with help of Ancient Demon Bei Lou). In the Cloud Sea Star System it's the God Sect's 'Divine Devil Armour'.",
                List.of("Pitch-black with demonic aura",
                        "Cloud Sea Star System: God Sect's Divine Devil Armour",
                        "Sold (Ch. 1178)"),
                List.of("armor", "ancient_devil", "bei_lou", "demon_spirit_land", "cloud_sea"),
                "devil_armor"
        ));

        register(CanonicalEntry.of(
                "azure_ancient_god_shield", "Azure Ancient God Shield / Cyan Light Shield", "青光盾",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 980", "Ch. 1082 (shattered)", "Ch. 1626 (restored)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient God inheritance. Shattered (Ch. 1082) by Tian Yunzi; repaired (Ch. 1626) by blasting open the Gate of Emptiness."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Defensive magical instrument of an 8-star Ancient God. Greatest strength: contains the 8-star Ancient God's life-saving divine ability 'Dreaming Back to Antiquity' (梦回太古). Shattered in the battle vs Tian Yunzi in the Immortal Spirit Realm. Repaired when Wang Lin blasted open the Gate of Emptiness. Saved Wang Lin's life again vs the Seven-Colored Daoist.",
                List.of("Contains 8-star life-saving spell 'Dreaming Back to Antiquity'",
                        "Shatterable AND repairable",
                        "Saved Wang Lin's life vs Seven-Colored Daoist"),
                List.of("shield", "ancient_god", "tu_si", "life_saving", "dreaming_back_to_antiquity", "inheritance")
        ));

        register(CanonicalEntry.of(
                "god_slaying_war_chariot_mid", "God-Slaying War Chariot (Mid Quality)", "杀神战车中品",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1080 (unsealed)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Inherited; unsealed (Ch. ~1080) — released the Silver-Horned Thunder Beast Lei Ji."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mid Quality God-Slaying War Chariot. When unsealed, released the Silver-Horned Thunder Beast (Lei Ji) that fights alongside Wang Lin. Strength comparable to late Soul-Ascension.",
                List.of("Beast-soul-origin: Lei Ji (Silver-Horned Thunder Beast)",
                        "Unsealed ~Ch. 1080"),
                List.of("chariot", "ancient_god", "lei_ji", "thunder_beast", "war_chariot")
        ));

        register(CanonicalEntry.withSpec(
                "collection_pavilion", "Collection Pavilion", "藏经阁",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 784"), 5,
                        "Per Baidu: later gifted by Wang Lin to his friends in the New Immortal World."),
                OwnershipState.FORMERLY_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.YES,
                        Transferability.CanGiftEquivalent.YES_EXACT_COPY,
                        Transferability.CanCreateNew.NO,
                        "RI Ch. 784 took from Thunder Celestial Realm. Master-locked: only Wang Lin can command it. Per Baidu, gifted to friends in New Immortal World."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Took from the Thunder Celestial Realm. Has various celestial spells stored within. Only Wang Lin can command it. Can change size. Per Baidu: later gifted by Wang Lin to his friends in the New Immortal World for their use.",
                List.of("Stores celestial spells",
                        "Size-changing",
                        "Master-locked (only Wang Lin can command)",
                        "Gifted to friends in New Immortal World"),
                List.of("storage", "celestial", "thunder_immortal_world", "spell_storing", "master_locked"),
                "wang_lin_storage_treasures"
        ));

        register(CanonicalEntry.withSpec(
                "space_stone", "Space Stone", "空间石",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1838"), 5,
                        "1 of 3 promised gifts from founder of Great Soul Sect."),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.TESTS_FIRST,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.PARTIAL,
                        Transferability.CanGiftEquivalent.AT_AFFINITY_LEVEL_X,
                        Transferability.CanCreateNew.NO,
                        "Gift from founder of the Great Soul Sect (1 of 3 promised gifts). Quasi-Third-Step treasure. One-use-per-pocket — limited duplication."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "At the cost of one pocket of space in the space stone, you can take out one item from your storage space without damage. Cannot be used more than once per pocket (instability → cluster collapse). Second role: nurture a Heavenly Dao.",
                List.of("One-use-per-pocket",
                        "Nurtures a Heavenly Dao (secondary role)",
                        "Instability → cluster collapse if overused"),
                List.of("storage", "space", "great_soul_sect", "heavenly_dao", "quasi_third_step", "consumable"),
                "wang_lin_storage_treasures"
        ));

        register(CanonicalEntry.withSpec(
                "fate_sealing_ring", "Fate Sealing Ring", "封命环",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1631"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Divine-retribution-forged. Celestial-tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Sealed within the divine retribution. Celestial-tier ring.",
                List.of("Sealed within divine retribution",
                        "Celestial-tier"),
                List.of("ring", "celestial", "divine_retribution", "storage"),
                "wang_lin_storage_treasures"
        ));

        register(CanonicalEntry.of(
                "sword_sheaths_set_of_5", "Sword Sheaths ×5", "剑鞘",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("throughout early-mid era"), 5,
                        "1st was from the new evil master of a former friend at Heng Yue Sect."),
                OwnershipState.FORMERLY_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.ATTEST_ONLY,
                        Transferability.CanExplain.YES_PARTIAL,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.NO,
                        Transferability.CanCreateNew.UNKNOWN,
                        "Suspected Immortal World objects. One is suspected Sub-Void Nirvana Sword. Mysterious origin — Wang Lin attests to function, not creation."
                ),
                Demonstrability.ATTEST_ONLY,
                "Mysterious sword sheaths. Inserting a flying sword enhances different powers. Suspected objects from the Immortal World. One is suspected to be a Sub-Void-Nirvana Sword. Another once appeared in Tuo Sen's possession. 1st was from the new evil master of a former friend at Heng Yue Sect.",
                List.of("Inserting a flying sword enhances different powers",
                        "One is suspected to be a Sub-Void-Nirvana Sword",
                        "Another once appeared in Tuo Sen's possession"),
                List.of("sword", "sheath", "mysterious_origin", "immortal_world", "tuo_sen", "enhancer")
        ));

        register(CanonicalEntry.of(
                "realm_defining_compass", "Realm-Defining Compass", "界定盘",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Lu Mo's borrow"), 4),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("Borrowed by Lu Mo from Old Man Miēshēng. One-time borrow (returned). Quasi-Third-Step."),
                Demonstrability.ATTEST_ONLY,
                "Compass whose core IS the Heaven-Defying Pearl. Used to define and shatter realms; Lu Mo borrowed it to blast open the bead. Has a 'Realm-Sealing' function. Returned to Old Man Miēshēng.",
                List.of("Used to crack open the Heaven-Defying Bead",
                        "Realm-Sealing function",
                        "Returned to Old Man Miēshēng"),
                List.of("compass", "lu_mo", "miēshēng", "realm_sealing", "quasi_third_step", "heaven_defying_bead")
        ));

        register(CanonicalEntry.of(
                "copper_mirror_time_domain", "Copper Mirror (with Time Domain)", "铜镜",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 662", "Ch. 664 (sold)"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("User-made; pseudo-celestial tier. Wang Lin made three pseudo-celestial treasures to sell."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Hand-mirror Wang Lin crafted on Planet Ran Yun; embeds a Time-domain; intended for sale to celestial-spirit-jade merchants. Has the side-effect of showing future/past flickers.",
                List.of("Crafted by Wang Lin on Planet Ran Yun",
                        "Embeds a Time-domain",
                        "Shows future/past flickers (side-effect)",
                        "Sold (Ch. 664 — 'Missed')"),
                List.of("mirror", "time", "celestial", "wang_lin_created", "planet_ran_yun", "pseudo_celestial")
        ));

        register(CanonicalEntry.of(
                "slaughter_crystal", "Slaughter Crystal", "杀戮晶",
                CanonicalCategory.ORIGIN_ENERGY,
                Provenance.explicit("Renegade Immortal", List.of("~Ch. 1290"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Origin essence. Fused with Qing Shui's Slaughter Sword to complete Wang Lin's Slaughter Essence. Cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Condensed Slaughter Origin essence Wang Lin fuses with Qing Shui's Slaughter Sword to complete his own Slaughter Essence. Condensed from slaughters / granted by Immortal Lord Qing Shui via the Sky-Gate vortex.",
                List.of("Condensed from slaughters",
                        "Granted by Immortal Lord Qing Shui via Sky-Gate vortex",
                        "Merged into Slaughter Origin True Body"),
                List.of("slaughter", "essence", "qing_shui", "sky_gate", "origin", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "great_heavenly_venerable_sun", "Great Heavenly Venerable Sun", "大天尊日",
                CanonicalCategory.ORIGIN_ENERGY,
                Provenance.explicit("Renegade Immortal", List.of("post-absorbing Immortal Ancestor head"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Will-condensed; perpetually active; portable power-source. Refining a new one requires an Immortal Ancestor head — unavailable."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A special three-color (black/white/gold) Heavenly Venerable Sun condensed by Wang Lin's will; fuses with quasi-Tread-Heaven power. The energy inside can be absorbed into the body to enhance strength at any time. Strongest of all Great Heavenly Venerable Suns. Refined from the head of the Immortal Ancestor (Celestial Ancestor).",
                List.of("Three-color (black/white/gold)",
                        "Strongest of all Great Heavenly Venerable Suns",
                        "Portable power-source",
                        "Makes Wang Lin the 10th Sun of the IAC"),
                List.of("origin", "celestial_ancestor", "grand_empyrean", "iac", "tenth_sun", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "heart_compass_annihilation", "Heart Compass (Annihilation Restriction Inheritance)", "心罗盘",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 858"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Annihilation Restriction inheritance treasure. Pairs with Destruction Restriction."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Annihilation Restriction inheritance treasure. Pairs with Destruction Restriction to seal all things (immortals, demons, mortal beasts).",
                List.of("Pairs with Destruction Restriction",
                        "Seals immortals / demons / mortal beasts"),
                List.of("restriction", "compass", "annihilation", "inheritance", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "ancient_soul_restriction_tortoise_beast", "Ancient Soul Restriction Tortoise Beast", "古魂禁龟兽",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1426"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by old Vermilion Bird in Fallen Land."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Given by the old Vermilion Bird in the Fallen Land. A living tortoise-beast with ancient-soul-restriction properties.",
                List.of("Living formation-component",
                        "Gifted by old Vermilion Bird in Fallen Land"),
                List.of("beast", "restriction", "vermilion_bird", "fallen_land", "inheritance")
        ));

        register(CanonicalEntry.of(
                "mountain_and_river_screen", "Mountain and River Screen", "山河屏",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 717"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.NO,
                        Transferability.CanGiftEquivalent.NO,
                        Transferability.CanCreateNew.NO,
                        "Stolen from Greed during Moongazer Serpent escape (Ch. 717). Unique celestial-tier defensive treasure."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Stolen from Greed as they escaped from the Moongazer Serpent. Projects an image of mountains and rivers that absorbs attacks.",
                List.of("Projects mountains-and-rivers image",
                        "Absorbs attacks"),
                List.of("shield", "celestial", "greed", "moongazer_serpent", "projection_absorbing")
        ));

        register(CanonicalEntry.of(
                "heaven_avoiding_coffin", "Heaven Avoiding Coffin", "避天棺",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 819"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Acquired to preserve Li Muwan's soul."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Coffin that preserves a dissipating nascent soul — fills with a mysterious force that surrounds the entire coffin; sustains the soul with the trace of life force inside.",
                List.of("Preserves dissipating nascent souls",
                        "Used to sustain Li Muwan's soul"),
                List.of("coffin", "soul", "li_muwan", "preservation", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "celestial_capture_net", "Celestial Capture Net", "仙罗网",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("early-Celestial era"), 5),
                OwnershipState.WANG_LIN_OWNED,
                new Transferability(
                        Transferability.CanTeach.TESTS_FIRST,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.PARTIAL,
                        Transferability.CanGiftEquivalent.YES_DERIVED_EQUIVALENT,
                        Transferability.CanCreateNew.REQUIRES_MATERIALS,
                        "Capture net used to capture Du Jian (who then became the source of the Copper Celestial Guard)."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial-tier capture net. Used to capture Du Jian (source of the Copper Celestial Guard).",
                List.of("Captured Du Jian (Copper Celestial Guard source)"),
                List.of("net", "celestial", "capture", "du_jian", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "celestial_emperor_crown", "Celestial Emperor Crown", "仙皇冠",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("late era"), 5,
                        "Per alt-wiki: requires 9,999 mortal emperors."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Requires 9,999 mortal emperors — cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A celestial crown. Per alt-wiki: requires 9,999 mortal emperors to forge. Represents Wang Lin's authority over mortal emperors across the worlds.",
                List.of("Forged from 9,999 mortal emperors",
                        "Authority-symbol treasure"),
                List.of("crown", "celestial", "mortal_emperor", "wang_lin_owned", "authority")
        ));

        register(CanonicalEntry.of(
                "thunder_origin_sword", "Thunder Origin Sword", "雷霆源剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1625+"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed from the strange power within the vortex formed by the collapse of the void gate."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the six Origin Swords Wang Lin condensed using the strange power within the vortex formed by the collapse of the void gate. Thunder-attribute.",
                List.of("One of the 6 Origin Swords",
                        "Condensed from void-gate collapse vortex"),
                List.of("sword", "thunder", "origin", "wang_lin_created", "void_gate")
        ));

        register(CanonicalEntry.of(
                "slaughter_origin_sword", "Slaughter Origin Sword", "杀戮源剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1561+ (from Qing Shui)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Originally Qing Shui's; given to Wang Lin. One of the 6 Origin Swords."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the 6 Origin Swords. Originally Qing Shui's Slaughter Sword, given to Wang Lin. Slaughter-attribute. Anchors the Slaughter Essence.",
                List.of("Originally Qing Shui's",
                        "Anchors the Slaughter Essence"),
                List.of("sword", "slaughter", "origin", "qing_shui", "inheritance")
        ));

        // ── Additional flying swords / weapons (I16–I31) ─────────────
        register(CanonicalEntry.of(
                "I09_dragon_formation", "The Dragon Formation (Li Muwan gift)", "龙阵",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent-Soul era"), 5,
                        "Cross-refs RICanonicalDatabase I09."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gift from Li Muwan; built based on Fighting Evil Sect formation. Sentiment-bound — Wang Lin retains as memento."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Formation-array treasure based on the Fighting Evil Sect (斗邪宗) formation. Gift from Li Muwan — sentiment-bound; retained to the end. Spirit-tier.",
                List.of("Formation-array treasure",
                        "Gift from Li Muwan — sentiment-bound",
                        "Built on Fighting Evil Sect formation",
                        "Retained to end"),
                List.of("formation", "li_muwan", "fighting_evil_sect", "sentiment", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I16_crystal_sword", "Crystal Sword (Pseudo Nirvana Void)", "水晶剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I16."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Acquired mid-era; later restored. Pseudo-Nirvana-Void-tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Upper-Nirvana-Void-Treasure-grade sword. Deceptively powerful despite its crystal appearance. Restored after breakage.",
                List.of("Upper-Nirvana-Void-Treasure grade",
                        "Deceptively powerful",
                        "Restored after breakage"),
                List.of("sword", "nirvana_void", "wang_lin_owned", "restored")
        ));

        register(CanonicalEntry.of(
                "I17_short_sword_black_white", "Short Sword (Black & White Dual Swords)", "白黑双剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I17."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Acquired mid-era; restored after breakage."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Black & white dual short swords. Spirit-tier paired swords. Restored after breakage.",
                List.of("Paired black & white short swords",
                        "Spirit-tier",
                        "Restored after breakage"),
                List.of("sword", "dual", "black_white", "wang_lin_owned", "restored")
        ));

        register(CanonicalEntry.of(
                "I18_half_moon_blade", "Half-Moon Blade", "半月刃",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I18."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfForged("Acquired mid-era; later retired."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Half-moon-shaped blade weapon. Spirit-tier. Retired (replaced by later swords).",
                List.of("Half-moon-shaped blade",
                        "Spirit-tier",
                        "Retired (replaced by later swords)"),
                List.of("blade", "half_moon", "wang_lin_owned", "retired")
        ));

        register(CanonicalEntry.of(
                "I19_axe_giant_demon_clan", "Axe of Giant Demon Clan", "巨魔族斧",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I19."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Acquired mid-era; eventually destroyed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Giant-Demon-Clan axe. Spirit-tier. Eventually destroyed.",
                List.of("Giant-Demon-Clan axe",
                        "Spirit-tier",
                        "Eventually destroyed"),
                List.of("axe", "giant_demon_clan", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I20_rusted_iron_sword", "Rusted Iron Sword (Pseudo Nirvana Void)", "锈铁剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I20."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Acquired mid-era; eventually destroyed. Pseudo-Nirvana-Void tier despite rusted appearance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Upper-Nirvana-Void-Treasure-grade sword. Deceptively powerful despite rusted appearance. Eventually destroyed.",
                List.of("Upper-Nirvana-Void-Treasure grade",
                        "Deceptively powerful despite rust",
                        "Eventually destroyed"),
                List.of("sword", "rusted", "nirvana_void", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I21_seven_colored_god_void_nails", "Seven-Colored God Void Nails", "七彩通天钉",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I21."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Acquired mid-era."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Seven-colored nails that pierce void and god-tier defenses. Spirit-tier.",
                List.of("Seven-colored nails",
                        "Pierce void and god-tier defenses",
                        "Spirit-tier"),
                List.of("nails", "seven_colored", "void_piercing", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I22_blood_slaughter_sword", "Blood Slaughter Sword (Seven Swords of the Ancient Dao)", "血煞剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I22."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("One of the Seven Swords of the Ancient Dao."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blood Slaughter Sword — one of the Seven Swords of the Ancient Dao. Spirit-tier; slaughter-bloodline attribute.",
                List.of("One of the Seven Swords of the Ancient Dao",
                        "Slaughter-bloodline attribute",
                        "Spirit-tier"),
                List.of("sword", "blood_slaughter", "ancient_dao", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I23_yin_blade", "Yin Blade", "阴刃",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I23."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Acquired mid-era; eventually destroyed. Yin-attribute."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Yin-attribute blade. Spirit-tier. Eventually destroyed.",
                List.of("Yin-attribute blade",
                        "Spirit-tier",
                        "Eventually destroyed"),
                List.of("blade", "yin", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I24_heaven_splitting_axe_ancestral", "Heaven-Splitting Axe (Ancestral)", "开天斧",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1626 (restored)"), 5,
                        "Cross-refs RICanonicalDatabase I24. The axe is the spirit of the Realm-Sealing Grand Array."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Spirit of the Realm-Sealing Grand Array. Restored when Wang Lin blasted open the Gate of Emptiness (Ch. 1626). Cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancestral Heaven-Splitting Axe — the spirit of the Realm-Sealing Grand Array. Restored when Wang Lin blasted open the Gate of Emptiness (Ch. 1626). Symbolizes Wang Lin's authority as Lord of the Sealed Realm.",
                List.of("Spirit of the Realm-Sealing Grand Array",
                        "Restored via Gate of Emptiness (Ch. 1626)",
                        "Symbol of Sealed Realm lordship"),
                List.of("axe", "heaven_splitting", "realm_sealing", "ancestral", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I25_demon_blade_earth_burial", "Demon Blade, Earth Burial", "魔刀·葬土",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I25."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Demon-element blade; earth-burial attribute."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Demon Blade Earth Burial — demon-element blade with earth-burial attribute. Spirit-tier.",
                List.of("Demon-element blade",
                        "Earth-burial attribute",
                        "Spirit-tier"),
                List.of("blade", "demon", "earth_burial", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I26_fog_devil_lance", "Fog Devil Lance", "雾魔枪",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1386 (Ancient Tomb)"), 5,
                        "Cross-refs RICanonicalDatabase I26."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Devil weapon. Given to Wang Lin's Ancient Demon clone."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fog Devil Lance — Ancient Devil weapon obtained in the Ancient Tomb (Ch. 1386). Lance form; devil-element power. Given to Wang Lin's Ancient Demon clone.",
                List.of("Ancient Devil weapon",
                        "Lance form",
                        "Devil-element power",
                        "Given to Ancient Demon clone"),
                List.of("lance", "fog_devil", "ancient_devil", "ancient_tomb", "ancient_demon_clone")
        ));

        register(CanonicalEntry.of(
                "I27_li_guang_bow", "Li Guang's Heaven-Shattering Bow", "李广弓",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1533"), 5,
                        "Cross-refs RICanonicalDatabase I27. Taught by Lian Daofei."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Bow Dao taught by Lian Daofei (madman) inside the Nether Beast (Ch. 1533). Forms Heaven-Shattering Bow Dao with Li Guang's Arrow."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Bow Dao treasure. Taught by Lian Daofei inside the Nether Beast (Ch. 1533). Forms Heaven-Shattering Bow Dao with Li Guang's Arrow. Fuses with Wang Lin's thunder essence for armor-piercing shots. Kills across realms.",
                List.of("Bow Dao treasure",
                        "Taught by Lian Daofei (Ch. 1533)",
                        "Forms Heaven-Shattering Bow Dao with Li Guang's Arrow",
                        "Fuses with thunder essence for armor-piercing shots",
                        "Kills across realms"),
                List.of("bow", "li_guang", "lian_daofei", "wang_lin_owned", "armor_piercing")
        ));

        register(CanonicalEntry.of(
                "I28_li_guang_arrow", "Li Guang's Arrow", "李广箭",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1577"), 5,
                        "Cross-refs RICanonicalDatabase I28."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Paired with Li Guang's Bow (Ch. 1577). Finite ammo — each shot depletes a Spirit-Vein-tier resource."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Arrow paired with Li Guang's Heaven-Shattering Bow. Each shot depletes a Spirit-Vein-tier resource — finite ammo supply.",
                List.of("Paired with Li Guang's Bow",
                        "Each shot depletes a Spirit-Vein-tier resource",
                        "Finite ammo supply"),
                List.of("arrow", "li_guang", "consumable", "wang_lin_owned", "spirit_vein")
        ));

        register(CanonicalEntry.of(
                "I29_seven_colored_lance", "Seven-Colored Lance", "七彩枪",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1543"), 5,
                        "Cross-refs RICanonicalDatabase I29. Taught by Lian Daofei."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taught by Lian Daofei (madman) inside the Nether Beast (Ch. 1543)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Seven-colored lance. Seven-colored light affects emotions of anyone who sees it. Three transformations: black → white → gray. Similar to Ethereal Fire but stronger.",
                List.of("Seven-colored light affects emotions",
                        "Three transformations: black → white → gray",
                        "Similar to Ethereal Fire but stronger"),
                List.of("lance", "seven_colored", "lian_daofei", "wang_lin_owned", "emotional")
        ));

        register(CanonicalEntry.of(
                "I30_slaughter_sword_qing_shui", "Slaughter Sword (from Immortal Lord Qing Shui)", "杀戮剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1561"), 5,
                        "Cross-refs RICanonicalDatabase I30. Originally Qing Shui's Slaughter Sword."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by Immortal Lord Qing Shui (Ch. 1561). Fuses with Wang Lin's Slaughter Origin to complete Slaughter Essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Origin Treasure (Third-Step) sword. Gifted by Immortal Lord Qing Shui (Ch. 1561). Fuses with Wang Lin's Slaughter Origin to complete Slaughter Essence. Condensed by Qing Shui using Sky Gate vortex power.",
                List.of("Origin Treasure (Third-Step) sword",
                        "Gifted by Qing Shui (Ch. 1561)",
                        "Fuses with Slaughter Origin → Slaughter Essence",
                        "Condensed by Qing Shui via Sky Gate vortex"),
                List.of("sword", "slaughter", "qing_shui", "origin_treasure", "sky_gate")
        ));

        register(CanonicalEntry.of(
                "I31_lightning_sword", "Lightning Sword (Origin Treasure)", "雷剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1625"), 5,
                        "Cross-refs RICanonicalDatabase I31. Self-condensed via void-gate vortex."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed via void-gate vortex (Ch. 1625)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder-element Origin Treasure sword. One of six fundamental origin-treasure swords. Self-condensed via void-gate vortex.",
                List.of("Thunder-element Origin Treasure sword",
                        "One of six fundamental origin-treasure swords",
                        "Self-condensed via void-gate vortex"),
                List.of("sword", "lightning", "thunder", "origin_treasure", "wang_lin_created", "void_gate")
        ));

        register(CanonicalEntry.withSpec(
                "I32_six_origin_swords", "Six Origin Swords (Fire, Life-Death, Karma, True-False, Slaughter, Restriction)", "六本源剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1715"), 5,
                        "Cross-refs RICanonicalDatabase I32."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed from void-gate vortex (Ch. 1715)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Six fundamental origin-element swords embodying Wang Lin's Origins. Each embodies one Origin: Fire, Life-Death, Karma, True-False, Slaughter, Restriction. Set completes power when all six are condensed.",
                List.of("6 fundamental origin-element swords",
                        "Each embodies one Origin",
                        "Set completes when all 6 are condensed",
                        "Self-condensed from void-gate vortex (Ch. 1715)"),
                List.of("sword", "origin", "six_origin_swords", "wang_lin_created", "void_gate", "wang_lin_owned"),
                "wang_lin_flying_swords"
        ));

        // ── Additional armor / shields / accessories (I35–I49) ───────
        register(CanonicalEntry.of(
                "I35_ancient_god_furnace", "Ancient God Furnace / Ancient God Cauldron", "古神鼎",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 838 (obtained)", "Ch. 1226 (destroyed)"), 5,
                        "Cross-refs RICanonicalDatabase I35. Tu Si inheritance."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Tu Si inheritance (Ch. 838). Created by Tu Si but discarded as unsatisfactory. Ancient God Treasure tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient God Furnace / Cauldron. Positional-swap treasure — teleport-swap with another target. Created by Tu Si but discarded as unsatisfactory. Ancient God Treasure tier. Destroyed Ch. 1226.",
                List.of("Positional-swap — teleport-swap with another target",
                        "Created by Tu Si but discarded as unsatisfactory",
                        "Ancient God Treasure tier",
                        "Destroyed Ch. 1226"),
                List.of("furnace", "cauldron", "tu_si", "ancient_god", "teleport_swap", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I37_emperor_furnace", "Emperor Furnace / Heavenly Emperor Furnace", "帝炉",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~838"), 5,
                        "Cross-refs RICanonicalDatabase I37. Seized from Tan Lang."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Seized from Tan Lang (who got it in the Ancient Tomb). Royal Ancient God Treasure tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Emperor Furnace / Heavenly Emperor Furnace. Can capture and refine all things. Used to refine Esteemed Ling Dong into ancient slave. Royal Ancient God Treasure tier.",
                List.of("Captures and refines all things",
                        "Refined Esteemed Ling Dong into ancient slave",
                        "Royal Ancient God Treasure tier"),
                List.of("furnace", "emperor", "ancient_god", "royal", "wang_lin_owned", "tan_lang")
        ));

        register(CanonicalEntry.of(
                "I38_protection_bone_tablets", "Protection Bone Tablets (Gauntlets)", "护骨板",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Cave World era", "Ch. 1580 (destroyed)", "Ch. 1626 (restored)"), 5,
                        "Cross-refs RICanonicalDatabase I38."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient God inheritance. Morph-to-gauntlet form."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Protection Bone Tablets that transform into gauntlets. Contain 8-star Ancient God life-saving spell. Ancient God Treasure tier. Destroyed Ch. 1580; restored Ch. 1626 by blasting open Gate of Emptiness.",
                List.of("Transform into gauntlets",
                        "Contain 8-star Ancient God life-saving spell",
                        "Destroyed Ch. 1580; restored Ch. 1626"),
                List.of("armor", "gauntlet", "ancient_god", "tu_si", "life_saving", "restored")
        ));

        register(CanonicalEntry.of(
                "I39_ancient_god_bracer", "Ancient God Bracer", "古神臂甲",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("post-Ancient-Tomb era"), 4,
                        "Cross-refs RICanonicalDatabase I39."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Tomb acquisition (post-Ancient-Tomb era)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient God Bracer. Deploys the 9-star Ancient God life-saving ability: Ancient Blessing. Defensive bracer of the Ancient God race.",
                List.of("Deploys 9-star Ancient God Ancient Blessing",
                        "Defensive bracer of the Ancient God race",
                        "Ancient Tomb acquisition"),
                List.of("armor", "bracer", "ancient_god", "ancient_blessing", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I42_three_bells_shield", "Three Bells Shield", "三铃盾",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("early Celestial era", "Ch. 965 (given to Ling'er)"), 5,
                        "Cross-refs RICanonicalDatabase I42."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Early Celestial era acquisition. Given to Ling'er (Ch. 965)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three Bells Shield. Three bells ring on impact — auditory warning system. Spirit-tier. Given away to Ling'er (Ch. 965).",
                List.of("Three bells ring on impact",
                        "Auditory warning system",
                        "Spirit-tier",
                        "Given to Ling'er (Ch. 965)"),
                List.of("shield", "bells", "auditory", "ling_er", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I44_gui_yi_sect_earth_armor", "Gui Yi Sect's Armour (Earth Element)", "鬼衣宗甲",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1735 (1st from Water General)", "Ch. 1886 (2nd from Old Ancestor Green Bull)"), 5,
                        "Cross-refs RICanonicalDatabase I44."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Two copies obtained: 1st from Water General of Planet Five Elements (Ch. 1735); 2nd via Old Ancestor Green Bull (Ch. 1886)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Earth-element human-shaped armor from the Gui Yi Sect. Rare even within the Gui Yi Sect. Two copies obtained.",
                List.of("Earth-element human-shaped armor",
                        "Rare even within the Gui Yi Sect",
                        "Two copies obtained (Ch. 1735, Ch. 1886)"),
                List.of("armor", "earth", "gui_yi_sect", "wang_lin_owned", "green_bull")
        ));

        register(CanonicalEntry.of(
                "I45_heavenly_bull_soul_armour", "Heavenly Bull Soul Armour", "天牛魂甲",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1874 (obtained)", "Ch. 1989 (destroyed)"), 5,
                        "Cross-refs RICanonicalDatabase I45."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Acquired late-game (Ch. 1874). Destroyed Ch. 1989."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul-armor of Heavenly Bull Continent origin. Spirit-tier soul protection. Destroyed Ch. 1989.",
                List.of("Soul-armor of Heavenly Bull Continent origin",
                        "Spirit-tier soul protection",
                        "Destroyed Ch. 1989"),
                List.of("armor", "soul", "heavenly_bull", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I46_blue_umbrella", "Blue Umbrella", "蓝伞",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1835 (won)", "Ch. 1869 (destroyed)"), 5,
                        "Cross-refs RICanonicalDatabase I46."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Won in a bet with Yan Lu (Ch. 1835). Destroyed Ch. 1869."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Defensive umbrella-form treasure. Won in a bet with Yan Lu (Ch. 1835). Destroyed Ch. 1869.",
                List.of("Defensive umbrella-form treasure",
                        "Won in a bet with Yan Lu (Ch. 1835)",
                        "Destroyed Ch. 1869"),
                List.of("umbrella", "defensive", "yan_lu", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I47_jade_thunder_defense", "Jade (Thunder Defense)", "御雷玉",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 871 (Xi Zifeng gift)"), 5,
                        "Cross-refs RICanonicalDatabase I47."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Obtained from Xi Zifeng (Ch. 871)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Jade that defends against powerful thunder. Spirit-tier thunder ward. Obtained from Xi Zifeng (Ch. 871).",
                List.of("Defends against powerful thunder",
                        "Spirit-tier thunder ward",
                        "Obtained from Xi Zifeng (Ch. 871)"),
                List.of("jade", "thunder", "defensive", "xi_zifeng", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I48_dagger_ge_hong", "A Dagger (from Ge Hong)", "匕首",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 747"), 5,
                        "Cross-refs RICanonicalDatabase I48. Obtained in Thunder Celestial Realm."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Obtained from Ge Hong in Thunder Celestial Realm (Ch. 747). Spirit-tier dagger; never used by Wang Lin."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Spirit-tier dagger obtained from Ge Hong in Thunder Celestial Realm (Ch. 747). Never used by Wang Lin.",
                List.of("Spirit-tier dagger",
                        "Obtained from Ge Hong (Ch. 747)",
                        "Never used by Wang Lin"),
                List.of("dagger", "ge_hong", "thunder_celestial_realm", "wang_lin_owned")
        ));

        // ── Soul-related artifacts (I50–I62) ─────────────────────────
        register(CanonicalEntry.of(
                "I50_soul_flag", "Soul Flag (Soul Refining Sect)", "魂旗",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent Soul era"), 5,
                        "Cross-refs RICanonicalDatabase I50. Soul Refining Sect inheritance."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Soul Refining Sect inheritance — Dun Tian gift (mid-Nascent Soul era)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Flag of the Soul Refining Sect. Holds and refines captured souls. Spirit-tier soul storage. Retired (replaced by Billion Soul Flag).",
                List.of("Holds and refines captured souls",
                        "Soul Refining Sect standard soul flag",
                        "Spirit-tier soul storage",
                        "Retired (replaced by Billion Soul Flag)"),
                List.of("soul", "flag", "soul_refining_sect", "dun_tian", "retired")
        ));

        register(CanonicalEntry.of(
                "I51_billion_soul_flag", "Billion Soul Flag / Ten-Billion Soul Banner", "十亿魂幡",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("mid-Nascent Soul era"), 5,
                        "Cross-refs RICanonicalDatabase I51."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Gifted by Dun Tian (Soul Refining Sect predecessor)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Billion Soul Flag / Ten-Billion Soul Banner. Contains 37 main souls + 1 billion ordinary souls. Freely changes size; can fuse into Soul-Ascension-level soul. 4th mysterious soul can battle Ascension cultivators. Retired / repaired.",
                List.of("37 main souls + 1 billion ordinary souls",
                        "Freely changes size",
                        "Can fuse into Soul-Ascension-level soul",
                        "4th mysterious soul can battle Ascension cultivators",
                        "Retired / repaired"),
                List.of("soul", "flag", "billion_soul", "dun_tian", "soul_refining_sect", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I52_devil_soul_bottle", "Devil Soul Bottle", "魔鬼魂瓶",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1388"), 5,
                        "Cross-refs RICanonicalDatabase I52."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Tomb acquisition (Ch. 1388). Ancient Devil Treasure tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Devil Soul Bottle. Contains multiple Ancient Devil souls. Ancient Devil Treasure tier soul storage. Obtained in the Ancient Tomb (Ch. 1388).",
                List.of("Contains multiple Ancient Devil souls",
                        "Ancient Devil Treasure tier soul storage",
                        "Obtained in the Ancient Tomb (Ch. 1388)"),
                List.of("soul", "bottle", "ancient_devil", "ancient_tomb", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I53_soul_lasher", "Soul Lasher / Kunji Whip", "坤极鞭",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("early-Nascent Soul era (acquired)", "Ch. 731 (fused into Karma Whip)"), 5,
                        "Cross-refs RICanonicalDatabase I53."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Taken from Hong Die's corpse (originally Red Butterfly's). Fused into Karma Whip (Ch. 731)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Lasher / Kunji Whip. Directly attacks primordial/origin soul at warp speed. Tian Yu Sect heavy treasure. Originally Red Butterfly's; taken from Hong Die's corpse. Fused into Karma Whip (Ch. 731).",
                List.of("Directly attacks primordial/origin soul at warp speed",
                        "Tian Yu Sect heavy treasure",
                        "Originally Red Butterfly's",
                        "Fused into Karma Whip (Ch. 731)"),
                List.of("whip", "soul", "red_butterfly", "hong_die", "karma_whip", "fused")
        ));

        register(CanonicalEntry.of(
                "I54_carving_domain_of_time", "Carving with the Domain of Time", "时间雕刻",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("post-Tianyunzi tutelage"), 5,
                        "Cross-refs RICanonicalDatabase I54."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-carved after experiencing Time domain with Yunzhe Zi's help."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Self-carved artifact with embedded Time domain. Allows one to stop time for a brief duration. Time-domain-bound carving. Self-carved after Tianyunzi instruction. Eventually destroyed.",
                List.of("Allows brief time-stop",
                        "Time-domain-bound carving",
                        "Self-carved after Tianyunzi instruction",
                        "Eventually destroyed"),
                List.of("carving", "time", "domain", "tianyunzi", "wang_lin_created", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I56_soul_gourd", "Soul Gourd", "魂葫芦",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1836 (won)", "Ch. 1869 (destroyed)"), 5,
                        "Cross-refs RICanonicalDatabase I56."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Won in a bet with Yan Lu (Ch. 1836). Great Soul Sect treasure from 9th ancestor Luo Yunhai."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Soul Gourd. Gathered 1 billion dao souls inside (now 30 million — broken). Single-use: attack equal to peak mid-stage Void Tribulant cultivator. Great Soul Sect treasure from 9th ancestor Luo Yunhai. Destroyed Ch. 1869.",
                List.of("Gathered 1 billion dao souls (now 30 million — broken)",
                        "Single-use attack equal to peak mid-stage Void Tribulant",
                        "Great Soul Sect treasure from Luo Yunhai",
                        "Destroyed Ch. 1869"),
                List.of("soul", "gourd", "great_soul_sect", "luo_yunhai", "destroyed", "single_use")
        ));

        register(CanonicalEntry.of(
                "I57_giant_head_skull_ancient_clansman", "Giant Head Skull of an Ancient Clansman", "古族人头骨",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ancient Tomb"), 5,
                        "Cross-refs RICanonicalDatabase I57."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taken from Ancient Tomb. Ancient-clansman-tier utility skull."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Giant Head Skull of an Ancient Clansman. Trees on its head produce Ancient Leaves (太古息叶). Taken from the Ancient Tomb.",
                List.of("Trees on its head produce Ancient Leaves",
                        "Ancient-clansman-tier utility skull",
                        "Taken from the Ancient Tomb"),
                List.of("skull", "ancient_clansman", "ancient_tomb", "ancient_leaf", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I58_infant_skull", "Infant Skull", "婴儿头骨",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid-late game"), 4,
                        "Cross-refs RICanonicalDatabase I58. Seized from Da Yi Great Heavenly Venerable."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Seized from Da Yi Great Heavenly Venerable (mid-late game)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Infant Skull. Filled with death energy. Plot clue containing information left by Lu Mo (Wang Lin's clone). Wang Lin's soul felt strange familiarity.",
                List.of("Filled with death energy",
                        "Plot clue left by Lu Mo (Wang Lin's clone)",
                        "Wang Lin's soul felt strange familiarity"),
                List.of("skull", "infant", "lu_mo", "plot_clue", "death_energy", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I59_white_hair_strand", "White Hair Strand", "白发丝",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Immortal Emperor's Palace"), 4,
                        "Cross-refs RICanonicalDatabase I59."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Found in Immortal Emperor's Palace."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "White Hair Strand. Cannot be destroyed by the Immortal Emperor. Plot clue containing Lu Mo's information about reincarnation cycle. Found in Immortal Emperor's Palace.",
                List.of("Cannot be destroyed by the Immortal Emperor",
                        "Plot clue containing Lu Mo's information",
                        "Found in Immortal Emperor's Palace"),
                List.of("hair", "white", "lu_mo", "plot_clue", "reincarnation", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I60_blood_red_nascent_soul", "Blood-Red Nascent Soul", "血红元神",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1194"), 5,
                        "Cross-refs RICanonicalDatabase I60."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Acquired in Seven-Colored Realm (Ch. 1194). One-use consumable soul item."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blood-red nascent soul usable in Seven-Colored Realm. One-use consumable soul item. Acquired Ch. 1194; consumed.",
                List.of("Usable in Seven-Colored Realm",
                        "One-use consumable soul item",
                        "Acquired Ch. 1194; consumed"),
                List.of("soul", "blood_red", "seven_colored_realm", "consumable", "used")
        ));

        register(CanonicalEntry.of(
                "I61_blood_ancestors_blood_body", "Blood Ancestor's Blood Body", "血祖血身",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 769 (taken)", "Ch. 789 (exploded)"), 5,
                        "Cross-refs RICanonicalDatabase I61."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Taken from Blood Ancestor's Blood Planet (Ch. 769). One-use explosive body."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blood Ancestor's Blood Body. Explodable as a weapon — ancient-blood-tier power. One-use explosive body. Taken Ch. 769; exploded Ch. 789.",
                List.of("Explodable as a weapon",
                        "Ancient-blood-tier power",
                        "One-use explosive body",
                        "Exploded Ch. 789"),
                List.of("blood", "blood_ancestor", "explosive", "consumable", "used")
        ));

        register(CanonicalEntry.of(
                "I62_wandering_souls", "Wandering Souls", "游魂",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 119 era"), 5,
                        "Cross-refs RICanonicalDatabase I62."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wild-soul recruitment — Wang Lin's soul has Soul-Devourer nature."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wandering Souls. Very strong souls that obey Soul-Devourers as subjects obey a king. Found in spatial rifts. Battlefield soul weapons. Recruited due to Wang Lin's Soul-Devourer nature (Ch. 119 era).",
                List.of("Very strong souls obeying Soul-Devourers",
                        "Found in spatial rifts",
                        "Battlefield soul weapons",
                        "Recruited due to Soul-Devourer nature"),
                List.of("soul", "wandering", "soul_devourer", "spatial_rift", "wang_lin_owned")
        ));

        // ── Restriction-related treasures (I63–I74) ──────────────────
        register(CanonicalEntry.of(
                "I64_three_purple_flags", "Three Purple Flags", "三紫旗",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5,
                        "Cross-refs RICanonicalDatabase I64."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfForged("Early-mid era acquisition. Defensive treasure — set of three purple flags."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three Purple Flags. Defensive treasure — set of three purple flags. Spirit-tier protection. Retired.",
                List.of("Set of three purple flags",
                        "Defensive",
                        "Spirit-tier protection",
                        "Retired"),
                List.of("flags", "purple", "defensive", "wang_lin_owned", "retired")
        ));

        register(CanonicalEntry.of(
                "I65_three_ink_stones", "3× Ink Stones", "三墨石",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("early era"), 5,
                        "Cross-refs RICanonicalDatabase I65. Consumed to make Restriction Flags."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Early era acquisition. Consumed to craft Restriction Flags."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three Ink Stones used to craft the three Restriction Flags. Spirit-tier crafting material. Consumed during the Restriction Flag forging process.",
                List.of("Used to craft the three Restriction Flags",
                        "Spirit-tier crafting material",
                        "Consumed during Restriction Flag forging"),
                List.of("material", "ink_stones", "restriction_flag", "consumed")
        ));

        register(CanonicalEntry.of(
                "I66_restriction_breaking_ancient_mirror", "Restriction Breaking Ancient Mirror", "破禁古镜",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 774 (Tattoo Tribe era)"), 5,
                        "Cross-refs RICanonicalDatabase I66."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Tattoo Tribe era acquisition (Ch. 774). Ancient treasure tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Restriction Breaking Ancient Mirror. Breaks restrictions/formations by reflecting the restriction's logic back at itself. Ancient treasure tier. Obtained during the Tattoo Tribe era (Ch. 774).",
                List.of("Breaks restrictions by reflecting restriction's logic",
                        "Ancient treasure tier",
                        "Obtained during Tattoo Tribe era (Ch. 774)"),
                List.of("mirror", "restriction_breaking", "ancient", "tattoo_tribe", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I71_isolation_restriction_compass", "Isolation Restriction Compass", "隔离禁盘",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1850 (obtained)", "Ch. 1864 (discarded)"), 5,
                        "Cross-refs RICanonicalDatabase I71."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Took from Green Devil Continent woman at pill sea (Ch. 1850)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Isolation Restriction Compass. Contains Devil Restriction Sect's Devil Isolation Restriction. Tracking-vulnerable — someone used it to pinpoint Wang Lin's location. Discarded Ch. 1864.",
                List.of("Contains Devil Restriction Sect's Devil Isolation Restriction",
                        "Tracking-vulnerable",
                        "Discarded Ch. 1864"),
                List.of("compass", "restriction", "isolation", "devil_restriction_sect", "discarded")
        ));

        register(CanonicalEntry.of(
                "I74_basic_formation_book", "Basic Formation Book", "基础阵书",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early era"), 5,
                        "Cross-refs RICanonicalDatabase I74."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Early era acquisition. Mortal-tier instructional book kept as memento."),
                Demonstrability.ATTEST_ONLY,
                "Foundation formation knowledge manual. Mortal-tier instructional book. Kept by Wang Lin as a memento of his early-cultivation days.",
                List.of("Foundation formation knowledge manual",
                        "Mortal-tier instructional book",
                        "Kept as memento"),
                List.of("book", "formation", "foundation", "memento", "wang_lin_owned")
        ));

        // ── Storage treasures (I75–I81) ──────────────────────────────
        register(CanonicalEntry.of(
                "I77_soul_eye_jade", "Jade (Soul Eye Dao)", "灵眼玉",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1840 (gifted)", "Ch. 2023 (used)"), 5,
                        "Cross-refs RICanonicalDatabase I77. One of three promised gifts from Great Soul Sect founder."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Gift from founder of the Great Soul Sect — 2 of 3 promised gifts (Ch. ~1840)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Jade (Soul Eye Dao). See changes in Immortal Astral Continent ONCE. Divinate ONE thing — single use only. Formed by founder's lifetime Soul Eye Dao cultivation. Used Ch. 2023.",
                List.of("See changes in Immortal Astral Continent ONCE",
                        "Divinate ONE thing — single use only",
                        "Formed by founder's lifetime Soul Eye Dao cultivation",
                        "Used Ch. 2023"),
                List.of("jade", "soul_eye_dao", "divination", "great_soul_sect", "single_use", "consumed")
        ));

        register(CanonicalEntry.of(
                "I78_water_essence_drop", "A Drop of Crystal Clear Water (Water Essence Drop)", "水元精滴",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1843 (gifted)"), 5,
                        "Cross-refs RICanonicalDatabase I78."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gift from founder of Great Soul Sect (Ch. 1843). 3 of 3 promised gifts. Quasi-Third-Step tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Single drop of crystal clear water essence. Refined from 99 rivers with multiple water-essence cultivators. Used to help comprehend Water Essence. Quasi-Third-Step tier.",
                List.of("Single drop of crystal clear water essence",
                        "Refined from 99 rivers by multiple water-essence cultivators",
                        "Helped comprehend Water Essence",
                        "Quasi-Third-Step tier"),
                List.of("water", "essence_drop", "great_soul_sect", "wang_lin_owned", "water_essence")
        ));

        register(CanonicalEntry.withSpec(
                "I79_earth_palace", "Earth Palace", "地宫",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1478"), 5,
                        "Cross-refs RICanonicalDatabase I79. Ye Mo inheritance."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ye Mo inheritance (Ch. 1478). One of three palaces of Ye Mo's Inheritance (heaven, earth, human)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Earth Palace. One of three palaces of Ye Mo's Inheritance (heaven, earth, human). Ye Mo inheritance treasure.",
                List.of("One of three palaces of Ye Mo's Inheritance",
                        "Ye Mo inheritance treasure"),
                List.of("palace", "earth", "ye_mo", "storage", "inheritance", "wang_lin_owned"),
                "wang_lin_storage_treasures"
        ));

        // ── Consumable attack treasures (I82–I88) ────────────────────
        register(CanonicalEntry.of(
                "I82_lu_fu_blood_balls", "Lu Fu Blood Balls", "陆符血球",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 947 (1st)", "Ch. 1095 (×6)"), 5,
                        "Cross-refs RICanonicalDatabase I82."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Early-acquired from Lu Fu (1st: Ch. 947; ×6: Ch. 1095)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Lu Fu Blood Balls. Consumable blood-ball weapons. Up to 6 used at once. Spirit-tier explosive power. All used.",
                List.of("Consumable blood-ball weapons",
                        "Up to 6 used at once",
                        "Spirit-tier explosive power",
                        "All consumed"),
                List.of("blood_balls", "lu_fu", "consumable", "explosive", "used")
        ));

        register(CanonicalEntry.of(
                "I83_ji_qiong_head", "Ji Qiong's Head", "计穷头",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1127 (taken)"), 5,
                        "Cross-refs RICanonicalDatabase I83."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Took from Ji Qiong (Ch. 1127). Used as consumable weapon."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ji Qiong's severed head. Used as consumable weapon. Spirit-tier power. Taken Ch. 1127. The skull had a divine sense left by Slaughter telling how to revive Li Muwan.",
                List.of("Severed head used as consumable weapon",
                        "Spirit-tier power",
                        "Divine sense left by Slaughter (Li Muwan revival clue)"),
                List.of("head", "ji_qiong", "consumable", "slaughter_clue", "used")
        ));

        register(CanonicalEntry.of(
                "I84_green_fragment_heaven_power", "Green Fragment with the Power of Heaven", "青碎片",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I84."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Mid-era acquisition. One-shot attack consumable."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Green Fragment with the Power of Heaven. Contains a thread of Heavenly power. One-shot attack consumable. Celestial-tier power. Used.",
                List.of("Contains a thread of Heavenly power",
                        "One-shot attack consumable",
                        "Celestial-tier power",
                        "Used"),
                List.of("fragment", "green", "heaven_power", "consumable", "used")
        ));

        register(CanonicalEntry.of(
                "I85_beads_seven_colored_realm", "Beads from the Seven-Colored Realm", "七彩界珠",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1197"), 5,
                        "Cross-refs RICanonicalDatabase I85."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Seven-Colored Realm acquisition (Ch. 1197)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Beads from the Seven-Colored Realm. Mimic Heaven-Defying Bead in appearance (without five-elements pattern). Used as decoys / one-use attack beads. Seven-Colored Realm treasure tier.",
                List.of("Mimic Heaven-Defying Bead appearance (no five-elements pattern)",
                        "Used as decoys / one-use attack beads",
                        "Seven-Colored Realm treasure tier"),
                List.of("beads", "seven_colored_realm", "decoy", "consumable", "used")
        ));

        register(CanonicalEntry.of(
                "I86_heaven_dao_crystal", "Heaven Dao Crystal", "天道晶",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid era"), 5,
                        "Cross-refs RICanonicalDatabase I86."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Mid-era acquisition. One-use consumable."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heaven Dao Crystal. Crystallized shard of Heavenly Dao power. One-use consumable. Celestial-tier power. Used.",
                List.of("Crystallized shard of Heavenly Dao power",
                        "One-use consumable",
                        "Celestial-tier power",
                        "Used"),
                List.of("crystal", "heaven_dao", "consumable", "celestial", "used")
        ));

        register(CanonicalEntry.of(
                "I87_nine_drops_poison", "Nine Drops of Poison", "九滴毒",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1460 (self-refined)", "Ch. 1526 (used)"), 5,
                        "Cross-refs RICanonicalDatabase I87. Self-refined from Joss Flames of Dao Master Miao Yin."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-refined (Ch. 1460). Quasi-Third-Step poison power."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Nine Drops of Poison. Refined from Joss Flames of Dao Master Miao Yin and Great Desolation's poison. Quasi-Third-Step poison power. One-use lethal poison. Self-refined Ch. 1460; used Ch. 1526.",
                List.of("Refined from Joss Flames of Dao Master Miao Yin",
                        "Quasi-Third-Step poison power",
                        "One-use lethal poison",
                        "Self-refined Ch. 1460; used Ch. 1526"),
                List.of("poison", "nine_drops", "miao_yin", "joss_flame", "consumable", "wang_lin_created")
        ));

        register(CanonicalEntry.withSpec(
                "I88_celestial_wine_jug", "Celestial Wine (in a jug)", "仙酒",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~625 (found)"), 5,
                        "Cross-refs RICanonicalDatabase I88. Found in Qing Lin's false cave."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Found in Qing Lin's false cave in Demon Spirit Land (~Ch. 625)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial Wine in a jug. Drinking or spilling grants temporary celestial-spirit-energy boost. Celestial-tier consumable wine. Found in Qing Lin's false cave (~Ch. 625).",
                List.of("Drinking or spilling grants temporary celestial-spirit-energy boost",
                        "Celestial-tier consumable wine",
                        "Found in Qing Lin's false cave (~Ch. 625)"),
                List.of("wine", "celestial", "qing_lin", "consumable", "used"),
                "qing_lin_inheritance"
        ));

        // ── Tools / accessories (I89–I108) ───────────────────────────
        register(CanonicalEntry.of(
                "I89_pair_metal_flints", "Pair of Metal Element Flints", "金属火石对",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 672"), 5,
                        "Cross-refs RICanonicalDatabase I89."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Took from Huang Family member's bag of holding (Ch. 672)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Pair of Metal Element Flints. Naturally-formed magical treasure flint pair. Metal-element flints. Took from Huang Family member's bag of holding (Ch. 672). Retired.",
                List.of("Naturally-formed magical treasure flint pair",
                        "Metal-element flints",
                        "Took from Huang Family member (Ch. 672)",
                        "Retired"),
                List.of("flints", "metal", "huang_family", "wang_lin_owned", "retired")
        ));

        register(CanonicalEntry.of(
                "I92_sword_teleportation_spell", "Sword with Teleportation Spell (Pseudo Celestial)", "传送剑",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 662 (made)", "Ch. 665 (sold)"), 5,
                        "Cross-refs RICanonicalDatabase I92."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin-made version sold Ch. 665; later version taken from Bai Zhan (Elder Ji Mo's disciple)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Sword with Teleportation Spell. Instant teleportation on strike. Blood-refined and primordial-spirit-fused. Pseudo Celestial Treasure tier. Wang Lin-made version sold Ch. 665; later Bai Zhan version retained.",
                List.of("Instant teleportation on strike",
                        "Blood-refined and primordial-spirit-fused",
                        "Pseudo Celestial Treasure tier",
                        "Original Wang Lin-made version sold Ch. 665",
                        "Bai Zhan version retained"),
                List.of("sword", "teleportation", "pseudo_celestial", "wang_lin_created", "battlemage")
        ));

        register(CanonicalEntry.of(
                "I93_hairpin_thousand_illusion_ruthless", "Hairpin with Thousand Illusion Ruthless Domain", "千幻无情簪",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 662 (made)"), 5,
                        "Cross-refs RICanonicalDatabase I93."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Wang Lin-made on Planet Ran Yun (~Ch. 662). Sold."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Hairpin with Thousand Illusion Ruthless Domain. Embeds the Thousand-Illusion Ruthless domain. Pseudo Celestial Treasure — domain-embedded hairpin. Made Ch. 662; sold.",
                List.of("Embeds the Thousand-Illusion Ruthless domain",
                        "Pseudo Celestial Treasure — domain-embedded hairpin",
                        "Wang Lin-made (Ch. 662)",
                        "Sold"),
                List.of("hairpin", "thousand_illusion", "domain", "wang_lin_created", "sold")
        ));

        register(CanonicalEntry.of(
                "I94_black_comb_19_teeth", "Black Comb, Nineteen Teeth", "十九齿黑梳",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 672"), 5,
                        "Cross-refs RICanonicalDatabase I94."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("One of two treasures in Huang Family member's bag of holding (Ch. 672)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Black comb with nineteen teeth containing attacking formation. Pseudo Celestial Treasure tier. One of two treasures in Huang Family member's bag of holding (Ch. 672).",
                List.of("Black comb with 19 teeth",
                        "Contains attacking formation",
                        "Pseudo Celestial Treasure tier",
                        "Taken from Huang Family member (Ch. 672)"),
                List.of("comb", "black", "nineteen_teeth", "pseudo_celestial", "huang_family", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I96_straw_hat", "Straw Hat / Li Ming Straw Hat", "黎明草帽",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("early Celestial era", "Ch. 965 (given to Ling'er)"), 5,
                        "Cross-refs RICanonicalDatabase I96."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Gifted by Yunque Zi / Tian Yunzi (early Celestial era)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Straw Hat (Li Ming Straw Hat). Blocks out divine senses. Identity concealment with numerous intricate formations. Yunque Zi's gift to potential Suzaku title candidates. Given away to Ling'er (Ch. 965).",
                List.of("Blocks out divine senses",
                        "Identity concealment with numerous formations",
                        "Yunque Zi's gift to potential Suzaku candidates",
                        "Given to Ling'er (Ch. 965)"),
                List.of("hat", "straw", "divine_sense_blocking", "identity_concealment", "tianyunzi", "ling_er")
        ));

        register(CanonicalEntry.of(
                "I97_bell_sealing_tracking", "A Bell (Sealing/Tracking)", "封踪铃",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~307"), 5,
                        "Cross-refs RICanonicalDatabase I97."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Early Celestial era acquisition (~Ch. 307)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Bell with Sealing and Tracking abilities. Celestial-tier bell. Early Celestial era acquisition (~Ch. 307). Retired.",
                List.of("Sealing and tracking abilities",
                        "Celestial-tier bell",
                        "Early Celestial era acquisition (~Ch. 307)",
                        "Retired"),
                List.of("bell", "sealing", "tracking", "celestial", "retired")
        ));

        register(CanonicalEntry.of(
                "I98_star_compass", "Star Compass", "星罗盘",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~477 (exchanged)"), 5,
                        "Cross-refs RICanonicalDatabase I98."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Exchanged with Giant Demon Clan Heir / Chi Hu (~Ch. 477)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Star Compass. Extremely fast void travel — no energy demanded. Enhanced with Ancient God memories. Void-only transportation. Upgraded to Silver Dragon Star Compass.",
                List.of("Extremely fast void travel",
                        "No energy demanded",
                        "Enhanced with Ancient God memories",
                        "Upgraded to Silver Dragon Star Compass"),
                List.of("compass", "star", "void_travel", "chi_hu", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I99_silver_dragon_star_compass", "Silver Dragon Star Compass", "银龙星罗盘",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 477 (self-upgraded)"), 5,
                        "Cross-refs RICanonicalDatabase I99."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-upgraded from Star Compass (Ch. 477)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Silver Dragon Star Compass. Self-upgraded Star Compass with silver-dragon beast. Void-only fast travel. No energy cost. Retired.",
                List.of("Self-upgraded Star Compass with silver-dragon beast",
                        "Void-only fast travel",
                        "No energy cost",
                        "Retired"),
                List.of("compass", "silver_dragon", "void_travel", "wang_lin_created", "retired")
        ));

        register(CanonicalEntry.of(
                "I100_celestial_mountain_soul", "Celestial Mountain Soul", "仙山魂",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 712 (obtained)", "Ch. 853 (exploded)", "Ch. 1177 (sold)"), 5,
                        "Cross-refs RICanonicalDatabase I100."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Celestial-era acquisition (Ch. 712)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial Mountain Soul. Celestial-tier soul fragment. Collapsed due to soul explosion (Ch. 853). Condensed remnant by Wang Lin. Sold Ch. 1177.",
                List.of("Celestial-tier soul fragment",
                        "Collapsed due to soul explosion (Ch. 853)",
                        "Condensed remnant by Wang Lin",
                        "Sold Ch. 1177"),
                List.of("soul", "celestial_mountain", "celestial", "sold")
        ));

        register(CanonicalEntry.of(
                "I101_broken_statue_ancient_celestial_emperor", "A Broken Statue (of the Ancient Celestial Emperor)", "残像",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1389"), 5,
                        "Cross-refs RICanonicalDatabase I101."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Tomb era acquisition (Ch. 1389)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Broken Statue of the Ancient Celestial Emperor. Depicts the Ancient Celestial Emperor — middle-aged man in cloud-pattern robe. Contact with its gaze causes entire body to tremble. Majestic aura despite damaged appearance.",
                List.of("Depicts the Ancient Celestial Emperor",
                        "Contact with its gaze causes body to tremble",
                        "Majestic aura despite damaged appearance",
                        "Ancient Tomb era acquisition (Ch. 1389)"),
                List.of("statue", "ancient_celestial_emperor", "ancient_tomb", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I102_cloak_vermilion_bird_emperor", "Cloak of Vermilion Bird Divine Emperor", "朱雀神帝斗篷",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1090"), 5,
                        "Cross-refs RICanonicalDatabase I102. Vermilion Bird Divine Emperor inheritance."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Emperor inheritance (Ch. 1090)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Cloak of Vermilion Bird Divine Emperor. Generational inheritance cloak of Vermilion Bird Divine Emperor. Vermilion Bird Divine Sect inheritance tier.",
                List.of("Generational inheritance cloak",
                        "Vermilion Bird Divine Emperor lineage",
                        "Vermilion Bird Divine Sect inheritance tier"),
                List.of("cloak", "vermilion_bird", "divine_emperor", "inheritance", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I103_vermilion_bird_holy_token", "Vermilion Bird Holy Token", "朱雀圣令",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1090"), 5,
                        "Cross-refs RICanonicalDatabase I103."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Emperor inheritance (~Ch. 1090)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Vermilion Bird Holy Token. Identification token for generations of Vermilion Bird Divine Emperors. Vermilion Bird Divine Sect authority.",
                List.of("Identification token for Vermilion Bird Divine Emperors",
                        "Vermilion Bird Divine Sect authority"),
                List.of("token", "vermilion_bird", "divine_emperor", "inheritance", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I104_holy_treasure_white_stone", "Holy Treasure (White Stone)", "白石圣宝",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1090 (obtained)", "Ch. 1124 (returned)"), 5,
                        "Cross-refs RICanonicalDatabase I104."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Vermilion Bird Divine Sect acquisition (~Ch. 1090). Returned (Ch. 1124)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Holy Treasure (White Stone). Most important treasure of Vermilion Bird Divine Sect. Supreme sect treasure. Returned Ch. 1124.",
                List.of("Most important treasure of Vermilion Bird Divine Sect",
                        "Supreme sect treasure",
                        "Returned Ch. 1124"),
                List.of("treasure", "white_stone", "vermilion_bird", "supreme", "returned")
        ));

        register(CanonicalEntry.of(
                "I105_vermilion_bird_feather", "Vermilion Bird Feather", "朱雀羽",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Vermilion Bird lineage"), 4,
                        "Cross-refs RICanonicalDatabase I105. Gifted by 1st-Generation Vermilion Bird."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by 1st-Generation Vermilion Bird."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Vermilion Bird Feather. Summons a true Vermilion Bird. Used to kill the third-generation evil sparrow. Celestial-tier — 1st-Gen Vermilion Bird gift.",
                List.of("Summons a true Vermilion Bird",
                        "Used to kill the third-generation evil sparrow",
                        "Celestial-tier — 1st-Gen Vermilion Bird gift"),
                List.of("feather", "vermilion_bird", "summon", "celestial", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I106_fire_bone", "Fire Bone", "火骨",
                CanonicalCategory.FLYING_SWORDS,
                Provenance.explicit("Renegade Immortal", List.of("Vermilion Bird lineage"), 4,
                        "Cross-refs RICanonicalDatabase I106. Gifted by 2nd-Generation Vermilion Bird."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gifted by 2nd-Generation Vermilion Bird."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fire Bone. Unleashes World Incineration Art (ultimate technique). Used when killing the third-generation Vermilion Bird. 2nd-Gen Vermilion Bird gift — celestial-tier.",
                List.of("Unleashes World Incineration Art (ultimate technique)",
                        "Used when killing the third-generation Vermilion Bird",
                        "2nd-Gen Vermilion Bird gift — celestial-tier"),
                List.of("bone", "fire", "world_incineration", "vermilion_bird", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I107_emerald_bracelet_li_qianmei", "Emerald Bracelet / Jade Bracelet", "翡翠镯",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1178 (gift)"), 5,
                        "Cross-refs RICanonicalDatabase I107. Gift from Li Qianmei."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gift from Li Qianmei — last of 3 treasures for answering 3 questions (Ch. 1178)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Emerald Bracelet / Jade Bracelet. Protective treasure bracelet. Gift from Li Qianmei — last of 3 treasures for answering 3 questions (Ch. 1178).",
                List.of("Protective treasure bracelet",
                        "Gift from Li Qianmei (Ch. 1178)",
                        "Last of 3 treasures for answering 3 questions"),
                List.of("bracelet", "emerald", "li_qianmei", "defensive", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I108_jade_bottle_black_liquid", "Jade Bottle with Black Liquid", "黑液玉瓶",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1191"), 5,
                        "Cross-refs RICanonicalDatabase I108. Seven-Colored Realm acquisition."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Seven-Colored Realm acquisition (Ch. 1191)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Jade Bottle with Black Liquid. Bottle half-filled with mysterious black liquid. Looks like blood but no blood smell. Seven-Colored Realm treasure.",
                List.of("Half-filled with mysterious black liquid",
                        "Looks like blood but no blood smell",
                        "Seven-Colored Realm treasure"),
                List.of("bottle", "black_liquid", "seven_colored_realm", "wang_lin_owned", "mysterious")
        ));

        // ── Late-game consumables & materials (I163–I178) ────────────
        register(CanonicalEntry.of(
                "I163_ancient_leaf", "Ancient Leaf / Ancient Breath Leaf", "太古息叶",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1387 (1st)", "Ch. 1449 (2nd)", "Ch. 1460 (×18)"), 5,
                        "Cross-refs RICanonicalDatabase I163."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Ancient Tomb acquisition (1st: Ch. 1387; ×18: Ch. 1460; 2nd: Ch. 1449)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancient Leaf / Ancient Breath Leaf. Releases powerful sealing force when used. 99 total in existence. Ancient-tier sealing leaves. Mostly used.",
                List.of("Releases powerful sealing force when used",
                        "99 total in existence",
                        "Ancient-tier sealing leaves",
                        "Mostly used"),
                List.of("leaf", "ancient", "sealing", "consumable", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I164_eternal_wood_spirit", "Eternal Wood Spirit", "长生木灵",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ancient Tomb"), 5,
                        "Cross-refs RICanonicalDatabase I164."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Ancient Tomb acquisition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Eternal Wood Spirit. Defensive wood-spirit. Ancient-tier origin.",
                List.of("Defensive wood-spirit",
                        "Ancient-tier origin"),
                List.of("wood_spirit", "defensive", "ancient_tomb", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I165_wood_carving_black_fiend", "Wood Carving (of Black Fiend Devil Saint)", "木雕",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 931"), 5,
                        "Cross-refs RICanonicalDatabase I165."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Took from Black Fiend Devil Saint (half-broken) (Ch. 931)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wood Carving of Black Fiend Devil Saint. Defends against Moongazer Serpent's Ancient God's Finger. Spirit-tier carving. Took from Black Fiend Devil Saint (half-broken) (Ch. 931).",
                List.of("Defends against Moongazer Serpent's Ancient God's Finger",
                        "Spirit-tier carving",
                        "Took from Black Fiend Devil Saint (half-broken) (Ch. 931)"),
                List.of("carving", "wood", "black_fiend", "defensive", "moongazer_serpent", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I166_dark_heaven_stone", "Dark Heaven Stone", "暗天石",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 965 (gift)"), 5,
                        "Cross-refs RICanonicalDatabase I166. Gift from Master Yi Chen."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Gift from Master Yi Chen (Ch. 965)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dark Heaven Stone. Store divine sense to create avatar or store power spells. Required by cultivators at high levels.",
                List.of("Store divine sense to create avatar",
                        "Store power spells",
                        "Required by cultivators at high levels",
                        "Gift from Master Yi Chen (Ch. 965)"),
                List.of("stone", "dark_heaven", "divine_sense_storage", "yi_chen", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I167_battle_scrolls_zhan", "Battle Scrolls ×3 (Zhan Family)", "战家三战卷",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1095"), 5,
                        "Cross-refs RICanonicalDatabase I167."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Zhan Family acquisition (Ch. 1095)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Battle Scrolls ×3 from the Zhan Family. Zhan Family's battle scrolls — set of 3. Spirit-tier combat scrolls. Anchors Wang Lin's Battle Will Domain.",
                List.of("Zhan Family battle scrolls",
                        "Set of 3",
                        "Spirit-tier combat scrolls",
                        "Anchors Wang Lin's Battle Will Domain"),
                List.of("scrolls", "battle", "zhan_family", "combat", "battle_will_domain", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I169_fragment_stamp_celestial_sealing", "Fragment Stamp", "碎片印",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 769 (forged)"), 5,
                        "Cross-refs RICanonicalDatabase I169. Evolved into 18-Hell Celestial Sealing Stamp."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-forged via divine tribulation (Ch. 769). Evolved into 18-Hell Celestial Sealing Stamp."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fragment Stamp. Refined by divine tribulation during Illusionary-Yin breakthrough (Ch. 769). Later evolved into 18-Hell Celestial Sealing Stamp.",
                List.of("Refined by divine tribulation (Ch. 769)",
                        "Illusionary-Yin breakthrough era",
                        "Evolved into 18-Hell Celestial Sealing Stamp"),
                List.of("stamp", "fragment", "divine_tribulation", "wang_lin_created", "evolved")
        ));

        register(CanonicalEntry.of(
                "I170_divine_soul_brush", "Divine Soul Brush / Golden Celestial Brush", "点仙笔",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 625 (taken)", "Ch. 1178 (given to Li Qianmei)"), 5,
                        "Cross-refs RICanonicalDatabase I170. Originally Qing Lin's creation for his daughter Qing Shuang."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Seized from short old man in Demon Spirit Land (Ch. 625); originally Qing Lin's creation."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Divine Soul Brush / Golden Celestial Brush. Outlines powerful talismanic scripts. Created by Immortal Emperor Qing Lin as gift to daughter Qing Shuang. Celestial-tier brush. Seized Ch. 625; given to Li Qianmei Ch. 1178.",
                List.of("Outlines powerful talismanic scripts",
                        "Created by Immortal Emperor Qing Lin",
                        "Originally a gift for daughter Qing Shuang",
                        "Celestial-tier brush",
                        "Given to Li Qianmei (Ch. 1178)"),
                List.of("brush", "divine_soul", "qing_lin", "celestial", "talismanic", "li_qianmei")
        ));

        register(CanonicalEntry.of(
                "I171_blood_jades_yao_xixue", "Blood Jades", "血玉",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~580 (stolen)"), 5,
                        "Cross-refs RICanonicalDatabase I171."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Stolen from Yao Xixue's bag of holding (~Ch. 580)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Blood Jades. Spirit-tier blood jades. Stolen from Yao Xixue's bag of holding (~Ch. 580). Retired.",
                List.of("Spirit-tier blood jades",
                        "Stolen from Yao Xixue's bag of holding (~Ch. 580)",
                        "Retired"),
                List.of("jades", "blood", "yao_xixue", "stolen", "retired")
        ));

        register(CanonicalEntry.of(
                "I172_seven_star_sword_formation", "Seven Star Sword Formation", "七星剑阵",
                CanonicalCategory.FORMATIONS,
                Provenance.explicit("Renegade Immortal", List.of("mid era", "Ch. 715 (destroyed)"), 5,
                        "Cross-refs RICanonicalDatabase I172. Assembled from Ling Tianhou's disciples' swords."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("Assembled from 7 of Ling Tianhou's disciples' 12 swords. Destroyed Ch. 715."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Seven Star Sword Formation. Sword formation from seven of Ling Tianhou's disciples' twelve swords. Spirit-tier set-formation. Destroyed Ch. 715.",
                List.of("Sword formation from 7 of Ling Tianhou's 12 disciple swords",
                        "Spirit-tier set-formation",
                        "Destroyed Ch. 715"),
                List.of("formation", "sword", "seven_star", "ling_tianhou", "destroyed")
        ));

        register(CanonicalEntry.of(
                "I173_golden_print_xuan_luo", "Golden Print (Xuan Luo-forged)", "金印",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1772 (forged)"), 5,
                        "Cross-refs RICanonicalDatabase I173."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Forged by Xuan Luo (Wang Lin's master) (Ch. 1772). Quasi-Third-Step tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Golden Print (Xuan Luo-forged). Sovereign's god-destruction dao spell turned into corporeal treasure. Contains hint of Nine Suns' power — indestructible. Quasi-Third-Step tier.",
                List.of("Sovereign's god-destruction dao spell turned corporeal",
                        "Contains hint of Nine Suns' power — indestructible",
                        "Quasi-Third-Step tier",
                        "Forged by Xuan Luo (Ch. 1772)"),
                List.of("print", "golden", "xuan_luo", "quasi_third_step", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I174_lantern_origin_soul_protection", "Lantern (origin soul protection)", "长明灯",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1867"), 5,
                        "Cross-refs RICanonicalDatabase I174."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taken after killing Xu Decai from Green Devil Continent (Ch. 1867)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Lantern (origin soul protection). User cannot die while fire is lit. Can nourish and slowly strengthen the origin soul. Refined into body: soft light surrounds body for extra protection layer. Taken after killing Xu Decai (Ch. 1867).",
                List.of("User cannot die while fire is lit",
                        "Nourishes and slowly strengthens the origin soul",
                        "Refined into body for extra protection layer",
                        "Taken after killing Xu Decai (Ch. 1867)"),
                List.of("lantern", "soul_protection", "xu_decai", "wang_lin_owned", "refined_into_body")
        ));

        register(CanonicalEntry.of(
                "I175_heavenly_bull_bead", "Heavenly Bull Bead", "天牛珠",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Heavenly Bull Continent era"), 5,
                        "Cross-refs RICanonicalDatabase I175."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Heavenly Bull Continent era acquisition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Heavenly Bull Bead. Bead from Heavenly Bull Continent. Spirit-tier.",
                List.of("Bead from Heavenly Bull Continent",
                        "Spirit-tier"),
                List.of("bead", "heavenly_bull", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I176_tortoise_shell", "A Tortoise Shell (Eastern Continent map)", "龟壳",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Eastern Continent era"), 5,
                        "Cross-refs RICanonicalDatabase I176."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Eastern Continent era acquisition."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tortoise Shell. Contains entire map of Eastern Continent. Less detailed further from Heavenly Bull Continent.",
                List.of("Contains entire map of Eastern Continent",
                        "Less detailed further from Heavenly Bull Continent"),
                List.of("shell", "tortoise", "eastern_continent", "map", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I177_palm_ice_dong_ling_pool", "Palm-Sized Piece of Ice (Dong Ling Pool ice, Ye Mo's left arm)", "东灵池冰·含夜魔左臂",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1768"), 5,
                        "Cross-refs RICanonicalDatabase I177."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Imitation Dong Ling Pool created by Seven Coloured Celestial Sovereign (Ch. 1768). Quasi-Third-Step tier."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Palm-Sized Piece of Ice. Ice from imitation Dong Ling Pool in cave world. Contains Ye Mo's left arm — inheritance bearing. Quasi-Third-Step tier.",
                List.of("Ice from imitation Dong Ling Pool in cave world",
                        "Contains Ye Mo's left arm — inheritance bearing",
                        "Quasi-Third-Step tier"),
                List.of("ice", "dong_ling_pool", "ye_mo", "inheritance", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "I178_golden_print_xu_decai", "Golden Print of Xu Decai's Rapid Spell Art", "金印·徐德才速法",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1772 (internalized)"), 5,
                        "Cross-refs RICanonicalDatabase I178."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.contractedInheritance("Taken after killing Xu Decai from Green Devil Continent (Ch. 1867). Technique internalized Ch. 1772."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Golden Print of Xu Decai's Rapid Spell Art. Rapid Spell Art — opens 9 spell veins in body. Each vein greatly increases casting speed. Uses sealing-based method to accelerate spell casting. Technique internalized.",
                List.of("Rapid Spell Art — opens 9 spell veins in body",
                        "Each vein greatly increases casting speed",
                        "Uses sealing-based method to accelerate spell casting",
                        "Technique internalized"),
                List.of("print", "golden", "xu_decai", "rapid_spell_art", "wang_lin_owned")
        ));

        // ── Sword Sheaths (5 individual entries, per user priority) ──
        register(CanonicalEntry.of(
                "sword_sheath_1st", "Sword Sheath (1st)", "剑鞘·第一",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("Heng Yue Sect era"), 5,
                        "1st sheath from the new evil master of a former friend at Heng Yue Sect."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("1st of 5 sword sheaths; mysterious origin — Wang Lin attests to function, not creation."),
                Demonstrability.ATTEST_ONLY,
                "1st of Wang Lin's 5 sword sheaths. Inserting a flying sword enhances different powers. Suspected Immortal World objects. 1st was from the new evil master of a former friend at Heng Yue Sect.",
                List.of("1st of 5 sword sheaths",
                        "Inserting a flying sword enhances different powers",
                        "From the new evil master of a former friend at Heng Yue Sect"),
                List.of("sword", "sheath", "1st", "mysterious_origin", "heng_yue_sect", "enhancer")
        ));

        register(CanonicalEntry.of(
                "sword_sheath_2nd", "Sword Sheath (2nd)", "剑鞘·第二",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5,
                        "2nd sheath collected across journey."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("2nd of 5 sword sheaths; mysterious origin."),
                Demonstrability.ATTEST_ONLY,
                "2nd of Wang Lin's 5 sword sheaths. Inserting a flying sword enhances different powers. Suspected Immortal World objects.",
                List.of("2nd of 5 sword sheaths",
                        "Inserting a flying sword enhances different powers",
                        "Suspected Immortal World objects"),
                List.of("sword", "sheath", "2nd", "mysterious_origin", "enhancer")
        ));

        register(CanonicalEntry.of(
                "sword_sheath_3rd", "Sword Sheath (3rd, suspected Sub-Void-Nirvana Sword)", "剑鞘·第三",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5,
                        "3rd sheath suspected to be a Sub-Void-Nirvana Sword."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("3rd of 5 sword sheaths; suspected to be a Sub-Void-Nirvana Sword."),
                Demonstrability.ATTEST_ONLY,
                "3rd of Wang Lin's 5 sword sheaths. Suspected to be a Sub-Void-Nirvana Sword. Inserting a flying sword enhances different powers.",
                List.of("3rd of 5 sword sheaths",
                        "Suspected to be a Sub-Void-Nirvana Sword",
                        "Inserting a flying sword enhances different powers"),
                List.of("sword", "sheath", "3rd", "sub_void_nirvana", "mysterious_origin", "enhancer")
        ));

        register(CanonicalEntry.of(
                "sword_sheath_4th", "Sword Sheath (4th)", "剑鞘·第四",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5,
                        "4th sheath collected across journey."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("4th of 5 sword sheaths; mysterious origin."),
                Demonstrability.ATTEST_ONLY,
                "4th of Wang Lin's 5 sword sheaths. Inserting a flying sword enhances different powers. Suspected Immortal World objects.",
                List.of("4th of 5 sword sheaths",
                        "Inserting a flying sword enhances different powers",
                        "Suspected Immortal World objects"),
                List.of("sword", "sheath", "4th", "mysterious_origin", "enhancer")
        ));

        register(CanonicalEntry.of(
                "sword_sheath_5th_tuo_sen", "Sword Sheath (5th, once in Tuo Sen's possession)", "剑鞘·第五",
                CanonicalCategory.STORAGE_TREASURES,
                Provenance.explicit("Renegade Immortal", List.of("early-mid era"), 5,
                        "5th sheath once appeared in Tuo Sen's possession."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.uniquelyBound("5th of 5 sword sheaths; once appeared in Tuo Sen's possession."),
                Demonstrability.ATTEST_ONLY,
                "5th of Wang Lin's 5 sword sheaths. Once appeared in Tuo Sen's possession. Inserting a flying sword enhances different powers.",
                List.of("5th of 5 sword sheaths",
                        "Once appeared in Tuo Sen's possession",
                        "Inserting a flying sword enhances different powers"),
                List.of("sword", "sheath", "5th", "tuo_sen", "mysterious_origin", "enhancer")
        ));

        // ── Domains as inventory (I138–I143) ─────────────────────────
        register(CanonicalEntry.of(
                "I138_life_death_domain_underworld_river", "Life-Death Domain / Underworld River", "生死域·冥河",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 264 (domain)", "Ch. 604 (Underworld River)"), 5,
                        "Cross-refs RICanonicalDatabase I138. First self-created Original Spell."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Catalyst to bind souls into Celestial Sealing Stamp."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Life-Death Domain / Underworld River. First self-created Original Spell. Life-and-death energy river. Catalyst to bind souls into Celestial Sealing Stamp → 18-Layers-of-Hell Reincarnation Realm. Virtual Origin tier.",
                List.of("First self-created Original Spell",
                        "Life-and-death energy river",
                        "Catalyst for Celestial Sealing Stamp soul-binding",
                        "Virtual Origin tier"),
                List.of("domain", "life_death", "underworld_river", "wang_lin_created", "virtual_origin")
        ));

        register(CanonicalEntry.of(
                "I139_karma_domain", "Karma Domain / Karmic Cycle", "因果域",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 731 (Karma Whip)", "Ch. 850 (Domain)"), 5,
                        "Cross-refs RICanonicalDatabase I139."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Evolved from Life-Death Domain."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Karma Domain / Karmic Cycle. Left eye yang, right eye yin — Karma Domain manifestation. Pairs with Karma Whip. Virtual Origin tier.",
                List.of("Left eye yang, right eye yin — Karma Domain manifestation",
                        "Pairs with Karma Whip",
                        "Evolved from Life-Death Domain",
                        "Virtual Origin tier"),
                List.of("domain", "karma", "karmic_cycle", "wang_lin_created", "virtual_origin")
        ));

        register(CanonicalEntry.of(
                "I140_true_false_domain", "True-False Domain", "真假域",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1102 (stabilized)", "Ch. 1163 (refined)"), 5,
                        "Cross-refs RICanonicalDatabase I140."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Evolved from Karma Domain."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "True-False Domain. Right eye lightning, left eye true/false — targets enter state between real and fake. Hallucinations make targets unable to distinguish real from fake. Controls Real and Unreal. Virtual Origin tier.",
                List.of("Right eye lightning, left eye true/false",
                        "Targets enter state between real and fake",
                        "Hallucinations make targets unable to distinguish real from fake",
                        "Controls Real and Unreal",
                        "Virtual Origin tier"),
                List.of("domain", "true_false", "wang_lin_created", "virtual_origin", "hallucination")
        ));

        register(CanonicalEntry.of(
                "I141_battle_will_domain", "Battle Will Domain", "战斗意志域",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1206"), 4,
                        "Cross-refs RICanonicalDatabase I141."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended (mid era). Anchored by Zhan Family Battle Scrolls + Zhan Xing."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Battle Will Domain. Gathers battle will as a weapon. Will-based domain. Anchored by the Zhan Family Battle Scrolls.",
                List.of("Gathers battle will as a weapon",
                        "Will-based domain",
                        "Anchored by Zhan Family Battle Scrolls"),
                List.of("domain", "battle_will", "wang_lin_created", "zhan_family")
        ));

        register(CanonicalEntry.of(
                "I142_star_of_law", "Star of Law", "法之星",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1220", "Ch. 1221 (sealed)"), 5,
                        "Cross-refs RICanonicalDatabase I142."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-fused (Ch. 1221)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Star of Law. Fusion of Life-Death, Karma domains + Fire and Thunder marks. Composite Origin tier.",
                List.of("Fusion of Life-Death + Karma domains",
                        "Fire and Thunder marks",
                        "Composite Origin tier"),
                List.of("domain", "star_of_law", "composite_origin", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "I143_18_layers_hell_reincarnation_realm", "18-Layers-of-Hell Reincarnation Realm", "十八层地狱轮回境",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 915"), 5,
                        "Cross-refs RICanonicalDatabase I143."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created (Ch. 915)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "18-Layers-of-Hell Reincarnation Realm. Wang Lin's own reincarnation cycle — pocket realm. Stores souls for Scatter Beans to Form Soldiers divine ability. Composite of Life-Death Dao Underworld River + Celestial Sealing Stamp.",
                List.of("Wang Lin's own reincarnation cycle — pocket realm",
                        "Stores souls for Scatter Beans to Form Soldiers",
                        "Composite of Life-Death Dao Underworld River + Celestial Sealing Stamp"),
                List.of("domain", "reincarnation", "18_hell", "pocket_realm", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "I05_five_sword_sheaths", "Five Sword Sheaths", "五把剑鞘",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~100-300"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The five sheaths that hold Wang Lin's signature flying swords. Each sheath has formation patterns.",
                List.of(
                        "Holds flying swords",
                        "Formation patterns carved on surface"
                ),
                List.of("sword", "sheath", "formation", "storage")
        ));


        register(CanonicalEntry.of(
                "I37_bronze_mirror", "Bronze Mirror / Time Domain", "铜镜/时间领域",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~800-1200"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An ancient mirror artifact that can create time-domain effects. Used by Wang Lin in mid-to-late stages.",
                List.of(
                        "Creates time-domain effects",
                        "Ancient artifact of unknown origin"
                ),
                List.of("time", "domain", "artifact", "mirror")
        ));


        register(CanonicalEntry.of(
                "I49_soul_flag_soul_refining_sect", "Soul Flag (Soul Refining Sect)", "魂幡（炼魂宗）",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("early Nascent Soul"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A soul banner from the Soul Refining Sect. One of Wang Lin's early soul-type artifacts. Evolved into the Billion Soul Flag.",
                List.of(
                        "Captures souls of the slain",
                        "Soul Refining Sect standard equipment"
                ),
                List.of("soul", "flag", "refining", "sect")
        ));


        register(CanonicalEntry.of(
                "I82_seven_colored_nails", "Seven-Colored Nail (set)", "七彩钉",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~600-1082"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Set of nails forged from Seven-Colored Daoist's essence. Used as sealing weapons. Destroyed in battle, later restored by Wang Lin at Heaven Trampling.",
                List.of(
                        "Sealing/striking weapon",
                        "Seven-Colored Daoist's essence",
                        "Destroyed then restored at Heaven Trampling (Ch.1082→1626)"
                ),
                List.of("seven_colored", "seal", "weapon", "restoration")
        ));


        register(CanonicalEntry.of(
                "I76_celestial_spirit_clock", "Celestial Spirit Clock", "仙灵钟",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("mid-to-late RI"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A clock-type artifact with celestial spiritual properties. Used for temporal sensing.",
                List.of(
                        "Temporal sensing",
                        "Celestial spiritual properties"
                ),
                List.of("time", "celestial", "clock", "sensing")
        ));


        register(CanonicalEntry.of(
                "I90_dot_immortal_pen", "Dot Immortal Pen", "点仙笔",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Restriction era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A pen artifact used to draw restrictions and formations. Wang Lin used it for restriction art.",
                List.of(
                        "Draws restrictions and formations",
                        "Restriction-art implement"
                ),
                List.of("restriction", "formation", "pen", "tool")
        ));


        register(CanonicalEntry.of(
                "I92_golden_print", "Golden Print", "金印",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A golden seal/print artifact. Used for authority and sealing purposes.",
                List.of(
                        "Sealing authority",
                        "Golden spiritual pressure"
                ),
                List.of("seal", "authority", "golden")
        ));


        register(CanonicalEntry.of(
                "I115_beast_skin_tattoo", "Beast Skin Tattoo", "兽皮刺青",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Core Formation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A tattoo carved on beast skin. Contains formation/restriction patterns for body enhancement.",
                List.of(
                        "Body enhancement patterns",
                        "Restriction-based tattoo art"
                ),
                List.of("tattoo", "formation", "body", "beast")
        ));


        register(CanonicalEntry.of(
                "I03_three_ink_stones", "Three Ink Stones", "三方墨石",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~180"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The three Ink Stones required for refining Restriction Flags. Obtained from the Land of the Ancient God trial.",
                List.of(
                        "Essential reagent for Restriction Flag refining",
                        "From the Land of the Ancient God"
                ),
                List.of("inkstone", "restriction", "ancient_god", "reagent")
        ));


        register(CanonicalEntry.of(
                "I100_infant_skull_da_yi", "Infant Skull (Da Yi)", "大妖婴儿头骨",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A skull from the infant Da Yi. Used as a cultivation/sealing component.",
                List.of(
                        "Sealing component",
                        "Da Yi's remains"
                ),
                List.of("skull", "sealing", "da_yi", "remains")
        ));


        register(CanonicalEntry.of(
                "I65_three_battle_scrolls", "Three Battle Scrolls", "战字三卷",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Three scrolls containing battle techniques. Part of Wang Lin's tactical arsenal.",
                List.of(
                        "Battle technique scrolls",
                        "Tactical combat arts"
                ),
                List.of("scroll", "battle", "technique", "tactics")
        ));


        register(CanonicalEntry.of(
                "I40_human_shaped_armor", "Human-Shaped Armor (Green Bull)", "人形铠甲（青牛）",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Soul Formation era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Armor shaped like a human figure, related to the Green Bull. Provides physical + spiritual defense.",
                List.of(
                        "Human-shaped defensive armor",
                        "Green Bull origin"
                ),
                List.of("armor", "defense", "green_bull")
        ));


        register(CanonicalEntry.of(
                "I160_dream_dao_mirror", "Dream Dao Mirror", "梦道镜",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A mirror related to the Dream Dao — one of Wang Lin's Original Spells. Reflects dream-based illusions.",
                List.of(
                        "Dream Dao implementation",
                        "Illusion reflection"
                ),
                List.of("dream_dao", "mirror", "illusion", "original_spell")
        ));


        register(CanonicalEntry.of(
                "AT01_red_lightning_ji_realm", "1st Accompanying Thunder (Ji Thunder)", "第一道伴雷（极雷）",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1368"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The first of Wang Lin's 9 accompanying thunders — formed from his Ji Realm divine sense. Red lightning.",
                List.of(
                        "1st of 9 accompanying thunders",
                        "Formed from Ji Realm divine sense",
                        "Red lightning attribute"
                ),
                List.of("thunder", "accompanying", "ji_realm", "lightning")
        ));


        register(CanonicalEntry.of(
                "I60_blood_nascent_soul_seven_colored", "Blood Red Nascent Soul (Seven-Colored)", "血色元婴（七彩）",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's blood-red Nascent Soul, infused with Seven-Colored Daoist's essence.",
                List.of(
                        "Blood-red Nascent Soul",
                        "Seven-Colored infusion"
                ),
                List.of("nascent_soul", "blood", "seven_colored")
        ));


        register(CanonicalEntry.of(
                "I167_ancient_bloodline_ancestor", "Ancient Bloodline Ancestor", "远古血脉之祖",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("late RI"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Ancestral bloodline power from the Ancient God lineage. Provides ancient heritage bonuses.",
                List.of(
                        "Ancient God bloodline heritage",
                        "Ancestral power source"
                ),
                List.of("bloodline", "ancient_god", "ancestor", "heritage")
        ));


        register(CanonicalEntry.of(
                "I168_celestial_bloodline_ancestor", "Celestial Bloodline Ancestor", "仙界血脉之祖",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial-tier bloodline ancestor power. Beyond mortal bloodline heritage.",
                List.of(
                        "Celestial bloodline heritage",
                        "Transcendent power source"
                ),
                List.of("bloodline", "celestial", "ancestor", "transcendent")
        ));


        register(CanonicalEntry.of(
                "I169_vermilion_bird_spirit", "Vermilion Bird Spirit", "朱雀之灵",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("throughout RI"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The spirit essence of the Vermilion Bird. Used for fire-based abilities and Vermilion Bird Mark.",
                List.of(
                        "Fire-based abilities",
                        "Vermilion Bird Mark power source"
                ),
                List.of("vermilion_bird", "spirit", "fire", "mark")
        ));


        register(CanonicalEntry.of(
                "I170_call_wind_summon_rain", "Call Wind Summon Rain", "呼风唤雨",
                CanonicalCategory.INVENTORY,
                Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"), 3),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin obtained this item during his cultivation journey."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A magical item/technique for weather manipulation — calling wind and summoning rain.",
                List.of(
                        "Weather manipulation",
                        "Wind and rain control"
                ),
                List.of("weather", "wind", "rain", "elemental")
        ));

    }
}
