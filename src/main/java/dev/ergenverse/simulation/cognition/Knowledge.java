package dev.ergenverse.simulation.cognition;

/**
 * Knowledge — verified facts known to a cognition actor.
 *
 * <p>A Knowledge entry is VERIFIED — it has been confirmed by one of the
 * five canonical verification sources. Unverified information is held in
 * the {@link BeliefRegistry} as a {@link Belief} (which can decay).
 *
 * <p>Canon audit:
 * <ul>
 *   <li>{@code OBSERVATION} — directly witnessed with own senses.</li>
 *   <li>{@code RESEARCH} — studied from jade slips, scriptures, sect archives.</li>
 *   <li>{@code SOUL_SEARCH} — forcibly extracted via soul-search technique.</li>
 *   <li>{@code INHERITED} — passed down from master to disciple, or bloodline memory.</li>
 *   <li>{@code CANON_SEEDED} — pre-loaded as world-canon (the world existed before
 *       the player arrived; NPCs know things).</li>
 * </ul>
 */
public final class Knowledge {

    public enum Source {
        OBSERVATION("Direct observation"),
        RESEARCH("Research / scripture study"),
        SOUL_SEARCH("Extracted via soul-search"),
        INHERITED("Inherited from master / bloodline"),
        CANON_SEEDED("World-canon seeded knowledge");

        public final String label;
        Source(String label) { this.label = label; }
    }

    public final String subject;     // e.g. "wang_tiangui", "mosquito_valley", "qi_gathering_grass"
    public final String predicate;   // e.g. "location", "weakness", "value"
    public final String value;       // e.g. "mistveil_village", "fire", "100_spirit_stones"
    public final Source source;
    public final long   verifiedTick;
    public final double confidence;  // 0..1 (always >= 0.7 for knowledge; below = belief)

    public Knowledge(String subject, String predicate, String value,
                     Source source, long verifiedTick, double confidence) {
        this.subject = subject;
        this.predicate = predicate;
        this.value = value;
        this.source = source;
        this.verifiedTick = verifiedTick;
        this.confidence = clamp01(confidence);
    }

    public String key() {
        return subject + "." + predicate;
    }

    @Override
    public String toString() {
        return "Know[" + key() + " = " + value + " (" + source.label + ", conf="
                + Math.round(confidence * 100) + "%)]";
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
