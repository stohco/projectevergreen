package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * SoulGourdItem — captures and stores souls from killed entities (魂葫芦).
 *
 * <p>Canon: Wang Lin's soul gourd is a dark, gourd-shaped treasure that can
 * capture the souls of the dead. In Renegade Immortal, soul-capturing treasures
 * are feared throughout the cultivation world — they trap the consciousness
 * of slain enemies, preventing their reincarnation. The gourd stores captured
 * souls and can release them as soul attacks or for refining purposes.
 *
 * <p>Mechanics:
 * <ul>
 *   <li>Right-click on a dead entity (within 3 seconds of death): capture its soul.</li>
 *   <li>Right-click (no target): release all stored souls as a burst of damage
 *       around the player (area effect).</li>
 *   <li>Max capacity: 10 souls (base gourd). Souls persist in NBT.</li>
 *   <li>Each captured soul stores: entity type, name, power level.</li>
 * </ul>
 *
 * <p>CRON-COMPLETIONIST-67: New functional item. Previously soul_gourd was a
 * display-only WangLinItem with no mechanics.
 *
 * <p>Self-critique: Soul capture requires right-clicking on a recently-killed
 * entity's corpse, which has a narrow time window (3s). In practice, players
 * may find it easier to just kill things without bothering to capture souls.
 * The area damage on release needs to scale with number of souls + their power
 * to make soul collection worthwhile. Currently uses flat 3.0 damage per soul.
 */
public class SoulGourdItem extends Item {

    private static final int MAX_SOULS = 10;
    private static final String NBT_SOULS = "CapturedSouls";
    private static final String NBT_SOUL_COUNT = "SoulCount";
    private static final String NBT_ENTITY_TYPE = "EntityType";
    private static final String NBT_ENTITY_NAME = "EntityName";
    private static final String NBT_POWER = "Power";
    private static final String NBT_CAPTURE_TIME = "CaptureTime";

    public SoulGourdItem(Properties props) {
        // NOTE: props already carries .durability(500) from registration.
        // In MC 1.20.1, durability() implicitly forces maxStackSize=1, and
        // calling .stacksTo(1) on top of durability() throws
        // "Unable to have damage AND stack" at registration time (server boot crash).
        // So we do NOT call stacksTo() here — rarity() is the only safe addition.
        super(props.rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack gourd = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(gourd);
        }

        if (player.isShiftKeyDown()) {
            // Shift + right-click: release all souls as area damage
            return releaseSouls(level, player, hand, gourd);
        } else {
            // Right-click: attempt to capture soul from nearby corpse
            return captureSoul(level, player, hand, gourd);
        }
    }

