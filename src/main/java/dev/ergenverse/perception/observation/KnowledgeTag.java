package dev.ergenverse.perception.observation;

import dev.ergenverse.perception.PerceptionTier;

/**
 * KnowledgeTag — perception-expanding knowledge a player can acquire.
 *
 * <p>This is the "knowledge unlocks perception" principle from Er Gen. A
 * mortal who has studied herb lore will recognize a Spirit Herb where another
 * mortal sees only a weed. The herb did not change; the mortal's
 * <i>understanding</i> did.
 *
 * <p>Knowledge tags are granted by completing {@link ObservationChain}s. Once
 * acquired, a tag persists (it is knowledge, not a buff). The
 * {@link dev.ergenverse.perception.PerceptionEngine} MAY consult
 * {@code ObservationEngine.hasKnowledge(player, tag)} in a future integration
 * to enrich {@link dev.ergenverse.perception.PerceptionResult}s for observers
 * who hold the relevant tag.
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Knowledge is not XP. It does not make the player "stronger." It makes
 *   them <i>see more</i>. A player with RECOGNIZES_RESTRICTION_GLYPHS sees
 *   restriction patterns carved in stone where another player sees only
 *   scratches. The stone never changed.
 * </blockquote>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public enum KnowledgeTag {
    /** The player can identify Spirit Herbs by sight (Qi Condensation equivalent perception). */
    RECOGNIZES_SPIRIT_HERBS("recognizes_spirit_herbs", "Spirit Herb Recognition",
            "You have learned to distinguish spirit herbs from ordinary weeds.",
            "spirit_herb_grove", PerceptionTier.MORTAL),

    /** The player can read restriction glyphs carved in stone. */
    RECOGNIZES_RESTRICTION_GLYPHS("recognizes_restriction_glyphs", "Restriction Glyph Reading",
            "You have learned that certain marks are not decoration — they are restrictions.",
            "restriction_site", PerceptionTier.MORTAL),

    /** The player can detect fake/counterfeit spirit stones by their qi signature. */
    RECOGNIZES_FAKE_SPIRIT_STONES("recognizes_fake_spirit_stones", "Counterfeit Stone Detection",
            "You have learned to feel the hollowness in counterfeit spirit stones.",
            "fake_stone_market", PerceptionTier.QI_CONDENSATION),

    /** The player can perceive qi density variations in the environment. */
    RECOGNIZES_QI_DENSITY("recognizes_qi_density", "Qi Density Perception",
            "You have learned to feel where the world's qi pools and where it drains.",
            "ancient_cultivator_cave", PerceptionTier.MORTAL),

    /** The player can read bloodline resonance in spirit beasts (Nascent Soul normally required). */
    RECOGNIZES_BLOODLINES("recognizes_bloodlines", "Bloodline Resonance",
            "You have learned to sense the ancient bloodlines slumbering in spirit beasts.",
            "beast_territory_boundary", PerceptionTier.FOUNDATION),

    /** The player can perceive karmic traces left by significant events. */
    RECOGNIZES_KARMIC_TRACES("recognizes_karmic_traces", "Karmic Trace Perception",
            "You have learned to read the residue of fate — where battles were fought, where oaths were broken.",
            "karmic_battlefield", PerceptionTier.NASCENT_SOUL),

    /** The player can identify formation nodes by their energy patterns. */
    RECOGNIZES_FORMATION_NODES("recognizes_formation_nodes", "Formation Node Sight",
            "You have learned to see the invisible threads that bind a formation together.",
            "formation_site", PerceptionTier.FOUNDATION),

    /** The player can detect the edge of concealment formations. */
    RECOGNIZES_CONCEALMENT("recognizes_concealment", "Concealment Edge Detection",
            "You have learned to feel where the world has been folded — the boundary of a hidden space.",
            "sealed_inheritance", PerceptionTier.FOUNDATION),

    /** The player can read Dao imprints left by ancient cultivators. */
    RECOGNIZES_DAO_IMPRINTS("recognizes_dao_imprints", "Dao Imprint Reading",
            "You have learned to perceive the Dao comprehension etched into ancient sites.",
            "dao_comprehension_site", PerceptionTier.SOUL_FORMATION),

    /** Lore: the player has studied herb lore and knows growth conditions, pairings, and dangers. */
    KNOWS_HERB_LORE("knows_herb_lore", "Herb Lore",
            "You have studied the growth and properties of spirit herbs.",
            "herb_lore_study", PerceptionTier.MORTAL),

    /** Lore: the player understands basic formation theory. */
    KNOWS_FORMATION_LORE("knows_formation_lore", "Formation Lore",
            "You have studied the principles of formation arrangement.",
            "formation_lore_study", PerceptionTier.MORTAL),

    /** Lore: the player knows beast behavior, territories, and pack dynamics. */
    KNOWS_BEAST_LORE("knows_beast_lore", "Beast Lore",
            "You have studied the behavior and territories of spirit beasts.",
            "beast_territory_boundary", PerceptionTier.MORTAL),

    /** Lore: the player recognizes faction insignia and scout behavior. */
    KNOWS_FACTION_SIGNS("knows_faction_signs", "Faction Sign Reading",
            "You have learned to read the marks sects leave on their territories.",
            "faction_presence", PerceptionTier.MORTAL);

    public final String id;
    public final String label;
    public final String description;
    public final String grantedByChainId;
    public final PerceptionTier perceptionTierFloor;

    KnowledgeTag(String id, String label, String description,
                 String grantedByChainId, PerceptionTier floor) {
        this.id = id;
        this.label = label;
        this.description = description;
        this.grantedByChainId = grantedByChainId;
        this.perceptionTierFloor = floor;
    }

    public static KnowledgeTag fromId(String id) {
        for (KnowledgeTag t : values()) if (t.id.equals(id)) return t;
        return null;
    }

    /** All tags that can be granted by completing the chain with the given ID. */
    public static java.util.List<KnowledgeTag> grantedByChain(String chainId) {
        java.util.List<KnowledgeTag> result = new java.util.ArrayList<>();
        for (KnowledgeTag t : values()) {
            if (chainId.equals(t.grantedByChainId)) result.add(t);
        }
        return result;
    }
}
