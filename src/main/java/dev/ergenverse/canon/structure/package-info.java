/**
 * Semantic structure composition — the <b>Canon Database</b> layer of the engine.
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
 * <p>Consequently, <b>this package contains zero {@code net.minecraft} imports.</b>
 * It is a pure domain model of the Er Gen world: settlements, buildings, rooms,
 * furniture, themes, anchors, and intents — all expressed as plain Java records
 * and enums with {@code int} coordinates. The world is not "rendered" here; it
 * is <i>described</i>.
 *
 * <h2>The compilation pipeline (CRON-127)</h2>
 * <pre>
 *   Er Gen Canon (lore)
 *         │
 *   ┌─────┴───────────────────────────────────┐  ◄── this package (Layer 1)
 *   │  CanonSettlement → CanonBuilding →       │      Pure semantic. No Minecraft.
 *   │  CanonRoom → CanonFurniture               │      Knows: id, evidence, theme,
 *   │  + BuildingTheme + Anchor + Intent        │      owner, era, anchors, bounds.
 *   └─────┬───────────────────────────────────┘
 *         │
 *   dev.ergenverse.assembly (Layer 2-3)
 *   WorldAssembler + 4 libraries + VoxelInstruction IR
 *         │
 *   dev.ergenverse.materialization (Layer 4)
 *   MaterialResolver (MaterialID→BlockState) + VoxelMaterializer
 *         │
 *   Minecraft Engine (Layer 5 — rendering, physics, lighting)
 * </pre>
 *
 * <h2>Contents</h2>
 * <ul>
 *   <li>{@link dev.ergenverse.canon.structure.CanonObject} — root interface:
 *       {@code canonId}, {@code canonEvidence}, {@code relativeBounds},
 *       {@code anchors}. No {@code materializeInto} (removed in CRON-127).</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonSettlement} — composition of
 *       {@link dev.ergenverse.canon.structure.CanonBuilding}s + open features.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonBuilding} — rooms +
 *       {@link dev.ergenverse.canon.structure.BuildingTheme}.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonRoom} — furniture +
 *       {@link dev.ergenverse.canon.structure.CanonRoom.RoomFunction} + owner.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonFurniture} — semantic enum
 *       (kind + evidence + {@link dev.ergenverse.canon.structure.Intent} +
 *       anchors). No block references.</li>
 *   <li>{@link dev.ergenverse.canon.structure.BuildingTheme} — pure-enum
 *       construction style (POOR_VILLAGE, ELDER_HOME, …).</li>
 *   <li>{@link dev.ergenverse.canon.structure.Anchor} — named attachment point
 *       with a {@link dev.ergenverse.canon.structure.SemanticRole}.</li>
 *   <li>{@link dev.ergenverse.canon.structure.SemanticRole} — navigational role
 *       (BED, MEDITATION, ENTRANCE, WELL, …).</li>
 *   <li>{@link dev.ergenverse.canon.structure.Intent} — semantic purpose
 *       (SLEEP, CULTIVATE, STUDY, …).</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonVolume} — pure-{@code int}
 *       AABB spatial primitive.</li>
 *   <li>{@link dev.ergenverse.canon.structure.WangFamilyVillageComposition} —
 *       authors Wang Family Village as a CanonSettlement.</li>
 * </ul>
 *
 * <p>The adapter that compiles + materializes a CanonSettlement lives in the
 * {@link dev.ergenverse.materialization} package
 * ({@link dev.ergenverse.materialization.CanonSettlementBuilder}), because it
 * references {@code ServerLevel} and therefore cannot reside in this
 * Minecraft-free layer.
 *
 * <h2>Canon fidelity</h2>
 *
 * <p>Per the user's "Canon is reality" Article I, every object in this package
 * is either canon (directly attested in 仙逆), mod-original (invented for the
 * mod but honestly flagged), or inferred (derived from canon). Every class's
 * Javadoc explicitly states which. No fabricated chapter citations.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
package dev.ergenverse.canon.structure;
