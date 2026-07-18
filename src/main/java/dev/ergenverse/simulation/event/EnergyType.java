package dev.ergenverse.simulation.event;

import net.minecraft.core.BlockPos;

/**
 * EnergyType — the typed radii of the WorldEventBus.
 *
 * <p>Per the Constitution (Art XVIII: Never Fake Depth) and the ChatGPT
 * architectural review ("build one enormous event network"), every world
 * disturbance carries an energy type that determines HOW FAR it propagates
 * and WHICH subscribers care about it.
 *
 * <p>Each energy type has a <b>canonical propagation radius</b> (in blocks).
 * These radii are canon-faithful, not arbitrary:
 * <ul>
 *   <li><b>QI</b> (128) — spiritual qi fluctuations. Felt by cultivators
 *       at Qi Condensation+ within roughly one chunk-group. Canon: Wang Lin
 *       senses the Heaven-Defying Bead's qi from a modest distance.</li>
 *   <li><b>ACQUIRE</b> (256) — opportunity-attracting disturbance. The
 *       "treasure draws cultivators" principle. Canon: millennium herbs
 *       attract beasts and cultivators from the surrounding region.</li>
 *   <li><b>SOUL</b> (64) — soul resonance. Short range; soul cultivators
 *       feel it, others don't. Canon: Soul Search, Soul Banner, soul
 *       tribulations are felt only by those attuned.</li>
 *   <li><b>KARMA</b> (512) — karmic ripple. Very long range but only
 *       perceived by those with karma sensitivity. Canon: Wang Lin's
 *       karma whip and karmic observations span great distances.</li>
 *   <li><b>SPIRITUAL</b> (192) — divine-sense-tier spiritual pressure.
 *       Felt by cultivators at Foundation Establishment+ with divine sense.
 *       Canon: divine sense perception range in the novels.</li>
 *   <li><b>PHYSICAL</b> (96) — physical disturbance (explosion, beast
 *       roar, structural collapse). Felt by everyone, not just cultivators.</li>
 *   <li><b>SOCIAL</b> (1024) — rumor / social information propagation.
 *       The longest range — rumors travel between settlements. Canon:
 *       news of Wang Lin's deeds spreads across Zhao within days.</li>
 * </ul>
 *
 * <p><b>Provenance: INFERRED.</b> The specific block radii are gameplay
 * abstractions of canon-described perception ranges. The energy TYPE
 * distinctions (qi vs soul vs karma vs social) are EXPLICIT canon.
 */
public enum EnergyType {
    QI(128),
    ACQUIRE(256),
    SOUL(64),
    KARMA(512),
    SPIRITUAL(192),
    PHYSICAL(96),
    SOCIAL(1024);

    private final int radiusBlocks;

    EnergyType(int radiusBlocks) {
        this.radiusBlocks = radiusBlocks;
    }

    /** The canonical propagation radius in blocks. */
    public int radius() {
        return radiusBlocks;
    }

    /**
     * The squared radius (for fast distance comparisons without sqrt).
     * Cached on first access.
     */
    public long radiusSquared() {
        return (long) radiusBlocks * radiusBlocks;
    }
}
