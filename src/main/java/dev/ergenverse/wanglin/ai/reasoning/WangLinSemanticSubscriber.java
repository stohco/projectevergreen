package dev.ergenverse.wanglin.ai.reasoning;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.RelationshipHistory;
import dev.ergenverse.simulation.action.ActorRelationshipStore;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * WangLinSemanticSubscriber — wires Wang Lin's cognition to the WorldEventBus.
 *
 * <p>Per the user's directive (2026-07-23, option f):
 * <pre>
 *   "Wire WangLinReasoningEngine to subscribe to semantic events
 *    (act_of_mercy, cultivation_revealed, promise_broken) so Wang Lin's
 *    opinion updates from MEANING, not just actions."
 * </pre>
 *
 * <p>Before this subscriber, Wang Lin's reasoning engine was a pull-only system:
 * the player asks for a gift, the engine evaluates, and that's it. Wang Lin
 * never independently <i>observed</i> the player's deeds. If the player saved
 * Wang Ping, showed mercy to a defeated enemy, or revealed cultivation
 * recklessly, Wang Lin had no mechanism to notice.
 *
 * <p>Now, every semantic event on the bus is observed by Wang Lin's cognition.
 * His opinion shifts silently — the player doesn't get a notification. But the
 * next time they ask for a gift, Wang Lin's answer reflects everything he's
 * witnessed.
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li>Subscribes to topic prefix {@code "semantic."} — all meaning-classified
 *       events.</li>
 *   <li>Filters for events where Wang Lin is the <b>target</b> (someone acted
 *       toward/around Wang Lin) OR where Wang Lin is a <b>witness</b> (the
 *       event happened within his observation radius and carries sufficient
 *       severity).</li>
 *   <li>Updates the player's ExpectationModel with new predictions derived
 *       from the observed event. E.g.:
 *       <ul>
 *         <li>ACT_OF_MERCY witnessed → updates "compassionate_nature" prediction.</li>
 *         <li>ACT_OF_CRUELTY witnessed → updates "ruthless_nature" prediction.</li>
 *         <li>PROMISE_BROKEN witnessed → updates "unreliable" prediction.</li>
 *         <li>TECHNIQUE_DISPLAYED witnessed → updates "cultivation_talent" prediction.</li>
 *         <li>CULTIVATION_REVEALED witnessed → updates "reckless_with_power" prediction.</li>
 *       </ul>
 *   </li>
 *   <li>Updates RelationshipHistory so that the player's standing with Wang Lin
 *       reflects the cumulative observation.</li>
 * </ul>
 *
 * <h2>Wang Lin's observation radius</h2>
 * <p>Wang Lin is a Nascent Soul+ cultivator (E28+). His spiritual sense extends
 * far. In gameplay terms, he "witnesses" events within 128 blocks (4 chunks).
 * Lower-realm NPCs have smaller radii; Wang Lin's is the largest.
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article X — Intelligence Is Reasoning:</b> Wang Lin doesn't react
 *       to scripts. He observes facts and updates his internal model.</li>
 *   <li><b>Article XXIV — NPCs Must Initiate Gameplay:</b> Wang Lin's opinion
 *       change is silent. The player discovers it through Wang Lin's next
 *       interaction — a warmer greeting, a colder refusal, an unexpected offer.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> WorldEventSubscriber that updates
 *       existing systems. No new bus, no new infrastructure.</li>
 * </ul>
 */
public final class WangLinSemanticSubscriber implements WorldEventSubscriber {

    /** Wang Lin's canon actor ID. */
    private static final String WANG_LIN_ID = "wang_lin";

    /** Wang Lin's spiritual sense observation radius (blocks). */
    private static final int OBSERVATION_RADIUS = 128;

    /** Minimum severity for Wang Lin to take notice. Trivial events are noise. */
    private static final float MIN_SEVERITY = 0.25f;

    @Override
    public String topicPrefix() {
        return "semantic.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        float severity = event.severity();
        if (severity < MIN_SEVERITY) return;

        String sourceId = event.sourceActorId();
        String targetId = event.targetActorId();
        String semanticTag = event.semanticTag();

        // Wang Lin must be involved (as target or as nearby witness).
        if (!isWangLinInvolved(event)) return;

        // Resolve the other actor (the one who isn't Wang Lin).
        String otherActorId = resolveOtherActor(sourceId, targetId);
        if (otherActorId == null || otherActorId.isEmpty()) return;

        // Check if the other actor is a player.
        ServerPlayer player = resolvePlayer(otherActorId);
        boolean isPlayer = player != null;

        // Update Wang Lin's internal model of this actor.
        updateWangLinOpinion(event, otherActorId, player, semanticTag);
    }

