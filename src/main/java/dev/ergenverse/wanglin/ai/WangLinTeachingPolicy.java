package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

import java.util.List;
import java.util.Set;

/**
 * WangLinTeachingPolicy — what Wang Lin will SHARE, and when.
 *
 * <p>This is the engine behind the user's directive: "i want pretty much an
 * exact copy that they can teach me the ways of." Wang Lin CAN teach the
 * player the ways of his techniques, items, and Dao — but only on his own
 * canon-faithful terms. He does not teach everything to everyone.
 *
 * <p>The policy encodes four canon-faithful gates (from the package-info):
 * <ul>
 *   <li><b>Heaven-Defying Bead's core mysteries</b> are NEVER shared —
 *       the bead is ABSOLUTE_UNIQUE (per the Opportunity System); only
 *       derivative understanding is possible.</li>
 *   <li><b>The Underworld Ascension Method</b> requires Situ Nan's
 *       explicit permission — Wang Lin is not free to redistribute a
 *       technique he received under contract.</li>
 *   <li><b>The Restriction Flag art</b> requires demonstrated patience —
 *       Wang Lin spent 7 years at Restriction Mountain and will not teach
 *       the art to someone who has not shown equivalent discipline.</li>
 *   <li><b>Life lessons about caution</b> are shared FREELY to those who
 *       remind him of his younger self — this is the one open door.</li>
 * </ul>
 *
 * <h2>The decision engine</h2>
 * <p>{@link #canTeach(PlayerRequest, String)} takes a {@link PlayerRequest}
 * (the player's current state: trust level, demonstrated virtues, special
 * permissions) and a subject identifier, and returns a
 * {@link TeachingDecision}:
 * <ul>
 *   <li><b>WILL_TEACH</b> — Wang Lin will teach the subject now.</li>
 *   <li><b>REFUSES</b> — Wang Lin will not teach this subject, ever, to
 *       this player. The refusal is canon-faithful.</li>
 *   <li><b>OFFERS_PARTIAL</b> — Wang Lin will teach a partial form of
 *       the subject (e.g. a lesser restriction, a derived technique).
 *       The full subject remains beyond reach.</li>
 *   <li><b>TESTS_FIRST</b> — Wang Lin will teach this subject ONLY after
 *       the player passes a test of patience, virtue, or resolve.</li>
 * </ul>
 *
 * <h2>Teaching-offering inventory (16 subjects)</h2>
 * <p>See the static constants below. Each is a canon-attested subject Wang
 * Lin is known to have studied, used, or been asked about.
 *
 * <p>Per the Prime Directive: every offering is canon-attested. The
 * prerequisite thresholds (trust levels, virtues) are Simulation-layer
 * design choices derived from canon emphasis; the underlying teaching
 * relationship is canon.
 */
public final class WangLinTeachingPolicy {

    private WangLinTeachingPolicy() {}

    /** The four possible outcomes of a teach request. */
    public enum TeachingDecision {
        /** Wang Lin will teach the subject now. */
        WILL_TEACH,
        /** Wang Lin will not teach this subject, ever, to this player. Canon-faithful refusal. */
        REFUSES,
        /** Wang Lin will teach a partial / derived form. The full subject remains beyond reach. */
        OFFERS_PARTIAL,
        /** Wang Lin will teach ONLY after the player passes a test of patience, virtue, or resolve. */
        TESTS_FIRST
    }

    /** Virtues Wang Lin recognizes and may require as teaching prerequisites. */
    public enum RecognizedVirtue {
        /** The patience to study a single thing for years. Required for restriction teaching. */
        PATIENCE,
        /** The caution to plan an escape before engaging. Required for survival-technique teaching. */
        CAUTION,
        /** The resolve to defy heaven rather than submit. Required for Heaven-Defying Will teaching. */
        HEAVEN_DEFYING_RESOLVE,
        /** The filial devotion to family. Required for Samsara Dao teaching. */
        FILIAL_DEVOTION,
        /** The pragmatic willingness to flee, disguise, or wait. Required for pragmatism teaching. */
        PRAGMATISM,
        /** Honesty in intention-testing. Required for trust-building. */
        HONESTY_IN_PROBE
    }

