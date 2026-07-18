package dev.ergenverse.manipulation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.core.WorldPhilosophy;

/**
 * The Heaven and Earth Manipulation engine — the universal system for
 * interacting with world objects.
 *
 * <p>Per the {@link WorldPhilosophy} and the design document: every object
 * in the world — from a pebble to a continent — has the same underlying
 * properties. The player's capability (cultivation, divine sense, technique,
 * Dao, treasure, formation) is compared against the object's resistance
 * (physical mass, spiritual mass, Dao anchoring, world law, formation
 * anchoring, owner resistance, historical Dao imprint, heavenly resistance,
 * karmic significance).
 *
 * <h2>The Three Manipulation Types</h2>
 *
 * <p>Per the Er Gen examples (Wang Lin's restriction mountain, Greed's
 * mountain soul extraction, Bai Fan's Mountain Crumble), there are THREE
 * distinct kinds of interaction. They are NOT interchangeable:
 *
 * <ol>
 *   <li><b>PHYSICAL</b> — move/break the physical mass. Brute force.
 *       Requires telekinetic force + Qi. Does NOT require Dao or perception.</li>
 *   <li><b>SPIRITUAL</b> — interact with the spiritual essence. Extract
 *       veins, move souls, awaken spirits. Requires divine sense +
 *       specialized method. Does NOT require brute force. Raw cultivation
 *       alone does NOT grant this.</li>
 *   <li><b>DAO</b> — transform the object's nature. Mountain Crumble,
 *       mountain-to-seal, mountain-to-treasure. Requires Dao compatibility
 *       + transformation technique. Does NOT require force or perception.
 *       This is how Wang Lin shrank a mountain at early First Step.</li>
 * </ol>
 *
 * <h2>The Comparison</h2>
 *
 * <pre>
 *   If Capability > Resistance        → SUCCESS (clean)
 *   If Capability > Resistance × 0.7  → PARTIAL (barely budges or fractures)
 *   If Capability ≤ Resistance × 0.7  → FAILURE (backlash)
 * </pre>
 *
 * <h2>No Hardcoded Realms</h2>
 *
 * <p>Per the design: an exceptionally talented Soul Formation cultivator
 * with a legendary earth-moving technique might relocate a modest mountain.
 * An ordinary Soul Formation cultivator cannot. A Third Step cultivator
 * might move entire mountain ranges. A Fourth Step cultivator may alter
 * the geography of a world. The equation handles all of this — no
 * "mountain moving realm" gate.
 */
public final class HeavenAndEarthManipulation {

    private HeavenAndEarthManipulation() {}

    /** The three manipulation types. */
    public enum Type {
        PHYSICAL("Physical Manipulation",
            "Move or break the physical mass. Brute force — telekinetic force + Qi + technique + treasure."),
        SPIRITUAL("Spiritual Manipulation",
            "Interact with the spiritual essence. Extract veins, move souls, awaken spirits. Divine sense + specialized method."),
        DAO("Dao Manipulation",
            "Transform the object's nature. Mountain Crumble, mountain-to-seal, mountain-to-treasure. Dao compatibility + transformation technique.");

        public final String name;
        public final String description;
        Type(String name, String description) { this.name = name; this.description = description; }
    }

    /** The result of a manipulation attempt. */
    public static final class Result {
        public enum Outcome { SUCCESS, PARTIAL, FAILURE }

        public final Type type;
        public final Outcome outcome;
        public final double capability;
        public final double resistance;
        public final double margin;          // capability / resistance (>1 = success)
        public final String description;
        public final Backlash backlash;

        public Result(Type type, Outcome outcome, double capability, double resistance,
                      String description, Backlash backlash) {
            this.type = type;
            this.outcome = outcome;
            this.capability = capability;
            this.resistance = resistance;
            this.margin = resistance > 0 ? capability / resistance : Double.MAX_VALUE;
            this.description = description;
            this.backlash = backlash;
        }

        public boolean succeeded() { return outcome == Outcome.SUCCESS; }

        public String summary() {
            return String.format("[%s] %s — capability %.0f vs resistance %.0f (margin %.2fx): %s",
                type.name(), outcome, capability, resistance, margin, description);
        }
    }

    /** Backlash from a failed manipulation. */
    public static final class Backlash {
        public final double qiDrain;          // Qi lost
        public final double divineSenseDrain; // S_sense lost
        public final boolean soulFracture;    // did the player's soul fracture?
        public final boolean daoHeartWavered; // did the player's Dao heart waver?
        public final boolean heartDemonRisk;  // is there a heart demon risk?
        public final String description;

        public Backlash(double qiDrain, double divineSenseDrain, boolean soulFracture,
                        boolean daoHeartWavered, boolean heartDemonRisk, String description) {
            this.qiDrain = qiDrain;
            this.divineSenseDrain = divineSenseDrain;
            this.soulFracture = soulFracture;
            this.daoHeartWavered = daoHeartWavered;
            this.heartDemonRisk = heartDemonRisk;
            this.description = description;
        }

