package dev.ergenverse.wanglin;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.cosmos.CosmologicalTree;
import dev.ergenverse.core.Ergenverse;

/**
 * Wang Lin's Cosmology — the Nested-Sealed Cosmology of Renegade Immortal.
 *
 * <p><b>The Prime Cosmological Truth of Wang Lin's universe:</b>
 * every "world" is a sealed farm owned by a higher-tier cultivator.
 * The Cave World is sealed by the Seven-Colored Daoist;
 * the Sealed Realm is sealed AGAIN by the Realm-Sealing Grand Array
 * (whose spirit is the Heaven-Splitting Axe) to prevent Third-Step
 * cultivators from rising. Wang Lin's final act is to kill the
 * Seven-Colored Daoist, become the new world-owner, and Transcend.
 *
 * <p><b>The 7 nested-sealed layers (canon, C5 confidence):</b>
 * <pre>
 *   L1. The Root Dao           (本源大道)         — substrate of all existence
 *   L2. Luo Tian Star System   (罗天)             — the "true" star-system reality
 *   L3. Immortal Astral Continent (仙罡大陆)      — the massive continent in Luo Tian's void
 *   L4. The Cave World         (洞天)             — sealed pocket-world owned by Seven-Colored Daoist
 *   L5. Sealed Realm + Outer Realm (封界/外界)    — inner/outer halves of the Cave World
 *   L6. Star Systems           (星系)             — Brilliant Void, Allheaven, Cloud Sea
 *   L7. Planets → Countries → Sects              — Planet Suzaku, Zhao Country, Heng Yue Sect
 * </pre>
 *
 * <p><b>Owner-Harvest Model:</b> each layer (except Root Dao) can be "owned" by
 * a higher-tier cultivator who farms it for Joss Flames. The owner imposes a
 * cultivation ceiling on the world. To escape, the owned must kill the owner.
 *
 * <p><b>Per the Prime Directive:</b> this cosmology is OBJECTIVE. The nested-sealed
 * structure exists whether the player knows it or not. A mortal on Planet Suzaku
 * does not know they live inside a sealed farm; that ignorance does not unmake
 * the farm. Cultivation lets you PERCEIVE the seal, then BREAK it.
 *
 * <p>Canon source: CANON_RI_COMPLETE_WORLD.md (3,034 lines, 78 locations catalogued).
 */
public final class WangLinCosmology {

    private WangLinCosmology() {}

    /** The 7 cosmological layers, in nesting order (outermost first). */
    public enum Layer {
        ROOT_DAO("root_dao", "The Root Dao", "本源大道", 7, false, null),
        LUO_TIAN("luo_tian", "Luo Tian Star System", "罗天", 6, false, null),
        IMMORTAL_ASTRAL_CONTINENT("immortal_astral_continent", "Immortal Astral Continent", "仙罔大陆", 5, false, null),
        CAVE_WORLD("cave_world", "The Cave World", "洞天", 4, true, "Seven-Colored Daoist"),
        SEALED_REALM("sealed_realm", "Sealed Realm (Inner Half)", "封界", 3, true, "Realm-Sealing Grand Array"),
        OUTER_REALM("outer_realm", "Outer Realm (Outer Half)", "外界", 3, false, null),
        STAR_SYSTEMS("star_systems", "Star Systems", "星系", 2, false, null),
        PLANETS("planets", "Cultivation Planets", "星球", 1, false, null),
        COUNTRIES("countries", "Mortal Countries", "国家", 0, false, null);

        public final String id;
        public final String name;
        public final String nameCn;
        public final int depth;           // 7 = outermost, 0 = innermost
        public final boolean isSealed;
        public final String sealedBy;     // null if not sealed

        Layer(String id, String name, String nameCn, int depth, boolean isSealed, String sealedBy) {
            this.id = id; this.name = name; this.nameCn = nameCn;
            this.depth = depth; this.isSealed = isSealed; this.sealedBy = sealedBy;
        }
    }

