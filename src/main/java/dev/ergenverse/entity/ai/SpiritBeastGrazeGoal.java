package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * SpiritBeastGrazeGoal — herbivores (deer, rabbit) graze when idle.
 *
 * <p>Behavior: when no target and on ground with no movement, the beast enters
 * a grazing cycle (state 1: graze for 3-6s → state 2: alert check for 1-2s →
 * repeat). While grazing, sets POSE_GRAZING. During alert, sets POSE_ALERT.
 * On stop, sets POSE_STANDING.
 *
 * <p>CRON-COMPLETIONIST-13: New goal to close the AI→pose→model loop.
 * Previously, deer's "graze" animation was driven by a blind sin(age*0.05)
 * cycle in the model, disconnected from any AI state. Now the GrazeGoal
 * explicitly sets the pose, and the model reads it.
 */
public class SpiritBeastGrazeGoal extends Goal {

    private final SpiritBeastEntity beast;
    private int state; // 0=idle, 1=grazing, 2=alert
    private int stateTimer;
    private BlockPos grazeTarget;

    public SpiritBeastGrazeGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only graze when idle: no target, on ground, not hurt recently
        if (beast.getTarget() != null) return false;
        if (!beast.onGround()) return false;
        if (beast.hurtTime > 0) return false;
        if (beast.getRandom().nextInt(120) != 0) return false; // ~0.8% per tick
        return beast.getNavigation().isDone();
    }

    @Override
    public boolean canContinueToUse() {
        return beast.getTarget() == null && beast.onGround() && state != 0;
    }

    @Override
    public void start() {
        state = 1; // start grazing
        stateTimer = 60 + beast.getRandom().nextInt(60); // 3-6 seconds
        // Pick a nearby grass block to look at
        grazeTarget = beast.blockPosition().offset(
                beast.getRandom().nextInt(5) - 2, 0, beast.getRandom().nextInt(5) - 2);
        beast.getNavigation().stop();
    }

    @Override
    public void stop() {
        state = 0;
        stateTimer = 0;
        grazeTarget = null;
        beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
    }

    @Override
    public void tick() {
        stateTimer--;

        // Look down at the grass
        beast.getLookControl().setLookAt(
                grazeTarget.getX() + 0.5D, grazeTarget.getY(), grazeTarget.getZ() + 0.5D,
                30.0F, 30.0F);

        if (state == 1) {
            // Grazing
            beast.setSpiritPose(SpiritBeastEntity.POSE_GRAZING);
            // Move slowly toward graze target
            if (grazeTarget != null && beast.distanceToSqr(
                    net.minecraft.world.phys.Vec3.atCenterOf(grazeTarget)) > 2.0D) {
                beast.getNavigation().moveTo(
                        grazeTarget.getX(), grazeTarget.getY(), grazeTarget.getZ(), 0.3D);
            }
            if (stateTimer <= 0) {
                state = 2; // transition to alert check
                stateTimer = 20 + beast.getRandom().nextInt(20); // 1-2 seconds
            }
        } else if (state == 2) {
            // Alert: snap head up, look around
            beast.setSpiritPose(SpiritBeastEntity.POSE_ALERT);
            // Random look around
            float lookYaw = beast.getYRot() + (beast.getRandom().nextFloat() - 0.5F) * 120.0F;
            beast.getLookControl().setLookAt(
                    beast.getX() + Math.cos(Math.toRadians(lookYaw)) * 5.0D,
                    beast.getEyeY(),
                    beast.getZ() + Math.sin(Math.toRadians(lookYaw)) * 5.0D,
                    30.0F, 30.0F);
            if (stateTimer <= 0) {
                // Resume grazing or stop
                if (beast.getRandom().nextInt(3) != 0) {
                    state = 1;
                    stateTimer = 60 + beast.getRandom().nextInt(60);
                } else {
                    stop();
                }
            }
        }
    }
}
