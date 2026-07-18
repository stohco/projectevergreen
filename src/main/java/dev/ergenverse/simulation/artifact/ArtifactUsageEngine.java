package dev.ergenverse.simulation.artifact;

import java.util.ArrayList;
import java.util.List;

/**
 * The core engine: computes how a treasure expresses its power for a
 * specific wielder attempting a specific action.
 *
 * <p>This is the function the user specified:
 * <pre>
 *   ArtifactOutput calculateUsage(userRealm, artifactRealm,
 *       cultivation, compatibility, energyAvailable)
 * </pre>
 * Expanded into a full {@link UsageContext} and an {@link ArtifactUsageProfile}.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li><b>Determine expressed layer.</b> The highest layer whose
 *       requirements the wielder meets FOR THIS ACTION. Holding never
 *       exceeds PHYSICAL. Activating needs qi + sense. Dao needs
 *       compatibility + recognition.</li>
 *   <li><b>Compute damage.</b> Physical damage from Layer 1, scaled by
 *       realm gap (mortals get full base damage but slow speed). Spiritual
 *       damage only at ACTIVATION+.</li>
 *   <li><b>Determine activated abilities.</b> The subset of the treasure's
 *       abilities whose individual costs the wielder can meet.</li>
 *   <li><b>Evaluate backlash.</b> Via {@link BacklashPolicy#evaluate}.
 *       Contextual: action-dependent, not mere-possession-dependent.</li>
 *   <li><b>Generate narrative note.</b> A canon-flavored description of
 *       what the player experiences at this expression level.</li>
 * </ol>
 *
 * <h2>The Golden Rule (encoded)</h2>
 * <p>There is NO code path in this engine that damages the player merely
 * for HOLDING an over-leveled treasure. The HOLDING action always produces
 * backlash risk 0.0. Backlash requires forcing — COMMANDING_SPIRIT without
 * recognition, FORCING_RESTRICTION beyond capability, OVERDRAWING energy.
 * This is the mechanical encoding of the user's directive:
 * <blockquote>
 *   Never make cultivation treasures binary. A cultivator does not either
 *   "have access" or "not have access."
 * </blockquote>
 */
public final class ArtifactUsageEngine {

    private ArtifactUsageEngine() {}

    /**
     * Calculate how a treasure expresses its power for a given wielder
     * and action.
     *
     * @param ctx    the wielder's state and intended action
     * @param profile the treasure's usage profile
     * @return the full output — never null, never a pass/fail boolean
     */
    public static ArtifactOutput calculateUsage(UsageContext ctx, ArtifactUsageProfile profile) {
        // ── Step 1: Determine expressed layer ──────────────────────
        ArtifactUsageLayer layer = determineExpressedLayer(ctx, profile);

        // ── Step 2: Compute damage ─────────────────────────────────
        DamageResult damage = computeDamage(ctx, profile, layer);

        // ── Step 3: Determine activated abilities ──────────────────
        List<String> abilities = activatedAbilities(ctx, profile, layer);

        // ── Step 4: Evaluate backlash ──────────────────────────────
        BacklashPolicy.BacklashResult backlash = BacklashPolicy.evaluate(ctx, profile, layer);

        // ── Step 5: Generate narrative ─────────────────────────────
        String narrative = generateNarrative(ctx, profile, layer, backlash);

        return new ArtifactOutput(layer, damage.physical, damage.spiritual,
                abilities, backlash.risk(), backlash.type(), narrative);
    }

    // ── Layer determination ────────────────────────────────────────

