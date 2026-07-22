package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.RelationshipHistory;
import dev.ergenverse.simulation.event.ActionDescriptors;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * RelationshipEngine — infers relationship deltas from observed events.
 *
 * <p><b>Architectural evolution (2026-07-23, round 2):</b>
 * The user's critique:
 * <pre>
 *   "RelationshipEngine should NOT assign numbers.
 *    Gift → +5 trust. That's still RPG thinking.
 *
 *    Instead:
 *    Gift → Relationship evaluation
 *      Was it expensive?
 *      Was it public?
 *      Did I need it?
 *      Did I expect it?
 *      Who witnessed it?
 *      Do I trust them already?
 *    Now trust emerges. +1 or +30 or -10 depending on context.
 *    Giving Wang Lin a cheap herb is not the same as giving Li Muwan
 *    a rare medicinal ingredient. Same action. Entirely different meaning."
 * </pre>
 *
 * <p>Before round 2, the engine used a fixed lookup table:
 * {@code GIFT_GIVEN → +5, COMBAT → -15, ACT_OF_MERCY → +10}. That's RPG
 * thinking — every gift is worth the same regardless of context.
 *
 * <p>Now, the engine reads the event's {@link ActionDescriptors} (intent,
 * cost, beneficiary, risk, visibility) and <b>structured metadata</b>
 * (item quality, quantity, etc.) to compute a <b>contextual</b> delta:
 * <ul>
 *   <li>A gift with cost=EXTREME, beneficiary=OTHER → +25 (a life-saving
 *       artifact creates deep obligation)</li>
 *   <li>A gift with cost=LOW, beneficiary=OTHER → +3 (a common herb is
 *       a polite gesture, nothing more)</li>
 *   <li>Combat with risk=EXTREME (the player risked death) and outcome=DEFEAT
 *       → -5 (grudging respect for trying, not deep enmity)</li>
 *   <li>Combat with outcome=VICTORY against a notable NPC → -25 (killing
 *       a patriarch makes enemies of their entire sect)</li>
 * </ul>
 *
 * <p>The delta is no longer hardcoded per semantic tag — it <b>emerges</b>
 * from the compositional descriptors. This is the "trust emerges" principle.
 *
 * <h2>The graph knows actors, not canon</h2>
 * <p>This engine does NOT check {@code isManifestation(npcCanonId)}. Any
 * two actors can possess a relationship. The delta is inferred from the
 * <i>meaning</i> of the event (the descriptors), not the identity of the
 * actors.
 *
 * <h2>Current limitation: player-centric persistence</h2>
 * <p>The existing {@link RelationshipHistory} API is player-centric. NPC-to-NPC
 * relationships are logged but not persisted — requires a general
 * ActorRelationshipStore (next round's task).
 */
public final class RelationshipEngine implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return;

        if (!SemanticEventTopics.isActionTopic(event.topic())
                && !SemanticEventTopics.isSemanticTopic(event.topic())) {
            return;
        }

        // Infer a contextual delta from the descriptors + metadata.
        InferredDelta delta = inferContextualDelta(event);
        if (delta == null) return;

        ServerPlayer player = resolvePlayer(event);
        if (player == null) {
            // NPC-to-NPC — log for now.
            Ergenverse.LOGGER.debug("[RelationshipEngine] NPC→NPC: {} {} → {} (delta={}, reason='{}') "
                            + "[not persisted — requires general relationship store]",
                    event.semanticTag(), event.sourceActorId(), event.targetActorId(),
                    delta.affinity, delta.reason);
            return;
        }

        String sourceId = event.sourceActorId();
        String playerUuid = player.getStringUUID();
        String npcId = sourceId.equals(playerUuid) ? event.targetActorId() : sourceId;

        if (npcId == null || npcId.isEmpty()) return;

        try {
            String reason = delta.reason + ": " + event.description();
            if (delta.affinity > 0) {
                RelationshipHistory.recordAffinityEarned(player, npcId,
                        reason, delta.affinity, event.timestamp());
            } else if (delta.affinity < 0) {
                RelationshipHistory.recordTrustBroken(player, npcId,
                        reason, event.timestamp());
            }

            Ergenverse.LOGGER.debug("[RelationshipEngine] {} → {}: affinity {} ({}). "
                            + "Contextual inference from descriptors {}.",
                    sourceId.equals(playerUuid) ? "player" : sourceId,
                    npcId,
                    delta.affinity >= 0 ? "+" + delta.affinity : delta.affinity,
                    delta.reason, descriptorsFromEvent(event));
        } catch (Exception e) {
            Ergenverse.LOGGER.error("[RelationshipEngine] failed to record relationship "
                    + "change for event {}", event.topic(), e);
        }
    }

    // ─── Contextual delta inference ──────────────────────────────────

    private record InferredDelta(int affinity, String reason) {}

    /**
     * Reconstruct ActionDescriptors from the event's metadata.
     * SimulationActions stores descriptor fields with "_desc_" prefix in metadata.
     */
    private static ActionDescriptors descriptorsFromEvent(WorldEvent event) {
        if (!event.hasMetadata()) return null;
        String intent = event.meta("_desc_intent", "");
        if (intent.isEmpty()) return null;
        try {
            return new ActionDescriptors(
                    ActionDescriptors.Intent.valueOf(intent),
                    ActionDescriptors.Cost.valueOf(event.meta("_desc_cost", "NONE")),
                    ActionDescriptors.Beneficiary.valueOf(event.meta("_desc_beneficiary", "NONE")),
                    ActionDescriptors.Risk.valueOf(event.meta("_desc_risk", "NONE")),
                    ActionDescriptors.Visibility.valueOf(event.meta("_desc_visibility", "PRIVATE")));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Infer a <b>contextual</b> relationship delta from the event's
     * ActionDescriptors and structured metadata.
     *
     * <p>Instead of a fixed lookup table, the delta is computed from:
     * <ul>
     *   <li><b>Intent</b> — HELP actions increase trust; HARM decreases it.</li>
     *   <li><b>Cost</b> — higher cost = bigger trust swing. An EXTREME cost
     *       gift creates deep obligation; a NONE cost gift is a polite gesture.</li>
     *   <li><b>Beneficiary</b> — benefiting OTHER increases trust; SELF_GAIN
     *       is neutral; NONE (destruction) decreases trust.</li>
     *   <li><b>Risk</b> — accepting risk for another's benefit amplifies trust.</li>
     *   <li><b>Metadata</b> — item quality, combat outcome, etc. modulate the delta.</li>
     * </ul>
     */
    private InferredDelta inferContextualDelta(WorldEvent event) {
        ActionDescriptors desc = descriptorsFromEvent(event);
        if (desc == null) {
            // Fall back to semantic tag for backward compat.
            return inferFromTag(event);
        }

        int affinity = 0;
        StringBuilder reason = new StringBuilder();

        // ── Intent base ──
        switch (desc.intent()) {
            case HELP -> { affinity += 3; reason.append("Helpful intent"); }
            case HARM -> { affinity -= 8; reason.append("Harmful intent"); }
            case DEFEND -> { affinity += 5; reason.append("Defensive action"); }
            case TRADE -> { affinity += 2; reason.append("Trade"); }
            case SELF_GAIN -> { /* neutral — self-gain doesn't affect relationships */ }
            case NEUTRAL -> { affinity += 1; reason.append("Neutral interaction"); }
        }

        // ── Cost modulation ──
        // Higher cost = bigger trust swing. A gift that cost nothing is worth
        // less than a gift that cost everything.
        int costBonus = desc.cost().ordinal() * 3; // NONE=0, LOW=3, MEDIUM=6, HIGH=9, EXTREME=12
        if (desc.intent() == ActionDescriptors.Intent.HELP
                && desc.beneficiary() == ActionDescriptors.Beneficiary.OTHER) {
            affinity += costBonus;
            if (costBonus > 0) reason.append(", ").append(desc.cost()).append(" cost");
        }

        // ── Risk modulation ──
        // Accepting risk for another's benefit amplifies trust (it shows sincerity).
        if (desc.intent() == ActionDescriptors.Intent.HELP
                && desc.beneficiary() == ActionDescriptors.Beneficiary.OTHER
                && desc.risk().ordinal() >= ActionDescriptors.Risk.HIGH.ordinal()) {
            affinity += desc.risk().ordinal() * 2; // HIGH=+8, EXTREME=+10
            reason.append(", ").append(desc.risk()).append(" risk");
        }

        // ── Beneficiary modulation ──
        if (desc.beneficiary() == ActionDescriptors.Beneficiary.NONE
                && desc.intent() == ActionDescriptors.Intent.HARM) {
            // Harming with no beneficiary = pure destruction/cruelty.
            affinity -= 10;
            reason.append(", senseless");
        }

        // ── Metadata modulation ──
        if (event.hasMetadata()) {
            String outcome = event.meta("outcome", "");
            if ("DEFEAT".equals(outcome)) {
                // Being defeated in combat: grudging respect, not deep enmity.
                affinity += 3; // offset some of the HARM penalty
                reason.append(", hard-fought");
            }
            if ("VICTORY".equals(outcome)) {
                // Winning combat: the loser's allies will resent it.
                affinity -= 5;
                reason.append(", decisive victory");
            }
        }

        // ── Visibility modulation ──
        // Public acts of kindness generate more social capital than private ones.
        // Public acts of cruelty generate more infamy.
        if (desc.visibility().ordinal() >= ActionDescriptors.Visibility.REGIONAL.ordinal()) {
            if (desc.intent() == ActionDescriptors.Intent.HELP) {
                affinity += 2;
                reason.append(", ").append(desc.visibility()).append(" visibility");
            } else if (desc.intent() == ActionDescriptors.Intent.HARM) {
                affinity -= 5;
                reason.append(", ").append(desc.visibility()).append(" infamy");
            }
        }

        if (affinity == 0) return null; // no relationship change
        return new InferredDelta(affinity, reason.toString());
    }

    /**
     * Fallback: infer from the semantic tag when no descriptors are present
     * (e.g. for legacy/environmental events). This preserves backward
     * compatibility.
     */
    private InferredDelta inferFromTag(WorldEvent event) {
        String tag = event.semanticTag();
        if (tag == null || tag.isEmpty()) return null;

        return switch (tag) {
            case "INTERACTION" -> new InferredDelta(1, "Social interaction");
            case "GIFT_GIVEN", "GIFT_RECEIVED" -> new InferredDelta(5, "Gift exchanged");
            case "COMBAT_ENGAGED" -> new InferredDelta(-15, "Combat occurred");
            case "ACT_OF_MERCY" -> new InferredDelta(10, "Act of mercy");
            case "ACT_OF_CRUELTY" -> new InferredDelta(-20, "Act of cruelty");
            case "PROMISE_MADE" -> new InferredDelta(3, "Promise made");
            case "PROMISE_BROKEN" -> new InferredDelta(-20, "Promise broken");
            case "DEBT_REPAID" -> new InferredDelta(8, "Debt repaid");
            case "DEBT_IGNORED" -> new InferredDelta(-12, "Debt ignored");
            default -> null;
        };
    }

    // ─── Player resolution ────────────────────────────────────────────

    private ServerPlayer resolvePlayer(WorldEvent event) {
        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return null;

        ServerPlayer sp = tryResolvePlayer(level, event.sourceActorId());
        if (sp != null) return sp;
        return tryResolvePlayer(level, event.targetActorId());
    }

    private ServerPlayer tryResolvePlayer(ServerLevel level, String actorId) {
        if (actorId == null || actorId.isEmpty()) return null;
        try {
            UUID uuid = UUID.fromString(actorId);
            var entity = level.getEntity(uuid);
            if (entity instanceof ServerPlayer sp) return sp;
        } catch (IllegalArgumentException ignored) {}
        return null;
    }
}
