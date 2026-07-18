package dev.ergenverse.world;

import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.perception.Objective;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-chunk data for the three layers of reality.
 *
 * <p>Per the {@link WorldPhilosophy}: "Instead of one Minecraft world,
 * think of every chunk as containing multiple overlapping layers."
 *
 * <p>Every chunk in the world has three overlapping layers:
 * <ul>
 *   <li><b>Physical</b> — vanilla Minecraft blocks and entities. This is
 *       what everyone sees and touches.</li>
 *   <li><b>Spiritual</b> — spirit veins, Qi currents, formations, spirit
 *       herbs, beast territories. Exists independently. Requires
 *       cultivation to perceive and interact.</li>
 *   <li><b>Dao</b> — karmic traces, historical imprints, residual will,
 *       ancient Dao, heavenly laws. The deepest layer.</li>
 * </ul>
 *
 * <p><b>Why this matters:</b> a mortal can TNT a mountain and the
 * physical rock is destroyed, but a formation anchored in the Spiritual
 * Layer remains untouched. The formation was never "in the rock" — the
 * rock was only the physical shadow.
 */
public final class ChunkLayerData {

    /** The {@link WorldLayer#PHYSICAL} layer is just vanilla Minecraft — we don't store it here. */

    /** Spirit veins, Qi currents, formations, spirit herbs, beast territories. */
    public final List<Objective> spiritualLayer = new ArrayList<>();

    /** Karmic traces, Dao imprints, heavenly laws, historical imprints. */
    public final List<Objective> daoLayer = new ArrayList<>();

    /** The world laws governing this chunk. */
    public WorldLaws laws;

    /** Add a spiritual-layer thing to this chunk. */
    public void addSpiritual(Objective thing) {
        spiritualLayer.add(thing);
    }

    /** Add a Dao-layer thing to this chunk. */
    public void addDao(Objective thing) {
        daoLayer.add(thing);
    }

    /**
     * Get all things in a given layer at a position.
     *
     * <p>Used by the perception engine: when a player sweeps divine sense,
     * we collect all {@link Objective} things in their perception radius
     * and ask the perception engine what they perceive.
     */
    public List<Objective> getInLayer(WorldLayer layer, BlockPos pos) {
        switch (layer) {
            case SPIRITUAL: return spiritualLayer;
            case DAO: return daoLayer;
            default: return List.of();
        }
    }

    /**
     * Can a physical-block break affect this chunk's spiritual/Dao layers?
     *
     * <p>Per the {@link WorldPhilosophy}: never. The physical break only
     * affects the physical layer. Spiritual-layer things are anchored in
     * spiritual space — they may shift according to their own laws, but
     * they are not broken by physical damage.
     */
    public boolean physicalBreakAffectsSpiritual() {
        return false;
    }
}
