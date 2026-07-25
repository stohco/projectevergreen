package dev.ergenverse.runtime;

/**
 * WorldRuntime — the central simulation authority for the Er Gen Verse.
 *
 * <p><b>ARCHITECTURE PIVOT (2026-07-25):</b> The user's directive is clear:
 * Minecraft is the rendering and interaction engine, NOT the source of truth.
 * The canonical blueprint plus the simulation own the world's state; Minecraft
 * entities, chunks, and blocks are materialized views of that state.
 *
 * <p>The data flow is:
 * <pre>
 *   PlanetSuzakuBlueprint  (canonical, immutable, hand-authored)
 *           ↓
 *   WorldRuntime           (the single authority — one JVM, one save, one player)
 *     ├── TerrainRuntime        (what blocks exist where — loaded from blueprint, not generated)
 *     ├── NPCRuntime            (NPCs load, never spawn — serialize/deserialize on chunk unload)
 *     ├── SettlementRuntime     (structures instantiated, not generated — every block hand-placed)
 *     ├── BeastRuntime          (spirit beasts live in the world, not spawned on demand)
 *     ├── RelationshipRuntime   (NPC-to-NPC relationships — the social graph)
 *     ├── MemoryRuntime         (what each actor remembers — deep memory, never forgotten)
 *     ├── CultivationRuntime    (realm progression — knowledge is progression, not XP)
 *     ├── EconomyRuntime        (spirit stones, trade routes, market state)
 *     ├── EventRuntime          (WorldEventBus — the simulation tick's nervous system)
 *     └── WeatherRuntime        (canon weather: Snow Domain blizzards, Sea of Devils storms)
 *           ↓
 *   Minecraft Entity/Block Materialization  (chunks load from blueprint, entities load from save)
 *           ↓
 *   Renderer / UI
 * </pre>
 *
 * <h2>Single-player maximalism (Article XLIII)</h2>
 * <p>One JVM. One save. One player. One authority. There is no server/client
 * split in the architecture — only Simulation Layer → Presentation Layer.
 * Minecraft's internal logical server still exists (we can't remove that),
 * but the code is written as if there is one thread of authority.
 *
 * <h2>The world as Git</h2>
 * <p>The save is:
 * <pre>
 *   Blueprint  +  Simulation history (deltas)
 * </pre>
 * NOT:
 * <pre>
 *   Random terrain  +  Player edits
 * </pre>
 *
 * <p>Chunk generation becomes <b>loading</b>:
 * <pre>
 *   Chunk requested
 *     ↓
 *   Blueprint database  (what already exists, canonically)
 *     ↓
 *   Block placements    (instantiated, not generated)
 *     ↓
 *   Apply simulation deltas  (what the simulation has changed since day 0)
 *     ↓
 *   Done
 * </pre>
 *
 * <p>There is no randomness. Generation becomes loading.
 *
 * <h2>UUIDs for everything</h2>
 * <p>Every important thing has a permanent identity:
 * {@code wang_lin}, {@code old_chen}, {@code li_muwan}, {@code wolf_pack_17},
 * {@code jade_slip_428}, {@code spirit_tree_31}. Not entity IDs. Permanent IDs.
 * The simulation references UUIDs; Minecraft entities are materialized views.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class WorldRuntime {

    private static WorldRuntime instance;

    // ── Subsystem references ──
    private final TerrainRuntime terrain;
    private final NPCRuntime npcs;
    private final SettlementRuntime settlements;
    private final BeastRuntime beasts;
    private final RelationshipRuntime relationships;
    private final MemoryRuntime memory;
    private final CultivationRuntime cultivation;
    private final EconomyRuntime economy;
    private final EventRuntime events;
    private final WeatherRuntime weather;

    // ── The canonical blueprint ──
    private final PlanetSuzakuBlueprint blueprint;

    private boolean initialized = false;

    private WorldRuntime() {
        this.blueprint = PlanetSuzakuBlueprint.canonical();
        this.terrain = new TerrainRuntime(blueprint);
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
     * Initialize the runtime from the canonical blueprint. Called on
     * server start. Idempotent — safe to call multiple times.
     */
    public void initialize() {
        if (initialized) return;
        blueprint.validate();
        terrain.loadFromBlueprint();
        npcs.loadAll();
        settlements.loadAll();
        beasts.loadAll();
        relationships.loadAll();
        memory.loadAll();
        cultivation.loadAll();
        economy.loadAll();
        events.start();
        weather.start();
        initialized = true;
    }

    /** The canonical, immutable blueprint for Planet Suzaku. */
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }

    public TerrainRuntime terrain() { return terrain; }
    public NPCRuntime npcs() { return npcs; }
    public SettlementRuntime settlements() { return settlements; }
    public BeastRuntime beasts() { return beasts; }
    public RelationshipRuntime relationships() { return relationships; }
    public MemoryRuntime memory() { return memory; }
    public CultivationRuntime cultivation() { return cultivation; }
    public EconomyRuntime economy() { return economy; }
    public EventRuntime events() { return events; }
    public WeatherRuntime weather() { return weather; }

    public boolean isInitialized() { return initialized; }
}
