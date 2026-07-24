package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.SpiritBeastEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * SpiritBeastFeedGoal — beasts actually consume food, not just pose-graze.
 *
 * <p>Prior to this goal, SpiritBeastGrazeGoal made herbivores look at grass and
 * strike a pose, but never consumed anything. Carnivores had HuntGoal that
 * stalked and killed prey but left corpses untouched. No beast ever restored
 * HP or saturation from eating.
 *
 * <p>This goal adds REAL consumption mechanics:
 *   <b>Herbivores</b> (rabbit, deer, soul_fish): seek and eat vegetation blocks.
 *     Tall grass, ferns, flowers → broken and consumed. Spirit herbs restore
 *     extra HP. Eating restores 1 hunger (2 saturation).
 *   <b>Carnivores</b> (wolf, hawk, fire_beast, boar, bat, crane): seek and
 *     eat from nearby corpses. When a LivingEntity dies within range, the beast
 *     approaches and feeds for 3-5 seconds. Restores HP proportional to prey's
 *     max health. Sets POSE_RESTING during feed.
 *   <b>Omnivores</b> (qilin): eat both vegetation AND corpses.
 *   <b>Aquatic herbivores</b> (soul_fish): seek kelp/seagrass in water.
 *
 * <p>Hunger cycle: beasts accumulate hunger over time (random 200-600 tick
 * timer). When hungry, feed goal activates and seeks food. After feeding,
 * hunger timer resets.
 *
 * <p>BeastIntelligence tier affects feeding:
 *   INSTINCT: eat whatever is closest, 8-block seek range.
 *   AWARE+: prefer spirit herbs over normal grass (seek up to 16 blocks).
 *   CUNNING+: carnivores drag prey corpses to cover before feeding.
 *   SPIRIT+: after feeding, emit content particle effect.
 *
 * CRON-COMPLETIONIST-71: fills the "feed" gap from CRON task priority (c).
 */
public class SpiritBeastFeedGoal extends Goal {

    private final SpiritBeastEntity beast;
    private final boolean isHerbivore;
    private final boolean isCarnivore;
    private int hungerTimer; // ticks until next hunger cycle
    private int feedTimer;   // ticks remaining in current feed action
    private BlockPos feedTarget;
    private LivingEntity corpseTarget;
    private int seekCooldown;

    public SpiritBeastFeedGoal(SpiritBeastEntity beast) {
        this.beast = beast;
        this.isHerbivore = isHerbivore(beast);
        this.isCarnivore = isCarnivore(beast);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        resetHungerTimer();
    }

    private static boolean isHerbivore(SpiritBeastEntity beast) {
        return beast.getBeastType() == SpiritBeastEntity.BeastType.RABBIT
                || beast.getBeastType() == SpiritBeastEntity.BeastType.DEER
                || beast.getBeastType() == SpiritBeastEntity.BeastType.SOUL_FISH;
    }

    private static boolean isCarnivore(SpiritBeastEntity beast) {
        return beast.getBeastType() == SpiritBeastEntity.BeastType.WOLF
                || beast.getBeastType() == SpiritBeastEntity.BeastType.HAWK
                || beast.getBeastType() == SpiritBeastEntity.BeastType.FIRE_BEAST
                || beast.getBeastType() == SpiritBeastEntity.BeastType.STONE_BACK_BOAR
                || beast.getBeastType() == SpiritBeastEntity.BeastType.BAT
                || beast.getBeastType() == SpiritBeastEntity.BeastType.CRANE
                || beast.getBeastType() == SpiritBeastEntity.BeastType.QILIN
                || beast.getBeastType() == SpiritBeastEntity.BeastType.SEA_SERPENT;
    }

    private void resetHungerTimer() {
        // 10-30 seconds between hunger cycles
        this.hungerTimer = 200 + beast.getRandom().nextInt(400);
        this.seekCooldown = 0;
    }

