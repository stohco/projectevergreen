package dev.ergenverse.simulation.settlement;

import java.util.Map;

/**
 * PresenceLocation — a named place where an actor can be, with per-time-of-day
 * presence weights.
 *
 * <p>Per Article XLIV (the user's directive): "NPC positions should not be
 * fixed offsets. Instead: Home, Meditation Spot, Favorite Tree, Restriction
 * Cave, Marketplace, Spirit Spring — each with weights. Morning 90% Home,
 * Afternoon Meditation Rock, Night Home. If wolves appear, everything changes."
 *
 * <p>A PresenceLocation is relative to its settlement's center. The
 * {@link ActorPresence} engine combines an actor's home (derived from their
 * {@link Residence}) with the settlement's shared PresenceLocations, applies
 * the time-of-day weights, applies contextual modifiers (threats), and
 * produces the actor's current position.
 *
 * <p>The position is NOT a spawn coordinate. It is a derived fact about a
 * living actor. The renderer materializes the actor at this position because
 * the actor was already there — not because a chunk loaded.
 *
 * <h2>Weight semantics</h2>
 * <ul>
 *   <li>0.0 — the actor is never here at this time.</li>
 *   <li>0.3 — the actor is sometimes here.</li>
 *   <li>0.6 — the actor is often here.</li>
 *   <li>0.9 — the actor is almost always here at this time.</li>
 * </ul>
 * Weights are relative within an actor's location set: the engine normalizes
 * them and does a deterministic weighted pick (seeded by actorId + day + phase
 * so the choice is stable within a phase, preventing teleport-flicker).
 */
public final class PresenceLocation {

    /** Stable identifier (e.g. "home", "meditation_rock", "herb_garden"). */
    public final String id;

    /** Human-readable label (e.g. "Wang Family Home", "Central Plaza"). */
    public final String label;

    /** X offset from the settlement center (blocks). */
    public final int offsetX;

    /** Z offset from the settlement center (blocks). */
    public final int offsetZ;

    /** Per-TimeOfDay presence weight (0.0-1.0). Unspecified phases default to 0.0. */
    public final Map<TimeOfDay, Float> weights;

    public PresenceLocation(String id, String label, int offsetX, int offsetZ,
                            Map<TimeOfDay, Float> weights) {
        this.id = id;
        this.label = label;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        this.weights = weights;
    }

    /** The presence weight at the given phase (0.0 if unspecified). */
    public float weightAt(TimeOfDay tod) {
        return weights.getOrDefault(tod, 0.0f);
    }
}
