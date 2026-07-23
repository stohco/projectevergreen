package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;

/**
 * CanonRelationshipSeeder — seeds the ActorRelationshipStore with
 * canonically-accurate starting NPC-to-NPC relationships on first world load.
 *
 * <p>Per Constitution Articles X, XXXIV, and XLI:
 * <ul>
 *   <li><b>Article X — Intelligence Is Reasoning:</b> NPCs reason, not script.
 *       Reasoning requires state. A relationship store starting at all-zeros
 *       means no NPC has ever met another NPC. This is wrong — canon NPCs
 *       have deep, pre-existing histories.</li>
 *   <li><b>Article XXXIV — Motivation &amp; Desire:</b> NPCs possess goals and
 *       relationships. Wang Lin's protectiveness toward Li Muwan, Qing Shui's
 *       loyalty toward Wang Lin, the Teng Clan's hostility toward Wang Lin —
 *       these are not emergent properties. They are canon.</li>
 *   <li><b>Article XLI §2 — Simulation Is General:</b> Every NPC has a full
 *       multi-axis relationship graph. Seeding only the player-facing ones
 *       would violate this principle.</li>
 *   <li><b>Article XLIII §2 — Permanence:</b> Seeding happens ONCE on first
 *       world load. After that, relationships evolve through the event bus.
 *       If the player has never joined, NPC-to-NPC relationships still have
 *       their canon foundation.</li>
 * </ul>
 *
 * <h2>Canon Sources (Renegade Immortal / 仙逆)</h2>
 * <p>Relationships are derived from specific novel scenes and character dynamics:
 * <ul>
 *   <li>Wang Lin &amp; Li Muwan: mutual devotion. Wang Lin risks everything to save her.
 *       She is the emotional anchor of the first half of the novel.</li>
 *   <li>Wang Lin &amp; Qing Shui: master-student, then peer. Qing Shui rescues Wang Lin
 *       multiple times. Deep trust and respect.</li>
 *   <li>Wang Lin &amp; Situ Nan: contentious allies. Situ Nan is unpredictable but
 *       ultimately reliable. Moderate trust, high familiarity, low predictability.</li>
 *   <li>Wang Lin &amp; Teng Clan: extreme hostility. The Teng Clan destroyed Wang Lin's
 *       family in Zhao Country. High grievance, high fear (on Teng side).</li>
 *   <li>Wang Ping &amp; Wang Lin: father-son. Wang Ping is Wang Lin's adopted son.
 *       Unconditional trust, deep protectiveness.</li>
 *   <li>Heng Yue Sect &amp; Wang Lin: complicated. Wang Lin was a disciple but left.
 *       The sect has mixed feelings — some respect his power, some resent his departure.</li>
 *   <li>Soul Refining Sect &amp; Wang Lin: adversarial. The Soul Refining Sect is a
 *       major antagonist in the middle arcs.</li>
 * </ul>
 *
 * <h2>Relationship Axis Values (Canon-Derived)</h2>
 * <p>Each axis is 0–100, representing the intensity of that dimension.
 * The values are NOT symmetric — Wang Lin trusts Li Muwan more than she trusts him
 * (because he's more guarded by nature).
 *
 * @see ActorRelationshipStore
 * @see dev.ergenverse.simulation.event.NpcSemanticRelationshipSubscriber
 */
public final class CanonRelationshipSeeder {

    private CanonRelationshipSeeder() {}

