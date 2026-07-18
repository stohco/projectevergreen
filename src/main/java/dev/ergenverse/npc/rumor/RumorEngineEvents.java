package dev.ergenverse.npc.rumor;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * RumorEngineEvents — Forge server-side event subscriber that drives the
 * {@link RumorNetwork}.
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 * No edits to {@code Ergenverse.java} required.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> — pre-fetches the RumorNetwork singleton.</li>
 *   <li><b>ServerTickEvent (END)</b> — calls
 *       {@link RumorNetwork#tick(ServerLevel, long)} every
 *       {@link RumorNetwork#PROPAGATION_INTERVAL} ticks (600 = 30 sec).</li>
 *   <li><b>PlayerLoggedInEvent</b> — logs the current rumor state for
 *       debugging. In Phase B.8 (world-sim-tick), player login will also
 *       inject the most relevant active rumors into the player's HUD
 *       via a packet.</li>
 * </ul>
 *
 * <h2>Rumor source hooks</h2>
 * <p>This class also provides static methods for other systems to create
 * rumors. These are called by:
 * <ul>
 *   <li>OpportunityEngineEvents — when an opportunity transitions to FORMING
 *       (spirit fruit becoming perceptible → mortals notice strange lights)</li>
 *   <li>HistoryEvents — when a combat event occurs near NPCs</li>
 *   <li>Future: WorldSimTick — NPC observations during AFK simulation</li>
 * </ul>
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/rumor/}. Integration with
 *   the Opportunity Engine and Expectation Model will be done by other
 *   build steps. This file is the ONLY integration point — a Forge event
 *   subscriber that auto-registers via {@link Mod.EventBusSubscriber}.
 * </blockquote>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class RumorEngineEvents {

    private RumorEngineEvents() {}

    /** Cached network reference — re-resolved on level load. */
    private static volatile RumorNetwork cachedNetwork = null;

    // ═══════════════════════════════════════════════════════════════════
    //  Level load / unload
    // ═══════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        cachedNetwork = RumorNetwork.get(level);
        Ergenverse.LOGGER.info("[RumorEngine] Level loaded. Network: {}",
                cachedNetwork.getStatusReport());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[RumorEngine] Level unloading. Final state: {}",
                cachedNetwork != null ? cachedNetwork.getStatusReport() : "(no network)");
        cachedNetwork = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Server tick — drives propagation every PROPAGATION_INTERVAL ticks
    // ═══════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % RumorNetwork.PROPAGATION_INTERVAL != 0) return;

        RumorNetwork network = cachedNetwork != null
                ? cachedNetwork
                : (cachedNetwork = RumorNetwork.get(overworld));
        if (network == null) return;

        network.tick(overworld, ticks);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Player login — future: inject rumors into player HUD
    // ═══════════════════════════════════════════════════════════════════

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (cachedNetwork == null) {
            cachedNetwork = RumorNetwork.get(player.serverLevel());
        }
        if (cachedNetwork == null) return;

        Ergenverse.LOGGER.debug("[RumorEngine] Player {} logged in. Rumor network: {}",
                player.getName().getString(), cachedNetwork.getStatusReport());

        // Phase B.8: Send most relevant rumors to player via packet.
        // For now, just log. The player "hears" rumors through NPC dialogue
        // which is built in Phase B.7 (dialogue from thoughts).
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Public API — rumor creation hooks for other systems
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Create a rumor from an opportunity event (e.g., spirit fruit becoming
     * perceptible). Called by OpportunityEngineEvents when an opportunity
     * transitions DORMANT → FORMING.
     *
     * <p>Per 6.11 worked example: "A spirit fruit ripened in Mosquito Valley"
     * → mortals hear "Strange lights in Mosquito Valley" (hop 1).
     *
     * @param content      the original event description
     * @param locationHint the specific location (e.g. "Mosquito Valley")
     * @param tick         the current game time
     */
    public static void createOpportunityRumor(String content, String locationHint, long tick) {
        RumorNetwork network = getNetwork();
        if (network == null) return;
        network.createRumor(content, "", Rumor.OriginType.SPIRIT_EVENT, locationHint, tick);
    }

    /**
     * Create a rumor from a combat event witnessed by NPCs.
     * Called by HistoryEvents when combat occurs near an NPC.
     *
     * @param content      what was witnessed (e.g. "Two cultivators fought near the valley")
     * @param witnessNpcId the NPC who witnessed it
     * @param locationHint where it happened
     * @param tick         the current game time
     */
    public static void createCombatRumor(String content, String witnessNpcId,
                                          String locationHint, long tick) {
        RumorNetwork network = getNetwork();
        if (network == null) return;
        network.createRumor(content, witnessNpcId, Rumor.OriginType.COMBAT_EVENT, locationHint, tick);
    }

    /**
     * Create a rumor from a player action witnessed by NPCs.
     * Called by ExpectationModelObserver or HistoryEvents.
     *
     * @param content      what the player did (e.g. "A cultivator was seen studying formations")
     * @param witnessNpcId the NPC who witnessed it
     * @param locationHint where it happened
     * @param tick         the current game time
     */
    public static void createPlayerActionRumor(String content, String witnessNpcId,
                                                String locationHint, long tick) {
        RumorNetwork network = getNetwork();
        if (network == null) return;
        network.createRumor(content, witnessNpcId, Rumor.OriginType.PLAYER_ACTION, locationHint, tick);
    }

    /**
     * Create a rumor from an environmental event.
     *
     * @param content      what happened (e.g. "Beasts are migrating away from the eastern mountains")
     * @param locationHint where
     * @param tick         the current game time
     */
    public static void createEnvironmentalRumor(String content, String locationHint, long tick) {
        RumorNetwork network = getNetwork();
        if (network == null) return;
        network.createRumor(content, "", Rumor.OriginType.ENVIRONMENTAL, locationHint, tick);
    }

    /**
     * Get rumors known by a specific NPC. Used by future cognitive systems
     * to feed distorted information into NPC decision-making.
     *
     * @param npcId the NPC's canon or graph ID
     * @return list of rumors this NPC has heard (may be distorted)
     */
    public static List<Rumor> getRumorsForNpc(String npcId) {
        RumorNetwork network = getNetwork();
        if (network == null) return List.of();
        return network.getRumorsKnownBy(npcId);
    }

    /**
     * Get the most distorted version of a rumor (forNPC consumption).
     * NPCs that are far from the source get the most distorted version.
     *
     * @param originalContent the original (true) event description
     * @return the most distorted version, or null
     */
    public static Rumor getMostDistorted(String originalContent) {
        RumorNetwork network = getNetwork();
        if (network == null) return null;
        return network.getMostDistortedVersion(originalContent);
    }

    // ─── Internal ──────────────────────────────────────────────────

    private static RumorNetwork getNetwork() {
        // This is called from event handlers or static hooks.
        // If called outside of a tick context, we can't get the network
        // without a ServerLevel reference. Return null and let callers handle it.
        return cachedNetwork;
    }
}