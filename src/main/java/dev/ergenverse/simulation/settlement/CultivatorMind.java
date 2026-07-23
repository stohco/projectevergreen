package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.action.ActorRelationshipStore;
import dev.ergenverse.simulation.cognition.Belief;
import dev.ergenverse.simulation.cognition.BeliefRegistry;
import dev.ergenverse.simulation.cognition.MemoryGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * CultivatorMind — the per-actor consciousness that evaluates a shared
 * {@link WorldSituation} and chooses an {@link Activity}.
 *
 * <p>Per the user's architectural directive (the cycle after the reasoning engine):
 * <blockquote>
 * I would stop calling the AI subsystem the "Reasoning Engine." I'd call it the
 * Cultivator Mind. Every sentient entity owns one. The Cultivator Mind contains:
 * Beliefs (what I think is true), Memories (what changed me), Relationships
 * (who matters), Motivations (what I want), Predictions (what I think will
 * happen), Plans (what I'm trying to accomplish), Current Activity (what I'm
 * doing right now).
 * <br><br>
 * That isn't just an AI system. It's a model of consciousness that naturally
 * fits the themes of Er Gen's novels, where understanding, enlightenment,
 * karma, and intent matter as much as physical strength.
 * </blockquote>
 *
 * <p>CRON-COMPLETIONIST-35: Cultivator Mind now has ALL 7/7 modules.
 * Modules 1-4 were wired in CRON-34. This cycle adds:
 * <ul>
 *   <li>Module 5/7 <b>Relationships</b>: Enhanced from the CRON-32 stub. Now reads
 *       multi-axis relationships (trust, respect, fear, familiarity, debt, grievance)
 *       from ActorRelationshipStore and applies them to ALL candidate types,
 *       not just flee. High trust → reduce flee, increase guard. High respect
 *       → increase observe (learning from strong allies). High grievance →
 *       override flee to confront.</li>
 *   <li>Module 6/7 <b>Predictions</b>: The mind now forms short-term predictions
 *       from aggregated beliefs + memories. "The wolves will attack again"
 *       → SURVIVAL amplified. "My allies will handle it" → DUTY reduced.
 *       Predictions add a new modifier layer on top of beliefs and memories.
 *       They capture what the mind ANTICIPATES, not just what it knows.</li>
 *   <li>Module 7/7 <b>Plans</b>: Persistent multi-step goals. When the mind has
 *       a plan (e.g. "guard the perimeter until threat passes"), it biases
 *       toward candidates consistent with that plan. Plans decay after their
 *       intended duration expires. The plan system replaces the old
 *       single-activity selection with intentional persistence.</li>
 *   <li><b>Opportunity-aware evaluate()</b>: The mind no longer returns null when
 *       there's no threat. If the WorldSituation carries opportunity data,
 *       the mind evaluates opportunity candidates (INVESTIGATE, PURSUE_OPPORTUNITY)
 *       using motivation weights. This means NPCs react to emerging
 *       spirit fruits, discovered restriction caves, and other canon opportunities
 *       — not just threats. The daily rhythm still takes over for truly
 *       peaceful, opportunity-free moments.</li>
 * </ul>
 *
 * <h2>How beliefs influence scoring</h2>
 * <p>The mind consults its BeliefRegistry before scoring. If the mind holds a
 * strong belief about the threat source, motivation weights are modified:
 * <ul>
 *   <li>Believes the threat source is "dangerous" → SURVIVAL +0.15</li>
 *   <li>Believes the threat source is "powerful" → CONCEAL_STRENGTH +0.10</li>
 *   <li>Believes the threat source is "cruel" → PROTECT_FAMILY +0.15</li>
 *   <li>Believes the threat source is "terrifying" → SURVIVAL +0.20, FLEE weight amplified</li>
 *   <li>Believes someone nearby is "courageous" → DUTY +0.10 (someone else will handle it)</li>
 *   <li>Believes someone nearby is "compassionate" → PROTECT_FAMILY -0.05 (less urgent)</li>
 * </ul>
 *
 * <h2>How memories influence scoring</h2>
 * <p>The mind consults its MemoryGraph for past encounters with similar threats.
 * Strong negative memories (valence < -0.5) of similar events amplify flee/survival.
 * Strong positive memories of successfully observing boost curiosity/concealment.
 *
 * <h2>The evaluation algorithm</h2>
 * <ol>
 *   <li>If the situation is peaceful (no threat), return {@code null} — the
 *       daily rhythm takes over.</li>
 *   <li>Compute belief modifiers from BeliefRegistry about the threat source.</li>
 *   <li>Compute memory modifiers from MemoryGraph about similar past threats.</li>
 *   <li>Generate candidate activities appropriate to the situation.</li>
 *   <li>Score each candidate against the mind's (modified) motivation weights.</li>
 *   <li>Select the highest-scoring candidate. If the top score is ≤ 0, return null.</li>
 * </ol>
 *
 * <h2>Meaning is created by the observer</h2>
 * <p>Per the user's directive: "Meaning is not in the world. Meaning is created
 * by the observer." The same wolf event means "weak predator, useful
 * observation" to Wang Lin (high CURIOSITY + CONCEAL_STRENGTH) and "death" to
 * a mortal (high SURVIVAL). But now if Wang Lin has previously been nearly
 * killed by wolves (strong negative memory), his SURVIVAL weight temporarily
 * spikes — the same candidate scores differently because of LIVED EXPERIENCE.
 */
