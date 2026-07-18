package dev.ergenverse.wanglin.ai;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinSpeechPatterns — how Wang Lin TALKS, each pattern with a {@link Provenance}.
 *
 * <p>Traits describe what he <i>is</i>; habits describe what he <i>does</i>;
 * speech patterns describe <i>how he sounds</i>. A dialogue generator
 * (future, Layer 2) consults these patterns to produce canon-faithful
 * dialogue: terse to strangers, gentle with Li Muwan and Wang Ping,
 * respectfully formal to mentors, and — when the cold mask drops — capable
 * of devastating one-line threats that he then follows through on.
 *
 * <p>Per the Prime Directive: every pattern is canon-attested. Er Gen wrote
 * the dialogue; the pattern name is the simulation's generalization. The
 * examples in each pattern's description are paraphrased from canon, not
 * quoted verbatim (translation variants exist).
 *
 * <h2>Speech-pattern inventory (7 patterns)</h2>
 * <ol>
 *   <li><b>TERSE_TO_PEERS</b> — short, information-dense sentences to
 *       cultivators of equal or lower status.</li>
 *   <li><b>COLD_TO_STRANGERS</b> — formal, emotionally flat, and
 *       untrusting with unknown cultivators.</li>
 *   <li><b>RESPECTFUL_TO_MENTORS</b> — uses honorifics; bowing language
 *       toward Situ Nan, Tu Si, Bai Fan, Dun Tian.</li>
 *   <li><b>GENTLE_WITH_FAMILY</b> — softens entirely with Li Muwan,
 *       Wang Ping, Wang Yiyi, his parents.</li>
 *   <li><b>WANG_SELF_REFERENCE_WHEN_COLD</b> — refers to himself as
 *       "Wang" or "I, Wang Lin" when delivering cold declarations
 *       (third-person self-reference as a power marker).</li>
 *   <li><b>RARE_BUT_FOLLOWED_THROUGH_THREATS</b> — almost never
 *       threatens; when he does, the threat is a promise.</li>
 *   <li><b>IMAGE_RICH_INTERNAL_MONOLOGUE</b> — his interior narration is
 *       dense with natural imagery (rivers, mountains, frost, the dao of
 *       heaven); his spoken dialogue is sparse by contrast.</li>
 * </ol>
 *
 * <h2>The contrast rule</h2>
 * <p>Wang Lin's defining speech characteristic is the <b>contrast between
 * sparse external speech and dense internal monologue</b>. He says little;
 * he thinks in images. A dialogue generator must respect this asymmetry:
 * terse lines to the world, rich imagery in the (player-visible) thought
 * bubbles or manifestation narration.
 */
public final class WangLinSpeechPatterns {

    private WangLinSpeechPatterns() {}

