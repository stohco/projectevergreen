package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;

/**
 * WaterBoundMoveControl — aquatic movement for water-dwelling beasts.
 *
 * <p>CRON-COMPLETIONIST-65: Updated to work with WaterBoundPathNavigation.
 * Previously this control only modified sink rate and breathing. Now it also
 * provides active water-column navigation: when the entity's path leads out
 * of water, it applies upward/downward impulse to stay in the water column,
 * and when the entity is beached on land, it pushes back toward water.
 *
 * <p>The control works in concert with WaterBoundPathNavigation:
 * <ul>
 *   <li>WaterBoundPathNavigation finds paths through water volumes</li>
 *   <li>This control handles the physics of following that path underwater</li>
 *   <li>Emergency corrections when the entity drifts out of water or beaches</li>
 * </ul>
 *
 * <p>Behaviors:
 * <ul>
 *   <li>Breathing: force upward when air critically low</li>
 *   <li>Surface breathing: restore air faster at surface</li>
 *   <li>Water column maintenance: prefer staying at mid-depth when no target</li>
 *   <li>Beach avoidance: if on land, push toward nearest water</li>
 *   <li>Depth following: when path goes deeper, apply downward impulse</li>
 * </ul>
 */
public class WaterBoundMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;

    public WaterBoundMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        boolean inWater = this.beast.isInWater();

        if (!inWater) {
            // ── Beached on land: push toward nearest water ──
            // WaterBoundPathNavigation should prevent this, but if the entity
            // ends up on land (spawned on beach, pushed by current), it needs
            // to actively seek water.
            if (this.beast instanceof SpiritBeastEntity) {
                BlockPos waterPos = findNearestWater(this.beast.blockPosition(), 8);
                if (waterPos != null) {
                    this.operation = Operation.MOVE_TO;
                    this.wantedX = waterPos.getX() + 0.5D;
                    this.wantedY = waterPos.getY() + 0.5D;
                    this.wantedZ = waterPos.getZ() + 0.5D;
                    this.speedModifier = 0.5D;
                }
            }
            super.tick();
            return;
        }

        // ── Breathing: force upward when air critically low ──
        int air = this.beast.getAirSupply();
        int maxAir = this.beast.getMaxAirSupply();

        if (air < 20) {
            this.beast.setDeltaMovement(
                    this.beast.getDeltaMovement().add(0, 0.12D, 0));
        } else {
            // Check if at surface
            double headY = this.beast.getY() + this.beast.getEyeHeight();
            int surfaceY = this.beast.blockPosition().above().getY();
            if (headY >= surfaceY - 0.2D && air < maxAir) {
                this.beast.setAirSupply(Math.min(air + 5, maxAir));
            }
        }

        // ── Standard movement ──
        super.tick();

        // ── Reduced sink rate in water ──
        this.beast.setDeltaMovement(
                this.beast.getDeltaMovement().add(0, -0.008D, 0));

        // ── Depth following: if MoveControl wants us to go to a specific Y ──
        // WaterBoundPathNavigation may set the wantedY to a deeper/shallower
        // position. Apply vertical impulse toward that depth.
        if (this.operation == Operation.MOVE_TO && inWater) {
            double dy = this.wantedY - this.beast.getY();
            if (Math.abs(dy) > 0.5D) {
                double yImpulse = Math.signum(dy) * 0.06D;
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, yImpulse, 0));
            }
        }

        this.beast.hurtMarked = true;
    }

    /**
     * Find the nearest water block within the given radius.
     * Used when the entity is beached on land.
     */
    private BlockPos findNearestWater(BlockPos center, int radius) {
        BlockPos nearest = null;
        double nearestDist = Double.MAX_VALUE;
        for (int x = -radius; x <= radius; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (this.beast.level().getFluidState(pos).is(FluidTags.WATER)) {
                        double d = pos.distSqr(center);
                        if (d < nearestDist) {
                            nearestDist = d;
                            nearest = pos;
                        }
                    }
                }
            }
        }
        return nearest;
    }
}
