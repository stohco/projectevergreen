package dev.ergenverse.simulation.event;

/**
 * WorldEventSubscriber — the subscriber interface for the WorldEventBus.
 *
 * <p>Per the ChatGPT architectural review: "build one enormous event
 * network." Every system that cares about world disturbances implements
 * this interface and registers with {@link WorldEventBus#subscribe}.
 *
 * <p>A subscriber declares:
 * <ul>
 *   <li><b>topicPrefix</b> — which topics it cares about. The bus only
 *       delivers events whose topic starts with this prefix.
 *       e.g. "opportunity." catches all opportunity events,
 *       "npc.breakthrough" catches only breakthrough events.</li>
 *   <li><b>onEvent</b> — the handler. Called synchronously on the server
 *       thread when a matching event is dispatched. MUST be fast
 *       (sub-millisecond) — no blocking I/O, no heavy computation.
 *       If work is needed, queue it and process next tick.</li>
 * </ul>
 *
 * <p><b>Constitution compliance:</b>
 * <ul>
 *   <li>Art III (Simulation Before Progression) — subscribers react to
 *       world state, never to player progression.</li>
 *   <li>Art V (Everything Exists Without The Player) — subscribers fire
 *       whether or not a player is nearby. LOD is handled by the bus,
 *       not the subscriber.</li>
 *   <li>Art XVI (Build Systems Not Features) — one subscriber interface,
 *       infinite uses. BeastPanic, CultivatorPerception, RumorSpread,
 *       HistoryLedger, EcologyShift all implement this same interface.</li>
 * </ul>
 */
public interface WorldEventSubscriber {

    /**
     * The topic prefix this subscriber cares about.
     * The bus delivers events whose topic starts with this string.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "opportunity."} — all opportunity events</li>
     *   <li>{@code "npc."} — all NPC events</li>
     *   <li>{@code "beast."} — all beast events</li>
     *   <li>{@code ""} — ALL events (use sparingly; only for HistoryLedger)</li>
     * </ul>
     */
    String topicPrefix();

    /**
     * Handle a dispatched event. Called on the server thread.
     *
     * @param event the event that was published
     */
    void onEvent(WorldEvent event);
}
