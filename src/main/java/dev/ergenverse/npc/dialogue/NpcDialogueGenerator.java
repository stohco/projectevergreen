package dev.ergenverse.npc.dialogue;

import dev.ergenverse.npc.goals.NpcGoalQueue;
import dev.ergenverse.npc.monologue.NpcInternalMonologue;
import dev.ergenverse.simulation.belief.BeliefStore;
import dev.ergenverse.simulation.action.ActorRelationshipStore;

import javax.annotation.Nullable;
import java.util.List;

/**
 * NpcDialogueGenerator — thought-driven dialogue generation (PROJECT_MASTER.md 6.7).
 *
 * <p>Per 6.7: "Don't store {@code 'Hello traveler.'} Store the <b>thought</b>.
 * Generator produces {@code 'Speak quickly.'} Another time:
 * {@code 'I don't have much time.'} Same thought, different wording."
 *
 * <p>This is a <b>stateless utility class</b> (no persistence). It synthesizes
 * dialogue from the cognitive subsystems in real time when the player
 * interacts with an NPC. The same underlying thought state can produce
 * different dialogue on each interaction, making NPCs feel alive.
 *
 * <h2>Input: cognitive state</h2>
 * <p>Dialogue is generated from:
 * <ul>
 *   <li><b>Internal Monologue (B.5)</b>: the NPC's current thought
 *       (objective, problem, concern, opportunity, danger, mood).</li>
 *   <li><b>Expectation Model (B.6)</b>: trust/danger/hostility predictions
 *       about the player — these determine the TONE of the dialogue.</li>
 *   <li><b>Memory (B.2)</b>: long-term social memories with this player
 *       influence warmth/repetition.</li>
 *   <li><b>Goal Queue (B.1)</b>: active goal determines urgency/busyness.</li>
 *   <li><b>Decision Style (B.1)</b>: patient_planner vs reactive, etc.</li>
 * </ul>
 *
 * <h2>Output: DialogueLine</h2>
 * <p>A {@link DialogueLine} contains the generated text, a tone enum,
 * and a "thought summary" showing what internal state produced it (for
 * debugging / future UI).
 *
 * <h2>Tone selection</h2>
 * <p>The tone is determined by a priority cascade:
 * <ol>
 *   <li>If hostile_intent >= 0.6 → {@link DialogueTone#HOSTILE}</li>
 *   <li>If trustworthy >= 0.6 and mood positive → {@link DialogueTone#WARM}</li>
 *   <li>If trustworthy &lt; 0.2 (stranger) → {@link DialogueTone#COLD}</li>
 *   <li>If active goal is urgent (DEFEND, ATTACK, FLEE) → {@link DialogueTone#URGENT}</li>
 *   <li>If decision style is patient_planner → {@link DialogueTone#MYSTERIOUS}</li>
 *   <li>Default → {@link DialogueTone#FORMAL}</li>
 * </ol>
 *
 * <h2>Template system</h2>
 * <p>Each tone has multiple templates parameterized by thought context.
 * A template is a format string with placeholders: {objective}, {problem},
 * {concern}, {danger}, {mood}. A {@link TemplateSlot} maps monologue fields
 * to natural-language fragments. The generator picks a random template
 * (seeded by world random for determinism) and fills the slots.
 *
 * <h2>Performance (6.12)</h2>
 * <p>No persistence, no ticking. Called only when the player actually
 * interacts with an NPC. O(1) per call — just template selection and
 * string formatting.
 *
 * <h2>Relationship to Wang Lin's system</h2>
 * <p>Wang Lin's {@code WangLinSpeechPatterns} describes his 7 canon speech
 * patterns (Provenance-anchored). This system is the generic generator for
 * ALL other NPCs. A future enhancement could make Wang Lin's generator
 * consult both this system AND his specific patterns for the most
 * canon-faithful output.
 *
 * @see DialogueLine  the generated output
 * @see DialogueTone  the tone enum
 * @see NpcDialogueTickHandler  the Forge event subscriber + static API
 */
public final class NpcDialogueGenerator {

    private NpcDialogueGenerator() {}

    // ─── Tone enum ────────────────────────────────────────────────────

    /**
     * The emotional register of a line of dialogue.
     * Each tone maps to a set of templates.
     */
    public enum DialogueTone {
        /** Short, information-dense, no filler. Busy or focused. */
        TERSE,
        /** Formal, emotionally flat, untrusting. First meetings. */
        COLD,
        /** Friendly, open, helpful. Positive relationship. */
        WARM,
        /** Polite, measured, respectful. Default for most NPCs. */
        FORMAL,
        /** Threatening, dismissive, hostile. High hostility prediction. */
        HOSTILE,
        /** Fast, clipped, panicked. Active danger/defense goal. */
        URGENT,
        /** Cryptic, elliptical, hinting. High-realm or secretive NPCs. */
        MYSTERIOUS,
        /** Looking down, dismissive of lower-realm beings. */
        CONDESCENDING
    }

