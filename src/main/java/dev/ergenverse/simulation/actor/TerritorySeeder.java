package dev.ergenverse.simulation.actor;

import dev.ergenverse.core.Ergenverse;

/**
 * TerritorySeeder — seeds the 6 canonical RI territories at world bootstrap.
 *
 * <p>The six canon territories (per Canon Ecological Principles):
 * <ol>
 *   <li><b>Mosquito Valley</b> — Wang Lin's mosquito swarm origin.
 *       Dense flora, dense herbivores, no apex (until Wang Lin's swarm).</li>
 *   <li><b>Heng Yue Faction Mountain</b> — Wang Lin's first sect.
 *       Spirit vein, sect cultivators, herb fields, formations.</li>
 *   <li><b>Sea of Devils</b> — devil-cultivator domain.
 *       High danger, low flora, devil beasts.</li>
 *   <li><b>Ancient God Battlefield</b> — ancient god ruins.
 *       Reality fissure pressure, ancient beasts, sealed inheritances.</li>
 *   <li><b>Suzaku Tomb</b> — Suzaku's inheritance tomb.
 *       Sealed entity, restriction decay pressure, karmic entanglement.</li>
 *   <li><b>Zhao Country</b> — Wang Lin's mortal homeland.
 *       Mortal villages, low Qi, joss-flame economy.</li>
 * </ol>
 */
public final class TerritorySeeder {

    private TerritorySeeder() {}

    public static void seed() {
        seed("mosquito_valley",      "Mosquito Valley",         "southern_wilds",    120,  64, -380, 256, "swamp");
        seed("heng_yue_mountain",    "Heng Yue Faction Mt.",    "zhao_country",      -240, 120,  400, 320, "mountain");
        seed("sea_of_devils",        "Sea of Devils",           "western_wastes",    800,  60, -1200, 512, "devil_swamp");
        seed("ancient_god_battlefield","Ancient God Battlefield","ruined_continent", -1600, 80, 1600, 1024, "wasteland");
        seed("suzaku_tomb",          "Suzaku Tomb",             "celestial_realm",    0,  40, 2400, 128, "tomb");
        seed("zhao_country",         "Zhao Country",            "mortal_lands",     -400, 70,  100, 768, "plains");

        Ergenverse.LOGGER.info("[Ergenverse] TerritorySeeder seeded 6 canon territories.");
    }

    private static void seed(String id, String name, String regionId,
                              int cx, int cy, int cz, int radius, String biome) {
        Territory t = new Territory(name, regionId, cx, cy, cz, radius);
        t.biomeType = biome;
        switch (id) {
            case "mosquito_valley":
                t.spiritVeinId = "sv_mosquito";
                t.qiDensity = 0.45;
                t.floraPopulation = 200;
                t.herbivorePopulation = 60;
                t.predatorPopulation = 10;
                t.apexPopulation = 0;
                t.dangerLevel = 0.5;
                t.resourceValue = 0.6;
                t.canonicalEventHistory.add("Wang Lin obtained mosquito swarm here.");
                break;
            case "heng_yue_mountain":
                t.spiritVeinId = "sv_heng_yue";
                t.qiDensity = 0.55;
                t.floraPopulation = 120;
                t.herbivorePopulation = 30;
                t.predatorPopulation = 6;
                t.humanPopulation = 80;
                t.dangerLevel = 0.3;
                t.resourceValue = 0.7;
                t.politicalStability = 0.7;
                t.canonicalEventHistory.add("Heng Yue Faction founded.");
                break;
            case "sea_of_devils":
                t.spiritVeinId = "sv_devils";
                t.qiDensity = 0.65;
                t.floraPopulation = 40;
                t.herbivorePopulation = 20;
                t.predatorPopulation = 40;
                t.apexPopulation = 2;
                t.dangerLevel = 0.9;
                t.resourceValue = 0.8;
                t.canonicalEventHistory.add("Devil cultivators dominate this region.");
                break;
            case "ancient_god_battlefield":
                t.spiritVeinId = "sv_ancient_god";
                t.qiDensity = 0.75;
                t.floraPopulation = 30;
                t.herbivorePopulation = 10;
                t.predatorPopulation = 20;
                t.apexPopulation = 5;
                t.dangerLevel = 0.95;
                t.resourceValue = 1.0;
                t.restrictions.add("reality_fissure");
                t.canonicalEventHistory.add("Ancient God fell here; reality is cracked.");
                break;
            case "suzaku_tomb":
                t.spiritVeinId = "sv_suzaku";
                t.qiDensity = 0.85;
                t.floraPopulation = 0;
                t.herbivorePopulation = 0;
                t.predatorPopulation = 0;
                t.apexPopulation = 1;
                t.dangerLevel = 1.0;
                t.resourceValue = 1.0;
                t.restrictions.add("seal_weakening");
                t.restrictions.add("karmic_entanglement");
                t.canonicalEventHistory.add("Suzaku's inheritance sealed here.");
                break;
            case "zhao_country":
                t.spiritVeinId = "sv_zhao";
                t.qiDensity = 0.15;
                t.floraPopulation = 80;
                t.herbivorePopulation = 40;
                t.predatorPopulation = 4;
                t.humanPopulation = 5000;
                t.dangerLevel = 0.1;
                t.resourceValue = 0.2;
                t.politicalStability = 0.8;
                t.canonicalEventHistory.add("Wang Lin's mortal homeland.");
                break;
            default:
                break;
        }
        // Register as an actor (territories ARE actors per the unified framework).
        Actor tActor = new Actor(id, ActorType.TERRITORY, "canon_seeded", "NOVEL_STATEMENT");
        tActor.displayName = name;
        tActor.blockX = cx;
        tActor.blockY = cy;
        tActor.blockZ = cz;
        ActorRegistry.register(tActor);
    }
}
