package dev.ergenverse.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

/**
 * TalismanType — the seven canon talisman effect types.
 *
 * <p>Each talisman type defines a single-use effect deployed on right-click.
 * Canon: talismans (符箓) are paper charms inscribed with spirit-array patterns
 * that unleash a one-shot effect when activated. Common types: fireball, barrier,
 * lightning, light, shield, sword-qi, speed-boost.
 */
public enum TalismanType {
    FIREBALL     ("Fireball Talisman",     "Hurls a spirit-fire fireball that ignites the target.", ParticleTypes.FLAME,        SoundEvents.FIRECHARGE_USE,   0xFF6600),
    BARRIER      ("Barrier Talisman",      "Erects a temporary qi barrier granting Resistance IV.", ParticleTypes.WITCH,        SoundEvents.BEACON_ACTIVATE,   0x66CCFF),
    LIGHTNING    ("Lightning Talisman",    "Summons a spirit-lightning strike at the target.",      ParticleTypes.ELECTRIC_SPARK,SoundEvents.LIGHTNING_BOLT_THUNDER, 0xFFFF66),
    LIGHT        ("Light Talisman",        "Grants Night Vision and illuminates the area.",         ParticleTypes.END_ROD,      SoundEvents.AMETHYST_BLOCK_CHIME,0xFFFFFF),
    SHIELD       ("Shield Talisman",       "Grants Absorption (absorbs the next hits).",            ParticleTypes.HEART,        SoundEvents.SHIELD_BLOCK,      0x9999FF),
    SWORD_QI     ("Sword Qi Talisman",    "Fires a piercing sword-qi ray that damages all in path.",ParticleTypes.SWEEP_ATTACK, SoundEvents.PLAYER_ATTACK_SWEEP,0xCCFFFF),
    SPEED_BOOST  ("Speed Boost Talisman",  "Grants Speed III + Haste II for 20 seconds.",           ParticleTypes.CLOUD,        SoundEvents.EXPERIENCE_ORB_PICKUP,0x66FF66),
    TELEPORT     ("Teleport Talisman",     "Single-use warp back to the world's spawn point.",      ParticleTypes.PORTAL,       SoundEvents.ENDERMAN_TELEPORT, 0xCC66FF);

    private final String displayName;
    private final String description;
    private final SimpleParticleType particle;
    private final SoundEvent sound;
    private final int color;

    TalismanType(String displayName, String description, SimpleParticleType particle, SoundEvent sound, int color) {
        this.displayName = displayName;
        this.description = description;
        this.particle = particle;
        this.sound = sound;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    public ParticleOptions getParticle() { return particle; }
    public SoundEvent getSound() { return sound; }
    public int getColor() { return color; }
}