    /** The 4 continents of the Immortal Astral Continent (Wang Lin visited all). */
    public enum IACContinent {
        HEAVENLY_BULL("Heavenly Bull Continent", "天牛大陆", "Wang Lin's primary IAC base; joined Great Soul Sect here"),
        GREEN_DEVIL("Green Devil Continent", "绿魔大陆", "Sealed Spirit beneath it granted Wang Lin Absolute Beginning + Absolute End Essences"),
        MOUNTAIN_SEA("Mountain Sea Continent", "山海大陆", "Cross-novel resonance with Meng Hao's Mountain & Sea Realm"),
        GREAT_SAINT("Great Saint Continent", "大圣大陆", "One of the 9 IAC continental divisions"),
        IMPERIAL_CITY("Dao Ancient Imperial Capital", "道古皇都", "Ruled by Ancient Clan Dao Ancient Great Heavenly Venerable");

        public final String name, nameCn, note;
        IACContinent(String n, String cn, String note) { this.name = n; this.nameCn = cn; this.note = note; }
    }

    /** The 9 Suns of the Immortal Astral Continent — Grand Empyreans all. Wang Lin becomes the 10th. */
    public enum IACSun {
        SUN_1_LIAN_DAOZHEN("Immortal Emperor Lian Daozhen", "炼道真", "Original IAC ruler"),
        SUN_2_GU_DAO("Gu Dao", "古道", "Strongest sun by size; Wang Lin slays him to claim #1"),
        SUN_3_THROUGH_8("Six Other Grand Empyreans", "其他六位大天尊", "Named individually in canon; abbreviated here"),
        SUN_9_ANCIENT_DAO("Ancient Clan Dao Ancient Great Heavenly Venerable", "道古大天尊", "Ancient Clan ruler of the Imperial Capital"),
        SUN_10_WANG_LIN("Wang Lin (the Tenth Sun)", "王林", "Achieved after slaying Gu Dao; second strongest by sun size");

        public final String name, nameCn, note;
        IACSun(String n, String cn, String note) { this.name = n; this.nameCn = cn; this.note = note; }
    }

    /** The star systems within the Sealed Realm (Wang Lin's home territory). */
    public enum SealedRealmStarSystem {
        BRILLIANT_VOID("Brilliant Void Star System", "璀璨星系", "Wang Lin's home star system"),
        ALLHEAVEN("Allheaven Star System", "苍天星系", "Named identically to ISSTH's Allheaven but is RI-specific — a star system, not a cosmic antagonist"),
        CLOUD_SEA("Cloud Sea Star System", "云海星系", "Where Wang Lin slew Daoist Water; completed Slaughter Essence");

        public final String name, nameCn, note;
        SealedRealmStarSystem(String n, String cn, String note) { this.name = n; this.nameCn = cn; this.note = note; }
    }

