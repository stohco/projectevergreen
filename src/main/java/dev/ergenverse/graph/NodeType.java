package dev.ergenverse.graph;

/**
 * The type of a graph node.
 *
 * <p>Each type has a namespace prefix used in NodeId's string form
 * (e.g. "artifact:heaven_defying_bead"). The prefix is also used for
 * dispatch — engines can filter nodes by type to find relevant entities.
 *
 * <p>The types cover the Er Gen multiverse. As new engines are added,
 * new types are added here. This is an open enum — it will grow with
 * the project.
 */
public enum NodeType {
    // ── Canon-derived types (loaded from registry) ────────────────
    /** A weapon, treasure, or item (maps to CanonicalInventory). */
    ARTIFACT("artifact"),
    /** A cultivation technique or spell (maps to CanonicalTechniques). */
    TECHNIQUE("technique"),
    /** A spirit beast or pet (maps to CanonicalPets). */
    BEAST("beast"),
    /** A contracted companion (maps to CanonicalCompanions). */
    COMPANION("companion"),
    /** A clone or avatar (maps to CanonicalAvatars). */
    AVATAR("avatar"),
    /** A body cultivation state (maps to CanonicalBodies). */
    BODY("body"),
    /** A restriction technique (maps to CanonicalRestrictions). */
    RESTRICTION("restriction"),
    /** A formation array (maps to CanonicalFormations). */
    FORMATION("formation"),
    /** A Dao or essence comprehension (maps to CanonicalDao + CanonicalEssences). */
    DAO("dao"),
    /** A named character (NPC or player). Sub-roles: protagonist, antagonist, elder, disciple,
     *  divine_emperor, patriarch, family_head, mount, servant, artifact_spirit. */
    NPC("npc"),
    /** A location (realm, planet, continent, country, sect, city, mountain, valley, cave,
     *  sea, secret_realm, tomb, star_domain, star_system). */
    LOCATION("location"),
    /** A historical event (maps to CanonicalHistoricalEvents). */
    EVENT("event"),
    /** Abstract knowledge (maps to CanonicalKnowledge). */
    KNOWLEDGE("knowledge"),
    /** A title or honorific (maps to CanonicalTitles). */
    TITLE("title"),
    /** A discrete skill (maps to CanonicalSkills). */
    SKILL("skill"),
    /** A life experience (maps to CanonicalExperiences). */
    EXPERIENCE("experience"),
    /** An ally relationship (maps to CanonicalAllies). */
    ALLY("ally"),
    /** An enemy (maps to CanonicalEnemies). */
    ENEMY("enemy"),
    /** A cosmological realm (maps to CanonicalRealms). */
    REALM("realm"),

