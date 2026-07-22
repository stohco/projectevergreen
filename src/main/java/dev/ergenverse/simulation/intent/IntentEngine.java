package dev.ergenverse.simulation.intent;

import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DaoIdentity;
import dev.ergenverse.simulation.cognition.PersonalityModel;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;

import java.util.List;

/**
 * IntentEngine — derives the immediate Intent from Goal + Identity + World State.
 *
 * <p>This is THE bridge between the cognition pipeline and the action pipeline.
 * Per the ChatGPT architectural review: "Identity → Goals → Immediate Intent
 * → Decision → Planner → Tasks → Minecraft Goals → World Changes."
 *
 * <p>Without this engine, NPCs jump straight from "I want X" (Goal) to "do Y"
 * (Action), which produces generic behavior. With this engine, the NPC first
 * frames HOW they want to approach the goal — cautiously, deceptively,
 * aggressively, observantly — and the Action is then chosen to serve that
 * framing.
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>Take the actor's active {@link CognitionGoal} (what they want).</li>
 *   <li>Take the actor's {@link DaoIdentity} (who they are).</li>
 *   <li>Query recent {@link WorldEvent}s from the {@link WorldEventBus}
 *       to understand the current situation (are there threats nearby?
 *       opportunities? witnesses?).</li>
 *   <li>Score each of the 17 {@link IntentNature} types against the
 *       goal + identity + situation.</li>
 *   <li>Return the highest-scoring Intent.</li>
 * </ol>
 *
 * <h2>Canon-faithful example: Wang Lin</h2>
 * <p>Wang Lin's Dao is DEFIANCE. His goalWeightModifier boosts SEEKING_DAO
 * and BREAKTHROUGH, and heavily penalizes SUBMIT and FLEE. So when his
 * active Goal is SEEKING_DAO, the IntentEngine should produce:
 * <ul>
 *   <li>AVOID_REVEALING_STRENGTH (default — he almost always hides his power)</li>
 *   <li>OBSERVE_FROM_DISTANCE (when others are present)</li>
 *   <li>SEEK_OPPORTUNITY (when alone and safe)</li>
 *   <li>EXPLORE_CAUTIOUSLY (when investigating an unknown area)</li>
 * </ul>
 * <p>He should NOT produce ESTABLISH_DOMINANCE or PROVOKE unless cornered —
 * those violate his DEFIANCE Dao's cautious survivor nature.
 *
 * <p><b>Provenance: INFERRED.</b> The scoring weights are distilled from
 * Wang Lin's behavioral patterns in Renegade Immortal.
 */
public final class IntentEngine {

    /** How many recent world events to consider for situational awareness. */
    private static final int SITUATION_WINDOW = 5;

    private IntentEngine() {}

    /**
     * Derive the immediate Intent for an actor.
     *
     * @param activeGoal   the actor's current active goal (may be null)
     * @param dao          the actor's Dao identity
     * @param personality  the actor's personality model (for cautious/aggressive traits)
     * @param actorId      the actor's ID (for diagnostics)
     * @param actorX       the actor's block X (for querying nearby events)
     * @param actorZ       the actor's block Z
     * @param currentTick  the current server tick
     * @return the highest-scoring Intent, or null if no goal is active
     */
    public static Intent derive(CognitionGoal activeGoal,
                                 DaoIdentity dao,
                                 PersonalityModel personality,
                                 String actorId,
                                 int actorX, int actorZ,
                                 long currentTick) {
        if (activeGoal == null) return null;

        // Score each IntentNature against the current situation.
        IntentNature bestNature = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestTarget = "";

        for (IntentNature nature : IntentNature.values()) {
            double score = scoreIntent(nature, activeGoal, dao, personality,
                    actorX, actorZ, currentTick);
            if (score > bestScore) {
                bestScore = score;
                bestNature = nature;
                bestTarget = inferTarget(nature, activeGoal);
            }
        }

        if (bestNature == null) return null;

        // Expected duration: how long does this intent typically last?
        long duration = expectedDuration(bestNature, activeGoal);

        // Urgency: derived from the source goal's urgency
        double urgency = activeGoal.urgency;

        return Intent.of(bestNature, bestTarget, activeGoal, urgency,
                duration, currentTick);
    }

