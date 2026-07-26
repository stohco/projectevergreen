package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * FollowPlayerGoal — CRON-COMPLETIONIST-102.
 *
 * <p>A companion AI goal that makes an {@link EntityCultivator} follow a
 * specific player. Designed for Li Muwan after her successful revival —
 * canon: "两人踏天同行，超越生死轮回" (together they transcend, beyond the
 * cycle of life and death). She follows Wang Lin as his eternal companion.
 *
 * <h2>Canon Basis (fact-checked via web-search 2026-07-26)</h2>
 * <p>In the novel 仙逆 by 耳根, after Wang Lin revives Li Muwan:
 * <ul>
 *   <li>"此后，两人踏天同行，超越生死轮回，相爱相守，生生世世" —
 *       they transcend together, beyond life and death, in love for all
 *       eternity.</li>
 *   <li>Li Muwan's power is immense: "弹指灭天" (destroys heaven with a
 *       flick of her finger), "天道法则竟如琉璃一般寸寸崩碎" (the laws of
 *       heaven shatter like glaze). She is Wang Lin's equal, not a
 *       helpless follower.</li>
 *   <li>Her cultivation reaches 踏天境 (Heaven-Trampling Realm = Fourth Step
 *       = TRANSCENDENCE), the same realm as Wang Lin.</li>
 * </ul>
 *
 * <p><b>NO fabricated chapter citation.</b> The post-revival companion
 * relationship is canon-attested via multiple web-search sources (Baidu
 * Baike, 360娱乐, etc.).
 *
 * <h2>Behavior</h2>
 * <p>The goal is always registered on every EntityCultivator, but it only
 * activates when the entity has a non-empty
 * {@link EntityCultivator#getFollowingPlayerUuid()} — i.e., the entity has
 * been designated as a companion (currently only revived Li Muwan).
 *
 * <p>When active:
 * <ul>
 *   <li>If the target player is more than {@link #FOLLOW_DISTANCE} blocks
 *       away, the cultivator pathfinds toward a position near the player.</li>
 *   <li>If the target player is within {@link #FOLLOW_DISTANCE}, the
 *       cultivator stands still (doesn't crowd the player).</li>
 *   <li>If the target player is more than {@link #TELEPORT_DISTANCE} blocks
 *       away (e.g., the player teleported or logged in far away), the
 *       cultivator teleports to a safe position near the player.</li>
 *   <li>The cultivator does NOT follow into combat — if the cultivator has
 *       a hostile target, this goal yields to the combat goal.</li>
 * </ul>
 *
 * <h2>Design notes</h2>
 * <ul>
 *   <li>The goal uses {@link DefaultRandomPos} to find a valid pathfinding
 *       target near the player, avoiding walls and hazards.</li>
 *   <li>The follow distance is intentionally close (6 blocks) — a companion
 *       walks beside you, not 30 blocks behind.</li>
 *   <li>The teleport distance (32 blocks) is generous to avoid losing the
 *       companion when the player sprints or flies. A future CRON could
 *       make this distance realm-aware (TRANSCENDENCE cultivators can
 *       teleport instantly across any distance).</li>
 *   <li>The goal does NOT use Minecraft's {@code FollowOwnerGoal} because
 *       EntityCultivator is not a {@code TamableAnimal}. The companion bond
 *       is set via {@link EntityCultivator#setFollowingPlayerUuid}, not via
 *       vanilla taming.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see EntityCultivator#setFollowingPlayerUuid
 * @see EntityCultivator#getFollowingPlayerUuid
 */
public class FollowPlayerGoal extends Goal {

    /** Distance (in blocks) at which the cultivator stops approaching the player. */
    private static final double FOLLOW_DISTANCE = 6.0;

    /** Distance (in blocks) at which the cultivator pathfinds toward the player. */
    private static final double PATHFIND_DISTANCE = 10.0;

    /** Distance (in blocks) beyond which the cultivator teleports to the player. */
    private static final double TELEPORT_DISTANCE = 32.0;

    /** Squared follow distance (avoid sqrt in the hot path). */
    private static final double FOLLOW_DISTANCE_SQ = FOLLOW_DISTANCE * FOLLOW_DISTANCE;

    /** Squared pathfind distance. */
    private static final double PATHFIND_DISTANCE_SQ = PATHFIND_DISTANCE * PATHFIND_DISTANCE;

    /** Squared teleport distance. */
    private static final double TELEPORT_DISTANCE_SQ = TELEPORT_DISTANCE * TELEPORT_DISTANCE;

    /** Cooldown between pathfinding attempts (prevents spamming the pathfinder). */
    private static final int PATHFIND_COOLDOWN = 10;

    private final EntityCultivator cultivator;
    private int pathfindCooldown;

    public FollowPlayerGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.pathfindCooldown = 0;
    }

    @Override
    public boolean canUse() {
        // Only active when a following-player UUID is set.
        if (!cultivator.isFollowingPlayer()) return false;
        // Don't follow while in combat — yield to combat goals.
        if (cultivator.getTarget() != null) return false;
        // Don't follow while activity-locked (e.g., meditating, lecturing).
        if (cultivator.isActivityLocked()) return false;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (pathfindCooldown > 0) {
            pathfindCooldown--;
        }

        String uuidStr = cultivator.getFollowingPlayerUuid();
        if (uuidStr == null || uuidStr.isEmpty()) return;

        UUID playerUuid;
        try {
            playerUuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            // Malformed UUID — clear the following flag to prevent recurring errors.
            cultivator.setFollowingPlayerUuid("");
            return;
        }

        // Find the target player in the server level.
        if (!(cultivator.level() instanceof ServerLevel serverLevel)) return;
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(playerUuid);
        if (player == null) {
            // Player is offline or in a different dimension. The cultivator
            // stays put — a future CRON could teleport the companion to the
            // player's last known position when they log back in.
            return;
        }

        double distSq = cultivator.distanceToSqr(player);

        // Phase 3: Teleport if too far away.
        if (distSq > TELEPORT_DISTANCE_SQ) {
            teleportToPlayer(player);
            return;
        }

        // Phase 2: Pathfind toward the player if at intermediate distance.
        if (distSq > PATHFIND_DISTANCE_SQ) {
            if (pathfindCooldown <= 0) {
                pathfindTowardPlayer(player);
                pathfindCooldown = PATHFIND_COOLDOWN;
            }
            return;
        }

        // Phase 1: Close enough — stop moving (don't crowd the player).
        cultivator.getNavigation().stop();
    }

    /**
     * Pathfind toward a position near the player. Uses
     * {@link DefaultRandomPos} to find a valid target on the player's side,
     * avoiding walls and hazards.
     */
    private void pathfindTowardPlayer(ServerPlayer player) {
        Vec3 playerPos = player.position();
        Vec3 cultivatorPos = cultivator.position();
        Vec3 direction = playerPos.subtract(cultivatorPos).normalize();

        // Target a position ~5 blocks toward the player (within follow distance).
        Vec3 target = cultivatorPos.add(direction.scale(5.0));

        // Use DefaultRandomPos to find a valid pathfinding target near the
        // desired position (avoids walls, lava, void, etc.).
        Vec3 validTarget = DefaultRandomPos.getPosTowards(cultivator, 8, 4,
                target, Math.PI / 2.0);

        if (validTarget != null) {
            cultivator.getNavigation().moveTo(validTarget.x, validTarget.y, validTarget.z, 0.6D);
        }
    }

    /**
     * Teleport the cultivator to a safe position near the player. Used when
     * the player has moved too far for pathfinding (e.g., teleported, logged
     * in far away, or sprinted beyond 32 blocks).
     *
     * <p>The cultivator teleports to the player's position offset by a few
     * blocks to avoid overlapping. The teleport finds the surface height
     * at the target position to avoid teleporting into the ground.
     */
    private void teleportToPlayer(ServerPlayer player) {
        // Find a safe position 2-4 blocks from the player.
        double angle = cultivator.getRandom().nextDouble() * Math.PI * 2.0;
        double offset = 2.0 + cultivator.getRandom().nextDouble() * 2.0;
        double targetX = player.getX() + Math.cos(angle) * offset;
        double targetZ = player.getZ() + Math.sin(angle) * offset;

        // Find the surface height at the target position.
        int surfaceY = cultivator.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE,
                net.minecraft.core.BlockPos.containing(targetX, player.getY(), targetZ)).getY();

        cultivator.moveTo(targetX + 0.5, surfaceY + 1.0, targetZ + 0.5,
                cultivator.getYRot(), cultivator.getXRot());
        cultivator.getNavigation().stop();

        Ergenverse.LOGGER.debug("[Ergenverse] CRON-102: FollowPlayerGoal teleported {} to "
                        + "near player {} at ({}, {}, {}).",
                cultivator.getDisplayNameCn(), player.getName().getString(),
                targetX, surfaceY, targetZ);
    }
}
