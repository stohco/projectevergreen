package dev.ergenverse.simulation.belief;

import java.util.ArrayList;
import java.util.List;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorType;
import dev.ergenverse.simulation.event.ActionDescriptors;
import dev.ergenverse.simulation.event.SemanticEventTopics;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import dev.ergenverse.simulation.intent.ActorEntityLink;

/**
 * BeliefFormationSubscriber — forms beliefs from observed events.
 *
 * <p>Per the user's architectural directive (2026-07-23, round 2):
 * <pre>
 *   "Player saved Wang Ping. → Fact.
 *    Old Chen sees it. → Believes: 'Player is courageous.'
 *    Teng scout sees it. → Believes: 'Player is trying to recruit villagers.'
 *    Bandit sees it. → Believes: 'Player is dangerous.'
 *    Same event. Three different beliefs."
 * </pre>
 *
 * <p>This subscriber observes events on the {@link WorldEventBus} and, for
 * each nearby NPC who witnessed the event, forms a <b>belief</b> about the
 * actor. The belief is not derived from the event alone — it's derived from
 * the event <b>filtered through the NPC's worldview</b>.
 *
 * <h2>How worldview shapes belief</h2>
 * <p>The same event produces different beliefs depending on who observes it:
 * <ul>
 *   <li><b>An act of mercy</b> (HELP + HIGH cost + OTHER beneficiary + HIGH risk):
 *     <ul>
 *       <li>A <b>villager</b> (values community) → believes the actor is
 *           "courageous" and "compassionate".</li>
 *       <li>A <b>sect cultivator</b> (values power) → believes the actor is
 *           "soft" or "naive" — mercy is weakness to the ruthless.</li>
 *       <li>A <b>bandit</b> (fears interference) → believes the actor is
 *           "dangerous" — someone who intervenes against predators is a
 *           threat to bandit operations.</li>
 *     </ul>
 *   </li>
 *   <li><b>Revealing cultivation</b> (SELF_GAIN + HIGH visibility):
 *     <ul>
 *       <li>A <b>sect elder</b> → believes the actor is "ambitious" —
 *           worthy of recruitment or surveillance.</li>
 *       <li>A <b>rival cultivator</b> → believes the actor is "arrogant"
 *           — needs to be put in their place.</li>
 *       <li>A <b>mortal</b> → believes the actor is "terrifying" —
 *           a being beyond mortal comprehension.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>How beliefs form</h2>
 * <p>For each witnessed event:
 * <ol>
 *   <li>Find all linked NPCs within perception range (64 blocks).</li>
 *   <li>Determine the NPC's worldview from their ActorType and provenance.</li>
 *   <li>Map the event's {@link ActionDescriptors} to candidate traits.</li>
 *   <li>Filter the candidates through the NPC's worldview (some traits
 *       are only formed by certain worldviews).</li>
 *   <li>Record each surviving belief in the {@link BeliefStore}.</li>
 * </ol>
 *
 * <h2>Perception range</h2>
 * <p>Only NPCs within 64 blocks of the event form beliefs from direct
 * observation. NPCs who hear about the event via rumors form beliefs
 * later (via a future RumorToBeliefSubscriber). This ensures beliefs
 * spread physically, like information — the user's "localized reputation"
 * principle.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a WorldEventSubscriber
 * that writes to BeliefStore. No new bus, no new infrastructure.
 */
public final class BeliefFormationSubscriber implements WorldEventSubscriber {

    /** Maximum distance at which an NPC can witness and form a belief from an event. */
    private static final int WITNESS_DISTANCE = 64;

    /** Minimum severity for an event to trigger belief formation. */
    private static final float MIN_SEVERITY_FOR_BELIEF = 0.3f;

