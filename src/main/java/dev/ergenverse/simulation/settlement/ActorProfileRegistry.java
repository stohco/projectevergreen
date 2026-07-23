package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;

import java.util.HashMap;
import java.util.Map;

/**
 * ActorProfileRegistry — the registry of {@link ActorProfile}s for every actor
 * in the simulation.
 *
 * <p>This is the <b>"different minds"</b> table. When a {@link WorldSituation}
 * arrives, the {@link ActorReasoningEngine} looks up each actor's profile here
 * and reasons over the situation through that profile's lens. Same world,
 * different conclusions.
 *
 * <h2>Canon vs Simulation profiles (Article XLIV §3)</h2>
 * <ul>
 *   <li><b>Canon profiles</b> (Wang Lin, Wang Tianshui, Wang Qingyue, Wang Zhou)
 *       are authored from the novels. Wang Lin's profile is the keystone: a
 *       HIDDEN_CULTIVATOR who appears weak (Qi Condensation Stage 1, called
 *       "waste" by the village) but has high caution, high observation, and
 *       concealed capability. His reasoning against a wolf pack produces
 *       OBSERVING_THREAT — not fleeing, not guarding, but watching from cover.</li>
 *   <li><b>Simulation profiles</b> (Da Niu, Wang Wei, Wang Ping, etc.) are
 *       generated to fill the village. They must never contradict canon. Their
 *       profiles produce the "ordinary villager" reactions (flee, guard,
 *       secure assets) that contrast with Wang Lin's unique response.</li>
 * </ul>
 *
 * <p>Per Article XLI: "Wang Lin is not special-cased. His behavior emerges from
 * his traits. If a mechanism works for Wang Lin, it works for any actor with
 * the same traits." — This registry honors that: Wang Lin's HIDDEN_CULTIVATOR
 * profile is just a profile. Any actor given the same profile would reason the
 * same way. The uniqueness is in the trait combination, not in special-cased
 * code.
 */
public final class ActorProfileRegistry {

    private static final Map<String, ActorProfile> BY_ID = new HashMap<>();

    private ActorProfileRegistry() {}

    /** Initialize all profiles. Called once on server start. */
    public static void initialize() {
        seedWangFamilyVillage();
        seedHengYueSect();
        Ergenverse.LOGGER.info("[ActorProfileRegistry] Registered {} actor profiles (the 'different minds' table).",
                BY_ID.size());
    }

