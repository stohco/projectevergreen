package dev.ergenverse.wanglin.registry;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * CanonicalEntry — the universal record for every entity in Wang Lin's knowledge graph.
 *
 * <p>Every item, technique, beast, companion, avatar, inheritance, treasure,
 * Dao, essence, restriction, formation, puppet, realm, ally, enemy, or
 * historical event Wang Lin encountered is wrapped in a CanonicalEntry. The
 * record carries the canon metadata, the ownership state relative to Wang Lin,
 * the transferability verdict (what he can do with it), the demonstrability
 * verdict (what he can show), the canon summary of what the novels say it IS,
 * the canon-demonstrated behaviors (what the novels show it DOING), and the
 * interaction tags used to walk the graph.
 *
 * <h2>Provenance on everything</h2>
 * <p>Every field traces back to {@code CANON_RI_COMPLETE_*.md} or
 * {@code ri_canon_database.json}. The {@link Provenance} records sourceNovel,
 * chapters, EXPLICIT/INFERRED, confidence 1..5, and ambiguities.
 *
 * <h2>Identity</h2>
 * <p>The {@code id} is a stable, lowercase-kebab identifier (e.g.
 * "restriction_flag", "heaven_defying_bead", "lu_mo_slaughter_clone"). Where
 * an item already exists in {@code RICanonicalDatabase} (e.g. "I09
 * Restriction Flag"), the id mirrors that canon id (e.g. "I09_restriction_flag")
 * so the two can be cross-referenced. Where the entry is a relationship /
 * event / knowledge-graph node not present in the database, the id is a new
 * stable identifier.
 *
 * <h2>Interaction tags</h2>
 * <p>The {@link #interactionTags} list enables graph-walking queries like
 * "show me everything tagged 'soul' that Wang Lin can teach". Tags are
 * lowercased kebab identifiers: "soul", "formation", "restriction",
 * "divine_sense", "ancient_god", "vermilion_bird", "celestial", "karma",
 * "time", "space", "samsara", "slaughter", "fire", "thunder", "wood",
 * "metal", "water", "earth", "primordial", "silent_extinction",
 * "reincarnation", "true_false", "life_death", "sword", "whip", "stamp",
 * "shield", "armor", "puppet", "clone", "beast", "devil", "inheritance",
 * "tu_si", "situ_nan", "bai_fan", "qing_shui", "dun_tian", "all_seer",
 * "seven_colored_daoist", "daoist_water", "tuo_sen", "gu_dao", etc.
 *
 * @param id                       stable lowercase-kebab identifier
 * @param displayName              human-readable EN name
 * @param displayNameCn            human-readable CN name (empty if unattested)
 * @param category                 the knowledge-graph node this belongs to
 * @param provenance               source novel, chapters, attestation, confidence, ambiguities
 * @param ownership                ownership state relative to Wang Lin
 * @param transferability          what Wang Lin can do with this (teach/demonstrate/explain/transfer/gift/create)
 * @param demonstrability          what Wang Lin can show (separate from transferability's teaching gate)
 * @param canonSummary             what the novels say it IS
 * @param canonDemonstratedBehaviors what the novels show it DOING (chapter-cited list)
 * @param interactionTags          graph-walking tags
 * @param behaviorSpecId           optional id linking this entry to a {@code BehavioralSpec}
 *                                  (in {@code dev.ergenverse.wanglin.behavior.BehavioralSpecRegistry}).
 *                                  Null when no behavioral spec exists for this entry. When set,
 *                                  callers can look up the spec via
 *                                  {@code BehavioralSpecRegistry.get(entry.behaviorSpecId())} to
 *                                  retrieve the full reverse-engineered observable mechanics
 *                                  (acquisition, lore, prerequisites, cost, activation, range,
 *                                  scaling, counters, weaknesses, environmental effects, visual,
 *                                  states, interactions, MC impl notes, NPC/player/AI usage).
 */
public record CanonicalEntry(
        String id,
        String displayName,
        String displayNameCn,
        CanonicalCategory category,
        Provenance provenance,
        OwnershipState ownership,
        Transferability transferability,
        Demonstrability demonstrability,
        String canonSummary,
        List<String> canonDemonstratedBehaviors,
        List<String> interactionTags,
        String behaviorSpecId
) {
    public CanonicalEntry {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("CanonicalEntry requires an id");
        }
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("CanonicalEntry '" + id + "' requires a displayName");
        }
        if (category == null) {
            throw new IllegalArgumentException("CanonicalEntry '" + id + "' requires a category");
        }
        if (provenance == null) {
            throw new IllegalArgumentException("CanonicalEntry '" + id + "' requires a Provenance");
        }
        if (ownership == null) ownership = OwnershipState.HEARD_OF_ONLY;
        if (transferability == null) transferability = Transferability.uniquelyBound("Default: gap.");
        if (demonstrability == null) demonstrability = Demonstrability.UNKNOWN;
        if (displayNameCn == null) displayNameCn = "";
        if (canonSummary == null) canonSummary = "";
        if (canonDemonstratedBehaviors == null) canonDemonstratedBehaviors = List.of();
        if (interactionTags == null) interactionTags = List.of();
        if (behaviorSpecId != null && behaviorSpecId.isBlank()) behaviorSpecId = null;
    }

    /** Convenience: is this entry eligible for the TeachingResolver's ownership gate? */
    public boolean passesOwnershipGate() {
        return ownership.passesOwnershipGate();
    }

    /** Convenience: does this entry have the given interaction tag? */
    public boolean hasTag(String tag) {
        if (tag == null) return false;
        return interactionTags.contains(tag);
    }

    /** Convenience: does this entry have a linked BehavioralSpec? */
    public boolean hasBehaviorSpec() {
        return behaviorSpecId != null && !behaviorSpecId.isBlank();
    }

    /** Compact one-line citation for tooltips / debug output. */
    public String citation() {
        return displayName + " [" + provenance.citation() + "]";
    }

    // ── Static factories ──────────────────────────────────────────────────

    /**
     * 11-arg factory preserving the original constructor signature (before
     * {@code behaviorSpecId} was added). Defaults {@code behaviorSpecId} to null.
     * Used by the 18 sub-registries' existing call sites.
     */
    public static CanonicalEntry of(
            String id,
            String displayName,
            String displayNameCn,
            CanonicalCategory category,
            Provenance provenance,
            OwnershipState ownership,
            Transferability transferability,
            Demonstrability demonstrability,
            String canonSummary,
            List<String> canonDemonstratedBehaviors,
            List<String> interactionTags
    ) {
        return new CanonicalEntry(id, displayName, displayNameCn, category, provenance,
                ownership, transferability, demonstrability, canonSummary,
                canonDemonstratedBehaviors, interactionTags, null);
    }

    /**
     * 12-arg factory that takes an explicit {@code behaviorSpecId}. Use this for
     * entries that have a corresponding {@code BehavioralSpec} (e.g. the 5
     * signature items: restriction_flag_1st/2nd/3rd, heaven_defying_bead,
     * karma_whip, soul_refining, soul_flag_production_method, the puppet entries).
     */
    public static CanonicalEntry withSpec(
            String id,
            String displayName,
            String displayNameCn,
            CanonicalCategory category,
            Provenance provenance,
            OwnershipState ownership,
            Transferability transferability,
            Demonstrability demonstrability,
            String canonSummary,
            List<String> canonDemonstratedBehaviors,
            List<String> interactionTags,
            String behaviorSpecId
    ) {
        return new CanonicalEntry(id, displayName, displayNameCn, category, provenance,
                ownership, transferability, demonstrability, canonSummary,
                canonDemonstratedBehaviors, interactionTags, behaviorSpecId);
    }
}
