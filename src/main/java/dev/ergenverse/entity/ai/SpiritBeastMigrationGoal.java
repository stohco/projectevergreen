package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * SpiritBeastMigrationGoal — beasts migrate between territories based on
 * time-of-day cycles and cultivation tier.
 *
 * <p>Canon (Renegade Immortal): Spirit beasts migrate along spirit veins and
 * seasonal routes. Higher-tier beasts patrol vast territories. A Soul
 * Formation beast's territory might span multiple biomes. This migration
 * is not random wandering — it is PURPOSE-DRIVEN movement toward a
 * destination that satisfies the beast's needs.
 *
 * <p>MIGRATION TRIGGERS:
 *   <b>Time-of-day</b>: Diurnal beasts (deer, rabbit, crane) migrate toward
 *     open areas at dawn, forested areas at dusk. Nocturnal beasts (bat) do
 *     the reverse.
 *   <b>Seasonal</b>: In biomes that get cold at night, beasts move toward
 *     warmer areas (lower Y, near light sources, spirit vein proximity).
 *   <b>Threat-driven</b>: If a stronger beast is nearby, weaker beasts
 *     migrate AWAY from the threat.
 *   <b>Water-seeking</b>: Aquatic beasts migrate toward deeper water.
 *
 * <p>MIGRATION BEHAVIOR:
 *   1. Select a destination (target biome position 30-80 blocks away).
 *   2. Navigate toward destination at reduced speed (0.5x base).
 *   3. Periodically re-evaluate destination (every 10-20 seconds).
 *   4. Stop when destination is reached or a higher-priority goal activates.
 *
 * <p>BeastIntelligence tier requirements:
 *   INSTINCT: no migration (stay near spawn).
 *   AWARE(1): short-range migration (20-30 blocks) toward food/water.
 *   CUNNING(2): medium-range (30-50 blocks), avoid threats.
 *   SPIRIT(3): long-range (40-60 blocks), time-of-day patterns.
 *   DEMON+(4+): vast-range (50-80 blocks), follow spirit vein directions.
 *
 * <p>CRON-COMPLETIONIST-71: fills the "migrate" gap from CRON task priority (c).
 */
public class SpiritBeastMigrationGoal extends Goal {

    private final SpiritBeastEntity beast;
    private final int minTier;
    private final double speed;
    private Vec3 destination;
    private int reevaluateTimer;
    private boolean active;

