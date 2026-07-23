package dev.ergenverse.simulation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.spawn.WangFamilyVillageBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * ReificationScan — the high-frequency proximity scanner that materializes
 * canon NPCs into the physical world when a player approaches.
 *
 * <h2>Two spawn modes (v2):</h2>
 * <ol>
 *   <li><b>Settlement-anchored:</b> If a player is near a known settlement
 *       (Wang Family Village, Heng Yue Sect), NPCs registered with
 *       {@link SettlementNpcAnchors} spawn at their fixed canonical
 *       positions within the settlement. This is the primary mode — it
 *       makes villages feel inhabited. Per Art. XXII: "A canon entry that
 *       exists only as data, never as experience, is a failure."</li>
 *   <li><b>Biome-offset fallback:</b> For NPCs without settlement anchors
 *       (wandering cultivators, beast NPCs in the wild), the old
 *       hash-derived offset from the player is used. These NPCs spawn
 *       anywhere in their registered biome.</li>
 * </ol>
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
 * spawn point.
 *
 * <h2>Prime Directive</h2>
 * <p>"Reality is objective." The NPC's stats come from the canon DB via
 * {@link WorldStateDataLoader#getEntry}, not from hardcoded values.
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

    /** Range at which settlement-anchored NPCs activate (blocks from settlement center). */
    public static final double SETTLEMENT_PROXIMITY_RANGE = 128.0;
    public static final double SETTLEMENT_PROXIMITY_RANGE_SQ = SETTLEMENT_PROXIMITY_RANGE * SETTLEMENT_PROXIMITY_RANGE;

    /**
     * Known settlement centers: settlementId → (centerX, centerZ).
     * Populated once at server start from builder constants.
     */
    private static final Map<String, int[]> SETTLEMENT_CENTERS = new java.util.HashMap<>();

    // ═══════════════════════════════════════════════════════════════════
    //  Initialization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Initialize settlement center coordinates. Called once on server start.
     */
    public static void initializeSettlements() {
        SETTLEMENT_CENTERS.put("wang_family_village",
                new int[]{WangFamilyVillageBuilder.VILLAGE_X, WangFamilyVillageBuilder.VILLAGE_Z});
        SETTLEMENT_CENTERS.put("heng_yue_sect",
                new int[]{5400, -1900});
        Ergenverse.LOGGER.info("[ReificationScan] Settlement centers initialized: {}", SETTLEMENT_CENTERS.keySet());
    }

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

        // ── Settlement-anchored spawn check ─────────────────────────────
        // Check if the player is near any known settlement. If so, spawn
        // anchored NPCs at their fixed canonical positions. This takes
        // priority over biome-offset spawning.
        if (scanSettlementAnchors(level, player)) {
            return; // Anchored NPCs handled; skip biome-offset for this tick.
        }

        // ── Biome-offset fallback: query the NpcSpawnRegistry ───────────
        // For NPCs without settlement anchors (wandering cultivators, etc.).
        // Also for NPCs that have anchors but the player is not near the
        // settlement — they spawn via biome offset as before.
        java.util.List<String> npcsHere = NpcSpawnRegistry.getNpcsForLocation(resolvedLocation);
        if (npcsHere.isEmpty()) return;

        for (String characterId : npcsHere) {
            // Skip NPCs that have settlement anchors — they only spawn at
            // their anchored position, not randomly in the biome.
            if (SettlementNpcAnchors.hasAnchor(characterId)) continue;
            handleBiomeOffsetSpawn(level, player, characterId);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Settlement-anchored spawning (v2 — fixes the ghost-town bug)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Check if the player is near any known settlement and spawn anchored
     * NPCs at their fixed canonical positions.
     *
     * @return true if any settlement anchors were processed (even if no
     *         NPCs were actually spawned — just being near is enough to
     *         suppress biome-offset spawning for anchored NPCs).
     */
    private static boolean scanSettlementAnchors(ServerLevel level, ServerPlayer player) {
        boolean anyProcessed = false;
        double playerX = player.getX();
        double playerZ = player.getZ();

        for (Map.Entry<String, int[]> entry : SETTLEMENT_CENTERS.entrySet()) {
            String settlementId = entry.getKey();
            int[] center = entry.getValue();

            // Check if player is within settlement proximity range
            double dx = playerX - (center[0] + 0.5);
            double dz = playerZ - (center[1] + 0.5);
            if (dx * dx + dz * dz > SETTLEMENT_PROXIMITY_RANGE_SQ) continue;

            anyProcessed = true;

            // Get anchored NPCs for this settlement
            List<SettlementNpcAnchors.NpcAnchor> anchors =
                    SettlementNpcAnchors.getAnchorsForSettlement(settlementId);
            if (anchors.isEmpty()) continue;

            int surfaceY = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(center[0], 0, center[1])).getY();

            for (SettlementNpcAnchors.NpcAnchor anchor : anchors) {
                int anchorX = center[0] + anchor.offsetX();
                int anchorZ = center[1] + anchor.offsetZ();
                int anchorY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, anchorX, anchorZ);
                Vec3 anchorPos = new Vec3(anchorX + 0.5, anchorY, anchorZ + 0.5);

                // Check if NPC already present
                if (isCultivatorAlreadyPresent(level, anchor.characterId(), anchorPos)) continue;

                // Check player proximity to this specific anchor
                double distSq = player.position().distanceToSqr(anchorPos);
                if (distSq > ACTIVATION_RANGE_SQ) continue;

                // Materialize at anchored position
                materializeAt(level, anchor.characterId(), anchorPos);
            }
        }
        return anyProcessed;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Generic NPC materialization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Biome-offset fallback: spawn an NPC at a deterministic offset from the
     * player. Used for NPCs without settlement anchors (wandering cultivators,
     * traveling merchants, etc.).
     *
     * @param level       the server level
     * @param player      the player whose proximity triggered the scan
     * @param characterId the NPC's canon character ID
     */
    private static void handleBiomeOffsetSpawn(ServerLevel level, ServerPlayer player, String characterId) {
        // ── Compute spawn point: deterministic offset from player ────────
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

        // ── Duplicate check ───────────────────────────────────────────────
        if (isCultivatorAlreadyPresent(level, characterId, spawnPoint)) return;

        // Materialize at the computed position
        materializeAt(level, characterId, spawnPoint);
    }

    /**
     * Core materialization logic — shared by both anchored and offset spawns.
     * Reads canon data, runtime overrides, checks for dead state, creates
     * the entity, and adds it to the world.
     *
     * @param level       the server level
     * @param characterId the NPC's canon character ID
     * @param spawnPoint   the exact world position to spawn at
     */
    private static void materializeAt(ServerLevel level, String characterId, Vec3 spawnPoint) {
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
