package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.List;

/**
 * Planner — turns a goal into a list of {@link ActionOption}s.
 *
 * <p>Given the highest-ranked {@link CognitionGoal} from the
 * {@link GoalGenerator}, the Planner selects the subset of
 * canonical actions that could pursue that goal, computes their
 * success probability / reward / risk / karmic cost based on the
 * actor's state, and returns the option list for the
 * {@link PersonalityModel} to score.
 *
 * <p>Canon audit principle: options are NEVER pre-filtered by Dao
 * Identity. The Dao Identity shapes priority; the Personality shapes
 * choice. Filtering options by Dao would collapse personality into a
 * single dimension.
 */
public final class Planner {

    private Planner() {}

    public static List<ActionOption> plan(CognitionGoal goal,
                                          PhysicalState physical,
                                          CultivationState cultivation,
                                          SocialState social) {
        List<ActionOption> all = ActionOption.canonicalSet();
        List<ActionOption> out = new ArrayList<>();

        for (ActionOption opt : all) {
            if (!applicable(opt, goal, physical, cultivation, social)) continue;
            fillEstimates(opt, goal, physical, cultivation, social);
            out.add(opt);
        }
        return out;
    }

    /** Returns true if the option could plausibly pursue this goal. */
    private static boolean applicable(ActionOption opt, CognitionGoal goal,
                                       PhysicalState p, CultivationState c, SocialState s) {
        // Most actions are always "applicable" — the personality filters by score.
        // A few are gated by hard prerequisites:
        switch (opt.id) {
            case BREAKTHROUGH:   return goal.category == CognitionGoal.Category.BREAKTHROUGH
                                        && c.canAttemptBreakthrough();
            case SEEK_DAO:       return goal.category == CognitionGoal.Category.SEEKING_DAO;
            case TAKE_REVENGE:   return goal.category == CognitionGoal.Category.KILL
                                        && s.hasEnemy();
            case DEFEND_TERRITORY: return s.hasTerritory;
            case FOUND_SECT:     return c.realmOrder() >= 4; // Nascent Soul+
            case ACCEPT_DISCIPLE: case OFFER_DISCIPLE:
                return c.realmOrder() >= 3;
            default:
                return true;
        }
    }

