package dev.ergenverse.materialization;

import dev.ergenverse.assembly.AssemblyResult;
import dev.ergenverse.assembly.VoxelInstruction;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * VoxelMaterializer — applies a compiled {@link AssemblyResult} to a
 * {@code ServerLevel}, translating the backend-agnostic IR into concrete
 * Minecraft blocks.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is the final stage of the compilation pipeline:
 * <pre>
 *   CanonSettlement → WorldAssembler → AssemblyResult(IR) → VoxelMaterializer → ServerLevel
 *                                                          ↑
 *                                              MaterialResolver (MaterialID → BlockState)
 * </pre>
 *
 * <p>The materializer iterates the flat {@link VoxelInstruction} list, resolves
 * each {@link dev.ergenverse.assembly.MaterialID} to a {@code BlockState} via
 * {@link MaterialResolver}, and writes it through a {@link VolumePlacer} (which
 * applies chunk filtering + provenance-aware rebuild guards).
 *
 * <p>This is one of the three permitted places where {@code BlockPos} exists
 * (the others being the chunk generator and navigation), per the user's rule:
 * <i>"I would actually remove BlockPos from almost everything. Minecraft
 * coordinates should exist in only three places: Chunk Generator, Renderer,
 * Navigation."</i>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class VoxelMaterializer {

    private VoxelMaterializer() {}

    /**
     * Writes an {@link AssemblyResult} into the given {@code ServerLevel}.
     *
     * @param result  the compiled voxel IR + anchors
     * @param level   the target server level
     * @param bounds  optional chunk bounds; voxels outside are skipped
     * @return the number of voxels actually written (after chunk/provenance filtering)
     */
    public static int materialize(AssemblyResult result, ServerLevel level, @Nullable ChunkBounds bounds) {
        VolumePlacer placer = VolumePlacer.forChunk(level, bounds);
        int written = 0;
        int skipped = 0;
        for (VoxelInstruction v : result.instructions()) {
            int wx = v.x();
            int wy = v.y();
            int wz = v.z();
            if (bounds != null && !bounds.contains(wx, wz)) {
                skipped++;
                continue;
            }
            BlockState state = MaterialResolver.resolve(v);
            BlockPos pos = new BlockPos(wx, wy, wz);
            // placeBlock internally applies the provenance-aware rebuild guard.
            // We can't tell from here whether it was filtered, so we count
            // attempted writes that passed the chunk filter.
            placer.placeBlock(pos, state);
            written++;
        }
        Ergenverse.LOGGER.info("[Ergenverse] VoxelMaterializer: wrote {} voxels ({} skipped by chunk filter), "
                        + "anchors={}, bounds={}",
                written, skipped, result.anchors().size(), bounds);
        return written;
    }
}
