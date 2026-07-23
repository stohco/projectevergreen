package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.simulation.WorldRuntimeState;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.opportunity.OpportunityRegistry;
import dev.ergenverse.simulation.opportunity.OpportunityState;
import dev.ergenverse.simulation.settlement.WorldSituation.OpportunitySnapshot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * ActorMaterializer — the <b>INVERTED renderer query</b>.
 *
 * <p>Per Article XLIV (the user's directive):
 * <blockquote>
 * Wang Lin should never "spawn". When you load a save, does Wang Lin suddenly
 * pop into existence? No. He already existed before you loaded. He was already
 * cultivating. Already thinking. Already planning. Already somewhere.
 * Minecraft is simply catching up to reality. The renderer asks: "Which actors
 * currently intersect loaded chunks?" Those become entities.
 * </blockquote>
 *
 * <p>This is the replacement for the spawn-driven
 * {@code ReificationScan} model. The flow is:
 * <pre>
 *   Settlement owns population
 *     → each actor has a current presence (derived from their life)
 *       → renderer materializes those intersecting loaded chunks
 *         → Minecraft renders them
 *           → dematerialize when distant (keep simulating)
 * </pre>
 *
 * <p>Contrast with the deprecated model:
 * <pre>
 *   chunk loads → spawn NPC → NPC exists   (BACKWARDS)
 * </pre>
 *
 * <h2>What this does each scan (every 100 ticks / 5 sec)</h2>
 * <ol>
 *   <li>For each player, find settlements within {@link #SETTLEMENT_SCAN_RANGE}.</li>
 *   <li>For each settlement's population, compute the actor's current presence
 *       position via {@link ActorPresence} (home + shared locations + time-of-day
 *       weights + contextual modifiers).</li>
 *   <li>If the actor's presence is within {@link #ACTIVATION_RANGE} of a player
 *       AND not already materialized → materialize at that position.</li>
 *   <li>If the actor is dead (runtime override {@code is_dead}) → never
 *       materialize. Death is a one-way door (Art XLIII §2).</li>
 * </ol>
 *
 * <h2>Coexistence with the legacy ReificationScan</h2>
 * <p>This runs <b>alongside</b> the legacy {@code ReificationScan} during the
 * transition. Both use the same duplicate-detection (characterId-based AABB
 * query), so an actor materialized by one is skipped by the other. Eventually
 * ReificationScan + {@code NpcSpawnRegistry} + {@code SettlementNpcAnchors}
 * will be deleted entirely (Article XLIV §2: "There shouldn't even BE a spawn
 * registry. I would actually delete this concept eventually.").
 *
 * <h2>Dematerialization (future)</h2>
 * <p>The "dematerialize when distant" half is not yet wired. Currently the
 * existing {@link EntityCultivator} hibernation (AI skipped when no player is
 * within {@code HIBERNATION_RANGE}) handles the performance side. True
 * dematerialization (removing the entity while the actor keeps simulating)
 * is a planned optimization — the actor's presence continues to update in the
 * SettlementRegistry regardless of whether an entity renders it.
 */
public final class ActorMaterializer {

    private ActorMaterializer() {}

    /** Scan interval: every 100 ticks = 5 seconds at 20 TPS (matches ReificationScan). */
    public static final int SCAN_INTERVAL_TICKS = 100;

    /** Materialize when an actor's presence is within this many blocks of a player. */
    public static final double ACTIVATION_RANGE = 64.0;
    public static final double ACTIVATION_RANGE_SQ = ACTIVATION_RANGE * ACTIVATION_RANGE;

    /** Consider settlements whose center is within this many blocks of a player. */
    public static final double SETTLEMENT_SCAN_RANGE = 192.0;
    public static final double SETTLEMENT_SCAN_RANGE_SQ = SETTLEMENT_SCAN_RANGE * SETTLEMENT_SCAN_RANGE;

    /** AABB half-extent for the "already present" duplicate check. */
    public static final double DUPLICATE_CHECK_RADIUS = 32.0;

    /**
     * Run the materialization scan. Called every server tick; internally
     * throttled to {@link #SCAN_INTERVAL_TICKS}.
     *
     * @param level        the server level (Planet Suzaku / overworld)
     * @param serverTicks  the server's game time (for throttling)
     */
    public static void executeTick(ServerLevel level, long serverTicks) {
        if (serverTicks % SCAN_INTERVAL_TICKS != 0) return;
        if (level.players().isEmpty()) return;

        long gameTime = level.getGameTime();

        for (ServerPlayer player : level.players()) {
            materializeAroundPlayer(level, player, gameTime);
        }
    }

    /**
     * For one player, find nearby settlements and materialize their population
     * actors whose current presence intersects the player's activation range.
     *
     * <p><b>Reasoning-engine cycle:</b> for each actor, this now calls the
     * {@link ActorReasoningEngine} FIRST. If the engine returns an
     * {@link Activity} (because a threat is active), the actor is materialized
     * at the activity's location with the activity's pose, and the entity is
     * activity-locked so it visibly HOLDS that reasoning-derived state. If the
     * engine returns null (peaceful), the actor falls back to the daily-rhythm
     * presence and materializes normally (wandering AI).
     *
     * <p>This is where the user's vision becomes observable: the same wolf
     * event produces Wang Lin at the treeline (observing), the patriarch at
     * the gate (guarding), and the others at home (fleeing) — all from one
     * shared {@link WorldSituation}.
     */
    private static void materializeAroundPlayer(ServerLevel level, ServerPlayer player, long gameTime) {
        double px = player.getX();
        double pz = player.getZ();

        for (Settlement settlement : SettlementRegistry.all()) {
            // Coarse settlement-proximity check.
            double sdx = px - (settlement.centerX + 0.5);
            double sdz = pz - (settlement.centerZ + 0.5);
            if (sdx * sdx + sdz * sdz > SETTLEMENT_SCAN_RANGE_SQ) continue;

            // ── Build the shared WorldSituation for this settlement ──
            // One situation, shared by every actor. Different minds reach
            // different conclusions. (The user's core directive.)
            WorldSituation.Threat threat = SettlementThreatIndex.getSituationThreat(
                    settlement.id, settlement.centerX, settlement.centerZ, gameTime);
            TimeOfDay tod = TimeOfDay.fromGameTime(gameTime);
            SettlementPersonality.Mood mood = settlement.personality != null
                    ? settlement.personality.mood : SettlementPersonality.Mood.PEACEFUL;
            WorldSituation situation;
            // CRON-COMPLETIONIST-43: Populate WorldSituation with nearby opportunity data.
            // The CultivatorMind needs to know what opportunities exist nearby so it
            // can decide whether to investigate or pursue them (or ignore them).
            // This bridges the gap between OpportunityCarrierSubscriber (which makes
            // NPCs aware) and CultivatorMind.evaluate() (which decides what to do).
            try {
                List<OpportunitySnapshot> oppSnapshots = new ArrayList<>();
                OpportunityRegistry oppRegistry = OpportunityRegistry.get(level);
                net.minecraft.core.BlockPos settlementCenter = new net.minecraft.core.BlockPos(
                        settlement.centerX, 64, settlement.centerZ);
                for (OpportunityState oppState : oppRegistry.getLiveOpportunitiesNear(
                        settlementCenter, 128.0)) {
                    // OpportunityState carries: opportunityId, pos, lifecycle, driverType.
                    // We derive topic/category from the opportunityId (e.g. "spirit_fruit_ripe")
                    // and use lifecycle ordinal as a proxy for intensity (EMERGED=active=0.5).
                    String oppId = oppState.getOpportunityId();
                    String topic = "opportunity." + oppId;
                    // FORMING/CONTESTED = active and perceptible (intensity 0.6);
                    // DORMANT = not yet perceptible (intensity 0.3, less interesting).
                    float intensity = (oppState.getLifecycle() ==
                            dev.ergenverse.simulation.opportunity.OpportunityLifecycle.FORMING
                            || oppState.getLifecycle() ==
                            dev.ergenverse.simulation.opportunity.OpportunityLifecycle.CONTESTED)
                            ? 0.6f : 0.3f;
                    oppSnapshots.add(new OpportunitySnapshot(
                            oppId, topic, oppId,
                            oppState.getPosX(),
                            oppState.getPosZ(),
                            intensity
                    ));
                }
                situation = new WorldSituation(threat, tod, mood, gameTime, oppSnapshots);
            } catch (Exception e) {
                // OpportunityRegistry may not be initialized yet — fall back to no opportunities.
                situation = new WorldSituation(threat, tod, mood, gameTime);
            }

            for (String actorId : settlement.getPopulation()) {
                // ── The actor's mind reasons over the shared situation ──
                // The mind scores candidate activities against the actor's
                // motivation weights. Nobody wrote "if Wang Lin" — the decision
                // emerges from what each actor cares about.
                Activity activity = ActorReasoningEngine.reason(actorId, situation, settlement);

                int offX, offZ;
                long lockExpiry = 0L;
                if (activity != null) {
                    // The mind produced a decision. Use the activity's location
                    // and lock the entity so it visibly holds this behavior.
                    offX = activity.offsetX;
                    offZ = activity.offsetZ;
                    lockExpiry = activity.expiryTick;
                } else {
                    // Peaceful — fall back to the daily-rhythm presence.
                    int[] off = ActorPresence.computeDailyRhythm(actorId, settlement, gameTime);
                    offX = off[0];
                    offZ = off[1];
                }

                int wx = settlement.centerX + offX;
                int wz = settlement.centerZ + offZ;
                int wy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);
                Vec3 presencePos = new Vec3(wx + 0.5, wy, wz + 0.5);

                // Only materialize if the actor's presence is near the player.
                double distSq = player.position().distanceToSqr(presencePos);
                if (distSq > ACTIVATION_RANGE_SQ) continue;

                // If already materialized, update its activity lock state
                // (the threat may have started or ended since last scan).
                EntityCultivator existing = findPresent(level, actorId, presencePos);
                if (existing != null) {
                    updateActivityLock(existing, activity, lockExpiry, gameTime,
                            settlement, actorId);
                    continue;
                }

                // Materialize at the mind-derived (or daily-rhythm) position.
                // The pose is DERIVED by the renderer from (activity.type +
                // actor role) — the activity does not carry a poseTag. Per the
                // user: "The renderer decides. Same activity. Different
                // presentation." A cultivator observing crouches; a mortal
                // observing stands still.
                materialize(level, actorId, presencePos, activity, lockExpiry);

                // ── Memory: record the threat-response to settlement history ──
                // This is the "village remembers" half of the golden path. When
                // the player returns later, the settlement's history carries
                // who did what during the wolf event.
                if (activity != null && activity.type != Activity.Type.MEDITATING) {
                    String memory = activity.reason;
                    settlement.recordEvent(gameTime,
                            "threat_response:" + activity.type.name().toLowerCase(),
                            memory);
                    if (settlement.personality != null) {
                        settlement.personality.remember(memory);
                    }
                    Ergenverse.LOGGER.info("[ActorMaterializer] {} → {} (emerged from motivations)",
                            actorId, activity.type);
                }
            }
        }
    }

    /**
     * Find an existing EntityCultivator with the given character_id within
     * {@link #DUPLICATE_CHECK_RADIUS} blocks. Returns null if none. Used for
     * duplicate detection (shared with the legacy ReificationScan's pattern).
     */
    private static EntityCultivator findPresent(ServerLevel level, String characterId, Vec3 near) {
        AABB box = AABB.ofSize(near,
                DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2);
        List<EntityCultivator> nearby = level.getEntitiesOfClass(EntityCultivator.class, box,
                c -> characterId.equals(c.getCharacterId()));
        return nearby.isEmpty() ? null : nearby.get(0);
    }

    /**
     * Update an already-materialized entity's activity-lock state. If a threat
     * is active, lock the entity to its mind-derived pose. If the threat has
     * expired, release the lock so the entity resumes daily rhythm.
     *
     * <p>The pose is DERIVED from (activity.type + actor role) — the renderer
     * decides, not the activity. Per the user: same activity, different
     * presentation per actor.
     */
    private static void updateActivityLock(EntityCultivator entity, Activity activity,
                                           long lockExpiry, long gameTime,
                                           Settlement settlement, String actorId) {
        if (activity != null && lockExpiry > gameTime) {
            // Threat still active — ensure the entity is locked to its pose.
            if (!entity.isActivityLocked()) {
                int pose = derivePose(activity, actorId);
                entity.lockToActivity(pose, lockExpiry);
                Ergenverse.LOGGER.info("[ActorMaterializer] {} locked to {} (threat active, pose derived)",
                        actorId, activity.type);
            }
        } else {
            // Threat expired (or peaceful) — release the lock.
            if (entity.isActivityLocked()) {
                entity.releaseActivityLock();
                Ergenverse.LOGGER.info("[ActorMaterializer] {} released from activity lock (threat ended)",
                        actorId);
            }
        }
    }

    /**
     * Derive the renderer pose from the activity type + the actor's role.
     *
     * <p>Per the user's directive: "Activity doesn't specify crouch. The
     * renderer decides: cultivator → crouch behind tree, while mortal →
     * stand still. Same activity. Different presentation."
     *
     * <p>This is the Activity → Intent → Animation Selection → Renderer
     * pipeline. The activity carries the intent (OBSERVING); the renderer
     * (here, the materializer acting as the animation-selector) maps intent +
     * actor type to the concrete pose constant the entity will hold.
     */
    private static int derivePose(Activity activity, String actorId) {
        if (activity == null) return EntityCultivator.POSE_IDLE;
        ActorProfile profile = ActorProfileRegistry.get(actorId);
        boolean isCultivator = profile != null
                && (profile.role == ActorProfile.Role.HIDDEN_CULTIVATOR
                    || profile.role == ActorProfile.Role.CULTIVATOR);
        return switch (activity.type) {
            case OBSERVING_THREAT -> isCultivator
                    ? EntityCultivator.POSE_OBSERVING   // cultivator crouches
                    : EntityCultivator.POSE_IDLE;       // mortal stands still
            case GUARDING -> EntityCultivator.POSE_GUARDING;
            // CRON-COMPLETIONIST-44: Investigating → crouch-observe for cultivators
            // (same visual as OBSERVING_THREAT — crouching behind cover).
            // Mortals stand still (can't comprehend the opportunity).
            case INVESTIGATING -> isCultivator
                    ? EntityCultivator.POSE_OBSERVING
                    : EntityCultivator.POSE_IDLE;
            // CRON-COMPLETIONIST-44: Pursuing → walk with purpose. Both
            // cultivators and mortals move decisively toward the target.
            case PURSUING_OPPORTUNITY -> EntityCultivator.POSE_PURSUING;
            // CRON-COMPLETIONIST-44: Socializing → relaxed facing stance.
            // Both cultivators and mortals turn to face their companion.
            case SOCIALIZING -> EntityCultivator.POSE_SOCIALIZING;
            case SECURING_ASSETS -> EntityCultivator.POSE_IDLE;
            case FLEEING_HOME -> EntityCultivator.POSE_IDLE;
            case MEDITATING -> EntityCultivator.POSE_MEDITATING;
            default -> EntityCultivator.POSE_IDLE;
        };
    }

    /**
     * Materialize one actor at their derived presence position.
     *
     * <p>Reads canon data (Layer 0/2) and runtime overrides (Layer 3). If the
     * actor is runtime-dead, never materializes (Art XLIII §2). The entity is
     * a <b>render shell</b> — the actor (in the SettlementRegistry) is the
     * source of truth; the entity just renders them.
     *
     * @param activity   the mind-derived activity (for pose derivation), or null for peaceful
     * @param lockExpiry the activity-lock expiry tick (>0 to lock, 0 for peaceful)
     */
    private static void materialize(ServerLevel level, String characterId, Vec3 pos,
                                     Activity activity, long lockExpiry) {
        var canonData = WorldStateDataLoader.getEntry("npcs", characterId);
        if (canonData == null) {
            Ergenverse.LOGGER.debug("[ActorMaterializer] No canon data for {} — skipping", characterId);
            return;
        }

        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag override = runtime.getNpcState(characterId);

        // Death is a one-way door (Art XLIII §2).
        if (override != null && override.getBoolean("is_dead")) return;

        EntityCultivator cultivator = EREntityTypes.CULTIVATOR.get().create(level);
        if (cultivator == null) {
            Ergenverse.LOGGER.error("[ActorMaterializer] Failed to create EntityCultivator for {}", characterId);
            return;
        }
        cultivator.moveTo(pos.x, pos.y, pos.z, level.random.nextFloat() * 360.0F, 0.0F);
        cultivator.initializeFromData(characterId, override);

        // If a threat is active, lock the entity to its mind-derived pose.
        // The pose is DERIVED from (activity.type + actor role) — the renderer
        // decides, not the activity. This makes the differentiated behavior
        // OBSERVABLE: Wang Lin crouched at the treeline observing, the
        // patriarch in combat stance at the gate guarding.
        if (lockExpiry > 0L && activity != null) {
            int pose = derivePose(activity, characterId);
            cultivator.lockToActivity(pose, lockExpiry);
        }

        level.addFreshEntity(cultivator);

        Ergenverse.LOGGER.info("[ActorMaterializer] Materialized {} at ({}, {}, {}) activity={} lock={} — Article XLIV actor-as-source-of-truth",
                characterId, pos.x, pos.y, pos.z,
                activity != null ? activity.type : "daily-rhythm", lockExpiry > 0L);
    }
}
