package dev.ergenverse.world;

import dev.ergenverse.core.WorldPhilosophy;

/**
 * The three layers of reality that every chunk contains.
 *
 * <p>Per the {@link WorldPhilosophy}: "Instead of one Minecraft world,
 * think of every chunk as containing multiple overlapping layers."
 *
 * <ul>
 *   <li><b>PHYSICAL</b> — trees, stone, water, animals, villages. Everyone
 *       occupies and perceives this layer. This is what vanilla Minecraft
 *       already provides.</li>
 *   <li><b>SPIRITUAL</b> — spirit veins, Qi currents, formations, spirit
 *       herbs, beast territories. Exists independently of any observer.
 *       Requires cultivation to perceive and interact. A mortal pickaxe
 *       cannot break things in this layer.</li>
 *   <li><b>DAO</b> — karmic traces, historical imprints, residual will,
 *       ancient Dao, heavenly laws. The deepest layer. Requires high
 *       cultivation (typically Soul Formation+) to perceive, and even
 *       higher to interact with.</li>
 * </ul>
 *
 * <p><b>Key invariant:</b> existence is observer-independent. A formation
 * in the Spiritual Layer exists whether or not any cultivator is present.
 * A mortal walking past does not destroy it, does not perceive it, and
 * cannot interact with it — because the mortal's existence does not
 * extend into that layer.
 *
 * <p><b>Why this matters:</b> a mortal can TNT a mountain and the
 * physical rock is destroyed, but a formation anchored in the Spiritual
 * Layer remains untouched. The formation was never "in the rock" — the
 * rock was only the physical shadow. The formation may shift according
 * to its own laws, but it does not break.
 */
public enum WorldLayer {
    /** Trees, stone, water, animals, villages. Everyone sees and touches this. */
    PHYSICAL(0, "Physical"),

    /** Spirit veins, Qi currents, formations, spirit herbs, beast territories. */
    SPIRITUAL(1, "Spiritual"),

    /** Karmic traces, historical imprints, residual will, ancient Dao, heavenly laws. */
    DAO(2, "Dao");

    public final int depth;
    public final String label;

    WorldLayer(int depth, String label) {
        this.depth = depth;
        this.label = label;
    }
}
