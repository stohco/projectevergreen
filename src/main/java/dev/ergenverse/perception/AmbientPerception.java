package dev.ergenverse.perception;

import dev.ergenverse.cultivation.RealmId;

import java.util.Map;
import java.util.function.Function;

/**
 * Ambient Perception — extends the perception system to blocks.
 *
 * <p>The Prime Directive applies to blocks just as it does to entities.
 * A spirit herb block IS a spirit herb regardless of who looks at it.
 * What changes is what the observer understands:
 * <ul>
 *   <li><b>Mortal</b>: sees "Sea Pickle" — an ordinary decoration.</li>
 *   <li><b>Qi Condensation</b>: sees "Spirit Herb (grade unknown)" —
 *       recognizes Qi but can't identify.</li>
 *   <li><b>Foundation</b>: sees the true name + grade — can harvest.</li>
 *   <li><b>Nascent Soul+</b>: sees origin + alchemical properties.</li>
 * </ul>
 *
 * <p>Also handles:
 * <ul>
 *   <li><b>Spirit vein ore</b> (nether_quartz_ore, calcite): mortals see
 *       "Nether Quartz Ore", Foundation+ see "Spirit Vein (Rank N)".</li>
 *   <li><b>Formation anchors</b> (future): certain blocks serve as
 *       formation anchors visible only at Foundation+.</li>
 * </ul>
 *
 * <p>These perceptions are BLOCK-LEVEL, not entity-level. They modify
 * item names in inventory, block names in the world (via Waila-style
 * tooltip), and harvest behavior.
 */
public final class AmbientPerception {

    private AmbientPerception() {}

    // ─── Spirit Herb Block Registry ─────────────────────────────────
    // Maps vanilla block registry names to their true (canon) identities.
    // The block IS the spirit herb; the observer just may not realize it.

    /** A spirit herb identity — what the block truly is. */
    public static final class HerbIdentity {
        public final String trueName;
        public final String trueNameCn;
        public final int grade;
        public final String effect;
        public final String origin;

        public HerbIdentity(String trueName, String trueNameCn, int grade,
                           String effect, String origin) {
            this.trueName = trueName;
            this.trueNameCn = trueNameCn;
            this.grade = grade;
            this.effect = effect;
            this.origin = origin;
        }
    }

    /** Canon spirit herbs mapped to their vanilla stand-in blocks. */
    public static final Map<String, HerbIdentity> SPIRIT_HERBS = Map.of(
        // The 6 canon spirit herbs from the task spec, using vanilla stand-ins
        "minecraft:sea_pickle",
            new HerbIdentity("Soul Nourishing Lotus", "养魂莲", 4,
                "Nourishes the soul sea. Stabilizes cultivation foundation.",
                "Planet Suzaku, Zhao Country — grows near spirit veins"),
        "minecraft:glow_berries",
            new HerbIdentity("Nine-Leaf Clover", "九叶草", 3,
                "Each leaf contains a different spiritual property. Rarely found complete.",
                "Planet Suzaku, Fog Valley region"),
        "minecraft:orange_tulip",
            new HerbIdentity("Blood Forgetting Grass", "忘血草", 5,
                "Erases emotional attachments. Dangerous — may damage the Dao heart.",
                "Planet Suzaku, Sea of Devils — feeds on battle residue Qi"),
        "minecraft:lily_of_the_valley",
            new HerbIdentity("Snow Heart Lotus", "雪心莲", 3,
                "Cools inner fire. Used in pills requiring cold-attribute ingredients.",
                "Planet Suzaku, Heng Yue Sect mountains"),
        "minecraft:brown_mushroom",
            new HerbIdentity("Qi Gathering Mushroom", "聚气菇", 2,
                "Accelerates Qi absorption during meditation. Common but essential.",
                "Planet Suzaku — widespread in temperate biomes"),
        "minecraft:red_mushroom",
            new HerbIdentity("Blood Spirit Mushroom", "血灵菇", 2,
                "Contains trace blood Qi. Used in body-refinement pills.",
                "Planet Suzaku — grows in damp, Qi-rich environments"),
        "minecraft:cyan_terracotta",
            new HerbIdentity("Spirit Clay", "灵土", 1,
                "Qi-infused clay used in formation anchors and pill cauldrons.",
                "Planet Suzaku — found in Zhao Country plains")
    );

