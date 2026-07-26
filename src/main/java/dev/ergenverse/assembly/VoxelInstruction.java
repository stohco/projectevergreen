package dev.ergenverse.assembly;

/**
 * VoxelInstruction — a single immutable entry in the <b>intermediate
 * representation</b> (IR) emitted by the world assembly compiler.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This is the pivotal type the user asked for. The pipeline is:
 * <pre>
 *   Er Gen Canon (lore)
 *         │
 *   Semantic AST  (CanonSettlement → CanonBuilding → CanonRoom → CanonFurniture)
 *         │
 *   WorldAssembler  (semantic → VoxelInstructions)   ← this package
 *         │
 *   VoxelInstruction IR  { x, y, z, material, rotation, layer }
 *         │
 *   MaterialResolver  (MaterialID → BlockState)
 *         │
 *   VoxelMaterializer  (IR → ServerLevel.setBlock)   ← materialization package
 *         │
 *   Minecraft Engine  (rendering, physics, lighting)
 * </pre>
 *
 * <p><b>No {@code BlockState}, no {@code BlockPos}, no {@code Blocks}.</b>
 * Coordinates are plain {@code int}s relative to a settlement origin; the
 * materializer translates them to world {@code BlockPos} at the very end. This
 * is the boundary the user's constitutional rule draws: Minecraft classes exist
 * <i>only</i> in the materialization backend.
 *
 * <p>The IR is intentionally dumb — a flat list of (x, y, z, material,
 * rotation, layer) tuples. No tree, no references, no world state. This makes
 * it trivially serialisable, diffable, and portable to a non-Minecraft backend.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public record VoxelInstruction(
        int x,
        int y,
        int z,
        MaterialID material,
        Rotation rotation,
        VoxelLayer layer
) {
    public VoxelInstruction {
        java.util.Objects.requireNonNull(material, "material");
        java.util.Objects.requireNonNull(rotation, "rotation");
        java.util.Objects.requireNonNull(layer, "layer");
    }

    /** Convenience factory with {@link Rotation#NONE}. */
    public static VoxelInstruction at(int x, int y, int z, MaterialID material, VoxelLayer layer) {
        return new VoxelInstruction(x, y, z, material, Rotation.NONE, layer);
    }

    /** Returns a new instruction translated by (dx, dy, dz). */
    public VoxelInstruction translate(int dx, int dy, int dz) {
        return new VoxelInstruction(x + dx, y + dy, z + dz, material, rotation, layer);
    }

    @Override
    public String toString() {
        return "Voxel[" + x + "," + y + "," + z + " " + material + " " + rotation + " " + layer + "]";
    }
}
