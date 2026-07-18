package dev.ergenverse.simulation.actor;

/**
 * SpiritVein — a 5-tier spirit vein that powers a territory's Qi density.
 *
 * <p>Per canon, spirit veins range from "low-grade" (a trickle of Qi) to
 * "immortal-grade" (a sovereign vein that produces enough Qi to sustain an
 * immortal sect). The tier determines ambient Qi density, herb growth rates,
 * beast cultivation ceilings, and sect potential.
 *
 * <p>The 5 tiers (canon audit):
 * <ol>
 *   <li>{@code MORTAL} — Qi density ~0.1. Supports mortal villages only.</li>
 *   <li>{@code LOW} — Qi density ~0.3. Supports Qi Condensation / Foundation sects.</li>
 *   <li>{@code MIDDLE} — Qi density ~0.5. Supports Core Formation / Nascent Soul sects.</li>
 *   <li>{@code HIGH} — Qi density ~0.7. Supports Soul Formation / Ascendant sects.</li>
 *   <li>{@code IMMORTAL} — Qi density ~0.95. Sovereign vein. Attracts immortals.</li>
 * </ol>
 */
public final class SpiritVein {

    public enum Tier {
        MORTAL  ("Mortal-grade",  0.10),
        LOW     ("Low-grade",     0.30),
        MIDDLE  ("Middle-grade",  0.50),
        HIGH    ("High-grade",    0.70),
        IMMORTAL("Immortal-grade",0.95);

        public final String label;
        public final double qiDensity;
        Tier(String label, double qiDensity) {
            this.label = label;
            this.qiDensity = qiDensity;
        }
    }

    public final String id;
    public final String name;
    public final Tier tier;
    public final int blockX;
    public final int blockY;
    public final int blockZ;

    /** Current output fraction (0..1). Depleted by use, regenerates over time. */
    public double outputFraction;

    public SpiritVein(String id, String name, Tier tier, int x, int y, int z) {
        this.id = id;
        this.name = name;
        this.tier = tier;
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        this.outputFraction = 1.0;
    }

    public double currentQiOutput() {
        return tier.qiDensity * outputFraction;
    }

    public void deplete(double amount) {
        this.outputFraction = Math.max(0, this.outputFraction - amount);
    }

    public void regenerate(double amount) {
        this.outputFraction = Math.min(1, this.outputFraction + amount);
    }
}