    /** Spirit vein ore blocks and their rank based on block type. */
    public static final Map<String, Integer> SPIRIT_VEIN_RANKS = Map.of(
        "minecraft:nether_quartz_ore", 3,
        "minecraft:calcite", 2
    );

    // ─── Perception Functions ───────────────────────────────────────

    /**
     * Get the perceived name of a block based on the observer's realm.
     *
     * <p>Mortals see the vanilla name. Qi Condensation+ see the true
     * identity (or "Spirit Herb" if they can't determine grade).
     * Foundation+ see the full name + grade.
     *
     * @param blockId the block's registry name (e.g. "minecraft:sea_pickle")
     * @param observerRealm the observer's cultivation realm
     * @return the perceived name string (may include color codes)
     */
    public static String perceiveBlockName(String blockId, RealmId observerRealm) {
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);

        // Check spirit herbs
        HerbIdentity herb = SPIRIT_HERBS.get(blockId);
        if (herb != null) {
            return perceiveHerbName(herb, tier);
        }

        // Check spirit vein ore
        Integer veinRank = SPIRIT_VEIN_RANKS.get(blockId);
        if (veinRank != null) {
            return perceiveVeinName(veinRank, tier);
        }

        // Not a perception-block — return null (caller should use vanilla name)
        return null;
    }

    /**
     * Get the perceived description/tooltip for a block.
     * Returns null if no special perception applies.
     */
    public static String perceiveBlockDescription(String blockId, RealmId observerRealm) {
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);

        HerbIdentity herb = SPIRIT_HERBS.get(blockId);
        if (herb != null) {
            return perceiveHerbDescription(herb, tier);
        }

        Integer veinRank = SPIRIT_VEIN_RANKS.get(blockId);
        if (veinRank != null) {
            return perceiveVeinDescription(veinRank, tier);
        }

        return null;
    }

    /**
     * Can the observer interact with this block?
     *
     * <p>Per the Prime Directive: the block EXISTS regardless. But
     * interaction is gated by perception. A mortal can physically
     * pick up a Sea Pickle — but they're picking up a Sea Pickle.
     * They are NOT harvesting a Soul Nourishing Lotus (because they
     * don't know it IS one). The item they get is the vanilla item.
     *
     * <p>Only when the observer is at Foundation+ do they harvest
     * the TRUE item (the spirit herb). Below Foundation, they get
     * the vanilla stand-in.
     */
    public static boolean canHarvestTrueForm(String blockId, RealmId observerRealm) {
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);
        // Spirit herbs: Foundation+ harvests the true form
        if (SPIRIT_HERBS.containsKey(blockId)) {
            return tier.order >= PerceptionTier.FOUNDATION.order;
        }
        // Spirit veins: Qi Condensation+ can interact
        if (SPIRIT_VEIN_RANKS.containsKey(blockId)) {
            return tier.order >= PerceptionTier.QI_CONDENSATION.order;
        }
        return true; // Non-perception blocks: always interactable
    }

    /**
     * Should this block be concealed from the observer?
     * Concealed blocks show their vanilla name and no cultivation info.
     */
    public static boolean isConcealed(String blockId, RealmId observerRealm) {
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);
        // Spirit herbs are concealed from mortals (they see "Sea Pickle", not "Spirit Herb")
        if (SPIRIT_HERBS.containsKey(blockId)) {
            return tier.order < PerceptionTier.QI_CONDENSATION.order;
        }
        // Spirit veins are concealed from mortals (they feel "a strange feeling" at most)
        if (SPIRIT_VEIN_RANKS.containsKey(blockId)) {
            return tier.order < PerceptionTier.QI_CONDENSATION.order;
        }
        return false;
    }

    // ─── Internal perception functions ──────────────────────────────

    private static String perceiveHerbName(HerbIdentity herb, PerceptionTier tier) {
        switch (tier) {
            case MORTAL:
                return null; // Use vanilla name
            case QI_CONDENSATION:
                return "\u00A72[?] Spirit Herb\u00A7r";
            case FOUNDATION:
                return "\u00A72" + herb.trueName + " (Grade " + herb.grade + ")\u00A7r";
            case NASCENT_SOUL:
                return "\u00A7b" + herb.trueName + "\u00A77 (" + herb.trueNameCn + ")\u00A7r";
            case SOUL_FORMATION:
                return "\u00A75" + herb.trueName + " \u00A77[" + herb.origin + "]\u00A7r";
            default:
                return "\u00A7f" + herb.trueName + " \u00A77" + herb.trueNameCn + "\u00A7r";
        }
    }

    private static String perceiveHerbDescription(HerbIdentity herb, PerceptionTier tier) {
        switch (tier) {
            case MORTAL:
                return null;
            case QI_CONDENSATION:
                return "You sense faint Qi within this plant. It may be valuable to cultivators.";
            case FOUNDATION:
                return "Grade " + herb.grade + " Spirit Herb. " + herb.effect;
            case NASCENT_SOUL:
                return herb.effect + " Origin: " + herb.origin + ".";
            default:
                return herb.effect + " Origin: " + herb.origin
                    + ". A thread in the region's karmic web.";
        }
    }

    private static String perceiveVeinName(int rank, PerceptionTier tier) {
        switch (tier) {
            case MORTAL:
                return null; // Use vanilla name
            case QI_CONDENSATION:
                return "\u00A7e[?] Spirit Vein\u00A7r";
            case FOUNDATION:
                return "\u00A7eSpirit Vein (Rank ~" + rank + ")\u00A7r";
            case NASCENT_SOUL:
                return "\u00A76Spirit Vein, Rank " + rank + "\u00A7r";
            default:
                return "\u00A7fSpirit Vein, Rank " + rank + " \u00A77[Pulse detection active]\u00A7r";
        }
    }

    private static String perceiveVeinDescription(int rank, PerceptionTier tier) {
        switch (tier) {
            case MORTAL:
                return "The air here feels heavier than usual.";
            case QI_CONDENSATION:
                return "A Spirit Vein pulses beneath the ground. Meditate nearby to accelerate cultivation.";
            case FOUNDATION:
                return "A Rank " + rank + " Spirit Vein. Qi density is elevated. Good for meditation.";
            default:
                return "Rank " + rank + " Spirit Vein. Qi density is elevated. The vein's flow is visible to divine sense.";
        }
    }

    // ─── Specialization bonuses ─────────────────────────────────────

    /**
     * Does the observer get extra info from a specialization?
     * Alchemists see herb grades one tier earlier.
     * Formation masters see vein patterns at lower tiers.
     */
    public static boolean hasSpecializationBonus(String blockId, RealmId observerRealm,
                                                   boolean isAlchemist, boolean isFormationMaster) {
        PerceptionTier tier = PerceptionTier.fromRealm(observerRealm);
        if (isAlchemist && SPIRIT_HERBS.containsKey(blockId)) {
            // Alchemists see grade at Qi Condensation instead of Foundation
            return tier.order >= PerceptionTier.QI_CONDENSATION.order;
        }
        if (isFormationMaster && SPIRIT_VEIN_RANKS.containsKey(blockId)) {
            // Formation masters see exact vein rank at Qi Condensation
            return tier.order >= PerceptionTier.QI_CONDENSATION.order;
        }
        return false;
    }
}