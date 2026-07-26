package dev.ergenverse.runtime.layer;

import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.EntityPlacementDelta;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

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

    // ── Entity placement (CRON-78) ─────────────────────────────────────

    /**
     * Record a player entity placement (ItemFrame or Painting) into the journal.
     * Does NOT mirror to the live level — the entity already exists in the
     * world (the player just placed it via right-click). The journal entry is
     * for reload: on chunk reload, the materializer calls
     * {@link #applyEntityPlacement} which re-creates the entity from NBT if it
     * isn't already present (idempotent).
     *
     * @param entityNbt full entity NBT (via {@code entity.saveWithoutId}) —
     *                  captures facing, item, rotation, variant, etc.
     */
    public void recordPlayerEntityPlacement(int x, int y, int z, CompoundTag entityNbt) {
        store.record(new EntityPlacementDelta(x, y, z,
                EntityPlacementDelta.Action.PLACE, entityNbt, Provenance.PLAYER));
    }

    /**
     * Record a player entity removal into the journal. Does NOT mirror to the
     * live level — the entity is already gone (the player attacked it, vanilla
     * discarded it). The journal entry tells the materializer NOT to re-create
     * a canon entity at this position on reload.
     */
    public void recordPlayerEntityRemoval(int x, int y, int z) {
        store.record(new EntityPlacementDelta(x, y, z,
                EntityPlacementDelta.Action.REMOVE, null, Provenance.PLAYER));
    }

    /**
     * Re-apply a recorded entity placement to the live level (used by the
     * ChunkMaterializer on reload, and by {@link EntityPlacementDelta#apply}).
     * Does NOT re-record — the delta is already in the journal.
     *
     * <p><b>Idempotent for PLACE:</b> if an entity already exists at (x, y, z)
     * (vanilla may have re-created it from chunk NBT), skip the spawn. This
     * prevents duplicate entities when both vanilla persistence and our journal
     * would re-create the same entity.
     *
     * <p><b>For REMOVE:</b> find any ItemFrame or Painting at (x, y, z) and
     * discard it. No-op if none present.
     */
    public void applyEntityPlacement(int x, int y, int z,
                                       EntityPlacementDelta.Action action,
                                       CompoundTag entityNbt,
                                       Provenance provenance) {
        if (level == null) return;
        try {
            BlockPos pos = new BlockPos(x, y, z);
            // Use a small box around the block pos — ItemFrames/Paintings have
            // their position at the hanging block, with a small entity-box offset.
            AABB box = new AABB(pos).inflate(0.5);

            if (action == EntityPlacementDelta.Action.PLACE) {
                // Idempotent: check if an entity already exists at this position.
                List<ItemFrame> existingFrames = level.getEntitiesOfClass(ItemFrame.class, box);
                if (!existingFrames.isEmpty()) {
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "ItemFrame already exists at ({},{},{}) — skipping spawn.", x, y, z);
                    return;
                }
                List<Painting> existingPaintings = level.getEntitiesOfClass(Painting.class, box);
                if (!existingPaintings.isEmpty()) {
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "Painting already exists at ({},{},{}) — skipping spawn.", x, y, z);
                    return;
                }

                if (entityNbt == null) {
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "PLACE delta at ({},{},{}) has null NBT — skipping.", x, y, z);
                    return;
                }

                // Load entity from NBT and spawn it.
                Entity entity = EntityType.loadEntityRecursive(entityNbt, level, e -> e);
                if (entity == null) {
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "failed to load entity from NBT at ({},{},{}).", x, y, z);
                    return;
                }
                // Force the entity's position to (x, y, z) — the NBT may have a
                // slightly different position due to floating-point entity coordinates.
                entity.setPos(x + 0.5, y + 0.5, z + 0.5);
                level.addFreshEntity(entity);
                Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                        "spawned {} at ({},{},{}).", entity.getType().toShortString(), x, y, z);
            } else {  // REMOVE
                for (ItemFrame frame : level.getEntitiesOfClass(ItemFrame.class, box)) {
                    frame.discard();
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "discarded ItemFrame at ({},{},{}).", x, y, z);
                }
                for (Painting painting : level.getEntitiesOfClass(Painting.class, box)) {
                    painting.discard();
                    Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement: " +
                            "discarded Painting at ({},{},{}).", x, y, z);
                }
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] WorldFacade.applyEntityPlacement failed at ({},{},{}): {}",
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

    /**
     * Resolve a block state string to a {@link BlockState} (air-safe).
     *
     * <p><b>CRON-COMPLETIONIST-94:</b> now delegates to
     * {@link dev.ergenverse.runtime.delta.BlockStateCodec#parse} which
     * supports property overrides (e.g. {@code "minecraft:chest[facing=north]"}).
     * Before CRON-94, this method called {@code block.defaultBlockState()},
     * discarding all property information — player-placed chests/stairs/slabs
     * reverted to default facing on chunk reload.
     *
     * <p>Backward compatible: bare block ids ({@code "minecraft:stone"})
     * still resolve to the default state.
     */
    private static BlockState resolveBlockState(String blockId) {
        return dev.ergenverse.runtime.delta.BlockStateCodec.parse(blockId);
    }
}
