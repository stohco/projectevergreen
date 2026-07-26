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

/**
 * CanonSettlementBuilder — the adapter that compiles a {@link CanonSettlement}
 * and materializes it into a {@code ServerLevel} via the CRON-127 pipeline.
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
     * Compile and materialize the Wang Family Village at its canon coordinate.
     *
     * @return the number of voxels written
     */
    public static int buildWangFamilyVillage(ServerLevel level, @Nullable ChunkBounds bounds) {
        CanonSettlement village = WangFamilyVillageComposition.create();
        int canonX = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x;
        int canonZ = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z;
        int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, canonX, canonZ);

        Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: assembling Wang Family Village "
                + "composition at ({}, {}, {}) bounds={}", canonX, surfaceY, canonZ, bounds);

        AssemblyResult result = WorldAssembler.assemble(village, canonX, surfaceY, canonZ);
        int written = VoxelMaterializer.materialize(result, level, bounds);

        // CRON-129: publish the compiled AnchorRegistry to the singleton
        // service so NPC AI goals can query semantic anchors by role.
        // This is the bridge between "the compiler knows where Wang Lin's
        // meditation mat is" and "the cultivator's meditation goal can
        // pathfind to that mat". Without this publish call, the registry
        // is logged-then-discarded and AI cannot ask anchor questions.
        AnchorRegistryService.get().register(
                PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.id, result.anchors());

        Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: Wang Family Village materialized — "
                + "{} voxels, {} anchors published to AnchorRegistryService.",
                written, result.anchors().size());
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
        AssemblyResult result = WorldAssembler.assemble(settlement, loc.x, surfaceY, loc.z);
        int written = VoxelMaterializer.materialize(result, level, bounds);

        // CRON-129: publish the compiled AnchorRegistry for AI consumption.
        AnchorRegistryService.get().register(settlementId, result.anchors());

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
