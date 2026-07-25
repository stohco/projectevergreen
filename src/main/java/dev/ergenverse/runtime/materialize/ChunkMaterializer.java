package dev.ergenverse.runtime.materialize;

import dev.ergenverse.runtime.WorldRuntime;

/**
 * ChunkMaterializer — the contract for materializing a chunk from the blueprint.
 *
 * <p><b>Architectural directive (2026-07-25):</b> "I'd stop thinking about chunk
 * generation entirely. Instead imagine this interface:
 * <pre>
 *   interface ChunkMaterializer {
 *       void materialize(ChunkPos pos, WorldRuntime runtime, ChunkAccess chunk);
 *   }
 * </pre>
 * When Minecraft requests chunk (12, -5):
 * <pre>
 *   Chunk requested
 *     ↓
 *   Blueprint queried (via SpatialIndex)
 *     ↓
 *   Structures intersecting chunk
 *     ↓
 *   Terrain intersecting chunk
 *     ↓
 *   Simulation deltas
 *     ↓
 *   Chunk written
 * </pre>
 * There is no 'generator.' There is only a materializer. That's a very
 * different philosophy."
 *
 * <p>The ChunkMaterializer is the bridge between the canonical blueprint
 * (which is purely data) and Minecraft's chunk system (which is blocks).
 * When Minecraft requests a chunk, the materializer:
 * <ol>
 *   <li>Queries the blueprint's spatial index for all objects intersecting
 *       the chunk's bounding box (settlements, roads, spirit veins, etc.)</li>
 *   <li>Places terrain blocks (from the blueprint's terrain definition, or
 *       from the deterministic noise base layer)</li>
 *   <li>Places structure blocks (every block hand-authored at fixed coords)</li>
 *   <li>Applies simulation + player deltas (via {@link dev.ergenverse.runtime.DeltaManager})</li>
 *   <li>Returns the materialized chunk</li>
 * </ol>
 *
 * <p>This is fundamentally different from vanilla Minecraft's pipeline:
 * <pre>
 *   Vanilla:  Chunk → Noise → Biome → Decorators → Structures → Done
 *   Er Gen:   Chunk → Blueprint.query() → Terrain + Structures + Deltas → Done
 * </pre>
 *
 * <h2>Three-layer delta model (2026-07-26)</h2>
 * <p>The materializer uses the {@link dev.ergenverse.runtime.DeltaManager}
 * to compose three layers:
 * <pre>
 *   Layer 1 (Immutable): PlanetSuzakuBlueprint — canon, never changes
 *   Layer 2 (Mutable):   SimulationDelta — runtime simulation changes
 *   Layer 3 (Player):    PlayerDelta — player modifications
 * </pre>
 * Block resolution priority: PLAYER > SIMULATION > CANON.
 * <pre>
 *   Load Blueprint → Apply Simulation Changes → Apply Player Changes → Chunk ready
 * </pre>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface ChunkMaterializer {

    /**
     * Materialize a chunk from the blueprint + simulation deltas + player deltas.
     *
     * <p>Called by the chunk loading hook when Minecraft requests a chunk.
     * The materializer is responsible for placing every block that should
     * exist in this chunk according to the canonical blueprint, then applying
     * simulation and player deltas via the {@link dev.ergenverse.runtime.DeltaManager}.
     *
     * <p>The pipeline is:
     * <ol>
     *   <li>Query the spatial index for all canon locations intersecting the chunk</li>
     *   <li>For each location, place the hand-authored structure blocks (CANON layer)</li>
     *   <li>Apply simulation delta changes (SIMULATION layer — e.g. harvested herbs)</li>
     *   <li>Apply player delta changes (PLAYER layer — e.g. mined blocks, placed blocks)</li>
     * </ol>
     *
     * @param chunkX the chunk X coordinate (block coords / 16)
     * @param chunkZ the chunk Z coordinate (block coords / 16)
     * @param runtime the WorldRuntime (provides blueprint + spatial index + delta manager)
     * @param chunk the chunk access to write blocks into
     */
    void materialize(int chunkX, int chunkZ, WorldRuntime runtime, ChunkAccess chunk);

    /**
     * ChunkAccess — a minimal write interface for chunks.
     *
     * <p>This abstraction decouples the materializer from Minecraft's
     * internal chunk types. The adapter implementation wraps
     * {@code net.minecraft.world.level.chunk.ChunkAccess} and translates
     * {@link #setBlock} calls into Minecraft block state writes.
     */
    interface ChunkAccess {
        /**
         * Set a block in the chunk.
         *
         * @param localX X within the chunk (0-15)
         * @param y absolute Y coordinate
         * @param localZ Z within the chunk (0-15)
         * @param blockId the block to place (e.g. "ergenverse:spirit_stone",
         *                "minecraft:stone", "minecraft:air")
         * @return true if the block was set successfully
         */
        boolean setBlock(int localX, int y, int localZ, String blockId);

        /**
         * Get the chunk's X coordinate.
         */
        int getChunkX();

        /**
         * Get the chunk's Z coordinate.
         */
        int getChunkZ();
    }
}
