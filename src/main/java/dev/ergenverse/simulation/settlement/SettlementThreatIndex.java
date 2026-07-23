package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventSubscriber;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SettlementThreatIndex — tracks active threats near each settlement and feeds
 * them into the {@link ActorPresence} presence model.
 *
 * <p>This is the wiring that makes Article XLIV §5's "if wolves appear,
 * everything changes" actually work. Without it, {@link ActorMaterializer}
 * hardcoded {@link ActorPresence.PresenceContext#peaceful()} — every actor
 * stayed on their normal daily rhythm regardless of danger.
 *
 * <h2>How it works</h2>
 * <p>This is a {@link WorldEventSubscriber} on the {@code "beast."} topic
 * prefix. When a beast event fires (wolf pack stalking, predator movement,
 * apex presence), this index:
 * <ol>
 *   <li>Finds the settlement nearest the event position (within
 *       {@link #THREAT_PROXIMITY_RANGE} blocks).</li>
 *   <li>Records a threat against that settlement with an expiry tick
 *       ({@link #THREAT_DURATION_TICKS} after the event).</li>
 * </ol>
 *
 * <p>{@link ActorMaterializer} then calls {@link #getThreatContext} each scan
 * to get the current {@link ActorPresence.PresenceContext} for a settlement.
 * If any non-expired threat exists, a threat context is returned — and
 * {@link ActorPresence#computePosition} collapses all actors onto their home
 * (or the settlement center if they have no home). The villagers flee indoors.
 *
 * <h2>Threat classification</h2>
 * <ul>
 *   <li>{@code beast.wolf_pack.*} → "wolf_pack" threat.</li>
 *   <li>{@code beast.predator.*} → "predator" threat.</li>
 *   <li>{@code beast.apex.*} → "apex" threat (longest duration).</li>
 *   <li>{@code beast.spirit_herd.*} → NOT a threat (herbivores grazing).</li>
 * </ul>
 *
 * <h2>Per Article XLIV §5</h2>
 * <p>"Morning: 90% Home. Afternoon: Meditation Rock. Night: Home. If wolves
 * appear, everything changes — the context collapses all actors onto
 * home/flee positions." This class IS that context.
 *
 * <h2>Per Article V</h2>
 * <p>Threats are recorded regardless of whether a player is watching. The
 * simulation owns the threat state; the renderer reads it.
 */
public final class SettlementThreatIndex implements WorldEventSubscriber {

    /** A beast event within this many blocks of a settlement center counts as a threat to it. */
    public static final int THREAT_PROXIMITY_RANGE = 256;

    /** How long a recorded threat stays active (ticks). 2400 = 2 MC minutes. */
    public static final long THREAT_DURATION_TICKS = 2400L;

    /** Apex threats last longer — the villagers stay spooked. */
    public static final long APEX_THREAT_DURATION_TICKS = 6000L;

    /** Public no-arg constructor for subscriber registration. */
    public SettlementThreatIndex() {}

    /** An active threat against a settlement. */
    private record Threat(String settlementId, String threatType, long expiryTick, float intensity) {}

    /** settlementId → list of active threats. */
    private static final Map<String, List<Threat>> THREATS = new ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        return "beast.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event.pos() == null) return;

        // Find the settlement nearest the event.
        Settlement nearest = SettlementRegistry.settlementNear(
                event.pos().getX(), event.pos().getZ(), THREAT_PROXIMITY_RANGE);
        if (nearest == null) return; // beast event in the wilderness — no settlement threatened

        String threatType = classifyThreat(event.topic());
        if (threatType == null) return; // not a threat (e.g. spirit_herd grazing)

        long duration = "apex".equals(threatType) ? APEX_THREAT_DURATION_TICKS : THREAT_DURATION_TICKS;
        long expiry = event.timestamp() + duration;

        Threat t = new Threat(nearest.id, threatType, expiry, event.intensity());
        THREATS.computeIfAbsent(nearest.id, k -> new ArrayList<>()).add(t);

        Ergenverse.LOGGER.debug("[SettlementThreatIndex] Threat {} recorded for {} (expiry tick {}, intensity {})",
                threatType, nearest.id, expiry, event.intensity());
    }

    /**
     * Get the current presence context for a settlement. Returns a threat
     * context if any non-expired threat is active, else a peaceful context.
     * Expired threats are cleaned up opportunistically.
     *
     * @param settlementId the settlement to query
     * @param currentTick  the current game tick
     * @return a PresenceContext (threat or peaceful)
     */
    public static ActorPresence.PresenceContext getThreatContext(String settlementId, long currentTick) {
        List<Threat> list = THREATS.get(settlementId);
        if (list == null || list.isEmpty()) {
            return ActorPresence.PresenceContext.peaceful();
        }

        // Opportunistic cleanup of expired threats.
        Iterator<Threat> it = list.iterator();
        String strongestType = null;
        long strongestTick = 0;
        float strongestIntensity = 0f;
        boolean anyActive = false;
        while (it.hasNext()) {
            Threat t = it.next();
            if (t.expiryTick <= currentTick) {
                it.remove();
                continue;
            }
            anyActive = true;
            // Pick the strongest active threat (highest intensity).
            if (t.intensity >= strongestIntensity) {
                strongestIntensity = t.intensity;
                strongestType = t.threatType;
                strongestTick = t.expiryTick;
            }
        }

        if (!anyActive) {
            THREATS.remove(settlementId); // all expired — drop the key
            return ActorPresence.PresenceContext.peaceful();
        }

        return ActorPresence.PresenceContext.threat(strongestType, strongestTick);
    }

    /**
     * Classify a beast event topic into a threat type.
     * @return "wolf_pack", "predator", "apex", or null (not a threat).
     */
    private static String classifyThreat(String topic) {
        if (topic == null) return null;
        if (topic.startsWith("beast.wolf_pack.")) return "wolf_pack";
        if (topic.startsWith("beast.predator.")) return "predator";
        if (topic.startsWith("beast.apex.")) return "apex";
        // beast.spirit_herd.* — herbivores, not a threat.
        return null;
    }

    /** Clear all threats. Called on world unload. */
    public static void clear() {
        THREATS.clear();
    }
}
