package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * OpportunityGenerator — the missing subscriber the user identified.
 *
 * <p>Per the user's architectural directive (2026-07-23):
 * <pre>
 *   "You found Rumors, Chronicle, Dialogue. Good.
 *    But I don't see Opportunity Generator.
 *
 *    Imagine. Player defeats a beast. Villager witnesses.
 *    Rumor spreads. Someone hears. That person now thinks
 *    'Maybe I should ask them to escort me.'
 *    That's not a rumor. That's a new possibility emerging.
 *    That's the simulation growing."
 * </pre>
 *
 * <p>Rumors spread <i>information</i>. Opportunities create <i>possibilities</i>.
 * When the player does something notable — defeats a beast, reveals their
 * cultivation, shows mercy, displays a technique — this subscriber observes
 * it and generates an <b>opportunity</b>: a new possibility that emerges
 * from the player's deed.
 *
 * <h2>Opportunity types generated</h2>
 * <ul>
 *   <li><b>player.combat.engaged</b> (player won) → ESCORT_REQUEST:
 *       "A villager heard of your victory and seeks an escort through
 *       beast-infested territory." The simulation grows: the player's
 *       strength created a new social possibility.</li>
 *   <li><b>player.breakthrough</b> (notable realm) → RECRUITMENT_OFFER:
 *       "A sect elder sensed your breakthrough and considers recruiting
 *       you." The player's power drew attention.</li>
 *   <li><b>semantic.act_of_mercy</b> → GRATITUDE_FAVOR:
 *       "Word of your mercy spread. Someone who was wronged seeks
 *       your protection." Mercy creates social capital.</li>
 *   <li><b>semantic.technique_displayed</b> → CHALLENGE_ISSUED:
 *       "A fellow cultivator witnessed your technique and wishes to
 *       spar." Displaying power attracts both allies and rivals.</li>
 *   <li><b>semantic.cultivation_revealed</b> → FACTION_INTEREST:
 *       "Your revealed cultivation has drawn the attention of a nearby
 *       faction." Power revealed is power contested.</li>
 * </ul>
 *
 * <h2>How opportunities emerge</h2>
 * <p>This subscriber doesn't create the opportunity immediately. It
 * <b>seeds</b> an opportunity — a potential future event. The opportunity
 * then needs a willing NPC to carry it (the villager who wants an escort,
 * the elder who wants to recruit). That NPC is found via the ActorRegistry
 * in a future wiring step. For now, this class logs the opportunity
 * emergence so the cascade is visible, and publishes a follow-up
 * {@code opportunity.emerged} event so other systems can react.
 *
 * <p><b>Cooldown:</b> each opportunity type has a per-player cooldown to
 * prevent spam. A player who kills 10 beasts doesn't get 10 escort
 * requests — they get one, and not again for a game-day.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a WorldEventSubscriber
 * that publishes follow-up events. It doesn't create new infrastructure.
 * The "opportunity" is itself just another bus event that other systems
 * (dialogue, quest hooks, NPC goals) can subscribe to.
 */
public final class OpportunityGenerator implements WorldEventSubscriber {

    /** Cooldown per player+opportunityType: 24000 ticks = 1 Minecraft day. */
    private static final long COOLDOWN_TICKS = 24000L;

    /** Key: "playerUuid|opportunityType" → last tick generated. */
    private final ConcurrentMap<String, Long> lastGenerated = new ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        // Catch-all — we filter to specific action/semantic topics inside onEvent.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return;

        String topic = event.topic();

        // Only react to specific high-impact events.
        if (!isOpportunityTrigger(topic)) return;

        // Resolve the player UUID (the actor whose deed created the opportunity).
        String playerUuid = resolvePlayerUuid(event);
        if (playerUuid == null) return; // NPC-sourced — future task

        // Determine the opportunity type from the event.
        OpportunityType oppType = mapToOpportunityType(event);
        if (oppType == null) return;

