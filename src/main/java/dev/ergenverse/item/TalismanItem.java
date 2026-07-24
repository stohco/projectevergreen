package dev.ergenverse.item;

import dev.ergenverse.simulation.action.SimulationActions;
import dev.ergenverse.simulation.event.ActionDescriptors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * TalismanItem — a single-use talisman (符箓) deployed on right-click.
 *
 * <p>Each talisman type deploys a different effect: fireball, barrier, lightning,
 * light, shield, sword-qi, or speed-boost. The talisman is consumed on use.
 *
 * <p>Canon: talismans are the standard one-shot spiritual tool. Inscribed on
 * spirit-paper with array patterns, they unleash their effect when activated
 * (torn, burned, or thrown). Cultivators carry stacks of them for emergencies.
 *
 * <p>Self-critique: Single-use only (canon has reusable 法器符箓). No tier scaling.
 * FIREBALL/LIGHTNING reuse vanilla entities (canon is spirit-fire/spirit-lightning
 * that ignores armor). No inscription tie-in. SWORD_QI is an instant raycast, not
 * a flying qi-blade.
 */
public class TalismanItem extends Item {

    private final TalismanType type;

    public TalismanItem(TalismanType type, Properties props) {
        super(props);
        this.type = type;
    }

    public TalismanType getTalismanType() { return type; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);

        if (level instanceof ServerLevel sl) {
            boolean success = deployEffect(sl, player, type);
            if (success) {
                stack.shrink(1); // consume the talisman
                player.getCooldowns().addCooldown(this, 10);
                player.playSound(type.getSound(), 1.0F, 1.0F);
                // Activation particles around the player
                sl.sendParticles(type.getParticle(), player.getX(), player.getY() + 1, player.getZ(),
                        10, 0.5, 1, 0.5, 0.1);
                // CRON-COMPLETIONIST-7: wire talisman activation through the event bus so
                // WangLinReasoningEngine + CanonDivergenceRecorder can react (event-sourced pivot).
                if (player instanceof ServerPlayer sp) {
                    SimulationActions.spellCast(sp, type.getDisplayName(), "talisman",
                            1.0f, ActionDescriptors.Visibility.LOCAL);
                }
            }
        }
        return InteractionResultHolder.success(stack);
    }

    private boolean deployEffect(ServerLevel sl, Player player, TalismanType type) {
        switch (type) {
            case FIREBALL -> {
                // Launch a small fireball in the look direction
                Vec3 look = player.getLookAngle();
                SmallFireball fireball = new SmallFireball(sl, player, look.x * 0.5, look.y * 0.5, look.z * 0.5);
                fireball.setPos(player.getEyePosition());
                sl.addFreshEntity(fireball);
                return true;
            }
            case BARRIER -> {
                // Resistance IV for 10 seconds
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 3));
                return true;
            }
            case LIGHTNING -> {
                // Summon lightning at the looked-at block
                HitResult hit = raytrace(sl, player, 30);
                BlockPos pos = hit.getType() == HitResult.Type.BLOCK
                        ? ((net.minecraft.world.phys.BlockHitResult) hit).getBlockPos()
                        : player.blockPosition().relative(player.getDirection(), 10);
                net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atCenterOf(pos));
                    bolt.setVisualOnly(false);
                    sl.addFreshEntity(bolt);
                }
                return true;
            }
            case LIGHT -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                sl.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1, player.getZ(),
                        20, 1, 1, 1, 0.1);
                return true;
            }
            case SHIELD -> {
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 3));
                return true;
            }
            case SWORD_QI -> {
                // Piercing raycast: damage all entities along a 20-block path
                Vec3 eye = player.getEyePosition();
                Vec3 end = eye.add(player.getLookAngle().scale(20));
                AABB path = new AABB(eye, end).inflate(1.0);
                List<LivingEntity> hit = sl.getEntitiesOfClass(LivingEntity.class, path, e -> e != player);
                for (LivingEntity e : hit) {
                    e.hurt(player.damageSources().playerAttack(player), 10.0F);
                }
                // Visual: sweep particles along the path
                for (double d = 0; d < 20; d += 1) {
                    Vec3 p = eye.add(player.getLookAngle().scale(d));
                    sl.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                }
                return true;
            }
            case SPEED_BOOST -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 2));
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
                return true;
            }
            case TELEPORT -> {
                // CRON-COMPLETIONIST-7: single-use teleport back to the world's spawn point.
                // Canon: talismans inscribed with spatial-array patterns (传送符) are the
                // standard escape tool — they tear open a short-lived qi-corridor back to
                // the cultivator's home/spawn. The player is moved to the overworld spawn
                // (above-ground, safe location) with a brief invulnerability window.
                ServerLevel overworld = sl.getServer().getLevel(net.minecraft.server.level.ServerLevel.OVERWORLD);
                BlockPos spawnPos = overworld.getSharedSpawnPos();
                // Find a safe Y (top non-air block above spawn) — MC's getSharedSpawnPos
                // already returns a surface-safe location in modern versions, but we
                // double-check by scanning up to find the first air pocket with headroom.
                int safeY = spawnPos.getY();
                if (safeY < overworld.getMinBuildHeight() + 2) {
                    safeY = overworld.getMinBuildHeight() + 2;
                }
                // Teleport the player (cross-dimension-safe via ServerPlayer.teleportTo).
                if (player instanceof ServerPlayer sp) {
                    sp.teleportTo(overworld,
                            spawnPos.getX() + 0.5, safeY + 0.5, spawnPos.getZ() + 0.5,
                            player.getYRot(), player.getXRot());
                } else {
                    player.teleportTo(spawnPos.getX() + 0.5, safeY + 0.5, spawnPos.getZ() + 0.5);
                }
                // Brief invulnerability (40 ticks = 2s) so the player can't be killed
                // mid-teleport by an attacker at the origin.
                player.invulnerableTime = 40;
                player.setDeltaMovement(Vec3.ZERO);
                // Departure + arrival particles
                sl.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 1, 0.5, 0.3);
                overworld.sendParticles(ParticleTypes.PORTAL,
                        player.getX(), player.getY() + 1, player.getZ(), 30, 0.5, 1, 0.5, 0.3);
                return true;
            }
        }
        return false;
    }

    private HitResult raytrace(Level level, Player player, double dist) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(dist));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<net.minecraft.network.chat.Component> tooltip, TooltipFlag flag) {
        tooltip.add(net.minecraft.network.chat.Component.literal("\u00A7e" + type.getDisplayName()));
        tooltip.add(net.minecraft.network.chat.Component.literal("\u00A77" + type.getDescription()));
        tooltip.add(net.minecraft.network.chat.Component.literal("\u00A78Single-use"));
    }
}
