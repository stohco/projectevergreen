package dev.ergenverse.simulation.action;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorType;
import dev.ergenverse.simulation.event.WorldEvent;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.event.WorldEventSubscriber;
import dev.ergenverse.simulation.intent.ActorEntityLink;
import net.minecraft.server.level.ServerLevel;

/**
 * NpcSemanticRelationshipSubscriber — wires ALL NPCs (not just Wang Lin)
 * to the WorldEventBus for semantic events, updating the multi-axis
 * ActorRelationshipStore when NPCs observe meaningful deeds.
 *
 * <p>Before this subscriber, only {@code WangLinSemanticSubscriber} updated
 * opinions from observed events. Every other NPC had zero semantic event
 * processing — they recorded memories (via {@code MemoryEventSubscriber})
 * and formed beliefs (via {@code BeliefFormationSubscriber}) but their
 * actual relationship state ({@code ActorRelationshipStore}) remained
 * unchanged by what they witnessed. An elder could watch the player
 * save a child 20 times and their relationship graph would show trust=0,
 * respect=0, fear=0, familiarity=0, debt=0, grievance=0.
 *
 * <p>Now, every semantic event observed by an NPC updates their multi-axis
 * relationship with the actor who performed the deed. The weights vary by
 * the observer's worldview (COMMUNAL, HIERARCHICAL, PREDATORY, FEARFUL)
 * — the same mechanism used by {@code BeliefFormationSubscriber}.
 *
 * <h2>How it works</h2>
 * <ul>
 *   <li>Subscribes to topic prefix {@code "semantic."} — all meaning-classified
 *       events.</li>
 *   <li>For each event, finds all linked NPCs within 64 blocks (witness radius).</li>
 *   <li>Determines the witness NPC's worldview from ActorType and provenance.</li>
 *   <li>Computes multi-axis relationship deltas based on the semantic tag
 *       and the witness's worldview.</li>
 *   <li>Updates the {@code ActorRelationshipStore} with trust, respect,
 *       fear, familiarity, and grievance deltas.</li>
 * </ul>
 *
 * <h2>Worldview-weighted relationship deltas</h2>
 * <p>The same semantic event produces different relationship changes for
 * different observers:
 * <ul>
 *   <li><b>ACT_OF_MERCY</b> witnessed by a COMMUNAL NPC:
 *       trust+5, respect+3, familiarity+2</li>
 *   <li><b>ACT_OF_MERCY</b> witnessed by a HIERARCHICAL NPC:
 *       trust+1, respect-2 (mercy seen as weakness), familiarity+2</li>
 *   <li><b>ACT_OF_MERCY</b> witnessed by a PREDATORY NPC:
 *       fear+4, respect-3 (interferer is dangerous), familiarity+1</li>
 *   <li><b>ACT_OF_CRUELTY</b> witnessed by a COMMUNAL NPC:
 *       grievance+8, trust-5, fear+3, familiarity+1</li>
 *   <li><b>ACT_OF_CRUELTY</b> witnessed by a HIERARCHICAL NPC:
 *       respect+2 (strength noted), grievance+2, fear+2</li>
 *   <li><b>ACT_OF_CRUELTY</b> witnessed by a PREDATORY NPC:
 *       respect+5, fear+3, familiarity+2</li>
 * </ul>
 *
 * <h2>Design principles</h2>
 * <ul>
 *   <li><b>Article X — Intelligence Is Reasoning:</b> NPCs don't react
 *       to scripts. They observe facts and update their relationship graph.</li>
 *   <li><b>Article XXXIV — Relationships Are Graphs:</b> Each dimension
 *       (trust, respect, fear, familiarity, debt, grievance) updates
 *       independently. Trust can rise while respect falls.</li>
 *   <li><b>Article XLI §4 — Familiarity Is A Relationship Dimension:</b>
 *       Every witnessed event increases familiarity slightly — the NPC
 *       "knows who this person is" from repeated exposure.</li>
 *   <li><b>Article XLI §2 — Simulation Is General:</b> This subscriber
 *       handles ALL NPCs, not just Wang Lin. No character is special-cased.</li>
 *   <li><b>Not a new Engine (Art XXVI):</b> WorldEventSubscriber that
 *       writes to ActorRelationshipStore. No new bus, no new infrastructure.</li>
 * </ul>
 */
