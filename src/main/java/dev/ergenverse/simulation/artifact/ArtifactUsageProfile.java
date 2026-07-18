package dev.ergenverse.simulation.artifact;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.cultivation.RealmId;

import java.util.List;

/**
 * Defines how a specific treasure expresses its power across the five usage layers.
 *
 * <p>Each treasure has ONE profile. The profile is grounded in canon where
 * Er Gen described the treasure's behavior, and marked as design where canon
 * is silent. Every threshold carries a {@link Provenance} citation where
 * canon-attested.
 *
 * <h2>How the four layers map to this profile</h2>
 * <ul>
 *   <li><b>Physical</b> — defined by {@code baseDamage}, {@code attackSpeed},
 *       {@code weight}. Always available.</li>
 *   <li><b>Passive</b> — defined by {@code passiveEffects}. Available without
 *       activation; some scale with {@code passiveRealmThreshold}.</li>
 *   <li><b>Activation</b> — defined by {@code activation}. Requires qi, sense,
 *       possibly blood refinement. Each ability has its own cost.</li>
 *   <li><b>Authority</b> — defined by {@code authorityRealm}. When the wielder
 *       reaches this realm, all documented abilities are fully available.</li>
 *   <li><b>Dao Manifestation</b> — defined by {@code daoManifestation}.
 *       Requires Dao comprehension, not just realm. The ultimate expression.</li>
 * </ul>
 *
 * <h2>Design principle</h2>
 * <p>{@code minRealm} fields are SOFT thresholds, not gates. They represent
 * the realm at which a canon character (usually Wang Lin) was observed
 * using the treasure at that layer. Below that realm, expression *scales
 * down* — it doesn't zero out. This is what makes the system non-binary.
 *
 * @see ArtifactUsageEngine
 */
