package dev.ergenverse.world;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.core.WorldPhilosophy;

import java.util.*;

/**
 * World Laws — the spiritual and Dao constitution of a location.
 *
 * <p>Per the {@link WorldPhilosophy}: "Every location should know why it
 * exists." A location is not just a biome. It has an origin, dominant and
 * suppressed Dao, space stability, lightning behavior — and these laws
 * then <b>determine</b> what herbs grow, what beasts evolve, what
 * techniques are stronger, what formations fail.
 *
 * <p>This is what makes the world feel authored rather than procedurally
 * generated. The Ancient Sea exists because an ancient battle split the
 * continent; therefore Water Dao is dominant and Lightning is suppressed;
 * therefore specific water-herbs grow there, specific beasts evolve there,
 * lightning tribulations are weaker there, and fire formations fail there.
 *
 * <h2>Example</h2>
 * <pre>
 *   Location: Ancient Sea
 *   Origin: Created when an ancient battle split the continent.
 *   Dao Affinities:
 *     Water Dao: dominant (1.5×)
 *     Fire Dao: suppressed (0.5×)
 *     Lightning Dao: suppressed (0.3×)
 *     Space Dao: unstable (0.7×)
 *   Space Stability: fragile
 *   Lightning Behavior: suppressed
 *   Cultivation Ceiling: Nascent Soul
 *   Consequences:
 *     Water-attribute herbs grow abundantly.
 *     Water-attribute beasts evolve here.
 *     Lightning tribulations are weaker (good for cultivators here).
 *     Fire formations fail or backfire.
 *     Teleportation arrays may shunt you into the void.
 * </pre>
 *
 * <p>These laws are derived from the location's origin, not rolled
 * randomly. The origin is canon-sourced (e.g., "Ancient battle" for the
 * Ancient Sea), and the laws follow logically.
 */
public final class WorldLaws {

    /** The origin reason — why this location exists. Canon-sourced. */
    public enum Origin {
        ANCIENT_BATTLE("Created when an ancient battle split the continent."),
        SEALED_BEAST("Created when a powerful beast was sealed beneath the land."),
        SPIRIT_VEIN_BLOOM("Formed when a spirit vein broke the surface and saturated the land."),
        FALLEN_IMMORTAL("Formed when a cultivator fell here and their Dao imprinted the land."),
        WORLD_BOUNDARY("Marked where the world's boundary was placed by its creator."),
        TRIBULATION_SCAR("Scarred by a heavenly tribulation that struck this place."),
        NATURAL_AGE("Formed by ten thousand years of natural spiritual accumulation."),
        ARTIFICIAL_FARM("Created by a higher being as a harvest pen."),
        DAO_LAW_SATURATION("Saturated with a specific Dao by an ancient cultivator's meditation."),
        COSMIC_RIFT("Opened by a rift in the cosmic structure.");

        public final String description;
        Origin(String description) { this.description = description; }
    }

    /** A Dao affinity entry — multiplier >1 is dominant, <1 is suppressed. */
    public static final class DaoAffinity {
        public final String dao; // "water", "fire", "lightning", etc.
        public final double multiplier;
        public final boolean dominant;
        public final boolean suppressed;

        public DaoAffinity(String dao, double multiplier) {
            this.dao = dao;
            this.multiplier = multiplier;
            this.dominant = multiplier > 1.2;
            this.suppressed = multiplier < 0.8;
        }
    }

    public final Origin origin;
    public final List<DaoAffinity> daoAffinities;
    public final double spaceStability; // 0.0 (collapsing) to 1.0 (rock-solid)
    public final double lightningSuppression; // 0.0 (normal) to 1.0 (tribulations impossible)
    public final String cultivationCeiling; // highest realm achievable here
    public final CanonEngine.RegionStatus regionStatus;
    public final CanonEngine.Confidence canonConfidence;
    public final String name;
    public final String description;