public final class NpcSemanticRelationshipSubscriber implements WorldEventSubscriber {

    /** Maximum distance at which an NPC can witness and update relationships. */
    private static final int WITNESS_RADIUS = 64;

    /** Minimum severity for an event to trigger relationship updates. */
    private static final float MIN_SEVERITY = 0.25f;

    @Override
    public String topicPrefix() {
        return "semantic.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        if (event.severity() < MIN_SEVERITY) return;

        String sourceId = event.sourceActorId();
        String targetId = event.targetActorId();
        String semanticTag = event.semanticTag();
        if (semanticTag.isEmpty()) return;

        // The actor the relationship update will be about (the source of the deed).
        String aboutActor = sourceId.isEmpty() ? targetId : sourceId;
        if (aboutActor == null || aboutActor.isEmpty()) return;

        // Find all nearby linked NPCs (witnesses).
        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return;

        int ex = event.pos().getX();
        int ez = event.pos().getZ();
        long distSq = (long) WITNESS_RADIUS * WITNESS_RADIUS;

        ActorRelationshipStore store = ActorRelationshipStore.get(level);
        long tick = level.getGameTime();

        for (Actor witness : ActorRegistry.all()) {
            if (!ActorEntityLink.isLinked(witness.id)) continue;
            if (witness.id.equals(aboutActor)) continue; // don't update relationship with yourself

            // Don't process Wang Lin here — he has his own subscriber with
            // custom canon-faithful weights (WangLinSemanticSubscriber).
            if ("wang_lin".equals(witness.id)) continue;

            // Distance check.
            long dx = witness.blockX - ex;
            long dz = witness.blockZ - ez;
            if (dx * dx + dz * dz > distSq) continue;

            // Determine the witness's worldview.
            Worldview worldview = inferWorldview(witness);

            // Compute multi-axis relationship deltas.
            RelationshipDeltas deltas = computeRelationshipDeltas(
                    event, semanticTag, worldview);

            if (deltas.isEmpty()) continue;

            // Write to the persistent store.
            store.recordMultiAxis(
                    witness.id, aboutActor,
                    deltas.trust, deltas.respect, deltas.fear,
                    deltas.familiarity, deltas.debt, deltas.grievance,
                    "Witnessed: " + event.description(),
                    tick);
        }
    }

    // ─── Worldview inference ─────────────────────────────────────────

    /**
     * A witness's worldview determines HOW they interpret observed deeds.
     * Same enum as BeliefFormationSubscriber — shared worldview taxonomy.
     */
    private enum Worldview {
        /** Values community, compassion, protection. Villagers, elders, mortals. */
        COMMUNAL,
        /** Values power, hierarchy, ambition. Sect cultivators, elders. */
        HIERARCHICAL,
        /** Values self-interest, predation, survival. Bandits, rogue cultivators, beasts. */
        PREDATORY,
        /** Fears cultivators, values safety. Mortals, children, weak NPCs. */
        FEARFUL,
        /** Neutral / unclassified. */
        NEUTRAL
    }

    private Worldview inferWorldview(Actor witness) {
        if (witness.type == ActorType.BEAST) return Worldview.PREDATORY;
        if (witness.provenance != null) {
            String p = witness.provenance.toLowerCase();
            if (p.contains("bandit") || p.contains("rogue")) return Worldview.PREDATORY;
            if (p.contains("elder") || p.contains("sect_head") || p.contains("patriarch")
                    || p.contains("founder") || p.contains("master"))
                return Worldview.HIERARCHICAL;
            if (p.contains("villager") || p.contains("child") || p.contains("farmer"))
                return Worldview.COMMUNAL;
            if (p.contains("mortal")) return Worldview.FEARFUL;
        }
        if (witness.type == ActorType.NPC) return Worldview.HIERARCHICAL;
        return Worldview.NEUTRAL;
    }

    // ─── Relationship delta computation ──────────────────────────────

    /** Container for multi-axis relationship deltas. */
    private record RelationshipDeltas(
            Integer trust, Integer respect, Integer fear,
            Integer familiarity, Integer debt, Integer grievance
    ) {
        boolean isEmpty() {
            return (trust == null || trust == 0)
                    && (respect == null || respect == 0)
                    && (fear == null || fear == 0)
                    && (familiarity == null || familiarity == 0)
                    && (debt == null || debt == 0)
                    && (grievance == null || grievance == 0);
        }
    }

