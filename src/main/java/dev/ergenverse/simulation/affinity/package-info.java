/**
 * <h1>Layer 2 — Simulation: Affinity System</h1>
 *
 * <p>The interaction mechanic between the player and the protagonist manifestations.
 *
 * <h2>Core philosophy (per user directive)</h2>
 * <pre>
 *   1. Canon originals remain theirs. We do not loot protagonists.
 *   2. The player receives an EXACT COPY of canon items through willing cooperation.
 *      The protagonist keeps their original; the player gets an identical duplicate.
 *   3. Affinity is the PREREQUISITE, not the CURRENCY.
 *      You cannot "spend 50 affinity" to extract Wang Lin's Bead.
 *   4. The manifestation decides to give based on a four-question in-universe evaluation,
 *      not a shop transaction.
 *   5. Different protagonists behave differently (Wang Lin reserved, Meng Hao bargains,
 *      Bai Xiaochun gives freely, Su Ming tests comprehension, Xu Qing judges in silence).
 * </pre>
 *
 * <h2>Why this is Layer 2, not Layer 1</h2>
 * <p>Er Gen never wrote about "manifestation companions" or "affinity systems."
 * These are the game's DESIGN for how a second protagonist (the player) can
 * legitimately access iconic canon content without invalidating the original
 * history. The canon originals remain Wang Lin's; the player receives an exact
 * copy through willing cooperation. This is a simulation rule, not a canon fact.
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link ManifestationGiftSystem} — the gift mechanic, four-question
 *       evaluation engine, 5 protagonist personality profiles, gift records.</li>
 *   <li>{@link BridgingPolicy} — the 5-tier canon-vs-generated policy gatekeeper.
 *       Determines what canon leaves silent vs. what can be intelligently generated.</li>
 * </ul>
 *
 * <h2>Relationship to Layer 3 (Emergent History)</h2>
 * <p>When the Affinity System grants an item to the player, that grant is recorded
 * in {@link dev.ergenverse.history.PlayerHistory} — it is emergent game history,
 * not retroactive canon. The player's copy of Wang Lin's bead is a NEW object
 * with its own history, not the original.
 */
package dev.ergenverse.simulation.affinity;
