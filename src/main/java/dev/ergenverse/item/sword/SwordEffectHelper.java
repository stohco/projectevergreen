package dev.ergenverse.item.sword;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

/**
 * SwordEffectHelper — static utility for applying sword effects on projectile hit.
 *
 * <p>Called from {@link dev.ergenverse.entity.FlyingSwordProjectileEntity#onHitEntity}
 * after the base damage is dealt. Reads the sword effect type from the projectile's
 * stored NBT data and delegates to {@link SwordEffectType#applyOnHit}.
 */
public final class SwordEffectHelper {

    private SwordEffectHelper() {}

    /**
     * Apply a sword effect to the hit entity.
     *
     * @param level     the server level
     * @param target    the entity that was hit
     * @param attacker  the player who launched the sword (may be null)
     * @param damage    the damage dealt by the projectile
     * @param effectType the sword effect to apply
     */
    public static void applyEffect(ServerLevel level, Entity target,
                                     @Nullable Player attacker, float damage,
                                     SwordEffectType effectType) {
        if (effectType == null || effectType == SwordEffectType.NONE) return;
        if (level == null || target == null || !target.isAlive()) return;

        try {
            effectType.applyOnHit(level, target, attacker, damage);
            Ergenverse.LOGGER.debug("[Ergenverse] SwordEffect {} applied to {} (damage={})",
                    effectType.getName(), target.getName().getString(), damage);
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] Error applying sword effect {}",
                    effectType.getName(), e);
        }
    }
}
