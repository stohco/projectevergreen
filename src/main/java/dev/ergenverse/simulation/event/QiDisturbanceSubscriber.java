package dev.ergenverse.simulation.event;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorTickLoop;

import net.minecraft.core.BlockPos;

import java.util.Collection;

/**
 * QiDisturbanceSubscriber — the FIRST real WorldEventBus subscriber.
 *
 * <p>Per the ENDGAME PROOF: "qi fluctuations propagate via event bus"
 * → "nearby cultivators independently detect via their perception."
 *
 * <p>This subscriber listens to all {@code "opportunity."} events and
 * does ONE thing: marks all Actors within the event's energy radius as
 * "dirty" so the {@link ActorTickLoop} will re-evaluate their cognition
 * on the next tick (event-driven path, not seasonal).
 *
 * <p>This is the CRITICAL glue between the WorldEventBus (the world's
 * nervous system) and the ActorTickLoop (the cognition pipeline). Without
 * this subscriber, events flow into WorldHistory (for record-keeping) but
 * never trigger NPCs to re-think. The IntentEngine's new
 * {@code situationalModifier()} would never see fresh events because no
 * actors would re-tick between seasonal passes (every 7 MC days).
 *
 * <h2>How it works</h2>
 * <ol>
 *   <li>A SpiritFruitTimeline milestone fires (e.g. +6h wolf arrives).</li>
 *   <li>SpiritFruitTimeline calls {@code WorldEventBus.publish("opportunity.spirit_fruit.predator_approaching",
 *       EnergyType.ACQUIRE, pos, 0.5f, ...)}.</li>
 *   <li>WorldEventBus.dispatch() delivers to ALL matching subscribers.</li>
 *   <li>This subscriber receives the event. It iterates all Actors
 *       within the event's energy radius (ACQUIRE = 256 blocks).</li>
 *   <li>For each nearby actor, it calls {@link ActorTickLoop#markDirty}
 *       with the current tick. The ActorTickLoop's event-driven path will
 *       pick these actors up on the next tick.</li>
 *   <li>The ActorTickLoop calls {@code tickFullCognition} →
 *       DecisionEngine → IntentEngine (whose situationalModifier now
 *       queries WorldHistory and finds the QI/ACQUIRE event) → produces
 *       SEEK_OPPORTUNITY intent → CognitionDrivenGoal → NPC moves toward
 *       the event.</li>
 * </ol>
 *
 * <p>This is "one enormous event network. No quests. No scripts. Just
 * consequences." — the ChatGPT architectural review's #1 directive, now
 * actually working end-to-end.
 *
 * <h2>Registration</h2>
 * <p>Registered in {@link dev.ergenverse.core.Ergenverse} during server
 * start. The bus is empty on world load (WorldEventBus.clearAll), so
 * subscribers must be re-registered each session.
 *
 * <p><b>Provenance: INFERRED.</b> The event-driven actor re-tick pattern
 * is standard simulation architecture. The radius-based proximity check
 * is inferred from the EnergyType radii defined in AUTO-COGNITION-010.
 */
public final class QiDisturbanceSubscriber implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        // Listen to ALL opportunity events (spirit fruit, restriction caves,
        // artifact discoveries, etc.). The prefix "opportunity." catches
        // every event that starts with "opportunity.".
        return "opportunity.";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null || event.isGlobal()) return;

        BlockPos pos = event.pos();
        int radius = event.energyType().radius();

        // Find all actors within the event's energy radius.
        // This is the "perception" check — can this actor sense the event?
        Collection<Actor> nearby = ActorRegistry.withinDistance(
                pos.getX(), pos.getY(), pos.getZ(), radius);

        if (nearby.isEmpty()) return;

        long currentTick = event.timestamp();

        // Mark each nearby actor as dirty — the ActorTickLoop will re-tick them
        // on the next server tick via its event-driven path.
        int marked = 0;
        for (Actor a : nearby) {
            // Only re-tick actors that have cognition (NPCs, beasts with intelligence)
            if (a.cognition == null) continue;
            // Only re-tick actors at ACTIVE_ACTOR or above — STATIC_DATA and
            // HISTORICAL actors don't think, and TERRITORY actors are aggregated.
            if (a.simLevel.order < dev.ergenverse.simulation.los.SimulationLevel.ACTIVE_ACTOR.order) continue;

            ActorTickLoop.markDirty(a, currentTick);
            marked++;
        }

        if (marked > 0) {
            Ergenverse.LOGGER.debug("[QiDisturbance] {} event at ({},{}) — marked {} actors for re-tick (radius={})",
                    event.topic(), pos.getX(), pos.getZ(), marked, radius);
        }
    }
}