package dev.ergenverse.assembly;

import java.util.ArrayList;
import java.util.List;

/**
 * TerrainLibrary — generates subterranean / terrain voxel features.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is one of the user's "four independent libraries":
 * <blockquote>
 *   Terrain Library: Spirit Vein → Rock Formation → Ore Layout.
 * </blockquote>
 *
 * <p>Terrain features differ from furniture/buildings/decoration because they
 * are <b>subterranean</b> and <b>procedurally extendable</b> — a spirit vein
 * beneath the village well runs downward through rock, not along a room grid.
 * The canon declares <i>that</i> a spirit vein exists beneath the well; this
 * library declares <i>how deep</i> and in <i>what pattern</i> it runs.
 *
 * <p>CRON-127 ships a simple vertical spirit-vein column — a pillar of
 * {@link MaterialID#SPIRIT_VEIN_STONE} descending {@code depth} blocks below
 * the origin. Future versions will add branching veins and ore pockets per the
 * user's "Rock Formation → Ore Layout" vision.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class TerrainLibrary {

    private TerrainLibrary() {}

    /**
     * Returns a vertical spirit-vein column of {@code depth} voxels, starting
     * one block below the origin (y = -1) and descending. Relative to (0, 0, 0).
     */
    public static List<VoxelInstruction> spiritVeinColumn(int depth) {
        List<VoxelInstruction> out = new ArrayList<>(depth);
        for (int i = 0; i < depth; i++) {
            out.add(VoxelInstruction.at(0, -1 - i, 0, MaterialID.SPIRIT_VEIN_STONE, VoxelLayer.FOUNDATION));
        }
        return out;
    }
}