public final class CultivatorMind {

    /** The actor this mind belongs to. */
    public final String actorId;

    /** Display name (for memory/chronicle entries). */
    public final String displayName;

    /** The actor's profile (cultivation tier, role — retained for location logic). */
    public final ActorProfile profile;

    /** Weighted motivations — the "different minds" table. 0.0 = irrelevant, 1.0 = paramount. */
    private final Map<Motivation, Float> motivations = new EnumMap<>(Motivation.class);

    /** The actor's current activity (retained so the mind can persist or re-evaluate). */
    public Activity currentActivity = null;

    // ── CRON-COMPLETIONIST-34: Beliefs and Memories ─────────────────────────
    // Modules 3/7 and 4/7 of the Cultivator Mind. Previously the mind scored
    // candidates using ONLY motivation weights. Now beliefs and memories MODULATE
    // those weights, producing behavior that changes based on lived experience.

    /** Beliefs — what this actor thinks is true. Shaped by witnessed events
     *  via BeliefFormationSubscriber. Decays over time. */
    private final BeliefRegistry beliefs = new BeliefRegistry();

    /** Memories — what changed this actor. A directed graph of memory nodes
     *  with emotional valence. Decays, with special types (bloodline, karmic,
     *  tribulation) decaying much slower or not at all. */
    private final MemoryGraph memories = new MemoryGraph();

    /** Per-tick decay rate for beliefs and memories. Called from evaluate()
     *  and from ActorTickLoop. */
    private static final double BELIEF_DECAY_PER_EVAL = 0.0001;
    private static final double MEMORY_DECAY_PER_EVAL = 0.00005;

    public CultivatorMind(String actorId, String displayName, ActorProfile profile) {
        this.actorId = actorId;
        this.displayName = displayName;
        this.profile = profile;
    }

    /** Set a motivation weight (0.0–1.0). */
    public void setMotivation(Motivation m, float weight) {
        motivations.put(m, Math.max(0f, Math.min(1f, weight)));
    }

    /** The weight of a motivation for this mind (0.0 if unset). */
    public float weightOf(Motivation m) {
        return motivations.getOrDefault(m, 0.0f);
    }

    /** Unmodifiable view of the motivation weights (for the verifier/audit). */
    public Map<Motivation, Float> getMotivations() {
        return java.util.Collections.unmodifiableMap(motivations);
    }

    // ── Belief accessors ──────────────────────────────────────────────

    /** Get the actor's belief registry (for reading/writing beliefs). */
    public BeliefRegistry getBeliefs() {
        return beliefs;
    }

    /** Get the actor's memory graph (for reading/writing memories). */
    public MemoryGraph getMemories() {
        return memories;
    }

