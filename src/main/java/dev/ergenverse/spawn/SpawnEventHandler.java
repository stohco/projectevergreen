package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SpawnEventHandler — handles world initialization and player first-join.
 *
 * <p><b>Phase 1: World initialization (ServerStartingEvent).</b>
 * When the server starts, the Wang Family Village is materialized at its
 * canonical fixed coordinate (3842, surface, -1184) on Planet Suzaku by
 * force-loading the village chunks. This triggers the composition system
 * (CanonSettlementBuilder via StructureBuilderRegistry →
 * PlanetSuzakuChunkMaterializer) which builds the village from the
 * semantic composition tree. The village exists objectively before any
 * player joins. Per the user's directive: "The player shouldn't be causing
 * canonical places to come into existence."
 *
 * <p><b>Phase 2: Player first-join (PlayerLoggedInEvent).</b>
 * On first join, the player is teleported directly INTO the Wang Family
 * Village plaza — standing on the hand-crafted Central Plaza floor,
 * surrounded by the Wang Family Home, Elder's Home, commoner homes,
 * perimeter fence, roads, and path lights. They receive NO tutorial book.
 * NO starter gear. NO chat messages. They wake up in the village and must
 * find their own way. Per the user's directive: "Nobody explains anything.
 * Learning comes from asking, watching, following, copying, exploring —
 * not opening Page 3 of a guidebook."
 *
 * <p>The player's spawn point is set to the village plaza so that
 * death-respawns return them there.
 *
 * <p><b>CRON-131 — SPAWN INTO THE VILLAGE (user directive).</b>
 *
 * <p>Prior to CRON-131, the player spawned 600 blocks WEST of the village
 * and had to travel east to find it. The user explicitly requested: "I
 * expect to spawn into the village and hand crafted world, not some
 * randomized seed." CRON-131 changes the spawn point to the village plaza.
 *
 * <p>CRON-131 also removes the LEGACY WangFamilyVillageBuilder.build()
 * call that ran at server start. The composition system handles village
 * materialization automatically on ChunkEvent.Load. The legacy call was:
 * <ul>
 *   <li>The 1220-line retired builder (CRON-126 retired it from the live
 *       materialization path — see StructureBuilderRegistry).</li>
 *   <li>Causing the double-build bug (CRON-129 self-critique #6: legacy
 *       builds ~80K blocks that get OVERWRITTEN by the composition system
 *       on chunk load).</li>
 *   <li>Placing an orphaned SPIRIT_VEIN_STONE marker at the plaza corner
 *       (CRON-129 self-critique #7: the composition's plaza floor is at a
 *       different Y, so the marker floats above the composition build).</li>
 *   <li>Wasting ~80K setBlock calls that get overwritten — server-start lag.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpawnEventHandler {

    private SpawnEventHandler() {}

    /** Per-player NBT flag: true once the player has been teleported to Suzaku. */
    private static final String NBT_SUZAKU_TELEPORTED = "ergenverse.suzaku_teleported";

    /**
     * Half-extent of the village chunk pre-load grid. A 7x7 chunk grid
     * ({@code VILLAGE_CHUNK_RADIUS=3}) covers a 112x112 block area — more
     * than sufficient to materialize the full 83x83 village footprint via
     * the composition system before any player arrives. The village is
     * objectively present when the player teleports in.
     *
     * <p>The village footprint is 83x83 blocks (VILLAGE_RADIUS=41). A 7x7
     * chunk grid (112x112 blocks) provides a 14-block margin around the
     * village perimeter, ensuring all village chunks — including the
     * perimeter fence, outer roads, and path lights — are materialized.
     */
    private static final int VILLAGE_CHUNK_RADIUS = 3;

    // ── Phase 1: Materialize the village on server start ───────────────

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // CRON-69 (ten-point refactor): initialize the WorldRuntime, bound to the
        // Planet Suzaku level. The runtime builds the spatial index, loads the
        // persisted delta journal (WorldDeltaSavedData), registers the concrete
        // stateless materializers, and loads all canon actors.
        ResourceKey<Level> suzakuKey = ResourceKey.create(Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
        ServerLevel suzakuLevel = event.getServer().getLevel(suzakuKey);
        if (suzakuLevel == null) {
            Ergenverse.LOGGER.error("[Ergenverse] Planet Suzaku dimension not found! WorldRuntime not initialized.");
            return;
        }

        try {
            dev.ergenverse.runtime.WorldRuntime runtime = dev.ergenverse.runtime.WorldRuntime.get();
            runtime.initialize(suzakuLevel);
            Ergenverse.LOGGER.info("[Ergenverse] WorldRuntime initialized. Blueprint: {} locations, spatial index: {} entries, delta journal: {} changes.",
                    runtime.blueprint().allLocations().size(), runtime.spatialIndex().size(), runtime.deltaStore().size());
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] WorldRuntime initialization failed: {}", e.getMessage(), e);
        }

        // CRON-131: Force-load the village chunks so the composition system
        // materializes the village at server start. The village exists
        // objectively before any player joins — per the user's directive:
        // "The player shouldn't be causing canonical places to come into
        // existence." The composition system (CanonSettlementBuilder via
        // StructureBuilderRegistry → PlanetSuzakuChunkMaterializer) handles
        // the actual build on ChunkEvent.Load. We just load the chunks.
        //
        // The legacy WangFamilyVillageBuilder.build() call is REMOVED
        // (CRON-131). It was the 1220-line retired builder causing the
        // double-build bug + orphaned marker + 80K wasted setBlock calls.
        // The composition system is the single source of truth for the
        // village structure.
        event.getServer().tell(new TickTask(event.getServer().getTickCount() + 20, () -> {
            try {
                int villageX = WangFamilyVillageBuilder.VILLAGE_X;
                int villageZ = WangFamilyVillageBuilder.VILLAGE_Z;
                int centerChunkX = villageX >> 4;
                int centerChunkZ = villageZ >> 4;

                Ergenverse.LOGGER.info("[Ergenverse] CRON-131: Force-loading village chunks ({}x{} grid around chunk ({},{})) to materialize Wang Family Village via composition system.",
                        (2 * VILLAGE_CHUNK_RADIUS + 1), (2 * VILLAGE_CHUNK_RADIUS + 1), centerChunkX, centerChunkZ);

                int loadedChunks = 0;
                for (int dx = -VILLAGE_CHUNK_RADIUS; dx <= VILLAGE_CHUNK_RADIUS; dx++) {
                    for (int dz = -VILLAGE_CHUNK_RADIUS; dz <= VILLAGE_CHUNK_RADIUS; dz++) {
                        int cx = centerChunkX + dx;
                        int cz = centerChunkZ + dz;
                        // Force chunk generation + load to FULL status.
                        // This triggers ChunkEvent.Load →
                        // PlanetSuzakuChunkMaterializer (deferred 1 tick) →
                        // CanonSettlementBuilder.buildWangFamilyVillage.
                        // The materializer's 1-tick defer means the village
                        // blocks land 50ms after this call returns; by the
                        // time a player joins (much later), the village is
                        // fully materialized.
                        suzakuLevel.getChunk(cx, cz, ChunkStatus.FULL, true);
                        loadedChunks++;
                    }
                }
                Ergenverse.LOGGER.info("[Ergenverse] CRON-131: {} village chunks force-loaded. Composition system materialized the village (blocks land 1 tick later via materializer defer).",
                        loadedChunks);

                // Materialize Wang Lin at his canonical starting location
                // (Wang Family Village). The village chunks are loaded;
                // the composition system's blocks will land 1 tick later
                // (materializer defer), but Wang Lin's entity spawn is safe
                // — the chunk is loaded, and the blocks materialize around
                // him. This is the milestone: "Wang Lin materializes from
                // the blueprint at his canonical starting location."
                dev.ergenverse.runtime.WorldRuntime runtime = dev.ergenverse.runtime.WorldRuntime.get();
                if (runtime.isInitialized() && !runtime.npcs().isMaterialized(dev.ergenverse.runtime.CanonUUID.WANG_LIN)) {
                    int eid = runtime.npcs().materializeActor(dev.ergenverse.runtime.CanonUUID.WANG_LIN, runtime);
                    Ergenverse.LOGGER.info("[Ergenverse] Wang Lin materialization: entity id {} (canon UUID {}).",
                            eid, dev.ergenverse.runtime.CanonUUID.WANG_LIN);
                }
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] CRON-131: Failed to force-load village / materialize Wang Lin: {}", e.getMessage(), e);
            }
        }));
    }

    // ── Phase 2: Teleport player to the village on first join ──────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.server == null) return;

        // CRON-103: migrate Li Muwan's revived flag from bead NBT to the
        // WorldDeltaStore's revived-actor set, if needed. This is a one-time
        // migration for pre-CRON-103 saves where the player had already
        // revived Li Muwan but the revived-actor set didn't exist yet. Runs
        // for ALL logins (first-join and returning); idempotent. Must run
        // AFTER WorldRuntime.initialize (which runs on ServerStartingEvent,
        // before any player logs in).
        try {
            dev.ergenverse.wanglin.bead.LiMuwanRevivalEvent.migrateRevivedFlagIfNeeded(sp);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-103: Li Muwan revived-flag migration failed for {}: {}",
                    sp.getName().getString(), t.getMessage());
        }

        boolean firstJoin = !sp.getPersistentData().getBoolean(NBT_SUZAKU_TELEPORTED);
        if (!firstJoin) return; // returning player — they're already on Suzaku

        Ergenverse.LOGGER.info("[Ergenverse] First join for {} — teleporting to Planet Suzaku (Wang Family Village plaza).",
                sp.getName().getString());

        // Mark immediately to prevent re-triggering.
        sp.getPersistentData().putBoolean(NBT_SUZAKU_TELEPORTED, true);

        // Delay by 40 ticks (2s) so the player has fully loaded and the
        // village chunks (force-loaded at server start T+20) are fully
        // materialized (materializer defer + composition build complete).
        sp.server.tell(new TickTask(sp.server.getTickCount() + 40, () -> {
            try {
                ResourceKey<Level> suzakuKey = ResourceKey.create(Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
                ServerLevel suzakuLevel = sp.server.getLevel(suzakuKey);

                if (suzakuLevel == null) {
                    Ergenverse.LOGGER.error("[Ergenverse] Planet Suzaku dimension not found! Player stays in overworld.");
                    return;
                }

                // CRON-131: Spawn the player IN the village plaza, not 600
                // blocks away. The user explicitly requested: "I expect to
                // spawn into the village and hand crafted world."
                //
                // The spawn point is the village center (the Central Plaza).
                // The plaza is a 9x9 hand-crafted area with the spirit well
                // at its center. The player arrives standing on the plaza
                // floor, surrounded by the Wang Family Home (NW), Elder's
                // Home (NE), commoner homes (N/S), perimeter fence, and
                // roads. Wang Lin is materialized nearby.
                int spawnX = WangFamilyVillageBuilder.VILLAGE_X;
                int spawnZ = WangFamilyVillageBuilder.VILLAGE_Z;
                // CRON-93: biome-aware canon surface height — the same
                // pure deterministic function the chunk generator uses.
                // The canon surface height is the SAME function that shapes
                // the terrain, so the player spawns exactly on the canon
                // surface every time.
                int surfaceY = dev.ergenverse.runtime.worldgen.BlueprintChunkGenerator
                        .surfaceHeightFor(suzakuLevel, spawnX, spawnZ);
                // Stand on the plaza floor. surfaceY is the top of the stone
                // fill (the chunk generator fills stone up to surfaceY). The
                // composition system's plaza floor is placed at/above this Y.
                // The player stands at surfaceY + 1 (one block above the
                // stone surface, on the plaza floor).
                int spawnY = surfaceY + 1;

                // Ensure the spawn chunk is loaded (safety net — the village
                // chunks were force-loaded at server start, but this guards
                // against edge cases like a fresh server where the player
                // joins before T+20).
                suzakuLevel.getChunkAt(new BlockPos(spawnX, spawnY, spawnZ));

                // Teleport the player to the village plaza.
                // Yaw = 0.0F faces south — the player sees the plaza and
                // the spirit well ahead, with the village layout visible.
                // Pitch = 0.0F (level gaze).
                sp.teleportTo(suzakuLevel, spawnX + 0.5, spawnY, spawnZ + 0.5, 0.0F, 0.0F);
                Ergenverse.LOGGER.info("[Ergenverse] CRON-131: Teleported {} to Wang Family Village plaza at ({}, {}, {}). Player spawns INTO the village.",
                        sp.getName().getString(), spawnX, spawnY, spawnZ);

                // Set the player's spawn point to the village plaza (so
                // death respawns return them to the village, not the
                // overworld or a random world-spawn).
                sp.setRespawnPosition(suzakuLevel.dimension(), new BlockPos(spawnX, spawnY, spawnZ),
                        0.0F, true, false);

                // NO tutorial book. NO starter gear. NO chat messages.
                // The player wakes up in the village. Nobody explains anything.
                // Per the user's directive: learning is emergent.
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] CRON-131: Failed to teleport {} to Wang Family Village: {}",
                        sp.getName().getString(), e.getMessage(), e);
            }
        }));
    }
}
