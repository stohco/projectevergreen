package dev.ergenverse.runtime.persist;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * WorldDeltaSavedData — persists the {@link WorldDeltaStore} with the Planet
 * Suzaku level via Minecraft's {@link SavedData} mechanism.
 *
 * <p><b>This closes CRON-68's #1 gap:</b> "The deltas are NOT serialized yet.
 * They exist in memory but are not saved to disk. On world save/load, the
 * deltas would be lost (all changes revert to canon). This is the #1 gap — the
 * deltas need NBT serialization in the world save."
 *
 * <p>{@code SavedData} is Minecraft's built-in per-level persistence. It is
 * saved automatically when the level saves and loaded automatically when the
 * level loads. We store the whole delta journal under a single data id
 * ({@code "ergenverse_world_deltas"}), so one read + one write per save cycle.
 *
 * <p><b>What persists.</b> Every recorded {@link dev.ergenverse.runtime.delta.BlockChangeDelta}
 * (player edits + simulation changes) is serialized via
 * {@link WorldDeltaStore#serialize}. On load, {@link WorldDeltaStore#deserialize}
 * rebuilds the journal and the per-provenance position index, so chunk
 * materialization replays the exact same changes. Future non-block deltas
 * (actor moves, relationships, memories) serialize through the same channel
 * automatically — they implement {@link dev.ergenverse.runtime.delta.WorldDelta}
 * and register a codec.
 *
 * <p><b>The milestone proof.</b> With this in place:
 * <ul>
 *   <li>Break a wall → PlayerBlockDeltaTracker records a PLAYER
 *       {@code BlockChangeDelta(pos, "minecraft:air")}.</li>
 *   <li>Save the world → this SavedData writes the delta to NBT.</li>
 *   <li>Reload → SavedData rebuilds the store → next chunk load replays the
 *       air → the wall is still broken.</li>
 *   <li>Create a second new save → the store is empty → the wall is intact
 *       (the blueprint was never modified).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldDeltaSavedData extends SavedData {

    private static final String DATA_ID = "ergenverse_world_deltas";

    private final WorldDeltaStore store;

    private WorldDeltaSavedData(WorldDeltaStore store) {
        this.store = store;
    }

    /** The store this SavedData owns. WorldRuntime reads/writes through it. */
    public WorldDeltaStore store() { return store; }

    /**
     * Get the WorldDeltaSavedData for the Planet Suzaku level. The provided
     * {@code storeToFill} becomes the canonical store: if a saved journal
     * exists on disk it is deserialized into that store; otherwise the store
     * stays empty (fresh save). The returned SavedData wraps the same store, so
     * WorldRuntime's reference and the SavedData's reference are one and the
     * same — writes through the facade are persisted automatically on level save.
     */
    public static WorldDeltaSavedData getOrCreate(ServerLevel level, WorldDeltaStore storeToFill) {
        // 1.20.1 SavedData API: computeIfAbsent(loader, factory, name).
        // Both the loader and the factory wrap the SAME storeToFill, so the
        // store WorldRuntime holds is the one that gets populated on load.
        return level.getDataStorage().computeIfAbsent(
                tag -> {
                    storeToFill.deserialize(tag);
                    return new WorldDeltaSavedData(storeToFill);
                },
                () -> new WorldDeltaSavedData(storeToFill),
                DATA_ID
        );
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        store.serialize(tag);
        Ergenverse.LOGGER.info("[Ergenverse] WorldDeltaSavedData saved: {} deltas ({} player block changes, {} simulation block changes).",
                store.size(), store.blockChangeCount(dev.ergenverse.runtime.Provenance.PLAYER),
                store.blockChangeCount(dev.ergenverse.runtime.Provenance.SIMULATION));
        return tag;
    }
}
