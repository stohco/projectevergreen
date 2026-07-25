package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.Provenance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CompositeWorldLayer — composes an ordered list of {@link WorldLayer}s into one
 * queryable world.
 *
 * <p><b>Architectural directive (CRON-69, point 3):</b> "DeltaManager shouldn't
 * know about priority. … I'd actually make layers composable. … CompositeWorldLayer
 * asks them in order. That means later you can insert another layer without
 * rewriting the manager. For example Temporary Event Layer or Quest Layer."
 *
 * <p>This class holds <b>no priority logic of its own</b>. The resolution order
 * is expressed purely by the order of layers in the constructor-supplied list:
 * the first layer to return a non-null {@code getBlock} wins. Today the order
 * is {@code [PlayerLayer, SimulationLayer, BlueprintLayer]} (PLAYER wins), but
 * inserting a {@code QuestLayer} or {@code TemporaryEventLayer} is a one-line
 * change to the list — no rewrite, no priority enum to edit.
 *
 * <p><b>Two access patterns.</b>
 * <ul>
 *   <li>{@link #getBlock} — query path, asked in <b>priority order</b>
 *       (high-priority first; first non-null wins).</li>
 *   <li>{@link #layersInMaterializationOrder} — apply path, returned in
 *       <b>reverse priority order</b> (low-priority first; high-priority
 *       overwrites). The {@link dev.ergenverse.runtime.materialize.ChunkMaterializer}
 *       iterates this when replaying a chunk so PLAYER edits land last and win
 *       on conflict.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CompositeWorldLayer {

    /** Priority order: index 0 is highest priority (asked first on query). */
    private final List<WorldLayer> layers;

    public CompositeWorldLayer(List<WorldLayer> layers) {
        this.layers = Collections.unmodifiableList(new ArrayList<>(layers));
    }

    /** The layers in priority (query) order — first wins. */
    public List<WorldLayer> layers() {
        return layers;
    }

    /**
     * The layers in materialization (apply) order — last wins, so the highest
     * priority layer is applied last and overwrites on conflict.
     */
    public List<WorldLayer> layersInMaterializationOrder() {
        List<WorldLayer> reversed = new ArrayList<>(layers);
        Collections.reverse(reversed);
        return reversed;
    }

    /**
     * Query the final block state at a position. Asks each layer in priority
     * order; the first non-null answer wins. Returns null if no layer has an
     * opinion (meaning: use the deterministic minecraft:noise base terrain).
     */
    public String getBlock(int x, int y, int z) {
        for (WorldLayer layer : layers) {
            String s = layer.getBlock(x, y, z);
            if (s != null) return s;
        }
        return null;
    }

    /**
     * The provenance that owns a position, for debugging ("why is this block
     * different from canon?"). Asks layers in priority order; the first layer
     * with an opinion wins. Returns {@link Provenance#CANON} if no layer has
     * an opinion (the position is unchanged canon / base terrain).
     */
    public Provenance provenanceAt(int x, int y, int z) {
        for (WorldLayer layer : layers) {
            if (layer.getBlock(x, y, z) != null) return layer.provenance();
        }
        return Provenance.CANON;
    }
}