    /**
     * Check if Wang Lin is involved in this event — either as target,
     * as source, or as a nearby witness.
     */
    private boolean isWangLinInvolved(WorldEvent event) {
        String sourceId = event.sourceActorId();
        String targetId = event.targetActorId();

        // Direct involvement: Wang Lin is source or target.
        if (WANG_LIN_ID.equals(sourceId) || WANG_LIN_ID.equals(targetId)) return true;

        // Witness: Wang Lin entity exists within observation radius.
        // This is a simplified check — in the full system, we'd query
        // ActorEntityLink for Wang Lin's actual entity position.
        // For now, we use the event position as a proxy.
        // The EntityCultivator with canon ID "wang_lin" would be checked.
        ServerLevel level = dev.ergenverse.simulation.event.WorldEventBus.currentLevel();
        if (level == null) return false;

        // Events with pos (0,0,0) are global — Wang Lin always witnesses those.
        if (event.pos().equals(net.minecraft.core.BlockPos.ZERO)) return event.severity() >= 0.5f;

        // Check distance: if Wang Lin's entity is within observation radius.
        // We search for an EntityCultivator linked to wang_lin.
        var actors = dev.ergenverse.simulation.actor.ActorRegistry.all();
        for (var actor : actors) {
            if (WANG_LIN_ID.equals(actor.id)) {
                double distSq = Math.pow(actor.blockX - event.pos().getX(), 2)
                              + Math.pow(actor.blockZ - event.pos().getZ(), 2);
                return distSq <= (double) OBSERVATION_RADIUS * OBSERVATION_RADIUS;
            }
        }
        return false;
    }

    /**
     * Resolve the actor that is NOT Wang Lin. If both source and target
     * are Wang Lin (shouldn't happen), returns null.
     */
    private String resolveOtherActor(String sourceId, String targetId) {
        if (WANG_LIN_ID.equals(sourceId)) return targetId;
        if (WANG_LIN_ID.equals(targetId)) return sourceId;
        // Neither is Wang Lin — this is a witnessed event.
        // Return the source (the actor who performed the deed).
        return sourceId;
    }

    /**
     * Update Wang Lin's internal model based on the observed event.
     * This modifies the player's ExpectationModel and RelationshipHistory.
     */
    private void updateWangLinOpinion(WorldEvent event, String otherActorId,
                                        ServerPlayer player, String semanticTag) {
        // ── 1. Update the ExpectationModel with new predictions ──
        if (player != null) {
            updateExpectationModel(player, event, semanticTag);
        }

        // ── 2. Update RelationshipHistory (Wang Lin → other actor) ──
        if (player != null) {
            updateRelationshipHistory(player, event, semanticTag);
        }

        // ── 3. Update ActorRelationshipStore (6-axis per Article XXXIV) ──
        // CRON-COMPLETIONIST-47: Previously, Wang Lin's semantic subscriber only
        // wrote to RelationshipHistory but NOT to ActorRelationshipStore. This meant
        // Wang Lin's seeded 6-axis relationships (trust, respect, fear, familiarity,
        // debt, grievance) from CanonRelationshipSeeder never evolved from observed
        // events. Now, every semantic event updates the multi-axis relationship store,
        // so Wang Lin's opinions actually change based on what he witnesses.
        updateActorRelationshipStore(event, otherActorId, semanticTag);

        Ergenverse.LOGGER.debug("[WangLinSemantic] Observed: {} by {} → opinion updated",
                semanticTag, otherActorId);
    }

