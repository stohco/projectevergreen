package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.List;

/**
 * ActivityProcess — an activity as a PROCESS, not a state.
 *
 * <p>Per Article XLI §2. This replaces the CultivatorActivity enum
 * (WANDERING / MEDITATING / BREAKING_THROUGH) with a lifecycle model
 * where interruption is a natural property of every activity.
 *
 * <p>The lifecycle is:
 *   Start → In Progress → Interrupted → Reacting → Resuming → Complete
 *
 * <p>Interruption is not a special feature added on top. An activity
 * DECLARES its own interruption conditions — which events can pause it,
 * and what the actor does when interrupted. This is per-activity,
 * per-actor, driven by personality traits.
 *
 * <p><b>Design principle (XLI §2):</b> activity isn't a state.
 * It is a process. This class tracks where in the process the activity is.
 *
 * <p><b>Relationship to existing code:</b> This class is NOT a new Engine.
 * It is a data object that the existing ActorTickLoop and
 * WorldEventBus subscribers reference. The CognitionDrivenGoal still
 * drives physical entity behavior via Intent → CultivationTask. This
 * class provides the ACTIVITY LAYER that the interruption subscriber
 * modifies, which causes the DecisionEngine to produce different
 * intents (via modified cognition or injected interruption context).
 *
 * <p><b>Provenance: INFERRED.</b> The process model is a standard
 * simulation pattern. The specific interruption conditions are derived
 * from the advisor's canon examples: Wang Lin studying a restriction
 * (ignores player proximity for 30 seconds), Li Muwan alchemy
 * (pauses at safe point before answering), Xu Liguo (self-interrupts
 * due to talkative personality).
 */
public final class ActivityProcess {

    // ── Lifecycle states ──────────────────────────────────────

    /** The activity has not started yet. */
    public static final int STATE_NOT_STARTED = 0;

    /** The activity is beginning. */
    public static final int STATE_STARTING = 1;

    /** The activity is actively running. */
    public static final int STATE_IN_PROGRESS = 2;

    /** Something paused this activity. The actor is reacting. */
    public static final int STATE_INTERRUPTED = 3;

    /** The actor is responding to whatever interrupted them. */
    public static final int STATE_REACTING = 4;

    /** The interruption is over; the activity is resuming. */
    public static final int STATE_RESUMING = 5;

    /** The activity finished naturally. */
    public static final int STATE_COMPLETED = 6;

    /** The activity was cancelled (higher priority took over). */
    public static final int STATE_ABANDONED = 7;

    /** Human-readable state names for diagnostics. */
    public static final String[] STATE_NAMES = {
        "NOT_STARTED", "STARTING", "IN_PROGRESS", "INTERRUPTED",
        "REACTING", "RESUMING", "COMPLETED", "ABANDONED"
    };

    // ── Activity identification ────────────────────────────────────

    /** What kind of activity (e.g. "meditation", "alchemy", "travel", "cultivation_breakthrough"). */
    public final String activityType;

    /** Current lifecycle state (one of STATE_* constants). */
    public int state = STATE_NOT_STARTED;

    /** Server tick when the activity started. */
    public long startedTick = 0;

    /** Progress through the activity (0.0 = just started, 1.0 = complete). */
    public float progress = 0.0f;

    /** Can this activity be interrupted? Default true. Breakthrough = false. */
    public boolean interruptible = true;

    // ── Interruption conditions ──────────────────────────────────

    /**
     * An interruption condition specifies WHAT can interrupt this activity.
     * When a WorldEventBus event matches, the condition is evaluated
     * against the actor's personality traits.
     */
    public static final class InterruptionCondition {
        /** Event topic prefix to match (e.g. "beast.wolf_pack." matches all wolf events). */
        public final String eventTopicPrefix;

        /**
         * Which personality trait to check (e.g. "caution").
         * The trait value (0.0-1.0, from the actor's PersonalityModel)
         * is compared against a threshold.
         */
        public final String personalityTrait;

        /**
         * Minimum personality value required to trigger interruption.
         * E.g. threshold 0.8 means only very cautious actors react.
         */
        public float personalityThreshold;

        /**
         * What the actor does when interrupted. Determines how
         * CognitionDrivenGoal / DecisionEngine responds.
         * Values: INVESTIGATE, OBSERVE, FLEE, APPROACH, IGNORE.
         */
        public final String reactionType;

        /**
         * How long the reaction lasts (in ticks). 0 = until condition
         * resolves. The actor resumes after this duration.
         */
        public int reactionDurationTicks;

        /**
         * Whether to resume the original activity after the reaction.
         * false = activity is ABANDONED permanently.
         */
        public boolean resumeAfter;

        public InterruptionCondition(String eventTopicPrefix, String personalityTrait,
                                  float personalityThreshold, String reactionType,
                                  int reactionDurationTicks, boolean resumeAfter) {
            this.eventTopicPrefix = eventTopicPrefix;
            this.personalityTrait = personalityTrait;
            this.personalityThreshold = personalityThreshold;
            this.reactionType = reactionType;
            this.reactionDurationTicks = reactionDurationTicks;
            this.resumeAfter = resumeAfter;
        }
    }

