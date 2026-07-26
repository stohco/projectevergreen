package dev.ergenverse.runtime.materialize;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.EntityPlacementDelta;
import dev.ergenverse.runtime.layer.ChunkContribution;
import dev.ergenverse.runtime.layer.CompositeWorldLayer;
import dev.ergenverse.runtime.layer.WorldLayer;
import dev.ergenverse.runtime.worldgen.DeterministicTerrainGenerator;
import dev.ergenverse.spawn.DeterministicSeedHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * PlanetSuzakuChunkMaterializer — the concrete, <b>stateless</b> chunk materializer.
 *
 * <p><b>Architectural directive (CRON-69, point 6):</b> "Materializer should
 * become stateless. … Instead of ChunkMaterializer being an object with state,
 * I'd make it almost a pure function. Blueprint + Simulation Layer + Player
 * Layer → Chunk. That makes debugging unbelievably easy."
 *
 * <p>This class holds <b>no mutable state</b>. {@link #materialize} is a pure
 * function of (chunkX, chunkZ, runtime, chunk): it asks the
 * {@link CompositeWorldLayer} what each layer contributes to the chunk, then
 * applies those contributions in materialization order (blueprint structures →
 * simulation changes → player changes), so PLAYER edits land last and win on
 * conflict. It also runs the {@link DeterministicTerrainGenerator} for canon
 * geographic accents.
 *
 * <p><b>Point 7 — chunk-scoped, not whole-chunk-from-scratch:</b> the
 * materializer never iterates all 65k blocks of a chunk asking "what should be
 * here?". It asks each layer "what do YOU contribute to this chunk?" and
 * applies only those contributions. For most chunks (far from any canon
 * structure, no recorded deltas) every layer's contribution is empty, so
 * materialization is a near-no-op — the deterministic minecraft:noise base
 * terrain (seeded with {@link DeterministicSeedHandler#CANON_SEED}) already
 * fills the chunk.
 *
 * <p><b>Wiring.</b> The {@link ChunkEvent.Load} subscriber below routes
 * Planet Suzaku chunk loads to {@link #materialize}. Writes are deferred one
 * tick via the server's {@code TickTask} to avoid recursive-load issues during
 * chunk assembly.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlanetSuzakuChunkMaterializer implements ChunkMaterializer {

    private static final ResourceKey<Level> SUZAKU_KEY = ResourceKey.create(
            Registries.DIMENSION, new ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));

    /**
     * Materialize a chunk: apply each layer's contribution in materialization
     * order, then run deterministic decoration. Stateless and idempotent.
     */
    @Override
    public void materialize(int chunkX, int chunkZ, WorldRuntime runtime, ChunkAccess chunk) {
        ServerLevel level = runtime.suzakuLevel();
        if (level == null) return;

        CompositeWorldLayer world = runtime.worldLayer();
        if (world == null) return;

        // 1. Apply each layer in materialization order (low-priority first so
        //    high-priority overwrites). Blueprint structures → sim changes → player changes.
        for (WorldLayer layer : world.layersInMaterializationOrder()) {
            ChunkContribution c = layer.getChunkContribution(chunkX, chunkZ);
            if (c.isEmpty()) continue;

            // Canon structures: delegate to the registered builder (idempotent).
            // CRON-COMPLETIONIST-62: pass chunk bounds so chunk-scoped builders
            // (e.g. WangFamilyVillageBuilder) only place blocks inside this chunk.
            // Builders that don't yet implement chunk-scoping ignore the bounds
            // and fall back to a full build (guarded by isAlreadyBuilt).
            if (layer.provenance() == dev.ergenverse.runtime.Provenance.CANON) {
                ChunkBounds bounds = ChunkBounds.forChunk(chunkX, chunkZ);
                for (PlanetSuzakuBlueprint.CanonLocation loc : c.structures) {
                    StructureBuilderRegistry.build(loc.id, level, bounds);
                }
            } else {
                // Sim/player block changes: replay onto the live level.
                for (BlockChangeDelta d : c.blockChanges) {
                    runtime.world().applyBlockChange(d.x(), d.y(), d.z(), d.blockState(), d.provenance());
                }
                // CRON-78: replay entity placements (PLACE and REMOVE) after
                // block changes. This re-creates player-placed ItemFrames/Paintings
                // (idempotent — skips if entity already exists) and discards
                // player-removed canon entities (no-op if already gone).
                for (EntityPlacementDelta d : c.entityPlacements) {
                    runtime.world().applyEntityPlacement(
                            d.x(), d.y(), d.z(), d.action(), d.entityNbt(), d.provenance());
                }
            }
        }

        // 2. Deterministic canon-region decoration (point 9).
        try {
            DeterministicTerrainGenerator.decorate(chunkX, chunkZ, runtime.blueprint(), level);
        } catch (Throwable t) {
            Ergenverse.LOGGER.debug("[Ergenverse] DeterministicTerrainGenerator failed for chunk ({},{}): {}",
                    chunkX, chunkZ, t.getMessage());
        }
    }

    // ── Chunk-load hook ─────────────────────────────────────────────────

    /**
     * On every Planet Suzaku chunk load, defer materialization to the next tick.
     * Deferring avoids mutating a chunk during its own assembly, which some
     * Forge versions reject.
     */
    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.isClientSide()) return;
        if (!level.dimension().equals(SUZAKU_KEY)) return;

        WorldRuntime runtime = WorldRuntime.get();
        if (!runtime.isInitialized()) return;
        ChunkMaterializer m = runtime.chunkMaterializer();
        if (!(m instanceof PlanetSuzakuChunkMaterializer)) return;

        int chunkX = event.getChunk().getPos().x;
        int chunkZ = event.getChunk().getPos().z;
        net.minecraft.world.level.chunk.ChunkAccess mcChunk = event.getChunk();

        // Defer to next tick — safe post-assembly writes.
        level.getServer().tell(new net.minecraft.server.TickTask(
                level.getServer().getTickCount() + 1,
                () -> {
                    try {
                        m.materialize(chunkX, chunkZ, runtime, new McChunkAccess(mcChunk));
                    } catch (Throwable t) {
                        Ergenverse.LOGGER.error("[Ergenverse] Chunk materialization failed for ({},{}): {}",
                                chunkX, chunkZ, t.getMessage(), t);
                    }
                }));
    }

    /**
     * Adapter from a Minecraft {@link net.minecraft.world.level.chunk.ChunkAccess}
     * to the materializer's {@link ChunkMaterializer.ChunkAccess} write
     * interface. Resolves the name collision between the two types. The
     * materializer today writes through the level facade rather than chunk-local
     * writes, but the adapter keeps the interface contract honored and future-safe.
     */
    private static final class McChunkAccess implements ChunkAccess {
        private final net.minecraft.world.level.chunk.ChunkAccess chunk;

        McChunkAccess(net.minecraft.world.level.chunk.ChunkAccess chunk) {
            this.chunk = chunk;
        }

        @Override
        public boolean setBlock(int localX, int y, int localZ, String blockId) {
            // Not currently used — the materializer writes via the WorldFacade.
            return false;
        }

        @Override public int getChunkX() { return chunk.getPos().x; }
        @Override public int getChunkZ() { return chunk.getPos().z; }
    }
}
