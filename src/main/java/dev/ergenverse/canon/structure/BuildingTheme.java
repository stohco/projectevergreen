package dev.ergenverse.canon.structure;

/**
 * BuildingTheme — a <b>semantic</b> construction style for a building, carrying
 * <b>no block references whatsoever</b>.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's directive:
 * <blockquote>
 *   Even themes shouldn't know blocks. Instead of
 *   {@code BuildingTheme { wallBlock, roofBlock, floorBlock }} I'd do
 *   {@code BuildingTheme { POOR_VILLAGE, ELDER_HOME, SECT_DISCIPLE, CORE_ELDER,
 *   ANCIENT_RUIN, IMMORTAL_PALACE }}. Then the backend decides: POOR_VILLAGE →
 *   oak planks, spruce roof, stone foundation. Later maybe you redesign every
 *   Zhao house — nothing above changes.
 * </blockquote>
 *
 * <p>A {@code BuildingTheme} is a pure enum. The
 * {@link dev.ergenverse.assembly.BuildingLibrary} is the <b>only</b> place that
 * maps a theme to concrete {@link dev.ergenverse.assembly.MaterialID}s and
 * shell geometry. Redesigning "every poor house" means editing one switch arm
 * in the library — no canon data changes.
 *
 * <p>This supersedes the CRON-125 {@code CanonBuilding.ShellType} enum, which
 * was semantically equivalent but nested and less expressive.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public enum BuildingTheme {
    /** A poor mortal village home — coarse wood, plank roof. */
    POOR_VILLAGE,
    /** A village elder's home — sturdier spruce construction. */
    ELDER_HOME,
    /** A storage shed — cobblestone floor, log walls. */
    STORAGE_SHED,
    /** An open communal plaza — polished stone, no walls. */
    COMMON_PLAZA,
    /** An open farm plot — farmland, no walls. */
    FARM_PLOT,
    /** A sect disciple's quarters (future). */
    SECT_DISCIPLE,
    /** A core elder's pavilion (future). */
    CORE_ELDER,
    /** An ancient ruin (future). */
    ANCIENT_RUIN,
    /** An immortal palace (future). */
    IMMORTAL_PALACE
}