    // ─── Generated output ────────────────────────────────────────────

    /**
     * A single generated line of dialogue. Immutable value object.
     */
    public static final class DialogueLine {
        /** The spoken text shown to the player. */
        public final String text;
        /** The emotional register of the line. */
        public final DialogueTone tone;
        /** One-line summary of the internal thought that produced this. */
        public final String thoughtSummary;
        /** Game tick when generated. */
        public final long generatedTick;

        public DialogueLine(String text, DialogueTone tone,
                            String thoughtSummary, long generatedTick) {
            this.text = text != null ? text : "...";
            this.tone = tone;
            this.thoughtSummary = thoughtSummary != null ? thoughtSummary : "";
            this.generatedTick = generatedTick;
        }
    }

    // ─── Context for generation ──────────────────────────────────────

    /**
     * All the cognitive state needed to generate one line of dialogue.
     * Assembled by the tick handler from B.1-B.6 subsystems.
     */
    public static final class NpcDialogueContext {
        public final String npcId;
        public final String targetPlayerUuid;
        public final NpcInternalMonologue.MonologueSnapshot monologue;
        public final double trustConfidence;
        public final double hostileConfidence;
        public final double dangerConfidence;
        public final boolean hasActiveUrgentGoal;
        public final NpcGoalQueue.DecisionStyle decisionStyle;
        public final int interactionCount; // how many times player interacted with this NPC

        // ── Art XXXI.5: Memory-driven dialogue ──
        /** The most recent WORLD_EVENT memory description, or null. */
        @Nullable
        public final String recentWorldEvent;
        /** The most recent OBSERVATION memory description, or null. */
        @Nullable
        public final String recentObservation;
        /** Whether the NPC has any WORLD_EVENT memory from the last 7 MC days. */
        public final boolean hasRecentWorldMemory;

        // ── Art XXXI.5: Rumor-driven dialogue (NPC→NPC retelling chain) ──
        /** The most recent RUMOR memory description, or null. */
        @Nullable
        public final String recentRumor;
        /** Whether the NPC has any RUMOR memory from the last 7 MC days. */
        public final boolean hasRecentRumor;

        // ── Belief-driven dialogue (2026-07-23 belief wiring) ──
        /** The dominant belief this NPC holds about the player (e.g. "dangerous", "compassionate"), or null. */
        @Nullable
        public final String dominantBeliefTrait;
        /** Confidence of the dominant belief (0.0–1.0), or 0 if no belief. */
        public final float dominantBeliefConfidence;
        /** All beliefs this NPC holds about the player (for template insertion). */
        public final java.util.List<BeliefStore.Belief> beliefsAboutPlayer;

        // ── Relationship-driven dialogue ──
        /** Cumulative affinity this NPC has with the player, or 0 if no relationship recorded. */
        public final int playerAffinity;

        public NpcDialogueContext(String npcId, String targetPlayerUuid,
                                  @Nullable NpcInternalMonologue.MonologueSnapshot monologue,
                                  double trustConfidence, double hostileConfidence,
                                  double dangerConfidence, boolean hasActiveUrgentGoal,
                                  NpcGoalQueue.DecisionStyle decisionStyle,
                                  int interactionCount,
                                  @Nullable String recentWorldEvent,
                                  @Nullable String recentObservation,
                                  boolean hasRecentWorldMemory,
                                  @Nullable String recentRumor,
                                  boolean hasRecentRumor) {
            this(npcId, targetPlayerUuid, monologue, trustConfidence, hostileConfidence,
                    dangerConfidence, hasActiveUrgentGoal, decisionStyle, interactionCount,
                    recentWorldEvent, recentObservation, hasRecentWorldMemory,
                    recentRumor, hasRecentRumor,
                    null, 0f, java.util.Collections.emptyList(), 0);
        }

