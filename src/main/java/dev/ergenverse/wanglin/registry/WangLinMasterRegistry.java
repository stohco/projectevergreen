package dev.ergenverse.wanglin.registry;

import dev.ergenverse.core.Ergenverse;

/**
 * WangLinMasterRegistry — the central registry of Wang Lin's complete canonical knowledge graph.
 *
 * <p>This is the single entry point the user demanded. It holds references to
 * all 18 sub-registries (Inventory, Techniques, Knowledge, Pets, Companions,
 * Avatars, Bodies, Restrictions, Formations, Essences, Dao, Titles, Skills,
 * Experiences, Enemies, Allies, Realms, History) and bootstraps them in a
 * deterministic order.
 *
 * <h2>Bootstrap order</h2>
 * <ol>
 *   <li>Inventory, Techniques — independent</li>
 *   <li>Bodies, Avatars, Clones — depends on Ancient God lineage</li>
 *   <li>Restrictions, Formations, Puppets — Wang Lin's signature systems</li>
 *   <li>Essences, Domains, Dao — depends on SamsaraDao</li>
 *   <li>Skills, Titles, Experiences — derived from above</li>
 *   <li>Pets, Companions — cross-refs pet package</li>
 *   <li>Enemies, Allies — cross-refs WangLinAntagonists + WangLinRelationships</li>
 *   <li>Realms, History — cross-refs WangLinCosmology + RITimelineEngine</li>
 *   <li>Knowledge — abstract lore, last</li>
 * </ol>
 *
 * <p>The {@link #bootstrap()} method is idempotent. After it returns, all
 * sub-registries are populated and queryable. The {@link TeachingResolver}
 * is also initialized.
 *
 * <p>This registry is called from {@link dev.ergenverse.core.Ergenverse}
 * AFTER {@link dev.ergenverse.wanglin.WangLinCosmologyRegistry#bootstrap()}
 * (so it can cross-reference cosmology, timeline, antagonists, etc.) and
 * BEFORE the {@link dev.ergenverse.wanglin.WangLinItems} DeferredRegister
 * (so the item tooltips can pull from the registry if needed).
 */
public final class WangLinMasterRegistry {

    private WangLinMasterRegistry() {}

    private static volatile boolean bootstrapped = false;

    // ── Sub-registries (populated at bootstrap) ──────────────────────
    public static CanonicalInventory INVENTORY;
    public static CanonicalTechniques TECHNIQUES;
    public static CanonicalKnowledge KNOWLEDGE;
    public static CanonicalPets PETS;
    public static CanonicalCompanions COMPANIONS;
    public static CanonicalAvatars AVATARS;
    public static CanonicalBodies BODIES;
    public static CanonicalRestrictions RESTRICTIONS;
    public static CanonicalFormations FORMATIONS;
    public static CanonicalEssences ESSENCES;
    public static CanonicalDao DAO;
    public static CanonicalTitles TITLES;
    public static CanonicalSkills SKILLS;
    public static CanonicalExperiences EXPERIENCES;
    public static CanonicalEnemies ENEMIES;
    public static CanonicalAllies ALLIES;
    public static CanonicalRealms REALMS;
    public static CanonicalHistoricalEvents HISTORY;

    /** The TeachingResolver — initialized after sub-registries. */
    public static TeachingResolver TEACHING_RESOLVER;