    private WorldLaws(String name, Origin origin, List<DaoAffinity> daoAffinities,
                      double spaceStability, double lightningSuppression,
                      String cultivationCeiling, CanonEngine.RegionStatus regionStatus,
                      CanonEngine.Confidence canonConfidence, String description) {
        this.name = name;
        this.origin = origin;
        this.daoAffinities = daoAffinities;
        this.spaceStability = spaceStability;
        this.lightningSuppression = lightningSuppression;
        this.cultivationCeiling = cultivationCeiling;
        this.regionStatus = regionStatus;
        this.canonConfidence = canonConfidence;
        this.description = description;
    }

    /** Get the multiplier for a specific Dao (1.0 if not listed). */
    public double daoMultiplier(String dao) {
        for (DaoAffinity a : daoAffinities) {
            if (a.dao.equals(dao)) return a.multiplier;
        }
        return 1.0;
    }

    /** Does this location's laws allow a formation of the given Dao to function? */
    public boolean formationFunctions(String dao) {
        return daoMultiplier(dao) > 0.5;
    }

    /** Does this location's laws allow teleportation arrays to work? */
    public boolean teleportationSafe() {
        return spaceStability > 0.5;
    }

    /** How strong is a tribulation in this location? (1.0 = normal, 0.0 = impossible) */
    public double tribulationStrength() {
        return 1.0 - lightningSuppression;
    }

    // ─── Builder ──────────────────────────────────────────────────────
    public static Builder named(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private Origin origin = Origin.NATURAL_AGE;
        private final List<DaoAffinity> affinities = new ArrayList<>();
        private double spaceStability = 1.0;
        private double lightningSuppression = 0.0;
        private String ceiling = "Nascent Soul";
        private CanonEngine.RegionStatus status = CanonEngine.RegionStatus.KNOWN_CANON;
        private CanonEngine.Confidence conf = CanonEngine.Confidence.WIKI_BACKED;
        private String desc = "";

        public Builder(String name) { this.name = name; }

        public Builder origin(Origin o) { this.origin = o; return this; }
        public Builder affinity(String dao, double mult) {
            this.affinities.add(new DaoAffinity(dao, mult));
            return this;
        }
        public Builder spaceStability(double s) { this.spaceStability = s; return this; }
        public Builder lightningSuppression(double s) { this.lightningSuppression = s; return this; }
        public Builder ceiling(String c) { this.ceiling = c; return this; }
        public Builder regionStatus(CanonEngine.RegionStatus s) { this.status = s; return this; }
        public Builder canon(CanonEngine.Confidence c) { this.conf = c; return this; }
        public Builder description(String d) { this.desc = d; return this; }

        public WorldLaws build() {
            return new WorldLaws(name, origin, Collections.unmodifiableList(affinities),
                spaceStability, lightningSuppression, ceiling, status, conf, desc);
        }
    }

    // ─── Canon location laws registry ────────────────────────────────
    private static final Map<String, WorldLaws> REGISTRY = new HashMap<>();

    public static void register(String locationId, WorldLaws laws) {
        REGISTRY.put(locationId, laws);
    }

    public static WorldLaws get(String locationId) {
        return REGISTRY.get(locationId);
    }

    /**
     * Get a simplified law tier (0-12) for a WorldLaws instance.
     * Higher tier = stronger world laws = divine sense more suppressed.
     *
     * <p>Derived from the cultivation ceiling:
     * Core Formation=3, Nascent Soul=4, Soul Formation=5, Soul Transformation=6,
     * Ascendant=7, True Immortal=10, Paragon=11, Transcendent=12.
     */
    public int getLawTier() {
        if (cultivationCeiling == null) return 0;
        String c = cultivationCeiling.toLowerCase();
        if (c.contains("transcend")) return 12;
        if (c.contains("paragon")) return 11;
        if (c.contains("ancient")) return 10;
        if (c.contains("true") || c.contains("immortal")) return 9;
        if (c.contains("nirvana")) return 7;
        if (c.contains("illusory") || c.contains("corporeal")) return 7;
        if (c.contains("soul") && c.contains("transform")) return 6;
        if (c.contains("soul") && c.contains("format")) return 5;
        if (c.contains("nascent")) return 4;
        if (c.contains("core")) return 3;
        if (c.contains("foundation")) return 2;
        if (c.contains("qi")) return 1;
        return 0;
    }

