package dev.ergenverse.core;

import dev.ergenverse.block.ErgenverseBlocks;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.canon.CanonEngine;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.cosmos.CosmologicalTree;
import dev.ergenverse.ecology.CausalEcology;
import dev.ergenverse.perception.PerceptionEngine;
import dev.ergenverse.wanglin.WangLinArsenalCommand;
import dev.ergenverse.wanglin.WangLinCosmologyRegistry;
import dev.ergenverse.wanglin.WangLinItems;
import dev.ergenverse.wanglin.ai.WangLinPersonality;
import dev.ergenverse.wanglin.bead.BeadDimension;
import dev.ergenverse.wanglin.bead.BeadFunctionMenu;
import dev.ergenverse.wanglin.registry.WangLinMasterRegistry;
import dev.ergenverse.world.WorldLaws;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ergenverse — A Living Cultivation World.
 *
 * <p>This mod reconstructs the Er Gen cultivation multiverse inside Minecraft.
 *
 * <p><b>The Prime Directive ({@link WorldPhilosophy}):</b>
 * <blockquote>
 *   Never hide or reveal objects because of the player's level. Hide or
 *   reveal interactions according to the laws of the world. The world is
 *   objective and exists independently of the player. Cultivation increases
 *   a character's ability to perceive, understand, and interact with deeper
 *   layers of reality — it does not create or replace reality.
 * </blockquote>
 *
 * <h2>Phase priority</h2>
 * <ol>
 *   <li><b>Phase 0 — The world exists before you arrive.</b> Living ecology,
 *       atmosphere, danger, no cultivation, no UI, no Wang Lin.</li>
 *   <li><b>Phase 1 — Discover cultivation.</b> Find a spirit vein, meditate,
 *       break through. Perception shift: deer was always a Spirit Deer;
 *       you just didn't know.</li>
 *   <li><b>Phase 2 — Find a sect (ecosystem).</b> Outer → inner → core →
 *       elders → patriarch, plus spirit beasts, herb fields, alchemy,
 *       formations, nearby villages.</li>
 *   <li><b>Phase 3 — World Pulse.</b> Every Minecraft day, the world
 *       evolves independently of the player.</li>
 *   <li><b>Phase 4 — Landmarks + Ocean dread.</b> Rare memorable landmarks.
 *       Ocean depth system. World-scale entities whose presence reshapes
 *       the environment.</li>
 *   <li><b>Phase 5 — Perception as core mechanic.</b> Same entity,
 *       different understanding at different tiers.</li>
 *   <li><b>Phase 6 (delayed) — Wang Lin dimension.</b> Rumors →
 *       investigation → eventual encounter.</li>
 * </ol>
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li>{@link WorldPhilosophy} — the prime directive, encoded.</li>
 *   <li>{@link dev.ergenverse.world.WorldLayer} — the three layers of reality
 *       (Physical / Spiritual / Dao) that every chunk contains.</li>
 *   <li>{@link WorldLaws} — every location knows why it exists; laws
 *       determine what herbs grow, what beasts evolve, what techniques
 *       work.</li>
 *   <li>{@link dev.ergenverse.perception.Objective} — things that exist
 *       independently of any observer.</li>
 *   <li>{@link PerceptionEngine} — produces understanding of objective
 *       reality. Does not change the world.</li>
 *   <li>{@link dev.ergenverse.perception.DivineSense} — the transformative
 *       sense mechanic.</li>
 *   <li>{@link CausalEcology} — every spirit beast exists because of an
 *       ecosystem. Exterminate herbivores → predators starve → herbs
 *       spread → sects notice.</li>
 *   <li>{@link CanonEngine} — canon confidence ratings; Level 0 forbidden
 *       in known canon regions.</li>
 * </ul>
 */
@Mod(Ergenverse.MOD_ID)
public final class Ergenverse {
    public static final String MOD_ID = "ergenverse";
    public static final Logger LOGGER = LoggerFactory.getLogger("Ergenverse");

