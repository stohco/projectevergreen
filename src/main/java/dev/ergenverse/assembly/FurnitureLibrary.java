package dev.ergenverse.assembly;

import dev.ergenverse.canon.structure.CanonFurniture;
import java.util.ArrayList;
import java.util.List;

/**
 * FurnitureLibrary — translates a semantic {@link CanonFurniture} kind into a
 * list of <b>relative</b> {@link VoxelInstruction}s.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is one of the user's "four independent libraries":
 * <blockquote>
 *   I'd actually have four independent libraries — Furniture Library, Building
 *   Library, Terrain Library, Decoration Library — because they evolve
 *   differently. Furniture Library: BED → Voxel Model.
 * </blockquote>
 *
 * <p>The library is the <b>only</b> place that knows what voxels make up a
 * "Sleeping Mat" (a single white-carpet voxel) or an "Alchemy Furnace" (a
 * single alchemy-furnace voxel). The canon layer declares <i>that</i> a room
 * contains a meditation mat; this library declares <i>how</i> a meditation mat
 * is shaped. Redesigning all meditation mats means editing one switch arm —
 * no canon data changes.
 *
 * <h2>The Intent seam (future)</h2>
 *
 * <p>Per the user's vision, the same semantic furniture can be rendered
 * differently by realm: a poor mortal's "Meditation Mat" is a gray carpet; a
 * Core Formation elder's is a spirit-vein-stone dais. The future API will
 * accept an {@link dev.ergenverse.canon.structure.Intent} + realm hint to
 * select the template. For now, CRON-127 ships the "poor village" templates —
 * the only realm the mod currently materializes.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class FurnitureLibrary {

    private FurnitureLibrary() {}

    /**
     * Returns the voxel geometry for {@code furniture}, relative to its origin
     * (0, 0, 0). The caller ({@link WorldAssembler}) translates these to the
     * furniture's placement offset.
     */
    public static List<VoxelInstruction> voxels(CanonFurniture furniture) {
        List<VoxelInstruction> out = new ArrayList<>();
        switch (furniture) {
            case SLEEPING_MAT -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.WHITE_CARPET, VoxelLayer.FURNITURE));

            case HIDDEN_STORAGE -> {
                out.add(VoxelInstruction.at(0, 0, 0, MaterialID.TRAPPED_CHEST, VoxelLayer.FURNITURE));
                out.add(VoxelInstruction.at(0, 1, 0, MaterialID.WHITE_CARPET, VoxelLayer.FURNITURE));
            }

            case MEDITATION_MAT -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.GRAY_CARPET, VoxelLayer.FURNITURE));

            case BOOKSHELF -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.BOOKSHELF, VoxelLayer.FURNITURE));

            case LECTERN -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.LECTERN, VoxelLayer.FURNITURE));

            case ALCHEMY_FURNACE -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.ALCHEMY_FURNACE, VoxelLayer.FURNITURE));

            case LANTERN -> {
                out.add(VoxelInstruction.at(0, 1, 0, MaterialID.OAK_FENCE, VoxelLayer.FURNITURE));
                out.add(VoxelInstruction.at(0, 0, 0, MaterialID.LANTERN, VoxelLayer.FURNITURE));
            }

            case WORK_TABLE -> {
                out.add(VoxelInstruction.at(0, 0, 0, MaterialID.OAK_FENCE, VoxelLayer.FURNITURE));
                out.add(VoxelInstruction.at(0, 1, 0, MaterialID.WORK_TABLE_TOP, VoxelLayer.FURNITURE));
            }

            case STORAGE_CHEST -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.CHEST, VoxelLayer.FURNITURE));

            case SPIRIT_WELL -> {
                for (int i = 0; i < 3; i++) {
                    out.add(VoxelInstruction.at(0, i, 0, MaterialID.SPIRIT_VEIN_STONE, VoxelLayer.FURNITURE));
                }
            }

            case FORMATION_CORE -> out.add(VoxelInstruction.at(0, 0, 0, MaterialID.FORMATION_CORE_STONE, VoxelLayer.FURNITURE));

            case FARM_PLOT_CELL -> {
                out.add(VoxelInstruction.at(0, 0, 0, MaterialID.FARMLAND, VoxelLayer.FURNITURE));
                out.add(VoxelInstruction.at(0, 1, 0, MaterialID.QI_GATHERING_GRASS, VoxelLayer.FURNITURE));
            }
        }
        return out;
    }
}
