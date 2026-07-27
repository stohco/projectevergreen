package dev.ergenverse.materialization;

import dev.ergenverse.assembly.AnchorRegistry;
import dev.ergenverse.assembly.AnchorRegistryService;
import dev.ergenverse.assembly.AssemblyResult;
import dev.ergenverse.assembly.WorldAssembler;
import dev.ergenverse.canon.structure.CanonSettlement;
import dev.ergenverse.canon.structure.WangFamilyVillageComposition;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * CanonSettlementBuilder — the adapter that compiles a {@link CanonSettlement}
 * and materializes it into a {@code ServerLevel} via the CRON-127 pipeline.
 *
 * <p><b>CRON-131 — ASSEMBLY CACHING (performance).</b>
 *
 * <p>Prior to CRON-131, every chunk-load in a settlement's footprint
 * triggered a full {@link WorldAssembler#assemble} call — re-compiling the
 * entire semantic composition tree into ~80K {@link VoxelInstruction}s.
 * For a 7x7 chunk grid (49 chunks) covering Wang Family Village, this meant
 * 49 full re-assemblies at server start (CRON-131 force-loads the village
 * chunks) — ~4M object allocations + iterations causing a multi-second lag
 * spike.
 *
 * <p>CRON-131 adds a per-settlement-id cache: the {@link AssemblyResult} is
 * assembled ONCE (on first chunk-load) and reused for all subsequent
 * chunk-loads. The {@link VoxelMaterializer} applies chunk-bounds filtering
 * on each call, so the same cached result is safe to reuse — it contains
 * absolute world coordinates that don't change between calls. The cache is
 * keyed by settlement id (not by chunk), so the 49 chunk-loads share one
 * assembly.
 *
 * <p>The cache is a simple {@link HashMap} — safe because chunk materialization
 * runs single-threaded on the server tick thread (no concurrent access).
 *
 * <p>The {@link AnchorRegistryService#register} call is ALSO guarded by a
 * per-settlement flag so anchors are published exactly once per settlement,
 * not 49 times.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>This adapter is the glue between the pure-semantic canon layer and the
 * Minecraft materialization backend. It performs the two-step compilation:
 * <pre>
 *   CanonSettlement ──► {@link WorldAssembler#assemble} ──► {@link AssemblyResult}
 *                                                                │
 *   ServerLevel     ◄── {@link VoxelMaterializer#materialize} ◄──┘
 * </pre>
 *
 * <p>The canon layer knows nothing about Minecraft; the materialization layer
 * knows nothing about canon semantics. This adapter is where the two meet —
 * one of the few places permitted to import from both sides.
 *
 * <h2>Migration status</h2>
 *
 * <p>CRON-126 routed {@code "wang_family_village"} in
 * {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry} to
 * {@link #buildWangFamilyVillage}. CRON-127 rewrites the internals to use the
 * WorldAssembler + VoxelMaterializer pipeline instead of the old direct
 * {@code materializeInto(VolumePlacer)} call. The external API is unchanged.
 *
 * <p><b>CRON-129 — ANCHOR REGISTRY PUBLISH.</b> After each successful
 * assembly, the compiled {@link AnchorRegistry} is published to
 * {@link AnchorRegistryService} so NPC AI goals can query semantic anchors
 * ("where is the nearest MEDITATION mat?") by settlement id + role. Before
 * CRON-129, the registry was logged then discarded; AI had no way to ask
 * the user's "Find Wang Lin → Find Bedroom → Find Bed" question.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonSettlementBuilder {

    private CanonSettlementBuilder() {}

    /**
     * CRON-131: per-settlement assembly cache. Keyed by settlement id.
     *
     * <p>The cached {@link AssemblyResult} contains absolute world coordinates
     * (already translated to the settlement's canon origin), so it is safe to
     * reuse across chunk-loads. {@link VoxelMaterializer} applies chunk-bounds
     * filtering on each call.
     *
     * <p>Thread-safety: chunk materialization runs single-threaded on the
     * server tick thread, so a plain HashMap is sufficient (no concurrent
     * access). If materialization ever becomes multi-threaded, switch to
     * ConcurrentHashMap.
     */
    private static final Map<String, AssemblyResult> ASSEMBLY_CACHE = new HashMap<>();

    /**
     * CRON-131: per-settlement anchor-publish flag. Ensures
     * {@link AnchorRegistryService#register} is called exactly once per
     * settlement, not once per chunk-load (which would be 49x for a 7x7 grid).
     */
    private static final Map<String, Boolean> ANCHORS_PUBLISHED = new HashMap<>();

    /**
     * Compile and materialize the Wang Family Village at its canon coordinate.
     *
     * <p>CRON-131: the assembly is cached — the first call assembles the full
     * composition (~80K voxels) and caches the {@link AssemblyResult};
     * subsequent calls (from other chunk-loads) reuse the cached result and
     * only re-run {@link VoxelMaterializer} with the new chunk bounds.
     *
     * @return the number of voxels written (in this chunk's bounds)
     */
    public static int buildWangFamilyVillage(ServerLevel level, @Nullable ChunkBounds bounds) {
        String settlementId = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.id;
        int canonX = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x;
        int canonZ = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z;
        int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, canonX, canonZ);

        // CRON-131: use cached assembly if available; assemble + cache on first call.
        AssemblyResult result = ASSEMBLY_CACHE.get(settlementId);
        if (result == null) {
            CanonSettlement village = WangFamilyVillageComposition.create();
            Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: assembling Wang Family Village "
                    + "composition at ({}, {}, {}) bounds={} — FIRST CALL (caching result).",
                    canonX, surfaceY, canonZ, bounds);
            result = WorldAssembler.assemble(village, canonX, surfaceY, canonZ);
            ASSEMBLY_CACHE.put(settlementId, result);
        } else {
            Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: reusing cached assembly for "
                    + "Wang Family Village ({} voxels) bounds={}.", result.instructions().size(), bounds);
        }

        int written = VoxelMaterializer.materialize(result, level, bounds);

        // CRON-129: publish the compiled AnchorRegistry to the singleton
        // service so NPC AI goals can query semantic anchors by role.
        // CRON-131: publish exactly ONCE per settlement (guarded by flag)
        // — previously this was called on every chunk-load (49x for a 7x7
        // grid), redundantly re-registering the same anchors.
        if (!ANCHORS_PUBLISHED.containsKey(settlementId)) {
            AnchorRegistryService.get().register(settlementId, result.anchors());
            ANCHORS_PUBLISHED.put(settlementId, Boolean.TRUE);
            Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: Wang Family Village materialized — "
                    + "{} voxels (this chunk), {} anchors published to AnchorRegistryService (FIRST PUBLISH).",
                    written, result.anchors().size());
        } else {
            Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: Wang Family Village materialized — "
                    + "{} voxels (this chunk). Anchors already published (skipped).", written);
        }
        return written;
    }

    /**
     * Compile and materialize any registered {@link CanonSettlement} at its
     * canon coordinate.
     *
     * @return the number of voxels written, or -1 if no composition was found
     */
    public static int build(String settlementId, ServerLevel level, @Nullable ChunkBounds bounds) {
        CanonSettlement settlement = resolveComposition(settlementId);
        if (settlement == null) return -1;

        PlanetSuzakuBlueprint.CanonLocation loc = PlanetSuzakuBlueprint.canonical()
                .allLocations().get(settlementId);
        if (loc == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] CanonSettlementBuilder: no canon location for '{}'.", settlementId);
            return -1;
        }

        int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, loc.x, loc.z);

        // CRON-131: use cached assembly if available; assemble + cache on first call.
        AssemblyResult result = ASSEMBLY_CACHE.get(settlementId);
        if (result == null) {
            result = WorldAssembler.assemble(settlement, loc.x, surfaceY, loc.z);
            ASSEMBLY_CACHE.put(settlementId, result);
        }

        int written = VoxelMaterializer.materialize(result, level, bounds);

        // CRON-129: publish the compiled AnchorRegistry for AI consumption.
        // CRON-131: publish exactly ONCE per settlement (guarded by flag).
        if (!ANCHORS_PUBLISHED.containsKey(settlementId)) {
            AnchorRegistryService.get().register(settlementId, result.anchors());
            ANCHORS_PUBLISHED.put(settlementId, Boolean.TRUE);
        }

        Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: '{}' materialized — "
                + "{} voxels, {} anchors published to AnchorRegistryService.",
                settlementId, written, result.anchors().size());
        return written;
    }

    private static @Nullable CanonSettlement resolveComposition(String settlementId) {
        if ("wang_family_village".equals(settlementId)) {
            return WangFamilyVillageComposition.create();
        }
        return null;
    }
}