    /**
     * Score a single IntentNature against the current situation.
     *
     * <p>The score combines:
     * <ul>
     *   <li><b>Goal alignment</b> — does this intent serve the goal's category?</li>
     *   <li><b>Dao alignment</b> — does this intent fit the actor's Dao identity?</li>
     *   <li><b>Situational modifiers</b> — nearby events (threats, opportunities, witnesses)</li>
     * </ul>
     */
    private static double scoreIntent(IntentNature nature, CognitionGoal goal,
                                       DaoIdentity dao, PersonalityModel personality,
                                       int actorX, int actorZ, long currentTick) {
        double score = 0.0;

        // (1) Goal alignment — does this intent serve this goal category?
        score += goalAlignment(nature, goal.category);

        // (2) Dao alignment — does this intent fit the actor's Dao?
        score += daoAlignment(nature, dao);

        // (3) Situational modifiers from the WorldEventBus
        score += situationalModifier(nature, actorX, actorZ, currentTick);

        // (4) Personality modifier (cautious NPCs prefer observe/retreat;
        //     aggressive NPCs prefer provoke/ambush)
        score += personalityModifier(nature, personality);

        return score;
    }

    /**
     * Goal alignment — how well does this intent nature serve this goal category?
     * Returns a bonus (positive) or penalty (negative).
     */
    private static double goalAlignment(IntentNature nature, CognitionGoal.Category cat) {
        // Direct mappings: some intents are natural fits for certain goals
        switch (cat) {
            case SEEKING_DAO:
                if (nature == IntentNature.SEEK_OPPORTUNITY) return 2.0;
                if (nature == IntentNature.EXPLORE_CAUTIOUSLY) return 1.5;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 1.0;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 1.0;
                if (nature == IntentNature.CULTIVATE_SECRETLY) return 1.5;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return -1.0;
                if (nature == IntentNature.PROVOKE) return -1.5;
                break;
            case SURVIVE:
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 2.0;
                if (nature == IntentNature.RETREAT_TACTICALLY) return 2.0;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 1.0;
                if (nature == IntentNature.PROVOKE) return -2.0;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return -1.5;
                break;
            case KILL:
                if (nature == IntentNature.AMBUSH) return 2.5;
                if (nature == IntentNature.PROVOKE) return 1.5;
                if (nature == IntentNature.DECEIVE) return 1.0;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 0.5;
                break;
            case DEFEND:
            case DEFEND_TERRITORY:
                if (nature == IntentNature.DEFEND_POSITION) return 2.5;
                if (nature == IntentNature.PROTECT_ASSET) return 2.0;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return 1.5;
                if (nature == IntentNature.RETREAT_TACTICALLY) return -1.0;
                break;
            case INVESTIGATE:
                if (nature == IntentNature.GATHER_INTEL) return 2.5;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 2.0;
                if (nature == IntentNature.EXPLORE_CAUTIOUSLY) return 1.5;
                break;
            case DECEIVE:
                if (nature == IntentNature.DECEIVE) return 2.5;
                if (nature == IntentNature.MAINTAIN_COVER) return 2.0;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 1.5;
                break;
            case MEDITATE:
            case BREAKTHROUGH:
                if (nature == IntentNature.CULTIVATE_SECRETLY) return 2.5;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 1.5;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 0.5;
                break;
            case STUDY:
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 2.0;
                if (nature == IntentNature.GATHER_INTEL) return 1.5;
                if (nature == IntentNature.CULTIVATE_SECRETLY) return 1.0;
                break;
            case TRADE:
                if (nature == IntentNature.NEGOTIATE) return 2.5;
                if (nature == IntentNature.GATHER_INTEL) return 1.0;
                break;
            case FLEE:
                if (nature == IntentNature.RETREAT_TACTICALLY) return 2.5;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 1.5;
                break;
            case EXPLORE:
                if (nature == IntentNature.EXPLORE_CAUTIOUSLY) return 2.5;
                if (nature == IntentNature.SEEK_OPPORTUNITY) return 1.5;
                break;
            case LEGACY:
            case OFFER_FAVOR:
            case SOCIAL:
                if (nature == IntentNature.TEST_JUDGMENT) return 2.0;
                if (nature == IntentNature.NEGOTIATE) return 1.5;
                break;
            default:
                // Neutral — no strong alignment
                break;
        }
        return 0.0;
    }

