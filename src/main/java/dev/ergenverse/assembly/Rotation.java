package dev.ergenverse.assembly;

/**
 * Rotation — a 90-degree-quantised horizontal rotation applied to a voxel
 * during materialization.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER</b>
 *
 * <p>The user's IR spec includes a {@code Rotation} field on every
 * {@link VoxelInstruction}. This lets the same furniture / door template be
 * reused facing any cardinal direction without duplicating geometry. The
 * {@link dev.ergenverse.materialization.MaterialResolver} applies the rotation
 * to directional {@code BlockState} properties (e.g. door facing, stair facing)
 * at the very last moment — the only place Minecraft {@code Direction} appears.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum Rotation {
    /** No rotation — faces its canonical direction (south by convention). */
    NONE,
    /** 90 degrees clockwise. */
    CW90,
    /** 180 degrees. */
    CW180,
    /** 270 degrees clockwise (= 90 counter-clockwise). */
    CW270
}
