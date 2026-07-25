package dev.ergenverse.runtime;

/**
 * BlockOwner — the three-layer ownership model for world blocks.
 *
 * <p><b>Architectural directive (2026-07-26):</b> "I would actually separate
 * block ownership. Every block effectively belongs to one of three owners:
 * CANON, SIMULATION, PLAYER. When querying a block:
 * <pre>
 *   BlockState getBlock(pos) {
 *       if (playerDelta.exists(pos)) return playerDelta;
 *       if (simulationDelta.exists(pos)) return simulationDelta;
 *       return blueprint.get(pos);
 *   }
 * </pre>
 * Elegant. Deterministic."
 *
 * <p>The priority is: PLAYER > SIMULATION > CANON. This means:
 * <ul>
 *   <li>If the player modified a block, that wins.</li>
 *   <li>If the simulation modified a block (and the player didn't), that wins.</li>
 *   <li>Otherwise, the canon blueprint state applies.</li>
 * </ul>
 *
 * <p>This is essentially Git: the blueprint is the initial commit, simulation
 * and player changes are deltas on top. The blueprint NEVER changes.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public enum BlockOwner {
    /**
     * Layer 1 — Immutable canon. The PlanetSuzakuBlueprint defines what
     * exists at day 0. This never changes. If a block is CANON-owned,
     * no simulation or player delta has touched it.
     */
    CANON,

    /**
     * Layer 2 — Mutable simulation state. The simulation can reshape the
     * world: a beast harvests an herb, a storm damages a roof, a sect
     * expands its walls. These changes are tracked as SimulationDelta and
     * persist in the save. They NEVER modify the blueprint.
     */
    SIMULATION,

    /**
     * Layer 3 — Player changes. The player mines stone, cuts trees, builds
     * houses, destroys mountains. These changes are tracked as PlayerDelta
     * and persist in the save. They NEVER modify the blueprint or the
     * simulation delta.
     */
    PLAYER;

    /**
     * The priority order for block resolution. Higher ordinal = higher priority.
     * PLAYER (2) > SIMULATION (1) > CANON (0).
     */
    public boolean overrides(BlockOwner other) {
        return this.ordinal() > other.ordinal();
    }
}
