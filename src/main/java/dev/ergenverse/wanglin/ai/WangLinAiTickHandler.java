package dev.ergenverse.wanglin.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.expectation.NpcExpectationNetwork;
import dev.ergenverse.npc.expectation.NpcExpectationTickHandler;
import dev.ergenverse.npc.goals.NpcGoalTickHandler;
import dev.ergenverse.npc.monologue.NpcInternalMonologue;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

/**
 * WangLinAiTickHandler — Forge event subscriber that drives Wang Lin's
 * integration with the generic NPC cognitive simulation (Phase B.9).
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 * No edits to {@code Ergenverse.java} required.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> — bootstraps Wang Lin into all cognitive
 *       systems via {@link WangLinCognitiveBridge#bootstrap}.</li>
 *   <li><b>LevelEvent.Unload</b> — resets bootstrap flag.</li>
 *   <li><b>ServerTickEvent (END)</b> — every {@link #SYNC_INTERVAL} ticks
 *       (2400 = 2 min), syncs habit-driven memories via
 *       {@link WangLinCognitiveBridge#syncHabitMemories}.</li>
 *   <li><b>PlayerInteractEvent.EntityInteract (HIGHEST)</b> — when a
 *       player right-clicks Wang Lin's EntityCultivator, seeds expectation
 *       tracking (B.6) with canon-weighted predictions. This runs at
 *       HIGHEST priority so it fires BEFORE the generic NpcDialogueTickHandler.</li>
 * </ul>
 *
 * <h2>Wang Lin's special expectation seeding</h2>
 * <p>Unlike generic NPCs (who get 0.15 baseline for 3 predictions), Wang Lin
 * starts with canon-grounded prediction biases:
 * <ul>
 *   <li><b>trustworthy: 0.05</b> — Wang Lin trusts almost no one initially.
 *       Trust must be earned through consistent behavior.</li>
 *   <li><b>hostile_intent: 0.10</b> — his EXTREME_CAUTION trait means he
 *       assumes potential hostility until proven otherwise.</li>
 *   <li><b>dangerous_if_provoked: 0.20</b> — he knows his own power and
 *       assumes others have hidden strength too.</li>
 *   <li><b>combat_able: 0.15</b> — he respects demonstrated ability.</li>
 * </ul>
 * These are NOT high enough to trigger hostile dialogue (which requires
 * hostile >= 0.6 in the dialogue system), but they bias inference.
 *
 * <h2>Player-relationship memory seeding</h2>
 * <p>On first interaction, Wang Lin records an OBSERVATION memory about
 * the player. Subsequent interactions generate PLAYER_ACTION memories.
 * This is in addition to the generic expectation seeding.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   This handler lives in {@code wanglin/ai/} and may READ from any
 *   {@code npc/} package. It WRITES to npc/ systems only through the
 *   WangLinCognitiveBridge (which is the single write boundary).
 * </blockquote>
 *
 * @see WangLinCognitiveBridge  the personality-to-cognitive write boundary
 * @see WangLinPersonality       the canon personality model
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WangLinAiTickHandler {

    private WangLinAiTickHandler() {}

    /**
     * How often to sync habit-driven memories (in server ticks).
     * 2400 ticks = 2 minutes. Less frequent than generic systems
     * because Wang Lin's habits are persistent, not reactive.
     */
    public static final int SYNC_INTERVAL = 2400;

    /** Track which players Wang Lin has met (to seed expectations once). */
    private static final java.util.Set<UUID> metPlayers =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    // ================================================================
    //  Level lifecycle
    // ================================================================

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;

        // Reset bootstrap so re-load re-initializes
        WangLinCognitiveBridge.resetBootstrap();
        metPlayers.clear();

        // Defer bootstrap by 1 tick to ensure all SavedData systems
        // are loaded (NpcGoalQueue, NpcCognitiveMemory, etc.)
        // We use a scheduled tick instead — but since we can't easily
        // schedule from a static event, we just bootstrap immediately.
        // The SavedData computeIfAbsent pattern handles lazy creation.
        if (WangLinPersonality.isLoaded()) {
            WangLinCognitiveBridge.bootstrap(level);
            Ergenverse.LOGGER.info("[WangLinAI] Level loaded. "
                    + "Full cognitive stack active for Wang Lin.");
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;

        WangLinCognitiveBridge.resetBootstrap();
        metPlayers.clear();
        Ergenverse.LOGGER.info("[WangLinAI] Level unloading. Cognitive bridge reset.");
    }

    // ================================================================
    //  Periodic sync
    // ================================================================

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % SYNC_INTERVAL != 0) return;

        if (!WangLinCognitiveBridge.isBootstrapped()) {
            // Late bootstrap (e.g., level loaded before personality)
            if (WangLinPersonality.isLoaded()) {
                WangLinCognitiveBridge.bootstrap(overworld);
            }
            return;
        }

        int synced = WangLinCognitiveBridge.syncHabitMemories(overworld);
        if (synced > 0) {
            Ergenverse.LOGGER.debug(
                    "[WangLinAI] Habit sync: {} memories injected.", synced);
        }
    }

    // ================================================================
    //  Player interaction — seed expectations + record memory
    // ================================================================

    /**
     * When a player right-clicks Wang Lin's EntityCultivator, seed
     * expectation tracking with canon-weighted biases and record
     * an observation memory.
     *
     * <p>Runs at HIGHEST priority to fire BEFORE the generic
     * NpcDialogueTickHandler (which generates the dialogue).
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerInteract(
            PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        net.minecraft.world.entity.Entity target = event.getTarget();
        if (!(target instanceof dev.ergenverse.entity.EntityCultivator ec))
            return;

        String characterId = ec.getCharacterId();
        if (!WangLinCognitiveBridge.WANG_LIN_NPC_ID.equals(characterId))
            return;

        String playerUuid = player.getUUID().toString();
        long tick = player.serverLevel().getGameTime();
        boolean firstMeeting = !metPlayers.contains(player.getUUID());

        // 1. Seed expectations with Wang Lin's canon-weighted biases
        if (firstMeeting) {
            // Wang Lin's initial predictions are more cautious than generic NPCs.
            // The seedPrediction signature is:
            //   seedPrediction(npcId, targetId, predId, desc, confidence, tick, evidence)
            NpcExpectationTickHandler.seedPrediction(
                    WangLinCognitiveBridge.WANG_LIN_NPC_ID,
                    playerUuid,
                    NpcExpectationNetwork.PRED_TRUSTWORTHY,
                    "Cautious — trust must be earned.", 0.05, tick,
                    "First meeting: " + player.getName().getString());
            NpcExpectationTickHandler.seedPrediction(
                    WangLinCognitiveBridge.WANG_LIN_NPC_ID,
                    playerUuid,
                    NpcExpectationNetwork.PRED_HOSTILE_INTENT,
                    "Assumes potential hostility until proven otherwise.",
                    0.10, tick,
                    "First meeting: " + player.getName().getString());
            NpcExpectationTickHandler.seedPrediction(
                    WangLinCognitiveBridge.WANG_LIN_NPC_ID,
                    playerUuid,
                    NpcExpectationNetwork.PRED_DANGEROUS_IF_PROVOKED,
                    "Everyone has hidden strength.", 0.20, tick,
                    "First meeting: " + player.getName().getString());
            NpcExpectationTickHandler.seedPrediction(
                    WangLinCognitiveBridge.WANG_LIN_NPC_ID,
                    playerUuid,
                    NpcExpectationNetwork.PRED_COMBAT_ABLE,
                    "Respects demonstrated ability.", 0.15, tick,
                    "First meeting: " + player.getName().getString());

            metPlayers.add(player.getUUID());

            Ergenverse.LOGGER.info(
                    "[WangLinAI] First meeting with player {}. "
                            + "Seeded cautious expectations (trust=0.05, "
                            + "hostile=0.10, danger=0.20, combat=0.15).",
                    player.getName().getString());
        }

        // 2. Record a PLAYER_ACTION memory about the interaction
        dev.ergenverse.npc.memory.NpcCognitiveMemory memory =
                dev.ergenverse.npc.memory.NpcCognitiveMemory.get(
                        player.serverLevel());
        if (memory != null) {
            String desc;
            if (firstMeeting) {
                desc = "A stranger approaches. I will observe before trusting.";
            } else {
                desc = "The cultivator returns. I note their demeanor and purpose.";
            }
            memory.recordShortTerm(
                    WangLinCognitiveBridge.WANG_LIN_NPC_ID,
                    dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryCategory
                            .PLAYER_ACTION,
                    desc, playerUuid, tick);
        }
    }

    // ================================================================
    //  Static query API
    // ================================================================

    /**
     * Get Wang Lin's enriched monologue (personality-driven).
     * Returns the generic monologue if personality is not loaded.
     */
    public static NpcInternalMonologue.MonologueSnapshot getWangLinMonologue(
            long currentTick) {
        return WangLinCognitiveBridge.getEnrichedMonologue(
                WangLinCognitiveBridge.WANG_LIN_NPC_ID, currentTick);
    }

    /**
     * Get Wang Lin's dominant speech pattern for a given player.
     * Used by dialogue systems.
     */
    public static String getWangLinSpeechPattern(String playerUuid) {
        return WangLinCognitiveBridge.getDominantSpeechPattern(playerUuid);
    }

    /**
     * Get Wang Lin's current Dao focus.
     */
    public static String getWangLinDaoFocus() {
        return WangLinCognitiveBridge.getDaoFocus();
    }

    /**
     * Check if Wang Lin has met a specific player.
     */
    public static boolean hasMetPlayer(UUID playerUuid) {
        return metPlayers.contains(playerUuid);
    }

    /**
     * Get the set of players Wang Lin has met.
     */
    public static java.util.Set<UUID> getMetPlayers() {
        return java.util.Collections.unmodifiableSet(metPlayers);
    }
}