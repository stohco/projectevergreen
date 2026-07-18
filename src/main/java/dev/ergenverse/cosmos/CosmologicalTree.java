package dev.ergenverse.cosmos;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.core.Ergenverse;

import java.util.HashMap;
import java.util.Map;

/**
 * The Cosmological Tree — the Er Gen multiverse as a branching tree of
 * connected cosmologies.
 *
 * <p>NOT a flat ladder — each protagonist has their own cosmological
 * branch, and branches only connect at the highest tiers (4th Step /
 * Transcendence).
 *
 * <pre>
 *                          ROOT DAO (unknown origin)
 *                                |
 *           ┌────────────────────┼────────────────────┐
 *           |                    |                    |
 *    Immortal Astral       Vast Expanse         Eternal Lands
 *     Continent                |                    |
 *           |            Mountain & Sea             Eternal River
 *      Cave Worlds              Realm                   |
 *           |             Planet South Heaven    Heavenspan Realm
 *      Star Systems                                      |
 *           |                                      Lower Worlds
 *      Planet Suzaku
 *           |
 *      Mortal World
 * </pre>
 *
 * <p>Plus parallel branches: Arid Triad Cosmos (Su Ming), Starry Cosmos
 * (Wang Baole), Wanggu / 36 Star Rings (Xu Qing).
 *
 * <p>Travel rules:
 * <ul>
 *   <li>Pre-4th-Step: travel ONLY within your cosmological branch.</li>
 *   <li>Early 4th Step: void crossing to ADJACENT branches (needs
 *       coordinates + karma).</li>
 *   <li>Peak 4th Step: true multiversal travel (all branches accessible).</li>
 * </ul>
 *
 * <p><b>Important (per the Prime Directive):</b> dimensions are an
 * implementation detail. From the player's perspective, travel should
 * feel like progressing through the Er Gen cosmology — ascension, void
 * crossing, world-barrier traversal. The presentation should never feel
 * like switching between disconnected game levels.
 */
public final class CosmologicalTree {

    private static final Map<String, CosmologicalNode> NODES = new HashMap<>();
    private static volatile boolean bootstrapped = false;

