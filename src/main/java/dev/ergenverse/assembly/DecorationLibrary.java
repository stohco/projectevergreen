package dev.ergenverse.assembly;

import java.util.ArrayList;
import java.util.List;

/**
 * DecorationLibrary — translates open-area features (trees, fences, roads,
 * path lights) into <b>relative</b> {@link VoxelInstruction}s.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is one of the user's "four independent libraries":
 * <blockquote>
 *   Decoration Library: Poor Village → Fences, Grass, Bushes, Paths. Those are
 *   separate problems [from furniture/building/terrain].
 * </blockquote>
 *
 * <p>The library is the <b>only</b> place that knows a "spirit tree" is four
 * oak logs topped with a 3×3 + 1 cap of spirit-wood leaves, or that a "road"
 * is a line of spirit-sand voxels. Open features in
 * {@link dev.ergenverse.canon.structure.CanonSettlement} are pure semantic
 * declarations ({@code FeatureType.SPIRIT_TREE, dx, dy, dz, span, orientation});
 * this library gives them geometry.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class DecorationLibrary {

    private DecorationLibrary() {}

    /**
     * Returns the voxel geometry for an open feature, relative to the feature's
     * declared origin (dx, dy, dz).
     *
     * @param type        the semantic feature type
     * @param span        the length (for fences / roads)
     * @param orientation the cardinal direction the span extends
     */
    public static List<VoxelInstruction> voxels(dev.ergenverse.canon.structure.CanonSettlement.FeatureType type,
                                                int span,
                                                dev.ergenverse.canon.structure.CanonSettlement.Orientation orientation) {
        List<VoxelInstruction> out = new ArrayList<>();
        switch (type) {
            case SPIRIT_TREE -> addSpiritTree(out);
            case FENCE -> addFence(out, span, orientation);
            case ROAD -> addRoad(out, span, orientation);
            case PATH_LIGHT -> addPathLight(out);
        }
        return out;
    }

    private static void addSpiritTree(List<VoxelInstruction> out) {
        for (int i = 0; i < 4; i++) {
            out.add(VoxelInstruction.at(0, i, 0, MaterialID.OAK_LOG, VoxelLayer.FLORA));
        }
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                out.add(VoxelInstruction.at(dx, 4, dz, MaterialID.SPIRIT_WOOD_LEAVES, VoxelLayer.FLORA));
            }
        }
        out.add(VoxelInstruction.at(0, 5, 0, MaterialID.SPIRIT_WOOD_LEAVES, VoxelLayer.FLORA));
    }

    private static void addFence(List<VoxelInstruction> out, int span,
                                 dev.ergenverse.canon.structure.CanonSettlement.Orientation o) {
        for (int i = 0; i < span; i++) {
            out.add(VoxelInstruction.at(stepX(o, i), 0, stepZ(o, i), MaterialID.OAK_FENCE, VoxelLayer.PATH));
        }
    }

    private static void addRoad(List<VoxelInstruction> out, int span,
                                dev.ergenverse.canon.structure.CanonSettlement.Orientation o) {
        for (int i = 0; i < span; i++) {
            out.add(VoxelInstruction.at(stepX(o, i), 0, stepZ(o, i), MaterialID.SPIRIT_SAND, VoxelLayer.PATH));
        }
    }

    private static void addPathLight(List<VoxelInstruction> out) {
        out.add(VoxelInstruction.at(0, 0, 0, MaterialID.OAK_FENCE, VoxelLayer.PATH));
        out.add(VoxelInstruction.at(0, 1, 0, MaterialID.SPIRIT_VEIN_STONE, VoxelLayer.PATH));
    }

    private static int stepX(dev.ergenverse.canon.structure.CanonSettlement.Orientation o, int i) {
        return switch (o) {
            case EAST -> i;
            case WEST -> -i;
            default -> 0;
        };
    }

    private static int stepZ(dev.ergenverse.canon.structure.CanonSettlement.Orientation o, int i) {
        return switch (o) {
            case SOUTH -> i;
            case NORTH -> -i;
            default -> 0;
        };
    }
}