    private static ArtifactUsageLayer determineExpressedLayer(UsageContext ctx,
                                                               ArtifactUsageProfile profile) {
        UsageAction action = ctx.action();

        // HOLDING and PASSIVE_BENEFIT never exceed PASSIVE
        if (action == UsageAction.HOLDING) return ArtifactUsageLayer.PHYSICAL;
        if (action == UsageAction.PASSIVE_BENEFIT) return ArtifactUsageLayer.PASSIVE;

        // SWINGING can reach PASSIVE but not ACTIVATION (you're just hitting things)
        if (action == UsageAction.SWINGING) {
            if (hasAnyPassiveEffect(ctx, profile)) return ArtifactUsageLayer.PASSIVE;
            return ArtifactUsageLayer.PHYSICAL;
        }

        // All remaining actions require energy. Check activation capability.
        boolean canActivate = canActivate(ctx, profile);

        // Blood refinement check (per-action, only for BLOOD_REFINING and ACTIVATING)
        if (action == UsageAction.BLOOD_REFINING) {
            if (ctx.bloodRefinementDepth() >= 1.0) {
                return ArtifactUsageLayer.AUTHORITY; // already refined
            }
            return canActivate ? ArtifactUsageLayer.ACTIVATION : ArtifactUsageLayer.PASSIVE;
        }

        // COMMANDING_SPIRIT requires spirit recognition
        if (action == UsageAction.COMMANDING_SPIRIT) {
            if (!profile.hasSpirit()) return ArtifactUsageLayer.PHYSICAL;
            TreasureSpirit.Recognition rec = ctx.spiritRecognition();
            if (rec == null) rec = TreasureSpirit.Recognition.NONE;
            if (rec == TreasureSpirit.Recognition.UNIFIED) return ArtifactUsageLayer.DAO_MANIFESTATION;
            if (rec == TreasureSpirit.Recognition.RECOGNIZED) return ArtifactUsageLayer.AUTHORITY;
            if (rec == TreasureSpirit.Recognition.ACKNOWLEDGED) return ArtifactUsageLayer.ACTIVATION;
            return canActivate ? ArtifactUsageLayer.ACTIVATION : ArtifactUsageLayer.PHYSICAL;
        }

        // FORCING_RESTRICTION — always risky, layer depends on capability
        if (action == UsageAction.FORCING_RESTRICTION) {
            if (!canActivate) return ArtifactUsageLayer.PHYSICAL;
            int gap = ctx.realmGap();
            if (gap <= 0) return ArtifactUsageLayer.AUTHORITY;
            if (gap <= 3) return ArtifactUsageLayer.ACTIVATION;
            return ArtifactUsageLayer.PASSIVE; // too far below, barely scratching
        }

        // OVERDRAWING — can reach ACTIVATION but with high backlash
        if (action == UsageAction.OVERDRAWING) {
            return canActivate ? ArtifactUsageLayer.ACTIVATION : ArtifactUsageLayer.PHYSICAL;
        }

        // Generic ACTIVATING, FLYING, ENTERING_INTERIOR
        if (!canActivate) return ArtifactUsageLayer.PASSIVE;

        // Check for Dao Manifestation
        if (canDaoManifest(ctx, profile)) return ArtifactUsageLayer.DAO_MANIFESTATION;

        // Check for Full Authority
        if (ctx.userRealm().isAtLeast(profile.authorityRealm())) {
            return ArtifactUsageLayer.AUTHORITY;
        }

        return ArtifactUsageLayer.ACTIVATION;
    }

    private static boolean hasAnyPassiveEffect(UsageContext ctx, ArtifactUsageProfile profile) {
        if (profile.passiveEffects() == null || profile.passiveEffects().isEmpty()) return false;
        // Passive effects are always present; the question is whether the
        // wielder is at a high enough realm to FEEL them. But even a mortal
        // holding a sharp sword benefits from the sharpness.
        return true;
    }

    private static boolean canActivate(UsageContext ctx, ArtifactUsageProfile profile) {
        var act = profile.activation();
        if (act == null || act.abilities().isEmpty()) return false;

        // Check minimum compatibility
        if (ctx.compatibility() < act.minCompatibility()) return false;

        // Check blood refinement requirement
        if (act.requiresBloodRefinement() && ctx.bloodRefinementDepth() <= 0) return false;

        // Check qi — must have SOME qi, but not necessarily full cost
        // (partial activation is allowed)
        if (ctx.qiAvailable() <= 0 && ctx.action().requiresEnergy) return false;

        // Check sense — must have some divine sense for any activation
        if (ctx.spiritualSenseStrength() <= 0 && ctx.action().requiresEnergy) return false;

        return true;
    }

    private static boolean canDaoManifest(UsageContext ctx, ArtifactUsageProfile profile) {
        if (profile.daoManifestation() == null) return false;
        // Dao manifestation requires: authority realm + high compatibility +
        // either unified spirit or deep blood refinement
        if (!ctx.userRealm().isAtLeast(profile.authorityRealm())) return false;
        if (ctx.compatibility() < 0.8) return false;
        if (profile.hasSpirit()) {
            TreasureSpirit.Recognition rec = ctx.spiritRecognition();
            if (rec != null && rec == TreasureSpirit.Recognition.UNIFIED) return true;
        }
        if (ctx.bloodRefinementDepth() >= 0.9) return true;
        return false;
    }

    // ── Damage computation ─────────────────────────────────────────

    private record DamageResult(double physical, double spiritual) {}

