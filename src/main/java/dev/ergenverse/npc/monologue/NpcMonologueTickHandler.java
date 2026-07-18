package dev.ergenverse.npc.monologue;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * NpcMonologueTickHandler -- Forge event subscriber that drives the
 * NPC internal monologue generation cycle and provides a static
 * convenience API.
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> -- creates NpcInternalMonologue instance.</li>
 *   <li><b>LevelEvent.Unload</b> -- clears monologue cache.</li>
 *   <li><b>ServerTickEvent (END)</b> -- regenerates monologues every
 *       {@link NpcInternalMonologue#UPDATE_INTERVAL} ticks (200 = 10s).</li>
 * </ul>
 *
 * <h2>Static convenience API</h2>
 * <ul>
 *   <li>{@link #getMonologue(String)} -- get current snapshot.</li>
 *   <li>{@link #trackNpc(String)} -- register NPC for tracking.</li>
 *   <li>{@link #formatMonologue(String)} -- formatted string for debug.</li>
 * </ul>
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/monologue/}. Integration
 *   with other Phase B systems is read-only (queries existing APIs).
 * </blockquote>
 *
 * @see NpcInternalMonologue  the monologue generator + state
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcMonologueTickHandler {

    private NpcMonologueTickHandler() {}

    private static volatile NpcInternalMonologue monologue = null;

    // ================================================================

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;

        monologue = new NpcInternalMonologue();
        Ergenverse.LOGGER.info("[NpcMonologue] Level loaded. Monologue system ready.");
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(
                net.minecraft.world.level.Level.OVERWORLD)) return;
        if (monologue != null) {
            Ergenverse.LOGGER.info("[NpcMonologue] Level unloading. {}",
                    monologue.getStatusReport());
            monologue.clear();
        }
        monologue = null;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();
        if (ticks % NpcInternalMonologue.UPDATE_INTERVAL != 0) return;

        NpcInternalMonologue m = monologue;
        NpcGoalQueue queue;
        NpcCognitiveMemory memory;
        try {
            queue = NpcGoalQueue.get(overworld);
            memory = NpcCognitiveMemory.get(overworld);
        } catch (Exception e) {
            return;
        }
        if (m == null || queue == null || memory == null) return;

        int count = m.generateAll(queue, memory, ticks);
        if (count > 0) {
            Ergenverse.LOGGER.debug(
                    "[NpcMonologue] Generated {} monologues. {}",
                    count, m.getStatusReport());
        }
    }

    // ================================================================
    //  Static convenience API
    // ================================================================

    /**
     * Get the current monologue snapshot for an NPC.
     * Returns null if no monologue exists (NPC not tracked yet,
     * or no cognitive data to synthesize).
     */
    @Nullable
    public static NpcInternalMonologue.MonologueSnapshot getMonologue(
            String npcId) {
        return monologue != null ? monologue.getSnapshot(npcId) : null;
    }

    /**
     * Get the formatted monologue string for an NPC.
     * Returns empty string if no monologue exists.
     */
    public static String formatMonologue(String npcId) {
        NpcInternalMonologue.MonologueSnapshot snap = getMonologue(npcId);
        return snap != null ? snap.format() : "";
    }

    /**
     * Register an NPC for monologue tracking.
     * Call this when an NPC first gets a goal pushed (from B.1)
     * or a memory recorded (from B.2).
     */
    public static void trackNpc(String npcId) {
        if (monologue != null) monologue.trackNpc(npcId);
    }

    // --- Internal ---

    static NpcInternalMonologue resolve() {
        return monologue;
    }
}