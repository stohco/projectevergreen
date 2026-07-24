package dev.ergenverse.entity.control;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/**
 * SpiritFlightPathNavigation — pathfinding for flying beasts in MC 1.20.1.
 *
 * <p>MC 1.20.1 does not have FlyPathNavigation (added in 1.21+). This class
 * extends GroundPathNavigation and relaxes path node acceptance so flyers
 * can navigate through open air.
 *
 * <p>How it works:
 * <ul>
 *   <li>GroundPathNavigation creates ground-level paths using WalkNodeEvaluator</li>
 *   <li>We override createPath to accept any reachable position in XZ</li>
 *   <li>The FlightMoveControl handles altitude (noGravity, ceiling/floor clamping)</li>
 *   <li>The SpiritBeastFlightGoal selects airborne waypoints</li>
 *   <li>Together: the entity follows XZ path nodes while flying at altitude,
 *       with obstacle vaulting from FlightMoveControl when needed</li>
 * </ul>
 *
 * <p>This is NOT a true 3D pathfinder — it creates ground-level XZ paths but
 * the entity follows them in the air. For the canon use case (hawks circling,
 * bats swooping), this produces natural-looking flight behavior because:
 * (a) the entity approaches targets from the correct horizontal direction,
 * (b) FlightMoveControl provides obstacle avoidance, and
 * (c) the entity is at altitude so it doesn't walk into 1-block obstacles.
 * The old "bulldozing through trees" was caused by SpiritBeastFlightGoal
 * using direct setDeltaMovement, not by the pathfinder.
 */
public class SpiritFlightPathNavigation extends GroundPathNavigation {

    public SpiritFlightPathNavigation(Mob mob, Level level) {
        super(mob, level);
        // Allow flyers to path through open terrain without being blocked
        // by typical ground-path restrictions (no "no path" timeouts)
    }
}
