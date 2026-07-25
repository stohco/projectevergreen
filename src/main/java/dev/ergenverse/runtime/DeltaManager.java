package dev.ergenverse.runtime;

import java.util.List;
import java.util.Map;

/**
 * DeltaManager — composes the three-layer world model.
 *
 * <p><b>Architectural directive (2026-07-26):</b>
 * "I'd make the distinction explicit in code with a Delta Manager.
 * <pre>
 *   PlanetSuzakuBlueprint
 *           │
 *           ▼
 *   DeltaManager
 *           ├── SimulationDelta
 *           └── PlayerDelta
 *                  │
 *                  ▼
 *           World Materializer
 *                  │
 *                  ▼
 *           Minecraft Chunks
 * </pre>
 * The World Materializer never asks 'what should I generate?' It asks
 * 'what is the final state at this location after applying all layers?'"
 *
 * <p>The three-layer model:
 * <pre>
 *   Layer 1 (Immutable): PlanetSuzakuBlueprint — canon, never changes
 *   Layer 2 (Mutable):   SimulationDelta — runtime simulation changes
 *   Layer 3 (Player):    PlayerDelta — player modifications
 * </pre>
 *
 * <p>Block resolution priority: PLAYER > SIMULATION > CANON.
 * <pre>
 *   BlockState getBlock(pos) {
 *       if (playerDelta.exists(pos)) return playerDelta;
 *       if (simulationDelta.exists(pos)) return simulationDelta;
 *       return blueprint.get(pos);
 *   }
 * </pre>
 *
 * <h2>Why this matters</h2>
 * <p>This solves restarting: every new save starts from the same handcrafted
 * canon. Not "similar." Identical. But freedom remains — the player can
 * mine, build, destroy, create. Those changes persist in the player delta.
 * The blueprint is NEVER rewritten.
 *
 * <h2>Debugging</h2>
 * <p>You can compare Blueprint vs Current World to answer "Why is this
 * different?" Instead of wondering whether world generation or a bug caused
 * a discrepancy, you query:
 * <pre>
 *   BlockOwner owner = deltaManager.getOwner(pos);
 *   // CANON = unchanged, SIMULATION = beast/weather/sect changed it,
 *   // PLAYER = the player changed it
 * </pre>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class DeltaManager {

    private final PlanetSuzakuBlueprint blueprint;
    private final SimulationDelta simulationDelta;
    private final PlayerDelta playerDelta;

    public DeltaManager(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
        this.simulationDelta = new SimulationDelta();
        this.playerDelta = new PlayerDelta();
    }

    /**
     * Get the final block state at a position, applying all three layers.
     *
     * <p>Priority: PLAYER > SIMULATION > CANON.
     *
     * @param x block X coordinate
     * @param y block Y coordinate
     * @param z block Z coordinate
     * @return the block state string (e.g. "minecraft:air", "ergenverse:spirit_stone"),
     *         or null if the blueprint doesn't define this position (vanilla terrain)
     */
    public String getBlock(int x, int y, int z) {
        // Layer 3: Player delta (highest priority)
        String playerState = playerDelta.get(x, y, z);
        if (playerState != null) return playerState;

        // Layer 2: Simulation delta
        String simState = simulationDelta.get(x, y, z);
        if (simState != null) return simState;

        // Layer 1: Canon blueprint (or null = not in blueprint = vanilla terrain)
        return blueprint.getBlock(x, y, z);
    }

    /**
     * Get the owner of the block at a position.
     *
     * @return PLAYER if the player changed it, SIMULATION if the simulation
     *         changed it, CANON if unchanged (or not in blueprint)
     */
    public BlockOwner getOwner(int x, int y, int z) {
        if (playerDelta.exists(x, y, z)) return BlockOwner.PLAYER;
        if (simulationDelta.exists(x, y, z)) return BlockOwner.SIMULATION;
        return BlockOwner.CANON;
    }

    /**
     * Record a simulation-caused block change.
     *
     * <p>Called when the simulation reshapes the world:
     * <ul>
     *   <li>A beast harvests an herb</li>
     *   <li>A storm damages a roof</li>
     *   <li>A sect expands its walls</li>
     *   <li>A spirit vein is depleted</li>
     * </ul>
     */
    public void setSimulationBlock(int x, int y, int z, String blockId) {
        simulationDelta.set(x, y, z, blockId);
    }

    /**
     * Record a player-caused block change.
     *
     * <p>Called when the player mines, places, or destroys a block.
     */
    public void setPlayerBlock(int x, int y, int z, String blockId) {
        playerDelta.set(x, y, z, blockId);
    }

    /**
     * Get all changed positions in a chunk, for chunk materialization.
     *
     * <p>Returns a list of (packedPos, blockState, owner) tuples for all
     * positions in the given chunk that have been changed by simulation
     * or player. The ChunkMaterializer applies these after placing the
     * blueprint's canon blocks.
     *
     * @param chunkX chunk X coordinate (block coords / 16)
     * @param chunkZ chunk Z coordinate (block coords / 16)
     * @return list of changed positions in the chunk
     */
    public List<ChangedBlock> getChunkDeltas(int chunkX, int chunkZ) {
        List<ChangedBlock> result = new java.util.ArrayList<>();
        int minBlockX = chunkX * 16;
        int minBlockZ = chunkZ * 16;
        int maxBlockX = minBlockX + 15;
        int maxBlockZ = minBlockZ + 15;

        // Scan simulation delta
        for (Map.Entry<Long, String> entry : simulationDelta.allChanges().entrySet()) {
            long packed = entry.getKey();
            int x = BlockDelta.unpackX(packed);
            int z = BlockDelta.unpackZ(packed);
            if (x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ) {
                int y = BlockDelta.unpackY(packed);
                result.add(new ChangedBlock(x, y, z, entry.getValue(), BlockOwner.SIMULATION));
            }
        }

        // Scan player delta
        for (Map.Entry<Long, String> entry : playerDelta.allChanges().entrySet()) {
            long packed = entry.getKey();
            int x = BlockDelta.unpackX(packed);
            int z = BlockDelta.unpackZ(packed);
            if (x >= minBlockX && x <= maxBlockX && z >= minBlockZ && z <= maxBlockZ) {
                int y = BlockDelta.unpackY(packed);
                result.add(new ChangedBlock(x, y, z, entry.getValue(), BlockOwner.PLAYER));
            }
        }

        return result;
    }

    /** The simulation delta (for serialization). */
    public SimulationDelta simulationDelta() { return simulationDelta; }

    /** The player delta (for serialization). */
    public PlayerDelta playerDelta() { return playerDelta; }

    /** The blueprint (immutable canon). */
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }

    /**
     * A changed block — position, state, and owner.
     */
    public static final class ChangedBlock {
        public final int x, y, z;
        public final String blockState;
        public final BlockOwner owner;

        ChangedBlock(int x, int y, int z, String blockState, BlockOwner owner) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockState = blockState;
            this.owner = owner;
        }
    }
}
