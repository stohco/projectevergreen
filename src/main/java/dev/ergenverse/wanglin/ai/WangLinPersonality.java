package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.core.Ergenverse;

import java.util.List;

/**
 * WangLinPersonality — the root composite of Wang Lin's mind.
 *
 * <p>This is the single entry point for the Wang Lin AI. A dialogue
 * generator, behaviour scheduler, or manifestation controller consults the
 * {@link #CANONICAL} instance to access Wang Lin's:
 * <ul>
 *   <li>{@link WangLinTraits} — what he IS (14 core traits)</li>
 *   <li>{@link WangLinHabits} — what he DOES reflexively (11 habits)</li>
 *   <li>{@link WangLinSpeechPatterns} — how he TALKS (7 patterns)</li>
 *   <li>{@code keyMemories} — what SHAPED him (~23 defining memories)</li>
 *   <li>{@link WangLinRelationships} — who he CARES about / hates (17 relationships)</li>
 *   <li>{@link WangLinTeachingPolicy} — what he will SHARE and when (16 offerings + decision engine)</li>
 * </ul>
 *
 * <h2>Not a dialogue tree</h2>
 * <p>This composite is deliberately NOT a branching dialogue tree. It is a
 * data-rich personality model. A dialogue engine (future, Layer 2) reads
 * the composite plus live game state (player affinity, recent player
 * actions, current location) and generates canon-faithful dialogue. The
 * separation keeps the personality immutable and canon-auditable, while
 * letting the dialogue engine react to emergent gameplay.
 *
 * <h2>Bootstrap</h2>
 * <p>Call {@link #bootstrap()} once during mod init (after
 * {@link dev.ergenverse.wanglin.WangLinCosmologyRegistry#bootstrap()}).
 * This loads the {@link #CANONICAL} instance and logs a summary. The
 * bootstrap is idempotent.
 *
 * <h2>Canon sourcing</h2>
 * <p>Every field is canon-attested. The ~23 memories each carry a
 * {@link Provenance} citing the chapter(s) or timeline event(s). Where a
 * memory's exact chapter is ambiguous, the most attested single chapter is
 * cited plus an ambiguity note. Per the Prime Directive: NO INVENTED
 * MEMORIES. Where canon is silent on a specific detail, the memory is
 * omitted or marked INFERRED with confidence 1-2.
 */
public final class WangLinPersonality {

    private WangLinPersonality() {}

    /**
     * The composite personality record.
     *
     * @param coreTraits      the 14 canon core traits (from {@link WangLinTraits#ALL_TRAITS})
     * @param habits           the 11 canon habits (from {@link WangLinHabits#ALL_HABITS})
     * @param speechPatterns   the 7 canon speech patterns (from {@link WangLinSpeechPatterns#ALL_PATTERNS})
     * @param keyMemories      the ~23 defining memories (loaded by {@link #loadCanonicalMemories()})
     * @param relationships    the 17 canon relationships (from {@link WangLinRelationships#ALL_RELATIONSHIPS})
     * @param teachingPolicy   the teaching subsystem (consulted via {@link WangLinTeachingPolicy#canTeach})
     */
    public record Personality(
            List<WangLinTraits.Trait> coreTraits,
            List<WangLinHabits.Habit> habits,
            List<WangLinSpeechPatterns.SpeechPattern> speechPatterns,
            List<WangLinMemory> keyMemories,
            List<WangLinRelationships.Relationship> relationships
    ) {
        public Personality {
            if (coreTraits == null) coreTraits = List.of();
            if (habits == null) habits = List.of();
            if (speechPatterns == null) speechPatterns = List.of();
            if (keyMemories == null) keyMemories = List.of();
            if (relationships == null) relationships = List.of();
        }

        /** Convenience: count of each component, for logging. */
        public String summary() {
            return String.format(
                    "WangLinPersonality{traits=%d, habits=%d, speech=%d, memories=%d, relationships=%d, offerings=%d}",
                    coreTraits.size(), habits.size(), speechPatterns.size(),
                    keyMemories.size(), relationships.size(),
                    WangLinTeachingPolicy.ALL_OFFERINGS.size());
        }
    }

    /** The canonical, immutable personality. Loaded by {@link #bootstrap()}. */
    private static volatile Personality CANONICAL_INSTANCE;

