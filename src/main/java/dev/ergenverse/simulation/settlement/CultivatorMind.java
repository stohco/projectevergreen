package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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
 * <p>This is the architectural shift from the prior cycle's
 * {@code ActorReasoningEngine} (which was a {@code switch(profile)} — still
 * scripting, just per-role). The engine is now a <b>thin dispatcher</b>; the
 * <b>mind does the reasoning</b>. Nobody ever wrote "Wang Lin observes wolves."
 * Instead, Wang Lin observes because — given his motivation weights — the
 * OBSERVE candidate scores highest against the shared situation. A mortal with
 * different weights scores FLEEING_HOME highest. Same candidates, different
 * minds, different winner. That is emergence.
 *
 * <h2>What the mind owns (per the user's directive)</h2>
 * <ul>
 *   <li><b>motivations</b> — weighted drives (PROTECT_FAMILY, CONCEAL_STRENGTH,
 *       CURIOSITY, SURVIVAL, DUTY, ...). The "different minds" table.</li>
 *   <li><b>beliefs</b> — what this actor thinks is true (stub for future: threat
 *       assessment, relationship trust, world knowledge).</li>
 *   <li><b>currentActivity</b> — what the actor is doing right now (the last
 *       chosen Activity, retained so the mind can decide whether to persist
 *       or re-evaluate).</li>
 * </ul>
 *
 * <h2>The evaluation algorithm</h2>
 * <ol>
 *   <li>If the situation is peaceful (no threat), return {@code null} — the
 *       daily rhythm takes over. (The mind does not yet reason over peaceful
 *       opportunities; that is a future cycle per the user's "World Situation"
 *       list.)</li>
 *   <li>Generate candidate activities appropriate to the situation (OBSERVE,
 *       FLEE_HOME, GUARD, SECURE_ASSETS, and FIGHT for combat-capable actors).</li>
 *   <li>Score each candidate against the mind's motivation weights.</li>
 *   <li>Select the highest-scoring candidate. If the top score is ≤ 0 (no
 *       candidate serves the actor's drives), return {@code null} — daily
 *       rhythm takes over.</li>
 * </ol>
 *
 * <h2>Meaning is created by the observer</h2>
 * <p>Per the user's directive: "Meaning is not in the world. Meaning is created
 * by the observer." The same wolf event means "weak predator, useful
 * observation" to Wang Lin (high CURIOSITY + CONCEAL_STRENGTH) and "death" to
 * a mortal (high SURVIVAL). The {@link WorldSituation} carries no meaning —
 * the mind <b>creates</b> meaning through its scoring. A wolf is just a wolf
 * until a mind evaluates it.
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

    /**
     * Evaluate the shared situation and choose an Activity.
     *
     * <p>This is the core of the Cultivator Mind. The situation is the same for
     * every actor; the mind's motivation weights produce a different winner.
     *
     * @param situation  the shared world situation
     * @param settlement the actor's settlement (for home/location resolution)
     * @return the chosen Activity, or null if the daily rhythm should take over
     */
    public Activity evaluate(WorldSituation situation, Settlement settlement) {
        if (situation == null || settlement == null) return null;

        // Peaceful — the mind does not yet reason over opportunities. Daily
        // rhythm takes over. (Future: extend to opportunity/reasoning.)
        if (!situation.hasThreat()) {
            return null;
        }

        // Generate candidates appropriate to the situation.
        List<CandidateActivity> candidates = generateCandidates(situation, settlement);

        // Score each and select the highest.
        CandidateActivity winner = null;
        double winnerScore = Double.NEGATIVE_INFINITY;
        CandidateActivity runnerUp = null;
        double runnerUpScore = Double.NEGATIVE_INFINITY;
        for (CandidateActivity c : candidates) {
            double s = c.scoreFor(this);
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
            // No candidate serves this mind's drives strongly enough. The daily
            // rhythm takes over — the actor continues their routine undisturbed.
            return null;
        }

        long expiry = situation.primaryThreat != null
                ? situation.primaryThreat.expiryTick() : situation.gameTime + 2400L;

        Activity chosen = new Activity(winner.type, winner.offsetX, winner.offsetZ,
                expiry, winner.reason);
        this.currentActivity = chosen;

        Ergenverse.LOGGER.debug("[CultivatorMind] {} chose {} (score {}, runner-up {} score {}) — emerged from motivations, not scripted",
                displayName, winner.type, Math.round(winnerScore * 100) / 100.0,
                runnerUp != null ? runnerUp.type : "none",
                Math.round(runnerUpScore * 100) / 100.0);

        return chosen;
    }

    // ── Candidate generation ────────────────────────────────────────────

    /**
     * Generate the candidate activities this mind will consider for the given
     * threat situation. The candidates are the same for every actor — the
     * <b>scoring</b> differs per mind. This is deliberate: the situation offers
     * the same options to everyone; the mind's motivations decide which wins.
     *
     * <p>Per the user's directive: "Nobody special-cases Wang Lin. Nobody
     * special-cases Li Muwan. The reasoning emerges from what they care about."
     * The candidates are generated from the <b>situation</b>, not the profile.
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
        fleeScores.put(Motivation.CONCEAL_STRENGTH, 0.2f); // fleeing doesn't reveal
        fleeScores.put(Motivation.CURIOSITY, -0.3f);       // but loses information
        fleeScores.put(Motivation.DUTY, -0.2f);            // and abandons post
        candidates.add(new CandidateActivity(Activity.Type.FLEEING_HOME,
                homeX, homeZ, fleeScores,
                displayName + " flees home and bars the door — the safe option."));

        // ── OBSERVE: watch from cover without revealing. ──
        // Available to anyone, but only scores high for minds that value
        // concealment + curiosity over immediate survival.
        int[] vantage = vantageToward(threat, 22);
        Map<Motivation, Float> observeScores = new EnumMap<>(Motivation.class);
        observeScores.put(Motivation.CURIOSITY, 0.7f);
        observeScores.put(Motivation.CONCEAL_STRENGTH, 0.6f);
        observeScores.put(Motivation.CULTIVATION_PROGRESS, 0.4f); // watching teaches stillness
        observeScores.put(Motivation.PROTECT_FAMILY, profile != null && profile.hasFamily ? 0.3f : 0.0f);
        observeScores.put(Motivation.SURVIVAL, mortalThreat ? 0.1f : -0.4f); // safe vs wolves, risky vs apex
        observeScores.put(Motivation.DUTY, -0.1f);
        candidates.add(new CandidateActivity(Activity.Type.OBSERVING_THREAT,
                vantage[0], vantage[1], observeScores,
                displayName + " observes from cover — assessing whether intervention is warranted."));

        // ── GUARD: stand at the perimeter with weapon ready. ──
        // Scores high for minds that value duty + protection over concealment.
        int[] perim = vantageToward(threat, 16);
        Map<Motivation, Float> guardScores = new EnumMap<>(Motivation.class);
        guardScores.put(Motivation.DUTY, 0.7f);
        guardScores.put(Motivation.PROTECT_FAMILY, profile != null && profile.hasFamily ? 0.4f : 0.2f);
        guardScores.put(Motivation.PRESTIGE, 0.3f); // visible bravery
        guardScores.put(Motivation.CONCEAL_STRENGTH, -0.5f); // REVEALS position
        guardScores.put(Motivation.SURVIVAL, apex ? -0.6f : (mortalThreat ? -0.1f : -0.3f));
        candidates.add(new CandidateActivity(Activity.Type.GUARDING,
                perim[0], perim[1], guardScores,
                displayName + " stands at the perimeter, ready to defend."));

        // ── SECURE_ASSETS: protect livestock/goods before fleeing. ──
        // Scores high for minds with duty toward assets (laborer, merchant).
        int[] assets = nearestAssetLocation(settlement);
        Map<Motivation, Float> secureScores = new EnumMap<>(Motivation.class);
        secureScores.put(Motivation.DUTY, 0.5f);
        secureScores.put(Motivation.GREED, 0.5f);
        secureScores.put(Motivation.PROTECT_FAMILY, 0.2f);
        secureScores.put(Motivation.SURVIVAL, 0.2f); // safer than guarding
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