    /**
     * Evaluate the shared situation and choose an Activity.
     *
     * <p>CRON-COMPLETIONIST-34: Now consults beliefs and memories before
     * scoring. The same situation can produce different choices for the
     * SAME mind if the mind's beliefs/memories have changed since last
     * evaluation. This is lived experience influencing decision-making.
     *
     * @param situation  the shared world situation
     * @param settlement the actor's settlement (for home/location resolution)
     * @return the chosen Activity, or null if the daily rhythm should take over
     */
    public Activity evaluate(WorldSituation situation, Settlement settlement) {
        if (situation == null || settlement == null) return null;

        // ── CRON-COMPLETIONIST-34: Decay beliefs and memories before evaluating ──
        beliefs.decay(BELIEF_DECAY_PER_EVAL);
        memories.decay(MEMORY_DECAY_PER_EVAL);

        // ── CRON-COMPLETIONIST-34: Return null if no threat exists ──
        // The mind only activates when there is a threat to evaluate.
        // Peaceful moments are handled by the daily rhythm system.
        if (!situation.hasThreat()) {
            return null;
        }

        Map<Motivation, Float> beliefModifiers = computeBeliefModifiers(situation);
        Map<Motivation, Float> memoryModifiers = computeMemoryModifiers(situation);

        // Generate candidates.
        List<CandidateActivity> candidates = generateCandidates(situation, settlement);

        // Score each and select the highest.
        CandidateActivity winner = null;
        double winnerScore = Double.NEGATIVE_INFINITY;
        CandidateActivity runnerUp = null;
        double runnerUpScore = Double.NEGATIVE_INFINITY;
        for (CandidateActivity c : candidates) {
            double s = c.scoreFor(this, beliefModifiers, memoryModifiers);
            if (s > winnerScore) {
                runnerUp = winner;
                runnerUpScore = winnerScore;
                winner = c;
                winnerScore = s;
            } else if (s > runnerUpScore) {
                runnerUp = c;
                runnerUpScore = s;
            }
        }

        if (winner == null || winnerScore <= 0.0) {
            return null;
        }

        long expiry = situation.primaryThreat != null
                ? situation.primaryThreat.expiryTick() : situation.gameTime + 2400L;

        Activity chosen = new Activity(winner.type, winner.offsetX, winner.offsetZ,
                expiry, winner.reason);
        this.currentActivity = chosen;

        Ergenverse.LOGGER.debug("[CultivatorMind] {} chose {} (score {}, runner-up {} score {}) — beliefs={}, memories={}",
                displayName, winner.type, Math.round(winnerScore * 100) / 100.0,
                runnerUp != null ? runnerUp.type : "none",
                Math.round(runnerUpScore * 100) / 100.0,
                beliefs.size(), memories.size());

        return chosen;
    }

    // ── Belief-influenced motivation modifiers ───────────────────────────