    /**
     * Compute multi-axis relationship deltas based on the semantic tag
     * and the witness's worldview.
     *
     * <p>Core design: every witnessed event increases familiarity slightly
     * (the NPC "saw this person do something"). The other axes vary by
     * worldview — a HIERARCHICAL NPC respects strength but distrusts mercy,
     * while a COMMUNAL NPC trusts mercy but fears cruelty.
     */
    private RelationshipDeltas computeRelationshipDeltas(
            WorldEvent event, String semanticTag, Worldview worldview) {
        // Familiarity: every witnessed event increases familiarity slightly.
        int famDelta = 2; // base familiarity gain for witnessing anything notable

        return switch (semanticTag) {
            case "ACT_OF_MERCY" -> computeMercyDeltas(event, worldview, famDelta);
            case "ACT_OF_CRUELTY" -> computeCrueltyDeltas(event, worldview, famDelta);
            case "PROMISE_MADE" -> computePromiseMadeDeltas(event, worldview, famDelta);
            case "PROMISE_BROKEN" -> computePromiseBrokenDeltas(event, worldview, famDelta);
            case "DEBT_REPAID" -> computeDebtRepaidDeltas(event, worldview, famDelta);
            case "DEBT_IGNORED" -> computeDebtIgnoredDeltas(event, worldview, famDelta);
            case "TECHNIQUE_DISPLAYED" -> computeTechniqueDisplayedDeltas(event, worldview, famDelta);
            case "CULTIVATION_REVEALED" -> computeCultivationRevealedDeltas(event, worldview, famDelta);
            case "PUBLIC_HUMILIATION" -> computePublicHumiliationDeltas(event, worldview, famDelta);
            case "FORBIDDEN_KNOWLEDGE_WITNESSED" -> computeForbiddenKnowledgeDeltas(event, worldview, famDelta);
            case "EXPECTATION_VIOLATION" -> computeExpectationViolationDeltas(event, worldview, famDelta);
            case "GIFT_GIVEN", "GIFT_RECEIVED" -> computeGiftDeltas(event, worldview, famDelta);
            case "COMBAT_ENGAGED" -> computeCombatDeltas(event, worldview, famDelta);
            default -> new RelationshipDeltas(null, null, null, famDelta, null, null);
        };
    }

    // ─── Per-tag delta tables (worldview-weighted) ────────────────────