    /** Fill in successProbability, reward, risk, karmicCost, traitAlignments. */
    private static void fillEstimates(ActionOption opt, CognitionGoal goal,
                                       PhysicalState p, CultivationState c, SocialState s) {
        // Default neutral estimates; override per action.
        opt.successProbability = 0.5;
        opt.reward = 0.5;
        opt.risk = 0.3;
        opt.karmicCost = 0.0;

        switch (opt.id) {
            case FIGHT:
                opt.withTrait("courage", +1).withTrait("ruthlessness", +0.6);
                opt.risk = 0.7; opt.reward = 0.6;
                opt.successProbability = clamp01(0.5 + (c.realmOrder() - 3) * 0.05);
                break;
            case FLEE:
                opt.withTrait("courage", -0.6).withTrait("caution", +1);
                opt.successProbability = 0.8; opt.risk = 0.2; opt.reward = 0.3;
                break;
            case HIDE:
                opt.withTrait("caution", +0.8).withTrait("courage", -0.4);
                opt.successProbability = 0.7; opt.risk = 0.1; opt.reward = 0.3;
                break;
            case CALL_HELP:
                opt.withTrait("loyalty", +0.8).withTrait("courage", -0.3);
                opt.successProbability = 0.5; opt.risk = 0.2; opt.reward = 0.5;
                break;
            case MEDITATE:
                opt.withTrait("patience", +1).withTrait("dao_heart", +0.8);
                opt.successProbability = 0.9; opt.risk = 0.05; opt.reward = 0.4;
                break;
            case BREAKTHROUGH:
                opt.withTrait("ambition", +1).withTrait("courage", +0.7).withTrait("dao_heart", +0.8);
                opt.successProbability = c.breakthroughChance();
                opt.risk = 0.5; opt.reward = 1.0;
                opt.karmicCost = 0.05;
                break;
            case STUDY_FORMATION:
                opt.withTrait("curiosity", +0.8).withTrait("patience", +0.7);
                opt.successProbability = 0.7; opt.risk = 0.1; opt.reward = 0.4;
                break;
            case BREAK_FORMATION:
                opt.withTrait("courage", +0.6).withTrait("curiosity", +0.7);
                opt.successProbability = 0.4; opt.risk = 0.4; opt.reward = 0.6;
                break;
            case CRAFT_TALISMAN: case CRAFT_PILL:
                opt.withTrait("patience", +0.7).withTrait("curiosity", +0.5);
                opt.successProbability = 0.6; opt.risk = 0.15; opt.reward = 0.5;
                break;
            case TRADE:
                opt.withTrait("greed", +0.5).withTrait("face", +0.4);
                opt.successProbability = 0.7; opt.risk = 0.15; opt.reward = 0.5;
                break;
            case STEAL:
                opt.withTrait("greed", +0.8).withTrait("ruthlessness", +0.6)
                   .withTrait("honor", -0.7);
                opt.successProbability = 0.4; opt.risk = 0.5; opt.reward = 0.7;
                opt.karmicCost = 0.2;
                break;
            case KILL_OWNER:
                opt.withTrait("ruthlessness", +1).withTrait("courage", +0.7)
                   .withTrait("honor", -0.8).withTrait("dao_heart", -0.4);
                opt.successProbability = 0.5; opt.risk = 0.6; opt.reward = 0.8;
                opt.karmicCost = 0.5;
                break;
            case OFFER_FAVOR:
                opt.withTrait("loyalty", +0.6).withTrait("honor", +0.5);
                opt.successProbability = 0.7; opt.risk = 0.1; opt.reward = 0.4;
                break;
            case WAIT:
                opt.withTrait("patience", +1).withTrait("caution", +0.6);
                opt.successProbability = 0.9; opt.risk = 0.0; opt.reward = 0.2;
                break;
            case EXPLORE:
                opt.withTrait("curiosity", +1).withTrait("courage", +0.5);
                opt.successProbability = 0.6; opt.risk = 0.4; opt.reward = 0.5;
                break;
            case DECEIVE:
                opt.withTrait("courage", -0.2).withTrait("honor", -0.5)
                   .withTrait("face", +0.4);
                opt.successProbability = 0.5; opt.risk = 0.3; opt.reward = 0.5;
                opt.karmicCost = 0.15;
                break;
            case INVESTIGATE:
                opt.withTrait("curiosity", +1).withTrait("caution", +0.4);
                opt.successProbability = 0.7; opt.risk = 0.2; opt.reward = 0.4;
                break;
            case SEEK_DAO:
                opt.withTrait("ambition", +0.8).withTrait("dao_heart", +0.9)
                   .withTrait("patience", +0.7);
                opt.successProbability = 0.3; opt.risk = 0.3; opt.reward = 1.0;
                break;
            case TAKE_REVENGE:
                opt.withTrait("ruthlessness", +1).withTrait("loyalty", +0.6)
                   .withTrait("honor", -0.6).withTrait("dao_heart", -0.5);
                opt.successProbability = 0.4; opt.risk = 0.6; opt.reward = 0.8;
                opt.karmicCost = 0.4;
                break;
            case HONOR_PROMISE:
                opt.withTrait("honor", +1).withTrait("loyalty", +0.7);
                opt.successProbability = 0.8; opt.risk = 0.3; opt.reward = 0.4;
                break;
            case DEFEND_TERRITORY:
                opt.withTrait("courage", +0.8).withTrait("loyalty", +0.6);
                opt.successProbability = 0.6; opt.risk = 0.5; opt.reward = 0.6;
                break;
            case GATHER_HERB: case GATHER_QI:
                opt.withTrait("greed", +0.5).withTrait("patience", +0.4);
                opt.successProbability = 0.7; opt.risk = 0.2; opt.reward = 0.5;
                break;
            case SUBMIT:
                opt.withTrait("courage", -0.8).withTrait("face", -0.6)
                   .withTrait("patience", +0.6);
                opt.successProbability = 0.8; opt.risk = 0.1; opt.reward = 0.3;
                break;
            case CORRUPT_TARGET:
                opt.withTrait("ruthlessness", +0.8).withTrait("honor", -0.6)
                   .withTrait("dao_heart", -0.5);
                opt.successProbability = 0.4; opt.risk = 0.4; opt.reward = 0.6;
                opt.karmicCost = 0.3;
                break;
            case OFFER_DISCIPLE: case ACCEPT_DISCIPLE:
                opt.withTrait("loyalty", +0.5).withTrait("face", +0.5);
                opt.successProbability = 0.6; opt.risk = 0.15; opt.reward = 0.5;
                break;
            case LEAVE_SECT:
                opt.withTrait("ambition", +0.6).withTrait("loyalty", -0.6)
                   .withTrait("face", -0.4);
                opt.successProbability = 0.7; opt.risk = 0.3; opt.reward = 0.4;
                break;
            case FOUND_SECT:
                opt.withTrait("ambition", +1).withTrait("face", +0.7)
                   .withTrait("loyalty", +0.5);
                opt.successProbability = 0.3; opt.risk = 0.5; opt.reward = 0.9;
                break;
            default:
                break;
        }
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
