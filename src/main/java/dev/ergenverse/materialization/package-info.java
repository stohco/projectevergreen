/**
 * The materialization backend — the <b>only</b> package that references
 * Minecraft world classes ({@code ServerLevel}, {@code BlockState},
 * {@code BlockPos}, {@code Blocks}).
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>Per the user's constitutional rule:
 * <blockquote>
 *   Minecraft classes (BlockState, BlockPos, Blocks, ServerLevel, Entity, etc.)
 *   may not appear in the Canon or Semantic World layers. They are backend
 *   implementation details and belong exclusively to the world
 *   assembly/materialization backend.
 * </blockquote>
 *
 * <p>This package is that backend. It contains:
 * <ul>
 *   <li>{@link dev.ergenverse.materialization.MaterialResolver} — translates a
 *       backend-agnostic {@link dev.ergenverse.assembly.MaterialID} into a
 *       concrete {@code BlockState} (applying rotation). This is the single
 *       seam between "semantic material" and "Minecraft block".</li>
 *   <li>{@link dev.ergenverse.materialization.VoxelMaterializer} — applies a
 *       list of {@link dev.ergenverse.assembly.VoxelInstruction}s to a
 *       {@code ServerLevel}, chunk-filtered and provenance-aware.</li>
 *   <li>{@link dev.ergenverse.materialization.VolumePlacer} — the chunk-filtered,
 *       provenance-aware sink for {@code setBlock} calls.</li>
 * </ul>
 *
 * <p>A future non-Minecraft backend (Godot, a custom renderer) would replace
 * this package without touching any canon or assembly code.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
package dev.ergenverse.materialization;