    private RelationshipDeltas computeMercyDeltas(WorldEvent e, Worldview w, int fam) {
        float severityScale = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(5 * severityScale),   // trust+5: "this person protects the weak"
                    round(3 * severityScale),   // respect+3: "this person has courage"
                    null,                        // fear unchanged
                    fam,                         // familiarity+
                    null, null                   // debt/grievance unchanged
            );
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(1 * severityScale),   // trust+1: "mercy is... acceptable"
                    round(-2 * severityScale),  // respect-2: "mercy is weakness"
                    null,                         // fear unchanged
                    fam, null, null
            );
            case PREDATORY -> new RelationshipDeltas(
                    null,                          // trust unchanged
                    round(-3 * severityScale),     // respect-3: "interferer"
                    round(4 * severityScale),      // fear+4: "this person is dangerous"
                    round(1 * (int) severityScale), // familiarity (smaller — less memorable)
                    null, null
            );
            case FEARFUL -> new RelationshipDeltas(
                    round(3 * severityScale),   // trust+3: "maybe they'll protect us"
                    round(4 * severityScale),   // respect+4: "powerful protector"
                    null,                        // fear: could go either way for fearful
                    fam, null, null
            );
            default -> new RelationshipDeltas(
                    round(2 * severityScale), round(1 * severityScale),
                    null, fam, null, null
            );
        };
    }

    private RelationshipDeltas computeCrueltyDeltas(WorldEvent e, Worldview w, int fam) {
        float severityScale = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-5 * severityScale),  // trust-5: "this person harms others"
                    round(-2 * severityScale),  // respect-2: "unnecessary cruelty"
                    round(3 * severityScale),   // fear+3: "they could hurt us"
                    fam,                         // familiarity+
                    null,
                    round(8 * severityScale)    // grievance+8: "this is unforgivable"
            );
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(-2 * severityScale),  // trust-2: "unpredictable"
                    round(2 * severityScale),  // respect+2: "they have power"
                    round(2 * severityScale),  // fear+2: "dangerous"
                    fam, null,
                    round(2 * severityScale)    // grievance+2: "noted but power commands respect"
            );
            case PREDATORY -> new RelationshipDeltas(
                    null,                          // trust unchanged
                    round(5 * severityScale),      // respect+5: "they are ruthless — good"
                    round(3 * severityScale),      // fear+3: "dangerous"
                    fam, null, null                 // no grievance — predators respect cruelty
            );
            case FEARFUL -> new RelationshipDeltas(
                    round(-6 * severityScale),  // trust-6: "they WILL hurt us"
                    round(-1 * severityScale),  // respect-1
                    round(7 * severityScale),   // fear+7: "terrifying"
                    fam, null,
                    round(6 * severityScale)    // grievance+6: "deep fear"
            );
            default -> new RelationshipDeltas(
                    round(-2 * severityScale), round(1 * severityScale),
                    round(3 * severityScale), fam, null, round(3 * severityScale)
            );
        };
    }

    private RelationshipDeltas computePromiseMadeDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(round(4 * s), round(2 * s), null, fam, null, null);
            case HIERARCHICAL -> new RelationshipDeltas(round(2 * s), round(3 * s), null, fam, null, null);
            case PREDATORY -> new RelationshipDeltas(round(1 * s), round(-1 * s), null, fam, null, null);
            case FEARFUL -> new RelationshipDeltas(round(3 * s), round(1 * s), null, fam, null, null);
            default -> new RelationshipDeltas(round(2 * s), round(1 * s), null, fam, null, null);
        };
    }

    private RelationshipDeltas computePromiseBrokenDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-6 * s), round(-3 * s), null, fam, null, round(10 * s));
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(-5 * s), round(-4 * s), round(2 * s), fam, null, round(8 * s));
            case PREDATORY -> new RelationshipDeltas(
                    round(-1 * s), round(-2 * s), null, fam, null, round(3 * s));
            case FEARFUL -> new RelationshipDeltas(
                    round(-7 * s), round(-2 * s), round(4 * s), fam, null, round(8 * s));
            default -> new RelationshipDeltas(
                    round(-4 * s), round(-2 * s), round(1 * s), fam, null, round(5 * s));
        };
    }

    private RelationshipDeltas computeDebtRepaidDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(round(6 * s), round(4 * s), null, fam, null, null);
            case HIERARCHICAL -> new RelationshipDeltas(round(4 * s), round(3 * s), null, fam, null, null);
            case PREDATORY -> new RelationshipDeltas(round(2 * s), round(1 * s), null, fam, null, null);
            case FEARFUL -> new RelationshipDeltas(round(5 * s), round(3 * s), null, fam, null, null);
            default -> new RelationshipDeltas(round(3 * s), round(2 * s), null, fam, null, null);
        };
    }

    private RelationshipDeltas computeDebtIgnoredDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-5 * s), round(-3 * s), null, fam, round(3 * s), round(7 * s));
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(-4 * s), round(-2 * s), round(1 * s), fam, round(2 * s), round(5 * s));
            case PREDATORY -> new RelationshipDeltas(
                    round(-1 * s), round(1 * s), null, fam, null, round(2 * s));
            case FEARFUL -> new RelationshipDeltas(
                    round(-5 * s), round(-1 * s), round(3 * s), fam, round(2 * s), round(6 * s));
            default -> new RelationshipDeltas(
                    round(-3 * s), round(-1 * s), round(1 * s), fam, round(1 * s), round(4 * s));
        };
    }

    private RelationshipDeltas computeTechniqueDisplayedDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(null, round(3 * s), round(2 * s), fam, null, null);
            case HIERARCHICAL -> new RelationshipDeltas(round(1 * s), round(5 * s), null, fam, null, null);
            case PREDATORY -> new RelationshipDeltas(null, round(3 * s), round(1 * s), fam, null, null);
            case FEARFUL -> new RelationshipDeltas(null, round(2 * s), round(5 * s), fam, null, null);
            default -> new RelationshipDeltas(null, round(2 * s), round(1 * s), fam, null, null);
        };
    }

    private RelationshipDeltas computeCultivationRevealedDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(round(1 * s), round(2 * s), round(3 * s), fam, null, null);
            case HIERARCHICAL -> new RelationshipDeltas(null, round(3 * s), round(-1 * s), fam, null, null);
            case PREDATORY -> new RelationshipDeltas(null, round(1 * s), round(2 * s), fam, null, null);
            case FEARFUL -> new RelationshipDeltas(round(-2 * s), round(1 * s), round(6 * s), fam, null, null);
            default -> new RelationshipDeltas(null, round(1 * s), round(2 * s), fam, null, null);
        };
    }

    private RelationshipDeltas computePublicHumiliationDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-4 * s), round(-3 * s), round(2 * s), fam, null, round(6 * s));
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(-2 * s), round(1 * s), round(1 * s), fam, null, round(3 * s));
            case PREDATORY -> new RelationshipDeltas(
                    null, round(2 * s), round(3 * s), fam, null, null);
            case FEARFUL -> new RelationshipDeltas(
                    round(-5 * s), round(-1 * s), round(5 * s), fam, null, round(7 * s));
            default -> new RelationshipDeltas(
                    round(-2 * s), round(-1 * s), round(2 * s), fam, null, round(3 * s));
        };
    }

    private RelationshipDeltas computeForbiddenKnowledgeDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-2 * s), null, round(3 * s), fam, null, round(3 * s));
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(1 * s), round(3 * s), null, fam, null, null);
            case PREDATORY -> new RelationshipDeltas(
                    null, round(1 * s), round(1 * s), fam, null, null);
            case FEARFUL -> new RelationshipDeltas(
                    round(-3 * s), null, round(5 * s), fam, null, round(4 * s));
            default -> new RelationshipDeltas(
                    round(-1 * s), round(1 * s), round(1 * s), fam, null, round(1 * s));
        };
    }

    private RelationshipDeltas computeExpectationViolationDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(-3 * s), round(-2 * s), round(1 * s), fam, null, round(5 * s));
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(-3 * s), round(-3 * s), round(1 * s), fam, null, round(4 * s));
            case PREDATORY -> new RelationshipDeltas(
                    round(-1 * s), round(-1 * s), null, fam, null, round(2 * s));
            case FEARFUL -> new RelationshipDeltas(
                    round(-4 * s), round(-1 * s), round(3 * s), fam, null, round(5 * s));
            default -> new RelationshipDeltas(
                    round(-2 * s), round(-1 * s), round(1 * s), fam, null, round(3 * s));
        };
    }

    private RelationshipDeltas computeGiftDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(round(3 * s), round(2 * s), null, fam, round(1 * s), null);
            case HIERARCHICAL -> new RelationshipDeltas(round(1 * s), round(3 * s), null, fam, round(1 * s), null);
            case PREDATORY -> new RelationshipDeltas(round(-1 * s), round(1 * s), null, fam, round(2 * s), null);
            case FEARFUL -> new RelationshipDeltas(round(2 * s), round(1 * s), null, fam, round(1 * s), null);
            default -> new RelationshipDeltas(round(1 * s), round(1 * s), null, fam, round(1 * s), null);
        };
    }

    private RelationshipDeltas computeCombatDeltas(WorldEvent e, Worldview w, int fam) {
        float s = Math.min(e.severity(), 1.0f);
        String outcome = e.meta("combat_outcome", "");
        boolean victory = "VICTORY".equals(outcome) || "player_won".equals(outcome);
        int sign = victory ? 1 : -1;
        return switch (w) {
            case COMMUNAL -> new RelationshipDeltas(
                    round(sign * 2 * s), round(sign * 3 * s), round(s * 2), fam, null, null);
            case HIERARCHICAL -> new RelationshipDeltas(
                    round(sign * 1 * s), round(sign * 5 * s), round(s * 1), fam, null, null);
            case PREDATORY -> new RelationshipDeltas(
                    null, round(sign * 4 * s), round(s * 2), fam, null, null);
            case FEARFUL -> new RelationshipDeltas(
                    null, round(sign * 1 * s), round(Math.abs(sign) * 4 * s), fam, null, null);
            default -> new RelationshipDeltas(
                    round(sign * 1 * s), round(sign * 2 * s), round(s * 1), fam, null, null);
        };
    }

    /** Round a float to the nearest integer, clamped to [-100, 100]. */
    private static Integer round(float value) {
        if (value == 0f) return null; // null means "no change"
        int r = Math.round(value);
        return Math.max(-100, Math.min(100, r));
    }
}
