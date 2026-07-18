package dev.ergenverse.wanglin.behavior;

/**
 * ScalingModel — how the item grows with the cultivator's progression.
 *
 * @param scalesWith           what the item scales with
 * @param scalingFormula       free-text canon formula (e.g. "power × star-tier × 1.5")
 * @param breakthroughCatalyst does a breakthrough catalyze a power spike? (e.g. Ji Realm awakening)
 * @param fusionTargets        what other items / essences it can fuse with
 * @param evolutionPath        documented evolution (e.g. "Fragment Stamp → 18-Hell Stamp")
 * @param notes                free-text canon notes
 */
public record ScalingModel(
        ScalesWith scalesWith,
        String scalingFormula,
        String breakthroughCatalyst,
        java.util.List<String> fusionTargets,
        String evolutionPath,
        String notes
) {
    public enum ScalesWith {
        CULTIVATION_REALM,      // grows with realm tier
        STAR_TIER,              // Ancient God star-tier scaling
        DIVINE_SENSE_STRENGTH,  // grows with divine sense
        KARMA_ACCUMULATED,      // grows with karmic weight
        SOULS_ABSORBED,         // grows with souls consumed (Soul Flag, etc.)
        TIME_HELD,              // grows with charge-up time
        USER_WILL,              // scales with user's will / resolve
        ESSENCE_COMPLETION,     // scales with essence comprehension
        FIXED,                  // does not scale — fixed power
        UNKNOWN
    }

    public static final ScalingModel UNKNOWN = new ScalingModel(
            ScalesWith.UNKNOWN, "UNKNOWN — canon silent on scaling.",
            "UNKNOWN", java.util.List.of(), "UNKNOWN", "");

    public ScalingModel {
        if (scalesWith == null) scalesWith = ScalesWith.UNKNOWN;
        if (scalingFormula == null) scalingFormula = "UNKNOWN";
        if (breakthroughCatalyst == null) breakthroughCatalyst = "UNKNOWN";
        if (fusionTargets == null) fusionTargets = java.util.List.of();
        if (evolutionPath == null) evolutionPath = "UNKNOWN";
        if (notes == null) notes = "";
    }
}
