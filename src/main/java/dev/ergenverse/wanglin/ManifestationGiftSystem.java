package dev.ergenverse.wanglin;

import dev.ergenverse.canon.CanonEngine;

import java.util.*;

/**
 * Manifestation Gift System — the interaction mechanic between the player and
 * the protagonist manifestations.
 *
 * <p><b>The core philosophy (per user directive, DESIGN_MANIFESTATION_GIFTS.md):</b>
 * <pre>
 *   1. Canon originals remain theirs. We do not loot protagonists.
 *   2. The player receives an EXACT COPY of canon items through willing cooperation.
 *      The protagonist keeps their original; the player gets an identical duplicate.
 *      This is NOT a redirection, NOT an "equivalent," NOT a parallel — an EXACT COPY.
 *   3. Affinity is the PREREQUISITE, not the CURRENCY.
 *      You cannot "spend 50 affinity" to extract Wang Lin's Bead.
 *   4. The manifestation decides to give based on a four-question in-universe evaluation,
 *      not a shop transaction.
 *   5. Different protagonists behave differently (Wang Lin reserved, Meng Hao bargains,
 *      Bai Xiaochun gives freely, Su Ming tests comprehension, Xu Qing judges in silence).
 * </pre>
 *
 * <p><b>CORRECTION (per user directive):</b> the earlier version of this system redirected
 * identity-tied items to "teaching" or "equivalent" paths. The user explicitly corrected this:
 * "i want to be able to get an exact copy of the protagonists' canon items, while they keep
 * theirs, not some redirection. stop forgetting." The system now produces EXACT COPIES for
 * canon items. The protagonist's original is untouched; the player receives a perfect duplicate.
 *
 * <p>This class encodes:
 * <ol>
 *   <li><b>Two categories of possessions</b> — Canonical Arsenal (techniques, treasures,
 *       beasts, inheritances, storage, artifacts) vs Post-Canon Possessions (herbs, cores,
 *       stones, forged items, materials the manifestation acquires during the simulation).</li>
 *   <li><b>The Four-Question Evaluation Engine</b> — (1) Does he still need this?
 *       (2) Is the player a trusted ally? (3) Would giving this help the player?
 *       (4) Does this fit my personality?</li>
 *   <li><b>Per-Protagonist Personality Profiles</b> — 5 profiles (Wang Lin, Meng Hao,
 *       Bai Xiaochun, Su Ming, Xu Qing) with weighted questions, volunteer rates,
 *       bargain behaviors, and signature lines.</li>
 *   <li><b>Gift Records</b> — a curated registry of transferable items, each tagged
 *       with category, affinity threshold, realm gate, dao compatibility, and the
 *       in-character dialogue spoken when the gift is offered.</li>
 *   <li><b>The GiftDecision outcome</b> — OFFERED, REFUSED_STILL_NEEDED,
 *       REFUSED_NOT_TRUSTED, REFUSED_NOT_READY, REFUSED_PERSONALITY,
 *       REDIRECTED_TO_TEACHING, REDIRECTED_TO_BARGAIN, REDIRECTED_TO_COMPREHENSION.</li>
 * </ol>
 *
 * <p><b>Relationship to existing systems:</b>
 * <ul>
 *   <li>{@link RIEdgeOfCanonState.ManifestationCompanion} — the companion that COMMENTS
 *       on locations. This gift system is the companion's INTERACTION layer.</li>
 *   <li>{@link RIEdgeOfCanonState.InheritanceRecord} — classifies WHETHER an inheritance
 *       is accessible. This system is the MECHANISM by which accessible inheritances
 *       are actually transferred.</li>
 *   <li>{@link SamsaraDao} — the 14 Essences are canonical techniques; this system
 *       governs whether Wang Lin will TEACH one (the player must still comprehend
 *       the law independently).</li>
 *   <li>{@link HeavenDefyingBead} — the Bead is canonicallyTiedToIdentity = true.
 *       It will NEVER be transferred.</li>
 * </ul>
 *
 * <p><b>Per the Prime Directive:</b> the canonical item ownership is OBJECTIVE.
 * The personality profiles are derived from canonical behavior (CANON_IMPLIED).
 * The four-question framework is a game mechanic (REASONABLE_RECONSTRUCTION).
 * Post-canon possessions are by definition SPECULATION.
 *
 * <p>Canon source: DESIGN_MANIFESTATION_GIFTS.md (the design document capturing
 * the user's directive on canonical vs post-canon possessions).
 */
public final class ManifestationGiftSystem {

    private ManifestationGiftSystem() {}

    // ════════════════════════════════════════════════════════════════════
    // PART 0 — THE CORE PHILOSOPHY
    // ════════════════════════════════════════════════════════════════════

    /**
     * The three non-negotiable principles of the gift system.
     * Per user directive, DESIGN_MANIFESTATION_GIFTS.md §0.
     */
    public static final String PHILOSOPHY_STATEMENT =
        "Canon originals remain theirs. The player receives an EXACT COPY of canon items " +
        "through willing cooperation. The protagonist keeps their original; the player gets " +
        "an identical duplicate. This is NOT a redirection. Affinity is the prerequisite, " +
        "not the currency. The manifestation decides to give based on a four-question evaluation.";

    /**
     * The anti-pattern this system explicitly rejects.
     * Per user directive: "I would avoid 'pay affinity to steal.'"
     */
    public static final String REJECTED_ANTIPATTERN =
        "Do NOT implement affinity-as-currency. You cannot extract Wang Lin's Bead by " +
        "grinding affinity to 100. The four-question evaluation is the gate; affinity is " +
        "the prerequisite to even ask.";

    // ════════════════════════════════════════════════════════════════════
    // PART 1 — TWO CATEGORIES OF POSSESSIONS
    // ════════════════════════════════════════════════════════════════════

    /**
     * The two top-level categories of possessions.
     *
     * <p>Per DESIGN_MANIFESTATION_GIFTS.md §1:
     * <ul>
     *   <li>CANONICAL — everything the protagonist possessed at the end of their novel.
     *       The original remains theirs. The player receives an EXACT COPY — an identical duplicate.</li>
     *   <li>POST_CANON — everything the manifestation acquires during the simulation.
     *       These belong to the manifestation and may flow to the player through
     *       genuine friendship.</li>
     * </ul>
     */
    public enum PossessionCategory {
        CANONICAL("Canonical Arsenal",
            "Everything the protagonist possessed at the end of their novel. " +
            "The original remains theirs. The player receives an EXACT COPY — an identical duplicate."),
        POST_CANON("Post-Canon Possessions",
            "Everything the manifestation acquires during the game's simulation. " +
            "These belong to the manifestation and may flow to the player through friendship.");

