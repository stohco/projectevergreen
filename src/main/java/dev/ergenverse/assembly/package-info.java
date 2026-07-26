/**
 * World assembly — the <b>compiler</b> layer that turns semantic canon objects
 * into a backend-agnostic intermediate representation (IR).
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's "compiler analogy":
 * <blockquote>
 *   Er Gen Canon → Semantic AST → Template Library (meaning → geometry) →
 *   Blueprint Compiler (geometry → voxels) → Minecraft Backend (voxels →
 *   BlockState). […] The semantic world never "renders." It is compiled. […]
 *   I'd probably call it WorldAssembler because it's assembling a world from
 *   semantic pieces.
 * </blockquote>
 *
 * <p>This package sits <b>above</b> Minecraft and <b>below</b> canon. It
 * contains:
 * <ul>
 *   <li>{@link dev.ergenverse.assembly.MaterialID} — a backend-agnostic voxel
 *       material identifier (no {@code BlockState}).</li>
 *   <li>{@link dev.ergenverse.assembly.VoxelInstruction} — a single IR entry:
 *       {@code (x, y, z, MaterialID, Rotation, VoxelLayer)}. The pivotal type.</li>
 *   <li>{@link dev.ergenverse.assembly.VoxelLayer} — the semantic role of a
 *       voxel (FOUNDATION, FLOOR, WALL, ROOF, FURNITURE, …).</li>
 *   <li>{@link dev.ergenverse.assembly.Rotation} — 90°-quantised horizontal
 *       rotation applied at materialization.</li>
 *   <li>Four independent <b>libraries</b> (per the user's directive):
 *     <ul>
 *       <li>{@link dev.ergenverse.assembly.FurnitureLibrary} —
 *           {@code CanonFurniture → VoxelInstructions}.</li>
 *       <li>{@link dev.ergenverse.assembly.BuildingLibrary} —
 *           {@code BuildingTheme → shell VoxelInstructions}.</li>
 *       <li>{@link dev.ergenverse.assembly.TerrainLibrary} — subterranean
 *           features (spirit veins, ore).</li>
 *       <li>{@link dev.ergenverse.assembly.DecorationLibrary} — open-area
 *           features (trees, fences, roads, lights).</li>
 *     </ul>
 *   </li>
 *   <li>{@link dev.ergenverse.assembly.WorldAssembler} — the compiler. Walks a
 *       {@link dev.ergenverse.canon.structure.CanonSettlement} tree, asks the
 *       libraries for geometry, and emits an {@link dev.ergenverse.assembly.AssemblyResult}.</li>
 *   <li>{@link dev.ergenverse.assembly.AssemblyResult} —
 *       {@code (List<VoxelInstruction>, AnchorRegistry)}.</li>
 *   <li>{@link dev.ergenverse.assembly.AnchorRegistry} — resolves semantic
 *       {@link dev.ergenverse.canon.structure.Anchor}s to world coordinates
 *       for AI navigation.</li>
 * </ul>
 *
 * <p><b>No {@code net.minecraft} imports anywhere in this package.</b> The IR
 * is pure data — serialisable, diffable, and portable to a non-Minecraft
 * backend. The {@link dev.ergenverse.materialization} package is the only
 * place that turns a {@code MaterialID} into a {@code BlockState}.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
package dev.ergenverse.assembly;
