package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.delta.BlockChangeDelta;

import java.util.Collections;
import java.util.List;

/**
 * WorldLayer — a composable stratum of world state.
 *
 * <p><b>Architectural directive (CRON-69, point 3):</b> "DeltaManager shouldn't
 * know about priority. … I'd actually make layers composable. Something like
 * {@code interface WorldLayer { BlockState get(...); }}. Then PlayerLayer,
 * SimulationLayer, BlueprintLayer, and CompositeWorldLayer asks them in order.
 * That means later you can insert another layer without rewriting the manager.
 * For example Temporary Event Layer or Quest Layer."
 *
 * <p><b>Point 7 — chunk-scoped answers:</b> "Don't materialize entire chunks.
 * … each layer should answer: What changes intersect this chunk? Blueprint →
 * returns Structures, Terrain patches, Road segments. Simulation → returns 4
 * modified blocks. Player → returns House, Mine shaft, Chest. Now merge."
 *
 * <p>So a layer answers two questions:
 * <ol>
 *   <li>{@link #getBlock} — "what is the state at this single position,
 *       according to me?" Returns null to defer to the next layer.</li>
 *   <li>{@link #getChunkContribution} — "what do I contribute to this whole
 *       chunk?" Returns block changes (delta layers) and/or canon structures
 *       (blueprint layer), at the granularity natural to that layer.</li>
 * </ol>
 *
 * <p>The {@link CompositeWorldLayer} asks layers in priority order for
 * {@code getBlock} (first non-null wins), and merges all layers'
 * {@code getChunkContribution} for materialization (applied low-priority-first
 * so high-priority overwrites).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public interface WorldLayer {

    /** Which provenance this layer speaks for. */
    Provenance provenance();

    /**
     * The block state at a position according to this layer, or {@code null} if
     * this layer has no opinion (defer to the next layer in the composite).
     *
     * <p>For {@link PlayerLayer} and {@link SimulationLayer} this is the latest
     * recorded change at that position. For {@link BlueprintLayer} this returns
     * null (the blueprint answers at structure granularity via
     * {@link #getChunkContribution}, never per-block — see point 8).
     */
    String getBlock(int x, int y, int z);

    /**
     * Everything this layer contributes to the given chunk. Delta layers return
     * their recorded {@link BlockChangeDelta}s in that chunk; the blueprint
     * layer returns the canon {@link PlanetSuzakuBlueprint.CanonLocation}s that
     * intersect it. Never null.
     */
    ChunkContribution getChunkContribution(int chunkX, int chunkZ);
}
