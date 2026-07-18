package dev.ergenverse.simulation.opportunity;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * OpportunityEngineEvents — the Forge server-side event listener that
 * drives the {@link OpportunityRegistry}.
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 * No edits to {@code Ergenverse.java} are required — Forge discovers this
 * class automatically.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>{@link LevelEvent.Load}</b> — loads the {@link OpportunityRegistry}
 *       from world storage. The {@link SavedData} machinery handles the
 *       actual load; we just trigger the get-or-create call. (The registry
 *       is lazy-loaded on first {@link OpportunityRegistry#get(ServerLevel)}
 *       call, so this is mostly a logging hook.)</li>
 *   <li><b>{@link TickEvent.ServerTickEvent} (phase=END)</b> — calls
 *       {@link OpportunityRegistry#tickAll} every {@link #TICK_INTERVAL}
 *       ticks (100 ticks = 5 seconds for testing cadence). Production
 *       would tick less frequently (e.g. every 600 ticks = 30 sec).</li>
 *   <li><b>{@link LevelEvent.Unload}</b> — saves the registry. (The
 *       {@link SavedData} machinery auto-saves on world save, so this is
 *       mostly a logging hook.)</li>
 *   <li><b>{@link PlayerEvent.PlayerLoggedInEvent}</b> — if no opportunities
 *       exist near the player's spawn, generates 1-2 dormant spirit fruit
 *       opportunities for the player to potentially discover. This is a
 *       SOFT world-seeding — once the world simulation is fully built
 *       (Phase B), natural opportunity generation will replace this.</li>
 * </ul>
 *
 * <h2>Why 100-tick cadence for testing</h2>
 * <p>The default RESOLVED→HISTORICAL cooldown is 6000 ticks (5 minutes).
 * Ticking every 100 ticks (5 sec) means the FSM gets 60 advance calls
 * during the cooldown, which is plenty of resolution to observe the
 * transition. Production would tick less often (less server load) and
 * have a much longer cooldown (months of in-world time before recent
 * events become history).
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code simulation/opportunity/}. All
 *   integration with other subsystems will be done by other subagents
 *   later. This file is the ONLY integration point — a Forge event
 *   subscriber that auto-registers via {@link Mod.EventBusSubscriber}.
 * </blockquote>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class OpportunityEngineEvents {

    private OpportunityEngineEvents() {}

    /** Tick interval for the opportunity engine (100 ticks = 5 sec at 20 TPS). */
    public static final int TICK_INTERVAL = 100;

    /** How far from the player's spawn to seed spirit-fruit opportunities. */
    public static final int SEED_RADIUS_MIN = 32;
    public static final int SEED_RADIUS_MAX = 128;

    /** Cached registry reference — re-resolved on level load. */
    private static volatile OpportunityRegistry cachedRegistry = null;

    // ═══════════════════════════════════════════════════════════════════
    //  Level load / unload
    // ═══════════════════════════════════════════════════════════════════

    /**
     * On level load, pre-fetch the registry so the first tick is fast.
     * <p>{@link SavedData} handles the actual persistence; we just trigger
     * the get-or-create call and log the result.
     */
    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        // Only the overworld carries the opportunity registry.
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        cachedRegistry = OpportunityRegistry.get(level);
        Ergenverse.LOGGER.info("[OpportunityEngine] Level loaded. Registry: {}",
            cachedRegistry.getStatusReport());
    }

    /**
     * On level unload, drop the cached registry reference. The SavedData
     * machinery has already persisted it on the last autosave.
     */
    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        Ergenverse.LOGGER.info("[OpportunityEngine] Level unloading. Final registry state: {}",
            cachedRegistry != null ? cachedRegistry.getStatusReport() : "(no registry)");
        cachedRegistry = null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Server tick — drives the registry every TICK_INTERVAL ticks
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Every {@link #TICK_INTERVAL} server ticks, advance all opportunities
     * by one FSM step. Runs at the END phase (after the world has ticked).
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % TICK_INTERVAL != 0) return;

        OpportunityRegistry registry = cachedRegistry != null
            ? cachedRegistry
            : (cachedRegistry = OpportunityRegistry.get(overworld));
        if (registry == null) return;

        registry.tickAll(overworld, ticks);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Player login — seed initial opportunities near spawn
    // ═══════════════════════════════════════════════════════════════════

    /**
     * On player login, if no opportunities exist near the player's spawn,
     * generate 1-2 dormant spirit fruit opportunities for the player to
     * potentially discover.
     *
     * <p>This is a SOFT world-seeding — once the world simulation is
     * fully built (Phase B), natural opportunity generation will replace
     * this. For now, it ensures a fresh world has SOMETHING for a curious
     * player to find.
     *
     * <p>Per §5.4: "The world does NOT spawn a 'quest.' It simulates
     * consequences." This seeding respects that — the spirit fruit is
     * registered as DORMANT, and the timeline + FSM advance it naturally.
     * The player must find it by being curious (following the unusual
     * deer migration, sensing spiritual fluctuations, etc.).
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (cachedRegistry == null) {
            cachedRegistry = OpportunityRegistry.get(player.serverLevel());
        }
        if (cachedRegistry == null) return;

        // Delay by 60 ticks (3 sec) so the player has time to load.
        player.server.tell(new net.minecraft.server.TickTask(
            player.server.getTickCount() + 60,
            () -> seedInitialOpportunitiesIfEmpty(player, cachedRegistry)
        ));
    }

    /**
     * If no live opportunities exist near the player, seed 1-2 dormant
     * spirit fruit opportunities.
     */
    private static void seedInitialOpportunitiesIfEmpty(ServerPlayer player,
                                                         OpportunityRegistry registry) {
        BlockPos playerPos = player.blockPosition();
        // Check for existing live opportunities within 256 blocks.
        List<OpportunityState> existing = registry.getLiveOpportunitiesNear(playerPos, 256.0);
        if (!existing.isEmpty()) {
            Ergenverse.LOGGER.debug(
                "[OpportunityEngine] Player {} found {} existing live opportunities near spawn — no seeding needed.",
                player.getName().getString(), existing.size());
            return;
        }

        // Seed 1-2 dormant spirit fruit opportunities at random positions
        // near the player (within SEED_RADIUS_MIN..SEED_RADIUS_MAX).
        ServerLevel level = player.serverLevel();
        int count = 1 + level.getRandom().nextInt(2);  // 1 or 2
        long currentTick = level.getGameTime();

        for (int i = 0; i < count; i++) {
            // Random offset in a ring around the player.
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0;
            int dist = SEED_RADIUS_MIN + level.getRandom().nextInt(SEED_RADIUS_MAX - SEED_RADIUS_MIN);
            int dx = (int) (Math.cos(angle) * dist);
            int dz = (int) (Math.sin(angle) * dist);
            BlockPos gladePos = new BlockPos(
                playerPos.getX() + dx,
                playerPos.getY(),  // keep at player Y for visibility (driver could pick a better Y later)
                playerPos.getZ() + dz);

            // Create the spirit fruit timeline at this position. The fruit
            // ripened NOW (currentTick), so the timeline starts at t=0.
            // The FSM will transition DORMANT → FORMING on the next tick
            // (isPerceptible returns true).
            SpiritFruitTimeline timeline = new SpiritFruitTimeline(gladePos, currentTick);
            OpportunityState state = timeline.toOpportunityState();
            registry.register(state);

            Ergenverse.LOGGER.info(
                "[OpportunityEngine] Seeded spirit fruit opportunity {} at {} for player {}",
                state.getOpportunityId(), gladePos, player.getName().getString());
        }
    }
}
