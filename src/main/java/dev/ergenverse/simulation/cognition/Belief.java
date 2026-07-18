package dev.ergenverse.simulation.cognition;

/**
 * Belief — an unverified (or partially verified) claim held by an actor.
 *
 * <p>Beliefs decay over time and are reinforced by corroborating observations.
 * A belief can be CONTRADICTED (then replaced) or REINFORCED (then promoted
 * to {@link Knowledge} when confidence &gt;= 0.7).
 *
 * <p>Canon audit belief sources (12 total, RI-canon-specific):
 * <ul>
 *   <li>RUMOR — heard from a stranger.</li>
 *   <li>INFERENCE — deduced from other beliefs/knowledge.</li>
 *   <li>OBSERVATION — seen directly (but unverified — could be illusion).</li>
 *   <li>SOUL_SEARCH — extracted via soul-search (but victim could lie via heart-demon).</li>
 *   <li>INHERITED — passed down orally (could be distorted).</li>
 *   <li>DREAM — seen in a dream (often prophetic, often deceptive).</li>
 *   <li>JADE_SLIP — read from a jade slip (scripture, but the scripture could be wrong).</li>
 *   <li>DIVINE_SENSE_SCAN — sensed with divine sense (could be misled by concealment formations).</li>
 *   <li>KARMIC_VISION — seen through karmic connection (usually true but partial).</li>
 *   <li>HEART_DEMON_TRIAL — surfaced during a heart-demon trial (often twists truth).</li>
 *   <li>INHERITANCE_MEMORY — bloodline or soul-fragment memory (true at origin, but context may have shifted).</li>
 *   <li>PROPHECY — divined prophecy (true but subject to interpretation).</li>
 * </ul>
 */
public final class Belief {

    public enum Source {
        RUMOR("Rumor"),
        INFERENCE("Inference"),
        OBSERVATION("Observation"),
        SOUL_SEARCH("Soul-search"),
        INHERITED("Inherited"),
        DREAM("Dream"),
        JADE_SLIP("Jade slip"),
        DIVINE_SENSE_SCAN("Divine-sense scan"),
        KARMIC_VISION("Karmic vision"),
        HEART_DEMON_TRIAL("Heart-demon trial"),
        INHERITANCE_MEMORY("Inheritance memory"),
        PROPHECY("Prophecy");

        public final String label;
        Source(String label) { this.label = label; }
    }

    public final String subject;
    public final String predicate;
    public String value;             // mutable — can be replaced when contradicted
    public final Source source;
    public double confidence;        // 0..1
    public double strength;          // 0..1 — decays over time, reinforced by corroboration
    public long   lastUpdatedTick;
    public long   createdTick;

    public Belief(String subject, String predicate, String value,
                  Source source, double confidence, long tick) {
        this.subject = subject;
        this.predicate = predicate;
        this.value = value;
        this.source = source;
        this.confidence = clamp01(confidence);
        this.strength = clamp01(confidence);
        this.lastUpdatedTick = tick;
        this.createdTick = tick;
    }

    public String key() {
        return subject + "." + predicate;
    }

    /** Decay this belief's strength by a per-tick amount. */
    public void decay(double amount) {
        this.strength = clamp01(this.strength - amount);
    }

    /** Reinforce this belief (raise strength + confidence). */
    public void reinforce(double amount) {
        this.strength = clamp01(this.strength + amount);
        this.confidence = clamp01(this.confidence + amount * 0.5);
    }

    /** Should this belief be promoted to Knowledge? */
    public boolean readyToPromote() {
        return confidence >= 0.7 && strength >= 0.7;
    }

    @Override
    public String toString() {
        return "Belief[" + key() + " = " + value + " (" + source.label
                + " conf=" + Math.round(confidence * 100)
                + "% str=" + Math.round(strength * 100) + "%)]";
    }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
}
