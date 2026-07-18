package dev.ergenverse.starry_sky;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.Registries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * StarrySkySimulation — the tick-based simulation of the Starry Sky dimension.
 *
 * <p><b>Canon (Renegade Immortal):</b> When Wang Lin leaves Planet Suzaku, he
 * travels through the "starry sky" (星空) — the void between cultivation planets
 * in the Alliance Star System / Vermilion Bird Starfield. Cultivators fly through
 * this space (higher realms can fly in vacuum; lower realms use teleportation
 * arrays or flying artifacts). There are void beasts (虚空兽), wandering
 * cultivators, abandoned ancient teleportation arrays, and meteor fields.
 *
 * <p><b>Design (per task directive):</b> NPC cultivators should be flying on
 * FIXED ROUTES between teleport points (like trade caravans in space), void
 * beasts should hunt them, abandoned teleportation arrays can be activated.
 * The simulation should be TICK-BASED — NPCs exist and move even when the
 * player isn't looking. This is DIFFERENT from vanilla mob spawning.
 *
 * <p><b>Simulation architecture:</b>
 * <ul>
 *   <li>Maintains a list of {@link WanderingCultivator} objects — pure data,
 *       NOT Minecraft entities. Each has: currentPos, destination, speed,
 *       cultivation realm (for flavor), name (canon-style generated).</li>
 *   <li>Every 20 server ticks (1 second), advances each cultivator toward
 *       their destination. When they arrive, picks a new destination.</li>
 *   <li>Canon teleport: cultivators passing near an "activated teleport
 *       array" position may teleport (jump to a distant point). This is the
 *       canon means of long-distance travel in the starry sky.</li>
 *   <li>When a player enters the starry_sky dimension, the simulation
 *       SPAWNS actual Minecraft entities (EntityCultivator with a simulated
 *       flavor name) at the simulated positions of nearby cultivators
 *       (within render distance). Each simulated cultivator is given a
 *       UUID-based tag linking the entity to the simulation record.</li>
 *   <li>When the player leaves, despawns those entities but the simulation
 *       continues. The cultivators keep moving in the background.</li>
 * </ul>
 *
 * <p><b>Lightweight constraints:</b>
 * <ul>
 *   <li>Maximum 50 simulated cultivators.</li>
 *   <li>Ticks every 20 server ticks (1 sim-second), not every tick.</li>
 *   <li>Simulation data persists across dimension unloads but resets on
 *       server restart (no NBT persistence — by design, per task spec).</li>
 * </ul>
 *
 * <p><b>Prime Directive compliance:</b> The starry sky is alive independently
 * of the player. Cultivators are flying, void beasts are hunting, teleport
 * arrays are activating — all as objective phenomena. The player observing
 * them is incidental; their existence does not depend on player observation.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class StarrySkySimulation {

    private StarrySkySimulation() {}

    // ── Dimension Key ───────────────────────────────────────────────────

    /** ResourceKey for the starry_sky dimension. */
    public static final ResourceKey<Level> STARRY_SKY_KEY = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(Ergenverse.MOD_ID, "starry_sky"));

    // ── Simulation Constants ────────────────────────────────────────────

    /** Maximum number of simulated cultivators. Per task spec. */
    public static final int MAX_CULTIVATORS = 50;

    /** Tick interval: 20 server ticks = 1 simulation step (1 second). */
    public static final int TICK_INTERVAL = 20;

    /** Range (in blocks) around a player where simulated cultivators are reified. */
    public static final double REIFICATION_RANGE = 128.0;

    /** Range (in blocks) around a teleport array within which a cultivator may teleport. */
    public static final double TELEPORT_ARRAY_RANGE = 16.0;

    /** Canonical teleport array positions (fixed points in the starry sky). */
    private static final BlockPos[] TELEPORT_ARRAYS = new BlockPos[] {
            new BlockPos(0, 80, 0),
            new BlockPos(800, 100, -1200),
            new BlockPos(-1500, 90, 600),
            new BlockPos(2000, 110, 1800),
            new BlockPos(-2200, 95, -1800),
            new BlockPos(1200, 105, 2400)
    };

    // ── Simulation State ────────────────────────────────────────────────

    /** The list of currently simulated wandering cultivators. */
    private static final List<WanderingCultivator> CULTIVATORS = new ArrayList<>();

    /** Whether the simulation has been initialized. */
    private static boolean initialized = false;

    /** Total simulation tick counter (incremented each TICK_INTERVAL). */
    private static long simTicks = 0;

    /** Counter for cultivators lost to void beasts (per task spec). */
    private static int lostToVoidBeasts = 0;

    // ── Initialization ──────────────────────────────────────────────────

    /**
     * Initialize the simulation if not already done. Seeds the starry sky
     * with up to {@link #MAX_CULTIVATORS} wandering cultivators at random
     * positions, each assigned a destination (a teleport array or random
     * point).
     */
    private static void initialize() {
        if (initialized) return;
        initialized = true;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int count = 20 + rng.nextInt(15); // 20-34 cultivators at start
        for (int i = 0; i < count; i++) {
            CULTIVATORS.add(spawnNewCultivator(rng));
        }
        Ergenverse.LOGGER.info("[StarrySkySimulation] Initialized with {} wandering cultivators.",
                CULTIVATORS.size());
    }

    /**
     * Create a new wandering cultivator at a random position with a random
     * destination.
     */
    private static WanderingCultivator spawnNewCultivator(ThreadLocalRandom rng) {
        BlockPos start = randomPos(rng);
        BlockPos dest = randomDestination(rng);
        return new WanderingCultivator(
                UUID.randomUUID(),
                generateName(rng),
                generateRealm(rng),
                start,
                dest,
                0.5 + rng.nextDouble() * 1.5 // 0.5 - 2.0 blocks/sec
        );
    }

    /**
     * Generate a random position in the starry sky. Spawns near teleport
     * arrays or in the open void.
     */
    private static BlockPos randomPos(ThreadLocalRandom rng) {
        if (TELEPORT_ARRAYS.length > 0 && rng.nextDouble() < 0.5) {
            // Spawn near a teleport array
            BlockPos array = TELEPORT_ARRAYS[rng.nextInt(TELEPORT_ARRAYS.length)];
            return array.offset(
                    rng.nextInt(-64, 64),
                    rng.nextInt(-16, 16),
                    rng.nextInt(-64, 64));
        }
        return new BlockPos(
                rng.nextInt(-3000, 3000),
                60 + rng.nextInt(80),
                rng.nextInt(-3000, 3000));
    }

    /**
     * Generate a destination — either a teleport array or a random point.
     */
    private static BlockPos randomDestination(ThreadLocalRandom rng) {
        if (TELEPORT_ARRAYS.length > 0 && rng.nextDouble() < 0.6) {
            return TELEPORT_ARRAYS[rng.nextInt(TELEPORT_ARRAYS.length)];
        }
        return randomPos(rng);
    }

    // ── Canon-style name + realm generators ─────────────────────────────

    private static final String[] SURNAMES = {
            "Wang", "Li", "Zhang", "Liu", "Chen", "Yang", "Huang", "Zhao", "Wu", "Zhou",
            "Xu", "Sun", "Ma", "Zhu", "Hu", "Lin", "Guo", "He", "Gao", "Luo"
    };
    private static final String[] GIVEN_NAMES = {
            "Lin", "Tian", "Yun", "Ming", "Han", "Yue", "Feng", "Xue", "Qing", "Xuan",
            "Hao", "Chen", "Jian", "Dao", "Shan", "Ling", "Ruo", "Yu", "Bing", "Shi"
    };
    private static final String[] REALMS = {
            "Qi Condensation", "Foundation Establishment", "Core Formation",
            "Nascent Soul", "Soul Formation", "Soul Transformation",
            "Nirvana Scryer", "Heaven Blight", "Third Step"
    };

    private static String generateName(ThreadLocalRandom rng) {
        return SURNAMES[rng.nextInt(SURNAMES.length)]
                + " " + GIVEN_NAMES[rng.nextInt(GIVEN_NAMES.length)];
    }

    private static String generateRealm(ThreadLocalRandom rng) {
        return REALMS[rng.nextInt(REALMS.length)];
    }

    // ── Tick Loop ───────────────────────────────────────────────────────

    /**
     * Tick the simulation. Called every server tick from
     * {@link StarrySkyEvents#onServerTick}. Internally gated to fire once
     * per {@link #TICK_INTERVAL} server ticks.
     *
     * @param server the Minecraft server
     */
    public static void tick(MinecraftServer server) {
        // Only tick if the starry sky dimension is loaded (avoid wasting CPU)
        ServerLevel starrySky = server.getLevel(STARRY_SKY_KEY);
        if (starrySky == null) return;

        initialize();

        long gameTime = starrySky.getGameTime();
        if (gameTime % TICK_INTERVAL != 0) return;

        simTicks++;

        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // ── Advance each cultivator ──────────────────────────────────
        for (int i = 0; i < CULTIVATORS.size(); i++) {
            WanderingCultivator c = CULTIVATORS.get(i);
            advanceCultivator(c, rng);

            // ── Void beast encounter check (per task spec) ──────────
            // A cultivator's simulated path crosses void beast territory.
            if (VoidBeastEncounter.checkCultivatorLost(c, rng)) {
                CULTIVATORS.set(i, spawnNewCultivator(rng)); // replace
                lostToVoidBeasts++;
                Ergenverse.LOGGER.debug("[StarrySkySimulation] Cultivator {} lost to void beasts. "
                        + "Total lost: {}", c.name, lostToVoidBeasts);
            }
        }

        // ── Occasionally spawn new cultivators (caravans arriving) ────
        if (simTicks % 60 == 0 && CULTIVATORS.size() < MAX_CULTIVATORS
                && rng.nextDouble() < 0.3) {
            CULTIVATORS.add(spawnNewCultivator(rng));
        }

        // ── Reify nearby cultivators for any player in the dimension ──
        reifyNearbyCultivators(starrySky);
    }

    /**
     * Advance a cultivator toward their destination by their speed.
     * If arrived, pick a new destination. If near a teleport array,
     * chance to teleport (canon means).
     */
    private static void advanceCultivator(WanderingCultivator c, ThreadLocalRandom rng) {
        double dx = c.destination.getX() - c.current.getX();
        double dy = c.destination.getY() - c.current.getY();
        double dz = c.destination.getZ() - c.current.getZ();
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (dist < 4.0) {
            // Arrived — pick a new destination
            c.destination = randomDestination(rng);
            return;
        }

        // Canon teleport: if cultivator is near a teleport array (and is
        // close to their destination anyway), they may "teleport" (jump to
        // a distant point). This is the canon means of long-distance travel.
        for (BlockPos array : TELEPORT_ARRAYS) {
            if (array.equals(c.destination) && dist < TELEPORT_ARRAY_RANGE
                    && rng.nextDouble() < 0.05) {
                BlockPos farPoint = randomDestination(rng);
                Ergenverse.LOGGER.debug("[StarrySkySimulation] {} teleported via array at {} → {}",
                        c.name, c.current, farPoint);
                c.current = farPoint;
                c.destination = randomDestination(rng);
                return;
            }
        }

        // Move toward destination at c.speed blocks per tick-step
        double step = Math.min(c.speed, dist);
        double nx = c.current.getX() + (dx / dist) * step;
        double ny = c.current.getY() + (dy / dist) * step;
        double nz = c.current.getZ() + (dz / dist) * step;
        c.current = new BlockPos((int) nx, (int) ny, (int) nz);
    }

    // ── Reification (spawn actual entities for nearby players) ──────────

    /**
     * For each player in the starry sky dimension, spawn actual
     * EntityCultivator instances at the simulated positions of nearby
     * cultivators. Each simulated cultivator gets at most one spawned
     * entity (tracked by UUID). If the cultivator moves out of range,
     * the entity is despawned.
     *
     * <p>This is what makes the dimension "feel alive" — players see
     * cultivators flying through space on their fixed routes, but the
     * underlying simulation continues even when no player is watching.
     */
    private static void reifyNearbyCultivators(ServerLevel starrySky) {
        // Find all players currently in the starry sky dimension
        List<ServerPlayer> playersInDim = new ArrayList<>();
        for (ServerPlayer player : starrySky.getServer().getPlayerList().getPlayers()) {
            if (player.level() == starrySky) {
                playersInDim.add(player);
            }
        }
        if (playersInDim.isEmpty()) {
            // No players in dim — despawn any reified cultivator entities
            // (the simulation continues, but no entities need to be loaded)
            despawnAllReified(starrySky);
            return;
        }

        // For each cultivator, check if any player is within REIFICATION_RANGE
        for (WanderingCultivator c : CULTIVATORS) {
            boolean anyPlayerNear = false;
            for (ServerPlayer p : playersInDim) {
                if (c.current.closerThan(p.blockPosition(), REIFICATION_RANGE)) {
                    anyPlayerNear = true;
                    break;
                }
            }
            if (anyPlayerNear) {
                spawnOrSyncReified(starrySky, c);
            } else {
                // No player nearby — despawn the reified entity (if any)
                if (c.reifiedEntityId != null) {
                    despawnReified(starrySky, c);
                }
            }
        }
    }

    /**
     * Spawn an EntityCultivator at the cultivator's simulated position, or
     * if already spawned, move it to the current simulated position.
     */
    private static void spawnOrSyncReified(ServerLevel level, WanderingCultivator c) {
        if (c.reifiedEntityId != null) {
            // Find and move the existing entity
            Mob existing = findReifiedMob(level, c.reifiedEntityId);
            if (existing != null) {
                existing.moveTo(c.current.getX() + 0.5, c.current.getY(),
                        c.current.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);
                return;
            } else {
                // Entity was removed (e.g., player killed it, or unloaded)
                c.reifiedEntityId = null;
            }
        }

        // Spawn a new EntityCultivator
        EntityCultivator cultivator = EREntityTypes.CULTIVATOR.get().create(level);
        if (cultivator == null) {
            Ergenverse.LOGGER.warn("[StarrySkySimulation] Failed to create EntityCultivator for {}.",
                    c.name);
            return;
        }
        cultivator.moveTo(c.current.getX() + 0.5, c.current.getY(), c.current.getZ() + 0.5,
                level.random.nextFloat() * 360.0F, 0.0F);
        // Use a simulated character ID so the canon DB lookup returns null,
        // and the EntityCultivator falls back to defaults — we override the
        // display name + realm directly to reflect the simulation state.
        cultivator.setCharacterId("starry_sky_wandering_" + c.uuid);
        cultivator.setDisplayNameCn(c.name + " (" + c.realm + ")");
        cultivator.setCultivationRealm(c.realm);
        cultivator.setCustomName(Component.literal(
                "\u00A7b" + c.name + " \u00A77[" + c.realm + "]"));
        cultivator.setCustomNameVisible(true);
        level.addFreshEntity(cultivator);
        c.reifiedEntityId = cultivator.getUUID();
    }

    /**
     * Despawn a reified cultivator entity (player out of range or left dim).
     */
    private static void despawnReified(ServerLevel level, WanderingCultivator c) {
        if (c.reifiedEntityId == null) return;
        Mob mob = findReifiedMob(level, c.reifiedEntityId);
        if (mob != null) {
            mob.discard();
        }
        c.reifiedEntityId = null;
    }

    /**
     * Despawn ALL reified cultivator entities (called when no players are
     * in the starry sky dimension). The simulation continues — only the
     * visual entities are removed.
     */
    public static void despawnAllReified(ServerLevel level) {
        for (WanderingCultivator c : CULTIVATORS) {
            if (c.reifiedEntityId != null) {
                despawnReified(level, c);
            }
        }
    }

    /**
     * Find a reified mob by UUID. Uses level's entity lookup.
     */
    @Nullable
    private static Mob findReifiedMob(ServerLevel level, UUID uuid) {
        net.minecraft.world.entity.Entity e = level.getEntity(uuid);
        return e instanceof Mob mob ? mob : null;
    }

    // ── Player enter/leave hooks ────────────────────────────────────────

    /**
     * Called when a player enters the starry sky dimension. Forces an
     * immediate reification scan so the player sees cultivators right away
     * instead of waiting for the next sim tick.
     */
    public static void onPlayerEnter(ServerPlayer player) {
        ServerLevel starrySky = player.getServer().getLevel(STARRY_SKY_KEY);
        if (starrySky == null) return;
        initialize();
        reifyNearbyCultivators(starrySky);
        player.sendSystemMessage(Component.literal(
                "\u00A7b\u2726 You enter the Starry Sky \u2014 the void between cultivation planets."));
        player.sendSystemMessage(Component.literal(
                "\u00A77Cultivators fly through this space on routes older than your sect. "
                + "Tread carefully — void beasts hunt here."));
        Ergenverse.LOGGER.info("[StarrySkySimulation] Player {} entered starry sky. "
                + "{} cultivators simulated, {} lost to void beasts so far.",
                player.getName().getString(), CULTIVATORS.size(), lostToVoidBeasts);
    }

    /**
     * Called when a player leaves the starry sky dimension. Despawns
     * reified entities — the simulation continues in the background.
     */
    public static void onPlayerLeave(ServerPlayer player) {
        ServerLevel starrySky = player.getServer().getLevel(STARRY_SKY_KEY);
        if (starrySky == null) return;
        despawnAllReified(starrySky);
        Ergenverse.LOGGER.info("[StarrySkySimulation] Player {} left starry sky. "
                + "Simulation continues ({} cultivators).",
                player.getName().getString(), CULTIVATORS.size());
    }

    // ── Public accessors ────────────────────────────────────────────────

    /** @return the current number of simulated cultivators. */
    public static int getCultivatorCount() {
        return CULTIVATORS.size();
    }

    /** @return the total number of cultivators lost to void beasts. */
    public static int getLostToVoidBeasts() {
        return lostToVoidBeasts;
    }

    /** @return the list of teleport arrays (for void beast encounter placement). */
    public static BlockPos[] getTeleportArrays() {
        return TELEPORT_ARRAYS.clone();
    }

    // ── Inner data class ────────────────────────────────────────────────

    /**
     * Pure-data representation of a wandering cultivator in the starry sky.
     * This is NOT a Minecraft entity — it's a simulation record that is
     * reified into an actual entity only when a player is nearby.
     */
    public static final class WanderingCultivator {
        public final UUID uuid;
        public final String name;
        public final String realm;
        public BlockPos current;
        public BlockPos destination;
        public final double speed; // blocks per simulation tick

        @Nullable
        public UUID reifiedEntityId; // UUID of the spawned EntityCultivator, if any

        public WanderingCultivator(UUID uuid, String name, String realm,
                                    BlockPos current, BlockPos destination, double speed) {
            this.uuid = uuid;
            this.name = name;
            this.realm = realm;
            this.current = current;
            this.destination = destination;
            this.speed = speed;
        }
    }
}
