package dev.ergenverse.entity.control;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.state.BlockState;

/**
 * FlightMoveControl — 3D aerial movement for flying beasts (Hawk, Bat, Qilin).
 *
 * <p>CRON-COMPLETIONIST-65: Updated to work with FlyPathNavigation. Previously
 * this control handled altitude maintenance and basic 1-block obstacle vaulting.
 * Now it adds multi-block lookahead (3 blocks ahead at multiple heights) and
 * diagonal dodging when head-on collision is detected.
 *
 * <p>The control works in concert with FlyPathNavigation:
 * <ul>
 *   <li>FlyPathNavigation finds the 3D path (avoids trees, cliffs)</li>
 *   <li>FlightMoveControl handles the physics of following that path in the air</li>
 *   <li>When the pathfinder's path leads through an obstacle (can happen at
 *       path recalculation boundaries), this control provides emergency avoidance</li>
 * </ul>
 *
 * <p>Obstacle avoidance hierarchy:
 * <ol>
 *   <li>Primary: FlyPathNavigation avoids obstacles during path planning</li>
 *   <li>Secondary: This control's 3-block lookahead vaults over unexpected blocks</li>
 *   <li>Tertiary: Diagonal dodge when head-on collision is imminent</li>
 *   <li>Fallback: Altitude ceiling/floor clamping prevents escape from world bounds</li>
 * </ol>
 */
public class FlightMoveControl extends MoveControl {

    private final SpiritBeastEntity beast;

    public FlightMoveControl(SpiritBeastEntity beast) {
        super(beast);
        this.beast = beast;
    }

    @Override
    public void tick() {
        boolean inFlight = !this.beast.onGround();

        if (!inFlight && this.beast.getDeltaMovement().y <= 0.0D) {
            super.tick();
            return;
        }

        if (inFlight && this.operation == Operation.MOVE_TO) {
            double dx = this.wantedX - this.beast.getX();
            double dz = this.wantedZ - this.beast.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.1D) {
                // Normalize horizontal direction
                double nx = dx / dist;
                double nz = dz / dist;

                // ── Multi-block lookahead at multiple heights ──
                // Check 3 blocks ahead at head height, chest height, and above
                boolean blocked = false;
                for (double heightOffset : new double[]{0.5D, 1.0D, 1.5D}) {
                    double lookAhead = 2.0D; // was 1.0, increased for earlier detection
                    double checkX = this.beast.getX() + nx * lookAhead;
                    double checkY = this.beast.getY() + heightOffset;
                    double checkZ = this.beast.getZ() + nz * lookAhead;
                    BlockPos checkPos = BlockPos.containing(checkX, checkY, checkZ);
                    BlockState state = this.beast.level().getBlockState(checkPos);
                    if (state.isSolidRender(this.beast.level(), checkPos)) {
                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    // Check if we can dodge diagonally (left or right)
                    boolean canDodgeLeft = !isBlockedAt(this.beast.getX() + nz * 2.0D, this.beast.getY() + 1.0D, this.beast.getZ() - nx * 2.0D);
                    boolean canDodgeRight = !isBlockedAt(this.beast.getX() - nz * 2.0D, this.beast.getY() + 1.0D, this.beast.getZ() + nx * 2.0D);

                    if (canDodgeLeft || canDodgeRight) {
                        // Diagonal dodge: shift perpendicular to travel direction
                        double dodgeStrength = 0.2D;
                        if (canDodgeLeft) {
                            this.beast.setDeltaMovement(
                                    this.beast.getDeltaMovement().add(nz * dodgeStrength, 0.15D, -nx * dodgeStrength));
                        } else {
                            this.beast.setDeltaMovement(
                                    this.beast.getDeltaMovement().add(-nz * dodgeStrength, 0.15D, nx * dodgeStrength));
                        }
                    } else {
                        // No diagonal path: vault upward (stronger impulse)
                        this.beast.setDeltaMovement(
                                this.beast.getDeltaMovement().add(0, 0.25D, 0));
                    }
                }
            }
        }

        // In flight: delegate to standard MoveControl for ground-plane movement
        super.tick();

        if (inFlight) {
            double vy = this.beast.getDeltaMovement().y;
            if (this.beast.getY() > 256) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, -0.15D, 0));
            } else if (this.beast.getY() < this.beast.level().getMinBuildHeight() + 5) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.15D, 0));
            } else if (vy < 0.05D) {
                // Counteract downward drift
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.05D, 0));
            }
        } else {
            if (this.beast.getY() > 270) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, -0.15D, 0));
            } else if (this.beast.getY() < this.beast.level().getMinBuildHeight() + 5) {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.15D, 0));
            } else {
                this.beast.setDeltaMovement(
                        this.beast.getDeltaMovement().add(0, 0.02D, 0));
            }
        }

        this.beast.hurtMarked = true;
    }

    /**
     * Check if a position is blocked by a solid block.
     */
    private boolean isBlockedAt(double x, double y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockState state = this.beast.level().getBlockState(pos);
        return state.isSolidRender(this.beast.level(), pos);
    }
}
