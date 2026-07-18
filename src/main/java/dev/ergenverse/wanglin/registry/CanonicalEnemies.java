package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalEnemies — Wang Lin's antagonists.
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.WangLinAntagonists} (the 3-layer
 * antagonist structure). The entries here surface the canonical personality /
 * relationship aspects of each enemy.
 */
public final class CanonicalEnemies extends AbstractSubRegistry {

    public CanonicalEnemies() {
        super("CanonicalEnemies");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "enemy_all_seer", "All-Seer (False Mentor)", "全知者",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Planet Tian Yun era"), 5),
                OwnershipState.ENCOUNTERED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Peak Third Step patriarch of Heavenly Fate Sect. Mortal-realm schemer; false mentor. Killed by Wang Lin."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mortal-realm schemer ruling the Heavenly Fate Sect; false mentor to Wang Lin. Plotted to absorb source origins of Wang Lin, Ling Tianhou, and Blood Ancestor. Taught Wang Lin the Celestial Slaughter Art with a trap inside; killed by Wang Lin.",
                List.of("False mentor (celestial cultivation induction)",
                        "Plotted to absorb source origins",
                        "Embedded trap in Celestial Slaughter Art",
                        "Killed by Wang Lin (trap turned back)"),
                List.of("enemy", "all_seer", "heavenly_fate_sect", "false_mentor", "wang_lin_killed")
        ));

        register(CanonicalEntry.of(
                "enemy_seven_colored_daoist", "Seven-Colored Daoist (Cave World Owner)", "七彩道人",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Cave World arc"), 5),
                OwnershipState.ENCOUNTERED,
                new Transferability(
                        Transferability.CanTeach.REFUSES,
                        Demonstrability.CAN_DEMONSTRATE_FULLY,
                        Transferability.CanExplain.YES_FULLY,
                        Transferability.CanTransfer.UNIQUELY_BOUND,
                        Transferability.CanGiftEquivalent.UNIQUELY_BOUND,
                        Transferability.CanCreateNew.NO,
                        "Cave World owner-farmer. Cosmic-tier antagonist. Killed by Wang Lin."
                ),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The creator-owner of the Cave World. Built it as a farm to harvest Joss Flames (cultivation energy from mortal faith). The Cave World's law: Third-Step cultivators cannot naturally arise — the seal suppresses them. Killed by Wang Lin; Wang Lin became the new owner.",
                List.of("Cave World owner-farmer",
                        "Harvests Joss Flames from mortal faith",
                        "Imposed the cultivation ceiling on the Cave World",
                        "Killed by Wang Lin"),
                List.of("enemy", "seven_colored_daoist", "cave_world", "joss_flame", "wang_lin_killed")
        ));

        register(CanonicalEntry.of(
                "enemy_teng_huayuan", "Teng Huayuan", "藤化元",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("early era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Killed Wang Lin's parents; killed by Wang Lin."),
                Demonstrability.ATTEST_ONLY,
                "Annihilated the Wang Clan to seize Wang Lin's soul for divination. Killed Wang Lin's parents (Wang Tianshui, Zhou Tingsu); their souls were trapped in Teng's soul flag (rescued by Situ Nan into the Heaven-Defying Bead).",
                List.of("Annihilated the Wang Clan",
                        "Killed Wang Lin's parents",
                        "Trapped parents' souls in soul flag",
                        "Killed by Wang Lin"),
                List.of("enemy", "teng_huayuan", "wang_clan_annihilation", "wang_lin_killed", "mortal_realm")
        ));

        register(CanonicalEntry.of(
                "enemy_tianyunzi", "Tianyunzi", "天运子",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Planet Tian Yun era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("All-Seer's family/clan member. Killed by Wang Lin via Dream Dao."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The All-Seer's family/clan member. Wang Lin used Dream Dao to learn of Tianyunzi's 99 cycles of reincarnation, ultimately discovering the location of his true body's wheel and destroying Tian Yunzi.",
                List.of("All-Seer's family",
                        "99 cycles of reincarnation",
                        "Killed by Wang Lin via Dream Dao"),
                List.of("enemy", "tianyunzi", "all_seer", "dream_dao", "wang_lin_killed")
        ));

        register(CanonicalEntry.of(
                "enemy_daoist_water", "Daoist Water", "水道人",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Cloud Sea Star System"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Third-Step antagonist; destroyed many of Wang Lin's treasures. Killed by Wang Lin."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Third-Step antagonist. Destroyed many of Wang Lin's treasures (God-Slaying Sword Ch. 1273, Ancient God Trident Ch. 1277, Seven-Colored Nail Ch. 1082 — all later restored Ch. 1626 via Void Gate). Killed by Wang Lin in the Cloud Sea Star System; Wang Lin completed Slaughter Essence here.",
                List.of("Third-Step antagonist",
                        "Destroyed God-Slaying Sword, Ancient God Trident, Seven-Colored Nail",
                        "Killed by Wang Lin (Slaughter Essence completed)"),
                List.of("enemy", "daoist_water", "cloud_sea", "wang_lin_killed", "slaughter_essence")
        ));

        register(CanonicalEntry.of(
                "enemy_tuo_sen", "Tuo Sen", "拓森",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Land of the Ancient God"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Born from Tu Si's failed Ink Flow Split Soul Technique — inherited Tu Si's 'power' portion. Rival to Wang Lin's 'knowledge' portion."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tuo Sen inherited Tu Si's 'power' inheritance (born from Tu Si's failed Ink Flow Split Soul Technique). Rival to Wang Lin (who received the 'knowledge' inheritance). Wang Lin's recurring Ancient God rival.",
                List.of("Tu Si's 'power' inheritor",
                        "Born from failed Ink Flow Split Soul Technique",
                        "Rival to Wang Lin's 'knowledge' inheritance"),
                List.of("enemy", "tuo_sen", "tu_si", "ancient_god", "rival", "wang_lin_owned")
        ));

        register(CanonicalEntry.of(
                "enemy_gu_dao", "Gu Dao (Strongest Sun)", "古道",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Strongest IAC sun by size. Killed by Wang Lin."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Strongest of the 9 suns by size. Wang Lin slays him to claim the #1 position; becomes the Tenth Sun of the IAC.",
                List.of("Strongest sun by size",
                        "Slain by Wang Lin to claim #1"),
                List.of("enemy", "gu_dao", "iac", "tenth_sun", "wang_lin_killed")
        ));

        register(CanonicalEntry.of(
                "enemy_blood_ancestor", "Blood Ancestor", "血祖",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Planet Tian Yun era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("One of the three whose source origins the All-Seer plotted to absorb."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the three (with Wang Lin and Ling Tianhou) whose source origins the All-Seer plotted to absorb.",
                List.of("Target of All-Seer's plot",
                        "Source origins targeted"),
                List.of("enemy", "blood_ancestor", "all_seer", "source_origin")
        ));

        register(CanonicalEntry.of(
                "enemy_ling_tianhou", "Ling Tianhou", "凌天候",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Planet Tian Yun era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("One of the three whose source origins the All-Seer plotted to absorb."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "One of the three (with Wang Lin and Blood Ancestor) whose source origins the All-Seer plotted to absorb.",
                List.of("Target of All-Seer's plot",
                        "Source origins targeted"),
                List.of("enemy", "ling_tianhou", "all_seer", "source_origin")
        ));

        register(CanonicalEntry.of(
                "enemy_tan_lang", "Tan Lang", "贪狼",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Ancient Tomb era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Situ Nan enemy. Seized the Emperor Furnace from the Ancient Tomb."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An enemy of Situ Nan. Seized the Emperor Furnace / Heavenly Emperor Furnace (Royal Ancient God Treasure) from the Ancient Tomb. Wang Lin later seized it from him.",
                List.of("Situ Nan's enemy",
                        "Seized Emperor Furnace from Ancient Tomb",
                        "Wang Lin later seized it from him"),
                List.of("enemy", "tan_lang", "situ_nan", "emperor_furnace", "ancient_tomb")
        ));

        register(CanonicalEntry.of(
                "enemy_lei_daozi", "Lei Daozi (Envoy of Lei Xian Hall)", "雷道子",
                CanonicalCategory.ENEMIES,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1080-1100 (Lei Ji era; donghua Ep. 143)"), 4,
                        "xian-ni.fandom.com/wiki/Lei_Ji + newhanfu.com/82748.html: 'Lei Daozi (雷道子) thought he was untouchable. A master of the Yin Xu (阴虚) level, backed by the mighty Lei Xian Hall (雷仙殿).' Wang Lin hunts him down after he threatens Wang Ping and refines him into an Immortal Guard puppet (donghua Ep. 143)."),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Yin Xu-tier master; envoy of Lei Xian Hall. Hunted down by Wang Lin and refined into his First Immortal Guard puppet (silent killer)."),
                Demonstrability.ATTEST_ONLY,
                "Envoy of Lei Xian Hall (Thunder Immortal Hall / 雷仙殿). A Yin Xu (阴虚) level master who coveted Wang Lin's Lei Ji (Thunder Beast). Threatened Wang Ping (Wang Lin's mortal father) to force Wang Lin's hand. Wang Lin hunted him down and refined him into his First Immortal Guard — a silent-killer puppet.",
                List.of("Envoy of Lei Xian Hall (雷仙殿)",
                        "Yin Xu (阴虚) level master",
                        "Coveted Wang Lin's Lei Ji (Thunder Beast)",
                        "Threatened Wang Ping (Wang Lin's mortal father) to force Wang Lin's hand",
                        "Hunted down by Wang Lin",
                        "Refined into Wang Lin's First Immortal Guard puppet (silent killer)"),
                List.of("enemy", "lei_daozi", "lei_xian_hall", "thunder", "puppet", "wang_ping", "lei_ji", "wang_lin_killed", "first_immortal_guard")
        ));
    }
}
