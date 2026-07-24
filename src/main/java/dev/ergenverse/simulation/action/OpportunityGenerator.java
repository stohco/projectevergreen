package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.SemanticTag;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OpportunityGenerator — seeds opportunities from notable player deeds.
 *
 * <p>Per the user's architectural directive (2026-07-23 #2):
 * <pre>
 *   "Opportunities shouldn't search for NPCs.
 *    NPCs should search for opportunities.
 *
 *   EscortOpportunityCreated gets published. Nobody owns it.
 *   Nearby NPCs evaluate it.
 *   One thinks 'I need protection.'
 *   Another thinks 'I hate that player.'
 *   Another thinks 'I already hired somebody.'
 *   Only one claims it.
 *
 *   That's pure emergence."
 * </pre>
 *
 * <p><b>Inverted ownership:</b> This class ONLY creates the opportunity
 * event. It does NOT assign it to an NPC. The follow-up event
 * ({@code opportunity.*.emerged}) is published to the bus, and any
 * NPC with a high-enough motivation to claim it can do so. The
 * {@link OpportunityClaimSubscriber} is the subscriber that NPCs
 * use to evaluate and claim opportunities — this class is decoupled
 * from which NPC claims what.
 *
 * <h2>Opportunity types</h2>
 * <ul>
 *   <li><b>player.combat.engaged</b> (player won) → ESCORT_REQUEST</li>
 *   <li><b>player.breakthrough</b> → RECRUITMENT_OFFER</li>
 *   <li><b>semantic.act_of_mercy</b> → GRATITUDE_FAVOR</li>
 *   <li><b>semantic.technique_displayed</b> → CHALLENGE_ISSUED</li>
 *   <li><b>semantic.cultivation_revealed</b> → FACTION_INTEREST</li>
 * </ul>
 */
public final class OpportunityGenerator implements WorldEventSubscriber {

    /** Cooldown per player+opportunityType: 24000 ticks = 1 MC day. */
    private static final long COOLDOWN_TICKS = 24000L;

    private final ConcurrentMap<String, Long> lastGenerated = new ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        return ""; // filter inside onEvent
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return;

        String playerUuid = resolvePlayerUuid(event);
        if (playerUuid == null) return;

        OpportunityType oppType = mapToOpportunityType(event);
        if (oppType == null) return;

        // Cooldown: don't spam.
        String cooldownKey = playerUuid + "|" + oppType.name();
        Long last = lastGenerated.get(cooldownKey);
        if (last != null && (event.timestamp() - last) < COOLDOWN_TICKS) return;
        lastGenerated.put(cooldownKey, event.timestamp());

        // Publish the opportunity — unowned. NPCs will evaluate and claim it.
        String desc = buildDescription(oppType, event);
        WorldEventBus.dispatch(WorldEvent.of(
                "opportunity." + oppType.topicSuffix() + ".emerged",
                event.energyType(), event.pos(),
                0.4f, 0.4f, desc,
                "SIMULATION:OpportunityGenerator", event.timestamp(),
                playerUuid, "", SemanticTag.OPPORTUNITY_EMERGED.name(),
                java.util.Map.of(
                        "opportunity_type", oppType.name(),
                        "player_uuid", playerUuid,
                        "source_topic", event.topic(),
                        "source_severity", String.valueOf(event.severity())
                )));

        Ergenverse.LOGGER.info("[OpportunityGenerator] Opportunity seeded: {} for player {} — {}",
                oppType.name(), playerUuid, desc);
    }

    private enum OpportunityType {
        ESCORT_REQUEST("escort_request"),
        RECRUITMENT_OFFER("recruitment_offer"),
        GRATITUDE_FAVOR("gratitude_favor"),
        CHALLENGE_ISSUED("challenge_issued"),
        FACTION_INTEREST("faction_interest");

        private final String suffix;
        OpportunityType(String s) { this.suffix = s; }
        String topicSuffix() { return suffix; }
    }

    private OpportunityType mapToOpportunityType(WorldEvent event) {
        String topic = event.topic();
        if (SemanticEventTopics.PLAYER_COMBAT_ENGAGED.equals(topic)) {
            String outcome = event.meta("outcome", "");
            if ("VICTORY".equals(outcome)) return OpportunityType.ESCORT_REQUEST;
            return null;
        }
        if (SemanticEventTopics.PLAYER_BREAKTHROUGH.equals(topic))
            return OpportunityType.RECRUITMENT_OFFER;
        if (SemanticEventTopics.SEMANTIC_ACT_OF_MERCY.equals(topic))
            return OpportunityType.GRATITUDE_FAVOR;
        if (SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED.equals(topic))
            return OpportunityType.CHALLENGE_ISSUED;
        if (SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED.equals(topic))
            return OpportunityType.FACTION_INTEREST;
        return null;
    }

    private String buildDescription(OpportunityType type, WorldEvent event) {
        return switch (type) {
            case ESCORT_REQUEST -> "A villager heard of the victory at "
                    + posStr(event) + " and seeks an escort through beast-infested territory.";
            case RECRUITMENT_OFFER -> "A sect elder sensed the breakthrough at "
                    + posStr(event) + " and considers issuing a recruitment offer.";
            case GRATITUDE_FAVOR -> "Word of the mercy shown at "
                    + posStr(event) + " has spread. Someone who was wronged seeks protection.";
            case CHALLENGE_ISSUED -> "A fellow cultivator witnessed the technique at "
                    + posStr(event) + " and wishes to spar.";
            case FACTION_INTEREST -> "The cultivation revealed at "
                    + posStr(event) + " has drawn the attention of a nearby faction.";
        };
    }

    private String posStr(WorldEvent event) {
        return "(" + event.pos().getX() + ", " + event.pos().getZ() + ")";
    }

    private String resolvePlayerUuid(WorldEvent event) {
        String source = event.sourceActorId();
        if (isUuid(source)) return source;
        String target = event.targetActorId();
        if (isUuid(target)) return target;
        return null;
    }

    private boolean isUuid(String s) {
        if (s == null || s.isEmpty()) return false;
        try { UUID.fromString(s); return true; }
        catch (IllegalArgumentException e) { return false; }
    }

    public void clearCooldowns() { lastGenerated.clear(); }
}