    private InteractionResultHolder<ItemStack> captureSoul(Level level, Player player,
                                                          InteractionHand hand, ItemStack gourd) {
        if (!(level instanceof ServerLevel sl)) {
            return InteractionResultHolder.success(gourd);
        }

        int soulCount = getSoulCount(gourd);
        if (soulCount >= MAX_SOULS) {
            player.sendSystemMessage(Component.literal(
                    "\u00A7cThe Soul Gourd is full! (" + MAX_SOULS + "/" + MAX_SOULS + ")"));
            player.sendSystemMessage(Component.literal("\u00A77Shift+right-click to release souls as an attack."));
            return InteractionResultHolder.fail(gourd);
        }

        // Find nearby dead/recently-killed entities within 5 blocks
        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(5.0),
                e -> e.isDeadOrDying() && e.deathTime <= 60 && e != player); // 3 seconds

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77No souls nearby to capture. Kill an entity and right-click its corpse within 3 seconds."));
            return InteractionResultHolder.success(gourd);
        }

        // Capture the soul of the closest target
        LivingEntity target = targets.get(0);
        CompoundTag tag = gourd.getOrCreateTag();
        ListTag souls = tag.getList(NBT_SOULS, CompoundTag.TAG_COMPOUND);

        CompoundTag soulTag = new CompoundTag();
        soulTag.putString(NBT_ENTITY_TYPE,
                net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString());
        soulTag.putString(NBT_ENTITY_NAME, target.getDisplayName().getString());
        soulTag.putFloat(NBT_POWER, (float) target.getMaxHealth());
        soulTag.putLong(NBT_CAPTURE_TIME, sl.getGameTime());
        souls.add(soulTag);

        tag.put(NBT_SOULS, souls);
        tag.putInt(NBT_SOUL_COUNT, souls.size());

        player.sendSystemMessage(Component.literal(
                "\u00A7d\u2726 Captured soul: \u00A7a" + target.getDisplayName().getString() +
                        " \u00A77(" + getSoulCount(gourd) + "/" + MAX_SOULS + ")"));

        // Visual: soul particles flowing from corpse to player
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                10, 0.5, 0.5, 0.5, 0.05);

        level.playSound(null, player.blockPosition(),
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.8f, 0.8f);

        return InteractionResultHolder.success(gourd);
    }

    private InteractionResultHolder<ItemStack> releaseSouls(Level level, Player player,
                                                            InteractionHand hand, ItemStack gourd) {
        if (!(level instanceof ServerLevel sl)) {
            return InteractionResultHolder.success(gourd);
        }

        int soulCount = getSoulCount(gourd);
        if (soulCount == 0) {
            player.sendSystemMessage(Component.literal("\u00A77The Soul Gourd is empty. Capture souls from killed entities."));
            return InteractionResultHolder.success(gourd);
        }

        // Release all souls as area damage around the player
        float totalPower = getTotalPower(gourd);
        float damagePerSoul = 3.0F + totalPower / soulCount * 0.5F;
        float totalDamage = damagePerSoul * soulCount;

        // Damage all living entities within 8 blocks
        List<LivingEntity> targets = sl.getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(8.0),
                e -> e.isAlive() && e != player);

        for (LivingEntity target : targets) {
            target.hurt(sl.damageSources().playerAttack(player), totalDamage / targets.size());
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.WITHER, 40, 0));
        }

        player.sendSystemMessage(Component.literal(
                "\u00A7c\u2726 Released " + soulCount + " souls! \u00A77" +
                        String.format("%.1f", totalDamage) + " total soul damage"));

        // Visual: massive soul burst
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 2.0, 2.0, 2.0, 0.1);
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 1.5, 1.5, 1.5, 0.05);

        level.playSound(null, player.blockPosition(),
                SoundEvents.WITHER_SHOOT, SoundSource.PLAYERS, 1.0f, 0.5f);

        // Clear captured souls
        CompoundTag tag = gourd.getOrCreateTag();
        tag.remove(NBT_SOULS);
        tag.putInt(NBT_SOUL_COUNT, 0);

        // Cost durability
        gourd.hurtAndBreak(5, player, p -> p.broadcastBreakEvent(hand));

        return InteractionResultHolder.success(gourd);
    }

    // ── Helpers ─────────────────────────────────────────────────────

    public int getSoulCount(ItemStack gourd) {
        return gourd.getOrCreateTag().getInt(NBT_SOUL_COUNT);
    }

    private float getTotalPower(ItemStack gourd) {
        CompoundTag tag = gourd.getOrCreateTag();
        ListTag souls = tag.getList(NBT_SOULS, CompoundTag.TAG_COMPOUND);
        float total = 0;
        for (int i = 0; i < souls.size(); i++) {
            total += souls.getCompound(i).getFloat(NBT_POWER);
        }
        return total;
    }

    // ── Tooltip ──────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        int count = getSoulCount(stack);
        tooltip.add(Component.literal("\u00A7dSoul Gourd (魂葫芦)"));
        tooltip.add(Component.literal("\u00A77Captures souls of the slain"));
        tooltip.add(Component.literal("\u00A78Souls: \u00A7c" + count + "/" + MAX_SOULS));
        tooltip.add(Component.literal("\u00A77Right-click corpse: capture soul"));
        tooltip.add(Component.literal("\u00A77Shift+right-click: release as attack"));

        if (count > 0) {
            // Show last 3 captured souls
            CompoundTag tag = stack.getOrCreateTag();
            ListTag souls = tag.getList(NBT_SOULS, CompoundTag.TAG_COMPOUND);
            int start = Math.max(0, souls.size() - 3);
            tooltip.add(Component.literal("\u00A78Recent captures:"));
            for (int i = start; i < souls.size(); i++) {
                CompoundTag soul = souls.getCompound(i);
                tooltip.add(Component.literal(
                        "\u00A7a  \u2620 " + soul.getString(NBT_ENTITY_NAME)));
            }
        }
    }
}
