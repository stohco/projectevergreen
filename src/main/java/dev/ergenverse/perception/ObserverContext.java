package dev.ergenverse.perception;

import dev.ergenverse.cultivation.RealmId;

import java.util.Map;

/**
 * The observer's context — what they bring to the act of perceiving.
 *
 * <p>Per the {@link dev.ergenverse.core.WorldPhilosophy}: perception
 * is about <b>understanding</b>, not existence. The observer's
 * understanding is a function of:
 *
 * <ul>
 *   <li>Their cultivation realm (how strong their divine sense is)</li>
 *   <li>Their Dao affinities (a beast-tamer sees bloodline purity
 *       that a regular cultivator of the same realm misses)</li>
 *   <li>Their prior knowledge (have they heard of this species before?)</li>
 *   <li>Active divine sense sweep (extending perception beyond the body)</li>
 *   <li>World laws at this location (is perception suppressed here?)</li>
 * </ul>
 *
 * <p>This is passed to {@link Objective#perceive(ObserverContext)} to
 * produce a {@link PerceptionResult} — what the observer understands
 * about the objective thing.
 */
public final class ObserverContext {

    public final RealmId realm;
    /** Dao the observer has comprehended, mapped to comprehension 0.0-1.0. */
    public final Map<String, Double> daoComprehension;
    /** Is the observer actively sweeping divine sense? (Boosts perception) */
    public final boolean divineSenseActive;
    /** Known entity ids/species the observer has encountered before. */
    public final java.util.Set<String> knownSpecies;
    /** World-law perception multiplier at the observer's location (0.0-1.0). */
    public final double locationPerceptionMultiplier;
    /** Beast-tamer specialization — recognizes bloodlines others miss. */
    public final boolean isBeastTamer;
    /** Alchemist specialization — recognizes herbs others miss. */
    public final boolean isAlchemist;
    /** Formation-master specialization — recognizes formations others miss. */
    public final boolean isFormationMaster;

    public ObserverContext(RealmId realm, Map<String, Double> daoComprehension,
                           boolean divineSenseActive, java.util.Set<String> knownSpecies,
                           double locationPerceptionMultiplier,
                           boolean isBeastTamer, boolean isAlchemist, boolean isFormationMaster) {
        this.realm = realm;
        this.daoComprehension = daoComprehension;
        this.divineSenseActive = divineSenseActive;
        this.knownSpecies = knownSpecies;
        this.locationPerceptionMultiplier = locationPerceptionMultiplier;
        this.isBeastTamer = isBeastTamer;
        this.isAlchemist = isAlchemist;
        this.isFormationMaster = isFormationMaster;
    }

    /** A bare mortal observer — no Dao, no divine sense, no specialization. */
    public static ObserverContext mortal() {
        return new ObserverContext(RealmId.MORTAL, Map.of(), false, java.util.Set.of(),
            1.0, false, false, false);
    }

    /** Convenience: an observer at the given realm with no specializations. */
    public static ObserverContext at(RealmId realm) {
        return new ObserverContext(realm, Map.of(), false, java.util.Set.of(),
            1.0, false, false, false);
    }

    /** Effective perception tier — what the observer can resolve. */
    public dev.ergenverse.perception.PerceptionTier effectiveTier() {
        return PerceptionTier.fromRealm(realm);
    }
}
