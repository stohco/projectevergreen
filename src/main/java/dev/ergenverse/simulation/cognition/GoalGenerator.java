package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * GoalGenerator — converts unsatisfied needs into goals.
 *
 * <p>Pipeline:
 * <ol>
 *   <li>Read the actor's {@link PhysicalState}, {@link CultivationState},
 *       {@link SocialState}, and need-intensity map.</li>
 *   <li>For each need with intensity &gt; {@link Need#threshold}, produce a
 *       {@link CognitionGoal}.</li>
 *   <li>Apply the actor's {@link DaoIdentity} weight modifier to each goal's
 *       priority.</li>
 *   <li>Return the sorted list (highest effective weight first).</li>
 * </ol>
 *
 * <p>The mapping from {@link Need} → {@link CognitionGoal.Category} is the
 * canon audit's "need-goal mapping table".
 */
public final class GoalGenerator {

    private GoalGenerator() {}

    public static List<CognitionGoal> generate(
            DaoIdentity dao,
            java.util.Map<Need, Double> needIntensities,
            PhysicalState physical,
            CultivationState cultivation,
            SocialState social,
            java.util.List<DesireState> desires) {

        List<CognitionGoal> goals = new ArrayList<>();

        // (1) Need-driven goals (existing pipeline)
        for (Map.Entry<Need, Double> e : needIntensities.entrySet()) {
            Need need = e.getKey();
            double intensity = e.getValue();
            if (intensity < need.threshold) continue;

            CognitionGoal.Category cat = categoryFor(need, physical, cultivation, social);
            double daoMod = dao.goalWeightModifier(cat);
            double urgency = computeUrgency(need, intensity, physical);
            double priority = clamp01(intensity * daoMod);

            String desc = need.label + " (intensity=" + round2(intensity) + ")";
            goals.add(new CognitionGoal(need, cat, desc, urgency, priority));
        }

        // (2) Desire-driven goals (Art XXXI — the world must desire)
        // Each active desire produces a SOCIAL goal. The existing
        // IntentEngine + DecisionEngine pipeline handles the rest.
        // No new engines. No new buses. Data in, behavior out.
        if (desires != null) {
            for (DesireState d : desires) {
                // Dormant desires (urgency < 0.3) don't become goals yet.
                if (d.urgency() < 0.3) continue;
                // urgency from JSON, priority weighted by Dao
                double daoMod = dao.goalWeightModifier(CognitionGoal.Category.SOCIAL);
                double priority = clamp01(d.urgency() * daoMod);
                String desc = d.what() + " [" + d.socialEngine() + " → " + d.target() + "]";
                goals.add(new CognitionGoal(null, CognitionGoal.Category.SOCIAL,
                        desc, d.urgency(), priority));
            }
        }

        goals.sort((a, b) -> Double.compare(
                b.effectiveWeight(dao.goalWeightModifier(b.category)),
                a.effectiveWeight(dao.goalWeightModifier(a.category))));
        return goals;
    }

    private static CognitionGoal.Category categoryFor(Need need, PhysicalState p,
                                                       CultivationState c, SocialState s) {
        switch (need) {
            case FOOD: case WATER: case SHELTER: case SAFETY:
                return CognitionGoal.Category.SURVIVE;
            case REST:
                return CognitionGoal.Category.WAIT;
            case QI:
                return CognitionGoal.Category.GATHER_RESOURCE;
            case SEEKING_DAO:
                return CognitionGoal.Category.SEEKING_DAO;
            case DAO_HEART:
                return CognitionGoal.Category.MEDITATE;
            case BREAKTHROUGH:
                return CognitionGoal.Category.BREAKTHROUGH;
            case TRIBULATION_DEBT:
                return CognitionGoal.Category.MEDITATE;
            case RESOURCE:
                return CognitionGoal.Category.GATHER_RESOURCE;
            case AFFECTION: case BELONGING:
                return CognitionGoal.Category.OFFER_FAVOR;
            case FACE: case REPUTATION: case STATUS:
                return CognitionGoal.Category.POLITICS;
            case KARMIC_DEBT:
                return CognitionGoal.Category.RESOLVE_DEBT;
            case HEART_DEMON:
                return CognitionGoal.Category.MEDITATE;
            case FILIAL_PIETY: case SWORN_BROTHERHOOD: case GRATITUDE:
                return CognitionGoal.Category.KEEP_PROMISE;
            case REVENGE:
                return CognitionGoal.Category.KILL;
            case KNOWLEDGE:
                return CognitionGoal.Category.STUDY;
            case CRAFT:
                return CognitionGoal.Category.CRAFT;
            case FREEDOM:
                return CognitionGoal.Category.FLEE;
            case CURIOSITY:
                return CognitionGoal.Category.EXPLORE;
            case LEGACY:
                return CognitionGoal.Category.LEGACY;
            case TRANSCENDENCE:
                return CognitionGoal.Category.SEEKING_DAO;
            default:
                return CognitionGoal.Category.OTHER;
        }
    }

    private static double computeUrgency(Need need, double intensity, PhysicalState p) {
        // Survival needs scale with how close to death the actor is.
        if (need == Need.FOOD || need == Need.WATER || need == Need.SAFETY) {
            double mortalityPressure = 1.0 - p.healthFraction;
            return clamp01(intensity * 0.5 + mortalityPressure * 0.5);
        }
        return clamp01(intensity);
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    private static double round2(double v) {
        return Math.round(v * 100) / 100.0;
    }
}
