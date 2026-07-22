package dev.ergenverse.simulation.event;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.WorldChronicle;
import net.minecraft.server.level.ServerLevel;

/**
 * ChronicleSubscriber — the bridge from the WorldEventBus to the WorldChronicle.
 *
 * <p>This subscriber receives ALL events on the bus (topicPrefix {@code ""})
 * and compiles every historically-notable one into a permanent prose entry
 * in the {@link WorldChronicle}. It is the mechanism by which "the simulation
 * writes its own history."
 *
 * <p><b>Severity gate:</b> only events with severity ≥
 * {@link WorldEventBus#LEDGER_SEVERITY_THRESHOLD} are chronicled — the same
 * threshold that gates write-through to {@link dev.ergenverse.history.WorldHistory}.
 * A beast wandering 10 blocks (severity 0.05) is not chronicle-worthy; a sect
 * war (severity 0.9) is.
 *
 * <p><b>Not a new Engine (Art XXVI):</b> this is a {@link WorldEventSubscriber}
 * — the same pattern as {@link QiDisturbanceSubscriber} and
 * {@link ActivityInterruptionSubscriber}. It uses the existing bus and the
 * existing WorldChronicle. No new infrastructure.
 *
 * <p><b>Constitution compliance:</b> Art III, Art V, Art XVI, Art XLII (Layer 3),
 * Art XLIII (the chronicle is one of the single-player maximalism features —
 * saves are historical records, not state snapshots).
 */
public final class ChronicleSubscriber implements WorldEventSubscriber {

    @Override
    public String topicPrefix() {
        // Catch-all: the chronicle is the world's historian. It hears everything
        // notable, regardless of topic. The severity gate (in onEvent) filters
        // out the noise.
        return "";
    }

    @Override
    public void onEvent(WorldEvent event) {
        if (event == null) return;
        // Only chronicle historically-notable events. A wolf shifting 10 blocks
        // is not a chronicle entry; a patriarch's breakthrough is.
        if (event.severity() < WorldEventBus.LEDGER_SEVERITY_THRESHOLD) return;

        ServerLevel level = WorldEventBus.currentLevel();
        if (level == null) return;

        try {
            WorldChronicle.recordBusEvent(level, event);
        } catch (Exception e) {
            // A chronicle write failure MUST NOT break the bus or other subscribers.
            Ergenverse.LOGGER.error("[ChronicleSubscriber] failed to record event {}", event.topic(), e);
        }
    }
}
