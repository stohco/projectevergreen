package dev.ergenverse.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * SectBannerItem — a sect banner (宗门旗帜) that buffs allies when planted.
 *
 * <p>Right-click to plant the banner: it radiates spiritual energy in a 16-block
 * radius, granting buffs to all non-hostile entities. The banner is consumed
 * on use (one-time planting).
 *
 * <p>Canon: sect banners are carried into battle and planted at strategic points.
 * Heng Yue Sect's purple banner flies over the sect gates; Teng Family's crimson
 * banner marks their territory. A planted banner is a declaration of presence
 * and a morale boost for allies.
 */
public class SectBannerItem extends Item {

    private final BannerType bannerType;

    public SectBannerItem(BannerType bannerType, Properties props) {
        super(props);
        this.bannerType = bannerType;
    }

    public BannerType getBannerType() { return bannerType; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        if (level instanceof ServerLevel sl) {
            // Find all non-hostile living entities within aura range
            AABB area = player.getBoundingBox().inflate(BannerType.AURA_RANGE);
            List<LivingEntity> allies = sl.getEntitiesOfClass(LivingEntity.class, area, e -> {
                // Buff the player and any allied entities (non-monsters)
                if (e == player) return true;
                // Consider non-hostile mobs as allies (villagers, animals, etc.)
                return !(e instanceof net.minecraft.world.entity.monster.Monster);
            });

            // Apply banner aura to all allies
            bannerType.applyAura(player, allies);

            // Consume the banner (planted in the ground)
            stack.shrink(1);

            // Banner planting sound — resonant activation
            player.playSound(bannerType.getSound(), 1.5F, 0.6F);

            // Visual: particle burst in banner's color from player position
            sl.sendParticles(bannerType.getParticle(),
                    player.getX(), player.getY() + 0.5, player.getZ(),
                    30, 1.0, 2.0, 1.0, 0.1);
            // Secondary: upward spiral of particles (banner energy rising)
            for (int i = 0; i < 12; i++) {
                double angle = (i / 12.0) * Math.PI * 2;
                double ox = Math.cos(angle) * 2.0;
                double oz = Math.sin(angle) * 2.0;
                sl.sendParticles(bannerType.getParticle(),
                        player.getX() + ox, player.getY() + i * 0.3, player.getZ() + oz,
                        2, 0.1, 0.1, 0.1, 0.02);
            }

            // Chat message
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7oThe banner radiates spiritual energy! " + allies.size() + " allies affected by: \u00A76" + bannerType.getDisplayName()));

            // Cooldown to prevent rapid planting
            player.getCooldowns().addCooldown(this, 200);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u00A7e" + bannerType.getDisplayName()));
        tooltip.add(Component.literal("\u00A77" + bannerType.getDescription()));
        tooltip.add(Component.literal("\u00A78Right-click to plant (16-block aura, consumed)"));
        tooltip.add(Component.literal("\u00A79Range: " + BannerType.AURA_RANGE + " blocks"));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
