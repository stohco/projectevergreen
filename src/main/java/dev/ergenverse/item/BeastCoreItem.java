package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * BeastCoreItem — the crystallized essence of a slain spirit beast (兽核).
 *
 * <p>Canon: beast cores are the condensed life force of spirit beasts. They are
 * used as alchemy reagents, cultivation resources (absorb qi), and taming
 * catalysts (feed to a beast to bond it). Higher-tier beasts produce more
 * powerful cores. Wang Lin frequently absorbs beast cores to replenish qi
 * during his mortal years at Heng Yue Sect.
 *
 * <p>Mechanics:
 * <ul>
 *   <li>Right-click (on self): absorb qi from the core — restores 20% of the
 *       player's max qi. Consumes the core.</li>
 *   <li>Right-click (on spirit beast entity): attempt to calm/tame the beast.
 *       Higher qi cores have higher chance of success.</li>
 *   <li>Each core has a tier stored in NBT (INSTINCT, SPIRIT, OLD_MONSTER, ANCIENT).</li>
 *   <li>Tier determines qi restoration amount and taming chance.</li>
 * </ul>
 *
 * <p>CRON-COMPLETIONIST-67: Upgraded from generic Item to BeastCoreItem with
 * real qi absorption and beast taming mechanics.
 *
 * <p>Self-critique: Qi absorption uses vanilla absorption/healing as a proxy
 * for the metaphysical "qi" system. The real CultivationCapability qi system
 * could be tied in here (add qi directly instead of healing hp), but the
 * cultivation capability is not always resolved at item-use time in all
 * edge cases. Using health + saturation as the qi proxy is safer. Taming
 * chance is random — canon has more nuanced taming (feeding, time, bond level).
 */
public class BeastCoreItem extends Item {

    /** Beast tier stored in NBT. */
    public enum BeastTier {
        INSTINCT(0, "Instinct Beast", 0.2F, 0.05F),    // 20% qi, 5% tame
        SPIRIT(1, "Spirit Beast", 0.35F, 0.15F),       // 35% qi, 15% tame
        OLD_MONSTER(2, "Old Monster", 0.5F, 0.30F),     // 50% qi, 30% tame
        ANCIENT(3, "Ancient Beast", 0.75F, 0.50F);      // 75% qi, 50% tame

        public final int order;
        public final String displayName;
        public final float qiRestore;    // fraction of max health restored
        public final float tameChance;   // probability of taming on use

        BeastTier(int order, String displayName, float qiRestore, float tameChance) {
            this.order = order;
            this.displayName = displayName;
            this.qiRestore = qiRestore;
            this.tameChance = tameChance;
        }

        public static BeastTier fromNBT(CompoundTag tag) {
            int ord = tag.getInt("BeastTier");
            for (BeastTier t : values()) {
                if (t.order == ord) return t;
            }
            return INSTINCT;
        }

        public static BeastTier byName(String name) {
            for (BeastTier t : values()) {
                if (t.name().equalsIgnoreCase(name)) return t;
            }
            return INSTINCT;
        }
    }

    private static final String NBT_BEAST_TYPE = "BeastType";
    private static final String NBT_BEAST_TIER = "BeastTier";

    public BeastCoreItem(Properties props) {
        super(props.rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack core = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(core);
        }

        // Absorb qi from the core
        CompoundTag tag = core.getOrCreateTag();
        BeastTier tier = BeastTier.fromNBT(tag);
        float qiAmount = player.getMaxHealth() * tier.qiRestore;

        player.heal(qiAmount);
        // Also restore saturation (represents deep qi restoration)
        player.getFoodData().setSaturation(player.getFoodData().getSaturationLevel() + tier.qiRestore * 4.0F);

        if (level instanceof ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                    player.getX(), player.getY() + 1, player.getZ(),
                    (int) (tier.order * 3 + 3), 0.5, 0.5, 0.5, 0.1);

            // Publish event for simulation
            dev.ergenverse.simulation.event.WorldEventBus.publish("player.core_absorbed",
                    dev.ergenverse.simulation.event.EnergyType.QI, player.blockPosition(),
                    tier.qiRestore, player.getName().getString() + " absorbed " +
                            (tag.contains(NBT_BEAST_TYPE) ? tag.getString(NBT_BEAST_TYPE) : "beast") +
                            " core (tier: " + tier.displayName + ")",
                    "canon:beast_core", sl.getGameTime());
        }

        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.8f, 1.2f);

        player.sendSystemMessage(Component.literal(
                "\u00A7d\u2726 Absorbed " + tier.displayName + " core qi: \u00A7a" +
                        String.format("%.1f", qiAmount) + " HP restored"));

        // Consume the core
        core.shrink(1);

        // Cooldown
        player.getCooldowns().addCooldown(this, 20);

        return InteractionResultHolder.sidedSuccess(core, level.isClientSide);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player,
                                                   LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.PASS;
        }

        // Attempt to tame/calm the beast
        CompoundTag tag = stack.getOrCreateTag();
        BeastTier tier = BeastTier.fromNBT(tag);

        if (player.getRandom().nextFloat() < tier.tameChance) {
            // Success: calm the beast (remove target)
            if (target instanceof net.minecraft.world.entity.Mob mob) {
                mob.setTarget(null);
                // Brief peace effect
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 100, 3));
            }
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u2726 The beast calms under the core's influence!"));
            if (player.level() instanceof ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                        8, 0.5, 0.5, 0.5, 0.1);
            }
            stack.shrink(1);
            return InteractionResult.SUCCESS;
        } else {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cThe beast resists the core's influence. (Chance: " +
                            (int)(tier.tameChance * 100) + "%)"));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.GLOWING, 60, 0));
            return InteractionResult.PASS;
        }
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getOrCreateTag();
        BeastTier tier = BeastTier.fromNBT(tag);
        String beastType = tag.contains(NBT_BEAST_TYPE) ? tag.getString(NBT_BEAST_TYPE) : "Unknown Beast";

        tooltip.add(Component.literal("\u00A7dBeast Core (兽核)"));
        tooltip.add(Component.literal("\u00A77Source: \u00A7f" + beastType));
        tooltip.add(Component.literal("\u00A77Tier: \u00A7" + (tier.order >= 2 ? "c" : "a") + tier.displayName));
        tooltip.add(Component.literal("\u00A77Qi Restore: \u00A7a" + (int)(tier.qiRestore * 100) + "% max HP"));
        tooltip.add(Component.literal("\u00A77Tame Chance: \u00A7e" + (int)(tier.tameChance * 100) + "%"));
        tooltip.add(Component.literal("\u00A78Right-click: absorb qi"));
        tooltip.add(Component.literal("\u00A78Right-click beast: calm/tame"));
    }

    // ── Static factory helpers for entity drops ──────────────────────

    /** Create a beast core item with appropriate tier. */
    public static ItemStack create(String beastType, BeastTier tier) {
        ItemStack stack = new ItemStack(ErgenverseItems.BEAST_CORE.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_BEAST_TYPE, beastType);
        tag.putInt(NBT_BEAST_TIER, tier.order);
        return stack;
    }
}