public record ArtifactUsageProfile(
        /** The canon ID this profile belongs to (e.g. "wanglin/god_slaying_sword"). */
        String canonId,

        /** The treasure's English name. */
        String name,

        /** The treasure's Chinese name. */
        String nameCn,

        /** The item category (sword, whip, bead, flag, etc.). */
        String category,

        /**
         * The realm the treasure was forged/designed for.
         * This is the "intended wielder" realm (Layer 4 — AUTHORITY).
         */
        RealmId artifactRealm,

        /** Source evidence for this profile. */
        Provenance provenance,

        // ── Layer 1: Physical ──────────────────────────────────────

        /** Base physical damage. What a mortal swinging it would deal. */
        double baseDamage,

        /**
         * Attack speed modifier. 1.0 = normal. >1.0 = fast. <1.0 = slow.
         * A mortal swinging a Third Step sword might get huge damage but 0.3 speed.
         */
        double attackSpeed,

        // ── Layer 2: Passive ───────────────────────────────────────

        /** Passive effects always active while held. */
        List<PassiveEffect> passiveEffects,

        // ── Layer 3: Activation ────────────────────────────────────

        /** Requirements and abilities for the Activation layer. */
        ActivationSpec activation,

        // ── Layer 4: Authority ─────────────────────────────────────

        /**
         * Soft threshold: the realm at which 100% of the treasure's
         * documented abilities are available. Below this, abilities
         * scale down proportionally.
         */
        RealmId authorityRealm,

        // ── Layer 5: Dao Manifestation ─────────────────────────────

        /**
         * What Dao-level manifestation means for THIS treasure.
         * Null if the treasure doesn't have a Dao-level expression.
         */
        DaoManifestation daoManifestation,

        // ── Spirit ─────────────────────────────────────────────────

        /** The treasure's spirit, or null if spiritless. */
        TreasureSpirit spirit,

        // ── Backlash profile ───────────────────────────────────────

        /** Per-treasure backlash modifiers. */
        BacklashProfile backlashProfile
) {
    /** Whether this treasure has a sentient spirit. */
    public boolean hasSpirit() { return spirit != null; }

    // ── Sub-records ───────────────────────────────────────────────

    /**
     * A passive effect that exists without activation.
     * Examples: self-repair, material sharpness, spiritual pressure.
     */
    public record PassiveEffect(
            String name,
            String description,
            double potency,       // 0..1 — how strong the effect is
            RealmId softThreshold // the realm at which this effect is fully felt
    ) {}

    /**
     * What the treasure can do when spiritually activated.
     * Each ability has a qi cost and a minimum sense requirement.
     */
    public record ActivationSpec(
            /** Qi cost to activate (normalized to artifact's full demand). */
            double qiCost,
            /** Divine sense requirement (normalized). */
            double senseCost,
            /** Whether blood refinement is needed before ANY activation. */
            boolean requiresBloodRefinement,
            /** Minimum compatibility to activate. 0.5 = moderate Dao fit needed. */
            double minCompatibility,
            /** The specific abilities this treasure can express. */
            List<ActivatableAbility> abilities
    ) {
        public static ActivationSpec none() {
            return new ActivationSpec(0, 0, false, 0, List.of());
        }
    }

    /**
     * A single activatable ability of the treasure.
     */
    public record ActivatableAbility(
            String name,
            String nameCn,
            String description,
            double qiCost,        // normalized 0..1 relative to full activation
            double senseCost,     // normalized 0..1
            RealmId softRealm,    // the realm where this ability was canon-observed
            String narrativeNote  // what the player sees when this activates
    ) {}

    /**
     * What Dao-level manifestation means for a specific treasure.
     * This is the ultimate expression — user and treasure share the same Dao.
     */
    public record DaoManifestation(
            String description,
            String narrativeNote,
            Provenance provenance // where in canon this Dao-level use is attested
    ) {}

    /**
     * Per-treasure backlash modifiers. Overrides the default BacklashPolicy
     * for this specific treasure.
     */
    public record BacklashProfile(
            /** Multiplier on all backlash risks. 1.0 = normal. >1.0 = more dangerous. */
            double riskMultiplier,
            /** Additional backlash types unique to this treasure. */
            List<BacklashType> additionalTypes,
            /** Narrative notes for backlash, keyed by action. */
            String backlashNarrative
    ) {
        public static BacklashProfile normal() {
            return new BacklashProfile(1.0, List.of(), "");
        }
    }

    // ── Builder ───────────────────────────────────────────────────

    public static Builder builder(String canonId, String name, String nameCn) {
        return new Builder(canonId, name, nameCn);
    }

    public static class Builder {
        private final String canonId;
        private final String name;
        private final String nameCn;
        private String category = "Miscellaneous";
        private RealmId artifactRealm = RealmId.MORTAL;
        private Provenance provenance = Provenance.inferred("System", List.of(), 3, "No specific citation");
        private double baseDamage = 1.0;
        private double attackSpeed = 1.0;
        private List<PassiveEffect> passiveEffects = List.of();
        private ActivationSpec activation = ActivationSpec.none();
        private RealmId authorityRealm = RealmId.MORTAL;
        private DaoManifestation daoManifestation = null;
        private TreasureSpirit spirit = null;
        private BacklashProfile backlashProfile = BacklashProfile.normal();

        Builder(String canonId, String name, String nameCn) {
            this.canonId = canonId;
            this.name = name;
            this.nameCn = nameCn;
        }

        public Builder category(String c) { this.category = c; return this; }
        public Builder artifactRealm(RealmId r) { this.artifactRealm = r; return this; }
        public Builder provenance(Provenance p) { this.provenance = p; return this; }
        public Builder baseDamage(double d) { this.baseDamage = d; return this; }
        public Builder attackSpeed(double s) { this.attackSpeed = s; return this; }
        public Builder passiveEffects(List<PassiveEffect> e) { this.passiveEffects = e; return this; }
        public Builder activation(ActivationSpec a) { this.activation = a; return this; }
        public Builder authorityRealm(RealmId r) { this.authorityRealm = r; return this; }
        public Builder daoManifestation(DaoManifestation d) { this.daoManifestation = d; return this; }
        public Builder spirit(TreasureSpirit s) { this.spirit = s; return this; }
        public Builder backlashProfile(BacklashProfile b) { this.backlashProfile = b; return this; }

        public ArtifactUsageProfile build() {
            return new ArtifactUsageProfile(canonId, name, nameCn, category,
                    artifactRealm, provenance, baseDamage, attackSpeed,
                    passiveEffects, activation, authorityRealm,
                    daoManifestation, spirit, backlashProfile);
        }
    }
}