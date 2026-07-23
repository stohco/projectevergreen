package dev.ergenverse.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * BannerType — the five canon sect banner varieties.
 *
 * <p>Each banner represents a major sect or faction from Renegade Immortal.
 * When planted (right-click), the banner radiates spiritual energy that buffs
 * allies within range.
 *
 * <p>Canon: sect banners (宗门旗帜) are carried into battle and planted at
 * strategic positions. They mark territory and boost allied morale. Heng Yue
 * Sect's purple banner, Teng Family's crimson banner, etc. are canon-accurate.
 */
public enum BannerType {
    HENG_YUE(
            "Heng Yue Sect Banner / 恒岳旗帜",
            "Purple banner of the Heng Yue Sect. Buffs allies with Resistance and Speed.",
            ParticleTypes.DRAGON_BREATH,
            SoundEvents.BEACON_ACTIVATE,
            0x9966CC,
            (player, allies) -> {
                for (LivingEntity e : allies) {
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
                }
            }),
    TENG_FAMILY(
            "Teng Family Banner / 滕家旗帜",
            "Crimson-gold banner of the Teng Family. Buffs allies with Strength and Resistance.",
            ParticleTypes.FLAME,
            SoundEvents.BEACON_ACTIVATE,
            0xCC3333,
            (player, allies) -> {
                for (LivingEntity e : allies) {
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
                }
            }),
    TIAN_SHUI(
            "Tian Shui City Banner / 天水旗帜",
            "Blue-white banner of Tian Shui City. Buffs allies with Speed and Regeneration.",
            ParticleTypes.END_ROD,
            SoundEvents.BEACON_ACTIVATE,
            0x3399FF,
            (player, allies) -> {
                for (LivingEntity e : allies) {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 1200, 0));
                }
            }),
    SOUL_REFINING(
            "Soul Refining Sect Banner / 炼魂旗帜",
            "Black-green banner of the Soul Refining Sect. Buffs allies with Absorption and Resistance.",
            ParticleTypes.SQUID_INK,
            SoundEvents.BEACON_ACTIVATE,
            0x336633,
            (player, allies) -> {
                for (LivingEntity e : allies) {
                    e.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1200, 0));
                }
            }),
    XUAN_DAO(
            "Xuan Dao Sect Banner / 玄道旗帜",
            "Silver-cyan banner of the Xuan Dao Sect. Buffs allies with Night Vision and Speed.",
            ParticleTypes.WAX_OFF,
            SoundEvents.BEACON_ACTIVATE,
            0x66CCCC,
            (player, allies) -> {
                for (LivingEntity e : allies) {
                    e.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 0));
                }
            });

    /** Range in blocks for the banner's aura effect. */
    public static final double AURA_RANGE = 16.0;

    private final String displayName;
    private final String description;
    private final SimpleParticleType particle;
    private final SoundEvent sound;
    private final int color;
    private final BannerEffect effect;

    BannerType(String displayName, String description, SimpleParticleType particle,
               SoundEvent sound, int color, BannerEffect effect) {
        this.displayName = displayName;
        this.description = description;
        this.particle = particle;
        this.sound = sound;
        this.color = color;
        this.effect = effect;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public SimpleParticleType getParticle() { return particle; }
    public SoundEvent getSound() { return sound; }
    public int getColor() { return color; }

    /** Apply the banner's aura effect to all allies within range. */
    public void applyAura(Player player, List<LivingEntity> allies) {
        effect.apply(player, allies);
    }

    @FunctionalInterface
    public interface BannerEffect {
        void apply(Player player, List<LivingEntity> allies);
    }
}