    /**
     * The player's current state, as relevant to a teaching decision.
     *
     * @param trustLevel              0..100 — the player's current affinity/trust with Wang Lin
     * @param demonstratedVirtues     the set of virtues the player has demonstrated in Wang Lin's presence
     * @param hasSituNanPermission    whether Situ Nan has explicitly permitted teaching the
     *                                Underworld Ascension Method (canon gate)
     * @param remindsHimOfYoungerSelf whether the player reminds Wang Lin of his younger self
     *                                (the open door for life-lesson teaching)
     * @param hasPassedRestrictionTrial whether the player has passed a Restriction-Mountain-style
     *                                patience trial
     */
    public record PlayerRequest(
            int trustLevel,
            Set<RecognizedVirtue> demonstratedVirtues,
            boolean hasSituNanPermission,
            boolean remindsHimOfYoungerSelf,
            boolean hasPassedRestrictionTrial
    ) {
        public PlayerRequest {
            if (trustLevel < 0) trustLevel = 0;
            if (trustLevel > 100) trustLevel = 100;
            if (demonstratedVirtues == null) demonstratedVirtues = Set.of();
        }
    }

    /**
     * The result of a teaching decision, with a canon-grounded reason.
     *
     * @param decision     the outcome
     * @param offering     the offering that was evaluated
     * @param reason       a one-sentence explanation Wang Lin would give (or that narrates the refusal)
     * @param partialNote  if OFFERS_PARTIAL, what the partial form IS; otherwise empty
     */
    public record TeachingResult(
            TeachingDecision decision,
            TeachingOffering offering,
            String reason,
            String partialNote
    ) {
        public TeachingResult {
            if (decision == null) decision = TeachingDecision.REFUSES;
            if (offering == null) {
                throw new IllegalArgumentException("TeachingResult requires an offering");
            }
            if (reason == null) reason = "";
            if (partialNote == null) partialNote = "";
        }

        /** Convenience: was the request accepted (fully or partially)? */
        public boolean accepted() {
            return decision == TeachingDecision.WILL_TEACH || decision == TeachingDecision.OFFERS_PARTIAL;
        }
    }

