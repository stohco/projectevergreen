package dev.ergenverse.simulation.affinity;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Bridging Policy — what canon leaves silent vs. what can be intelligently generated.
 *
 * <p><b>User directive:</b> "make sure you have all the information and details needed
 * before you build the wang lin cosmology and its systems. note that after thats all
 * good and concreted into the game, of course there could be intelligently generated
 * things to bridge in gaps that werent spoken about in the novels, but definitely do exist."
 *
 * <p>This class is the policy gatekeeper. Every system in the Wang Lin cosmology
 * is classified as one of 5 tiers. Registry contains 113 entries across 19 sections
 * covering cosmology, factions, techniques, items, ecology, civilization, world
 * mechanics, and forbidden content.
 *
 * <ol>
 *   <li><b>CANON_CONCRETE</b> (Type A, confidence 5) — Explicitly attested in canon
 *       with chapter citation. Cannot be modified by intelligent generation.
 *       Implementation must match canon exactly.</li>
 *   <li><b>CANON_IMPLIED</b> (Type B, confidence 4) — Strongly implied by canon but
 *       not explicitly detailed. Intelligent generation MAY fill in specifics, but
 *       must not contradict canon.</li>
 *   <li><b>REASONABLE_RECONSTRUCTION</b> (Type C, confidence 3) — Canon is silent
 *       on this specific point, but the existence of the thing is logically necessary
 *       (e.g. "the All-Seer had disciples" — canon doesn't name them all, but they
 *       must exist). Intelligent generation MAY create these, but they must be
 *       (a) consistent with canon, (b) flagged as generated, (c) overridable if
 *       canon later specifies.</li>
 *   <li><b>SPECULATION</b> (Type D, confidence 1-2) — The most constrained generation
 *       tier. Canon is silent, and the thing is NOT logically necessary, but it may
 *       be needed to fill a gap. Divided into two sub-tiers:
 *       <ul>
 *         <li><b>DERIVED_SPECULATION</b> (D2, conf 2) — Weakly derived from established
 *             canon. There is at least one canon fact that makes this a reasonable
 *             possibility, but it is not logically necessary. Generation must be
 *             derived from geography, cultivation systems, historical events, ecology,
 *             economy, or social consequences already established by canon.</li>
 *         <li><b>BARE_CONJECTURE</b> (D1, conf 1) — Barely constrained. Only when
 *             absolutely necessary and no higher-tier source can cover the gap.
 *             Must still be consistent with the Prime Directive but carries the
 *             highest risk of future canon contradiction.</li>
 *       </ul>
 *   <li><b>FORBIDDEN</b> — Canon explicitly does NOT have this, or the 30 user
 *       corrections forbid it. Intelligent generation MUST NOT create these.</li>
 * </ol>
 *
 * <p><b>The golden rule (per the Prime Directive):</b>
 * Canon-concrete systems are OBJECTIVE. All generated systems (B through D) are ALSO
 * OBJECTIVE — they exist in the world whether the player knows they're generated or not.
 * The "generated" flag is for the developer, not for the player. From the player's
 * perspective, a generated "fourth disciple of the All-Seer" is just as real as
 * a canon-named "Blood Ancestor Yao Xinghai".
 *
 * <p><b>The override rule:</b> if a future canon source names something we generated,
 * the canon version wins. The generated version is replaced. This is non-destructive
 * (generated content has no karma with the player; the player's interactions
 * transfer to the canon version).
 *
 * <p><b>The Prime Directive (non-negotiable):</b>
 * "Never fill empty space with generic fantasy content. Empty space in the novels
 * represents unknown territory, not permission to invent randomly. Any generated
 * content must be derived from geography, cultivation systems, historical events,
 * ecology, economy, or social consequences already established by canon."
 *
 * <p><b>Speculation is the last resort.</b> The generation priority order is:
 * CANON_IMPLIED > REASONABLE_RECONSTRUCTION > SPECULATION_D2 > SPECULATION_D1.
 * If a gap can be filled at a higher tier, the SPECULATION tier MUST NOT be used.
 */
public final class BridgingPolicy {

    private BridgingPolicy() {}

    // ─── Classification (5 tiers) ────────────────────────────────────────

    /**
     * The 5-tier classification for canon-vs-generated content.
     * Aligned with the user's Type A-D model plus FORBIDDEN.
     */
    public enum Classification {
        /** Type A, confidence 5. Explicitly attested in canon. Immutable. */
        CANON_CONCRETE("Type A, conf 5. Explicitly attested in canon with chapter citation. Cannot be modified."),
        /** Type B, confidence 4. Strongly implied; generation fills specifics. */
        CANON_IMPLIED("Type B, conf 4. Strongly implied by canon but not detailed. Generation may fill specifics."),
        /** Type C, confidence 3. Logically necessary; generation may create. */
        REASONABLE_RECONSTRUCTION("Type C, conf 3. Canon is silent; existence is logically necessary. Generation may create."),
        /** Type D, confidence 1-2. Only when needed; never overwrites canon. */
        SPECULATION("Type D, conf 1-2. Only when needed; never overwrites canon. Must be derived from established canon."),
        /** Forbidden. Canon or user corrections explicitly exclude this. */
        FORBIDDEN("Canon explicitly does not have this, or user corrections forbid it.");

        public final String description;

        Classification(String description) {
            this.description = description;
        }
    }

    // ─── Speculation Sub-Tier (distinct from all other tiers) ────────────

    /**
     * The two sub-tiers of SPECULATION (Type D).
     *
     * <p>The distinction matters for the Forge mod's content-generation priority queue:
     * DERIVED_SPECULATION (D2) is preferred over BARE_CONJECTURE (D1) because D2 has
     * at least one canon-derived anchor point, while D1 is almost entirely unconstrained.
     *
     * <p><b>Key difference:</b> D2 has a {@code speculationAnchor} — a specific canon fact
     * or causal chain that makes the speculation reasonable. D1 has no such anchor;
     * it exists only because a gap MUST be filled and no other tier can cover it.
     */
    public enum SpeculationSubTier {
        /**
         * D2, confidence 2. Weakly derived from established canon.
         * At least one canon fact makes this a reasonable possibility.
         * Examples: "Tu Si was an Ancient God who left the Ancient God Tactic" —
         * we know he left a legacy (canon), so speculating about his life is D2.
         *
         * <p>Generation rules:
         * <ul>
         *   <li>MUST cite the canon anchor point</li>
         *   <li>MUST be consistent with the anchor's implications</li>
         *   <li>MUST NOT contradict any other canon fact</li>
         *   <li>Flagged as "generated — derived speculation"</li>
         * </ul>
         */
        DERIVED_SPECULATION("D2", 2,
            "Weakly derived from canon. At least one canon fact makes this reasonable."),

        /**
         * D1, confidence 1. Barely constrained. Last resort only.
         * No direct canon anchor exists. Used only when a gap MUST be filled
         * and no higher-tier source (A/B/C/D2) can cover it.
         * Examples: "The specific names of the other 5-6 unnamed IAC Suns" —
         * we know 9 suns exist (canon) and ~3-4 are named (canon), but the
         * remaining names have zero canon basis. This is D1.
         *
         * <p>Generation rules:
         * <ul>
         *   <li>MUST be consistent with the Prime Directive (no generic fantasy)</li>
         *   <li>MUST respect the genre conventions established by canon</li>
         *   <li>MUST NOT contradict any canon fact</li>
         *   <li>Flagged as "generated — bare conjecture; high contradiction risk"</li>
         *   <li>Automatically flagged for review if canon source is later added</li>
         * </ul>
         */
        BARE_CONJECTURE("D1", 1,
            "Barely constrained. Last resort only. No direct canon anchor. High contradiction risk.");

        /** Short code for logging (D1, D2). */
        public final String code;
        /** Confidence level (1 or 2). */
        public final int confidence;
        /** Human-readable description. */
        public final String description;

        SpeculationSubTier(String code, int confidence, String description) {
            this.code = code;
            this.confidence = confidence;
            this.description = description;
        }
    }

