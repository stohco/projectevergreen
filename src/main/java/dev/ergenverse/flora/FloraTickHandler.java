package dev.ergenverse.flora;

import dev.ergenverse.core.Ergenverse;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.server.level.ServerLevel;

/**
 * FloraTickHandler — Forge event subscriber for the FloraSystem.
 *
 * <p>Handles:
 * <ul>
 *   <li>{@link LevelEvent.Load} — loads the {@link FloraSpecies#REGISTRY} from
 *       the classpath JSON files. Idempotent.</li>
 *   <li>{@link LevelEvent.Unload} — clears the registry so it can be reloaded
 *       on next world load (e.g. on world switch in dev).</li>
 *   <li>{@link TickEvent.ServerTickEvent} (END, every 600 ticks / 30s) — a
 *       future hook for ecology simulation. Block entities handle their own
 *       per-tick growth; this loop is for cross-block ecology events
 *       (disease spread, pollinator migration, etc.) not yet implemented.</li>
 * </ul>
 *
 * <p>No interaction handler here — harvest is intercepted in
 * {@link SmallHerbBlock.BreakHandler} via {@link net.minecraftforge.event.level.BlockEvent.BreakEvent}.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class FloraTickHandler {

    private FloraTickHandler() {}

    /** Counter for the 600-tick ecology hook. */
    private static long tickCounter = 0L;

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        // Load species JSONs (idempotent — skips if already loaded).
        FloraSpecies.loadAll();
        Ergenverse.LOGGER.info("[Flora] Level loaded — FloraSpecies registry ready ({} species).",
                FloraSpecies.REGISTRY.size());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel)) return;
        // Clear registry so next world-load re-reads JSON from classpath.
        FloraSpecies.clear();
        Ergenverse.LOGGER.info("[Flora] Level unloaded — FloraSpecies registry cleared.");
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tickCounter++;
        // Every 600 ticks (30s): future ecology hook.
        if (tickCounter % 600L == 0L) {
            ecologyHook();
        }
    }

    /**
     * Future ecology hook — placeholder for cross-block flora events:
     * <ul>
     *   <li>Disease spread between adjacent plants of the same species</li>
     *   <li>Pollinator visit propagation (insects/beasts from ecologyAttracts)</li>
     *   <li>Seasonal dormancy triggers (winter → mass DORMANT transition)</li>
     *   <li>Karmic residue diffusion from nearby tribulation sites</li>
     * </ul>
     *
     * <p>Not implemented in v1 — block entities handle their own ticks.
     * This method is the seam where future ecology simulation plugs in.
     */
    private static void ecologyHook() {
        // No-op for v1. Block entities self-tick via SmallHerbBlock.getTicker().
        // Cross-block ecology (disease spread, pollinator propagation, seasonal
        // dormancy) will be implemented here in a later phase.
    }
}
