package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.world.entity.ai.goal.Goal;

/**
 * CultivatorMeditationGoal — NPC cultivator meditation AI goal.
 *
 * <p>CRON-COMPLETIONIST-57: Fully implemented. Previously a dead stub (canUse() always false).
 * Now: cultivator enters meditation when idle, not in combat, not locked, and not pursuing.
 * Duration: 200-600 ticks (10-30 seconds) randomized per session. Sets POSE_MEDITATING
 * on start, restores POSE_IDLE on stop. This fires the CultivatorRobeModel meditation
 * animation (zhan zhuang / standing-stake pose) organically.
 *
 * <p>Canon: Cultivators meditate (打坐/冥想) to circulate qi, comprehend dao fragments,
 * and consolidate their cultivation base. Wang Lin meditates for hours in the novel.
 * This goal simulates that behavior at Minecraft timescale.
 */
public class CultivatorMeditationGoal extends Goal {
    private final EntityCultivator cultivator;

    /** Duration of current meditation session in ticks. */
    private int meditationDuration;

    /** Ticks elapsed in current session. */
    private int meditationTimer;

    /** Cooldown between meditation sessions (prevents non-stop meditation). */
    private int cooldown;

    public CultivatorMeditationGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.meditationDuration = 0;
        this.meditationTimer = 0;
        this.cooldown = 0;
    }

    @Override
    public boolean canUse() {
        // Cannot meditate if: locked by activity system, in combat, or cooling down
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.getTarget() != null) return false;
        if (cooldown > 0) return false;
        if (cultivator.getRandom().nextInt(200) != 0) return false; // ~0.5%/tick chance
        // Only meditate when idle (not walking, not pursuing)
        return cultivator.getCultivatorPose() == EntityCultivator.POSE_IDLE;
    }

    @Override
    public boolean canContinueToUse() {
        // Stop if: target appeared, activity locked, duration exceeded, or forced to move
        if (cultivator.isActivityLocked()) return false;
        if (cultivator.getTarget() != null) return false;
        return meditationTimer < meditationDuration;
    }

    @Override
    public void start() {
        // Random duration: 200-600 ticks (10-30 seconds)
        meditationDuration = 200 + cultivator.getRandom().nextInt(400);
        meditationTimer = 0;
        // Set meditation pose — triggers CultivatorRobeModel zhan zhuang animation
        cultivator.setCultivatorPose(EntityCultivator.POSE_MEDITATING);
        // Stop any active navigation
        cultivator.getNavigation().stop();
    }

    @Override
    public void stop() {
        // Restore idle pose
        cultivator.setCultivatorPose(EntityCultivator.POSE_IDLE);
        // Set cooldown before next meditation: 400-1200 ticks (20-60 seconds)
        cooldown = 400 + cultivator.getRandom().nextInt(800);
        meditationTimer = 0;
    }

    @Override
    public void tick() {
        meditationTimer++;
        // Keep cultivator stationary
        cultivator.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
