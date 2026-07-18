package dev.ergenverse.perception;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.cultivation.RealmId;

import java.util.Map;
import java.util.Set;

/**
 * Demonstration of the corrected perception system.
 *
 * <p>This class proves that the rabbit example from the Prime Directive
 * works as specified: the rabbit never changes; what changes is what
 * the observer understands.
 *
 * <p>Run this in a unit test or via a command to see the difference
 * between observers. The output should be:
 *
 * <pre>
 *   Mortal sees:        "a large spirit rabbit" — "That's the biggest spirit rabbit..."
 *   Qi Condensation:    "Spirit Beast (Spirit Rabbit)" — "Spirit Beast. Leave. Now."
 *   Beast Tamer (Fd):   "Spirit Rabbit, Rank 5" — "bloodline is unusually pure"
 *   Nascent Soul:       "Spirit Rabbit, Rank 5 (Divine Hare)" — full karmic history
 *   Transcendence:      "Spirit Rabbit — cosmic echo" — Root Dao web
 * </pre>
 *
 * <p>The Spirit Rabbit objective is the SAME object in all five cases.
 * It is not mutated. It does not "become" a Spirit Rabbit at Qi
 * Condensation — it was always a Spirit Rabbit. The observer's
 * understanding changes, that's all.
 */
public final class PerceptionDemo {

    private PerceptionDemo() {}

    /** The objective Fifth-Rank Spirit Rabbit — the same rabbit for all observers. */
    public static final Objective SPIRIT_RABBIT = new Objective() {
        @Override
        public ObjectiveNature nature() {
            return ObjectiveNature.spiritBeast(
                "Spirit Rabbit", "灵兔", 5, RealmId.SOUL_FORMATION,
                "Divine Hare (extinct)",
                "descended from Meng Hao's Paragon Spirit Deer",
                "killed three cultivators who tried to harvest it; spared by a Nascent Soul who passed by",
                CanonEngine.Confidence.WIKI_BACKED,
                "ISSTH — Paragon Spirit Deer lineage"
            );
        }
        @Override
        public PerceptionResult perceive(ObserverContext observer) {
            return PerceptionEngine.perceive(this, observer);
        }
    };

    /** Run the demo. Returns the five readings as an array. */
    public static PerceptionResult[] runDemo() {
        PerceptionResult[] results = new PerceptionResult[5];

        // 1. A mortal — sees a "big rabbit," doesn't understand
        results[0] = PerceptionEngine.perceive(SPIRIT_RABBIT, ObserverContext.mortal());

        // 2. A Qi Condensation cultivator — recognizes spirit beast, can't tell rank
        results[1] = PerceptionEngine.perceive(SPIRIT_RABBIT, ObserverContext.at(RealmId.QI_CONDENSATION));

        // 3. A Foundation beast-tamer — recognizes bloodline purity
        results[2] = PerceptionEngine.perceive(SPIRIT_RABBIT, new ObserverContext(
            RealmId.FOUNDATION, Map.of("beast", 0.7), false, Set.of(),
            1.0, true, false, false
        ));

        // 4. A Nascent Soul with divine sense — sees karmic history
        results[3] = PerceptionEngine.perceive(SPIRIT_RABBIT, new ObserverContext(
            RealmId.NASCENT_SOUL, Map.of("beast", 0.5, "karma", 0.3),
            true, Set.of(), 1.0, false, false, false
        ));

        // 5. A Transcendent — sees the cosmic significance
        results[4] = PerceptionEngine.perceive(SPIRIT_RABBIT, ObserverContext.at(RealmId.TRANSCENDENCE));

        return results;
    }

    /**
     * Print the demo to a logger.
     *
     * <p>Useful for in-game command {@code /ergen perceive demo} to verify
     * the system is working.
     */
    public static void printDemo(java.util.function.Consumer<String> logger) {
        String[] labels = {
            "MORTAL", "QI_CONDENSATION", "FOUNDATION_BEAST_TAMER",
            "NASCENT_SOUL_DIVINE_SENSE", "TRANSCENDENCE"
        };
        PerceptionResult[] results = runDemo();

        logger.accept("=== Perception Demo: The Fifth-Rank Spirit Rabbit ===");
        logger.accept("The rabbit is the SAME objective thing in all five cases.");
        logger.accept("What changes is what the observer UNDERSTANDS.");
        logger.accept("");

        for (int i = 0; i < results.length; i++) {
            PerceptionResult r = results[i];
            logger.accept(String.format("[%s] perceivedName: %s", labels[i], r.perceivedName));
            logger.accept(String.format("[%s] description: %s", labels[i], r.perceivedDescription));
            logger.accept(String.format("[%s] recognizedRank: %s | canInteract: %s | concealed: %s",
                labels[i],
                r.recognizedRank == null ? "(can't tell)" : r.recognizedRank,
                r.canInteract, r.concealed));
            logger.accept("");
        }
    }
}
