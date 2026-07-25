package dev.ergenverse.runtime;

/**
 * SimulationDelta — Layer 2: mutable simulation state.
 *
 * <p><b>Contract (2026-07-26 directive):</b> "Once the game begins, everything
 * becomes alive. Old Chen moves. Wang Lin cultivates. Wolf pack migrates.
 * A herb gets harvested. Spirit beast reproduces. Road becomes dangerous.
 * Sect expands. Someone dies. Weather changes. Economy changes. These are
 * runtime deltas. They never modify the blueprint."
 *
 * <p>Examples of simulation changes:
 * <ul>
 *   <li>A spirit beast harvests an herb → the herb block becomes air (SIMULATION)</li>
 *   <li>A storm damages a roof → roof blocks become air (SIMULATION)</li>
 *   <li>A sect expands its walls → new wall blocks placed (SIMULATION)</li>
 *   <li>A cave-in collapses a tunnel → blocks placed (SIMULATION)</li>
 *   <li>A spirit vein is depleted → ore becomes stone (SIMULATION)</li>
 * </ul>
 *
 * <p>The simulation delta is saved with the world. On new save creation,
 * it is empty — every playthrough starts from pure canon.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class SimulationDelta extends BlockDelta {

    public SimulationDelta() {
        super(BlockOwner.SIMULATION);
    }

    /**
     * Record a simulation-caused block change.
     *
     * <p>Examples:
     * <ul>
     *   <li>Herb harvested by beast: set(x, y, z, "minecraft:air")</li>
     *   <li>Sect wall expanded: set(x, y, z, "ergenverse:spirit_stone_bricks")</li>
     *   <li>Spirit vein depleted: set(x, y, z, "minecraft:stone")</li>
     * </ul>
     */
    @Override
    public void set(int x, int y, int z, String blockId) {
        super.set(x, y, z, blockId);
    }
}