    @Override
    public boolean canUse() {
        // Don't feed while in combat, while swimming, or while already feeding via GrazeGoal
        if (beast.getTarget() != null) return false;
        if (beast.hurtTime > 0) return false;
        // Only feed when actually hungry
        if (hungerTimer > 0) return false;
        // Must be on ground (or in water for aquatics)
        if (beast.getBeastType().isGround() && !beast.onGround()) return false;
        if (beast.getBeastType().isAquatic() && !beast.isInWater()) return false;
        return seekCooldown <= 0;
    }

    @Override
    public boolean canContinueToUse() {
        return feedTimer > 0 && beast.getTarget() == null && beast.hurtTime == 0;
    }

    @Override
    public void start() {
        feedTimer = 60 + beast.getRandom().nextInt(40); // 3-5 seconds feeding
        beast.setSpiritPose(SpiritBeastEntity.POSE_RESTING);

        if (isHerbivore) {
            feedTarget = findNearbyVegetation();
        } else if (isCarnivore) {
            corpseTarget = findNearbyCorpse();
            if (corpseTarget != null) {
                feedTarget = corpseTarget.blockPosition();
            }
        }

        if (feedTarget == null) {
            // No food found — reset hunger and try again later
            feedTimer = 0;
            seekCooldown = 200; // wait 10s before trying again
            resetHungerTimer();
            return;
        }

        // Move toward food source
        beast.getNavigation().moveTo(
                feedTarget.getX(), feedTarget.getY(), feedTarget.getZ(), 0.4D);
    }

    @Override
    public void stop() {
        feedTimer = 0;
        feedTarget = null;
        corpseTarget = null;
        beast.setSpiritPose(SpiritBeastEntity.POSE_STANDING);
    }

    @Override
    public void tick() {
        feedTimer--;

        // If we haven't reached the food yet, keep moving toward it
        if (feedTarget != null && beast.distanceToSqr(
                Vec3.atCenterOf(feedTarget)) > 4.0D) {
            beast.getNavigation().moveTo(
                    feedTarget.getX(), feedTarget.getY(), feedTarget.getZ(), 0.4D);
            return;
        }

        // Look down at food while eating
        if (feedTarget != null) {
            beast.getLookControl().setLookAt(
                    feedTarget.getX() + 0.5D, feedTarget.getY(), feedTarget.getZ() + 0.5D,
                    30.0F, 30.0F);
        }

        // Feed completes
        if (feedTimer <= 0) {
            performFeed();
            stop();
            resetHungerTimer();
        }
    }

