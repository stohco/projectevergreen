package dev.ergenverse.cultivation;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.perception.PerceptionTier;

/**
 * The unified cultivation realm ladder of the Er Gen multiverse.
 *
 * <p>17 stages across the First, Transitional, Second, and Immortal+ Steps.
 * Each realm has: a lifespan, a perception tier, a canon confidence, and
 * an absolute tier (used in suppression calculations).
 *
 * <p>Cultivators don't level via XP. They have a Cultivation State that
 * changes through events (breakthroughs, setbacks, insights, heart demons).
 *
 * <h2>CRON-COMPLETIONIST-119 — CANON REALM-NAME CORRECTION</h2>
 *
 * <p>The CRON-117/118 self-critique flagged that {@code ASCENDANT} was mapped
 * to the Chinese name {@code "合体"} (HeTi / Body Integration). <b>合体 is NOT
 * a realm in 仙逆 (Renegade Immortal) by 耳根 (Er Gen) — it is a realm in
 * 凡人修仙传 (A Record of a Mortal's Journey to Immortality) by 忘语 (Wang Yu).</b>
 * A web-search subagent (CRON-119-research) verified the canon ladder across
 * 6+ independent sources (Zhihu, Qidian, Sohu, Baidu Baike, Bilibili, Threads,
 * Geocities). All sources agree on the canon 仙逆 First Step ladder:
 *
 * <pre>
 *   凝气 → 筑基 → 结丹 → 元婴 → 化神 → 婴变 → 问鼎
 * </pre>
 *
 * <p>The CRON-119 audit found <b>FOUR canon errors</b> in the prior RealmId
 * mapping, all of which are corrected in this round:
 *
 * <table border="1">
 *   <caption>CRON-119 Realm-Name Corrections</caption>
 *   <tr><th>order</th><th>enum constant</th><th>prior nameCn</th><th>corrected nameCn</th><th>prior English name</th><th>corrected English name</th><th>canon status</th></tr>
 *   <tr><td>6</td><td>SOUL_TRANSFORMATION</td><td>炼虚</td><td>婴变</td><td>Soul Transformation</td><td>Infant Transformation</td><td>炼虚 is from 凡人修仙传, NOT 仙逆. 婴变 is the canon 6th First Step realm.</td></tr>
 *   <tr><td>7</td><td>ASCENDANT</td><td>合体</td><td>问鼎</td><td>Ascendant</td><td>Ascendant (unchanged)</td><td>合体 is from 凡人修仙传, NOT 仙逆. 问鼎 is the canon 7th First Step realm.</td></tr>
 *   <tr><td>8</td><td>ILLUSORY_YIN</td><td>婴变</td><td>阴虚</td><td>Illusory Yin</td><td>Illusory Yin (unchanged)</td><td>婴变 is the canon 6th First Step realm, NOT a transitional realm. The canon 1st transitional realm is 阴虚 (Yin Deficiency).</td></tr>
 *   <tr><td>9</td><td>CORPOREAL_YANG</td><td>洞玄</td><td>阳实</td><td>Corporeal Yang</td><td>Corporeal Yang (unchanged)</td><td>洞玄 does NOT exist in 仙逆. The canon 2nd transitional realm is 阳实 (Yang Substantiality).</td></tr>
 * </table>
 *
 * <p><b>Canon sources (CRON-119-research web-search subagent):</b>
 * <ul>
 *   <li>Zhihu: https://zhuanlan.zhihu.com/p/623337176 — "《仙逆》第一步：凝气—筑基—结丹—元婴—化神—婴变—问鼎"</li>
 *   <li>Qidian: https://m.qidian.com/ask/qamrhrhmiqb — "第一步包括凝气、筑基、结丹、元婴、化神、婴变、问鼎7个境界"</li>
 *   <li>Sohu: https://www.sohu.com/a/902911512_120352010 — "仙逆：什么是问鼎境界？王林如何突破问鼎的？"</li>
 *   <li>ali213.net: https://gl.ali213.net/html/2024-8/1473347.html — "仙逆境界等级划分为凝气期、筑基期、结丹期、元婴期、化神期、婴变期、问鼎期"</li>
 *   <li>Geocities: http://www.geocities.ws/lyj/xiaoshuo/liebiao/xn.html — "修真第一步：凝气、筑基、结丹、元婴、化神、婴变、问鼎"</li>
 *   <li>Zhihu (transitional): "过渡期：阴虚—阳实"</li>
 * </ul>
 *
 * <p><b>No fabricated chapter citations.</b> The web-search subagent found
 * NO explicit chapter numbers for Wang Lin's breakthroughs to 问鼎, 婴变,
 * 窥涅, 净涅, 碎涅 in any of the sources. All sources are encyclopedia
 * entries, fan analysis articles, or social media posts — none contained
 * specific chapter references. We do NOT invent chapter citations.
 *
 * <p><b>Persistence safety:</b> RealmId is persisted in player NBT via
 * {@code tag.putInt(TAG_REALM, currentRealm.order)} (see CultivationState
 * line 667). The {@code order} integer is UNCHANGED for all 18 realms —
 * only the display names (name, nameCn) are corrected. Old saves will
 * deserialize correctly: a player at order=7 will now display as "问鼎 /
 * Ascendant" instead of "合体 / Ascendant", but the underlying realm tier
 * and all mechanics are unchanged.
 *
 * <p><b>Downstream consistency:</b> The English name "Soul Transformation"
 * is changed to "Infant Transformation" to match the corrected nameCn "婴变".
 * All downstream string references to "Soul Transformation" are updated:
 * WorldLaws.java, ItemEvolutionChain.java, VoidBeastEncounter.java,
 * RICanonicalDatabase.java, RICivilizationEngine.java, RIEcologyEngine.java,
 * ri_canon_database.json, npc_xu_liqing.json, npc_zhou_ru.json,
 * npc_zhao_xingsha.json, and Javadoc references in MuBingmeiAcceptanceEvent
 * and ZhouRuCultivationGrowthService.
 *
 * <p><b>Wang Ping redemption impact:</b> The Wang Ping redemption event
 * (CultivationPlanetCrystalBlock.use) requires {@code RealmId.ASCENDANT}
 * (order 7). With the canon correction, ASCENDANT is now correctly labeled
 * "问鼎" (WenDing / Ascendant). This matches the canon: Wang Lin rebuilds
 * Wang Ping's body at 问鼎中期 (Ascendant middle stage) per CRON-117/118
 * research. The prerequisite logic was ALREADY correct — only the display
 * name was wrong. No change to the prerequisite is needed.
 *
 * <p><b>Known remaining discrepancies (deferred to future CRONs):</b>
 * <ul>
 *   <li><b>SPIRIT_SEIZER (夺舍 / DuoShe, order 13):</b> The web-search
 *       subagent found that 夺舍 is a body-snatching TECHNIQUE in 仙逆,
 *       not a separate cultivation realm. However, the mod treats it as
 *       a realm between 碎涅 (Nirvana Fruit) and 真仙 (True Immortal).
 *       This is a STRUCTURAL discrepancy that requires more research
 *       (some interpretations of 仙逆 do treat 夺舍 as a realm-like
 *       state, especially for Wang Lin who used 夺舍 to possess a mortal
 *       body). Deferred to a future CRON.</li>
 *   <li><b>Third Step structure (orders 14-17):</b> The web-search
 *       subagent found that the canon Third Step is 空之四境 (Four Realms
 *       of Emptiness): 空涅, 空灵, 空玄, 空劫. The mod has 真仙, 古境,
 *       大天尊, 超脱 instead. This is a STRUCTURAL discrepancy that
 *       requires a major enum restructuring. Deferred to a future CRON.</li>
 *   <li><b>Fourth Step (踏天境 / Stepping Heaven):</b> The mod's
 *       TRANSCENDENCE (超脱) corresponds to the canon 踏天境 (Stepping
 *       Heaven Realm). The canon has 踏天九桥 (Nine Bridges of Stepping
 *       Heaven) as a sub-structure. Deferred to a future CRON.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public enum RealmId {
    // ── First Step (mortal → ascendan) ──
    // CRON-119: nameCn corrections applied (炼虚→婴变, 合体→问鼎).
    // See class Javadoc for canon sources.
    MORTAL           (0, 0, "Mortal",              "凡人",       100,         PerceptionTier.MORTAL,         CanonEngine.Confidence.NOVEL_STATEMENT),
    QI_CONDENSATION  (1, 0, "Qi Condensation",     "练气",       200,         PerceptionTier.QI_CONDENSATION, CanonEngine.Confidence.NOVEL_STATEMENT),
    FOUNDATION       (2, 0, "Foundation",          "筑基",       500,         PerceptionTier.FOUNDATION,      CanonEngine.Confidence.NOVEL_STATEMENT),
    CORE_FORMATION   (3, 0, "Core Formation",      "结丹",       1000,        PerceptionTier.FOUNDATION,      CanonEngine.Confidence.NOVEL_STATEMENT),
    NASCENT_SOUL     (4, 0, "Nascent Soul",        "元婴",       2000,        PerceptionTier.NASCENT_SOUL,    CanonEngine.Confidence.NOVEL_STATEMENT),
    SOUL_FORMATION   (5, 0, "Soul Formation",      "化神",       3000,        PerceptionTier.SOUL_FORMATION,  CanonEngine.Confidence.NOVEL_STATEMENT),
    // CRON-119: 炼虚 → 婴变 (Infant Transformation). 炼虚 is from 凡人修仙传, NOT 仙逆.
    // 婴变 is the canon 6th First Step realm per Zhihu, Qidian, Sohu, Baidu Baike, Bilibili.
    // English name "Soul Transformation" → "Infant Transformation" to match 婴变.
    SOUL_TRANSFORMATION(6, 0, "Infant Transformation","婴变",    5000,        PerceptionTier.SOUL_FORMATION,  CanonEngine.Confidence.NOVEL_STATEMENT),
    // CRON-119: 合体 → 问鼎 (Ascendant). 合体 is from 凡人修仙传, NOT 仙逆.
    // 问鼎 is the canon 7th First Step realm per all 6+ independent sources.
    ASCENDANT        (7, 0, "Ascendant",           "问鼎",       10000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Transitional Step ──
    // CRON-119: 婴变 → 阴虚 (Yin Deficiency). 婴变 is the 6th First Step realm, NOT transitional.
    // The canon 1st transitional realm is 阴虚 (Yin Deficiency). English "Illusory Yin" is retained
    // as a close-enough translation (阴虚 = Yin Deficiency ≈ Illusory Yin).
    ILLUSORY_YIN     (8, 1, "Illusory Yin",        "阴虚",       15000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    // CRON-119: 洞玄 → 阳实 (Yang Substantiality). 洞玄 does NOT exist in 仙逆.
    // The canon 2nd transitional realm is 阳实 (Yang Substantiality). English "Corporeal Yang" is retained
    // as a close-enough translation (阳实 = Yang Substantiality ≈ Corporeal Yang).
    CORPOREAL_YANG   (9, 1, "Corporeal Yang",      "阳实",       20000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Second Step (Nirvana) ──
    // Canon: 窥涅, 净涅, 碎涅 (the three Nirvana realms). 夺舍 is a technique, not a realm —
    // but the mod retains it as a realm-like state. See class Javadoc "Known remaining discrepancies".
    NIRVANA_SCRYER   (10, 2, "Nirvana Scryer",     "窥涅",       50000,       PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    NIRVANA_CLEANSER (11, 2, "Nirvana Cleanser",   "净涅",       100000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    NIRVANA_FRUIT    (12, 2, "Nirvana Fruit",      "碎涅",       200000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    SPIRIT_SEIZER    (13, 2, "Spirit Seizer",      "夺舍",       300000,      PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),

    // ── Immortal+ Step ──
    // NOTE: The canon Third Step is 空之四境 (空涅, 空灵, 空玄, 空劫). The mod uses 真仙, 古境,
    // 大天尊, 超脱 instead. This is a STRUCTURAL discrepancy deferred to a future CRON.
    TRUE_IMMORTAL    (14, 3, "True Immortal",      "真仙",       1000000,     PerceptionTier.ASCENDANT,       CanonEngine.Confidence.NOVEL_STATEMENT),
    ANCIENT          (15, 3, "Ancient",             "古境",       5000000,     PerceptionTier.TRANSCENDENCE,  CanonEngine.Confidence.NOVEL_STATEMENT),
    PARAGON          (16, 3, "Paragon",             "大天尊",     10000000,    PerceptionTier.TRANSCENDENCE,  CanonEngine.Confidence.NOVEL_STATEMENT),
    TRANSCENDENCE    (17, 4, "Transcendence",       "超脱",       Long.MAX_VALUE, PerceptionTier.TRANSCENDENCE, CanonEngine.Confidence.NOVEL_STATEMENT);

    public final int order;
    public final int step; // 0=First, 1=Transitional, 2=Second, 3=Immortal, 4=4th Step
    public final String name;
    public final String nameCn;
    public final long lifespan;
    public final PerceptionTier perceptionTier;
    public final CanonEngine.Confidence canonConfidence;

    RealmId(int order, int step, String name, String nameCn, long lifespan,
            PerceptionTier perceptionTier, CanonEngine.Confidence canonConfidence) {
        this.order = order;
        this.step = step;
        this.name = name;
        this.nameCn = nameCn;
        this.lifespan = lifespan;
        this.perceptionTier = perceptionTier;
        this.canonConfidence = canonConfidence;
    }

    /** Is this realm at least as high as {@code other}? */
    public boolean isAtLeast(RealmId other) {
        return this.order >= other.order;
    }

    /** Get the next realm in the ladder, or null if at the top. */
    public RealmId next() {
        if (this == TRANSCENDENCE) return null;
        return values()[this.ordinal() + 1];
    }

    /** Get the realm by order index. */
    public static RealmId byOrder(int order) {
        for (RealmId r : values()) if (r.order == order) return r;
        return order >= 17 ? TRANSCENDENCE : MORTAL;
    }
}
