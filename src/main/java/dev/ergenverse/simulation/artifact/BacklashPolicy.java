package dev.ergenverse.simulation.artifact;

/**
 * Evaluates contextual backlash — the damage a cultivator suffers when
 * forcing a treasure beyond their capability.
 *
 * <h2>The core principle (user directive, canon-faithful)</h2>
 * <blockquote>
 *   Not: "high-level item = hurts low-level user"<br>
 *   Instead: "high-level item = dangerous when forcing beyond capability"
 * </blockquote>
 * <ul>
 *   <li>Swinging a heavy divine sword — fine (just tiring).</li>
 *   <li>Forcing open a sealed restriction — meridian damage, soul injury.</li>
 *   <li>Forcing a treasure spirit to obey — rejection, retaliation.</li>
 *   <li>Overdrawing energy — exhaustion, qi depletion, regression.</li>
 * </ul>
 *
 * <p>There is NO code path that damages the player for HOLDING an
 * over-leveled treasure. The HOLDING action always produces risk 0.0.
 */
public final class BacklashPolicy {

    private BacklashPolicy() {}

    /**
     * Evaluate the backlash risk and type for a given usage scenario.
     *
     * @return a result with risk (0..1) and type. Risk 0 = safe.
     */
    public static BacklashResult evaluate(UsageContext ctx,
                                           ArtifactUsageProfile profile,
                                           ArtifactUsageLayer expressedLayer) {

        UsageAction action = ctx.action();

        // ── Actions that NEVER backlash ───────────────────────────
        if (action == UsageAction.HOLDING || action == UsageAction.PASSIVE_BENEFIT) {
            return BacklashResult.safe();
        }

        // ── SWINGING — only fatigue from extreme weight ────────────
        if (action == UsageAction.SWINGING) {
            int gap = ctx.realmGap();
            if (gap <= 3) return BacklashResult.safe();
            // Extremely heavy treasure, low realm wielder
            double fatigueRisk = Math.min(0.3, (gap - 3) * 0.05);
            return new BacklashResult(fatigueRisk, BacklashType.PHYSICAL_EXHAUSTION);
        }

        // ── All remaining actions require energy and can backlash ─

        double baseRisk = action.baseBacklashMultiplier;
        double realmGapFactor = realmGapFactor(ctx);
        double energyFactor = energyFactor(ctx);
        double spiritFactor = spiritFactor(ctx, profile);
        double treasureModifier = profile.backlashProfile().riskMultiplier();

        double totalRisk = baseRisk * realmGapFactor * energyFactor
                          * spiritFactor * treasureModifier;

        // Cap at 1.0
        totalRisk = Math.min(1.0, totalRisk);

        // Determine the backlash type based on the risk level and action
        BacklashType type = determineType(action, totalRisk, ctx, profile);

        return new BacklashResult(totalRisk, type);
    }

    // ── Risk factors ───────────────────────────────────────────────

    /**
     * How much the realm gap contributes to backlash.
     * 0..1. At or above artifact realm: 0. Below: scales with gap.
     */
    private static double realmGapFactor(UsageContext ctx) {
        int gap = ctx.realmGap();
        if (gap <= 0) return 0.0;
        // Each realm of gap adds 0.15 risk factor, soft-capped
        return Math.min(1.0, gap * 0.15);
    }

    /**
     * How much energy deficit contributes to backlash.
     * If the wielder has plenty of energy, this is near 0.
     * If they're overdrawing, this spikes.
     */
    private static double energyFactor(UsageContext ctx) {
        if (ctx.action() != UsageAction.OVERDRAWING) {
            // For non-overdraw actions, energy only matters if very low
            if (ctx.qiAvailable() >= 0.3) return 0.2; // minimal baseline
            return 0.2 + (0.3 - ctx.qiAvailable()) * 2.0; // spike when low
        }
        // OVERDRAWING: risk is proportional to how little reserve they have
        return 0.5 + (1.0 - ctx.energyReserve()) * 0.5;
    }

