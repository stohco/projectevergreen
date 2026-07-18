package dev.ergenverse.ecology;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.core.WorldPhilosophy;

import java.util.*;

/**
 * Causal Ecology — every spirit beast exists because of an ecosystem.
 *
 * <p>Per the {@link WorldPhilosophy}: "Every spirit beast should exist
 * because of an ecosystem. Spirit Vein → High-Qi Plants → Spirit
 * Herbivores → Spirit Predators → Ancient Apex Beast → Sect patrols →
 * Cultivator hunting parties → Merchant caravans. If the player
 * exterminates all the herbivores, eventually predators starve, herbs
 * spread differently, sects notice, alchemists lose ingredients. The
 * ecosystem evolves."
 *
 * <h2>How it works</h2>
 *
 * <p>An {@link Ecosystem} is anchored to a spirit vein. Each spirit vein
 * produces Qi, which feeds {@link SpiritFlora} (spirit herbs), which
 * feeds {@link TrophicLevel#HERBIVORE herbivores}, which feeds
 * {@link TrophicLevel#PREDATOR predators}, which feeds
 * {@link TrophicLevel#APEX apex beasts}. Sects and merchants are
 * {@link TrophicLevel#HUMAN human} populations that depend on the
 * ecosystem (cultivators harvest herbs, alchemists need ingredients,
 * merchants trade them).
 *
 * <p>Each population has a size. Each tick (Minecraft day), the ecosystem
 * resolves:
 *
 * <ul>
 *   <li>Flora grows based on Qi and herbivore pressure.</li>
 *   <li>Herbivores grow based on flora and predator pressure.</li>
 *   <li>Predators grow based on herbivores and apex pressure.</li>
 *   <li>Apex beasts grow based on predators.</li>
 *   <li>Humans grow/shrink based on harvestable resources and beast
 *       threat.</li>
 * </ul>
 *
 * <p>If the player exterminates herbivores, predators starve (population
 * drops), herbs spread unchecked (flora population grows), sects notice
 * (quest hook), alchemists lose ingredients (economic effect). The
 * ecosystem responds causally, not by script.
 */
public final class CausalEcology {

    private static final Map<UUID, Ecosystem> ECOSYSTEMS = new HashMap<>();

    private CausalEcology() {}

    /** Register an ecosystem anchored to a spirit vein. */
    public static void register(Ecosystem ecosystem) {
        ECOSYSTEMS.put(ecosystem.id, ecosystem);
    }

    public static Ecosystem get(UUID id) {
        return ECOSYSTEMS.get(id);
    }

    public static Collection<Ecosystem> all() {
        return ECOSYSTEMS.values();
    }

    /**
     * Resolve one ecosystem tick (one Minecraft day).
     *
     * <p>This is called by the World Pulse. Each ecosystem's populations
     * are updated based on the trophic relationships.
     */
    public static void tickAll() {
        for (Ecosystem e : ECOSYSTEMS.values()) {
            e.tick();
        }
    }

    // ─── Ecosystem ────────────────────────────────────────────────────

    /**
     * A complete ecosystem anchored to a spirit vein.
     */
    public static final class Ecosystem {
        public final UUID id;
        public final String name;
        public final double spiritVeinQiOutput;  // Qi produced per tick

        // Populations (in arbitrary units — could be entity count, biomass, etc.)
        public double flora;          // spirit herbs / spirit grasses
        public double herbivores;     // spirit rabbits, spirit deer, etc.
        public double predators;      // spirit wolves, spirit hawks, etc.
        public double apex;           // ancient beast — usually 0 or 1
        public double sectCultivators; // sect members harvesting this area
        public double merchants;      // caravan traffic through this area

        public final List<String> floraSpecies = new ArrayList<>();
        public final List<String> herbivoreSpecies = new ArrayList<>();
        public final List<String> predatorSpecies = new ArrayList<>();
        public final List<String> apexSpecies = new ArrayList<>();

        // History (for the world-pulse to surface)
        public final List<String> recentEvents = new ArrayList<>();

        public Ecosystem(UUID id, String name, double spiritVeinQiOutput,
                         double flora, double herbivores, double predators, double apex,
                         double sectCultivators, double merchants) {
            this.id = id;
            this.name = name;
            this.spiritVeinQiOutput = spiritVeinQiOutput;
            this.flora = flora;
            this.herbivores = herbivores;
            this.predators = predators;
            this.apex = apex;
            this.sectCultivators = sectCultivators;
            this.merchants = merchants;
        }

