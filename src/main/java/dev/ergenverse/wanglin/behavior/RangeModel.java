package dev.ergenverse.wanglin.behavior;

/**
 * RangeModel — the spatial extent of the item's effect.
 *
 * @param minRangeBlocks   minimum range (Minecraft blocks); 0 = self-touch
 * @param maxRangeBlocks   maximum range; -1 = unlimited / world-scale
 * @param areaOfEffectBlocks  AOE radius (0 = single-target)
 * @param targeting        targeting model
 * @param notes            free-text canon notes
 */
public record RangeModel(
        int minRangeBlocks,
        int maxRangeBlocks,
        int areaOfEffectBlocks,
        Targeting targeting,
        String notes
) {
    public enum Targeting {
        SINGLE_TARGET,          // one entity
        AOE_SPHERE,             // sphere around caster
        AOE_CONE,               // cone in front of caster
        AOE_LINE,               // line / beam
        WORLD_SCALE,            // entire world / star-system
        CROSS_WORLD,            // multiple worlds (e.g. Karma Whip's 7-million-worlds strike)
        SELF_ONLY,              // affects only the caster
        CONTAINMENT,            // seals / contains an area
        UNKNOWN
    }

    public static final RangeModel UNKNOWN = new RangeModel(
            -1, -1, -1, Targeting.UNKNOWN,
            "UNKNOWN — canon silent on range.");

    public RangeModel {
        if (targeting == null) targeting = Targeting.UNKNOWN;
        if (notes == null) notes = "";
    }
}
