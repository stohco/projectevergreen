package dev.ergenverse.simulation.artifact;

/**
 * The five layers of treasure usage, from "Can I swing it?" to Dao-level oneness.
 *
 * <p>Every cultivation treasure exists on a spectrum. This enum names the
 * five points on that spectrum. Layer assignment is per-action and per-wielder:
 * the same sword might be at PHYSICAL for a mortal and AUTHORITY for a Soul
 * Formation cultivator.
 *
 * <p>Canon basis (Renegade Immortal): Wang Lin repeatedly uses treasures far
 * above his cultivation realm. The treasures do not reject or destroy him. What
 * changes is how much of the treasure's power he can express. This is the
 * mechanical encoding of that observation.
 *
 * <h2>Layer descriptions</h2>
 * <ul>
 *   <li>{@link #PHYSICAL} — The item's physical form. Always available. A
 *       mortal can hold a divine sword; it's just heavy. Base damage, weight,
 *       durability, attack speed.</li>
 *   <li>{@link #PASSIVE} — Effects that exist without activation. Self-repair,
 *       sharper material, spiritual pressure, aura suppression. The wielder
 *       may benefit slightly just by holding the item.</li>
 *   <li>{@link #ACTIVATION} — The treasure actually functions. Requires qi,
 *       divine sense, blood refinement, compatibility. Flying swords fly.
 *       Formations activate. The restriction flag summons restrictions.</li>
 *   <li>{@link #AUTHORITY} — The wielder is at the treasure's intended level.
 *       Full documented abilities available. Wielder and treasure operate at
 *       the same conceptual level.</li>
 *   <li>{@link #DAO_MANIFESTATION} — Owner and treasure share the same Dao.
 *       They understand its laws, supply its energy natively. User and treasure
 *       are one. For treasures with a bound spirit, spirit and master are
 *       fully unified.</li>
 * </ul>
 */
public enum ArtifactUsageLayer {

    /** Layer 1 — "Can I swing it?" Always available to anyone holding the item. */
    PHYSICAL(1, "Physical Use", "Hold, swing, strike. The item's physical form."),

    /** Layer 2 — Passive properties that exist without activation. */
    PASSIVE(2, "Passive Properties", "Self-repair, sharpness, spiritual pressure, aura suppression."),

    /** Layer 3 — The treasure actually functions. Requires qi, sense, compatibility. */
    ACTIVATION(3, "Spiritual Activation", "Qi injection, divine-sense control, formation activation."),

    /** Layer 4 — Full authority. Wielder at the treasure's intended level. */
    AUTHORITY(4, "Full Authority", "All documented abilities. Wielder and treasure at the same conceptual level."),

    /** Layer 5 — Dao-level manifestation. Owner and treasure share the same Dao. */
    DAO_MANIFESTATION(5, "Dao Manifestation", "User and treasure are one. Laws understood natively."),

    /**
     * The item cannot be used at all — reserved for items that require a
     * specific karmic bond or bloodline the wielder lacks. This is NOT
     * "realm too low" — it's "this item was never yours to use."
     * Rare in canon; used only for truly bound inheritances.
     */
    SEALED(0, "Sealed", "The item does not recognize this wielder at all. No interaction possible.");

    public final int tier;
    public final String name;
    public final String description;

    ArtifactUsageLayer(int tier, String name, String description) {
        this.tier = tier;
        this.name = name;
        this.description = description;
    }

    /** Whether this layer represents any usable expression of the treasure. */
    public boolean isUsable() {
        return this != SEALED;
    }

    /** Whether this layer involves active energy channeling (qi, sense, etc.). */
    public boolean requiresEnergy() {
        return this == ACTIVATION || this == AUTHORITY || this == DAO_MANIFESTATION;
    }
}