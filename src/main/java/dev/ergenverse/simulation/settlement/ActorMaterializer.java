package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.simulation.WorldRuntimeState;
import dev.ergenverse.simulation.WorldStateDataLoader;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
     */
    private static void materializeAroundPlayer(ServerLevel level, ServerPlayer player, long gameTime) {
        double px = player.getX();
        double pz = player.getZ();

        for (Settlement settlement : SettlementRegistry.all()) {
            // Coarse settlement-proximity check.
            double sdx = px - (settlement.centerX + 0.5);
            double sdz = pz - (settlement.centerZ + 0.5);
            if (sdx * sdx + sdz * sdz > SETTLEMENT_SCAN_RANGE_SQ) continue;

            // Query the active threat context for this settlement (Article XLIV §5).
            // If wolves or predators are near, the SettlementThreatIndex returns a
            // threat context — and ActorPresence collapses all actors onto home/flee.
            // "If wolves appear, everything changes."
            ActorPresence.PresenceContext ctx =
                    SettlementThreatIndex.getThreatContext(settlement.id, gameTime);

            for (String actorId : settlement.getPopulation()) {
                // Derive the actor's current presence position from their life.
                int[] off = ActorPresence.computePosition(actorId, settlement, gameTime, ctx);
                int wx = settlement.centerX + off[0];
                int wz = settlement.centerZ + off[1];
                int wy = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, wx, wz);
                Vec3 presencePos = new Vec3(wx + 0.5, wy, wz + 0.5);

                // Only materialize if the actor's presence is near the player.
                double distSq = player.position().distanceToSqr(presencePos);
                if (distSq > ACTIVATION_RANGE_SQ) continue;

                // Don't double-materialize (legacy ReificationScan may have done it).
                if (isAlreadyPresent(level, actorId, presencePos)) continue;

                materialize(level, actorId, presencePos);
            }
        }
    }

    /**
     * Check whether an EntityCultivator with the given character_id already
     * exists within {@link #DUPLICATE_CHECK_RADIUS} blocks. Shared with the
     * legacy ReificationScan's logic so the two paths never double-spawn.
     */
    private static boolean isAlreadyPresent(ServerLevel level, String characterId, Vec3 near) {
        AABB box = AABB.ofSize(near,
                DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2);
        List<EntityCultivator> nearby = level.getEntitiesOfClass(EntityCultivator.class, box,
                c -> characterId.equals(c.getCharacterId()));
        return !nearby.isEmpty();
    }

    /**
     * Materialize one actor at their derived presence position.
     *
     * <p>Reads canon data (Layer 0/2) and runtime overrides (Layer 3). If the
     * actor is runtime-dead, never materializes (Art XLIII §2). The entity is
     * a <b>render shell</b> — the actor (in the SettlementRegistry) is the
     * source of truth; the entity just renders them.
     */
    private static void materialize(ServerLevel level, String characterId, Vec3 pos) {
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
        level.addFreshEntity(cultivator);

        Ergenverse.LOGGER.info("[ActorMaterializer] Materialized {} (presence-derived) at ({}, {}, {}) — Article XLIV actor-as-source-of-truth",
                characterId, pos.x, pos.y, pos.z);
    }
}
