package dev.ergenverse.npc.memory;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * NpcMemoryTickHandler — Forge event subscriber that drives the NPC
 * cognitive memory decay cycle.
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 * No edits to {@code Ergenverse.java} required.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> — pre-fetches the NpcCognitiveMemory singleton.</li>
 *   <li><b>ServerTickEvent (END)</b> — calls
 *       {@link NpcCognitiveMemory#decayTick(long)} every
 *       {@link NpcCognitiveMemory#DECAY_INTERVAL} ticks (1200 = 1 min).</li>
 * </ul>
 *
 * <h2>Static convenience methods</h2>
 * <p>Provides static methods for other systems to record NPC memories
 * without needing to resolve the SavedData themselves:
 * <ul>
 *   <li>{@link #recordLongTerm(String, NpcCognitiveMemory.MemoryCategory, String, String)}</li>
 *   <li>{@link #recordObservation(String, String, String, long)}</li>
 *   <li>{@link #recordCombat(String, String, String, long)}</li>
 *   <li>{@link #recordPlayerAction(String, String, String, long)}</li>
 *   <li>{@link #recordEmotional(String, String, double, long)}</li>
 *   <li>{@link #recordRumorHeard(String, String, String, long)}</li>
 * </ul>
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/memory/}. Integration with
 *   other Phase B systems will be done by other build steps.
 * </blockquote>
 *
 * @see NpcCognitiveMemory  the 3-tier memory store + persistence
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcMemoryTickHandler {

    private NpcMemoryTickHandler() {}

    private static volatile NpcCognitiveMemory cachedMemory = null;

    // ═══════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        cachedMemory = NpcCognitiveMemory.get(level);
        Ergenverse.LOGGER.info("[NpcMemory] Level loaded. State: {}",
                cachedMemory.getStatusReport());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[NpcMemory] Level unloading. Final state: {}",
                cachedMemory != null ? cachedMemory.getStatusReport() : "(none)");
        cachedMemory = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % NpcCognitiveMemory.DECAY_INTERVAL != 0) return;

        NpcCognitiveMemory memory = cachedMemory != null
                ? cachedMemory
                : (cachedMemory = NpcCognitiveMemory.get(overworld));
        if (memory == null) return;

        int changes = memory.decayTick(ticks);
        if (changes > 0) {
            Ergenverse.LOGGER.debug("[NpcMemory] Decay cycle: {} memories moved/forgotten. {}",
                    changes, memory.getStatusReport());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Static convenience API for other systems
    // ═══════════════════════════════════════════════════════════════

    /**
     * Record a long-term (permanent) memory for an NPC.
     * Use for life events: killed my son, saved my life, joined sect.
     */
    public static void recordLongTerm(String npcId,
                                       NpcCognitiveMemory.MemoryCategory category,
                                       String description, String targetId) {
        NpcCognitiveMemory mem = resolve();
        if (mem != null) mem.recordLongTerm(npcId, category, description, targetId);
    }

    /**
     * Record a medium-term memory (weeks) for an NPC.
     * Use for recent interactions: talked, traded, borrowed.
     */
    public static void recordMediumTerm(String npcId,
                                         NpcCognitiveMemory.MemoryCategory category,
                                         String description, String targetId, long tick) {
        NpcCognitiveMemory mem = resolve();
        if (mem != null) mem.recordMediumTerm(npcId, category, description, targetId, tick);
    }

    /**
     * Record a short-term memory (hours-days) for an NPC.
     * Use for fleeting observations: passed by, asked directions.
     */
    public static void recordShortTerm(String npcId,
                                        NpcCognitiveMemory.MemoryCategory category,
                                        String description, String targetId, long tick) {
        NpcCognitiveMemory mem = resolve();
        if (mem != null) mem.recordShortTerm(npcId, category, description, targetId, tick);
    }

    /**
     * Record an observation memory (short-term by default).
     * Example: NPC saw the player studying formations.
     */
    public static void recordObservation(String npcId, String description,
                                          String targetId, long tick) {
        recordShortTerm(npcId, NpcCognitiveMemory.MemoryCategory.OBSERVATION,
                description, targetId, tick);
    }

    /**
     * Record a combat memory (medium-term — fights are memorable).
     * Example: NPC fought the player and lost.
     */
    public static void recordCombat(String npcId, String description,
                                     String targetId, long tick) {
        recordMediumTerm(npcId, NpcCognitiveMemory.MemoryCategory.COMBAT,
                description, targetId, tick);
    }

    /**
     * Record a player action memory (short-term).
     * Example: Player was seen near the sect treasury.
     */
    public static void recordPlayerAction(String npcId, String description,
                                           String playerUuid, long tick) {
        recordShortTerm(npcId, NpcCognitiveMemory.MemoryCategory.PLAYER_ACTION,
                description, playerUuid, tick);
    }

    /**
     * Record a social interaction memory (medium-term).
     * Example: Player gave NPC a gift.
     */
    public static void recordSocial(String npcId, String description,
                                     String targetId, long tick) {
        recordMediumTerm(npcId, NpcCognitiveMemory.MemoryCategory.SOCIAL,
                description, targetId, tick);
    }

    /**
     * Record a rumor heard by an NPC (short-term, linked to RumorNetwork).
     */
    public static void recordRumorHeard(String npcId, String rumorId,
                                          String description, long tick) {
        NpcCognitiveMemory mem = resolve();
        if (mem != null) mem.recordRumorHeard(npcId, rumorId, description, tick);
    }

    /**
     * Record an emotional memory for an NPC.
     * Emotional memories feed the NPC's mood computation.
     *
     * @param emotionalWeight -1.0 (extreme grief/rage) to +1.0 (extreme joy/gratitude)
     */
    public static void recordEmotional(String npcId, String description,
                                        double emotionalWeight, long tick) {
        NpcCognitiveMemory mem = resolve();
        if (mem != null) mem.recordEmotional(npcId, description, emotionalWeight, tick);
    }

    /**
     * Get the emotional state for an NPC (-1.0 to +1.0).
     * Used by the Internal Monologue system (B.5) to populate the mood field.
     */
    public static double getEmotionalState(String npcId, long currentTick) {
        NpcCognitiveMemory mem = resolve();
        if (mem == null) return 0.0;
        return mem.getEmotionalState(npcId, currentTick);
    }

    /**
     * Get all memories an NPC has about a specific target.
     * Used by the Expectation Model (B.6) to inform NPC predictions.
     */
    public static java.util.List<NpcCognitiveMemory.MemoryEntry> getMemoriesAbout(
            String npcId, String targetId) {
        NpcCognitiveMemory mem = resolve();
        if (mem == null) return java.util.List.of();
        return mem.getMemoriesAbout(npcId, targetId);
    }

    // ─── Internal ───────────────────────────────────────────────

    private static NpcCognitiveMemory resolve() {
        return cachedMemory; // null if level not yet loaded
    }
}