package dev.ergenverse.perception.observation;

import dev.ergenverse.perception.PerceptionTier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ObservationChain — a linked set of phenomena that, when all noticed by a
 * player, yield emergent understanding.
 *
 * <p>This is the "Observation Chains" mechanic from the design vision:
 * <pre>
 *   Player notices birds.
 *   ↓
 *   Birds avoid valley.
 *   ↓
 *   Valley unusually quiet.
 *   ↓
 *   No insects. No beasts.
 *   ↓
 *   Spirit pressure.
 *   ↓
 *   Ancient cultivator cave.
 * </pre>
 *
 * <p>That entire chain emerges naturally. Nothing says "Quest Started." The
 * player simply notices phenomena, and when enough align, understanding
 * emerges — granted as a {@link KnowledgeTag} and a subtle insight message.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Chains are NEVER announced. No "Chain Started: Ancient Cultivator Cave."
 *   The player notices a bird. Then silence. Then pressure. Then they
 *   <i>understand</i>. The understanding arrives as a whisper, not a banner.
 * </blockquote>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public final class ObservationChain {

    public final String chainId;
    public final String label;
    public final List<ObservationPhenomenon.Kind> requiredPhenomenonKinds;
    public final PerceptionTier minPerceptionTier;
    public final KnowledgeTag grantsKnowledgeTag;
    public final String emergentUnderstanding;
    public final String discoveryHint;  // optional — where to look next (null = no hint)

    public ObservationChain(String chainId, String label, List<ObservationPhenomenon.Kind> requiredKinds,
                            PerceptionTier minTier, KnowledgeTag grantsTag,
                            String understanding, String discoveryHint) {
        this.chainId = chainId;
        this.label = label;
        this.requiredPhenomenonKinds = Collections.unmodifiableList(new ArrayList<>(requiredKinds));
        this.minPerceptionTier = minTier;
        this.grantsKnowledgeTag = grantsTag;
        this.emergentUnderstanding = understanding;
        this.discoveryHint = discoveryHint;
    }

    /**
     * Is this chain complete given the set of phenomenon kinds the player has noticed?
     * Order does not matter — the player notices phenomena as they encounter them.
     */
    public boolean isComplete(Set<ObservationPhenomenon.Kind> noticedKinds) {
        for (ObservationPhenomenon.Kind required : requiredPhenomenonKinds) {
            if (!noticedKinds.contains(required)) return false;
        }
        return true;
    }

    /** How many of the required kinds has the player noticed? (0 to requiredKinds.size()) */
    public int progressCount(Set<ObservationPhenomenon.Kind> noticedKinds) {
        int count = 0;
        for (ObservationPhenomenon.Kind required : requiredPhenomenonKinds) {
            if (noticedKinds.contains(required)) count++;
        }
        return count;
    }

    // ─── Predefined Chains ─────────────────────────────────────────────

    /** The canonical example: birds avoid a valley → silence → pressure → ancient cave. */
    public static final ObservationChain ANCIENT_CULTIVATOR_CAVE = new ObservationChain(
            "ancient_cultivator_cave",
            "The Slumbering Cave",
            Arrays.asList(ObservationPhenomenon.Kind.BIRD_MIGRATION, ObservationPhenomenon.Kind.UNUSUAL_SILENCE, ObservationPhenomenon.Kind.SPIRIT_PRESSURE),
            PerceptionTier.MORTAL,
            KnowledgeTag.RECOGNIZES_QI_DENSITY,
            "The birds flee this valley. The silence is not natural. Something powerful slumbers beneath — you can feel the weight of it in the air.",
            "Search below the silent ground.");

    /** Herb grove: birds gather where qi is rich → herb cluster → spirit herbs. */
    public static final ObservationChain SPIRIT_HERB_GROVE = new ObservationChain(
            "spirit_herb_grove",
            "The Gathering of Beasts",
            Arrays.asList(ObservationPhenomenon.Kind.HERB_CLUSTER, ObservationPhenomenon.Kind.BIRD_MIGRATION),
            PerceptionTier.MORTAL,
            KnowledgeTag.RECOGNIZES_SPIRIT_HERBS,
            "The birds gather where the qi is rich. Spirit herbs grow there — you can now tell them apart from ordinary weeds.",
            "Follow where the birds descend.");

    /** Restriction site: ancient inscription + concealment trace → sealed place. */
    public static final ObservationChain RESTRICTION_SITE = new ObservationChain(
            "restriction_site",
            "The Sealed Mark",
            Arrays.asList(ObservationPhenomenon.Kind.ANCIENT_INSCRIPTION, ObservationPhenomenon.Kind.CONCEALMENT_TRACE),
            PerceptionTier.MORTAL,
            KnowledgeTag.RECOGNIZES_RESTRICTION_GLYPHS,
            "These marks are not decoration. They are a restriction — someone sealed this place long ago. You can now read such glyphs wherever they are carved.",
            "Trace the inscription's pattern to its center.");

    /** Beast territory boundary: beasts patrol + karmic trace → guarded thing. */
    public static final ObservationChain BEAST_TERRITORY_BOUNDARY = new ObservationChain(
            "beast_territory_boundary",
            "The Patrolled Boundary",
            Arrays.asList(ObservationPhenomenon.Kind.BEAST_TERRITORY, ObservationPhenomenon.Kind.KARMIC_TRACE),
            PerceptionTier.MORTAL,
            KnowledgeTag.KNOWS_BEAST_LORE,
            "The beasts patrol a boundary. They are guarding something within. You have learned to read such territories — where beasts hold a line, something lies worth protecting.",
            "Cross the boundary at your own risk.");

    /** Faction presence: scout + tribulation aftermath → sect territory. */
    public static final ObservationChain FACTION_PRESENCE = new ObservationChain(
            "faction_presence",
            "The Sect's Shadow",
            Arrays.asList(ObservationPhenomenon.Kind.FACTION_SCOUT, ObservationPhenomenon.Kind.TRIBULATION_AFTERMATH),
            PerceptionTier.MORTAL,
            KnowledgeTag.KNOWS_FACTION_SIGNS,
            "These are not wanderers. A sect has claimed this territory — the tribulation marks are from their disciples' breakthroughs. You can now read such signs.",
            "Watch the scout's patrol route.");

    /** Qi density mastery: fluctuation + anomaly → qi reading. */
    public static final ObservationChain QI_DENSITY_MASTERY = new ObservationChain(
            "qi_density_mastery",
            "The Flow of Qi",
            Arrays.asList(ObservationPhenomenon.Kind.SPIRIT_FLUCTUATION, ObservationPhenomenon.Kind.QI_DENSITY_ANOMALY),
            PerceptionTier.MORTAL,
            KnowledgeTag.RECOGNIZES_QI_DENSITY,
            "You have felt the world's qi flow — where it pools, where it drains, where it surges. The density of spirit is no longer invisible to you.",
            null);

    /** Sealed inheritance: concealment + karmic → hidden inheritance. */
    public static final ObservationChain SEALED_INHERITANCE = new ObservationChain(
            "sealed_inheritance",
            "The Hidden Inheritance",
            Arrays.asList(ObservationPhenomenon.Kind.CONCEALMENT_TRACE, ObservationPhenomenon.Kind.KARMIC_TRACE),
            PerceptionTier.FOUNDATION,
            KnowledgeTag.RECOGNIZES_CONCEALMENT,
            "The world folds here. Someone concealed an inheritance — and the karma of its concealment still lingers. You can now feel the edge of such hidden spaces.",
            "Step through where the air feels thin.");

    /** All predefined chains. */
    public static final List<ObservationChain> ALL_CHAINS = Collections.unmodifiableList(Arrays.asList(
            ANCIENT_CULTIVATOR_CAVE,
            SPIRIT_HERB_GROVE,
            RESTRICTION_SITE,
            BEAST_TERRITORY_BOUNDARY,
            FACTION_PRESENCE,
            QI_DENSITY_MASTERY,
            SEALED_INHERITANCE
    ));

    /** Find a chain by ID. */
    public static ObservationChain byId(String id) {
        for (ObservationChain c : ALL_CHAINS) {
            if (c.chainId.equals(id)) return c;
        }
        return null;
    }

    /** All chains the player could be progressing toward (not yet complete). */
    public static List<ObservationChain> activeChainsFor(Set<ObservationPhenomenon.Kind> noticedKinds, Set<String> completedChainIds) {
        List<ObservationChain> result = new ArrayList<>();
        for (ObservationChain c : ALL_CHAINS) {
            if (completedChainIds.contains(c.chainId)) continue;
            if (c.progressCount(noticedKinds) > 0 || c.requiredPhenomenonKinds.size() > 0) {
                result.add(c);
            }
        }
        return result;
    }
}