    /**
     * How spirit recognition affects backlash.
     * Forcing a spirit that doesn't recognize you is the #1 cause
     * of serious backlash in canon.
     */
    private static double spiritFactor(UsageContext ctx, ArtifactUsageProfile profile) {
        if (!profile.hasSpirit()) return 0.5; // neutral, no spirit

        TreasureSpirit.Recognition rec = ctx.spiritRecognition();
        if (rec == null) rec = TreasureSpirit.Recognition.NONE;

        // If the action doesn't involve the spirit, recognition doesn't matter much
        if (ctx.action() != UsageAction.COMMANDING_SPIRIT) {
            return switch (rec) {
                case NONE -> 0.7;        // unknown spirit, slightly risky
                case SENSED -> 0.6;
                case ACKNOWLEDGED -> 0.5;
                case RECOGNIZED -> 0.3;
                case UNIFIED -> 0.1;     // unified spirit actually protects
            };
        }

        // COMMANDING_SPIRIT — recognition is THE factor
        return switch (rec) {
            case NONE -> 1.5;            // commanding an unknown spirit: very dangerous
            case SENSED -> 1.2;          // the spirit knows you're there, doesn't obey
            case ACKNOWLEDGED -> 0.8;    // acknowledged but not obeying: risky
            case RECOGNIZED -> 0.1;      // recognized master: safe
            case UNIFIED -> 0.0;         // unified: perfectly safe
        };
    }

    // ── Type determination ─────────────────────────────────────────

    private static BacklashType determineType(UsageAction action, double risk,
                                                UsageContext ctx,
                                                ArtifactUsageProfile profile) {
        // Spirit actions have spirit-specific backlash types
        if (action == UsageAction.COMMANDING_SPIRIT && profile.hasSpirit()) {
            TreasureSpirit.Recognition rec = ctx.spiritRecognition();
            if (rec == null) rec = TreasureSpirit.Recognition.NONE;
            if (rec.level < TreasureSpirit.Recognition.RECOGNIZED.level) {
                if (risk >= 0.65) return BacklashType.POSSESSION;
                if (risk >= 0.50) return BacklashType.SPIRIT_RETALIATION;
                if (risk >= 0.30) return BacklashType.SPIRIT_REJECTION;
            }
        }

        // Forcing restrictions has special types
        if (action == UsageAction.FORCING_RESTRICTION) {
            if (risk >= 0.70) return BacklashType.CULTIVATION_REGRESSION;
            if (risk >= 0.55) return BacklashType.KARMIC_DEBT;
            if (risk >= 0.45) return BacklashType.SOUL_INJURY;
            if (risk >= 0.15) return BacklashType.MERIDIAN_STRAIN;
        }

        // Overdrawing energy
        if (action == UsageAction.OVERDRAWING) {
            if (risk >= 0.70) return BacklashType.CULTIVATION_REGRESSION;
            if (risk >= 0.25) return BacklashType.QI_DEPLETION;
            if (risk >= 0.15) return BacklashType.MERIDIAN_STRAIN;
        }

        // Generic activation backlash
        if (risk >= 0.70) return BacklashType.SOUL_INJURY;
        if (risk >= 0.45) return BacklashType.MERIDIAN_STRAIN;
        if (risk >= 0.25) return BacklashType.QI_DEPLETION;

        // Below all floors
        return BacklashType.NONE;
    }

    // ── Result record ──────────────────────────────────────────────

    /**
     * The result of backlash evaluation.
     */
    public record BacklashResult(
            /** Backlash risk 0..1. 0 = completely safe. */
            double risk,
            /** The type of backlash that would occur. */
            BacklashType type
    ) {
        public static BacklashResult safe() {
            return new BacklashResult(0.0, BacklashType.NONE);
        }

        public boolean isSafe() {
            return risk <= 0.05 || type == BacklashType.NONE;
        }
    }
}