    /**
     * Bootstrap the entire canonical knowledge graph. Idempotent.
     */
    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        Ergenverse.LOGGER.info("[WangLinMasterRegistry] ════════════════════════════════════════════");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]  Bootstrapping complete canonical knowledge graph...");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]  (NOT a whitelist — every node expands to canon entries)");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry] ════════════════════════════════════════════");

        // 1. Inventory — Wang Lin's owned items (signature subset; cross-refs DB)
        INVENTORY = new CanonicalInventory();
        INVENTORY.bootstrap();

        // 2. Techniques — every technique Wang Lin learned / created / inherited
        TECHNIQUES = new CanonicalTechniques();
        TECHNIQUES.bootstrap();

        // 3. Bodies — Ancient God star-tiers, True Bodies
        BODIES = new CanonicalBodies();
        BODIES.bootstrap();

        // 4. Avatars — clones & true bodies (Lu Mo, Void Avatar, etc.)
        AVATARS = new CanonicalAvatars();
        AVATARS.bootstrap();

        // 5. Restrictions — 4 Great Restrictions + Restriction Flag system + Restriction Essence
        RESTRICTIONS = new CanonicalRestrictions();
        RESTRICTIONS.bootstrap();

        // 6. Formations — Soul Devil Ship, Unnamed Wheel Formation, 9 Deaths Perish, etc.
        FORMATIONS = new CanonicalFormations();
        FORMATIONS.bootstrap();

        // 7. Essences — the 14 Essences of Samsara Dao
        ESSENCES = new CanonicalEssences();
        ESSENCES.bootstrap();

        // 8. Dao — Wang Lin's Dao comprehension track
        DAO = new CanonicalDao();
        DAO.bootstrap();

        // 9. Titles — titles & honorifics
        TITLES = new CanonicalTitles();
        TITLES.bootstrap();

        // 10. Skills — discrete skills (Soul Refining, Alchemy, Refining, etc.)
        SKILLS = new CanonicalSkills();
        SKILLS.bootstrap();

        // 11. Experiences — defining life experiences (Teng Clan, Restriction Mountain, Reincarnation Pool)
        EXPERIENCES = new CanonicalExperiences();
        EXPERIENCES.bootstrap();

        // 12. Pets — delegates to dev.ergenverse.wanglin.pet for full data model
        PETS = new CanonicalPets();
        PETS.bootstrap();

        // 13. Companions — devils (Xu Liguo), life-bound beasts (Nether Beast), etc.
        COMPANIONS = new CanonicalCompanions();
        COMPANIONS.bootstrap();

        // 14. Enemies — antagonists (cross-refs WangLinAntagonists)
        ENEMIES = new CanonicalEnemies();
        ENEMIES.bootstrap();

        // 15. Allies — mentors & allies (cross-refs WangLinRelationships)
        ALLIES = new CanonicalAllies();
        ALLIES.bootstrap();

        // 16. Realms — cosmology layers (cross-refs WangLinCosmology.Layer)
        REALMS = new CanonicalRealms();
        REALMS.bootstrap();

        // 17. History — Wang Lin's life events (cross-refs RITimelineEngine)
        HISTORY = new CanonicalHistoricalEvents();
        HISTORY.bootstrap();

        // 18. Knowledge — abstract lore (last; depends on everything above)
        KNOWLEDGE = new CanonicalKnowledge();
        KNOWLEDGE.bootstrap();

        // Teaching Resolver — initialized AFTER all sub-registries
        TEACHING_RESOLVER = new TeachingResolver();

        int total = totalEntries();
        Ergenverse.LOGGER.info("[WangLinMasterRegistry] ──────────────────────────────────────");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]  Knowledge graph complete: {} canonical entries across {} sub-registries",
                total, 18);
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]  TeachingResolver initialized.");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry]  Prime Directive: every entry carries a Provenance; gaps are flagged UNKNOWN.");
        Ergenverse.LOGGER.info("[WangLinMasterRegistry] ════════════════════════════════════════════");
    }

    /** Total entries across all sub-registries. */
    public static int totalEntries() {
        if (!bootstrapped) return 0;
        return INVENTORY.size() + TECHNIQUES.size() + KNOWLEDGE.size()
                + PETS.size() + COMPANIONS.size() + AVATARS.size()
                + BODIES.size() + RESTRICTIONS.size() + FORMATIONS.size()
                + ESSENCES.size() + DAO.size() + TITLES.size()
                + SKILLS.size() + EXPERIENCES.size() + ENEMIES.size()
                + ALLIES.size() + REALMS.size() + HISTORY.size();
    }

    /** Lookup an entry across ALL sub-registries by id. Returns null if not found. */
    public static CanonicalEntry lookup(String id) {
        if (!bootstrapped) return null;
        CanonicalEntry e;
        if ((e = INVENTORY.get(id)) != null) return e;
        if ((e = TECHNIQUES.get(id)) != null) return e;
        if ((e = KNOWLEDGE.get(id)) != null) return e;
        if ((e = PETS.get(id)) != null) return e;
        if ((e = COMPANIONS.get(id)) != null) return e;
        if ((e = AVATARS.get(id)) != null) return e;
        if ((e = BODIES.get(id)) != null) return e;
        if ((e = RESTRICTIONS.get(id)) != null) return e;
        if ((e = FORMATIONS.get(id)) != null) return e;
        if ((e = ESSENCES.get(id)) != null) return e;
        if ((e = DAO.get(id)) != null) return e;
        if ((e = TITLES.get(id)) != null) return e;
        if ((e = SKILLS.get(id)) != null) return e;
        if ((e = EXPERIENCES.get(id)) != null) return e;
        if ((e = ENEMIES.get(id)) != null) return e;
        if ((e = ALLIES.get(id)) != null) return e;
        if ((e = REALMS.get(id)) != null) return e;
        if ((e = HISTORY.get(id)) != null) return e;
        return null;
    }
}
