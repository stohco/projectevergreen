package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.EntityPlacementDelta;
import dev.ergenverse.runtime.delta.WorldDeltaStore;

/**
 * PlayerLayer — the player-edit stratum (Provenance.PLAYER).
 *
 * <p>Backed by the {@link WorldDeltaStore}'s per-provenance block AND entity
 * indexes. This layer has <i>no state of its own</i> — it is a stateless view
 * over the store, filtered to PLAYER-provenance changes. That keeps the store
 * as the single persistence unit (one language, one journal) while the
 * {@link CompositeWorldLayer} composes this layer alongside the simulation and
 * blueprint layers.
 *
 * <p>Resolution priority: PLAYER &gt; SIMULATION &gt; CANON. The composite asks
 * this layer first, so any position the player has edited resolves to the
 * player's state.
 *
 * <p><b>CRON-78:</b> {@link #getChunkContribution} now also surfaces
 * {@link EntityPlacementDelta}s (player-placed ItemFrames/Paintings and
 * player-removed canon entities) so the chunk-materializer can replay them
 * after canon blocks are placed.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class PlayerLayer implements WorldLayer {

    private final WorldDeltaStore store;

    public PlayerLayer(WorldDeltaStore store) {
        this.store = store;
    }

    @Override public Provenance provenance() { return Provenance.PLAYER; }

    @Override
    public String getBlock(int x, int y, int z) {
        return store.getBlock(x, y, z, Provenance.PLAYER);
    }

    @Override
    public ChunkContribution getChunkContribution(int chunkX, int chunkZ) {
        ChunkContribution c = new ChunkContribution();
        for (BlockChangeDelta d : store.getBlockChangesInChunk(chunkX, chunkZ)) {
            if (d.provenance() == Provenance.PLAYER) c.blockChanges.add(d);
        }
        // CRON-78: surface entity placements (PLACE and REMOVE) so the
        // materializer can replay player-placed ItemFrames/Paintings and
        // honor player-removed canon entities.
        for (EntityPlacementDelta d : store.getEntityPlacementsInChunk(chunkX, chunkZ)) {
            if (d.provenance() == Provenance.PLAYER) c.entityPlacements.add(d);
        }
        return c;
    }
}
