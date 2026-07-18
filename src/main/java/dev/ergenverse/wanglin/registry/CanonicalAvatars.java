package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalAvatars — Wang Lin's clones & true bodies.
 *
 * <p>Cross-refs CANON_RI_COMPLETE_ITEMS.md §11 (12 avatars/clones/true bodies).
 * Includes the Cultivator Clone (Divine Path), Celestial Body, Thunder Body,
 * Ancient Demon, Ancient Devil, Void Avatar, Five Elements True Body, Lu Mo
 * (Slaughter True Body), Annihilating Thunder, and the four special-origin
 * True Bodies.
 */
public final class CanonicalAvatars extends AbstractSubRegistry {

    public CanonicalAvatars() {
        super("CanonicalAvatars");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "cultivator_clone", "Cultivator Clone (Human-Celestial Cultivating Dao/Qi)", "修仙者分身·人仙",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 211"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Created via Divine Path technique. Fused back with Main Body after Immortal Ancestor hair strand."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Created via Divine Path technique. Avatar helps breakthrough to Nascent Soul. Drawback: avatar has no cultivation and can only live 30 years. Wang Lin broke the lifespan drawback by fusing Avatar back into Main Body — the Ji Realm conflict with the Avatar removed the drawback.",
                List.of("Created via War God Shrine's Divine Path",
                        "30-year lifespan (originally)",
                        "Lifespan drawback removed via Ji Realm conflict"),
                List.of("avatar", "divine_path", "ji_realm", "war_god_shrine", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "celestial_body_mid", "Celestial Body (Mid Quality)", "天身·中品",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 424"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-created. Former — fused/discarded."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Celestial-energy cultivating avatar. Fused back into Main Body.",
                List.of("Celestial-energy cultivator",
                        "Fused back into Main Body"),
                List.of("avatar", "celestial", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "thunder_body", "Thunder Body", "雷身",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 719"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.selfCreated("Self-created. Former — fused back."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder-element avatar. Fused back into Main Body.",
                List.of("Thunder-element avatar",
                        "Fused back into Main Body"),
                List.of("avatar", "thunder", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "immortal_celestial_body", "Immortal Celestial Body", "仙天身",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1538"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created post-Celestial-Vein fusion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fused celestial and immortal energy avatar.",
                List.of("Celestial + immortal hybrid",
                        "Post-Celestial-Vein fusion"),
                List.of("avatar", "celestial", "immortal", "wang_lin_created")
        ));

        register(CanonicalEntry.of(
                "ancient_demon_clone", "Ancient Demon (clone)", "古魔",
                CanonicalCategory.ANCIENT_DEMON,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1002"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("From statue in Land of Demonic Spirits."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A statue in the Land of Demonic Spirits. Cultivates demonic energy. Fused back with Main Body to become an Ancient One. The Fog Devil Lance was given to this clone.",
                List.of("From statue in Land of Demonic Spirits",
                        "Fused back with Main Body to become Ancient One",
                        "Wielded the Fog Devil Lance"),
                List.of("avatar", "ancient_demon", "demonic", "ancient_one", "inheritance", "fog_devil_lance")
        ));

        register(CanonicalEntry.of(
                "ancient_devil_clone", "Ancient Devil (clone)", "古妖",
                CanonicalCategory.ANCIENT_DEVIL,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1000"), 5),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("From corpse in Daogu Yemo's tomb."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "A corpse from the tomb of Daogu Yemo. Cultivates devilish energy. Fused back with Main Body to become an Ancient One.",
                List.of("From corpse in Daogu Yemo's tomb",
                        "Fused back with Main Body to become Ancient One"),
                List.of("avatar", "ancient_devil", "devilish", "ancient_one", "inheritance", "daogu_yemo")
        ));

        register(CanonicalEntry.of(
                "void_avatar", "Otherworldly Void Avatar / Void Clone", "虚空分身",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1798"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-created (Void-Destiny inheritance)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Possesses special powers; completely fused during his battle with the Ancient Path, where he gained enlightenment about his own path. Possesses the same Void Destiny as the Immortal Ancestor and Ancient Ancestor.",
                List.of("Same Void Destiny as Immortal Ancestor / Ancient Ancestor",
                        "Fused during battle with Ancient Path"),
                List.of("avatar", "void", "void_destiny", "wang_lin_created", "ancient_path")
        ));

        register(CanonicalEntry.of(
                "five_elements_true_body", "Five Elements True Body", "五行真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed from Five Elements Origins."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Each of the Five Elements Origins (Metal, Wood, Water, Fire, Earth) has condensed a True Body; they can also fuse together to form a single Five Elements True Body.",
                List.of("Each element forms its own True Body",
                        "Five True Bodies can fuse into one"),
                List.of("true_body", "five_elements", "wang_lin_created", "origin", "fusion")
        ));

        register(CanonicalEntry.of(
                "lu_mo_slaughter_clone", "Slaughter True Body / Lu Mo", "杀戮真身·路摩",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Lu Mo is a special clone that achieved the Fourth Step. Self-fused from Slaughter Origin + Silent Extinction/Miemie Origin. Cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Killing Origin and the Silent Extinction Origin fuse to form Lu Mo. Wang Lin's strongest clone — achieved the Fourth Step. Lu Mo borrowed the Realm-Defining Compass from Old Man Miēshēng and blasted the Heaven-Defying Pearl open via Dream Dao, sending it back through time to the main body. Lu Mo left information in the White Hair Strand and Infant Skull to resolve the main body's confusion about reincarnation. Used Flowing Time to send Lu Mo back to the past to search for a method to resurrect Li Muwan.",
                List.of("Fourth-Step-achiever",
                        "Borrowed Realm-Defining Compass from Old Man Miēshēng",
                        "Blasted Heaven-Defying Pearl open via Dream Dao",
                        "Sent bead back through time to main body",
                        "Used Flowing Time to search for Li Muwan revival method"),
                List.of("avatar", "true_body", "slaughter", "silent_extinction", "lu_mo", "fourth_step", "wang_lin_unique", "time_travel", "li_muwan_revival")
        ));

        register(CanonicalEntry.of(
                "annihilating_thunder", "Annihilating Thunder", "灭雷",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-fused (Slaughter Origin + Thunder Origin)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The Killing Origin and the Thunder Origin fuse to form Annihilating Thunder.",
                List.of("Killing + Thunder fusion"),
                List.of("true_body", "slaughter", "thunder", "wang_lin_created", "fusion")
        ));

        register(CanonicalEntry.of(
                "taichu_origin_true_body", "Taichu Origin True Body", "太初真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "True Body formed from the Taichu (Beginning) Origin.",
                List.of("Formed from the Taichu Origin"),
                List.of("true_body", "taichu", "wang_lin_created", "origin")
        ));

        register(CanonicalEntry.of(
                "miemie_origin_true_body", "Miemie Origin True Body", "寂灭真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "True Body formed from the Miemie (Silent Extinction) Origin. Pairs with Slaughter Origin → Lu Mo.",
                List.of("Formed from the Miemie Origin",
                        "Pairs with Slaughter Origin → Lu Mo"),
                List.of("true_body", "miemie", "wang_lin_created", "origin", "lu_mo")
        ));

        register(CanonicalEntry.of(
                "restriction_origin_true_body", "Restriction Origin True Body", "禁制真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "True Body formed from the Restriction Origin.",
                List.of("Formed from the Restriction Origin"),
                List.of("true_body", "restriction", "wang_lin_created", "origin")
        ));

        register(CanonicalEntry.of(
                "thunder_origin_true_body", "Thunder Origin True Body", "雷霆真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("post-Origin-awakening"), 4),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "True Body formed from the Thunder Origin. Pairs with Slaughter Origin → Annihilating Thunder.",
                List.of("Formed from the Thunder Origin",
                        "Pairs with Slaughter Origin → Annihilating Thunder"),
                List.of("true_body", "thunder", "wang_lin_created", "origin", "annihilating_thunder")
        ));

        // ── Per-element Essence True Bodies (T145–T150) ──────────────
        register(CanonicalEntry.of(
                "fire_essence_true_body", "Fire Essence True Body", "火源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1825"), 5,
                        "Cross-refs RICanonicalDatabase T145. Consumed ~120 Fire Veins."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1825)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Fire Essence True Body. Self-condensed at Ch. 1825. Wang Lin consumed ~120 Fire Veins to condense it.",
                List.of("Self-condensed Ch. 1825",
                        "Consumed ~120 Fire Veins",
                        "Fire-element Origin True Body"),
                List.of("true_body", "fire", "essence_true_body", "wang_lin_created", "origin")
        ));

