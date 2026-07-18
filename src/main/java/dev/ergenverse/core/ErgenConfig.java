package dev.ergenverse.core;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * ErgenConfig — the mod's configuration spec.
 *
 * <p>Holds tunable parameters for the Ergenverse mod. All values have sensible
 * defaults derived from the canon docs and the design philosophy.
 *
 * <p>Categories:
 * <ul>
 *   <li><b>COMMON</b> — world simulation tuning (world-pulse interval, ecology rates)</li>
 *   <li><b>CLIENT</b> — perception/visual settings (tooltip verbosity, perception tier display)</li>
 * </ul>
 */
public class ErgenConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue WORLD_PULSE_INTERVAL_TICKS;
    public static final ForgeConfigSpec.BooleanValue ENABLE_WANGLIN_ARSENAL;
    public static final ForgeConfigSpec.BooleanValue NO_LOCKED_UPGRADES;
    public static final ForgeConfigSpec.BooleanValue CANON_STRICT_MODE;
    public static final ForgeConfigSpec.IntValue MAX_EVOLUTION_STAGE_DISPLAY;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Ergenverse — Living Cultivation World configuration")
               .push("general");

        WORLD_PULSE_INTERVAL_TICKS = builder
                .comment("Ticks between world-pulse updates (20 ticks = 1 second).",
                         "Default: 24000 (one Minecraft day). The world evolves independently",
                         "of the player at this interval.")
                .defineInRange("worldPulseIntervalTicks", 24000, 200, 72000);

        ENABLE_WANGLIN_ARSENAL = builder
                .comment("Whether Wang Lin's arsenal items are registered.",
                         "Default: true. Disable only for debugging world-gen without items.")
                .define("enableWangLinArsenal", true);

        NO_LOCKED_UPGRADES = builder
                .comment("Whether every item evolution chain is fully walkable (no dead ends).",
                         "Default: true. This is a USER DIRECTIVE — do not disable.",
                         "When true, every item has a path to peak state via bridging alternatives",
                         "(typically Manifestation Gift substitutes).")
                .define("noLockedUpgrades", true);

        CANON_STRICT_MODE = builder
                .comment("Whether canon-strict mode is enforced (Level 0 filler forbidden in canon regions).",
                         "Default: true. Disable only for sandbox/experimental play.")
                .define("canonStrictMode", true);

        MAX_EVOLUTION_STAGE_DISPLAY = builder
                .comment("Maximum evolution stages to show in item tooltips before truncating.",
                         "Default: 5.")
                .defineInRange("maxEvolutionStageDisplay", 5, 1, 20);

        builder.pop();

        SPEC = builder.build();
    }
}
