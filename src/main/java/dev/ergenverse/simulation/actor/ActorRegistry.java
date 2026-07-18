package dev.ergenverse.simulation.actor;

import dev.ergenverse.core.Ergenverse;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * ActorRegistry — central registry of all {@link Actor}s in the simulation.
 *
 * <p>Holds actors by id, by type, and by chunk-region (for spatial queries).
 * The {@link ActorTickLoop} reads from this registry; the
 * {@link dev.ergenverse.simulation.los.SimulationImportanceScore} engine
 * reads from it for distance calcs.
 */
public final class ActorRegistry {

    private static final Map<String, Actor> BY_ID = new HashMap<>();

    private ActorRegistry() {}

    public static void register(Actor a) {
        BY_ID.put(a.id, a);
        Ergenverse.LOGGER.debug("[Ergenverse] ActorRegistry registered {} ({})", a.id, a.type);
    }

    public static Actor get(String id) {
        return BY_ID.get(id);
    }

    public static Collection<Actor> all() {
        return BY_ID.values();
    }

    public static int count() {
        return BY_ID.size();
    }

    public static java.util.Collection<Actor> byType(ActorType type) {
        java.util.List<Actor> out = new java.util.ArrayList<>();
        for (Actor a : BY_ID.values()) if (a.type == type) out.add(a);
        return out;
    }

    /** Returns actors within {@code radiusBlocks} of the given block coordinate. */
    public static java.util.Collection<Actor> withinDistance(int x, int y, int z, int radiusBlocks) {
        java.util.List<Actor> out = new java.util.ArrayList<>();
        long r2 = (long) radiusBlocks * radiusBlocks;
        for (Actor a : BY_ID.values()) {
            long dx = a.blockX - x;
            long dy = a.blockY - y;
            long dz = a.blockZ - z;
            long d2 = dx * dx + dy * dy + dz * dz;
            if (d2 <= r2) out.add(a);
        }
        return out;
    }

    public static void clear() {
        BY_ID.clear();
    }
}
