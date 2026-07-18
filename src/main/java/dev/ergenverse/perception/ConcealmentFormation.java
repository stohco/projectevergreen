package dev.ergenverse.perception;

import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.world.WorldLayer;

/**
 * A Concealment Formation — a formation anchored in the {@link WorldLayer#SPIRITUAL}
 * layer that hides something from insufficient observers.
 *
 * <p>Per the {@link WorldPhilosophy}: "Hidden things should be hidden because
 * of the world's laws, not because of rendering tricks. A formation isn't
 * invisible because the game doesn't draw it. It's invisible because it
 * exists partially in another layer of reality, it is concealed by Dao,
 * it suppresses perception, it requires sufficient divine sense, it
 * intentionally rejects weaker observers."
 *
 * <h2>How this works mechanically</h2>
 *
 * <p>A Spirit Herb growing in a spirit valley might have a concealment
 * formation woven around it. The formation:
 *
 * <ul>
 *   <li>exists in the {@link WorldLayer#SPIRITUAL} layer — mortals cannot
 *       touch it because mortals only occupy the {@link WorldLayer#PHYSICAL}
 *       layer;</li>
 *   <li>has a <b>perception floor</b> — observers below this tier cannot
 *       resolve the herb at all;</li>
 *   <li>has a <b>physical disguise</b> — what insufficient observers see
 *       instead (e.g., "an ordinary weed" or "a patch of bare ground");</li>
 *   <li>has a <b>break tier</b> — observers at or above this tier can
 *       dismantle the formation and harvest the herb.</li>
 * </ul>
 *
 * <h2>The mortal cannot destroy it</h2>
 *
 * <p>If a mortal mines the physical rock around the formation, the
 * physical rock is destroyed — but the formation remains, because it
 * was never in the physical rock. It was in the spiritual layer. The
 * formation may shift according to its own laws (e.g., re-anchoring to
 * a nearby physical feature), but it does not break.
 *
 * <p>Only a cultivator at or above the formation's <b>break tier</b> can
 * dismantle it, because only their cultivation extends into the
 * spiritual layer.
 */
public final class ConcealmentFormation implements Objective {

    /** The objective nature of the formation itself. */
    public final ObjectiveNature nature;
    /** Minimum perception tier to see through the concealment. */
    public final PerceptionTier perceptionFloor;
    /** Minimum cultivation realm to break the formation. */
    public final RealmId breakTier;
    /** What insufficient observers see instead of the hidden thing. */
    public final String physicalDisguise;
    /** What the formation is hiding. */
    public final Objective hiddenThing;

    public ConcealmentFormation(ObjectiveNature nature, PerceptionTier perceptionFloor,
                                RealmId breakTier, String physicalDisguise,
                                Objective hiddenThing) {
        this.nature = nature;
        this.perceptionFloor = perceptionFloor;
        this.breakTier = breakTier;
        this.physicalDisguise = physicalDisguise;
        this.hiddenThing = hiddenThing;
    }

    @Override
    public ObjectiveNature nature() {
        return nature;
    }

    @Override
    public PerceptionResult perceive(ObserverContext observer) {
        PerceptionTier tier = observer.effectiveTier();

        // If the observer is below the perception floor, they perceive
        // only the disguise — the formation's laws actively reject them.
        if (tier.order < perceptionFloor.order) {
            return new PerceptionResult(
                physicalDisguise,
                "You see only " + physicalDisguise + ". Something feels off about this place, but you cannot resolve what.",
                null, false, true, dev.ergenverse.canon.CanonEngine.RealityLevel.UNKNOWN, tier
            );
        }

        // If the observer is at or above the perception floor but below
        // the break tier, they perceive the formation but cannot break it.
        if (tier.order < PerceptionTier.fromRealm(breakTier).order) {
            return new PerceptionResult(
                "Concealment Formation (" + nature.trueName + ")",
                "A concealment formation of Tier " + nature.trueRank + " hides something here. You can see through it, but you cannot break it — you would need to be at least " + breakTier.name + ".",
                nature.trueRank, false, false,
                dev.ergenverse.canon.CanonEngine.RealityLevel.REALITY, tier
            );
        }

        // Observer can both perceive and break the formation.
        // They also perceive the hidden thing.
        PerceptionResult hiddenResult = hiddenThing.perceive(observer);
        return new PerceptionResult(
            "Concealment Formation (" + nature.trueName + ") — broken reveals: " + hiddenResult.perceivedName,
            "A concealment formation of Tier " + nature.trueRank + ", breakable by a " + breakTier.name + " cultivator. Beyond it: " + hiddenResult.perceivedDescription,
            nature.trueRank, true, false,
            dev.ergenverse.canon.CanonEngine.RealityLevel.REALITY, tier
        );
    }

    /**
     * Can a mortal's physical attack break this formation?
     *
     * <p>Never. The formation is anchored in the {@link WorldLayer#SPIRITUAL}
     * layer. A mortal only occupies the {@link WorldLayer#PHYSICAL} layer.
     * The mortal's pickaxe hits physical blocks, not spiritual anchors.
     * The formation may shift according to its own laws, but it does not
     * break.
     */
    public boolean isVulnerableToPhysicalAttack() {
        return false;
    }

    /**
     * Can a cultivator at the given realm break this formation?
     */
    public boolean isBreakableBy(RealmId realm) {
        return realm.isAtLeast(breakTier);
    }
}