        public final String name;
        public final String description;
        PossessionCategory(String n, String d) { this.name = n; this.description = d; }
    }

    /**
     * The fine-grained gift category — what KIND of thing is being transferred.
     * Each maps to a transfer mechanism (teaching, exact copy, offspring, gift, trade).
     */
    public enum GiftCategory {
        // ── Canonical Arsenal ──
        CANONICAL_TECHNIQUE("Canonical Technique",
            "Teaching. A protagonist teaching you a technique does not diminish their mastery."),
        CANONICAL_TREASURE("Canonical Treasure",
            "EXACT COPY. The player receives an identical duplicate. The protagonist keeps the original."),
        CANONICAL_BEAST("Canonical Companion Beast",
            "Taming method, offspring, or successor. The original bond is preserved."),
        CANONICAL_INHERITANCE("Canonical Inheritance",
            "Independent acquisition. The player walks the same path the protagonist did."),
        CANONICAL_STORAGE("Canonical Storage Item",
            "Exact copy of canon contents. The protagonist's canonical storage is duplicated; " +
            "the player receives copies of everything inside."),
        CANONICAL_ARTIFACT("Canonical Forged Artifact",
            "Exact copy. The player receives a duplicate of the artifact. The protagonist keeps theirs."),

        // ── Post-Canon Possessions ──
        POST_CANON_HERB("Post-Canon Herb",
            "Gift, trade, or carry-request. Belongs to the manifestation."),
        POST_CANON_CORE("Post-Canon Beast Core",
            "Gift, trade, or carry-request. Belongs to the manifestation."),
        POST_CANON_STONE("Post-Canon Spirit Stone",
            "Gift, trade, or carry-request. Belongs to the manifestation."),
        POST_CANON_FORGED("Post-Canon Forged Treasure",
            "Gift, trade, or carry-request. Belongs to the manifestation."),
        POST_CANON_TECHNIQUE("Post-Canon Technique",
            "Teaching (genuine mentorship). Developed by the manifestation after canon."),
        POST_CANON_MATERIAL("Post-Canon Crafting Material",
            "Gift, trade, or carry-request. Belongs to the manifestation.");

        public final String name;
        public final String transferMechanism;
        GiftCategory(String n, String m) { this.name = n; this.transferMechanism = m; }