    /** Get an actor's profile, or null if unregistered (falls back to peaceful daily rhythm). */
    public static ActorProfile get(String actorId) {
        return BY_ID.get(actorId);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Wang Family Village — 9 villagers + Wang Lin
    // ═══════════════════════════════════════════════════════════════════
    private static void seedWangFamilyVillage() {
        // ── Canon NPCs (authored from the novels) ──

        // WANG LIN — the keystone profile. Young Wang Lin at Chapter 1: Qi
        // Condensation Stage 1, called "waste" by the village, secretly
        // studying a restriction in the cave above the village. His actual
        // strength is not aptitude but extreme caution, observation, and
        // persistence. Against mortal wolves: no danger, observe from cover,
        // protect family only if necessary, NEVER reveal strength.
        // (RI Ch.1-5: Wang Lin watches and waits; he does not posture.)
        register(new ActorProfile("npc_wang_lin", "Wang Lin",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.50f,           // courage — moderate (not reckless, not cowardly)
                ActorProfile.Role.HIDDEN_CULTIVATOR,
                true,            // has family (parents Tianshui + Qingyue)
                false,           // revealedStrength — HIDDEN, the whole point
                0.65f,           // combatConfidence — low power but high assessment skill
                true));          // canon-sourced

        // WANG TIANSHUI — Wang Lin's father, patriarch of the Wang family.
        // A mortal farmer, but as patriarch he defends the family compound.
        // Against wolves: grabs a tool, stands at the gate. (INFERRED: the
        // patriarch's duty; the village looks to the leading family.)
        register(new ActorProfile("npc_wang_tianshui", "Wang Tianshui",
                ActorProfile.CultivationTier.MORTAL,
                0.55f,           // courage — moderate (patriarch's resolve)
                ActorProfile.Role.FARMER,
                true,            // has family
                true,            // revealedStrength — mortal, nothing to hide
                0.35f,           // combatConfidence — a farmer vs. wolves
                true));

        // WANG QINGYUE — Wang Lin's mother. Gather children, bar the door.
        register(new ActorProfile("npc_wang_qingyue", "Wang Qingyue",
                ActorProfile.CultivationTier.MORTAL,
                0.45f,           // courage — moderate-low (protective mother)
                ActorProfile.Role.HOMEMAKER,
                true,            // has family
                true,
                0.20f,
                true));

        // WANG ZHOU — village elder. Old, wisdom over strength. Organizes the
        // retreat, does not personally guard.
        register(new ActorProfile("npc_wang_zhou", "Wang Zhou",
                ActorProfile.CultivationTier.MORTAL,
                0.40f,           // courage — aged, cautious
                ActorProfile.Role.ELDER,
                false,           // family grown
                true,
                0.15f,
                true));

        // ── Simulation NPCs (generated; never contradict canon) ──

        // DA NIU — village laborer. Strong, responsible for livestock. Against
        // wolves: runs to the livestock pen to secure the animals, then guards
        // the pen with a pitchfork. (Fills the "hunter/guard" niche the user
        // named, using an existing villager.)
        register(new ActorProfile("npc_da_niu", "Da Niu",
                ActorProfile.CultivationTier.MORTAL,
                0.60f,           // courage — higher than average (strong laborer)
                ActorProfile.Role.LABORER,
                false,           // no family (bachelor, per typical laborer filler)
                true,
                0.40f,           // combatConfidence — strong, has tools
                false));

        // WANG TIANSHAN — extended family, farmer. Flees home.
        register(new ActorProfile("npc_wang_tianshan", "Wang Tianshan",
                ActorProfile.CultivationTier.MORTAL,
                0.35f,
                ActorProfile.Role.FARMER,
                true,
                true,
                0.25f,
                false));

        // ZHOU TINGSU — married into village, homemaker. Gather children, flee.
        register(new ActorProfile("npc_zhou_tingsu", "Zhou Tingsu",
                ActorProfile.CultivationTier.MORTAL,
                0.40f,
                ActorProfile.Role.HOMEMAKER,
                true,
                true,
                0.20f,
                false));

        // WANG WEI — village boy (teen). Follows the elders, flees.
        register(new ActorProfile("npc_wang_wei", "Wang Wei",
                ActorProfile.CultivationTier.MORTAL,
                0.30f,
                ActorProfile.Role.CHILD,
                true,
                true,
                0.10f,
                false));

        // WANG PING — village child. Always flees to family.
        register(new ActorProfile("npc_wang_ping", "Wang Ping",
                ActorProfile.CultivationTier.MORTAL,
                0.20f,
                ActorProfile.Role.CHILD,
                true,
                true,
                0.05f,
                false));

        // WANG YIYI — village girl (child). Always flees to family.
        register(new ActorProfile("npc_wang_yiyi", "Wang Yiyi",
                ActorProfile.CultivationTier.MORTAL,
                0.20f,
                ActorProfile.Role.CHILD,
                true,
                true,
                0.05f,
                false));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Heng Yue Sect — 4 disciples (stub profiles for the sect)
    // ═══════════════════════════════════════════════════════════════════
    private static void seedHengYueSect() {
        // Sect disciples are entry cultivators in a competitive sect.
        // Against a threat: they guard (sect duty) rather than flee.
        register(new ActorProfile("npc_qiu_siping", "Qiu Siping",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.60f, ActorProfile.Role.CULTIVATOR, false, true, 0.50f, true));
        register(new ActorProfile("npc_wang_zhuo", "Wang Zhuo",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.55f, ActorProfile.Role.CULTIVATOR, false, true, 0.45f, true));
        register(new ActorProfile("npc_wang_hao", "Wang Hao",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.50f, ActorProfile.Role.CULTIVATOR, false, true, 0.40f, true));
        register(new ActorProfile("npc_sun_dazhu", "Sun Dazhu",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.45f, ActorProfile.Role.CULTIVATOR, false, true, 0.35f, true));
    }

    private static void register(ActorProfile profile) {
        BY_ID.put(profile.actorId, profile);
    }
}
