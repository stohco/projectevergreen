package dev.ergenverse.npc.dialogue;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.npc.expectation.NpcExpectationTickHandler;
import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.goals.NpcGoalTickHandler;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.monologue.NpcInternalMonologue;
import dev.ergenverse.npc.monologue.NpcMonologueTickHandler;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

/**
 * NpcDialogueTickHandler — Forge event subscriber that wires thought-driven
 * dialogue generation into NPC interactions (PROJECT_MASTER.md 6.7).
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>PlayerInteractEvent.EntityInteract</b> — when a player
 *       right-clicks an EntityCultivator, assembles a
 *       {@link NpcDialogueGenerator.NpcDialogueContext} from B.1-B.6
 *       subsystems and generates a line of dialogue via
 *       {@link NpcDialogueGenerator#generate}. The dialogue is stored
 *       in a per-NPC cache and sent to the player.</li>
 *   <li><b>LevelEvent.Load/Unload</b> — clears the dialogue cache.</li>
 * </ul>
 *
 * <h2>How dialogue reaches the player</h2>
 * <p>v1: The generated dialogue is stored in a cache and can be retrieved
 * via the static API ({@link #getLastDialogue(String)}). The
 * {@link EntityCultivator} interaction handler (v2 hook, noted in its
 * Javadoc) or a future packet system can send this to the client for
 * display. For now, the dialogue is logged server-side and available
 * to any system that wants to display it.
 *
 * <h2>Context assembly</h2>
 * <p>On each player-NPC interaction, the handler reads:
 * <ol>
 *   <li><b>B.5 Monologue</b>: {@link NpcMonologueTickHandler#getMonologue(npcId)}</li>
 *   <li><b>B.6 Expectations</b>: trust/hostile/danger confidence about the player</li>
 *   <li><b>B.1 Goals</b>: active goal type (to detect urgency) + decision style</li>
 *   <li><b>B.2 Memory</b>: interaction count (social memories about player)</li>
 * </ol>
 * All reads are cross-package queries — this handler does NOT modify
 * any external system.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/dialogue/}. Integration
 *   with other Phase B systems is read-only (queries existing APIs).
 * </blockquote>
 *
 * @see NpcDialogueGenerator  the stateless thought-to-dialogue engine
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcDialogueTickHandler {

    private NpcDialogueTickHandler() {}

    /** Cached last-generated dialogue per NPC. Cleared on level unload. */
    private static final java.util.concurrent.ConcurrentHashMap<String,
            NpcDialogueGenerator.DialogueLine> lastDialogue =
            new java.util.concurrent.ConcurrentHashMap<>();

    // ─── Level lifecycle ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;
        lastDialogue.clear();
        Ergenverse.LOGGER.info("[NpcDialogue] Level loaded. Dialogue system ready.");
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[NpcDialogue] Level unloading. {} cached dialogues cleared.",
                lastDialogue.size());
        lastDialogue.clear();
    }

    // ─── Player interaction: generate dialogue ──────────────────────

    /**
     * When a player right-clicks an EntityCultivator, generate dialogue
     * from the NPC's cognitive state.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        if (!(target instanceof EntityCultivator cultivator)) return;

        String npcId = cultivator.getCharacterId();
        if (npcId == null || npcId.isEmpty()) return;

        ServerLevel serverLevel = player.serverLevel();
        NpcDialogueGenerator.DialogueLine line = generateDialogue(
                npcId, player.getUUID().toString(), serverLevel);

        if (line != null) {
            lastDialogue.put(npcId, line);
            // Send to player as action bar message (above hotbar).
            // This is v1 — a future packet system can render this
            // in a custom UI with more formatting.
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "<" + cultivator.getDisplayNameCn() + "> " + line.text),
                    true); // action bar
            Ergenverse.LOGGER.debug("[NpcDialogue] {} -> player {}: [{}] \"{}\" ({})",
                    npcId, player.getName().getString(),
                    line.tone, line.text, line.thoughtSummary);
        }
    }

    // ─── Context assembly ────────────────────────────────────────────

    /**
     * Assemble a full dialogue context from all cognitive subsystems
     * and generate a line.
     */
    @Nullable
    public static NpcDialogueGenerator.DialogueLine generateDialogue(
            String npcId, String playerUuid, ServerLevel serverLevel) {

        long currentTick = serverLevel.getGameTime();

        // 1. Monologue (B.5)
        NpcInternalMonologue.MonologueSnapshot monologue =
                NpcMonologueTickHandler.getMonologue(npcId);

        // 2. Expectation Model (B.6)
        double trust = NpcExpectationTickHandler.getConfidence(
                npcId, playerUuid, "trustworthy");
        double hostile = NpcExpectationTickHandler.getConfidence(
                npcId, playerUuid, "hostile_intent");
        double danger = NpcExpectationTickHandler.getConfidence(
                npcId, playerUuid, "dangerous_if_provoked");

        // 3. Goal Queue (B.1) — decision style + urgent goal detection
        NpcGoalQueue.DecisionStyle style =
                NpcGoalTickHandler.getDecisionStyle(npcId);
        NpcGoalQueue.NpcGoal activeGoal =
                NpcGoalTickHandler.getActiveGoal(npcId);
        boolean hasUrgentGoal = false;
        if (activeGoal != null) {
            hasUrgentGoal = (activeGoal.type == NpcGoalQueue.GoalType.DEFEND)
                    || (activeGoal.type == NpcGoalQueue.GoalType.ATTACK)
                    || (activeGoal.type == NpcGoalQueue.GoalType.FLEE);
        }

        // 4. Memory (B.2) — count social interactions with this player
        int interactionCount = 0;
        NpcCognitiveMemory memory = NpcCognitiveMemory.get(serverLevel);
        String recentWorldEvent = null;
        String recentObservation = null;
        boolean hasRecentWorldMemory = false;

        if (memory != null) {
            NpcCognitiveMemory.NpcMemoryStore store = memory.getStore(npcId);

            // 4a. Count social interactions with this player
            if (store != null) {
                List<NpcCognitiveMemory.MemoryEntry> socialMemories =
                        store.aboutTarget(playerUuid);
                for (NpcCognitiveMemory.MemoryEntry e : socialMemories) {
                    if (e.category == NpcCognitiveMemory.MemoryCategory.SOCIAL
                            || e.category == NpcCognitiveMemory.MemoryCategory.PLAYER_ACTION) {
                        interactionCount++;
                    }
                }

                // 4b. Art XXXI.5: Fetch most recent WORLD_EVENT and OBSERVATION
                // memories for memory-driven dialogue. These let NPCs reference
                // what they've perceived — the Memory Metric's expression half.
                // Only look at medium-term (14 days) and short-term (3 days).
                long sevenDaysTicks = 7L * 24000L;
                String newestWorldDesc = null;
                long newestWorldTs = 0;
                String newestObsDesc = null;
                long newestObsTs = 0;

                for (NpcCognitiveMemory.MemoryEntry e : store.getMediumTerm()) {
                    long age = currentTick - e.timestamp;
                    if (age > sevenDaysTicks || age < 0) continue;
                    if (e.category == NpcCognitiveMemory.MemoryCategory.WORLD_EVENT
                            && e.timestamp > newestWorldTs) {
                        newestWorldTs = e.timestamp;
                        newestWorldDesc = e.description;
                    }
                    if (e.category == NpcCognitiveMemory.MemoryCategory.OBSERVATION
                            && e.timestamp > newestObsTs) {
                        newestObsTs = e.timestamp;
                        newestObsDesc = e.description;
                    }
                }
                for (NpcCognitiveMemory.MemoryEntry e : store.getShortTerm()) {
                    long age = currentTick - e.timestamp;
                    if (age > sevenDaysTicks || age < 0) continue;
                    if (e.category == NpcCognitiveMemory.MemoryCategory.WORLD_EVENT
                            && e.timestamp > newestWorldTs) {
                        newestWorldTs = e.timestamp;
                        newestWorldDesc = e.description;
                    }
                    if (e.category == NpcCognitiveMemory.MemoryCategory.OBSERVATION
                            && e.timestamp > newestObsTs) {
                        newestObsTs = e.timestamp;
                        newestObsDesc = e.description;
                    }
                }

                recentWorldEvent = newestWorldDesc;
                recentObservation = newestObsDesc;
                hasRecentWorldMemory = (newestWorldDesc != null);
            }
        }

        // Assemble context
        NpcDialogueGenerator.NpcDialogueContext ctx =
                new NpcDialogueGenerator.NpcDialogueContext(
                        npcId, playerUuid, monologue,
                        trust, hostile, danger,
                        hasUrgentGoal, style, interactionCount,
                        recentWorldEvent, recentObservation, hasRecentWorldMemory);

        // Generate using world random seed
        long seed = serverLevel.getRandom().nextLong();
        return NpcDialogueGenerator.generate(ctx, seed);
    }

    // ================================================================
    //  Static convenience API
    // ================================================================

    /**
     * Get the last generated dialogue for an NPC.
     * Returns null if no dialogue has been generated yet.
     */
    @Nullable
    public static NpcDialogueGenerator.DialogueLine getLastDialogue(String npcId) {
        return lastDialogue.get(npcId);
    }

    /**
     * Get the last generated dialogue text for an NPC.
     * Returns empty string if no dialogue exists.
     */
    public static String getLastDialogueText(String npcId) {
        NpcDialogueGenerator.DialogueLine line = lastDialogue.get(npcId);
        return line != null ? line.text : "";
    }

    /**
     * Generate dialogue for a specific NPC-player pair without
     * caching (useful for systems that want to preview dialogue
     * without triggering an interaction).
     */
    @Nullable
    public static NpcDialogueGenerator.DialogueLine previewDialogue(
            String npcId, String playerUuid, ServerLevel serverLevel) {
        return generateDialogue(npcId, playerUuid, serverLevel);
    }
}