    @Override
    public String topicPrefix() {
        // Catch-all — we filter to action/semantic topics inside onEvent.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (!event.hasActors()) return; // environmental event — no belief to form
        if (event.severity() < MIN_SEVERITY_FOR_BELIEF) return;

        // Only form beliefs from action and semantic events.
        if (!SemanticEventTopics.isActionTopic(event.topic())
                && !SemanticEventTopics.isSemanticTopic(event.topic())) {
            return;
        }

        // The actor the belief will be about (the source of the action).
        String aboutActor = event.sourceActorId();
        if (aboutActor == null || aboutActor.isEmpty()) return;

        ActionDescriptors desc = descriptorsFromEvent(event);
        if (desc == null) return;

        // Find all nearby linked NPCs (witnesses).
        int ex = event.pos().getX();
        int ez = event.pos().getZ();
        long distSq = (long) WITNESS_DISTANCE * WITNESS_DISTANCE;

        for (Actor witness : ActorRegistry.all()) {
            if (!ActorEntityLink.isLinked(witness.id)) continue;
            if (witness.id.equals(aboutActor)) continue; // don't form beliefs about yourself

            // Distance check.
            long dx = witness.blockX - ex;
            long dz = witness.blockZ - ez;
            if (dx * dx + dz * dz > distSq) continue;

            // Determine the witness's worldview.
            Worldview worldview = inferWorldview(witness);

            // Map the event's descriptors to candidate traits, filtered by worldview.
            List<TraitCandidate> candidates = mapEventToTraits(event, desc, worldview);
            if (candidates.isEmpty()) continue;

            // Form beliefs.
            BeliefStore store = BeliefStore.get();
            for (TraitCandidate tc : candidates) {
                store.recordBelief(witness.id, aboutActor, tc.trait, tc.confidence,
                        event.timestamp());
            }

            Ergenverse.LOGGER.debug("[BeliefFormation] {} witnessed {} → formed beliefs: {}",
                    witness.id, event.topic(),
                    candidates.stream()
                            .map(c -> c.trait + "(" + Math.round(c.confidence * 100) + "%)")
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("(none)"));
        }
    }

    // ─── Worldview inference ─────────────────────────────────────────

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
     * A witness's worldview determines which traits they attribute to
     * observed actors. This is the "filter" that makes the same event
     * produce different beliefs for different observers.
     *
     * <p>Worldview is inferred from the witness's ActorType and provenance.
     * A more sophisticated system would read from the NPC's personality
     * model (Ontology/DaoIdentity); this is a first approximation.
     */
    private enum Worldview {
        /** Values community, compassion, protection. Villagers, elders. */
        COMMUNAL,
        /** Values power, hierarchy, ambition. Sect cultivators, elders. */
        HIERARCHICAL,
        /** Values self-interest, predation, fear. Bandits, rogue cultivators. */
        PREDATORY,
        /** Fears cultivators, values safety. Mortals, children. */
        FEARFUL,
        /** Neutral / unclassified. Default. */
        NEUTRAL
    }

    private Worldview inferWorldview(Actor witness) {
        if (witness.type == ActorType.BEAST) return Worldview.PREDATORY;
        if (witness.provenance != null) {
            String p = witness.provenance.toLowerCase();
            if (p.contains("bandit") || p.contains("rogue")) return Worldview.PREDATORY;
            if (p.contains("elder") || p.contains("sect_head") || p.contains("patriarch"))
                return Worldview.HIERARCHICAL;
            if (p.contains("villager") || p.contains("child") || p.contains("farmer"))
                return Worldview.COMMUNAL;
            if (p.contains("mortal")) return Worldview.FEARFUL;
        }
        // Default: NPC-type actors in sects → hierarchical; others → neutral.
        if (witness.type == ActorType.NPC) return Worldview.HIERARCHICAL;
        return Worldview.NEUTRAL;
    }

    // ─── Event → Trait mapping ────────────────────────────────────────

    /** A candidate trait with a confidence level. */
    private record TraitCandidate(String trait, float confidence) {}

