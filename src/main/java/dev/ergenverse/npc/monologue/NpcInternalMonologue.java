package dev.ergenverse.npc.monologue;

import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.goals.NpcGoalTickHandler;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NpcInternalMonologue -- per-NPC hidden internal monologue (6.9).
 *
 * <p>Every important NPC has an internal monologue updating every few seconds
 * (not shown to player):
 * <pre>
 *   Current Objective: Repair formation.
 *   Problem: Need spirit stones.
 *   Concern: Outer disciple watching me.
 *   Opportunity: Merchant arriving tomorrow.
 *   Danger: Formation instability rising.
 *   Mood: Anxious
 * </pre>
 * Every decision the NPC makes comes from this monologue.
 *
 * <h2>Data sources (synthesized into the monologue)</h2>
 * <ul>
 *   <li><b>Priority Queue (B.1)</b>: active goal -&gt; "Current Objective".</li>
 *   <li><b>Memory System (B.2)</b>: recent memories -&gt; "Problem",
 *       "Concern", "Danger".</li>
 *   <li><b>Rumor System (B.3)</b>: heard rumors -&gt; "Opportunity".</li>
 *   <li><b>Realm Cognition (B.4)</b>: concern focus calibrates depth.</li>
 * </ul>
 *
 * <h2>Consumers</h2>
 * <ul>
 *   <li><b>Dialogue Generation (B.7)</b>: thought -&gt; spoken words.</li>
 *   <li><b>World-Sim-Tick (B.8)</b>: "Current Objective" drives AFK behavior.</li>
 *   <li><b>Wang Lin AI (B.9)</b>: full cognitive stack.</li>
 * </ul>
 *
 * <h2>Performance (6.12)</h2>
 * <p>Lightweight string assembly. Max 200 tracked NPCs.
 * Update interval: 200 ticks (10 seconds).
 * <b>NOT persisted</b> -- regenerated from cognitive systems on load.
 *
 * @see NpcMonologueTickHandler
 */
public class NpcInternalMonologue {

    /** Update interval in server ticks. 200 = 10 seconds. */
    public static final int UPDATE_INTERVAL = 200;

    /** Max NPCs to track monologues for. */
    public static final int MAX_TRACKED = 200;

    // ================================================================
    //  Monologue snapshot (immutable value object)
    // ================================================================

    /**
     * A snapshot of an NPC's internal monologue at a point in time.
     * Immutable. Regenerated periodically.
     */
    public static final class MonologueSnapshot {
        /** The NPC's current top-priority goal (from B.1). */
        public final String currentObjective;
        /** Most pressing unresolved need (from B.2). */
        public final String problem;
        /** A social or situational concern (from B.2). */
        public final String concern;
        /** A potential opportunity (from B.3 rumors or B.2). */
        public final String opportunity;
        /** A threat the NPC perceives (from B.2 combat/emotional). */
        public final String danger;
        /** Current emotional state as a label. */
        public final String moodLabel;
        /** Game tick when generated. */
        public final long generatedTick;

        public MonologueSnapshot(String currentObjective, String problem,
                                 String concern, String opportunity,
                                 String danger, String moodLabel,
                                 long generatedTick) {
            this.currentObjective = orEmpty(currentObjective);
            this.problem = orEmpty(problem);
            this.concern = orEmpty(concern);
            this.opportunity = orEmpty(opportunity);
            this.danger = orEmpty(danger);
            this.moodLabel = orEmpty(moodLabel);
            this.generatedTick = generatedTick;
        }

        /**
         * Format as multi-line string for debug display.
         */
        public String format() {
            StringBuilder sb = new StringBuilder();
            if (!currentObjective.isEmpty())
                sb.append("Objective: ").append(currentObjective).append("\n");
            if (!problem.isEmpty())
                sb.append("Problem: ").append(problem).append("\n");
            if (!concern.isEmpty())
                sb.append("Concern: ").append(concern).append("\n");
            if (!opportunity.isEmpty())
                sb.append("Opportunity: ").append(opportunity).append("\n");
            if (!danger.isEmpty())
                sb.append("Danger: ").append(danger).append("\n");
            sb.append("Mood: ").append(moodLabel);
            return sb.toString();
        }
    }