        /** Full constructor including belief and relationship data. */
        public NpcDialogueContext(String npcId, String targetPlayerUuid,
                                  @Nullable NpcInternalMonologue.MonologueSnapshot monologue,
                                  double trustConfidence, double hostileConfidence,
                                  double dangerConfidence, boolean hasActiveUrgentGoal,
                                  NpcGoalQueue.DecisionStyle decisionStyle,
                                  int interactionCount,
                                  @Nullable String recentWorldEvent,
                                  @Nullable String recentObservation,
                                  boolean hasRecentWorldMemory,
                                  @Nullable String recentRumor,
                                  boolean hasRecentRumor,
                                  @Nullable String dominantBeliefTrait,
                                  float dominantBeliefConfidence,
                                  java.util.List<BeliefStore.Belief> beliefsAboutPlayer,
                                  int playerAffinity) {
            this.npcId = npcId;
            this.targetPlayerUuid = targetPlayerUuid;
            this.monologue = monologue;
            this.trustConfidence = trustConfidence;
            this.hostileConfidence = hostileConfidence;
            this.dangerConfidence = dangerConfidence;
            this.hasActiveUrgentGoal = hasActiveUrgentGoal;
            this.decisionStyle = decisionStyle;
            this.interactionCount = interactionCount;
            this.recentWorldEvent = recentWorldEvent;
            this.recentObservation = recentObservation;
            this.hasRecentWorldMemory = hasRecentWorldMemory;
            this.recentRumor = recentRumor;
            this.hasRecentRumor = hasRecentRumor;
            this.dominantBeliefTrait = dominantBeliefTrait;
            this.dominantBeliefConfidence = dominantBeliefConfidence;
            this.beliefsAboutPlayer = beliefsAboutPlayer != null
                    ? beliefsAboutPlayer : java.util.Collections.emptyList();
            this.playerAffinity = playerAffinity;
        }

        /**
         * Enrich an existing NpcDialogueContext with belief and relationship data
         * from BeliefStore and ActorRelationshipStore. Call this before passing
         * the context to generate().
         *
         * <p>This is the consumption point where the event-sourced architecture
         * becomes visible: beliefs formed from observed events now modulate dialogue.
         * The player performed deeds → events were published → beliefs were formed
         * → dialogue reflects those beliefs. The full cascade.
         *
         * @param base the existing context without belief/relationship data
         * @return a NEW context with belief and relationship data populated
         */
        public static NpcDialogueContext withBeliefs(NpcDialogueContext base) {
            String npcId = base.npcId;
            String playerUuid = base.targetPlayerUuid;
            if (npcId == null || npcId.isEmpty() || playerUuid == null || playerUuid.isEmpty()) {
                return base; // no belief enrichment possible
            }

            // ── Read beliefs from BeliefStore ──
            BeliefStore store = BeliefStore.get();
            List<BeliefStore.Belief> beliefs = store.beliefsAbout(npcId, playerUuid);

            String dominantTrait = null;
            float dominantConf = 0f;
            for (BeliefStore.Belief b : beliefs) {
                if (b.confidence() > dominantConf) {
                    dominantConf = b.confidence();
                    dominantTrait = b.trait();
                }
            }

            // ── Read relationship affinity from ActorRelationshipStore ──
            // (Requires a ServerLevel; if unavailable, affinity defaults to 0)
            int affinity = 0;
            try {
                net.minecraft.server.level.ServerLevel level =
                    dev.ergenverse.simulation.event.WorldEventBus.currentLevel();
                if (level != null) {
                    ActorRelationshipStore relStore = ActorRelationshipStore.get(level);
                    affinity = relStore.getAffinity(npcId, playerUuid);
                }
            } catch (Exception ignored) {
                // Non-server context (e.g., unit test) — skip relationship lookup.
            }

            return new NpcDialogueContext(
                    npcId, playerUuid, base.monologue,
                    base.trustConfidence, base.hostileConfidence, base.dangerConfidence,
                    base.hasActiveUrgentGoal, base.decisionStyle, base.interactionCount,
                    base.recentWorldEvent, base.recentObservation, base.hasRecentWorldMemory,
                    base.recentRumor, base.hasRecentRumor,
                    dominantTrait, dominantConf, beliefs, affinity);
        }
    }

    // ─── Template slots ──────────────────────────────────────────────

    /**
     * Maps a raw monologue field to a natural-language fragment for
     * insertion into dialogue templates.
     */
    private static String slotObjective(@Nullable NpcInternalMonologue.MonologueSnapshot m) {
        if (m == null || m.currentObjective.isEmpty()) return null;
        String obj = m.currentObjective;
        // Truncate long objectives for natural speech
        if (obj.length() > 40) obj = obj.substring(0, 37) + "...";
        return obj;
    }

    private static String slotProblem(@Nullable NpcInternalMonologue.MonologueSnapshot m) {
        if (m == null || m.problem.isEmpty()) return null;
        String p = m.problem;
        if (p.length() > 50) p = p.substring(0, 47) + "...";
        return p;
    }

    private static String slotMood(@Nullable NpcInternalMonologue.MonologueSnapshot m) {
        if (m == null) return "calm";
        return switch (m.moodLabel) {
            case "Elated", "Pleased" -> "good";
            case "Content" -> "settled";
            case "Calm" -> "calm";
            case "Uneasy" -> "troubled";
            case "Anxious" -> "anxious";
            case "Distressed" -> "terrible";
            default -> "calm";
        };
    }

    // ─── Tone selection ─────────────────────────────────────────────

