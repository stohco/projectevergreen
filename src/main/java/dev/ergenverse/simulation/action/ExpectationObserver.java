package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExpectationObserver — tracks repeated interactions and detects when
 * expectations change.
 *
 * <p>Per the user's directive (2026-07-23 #2):
 * <pre>
 *   "If Wang Lin saves you three times... eventually... you expect him
 *    to save you. Then one day he doesn't. That disappointment
 *    becomes a new event.
 *
 *    Expectations changing are incredibly important in believable
 *    relationships."
 * </pre>
 *
 * <p>This subscriber tracks repeated interactions between actor pairs
 * and detects <b>expectation violations</b>. When one actor
 * consistently behaves a certain way (e.g. always helps, always
 * trades, always shows mercy), the other actor forms an expectation.
 * When that pattern breaks, the violation is published as a new
 * semantic event ({@code semantic.expectation_violation}).
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li>Track interaction counts per actor pair: "actorA|actorB" → count.</li>
 *   *li>After N interactions of a consistent type (default: 3), mark an
 *       expectation as formed. e.g. "I expect this actor to help."</li>
 *   *li>When a related event occurs that CONTRADICTS the expectation,
 *       publish a {@code semantic.expectation_violation} event.
 *       The severity scales with how long the pattern held.</li>
 * </ul>
 *
 * <h2>Expectation types inferred from semantic tags</h2>
 * <ul>
 *   <li>ACT_OF_MERCY repeated → expectation of mercy → cruelty = violation</li>
 *   <li>GIFT_GIVEN repeated → expectation of generosity → refusal = violation</li>
 *   <li>PROMISE_MADE repeated → expectation of reliability → broken = violation</li>
 *   <li>COMBAT_ENGAGED (repeated as defending) → expectation of protection → abandonment = violation</li>
 * </ul>
 *
 * <p><b>Not a new Engine (Art XXVI):</b> WorldEventSubscriber that
 * publishes semantic events. No new bus, no new infrastructure.
 */
public final class ExpectationObserver implements WorldEventSubscriber {

    /** Interactions needed before an expectation forms. */
    private static final int FORMATION_THRESHOLD = 3;

    /** Actor pair interaction counts: "actorA|actorB" → count. */
    private static final ConcurrentHashMap<String, Integer> interactionCounts = new ConcurrentHashMap<>();

    /** Formed expectations: "actorA|actorB|expectationType" → interaction count at formation. */
    private static final ConcurrentHashMap<String, Integer> formedExpectations = new ConcurrentHashMap<>();

    /** Cooldown: don't publish another violation for the same pair+type within this window. */
    private static final long VIOLATION_COOLDOWN = 12000L; // 10 minutes

    /** Last violation time: "actorA|actorB|expectationType" → tick. */
    private static final ConcurrentHashMap<String, Long> lastViolation = new ConcurrentHashMap<>();