    // ================================================================
    //  Monologue state
    // ================================================================

    private final Map<String, MonologueSnapshot> monologues =
            new ConcurrentHashMap<>();

    /** Set of NPC IDs known to have cognitive state (for batch generation). */
    private final Set<String> trackedNpcIds =
            ConcurrentHashMap.newKeySet();

    // ================================================================
    //  Monologue generation
    // ================================================================

    /**
     * Generate a fresh monologue for an NPC by synthesizing all
     * cognitive subsystems.
     *
     * <ol>
     *   <li><b>Objective</b>: active goal from NpcGoalQueue (B.1).</li>
     *   <li><b>Problem</b>: most pressing CULTIVATION/WORLD_EVENT memory.</li>
     *   <li><b>Concern</b>: most recent SOCIAL or PLAYER_ACTION memory.</li>
     *   <li><b>Opportunity</b>: most recent RUMOR or positive WORLD_EVENT.</li>
     *   <li><b>Danger</b>: most recent COMBAT or negative EMOTIONAL.</li>
     *   <li><b>Mood</b>: from NpcCognitiveMemory emotional state.</li>
     * </ol>
     *
     * @return fresh snapshot, or null if no cognitive data exists
     */
    @Nullable
    public MonologueSnapshot generate(String npcId, long currentTick,
                                        NpcCognitiveMemory memory) {
        // Get memory store
        List<NpcCognitiveMemory.MemoryEntry> shortTerm = List.of();
        List<NpcCognitiveMemory.MemoryEntry> mediumTerm = List.of();
        double emotionalState = 0.0;
        NpcCognitiveMemory.NpcMemoryStore store =
                memory.getStore(npcId);
        if (store != null) {
            shortTerm = store.getShortTerm();
            mediumTerm = store.getMediumTerm();
            emotionalState = store.computeEmotionalState(currentTick);
        }

        // 1. Objective from Priority Queue (B.1)
        String objective = "";
        NpcGoalQueue.NpcGoal activeGoal =
                NpcGoalTickHandler.getActiveGoal(npcId);
        if (activeGoal != null) {
            objective = activeGoal.description;
        }

        // 2. Problem: CULTIVATION or WORLD_EVENT memory
        String problem = findMostRecentIn(shortTerm, mediumTerm,
                NpcCognitiveMemory.MemoryCategory.CULTIVATION,
                NpcCognitiveMemory.MemoryCategory.WORLD_EVENT);
        if (problem.isEmpty()) {
            problem = findMostRecentIn(shortTerm, mediumTerm,
                    NpcCognitiveMemory.MemoryCategory.OBSERVATION);
        }

        // 3. Concern: SOCIAL or PLAYER_ACTION
        String concern = findMostRecentIn(shortTerm, mediumTerm,
                NpcCognitiveMemory.MemoryCategory.SOCIAL);
        if (concern.isEmpty()) {
            concern = findMostRecentIn(shortTerm, mediumTerm,
                    NpcCognitiveMemory.MemoryCategory.PLAYER_ACTION);
        }

        // 4. Opportunity: RUMOR, or positive WORLD_EVENT
        String opportunity = findMostRecentIn(shortTerm, mediumTerm,
                NpcCognitiveMemory.MemoryCategory.RUMOR);
        if (opportunity.isEmpty()) {
            for (int i = shortTerm.size() - 1; i >= 0; i--) {
                NpcCognitiveMemory.MemoryEntry e = shortTerm.get(i);
                if (e.category
                        == NpcCognitiveMemory.MemoryCategory.WORLD_EVENT
                        && e.emotionalWeight > 0) {
                    opportunity = e.description;
                    break;
                }
            }
        }

        // 5. Danger: COMBAT or negative EMOTIONAL
        String danger = findMostRecentIn(shortTerm, mediumTerm,
                NpcCognitiveMemory.MemoryCategory.COMBAT);
        if (danger.isEmpty()) {
            for (int i = shortTerm.size() - 1; i >= 0; i--) {
                NpcCognitiveMemory.MemoryEntry e = shortTerm.get(i);
                if (e.category
                        == NpcCognitiveMemory.MemoryCategory.EMOTIONAL
                        && e.emotionalWeight < -0.5) {
                    danger = e.description;
                    break;
                }
            }
        }

        // 6. Mood label
        String moodLabel = moodToLabel(emotionalState);

        // Only produce a snapshot if we have at least one field
        if (objective.isEmpty() && problem.isEmpty() && concern.isEmpty()
                && opportunity.isEmpty() && danger.isEmpty()) {
            return null;
        }

        MonologueSnapshot snapshot = new MonologueSnapshot(
                objective, problem, concern, opportunity, danger,
                moodLabel, currentTick);

        monologues.put(npcId, snapshot);
        trackedNpcIds.add(npcId);

        // Cap
        while (monologues.size() > MAX_TRACKED) {
            evictOldest();
        }

        return snapshot;
    }

