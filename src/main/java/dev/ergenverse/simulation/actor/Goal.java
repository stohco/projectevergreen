package dev.ergenverse.simulation.actor;

/**
 * Goal — a priority-driven goal in the actor framework.
 *
 * <p>Distinct from {@link dev.ergenverse.simulation.cognition.CognitionGoal}:
 * the actor-framework Goal is the SIMULATION-LAYER goal (what the world
 * asks of the actor), while CognitionGoal is the COGNITION-LAYER goal
 * (what the actor decides to pursue, after needs + Dao weighting).
 *
 * <p>A Goal carries:
 * <ul>
 *   <li>{@link Category} — what kind of goal (combat, social, cultivation, etc.)</li>
 *   <li>{@link Status} — pending / active / achieved / failed / abandoned</li>
 *   <li>{@code priority} — 0..1, drives scheduling</li>
 *   <li>{@code deadlineTick} — when the goal expires (or Long.MAX_VALUE)</li>
 * </ul>
 */
public final class Goal {

    public enum Category {
        CULTIVATION, COMBAT, SOCIAL, ECONOMIC, POLITICAL,
        EXPLORATION, CRAFTING, LEARNING, SURVIVAL, LEGACY, OTHER
    }

    public enum Status {
        PENDING, ACTIVE, ACHIEVED, FAILED, ABANDONED
    }

    public final String id;
    public final String description;
    public final Category category;
    public double priority;        // 0..1
    public long   deadlineTick;    // Long.MAX_VALUE = no deadline
    public Status status;

    public Goal(String id, String description, Category category,
                double priority, long deadlineTick) {
        this.id = id;
        this.description = description;
        this.category = category;
        this.priority = clamp01(priority);
        this.deadlineTick = deadlineTick;
        this.status = Status.PENDING;
    }

    public boolean isExpired(long currentTick) {
        return currentTick > deadlineTick && deadlineTick != Long.MAX_VALUE;
    }

    public boolean isActive()     { return status == Status.ACTIVE; }
    public boolean isAchieved()   { return status == Status.ACHIEVED; }
    public boolean isFinished()   { return status == Status.ACHIEVED
            || status == Status.FAILED
            || status == Status.ABANDONED; }

    public void activate()    { if (status == Status.PENDING) status = Status.ACTIVE; }
    public void achieve()     { status = Status.ACHIEVED; }
    public void fail()        { status = Status.FAILED; }
    public void abandon()     { status = Status.ABANDONED; }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
