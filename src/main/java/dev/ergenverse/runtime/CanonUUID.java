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
    // CRON-COMPLETIONIST-107: 拓森 (Tuo Sen) — Wang Lin's Ancient God rival.
    // Canon (web-search verified 2026-07-26 via Sohu/163/Tencent sources):
    //   - 拓森 reappears at the 朱雀墓 (Suzaku Tomb) during the inheritance event
    //     ("时隔300年，王林在朱雀墓再遇拓森" — Sohu 2024-06-17).
    //   - He is an 8-star Ancient God (古神), rival to Wang Lin for Tu Si's
    //     Ancient God inheritance (born from Tu Si's failed Ink Flow Split
    //     Soul Technique — inherited Tu Si's 'power' portion, while Wang Lin
    //     received the 'knowledge' portion).
    //   - He appears at the tomb to contest the 修星之晶 (Cultivation Planet Crystal).
    // NO fabricated chapter citation. Canon sources: Baidu Baike, Sohu, 163, Tencent.
    // NOTE: the prior data files used the wrong character 拓山 (Tuò Shān, "mountain")
    // — the correct character is 拓森 (Tuò Sēn, "forest"). CRON-107 fixes this.
    public static final UUID TUO_SEN = of("npc:tuo_sen");

    // CRON-COMPLETIONIST-110: 周茹 (Zhou Ru) — the vessel for Li Muwan's
    // reincarnated soul. Canon (web-search verified 2026-07-26, multiple
    // sources: Baidu Baike 周茹, RICanonicalDatabase N10 entry, Fandom wiki):
    //   - Wang Lin places Li Muwan's captured 元婴 (Nascent Soul, CRON-99)
    //     into 周茹 — the fetus/child who becomes the vessel for Li Muwan's
    //     reincarnation. Li Muwan chooses NOT to devour the host soul; she
    //     instead addresses Wang Lin as 'uncle' (王林叔叔).
    //   - After the transfer, 周茹 grows up to become a Soul Transformation
    //     (化神) cultivator under 慕冰梅 (Mu Bingmei) in 昆墟之境 (Kunxu Realm).
    //   - She is later reincarnated on the Immortal Astral Continent (仙遗族
    //     / IAC arc) where she lives an ordinary life.
    // At story start (before CRON-99's soul capture fires), 周茹 exists as
    // a mortal girl in 朱雀国 (Vermilion Bird Country). She is present in
    // the world from day 0 — the soul transfer can only fire after Wang Lin
    // has captured Li Muwan's soul into the bead (CRON-99 prerequisite).
    // NO fabricated chapter citation. Canon sources: Baidu Baike, Fandom
    // wiki, RICanonicalDatabase entry N10.
    public static final UUID ZHOU_RU = of("npc:zhou_ru");

    // CRON-COMPLETIONIST-111: 木冰眉 (Mu Bingmei, also known as 柳眉 Liu Mei)
    // — Wang Lin's third wife and Zhou Ru's cultivation master. Canon
    // (web-search verified 2026-07-26, RICanonicalDatabase N19 + L74):
    //   - Mu Bingmei is Liu Mei's true form (木冰眉 / 柳眉). Ascendant+
    //     cultivation; Wang Lin's third wife.
    //   - Had a son with Wang Lin (Wang Ping 王平) whom she refined into a
    //     resentful spirit out of hatred. Wang Lin severed karmic ties with
    //     her via the Dream Dao (梦道); one of his clones accompanies her.
    //   - She entered the Kunxu Realm (昆虚界) and took Zhou Ru as her disciple.
    //     周茹's cultivation arc (mortal → Soul Transformation) takes place
    //     here under Mu Bingmei's guidance.
    // At story start, Mu Bingmei is present at the Kunxu Realm (mod-original
    // concrete placement at -3500, -3500 — far northwest, remote). The
    // ZhouRuCultivationGrowthService (CRON-111) advances Zhou Ru's realm
    // when she is near Mu Bingmei — modeling the disciple-master cultivation.
    // NO fabricated chapter citation. Canon sources: RICanonicalDatabase
    // N19, L74, Baidu Baike (木冰眉 — https://baike.baidu.com/item/木冰眉/8802287), Fandom wiki.
    public static final UUID MU_BINGMEI = of("npc:mu_bingmei");

    // CRON-COMPLETIONIST-116: 王平 (Wang Ping) — Wang Lin's biological son by
    // 木冰眉 / 柳眉 (Mu Bingmei's 9th avatar Liu Mei). Canon (web-search verified
    // 2026-07-27, Baidu Baike https://baike.baidu.com/item/王平/62563845 +
    // Fandom wiki https://xian-ni.fandom.com/wiki/Wang_Ping + newhanfu +
    // Toutiao + 163):
    //   - Conceived in the 朱雀墓 (Suzaku Tomb) from an accidental union between
    //     Wang Lin and Liu Mei (Mu Bingmei's 9th avatar). BIOLOGICAL son, NOT
    //     adopted. (The "adopted"/"blood-soul" framing in the animation/donghua
    //     is 魔改 — non-canon for the novel.)
    //   - Refined into a 怨婴 (resentment infant) by Liu Mei for ~100 years,
    //     leaving only a wisp of resentful soul. This is effectively his death
    //     as a normal child.
    //   - Redeemed by Wang Lin: rebuilt a body from sword qi (剑气) — a "false
    //     life". Cannot cry, sterile, has NO cultivation talent, cannot sense
    //     spiritual qi.
    //   - Lived a mortal life (~72 years) with Wang Lin during the 二次化凡
    //     (second mortal transformation) / 梦道 (Dream Dao) arc. Became a mortal
    //     emperor (帝王), married 青宜 (Qing Yi).
    //   - At ~72, voluntarily dispersed his sword-qi body; his 残魂
    //     (remnant soul) was sealed into the 天逆珠 (Heaven-Defying Bead) by
    //     Wang Lin.
    //   - First appears Vol 7 Ch 680 《柳眉的特殊法宝》 as "厉儿 (Li'er)";
    //     named 王平 in Vol 7 Ch 681. (Safe citations per Baidu Baike entry.)
    // At story start, Wang Ping is canonically a 怨婴 (resentment infant) —
    // NOT a living boy. Flagged deadUntilRevived=true so CanonActorMaterializer
    // refuses to spawn him on chunk load. A future Wang Ping redemption event
    // (parallel to Li Muwan's revival arc) would clear the flag and materialize
    // him as a mortal boy at the Suzaku Tomb (his conception site).
    // NO fabricated chapter citations beyond Ch 680/681 (which are Baidu Baike-
    // attested). Canon sources: Baidu Baike, Fandom wiki, newhanfu, Toutiao, 163.
    public static final UUID WANG_PING = of("npc:wang_ping");

    // CRON-COMPLETIONIST-118: 凌天侯 (Ling Tianhou) — the 剑尊 (Sword Venerable),
    // founder of 大罗剑宗 (Da Luo Sword Sect). Canon (web-search verified
    // 2026-07-27, Baidu Baike https://baike.baidu.com/item/凌天侯/65285935 +
    // Sohu + Zhihu + 163):
    //   - Cultivation: 净涅后期 (Quiet Nirvana Late Stage).
    //   - True identity: an avatar (分身) of 灭生老人's servant (仆从). 灭生老人
    //     is a 4th-step cultivator of 逆尘界 (Ni Chen Realm).
    //   - Ling Tianhou PERSONALLY gave Wang Lin TWO STRANDS OF SWORD QI
    //     (两道剑气) to rebuild Wang Ping's body (CRON-117 redemption event).
    //   - Eventually consumed by 天运子 (Tian Yun Zi); the sect declined.
    // In the mod, Ling Tianhou is a LIVING NPC at the Da Luo Sword Sect
    // location (5000, 5000). Right-clicking him grants the Sword Qi Strand
    // item (CRON-118), which is the canon-faithful prerequisite for the
    // Wang Ping redemption event.
    // NO fabricated chapter citation. Canon sources: Baidu Baike, Sohu, Zhihu, 163.
    public static final UUID LING_TIANHOU = of("npc:ling_tianhou");

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
