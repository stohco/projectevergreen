package dev.ergenverse.npc.goals;

import dev.ergenverse.npc.memory.NpcCognitiveMemory;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcGoalQueue — per-NPC priority-driven goal stack (PROJECT_MASTER.md 6.3).
 *
 * <p>Every NPC maintains a ranked list of goals. The highest-priority active
 * goal is the NPC's "Current Task" (6.2). Goals are re-prioritized periodically
 * based on:
 * <ul>
 *   <li><b>Canonical priority</b> — from {@code CANON_RI_CHARACTER_DECISIONS.md}
 *       (Primary &gt; Secondary &gt; Tertiary).</li>
 *   <li><b>Memory pressure</b> — recent COMBAT memories raise DEFEND, recent
 *       SOCIAL memories raise INTERACT, etc. (feeds from B.2).</li>
 *   <li><b>Emotional state</b> — extreme negative emotions can override normal
 *       priorities (rage -> ATTACK, fear -> FLEE).</li>
 *   <li><b>Rumor urgency</b> — recently heard RUMOR memories can create
 *       transient INVESTIGATE goals (feeds from B.3).</li>
 *   <li><b>Decision style</b> — {@code patient_planner} NPCs resist priority
 *       shifts, {@code reactive_opportunist} NPCs react faster.</li>
 * </ul>
 *
 * <h2>Goal lifecycle</h2>
 * <pre>
 *   PUSHED --&gt; ACTIVE --&gt; COMPLETED / ABANDONED / EXPIRED
 *     |           |
 *     |           +-- (re-prioritized down -&gt; may become SUSPENDED)
 *     +-- (from memory trigger, rumor, world event, canon baseline)
 * </pre>
 *
 * <h2>Performance (6.12)</h2>
 * <p>Per 6.12, ordinary NPCs get a lightweight deterministic goal (single
 * active goal, no re-prioritization). Only major NPCs run the full cognitive
 * stack. Max tracked NPCs: 200.
 *
 * <h2>Integration points</h2>
 * <ul>
 *   <li><b>Memory System (B.2)</b>: {@link #recomputePriorities} reads
 *       NpcCognitiveMemory to modulate goal scores.</li>
 *   <li><b>Rumor System (B.3)</b>: RUMOR memories spawn INVESTIGATE goals.</li>
 *   <li><b>Internal Monologue (B.5)</b>: top goal -&gt; "Current Objective" field.</li>
 *   <li><b>Dialogue Generation (B.7)</b>: goal context -&gt; thought generation.</li>
 *   <li><b>World-Sim-Tick (B.8)</b>: drives goal execution when player is AFK.</li>
 *   <li><b>Wang Lin AI (B.9)</b>: Wang Lin gets full cognitive stack integration.</li>
 * </ul>
 *
 * @see NpcGoalTickHandler  the Forge tick subscriber
 */
public class NpcGoalQueue extends SavedData {

    private static final String DATA_NAME = "ergenverse_npc_goal_queue";

    // --- NBT keys ---
    private static final String TAG_NPCS = "npcs";
    private static final String TAG_NPC_ID = "id";
    private static final String TAG_GOALS = "goals";
    private static final String TAG_ACTIVE_IDX = "ai";
    private static final String TAG_DECISION_STYLE = "ds";

    // Per-goal NBT keys
    private static final String TAG_GOAL_TYPE = "gt";
    private static final String TAG_GOAL_DESC = "gd";
    private static final String TAG_GOAL_PRIORITY = "gp";
    private static final String TAG_GOAL_STATE = "gs";
    private static final String TAG_GOAL_CREATED = "gc";
    private static final String TAG_GOAL_DEADLINE = "gx";
    private static final String TAG_GOAL_TARGET = "gtg";
    private static final String TAG_GOAL_SOURCE = "gso";
    private static final String TAG_GOAL_PERSISTENT = "gpe";

    // --- Capacity ---
    /** Max goals per NPC. Oldest non-persistent goals are evicted. */
    public static final int MAX_GOALS_PER_NPC = 15;

    /** Max distinct NPCs tracked. */
    public static final int MAX_TRACKED_NPCS = 200;

    /**
     * How often to re-prioritize (in server ticks).
     * 600 ticks = 30 seconds.
     */
    public static final int REPRIO_INTERVAL = 600;

    /**
     * Default goal expiration (in game ticks).
     * 24000 * 3 = 3 in-game days. Persistent goals ignore this.
     */
    public static final long DEFAULT_EXPIRY_TICKS = 24000L * 3;

    // --- Goal type enum ---

    /**
     * The canonical goal types for NPC cognitive simulation.
     * Each type maps to a category of NPC behavior per 6.3.
     */
    public enum GoalType {
        /** Cultivate, meditate, breakthrough. */
        CULTIVATE,
        /** Defend self, sect, ally, territory. */
        DEFEND,
        /** Attack a target (enemy, rival, prey). */
        ATTACK,
        /** Flee from a threat. */
        FLEE,
        /** Gather resources: spirit stones, herbs, materials. */
        GATHER,
        /** Travel to a location. */
        TRAVEL,
        /** Socialize: talk, trade, negotiate. */
        INTERACT,
        /** Investigate something: rumor, anomaly, player. */
        INVESTIGATE,
        /** Rest, eat, recover. */
        REST,
        /** Teach a disciple, transmit knowledge. */
        TEACH,
        /** Craft, forge, refine, alchemy. */
        CRAFT,
        /** Patrol, scout, surveil. */
        PATROL,
        /** Manage faction affairs: assign tasks, judge disputes. */
        MANAGE,
        /** Hide, lie low, avoid detection. */
        HIDE,
        /** Pursue a long-term scheme (for patient_planner types). */
        SCHEME,
        /** Pray, worship, perform ritual. */
        RITUAL,
        /** Custom/fallback goal for extensibility. */
        CUSTOM
    }

    // --- Goal state enum ---

    public enum GoalState {
        /** Currently being pursued -- the NPC's "Current Task". */
        ACTIVE,
        /** Not the top priority, but still tracked. */
        QUEUED,
        /** Temporarily paused by a higher-priority goal. */
        SUSPENDED,
        /** Goal was achieved. Pruned on next cleanup. */
        COMPLETED,
        /** Goal was abandoned. */
        ABANDONED,
        /** Goal exceeded its deadline. */
        EXPIRED
    }

    // --- Goal source enum ---

    public enum GoalSource {
        /** From CANON_RI_CHARACTER_DECISIONS.md (baseline). High inertia. */
        CANON_BASELINE,
        /** Created by the Memory System (B.2). */
        MEMORY_TRIGGERED,
        /** Created by the Rumor System (B.3). */
        RUMOR_TRIGGERED,
        /** Created by the Opportunity Engine (A.3). */
        WORLD_EVENT,
        /** Created in response to player action. */
        PLAYER_DRIVEN,
        /** Created by the Internal Monologue (B.5). */
        SELF_GENERATED,
        /** Created by the Expectation Model (B.6). */
        PREDICTION_DRIVEN,
        /** Created by another NPC's request. */
        FACTION_ORDER,
        /** Fallback / unknown source. */
        OTHER
    }

    // --- Decision style enum ---

    /**
     * How an NPC approaches decision-making. Affects re-prioritization.
     * Mirrors CANON_RI_CHARACTER_DECISIONS.md taxonomy.
     */
    public enum DecisionStyle {
        /** Long-term schemer; resists priority shifts. Weight: 0.3x. */
        PATIENT_PLANNER(0.3),
        /** Acts on immediate opportunities. Weight: 1.8x. */
        REACTIVE_OPPORTUNIST(1.8),
        /** Direct force, territorial. Weight: 1.5x for ATTACK/DEFEND. */
        AGGRESSIVE_EXPANSIONIST(1.5),
        /** Minimizes risk. Weight: 0.5x for risky goals. */
        CAUTIOUS_CONSERVATIVE(0.5),
        /** Defends allies above self. Weight: 1.5x for DEFEND. */
        PROTECTIVE_LOYALIST(1.5),
        /** Seeks knowledge. Weight: 1.5x for INVESTIGATE. */
        CURIOUS_EXPLORER(1.5),
        /** All decisions through survival. Weight: 2.0x for FLEE/REST. */
        SURVIVAL_DRIVEN(2.0),
        /** Default/balanced. Weight: 1.0x. */
        NEUTRAL(1.0);

        public final double memoryPressureWeight;
        DecisionStyle(double memoryPressureWeight) {
            this.memoryPressureWeight = memoryPressureWeight;
        }
    }

    // --- Goal entry (immutable value object) ---

    /**
     * A single goal in an NPC's priority queue. Immutable.
     */
    public static final class NpcGoal {
        public final GoalType type;
        public final String description;
        public final double priority;       // 0.0-100.0
        public final GoalState state;
        public final long createdTick;
        public final long deadlineTick;     // 0 = no deadline
        public final String targetId;
        public final GoalSource source;
        public final boolean persistent;

        public NpcGoal(GoalType type, String description, double priority,
                       GoalState state, long createdTick, long deadlineTick,
                       String targetId, GoalSource source, boolean persistent) {
            this.type = type;
            this.description = description != null ? description : "";
            this.priority = Math.max(0.0, Math.min(100.0, priority));
            this.state = state;
            this.createdTick = createdTick;
            this.deadlineTick = deadlineTick;
            this.targetId = targetId != null ? targetId : "";
            this.source = source;
            this.persistent = persistent;
        }

        public NpcGoal withState(GoalState newState) {
            return new NpcGoal(type, description, priority, newState,
                    createdTick, deadlineTick, targetId, source, persistent);
        }

        public NpcGoal withPriority(double newPriority) {
            return new NpcGoal(type, description, newPriority, state,
                    createdTick, deadlineTick, targetId, source, persistent);
        }

        public boolean canActivate() {
            return state == GoalState.QUEUED || state == GoalState.SUSPENDED;
        }

        public boolean isTerminal() {
            return state == GoalState.COMPLETED
                    || state == GoalState.ABANDONED
                    || state == GoalState.EXPIRED;
        }

        // --- NBT ---

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_GOAL_TYPE, type.name());
            tag.putString(TAG_GOAL_DESC, description);
            tag.putDouble(TAG_GOAL_PRIORITY, priority);
            tag.putString(TAG_GOAL_STATE, state.name());
            tag.putLong(TAG_GOAL_CREATED, createdTick);
            tag.putLong(TAG_GOAL_DEADLINE, deadlineTick);
            tag.putString(TAG_GOAL_TARGET, targetId);
            tag.putString(TAG_GOAL_SOURCE, source.name());
            tag.putBoolean(TAG_GOAL_PERSISTENT, persistent);
            return tag;
        }

        public static NpcGoal load(CompoundTag tag) {
            return new NpcGoal(
                    GoalType.valueOf(tag.getString(TAG_GOAL_TYPE)),
                    tag.getString(TAG_GOAL_DESC),
                    tag.getDouble(TAG_GOAL_PRIORITY),
                    GoalState.valueOf(tag.getString(TAG_GOAL_STATE)),
                    tag.getLong(TAG_GOAL_CREATED),
                    tag.getLong(TAG_GOAL_DEADLINE),
                    tag.getString(TAG_GOAL_TARGET),
                    GoalSource.valueOf(tag.getString(TAG_GOAL_SOURCE)),
                    tag.getBoolean(TAG_GOAL_PERSISTENT)
            );
        }
    }

    // --- Per-NPC goal stack ---

    /**
     * The complete goal state for one NPC.
     */
    public static final class NpcGoalStack {
        private final String npcId;
        final List<NpcGoal> goals = new ArrayList<>();
        private DecisionStyle decisionStyle = DecisionStyle.NEUTRAL;

        public NpcGoalStack(String npcId) {
            this.npcId = npcId;
        }

        public String getNpcId() { return npcId; }
        public List<NpcGoal> getGoals() { return Collections.unmodifiableList(goals); }
        public DecisionStyle getDecisionStyle() { return decisionStyle; }
        public void setDecisionStyle(DecisionStyle style) { this.decisionStyle = style; }

        /**
         * Push a new goal. Evicts lowest-priority non-persistent if at capacity.
         */
        public void push(NpcGoal goal) {
            goals.add(goal);
            if (goals.size() > MAX_GOALS_PER_NPC) {
                evictLowest();
            }
            promoteTopToActive();
        }

        @Nullable
        public NpcGoal getActiveGoal() {
            for (NpcGoal g : goals) {
                if (g.state == GoalState.ACTIVE) return g;
            }
            return null;
        }

        public List<NpcGoal> getRankedGoals() {
            List<NpcGoal> active = new ArrayList<>();
            for (NpcGoal g : goals) {
                if (!g.isTerminal()) active.add(g);
            }
            active.sort((a, b) -> Double.compare(b.priority, a.priority));
            return active;
        }

        public void completeActiveGoal() {
            NpcGoal active = getActiveGoal();
            if (active != null) {
                int idx = goals.indexOf(active);
                if (idx >= 0) goals.set(idx, active.withState(GoalState.COMPLETED));
                promoteTopToActive();
            }
        }

        public void abandonActiveGoal() {
            NpcGoal active = getActiveGoal();
            if (active != null) {
                int idx = goals.indexOf(active);
                if (idx >= 0) goals.set(idx, active.withState(GoalState.ABANDONED));
                promoteTopToActive();
            }
        }

        public void completeGoalOfType(GoalType type, String targetId) {
            boolean changed = false;
            for (int i = 0; i < goals.size(); i++) {
                NpcGoal g = goals.get(i);
                if (g.type == type && !g.isTerminal()) {
                    if (targetId.isEmpty() || g.targetId.equals(targetId)) {
                        goals.set(i, g.withState(GoalState.COMPLETED));
                        changed = true;
                    }
                }
            }
            if (changed) promoteTopToActive();
        }

        public void suspendActive() {
            NpcGoal active = getActiveGoal();
            if (active != null) {
                int idx = goals.indexOf(active);
                if (idx >= 0) goals.set(idx, active.withState(GoalState.SUSPENDED));
            }
            promoteTopToActive();
        }

        public boolean hasGoalOfType(GoalType type) {
            for (NpcGoal g : goals) {
                if (g.type == type && !g.isTerminal()) return true;
            }
            return false;
        }

        public double getHighestPriorityForType(GoalType type) {
            double max = 0.0;
            for (NpcGoal g : goals) {
                if (g.type == type && !g.isTerminal() && g.priority > max) {
                    max = g.priority;
                }
            }
            return max;
        }

        public int activeGoalCount() {
            int count = 0;
            for (NpcGoal g : goals) {
                if (!g.isTerminal()) count++;
            }
            return count;
        }

        public int expireOverdue(long currentTick) {
            int count = 0;
            for (int i = 0; i < goals.size(); i++) {
                NpcGoal g = goals.get(i);
                if (g.persistent) continue;
                if (g.deadlineTick > 0 && currentTick > g.deadlineTick
                        && !g.isTerminal()) {
                    goals.set(i, g.withState(GoalState.EXPIRED));
                    count++;
                }
            }
            if (count > 0) promoteTopToActive();
            return count;
        }

        public int pruneTerminal(long olderThanTick) {
            int before = goals.size();
            goals.removeIf(g -> g.isTerminal() && g.createdTick < olderThanTick);
            return before - goals.size();
        }

        // --- NBT ---

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString(TAG_NPC_ID, npcId);
            tag.putString(TAG_DECISION_STYLE, decisionStyle.name());
            ListTag goalList = new ListTag();
            for (NpcGoal g : goals) goalList.add(g.save());
            tag.put(TAG_GOALS, goalList);
            NpcGoal active = getActiveGoal();
            tag.putInt(TAG_ACTIVE_IDX, active != null ? goals.indexOf(active) : -1);
            return tag;
        }

        public static NpcGoalStack load(CompoundTag tag) {
            String npcId = tag.getString(TAG_NPC_ID);
            NpcGoalStack stack = new NpcGoalStack(npcId);
            try {
                stack.decisionStyle = DecisionStyle.valueOf(tag.getString(TAG_DECISION_STYLE));
            } catch (IllegalArgumentException e) {
                // Legacy data without decision style
            }
            ListTag goalList = tag.getList(TAG_GOALS, Tag.TAG_COMPOUND);
            for (int i = 0; i < goalList.size(); i++) {
                stack.goals.add(NpcGoal.load(goalList.getCompound(i)));
            }
            int activeIdx = tag.getInt(TAG_ACTIVE_IDX);
            if (activeIdx >= 0 && activeIdx < stack.goals.size()) {
                NpcGoal activeGoal = stack.goals.get(activeIdx);
                int idx = stack.goals.indexOf(activeGoal);
                if (idx >= 0) {
                    stack.goals.set(idx, activeGoal.withState(GoalState.ACTIVE));
                }
            }
            stack.promoteTopToActive();
            return stack;
        }

        // --- Internal ---

        void promoteTopToActive() {
            for (int i = 0; i < goals.size(); i++) {
                NpcGoal g = goals.get(i);
                if (g.state == GoalState.ACTIVE) {
                    goals.set(i, g.withState(GoalState.QUEUED));
                }
            }
            NpcGoal best = null;
            int bestIdx = -1;
            for (int i = 0; i < goals.size(); i++) {
                NpcGoal g = goals.get(i);
                if (g.canActivate() && (best == null || g.priority > best.priority)) {
                    best = g;
                    bestIdx = i;
                }
            }
            if (bestIdx >= 0 && best != null) {
                goals.set(bestIdx, best.withState(GoalState.ACTIVE));
            }
        }

        private void evictLowest() {
            goals.removeIf(NpcGoal::isTerminal);
            if (goals.size() <= MAX_GOALS_PER_NPC) return;

            int worstIdx = -1;
            double worstPriority = Double.MAX_VALUE;
            for (int i = 0; i < goals.size(); i++) {
                NpcGoal g = goals.get(i);
                if (!g.persistent && !g.isTerminal() && g.priority < worstPriority) {
                    worstPriority = g.priority;
                    worstIdx = i;
                }
            }
            if (worstIdx >= 0) goals.remove(worstIdx);

            while (goals.size() > MAX_GOALS_PER_NPC) {
                int oldestIdx = 0;
                long oldestTick = Long.MAX_VALUE;
                for (int i = 0; i < goals.size(); i++) {
                    NpcGoal g = goals.get(i);
                    if (g.createdTick < oldestTick) {
                        oldestTick = g.createdTick;
                        oldestIdx = i;
                    }
                }
                goals.remove(oldestIdx);
            }
        }
    }

    // ================================================================
    //  NpcGoalQueue -- the world-level SavedData container
    // ================================================================

    private final Map<String, NpcGoalStack> npcStacks = new ConcurrentHashMap<>();

    public NpcGoalQueue() {}

    public static NpcGoalQueue load(CompoundTag tag) {
        NpcGoalQueue queue = new NpcGoalQueue();
        if (tag.contains(TAG_NPCS, Tag.TAG_LIST)) {
            ListTag npcList = tag.getList(TAG_NPCS, Tag.TAG_COMPOUND);
            for (int i = 0; i < npcList.size(); i++) {
                NpcGoalStack stack = NpcGoalStack.load(npcList.getCompound(i));
                queue.npcStacks.put(stack.getNpcId(), stack);
            }
        }
        return queue;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag npcList = new ListTag();
        for (NpcGoalStack stack : npcStacks.values()) {
            npcList.add(stack.save());
        }
        tag.put(TAG_NPCS, npcList);
        return tag;
    }

    public static NpcGoalQueue get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(NpcGoalQueue::load, NpcGoalQueue::new, DATA_NAME);
    }

    // ================================================================
    //  Public API -- goal management
    // ================================================================

    public void pushGoal(String npcId, NpcGoal goal) {
        NpcGoalStack stack = getOrCreateStack(npcId);
        stack.push(goal);
        setDirty();
    }

    public void pushGoal(String npcId, GoalType type, String description,
                         double priority, long currentTick, String targetId,
                         GoalSource source) {
        pushGoal(npcId, new NpcGoal(type, description, priority,
                GoalState.QUEUED, currentTick,
                currentTick + DEFAULT_EXPIRY_TICKS,
                targetId, source, false));
    }

    public void pushPersistentGoal(String npcId, GoalType type,
                                   String description, double priority,
                                   long currentTick, String targetId,
                                   GoalSource source) {
        pushGoal(npcId, new NpcGoal(type, description, priority,
                GoalState.QUEUED, currentTick, 0,
                targetId, source, true));
    }

    public void completeActiveGoal(String npcId) {
        NpcGoalStack stack = npcStacks.get(npcId);
        if (stack != null) {
            stack.completeActiveGoal();
            setDirty();
        }
    }

    public void completeGoalOfType(String npcId, GoalType type, String targetId) {
        NpcGoalStack stack = npcStacks.get(npcId);
        if (stack != null) {
            stack.completeGoalOfType(type, targetId);
            setDirty();
        }
    }

    public void abandonActiveGoal(String npcId) {
        NpcGoalStack stack = npcStacks.get(npcId);
        if (stack != null) {
            stack.abandonActiveGoal();
            setDirty();
        }
    }

    // ================================================================
    //  Public API -- queries
    // ================================================================

    @Nullable
    public NpcGoalStack getStack(String npcId) {
        return npcStacks.get(npcId);
    }

    public NpcGoalStack getOrCreateStack(String npcId) {
        return npcStacks.computeIfAbsent(npcId, NpcGoalStack::new);
    }

    @Nullable
    public NpcGoal getActiveGoal(String npcId) {
        NpcGoalStack stack = npcStacks.get(npcId);
        return stack != null ? stack.getActiveGoal() : null;
    }

    public List<NpcGoal> getRankedGoals(String npcId) {
        NpcGoalStack stack = npcStacks.get(npcId);
        return stack != null ? stack.getRankedGoals() : List.of();
    }

    public void setDecisionStyle(String npcId, DecisionStyle style) {
        NpcGoalStack stack = getOrCreateStack(npcId);
        stack.setDecisionStyle(style);
        setDirty();
    }

    // ================================================================
    //  Re-prioritization -- memory-driven priority modulation
    // ================================================================

    /**
     * Memory-category -> GoalType boost mapping.
     * When an NPC has recent memories of a category, the corresponding
     * goal types get a priority boost.
     */
    private static final Map<String, GoalType[]> MEMORY_TO_GOAL_BOOST = new HashMap<>();
    static {
        MEMORY_TO_GOAL_BOOST.put("COMBAT", new GoalType[]{
                GoalType.DEFEND, GoalType.ATTACK, GoalType.FLEE});
        MEMORY_TO_GOAL_BOOST.put("SOCIAL", new GoalType[]{
                GoalType.INTERACT});
        MEMORY_TO_GOAL_BOOST.put("OBSERVATION", new GoalType[]{
                GoalType.INVESTIGATE, GoalType.PATROL});
        MEMORY_TO_GOAL_BOOST.put("WORLD_EVENT", new GoalType[]{
                GoalType.INVESTIGATE, GoalType.CULTIVATE});
        MEMORY_TO_GOAL_BOOST.put("PLAYER_ACTION", new GoalType[]{
                GoalType.INVESTIGATE, GoalType.INTERACT, GoalType.DEFEND});
        MEMORY_TO_GOAL_BOOST.put("RUMOR", new GoalType[]{
                GoalType.INVESTIGATE, GoalType.TRAVEL});
        MEMORY_TO_GOAL_BOOST.put("CULTIVATION", new GoalType[]{
                GoalType.CULTIVATE, GoalType.REST});
        MEMORY_TO_GOAL_BOOST.put("EMOTIONAL", new GoalType[]{
                GoalType.REST, GoalType.HIDE, GoalType.FLEE});
    }

    /**
     * Re-prioritize all goals for all NPCs based on current memories.
     *
     * <p>Algorithm per NPC:
     * <ol>
     *   <li>Query NpcCognitiveMemory for recent memories by category.</li>
     *   <li>For each category with recent entries, boost the
     *       corresponding goal types.</li>
     *   <li>Apply decision-style weighting.</li>
     *   <li>Apply emotional override (extreme emotions force FLEE/HIDE).</li>
     *   <li>Re-rank and promote the new top goal.</li>
     * </ol>
     *
     * @param level       the server overworld
     * @param currentTick the current game time
     * @return number of NPCs whose active goal changed
     */
    public int recomputePriorities(ServerLevel level, long currentTick) {
        int changes = 0;

        NpcCognitiveMemory memory;
        try {
            memory = NpcCognitiveMemory.get(level);
        } catch (Exception e) {
            return 0;
        }

        for (Map.Entry<String, NpcGoalStack> entry : npcStacks.entrySet()) {
            String npcId = entry.getKey();
            NpcGoalStack stack = entry.getValue();

            if (stack.activeGoalCount() == 0) continue;

            NpcGoal oldActive = stack.getActiveGoal();

            // 1. Compute memory-driven boosts per goal type
            Map<GoalType, Double> boosts = new HashMap<>();
            NpcCognitiveMemory.NpcMemoryStore store = memory.getStore(npcId);

            if (store != null) {
                for (Map.Entry<String, GoalType[]> mapping
                        : MEMORY_TO_GOAL_BOOST.entrySet()) {
                    String category = mapping.getKey();
                    GoalType[] goalTypes = mapping.getValue();

                    // Count recent memories in this category
                    List<NpcCognitiveMemory.MemoryEntry> recentMemories =
                            new ArrayList<>();
                    for (NpcCognitiveMemory.MemoryEntry e : store.getShortTerm()) {
                        if (e.category.name().equals(category))
                            recentMemories.add(e);
                    }
                    for (NpcCognitiveMemory.MemoryEntry e : store.getMediumTerm()) {
                        if (e.category.name().equals(category))
                            recentMemories.add(e);
                    }

                    if (recentMemories.isEmpty()) continue;

                    double boostStrength =
                            Math.min(15.0, recentMemories.size() * 3.0);
                    double styleWeight =
                            stack.getDecisionStyle().memoryPressureWeight;

                    // Decision-style-specific amplification
                    switch (stack.getDecisionStyle()) {
                        case AGGRESSIVE_EXPANSIONIST:
                            for (GoalType gt : goalTypes) {
                                if (gt == GoalType.ATTACK
                                        || gt == GoalType.DEFEND)
                                    styleWeight = 1.5;
                            }
                            break;
                        case PROTECTIVE_LOYALIST:
                            for (GoalType gt : goalTypes) {
                                if (gt == GoalType.DEFEND) styleWeight = 1.5;
                            }
                            break;
                        case CURIOUS_EXPLORER:
                            for (GoalType gt : goalTypes) {
                                if (gt == GoalType.INVESTIGATE)
                                    styleWeight = 1.5;
                            }
                            break;
                        case SURVIVAL_DRIVEN:
                            for (GoalType gt : goalTypes) {
                                if (gt == GoalType.FLEE || gt == GoalType.REST)
                                    styleWeight = 2.0;
                            }
                            break;
                        default:
                            break;
                    }

                    double finalBoost = boostStrength * styleWeight;
                    for (GoalType gt : goalTypes) {
                        boosts.merge(gt, finalBoost, Double::sum);
                    }
                }

                // 2. Emotional override
                double emotionalState = store.computeEmotionalState(currentTick);
                if (emotionalState < -0.7) {
                    double fearBoost = Math.abs(emotionalState) * 25.0;
                    boosts.merge(GoalType.FLEE, fearBoost, Double::sum);
                    boosts.merge(GoalType.HIDE, fearBoost * 0.7, Double::sum);
                } else if (emotionalState > 0.7) {
                    boosts.merge(GoalType.INTERACT, 10.0, Double::sum);
                }
            }

            // 3. Apply boosts to existing non-persistent goals
            if (!boosts.isEmpty()) {
                for (int i = 0; i < stack.goals.size(); i++) {
                    NpcGoal g = stack.goals.get(i);
                    if (g.isTerminal() || g.persistent) continue;

                    Double boost = boosts.get(g.type);
                    if (boost != null && boost > 0.5) {
                        double nudge = boost * 0.4;
                        double newPriority =
                                Math.min(100.0, g.priority + nudge);
                        stack.goals.set(i, g.withPriority(newPriority));
                    }
                }
                stack.promoteTopToActive();
            }

            // 4. Expire overdue + prune old terminal
            stack.expireOverdue(currentTick);
            stack.pruneTerminal(currentTick - 24000L);

            NpcGoal newActive = stack.getActiveGoal();
            if (oldActive != newActive
                    && (oldActive == null || newActive == null
                    || !oldActive.description.equals(newActive.description))) {
                changes++;
            }
        }

        // Cap total tracked NPCs
        while (npcStacks.size() > MAX_TRACKED_NPCS) {
            String minId = null;
            int minCount = Integer.MAX_VALUE;
            for (Map.Entry<String, NpcGoalStack> e : npcStacks.entrySet()) {
                int c = e.getValue().activeGoalCount();
                if (c < minCount) {
                    minCount = c;
                    minId = e.getKey();
                }
            }
            if (minId != null) npcStacks.remove(minId);
        }

        if (changes > 0) setDirty();
        return changes;
    }

    public String getStatusReport() {
        int totalGoals = 0;
        int activeCount = 0;
        for (NpcGoalStack s : npcStacks.values()) {
            totalGoals += s.getGoals().size();
            if (s.getActiveGoal() != null) activeCount++;
        }
        return String.format(
                "%d NPCs tracked, %d total goals, %d with active goals",
                npcStacks.size(), totalGoals, activeCount);
    }
}