    /**
     * Determine the dialogue tone from the cognitive context.
     * Priority cascade per the class Javadoc.
     */
    public static DialogueTone selectTone(NpcDialogueContext ctx) {
        // ── Belief modulation: adjust trust/hostile based on NPC's beliefs about the player ──
        // Beliefs are the NPC's SUBJECTIVE interpretation — they override objective predictions.
        // If an NPC believes the player is "dangerous", that's more powerful than a generic
        // hostileConfidence prediction. This is the "same event, different beliefs" principle.
        double effectiveTrust = ctx.trustConfidence;
        double effectiveHostile = ctx.hostileConfidence;

        if (ctx.dominantBeliefTrait != null && ctx.dominantBeliefConfidence >= 0.4f) {
            String trait = ctx.dominantBeliefTrait;
            float conf = ctx.dominantBeliefConfidence;
            // Positive beliefs boost trust; negative beliefs boost hostility.
            // The modulation scales with confidence: a strong belief overrides predictions.
            switch (trait) {
                case "compassionate", "kind", "courageous", "reliable",
                     "generous", "trustworthy", "powerful" ->
                    effectiveTrust = Math.max(effectiveTrust, conf * 0.8);
                case "dangerous", "cruel", "ruthless", "ruthless_toward_innocents",
                     "treacherous", "threatening", "arrogant" ->
                    effectiveHostile = Math.max(effectiveHostile, conf * 0.8);
                case "naive" -> {
                    // NPCs who think the player is naive may be condescending.
                    // No modulation — let the base predictions determine tone.
                }
                case "ambitious", "noteworthy" -> {
                    // Ambition can be respected or threatening depending on NPC type.
                    // No modulation — let the base predictions determine tone.
                }
            }
        }

        // ── Relationship affinity modulation ──
        // A deeply negative relationship (≤ -20) amplifies hostility.
        // A strongly positive relationship (≥ 20) amplifies warmth.
        if (ctx.playerAffinity <= -20) {
            effectiveHostile = Math.max(effectiveHostile, 0.7);
        } else if (ctx.playerAffinity >= 20) {
            effectiveTrust = Math.max(effectiveTrust, 0.7);
        }

        // 1. High hostility → HOSTILE
        if (effectiveHostile >= 0.6) return DialogueTone.HOSTILE;

        // 2. High trust + positive mood → WARM
        if (effectiveTrust >= 0.6 && ctx.monologue != null) {
            String mood = ctx.monologue.moodLabel;
            if (mood.equals("Elated") || mood.equals("Pleased")
                    || mood.equals("Content") || mood.equals("Calm")) {
                return DialogueTone.WARM;
            }
        }

        // 3. Low trust (stranger) → COLD
        if (effectiveTrust < 0.2 && ctx.interactionCount <= 1) {
            return DialogueTone.COLD;
        }

        // 4. Active urgent goal → URGENT
        if (ctx.hasActiveUrgentGoal) return DialogueTone.URGENT;

        // 5. Patient planner → MYSTERIOUS
        if (ctx.decisionStyle == NpcGoalQueue.DecisionStyle.PATIENT_PLANNER) {
            return DialogueTone.MYSTERIOUS;
        }

        // 6. Aggressive or survival-driven → TERSE
        if (ctx.decisionStyle == NpcGoalQueue.DecisionStyle.AGGRESSIVE_EXPANSIONIST
                || ctx.decisionStyle == NpcGoalQueue.DecisionStyle.SURVIVAL_DRIVEN) {
            return DialogueTone.TERSE;
        }

        // 7. High danger confidence → CONDESCENDING (they feel powerful)
        if (ctx.dangerConfidence >= 0.5) return DialogueTone.CONDESCENDING;

        // 8. Default
        return DialogueTone.FORMAL;
    }

    // ─── Dialogue generation ─────────────────────────────────────────

    /**
     * Generate a line of dialogue for an NPC speaking to a player.
     *
     * @param ctx the assembled cognitive context
     * @param randomSeed a seed from the world's RandomSource for
     *        deterministic but varied output
     * @return a generated DialogueLine (never null)
     */
    public static DialogueLine generate(NpcDialogueContext ctx, long randomSeed) {
        DialogueTone tone = selectTone(ctx);
        String text = generateText(tone, ctx, randomSeed);

        // Build thought summary for debug
        String thought = buildThoughtSummary(ctx, tone);

        return new DialogueLine(text, tone, thought,
                ctx.monologue != null ? ctx.monologue.generatedTick : 0);
    }

    /**
     * Get a memory fragment for insertion into dialogue templates.
     * Returns the recentWorldEvent, falling back to recentObservation.
     * Returns null if neither exists.
     */
    @Nullable
    private static String slotMemory(NpcDialogueContext ctx) {
        if (ctx.recentWorldEvent != null) return ctx.recentWorldEvent;
        if (ctx.recentObservation != null) return ctx.recentObservation;
        return null;
    }