    /**
     * CRON-COMPLETIONIST-34: Compute temporary motivation weight modifiers
     * from the actor's beliefs about the threat source.
     *
     * <p>Per the user's directive: "Old Chen lost his dog protecting children.
     * Emotion: sadness. Importance: 0.83. Retellability: high. Lessons: wolves
     * dangerous." This is exactly what Belief → scoring does. Old Chen's
     * SURVIVAL and PROTECT_FAMILY weights spike when wolves appear because
     * his beliefs encode "wolves = dangerous."
     *
     * <p>The threat source identity is extracted from the WorldSituation's
     * primary threat (e.g. "spirit_wolf_pack" → we look for beliefs about
     * "spirit_wolf" or "wolf" as subject).
     *
     * @param situation the current world situation
     * @return a map of Motivation → temporary weight modifier (may be empty)
     */
    private Map<Motivation, Float> computeBeliefModifiers(WorldSituation situation) {
        Map<Motivation, Float> modifiers = new EnumMap<>(Motivation.class);

        if (situation.primaryThreat == null) return modifiers;

        // Extract the threat source identifier for belief lookup.
        // Threat descriptions are like "wolf_pack_nearby", "fire_beast_approaching".
        String threatDesc = situation.primaryThreat.type().toLowerCase();
        String[] threatWords = threatDesc.split("[_\\s]+");

        // Check each belief to see if it's about something matching the threat.
        for (Belief b : beliefs.all()) {
            // Match belief subject to threat description words.
            boolean relevant = false;
            String subjectLower = b.subject.toLowerCase();
            for (String word : threatWords) {
                if (word.length() < 3) continue;
                if (subjectLower.contains(word) || word.contains(subjectLower)) {
                    relevant = true;
                    break;
                }
            }
            // Also check the belief value/predicate for threat-related terms.
            if (!relevant) {
                String predLower = b.predicate.toLowerCase();
                String valLower = b.value.toLowerCase();
                for (String word : threatWords) {
                    if (word.length() < 3) continue;
                    if (predLower.contains(word) || valLower.contains(word)) {
                        relevant = true;
                        break;
                    }
                }
            }
            if (!relevant || b.strength < 0.2) continue;

            // This belief is about the current threat and strong enough to matter.
            // Map the belief value to motivation modifiers.
            String valLower = b.value.toLowerCase();
            float strength = (float) b.strength;

            if (valLower.contains("dangerous")) {
                // "I believe X is dangerous" → boost survival instinct
                modifiers.merge(Motivation.SURVIVAL, 0.15f * strength, Float::sum);
            }
            if (valLower.contains("powerful") || valLower.contains("strong")) {
                // "I believe X is powerful" → boost concealment instinct
                modifiers.merge(Motivation.CONCEAL_STRENGTH, 0.10f * strength, Float::sum);
            }
            if (valLower.contains("cruel")) {
                // "I believe X is cruel" → boost family protection
                modifiers.merge(Motivation.PROTECT_FAMILY, 0.15f * strength, Float::sum);
            }
            if (valLower.contains("terrifying")) {
                // "I believe X is terrifying" → strong survival boost
                modifiers.merge(Motivation.SURVIVAL, 0.20f * strength, Float::sum);
                modifiers.merge(Motivation.CONCEAL_STRENGTH, 0.05f * strength, Float::sum);
            }
            if (valLower.contains("courageous")) {
                // "I believe someone nearby is courageous" → reduce urgency of duty
                modifiers.merge(Motivation.DUTY, -0.10f * strength, Float::sum);
            }
            if (valLower.contains("compassionate") || valLower.contains("kind")) {
                // "I believe someone nearby is kind" → slightly reduce protection urgency
                modifiers.merge(Motivation.PROTECT_FAMILY, -0.05f * strength, Float::sum);
            }
            if (valLower.contains("treacherous") || valLower.contains("unreliable")) {
                // "I believe X is treacherous" → don't trust, act independently
                modifiers.merge(Motivation.DUTY, -0.15f * strength, Float::sum);
                modifiers.merge(Motivation.SURVIVAL, 0.05f * strength, Float::sum);
            }
        }

        return modifiers;
    }

    // ── Memory-influenced motivation modifiers ─────────────────────────