    /**
     * Update the player's ExpectationModel with predictions derived from
     * the semantic event. These predictions influence Wang Lin's 6-factor
     * JUDGMENT score the next time the player makes a request.
     */
    private void updateExpectationModel(ServerPlayer player, WorldEvent event,
                                         String semanticTag) {
        ExpectationModel model = ExpectationModelObserver.getOrCreate(player);
        long tick = player.level().getGameTime();
        String evidence = "Observed: " + event.description();

        switch (semanticTag) {
            case "ACT_OF_MERCY" -> {
                // Player showed mercy → Wang Lin judges character.
                // Canon: Wang Lin respects those who show mercy to the weak,
                // but despises mercy shown to the treacherous.
                double confidence = 0.4 + event.severity() * 0.3;
                model.updatePrediction("compassionate_nature",
                        "Has shown mercy to others", confidence, tick, evidence);
                // Also update "worthy_of_teaching" — mercy is a canon virtue.
                model.updatePrediction("worthy_of_teaching",
                        "Demonstrates compassion", confidence * 0.7, tick, evidence);
            }
            case "ACT_OF_CRUELTY" -> {
                // Player was cruel → Wang Lin notes ruthlessness.
                // Canon: Wang Lin understands ruthlessness (Slaughter Dao) but
                // judges whether it was justified or senseless.
                double confidence = 0.4 + event.severity() * 0.3;
                String target = event.targetActorId();
                boolean targetIsInnocent = isInnocent(target);
                if (targetIsInnocent) {
                    // Cruelty to innocents: negative judgment.
                    model.updatePrediction("ruthless_toward_innocents",
                            "Has harmed innocents", confidence, tick, evidence);
                    model.updatePrediction("worthy_of_teaching",
                            "Harms innocents — teaching risky", 0.6, tick, evidence);
                } else {
                    // Cruelty to enemies: Wang Lin understands.
                    model.updatePrediction("capable_of_ruthlessness",
                            "Ruthless against enemies", confidence, tick, evidence);
                }
            }
            case "PROMISE_MADE" -> {
                // Player made a promise → reliability indicator.
                double confidence = 0.3 + event.severity() * 0.2;
                model.updatePrediction("honors_commitments",
                        "Made a promise", confidence, tick, evidence);
            }
            case "PROMISE_BROKEN" -> {
                // Player broke a promise → trust violation.
                double confidence = 0.5 + event.severity() * 0.3;
                model.updatePrediction("breaks_promises",
                        "Broke a promise", confidence, tick, evidence);
                model.updatePrediction("worthy_of_teaching",
                        "Unreliable — broke commitment", confidence * 0.5, tick, evidence);
            }
            case "TECHNIQUE_DISPLAYED" -> {
                // Player showed cultivation ability → talent assessment.
                double confidence = 0.3 + event.severity() * 0.3;
                model.updatePrediction("cultivation_talent",
                        "Displayed cultivation technique", confidence, tick, evidence);
            }
            case "CULTIVATION_REVEALED" -> {
                // Player revealed cultivation level → recklessness check.
                double confidence = 0.3 + event.severity() * 0.2;
                model.updatePrediction("reckless_with_power",
                        "Revealed cultivation publicly", confidence, tick, evidence);
                // Also note the realm if available.
                String realm = event.meta("realm", "");
                if (!realm.isEmpty()) {
                    model.updatePrediction("observed_realm_" + realm.toLowerCase(),
                            "Cultivation realm observed: " + realm, confidence, tick, evidence);
                }
            }
            case "GIFT_GIVEN", "GIFT_RECEIVED" -> {
                // Player engaged in trade/gift — generosity or pragmatism.
                double confidence = 0.2 + event.severity() * 0.15;
                model.updatePrediction("generous_nature",
                        "Engaged in gift-giving", confidence, tick, evidence);
            }
            case "COMBAT_ENGAGED" -> {
                // Player fought — courage indicator.
                String outcome = event.meta("outcome", "");
                double confidence = 0.25 + event.severity() * 0.2;
                if ("VICTORY".equals(outcome) || "player_won".equals(outcome)) {
                    model.updatePrediction("combat_capable",
                            "Won a fight", confidence, tick, evidence);
                } else {
                    model.updatePrediction("will_challenge_stronger",
                            "Engaged in combat", confidence * 0.6, tick, evidence);
                }
            }
            case "DEBT_REPAID" -> {
                double confidence = 0.35 + event.severity() * 0.2;
                model.updatePrediction("honors_debts",
                        "Repaid a debt", confidence, tick, evidence);
            }
            case "DEBT_IGNORED" -> {
                double confidence = 0.4 + event.severity() * 0.3;
                model.updatePrediction("ignores_debts",
                        "Ignored a debt obligation", confidence, tick, evidence);
            }
            case "PUBLIC_HUMILIATION" -> {
                double confidence = 0.3 + event.severity() * 0.2;
                model.updatePrediction("cruel_to_reputation",
                        "Publicly humiliated another", confidence, tick, evidence);
            }
            case "FORBIDDEN_KNOWLEDGE_WITNESSED" -> {
                // Canon: Wang Lin respects those who seek knowledge,
                // but warns against dangerous paths.
                double confidence = 0.25 + event.severity() * 0.2;
                model.updatePrediction("seeks_forbidden_knowledge",
                        "Witnessed forbidden knowledge", confidence, tick, evidence);
            }
            case "EXPECTATION_VIOLATION" -> {
                // The ExpectationObserver published this — pattern was broken.
                double confidence = 0.4 + event.severity() * 0.2;
                String violatedExpectation = event.meta("expected_action", "");
                model.updatePrediction("pattern_broken",
                        "Broke pattern of " + violatedExpectation, confidence, tick, evidence);
            }
            default -> {
                // Unknown semantic tag — no prediction update.
            }
        }

        // Persist the updated model.
        ExpectationModelObserver.save(player, model);
    }