    /**
     * Whether to use a memory-referencing template.
     * Only activates when the NPC has recent world memories.
     * Uses idx to make it occasional (not every line mentions memory).
     */
    private static boolean shouldReferenceMemory(NpcDialogueContext ctx, int idx) {
        return ctx.hasRecentWorldMemory && slotMemory(ctx) != null && idx < 25;
    }

    /**
     * Generate dialogue text for a given tone and context.
     */
    private static String generateText(DialogueTone tone, NpcDialogueContext ctx,
                                       long randomSeed) {
        String objective = slotObjective(ctx.monologue);
        String problem = slotProblem(ctx.monologue);
        String mood = slotMood(ctx.monologue);
        String memory = slotMemory(ctx);
        int idx = (int) Math.abs(randomSeed % 100);

        // Art XXXI.5: ~12% chance to reference a rumor (secondhand info, rarer).
        if (ctx.hasRecentRumor && ctx.recentRumor != null && idx < 12) {
            return generateRumorLine(tone, ctx.recentRumor, idx);
        }

        // 2026-07-23 belief wiring: ~18% chance to reference a belief about the player.
        // This makes the event-sourced architecture VISIBLE to the player:
        // "I've seen your cruelty" or "People say you're compassionate."
        if (shouldReferenceBelief(ctx, idx)) {
            return generateBeliefLine(tone, ctx, idx);
        }

        // Art XXXI.5: ~25% chance to reference a memory when one exists.
        if (shouldReferenceMemory(ctx, idx) && memory != null) {
            return generateMemoryLine(tone, memory, idx);
        }

        return switch (tone) {
            case TERSE -> generateTerse(objective, problem, idx);
            case COLD -> generateCold(objective, idx, ctx.interactionCount);
            case WARM -> generateWarm(objective, problem, mood, idx);
            case FORMAL -> generateFormal(objective, problem, idx, ctx.interactionCount);
            case HOSTILE -> generateHostile(objective, problem, idx);
            case URGENT -> generateUrgent(objective, problem, idx);
            case MYSTERIOUS -> generateMysterious(objective, problem, idx);
            case CONDESCENDING -> generateCondescending(objective, idx);
        };
    }

    // ─── Tone-specific generators ────────────────────────────────────
    // Each returns a line of dialogue. The idx parameter selects among
    // multiple variants for the same underlying thought (per 6.7).

    private static String generateTerse(@Nullable String objective,
                                        @Nullable String problem, int idx) {
        if (idx < 20 && objective != null)
            return "Focus on your own path. I have " + objective + " to attend to.";
        if (idx < 40 && problem != null)
            return problem + " — not your concern.";
        if (idx < 55)
            return "Speak quickly.";
        if (idx < 70)
            return "I don't have much time.";
        if (idx < 85)
            return "State your business.";
        return "What do you want?";
    }

    private static String generateCold(@Nullable String objective, int idx,
                                       int interactionCount) {
        if (interactionCount == 0) {
            // First meeting — coldest
            if (idx < 25) return "I don't know you.";
            if (idx < 50) return "...";
            if (idx < 75) return "You have business with me?";
            return "A stranger. How unusual.";
        }
        // Repeat meeting but still low trust
        if (idx < 25) return "You again.";
        if (idx < 50) return "What is it this time?";
        if (idx < 75) return "I remember you. That doesn't mean I trust you.";
        return "Make it quick. I haven't forgotten our last exchange.";
    }

    private static String generateWarm(@Nullable String objective,
                                       @Nullable String problem, String mood, int idx) {
        if (idx < 15 && objective != null)
            return "I was just thinking about " + objective
                    + ". Good timing — how can I help?";
        if (idx < 30 && problem != null)
            return "These are difficult times. " + problem
                    + ". But it's good to see a familiar face.";
        if (idx < 45)
            return "It's been too long. How have you been?";
        if (idx < 55 && mood.equals("good"))
            return "The dao smiles on us today. What brings you here?";
        if (idx < 70)
            return "I'm glad you came. I've been meaning to speak with you.";
        if (idx < 85)
            return "You've proven yourself before. What do you need?";
        return "Come, sit. Tell me what's on your mind.";
    }

