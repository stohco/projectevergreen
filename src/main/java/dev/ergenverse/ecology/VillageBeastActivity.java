package dev.ergenverse.ecology;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.WorldEventBus;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;
import java.util.Random;

/**
 * VillageBeastActivity — fires periodic beast events on the WorldEventBus
 * near locations where NPCs are active.
 *
 * <p>This is the EVENT SOURCE that makes Canon Experience #1 possible:
 * without beast events, the ActivityInterruptionSubscriber has nothing
 * to interrupt, and cautious cultivators never observe predators.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> This is a static utility method
 * called from {@code Ergenverse.onServerTick}. It uses the existing
 * WorldEventBus (no new bus), existing ActorRegistry (no new registry),
 * and existing subscriber pattern. It is data-driven: the event interval,
 * topic, intensity, and radius are all constants that could be moved to
 * a JSON config. No new infrastructure.
 *
 * <p><b>Canon basis (INFERRED):</b> Renegade Immortal Chapter 1-5
 * establishes that the hills around Wang Family Village have wolves.
 * Wang Lin hears them at night. The village elder worries about them.
 * They are part of the ecology, not a quest trigger. This class makes
 * that ecology produce events that the simulation can react to.
 *
 * <p><b>Per Article XLI:</b> this is not Wang Lin-specific. Any actor
 * with a personality and an activity will be interrupted by these events
 * based on their traits. A reckless NPC ignores the wolves; a cautious
 * NPC observes them. The world judges, not the developer.
 *
 * <p><b>Per Article V:</b> these events fire regardless of whether
 * the player is watching. If no player is within 128 blocks, the events
 * still fire (but no subscriber's distance check will match, so no
 * observable behavior change occurs — this is correct, it saves CPU).
 */
public final class VillageBeastActivity {

    private VillageBeastActivity() {}

    /**
     * How often (in ticks) a beast event fires near each cluster of NPCs.
     * 6000 ticks = 5 minutes at 20 TPS. Frequent enough that a player
     * who stands near a meditating NPC for 5-10 minutes will witness
     * at least one interruption. Rare enough to not spam the event bus.
     */
    public static final int EVENT_INTERVAL_TICKS = 6000;

    /** Random for offset calculation (deterministic per call, not seeded). */
    private static final Random RNG = new Random();

    /**
     * Fire beast activity events near clusters of linked NPCs.
     *
     * <p>Called from the server tick loop. Internally throttled to
     * {@link #EVENT_INTERVAL_TICKS}.
     *
     * <p>The algorithm:
     * <ol>
     *   <li>Find all linked (spawned) actors.</li>
     *   <li>Cluster them by 128-block regions.</li>
     *   <li>For each cluster, if enough ticks have passed since the
     *       last event in that region, fire a beast event at a random
     *       position 30-60 blocks from the cluster center.</li>
     * </ol>
     *
     * <p>The event uses {@link EnergyType#PHYSICAL} (96-block radius)
     * because wolf howls and movement are physical disturbances that
     * anyone can hear. The intensity is 0.4 (moderate — enough to
     * exceed the 0.2 severity floor in ActivityInterruptionSubscriber).
     *
     * @param level the server level
     * @param tick  the current server tick
     */
    public static void tick(ServerLevel level, long tick) {
        if (tick % EVENT_INTERVAL_TICKS != 0) return;
        if (level.players().isEmpty()) return;

        // Collect linked actor positions and cluster them.
        // NPCs within 128 blocks of each other form one cluster.
        // One beast event fires per cluster per interval, not per NPC.
        java.util.ArrayList<int[]> rawPositions = new java.util.ArrayList<>();
        for (Actor a : ActorRegistry.all()) {
            if (dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)) {
                rawPositions.add(new int[]{a.blockX, a.blockY, a.blockZ});
            }
        }
        if (rawPositions.isEmpty()) return;