        public static Backlash none() {
            return new Backlash(0, 0, false, false, false, "");
        }
    }

    // ─── Physical manipulation ───────────────────────────────────────

    /**
     * Attempt PHYSICAL manipulation — move or break the physical mass.
     *
     * <p>Resistance = physicalMass + worldLawResistance + formationAnchoring
     * + ownerResistance + heavenlyResistance.
     *
     * <p>Capability = telekineticForce × (1 + technique) + treasure +
     * formation + divineSense × 0.1 + telekineticForce × daoCompatibility × 0.3.
     *
     * <p>Failure: the object barely budges or partially fractures. Qi drains.
     * No spiritual consequence.
     */
    public static Result attemptPhysical(WorldObject target, ManipulationCapability capability,
                                         double currentQi) {
        double resistance = computePhysicalResistance(target);
        double cap = capability.physicalCapability();

        Ergenverse.LOGGER.debug("[Ergenverse] Physical manipulation: cap={} vs res={}", cap, resistance);

        if (cap >= resistance) {
            // Success
            double qiCost = target.physicalMass * 0.001 * (1.0 / Math.max(0.1, cap / resistance));
            return new Result(Type.PHYSICAL, Result.Outcome.SUCCESS, cap, resistance,
                "The " + target.material + " yields to your force. It moves cleanly.",
                new Backlash(qiCost, 0, false, false, false, "Qi drain from the effort"));
        }

        if (cap >= resistance * 0.7) {
            // Partial — barely budges or fractures
            double qiCost = target.physicalMass * 0.005;
            return new Result(Type.PHYSICAL, Result.Outcome.PARTIAL, cap, resistance,
                "The " + target.material + " barely budges. It partially fractures — pieces break off. You need more force, or a better technique.",
                new Backlash(qiCost, 0, false, false, false, "Heavy Qi drain from the failed effort"));
        }

        // Failure — backlash
        double qiCost = currentQi * 0.3;  // lose 30% of current Qi
        boolean soulFracture = cap < resistance * 0.3;  // massive failure
        return new Result(Type.PHYSICAL, Result.Outcome.FAILURE, cap, resistance,
            "The " + target.material + " refuses to budge. Your force rebounds — backlash!" +
            (soulFracture ? " The rebound fractures your soul!" : ""),
            new Backlash(qiCost, soulFracture ? capability.divineSenseStrength * 0.5 : 0,
                soulFracture, false, false, "Backlash from failed physical manipulation"));
    }

    private static double computePhysicalResistance(WorldObject obj) {
        double ownerResistance = obj.ownerId != null ? 500 : 0;
        return obj.physicalMass
             + obj.worldLawResistance * 1000
             + obj.formationAnchoring * 2000
             + ownerResistance
             + obj.heavenlyResistance * 5000
             + obj.karmicSignificance * 1000;  // karmically significant objects resist
    }

    // ─── Spiritual manipulation ──────────────────────────────────────

    /**
     * Attempt SPIRITUAL manipulation — interact with the spiritual essence.
     *
     * <p>Resistance = spiritualMass + formationAnchoring + ownerResistance
     * + heavenlyResistance + karmicSignificance.
     *
     * <p>Capability = divineSense × (1 + technique) + treasure +
     * divineSense × daoCompatibility × 0.5.
     *
     * <p>NOTE: telekineticForce is deliberately excluded — brute force
     * cannot extract a soul. This is why a brute-force Soul Formation
     * cultivator cannot extract a spirit vein, but a perceptive Nascent
     * Soul with the right technique can.
     *
     * <p>Failure: the spiritual essence slips away, or the object's soul
     * resists. Divine sense strain accumulates. If the object has a
     * formation anchor, the formation may alert its owner.
     */
    public static Result attemptSpiritual(WorldObject target, ManipulationCapability capability) {
        double resistance = computeSpiritualResistance(target);
        double cap = capability.spiritualCapability();

        Ergenverse.LOGGER.debug("[Ergenverse] Spiritual manipulation: cap={} vs res={}", cap, resistance);

        if (cap >= resistance) {
            double strain = target.spiritualMass * 0.01;
            return new Result(Type.SPIRITUAL, Result.Outcome.SUCCESS, cap, resistance,
                "Your divine sense grips the " + (target.hasSoul ? target.soulNature : "spiritual essence") + " and pulls it free. The physical " + target.material + " remains, but its spirit is yours.",
                new Backlash(0, strain, false, false, false, "Divine sense strain from the extraction"));
        }

        if (cap >= resistance * 0.7) {
            double strain = target.spiritualMass * 0.05;
            return new Result(Type.SPIRITUAL, Result.Outcome.PARTIAL, cap, resistance,
                "The " + (target.hasSoul ? target.soulNature : "spiritual essence") + " slips partially free, then resists. You need deeper perception or a better method.",
                new Backlash(0, strain, false, false, false, "Divine sense strain from the partial extraction"));
        }

        double strain = capability.divineSenseStrength * 0.3;
        boolean soulFracture = cap < resistance * 0.3;
        return new Result(Type.SPIRITUAL, Result.Outcome.FAILURE, cap, resistance,
            "The " + (target.hasSoul ? target.soulNature : "spiritual essence") + " resists your divine sense and rebuffs you!" +
            (target.formationAnchoring > 0.5 ? " The formation anchoring this object flares — its owner has been alerted." : "") +
            (soulFracture ? " The spiritual rebound fractures your soul!" : ""),
            new Backlash(0, strain, soulFracture, false, false, "Spiritual backlash from failed extraction"));
    }