    /**
     * Get the world-law tier at a position in a level.
     * For v1, defaults to 0 (no suppression). Future: biome → location lookup.
     */
    public static int getLawTierAt(net.minecraft.server.level.ServerLevel level,
                                    net.minecraft.core.BlockPos pos) {
        // v1: default 0 (no law suppression).
        // Future: map the biome at pos to a WorldLaws registry entry and use its getLawTier().
        return 0;
    }

    /** Bootstrap the canon location laws. */
    public static void bootstrap() {
        Ergenverse.LOGGER.info("[Ergenverse] Bootstrapping World Laws for {} canon locations.", REGISTRY.size());
    }

    static {
        // ── Wang Lin's branch — Planet Suzaku locations ──
        register("zhao_country", WorldLaws.named("Zhao Country")
            .origin(Origin.NATURAL_AGE)
            .affinity("fire", 1.1).affinity("wood", 1.0).affinity("lightning", 0.9)
            .spaceStability(0.9).lightningSuppression(0.05)
            .ceiling("Core Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A small mortal country on Planet Suzaku. Qi is thin, laws are fragile. Most cultivators here never reach Nascent Soul. Heng Yue Sect (secretly led by 5th-Gen Vermilion Bird Lu Yun), Xuan Dao Sect, the Teng Clan, and the Wang Clan all reside here. Wang Lin's birthplace.")
            .build());

        register("chu_country", WorldLaws.named("Chu Country")
            .origin(Origin.NATURAL_AGE)
            .affinity("wind", 1.3).affinity("cloud", 1.2).affinity("water", 1.0)
            .spaceStability(0.85).lightningSuppression(0.1)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("Neighboring country to Zhao. The Cloud Sky Sect is based here; Li Muwan became elder then Sect Master. Cloud-aspect Qi enables flight techniques. Wang Lin killed Sun Zhenwei at Li Muwan's wedding, became Sect Master, then handed the seat to Li Muwan. He returned to build a small house with her for her final 10 years.")
            .build());

        register("fire_burn_country", WorldLaws.named("Fire Burn Country")
            .origin(Origin.SPIRIT_VEIN_BLOOM)
            .affinity("fire", 1.5).affinity("earth", 1.2).affinity("water", 0.5)
            .spaceStability(0.8).lightningSuppression(0.2)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("Li Muwan's home country. The Luo He Sect is based here; her brother Li Qiqing was an elite disciple. Fire Beasts roam this region. Wang Lin (in Ma Liang's body) met Li Muwan while escaping a Fire Beast. The Heaven-Defying Bead ate the King of Fire Beasts here. Fire-aspect spirit veins dominate the landscape.")
            .build());

        register("sky_demon_country", WorldLaws.named("Sky Demon Country")
            .origin(Origin.SEALED_BEAST)
            .affinity("demon", 1.4).affinity("fire", 1.1).affinity("soul", 1.2)
            .spaceStability(0.6).lightningSuppression(0.3)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A demonic country on Planet Suzaku, likely in the East Demon Spirit Sea region. The Ancient Demon City (Demon Capital) is the capital; site of the Demonic Drum tournament. Demonic-aspect spirit veins. The demonic Qi is objective — it exists because of the sealed demonic entities beneath the land, not because cultivators summoned it.")
            .build());

        register("fire_demon_country", WorldLaws.named("Fire Demon Country")
            .origin(Origin.SEALED_BEAST)
            .affinity("fire", 1.3).affinity("demon", 1.3).affinity("water", 0.4)
            .spaceStability(0.5).lightningSuppression(0.4)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A demonic country near the East Demon Spirit Sea. A fragmented ancient demon is sealed beneath this country — Bei Luo devours it. Combined fire + demonic Qi. The sealed demon's 'leakage' creates lesser demon-spawn in the vicinity. The seal is objective — it was placed before Wang Lin's story began.")
            .build());

        register("pilu_kingdom", WorldLaws.named("Pilu Kingdom")
            .origin(Origin.NATURAL_AGE)
            .affinity("soul", 1.4).affinity("fire", 1.2).affinity("water", 0.6)
            .spaceStability(0.7).lightningSuppression(0.15)
            .ceiling("Soul Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("Headquarters of the Soul Refining Sect. Dun Tian was the ancestor; Nian Tian was his senior brother. Wang Lin inherited the Ten Billion Soul Banner and the Soul Refining Sect legacy here. After Wang Lin's actions, the Soul Refining Tribe (1M+ people, originally the Mountain Valley Tribe) was elevated. The land is arid — soul and fire energy dominate.")
            .build());

        register("snow_domain_country", WorldLaws.named("Snow Domain Country")
            .origin(Origin.NATURAL_AGE)
            .affinity("ice", 1.5).affinity("water", 1.2).affinity("fire", 0.4)
            .spaceStability(0.85).lightningSuppression(0.2)
            .ceiling("Core Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("The cold-region country on Planet Suzaku. Ice-aspect spirit veins. Frost-Tusk Mammoth herds roam the tundra. Snow-Heart Herb grows in the permafrost. The cold is objective — mortals here have adapted to it over generations; cultivators do not 'create' the cold, they perceive the ice-aspect Qi that was always there.")
            .build());

        register("xuan_wu_country", WorldLaws.named("Xuan Wu Country")
            .origin(Origin.NATURAL_AGE)
            .affinity("earth", 1.3).affinity("water", 1.1).affinity("fire", 0.8)
            .spaceStability(0.9).lightningSuppression(0.1)
            .ceiling("Core Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A country Wang Lin fled to while being chased by Duanmu Ji from the Sea of Devils. Cooler temperate climate, mountainous terrain. The mountains provided cover — the chase narrative happened here because of the geography, not the other way around.")
            .build());

        register("vermilion_bird_country", WorldLaws.named("Vermilion Bird Country")
            .origin(Origin.DAO_LAW_SATURATION)
            .affinity("fire", 1.5).affinity("lightning", 1.2).affinity("blood", 1.3)
            .spaceStability(0.95).lightningSuppression(0.05)
            .ceiling("Soul Transformation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("The ruling cultivation nation of Planet Suzaku — level-6 (raised by Situ Nan). The Vermilion Bird Master is a renewable office: Situ Nan was 2nd-Gen, Ye Wuyou 1st, the 3rd betrayed Situ Nan, the 13th was Qian Pinghai, the 14th severed Situ Nan's arm, the 15th was Zhou Wutai, the 16th was Wang Lin. The Vermilion Bird Divine Sect headquarters is here. Highest-tier ecology on the planet — the elevated Qi density is objective.")
            .build());

        register("qing_shui_ruin", WorldLaws.named("Qing Shui Kingdom (Ruin)")
            .origin(Origin.TRIBULATION_SCAR)
            .affinity("slaughter", 1.5).affinity("blood", 1.2).affinity("life", 0.4)
            .spaceStability(0.4).lightningSuppression(0.5)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A destroyed mortal kingdom. Its annihilation 'seeded Qing Shui's slaughter essence.' The slaughter residue is objective — it saturates the soil whether the player perceives it or not. A mortal walking here feels unease without knowing why; a cultivator at Soul Formation+ can perceive the slaughter-essence Qi. The ruin generates slaughter-aspect spirit beasts and herbs (Law G4).")
            .build());

        register("jue_ming_valley", WorldLaws.named("Jue Ming Valley")
            .origin(Origin.COSMIC_RIFT)
            .affinity("space", 1.3).affinity("seal", 1.2).affinity("soul", 1.0)
            .spaceStability(0.3).lightningSuppression(0.3)
            .ceiling("Core Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("The valley whose tokens grant access to the Foreign Battleground / Extraterrestrial Battlefield — a sealed battlefield outside normal Suzaku geography. The rift exists independently of the tokens; the tokens are the only safe way through. Without one, a cultivator who enters the rift is lost. Space is unstable here — teleportation arrays may shunt you into the void.")
            .build());

        register("sea_of_devils", WorldLaws.named("Sea of Devils")
            .origin(Origin.ANCIENT_BATTLE)
            .affinity("water", 1.4).affinity("soul", 1.3).affinity("fire", 0.6).affinity("lightning", 0.7)
            .spaceStability(0.6).lightningSuppression(0.3)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("Also called Demon Cultivation Sea. A vast demonic-sea region divided into 14+ districts. An ancient battle shattered the land here. Water and Soul Dao are dominant; fire and lightning suppressed. The Fighting Evil Sect, Soul Refining Sect, and Corpse Yin Sect all operate in/around it. Wang Lin found the Mosquito Beast in these waters and made his Core Formation breakthrough here with Li Muwan.")
            .build());

        register("land_of_ancient_god", WorldLaws.named("Land of the Ancient God")
            .origin(Origin.FALLEN_IMMORTAL)
            .affinity("seal", 1.5).affinity("earth", 1.3).affinity("space", 0.5)
            .spaceStability(0.4).lightningSuppression(0.6)
            .ceiling("Soul Formation")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("An ancient god fell here in the age before memory. Its corpse imprinted the land with Seal Dao. Space is unstable. Wang Lin studied restrictions here for 7 years. His hair turned white.")
            .build());

        // ── Meng Hao's branch — Mountain and Sea Realm ──
        register("mountain_sea_realm", WorldLaws.named("Mountain and Sea Realm")
            .origin(Origin.DAO_LAW_SATURATION)
            .affinity("seal", 1.5).affinity("karma", 1.2).affinity("fire", 1.0)
            .spaceStability(0.95).lightningSuppression(0.1)
            .ceiling("Paragon")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("Founded by Paragon Nine Seals. 9 Mountains and 9 Seas orbit a sun. Seal Dao is dominant because the realm itself was sealed by Allheaven. The Bridge of Immortality connects to Transcendence.")
            .build());

        // ── Bai Xiaochun's branch — Heavenspan Realm ──
        register("heavenspan_realm", WorldLaws.named("Heavenspan Realm")
            .origin(Origin.WORLD_BOUNDARY)
            .affinity("longevity", 1.5).affinity("water", 1.3).affinity("fire", 1.1)
            .spaceStability(0.7).lightningSuppression(0.2)
            .ceiling("Celestial")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.NOVEL_STATEMENT)
            .description("A sealed realm. The Heavenspan River is the seal's spine — a sleeping spirit beast. Longevity Dao is dominant because the sect's cultivation method extends lifespan unnaturally. The Gravekeeper waits beyond the sky.")
            .build());

        // ── Xu Qing's branch — Wanggu (9th Star Ring) ──
        register("south_phoenix_continent", WorldLaws.named("South Phoenix Continent")
            .origin(Origin.COSMIC_RIFT)
            .affinity("heterogeneity", 1.6).affinity("blood", 1.2).affinity("lightning", 0.4)
            .spaceStability(0.5).lightningSuppression(0.6)
            .ceiling("Nascent Soul")
            .regionStatus(CanonEngine.RegionStatus.KNOWN_CANON)
            .canon(CanonEngine.Confidence.WIKI_BACKED)
            .description("Xu Qing's starting continent. The Broken God's Face pollutes the sky with Heterogeneity. Lightning is suppressed — tribulations here are weaker but the world itself is corrupted. The Forbidden Sea is the corpse of something vast.")
            .build());

        Ergenverse.LOGGER.info("[Ergenverse] Registered {} canon location laws.", REGISTRY.size());
    }
}