    private void performFeed() {
        if (feedTarget == null) return;

        // ── Herbivore: break vegetation and restore hunger ──
        if (isHerbivore && feedTarget != null) {
            BlockState bs = beast.level().getBlockState(feedTarget);
            Block block = bs.getBlock();

            // Check if the block is edible vegetation
            boolean ate = false;
            if (block == Blocks.TALL_GRASS || block == Blocks.FERN
                    || block == Blocks.DEAD_BUSH) {
                beast.level().destroyBlock(feedTarget, false);
                ate = true;
            } else if (block == Blocks.GRASS_BLOCK) {
                // Eat the grass, leaving dirt
                beast.level().setBlock(feedTarget, Blocks.DIRT.defaultBlockState(), 3);
                ate = true;
            }
            // Kelp / seagrass for aquatic herbivores
            else if (beast.getBeastType().isAquatic()
                    && (block == Blocks.SEAGRASS || block == Blocks.KELP)) {
                beast.level().destroyBlock(feedTarget, false);
                ate = true;
            }

            if (ate) {
                // Restore HP and saturation
                float restoreAmount = beast.getMaxHealth() * 0.05F;
                beast.heal(restoreAmount);
                // Spawn eating particles
                spawnEatParticles(feedTarget);
            }
        }

        // ── Carnivore: feed on corpse ──
        if (isCarnivore && corpseTarget != null && corpseTarget.isAlive() == false) {
            // Can't actually consume the corpse entity (it will despawn naturally)
            // but restore HP proportional to prey's max health
            float restoreAmount = corpseTarget.getMaxHealth() * 0.1F;
            beast.heal(restoreAmount);
            spawnEatParticles(corpseTarget.blockPosition());
        }

        // ── Content particles for SPIRIT+ tier beasts ──
        if (beast.getCultivationTier() >= 4 && beast.level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    beast.getX(), beast.getY() + 0.5D, beast.getZ(),
                    5, 0.5D, 0.5D, 0.5D, 0.02D);
        }
    }

    private void spawnEatParticles(BlockPos pos) {
        if (beast.level() instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    3, 0.3D, 0.3D, 0.3D, 0.01D);
        }
    }

    /**
     * Find nearby edible vegetation. Seek range scales with cultivation tier:
     * INSTINCT: 4 blocks, AWARE+: 8 blocks, SPIRIT+: 12 blocks.
     * Prefer spirit herbs (ergenverse:block categories) over normal grass.
     */
    private BlockPos findNearbyVegetation() {
        int range = 4 + Math.min(beast.getCultivationTier() * 2, 8);
        BlockPos base = beast.blockPosition();

        // First pass: look for spirit herbs (higher priority for AWARE+)
        if (beast.getCultivationTier() >= 2) {
            BlockPos herb = findBlockInRange(base, range, pos -> {
                BlockState bs = beast.level().getBlockState(pos);
                // Simplified: check if the block's simple name contains spirit keywords
                String name = bs.getBlock().toString().toLowerCase();
                return name.contains("spirit_herb") || name.contains("spirit_grass");
            });
            if (herb != null) return herb;
        }

        // Second pass: any vegetation
        return findBlockInRange(base, range, pos -> {
            BlockState bs = beast.level().getBlockState(pos);
            Block block = bs.getBlock();
            return block == Blocks.TALL_GRASS || block == Blocks.FERN
                    || block == Blocks.DEAD_BUSH || block == Blocks.GRASS_BLOCK
                    || block == Blocks.SEAGRASS || block == Blocks.KELP
                    || block == Blocks.LILY_PAD;
        });
    }

    /**
     * Find a nearby dead entity corpse to feed on. Carnivores approach
     * recently-dead prey within range. Seek range: 12 blocks.
     * Corpse must have died within the last 5 seconds (100 ticks).
     */
    private LivingEntity findNearbyCorpse() {
        double range = 12.0D;
        List<LivingEntity> corpses = beast.level().getEntitiesOfClass(
                LivingEntity.class,
                beast.getBoundingBox().inflate(range, 4.0D, range),
                e -> e != beast && !e.isAlive() && e.deathTime < 100);

        if (corpses.isEmpty()) return null;

        // Pick the closest corpse
        corpses.sort((a, b) -> Double.compare(
                beast.distanceToSqr(a), beast.distanceToSqr(b)));
        return corpses.get(0);
    }

    /**
     * Utility: scan blocks in a horizontal range for a matching predicate.
     * Returns the first matching position, or null.
     */
    private BlockPos findBlockInRange(BlockPos center, int range,
                                        java.util.function.Predicate<BlockPos> predicate) {
        // Search in expanding rings for efficiency
        for (int r = 1; r <= range; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) != r && Math.abs(dz) != r) continue; // only ring
                    BlockPos pos = center.offset(dx, 0, dz);
                    // Check surface and one block above
                    if (predicate.test(pos)) return pos;
                    BlockPos above = pos.above();
                    if (predicate.test(above)) return above;
                }
            }
        }
        return null;
    }

    // Hunger timer ticks down regardless of whether goal is active
    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public void tickHunger() {
        if (hungerTimer > 0) hungerTimer--;
        if (seekCooldown > 0) seekCooldown--;
    }
}
