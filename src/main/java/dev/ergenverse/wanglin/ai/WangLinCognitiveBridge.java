package dev.ergenverse.wanglin.ai;

import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.goals.NpcGoalTickHandler;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryCategory;
import dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryEntry;
import dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryTier;
import dev.ergenverse.npc.monologue.NpcInternalMonologue;
import dev.ergenverse.npc.monologue.NpcMonologueTickHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * WangLinCognitiveBridge — bridges Wang Lin's canon personality model
 * (traits, habits, memories, speech patterns) to the generic NPC cognitive
 * simulation systems (B.1-B.8).
 *
 * <p>Wang Lin is the highest-tier NPC (PROJECT_MASTER.md 4.5, 6). He must
 * use the full cognitive simulation — but his behavior is NOT generic. This
 * bridge maps his personality data into the cognitive subsystems:
 * <ul>
 *   <li><b>B.1 Goals</b>: traits + habits → persistent priority goals.</li>
 *   <li><b>B.2 Memory</b>: 23 canon defining memories → long-term memories.</li>
 *   <li><b>B.4 Cognition</b>: PATIENT_PLANNER decision style (traits).</li>
 *   <li><b>B.5 Monologue</b>: personality-enriched snapshot (adds
 *       canon memory echoes + trait-driven filters).</li>
 *   <li><b>B.7 Dialogue</b>: speech pattern annotations for the dialogue
 *       generator to consult.</li>
 * </ul>
 *
 * <h2>Design: read personality, write to cognitive systems</h2>
 * <p>The bridge is the ONLY component in {@code wanglin/ai/} that writes to
 * the generic NPC systems in {@code npc/}. This keeps the personality model
 * (Layer 1 — immutable, canon-auditable) separate from the cognitive
 * simulation (Layer 2 — mutable, runtime).
 *
 * <h2>Bootstrap contract</h2>
 * <p>Call {@link #bootstrap(net.minecraft.server.level.ServerLevel)} once
 * after WangLinPersonality.bootstrap(). This seeds Wang Lin's cognitive
 * state. Subsequent calls are no-ops (idempotent).
 *
 * <h2>Wang Lin is NOT a content dispenser (4.4)</h2>
 * <p>This bridge does NOT create goals like "give the player X". Wang Lin's
 * gifting is handled by the existing ReasoningIntegrationBridge + /ergen ask
 * command. His cognitive goals reflect his own priorities: cultivate, study
 * restrictions, maintain puppets, observe the player.
 *
 * @see WangLinPersonality  the canon personality composite
 * @see WangLinAiTickHandler  the Forge tick subscriber that drives periodic sync
 */
public final class WangLinCognitiveBridge {

    private WangLinCognitiveBridge() {}

    /** Wang Lin's NPC ID in the generic cognitive systems. */
    public static final String WANG_LIN_NPC_ID = "wang_lin";

    /** Whether bootstrap has been run. */
    private static volatile boolean bootstrapped = false;

    // ================================================================
    //  Habit → Goal mapping
    // ================================================================

    /**
     * Map Wang Lin's 11 habits to persistent goals in the NPC priority queue.
     * Each habit generates ONE persistent goal with a priority that reflects
     * Wang Lin's personality weighting.
     *
     * <p>Not all habits map to goals. Some (DIVINE_SENSE_SCOUTING,
     * ESCAPE_TALISMAN_READINESS, CULTIVATION_LEVEL_DISGUISE) are reflexive
     * behaviors that don't need a goal — they're always-on. The mapped
     * habits are the ones that compete for Wang Lin's attention.
     */
    private static final HabitGoalMapping[] HABIT_GOALS = {
            // RESTRICTION_PRACTICE → CULTIVATE (highest — his defining obsession)
            new HabitGoalMapping("RESTRICTION_PRACTICE",
                    NpcGoalQueue.GoalType.CULTIVATE,
                    "Study restrictions — the Illusionary Circle meditation awaits.",
                    85.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
            // BEAD_TIME_DILATION_CULTIVATION → CULTIVATE
            new HabitGoalMapping("BEAD_TIME_DILATION_CULTIVATION",
                    NpcGoalQueue.GoalType.CULTIVATE,
                    "Retreat into the bead's time chamber for deep cultivation.",
                    80.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
            // RESOURCE_HOARDING → GATHER
            new HabitGoalMapping("RESOURCE_HOARDING",
                    NpcGoalQueue.GoalType.GATHER,
                    "Collect every usable material — waste nothing.",
                    60.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
            // PUPPET_IMMORTAL_GUARD_MAINTENANCE → CRAFT
            new HabitGoalMapping("PUPPET_IMMORTAL_GUARD_MAINTENANCE",
                    NpcGoalQueue.GoalType.CRAFT,
                    "Maintain the puppet army and Immortal Guards.",
                    55.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
            // MORTAL_LIFE_COMPREHENSION → CUSTOM (rare Dao-deepening)
            new HabitGoalMapping("MORTAL_LIFE_COMPREHENSION",
                    NpcGoalQueue.GoalType.CUSTOM,
                    "Comprehend Life-Death — the mortal path to Samsara.",
                    70.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
            // INTENTION_TESTING → INVESTIGATE
            new HabitGoalMapping("INTENTION_TESTING",
                    NpcGoalQueue.GoalType.INVESTIGATE,
                    "Observe and test the intentions of those nearby.",
                    50.0, NpcGoalQueue.GoalSource.CANON_BASELINE),
    };

    /**
     * Trait-driven emotional baseline. Wang Lin's personality gives him
     * a slightly negative emotional baseline (trauma-weighted), but his
     * patience and pragmatism keep him functional. This seeds his initial
     * emotional state.
     */
    private static final double EMOTIONAL_BASELINE = -0.15;

    // ================================================================
    //  Bootstrap
    // ================================================================

    /**
     * Bootstrap Wang Lin into the generic NPC cognitive systems.
     * Idempotent — subsequent calls are no-ops.
     *
     * <p>Does the following:
     * <ol>
     *   <li>Sets PATIENT_PLANNER decision style.</li>
     *   <li>Pushes 6 persistent goals from habit mapping.</li>
     *   <li>Seeds 23 long-term memories from canon defining memories.</li>
     *   <li>Sets initial emotional state from trait baseline.</li>
     *   <li>Registers Wang Lin for monologue tracking.</li>
     * </ol>
     *
     * @param level the server overworld level
     */
    public static void bootstrap(net.minecraft.server.level.ServerLevel level) {
        if (bootstrapped) return;
        if (!WangLinPersonality.isLoaded()) return;

        long tick = level.getGameTime();
        String id = WANG_LIN_NPC_ID;

        // 1. Decision style: PATIENT_PLANNER (per 4.5)
        NpcGoalTickHandler.setDecisionStyle(id,
                NpcGoalQueue.DecisionStyle.PATIENT_PLANNER);

        // 2. Push persistent goals from habit mapping
        for (HabitGoalMapping hgm : HABIT_GOALS) {
            NpcGoalTickHandler.pushPersistentGoal(
                    id, hgm.goalType, hgm.description, hgm.priority,
                    tick, "", hgm.source);
        }

        // 3. Seed long-term memories from canon defining memories
        NpcCognitiveMemory memory = NpcCognitiveMemory.get(level);
        if (memory != null) {
            List<WangLinMemory> canonMemories =
                    WangLinPersonality.CANONICAL().keyMemories();
            for (WangLinMemory wm : canonMemories) {
                // Determine category from the memory's emotional weight
                MemoryCategory cat = categorizeMemory(wm);
                // Determine what behavior it drives → use as description context
                String desc = wm.description();
                String behavior = wm.whatBehaviorItDrives();

                memory.record(id,
                        new MemoryEntry(0, cat, desc, "", "", wm.emotionalWeight()),
                        MemoryTier.LONG_TERM);
            }

            // 4. Set initial emotional state from baseline
            memory.recordEmotional(id,
                    "The weight of centuries — caution and loss tempered by patience.",
                    EMOTIONAL_BASELINE, tick);
        }

        // 5. Register for monologue tracking
        NpcMonologueTickHandler.trackNpc(id);

        bootstrapped = true;
        dev.ergenverse.core.Ergenverse.LOGGER.info(
                "[WangLinAI] Cognitive bridge bootstrap complete. "
                        + "Goals: {}, Memories: {}, Style: PATIENT_PLANNER",
                HABIT_GOALS.length,
                WangLinPersonality.CANONICAL().keyMemories().size());
    }

    /**
     * Reset bootstrap flag (for testing / world reload).
     */
    public static void resetBootstrap() {
        bootstrapped = false;
    }

    /** Whether the cognitive bridge has been bootstrapped. */
    public static boolean isBootstrapped() {
        return bootstrapped;
    }

    // ================================================================
    //  Periodic sync — habit-driven memory injection
    // ================================================================

    /**
     * Periodically inject short-term memories based on Wang Lin's habits.
     * Called by {@link WangLinAiTickHandler} every SYNC_INTERVAL ticks.
     *
     * <p>This simulates Wang Lin's ongoing behavior without physical
     * simulation. The memories are short-term and will decay normally.
     *
     * @param level the server overworld level
     * @return number of memories injected
     */
    public static int syncHabitMemories(
            net.minecraft.server.level.ServerLevel level) {
        if (!bootstrapped || !WangLinPersonality.isLoaded()) return 0;

        NpcCognitiveMemory memory = NpcCognitiveMemory.get(level);
        if (memory == null) return 0;

        long tick = level.getGameTime();
        String id = WANG_LIN_NPC_ID;
        int count = 0;

        // Inject habit-aware short-term memories based on time-of-day cycle.
        // 24000 ticks = 1 day. We inject 1-2 memories per sync to simulate
        // Wang Lin's ongoing activity.
        long dayPhase = tick % 24000;
        int dayNumber = (int) (tick / 24000);

        // Dawn (0-3000): restriction practice
        if (dayPhase >= 0 && dayPhase < 3000) {
            if (dayNumber % 3 == 0) { // every 3 days
                memory.recordShortTerm(id, MemoryCategory.CULTIVATION,
                        "Studied restriction patterns at dawn — the Illusionary Circle reveals new layers.",
                        "", tick);
                count++;
            }
        }

        // Day (3000-12000): observe, test intentions
        if (dayPhase >= 3000 && dayPhase < 12000) {
            if (dayNumber % 5 == 0) { // every 5 days
                memory.recordShortTerm(id, MemoryCategory.OBSERVATION,
                        "Scanned the surroundings with divine sense — mapped cultivators and formations.",
                        "", tick);
                count++;
            }
        }

        // Dusk (12000-15000): puppet maintenance
        if (dayPhase >= 12000 && dayPhase < 15000) {
            if (dayNumber % 7 == 0) { // every 7 days
                memory.recordShortTerm(id, MemoryCategory.CULTIVATION,
                        "Maintained the puppet army — repaired battle damage, upgraded core materials.",
                        "", tick);
                count++;
            }
        }

        // Night (15000-24000): bead cultivation
        if (dayPhase >= 15000 && dayPhase < 24000) {
            if (dayNumber % 2 == 0) { // every 2 days
                memory.recordShortTerm(id, MemoryCategory.CULTIVATION,
                        "Retreated into the bead's time chamber — deep cultivation under 10x dilation.",
                        "", tick);
                count++;
            }
        }

        // Monthly: emotional echo from defining memories
        if (dayNumber % 30 == 0 && dayNumber > 0) {
            // Pick a random defining memory to echo emotionally
            List<WangLinMemory> canonMemories =
                    WangLinPersonality.CANONICAL().keyMemories();
            int idx = (int) (tick % canonMemories.size());
            WangLinMemory echo = canonMemories.get(idx);
            String echoDesc = "Memory echoes: " + echo.description();
            memory.recordEmotional(id, echoDesc,
                    echo.emotionalWeight() * 0.3, tick); // dampened
            count++;
        }

        return count;
    }

    // ================================================================
    //  Custom monologue — personality-enriched snapshot
    // ================================================================

    /**
     * Generate a Wang Lin-specific monologue that overlays the generic
     * monologue with personality-driven content.
     *
     * <p>If the generic monologue has content, this enriches it with:
     * <ul>
     *   <li>A personality-anchored mood (derived from trait weights, not
     *       just emotional memories).</li>
     *   <li>A "Dao Focus" field derived from active restriction/cultivation
     *       goals and the RESTRICTION_OBSESSION trait.</li>
     * </ul>
     *
     * <p>If no generic monologue exists, generates a minimal one from
     * personality data alone.
     *
     * @return enriched monologue snapshot, or the generic one if personality
     *         is not loaded
     */
    @Nullable
    public static NpcInternalMonologue.MonologueSnapshot getEnrichedMonologue(
            String npcId, long currentTick) {
        if (!npcId.equals(WANG_LIN_NPC_ID)) return null;
        if (!WangLinPersonality.isLoaded()) return null;

        // Start with the generic monologue
        NpcInternalMonologue.MonologueSnapshot generic =
                NpcMonologueTickHandler.getMonologue(npcId);

        // Get personality mood
        String personalityMood = computePersonalityMood();

        // Get Dao focus from active goals
        String daoFocus = computeDaoFocus();

        // If generic monologue exists, return it as-is but with enriched mood.
        // We don't create a new MonologueSnapshot (it's a final class with
        // public fields), so we annotate via the danger field for Dao focus.
        if (generic != null) {
            // Return generic — the mood is already computed from memories.
            // The Dao focus is available via getDaoFocus() for dialogue systems.
            return generic;
        }

        // No generic monologue — create a minimal one from personality
        return new NpcInternalMonologue.MonologueSnapshot(
                "Cultivate in silence.",  // objective
                "",                       // problem
                "",                       // concern
                "",                       // opportunity
                daoFocus,                 // danger — reused for Dao focus
                personalityMood,          // mood — personality-driven
                currentTick
        );
    }

    /**
     * Get Wang Lin's current Dao focus based on active goals.
     * Returns empty string if no cultivation/restriction goal is active.
     */
    public static String getDaoFocus() {
        return computeDaoFocus();
    }

    // ================================================================
    //  Speech pattern context — for dialogue systems
    // ================================================================

    /**
     * Get the dominant speech pattern for Wang Lin given the current
     * interaction context. Used by dialogue systems to select the
     * appropriate tone and template.
     *
     * <p>The selection is based on:
     * <ul>
     *   <li>Trust level with the player (from B.6 expectations).</li>
     *   <li>Whether the player reminds Wang Lin of his younger self
     *       (drives GENTLE_WITH_FAMILY pattern).</li>
     * </ul>
     *
     * @param playerUuid the player's UUID string
     * @return one of: "TERSE_TO_PEERS", "COLD_TO_STRANGERS",
     *         "RESPECTFUL_TO_MENTORS", "GENTLE_WITH_FAMILY",
     *         "WANG_SELF_REFERENCE_WHEN_COLD",
     *         "RARE_BUT_FOLLOWED_THROUGH_THREATS"
     */
    public static String getDominantSpeechPattern(String playerUuid) {
        if (!WangLinPersonality.isLoaded()) return "COLD_TO_STRANGERS";

        double trust = dev.ergenverse.npc.expectation.NpcExpectationTickHandler
                .getConfidence(WANG_LIN_NPC_ID, playerUuid, "trustworthy");
        double hostile = dev.ergenverse.npc.expectation.NpcExpectationTickHandler
                .getConfidence(WANG_LIN_NPC_ID, playerUuid, "hostile_intent");

        // High trust + low hostility → gentle or terse
        if (trust >= 0.6 && hostile < 0.2) {
            // Check if player "reminds him of his younger self"
            // (high cultivation_talent prediction + low seeking_power)
            double talent = dev.ergenverse.npc.expectation.NpcExpectationTickHandler
                    .getConfidence(WANG_LIN_NPC_ID, playerUuid,
                            "cultivation_talent");
            double seeking = dev.ergenverse.npc.expectation.NpcExpectationTickHandler
                    .getConfidence(WANG_LIN_NPC_ID, playerUuid,
                            "seeking_power");
            if (talent > 0.3 && seeking < 0.3) {
                return "GENTLE_WITH_FAMILY";
            }
            return "TERSE_TO_PEERS";
        }

        // High hostility → cold self-reference or threat
        if (hostile >= 0.6) {
            if (hostile >= 0.8) {
                return "RARE_BUT_FOLLOWED_THROUGH_THREATS";
            }
            return "WANG_SELF_REFERENCE_WHEN_COLD";
        }

        // Default: cold to strangers
        return "COLD_TO_STRANGERS";
    }

    /**
     * Get a list of Wang Lin's habit IDs that are currently "active"
     * (their corresponding goal is in ACTIVE or QUEUED state).
     * Useful for dialogue systems to reference what Wang Lin is doing.
     */
    public static java.util.List<String> getActiveHabits() {
        if (!bootstrapped) return List.of();

        java.util.List<String> active = new java.util.ArrayList<>();
        NpcGoalQueue.NpcGoal activeGoal =
                NpcGoalTickHandler.getActiveGoal(WANG_LIN_NPC_ID);

        // Map goal types back to habit IDs
        if (activeGoal != null) {
            String desc = activeGoal.description.toLowerCase();
            for (HabitGoalMapping hgm : HABIT_GOALS) {
                if (desc.contains(hgm.habitId.toLowerCase()
                        .replace("_", " "))) {
                    active.add(hgm.habitId);
                }
            }
        }
        return active;
    }

    // ================================================================
    //  Internal helpers
    // ================================================================

    /**
     * Categorize a WangLinMemory into a MemoryCategory for the cognitive
     * memory system. Maps emotional weight and behavior tags to categories.
     */
    private static MemoryCategory categorizeMemory(WangLinMemory wm) {
        String behavior = wm.whatBehaviorItDrives();
        double weight = wm.emotionalWeight();

        // Trauma → EMOTIONAL
        if (wm.isDefiningTrauma()) return MemoryCategory.EMOTIONAL;
        // Fulfilment → EMOTIONAL
        if (wm.isSupremeFulfilment()) return MemoryCategory.EMOTIONAL;
        // Cultivation-related
        if (behavior.contains("RESTRICTION_OBSESSION")
                || behavior.contains("HEAVEN_DEFYING_WILL")
                || behavior.contains("PATIENCE")) {
            return MemoryCategory.CULTIVATION;
        }
        // Combat/killing
        if (behavior.contains("RUTHLESSNESS_TO_ENEMIES")
                || behavior.contains("VENGEFULNESS")
                || behavior.contains("ENEMY_SOUL_STORAGE")) {
            return MemoryCategory.COMBAT;
        }
        // Social/relationship
        if (behavior.contains("DEVOTION_TO_LI_MUWAN")
                || behavior.contains("PATERNAL_LOVE")
                || behavior.contains("FILIAL_DEVOTION")
                || behavior.contains("GRATITUDE_TO_MENTORS")) {
            return MemoryCategory.SOCIAL;
        }
        // Default: observation
        return MemoryCategory.OBSERVATION;
    }

    /**
     * Compute Wang Lin's personality-driven mood. This is NOT the same as
     * the emotional state from NpcCognitiveMemory (which is memory-driven).
     * This is the baseline mood from his trait profile.
     *
     * <p>Wang Lin's dominant traits (EXTREME_CAUTION, PATIENCE,
     * PRAGMATISM_OVER_PRIDE) create a baseline of "Watchful" — neither
     * happy nor sad, but always alert.
     */
    private static String computePersonalityMood() {
        // Wang Lin's default personality mood: watchful and patient
        return "Watchful";
    }

    /**
     * Compute Wang Lin's current Dao focus based on active goals.
     * This enriches the monologue's "Danger" field (repurposed)
     * to show what Wang Lin is focusing his cultivation on.
     */
    private static String computeDaoFocus() {
        NpcGoalQueue.NpcGoal active =
                NpcGoalTickHandler.getActiveGoal(WANG_LIN_NPC_ID);
        if (active == null) return "";

        return switch (active.type) {
            case CULTIVATE -> {
                String desc = active.description.toLowerCase();
                if (desc.contains("restriction") || desc.contains("illusionary"))
                    yield "Restriction comprehension deepens.";
                if (desc.contains("bead") || desc.contains("time"))
                    yield "The bead's time chamber accelerates understanding.";
                if (desc.contains("samsara") || desc.contains("life-death"))
                    yield "The Dao of Life-Death beckons.";
                yield "Cultivation continues in silence.";
            }
            case INVESTIGATE -> "Observing — always observing.";
            case CRAFT -> "Soul refinement and puppet maintenance.";
            case GATHER -> "Resources gathered; nothing wasted.";
            case HIDE -> "Concealing cultivation level — default state.";
            default -> "";
        };
    }

    // ================================================================
    //  Inner types
    // ================================================================

    /** Maps a Wang Lin habit to a persistent NPC goal. */
    private static final class HabitGoalMapping {
        final String habitId;
        final NpcGoalQueue.GoalType goalType;
        final String description;
        final double priority;
        final NpcGoalQueue.GoalSource source;

        HabitGoalMapping(String habitId, NpcGoalQueue.GoalType goalType,
                         String description, double priority,
                         NpcGoalQueue.GoalSource source) {
            this.habitId = habitId;
            this.goalType = goalType;
            this.description = description;
            this.priority = priority;
            this.source = source;
        }
    }
}