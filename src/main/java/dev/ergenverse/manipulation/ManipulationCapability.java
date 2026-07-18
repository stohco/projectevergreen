package dev.ergenverse.manipulation;

import dev.ergenverse.cultivation.RealmId;

import java.util.Map;

/**
 * The player's capability for Heaven and Earth Manipulation.
 *
 * <p>This is the "can I move this?" side of the comparison. It aggregates
 * all the player's relevant strengths:
 *
 * <ul>
 *   <li><b>telekineticForce</b> — raw power, f(realm, currentQi)</li>
 *   <li><b>divineSenseStrength</b> — S_sense, perception/precision</li>
 *   <li><b>daoCompatibility</b> — how well the player's Dao matches the target's daoAffinity (0-1)</li>
 *   <li><b>techniqueMultiplier</b> — the technique's power (grade × comprehension)</li>
 *   <li><b>treasureBonus</b> — artifacts that boost manipulation (restriction flag, etc.)</li>
 *   <li><b>formationBonus</b> — if standing in a helpful formation</li>
 * </ul>
 *
 * <p>Per the design: NO hardcoded realms. An exceptional Soul Formation with
 * a legendary technique and treasure can outperform an ordinary higher-realm
 * cultivator. The equation handles it.
 */
public final class ManipulationCapability {

    public final double telekineticForce;
    public final double divineSenseStrength;
    public final double daoCompatibility;      // 0-1: how well the player's Dao matches the target
    public final double techniqueMultiplier;   // technique grade × comprehension
    public final double treasureBonus;
    public final double formationBonus;

    public ManipulationCapability(double telekineticForce, double divineSenseStrength,
                                  double daoCompatibility, double techniqueMultiplier,
                                  double treasureBonus, double formationBonus) {
        this.telekineticForce = telekineticForce;
        this.divineSenseStrength = divineSenseStrength;
        this.daoCompatibility = daoCompatibility;
        this.techniqueMultiplier = techniqueMultiplier;
        this.treasureBonus = treasureBonus;
        this.formationBonus = formationBonus;
    }

    /**
     * Compute the player's capability for PHYSICAL manipulation.
     *
     * <p>Physical manipulation is moving/breaking the physical mass.
     *
     * <p>In Er Gen, divine sense is NOT just "precision" — it IS the
     * mechanism by which cultivators interact with the physical world at
     * range. Wang Lin grips and throws objects with his divine sense; the
     * Ji Realm Divine Sense is a weapon in its own right. Divine sense is
     * the "hand" that grips; Qi/realm provides the raw force behind the
     * grip. Both matter — and both scale together.
     *
     * <p>The formula:
     * <ul>
     *   <li><b>telekineticForce</b> × (1 + technique) — raw power, scaled by technique</li>
     *   <li><b>divineSenseStrength</b> × (1 + technique) — the gripping force, scaled by technique.
     *       Divine sense is the PRIMARY mechanism for physical manipulation in Er Gen.</li>
     *   <li><b>treasureBonus + formationBonus</b> — external multipliers</li>
     *   <li><b>telekineticForce × daoCompatibility × 0.3</b> — Dao helps but isn't required</li>
     *   <li><b>divineSenseStrength × daoCompatibility × 0.5</b> — Dao matters more for the gripping</li>
     * </ul>
     *
     * <p>This means a cultivator with high divine sense but lower realm
     * (like Wang Lin with his soul-eating methods) can still manipulate
     * objects effectively — because divine sense IS the gripping force,
     * not just "precision." The S_tempering path (soul-eating) directly
     * boosts physical manipulation.
     */
    public double physicalCapability() {
        return telekineticForce * (1 + techniqueMultiplier)
             + divineSenseStrength * (1 + techniqueMultiplier)  // divine sense IS the gripping force, not just precision
             + treasureBonus + formationBonus
             + telekineticForce * daoCompatibility * 0.3
             + divineSenseStrength * daoCompatibility * 0.5;  // Dao matters more for the gripping
    }

