package dev.ergenverse.simulation.cognition;

import dev.ergenverse.simulation.actor.Territory;

import java.util.HashMap;
import java.util.Map;

/**
 * Ontology — the unified entity structure for cognition-driven actors.
 *
 * <p>An Ontology bundles the 11 canonical "pieces" of an actor into a single
 * value object. This is the CANONICAL ACTOR MODEL for the cognition system.
 *
 * <p>The 11 pieces (canon audit):
 * <ol>
 *   <li>{@link Identity} — stable id, name, type</li>
 *   <li>{@link PhysicalState} — body, mortality, age</li>
 *   <li>{@link CultivationState} — realm, qi, dao-heart, planning horizon</li>
 *   <li>{@link SocialState} — faction, allies, enemies, territory</li>
 *   <li>{@link Territory} — territory the actor controls (if any)</li>
 *   <li>{@link Knowledge} set — verified facts</li>
 *   <li>{@link BeliefRegistry} — unverified beliefs</li>
 *   <li>{@link PersonalityModel} — trait curves (the heart of decision-making)</li>
 *   <li>Goals — current goal queue (managed by {@link DecisionEngine})</li>
 *   <li>{@link Planner} output — current plan / hierarchy of steps</li>
 *   <li>{@link MemoryGraph} — episodic memory</li>
 * </ol>
 *
 * <p>The {@link ReputationRegistry} is shared per-faction, not per-actor, so it
 * is held externally.
 */
public final class Ontology {

    /** Identity — stable identification of the actor. */
    public static final class Identity {
        public final String id;
        public final String displayName;
        public final String typeId;  // "npc", "beast", "spirit", "sect_leader", ...
        public Identity(String id, String displayName, String typeId) {
            this.id = id;
            this.displayName = displayName;
            this.typeId = typeId;
        }
    }

    public final Identity identity;
    public final PhysicalState physical;
    public final CultivationState cultivation;
    public final SocialState social;
    public final Territory territory;
    public final PersonalityModel personality;
    public final MemoryGraph memory;
    public final BeliefRegistry beliefs;

    /** Verified knowledge map keyed by {@code subject.predicate}. */
    public final Map<String, Knowledge> knowledge = new HashMap<>();

    /** The actor's primary Dao identity. Drives goal weighting. */
    public DaoIdentity daoIdentity = DaoIdentity.SEEKING_DAO;

    /** Currently active goal (set by the DecisionEngine each tick). */
    public CognitionGoal activeGoal = null;

    /**
     * Currently active intent (set by the IntentEngine each cognition tick).
     * The intent is the strategic framing of the current behavior — the "WHY"
     * behind the action. Sits between Goal (hours) and Decision/Action (seconds).
     * May be null if no goal is active or intent derivation failed.
     */
    public dev.ergenverse.simulation.intent.Intent activeIntent = null;

    /** Currently active plan (set by the Planner when a goal is chosen). */
    public Plan activePlan = null;

    public Ontology(Identity identity) {
        this.identity = identity;
        this.physical = new PhysicalState();
        this.cultivation = new CultivationState();
        this.social = new SocialState();
        this.territory = null;
        this.personality = new PersonalityModel();
        this.memory = new MemoryGraph();
        this.beliefs = new BeliefRegistry();
    }

    /** Convenience: store a verified fact. */
    public void addKnowledge(Knowledge k) {
        knowledge.put(k.key(), k);
    }

    /** Convenience: lookup a verified fact. */
    public Knowledge getKnowledge(String subject, String predicate) {
        return knowledge.get(subject + "." + predicate);
    }

    /** Need intensities for this actor — defaults derived from physical + cultivation. */
    public Map<Need, Double> computeNeedIntensities() {
        Map<Need, Double> needs = new HashMap<>();
        // Survival
        needs.put(Need.FOOD,    physical.hunger);
        needs.put(Need.WATER,   physical.thirst);
        needs.put(Need.REST,    physical.fatigue);
        needs.put(Need.SAFETY,  physical.mortalityPressure());
        needs.put(Need.SHELTER, physical.mortalityPressure() * 0.5);
        // Cultivation
        needs.put(Need.QI,             1.0 - cultivation.qiFraction());
        needs.put(Need.SEEKING_DAO,    cultivation.inSeclusion() ? 0.8 : 0.3);
        needs.put(Need.DAO_HEART,      1.0 - cultivation.daoHeartStability());
        needs.put(Need.BREAKTHROUGH,   cultivation.breakthroughReadiness());
        needs.put(Need.TRIBULATION_DEBT, cultivation.karmicDebt());
        needs.put(Need.RESOURCE,       1.0 - cultivation.qiFraction());
        // Social
        needs.put(Need.AFFECTION,        social.hasAlly() ? 0.2 : 0.6);
        needs.put(Need.BELONGING,        social.inFaction() ? 0.2 : 0.6);
        needs.put(Need.FACE,             0.4);
        needs.put(Need.REPUTATION,       0.4);
        needs.put(Need.STATUS,           social.isSectLeader() ? 0.2 : 0.5);
        // Karmic / moral
        needs.put(Need.KARMIC_DEBT,      cultivation.karmicDebt());
        needs.put(Need.HEART_DEMON,      1.0 - cultivation.daoHeartStability());
        needs.put(Need.FILIAL_PIETY,     social.hasMaster ? 0.4 : 0.1);
        needs.put(Need.SWORN_BROTHERHOOD, social.hasSwornBrothers ? 0.3 : 0.4);
        needs.put(Need.GRATITUDE,        !social.debtsOwedTo.isEmpty() ? 0.6 : 0.2);
        needs.put(Need.REVENGE,          social.hasEnemy() ? 0.7 : 0.0);
        // Info / mastery
        needs.put(Need.KNOWLEDGE,        0.4);
        needs.put(Need.CRAFT,            0.3);
        // Autonomy
        needs.put(Need.FREEDOM,          social.inFaction() ? 0.4 : 0.1);
        needs.put(Need.CURIOSITY,        0.4);
        // Self-actualization
        needs.put(Need.LEGACY,           cultivation.realmOrder() >= 4 ? 0.5 : 0.2);
        needs.put(Need.TRANSCENDENCE,    cultivation.realmOrder() >= 7 ? 0.5 : 0.1);
        return needs;
    }
}
