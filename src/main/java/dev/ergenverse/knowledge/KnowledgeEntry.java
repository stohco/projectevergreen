package dev.ergenverse.knowledge;

import dev.ergenverse.canon.Provenance;
import java.util.*;

/**
 * KnowledgeEntry — a single piece of knowledge the player (or NPC) possesses.
 *
 * <p>Per the user's directive: "You don't unlock recipes. You acquire knowledge.
 * Knowledge sources: jade slips, manuals, sect libraries, observing masters,
 * experimentation, divine sense, dreams, memories, Wang Lin, ruins, inheritances."
 *
 * <p>Knowledge unlocks: recipes, techniques, restrictions, formations, theories, diagnoses.
 */
public final class KnowledgeEntry {

    public enum KnowledgeType {
        RECIPE,         // a crafting recipe (canon or derived)
        TECHNIQUE,      // a cultivation technique
        RESTRICTION,    // a restriction pattern
        FORMATION,      // a formation array
        THEORY,         // a theoretical understanding (enables derivation)
        DIAGNOSIS,      // ability to diagnose conditions
        MATERIAL_LORE,  // knowledge about a material's properties
        BEAST_LORE,     // knowledge about a beast species
        LOCATION_LORE,  // knowledge about a location
        DAO_INSIGHT,    // a Dao comprehension
        HISTORY,        // a historical fact
        SECRET          // a hidden truth (from inheritance, soul search, etc.)
    }

    public enum KnowledgeSource {
        JADE_SLIP,      // read from a jade slip
        MANUAL,         // studied from a manual/book
        SECT_LIBRARY,   // learned in a sect library
        OBSERVATION,    // observed a master performing it
        EXPERIMENTATION, // discovered through trial and error
        DIVINE_SENSE,   // perceived through divine sense
        DREAM,          // seen in a dream/vision
        MEMORY,         // inherited or soul-searched memory
        WANG_LIN,       // taught by Wang Lin's manifestation
        RUINS,          // found in ancient ruins
        INHERITANCE,    // received through inheritance trial
        CANON_SEEDING   // pre-loaded at edge of canon
    }

    private final String id;
    private final KnowledgeType type;
    private final KnowledgeSource source;
    private final String subject;      // what this knowledge is about
    private final String description;  // what the knowledge says
    private double mastery;            // 0.0-1.0 (how well understood)
    private final long acquiredTick;   // when it was learned
    private final Set<String> unlocks; // what this knowledge enables (recipe IDs, technique IDs, etc.)
    private final Provenance provenance;

    public KnowledgeEntry(String id, KnowledgeType type, KnowledgeSource source,
                          String subject, String description, long tick) {
        this.id = id;
        this.type = type;
        this.source = source;
        this.subject = subject;
        this.description = description;
        this.mastery = 0.1; // starts at basic understanding
        this.acquiredTick = tick;
        this.unlocks = new LinkedHashSet<>();
        this.provenance = Provenance.inferred("Experience", null, 3);
    }

    public String id() { return id; }
    public KnowledgeType type() { return type; }
    public KnowledgeSource source() { return source; }
    public String subject() { return subject; }
    public String description() { return description; }
    public double mastery() { return mastery; }
    public long acquiredTick() { return acquiredTick; }
    public Set<String> unlocks() { return Collections.unmodifiableSet(unlocks); }
    public Provenance provenance() { return provenance; }

    public void setMastery(double m) { this.mastery = Math.max(0.0, Math.min(1.0, m)); }
    public void increaseMastery(double amount) { this.mastery = Math.min(1.0, this.mastery + amount); }

    public KnowledgeEntry unlocks(String id) {
        this.unlocks.add(id);
        return this;
    }

    /**
     * Check if the player has mastered this knowledge enough to use it.
     */
    public boolean isUsable() {
        return mastery >= 0.3;
    }

    /**
     * Check if the player has mastered this knowledge enough to derive from it.
     */
    public boolean canDerive() {
        return mastery >= 0.6;
    }

    /**
     * Check if the player has mastered this knowledge enough to teach it.
     */
    public boolean canTeach() {
        return mastery >= 0.8;
    }

    @Override
    public String toString() {
        return type + ": " + subject + " (" + String.format("%.0f%%", mastery * 100) + ")";
    }
}
