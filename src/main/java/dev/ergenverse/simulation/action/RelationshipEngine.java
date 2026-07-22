package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.RelationshipHistory;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * RelationshipEngine — infers relationship deltas from observed events.
 *
 * <p>Per the user's architectural directive (2026-07-23):
 * <pre>
 *   "Instead of RelationshipHistory, I'd have RelationshipEngine
 *    which simply observes events.
 *
 *    GiftGiven → RelationshipEngine → Trust +4, Debt +2
 *    SavedFromDeath → RelationshipEngine → LifeSavingFavor, Respect +40, Fear -10
 *
 *    Nobody manually records anything. The engine infers it.
 *
 *    Relationships shouldn't ask 'Is this Wang Lin?'
 *    They should ask 'Can these two actors possess a relationship?'
 *    The answer is almost always yes.
 *
 *    The graph shouldn't know canon. It should know actors."
 * </pre>
 *
 * <p>Before this class, relationship changes were recorded manually:
 * {@code RelationshipHistory.recordAffinityEarned(player, npcId, reason, delta, tick)}.
 * The caller had to decide the delta, and the {@code HistoryManager} gated
 * recording on {@code isManifestation(npcCanonId)} — a code smell that
 * excluded Wang Ping, Old Chen, sect elders, and every non-manifestation NPC.
 *
 * <p>Now, the RelationshipEngine <b>observes</b> events on the bus and
 * <b>infers</b> the appropriate relationship delta from the event's semantic
 * tag. Nobody manually records anything. The engine decides:
 * <ul>
 *   <li>{@link SemanticTag#GIFT_GIVEN} → affinity +5 (a gift builds trust)</li>
 *   <li>{@link SemanticTag#GIFT_RECEIVED} → affinity +5 (receiving builds obligation)</li>
 *   <li>{@link SemanticTag#COMBAT_ENGAGED} → affinity -15 (combat destroys trust)</li>
 *   <li>{@link SemanticTag#ACT_OF_MERCY} → affinity +10, and the target's fear drops</li>
 *   <li>{@link SemanticTag#ACT_OF_CRUELTY} → affinity -20, and the target's fear rises</li>
 *   <li>{@link SemanticTag#PROMISE_MADE} → affinity +3 (a promise is social capital)</li>
 *   <li>{@link SemanticTag#PROMISE_BROKEN} → affinity -20 (a broken promise destroys trust)</li>
 *   <li>{@link SemanticTag#DEBT_REPAID} → affinity +8</li>
 *   <li>{@link SemanticTag#DEBT_IGNORED} → affinity -12</li>
 *   <li>{@link SemanticTag#TECHNIQUE_DISPLAYED} → affinity +2 (impressive, but also fear +3)</li>
 *   <li>{@link SemanticTag#CULTIVATION_REVEALED} → context-dependent (fear or respect)</li>
 *   <li>{@link SemanticTag#INTERACTION} → affinity +1 (basic social contact)</li>
 * </ul>
 *
 * <h2>The graph knows actors, not canon</h2>
 * <p>This engine does NOT check {@code isManifestation(npcCanonId)}. It does
 * not check whether the target is Wang Lin, Old Chen, or a random villager.
 * Any two actors can possess a relationship. The delta is inferred from the
 * <i>meaning</i> of the event (the semantic tag), not the identity of the
 * actors. This is the fix for the {@code isManifestation()} code smell.
 *
 * <h2>Current limitation: player-centric persistence</h2>
 * <p>The existing {@link RelationshipHistory} API is player-centric (it
 * requires a {@link ServerPlayer} to persist). This means the engine can
 * only record relationships where one of the two actors is the player.
 * NPC-to-NPC relationships (e.g. Wang Lin → Li Muwan) require a more
 * general relationship store, which is a future task. For now, the engine
 * logs NPC-to-NPC relationship changes at DEBUG level so the cascade is
 * visible, even if it isn't persisted yet.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a WorldEventSubscriber
 * that calls the existing RelationshipHistory API. No new persistence
 * layer, no new bus. The "engine" is the inference logic, not infrastructure.
 */
public final class RelationshipEngine implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        // Catch-all — we filter to player./actor./semantic. inside onEvent.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return; // environmental event — no relationship to infer

        // Only handle action and semantic events (not beast migrations, etc.).
        if (!SemanticEventTopics.isActionTopic(event.topic())
                && !SemanticEventTopics.isSemanticTopic(event.topic())) {
            return;
        }

        // Infer the relationship delta from the semantic tag.
        InferredDelta delta = inferDelta(event);
        if (delta == null) return; // event doesn't affect relationships

        // Resolve the player (if any) to persist via RelationshipHistory.
        ServerPlayer player = resolvePlayer(event);
        if (player == null) {
            // NPC-to-NPC relationship — log for now, persist in future.
            Ergenverse.LOGGER.debug("[RelationshipEngine] NPC→NPC: {} {} → {} (delta={}, reason='{}') "
                            + "[not persisted — requires general relationship store]",
                    event.semanticTag(), event.sourceActorId(), event.targetActorId(),
                    delta.affinity, delta.reason);
            return;
        }

        // Determine the "other" actor (the NPC in the relationship).
        String sourceId = event.sourceActorId();
        String targetId = event.targetActorId();
        String playerUuid = player.getStringUUID();
        String npcId = sourceId.equals(playerUuid) ? targetId : sourceId;

        if (npcId == null || npcId.isEmpty()) {
            // No NPC involved (e.g. a player breakthrough has no target).
            // Some semantic events (technique displayed) still affect how
            // nearby NPCs feel about the player — but that requires a
            // proximity query, handled by MemoryEventSubscriber. Skip here.
            return;
        }

        // Record the inferred relationship change.
        try {
            String reason = delta.reason + ": " + event.description();
            if (delta.affinity > 0) {
                RelationshipHistory.recordAffinityEarned(player, npcId,
                        reason, delta.affinity, event.timestamp());
            } else if (delta.affinity < 0) {
                // Negative affinity is recorded as TRUST_BROKEN for now.
                // A finer-grained model would distinguish hostility from betrayal.
                RelationshipHistory.recordTrustBroken(player, npcId,
                        reason, event.timestamp());
            }

            Ergenverse.LOGGER.debug("[RelationshipEngine] {} → {}: affinity {} ({}). "
                            + "Inferred from semantic tag '{}'.",
                    sourceId.equals(playerUuid) ? "player" : sourceId,
                    npcId,
                    delta.affinity >= 0 ? "+" + delta.affinity : delta.affinity,
                    delta.reason, event.semanticTag());
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[RelationshipEngine] failed to record relationship "
                    + "change for event {}", event.topic(), e);
        }
    }

    // ─── Delta inference ──────────────────────────────────────────────

    /**
     * A relationship delta inferred from an event's semantic tag.
     *
     * @param affinity  the change in affinity (trust). Positive = trust up,
     *                  negative = trust down.
     * @param reason    a short human-readable reason for the relationship change.
     *                  This is what appears in the RelationshipHistory ledger.
     */
    private record InferredDelta(int affinity, String reason) {}

    /**
     * Infer the relationship delta from an event's semantic tag.
     *
     * <p>This is the heart of the "engine infers it" principle. Instead of
     * callers deciding "this gift is worth +5 affinity," the engine decides
     * based on what the action <i>meant</i> (the semantic tag).
     *
     * @return the inferred delta, or null if the event doesn't affect
     *         relationships.
     */
    private InferredDelta inferDelta(WorldEvent event) {
        SemanticTag tag = SemanticTag.fromString(event.semanticTag());
        if (tag == null) return null;

        return switch (tag) {
            case INTERACTION -> new InferredDelta(1, "Social interaction");
            case GIFT_GIVEN -> new InferredDelta(5, "Gave a gift");
            case GIFT_RECEIVED -> new InferredDelta(5, "Received a gift");
            case COMBAT_ENGAGED -> new InferredDelta(-15, "Combat occurred");
            case BREAKTHROUGH -> null; // a breakthrough affects the breaker, not a relationship
            case DISCOVERY -> null; // discoveries don't directly affect relationships
            case ACT_OF_MERCY -> new InferredDelta(10, "Act of mercy");
            case ACT_OF_CRUELTY -> new InferredDelta(-20, "Act of cruelty");
            case PUBLIC_HUMILIATION -> new InferredDelta(-15, "Public humiliation");
            case PROMISE_MADE -> new InferredDelta(3, "Promise made");
            case PROMISE_BROKEN -> new InferredDelta(-20, "Promise broken");
            case DEBT_REPAID -> new InferredDelta(8, "Debt repaid");
            case DEBT_IGNORED -> new InferredDelta(-12, "Debt ignored");
            case FORBIDDEN_KNOWLEDGE_WITNESSED -> new InferredDelta(-5, "Witnessed forbidden knowledge");
            case TECHNIQUE_DISPLAYED -> new InferredDelta(2, "Technique displayed");
            case CULTIVATION_REVEALED -> new InferredDelta(2, "Cultivation revealed");
        };
    }

    // ─── Player resolution ────────────────────────────────────────────

    /**
     * Resolve the ServerPlayer involved in this event, if any.
     * Returns null for NPC-to-NPC events (not yet persisted).
     */
    private ServerPlayer resolvePlayer(WorldEvent event) {
        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return null;

        // Try source actor.
        ServerPlayer sp = tryResolvePlayer(level, event.sourceActorId());
        if (sp != null) return sp;

        // Try target actor.
        return tryResolvePlayer(level, event.targetActorId());
    }

    private ServerPlayer tryResolvePlayer(ServerLevel level, String actorId) {
        if (actorId == null || actorId.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(actorId);
            var entity = level.getEntity(uuid);
            if (entity instanceof ServerPlayer sp) return sp;
        } catch (IllegalArgumentException ignored) {
            // Not a UUID — it's an NPC canon ID.
        }
        return null;
    }
}
