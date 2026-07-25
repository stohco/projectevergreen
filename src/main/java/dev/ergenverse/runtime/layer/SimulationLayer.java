package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.WorldDeltaStore;

/**
 * SimulationLayer — the simulation-evolution stratum (Provenance.SIMULATION).
 *
 * <p>Backed by the {@link WorldDeltaStore}'s per-provenance block index,
 * filtered to SIMULATION-provenance changes. Stateless view, like
 * {@link PlayerLayer}.
 *
 * <p>Examples of what lives here: a spirit beast harvests an herb (herb → air),
 * a storm breaks a roof (roof → air), a sect extends its walls (air → spirit
 * stone), a spirit vein is depleted (ore → stone). These are the world evolving
 * on its own — never the player, never the immutable blueprint.
 *
 * <p>Resolution priority: PLAYER &gt; SIMULATION &gt; CANON. If the player has
 * also edited a position, the player wins (the composite asks PlayerLayer
 * first). The simulation layer is the second authority.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class SimulationLayer implements WorldLayer {

    private final WorldDeltaStore store;

    public SimulationLayer(WorldDeltaStore store) {
        this.store = store;
    }

    @Override public Provenance provenance() { return Provenance.SIMULATION; }

    @Override
    public String getBlock(int x, int y, int z) {
        return store.getBlock(x, y, z, Provenance.SIMULATION);
    }

    @Override
    public ChunkContribution getChunkContribution(int chunkX, int chunkZ) {
        ChunkContribution c = new ChunkContribution();
        for (BlockChangeDelta d : store.getBlockChangesInChunk(chunkX, chunkZ)) {
            if (d.provenance() == Provenance.SIMULATION) c.blockChanges.add(d);
        }
        return c;
    }
}