    /** The canonical personality. Throws if bootstrap() has not been called. */
    public static Personality CANONICAL() {
        if (CANONICAL_INSTANCE == null) {
            throw new IllegalStateException("WangLinPersonality.bootstrap() has not been called.");
        }
        return CANONICAL_INSTANCE;
    }

    /** Whether the canonical personality has been loaded. */
    public static boolean isLoaded() {
        return CANONICAL_INSTANCE != null;
    }

    private static volatile boolean bootstrapped = false;

    /**
     * Bootstrap the Wang Lin personality model. Idempotent.
     *
     * <p>Loads the {@link #CANONICAL()} instance by composing the trait,
     * habit, speech-pattern, relationship, and teaching-policy subsystems
     * with the ~23 canon defining memories. Logs a summary.
     */
    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;

        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");
        Ergenverse.LOGGER.info("[Ergenverse]  WANG LIN AI PERSONALITY — bootstrapping");
        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");

        List<WangLinMemory> memories = loadCanonicalMemories();

        CANONICAL_INSTANCE = new Personality(
                WangLinTraits.ALL_TRAITS,
                WangLinHabits.ALL_HABITS,
                WangLinSpeechPatterns.ALL_PATTERNS,
                memories,
                WangLinRelationships.ALL_RELATIONSHIPS
        );

