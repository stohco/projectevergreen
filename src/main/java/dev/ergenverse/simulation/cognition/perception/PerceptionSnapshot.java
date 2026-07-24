package dev.ergenverse.simulation.cognition.perception;

import dev.ergenverse.simulation.event.WorldEvent;

import java.util.Collections;
import java.util.List;

/**
 * PerceptionSnapshot — what one actor perceives at one tick.
 *
 * <p><b>Constitution Article XXXV — Character-First pipeline:</b>
 * <pre>
 *   Perception -> Beliefs -> Relationships -> Identity ->
 *   Circumstances -> Opportunities -> Prediction -> Intent -> Plan -> Action
 * </pre>
 *
 * <p>Before this class existed, the {@link dev.ergenverse.simulation.cognition.DecisionEngine}
 * generated goals purely from internal NEEDS (hunger, qi, safety...). A wolf
 * could be 5 blocks from Wang Lin and his goal would still be MEDITATE,
 * because he never SAW the wolf. That produced the "T-pose sliding" the
 * user criticized: NPCs made decisions blind to their surroundings.
 *
 * <p>A PerceptionSnapshot is the actor's sensory picture of the world at
 * the moment of decision. It is built by {@link PerceptionSensor} from:
 * <ul>
 *   <li><b>Nearby entities</b> — classified by hostility / neutrality /
 *       opportunity / witness (so Wang Lin knows if he is being observed).</li>
 *   <li><b>Nearby world events</b> — recent WorldEventBus disturbances
 *       within perception radius (a beast panic 40 blocks away, a qi
 *       disturbance, a rumor).</li>
 *   <li><b>Environmental context</b> — time of day (affects threat model),
 *       biome / location (affects which goals are sensible), light level.</li>
 *   <li><b>Threat assessment</b> — the nearest hostile entity and its
 *       estimated power relative to the actor.</li>
 * </ul>
 *
 * <p>The snapshot is IMMUTABLE within a tick. The DecisionEngine reads it;
 * the Interpretation layer classifies it; the ActionPredictor uses it to
 * forecast outcomes. None of them mutate it.
 *
 * <h2>Perception radius</h2>
 * <p>Perception radius scales with cultivation realm (Art III / Art XIV LOD):
 * mortals perceive ~24 blocks; Foundation cultivators ~64; Nascent Soul+ ~128.
 * Divine sense (Art XV / canon: Wang Lin's divine-sense coverage) extends
 * further still. This is enforced in {@link PerceptionSensor}, not here.
 */
public final class PerceptionSnapshot {

    /** One perceived entity, pre-classified. */
    public static final class PerceivedEntity {
        public final String entityId;        // minecraft entity UUID string or actor id
        public final String classification;  // "hostile", "neutral", "prey", "witness", "ally", "unknown"
        public final String entityType;      // "spirit_wolf", "cultivator", "player", "villager", ...
        public final double distanceBlocks;
        public final double relativePower;   // -1..+1, negative = weaker than perceiver, positive = stronger
        public final String displayName;

        public PerceivedEntity(String entityId, String classification, String entityType,
                               double distanceBlocks, double relativePower, String displayName) {
            this.entityId = entityId;
            this.classification = classification;
            this.entityType = entityType;
            this.distanceBlocks = distanceBlocks;
            this.relativePower = relativePower;
            this.displayName = displayName;
        }
    }

    /** One perceived recent event. */
    public static final class PerceivedEvent {
        public final String topic;
        public final String description;
        public final double distanceBlocks;
        public final float intensity;
        public final long ageTicks;

        public PerceivedEvent(String topic, String description, double distanceBlocks,
                              float intensity, long ageTicks) {
            this.topic = topic;
            this.description = description;
            this.distanceBlocks = distanceBlocks;
            this.intensity = intensity;
            this.ageTicks = ageTicks;
        }
    }

    // ── Fields (all final, snapshot is immutable) ──
    public final String actorId;
    public final long tick;

    public final List<PerceivedEntity> nearbyEntities;
    public final List<PerceivedEvent> nearbyEvents;

    public final int perceptionRadiusBlocks;
    public final boolean isNight;
    public final boolean isUnderground;
    public final String biomeOrLocation;

    public final boolean hasThreat;
    public final boolean hasOpportunity;
    public final boolean isObserved;
    public final boolean isAlone;

    public final long buildTimeNanos;

    public PerceptionSnapshot(String actorId, long tick,
                              List<PerceivedEntity> nearbyEntities,
                              List<PerceivedEvent> nearbyEvents,
                              int perceptionRadiusBlocks,
                              boolean isNight, boolean isUnderground,
                              String biomeOrLocation,
                              boolean hasThreat, boolean hasOpportunity,
                              boolean isObserved, boolean isAlone,
                              long buildTimeNanos) {
        this.actorId = actorId;
        this.tick = tick;
        this.nearbyEntities = Collections.unmodifiableList(nearbyEntities);
        this.nearbyEvents = Collections.unmodifiableList(nearbyEvents);
        this.perceptionRadiusBlocks = perceptionRadiusBlocks;
        this.isNight = isNight;
        this.isUnderground = isUnderground;
        this.biomeOrLocation = biomeOrLocation;
        this.hasThreat = hasThreat;
        this.hasOpportunity = hasOpportunity;
        this.isObserved = isObserved;
        this.isAlone = isAlone;
        this.buildTimeNanos = buildTimeNanos;
    }

    public int nearbyEntityCount() { return nearbyEntities.size(); }
    public int nearbyEventCount() { return nearbyEvents.size(); }

    /** The single highest-salience perceived entity, or null if none. */
    public PerceivedEntity mostSalientEntity() {
        PerceivedEntity best = null;
        double bestSalience = Double.NEGATIVE_INFINITY;
        for (PerceivedEntity e : nearbyEntities) {
            double s = salience(e);
            if (s > bestSalience) {
                bestSalience = s;
                best = e;
            }
        }
        return best;
    }

    /**
     * Salience of a perceived entity — how much it deserves attention.
     * Hostile + close + strong = highest. Neutral + far = low.
     * This is the <b>attention</b> layer of Article XXXV.
     */
    public static double salience(PerceivedEntity e) {
        double proximity = 1.0 / (1.0 + e.distanceBlocks * 0.05);  // closer = more salient
        double classWeight = switch (e.classification) {
            case "hostile" -> 1.0;
            case "prey"    -> 0.6;
            case "witness" -> 0.5;
            case "ally"    -> 0.4;
            case "neutral" -> 0.2;
            default        -> 0.3;
        };
        double powerWeight = 1.0 + Math.max(0.0, e.relativePower);  // stronger threats more salient
        return proximity * classWeight * powerWeight;
    }

    @Override
    public String toString() {
        return "Perception[" + actorId + " t=" + tick
                + " ent=" + nearbyEntities.size()
                + " evt=" + nearbyEvents.size()
                + (hasThreat ? " THREAT" : "")
                + (hasOpportunity ? " OPP" : "")
                + (isObserved ? " OBSERVED" : "")
                + (isAlone ? " ALONE" : "")
                + "]";
    }
}
