package dev.ergenverse.simulation.settlement;

/**
 * TimeOfDay — canonical phases of a Minecraft day (24000 ticks).
 *
 * <p>Used by {@link PresenceLocation} to define per-phase presence weights
 * (where actors tend to be at different times of day). Also used by
 * {@link ActorPresence} to compute the deterministic weighted pick.
 *
 * <p>MC 1.20.1 day cycle:
 * <pre>
 *   Tick 0-6000     : daytime   (sunrise to noon)
 *   Tick 6000-12000 : daytime   (noon to sunset)
 *   Tick 12000-13000: dusk      (sunset transition)
 *   Tick 13000-23000: nighttime (moonrise to pre-dawn)
 *   Tick 23000-24000: dawn      (pre-sunrise)
 * </pre>
 *
 * <p>These 7 phases provide sufficient granularity for daily rhythms without
 * per-tick jitter. The {@link ActorPresence} engine produces stable picks
 * within each phase.
 */
public enum TimeOfDay {

    /** Tick 23000–23999 + 0–1000: pre-sunrise, early morning. Villagers wake. */
    DAWN(23000, 1000),

    /** Tick 1000–4000: morning. Market opens, farmers head to fields. */
    MORNING(1000, 4000),

    /** Tick 4000–7000: midday. Peak outdoor activity. */
    MIDDAY(4000, 7000),

    /** Tick 7000–11000: afternoon. Activity winds down, cultivators meditate. */
    AFTERNOON(7000, 11000),

    /** Tick 11000–13500: dusk. Everyone heads home. Dinner preparation. */
    DUSK(11000, 13500),

    /** Tick 13500–18000: evening. Indoor activities, family meals. */
    EVENING(13500, 18000),

    /** Tick 18000–23000: night. Most asleep, guards awake, cultivators meditate. */
    NIGHT(18000, 23000);

    /** Tick when this phase begins (wrapping at 24000). */
    public final int startTick;
    /** Tick when this phase ends (exclusive). */
    public final int endTick;

    TimeOfDay(int startTick, int endTick) {
        this.startTick = startTick;
        this.endTick = endTick;
    }

    /**
     * Determine the current TimeOfDay from a Minecraft gameTime value.
     *
     * <p>Handles the wrapping case (DAWN spans 23000–24000 → 0–1000)
     * by normalizing the tick to [0, 24000).
     *
     * @param gameTime the level's gameTime (ticks)
     * @return the current TimeOfDay phase
     */
    public static TimeOfDay fromGameTime(long gameTime) {
        int tick = (int)(gameTime % 24000L);
        if (tick < 0) tick += 24000;

        // Handle DAWN wrap: 23000–23999 and 0–1000
        if (tick >= 23000 || tick < 1000) return DAWN;
        if (tick < 4000) return MORNING;
        if (tick < 7000) return MIDDAY;
        if (tick < 11000) return AFTERNOON;
        if (tick < 13500) return DUSK;
        if (tick < 18000) return EVENING;
        return NIGHT;
    }

    /** Human-readable label. */
    public String label() {
        return switch (this) {
            case DAWN -> "Dawn (黎明)";
            case MORNING -> "Morning (上午)";
            case MIDDAY -> "Midday (正午)";
            case AFTERNOON -> "Afternoon (下午)";
            case DUSK -> "Dusk (黄昏)";
            case EVENING -> "Evening (傍晚)";
            case NIGHT -> "Night (夜晚)";
        };
    }
}
