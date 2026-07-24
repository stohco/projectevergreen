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
        seedTengFamilyCity();
        seedTianShuiCity();
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

    // ═══════════════════════════════════════════════════════════════════
    //  Teng Family City (滕城) — 12 NPCs (3 canon + 9 simulation)
    //  The largest city in Zhao Country. Teng Huayuan rules here.
    //  Per Article XLI: no character is special-cased. These profiles
    //  produce the city's social dynamics through the ActorReasoningEngine.
    // ═══════════════════════════════════════════════════════════════════
    private static void seedTengFamilyCity() {

        // ── Canon NPCs ──────────────────────────────────────────────

        // TENG HUAYUAN — the patriarch. Half-Step Deity Transformation —
        // the most powerful being in Zhao Country. Arrogant, ruthless,
        // vengeful. Destroyed the Wang family. Against any threat near his
        // city: assesses whether it's worth his attention; if trivial, ignores;
        // if interesting, observes; if threatening, annihilates.
        // Canon (RI Ch.1-3): main antagonist of Wang Lin's early arc.
        register(new ActorProfile("npc_teng_huayuan", "Teng Huayuan",
                ActorProfile.CultivationTier.SOUL_FORMATION,  // Half-Step Deity Transformation ≈ Soul Formation tier
                0.85f,          // courage — extremely high; he has the power to back it
                ActorProfile.Role.CULTIVATOR,
                false,           // no family bonds (he sacrificed them for power)
                true,            // revealedStrength — everyone knows his power
                0.95f,           // combatConfidence — he can destroy anything in Zhao Country
                true));          // canon

        // TENG LI — arrogant young master. Late Foundation Establishment.
        // Bullies weaker cultivators and mortals. Cruel, entitled.
        // Against threats: overconfident, attacks aggressively, may flee
        // if clearly outmatched (but rarely admits being outmatched).
        // Canon (RI Ch.1): the young master who persecutes Wang Lin.
        register(new ActorProfile("npc_teng_li", "Teng Li",
                ActorProfile.CultivationTier.FOUNDATION,
                0.70f,          // courage — high from arrogance, not actual bravery
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.55f,           // combatConfidence — overestimates himself
                true));

        // TENG XIUXIU — conflicted family member. Low-tier cultivator.
        // Torn between loyalty to the Teng family and disgust at their
        // cruelty. Quietly sympathetic to the oppressed. Against threats:
        // guards defensively, protects weaker people first.
        // Canon: unique personality — the one Teng who questions the family.
        register(new ActorProfile("npc_teng_xiuxiu", "Teng Xiuxiu",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.45f,          // courage — moderate (conflicted, not bold)
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.30f,           // combatConfidence — knows she is weak
                true));

        // ── Simulation NPCs ────────────────────────────────────────

        // TENG GUARD CAPTAIN — mid Foundation Establishment. Stern,
        // disciplined, loyal to Teng Huayuan. Commands the city guard.
        // Against threats: guards his post, coordinates defense, calls
        // for reinforcements. Never flees.
        register(new ActorProfile("npc_teng_guard_captain", "Teng Guard Captain",
                ActorProfile.CultivationTier.FOUNDATION,
                0.75f,          // courage — high (disciplined soldier)
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.60f,           // combatConfidence — trained and equipped
                false));

        // TENG CULTIVATOR GUARD — early Core Formation. Arrogant,
        // condescending, paranoid. The Teng family's elite enforcer.
        // Patrols the cultivator quarter. Against threats: attacks
        // immediately, paranoid about hidden cultivators.
        register(new ActorProfile("npc_teng_cultivator_guard", "Teng Cultivator Guard",
                ActorProfile.CultivationTier.CORE,
                0.65f,          // courage — high (elite enforcer)
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.70f,           // combatConfidence — Core Formation vs most threats
                false));

        // TENG MERCHANT — mortal. Shrewd, opportunistic, respectful
        // to power. Runs the market stall. Against threats: hides
        // goods first, then seeks shelter. Never fights.
        register(new ActorProfile("npc_teng_merchant", "Teng Family Merchant",
                ActorProfile.CultivationTier.MORTAL,
                0.30f,          // courage — low (merchant mentality)
                ActorProfile.Role.MERCHANT,
                true, true,
                0.10f,           // combatConfidence — no combat ability
                false));

        // TENG SERVANT — mortal. Obedient, fearful, secretly observant.
        // Serves in the governor's keep. Hears everything. Against threats:
        // obeys Teng Huayuan's orders. If no orders, hides.
        register(new ActorProfile("npc_teng_servant", "Teng Family Servant",
                ActorProfile.CultivationTier.MORTAL,
                0.20f,          // courage — very low (conditioned obedience)
                ActorProfile.Role.HOMEMAKER,
                false, true,
                0.05f,           // combatConfidence — servant, no training
                false));

        // TEMPLE PRIEST LIU — mortal. Pious, haunted, quietly defiant.
        // Tends the temple. Knows the city's secrets. Against threats:
        // protects the temple first, then civilians. Nonviolent.
        register(new ActorProfile("npc_teng_temple_priest", "Temple Priest Liu",
                ActorProfile.CultivationTier.MORTAL,
                0.40f,          // courage — moderate (quietly defiant)
                ActorProfile.Role.ELDER,
                false, true,
                0.10f,           // combatConfidence — priest, not a fighter
                false));

        // TAVEN KEEPER MEI — mortal. Warm, gossipy, perceptive.
        // Knows everyone's business. Against threats: locks the tavern,
        // protects patrons, gossips about the threat afterward.
        register(new ActorProfile("npc_teng_tavern_keeper", "Tavern Keeper Mei",
                ActorProfile.CultivationTier.MORTAL,
                0.35f,          // courage — moderate-low (protects her business)
                ActorProfile.Role.MERCHANT,
                true, true,
                0.15f,           // combatConfidence — keeps a kitchen knife
                false));

        // ONE-EARED ZHOU (smuggler) — late Qi Condensation. Cunning,
        // paranoid, mercenary. Operates the smuggler tunnels. Against
        // threats: assesses profit, fights only if cornered, prefers
        // to escape via tunnels.
        register(new ActorProfile("npc_teng_smuggler", "One-Eared Zhou",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.40f,          // courage — moderate (mercenary, not brave)
                ActorProfile.Role.MERCHANT,
                false, true,
                0.35f,           // combatConfidence — can fight but prefers not to
                false));

        // DOCK FOREMAN CHEN — mortal. Gruff, overworked, honest.
        // Manages the warehouse district. Against threats: secures
        // cargo, then helps with defense if capable.
        register(new ActorProfile("npc_teng_dock_foreman", "Dock Foreman Chen",
                ActorProfile.CultivationTier.MORTAL,
                0.50f,          // courage — moderate (laborer toughness)
                ActorProfile.Role.LABORER,
                true, true,
                0.25f,           // combatConfidence — has tools and strength
                false));

        // OLD WEI THE BEGGAR — mortal. Broken, bitter, surprisingly
        // observant. A failed cultivator. Hears everything in the
        // streets. Against threats: flees, but remembers everything.
        register(new ActorProfile("npc_teng_beggar", "Old Wei the Beggar",
                ActorProfile.CultivationTier.MORTAL,
                0.15f,          // courage — very low (broken by life)
                ActorProfile.Role.ELDER,
                false, true,
                0.05f,           // combatConfidence — beggar, no fight
                false));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Tian Shui City (天水城) — 13 NPCs (2 INFERRED canon + 11 simulation)
    //  Major trade hub of Zhao Country, 100,000 population.
    //  Per Article XLI: no character is special-cased. These profiles
    //  produce the city's social dynamics through the ActorReasoningEngine.
    //  The city's unique politics: merchant guild vs cultivator guild,
    //  governor's reliance on divination.
    // ═══════════════════════════════════════════════════════════════════
    private static void seedTianShuiCity() {

        // ── INFERRED Canon NPCs ────────────────────────────────────────

        // GOVERNOR BAI — INFERRED canon. Early Foundation Establishment.
        // Calculating, believes in fate, delegates to diviners. Runs the
        // city as a businessman, not a warrior. Against threats: assesses
        // whether it affects commerce; if yes, mobilizes guard; if not,
        // consults Gao Qiming for auspicious timing.
        register(new ActorProfile("npc_tianshui_governor_bai", "Governor Bai",
                ActorProfile.CultivationTier.FOUNDATION,
                0.50f,          // courage — moderate (calculating, not brave)
                ActorProfile.Role.CULTIVATOR,
                true,            // has family (mortal granny in mansion)
                true,            // revealedStrength — governor's authority is visible
                0.40f,           // combatConfidence — early Foundation, relies on guards
                true));          // INFERRED canon

        // GAO QIMING — INFERRED canon. Qi Condensation diviner.
        // Mysterious, observant, speaks in riddles. Serves as the
        // governor's advisor. Against threats: observes from safety,
        // provides cryptic advice. Never fights directly.
        register(new ActorProfile("npc_gao_qiming", "Gao Qiming",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.35f,          // courage — low (seer, not warrior)
                ActorProfile.Role.CULTIVATOR,
                false,           // no family bonds
                true,            // revealedStrength — openly practices divination
                0.15f,           // combatConfidence — avoids combat
                true));          // INFERRED canon

        // ── Simulation NPCs ──────────────────────────────────────────

        // GUARD CAPTAIN ZHAO — mid Foundation Establishment. Disciplined,
        // professional. Leads the city guard under Governor Bai's orders.
        // Against threats: mobilizes guards, coordinates defense, calls
        // for cultivator guild support if needed. Never flees post.
        register(new ActorProfile("npc_tianshui_guard_zhao", "Guard Captain Zhao",
                ActorProfile.CultivationTier.FOUNDATION,
                0.70f,          // courage — high (professional soldier)
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.60f,           // combatConfidence — trained and equipped
                false));

        // CULTIVATOR YE — Core Formation. The cultivator guild enforcer.
        // Arrogant toward mortals, respects power. Patrols the guild district.
        // Against threats: attacks immediately if within guild territory;
        // otherwise, reports to guild master and Governor Bai.
        register(new ActorProfile("npc_tianshui_cultivator_ye", "Cultivator Ye",
                ActorProfile.CultivationTier.CORE,
                0.60f,          // courage — moderate-high (Core Formation strength)
                ActorProfile.Role.CULTIVATOR,
                false, true,
                0.65f,           // combatConfidence — strongest fighter in the city guard
                false));

        // MERCHANT LIU — mortal. Shrewd, opportunistic, networked.
        // Runs a shop in the merchant quarter. Knows every trade secret.
        // Against threats: hides valuables, locks shop, evacuates
        // via smuggler tunnels if desperate.
        register(new ActorProfile("npc_tianshui_merchant_liu", "Merchant Liu",
                ActorProfile.CultivationTier.MORTAL,
                0.35f,          // courage — moderate (calculating merchant)
                ActorProfile.Role.MERCHANT,
                true, true,
                0.10f,           // combatConfidence — merchant, no fight
                false));

        // TAVERN KEEPER — mortal. Warm, gossipy, knows everyone's business.
        // The Drunk Dragon Inn is the city's social hub. Against threats:
        // locks doors, protects patrons, gossips about the aftermath.
        register(new ActorProfile("npc_tianshui_tavern_keeper", "Tavern Keeper",
                ActorProfile.CultivationTier.MORTAL,
                0.35f,          // courage — moderate-low (protects business)
                ActorProfile.Role.MERCHANT,
                true, true,
                0.15f,           // combatConfidence — kitchen knife at best
                false));

        // TEMPLE PRIEST FENG — mortal. Pious, serene, connected to the
        // temple's fortune-telling traditions. Performs ceremonies at dawn
        // and dusk. Against threats: protects the temple relics, prays,
        // nonviolent. The temple is neutral ground.
        register(new ActorProfile("npc_tianshui_temple_feng", "Temple Priest Feng",
                ActorProfile.CultivationTier.MORTAL,
                0.40f,          // courage — moderate (quietly defiant)
                ActorProfile.Role.ELDER,
                false, true,
                0.10f,           // combatConfidence — priest, not a fighter
                false));

        // WAREHOUSE FENG — mortal. Gruff, organized, controls the port
        // warehouse district. Against threats: secures goods, barricades
        // warehouse, coordinates with dock foreman.
        register(new ActorProfile("npc_tianshui_warehouse_feng", "Warehouse Feng",
                ActorProfile.CultivationTier.MORTAL,
                0.45f,          // courage — moderate (laborer toughness)
                ActorProfile.Role.LABORER,
                false, true,
                0.20f,           // combatConfidence — has tools and strength
                false));

        // SMUGGLER HEI — late Qi Condensation. Cunning, paranoid, operates
        // underground. The dark underbelly of a trade city. Against threats:
        // escapes via tunnels, fights only if cornered. Knows every
        // secret passage in the city.
        register(new ActorProfile("npc_tianshui_smuggler_hei", "Smuggler Hei",
                ActorProfile.CultivationTier.QI_CONDENSATION,
                0.35f,          // courage — low (survives by hiding)
                ActorProfile.Role.MERCHANT,
                false, true,
                0.40f,           // combatConfidence — can fight but prefers escape
                false));

        // DOCK ZHOU — mortal. Gruff, overworked, manages the port piers.
        // Against threats: secures cargo, coordinates with warehouse.
        register(new ActorProfile("npc_tianshui_dock_zhou", "Dock Zhou",
                ActorProfile.CultivationTier.MORTAL,
                0.50f,          // courage — moderate (dock worker toughness)
                ActorProfile.Role.LABORER,
                true, true,
                0.25f,           // combatConfidence — dock tools
                false));

        // BEGGAR SUN — mortal. Silent, watchful, surprisingly perceptive.
        // Hears everything in the streets. Knows the city's secrets.
        // Against threats: flees, but remembers everything for gossip.
        register(new ActorProfile("npc_tianshui_beggar_sun", "Beggar Sun",
                ActorProfile.CultivationTier.MORTAL,
                0.20f,          // courage — very low (beggar survival instinct)
                ActorProfile.Role.ELDER,
                false, true,
                0.05f,           // combatConfidence — beggar, no fight
                false));

        // MORTAL GRANNY — mortal. Elderly woman, gossipy, knows the
        // city's history. Lives in the governor's mansion as attendant.
        register(new ActorProfile("npc_tianshui_mortal_granny", "Mortal Granny",
                ActorProfile.CultivationTier.MORTAL,
                0.15f,          // courage — very low (elderly woman)
                ActorProfile.Role.HOMEMAKER,
                false, true,
                0.05f,           // combatConfidence — elderly, no fight
                false));
    }
}
