package dev.ergenverse.wanglin.behavior;

/**
 * SystemInteraction — how this item interacts with another system (formations, treasures, souls, beasts, avatars, karma, restrictions, divine sense).
 *
 * @param interactsWith    the system it interacts with
 * @param interactionType  the type of interaction
 * @param interactionDetail free-text canon detail
 * @param canonBasis       chapter citation
 */
public record SystemInteraction(
        SystemType interactsWith,
        InteractionType interactionType,
        String interactionDetail,
        String canonBasis
) {
    public enum SystemType {
        FORMATIONS,             // formation-arrays
        TREASURES,              // other magical treasures
        SOULS,                  // soul items / soul cultivators
        SPIRIT_BEASTS,          // tamed beasts
        AVATARS_CLONES,         // avatars / clones / true bodies
        KARMA,                  // karmic threads / karma domain
        RESTRICTIONS,           // restrictions / sealing
        DIVINE_SENSE,           // divine-sense techniques
        SPACE_POCKET,           // spatial pockets / storage
        TIME,                   // time dilation / time-reversal
        LIFE_DEATH,             // life-death domain / underworld river
        REINCARNATION,          // reincarnation pool / samsara
        JOSS_FLAME,             // joss flame economy
        TRIBULATION,            // divine tribulation
        ANCIENT_GOD_LINEAGE,    // ancient god body
        CELESTIAL_LINEAGE,      // celestial-tier items
        VERMILION_BIRD_LINEAGE, // Vermilion Bird inheritance
        UNKNOWN
    }

    public enum InteractionType {
        AMPLIFIES,              // boosts the other system
        SUPPRESSES,             // suppresses the other system
        CONSUMES,               // consumes from the other system
        STORES,                 // stores within the other system
        FUSES_WITH,             // can fuse with the other system
        COUNTERS,               // defeats the other system
        REQUIRES,               // requires the other system to function
        RESONATES_WITH,         // sympathetic resonance
        ANCHORS,                // anchors the other system
        SEALS,                  // seals the other system
        SUMMONS,                // summons the other system (e.g. tribulation)
        UNKNOWN
    }

    public SystemInteraction {
        if (interactsWith == null) interactsWith = SystemType.UNKNOWN;
        if (interactionType == null) interactionType = InteractionType.UNKNOWN;
        if (interactionDetail == null) interactionDetail = "";
        if (canonBasis == null) canonBasis = "";
    }
}
