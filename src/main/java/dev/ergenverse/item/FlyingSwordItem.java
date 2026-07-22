package dev.ergenverse.item;

import dev.ergenverse.entity.FlyingSwordProjectileEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * FlyingSwordItem — a flying sword (飞剑) with real mechanics.
 *
 * <p>Extends SwordItem so left-click melee works (standard sword damage).
 * Right-click launches a FlyingSwordProjectile toward the player's look direction —
 * a streaking blade of qi that homes on the player's last-hurt target, deals damage,
 * and returns to the owner.
 *
 * <p>Canon: flying swords are the signature weapon of cultivators. Wang Lin's
 * Wealth Flying Sword, Core Treasure Sword, Dark Green Flying Sword, Blood
 * Slaughter Sword — each is a sentient weapon that flies out and returns.
 *
 * <p>Self-critique: Flat 8.0 projectile damage (should scale with sword tier ×
 * user realm × sword intent). Sword-spirit NBT is just a cooldown discount, not
 * the canon sentient entity (Jufu, Xu Liguo). No per-sword effects (Dark Green
 * poison, Core-Treasure teleport, God-Slaying armor-bypass). Uses Tiers.IRON
 * as a placeholder tier — should use a custom spirit-iron tier.
 */
public class FlyingSwordItem extends SwordItem {

    private final float projectileDamage;

    public FlyingSwordItem(float projectileDamage, Properties props) {
        super(Tiers.IRON, 3, -2.0F, props);
        this.projectileDamage = projectileDamage;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Launch the flying sword projectile
        if (!level.isClientSide && level instanceof ServerLevel sl) {
            FlyingSwordProjectileEntity projectile = new FlyingSwordProjectileEntity(level, player);
            // Start at the player's eye position
            projectile.setPos(player.getEyePosition());
            // Velocity in the look direction
            Vec3 look = player.getLookAngle();
            projectile.shoot(look.x, look.y, look.z, 2.0F, 1.0F);
            projectile.setDamage(projectileDamage);
            sl.addFreshEntity(projectile);

            // Play sword-swish sound
            player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0F, 1.5F);
        }

        // Cooldown (shorter if the sword has a "sword spirit" NBT)
        int cooldown = stack.hasTag() && stack.getTag().contains("SwordSpirit") ? 20 : 30;
        player.getCooldowns().addCooldown(this, cooldown);

        // Cost 1 durability per launch
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

        return InteractionResultHolder.success(stack);
    }
}