    /**
     * Dao alignment — does this intent fit the actor's Dao identity?
     * Wang Lin (DEFIANCE) strongly prefers AVOID_REVEALING_STRENGTH.
     */
    private static double daoAlignment(IntentNature nature, DaoIdentity dao) {
        switch (dao) {
            case DEFIANCE:
                // Wang Lin's signature: hide power, observe, advance cautiously
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 3.0;
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 2.0;
                if (nature == IntentNature.EXPLORE_CAUTIOUSLY) return 1.5;
                if (nature == IntentNature.RETREAT_TACTICALLY) return 1.0;
                if (nature == IntentNature.PROVOKE) return -2.0;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return -2.0;
                break;
            case SLAUGHTER:
                if (nature == IntentNature.AMBUSH) return 2.0;
                if (nature == IntentNature.PROVOKE) return 2.0;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return 1.5;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return -1.0;
                break;
            case KARMA:
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 1.5;
                if (nature == IntentNature.NEGOTIATE) return 1.5;
                if (nature == IntentNature.TEST_JUDGMENT) return 1.5;
                break;
            case RESTRICTION:
                if (nature == IntentNature.OBSERVE_FROM_DISTANCE) return 2.0;
                if (nature == IntentNature.GATHER_INTEL) return 1.5;
                if (nature == IntentNature.EXPLORE_CAUTIOUSLY) return 1.0;
                break;
            case ROGUE_CULTIVATOR:
                if (nature == IntentNature.SEEK_OPPORTUNITY) return 2.0;
                if (nature == IntentNature.AVOID_REVEALING_STRENGTH) return 1.5;
                if (nature == IntentNature.RETREAT_TACTICALLY) return 1.5;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return -1.0;
                break;
            case SECT_LEADER:
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return 2.0;
                if (nature == IntentNature.DEFEND_POSITION) return 1.5;
                if (nature == IntentNature.PROTECT_ASSET) return 1.5;
                break;
            case BEAST_KING:
                if (nature == IntentNature.DEFEND_POSITION) return 2.5;
                if (nature == IntentNature.ESTABLISH_DOMINANCE) return 2.0;
                if (nature == IntentNature.AMBUSH) return 1.5;
                break;
            case HONOR:
                if (nature == IntentNature.DEFEND_POSITION) return 1.5;
                if (nature == IntentNature.PROTECT_ASSET) return 2.0;
                if (nature == IntentNature.DECEIVE) return -2.0;
                break;
            default:
                break;
        }
        return 0.0;
    }