    private static String generateFormal(@Nullable String objective,
                                         @Nullable String problem, int idx,
                                         int interactionCount) {
        if (interactionCount == 0) {
            if (idx < 20) return "Greetings, fellow cultivator.";
            if (idx < 40) return "Welcome. State your purpose.";
            if (idx < 60) return "I am not familiar with you. How may I assist?";
            return "A new face. The dao brings all manner of travelers.";
        }
        if (idx < 15 && objective != null)
            return "I trust your cultivation goes well. I am occupied with "
                    + objective + " at present.";
        if (idx < 30 && problem != null)
            return "There are matters that weigh on my mind — "
                    + problem + ". But we can speak.";
        if (idx < 50)
            return "We meet again. What news do you bring?";
        if (idx < 70)
            return "I have a moment. Tell me what concerns you.";
        return "The path of cultivation is long. It is good to walk it with companions.";
    }

    private static String generateHostile(@Nullable String objective,
                                          @Nullable String problem, int idx) {
        if (idx < 15 && objective != null)
            return "You dare approach me while I'm dealing with "
                    + objective + "? Leave. Now.";
        if (idx < 30 && problem != null)
            return "You think " + problem + " is a joke? Get out of my sight.";
        if (idx < 45)
            return "One more step and you'll regret it.";
        if (idx < 60)
            return "I know what you are. Don't test me.";
        if (idx < 75)
            return "Your presence offends me.";
        if (idx < 90)
            return "You've made a mistake coming here.";
        return "Leave. While you still can.";
    }

    private static String generateUrgent(@Nullable String objective,
                                         @Nullable String problem, int idx) {
        if (idx < 20 && objective != null)
            return "No time! " + objective + " — if you want to help, prove it. Otherwise stay out of the way!";
        if (idx < 40 && problem != null)
            return "Can't you see? " + problem + " — we don't have time for pleasantries!";
        if (idx < 55)
            return "Not now! Something urgent demands my attention.";
        if (idx < 70)
            return "Move! If you want to help, follow. If not, stay clear.";
        if (idx < 85)
            return "There's no time to explain. Either act or leave.";
        return "This can't wait. Make yourself useful or step aside.";
    }

    private static String generateMysterious(@Nullable String objective,
                                             @Nullable String problem, int idx) {
        if (idx < 15 && objective != null)
            return "I sense you're curious about " + objective
                    + ". Some things reveal themselves only to those who wait.";
        if (idx < 30 && problem != null)
            return "You see the surface. " + problem
                    + " — but the truth runs deeper than you know.";
        if (idx < 45)
            return "The dao moves in ways the impatient cannot perceive.";
        if (idx < 60)
            return "You ask the right questions. Whether you're ready for the answers... that remains to be seen.";
        if (idx < 75)
            return "I've watched many cultivators pass through here. Few stay long enough to understand.";
        if (idx < 90)
            return "There are patterns in the world that most overlook. I find them... interesting.";
        return "Perhaps what you seek is not what you need. Consider that.";
    }

    private static String generateCondescending(@Nullable String objective, int idx) {
        if (idx < 20 && objective != null)
            return "You couldn't begin to understand " + objective
                    + ". Run along now.";
        if (idx < 40)
            return "How... quaint. A junior cultivator approaches.";
        if (idx < 60)
            return "The gap between us is wider than you imagine.";
        if (idx < 80)
            return "I was breaking through realms before your grandparents were born.";
        return "Don't waste my time with trivialities.";
    }

    // ─── Memory-referencing dialogue (Art XXXI.5) ──────────────────
    // When an NPC has recent world memories, these templates insert
    // the memory content into natural dialogue. The Memory Metric test:
    // "Week 5 Old Chen says 'Used to have one.'" requires this.

    /**
     * Generate a memory-referencing line for the given tone.
     * Each tone has 2-3 variants. The memory text is inserted directly
     * — it was written by MemoryEventSubscriber from canon-faithful
     * event descriptions (e.g. "A wolf pack is moving through the
     * hills, their eyes catching the moonlight.").
     */
    private static String generateMemoryLine(DialogueTone tone, String memory, int idx) {
        // Truncate very long memory descriptions for natural speech.
        if (memory.length() > 80) memory = memory.substring(0, 77) + "...";

        return switch (tone) {
            case FORMAL -> {
                if (idx < 9) yield "There have been troubling signs. " + memory + ". We must be cautious.";
                if (idx < 18) yield "I've been thinking about what happened. " + memory + ". The village cannot ignore this.";
                yield "You should know — " + memory + ". Keep your guard up.";
            }
            case WARM -> {
                if (idx < 9) yield "It's good to see you. By the way — " + memory + ". Be careful out there.";
                if (idx < 18) yield "I've been meaning to tell someone. " + memory + ". I worry what it means for us.";
                yield "Listen — " + memory + ". Stay safe, will you?";
            }
            case TERSE -> {
                if (idx < 12) yield memory + ". Stay alert.";
                yield "Remember: " + memory + ".";
            }
            case COLD -> {
                if (idx < 12) yield "... " + memory + ". That is all.";
                yield memory + ". Do with that what you will.";
            }
            case URGENT -> {
                if (idx < 9) yield "Can't you see? " + memory + " — we need to act!";
                yield "This is exactly what I feared. " + memory + ". There's no time to waste!";
            }
            case HOSTILE -> {
                if (idx < 12) yield "And now " + memory.toLowerCase() + ". As if I needed more problems.";
                yield memory + ". If you caused this, you'll answer for it.";
            }
            case MYSTERIOUS -> {
                if (idx < 12) yield "The world stirs. " + memory + ". Perhaps you sense it too.";
                yield "I've seen signs. " + memory + ". The dao reveals what eyes cannot.";
            }
            case CONDESCENDING -> {
                yield memory + ". A triviality to one such as myself, but perhaps it concerns you.";
            }
        };
    }

