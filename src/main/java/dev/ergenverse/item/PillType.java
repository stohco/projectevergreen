package dev.ergenverse.item;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

/**
 * PillType — the canon spirit pill (丹药) types with their effects.
 *
 * <p>Each pill type defines a set of MobEffects applied when the pill is consumed.
 * Canon: pills are refined by alchemists from spirit herbs. Different pills serve
 * different cultivation purposes — qi gathering, foundation building, purification,
 * soul mending, etc. Dark pills (blood soul) have side effects.
 */
public enum PillType {
    QI_GATHERING ("Qi Gathering Pill", "聚气丹", "Refines impure qi. Grants Haste + Regeneration.",
            new EffectSpec[]{new EffectSpec(MobEffects.DIG_SPEED, 1200, 1), new EffectSpec(MobEffects.REGENERATION, 600, 0)},
            false, false),
    FOUNDATION   ("Foundation Pill", "筑基丹", "Strengthens the foundation. Grants Resistance + Strength.",
            new EffectSpec[]{new EffectSpec(MobEffects.DAMAGE_RESISTANCE, 2400, 1), new EffectSpec(MobEffects.DAMAGE_BOOST, 1200, 0), new EffectSpec(MobEffects.REGENERATION, 200, 0)},
            false, false),
    PURIFICATION ("Purification Pill", "洗髓丹", "Cleanses impurities. Removes negative effects + Regeneration II.",
            new EffectSpec[]{new EffectSpec(MobEffects.REGENERATION, 400, 1)},
            true, false),
    SOUL_MENDING ("Soul Mending Pill", "补魂丹", "Mends the soul. Regeneration III + clears Wither.",
            new EffectSpec[]{new EffectSpec(MobEffects.REGENERATION, 800, 2), new EffectSpec(MobEffects.SLOW_FALLING, 1200, 0)},
            false, true),
    BLOOD_SOUL   ("Blood Soul Pill", "血魂丹", "A dark pill. Great power, but at a cost.",
            new EffectSpec[]{new EffectSpec(MobEffects.DAMAGE_BOOST, 600, 2), new EffectSpec(MobEffects.MOVEMENT_SPEED, 600, 1), new EffectSpec(MobEffects.HUNGER, 400, 0)},
            false, false),
    WASTE_PILL   ("Waste Pill", "废丹", "A failed refinement. Nausea + Poison.",
            new EffectSpec[]{new EffectSpec(MobEffects.CONFUSION, 400, 0), new EffectSpec(MobEffects.POISON, 200, 0)},
            false, false),
    MINOR_HEALING("Minor Healing Pill", "小还丹", "A cheap healing pill. Regeneration I.",
            new EffectSpec[]{new EffectSpec(MobEffects.REGENERATION, 200, 0)},
            false, false);

    private final String displayName;
    private final String nameCn;
    private final String flavor;
    private final EffectSpec[] effects;
    private final boolean clearNegatives;
    private final boolean clearWither;

    PillType(String displayName, String nameCn, String flavor, EffectSpec[] effects,
             boolean clearNegatives, boolean clearWither) {
        this.displayName = displayName;
        this.nameCn = nameCn;
        this.flavor = flavor;
        this.effects = effects;
        this.clearNegatives = clearNegatives;
        this.clearWither = clearWither;
    }

    public String getDisplayName() { return displayName; }
    public String getNameCn() { return nameCn; }
    public String getFlavor() { return flavor; }
    public EffectSpec[] getEffects() { return effects; }
    public boolean clearsNegatives() { return clearNegatives; }
    public boolean clearsWither() { return clearWither; }

    /** A single effect in a pill's effect set. */
    public record EffectSpec(MobEffect effect, int duration, int amplifier) {}
}