    private CosmologicalTree() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        Ergenverse.LOGGER.info("[Ergenverse] Cosmological Tree: {} nodes registered across 7 branches.", NODES.size());
    }

    public static void register(CosmologicalNode node) {
        NODES.put(node.id, node);
    }

    public static CosmologicalNode get(String id) {
        return NODES.get(id);
    }

    /** A node in the cosmological tree. */
    public static final class CosmologicalNode {
        public final String id;
        public final String name;
        public final String nameCn;
        public final String branch;
        public final String parentId;
        public final String[] childIds;
        public final String[] siblingBranches;
        public final String worldLawStrength; // fragile | low | medium | high | absolute
        public final String protagonistId;     // null if no protagonist lives here
        public final String protagonistLocation;
        public final String travelMethod;
        public final String minRealmToEnter;
        public final boolean isSealed;
        public final String sealReason;
        public final String description;
        public final String[] biomes;
        public final String localAntagonist;
        public final CanonEngine.RegionStatus regionStatus;
        public final CanonEngine.Confidence canonConfidence;

        public CosmologicalNode(String id, String name, String nameCn, String branch,
                                String parentId, String[] childIds, String[] siblingBranches,
                                String worldLawStrength, String protagonistId, String protagonistLocation,
                                String travelMethod, String minRealmToEnter, boolean isSealed,
                                String sealReason, String description, String[] biomes,
                                String localAntagonist, CanonEngine.RegionStatus regionStatus,
                                CanonEngine.Confidence canonConfidence) {
            this.id = id;
            this.name = name;
            this.nameCn = nameCn;
            this.branch = branch;
            this.parentId = parentId;
            this.childIds = childIds;
            this.siblingBranches = siblingBranches;
            this.worldLawStrength = worldLawStrength;
            this.protagonistId = protagonistId;
            this.protagonistLocation = protagonistLocation;
            this.travelMethod = travelMethod;
            this.minRealmToEnter = minRealmToEnter;
            this.isSealed = isSealed;
            this.sealReason = sealReason;
            this.description = description;
            this.biomes = biomes;
            this.localAntagonist = localAntagonist;
            this.regionStatus = regionStatus;
            this.canonConfidence = canonConfidence;
        }
    }

    static {
        // Root
        register(new CosmologicalNode("root_dao", "The Root Dao", "根源道", "root_dao",
            null, new String[]{"immortal_astral_continent", "vast_expanse_realm", "eternal_lands",
                "arid_triad_cosmos", "starry_cosmos", "wanggu_world"},
            new String[]{}, "absolute", null, null,
            "Unknown — beyond all current cultivation. Only the Root Dao itself grants access.",
            "transcendence", false, null,
            "The unknown origin from which all cosmological branches spring.",
            new String[]{}, null, CanonEngine.RegionStatus.UNKNOWN, CanonEngine.Confidence.COMMUNITY));

        // Wang Lin's branch — Immortal Astral
        register(new CosmologicalNode("immortal_astral_continent", "The Immortal Astral Continent", "仙罡大陆",
            "immortal_astral", "root_dao", new String[]{"cave_world"},
            new String[]{"vast_expanse", "eternal_lands", "arid_triad", "starry_cosmos", "wanggu_star_rings"},
            "absolute", "wang_lin", "Heavenly Bull Continent",
            "4th Step void crossing from any sibling branch. Within branch: ascend from Cave World.",
            "ancient", false, null,
            "The \"true\" reality outside Wang Lin's Cave World. Wang Lin rebuilt it after escaping.",
            new String[]{"celestial_mountains", "ancient_god_ruins", "star_fields"}, "All-Seer; Seven-Colored Daoist (defeated)",
            CanonEngine.RegionStatus.KNOWN_CANON, CanonEngine.Confidence.NOVEL_STATEMENT));

        register(new CosmologicalNode("cave_world", "The Cave World", "洞府世界", "immortal_astral",
            "immortal_astral_continent", new String[]{"planet_suzaku"}, new String[]{},
            "low", null, null,
            "Within IAC branch. Ascend through the star systems to reach the IAC.",
            "mortal", true, "Seven-Colored Daoist created the Cave World as an artificial farm to harvest Joss Flames.",
            "An artificial pocket dimension. Planet Suzaku is the starting world.",
            new String[]{"spirit_mountains", "mortal_plains", "sea_of_devils"},
            "Seven-Colored Daoist", CanonEngine.RegionStatus.KNOWN_CANON, CanonEngine.Confidence.NOVEL_STATEMENT));

        register(new CosmologicalNode("planet_suzaku", "Planet Suzaku", "朱雀星", "immortal_astral",
            "cave_world", new String[]{"zhao_country"}, new String[]{},
            "fragile", null, null,
            "Starting world for players in Wang Lin's branch. Flying sword / transfer array from here to star systems.",
            "mortal", false, null,
            "A lower-tier cultivation planet. Wang Lin's birthplace.",
            new String[]{"jade_hills", "spirit_forests", "mortal_villages"},
            "Teng Huayuan (early); All-Seer (mid); Seven-Colored Daoist (late)",
            CanonEngine.RegionStatus.KNOWN_CANON, CanonEngine.Confidence.NOVEL_STATEMENT));

        register(new CosmologicalNode("zhao_country", "Zhao Country", "赵国", "immortal_astral",
            "planet_suzaku", new String[]{}, new String[]{},
            "fragile", null, null, "Starting location. Walk or ride.",
            "mortal", false, null,
            "A small country on Planet Suzaku. Contains Wang Family Village, Heng Yue Sect, Tian Shui City.",
            new String[]{"mortal_village", "sect_mountain", "dense_forest"},
            null, CanonEngine.RegionStatus.KNOWN_CANON, CanonEngine.Confidence.NOVEL_STATEMENT));

        // (Other branches will be added as the mod expands. For Phase 0,
        // we focus on Planet Suzaku / Zhao Country — Wang Lin's branch.)
    }
}
