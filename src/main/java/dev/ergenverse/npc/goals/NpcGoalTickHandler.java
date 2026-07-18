package dev.ergenverse.npc.goals;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.goals.NpcGoalQueue.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * NpcGoalTickHandler -- Forge event subscriber that drives the NPC
 * priority queue re-prioritization cycle and provides a static
 * convenience API for other systems.
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 * No edits to {@code Ergenverse.java} required.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> -- pre-fetches the NpcGoalQueue singleton.</li>
 *   <li><b>LevelEvent.Unload</b> -- releases cached reference.</li>
 *   <li><b>ServerTickEvent (END)</b> -- calls
 *       {@link NpcGoalQueue#recomputePriorities} every
 *       {@link NpcGoalQueue#REPRIO_INTERVAL} ticks (600 = 30 sec).</li>
 * </ul>
 *
 * <h2>Static convenience API</h2>
 * <p>Allows any subsystem to push/query/complete NPC goals without
 * resolving the SavedData themselves:
 * <ul>
 *   <li>{@link #pushGoal(String, GoalType, String, double, long, String, GoalSource)}</li>
 *   <li>{@link #pushPersistentGoal(String, GoalType, String, double, long, String, GoalSource)}</li>
 *   <li>{@link #getActiveGoal(String)}</li>
 *   <li>{@link #getRankedGoals(String)}</li>
 *   <li>{@link #completeActiveGoal(String)}</li>
 *   <li>{@link #completeGoalOfType(String, GoalType, String)}</li>
 *   <li>{@link #setDecisionStyle(String, DecisionStyle)}</li>
 * </ul>
 *
 * <h2>Memory-to-goal bridging</h2>
 * <p>On each re-prioritization cycle, the system:
 * <ol>
 *   <li>Reads NpcCognitiveMemory for each tracked NPC.</li>
 *   <li>Maps memory categories to goal type boosts.</li>
 *   <li>Applies decision-style weighting.</li>
 *   <li>Applies emotional overrides (extreme fear -> FLEE).</li>
 *   <li>Re-ranks and promotes the new top goal.</li>
 * </ol>
 *
 * <h2>Rumor-to-goal bridging</h2>
 * <p>When an NPC has RUMOR category memories, INVESTIGATE and TRAVEL
 * goals receive a priority boost. External systems (B.3 RumorEngineEvents)
 * can also directly push INVESTIGATE goals via
 * {@link #pushRumorDrivenGoal(String, String, String, long)}.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/goals/}. Integration with
 *   other Phase B systems will be done by other build steps.
 * </blockquote>
 *
 * @see NpcGoalQueue  the per-NPC priority goal stack + persistence
 * @see dev.ergenverse.npc.memory.NpcCognitiveMemory  the 3-tier memory store
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcGoalTickHandler {

    private NpcGoalTickHandler() {}

    private static volatile NpcGoalQueue cachedQueue = null;

    // ================================================================

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        cachedQueue = NpcGoalQueue.get(level);
        Ergenverse.LOGGER.info("[NpcGoalQueue] Level loaded. State: {}",
                cachedQueue.getStatusReport());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[NpcGoalQueue] Level unloading. Final state: {}",
                cachedQueue != null ? cachedQueue.getStatusReport() : "(none)");
        cachedQueue = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % NpcGoalQueue.REPRIO_INTERVAL != 0) return;

        NpcGoalQueue queue = cachedQueue != null
                ? cachedQueue
                : (cachedQueue = safeGet(overworld));
        if (queue == null) return;

        int changes = queue.recomputePriorities(overworld, ticks);
        if (changes > 0) {
            Ergenverse.LOGGER.debug(
                    "[NpcGoalQueue] Re-prioritization: {} NPCs changed active goal. {}",
                    changes, queue.getStatusReport());
        }
    }

    // ================================================================
    //  Static convenience API for other systems
    // ================================================================

    /**
     * Push a new goal for an NPC (non-persistent, expires in 3 days).
     *
     * @param npcId       NPC's canon or graph ID
     * @param type        goal type
     * @param description human-readable description of the goal
     * @param priority    0.0-100.0 (higher = more urgent)
     * @param tick        current game time
     * @param targetId    who/what the goal targets (empty if none)
     * @param source      where this goal originated
     */
    public static void pushGoal(String npcId, GoalType type,
                                String description, double priority,
                                long tick, String targetId,
                                GoalSource source) {
        NpcGoalQueue q = resolve();
        if (q != null) q.pushGoal(npcId, type, description, priority,
                tick, targetId, source);
    }

    /**
     * Push a persistent goal (never expires). Use for canon baseline
     * goals from CANON_RI_CHARACTER_DECISIONS.md.
     */
    public static void pushPersistentGoal(String npcId, GoalType type,
                                          String description, double priority,
                                          long tick, String targetId,
                                          GoalSource source) {
        NpcGoalQueue q = resolve();
        if (q != null) q.pushPersistentGoal(npcId, type, description,
                priority, tick, targetId, source);
    }

    /**
     * Push a rumor-driven INVESTIGATE goal. Convenience method for
     * the Rumor System (B.3) to create goals when an NPC hears
     * an interesting rumor.
     *
     * @param npcId      the hearing NPC
     * @param rumorId    the rumor's ID (becomes the targetId)
     * @param description brief description (e.g., "Investigate strange lights")
     * @param tick       current game time
     */
    public static void pushRumorDrivenGoal(String npcId, String rumorId,
                                            String description, long tick) {
        pushGoal(npcId, GoalType.INVESTIGATE, description,
                35.0, tick, rumorId, GoalSource.RUMOR_TRIGGERED);
    }

    /**
     * Push a combat-triggered DEFEND goal. Convenience method for
     * the Memory System (B.2) when an NPC is attacked.
     *
     * @param npcId    the defending NPC
     * @param attackerId  the attacker's ID
     * @param tick     current game time
     */
    public static void pushDefendGoal(String npcId, String attackerId,
                                      long tick) {
        pushGoal(npcId, GoalType.DEFEND,
                "Defend against " + attackerId,
                75.0, tick, attackerId, GoalSource.MEMORY_TRIGGERED);
    }

    /**
     * Get the currently active goal for an NPC (their "Current Task").
     * Returns null if no active goal exists.
     */
    @Nullable
    public static NpcGoal getActiveGoal(String npcId) {
        NpcGoalQueue q = resolve();
        return q != null ? q.getActiveGoal(npcId) : null;
    }

    /**
     * Get all non-terminal goals sorted by priority for an NPC.
     */
    public static java.util.List<NpcGoal> getRankedGoals(String npcId) {
        NpcGoalQueue q = resolve();
        return q != null ? q.getRankedGoals(npcId) : java.util.List.of();
    }

    /**
     * Complete the currently active goal for an NPC.
     * The next-highest priority goal becomes active.
     */
    public static void completeActiveGoal(String npcId) {
        NpcGoalQueue q = resolve();
        if (q != null) q.completeActiveGoal(npcId);
    }

    /**
     * Complete any non-terminal goal matching the given type and target.
     */
    public static void completeGoalOfType(String npcId, GoalType type,
                                          String targetId) {
        NpcGoalQueue q = resolve();
        if (q != null) q.completeGoalOfType(npcId, type, targetId);
    }

    /**
     * Abandon the currently active goal for an NPC.
     */
    public static void abandonActiveGoal(String npcId) {
        NpcGoalQueue q = resolve();
        if (q != null) q.abandonActiveGoal(npcId);
    }

    /**
     * Set the decision style for an NPC. Should be called once when
     * the NPC is first loaded from canon data (maps from the
     * "Decision style" field in CANON_RI_CHARACTER_DECISIONS.md).
     */
    public static void setDecisionStyle(String npcId, DecisionStyle style) {
        NpcGoalQueue q = resolve();
        if (q != null) q.setDecisionStyle(npcId, style);
    }

    /**
     * Get the decision style for an NPC (null if not set).
     */
    @Nullable
    public static DecisionStyle getDecisionStyle(String npcId) {
        NpcGoalQueue q = resolve();
        if (q == null) return null;
        NpcGoalQueue.NpcGoalStack stack = q.getStack(npcId);
        return stack != null ? stack.getDecisionStyle() : null;
    }

    // --- Internal ---

    private static NpcGoalQueue resolve() {
        return cachedQueue;
    }

    private static NpcGoalQueue safeGet(ServerLevel level) {
        try {
            return NpcGoalQueue.get(level);
        } catch (Exception e) {
            Ergenverse.LOGGER.warn("[NpcGoalQueue] Failed to load SavedData", e);
            return null;
        }
    }
}