    private static DamageResult computeDamage(UsageContext ctx, ArtifactUsageProfile profile,
                                               ArtifactUsageLayer layer) {
        double physical = profile.baseDamage();
        double spiritual = 0.0;

        // Realm gap scaling for physical damage
        // A mortal swinging a Third Step sword gets full base damage but
        // the attack is slow (handled by attackSpeed in the profile,
        // not here — we only compute the damage numbers).
        // If the wielder is BELOW the artifact realm, physical damage
        // is still full (the sword is still made of that material).
        // The EXPRESSION of the treasure's deeper abilities is what scales.

        // Passive layer: slight damage bonus from passive effects
        if (layer.tier >= ArtifactUsageLayer.PASSIVE.tier && profile.passiveEffects() != null) {
            for (var effect : profile.passiveEffects()) {
                physical *= (1.0 + effect.potency() * 0.2); // passive adds up to 20%
            }
        }

        // Activation layer: spiritual damage appears
        if (layer.tier >= ArtifactUsageLayer.ACTIVATION.tier) {
            // Spiritual damage scales with how much qi the wielder can supply
            double qiFraction = Math.min(1.0, ctx.qiAvailable());
            double senseFraction = Math.min(1.0, ctx.spiritualSenseStrength());
            spiritual = profile.baseDamage() * qiFraction * senseFraction * 0.5;

            // Blood refinement boosts both
            if (ctx.bloodRefinementDepth() > 0) {
                double bloodBonus = 1.0 + ctx.bloodRefinementDepth() * 0.5;
                physical *= bloodBonus;
                spiritual *= bloodBonus;
            }

            // Compatibility modulates spiritual damage
            spiritual *= (0.5 + ctx.compatibility() * 0.5);
        }

        // Authority layer: full documented damage
        if (layer.tier >= ArtifactUsageLayer.AUTHORITY.tier) {
            physical = profile.baseDamage() * 1.5;
            spiritual = profile.baseDamage() * 1.0;
        }

        // Dao Manifestation: terrifying
        if (layer.tier >= ArtifactUsageLayer.DAO_MANIFESTATION.tier) {
            physical = profile.baseDamage() * 2.0;
            spiritual = profile.baseDamage() * 2.0;
        }

        return new DamageResult(physical, spiritual);
    }

    // ── Activated abilities ────────────────────────────────────────

    private static List<String> activatedAbilities(UsageContext ctx,
                                                    ArtifactUsageProfile profile,
                                                    ArtifactUsageLayer layer) {
        if (layer.tier < ArtifactUsageLayer.ACTIVATION.tier) return List.of();
        if (profile.activation() == null) return List.of();

        List<String> result = new ArrayList<>();
        for (var ability : profile.activation().abilities()) {
            if (canUseAbility(ctx, ability)) {
                result.add(ability.name());
            }
        }
        return result;
    }

    private static boolean canUseAbility(UsageContext ctx,
                                          ArtifactUsageProfile.ActivatableAbility ability) {
        // Qi check: wielder must meet the ability's qi cost
        if (ctx.qiAvailable() < ability.qiCost() * 0.5) return false; // 50% threshold for partial

        // Sense check: wielder must meet the ability's sense cost
        if (ctx.spiritualSenseStrength() < ability.senseCost() * 0.5) return false;

        // Realm is soft — being below the soft realm means the ability
        // is degraded (lower damage, slower), not unavailable.
        // We allow it but the damage computation above already scaled down.

        return true;
    }

    // ── Narrative generation ───────────────────────────────────────

    private static String generateNarrative(UsageContext ctx, ArtifactUsageProfile profile,
                                             ArtifactUsageLayer layer, BacklashPolicy.BacklashResult backlash) {
        String itemName = profile.nameCn().isEmpty() ? profile.name() : profile.nameCn();
        int gap = ctx.realmGap();

        if (layer == ArtifactUsageLayer.SEALED) {
            return "The " + itemName + " is cold and lifeless in your hands. It does not respond.";
        }

        if (backlash.risk() > 0.6 && backlash.type() != BacklashType.NONE) {
            return "The " + itemName + " " + backlash.type().narrativeNote;
        }

        return switch (layer) {
            case PHYSICAL -> {
                if (gap > 5) yield "The " + itemName + " is impossibly heavy. You can barely lift it.";
                else if (gap > 2) yield "The " + itemName + " is heavy in your hand. There is more to it than you can sense.";
                else yield "You wield the " + itemName + ".";
            }
            case PASSIVE -> {
                if (profile.passiveEffects() != null && !profile.passiveEffects().isEmpty()) {
                    var first = profile.passiveEffects().get(0);
                    yield "The " + itemName + " hums faintly. You feel " + first.name() + ".";
                }
                yield "The " + itemName + " seems to respond to your presence.";
            }
            case ACTIVATION -> {
                int abilities = activatedAbilities(ctx, profile, layer).size();
                if (abilities == 0) yield "You channel qi into the " + itemName + " but it dissipates.";
                else if (abilities == 1) yield "The " + itemName + " responds! One ability stirs within.";
                else yield "The " + itemName + " awakens. " + abilities + " abilities respond to your qi.";
            }
            case AUTHORITY -> "The " + itemName + " sings in your grasp. You command its full power.";
            case DAO_MANIFESTATION -> {
                if (profile.daoManifestation() != null) {
                    yield "You and the " + itemName + " are one. " + profile.daoManifestation().narrativeNote();
                }
                yield "The " + itemName + " and your Dao resonate as one.";
            }
            default -> "The " + itemName + " rests in your hand.";
        };
    }
}