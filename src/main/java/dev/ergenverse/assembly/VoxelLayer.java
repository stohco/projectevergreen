package dev.ergenverse.assembly;

/**
 * VoxelLayer — the semantic layer of a voxel placement within the assembly
 * pipeline.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER</b>
 *
 * <p>Every {@link VoxelInstruction} carries a {@code VoxelLayer}. This lets the
 * materializer, the AI, and debugging tools understand <i>what role</i> a voxel
 * plays — "is this a foundation block, a furniture block, or decoration?" —
 * without inspecting the material.
 *
 * <p>Layers are ordered roughly bottom-up: a building is assembled foundation →
 * floor → walls → roof → door → furniture → decoration. Open-area features use
 * FLORA (trees) and PATH (roads, lights, fences).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum VoxelLayer {
    /** Bedrock-ish / ground preparation below the floor. */
    FOUNDATION,
    /** The walking surface inside a building. */
    FLOOR,
    /** Vertical walls of a building shell. */
    WALL,
    /** The ceiling / roof of a building shell. */
    ROOF,
    /** Door blocks (lower + upper halves). */
    DOOR,
    /** Furniture placed inside rooms. */
    FURNITURE,
    /** Trees, bushes, and other flora in open areas. */
    FLORA,
    /** Roads, fences, and path lights in open areas. */
    PATH,
    /** Miscellaneous decoration not covered above. */
    DECORATION
}
