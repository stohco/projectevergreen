package dev.ergenverse.starry_sky;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VoidBeastEncounter — handles void beast (虚空兽) encounters in the Starry
 * Sky dimension.
 *
 * <p><b>Canon (Renegade Immortal):</b> The starry sky between cultivation
 * planets is not empty — it is home to void beasts (虚空兽), monstrous
 * creatures that hunt cultivators traveling through the void. Wang Lin
 * encountered void beasts during his inter-planetary travels. They are
 * attracted to spiritual energy and ambush cultivators flying between
 * teleportation arrays.
 *
 * <p><b>Design (per task spec):</b>
 * <ul>
 *   <li>Void beast territories are random points in the void — fixed
 *       locations where void beasts lair. These are generated at sim
 *       init and remain stable.</li>
 *   <li>When a wandering cultivator's simulated path crosses a void
 *       beast territory (within ~32 blocks of a territory center), there
 *       is a chance the cultivator is "lost" (consumed by the void beast,
 *       removed from the simulation, counter incremented).</li>
 *   <li>When a player is nearby a territory (within ~64 blocks), an
 *       actual void beast entity is spawned at that location — a
 *       re-skinned Enderman with a custom name "虚空兽 / Void Beast" and
 *       increased HP/damage to reflect the canon threat.</li>
 *   <li>This makes the starry sky dangerous even for NPCs — the void
 *       is not a safe travel corridor.</li>
 * </ul>
 *
 * <p><b>Prime Directive compliance:</b> Void beasts are objective
 * inhabitants of the starry sky. They exist and hunt regardless of player
 * observation. The simulation of cultivator losses is independent of
 * player presence — the player only sees the actual entity when they
 * approach a territory.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class VoidBeastEncounter {

    private VoidBeastEncounter() {}

    // ── Encounter Constants ─────────────────────────────────────────────

    /** Number of void beast territories in the starry sky. */
    public static final int TERRITORY_COUNT = 12;

    /** Range (in blocks) within which a cultivator is "in" a territory. */
    public static final double TERRITORY_RANGE = 48.0;

    /** Range (in blocks) within which a player triggers void beast spawning. */
    public static final double PLAYER_TRIGGER_RANGE = 96.0;

    /** Chance per sim-tick that a cultivator in a territory is lost. */
    public static final double LOSS_CHANCE_PER_TICK = 0.04;

    /** Range (in blocks) for player to keep a spawned void beast alive. */
    public static final double PLAYER_KEEP_RANGE = 144.0;

    // ── Territory State ─────────────────────────────────────────────────

    /** Void beast territory centers — fixed at sim init. */
    private static final BlockPos[] TERRITORIES = new BlockPos[TERRITORY_COUNT];

    /** Whether territory state has been initialized. */
    private static boolean territoriesInitialized = false;

    /**
     * Map of territory index → UUID of spawned void beast entity (if any).
     * Tracks reified entities so we don't spawn duplicates.
     */
    private static final Map<Integer, UUID> spawnedBeasts = new HashMap<>();

    // ── Initialization ──────────────────────────────────────────────────

    /**
     * Initialize the void beast territories. Called lazily on first
     * simulation tick.
     */
    private static void initializeTerritories() {
        if (territoriesInitialized) return;
        territoriesInitialized = true;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        // Generate territories at random positions, biased away from
        // teleport arrays (void beasts lair in the deep void, not near
        // civilized teleport points).
        BlockPos[] arrays = StarrySkySimulation.getTeleportArrays();
        for (int i = 0; i < TERRITORY_COUNT; i++) {
            BlockPos candidate;
            int attempts = 0;
            do {
                candidate = new BlockPos(
                        rng.nextInt(-3500, 3500),
                        40 + rng.nextInt(120),
                        rng.nextInt(-3500, 3500));
                attempts++;
            } while (attempts < 10 && tooCloseToArrays(candidate, arrays));
            TERRITORIES[i] = candidate;
        }
        Ergenverse.LOGGER.info("[VoidBeastEncounter] Initialized {} void beast territories.",
                TERRITORY_COUNT);
    }

    private static boolean tooCloseToArrays(BlockPos pos, BlockPos[] arrays) {
        for (BlockPos array : arrays) {
            if (pos.closerThan(array, 256.0)) return true;
        }
        return false;
    }

    // ── Cultivator Loss Check ───────────────────────────────────────────

    /**
     * Check whether a wandering cultivator (currently at their simulated
     * position) has been consumed by a void beast. Called once per
     * simulation tick per cultivator.
     *
     * <p>If the cultivator is within {@link #TERRITORY_RANGE} of any
     * territory center, there is a {@link #LOSS_CHANCE_PER_TICK} chance
     * they are lost (consumed by the void beast).
     *
     * @param cultivator the wandering cultivator to check
     * @param rng        the thread-local random
     * @return true if the cultivator was lost (consumed by a void beast)
     */
    public static boolean checkCultivatorLost(
            StarrySkySimulation.WanderingCultivator cultivator, ThreadLocalRandom rng) {
        initializeTerritories();
        for (BlockPos territory : TERRITORIES) {
            if (cultivator.current.closerThan(territory, TERRITORY_RANGE)) {
                // Higher-realm cultivators are harder to consume (canon: only
                // the strongest survive void beast attacks)
                double lossChance = scaleLossChanceByRealm(cultivator.realm);
                return rng.nextDouble() < lossChance;
            }
        }
        return false;
    }

    /**
     * Scale the void beast loss chance by the cultivator's realm.
     * Higher realms survive more often. Canon: Wang Lin at Soul Formation+
     * could fight void beasts; lower-realm cultivators were devoured.
     */
    private static double scaleLossChanceByRealm(String realm) {
        if (realm == null) return LOSS_CHANCE_PER_TICK;
        switch (realm) {
            case "Qi Condensation":
                return LOSS_CHANCE_PER_TICK * 3.0;     // mortals easily devoured
            case "Foundation Establishment":
                return LOSS_CHANCE_PER_TICK * 2.0;
            case "Core Formation":
                return LOSS_CHANCE_PER_TICK * 1.2;
            case "Nascent Soul":
                return LOSS_CHANCE_PER_TICK * 0.6;
            case "Soul Formation":
                return LOSS_CHANCE_PER_TICK * 0.3;
            case "Soul Transformation":
                return LOSS_CHANCE_PER_TICK * 0.1;
            case "Nirvana Scryer":
            case "Heaven Blight":
            case "Third Step":
                return 0.0; // third-step+ cultivators cannot be consumed
            default:
                return LOSS_CHANCE_PER_TICK;
        }
    }

    // ── Player-triggered Void Beast Spawning ────────────────────────────

    /**
     * For each player in the starry sky dimension, check if they are near
     * any void beast territory. If yes, spawn (or sync) an actual void
     * beast entity at that territory. If no players are near a territory,
     * despawn its entity.
     *
     * <p>Called from {@link StarrySkyEvents#onServerTick} after the
     * simulation tick.
     *
     * @param starrySky the starry sky server level
     */
    public static void tickPlayerEncounters(ServerLevel starrySky) {
        initializeTerritories();

        List<ServerPlayer> playersInDim = starrySky.getServer().getPlayerList()
                .getPlayers().stream()
                .filter(p -> p.level() == starrySky)
                .toList();

        for (int i = 0; i < TERRITORIES.length; i++) {
            BlockPos territory = TERRITORIES[i];
            boolean anyPlayerNear = false;
            for (ServerPlayer p : playersInDim) {
                if (p.blockPosition().closerThan(territory, PLAYER_TRIGGER_RANGE)) {
                    anyPlayerNear = true;
                    break;
                }
            }
            if (anyPlayerNear) {
                spawnOrSyncVoidBeast(starrySky, i, territory);
            } else {
                // No player nearby — despawn if spawned, but keep the territory
                UUID spawned = spawnedBeasts.get(i);
                if (spawned != null) {
                    Mob mob = findMobByUuid(starrySky, spawned);
                    if (mob != null) {
                        mob.discard();
                    }
                    spawnedBeasts.remove(i);
                }
            }
        }
    }

    /**
     * Spawn a void beast (re-skinned Enderman with custom name + buffed
     * stats) at the given territory, or sync the existing one's position.
     */
    private static void spawnOrSyncVoidBeast(ServerLevel level, int territoryIdx, BlockPos territory) {
        UUID existing = spawnedBeasts.get(territoryIdx);
        if (existing != null) {
            Mob mob = findMobByUuid(level, existing);
            if (mob != null && !mob.isRemoved()) {
                // Already spawned and alive — leave it (the Enderman AI
                // handles its own movement toward the player)
                return;
            } else {
                spawnedBeasts.remove(territoryIdx);
            }
        }

        // Don't spawn on peaceful — vanilla Endermen don't spawn on peaceful
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;

        EnderMan beast = EntityType.ENDERMAN.create(level);
        if (beast == null) {
            Ergenverse.LOGGER.warn("[VoidBeastEncounter] Failed to create EnderMan for void beast.");
            return;
        }
        beast.moveTo(territory.getX() + 0.5, territory.getY(), territory.getZ() + 0.5,
                level.random.nextFloat() * 360.0F, 0.0F);
        // Canon void beast flavor: glowing name, hostile
        beast.setCustomName(Component.literal(
                "\u00A7d\u00A7l虚空兽 \u00A77/ Void Beast"));
        beast.setCustomNameVisible(true);
        // Boost HP to reflect canon threat (vanilla Enderman = 40 HP)
        // Use attribute modification via heal + max health attribute
        if (beast.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH) != null) {
            beast.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH)
                    .setBaseValue(80.0);
        }
        beast.setHealth(80.0F);
        // Mark as persistent (no despawn when far away)
        beast.setPersistenceRequired();
        // Spawn reason: TRIGGERED (player-triggered encounter)
        beast.finalizeSpawn(level, level.getCurrentDifficultyAt(territory),
                MobSpawnType.TRIGGERED, null, null);
        level.addFreshEntity(beast);
        spawnedBeasts.put(territoryIdx, beast.getUUID());
        Ergenverse.LOGGER.debug("[VoidBeastEncounter] Spawned void beast at territory #{} ({})",
                territoryIdx, territory);
    }

    /**
     * Despawn all spawned void beasts (e.g., when last player leaves the
     * starry sky dimension).
     */
    public static void despawnAll(ServerLevel level) {
        for (Map.Entry<Integer, UUID> entry : spawnedBeasts.entrySet()) {
            Mob mob = findMobByUuid(level, entry.getValue());
            if (mob != null) {
                mob.discard();
            }
        }
        spawnedBeasts.clear();
    }

    /**
     * Find a mob by UUID in the given level.
     */
    @Nullable
    private static Mob findMobByUuid(ServerLevel level, UUID uuid) {
        net.minecraft.world.entity.Entity e = level.getEntity(uuid);
        return e instanceof Mob mob ? mob : null;
    }

    // ── Public accessors ────────────────────────────────────────────────

    /** @return the array of void beast territory centers. */
    public static BlockPos[] getTerritories() {
        initializeTerritories();
        return TERRITORIES.clone();
    }
}
