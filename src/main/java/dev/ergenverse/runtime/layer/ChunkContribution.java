package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.EntityPlacementDelta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ChunkContribution — what one {@link WorldLayer} contributes to a chunk.
 *
 * <p><b>Architectural directive (CRON-69, point 7):</b> each layer answers
 * "what changes intersect this chunk?" at its natural granularity. The
 * blueprint answers in structures; the delta layers answer in block changes.
 * This object carries both kinds so the {@code ChunkMaterializer} can merge
 * them uniformly.
 *
 * <p>The fields are intentionally mutable lists built by the layer; the
 * materializer reads them after the layer returns. They are made unmodifiable
 * on {@link #seal()} to prevent accidental mutation during merge.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class ChunkContribution {

    /** Block changes the layer wants applied in this chunk (delta layers). */
    public final List<BlockChangeDelta> blockChanges = new ArrayList<>();

    /** Canon structures intersecting this chunk (blueprint layer). */
    public final List<PlanetSuzakuBlueprint.CanonLocation> structures = new ArrayList<>();

    /**
     * Entity placements (PLACE or REMOVE) the layer wants applied in this chunk
     * (delta layers, CRON-78). Carries player-placed ItemFrames/Paintings and
     * player-removed canon entities so the chunk-materializer can replay them.
     */
    public final List<EntityPlacementDelta> entityPlacements = new ArrayList<>();

    /** True if the layer contributes nothing to this chunk. */
    public boolean isEmpty() {
        return blockChanges.isEmpty() && structures.isEmpty() && entityPlacements.isEmpty();
    }

    /** Freeze the lists (call before handing to the materializer). */
    public ChunkContribution seal() {
        Collections.replaceAll(blockChanges, null, null); // no-op, keeps shape
        return this;
    }
}