    /**
     * Situational modifier — nearby world events influence which intent is best.
     *
     * <p>This is the "perception" part of the cognition pipeline: the NPC
     * queries WorldHistory for recent events near its position and adjusts
     * its intent scores accordingly. Per ENDGAME PROOF (e): "nearby
     * cultivators independently detect via their perception."
     *
     * <p>Examples:
     * <ul>
     *   <li>A QI event nearby → SEEK_OPPORTUNITY gets +2.0 (go investigate)</li>
     *   <li>An ACQUIRE event nearby → SEEK_OPPORTUNITY +1.5 (go grab it)</li>
     *   <li>A KARMA event nearby → AVOID_REVEALING_STRENGTH +1.5 (lay low)</li>
     *   <li>A SOCIAL event nearby → OBSERVE_FROM_DISTANCE +1.0 (watch)</li>
     * </ul>
     */
    private static double situationalModifier(IntentNature nature, int x, int z, long tick) {
        double mod = 0.0;

        // Query WorldHistory for recent events near the actor.
        // Use QI radius (128) as the perception range — cultivators sense qi.
        var history = dev.ergenverse.history.WorldHistory.get(
                dev.ergenverse.simulation.event.WorldEventBus.currentLevel());
        if (history == null) return 0.0;

        // Check within QI perception radius (128 blocks) for recent events.
        java.util.List<dev.ergenverse.history.WorldHistory.WorldEvent> nearby =
                history.findNearby(x, z, 128, 5);
        if (nearby.isEmpty()) return 0.0;

        // Classify nearby events by topic prefix
        boolean hasQiEvent = false;
        boolean hasAcquireEvent = false;
        boolean hasKarmaEvent = false;
        boolean hasSocialEvent = false;
        for (var e : nearby) {
            if (!e.hasTopic()) continue;
            String t = e.topic();
            if (t.contains("qi_fluctuation") || t.contains("cultivator_approaching")) hasQiEvent = true;
            if (t.contains("predator_approaching") || t.contains("conflict")) hasAcquireEvent = true;
            if (t.startsWith("karma.") || t.contains("karma")) hasKarmaEvent = true;
            if (t.startsWith("sect.") || t.startsWith("npc.")) hasSocialEvent = true;
        }

        // Adjust intent scores based on what's happening nearby
        if (hasQiEvent) {
            if (nature == IntentNature.SEEK_OPPORTUNITY) mod += 2.0;
            if (nature == IntentNature.EXPLORE_CAUTIOUSLY) mod += 1.0;
            if (nature == IntentNature.GATHER_INTEL) mod += 0.8;
        }
        if (hasAcquireEvent) {
            if (nature == IntentNature.SEEK_OPPORTUNITY) mod += 1.5;
            if (nature == IntentNature.OBSERVE_FROM_DISTANCE) mod += 1.0;
            if (nature == IntentNature.AMBUSH) mod += 1.5;
        }
        if (hasKarmaEvent) {
            if (nature == IntentNature.AVOID_REVEALING_STRENGTH) mod += 1.5;
            if (nature == IntentNature.OBSERVE_FROM_DISTANCE) mod += 1.0;
            if (nature == IntentNature.RETREAT_TACTICALLY) mod += 1.0;
            if (nature == IntentNature.SEEK_OPPORTUNITY) mod -= 0.5; // dangerous
        }
        if (hasSocialEvent) {
            if (nature == IntentNature.OBSERVE_FROM_DISTANCE) mod += 1.0;
            if (nature == IntentNature.GATHER_INTEL) mod += 0.8;
        }

        return mod;
    }