    /**
     * Update RelationshipHistory: Wang Lin's affinity toward the player.
     * This is the mechanism by which Wang Lin's opinion permanently changes
     * based on observed events.
     */
    private void updateRelationshipHistory(ServerPlayer player, WorldEvent event,
                                            String semanticTag) {
        int affinityDelta = computeWangLinAffinityDelta(event, semanticTag);
        if (affinityDelta == 0) return;

        String reason = "Wang Lin observed: " + event.description();
        try {
            if (affinityDelta > 0) {
                RelationshipHistory.recordAffinityEarned(player, WANG_LIN_ID,
                        reason, affinityDelta, event.timestamp());
            } else {
                RelationshipHistory.recordTrustBroken(player, WANG_LIN_ID,
                        reason, event.timestamp());
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[WangLinSemantic] Failed to record affinity: {}", e.getMessage());
        }
    }

    /**
     * Compute how this event changes Wang Lin's affinity toward the player.
     * These are Wang-Lin-specific weights — not generic RelationshipEngine
     * deltas. Wang Lin values different things than a random NPC.
     *
     * <ul>
     *   <li>Mercy: +8 (Wang Lin values compassion — his parents were killed)</li>
     *   <li>Cruelty to innocents: -15 (deeply personal — Teng Clan parallel)</li>
     *   <li>Cruelty to enemies: +3 (Wang Lin respects ruthlessness when justified)</li>
     *   <li>Promise made: +5 (Wang Lin honors commitments)</li>
     *   <li>Promise broken: -18 (Wang Lin despises betrayal — Situ Nan parallel)</li>
     *   <li>Debt repaid: +10 (filial/piety — Art XLIII memory)</li>
     *   <li>Debt ignored: -12 (violates Wang Lin's core values)</li>
     *   <li>Technique displayed: +4 (Wang Lin appreciates cultivation talent)</li>
     *   <li>Cultivation revealed: -3 (Wang Lin believes in concealment)</li>
     *   <li>Gift given/received: +2 (neutral-positive)</li>
     *   <li>Public humiliation: -8 (Wang Lin understands power but not cruelty for sport)</li>
     * </ul>
     */
    private int computeWangLinAffinityDelta(WorldEvent event, String semanticTag) {
        return switch (semanticTag) {
            case "ACT_OF_MERCY" -> 8;
            case "ACT_OF_CRUELTY" -> {
                String target = event.targetActorId();
                yield isInnocent(target) ? -15 : 3;
            }
            case "PROMISE_MADE" -> 5;
            case "PROMISE_BROKEN" -> -18;
            case "DEBT_REPAID" -> 10;
            case "DEBT_IGNORED" -> -12;
            case "TECHNIQUE_DISPLAYED" -> 4;
            case "CULTIVATION_REVEALED" -> -3;
            case "GIFT_GIVEN", "GIFT_RECEIVED" -> 2;
            case "PUBLIC_HUMILIATION" -> -8;
            case "COMBAT_ENGAGED" -> {
                String outcome = event.meta("outcome", "");
                yield "VICTORY".equals(outcome) || "player_won".equals(outcome) ? 2 : -2;
            }
            case "FORBIDDEN_KNOWLEDGE_WITNESSED" -> 1; // curiosity is neutral-positive
            case "EXPECTATION_VIOLATION" -> -5; // Wang Lin values consistency
            default -> 0;
        };
    }

    /**
     * Heuristic: is the target likely an "innocent" (mortal, child, non-combatant)?
     * In the full system, this would query the target's Actor type and capabilities.
     * For now, we use canon IDs and keywords.
     */
    private boolean isInnocent(String targetId) {
        if (targetId == null || targetId.isEmpty()) return false;
        String lower = targetId.toLowerCase();
        // Known canon innocents
        if (lower.contains("wang_ping") || lower.contains("child")
                || lower.contains("elder") || lower.contains("villager")
                || lower.contains("mortal")) return true;
        // NPCs with combat capability are not innocents
        if (lower.contains("teng") || lower.contains("demon")
                || lower.contains("enemy") || lower.contains("bandit")) return false;
        // Default: unknown = not innocent (Wang Lin doesn't assume innocence)
        return false;
    }

    /**
     * Update the ActorRelationshipStore with Wang-Lin-specific multi-axis deltas.
     * This is the CRON-COMPLETIONIST-47 gap fix: without this, Wang Lin's 6-axis
     * relationships from CanonRelationshipSeeder are static and never change.
     *
     * <p>Uses Wang-Lin-specific weights that reflect his canon personality:
     * he values mercy (parents killed), despises betrayal (Situ Nan), honors
     * debts (filial/piety), respects ruthless efficiency against enemies.
     */
    private void updateActorRelationshipStore(WorldEvent event, String otherActorId,
                                                String semanticTag) {
        ServerLevel level = dev.ergenverse.simulation.event.WorldEventBus.currentLevel();
        if (level == null) return;
        ActorRelationshipStore store = ActorRelationshipStore.get(level);
        long tick = level.getGameTime();

        float s = Math.min(event.severity(), 1.0f);
        int trustDelta = 0, respectDelta = 0, fearDelta = 0;
        int familiarityDelta = 2; // base familiarity gain for witnessing anything
        int debtDelta = 0, grievanceDelta = 0;

        switch (semanticTag) {
            case "ACT_OF_MERCY" -> {
                trustDelta = round(4 * s);
                respectDelta = round(3 * s);
                grievanceDelta = round(-2 * s);
            }
            case "ACT_OF_CRUELTY" -> {
                String target = event.targetActorId();
                if (isInnocent(target)) {
                    // Cruelty to innocents: mirrors Teng Clan parallel
                    trustDelta = round(-8 * s);
                    respectDelta = round(-5 * s);
                    fearDelta = round(2 * s);
                    grievanceDelta = round(10 * s);
                } else {
                    // Cruelty to enemies: Wang Lin understands
                    respectDelta = round(2 * s);
                }
            }
            case "PROMISE_MADE" -> {
                trustDelta = round(3 * s);
                respectDelta = round(1 * s);
            }
            case "PROMISE_BROKEN" -> {
                // Mirrors Situ Nan betrayal parallel
                trustDelta = round(-10 * s);
                respectDelta = round(-3 * s);
                grievanceDelta = round(8 * s);
            }
            case "DEBT_REPAID" -> {
                // Filial/piety values
                trustDelta = round(6 * s);
                respectDelta = round(4 * s);
            }
            case "DEBT_IGNORED" -> {
                trustDelta = round(-6 * s);
                grievanceDelta = round(6 * s);
            }
            case "TECHNIQUE_DISPLAYED" -> {
                respectDelta = round(2 * s);
            }
            case "CULTIVATION_REVEALED" -> {
                // Wang Lin believes in concealment
                respectDelta = round(-1 * s);
                fearDelta = round(-1 * s);
            }
            case "GIFT_GIVEN", "GIFT_RECEIVED" -> {
                trustDelta = round(1 * s);
            }
            case "PUBLIC_HUMILIATION" -> {
                respectDelta = round(-3 * s);
                grievanceDelta = round(5 * s);
            }
            case "COMBAT_ENGAGED" -> {
                String outcome = event.meta("outcome", "");
                if ("VICTORY".equals(outcome) || "player_won".equals(outcome)) {
                    respectDelta = round(1 * s);
                }
            }
            case "FORBIDDEN_KNOWLEDGE_WITNESSED" -> {
                // curiosity is neutral-positive for Wang Lin
            }
            case "EXPECTATION_VIOLATION" -> {
                trustDelta = round(-3 * s);
                grievanceDelta = round(3 * s);
            }
            default -> { /* no deltas */ }
        }

        if (trustDelta == 0 && respectDelta == 0 && fearDelta == 0
                && familiarityDelta == 0 && debtDelta == 0 && grievanceDelta == 0) return;

        store.recordMultiAxis(
                WANG_LIN_ID, otherActorId,
                trustDelta, respectDelta, fearDelta,
                familiarityDelta, debtDelta, grievanceDelta,
                "Wang Lin observed: " + event.description(),
                tick);
    }

    private int round(float value) {
        return Math.max(-100, Math.min(100, Math.round(value)));
    }

    private ServerPlayer resolvePlayer(String actorId) {
        if (actorId == null || actorId.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(actorId);
            ServerLevel level = dev.ergenverse.simulation.event.WorldEventBus.currentLevel();
            if (level == null) return null;
            var entity = level.getEntity(uuid);
            if (entity instanceof ServerPlayer sp) return sp;
        } catch (IllegalArgumentException ignored) {
            // Not a UUID — it's an NPC canon ID.
        }
        return null;
    }
}
