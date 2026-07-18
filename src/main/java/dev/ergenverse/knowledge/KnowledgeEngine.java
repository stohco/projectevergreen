package dev.ergenverse.knowledge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * KnowledgeEngine — the central knowledge system for a player or NPC.
 *
 * <p>Per the user's directive: "Knowledge should be one of the central progression
 * systems." This engine manages all knowledge entries, handles learning from
 * various sources, and provides queries for what the entity knows/can do.
 *
 * <p>Knowledge replaces traditional "recipe unlocking." Instead of unlocking
 * a recipe, the entity acquires KNOWLEDGE about the recipe. The mastery level
 * determines how well they can execute it and whether they can derive variants.
 */
public final class KnowledgeEngine {

    private final Map<String, KnowledgeEntry> knowledge = new ConcurrentHashMap<>();

    /**
     * Learn a new piece of knowledge, or increase mastery if already known.
     */
    public void learn(KnowledgeEntry entry) {
        KnowledgeEntry existing = knowledge.get(entry.id());
        if (existing != null) {
            // Already known — increase mastery
            existing.increaseMastery(0.15);
        } else {
            knowledge.put(entry.id(), entry);
        }
    }

    /**
     * Learn from a specific source. Creates a new entry if not known.
     */
    public void learn(KnowledgeEntry.KnowledgeType type, KnowledgeEntry.KnowledgeSource source,
                      String subject, String description, long tick) {
        String id = type + ":" + subject.toLowerCase().replace(" ", "_");
        KnowledgeEntry existing = knowledge.get(id);
        if (existing != null) {
            existing.increaseMastery(0.15);
        } else {
            learn(new KnowledgeEntry(id, type, source, subject, description, tick));
        }
    }

    /**
     * Check if the entity knows about a subject.
     */
    public boolean knows(String subject) {
        return knowledge.values().stream()
                .anyMatch(k -> k.subject().equalsIgnoreCase(subject) && k.isUsable());
    }

    /**
     * Check if the entity knows a specific knowledge entry.
     */
    public boolean knows(String id, boolean requireUsable) {
        KnowledgeEntry entry = knowledge.get(id);
        return entry != null && (!requireUsable || entry.isUsable());
    }

    /**
     * Get a specific knowledge entry.
     */
    public KnowledgeEntry get(String id) {
        return knowledge.get(id);
    }

    /**
     * Get all knowledge of a specific type.
     */
    public List<KnowledgeEntry> getByType(KnowledgeEntry.KnowledgeType type) {
        return knowledge.values().stream()
                .filter(k -> k.type() == type)
                .collect(Collectors.toList());
    }

    /**
     * Get all knowledge from a specific source.
     */
    public List<KnowledgeEntry> getBySource(KnowledgeEntry.KnowledgeSource source) {
        return knowledge.values().stream()
                .filter(k -> k.source() == source)
                .collect(Collectors.toList());
    }

    /**
     * Get all recipes the entity knows.
     */
    public List<KnowledgeEntry> getKnownRecipes() {
        return getByType(KnowledgeEntry.KnowledgeType.RECIPE).stream()
                .filter(KnowledgeEntry::isUsable)
                .collect(Collectors.toList());
    }

    /**
     * Check if the entity can derive from a subject (mastery >= 0.6).
     */
    public boolean canDerive(String subject) {
        return knowledge.values().stream()
                .anyMatch(k -> k.subject().equalsIgnoreCase(subject) && k.canDerive());
    }

    /**
     * Get all unlocks provided by known knowledge.
     */
    public Set<String> getAllUnlocks() {
        Set<String> all = new LinkedHashSet<>();
        for (KnowledgeEntry entry : knowledge.values()) {
            if (entry.isUsable()) {
                all.addAll(entry.unlocks());
            }
        }
        return all;
    }

    /**
     * Total knowledge count.
     */
    public int knowledgeCount() {
        return knowledge.size();
    }

    /**
     * Get all knowledge entries (immutable).
     */
    public Collection<KnowledgeEntry> all() {
        return Collections.unmodifiableCollection(knowledge.values());
    }

    /**
     * Get a status report.
     */
    public String getStatusReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("Knowledge Engine: ").append(knowledge.size()).append(" entries\n");
        for (KnowledgeEntry.KnowledgeType type : KnowledgeEntry.KnowledgeType.values()) {
            List<KnowledgeEntry> entries = getByType(type);
            if (!entries.isEmpty()) {
                sb.append("  ").append(type).append(": ").append(entries.size()).append("\n");
                for (KnowledgeEntry e : entries) {
                    sb.append("    ").append(e).append("\n");
                }
            }
        }
        return sb.toString();
    }
}