    // ── Simulation-generated types (created by engines) ───────────
    /** A spirit plant / herb. 5 grades: mortal, spirit, earth, heaven, dao.
     *  9 environments. Has composition parts (leaves, stem, seeds, roots, flower, fruit). */
    PLANT("plant"),
    /** A sect or faction. 6 types: sect, clan, royal_court, merchant_guild, rogue_band, demon_sect.
     *  Has alignment, treasury, treaties, politics. */
    FACTION("faction"),
    /** @deprecated Use FACTION instead. Kept for backward compatibility. */
    @Deprecated SECT("faction"),
    /** An inventory container (player, NPC, sect treasury). */
    INVENTORY("inventory"),
    /** A treasury (sect-level inventory). */
    TREASURY("treasury"),
    /** A market / shop (created by Economy Engine). */
    MARKET("market"),
    /** A structure within a location (building, room, chamber). */
    STRUCTURE("structure"),
    /** A spirit vein or qi source. */
    SPIRIT_VEIN("spirit_vein"),
    /** A region-level environmental state. */
    ENVIRONMENT("environment"),
    /** A cosmological layer (Planet, Cave World, Immortal Astral Continent).
     *  5 tiers: singular_world, true_world, expanse_cosmos, vast_expanse, universe. */
    WORLD("world"),
    /** A personal pocket dimension inside a storage treasure (Cave World, storage cave). */
    CAVE_WORLD("cave_world"),
    /** A rumor or information propagating through the world. */
    RUMOR("rumor"),
    /** A contract or agreement between entities. */
    CONTRACT("contract"),
    /** A law or rule (sect rules, city laws). */
    LAW("law"),
    /** A karmic thread between entities (visible at Nascent Soul+). */
    KARMA_THREAD("karma"),
    /** A Dao Essence (one of Wang Lin's 14 Essences). */
    ESSENCE("essence"),
    /** A Heaven Trampling Bridge (9 bridges, each a unique trial). */
    BRIDGE("bridge"),
    /** An Accompanying Thunder (stolen/devoured, contributes to Thunder Essence). */
    ACCOMPANYING_THUNDER("accompanying_thunder"),
    /** A tribulation event (heart demon, lightning, karma). */
    TRIBULATION("tribulation"),
    /** A Joss Flame flow (mortal faith -> cultivation energy). */
    JOSS_FLAME_FLOW("joss_flame"),
    /** An inheritance site (sealed, opening, contested, claimed). */
    INHERITANCE("inheritance"),
    /** A world exit method (authorized or illegal). */
    WORLD_EXIT("world_exit"),
    /** A protagonist manifestation/clone (Tier 0/1/2 LOD). */
    MANIFESTATION("manifestation"),
    /** A cosmological position (Vermilion Bird Emperor, Cave World owner — singular). */
    POSITION("position"),
    /** A beast species (ecological type, not individual instance). */
    BEAST_SPECIES("beast_species"),
    /** A bloodline (Ancient God, Ancient Devil, Ancient Demon). */
    BLOODLINE("bloodline"),
    /** A composition part (beast heart/blood/bones/horn/core/soul/scales, plant parts). */
    PART("part"),
    /** A soul brand layer on an artifact (per-owner imprint). */
    SOUL_BRAND("soul_brand"),
    /** A historical timeline event (one of 108+ events across 11 eras). */
    HISTORY_EVENT("history_event"),
    /** An artifact spirit (sentient treasure personality, e.g. Xu Liguo). */
    ARTIFACT_SPIRIT("artifact_spirit"),
    /** A language or script system. */
    LANGUAGE("language"),
    /** A transportation route or connection. */
    ROUTE("route"),

    // ── Meta types ────────────────────────────────────────────────
    /** Fallback type when no specific type applies. */
    GENERIC("generic");

    private final String prefix;

    NodeType(String prefix) {
        this.prefix = prefix;
    }

    /** The namespace prefix for NodeId string form. */
    public String prefix() {
        return prefix;
    }

    /** Resolve a prefix string back to a NodeType. Returns GENERIC if unknown. */
    public static NodeType fromPrefix(String prefix) {
        if (prefix == null) return GENERIC;
        for (NodeType t : values()) {
            if (t.prefix.equals(prefix)) return t;
        }
        return GENERIC;
    }

    /** Whether this type corresponds to a canon entity (loaded from the registry). */
    public boolean isCanonType() {
        return this == ARTIFACT || this == TECHNIQUE || this == BEAST || this == COMPANION
                || this == AVATAR || this == BODY || this == RESTRICTION || this == FORMATION
                || this == DAO || this == NPC || this == LOCATION || this == EVENT
                || this == KNOWLEDGE || this == TITLE || this == SKILL || this == EXPERIENCE
                || this == ALLY || this == ENEMY || this == REALM || this == ESSENCE
                || this == BRIDGE || this == ACCOMPANYING_THUNDER || this == BEAST_SPECIES
                || this == BLOODLINE || this == INHERITANCE || this == FACTION
                || this == CAVE_WORLD || this == WORLD || this == HISTORY_EVENT
                || this == POSITION || this == WORLD_EXIT || this == MANIFESTATION;
    }

    /** Whether this type is a living entity (has CultivationState, SoulMatrix). */
    public boolean isLivingEntity() {
        return this == NPC || this == BEAST || this == COMPANION || this == AVATAR
                || this == MANIFESTATION || this == ARTIFACT_SPIRIT;
    }

    /** Whether this type can carry a Composition component (has harvestable parts). */
    public boolean hasComposition() {
        return this == BEAST || this == PLANT || this == BEAST_SPECIES;
    }

    /** Whether this type can carry a RealityProfile (world law data). */
    public boolean hasReality() {
        return this == LOCATION || this == CAVE_WORLD || this == WORLD;
    }
}