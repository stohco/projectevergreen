package dev.ergenverse.canon;

/**
 * The Canon Reconstruction Engine.
 *
 * <p>Every piece of data in the simulation carries a canon confidence rating.
 * This prevents the engine from treating confirmed novel content the same
 * as procedurally generated filler. Canon preservation over invention.
 *
 * <h2>Canon Confidence Levels</h2>
 * <ul>
 *   <li><b>5</b> — Direct novel statement (immutable reality — protagonist
 *       death, named items, named relationships, cosmological structure)</li>
 *   <li><b>4</b> — Explicit wiki summary backed by novel (treated as
 *       reality in-game)</li>
 *   <li><b>3</b> — Strong implication from canon (requires caution —
 *       presented as "known but uncertain")</li>
 *   <li><b>2</b> — Community interpretation (becomes RUMOR in-game — NPCs
 *       whisper it, player may hear conflicting versions)</li>
 *   <li><b>1</b> — Speculation (becomes LEGEND in-game — ancient tales,
 *       half-forgotten)</li>
 *   <li><b>0</b> — Generated filler (FORBIDDEN in canon regions; only in
 *       unknown territory, and always labeled as "the unknown")</li>
 * </ul>
 *
 * <p>Design principle (encoded):
 * <pre>
 *   "Do not create an original xianxia universe. Reconstruct the Er Gen
 *    multiverse from primary sources. Canon preservation over invention.
 *    Unknown stays unknown until discovered."
 * </pre>
 */
public final class CanonEngine {

    private static volatile boolean bootstrapped = false;

    private CanonEngine() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        // No state to load — the engine is pure functions over canon data.
        // The bootstrap exists to make the registration order explicit in
        // the mod entry point.
    }

    /** Canon confidence rating 0-5. */
    public enum Confidence {
        FILLER(0),
        SPECULATION(1),
        COMMUNITY(2),
        IMPLICATION(3),
        WIKI_BACKED(4),
        NOVEL_STATEMENT(5);

        public final int level;
        Confidence(int level) { this.level = level; }

        public static Confidence fromLevel(int level) {
            for (Confidence c : values()) if (c.level == level) return c;
            return level >= 5 ? NOVEL_STATEMENT : FILLER;
        }
    }

    /** The in-game reality level — what the player perceives a fact as. */
    public enum RealityLevel {
        REALITY("Canon Reality", "Confirmed truth of the Er Gen multiverse. Immutable.", 0x4CAF7A),
        TRADITION("Canon Tradition", "Widely held belief with canon basis. May hide deeper truth.", 0xB89554),
        RUMOR("Whispered Rumor", "NPCs whisper this. Often partially wrong. Conflicting versions exist.", 0xA88A3C),
        LEGEND("Ancient Legend", "Fragmented tale from before memory. Truth may be greater.", 0x9B7AC4),
        UNKNOWN("The Unknown", "No record exists. The void beyond knowledge. Preserved, not filled.", 0x7A7A8C),
        FORBIDDEN("Forbidden Truth", "Sealed by the world itself. Knowing it draws tribulation.", 0xA63C3C);

        public final String label;
        public final String description;
        public final int color; // RGB for HUD display
        RealityLevel(String label, String description, int color) {
            this.label = label;
            this.description = description;
            this.color = color;
        }
    }

    /** Region status — controls what canon confidence is allowed where. */
    public enum RegionStatus {
        KNOWN_CANON,
        PARTIALLY_KNOWN,
        FRONTIER,
        UNKNOWN
    }

    /** The result of a canon filter check. */
    public static final class FilterResult {
        public final boolean allowed;
        public final RealityLevel reality;
        public final String reason;

        public FilterResult(boolean allowed, RealityLevel reality, String reason) {
            this.allowed = allowed;
            this.reality = reality;
            this.reason = reason;
        }
    }

    /**
     * Map a canon confidence level to an in-game reality level.
     */
    public static RealityLevel confidenceToReality(Confidence c) {
        switch (c) {
            case NOVEL_STATEMENT:
            case WIKI_BACKED:    return RealityLevel.REALITY;
            case IMPLICATION:    return RealityLevel.TRADITION;
            case COMMUNITY:      return RealityLevel.RUMOR;
            case SPECULATION:    return RealityLevel.LEGEND;
            case FILLER:
            default:             return RealityLevel.UNKNOWN;
        }
    }

    /**
     * The canon filter. Used at world-gen and content-presentation time.
     *
     * <p>In known canon regions, Level 0 (filler) is <b>FORBIDDEN</b> entirely.
     * The engine must never generate a Level 0 "fact" as reality.
     *
     * @param confidence   the entry's canon confidence (0-5)
     * @param regionStatus the region's status (known canon / frontier / unknown)
     * @return whether the entry may be presented as reality, and at what level
     */
    public static FilterResult filter(Confidence confidence, RegionStatus regionStatus) {
        switch (regionStatus) {
            case KNOWN_CANON: {
                if (confidence == Confidence.FILLER) {
                    return new FilterResult(false, RealityLevel.UNKNOWN,
                        "Level 0 filler forbidden in known canon regions.");
                }
                return new FilterResult(true, confidenceToReality(confidence),
                    "Known canon region.");
            }
            case PARTIALLY_KNOWN: {
                if (confidence == Confidence.FILLER) {
                    return new FilterResult(true, RealityLevel.UNKNOWN,
                        "Filler permitted in partially-known regions, but presented as the unknown.");
                }
                return new FilterResult(true, confidenceToReality(confidence),
                    "Partially-known region.");
            }
            case FRONTIER: {
                // Frontier: canon facts (5,4) anchor reality; everything
                // else is rumor/legend.
                if (confidence.level >= 4) return new FilterResult(true, RealityLevel.REALITY,
                    "Canon anchor in frontier territory.");
                if (confidence.level == 3) return new FilterResult(true, RealityLevel.TRADITION,
                    "Frontier tradition.");
                if (confidence.level == 2) return new FilterResult(true, RealityLevel.RUMOR,
                    "Frontier rumor.");
                if (confidence.level == 1) return new FilterResult(true, RealityLevel.LEGEND,
                    "Frontier legend.");
                return new FilterResult(true, RealityLevel.UNKNOWN,
                    "Frontier unknown — preserved.");
            }
            case UNKNOWN:
            default: {
                // In unknown territory, NOTHING may be presented as
                // confirmed reality. Canon anchors (5,4) degrade to
                // "legend" — the player may recognize them.
                if (confidence.level >= 3) return new FilterResult(true, RealityLevel.LEGEND,
                    "Canon fact surfacing as legend in unknown territory.");
                if (confidence.level == 2) return new FilterResult(true, RealityLevel.RUMOR,
                    "Unknown territory rumor.");
                if (confidence.level == 1) return new FilterResult(true, RealityLevel.LEGEND,
                    "Unknown territory legend.");
                return new FilterResult(true, RealityLevel.UNKNOWN,
                    "Unknown territory — preserved as the unknown.");
            }
        }
    }

    /**
     * Convenience overload for integer confidence levels.
     */
    public static FilterResult filter(int confidenceLevel, RegionStatus regionStatus) {
        return filter(Confidence.fromLevel(confidenceLevel), regionStatus);
    }
}
