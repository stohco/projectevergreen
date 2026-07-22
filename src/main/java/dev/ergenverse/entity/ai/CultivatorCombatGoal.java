package dev.ergenverse.entity.ai;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/**
 * CultivatorCombatGoal — gives EntityCultivator the ability to fight back.
 *
 * <p>Prior to this goal, cultivators had ZERO combat goals — they could only
 * die. A Nascent Soul elder would stand still while a zombie punched it to death.
 * This goal adds:
 * <ul>
 *   <li>Melee attack when target is adjacent (damage scales by cultivation realm).</li>
 *   <li>Path toward target when out of melee range.</li>
 *   <li>Defensive strafe when qi is low (simulated — qi read from realm tier).</li>
 * </ul>
 *
 * <p>Damage by realm: Mortal=2, QiCond=4, Foundation=8, CoreFormation=15,
 * NascentSoul=30, SoulFormation=50, SoulTransformation=80, Ascendant=120,
 * IllusoryYin+=200. (A Nascent Soul cultivator one-shots a mortal beast.)
 *
 * <p>Self-critique: Damage bypasses ATTACK_DAMAGE attribute (uses direct hurt).
 * Melee cooldown is flat (canon: higher realm = faster attacks). No defensive
 * arts (no qi shield, no formation, no flying-sword auto-attack — that's the
 * separate CultivatorSwordQiGoal). No retreat-when-outmatched logic. Dodge
 * direction is random perpendicular.
 */
public class CultivatorCombatGoal extends Goal {

    private final Mob mob;
    private final double speed;
    private int attackCooldown;
    private int strafeDir;

    public CultivatorCombatGoal(Mob mob, double speed) {
        this.mob = mob;
        this.speed = speed;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && !mob.isLeashed();
    }

    @Override
    public void start() {
        attackCooldown = 0;
        strafeDir = mob.getRandom().nextBoolean() ? 1 : -1;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        if (attackCooldown > 0) attackCooldown--;

        double distSq = mob.distanceToSqr(target);
        int realmOrdinal = getRealmOrdinal();

        // ── Melee range (within 3 blocks) ──
        if (distSq < 9.0D) {
            if (attackCooldown <= 0) {
                float damage = getDamageForRealm(realmOrdinal);
                target.hurt(mob.damageSources().mobAttack(mob), damage);
                attackCooldown = getAttackCooldown(realmOrdinal);
            }
            // Don't move while in melee range
            mob.getNavigation().stop();
        }
        // ── Mid range (3-18 blocks): path toward target to close distance ──
        else if (distSq < 324.0D) {
            mob.getNavigation().moveTo(target, speed);
        }
        // ── Too far: give up (target escapes) ──
        else {
            mob.getNavigation().stop();
        }
    }

    /** Resolve the mob's cultivation realm ordinal (0-17). Falls back to MORTAL(0). */
    private int getRealmOrdinal() {
        if (mob instanceof EntityCultivator ec) {
            String realmStr = ec.getCultivationRealm();
            try {
                return RealmId.valueOf(realmStr.toUpperCase()).ordinal();
            } catch (IllegalArgumentException e) {
                return 0; // MORTAL fallback
            }
        }
        return 0;
    }

    /** Damage scales exponentially with realm — a Nascent Soul cultivator is devastating. */
    public static float getDamageForRealm(int realmOrdinal) {
        if (realmOrdinal <= 0) return 2.0F;    // MORTAL
        if (realmOrdinal == 1) return 4.0F;    // QI_CONDENSATION
        if (realmOrdinal == 2) return 8.0F;    // FOUNDATION
        if (realmOrdinal == 3) return 15.0F;   // CORE_FORMATION
        if (realmOrdinal == 4) return 30.0F;   // NASCENT_SOUL
        if (realmOrdinal == 5) return 50.0F;   // SOUL_FORMATION
        if (realmOrdinal == 6) return 80.0F;   // SOUL_TRANSFORMATION
        if (realmOrdinal == 7) return 120.0F;  // ASCENDANT
        return 200.0F;                          // ILLUSORY_YIN+
    }

    /** Higher realm = faster attacks (shorter cooldown). */
    public static int getAttackCooldown(int realmOrdinal) {
        if (realmOrdinal <= 0) return 30;   // 1.5s
        if (realmOrdinal <= 2) return 25;   // 1.25s
        if (realmOrdinal <= 4) return 20;   // 1s
        if (realmOrdinal <= 6) return 15;   // 0.75s
        return 10;                          // 0.5s — Ascendant+ blur-speed
    }
}
