package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PlanetSuzakuBlueprint — the canonical, immutable, hand-authored <i>description</i>
 * of Planet Suzaku (Wang Lin's home cultivation world).
 *
 * <p><b>This is the source of truth.</b> Not the chunk generator. Not the world
 * save. Not the entity data. The blueprint defines what EXISTS; the simulation
 * defines what CHANGES; the save stores only the deltas.
 *
 * <p><b>Canon (CRON-69 fact-checked against the novel / Baidu Baike / Fandom):</b>
 * Planet Suzaku (朱雀星) is a 6th-level cultivation planet in the Kun Xu star
 * domain. The early story takes place on it. Wang Lin (王林) is born in a remote
 * mountain village in Zhao Country (赵国). He joins Heng Yue Sect (恒岳派) on
 * Heng Yue Mountain. The Teng family (藤家 — note 藤, vine, not 滕) is a powerful
 * family in Zhao; its ancestor Teng Huayuan (藤化元) is a Nascent Soul cultivator
 * and early antagonist; the young antagonist is Teng Li (藤厉), not "Teng Lijun".
 * Li Muwan (李慕婉) is from Luo He Sect (洛河门) in Huo Fen Country. Situ Nan
 * (司徒南) is the 2nd-generation Suzaku Son (朱雀子) of Suzaku Country (朱雀国).
 * The Sea of Devils is canonically 修魔海 (Xiu Mo Hai). The Snow Country is
 * 雪域国 (Snow Domain Country). The Suzaku Tomb (朱雀墓) is the underground
 * inheritance site of the Suzaku Son lineage.
 *
 * <h2>The blueprint is a description, not a block array (CRON-69, point 8)</h2>
 * <p>"PlanetSuzakuBlueprint should never answer 'getBlock'. … Instead I'd make
 * the blueprint answer higher-level questions: queryTerrain(...),
 * queryStructures(...), queryActors(...), querySpiritVeins(...). Because the
 * blueprint isn't actually a giant block array. It's a description of the
 * world." So this class exposes <b>query</b> methods that return canon objects
 * (locations, actors, spirit veins) — never a per-block {@code getBlock}. The
 * {@link dev.ergenverse.runtime.layer.BlueprintLayer} and the
 * {@link dev.ergenverse.runtime.materialize.PlanetSuzakuChunkMaterializer}
 * consume these queries; the actual blocks are placed by hand-authored builders.
 *
 * <h2>Canonical geography (fixed coordinates, deterministic seed)</h2>
 * <p>Every location has a fixed canonical coordinate. The deterministic seed
 * ({@link dev.ergenverse.spawn.DeterministicSeedHandler#CANON_SEED}) ensures
 * the noise-generated base terrain is identical every playthrough, so these
 * coordinates always sit in the same geography.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class PlanetSuzakuBlueprint {

    private static final PlanetSuzakuBlueprint CANONICAL = new PlanetSuzakuBlueprint();

    /** Get the canonical, immutable blueprint instance. */
    public static PlanetSuzakuBlueprint canonical() { return CANONICAL; }

    private PlanetSuzakuBlueprint() {}

    // ════════════════════════════════════════════════════════════════════
    //  CANONICAL COORDINATES — fixed, hand-authored, never random
    // ════════════════════════════════════════════════════════════════════

    /**
     * A canonical location on Planet Suzaku. Each location has a permanent
     * {@code id}, a canon {@code name}, fixed coordinates, a {@code category},
     * and a {@code canonReference} citing the novel.
     */
    public static final class CanonLocation {
        public final String id;
        public final String name;
        public final int x, y, z;
        public final String category;
        public final String canonReference;

        public CanonLocation(String id, String name, int x, int y, int z,
                             String category, String canonReference) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.category = category;
            this.canonReference = canonReference;
        }
    }

    // ── Wang Family Village (Wang Lin's birthplace; 赵国某偏僻小山村) ──
    public static final CanonLocation WANG_FAMILY_VILLAGE =
            new CanonLocation("wang_family_village", "Wang Family Village",
                    3842, 0, -1184, "settlement", "RI Ch.1 — Zhao Country remote mountain village (王氏)");

    // ── Heng Yue Sect (恒岳派 on 恒岳山, Zhao Country's largest sect, declined) ──
    public static final CanonLocation HENG_YUE_SECT =
            new CanonLocation("heng_yue_sect", "Heng Yue Sect",
                    4200, 0, -1400, "sect", "RI Ch.3-15 — 恒岳派, on 恒岳山");

    // ── Teng Family City (藤家城 — note 藤, vine) ──
    public static final CanonLocation TENG_FAMILY_CITY =
            new CanonLocation("teng_family_city", "Teng Family City (藤家城)",
                    3500, 0, -900, "settlement", "RI Ch.20-35 — 藤家, Zhao Country");

    // ── Tian Shui City (天水城 — largest city in northern Zhao Country) ──
    public static final CanonLocation TIAN_SHUI_CITY =
            new CanonLocation("tian_shui_city", "Tian Shui City (天水城)",
                    2600, 0, -2000, "settlement", "RI Ch.50-65 — northern Zhao Country military city");

    // ── Qilin City (麒麟城 — a 修魔海 beast-city) ──
    public static final CanonLocation QILIN_CITY =
            new CanonLocation("qilin_city", "Qilin City (麒麟城)",
                    1800, 0, -2600, "settlement", "RI 修魔海 arc — 麒麟兽城");

    // ── Nan Dou City (南斗城 — a 修魔海 beast-city on an ancient beast) ──
    public static final CanonLocation NAN_DOU_CITY =
            new CanonLocation("nan_dou_city", "Nan Dou City (南斗城)",
                    4400, 0, -2400, "settlement", "RI 修魔海 arc — beast-city");

    // ── Snow Domain Capital (雪域国 — Snow Domain Country) ──
    public static final CanonLocation SNOW_DOMAIN_CAPITAL =
            new CanonLocation("snow_domain_capital", "Snow Domain Capital (雪域国)",
                    2000, 0, 3200, "settlement", "RI Ch.200-220 — 雪域国, home of genius 红蝶");

    // ── Vermilion Bird Capital (朱雀国 — seat of the 朱雀子 / Suzaku Son) ──
    public static final CanonLocation VERMILION_BIRD_CAPITAL =
            new CanonLocation("vermilion_bird_capital", "Vermilion Bird Capital (朱雀国)",
                    0, 0, 0, "settlement", "RI Ch.300-320 — 朱雀国, seat of the 朱雀子; mod infers a capital city");

    // ── Soul Refining Sect (炼魂宗 — patriarch 遁天 / Dun Tian) ──
    public static final CanonLocation SOUL_REFINING_SECT =
            new CanonLocation("soul_refining_sect", "Soul Refining Sect (炼魂宗)",
                    -1600, 0, -1800, "sect", "RI Ch.250-270 — 炼魂宗, treasure 十亿尊魂幡");

    // ── Xuan Dao Sect (玄道宗 — patriarch 朴南子 destroys Heng Yue) ──
    public static final CanonLocation XUAN_DAO_SECT =
            new CanonLocation("xuan_dao_sect", "Xuan Dao Sect (玄道宗)",
                    -2400, 0, 1400, "sect", "RI Ch.280-300 — 玄道宗");

    // ── Luo He Sect (洛河门 — Li Muwan's original sect, in 火焚国) ──
    public static final CanonLocation LUO_HE_SECT =
            new CanonLocation("luo_he_sect", "Luo He Sect (洛河门)",
                    3000, 0, 2400, "sect", "RI — 洛河门, 火焚国; Li Muwan's sect");

    // ── Four Sects Alliance (四派联盟 — site of Wang Lin's 化凡 mortal-life arc, 曾大牛's home) ──
    public static final CanonLocation FOUR_SECTS_ALLIANCE =
            new CanonLocation("four_sects_alliance", "Four Sects Alliance (四派联盟)",
                    1000, 0, 1600, "region", "RI 化凡 arc — 四派联盟; home of 曾大牛 / 曾小牛");

    // ── Geographic features ──
    // Sea of Devils = 修魔海 (Xiu Mo Hai), the perilous demon-cultivation sea.
    public static final CanonLocation SEA_OF_DEVILS =
            new CanonLocation("sea_of_devils", "Sea of Devils (修魔海)",
                    6000, 0, -1184, "geographic", "RI — 修魔海; 古神之地, 蚊兽, 十亿尊魂幡 events");

    // Jue Ming Valley = 决明谷 (Jue Ming Gu) — Valley of Decisive Brightness.
    // Canon: Wang Lin flees here to await Teng Huayuan's revenge; his physical body is
    // destroyed here and his soul flees to the Foreign Battleground (域外战场) via the
    // 天逆珠. A trapping formation holds cultivators inside until the sect competition
    // for tokens ends. Wang Lin's Ji Realm (极境) awakening occurs here, driven by grief
    // over his clan's annihilation. Verified via Baidu Baike 仙逆编年史 + multiple chapter
    // summaries (Douban 仙逆王林编年史, Zhihu 仙逆故事线整理, Baidu Baike 仙逆).
    //
    // CRON-COMPLETIONIST-79: Canon name verification. The correct Chinese name is
    // 决明谷 (jué míng gǔ, "decisive brightness valley"). An earlier round (CRON-68)
    // incorrectly noted that "the novel uses 决明 in some sources and 绝命 in others"
    // — this was WRONG. A web search on 2026-07-26 confirmed that ALL primary sources
    // (Baidu Baike 仙逆编年史, Baidu Baike 仙逆, Douban, Zhihu) consistently use 决明谷.
    // NO source uses 绝命谷 (jué mìng gǔ, "certain death valley"). The 绝命 character
    // was a misreading/typo that propagated through the enriched canon DB and multiple
    // code comments. CRON-79 unifies ALL occurrences project-wide to 决明谷. The English
    // translation "Valley of Certain Death" is retained as the common English rendering
    // (it captures the valley's narrative role even though the literal translation of
    // 决明 is "decisive brightness", not "certain death").
    public static final CanonLocation JUE_MING_VALLEY =
            new CanonLocation("jue_ming_valley", "Jue Ming Valley (决明谷)",
                    4500, 0, -500, "dangerous_region",
                    "RI — 决明谷; Wang Lin's physical body destroyed here, "
                    + "Ji Realm awakening, Teng Huayuan confrontation, soul flees to 域外战场");

    // Suzaku Tomb (朱雀墓) — underground inheritance site of the 朱雀子 lineage.
    public static final CanonLocation SUZAKU_TOMB =
            new CanonLocation("suzaku_tomb", "Suzaku Tomb (朱雀墓)",
                    0, -60, 0, "ruin", "RI — 朱雀墓, underground; 15th-gen 朱雀子 inheritance, 拓森 reappears");

    // ── Kunxu Realm (昆虚界) — pocket-realm where Mu Bingmei takes Zhou Ru as disciple ──
    // CRON-COMPLETIONIST-111: Canon (web-search verified 2026-07-26, RICanonicalDatabase
    // L74 + N19): 木冰眉 (Mu Bingmei) enters the Kunxu Realm and takes 周茹 (Zhou Ru)
    // as her disciple. 周茹's cultivation arc (mortal → Soul Transformation) takes
    // place here under Mu Bingmei's guidance.
    //
    // The Kunxu Realm is canonically a pocket-realm accessible from the Cave World,
    // not a surface location on Planet Suzaku. This mod-original concrete placement
    // (far northwest, remote) reflects its "secret realm" nature — far from all
    // other locations. The closest is Soul Refining Sect (-1600, -1800), which is
    // ~2000 blocks away. A future CRON could implement the Kunxu Realm as a
    // separate dimension (like the bead interior), but for now it's a remote
    // overworld region so Mu Bingmei can materialize there and 周茹 can travel
    // there for cultivation.
    //
    // Canon sources: RICanonicalDatabase L74 (Kunxu Realm), N19 (Mu Bingmei),
    // N10 (Zhou Ru). NO fabricated chapter citation.
    public static final CanonLocation KUNXU_REALM =
            new CanonLocation("kunxu_realm", "Kunxu Realm (昆虚界)",
                    -3500, 0, -3500, "secret_realm",
                    "RI — 昆虚界; Mu Bingmei's cultivation retreat; Zhou Ru's disciple arc");

    // ── Canonical NPC id strings (the UUID lives in CanonUUID) ──
    public static final String NPC_WANG_LIN = "wang_lin";
    public static final String NPC_OLD_CHEN = "old_chen";
    public static final String NPC_DA_NIU = "zeng_da_niu";   // 曾大牛 — surname 曾
    public static final String NPC_LI_MUWAN = "li_muwan";
    public static final String NPC_WANG_ZHUO = "wang_zhuo";
    public static final String NPC_TENG_HUAYUAN = "teng_huayuan";  // 藤化元
    public static final String NPC_TENG_LI = "teng_li";             // 藤厉 (not "Teng Lijun")
    public static final String NPC_SITU_NAN = "situ_nan";
    public static final String NPC_WANG_HAO = "wang_hao";
    // CRON-COMPLETIONIST-107: 拓森 (Tuo Sen) — Ancient God rival who reappears
    // at the Suzaku Tomb during the 15th-gen Suzaku Son inheritance event.
    // Canon: web-search verified 2026-07-26 (Sohu, 163, Tencent sources).
    // Spawns via TuoSenSpawnEvent when CultivationPlanetCrystalBlock.use()
    // triggers the inheritance (CRON-106). Until then, deadUntilRevived=true.
    public static final String NPC_TUO_SEN = "tuo_sen";  // 拓森 (NOT 拓山)

    // CRON-COMPLETIONIST-110: 周茹 (Zhou Ru) — the mortal vessel for Li
    // Muwan's reincarnation. The soul transfer (ZhouRuSoulTransferEvent)
    // fires when the player right-clicks this NPC while holding the
    // Heaven-Defying Bead with Li Muwan's soul captured (CRON-99).
    // Canon: web-search verified 2026-07-26 (Baidu Baike, Fandom wiki).
    // After the transfer she eventually becomes a Soul Transformation
    // cultivator under 慕冰梅 (Mu Bingmei) in 昆墟之境 (Kunxu Realm).
    public static final String NPC_ZHOU_RU = "zhou_ru";  // 周茹

    // CRON-COMPLETIONIST-111: 木冰眉 (Mu Bingmei, also known as 柳眉 Liu Mei)
    // — Wang Lin's third wife and Zhou Ru's cultivation master. Canon
    // (web-search verified 2026-07-26, RICanonicalDatabase N19 + L74):
    //   - Mu Bingmei is Liu Mei's true form (木冰眉 / 柳眉).
    //   - Ascendant+ cultivation; Wang Lin's third wife.
    //   - Had a son with Wang Lin (Wang Ping) whom she refined into a
    //     resentful spirit out of hatred.
    //   - Wang Lin severed karmic ties with her via the Dream Dao; one of
    //     his clones accompanies her.
    //   - She entered the Kunxu Realm (昆虚界) and took Zhou Ru as her disciple.
    // The ZhouRuCultivationGrowthService (CRON-111) advances Zhou Ru's realm
    // when she is near Mu Bingmei — modeling the disciple-master cultivation.
    public static final String NPC_MU_BINGMEI = "mu_bingmei";  // 木冰眉 / 柳眉

    // CRON-COMPLETIONIST-116: 王平 (Wang Ping) — Wang Lin's biological son by
    // 木冰眉 / 柳眉 (Mu Bingmei's 9th avatar Liu Mei). Canon (web-search verified
    // 2026-07-27, Baidu Baike + Fandom wiki + newhanfu + Toutiao + 163):
    //   - Conceived in the 朱雀墓 (Suzaku Tomb) from an accidental union.
    //   - Refined into a 怨婴 (resentment infant) by Liu Mei for ~100 years.
    //   - Redeemed by Wang Lin: rebuilt a body from sword qi (剑气); lived a
    //     mortal life (~73 years) with Wang Lin during the 二次化凡 arc.
    //   - At ~72-73, voluntarily dispersed his sword-qi body; 残魂 sealed into
    //     the 天逆珠 by Wang Lin.
    //   - First appears Vol 7 Ch 680 《柳眉的特殊法宝》 as "厉儿 (Li'er)";
    //     named 王平 in Vol 7 Ch 681. (Safe citations per Baidu Baike.)
    // At story start, Wang Ping is canonically a 怨婴 (resentment infant) —
    // flagged deadUntilRevived=true. Mod-original placement: Suzaku Tomb
    // (his conception site). A future redemption event would clear the flag.
    public static final String NPC_WANG_PING = "wang_ping";  // 王平

    /**
     * All canonical locations, indexed by id. Immutable. Built fresh each call
     * (the set is tiny — 15 entries — so this is negligible).
     */
    public Map<String, CanonLocation> allLocations() {
        Map<String, CanonLocation> map = new LinkedHashMap<>();
        map.put(WANG_FAMILY_VILLAGE.id, WANG_FAMILY_VILLAGE);
        map.put(HENG_YUE_SECT.id, HENG_YUE_SECT);
        map.put(TENG_FAMILY_CITY.id, TENG_FAMILY_CITY);
        map.put(TIAN_SHUI_CITY.id, TIAN_SHUI_CITY);
        map.put(QILIN_CITY.id, QILIN_CITY);
        map.put(NAN_DOU_CITY.id, NAN_DOU_CITY);
        map.put(SNOW_DOMAIN_CAPITAL.id, SNOW_DOMAIN_CAPITAL);
        map.put(VERMILION_BIRD_CAPITAL.id, VERMILION_BIRD_CAPITAL);
        map.put(SOUL_REFINING_SECT.id, SOUL_REFINING_SECT);
        map.put(XUAN_DAO_SECT.id, XUAN_DAO_SECT);
        map.put(LUO_HE_SECT.id, LUO_HE_SECT);
        map.put(FOUR_SECTS_ALLIANCE.id, FOUR_SECTS_ALLIANCE);
        map.put(SEA_OF_DEVILS.id, SEA_OF_DEVILS);
        map.put(JUE_MING_VALLEY.id, JUE_MING_VALLEY);
        map.put(SUZAKU_TOMB.id, SUZAKU_TOMB);
        map.put(KUNXU_REALM.id, KUNXU_REALM);
        return Collections.unmodifiableMap(map);
    }

    // ════════════════════════════════════════════════════════════════════
    //  QUERY API — the blueprint answers higher-level questions, never getBlock
    //  (CRON-69, point 8)
    // ════════════════════════════════════════════════════════════════════

    /**
     * All canon locations whose footprint intersects the given block rectangle.
     * Used by the {@link dev.ergenverse.runtime.layer.BlueprintLayer} to answer
     * "what structures intersect this chunk?" without iterating per-block.
     */
    public List<CanonLocation> queryStructures(int minX, int minZ, int maxX, int maxZ) {
        List<CanonLocation> out = new ArrayList<>();
        for (CanonLocation loc : allLocations().values()) {
            int half = 50;
            if (loc.x - half <= maxX && loc.x + half >= minX
                    && loc.z - half <= maxZ && loc.z + half >= minZ) {
                out.add(loc);
            }
        }
        return out;
    }

    /** All canon locations of a given category (e.g. "sect", "settlement", "ruin"). */
    public List<CanonLocation> queryByCategory(String category) {
        List<CanonLocation> out = new ArrayList<>();
        for (CanonLocation loc : allLocations().values()) {
            if (loc.category.equals(category)) out.add(loc);
        }
        return out;
    }

    /**
     * Canon actors (NPCs) anchored at a location intersecting the rectangle.
     * The actor's full state lives in {@link NPCRuntime}; this returns the
     * anchoring location ids. Used by the materializer to decide which actors
     * to materialize when a chunk loads.
     */
    public List<String> queryActors(int minX, int minZ, int maxX, int maxZ) {
        // The actor→anchor mapping is owned by NPCRuntime (registered on loadAll).
        // The blueprint only owns locations; this query is a thin pass-through
        // kept on the blueprint so callers have one place to ask "what's here?".
        List<String> out = new ArrayList<>();
        for (CanonLocation loc : queryStructures(minX, minZ, maxX, maxZ)) {
            out.add(loc.id);
        }
        return out;
    }

    /**
     * Canon spirit veins intersecting the rectangle. Today the spirit-vein
     * registry lives in {@link CanonUUID} (vein ids); the geographic placement
     * is co-located with the hosting sect/location. This returns the hosting
     * locations so the materializer can place vein blocks.
     */
    public List<CanonLocation> querySpiritVeins(int minX, int minZ, int maxX, int maxZ) {
        // Spirit veins are co-located with their hosting sect/landmark for now.
        List<CanonLocation> out = new ArrayList<>();
        for (CanonLocation loc : queryStructures(minX, minZ, maxX, maxZ)) {
            if (loc.category.equals("sect") || loc.id.equals("suzaku_tomb")
                    || loc.id.equals("wang_family_village")) {
                out.add(loc);
            }
        }
        return out;
    }

    /** Canon landmarks (ruins, geographic features) intersecting the rectangle. */
    public List<CanonLocation> queryLandmarks(int minX, int minZ, int maxX, int maxZ) {
        List<CanonLocation> out = new ArrayList<>();
        for (CanonLocation loc : queryStructures(minX, minZ, maxX, maxZ)) {
            if (loc.category.equals("ruin") || loc.category.equals("geographic") || loc.category.equals("region")) {
                out.add(loc);
            }
        }
        return out;
    }

    /**
     * Canon terrain constraints for a region (placeholder for future
     * deterministic-procedural decoration, point 9). Today returns the
     * intersecting geographic landmarks; a future expansion will carry climate,
     * elevation, spirit-density, and tree-species constraints per region.
     */
    public List<CanonLocation> queryTerrain(int minX, int minZ, int maxX, int maxZ) {
        return queryLandmarks(minX, minZ, maxX, maxZ);
    }

    // ════════════════════════════════════════════════════════════════════
    //  VALIDATION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Validate the blueprint's internal consistency. Called on initialize().
     */
    public void validate() {
        Map<String, CanonLocation> locs = allLocations();
        if (locs.isEmpty()) {
            throw new IllegalStateException("Blueprint has no locations");
        }
        for (CanonLocation a : locs.values()) {
            for (CanonLocation b : locs.values()) {
                if (a == b) continue;
                if (a.category.equals("settlement") && b.category.equals("settlement")) {
                    double dist = Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2));
                    if (dist < 500.0) {
                        Ergenverse.LOGGER.warn(
                                "[Ergenverse] Blueprint warning: settlements {} and {} are only {} blocks apart (< 500).",
                                a.id, b.id, (int) dist);
                    }
                }
            }
        }
        Ergenverse.LOGGER.info("[Ergenverse] PlanetSuzakuBlueprint validated: {} canonical locations.", locs.size());
    }

    // NOTE: there is deliberately NO getBlock(int,int,int) method on the
    // blueprint. Per CRON-69 point 8, the blueprint is a description, not a
    // block array. Per-block canon is materialized by hand-authored builders
    // invoked through the ChunkMaterializer, never by a per-block lookup here.
}
