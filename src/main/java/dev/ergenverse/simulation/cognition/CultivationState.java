package dev.ergenverse.simulation.cognition;

/**
 * CultivationState (cognition-side) — the cultivation progress that drives
 * the cognition pipeline.
 *
 * <p><b>Exponential planning horizon (canon audit):</b> higher-realm
 * cultivators plan further into the future. Mortals plan 1 day ahead;
 * Qi Condensation: 1 week; Foundation: 1 month; Nascent Soul: 1 year;
 * Soul Formation: 10 years; Ascendant+: centuries → millennia.
 * This is encoded in {@link #planningHorizonDays()}.
 *
 * <p>This is intentionally lighter than {@link dev.ergenverse.cultivation.CultivationState}
 * — the cognition system needs only what affects decisions, not the
 * full per-player record.
 */
public final class CultivationState {

    /** Realm order (matches {@link dev.ergenverse.cultivation.RealmId#order}). */
    private int realmOrder;
    private double qiFraction;          // 0..1
    private double daoHeartStability;   // 0..1
    private double karmicDebt;          // 0..1
    private double breakthroughReadiness; // 0..1
    private boolean inSeclusion;

    public CultivationState() {
        this.realmOrder = 0;
        this.qiFraction = 0.0;
        this.daoHeartStability = 0.7;
        this.karmicDebt = 0.0;
        this.breakthroughReadiness = 0.0;
        this.inSeclusion = false;
    }

    public int realmOrder()                     { return realmOrder; }
    public double qiFraction()                  { return qiFraction; }
    public double daoHeartStability()           { return daoHeartStability; }
    public double karmicDebt()                  { return karmicDebt; }
    public double breakthroughReadiness()       { return breakthroughReadiness; }
    public boolean inSeclusion()                { return inSeclusion; }

    public void setRealmOrder(int v)            { this.realmOrder = Math.max(0, v); }
    public void setQiFraction(double v)         { this.qiFraction = clamp01(v); }
    public void setDaoHeartStability(double v)  { this.daoHeartStability = clamp01(v); }
    public void setKarmicDebt(double v)         { this.karmicDebt = clamp01(v); }
    public void setBreakthroughReadiness(double v) { this.breakthroughReadiness = clamp01(v); }
    public void setInSeclusion(boolean v)       { this.inSeclusion = v; }

    public boolean canAttemptBreakthrough() {
        return qiFraction > 0.8
                && daoHeartStability > 0.5
                && breakthroughReadiness > 0.6;
    }

    public double breakthroughChance() {
        double base = 0.3 + qiFraction * 0.2 + daoHeartStability * 0.2;
        double karmaPenalty = karmicDebt * 0.4;
        double chance = base - karmaPenalty;
        if (chance < 0.05) return 0.05;
        if (chance > 0.95) return 0.95;
        return chance;
    }

    /**
     * Exponential planning horizon (canon audit requirement).
     *
     * <p>{@code horizon = baseDays * 7^realmStep}, where realmStep is
     * realmOrder / 2 (clamped). Mortals (step 0) plan 1 day ahead;
     * Qi Condensation (step 0-1) ~1 week; Foundation (step 1) ~1 week;
     * Nascent Soul (step 2) ~1 month; Soul Formation (step 3) ~1 year;
     * Ascendant+ → centuries.
     */
    public long planningHorizonDays() {
        int step = Math.min(realmOrder / 2, 8);
        long base = 1L; // 1 day
        for (int i = 0; i < step; i++) base *= 7;
        return base;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