    /**
     * Seed canon relationships into the ActorRelationshipStore.
     * Called on first world load (when store has 0 relationships).
     * Idempotent — subsequent calls skip if already seeded.
     */
    public static void seedIfEmpty(ServerLevel level) {
        ActorRelationshipStore store = ActorRelationshipStore.get(level);
        if (store.relationshipCount() > 0) {
            Ergenverse.LOGGER.info("[CanonRelSeeder] Skipping — {} relationships already exist.",
                    store.relationshipCount());
            return;
        }

        long tick = level.getGameTime();

        // ════════════════════════════════════════════════════════════
        // WANG LIN — core relationships (the protagonist's social graph)
        // ════════════════════════════════════════════════════════════

        // Wang Lin ↔ Li Muwan
        // Canon: The central romance. Wang Lin would cross continents to save her.
        // She is his emotional anchor. Mutual high trust and deep familiarity.
        store.recordMultiAxis(
                "wang_lin", "li_muwan",
                90, 85, 0, 85, 0, 0,  // trust=90, respect=85, fear=0, familiarity=85
                "Canon: Wang Lin and Li Muwan share the deepest mutual devotion. "
                        + "Wang Lin risked everything — his cultivation, his freedom, his life — "
                        + "to save her from the Soul Refining Sect. She is his emotional anchor "
                        + "and the reason he persists through the harshest trials.",
                tick);

        // Wang Lin ↔ Qing Shui (Slaughter Celestial)
        // Canon: Master-student → peer. Qing Shui gave Wang Lin his inheritance
        // and rescued him multiple times. Deep respect, high trust.
        store.recordMultiAxis(
                "wang_lin", "qing_shui",
                75, 95, 5, 70, 3, 0,  // trust=75, respect=95, fear=5 (awe), familiarity=70
                "Canon: Qing Shui is Wang Lin's senior martial brother in all but name. "
                        + "He entrusted his life's work — the Slaughter Sword — to Wang Lin. "
                        + "Wang Lin holds him in the highest regard, bordering on reverence.",
                tick);

        // Wang Lin ↔ Situ Nan
        // Canon: Unpredictable ally. Situ Nan is chaotic but ultimately saves Wang Lin.
        // Moderate trust (he HAS betrayed before), high familiarity, some fear of his chaos.
        store.recordMultiAxis(
                "wang_lin", "situ_nan",
                40, 55, 20, 80, 2, 15,  // trust=40, respect=55, fear=20, familiarity=80
                "Canon: Situ Nan is the unpredictable wildcard. He has helped and "
                        + "hindered Wang Lin in equal measure. High familiarity from "
                        + "extended travel together, but trust is cautious — "
                        + "Situ Nan's loyalty shifts with his mood.",
                tick);

        // Wang Lin ↔ Wang Ping (adopted son)
        // Canon: Wang Lin adopted Wang Ping as a child. Unconditional paternal bond.
        store.recordMultiAxis(
                "wang_lin", "wang_ping",
                95, 40, 0, 90, 5, 0,  // trust=95, respect=40 (son, not cultivator), familiarity=90
                "Canon: Wang Ping is Wang Lin's adopted son. The relationship "
                        + "transcends cultivation politics. Wang Lin would sacrifice "
                        + "everything for his safety. Trust is absolute.",
                tick);

        // Wang Lin ↔ Teng Huayuan (Teng Clan Patriarch)
        // Canon: Extreme hostility. Teng Clan destroyed Wang Lin's family.
        // High grievance on Wang Lin's side, high fear on Teng's side.
        store.recordMultiAxis(
                "wang_lin", "teng_huayuan",
                0, 30, 0, 60, 0, 90,  // trust=0, respect=30 (acknowledges power), grievance=90
                "Canon: The Teng Clan killed Wang Lin's parents and destroyed his village. "
                        + "Wang Lin's grievance is absolute. The Teng Clan Patriarch fears "
                        + "Wang Lin's growing power but respects it.",
                tick);

        // Teng Clan's internal view of Wang Lin
        store.recordMultiAxis(
                "teng_huayuan", "wang_lin",
                0, 40, 70, 60, 0, 20,
                "Canon: The Teng Patriarch recognizes Wang Lin's terrifying potential. "
                        + "He fears what Wang Lin could do to the Teng Clan in retribution. "
                        + "Respect is grudging — he has seen Wang Lin's power firsthand.",
                tick);

        // ════════════════════════════════════════════════════════════
        // HENG YUE SECT — Wang Lin's origin sect
        // ════════════════════════════════════════════════════════════

        // Wang Lin ↔ Heng Yue Sect (as an institution)
        // Canon: Wang Lin was a disciple of the outer sect. His talent was dismissed
        // but he eventually proved exceptional. The sect has mixed feelings.
        store.recordMultiAxis(
                "wang_lin", "heng_yue_sect",
                20, 50, 5, 70, 0, 30,  // trust=20, respect=50, grievance=30
                "Canon: Wang Lin was an overlooked outer disciple of Heng Yue. "
                        + "His eventual rise proved their evaluation wrong. Some elders "
                        + "respect his power; others resent his departure. The sect "
                        + "feels both pride and loss.",
                tick);

        // Wang Lin ↔ Heng Yue Sect Elder (represented by elder canon ID)
        store.recordMultiAxis(
                "heng_yue_elder", "wang_lin",
                35, 60, 10, 55, 0, 15,
                "Canon: The elder who once dismissed Wang Lin now sees his rise "
                        + "as proof of the sect's failure to nurture talent. Mixed pride "
                        + "and regret. Moderately respects his current power.",
                tick);

        // ════════════════════════════════════════════════════════════
        // SOUL REFINING SECT — primary antagonist of middle arcs
        // ════════════════════════════════════════════════════════════

        // Wang Lin ↔ Soul Refining Sect Patriarch
        // Canon: Wang Lin infiltrated and ultimately destroyed the Soul Refining
        // Sect to save Li Muwan. Maximum hostility.
        store.recordMultiAxis(
                "wang_lin", "soul_refining_patriarch",
                0, 10, 0, 40, 0, 100,  // trust=0, respect=10, grievance=100
                "Canon: The Soul Refining Sect Patriarch captured Li Muwan, "
                        + "triggering Wang Lin's most devastating assault. The "
                        + "grievance is maximal — Wang Lin would erase the sect entirely.",
                tick);

        // Soul Refining Sect Patriarch → Wang Lin
        store.recordMultiAxis(
                "soul_refining_patriarch", "wang_lin",
                0, 25, 60, 40, 0, 80,
                "Canon: The Patriarch initially underestimated Wang Lin as a "
                        + "nobody outer disciple. After Wang Lin's assault, he fears "
                        + "his power deeply. Sees him as a dangerous anomaly.",
                tick);

        // ════════════════════════════════════════════════════════════
        // SUPPORTING CAST — Wang Family Village NPCs
        // ════════════════════════════════════════════════════════════

        // Wang Lin ↔ Old Chen (village elder)
        // Canon: Old Chen was the village elder who allowed the cultivator
        // to test village children. Complex relationship — gratitude and guilt.
        store.recordMultiAxis(
                "wang_lin", "old_chen",
                30, 20, 0, 75, 0, 25,  // trust=30, grievance=25
                "Canon: Old Chen's decision to allow the cultivator test led to "
                        + "Wang Lin's departure and family's destruction. Wang Lin "
                        + "doesn't blame Old Chen directly, but the wound remains. "
                        + "High familiarity from growing up in the village.",
                tick);

        // Old Chen → Wang Lin
        store.recordMultiAxis(
                "old_chen", "wang_lin",
                40, 60, 15, 75, 5, 0,
                "Canon: Old Chen watches Wang Lin's rise with a mixture of "
                        + "pride (a village boy became a cultivator) and guilt "
                        + "(was it right to let the cultivator test the children?).",
                tick);

        // Wang Lin ↔ Da Niu (village youth)
        // Canon: Da Niu was Wang Lin's childhood friend. Remained mortal while
        // Wang Lin ascended. Deep familiarity, moderate trust.
        store.recordMultiAxis(
                "wang_lin", "da_niu",
                55, 15, 0, 90, 3, 0,
                "Canon: Da Niu is the friend Wang Lin left behind. Their bond "
                        + "is forged in childhood innocence — before cultivation "
                        + "created an unbridgeable gap. Pure, uncomplicated loyalty.",
                tick);

        // Da Niu → Wang Lin
        store.recordMultiAxis(
                "da_niu", "wang_lin",
                65, 30, 10, 90, 0, 0,
                "Canon: Da Niu views Wang Lin with awe and brotherly love. "
                        + "He doesn't understand cultivation politics but trusts "
                        + "Wang Lin absolutely as the boy he grew up with.",
                tick);

        // Wang Lin ↔ Zhang Tian (Heng Yue recruiter)
        // Canon: Zhang Tian recruited Wang Lin to Heng Yue Sect. This is the
        // pivotal relationship that started everything.
        store.recordMultiAxis(
                "wang_lin", "zhang_tian",
                40, 50, 5, 60, 0, 0,
                "Canon: Zhang Tian recruited Wang Lin, changing his life forever. "
                        + "Whether this was a blessing or a curse depends on perspective. "
                        + "Moderate trust — Zhang Tian was just doing his job.",
                tick);

        // ════════════════════════════════════════════════════════════
        // CROSS-FACTION RELATIONSHIPS (not involving Wang Lin)
        // ════════════════════════════════════════════════════════════

        // Li Muwan ↔ Qing Shui
        // Canon: Li Muwan and Qing Shui are contemporaries. Qing Shui
        // respects her alchemical talent. Professional respect.
        store.recordMultiAxis(
                "li_muwan", "qing_shui",
                50, 70, 5, 55, 0, 0,
                "Canon: Li Muwan is an exceptional alchemist. Qing Shui, "
                        + "as a senior cultivator, respects her expertise. "
                        + "Their relationship is collegial — mutual professional respect.",
                tick);

        // Li Muwan ↔ Soul Refining Sect Patriarch
        // Canon: The Patriarch captured Li Muwan to use her alchemical talent.
        // Maximum hostility from her side.
        store.recordMultiAxis(
                "li_muwan", "soul_refining_patriarch",
                0, 0, 80, 30, 0, 95,
                "Canon: Li Muwan was imprisoned and exploited by the Soul "
                        + "Refining Sect Patriarch. The trauma is searing. "
                        + "She fears him deeply and carries immense grievance.",
                tick);

        // Heng Yue Sect ↔ Teng Family City
        // Canon: Neighboring powers with a complex political relationship.
        // Neither trusts the other fully. Mutual wariness.
        store.recordMultiAxis(
                "heng_yue_sect", "teng_huayuan",
                25, 40, 15, 50, 0, 10,
                "Canon: Heng Yue and the Teng Clan coexist in Zhao Country "
                        + "with an uneasy peace. Neither fully trusts the other. "
                        + "Professional respect for each other's power.",
                tick);

        // Wang Ping ↔ Zhou Tingsu (village girl)
        // Canon: Childhood sweethearts in Wang Family Village.
        // Pure, innocent bond.
        store.recordMultiAxis(
                "wang_ping", "zhou_tingsu",
                60, 10, 0, 85, 0, 0,
                "Canon: Wang Ping and Zhou Tingsu are childhood friends from "
                        + "the village. Their bond is innocent and unaffected by "
                        + "cultivation politics.",
                tick);

        // Old Chen ↔ Da Niu (village elder & village youth)
        // Canon: Elder-villager relationship. Mentorship and duty.
        store.recordMultiAxis(
                "old_chen", "da_niu",
                45, 10, 0, 80, 0, 0,
                "Canon: Old Chen is the village elder who looks after all "
                        + "the young people. Da Niu is one of the village's most "
                        + "reliable youths. Familiarity from daily village life.",
                tick);

        // ════════════════════════════════════════════════════════════
        // SOUL REFINING SECT INTERNAL POLITICS
        // ════════════════════════════════════════════════════════════

        // Soul Refining Sect Patriarch ↔ Teng Huayuan
        // Canon: The Teng Clan and Soul Refining Sect have an alliance
        // of convenience — both oppose Wang Lin's rise.
        store.recordMultiAxis(
                "soul_refining_patriarch", "teng_huayuan",
                50, 45, 5, 55, 5, 0,
                "Canon: The Soul Refining Sect and Teng Clan share a common "
                        + "enemy in Wang Lin. Their alliance is pragmatic, not "
                        + "friendly. Mutual wariness even among allies.",
                tick);

        // ════════════════════════════════════════════════════════════
        // ZHOU RUI & ZHANG TIAN (chapter 1 NPCs)
        // ════════════════════════════════════════════════════════════

        // Zhou Rui (Teng servant) ↔ Zhang Tian (Heng Yue recruiter)
        // Canon: Different factions. Zhou Rui serves the Teng family.
        // Zhang Tian recruits for Heng Yue. Political tension.
        store.recordMultiAxis(
                "zhou_rui", "zhang_tian",
                15, 20, 10, 30, 0, 5,
                "Canon: Zhou Rui serves the Teng Family while Zhang Tian "
                        + "recruits for Heng Yue. Their factions are rivals in "
                        + "Zhao Country. Cautious, politically aware interaction.",
                tick);

        // ════════════════════════════════════════════════════════════
        // WANG QINGYUE (Wang Lin's childhood acquaintance)
        // ════════════════════════════════════════════════════════════

        // Wang Qingyue ↔ Old Chen
        // Canon: Village neighbors. Everyday familiarity.
        store.recordMultiAxis(
                "wang_qingyue", "old_chen",
                40, 25, 0, 80, 0, 0,
                "Canon: Wang Qingyue is a village youth who grew up under "
                        + "Old Chen's watch. Standard village elder-resident relationship.",
                tick);

        Ergenverse.LOGGER.info("[CanonRelSeeder] Seeded {} canon NPC-to-NPC relationships "
                + "into ActorRelationshipStore. These relationships provide the foundation "
                + "for the event-sourced architecture: all future relationship changes "
                + "(via NpcSemanticRelationshipSubscriber, RelationshipEngine, "
                + "WangLinSemanticSubscriber) will build upon these canon seeds.",
                store.relationshipCount());
    }
}
