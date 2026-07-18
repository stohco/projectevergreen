package dev.ergenverse.perception;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.cultivation.RealmId;

import java.util.Map;

/**
 * The Perception Engine — produces <b>understanding</b> of objective reality.
 *
 * <p>This engine does NOT change the world. It does NOT swap models. It
 * does NOT spawn things because the player leveled up.
 *
 * <p>What it does: given an {@link Objective} thing and an
 * {@link ObserverContext}, produce a {@link PerceptionResult} describing
 * what <b>that observer</b> understands about <b>that objective thing</b>.
 *
 * <h2>The Prime Directive (from {@link WorldPhilosophy})</h2>
 *
 * <blockquote>
 *   Never hide or reveal objects because of the player's level. Hide or
 *   reveal interactions according to the laws of the world. The world is
 *   objective and exists independently of the player. Cultivation increases
 *   a character's ability to perceive, understand, and interact with deeper
 *   layers of reality — it does not create or replace reality.
 * </blockquote>
 *
 * <h2>The Rabbit Example (done right)</h2>
 *
 * <p>Consider a Fifth Rank Spirit Rabbit. Its {@link ObjectiveNature} is:
 * <pre>
 *   kind       = SPIRIT_BEAST
 *   trueName   = "Spirit Rabbit"
 *   trueRank   = 5
 *   trueRealm  = SOUL_FORMATION
 *   bloodline  = "Divine Hare (extinct)"
 *   origin     = "descended from Meng Hao's Paragon Spirit Deer"
 *   karmicHistory = "killed three cultivators who tried to harvest it; spared by a Nascent Soul who passed by"
 * </pre>
 *
 * <p>This rabbit is the same rabbit to all observers. What changes is the
 * observer's understanding:
 *
 * <ul>
 *   <li><b>Mortal</b> — perceives a "very large rabbit." Thinks it's odd.
 *       Cannot interact (the rabbit's spiritual nature means a mortal's
 *       hands only touch the physical shadow). If the mortal attacks it,
 *       the rabbit either flees (it has no interest in mortals) or, if
 *       cornered, releases its aura and the mortal dies of fright.</li>
 *   <li><b>Qi Condensation</b> — perceives "Spirit Beast. Leave. Now."
 *       Recognizes it's a spirit beast but cannot tell the rank. Knows
 *       to flee.</li>
 *   <li><b>Foundation, beast-tamer specialization</b> — perceives "Spirit
 *       Rabbit, bloodline unusually pure." The beast-tamer specialization
 *       reveals bloodline quality that a regular Foundation cultivator
 *       misses.</li>
 *   <li><b>Nascent Soul, divine sense sweep</b> — perceives the full
 *       nature: "Fifth Rank Spirit Rabbit, Divine Hare bloodline, three
 *       kills in its karmic history." Can interact (Foundation is enough
 *       to interact; Nascent Soul is enough to fully understand).</li>
 *   <li><b>Fourth Step</b> — perceives "Its ancestor lineage traces back
 *       to an extinct divine hare. It is the last echo of Meng Hao's
 *       Paragon Spirit Deer bloodline."</li>
 * </ul>
 *
 * <p>The rabbit never changed. The observer's understanding did.
 */
public final class PerceptionEngine {

    private static volatile boolean bootstrapped = false;

