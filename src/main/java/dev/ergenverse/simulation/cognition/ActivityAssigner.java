package dev.ergenverse.simulation.cognition;

import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.core.Ergenverse;

/**
 * ActivityAssigner — maps cognition decisions to ActivityProcess objects.
 *
 * <p>Per Article XLI: activity is a process, not a state. When the
 * DecisionEngine produces a decision with a goal, this class
 * creates the appropriate ActivityProcess with the correct
 * interruption conditions and attaches it to the actor.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a static utility
 * method called from {@link dev.ergenverse.simulation.actor.ActorTickLoop}.
 * It creates data objects (ActivityProcess) that existing systems
 * (ActorTickLoop, ActivityInterruptionSubscriber) already handle.
 * No new bus, no new subscriber, no new infrastructure.
 *
 * <p><b>How CE #1 flows through this:</b>
 * <ol>
 *   <li>DecisionEngine decides goal = MEDITATE for a cautious actor.</li>
 *   <li>ActivityAssigner.assign() creates ActivityProcess("meditation")
 *       with interruption condition: beast. prefix, caution threshold 0.7,
 *       reaction=OBSERVE, duration=200 ticks, resumeAfter=true.</li>
 *   <li>ActorTickLoop.tickActivity() advances the meditation progress.</li>
 *   <li>A beast event fires → ActivityInterruptionSubscriber interrupts
 *       the meditation → actor reacts → resumes.</li>
 *   <li>Player witnesses a cultivator stand up, look toward the hills,
 *       watch silently, then sit back down. Canon Experience #1.</li>
 * </ol>
 *
 * <p><b>Provenance: INFERRED.</b> The goal→activity mapping and
 * interruption conditions are derived from Wang Lin's early behavior
 * in Renegade Immortal chapters 1-5.
 */
public final class ActivityAssigner {

    private ActivityAssigner() {}

    /**
     * Map a CognitionGoal category to a process-friendly activity type string.
     * Returns null if the category doesn't map to a trackable activity.
     */
    public static String mapCategoryToActivity(CognitionGoal.Category category) {
        if (category == null) return null;
        switch (category) {
            case MEDITATE:
                return "meditation";
            case BREAKTHROUGH:
                return "cultivation_breakthrough";
            case STUDY:
                return "study_restriction";
            case CRAFT:
                return "craft_artifact";
            case INVESTIGATE:
                return "investigate";
            case EXPLORE:
                return "exploration";
            case WAIT:
                return "waiting";
            case GATHER_RESOURCE:
                return "gathering";
            default:
                // SURVIVE, DEFEND, FLEE, HIDE, KILL, POLITICS, TRADE, etc.
                // These are handled by the entity AI directly, not by
                // the ActivityProcess system. They produce immediate
                // movement/combat goals, not long-running processes.
                return null;
        }
    }

    /**
     * Create an ActivityProcess for a given goal and actor, with
     * appropriate interruption conditions.
     *
     * <p>If the goal category doesn't map to a trackable activity
     * (e.g. FLEE, KILL), the actor's current activity is cleared
     * and null is returned. The entity AI handles it instead.
     *
     * @param actor  the actor to assign the activity to
     * @param goal   the cognition goal from the DecisionEngine
     * @param tick   the current server tick
     * @return the created ActivityProcess, or null if not applicable
     */
    public static ActivityProcess assign(Actor actor, CognitionGoal goal, long tick) {
        String activityType = mapCategoryToActivity(goal.category);
        if (activityType == null) {
            // Not a trackable activity — clear any existing one.
            actor.currentActivity = null;
            return null;
        }

        ActivityProcess ap = new ActivityProcess(activityType);

        // Add interruption conditions based on activity type.
        // These are the conditions that make a world event interrupt
        // this specific activity.
        addInterruptionConditions(ap, actor);

        // Start the activity.
        ap.start(tick);

        // Attach to actor.
        actor.currentActivity = ap;

        Ergenverse.LOGGER.debug("[Ergenverse] ActivityAssigner: assigned {} to {} (goal: {})",
                activityType, actor.id, goal);

        return ap;
    }

