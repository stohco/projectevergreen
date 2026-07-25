package dev.ergenverse.runtime;

import dev.ergenverse.runtime.materialize.ActorMaterializer;
import dev.ergenverse.runtime.materialize.ChunkMaterializer;
import dev.ergenverse.runtime.spatial.Quadtree;
import dev.ergenverse.runtime.spatial.SpatialIndex;

/**
 * WorldRuntime — the central simulation authority for the Er Gen Verse.
 *
 * <p><b>ARCHITECTURE (2026-07-25, refined):</b>
 *
 * <p>The user's directive is clear: "You're writing something closer to
 * Er Gen World Simulator + Minecraft Adapter, where Forge is just the
 * adapter that renders entities, renders blocks, handles input, saves data,
 * plays sounds. Everything else — canon, simulation, actor reasoning,
 * settlements, and world state — belongs to your own engine."
 *
 * <h2>Immutable vs Mutable split</h2>
 * <p>The user's refinement (2026-07-25): "I'd divide it into immutable and
 * mutable. Blueprint contains Terrain, Structures, Countries, Spirit Veins,
 * Roads, Cities, Static formations — those are never simulated. Runtime
 * contains People, Animals, Relationships, Weather, Economy, Events,
 * Politics, Cultivation, Rumors, Memories. Notice terrain disappeared.
 * Terrain isn't 'running.' It's just there."
 *
 * <p>The data flow is:
 * <pre>
 *   PlanetSuzakuBlueprint  (immutable: terrain, structures, countries, veins, roads, cities, formations)
 *           ↓
 *   SpatialIndex           (quadtree — O(log n) queries for chunk materialization)
 *           ↓
 *   WorldRuntime           (mutable: people, animals, relationships, weather, economy, events, cultivation, memories)
 *     ├── NPCRuntime           (actors load, never spawn — materializeActor/dematerializeActor)
 *     ├── SettlementRuntime    (structures instantiated by ChunkMaterializer, not generated)
 *     ├── BeastRuntime         (beasts load, never spawn — same materialize/dematerialize cycle)
 *     ├── RelationshipRuntime  (NPC-to-NPC social graph)
 *     ├── MemoryRuntime        (deep memory, never forgotten)
 *     ├── CultivationRuntime   (knowledge is progression, not XP)
 *     ├── EconomyRuntime       (spirit stones, trade routes, markets)
 *     ├── EventRuntime         (WorldEventBus — simulation nervous system)
 *     └── WeatherRuntime       (canon weather: Snow Domain blizzards, etc.)
 *           ↓
 *   Materializers           (ChunkMaterializer + ActorMaterializer)
 *           ↓
 *   Minecraft Entity/Block Materialization  (chunks loaded from blueprint, actors materialized on chunk load)
 *           ↓
 *   Renderer/UI
 * </pre>
 *
 * <h2>Single-player maximalism (Article XLIII)</h2>
 * <p>One JVM. One save. One player. One authority. There is no server/client
 * split in the architecture — only Simulation Layer → Presentation Layer.
 *
 * <h2>The world as Git</h2>
 * <p>The save is: Blueprint + Simulation history (deltas). NOT Random terrain
 * + Player edits. Chunk generation becomes LOADING via the ChunkMaterializer.
 *
 * <h2>Permanent UUIDs</h2>
 * <p>Every important thing has a permanent identity (see {@link CanonUUID}):
 * NPCs, spirit beasts, artifacts, spirit veins, herb patches, buildings,
 * ancient formations, teleport arrays, named caves, sect halls, important
 * trees, ancient battlefields, storage rings, flying swords. Not entity IDs.
 * Permanent IDs. The simulation references UUIDs; Minecraft entities are
 * materialized views.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldRuntime {

    private static WorldRuntime instance;

    // ── The canonical, immutable blueprint ──
    private final PlanetSuzakuBlueprint blueprint;

    // ── Spatial index for O(log n) chunk queries ──
    private final SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex;

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

    // ── Materializers (bridge between simulation and Minecraft) ──
    private ChunkMaterializer chunkMaterializer;
    private ActorMaterializer actorMaterializer;

    private boolean initialized = false;

    private WorldRuntime() {
        this.blueprint = PlanetSuzakuBlueprint.canonical();
        // Quadtree spanning -100,000 to +100,000 blocks (200km x 200km world)
        this.spatialIndex = new Quadtree<>(
                -100_000, -100_000, 100_000, 100_000, 8, 12);

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

    /**
     * Get the singleton WorldRuntime instance. The runtime is initialized
     * on first access (lazy). In single-player, this happens when the
     * integrated server starts.
     */
    public static synchronized WorldRuntime get() {
        if (instance == null) {
            instance = new WorldRuntime();
        }
        return instance;
    }

    /**
     * Initialize the runtime from the canonical blueprint.
     *
     * <p>The initialization order matters:
     * <ol>
     *   <li>Validate the blueprint (internal consistency)</li>
     *   <li>Build the spatial index (O(n) build, enables O(log n) queries)</li>
     *   <li>Register settlement materializers (link builders to blueprint locations)</li>
     *   <li>Install chunk hooks (ChunkMaterializer + ActorMaterializer)</li>
     *   <li>Load all actors from the save (or instantiate from blueprint on day 0)</li>
     *   <li>Start the event bus and weather simulation</li>
     * </ol>
     *
     * <p>Per the user's directive: "The village shouldn't be built after
     * runtime initializes. The runtime should own building." So this method
     * registers materializers; the actual block placement happens lazily
     * when Minecraft requests the relevant chunks.
     */
    public void initialize() {
        if (initialized) return;

        // 1. Validate blueprint
        blueprint.validate();

        // 2. Build spatial index
        buildSpatialIndex();

        // 3. Register materializers (stub — wired in future rounds)
        registerMaterializers();

        // 4. Load mutable subsystems
        npcs.loadAll();
        settlements.loadAll();
        beasts.loadAll();
        relationships.loadAll();
        memory.loadAll();
        cultivation.loadAll();
        economy.loadAll();

        // 5. Start event bus + weather
        events.start();
        weather.start();

        initialized = true;
    }

    /**
     * Build the spatial index from the blueprint's canonical locations.
     * This is O(n) — done once at initialization. After this, chunk
     * materialization queries are O(log n).
     */
    private void buildSpatialIndex() {
        for (PlanetSuzakuBlueprint.CanonLocation loc : blueprint.allLocations().values()) {
            // Each location occupies a ~100x100 block footprint centered on its coordinate.
            // The actual structure size is determined by the settlement builder.
            int half = 50;
            spatialIndex.insert(loc, loc.x - half, loc.z - half, loc.x + half, loc.z + half);
        }
    }

    /**
     * Register the chunk and actor materializers. In future rounds, this
     * will create a PlanetSuzakuChunkMaterializer that queries the spatial
     * index and places blocks, and a CanonActorMaterializer that
     * materializes actors by canon UUID.
     */
    private void registerMaterializers() {
        // TODO: Create concrete PlanetSuzakuChunkMaterializer that:
        //   1. Queries spatialIndex for the chunk's bounds
        //   2. For each intersecting location, calls the corresponding builder
        //   3. Applies simulation deltas (changed blocks)
        // For now, the existing SpawnEventHandler + WangFamilyVillageBuilder
        // handle village placement. The materializer will replace this.
        this.chunkMaterializer = null; // wired in future round

        // TODO: Create concrete CanonActorMaterializer that:
        //   1. Looks up the actor's simulation state by canon UUID
        //   2. Creates a Minecraft entity (EntityCultivator or SpiritBeastEntity)
        //   3. Sets the entity's persistence UUID to the canon UUID
        //   4. Places the entity at its canonical location
        //   5. On chunk unload, serializes state and destroys the entity
        this.actorMaterializer = null; // wired in future round
    }

    // ── Accessors ──

    /** The canonical, immutable blueprint for Planet Suzaku. */
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }

    /**
     * The spatial index for O(log n) chunk queries.
     * Used by the ChunkMaterializer to find what objects intersect a chunk.
     */
    public SpatialIndex<PlanetSuzakuBlueprint.CanonLocation> spatialIndex() { return spatialIndex; }

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