    // ─── Policy Entry ────────────────────────────────────────────────────

    /**
     * A policy entry — one per system or sub-system.
     *
     * <p>For SPECULATION-classified entries, the {@code speculationSubTier} and
     * {@code speculationAnchor} fields provide the additional depth required
     * to distinguish D1 from D2 and to enforce the Prime Directive at generation time.
     */
    public static final class PolicyEntry {
        public final String systemId;
        public final String systemName;
        public final Classification classification;
        public final int confidenceLevel;     // 1-5 (matches Type A-D + FORBIDDEN)
        public final String canonBasis;        // for CANON_CONCRETE / CANON_IMPLIED: the canon citation
        public final String bridgingRationale; // for REASONABLE_RECONSTRUCTION / SPECULATION: why this must exist
        public final String generationConstraints;  // for REASONABLE_RECONSTRUCTION / SPECULATION: what generation must respect

        /** SPECULATION only: which sub-tier (D1 or D2). Null for non-SPECULATION entries. */
        public final SpeculationSubTier speculationSubTier;
        /** SPECULATION only: the specific canon fact that anchors this speculation. Null for D1 or non-SPECULATION. */
        public final String speculationAnchor;
        /** SPECULATION only: why even this weak derivation is justified. Null for non-SPECULATION. */
        public final String speculationRationale;
        /** SPECULATION only: what would need to happen for this to be promoted to a higher tier. */
        public final String promotionPath;

        /**
         * Constructor for CANON_CONCRETE, CANON_IMPLIED, REASONABLE_RECONSTRUCTION, FORBIDDEN.
         */
        public PolicyEntry(String systemId, String systemName, Classification classification,
                          String canonBasis, String bridgingRationale, String generationConstraints) {
            this(systemId, systemName, classification, canonBasis, bridgingRationale,
                 generationConstraints, null, null, null, null);
        }

        /**
         * Full constructor including SPECULATION fields.
         */
        public PolicyEntry(String systemId, String systemName, Classification classification,
                          String canonBasis, String bridgingRationale, String generationConstraints,
                          SpeculationSubTier speculationSubTier, String speculationAnchor,
                          String speculationRationale, String promotionPath) {
            this.systemId = systemId;
            this.systemName = systemName;
            this.classification = classification;
            this.speculationSubTier = speculationSubTier;
            this.speculationAnchor = speculationAnchor;
            this.speculationRationale = speculationRationale;
            this.promotionPath = promotionPath;
            this.canonBasis = canonBasis;
            this.bridgingRationale = bridgingRationale;
            this.generationConstraints = generationConstraints;

            // Confidence: for SPECULATION, use the sub-tier's confidence; otherwise use the classification default.
            if (classification == Classification.SPECULATION && speculationSubTier != null) {
                this.confidenceLevel = speculationSubTier.confidence;
            } else {
                this.confidenceLevel = confidenceFor(classification);
            }
        }

        private static int confidenceFor(Classification c) {
            switch (c) {
                case CANON_CONCRETE: return 5;
                case CANON_IMPLIED: return 4;
                case REASONABLE_RECONSTRUCTION: return 3;
                case SPECULATION: return 2;  // Default if no sub-tier specified; prefer explicit sub-tier
                case FORBIDDEN: return 0;
                default: return 0;
            }
        }

        /** Whether this entry is a SPECULATION entry. */
        public boolean isSpeculation() {
            return classification == Classification.SPECULATION;
        }

        /** Whether this entry is a high-risk speculation (D1 — bare conjecture). */
        public boolean isHighRisk() {
            return classification == Classification.SPECULATION
                && speculationSubTier == SpeculationSubTier.BARE_CONJECTURE;
        }

        /** Whether this entry is a forbidden entry. */
        public boolean isForbidden() {
            return classification == Classification.FORBIDDEN;
        }

        /** Whether this entry is canon-concrete (immutable). */
        public boolean isCanonConcrete() {
            return classification == Classification.CANON_CONCRETE;
        }

        /** Generation priority for the Forge mod's content queue (lower = generate first). */
        public int generationPriority() {
            switch (classification) {
                case CANON_IMPLIED: return 1;            // Fill implied gaps first
                case REASONABLE_RECONSTRUCTION: return 2; // Then logically-necessary gaps
                case SPECULATION:
                    if (speculationSubTier == SpeculationSubTier.DERIVED_SPECULATION) return 3;
                    if (speculationSubTier == SpeculationSubTier.BARE_CONJECTURE) return 4;
                    return 5; // Unspecified sub-tier — treat as lowest priority
                case CANON_CONCRETE: return 99;          // No generation needed
                case FORBIDDEN: return 100;              // Never generate
                default: return 50;
            }
        }

        /** Short classification tag for logging. */
        public String tag() {
            switch (classification) {
                case CANON_CONCRETE: return "A/5";
                case CANON_IMPLIED: return "B/4";
                case REASONABLE_RECONSTRUCTION: return "C/3";
                case SPECULATION:
                    if (speculationSubTier != null) return "D/" + speculationSubTier.confidence;
                    return "D/?";
                case FORBIDDEN: return "F";
                default: return "?";
            }
        }
    }

    // ─── The full policy registry for Wang Lin's cosmology ────────────────

