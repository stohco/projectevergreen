package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * SpiritBeastSwimGoal — beasts swim toward surface and navigate in water.
 *
 * <p>Behavior: when the beast is in water, this goal activates and sets
 * POSE_SWIMMING. The beast navigates toward the nearest surface point and
 * then toward the shore. Prevents beasts from drowning by swimming upward
 * when air supply runs low.
 *
 * <p>CRON-COMPLETIONIST-13: New goal. Previously, all beasts had FloatGoal
 * (prevents drowning) but no actual swimming AI. Aquatic beasts (future:
 * sea serpents) will use this to navigate properly underwater.
 */
public class SpiritBeastSwimGoal extends Goal {

    private final SpiritBeastEntity beast;
    private int stateTimer;

    public SpiritBeastSwimGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return beast.isInWater() && beast.getRandom().nextInt(20) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        return beast.isInWater();
    }

    @Override
    public void start() {
        stateTimer = 100 + beast.getRandom().nextInt(100);
    }

    @Override
    public void stop() {
        beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
    }

    @Override
    public void tick() {
        stateTimer--;
        beast.setSpiritPose(SpiritBeastEntity.POSE_SWIMMING);

        // Priority 1: swim toward surface if air is low
        if (beast.getAirSupply() < beast.getMaxAirSupply() * 0.3F) {
            beast.setDeltaMovement(beast.getDeltaMovement().add(0.0D, 0.08D, 0.0D));
            beast.hurtMarked = true;
            return;
        }

        // Priority 2: swim toward nearest shore
        Vec3 shore = findNearestShore();
        if (shore != null) {
            Vec3 direction = shore.subtract(beast.position()).normalize();
            beast.setDeltaMovement(direction.scale(0.15D));
            beast.getNavigation().moveTo(shore.x, shore.y, shore.z, 0.5D);
        }

        // Stop if we've been swimming too long or reached shore
        if (stateTimer <= 0 || !beast.isInWater()) {
            stop();
        }
    }

    private Vec3 findNearestShore() {
        // Scan in a spiral outward from current position for dry land
        for (int r = 1; r <= 20; r++) {
            for (int a = 0; a < 8; a++) {
                double angle = a * Math.PI * 2 / 8;
                int x = beast.blockPosition().getX() + (int) (Math.cos(angle) * r);
                int z = beast.blockPosition().getZ() + (int) (Math.sin(angle) * r);
                int y = beast.blockPosition().getY();
                // Check for solid ground above water level
                if (!beast.level().getBlockState(new net.minecraft.core.BlockPos(x, y, z))
                        .getFluidState().isSource()) {
                    return new Vec3(x + 0.5D, y, z + 0.5D);
                }
            }
        }
        return null;
    }
}