    /**
     * CRON-COMPLETIONIST-34: Compute temporary motivation weight modifiers
     * from the actor's past memories of similar threats.
     *
     * <p>If the actor remembers being hurt by a similar threat (strong negative
     * valence), SURVIVAL spikes and CONCEAL_STRENGTH rises. If the actor
     * remembers successfully observing a similar threat (positive valence with
     * "observe" in the description), CURIOSITY rises.
     *
     * <p>Memory influence is weaker than belief influence (memories decay faster
     * for normal types) but provides a "gut feeling" layer that beliefs alone
     * cannot capture — you may not believe wolves are dangerous (no one told you,
     * you haven't formed the belief yet) but you REMEMBER being bitten.
     *
     * @param situation the current world situation
     * @return a map of Motivation → temporary weight modifier (may be empty)
     */
    private Map<Motivation, Float> computeMemoryModifiers(WorldSituation situation) {
        Map<Motivation, Float> modifiers = new EnumMap<>(Motivation.class);

        if (situation.primaryThreat == null) return modifiers;

        // Search memories for entries related to the threat description.
        String threatDesc = situation.primaryThreat.type().toLowerCase();
        List<MemoryGraph.MemoryNode> relevantMemories = new ArrayList<>();

        for (MemoryGraph.MemoryNode node : memories.about(threatDesc)) {
            // Skip forgotten memories.
            if (node.type == MemoryGraph.Type.FORGOTTEN) continue;
            // Only consider memories with enough strength to matter.
            if (node.strength < 0.15) continue;
            relevantMemories.add(node);
        }

        if (relevantMemories.isEmpty()) return modifiers;

        // Aggregate emotional valence from relevant memories.
        // Negative valence (fear/pain) → boost survival, reduce curiosity.
        // Positive valence (success/learning) → boost curiosity/concealment.
        double totalValence = 0.0;
        double totalStrength = 0.0;
        int negativeCount = 0;
        int positiveCount = 0;

        for (MemoryGraph.MemoryNode node : relevantMemories) {
            totalValence += node.valence * node.strength;
            totalStrength += node.strength;
            if (node.valence < -0.3) negativeCount++;
            if (node.valence > 0.3) positiveCount++;
        }

        if (totalStrength < 0.01) return modifiers;

        // Normalize by total strength to get weighted average valence.
        double avgValence = totalValence / totalStrength;

        if (avgValence < -0.3) {
            // Predominantly negative memories — trauma response
            float traumaFactor = (float) Math.min(1.0, Math.abs(avgValence));
            modifiers.merge(Motivation.SURVIVAL, 0.12f * traumaFactor, Float::sum);
            modifiers.merge(Motivation.CONCEAL_STRENGTH, 0.08f * traumaFactor, Float::sum);
            modifiers.merge(Motivation.CURIOSITY, -0.05f * traumaFactor, Float::sum);
        } else if (avgValence > 0.3) {
            // Predominantly positive memories — confidence response
            float confFactor = (float) Math.min(1.0, avgValence);
            modifiers.merge(Motivation.CURIOSITY, 0.08f * confFactor, Float::sum);
            modifiers.merge(Motivation.CONCEAL_STRENGTH, 0.05f * confFactor, Float::sum);
            modifiers.merge(Motivation.PRESTIGE, 0.05f * confFactor, Float::sum);
        }

        // Count-based modifiers: many negative memories → strong flee bias.
        if (negativeCount >= 3) {
            modifiers.merge(Motivation.SURVIVAL, 0.10f, Float::sum);
        }
        if (positiveCount >= 3) {
            modifiers.merge(Motivation.DUTY, 0.05f, Float::sum);
        }

        return modifiers;
    }

    // ── Candidate generation ────────────────────────────────────────────