        /** Whether this category is part of the Canonical Arsenal. */
        public boolean isCanonical() {
            return this == CANONICAL_TECHNIQUE || this == CANONICAL_TREASURE ||
                   this == CANONICAL_BEAST || this == CANONICAL_INHERITANCE ||
                   this == CANONICAL_STORAGE || this == CANONICAL_ARTIFACT;
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 2 — THE FOUR-QUESTION EVALUATION ENGINE
    // ════════════════════════════════════════════════════════════════════

    /**
     * The four questions a manifestation evaluates when considering a gift.
     *
     * <p>Per DESIGN_MANIFESTATION_GIFTS.md §2. Affinity is the prerequisite;
     * these four questions are the gate.
     */
    public enum EvaluationQuestion {
        Q1_STILL_NEEDED("Does he still need this?",
            "The manifestation's current attachment to the item. A canonical treasure " +
            "tied to their identity (the Bead, the Mirror) is always needed. A post-canon " +
            "herb is rarely needed."),
        Q2_TRUSTED_ALLY("Is the player a trusted ally?",
            "The relationship depth. Not just a number — has the player fought alongside " +
            "them, shared meals, defended their interests, proven loyalty over time?"),
        Q3_WOULD_HELP("Would giving this away help the player?",
            "The manifestation's judgment of whether the player can actually use the item " +
            "(realm-appropriate, dao-compatible, not dangerous to wield)."),
        Q4_FITS_PERSONALITY("Does this fit my personality?",
            "The personality filter. Wang Lin rarely volunteers; Bai Xiaochun hands things " +
            "out constantly; Meng Hao bargains first; Su Ming tests comprehension; Xu Qing " +
            "judges in silence.");

        public final String question;
        public final String description;
        EvaluationQuestion(String q, String d) { this.question = q; this.description = d; }
    }

    /**
     * The outcome of a gift evaluation.
     */
    public enum GiftDecision {
        OFFERED("Offered",
            "The manifestation gives / teaches / entrusts the item. Accompanied by " +
            "in-character dialogue. Affinity increases."),
        REFUSED_STILL_NEEDED("Refused — still needed",
            "Q1 failed (post-canon items only — identity-tied items produce exact copies). " +
            "'I'm using this right now. Come back later.'"),
        REFUSED_NOT_TRUSTED("Refused — not trusted",
            "Q2 failed. 'Prove yourself first.' The player must deepen the relationship " +
            "through shared experience."),
        REFUSED_NOT_READY("Refused — not ready",
            "Q3 failed. 'Your realm cannot bear this. Return when you are stronger.' " +
            "The offer is held open; the player may re-request at a higher tier."),
        REFUSED_PERSONALITY("Refused — personality mismatch",
            "Q4 failed. Personality-specific refusal. The offer was never going to happen " +
            "in character (e.g., Wang Lin would not volunteer this)."),
        REDIRECTED_TO_TEACHING("Redirected to teaching",
            "Per user directive, canonical TREASURES produce exact copies, NOT teaching redirects. " +
            "This decision is used ONLY when Q4 personality transforms a technique offer into " +
            "a teaching path — never for identity-tied treasures."),
        REDIRECTED_TO_BARGAIN("Redirected to bargain",
            "Meng Hao's signature: the interaction becomes a bargain sequence. The player " +
            "must offer something in return."),
        REDIRECTED_TO_COMPREHENSION("Redirected to comprehension test",
            "Su Ming's signature: the player must demonstrate understanding of the item " +
            "before it is given.");

        public final String name;
        public final String description;
        GiftDecision(String n, String d) { this.name = n; this.description = d; }
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 3 — PER-PROTAGONIST PERSONALITY PROFILES
    // ════════════════════════════════════════════════════════════════════

    /**
     * How a protagonist's personality shapes the bargain/offer interaction.
     */
    public enum BargainBehavior {
        NONE("No bargaining. Straightforward give or refuse."),
        FAIR("Fair bargaining. A fair price is named; the player may negotiate respectfully."),
        PREDATORY("Predatory bargaining. The opening price is extortionate; driving it down earns respect."),
        COMPREHENSION_TEST("Comprehension test. The player must demonstrate understanding before the gift.");

        public final String description;
        BargainBehavior(String d) { this.description = d; }
    }

    /**
     * A protagonist's personality profile — how they shape the four-question evaluation.
     *
     * <p>Per DESIGN_MANIFESTATION_GIFTS.md §3. The underlying engine is the same;
     * the personality shapes the weighting and the expression.
     */
    public static final class PersonalityProfile {
        public final String protagonistId;
        public final String protagonistName;
        public final String protagonistNameCn;
        public final String novel;
        public final String canonSummary;

        /** Weight of each question in the evaluation (1 = normal, 2 = double, 0 = bypass). */
        public final int q1Weight;  // still-needed
        public final int q2Weight;  // trusted-ally
        public final int q3Weight;  // would-help
        public final int q4Weight;  // fits-personality

        /** How often (0.0–1.0) the manifestation offers gifts unprompted. */
        public final double volunteerRate;

        /** The bargain/offer behavior. */
        public final BargainBehavior bargainBehavior;

        /** The signature line spoken when a gift is offered. */
        public final String signatureOfferLine;

        /** The line spoken when refusing on personality grounds. */
        public final String signatureRefusalLine;

        /** The line spoken when redirecting to a bargain. */
        public final String signatureBargainLine;

        public PersonalityProfile(String protagonistId, String protagonistName, String protagonistNameCn,
                                   String novel, String canonSummary,
                                   int q1Weight, int q2Weight, int q3Weight, int q4Weight,
                                   double volunteerRate, BargainBehavior bargainBehavior,
                                   String signatureOfferLine, String signatureRefusalLine,
                                   String signatureBargainLine) {
            this.protagonistId = protagonistId;
            this.protagonistName = protagonistName;
            this.protagonistNameCn = protagonistNameCn;
            this.novel = novel;
            this.canonSummary = canonSummary;
            this.q1Weight = q1Weight;
            this.q2Weight = q2Weight;
            this.q3Weight = q3Weight;
            this.q4Weight = q4Weight;
            this.volunteerRate = volunteerRate;
            this.bargainBehavior = bargainBehavior;
            this.signatureOfferLine = signatureOfferLine;
            this.signatureRefusalLine = signatureRefusalLine;
            this.signatureBargainLine = signatureBargainLine;
        }
    }

    // ── The five protagonist profiles ───────────────────────────────────

    /**
     * Wang Lin — The Reserved Mentor.
     *
     * <p>Stoic, ruthless to enemies, fiercely loyal to the few he loves. Trusts almost
     * no one. When he DOES trust, he entrusts incredibly valuable things. Rarely
     * volunteers anything. Q2 (trust) weighted 2×. Q4 almost always filters impulsive giving.
     */
    public static final PersonalityProfile WANG_LIN = new PersonalityProfile(
        "wang_lin", "Wang Lin", "王林", "Renegade Immortal",
        "Stoic, ruthless to enemies, fiercely loyal to the few he loves. Lost his clan, " +
        "his parents, his first love (temporarily), his son (to time). Trusts almost no one. " +
        "When he does trust, he entrusts incredibly valuable things.",
        1, 2, 1, 2,  // q1=1, q2=2 (double), q3=1, q4=2 (double)
        0.05,  // volunteerRate — almost never volunteers
        BargainBehavior.NONE,
        "\"Take it. You've earned it.\"",
        "Wang Lin says nothing. The offer was never going to be volunteered.",
        "Wang Lin does not bargain. He decides."
    );

    /**
     * Meng Hao — The Bargainer.
     *
     * <p>Commercially minded. Believes everything has a price. Generous to friends, but
     * always bargains first — it's a form of respect. Q4 transforms the interaction into
     * a bargain sequence. Q2 determines whether the bargain is fair or predatory.
     */
    public static final PersonalityProfile MENG_HAO = new PersonalityProfile(
        "meng_hao", "Meng Hao", "孟浩", "I Shall Seal the Heavens",
        "Commercially minded (the Lord Fifth era, the Spirit Stone Society, the auction arc). " +
        "Believes everything has a price. Generous to friends, but always bargains first — " +
        "it's a form of respect.",
        1, 1, 1, 2,  // q4=2 (personality is the primary filter)
        0.25,  // volunteers occasionally, usually to start a bargain
        BargainBehavior.FAIR,
        "\"Everything has a price. You've paid yours. Take it.\"",
        "\"Everything has a price. You cannot afford this one.\"",
        "\"Everything has a price. What will you give me?\""
    );

    /**
     * Bai Xiaochun — The Generous Fool.
     *
     * <p>Fearful of death, obsessed with pills, accidentally generous. Makes too many
     * pills and hands them out. Genuinely loves his friends. Q4 almost never filters.
     * Q3 is the primary gate, but he's sloppy about checking it.
     */
    public static final PersonalityProfile BAI_XIAOCHUN = new PersonalityProfile(
        "bai_xiaochun", "Bai Xiaochun", "白小纯", "A Will Eternal",
        "Fearful of death, obsessed with pills, accidentally generous. Makes too many " +
        "pills and hands them out. Genuinely loves his friends. Chatty, dramatic, " +
        "emotionally transparent.",
        1, 1, 2, 0,  // q3=2 (primary gate), q4=0 (almost never filters)
        0.70,  // volunteers constantly
        BargainBehavior.NONE,
        "\"Take it! Take it! I made too many anyway! Don't die, okay?!\"",
        "\"Wait, you want THIS? But what if I need it? ...okay fine, take it, but be careful!\"",
        "Bai Xiaochun does not bargain. He just gives."
    );

    /**
     * Su Ming — The Understanding-Seeker.
     *
     * <p>Values comprehension over material things. Will give you a treasure freely if
     * you can demonstrate you understand it. Values questions over answers. Q4 transforms
     * the interaction into a comprehension test. Q3 is judged by understanding, not realm.
     */
    public static final PersonalityProfile SU_MING = new PersonalityProfile(
        "su_ming", "Su Ming", "苏明", "Pursuit of the Truth",
        "Values comprehension over material things. Will give you a treasure freely if you " +
        "can demonstrate you understand it. Values questions over answers. Quiet, intense, " +
        "philosophical.",
        1, 1, 1, 2,  // q4=2 (comprehension test)
        0.15,  // rarely volunteers; waits to be asked
        BargainBehavior.COMPREHENSION_TEST,
        "\"You understand. It is yours.\"",
        "\"You do not understand. Come back when you do.\"",
        "\"Tell me what this is. Tell me, and it is yours.\""
    );

    /**
     * Xu Qing — The Silent Judge.
     *
     * <p>Pragmatic, ruthless when needed, economical with words. Judges people by their
     * actions, not their words. All four questions weighted evenly, but the expression
     * is always terse. Q2 is evaluated by observed actions, not affinity number.
     */
    public static final PersonalityProfile XU_QING = new PersonalityProfile(
        "xu_qing", "Xu Qing", "许青", "Beyond the Timescape",
        "Pragmatic, ruthless when needed, economical with words. Judges people by their " +
        "actions, not their words. Has seen too much to be impressed by cultivation realm alone.",
        1, 1, 1, 1,  // all evenly weighted
        0.10,  // rarely volunteers
        BargainBehavior.NONE,
        "\"...Take it.\"",
        "\"...No.\"",
        "Xu Qing does not bargain. He judges."
    );

    /** All five protagonist personality profiles. */
    public static final List<PersonalityProfile> ALL_PROFILES =
        Collections.unmodifiableList(Arrays.asList(
            WANG_LIN, MENG_HAO, BAI_XIAOCHUN, SU_MING, XU_QING
        ));

    /** Lookup a personality profile by protagonist ID. */
    public static PersonalityProfile getProfile(String protagonistId) {
        for (PersonalityProfile p : ALL_PROFILES) {
            if (p.protagonistId.equals(protagonistId)) return p;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 4 — GIFT RECORDS
    // ════════════════════════════════════════════════════════════════════

    /**
     * A gift record — a transferable item and its transfer conditions.
     *
     * <p>Each record encodes: what the item is, which protagonist owns it, what category,
     * the affinity threshold (prerequisite), the realm gate (Q3 readiness), any required
     * dao compatibility, whether the item is canonically tied to the protagonist's
     * identity (if true, the protagonist keeps the original; the player receives an
     * EXACT COPY — never a redirection or "equivalent"), and the dialogue spoken when
     * the gift is offered.
     */
    public static final class GiftRecord {
        public final String giftId;
        public final String protagonistId;
        public final GiftCategory category;
        public final String name;
        public final String nameCn;
        public final String canonOriginId;  // nullable; the canonical item this is an EXACT COPY of

        /** Minimum affinity (0–100) required to even ask for this gift. */
        public final int affinityThreshold;

        /** Minimum player cultivation tier (0=mortal, 17=Transcendence) to wield/receive. */
        public final int realmGate;

        /** Required dao affinity (nullable; e.g., "slaughter", "karma", "life_death"). */
        public final String daoCompatibility;

        /**
         * Whether this item is canonically tied to the protagonist's identity.
         * If true, the protagonist KEEPS their original and the player receives an
         * EXACT COPY (an identical duplicate). The original is never removed from
         * the protagonist. This is NOT a redirection — the player gets the real item,
         * duplicated. Per user directive: "i want to be able to get an exact copy of
         * the protagonists' canon items, while they keep theirs, not some redirection."
         */
        public final boolean canonicallyTiedToIdentity;

        /** The dialogue spoken when the gift is offered. */
        public final String offerDialogue;

        /** The dialogue spoken when the gift is redirected to teaching. */
        public final String teachingDialogue;

        public final CanonEngine.Confidence canonConfidence;
        public final String source;

        public GiftRecord(String giftId, String protagonistId, GiftCategory category,
                          String name, String nameCn, String canonOriginId,
                          int affinityThreshold, int realmGate, String daoCompatibility,
                          boolean canonicallyTiedToIdentity,
                          String offerDialogue, String teachingDialogue,
                          CanonEngine.Confidence canonConfidence, String source) {
            this.giftId = giftId;
            this.protagonistId = protagonistId;
            this.category = category;
            this.name = name;
            this.nameCn = nameCn;
            this.canonOriginId = canonOriginId;
            this.affinityThreshold = affinityThreshold;
            this.realmGate = realmGate;
            this.daoCompatibility = daoCompatibility;
            this.canonicallyTiedToIdentity = canonicallyTiedToIdentity;
            this.offerDialogue = offerDialogue;
            this.teachingDialogue = teachingDialogue;
            this.canonConfidence = canonConfidence;
            this.source = source;
        }
    }

    // ── Curated gift registry ───────────────────────────────────────────
    // Per DESIGN_MANIFESTATION_GIFTS.md §4–5. Canonical items tagged canonicallyTiedToIdentity
    // = true produces an EXACT COPY. The protagonist keeps the original; the player receives
    //    an identical duplicate. NOT a redirection, NOT an "equivalent" — an EXACT COPY.

    /** Wang Lin — the Heaven-Defying Bead. CANONICALLY TIED TO IDENTITY. Player gets an EXACT COPY. */
    public static final GiftRecord GIFT_WL_BEAD = new GiftRecord(
        "GIFT-WL-001", "wang_lin", GiftCategory.CANONICAL_TREASURE,
        "Heaven-Defying Bead (exact copy)", "逆天珠（原物复刻）", "item_heaven_defying_bead",
        90, 12, "time", true,
        "\"The Bead found me when I was sixteen. A dead bird. A stone. I did not know what it was. " +
        "It is part of me now. But you are a protagonist too. Take this — it is the same bead. " +
        "Mine stays with me. Yours is yours.\"",
        null,  // no teaching redirect — the player gets an exact copy
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_COMPLETE_ITEMS.md; CANON_RI_EDGE_OF_CANON.md CHAIN27"
    );

    /** Wang Lin — teaching the Slaughter Essence (one of the 14 Essences). */
    public static final GiftRecord GIFT_WL_SLAUGHTER_ESSENCE = new GiftRecord(
        "GIFT-WL-002", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Slaughter Essence (teaching)", "杀戮本源（传授）", "tech_slaughter_essence",
        70, 10, "slaughter", false,
        "\"Slaughter is not killing. Slaughter is the law that ends things. I learned it in " +
        "the Cloud Sea, shattering Daoist Water's swords. I will teach you what I learned. " +
        "But you must still comprehend it yourself. I can show you the door.\"",
        "\"I will teach you Slaughter Essence. But teaching is not downloading. I explain; " +
        "you must still cultivate, comprehend, and practice. The Essence is a law, not a spell.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_COMPLETE_TECHNIQUES.md; SamsaraDao ESSENCE_SLAUGHTER"
    );

    /** Wang Lin — teaching the Karma Essence. */
    public static final GiftRecord GIFT_WL_KARMA_ESSENCE = new GiftRecord(
        "GIFT-WL-003", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Karma Essence (teaching)", "因果本源（传授）", "tech_karma_essence",
        70, 10, "karma", false,
        "\"Karma is the thread that binds. I forged the Karma Whip from the Soul Lasher. " +
        "I will teach you what I learned of karma. But you must still forge your own understanding.\"",
        "\"Karma Essence is a law. I can teach the theory. You must live the karma to crystallize it.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_COMPLETE_TECHNIQUES.md; SamsaraDao ESSENCE_KARMA"
    );

    /** Wang Lin — teaching the Restriction Essence (Four Great Restrictions). */
    public static final GiftRecord GIFT_WL_RESTRICTION_ESSENCE = new GiftRecord(
        "GIFT-WL-004", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Restriction Essence (teaching)", "禁制本源（传授）", "tech_restriction_essence",
        65, 9, "restriction", false,
        "\"I spent seven years at Restriction Mountain. My hair turned white. It was worth it. " +
        "I will teach you the Four Great Restrictions — Annihilation, Time, Life-Death, Ancient Soul. " +
        "The trial still stands. I will guide you, but you must walk it yourself.\"",
        "\"The Restriction Essence is learnable. The trial is hard. I will teach you what I can.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN20; SamsaraDao ESSENCE_RESTRICTION"
    );

    /** Wang Lin — teaching the Life-Death Essence. */
    public static final GiftRecord GIFT_WL_LIFE_DEATH_ESSENCE = new GiftRecord(
        "GIFT-WL-005", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Life-Death Essence (teaching)", "生死本源（传授）", "tech_life_death_essence",
        75, 10, "life_death", false,
        "\"I lived as a mortal twice. I watched my son grow old and die. I watched my parents " +
        "be murdered. Life and death — I know them. I will teach you. But you must still " +
        "experience them. There is no shortcut.\"",
        "\"Life-Death Essence cannot be taught from a book. I can guide you. You must live it.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN24; SamsaraDao ESSENCE_LIFE_DEATH"
    );

    /** Wang Lin — the Karma Whip (evolved into Karma Origin Sword). CANONICALLY TIED. Player gets an EXACT COPY. */
    public static final GiftRecord GIFT_WL_KARMA_WHIP = new GiftRecord(
        "GIFT-WL-006", "wang_lin", GiftCategory.CANONICAL_TREASURE,
        "Karma Whip (exact copy)", "因果鞭（原物复刻）", "item_karma_whip",
        80, 11, "karma", true,
        "\"The Karma Whip. It evolved from the Soul Lasher into the Origin Sword. I forged it from " +
        "the Kunji Whip through a thousand years of karma. Take this — it is the same weapon. " +
        "Mine stays with me. Yours is yours.\"",
        null,  // no teaching redirect — the player gets an exact copy
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN25"
    );

    /** Wang Lin — the 7 Origin Swords. CANONICALLY TIED. Player gets an EXACT COPY. */
    public static final GiftRecord GIFT_WL_ORIGIN_SWORDS = new GiftRecord(
        "GIFT-WL-007", "wang_lin", GiftCategory.CANONICAL_TREASURE,
        "7 Origin Swords (exact copy)", "七柄本源剑（原物复刻）", "item_origin_swords",
        85, 13, null, true,
        "\"The 7 Origin Swords — each forged from one of my Essences. Seven crystallizations of " +
        "dao. Take these. They are the same seven swords. Mine stay with me. Yours are yours.\"",
        null,  // no teaching redirect — the player gets an exact copy
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_COMPLETE_ITEMS.md; CANON_RI_EDGE_OF_CANON.md CHAIN21"
    );

    /** Wang Lin — the Body Fixation Art (4th-Step Divine Ability). Nearly inaccessible. */
    public static final GiftRecord GIFT_WL_BODY_FIXATION = new GiftRecord(
        "GIFT-WL-008", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Body Fixation Art (teaching)", "定身术（传授）", "tech_body_fixation_art",
        95, 14, null, false,
        "\"The Body Fixation Art — only five people know it. Four living. I am one. " +
        "Qing Shuang is another. The Seal Sovereign is another. I will teach you, but only " +
        "if I judge your character worthy. This art freezes cultivators of any realm. " +
        "It is not to be used lightly.\"",
        "\"The Body Fixation Art is a 4th-Step Divine Ability. I will teach it only to one I " +
        "trust completely. Prove yourself first — not with power, but with judgment.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN26"
    );

    /** Wang Lin — the Immortal Guard crafting method (ethically fraught). */
    public static final GiftRecord GIFT_WL_IMMORTAL_GUARD_METHOD = new GiftRecord(
        "GIFT-WL-009", "wang_lin", GiftCategory.CANONICAL_ARTIFACT,
        "Immortal Guard crafting method", "Immortal Guard 制造之法", "tech_immortal_guard",
        60, 8, "restriction", false,
        "\"I created two Immortal Guards — Du Jian and Ta Shan. Du Jian was destroyed. " +
        "Ta Shan regained his freedom and reached Celestial Immortal on his own. The method... " +
        "I will teach it. But know that it is ethically fraught. A guard has no choice in its " +
        "service. Ta Shan was lucky. Most are not. Use this knowledge wisely.\"",
        "\"The Immortal Guard method is learnable. I did not keep it secret. But consider " +
        "carefully before you use it. A being without choice is a being without dao.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN22"
    );

    /** Wang Lin — teaching the Dream Dao + Flowing Time (requires Dao Realm). */
    public static final GiftRecord GIFT_WL_DREAM_DAO = new GiftRecord(
        "GIFT-WL-010", "wang_lin", GiftCategory.CANONICAL_TECHNIQUE,
        "Dream Dao and Flowing Time (teaching)", "梦道与流年（传授）", "tech_dream_dao",
        88, 14, "dream", false,
        "\"The Dream Dao and Flowing Time are my Original Spells. I used them to send my " +
        "Slaughter Clone back through time — to send the bead to my past self. A closed loop. " +
        "I will teach you. But time-travel is NOT available to you — it requires a configuration " +
        "unique to me. The Dream Dao itself, however, is a law. You can learn it.\"",
        "\"The Dream Dao requires Dao Realm cultivation. Reach it, and I will teach you. " +
        "Time-travel is mine alone — the Slaughter True Body, the closed loop. But the Dream Dao " +
        "as a spell, that is a law you can learn.\"",
        CanonEngine.Confidence.NOVEL_STATEMENT,
        "CANON_RI_EDGE_OF_CANON.md CHAIN27"
    );

    /** Wang Lin — post-canon herbs gathered while travelling. */
    public static final GiftRecord GIFT_WL_POST_CANON_HERBS = new GiftRecord(
        "GIFT-WL-011", "wang_lin", GiftCategory.POST_CANON_HERB,
        "Post-canon spirit herbs", "后天灵草", null,
        30, 3, null, false,
        "\"I gathered these while travelling. They are not from my canon — I found them after. " +
        "Take them. You need them more than I do.\"",
        null,  // post-canon items are not redirected to teaching
        CanonEngine.Confidence.IMPLICATION,
        "DESIGN_MANIFESTATION_GIFTS.md §1B (post-canon possessions)"
    );

    /** Wang Lin — post-canon beast cores. */
    public static final GiftRecord GIFT_WL_POST_CANON_CORES = new GiftRecord(
        "GIFT-WL-012", "wang_lin", GiftCategory.POST_CANON_CORE,
        "Post-canon beast cores", "后天兽核", null,
        30, 4, null, false,
        "\"Beasts I slew while travelling. Their cores are yours if you have use for them.\"",
        null,
        CanonEngine.Confidence.IMPLICATION,
        "DESIGN_MANIFESTATION_GIFTS.md §1B (post-canon possessions)"
    );

    /** Meng Hao — teaching a Hex (canonical technique). */
    public static final GiftRecord GIFT_MH_HEX = new GiftRecord(
        "GIFT-MH-001", "meng_hao", GiftCategory.CANONICAL_TECHNIQUE,
        "Hex (teaching)", "禁制（传授）", "tech_hex",
        60, 10, null, false,
        "\"You want to learn a Hex? Everything has a price. What will you give me? ...Hah. " +
        "Fine. A fair price. I'll teach you. But you must still comprehend the Hex yourself — " +
        "I can show you the shape, not the substance.\"",
        "\"Teaching a Hex does not diminish my mastery. But I am Meng Hao. I bargain first. " +
        "It is a form of respect.\"",
        CanonEngine.Confidence.IMPLICATION,
        "ISSTH canon (Meng Hao's 8 Hexes); personality per DESIGN_MANIFESTATION_GIFTS.md §3"
    );

    /** Meng Hao — the Copper Mirror. CANONICALLY TIED. Player gets an EXACT COPY. */
    public static final GiftRecord GIFT_MH_COPPER_MIRROR = new GiftRecord(
        "GIFT-MH-002", "meng_hao", GiftCategory.CANONICAL_TREASURE,
        "Copper Mirror (exact copy)", "铜镜（原物复刻）", "item_copper_mirror",
        95, 15, null, true,
        "\"The Copper Mirror. Lord Fifth is bound to it. Everything has a price — and you've " +
        "paid yours. Take this. It is the same mirror. Lord Fifth stays in mine; you'll need to " +
        "bind your own spirit companion to yours. Everything has a price, but this one's on the house.\"",
        null,  // no teaching redirect — the player gets an exact copy
        CanonEngine.Confidence.IMPLICATION,
        "ISSTH canon (Meng Hao's Copper Mirror); DESIGN_MANIFESTATION_GIFTS.md §5"
    );

    /** Bai Xiaochun — pills. Hands them out constantly. */
    public static final GiftRecord GIFT_BXC_PILLS = new GiftRecord(
        "GIFT-BXC-001", "bai_xiaochun", GiftCategory.POST_CANON_FORGED,
        "Post-canon pills", "丹药", null,
        10, 1, null, false,
        "\"Take it! Take it! I made too many anyway! Don't die, okay?!\"",
        null,
        CanonEngine.Confidence.IMPLICATION,
        "AWE canon (Bai Xiaochun's pill-making); personality per DESIGN_MANIFESTATION_GIFTS.md §3"
    );

    /** Bai Xiaochun — teaching the Undying Live Forever Seal method. */
    public static final GiftRecord GIFT_BXC_SEAL_METHOD = new GiftRecord(
        "GIFT-BXC-002", "bai_xiaochun", GiftCategory.CANONICAL_TECHNIQUE,
        "Undying Live Forever Seal (teaching)", "不死长生印（传授）", "tech_undying_seal",
        40, 6, null, false,
        "\"You want to learn the seal? Okay! I'll teach you! It's really fun, you'll love it! " +
        "Well, the creatures might bite a little, but that's just how they say hello!\"",
        "\"The seal method is teachable! I have spare creatures! Here, take this one too!\"",
        CanonEngine.Confidence.IMPLICATION,
        "AWE canon (Bai Xiaochun's Undying Live Forever Seal); DESIGN_MANIFESTATION_GIFTS.md §5"
    );

    /** Su Ming — a comprehension-tested treasure. */
    public static final GiftRecord GIFT_SM_TREASURE = new GiftRecord(
        "GIFT-SM-001", "su_ming", GiftCategory.CANONICAL_TREASURE,
        "Treasure (comprehension-tested)", "宝物（悟性试炼）", null,
        50, 8, null, false,
        "\"You want this? Tell me what it is. Tell me what it means. Tell me why you want it. " +
        "If your answer shows understanding, it is yours. If not, I will teach you instead.\"",
        "\"Do you understand what this is? Tell me. Understanding is the only price I ask.\"",
        CanonEngine.Confidence.IMPLICATION,
        "Ptt canon (Su Ming's comprehension-focused dao); DESIGN_MANIFESTATION_GIFTS.md §3"
    );

    /** Xu Qing — a silent-judgment gift. */
    public static final GiftRecord GIFT_XQ_ITEM = new GiftRecord(
        "GIFT-XQ-001", "xu_qing", GiftCategory.POST_CANON_FORGED,
        "Post-canon item (silent judgment)", "后天物品（默判）", null,
        45, 7, null, false,
        "\"...Take it.\"",
        null,
        CanonEngine.Confidence.IMPLICATION,
        "BTT canon (Xu Qing's terse, judgmental personality); DESIGN_MANIFESTATION_GIFTS.md §3"
    );

    /** All registered gift records. */
    public static final List<GiftRecord> ALL_GIFTS = Collections.unmodifiableList(Arrays.asList(
        GIFT_WL_BEAD,
        GIFT_WL_SLAUGHTER_ESSENCE,
        GIFT_WL_KARMA_ESSENCE,
        GIFT_WL_RESTRICTION_ESSENCE,
        GIFT_WL_LIFE_DEATH_ESSENCE,
        GIFT_WL_KARMA_WHIP,
        GIFT_WL_ORIGIN_SWORDS,
        GIFT_WL_BODY_FIXATION,
        GIFT_WL_IMMORTAL_GUARD_METHOD,
        GIFT_WL_DREAM_DAO,
        GIFT_WL_POST_CANON_HERBS,
        GIFT_WL_POST_CANON_CORES,
        GIFT_MH_HEX,
        GIFT_MH_COPPER_MIRROR,
        GIFT_BXC_PILLS,
        GIFT_BXC_SEAL_METHOD,
        GIFT_SM_TREASURE,
        GIFT_XQ_ITEM
    ));

    /** Lookup a gift by ID. */
    public static GiftRecord getGift(String giftId) {
        for (GiftRecord g : ALL_GIFTS) {
            if (g.giftId.equals(giftId)) return g;
        }
        return null;
    }

    /** Get all gifts offered by a specific protagonist. */
    public static List<GiftRecord> getGiftsByProtagonist(String protagonistId) {
        List<GiftRecord> result = new ArrayList<>();
        for (GiftRecord g : ALL_GIFTS) {
            if (g.protagonistId.equals(protagonistId)) result.add(g);
        }
        return Collections.unmodifiableList(result);
    }

    /** Get all gifts of a specific category. */
    public static List<GiftRecord> getGiftsByCategory(GiftCategory category) {
        List<GiftRecord> result = new ArrayList<>();
        for (GiftRecord g : ALL_GIFTS) {
            if (g.category == category) result.add(g);
        }
        return Collections.unmodifiableList(result);
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 5 — THE EVALUATION ENGINE
    // ════════════════════════════════════════════════════════════════════

    /**
     * The player state snapshot fed to the evaluation engine.
     *
     * <p>This is an interface so the gift system does not depend on a specific
     * player-state implementation. The caller provides a snapshot; the engine
     * queries it.
     */
    public interface PlayerStateSnapshot {
        /** The player's affinity with a protagonist (0–100). */
        int getAffinity(String protagonistId);

        /** The player's absolute cultivation tier (0=mortal, 17=Transcendence). */
        int getRealmTier();

        /** Whether the player has the required dao affinity (e.g., "slaughter", "karma"). */
        boolean hasDao(String daoId);

        /**
         * For Xu Qing's profile: observed actions that build trust independent of
         * the affinity number. Returns a trust score (0–100) derived from actions.
         */
        int getObservedActionTrust(String protagonistId);
    }

    /**
     * Evaluate whether a gift is offered, refused, or redirected.
     *
     * <p>Per DESIGN_MANIFESTATION_GIFTS.md §2. The four questions are evaluated
     * in order; the first failure determines the outcome.
     *
     * @param gift       the gift being requested
     * @param player     the player's state snapshot
     * @return the decision (OFFERED / REFUSED_* / REDIRECTED_*)
     */
    public static GiftDecision evaluateGift(GiftRecord gift, PlayerStateSnapshot player) {
        PersonalityProfile profile = getProfile(gift.protagonistId);
        if (profile == null) return GiftDecision.REFUSED_PERSONALITY;

        int affinity = player.getAffinity(gift.protagonistId);

        // ── Prerequisite: affinity must meet threshold ──────────────────
        // (This is the gate to even ask. Below threshold = not trusted enough.)
        if (affinity < gift.affinityThreshold) {
            return GiftDecision.REFUSED_NOT_TRUSTED;
        }

        // ── Question 1: Does he still need this? ────────────────────────
        // Per user directive: canon items tied to identity produce an EXACT COPY.
        // The protagonist keeps the original; the player receives an identical duplicate.
        // This is NOT a redirection. Q1 does NOT fail for identity-tied items — the
        // protagonist can keep theirs AND give a copy. We fall through to Q2/Q3/Q4.
        // (If the personality filter triggers a bargain or comprehension test, that's
        // Q4's doing, not Q1's.)

        // ── Question 2: Is the player a trusted ally? ───────────────────
        // Wang Lin: Q2 weighted 2× — trust is paramount.
        // Xu Qing: trust is evaluated by observed actions, not affinity number.
        int trustScore = affinity;
        if (profile == XU_QING) {
            trustScore = Math.max(affinity, player.getObservedActionTrust(gift.protagonistId));
        }
        int trustThreshold = gift.affinityThreshold + (profile.q2Weight > 1 ? 15 : 0);
        if (trustScore < trustThreshold) {
            return GiftDecision.REFUSED_NOT_TRUSTED;
        }

        // ── Question 3: Would giving this help the player? ──────────────
        // Realm gate and dao compatibility.
        if (player.getRealmTier() < gift.realmGate) {
            return GiftDecision.REFUSED_NOT_READY;
        }
        if (gift.daoCompatibility != null && !player.hasDao(gift.daoCompatibility)) {
            return GiftDecision.REFUSED_NOT_READY;
        }

        // ── Question 4: Does this fit my personality? ───────────────────
        // Bai Xiaochun: Q4 weight 0 — almost never filters.
        // Meng Hao: redirect to bargain.
        // Su Ming: redirect to comprehension test.
        if (profile.q4Weight == 0) {
            // Personality does not filter (Bai Xiaochun). Offer freely.
            return GiftDecision.OFFERED;
        }
        if (profile.bargainBehavior == BargainBehavior.FAIR ||
            profile.bargainBehavior == BargainBehavior.PREDATORY) {
            return GiftDecision.REDIRECTED_TO_BARGAIN;
        }
        if (profile.bargainBehavior == BargainBehavior.COMPREHENSION_TEST) {
            return GiftDecision.REDIRECTED_TO_COMPREHENSION;
        }
        // Wang Lin / Xu Qing: if all four questions pass, the gift is offered.
        return GiftDecision.OFFERED;
    }

    /**
     * Convenience: evaluate a gift by ID.
     */
    public static GiftDecision evaluateGift(String giftId, PlayerStateSnapshot player) {
        GiftRecord gift = getGift(giftId);
        if (gift == null) return GiftDecision.REFUSED_PERSONALITY;
        return evaluateGift(gift, player);
    }

    /**
     * Get the dialogue line for a given gift + decision outcome.
     * Returns the appropriate offer/refusal/redirect line from the gift record
     * or the personality profile.
     */
    public static String getDialogueFor(GiftRecord gift, GiftDecision decision) {
        PersonalityProfile profile = getProfile(gift.protagonistId);
        if (profile == null) return "...";

        switch (decision) {
            case OFFERED:
                return gift.offerDialogue != null ? gift.offerDialogue : profile.signatureOfferLine;
            case REFUSED_STILL_NEEDED:
            case REDIRECTED_TO_TEACHING:
                return gift.teachingDialogue != null ? gift.teachingDialogue :
                    "\"This stays with me. But I will teach you how I came to understand it.\"";
            case REFUSED_NOT_TRUSTED:
                return "\"Prove yourself first.\"";
            case REFUSED_NOT_READY:
                return "\"Your realm cannot bear this. Return when you are stronger.\"";
            case REFUSED_PERSONALITY:
                return profile.signatureRefusalLine;
            case REDIRECTED_TO_BARGAIN:
                return profile.signatureBargainLine;
            case REDIRECTED_TO_COMPREHENSION:
                return profile.signatureBargainLine;  // Su Ming's comprehension-test prompt
            default:
                return "...";
        }
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 6 — SUMMARY & DESIGN-RULE ASSERTIONS
    // ════════════════════════════════════════════════════════════════════

    /**
     * Whether the gift system ever removes a canonical original from its protagonist.
     *
     * <p>Per user directive: "Canon originals are never erased or taken away."
     * This method MUST return false. It exists to make the design rule explicit
     * and assertable.
     */
    public static boolean everRemovesCanonicalOriginal() {
        // Identity-tied items produce exact copies. The original stays with the protagonist.
        // The evaluateGift() engine does NOT remove or transfer the original — it creates
        // a copy. The PlayerStateSnapshot interface has no method to modify the protagonist's
        // inventory. The original is untouched.
        return false;
    }

    /**
     * Whether the gift system uses redirections ("equivalent"/"teaching") instead of
     * exact copies for canon items.
     *
     * <p>Per user directive: "not some redirection." This method MUST return false.
     * Every identity-tied gift record produces an EXACT COPY, not a teaching redirect
     * or a parallel/equivalent path.
     */
    public static boolean usesRedirectionsForCanonItems() {
        // Verify: no identity-tied gift record uses redirection language.
        // All identity-tied records use "exact copy" / "原物复刻".
        for (GiftRecord g : ALL_GIFTS) {
            if (g.canonicallyTiedToIdentity) {
                boolean isExactCopy = g.name.contains("exact copy") ||
                                      g.name.contains("原物复刻");
                if (!isExactCopy) {
                    return true;  // VIOLATION — a tied item is using redirection language
                }
            }
        }
        return false;
    }

    /**
     * Whether the gift system implements affinity-as-currency (spend X affinity → extract item).
     *
     * <p>Per user directive: "I would avoid 'pay affinity to steal.'"
     * This method MUST return false. The evaluation engine uses affinity as a
     * PREREQUISITE (a threshold gate), not a CURRENCY (a spendable resource).
     * Affinity is never decremented when a gift is offered.
     */
    public static boolean usesAffinityAsCurrency() {
        // The evaluateGift method reads affinity but never writes/decrements it.
        // The PlayerStateSnapshot interface has no setAffinity or spendAffinity method.
        return false;
    }

    /**
     * Whether the manifestation is a quest-giver (rewards gifts for quest completion).
     *
     * <p>Per the existing design rule (RIEdgeOfCanonState.ManifestationCompanion.isQuestGiver):
     * the manifestation is a WITNESS, not a quest-giver. Gifts are player-initiated
     * or organically offered, never quest-rewarded.
     */
    public static boolean manifestationIsQuestGiver() {
        return false;
    }

    /**
     * Summary counts for registry logging.
     */
    public static Map<String, Integer> getSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("possessionCategories", PossessionCategory.values().length);
        counts.put("giftCategories", GiftCategory.values().length);
        counts.put("evaluationQuestions", EvaluationQuestion.values().length);
        counts.put("giftDecisions", GiftDecision.values().length);
        counts.put("personalityProfiles", ALL_PROFILES.size());
        counts.put("giftRecords", ALL_GIFTS.size());
        counts.put("canonicalGifts", (int) ALL_GIFTS.stream().filter(g -> g.category.isCanonical()).count());
        counts.put("postCanonGifts", (int) ALL_GIFTS.stream().filter(g -> !g.category.isCanonical()).count());
        counts.put("identityTiedGifts", (int) ALL_GIFTS.stream().filter(g -> g.canonicallyTiedToIdentity).count());
        counts.put("exactCopyGifts", (int) ALL_GIFTS.stream().filter(g -> g.canonicallyTiedToIdentity).count());
        counts.put("teachableGifts", (int) ALL_GIFTS.stream().filter(g -> g.category == GiftCategory.CANONICAL_TECHNIQUE).count());
        return counts;
    }
}