        Ergenverse.LOGGER.info("[Ergenverse]   ✓ {}", CANONICAL_INSTANCE.summary());
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Teaching offerings: {} (WILL_TEACH/REFUSES/OFFERS_PARTIAL/TESTS_FIRST decision engine)",
                WangLinTeachingPolicy.ALL_OFFERINGS.size());

        long traumas = memories.stream().filter(WangLinMemory::isDefiningTrauma).count();
        long fulfilments = memories.stream().filter(WangLinMemory::isSupremeFulfilment).count();
        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Memories: {} total ({} defining traumas, {} supreme fulfilments)",
                memories.size(), traumas, fulfilments);

        Ergenverse.LOGGER.info("[Ergenverse]   ✓ Canon-faithful gates enforced:");
        Ergenverse.LOGGER.info("[Ergenverse]     - Heaven-Defying Bead mysteries: NEVER shared (ABSOLUTE_UNIQUE)");
        Ergenverse.LOGGER.info("[Ergenverse]     - Underworld Ascension Method: requires Situ Nan's permission");
        Ergenverse.LOGGER.info("[Ergenverse]     - Restriction Flag art: requires demonstrated patience (trial gate)");
        Ergenverse.LOGGER.info("[Ergenverse]     - Life lessons of caution: FREELY shared to those who remind him of his younger self");

        Ergenverse.LOGGER.info("[Ergenverse] ──────────────────────────────────────────────────────");
        Ergenverse.LOGGER.info("[Ergenverse]  Wang Lin's mind is live. Canon sources: RI Ch. 8 → Ch. 1715.");
        Ergenverse.LOGGER.info("[Ergenverse] ════════════════════════════════════════════════════════");
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE ~23 CANON DEFINING MEMORIES
    // ───────────────────────────────────────────────────────────────────

    /**
     * Load the canon defining memories. Each is a canon-attested event from
     * Wang Lin's life, with an emotional weight and a {@link Provenance}.
     *
     * <p>The memories are ordered chronologically by timeline event.
     */
    private static List<WangLinMemory> loadCanonicalMemories() {
        return List.of(
                // ── Origin & childhood (E08-E12) ──────────────────────────
                new WangLinMemory("E08_birth",
                        "Birth in the Wang family village; given the mortal nickname 'Tie Zhu' (Iron Pillar) to ward off bad fortune.",
                        0.0,
                        "FILIAL_DEVOTION, MORTAL_WORLD_ATTACHMENT",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E08"), 5,
                                "Cross-referenced CANON_RI_TIMELINE.md E08.")),
                new WangLinMemory("E10_heng_yue_entry",
                        "Entered the Heng Yue Sect as a low-talent disciple; his dao dream (the stone bead) was dismissed.",
                        -0.2,
                        "PATIENCE, LONELINESS_ISOLATION",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 1-12", "E10"), 5,
                                "The Heng Yue Sect entry is the novel's opening arc.")),
                new WangLinMemory("E12_bead_acquired",
                        "Acquired the Heaven-Defying Bead from the Heng Yue Sect stone bead repository; unrecognized.",
                        +0.3,
                        "HEAVEN_DEFYING_WILL, RESTRICTION_OBSESSION",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 8", "E12"), 5,
                                "The bead acquisition is the novel's foundational artifact event.")),

                // ── The defining trauma (E13-E14) ─────────────────────────
                new WangLinMemory("E13_teng_massacre",
                        "The Teng Clan slaughtered the Wang family — his parents Wang Tianshui and Zhou Tingsu killed. His defining trauma.",
                        -1.0,
                        "FILIAL_DEVOTION, VENGEFULNESS, RUTHLESSNESS_TO_ENEMIES",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E13"), 5,
                                "The foundational trauma; cross-referenced CANON_RI_TIMELINE.md E13.")),
                new WangLinMemory("E14_teng_revenge_soul_rescue",
                        "Systematically exterminated the Teng Clan; rescued his parents' souls from Teng Huayuan's soul flag.",
                        +0.4,
                        "VENGEFULNESS, FILIAL_DEVOTION, ENEMY_SOUL_STORAGE",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E14"), 5,
                                "The revenge arc; the soul rescue is canon-explicit.")),

                // ── Sea of Devils & Foreign Battleground (E15-E24) ───────
                new WangLinMemory("E15_sea_of_devils_zhou_yi",
                        "Fled to the Sea of Devils; reunited with Zhou Yi; developed the Earth Escape.",
                        +0.2,
                        "EXTREME_CAUTION, PATIENCE",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 134", "E15"), 5,
                                "The Sea of Devils arc; Zhou Yi reunion is canon.")),
                new WangLinMemory("E20_qing_shui_sworn_brother",
                        "Sworn brotherhood with Qing Shui; recognized the Slaughter Dao lineage.",
                        +0.6,
                        "DEVOTION_TO_LI_MUWAN (Qing Shui parallels)",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E20+"), 5,
                                "The sworn-brother bond; one of the novel's deepest male friendships.")),
                new WangLinMemory("E22_all_seer_killed",
                        "Killed the All-Seer after his possession plot was revealed.",
                        +0.3,
                        "RUTHLESSNESS_TO_ENEMIES, REACTIVE_NOT_INITIATING",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E22"), 5,
                                "Canon-explicit killing after plot reveal.")),
                new WangLinMemory("E24_thunder_celestial_tournament",
                        "Destroyed multiple planets during the Thunder Celestial Tournament when the Yao Family issued a kill-order.",
                        -0.3,
                        "RUTHLESSNESS_TO_ENEMIES, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E24"), 5,
                                "The planet-destruction is canon-explicit.")),

                // ── Mentors & inheritances (E25-E27, E43) ─────────────────
                new WangLinMemory("E27_bai_fan_six_spells",
                        "Inherited Bai Fan's six spells and the bead's deeper recognition.",
                        +0.7,
                        "GRATITUDE_TO_MENTORS, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E27"), 5,
                                "The six-spells inheritance is canon-explicit.")),
                new WangLinMemory("E43_restriction_mountain_trial",
                        "Spent 7 years at the Restriction Mountain trial in the Land of the Ancient God; only the 4th person in history to complete it; his hair turned white.",
                        +0.5,
                        "RESTRICTION_OBSESSION, PATIENCE",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 179-180", "E43"), 5,
                                "The 7-year trial and the hair-whitening are canon-explicit.")),
                new WangLinMemory("E43_tu_si_inheritance",
                        "Received the Tu Si Ancient God inheritance at the Land of the Ancient God.",
                        +0.7,
                        "GRATITUDE_TO_MENTORS",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E43"), 5,
                                "The Ancient God inheritance is canon-explicit.")),

                // ── Li Muwan — the central arc (E28, E88, E36) ───────────
                new WangLinMemory("E28_li_muwan_death",
                        "Li Muwan's body perished during Nascent Soul formation. Stored her Nascent Soul in the Heaven-Defying Bead.",
                        -1.0,
                        "DEVOTION_TO_LI_MUWAN, PATIENCE, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E28"), 5,
                                "Her death is Wang Lin's lowest point; the 700-year storage is canon.")),
                new WangLinMemory("E88_qi_xi_spell",
                        "Attempted the Qi Xi Spell at the cost of his own vitality to rebuild Li Muwan's body. The spell failed.",
                        -0.8,
                        "DEVOTION_TO_LI_MUWAN, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E88"), 5,
                                "The failed spell is canon-explicit; the vitality cost is canon.")),
                new WangLinMemory("E36_transcendence_with_li_muwan",
                        "Resurrected and Transcended with Li Muwan. His supreme fulfilment.",
                        +1.0,
                        "DEVOTION_TO_LI_MUWAN, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E36"), 5,
                                "The Transcendence with her is the novel's culminating fulfilment.")),

                // ── Wang Ping — the mortal lifetime (E50, CD-31) ──────────
                new WangLinMemory("E50_wang_ping_mortal_life",
                        "Lived a mortal lifetime raising his adopted son Wang Ping in a desolate village.",
                        +0.4,
                        "PATERNAL_LOVE, MORTAL_WORLD_ATTACHMENT, PATIENCE",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 701", "E50"), 5,
                                "The mortal lifetime is one of the novel's most-cited arcs.")),
                new WangLinMemory("CD31_wang_ping_death_karma",
                        "Wang Ping's death triggered Wang Lin's Karma Domain evolution.",
                        -0.8,
                        "PATERNAL_LOVE, DEVOTION_TO_LI_MUWAN (Dao evolution)",
                        Provenance.explicit("Renegade Immortal",
                                List.of("CD-31"), 5,
                                "The Karma Domain evolution is canon-attested.")),

                // ── The Four Great Restrictions (E55, E55b, E55c, E55d) ────
                new WangLinMemory("E55_annihilation_restriction",
                        "Collected the Annihilation Restriction — the first of the Four Great Restrictions.",
                        +0.4,
                        "RESTRICTION_OBSESSION",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 754", "E55"), 5,
                                "Canon-explicit collection.")),
                new WangLinMemory("E55c_life_death_restriction",
                        "Collected the Life-Death Restriction — the third of the Four Great.",
                        +0.5,
                        "RESTRICTION_OBSESSION, MORTAL_WORLD_ATTACHMENT",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 1229", "E55c"), 5,
                                "Canon-explicit; bound to the Wang Ping mortal-life comprehension.")),

                // ── Heaven-Defying Will (E34, Ch. 599, Ch. 1368) ─────────
                new WangLinMemory("E34_third_heaven_trampling_bridge",
                        "Crossed the 3rd Heaven Trampling Bridge by embracing his inner demons instead of closing them off — via Heaven-Defying Will.",
                        +0.7,
                        "HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E34"), 5,
                                "The embracing-inner-demons crossing is canon-explicit.")),
                new WangLinMemory("Ch599_third_heaven_defying_cultivator",
                        "Recognized as the third Heaven-Defying Cultivator in history.",
                        +0.8,
                        "HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 599"), 5,
                                "The recognition is canon-explicit.")),

                // ── The cosmic antagonists (E30) ──────────────────────────
                new WangLinMemory("E30_seven_colored_daoist_killed",
                        "Killed the Seven-Colored Daoist after the farm nature of the Cave World was revealed.",
                        +0.5,
                        "RUTHLESSNESS_TO_ENEMIES, REACTIVE_NOT_INITIATING",
                        Provenance.explicit("Renegade Immortal",
                                List.of("E30", "CD-02"), 5,
                                "The farm-reveal and the killing are canon-explicit.")),

                // ── The capstone (E80, Ch. 1433, Ch. 1715) ───────────────
                new WangLinMemory("Ch1433_two_thousand_years_alone",
                        "The '2,000 years alone' passage — Wang Lin's solitude inside the bead, cultivating across centuries of interior time.",
                        -0.4,
                        "LONELINESS_ISOLATION, PATIENCE, DEVOTION_TO_LI_MUWAN",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 1433"), 5,
                                "One of the novel's most-cited passages; the solitude is canon-explicit.")),
                new WangLinMemory("E80_restriction_essence",
                        "Comprehended the Restriction Essence itself — the unity underlying all restrictions. The capstone of his restriction career.",
                        +0.8,
                        "RESTRICTION_OBSESSION, HEAVEN_DEFYING_WILL",
                        Provenance.explicit("Renegade Immortal",
                                List.of("Ch. 1715", "E80"), 5,
                                "The Restriction Essence comprehension is canon-explicit."))
        );
    }
}