    public static final List<PolicyEntry> POLICY = Collections.unmodifiableList(Arrays.asList(

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 1: COSMOLOGY LAYERS — all CANON_CONCRETE (Type A/5)
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("layer_root_dao", "The Root Dao", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L01; C5 confidence",
            null, null),
        new PolicyEntry("layer_luo_tian", "Luo Tian Star System", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L02; C4 confidence",
            null, null),
        new PolicyEntry("layer_immortal_astral_continent", "Immortal Astral Continent", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L03; C5 confidence",
            null, null),
        new PolicyEntry("layer_cave_world", "The Cave World", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L04; C5 confidence; explicit ownership by Seven-Colored Daoist",
            null, null),
        new PolicyEntry("layer_sealed_realm", "Sealed Realm", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L05; C5 confidence; explicit Realm-Sealing Grand Array",
            null, null),
        new PolicyEntry("layer_outer_realm", "Outer Realm", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L06; C5 confidence",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 2: IAC SUBDIVISIONS — mixed A/B/C
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("iac_continents", "IAC Continents (Heavenly Bull, Green Devil, Mountain Sea, Great Saint)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L03; C5 confidence; all 4 named in canon",
            null, null),
        new PolicyEntry("iac_imperial_city", "Dao Ancient Imperial Capital", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L03; C5 confidence",
            null, null),
        new PolicyEntry("iac_sub_regions", "IAC Sub-Regions (cities, mountains, valleys within each continent)", Classification.REASONABLE_RECONSTRUCTION,
            null,
            "A continent 'so vast it has nine suns' must contain thousands of sub-regions. Canon names only a few (Great Soul Sect's mountain, Pill Sea, etc.). The rest must be generated.",
            "Generation must respect: (a) the continent's character (Heavenly Bull = Wang Lin's base; Green Devil = sealed-spirit-beneath; Mountain Sea = Meng Hao resonance; Great Saint = canonical but sparse); (b) the world-law tier (absolute); (c) no conflict with canon-named locations."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 3: THE 9 SUNS — CANON_CONCRETE for named; SPECULATION for unnamed
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("iac_suns_named", "Named IAC Suns (Lian Daozhen, Gu Dao, Ancient Dao Great Heavenly Venerable)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L03 + Wang Lin NPC entry; C5 confidence",
            null, null),
        new PolicyEntry("iac_suns_unnamed", "The 6 unnamed IAC Suns (Grand Empyreans)", Classification.REASONABLE_RECONSTRUCTION,
            null,
            "Canon establishes 9 suns but names only ~3-4. The other ~5-6 must be generated to fill the cosmology.",
            "Generation must respect: (a) each Sun is a Grand Empyrean (Paragon-tier); (b) each Sun rules a continental division or major sect; (c) Wang Lin becomes the 10th Sun — generation must leave narrative space for this; (d) no Sun can be stronger than Gu Dao (canon: Gu Dao is strongest by sun-size, Wang Lin is 2nd after slaying him)."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 4: CAVE WORLD OWNERSHIP — CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("cave_world_ownership", "Cave World owned by Seven-Colored Daoist", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L04 + key antagonist finding; C5 confidence",
            null, null),
        new PolicyEntry("joss_flame_economy", "Joss Flame harvest loop (mortal faith -> owner harvest)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L04 + CANON_RI_COMPLETE_ITEMS.md; C5 confidence",
            null, null),
        new PolicyEntry("joss_flame_specifics", "Specific Joss Flame generation rates per village", Classification.REASONABLE_RECONSTRUCTION,
            null,
            "Canon establishes the loop but not the specific rates. Generation must produce consistent rates.",
            "Generation must respect: (a) more mortals = more flames; (b) more pious mortals = more flames; (c) the owner's harvest percentage is canon (~30% via the seal); (d) collectors must be Soul Formation+ to perceive flames."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 5: HEAVEN-DEFYING BEAD — mixed A/C/D
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("bead_core_mechanics", "Heaven-Defying Bead core (interior chamber, 10x time dilation, Five Elements pattern, sentience)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md item #1; C5 confidence",
            null, null),
        new PolicyEntry("bead_li_muwan_storage", "Li Muwan's Nascent Soul stored in bead", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md item #1; C5 confidence",
            null, null),
        new PolicyEntry("bead_hidden_fragments", "The 3 hidden fragments of the bead (parts 7-9) — existence", Classification.REASONABLE_RECONSTRUCTION,
            "CANON_RI_COMPLETE_ITEMS.md item #1 mentions '9 parts' but only details 6",
            "Canon establishes 9 parts but only details 6 (core + 5 elements). The remaining 3 must be generated to complete the bead's progression.",
            "Generation must respect: (a) the bead's heaven-defying nature; (b) cross-novel resonance (Su Ming, Xuan Zang); (c) the 3 hidden fragments should relate to the 3 highest-tier Essences (Reincarnation, Restriction, Slaughter) or the 3 Heaven Trampling bridge-related mysteries; (d) fragments must be progressive (7 < 8 < 9 in power)."),
        new PolicyEntry("bead_hidden_fragment_natures", "The specific NATURE of each of the bead's 3 hidden fragments",
            Classification.SPECULATION,
            null,
            "Canon establishes 9 parts but is silent on what fragments 7-9 specifically ARE. REASONABLE_RECONSTRUCTION covers their existence; SPECULATION covers their specific nature.",
            "Generation must respect: (a) derived from the bead's heaven-defying nature; (b) cross-novel resonance; (c) relate to Reincarnation/Restriction/Slaughter essences OR Heaven Trampling bridge mysteries; (d) fragments must be progressive (7 < 8 < 9). MUST NOT invent wholesale.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "The bead has 9 parts (canon: item #1) and the 5 element fragments are explicitly mapped. The 3 remaining fragments must relate to the bead's higher functions (time/space/soul manipulation).",
            "The bead's 5 element fragments are known (canon). The 3 remaining slots must serve the bead's ultimate function (heaven-defying). The Essences Wang Lin masters after the elements are Reincarnation, Restriction, and Slaughter — these are the most likely anchors.",
            "Promoted to CANON_IMPLIED if any canon source describes even one hidden fragment's function."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 6: THE 14 ESSENCENCES + 9 BRIDGES — CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("essences_14", "The 14 Essences (taxonomy + Wang Lin's completion order)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md essence section; C5 confidence; Baidu cross-reference",
            null, null),
        new PolicyEntry("essences_resonance_web", "Cross-essence resonance web", Classification.CANON_IMPLIED,
            "CANON_RI_COMPLETE_TECHNIQUES.md mentions Heart-Pounding Thunder (fire+thunder fusion) and Five Elements True Body fusion",
            "Canon explicitly shows some essences fuse/resonate. The full resonance web is implied but not enumerated.",
            "Generation must respect: (a) all 5 Elements resonate with each other; (b) Primordial-Silent Extinction are a pair; (c) Life-Death-Reincarnation are a cycle; (d) Karma-True-False are conceptually adjacent; (e) Restriction-Slaughter are the two 'action' essences."),
        new PolicyEntry("bridges_9", "The 9 Heaven Trampling Bridges (tests + Wang Lin's results)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md bridge section; C5 confidence",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 7: ANTAGONISTS — CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("antagonist_all_seer", "The All-Seer (mortal-realm schemer)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md key antagonist finding + Heavenly Fate Sect entry; C5 confidence",
            null, null),
        new PolicyEntry("antagonist_seven_colored_daoist", "The Seven-Colored Daoist (cosmic owner)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md key antagonist finding + L04; C5 confidence",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 8: ANTAGONIST ORGANIZATIONAL DETAILS — B/C
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("all_seer_seven_disciples", "The All-Seer's seven color-division disciples", Classification.CANON_IMPLIED,
            "CANON_RI_COMPLETE_WORLD.md Heavenly Fate Sect entry mentions 7 color divisions",
            "Canon establishes 7 color divisions (red/orange/yellow/green/blue/cyan/purple) but doesn't name all 7 disciples.",
            "Generation must respect: (a) each disciple leads one color division; (b) each disciple's cultivation is below All-Seer's; (c) Wang Lin must defeat or befriend several of them in canon arcs; (d) Blood Ancestor Yao Xinghai is one of them (canon-confirmed)."),
        new PolicyEntry("seven_colored_daoist_servants", "The Seven-Colored Daoist's servants/guardians", Classification.REASONABLE_RECONSTRUCTION,
            null,
            "A Cave World owner of Paragon tier must have servants/guardians. Canon doesn't detail them.",
            "Generation must respect: (a) servants are below the Daoist's tier; (b) servants manage the Joss Flame harvest on his behalf; (c) Wang Lin must defeat several to reach the Daoist; (d) at least one servant should have a karmic thread to Wang Lin (the Seven-Colored Realm's Master Ashen Pine is canon-confirmed as one such)."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 9: CROSS-NOVEL RESONANCE — CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("cross_novel_wang_lin_awwp", "Wang Lin as 'Paragon Wang' in AWWP (mentors Wang Baole)", Classification.CANON_CONCRETE,
            "SYSTEMS_AUDIT_COMPLETE.md correction #23 + AWWP ch60s, ch69 cross-reference; C5 confidence",
            null, null),
        new PolicyEntry("cross_novel_bald_crane_to_iac", "Su Ming sends Bald Crane to Wang Lin's IAC", Classification.CANON_CONCRETE,
            "SYSTEMS_AUDIT_COMPLETE.md correction #16; C4 confidence",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 10: FORBIDDEN — per user corrections
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("forbidden_allheaven_universal", "Allheaven as universal antagonist (NOT Wang Lin's)", Classification.FORBIDDEN,
            "User correction #8 + #19: Allheaven is ISSTH-specific. Wang Lin's antagonists are All-Seer + Seven-Colored Daoist.",
            null,
            "Generation must NEVER use Allheaven as Wang Lin's antagonist. Allheaven belongs to ISSTH / Meng Hao's cosmology only."),
        new PolicyEntry("forbidden_wang_lin_wang_baole_reincarnation", "Wang Baole as Wang Lin's reincarnation", Classification.FORBIDDEN,
            "User correction #23: They COEXIST. Wang Lin appears in AWWP as 'Paragon Wang' (separate living character) and mentors Wang Baole.",
            null,
            "Generation must NEVER treat Wang Baole as Wang Lin's reincarnation. They are separate characters who coexist."),
        new PolicyEntry("forbidden_9_9_1_tribulation", "9-9-1 tribulation as canon", Classification.FORBIDDEN,
            "User correction #9: 9-9-1 is fan-synthesis. Use ordinal multipliers (1st, 2nd, 3rd... tribulation), not numeric.",
            null,
            "Generation must NEVER use '9-9-1' as a tribulation structure. Use ordinal-named tribulations (e.g. 'Wang Lin's Third Ancient God Tribulation')."),
        new PolicyEntry("forbidden_bald_crane_illusion_bypass", "Bald Crane's illusions bypassing divine sense", Classification.FORBIDDEN,
            "User correction #13: Bald Crane's illusions do NOT bypass divine sense. Divine sense pierces them.",
            null,
            "Generation must NEVER let Bald Crane's illusions bypass divine sense. Divine sense is the canonical counter."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 11: TIMELINE — A/C
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("timeline_events", "RI timeline events (39 events across 11 eras)", Classification.CANON_CONCRETE,
            "CANON_RI_TIMELINE.md; every event has chapter citation; C5 confidence for most",
            null, null),
        new PolicyEntry("timeline_year_numbers", "Specific in-universe year numbers (e.g. 'Year ~100')", Classification.REASONABLE_RECONSTRUCTION,
            "Canon gives chapter numbers, not in-universe years. The chapter-to-year mapping is inferred from Wang Lin's age at key points.",
            "Generation must respect: (a) Wang Lin is 16 at bead discovery; (b) ~60 at Soul Formation; (c) ~2000+ at Transcendence; (d) the era structure is CANON_CONCRETE; only the specific year numbers are REASONABLE_RECONSTRUCTION.", null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 12: SPECULATION ENTRIES (Type D — most constrained tier)
        // ═══════════════════════════════════════════════════════════════════

        // ── D2: DERIVED_SPECULATION (conf 2) — has a canon anchor ──

        new PolicyEntry("spec_tu_si_life", "Tu Si's specific life story before the legacy",
            Classification.SPECULATION,
            null,
            "Canon establishes Tu Si left a legacy but is silent on his life. A full Ancient God's life is logically necessary for the legacy to exist.",
            "Generation must respect: (a) Tu Si was an Ancient God-tier cultivator; (b) his legacy is the Ancient God Tactic (essence: plunder); (c) his body became the Land of the Ancient God; (d) MUST NOT contradict any canon mention. MUST NOT invent wholesale.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "Tu Si left the Ancient God Tactic as a legacy (canon: techniques doc). His body became the Land of the Ancient God (canon: world doc). An Ancient God's life must have produced these.",
            "The legacy's nature (plunder essence, body-land transformation) constrains what kind of life Tu Si led. He was a plunder-type Ancient God whose death created geography.",
            "Promoted to CANON_IMPLIED if any canon source describes Tu Si's life events."),

        new PolicyEntry("spec_bai_fan_life", "Bai Fan's specific life story before the Six Paths Triple Arts",
            Classification.SPECULATION,
            null,
            "Canon establishes Bai Fan left the Six Paths Triple Arts (including Mountain Crumble) but is silent on his life.",
            "Generation must respect: (a) Bai Fan was an ancient cultivator; (b) his arts include Mountain Crumble (divine-sense-based mountain manipulation); (c) MUST NOT contradict any canon mention. MUST NOT invent wholesale.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "Bai Fan created the Six Paths Triple Arts (canon: techniques doc), including Mountain Crumble which Wang Lin inherits. The nature of these arts constrains what kind of cultivator Bai Fan was.",
            "Mountain Crumble is a divine-sense-based art (manipulating physical mountains via divine sense). Bai Fan must have been a divine-sense specialist of at least Soul Transformation tier.",
            "Promoted to CANON_IMPLIED if any canon source describes Bai Fan's life."),

        new PolicyEntry("spec_lu_mo_identity", "Lu Mo's exact composition of fused Essences",
            Classification.SPECULATION,
            null,
            "Canon establishes Lu Mo as a Slaughter-Silence clone but the exact essence composition is debated between sources.",
            "Generation must respect: (a) Lu Mo IS the Slaughter-Silence clone (canon); (b) his composition may include Restriction, Absolute Beginning, Absolute End, and Thunder (Baidu); (c) MUST NOT contradict the core Slaughter-Silence identity.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "Lu Mo is explicitly the Slaughter-Silence clone (canon). Baidu adds 'Restriction + Absolute Beginning + Absolute End + Thunder' to the fusion, but this is not wiki-confirmed.",
            "The fusion of Slaughter + Silence is the minimum canon baseline. Additional essences are plausible (Wang Lin masters all 14) but unconfirmed for this specific clone.",
            "Promoted to CANON_IMPLIED if wiki or novel text confirms the additional essences. Currently flagged in UNFINISHED_RESEARCH.md."),

        new PolicyEntry("spec_heavenly_fate_divisions_structure", "Internal structure of the Heavenly Fate Sect's 7 color divisions beyond the named disciples",
            Classification.SPECULATION,
            null,
            "Canon names the 7 color divisions and a few division heads, but not the internal structure (elders, disciples, resources) of each division.",
            "Generation must respect: (a) each division has one color and one head; (b) divisions serve the All-Seer's grand scheme; (c) the Red Division (Yao Xinghai) is the strongest; (d) divisions may cooperate or compete per the All-Seer's design.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "The 7 color divisions are canon (world doc: Heavenly Fate Sect entry). The Red Division head is Yao Xinghai (canon). The divisions exist to serve the All-Seer's calculations.",
            "A sect with 7 distinct divisions implies internal structure parallel to each division's purpose. The All-Seer's calculative nature means the divisions are likely specialized (divination, combat, intelligence, etc.).",
            "Promoted to REASONABLE_RECONSTRUCTION if any canon source describes division internals."),

        new PolicyEntry("spec_cave_world_pre_seal_history", "History of the Cave World before the Seven-Colored Daoist's ownership",
            Classification.SPECULATION,
            null,
            "Canon establishes the Cave World exists and is owned by Seven-Colored Daoist, but its origin (natural or artificial?) and pre-ownership history are not detailed.",
            "Generation must respect: (a) the Cave World is at the same cosmological layer as the Sealed Realm; (b) it has its own world-law; (c) the Seven-Colored Daoist claimed it, not created it (implied by 'ownership' language); (d) MUST NOT contradict the ownership model.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "The Cave World exists as a distinct cosmological layer (canon: world doc L04). The Seven-Colored Daoist owns it (canon: L04 + antagonist finding). The Joss Flame economy depends on a mortal population that predates the Daoist's ownership.",
            "A mortal population capable of generating Joss Flame implies the Cave World was habitable before the Daoist. The 'ownership' language (not 'creation') implies pre-existing geography.",
            "Promoted to CANON_IMPLIED if any canon source describes the Cave World's origin."),

        new PolicyEntry("spec_realm_sealing_array_original_purpose", "The original purpose of the Realm-Sealing Grand Array before repurposing",
            Classification.SPECULATION,
            null,
            "Canon establishes the array exists and was used to seal the Sealed Realm, but whether it was BUILT for that purpose or repurposed from an older function is not stated.",
            "Generation must respect: (a) the array's current function is canon (seal Third Step cultivators); (b) the Heaven-Splitting Axe is its spirit (canon); (c) the array predates the current political situation; (d) MUST NOT contradict the array's known mechanics.",
            SpeculationSubTier.DERIVED_SPECULATION,
            "The Realm-Sealing Grand Array is an ancient artifact with a spirit (Heaven-Splitting Axe, canon). Its complexity suggests it was not hastily built for the Sealed Realm.",
            "Artifacts with sentient spirits typically have long histories. The array's power level (seals Third Step) suggests it was designed for a purpose at least as significant as its current use.",
            "Promoted to CANON_IMPLIED if any canon source describes the array's pre-seal purpose."),

        // ── D1: BARE_CONJECTURE (conf 1) — no direct canon anchor, last resort ──

        new PolicyEntry("spec_unnamed_sun_names", "The specific names of the 5-6 unnamed IAC Grand Empyrean Suns",
            Classification.SPECULATION,
            null,
            "Canon establishes 9 suns but names only ~3-4. The remaining names have zero canon basis.",
            "Generation must respect: (a) each is a Grand Empyrean (Paragon-tier); (b) naming convention should match the IAC's cultivation-aesthetic (Dao-sounding titles); (c) MUST leave space for Wang Lin as 10th Sun; (d) MUST NOT use any name from other novels' sun/star entities.",
            SpeculationSubTier.BARE_CONJECTURE,
            null, // No canon anchor — this is the defining feature of D1
            "The 9-sun structure is canon (world doc L03). ~3-4 are named (canon). The gap is purely nomenclatural — the structure exists, only the labels are missing.",
            "Promoted to SPECULATION_D2 if ANY canon source names even one additional Sun. Promoted to CANON_CONCRETE if all are eventually named."),

        new PolicyEntry("spec_wang_lin_parents_detailed", "Detailed personalities and histories of Wang Lin's parents (Wang Tian Sheng + mother)",
            Classification.SPECULATION,
            null,
            "Canon establishes Wang Lin's parents exist and his father is Wang Tian Sheng, but their detailed personalities and backstories are not given.",
            "Generation must respect: (a) father is Wang Tian Sheng (canon); (b) they are mortals in Zhao Country's Wang Family Village; (c) Wang Lin's personality (stubborn, patient, ruthless when necessary) must be partially heritable or environmentally shaped; (d) MUST NOT give them cultivation abilities (they are mortals).",
            SpeculationSubTier.BARE_CONJECTURE,
            null, // No canon anchor for personality details
            "Wang Lin's parents are named (father: Wang Tian Sheng) and located (Wang Family Village, Zhao Country). But personality and backstory are entirely absent from canon.",
            "Promoted to SPECULATION_D2 if any canon source describes their personalities. Promoted to CANON_IMPLIED if Wang Lin's personality is explicitly linked to a parent."),

        new PolicyEntry("spec_zhao_country_minor_sects", "The specific names and structures of Zhao Country's minor (non-Heng Yue, non-Teng) cultivation sects",
            Classification.SPECULATION,
            null,
            "Canon establishes Zhao Country has multiple cultivation sects but only details Heng Yue Sect and the Teng Clan. Other minor sects are referenced generically.",
            "Generation must respect: (a) Zhao Country's world-law tier is 'fragile' (ecology doc); (b) peak cultivation in Zhao Country is Core Formation (Zhang Hu, Xuan Dao Zi); (c) minor sects must be weaker than Heng Yue; (d) MUST NOT use any name from SYSTEMS_AUDIT_COMPLETE.md forbidden list.",
            SpeculationSubTier.BARE_CONJECTURE,
            null, // The existence of minor sects is REASONABLE_RECONSTRUCTION; their SPECIFIC NAMES are D1
            "The existence of other sects in Zhao Country is covered by REASONABLE_RECONSTRUCTION. This entry covers only the SPECIFIC NAMES AND INTERNAL STRUCTURES of those sects — which have no canon basis whatsoever.",
            "Promoted to SPECULATION_D2 if any canon source names a specific minor sect. Promoted to CANON_CONCRETE if detailed."),

        new PolicyEntry("spec_mortal_village_daily_life", "Specific daily-life details of mortal villages in the Cave World Joss Flame economy",
            Classification.SPECULATION,
            null,
            "Canon establishes the Joss Flame harvest loop but not the specific daily routines, festivals, or cultural practices of the mortal villages.",
            "Generation must respect: (a) the Joss Flame economy shapes every aspect of village life; (b) villages are unaware they are being harvested; (c) faith/piety drives flame generation; (d) village culture should feel like real xianxia mortal life, NOT generic fantasy; (e) MUST include the existential horror implicit in the system (villages that stop generating flames may be 'cleansed').",
            SpeculationSubTier.BARE_CONJECTURE,
            null, // The economy is canon; the daily-life details are entirely absent
            "The Joss Flame economy is CANON_CONCRETE (world doc L04). But the cultural practices of the harvested villages are never described. This entry covers ONLY the village-level cultural details.",
            "Promoted to SPECULATION_D2 if any canon source describes village life under Joss Flame harvest."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 12: MAJOR FACTIONS & SECTS — CANON_CONCRETE (Type A/5)
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("faction_heng_yue_sect", "Heng Yue Sect (恒岳派)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N25; CANON_RI_CIVILIZATION.md CIV-01; C5 confidence; Wang Lin's origin sect",
            null, null),
        new PolicyEntry("faction_fighting_evil_sect", "Fighting Evil Sect (斗邪宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md; CANON_RI_CIVILIZATION.md CIV-04; C5 confidence; Zhao Country rival sect",
            null, null),
        new PolicyEntry("faction_teng_clan", "Teng Clan (藤族)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N83 (Teng Huayuan); CANON_RI_CIVILIZATION.md CIV-11; C5 confidence; massacres Wang Lin's village (E13, E14)",
            null, null),
        new PolicyEntry("faction_soul_refining_sect", "Soul Refining Sect (炼魂宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md F03; CANON_RI_CIVILIZATION.md CIV-02; C5 confidence; Du Tian's sect; soul refining + restriction flags + Billion Soul Banner",
            null, null),
        new PolicyEntry("faction_corpse_yin_sect", "Corpse Yin Sect (尸阴宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N37 (Sun Tai); CANON_RI_CIVILIZATION.md CIV-03; C5 confidence; Yin-path corpse refinement sect in Sea of Devils",
            null, null),
        new PolicyEntry("faction_heavenly_fate_sect", "Heavenly Fate Sect (天运宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N113 (Tianyunzi); CANON_RI_CIVILIZATION.md CIV-05, CIV-08; C5 confidence; multi-planet sect with 7 color divisions; E20-E22",
            null, null),
        new PolicyEntry("faction_vermilion_bird_divine_sect", "Vermilion Bird Divine Sect (朱雀神宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N25, N58, N79-N82; CANON_RI_CIVILIZATION.md CIV-07; C5 confidence; manages generational Vermilion Bird inheritance; E46, E64",
            null, null),
        new PolicyEntry("faction_cloud_sky_sect", "Cloud Sky Sect (云天宗)", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md CIV-12; C5 confidence; Wang Lin becomes 6th-Gen Vermilion Bird Divine Emperor and masters this sect; E46, E47",
            null, null),
        new PolicyEntry("faction_yao_family", "Yao Family (姚家)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N98 (Yao Xinghai), N99 (Yao Xixue); CANON_RI_CIVILIZATION.md CIV-09; C5 confidence; Blood Ancestor's family; issues kill-order on Wang Lin; E93, E94",
            null, null),
        new PolicyEntry("faction_ancient_clan", "Ancient Clan (古族)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md L73, N116+; CANON_RI_CIVILIZATION.md CIV-10; C5 confidence; IAC great power; ancestral temples; E84-E90",
            null, null),
        new PolicyEntry("faction_dao_devil_sect", "Dao Devil Sect / Dao Demon Sect (道魔宗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N122; CANON_RI_CIVILIZATION.md; C4 confidence; IAC antagonist sect; Wang Lin devours its master; E32, E83",
            null, null),
        new PolicyEntry("faction_great_soul_sect", "Great Soul Sect (大魂宗)", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md CIV-13; C5 confidence; ancient sect on Alliance Star System; founder gives Wang Lin three gifts; E98",
            null, null),
        new PolicyEntry("faction_origin_sect", "Origin Sect / Guiyuan Sect (归一宗)", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md CIV-15; C5 confidence; Soul Formation-tier sect; multiple named NPCs (N70-N73: Zhao Yu, Lu Yanfei, Lu Yuncong)",
            null, null),
        new PolicyEntry("faction_da_lou_sword_sect", "Da Lou Sword Sect (大罗剑宗)", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md CIV-16; C5 confidence; Ling Tianhou's sect (N39); sword-focused; Alliance Star System",
            null, null),
        new PolicyEntry("faction_cultivation_alliance", "Cultivation Alliance (修真联盟)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_WORLD.md N60 (Mo Zhi); CANON_RI_CIVILIZATION.md CIV-17; C5 confidence; governing body of Alliance Star System",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 13: TECHNIQUE SYSTEMS — CANON_CONCRETE (Type A/5)
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("technique_underworld_ascension", "Underworld Ascension Method (黄泉升窍诀)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md first ### entry; Ch. 86; E12; C5 confidence; Situ Nan's foundational Yin-path art for Wang Lin",
            null, null),
        new PolicyEntry("technique_vermilion_bird_burning_heaven", "Vermilion Bird Burning Heaven Art (朱雀焚天功)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entry; E46; C5 confidence; 2nd-Gen Vermilion Bird lineage core fire art",
            null, null),
        new PolicyEntry("technique_ancient_god_tactic", "Ancient God Tactic (古神诀)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entry; E17; Tu Si legacy (N22); C5 confidence; body-cultivation method whose essence is 'plunder'",
            null, null),
        new PolicyEntry("technique_ji_realm_divine_sense", "Ji Realm / Extreme Realm Divine Sense (极境神识)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entry; E41; C5 confidence; red-lightning divine sense that doubles combat power but carries a curse",
            null, null),
        new PolicyEntry("technique_soul_refining_system", "Soul Refining System (炼魂体系)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md (10+ ### entries on soul techniques); E45; C5 confidence; soul capture, refinement, flag creation, soul-devourer nature",
            null, null),
        new PolicyEntry("technique_four_great_restrictions", "Four Great Restrictions (四大禁制)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entries for Annihilation, Time, Life-Death, Ancient Soul; E55 (Annihilation inheritance), E70; C5 confidence",
            null, null),
        new PolicyEntry("technique_original_spells", "Six Original Spells (六道本源之术)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entries for Sundered Night, Flowing Time, Dream Dao, Karma Print, Life-Death Seal, True-False Eternal Seal; E61, E62, E63, E69; C5 confidence",
            null, null),
        new PolicyEntry("technique_dao_domains", "Dao Domains (道域) — Life-Death, Karma, True-False, Battle Will", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entries for all four domain types; E18; C5 confidence",
            null, null),
        new PolicyEntry("technique_true_bodies", "True Body / Avatar System (真身/分身体系)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md (15+ ### entries); CANON_RI_COMPLETE_ITEMS.md avatar section; E86-E90; C5 confidence; Wang Lin's 11+ true bodies and avatars",
            null, null),
        new PolicyEntry("technique_cultivation_realms", "Cultivation Realm System (修真境界体系)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entries for every realm stage from Qi Condensation through Heaven Trampling; C5 confidence; the backbone of all power scaling",
            null, null),
        new PolicyEntry("technique_nine_cycle_celestial_refining", "Nine Cycle Celestial Refining Tactic (九转炼仙诀)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entry; Ch. 493; E07, E60; C5 confidence; Bai Fan's immortal-power cycling method",
            null, null),
        new PolicyEntry("technique_essence_cultivation", "Essence Cultivation Track (本源之道)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### 'Essence Cultivation Track'; E75-E81; C5 confidence; system by which cultivators comprehend and condense Essences for 3rd Step+",
            null, null),
        new PolicyEntry("technique_ancient_order_cultivation", "Ancient Order Cultivation Track (古序修炼体系)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### 'Parallel Track: Ancient Order Cultivation'; C5 confidence; parallel cultivation for Ancient Gods, Demons, Devils",
            null, null),
        new PolicyEntry("technique_heart_pounding_thunder", "Heart-Pounding Thunder & Nine Accompanying Thunders (心悸雷/九道极雷)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entries; E68; C5 confidence; Wang Lin's unique thunder system including Annihilating, Bloodline, Defying Thunders and Ethereal Fire",
            null, null),
        new PolicyEntry("technique_celestial_slaughter_art", "Celestial Slaughter Art (屠仙术)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md ### entry; E49; C5 confidence",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 14: MAJOR ITEMS / ARTIFACTS — CANON_CONCRETE (Type A/5)
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("item_heaven_splitting_axe", "Heaven-Splitting Axe / Ancestral Axe (开天斧)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; E71; C5 confidence; ancestral-level weapon; Realm-Sealing Array's sentient spirit",
            null, null),
        new PolicyEntry("item_blood_slaughter_sword", "Blood Slaughter Sword (血煞剑)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; C5 confidence; one of the Seven Swords of the Ancient Dao",
            null, null),
        new PolicyEntry("item_karma_whip", "Karma Whip (因果鞭)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; E52; C5 confidence; Wang Lin's self-created weapon anchored to Karma Essence",
            null, null),
        new PolicyEntry("item_restriction_flag", "Restriction Flag (禁旗)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; CANON_RI_COMPLETE_TECHNIQUES.md 'Restriction Flags Refining Method'; C5 confidence; set of 3 flags central to restriction combat",
            null, null),
        new PolicyEntry("item_billion_soul_banner", "Billion Soul Banner / Ten-Billion Soul Flag (十亿魂幡)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; C5 confidence; ultimate soul-refining weapon; Soul Refining Sect signature",
            null, null),
        new PolicyEntry("item_realm_defining_compass", "Realm-Defining Compass (界定罗盘)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; E72; N113 (Tianyunzi); C5 confidence; treasure spirit is Tianyunzi clone; core IS the Heaven-Defying Pearl",
            null, null),
        new PolicyEntry("item_copper_mirror", "Copper Mirror with Time Domain (铜镜)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; Ch. 662-664; C5 confidence; Wang Lin-crafted pseudo-celestial treasure with Time Domain",
            null, null),
        new PolicyEntry("item_slaughter_crystal", "Slaughter Crystal (杀戮晶)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; C5 confidence; condensed Slaughter Origin essence; fused with Qing Shui's sword",
            null, null),
        new PolicyEntry("item_great_heavenly_venerable_sun", "Great Heavenly Venerable Sun (大天尊日)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; C5 confidence; tricolor sun of Grand Empyrean power",
            null, null),
        new PolicyEntry("item_soul_devil_ship", "Soul Devil Ship (魂魔船)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; E96; C5 confidence; major vessel Wang Lin creates",
            null, null),
        new PolicyEntry("item_rain_celestial_sword", "Rain Celestial Sword (雨仙剑)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; E53; N62 (Xu Liguo); C5 confidence; mid-quality celestial sword from Zhou Yi",
            null, null),
        new PolicyEntry("item_god_slaying_war_chariot", "God-Slaying War Chariot (杀神战车)", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_ITEMS.md ### entry; C5 confidence; set of 3 war chariots; major mid-game combat asset",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 15: ECOLOGY SYSTEMS — mostly CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("eco_zhao_country_spirit_beasts", "Zhao Country Spirit Beast Ecosystem", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 1; C5 confidence; spirit vein-driven beast ecology with tier ceilings",
            null, null),
        new PolicyEntry("eco_sea_of_devils", "Sea of Devils Ecology (魔海)", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 4; C5 confidence; chaotic demon-cultivation sea with spatial rifts and ruin ecology",
            null, null),
        new PolicyEntry("eco_ancient_battlefield", "Ancient Battlefield / Land of the Ancient God", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 3; C5 confidence; corpse of Ancient God Tu Si; residual energy and unique ecology; E17",
            null, null),
        new PolicyEntry("eco_planet_suzaku_general", "Planet Suzaku General Ecosystem", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 2; C5 confidence; overall Planet Suzaku ecology including all countries beyond Zhao",
            null, null),
        new PolicyEntry("eco_allheaven_star_system", "Allheaven Star System Ecology", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 6; C5 confidence; mid-late game ecology where Wang Lin operates",
            null, null),
        new PolicyEntry("eco_sealed_realm_boundary", "Sealed Realm / Outer Realm Boundary Ecology", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md REGION 8; C5 confidence; unique ecology at Realm-Sealing Array boundary; E26",
            null, null),
        new PolicyEntry("eco_cave_world_spirit_veins", "Cave World Spirit Vein Network", Classification.CANON_IMPLIED,
            "CANON_RI_ECOLOGY.md Law G3 establishes spirit veins as ecological foundation; C5 for existence, C4 for full network mapping",
            "Canon establishes spirit veins determine cultivation potential and beast tier ceilings across the Cave World, but the full vein network is never fully mapped.",
            "Generation must respect: (a) major spirit veins are at canon-named locations (Heng Yue Mountain, Sea of Devils cores, etc.); (b) vein quality correlates with world-law tier of the region; (c) veins degrade if over-harvested."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 16: CIVILIZATION SYSTEMS — mixed CANON_CONCRETE / CANON_IMPLIED
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("civ_currency_hierarchy", "Currency Hierarchy (灵石/仙玉/宝物)", Classification.CANON_IMPLIED,
            "CANON_RI_CIVILIZATION.md §4.1; canon establishes spirit stones as currency (C5); full hierarchy is strongly implied",
            "Canon establishes spirit stones, celestial jade, and barter-for-treasures as currency tiers. The exact exchange rates and sub-denominations are not fully detailed.",
            "Generation must respect: (a) spirit stones are the base unit; (b) higher-tier currencies exist in higher-tier regions (celestial jade for IAC); (c) pills and materials serve as informal currency in barter; (d) NO paper money or minted coins."),
        new PolicyEntry("civ_trade_network", "Inter-Region Trade Network", Classification.CANON_IMPLIED,
            "CANON_RI_CIVILIZATION.md §4.2; Trading Planet (L19) and Li Dannan (N66) prove trade exists; C5 for existence, C3 for specific routes",
            "Trading Planet and multiple merchant NPCs (Li Dannan, Liu Jinbiao) prove inter-region trade. Specific routes and trade volumes are reconstructed.",
            "Generation must respect: (a) trade routes connect regions with complementary resources; (b) Sea of Devils is a dangerous trade corridor; (c) the Trading Planet is a hub; (d) Cultivation Alliance regulates Alliance Star System trade."),
        new PolicyEntry("civ_pill_economy", "Pill Economy System", Classification.CANON_IMPLIED,
            "CANON_RI_CIVILIZATION.md §4.3; pills are canon-attested as trade goods (C5); full economic system is implied",
            "Pills are central to cultivation advancement and are traded at every tier. The specific pill market mechanics (supply, demand, pricing by tier) are reconstructed.",
            "Generation must respect: (a) pill tier must match buyer's cultivation realm; (b) core formation+ pills require alchemist sects; (c) pill quality grades follow canon (ordinary/fine/perfect/transcendent); (d) rare pill ingredients drive exploration of dangerous areas."),
        new PolicyEntry("civ_artifact_economy", "Artifact / Celestial Treasure Economy", Classification.CANON_IMPLIED,
            "CANON_RI_CIVILIZATION.md §4.4; 175+ items in CANON_RI_COMPLETE_ITEMS.md prove a robust artifact economy; C5 for existence, C4 for market mechanics",
            "The sheer volume of artifacts implies a mature market. Specific pricing, appraisal systems, and auction houses are reconstructed.",
            "Generation must respect: (a) artifact tiers follow canon grades (mortal/foundation/core/nascent/soul/celestial/transcendent); (b) pseudo-celestial treasures are Wang Lin's innovation (Ch. 662-664); (c) celestial treasures are extremely rare in the Cave World; (d) NO artifact has random properties — every effect must derive from cultivation principles."),
        new PolicyEntry("civ_sect_lifecycle", "Sect Formation/Growth/Decline/Destruction Lifecycle", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md §6.1-§6.5; multiple sects shown forming, growing, declining, destroyed in canon; C5 confidence",
            null, null),
        new PolicyEntry("civ_zhao_country_power_structure", "Zhao Country Power Structure", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md §5.1; E13, E14; CIV-01, CIV-04, CIV-11; C5 confidence; Heng Yue vs Fighting Evil vs Teng Clan dynamics",
            null, null),
        new PolicyEntry("civ_planet_tian_yun_power_structure", "Planet Tian Yun Power Structure", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md §5.3; CIV-08; E20-E22; C5 confidence; Heavenly Fate Sect dominance and subordinate relationships",
            null, null),
        new PolicyEntry("civ_iac_power_structure", "IAC Power Structure (Nine Suns)", Classification.CANON_CONCRETE,
            "CANON_RI_CIVILIZATION.md §5.5; world doc L65-L72; E31-E35; N77, N78, N110-N131; C5 confidence; Grand Empyreans, Celestial Venerables, Imperial Preceptor",
            null, null),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 17: WORLD MECHANICS — mostly CANON_CONCRETE
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("mech_realm_sealing_grand_array", "Realm-Sealing Grand Array Mechanics (封界大阵)", Classification.CANON_CONCRETE,
            "E05; CANON_RI_COMPLETE_WORLD.md L05; CANON_RI_ECOLOGY.md Axiom 4; C5 confidence; structure, power source (Heaven-Splitting Axe), cultivation cap effects",
            null, null),
        new PolicyEntry("mech_sealed_realm_war", "Sealed Realm War mechanics", Classification.CANON_CONCRETE,
            "E26; CANON_RI_COMPLETE_WORLD.md; C5 confidence; political and ecological consequences of Sealed vs Outer Realm conflict",
            null, null),
        new PolicyEntry("mech_vermilion_bird_inheritance", "Vermilion Bird Divine Emperor Inheritance System", Classification.CANON_CONCRETE,
            "E46, E64; N25, N58, N79-N82; world doc L52; C5 confidence; generational mark transfer, trial requirements, Holy Token",
            null, null),
        new PolicyEntry("mech_three_souls_seven_spirits", "Three Souls and Seven Spirits System", Classification.CANON_CONCRETE,
            "E03; CANON_RI_COMPLETE_WORLD.md Seven-Colored Daoist entry; C5 confidence; Cave World soul architecture created by Seven-Colored Daoist",
            null, null),
        new PolicyEntry("mech_ji_realm_curse", "Ji Realm Curse Mechanics", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md 'Ji Realm Curse and Resolution' ### entry; E41; C5 confidence; curse effects and resolution conditions",
            null, null),
        new PolicyEntry("mech_bead_evolution_stages", "Heaven-Defying Bead Evolution Stages", Classification.CANON_CONCRETE,
            "CANON_RI_COMPLETE_TECHNIQUES.md 'Heaven-Defying Bead Evolution Stages' ### entry; C5 confidence; progressive awakening across the story",
            null, null),
        new PolicyEntry("mech_cave_world_cultivation_cap", "Cave World Third-Step Cultivation Cap", Classification.CANON_CONCRETE,
            "CANON_RI_ECOLOGY.md Axiom 4; E05; world doc L04; C5 confidence; law preventing Third-Step cultivators from arising naturally in the Cave World",
            null, null),
        new PolicyEntry("mech_heaven_trampling_bridge_trials", "Heaven Trampling Bridge Trial Mechanics", Classification.CANON_IMPLIED,
            "E34, E92, E101-E105; bridges_9 registers the list; C5 for bridge existence, C4 for trial mechanics",
            "The 9 bridges are CANON_CONCRETE. The specific trial mechanics (what each bridge tests, how trials manifest) are implied but not fully detailed.",
            "Generation must respect: (a) each bridge corresponds to one of Wang Lin's Essences; (b) trials test dao comprehension, not raw power; (c) the 9th bridge (Reincarnation) is the final trial; (d) bridge difficulty scales with the cultivator's other Essences."),

        // ═══════════════════════════════════════════════════════════════════
        // SECTION 18: ADDITIONAL FORBIDDEN ENTRIES — Prime Directive guards
        // ═══════════════════════════════════════════════════════════════════
        new PolicyEntry("forbidden_generic_fantasy_races", "Generic Fantasy Races (elves, dwarves, orcs, halflings)", Classification.FORBIDDEN,
            "Prime Directive + SYSTEMS_AUDIT_COMPLETE.md; the Cave World is strictly xianxia with no non-human mortal races attested",
            null,
            "Generation must NEVER introduce Western fantasy races. Only xianxia races (humans, Ancient Gods/Demons/Devils, spirit beasts, demons/ghosts) are permitted."),
        new PolicyEntry("forbidden_modern_technology", "Modern Technology in Cave World", Classification.FORBIDDEN,
            "Prime Directive + canon setting; technology level is strictly pre-industrial cultivation society",
            null,
            "Generation must NEVER introduce post-industrial technology (guns, electricity, computers). Only ancient Chinese + magical cultivation technology."),
        new PolicyEntry("forbidden_western_magic_systems", "Western Magic Systems (mana, elemental schools, Vancian magic)", Classification.FORBIDDEN,
            "Prime Directive + canon cultivation system; the Cave World uses qi/spiritual-energy-based cultivation exclusively",
            null,
            "Generation must NEVER use Western magic paradigms. All power systems must derive from qi cultivation, Ancient Order cultivation, or canon-attested special tracks."),
        new PolicyEntry("forbidden_non_ri_ergen_contamination", "Non-RI Er Gen Novel Mechanics (ISSTH/AWWP/Ptt/BTT content not cross-referenced)", Classification.FORBIDDEN,
            "SYSTEMS_AUDIT_COMPLETE.md correction #8, #16, #23; only 3 explicitly cross-referenced items are permitted (Paragon Wang, Bald Crane, 'The God')",
            null,
            "Generation must ONLY use RI-specific content. The 3 permitted cross-references are: (a) Wang Lin as 'Paragon Wang' in AWWP, (b) Su Ming sends Bald Crane to IAC, (c) Wang Lin as 'The God' referenced in later novels. All other Er Gen novel content is FORBIDDEN in RI's cosmology."),
        new PolicyEntry("forbidden_cave_world_native_third_step", "Native Cave World Cultivators Above Third Step (without external intervention)", Classification.FORBIDDEN,
            "Canon: Realm-Sealing Array prevents Third-Step cultivators from arising naturally; Ecology Axiom 4; E05; SYSTEMS_AUDIT_COMPLETE.md",
            null,
            "Generation must NEVER create a Cave World native who naturally reached Third Step or above. Any Third-Step character in the Cave World must have: (a) come from outside (e.g., Seven-Colored Daoist fragments), or (b) broken through during a moment when the seal was weakened (e.g., Wang Lin), or (c) been explicitly canon-attested (e.g., All-Seer).")
    ));

    // ─── Query Methods ───────────────────────────────────────────────────

    /**
     * Get the classification for a system.
     * Default: SPECULATION (most conservative — flag for review).
     */
    public static Classification classify(String systemId) {
        for (PolicyEntry e : POLICY) {
            if (e.systemId.equals(systemId)) return e.classification;
        }
        // Per the Prime Directive: unknown systems must be reviewed before generation.
        return Classification.SPECULATION;
    }

    /**
     * The "can intelligent generation create this?" gate.
     * Returns true if the system is CANON_IMPLIED, REASONABLE_RECONSTRUCTION, or SPECULATION.
     * Returns false if CANON_CONCRETE (no generation needed) or FORBIDDEN (no generation allowed).
     *
     * <p>Note: SPECULATION is allowed but MOST constrained — it must be derived from
     * established canon, not invented wholesale. Use only when REASONABLE_RECONSTRUCTION
     * cannot cover the gap.
     */
    public static boolean canGenerate(String systemId) {
        Classification c = classify(systemId);
        return c == Classification.CANON_IMPLIED
            || c == Classification.REASONABLE_RECONSTRUCTION
            || c == Classification.SPECULATION;
    }

    /**
     * Enhanced generation gate that also checks SPECULATION sub-tier.
     * Returns false for BARE_CONJECTURE entries unless {@code allowHighRisk} is true.
     *
     * <p>Use this method when the Forge mod wants to avoid generating D1 content
     * unless explicitly authorized (e.g. the player has explored enough that
     * the gap MUST be filled).
     *
     * @param systemId       the system to check
     * @param allowHighRisk  if true, BARE_CONJECTURE (D1) is also allowed
     * @return true if generation is permitted at the specified risk level
     */
    public static boolean canGenerate(String systemId, boolean allowHighRisk) {
        Classification c = classify(systemId);
        if (c == Classification.CANON_CONCRETE || c == Classification.FORBIDDEN) return false;
        if (c == Classification.CANON_IMPLIED || c == Classification.REASONABLE_RECONSTRUCTION) return true;
        if (c == Classification.SPECULATION) {
            PolicyEntry entry = getEntry(systemId);
            if (entry == null) return false;
            if (entry.speculationSubTier == SpeculationSubTier.BARE_CONJECTURE && !allowHighRisk) return false;
            return true;
        }
        return false;
    }

    /**
     * Whether intelligent generation MUST respect this constraint.
     * Returns the generation constraints for a system, or null if none.
     */
    public static String constraintsFor(String systemId) {
        PolicyEntry e = getEntry(systemId);
        return e != null ? e.generationConstraints : null;
    }

    /**
     * Get the full PolicyEntry for a system, or null if not registered.
     */
    public static PolicyEntry getEntry(String systemId) {
        for (PolicyEntry e : POLICY) {
            if (e.systemId.equals(systemId)) return e;
        }
        return null;
    }

    /**
     * Get all entries classified as SPECULATION (both D1 and D2).
     */
    public static List<PolicyEntry> getSpeculationEntries() {
        return POLICY.stream()
            .filter(PolicyEntry::isSpeculation)
            .collect(Collectors.toList());
    }

    /**
     * Get all high-risk speculation entries (D1 — BARE_CONJECTURE).
     * These require explicit authorization before generation.
     */
    public static List<PolicyEntry> getHighRiskSpeculations() {
        return POLICY.stream()
            .filter(PolicyEntry::isHighRisk)
            .collect(Collectors.toList());
    }

    /**
     * Get all SPECULATION entries of a specific sub-tier.
     */
    public static List<PolicyEntry> getSpeculationBySubTier(SpeculationSubTier subTier) {
        return POLICY.stream()
            .filter(e -> e.isSpeculation() && e.speculationSubTier == subTier)
            .collect(Collectors.toList());
    }

    /**
     * Get all entries of a given classification.
     */
    public static List<PolicyEntry> getByClassification(Classification classification) {
        return POLICY.stream()
            .filter(e -> e.classification == classification)
            .collect(Collectors.toList());
    }

    /**
     * Get all entries sorted by generation priority (lower = generate first).
     * CANON_CONCRETE and FORBIDDEN are at the end (no generation needed/allowed).
     */
    public static List<PolicyEntry> getByGenerationPriority() {
        return POLICY.stream()
            .sorted(Comparator.comparingInt(PolicyEntry::generationPriority))
            .collect(Collectors.toList());
    }

    /**
     * Get all FORBIDDEN entries.
     */
    public static List<PolicyEntry> getForbiddenEntries() {
        return POLICY.stream()
            .filter(PolicyEntry::isForbidden)
            .collect(Collectors.toList());
    }

    /**
     * Get all CANON_CONCRETE entries (immutable — no generation allowed).
     */
    public static List<PolicyEntry> getCanonConcreteEntries() {
        return POLICY.stream()
            .filter(PolicyEntry::isCanonConcrete)
            .collect(Collectors.toList());
    }

    /**
     * Get a summary count by classification tier.
     * Returns a map: classification name -> count.
     */
    public static Map<String, Long> getSummaryCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Classification c : Classification.values()) {
            long count = POLICY.stream().filter(e -> e.classification == c).count();
            counts.put(c.name(), count);
        }
        // Also break down SPECULATION by sub-tier
        long d2 = POLICY.stream()
            .filter(e -> e.speculationSubTier == SpeculationSubTier.DERIVED_SPECULATION).count();
        long d1 = POLICY.stream()
            .filter(e -> e.speculationSubTier == SpeculationSubTier.BARE_CONJECTURE).count();
        counts.put("SPECULATION_D2_" + SpeculationSubTier.DERIVED_SPECULATION.code, d2);
        counts.put("SPECULATION_D1_" + SpeculationSubTier.BARE_CONJECTURE.code, d1);
        return counts;
    }
}