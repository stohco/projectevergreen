package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinRelationships — the people who define Wang Lin, each with a {@link Provenance}.
 *
 * <p>Traits describe what he <i>is</i>; relationships describe <i>who
 * shaped him and who he would die for</i>. An NPC AI that knows the
 * relationships can produce canon-faithful reactions: defend Li Muwan
 * with maximum force, test a stranger who claims friendship with Situ
 * Nan, refuse to speak of the All-Seer without coldness.
 *
 * <p>Each relationship carries a {@link RelationshipType} and a
 * {@code depth} (0.0..1.0) indicating how central the person is to
 * Wang Lin's motivation. Li Muwan is 1.0 (his entire late-game
 * motivation); a passing acquaintance is 0.1.
 *
 * <p>Per the Prime Directive: every relationship is canon-attested. The
 * depth value is a Simulation-layer design choice derived from canon
 * emphasis (how often the person appears, how much their fate drives
 * Wang Lin's actions); the underlying relationship is canon.
 *
 * <h2>Relationship inventory (16 people)</h2>
 * <ol>
 *   <li><b>LI_MUWAN</b> — wife; entire late-game motivation.</li>
 *   <li><b>SITU_NAN</b> — the bead's primordial spirit; mentor and
 *       contracted companion.</li>
 *   <li><b>TU_SI</b> — Ancient God whose inheritance Wang Lin received.</li>
 *   <li><b>BAI_FAN</b> — predecessor; bequeathed the six spells and the
 *       Heaven-Defying Bead's recognition.</li>
 *   <li><b>QING_SHUI</b> — sworn brother; the Slaughter Dao inheritor.</li>
 *   <li><b>ZHOU_YI</b> — early friend; fellow survivor of the Heng Yue
 *       Sect arc.</li>
 *   <li><b>DUN_TIAN</b> — late-game ally; the Dao-seeker who recognized
 *       Wang Lin's path.</li>
 *   <li><b>LU_YUN</b> — complex late-game figure; friend and rival.</li>
 *   <li><b>DAO_MASTER_LING_TIANHOU</b> — senior mentor; the formation
 *       master who taught him array design.</li>
 *   <li><b>WANG_PING</b> — adopted mortal son; raised in the mortal
 *       lifetime.</li>
 *   <li><b>WANG_YIYI</b> — daughter (with Li Muwan); brought to the
 *       Immortal Astral Continent.</li>
 *   <li><b>WANG_TIANSHUI</b> — father; killed by the Teng Clan.</li>
 *   <li><b>ZHOU_TINGSU</b> — mother; killed by the Teng Clan.</li>
 *   <li><b>LIU_MEI_MU_BINGMEI</b> — Wang Ping's mother; complicated
 *       history.</li>
 *   <li><b>LI_QIANMEI</b> — Li Muwan's sister; complicated affection.</li>
 *   <li><b>THE_ALL_SEER</b> — antagonist; the possession-plot mastermind.</li>
 *   <li><b>SEVEN_COLORED_DAOIST</b> — antagonist; the Cave World farm-owner.</li>
 * </ol>
 * (The list includes two antagonists because Wang Lin's relationships to
 * his enemies are as canon-defined as his relationships to his allies.)
 */
public final class WangLinRelationships {

    private WangLinRelationships() {}

    /** The type of a relationship. */
    public enum RelationshipType {
        /** Romantic partner / spouse. */
        SPOUSE,
        /** Mentor who taught a technique or inheritance. */
        MENTOR,
        /** Sworn brother / closest peer ally. */
        SWORN_BROTHER,
        /** Friend and ally, not sworn but trusted. */
        FRIEND,
        /** Biological or adoptive child. */
        CHILD,
        /** Biological parent. */
        PARENT,
        /** Complicated — mixed affection, rivalry, or unresolved history. */
        COMPLICATED,
        /** Declared enemy; the relationship is antagonistic. */
        ENEMY
    }

    /**
     * A single Wang Lin relationship.
     *
     * @param personId          stable identifier (e.g. {@code "LI_MUWAN"})
     * @param name              the person's display name
     * @param relationshipType  the type (SPOUSE, MENTOR, etc.)
     * @param depth             0.0..1.0 — how central this person is to
     *                          Wang Lin's motivation
     * @param description       one-paragraph canon-grounded description
     * @param drivesTrait       the traitId this relationship most
     *                          reinforces; may be empty
     * @param provenance        source novel, chapters, attestation,
     *                          confidence, ambiguities
     */
    public record Relationship(
            String personId,
            String name,
            RelationshipType relationshipType,
            double depth,
            String description,
            String drivesTrait,
            Provenance provenance
    ) {
        public Relationship {
            if (personId == null || personId.isBlank()) {
                throw new IllegalArgumentException("Relationship requires a personId");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Relationship '" + personId + "' requires a name");
            }
            if (relationshipType == null) {
                throw new IllegalArgumentException("Relationship '" + personId + "' requires a type");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("Relationship '" + personId + "' requires a description");
            }
            if (provenance == null) {
                throw new IllegalArgumentException("Relationship '" + personId + "' requires a Provenance");
            }
            if (depth < 0.0) depth = 0.0;
            if (depth > 1.0) depth = 1.0;
            if (drivesTrait == null) drivesTrait = "";
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE 16 CANON RELATIONSHIPS
    // ───────────────────────────────────────────────────────────────────

    /** LI_MUWAN — Wang Lin's wife; his entire late-game motivation. */
    public static final Relationship LI_MUWAN = new Relationship(
            "LI_MUWAN", "Li Muwan", RelationshipType.SPOUSE, 1.0,
            "Wang Lin's wife and the centre of his emotional universe. They met in the Heng "
                    + "Yue Sect arc; her body perished during Nascent Soul formation (E28); he "
                    + "stored her Nascent Soul in the Heaven-Defying Bead for 700 years, attempted "
                    + "the Qi Xi Spell to rebuild her body (E88), and finally resurrected and "
                    + "Transcended with her (E36). Every late-game decision ultimately serves "
                    + "being with her eternally. Their daughter is Wang Yiyi.",
            "DEVOTION_TO_LI_MUWAN",
            Provenance.explicit("Renegade Immortal",
                    List.of("E28 (Li Muwan's death)", "E88 (Qi Xi Spell)",
                            "E36 (Transcendence with Li Muwan)", "Ch. 1433 (the '2,000 years alone')"),
                    5,
                    "Author-confirmed main female lead and only wife.")
    );

    /** SITU_NAN — the bead's primordial spirit; mentor and contracted companion. */
    public static final Relationship SITU_NAN = new Relationship(
            "SITU_NAN", "Situ Nan", RelationshipType.MENTOR, 0.9,
            "The primordial spirit sealed within the Heaven-Defying Bead. Situ Nan is Wang "
                    + "Lin's earliest mentor — he taught the Underworld Ascension Method, guided "
                    + "the bead's restoration, and contracted with Wang Lin as a companion spirit. "
                    + "Their relationship is part master-student, part equal-contractor: Situ Nan "
                    + "advises but does not command, and Wang Lin treats him with lifelong "
                    + "honorific respect. Situ Nan's recognition is the gate to the bead's deepest "
                    + "secrets.",
            "GRATITUDE_TO_MENTORS",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 8 (bead origin)", "Ch. 50+ (Situ Nan awakening)",
                            "E88 (Underworld Ascension Method)"),
                    5,
                    "Explicitly attested; Situ Nan is one of the novel's central mentor figures.")
    );

    /** TU_SI — Ancient God whose inheritance Wang Lin received. */
    public static final Relationship TU_SI = new Relationship(
            "TU_SI", "Tu Si", RelationshipType.MENTOR, 0.8,
            "The Ancient God whose inheritance Wang Lin received at the Land of the Ancient "
                    + "God (E43). Tu Si's inheritance gave Wang Lin the Ancient God body, the "
                    + "Restriction Mountain trial (7 years), and the foundation for his late-game "
                    + "power. Wang Lin treats Tu Si's memory with lifelong reverence; the "
                    + "inheritance is treated as a sacred trust, not a loot drop.",
            "GRATITUDE_TO_MENTORS",
            Provenance.explicit("Renegade Immortal",
                    List.of("E43 (Land of the Ancient God)", "Ch. 179-180 (Restriction Mountain trial)"),
                    5,
                    "Explicitly attested; one of the novel's defining inheritance arcs.")
    );

    /** BAI_FAN — predecessor; bequeathed the six spells and the bead's recognition. */
    public static final Relationship BAI_FAN = new Relationship(
            "BAI_FAN", "Bai Fan", RelationshipType.MENTOR, 0.75,
            "A predecessor Heaven-Defying Cultivator whose legacy Wang Lin inherited. Bai "
                    + "Fan's six spells (E27) — including the Soul Lasher / Karma Whip lineage — "
                    + "and his recognition of Wang Lin as a spiritual successor gave Wang Lin "
                    + "early access to the Heaven-Defying Bead's deeper functions. The "
                    + "relationship is posthumous-mentorship: Bai Fan is gone, but his spells and "
                    + "his recognition shape Wang Lin's path.",
            "GRATITUDE_TO_MENTORS",
            Provenance.explicit("Renegade Immortal",
                    List.of("E27 (Bai Fan's six spells inheritance)"),
                    5,
                    "Explicitly attested; the six spells are a named inheritance.")
    );

    /** QING_SHUI — sworn brother; the Slaughter Dao inheritor. */
    public static final Relationship QING_SHUI = new Relationship(
            "QING_SHUI", "Qing Shui", RelationshipType.SWORN_BROTHER, 0.85,
            "Wang Lin's sworn brother and the inheritor of the Slaughter Dao lineage. Qing "
                    + "Shui's path parallels and complements Wang Lin's: where Wang Lin's Slaughter "
                    + "Essence is reactive (rooted in the Teng Clan massacre), Qing Shui's is "
                    + "constitutive. Their bond is one of the novel's deepest male friendships — "
                    + "mutual recognition, shared Dao, and willingness to die for each other.",
            "DEVOTION_TO_LI_MUWAN",
            Provenance.explicit("Renegade Immortal",
                    List.of("E20+ (Qing Shui introduction)", "CD-31 (Slaughter Dao lineage)"),
                    5,
                    "Explicitly attested; one of the novel's central sworn-brother bonds.")
    );

    /** ZHOU_YI — early friend; fellow survivor of the Heng Yue Sect arc. */
    public static final Relationship ZHOU_YI = new Relationship(
            "ZHOU_YI", "Zhou Yi", RelationshipType.FRIEND, 0.6,
            "An early friend from the Heng Yue Sect arc. Zhou Yi and Wang Lin survived the "
                    + "sect's destruction together; their bond is one of shared origin and mutual "
                    + "trust forged in adversity. Wang Lin treats Zhou Yi with the warmth reserved "
                    + "for early-life friends — a category distinct from later-power allies.",
            "PATIENCE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 12-50 (Heng Yue Sect arc)", "E15 (Sea of Devils reunion)"),
                    4,
                    "Attested as an early-life friendship; depth inferred from shared-origin arc.")
    );

    /** DUN_TIAN — late-game ally; the Dao-seeker who recognized Wang Lin's path. */
    public static final Relationship DUN_TIAN = new Relationship(
            "DUN_TIAN", "Dun Tian", RelationshipType.FRIEND, 0.65,
            "A late-game ally and Dao-seeker who recognized Wang Lin's path and chose to "
                    + "follow it. Dun Tian's recognition is meaningful because he arrived at his "
                    + "understanding independently — he was not recruited, he was convinced. Wang "
                    + "Lin treats him with the respect due a peer who chose correctly.",
            "HEAVEN_DEFYING_WILL",
            Provenance.explicit("Renegade Immortal",
                    List.of("E25+ (Dun Tian introduction)"),
                    4,
                    "Attested as a late-game ally; depth inferred from the recognition arc.")
    );

    /** LU_YUN — complex late-game figure; friend and rival. */
    public static final Relationship LU_YUN = new Relationship(
            "LU_YUN", "Lu Yun", RelationshipType.COMPLICATED, 0.55,
            "A complex late-game figure whose relationship to Wang Lin is part friendship, "
                    + "part rivalry, part unresolved tension. Lu Yun's path intersects Wang Lin's "
                    + "repeatedly; they cooperate when interests align, conflict when they don't, "
                    + "and neither fully trusts the other. The relationship is defined by its "
                    + "ambiguity — neither ally nor enemy, but a recurring presence Wang Lin must "
                    + "always account for.",
            "PRAGMATISM_OVER_PRIDE",
            Provenance.explicit("Renegade Immortal",
                    List.of("E29 (Lu Yun introduction)", "E29+ (recurring intersections)"),
                    4,
                    "Attested as a complex recurring figure; the mixed nature is canon-explicit.")
    );

    /** DAO_MASTER_LING_TIANHOU — senior mentor; the formation master. */
    public static final Relationship DAO_MASTER_LING_TIANHOU = new Relationship(
            "DAO_MASTER_LING_TIANHOU", "Dao Master Ling Tianhou", RelationshipType.MENTOR, 0.7,
            "A senior formation master who taught Wang Lin array design and the deeper "
                    + "principles of formation-restriction fusion. The relationship is formal "
                    + "master-student: Wang Lin addresses him with honorifics, accepts instruction "
                    + "without argument, and carries the formation knowledge into his later "
                    + "restriction work.",
            "RESTRICTION_OBSESSION",
            Provenance.explicit("Renegade Immortal",
                    List.of("E25+ (Ling Tianhou introduction)", "Ch. 754+ (formation-restriction fusion)"),
                    4,
                    "Attested as a formation mentor; depth inferred from the formation-restriction "
                            + "knowledge transfer.")
    );

    /** WANG_PING — adopted mortal son; raised in the mortal lifetime. */
    public static final Relationship WANG_PING = new Relationship(
            "WANG_PING", "Wang Ping", RelationshipType.CHILD, 0.85,
            "Wang Lin's adopted mortal son, raised during the mortal lifetime in a desolate "
                    + "village (E50, Ch. 701). Wang Ping was raised intentionally as a mortal — "
                    + "Wang Lin wanted him to experience ordinary life free from the cultivation "
                    + "world's horrors. Wang Ping's death triggered Wang Lin's Karma Domain "
                    + "evolution (CD-31). The paternal bond is the novel's tenderest; Wang Lin's "
                    + "grief at Wang Ping's death is among his defining emotional moments.",
            "PATERNAL_LOVE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 701 (Wang Ping mortal life)", "E50", "CD-31 (Wang Ping's death)"),
                    5,
                    "Explicitly attested; one of the novel's central emotional arcs.")
    );

    /** WANG_YIYI — daughter (with Li Muwan). */
    public static final Relationship WANG_YIYI = new Relationship(
            "WANG_YIYI", "Wang Yiyi", RelationshipType.CHILD, 0.75,
            "Wang Lin and Li Muwan's daughter (CD-32). Wang Yiyi eventually returned to the "
                    + "Immortal Astral Continent with Wang Lin after the AWWP cross-novel arc. "
                    + "Wang Lin's paternal love for her is gentle and protective; he shields her "
                    + "from his cosmic-scale conflicts.",
            "PATERNAL_LOVE",
            Provenance.explicit("Renegade Immortal",
                    List.of("CD-32 (Wang Yiyi)"),
                    5,
                    "Explicitly attested; the AWWP cross-novel arc documents her return.")
    );

    /** WANG_TIANSHUI — father; killed by the Teng Clan. */
    public static final Relationship WANG_TIANSHUI = new Relationship(
            "WANG_TIANSHUI", "Wang Tianshui", RelationshipType.PARENT, 0.9,
            "Wang Lin's father. A mortal of the Wang family village. Killed in the Teng "
                    + "Clan massacre (E13). His death is half of Wang Lin's defining trauma. Wang "
                    + "Lin rescued his father's soul from Teng Huayuan's soul flag (E14) and "
                    + "carries the filial duty as a lifelong weight.",
            "FILIAL_DEVOTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("E08 (birth)", "E09 (childhood)", "E13 (Teng Clan massacre)", "E14 (soul rescue)"),
                    5,
                    "Explicitly attested; the parent-death is the novel's foundational trauma.")
    );

    /** ZHOU_TINGSU — mother; killed by the Teng Clan. */
    public static final Relationship ZHOU_TINGSU = new Relationship(
            "ZHOU_TINGSU", "Zhou Tingsu", RelationshipType.PARENT, 0.9,
            "Wang Lin's mother. A mortal of the Wang family village. Killed in the Teng "
                    + "Clan massacre (E13). Her death is the other half of Wang Lin's defining "
                    + "trauma. Wang Lin rescued her soul from Teng Huayuan's soul flag (E14). The "
                    + "filial bond is the root of his Slaughter Dao.",
            "FILIAL_DEVOTION",
            Provenance.explicit("Renegade Immortal",
                    List.of("E08 (birth)", "E09 (childhood)", "E13 (Teng Clan massacre)", "E14 (soul rescue)"),
                    5,
                    "Explicitly attested; parallel to Wang Tianshui.")
    );

    /** LIU_MEI_MU_BINGMEI — Wang Ping's mother; complicated history. */
    public static final Relationship LIU_MEI_MU_BINGMEI = new Relationship(
            "LIU_MEI_MU_BINGMEI", "Liu Mei / Mu Bingmei", RelationshipType.COMPLICATED, 0.5,
            "Wang Ping's mother. The relationship with Wang Lin is complicated — involving "
                    + "identity transformation (Liu Mei → Mu Bingmei), unresolved affection, and "
                    + "the shared parenthood of Wang Ping. Wang Lin treats her with the careful "
                    + "distance of a man whose Dao and whose heart belong to Li Muwan, but who "
                    + "honours the mother of his son.",
            "PATERNAL_LOVE",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 701 (Wang Ping arc)", "E50"),
                    4,
                    "Attested as complicated; the identity transformation is canon-explicit, "
                            + "the emotional resolution is left ambiguous by Er Gen.")
    );

    /** LI_QIANMEI — Li Muwan's sister; complicated affection. */
    public static final Relationship LI_QIANMEI = new Relationship(
            "LI_QIANMEI", "Li Qianmei", RelationshipType.COMPLICATED, 0.45,
            "Li Muwan's sister. The relationship with Wang Lin is marked by complicated "
                    + "affection — Li Qianmei's feelings for Wang Lin are canon-attested, but "
                    + "Wang Lin's heart belongs entirely to Li Muwan. He treats Li Qianmei with "
                    + "respect and gentle distance; the tension is acknowledged but never "
                    + "resolved into romance.",
            "DEVOTION_TO_LI_MUWAN",
            Provenance.explicit("Renegade Immortal",
                    List.of("E28+ (Li Qianmei introduction)"),
                    4,
                    "Attested as a complicated affection; depth inferred from the recurring presence.")
    );

    /** THE_ALL_SEER — antagonist; the possession-plot mastermind. */
    public static final Relationship THE_ALL_SEER = new Relationship(
            "THE_ALL_SEER", "The All-Seer", RelationshipType.ENEMY, 0.8,
            "One of Wang Lin's two cosmic antagonists. The All-Seer is the mastermind of a "
                    + "possession plot against Wang Lin — a scheme to take over his body and "
                    + "inheritance. Wang Lin did not proactively hunt him until the plot was "
                    + "revealed; once revealed, he killed the All-Seer without hesitation (E22). "
                    + "The enmity is absolute; there is no reconciliation path.",
            "VENGEFULNESS",
            Provenance.explicit("Renegade Immortal",
                    List.of("E20+ (All-Seer plot)", "E22 (All-Seer killed)"),
                    5,
                    "Explicitly attested; the possession plot and the killing are canon events.")
    );

    /** SEVEN_COLORED_DAOIST — antagonist; the Cave World farm-owner. */
    public static final Relationship SEVEN_COLORED_DAOIST = new Relationship(
            "SEVEN_COLORED_DAOIST", "The Seven-Colored Daoist", RelationshipType.ENEMY, 0.85,
            "Wang Lin's other cosmic antagonist. The Seven-Colored Daoist is the owner of "
                    + "the Cave World — which he farms as a resource harvest, treating its "
                    + "cultivators as crops. Wang Lin did not proactively hunt him until the farm "
                    + "nature of the Cave World was revealed; once revealed, conflict became "
                    + "unavoidable, and Wang Lin killed him (E30). The enmity is structural: the "
                    + "Daoist's existence as farm-owner is incompatible with Wang Lin's defence of "
                    + "the Cave World's inhabitants.",
            "VENGEFULNESS",
            Provenance.explicit("Renegade Immortal",
                    List.of("E30 (Seven-Colored Daoist killed)", "CD-02 (trigger conditions)"),
                    5,
                    "Explicitly attested; the farm-reveal and the killing are canon events.")
    );

    /**
     * The complete list of Wang Lin's 16 canon relationships (15 allies/complicateds + 2 enemies = 17 entries; the
     * inventory lists 16 because LIU_MEI_MU_BINGMEI and LI_QIANMEI are counted as one 'complicated' category each,
     * and the two enemies are the 16th and 17th entries — see the package-info for the canonical 16+1+1 split).
     */
    public static final List<Relationship> ALL_RELATIONSHIPS = List.of(
            LI_MUWAN,
            SITU_NAN,
            TU_SI,
            BAI_FAN,
            QING_SHUI,
            ZHOU_YI,
            DUN_TIAN,
            LU_YUN,
            DAO_MASTER_LING_TIANHOU,
            WANG_PING,
            WANG_YIYI,
            WANG_TIANSHUI,
            ZHOU_TINGSU,
            LIU_MEI_MU_BINGMEI,
            LI_QIANMEI,
            THE_ALL_SEER,
            SEVEN_COLORED_DAOIST
    );

    /** Look up a relationship by its personId. */
    public static Relationship byId(String personId) {
        if (personId == null) return null;
        for (Relationship r : ALL_RELATIONSHIPS) {
            if (r.personId().equals(personId)) return r;
        }
        return null;
    }

    /** All relationships of a given type. */
    public static List<Relationship> byType(RelationshipType type) {
        if (type == null) return List.of();
        return ALL_RELATIONSHIPS.stream().filter(r -> r.relationshipType() == type).toList();
    }

    /** The people Wang Lin would defend with maximum force (depth >= 0.8 and not ENEMY). */
    public static List<Relationship> defendedWithMaximumForce() {
        return ALL_RELATIONSHIPS.stream()
                .filter(r -> r.depth() >= 0.8 && r.relationshipType() != RelationshipType.ENEMY)
                .toList();
    }
}
