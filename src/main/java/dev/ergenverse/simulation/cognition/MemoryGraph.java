package dev.ergenverse.simulation.cognition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MemoryGraph — an actor's memory modeled as a directed graph.
 *
 * <p>Each memory node carries a {@link Type} (15 canonical types), a subject,
 * a description, a timestamp, an emotional valence, and a strength that decays.
 * Edges connect related memories (cause → effect, witness → event, etc.).
 *
 * <p>Canon audit memory types (15):
 * <ul>
 *   <li>OBSERVED — directly seen.</li>
 *   <li>PARTICIPATED — actor was involved.</li>
 *   <li>HEARD — heard from someone (second-hand).</li>
 *   <li>INFERRED — deduced.</li>
 *   <li>DREAMED — seen in a dream.</li>
 *   <li>SOUL_SEARCHED — extracted via soul-search.</li>
 *   <li>INHERITED — passed down.</li>
 *   <li>FORGOTTEN — decayed past recall.</li>
 *   <li>FALSE_MEMORY — implanted by heart-demon or illusion.</li>
 *   <li>SUPPRESSED — actively suppressed by the actor (but can resurface).</li>
 *   <li>BLOODLINE — bloodline memory (genetic).</li>
 *   <li>KARMIC_IMPRINT — burned in by karmic resonance.</li>
 *   <li>TRIBULATION_MEMORY — survived a tribulation (very sticky).</li>
 *   <li>SOUL_FRAGMENT — partial soul memory from a previous life or absorbed fragment.</li>
 *   <li>REINCARNATION_ECHO — faint memory from a past life.</li>
 * </ul>
 */
public final class MemoryGraph {

    public enum Type {
        OBSERVED, PARTICIPATED, HEARD, INFERRED, DREAMED,
        SOUL_SEARCHED, INHERITED, FORGOTTEN, FALSE_MEMORY, SUPPRESSED,
        BLOODLINE, KARMIC_IMPRINT, TRIBULATION_MEMORY, SOUL_FRAGMENT, REINCARNATION_ECHO
    }

    public static final class MemoryNode {
        public final long id;
        public Type type;
        public final String subject;
        public final String description;
        public final long tick;
        public double valence;     // −1..+1 emotional weight
        public double strength;    // 0..1, decays

        public MemoryNode(long id, Type type, String subject, String description,
                          long tick, double valence, double strength) {
            this.id = id;
            this.type = type;
            this.subject = subject;
            this.description = description;
            this.tick = tick;
            this.valence = clamp11(valence);
            this.strength = clamp01(strength);
        }
    }

    public static final class MemoryEdge {
        public final long fromId;
        public final long toId;
        public final String relation;  // "caused", "witnessed", "contradicts", ...
        public MemoryEdge(long from, long to, String relation) {
            this.fromId = from; this.toId = to; this.relation = relation;
        }
    }

    private final Map<Long, MemoryNode> nodes = new HashMap<>();
    private final List<MemoryEdge> edges = new ArrayList<>();
    private long nextId = 1;

    public MemoryNode remember(Type type, String subject, String description,
                                long tick, double valence, double strength) {
        MemoryNode n = new MemoryNode(nextId++, type, subject, description, tick, valence, strength);
        nodes.put(n.id, n);
        return n;
    }

    public void link(long fromId, long toId, String relation) {
        edges.add(new MemoryEdge(fromId, toId, relation));
    }

    public MemoryNode get(long id) {
        return nodes.get(id);
    }

    public List<MemoryNode> about(String subject) {
        List<MemoryNode> out = new ArrayList<>();
        for (MemoryNode n : nodes.values()) {
            if (n.subject.equals(subject) && n.type != Type.FORGOTTEN) {
                out.add(n);
            }
        }
        out.sort((a, b) -> Double.compare(b.strength, a.strength));
        return out;
    }

    /** Apply per-tick decay. SUPPRESSED, BLOODLINE, KARMIC_IMPRINT, TRIBULATION_MEMORY
     *  decay much slower than OBSERVED / HEARD. */
    public void decay(double baseAmount) {
        List<Long> toForget = new ArrayList<>();
        for (MemoryNode n : nodes.values()) {
            double mult = decayMultiplier(n.type);
            n.strength = clamp01(n.strength - baseAmount * mult);
            if (n.strength < 0.05 && n.type != Type.BLOODLINE
                    && n.type != Type.KARMIC_IMPRINT
                    && n.type != Type.TRIBULATION_MEMORY) {
                n.type = Type.FORGOTTEN;
            }
        }
        for (Long id : toForget) nodes.remove(id);
    }

    private static double decayMultiplier(Type t) {
        switch (t) {
            case OBSERVED:           return 1.0;
            case PARTICIPATED:       return 0.6;
            case HEARD:              return 1.5;
            case INFERRED:           return 0.8;
            case DREAMED:            return 2.0;
            case SOUL_SEARCHED:      return 0.4;
            case INHERITED:          return 0.3;
            case FORGOTTEN:          return 1.0;
            case FALSE_MEMORY:       return 0.5;
            case SUPPRESSED:         return 0.05;
            case BLOODLINE:          return 0.0;
            case KARMIC_IMPRINT:     return 0.0;
            case TRIBULATION_MEMORY: return 0.0;
            case SOUL_FRAGMENT:      return 0.1;
            case REINCARNATION_ECHO: return 0.05;
            default:                 return 1.0;
        }
    }

    public int size() { return nodes.size(); }
    public int edgeCount() { return edges.size(); }

    private static double clamp01(double v) {
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }
    private static double clamp11(double v) {
        if (v < -1) return -1;
        if (v > 1) return 1;
        return v;
    }
}