    // ─── Rumor-referencing dialogue (Art XXXI.5 retelling chain) ──
    // When an NPC has heard a rumor (RUMOR memory from NPC→NPC spread),
    // these templates express it as secondhand information — hedged,
    // attributed to "someone," or uncertain. The Memory Metric test:
    // "Months later a child tells the story" requires this.

    /**
     * Generate a rumor-referencing line for the given tone.
     * Rumor dialogue is more hedged and secondhand than direct memory
     * dialogue — NPCs heard this from someone else, not witnessed it.
     */
    private static String generateRumorLine(DialogueTone tone, String rumor, int idx) {
        if (rumor.length() > 80) rumor = rumor.substring(0, 77) + "...";

        return switch (tone) {
            case FORMAL -> {
                if (idx < 3) yield "I've heard something troubling. " + rumor + ". I cannot confirm it.";
                if (idx < 6) yield "Word is spreading. " + rumor + ". We should be cautious about rumors, however.";
                yield "Someone mentioned — " + rumor + ". I don't know the source.";
            }
            case WARM -> {
                if (idx < 3) yield "Oh, have you heard? " + rumor + ". I'm not sure if it's true, but...";
                if (idx < 6) yield "People are talking. " + rumor + ". I thought you should know.";
                yield "Between us — " + rumor + ". Don't tell anyone I told you.";
            }
            case TERSE -> {
                if (idx < 4) yield "Heard a rumor. " + rumor + ".";
                yield "People say: " + rumor + ".";
            }
            case COLD -> {
                if (idx < 4) yield "... " + rumor + ". That's what they say.";
                yield "Someone told me " + rumor + ". I have no opinion on it.";
            }
            case URGENT -> {
                if (idx < 4) yield "Have you heard what they're saying? " + rumor + " — if it's true, we're in danger!";
                yield "The rumors — " + rumor + ". We need to verify this immediately.";
            }
            case HOSTILE -> {
                if (idx < 4) yield "Rumors. " + rumor + ". People will believe anything.";
                yield "Someone is spreading stories. " + rumor + ". Probably nonsense.";
            }
            case MYSTERIOUS -> {
                if (idx < 4) yield "Interesting whispers. " + rumor + ". There may be more to this than meets the eye.";
                yield "The wind carries strange tales. " + rumor + ". Truth or deception?";
            }
            case CONDESCENDING -> {
                yield "The common folk are abuzz. " + rumor + ". Amusing, but likely distorted.";
            }
        };
    }

    // ─── Thought summary (debug) ─────────────────────────────────────

    /**
     * Whether to use a belief-referencing template.
     * Only activates when the NPC has beliefs about the player with sufficient confidence.
     * Uses idx to make it occasional (~18% chance).
     */
    private static boolean shouldReferenceBelief(NpcDialogueContext ctx, int idx) {
        return ctx.dominantBeliefTrait != null
                && ctx.dominantBeliefConfidence >= 0.4f
                && !ctx.beliefsAboutPlayer.isEmpty()
                && idx < 18;
    }

