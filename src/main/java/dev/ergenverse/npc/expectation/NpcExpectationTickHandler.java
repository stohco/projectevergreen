package dev.ergenverse.npc.expectation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.npc.cognition.NpcRealmCognition;
import dev.ergenverse.npc.memory.NpcCognitiveMemory;
import dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryCategory;
import dev.ergenverse.npc.memory.NpcCognitiveMemory.MemoryEntry;
import dev.ergenverse.wanglin.ai.reasoning.Prediction;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

/**
 * NpcExpectationTickHandler — Forge event subscriber that drives the
 * generalized NPC Expectation Model (PROJECT_MASTER.md 6.13, 6.13.1).
 *
 * <p>Auto-registered via {@link Mod.EventBusSubscriber} on the FORGE bus.
 *
 * <h2>Event wiring</h2>
 * <ul>
 *   <li><b>LevelEvent.Load</b> — caches NpcExpectationNetwork reference.</li>
 *   <li><b>LevelEvent.Unload</b> — clears cache.</li>
 *   <li><b>ServerTickEvent (END)</b> — runs two periodic cycles:
 *       <ol>
 *         <li><b>Decay</b> every 1200 ticks (1 min): fades old predictions.</li>
 *         <li><b>Inference</b> every 600 ticks (30s): reads NPC memories and
 *             updates predictions based on observed behavior.</li>
 *       </ol>
 *   </li>
 *   <li><b>PlayerInteractEvent.EntityInteract</b> — when a player right-clicks
 *       an EntityCultivator, seeds expectation tracking for that NPC about
 *       the player (if not already tracking).</li>
 * </ul>
 *
 * <h2>Memory-driven inference rules</h2>
 * <p>For each NPC that has recent memories about a target, the inference
 * engine applies category-specific rules:
 * <table>
 *   <tr><th>Memory Category</th><th>Prediction Effect</th></tr>
 *   <tr><td>OBSERVATION (target studying/practicing)</td>
 *       <td>combat_able +0.04, seeking_power +0.03</td></tr>
 *   <tr><td>PLAYER_ACTION (hostile/aggressive)</td>
 *       <td>hostile_intent +0.08, dangerous_if_provoked +0.06</td></tr>
 *   <tr><td>PLAYER_ACTION (peaceful/helpful)</td>
 *       <td>trustworthy +0.05, valuable_ally +0.03</td></tr>
 *   <tr><td>COMBAT (target won against NPC)</td>
 *       <td>combat_able +0.10, dangerous_if_provoked +0.08</td></tr>
 *   <tr><td>COMBAT (target lost to NPC)</td>
 *       <td>combat_able -0.05, dangerous_if_provoked -0.03</td></tr>
 *   <tr><td>SOCIAL (positive interaction)</td>
 *       <td>trustworthy +0.04, will_betray -0.03</td></tr>
 *   <tr><td>SOCIAL (negative interaction)</td>
 *       <td>trustworthy -0.04, will_betray +0.03</td></tr>
 * </table>
 *
 * <h2>Realm-scaled belief (6.6)</h2>
 * <p>The confidence delta from inference is multiplied by
 * {@link NpcRealmCognition#getRumorBelievability(RealmId)}.
 * Higher-realm NPCs are more resistant to forming quick judgments.
 * A mortal (believability=0.8) adjusts at 80% strength;
 * a Soul Formation elder (believability=0.4) adjusts at 40% strength.
 * This prevents high-realm NPCs from being swayed by a single observation.
 *
 * <h2>Interaction seeding</h2>
 * <p>When a player right-clicks an EntityCultivator, the handler checks
 * if the NPC is already tracking the player. If not, it seeds 3 baseline
 * predictions at low confidence (0.15):
 * <ul>
 *   <li>trustworthy: "Unknown stranger — no basis for trust."</li>
 *   <li>dangerous_if_provoked: "Unfamiliar with this person's strength."</li>
 *   <li>valuable_ally: "No data yet."</li>
 * </ul>
 *
 * <h2>Design rule</h2>
 * <blockquote>
 *   Do NOT modify any file outside {@code npc/expectation/}. Integration
 *   with other Phase B systems is read-only (queries existing APIs).
 * </blockquote>
 *
 * @see NpcExpectationNetwork  the world-level SavedData persistence
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class NpcExpectationTickHandler {

    private NpcExpectationTickHandler() {}

    private static volatile NpcExpectationNetwork network = null;

    // ─── Tick intervals ──────────────────────────────────────────────
    /** Decay cycle: every 1200 ticks (1 minute). */
    private static final int DECAY_INTERVAL = 1200;

    /** Inference cycle: every 600 ticks (30 seconds). */
    private static final int INFERENCE_INTERVAL = NpcExpectationNetwork.INFERENCE_INTERVAL_TICKS;

    /** Maximum memories to scan per NPC per inference cycle (performance guard). */
    private static final int MAX_MEMORIES_SCANNED_PER_NPC = 30;

    /** Only process the most recent N ticks of memories for inference. */
    private static final long INFERENCE_MEMORY_WINDOW_TICKS = 24000L * 3; // 3 in-game days

    // ─── Level lifecycle ─────────────────────────────────────────────

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;

        network = NpcExpectationNetwork.get(level);
        Ergenverse.LOGGER.info("[NpcExpectation] Level loaded. Network ready ({} pairs).",
                network.totalPairs());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(net.minecraft.world.level.Level.OVERWORLD)) return;
        if (network != null) {
            Ergenverse.LOGGER.info("[NpcExpectation] Level unloading. {} pairs tracked.",
                    network.totalPairs());
        }
        network = null;
    }

    // ─── Server tick ─────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.getServer() == null) return;

        ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();

        // 1. Decay cycle
        if (ticks % DECAY_INTERVAL == 0) {
            NpcExpectationNetwork net = network;
            if (net != null) {
                int pruned = net.decayTick(ticks);
                if (pruned > 0) {
                    Ergenverse.LOGGER.debug("[NpcExpectation] Decayed: {} predictions pruned.",
                            pruned);
                }
            }
        }

        // 2. Inference cycle
        if (ticks % INFERENCE_INTERVAL == 0) {
            NpcExpectationNetwork net = network;
            if (net == null) return;
            NpcCognitiveMemory memory = NpcCognitiveMemory.get(overworld);
            if (memory == null) return;

            runInference(net, memory, ticks);
        }
    }

    // ─── Player interaction: seed tracking ───────────────────────────

    /**
     * When a player right-clicks an EntityCultivator, ensure the NPC
     * starts tracking that player in the expectation network.
     */
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Entity target = event.getTarget();
        if (!(target instanceof EntityCultivator cultivator)) return;

        String npcId = cultivator.getCharacterId();
        if (npcId == null || npcId.isEmpty()) return;

        String playerUuid = player.getUUID().toString();
        long tick = player.level().getGameTime();

        NpcExpectationNetwork net = network;
        if (net == null) return;

        // Only seed if not already tracking
        if (net.getExpectation(npcId, playerUuid) == null) {
            net.seedPrediction(npcId, playerUuid,
                    NpcExpectationNetwork.PRED_TRUSTWORTHY,
                    "Unknown stranger — no basis for trust.",
                    0.15, tick, "First interaction: " + player.getName().getString());
            net.seedPrediction(npcId, playerUuid,
                    NpcExpectationNetwork.PRED_DANGEROUS_IF_PROVOKED,
                    "Unfamiliar with this person's strength.",
                    0.15, tick, "First interaction: " + player.getName().getString());
            net.seedPrediction(npcId, playerUuid,
                    NpcExpectationNetwork.PRED_VALUABLE_ALLY,
                    "No data yet.",
                    0.10, tick, "First interaction: " + player.getName().getString());

            Ergenverse.LOGGER.debug("[NpcExpectation] Seeded tracking: {} -> player {}",
                    npcId, player.getName().getString());
        }
    }

    // ─── Memory-driven inference engine ──────────────────────────────

    /**
     * Scan activated NPCs' recent memories and update predictions.
     * This is the core of 6.13: "NPCs predict the player and act on predictions."
     */
    private static void runInference(NpcExpectationNetwork net,
                                     NpcCognitiveMemory memory, long currentTick) {
        Set<String> npcIds = net.allNpcIds();
        int inferenceCount = 0;

        for (String npcId : npcIds) {
            NpcCognitiveMemory.NpcMemoryStore store = memory.getStore(npcId);
            if (store == null) continue;

            // Find all targets this NPC has memories about
            // (scan recent short-term and medium-term memories)
            List<MemoryEntry> recentMemories = getRecentMemories(store, currentTick);
            if (recentMemories.isEmpty()) continue;

            // Collect unique target IDs from recent memories
            java.util.Set<String> targets = new java.util.HashSet<>();
            for (MemoryEntry e : recentMemories) {
                if (e.targetId != null && !e.targetId.isEmpty()) {
                    targets.add(e.targetId);
                }
            }

            // Compute realm-scaled belief factor for this NPC
            double beliefFactor = 0.5; // default
            RealmId npcRealm = guessNpcRealm(npcId);
            if (npcRealm != null) {
                beliefFactor = NpcRealmCognition.getRumorBelievability(npcRealm);
            }

            // Process each target's memories
            for (String targetId : targets) {
                int count = inferFromMemories(net, npcId, targetId, recentMemories,
                        currentTick, beliefFactor);
                inferenceCount += count;
            }
        }

        if (inferenceCount > 0) {
            Ergenverse.LOGGER.debug("[NpcExpectation] Inference: {} prediction adjustments.",
                    inferenceCount);
        }
    }

    /**
     * Given a list of memories for one NPC, apply inference rules for
     * memories about a specific target. Returns the number of adjustments made.
     */
    private static int inferFromMemories(NpcExpectationNetwork net, String npcId,
                                         String targetId,
                                         List<MemoryEntry> memories,
                                         long currentTick, double beliefFactor) {
        int adjustments = 0;
        String evidenceBase = "Inferred from " + npcId + "'s memory about " + targetId;

        for (MemoryEntry mem : memories) {
            // Only process memories about this target
            if (!targetId.equals(mem.targetId)) continue;

            double delta;
            String predId;
            String desc;

            switch (mem.category) {
                case OBSERVATION -> {
                    // NPC observed the target studying/practicing/doing something notable
                    String lower = mem.description.toLowerCase();
                    if (lower.contains("attack") || lower.contains("fight")
                            || lower.contains("combat") || lower.contains("cultivat")
                            || lower.contains("breakthrough")) {
                        // Observed cultivation/combat activity
                        delta = 0.04 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_COMBAT_ABLE;
                        desc = "Target observed cultivating/training.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    }
                    if (lower.contains("seek") || lower.contains("gather")
                            || lower.contains("collect") || lower.contains("hoard")) {
                        delta = 0.03 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_SEEKING_POWER;
                        desc = "Target appears to be actively seeking resources.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    }
                }
                case PLAYER_ACTION -> {
                    String lower = mem.description.toLowerCase();
                    if (lower.contains("attack") || lower.contains("kill")
                            || lower.contains("steal") || lower.contains("threaten")
                            || lower.contains("hostile") || lower.contains("aggress")) {
                        // Hostile player action
                        delta = 0.08 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_HOSTILE_INTENT;
                        desc = "Target has shown hostile behavior.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = 0.06 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_DANGEROUS_IF_PROVOKED;
                        desc = "Target is dangerous when provoked.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        // Trust penalty
                        delta = -0.05 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_TRUSTWORTHY;
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    } else if (lower.contains("help") || lower.contains("gift")
                            || lower.contains("save") || lower.contains("protect")
                            || lower.contains("trade") || lower.contains("friendly")) {
                        // Helpful player action
                        delta = 0.05 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_TRUSTWORTHY;
                        desc = "Target has shown helpful/positive behavior.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = 0.03 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_VALUABLE_ALLY;
                        desc = "Target could be a valuable ally.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    }
                }
                case COMBAT -> {
                    String lower = mem.description.toLowerCase();
                    if (lower.contains("won") || lower.contains("victor")
                            || lower.contains("defeated") || lower.contains("stronger")) {
                        // Target won a fight (against this NPC or witnessed)
                        delta = 0.10 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_COMBAT_ABLE;
                        desc = "Target has demonstrated combat ability.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = 0.08 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_DANGEROUS_IF_PROVOKED;
                        desc = "Target is dangerous — proven in combat.";
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    } else if (lower.contains("lost") || lower.contains("fled")
                            || lower.contains("weak") || lower.contains("defeated by")) {
                        // Target lost a fight
                        delta = -0.05 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_COMBAT_ABLE;
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = -0.03 * beliefFactor;
                        predId = NpcExpectationNetwork.PRED_DANGEROUS_IF_PROVOKED;
                        net.adjustConfidence(npcId, targetId, predId, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    }
                }
                case SOCIAL -> {
                    String lower = mem.description.toLowerCase();
                    if (lower.contains("gift") || lower.contains("help")
                            || lower.contains("kind") || lower.contains("gratitude")
                            || lower.contains("thank") || lower.contains("honor")) {
                        // Positive social
                        delta = 0.04 * beliefFactor;
                        net.adjustConfidence(npcId, targetId,
                                NpcExpectationNetwork.PRED_TRUSTWORTHY, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = -0.03 * beliefFactor;
                        net.adjustConfidence(npcId, targetId,
                                NpcExpectationNetwork.PRED_WILL_BETRAY, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    } else if (lower.contains("betray") || lower.contains("deceiv")
                            || lower.contains("lie") || lower.contains("insult")
                            || lower.contains("threat")) {
                        // Negative social
                        delta = -0.04 * beliefFactor;
                        net.adjustConfidence(npcId, targetId,
                                NpcExpectationNetwork.PRED_TRUSTWORTHY, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;

                        delta = 0.03 * beliefFactor;
                        net.adjustConfidence(npcId, targetId,
                                NpcExpectationNetwork.PRED_WILL_BETRAY, delta,
                                currentTick, evidenceBase + ": " + mem.description);
                        adjustments++;
                    }
                }
                default -> {
                    // CULTIVATION, WORLD_EVENT, RUMOR, EMOTIONAL — not direct
                    // prediction drivers for expectations about targets.
                    // (CULTIVATION memories about self don't predict others;
                    //  RUMOR memories feed monologue instead.)
                }
            }
        }
        return adjustments;
    }

    // ─── Helpers ─────────────────────────────────────────────────────

    /**
     * Get recent memories from all tiers, limited by window and count.
     */
    private static List<MemoryEntry> getRecentMemories(
            NpcCognitiveMemory.NpcMemoryStore store, long currentTick) {
        List<MemoryEntry> recent = new java.util.ArrayList<>();
        long cutoff = currentTick - INFERENCE_MEMORY_WINDOW_TICKS;

        // Scan all tiers (long-term is most important, short-term most recent)
        for (MemoryEntry e : store.getLongTerm()) {
            recent.add(e);
        }
        for (MemoryEntry e : store.getMediumTerm()) {
            if (e.timestamp >= cutoff) recent.add(e);
        }
        for (MemoryEntry e : store.getShortTerm()) {
            if (e.timestamp >= cutoff) recent.add(e);
        }

        // Sort by timestamp descending (most recent first) and limit
        recent.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        if (recent.size() > MAX_MEMORIES_SCANNED_PER_NPC) {
            recent = recent.subList(0, MAX_MEMORIES_SCANNED_PER_NPC);
        }
        return recent;
    }

    /**
     * Best-effort guess of an NPC's realm from their character ID.
     * Parses common naming conventions like "nascent_soul_elder_zhang"
     * or falls back to MORTAL. This is a heuristic — in a future version,
     * the actual EntityCultivator's realm could be queried at tick time.
     */
    @Nullable
    private static RealmId guessNpcRealm(String npcId) {
        if (npcId == null || npcId.isEmpty()) return RealmId.MORTAL;
        String lower = npcId.toLowerCase();

        // Try to match realm keywords in the NPC's ID
        if (lower.contains("transcendence") || lower.contains("dao_ancestor"))
            return RealmId.TRANSCENDENCE;
        if (lower.contains("nirvana_fruit") || lower.contains("spirit_seizer"))
            return RealmId.SPIRIT_SEIZER;
        if (lower.contains("soul_transformation") || lower.contains("soul_transform"))
            return RealmId.SOUL_TRANSFORMATION;
        if (lower.contains("ascendant") || lower.contains("he_union"))
            return RealmId.ASCENDANT;
        if (lower.contains("soul_formation") || lower.contains("soul_forming"))
            return RealmId.SOUL_FORMATION;
        if (lower.contains("nascent_soul") || lower.contains("nascent"))
            return RealmId.NASCENT_SOUL;
        if (lower.contains("core_formation") || lower.contains("core_forming"))
            return RealmId.CORE_FORMATION;
        if (lower.contains("foundation"))
            return RealmId.FOUNDATION;
        if (lower.contains("qi_condensation") || lower.contains("qi_condens"))
            return RealmId.QI_CONDENSATION;

        return RealmId.MORTAL;
    }

    // ================================================================
    //  Static convenience API
    // ================================================================

    /** Get the current prediction confidence an NPC holds about a target. */
    public static double getConfidence(String npcId, String targetId, String predictionId) {
        NpcExpectationNetwork net = network;
        return net != null ? net.confidenceOf(npcId, targetId, predictionId) : 0.0;
    }

    /** Whether an NPC has a strong prediction (>= 0.3) about a target. */
    public static boolean hasStrongPrediction(String npcId, String targetId,
                                              String predictionId) {
        return getConfidence(npcId, targetId, predictionId) >= 0.3;
    }

    /** Get top N predictions an NPC holds about a target. */
    public static List<Prediction> getTopPredictions(String npcId, String targetId, int n) {
        NpcExpectationNetwork net = network;
        return net != null ? net.getTopPredictions(npcId, targetId, n) : List.of();
    }

    /** Get all targets an NPC is tracking predictions about. */
    public static Set<String> getTrackedTargets(String npcId) {
        NpcExpectationNetwork net = network;
        return net != null ? net.getTrackedTargets(npcId) : Set.of();
    }

    /** Get all NPCs tracking predictions about a specific target. */
    public static Set<String> getObserversOf(String targetId) {
        NpcExpectationNetwork net = network;
        return net != null ? net.getObserversOf(targetId) : Set.of();
    }

    /**
     * Manually update a prediction (for external systems that want to
     * directly set a prediction, e.g., the rumor system hearing about
     * a player's deeds).
     */
    public static boolean updatePrediction(String npcId, String targetId,
                                           String id, String description,
                                           double confidence, long worldTick,
                                           String evidence) {
        NpcExpectationNetwork net = network;
        return net != null && net.updatePrediction(npcId, targetId, id,
                description, confidence, worldTick, evidence);
    }

    /**
     * Seed a baseline prediction (only if it doesn't exist yet).
     * Used by external systems to initialize NPC expectations from canon profiles.
     */
    public static boolean seedPrediction(String npcId, String targetId,
                                         String id, String description,
                                         double initialConfidence, long worldTick,
                                         String evidence) {
        NpcExpectationNetwork net = network;
        return net != null && net.seedPrediction(npcId, targetId, id,
                description, initialConfidence, worldTick, evidence);
    }
}