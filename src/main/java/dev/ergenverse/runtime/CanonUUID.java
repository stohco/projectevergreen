package dev.ergenverse.runtime;

import java.util.UUID;

/**
 * CanonUUID — permanent identity for everything important in the Er Gen Verse.
 *
 * <p><b>Architectural directive (2026-07-25):</b> "I would actually expand this.
 * Instead of only NPCs, I'd give permanent IDs to: NPCs, Spirit Beasts, Artifacts,
 * Spirit Veins, Herb patches, Buildings, Ancient formations, Teleport arrays,
 * Named caves, Sect halls, Important trees, Ancient battlefields, Storage rings,
 * Flying swords — everything important. That lets memories, rumors, and
 * relationships reference stable identities rather than ephemeral entity IDs."
 *
 * <p>A CanonUUID is derived deterministically from a string key (e.g.
 * "wang_lin", "flying_sword_jagged", "spirit_vein_heng_yue_peak"). This means:
 * <ul>
 *   <li>The same key always produces the same UUID — across saves, across
 *       world regenerations, across playthroughs.</li>
 *   <li>Memories, rumors, relationships, and karmic bonds can reference a
 *       canon UUID without worrying about entity ID changes, chunk unloads,
 *       or save/load cycles.</li>
 *   <li>The Minecraft entity is a <b>materialized view</b> of the canon
 *       UUID — when the chunk unloads, the entity is destroyed, but the
 *       canon UUID continues to exist in the simulation.</li>
 * </ul>
 *
 * <p>Canon UUIDs are NEVER generated randomly. They are ALWAYS derived from
 * a meaningful string key. This is the difference between "Wang Lin exists"
 * (canon UUID, permanent) and "entity #1234 is named Wang Lin" (entity ID,
 * ephemeral).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonUUID {

    private CanonUUID() {}

    /**
     * Derive a deterministic UUID from a canon string key.
     *
     * <p>Uses UUID.nameUUIDFromBytes (type 3 UUID) so the same key always
     * produces the same UUID. This is NOT random — it's a hash.
     *
     * @param key the canon key (e.g. "wang_lin", "spirit_vein_heng_yue_peak")
     * @return the permanent UUID for that key
     */
    public static UUID of(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Canon UUID key cannot be null or empty");
        }
        return UUID.nameUUIDFromBytes(("ergenverse:" + key).getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // ════════════════════════════════════════════════════════════════════
    //  CANON UUID CATEGORIES
    // ════════════════════════════════════════════════════════════════════

    // ── NPCs (canonical characters from the novels) ──
    public static final UUID WANG_LIN = of("npc:wang_lin");
    public static final UUID OLD_CHEN = of("npc:old_chen");
    public static final UUID DA_NIU = of("npc:da_niu");
    public static final UUID LI_MUWAN = of("npc:li_muwan");
    public static final UUID WANG_ZHUO = of("npc:wang_zhuo");
    // CRON-69 canon fact-check: surname is 藤 (vine); the young antagonist is
    // 藤厉 (Teng Li), NOT "Teng Lijun" — there is no "军" in the canon name.
    public static final UUID TENG_HUAYUAN = of("npc:teng_huayuan");
    public static final UUID TENG_LI = of("npc:teng_li");
    public static final UUID SITU_NAN = of("npc:situ_nan");
    public static final UUID WANG_HAO = of("npc:wang_hao");

    // ── Spirit Beasts (named/canonical beasts, not generic spawns) ──
    public static final UUID WOLF_PACK_ALPHA_ZHAO = of("beast:wolf_pack_alpha_zhao");
    public static final UUID SPIRIT_TIGER_ZHAO_MOUNTAINS = of("beast:spirit_tiger_zhao_mountains");
    public static final UUID CRANE_MATRIARCH_HENG_YUE = of("beast:crane_matriarch_heng_yue");
    public static final UUID SEA_SERPENT_SEA_OF_DEVILS = of("beast:sea_serpent_sea_of_devils");
    public static final UUID QILIN_OF_VERMILION_BIRD = of("beast:qilin_of_vermilion_bird");

    // ── Artifacts (canon-named items) ──
    public static final UUID HEAVEN_DEFYING_BEAD = of("artifact:heaven_defying_bead");
    public static final UUID FLYING_SWORD_JAGGED = of("artifact:flying_sword_jagged");
    public static final UUID SOUL_FLAG_WANG_LIN = of("artifact:soul_flag_wang_lin");
    public static final UUID RESTRICTION_BANNER = of("artifact:restriction_banner");
    public static final UUID JADE_SLIP_WANG_LIN = of("artifact:jade_slip_wang_lin");

    // ── Spirit Veins (qi-rich geological formations) ──
    public static final UUID SPIRIT_VEIN_HENG_YUE_PEAK = of("vein:spirit_vein_heng_yue_peak");
    public static final UUID SPIRIT_VEIN_WANG_FAMILY_HILL = of("vein:spirit_vein_wang_family_hill");
    public static final UUID SPIRIT_VEIN_SUZAKU_TOMB = of("vein:spirit_vein_suzaku_tomb");
    public static final UUID SPIRIT_VEIN_SEA_OF_DEVILS_DEEP = of("vein:spirit_vein_sea_of_devils_deep");

    // ── Herb Patches (cultivable herb gardens) ──
    public static final UUID HERB_PATCH_WANG_FAMILY = of("herb:herb_patch_wang_family");
    public static final UUID HERB_PATCH_HENG_YUE_ALCHEMY = of("herb:herb_patch_heng_yue_alchemy");
    public static final UUID HERB_PATCH_FOREST_OF_SENSE = of("herb:herb_patch_forest_of_sense");

    // ── Buildings (named structures) ──
    public static final UUID BUILDING_WANG_LIN_HOUSE = of("building:wang_lin_house");
    public static final UUID BUILDING_HENG_YUE_MAIN_HALL = of("building:heng_yue_main_hall");
    public static final UUID BUILDING_HENG_YUE_SWORD_TOMB = of("building:heng_yue_sword_tomb");
    public static final UUID BUILDING_SUZAKU_TOMB_ENTRANCE = of("building:suzaku_tomb_entrance");

    // ── Ancient Formations (canon restriction arrays) ──
    public static final UUID FORMATION_HENG_YUE_SECT_GUARD = of("formation:heng_yue_sect_guard");
    public static final UUID FORMATION_SUZAKU_TOMB_SEAL = of("formation:suzaku_tomb_seal");
    public static final UUID FORMATION_FOREST_OF_SENSE_DISTORTION = of("formation:forest_of_sense_distortion");

    // ── Teleport Arrays (canon fast-travel nodes) ──
    public static final UUID TELEPORT_HENG_YUE_SECT = of("teleport:heng_yue_sect");
    public static final UUID TELEPORT_VERMILION_BIRD_CAPITAL = of("teleport:vermilion_bird_capital");
    public static final UUID TELEPORT_SEA_OF_DEVILS_EDGE = of("teleport:sea_of_devils_edge");

    // ── Named Caves (cultivation retreats) ──
    public static final UUID CAVE_WANG_LIN_RETREAT = of("cave:wang_lin_retreat");
    public static final UUID CAVE_OLD_CHEN_MEDITATION = of("cave:old_chen_meditation");

    // ── Sect Halls (named rooms within sects) ──
    public static final UUID HALL_HENG_YUE_ANCESTOR = of("hall:heng_yue_ancestor");
    public static final UUID HALL_HENG_YUE_LIBRARY = of("hall:heng_yue_library");
    public static final UUID HALL_HENG_YUE_ALCHEMY = of("hall:heng_yue_alchemy");

    // ── Important Trees (canon-named trees) ──
    public static final UUID TREE_ANCIENT_SPIRIT_WANG_FAMILY = of("tree:ancient_spirit_wang_family");
    public static final UUID TREE_BODHI_XUAN_DAO_SECT = of("tree:bodhi_xuan_dao_sect");

    // ── Ancient Battlefields (canon historical sites) ──
    public static final UUID BATTLEFIELD_SEALING_WAR = of("battlefield:sealing_war");
    public static final UUID BATTLEFIELD_VERMILION_BIRD_ASCENSION = of("battlefield:vermilion_bird_ascension");

    // ── Storage Rings (canon-named storage artifacts) ──
    public static final UUID STORAGE_RING_WANG_LIN = of("storage:wang_lin_storage_ring");
    public static final UUID STORAGE_RING_LI_MUWAN = of("storage:li_muwan_storage_ring");

    /**
     * Check if a UUID is a canon UUID (derived from a string key). This is
     * useful for filtering — canon UUIDs persist across saves; random UUIDs
     * are temporary entity IDs that should not be referenced in memories or
     * relationships.
     */
    public static boolean isCanon(UUID uuid) {
        if (uuid == null) return false;
        // Type 3 UUIDs (name-based) have version bits 0x3000.
        // Canon UUIDs are all type 3.
        return (uuid.version() == 3);
    }
}