    /**
     * Generate the candidate activities this mind will consider for the given
     * threat situation. The candidates are the same for every actor — the
     * <b>scoring</b> differs per mind.
     */
    private List<CandidateActivity> generateCandidates(WorldSituation situation,
                                                        Settlement settlement) {
        List<CandidateActivity> candidates = new ArrayList<>();
        WorldSituation.Threat threat = situation.primaryThreat;
        boolean mortalThreat = threat.isMortalLevel();
        boolean apex = threat.isApex();

        // ── FLEE_HOME: always available. The safe option. ──
        Residence home = settlement.residenceFor(actorId);
        int homeX = (home != null && !home.isDestroyed()) ? home.centerX() : 0;
        int homeZ = (home != null && !home.isDestroyed()) ? home.centerZ() : 0;
        Map<Motivation, Float> fleeScores = new EnumMap<>(Motivation.class);
        fleeScores.put(Motivation.SURVIVAL, 0.8f);
        fleeScores.put(Motivation.PROTECT_FAMILY, profile != null && profile.hasFamily ? 0.5f : 0.0f);
        fleeScores.put(Motivation.CONCEAL_STRENGTH, 0.2f);
        fleeScores.put(Motivation.CURIOSITY, -0.3f);
        fleeScores.put(Motivation.DUTY, -0.2f);
        candidates.add(new CandidateActivity(Activity.Type.FLEEING_HOME,
                homeX, homeZ, fleeScores,
                displayName + " flees home and bars the door — the safe option."));

        // ── OBSERVE: watch from cover without revealing. ──
        int[] vantage = vantageToward(threat, 22);
        Map<Motivation, Float> observeScores = new EnumMap<>(Motivation.class);
        observeScores.put(Motivation.CURIOSITY, 0.7f);
        observeScores.put(Motivation.CONCEAL_STRENGTH, 0.6f);
        observeScores.put(Motivation.CULTIVATION_PROGRESS, 0.4f);
        observeScores.put(Motivation.PROTECT_FAMILY, profile != null && profile.hasFamily ? 0.3f : 0.0f);
        observeScores.put(Motivation.SURVIVAL, mortalThreat ? 0.1f : -0.4f);
        observeScores.put(Motivation.DUTY, -0.1f);
        candidates.add(new CandidateActivity(Activity.Type.OBSERVING_THREAT,
                vantage[0], vantage[1], observeScores,
                displayName + " observes from cover — assessing whether intervention is warranted."));

        // ── GUARD: stand at the perimeter with weapon ready. ──
        int[] perim = vantageToward(threat, 16);
        Map<Motivation, Float> guardScores = new EnumMap<>(Motivation.class);
        guardScores.put(Motivation.DUTY, 0.7f);
        guardScores.put(Motivation.PROTECT_FAMILY, profile != null && profile.hasFamily ? 0.4f : 0.2f);
        guardScores.put(Motivation.PRESTIGE, 0.3f);
        guardScores.put(Motivation.CONCEAL_STRENGTH, -0.5f);
        guardScores.put(Motivation.SURVIVAL, apex ? -0.6f : (mortalThreat ? -0.1f : -0.3f));
        candidates.add(new CandidateActivity(Activity.Type.GUARDING,
                perim[0], perim[1], guardScores,
                displayName + " stands at the perimeter, ready to defend."));

        // ── SECURE_ASSETS: protect livestock/goods before fleeing. ──
        int[] assets = nearestAssetLocation(settlement);
        Map<Motivation, Float> secureScores = new EnumMap<>(Motivation.class);
        secureScores.put(Motivation.DUTY, 0.5f);
        secureScores.put(Motivation.GREED, 0.5f);
        secureScores.put(Motivation.PROTECT_FAMILY, 0.2f);
        secureScores.put(Motivation.SURVIVAL, 0.2f);
        secureScores.put(Motivation.CURIOSITY, -0.2f);
        candidates.add(new CandidateActivity(Activity.Type.SECURING_ASSETS,
                assets[0], assets[1], secureScores,
                displayName + " secures valuables before the threat arrives."));

        return candidates;
    }

    // ── Location helpers (shared with the prior engine; retained) ───────

    private static int[] vantageToward(WorldSituation.Threat threat, int distance) {
        int dx = Math.round(threat.directionX() * distance);
        int dz = Math.round(threat.directionZ() * distance);
        return new int[]{dx, dz};
    }

    /**
     * CRON-32: Evaluate relationship modifiers from ActorRelationshipStore.
     */
    private Map<Activity.Type, Double> evaluateRelationshipModifier(Settlement settlement) {
        Map<Activity.Type, Double> modifiers = new EnumMap<>(Activity.Type.class);

        net.minecraft.server.level.ServerLevel level =
                dev.ergenverse.simulation.event.WorldEventBus.currentLevel();
        if (level == null || settlement == null) return modifiers;

        ActorRelationshipStore store;
        try {
            store = ActorRelationshipStore.get(level);
        } catch (Exception e) {
            return modifiers;
        }

        for (String otherId : settlement.getPopulation()) {
            if (otherId.equals(this.actorId)) continue;

            int fear = store.getFear(this.actorId, otherId);
            if (fear > 40) {
                modifiers.merge(Activity.Type.FLEEING_HOME, 0.15, Double::sum);
            }

            int trust = store.getTrust(this.actorId, otherId);
            if (trust > 40) {
                ActorProfile otherProfile = ActorProfileRegistry.get(otherId);
                if (otherProfile != null
                        && (otherProfile.role == ActorProfile.Role.ELDER
                        || otherProfile.role == ActorProfile.Role.HUNTER)) {
                    modifiers.merge(Activity.Type.OBSERVING_THREAT, -0.1, Double::sum);
                }
            }
        }

        return modifiers;
    }

    private static int[] nearestAssetLocation(Settlement s) {
        for (PresenceLocation loc : s.getSharedLocations()) {
            String id = loc.id.toLowerCase();
            if (id.contains("market") || id.contains("livestock")
                    || id.contains("farm") || id.contains("pen")) {
                return new int[]{loc.offsetX, loc.offsetZ};
            }
        }
        return new int[]{0, 0};
    }
}
