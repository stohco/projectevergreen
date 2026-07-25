package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * WorldFacade — the simulation's single front door for writing the world.
 *
 * <p><b>Architectural directive (CRON-69, point 5):</b> "DeltaManager should be
 * almost invisible. Ideally gameplay never writes {@code playerDelta.set(...)}.
 * Instead {@code runtime.world().setBlock(...)}. Then internally
 * runtime → DeltaManager → PlayerDelta. That keeps systems from depending on
 * implementation details."
 *
 * <p>So gameplay code — the {@code PlayerBlockDeltaTracker}, beast harvest AI,
 * weather damage, sect expansion — never touches {@link WorldDeltaStore} or any
 * layer directly. It calls {@code runtime.world().setBlock(...)} (for player
 * edits) or {@code runtime.world().setSimulationBlock(...)} (for simulation
 * edits). This facade routes the call to <i>both</i>:
 * <ol>
 *   <li>the {@link WorldDeltaStore} (so the change is journaled, persisted, and
 *       provenance-tracked), and</li>
 *   <li>the live Minecraft {@link ServerLevel} (so the change is immediately
 *       visible in the world).</li>
 * </ol>
 *
 * <p>This dual write is what makes the delta layer a <b>faithful mirror</b> of
 * the live world rather than a divergent shadow. Minecraft's native chunk save
 * persists the live block; the delta journal persists the provenance + the
 * simulation-only changes that Minecraft would otherwise not know about (e.g.
 * a beast harvesting an herb in an unloaded chunk).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldFacade {

    private final WorldDeltaStore store;
    private ServerLevel level;

    public WorldFacade(WorldDeltaStore store) {
        this.store = store;
    }

    /** Bind the facade to the Planet Suzaku server level (called on initialize). */
    public void bind(ServerLevel level) {
        this.level = level;
    }

    /** The bound level, or null before initialize. */
    public ServerLevel level() { return level; }

    /**
     * Record and apply a player block edit. Writes to the live level AND to the
     * delta journal under {@link Provenance#PLAYER}.
     *
     * @param blockId registry id, e.g. {@code "minecraft:air"} (a break) or
     *                {@code "minecraft:stone"} (a place)
     */
    public void setPlayerBlock(int x, int y, int z, String blockId) {
        recordAndApply(new BlockChangeDelta(x, y, z, blockId, Provenance.PLAYER));
    }

    /**
     * Record and apply a simulation block change. Writes to the live level AND
     * to the delta journal under {@link Provenance#SIMULATION}.
     */
    public void setSimulationBlock(int x, int y, int z, String blockId) {
        recordAndApply(new BlockChangeDelta(x, y, z, blockId, Provenance.SIMULATION));
    }

    /**
     * Re-apply a recorded block change to the live level (used by the
     * ChunkMaterializer on reload, and by {@link BlockChangeDelta#apply}). Does
     * NOT re-record — the delta is already in the journal.
     */
    public void applyBlockChange(int x, int y, int z, String blockId, Provenance provenance) {
        if (level == null) return;
        try {
            BlockState state = resolveBlockState(blockId);
            if (state == null) return;
            level.setBlock(new BlockPos(x, y, z), state, net.minecraft.world.level.block.Block.UPDATE_ALL);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyBlockChange failed at ({},{},{}): {}",
                    x, y, z, t.getMessage());
        }
    }

    // ── internals ───────────────────────────────────────────────────────

    private void recordAndApply(BlockChangeDelta delta) {
        // 1. Journal the change (provenance + persistence).
        store.record(delta);
        // 2. Mirror into the live world (immediate visibility).
        if (level != null) {
            try {
                BlockState state = resolveBlockState(delta.blockState());
                if (state != null) {
                    level.setBlock(new BlockPos(delta.x(), delta.y(), delta.z()), state,
                            net.minecraft.world.level.block.Block.UPDATE_ALL);
                }
            } catch (Throwable t) {
                Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade live-mirror failed at ({},{},{}): {}",
                        delta.x(), delta.y(), delta.z(), t.getMessage());
            }
        }
    }

    /** Resolve a registry id string to a BlockState (air-safe). */
    private static BlockState resolveBlockState(String blockId) {
        if (blockId == null || blockId.isEmpty()) return null;
        try {
            ResourceLocation rl = new ResourceLocation(blockId);
            var block = ForgeRegistries.BLOCKS.getValue(rl);
            if (block == null) return null;
            return block.defaultBlockState();
        } catch (Throwable t) {
            return null;
        }
    }
}
