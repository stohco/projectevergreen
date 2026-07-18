package dev.ergenverse.npc.cognition;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.npc.goals.NpcGoalQueue.GoalSource;
import dev.ergenverse.npc.goals.NpcGoalQueue.GoalType;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * NpcRealmCognition — realm-scaled cognitive parameters (PROJECT_MASTER.md 6.6).
 *
 * <p>"Cultivators think differently by realm. No xianxia game has captured this."
 *
 * <p>Each cultivation realm maps to a distinct cognitive profile that affects:
 * <ul>
 *   <li><b>Planning horizon</b> — how far into the future the NPC thinks.
 *       Mortals plan for today; Third Step plans for 100,000+ years.</li>
 *   <li><b>Concern focus</b> — what category of need dominates their thinking.
 *       Mortals need food; Nascent Soul needs allies.</li>
 *   <li><b>Goal relevance</b> — which goal types are considered meaningful.
 *       A mortal will never have a SCHEME goal; a Soul Formation cultivator
 *       will rarely have a REST goal.</li>
 *   <li><b>Re-prioritization speed</b> — how quickly the NPC shifts goals.
 *       Higher realms resist reactive shifts (they've seen centuries pass).</li>
 *   <li><b>Max simultaneous goals</b> — cognitive bandwidth.
 *       Mortals track 3-4 goals; ancient cultivators track 10+.</li>
 *   <li><b>Goal deadline multiplier</b> — how long the NPC is willing to
 *       pursue a goal before giving up. Mortals give up in hours;
 *       immortals wait decades.</li>
 *   <li><b>Trusted information sources</b> — what sources of knowledge
 *       the NPC considers reliable. Mortals trust what they see;
 *       Soul Formation cultivators trust divination and karma.</li>
 * </ul>
 *
 * <h2>Integration points</h2>
 * <ul>
 *   <li><b>Priority Queue (B.1)</b>: {@link #getMaxGoals(RealmId)} limits
 *       goal stack size. {@link #getGoalDeadlineMultiplier(RealmId)} scales
 *       goal expiry. {@link #getReprIoWeight(RealmId)} modulates memory
 *       pressure on re-prioritization.</li>
 *   <li><b>Internal Monologue (B.5)</b>: {@link #getConcernFocus(RealmId)}
 *       and {@link #getPlanningHorizon(RealmId)} populate the "Problem"
 *       and "Current Objective" fields.</li>
 *   <li><b>Expectation Model (B.6)</b>: Higher realms have wider prediction
 *       horizons — a Nascent Soul elder predicts decades ahead.</li>
 *   <li><b>Dialogue Generation (B.7)</b>: Realm affects vocabulary, patience,
 *       and what topics the NPC considers worth discussing.</li>
 *   <li><b>Rumor System (B.3)</b>: Higher realms are more skeptical of
 *       rumors (they've seen centuries of misinformation).</li>
 * </ul>
 *
 * <h2>Performance (6.12)</h2>
 * <p>This is a stateless utility class — no persistence, no per-NPC data.
 * All profiles are pre-computed static constants. Zero allocation on query.
 *
 * @see RealmId  the 18-stage cultivation realm enum
 */
public final class NpcRealmCognition {

    private NpcRealmCognition() {}

    // ================================================================
    //  Realm Cognitive Profile
    // ================================================================

    /**
     * The cognitive profile for a cultivation realm. Immutable value object.
     * Pre-computed for all 18 realms in {@link #PROFILES}.
     */
    public static final class CognitionProfile {
        /** The realm this profile belongs to. */
        public final RealmId realm;

        /**
         * Planning horizon in in-game days. How far into the future
         * the NPC thinks when setting goals.
         * Mortal=1 day, Third Step=100,000+ years.
         */
        public final long planningHorizonDays;

        /**
         * The dominant concern that colors all of the NPC's thinking.
         * Used by Internal Monologue (B.5) to generate the "Problem" field.
         */
        public final String concernFocus;

        /**
         * Which goal types are considered meaningful at this realm.
         * Goals outside this set get a priority penalty.
         */
        public final Set<GoalType> relevantGoalTypes;

        /**
         * Goal types that are strongly amplified at this realm.
         * These get a priority bonus when pushed.
         */
        public final Set<GoalType> amplifiedGoalTypes;

        /**
         * Goal types that are suppressed at this realm.
         * These get a priority penalty when pushed.
         */
        public final Set<GoalType> suppressedGoalTypes;

        /**
         * Maximum number of simultaneous non-terminal goals the NPC
         * can track. Cognitive bandwidth scales with realm.
         */
        public final int maxGoals;

        /**
         * Multiplier applied to goal deadlines. A mortal with a
         * 3-day default deadline and 0.3x multiplier effectively
         * gives up after ~0.9 days. An ancient cultivator with
         * 50x multiplier waits 150 days.
         */
        public final double goalDeadlineMultiplier;

        /**
         * How much memory pressure affects re-prioritization.
         * Higher realms are more resistant to reactive shifts.
         * 1.0 = normal, 0.2 = very resistant.
         */
        public final double rePrioWeight;

        /**
         * Rumor skepticism factor (0.0-1.0). How likely the NPC is
         * to believe a rumor. Mortals believe everything (0.9);
         * ancient cultivators are highly skeptical (0.2).
         */
        public final double rumorBelievability;

        /**
         * Whether the NPC considers karma, fate, and cosmic forces
         * in their decision-making. False for Mortal/Qi Condensation.
         */
        public final boolean considersKarma;

        /**
         * Whether the NPC considers faction politics in their
         * decision-making. False for mortals.
         */
        public final boolean considersFactionPolitics;

        /**
         * Typical concern examples for internal monologue generation.
         * Format strings that can be filled with context.
         */
        public final String[] concernExamples;

        CognitionProfile(RealmId realm, long planningHorizonDays,
                         String concernFocus,
                         Set<GoalType> relevantGoalTypes,
                         Set<GoalType> amplifiedGoalTypes,
                         Set<GoalType> suppressedGoalTypes,
                         int maxGoals, double goalDeadlineMultiplier,
                         double rePrioWeight, double rumorBelievability,
                         boolean considersKarma, boolean considersFactionPolitics,
                         String[] concernExamples) {
            this.realm = realm;
            this.planningHorizonDays = planningHorizonDays;
            this.concernFocus = concernFocus;
            this.relevantGoalTypes = relevantGoalTypes;
            this.amplifiedGoalTypes = amplifiedGoalTypes;
            this.suppressedGoalTypes = suppressedGoalTypes;
            this.maxGoals = maxGoals;
            this.goalDeadlineMultiplier = goalDeadlineMultiplier;
            this.rePrioWeight = rePrioWeight;
            this.rumorBelievability = rumorBelievability;
            this.considersKarma = considersKarma;
            this.considersFactionPolitics = considersFactionPolitics;
            this.concernExamples = concernExamples;
        }
    }

    // ================================================================
    //  Static profile table — one per RealmId
    // ================================================================

    private static final Map<RealmId, CognitionProfile> PROFILES = new EnumMap<>(RealmId.class);

    static {
        // --- First Step ---

        PROFILES.put(RealmId.MORTAL, new CognitionProfile(
                RealmId.MORTAL,
                1,                          // horizon: today
                "Survival and daily needs",
                Set.of(GoalType.REST, GoalType.GATHER, GoalType.INTERACT,
                        GoalType.FLEE, GoalType.CULTIVATE, GoalType.TRAVEL),
                Set.of(GoalType.REST, GoalType.GATHER),
                Set.of(GoalType.SCHEME, GoalType.MANAGE, GoalType.PATROL),
                4,                          // max goals
                0.3,                        // deadline multiplier
                1.5,                        // reactive (mortals are impulsive)
                0.9,                        // believe most rumors
                false, false,
                new String[]{"I need food.", "I need shelter.", "I need to survive."}
        ));

        PROFILES.put(RealmId.QI_CONDENSATION, new CognitionProfile(
                RealmId.QI_CONDENSATION,
                30,                         // horizon: ~1 month
                "Gathering cultivation resources",
                Set.of(GoalType.CULTIVATE, GoalType.GATHER, GoalType.INTERACT,
                        GoalType.TRAVEL, GoalType.REST, GoalType.INVESTIGATE),
                Set.of(GoalType.CULTIVATE, GoalType.GATHER),
                Set.of(GoalType.SCHEME, GoalType.MANAGE),
                5,
                0.5,
                1.2,
                0.8,
                false, false,
                new String[]{"I need pills.", "I need spirit stones.",
                        "My breakthrough is approaching."}
        ));

        PROFILES.put(RealmId.FOUNDATION, new CognitionProfile(
                RealmId.FOUNDATION,
                365,                        // horizon: ~1 year
                "Securing territory and resources",
                Set.of(GoalType.CULTIVATE, GoalType.GATHER, GoalType.DEFEND,
                        GoalType.PATROL, GoalType.TRAVEL, GoalType.INVESTIGATE,
                        GoalType.INTERACT, GoalType.CRAFT),
                Set.of(GoalType.CULTIVATE, GoalType.DEFEND, GoalType.GATHER),
                Set.of(GoalType.FLEE),
                6,
                1.0,
                1.0,
                0.7,
                false, true,
                new String[]{"I need territory.", "I need a stable cultivation spot.",
                        "I need to secure my resources."}
        ));

        PROFILES.put(RealmId.CORE_FORMATION, new CognitionProfile(
                RealmId.CORE_FORMATION,
                3650,                       // horizon: ~10 years
                "Long-term resource acquisition",
                Set.of(GoalType.CULTIVATE, GoalType.GATHER, GoalType.DEFEND,
                        GoalType.CRAFT, GoalType.INVESTIGATE, GoalType.TRAVEL,
                        GoalType.PATROL, GoalType.MANAGE, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.GATHER, GoalType.CRAFT),
                Set.of(GoalType.FLEE, GoalType.REST),
                7,
                2.0,
                0.8,
                0.6,
                false, true,
                new String[]{"I need resources for Core Formation.",
                        "I need rare materials.", "I must prepare for Nascent Soul."}
        ));

        PROFILES.put(RealmId.NASCENT_SOUL, new CognitionProfile(
                RealmId.NASCENT_SOUL,
                36500,                      // horizon: ~100 years
                "Building alliances and legacy",
                Set.of(GoalType.CULTIVATE, GoalType.MANAGE, GoalType.DEFEND,
                        GoalType.INVESTIGATE, GoalType.TEACH, GoalType.SCHEME,
                        GoalType.CRAFT, GoalType.INTERACT, GoalType.TRAVEL,
                        GoalType.GATHER),
                Set.of(GoalType.MANAGE, GoalType.TEACH, GoalType.SCHEME,
                        GoalType.CULTIVATE),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                8,
                5.0,
                0.6,
                0.5,
                true, true,
                new String[]{"I need allies.", "I need to secure my sect.",
                        "I need a successor.", "My Nascent Soul needs nourishment."}
        ));

        PROFILES.put(RealmId.SOUL_FORMATION, new CognitionProfile(
                RealmId.SOUL_FORMATION,
                365000,                     // horizon: ~1,000 years
                "Karmic opportunities and cosmic positioning",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.INVESTIGATE,
                        GoalType.MANAGE, GoalType.TEACH, GoalType.DEFEND,
                        GoalType.RITUAL, GoalType.CRAFT, GoalType.INTERACT),
                Set.of(GoalType.SCHEME, GoalType.INVESTIGATE, GoalType.CULTIVATE,
                        GoalType.RITUAL),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                9,
                10.0,
                0.4,
                0.4,
                true, true,
                new String[]{"I need karmic opportunities.",
                        "The heavens are watching.", "I must understand the Dao.",
                        "A tribulation approaches."}
        ));

        PROFILES.put(RealmId.SOUL_TRANSFORMATION, new CognitionProfile(
                RealmId.SOUL_TRANSFORMATION,
                365000,                     // horizon: ~1,000 years
                "Refining the soul and understanding space",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.INVESTIGATE,
                        GoalType.RITUAL, GoalType.CRAFT, GoalType.MANAGE,
                        GoalType.TEACH, GoalType.DEFEND, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                9,
                12.0,
                0.35,
                0.35,
                true, true,
                new String[]{"Space is my canvas.", "I must refine my soul.",
                        "The boundary between illusion and reality thins."}
        ));

        PROFILES.put(RealmId.ASCENDANT, new CognitionProfile(
                RealmId.ASCENDANT,
                3650000,                    // horizon: ~10,000 years
                "Preserving lineage and ascending",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.MANAGE,
                        GoalType.TEACH, GoalType.DEFEND, GoalType.INVESTIGATE,
                        GoalType.RITUAL, GoalType.CRAFT, GoalType.INTERACT,
                        GoalType.PATROL),
                Set.of(GoalType.SCHEME, GoalType.MANAGE, GoalType.CULTIVATE,
                        GoalType.TEACH),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK),
                10,
                20.0,
                0.3,
                0.3,
                true, true,
                new String[]{"I need to preserve this lineage.",
                        "Ascension is within reach.", "My disciples must be ready."}
        ));

        // --- Transitional Step ---

        PROFILES.put(RealmId.ILLUSORY_YIN, new CognitionProfile(
                RealmId.ILLUSORY_YIN,
                3650000,                    // horizon: ~10,000 years
                "Navigating the Heavenly Tribulation",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.MANAGE, GoalType.TEACH, GoalType.INVESTIGATE,
                        GoalType.DEFEND, GoalType.CRAFT, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                10,
                25.0,
                0.25,
                0.25,
                true, true,
                new String[]{"The Nascent Soul transforms.",
                        "Heaven will test me.", "I must prepare my transformation."}
        ));

        PROFILES.put(RealmId.CORPOREAL_YANG, new CognitionProfile(
                RealmId.CORPOREAL_YANG,
                36500000,                   // horizon: ~100,000 years
                "Mastery of the physical and spiritual",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.MANAGE, GoalType.INVESTIGATE, GoalType.DEFEND,
                        GoalType.CRAFT, GoalType.TEACH, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.CRAFT),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                11,
                30.0,
                0.2,
                0.2,
                true, true,
                new String[]{"Body and soul converge.",
                        "The Second Step beckons.", "I see the threads of fate."}
        ));

        // --- Second Step (Nirvana) ---

        PROFILES.put(RealmId.NIRVANA_SCRYER, new CognitionProfile(
                RealmId.NIRVANA_SCRYER,
                365000000,                  // horizon: ~1,000,000 years
                "Peering into the Heavenly Dao",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.TEACH,
                        GoalType.DEFEND, GoalType.CRAFT, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.INVESTIGATE, GoalType.SCHEME),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK),
                11,
                50.0,
                0.15,
                0.15,
                true, true,
                new String[]{"I can glimpse the Dao.",
                        "Nirvana is a process, not a state.",
                        "What lies beyond the Second Step?"}
        ));

        PROFILES.put(RealmId.NIRVANA_CLEANSER, new CognitionProfile(
                RealmId.NIRVANA_CLEANSER,
                365000000,
                "Purifying karma and cleansing the soul",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.DEFEND,
                        GoalType.CRAFT, GoalType.TEACH, GoalType.INTERACT),
                Set.of(GoalType.CULTIVATE, GoalType.RITUAL, GoalType.SCHEME),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER),
                11,
                60.0,
                0.12,
                0.12,
                true, true,
                new String[]{"Karma must be cleansed.",
                        "Every action echoes through eternity.",
                        "I approach the Nirvana Fruit."}
        ));

        PROFILES.put(RealmId.NIRVANA_FRUIT, new CognitionProfile(
                RealmId.NIRVANA_FRUIT,
                3650000000L,                // horizon: ~10,000,000 years
                "Achieving Nirvana and seizing the Dao",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.DEFEND,
                        GoalType.CRAFT, GoalType.TEACH),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.INTERACT),
                12,
                80.0,
                0.1,
                0.1,
                true, true,
                new String[]{"The Nirvana Fruit ripens.",
                        "I am close to the Third Step.",
                        "Immortality awaits."}
        ));

        PROFILES.put(RealmId.SPIRIT_SEIZER, new CognitionProfile(
                RealmId.SPIRIT_SEIZER,
                3650000000L,
                "Seizing the Dao and preparing for immortality",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.DEFEND,
                        GoalType.CRAFT),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.INTERACT, GoalType.ATTACK),
                12,
                100.0,
                0.08,
                0.08,
                true, true,
                new String[]{"I will seize the Dao.",
                        "The Spirit Seizer's path is lonely.",
                        "Immortality is the only goal."}
        ));

        // --- Immortal+ Step ---

        PROFILES.put(RealmId.TRUE_IMMORTAL, new CognitionProfile(
                RealmId.TRUE_IMMORTAL,
                Integer.MAX_VALUE,           // horizon: effectively infinite
                "Establishing immortal legacy and dao comprehension",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.MANAGE,
                        GoalType.INVESTIGATE, GoalType.TEACH, GoalType.RITUAL,
                        GoalType.CRAFT, GoalType.DEFEND),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.MANAGE),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK, GoalType.INTERACT),
                13,
                200.0,
                0.05,
                0.05,
                true, true,
                new String[]{"My legacy must endure.",
                        "The Dao is infinite.",
                        "Mortals are mayflies."}
        ));

        PROFILES.put(RealmId.ANCIENT, new CognitionProfile(
                RealmId.ANCIENT,
                Integer.MAX_VALUE,
                "Preserving civilization and ancient knowledge",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.MANAGE,
                        GoalType.INVESTIGATE, GoalType.TEACH, GoalType.RITUAL,
                        GoalType.CRAFT, GoalType.DEFEND),
                Set.of(GoalType.SCHEME, GoalType.MANAGE, GoalType.TEACH),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK, GoalType.INTERACT),
                13,
                500.0,
                0.03,
                0.03,
                true, true,
                new String[]{"I have seen civilizations rise and fall.",
                        "In 80,000 years this becomes important.",
                        "Preserve the knowledge."}
        ));

        PROFILES.put(RealmId.PARAGON, new CognitionProfile(
                RealmId.PARAGON,
                Integer.MAX_VALUE,
                "Approaching the peak of the known Dao",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.TEACH,
                        GoalType.DEFEND),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK, GoalType.INTERACT, GoalType.CRAFT),
                14,
                1000.0,
                0.02,
                0.02,
                true, true,
                new String[]{"I am close to the peak.",
                        "The Heavenly Dao resists.",
                        "Allheaven watches."}
        ));

        PROFILES.put(RealmId.TRANSCENDENCE, new CognitionProfile(
                RealmId.TRANSCENDENCE,
                Integer.MAX_VALUE,
                "Transcending all limitations",
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME, GoalType.RITUAL,
                        GoalType.INVESTIGATE, GoalType.MANAGE, GoalType.TEACH),
                Set.of(GoalType.CULTIVATE, GoalType.SCHEME),
                Set.of(GoalType.FLEE, GoalType.REST, GoalType.GATHER,
                        GoalType.ATTACK, GoalType.INTERACT, GoalType.CRAFT,
                        GoalType.DEFEND, GoalType.PATROL, GoalType.MANAGE),
                15,
                Double.MAX_VALUE,
                0.01,
                0.01,
                true, true,
                new String[]{"I have surpassed the Heavenly Dao.",
                        "The universe is my canvas.",
                        "Time is meaningless."}
        ));
    }

    // ================================================================
    //  Public Query API
    // ================================================================

    /** Get the cognitive profile for a realm. Never returns null. */
    public static CognitionProfile getProfile(RealmId realm) {
        return PROFILES.getOrDefault(realm, PROFILES.get(RealmId.MORTAL));
    }

    /** Get the planning horizon in in-game days for a realm. */
    public static long getPlanningHorizon(RealmId realm) {
        return getProfile(realm).planningHorizonDays;
    }

    /** Get the concern focus string for a realm. */
    public static String getConcernFocus(RealmId realm) {
        return getProfile(realm).concernFocus;
    }

    /**
     * Get the maximum number of simultaneous goals for a realm.
     * Used by NpcGoalQueue to override MAX_GOALS_PER_NPC.
     */
    public static int getMaxGoals(RealmId realm) {
        return getProfile(realm).maxGoals;
    }

    /**
     * Get the goal deadline multiplier for a realm.
     * Multiply the base deadline (3 days = 72,000 ticks) by this.
     */
    public static double getGoalDeadlineMultiplier(RealmId realm) {
        return getProfile(realm).goalDeadlineMultiplier;
    }

    /**
     * Get the re-prioritization weight for a realm.
     * Multiplied with the DecisionStyle weight in NpcGoalQueue.
     * Higher realms resist reactive shifts.
     */
    public static double getRePrioWeight(RealmId realm) {
        return getProfile(realm).rePrioWeight;
    }

    /**
     * Get the rumor believability for a realm (0.0-1.0).
     * Used by RumorSystem to determine if an NPC acts on a rumor.
     */
    public static double getRumorBelievability(RealmId realm) {
        return getProfile(realm).rumorBelievability;
    }

    /** Whether NPCs at this realm consider karma and fate. */
    public static boolean considersKarma(RealmId realm) {
        return getProfile(realm).considersKarma;
    }

    /** Whether NPCs at this realm consider faction politics. */
    public static boolean considersFactionPolitics(RealmId realm) {
        return getProfile(realm).considersFactionPolitics;
    }

    /**
     * Check if a goal type is relevant for a given realm.
     * Irrelevant goals get a priority penalty.
     */
    public static boolean isGoalRelevant(RealmId realm, GoalType type) {
        return getProfile(realm).relevantGoalTypes.contains(type);
    }

    /**
     * Check if a goal type is amplified for a given realm.
     * Amplified goals get a priority bonus.
     */
    public static boolean isGoalAmplified(RealmId realm, GoalType type) {
        return getProfile(realm).amplifiedGoalTypes.contains(type);
    }

    /**
     * Check if a goal type is suppressed for a given realm.
     * Suppressed goals get a priority penalty.
     */
    public static boolean isGoalSuppressed(RealmId realm, GoalType type) {
        return getProfile(realm).suppressedGoalTypes.contains(type);
    }

    /**
     * Compute the realm-based priority modifier for a goal.
     * Returns a multiplier: >1.0 for amplified, <1.0 for suppressed,
     * 1.0 for normal/relevant.
     *
     * <p>Also applies a soft penalty (0.7x) for goals that are not
     * in the relevant set at all.
     *
     * @param realm the NPC's cultivation realm
     * @param type  the goal type
     * @return priority multiplier (e.g., 1.5 = 50% boost, 0.5 = 50% penalty)
     */
    public static double getGoalPriorityModifier(RealmId realm, GoalType type) {
        CognitionProfile profile = getProfile(realm);
        if (profile.suppressedGoalTypes.contains(type)) return 0.5;
        if (profile.amplifiedGoalTypes.contains(type)) return 1.5;
        if (profile.relevantGoalTypes.contains(type)) return 1.0;
        return 0.7; // not relevant — soft penalty
    }

    /**
     * Compute the effective deadline in ticks for a goal at a given realm.
     *
     * @param baseDeadlineTicks the base deadline (e.g., 72000 = 3 days)
     * @param realm             the NPC's cultivation realm
     * @return the scaled deadline in ticks
     */
    public static long getEffectiveDeadline(long baseDeadlineTicks,
                                             RealmId realm) {
        double multiplier = getGoalDeadlineMultiplier(realm);
        return (long) (baseDeadlineTicks * multiplier);
    }

    /**
     * Get a random concern example for internal monologue generation.
     * Used by B.5 to populate the "Problem" field.
     *
     * @param realm the NPC's cultivation realm
     * @param random a java.util.Random instance
     * @return a concern string, or the focus string if no examples exist
     */
    public static String getRandomConcern(RealmId realm, java.util.Random random) {
        CognitionProfile profile = getProfile(realm);
        if (profile.concernExamples == null
                || profile.concernExamples.length == 0) {
            return profile.concernFocus;
        }
        return profile.concernExamples[
                random.nextInt(profile.concernExamples.length)];
    }

    /**
     * Get the NPC's "worldview scale" — a human-readable description
     * of how broadly they perceive the world.
     * Used by B.7 (dialogue generation) to calibrate vocabulary.
     */
    public static String getWorldviewScale(RealmId realm) {
        return switch (realm.step) {
            case 0 -> switch (realm) {
                case MORTAL -> "village";
                case QI_CONDENSATION -> "region";
                case FOUNDATION -> "territory";
                case CORE_FORMATION -> "province";
                case NASCENT_SOUL -> "continent";
                case SOUL_FORMATION -> "world";
                case SOUL_TRANSFORMATION -> "world";
                case ASCENDANT -> "world";
                default -> "world";
            };
            case 1 -> "realm";
            case 2 -> "cosmology";
            case 3 -> "multiverse";
            case 4 -> "transcendent";
            default -> "world";
        };
    }
}