/**
 * <h1>Layer 2 — Simulation: Wang Lin AI Personality Model</h1>
 *
 * <p><b>The canon-exact mind of Wang Lin as an in-game manifestation companion.</b>
 *
 * <p>This package encodes Wang Lin's personality, habits, speech patterns,
 * memories, relationships, and teaching policy as a runtime model that an
 * NPC / manifestation-companion AI can consult. The model is the <b>Simulation
 * Layer's design</b> for how Wang Lin thinks and behaves when he is manifested
 * alongside the player; it is <b>grounded in canon</b> via a
 * {@link dev.ergenverse.canon.Provenance} attached to every trait, habit,
 * memory, relationship detail, and teaching rule.
 *
 * <h2>Why this is Layer 2 (Simulation), not Layer 1 (Canon)</h2>
 * <p>What Er Gen wrote about Wang Lin — his traits, his memories, his
 * relationships, his techniques — IS canon, and lives in Layer 1
 * ({@link dev.ergenverse.canon}). What this package contains is the
 * <b>runtime AI model</b> that turns those canon facts into NPC behaviour:
 * how the manifestation decides what to say, what goal to pursue, and whether
 * to teach the player. That model is design. Er Gen never wrote a "personality
 * record" or a "teaching policy" — those are the game's implementation of his
 * characterization. Every field cites the canon it is derived from, but the
 * structure (Goal/Need/Fear/Memory/Relationship/Teaching-Policy composite)
 * is simulation design.
 *
 * <h2>Wang Lin as a manifestation companion</h2>
 * <p>Per the user's directive, Wang Lin is NOT the player. He is a canonical
 * protagonist who exists in the world. The player is an ADDITIONAL protagonist
 * entering his world. Per the Affinity System
 * ({@link dev.ergenverse.simulation.affinity}), Wang Lin's manifestation can
 * appear beside the player once sufficient affinity is built. When manifested,
 * he is not a scripted quest-giver — he is a <b>goal/need/fear/memory/
 * relationship-driven</b> NPC who:
 * <ul>
 *   <li>pursues his own canon motivations (protect Li Muwan, guard his
 *       network, maintain the Cave World);</li>
 *   <li>observes the player and forms an opinion based on the player's
 *       actions;</li>
 *   <li>can teach the player the ways of his techniques, items, and Dao —
 *       but <b>only on his own canon-faithful terms</b> (see
 *       {@link dev.ergenverse.wanglin.ai.WangLinTeachingPolicy}).</li>
 * </ul>
 *
 * <h2>Design philosophy — NOT a dialogue tree</h2>
 * <p>The model is deliberately <b>not</b> a branching dialogue tree. It is a
 * composite of records:
 * <pre>
 *   WangLinPersonality
 *     ├─ coreTraits      (WangLinTraits      — what he IS)
 *     ├─ habits           (WangLinHabits      — what he DOES reflexively)
 *     ├─ speechPatterns   (WangLinSpeechPatterns — how he TALKS)
 *     ├─ keyMemories      (List&lt;WangLinMemory&gt; — what shaped him)
 *     ├─ relationships    (WangLinRelationships — who he cares about / hates)
 *     └─ teachingPolicy   (WangLinTeachingPolicy — what he will SHARE and when)
 * </pre>
 * <p>A dialogue generator (future, Layer 2) consults this composite plus the
 * live game state (player affinity, recent player actions, current location)
 * to produce canon-faithful dialogue. This separation keeps the personality
 * immutable and canon-auditable, while letting the dialogue engine react to
 * emergent gameplay.
 *
 * <h2>Canon sourcing discipline</h2>
 * <p>Per the Prime Directive: NO INVENTED TRAITS. Every field cites the
 * Renegade Immortal novel — chapters where possible (e.g. <i>Ch. 1433</i> for
 * the "2,000 years alone" solitude passage), timeline events where a single
 * chapter is ambiguous (e.g. <i>E13 / CANON_RI_TIMELINE.md</i> for the Teng
 * Clan massacre). Where canon is silent on a specific detail, the field is
 * either omitted or marked {@code INFERRED} with a confidence of 1-2 and an
 * explicit ambiguity note. Canon silence is recorded as a gap, never invented
 * over.
 *
 * <h2>Files</h2>
 * <ul>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinPersonality} — the root
 *       composite; bootstrap loads the canonical model.</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinTraits} — 14 canon core traits
 *       (caution, ruthlessness, devotion to Li Muwan, filial devotion,
 *       paternal love, gratitude, patience, vengefulness, pragmatism,
 *       loneliness, mortal-world attachment, heaven-defying will,
 *       restriction obsession, reactive-not-initiating).</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinHabits} — 11 canon behavioural
 *       habits (divine-sense scouting, escape-talisman readiness, resource
 *       hoarding, puppet/Immortal-Guard maintenance, restriction practice,
 *       bead time-dilation cultivation, cultivation-level disguise, intention
 *       testing, enemy-soul storage, mortal-life comprehension,
 *       avatar/main-body split).</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinSpeechPatterns} — 7 canon
 *       speech patterns (terse, cold to strangers, respectful to mentors,
 *       gentle with Li Muwan/Wang Ping, "Wang" self-reference when cold,
 *       rare-but-followed-through threats, image-rich internal monologue).</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinMemory} — a single
 *       defining-memory record; ~23 instances indexed in
 *       {@link dev.ergenverse.wanglin.ai.WangLinPersonality}.</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinRelationships} — 16 canon
 *       relationships (Li Muwan, Situ Nan, Tu Si, Bai Fan, Qing Shui, Zhou Yi,
 *       Dun Tian, Lu Yun, Dao Master Ling Tianhou, Wang Ping, Wang Yiyi,
 *       Wang Tianshui &amp; Zhou Tingsu, Liu Mei / Mu Bingmei, Li Qianmei,
 *       the All-Seer, the Seven-Colored Daoist).</li>
 *   <li>{@link dev.ergenverse.wanglin.ai.WangLinTeachingPolicy} — the
 *       "teach me the ways of" subsystem. 16 canon teaching offerings with
 *       per-offering prerequisites and a {@code canTeach(...)} decision
 *       engine. Wang Lin does NOT teach everything to anyone — the policy
 *       encodes his canon-faithful reluctance (Heaven-Defying Bead mysteries
 *       are NEVER shared; the Underworld Ascension Method requires Situ Nan's
 *       permission; the Restriction Flag art requires demonstrated patience;
 *       life lessons about caution are shared FREELY to those who remind him
 *       of his younger self).</li>
 * </ul>
 *
 * <h2>Relationship to Layer 1 (Canon)</h2>
 * <p>This package imports {@link dev.ergenverse.canon.Provenance} for every
 * canon citation. It does NOT modify canon. It does NOT introduce new canon
 * facts. It IS the simulation-side interpretation of canon facts as an
 * AI-usable model. If a future canon revision adds or corrects a Wang Lin
 * fact, this package must be updated to match.
 *
 * <h2>Relationship to the Affinity System</h2>
 * <p>The Affinity System ({@link dev.ergenverse.simulation.affinity}) governs
 * <b>whether</b> Wang Lin's manifestation is present at all and how much the
 * player has earned his trust. This package governs <b>what Wang Lin does</b>
 * once manifested — including what he is willing to teach at the current
 * trust level. The two layers cooperate: Affinity is the prerequisite; the
 * Teaching Policy is the decision.
 *
 * <h2>Protagonist Sharing Philosophy — Wang Lin is CAUTIOUS</h2>
 * <p>Per the Simulation-layer
 * {@link dev.ergenverse.simulation.opportunity.ProtagonistSharingPhilosophy},
 * Wang Lin's temperament is {@code CAUTIOUS} — base trust 60, deep trust 95.
 * He evaluates every share request against: (a) does it endanger Li Muwan or
 * his network? (b) does it require a transmission he is not authorised to
 * grant? (c) does the recipient demonstrate the virtue the technique demands?
 * He refuses far more often than he agrees. His refusal is itself canon —
 * "I, Wang Lin, would never kneel to the heavens" is the disposition of a
 * cultivator who answers to his own Dao, not to social pressure.
 */
package dev.ergenverse.wanglin.ai;