    /**
     * Generate monologues for all tracked NPCs that have goal stacks.
     * Called periodically by the tick handler.
     *
     * @param queue       the NpcGoalQueue (to discover active NPCs)
     * @param currentTick the current game time
     * @return number of monologues generated
     */
    public int generateAll(NpcGoalQueue queue,
                                 NpcCognitiveMemory memory,
                                 long currentTick) {
        int count = 0;
        for (String npcId : trackedNpcIds) {
            NpcGoalQueue.NpcGoalStack stack = queue.getStack(npcId);
            if (stack != null && stack.activeGoalCount() > 0) {
                if (generate(npcId, currentTick, memory) != null) count++;
            }
        }
        return count;
    }

    /**
     * Register an NPC for monologue tracking (call when an NPC
     * first gets a goal or memory).
     */
    public void trackNpc(String npcId) {
        trackedNpcIds.add(npcId);
    }

    /** Get current monologue for an NPC, or null. */
    @Nullable
    public MonologueSnapshot getSnapshot(String npcId) {
        return monologues.get(npcId);
    }

    /** Remove a monologue (e.g., NPC unloaded). */
    public void remove(String npcId) {
        monologues.remove(npcId);
        trackedNpcIds.remove(npcId);
    }

    /** Clear all (e.g., level unload). */
    public void clear() {
        monologues.clear();
        trackedNpcIds.clear();
    }

    /** Status report for logging. */
    public String getStatusReport() {
        return String.format("%d monologues, %d tracked NPCs",
                monologues.size(), trackedNpcIds.size());
    }

    // ================================================================
    //  Internal helpers
    // ================================================================

    private static String findMostRecentIn(
            List<NpcCognitiveMemory.MemoryEntry> shortTerm,
            List<NpcCognitiveMemory.MemoryEntry> mediumTerm,
            NpcCognitiveMemory.MemoryCategory... categories) {
        Set<String> catNames = new HashSet<>();
        for (NpcCognitiveMemory.MemoryCategory cat : categories) {
            catNames.add(cat.name());
        }
        for (int i = shortTerm.size() - 1; i >= 0; i--) {
            if (catNames.contains(shortTerm.get(i).category.name()))
                return shortTerm.get(i).description;
        }
        for (int i = mediumTerm.size() - 1; i >= 0; i--) {
            if (catNames.contains(mediumTerm.get(i).category.name()))
                return mediumTerm.get(i).description;
        }
        return "";
    }

    static String moodToLabel(double emotionalState) {
        if (emotionalState > 0.8) return "Elated";
        if (emotionalState > 0.5) return "Pleased";
        if (emotionalState > 0.2) return "Content";
        if (emotionalState > -0.2) return "Calm";
        if (emotionalState > -0.5) return "Uneasy";
        if (emotionalState > -0.8) return "Anxious";
        return "Distressed";
    }

    private void evictOldest() {
        String oldestId = null;
        long oldestTick = Long.MAX_VALUE;
        for (Map.Entry<String, MonologueSnapshot> e
                : monologues.entrySet()) {
            if (e.getValue().generatedTick < oldestTick) {
                oldestTick = e.getValue().generatedTick;
                oldestId = e.getKey();
            }
        }
        if (oldestId != null) {
            monologues.remove(oldestId);
            trackedNpcIds.remove(oldestId);
        }
    }

    private static String orEmpty(String s) {
        return s != null ? s : "";
    }
}