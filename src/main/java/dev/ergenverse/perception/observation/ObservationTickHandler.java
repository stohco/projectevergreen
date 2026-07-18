package dev.ergenverse.perception.observation;

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
 * ObservationTickHandler — Forge event wiring for the ObservationEngine.
 *
 * <p>On each server tick (throttled to every 400 ticks / 20s), scans the world
 * around each online player for phenomena, lets them perceive what they can,
 * and evaluates chains for emergent understanding.
 *
 * <p>On player login, loads their observation state from persistent NBT.
 * On logout, saves it. This follows the same pattern as
 * {@link dev.ergenverse.origin.ExplorationShortcutSystem} (player persistent
 * data under a namespaced key).
 *
 * <h2>Why this coexists with ExplorationShortcutSystem</h2>
 * <p>The shortcut system is the v1 trigger-reward layer (position + interaction
 * triggers). The ObservationEngine is the v2 emergent layer (world-state
 * phenomena + perception + chains). They coexist during transition: the
 * shortcut system handles explicit "right-click X" triggers, while the
 * observation engine handles ambient "the world is being itself" perception.
 * A future refactor may fold the shortcut triggers into observation chains.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ObservationTickHandler {

    private ObservationTickHandler() {}

    /** Scan interval — every 400 ticks (20 seconds). */
    private static final long SCAN_INTERVAL = 400L;
    /** Perception scan radius (blocks). */
    private static final int SCAN_RADIUS = 32;

    // ─── Level lifecycle ───────────────────────────────────────────────

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Nothing to preload — states are lazy-loaded per player
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        // States are per-player, cleared on logout — nothing to do here
    }

    // ─── Player lifecycle ──────────────────────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Force-load the player's state (caches it in the STATES map)
        ObservationEngine.getState(player);
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ObservationEngine.saveState(player);
        ObservationEngine.clearState(player.getUUID());
    }

    // ─── Server tick: scan + perceive + evaluate ───────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        net.minecraft.server.MinecraftServer server = event.getServer();
        if (server == null) return;
        long tick = server.getTickCount();
        if (tick % SCAN_INTERVAL != 0) return;

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (player.isSpectator()) continue;

            // Scan the world around the player for phenomena
            List<ObservationPhenomenon> phenomena =
                    ObservationEngine.scanForPhenomena(level, player.blockPosition(), SCAN_RADIUS);

            // Let the player perceive each one (records noticed phenomena)
            for (ObservationPhenomenon p : phenomena) {
                ObservationEngine.perceive(player, p);
            }

            // Evaluate chains — grant knowledge + emergent understanding
            ObservationEngine.evaluateChains(player);
        }
    }
}
