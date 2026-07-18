/**
 * <h1>WangLinMasterRegistry — the Complete Canonical Knowledge Graph</h1>
 *
 * <p><b>This is NOT a whitelist.</b> It is a complete canonical knowledge graph that
 * expands every top-level node of Wang Lin's universe (Cultivation, Body cultivation,
 * Soul cultivation, Restrictions, Formations, Puppets, Alchemy, Refining, Flying swords,
 * Storage treasures, Ancient God, Ancient Dao, Ancient Clan, Ancient Demon, Ancient
 * Devil, Ji Realm, Essences, Domains, Avatars, Clones, Divine Sense, Space, Time,
 * Karma, Life and Death, Samsara, Slaughter, Celestial techniques, Underworld
 * techniques, Origin Energy, Third Step, Fourth Step, Inventory, Techniques, Pets,
 * Companions, Knowledge, Enemies, Allies, Realms, History) into a tree of concrete
 * canon entities — every Wang Lin–attested item, technique, beast, companion, avatar,
 * inheritance, and treasure.
 *
 * <h2>Architecture</h2>
 * <p>Every canonical entity is a {@link
 * dev.ergenverse.wanglin.registry.CanonicalEntry} record carrying:
 * <ul>
 *   <li><b>Identity</b> — id, displayName, nameCn (where attested)</li>
 *   <li><b>Category</b> — a {@link dev.ergenverse.wanglin.registry.CanonicalCategory}
 *       mapping to the knowledge-graph node it belongs to</li>
 *   <li><b>Provenance</b> — {@link dev.ergenverse.canon.Provenance} with
 *       sourceNovel, chapters, EXPLICIT/INFERRED attestation, confidence 1..5,
 *       and ambiguities (the same record used everywhere else in the mod)</li>
 *   <li><b>Ownership state</b> — {@link dev.ergenverse.wanglin.registry.OwnershipState}:
 *       WANG_LIN_OWNED, FORMERLY_OWNED, ENCOUNTERED, WITNESSED_IN_USE, HEARD_OF_ONLY,
 *       INHERITED_THEN_LOST</li>
 *   <li><b>Transferability</b> — {@link dev.ergenverse.wanglin.registry.Transferability}:
 *       canTeach, canDemonstrate, canExplain, canTransfer, canGiftEquivalent, canCreateNew,
 *       each with a canonBasis citing the chapter(s) that justify the verdict</li>
 *   <li><b>Demonstrability</b> — {@link dev.ergenverse.wanglin.registry.Demonstrability}:
 *       whether Wang Lin can show it, explain it, or only attest to it</li>
 *   <li><b>canonSummary</b> — what the novels say it IS</li>
 *   <li><b>canonDemonstratedBehaviors</b> — what the novels show it DOING</li>
 *   <li><b>interactionTags</b> — tags like "soul", "formation", "divine_sense" used
 *       to walk the graph (every piece of knowledge interacts with every other)</li>
 * </ul>
 *
 * <h2>The Teaching Resolver — the pipeline the user demanded</h2>
 * <p>The {@link dev.ergenverse.wanglin.registry.TeachingResolver} walks:
 * <pre>
 *   Technique requested
 *     → look up CanonicalEntry
 *     → check OwnershipState (must be WANG_LIN_OWNED or INHERITED_THEN_LOST)
 *     → check Transferability.canTeach
 *     → evaluate WangLinPersonality (mood, current goal)
 *     → evaluate WangLinRelationships (player affinity, trust level)
 *     → produce TeachingResult { outcome, reason, canonBasis, simulationNotes }
 * </pre>
 * The resolver delegates canon-grounded teaching decisions to the existing
 * {@link dev.ergenverse.wanglin.ai.WangLinTeachingPolicy#canTeach} engine (which
 * already implements the four canon gates: bead-binding, Situ Nan contract,
 * Restriction Mountain patience trial, "reminds him of his younger self" open
 * door). The resolver wraps the policy's result with provenance + simulation
 * notes pulled from the {@link CanonicalEntry}.
 *
 * <h2>Sub-registries</h2>
 * <p>The graph is partitioned into 18 sub-registries, each owning its slice of
 * the canon. All are bootstrapped in a single call to
 * {@link dev.ergenverse.wanglin.registry.WangLinMasterRegistry#bootstrap()}:
 * <ul>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalInventory} — Wang Lin's
 *       owned artifacts (cross-references the 178 {@code
 *       RICanonicalDatabase} artifacts + 175 wiki items)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalTechniques} — every
 *       technique Wang Lin learned, created, or inherited</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalKnowledge} — abstract
 *       knowledge (Dao comprehension, world-truths, lore Wang Lin attests to)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalPets} — Wang Lin's
 *       tamed/bred beasts (delegates to {@code dev.ergenverse.wanglin.pet}
 *       for the full data model)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalCompanions} — devils,
 *       humanoids, contracted allies (Xu Liguo etc.)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalAvatars} — Wang Lin's
 *       12+ clones / true bodies (Cultivator Clone, Void Avatar, Lu Mo, …)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalBodies} — body-cultivation
 *       states (Ancient God 1★ → 27★, Five Elements True Body, etc.)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalRestrictions} — Wang Lin's
 *       4 Great Restrictions + Restriction Flag system</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalFormations} — Wang Lin's
 *       formation-array treasures and self-erected formations</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalEssences} — the 14 Essences
 *       of Wang Lin's Samsara Dao (cross-refs {@code SamsaraDao.Essence})</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalDao} — Wang Lin's Dao
 *       comprehension track (Samsara Dao → Reincarnation Essence → Heaven Trampling)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalTitles} — titles &
 *       honorifics (6th-Gen Vermilion Bird Emperor, Tenth Sun, Great Enlightened One, …)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalSkills} — discrete skills
 *       Wang Lin demonstrated (Soul Refining, Alchemy, Refining, …)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalExperiences} — defining
 *       life experiences (Teng Clan annihilation, 7 years at Restriction
 *       Mountain, 13 years at Reincarnation Pool, …)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalEnemies} — Wang Lin's
 *       antagonists (delegates to {@code WangLinAntagonists} for the full structure)</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalAllies} — Wang Lin's
 *       allies / mentors (cross-refs {@code WangLinRelationships})</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalRealms} — the cosmology
 *       layers (cross-refs {@code WangLinCosmology.Layer})</li>
 *   <li>{@link dev.ergenverse.wanglin.registry.CanonicalHistoricalEvents} — Wang Lin's
 *       life events (cross-refs {@code RITimelineEngine})</li>
 * </ul>
 *
 * <h2>Provenance on everything</h2>
 * <p>Every entry cites its source. Per the Prime Directive: <blockquote>Never
 * fill empty space with generic fantasy content. Reality is objective;
 * cultivation changes understanding, not existence.</blockquote> Where canon is
 * silent on a detail, the field is marked UNKNOWN or omitted — never invented.
 *
 * <h2>Three-Layer Architecture</h2>
 * <p>This package lives in <b>Layer 1 — Canon</b>. It is immutable and is
 * never back-written by Simulation or Emergent-History events. When a player
 * acquires an exact copy of a canon item (per the user's "exact copy"
 * directive), that fact is recorded in Layer 3 — Emergent History, never here.
 */
package dev.ergenverse.wanglin.registry;