    /**
     * Bootstrap — registers all Wang Lin cosmology nodes into the CosmologicalTree.
     * Called once during mod init.
     */
    public static void bootstrap() {
        Ergenverse.LOGGER.info("[Ergenverse] Wang Lin Cosmology: registering 7 nested-sealed layers + IAC subdivisions.");

        // The 7 nested-sealed layers (outermost first; each is sealed by its parent layer's owner)
        // Root Dao is the substrate — no owner, no seal
        registerLayerNode(Layer.ROOT_DAO, null, new String[]{"luo_tian"},
            "absolute", "transcendence", false, null,
            "The substrate of all existence. Source-thesis of all Daos. Wang Lin comprehended his 14th Essence (Reincarnation) here, achieving Heaven Trampling.",
            null, CanonEngine.Confidence.NOVEL_STATEMENT);

        // Luo Tian — the "true" star-system reality outside the Cave World
        registerLayerNode(Layer.LUO_TIAN, "root_dao", new String[]{"immortal_astral_continent"},
            "absolute", "paragon", false, null,
            "The star-system tier above the IAC. The Cave World is a bubble floating in Luo Tian's void. The Luo Tian Alliance War was fought here. The Luo Tian Thunder Immortal Realm collapsed before Wang Lin's era.",
            "Water Daoist (defeated)", CanonEngine.Confidence.NOVEL_STATEMENT);

        // Immortal Astral Continent — the massive landmass in Luo Tian's void
        registerLayerNode(Layer.IMMORTAL_ASTRAL_CONTINENT, "luo_tian", new String[]{"cave_world"},
            "absolute", "ancient", false, null,
            "The 'true' reality outside the Cave World. A continent so vast it has nine suns (Grand Empyreans all). Subdivided into Heavenly Bull, Green Devil, Mountain Sea, Great Saint continents + Dao Ancient Imperial Capital. Wang Lin became the 'Tenth Sun' here.",
            "Lian Daozhen (original); Gu Dao (later, slain by Wang Lin)", CanonEngine.Confidence.NOVEL_STATEMENT);

        // Cave World — the sealed pocket-world. OWNER: Seven-Colored Daoist
        registerLayerNode(Layer.CAVE_WORLD, "immortal_astral_continent", new String[]{"sealed_realm", "outer_realm"},
            "medium", "true_immortal", true, "Seven-Colored Daoist (creator-owner)",
            "An artificial pocket dimension created by the Seven-Colored Daoist as a farm to harvest Joss Flames (cultivation energy from mortal faith). Contains billions of cultivation planets. The Cave World's law: Third-Step cultivators cannot naturally arise — the seal suppresses them. Wang Lin renamed it 'Wang Lin's Cave World' after killing the owner.",
            "Seven-Colored Daoist", CanonEngine.Confidence.NOVEL_STATEMENT);

        // Sealed Realm (Inner Half) — sealed AGAIN by the Realm-Sealing Grand Array
        registerLayerNode(Layer.SEALED_REALM, "cave_world", new String[]{"star_systems"},
            "medium", "nirvana_fruit", true, "Realm-Sealing Grand Array (spirit: Heaven-Splitting Axe)",
            "The inner half of the Cave World. Where Wang Lin and most Cave World cultivators live. The Realm-Sealing Grand Array (whose spirit is the Heaven-Splitting Axe) suppresses Third-Step cultivation here — only 'Heaven Blight' cultivators can squeeze through. Wang Lin became 'Lord of the Sealed Realm' and reset the array at end of arc.",
            "Realm-Sealing Grand Array", CanonEngine.Confidence.NOVEL_STATEMENT);

        // Outer Realm — the outer half, NOT sealed by the Realm-Sealing Array
        registerLayerNode(Layer.OUTER_REALM, "cave_world", new String[]{},
            "high", "spirit_seizer", false, null,
            "The outer half of the Cave World, NOT inside the Realm-Sealing Grand Array. Higher-tier cultivators dwell here. Wang Lin fled here with Li Qianmei after the Wind Celestial Realm arc. Multiple Outer-Realm Third-Step cultivators were killed by Wang Lin borrowing the Heaven-Splitting Axe.",
            null, CanonEngine.Confidence.NOVEL_STATEMENT);

        // Star Systems layer — Brilliant Void, Allheaven, Cloud Sea
        registerLayerNode(Layer.STAR_SYSTEMS, "sealed_realm", new String[]{"planets"},
            "medium", "soul_transformation", false, null,
            "The star systems within the Sealed Realm. Three primary systems: Brilliant Void (Wang Lin's home), Allheaven, Cloud Sea. Cross-star-system travel requires Nascent Soul+ and either flying treasure or transport array.",
            null, CanonEngine.Confidence.NOVEL_STATEMENT);

        // Planets layer — Planet Suzaku and others
        registerLayerNode(Layer.PLANETS, "star_systems", new String[]{"countries"},
            "low", "foundation", false, null,
            "Cultivation planets. Planet Suzaku is Wang Lin's birthplace (a rank-7 cultivation planet). Other planets: Planet Tian Yun (All-Seer's seat), Planet Five Elements (where Wang Lin condensed Water Essence), Demon Spirit Land.",
            null, CanonEngine.Confidence.NOVEL_STATEMENT);

        // Countries layer — Zhao Country and others on Planet Suzaku
        registerLayerNode(Layer.COUNTRIES, "planets", new String[]{},
            "fragile", "mortal", false, null,
            "Mortal countries on cultivation planets. Zhao Country on Planet Suzaku contains Wang Family Village, Heng Yue Sect, Tian Shui City. This is where the player starts in Wang Lin's branch.",
            null, CanonEngine.Confidence.NOVEL_STATEMENT);

        Ergenverse.LOGGER.info("[Ergenverse] Wang Lin Cosmology: {} layers registered.", Layer.values().length);
    }

