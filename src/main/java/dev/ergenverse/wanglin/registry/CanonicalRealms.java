package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalRealms — the cosmology layers (Wang Lin's nested-sealed universe).
 *
 * <p>Cross-refs {@code dev.ergenverse.wanglin.WangLinCosmology.Layer} (the 9
 * nested-sealed layers). Each entry surfaces the canon-documented seal and
 * owner, and the escape cost from that layer.
 */
public final class CanonicalRealms extends AbstractSubRegistry {

    public CanonicalRealms() {
        super("CanonicalRealms");
    }

    @Override
    protected void doBootstrap() {
        register(CanonicalEntry.of(
                "realm_root_dao", "The Root Dao", "本源大道",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 2087"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("The substrate of all existence. Wang Lin comprehended the Reincarnation Essence here."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The substrate of all existence. Source-thesis of all Daos. Wang Lin comprehended his 14th Essence (Reincarnation) here, achieving Heaven Trampling. No owner; no seal.",
                List.of("Substrate of all existence",
                        "Wang Lin comprehended Reincarnation Essence here",
                        "Achieved Heaven Trampling here"),
                List.of("realm", "root_dao", "transcendence", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "realm_luo_tian", "Luo Tian Star System", "罗天",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("Luo Tian Alliance War era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Star-system reality outside the Cave World."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The star-system tier above the IAC. The Cave World is a bubble floating in Luo Tian's void. The Luo Tian Alliance War was fought here. The Luo Tian Thunder Immortal Realm collapsed before Wang Lin's era. Local antagonist: Water Daoist (defeated).",
                List.of("Star-system reality outside the Cave World",
                        "Cave World is a bubble in Luo Tian's void",
                        "Luo Tian Alliance War fought here"),
                List.of("realm", "luo_tian", "star_system", "water_daoist")
        ));

        register(CanonicalEntry.of(
                "realm_immortal_astral_continent", "Immortal Astral Continent", "仙罔大陆",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("IAC era"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("The 'true' reality outside the Cave World. 9 suns + Wang Lin (10th)."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The 'true' reality outside the Cave World. A continent so vast it has nine suns (Grand Empyreans all). Subdivided into Heavenly Bull, Green Devil, Mountain Sea, Great Saint continents + Dao Ancient Imperial Capital. Wang Lin became the 'Tenth Sun' here. Original owner: Lian Daozhen; later Gu Dao (slain by Wang Lin).",
                List.of("'True' reality outside the Cave World",
                        "9 suns (Grand Empyreans)",
                        "4 continents + Imperial Capital",
                        "Wang Lin became the 10th Sun"),
                List.of("realm", "iac", "grand_empyrean", "lian_daozhen", "gu_dao", "tenth_sun")
        ));

        register(CanonicalEntry.of(
                "realm_cave_world", "The Cave World", "洞天",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin became the new owner after killing the Seven-Colored Daoist."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "An artificial pocket dimension created by the Seven-Colored Daoist as a farm to harvest Joss Flames (cultivation energy from mortal faith). Contains billions of cultivation planets. The Cave World's law: Third-Step cultivators cannot naturally arise — the seal suppresses them. Wang Lin renamed it 'Wang Lin's Cave World' after killing the owner.",
                List.of("Artificial pocket dimension",
                        "Owner-farm: Seven-Colored Daoist harvests Joss Flames",
                        "Cultivation ceiling: Third-Step cannot arise",
                        "Wang Lin became new owner"),
                List.of("realm", "cave_world", "seven_colored_daoist", "joss_flame", "sealed", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "realm_sealed_realm", "Sealed Realm (Inner Half)", "封界",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.WANG_LIN_OWNED,
                Transferability.uniquelyBound("Wang Lin became Lord of the Sealed Realm; reset the Realm-Sealing Grand Array."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The inner half of the Cave World. Where Wang Lin and most Cave World cultivators live. The Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe) suppresses Third-Step cultivation here — only 'Heaven Blight' cultivators can squeeze through. Wang Lin became 'Lord of the Sealed Realm' and reset the array at end of arc.",
                List.of("Inner half of the Cave World",
                        "Sealed by Realm-Sealing Grand Array (spirit: Heaven-Splitting Axe)",
                        "Suppresses Third-Step cultivation",
                        "Wang Lin became Lord + reset the array"),
                List.of("realm", "sealed_realm", "realm_sealing_grand_array", "heaven_splitting_axe", "wang_lin_unique")
        ));

        register(CanonicalEntry.of(
                "realm_outer_realm", "Outer Realm (Outer Half)", "外界",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Outer half of the Cave World, NOT inside the Realm-Sealing Array."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The outer half of the Cave World, NOT inside the Realm-Sealing Grand Array. Higher-tier cultivators dwell here. Wang Lin fled here with Li Qianmei after the Wind Celestial Realm arc. Multiple Outer-Realm Third-Step cultivators were killed by Wang Lin borrowing the Heaven-Splitting Axe.",
                List.of("Outer half of the Cave World",
                        "NOT inside the Realm-Sealing Grand Array",
                        "Higher-tier cultivators dwell here"),
                List.of("realm", "outer_realm", "cave_world", "heaven_splitting_axe")
        ));

        register(CanonicalEntry.of(
                "realm_star_systems", "Star Systems (Brilliant Void / Allheaven / Cloud Sea)", "星系",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Three primary star systems within the Sealed Realm."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "The star systems within the Sealed Realm. Three primary systems: Brilliant Void (Wang Lin's home), Allheaven, Cloud Sea. Cross-star-system travel requires Nascent Soul+ and either flying treasure or transport array. NOTE: Allheaven is a star system, not the ISSTH cosmic antagonist (per user correction #1).",
                List.of("3 primary star systems: Brilliant Void, Allheaven, Cloud Sea",
                        "Cross-system travel requires Nascent Soul+",
                        "Allheaven here = star system, NOT cosmic antagonist (per correction #1)"),
                List.of("realm", "star_systems", "brilliant_void", "allheaven", "cloud_sea", "wang_lin_home")
        ));

        register(CanonicalEntry.of(
                "realm_planets", "Cultivation Planets (Suzaku, Tian Yun, Five Elements)", "星球",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("throughout"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Planet Suzaku is Wang Lin's birthplace."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Cultivation planets. Planet Suzaku is Wang Lin's birthplace (a rank-7 cultivation planet). Other planets: Planet Tian Yun (All-Seer's seat), Planet Five Elements (where Wang Lin condensed Water Essence), Demon Spirit Land. NOTE: Suzaku is a planet, not a continent (per user correction #6).",
                List.of("Planet Suzaku = Wang Lin's birthplace (rank-7)",
                        "Planet Tian Yun = All-Seer's seat",
                        "Planet Five Elements = Water Essence condensation",
                        "Per correction #6: Suzaku is a planet, not a continent"),
                List.of("realm", "planets", "suzaku", "tian_yun", "five_elements", "wang_lin_home")
        ));

        register(CanonicalEntry.of(
                "realm_countries", "Mortal Countries (Zhao)", "国家",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 1+"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Mortal countries — where Wang Lin was born."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Mortal countries on cultivation planets. Zhao Country on Planet Suzaku contains Wang Family Village, Heng Yue Sect, Tian Shui City. This is where the player starts in Wang Lin's branch.",
                List.of("Zhao Country on Planet Suzaku",
                        "Contains Wang Family Village, Heng Yue Sect, Tian Shui City",
                        "Player's starting branch"),
                List.of("realm", "countries", "zhao", "wang_family_village", "heng_yue_sect", "player_start")
        ));

        register(CanonicalEntry.of(
                "realm_land_of_ancient_god", "Land of the Ancient God (Tu Si's Body)", "古神之地",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. 190+"), 5),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Tu Si's body became this 3-level Chaotic Broken Stars realm."),
                Demonstrability.CAN_DEMONSTRATE_FULLY,
                "Tu Si's body became the Land of the Ancient God (3-level Chaotic Broken Stars realm). Site of the Ancient God inheritance, the Restrictions Mountain Trial, and Tuo Sen's birth.",
                List.of("Tu Si's body became this realm",
                        "3-level Chaotic Broken Stars",
                        "Site of the Restrictions Mountain Trial"),
                List.of("realm", "ancient_god", "tu_si", "restriction_mountain", "tuo_sen")
        ));

        register(CanonicalEntry.of(
                "realm_lei_xian_hall", "Lei Xian Hall (Thunder Immortal Hall)", "雷仙殿",
                CanonicalCategory.REALMS,
                Provenance.explicit("Renegade Immortal", List.of("Ch. ~1080-1100 (Lei Ji era; donghua Ep. 143)"), 4,
                        "xian-ni.fandom.com + newhanfu.com/82748.html: 'backed by the mighty Lei Xian Hall (雷仙殿).' Faction of envoy Lei Daozi. Canon disambiguation pending: may overlap with Thunder Celestial Temple (L54 thunder_celestial_temple); relationship needs canon confirmation — flagged as gap."),
                OwnershipState.ENCOUNTERED,
                Transferability.uniquelyBound("Faction/location — Lei Daozi's home faction. Relationship to Thunder Celestial Temple (L54) needs canon disambiguation."),
                Demonstrability.ATTEST_ONLY,
                "Thunder Immortal Hall (雷仙殿) — the faction of Lei Daozi. Sent envoys (including Lei Daozi) who threatened Wang Lin's sect / family (Wang Ping) to coerce Wang Lin into surrendering Lei Ji. May overlap with Thunder Celestial Temple (L54); relationship needs canon disambiguation. Not registered as a distinct Wang Lin-owned location.",
                List.of("Lei Daozi's home faction (雷仙殿)",
                        "Sent envoys threatening Wang Lin's sect / Wang Ping",
                        "Possible overlap with Thunder Celestial Temple (L54) — disambiguation pending",
                        "Name UNKNOWN at edge of canon for distinct faction identity — not invented"),
                List.of("realm", "lei_xian_hall", "thunder", "faction", "location", "lei_daozi", "needs_disambiguation")
        ));
    }
}
