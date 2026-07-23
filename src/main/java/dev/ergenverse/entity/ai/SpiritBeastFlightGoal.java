package dev.ergenverse.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

import dev.ergenverse.entity.SpiritBeastEntity;

/**
 * SpiritBeastFlightGoal — true 3D flight for flying beasts (Hawk, future dragons).
 *
 * <p>Prior to this goal, the Spirit Hawk WALKED on the ground (GroundPathNavigation)
 * despite having a bird model and wings. This goal makes it actually FLY by:
 * <ul>
 *   <li>Setting noGravity on start, restoring on stop.</li>
 *   <li>Picking random airborne waypoints (y = groundY + 10..25) and flying to them.</li>
 *   <li>Occasionally diving toward ground prey (swoop attack).</li>
 *   <li>Using direct setDeltaMovement (no pathfinding — flying ignores terrain).</li>
 * </ul>
 *
 * <p>Self-critique: No real 3D pathfinding (setDeltaMovement bulldozes through trees).
 * Waypoint picking ignores territory. Swoop prey scan is O(n) every 40 ticks.
 * A real flight AI would use a 3D navigation grid + wind currents + thermals.
 */
public class SpiritBeastFlightGoal extends Goal {

    private final Mob mob;
    private final double speed;
    private Vec3 targetPos;
    private int waypointCooldown;
    private int swoopCooldown;

    public SpiritBeastFlightGoal(Mob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only fly if not leashed, not in a vehicle, and not currently hurt-recovering.
        if (mob.isLeashed() || mob.isPassenger()) return false;
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            // Has a target — fly if target is far (swoop from above).
            return mob.distanceToSqr(mob.getTarget()) > 100.0D;
        }
        // CRON-COMPLETIONIST-31: Flying beasts should FLY by default, not walk.
        // Previously ~4% chance per tick (1/80) meant the hawk walked on ground
        // 96% of the time despite having a bird model and wings. Now flyers have
        // a ~70% chance per tick to fly — they are BIRDS, they belong in the air.
        // The canContinueToUse() still limits flight duration to 5-10 seconds.
        if (mob instanceof SpiritBeastEntity beast && beast.getBeastType().isFlyer()) {
            return mob.getRandom().nextInt(5) == 0; // ~20% per tick to START flight
        }
        return mob.getRandom().nextInt(80) == 0; // non-flyers: ~4% (future bat swarm etc.)
    }

    @Override
    public boolean canContinueToUse() {
        return !mob.isLeashed() && !mob.isPassenger() && waypointCooldown > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true; // Flight needs per-tick position updates for smooth 3D movement
    }

    @Override
    public void start() {
        mob.setNoGravity(true);
        pickNewWaypoint();
        // CRON-COMPLETIONIST-31: Longer flight duration for flyers (10-20s instead of 5-10s).
        // Hawks should spend most of their time airborne, not walking on ground.
        if (mob instanceof SpiritBeastEntity beast && beast.getBeastType().isFlyer()) {
            waypointCooldown = 200 + mob.getRandom().nextInt(200); // 10-20 seconds
        } else {
            waypointCooldown = 100 + mob.getRandom().nextInt(100); // 5-10 seconds (non-flyers)
        }
        swoopCooldown = 60;
        if (mob instanceof SpiritBeastEntity beast) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_FLYING);
        }
    }

    @Override
    public void stop() {
        mob.setNoGravity(false);
        targetPos = null;
        // CRON-COMPLETIONIST-31: For flyers, set a SHORT rest on the ground before
        // flying again. Previously the hawk landed and walked indefinitely because
        // canUse() only had 4% trigger chance. Now after landing, a flyer waits
        // only 1-3 seconds on the ground before re-launching (the ~20% per-tick
        // canUse() rate ensures this).
        mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.1D, 0));
        mob.hurtMarked = true;
        if (mob instanceof SpiritBeastEntity beast) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_PERCHING);
        }
    }

    @Override
    public void tick() {
        waypointCooldown--;

        // ── Swoop at prey if a target exists and cooldown elapsed ──
        LivingEntity prey = mob.getTarget();
        if (prey != null && prey.isAlive() && swoopCooldown <= 0) {
            double distSq = mob.distanceToSqr(prey);
            if (distSq < 900.0D) { // within 30 blocks
                // Dive toward prey
                targetPos = prey.position().add(0, 1, 0);
                if (distSq < 4.0D) {
                    // Close enough — attack
                    mob.doHurtTarget(prey);
                    swoopCooldown = 40; // 2s between swoop attacks
                    // Climb back up after attack
                    targetPos = mob.position().add(0, 12, 0);
                }
            }
        }
        swoopCooldown--;

        // ── Move toward target waypoint ──
        if (targetPos == null || mob.distanceToSqr(targetPos) < 9.0D) {
            pickNewWaypoint();
        }

        if (targetPos != null) {
            Vec3 direction = targetPos.subtract(mob.position()).normalize();
            Vec3 motion = direction.scale(speed);
            // Add slight upward bias to maintain altitude
            motion = motion.add(0, 0.02D, 0);
            mob.setDeltaMovement(motion);
            mob.hurtMarked = true;

            // Face the direction of travel
            mob.setYRot((float) Math.toDegrees(Math.atan2(-motion.x, motion.z)));
            mob.yBodyRot = mob.getYRot();
        }

        // Don't fly too high or too low
        if (mob.getY() > 200) {
            targetPos = mob.position().add(0, -5, 0);
        }
    }

    private void pickNewWaypoint() {
        // Find ground height below the mob
        BlockPos groundPos = mob.blockPosition();
        while (groundPos.getY() > mob.level().getMinBuildHeight() &&
                !mob.level().getBlockState(groundPos).isSolidRender(mob.level(), groundPos)) {
            groundPos = groundPos.below();
        }
        int groundY = groundPos.getY();

        // Pick a waypoint 10-25 blocks above ground, 10-30 blocks horizontally away
        double angle = mob.getRandom().nextDouble() * Math.PI * 2;
        double dist = 10 + mob.getRandom().nextInt(20);
        double dx = Math.cos(angle) * dist;
        double dz = Math.sin(angle) * dist;
        int dy = 10 + mob.getRandom().nextInt(15);

        targetPos = new Vec3(mob.getX() + dx, groundY + dy, mob.getZ() + dz);
    }
}