        // Simple clustering: for each position, check if it's within
        // CLUSTER_RADIUS of an existing cluster center. If not, start
        // a new cluster. Each cluster gets exactly one beast event.
        java.util.ArrayList<int[]> clusterCenters = new java.util.ArrayList<>();
        int clusterRadiusSq = 128 * 128;
        for (int[] pos : rawPositions) {
            boolean merged = false;
            for (int[] center : clusterCenters) {
                double dsq = Math.pow(pos[0] - center[0], 2)
                             + Math.pow(pos[2] - center[2], 2);
                if (dsq < clusterRadiusSq) {
                    // Merge into existing cluster (update center toward this pos).
                    center[0] = (center[0] + pos[0]) / 2;
                    center[2] = (center[2] + pos[2]) / 2;
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                clusterCenters.add(new int[]{pos[0], pos[1], pos[2]});
            }
        }

        // Fire one beast event per cluster.
        for (int[] center : clusterCenters) {
            fireBeastEventNear(level, tick, center[0], center[1], center[2]);
        }
    }

    /**
     * Fire a single beast event near the given position.
     *
     * <p>The event is placed 30-60 blocks away in a random direction.
     * This matches the canon: wolves patrol the treeline outside
     * the village, not inside it.
     */
    private static void fireBeastEventNear(ServerLevel level, long tick,
                                             int cx, int cy, int cz) {
        // Random offset: 30-60 blocks, random direction.
        double angle = RNG.nextDouble() * Math.PI * 2.0;
        double dist = 30.0 + RNG.nextDouble() * 30.0;
        int eventX = cx + (int) (Math.cos(angle) * dist);
        int eventZ = cz + (int) (Math.sin(angle) * dist);
        int eventY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, eventX, eventZ);
        BlockPos eventPos = new BlockPos(eventX, eventY, eventZ);

        // Choose a beast event topic. Variety makes the world feel alive.
        String topic = pickBeastTopic();

        // Intensity 0.4 = moderate. Enough to pass the 0.2 severity
        // floor in ActivityInterruptionSubscriber, not so high that
        // every NPC panics.
        float intensity = 0.35f + RNG.nextFloat() * 0.15f;

        String description = pickBeastDescription(topic);

        WorldEventBus.publish(
                topic,
                EnergyType.PHYSICAL,
                eventPos,
                intensity,
                description,
                "INFERRED from RI Ch.1-5 (wolves patrol hills near Wang Family Village)",
                tick
        );

        Ergenverse.LOGGER.debug("[Ergenverse] Beast activity: {} at ({}, {}, {}) i={}",
                topic, eventX, eventY, eventZ, intensity);
    }

    /**
     * Pick a beast event topic. Weighted toward wolf_pack (the CE #1
     * trigger) but includes variety for a living world.
     */
    private static String pickBeastTopic() {
        double r = RNG.nextDouble();
        if (r < 0.50) return "beast.wolf_pack.stalking";
        if (r < 0.70) return "beast.wolf_pack.howl";
        if (r < 0.85) return "beast.predator.movement";
        if (r < 0.95) return "beast.spirit_herd.grazing";
        return "beast.apex.presence";
    }

    /**
     * Pick a canon-faithful description for the beast event.
     */
    private static String pickBeastDescription(String topic) {
        switch (topic) {
            case "beast.wolf_pack.stalking":
                return "A wolf pack is moving through the hills, their eyes catching the moonlight.";
            case "beast.wolf_pack.howl":
                return "A long, mournful howl echoes across the valley. The wolves are communicating.";
            case "beast.predator.movement":
                return "Something large moves through the underbrush beyond the treeline.";
            case "beast.spirit_herd.grazing":
                return "Spirit deer graze at the forest edge, their faint glow visible at dusk.";
            case "beast.apex.presence":
                return "The birds have gone silent. Something ancient is nearby.";
            default:
                return "Beast activity detected in the surrounding wilderness.";
        }
    }
}