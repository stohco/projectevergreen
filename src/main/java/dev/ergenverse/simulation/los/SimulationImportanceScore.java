package dev.ergenverse.simulation.los;

/**
 * SimulationImportanceScore — a dynamic 0-100 score for an entity.
 *
 * <p>The LoS engine uses this score to decide which {@link SimulationLevel}
 * to assign: 0-9 = STATIC_DATA, 10-24 = HISTORICAL, 25-49 = TERRITORY,
 * 50-74 = ACTIVE_ACTOR, 75-89 = FULL_COGNITION, 90-100 = STORY_IMPORTANCE.
 *
 * <p>The score is DYNAMIC — it can rise and fall. The 6 contributors (canon audit):
 * <ol>
 *   <li>Distance — closer to a player = more important.</li>
 *   <li>Story significance — canon-protagonist / canon-antagonist multiplier.</li>
 *   <li>Active interaction — currently in combat / dialogue / trade with player.</li>
 *   <li>Player influence — player has affected the entity's state.</li>
 *   <li>World events — entity is referenced by an active world event.</li>
 *   <li>Divine sense — a cultivator is currently scrying this entity.</li>
 * </ol>
 */
public final class SimulationImportanceScore {

    /** The 6 contributors. */
    public double distance;             // 0..1, where 1=closest
    public double storySignificance;    // 0..1
    public double activeInteraction;    // 0..1
    public double playerInfluence;      // 0..1
    public double worldEventSalience;   // 0..1
    public double divineSenseFocus;     // 0..1

    public SimulationImportanceScore() {
        this.distance = 0;
        this.storySignificance = 0;
        this.activeInteraction = 0;
        this.playerInfluence = 0;
        this.worldEventSalience = 0;
        this.divineSenseFocus = 0;
    }

    /** Compute the 0-100 score. */
    public int score() {
        double s = 0;
        s += distance           * 25.0;
        s += storySignificance  * 30.0;
        s += activeInteraction  * 20.0;
        s += playerInfluence    * 10.0;
        s += worldEventSalience * 10.0;
        s += divineSenseFocus   * 5.0;
        if (s < 0) s = 0;
        if (s > 100) s = 100;
        return (int) Math.round(s);
    }

    /** Map score → SimulationLevel. */
    public SimulationLevel level() {
        int s = score();
        if (s >= 90) return SimulationLevel.STORY_IMPORTANCE;
        if (s >= 75) return SimulationLevel.FULL_COGNITION;
        if (s >= 50) return SimulationLevel.ACTIVE_ACTOR;
        if (s >= 25) return SimulationLevel.TERRITORY;
        if (s >= 10) return SimulationLevel.HISTORICAL;
        return SimulationLevel.STATIC_DATA;
    }

    @Override
    public String toString() {
        return "SIScore[" + score() + " → " + level() + "]";
    }
}
