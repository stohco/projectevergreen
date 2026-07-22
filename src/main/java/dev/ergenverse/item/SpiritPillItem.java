package dev.ergenverse.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * SpiritPillItem — a consumable spirit pill (丹药) with real buff effects.
 *
 * <p>Right-click starts the eating animation (32 ticks). On finish, applies the
 * pill's effects to the player and consumes 1 from the stack. Each pill type has
 * a different effect set defined in {@link PillType}.
 *
 * <p>Canon: pills are refined by alchemists. Qi Gathering (聚气丹) is the most
 * common — a beginner pill. Foundation (筑基丹) is expensive and sought-after.
 * Purification (洗髓丹) cleanses the body. Soul Mending (补魂丹) repairs soul damage.
 *
 * <p>Self-critique: Effects mapped to vanilla MobEffects (canon pills have unique
 * metaphysical effects). No pill-toxicity/accumulation. No quality grade. No
 * recipe tie-in (AlchemyCraftingLogic already exists). WASTE_PILL is a separate
 * type, not a failed-refinement outcome.
 */
public class SpiritPillItem extends Item {

    private final PillType type;

    public SpiritPillItem(PillType type, Properties props) {
        super(props);
        this.type = type;
    }

    public PillType getPillType() { return type; }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }

    @Override
    public int getUseDuration(ItemStack stack) { return 32; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.canEat(false)) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            // Apply effects
            for (PillType.EffectSpec spec : type.getEffects()) {
                player.addEffect(new MobEffectInstance(spec.effect(), spec.duration(), spec.amplifier()));
            }
            // Purification: clear all negative effects
            if (type.clearsNegatives()) {
                clearNegativeEffects(player);
            }
            // Soul mending: clear Wither specifically
            if (type.clearsWither()) {
                player.removeEffect(MobEffects.WITHER);
            }
            // Visual + sound
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                        player.getX(), player.getY() + 1, player.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
            }
            player.playSound(net.minecraft.sounds.SoundEvents.PLAYER_BURP, 0.5F, 1.0F);
        }
        // Consume 1 pill
        stack.shrink(1);
        return stack;
    }

    private void clearNegativeEffects(Player player) {
        MobEffect[] negatives = {
            MobEffects.POISON, MobEffects.WITHER, MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.DIG_SLOWDOWN, MobEffects.WEAKNESS, MobEffects.BLINDNESS,
            MobEffects.CONFUSION, MobEffects.HUNGER, MobEffects.LEVITATION
        };
        for (MobEffect neg : negatives) {
            player.removeEffect(neg);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.literal("\u00A7d" + type.getDisplayName() + " " + type.getNameCn()));
        tooltip.add(net.minecraft.network.chat.Component.literal("\u00A77" + type.getFlavor()));
        for (PillType.EffectSpec spec : type.getEffects()) {
            String name = spec.effect().getDescriptionId();
            tooltip.add(net.minecraft.network.chat.Component.literal("\u00A7a  +" + name +
                    " (" + (spec.duration() / 20) + "s)"));
        }
    }
}
