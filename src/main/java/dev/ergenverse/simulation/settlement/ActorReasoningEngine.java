package dev.ergenverse.simulation.settlement;

/**
 * ActorReasoningEngine — a <b>thin dispatcher</b> that delegates reasoning to
 * the actor's {@link CultivatorMind}.
 *
 * <p>Per the user's architectural directive (the cycle after the prior
 * reasoning engine):
 * <blockquote>
 * I still don't think ActorReasoningEngine should ultimately own reasoning.
 * ... The actor should reason. Not the reasoning engine.
 * <br><br>
 * Instead of ReasoningEngine → switch(profile), I'd rather see
 * Actor → evaluate(WorldSituation). Because Wang Lin is not a profile.
 * He's an individual.
 * <br><br>
 * Nobody special-cases Wang Lin. Nobody special-cases Li Muwan. The reasoning
 * emerges from what they care about.
 * </blockquote>
 *
 * <p>This class is now a <b>thin static dispatcher</b>. It does NOT contain any
 * per-role logic, any {@code switch(profile)}, or any "if Wang Lin." All of
 * that has moved to the {@link CultivatorMind}, which owns the motivation
 * weights and the candidate-scoring logic. The engine's only job is to look
 * up the actor's mind and call {@code mind.evaluate(situation, settlement)}.
 *
 * <h2>Why the engine still exists</h2>
 * <p>The {@link ActorMaterializer} calls a static {@code reason()} method per
 * actor per scan. Keeping this thin dispatcher preserves that call site while
 * routing the work to the mind. The class could be inlined into the
 * materializer, but retaining it makes the reasoning boundary explicit in the
 * call stack (easier to audit). It is deliberately stateless and logic-free.
 *
 * <h2>The architectural lineage</h2>
 * <ol>
 *   <li><b>CRON-28:</b> {@code SettlementThreatIndex} → hardcoded "if threat,
 *       everyone home" in {@code ActorPresence}. Everyone reacted identically.</li>
 *   <li><b>REASONING-ENGINE-29:</b> {@code ActorReasoningEngine} with
 *       {@code switch(profile)} — different minds, but still scripted per-role.</li>
 *   <li><b>This cycle (CULTIVATOR-MIND-30):</b> {@code CultivatorMind.evaluate()}
 *       scores candidates against per-actor motivation weights. Nobody wrote
 *       "if Wang Lin." The decision emerges from what each actor cares about.</li>
 * </ol>
 *
 * <p>Per the user: "That's a huge difference. Every decision should answer ONE
 * question: not 'What should I do?' but 'What future am I trying to create?'"
 * The mind's scoring answers that question — each candidate's score is how
 * much it creates the future the actor's motivations point toward.
 */
public final class ActorReasoningEngine {

    private ActorReasoningEngine() {}

    /**
     * Dispatch the reasoning to the actor's {@link CultivatorMind}.
     *
     * <p>This method contains NO per-actor logic. It looks up the mind and
     * delegates. If the actor has no registered mind, it returns {@code null}
     * (the daily rhythm takes over — a safe default for unregistered actors).
     *
     * @param actorId    the actor's id
     * @param situation  the shared world situation (same for everyone)
     * @param settlement the actor's settlement (for home/location resolution)
     * @return the mind's chosen Activity, or null if the daily rhythm should
     *         take over (peaceful situation, no mind, or no candidate scored
     *         above zero)
     */
    public static Activity reason(String actorId, WorldSituation situation,
                                  Settlement settlement) {
        if (actorId == null || situation == null || settlement == null) return null;

        CultivatorMind mind = CultivatorMindRegistry.get(actorId);
        if (mind == null) {
            // No mind registered — the actor has no motivations to reason with.
            // The daily rhythm takes over. (All canon/simulation NPCs should
            // have minds; this is a safe fallback for any unregistered actor.)
            return null;
        }

        return mind.evaluate(situation, settlement);
    }
}