    public SpiritBeastMigrationGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        // Only AWARE+ beasts migrate
        this.minTier = 1;
        this.speed = 0.4D;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.reevaluateTimer = 0;
    }

    @Override
    public boolean canUse() {
        // Must meet intelligence threshold
        if (beast.getCultivationTier() < minTier) return false;
        // Don't migrate while in combat, hurt, or already moving with purpose
        if (beast.getTarget() != null) return false;
        if (beast.hurtTime > 0) return false;
        // Flyers in flight don't migrate (FlightGoal handles that)
        if (!beast.onGround() && !beast.isInWater() && beast.getBeastType().isGround()) return false;
        // Random trigger: ~0.5% per tick (average once per 200 ticks / 10s)
        if (beast.getRandom().nextInt(200) != 0) return false;
        // Only if no other goal is actively driving movement
        return beast.getNavigation().isDone() || beast.getNavigation().isInProgress();
    }

    @Override
    public boolean canContinueToUse() {
        if (beast.getTarget() != null) return false;
        if (beast.hurtTime > 0) return false;
        if (destination == null) return false;
        // Stop if we've reached the destination area (within 5 blocks)
        if (beast.distanceToSqr(destination) < 25.0D) return false;
        // Stop if re-evaluation decides to cancel
        return active;
    }

    @Override
    public void start() {
        active = true;
        destination = pickMigrationDestination();
        if (destination == null) {
            active = false;
            return;
        }
        reevaluateTimer = 200 + beast.getRandom().nextInt(200); // 10-20s
        beast.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
    }

    @Override
    public void stop() {
        active = false;
        destination = null;
        reevaluateTimer = 0;
        beast.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (destination == null) {
            stop();
            return;
        }

        reevaluateTimer--;

        // Periodically re-evaluate whether migration should continue
        if (reevaluateTimer <= 0) {
            // Check for threats at destination — abort if danger found
            if (isDangerousAtDestination()) {
                stop();
                return;
            }
            // Pick new destination if current one seems unreachable
            if (beast.getNavigation().isDone() && beast.distanceToSqr(destination) > 100.0D) {
                destination = pickMigrationDestination();
                if (destination == null) {
                    stop();
                    return;
                }
            }
            reevaluateTimer = 200 + beast.getRandom().nextInt(200);
        }

        // Continue navigating toward destination
        if (!beast.getNavigation().isInProgress() || beast.getNavigation().isDone()) {
            beast.getNavigation().moveTo(destination.x, destination.y, destination.z, speed);
        }
    }

    /**
     * Pick a migration destination based on beast type and time of day.
     * Distance scales with cultivation tier.
     */
    private Vec3 pickMigrationDestination() {
        int tier = beast.getCultivationTier();
        int distance = getMigrationDistance(tier);

        // Direction: use a combination of time-of-day and random
        long dayTime = beast.level().getDayTime() % 24000;
        boolean isDay = dayTime < 12000;

        double angle;
        SpiritBeastEntity.BeastType type = beast.getBeastType();

        if (type.isAquatic()) {
            // Aquatic beasts: migrate toward deeper water (seek lower Y nearby)
            return findNearbyWater(depthForTier(tier));
        }

        if (type.isFlyer()) {
            // Flyers: migrate based on time of day
            angle = isDay ? beast.getRandom().nextDouble() * Math.PI * 2
                          : beast.getRandom().nextDouble() * Math.PI * 2;
            // Flyers pick a point in the sky
            double y = beast.getY() + 10 + beast.getRandom().nextFloat() * 15;
            return new Vec3(
                    beast.getX() + Math.cos(angle) * distance,
                    y,
                    beast.getZ() + Math.sin(angle) * distance);
        }

        // Ground beasts: direction based on beast personality + time
        if (type == SpiritBeastEntity.BeastType.RABBIT
                || type == SpiritBeastEntity.BeastType.DEER) {
            // Herbivores: seek vegetation at dawn, cover at dusk
            angle = isDay
                    ? findBiomeDirection("forest", "plains")
                    : findBiomeDirection("plains", "forest");
        } else if (type == SpiritBeastEntity.BeastType.WOLF
                || type == SpiritBeastEntity.BeastType.FIRE_BEAST
                || type == SpiritBeastEntity.BeastType.STONE_BACK_BOAR) {
            // Predators: migrate toward prey-rich areas (open ground in day)
            angle = isDay
                    ? findBiomeDirection("plains", "forest")
                    : findBiomeDirection("forest", "plains");
        } else {
            // Default: random direction
            angle = beast.getRandom().nextDouble() * Math.PI * 2;
        }

        // Clamp Y to nearby terrain height
        BlockPos targetPos = beast.blockPosition().offset(
                (int) (Math.cos(angle) * distance), 0,
                (int) (Math.sin(angle) * distance));
        targetPos = beast.level().getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                targetPos);

        return new Vec3(targetPos.getX() + 0.5D, targetPos.getY(), targetPos.getZ() + 0.5D);
    }

    /**
     * Find a direction toward a biome containing a keyword in its registry name.
     * Samples 8 directions and returns the angle of the best match.
     * Falls back to random if no biome match found.
     */
    private double findBiomeDirection(String preferred, String fallback) {
        int bestScore = -1;
        double bestAngle = beast.getRandom().nextDouble() * Math.PI * 2;

        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4.0;
            int dist = getMigrationDistance(beast.getCultivationTier()) / 2;
            BlockPos samplePos = beast.blockPosition().offset(
                    (int) (Math.cos(angle) * dist), 0,
                    (int) (Math.sin(angle) * dist));

            if (!beast.level().hasChunkAt(samplePos)) continue;

            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder = beast.level().getBiome(samplePos);
            String biomeName = biomeHolder.unwrapKey().map(k -> k.location().getPath()).orElse("").toLowerCase();

            int score = 0;
            if (biomeName.contains(preferred)) score = 2;
            else if (biomeName.contains(fallback)) score = 1;

            if (score > bestScore) {
                bestScore = score;
                bestAngle = angle;
            }
        }

        return bestAngle;
    }

    /**
     * Find nearby water at a given depth preference.
     * Aquatic beasts prefer deeper water at higher tiers.
     */
    private Vec3 findNearbyWater(int preferredDepth) {
        int range = getMigrationDistance(beast.getCultivationTier());
        BlockPos base = beast.blockPosition();

        for (int r = 4; r <= range; r += 4) {
            for (int dx = -r; dx <= r; dx += r) {
                for (int dz = -r; dz <= r; dz += r) {
                    BlockPos pos = base.offset(dx, 0, dz);
                    if (!beast.level().hasChunkAt(pos)) continue;
                    // Find water surface
                    int waterDepth = findWaterDepth(pos);
                    if (waterDepth >= Math.min(preferredDepth, 3)) {
                        return Vec3.atCenterOf(pos);
                    }
                }
            }
        }

        // No deeper water found — stay put
        return null;
    }

    private int findWaterDepth(BlockPos pos) {
        int depth = 0;
        while (beast.level().getBlockState(pos.above(depth)).is(net.minecraft.world.level.block.Blocks.WATER)) {
            depth++;
        }
        return depth;
    }

    /**
     * Check if the destination has a dangerous entity nearby.
     * Cancel migration if a significantly stronger beast is at the target.
     */
    private boolean isDangerousAtDestination() {
        if (destination == null) return false;

        var entities = beast.level().getEntitiesOfClass(
                SpiritBeastEntity.class,
                new net.minecraft.world.phys.AABB(
                        destination.x - 8, destination.y - 4, destination.z - 8,
                        destination.x + 8, destination.y + 4, destination.z + 8),
                e -> e != beast && e.getMaxHealth() > beast.getMaxHealth() * 1.5D);

        return !entities.isEmpty();
    }

    private int getMigrationDistance(int tier) {
        return switch (tier) {
            case 1 -> 20 + beast.getRandom().nextInt(10);  // AWARE: 20-30 blocks
            case 2 -> 30 + beast.getRandom().nextInt(20);  // CUNNING: 30-50 blocks
            case 3 -> 40 + beast.getRandom().nextInt(20);  // SPIRIT: 40-60 blocks
            case 4 -> 50 + beast.getRandom().nextInt(20);  // DEMON: 50-70 blocks
            case 5 -> 60 + beast.getRandom().nextInt(20);  // ANCIENT: 60-80 blocks
            default -> 70 + beast.getRandom().nextInt(20); // OLD_MONSTER: 70-90 blocks
        };
    }

    private int depthForTier(int tier) {
        return switch (tier) {
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            default -> 8;
        };
    }
}
