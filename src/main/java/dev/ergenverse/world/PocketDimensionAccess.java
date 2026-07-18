package dev.ergenverse.world;

import dev.ergenverse.advanced.AdvancedMechanicsEvents;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.function.Function;

/**
 * PocketDimensionAccess — handles teleportation to pocket dimensions.
 *
 * <p>Canon pocket dimensions (accessed from Planet Suzaku surface):
 * <ul>
 *   <li><b>Land of the Ancient God</b> (ancient_god_land) — Tu Si's 3-level
 *       corpse realm. Accessed via the rift in Jue Ming Valley.
 *       Requires: Core Formation+ (the rift's spatial pressure crushes weaker cultivators).
 *       Canon (C5): Wang Lin entered after reaching Soul Formation via Jue Ming Valley.</li>
 *   <li><b>Suzaku Tomb</b> (suzaku_tomb) — underground tomb with the
 *       Cultivation Planet Crystal. Requires: Foundation Establishment+
 *       to survive the tomb's pressure. Canon (C2): Wang Lin found the
 *       Half-Moon Blade here early in his journey.</li>
 *   <li><b>Immortal Graveyard</b> (immortal_graveyard) — 17-layer immortal
 *       graveyard in the Vermilion Bird Starfield. Requires: Soul Formation+
 *       (the spatial distance and seal require substantial power to traverse).
 *       Canon (C8): Wang Lin glimpsed the Fu Clan's Golden Leaf Flame Source
 *       Origin on the 17th layer.</li>
 * </ul>
 *
 * <p><b>Prime Directive:</b> These dimensions exist objectively. The rift/portal
 * is a physical feature of the world, not a perception-gated illusion. However,
 * the spatial pressure INSIDE the dimension can kill weak cultivators — that's
 * physics, not perception.
 *
 * <p>Access methods:
 * <ul>
 *   <li>Land of the Ancient God: right-click a Nether Portal block in Jue Ming Valley
 *       biome while at Core Formation+</li>
 *   <li>Suzaku Tomb: right-click a End Portal Frame block while at Foundation Establishment+</li>
 *   <li>Immortal Graveyard: right-click a Sculk Shrieker block while at Soul Formation+</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID)
public final class PocketDimensionAccess {

    private PocketDimensionAccess() {}

    // ── Dimension Keys ──────────────────────────────────────────────────

    public static final ResourceKey<Level> ANCIENT_GOD_LAND = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation(Ergenverse.MOD_ID, "ancient_god_land"));

    public static final ResourceKey<Level> SUZAKU_TOMB = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation(Ergenverse.MOD_ID, "suzaku_tomb"));

    public static final ResourceKey<Level> IMMORTAL_GRAVEYARD = ResourceKey.create(
            net.minecraft.core.registries.Registries.DIMENSION,
            new ResourceLocation(Ergenverse.MOD_ID, "immortal_graveyard"));

    // ── Access Requirements (minimum absolute tier) ─────────────────────
    // Tier mapping from RealmId:
    //   0=Mortal, 1=Qi Condensation, 2=Foundation Establishment, 3=Core Formation,
    //   4=Nascent Soul, 5=Soul Formation, 6=Soul Transformation, 7=Nirvana Scryer,
    //   8=Heaven Blight, 9=Third Step, 10+ = Fourth Step+

    /** Minimum absolute tier to enter the Land of the Ancient God. */
    private static final int ANCIENT_GOD_MIN_TIER = 3;  // Core Formation
    /** Minimum absolute tier to enter the Suzaku Tomb. */
    private static final int SUZAKU_TOMB_MIN_TIER = 2;  // Foundation Establishment
    /** Minimum absolute tier to enter the Immortal Graveyard. */
    private static final int IMMORTAL_GRAVEYARD_MIN_TIER = 5;  // Soul Formation

    // ── Access Block Triggers ───────────────────────────────────────────
    // These are "rift markers" — vanilla blocks that serve as teleportation
    // triggers until we have custom rift blocks.

    /** Nether Portal blocks in Jue Ming Valley → Land of the Ancient God. */
    private static final net.minecraft.world.level.block.Block RIFT_ANCIENT_GOD =
            net.minecraft.world.level.block.Blocks.NETHER_PORTAL;

    /** End Portal Frame blocks → Suzaku Tomb. */
    private static final net.minecraft.world.level.block.Block RIFT_SUZAKU_TOMB =
            net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME;

    /** Sculk Shrieker blocks → Immortal Graveyard. */
    private static final net.minecraft.world.level.block.Block RIFT_IMMORTAL_GRAVEYARD =
            net.minecraft.world.level.block.Blocks.SCULK_SHRIEKER;

    // ── Dimension Info (for chat messages) ──────────────────────────────

    private static final Component ANCIENT_GOD_NAME =
            Component.literal("\u00A75\u00A7lLand of the Ancient God");
    private static final Component SUZAKU_TOMB_NAME =
            Component.literal("\u00A78\u00A7lSuzaku Tomb");
    private static final Component IMMORTAL_GRAVEYARD_NAME =
            Component.literal("\u00A74\u00A7lImmortal Graveyard");

    // ── Teleportation Logic ─────────────────────────────────────────────

    /**
     * Attempt to teleport a player to a pocket dimension.
     *
     * @param player     the server player
     * @param dimKey     the target dimension ResourceKey
     * @param minTier    minimum absolute cultivation tier required
     * @param dimName    display name for messages
     * @return true if teleportation succeeded
     */
    public static boolean tryTeleport(ServerPlayer player, ResourceKey<Level> dimKey,
                                       int minTier, Component dimName) {
        // Check cultivation tier
        CultivationState state = CultivationCapability.getOrThrow(player);
        if (state.getCurrentRealm().order < minTier) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cThe spatial pressure of " + dimName.getString()
                    + " crushes against you. Your cultivation is insufficient."));
            player.sendSystemMessage(Component.literal(
                    "\u00A77Required: " + getRealmNameForTier(minTier)
                    + " or above."));
            // Spatial backlash — knockback + damage
            player.hurt(player.level().damageSources().magic(), 4.0f);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 0.5f);
            return false;
        }

        // Get the target level
        MinecraftServer server = player.getServer();
        if (server == null) return false;
        ServerLevel targetLevel = server.getLevel(dimKey);
        if (targetLevel == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cThe rift is unstable — the dimension cannot be reached."));
            return false;
        }

        // Find a safe spawn point in the target dimension
        BlockPos spawnPos = targetLevel.getSharedSpawnPos();
        // Adjust Y to find solid ground (nether-like dimensions may need adjustment)
        spawnPos = targetLevel.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                spawnPos);

        // Perform teleportation
        player.teleportTo(targetLevel, spawnPos.getX() + 0.5, spawnPos.getY() + 1,
                spawnPos.getZ() + 0.5, java.util.Set.of(), player.getYRot(), player.getXRot());

        // Effects
        targetLevel.playSound(null, spawnPos, SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        player.sendSystemMessage(Component.literal(
                "\u00A7aThe rift swallows you. You arrive in " + dimName.getString() + "."));

        Ergenverse.LOGGER.info("[Ergenverse] Player {} teleported to {} (tier {})",
                player.getName().getString(), dimKey.location(), state.getCurrentRealm().order);
        return true;
    }

    /**
     * Get a realm name string for a given absolute tier (for requirement messages).
     */
    private static String getRealmNameForTier(int tier) {
        for (RealmId realm : RealmId.values()) {
            if (realm.order == tier) return realm.name;
        }
        return "Tier " + tier;
    }

    // ── Event Handlers ──────────────────────────────────────────────────

    /**
     * Right-click on rift blocks triggers dimension teleportation.
     *
     * <p>Uses vanilla blocks as rift markers:
     * <ul>
     *   <li>Nether Portal → Land of the Ancient God (Core Formation+)</li>
     *   <li>End Portal Frame → Suzaku Tomb (Foundation Establishment+)</li>
     *   <li>Sculk Shrieker → Immortal Graveyard (Soul Formation+)</li>
     * </ul>
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;

        BlockPos pos = event.getPos();
        net.minecraft.world.level.block.state.BlockState state = player.level().getBlockState(pos);
        net.minecraft.world.level.block.Block block = state.getBlock();

        if (block == RIFT_ANCIENT_GOD) {
            event.setCanceled(true);
            tryTeleport(player, ANCIENT_GOD_LAND, ANCIENT_GOD_MIN_TIER, ANCIENT_GOD_NAME);
        } else if (block == RIFT_SUZAKU_TOMB) {
            event.setCanceled(true);
            tryTeleport(player, SUZAKU_TOMB, SUZAKU_TOMB_MIN_TIER, SUZAKU_TOMB_NAME);
        } else if (block == RIFT_IMMORTAL_GRAVEYARD) {
            event.setCanceled(true);
            tryTeleport(player, IMMORTAL_GRAVEYARD, IMMORTAL_GRAVEYARD_MIN_TIER, IMMORTAL_GRAVEYARD_NAME);
        }
        // Note: the Nether Portal block will also trigger vanilla nether teleportation.
        // Since we cancel the event for our dimensions, the vanilla teleport won't fire.
        // However, if the player enters a nether portal via the vanilla portal animation
        // (standing inside it), vanilla will handle that separately. Our right-click
        // handler gives an ADDITIONAL, controlled access path.
    }
}