package dev.ergenverse.materialization;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * VolumePlacer — the chunk-filtered, provenance-aware sink for
 * {@code setBlock} calls during voxel materialization.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER</b>
 *
 * <p>This is the final hop in the pipeline: {@link VoxelMaterializer} resolves
 * each {@link dev.ergenverse.assembly.VoxelInstruction} into a
 * {@code BlockState} via {@link MaterialResolver} and hands it here. The placer
 * applies the same two guards the legacy monolithic
 * {@code WangFamilyVillageBuilder.sb()} applied:
 *
 * <ol>
 *   <li><b>Chunk filter</b> (CRON-62): if a {@link ChunkBounds} is set,
 *       placements whose (x, z) fall outside the bounds are skipped — the
 *       settlement IR is assembled in full but only the current chunk is
 *       written.</li>
 *   <li><b>Provenance-aware rebuild guard</b> (CRON-63): if a PLAYER or
 *       SIMULATION delta exists at the target position, the CANON placement is
 *       skipped. The player's edits take priority over CANON.</li>
 * </ol>
 *
 * <p>CRON-125 placed this class in {@code canon.structure}; CRON-127 moves it
 * here ({@code materialization}) because it references {@code ServerLevel} and
 * {@code BlockState} — Minecraft types that must not appear in the canon layer.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface VolumePlacer {

    void placeBlock(BlockPos pos, BlockState state, int flags);

    default void placeBlock(BlockPos pos, BlockState state) {
        placeBlock(pos, state, 2);
    }

    @Nullable
    ChunkBounds bounds();

    ServerLevel level();

    default boolean contains(int x, int z) {
        ChunkBounds b = bounds();
        return b == null || b.contains(x, z);
    }

    static VolumePlacer forChunk(ServerLevel level, @Nullable ChunkBounds bounds) {
        return new ChunkFilteredPlacer(level, bounds);
    }

    final class ChunkFilteredPlacer implements VolumePlacer {
        private final ServerLevel level;
        private final ChunkBounds bounds;

        ChunkFilteredPlacer(ServerLevel level, @Nullable ChunkBounds bounds) {
            if (level == null) throw new IllegalArgumentException("level");
            this.level = level;
            this.bounds = bounds;
        }

        @Override
        public void placeBlock(BlockPos pos, BlockState state, int flags) {
            if (bounds != null && !bounds.contains(pos.getX(), pos.getZ())) return;
            if (hasPlayerOrSimulationDelta(pos)) return;
            level.setBlock(pos, state, flags);
        }

        @Override
        @Nullable
        public ChunkBounds bounds() {
            return bounds;
        }

        @Override
        public ServerLevel level() {
            return level;
        }

        private static boolean hasPlayerOrSimulationDelta(BlockPos pos) {
            try {
                WorldRuntime runtime = WorldRuntime.get();
                if (!runtime.isInitialized()) return false;
                WorldDeltaStore store = runtime.deltaStore();
                int x = pos.getX(), y = pos.getY(), z = pos.getZ();
                if (store.hasBlock(x, y, z, Provenance.PLAYER)
                        || store.hasBlock(x, y, z, Provenance.SIMULATION)) return true;
                if (store.hasEntityPlacement(x, y, z, Provenance.PLAYER)
                        || store.hasEntityPlacement(x, y, z, Provenance.SIMULATION)) return true;
                return false;
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] VolumePlacer: provenance guard failed at {}: {} — proceeding.",
                        pos, t.getMessage());
                return false;
            }
        }
    }
}
