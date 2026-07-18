package dev.ergenverse.formation;

import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.core.WorldPhilosophy;
import dev.ergenverse.world.WorldLayer;

import java.util.ArrayList;
import java.util.List;

/**
 * A Formation — a multi-block spiritual structure anchored in the world.
 *
 * <p>Per the {@link WorldPhilosophy} and the design document: formations
 * exist in the {@link WorldLayer#SPIRITUAL} layer. They are anchored by
 * physical blocks (formation flags, cores, spirit veins) in the
 * {@link WorldLayer#PHYSICAL} layer, but the formation's ACTIVE EFFECT
 * exists in spiritual space.
 *
 * <p>A mortal can break the physical flag blocks, but the formation's
 * spiritual anchor may persist or shift (per the Prime Directive). Only
 * a cultivator with sufficient divine sense and the right technique can
 * break the spiritual anchor.
 *
 * <h2>The hybrid implementation</h2>
 *
 * <p>Formations use a hybrid of blocks and items:
 *
 * <ul>
 *   <li><b>Sect-scale formations</b> (defensive, transport, alchemy_aux) =
 *       BLOCK-BASED. Permanent structures. Generate at canon sect locations.</li>
 *   <li><b>Personal/portable formations</b> (restriction, sealing, surveillance) =
 *       ITEM-BASED. Wang Lin's Restriction Flag is an item he carries and plants.</li>
 *   <li><b>Talisman formations</b> (paper talismans, jade slips, soul banners) =
 *       ITEM-BASED (consumable or persistent).</li>
 * </ul>
 *
 * <h2>Canon formation types</h2>
 *
 * <p>From the Er Gen novels: defensive, offensive, trapping, transport,
 * sealing, surveillance, illusion, soul, alchemy_aux, hybrid. Grades:
 * mortal → spirit → earth → heaven → dao → immortal.
 *
 * <h2>Key canon examples</h2>
 *
 * <ul>
 *   <li><b>Restriction Flag</b> (禁幡) — Wang Lin's portable formation-flag.
 *       Embeds restriction matrices, seals regions, suppresses enemies.</li>
 *   <li><b>The 4 Great Restrictions</b> — Annihilation, Time, Life-Death,
 *       Destruction (all Dao-grade).</li>
 *   <li><b>Sect-Protecting Array</b> — universal. Layered defense with
 *       spirit-vein power source, flag-based perimeter, heart-array core.</li>
 *   <li><b>Six Cultivation Planets Restriction</b> — Wang Lin sealed 6
 *       planets at once.</li>
 * </ul>
 */
public final class Formation {

    /** The formation type. */
    public enum Type {
        DEFENSIVE("Defensive", "Shields, wards, perimeter protection"),
        OFFENSIVE("Offensive", "Attacks enemies in an area"),
        TRAPPING("Trapping", "Restrains enemies"),
        TRANSPORT("Transport", "Teleportation arrays"),
        SEALING("Sealing", "Seals regions, objects, or beings"),
        SURVEILLANCE("Surveillance", "Scrying, spying, monitoring"),
        ILLUSION("Illusion", "Creates false appearances"),
        SOUL("Soul", "Affects souls — refining, sealing, summoning"),
        ALCHEMY_AUX("Alchemy Auxiliary", "Assists pill refinement"),
        HYBRID("Hybrid", "Multiple functions");

        public final String label;
        public final String description;
        Type(String label, String description) { this.label = label; this.description = description; }
    }

    /** The formation grade. */
    public enum Grade {
        MORTAL("Mortal", 1),
        SPIRIT("Spirit", 2),
        EARTH("Earth", 3),
        HEAVEN("Heaven", 4),
        DAO("Dao", 5),
        IMMORTAL("Immortal", 6);

        public final String label;
        public final int power;
        Grade(String label, int power) { this.label = label; this.power = power; }
    }

    /** How the formation is anchored in the world. */
    public enum Anchoring {
        /** Block-based — permanent structure (sect-scale formations). */
        BLOCK_BASED,
        /** Item-based — portable, planted when activated (Restriction Flag). */
        ITEM_BASED,
        /** Consumable — one-use (paper talismans). */
        CONSUMABLE,
        /** Persistent item — carried and activated repeatedly (soul banners, compasses). */
        PERSISTENT_ITEM
    }

    // ─── Formation definition ────────────────────────────────────────

    public final String id;
    public final String name;
    public final String nameCn;
    public final Type type;
    public final Grade grade;
    public final Anchoring anchoring;
    public final CanonEngine.Confidence canonConfidence;
    public final String description;
    public final String canonSource;     // which novel/wiki page
    public final String user;            // who uses it in canon (e.g., "Wang Lin")

    /** The formation's power — derived from grade + canon significance. */
    public final double power;

    /** The formation's spiritual resistance — how hard it is to break. */
    public final double spiritualResistance;

    /** Minimum cultivation realm to perceive the formation. */
    public final String minRealmToPerceive;

    /** Minimum cultivation realm to break the formation. */
    public final String minRealmToBreak;

    public Formation(String id, String name, String nameCn, Type type, Grade grade,
                     Anchoring anchoring, CanonEngine.Confidence canonConfidence,
                     String description, String canonSource, String user,
                     double power, double spiritualResistance,
                     String minRealmToPerceive, String minRealmToBreak) {
        this.id = id;
        this.name = name;
        this.nameCn = nameCn;
        this.type = type;
        this.grade = grade;
        this.anchoring = anchoring;
        this.canonConfidence = canonConfidence;
        this.description = description;
        this.canonSource = canonSource;
        this.user = user;
        this.power = power;
        this.spiritualResistance = spiritualResistance;
        this.minRealmToPerceive = minRealmToPerceive;
        this.minRealmToBreak = minRealmToBreak;
    }

