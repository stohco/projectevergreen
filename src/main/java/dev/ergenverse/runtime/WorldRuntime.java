package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import dev.ergenverse.runtime.layer.BlueprintLayer;
import dev.ergenverse.runtime.layer.CompositeWorldLayer;
import dev.ergenverse.runtime.layer.PlayerLayer;
import dev.ergenverse.runtime.layer.SimulationLayer;
import dev.ergenverse.runtime.layer.WorldFacade;
import dev.ergenverse.runtime.layer.WorldLayer;
import dev.ergenverse.runtime.materialize.ActorMaterializer;
import dev.ergenverse.runtime.materialize.CanonActorMaterializer;
import dev.ergenverse.runtime.materialize.ChunkMaterializer;
import dev.ergenverse.runtime.materialize.PlanetSuzakuChunkMaterializer;
import dev.ergenverse.runtime.persist.WorldDeltaSavedData;
import dev.ergenverse.runtime.spatial.Quadtree;
import dev.ergenverse.runtime.spatial.SpatialIndex;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

/**
 * WorldRuntime — the central simulation authority for the Er Gen Verse.
 *
 * <p><b>ARCHITECTURE (CRON-69, ten-point refactor):</b> the world is now a
 * composition of {@link WorldLayer}s, journaled by a unified {@link WorldDeltaStore},
 * and materialized by stateless functions. The user's directive, point by point:
 *
 * <ol>
 *   <li><b>One delta language.</b> Every change — block, actor, relationship —
 *       is a {@link dev.ergenverse.runtime.delta.WorldDelta}. One journal,
 *       one persistence mechanism.</li>
 *   <li><b>Provenance, not ownership.</b> {@link Provenance} (CANON/SIMULATION/PLAYER)
 *       describes where state came from. No one "owns" a mountain.</li>
 *   <li><b>Composable layers.</b> {@link CompositeWorldLayer} asks layers in
 *       order; no hardcoded priority in a manager. Insert a QuestLayer later.</li>
 *   <li><b>No "removed" blocks.</b> Mining = {@code BlockChangeDelta(pos, "minecraft:air", PLAYER)}.
 *       Air is just a state.</li>
 *   <li><b>Invisible manager.</b> Gameplay writes {@code runtime.world().setBlock(...)}.
 *       The {@link WorldFacade} routes to the journal + live level.</li>
 *   <li><b>Stateless materializer.</b> {@link PlanetSuzakuChunkMaterializer#materialize}
 *       is a pure function of (chunk, runtime, layers).</li>
 *   <li><b>Chunk-scoped answers.</b> Each layer answers "what do I contribute to
 *       this chunk?" — never "regenerate all 65k blocks."</li>
 *   <li><b>Blueprint never answers getBlock.</b> {@link PlanetSuzakuBlueprint}
 *       answers at structure granularity; {@link BlueprintLayer#getBlock} returns null.</li>
 *   <li><b>Deterministic decoration.</b> {@link dev.ergenverse.runtime.worldgen.DeterministicTerrainGenerator}
 *       fills accents deterministically; handcraft reserved for narrative places.</li>
 *   <li><b>Milestone.</b> Planet Suzaku loads from Blueprint + Layers; Wang Lin
 *       materializes; a broken wall persists across reload; a new save resets it.</li>
 * </ol>
 *
 * <h2>Single-player maximalism (Article XLIII)</h2>
 * <p>One JVM. One save. One player. One authority. No server/client split in the
 * architecture — only Simulation Layer → Presentation Layer.
 *
 * <h2>The world as Git</h2>
 * <p>The save is: Blueprint (immutable) + WorldDeltaStore (the journal of every
 * change since day 0). The blueprint is NEVER rewritten; a fresh save starts
 * from identical canon.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldRuntime {

    private static WorldRuntime instance;

    // ── The canonical, immutable blueprint ──
    private final PlanetSuzakuBlueprint blueprint;

    // ── Spatial index for O(log n) chunk queries ──
    private final SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex;

    // ── The unified delta journal (persistence unit) ──
    private final WorldDeltaStore deltaStore;

    // ── The composable world layers ──
    private final CompositeWorldLayer worldLayer;
    private final WorldFacade worldFacade;

    // ── Mutable subsystems (simulation state) ──
    private final NPCRuntime npcs;
    private final SettlementRuntime settlements;
    private final BeastRuntime beasts;
    private final RelationshipRuntime relationships;
    private final MemoryRuntime memory;
    private final CultivationRuntime cultivation;
    private final EconomyRuntime economy;
    private final EventRuntime events;
    private final WeatherRuntime weather;

    // ── Materializers (stateless bridges to Minecraft) ──
    private ChunkMaterializer chunkMaterializer;
    private CanonActorMaterializer actorMaterializer;

    // ── The bound Planet Suzaku level (set on initialize) ──
    private ServerLevel suzakuLevel;

    // ── The persisted delta journal (set on initialize) ──
    private WorldDeltaSavedData savedData;

    private boolean initialized = false;

    private WorldRuntime() {
        this.blueprint = PlanetSuzakuBlueprint.canonical();
        // Quadtree spanning -100,000 to +100,000 blocks (200km x 200km world)
        this.spatialIndex = new Quadtree<>(
                -100_000, -100_000, 100_000, 100_000, 8, 12);

        // The unified delta journal — one language, one persistence mechanism.
        this.deltaStore = new WorldDeltaStore();

        // Compose the layers in priority (query) order: PLAYER > SIMULATION > CANON.
        // Inserting a QuestLayer or TemporaryEventLayer later is a one-line change.
        this.worldLayer = new CompositeWorldLayer(List.of(
                new PlayerLayer(deltaStore),
                new SimulationLayer(deltaStore),
                new BlueprintLayer(blueprint, spatialIndex)
        ));

        // The invisible manager — gameplay writes runtime.world().setBlock(...).
        this.worldFacade = new WorldFacade(deltaStore);

        this.npcs = new NPCRuntime(blueprint);
        this.settlements = new SettlementRuntime(blueprint);
        this.beasts = new BeastRuntime(blueprint);
        this.relationships = new RelationshipRuntime(blueprint);
        this.memory = new MemoryRuntime(blueprint);
        this.cultivation = new CultivationRuntime(blueprint);
        this.economy = new EconomyRuntime(blueprint);
        this.events = new EventRuntime(blueprint);
        this.weather = new WeatherRuntime(blueprint);
    }

    /** Get the singleton WorldRuntime instance (lazy). */
    public static synchronized WorldRuntime get() {
        if (instance == null) {
            instance = new WorldRuntime();
        }
        return instance;
    }

    /**
     * Initialize the runtime, bound to the Planet Suzaku server level.
     *
     * <p>Order:
     * <ol>
     *   <li>Validate the blueprint</li>
     *   <li>Build the spatial index</li>
     *   <li>Bind the facade + actor materializer to the level</li>
     *   <li>Load (or create) the persisted delta journal</li>
     *   <li>Register the concrete materializers</li>
     *   <li>Load mutable subsystems</li>
     *   <li>Start the event bus and weather</li>
     * </ol>
     */
    public void initialize(ServerLevel suzakuLevel) {
        if (initialized) return;
        this.suzakuLevel = suzakuLevel;

        // 1. Validate blueprint
        blueprint.validate();

        // 2. Build spatial index
        buildSpatialIndex();

        // 3. Bind the facade + actor materializer to the live level
        worldFacade.bind(suzakuLevel);

        // 4. Load (or create) the persisted delta journal — this fills the
        //    deltaStore from NBT, so PLAYER/SIMULATION changes survive reload.
        try {
            this.savedData = WorldDeltaSavedData.getOrCreate(suzakuLevel, deltaStore);
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] WorldDeltaSavedData load failed: {}", t.getMessage(), t);
        }

        // 5. Register the concrete, stateless materializers
        this.chunkMaterializer = new PlanetSuzakuChunkMaterializer();
        this.actorMaterializer = new CanonActorMaterializer();
        this.actorMaterializer.bind(suzakuLevel);

        // 6. Load mutable subsystems
        npcs.loadAll();
        settlements.loadAll();
        beasts.loadAll();
        relationships.loadAll();
        memory.loadAll();
        cultivation.loadAll();
        economy.loadAll();

        // 7. Start event bus + weather
        events.start();
        weather.start();

        initialized = true;
        Ergenverse.LOGGER.info("[Ergenverse] WorldRuntime initialized. Blueprint: {} locations, spatial index: {} entries, delta journal: {} recorded changes.",
                blueprint.allLocations().size(), spatialIndex.size(), deltaStore.size());
    }

    /**
     * Build the spatial index from the blueprint's canonical locations.
     * O(n) once at init; enables O(log n) chunk materialization queries.
     */
    private void buildSpatialIndex() {
        for (PlanetSuzakuBlueprint.CanonLocation loc : blueprint.allLocations().values()) {
            int half = 50;
            spatialIndex.insert(loc, loc.x - half, loc.z - half, loc.x + half, loc.z + half);
        }
    }

    // ── Accessors ──

    /** The canonical, immutable blueprint for Planet Suzaku. */
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }

    /** The spatial index for O(log n) chunk queries. */
    public SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex() { return spatialIndex; }

    /** The unified delta journal (persistence unit). */
    public WorldDeltaStore deltaStore() { return deltaStore; }

    /** The composable world layers (PLAYER > SIMULATION > CANON). */
    public CompositeWorldLayer worldLayer() { return worldLayer; }

    /**
     * The invisible world-write facade. Gameplay code calls
     * {@code runtime.world().setPlayerBlock(...)} or {@code setSimulationBlock(...)} —
     * never touches the journal or layers directly.
     */
    public WorldFacade world() { return worldFacade; }

    /** The bound Planet Suzaku server level, or null before initialize. */
    public ServerLevel suzakuLevel() { return suzakuLevel; }

    public NPCRuntime npcs() { return npcs; }
    public SettlementRuntime settlements() { return settlements; }
    public BeastRuntime beasts() { return beasts; }
    public RelationshipRuntime relationships() { return relationships; }
    public MemoryRuntime memory() { return memory; }
    public CultivationRuntime cultivation() { return cultivation; }
    public EconomyRuntime economy() { return economy; }
    public EventRuntime events() { return events; }
    public WeatherRuntime weather() { return weather; }

    public ChunkMaterializer chunkMaterializer() { return chunkMaterializer; }
    public ActorMaterializer actorMaterializer() { return actorMaterializer; }

    public boolean isInitialized() { return initialized; }
}
