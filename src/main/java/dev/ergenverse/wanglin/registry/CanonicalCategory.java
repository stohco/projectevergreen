package dev.ergenverse.wanglin.registry;

/**
 * CanonicalCategory — every top-level node of Wang Lin's knowledge graph.
 *
 * <p>The user enumerated these nodes explicitly: Cultivation, Body cultivation,
 * Soul cultivation, Restrictions, Formations, Puppets, Alchemy, Refining,
 * Flying swords, Storage treasures, Ancient God, Ancient Dao, Ancient Clan,
 * Ancient Demon, Ancient Devil, Ji Realm, Essences, Domains, Avatars, Clones,
 * Divine Sense, Space, Time, Karma, Life and Death, Samsara, Slaughter,
 * Celestial techniques, Underworld techniques, Origin Energy, Third Step,
 * Fourth Step — and the cross-cutting registries Inventory, Techniques, Pets,
 * Companions, Knowledge, Enemies, Allies, Realms, History.
 *
 * <p>Every {@link CanonicalEntry} belongs to exactly one CanonicalCategory.
 * Sub-registries filter their entries by category to produce their slice of
 * the graph.
 *
 * <h2>Grouping</h2>
 * <p>For convenience, categories are grouped via {@link Group}:
 * <ul>
 *   <li>{@link Group#CULTIVATION_TRACK} — the qi/refining/body/soul foundation</li>
 *   <li>{@link Group#ANCIENT_LINEAGE} — Ancient God / Dao / Clan / Demon / Devil</li>
 *   <li>{@link Group#RESTRICTION_FORMATION} — Wang Lin's signature systems</li>
 *   <li>{@link Group#DAO_ESSENCE} — Essences, Domains, Avatars, Origin Energy</li>
 *   <li>{@link Group#LAWS} — Space, Time, Karma, Life-Death, Samsara, Slaughter, Ji Realm</li>
 *   <li>{@link Group#CELESTIAL_UNDERWORLD} — Celestial & Underworld technique families</li>
 *   <li>{@link Group#TRANSCENDENCE} — Third Step / Fourth Step / Heaven Trampling</li>
 *   <li>{@link Group#ARSENAL} — Inventory, Techniques, Pets, Companions, Knowledge</li>
 *   <li>{@link Group#RELATIONSHIPS} — Enemies, Allies, Realms, History</li>
 * </ul>
 */
public enum CanonicalCategory {
    // ── Cultivation track ────────────────────────────────────────────
    CULTIVATION(Group.CULTIVATION_TRACK, "Cultivation", "Qi-condensation, foundation, core, nascent soul, soul transformation"),
    BODY_CULTIVATION(Group.CULTIVATION_TRACK, "Body cultivation", "Body-tempering tracks: Ancient God body, undying body, Five Elements True Body"),
    SOUL_CULTIVATION(Group.CULTIVATION_TRACK, "Soul cultivation", "Soul-refining, soul-extracting, soul-sealing, Soul-Devourer nature"),
    REFINING(Group.CULTIVATION_TRACK, "Refining", "Treasure-refining arts (Restriction Flag method, soul-flag method, blood-refinement)"),
    ALCHEMY(Group.CULTIVATION_TRACK, "Alchemy", "Pill-craft (Wang Lin learned the basics; not his primary focus)"),

    // ── Ancient lineage ──────────────────────────────────────────────
    ANCIENT_GOD(Group.ANCIENT_LINEAGE, "Ancient God", "Tu Si's Ancient God inheritance; Wang Lin's body-cultivation track 1★→27★"),
    ANCIENT_DAO(Group.ANCIENT_LINEAGE, "Ancient Dao", "Dao Ancient lineage (Ancient Clan sub-lineage; Imperial Capital)"),
    ANCIENT_CLAN(Group.ANCIENT_LINEAGE, "Ancient Clan", "The Ancient Clan (古族); Wang Lin's 13★→27★ Ancient Clan ascension"),
    ANCIENT_DEMON(Group.ANCIENT_LINEAGE, "Ancient Demon", "Bei Lou lineage; Ancient Demon clone (fused into Ancient One)"),
    ANCIENT_DEVIL(Group.ANCIENT_LINEAGE, "Ancient Devil", "Daogu Yemo devil-corpse clone (fused into Ancient One)"),

    // ── Restriction & formation ──────────────────────────────────────
    RESTRICTIONS(Group.RESTRICTION_FORMATION, "Restrictions", "Wang Lin's signature Dao: 4 Great Restrictions, Restriction Flag, Restriction Essence"),
    FORMATIONS(Group.RESTRICTION_FORMATION, "Formations", "Formation-array treasures & self-erected formations (Soul Devil Ship, Unnamed Wheel Formation)"),
    PUPPETS(Group.RESTRICTION_FORMATION, "Puppets", "Celestial Guards, refined corpses, ancient slaves (Emperor Furnace output)"),

