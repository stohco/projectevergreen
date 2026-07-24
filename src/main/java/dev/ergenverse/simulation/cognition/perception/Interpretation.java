package dev.ergenverse.simulation.cognition.perception;

import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DaoIdentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interpretation — the third layer of the Article XXXV pipeline.
 *
 * <pre>
 *   World -> Perception -> Attention -> Interpretation ->
 *   Prediction -> Goals -> Intent -> Planning -> Tasks -> Activities -> Minecraft
 * </pre>
 *
 * <p>Perception gives the actor raw data (entities + events + environment).
 * Attention ranks that data by salience. <b>Interpretation</b> answers:
 * "what does the attended stimulus MEAN to me, given who I am?"
 *
 * <p>The same perception produces different interpretations for different
 * actors. A wolf 10 blocks away:
 * <ul>
 *   <li>To a mortal villager: THREAT_TO_LIFE → flee</li>
 *   <li>To a Core Formation cultivator: MINOR_NUISANCE → ignore or dispatch</li>
 *   <li>To a hunting spirit beast: PREY_DETECTED → stalk</li>
 *   <li>To Wang Lin (Defiance Dao, hiding power): WITNESS_RISK → avoid
 *       revealing strength, observe from distance</li>
 * </ul>
 *
 * <p>This class is PURE — it takes a PerceptionSnapshot + the actor's Dao
 * identity + personality and returns an Interpretation. No mutation, no
 * side effects. The DecisionEngine uses the Interpretation's
 * {@link #suggestedGoalOverride} to inject perception-driven goals ABOVE
 * the need-driven goal queue when the situation demands it.
 *
 * <h2>Interpretation categories</h2>
 * <ul>
 *   <li>{@link Category#THREAT_TO_LIFE} — hostile entity stronger than self, close</li>
 *   <li>{@link Category#MINOR_NUISANCE} — hostile but weaker; can be ignored or dispatched</li>
 *   <li>{@link Category#PREY_DETECTED} — food opportunity for beasts / hungry actors</li>
 *   <li>{@link Category#WITNESS_RISK} — observed by someone; caution needed (Wang Lin)</li>
 *   <li>{@link Category#SOCIAL_OPPORTUNITY} — ally / witness present; interaction possible</li>
 *   <li>{@link Category#RESOURCE_OPPORTUNITY} — perceived event indicates a resource</li>
 *   <li>{@link Category#SAFE_TO_ACT} — alone, no threats; can meditate / cultivate freely</li>
 *   <li>{@link Category#UNEVENTFUL} — nothing notable; continue current activity</li>
 * </ul>
 */
public final class Interpretation {

    public enum Category {
        THREAT_TO_LIFE,         // flee / hide / defend
        MINOR_NUISANCE,         // dispatch or ignore
        PREY_DETECTED,          // hunt
        WITNESS_RISK,           // hide power, observe cautiously
        SOCIAL_OPPORTUNITY,     // approach / interact
        RESOURCE_OPPORTUNITY,   // investigate
        SAFE_TO_ACT,            // proceed with planned activity
        UNEVENTFUL              // nothing notable
    }

    public final Category category;
    public final String summary;
    public final double urgency;          // 0..1, how immediately this demands action
    public final PerceptionSnapshot.PerceivedEntity focusEntity;
    public final PerceptionSnapshot.PerceivedEvent focusEvent;

    /** A goal category that the situation suggests, or null if no override. */
    public final CognitionGoal.Category suggestedGoalOverride;

    public Interpretation(Category category, String summary, double urgency,
                          PerceptionSnapshot.PerceivedEntity focusEntity,
                          PerceptionSnapshot.PerceivedEvent focusEvent,
                          CognitionGoal.Category suggestedGoalOverride) {
        this.category = category;
        this.summary = summary;
        this.urgency = Math.max(0.0, Math.min(1.0, urgency));
        this.focusEntity = focusEntity;
        this.focusEvent = focusEvent;
        this.suggestedGoalOverride = suggestedGoalOverride;
    }

    /**
     * Interpret a perception snapshot for an actor.
     *
     * @param snap   the perception
     * @param dao    the actor's Dao identity (may be null for beasts)
     * @param isBeast true if the actor is a beast (changes prey/threat logic)
     * @return the interpretation (never null)
     */
    public static Interpretation interpret(PerceptionSnapshot snap, DaoIdentity dao, boolean isBeast) {
        // 1) Find the most salient entity (attention layer already ranked them).
        PerceptionSnapshot.PerceivedEntity focus = snap.mostSalientEntity();
        PerceptionSnapshot.PerceivedEvent focusEvt = snap.nearbyEvents.isEmpty()
                ? null : snap.nearbyEvents.get(0);

        // 2) Threat assessment — highest urgency.
        if (snap.hasThreat && focus != null && "hostile".equals(focus.classification)) {
            double relPower = focus.relativePower;
            double urgency = 0.5 + (0.5 * Math.max(0.0, relPower)) + (1.0 / (1.0 + focus.distanceBlocks * 0.1)) * 0.3;
            urgency = Math.min(1.0, urgency);

            if (relPower > 0.2) {
                // Stronger than us → flee / hide.
                return new Interpretation(Category.THREAT_TO_LIFE,
                        "Hostile " + focus.entityType + " (" + focus.displayName + ") stronger than self at "
                                + Math.round(focus.distanceBlocks) + " blocks",
                        urgency, focus, focusEvt, CognitionGoal.Category.FLEE);
            } else {
                // Weaker hostile → dispatch or ignore depending on dao.
                return new Interpretation(Category.MINOR_NUISANCE,
                        "Hostile " + focus.entityType + " at " + Math.round(focus.distanceBlocks)
                                + " blocks — manageable",
                        urgency * 0.6, focus, focusEvt, null);
            }
        }

        // 3) Prey detected — beasts / hungry actors.
        if (isBeast && focus != null && "prey".equals(focus.classification)) {
            return new Interpretation(Category.PREY_DETECTED,
                    "Prey " + focus.entityType + " at " + Math.round(focus.distanceBlocks) + " blocks",
                    0.6, focus, focusEvt, CognitionGoal.Category.GATHER_RESOURCE);
        }

        // 4) Witness risk — Wang Lin's signature caution (Defiance Dao).
        // A cultivator hiding their power who is being observed must not reveal it.
        if (snap.isObserved && dao != null) {
            boolean hidingPower = dao == DaoIdentity.DEFIANCE || dao == DaoIdentity.SEEKING_DAO;
            if (hidingPower && focus != null && "witness".equals(focus.classification)) {
                return new Interpretation(Category.WITNESS_RISK,
                        "Observed by " + focus.displayName + " — avoid revealing strength",
                        0.55, focus, focusEvt, CognitionGoal.Category.HIDE);
            }
        }

        // 5) Resource opportunity — perceived event hints at a resource.
        if (snap.hasOpportunity && focusEvt != null) {
            return new Interpretation(Category.RESOURCE_OPPORTUNITY,
                    "Sensed: " + focusEvt.description + " (" + Math.round(focusEvt.distanceBlocks) + " blocks away)",
                    0.5, focus, focusEvt, CognitionGoal.Category.INVESTIGATE);
        }

        // 6) Social opportunity — witness present, no threat.
        if (snap.isObserved && !snap.hasThreat && focus != null && "witness".equals(focus.classification)) {
            return new Interpretation(Category.SOCIAL_OPPORTUNITY,
                    "Possible interaction with " + focus.displayName,
                    0.3, focus, focusEvt, CognitionGoal.Category.SOCIAL);
        }

        // 7) Safe to act — alone, no threats, no witnesses.
        if (snap.isAlone && !snap.hasThreat) {
            return new Interpretation(Category.SAFE_TO_ACT,
                    "Alone and unthreatened — safe to meditate / cultivate",
                    0.1, null, null, null);
        }

        // 8) Default — uneventful.
        return new Interpretation(Category.UNEVENTFUL,
                "Nothing notable within " + snap.perceptionRadiusBlocks + " blocks",
                0.05, focus, focusEvt, null);
    }

    @Override
    public String toString() {
        return "Interpretation[" + category + " u=" + String.format("%.2f", urgency)
                + (focusEntity != null ? " focus=" + focusEntity.entityType : "")
                + (suggestedGoalOverride != null ? " -> " + suggestedGoalOverride : "")
                + "]";
    }
}
