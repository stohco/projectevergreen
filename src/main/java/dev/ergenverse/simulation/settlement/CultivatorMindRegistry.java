package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;

import java.util.HashMap;
import java.util.Map;

/**
 * CultivatorMindRegistry — the registry of {@link CultivatorMind}s.
 *
 * <p>This is the <b>"different minds"</b> table — the per-actor motivation
 * weights that make the same {@link WorldSituation} produce different
 * {@link Activity} decisions. Per the user's directive: "Nobody special-cases
 * Wang Lin. Nobody special-cases Li Muwan. The reasoning emerges from what
 * they care about."
 *
 * <h2>Canon-faithful motivation weights</h2>
 * <p>Each mind is seeded with weights drawn from the novels:
 * <ul>
 *   <li><b>Wang Lin</b> (RI Ch.1-5): CONCEAL_STRENGTH and CURIOSITY paramount.
 *       He watches, he waits, he does not posture. SURVIVAL moderate (he's
 *       cautious, not cowardly). PROTECT_FAMILY high (his parents). PRESTIGE
 *       near-zero (the village calls him "waste" and he doesn't care).</li>
 *   <li><b>Wang Tianshui</b> (patriarch): PROTECT_FAMILY + DUTY high. A mortal
 *       who stands at the gate because that's his responsibility, not because
 *       he's brave.</li>
 *   <li><b>Da Niu</b> (laborer): DUTY toward livestock high. SURVIVAL moderate.
 *       CONCEAL_STRENGTH zero (a mortal with a pitchfork has nothing to hide).</li>
 *   <li><b>Mortals</b> (farmers, children, homemakers): SURVIVAL dominates.
 *       CURIOSITY low. CONCEAL_STRENGTH zero. → FLEEING_HOME scores highest.</li>
 *   <li><b>Sect disciples</b>: DUTY + PRESTIGE high (sect gate duty, face).
 *       CONCEAL_STRENGTH low (they're proud of their cultivation).</li>
 * </ul>
 *
 * <p>The same wolf event produces: Wang Lin → OBSERVE (concealment + curiosity
 * win), Da Niu → GUARD (duty wins), mortals → FLEE (survival wins). No
 * "if Wang Lin" anywhere — the weights decide.
 */
public final class CultivatorMindRegistry {

    private static final Map<String, CultivatorMind> BY_ID = new HashMap<>();

    private CultivatorMindRegistry() {}

    /** Initialize all minds. Called once on server start. */
    public static void initialize() {
        seedWangFamilyVillage();
        seedHengYueSect();
        Ergenverse.LOGGER.info("[CultivatorMindRegistry] Registered {} cultivator minds (the 'different minds' table).",
                BY_ID.size());
    }