    public Ergenverse(ModContainer modContainer, IEventBus modEventBus) {
        LOGGER.info("[Ergenverse] Booting the Living Mortal World...");
        LOGGER.info("[Ergenverse] Prime Directive: {}", WorldPhilosophy.PRIME_DIRECTIVE);

        // ── Canon-first: every piece of content carries a canon confidence
        //    rating. Level 0 (filler) is forbidden in known canon regions.
        CanonEngine.bootstrap();
        CosmologicalTree.bootstrap();

        // ── Opportunity Classification (Layer 2 — Simulation): resolves how the
        //    player can obtain things related to canon opportunities, without ever
        //    rewriting canonical ownership. Six categories: Transferable, Replicable,
        //    Successor, Parallel, Relationship Exclusive, Absolute Unique.
        dev.ergenverse.simulation.opportunity.OpportunityResolver.bootstrap();

        // ── Wang Lin Cosmology (Layer 1 + Layer 2 bridge): concreted the entire
        //    Renegade Immortal canon into the game — 7 cosmology layers, Cave World
        //    ownership, Joss Flame economy, Heaven-Defying Bead, Samsara Dao (14
        //    Essences + 9 Heaven Trampling Bridges), antagonists, Realm-Sealing Grand
        //    Array, Bridging Policy, Timeline (108 events), Civilization (45 factions),
        //    Ecology (10 ecotopes), Canonical Database (630 entries), Edge-of-Canon
        //    State, and the Manifestation Gift System. Must run BEFORE the Wang Lin
        //    item registration and AI personality, which depend on the cosmology.
        WangLinCosmologyRegistry.bootstrap();

        // ── Wang Lin AI Personality (Layer 2 — Simulation): the canon-exact mind
        //    of Wang Lin as a manifestation companion. 14 traits, 11 habits, 7 speech
        //    patterns, ~23 defining memories, 17 relationships, and the 16-offering
        //    teaching decision engine (canTeach). Must run AFTER the cosmology
        //    registry (it depends on canon being loaded) and BEFORE the arsenal
        //    item registration (the items reference the personality for tooltips).
        WangLinPersonality.bootstrap();

        // ── Wang Lin Master Registry (Layer 1 — Canon Knowledge Graph): the
        //    complete canonical knowledge graph of Wang Lin's universe. NOT a
        //    whitelist — every top-level node (Cultivation, Restrictions,
        //    Formations, Essences, Dao, Avatars, Bodies, Pets, Companions,
        //    Realms, History, Knowledge, ...) expands into a tree of concrete
        //    canon entries, each carrying ownership state, transferability,
        //    demonstrability, interaction tags, and a Provenance. The
        //    TeachingResolver walks the graph: Technique → Canonical ownership
        //    → Transferability → Personality → Relationship → Simulation
        //    result. Must run AFTER the cosmology registry + personality
        //    (which the TeachingResolver delegates to) and BEFORE WangLinItems
        //    (so item tooltips can pull canon metadata from the registry).
        WangLinMasterRegistry.bootstrap();

        // ── World Laws: every location knows why it exists. The laws
        //    determine what herbs grow, what beasts evolve, what
        //    techniques work, what formations fail.
        WorldLaws.bootstrap();

        // ── Suzaku Features: canon-grounded spirit herbs + spirit vein ore
        //    are now data-driven via JSON files in
        //    data/ergenverse/worldgen/configured_feature/ + placed_feature/.
        //    Biome JSONs reference them directly. The SuzakuFeatureIndex class
        //    exposes a featuresForBiome() lookup for the future perception
        //    system (higher-tier cultivators see more feature types).

        // ── World Blueprint: AUTHORED geography of Planet Suzaku.
        //    The simulation is the source of truth; Minecraft is the renderer.
        //    Minecraft's random seed does NOT determine geography — this
        //    blueprint does. Countries, settlements, mountain ranges, rivers,
        //    spirit veins, and roads are all defined as data. The stage is
        //    fixed; the simulation (NPCs, ecology, events) varies per playthrough.
        //    Like Zelda/Elden Ring/Skyrim — Whiterun doesn't randomly spawn
        //    somewhere else. Neither does Wang Family Village.
        dev.ergenverse.world.blueprint.WorldBlueprintManager.load();

        // ── Perception Engine: produces understanding of objective
        //    reality. Does NOT change the world. Does NOT swap models.
        //    Does NOT spawn things because the player leveled up.
        PerceptionEngine.bootstrap();

        // ── Causal Ecology: every spirit beast exists because of an
        //    ecosystem. Exterminate herbivores → predators starve →
        //    herbs spread → sects notice.
        // (Registered later, when ecosystems are placed during world-gen.)

        // ── Cognition / LoS / Actor / Location-Layers / Ecology bootstrap.
        //    These systems were re-applied from the worklog (Task
        //    REBUILD-COGNITION) after the July 12 sandbox reset.
        //    Order matters: Location Layers must be seeded before
        //    Ecosystems (the latter binds to the former's FLORA/FAUNA
        //    layers). Territory seeding is independent.
        dev.ergenverse.simulation.LocationLayerBootstrap.bootstrap();
        dev.ergenverse.ecology.EcosystemSeeder.seed();
        dev.ergenverse.ecology.EcosystemManager.bindAll();
        dev.ergenverse.simulation.actor.TerritorySeeder.seed();

        // ── Wang Lin's Arsenal: register ALL 309 arsenal items (every item,
        //    technique, pet, clone, companion Wang Lin encountered/owned) as
        //    actual Minecraft items with always-upgradeable evolution chains.
        //    This is the completionist Wang Lin branch — nothing is locked.
        //    The Heaven-Defying Bead is registered as its own subclass
        //    (HeavenDefyingBeadItem) with NBT state, storage menu, and dimension.
        WangLinItems.register(modEventBus);
        ErgenverseBlocks.register(modEventBus);
        dev.ergenverse.item.ErgenverseItems.register(modEventBus);
        EREntityTypes.ENTITY_TYPES.register(modEventBus);

        // ── Workstation systems: Alchemy Furnace block entity + menu types.
        //    Block entities MUST be registered AFTER their owning blocks
        //    (the BlockEntityType.Builder references the Block). Menu types
        //    MUST be registered AFTER their owning block entities (the
        //    client factory references the BE class).
        dev.ergenverse.block.entity.ErgenverseBlockEntities.register(modEventBus);
        dev.ergenverse.screen.ErgenverseMenus.register(modEventBus);

        // ── Heaven-Defying Bead systems (Layer 2 — Simulation):
        //    The bead's storage menu (MenuType) and interior dimension.
        //    Must run AFTER WangLinItems (which registers the bead item)
        //    and AFTER all canon registries (which the bead references).
        BeadFunctionMenu.register(modEventBus);
        BeadDimension.bootstrap();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(CultivationCapability::register);
        // EntityAttributeCreationEvent fires on the MOD event bus, not the FORGE bus.
        modEventBus.addListener(this::onAttributeRegistry);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(WangLinArsenalCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.WorldStateCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.PerceptionCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.ManifestationGiftCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.HistoryCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.history.HistoryEvents.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.entity.ai.SectMissionInteraction.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.entity.ai.LectureInteraction.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.advanced.AdvancedMechanicsCommand.class);
        // ── Cognition / LoS / Actor / Location-Layers / Ecology commands.
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.LocationLayersCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.EcologyCommand.class);
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.command.ActorCommand.class);
        // ── Spawn system: village builder + tutorial book + diagnostic command.
        //    The SpawnEventHandler is @Mod.EventBusSubscriber (auto-registered),
        //    but the command needs explicit registration on the FORGE bus.
        MinecraftForge.EVENT_BUS.register(dev.ergenverse.spawn.ErgenverseCommand.class);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ErgenConfig.SPEC);
        LOGGER.info("[Ergenverse] The world has existed for ten thousand years before you arrived.");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LOGGER.info("[Ergenverse] Common setup complete.");
            LOGGER.info("[Ergenverse] Cultivation capability system active.");
            // Register network channel (must be in enqueueWork — thread-safe)
            dev.ergenverse.network.ERNetwork.register();
            LOGGER.info("[Ergenverse] Network channel initialized (protocol v{}).",
                    dev.ergenverse.network.ERNetwork.PROTOCOL_VERSION);
        });
    }

    /**
     * Register entity attributes (MAX_HEALTH, MOVEMENT_SPEED, etc.) for
     * mod entities. Required for any Mob-based entity to spawn — without
     * this, Minecraft throws "No attribute registry entry for ..." at spawn.
     *
     * <p>Fires on the MOD event bus during FMLCommonSetup, before world load.
     * Registered via {@code modEventBus.addListener(this::onAttributeRegistry)}
     * because EntityAttributeCreationEvent is a mod-lifecycle event, not a
     * runtime FORGE event.
     */
    public void onAttributeRegistry(net.minecraftforge.event.entity.EntityAttributeCreationEvent event) {
        event.put(dev.ergenverse.entity.EREntityTypes.CULTIVATOR.get(),
                dev.ergenverse.entity.EntityCultivator.createAttributes().build());
        LOGGER.info("[Ergenverse] Registered EntityCultivator attributes.");

        // ── Spirit Beast attributes (4 new entities). Without these, MC
        //    throws "No attribute registry entry for ..." at spawn.
        event.put(dev.ergenverse.entity.EREntityTypes.SPIRIT_RABBIT.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        event.put(dev.ergenverse.entity.EREntityTypes.SPIRIT_WOLF.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        event.put(dev.ergenverse.entity.EREntityTypes.SPIRIT_DEER.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        event.put(dev.ergenverse.entity.EREntityTypes.FIRE_BEAST.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        event.put(dev.ergenverse.entity.EREntityTypes.STONE_BACK_BOAR.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        event.put(dev.ergenverse.entity.EREntityTypes.SPIRIT_HAWK.get(),
                dev.ergenverse.entity.SpiritBeastEntity.createAttributes().build());
        LOGGER.info("[Ergenverse] Registered Spirit Beast attributes (rabbit/wolf/deer/hawk).");
    }

    /**
     * Server tick — runs the three decoupled loops:
     *
     * <ol>
     *   <li><b>CausalEcology</b> — every tick. The living ecology (spirit
     *       beasts, herb growth, predator/prey). Existing system, not
     *       touched.</li>
     *   <li><b>WorldStateEngine</b> — every 24000 ticks (1 MC day). The
     *       macro simulation: time events, migrations, karma resolution.
     *       Advances divergence counter on WorldRuntimeState.</li>
     *   <li><b>ReificationScan</b> — every 100 ticks (5 sec). The proximity
     *       scanner that materializes canon NPCs when a player approaches.
     *       Decoupled from chunk events to avoid cascading generation.</li>
     * </ol>
     *
     * <p>These are THREE SEPARATE LOOPS with different frequencies and
     * concerns. The prior design conflated ReificationScan with
     * WorldStateEngine under one 24000-tick interval, which would mean a
     * player could stand next to Wang Tiangui's spawn point for 20 minutes
     * before he appeared. Now they're independent.
     */
    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        net.minecraft.server.level.ServerLevel overworld = event.getServer().overworld();
        long ticks = overworld.getGameTime();

        // ── Bootstrap: register WorldEventBus subscribers + NpcSpawnRegistry on first tick ──
        // The bus is cleared on world unload, so subscribers must be
        // re-registered each session. We do it on tick 1 (before any
        // events can fire) and guard with a flag to avoid re-registering.
        if (ticks == 1) {
            dev.ergenverse.simulation.event.WorldEventBus.subscribe(
                    new dev.ergenverse.simulation.event.QiDisturbanceSubscriber());
            dev.ergenverse.simulation.event.WorldEventBus.subscribe(
                    new dev.ergenverse.simulation.event.BirdFlightSubscriber());
            dev.ergenverse.simulation.event.WorldEventBus.subscribe(
                    new dev.ergenverse.simulation.event.ActivityInterruptionSubscriber());
            LOGGER.info("[Ergenverse] Registered QiDisturbanceSubscriber + BirdFlightSubscriber + ActivityInterruptionSubscriber on WorldEventBus.");
            // Initialize the NPC spawn registry — maps locations to canon NPCs.
            // Without this, only wang_tiangui would ever spawn.
            dev.ergenverse.simulation.NpcSpawnRegistry.initialize();
        }

        // Loop A: CausalEcology — every tick (existing system, unchanged)
        CausalEcology.tickAll();

        // Loop B: WorldStateEngine — every 24000 ticks (1 MC day = 1 sim day)
        if (ticks % 24000 == 0) {
            dev.ergenverse.simulation.WorldStateEngine.tick();
        }

        // Loop C: ReificationScan — every 100 ticks (5 sec proximity scan)
        // Materializes canon NPCs when players approach their spawn anchors.
        dev.ergenverse.simulation.ReificationScan.executeTick(
                overworld, ticks,
                dev.ergenverse.simulation.SpatialBiomeCacheIndex.getInstance());

        // Loop D: WorldHistory — every 24000 ticks (daily checkpoint + future timeline advance)
        // Also ticks on every call; tickWorldHistory internally gates to daily.
        dev.ergenverse.history.WorldHistory.tickWorldHistory(overworld, ticks);

        // Loop E: Joss Flame Economy — every 24000 ticks (daily harvest loop)
        // The harvest loop runs OBJECTIVELY regardless of player perception.
        // Mortal villages generate flames, the owner siphons 30%, uncollected flames dissipate.
        dev.ergenverse.advanced.AdvancedMechanicsEvents.tickJossFlameEconomy(overworld, ticks / 24000);

        // Loop F: Cave World Ownership persistence — restore on first tick, persist on change
        // Ensures ownership transfers survive server restarts.
        if (ticks == 1) {
            dev.ergenverse.wanglin.CaveWorldOwnership.restoreFromWorldSave(overworld);
        }

        // Loop G: ActorTickLoop — EVERY tick.
        // The cognition pipeline (Perception → Beliefs → Goals → Reasoning → Decision → Intent).
        // Also syncs linked entity positions into Actors so the IntentEngine
        // has current positions for its situational queries.
        // Without this call, 5,800+ LOC of cognition is dead code (Art III, V, X).
        dev.ergenverse.simulation.actor.ActorTickLoop.tick(ticks, overworld);

        // Loop H: WorldEventBus — set current level for write-through.
        // The bus itself is event-driven (publish/subscribe); this just ensures
        // it has the ServerLevel reference needed for WorldHistory persistence.
        dev.ergenverse.simulation.event.WorldEventBus.setCurrentLevel(overworld);
    }
}
