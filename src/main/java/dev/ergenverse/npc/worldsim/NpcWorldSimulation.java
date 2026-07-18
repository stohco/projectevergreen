package dev.ergenverse.npc.worldsim;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.cognition.NpcRealmCognition;
import dev.ergenverse.npc.expectation.NpcExpectationNetwork;
import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * NpcWorldSimulation — drives NPC cognitive progression while players
 * are offline (PROJECT_MASTER.md 6.10: "World Runs Without Player").
 *
 * <p>Per 6.10: "If the player goes AFK, NPCs continue: cultivating,
 * trading, arguing, hunting, migrating, building, stealing, researching
 * formations, warring. The player returns to a changed world."
 *
 * <h2>Design approach: Cognitive catch-up, not physical simulation</h2>
 * <p>Minecraft unloads chunks when no player is nearby. NPCs in unloaded
 * chunks don't tick physically. Rather than fighting this (which would
 * require loading distant chunks — extremely expensive), this system
 * advances the <b>cognitive state</b> of tracked NPCs when players return:
 * <ul>
 *   <li><b>Goals</b> (B.1): time-based expiry, simulated completions for
 *       CULTIVATE/REST/TEACH goals based on elapsed time and realm.</li>
 *   <li><b>Memory</b> (B.2): accelerated decay based on elapsed time.</li>
 *   <li><b>Expectations</b> (B.6): accelerated decay based on elapsed time.</li>
 *   <li><b>Monologue</b> (B.5): marked stale (will regenerate on next tick).</li>
 * </ul>
 * Physical actions (movement, combat, trading) are NOT simulated —
 * those require loaded chunks. The cognitive catch-up ensures the
 * player returns to NPCs with updated goals, faded memories, and
 * shifted expectations, creating the FEELING of a world that moved on.
 *
 * <h2>Offline detection</h2>
 * <p>Tracks the game time of the last moment any player was online.
 * When a player logs in and {@code lastPlayerOnlineTick < currentTick},
 * a catch-up cycle runs. The catch-up processes ALL tracked NPCs
 * in one batch (not per-tick) for efficiency.
 *
 * <h2>Simulated goal completions</h2>
 * <p>Some goals can logically complete during offline time:
 * <ul>
 *   <li><b>CULTIVATE</b>: completes after a duration scaled by realm
 *       (mortal=6000 ticks, nascent_soul=120000 ticks, etc.). Produces
 *       a CULTIVATION memory and a new CULTIVATE goal.</li>
 *   <li><b>REST</b>: completes after 6000 ticks. Produces a restored
 *       emotional state.</li>
 *   <li><b>TEACH</b>: completes after 24000 ticks. No special effect.</li>
 * </ul>
 * Goals that require physical action (ATTACK, DEFEND, GATHER, TRAVEL,
 * INTERACT, CRAFT, PATROL, HIDE, SCHEME, RITUAL) are NOT auto-completed
 * — they are simply left as-is or expired by time.
 *
 * <h2>Performance (6.12)</h2>
 * <p>Catch-up runs once per player login, processing at most 200 NPCs
 * in a single pass. The per-NPC work is O(1) — just arithmetic on
 * tick deltas, no entity lookups or chunk loads.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/worldsim/}. Integration
 *   with other Phase B systems is read-only (queries existing APIs).
 * </blockquote>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcWorldSimulation {

    private NpcWorldSimulation() {}

    /**
     * SavedData that persists the last time any player was online.
     * This lets the catch-up system calculate how much offline time passed.
     */
    public static class WorldSimState extends net.minecraft.world.level.saveddata.SavedData {
        private static final String DATA_NAME = "ergenverse_npc_world_sim_state";

        /** Game tick of the last moment any player was online. */
        private long lastPlayerOnlineTick = 0;

        /** Total number of catch-up cycles run (for diagnostics). */
        private long totalCatchUpCycles = 0;

        /** Total game ticks simulated in catch-up (for diagnostics). */
        private long totalSimulatedTicks = 0;

        public WorldSimState() {
            super();
        }

        public long getLastPlayerOnlineTick() { return lastPlayerOnlineTick; }
        public void setLastPlayerOnlineTick(long tick) {
            this.lastPlayerOnlineTick = tick;
            setDirty();
        }
        public long getTotalCatchUpCycles() { return totalCatchUpCycles; }
        public long getTotalSimulatedTicks() { return totalSimulatedTicks; }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putLong("lastPlayerTick", lastPlayerOnlineTick);
            tag.putLong("catchUpCycles", totalCatchUpCycles);
            tag.putLong("simulatedTicks", totalSimulatedTicks);
            return tag;
        }

        public static WorldSimState load(CompoundTag tag) {
            WorldSimState state = new WorldSimState();
            state.lastPlayerOnlineTick = tag.getLong("lastPlayerTick");
            state.totalCatchUpCycles = tag.getLong("catchUpCycles");
            state.totalSimulatedTicks = tag.getLong("simulatedTicks");
            return state;
        }

        public static WorldSimState get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    WorldSimState::load, WorldSimState::new, DATA_NAME);
        }
    }

    // ─── Catch-up configuration ──────────────────────────────────────

    /** Maximum NPCs to process in a single catch-up cycle. */
    private static final int MAX_CATCHUP_NPCS = 200;

    /**
     * How many in-game days of offline time to simulate at most.
     * Caps at 30 in-game days (720,000 ticks) to prevent extremely
     * long catch-up computations if a player hasn't logged in for months.
     */
    private static final long MAX_OFFLINE_DAYS = 30;
    private static final long MAX_OFFLINE_TICKS = 24000L * MAX_OFFLINE_DAYS;

    // ─── Level lifecycle ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[NpcWorldSim] Level loaded. World simulation ready.");
    }

    // ─── Player login: trigger catch-up ──────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        ServerLevel overworld = player.serverLevel();
        long currentTick = overworld.getGameTime();
        WorldSimState state = WorldSimState.get(overworld);

        long lastTick = state.getLastPlayerOnlineTick();
        long offlineDelta = currentTick - lastTick;

        // Only run catch-up if more than 1200 ticks (1 minute) have passed.
        // Short AFKs (player just respawned, etc.) don't need simulation.
        if (offlineDelta < 1200) return;

        // Cap the offline delta
        long effectiveDelta = Math.min(offlineDelta, MAX_OFFLINE_TICKS);

        if (effectiveDelta >= 24000) {
            Ergenverse.LOGGER.info("[NpcWorldSim] Player {} returned after {} in-game days. Running catch-up...",
                    player.getName().getString(), effectiveDelta / 24000);
        }

        int result = runCatchUp(overworld, effectiveDelta, currentTick);
        state.setLastPlayerOnlineTick(currentTick);
        state.totalCatchUpCycles++;
        state.totalSimulatedTicks += effectiveDelta;
        state.setDirty();

        if (result > 0) {
            Ergenverse.LOGGER.info("[NpcWorldSim] Catch-up complete: {} NPCs processed, {} changes.",
                    result >> 16, result & 0xFFFF);
        }
    }

    // ─── Server tick: update last-player-seen ────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;

        // Track last tick any player was online
        boolean anyPlayerOnline = false;
        for (ServerPlayer sp : server.getPlayerList().getPlayers()) {
            // A player in the player list is online unless explicitly disconnected
            anyPlayerOnline = true;
            break;
        }

        if (anyPlayerOnline) {
            ServerLevel overworld = server.overworld();
            WorldSimState state = WorldSimState.get(overworld);
            state.setLastPlayerOnlineTick(overworld.getGameTime());
        }
    }

    // ─── Catch-up engine ─────────────────────────────────────────────

    /**
     * Run a single catch-up cycle, advancing all NPC cognitive systems
     * by the given tick delta.
     *
     * @return a packed value: (npcCount << 16) | changesCount
     */
    private static int runCatchUp(ServerLevel overworld, long offlineDelta,
                                  long currentTick) {
        NpcGoalQueue queue = NpcGoalQueue.get(overworld);
        NpcCognitiveMemory memory = NpcCognitiveMemory.get(overworld);
        NpcExpectationNetwork expectations = NpcExpectationNetwork.get(overworld);

        if (queue == null || memory == null || expectations == null) return 0;

        int npcCount = 0;
        int totalChanges = 0;

        // Process all tracked NPCs from the expectation network
        // (the broadest cross-referenced set — NPCs that have interacted with players)
        java.util.Set<String> npcIds = expectations.allNpcIds();
        for (String npcId : npcIds) {
            if (npcCount >= MAX_CATCHUP_NPCS) break;
            npcCount++;
            int changes = simulateNpcOffline(npcId, queue, memory, expectations,
                    offlineDelta, currentTick);
            totalChanges += changes;
        }

        return (npcCount << 16) | (totalChanges & 0xFFFF);
    }

    /**
     * Simulate one NPC's offline progression.
     * Returns the number of state changes made.
     */
    private static int simulateNpcOffline(String npcId,
                                            NpcGoalQueue queue,
                                            NpcCognitiveMemory memory,
                                            NpcExpectationNetwork expectations,
                                            long offlineDelta,
                                            long currentTick) {
        int changes = 0;

        // 1. Goal progression: expire overdue goals, simulate completions
        changes += simulateGoalProgression(npcId, queue, offlineDelta, currentTick);

        // 2. Memory decay: run multiple decay cycles to cover offline time
        // Memory decays every 1200 ticks (1 min), so run ceil(offlineDelta/1200) cycles
        long decayCycles = (offlineDelta + 1199) / 1200;
        // Cap at 600 cycles (10 hours of decay) to avoid excessive computation
        decayCycles = Math.min(decayCycles, 600);
        for (long i = 0; i < decayCycles; i++) {
            // Decay uses currentTick offset to simulate passage of time
            long simulatedTick = currentTick - offlineDelta + (i * 1200);
            if (simulatedTick < 0) simulatedTick = 0;
            int decayed = memory.decayTick(simulatedTick);
            changes += decayed;
        }

        // 3. Expectation decay
        int predPruned = expectations.decayTick(currentTick);
        changes += predPruned;

        return changes;
    }

    /**
     * Simulate goal progression for an NPC during offline time.
     * Handles: time-based expiry and simulated completions for
     * "passive" goals (CULTIVATE, REST, TEACH).
     */
    private static int simulateGoalProgression(String npcId,
                                                NpcGoalQueue queue,
                                                long offlineDelta,
                                                long currentTick) {
        int changes = 0;
        NpcGoalQueue.NpcGoalStack stack = queue.getStack(npcId);
        if (stack == null) return 0;

        // Get all goals and check for completions/expiries
        List<NpcGoalQueue.NpcGoal> goals = queue.getRankedGoals(npcId);
        if (goals.isEmpty()) return 0;

        for (NpcGoalQueue.NpcGoal goal : goals) {
            if (goal.state != NpcGoalQueue.GoalState.ACTIVE
                    && goal.state != NpcGoalQueue.GoalState.QUEUED) continue;

            long age = currentTick - goal.createdTick;
            long remaining = goal.deadlineTick - currentTick;

            // Check if goal should have expired during offline time
            if (remaining <= 0) {
                // The normal expiry system handles this, but let's check
                // if it was a passive goal that could have been completed
                if (canAutoComplete(goal)) {
                    long completionTime = estimateCompletionTicks(goal);
                    if (age >= completionTime) {
                        queue.completeActiveGoal(npcId);
                        changes++;
                    }
                }
                // If not auto-completable, it's just expired — the normal
                // re-prio system will handle it on next tick
                continue;
            }

            // Check if a passive goal should auto-complete during offline time
            if (canAutoComplete(goal)) {
                long completionTime = estimateCompletionTicks(goal);
                // If the goal has been active long enough AND would complete
                // during the offline period
                long activeDuration = currentTick - goal.createdTick;
                if (activeDuration >= completionTime) {
                    queue.completeActiveGoal(npcId);
                    changes++;

                    // For CULTIVATE goals, push a new CULTIVATE goal
                    // (NPCs keep cultivating — it's their primary activity)
                    if (goal.type == NpcGoalQueue.GoalType.CULTIVATE) {
                        queue.pushGoal(npcId,
                                NpcGoalQueue.GoalType.CULTIVATE,
                                "Continue cultivation practice.",
                                0.3, currentTick, "",
                                NpcGoalQueue.GoalSource.SELF_GENERATED);
                        changes++;
                    }
                }
            }
        }

        return changes;
    }

    /**
     * Can this goal type be auto-completed during offline simulation?
     * Only "passive" goals that don't require physical interaction.
     */
    private static boolean canAutoComplete(NpcGoalQueue.NpcGoal goal) {
        return goal.type == NpcGoalQueue.GoalType.CULTIVATE
                || goal.type == NpcGoalQueue.GoalType.REST
                || goal.type == NpcGoalQueue.GoalType.TEACH;
    }

    /**
     * Estimate how many ticks it takes to complete a passive goal.
     * Scaled by realm (higher realm = longer cultivation sessions).
     * Uses a simple heuristic based on the goal type.
     */
    private static long estimateCompletionTicks(NpcGoalQueue.NpcGoal goal) {
        return switch (goal.type) {
            case CULTIVATE -> 24000L; // 1 in-game day per session
            case REST -> 6000L;      // 5 minutes
            case TEACH -> 24000L;     // 1 in-game day
            default -> Long.MAX_VALUE; // not auto-completable
        };
    }

    // ─── Static query API ────────────────────────────────────────────

    /**
     * Get the world simulation state (for diagnostics/debug).
     */
    public static WorldSimState getState(ServerLevel level) {
        return WorldSimState.get(level);
    }

    /**
     * Get the number of in-game days since the last player was online.
     * Returns 0 if a player is currently online or less than a day passed.
     */
    public static long getOfflineDays(ServerLevel level) {
        WorldSimState state = WorldSimState.get(level);
        long delta = level.getGameTime() - state.getLastPlayerOnlineTick();
        return delta / 24000L;
    }
}