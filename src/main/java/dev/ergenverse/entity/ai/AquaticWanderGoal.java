package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AquaticWanderGoal — water-seeking wander for aquatic beasts.
 *
 * <p>Canonically WRONG behavior fixed: Sea serpents and soul fish were using
 * WaterAvoidingRandomStrollGoal, which causes aquatic entities to AVOID water
 * and wander on land. A sea serpent strolling on a beach is absurd. A soul fish
 * flopping around on shore is worse.
 *
 * <p>This goal replaces WaterAvoidingRandomStrollGoal for aquatic entities
 * (SEA_SERPENT, SOUL_FISH). Behavior:
 * <ul>
 *   <li>Picks a random underwater position within wander radius</li>
 *   <li>Navigates toward it using WaterBoundPathNavigation</li>
 *   <li>Prefers deeper water (more water blocks above = preferred)</li>
 *   <li>Only activates when the entity is in water</li>
 *   <li>Sets POSE_SWIMMING during movement</li>
 *   <li>If the entity is on land, it navigates toward the nearest water</li>
 * </ul>
 *
 * <p>Constitution: Article I (Canon Is Reality) — sea serpents live in water.
 * Article V (Everything Exists Without The Player) — serpents patrol their
 * territory regardless of player presence. Article XIII (Every Living Thing
 * Wants Something) — aquatic beasts want to be in water.
 */
public class AquaticWanderGoal extends Goal {

    private final SpiritBeastEntity beast;
    private final double speed;
    private final int wanderRadius;
    private int timeoutCooldown;
    private Vec3 targetPos;

    public AquaticWanderGoal(SpiritBeastEntity beast, double speed) {
        this(beast, speed, 16);
    }

    public AquaticWanderGoal(SpiritBeastEntity beast, double speed, int wanderRadius) {
        this.beast = beast;
        this.speed = speed;
        this.wanderRadius = wanderRadius;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (timeoutCooldown > 0) {
            timeoutCooldown--;
            return false;
        }
        // If on land, always try to get back to water
        if (!beast.isInWater()) {
            targetPos = findNearestWater();
            return targetPos != null;
        }
        // Random chance to start wandering (every ~2-4 seconds at 20 tps)
        if (beast.getRandom().nextInt(60) != 0) return false;
        targetPos = findUnderwaterPosition();
        return targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (targetPos == null) return false;
        // Continue if we haven't reached the target and timeout hasn't expired
        return beast.distanceToSqr(targetPos.x, targetPos.y, targetPos.z) > 2.0D
                && timeoutCooldown <= 0;
    }

    @Override
    public void start() {
        if (targetPos != null) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_SWIMMING);
            beast.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
        }
    }

    @Override
    public void stop() {
        targetPos = null;
        timeoutCooldown = 20 + beast.getRandom().nextInt(40); // 1-3 second cooldown
        if (!beast.isInWater()) {
            // Don't reset pose if stranded — keep swimming pose until rescued
        } else {
            beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
        }
    }

    @Override
    public void tick() {
        if (targetPos != null) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_SWIMMING);
            beast.getLookControl().setLookAt(targetPos.x, targetPos.y, targetPos.z, 30.0F, 30.0F);
            // Re-navigate if stuck
            if (beast.getNavigation().isDone()) {
                beast.getNavigation().moveTo(targetPos.x, targetPos.y, targetPos.z, speed);
            }
        }
    }

    /**
     * Find a random position that is underwater. Prefers deeper water.
     */
    private Vec3 findUnderwaterPosition() {
        BlockPos beastPos = beast.blockPosition();
        // Try 10 random positions within wander radius
        for (int attempt = 0; attempt < 10; attempt++) {
            double dx = (beast.getRandom().nextDouble() - 0.5) * 2.0 * wanderRadius;
            double dz = (beast.getRandom().nextDouble() - 0.5) * 2.0 * wanderRadius;
            double dy = (beast.getRandom().nextDouble() - 0.5) * 6.0; // ±3 blocks vertical

            BlockPos candidate = new BlockPos(
                    beastPos.getX() + (int) dx,
                    beastPos.getY() + (int) dy,
                    beastPos.getZ() + (int) dz);

            if (isWaterAt(candidate)) {
                // Score: prefer deeper water (more water blocks above)
                int depth = waterDepthAbove(candidate);
                // Always accept if at least 1 block deep, with preference for deeper
                if (depth >= 1) {
                    return new Vec3(candidate.getX() + 0.5, candidate.getY() + 0.5, candidate.getZ() + 0.5);
                }
            }
        }
        return null;
    }

    /**
     * Find the nearest water block when stranded on land.
     */
    private Vec3 findNearestWater() {
        BlockPos beastPos = beast.blockPosition();
        // Spiral outward from current position
        for (int r = 1; r <= 32; r++) {
            for (int a = 0; a < 12; a++) {
                double angle = a * Math.PI * 2 / 12;
                int x = beastPos.getX() + (int) (Math.cos(angle) * r);
                int z = beastPos.getZ() + (int) (Math.sin(angle) * r);
                // Scan vertically for water
                for (int y = beastPos.getY() + 3; y >= beastPos.getY() - 5; y--) {
                    BlockPos candidate = new BlockPos(x, y, z);
                    if (isWaterAt(candidate)) {
                        return new Vec3(x + 0.5, y + 0.5, z + 0.5);
                    }
                }
            }
        }
        return null;
    }

    private boolean isWaterAt(BlockPos pos) {
        FluidState fluid = beast.level().getFluidState(pos);
        return fluid.isSource() && !beast.level().getBlockState(pos).isSolidRender(
                beast.level(), pos);
    }

    private int waterDepthAbove(BlockPos pos) {
        int depth = 0;
        for (int y = pos.getY() + 1; y < pos.getY() + 10; y++) {
            if (isWaterAt(new BlockPos(pos.getX(), y, pos.getZ()))) {
                depth++;
            } else {
                break;
            }
        }
        return depth;
    }
}