    /**
     * A single teaching offering — a subject Wang Lin can be asked to teach.
     *
     * @param offeringId           stable identifier (e.g. {@code "RESTRICTION_FLAG_ART"})
     * @param subject              the subject name
     * @param description          one-paragraph canon-grounded description of what is taught
     * @param prerequisiteTrust    the minimum trust level (0..100) required
     * @param prerequisiteVirtues  the virtues the player must have demonstrated
     * @param defaultDecision      the decision returned when prerequisites are not met
     * @param requiresSituNanPermission whether Situ Nan's explicit permission is a hard gate
     * @param requiresRestrictionTrial  whether a passed Restriction-Mountain-style trial is a hard gate
     * @param isFreelySharedToYounger   whether this is freely shared to those who remind him of his younger self
     * @param provenance           source novel, chapters, attestation, confidence, ambiguities
     */
    public record TeachingOffering(
            String offeringId,
            String subject,
            String description,
            int prerequisiteTrust,
            Set<RecognizedVirtue> prerequisiteVirtues,
            TeachingDecision defaultDecision,
            boolean requiresSituNanPermission,
            boolean requiresRestrictionTrial,
            boolean isFreelySharedToYounger,
            Provenance provenance
    ) {
        public TeachingOffering {
            if (offeringId == null || offeringId.isBlank()) {
                throw new IllegalArgumentException("TeachingOffering requires an offeringId");
            }
            if (subject == null || subject.isBlank()) {
                throw new IllegalArgumentException("TeachingOffering '" + offeringId + "' requires a subject");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("TeachingOffering '" + offeringId + "' requires a description");
            }
            if (defaultDecision == null) defaultDecision = TeachingDecision.REFUSES;
            if (prerequisiteVirtues == null) prerequisiteVirtues = Set.of();
            if (prerequisiteTrust < 0) prerequisiteTrust = 0;
            if (prerequisiteTrust > 100) prerequisiteTrust = 100;
            if (provenance == null) {
                throw new IllegalArgumentException("TeachingOffering '" + offeringId + "' requires a Provenance");
            }
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE 16 CANON TEACHING OFFERINGS
    // ───────────────────────────────────────────────────────────────────

    /**
     * CAUTION_LIFE_LESSON — life lessons about caution, survival, and reading the world.
     * The ONE open door: freely shared to those who remind Wang Lin of his younger self.
     */
    public static final TeachingOffering CAUTION_LIFE_LESSON = new TeachingOffering(
            "CAUTION_LIFE_LESSON",
            "Life Lessons of Caution",
            "Wang Lin's hard-won survival wisdom: always plan an escape, never trust a "
                    + "stranger's first word, disguise your realm, hoard your resources, and never "
                    + "let overconfidence kill you. This is the one subject Wang Lin shares "
                    + "FREELY — to those who remind him of his younger, mortal, pre-cultivation "
                    + "self. He sees his past self in the player and does not want them to repeat "
                    + "his losses.",
            10, Set.of(), TeachingDecision.WILL_TEACH,
            false, false, true,
            Provenance.explicit("Renegade Immortal",
                    List.of("E08-E14 (mortal origin → Teng Clan trauma → caution forged)",
                            "Ch. 134 (Earth Escape)", "Ch. 493 (Teleportation Restriction)"),
                    5,
                    "The 'reminds him of his younger self' open door is canon-attested through "
                            + "Wang Lin's recurring pattern of protecting mortals and young cultivators.")
    );

    /**
     * EARTH_ESCAPE_TECHNIQUE — the basic earth-burrowing escape. A survival skill;
     * teachable freely once basic trust is established.
     */
    public static final TeachingOffering EARTH_ESCAPE_TECHNIQUE = new TeachingOffering(
            "EARTH_ESCAPE_TECHNIQUE",
            "Earth Escape Technique",
            "The basic earth-burrowing escape technique Wang Lin developed in the Sea of "
                    + "Devils (Ch. 134). A survival skill — it lets the cultivator melt into "
                    + "earth and flee. Wang Lin considers this a defensive art worth sharing: a "
                    + "living cultivator can cultivate; a dead one cannot. He teaches it once "
                    + "basic trust is established, with no further gates.",
            25, Set.of(RecognizedVirtue.CAUTION), TeachingDecision.WILL_TEACH,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 134 (Earth Escape)"),
                    5,
                    "Canon-attested technique; the teaching-willingness is inferred from his "
                            + "protective pattern toward young cultivators, confidence 4.")
    );

    /**
     * ILLUSIONARY_CIRCLE_TECHNIQUE — wave-based restriction analysis. Teachable to
     * students who demonstrate the patience to study restriction.
     */
    public static final TeachingOffering ILLUSIONARY_CIRCLE_TECHNIQUE = new TeachingOffering(
            "ILLUSIONARY_CIRCLE_TECHNIQUE",
            "Illusionary Circle Technique",
            "The wave-based restriction analysis technique Wang Lin developed at Restriction "
                    + "Mountain (Ch. 180). It lets the cultivator read a restriction through wave "
                    + "resonance without needing to see it directly. Wang Lin teaches this to "
                    + "students who demonstrate PATIENCE — the same patience his 7-year trial "
                    + "required. Without patience, the technique is dangerous (misreading a "
                    + "restriction is fatal).",
            40, Set.of(RecognizedVirtue.PATIENCE), TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 179-180 (Restriction Mountain trial, Illusionary Circle)"),
                    5,
                    "Canon-attested technique; the patience gate is inferred from the 7-year "
                            + "trial's nature, confidence 5.")
    );