    /** The interruption conditions for this activity. Populated at assignment time. */
    public final List<InterruptionCondition> interruptionConditions = new ArrayList<>();

    // ── Current interruption (populated when state transitions to INTERRUPTED) ─

    /** The WorldEventBus event topic that caused the interruption. */
    public String interruptionEventTopic;

    /** Server tick when the interruption occurred. */
    public long interruptionTick;

    /** What the actor is reacting to (INVESTIGATE, OBSERVE, FLEE, etc.). */
    public String reactionType = "OBSERVE";

    /** Target of the reaction (entity UUID or position). */
    public String reactionTarget;

    /** Position of the reaction target (for OBSERVE/INVESTIGATE). */
    public int reactionTargetX, reactionTargetY, reactionTargetZ;

    /** Ticks remaining in the current reaction. */
    public int reactionTicksRemaining;

    // ── Resumption state ───────────────────────────────────────

    /** Progress at the moment of interruption (for resumption). */
    public float progressAtInterruption = 0.0f;

    /** Activity type at the moment of interruption (for resumption). */
    public String activityTypeAtInterruption;

    /** Custom context the actor needs to resume (position, conversation partner, etc.). */
    public String resumptionContext = "";

    /** Tick when the reaction completes and resumption should begin. */
    public long resumeTick = 0;

    // ── Construction ───────────────────────────────────────────────

    public ActivityProcess(String activityType) {
        this.activityType = activityType;
    }

    // ── Lifecycle transitions ───────────────────────────────────

    /** Begin the activity. */
    public void start(long tick) {
        this.state = STATE_STARTING;
        this.startedTick = tick;
        this.progress = 0.0f;
        this.state = STATE_IN_PROGRESS;
    }

    /** Mark this activity as interrupted by an event. */
    public void interrupt(String eventTopic, long tick, String reactionType,
                        String target, int targetX, int targetY, int targetZ,
                        int durationTicks, boolean resumeAfter) {
        this.state = STATE_INTERRUPTED;
        this.interruptionEventTopic = eventTopic;
        this.interruptionTick = tick;
        this.reactionType = reactionType;
        this.reactionTarget = target;
        this.reactionTargetX = targetX;
        this.reactionTargetY = targetY;
        this.reactionTargetZ = targetZ;
        this.reactionTicksRemaining = durationTicks;
        this.progressAtInterruption = this.progress;
        this.activityTypeAtInterruption = this.activityType;
        this.resumptionContext = ""; // TODO: save context
    }

    /** Begin reacting to the interruption. */
    public void beginReaction(long tick) {
        this.state = STATE_REACTING;
    }

    /** Mark the interruption as over and begin resuming. */
    public void beginResume(long tick) {
        this.state = STATE_RESUMING;
        this.resumeTick = tick;
    }

    /** Mark the activity as completed naturally. */
    public void complete() {
        this.state = STATE_COMPLETED;
    }

    /** Mark the activity as abandoned (permanently cancelled). */
    public void abandon() {
        this.state = STATE_ABANDONED;
    }

    /** Reset the activity for reuse. */
    public void reset() {
        this.state = STATE_NOT_STARTED;
        this.startedTick = 0;
        this.progress = 0.0f;
        this.interruptionEventTopic = null;
        this.interruptionTick = 0;
        this.reactionType = "OBSERVE";
        this.reactionTarget = null;
        this.reactionTicksRemaining = 0;
        this.progressAtInterruption = 0.0f;
        this.activityTypeAtInterruption = "";
        this.resumptionContext = "";
        this.resumeTick = 0;
    }

    // ── State queries ──────────────────────────────────────────

    public boolean isInterruptible() { return interruptible; }

    public boolean isActive() {
        return state == STATE_IN_PROGRESS;
    }

    public boolean isInterrupted() {
        return state == STATE_INTERRUPTED || state == STATE_REACTING;
    }

    public boolean isReacting() { return state == STATE_REACTING; }

    public boolean isResuming() { return state == STATE_RESUMING; }

    public boolean isComplete() { return state == STATE_COMPLETED; }

    public boolean isAbandoned() { return state == STATE_ABANDONED; }

    public boolean hasStarted() { return state != STATE_NOT_STARTED; }

    /**
     * Check if any interruption condition matches the given event topic.
     * Returns the matching condition, or null if no match.
     */
    public InterruptionCondition findMatchingCondition(String eventTopic) {
        for (InterruptionCondition cond : interruptionConditions) {
            if (eventTopic.startsWith(cond.eventTopicPrefix)) {
                return cond;
            }
        }
        return null;
    }

    /**
     * Create an interruption condition that matches events with the given
     * topic prefix, evaluated against a personality trait.
     */
    public static InterruptionCondition personalityCondition(
            String eventTopicPrefix, String personalityTrait,
            float threshold, String reactionType,
            int durationTicks, boolean resumeAfter) {
        return new InterruptionCondition(
                eventTopicPrefix, personalityTrait, threshold, reactionType,
                durationTicks, resumeAfter);
    }

    @Override
    public String toString() {
        String stateName = (state >= 0 && state < STATE_NAMES.length)
                ? STATE_NAMES[state] : "UNKNOWN(" + state;
        return "ActivityProcess[" + activityType + "] state=" + stateName;
    }
}