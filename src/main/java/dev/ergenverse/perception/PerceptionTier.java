package dev.ergenverse.perception;

import dev.ergenverse.cultivation.RealmId;

/**
 * Perception Tier — derived from the observer's cultivation realm.
 *
 * <p>Perception tier determines what an observer can <b>understand</b>
 * about an objective thing. It does NOT change the thing being observed.
 *
 * <p>The tiers (collapsing the 17-stage realm ladder to 7 perception
 * bands):
 *
 * <ul>
 *   <li><b>MORTAL</b> — sees physical reality. Understands only the
 *       surface: "that's a big rabbit."</li>
 *   <li><b>QI_CONDENSATION</b> — senses Qi. Recognizes spirit beasts and
 *       spirit herbs as such. Knows to flee or harvest.</li>
 *   <li><b>FOUNDATION</b> — sees Qi currents, minor formations, spirit
 *       veins. Identifies concealment formations (without necessarily
 *       being able to break them).</li>
 *   <li><b>NASCENT_SOUL</b> — sees karmic traces, bloodline resonance.
 *       Can read an entity's history through divine sense.</li>
 *   <li><b>SOUL_FORMATION</b> — sees sealed inheritances, the world's
 *       "containment." Understands Dao imprints.</li>
 *   <li><b>ASCENDANT</b> — sees the world boundary, the local
 *       antagonist's will. Recognizes ancient bloodline descendants.</li>
 *   <li><b>TRANSCENDENCE</b> — sees the true nature of the cosmos.</li>
 * </ul>
 *
 * <p>This enum is <b>only</b> about understanding. Existence is handled
 * by {@link WorldLayer} and {@link ObjectiveNature}. A mortal and a
 * Transcendent both see the same rabbit — the Transcendent just
 * understands more about it.
 */
public enum PerceptionTier {
    MORTAL("Mortal Eyes", 0),
    QI_CONDENSATION("Qi-Sense", 1),
    FOUNDATION("Foundation Sight", 2),
    NASCENT_SOUL("Nascent Soul Perception", 3),
    SOUL_FORMATION("Soul Formation Vision", 4),
    ASCENDANT("Ascendant Gaze", 5),
    TRANSCENDENCE("Transcendent Sight", 6);

    public final String label;
    public final int order;

    PerceptionTier(String label, int order) {
        this.label = label;
        this.order = order;
    }

    /** Derive the perception tier from the observer's cultivation realm. */
    public static PerceptionTier fromRealm(RealmId realm) {
        switch (realm) {
            case MORTAL: return MORTAL;
            case QI_CONDENSATION: return QI_CONDENSATION;
            case FOUNDATION:
            case CORE_FORMATION: return FOUNDATION;
            case NASCENT_SOUL: return NASCENT_SOUL;
            case SOUL_FORMATION:
            case SOUL_TRANSFORMATION: return SOUL_FORMATION;
            case ASCENDANT:
            case ILLUSORY_YIN:
            case CORPOREAL_YANG:
            case NIRVANA_SCRYER:
            case NIRVANA_CLEANSER:
            case NIRVANA_FRUIT:
            case SPIRIT_SEIZER:
            case TRUE_IMMORTAL: return ASCENDANT;
            case ANCIENT:
            case PARAGON:
            case TRANSCENDENCE:
            default: return TRANSCENDENCE;
        }
    }

    public boolean canPerceive(PerceptionTier required) {
        return this.order >= required.order;
    }

    public PerceptionTier next() {
        if (this == TRANSCENDENCE) return this;
        return values()[this.ordinal() + 1];
    }
}