    /** Get an actor's mind, or null if unregistered. */
    public static CultivatorMind get(String actorId) {
        return BY_ID.get(actorId);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Wang Family Village
    // ═══════════════════════════════════════════════════════════════════
    private static void seedWangFamilyVillage() {
        // WANG LIN — the keystone mind. His defining traits (RI Ch.1-5):
        // extreme caution, observation skill, concealment, stubborn persistence.
        // He is called "waste" and does not care about face. His parents are
        // his world. He studies a restriction in secret.
        CultivatorMind wangLin = new CultivatorMind("npc_wang_lin", "Wang Lin",
                ActorProfileRegistry.get("npc_wang_lin"));
        wangLin.setMotivation(Motivation.CONCEAL_STRENGTH, 0.90f); // paramount — never reveal
        wangLin.setMotivation(Motivation.CURIOSITY, 0.80f);        // his actual talent
        wangLin.setMotivation(Motivation.PROTECT_FAMILY, 0.85f);   // his parents
        wangLin.setMotivation(Motivation.CULTIVATION_PROGRESS, 0.70f);
        wangLin.setMotivation(Motivation.SURVIVAL, 0.55f);         // cautious, not cowardly
        wangLin.setMotivation(Motivation.DUTY, 0.20f);
        wangLin.setMotivation(Motivation.PRESTIGE, 0.05f);         // doesn't care about face
        wangLin.setMotivation(Motivation.GREED, 0.10f);
        wangLin.setMotivation(Motivation.COMPASSION, 0.30f);
        register(wangLin);

        // WANG TIANSHUI — patriarch. Mortal, but duty-bound to defend the
        // family compound. PROTECT_FAMILY + DUTY high.
        CultivatorMind tianshui = new CultivatorMind("npc_wang_tianshui", "Wang Tianshui",
                ActorProfileRegistry.get("npc_wang_tianshui"));
        tianshui.setMotivation(Motivation.PROTECT_FAMILY, 0.85f);
        tianshui.setMotivation(Motivation.DUTY, 0.75f);
        tianshui.setMotivation(Motivation.SURVIVAL, 0.50f);
        tianshui.setMotivation(Motivation.PRESTIGE, 0.40f); // patriarch's face
        tianshui.setMotivation(Motivation.CONCEAL_STRENGTH, 0.0f);
        tianshui.setMotivation(Motivation.CURIOSITY, 0.20f);
        register(tianshui);

        // WANG QINGYUE — mother. Gather children, bar the door.
        CultivatorMind qingyue = new CultivatorMind("npc_wang_qingyue", "Wang Qingyue",
                ActorProfileRegistry.get("npc_wang_qingyue"));
        qingyue.setMotivation(Motivation.PROTECT_FAMILY, 0.90f);
        qingyue.setMotivation(Motivation.SURVIVAL, 0.60f);
        qingyue.setMotivation(Motivation.COMPASSION, 0.60f);
        qingyue.setMotivation(Motivation.DUTY, 0.55f);
        qingyue.setMotivation(Motivation.CURIOSITY, 0.10f);
        register(qingyue);

        // WANG ZHOU — village elder. Wisdom over strength.
        CultivatorMind zhou = new CultivatorMind("npc_wang_zhou", "Wang Zhou",
                ActorProfileRegistry.get("npc_wang_zhou"));
        zhou.setMotivation(Motivation.DUTY, 0.70f);          // stewardship of the village
        zhou.setMotivation(Motivation.PROTECT_FAMILY, 0.40f);
        zhou.setMotivation(Motivation.SURVIVAL, 0.65f);
        zhou.setMotivation(Motivation.COMPASSION, 0.50f);
        zhou.setMotivation(Motivation.CURIOSITY, 0.30f);
        zhou.setMotivation(Motivation.PRESTIGE, 0.45f);      // elder's dignity
        register(zhou);

        // DA NIU — laborer. Responsible for livestock. DUTY + SURVIVAL.
        CultivatorMind daNiu = new CultivatorMind("npc_da_niu", "Da Niu",
                ActorProfileRegistry.get("npc_da_niu"));
        daNiu.setMotivation(Motivation.DUTY, 0.80f);          // livestock responsibility
        daNiu.setMotivation(Motivation.SURVIVAL, 0.55f);
        daNiu.setMotivation(Motivation.PROTECT_FAMILY, 0.30f);
        daNiu.setMotivation(Motivation.PRESTIGE, 0.35f);      // wants to be seen as capable
        daNiu.setMotivation(Motivation.CURIOSITY, 0.15f);
        daNiu.setMotivation(Motivation.CONCEAL_STRENGTH, 0.0f);
        register(daNiu);

        // WANG TIANSHAN — extended family, farmer. Mortal.
        CultivatorMind tianshan = new CultivatorMind("npc_wang_tianshan", "Wang Tianshan",
                ActorProfileRegistry.get("npc_wang_tianshan"));
        tianshan.setMotivation(Motivation.SURVIVAL, 0.80f);
        tianshan.setMotivation(Motivation.PROTECT_FAMILY, 0.60f);
        tianshan.setMotivation(Motivation.DUTY, 0.40f);
        register(tianshan);

        // ZHOU TINGSU — married into village, homemaker.
        CultivatorMind tingsu = new CultivatorMind("npc_zhou_tingsu", "Zhou Tingsu",
                ActorProfileRegistry.get("npc_zhou_tingsu"));
        tingsu.setMotivation(Motivation.PROTECT_FAMILY, 0.85f);
        tingsu.setMotivation(Motivation.SURVIVAL, 0.65f);
        tingsu.setMotivation(Motivation.COMPASSION, 0.50f);
        register(tingsu);

        // WANG WEI — village boy (teen). Follows elders, low courage.
        CultivatorMind wei = new CultivatorMind("npc_wang_wei", "Wang Wei",
                ActorProfileRegistry.get("npc_wang_wei"));
        wei.setMotivation(Motivation.SURVIVAL, 0.85f);
        wei.setMotivation(Motivation.PROTECT_FAMILY, 0.50f);
        wei.setMotivation(Motivation.CURIOSITY, 0.35f);   // youth's curiosity
        register(wei);

        // WANG PING — child.
        CultivatorMind ping = new CultivatorMind("npc_wang_ping", "Wang Ping",
                ActorProfileRegistry.get("npc_wang_ping"));
        ping.setMotivation(Motivation.SURVIVAL, 0.90f);
        ping.setMotivation(Motivation.PROTECT_FAMILY, 0.60f);
        ping.setMotivation(Motivation.CURIOSITY, 0.40f);
        register(ping);

        // WANG YIYI — village girl (child).
        CultivatorMind yiyi = new CultivatorMind("npc_wang_yiyi", "Wang Yiyi",
                ActorProfileRegistry.get("npc_wang_yiyi"));
        yiyi.setMotivation(Motivation.SURVIVAL, 0.90f);
        yiyi.setMotivation(Motivation.PROTECT_FAMILY, 0.60f);
        yiyi.setMotivation(Motivation.CURIOSITY, 0.40f);
        register(yiyi);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Heng Yue Sect
    // ═══════════════════════════════════════════════════════════════════
    private static void seedHengYueSect() {
        // Sect disciples: DUTY (gate duty) + PRESTIGE (face) high.
        // CONCEAL_STRENGTH low (proud of their cultivation).
        seedDisciple("npc_qiu_siping", "Qiu Siping", 0.60f, 0.50f);
        seedDisciple("npc_wang_zhuo", "Wang Zhuo", 0.55f, 0.55f);
        seedDisciple("npc_wang_hao", "Wang Hao", 0.50f, 0.50f);
        seedDisciple("npc_sun_dazhu", "Sun Dazhu", 0.45f, 0.40f);
    }

    private static void seedDisciple(String id, String name, float duty, float prestige) {
        CultivatorMind m = new CultivatorMind(id, name, ActorProfileRegistry.get(id));
        m.setMotivation(Motivation.DUTY, duty);
        m.setMotivation(Motivation.PRESTIGE, prestige);
        m.setMotivation(Motivation.SURVIVAL, 0.50f);
        m.setMotivation(Motivation.CULTIVATION_PROGRESS, 0.55f);
        m.setMotivation(Motivation.CONCEAL_STRENGTH, 0.15f); // proud, not hidden
        m.setMotivation(Motivation.PROTECT_FAMILY, 0.20f);
        register(m);
    }

    private static void register(CultivatorMind mind) {
        BY_ID.put(mind.actorId, mind);
    }
}