    private PerceptionEngine() {}

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        Ergenverse.LOGGER.info("[Ergenverse] Perception Engine bootstrapped — understanding over objective reality.");
        Ergenverse.LOGGER.info("[Ergenverse] Prime Directive: {}", WorldPhilosophy.PRIME_DIRECTIVE);
    }

    /**
     * Perceive an objective thing as the given observer.
     *
     * <p>This is the core function of the engine. The objective thing is
     * unchanged; what's returned is the observer's understanding.
     */
    public static PerceptionResult perceive(Objective thing, ObserverContext observer) {
        ObjectiveNature nature = thing.nature();
        PerceptionTier tier = observer.effectiveTier();

        // ── Concealment check ──
        // Concealment is a property of the thing, not the observer.
        // The thing has a concealment formation (or Dao) with a
        // perception floor. If the observer's tier is below that floor,
        // the thing is concealed FROM THEM — but it still exists.
        int concealmentFloor = concealmentFloor(nature);
        if (tier.order < concealmentFloor) {
            // The observer cannot resolve the thing at all. They may
            // sense "something" (a mortal feels a chill; a Qi cultivator
            // feels Qi drifting from a wall), but they cannot perceive
            // the thing itself.
            return PerceptionResult.concealed(tier);
        }

        // ── Build understanding based on tier and specialization ──
        return buildUnderstanding(nature, observer, tier);
    }

    /**
     * What is the perception floor required to perceive this thing at all?
     *
     * <p>Most things have floor 0 (mortals can see them). A formation
     * anchored in the spiritual layer has floor 1 (Qi Condensation). A
     * concealed inheritance vault has floor 2-3. A Dao imprint has
     * floor 4. A heavenly law has floor 5.
     */
    private static int concealmentFloor(ObjectiveNature nature) {
        switch (nature.kind) {
            case MORTAL_ENTITY:
            case MORTAL_OBJECT: return 0;            // everyone sees these
            case SPIRIT_BEAST: return 0;             // spirit beasts have a physical form — mortals see them
                                                     // (they just don't understand what they are)
            case SPIRIT_HERB: return 1;              // herbs without concealment: Qi-sense reveals them
                                                     // (concealed herbs have their own formation — handled separately)
            case SPIRIT_VEIN: return 1;              // Qi-sense reveals spirit veins
            case FORMATION: return 2;                // Foundation required to even see a formation exists
            case CONCEALMENT_ARRAY: return 2;        // Foundation required to identify a concealment array
            case INHERITANCE_VAULT: return 3;        // Nascent Soul to perceive a sealed vault
            case RUIN: return 1;                     // Qi-sense to identify a ruin as more than rubble
            case KARMIC_TRACE: return 3;             // Nascent Soul to see karmic traces
            case DAO_IMPRINT: return 4;              // Soul Formation to perceive Dao imprints
            case HEAVENLY_LAW: return 5;             // Ascendant to perceive heavenly laws
            default: return 0;
        }
    }

    /**
     * Build the observer's understanding of the thing.
     *
     * <p>This is where the rabbit example lives. The thing's nature is
     * fixed; we vary the description based on observer tier and
     * specialization.
     */
    private static PerceptionResult buildUnderstanding(ObjectiveNature nature,
                                                       ObserverContext observer,
                                                       PerceptionTier tier) {
        // ── Reality level (canon filter) ──
        CanonEngine.RealityLevel reality = CanonEngine.confidenceToReality(nature.canonConfidence);

        switch (nature.kind) {
            case SPIRIT_BEAST:
                return perceiveSpiritBeast(nature, observer, tier, reality);
            case SPIRIT_HERB:
                return perceiveSpiritHerb(nature, observer, tier, reality);
            case FORMATION:
            case CONCEALMENT_ARRAY:
                return perceiveFormation(nature, observer, tier, reality);
            case SPIRIT_VEIN:
                return perceiveSpiritVein(nature, observer, tier, reality);
            case KARMIC_TRACE:
                return perceiveKarmicTrace(nature, observer, tier, reality);
            case DAO_IMPRINT:
                return perceiveDaoImprint(nature, observer, tier, reality);
            case HEAVENLY_LAW:
                return perceiveHeavenlyLaw(nature, observer, tier, reality);
            case INHERITANCE_VAULT:
                return perceiveInheritanceVault(nature, observer, tier, reality);
            case RUIN:
                return perceiveRuin(nature, observer, tier, reality);
            case CULTIVATOR:
                return perceiveCultivatorEntity(nature, observer, tier, reality);
            case MORTAL_ENTITY:
            case MORTAL_OBJECT:
            default:
                return new PerceptionResult(
                    nature.trueName,
                    "An ordinary " + nature.trueName.toLowerCase() + ".",
                    null, true, false, CanonEngine.RealityLevel.REALITY, tier
                );
        }
    }

    // ── Cultivator entity perception — the core of Task (7) ───────

    /**
     * How different observers perceive a cultivator NPC.
     *
     * <p>Per the Prime Directive: the cultivator's objective nature is fixed.
     * What changes is the observer's understanding.
     *
     * <p>Example — a Soul Formation cultivator named "Wang Tiangui" (actually
     * a disguised ancient patriarch):
     * <ul>
     *   <li><b>Mortal</b>: "a tired old man" — no cultivation info visible.</li>
     *   <li><b>Qi Condensation</b>: "Wang Tiangui" + "(faint Qi presence)" —
     *       recognizes something unusual but can't identify the realm.</li>
     *   <li><b>Foundation</b>: "Wang Tiangui" + "Qi Condensation?" —
     *       can estimate approximate realm if the entity is not
     *       actively suppressing.</li>
     *   <li><b>Core Formation</b>: "Wang Tiangui (Core Formation)" —
     *       precise realm identification. The observer is at Core Formation
     *       themselves, so they can recognize peers.</li>
     *   <li><b>Nascent Soul</b>: full name + realm + karmic trace —
     *       divine sense reads the entity's karmic history.</li>
     *   <li><b>Soul Formation</b>: full name + realm + sect + Dao affinities —
     *       can see the Dao paths the entity has comprehended.</li>
     *   <li><b>Ascendant+</b>: everything including titles, bloodline, origin,
     *       and the cosmic significance of their existence.</li>
     * </ul>
     *
     * <p>Realm suppression: if the entity's true realm is higher than the
     * observer's, the observer's estimate is less accurate. A Foundation
     * cultivator looking at a Soul Formation entity sees "above my
     * perception" rather than the true realm.
     */
    private static PerceptionResult perceiveCultivatorEntity(ObjectiveNature nature,
                                                              ObserverContext observer,
                                                              PerceptionTier tier,
                                                              CanonEngine.RealityLevel reality) {
        String name = nature.trueName;
        String nameCn = nature.trueNameCn;
        RealmId entityRealm = nature.trueRealm;

        // Whether the entity is actively suppressing (concealing their cultivation)
        // Concealment floor for cultivators: same as their perception tier.
        // An entity whose realm tier exceeds the observer's is "concealed."
        PerceptionTier entityPerceptionTier = PerceptionTier.fromRealm(entityRealm);
        boolean realmSuppressed = entityPerceptionTier.order > tier.order + 1;

        switch (tier) {
            case MORTAL:
                // Mortals see a mundane person. They cannot sense Qi at all.
                // The cultivator is physically there but the mortal doesn't
                // understand what they're looking at.
                String mortalName = toMundaneDescription(name);
                return new PerceptionResult(
                    mortalName,
                    "A " + mortalName + ". There is something about their bearing that feels unusual, but you cannot place it.",
                    null, false, false, reality, tier
                );

            case QI_CONDENSATION:
                // Qi Condensation can sense Qi but cannot precisely identify realm.
                // They know this person is a cultivator, but not how strong.
                if (realmSuppressed) {
                    return new PerceptionResult(
                        name,
                        "A cultivator. Their Qi is beyond your ability to measure. Be cautious.",
                        null, false, false, reality, tier
                    );
                }
                return new PerceptionResult(
                    name,
                    "A cultivator with faint Qi presence. You sense cultivation energy, but cannot determine their exact realm.",
                    null, false, false, reality, tier
                );

            case FOUNDATION:
                // Foundation can estimate approximate realm for entities at or
                // below their perception. Higher-realm entities are "above."
                if (realmSuppressed) {
                    return new PerceptionResult(
                        name,
                        "A cultivator whose cultivation far exceeds your own. Your divine sense cannot penetrate their Qi concealment.",
                        null, false, false, reality, tier
                    );
                }
                // Estimate realm: Foundation can identify up to Core Formation
                String estimatedRealm = entityRealm == RealmId.MORTAL
                    ? "no cultivation detected"
                    : "approximately " + entityRealm.name;
                return new PerceptionResult(
                    name + " (" + estimatedRealm + ")",
                    "A cultivator at " + estimatedRealm + ". You can sense their cultivation aura clearly."
                        + (nature.sect.isEmpty() ? "" : " Faint traces suggest affiliation with " + nature.sect + "."),
                    nature.trueRank, true, false, reality, tier
                );

            case NASCENT_SOUL:
                // Nascent Soul identifies realm precisely + reads karmic trace
                if (realmSuppressed) {
                    return new PerceptionResult(
                        name + " (realm obscured)",
                        "This cultivator's true realm exceeds your perception. They are at least " + entityRealm.name + "."
                            + (nature.sect.isEmpty() ? "" : " Sect aura: " + nature.sect + "."),
                        null, false, false, reality, tier
                    );
                }
                StringBuilder nsDesc = new StringBuilder();
                nsDesc.append("A cultivator at ").append(entityRealm.name).append(".");
                if (!nature.sect.isEmpty()) {
                    nsDesc.append(" Sect: ").append(nature.sect).append(".");
                }
                if (!nature.titles.isEmpty()) {
                    nsDesc.append(" Known as: ").append(nature.titles).append(".");
                }
                if (!nature.karmicHistory.isEmpty()) {
                    nsDesc.append(" Karmic trace: ").append(nature.karmicHistory).append(".");
                }
                return new PerceptionResult(
                    name + " (" + entityRealm.name + ")",
                    nsDesc.toString(),
                    nature.trueRank, true, false, reality, tier
                );

            case SOUL_FORMATION:
                // Soul Formation sees full info including Dao affinities
                if (realmSuppressed) {
                    return new PerceptionResult(
                        name + " (" + entityRealm.name + ")",
                        "A cultivator at " + entityRealm.name + ". Their Dao aura is impenetrable."
                            + (nature.sect.isEmpty() ? "" : " Sect: " + nature.sect + ".")
                            + (nature.karmicHistory.isEmpty() ? "" : " Karmic trace: " + nature.karmicHistory + "."),
                        nature.trueRank, true, false, reality, tier
                    );
                }
                StringBuilder sfDesc = new StringBuilder();
                sfDesc.append("A cultivator at ").append(entityRealm.name).append(".");
                if (!nature.sect.isEmpty()) {
                    sfDesc.append(" Sect: ").append(nature.sect).append(".");
                }
                if (!nature.titles.isEmpty()) {
                    sfDesc.append(" Title: ").append(nature.titles).append(".");
                }
                if (!nature.daoAffinities.isEmpty()) {
                    sfDesc.append(" Dao Affinities: ").append(nature.daoAffinities).append(".");
                }
                if (!nature.karmicHistory.isEmpty()) {
                    sfDesc.append(" Karmic history: ").append(nature.karmicHistory).append(".");
                }
                if (!nature.origin.isEmpty()) {
                    sfDesc.append(" Origin: ").append(nature.origin).append(".");
                }
                return new PerceptionResult(
                    name + " (" + entityRealm.name + ")" + (nature.sect.isEmpty() ? "" : " [" + nature.sect + "]"),
                    sfDesc.toString(),
                    nature.trueRank, true, false, reality, tier
                );

            case ASCENDANT:
            case TRANSCENDENCE:
                // Highest tiers see everything, including bloodline and cosmic context
                StringBuilder highDesc = new StringBuilder();
                highDesc.append("A cultivator at ").append(entityRealm.name).append(".");
                if (!nature.sect.isEmpty()) {
                    highDesc.append(" Sect: ").append(nature.sect).append(".");
                }
                if (!nature.titles.isEmpty()) {
                    highDesc.append(" Titles: ").append(nature.titles).append(".");
                }
                if (!nature.daoAffinities.isEmpty()) {
                    highDesc.append(" Dao Affinities: ").append(nature.daoAffinities).append(".");
                }
                if (!nature.bloodline.isEmpty()) {
                    highDesc.append(" Bloodline: ").append(nature.bloodline).append(".");
                }
                if (!nature.origin.isEmpty()) {
                    highDesc.append(" Origin: ").append(nature.origin).append(".");
                }
                if (!nature.karmicHistory.isEmpty()) {
                    highDesc.append(" Karmic history visible: ").append(nature.karmicHistory).append(".");
                }
                if (tier == PerceptionTier.TRANSCENDENCE) {
                    highDesc.append(" This being's existence ripples through the karmic web of the cosmos.");
                }
                return new PerceptionResult(
                    name + " (" + entityRealm.name + ")" + buildFullTitle(nature),
                    highDesc.toString(),
                    nature.trueRank, true, false,
                    tier == PerceptionTier.TRANSCENDENCE ? CanonEngine.RealityLevel.REALITY : reality,
                    tier
                );

            default:
                return new PerceptionResult(name, "A cultivator.", nature.trueRank, true, false, reality, tier);
        }
    }

    /** Build a full title suffix for high-tier perception display. */
    private static String buildFullTitle(ObjectiveNature nature) {
        StringBuilder sb = new StringBuilder();
        if (!nature.titles.isEmpty()) {
            sb.append(" — ").append(nature.titles);
        } else if (!nature.sect.isEmpty()) {
            sb.append(" [").append(nature.sect).append("]");
        }
        return sb.toString();
    }

    /**
     * Generate a mundane description of a cultivator's name.
     * Used when the observer is a mortal who cannot sense Qi at all.
     *
     * <p>Uses heuristic: if the name contains known canon terms, generate
     * a mundane equivalent. Otherwise, produce a generic description.
     * The mortal's inability to sense Qi means they see only the
     * physical appearance — an old man, a stern elder, a child.
     */
    private static String toMundaneDescription(String name) {
        if (name == null || name.isEmpty()) return "A person";
        String lower = name.toLowerCase();
        // Wang Lin's father
        if (lower.contains("wang tiangui") || lower.contains("王天贵")) return "A tired old man";
        if (lower.contains("wang lin") || lower.contains("王林")) return "A young man with calm eyes";
        if (lower.contains("teng")) return "An imposing noble";
        if (lower.contains("liu") || lower.contains("刘")) return "A stern-looking person";
        if (lower.contains("qiu") || lower.contains("丘")) return "A stern elder";
        if (lower.contains("situ") || lower.contains("司徒")) return "A strange old man with an unsettling gaze";
        if (lower.contains("zhang") || lower.contains("张")) return "A middle-aged man";
        if (lower.contains("xu") || lower.contains("许")) return "An ordinary-looking person";
        // Sect heads / patriarchs tend to look imposing to mortals
        if (lower.contains("patriarch") || lower.contains("sect head") || lower.contains("elder"))
            return "An elder with an imposing bearing";
        // Default: just "A person"
        return "A person";
    }

    // ── Spirit beast perception — the rabbit example ─────────────────

    private static PerceptionResult perceiveSpiritBeast(ObjectiveNature nature,
                                                        ObserverContext observer,
                                                        PerceptionTier tier,
                                                        CanonEngine.RealityLevel reality) {
        // Specialization: beast-tamer at Foundation+ recognizes bloodline
        boolean beastTamerReveal = observer.isBeastTamer && tier.order >= PerceptionTier.FOUNDATION.order;

        switch (tier) {
            case MORTAL:
                // The mortal SEES the spirit beast (it has physical form)
                // but does not understand what it is.
                return new PerceptionResult(
                    "a large " + nature.trueName.toLowerCase(),
                    "That's the biggest " + nature.trueName.toLowerCase() + " you've ever seen. There's something unsettling about the way it watches you.",
                    null, false, false, reality, tier
                );

            case QI_CONDENSATION:
                // Qi-sense recognizes it as a spirit beast, but cannot
                // determine rank or bloodline.
                return new PerceptionResult(
                    "Spirit Beast (" + nature.trueName + ")",
                    "Spirit Beast. You sense Qi radiating from it. You cannot tell its exact rank, but you know to leave. Now.",
                    null, false, false, reality, tier
                );

            case FOUNDATION:
                // Foundation can determine approximate rank.
                if (beastTamerReveal) {
                    return new PerceptionResult(
                        nature.trueName + ", Rank " + nature.trueRank,
                        "A " + nature.trueName + " of Rank " + nature.trueRank + ". Its bloodline is unusually pure" + (nature.bloodline.isEmpty() ? "." : " — you can sense traces of " + nature.bloodline + "."),
                        nature.trueRank, true, false, reality, tier
                    );
                }
                return new PerceptionResult(
                    nature.trueName + " (Rank ~" + nature.trueRank + ")",
                    "A " + nature.trueName + ". Its cultivation is roughly equivalent to " + nature.trueRealm.name + ". You can interact with it — carefully.",
                    nature.trueRank, true, false, reality, tier
                );

            case NASCENT_SOUL:
                // Nascent Soul sees karmic history via divine sense.
                return new PerceptionResult(
                    nature.trueName + ", Rank " + nature.trueRank + (nature.bloodline.isEmpty() ? "" : " (" + nature.bloodline + ")"),
                    "A " + nature.trueName + " of Rank " + nature.trueRank + "." +
                        (nature.bloodline.isEmpty() ? "" : " Bloodline: " + nature.bloodline + ".") +
                        (nature.karmicHistory.isEmpty() ? "" : " Karmic history: " + nature.karmicHistory + "."),
                    nature.trueRank, true, false, reality, tier
                );

            case SOUL_FORMATION:
            case ASCENDANT:
                // Higher tiers perceive the full origin lineage.
                return new PerceptionResult(
                    nature.trueName + ", Rank " + nature.trueRank + " — " + nature.origin,
                    "A " + nature.trueName + " of Rank " + nature.trueRank + ". " +
                        (nature.bloodline.isEmpty() ? "" : "Bloodline: " + nature.bloodline + ". ") +
                        "Origin: " + nature.origin + ". " +
                        (nature.karmicHistory.isEmpty() ? "" : "Karmic history visible: " + nature.karmicHistory + "."),
                    nature.trueRank, true, false, reality, tier
                );

            case TRANSCENDENCE:
                // Transcendent sees the cosmic significance.
                return new PerceptionResult(
                    nature.trueName + " — cosmic echo",
                    "This " + nature.trueName + " is " + nature.origin + ". Its bloodline (" + (nature.bloodline.isEmpty() ? "ordinary" : nature.bloodline) + ") is a thread in the Root Dao's karmic web. Killing it would unravel connections you cannot foresee. Sparing it may plant seeds you will reap in ten thousand years.",
                    nature.trueRank, true, false, CanonEngine.RealityLevel.REALITY, tier
                );

            default:
                return new PerceptionResult(nature.trueName, "A " + nature.trueName + ".",
                    null, true, false, reality, tier);
        }
    }

    // ── Spirit herb perception ────────────────────────────────────────

    private static PerceptionResult perceiveSpiritHerb(ObjectiveNature nature,
                                                       ObserverContext observer,
                                                       PerceptionTier tier,
                                                       CanonEngine.RealityLevel reality) {
        boolean alchemistReveal = observer.isAlchemist && tier.order >= PerceptionTier.FOUNDATION.order;

        switch (tier) {
            case MORTAL:
                // Mortals see a spirit herb as an ordinary weed — IF it
                // has no concealment. (Concealed herbs are handled by the
                // concealment floor check above.)
                return new PerceptionResult(
                    "an ordinary weed",
                    "A weed. You'd pull it, but it doesn't seem worth the effort.",
                    null, false, false, reality, tier
                );

            case QI_CONDENSATION:
                return new PerceptionResult(
                    "Spirit Herb (" + nature.trueName + ")",
                    "A Spirit Herb. You can sense faint Qi within it. You don't know its grade or properties.",
                    null, true, false, reality, tier
                );

            case FOUNDATION:
                if (alchemistReveal) {
                    return new PerceptionResult(
                        nature.trueName + ", Grade " + nature.trueRank,
                        "A " + nature.trueName + " of Grade " + nature.trueRank + ". Origin: " + nature.origin + ". Valuable to alchemists.",
                        nature.trueRank, true, false, reality, tier
                    );
                }
                return new PerceptionResult(
                    nature.trueName + " (Grade ~" + nature.trueRank + ")",
                    "A " + nature.trueName + ". Its grade is roughly " + nature.trueRank + ". You can harvest it.",
                    nature.trueRank, true, false, reality, tier
                );

            default:
                return new PerceptionResult(
                    nature.trueName + ", Grade " + nature.trueRank,
                    "A " + nature.trueName + " of Grade " + nature.trueRank + ". Origin: " + nature.origin + ".",
                    nature.trueRank, true, false, reality, tier
                );
        }
    }

    // ── Formation perception ──────────────────────────────────────────

    private static PerceptionResult perceiveFormation(ObjectiveNature nature,
                                                      ObserverContext observer,
                                                      PerceptionTier tier,
                                                      CanonEngine.RealityLevel reality) {
        boolean formationMaster = observer.isFormationMaster && tier.order >= PerceptionTier.FOUNDATION.order;

        switch (tier) {
            case MORTAL:
            case QI_CONDENSATION:
                // Mortals and Qi cultivators cannot perceive formations at all.
                // (They're in the Spiritual Layer.)
                return PerceptionResult.concealed(tier);

            case FOUNDATION:
                if (formationMaster) {
                    return new PerceptionResult(
                        nature.trueName + " (Tier " + nature.trueRank + ")",
                        "A " + nature.trueName + " of Tier " + nature.trueRank + ". Created by a " + nature.trueRealm.name + " cultivator. You can attempt to break it, but the cost will be significant.",
                        nature.trueRank, true, false, reality, tier
                    );
                }
                return new PerceptionResult(
                    "a formation",
                    "There is a formation here. You can sense its presence but cannot identify its type or tier.",
                    null, false, false, reality, tier
                );

            case NASCENT_SOUL:
                return new PerceptionResult(
                    nature.trueName + " (Tier " + nature.trueRank + ")",
                    "A " + nature.trueName + " of Tier " + nature.trueRank + ", created by a " + nature.trueRealm.name + " cultivator. " + nature.origin + ".",
                    nature.trueRank, true, false, reality, tier
                );

            default:
                return new PerceptionResult(
                    nature.trueName + " — full structure visible",
                    "A " + nature.trueName + " of Tier " + nature.trueRank + ". Its full structure is visible to you. Origin: " + nature.origin + ". You can dismantle it, repair it, or repurpose it.",
                    nature.trueRank, true, false, reality, tier
                );
        }
    }

    // ── Spirit vein perception ────────────────────────────────────────

    private static PerceptionResult perceiveSpiritVein(ObjectiveNature nature,
                                                       ObserverContext observer,
                                                       PerceptionTier tier,
                                                       CanonEngine.RealityLevel reality) {
        switch (tier) {
            case MORTAL:
                // Mortals feel the spiritual pressure but can't identify it.
                return new PerceptionResult(
                    "a strange feeling",
                    "The air here feels different. Cleaner. Heavier. You can't explain why.",
                    null, false, false, reality, tier
                );

            case QI_CONDENSATION:
                return new PerceptionResult(
                    "Spirit Vein (faint)",
                    "A Spirit Vein pulses beneath the ground here. You can meditate near it to accelerate your cultivation.",
                    null, true, false, reality, tier
                );

            default:
                return new PerceptionResult(
                    "Spirit Vein, Rank " + nature.trueRank,
                    "A Rank " + nature.trueRank + " Spirit Vein pulses beneath the ground. Origin: " + nature.origin + ".",
                    nature.trueRank, true, false, reality, tier
                );
        }
    }

    // ── Karmic trace perception ───────────────────────────────────────

    private static PerceptionResult perceiveKarmicTrace(ObjectiveNature nature,
                                                        ObserverContext observer,
                                                        PerceptionTier tier,
                                                        CanonEngine.RealityLevel reality) {
        // Karmic traces are only visible at Nascent Soul+.
        if (tier.order < PerceptionTier.NASCENT_SOUL.order) {
            return PerceptionResult.concealed(tier);
        }
        return new PerceptionResult(
            "Karmic Trace",
            nature.karmicHistory + " (visible at " + tier.label + ")",
            null, true, false, reality, tier
        );
    }

    // ── Dao imprint perception ────────────────────────────────────────

    private static PerceptionResult perceiveDaoImprint(ObjectiveNature nature,
                                                       ObserverContext observer,
                                                       PerceptionTier tier,
                                                       CanonEngine.RealityLevel reality) {
        if (tier.order < PerceptionTier.SOUL_FORMATION.order) {
            return PerceptionResult.concealed(tier);
        }
        return new PerceptionResult(
            "Dao Imprint (" + nature.origin + ")",
            "An ancient Dao imprint lingers here. Origin: " + nature.origin + ". Meditating here may grant comprehension.",
            null, true, false, reality, tier
        );
    }

    // ── Heavenly law perception ───────────────────────────────────────

    private static PerceptionResult perceiveHeavenlyLaw(ObjectiveNature nature,
                                                        ObserverContext observer,
                                                        PerceptionTier tier,
                                                        CanonEngine.RealityLevel reality) {
        if (tier.order < PerceptionTier.ASCENDANT.order) {
            return PerceptionResult.concealed(tier);
        }
        return new PerceptionResult(
            "Heavenly Law",
            nature.origin + ". This law shapes the world itself — to perceive it is to begin to grasp the cosmos.",
            null, true, false, reality, tier
        );
    }

    // ── Inheritance vault perception ──────────────────────────────────

    private static PerceptionResult perceiveInheritanceVault(ObjectiveNature nature,
                                                              ObserverContext observer,
                                                              PerceptionTier tier,
                                                              CanonEngine.RealityLevel reality) {
        switch (tier) {
            case MORTAL:
            case QI_CONDENSATION:
                return new PerceptionResult(
                    "a cold wind",
                    "A cold wind blows from somewhere nearby. You can't find its source.",
                    null, false, true, reality, tier
                );
            case FOUNDATION:
                return new PerceptionResult(
                    "Qi flowing into the cliff",
                    "Qi is flowing into the cliff here, as if something beneath is drinking it in.",
                    null, false, true, reality, tier
                );
            case NASCENT_SOUL:
                return new PerceptionResult(
                    "a concealed entrance",
                    "There is a concealed entrance here, hidden by a formation. You cannot break it alone.",
                    null, false, true, reality, tier
                );
            default:
                return new PerceptionResult(
                    "Inheritance Vault (Tier " + nature.trueRank + ")",
                    "An Inheritance Vault of Tier " + nature.trueRank + ", created by a " + nature.trueRealm.name + " cultivator. Origin: " + nature.origin + ".",
                    nature.trueRank, true, false, reality, tier
                );
        }
    }

    // ── Ruin perception ───────────────────────────────────────────────

    private static PerceptionResult perceiveRuin(ObjectiveNature nature,
                                                  ObserverContext observer,
                                                  PerceptionTier tier,
                                                  CanonEngine.RealityLevel reality) {
        switch (tier) {
            case MORTAL:
                return new PerceptionResult(
                    "old rubble",
                    "Old rubble. The stones are weathered beyond recognition.",
                    null, true, false, reality, tier
                );
            case QI_CONDENSATION:
                return new PerceptionResult(
                    "ancient ruins",
                    "Ancient ruins. The stones still carry faint traces of Qi — this was once a place of cultivation.",
                    null, true, false, reality, tier
                );
            default:
                return new PerceptionResult(
                    "Ruin of " + nature.origin,
                    "Ruins of " + nature.origin + ". Created by a " + nature.trueRealm.name + " cultivator. " + nature.origin + ".",
                    null, true, false, reality, tier
                );
        }
    }
}