        /**
         * Resolve one tick of the ecosystem.
         *
         * <p>Trophic model (Lotka-Volterra inspired, simplified):
         * <ul>
         *   <li>Flora grows based on Qi, limited by herbivore consumption.</li>
         *   <li>Herbivores grow based on flora, limited by predator consumption.</li>
         *   <li>Predators grow based on herbivores, limited by apex consumption.</li>
         *   <li>Apex grows slowly based on predators.</li>
         *   <li>Sect cultivators grow based on herb availability and apex threat.</li>
         *   <li>Merchants grow based on sect activity.</li>
         * </ul>
         */
        public void tick() {
            double prevFlora = flora;
            double prevHerb = herbivores;
            double prevPred = predators;
            double prevApex = apex;

            // Flora: grows with Qi, consumed by herbivores.
            double floraGrowth = spiritVeinQiOutput * 0.1;
            double floraConsumed = herbivores * 0.5;
            flora = Math.max(0, flora + floraGrowth - floraConsumed);

            // Herbivores: grow with flora, consumed by predators.
            double herbGrowth = flora * 0.05;
            double herbConsumed = predators * 0.3;
            herbivores = Math.max(0, herbivores + herbGrowth - herbConsumed);

            // Predators: grow with herbivores, consumed by apex.
            double predGrowth = herbivores * 0.02;
            double predConsumed = apex * 0.1;
            predators = Math.max(0, predators + predGrowth - predConsumed);

            // Apex: grows very slowly with predators.
            double apexGrowth = predators * 0.005;
            apex = Math.max(0, apex + apexGrowth);

            // Sect cultivators: grow with herb availability, flee from apex.
            double sectGrowth = (flora * 0.01) - (apex * 0.5);
            sectCultivators = Math.max(0, sectCultivators + sectGrowth);

            // Merchants: follow sect activity.
            merchants = Math.max(0, merchants + (sectCultivators * 0.02 - 0.01));

            // ── Detect collapses and booms ──
            detectEvents(prevFlora, prevHerb, prevPred, prevApex);
        }

        private void detectEvents(double prevFlora, double prevHerb, double prevPred, double prevApex) {
            // Herbivore collapse → predator starvation
            if (prevHerb > 10 && herbivores < 2) {
                recentEvents.add(0, "Herbivore population collapsed. Predators will starve within seasons.");
                Ergenverse.LOGGER.info("[Ergenverse] Ecology {}: herbivore collapse detected.", name);
            }
            // Flora boom (herbivores gone) → herbs spread
            if (prevHerb > 10 && herbivores < 2 && flora > prevFlora * 1.5) {
                recentEvents.add(0, "With herbivores gone, spirit herbs spread unchecked across the valley.");
            }
            // Predator collapse → herbivore boom
            if (prevPred > 5 && predators < 1) {
                recentEvents.add(0, "Predator population collapsed. Herbivores will boom.");
            }
            // Apex beast arrival → sects flee
            if (prevApex < 0.5 && apex >= 1) {
                recentEvents.add(0, "An apex beast has claimed this territory. Sect cultivators are evacuating.");
            }
            // Sect notice (low herbs)
            if (prevFlora > 20 && flora < 5) {
                recentEvents.add(0, "Sect alchemists report a shortage of spirit herbs from " + name + ".");
            }

            // Keep only recent 10 events
            while (recentEvents.size() > 10) recentEvents.remove(recentEvents.size() - 1);
        }

        /** Exterminate a population (player action or world event). */
        public void exterminate(TrophicLevel level, double amount) {
            switch (level) {
                case FLORA: flora = Math.max(0, flora - amount); break;
                case HERBIVORE: herbivores = Math.max(0, herbivores - amount); break;
                case PREDATOR: predators = Math.max(0, predators - amount); break;
                case APEX: apex = Math.max(0, apex - amount); break;
                case HUMAN_SECT: sectCultivators = Math.max(0, sectCultivators - amount); break;
                case HUMAN_MERCHANT: merchants = Math.max(0, merchants - amount); break;
            }
        }

        /** Get the population of a trophic level. */
        public double population(TrophicLevel level) {
            switch (level) {
                case FLORA: return flora;
                case HERBIVORE: return herbivores;
                case PREDATOR: return predators;
                case APEX: return apex;
                case HUMAN_SECT: return sectCultivators;
                case HUMAN_MERCHANT: return merchants;
                default: return 0;
            }
        }
    }

    /** Trophic levels in the ecosystem. */
    public enum TrophicLevel {
        FLORA("Spirit Flora"),
        HERBIVORE("Spirit Herbivore"),
        PREDATOR("Spirit Predator"),
        APEX("Ancient Apex Beast"),
        HUMAN_SECT("Sect Cultivator"),
        HUMAN_MERCHANT("Merchant Caravan");

        public final String label;
        TrophicLevel(String label) { this.label = label; }
    }
}