    // ─── Canon formation registry ────────────────────────────────────

    private static final List<Formation> CANON_FORMATIONS = new ArrayList<>();

    public static void register(Formation f) {
        CANON_FORMATIONS.add(f);
    }

    public static List<Formation> all() {
        return java.util.Collections.unmodifiableList(CANON_FORMATIONS);
    }

    public static Formation byId(String id) {
        for (Formation f : CANON_FORMATIONS) {
            if (f.id.equals(id)) return f;
        }
        return null;
    }

    // ─── Bootstrap the canon formations ──────────────────────────────

    static {
        // ── Wang Lin's Restriction Flag (the signature portable formation) ──
        register(new Formation(
            "ergen:formation/restriction_flag",
            "Restriction Flag", "禁幡",
            Type.HYBRID,  // hybrid: seals + suppresses + stores
            Grade.IMMORTAL,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Flagship formation-flag artifact. Embeds restriction matrices, seals regions, suppresses enemies, stores pocket army. Doubles as storage + portable restriction matrix.",
            "RI — Wang Lin's signature artifact",
            "Wang Lin",
            10000, 50000,  // very high power and resistance
            "foundation",   // Foundation cultivators can perceive it
            "soul_formation" // Only Soul Formation+ can break Wang Lin's restriction
        ));

        // ── The 4 Great Restrictions (Dao-grade) ──
        register(new Formation(
            "ergen:formation/annihilation_restriction",
            "Annihilation Restriction", "寂灭禁制",
            Type.OFFENSIVE,
            Grade.DAO,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "1st of the 4 Great Restrictions. Annihilates whatever is sealed — body, soul, or dao.",
            "RI",
            "Wang Lin",
            50000, 100000,
            "nascent_soul",
            "transcendence"
        ));

        register(new Formation(
            "ergen:formation/time_restriction",
            "Time Restriction", "岁月禁制",
            Type.SEALING,
            Grade.DAO,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "2nd of the 4 Great Restrictions. Freezes/seals targets in time.",
            "RI",
            "Wang Lin",
            50000, 100000,
            "nascent_soul",
            "transcendence"
        ));

        register(new Formation(
            "ergen:formation/life_death_restriction",
            "Life-Death Restriction", "生死禁制",
            Type.SEALING,
            Grade.DAO,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "3rd of the 4 Great Restrictions. Controls life and death of sealed target.",
            "RI",
            "Wang Lin",
            50000, 100000,
            "nascent_soul",
            "transcendence"
        ));

        register(new Formation(
            "ergen:formation/destruction_restriction",
            "Destruction Restriction", "毁灭禁制",
            Type.OFFENSIVE,
            Grade.DAO,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "4th of the 4 Great Restrictions. Total destruction seal.",
            "RI",
            "Wang Lin",
            50000, 100000,
            "nascent_soul",
            "transcendence"
        ));

        // ── Sect-Protecting Array (universal, block-based) ──
        register(new Formation(
            "ergen:formation/sect_protecting_array",
            "Sect-Protecting Array", "护宗阵",
            Type.DEFENSIVE,
            Grade.SPIRIT,
            Anchoring.BLOCK_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Universal formation archetype. Every sect has one. Layered defense with spirit-vein power source, flag-based perimeter, and heart-array core.",
            "cross — all novels",
            "all sects",
            2000, 8000,
            "qi_condensation",
            "nascent_soul"
        ));

        // ── Heng Yue Sect Protecting Array (specific canon instance) ──
        register(new Formation(
            "ergen:formation/heng_yue_protecting",
            "Heng Yue Sect Protecting Array", "恒岳宗护宗阵",
            Type.DEFENSIVE,
            Grade.SPIRIT,
            Anchoring.BLOCK_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Outer perimeter + spirit-gathering + heart-array core flag layered matrix. Wang Lin's first sect's defensive formation.",
            "RI",
            "Heng Yue Sect",
            1500, 6000,
            "qi_condensation",
            "core_formation"
        ));

        // ── Six Cultivation Planets Restriction (Wang Lin's feat) ──
        register(new Formation(
            "ergen:formation/six_planets_restriction",
            "Six Cultivation Planets Restriction", "六星禁制",
            Type.SEALING,
            Grade.DAO,
            Anchoring.ITEM_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Seals 6 planets at once. Collapsed one planet on trigger. A demonstration of Wang Lin's restriction mastery at the highest tier.",
            "RI",
            "Wang Lin",
            100000, 500000,
            "soul_formation",
            "transcendence"
        ));

        // ── Soul Refining Sect Blood-Sacrifice Array (dark formation) ──
        register(new Formation(
            "ergen:formation/soul_refining_blood_sacrifice",
            "Soul Refining Sect Blood-Sacrifice Array", "炼魂宗血祭阵",
            Type.SOUL,
            Grade.IMMORTAL,
            Anchoring.BLOCK_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Sect-growth method refining outer-sect disciples' souls into banner fuel. A dark formation used by the Soul Refining Sect.",
            "RI",
            "Soul Refining Sect",
            8000, 20000,
            "foundation",
            "nascent_soul"
        ));

        // ── Transport Array (universal, block-based, paired) ──
        register(new Formation(
            "ergen:formation/transport_array",
            "Transport Array", "传送阵",
            Type.TRANSPORT,
            Grade.SPIRIT,
            Anchoring.BLOCK_BASED,
            CanonEngine.Confidence.NOVEL_STATEMENT,
            "Paired teleportation arrays. Two arrays at different locations, linked via a jade slip. Instant travel between them.",
            "cross — all novels",
            "all sects",
            1000, 3000,
            "qi_condensation",
            "foundation"
        ));
    }
}