    /**
     * Map an event's descriptors to candidate traits, filtered by the
     * witness's worldview.
     *
     * <p>This is where the "same event → different beliefs" magic happens.
     * The descriptors are objective; the trait attribution is subjective.
     */
    private List<TraitCandidate> mapEventToTraits(WorldEvent event,
                                                             ActionDescriptors desc,
                                                             Worldview worldview) {
        List<TraitCandidate> traits = new ArrayList<>();

        // ── Acts of mercy / help ──
        if (desc.intent() == ActionDescriptors.Intent.HELP
                && desc.beneficiary() == ActionDescriptors.Beneficiary.OTHER) {
            float baseConf = 0.5f + (desc.cost().ordinal() * 0.08f) + (desc.risk().ordinal() * 0.08f);
            baseConf = Math.min(0.95f, baseConf);

            switch (worldview) {
                case COMMUNAL -> {
                    traits.add(new TraitCandidate("compassionate", baseConf));
                    if (desc.risk().ordinal() >= ActionDescriptors.Risk.HIGH.ordinal())
                        traits.add(new TraitCandidate("courageous", baseConf));
                }
                case HIERARCHICAL -> {
                    // Sect cultivators see mercy as softness (unless the risk was extreme).
                    if (desc.risk().ordinal() >= ActionDescriptors.Risk.EXTREME.ordinal()) {
                        traits.add(new TraitCandidate("courageous", baseConf * 0.7f));
                    } else {
                        traits.add(new TraitCandidate("naive", baseConf * 0.5f));
                    }
                }
                case PREDATORY -> {
                    // Bandits see helpers as dangerous interferers.
                    traits.add(new TraitCandidate("dangerous", baseConf * 0.8f));
                }
                case FEARFUL -> {
                    // Mortals see powerful helpers as protectors — or as terrifying.
                    if (desc.risk().ordinal() >= ActionDescriptors.Risk.HIGH.ordinal()) {
                        traits.add(new TraitCandidate("terrifying", baseConf * 0.6f));
                    } else {
                        traits.add(new TraitCandidate("kind", baseConf * 0.7f));
                    }
                }
            }
        }

        // ── Acts of harm / cruelty ──
        if (desc.intent() == ActionDescriptors.Intent.HARM) {
            float baseConf = 0.6f;

            switch (worldview) {
                case COMMUNAL, FEARFUL -> {
                    traits.add(new TraitCandidate("dangerous", baseConf));
                    if (desc.beneficiary() == ActionDescriptors.Beneficiary.NONE)
                        traits.add(new TraitCandidate("cruel", baseConf));
                }
                case HIERARCHICAL -> {
                    // Sect cultivators may see strength in violence.
                    if (desc.beneficiary() == ActionDescriptors.Beneficiary.SELF)
                        traits.add(new TraitCandidate("strong", baseConf * 0.6f));
                    else
                        traits.add(new TraitCandidate("ruthless", baseConf));
                }
                case PREDATORY -> {
                    // Bandits respect violence.
                    traits.add(new TraitCandidate("powerful", baseConf * 0.7f));
                }
            }
        }

        // ── Cultivation revealed / technique displayed ──
        if (event.hasSemanticTag()) {
            String tag = event.semanticTag();
            if (tag.equals("CULTIVATION_REVEALED") || tag.equals("TECHNIQUE_DISPLAYED")) {
                float baseConf = 0.5f + (desc.visibility().ordinal() * 0.1f);
                baseConf = Math.min(0.9f, baseConf);

                switch (worldview) {
                    case HIERARCHICAL -> {
                        traits.add(new TraitCandidate("ambitious", baseConf));
                        if (desc.visibility().ordinal() >= ActionDescriptors.Visibility.REGIONAL.ordinal())
                            traits.add(new TraitCandidate("noteworthy", baseConf * 0.8f));
                    }
                    case FEARFUL -> {
                        traits.add(new TraitCandidate("terrifying", baseConf));
                    }
                    case PREDATORY -> {
                        traits.add(new TraitCandidate("threatening", baseConf * 0.7f));
                    }
                    case COMMUNAL -> {
                        traits.add(new TraitCandidate("powerful", baseConf * 0.6f));
                    }
                }
            }
        }

        // ── Promises ──
        if (event.hasSemanticTag() && event.semanticTag().equals("PROMISE_MADE")) {
            traits.add(new TraitCandidate("reliable", 0.4f));
        }
        if (event.hasSemanticTag() && event.semanticTag().equals("PROMISE_BROKEN")) {
            // Breaks the "reliable" belief, forms "treacherous".
            traits.add(new TraitCandidate("treacherous", 0.7f));
        }

        return traits;
    }
}
