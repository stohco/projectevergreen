package dev.ergenverse.item.sword;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * SwordEffectType — the unique supernatural effect each flying sword applies on hit.
 *
 * <p>Canon: In Er Gen's works, each notable flying sword has a distinct
 * supernatural property beyond mere damage:
 * <ul>
 *   <li><b>Wealth Flying Sword</b> (Wang Lin's first) — a basic qi blade, no special effect.</li>
 *   <li><b>Core Treasure Sword</b> (剑丸) — can teleport targets, displacing enemies.</li>
 *   <li><b>Blood Slaughter Sword</b> (嗜血剑) — drains the life force of those it cuts.</li>
 *   <li><b>Dark Green Flying Sword</b> (墨绿剑) — seeps poison into wounds.</li>
 *   <li><b>God-Slaying Sword</b> (诛仙剑) — ignores defenses, restriction-tier power.</li>
 * </ul>
 *
 * <p>Each effect is applied via {@link SwordEffectHelper} from the projectile's
 * hit callback. The effect type is stored in the sword item's NBT as a string.
 */
public enum SwordEffectType {

    /** No special effect — the basic Wealth Flying Sword. */
    NONE("none", "No special effect") {
        @Override
        public void applyOnHit(ServerLevel level, Entity target, Player attacker, float damage) {
            // No effect to apply
        }
    },

    /** Core Treasure Sword — teleport the target 5 blocks in a random direction. */
    TELEPORT("teleport", "Teleports the target on hit") {
        @Override
        public void applyOnHit(ServerLevel level, Entity target, Player attacker, float damage) {
            if (!(target instanceof LivingEntity living)) return;
            RandomSource rng = level.random;
            // Random direction on the XZ plane
            double angle = rng.nextDouble() * Math.PI * 2;
            Vec3 offset = new Vec3(Math.cos(angle) * 5, 0, Math.sin(angle) * 5);
            Vec3 newPos = target.position().add(offset);
            // Find a safe Y position (don't teleport into the ground)
            newPos = new Vec3(newPos.x, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                    (int) Math.floor(newPos.x), (int) Math.floor(newPos.z)), newPos.z);
            living.teleportTo(newPos.x, newPos.y, newPos.z);
            if (attacker instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The Core Treasure Sword displaces your enemy!"));
            }
        }
    },

    /** Blood Slaughter Sword — heal attacker for 30% of damage dealt. */
    LIFESTEAL("lifesteal", "Heals the attacker for 30% of damage dealt") {
        @Override
        public void applyOnHit(ServerLevel level, Entity target, Player attacker, float damage) {
            if (attacker == null || !attacker.isAlive()) return;
            float healAmount = damage * 0.3F;
            attacker.heal(healAmount);
        }
    },

    /** Dark Green Flying Sword — apply Wither II for 3 seconds. */
    POISON("poison", "Applies Wither II for 3 seconds") {
        @Override
        public void applyOnHit(ServerLevel level, Entity target, Player attacker, float damage) {
            if (!(target instanceof LivingEntity living)) return;
            living.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1)); // 3s × 20 ticks, amplifier 1 = Wither II
        }
    },

    /** God-Slaying Sword — ignore 50% of target's armor (applied via bonus damage). */
    RESTRICTION("restriction", "Ignores 50% of target's armor") {
        @Override
        public void applyOnHit(ServerLevel level, Entity target, Player attacker, float damage) {
            if (!(target instanceof LivingEntity living)) return;
            // Armor reduction effect: apply additional damage proportional to target's armor
            float armorValue = living.getArmorValue();
            float bonusDamage = armorValue * 0.25F; // 50% armor bypass ≈ extra damage equivalent to half armor value
            living.hurt(level.damageSources().magic(), bonusDamage);
            if (attacker instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "The God-Slaying Sword pierces through armor!"));
            }
        }
    };

    private final String name;
    private final String description;

    SwordEffectType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /** The serializable name (stored in NBT). */
    public String getName() {
        return name;
    }

    /** Human-readable description. */
    public String getDescription() {
        return description;
    }

    /**
     * Apply this effect when the flying sword hits a target.
     *
     * @param level    the server level
     * @param target   the entity hit by the projectile
     * @param attacker the player who launched the sword (may be null)
     * @param damage   the damage dealt by the projectile hit
     */
    public abstract void applyOnHit(ServerLevel level, Entity target,
                                    @Nullable Player attacker, float damage);

    /**
     * Look up a SwordEffectType by its serializable name.
     * Returns NONE if the name is not recognized.
     */
    public static SwordEffectType byName(String name) {
        for (SwordEffectType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        Ergenverse.LOGGER.warn("[Ergenverse] Unknown SwordEffectType '{}', defaulting to NONE", name);
        return NONE;
    }
}
