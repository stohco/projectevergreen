package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalEssences — the 14 Essences of Wang Lin's Samsara Dao.
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.SamsaraDao.Essence}. Each entry
 * carries Wang Lin's completion chapter and a canon-grounded transferability
 * verdict. The 14th Essence (Reincarnation) is the one whose comprehension
 * triggers Heaven Trampling — it is UNIQUELY_BOUND to Wang Lin's path.
 *
 * <p>Taxonomy:
 * <ul>
 *   <li>6 substantial (Five Elements + Thunder) — each forms a True Body</li>
 *   <li>4 virtual (Life-Death, Karma, True-False, Reincarnation)</li>
 *   <li>4 special (Primordial/Taichu, Silent Extinction/Miemie, Restriction, Slaughter)</li>
 * </ul>
 */
public final class CanonicalEssences extends AbstractSubRegistry {

    public CanonicalEssences() {
        super("CanonicalEssences");
    }

    @Override
    protected void doBootstrap() {
        // ── 6 substantial ────────────────────────────────────────────
        register(CanonicalEntry.of(
                "essence_thunder", "Thunder Essence", "雷源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 127"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's FIRST essence (Ch. 127). Heart-attribute thunder fuses with fire essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's FIRST essence in completion order (Ch. 127). Heart-attribute thunder fuses with fire essence (Heart-Pounding Thunder). Forms the Thunder True Body. Pairs with Slaughter Origin → Annihilating Thunder.",
                List.of("Wang Lin's first Essence (Ch. 127)",
                        "Heart-attribute thunder",
                        "Fuses with Fire Essence → Heart-Pounding Thunder",
                        "Pairs with Slaughter → Annihilating Thunder"),
                List.of("essence", "substantial", "thunder", "heart_pounding_thunder", "wang_lin_first")
        ));

        register(CanonicalEntry.of(
                "essence_fire", "Fire Essence", "火源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 296"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Awakened at the Fire Spirit planet. Wang Lin's 2nd essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 2nd essence in completion order (Ch. 296). Awakened at the Fire Spirit planet. Fuels the Fire Essence True Body. Heart-Pounding Thunder fuses fire+thunder.",
                List.of("Awakened at Fire Spirit planet",
                        "Fuses with Thunder → Heart-Pounding Thunder"),
                List.of("essence", "substantial", "fire", "wang_lin_created", "heart_pounding_thunder")
        ));

        register(CanonicalEntry.of(
                "essence_water", "Water Essence", "水源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1720 (potential)", "Ch. 1843 (complete)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Comprehended via 380 million changes at Planet Five Elements; completed 9th cycle at Pill Sea."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 7th essence (Ch. 1720 potential / 1843 complete). Comprehended via 380 million changes at Planet Five Elements; completed 9th cycle at Pill Sea. Forms the Water True Body. The founder of Great Soul Sect gave Wang Lin a Drop of Crystal Clear Water essence to help.",
                List.of("Comprehended via 380 million changes at Planet Five Elements",
                        "Completed 9th cycle at Pill Sea",
                        "Forms Water True Body"),
                List.of("essence", "substantial", "water", "planet_five_elements", "pill_sea", "great_soul_sect")
        ));

        register(CanonicalEntry.of(
                "essence_metal", "Metal Essence", "金源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("post-Water"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Obtained at the Metal Spirit planet. Tied to Eyes Suppressing the World (Ch. 1997 completion)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 8th essence. Obtained at the Metal Spirit planet. Forms the Metal True Body. Tied to the third fragment of the Celestial Ancestor's Sword (Eyes Suppressing the World, Ch. 1997).",
                List.of("Obtained at Metal Spirit planet",
                        "Forms Metal True Body",
                        "Tied to Eyes Suppressing the World (Ch. 1997)"),
                List.of("essence", "substantial", "metal", "celestial_ancestor", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "essence_wood", "Wood Essence", "木源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("post-Metal"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Obtained via the Wood Spirit inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 9th essence. Obtained via the Wood Spirit inheritance. Forms the Wood True Body.",
                List.of("Obtained via Wood Spirit inheritance",
                        "Forms Wood True Body"),
                List.of("essence", "substantial", "wood", "wood_spirit", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "essence_earth", "Earth Essence", "土源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("post-Wood"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Obtained via the Earth Spirit inheritance."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 10th essence. Obtained via the Earth Spirit inheritance. Forms the Earth True Body.",
                List.of("Obtained via Earth Spirit inheritance",
                        "Forms Earth True Body"),
                List.of("essence", "substantial", "earth", "earth_spirit", "wang_lin_created")
        ));

        // ── 4 virtual ────────────────────────────────────────────────
        register(CanonicalEntry.of(
                "essence_karma", "Karma Essence (Cause-Effect)", "因果源",
                CanonicalCategory.KARMA,
                Provenance.explicit("Renegade Immortal", List.of("3rd essence in completion order"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's 3rd essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 3rd essence in completion order. The law of karmic consequence. Lets him see karmic threads and manipulate them. Anchors the Karma Domain and Karma Whip.",
                List.of("Lets him see karmic threads",
                        "Anchors the Karma Domain and Karma Whip"),
                List.of("essence", "virtual", "karma", "wang_lin_created", "karma_whip", "karma_domain")
        ));

        register(CanonicalEntry.of(
                "essence_life_death", "Life-Death Essence", "生死源",
                CanonicalCategory.LIFE_AND_DEATH,
                Provenance.explicit("Renegade Immortal", List.of("4th essence in completion order"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Comprehended via Wang Lin's Domain (living as a mortal among mortals)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 4th essence. The cycle of mortality. Comprehended via Wang Lin's Domain (living as a mortal among mortals). Anchors the Life-Death Domain / Underworld River.",
                List.of("Comprehended by living as a mortal",
                        "Anchors Life-Death Domain / Underworld River"),
                List.of("essence", "virtual", "life_death", "underworld_river", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "essence_true_false", "True-False Essence", "真假源",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("5th essence"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's 5th essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 5th essence. The distinction between reality and illusion. Resonates with Wang Lin's right-eye truth-sight.",
                List.of("Controls Real and Unreal",
                        "Resonates with right-eye truth-sight"),
                List.of("essence", "virtual", "true_false", "wang_lin_created", "truth_sight")
        ));

        register(CanonicalEntry.of(
                "essence_reincarnation", "Reincarnation Essence (Samsara)", "轮回源",
                CanonicalCategory.SAMSARA,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1943 (potential)", "Ch. 2087 (completed)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's 14th and FINAL essence. Achieved at the Reincarnation Pool (13 years meditation). Comprehension = Heaven Trampling. Cannot be transferred — it IS Wang Lin's path."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 14th and final essence. Achieved at the Reincarnation Pool (13 years meditation). Completing this Essence = Wang Lin achieves Heaven Trampling the moment he masters it (Ch. 2087). The 14th bypasses the 9th Heaven Trampling Bridge.",
                List.of("14th and final essence",
                        "13 years of meditation at the Reincarnation Pool",
                        "Completing it = Heaven Trampling achieved (Ch. 2087)",
                        "Bypasses the 9th Heaven Trampling Bridge"),
                List.of("essence", "virtual", "reincarnation", "wang_lin_unique", "heaven_trampling", "reincarnation_pool")
        ));

        // ── 4 special ────────────────────────────────────────────────
        register(CanonicalEntry.of(
                "essence_primordial", "Primordial (Taichu) Essence", "太初源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("11th essence"), 5,
                        "Comprehended at the False/True Reincarnation Pool."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's 11th essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 11th essence. The origin of all things. Comprehended at the False/True Reincarnation Pool. Own True Body. Resonates with Sundered Night (1st Original Spell).",
                List.of("Origin of all things",
                        "Own True Body",
                        "Resonates with Sundered Night"),
                List.of("essence", "special", "primordial", "taichu", "wang_lin_created", "sundered_night")
        ));

        register(CanonicalEntry.of(
                "essence_silent_extinction", "Silent Extinction (Miemie) Essence", "灭绝源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("12th essence"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's 12th essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's 12th essence. The cessation of all things. The complement to Primordial. Own True Body. Pairs with Slaughter Origin → Lu Mo.",
                List.of("Cessation of all things",
                        "Complement to Primordial",
                        "Pairs with Slaughter → Lu Mo"),
                List.of("essence", "special", "silent_extinction", "miemie", "wang_lin_created", "lu_mo")
        ));

        register(CanonicalEntry.of(
                "essence_restriction", "Restriction (Sealing) Essence", "封印源",
                CanonicalCategory.RESTRICTIONS,
                Provenance.explicit("Renegade Immortal", List.of("13th essence"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's signature Dao. 13th essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's signature Dao. Restriction-essence is the 4th-Step-tier essence. Own True Body. Resonates with the Realm-Sealing Grand Array.",
                List.of("Wang Lin's signature Dao",
                        "4th-Step-tier essence",
                        "Own True Body",
                        "Resonates with the Realm-Sealing Grand Array"),
                List.of("essence", "special", "restriction", "wang_lin_signature", "realm_sealing_grand_array")
        ));

        register(CanonicalEntry.of(
                "essence_slaughter", "Slaughter Essence", "杀戮源",
                CanonicalCategory.SLAUGHTER,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1509 (initial)", "Ch. 1622 (complete)"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Wang Lin's first major Dao. 6th essence."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wang Lin's first major Dao. Comprehended through a lifetime of killing (initial: slaying Daoist Water; complete: consuming Qing Shui's Black Sword). Own True Body. Pairs with Silent Extinction → Lu Mo; with Thunder → Annihilating Thunder.",
                List.of("Wang Lin's first major Dao",
                        "Initial: slaying Daoist Water (Ch. 1509)",
                        "Complete: consuming Qing Shui's Black Sword (Ch. 1622)",
                        "Pairs with Silent Extinction → Lu Mo"),
                List.of("essence", "special", "slaughter", "wang_lin_created", "qing_shui", "daoist_water", "lu_mo")
        ));

        // ── Additional virtual Origin / Dao essences (per RICanonicalDatabase E12, E13, E14) ──
        register(CanonicalEntry.of(
                "essence_dao", "Dao Essence", "道之源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 4,
                        "Cross-refs RICanonicalDatabase E12. 1 of 14 Essences."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Dao Essence. 1 of the 14 Essences of Wang Lin's Samsara Dao system. The Dao-itself Origin — the meta-essence that lets Wang Lin perceive Dao itself.",
                List.of("1 of 14 Essences",
                        "Meta-essence — perceives Dao itself",
                        "Self-comprehended"),
                List.of("essence", "virtual", "dao", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "essence_primordial_silent_extinction_pair", "Primordial/Silent Extinction Essence Pair", "太初/寂灭之源",
                CanonicalCategory.ESSENCES,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5,
                        "Cross-refs RICanonicalDatabase E13."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Primordial/Silent Extinction Essence Pair. The complement-pair (Taichu + Miemie) — the beginning and the cessation of all things. The pair is one of the 4 special Essences.",
                List.of("The complement-pair (Taichu + Miemie)",
                        "Beginning and cessation of all things",
                        "1 of the 4 special Essences"),
                List.of("essence", "special", "primordial_silent_extinction", "pair", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "essence_cause_effect", "Cause-and-Effect Essence (alias for Karma Essence)", "因果之源",
                CanonicalCategory.KARMA,
                Provenance.explicit("Renegade Immortal", List.of("3rd essence in completion order"), 5,
                        "Cross-refs RICanonicalDatabase E14. Alias for essence_karma."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended. Same as essence_karma — alias registered for cross-DB searchability."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Alias for Karma Essence (essence_karma). Registered to ensure the cause-effect (因果) search term resolves. Same entity.",
                List.of("Alias for essence_karma",
                        "Cause-and-Effect Origin",
                        "Same entity — alternative id"),
                List.of("essence", "virtual", "karma", "cause_effect", "alias")
        ));

        // ── Battle Will Domain / Star of Law as essence-tier entries ──
        register(CanonicalEntry.of(
                "essence_battle_will_domain", "Battle Will Domain (essence-tier)", "战斗意志域",
                CanonicalCategory.DOMAINS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1206"), 4,
                        "Cross-refs RICanonicalDatabase I141."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-comprehended (mid era)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Battle Will Domain. Gathers battle will as a weapon. Will-based domain. Anchored by the Zhan Family Battle Scrolls.",
                List.of("Gathers battle will as a weapon",
                        "Will-based domain",
                        "Anchored by Zhan Family Battle Scrolls"),
                List.of("domain", "battle_will", "essence_tier", "wang_lin_created", "zhan_family")
        ));

        register(CanonicalEntry.of(
                "essence_star_of_law", "Star of Law (essence-tier)", "法之星",
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
                List.of("domain", "star_of_law", "essence_tier", "wang_lin_created", "composite_origin")
        ));
    }
}