    /**
     * Personality modifier — cautious NPCs prefer observe/retreat;
     * aggressive NPCs prefer provoke/ambush.
     */
    private static double personalityModifier(IntentNature nature, PersonalityModel personality) {
        if (personality == null) return 0.0;

        // Use "intent_derivation" as the context for personality lookups.
        String ctx = "intent_derivation";
        double caution = personality.get("caution", ctx);
        double courage = personality.get("courage", ctx);
        double ruthlessness = personality.get("ruthlessness", ctx);
        double patience = personality.get("patience", ctx);
        double curiosity = personality.get("curiosity", ctx);
        double ambition = personality.get("ambition", ctx);

        double mod = 0.0;

        // Cautious NPCs prefer hiding, observing, retreating
        if (nature == IntentNature.AVOID_REVEALING_STRENGTH) mod += (caution - 0.5) * 2.0;
        if (nature == IntentNature.OBSERVE_FROM_DISTANCE) mod += (caution - 0.5) * 1.5 + (patience - 0.5) * 1.0;
        if (nature == IntentNature.RETREAT_TACTICALLY) mod += (caution - 0.5) * 1.5;
        if (nature == IntentNature.EXPLORE_CAUTIOUSLY) mod += (caution - 0.5) * 1.0 + (curiosity - 0.5) * 1.5;

        // Courageous NPCs prefer advancing, defending, establishing dominance
        if (nature == IntentNature.ADVANCE_OPPORTUNISTICALLY) mod += (courage - 0.5) * 2.0;
        if (nature == IntentNature.DEFEND_POSITION) mod += (courage - 0.5) * 1.5;
        if (nature == IntentNature.ESTABLISH_DOMINANCE) mod += (courage - 0.5) * 1.5 + (ambition - 0.5) * 1.0;

        // Ruthless NPCs prefer ambush, provoke, deceive
        if (nature == IntentNature.AMBUSH) mod += (ruthlessness - 0.5) * 2.0;
        if (nature == IntentNature.PROVOKE) mod += (ruthlessness - 0.5) * 1.5;
        if (nature == IntentNature.DECEIVE) mod += (ruthlessness - 0.5) * 1.0;

        // Patient NPCs prefer gather_intel, test_judgment, cultivate_secretly
        if (nature == IntentNature.GATHER_INTEL) mod += (patience - 0.5) * 1.5 + (curiosity - 0.5) * 1.0;
        if (nature == IntentNature.TEST_JUDGMENT) mod += (patience - 0.5) * 1.5;
        if (nature == IntentNature.CULTIVATE_SECRETLY) mod += (patience - 0.5) * 1.0;

        // Curious NPCs prefer explore, seek_opportunity, gather_intel
        if (nature == IntentNature.SEEK_OPPORTUNITY) mod += (curiosity - 0.5) * 1.5 + (ambition - 0.5) * 1.0;

        // Ambitious NPCs prefer seek_opportunity, establish_dominance, negotiate
        if (nature == IntentNature.NEGOTIATE) mod += (ambition - 0.5) * 1.0;

        return mod;
    }

    /**
     * Infer the target of the intent based on the goal.
     */
    private static String inferTarget(IntentNature nature, CognitionGoal goal) {
        // For now, the target is embedded in the goal description.
        // A more sophisticated version would parse the goal's target field.
        if (nature == IntentNature.CULTIVATE_SECRETLY
                || nature == IntentNature.AVOID_REVEALING_STRENGTH
                || nature == IntentNature.SEEK_OPPORTUNITY
                || nature == IntentNature.EXPLORE_CAUTIOUSLY) {
            return ""; // self-directed
        }
        return goal.description;
    }

    /**
     * Expected duration for each intent type (in ticks).
     */
    private static long expectedDuration(IntentNature nature, CognitionGoal goal) {
        switch (nature) {
            case OBSERVE_FROM_DISTANCE: return 1200;   // 1 minute
            case GATHER_INTEL: return 2400;            // 2 minutes
            case AVOID_REVEALING_STRENGTH: return 6000; // 5 minutes
            case EXPLORE_CAUTIOUSLY: return 6000;      // 5 minutes
            case CULTIVATE_SECRETLY: return 24000;     // 1 MC day
            case SEEK_OPPORTUNITY: return 12000;       // 10 minutes
            case MAINTAIN_COVER: return 24000;         // 1 MC day
            case AMBUSH: return 3600;                  // 3 minutes
            case NEGOTIATE: return 2400;               // 2 minutes
            case DECEIVE: return 3600;                 // 3 minutes
            case RETREAT_TACTICALLY: return 1200;      // 1 minute
            case ADVANCE_OPPORTUNISTICALLY: return 2400; // 2 minutes
            case TEST_JUDGMENT: return 12000;          // 10 minutes
            case PROVOKE: return 600;                  // 30 seconds
            case DEFEND_POSITION: return 6000;         // 5 minutes
            case PROTECT_ASSET: return 6000;           // 5 minutes
            case ESTABLISH_DOMINANCE: return 2400;     // 2 minutes
            default: return 2400;
        }
    }
}