    /**
     * RESTRICTION_FLAG_ART — the Restriction Flag technique. Requires demonstrated
     * patience (passed Restriction-Mountain-style trial).
     */
    public static final TeachingOffering RESTRICTION_FLAG_ART = new TeachingOffering(
            "RESTRICTION_FLAG_ART",
            "Restriction Flag Art",
            "The art of forging and wielding Restriction Flags — the foundational tools of "
                    + "restriction work. Wang Lin's signature. He requires DEMONSTRATED PATIENCE "
                    + "before teaching this: the player must have passed a Restriction-Mountain-"
                    + "style trial (or equivalent multi-year discipline). Without that patience, "
                    + "the flag art becomes a toy, and restriction treated as a toy kills.",
            55, Set.of(RecognizedVirtue.PATIENCE), TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 179-180 (Restriction Mountain)", "E43 (7-year trial)",
                            "Ch. 754 (Annihilation Restriction via flag)"),
                    5,
                    "Canon-attested; the patience requirement is explicit in the 7-year trial precedent.")
    );

    /**
     * TELEPORTATION_RESTRICTION — the refined escape. Teachable to tested students.
     */
    public static final TeachingOffering TELEPORTATION_RESTRICTION = new TeachingOffering(
            "TELEPORTATION_RESTRICTION",
            "Teleportation Restriction",
            "The refined teleportation restriction Wang Lin developed from the Earth Escape "
                    + "(Ch. 493). A superior escape — instant spatial relocation along a prepared "
                    + "restriction. Wang Lin teaches this only to students who have passed the "
                    + "restriction trial AND demonstrated caution: a mis-teleported cultivator "
                    + "materializes inside solid rock.",
            60, Set.of(RecognizedVirtue.PATIENCE, RecognizedVirtue.CAUTION),
            TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 493 (Teleportation Restriction)"),
                    5,
                    "Canon-attested; the dual patience+caution gate is inferred from the "
                            + "technique's danger, confidence 4.")
    );

    /**
     * UNDERWORLD_ASCENSION_METHOD — requires Situ Nan's explicit permission.
     * Wang Lin is not free to redistribute a technique he received under contract.
     */
    public static final TeachingOffering UNDERWORLD_ASCENSION_METHOD = new TeachingOffering(
            "UNDERWORLD_ASCENSION_METHOD",
            "Underworld Ascension Method",
            "The Underworld Ascension Method — Situ Nan's signature teaching, transmitted to "
                    + "Wang Lin under contract. Wang Lin CANNOT teach this without Situ Nan's "
                    + "explicit permission. The bead's spirit is the gatekeeper; Wang Lin will "
                    + "refuse even a deeply trusted student if Situ Nan has not consented. If "
                    + "Situ Nan DOES consent, Wang Lin teaches fully.",
            70, Set.of(RecognizedVirtue.CAUTION, RecognizedVirtue.HONESTY_IN_PROBE),
            TeachingDecision.REFUSES,
            true, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 50+ (Situ Nan contract)", "E88 (Underworld Ascension Method)"),
                    5,
                    "The contract-gate is canon-attested; Situ Nan's permission requirement is "
                            + "explicit in the master-student relationship.")
    );

    /**
     * SOUL_LASHER_KARMA_WHIP_USE — relationship-gated. Wang Lin teaches the use of
     * the soul-storing whip lineage to those he deeply trusts.
     */
    public static final TeachingOffering SOUL_LASHER_KARMA_WHIP_USE = new TeachingOffering(
            "SOUL_LASHER_KARMA_WHIP_USE",
            "Soul Lasher / Karma Whip — Use",
            "The use of the soul-storing whip lineage — the Soul Lasher (Ch. 86) and its "
                    + "reforged form the Karma Whip (Ch. 731). Wang Lin teaches the use (not the "
                    + "ownership — see the Opportunity System for the RELATIONSHIP_EXCLUSIVE "
                    + "category) to those he deeply trusts. The whip's soul-storage function is "
                    + "morally weighty; Wang Lin will not teach it to someone who would misuse it.",
            80, Set.of(RecognizedVirtue.FILIAL_DEVOTION, RecognizedVirtue.HONESTY_IN_PROBE),
            TeachingDecision.TESTS_FIRST,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 86 (Soul Lasher)", "Ch. 731 (Karma Whip reforging)"),
                    5,
                    "Canon-attested; the deep-trust gate is inferred from the weapon's moral weight.")
    );

    /**
     * BLOOD_REFINING_TECHNIQUE — offered partial. The full technique is too morally
     * costly; a derived form is teachable.
     */
    public static final TeachingOffering BLOOD_REFINING_TECHNIQUE = new TeachingOffering(
            "BLOOD_REFINING_TECHNIQUE",
            "Blood Refining Technique",
            "The technique of refining treasures and constructs through blood sacrifice. Wang "
                    + "Lin learned it early (Ch. 50+) and used it throughout, but the full form "
                    + "requires morally costly blood. He OFFERS A PARTIAL FORM: a derived "
                    + "technique that uses the cultivator's own blood (not others') — enough to "
                    + "understand the principle without the moral weight. The full technique is "
                    + "withheld.",
            50, Set.of(RecognizedVirtue.CAUTION), TeachingDecision.OFFERS_PARTIAL,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 50+ (Blood Refining Technique)"),
                    5,
                    "Canon-attested; the partial-form offer is a simulation design derived from "
                            + "Wang Lin's demonstrated moral calibration, confidence 4.")
    );

    /**
     * FOUNDATION_STEALING_TECHNIQUE — refuses. Too dark; Wang Lin will not teach
     * the art of stealing another's cultivation foundation.
     */
    public static final TeachingOffering FOUNDATION_STEALING_TECHNIQUE = new TeachingOffering(
            "FOUNDATION_STEALING_TECHNIQUE",
            "Foundation Stealing Technique",
            "The dark art of stealing another cultivator's Foundation — Wang Lin used it "
                    + "(Ch. 86) but considers it a stain. He REFUSES to teach it. The technique "
                    + "destroys the victim's cultivation and is a karmic burden Wang Lin does not "
                    + "wish on any student. The refusal is absolute; no trust level overcomes it.",
            100, Set.of(), TeachingDecision.REFUSES,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 86 (Foundation Stealing)"),
                    5,
                    "Canon-attested as a technique Wang Lin used; the refusal-to-teach is "
                            + "inferred from his characterization, confidence 5.")
    );

    /**
     * RESTRICTION_ESSENCE_INSIGHT — tests first. The deepest restriction insight;
     * Wang Lin tests the student's readiness before sharing any of it.
     */
    public static final TeachingOffering RESTRICTION_ESSENCE_INSIGHT = new TeachingOffering(
            "RESTRICTION_ESSENCE_INSIGHT",
            "Restriction Essence — Insight",
            "Wang Lin's comprehension of the Restriction Essence itself (Ch. 1715, E80) — "
                    + "the unity underlying all restrictions. This is his deepest restriction "
                    + "insight and the capstone of his restriction career. He TESTS the student "
                    + "severely before sharing even a glimpse: the student must demonstrate not "
                    + "just patience but a genuine restriction Dao. The test is multi-year.",
            85, Set.of(RecognizedVirtue.PATIENCE, RecognizedVirtue.HEAVEN_DEFYING_RESOLVE),
            TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1715 (Restriction Essence)", "E80"),
                    5,
                    "Canon-attested as Wang Lin's capstone insight; the severe test gate is "
                            + "inferred from the insight's depth, confidence 5.")
    );

    /**
     * ANNIHILATION_RESTRICTION — tests first. One of the Four Great Restrictions.
     */
    public static final TeachingOffering ANNIHILATION_RESTRICTION = new TeachingOffering(
            "ANNIHILATION_RESTRICTION",
            "Annihilation Restriction",
            "The Annihilation Restriction — the first of the Four Great Restrictions Wang "
                    + "Lin collected (Ch. 754, E55). A destructive restriction of immense power. "
                    + "He TESTS the student before teaching: the restriction is too dangerous to "
                    + "share casually, and a student who misuses it draws karmic retribution on "
                    + "both themselves and the teacher.",
            75, Set.of(RecognizedVirtue.PATIENCE, RecognizedVirtue.CAUTION),
            TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 754 (Annihilation Restriction)", "E55"),
                    5,
                    "Canon-attested as one of the Four Great Restrictions.")
    );

    /**
     * TIME_RESTRICTION — refuses. Personal comprehension; Wang Lin does not teach
     * the Time Restriction because it is bound to his personal Dao and the bead.
     */
    public static final TeachingOffering TIME_RESTRICTION = new TeachingOffering(
            "TIME_RESTRICTION",
            "Time Restriction",
            "The Time Restriction (Ch. 1223) — one of the Four Great Restrictions. Wang Lin "
                    + "REFUSES to teach this. His comprehension of Time is bound to the Heaven-"
                    + "Defying Bead's time-dilation interior and his personal Dao; it cannot be "
                    + "transmitted without transmitting the bead itself, which is impossible "
                    + "(ABSOLUTE_UNIQUE). He will describe the restriction's existence but not "
                    + "teach its use.",
            100, Set.of(), TeachingDecision.REFUSES,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1223 (Time Restriction)"),
                    5,
                    "Canon-attested; the refusal is inferred from the bead-binding, confidence 5.")
    );

    /**
     * LIFE_DEATH_RESTRICTION — offers partial. The full comprehension requires the
     * mortal-life practice; a partial form is teachable.
     */
    public static final TeachingOffering LIFE_DEATH_RESTRICTION = new TeachingOffering(
            "LIFE_DEATH_RESTRICTION",
            "Life-Death Restriction",
            "The Life-Death Restriction (Ch. 1229) — one of the Four Great Restrictions. "
                    + "Wang Lin's full comprehension required the mortal lifetime with Wang Ping "
                    + "(E50, Ch. 701); that experience cannot be transmitted. He OFFERS A PARTIAL "
                    + "FORM: the theoretical framework and the basic life-death cycle meditation. "
                    + "The deep comprehension requires the student's own mortal-life practice.",
            70, Set.of(RecognizedVirtue.FILIAL_DEVOTION, RecognizedVirtue.PATIENCE),
            TeachingDecision.OFFERS_PARTIAL,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1229 (Life-Death Restriction)", "Ch. 701 (Wang Ping mortal life)", "E50"),
                    5,
                    "Canon-attested; the partial-form offer is inferred from the experience-binding, "
                            + "confidence 5.")
    );

    /**
     * ANCIENT_SOUL_RESTRICTION — tests first. The fourth of the Four Great Restrictions.
     */
    public static final TeachingOffering ANCIENT_SOUL_RESTRICTION = new TeachingOffering(
            "ANCIENT_SOUL_RESTRICTION",
            "Ancient Soul Restriction",
            "The Ancient Soul Restriction (Ch. 1697) — the fourth of the Four Great "
                    + "Restrictions. A restriction that operates on the soul itself. Wang Lin "
                    + "TESTS the student severely: misuse of this restriction shatters souls, and "
                    + "the karmic weight is extreme. The test includes demonstrated respect for "
                    + "the dead and the soul.",
            80, Set.of(RecognizedVirtue.PATIENCE, RecognizedVirtue.FILIAL_DEVOTION),
            TeachingDecision.TESTS_FIRST,
            false, true, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1697 (Ancient Soul Restriction)"),
                    5,
                    "Canon-attested as one of the Four Great Restrictions.")
    );

    /**
     * SLAUGHTER_DAO_INSIGHT — refuses. Wang Lin's personal Dao, rooted in the Teng
     * Clan massacre; not teachable. Each cultivator's Dao is their own.
     */
    public static final TeachingOffering SLAUGHTER_DAO_INSIGHT = new TeachingOffering(
            "SLAUGHTER_DAO_INSIGHT",
            "Slaughter Dao — Insight",
            "Wang Lin's Slaughter Dao (E14, CD-31) — rooted in the Teng Clan massacre of "
                    + "his family. He REFUSES to teach this. A Dao is not a technique; it is the "
                    + "shape of a cultivator's suffering and resolve. Wang Lin's Slaughter Dao is "
                    + " HIS, forged by HIS losses. A student must find their own Dao. He will "
                    + "describe the Slaughter Dao's existence but will not teach its formation.",
            100, Set.of(), TeachingDecision.REFUSES,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("E14 (Teng Clan massacre → Slaughter Dao)", "CD-31 (Slaughter Dao)"),
                    5,
                    "Canon-attested; the refusal is inferred from the Dao's personal nature, "
                            + "confidence 5.")
    );

    /**
     * HEAVEN_DEFYING_BEAD_MYSTERIES — NEVER shared. The bead is ABSOLUTE_UNIQUE.
     * Only derivative understanding is possible.
     */
    public static final TeachingOffering HEAVEN_DEFYING_BEAD_MYSTERIES = new TeachingOffering(
            "HEAVEN_DEFYING_BEAD_MYSTERIES",
            "Heaven-Defying Bead — Core Mysteries",
            "The core mysteries of the Heaven-Defying Bead — its 9 Parts, its Five Elements "
                    + "alignment, its interior dimension, its time dilation, its deepest secrets "
                    + "(Li Muwan's Nascent Soul, Samsara). Wang Lin NEVER shares these. The bead "
                    + "is ABSOLUTE_UNIQUE (per the Opportunity System); its mysteries cannot be "
                    + "transmitted because they are bound to the bead's singular existence. The "
                    + "player can obtain derivative understanding (the bead's PRINCIPLES) but not "
                    + "the bead's MYSTERIES.",
            100, Set.of(), TeachingDecision.REFUSES,
            false, false, false,
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 8 (bead origin)", "E88 (bead mysteries)",
                            "Ch. 1433 (the '2,000 years alone' — bead interior depth)"),
                    5,
                    "Canon-attested; the ABSOLUTE_UNIQUE classification is from the Opportunity "
                            + "System (Layer 2), which itself is canon-derived.")
    );

    /**
     * The complete list of Wang Lin's 16 teaching offerings.
     */
    public static final List<TeachingOffering> ALL_OFFERINGS = List.of(
            CAUTION_LIFE_LESSON,
            EARTH_ESCAPE_TECHNIQUE,
            ILLUSIONARY_CIRCLE_TECHNIQUE,
            RESTRICTION_FLAG_ART,
            TELEPORTATION_RESTRICTION,
            UNDERWORLD_ASCENSION_METHOD,
            SOUL_LASHER_KARMA_WHIP_USE,
            BLOOD_REFINING_TECHNIQUE,
            FOUNDATION_STEALING_TECHNIQUE,
            RESTRICTION_ESSENCE_INSIGHT,
            ANNIHILATION_RESTRICTION,
            TIME_RESTRICTION,
            LIFE_DEATH_RESTRICTION,
            ANCIENT_SOUL_RESTRICTION,
            SLAUGHTER_DAO_INSIGHT,
            HEAVEN_DEFYING_BEAD_MYSTERIES
    );

    /** Look up a teaching offering by its offeringId or subject. */
    public static TeachingOffering byId(String offeringId) {
        if (offeringId == null) return null;
        for (TeachingOffering o : ALL_OFFERINGS) {
            if (o.offeringId().equals(offeringId) || o.subject().equalsIgnoreCase(offeringId)) return o;
        }
        return null;
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE DECISION ENGINE
    // ───────────────────────────────────────────────────────────────────

    /**
     * Evaluate whether Wang Lin will teach the requested subject to the player.
     *
     * <p>The engine applies the four canon-faithful gates in order:
     * <ol>
     *   <li><b>ABSOLUTE_UNIQUE / bead-binding</b> — if the offering is
     *       HEAVEN_DEFYING_BEAD_MYSTERIES, the answer is always REFUSES
     *       (the bead is singular).</li>
     *   <li><b>Contract gate</b> — if the offering requires Situ Nan's
     *       permission and the player does not have it, REFUSES.</li>
     *   <li><b>Trial gate</b> — if the offering requires a passed
     *       Restriction trial and the player has not passed, TESTS_FIRST.</li>
     *   <li><b>Open door</b> — if the offering is freely shared to those
     *       who remind him of his younger self, and the player does,
     *       WILL_TEACH.</li>
     *   <li><b>Trust + virtue gate</b> — if the player meets the trust
     *       threshold and has demonstrated the prerequisite virtues,
     *       WILL_TEACH; otherwise the offering's defaultDecision.</li>
     * </ol>
     *
     * @param player  the player's current state
     * @param subject the offeringId or subject name
     * @return the teaching result (decision + reason + partial note)
     */
    public static TeachingResult canTeach(PlayerRequest player, String subject) {
        TeachingOffering offering = byId(subject);
        if (offering == null) {
            return new TeachingResult(
                    TeachingDecision.REFUSES, unknownOffering(subject),
                    "Wang Lin does not know of a teaching called '" + subject + "'.", "");
        }

        // Gate 1: ABSOLUTE_UNIQUE / bead-binding
        if (offering.offeringId().equals("HEAVEN_DEFYING_BEAD_MYSTERIES")) {
            return new TeachingResult(
                    TeachingDecision.REFUSES, offering,
                    "The Heaven-Defying Bead is singular. Its mysteries cannot be transmitted; "
                            + "they are bound to the bead's existence. I can share the bead's "
                            + "principles, but not its mysteries.", "");
        }

        // Gate 2: Contract gate (Situ Nan's permission)
        if (offering.requiresSituNanPermission() && !player.hasSituNanPermission()) {
            return new TeachingResult(
                    TeachingDecision.REFUSES, offering,
                    "The " + offering.subject() + " was transmitted to me under contract with "
                            + "Senior Situ Nan. I cannot teach it without his explicit permission. "
                            + "Ask him, not me.", "");
        }

        // Gate 3: Trial gate (Restriction-Mountain-style patience trial)
        if (offering.requiresRestrictionTrial() && !player.hasPassedRestrictionTrial()) {
            return new TeachingResult(
                    TeachingDecision.TESTS_FIRST, offering,
                    "The " + offering.subject() + " demands the patience I learned in seven "
                            + "years at Restriction Mountain. Prove that patience, and I will "
                            + "teach you. Without it, the art would kill you.", "");
        }

        // Gate 4: Open door — freely shared to those who remind him of his younger self
        if (offering.isFreelySharedToYounger() && player.remindsHimOfYoungerSelf()) {
            return new TeachingResult(
                    TeachingDecision.WILL_TEACH, offering,
                    "You remind me of myself, before the Teng Clan, before the bead. I will "
                            + "teach you what I have learned. Do not repeat my losses.", "");
        }

        // Gate 5: Trust + virtue gate
        boolean trustMet = player.trustLevel() >= offering.prerequisiteTrust();
        boolean virtuesMet = player.demonstratedVirtues().containsAll(offering.prerequisiteVirtues());

        if (trustMet && virtuesMet) {
            return new TeachingResult(
                    TeachingDecision.WILL_TEACH, offering,
                    "You have shown me enough. I will teach you the " + offering.subject() + ".",
                    "");
        }

        // Default: apply the offering's default decision with a reason
        return switch (offering.defaultDecision()) {
            case WILL_TEACH -> new TeachingResult(
                    TeachingDecision.WILL_TEACH, offering,
                    "I will teach you the " + offering.subject() + ".", "");
            case REFUSES -> new TeachingResult(
                    TeachingDecision.REFUSES, offering,
                    refuseReason(offering), partialNoteIfAny(offering));
            case OFFERS_PARTIAL -> new TeachingResult(
                    TeachingDecision.OFFERS_PARTIAL, offering,
                    "The full " + offering.subject() + " is not mine to give. But I can offer "
                            + "you a partial form.",
                    partialNoteIfAny(offering));
            case TESTS_FIRST -> new TeachingResult(
                    TeachingDecision.TESTS_FIRST, offering,
                    "The " + offering.subject() + " is not given; it is earned. Prove your "
                            + "patience and your resolve, and I will teach you.", "");
        };
    }

    /** A canonical reason for a flat refusal, canon-grounded per offering. */
    private static String refuseReason(TeachingOffering offering) {
        return switch (offering.offeringId()) {
            case "FOUNDATION_STEALING_TECHNIQUE" ->
                    "The Foundation Stealing Technique is a stain I carry. I will not teach it. "
                            + "Find your own foundation; do not steal another's.";
            case "TIME_RESTRICTION" ->
                    "The Time Restriction is bound to my bead and my Dao. It cannot be "
                            + "transmitted without transmitting the bead itself, which is "
                            + "impossible. I will describe its existence; I will not teach its use.";
            case "SLAUGHTER_DAO_INSIGHT" ->
                    "My Slaughter Dao is mine — forged by my losses, my family's blood. A Dao "
                            + "is not a technique. You must find your own.";
            case "UNDERWORLD_ASCENSION_METHOD" ->
                    "The Underworld Ascension Method is Senior Situ Nan's to give, not mine. "
                            + "I will not teach it without his permission.";
            default ->
                    "I will not teach the " + offering.subject() + ".";
        };
    }

    /** The partial-form note, if the offering has one. */
    private static String partialNoteIfAny(TeachingOffering offering) {
        return switch (offering.offeringId()) {
            case "BLOOD_REFINING_TECHNIQUE" ->
                    "Partial form: the self-blood refinement — using your own blood, not "
                            + "another's. Enough to understand the principle; the full technique "
                            + "is withheld.";
            case "LIFE_DEATH_RESTRICTION" ->
                    "Partial form: the theoretical framework and the basic life-death cycle "
                            + "meditation. The deep comprehension requires your own mortal-life "
                            + "practice, which I cannot give you.";
            default -> "";
        };
    }

    /** A placeholder offering for unknown subjects, so the result is well-formed. */
    private static TeachingOffering unknownOffering(String subject) {
        return new TeachingOffering(
                "UNKNOWN", subject, "An unknown subject.", 100, Set.of(),
                TeachingDecision.REFUSES, false, false, false,
                Provenance.inferred("Renegade Immortal", List.of(), 1,
                        "Subject '" + subject + "' is not a recognized Wang Lin teaching offering."));
    }
}
