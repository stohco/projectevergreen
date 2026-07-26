/**
 * Semantic structure composition — the "Canon Database" layer of the engine.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>The user's 2026-07-27 architectural redirection:
 * <blockquote>
 *   The bottleneck has become <b>content representation</b>. Not AI. Not chunk
 *   generation. Not deltas. Representation. […] I'd eventually replace builders
 *   entirely. Village → Buildings → Rooms → Furniture → Blocks. Each object
 *   should know how to materialize itself. […] Now the world actually understands
 *   "This is Wang Lin's bedroom." rather than "Stone brick at (3844,72,-1182)."
 * </blockquote>
 *
 * <p>This package implements that vision. It contains:
 * <ul>
 *   <li>{@link dev.ergenverse.canon.structure.CanonObject} — the root interface
 *       for all canon world objects. Lore-only: no BlockStates, no entities,
 *       no chunks. Knows how to {@code materializeInto} a {@link dev.ergenverse.canon.structure.VolumePlacer}.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonSettlement} — a composition
 *       of {@link dev.ergenverse.canon.structure.CanonBuilding}s.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonBuilding} — a composition
 *       of {@link dev.ergenverse.canon.structure.CanonRoom}s + owner + era +
 *       purpose + shell type.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonRoom} — a composition of
 *       {@link dev.ergenverse.canon.structure.CanonFurniture}s + a semantic
 *       function (BEDROOM, KITCHEN, MEDITATION, STORAGE, COURTYARD, WORKSHOP)
 *       + owner + bounding volume.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonFurniture} — an enum of
 *       semantic furniture (BED, MEDITATION_MAT, BOOKSHELF, HIDDEN_STORAGE,
 *       DESK, LAMP, ALCHEMY_FURNACE, SPIRIT_WELL, etc.).</li>
 *   <li>{@link dev.ergenverse.canon.structure.VolumePlacer} — the chunk-filtered,
 *       provenance-aware sink for block placements.</li>
 *   <li>{@link dev.ergenverse.canon.structure.WangFamilyVillageComposition} —
 *       authors Wang Family Village as a CanonSettlement.</li>
 *   <li>{@link dev.ergenverse.canon.structure.CanonSettlementBuilder} — the
 *       adapter that lets a CanonSettlement plug into the existing
 *       {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry}.</li>
 * </ul>
 *
 * <h2>Architectural layering (per the user's five-layer model)</h2>
 * <pre>
 *   Er Gen Canon (lore)
 *         │
 *   Canon Database (this package — semantic world objects, NO Minecraft world imports)
 *         │
 *   Blueprint (PlanetSuzakuBlueprint — coordinates + queries)
 *         │
 *   World Runtime (WorldDeltaStore + CompositeWorldLayer + WorldFacade)
 *         │
 *   Chunk Materializer (PlanetSuzakuChunkMaterializer + BlueprintChunkGenerator)
 *         │
 *   Minecraft Engine (rendering, physics, chunk I/O)
 * </pre>
 *
 * <h2>Canon fidelity</h2>
 *
 * <p>Per the user's "Canon is reality" Article I, every object in this package
 * is either canon (directly attested in 仙逆), mod-original (invented for the
 * mod but honestly flagged), or inferred (derived from canon). Every class's
 * Javadoc explicitly states which. No fabricated chapter citations.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
package dev.ergenverse.canon.structure;
