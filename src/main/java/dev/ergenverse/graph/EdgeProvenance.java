package dev.ergenverse.graph;

/**
 * Provenance for graph edges — where the edge comes from.
 *
 * <p>Different from {@code dev.ergenverse.canon.Provenance} (which is for
 * Layer 1 canon facts). Edge provenance is lighter: it just records the
 * source type and a human-readable reference. Full canon provenance is
 * on the referenced registry entry, not duplicated on every edge.
 */
public record EdgeProvenance(Source source, String reference, int confidence) {

    /** Where this edge's information comes from. */
    public enum Source {
        /** Directly attested in the Er Gen novels. Confidence 4-5. */
        CANON,
        /** Derived from canon context (e.g., geographic inference). Confidence 3-4. */
        BOOTSTRAP,
        /** Created by engine logic during gameplay. Confidence N/A (it happened). */
        SIMULATION,
        /** Player action. */
        PLAYER
    }

    /** Simulation edge — no canon reference needed. */
    public static final EdgeProvenance SIMULATION = new EdgeProvenance(Source.SIMULATION, "", 0);

    /** Player action edge. */
    public static final EdgeProvenance PLAYER = new EdgeProvenance(Source.PLAYER, "", 0);

    public EdgeProvenance {
        if (source == null) source = Source.SIMULATION;
        if (reference == null) reference = "";
        confidence = Math.max(0, Math.min(5, confidence));
    }

    /** A canon-source edge provenance. */
    public static EdgeProvenance canon(String reference, int confidence) {
        return new EdgeProvenance(Source.CANON, reference, confidence);
    }

    /** A bootstrap (canon-derived) edge provenance. */
    public static EdgeProvenance bootstrap(String reference, int confidence) {
        return new EdgeProvenance(Source.BOOTSTRAP, reference, confidence);
    }

    /** Whether this edge represents a canon fact. */
    public boolean isCanon() {
        return source == Source.CANON || source == Source.BOOTSTRAP;
    }
}