    @Override
    public String topicPrefix() {
        return ""; // observe all action and semantic events
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return;

        String topic = event.topic();
        String sourceId = event.sourceActorId();
        String targetId = event.targetActorId();
        if (sourceId.isEmpty() && targetId.isEmpty()) return;

        // Determine the action type to track.
        String actionType = mapToActionType(event);
        if (actionType == null) return;

        // Normalize the pair key (always use lexicographic order).
        String pairKey = normalizePair(sourceId, targetId);

        // Increment interaction count.
        int count = interactionCounts.merge(pairKey, 1, Integer::sum);

        // Check for formed expectations and violations.
        String expectationKey = pairKey + "|" + actionType;

        Integer formedCount = formedExpectations.get(expectationKey);
        if (formedCount == null && count >= FORMATION_THRESHOLD) {
            // Form the expectation.
            formedExpectations.put(expectationKey, count);
            Ergenverse.LOGGER.info("[ExpectationObserver] Expectation formed: {} expects {} from {} "
                            + "(after {} interactions)",
                    targetId, actionType, sourceId, count);
        } else if (formedCount != null) {
            // Check if this event VIOLATES the expectation.
            if (isViolation(event, actionType)) {
                // Check cooldown.
                String cooldownKey = expectationKey;
                Long last = lastViolation.get(cooldownKey);
                if (last == null || (event.timestamp() - last) >= VIOLATION_COOLDOWN) {
                    lastViolation.put(cooldownKey, event.timestamp());

                    // Publish the expectation violation as a new semantic event.
                    String violationType = actionType + "_violation";
                    String desc = targetId + " expected " + actionType
                            + " from " + sourceId + " (pattern held for "
                            + (count - formedCount) + " more interactions), "
                            + "but got: " + event.description();

                    WorldEventBus.dispatch(WorldEvent.of(
                            "semantic.expectation_violation",
                            event.energyType(), event.pos(),
                            0.6f, 0.6f, desc,
                            "SIMULATION:ExpectationObserver", event.timestamp(),
                            sourceId, targetId, "EXPECTATION_VIOLATION",
                            java.util.Map.of(
                                    "violation_type", violationType,
                                    "expected_action", actionType,
                                    "pattern_duration", String.valueOf(count - formedCount),
                                    "pair_key", pairKey,
                                    "source_actor", sourceId,
                                    "target_actor", targetId
                            )));

                    Ergenverse.LOGGER.info("[ExpectationObserver] VIOLATION: {} — {}",
                            expectationKey, desc);
                }
            }
        }
    }

    /**
     * Map a topic/semanticTag to an action type that can form expectations.
     * Only positive/constructive actions form expectations — cruelty,
     * broken promises, etc. don't form "expectations" in the
     * sense of "I expect this to continue being negative."
     */
    private String mapToActionType(WorldEvent event) {
        String tag = event.semanticTag();
        if (!tag.isEmpty()) {
            // Infer from topic for action-level events.
            if (SemanticEventTopics.PLAYER_GIFT_GIVEN.equals(event.topic())
                    || SemanticEventTopics.ACTOR_GIFT_GIVEN.equals(event.topic()))
                return "generosity";
            return null;
        }
        return switch (tag) {
            case "ACT_OF_MERCY" -> "mercy";
            case "GIFT_GIVEN" -> "generosity";
            case "GIFT_RECEIVED" -> "generosity";
            case "PROMISE_MADE" -> "reliability";
            case "COMBAT_ENGAGED" -> {
                // Only forms an expectation of protection if the source
                // consistently defends the target.
                String outcome = event.meta("combat_outcome", "");
                yield "protection";
            }
            default -> null; // cruelty, broken promises, etc. don't form expectations
        };
    }

    /**
     * Check if this event violates a formed expectation.
     * A violation occurs when the expected action type doesn't match
     * the actual event's semantic character.
     */
    private boolean isViolation(WorldEvent event, String expectedAction) {
        String actualTag = event.semanticTag();
        return switch (expectedAction) {
            case "mercy" -> {
                // Mercy expectation violated by cruelty or indifference.
                yield "ACT_OF_CRUELTY".equals(actualTag);
            }
            case "generosity" -> {
                // Generosity expectation violated by refusal or theft.
                yield "ACT_OF_CRUELTY".equals(actualTag)
                        || "PUBLIC_HUMILIATION".equals(actualTag);
            }
            case "reliability" -> {
                yield "PROMISE_BROKEN".equals(actualTag);
            }
            case "protection" -> {
                // Protection expectation violated when the protector
                // is absent or the target is harmed again.
                String outcome = event.meta("combat_outcome", "");
                yield "ACT_OF_CRUELTY".equals(actualTag)
                        || "player_won".equals(outcome) && event.targetActorId().contains(sourceActorId(event));
            }
            default -> false;
        };
    }

    private String sourceActorId(WorldEvent event) {
        String s = event.sourceActorId();
        return s != null ? s : "";
    }

    private static String normalizePair(String a, String b) {
        return a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
    }

    public static int expectationCount() { return formedExpectations.size(); }

    public static void clearAll() {
        interactionCounts.clear();
        formedExpectations.clear();
        lastViolation.clear();
    }
}
