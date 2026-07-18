package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * ReificationScan — the high-frequency proximity scanner that materializes
 * canon NPCs into the physical world when a player approaches.
 *
 * <h2>Why a separate scan (not ChunkEvent.Load)?</h2>
 * <p>The prior design hooked {@code ChunkEvent.Load} and spawned entities
 * synchronously inside the event. This risks cascading generation
 * (structure feature loads a neighbor chunk → event fires → entity spawns →
 * entity loads more chunks → cascade) and is fragile.
 *
 * <p>This scan runs on the server tick — every {@link #SCAN_INTERVAL_TICKS}
 * (100 ticks = 5 seconds) — and checks player proximity to registered spawn
 * points. This is decoupled from chunk generation, predictable, and
 * throttled.
 *
 * <h2>Frequency</h2>
 * <p>{@link #SCAN_INTERVAL_TICKS} = 100 ticks (5 seconds). This is separate
 * from the {@link WorldStateEngine#tick()} 24000-tick simulation tick:
 * <ul>
 *   <li><b>WorldStateEngine</b> (every 24000 ticks / 20 min): advances the
 *       macro simulation — time events, migrations, karma resolution.</li>
 *   <li><b>ReificationScan</b> (every 100 ticks / 5 sec): materializes/
 *       dematerializes entity shells based on player proximity.</li>
 * </ul>
 * These are two different loops with different concerns. Don't conflate them.
 *
 * <h2>Activation range</h2>
 * <p>A canon NPC materializes when a player is within
 * {@link #ACTIVATION_RANGE} blocks of the NPC's spawn anchor. This matches
 * the {@link EntityCultivator#HIBERNATION_RANGE} (64 blocks) — the entity
 * spawns when a player enters hibernation range, and hibernates when they
 * leave. No thrashing.
 *
 * <h2>Duplicate prevention</h2>
 * <p>Before spawning, we check whether an {@link EntityCultivator} with the
 * same {@code character_id} already exists within a small AABB around the
 * spawn point. The prior design used {@code level.getEntities().getAll()
 * .spliterator().estimateSize() > 0} — which returns {@code Long.MAX_VALUE}
 * for non-collection iterables and would always return true, preventing
 * ALL spawns. We use a proper typed entity scan with AABB.
 *
 * <h2>v1 scope</h2>
 * <p>Only {@code wang_tiangui} (Wang Lin's mortal father) is wired. He spawns
 * at a fixed surface heightmap position in {@code zhao_plains}. The spawn
 * coordinate is a placeholder — marked {@code TODO: structure anchor} — until
 * the Wang Family Village structure JSON (Task 3) provides a real anchor
 * point.
 *
 * <h2>Prime Directive</h2>
 * <p>"Reality is objective." The NPC's stats come from the canon DB via
 * {@link WorldStateDataLoader#getEntry}, not from hardcoded values. The
 * simulation doesn't invent Wang Tiangui's HP — it reads it from
 * {@code data/ergenverse/npcs/wang_tiangui.json}.
 */
public final class ReificationScan {

    private ReificationScan() {}

    /** Scan interval: every 100 ticks = 5 seconds at 20 TPS. */
    public static final int SCAN_INTERVAL_TICKS = 100;

    /** Materialize when a player is within this many blocks of the spawn anchor. */
    public static final double ACTIVATION_RANGE = 64.0;
    public static final double ACTIVATION_RANGE_SQ = ACTIVATION_RANGE * ACTIVATION_RANGE;

    /** AABB half-extent for the "already present" duplicate check. */
    public static final double DUPLICATE_CHECK_RADIUS = 32.0;

    // ═══════════════════════════════════════════════════════════════════
    //  Main entry point — called from Ergenverse.onServerTick
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Run the proximity scan. Called every server tick; internally throttled
     * to {@link #SCAN_INTERVAL_TICKS}.
     *
     * @param level the overworld (or any dimension with canon NPCs)
     * @param serverTicks the server's game time (for throttling)
     * @param spatialIndex the shared biome cache (resolves chunk→location)
     */
    public static void executeTick(ServerLevel level, long serverTicks, SpatialBiomeCacheIndex spatialIndex) {
        if (serverTicks % SCAN_INTERVAL_TICKS != 0) return;
        if (level.players().isEmpty()) return;

        // v1: only process the overworld (Planet Suzaku).
        // Future: iterate all loaded dimensions.
        for (ServerPlayer player : level.players()) {
            scanAroundPlayer(level, player, spatialIndex);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Per-player scan
    // ═══════════════════════════════════════════════════════════════════

    private static void scanAroundPlayer(ServerLevel level, ServerPlayer player, SpatialBiomeCacheIndex spatialIndex) {
        ChunkPos playerChunk = player.chunkPosition();

        // Resolve the player's current location via the biome cache
        ChunkAccess chunk = level.getChunkSource().getChunk(playerChunk.x, playerChunk.z, false);
        if (chunk == null) return;

        String resolvedLocation = spatialIndex.resolveAndCacheChunk(level, chunk);
        if (resolvedLocation == null) return; // chunk not fully generated yet

        // ── v1: only handle zhao_plains (Wang Tiangui's location) ────────
        // Future: data-driven spawn registry keyed by locationId
        if ("zhao_plains".equals(resolvedLocation)) {
            handleWangTiangui(level, player);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  v1 target: Wang Tiangui
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Materialize Wang Tiangui (王天贵, Wang Lin's mortal father) if the player
     * is near his spawn anchor and he isn't already present.
     *
     * <p>Canon: Wang Tiangui is a mortal resident of Wang Family Village in
     * Zhao Country. He has no cultivation, ~20 HP, passive behavior. His
     * death (when Wang Lin's family is massacred) is a canon event — but
     * that's t>0 simulation, not t₀. At t₀ he is alive.
     *
     * <p>Spawn anchor: TODO — replace with Wang Family Village structure
     * anchor once Task 3 (structure JSONs) is complete. For now, spawn near
     * the player's current position (surface heightmap) so the v1 proof of
     * concept is testable.
     */
    private static void handleWangTiangui(ServerLevel level, ServerPlayer player) {
        // ── v1 spawn anchor: near the player ─────────────────────────────
        // TODO: Task 3 — replace with Wang Family Village structure anchor.
        // For now, spawn 16 blocks north of the player's position on the surface.
        // This is explicitly a placeholder to prove the materialization loop.
        int spawnX = player.blockPosition().getX();
        int spawnZ = player.blockPosition().getZ() - 16;
        int spawnY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
        Vec3 spawnPoint = new Vec3(spawnX + 0.5, spawnY, spawnZ + 0.5);

        // ── Check player proximity ───────────────────────────────────────
        double distSq = player.position().distanceToSqr(spawnPoint);
        if (distSq > ACTIVATION_RANGE_SQ) return;

        // ── Duplicate check: is Wang Tiangui already spawned nearby? ─────
        if (isCultivatorAlreadyPresent(level, "wang_tiangui", spawnPoint)) return;

        // ── Read canon baseline ──────────────────────────────────────────
        // This is the "Fix 4" that was commented out in the Gemini code.
        // We actually call getEntry and use it.
        var canonData = WorldStateDataLoader.getEntry("npcs", "wang_tiangui");
        if (canonData == null) {
            Ergenverse.LOGGER.warn("[ReificationScan] No canon data for wang_tiangui — skipping spawn");
            return;
        }

        // ── Read runtime overrides (t>0 mutations) ───────────────────────
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag runtimeOverride = runtime.getNpcState("wang_tiangui");

        // ── Check if the NPC is canonically/runtime dead ─────────────────
        if (runtimeOverride != null && runtimeOverride.getBoolean("is_dead")) {
            // Wang Tiangui died (canon event or player kill). Don't respawn.
            return;
        }

        // ── Materialize ──────────────────────────────────────────────────
        EntityCultivator cultivator = EREntityTypes.CULTIVATOR.get().create(level);
        if (cultivator == null) {
            Ergenverse.LOGGER.error("[ReificationScan] Failed to create EntityCultivator");
            return;
        }

        cultivator.moveTo(spawnPoint.x, spawnPoint.y, spawnPoint.z,
                level.random.nextFloat() * 360.0F, 0.0F);
        cultivator.initializeFromData("wang_tiangui", runtimeOverride);

        level.addFreshEntity(cultivator);
        Ergenverse.LOGGER.info("[ReificationScan] Materialized wang_tiangui at ({}, {}, {}) — divergence={}",
                spawnPoint.x, spawnPoint.y, spawnPoint.z, runtime.getDivergenceCounter());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Duplicate detection — the REAL version
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check whether an {@link EntityCultivator} with the given
     * {@code character_id} already exists within {@link #DUPLICATE_CHECK_RADIUS}
     * blocks of {@code near}.
     *
     * <p>This replaces the broken {@code level.getEntities().getAll()
     * .spliterator().estimateSize() > 0} check, which would return true for
     * any level with any entity (cow, zombie, etc.) and prevent all spawns.
     *
     * <p>Uses {@link ServerLevel#getEntitiesOfClass(Class, AABB)} which is
     * a proper typed spatial query — O(loaded entities in AABB), not
     * O(all entities in level).
     */
    private static boolean isCultivatorAlreadyPresent(ServerLevel level, String characterId, Vec3 near) {
        AABB searchBox = AABB.ofSize(near, DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2, DUPLICATE_CHECK_RADIUS * 2);
        List<EntityCultivator> nearby = level.getEntitiesOfClass(EntityCultivator.class, searchBox,
                c -> characterId.equals(c.getCharacterId()));
        return !nearby.isEmpty();
    }
}
