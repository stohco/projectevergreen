/**
 * <h1>Entity AI Goals — Layer 2 simulation behaviors for reified NPC shells.</h1>
 *
 * <p>This subpackage contains Minecraft {@code Goal} implementations used by
 * {@link dev.ergenverse.entity.EntityCultivator}. Each goal corresponds to
 * one {@link dev.ergenverse.simulation.CultivatorActivity} state — the goal
 * activates when the simulation (via
 * {@link dev.ergenverse.simulation.CultivatorActivityResolver}) says the NPC
 * is doing that activity, and deactivates when the simulation changes its
 * mind.
 *
 * <h2>Design</h2>
 * <p>The goals are deliberately thin — they don't decide what to do, they
 * execute what the simulation has already decided. The decision lives in
 * {@link dev.ergenverse.simulation.CultivatorActivityResolver} (Layer 2);
 * the goals here are Layer 3 (emergent visible behavior) — they translate
 * the simulation's decision into actual entity motion and pose.
 *
 * <ul>
 *   <li>{@link dev.ergenverse.entity.ai.CultivatorMeditationGoal} —
 *       activates when activity is {@code MEDITATING} (or
 *       {@code BREAKING_THROUGH}, which is stationary); stops movement,
 *       holds the entity stationary in a meditation pose. Particle effects
 *       are spawned separately in {@code EntityCultivator.aiStep()} so they
 *       fire regardless of goal state (defensive — if the goal fails to
 *       activate, particles still spawn).</li>
 * </ul>
 *
 * <h2>Why not a Goal for breakthrough?</h2>
 * <p>Breakthrough is a transient visual state, not a behavior — the entity
 * is stationary (same as meditation) but with intense particles. There's
 * no separate behavior to run, so the {@code CultivatorMeditationGoal}
 * handles both: when activity is {@code BREAKING_THROUGH}, the meditation
 * goal activates (entity stationary) and {@code EntityCultivator.aiStep()}
 * spawns the more dramatic breakthrough particles based on the activity
 * enum.
 *
 * <h2>Layer归属</h2>
 * <p>Layer 2 (Simulation). The canon JSONs do not prescribe behavior trees
 * per character — they describe attributes. The simulation layer decides
 * what an NPC does moment-to-moment; this package implements that decision
 * as Minecraft AI goals.
 */
package dev.ergenverse.entity.ai;