    /**
     * A single Wang Lin speech pattern.
     *
     * @param patternId        stable identifier (e.g. {@code "TERSE_TO_PEERS"})
     * @param name             display name
     * @param description      one-paragraph canon-grounded description,
     *                         including paraphrased examples
     * @param exampleContext   a typical situation in which this pattern fires
     * @param typicalRecipient who he is typically speaking to when this
     *                         pattern is active
     * @param provenance       source novel, chapters, attestation,
     *                         confidence, ambiguities
     */
    public record SpeechPattern(
            String patternId,
            String name,
            String description,
            String exampleContext,
            String typicalRecipient,
            Provenance provenance
    ) {
        public SpeechPattern {
            if (patternId == null || patternId.isBlank()) {
                throw new IllegalArgumentException("SpeechPattern requires a patternId");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("SpeechPattern '" + patternId + "' requires a name");
            }
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException("SpeechPattern '" + patternId + "' requires a description");
            }
            if (provenance == null) {
                throw new IllegalArgumentException("SpeechPattern '" + patternId + "' requires a Provenance");
            }
            if (exampleContext == null) exampleContext = "";
            if (typicalRecipient == null) typicalRecipient = "";
        }
    }

    // ───────────────────────────────────────────────────────────────────
    //  THE 7 CANON SPEECH PATTERNS
    // ───────────────────────────────────────────────────────────────────

    /**
     * TERSE_TO_PEERS — short, information-dense sentences to cultivators
     * of equal or lower status. No filler, no pleasantries, no
     * explanation. Canon: the majority of his dialogue to sect members,
     * fellow competitors, and subordinates.
     */
    public static final SpeechPattern TERSE_TO_PEERS = new SpeechPattern(
            "TERSE_TO_PEERS",
            "Terse to Peers — Short, Information-Dense Sentences",
            "Wang Lin's default speech register to cultivators of equal or lower status. "
                    + "Short sentences. No filler. No pleasantries. He states what is needed and "
                    + "stops. If a peer asks a question, he answers with the minimum viable "
                    + "information. He does not narrate his reasoning. A typical exchange: "
                    + "'Where is the herb?' / 'Three li north. Gone by dusk.' He is not rude — "
                    + "he is efficient. Silence fills the gaps where another cultivator would "
                    + "perform status.",
            "Routine sect interaction, market trade, post-battle debrief.",
            "Peers, subordinates, sect members of equal or lower realm.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 12-50 (Heng Yue Sect dialogue)",
                            "Ch. 180+ (Sea of Devils peer interactions)",
                            "E24 (Thunder Celestial Tournament — peer dialogue)"),
                    5,
                    "Pervasive across the novel; the dominant register of his spoken dialogue.")
    );

    /**
     * COLD_TO_STRANGERS — formal, emotionally flat, and untrusting with
     * unknown cultivators. He gives nothing away; his tone is a wall.
     * Canon: his first encounters with unknown cultivators throughout the
     * novel.
     */
    public static final SpeechPattern COLD_TO_STRANGERS = new SpeechPattern(
            "COLD_TO_STRANGERS",
            "Cold to Strangers — Formal, Emotionally Flat, Untrusting",
            "With unknown cultivators, Wang Lin's speech becomes formal, emotionally flat, "
                    + "and deliberately unrevealing. He answers questions with the fewest words "
                    + "possible, declines invitations without explanation, and offers no personal "
                    + "information. His tone is a wall: polite enough to avoid giving offense, "
                    + "cold enough to discourage further probing. He watches the stranger's "
                    + "reactions to this coldness — impatience or offense is noted; restraint "
                    + "earns a single cautious opening. This is the speech-layer expression of "
                    + "the INTENTION_TESTING habit.",
            "First encounter with any unknown cultivator.",
            "Strangers, unknown cultivators, anyone not yet vetted.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 50+ (Situ Nan — initial coldness before vetting)",
                            "E15 (Zhou Yi — cold first meeting)",
                            "E29 (Lu Yun — cautious opening)"),
                    4,
                    "Attested through consistent first-meeting dialogue patterns across the novel.")
    );

    /**
     * RESPECTFUL_TO_MENTORS — uses honorifics; bowing language toward
     * Situ Nan, Tu Si, Bai Fan, Dun Tian, Dao Master Ling Tianhou.
     * Canon: his reverence for those who taught him is explicit in his
     * speech.
     */
    public static final SpeechPattern RESPECTFUL_TO_MENTORS = new SpeechPattern(
            "RESPECTFUL_TO_MENTORS",
            "Respectful to Mentors — Honorifics, Bowing Language",
            "With his mentors — Situ Nan (the bead's spirit, who taught him the Underworld "
                    + "Ascension Method), Tu Si (the Ancient God whose inheritance he received), "
                    + "Bai Fan (whose six spells he inherited), Dun Tian, Dao Master Ling "
                    + "Tianhou — Wang Lin's speech shifts to formal honorifics and bowing "
                    + "language. He uses 'senior' or the mentor's title, never their bare name. "
                    + "He accepts instruction without argument. He thanks them explicitly and "
                    + "remembers the debt across lifetimes. This is the speech expression of "
                    + "GRATITUDE_TO_MENTORS.",
            "Any interaction with a recognized mentor or senior.",
            "Situ Nan, Tu Si, Bai Fan, Dun Tian, Dao Master Ling Tianhou, Xuan Luo.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 50+ (Situ Nan — 'senior' address throughout)",
                            "E43 (Tu Si — Ancient God inheritance, reverent address)",
                            "E27 (Bai Fan — six spells inheritance)"),
                    5,
                    "Explicitly attested; the honorific register with mentors is consistent and "
                            + "named through Er Gen's narration.")
    );

    /**
     * GENTLE_WITH_FAMILY — softens entirely with Li Muwan, Wang Ping,
     * Wang Yiyi, his parents. The cold mask drops; his voice warms.
     * Canon: his scenes with Li Muwan and Wang Ping are the novel's
     * tenderest.
     */
    public static final SpeechPattern GENTLE_WITH_FAMILY = new SpeechPattern(
            "GENTLE_WITH_FAMILY",
            "Gentle with Family — The Cold Mask Drops",
            "With Li Muwan (his wife), Wang Ping (his adopted son), Wang Yiyi (his daughter), "
                    + "and his parents Wang Tianshui and Zhou Tingsu, Wang Lin's speech softens "
                    + "entirely. The cold, terse register vanishes. He uses endearments, asks "
                    + "after their well-being, tells small jokes, recounts the day's small "
                    + "events. He listens more than he speaks. He is patient with childish "
                    + "questions and gentle with mortal fears. These scenes — particularly the "
                    + "mortal lifetime with Wang Ping (Ch. 701) — are the novel's tenderest. "
                    + "The contrast with his coldness to the world is the measure of his love.",
            "Private moments with Li Muwan, Wang Ping, Wang Yiyi, or his parents.",
            "Li Muwan, Wang Ping, Wang Yiyi, Wang Tianshui, Zhou Tingsu.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 701 (Wang Ping mortal life — tender dialogue)",
                            "E28 (Li Muwan's death — grieving speech)",
                            "E36 (Transcendence with Li Muwan)"),
                    5,
                    "Explicitly attested; the gentleness is the emotional core of the late novel.")
    );

    /**
     * WANG_SELF_REFERENCE_WHEN_COLD — refers to himself as "Wang" or
     * "I, Wang Lin" when delivering cold declarations. Third-person
     * self-reference as a power marker. Canon: his iconic cold declarations.
     */
    public static final SpeechPattern WANG_SELF_REFERENCE_WHEN_COLD = new SpeechPattern(
            "WANG_SELF_REFERENCE_WHEN_COLD",
            "'Wang' Self-Reference When Cold — Third-Person Self-Reference as Power Marker",
            "When Wang Lin delivers a cold declaration — a refusal, a verdict, a death sentence "
                    + "— he shifts to third-person self-reference: 'Wang does not...' or 'I, Wang "
                    + "Lin, would never...'. The shift is deliberate: it distances him from the "
                    + "interlocutor and elevates the statement from personal opinion to cosmic "
                    + "disposition. The most-cited instance: 'I, Wang Lin, would never kneel to "
                    + "the heavens' — the declaration that defines his Heaven-Defying Will. This "
                    + "register appears only in moments of high resolve or final judgement; in "
                    + "ordinary speech he uses the ordinary first person.",
            "Moments of high resolve, final judgement, or refusal of submission.",
            "Enemies, heavenly tribulation, any force demanding his submission.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 599 (third Heaven-Defying Cultivator declaration)",
                            "E34 (3rd Heaven Trampling Bridge — defying declaration)",
                            "Ch. 1368 (Defying Thunder formation)"),
                    5,
                    "Explicitly attested as a named rhetorical register; the 'I, Wang Lin' "
                            + "construction is one of the novel's signature lines.")
    );

    /**
     * RARE_BUT_FOLLOWED_THROUGH_THREATS — almost never threatens; when he
     * does, the threat is a promise. Canon: his threats are infrequent and
     * always executed.
     */
    public static final SpeechPattern RARE_BUT_FOLLOWED_THROUGH_THREATS = new SpeechPattern(
            "RARE_BUT_FOLLOWED_THROUGH_THREATS",
            "Rare but Followed-Through Threats — When He Threatens, He Executes",
            "Wang Lin almost never threatens. He does not posture, does not warn, does not "
                    + "give enemies the chance to reform. When he DOES speak a threat — usually "
                    + "after a defining wrong, such as the Teng Clan massacre of his family — it "
                    + "is a calm, factual statement of intent: 'The Teng Clan will end. I will "
                    + "see to it.' He then executes, regardless of how long it takes (the Teng "
                    + "Clan extermination took years). This pattern makes his threats uniquely "
                    + "terrifying: a threatened enemy knows that the words are a promise, not a "
                    + "negotiation. He does not make threats he will not keep.",
            "After a defining wrong has been committed against him or his.",
            "Enemies who have crossed an irreversible line (family harmed, trust betrayed).",
            Provenance.explicit("Renegade Immortal",
                    List.of("E13-E14 (Teng Clan — threat declared, then executed over years)",
                            "E22 (All-Seer — threat declared after plot revealed, then executed)",
                            "E30 (Seven-Colored Daoist — threat declared, then executed)"),
                    5,
                    "Explicitly attested; the 'rare threat → years-long execution' pattern is "
                            + "one of his most-characteristic behavioural signatures.")
    );

    /**
     * IMAGE_RICH_INTERNAL_MONOLOGUE — his interior narration is dense
     * with natural imagery (rivers, mountains, frost, the dao of heaven);
     * his spoken dialogue is sparse by contrast. Canon: Er Gen's
     * narration of Wang Lin's thoughts is famously image-laden.
     */
    public static final SpeechPattern IMAGE_RICH_INTERNAL_MONOLOGUE = new SpeechPattern(
            "IMAGE_RICH_INTERNAL_MONOLOGUE",
            "Image-Rich Internal Monologue — Dense Imagery in Thought, Sparse in Speech",
            "The defining asymmetry of Wang Lin's voice: his spoken dialogue is terse and "
                    + "plain, but his internal monologue — the narration Er Gen gives us of his "
                    + "thoughts — is dense with natural and cosmic imagery. He thinks in rivers, "
                    + "mountains, frost, falling leaves, the dao of heaven, the weight of karma. "
                    + "A combat decision that he speaks as 'Kill him' is internally narrated as "
                    + "'the karma of this life has settled; the river of his dao ends here, and "
                    + "the frost of my intention freezes the last current.' A dialogue generator "
                    + "must honour this: sparse external lines, rich internal narration.",
            "Continuous; the internal monologue accompanies nearly every decision.",
            "Himself (internal); the player perceives it via manifestation narration.",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 1433 (the '2,000 years alone' passage — image-rich solitude)",
                            "Ch. 599 (Heaven-Defying Will declaration — cosmic imagery)",
                            "Ch. 1715 (Restriction Essence comprehension — restriction imagery)"),
                    5,
                    "Explicitly attested; Er Gen's signature narrative voice for Wang Lin is "
                            + "image-dense internal monologue contrasting with sparse dialogue.")
    );

    /**
     * The complete list of Wang Lin's 7 canon speech patterns.
     * Used by the {@link WangLinPersonality} bootstrap.
     */
    public static final List<SpeechPattern> ALL_PATTERNS = List.of(
            TERSE_TO_PEERS,
            COLD_TO_STRANGERS,
            RESPECTFUL_TO_MENTORS,
            GENTLE_WITH_FAMILY,
            WANG_SELF_REFERENCE_WHEN_COLD,
            RARE_BUT_FOLLOWED_THROUGH_THREATS,
            IMAGE_RICH_INTERNAL_MONOLOGUE
    );

    /** Look up a speech pattern by its patternId. */
    public static SpeechPattern byId(String patternId) {
        if (patternId == null) return null;
        for (SpeechPattern p : ALL_PATTERNS) {
            if (p.patternId().equals(patternId)) return p;
        }
        return null;
    }
}
