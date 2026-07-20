package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SpawnEventHandler — handles world initialization and player first-join.
 *
 * <p><b>Phase 1: World initialization (ServerStartingEvent).</b>
 * When the server starts, the Wang Family Village is built at its canonical
 * fixed coordinate (3842, surface, -1184) on Planet Suzaku. This happens
 * BEFORE any player joins. The village exists independently of the player.
 * Per the user's directive: "The player shouldn't be causing canonical
 * places to come into existence."
 *
 * <p><b>Phase 2: Player first-join (PlayerLoggedInEvent).</b>
 * On first join, the player is teleported to Planet Suzaku at a point
 * ~600 blocks from the village. They receive NO tutorial book. NO starter
 * gear. NO chat messages. They wake up in the world and must find their
 * own way. Per the user's directive: "Nobody explains anything. Learning
 * comes from asking, watching, following, copying, exploring — not opening
 * Page 3 of a guidebook."
 *
 * <p>The player's spawn point is set to the teleport location so that
 * death-respawns return them there (not to the village).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpawnEventHandler {

    private SpawnEventHandler() {}

    /** Per-player NBT flag: true once the player has been teleported to Suzaku. */
    private static final String NBT_SUZAKU_TELEPORTED = "ergenverse.suzaku_teleported";

    /** Distance from the village where the player spawns (blocks). */
    private static final int SPAWN_DISTANCE_FROM_VILLAGE = 600;

    // ── Phase 1: Build the village on server start ───────────────────

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        // Build the Wang Family Village at its canonical coordinate on
        // Planet Suzaku. This happens before any player joins — the village
        // exists independently of the player.
        ResourceKey<Level> suzakuKey = ResourceKey.create(Registries.DIMENSION,
                new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
        ServerLevel suzakuLevel = event.getServer().getLevel(suzakuKey);

        if (suzakuLevel == null) {
            Ergenverse.LOGGER.error("[Ergenverse] Planet Suzaku dimension not found! Village not built.");
            return;
        }

        // Delay by 20 ticks (1s) to ensure all dimensions are fully loaded.
        event.getServer().tell(new TickTask(event.getServer().getTickCount() + 20, () -> {
            try {
                if (!WangFamilyVillageBuilder.isAlreadyBuilt(suzakuLevel)) {
                    Ergenverse.LOGGER.info("[Ergenverse] Building Wang Family Village at canonical coordinate ({}, ?, {}) on Planet Suzaku.",
                            WangFamilyVillageBuilder.VILLAGE_X, WangFamilyVillageBuilder.VILLAGE_Z);
                    WangFamilyVillageBuilder.build(suzakuLevel);
                } else {
                    Ergenverse.LOGGER.info("[Ergenverse] Wang Family Village already built at canonical coordinate.");
                }
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] Failed to build Wang Family Village: {}", e.getMessage(), e);
            }
        }));
    }

    // ── Phase 2: Teleport player to Planet Suzaku on first join ──────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.server == null) return;

        boolean firstJoin = !sp.getPersistentData().getBoolean(NBT_SUZAKU_TELEPORTED);
        if (!firstJoin) return; // returning player — they're already on Suzaku

        Ergenverse.LOGGER.info("[Ergenverse] First join for {} — teleporting to Planet Suzaku.",
                sp.getName().getString());

        // Mark immediately to prevent re-triggering.
        sp.getPersistentData().putBoolean(NBT_SUZAKU_TELEPORTED, true);

        // Delay by 40 ticks (2s) so the player has fully loaded.
        sp.server.tell(new TickTask(sp.server.getTickCount() + 40, () -> {
            try {
                ResourceKey<Level> suzakuKey = ResourceKey.create(Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
                ServerLevel suzakuLevel = sp.server.getLevel(suzakuKey);

                if (suzakuLevel == null) {
                    Ergenverse.LOGGER.error("[Ergenverse] Planet Suzaku dimension not found! Player stays in overworld.");
                    return;
                }

                // Player spawn point: SPAWN_DISTANCE_FROM_VILLAGE blocks west
                // of the village, at the same Z. The player must travel east
                // to find the village.
                int spawnX = WangFamilyVillageBuilder.VILLAGE_X - SPAWN_DISTANCE_FROM_VILLAGE;
                int spawnZ = WangFamilyVillageBuilder.VILLAGE_Z;
                int spawnY = suzakuLevel.getHeightmapPos(
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        new BlockPos(spawnX, 0, spawnZ)).getY();

                // Ensure the chunk is loaded.
                suzakuLevel.getChunkAt(new BlockPos(spawnX, spawnY, spawnZ));

                // Teleport the player.
                sp.teleportTo(suzakuLevel, spawnX + 0.5, spawnY + 1.0, spawnZ + 0.5, 90.0F, 0.0F);
                Ergenverse.LOGGER.info("[Ergenverse] Teleported {} to Planet Suzaku at ({}, {}, {}) — {} blocks from the village.",
                        sp.getName().getString(), spawnX, spawnY, spawnZ, SPAWN_DISTANCE_FROM_VILLAGE);

                // Set the player's spawn point to this location (so death
                // respawns them here, not at the village).
                sp.setRespawnPosition(suzakuLevel.dimension(), new BlockPos(spawnX, spawnY, spawnZ),
                        0.0F, true, false);

                // NO tutorial book. NO starter gear. NO chat messages.
                // The player wakes up in the world. Nobody explains anything.
                // Per the user's directive: learning is emergent.
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] Failed to teleport {} to Planet Suzaku: {}",
                        sp.getName().getString(), e.getMessage(), e);
            }
        }));
    }
}