    /**
     * Compute the player's capability for SPIRITUAL manipulation.
     *
     * <p>Spiritual manipulation interacts with the object's spiritual
     * essence — extracting spirit veins, moving mountain souls, awakening
     * spirits. It relies on divine sense + specialized method (technique).
     * Raw telekinetic force does NOT help — you can't brute-force a soul.
     * Dao compatibility helps if the player's Dao matches the object's
     * spiritual nature.
     */
    public double spiritualCapability() {
        return divineSenseStrength * (1 + techniqueMultiplier) + treasureBonus
             + divineSenseStrength * daoCompatibility * 0.5;  // dao matters more for spiritual
        // NOTE: telekineticForce deliberately excluded — brute force cannot extract a soul
    }

    /**
     * Compute the player's capability for DAO manipulation.
     *
     * <p>Dao manipulation transforms the object's fundamental nature —
     * Mountain Crumble, mountain-to-seal, mountain-to-treasure. It relies
     * on Dao compatibility + technique. Raw force and divine sense do NOT
     * help — this is about understanding, not power or perception.
     */
    public double daoCapability() {
        return daoCompatibility * 10000 * (1 + techniqueMultiplier) + treasureBonus;
        // NOTE: telekineticForce and divineSenseStrength deliberately excluded
        // Dao manipulation is purely about understanding + technique + treasure
    }

    // ─── Builder ─────────────────────────────────────────────────────

    public static Builder forRealm(RealmId realm, long sSense, double currentQi) {
        return new Builder(realm, sSense, currentQi);
    }

    public static final class Builder {
        private final double telekineticForce;
        private final double divineSenseStrength;
        private double daoCompatibility = 0;
        private double techniqueMultiplier = 1;
        private double treasureBonus = 0;
        private double formationBonus = 0;

        public Builder(RealmId realm, long sSense, double currentQi) {
            // Telekinetic force = f(realm, currentQi).
            // S_realm gives the baseline; currentQi scales it (0 Qi = 0 force).
            long sRealm = dev.ergenverse.perception.DivineSense.S_REALM.getOrDefault(realm, 1L);
            this.telekineticForce = sRealm * Math.max(0, Math.min(1.5, currentQi));
            this.divineSenseStrength = sSense;
        }

        /**
         * Set the player's Dao compatibility with the target.
         *
         * @param playerDaoComprehension 0-1: how deeply the player has comprehended the relevant Dao
         * @param targetDaoAffinity      the target's Dao affinity (earth, water, fire, etc.)
         * @param playerDaoAffinities    the player's Dao affinities (dao id → comprehension 0-1)
         */
        public Builder daoCompatibility(double playerDaoComprehension, String targetDaoAffinity,
                                        Map<String, Double> playerDaoAffinities) {
            if (targetDaoAffinity == null || targetDaoAffinity.isEmpty()) {
                this.daoCompatibility = 0;
            } else {
                double playerAffinity = playerDaoAffinities.getOrDefault(targetDaoAffinity, 0.0);
                this.daoCompatibility = playerAffinity * playerDaoComprehension;
            }
            return this;
        }

        public Builder technique(String grade, double comprehension) {
            // grade multiplier: mortal=1, magical=2, spirit=5, immortal=10, dao=50
            java.util.Map<String, Double> gradeMult = java.util.Map.of(
                "mortal", 1.0, "magical", 2.0, "spirit", 5.0, "immortal", 10.0, "dao", 50.0
            );
            double mult = gradeMult.getOrDefault(grade, 1.0);
            this.techniqueMultiplier = mult * (0.5 + comprehension);
            return this;
        }

        public Builder treasure(double bonus) {
            this.treasureBonus = bonus;
            return this;
        }

        public Builder formation(double bonus) {
            this.formationBonus = bonus;
            return this;
        }

        public ManipulationCapability build() {
            return new ManipulationCapability(telekineticForce, divineSenseStrength,
                daoCompatibility, techniqueMultiplier, treasureBonus, formationBonus);
        }
    }
}
