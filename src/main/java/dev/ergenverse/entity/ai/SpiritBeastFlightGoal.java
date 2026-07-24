package dev.ergenverse.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

import dev.ergenverse.entity.SpiritBeastEntity;

/**
 * SpiritBeastFlightGoal — obstacle-aware 3D flight for flying beasts.
 *
 * <p>CRON-COMPLETIONIST-65: Major rewrite. Previously this goal used direct
 * setDeltaMovement to bulldoze toward waypoints, clipping through trees and
 * terrain. Now the entity uses its PathNavigation (FlyPathNavigation for
 * flyers, installed by SpiritBeastEntity.reassessNavigation()) to path
 * through 3D space with obstacle avoidance.
 *
 * <p>The flight pattern is now:
 * <ul>
 *   <li>Pick an airborne waypoint 10-25 blocks above ground</li>
 *   <li>Navigate to it using the entity's pathfinder (not direct movement)</li>
 *   <li>The pathfinder handles obstacle avoidance (trees, cliffs, buildings)</li>
 *   <li>On approaching the waypoint, pick a new one</li>
 *   <li>If a target exists and is within swoop range, dive toward it</li>
 *   <li>noGravity is set during flight, cleared on landing</li>
 * </ul>
 *
 * <p>This is the behavioral counterpart to the FlyPathNavigation fix in
 * SpiritBeastEntity. Together they eliminate the "bulldozing through trees"
 * problem that existed for 50+ CRON rounds.
 */
public class SpiritBeastFlightGoal extends Goal {

    private final Mob mob;
    private final double speed;
    private int flightDuration;
    private int swoopCooldown;
    private boolean isSwooping;

    public SpiritBeastFlightGoal(Mob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (mob.isLeashed() || mob.isPassenger()) return false;
        if (mob.getTarget() != null && mob.getTarget().isAlive()) {
            // Has a combat target — fly if target is far enough for a swoop approach
            return mob.distanceToSqr(mob.getTarget()) > 64.0D; // >8 blocks
        }
        // Flyers fly by default — ~20% chance per tick to start flight
        if (mob instanceof SpiritBeastEntity beast && beast.getBeastType().isFlyer()) {
            return mob.getRandom().nextInt(5) == 0;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.isLeashed() || mob.isPassenger()) return false;
        return flightDuration > 0;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        mob.setNoGravity(true);
        isSwooping = false;
        // Flyers fly for 10-20 seconds; non-flyers (future use) for 5-10s
        if (mob instanceof SpiritBeastEntity beast && beast.getBeastType().isFlyer()) {
            flightDuration = 200 + mob.getRandom().nextInt(200);
        } else {
            flightDuration = 100 + mob.getRandom().nextInt(100);
        }
        swoopCooldown = 60;
        if (mob instanceof SpiritBeastEntity beast) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_FLYING);
        }
        pickNewWaypoint();
    }

    @Override
    public void stop() {
        mob.setNoGravity(false);
        // Gentle descent to ground
        mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.1D, 0));
        mob.hurtMarked = true;
        if (mob instanceof SpiritBeastEntity beast) {
            beast.setSpiritPose(SpiritBeastEntity.POSE_PERCHING);
        }
        mob.getNavigation().stop();
        isSwooping = false;
    }

    @Override
    public void tick() {
        flightDuration--;
        swoopCooldown--;

        LivingEntity prey = mob.getTarget();

        // ── Swoop at prey if cooldown elapsed ──
        if (prey != null && prey.isAlive() && swoopCooldown <= 0) {
            double distSq = mob.distanceToSqr(prey);
            if (distSq < 625.0D) { // within 25 blocks
                isSwooping = true;
                // Use navigation to path toward the prey's position
                // FlyPathNavigation will find a 3D path that avoids obstacles
                mob.getNavigation().moveTo(prey, speed * 1.5);
                if (distSq < 4.0D) {
                    // Close enough — attack
                    mob.doHurtTarget(prey);
                    swoopCooldown = 40; // 2s between swoop attacks
                    isSwooping = false;
                }
            } else {
                isSwooping = false;
            }
        }

        // ── Navigate toward waypoint using pathfinder (not bulldozing) ──
        if (!isSwooping) {
            // Check if we've reached the current path destination or navigation stalled
            if (!mob.getNavigation().isInProgress() || mob.getNavigation().isDone()) {
                pickNewWaypoint();
            }
        }

        // ── Altitude clamping ──
        if (mob.getY() > 256) {
            // Above build limit: push down gently
            mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.1D, 0));
            mob.hurtMarked = true;
        } else if (mob.getY() < mob.level().getMinBuildHeight() + 5) {
            // Too low: push up
            mob.setDeltaMovement(mob.getDeltaMovement().add(0, 0.15D, 0));
            mob.hurtMarked = true;
        }

        // ── Minimal altitude maintenance (counteract gravity drift) ──
        // FlightMoveControl handles the main altitude maintenance.
        // This is a gentle correction for micro-drift.
        if (!mob.onGround() && mob.getDeltaMovement().y < -0.05D && !isSwooping) {
            mob.setDeltaMovement(mob.getDeltaMovement().add(0, 0.03D, 0));
            mob.hurtMarked = true;
        }
    }

    /**
     * Pick a new airborne waypoint and navigate to it using the pathfinder.
     * The waypoint is 10-25 blocks above local ground level, 10-30 blocks
     * horizontally away. The entity's PathNavigation (FlyPathNavigation)
     * handles obstacle avoidance.
     */
    private void pickNewWaypoint() {
        // Find ground height below the mob
        BlockPos groundPos = mob.blockPosition();
        while (groundPos.getY() > mob.level().getMinBuildHeight() + 1
                && !mob.level().getBlockState(groundPos).isSolidRender(mob.level(), groundPos)
                && !mob.level().getBlockState(groundPos.below()).isSolidRender(mob.level(), groundPos.below())) {
            groundPos = groundPos.below();
        }
        int groundY = groundPos.getY();

        // Pick a waypoint 10-25 blocks above ground, 10-30 blocks horizontally
        double angle = mob.getRandom().nextDouble() * Math.PI * 2;
        double dist = 10 + mob.getRandom().nextInt(20);
        double dx = Math.cos(angle) * dist;
        double dz = Math.sin(angle) * dist;
        int dy = 10 + mob.getRandom().nextInt(15);

        BlockPos target = BlockPos.containing(
                mob.getX() + dx,
                Math.max(groundY + dy, mob.level().getMinBuildHeight() + 5),
                mob.getZ() + dz);

        // Use the entity's pathfinder (FlyPathNavigation) to navigate there
        // This replaces the old direct setDeltaMovement that bulldozed through terrain
        mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
    }
}
