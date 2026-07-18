package dev.ergenverse.wanglin.behavior;

/**
 * EnvironmentalEffect — how weather / terrain / time-of-day / world-law affects the item.
 *
 * @param factor          the environmental factor
 * @param effect          the effect on the item
 * @param magnitude       relative magnitude (-1.0 = item fails, 0.0 = no effect, +1.0 = doubled, etc.)
 * @param canonBasis      chapter citation
 */
public record EnvironmentalEffect(
        Factor factor,
        String effect,
        double magnitude,
        String canonBasis
) {
    public enum Factor {
        EXTREME_YIN_SITE,       // places of extreme Yin (boosts Underworld Ascension Method)
        EXTREME_YANG_SITE,      // places of extreme Yang
        DAY_NIGHT_CYCLE,        // daytime vs nighttime
        WEATHER_LIGHTNING,      // lightning storms
        WEATHER_RAIN,           // rain
        WORLD_LAW_STRENGTH,     // the world's law tier (fragile/low/medium/high/absolute)
        SEALED_REALM,           // inside the Realm-Sealing Grand Array
        OUTER_REALM,            // outside the Realm-Sealing Array
        ANCIENT_GOD_BODY,       // inside an Ancient God's body (Tu Si's body)
        REINCARNATION_POOL,     // the Reincarnation Pool
        TIME_DILATION_FIELD,    // inside the Heaven-Defying Bead's interior
        TRIBULATION_ACTIVE,     // while a divine tribulation is in progress
        UNKNOWN
    }

    public EnvironmentalEffect {
        if (factor == null) factor = Factor.UNKNOWN;
        if (effect == null) effect = "";
        if (canonBasis == null) canonBasis = "";
    }
}