    /**
     * Generate a belief-referencing line for the given tone.
     * The NPC expresses their subjective belief about the player.
     * This is the visible manifestation of the event-sourced architecture:
     * the player sees NPCs react to deeds the player performed in the past.
     */
    private static String generateBeliefLine(DialogueTone tone, NpcDialogueContext ctx, int idx) {
        String trait = ctx.dominantBeliefTrait;
        float conf = ctx.dominantBeliefConfidence;

        // Map belief traits to natural-language phrases.
        // Each trait produces different dialogue depending on the NPC's
        // existing tone — a WARM NPC mentions beliefs more kindly than a COLD NPC.
        String beliefPhrase = beliefToPhrase(trait);
        String confidenceHedge = conf >= 0.7 ? "" : "I'm beginning to think ";

        return switch (tone) {
            case FORMAL -> {
                if (idx < 3) yield "Word of your deeds has reached me. " + confidenceHedge + beliefPhrase.toLowerCase() + ".";
                if (idx < 6) yield "I've formed my own assessment. " + beliefPhrase + ". Make of that what you will.";
                yield "Let me be direct. " + beliefPhrase + ". I hope my judgment is wrong.";
            }
            case WARM -> {
                if (idx < 3) yield "I've seen what you've done. " + beliefPhrase.toLowerCase() + ". That speaks well of you.";
                if (idx < 6) yield "People talk, but I judge with my own eyes. " + beliefPhrase.toLowerCase() + ".";
                yield "Between us — " + confidenceHedge.toLowerCase() + beliefPhrase.toLowerCase() + ". I'm glad for it.";
            }
            case TERSE -> {
                if (idx < 5) yield "I know what you are. " + beliefPhrase + ". End of discussion.";
                yield beliefPhrase + ". Remember that.";
            }
            case COLD -> {
                if (idx < 3) yield "... " + beliefPhrase + ". Don't expect me to forget.";
                if (idx < 6) yield "I've observed you. " + beliefPhrase.toLowerCase() + ". That is all.";
                yield beliefPhrase + ". Whether that matters to you is not my concern.";
            }
            case HOSTILE -> {
                if (idx < 3) yield "Don't play innocent. " + beliefPhrase.toLowerCase() + ". I know exactly what you are.";
                if (idx < 6) yield "Your reputation precedes you. " + beliefPhrase.toLowerCase() + ". And I don't forgive easily.";
                yield beliefPhrase + ". That's why I'm telling you to leave. Now.";
            }
            case URGENT -> {
                if (idx < 5) yield "No time for pleasantries, but know this: " + beliefPhrase.toLowerCase() + ". Act accordingly.";
                yield "I'll remember what I know about you. " + beliefPhrase + ". Now move!";
            }
            case MYSTERIOUS -> {
                if (idx < 3) yield "Interesting. " + confidenceHedge.toLowerCase() + beliefPhrase.toLowerCase() + ". The dao reveals character through action.";
                yield "I've watched you. " + beliefPhrase.toLowerCase() + ". Whether that's a strength or weakness... time will tell.";
            }
            case CONDESCENDING -> {
                yield "How quaint. " + beliefPhrase.toLowerCase() + ". As if that matters to one such as myself.";
            }
        };
    }

    /**
     * Map a belief trait to a first-person attribution phrase.
     * E.g. "dangerous" → "I believe you are a dangerous person".
     */
    private static String beliefToPhrase(String trait) {
        return switch (trait) {
            case "compassionate" -> "I believe you have a compassionate heart";
            case "courageous" -> "I believe you possess true courage";
            case "kind" -> "I believe you are fundamentally kind";
            case "reliable" -> "I believe you honor your commitments";
            case "generous" -> "I believe you are generous";
            case "trustworthy" -> "I believe you are trustworthy";
            case "powerful" -> "I believe you possess real power";
            case "naive" -> "I believe you are dangerously naive";
            case "ambitious" -> "I believe you are ambitious";
            case "noteworthy" -> "I believe you are someone worth watching";
            case "dangerous" -> "I believe you are a dangerous person";
            case "cruel" -> "I believe you are cruel";
            case "ruthless" -> "I believe you are utterly ruthless";
            case "ruthless_toward_innocents" -> "I believe you harm those who cannot defend themselves";
            case "treacherous" -> "I believe you are treacherous";
            case "threatening" -> "I believe you are a threat";
            case "terrifying" -> "I believe you are terrifying";
            case "arrogant" -> "I believe you are arrogant";
            default -> "I believe you are " + trait;
        };
    }

    private static String buildThoughtSummary(NpcDialogueContext ctx, DialogueTone tone) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(tone.name()).append("] ");
        if (ctx.monologue != null) {
            if (!ctx.monologue.moodLabel.isEmpty())
                sb.append(ctx.monologue.moodLabel).append(". ");
            if (!ctx.monologue.currentObjective.isEmpty())
                sb.append("Obj: ").append(ctx.monologue.currentObjective).append(". ");
            if (!ctx.monologue.danger.isEmpty())
                sb.append("Danger: ").append(ctx.monologue.danger).append(". ");
        }
        sb.append("Trust:").append(Math.round(ctx.trustConfidence * 100)).append("% ");
        sb.append("Hostile:").append(Math.round(ctx.hostileConfidence * 100)).append("%");
        if (ctx.dominantBeliefTrait != null) {
            sb.append(" Belief:").append(ctx.dominantBeliefTrait)
              .append("(").append(Math.round(ctx.dominantBeliefConfidence * 100)).append("%)");
        }
        if (ctx.playerAffinity != 0) {
            sb.append(" Affinity:").append(ctx.playerAffinity > 0 ? "+" : "")
              .append(ctx.playerAffinity);
        }
        return sb.toString();
    }
}