    private static void registerLayerNode(Layer layer, String parentId, String[] childIds,
                                          String worldLawStrength, String minRealmToEnter,
                                          boolean isSealed, String sealReason,
                                          String description, String localAntagonist,
                                          CanonEngine.Confidence confidence) {
        CosmologicalTree.register(new CosmologicalTree.CosmologicalNode(
            layer.id, layer.name, layer.nameCn, "immortal_astral",
            parentId, childIds, new String[]{"vast_expanse", "eternal_lands", "arid_triad", "starry_cosmos", "wanggu_star_rings"},
            worldLawStrength, "wang_lin", null,
            isSealed ? "Break the seal (kill the owner or shatter the array)." : "Standard ascension within branch.",
            minRealmToEnter, isSealed, sealReason, description,
            new String[]{}, localAntagonist,
            CanonEngine.RegionStatus.KNOWN_CANON, confidence));
    }

    /**
     * Given a player's current layer, return the layer they must escape to progress.
     * Returns null if already at Root Dao (no further escape possible).
     */
    public static Layer escapeTarget(Layer current) {
        switch (current) {
            case COUNTRIES: return Layer.STAR_SYSTEMS;     // leave planet
            case PLANETS: return Layer.STAR_SYSTEMS;        // already at star system
            case STAR_SYSTEMS: return Layer.OUTER_REALM;    // break out of sealed realm
            case SEALED_REALM: return Layer.OUTER_REALM;    // bypass the array
            case OUTER_REALM: return Layer.CAVE_WORLD;      // already in cave world proper
            case CAVE_WORLD: return Layer.IMMORTAL_ASTRAL_CONTINENT; // kill Seven-Colored Daoist
            case IMMORTAL_ASTRAL_CONTINENT: return Layer.LUO_TIAN;   // transcend IAC
            case LUO_TIAN: return Layer.ROOT_DAO;           // enter Root Dao
            case ROOT_DAO: return null;                      // already at substrate
            default: return null;
        }
    }

    /**
     * The cost to escape a layer — what the player must do.
     * This is the cosmological truth: escape is not given, it is taken.
     */
    public static String escapeCost(Layer current) {
        switch (current) {
            case SEALED_REALM:
                return "Bypass or shatter the Realm-Sealing Grand Array. Requires Heaven Blight (quasi-Third-Step) cultivation minimum, OR borrow the Heaven-Splitting Axe (the array's own spirit).";
            case CAVE_WORLD:
                return "Kill the Seven-Colored Daoist (the Cave World's owner). He harvests Joss Flames from every cultivator inside; he will not let his farm go. Wang Lin's exact path: condense Void Clone → manifest as 9 suns → join Great Soul Sect → devour Dao Demon Sect Master → slay Gu Dao → become Tenth Sun → cross 9 Heaven Trampling Bridges → kill Seven-Colored Daoist.";
            case IMMORTAL_ASTRAL_CONTINENT:
                return "Comprehend the 14th Essence (Reincarnation) → cross the 9 Heaven Trampling Bridges → Transcend with your dao-companion. This is Wang Lin's final act.";
            default:
                return "Standard cultivation ascension. No owner-opposed escape required at this layer.";
        }
    }
}
