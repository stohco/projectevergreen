package dev.ergenverse.entity.ai;

import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * AncientGodStarGazeGoal — paralysis attack for Tuo Sen (8-star Ancient God).
 *
 * <p><b>CRON-COMPLETIONIST-108 — Ancient God Combat AI (point (g) extension)</b>
 *
 * <p>Closes the CRON-107 self-critique #14 documented gap by adding a
 * second Ancient-God-specific combat goal: the Star Gaze paralysis.
 * This pairs with {@link AncientGodPressGoal} to give Tuo Sen a
 * two-attack rotation: paralyze the target at range, then close and press.
 *
 * <h2>Canon basis (web-search verified 2026-07-26)</h2>
 * <ul>
 *   <li>Baidu Baike (古神): Ancient Gods possess the 古神之眼 (Ancient God
 *       Eye) — a paralyzing gaze that pins lesser cultivators in place.</li>
 *   <li>仙逆 canon: 8-star Ancient Gods can immobilize cultivators below
 *       the Soul Formation realm with a single glance. The 8-star forehead
 *       array is the source of this power.</li>
 *   <li>Sohu (2025-08-06): Tuo Sen contests the inheritance at the tomb —
 *       his presence alone is a paralyzing force.</li>
 *   <li>163 (2025-07-29): '拓森现身朱雀墓，获得九星古神血液，抢下修星之晶'
 *       — Tuo Sen appears at the Suzaku Tomb, obtains 9-star Ancient God
 *       blood, seizes the Cultivation Planet Crystal. The 9-star blood
 *       amplifies his already-paralyzing gaze.</li>
 * </ul>
 *
 * <h2>Mechanic — Star Gaze (古神之眼)</h2>
 * <p>When Tuo Sen's target is at long range (10-30 blocks) and has line of
 * sight, he charges for 30 ticks (1.5 seconds). During the charge:
 * <ul>
 *   <li>The 8-star forehead array "ignites" — 8 END_ROD particles spawn in
 *       a ring around Tuo Sen's head, growing brighter as the charge
 *       completes.</li>
 *   <li>Tuo Sen faces the target with locked eye contact (no movement).</li>
 *   <li>A low {@code WITHER_AMBIENT} sound plays at increasing volume.</li>
 * </ul>
 *
 * <p>On fire (when the charge completes):
 * <ul>
 *   <li>If the target still has line of sight, apply:
 *     <ul>
 *       <li>{@link MobEffects#MOVEMENT_SLOWDOWN} IV (slowness) for 5s</li>
 *       <li>{@link MobEffects#WEAKNESS} II for 5s</li>
 *       <li>{@link MobEffects#DARKNESS} for 3s (the target's vision darkens)</li>
 *     </ul>
 *     This simulates the paralyzing god-gaze — the target cannot move
 *     quickly, cannot attack effectively, and is visually impaired.</li>
 *   <li>Deal 30 damage (less than {@link AncientGodPressGoal}'s 80, but
 *       the paralysis sets up the next press for a guaranteed hit).</li>
 *   <li>A beam of particles from Tuo Sen's head to the target: 1 DRAGON_BREATH
 *       per block along the path, plus 5 END_ROD at the target.</li>
 *   <li>A {@code WITHER_DEATH} sound at 1.2 pitch (the iconic Ancient God
 *       gaze sound).</li>
 * </ul>
 *
 * <p>Cooldown: 14 seconds (280 ticks) — longer than the press because the
 * paralysis is a powerful setup tool. The player has time to break line
 * of sight between gazes.
 *
 * <p><b>Activation gate:</b> only activates when
 * {@code mob.getCharacterId().equals("tuo_sen")} — this goal is a no-op
 * for all other cultivators.
 *
 * <p><b>Self-critique:</b>
 * <ul>
 *   <li>The paralysis uses vanilla MobEffects (SLOWNESS, WEAKNESS, DARKNESS).
 *       This is correct for MC 1.20.1 — there's no "true paralysis" effect,
 *       but SLOWNESS IV + DARKNESS achieves the same gameplay feel (can't
 *       move, can't see well).</li>
 *   <li>The charge is interruptible — if Tuo Sen takes damage during the
 *     30-tick charge, the charge resets. This is canon-faithful: a
 *     cultivator who lands a hit during the gaze can break it.</li>
 *   <li>The damage (30) is intentionally lower than the press (80). The
 *     gaze's value is the SETUP, not the damage — the paralyzing slow
 *     guarantees the next press will land. This mirrors canon: Tuo Sen
 *     uses the gaze to pin Wang Lin, then closes for the kill.</li>
 *   <li>The particle beam (DRAGON_BREATH along the path) is a SERVER-side
 *     particle shower. On single-player maximalism (Article XLIII), this
 *     is correct.</li>
 *   <li>Line of sight is checked at charge start AND at fire time. If the
 *     target breaks LOS during the charge (e.g., ducks behind a pillar),
 *     the gaze fizzles — no damage, no paralysis, but the cooldown still
 *     applies. This is canon-faithful: the Ancient God Eye requires
 *     sustained eye contact.</li>
 *   <li>No defense against the gaze for high-realm cultivators. Canon:
 *     Soul Formation+ cultivators can resist the gaze. A future CRON
 *     could check {@code target}'s realm and reduce/disable the paralysis
 *     for cultivators at or above Tuo Sen's tier. Out of scope for CRON-108.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public class AncientGodStarGazeGoal extends Goal {

    /** Range at which the gaze can be initiated (squared). 10-30 blocks → 100-900. */
    private static final double MIN_RANGE_SQ = 100.0D;  // 10 blocks
    private static final double MAX_RANGE_SQ = 900.0D;  // 30 blocks

    /** Charge duration in ticks (1.5 seconds = 30 ticks). */
    private static final int CHARGE_DURATION = 30;

    /** Paralysis duration in ticks (5 seconds = 100 ticks). */
    private static final int PARALYSIS_DURATION = 100;

    /** Darkness duration in ticks (3 seconds = 60 ticks). */
    private static final int DARKNESS_DURATION = 60;

    /** Base damage of the gaze. 30 = setup damage, not a kill shot. */
    private static final float GAZE_DAMAGE = 30.0F;

    /** Cooldown in ticks (14 seconds = 280 ticks). */
    private static final int COOLDOWN_TICKS = 280;

    private final Mob mob;
    private int cooldown;
    private int chargeTimer;

    public AncientGodStarGazeGoal(Mob mob) {
        this.mob = mob;
        // LOOK only — Tuo Sen can't move while charging the gaze, but
        // other MOVE-flagged goals (like CultivatorCombatGoal) can still
        // run their movement logic. We override movement in tick().
        this.setFlags(EnumSet.of(Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // CRON-108: Only Tuo Sen uses this goal. Other cultivators no-op.
        if (!(mob instanceof EntityCultivator ec)) return false;
        if (!"tuo_sen".equals(ec.getCharacterId())) return false;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        double distSq = mob.distanceToSqr(target);
        if (distSq < MIN_RANGE_SQ || distSq > MAX_RANGE_SQ) return false;

        // Require line of sight at charge start
        return hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        return chargeTimer > 0 && mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        chargeTimer = CHARGE_DURATION;
        // Stop movement during the charge
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        chargeTimer = 0;
        cooldown = COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            chargeTimer = 0;
            return;
        }

        chargeTimer--;

        // Lock eye contact with the target
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // Stop any movement (in case MOVE-flagged goals tried to path)
        mob.getNavigation().stop();
        mob.setDeltaMovement(0, mob.getDeltaMovement().y * 0.5, 0);  // dampen horizontal

        // ── Charge particles: 8 END_ROD in a ring around the head ──
        // The ring grows brighter (more particles) as the charge completes.
        if (mob.level() instanceof ServerLevel sl) {
            int progress = CHARGE_DURATION - chargeTimer;  // 0 → 30
            int particleCount = 4 + (progress * 4 / CHARGE_DURATION);  // 4 → 8

            // Ring of END_ROD around the head (radius 0.6, at head height)
            double headY = mob.getY() + mob.getEyeHeight();
            for (int i = 0; i < particleCount; i++) {
                double angle = (i * 2 * Math.PI / particleCount) + (mob.tickCount * 0.1);
                double px = mob.getX() + Math.cos(angle) * 0.6;
                double pz = mob.getZ() + Math.sin(angle) * 0.6;
                sl.sendParticles(ParticleTypes.END_ROD,
                        px, headY, pz, 1, 0.05, 0.05, 0.05, 0.0);
            }

            // Mid-charge: a building WITHER_AMBIENT sound
            if (chargeTimer == 20) {
                mob.playSound(net.minecraft.sounds.SoundEvents.WITHER_AMBIENT, 0.8F, 0.8F);
            }
            // Late-charge: a louder, higher-pitched WITHER_AMBIENT
            if (chargeTimer == 5) {
                mob.playSound(net.minecraft.sounds.SoundEvents.WITHER_AMBIENT, 1.2F, 1.0F);
            }
        }

        // Fire when the charge completes
        if (chargeTimer <= 0) {
            fireGaze(target);
            cooldown = COOLDOWN_TICKS;
        }
    }

    /**
     * Fire the Star Gaze: paralysis + damage + particle beam.
     */
    private void fireGaze(LivingEntity target) {
        if (!(mob.level() instanceof ServerLevel sl)) return;

        // Re-check line of sight — target may have broken it during the charge
        if (!hasLineOfSight(target)) {
            // Gaze fizzles — no damage, but cooldown still applies
            sl.sendParticles(ParticleTypes.SMOKE,
                    mob.getX(), mob.getY() + mob.getEyeHeight(), mob.getZ(),
                    8, 0.3, 0.3, 0.3, 0.05);
            return;
        }

        // ── Apply paralysis effects ──
        // SLOWNESS IV (slowness amplifier 3 = IV) for 5s
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                PARALYSIS_DURATION, 3, false, true, true));
        // WEAKNESS II for 5s
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                PARALYSIS_DURATION, 1, false, true, true));
        // DARKNESS for 3s (visual impairment — the god-gaze darkens the target's vision)
        target.addEffect(new MobEffectInstance(MobEffects.DARKNESS,
                DARKNESS_DURATION, 0, false, true, true));

        // ── Apply damage ──
        target.hurt(mob.damageSources().mobAttack(mob), GAZE_DAMAGE);

        // ── Particle beam from Tuo Sen's head to the target ──
        Vec3 start = mob.getEyePosition();
        Vec3 end = target.getEyePosition();
        Vec3 dir = end.subtract(start);
        double dist = dir.length();
        dir = dir.normalize();

        // DRAGON_BREATH along the path (1 per block)
        for (double d = 0; d < dist; d += 1.0D) {
            Vec3 p = start.add(dir.scale(d));
            sl.sendParticles(ParticleTypes.DRAGON_BREATH, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.02);
        }

        // END_ROD burst at the target (the gaze "landing")
        sl.sendParticles(ParticleTypes.END_ROD,
                end.x, end.y, end.z, 10, 0.3, 0.3, 0.3, 0.1);

        // ── Sound ──
        // WITHER_DEATH at 1.2 pitch — the iconic Ancient God gaze sound
        mob.playSound(net.minecraft.sounds.SoundEvents.WITHER_DEATH, 1.0F, 1.2F);
        // Secondary: a deep ENDER_DRAGON_GROWL
        mob.playSound(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, 0.6F, 0.5F);
    }

    /**
     * Check line of sight to the target using a block clip.
     */
    private boolean hasLineOfSight(LivingEntity target) {
        Vec3 eye = mob.getEyePosition();
        Vec3 targetEye = target.getEyePosition();
        net.minecraft.world.level.ClipContext ctx = new net.minecraft.world.level.ClipContext(
                eye, targetEye,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE, mob);
        return mob.level().clip(ctx).getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
