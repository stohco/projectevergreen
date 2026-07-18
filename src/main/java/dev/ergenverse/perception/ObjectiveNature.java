package dev.ergenverse.perception;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.cultivation.RealmId;

/**
 * The objective nature of an observable thing.
 *
 * <p>This is the truth of the thing — observer-independent. It exists
 * whether or not anyone is looking. Mortals, cultivators, and Wang Lin
 * all observe the same {@code ObjectiveNature} when they look at the
 * same entity. The difference is only in {@link PerceptionResult} (what
 * they understand).
 *
 * <h2>Why this matters</h2>
 *
 * <p>The instinct in game design is to make the rabbit change at higher
 * cultivation tiers — "now the deer is a Spirit Deer." That is wrong.
 * The deer was always a Spirit Deer. At mortal tier the player did not
 * have the perception to recognize it as such; at Qi Condensation they
 * do. The deer never changed; the player's understanding did.
 *
 * <p>This matters because it preserves the objectivity of the world.
 * A mortal who picks up a Spirit Herb but doesn't know it's a Spirit
 * Herb has not "found an ordinary herb." They have found a Spirit Herb
 * they cannot identify. If they eat it, they get the Spirit Herb's
 * effects (probably overdose). If they discard it as a weed, they
 * discarded a Spirit Herb. The world is consistent.
 */
public final class ObjectiveNature {

    /** The kind of thing this is. */
    public enum Kind {
        SPIRIT_BEAST, SPIRIT_HERB, SPIRIT_VEIN, FORMATION,
        CONCEALMENT_ARRAY, INHERITANCE_VAULT, RUIN,
        KARMIC_TRACE, DAO_IMPRINT, HEAVENLY_LAW,
        CULTIVATOR, MORTAL_ENTITY, MORTAL_OBJECT
    }

    public final Kind kind;
    public final String trueName;        // "Spirit Rabbit" (never "Big Rabbit" — that's perception)
    public final String trueNameCn;
    public final int trueRank;           // spirit beast rank, herb grade, formation tier
    public final RealmId trueRealm;      // equivalent cultivation realm
    public final String bloodline;       // bloodline if any ("Divine Hare", "Vermilion Bird", etc.)
    public final String origin;          // where this came from ("descended from Meng Hao's Paragon Spirit Deer")
    public final String karmicHistory;   // visible at Nascent Soul+ via divine sense
    public final CanonEngine.Confidence canonConfidence;
    public final String canonSource;     // novel/wiki/etc.
    /** Dao affinities this entity has comprehended (visible at Soul Formation+). */
    public final String daoAffinities;
    /** Known aliases or titles (visible at higher perception tiers). */
    public final String titles;
    /** The entity's sect or faction affiliation. */
    public final String sect;

    /** Backward-compatible constructor (dao/titles/sect default to empty). */
    public ObjectiveNature(Kind kind, String trueName, String trueNameCn, int trueRank,
                           RealmId trueRealm, String bloodline, String origin,
                           String karmicHistory, CanonEngine.Confidence canonConfidence,
                           String canonSource) {
        this(kind, trueName, trueNameCn, trueRank, trueRealm, bloodline, origin,
            karmicHistory, canonConfidence, canonSource, "", "", "");
    }

    /** Full constructor with all fields. */
    public ObjectiveNature(Kind kind, String trueName, String trueNameCn, int trueRank,
                           RealmId trueRealm, String bloodline, String origin,
                           String karmicHistory, CanonEngine.Confidence canonConfidence,
                           String canonSource, String daoAffinities, String titles, String sect) {
        this.kind = kind;
        this.trueName = trueName;
        this.trueNameCn = trueNameCn;
        this.trueRank = trueRank;
        this.trueRealm = trueRealm;
        this.bloodline = bloodline;
        this.origin = origin;
        this.karmicHistory = karmicHistory;
        this.canonConfidence = canonConfidence;
        this.canonSource = canonSource;
        this.daoAffinities = daoAffinities;
        this.titles = titles;
        this.sect = sect;
    }

    // ─── Common constructors ─────────────────────────────────────────

    public static ObjectiveNature spiritBeast(String name, String nameCn, int rank, RealmId realm,
                                              String bloodline, String origin, String karmic,
                                              CanonEngine.Confidence conf, String source) {
        return new ObjectiveNature(Kind.SPIRIT_BEAST, name, nameCn, rank, realm, bloodline,
            origin, karmic, conf, source);
    }

    public static ObjectiveNature spiritHerb(String name, String nameCn, int grade,
                                             String origin, CanonEngine.Confidence conf, String source) {
        return new ObjectiveNature(Kind.SPIRIT_HERB, name, nameCn, grade, RealmId.MORTAL,
            "", origin, "", conf, source);
    }

    public static ObjectiveNature formation(String name, int tier, RealmId creatorRealm,
                                            String origin, CanonEngine.Confidence conf, String source) {
        return new ObjectiveNature(Kind.FORMATION, name, "", tier, creatorRealm,
            "", origin, "", conf, source);
    }

    public static ObjectiveNature karmicTrace(String description, RealmId tier,
                                              CanonEngine.Confidence conf, String source) {
        return new ObjectiveNature(Kind.KARMIC_TRACE, description, "", 0, tier,
            "", description, description, conf, source);
    }

    /** A cultivator entity with optional sect, titles, and Dao affinities. */
    public static ObjectiveNature cultivator(String name, String nameCn, RealmId realm,
                                              String bloodline, String origin,
                                              String karmicHistory,
                                              CanonEngine.Confidence conf, String source,
                                              String daoAffinities, String titles, String sect) {
        return new ObjectiveNature(Kind.CULTIVATOR, name, nameCn, realm.order, realm,
            bloodline, origin, karmicHistory, conf, source,
            daoAffinities, titles, sect);
    }
}