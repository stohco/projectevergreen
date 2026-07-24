package dev.ergenverse.simulation.actor;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.memory.NpcMemoryTickHandler;
import dev.ergenverse.simulation.cognition.ActivityProcess;
import dev.ergenverse.simulation.cognition.CognitionGoal;
import dev.ergenverse.simulation.cognition.DecisionEngine;
import dev.ergenverse.simulation.cognition.MemoryGraph;
import dev.ergenverse.simulation.cognition.perception.AttentionFilter;
import dev.ergenverse.simulation.cognition.perception.Interpretation;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;
import dev.ergenverse.simulation.cognition.perception.PerceptionSensor;
import dev.ergenverse.simulation.cognition.prediction.ActionPredictor;
import dev.ergenverse.simulation.intent.Commitment;
import dev.ergenverse.simulation.intent.CommitmentContext;
import dev.ergenverse.simulation.intent.CompletionPredicate;
import dev.ergenverse.simulation.intent.CultivationTask;
import dev.ergenverse.simulation.intent.Intent;
import dev.ergenverse.simulation.intent.IntentDecomposer;
import dev.ergenverse.simulation.intent.IntentNature;
import dev.ergenverse.simulation.los.SimulationLevel;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * ActorTickLoop — seasonal / event-driven actor simulation tick.
 *
 * <p><b>Canon audit: NOT daily.</b> Per the Simulation Fidelity Directive,
 * actors are NOT simulated every Minecraft day. They are simulated:
 * <ul>
 *   <li><b>Seasonally</b> (every 7 MC days = ~1 season) for ACTIVE_ACTOR and below.</li>
 *   <li><b>On event</b> (when an event triggers them) for any level.</li>
 *   <li><b>On proximity</b> (when a player approaches within 4 chunks) for
 *       any level — promotes them to a higher sim level temporarily.</li>
 * </ul>
 *
 * <p>The loop reads from {@link ActorRegistry}, filters by sim level, and
 * dispatches to either:
 * <ul>
 *   <li>A light "territory/aggregated" tick for TERRITORY-level actors.</li>
 *   <li>A "goal + plan" tick for ACTIVE_ACTOR-level actors.</li>
 *   <li>A full {@link DecisionEngine#decide} call for FULL_COGNITION+ actors.</li>
 * </ul>
 */
public final class ActorTickLoop {

    /** One season = 7 MC days = 7 * 24000 ticks. */
    public static final long SEASON_TICKS = 7L * 24000L;

    /** Minimum gap between full-cognition ticks for the SAME actor.
     *  600 ticks = 30 seconds. This is fast enough that a player who
     *  walks up to a meditating NPC sees the NPC make a new decision
     *  within half a minute, but slow enough to avoid hot-spinning.
     *  Before this was 24000 (20 min) — so slow that the pipeline
     *  was effectively dead for linked actors. */
    public static final long FULL_COGNITION_MIN_GAP = 600L;

    private ActorTickLoop() {}

    /**
     * The ServerLevel currently being ticked. Set at the start of {@link #tick}
     * so that {@link #tickFullCognition} (which doesn't take a level parameter)
     * can pass it to {@link PerceptionSensor}. Cleared after the tick pass.
     *
     * <p>This is safe because the actor tick loop runs synchronously on the
     * server thread — there is no concurrent access.
     */
    private static ServerLevel currentServerLevel = null;

    /**
     * Run one actor-tick pass. Called from the main server tick loop.
     *
     * <p>Also syncs linked actors' positions from their Minecraft entities
     * (via {@link dev.ergenverse.simulation.intent.ActorEntityLink}) so the
     * IntentEngine's situational queries use current positions.
     *
     * <p><b>Importance score computation (CE #1 prerequisite):</b>
     * For each linked actor, we compute the distance-to-nearest-player
     * contribution and set storySignificance for canon NPCs. Without this,
     * all actors remain at STATIC_DATA (score 0) and the cognition pipeline
     * never fires. Per Article XLI: the simulation must work — this is the
     * minimum wiring to make it so.
     *
     * @param currentTick the current server tick (overworld gameTime)
     * @param level       the overworld ServerLevel (for entity lookups)
     */
    public static void tick(long currentTick, ServerLevel level) {
        // Track the current level for the duration of this tick pass so
        // tickFullCognition can pass it to PerceptionSensor.
        currentServerLevel = level;
        try {
            tickImpl(currentTick, level);
        } finally {
            currentServerLevel = null;
        }
    }

    private static void tickImpl(long currentTick, ServerLevel level) {
        // ── Position sync + importance score computation ──
        // Before this fix, SimulationImportanceScore was never written to,
        // so all actors had score 0 → STATIC_DATA → no cognition ever ran.
        // Now we compute distance for linked actors so player proximity
        // promotes them to ACTIVE_ACTOR or FULL_COGNITION.
        if (level != null) {
            List<ServerPlayer> players = level.players();
            for (Actor a : ActorRegistry.all()) {
                if (dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)) {
                    dev.ergenverse.simulation.intent.ActorEntityLink.syncPosition(a.id, level);

                    // Compute distance to nearest player (0..1, 1 = closest).
                    // MAX_IMPORTANCE_DISTANCE = 128 blocks (8 chunks).
                    // Within 16 blocks → distance = 1.0 (max).
                    // At 128+ blocks → distance = 0.0 (no contribution).
                    double nearestDistSq = Double.MAX_VALUE;
                    for (ServerPlayer p : players) {
                        double dx = a.blockX + 0.5 - p.getX();
                        double dz = a.blockZ + 0.5 - p.getZ();
                        double distSq = dx * dx + dz * dz;
                        if (distSq < nearestDistSq) nearestDistSq = distSq;
                    }
                    double distBlocks = Math.sqrt(nearestDistSq);
                    a.importance.distance = Math.max(0.0, 1.0 - (distBlocks / 128.0));

                    // Canon NPCs get a base story significance so they can
                    // reach FULL_COGNITION when near a player.
                    // Per Article XLI: no character special-casing — ALL
                    // canon NPCs get this, not just Wang Lin.
                    if (a.isCanon() && a.importance.storySignificance < 0.4) {
                        a.importance.storySignificance = 0.4;
                    }
                }
            }
        }

        boolean seasonalTick = (currentTick % SEASON_TICKS == 0);
        if (!seasonalTick) {
            // Non-seasonal: tick actors that are (a) flagged dirty by events, or
            // (b) linked + promoted to at least ACTIVE_ACTOR by player proximity.
            // Without (b), the cognition pipeline never fires for nearby NPCs.
            for (Actor a : ActorRegistry.all()) {
                boolean dirty = a.lastSimulatedTick == currentTick;
                boolean linkedAndActive = dev.ergenverse.simulation.intent.ActorEntityLink.isLinked(a.id)
                        && a.simLevel.order >= SimulationLevel.ACTIVE_ACTOR.order;
                if (dirty || linkedAndActive) {
                    tickActor(a, currentTick);
                }
            }
            return;
        }

        for (Actor a : ActorRegistry.all()) {
            tickActor(a, currentTick);
        }
    }

    private static void tickActor(Actor a, long currentTick) {
        a.recomputeSimLevel();

        switch (a.simLevel) {
            case STATIC_DATA:
            case HISTORICAL:
                // No sim. Existence is enough.
                return;
            case TERRITORY:
                tickTerritory(a, currentTick);
                break;
            case ACTIVE_ACTOR:
                tickActive(a, currentTick);
                break;
            case FULL_COGNITION:
            case STORY_IMPORTANCE:
                tickFullCognition(a, currentTick);
                break;
        }
        a.lastSimulatedTick = currentTick;
    }

    private static void tickTerritory(Actor a, long tick) {
        // Aggregated populations handled by CausalEcology; just record an event heartbeat.
        Ergenverse.LOGGER.debug("[Ergenverse] ActorTick[territory] {} @{}", a.id, tick);
    }

    private static void tickActive(Actor a, long tick) {
        // Process one goal step.
        for (Goal g : a.goals) {
            if (g.isExpired(tick)) g.abandon();
            if (g.isActive()) break;
            if (g.status == Goal.Status.PENDING) {
                g.activate();
                break;
            }
        }
        // Tick the actor's current activity process.
        tickActivity(a, tick);
    }

    private static void tickFullCognition(Actor a, long tick) {
        if (a.cognition == null) return;
        // Tick the actor's current activity process EVERY tick
        // (interruption checking, progress, reaction, resume).
        tickActivity(a, tick);

        // DecisionEngine: throttle to avoid hot-spinning.
        // Check using the seasonal tick gate — if this is a non-seasonal
        // tick AND the actor already has an active activity, skip the
        // full decision. This way, the decision fires on seasonal ticks
        // (every 7 MC days) and when the actor has no activity yet.
        boolean hasActiveActivity = a.currentActivity != null
                && !a.currentActivity.isComplete()
                && !a.currentActivity.isAbandoned();
        boolean seasonalTick = (tick % SEASON_TICKS == 0);
        if (!seasonalTick && hasActiveActivity) {
            return;
        }

        // ── ARTICLE XXXV COGNITION CHAIN ──
        // World -> Perception -> Attention -> Interpretation ->
        // Prediction -> Goals -> Intent -> Plan -> Tasks -> Activities -> Minecraft
        //
        // Before this wiring, the DecisionEngine generated goals purely from
        // internal NEEDS. A wolf 5 blocks from a meditating cultivator did
        // not change the cultivator's goal — because the cultivator never
        // PERCEIVED the wolf. This is the gap the user identified:
        // "Wang Lin doesn't immediately evaluate 'wolf.' He first notices
        //  it, then decides if it matters, then predicts what will happen,
        //  then decides whether to intervene."
        //
        // We now run the full chain each cognition pass:
        //   1. PERCEPTION  — PerceptionSensor.sense() builds a snapshot
        //   2. INTERPRETATION — Interpretation.interpret() classifies it
        //   3. DECISION    — DecisionEngine.decide() with interpretation context
        //   4. PREDICTION  — ActionPredictor.predict() on the chosen action
        //   5. INTENT      — IntentEngine.derive() the strategic framing
        //   6. ACTIVITY    — ActivityAssigner.assign() the concrete activity

        // ── Step 1: Perception ──
        PerceptionSnapshot perception = null;
        Interpretation interpretation = null;
        try {
            perception = PerceptionSensor.sense(a, currentServerLevel, tick);
            a.lastRawPerception = perception;
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] PerceptionSensor failed for {}: {}", a.id, e.toString());
        }

        // ── Step 1.5: Attention Filter ──
        // CRON-COMPLETIONIST-65: The user identified that "Wang Lin doesn't
        // immediately evaluate 'wolf.' He first notices it. Then decides
        // if it matters." This attention gate filters the raw perception
        // by salience threshold before interpretation. A Nascent Soul cultivator
        // ignores distant rabbits; a mortal notices everything.
        if (perception != null && a.cognition != null) {
            int realmOrder = a.cognition.cultivation != null ? a.cognition.cultivation.realmOrder() : 0;
            try {
                perception = AttentionFilter.attend(perception, realmOrder, a.cognition.personality);
                a.lastPerception = perception;
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] AttentionFilter failed for {}: {}", a.id, e.toString());
            }
        }

        // ── Step 2: Interpretation ──
        if (perception != null) {
            try {
                boolean isBeast = a.type == ActorType.BEAST;
                interpretation = Interpretation.interpret(perception, a.daoIdentity, isBeast);
                a.lastInterpretation = interpretation;
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] Interpretation failed for {}: {}", a.id, e.toString());
            }
        }

        // ── Step 3: Decision (needs + desires, now context-aware via interpretation) ──
        var needs = a.cognition.computeNeedIntensities();

        // If the interpretation suggests an urgent goal override (e.g. FLEE
        // from a THREAT_TO_LIFE), inject it as a high-urgency synthetic goal.
        // This is how a wolf appearing near Wang Lin interrupts his meditation:
        // the perception → interpretation chain produces a FLEE goal with
        // urgency 0.9+, which outranks the MEDITATE goal from his needs.
        if (interpretation != null
                && interpretation.suggestedGoalOverride != null
                && interpretation.urgency > 0.5) {
            CognitionGoal overrideGoal = new CognitionGoal(
                    null, interpretation.suggestedGoalOverride,
                    interpretation.summary, interpretation.urgency, 0.95);
            overrideGoal.status = CognitionGoal.Status.ACTIVE;
            a.cognition.activeGoal = overrideGoal;

            // Skip the normal DecisionEngine — the situation demands immediate action.
            assignAndDerive(a, overrideGoal, perception, interpretation, tick);
            logCognition(a, perception, interpretation, overrideGoal, null, tick);
            return;
        }

        var decision = DecisionEngine.decide(
                a.cognition.daoIdentity,
                needs,
                a.cognition.physical,
                a.cognition.cultivation,
                a.cognition.social,
                a.cognition.personality,
                interpretation != null ? interpretation.category.name() : "default",
                a.cognition.desires  // Art XXXI: desires produce SOCIAL goals
        );
        a.cognition.activeGoal = decision.goal;

        // ── Step 4 + 5 + 6: Prediction + Intent + Activity ──
        if (decision.goal != null) {
            decision.goal.status = CognitionGoal.Status.ACTIVE;

            // Prediction on the chosen action — does this action make sense
            // given what the actor just perceived? If the prediction is
            // catastrophically bad (e.g. MEDITATE while a wolf attacks),
            // the actor re-evaluates with FLEE forced.
            ActionPredictor.Outcome prediction = null;
            if (decision.chosen != null) {
                try {
                    prediction = ActionPredictor.predict(
                            decision.chosen, decision.goal, perception, interpretation,
                            a.cognition.personality);
                    a.lastPrediction = prediction;

                    // Catastrophe guard: if the chosen action has EV < -0.3 and
                    // there's a lethal threat, fall back to FLEE. This is the
                    // "prediction vetoes decision" mechanism — the mind
                    // simulating the future and refusing a suicidal action.
                    if (prediction.expectedValue < -0.3
                            && interpretation != null
                            && interpretation.category == Interpretation.Category.THREAT_TO_LIFE) {
                        CognitionGoal fleeGoal = new CognitionGoal(
                                null, CognitionGoal.Category.FLEE,
                                "Fleeing threat: " + interpretation.summary,
                                interpretation.urgency, 0.95);
                        fleeGoal.status = CognitionGoal.Status.ACTIVE;
                        a.cognition.activeGoal = fleeGoal;
                        assignAndDerive(a, fleeGoal, perception, interpretation, tick);
                        logCognition(a, perception, interpretation, fleeGoal, prediction, tick);
                        return;
                    }
                } catch (Exception e) {
                    Ergenverse.LOGGER.error("[Ergenverse] ActionPredictor failed for {}: {}", a.id, e.toString());
                }
            }

            assignAndDerive(a, decision.goal, perception, interpretation, tick);
            logCognition(a, perception, interpretation, decision.goal, prediction, tick);
        }
    }

    /** Assign the activity process and derive the intent for a chosen goal. */
    private static void assignAndDerive(Actor a, CognitionGoal goal,
                                         PerceptionSnapshot perception,
                                         Interpretation interpretation, long tick) {
        // Activity assignment (Article XLI).
        if (a.currentActivity == null || a.currentActivity.isComplete()
                || a.currentActivity.isAbandoned()) {
            dev.ergenverse.simulation.cognition.ActivityAssigner.assign(a, goal, tick);
        }

        // Intent derivation — the strategic "WHY" behind the action.
        try {
            var intent = dev.ergenverse.simulation.intent.IntentEngine.derive(
                    goal,
                    a.cognition.daoIdentity,
                    a.cognition.personality,
                    a.id,
                    a.blockX, a.blockZ,
                    tick
            );
            // CRON-COMPLETIONIST-14: Write directly to Ontology field.
            // The Actor-level duplicate (a.activeIntent) was removed.
            if (a.cognition != null) {
                a.cognition.activeIntent = intent;
            }

            if (intent != null) {
                // CRON-COMPLETIONIST-65: Wire IntentDecomposer into the tick loop.
                // Before this fix, IntentDecomposer existed but was never called.
                // The Intent was derived but never decomposed into concrete tasks.
                // Now we decompose the intent into a task queue that the
                // CognitionDrivenGoal executes step by step.
                List<CultivationTask> tasks = IntentDecomposer.decompose(
                        intent,
                        a.blockX, a.blockZ,
                        null, // nearestPlayerUuid — resolved later by CognitionDrivenGoal
                        999.0, // placeholder distance
                        null, // targetPos — derived from perception focus
                        tick
                );
                a.activeTasks.clear();
                a.activeTasks.addAll(tasks);
                a.currentTaskIndex = tasks.isEmpty() ? -1 : 0;

                Ergenverse.LOGGER.debug("[Ergenverse] ActorTick[cognition] {} intent: {} -> {} tasks",
                        a.id, intent.nature().label, tasks.size());

                // ── COMMITMENT FORMATION (CRON-COMPLETIONIST-12) ──
                // The bridge the user named: "No code path yet SETS
                // activeCommitment." This is that path. When the chosen goal
                // is commitment-worthy (a persistent decision, not a transient
                // reaction), form a Commitment with situation-derived success
                // and abandon predicates. Per the user:
                //   "Commitments should not simply expire. They should also be
                //    completed by conditions. The world should decide when a
                //    commitment ends. Not a timer. The timer is merely
                //    insurance against bugs."
                formCommitmentIfWarranted(a, goal, intent, perception, tick);
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[Ergenverse] IntentEngine failed for {}", a.id, e);
        }
    }

    /**
     * Form a {@link Commitment} for the actor if the chosen goal is
     * commitment-worthy and no commitment is currently active.
     *
     * <p>Per the user's design review: the bridge between Reasoning and
     * Commitment was missing — "No code path yet SETS activeCommitment."
     * This method IS that path. It runs after the IntentEngine derives an
     * intent. If the goal category is one that warrants persistence (the
     * actor is making a decision, not reacting to a transient stimulus),
     * a Commitment is built with success and abandon predicates derived
     * from the current perception.
     *
     * <h2>Which goals are commitment-worthy?</h2>
     * <p>Persistent decisions (the actor decides to do something and
     * should hold that course until the world says stop):
     * INVESTIGATE, DEFEND, DEFEND_TERRITORY, SEEKING_DAO, BREAKTHROUGH,
     * MEDITATE, STUDY, EXPLORE, KEEP_PROMISE, RESOLVE_DEBT, LEGACY.
     *
     * <p>Transient reactions (should NOT form commitments — they're
     * per-tick): FLEE, HIDE, SURVIVE, KILL, DECEIVE. These are reactive;
     * the actor should re-evaluate them every tick.
     *
     * <h2>Condition derivation</h2>
     * <p>The predicates are derived from the actor's perception:
     * <ul>
     *   <li><b>Success</b>: the threat the commitment addresses is gone
     *       (no hostile entities perceived, no threatening events). This
     *       is the "I achieved what I committed to" path — the wolves
     *       left, the herb was harvested, the promise was kept.</li>
     *   <li><b>Abandon</b>: danger exceeds tolerance (a hostile entity
     *       stronger than the actor comes within 12 blocks), or the
     *       commitment has outlived its safety-net max duration. This is
     *       the "the world changed; continuing is pointless/unsafe" path.</li>
     * </ul>
     *
     * <p>The safety-net max duration is set per category — long-lived
     * commitments (SEEKING_DAO, LEGACY) get a long safety net; short-lived
     * ones (INVESTIGATE) get a shorter one. Per the user: "The timer is
     * merely insurance against bugs."
     *
     * @param a the actor
     * @param goal the chosen cognition goal
     * @param intent the intent derived from the goal
     * @param perception the actor's current perception (may be null)
     * @param tick the current server tick
     */
    private static void formCommitmentIfWarranted(Actor a, CognitionGoal goal,
                                                    Intent intent,
                                                    PerceptionSnapshot perception,
                                                    long tick) {
        if (goal == null || intent == null) return;
        if (a.cognition == null) return;

        // If a commitment is already active, don't replace it. The user's
        // directive: "Intent can change without Commitment changing." A
        // new intent (per-tick flicker) does not override an active
        // commitment. The commitment ends only when the world says so.
        // CRON-COMPLETIONIST-14: Check Ontology field directly. The Actor-level
        // duplicate was removed. No sync needed.
        if (a.cognition.activeCommitment != null
                && a.cognition.activeCommitment.isActionable()) {
            return;
        }

        if (!isCommitmentWorthy(goal.category)) return;

        // Build the commitment with perception-derived predicates.
        IntentNature nature = intent.nature();
        String targetId = intent.targetId() != null ? intent.targetId()
                : (goal.description != null ? goal.description : goal.category.name());
        long maxDuration = safetyNetDurationFor(goal.category);

        // ── Success condition 1: belief that the threat is addressed ──
        // CRON-COMPLETIONIST-20: Reframed from environmental to cognitive per
        // the user's 2026-07-25 design review: "The predicates shouldn't
        // describe the world. Instead of `success: wolves disappeared`,
        // think `success: confidence that hunting pattern understood >= 0.85`.
        //
        // BEFORE: pure environmental check — !hasThreat && no hostiles.
        // AFTER: cognitive check — the actor BELIEVES the threat is gone
        // because (a) no hostiles are currently perceived, AND (b) the
        // actor's MemoryGraph contains memories about the target that
        // indicate threat retreat ("wolf retreated", "pack dispersed", etc.).
        // This means two cultivators observing the same scene could reach
        // different conclusions: one has more memories about retreat
        // patterns and believes the threat is addressed, while the other
        // (fewer memories, less confident) continues observing.
        //
        // The 200-tick minimum remains — an actor shouldn't declare victory
        // instantly. But the completion now reflects the ACTOR'S UNDERSTANDING,
        // not the world state.
        CompletionPredicate successThreatGone = ctx -> {
            if (ctx.perception() == null) return false;
            if (ctx.currentTick() - tick < 200L) return false; // let it breathe
            // Environmental check: no hostiles currently perceived
            boolean noCurrentThreat = !ctx.perception().hasThreat
                    && ctx.perception().nearbyEntities.stream()
                        .noneMatch(e -> "hostile".equals(e.classification));
            if (!noCurrentThreat) return false;
            // Cognitive layer: the actor must BELIEVE the threat is addressed.
            // Check MemoryGraph for retreat/disappear memories about the target.
            if (ctx.actor() == null || ctx.actor().cognition == null) return true;
            var mem = ctx.actor().cognition.memory;
            if (mem == null) return true; // no memory → can't confirm belief,
                                         // but threat is gone so OK to complete
            java.util.List<MemoryGraph.MemoryNode> memories =
                    mem.aboutKeywords(targetId);
            // Count "retreat" / "disappear" / "fled" memories as evidence of
            // threat resolution. These are OBSERVED memories with moderate
            // strength — the actor SAW the wolves leaving.
            long retreatMemories = memories.stream()
                    .filter(n -> n.type != MemoryGraph.Type.INFERRED)
                    .filter(n -> n.strength >= 0.2)
                    .filter(n -> n.subject != null && (
                            n.subject.toLowerCase().contains("retreat") ||
                            n.subject.toLowerCase().contains("disappear") ||
                            n.subject.toLowerCase().contains("fled") ||
                            n.subject.toLowerCase().contains("dispersed") ||
                            n.subject.toLowerCase().contains("driven off")))
                    .count();
            // One retreat memory is enough to confirm the actor's belief
            // that the threat has been addressed. Without it, the actor
            // continues observing — the wolves might come back.
            return retreatMemories >= 1;
        };

        // ── Success condition 2: understood pattern ──
        // CRON-COMPLETIONIST-16: Fixed two bugs from CRON-COMPLETIONIST-15:
        //   (a) Subject mismatch: targetId may be "wolves near village" while
        //       MemoryGraph memories are stored under "wolf" or "hostile_creature".
        //       Now uses aboutKeywords() for fuzzy matching instead of exact-match
        //       about(). This extracts individual words and matches them against
        //       memory subjects bidirectionally (subject-contains-keyword OR
        //       keyword-contains-subject), with stop-word filtering.
        //   (b) Double-counting: the old code iterated mem.about(subject) twice.
        //       An INFERRED memory with strength >= 0.3 was counted in BOTH loops.
        //       Now iterates once, counting OBSERVED/PARTICIPATED at >= 0.3 and
        //       INFERRED at >= 0.2 in a single pass, with each node counted once.
        CompletionPredicate successPatternUnderstood = ctx -> {
            if (ctx.actor() == null) return false;
            if (ctx.actor().cognition == null) return false;
            if (goal.category != CognitionGoal.Category.INVESTIGATE) return false;
            if (ctx.currentTick() - tick < 400L) return false; // need time to learn
            var mem = ctx.actor().cognition.memory;
            if (mem == null) return false;

            // Fuzzy keyword search: matches "wolves near village" against "wolf",
            // "hostile_creature", "village_events", etc.
            java.util.List<MemoryGraph.MemoryNode> candidates = mem.aboutKeywords(targetId);
            if (candidates.isEmpty()) return false;

            int strongMemories = 0;
            for (MemoryGraph.MemoryNode node : candidates) {
                if (node.type == MemoryGraph.Type.INFERRED) {
                    // INFERRED memories count at a lower threshold — they represent
                    // the actor's synthesis of observed patterns.
                    if (node.strength >= 0.2) strongMemories++;
                } else {
                    // OBSERVED, PARTICIPATED, HEARD, SOUL_SEARCHED, etc.
                    if (node.strength >= 0.3) strongMemories++;
                }
            }
            return strongMemories >= 3;
        };

        // ── Abandon condition 1: danger exceeds tolerance ──
        // "The world changed; continuing is unsafe." A hostile entity
        // stronger than the actor has come within 12 blocks. This is the
        // "danger exceeds tolerance" path from the user's example.
        CompletionPredicate abandonDanger = ctx -> {
            if (ctx.perception() == null) return false;
            return ctx.perception().nearbyEntities.stream()
                    .filter(e -> "hostile".equals(e.classification))
                    .anyMatch(e -> e.relativePower > 0.5 && e.distanceBlocks < 12.0);
        };

        // ── Abandon condition 2: belief that further observation is futile ──
        // CRON-COMPLETIONIST-20: Reframed from "target gone" to cognitive.
        // BEFORE: pure environmental check — no threats/prey for 400 ticks.
        // AFTER: "belief: further observation unlikely to increase understanding."
        // The actor checks: (a) no threats are currently perceived, AND
        // (b) no NEW memories about the target have formed in the last 200
        // ticks. If the actor hasn't learned anything new recently,
        // continued observation is unlikely to yield new understanding —
        // the commitment has reached diminishing returns.
        //
        // This is the user's design review: "Instead of `target gone`, think
        // `belief: further observation unlikely to increase understanding`."
        // Two cultivators observing the same empty field: one with rich
        // memories (understood pattern) might complete via successPattern-
        // Understood. The other (no new memories, no understanding gained)
        // abandons here — the observation yielded nothing.
        CompletionPredicate abandonTargetGone = ctx -> {
            if (ctx.perception() == null) return false;
            if (ctx.currentTick() - tick < 400L) return false; // grace period
            // Environmental: no threats currently perceived
            boolean noTarget = !ctx.perception().hasThreat
                    && ctx.perception().nearbyEntities.stream()
                        .noneMatch(e -> "hostile".equals(e.classification)
                                || "prey".equals(e.classification));
            if (!noTarget) return false;
            // Cognitive: has the actor learned anything new recently?
            // Check MemoryGraph for memories with timestamp >= (tick - 200).
            // If no new memories formed in the last 200 ticks, the actor
            // believes continued observation won't increase understanding.
            if (ctx.actor() == null || ctx.actor().cognition == null) return true;
            var mem = ctx.actor().cognition.memory;
            if (mem == null) return true; // no memory → no evidence of learning
            java.util.List<MemoryGraph.MemoryNode> recentMemories =
                    mem.aboutKeywords(targetId);
            long recentNew = recentMemories.stream()
                    .filter(n -> n.tick >= tick + 200L - 200L) // last 200 ticks
                    .count();
            // If no new memories formed, the observation is yielding nothing.
            // The actor believes: "I've been staring at empty space for
            // 10 seconds. Nothing new to learn here."
            return recentNew == 0;
        };

        // ── Abandon condition 3: a HIGHER-PRIORITY commitment demands attention ──
        // CRON-COMPLETIONIST-20: Reframed from "ally nearby" to priority-based
        // interruption per the user's 2026-07-25 design review:
        //   "Wang Lin shouldn't abandon observation because someone exists nearby.
        //    He abandons because another commitment temporarily outranks
        //    the current one."
        //   "Current commitment (52). New pressure: family member attacked (97).
        //    The previous commitment is interrupted because something more
        //    important emerged."
        //
        // BEFORE: any hostile within 6 blocks of any ally → abandon.
        //   Problem: a weak wolf 5 blocks from a villager triggers this,
        //   even if the actor's observation commitment is important and the
        //   wolf isn't actually attacking.
        //
        // AFTER: a HOSTILE is within IMMEDIATE striking distance (3 blocks)
        //   of an ally AND the hostile is STRONGER than the ally. This is
        //   "family member about to be struck" — a genuine emergency — not
        //   "ally happened to exist within 6 blocks." The distinction is
        //   critical: an observation commitment persists through ambient
        //   danger (weak wolves patrolling far from villagers) but yields to
        //   imminent violence.
        //
        // The distance threshold is reduced from 6.0 to 3.0 because 6 blocks
        //   is "nearby" (the user's criticism) while 3 blocks is "about to be
        //   struck" (imminent). The power threshold is raised from 0.3 to 0.5
        // because a weak beast near an ally is concerning but not yet an
        // emergency requiring commitment interruption.
        //
        // CRON-COMPLETIONIST-15's entity-to-entity distance fix is preserved.
        CompletionPredicate abandonFamilyNeeds = ctx -> {
            if (ctx.perception() == null) return false;
            if (!ctx.perception().hasThreat) return false;
            java.util.List<PerceptionSnapshot.PerceivedEntity> entities =
                    ctx.perception().nearbyEntities;
            // Find all hostiles
            java.util.List<PerceptionSnapshot.PerceivedEntity> hostiles =
                    entities.stream()
                            .filter(e -> "hostile".equals(e.classification))
                            .toList();
            if (hostiles.isEmpty()) return false;
            // Find all allies/witnesses
            java.util.List<PerceptionSnapshot.PerceivedEntity> allies =
                    entities.stream()
                            .filter(e -> "witness".equals(e.classification)
                                    || "ally".equals(e.classification))
                            .toList();
            if (allies.isEmpty()) return false;
            // CRON-COMPLETIONIST-20: Only abandon if a hostile is within
            // IMMEDIATE striking distance (3 blocks) of an ally AND is strong
            // enough to pose genuine threat. This is the priority-based
            // interruption: "Current commitment priority=52, family defense
            // priority=97 → interrupt."
            // 3.0 blocks = the distance at which a hostile is actively
            // attacking (not just patrol-distance). 0.5 relativePower = the
            // hostile is stronger than the ally (meaningful power differential).
            final double imminentStrikeDist = 3.0;
            final double relativePowerThreshold = 0.5;
            for (PerceptionSnapshot.PerceivedEntity hostile : hostiles) {
                if (hostile.relativePower < relativePowerThreshold) continue;
                for (PerceptionSnapshot.PerceivedEntity ally : allies) {
                    if (hostile.isWithin(ally, imminentStrikeDist)) {
                        return true;
                    }
                }
            }
            return false;
        };

        String reasonText = a.displayName + " commits to " + nature.label
                + " (" + goal.category + "): " + goal.description;

        Commitment commitment = Commitment.builder(nature, targetId, goal)
                .reason(reasonText)
                .maxDuration(maxDuration)
                .successDescription("Threat addressed; no hostiles perceived for 200+ ticks, or pattern understood.")
                .successWhen(successThreatGone)
                .successWhen(successPatternUnderstood)
                .abandonWhen(abandonDanger)
                .abandonWhen(abandonTargetGone)
                .abandonWhen(abandonFamilyNeeds)
                .form(tick);

        // CRON-COMPLETIONIST-14: Write directly to Ontology field.
        a.cognition.activeCommitment = commitment;

        Ergenverse.LOGGER.info("[Ergenverse] ActorTick[commitment] {} FORMED: {} → {} "
                        + "(success={}, abandon={}, maxDur={}t)",
                a.id, nature.label, targetId,
                commitment.successConditions.size(),
                commitment.abandonConditions.size(),
                maxDuration);
    }

    /**
     * Is this goal category commitment-worthy? Persistent decisions warrant
     * commitments; transient reactions do not.
     */
    private static boolean isCommitmentWorthy(CognitionGoal.Category category) {
        return switch (category) {
            case INVESTIGATE, DEFEND, DEFEND_TERRITORY, SEEKING_DAO,
                 BREAKTHROUGH, MEDITATE, STUDY, EXPLORE, KEEP_PROMISE,
                 RESOLVE_DEBT, LEGACY, CRAFT, TRADE, OFFER_FAVOR -> true;
            // Transient reactions — re-evaluate every tick, don't persist.
            case FLEE, HIDE, SURVIVE, KILL, DECEIVE, CORRUPT, POLITICS,
                 CALL_HELP, SUBMIT, FORGIVE, RESURRECT, WAIT, OTHER,
                 SOCIAL, GATHER_RESOURCE, BREAK_FORMATION -> false;
        };
    }

    /**
     * Safety-net max duration per goal category. Per the user: "The timer
     * is merely insurance against bugs." These durations are the backstop —
     * the primary lifecycle is condition-driven.
     */
    private static long safetyNetDurationFor(CognitionGoal.Category category) {
        return switch (category) {
            case SEEKING_DAO, LEGACY, BREAKTHROUGH -> 240000L;  // ~3.3 hours real-time
            case MEDITATE, STUDY, CRAFT -> 120000L;             // ~1.7 hours
            case DEFEND, DEFEND_TERRITORY, KEEP_PROMISE,
                 RESOLVE_DEBT, TRADE, OFFER_FAVOR -> 60000L;    // ~50 min
            case INVESTIGATE, EXPLORE -> 24000L;                // ~20 min
            default -> 12000L;                                   // ~10 min
        };
    }

    /** One-line debug log of the full cognition chain for this tick. */
    private static void logCognition(Actor a, PerceptionSnapshot perception,
                                      Interpretation interpretation, CognitionGoal goal,
                                      ActionPredictor.Outcome prediction, long tick) {
        if (perception == null) return;
        Ergenverse.LOGGER.debug("[Ergenverse] Cognition[{}] t={}: {} -> {} -> {}{}",
                a.id, tick,
                perception,
                interpretation == null ? "no-interp" : interpretation,
                goal == null ? "no-goal" : goal.category,
                prediction == null ? "" : " -> " + prediction);
    }

    /**
     * Tick the actor's current ActivityProcess.
     *
     * <p>Per Article XLI: activity is a process, not a state.
     * This method advances the process through its lifecycle:
     *   IN_PROGRESS → advance progress
     *   REACTING → count down reaction timer, then begin RESUMING
     *   RESUMING → restore progress, transition to IN_PROGRESS
     *
     * <p>INTERRUPTED state is set by {@link dev.ergenverse.simulation.event.ActivityInterruptionSubscriber}
     * when a WorldEventBus event matches an activity's interruption conditions.
     */
    private static void tickActivity(Actor a, long tick) {
        ActivityProcess ap = a.currentActivity;
        if (ap == null) return;

        switch (ap.state) {
            case ActivityProcess.STATE_IN_PROGRESS:
                // Advance progress. Rate is ~1% per 20 ticks (6.7 seconds at 20 tps).
                // A full meditation session takes ~2000 ticks (~100 seconds).
                ap.progress = Math.min(1.0f, ap.progress + 0.0005f);
                if (ap.progress >= 1.0f) {
                    ap.complete();
                    // Art XXXI.5: NPCs remember what they did.
                    // Activity completion creates a memory so the world
                    // can reference past actions.
                    recordActivityMemory(a, ap, tick);
                    Ergenverse.LOGGER.debug("[Ergenverse] Activity complete: {} for {}",
                            ap.activityType, a.id);
                }
                break;

            case ActivityProcess.STATE_INTERRUPTED:
                // Transitions to REACTING immediately (the subscriber set the reaction).
                ap.beginReaction(tick);
                break;

            case ActivityProcess.STATE_REACTING:
                ap.reactionTicksRemaining--;
                if (ap.reactionTicksRemaining <= 0) {
                    // Reaction over. For now, always resume. A more sophisticated
                    // version would check the InterruptionCondition.resumeAfter flag.
                    ap.beginResume(tick);
                }
                break;

            case ActivityProcess.STATE_RESUMING:
                // Restore progress and return to IN_PROGRESS.
                ap.progress = ap.progressAtInterruption;
                ap.state = ActivityProcess.STATE_IN_PROGRESS;
                Ergenverse.LOGGER.debug("[Ergenverse] Activity resumed: {} for {} at progress {}",
                        ap.activityType, a.id, ap.progress);
                break;

            case ActivityProcess.STATE_COMPLETED:
            case ActivityProcess.STATE_ABANDONED:
                // Activity is done. Clear it so the DecisionEngine can assign a new one.
                a.currentActivity = null;
                break;

            default:
                // NOT_STARTED, STARTING — no action needed. The entity AI handles start.
                break;
        }
    }

    // ── Memory recording (Art XXXI.5) ──

    /**
     * Record a memory when an NPC completes an activity.
     * Social activities → SOCIAL memory. Meditation → EMOTIONAL.
     * Other activities → OBSERVATION. Tier: medium-term (weeks).
     *
     * <p>This is the other half of the Memory Metric bridge:
     * MemoryEventSubscriber handles world events → memories.
     * This handles NPC actions → memories. Together, the world
     * remembers both what happened TO it and what its inhabitants DID.
     */
    private static void recordActivityMemory(Actor a, ActivityProcess ap, long tick) {
        if (ap.activityType == null) return;

        NpcCognitiveMemory.MemoryCategory category;
        String desc;

        String act = ap.activityType.toLowerCase();
        if (act.contains("social") || act.contains("trade") || act.contains("gift")) {
            category = NpcCognitiveMemory.MemoryCategory.SOCIAL;
            desc = "Completed: " + ap.activityType;
        } else if (act.contains("meditat") || act.contains("cultivat") || act.contains("breath")) {
            category = NpcCognitiveMemory.MemoryCategory.EMOTIONAL;
            desc = "Finished meditating — mind is calm";
        } else if (act.contains("combat") || act.contains("fight") || act.contains("hunt")) {
            category = NpcCognitiveMemory.MemoryCategory.COMBAT;
            desc = "Finished: " + ap.activityType;
        } else {
            category = NpcCognitiveMemory.MemoryCategory.OBSERVATION;
            desc = "Completed: " + ap.activityType;
        }

        NpcMemoryTickHandler.recordMediumTerm(a.id, category, desc, "self", tick);
    }

    /** Mark an actor dirty for event-driven re-tick this pass. */
    public static void markDirty(Actor a, long currentTick) {
        a.lastSimulatedTick = currentTick; // will be picked up by tick()
    }

    /** Promote an actor's sim level temporarily (player proximity). */
    public static void promoteTemporarily(Actor a, SimulationLevel floor) {
        if (a.simLevel.order < floor.order) {
            a.simLevel = floor;
        }
    }
}
