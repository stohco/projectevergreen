package dev.ergenverse.canon.structure;

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
 * VolumePlacer — the chunk-filtered, provenance-aware sink for block placements
 * during structure materialization.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>This is the bridge between the {@code canon.structure} package (lore-only,
 * no Minecraft world imports) and the existing CRON-62/63/69/72/104 chunk-scoped
 * builder architecture. A {@link CanonObject} (e.g. a {@link CanonFurniture})
 * emits block placements into a {@code VolumePlacer}; the placer applies the
 * same two guards the monolithic {@code WangFamilyVillageBuilder.sb()} applied:
 *
 * <ol>
 *   <li><b>Chunk filter</b> (CRON-62): if a {@link ChunkBounds} is set,
 *       placements whose (x, z) fall outside the bounds are skipped.</li>
 *   <li><b>Provenance-aware rebuild guard</b> (CRON-63): if a PLAYER or
 *       SIMULATION delta exists at the target position, the CANON placement is
 *       skipped. The player's edits take priority over CANON.</li>
 * </ol>
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
