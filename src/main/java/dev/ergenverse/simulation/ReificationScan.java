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

        // ── Data-driven spawn: query the NpcSpawnRegistry ────────────────
        // Any location can have zero or more NPCs registered to spawn there.
        // The registry maps locationId → List<characterId>.
        java.util.List<String> npcsHere = NpcSpawnRegistry.getNpcsForLocation(resolvedLocation);
        if (npcsHere.isEmpty()) return;

        for (String characterId : npcsHere) {
            handleNpcSpawn(level, player, characterId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Generic NPC materialization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Materialize a canon NPC if the player is near the spawn point and the
     * NPC isn't already present or dead.
     *
     * <p>This is the generic version of the old {@code handleWangTiangui} —
     * it works for ANY canon NPC registered in {@link NpcSpawnRegistry}.
     * The spawn point is computed as a deterministic offset from the player's
     * position (seeded by the character ID hash) so each NPC spawns at a
     * consistent position relative to the player, not stacked on top of
     * each other.
     *
     * @param level       the server level
     * @param player      the player whose proximity triggered the scan
     * @param characterId the NPC's canon character ID (e.g. "situ_nan")
     */
    private static void handleNpcSpawn(ServerLevel level, ServerPlayer player, String characterId) {
        // ── Compute spawn point: deterministic offset from player ────────
        // Each NPC gets a unique offset (seeded by character ID hash) so
        // multiple NPCs in the same location don't stack on the same block.
        // Offset range: 8-48 blocks from the player, in a deterministic direction.
        int hash = characterId.hashCode();
        int angle = (hash & 0xFF) * 360 / 256;  // 0-359 degrees
        int dist = 8 + ((hash >> 8) & 0x3F);     // 8-71 blocks
        int spawnX = player.blockPosition().getX() + (int)(Math.cos(Math.toRadians(angle)) * dist);
        int spawnZ = player.blockPosition().getZ() + (int)(Math.sin(Math.toRadians(angle)) * dist);
        int spawnY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnX, spawnZ);
        Vec3 spawnPoint = new Vec3(spawnX + 0.5, spawnY, spawnZ + 0.5);

        // ── Check player proximity ───────────────────────────────────────
        double distSq = player.position().distanceToSqr(spawnPoint);
        if (distSq > ACTIVATION_RANGE_SQ) return;

        // ── Duplicate check: is this NPC already spawned nearby? ────────
        if (isCultivatorAlreadyPresent(level, characterId, spawnPoint)) return;

        // ── Read canon baseline ──────────────────────────────────────────
        var canonData = WorldStateDataLoader.getEntry("npcs", characterId);
        if (canonData == null) {
            Ergenverse.LOGGER.warn("[ReificationScan] No canon data for {} — skipping spawn", characterId);
            return;
        }

        // ── Read runtime overrides (t>0 mutations) ───────────────────────
        WorldRuntimeState runtime = WorldRuntimeState.get(level);
        CompoundTag runtimeOverride = runtime.getNpcState(characterId);

        // ── Check if the NPC is canonically/runtime dead ─────────────────
        if (runtimeOverride != null && runtimeOverride.getBoolean("is_dead")) {
            return;
        }

        // ── Materialize ──────────────────────────────────────────────────
        EntityCultivator cultivator = EREntityTypes.CULTIVATOR.get().create(level);
        if (cultivator == null) {
            Ergenverse.LOGGER.error("[ReificationScan] Failed to create EntityCultivator for {}", characterId);
            return;
        }

        cultivator.moveTo(spawnPoint.x, spawnPoint.y, spawnPoint.z,
                level.random.nextFloat() * 360.0F, 0.0F);
        cultivator.initializeFromData(characterId, runtimeOverride);

        level.addFreshEntity(cultivator);
        Ergenverse.LOGGER.info("[ReificationScan] Materialized {} at ({}, {}, {}) — divergence={}",
                characterId, spawnPoint.x, spawnPoint.y, spawnPoint.z, runtime.getDivergenceCounter());
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
