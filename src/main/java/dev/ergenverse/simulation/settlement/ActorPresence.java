package dev.ergenverse.simulation.settlement;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ActorPresence — the simulation-owned "where is this actor right now" model.
 *
 * <p>This is the <b>core inversion</b> of Article XLIV. Per the user's
 * directive:
 * <blockquote>
 * NPC positions should not be fixed offsets. Instead: Home, Meditation Spot,
 * Favorite Tree, Restriction Cave, Marketplace, Spirit Spring — each with
 * weights. Morning 90% Home, Afternoon Meditation Rock, Night Home. If wolves
 * appear, everything changes.
 * </blockquote>
 *
 * <p>An actor does not "spawn" at a fixed coordinate. An actor <b>lives</b> —
 * they have a home, a meditation spot, a favorite tree. Their current position
 * is <b>derived</b> from their life, the time of day, their current activity,
 * and contextual modifiers (a wolf pack near the village shifts everyone to
 * "home/flee" weights).
 *
 * <p>The renderer ({@link ActorMaterializer}) asks: "which actors' current
 * presence intersects loaded chunks?" Those become entities. The actor never
 * "spawns" — they materialize because they were already there.
 *
 * <h2>Algorithm</h2>
 * <ol>
 *   <li>Gather the actor's presence locations: their home (from their
 *       {@link Residence}) plus the settlement's shared locations.</li>
 *   <li>Look up each location's weight for the current {@link TimeOfDay}.</li>
 *   <li>Apply contextual modifiers (threat → collapse weights onto home/flee).</li>
 *   <li>Deterministic weighted pick, seeded by (actorId, day, phase) so the
 *       choice is stable within a phase — no teleport-flicker between scans.</li>
 * </ol>
 *
 * <h2>Determinism</h2>
 * <p>The pick is seeded by (actorId, day, tod-block). This means: within a
 * single phase of a single day, the actor stays at the chosen location. When
 * the phase changes (e.g. MORNING → MIDDAY), the actor may move to a new
 * location. This produces believable daily rhythms without per-tick jitter.
 */
public final class ActorPresence {

    private ActorPresence() {}

    /**
     * Compute the actor's current presence position as a settlement-local
     * offset [offsetX, offsetZ].
     *
     * @param actorId    the actor's id
     * @param settlement the settlement the actor lives in
     * @param gameTime   the level's gameTime (ticks)
     * @param context    contextual modifiers (threats, events), or null for peaceful
     * @return a 2-element array [offsetX, offsetZ] relative to settlement center
     */
    public static int[] computePosition(String actorId, Settlement settlement,
                                        long gameTime, PresenceContext context) {
        TimeOfDay tod = TimeOfDay.fromGameTime(gameTime);

        // ── Threat override: if a threat is active, everyone collapses to home ──
        // Per the user's directive: "If wolves appear, everything changes."
        if (context != null && context.threatActive) {
            Residence home = settlement.residenceFor(actorId);
            if (home != null && !home.isDestroyed()) {
                return new int[]{home.centerX(), home.centerZ()};
            }
            // No home — flee to the settlement center (plaza / meeting point).
            return new int[]{0, 0};
        }

        // ── Gather this actor's presence locations ──
        List<PresenceLocation> locations = new ArrayList<>();
        locations.addAll(settlement.getSharedLocations());

        Residence home = settlement.residenceFor(actorId);
        if (home != null && !home.isDestroyed()) {
            locations.add(homeLocation(home));
        }

        if (locations.isEmpty()) {
            // No locations at all — fall back to settlement center.
            return new int[]{0, 0};
        }

        // ── Deterministic weighted pick ──
        // Seed by (actorId, day, phase) so the pick is stable within a phase.
        long day = gameTime / 24000L;
        int hash = Objects.hash(actorId, day, tod.ordinal());

        float total = 0f;
        for (PresenceLocation loc : locations) {
            total += loc.weightAt(tod);
        }
        if (total <= 0f) {
            // No location has weight at this phase — default to home if it
            // exists, else the first shared location, else center.
            if (home != null && !home.isDestroyed()) {
                return new int[]{home.centerX(), home.centerZ()};
            }
            PresenceLocation fallback = locations.get(0);
            return new int[]{fallback.offsetX, fallback.offsetZ};
        }

        float roll = ((hash & 0xFFFF) / 65535.0f) * total;
        float acc = 0f;
        for (PresenceLocation loc : locations) {
            acc += loc.weightAt(tod);
            if (roll <= acc) {
                return new int[]{loc.offsetX, loc.offsetZ};
            }
        }
        PresenceLocation last = locations.get(locations.size() - 1);
        return new int[]{last.offsetX, last.offsetZ};
    }

    /**
     * Build the "home" PresenceLocation from a residence, with canonical
     * daily-rhythm weights: home dominates at night/dawn/evening, lighter at
     * midday/afternoon (when the actor is out).
     */
    private static PresenceLocation homeLocation(Residence home) {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.9f);
        w.put(TimeOfDay.MORNING, 0.5f);
        w.put(TimeOfDay.MIDDAY, 0.25f);
        w.put(TimeOfDay.AFTERNOON, 0.35f);
        w.put(TimeOfDay.DUSK, 0.7f);
        w.put(TimeOfDay.EVENING, 0.9f);
        w.put(TimeOfDay.NIGHT, 0.95f);
        return new PresenceLocation("home:" + home.id, home.label,
                home.centerX(), home.centerZ(), w);
    }

    /**
     * Contextual modifiers for presence computation.
     *
     * <p>Per the user's directive: "If wolves appear, everything changes."
     * A threat context collapses all actors' presence onto their home (or the
     * settlement center if they have no home). Future contexts: festival
     * (everyone to plaza), mourning (everyone home), market_day (everyone to
     * market), sect_lecture (disciples to lecture hall).
     */
    public static final class PresenceContext {
        /** True when a threat (wolf pack, bandit raid) is active nearby. */
        public boolean threatActive;
        /** The threat type ("wolf_pack", "bandit_raid", ...). */
        public String threatType;
        /** When the threat was detected (gameTime). */
        public long threatTick;

        /** A peaceful (no-threat) context. */
        public static PresenceContext peaceful() { return new PresenceContext(); }

        /** A threat context. */
        public static PresenceContext threat(String type, long tick) {
            PresenceContext c = new PresenceContext();
            c.threatActive = true;
            c.threatType = type;
            c.threatTick = tick;
            return c;
        }
    }
}
