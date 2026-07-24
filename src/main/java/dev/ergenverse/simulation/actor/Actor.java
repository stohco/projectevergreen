package dev.ergenverse.simulation.actor;

import dev.ergenverse.simulation.cognition.DaoIdentity;
import dev.ergenverse.simulation.cognition.Ontology;
import dev.ergenverse.simulation.cognition.perception.Interpretation;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;
import dev.ergenverse.simulation.cognition.prediction.ActionPredictor;
import dev.ergenverse.simulation.los.SimulationImportanceScore;
import dev.ergenverse.simulation.los.SimulationLevel;
import dev.ergenverse.simulation.cognition.ActivityProcess;
import dev.ergenverse.simulation.intent.CultivationTask;
import dev.ergenverse.simulation.intent.Intent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Actor — the unified actor in the simulation.
 *
 * <p>Every world-actor — beast, NPC, player, sect, settlement, spirit vein,
 * formation, territory — is an {@code Actor}. The {@link ActorType} field
 * distinguishes their behavior; the {@link Ontology} holds their cognition
 * state (if they have one); the {@link SimulationImportanceScore} decides
 * how much simulation effort they receive.
 *
 * <h2>Provenance</h2>
 * <p>Every actor carries a provenance string (where it came from in the canon)
 * and a canon-confidence flag. Canon-actor provenance is non-null; procedurally
 * generated actors have provenance "procedural".
 */
public final class Actor {

    public final String id;
    public final ActorType type;
    public final String provenance;      // "canon:wang_lin", "procedural", "canon_seeded:sect_x", ...
    public final String canonConfidence; // NOVEL_STATEMENT, INFERRED, FILLER...

    /** Display name (canonical name for canon actors; generated name otherwise). */
    public String displayName;

    /** Capabilities this actor possesses. */
    public final Set<Capability> capabilities = EnumSet.noneOf(Capability.class);

    /** Current goals (simulation-layer). */
    public final List<Goal> goals = new ArrayList<>();

    /** Cognition state — only populated if simLevel >= ACTIVE_ACTOR. */
    public Ontology cognition;

    /** Dao identity — only relevant for cultivators and beasts ≥ SPIRIT. */
    public DaoIdentity daoIdentity = null;

    /** Beast intelligence tier — only relevant for BEAST-type actors. */
    public BeastIntelligence beastTier = null;

    /** LoS — simulation importance score (re-evaluated each tick). */
    public final SimulationImportanceScore importance = new SimulationImportanceScore();

    /** Current simulation level (derived from {@link #importance}). */
    public SimulationLevel simLevel = SimulationLevel.STATIC_DATA;

    /** Last-simulated tick (for the {@link ActorTickLoop} scheduler). */
    public long lastSimulatedTick = 0;

    /** Current activity process (null when idle). Populated by DecisionEngine wiring. */
    public ActivityProcess currentActivity = null;

    // ── Article XXXV cognition chain (Perception -> ... -> Action) ──
    /** Last raw perception snapshot (before attention filter). */
    public PerceptionSnapshot lastRawPerception = null;
    /** Last perception snapshot after attention filter (what the actor actually noticed). */
    public PerceptionSnapshot lastPerception = null;
    /** Last interpretation of the attended perception (null until first cognition tick). */
    public Interpretation lastInterpretation = null;
    /** Last action-outcome prediction for the chosen action (null until first cognition tick). */
    public ActionPredictor.Outcome lastPrediction = null;
    /** Current active intent (derived by IntentEngine). */
    public Intent activeIntent = null;
    /** Current task queue from IntentDecomposer (ordered steps). */
    public final List<CultivationTask> activeTasks = new ArrayList<>();
    /** Index of the currently executing task in the queue (-1 if idle). */
    public int currentTaskIndex = -1;

    /** Block position (used for distance-to-player importance calc). */
    public int blockX, blockY, blockZ;

    public Actor(String id, ActorType type, String provenance, String canonConfidence) {
        this.id = id;
        this.type = type;
        this.provenance = provenance;
        this.canonConfidence = canonConfidence;
        this.displayName = id;
    }

    public boolean hasCapability(Capability c) {
        return capabilities.contains(c);
    }

    public void grant(Capability c) {
        capabilities.add(c);
    }

    public void revoke(Capability c) {
        capabilities.remove(c);
    }

    /** Re-evaluate simulation level from importance score. */
    public void recomputeSimLevel() {
        this.simLevel = importance.level();
    }

    /** Is this actor canon-sourced (not procedural)? */
    public boolean isCanon() {
        return provenance != null && provenance.startsWith("canon");
    }
}