        register(CanonicalEntry.of(
                "thunder_essence_true_body", "Thunder Essence True Body", "雷源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1892"), 5,
                        "Cross-refs RICanonicalDatabase T146."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1892). Dao Devil Sect Master fed; Wang Lin seized."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Thunder Essence True Body. Self-condensed at Ch. 1892. Dao Devil Sect Master fed Wang Lin; Wang Lin seized the body.",
                List.of("Self-condensed Ch. 1892",
                        "Dao Devil Sect Master fed Wang Lin",
                        "Wang Lin seized the body"),
                List.of("true_body", "thunder", "essence_true_body", "wang_lin_created", "origin", "dao_devil_sect")
        ));

        register(CanonicalEntry.of(
                "earth_essence_true_body", "Earth Essence True Body", "土源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1895"), 5,
                        "Cross-refs RICanonicalDatabase T147. Ji Si used 3 Meng Earth Beads."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1895)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Earth Essence True Body. Self-condensed at Ch. 1895. Ji Si used 3 Meng Earth Beads.",
                List.of("Self-condensed Ch. 1895",
                        "Ji Si used 3 Meng Earth Beads",
                        "Earth-element Origin True Body"),
                List.of("true_body", "earth", "essence_true_body", "wang_lin_created", "origin", "ji_si")
        ));

        register(CanonicalEntry.of(
                "water_essence_true_body", "Water Essence True Body", "水源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1897"), 5,
                        "Cross-refs RICanonicalDatabase T148. Ji Si used Celestial Sea Mother Soul."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 1897)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Water Essence True Body. Self-condensed at Ch. 1897. Ji Si used Celestial Sea Mother Soul.",
                List.of("Self-condensed Ch. 1897",
                        "Ji Si used Celestial Sea Mother Soul",
                        "Water-element Origin True Body"),
                List.of("true_body", "water", "essence_true_body", "wang_lin_created", "origin", "ji_si")
        ));

        register(CanonicalEntry.of(
                "wood_essence_true_body", "Wood Essence True Body", "木源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2017"), 5,
                        "Cross-refs RICanonicalDatabase T149. Ji Du pure Wood Essence liquid."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 2017)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Wood Essence True Body. Self-condensed at Ch. 2017. Ji Du pure Wood Essence liquid.",
                List.of("Self-condensed Ch. 2017",
                        "Ji Du pure Wood Essence liquid",
                        "Wood-element Origin True Body"),
                List.of("true_body", "wood", "essence_true_body", "wang_lin_created", "origin", "ji_du")
        ));

        register(CanonicalEntry.of(
                "metal_essence_true_body", "Metal Essence True Body", "金源真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2024"), 5,
                        "Cross-refs RICanonicalDatabase T150. Celestial Ancestor Sword 3rd fragment."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (Ch. 2024)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Metal Essence True Body. Self-condensed at Ch. 2024. Celestial Ancestor Sword 3rd fragment.",
                List.of("Self-condensed Ch. 2024",
                        "Celestial Ancestor Sword 3rd fragment",
                        "Metal-element Origin True Body"),
                List.of("true_body", "metal", "essence_true_body", "wang_lin_created", "origin", "celestial_ancestor")
        ));

        register(CanonicalEntry.of(
                "slaughter_essence_true_body", "Slaughter Essence True Body", "杀戮真身",
                CanonicalCategory.BODIES,
                Provenance.explicit("Renegade Immortal", List.of("after Ch. 1900"), 5,
                        "Cross-refs RICanonicalDatabase T144. Fuses into Punishment Slaughter."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.selfCreated("Self-condensed (after Ch. 1900)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Slaughter Essence True Body. Self-condensed after Ch. 1900. Fuses into Punishment Slaughter.",
                List.of("Self-condensed after Ch. 1900",
                        "Fuses into Punishment Slaughter",
                        "Slaughter-element Origin True Body"),
                List.of("true_body", "slaughter", "essence_true_body", "wang_lin_created", "origin", "punishment_slaughter")
        ));

        register(CanonicalEntry.of(
                "punishment_slaughter", "Punishment Slaughter (final fused avatar)", "刑戮",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~2065"), 5,
                        "Cross-refs RICanonicalDatabase T151. Final fused avatar."),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin's final fused avatar — Punishment Slaughter. Cannot be duplicated."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Punishment Slaughter. The final fused avatar formed around Ch. 2065. Combines all the Essence True Bodies into a single avatar.",
                List.of("Final fused avatar (Ch. ~2065)",
                        "Combines all Essence True Bodies",
                        "Single avatar of all Essences"),
                List.of("avatar", "true_body", "punishment_slaughter", "fusion", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "divine_path_clone", "Divine Path Clone (Cultivator Clone, alternative name)", "神道分身",
                CanonicalCategory.AVATARS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 129", "Ch. 211"), 5,
                        "Cross-refs RICanonicalDatabase T129 / I124."),
                OwnershipState.FORMERLY_OWNED,
                Transferability.contractedInheritance("War God Shrine transmission (Ch. 129)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Alternative name for the Cultivator Clone created via the Divine Path (Ch. 211). Same entity as cultivator_clone — duplicate registered to ensure the Divine Path alias is searchable.",
                List.of("Alias for Cultivator Clone",
                        "Created via War God Shrine Divine Path",
                        "Ch. 211"),
                List.of("avatar", "divine_path", "cultivator_clone", "war_god_shrine", "alias")
        ));
    }
}
