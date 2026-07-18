/**
 * <h1>Layer 3 — Emergent History</h1>
 *
 * <p><b>Everything that happens after the player enters the world.</b>
 *
 * <p>This layer records the NEW history created by the player's presence and
 * actions. It is separate from Layer 1 (Canon) and Layer 2 (Simulation Rules).
 * Nothing in this layer is back-written into canon. The player is an ADDITIONAL
 * protagonist creating their own legend, not a replay of Wang Lin's story.
 *
 * <h2>What belongs here</h2>
 * <ul>
 *   <li><b>{@link dev.ergenverse.history.PlayerHistory}</b> — the player's personal
 *       history: breakthroughs achieved, gifts received, NPCs killed, discoveries
 *       made, techniques learned, karmic actions. Persisted per-player in
 *       WorldRuntimeState. Max 500 entries per player.</li>
 *   <li><b>{@link dev.ergenverse.history.WorldHistory}</b> — changes to the world
 *       caused by the player AND by canon consequences. Seeded at t0 with 9
 *       canon-consequence events (Zhao Country, Heng Yue Sect, Heavenly Fate
 *       Sect, Soul Refining Sect, Corpse Yin Sect, Teng Family, Wang Family
 *       Village, Tian Shui City, Planet Suzaku). Global singleton, ticked
 *       daily from Ergenverse.onServerTick. Max 2000 global events.</li>
 *   <li><b>{@link dev.ergenverse.history.NpcMemory}</b> — what each NPC remembers
 *       about the player: interactions, combat outcomes, gifts, favors. Trust
 *       scoring with time-based decay. Max 50 memories per NPC, 200 NPCs tracked.</li>
 *   <li><b>{@link dev.ergenverse.history.RelationshipHistory}</b> — the
 *       affinity/relationship timeline between the player and each canon
 *       protagonist manifestation. Tracks trust earned, gifts exchanged,
 *       shared experiences. Max 100 events per protagonist.</li>
 *   <li><b>{@link dev.ergenverse.history.HistoryManager}</b> — the cross-system
 *       wiring layer. Static hook methods called by CultivationEvents,
 *       ManifestationGiftHandler, EntityCultivator, and PerceptionEvents.</li>
 * </ul>
 *
 * <h2>Active wiring points (events that auto-record history)</h2>
 * <ul>
 *   <li>Cultivation breakthrough → PlayerHistory + WorldHistory (if notable realm)</li>
 *   <li>Perception tier shift → PlayerHistory (DISCOVERY)</li>
 *   <li>Manifestation gift received → PlayerHistory + RelationshipHistory + NpcMemory + WorldHistory</li>
 *   <li>NPC right-click interaction → NpcMemory (INTERACTION)</li>
 *   <li>NPC killed by player → NpcMemory (COMBAT) + PlayerHistory (KILL) + WorldHistory (if notable NPC)</li>
 * </ul>
 *
 * <h2>Player command</h2>
 * <p>{@code /ergen history} — view personal history, world events, NPC memories,
 * relationship status, and system statistics. See
 * {@link dev.ergenverse.command.HistoryCommand}.
 *
 * <h2>The two-timeline principle</h2>
 * <pre>
 *   Canon Timeline (immutable, Layer 1):
 *     Sword exists in one form (Er Gen wrote it that way)
 *
 *   Game History (emergent, Layer 3):
 *     Player refines sword → sword evolves
 *     (This is new history, not retroactive canon)
 * </pre>
 *
 * <h2>Status</h2>
 * <p>v1 COMPLETE. All four data classes have full NBT serialization via
 * WorldRuntimeState (SavedData). HistoryManager wires into 5 event types.
 * WorldHistory ticks daily. HistoryCommand provides in-game inspection.
 * v2 will add: NPC dialogue-driven history, ecology shift recording,
 * faction succession tracking, and RITimelineEngine integration for
 * canon-consequence advancement.
 */
package dev.ergenverse.history;