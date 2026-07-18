package dev.ergenverse.simulation.cognition;

/**
 * PhysicalState — the actor's body, mortal-tier state.
 *
 * <p>Tracked: health, hunger, thirst, fatigue, injuries, poison, age,
 * bloodline purity, lifespan remaining, and a coarse location/proximity.
 *
 * <p>This is a value-only class — no behavior. The {@link GoalGenerator}
 * and {@link Planner} read it; the world tick mutates it.
 */
public final class PhysicalState {

    public double healthFraction;   // 0..1
    public double hunger;           // 0..1 (1=starving)
    public double thirst;           // 0..1 (1=dehydrated)
    public double fatigue;          // 0..1
    public double injurySeverity;   // 0..1
    public double poisonSeverity;   // 0..1
    public int    ageYears;
    public int    lifespanYears;
    public double bloodlinePurity;  // 0..1
    public long   lastTickUpdated;

    public PhysicalState() {
        this.healthFraction = 1.0;
        this.hunger = 0.0;
        this.thirst = 0.0;
        this.fatigue = 0.0;
        this.injurySeverity = 0.0;
        this.poisonSeverity = 0.0;
        this.ageYears = 16;
        this.lifespanYears = 100;
        this.bloodlinePurity = 0.1;
        this.lastTickUpdated = 0;
    }

    /** Mortality pressure = avg of health, hunger, thirst, injury, poison. */
    public double mortalityPressure() {
        double physical = (1 - healthFraction) * 0.4
                        + hunger * 0.2
                        + thirst * 0.2
                        + injurySeverity * 0.1
                        + poisonSeverity * 0.1;
        if (physical < 0) return 0;
        if (physical > 1) return 1;
        return physical;
    }

    public boolean isDying() {
        return healthFraction < 0.05 || hunger > 0.95 || thirst > 0.95;
    }

    public boolean isAging() {
        return ageYears > lifespanYears * 0.8;
    }
}
