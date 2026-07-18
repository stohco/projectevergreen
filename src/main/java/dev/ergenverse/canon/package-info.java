/**
 * <h1>Layer 1 — Canon Reconstruction</h1>
 *
 * <p><b>Immutable. Exactly what Er Gen wrote. Nothing more.</b>
 *
 * <p>This package and its subpackages contain ONLY canon-attested facts from the
 * Er Gen novels. Every item, technique, beast, character, location, realm, and
 * event here carries a {@link dev.ergenverse.canon.Provenance} recording its
 * source novel, chapter(s), whether it is EXPLICIT or INFERRED, a confidence
 * rating (1-5), and any known ambiguities.
 *
 * <h2>The Prime Directive (rewritten for the project)</h2>
 * <blockquote>
 *   The Er Gen novels are the specification. The game is an implementation of
 *   that specification. If canon is silent, do not invent mechanics during the
 *   reconstruction phase. Record the silence as a gap. Gaps are filled only
 *   after the canon layer is complete, and every gap-filled addition must be
 *   clearly marked as inferred rather than canonical.
 * </blockquote>
 *
 * <h2>What belongs here</h2>
 * <ul>
 *   <li>The canonical state histories of Wang Lin's arsenal items (each state
 *       tied to a specific canon event — an <i>event-driven Historical State
 *       Machine</i>, NOT an XP upgrade chain).</li>
 *   <li>Canon character data, canon locations, canon timeline, canon ecology.</li>
 *   <li>The {@link dev.ergenverse.canon.Provenance} record attached to every fact.</li>
 * </ul>
 *
 * <h2>What does NOT belong here</h2>
 * <ul>
 *   <li><b>Simulation rules</b> (cultivation mechanics, affinity, NPC AI, ecology
 *       ticking, world persistence) — those live in {@link dev.ergenverse.simulation}.</li>
 *   <li><b>Manifestation Gift / Bridging Policy</b> — these are Simulation-layer
 *       concepts. They have been MOVED to {@link dev.ergenverse.simulation.affinity}.
 *       They must NEVER be referenced from the Canon layer.</li>
 *   <li><b>Emergent game history</b> (player refinements, new techniques invented
 *       in-play, beast mutations, sect rises) — those live in {@link dev.ergenverse.history}.</li>
 * </ul>
 *
 * <h2>The "No Locked Upgrades" directive, rewritten</h2>
 * <p>The old directive ("no locked upgrades, every item fully upgradeable to peak")
 * has been REPLACED with the architecturally honest formulation:
 * <blockquote>
 *   Every canonical state must be obtainable.
 * </blockquote>
 * <p>If an item has 3 canon-documented states, the player must eventually be able
 * to experience all 3. If it has 1 canon state, that is the complete item —
 * nothing is missing. We do NOT invent "Peak" / "Awakened" / "Ascended" stages
 * that Er Gen never wrote. The registry is a historical record, not a
 * progression guide.
 *
 * <h2>Historical State Machine (not Upgrade Chain)</h2>
 * <p>Er Gen's artifacts change because of <i>specific events</i>, not XP:
 * <pre>
 *   Event: Ji Realm absorbed
 *     ↓
 *   Bead changes
 *     ↓
 *   Capabilities change
 * </pre>
 * Not:
 * <pre>
 *   XP reached → Upgrade
 * </pre>
 * <p>The {@link dev.ergenverse.wanglin.ItemEvolutionChain} (retained name for
 * Forge registration compatibility) is therefore an ordered list of
 * canon-documented states, each tied to a canon event via its {@code Provenance}.
 * Most arsenal items have exactly ONE canon state (single-stage history). Only
 * items with DOCUMENTED transformations (e.g. Core-Treasure Sword → Dark Green
 * Flying Sword, Soul Lasher + Karma Domain → Karma Whip, Fragment Stamp →
 * 18-Hell Celestial Sealing Stamp) have multi-state histories.
 */
package dev.ergenverse.canon;