    /**
     * Add interruption conditions to an activity based on its type
     * and the actor's personality.
     *
     * <p>Conditions are personality-gated: a cautious actor is
     * interrupted by beast events at a lower threshold than
     * a reckless one. A patient actor ignores short disturbances.
     *
     * <p><b>INFERRED from canon:</b> Wang Lin meditates in the cave
     * and ignores the player approaching, but reacts to qi
     * disturbances and beast sounds. His caution is high (0.8+)
     * but his patience is also high (0.7+), so he only interrupts
     * for genuinely threatening events, not minor ones.
     */
    private static void addInterruptionConditions(ActivityProcess ap, Actor actor) {
        float caution = getTraitOrDefault(actor, "caution", 0.5f);
        float curiosity = getTraitOrDefault(actor, "curiosity", 0.3f);

        switch (ap.activityType) {
            case "meditation":
                // Beast events interrupt cautious cultivators.
                // A cautious cultivator (caution > 0.5) stops meditating
                // to observe predators.
                if (caution > 0.3f) {
                    ap.interruptionConditions.add(
                            ActivityProcess.personalityCondition(
                                    "beast.",
                                    "caution",
                                    0.3f + caution * 0.4f, // threshold 0.42–0.70
                                    "OBSERVE",
                                    200, // ~10 seconds of observation
                                    true  // resume after
                            ));
                }
                // Qi disturbances interrupt curious cultivators.
                if (curiosity > 0.4f) {
                    ap.interruptionConditions.add(
                            ActivityProcess.personalityCondition(
                                    "spirit_vein.",
                                    "curiosity",
                                    0.5f,
                                    "INVESTIGATE",
                                    100,
                                    true
                            ));
                }
                break;

            case "study_restriction":
                // Restriction study is VERY interruptible — even low-severity
                // events can distract a studying cultivator.
                ap.interruptionConditions.add(
                        ActivityProcess.personalityCondition(
                                "beast.",
                                "caution",
                                0.1f + caution * 0.3f,
                                "INVESTIGATE",
                                150,
                                true
                        ));
                ap.interruptionConditions.add(
                        ActivityProcess.personalityCondition(
                                "npc.",
                                "curiosity",
                                0.6f,
                                "OBSERVE",
                                100,
                                true
                        ));
                break;

            case "cultivation_breakthrough":
                // Breakthrough is NOT interruptible (per canon — Wang Lin
                // is vulnerable during breakthrough).
                ap.interruptible = false;
                break;

            case "craft_artifact":
                // Crafting is interruptible by dangerous events.
                ap.interruptionConditions.add(
                        ActivityProcess.personalityCondition(
                                "beast.",
                                "caution",
                                0.5f + caution * 0.3f,
                                "FLEE",
                                60,
                                false  // abandon crafting
                        ));
                break;

            case "investigate":
                // Investigation is interruptible by more urgent threats.
                ap.interruptionConditions.add(
                        ActivityProcess.personalityCondition(
                                "beast.",
                                "caution",
                                0.6f + caution * 0.3f,
                                "FLEE",
                                40,
                                true
                        ));
                break;

            default:
                // Generic: cautious actors notice beast events.
                ap.interruptionConditions.add(
                        ActivityProcess.personalityCondition(
                                "beast.",
                                "caution",
                                0.4f,
                                "OBSERVE",
                                100,
                                true
                        ));
                break;
        }
    }

    /**
     * Get a personality trait value for an actor, with a safe default.
     */
    private static float getTraitOrDefault(Actor actor, String trait, float defaultVal) {
        if (actor.cognition != null && actor.cognition.personality != null) {
            double val = actor.cognition.personality.get(trait, "default");
            return (float) val;
        }
        return defaultVal;
    }
}