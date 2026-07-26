package dev.ergenverse.canon.structure;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

/**
 * CanonSettlementBuilder — the adapter that lets a {@link CanonSettlement}
 * plug into the existing {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry}.
 *
 * <p><b>CRON-COMPLETIONIST-125 — STRUCTURE COMPOSITION SYSTEM (user roadmap #2)</b>
 *
 * <p>The {@link dev.ergenverse.runtime.materialize.StructureBuilderRegistry}
 * expects a builder with a {@code buildForChunk(ServerLevel, ChunkBounds)}
 * signature. This adapter wraps a {@link CanonSettlement} so it can be
 * registered as a builder, preserving the existing CRON-62/63/69/72/104
 * chunk-materializer architecture intact.
 *
 * <h2>Migration status</h2>
 *
 * <p>CRON-125 does <b>NOT</b> switch the registry entry — the legacy
 * {@code WangFamilyVillageBuilder.buildForChunk} remains registered for
 * {@code "wang_family_village"}. The composition system is exercised via the
 * new {@code /ergen debug canon-build wang_family_village} command (added to
 * {@link dev.ergenverse.command.ErgenDebugCommand}). This incremental
 * migration lets the user playtest the composition system in isolation
 * before switching the registry.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonSettlementBuilder {

    private CanonSettlementBuilder() {}

    /**
     * Build the Wang Family Village via the composition system.
     *
     * <p>Translates the settlement's relative composition to the canon world
     * coordinate (3842, surface, -1184), then delegates to
     * {@link CanonSettlement#materializeInto}.
     */
    public static int buildWangFamilyVillage(ServerLevel level, @Nullable ChunkBounds bounds) {
        CanonSettlement village = WangFamilyVillageComposition.create();
        int canonX = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x;
        int canonZ = PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z;
        int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, canonX, canonZ);

        VolumePlacer placer = VolumePlacer.forChunk(level, bounds);
        Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: materializing Wang Family Village "
                + "composition at ({}, {}, {}) bounds={}", canonX, surfaceY, canonZ, bounds);
        village.materializeInto(placer, canonX, surfaceY, canonZ);
        Ergenverse.LOGGER.info("[Ergenverse] CanonSettlementBuilder: Wang Family Village composition materialized.");
        return 0;
    }

    /**
     * Build any registered {@link CanonSettlement} at its canon coordinate.
     *
     * @return true if a composition was found and materialized; false otherwise
     */
    public static boolean build(String settlementId, ServerLevel level, @Nullable ChunkBounds bounds) {
        CanonSettlement settlement = resolveComposition(settlementId);
        if (settlement == null) return false;

        PlanetSuzakuBlueprint.CanonLocation loc = PlanetSuzakuBlueprint.canonical()
                .allLocations().get(settlementId);
        if (loc == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] CanonSettlementBuilder: no canon location for '{}'.", settlementId);
            return false;
        }

        int surfaceY = BlueprintChunkGenerator.surfaceHeightFor(level, loc.x, loc.z);
        VolumePlacer placer = VolumePlacer.forChunk(level, bounds);
        settlement.materializeInto(placer, loc.x, surfaceY, loc.z);
        return true;
    }

    private static @Nullable CanonSettlement resolveComposition(String settlementId) {
        if ("wang_family_village".equals(settlementId)) {
            return WangFamilyVillageComposition.create();
        }
        return null;
    }
}
