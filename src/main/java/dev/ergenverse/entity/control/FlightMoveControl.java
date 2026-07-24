package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FlightMoveControl — 3D aerial movement for flying beasts (Hawk).
 *
 * <p>CRON-COMPLETIONIST-13: Created to close the behavioral gap where
 * flyers bulldozed through terrain with direct setDeltaMovement.
 *
 * <p>CRON-COMPLETIONIST-31: Improved altitude maintenance. Previously the
 * control added a flat 0.02 upward drift which was too weak to counter
 * gravity effectively, and the onGround() check caused the hawk to "fall
 * into walk mode" whenever it touched the ground during landing. Now flyers
 * maintain altitude more aggressively when noGravity is set.
 *
 * <p>Obstacle avoidance: Before horizontal movement, check if the next
 * position at head height has a solid block. If blocked, add upward impulse
 * to vault over obstacles.
 */
public class FlightMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;

    public FlightMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        // CRON-COMPLETIONIST-31: If the entity is off the ground, it's in flight.
        // Use !onGround() as the proxy for "noGravity is set by FlightGoal".
        // This prevents the hawk from reverting to walk mode during low passes.
        boolean inFlight = !this.beast.onGround();

        if (!inFlight && this.beast.getDeltaMovement().y <= 0.0D) {
            super.tick();
            return;
        }

        // Obstacle avoidance: check if horizontal movement is blocked at head height
        if (inFlight && this.operation == Operation.MOVE_TO) {
            double dx = this.wantedX - this.beast.getX();
            double dz = this.wantedZ - this.beast.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.1D) {
                // Normalize horizontal direction
                double nx = dx / dist;
                double nz = dz / dist;
                // Check 1 block ahead at head height (entity Y + 1 block)
                double lookAhead = 1.0D;
                double checkX = this.beast.getX() + nx * lookAhead;
                double checkY = this.beast.getY() + 1.0D;
                double checkZ = this.beast.getZ() + nz * lookAhead;
                BlockPos checkPos = BlockPos.containing(checkX, checkY, checkZ);
                BlockState state = this.beast.level().getBlockState(checkPos);
                if (state.isSolidRender(this.beast.level(), checkPos)) {
                    // Blocked: add upward impulse to vault over
                    this.beast.setDeltaMovement(
                            this.beast.getDeltaMovement().add(0, 0.15D, 0));
                }
            }
        }

        // In flight: delegate to standard MoveControl for ground-plane movement,
        // then add vertical component.
        super.tick();

        if (inFlight) {
            // Stronger altitude maintenance when actively flying
            // Target altitude: maintain current Y (don't sink)
            double vy = this.beast.getDeltaMovement().y;
            if (this.beast.getY() > 300) {
                // Ceiling: push down
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, -0.15D, 0));
            } else if (this.beast.getY() < this.beast.level().getMinBuildHeight() + 5) {
                // Floor: push up
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.15D, 0));
            } else if (vy < 0.05D) {
                // Counteract downward drift — maintain altitude
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.05D, 0));
            }
        } else {
            // Not in flight but not on ground (falling/landing): gentle drift
            if (this.beast.getY() > 320) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, -0.15D, 0));
            } else if (this.beast.getY() < this.beast.level().getMinBuildHeight() + 5) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.15D, 0));
            } else {
                // Gentle upward drift to maintain altitude
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.02D, 0));
            }
        }

        this.beast.hurtMarked = true;
    }
}
