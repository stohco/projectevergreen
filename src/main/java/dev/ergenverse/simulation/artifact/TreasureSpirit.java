package dev.ergenverse.simulation.artifact;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * Models a treasure's sentient spirit and its recognition of a wielder.
 *
 * <p>Many of Wang Lin's treasures have their own intelligence:
 * <ul>
 *   <li><b>Heaven-Defying Bead</b> — initially houses Situ Nan's remnant soul.
 *       Wang Lin's first mentor. Transmits the Underworld Ascension Method.
 *       Departs once Wang Lin's cultivation suffices.</li>
 *   <li><b>Rain Celestial Sword (Xu Liguo)</b> — comedic, cowardly, loyal
 *       despite himself. Complains constantly but always follows through.
 *       One of the most memorable characters in RI.</li>
 *   <li><b>Restriction Flags</b> — sealed spirits within the restrictions.
 *       Obedient once the flag is fully refined.</li>
 *   <li><b>Mosquito Beast</b> — loyal mount, limited intelligence but
 *       strong bond with Wang Lin.</li>
 * </ul>
 *
 * <h2>Recognition levels</h2>
 * <p>A spirit progresses from ignoring the wielder to full unification.
 * Forcing commands at low recognition causes SPIRIT_REJECTION or
 * SPIRIT_RETALIATION. This is the single most common source of serious
 * backlash in canon.
 *
 * <h2>Canon fidelity</h2>
 * <p>Each named spirit's personality is hardcoded from canon, not generated.
 * Xu Liguo's cowardice and complaints, Situ Nan's gruff mentorship — these
 * are character traits Er Gen wrote, not AI behaviors we invent. The
 * {@code nameCn}, {@code personality}, and {@code canonBehavior} fields
 * carry {@link Provenance} citations.
 */
public record TreasureSpirit(
        /** The spirit's name (English). */
        String name,

        /** The spirit's name (Chinese). */
        String nameCn,

        /** Canon-attested personality description. */
        String personality,

        /** Canon-attested behavior patterns. */
        String canonBehavior,

        /** Source evidence for this spirit's existence and traits. */
        Provenance provenance
) {
    public TreasureSpirit {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("TreasureSpirit requires a name");
        }
        if (nameCn == null) nameCn = "";
        if (personality == null) personality = "";
        if (canonBehavior == null) canonBehavior = "";
        if (provenance == null) {
            throw new IllegalArgumentException("TreasureSpirit requires a Provenance");
        }
    }

    /** No spirit bound to this treasure. */
    public static final TreasureSpirit NONE = new TreasureSpirit(
            "None", "", "No spirit.", "No behavior.", 
            Provenance.inferred("System", List.of(), 5, "Default: no spirit"));

    // ── Recognition levels ──────────────────────────────────────────

    /**
     * How much a treasure spirit recognizes the current wielder.
     * This is per-wielder, not global — the same spirit might be
     * RECOGNIZED for Wang Lin but NONE for a stranger.
     */
    public enum Recognition {
        /** The spirit does not know this person exists. */
        NONE(0, "Unknown"),
        /** The spirit senses someone but hasn't identified them. */
        SENSED(1, "Sensed"),
        /** The spirit acknowledges the wielder but doesn't obey. */
        ACKNOWLEDGED(2, "Acknowledged"),
        /** The spirit recognizes the wielder as its master. */
        RECOGNIZED(3, "Recognized"),
        /** Spirit and master are fully unified. Dao-level bond. */
        UNIFIED(4, "Unified");

        public final int level;
        public final String label;
        Recognition(int level, String label) {
            this.level = level;
            this.label = label;
        }
    }

    // ── Named canon spirits (hardcoded from Er Gen's writing) ──────

    /**
     * Situ Nan ( 司徒南) — the 2nd-Generation Vermilion Bird's remnant soul.
     * Wang Lin's first mentor. Lives inside the Heaven-Defying Bead.
     *
     * <p>Personality: gruff, proud, secretly caring. Transmits the
     * Underworld Ascension Method and Vermilion Bird Burning Heaven Art.
     * Departs once Wang Lin is strong enough to stand on his own.
     */
    public static final TreasureSpirit SITU_NAN = new TreasureSpirit(
            "Situ Nan", "司徒南",
            "Gruff, proud, and short-tempered, but genuinely cares for Wang Lin. " +
            "Speaks with authority and disdain for the weak. Complains about being " +
            "trapped in the bead but stays because of his bond with Wang Lin. " +
            "A true senior of the cultivation world who has seen the heights.",
            "Transmits techniques to Wang Lin (Underworld Ascension Method, " +
            "Vermilion Bird Burning Heaven Art). Departs when Wang Lin's " +
            "cultivation is sufficient. His presence initially blocks full " +
            "access to the bead; the bead only opens after Situ Nan recognizes " +
            "Wang Lin through the Five Elements alignment.",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 8+", "Ch. ~200"),
                    5, "Exact chapter of first transmission varies by translation")
    );

    /**
     * Xu Liguo ( 许立国) — the sword spirit of the Rain Celestial Sword.
     * One of the most beloved side characters in RI.
     *
     * <p>Personality: cowardly, talkative, complains constantly, secretly
     * loyal. Will grovel and beg when frightened but always follows through
     * on Wang Lin's orders in the end. Provides comic relief but is
     * genuinely dependable in a fight. Canon: the sword spirit was later
     * passed to Xu Liguo (the person), which is a source of confusion in
     * wikis — the spirit and the person share a name.
     */
    public static final TreasureSpirit XU_LIGUO = new TreasureSpirit(
            "Xu Liguo (Sword Spirit)", "许立国（剑灵）",
            "Cowardly and talkative to a fault. Complains about everything — " +
            "being swung too hard, facing too strong an enemy, not being " +
            "polished enough. Grovels and begs when frightened. But beneath " +
            "the cowardice is genuine loyalty to Wang Lin; he always follows " +
            "through on orders, often saving the day precisely because he " +
            "complained so loudly that the enemy underestimated him.",
            "Always follows Wang Lin's combat orders despite complaining. " +
            "His complaints are actually useful intelligence — he senses " +
            "enemy strength and warns Wang Lin. At full authority, the " +
            "complaints stop and he becomes a devastating combat weapon. " +
            "Canon: passed to the person Xu Liguo later in the story.",
            Provenance.explicit("Renegade Immortal", List.of("Ch. ~700+"),
                    5, "Exact first appearance chapter varies; consistently present from Soul Transformation era")
    );

    /**
     * The restriction flag spirit — the sealed souls within the 1st Restriction Flag.
     * Less individually characterized than Xu Liguo or Situ Nan, but
     * canon-attested as having its own will.
     */
    public static final TreasureSpirit RESTRICTION_FLAG_SPIRIT = new TreasureSpirit(
            "Restriction Flag Spirit", "禁制旗灵",
            "Cold and orderly. The spirit of 99,999 sealed restrictions. " +
            "Obeys the flag's master without question once recognition is " +
            "established. Has no individual personality in the way Xu Liguo " +
            "does — it is more like a programmed intelligence.",
            "Summons restrictions on command. The spirit's power scales " +
            "with how many restrictions have been completed (out of 99,999). " +
            "Canon: the flag summons divine tribulation even when incomplete.",
            Provenance.explicit("Renegade Immortal", List.of("Nascent Soul era"),
                    4, "The spirit is attested but less individually characterized than Xu Liguo")
    );
}