package dev.ergenverse.ecology;

import dev.ergenverse.core.Ergenverse;

import java.util.UUID;

/**
 * EcosystemSeeder — seeds the 7 key RI-location ecosystems into
 * {@link CausalEcology}.
 *
 * <p>These are the ecosystems that anchor the canon locations. They are
 * seeded with canon-faithful initial populations and Qi output, then the
 * {@link CausalEcology#tickAll()} loop evolves them each Minecraft day.
 *
 * <p>The 7 key ecosystems:
 * <ol>
 *   <li>Mosquito Valley — dense flora, dense herbivores, mosquito swarm apex.</li>
 *   <li>Heng Yue Mountain — sect cultivators + herb fields.</li>
 *   <li>Sea of Devils — devil beasts, low flora.</li>
 *   <li>Ancient God Battlefield — ancient apex, no flora.</li>
 *   <li>Suzaku Tomb — sealed apex, no fauna.</li>
 *   <li>Zhao Country — mortal population, low Qi.</li>
 *   <li>Cloud Sea — sky beasts, cloud lotus flora.</li>
 * </ol>
 */
public final class EcosystemSeeder {

    private EcosystemSeeder() {}

    public static void seed() {
        seed("mosquito_valley",          0.45,  200, 60, 10, 1,  0,   0);
        seed("heng_yue_mountain",        0.55,  120, 30, 6,  0,  80,  10);
        seed("sea_of_devils",            0.65,   40, 20, 40, 2,  20,  5);
        seed("ancient_god_battlefield",  0.75,   30, 10, 20, 5,  0,   0);
        seed("suzaku_tomb",              0.85,    0,  0, 0,  1,  0,   0);
        seed("zhao_country",             0.15,   80, 40, 4,  0,  5000,20);
        seed("cloud_sea",                0.70,   60, 20, 10, 0, 50,  15);

        Ergenverse.LOGGER.info("[Ergenverse] EcosystemSeeder seeded 7 RI-location ecosystems.");
    }

    private static void seed(String name, double qi, double flora, double herb,
                              double pred, double apex, double sectCult, double merch) {
        UUID id = UUID.nameUUIDFromBytes(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        CausalEcology.Ecosystem eco = new CausalEcology.Ecosystem(
                id, name, qi, flora, herb, pred, apex, sectCult, merch);
        // Pre-seed species lists so the trophic web is named, not just numeric.
        switch (name) {
            case "mosquito_valley":
                eco.floraSpecies.add("spirit_grass");
                eco.floraSpecies.add("blood_forgetting_grass");
                eco.herbivoreSpecies.add("spirit_rabbit");
                eco.predatorSpecies.add("spirit_wolf");
                eco.apexSpecies.add("mosquito_swarm");
                break;
            case "heng_yue_mountain":
                eco.floraSpecies.add("qi_gathering_grass");
                eco.floraSpecies.add("sword_edge_moss");
                eco.herbivoreSpecies.add("spirit_deer");
                eco.predatorSpecies.add("spirit_hawk");
                break;
            case "sea_of_devils":
                eco.floraSpecies.add("poison_grass");
                eco.herbivoreSpecies.add("devil_toad");
                eco.predatorSpecies.add("devil_python");
                eco.apexSpecies.add("devil_lord");
                break;
            case "ancient_god_battlefield":
                eco.predatorSpecies.add("ancient_remnant");
                eco.apexSpecies.add("ancient_god_fragment");
                break;
            case "suzaku_tomb":
                eco.apexSpecies.add("suzaku_fragment");
                break;
            case "zhao_country":
                eco.floraSpecies.add("mortal_rice");
                eco.herbivoreSpecies.add("mortal_cattle");
                eco.predatorSpecies.add("mortal_wolf");
                break;
            case "cloud_sea":
                eco.floraSpecies.add("cloud_lotus");
                eco.herbivoreSpecies.add("cloud_deer");
                eco.predatorSpecies.add("sky_hawk");
                break;
            default:
                break;
        }
        CausalEcology.register(eco);
    }
}