    // ── Dao essence ──────────────────────────────────────────────────
    ESSENCES(Group.DAO_ESSENCE, "Essences", "The 14 Essences of Wang Lin's Samsara Dao (6 substantial + 4 virtual + 4 special)"),
    DOMAINS(Group.DAO_ESSENCE, "Domains", "Dao Domains (Life-Death, Karma, True-False, Battle Will, 18-Layer Hell Reincarnation Realm)"),
    AVATARS(Group.DAO_ESSENCE, "Avatars", "Wang Lin's clones (Cultivator Clone, Thunder Body, Void Avatar, Lu Mo, Annihilating Thunder)"),
    CLONES(Group.DAO_ESSENCE, "Clones", "Samsara Incarnation fragments (one billion incarnations)"),
    BODIES(Group.DAO_ESSENCE, "Bodies", "True Bodies condensed from Origins (Five Elements True Body, Taichu/Miemie/Restriction/Thunder Origin True Bodies)"),
    ORIGIN_ENERGY(Group.DAO_ESSENCE, "Origin Energy", "Origin-tier sources (Origin Swords, Heavenly Venerable Sun, Slaughter Crystal)"),

    // ── Laws ─────────────────────────────────────────────────────────
    DIVINE_SENSE(Group.LAWS, "Divine Sense", "Ji Realm, Soul-Piercing Eyes, Eyes Suppressing the World, Samsara Eye"),
    SPACE(Group.LAWS, "Space", "Spatial-bending, teleportation restrictions, pocket dimensions"),
    TIME(Group.LAWS, "Time", "Flowing Time, time-reversal, Heaven-Defying Bead's 10× interior dilation"),
    KARMA(Group.LAWS, "Karma", "Karma Whip, Karma Domain, karmic-thread perception"),
    LIFE_AND_DEATH(Group.LAWS, "Life and Death", "Underworld River, Life-Death Domain, Life-Death Restriction"),
    SAMSARA(Group.LAWS, "Samsara", "Reincarnation Pool, 1-Billion Samsara Incarnations, Reincarnation Essence"),
    SLAUGHTER(Group.LAWS, "Slaughter", "Heart of Slaughter, Slaughter Crystal, Slaughter True Body / Lu Mo"),
    JI_REALM(Group.LAWS, "Ji Realm", "Extreme Realm divine sense — Wang Lin's first killing divine sense (Ch. 127)"),

    // ── Celestial & underworld ───────────────────────────────────────
    CELESTIAL_TECHNIQUES(Group.CELESTIAL_UNDERWORLD, "Celestial techniques", "Celestial-tier spells & arts (Celestial Slaughter Art, Finger of Death trio, Light-Shadow Shield)"),
    UNDERWORLD_TECHNIQUES(Group.CELESTIAL_UNDERWORLD, "Underworld techniques", "Underworld Ascension Method, Yellow Springs Finger, Blue Flames"),

    // ── Transcendence ────────────────────────────────────────────────
    THIRD_STEP(Group.TRANSCENDENCE, "Third Step", "True Immortal / Ancient / Paragon tier"),
    FOURTH_STEP(Group.TRANSCENDENCE, "Fourth Step", "Heaven Trampling — Wang Lin achieves by completing Reincarnation Essence (Ch. 2087)"),

    // ── Arsenal (cross-cutting) ──────────────────────────────────────
    INVENTORY(Group.ARSENAL, "Inventory", "Wang Lin's owned items (cross-refs RICanonicalDatabase.ALL_ARTIFACTS)"),
    TECHNIQUES(Group.ARSENAL, "Techniques", "Wang Lin's learned / created / inherited techniques (cross-refs RICanonicalDatabase.ALL_TECHNIQUES)"),
    PETS(Group.ARSENAL, "Pets", "Wang Lin's tamed beasts (delegates to dev.ergenverse.wanglin.pet)"),
    COMPANIONS(Group.ARSENAL, "Companions", "Devils (Xu Liguo), contracted humanoids, life-bound beasts"),
    KNOWLEDGE(Group.ARSENAL, "Knowledge", "Abstract lore: Dao truths, cosmology, world mechanics Wang Lin attests to"),

    // ── Relationships ────────────────────────────────────────────────
    ENEMIES(Group.RELATIONSHIPS, "Enemies", "Antagonists (All-Seer, Seven-Colored Daoist, Teng Huayuan, Tianyunzi, Daoist Water, Tuo Sen, Gu Dao)"),
    ALLIES(Group.RELATIONSHIPS, "Allies", "Mentors & allies (Situ Nan, Tu Si, Bai Fan, Qing Shui, Dun Tian, Dao Master Blue Dream)"),
    REALMS(Group.RELATIONSHIPS, "Realms", "Cosmology layers (cross-refs WangLinCosmology.Layer)"),
    HISTORY(Group.RELATIONSHIPS, "History", "Wang Lin's life events (cross-refs RITimelineEngine)"),

    // ── Storage treasures and flying swords — explicitly named by user ──
    STORAGE_TREASURES(Group.ARSENAL, "Storage treasures", "Storage Space, Space Stone, Collection Pavilion, Earth Palace, Fate Sealing Ring, Sword Sheaths ×5"),
    FLYING_SWORDS(Group.ARSENAL, "Flying swords", "Wealth, Core-Treasure Sword, Dark Green Flying Sword, God-Slaying Sword, Rain Celestial Sword, Origin Swords");

    /** Convenience grouping for UI / summaries. */
    public enum Group {
        CULTIVATION_TRACK, ANCIENT_LINEAGE, RESTRICTION_FORMATION, DAO_ESSENCE,
        LAWS, CELESTIAL_UNDERWORLD, TRANSCENDENCE, ARSENAL, RELATIONSHIPS
    }

    public final Group group;
    public final String label;
    public final String description;

    CanonicalCategory(Group group, String label, String description) {
        this.group = group;
        this.label = label;
        this.description = description;
    }
}
