package dev.ergenverse.wanglin.bead;

/**
 * The natural capacity growth model for the Heaven-Defying Bead.
 *
 * <p><b>User's directive:</b> "Storage capacity should grow naturally.
 * Not: Level 1: 100 slots, Level 2: 200 slots. That's game logic.
 * Instead: Current spatial stability + bead restoration + owner authority
 * + internal world growth determines capacity."
 *
 * <p>This model takes four factors and produces a storage slot count
 * and interior stage. The factors are driven by in-game events
 * (aligning element fragments, gaining spirit recognition), not by XP.
 *
 * <h2>The four factors</h2>
 * <ul>
 *   <li><b>Spatial stability</b> (0..1) — the internal integrity of the
 *       bead's spatial structure. Grows as the bead is restored and
 *       stabilized. Canon: the bead's space was unstable early on and
 *       solidified as Wang Lin aligned the elements.</li>
 *   <li><b>Bead restoration</b> (0..1) — how many of the 9 Parts
 *       (core + 5 elements + 3 hidden) have been aligned. This is the
 *       primary driver. Canon: "Once Five Elements perfected, the master
 *       truly owns it."</li>
 *   <li><b>Owner authority</b> (0..1) — the sentience level, derived
 *       from the bead's recognition of the owner. Canon: the bead
 *       recognizes Wang Lin only after he proves worthy.</li>
 *   <li><b>Interior growth</b> (0..1) — how developed the interior
 *       world is. This is partly a consequence of the other three
 *       factors and partly driven by time and use.</li>
 * </ul>
 */
public final class BeadCapacityModel {

    private BeadCapacityModel() {}

    /**
     * Calculate the current storage slot count.
     *
     * <p>The formula produces natural-feeling growth:
     * - All factors at 0: 0 slots (dormant stone)
     * - Restoration 1/9 (core only): 9 slots
     * - Restoration 6/9 (all 5 elements): ~54 slots
     * - All factors near 1.0: ~216 slots (a large ecosystem)
     *
     * @param stability   spatial stability 0..1
     * @param restoration parts aligned / 9 (0..1)
     * @param authority    owner authority 0..1
     * @param interior     interior growth 0..1
     * @return storage slot count
     */
    public static int storageSlots(double stability, double restoration,
                                    double authority, double interior) {
        // Base slots from the interior stage
        BeadInteriorStage stage = stageFor(stability, restoration, authority, interior);
        int baseSlots = stage.baseSlots;

        // Bonus slots from other factors (up to 50% more)
        double bonusFactor = 1.0
                + stability * 0.15
                + authority * 0.15
                + interior * 0.20;

        return (int) (baseSlots * bonusFactor);
    }

    /**
     * Determine the bead's interior development stage.
     *
     * <p>The stage is primarily driven by restoration (parts aligned)
     * and secondarily by the other factors. A bead with 1/9 parts
     * aligned but high authority might still be at SMALL_SPACE.
     * A bead with 6/9 parts and good growth reaches VALLEY.
     */
    public static BeadInteriorStage stageFor(double stability, double restoration,
                                               double authority, double interior) {
        double score = restoration * 0.50
                     + stability * 0.15
                     + authority * 0.15
                     + interior * 0.20;

        if (score < 0.05) return BeadInteriorStage.DORMANT_STONE;
        if (score < 0.15) return BeadInteriorStage.CRACK_OPENED;
        if (score < 0.35) return BeadInteriorStage.SMALL_SPACE;
        if (score < 0.60) return BeadInteriorStage.VALLEY;
        if (score < 0.85) return BeadInteriorStage.SMALL_WORLD;
        return BeadInteriorStage.COMPLETE_ECOSYSTEM;
    }

    /**
     * Calculate time dilation factor. 1.0 = normal, 10.0 = 10x.
     *
     * <p>Canon: the bead's time dilation is always 10x when active.
     * But early on, the time domain isn't accessible. This returns
     * the effective dilation (1.0 = no dilation, 10.0 = full).
     */
    public static double timeDilationFactor(BeadInteriorStage stage) {
        return stage.hasTimeDilation ? 10.0 : 1.0;
    }
}