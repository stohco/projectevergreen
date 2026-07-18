package dev.ergenverse.perception;

import dev.ergenverse.core.WorldPhilosophy;

/**
 * Objective Reality — the foundation of perception.
 *
 * <p>Per the {@link WorldPhilosophy}: "Reality is objective. The world
 * exists independently of the player. The difference [between observers]
 * isn't what they see. It's what they understand."
 *
 * <p>Every observable thing in the world has an <b>objective nature</b>
 * — a true state that exists regardless of any observer. A Fifth Rank
 * spirit rabbit <em>is</em> a Fifth Rank spirit rabbit. A mortal sees
 * it (it's there). A Qi Condensation cultivator sees it. Wang Lin sees
 * it. The rabbit does not change.
 *
 * <p>What changes is what the observer <b>understands</b> about it.
 * Understanding is a function of the observer's perception tier, their
 * Dao affinities (a beast tamer notices bloodline purity that a regular
 * cultivator misses), and their prior knowledge.
 *
 * <p>This interface is implemented by anything that can be observed.
 * The implementation returns a {@link PerceptionResult} describing what
 * a given observer understands. The object itself is not mutated by
 * being observed.
 */
public interface Objective {

    /**
     * The objective nature of this thing. Always the same, regardless
     * of observer. This is the rabbit's true rank, true species, true
     * bloodline, true karmic history.
     */
    ObjectiveNature nature();

    /**
     * What an observer at the given perception tier understands about
     * this thing.
     *
     * <p>This method does not mutate the object. It returns a description
     * of the observer's <b>interpretation</b>, which becomes more complete
     * as perception tier rises. The object's true nature is unchanged.
     *
     * <p>Example — a 5th-rank Spirit Rabbit:
     * <ul>
     *   <li>Mortal: "That's the biggest rabbit I've ever seen..."</li>
     *   <li>Qi Condensation: "Spirit Beast. Leave. Now."</li>
     *   <li>Beast tamer (Foundation, beast-Dao): "Its bloodline is unusually pure."</li>
     *   <li>Fourth Step: "Its ancestor lineage traces back to an extinct divine hare."</li>
     * </ul>
     *
     * <p>The rabbit was the same rabbit in all four cases. What changed
     * was the observer's understanding.
     */
    PerceptionResult perceive(ObserverContext observer);
}
