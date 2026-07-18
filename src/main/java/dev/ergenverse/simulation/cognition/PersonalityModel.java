package dev.ergenverse.simulation.cognition;

import java.util.HashMap;
import java.util.Map;

/**
 * PersonalityModel — the personality of a cognition-driven actor.
 *
 * <p><b>Critical design decision (canon audit):</b> personality is modeled as
 * CURVES, not flat values. A trait's value is not a single number — it is a
 * function of CONTEXT. {@code courage} is high in "defend_family" context
 * but lower in "confront-ancient-immortal" context.
 *
 * <p>A {@link TraitCurve} is a {@code Map<context, value>} where value ∈ [0,1].
 * The {@link #scoreOption} function is THE critical function in the cognition
 * pipeline: given an {@link ActionOption} and a context, it produces a single
 * decision score that the {@link DecisionEngine} uses to pick the winner.
 *
 * <p>Canonical RI personality traits:
 * <ul>
 *   <li>{@code courage} — willingness to engage risk</li>
 *   <li>{@code patience} — willingness to wait for opportunities</li>
 *   <li>{@code greed} — desire for resources and treasures</li>
 *   <li>{@code loyalty} — fidelity to sect / master / sworn brothers</li>
 *   <li>{@code ruthlessness} — willingness to kill / harm</li>
 *   <li>{@code caution} — risk-aversion, planning depth</li>
 *   <li>{@code curiosity} — drive to investigate the unknown</li>
 *   <li>{@code ambition} — drive to climb the cultivation ladder</li>
 *   <li>{@code face} — sensitivity to public reputation</li>
 *   <li>{@code honor} — commitment to moral principles</li>
 *   <li>{@code dao_heart} — resistance to heart demons</li>
 * </ul>
 */
public final class PersonalityModel {

    /** A trait curve maps context strings → values in [0,1]. */
    public static final class TraitCurve {
        public final String traitName;
        public final Map<String, Double> curve = new HashMap<>();

        public TraitCurve(String traitName) {
            this.traitName = traitName;
        }

        public TraitCurve set(String context, double value) {
            curve.put(context, clamp01(value));
            return this;
        }

        /** Lookup value with a default fallback. */
        public double get(String context, double fallback) {
            Double v = curve.get(context);
            return v == null ? clamp01(fallback) : v;
        }

        /** Lookup value, defaulting to the curve's average if context is unknown. */
        public double getOrDefault(String context) {
            Double v = curve.get(context);
            if (v != null) return v;
            if (curve.isEmpty()) return 0.5;
            double sum = 0;
            for (double d : curve.values()) sum += d;
            return sum / curve.size();
        }
    }

    private final Map<String, TraitCurve> traits = new HashMap<>();

    public PersonalityModel() {
        // Default empty curves for all canonical traits.
        for (String t : CANONICAL_TRAITS) {
            traits.put(t, new TraitCurve(t));
        }
    }

    public TraitCurve trait(String name) {
        return traits.computeIfAbsent(name, TraitCurve::new);
    }

    public PersonalityModel set(String trait, String context, double value) {
        trait(trait).set(context, value);
        return this;
    }

    public double get(String trait, String context) {
        TraitCurve c = traits.get(trait);
        return c == null ? 0.5 : c.getOrDefault(context);
    }

    /**
     * THE critical function of the cognition pipeline.
     *
     * <p>Given an ActionOption and the current context, produce a single
     * decision score in [−1, +1]. Higher scores = more attractive.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>For each trait alignment in the option, look up the personality
     *       value at the given context.</li>
     *   <li>Multiply alignment (−1..+1) by personality value (0..1) and sum.</li>
     *   <li>Add expected-reward term: {@code reward × successProbability × ambition}.</li>
     *   <li>Subtract risk term: {@code risk × caution × (1 − courage)}.</li>
     *   <li>Subtract karmic cost weighted by honor + dao_heart.</li>
     *   <li>Clamp to [−1, +1].</li>
     * </ol>
     */
    public double scoreOption(ActionOption option, String context) {
        double alignmentSum = 0;
        int n = 0;
        for (Map.Entry<String, Double> e : option.traitAlignments.entrySet()) {
            double pVal = get(e.getKey(), context);
            alignmentSum += e.getValue() * (pVal * 2 - 1); // shift [0,1]→[−1,+1]
            n++;
        }
        double alignmentTerm = (n == 0) ? 0 : alignmentSum / n;

        double ambition = get("ambition", context);
        double rewardTerm = option.reward * option.successProbability * ambition;

        double caution = get("caution", context);
        double courage = get("courage", context);
        double riskTerm = option.risk * caution * (1 - courage);

        double honor = get("honor", context);
        double daoHeart = get("dao_heart", context);
        double karmaTerm = option.karmicCost * (honor + daoHeart) * 0.5;

        double raw = alignmentTerm + rewardTerm - riskTerm - karmaTerm;
        if (raw < -1) return -1;
        if (raw > 1) return 1;
        return raw;
    }

    public Map<String, TraitCurve> allTraits() {
        return traits;
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    /** The 11 canonical RI personality traits. */
    public static final String[] CANONICAL_TRAITS = {
            "courage", "patience", "greed", "loyalty", "ruthlessness",
            "caution", "curiosity", "ambition", "face", "honor", "dao_heart"
    };
}
