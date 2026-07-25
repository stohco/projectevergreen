package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.spatial.SpatialIndex;

/**
 * BlueprintLayer — the immutable canon stratum (Provenance.CANON).
 *
 * <p><b>Architectural directive (CRON-69, point 8):</b> "PlanetSuzakuBlueprint
 * should never answer 'getBlock'. … Instead I'd make the blueprint answer
 * higher-level questions. Like queryTerrain(...), queryStructures(...),
 * queryActors(...), querySpiritVeins(...). Why? Because the blueprint isn't
 * actually a giant block array. It's a description of the world."
 *
 * <p>So this layer's {@link #getBlock} <b>always returns null</b> — it defers
 * to the minecraft:noise base terrain (which is itself deterministic via
 * {@code DeterministicSeedHandler.CANON_SEED}, so "zero dependence on random
 * terrain" still holds). The blueprint never pretends to be a per-block array.
 *
 * <p>Instead, {@link #getChunkContribution} answers at <b>structure granularity</b>
 * (point 7: "Blueprint returns Structures, Terrain patches, Road segments"):
 * it queries the {@link SpatialIndex} for every {@link PlanetSuzakuBlueprint.CanonLocation}
 * whose footprint intersects the chunk. The {@link dev.ergenverse.runtime.materialize.ChunkMaterializer}
 * then invokes each location's registered builder (idempotent) to place the
 * hand-authored blocks. That is how canon enters the live world — as named
 * structures materialized on demand, never as a per-block lookup.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class BlueprintLayer implements WorldLayer {

    private final PlanetSuzakuBlueprint blueprint;
    private final SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex;

    public BlueprintLayer(PlanetSuzakuBlueprint blueprint,
                          SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex) {
        this.blueprint = blueprint;
        this.spatialIndex = spatialIndex;
    }

    @Override public Provenance provenance() { return Provenance.CANON; }

    /**
     * Always null. The blueprint is a description of the world, not a block
     * array. Per-block canon is materialized by builders via
     * {@link #getChunkContribution}, not by this method.
     */
    @Override
    public String getBlock(int x, int y, int z) {
        return null;
    }

    /**
     * The canon structures (settlements, sects, ruins, geographic landmarks)
     * whose footprint intersects this chunk. The materializer builds each via
     * its registered {@code StructureBuilder} (idempotent).
     */
    @Override
    public ChunkContribution getChunkContribution(int chunkX, int chunkZ) {
        ChunkContribution c = new ChunkContribution();
        int minX = chunkX * 16, minZ = chunkZ * 16;
        int maxX = minX + 15, maxZ = minZ + 15;
        for (PlanetSuzakuBlueprint.CanonLocation loc : spatialIndex.query(minX, minZ, maxX, maxZ)) {
            c.structures.add(loc);
        }
        return c;
    }

    /** The blueprint this layer reads from. */
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
