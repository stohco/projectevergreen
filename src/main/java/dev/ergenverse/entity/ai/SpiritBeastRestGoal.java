package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * SpiritBeastRestGoal — beasts rest when safe, idle, and at full health.
 *
 * <p>CRON-COMPLETIONIST-15: Created to close the POSE_RESTING gap. Previously,
 * no goal ever set POSE_RESTING — it existed as a constant but was never used.
 * This goal fires when the beast has no target, is on the ground, health is
 * above 90%, and the navigation is idle. The beast lies down for 5-15 seconds,
 * setting POSE_RESTING for the model to render a resting pose (curled legs,
 * lowered head, tail tucked).
 *
 * <p>This is distinct from BeastRestRecoverGoal (which fires when health < 80%
 * to heal). RestGoal is for HEALTHY idle rest; RecoverGoal is for INJURED healing.
 * Both can coexist — a beast won't rest while recovering because RecoverGoal
 * takes priority (lower number = higher priority).
 *
 * <p>Self-critique: No "find safe location" behavior — rests wherever it stands.
 * No bedding/nest preference. No hysteresis on the 90% health threshold
 * (beast might oscillate between rest and stand if health hovers at 90%).
 */
public class SpiritBeastRestGoal extends Goal {

    private final SpiritBeastEntity beast;
    private int restTimer;

    public SpiritBeastRestGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Only rest when: no target, on ground, healthy, idle navigation, random chance
        if (beast.getTarget() != null) return false;
        if (!beast.onGround()) return false;
        if (beast.getHealth() < beast.getMaxHealth() * 0.9D) return false;
        if (!beast.getNavigation().isDone()) return false;
        if (beast.hurtTime > 0) return false;
        // ~1.7% chance per tick — roughly rests every 60 seconds of idle time
        return beast.getRandom().nextInt(60) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return restTimer > 0
                && beast.getTarget() == null
                && beast.onGround()
                && beast.hurtTime == 0;
    }

    @Override
    public void start() {
        restTimer = 100 + beast.getRandom().nextInt(200); // 5-15 seconds
        beast.getNavigation().stop();
        beast.setSpiritPose(SpiritBeastEntity.POSE_RESTING);
    }

    @Override
    public void stop() {
        restTimer = 0;
        beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
    }

    @Override
    public void tick() {
        restTimer--;
        // Keep setting pose each tick so tick() heuristic doesn't override it
        beast.setSpiritPose(SpiritBeastEntity.POSE_RESTING);
        // Occasionally look around while resting (head movement only)
        if (restTimer % 40 == 0) {
            float lookYaw = beast.getYRot() + (beast.getRandom().nextFloat() - 0.5F) * 60.0F;
            beast.getLookControl().setLookAt(
                    beast.getX() + Math.cos(Math.toRadians(lookYaw)) * 3.0D,
                    beast.getEyeY() - 0.3D,
                    beast.getZ() + Math.sin(Math.toRadians(lookYaw)) * 3.0D,
                    30.0F, 30.0F);
        }
    }
}
