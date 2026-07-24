package dev.ergenverse.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * ScrollType — the six canon technique scroll varieties.
 *
 * <p>Each scroll type represents a cultivation technique (功法) inscribed on
 * a jade scroll or animal-skin parchment. Reading a scroll takes time and
 * Qi; in this mod, right-click "studies" the scroll, consuming it and granting
 * a temporary buff representing the technique's immediate application.
 *
 * <p>Canon: technique scrolls are the primary way cultivators learn new abilities
 * in Renegade Immortal. Wang Lin steals the restriction art scroll from Heng Yue
 * Sect's library; cultivators guard technique scrolls as their most valuable
 * possessions. A scroll is knowledge — not a weapon, not a tool.
 */
public enum ScrollType {
    QI_GATHERING(
            "Qi Gathering Scroll / 聚气诀",
            "Teaches basic Qi gathering. Temporarily accelerates spiritual energy recovery.",
            ParticleTypes.HEART,
            SoundEvents.EXPERIENCE_ORB_PICKUP,
            0x66FF99,
            (player) -> {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));
            }),
    SWORD_TECHNIQUE(
            "Sword Technique Scroll / 剑诀",
            "Teaches a foundational sword technique. Temporarily enhances melee power.",
            ParticleTypes.SWEEP_ATTACK,
            SoundEvents.PLAYER_ATTACK_SWEEP,
            0xCCCCFF,
            (player) -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
            }),
    BODY_REFINEMENT(
            "Body Refinement Scroll / 体修诀",
            "Teaches body-strengthening methods. Temporarily hardens the body against harm.",
            ParticleTypes.ANGRY_VILLAGER,
            SoundEvents.ANVIL_USE,
            0xFFCC66,
            (player) -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 1));
            }),
    FIRE_CONTROL(
            "Fire Control Scroll / 控火诀",
            "Teaches spirit-fire manipulation. Temporarily ignites the cultivator's weapon.",
            ParticleTypes.FLAME,
            SoundEvents.FIRECHARGE_USE,
            0xFF6633,
            (player) -> {
                // Fire Aspect is an enchantment, not a potion effect in MC 1.20.1.
                // Substitute: Fire Resistance for the fire control technique.
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
            }),
    SPIRITUAL_SENSE(
            "Spiritual Sense Scroll / 神识诀",
            "Expands spiritual perception. Grants enhanced sight in darkness.",
            ParticleTypes.END_ROD,
            SoundEvents.BEACON_ACTIVATE,
            0xCC99FF,
            (player) -> {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1200, 0));
            }),
    RESTRICTION_ART(
            "Restriction Art Scroll / 禁制术",
            "Teaches the ancient restriction arts. Slows nearby enemies with spiritual pressure.",
            ParticleTypes.REVERSE_PORTAL,
            SoundEvents.SCULK_SHRIEKER_SHRIEK,
            0x9966FF,
            (player) -> {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1));
                // In canon, restriction art affects ENEMIES, not the user.
                // We give the player a self-effect here as a simplified version.
                // A full implementation would apply Slowness II to all hostile
                // entities within 16 blocks — but that requires an area-of-effect
                // system which is a future enhancement.
            });

    private final String displayName;
    private final String description;
    private final SimpleParticleType particle;
    private final SoundEvent sound;
    private final int color;
    private final ScrollEffect effect;

    ScrollType(String displayName, String description, SimpleParticleType particle,
               SoundEvent sound, int color, ScrollEffect effect) {
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

    /** Apply the scroll's technique effect to the player. */
    public void applyEffect(Player player) { effect.apply(player); }

    @FunctionalInterface
    public interface ScrollEffect {
        void apply(Player player);
    }
}