    private static double computeSpiritualResistance(WorldObject obj) {
        double ownerResistance = obj.ownerId != null ? 1000 : 0;
        return obj.spiritualMass
             + obj.formationAnchoring * 3000    // formations strongly anchor spiritual things
             + ownerResistance
             + obj.heavenlyResistance * 5000
             + obj.karmicSignificance * 2000;
    }

    // ─── Dao manipulation ────────────────────────────────────────────

    /**
     * Attempt DAO manipulation — transform the object's nature.
     *
     * <p>Resistance = daoAnchoring + historicalDaoImprint + heavenlyResistance
     * + karmicSignificance.
     *
     * <p>Capability = daoCompatibility × 10000 × (1 + technique) + treasure.
     *
     * <p>NOTE: telekineticForce and divineSenseStrength are deliberately
     * excluded — Dao manipulation is purely about understanding + technique.
     * This is how Wang Lin shrank a mountain at early First Step: his
     * restriction technique (a Dao method) overcame the mountain's Dao
     * anchoring, even though his raw power was far below "mountain moving."
     *
     * <p>Failure: the Dao rebels. The object resists transformation. Dao
     * heart wavers, comprehension may drop, heart demon risk. If the
     * object has a Historical Dao Imprint, the imprint's creator may
     * notice you.
     */
    public static Result attemptDao(WorldObject target, ManipulationCapability capability,
                                    String transformationDescription) {
        double resistance = computeDaoResistance(target);
        double cap = capability.daoCapability();

        Ergenverse.LOGGER.debug("[Ergenverse] Dao manipulation: cap={} vs res={}", cap, resistance);

        if (cap >= resistance) {
            return new Result(Type.DAO, Result.Outcome.SUCCESS, cap, resistance,
                "Your Dao overwhelms the " + target.material + "'s nature. " + transformationDescription,
                Backlash.none());
        }

        if (cap >= resistance * 0.7) {
            return new Result(Type.DAO, Result.Outcome.PARTIAL, cap, resistance,
                "The " + target.material + " partially transforms, then resists. Your Dao is not yet deep enough to fully rewrite its nature.",
                new Backlash(0, 0, false, true, false, "Dao heart wavered from the resistance"));
        }

        boolean heartDemon = cap < resistance * 0.3;
        boolean imprintAlert = target.historicalDaoImprint > 0.5;
        return new Result(Type.DAO, Result.Outcome.FAILURE, cap, resistance,
            "The " + target.material + "'s Dao rebels against yours! Transformation fails." +
            (imprintAlert ? " The historical Dao imprint flares — its creator may have noticed you." : "") +
            (heartDemon ? " A heart demon stirs from the Dao conflict!" : ""),
            new Backlash(0, 0, false, true, heartDemon,
                "Dao backlash from failed transformation" + (imprintAlert ? " + imprint creator alerted" : "")));
    }

    private static double computeDaoResistance(WorldObject obj) {
        return obj.daoAnchoring * 10000
             + obj.historicalDaoImprint * 20000  // Bai Fan's imprint is very hard to overcome
             + obj.heavenlyResistance * 5000
             + obj.karmicSignificance * 3000;    // karmically significant objects resist transformation
    }

    // ─── Combined assessment (for UI: "can I move this?") ────────────

    /**
     * Assess all three manipulation types at once.
     *
     * <p>Used for the UI tooltip: when the player looks at an object with
     * divine sense active, show all three results so they can choose
     * their approach.
     */
    public static ManipulationAssessment assess(WorldObject target, ManipulationCapability capability,
                                                double currentQi) {
        return new ManipulationAssessment(
            attemptPhysical(target, capability, currentQi),
            attemptSpiritual(target, capability),
            attemptDao(target, capability, "transform")
        );
    }

    /** The full assessment across all three manipulation types. */
    public static final class ManipulationAssessment {
        public final Result physical;
        public final Result spiritual;
        public final Result dao;

        public ManipulationAssessment(Result physical, Result spiritual, Result dao) {
            this.physical = physical;
            this.spiritual = spiritual;
            this.dao = dao;
        }

        /** Which manipulation type has the best margin? */
        public Type bestApproach() {
            double physMargin = physical.margin;
            double spirMargin = spiritual.margin;
            double daoMargin = dao.margin;
            if (physMargin >= spirMargin && physMargin >= daoMargin) return Type.PHYSICAL;
            if (spirMargin >= daoMargin) return Type.SPIRITUAL;
            return Type.DAO;
        }
    }
}