        // Cooldown check: don't spam the same opportunity type for the same player.
        String cooldownKey = playerUuid + "|" + oppType.name();
        Long last = lastGenerated.get(cooldownKey);
        if (last != null && (event.timestamp() - last) < COOLDOWN_TICKS) return;
        lastGenerated.put(cooldownKey, event.timestamp());

        // Generate the opportunity: publish a follow-up event so other
        // systems (dialogue, NPC goals) can react.
        String desc = buildOpportunityDescription(oppType, event);
        WorldEventBus.publish(
                "opportunity." + oppType.topicSuffix() + ".emerged",
                event.energyType(),
                event.pos(),
                0.4f,
                desc,
                "SIMULATION:OpportunityGenerator",
                event.timestamp());

        Ergenverse.LOGGER.info("[OpportunityGenerator] Opportunity emerged for player {}: "
                        + "{} — {}",
                playerUuid, oppType.name(), desc);
    }

    // ─── Opportunity classification ───────────────────────────────────

    /**
     * The types of opportunities that can emerge from player deeds.
     * Each maps to a topic suffix for the follow-up event.
     */
    private enum OpportunityType {
        /** A villager seeks an escort through dangerous territory. */
        ESCORT_REQUEST("escort_request"),
        /** A sect elder considers recruiting the player. */
        RECRUITMENT_OFFER("recruitment_offer"),
        /** Someone wronged seeks the player's protection (from mercy). */
        GRATITUDE_FAVOR("gratitude_favor"),
        /** A fellow cultivator wishes to spar (from technique display). */
        CHALLENGE_ISSUED("challenge_issued"),
        /** A nearby faction takes interest in the player (from cultivation reveal). */
        FACTION_INTEREST("faction_interest");

        private final String suffix;
        OpportunityType(String suffix) { this.suffix = suffix; }
        String topicSuffix() { return suffix; }
    }

    /** Whether a topic can trigger opportunity generation. */
    private boolean isOpportunityTrigger(String topic) {
        return SemanticEventTopics.PLAYER_COMBAT_ENGAGED.equals(topic)
                || SemanticEventTopics.PLAYER_BREAKTHROUGH.equals(topic)
                || SemanticEventTopics.SEMANTIC_ACT_OF_MERCY.equals(topic)
                || SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED.equals(topic)
                || SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED.equals(topic);
    }

    /** Map an event to the opportunity type it generates. */
    private OpportunityType mapToOpportunityType(WorldEvent event) {
        String topic = event.topic();
        if (SemanticEventTopics.PLAYER_COMBAT_ENGAGED.equals(topic)) {
            // Only a victory generates an escort request.
            if (event.description().contains("killed")) return OpportunityType.ESCORT_REQUEST;
            return null;
        }
        if (SemanticEventTopics.PLAYER_BREAKTHROUGH.equals(topic)) {
            return OpportunityType.RECRUITMENT_OFFER;
        }
        if (SemanticEventTopics.SEMANTIC_ACT_OF_MERCY.equals(topic)) {
            return OpportunityType.GRATITUDE_FAVOR;
        }
        if (SemanticEventTopics.SEMANTIC_TECHNIQUE_DISPLAYED.equals(topic)) {
            return OpportunityType.CHALLENGE_ISSUED;
        }
        if (SemanticEventTopics.SEMANTIC_CULTIVATION_REVEALED.equals(topic)) {
            return OpportunityType.FACTION_INTEREST;
        }
        return null;
    }

    /** Build a human-readable description for the emerged opportunity. */
    private String buildOpportunityDescription(OpportunityType type, WorldEvent event) {
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

    // ─── Player resolution ────────────────────────────────────────────

    private String resolvePlayerUuid(WorldEvent event) {
        // For player.* events, the source is the player.
        String source = event.sourceActorId();
        if (isUuid(source)) return source;

        // For actor.* events where the player is the target.
        String target = event.targetActorId();
        if (isUuid(target)) return target;

        return null;
    }

    private boolean isUuid(String s) {
        if (s == null || s.isEmpty()) return false;
        try {
            UUID.fromString(s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /** Clear cooldown state (called on world unload). */
    public void clearCooldowns() {
        lastGenerated.clear();
    }
}
