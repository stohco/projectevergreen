/**
 * <h1>Layer 2 — Simulation Rules</h1>
 *
 * <p><b>How the universe functions. Mostly design, grounded in canon.</b>
 *
 * <p>This layer contains the RULES that govern how the reconstructed canon world
 * operates at runtime. These rules are the game's design, but they must never
 * contradict Layer 1 (Canon). Where canon specifies a mechanic, the simulation
 * implements it faithfully. Where canon is silent, the simulation may define a
 * rule, but that rule is clearly marked as design, not canon.
 *
 * <h2>What belongs here</h2>
 * <ul>
 *   <li><b>{@link dev.ergenverse.simulation.affinity}</b> — the Affinity System
 *       (moved here from the canon layer). This includes:
 *       <ul>
 *         <li>{@link dev.ergenverse.simulation.affinity.ManifestationGiftSystem} —
 *             the interaction mechanic between the player and protagonist
 *             manifestations. This is where Wang Lin's manifestation companion
 *             grants items, evolution insights, and lore.</li>
 *         <li>{@link dev.ergenverse.simulation.affinity.BridgingPolicy} — the
 *             5-tier canon-vs-generated policy gatekeeper. Determines what canon
 *             leaves silent vs. what can be intelligently generated.</li>
 *       </ul>
 *   </li>
 *   <li><b>Cultivation mechanics</b> — qi, breakthrough events, tribulation
 *       (to be built in {@code dev.ergenverse.simulation.cultivation}).</li>
 *   <li><b>NPC AI</b> — goal-based planner (Goals/Needs/Resources/Fears/Knowledge/
 *       Relationships → planner → actions). To be built.</li>
 *   <li><b>World persistence</b> — how the world state is saved and evolves
 *       independently of the player (the World Pulse).</li>
 *   <li><b>Trading, economy, ecology ticking</b> — the runtime loops.</li>
 * </ul>
 *
 * <h2>What does NOT belong here</h2>
 * <ul>
 *   <li><b>Canon facts</b> — those live in {@link dev.ergenverse.canon} (Layer 1).</li>
 *   <li><b>Player-driven emergent history</b> — that lives in
 *       {@link dev.ergenverse.history} (Layer 3).</li>
 * </ul>
 *
 * <h2>Relationship to the Affinity System</h2>
 * <p>The user's directive reframes the Manifestation Gift concept:
 * <blockquote>
 *   "As another protagonist, through enough trust, I can eventually receive
 *   access to everything in the canonical protagonists' arsenals without
 *   depriving them of the originals."
 * </blockquote>
 * <p>This is a Simulation-layer rule, NOT a canon fact. Er Gen never wrote about
 * "manifestation companions" or "affinity systems" — these are the game's design
 * for how a SECOND protagonist (the player) can legitimately access iconic canon
 * content without invalidating the original history. The canon originals remain
 * Wang Lin's; the player receives an EXACT COPY through willing cooperation.
 *
 * <h2>One-time consumed inheritances — the unresolved rule</h2>
 * <p>The user identified one category that needs a consistent lore rule: unique,
 * one-time world events that were truly consumed forever in the novel (e.g. an
 * ancient inheritance Wang Lin absorbed, with no indication it can be taught,
 * recreated, or shared). The Affinity System is the layer where this rule will
 * be defined — not by rewriting the past, but by defining how the multiverse
 * allows another protagonist to legitimately obtain equivalents or successors
 * without invalidating the original history. This is recorded as a gap.
 */
package dev.ergenverse.simulation;
