package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

/**
 * SchoolingGoal — soul fish cluster together in schools.
 *
 * <p>Canon (Renegade Immortal): soul fish are bioluminescent qi-infused fish
 * that travel in shimmering schools through spirit veins and underwater caves.
 * A single soul fish is a curiosity; a school of dozens is a spirit vein
 * indicator that cultivators seek out.
 *
 * <p>Behavior: when nearby same-type entities exist, steer toward the center
 * of mass of the nearest school. Maintain a minimum distance (avoid overlapping)
 * and a maximum distance (rejoin if strayed). Produces natural clustering
 * behavior without explicit school spawning.
 *
 * <p>This is a SEPARATION-ALIGNMENT-COHESION boid rule subset (simplified):
 * <ul>
 *   <li>COHESION: steer toward center of nearby same-type fish</li>
 *   <li>SEPARATION: avoid getting too close to any one fish</li>
 *   <li>ALIGNMENT: loosely match movement direction of neighbors</li>
 * </ul>
 *
 * <p>Only activates for SOUL_FISH beast type. Other aquatic entities (sea serpent)
 * are solitary and do not school.
 *
 * <p>Constitution: Article XIII (Every Living Thing Wants Something) — soul fish
 * want to be near other soul fish. Article V (Everything Exists Without The Player)
 * — schools form and move regardless of player observation.
 */
public class SchoolingGoal extends Goal {

    private final SpiritBeastEntity beast;
    private final double cohesionStrength;
    private final double separationRadius;
    private final double separationStrength;
    private final double detectionRadius;
    private int recheckTimer;

    public SchoolingGoal(SpiritBeastEntity beast) {
        this(beast, 0.03D, 2.0D, 0.08D, 0.15D, 12.0D);
    }

    public SchoolingGoal(SpiritBeastEntity beast, double cohesionStrength,
                          double separationRadius, double separationStrength,
                          double alignmentStrength, double detectionRadius) {
        this.beast = beast;
        this.cohesionStrength = cohesionStrength;
        this.separationRadius = separationRadius;
        this.separationStrength = separationStrength;
        this.detectionRadius = detectionRadius;
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        // Only for soul fish
        if (beast.getBeastType() != SpiritBeastEntity.BeastType.SOUL_FISH) return false;
        // Must be in water
        if (!beast.isInWater()) return false;
        // Don't override panic or combat
        if (beast.getTarget() != null) return false;
        return findNearbySchoolmates().size() > 0;
    }

    @Override
    public boolean canContinueToUse() {
        return beast.isInWater() && beast.getTarget() == null
                && findNearbySchoolmates().size() > 0;
    }

    @Override
    public void start() {
        recheckTimer = 0;
    }

    @Override
    public void stop() {
        // No special cleanup needed
    }

    @Override
    public void tick() {
        recheckTimer++;
        // Only recalculate school center every 10 ticks (0.5s) for performance
        if (recheckTimer % 10 != 0) return;

        List<SpiritBeastEntity> schoolmates = findNearbySchoolmates();
        if (schoolmates.isEmpty()) return;

        // Calculate center of mass
        double cx = 0, cy = 0, cz = 0;
        for (SpiritBeastEntity mate : schoolmates) {
            cx += mate.getX();
            cy += mate.getY();
            cz += mate.getZ();
        }
        cx /= schoolmates.size();
        cy /= schoolmates.size();
        cz /= schoolmates.size();

        // COHESION: steer toward center of mass (gentle)
        double dx = cx - beast.getX();
        double dy = cy - beast.getY();
        double dz = cz - beast.getZ();
        double distSq = dx * dx + dy * dy + dz * dz;

        if (distSq > 4.0D) { // More than 2 blocks from center
            double dist = Math.sqrt(distSq);
            beast.setDeltaMovement(beast.getDeltaMovement().add(
                    (dx / dist) * cohesionStrength,
                    (dy / dist) * cohesionStrength * 0.5, // Weaker vertical pull
                    (dz / dist) * cohesionStrength));
            beast.hurtMarked = true;
        }

        // SEPARATION: avoid getting too close to any one schoolmate
        for (SpiritBeastEntity mate : schoolmates) {
            double sx = beast.getX() - mate.getX();
            double sy = beast.getY() - mate.getY();
            double sz = beast.getZ() - mate.getZ();
            double sDistSq = sx * sx + sy * sy + sz * sz;
            if (sDistSq < separationRadius * separationRadius && sDistSq > 0.01) {
                double sDist = Math.sqrt(sDistSq);
                double pushStrength = separationStrength * (1.0 - sDist / separationRadius);
                beast.setDeltaMovement(beast.getDeltaMovement().add(
                        (sx / sDist) * pushStrength,
                        (sy / sDist) * pushStrength * 0.3,
                        (sz / sDist) * pushStrength));
                beast.hurtMarked = true;
            }
        }

        // Set swimming pose
        beast.setSpiritPose(SpiritBeastEntity.POSE_SWIMMING);
    }

    private List<SpiritBeastEntity> findNearbySchoolmates() {
        return beast.level().getEntitiesOfClass(
                SpiritBeastEntity.class,
                beast.getBoundingBox().inflate(detectionRadius, detectionRadius * 0.5, detectionRadius),
                e -> e != beast
                        && e.isAlive()
                        && e.getBeastType() == SpiritBeastEntity.BeastType.SOUL_FISH
                        && e.isInWater());
    }
}
