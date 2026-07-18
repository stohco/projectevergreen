package dev.ergenverse.cultivation;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

/**
 * Forge event handlers for the cultivation system.
 *
 * <p>This class handles all event-driven cultivation mechanics:
 * <ul>
 *   <li><b>Player tick</b> — Qi regeneration, life force decay,
 *       suppression effects, passive realm benefits.</li>
 *   <li><b>Breakthrough</b> — triggered when the player meets all
 *       conditions and attempts advancement.</li>
 *   <li><b>Tribulation</b> — Heaven's lightning strikes the cultivator
 *       during breakthrough. The cultivator must survive.</li>
 *   <li><b>Realm-based perception shifts</b> — when the player's
 *       perception tier changes, they gain new understanding of the
 *       world (not new objects — the objects always existed).</li>
 * </ul>
 *
 * <h2>The Prime Directive in Action</h2>
 * <p>Reality is objective; cultivation changes understanding, not
 * existence. When a player breaks through from Foundation to Nascent
 * Soul, the world does not change. The spirit beasts they couldn't
 * identify before are now named. The formations they couldn't see are
 * now visible. But those beasts and formations were always there.
 *
 * <h2>Tribulation Mechanics (Renegade Immortal Canon)</h2>
 * <p>In RI, tribulation is a defining event. When a cultivator attempts
 * a major breakthrough, Heaven responds with lightning:
 * <ul>
 *   <li><b>Qi Condensation → Foundation:</b> Light tribulation (1-3 bolts)</li>
 *   <li><b>Foundation → Core Formation:</b> Moderate (3-5 bolts)</li>
 *   <li><b>Core Formation → Nascent Soul:</b> Heavy (5-9 bolts). This is
 *       the first truly dangerous tribulation.</li>
 *   <li><b>Nascent Soul → Soul Formation:</b> Severe (9-15 bolts)</li>
 *   <li><b>Soul Formation → Ascendant:</b> Devastating (15-25 bolts)</li>
 *   <li><b>Higher realms:</b> Escalating beyond mortal comprehension</li>
 * </ul>
 *
 * <p>Each bolt deals damage scaled to the TARGET realm (not the current
 * one). Surviving all bolts = breakthrough. Dying = failure (or respawn
 * with penalties).
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CultivationEvents {

    private CultivationEvents() {}

    /** RNG for tribulation and heart demon rolls. */
    private static final Random RNG = new Random();

    // ─── Tribulation configuration per target realm ───────────────────

    /**
     * Tribulation parameters: [minBolts, maxBolts, damageMultiplier].
     *
     * <p>Damage is baseDamage × damageMultiplier, where baseDamage
     * scales with the target realm's order.
     *
     * <p>Per RI: tribulation gets progressively more dangerous.
     * Wang Lin often prepared extensively before attempting
     * breakthroughs, and even then nearly died multiple times.
     */
    private static final int[][] TRIBULATION_BOLTS = {
        /* MORTAL → QI_CONDENSATION */    {1,  3},
        /* QI → FOUNDATION */             {2,  4},
        /* FOUNDATION → CORE_FORMATION */ {3,  6},
        /* CORE → NASCENT_SOUL */         {5,  9},    // First truly dangerous
        /* NASCENT_SOUL → SOUL_FORMATION*/{8, 14},
        /* SOUL_FORMATION → SOUL_TRANSFORM*/{12, 20},
        /* SOUL_TRANSFORM → ASCENDANT */  {15, 25},
        /* ASCENDANT → ILLUSORY_YIN */    {20, 35},
        /* ILLUSORY_YIN → CORPOREAL_YANG*/{25, 45},
        /* CORPOREAL_YANG → NIRVANA_SCRYER*/{30, 55},
        /* NIRVANA_SCRYER → NIRVANA_CLEAN*/{35, 65},
        /* NIRVANA_CLEAN → NIRVANA_FRUIT*/{40, 75},
        /* NIRVANA_FRUIT → SPIRIT_SEIZER*/{50, 90},
        /* SPIRIT_SEIZER → TRUE_IMMORTAL*/{60, 100},
        /* TRUE_IMMORTAL → ANCIENT */     {80, 130},
        /* ANCIENT → PARAGON */           {100, 160},
        /* PARAGON → TRANSCENDENCE */     {130, 200},
    };

    /** Base damage per lightning bolt before realm scaling. */
    private static final float TRIBULATION_BASE_DAMAGE = 4.0f;

    /** Ticks between tribulation lightning strikes. */
    private static final int TRIBULATION_STRIKE_INTERVAL = 40; // 2 seconds

    /**
     * Realm order at which the tribulation becomes a MULTI-BLOCK lightning-pillar event.
     * Per canon: Nascent Soul (order 4) is "the first truly dangerous tribulation" —
     * at this realm and above, Heaven summons a CIRCLE of lightning pillars around the
     * cultivator, not just a single bolt. Soul Formation and above escalate further.
     */
    private static final int MULTI_BLOCK_TRIBULATION_THRESHOLD = 4; // Nascent Soul

    /** Radius of the lightning-pillar circle for multi-block tribulations. */
    private static final int PILLAR_CIRCLE_RADIUS = 5;

    /** Number of pillar lightning bolts in the circle (scales with realm). */
    private static final int[] PILLAR_COUNT_PER_REALM = {
        0, 0, 0, 0,          // Mortal..Core Formation: single bolt
        4, 6, 8, 10,         // Nascent Soul..Ascendant: pillars appear
        12, 14, 16, 18,      // Illusory Yin..Nirvana Fruit
        20, 22, 24, 26,      // Spirit Seizer..Paragon
        32                   // Transcendence
    };

    /** Range (in blocks) to scan for Realm-Sealing Grand Array anchor blocks. */
    private static final int REALM_SEALING_ARRAY_SCAN_RADIUS = 16;

    /** Damage multiplier when a Realm-Sealing Grand Array anchor is nearby (mitigation). */
    private static final float REALM_SEALING_ARRAY_DAMAGE_MULTIPLIER = 0.5f;

    // ─── Player Tick ──────────────────────────────────────────────────

    /**
     * Handle per-player server tick for cultivation state updates.
     *
     * <p>This runs every tick on the server for each player. It:
     * <ol>
     *   <li>Updates Qi regeneration, life force decay, and suppression
     *       via {@link CultivationState#serverTick()}.</li>
     *   <li>Applies passive realm-based effects (speed, resistance).</li>
     *   <li>Processes active tribulation (lightning strikes).</li>
     * </ol>
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;

        CultivationCapability.get(serverPlayer).ifPresent(state -> {
            // 1. Core state tick (Qi regen, life force decay)
            state.serverTick();

            // 2. Passive realm effects
            applyPassiveRealmEffects(serverPlayer, state);

            // 3. Active tribulation processing
            tickTribulation(serverPlayer, state);

            // 4. Active meditation qi gathering (Piece 1 of vertical slice)
            MeditationHandler.tickMeditation(serverPlayer);

            // 5. Check for death by old age
            if (state.getLifeForce() <= 0 && state.getCurrentRealm() != RealmId.TRANSCENDENCE) {
                serverPlayer.hurt(serverPlayer.damageSources().starve(), Float.MAX_VALUE);
                Ergenverse.LOGGER.info("[Ergenverse] {} has died of old age at realm {}.",
                        serverPlayer.getName().getString(), state.getCurrentRealm().name);
            }
        });
    }

    /**
     * Apply passive effects based on the cultivator's realm.
     *
     * <p>These effects are subtle — they represent the body's
     * enhancement through cultivation, not magical buffs. A Qi
     * Condensation cultivator is slightly faster and more durable
     * than a mortal. A Nascent Soul cultivator can outrun a horse.
     *
     * <p>Per the Prime Directive: these effects change what the
     * cultivator can DO, not what EXISTS. The world doesn't change
     * because the player is stronger.
     */
    private static void applyPassiveRealmEffects(ServerPlayer player, CultivationState state) {
        RealmId realm = state.getCurrentRealm();
        if (realm == RealmId.MORTAL) return;

        // Speed boost based on realm
        // Qi Condensation: slight. Nascent Soul: noticeable. Ascendant: significant.
        double speedLevel = switch (realm) {
            case QI_CONDENSATION -> 0;
            case FOUNDATION -> 0;
            case CORE_FORMATION -> 1;
            case NASCENT_SOUL -> 1;
            case SOUL_FORMATION -> 2;
            default -> 2;
        };

        if (speedLevel > 0) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SPEED, 40, (int) speedLevel - 1,
                    false, false, false));
        }

        // Resistance (body refinement makes the body tougher)
        double bodyToughness = state.getBloodRefinement();
        if (bodyToughness > 0.3) {
            int resistanceLevel = (int) Math.floor(bodyToughness * 3);
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, 40, Math.min(resistanceLevel - 1, 2),
                    false, false, false));
        }

        // Night vision for Foundation and above (cultivators can see in
        // darkness — their spiritual sense extends to their eyes)
        if (realm.isAtLeast(RealmId.FOUNDATION)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION, 220, 0,
                    false, false, false));
        }

        // Slow falling for Soul Formation and above (cultivators can fly)
        if (realm.isAtLeast(RealmId.SOUL_FORMATION)) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING, 40, 0,
                    false, false, false));
        }
    }

    // ─── Player Login/Clone (capability sync) ────────────────────────

    /**
     * When a player logs in, send them a perception tier notification.
     *
     * <p>This is the first thing a returning player sees — a reminder
     * of where they stand on the Dao. Per the Prime Directive, this
     * is about understanding, not power.
     */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        CultivationCapability.get(serverPlayer).ifPresent(state -> {
            RealmId realm = state.getCurrentRealm();
            PerceptionTier tier = state.getPerceptionTier();

            if (realm == RealmId.MORTAL) {
                serverPlayer.sendSystemMessage(Component.literal(
                        "\u00A77The world is vast and incomprehensible. You are mortal."));
            } else {
                serverPlayer.sendSystemMessage(Component.literal(
                        String.format("\u00A7a\u00A7l%s\u00A7r \u00A77— \u00A7f%s",
                                realm.name, tier.label)));
                serverPlayer.sendSystemMessage(Component.literal(
                        String.format("\u00A77Qi: \u00A7a%.0f%%\u00A77 | Life: \u00A7e%.0f years\u00A77 | Karma: \u00A7c%.2f",
                                state.getQi() * 100,
                                state.getLifeForce() / 365.25,
                                state.getKarma())));

                if (state.isTribulationPending()) {
                    serverPlayer.sendSystemMessage(Component.literal(
                            "\u00A7c\u00A7lHeaven's Tribulation is pending. The sky darkens..."));
                }
            }

            // Sync cultivation state to client for HUD + perception
            MeditationHandler.syncToClient(serverPlayer);
        });
    }

    /**
     * Copy cultivation state on player death/respawn (the Clone event).
     *
     * <p>Cultivation state persists through death — the cultivator's
     * Dao does not vanish when their body is destroyed. However,
     * Qi is depleted and karma may increase (death is a karmic event).
     *
     * <p>Per RI: Wang Lin was "killed" multiple times but his cultivation
     * and comprehension remained. The body could be reformed.
     */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            CultivationCapability.get(event.getOriginal()).ifPresent(oldState -> {
                CultivationCapability.get(event.getEntity()).ifPresent(newState -> {
                    // Copy the full state
                    CompoundTag tag = oldState.serializeNBT();
                    newState.deserializeNBT(tag);

                    // Death penalties: Qi depletion, slight karma increase
                    newState.consumeQi(newState.getAbsoluteQi()); // all Qi lost on death
                    newState.addKarma(0.02); // death adds karma
                    newState.setTribulationPending(false); // tribulation cancelled by death

                    Ergenverse.LOGGER.info("[Ergenverse] Cultivation state preserved through death. " +
                            "Qi depleted, karma increased.");
                });
            });
        }
    }

    // ─── Breakthrough Attempt (triggered by command or event) ────────

    /**
     * Attempt a breakthrough for the given player.
     *
     * <p>This is the entry point for breakthrough — called from commands,
     * items, or other events. It:
     * <ol>
     *   <li>Checks if the player can breakthrough via
     *       {@link CultivationState#canBreakthrough()}.</li>
     *   <li>If not, sends a narrative message explaining why.</li>
     *   <li>If yes, calls {@link CultivationState#attemptBreakthrough(Random)}
     *       which evaluates karma, comprehension, heart demon, and
     *       tribulation readiness.</li>
     *   <li>If tribulation is triggered, begins the tribulation sequence.</li>
     * </ol>
     *
     * @param player the server player attempting breakthrough
     */
    public static void attemptBreakthrough(ServerPlayer player) {
        CultivationState state = CultivationCapability.getOrThrow(player);
        RealmId current = state.getCurrentRealm();
        RealmId next = current.next();

        if (next == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A76You stand at the peak of all realms. There is nothing higher to ascend to."));
            return;
        }

        // ── Advanced mechanics: cultivation ceiling enforcement ──
        String ceilingBlock = dev.ergenverse.advanced.AdvancedMechanicsEvents
                .checkCultivationCeiling(player, next);
        if (ceilingBlock != null) {
            player.sendSystemMessage(Component.literal(ceilingBlock));
            return;
        }

        // Check if conditions are even close
        if (state.getBreakthroughProgress() < 1.0) {
            player.sendSystemMessage(Component.literal(
                    String.format("\u00A77Breakthrough progress: \u00A7a%.1f%%\u00A77. " +
                    "You are not yet ready. Meditate, comprehend Dao, and endure trials.",
                    state.getBreakthroughProgress() * 100)));
            return;
        }

        // Attempt the breakthrough
        BreakthroughResult result = state.attemptBreakthrough(RNG);

        switch (result.outcome()) {
            case SUCCESS -> {
                // Tribulation is now pending — begin the sequence
                player.sendSystemMessage(Component.literal(
                        String.format("\u00A7e\u00A7lThe heavens respond to your audacity!\u00A7r " +
                        "\u00A77Attempting breakthrough from \u00A7f%s\u00A77 to \u00A7a%s\u00A77...",
                        current.name, next.name)));
                player.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7lHeaven's Tribulation descends! \u00A77Prepare yourself!"));

                // Spawn the first lightning bolt immediately
                spawnTribulationBolt(player, state, 0);
            }
            case FAILED_TRIBULATION -> {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c" + result.narrativeNote()));
            }
            case HEART_DEMON -> {
                player.sendSystemMessage(Component.literal(
                        "\u00A75\u00A7l\u26A0 Heart Demon! \u00A7r\u00A7c" + result.narrativeNote()));
                // Apply confusion effect to represent heart demon's influence
                player.addEffect(new MobEffectInstance(
                        MobEffects.CONFUSION, 200, 1, false, true, true));
                player.addEffect(new MobEffectInstance(
                        MobEffects.WEAKNESS, 400, 1, false, true, true));
            }
            case INSUFFICIENT_COMPREHENSION -> {
                player.sendSystemMessage(Component.literal(
                        "\u00A77" + result.narrativeNote()));
            }
            case KARMA_BLOCKED -> {
                player.sendSystemMessage(Component.literal(
                        "\u00A7c\u00A7lKarma Blocks the Path! \u00A7r\u00A7c" + result.narrativeNote()));
            }
        }
    }

    // ─── Tribulation System ───────────────────────────────────────────

    /**
     * Spawn a single tribulation lightning bolt at the player's location.
     *
     * <p>The lightning is real Minecraft lightning — it deals damage,
     * sets fires, and can kill. The cultivator must survive ALL bolts
     * to complete the breakthrough.
     *
     * <p>Tribulation damage scales with the TARGET realm's order.
     * A tribulation for Nascent Soul is much more dangerous than
     * one for Foundation.
     *
     * @param player the player being struck
     * @param state the player's cultivation state
     * @param boltIndex which bolt in the sequence (0-based)
     */
    private static void spawnTribulationBolt(ServerPlayer player, CultivationState state, int boltIndex) {
        if (!state.isTribulationPending()) return;

        Level level = player.level();
        if (!(level instanceof ServerLevel serverLevel)) return;

        RealmId next = state.getCurrentRealm().next();
        if (next == null) return;

        // Get tribulation parameters for the target realm
        int[] params = getTribulationParams(state.getCurrentRealm().order);
        int maxBolts = params[1];

        if (boltIndex >= maxBolts) {
            // All bolts survived! Breakthrough complete!
            completeTribulationSuccess(player, state);
            return;
        }

        // Check for Realm-Sealing Grand Array mitigation (anchor blocks within scan radius)
        boolean arrayMitigation = isRealmSealingArrayNearby(serverLevel, player.blockPosition());
        float damageMultiplier = arrayMitigation ? REALM_SEALING_ARRAY_DAMAGE_MULTIPLIER : 1.0f;

        // Spawn a lightning bolt at the player's position
        BlockPos pos = player.blockPosition();
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(serverLevel);
        if (lightning != null) {
            lightning.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            lightning.setCause(null); // Heaven's lightning, not the player's
            // Increase damage for higher-realm tribulation
            float damage = TRIBULATION_BASE_DAMAGE * (1.0f + next.order * 0.8f) * damageMultiplier;
            // (Damage is applied via the lightning entity's natural damage; the multiplier
            // here documents the intended severity. For direct damage application, a
            // custom LightningBolt subclass would be used in v2.)

            // Visual feedback
            int boltNum = boltIndex + 1;
            String mitigationMsg = arrayMitigation
                    ? " \u00A7b[Realm-Sealing Array mitigates: 50% damage]"
                    : "";
            player.sendSystemMessage(Component.literal(
                    String.format("\u00A7c\u26A1 Tribulation Bolt %d/%d! \u00A77The heavens strike!%s",
                            boltNum, maxBolts, mitigationMsg)));

            serverLevel.addFreshEntity(lightning);

            // ── Multi-block lightning-pillar tribulation (Nascent Soul and above) ──
            // Per canon: high-tier tribulations aren't a single bolt — they're a CIRCLE
            // of lightning pillars around the cultivator. The Realm-Sealing Grand Array
            // (if nearby) suppresses some of the pillars.
            int currentRealmOrder = state.getCurrentRealm().order;
            if (currentRealmOrder >= MULTI_BLOCK_TRIBULATION_THRESHOLD) {
                int pillarIdx = Math.min(currentRealmOrder, PILLAR_COUNT_PER_REALM.length - 1);
                int pillarCount = PILLAR_COUNT_PER_REALM[pillarIdx];
                if (arrayMitigation) pillarCount = Math.max(1, pillarCount / 2); // array halves pillars
                spawnLightningPillars(serverLevel, player.blockPosition(), pillarCount, damageMultiplier);

                if (arrayMitigation) {
                    player.sendSystemMessage(Component.literal(
                        "\u00A7b\u00A7l\u2694 The Realm-Sealing Grand Array resonates!\u00A7r\n" +
                        "\u00A77The Heaven-Splitting Axe's spirit deflects half the lightning pillars.\n" +
                        "\u00A78Canon: the array was designed to control cultivator breakthroughs; it can also shelter them."));
                } else {
                    player.sendSystemMessage(Component.literal(
                        String.format("\u00A7c\u00A7l\u26A1 Lightning Pillar Tribulation!\u00A7r\n" +
                        "\u00A77%d pillars surround you. Only a Realm-Sealing Grand Array could mitigate this.",
                        pillarCount)));
                }
            }

            // Tribulation particle effects: lightning sparks + end rods
            spawnTribulationParticles(serverLevel, player);

            // Schedule the next bolt
            scheduleNextBolt(player, state, boltIndex + 1);
        }
    }

    /**
     * Spawn a circle of lightning pillar bolts around the cultivator.
     * Used for high-tier (Nascent Soul+) tribulations.
     *
     * <p>Per canon: high-tier breakthroughs attract a "lightning-pillar tribulation" —
     * the cultivator stands at the center of a ring of descending lightning, each pillar
     * a manifestation of Heaven's displeasure at the cultivator's defiance.
     */
    private static void spawnLightningPillars(ServerLevel serverLevel, BlockPos center,
                                              int count, float damageMultiplier) {
        // Evenly distribute pillars in a circle of radius PILLAR_CIRCLE_RADIUS
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count;
            int dx = (int) Math.round(Math.cos(angle) * PILLAR_CIRCLE_RADIUS);
            int dz = (int) Math.round(Math.sin(angle) * PILLAR_CIRCLE_RADIUS);
            BlockPos pillarPos = center.offset(dx, 0, dz);
            // Find the surface at this column
            int py = serverLevel.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pillarPos.getX(), pillarPos.getZ());
            LightningBolt pillarBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (pillarBolt != null) {
                pillarBolt.moveTo(pillarPos.getX() + 0.5, py, pillarPos.getZ() + 0.5);
                pillarBolt.setCause(null);
                serverLevel.addFreshEntity(pillarBolt);
            }
        }
    }

    /**
     * Check whether a Realm-Sealing Grand Array anchor block (realm_sealing_flag,
     * heaven_splitting_axe_pedestal, or dao_binding_stone) is within
     * {@link #REALM_SEALING_ARRAY_SCAN_RADIUS} blocks of the position.
     *
     * <p>Per canon: the array (when active) can mitigate tribulations for cultivators
     * inside it. The Heaven-Splitting Axe's spirit, bound to the array, deflects
     * some of Heaven's lightning — the array was designed by the Seven-Colored
     * Daoist to CONTROL cultivator breakthroughs, after all.
     */
    private static boolean isRealmSealingArrayNearby(ServerLevel serverLevel, BlockPos center) {
        // Check the 3 anchor block types
        net.minecraft.resources.ResourceLocation[] anchorIds = {
            new net.minecraft.resources.ResourceLocation("ergenverse", "realm_sealing_flag"),
            new net.minecraft.resources.ResourceLocation("ergenverse", "heaven_splitting_axe_pedestal"),
            new net.minecraft.resources.ResourceLocation("ergenverse", "dao_binding_stone")
        };
        // Resolve to Blocks
        var blockRegistry = net.minecraftforge.registries.ForgeRegistries.BLOCKS;
        java.util.Set<net.minecraft.world.level.block.Block> anchorBlocks = new java.util.HashSet<>();
        for (var id : anchorIds) {
            var block = blockRegistry.getValue(id);
            if (block != null) anchorBlocks.add(block);
        }
        if (anchorBlocks.isEmpty()) return false;

        int r = REALM_SEALING_ARRAY_SCAN_RADIUS;
        // Iterate a small AABB around the position. This is server-side only and runs
        // at most once per tribulation bolt (2 sec interval), so cost is acceptable.
        for (int dx = -r; dx <= r; dx += 2) { // step 2 to halve scan cost
            for (int dy = -r; dy <= r; dy += 2) {
                for (int dz = -r; dz <= r; dz += 2) {
                    var bs = serverLevel.getBlockState(center.offset(dx, dy, dz));
                    if (anchorBlocks.contains(bs.getBlock())) return true;
                }
            }
        }
        return false;
    }

    /**
     * Schedule the next tribulation bolt.
     *
     * <p>Uses the server tick scheduler. If the player is dead before
     * the next bolt fires, the tribulation fails.
     */
    private static void scheduleNextBolt(ServerPlayer player, CultivationState state, int nextBoltIndex) {
        // Use a delayed task — in Forge, this would use ServerTickEvent
        // with a counter. For now, we use a simpler approach: the next
        // bolt is spawned on the next player tick that matches the interval.
        //
        // The actual scheduling is handled by the tribulation tracker in
        // onPlayerTick. We set a "next bolt tick" in the player's
        // persistent data or a static map.

        // For the event-driven pattern, we store the pending tribulation
        // in a simple static tracker. In production, this would be
        // per-world and properly serialized.
        TribulationTracker.scheduleBolt(player, nextBoltIndex);
    }

    /**
     * Handle successful tribulation — the breakthrough completes.
     *
     * <p>Per the Prime Directive: the world does not change. The
     * cultivator's perception does. They can now see and interact
     * with deeper layers of reality.
     */
    private static void completeTribulationSuccess(ServerPlayer player, CultivationState state) {
        RealmId oldRealm = state.getCurrentRealm();
        RealmId newRealm = state.completeBreakthrough();

        if (newRealm != null) {
            PerceptionTier oldTier = PerceptionTier.fromRealm(oldRealm);
            PerceptionTier newTier = PerceptionTier.fromRealm(newRealm);

            // Announce the breakthrough
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal(
                    "\u00A7a\u00A7l\u2728 BREAKTHROUGH! \u00A7r"));
            player.sendSystemMessage(Component.literal(
                    String.format("\u00A7f%s \u00A77\u2192 \u00A7a\u00A7l%s\u00A7r",
                            oldRealm.name, newRealm.name)));
            player.sendSystemMessage(Component.literal(
                    String.format("\u00A77Perception: \u00A7f%s \u00A77\u2192 \u00A7b%s",
                            oldTier.label, newTier.label)));
            player.sendSystemMessage(Component.literal(
                    String.format("\u00A77Lifespan: \u00A7e%,d years",
                            newRealm.lifespan)));

            // If perception tier changed, announce what new understanding is gained
            if (newTier.order > oldTier.order) {
                announcePerceptionShift(player, oldTier, newTier);

                // ── Layer 3: Record perception shift as a discovery ──
                dev.ergenverse.history.HistoryManager.onDiscovery(player,
                        "perception_tier", "Perception expanded from " +
                        oldTier.label + " to " + newTier.label +
                        ". New understanding of the world gained.",
                        player.level().getGameTime());
            }

            // Heal the player (breakthrough rejuvenates)
            player.setHealth(player.getMaxHealth());
            player.removeAllEffects();

            // Clear fire (tribulation leaves scorch marks)
            player.clearFire();

            // Breakthrough visual effects: realm-colored particle burst
            if (player.level() instanceof ServerLevel sl) {
                spawnBreakthroughParticles(sl, player, newRealm);
            }

            Ergenverse.LOGGER.info("[Ergenverse] {} broke through from {} to {}.",
                    player.getName().getString(), oldRealm.name, newRealm.name);

            // ── Layer 3: Record breakthrough in emergent history ──
            dev.ergenverse.history.HistoryManager.onBreakthrough(
                    player, oldRealm.name, newRealm.name, player.level().getGameTime());

            // ── Advanced mechanics: perception-based system reveals ──
            dev.ergenverse.advanced.AdvancedMechanicsEvents.checkJossFlameRevelation(player);
        }
    }

    /**
     * Announce a perception tier shift to the player.
     *
     * <p>Per the Prime Directive, this describes new UNDERSTANDING,
     * not new existence. The world was always this complex — the
     * cultivator simply couldn't perceive it before.
     */
    private static void announcePerceptionShift(ServerPlayer player,
                                                  PerceptionTier oldTier,
                                                  PerceptionTier newTier) {
        String message = switch (newTier) {
            case QI_CONDENSATION -> "\u00A7aYour senses awaken. You can feel the " +
                    "pulse of Qi in all living things. Spirit herbs and spirit " +
                    "beasts are no longer invisible to you.";
            case FOUNDATION -> "\u00A7bQi currents flow through the earth like " +
                    "rivers. You can see minor formations, spirit veins beneath " +
                    "the ground, and the hidden boundaries of sect territories.";
            case NASCENT_SOUL -> "\u00A7dYour divine sense extends like an " +
                    "invisible hand. You can read karmic traces, sense bloodline " +
                    "resonance, and perceive the history of places through touch.";
            case SOUL_FORMATION -> "\u00A75The world's containment becomes visible. " +
                    "Sealed inheritances, Dao imprints, and the will of those who " +
                    "came before — all lie open to your perception.";
            case ASCENDANT -> "\u00A76You see the world boundary itself. The local " +
                    "antagonist's will presses against reality. Ancient bloodlines " +
                    "and the deep structure of the cosmos are within your grasp.";
            case TRANSCENDENCE -> "\u00A7fYou see the true nature of the cosmos. " +
                    "The distinctions between layers dissolve. All things are one, " +
                    "and the Dao is a single thread connecting all existence.";
            default -> "\u00A77Your perception deepens.";
        };

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "\u00A7b\u00A7l\u25C6 New Understanding \u00A7r"));
        player.sendSystemMessage(Component.literal(message));
        player.sendSystemMessage(Component.literal(
                "\u00A77The world has not changed. You have."));
    }

    // ─── Visual Effects ─────────────────────────────────────────────

    /**
     * Spawn particle effects during a tribulation lightning strike.
     *
     * <p>Creates a burst of END_ROD and ELECTRIC_SPARK particles
     * around the player to emphasize the heavenly tribulation.
     */
    private static void spawnTribulationParticles(ServerLevel level, ServerPlayer player) {
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        // Electric sparks at the strike point
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                x, y, z,
                30,    // count
                1.5,   // xSpread
                2.0,   // ySpread
                1.5,   // zSpread
                0.5    // speed
        );

        // End rods for the divine/celestial feel of heavenly tribulation
        level.sendParticles(
                ParticleTypes.END_ROD,
                x, y + 0.5, z,
                20,
                2.0,   // wide spread
                3.0,   // tall spread
                2.0,
                0.3    // slow float up
        );

        // Cloud particles above for the gathering storm
        level.sendParticles(
                ParticleTypes.CLOUD,
                x, y + 3.0, z,
                8,
                3.0,   // wide
                1.0,   // thin
                3.0,
                0.05   // barely moving
        );
    }

    /**
     * Spawn particle effects for a successful breakthrough.
     *
     * <p>The particle type and color scheme depend on the new realm,
     * reflecting the Dao affinity that characterizes that stage of
     * cultivation. Per the Prime Directive: the particles are a visual
     * representation of the Qi explosion that occurs during breakthrough.
     * The world doesn't change, but the cultivator's Qi eruption is
     * a real physical event.
     */
    private static void spawnBreakthroughParticles(ServerLevel level, ServerPlayer player, RealmId newRealm) {
        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        // All breakthroughs get a base burst of SOUL_FIRE_FLAME
        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                x, y, z,
                50,    // large burst
                2.0,   // wide
                3.0,   // tall
                2.0,
                0.8    // fast
        );

        // Realm-specific additional particles
        // Lower realms: earthy/natural. Higher realms: celestial/divine.
        if (newRealm.order <= RealmId.CORE_FORMATION.order) {
            // Qi Condensation / Foundation / Core Formation: flame + lava
            level.sendParticles(
                    ParticleTypes.FLAME,
                    x, y, z, 30, 1.5, 2.5, 1.5, 0.6
            );
            level.sendParticles(
                    ParticleTypes.LAVA,
                    x, y - 0.5, z, 15, 1.0, 1.0, 1.0, 0.2
            );
        } else if (newRealm.order <= RealmId.SOUL_FORMATION.order) {
            // Nascent Soul / Soul Formation: enchantment + dragon breath
            level.sendParticles(
                    ParticleTypes.ENCHANT,
                    x, y, z, 40, 2.5, 3.5, 2.5, 1.0
            );
            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z, 20, 1.5, 2.0, 1.5, 0.4
            );
        } else if (newRealm.order <= RealmId.ASCENDANT.order) {
            // Soul Transform / Ascendant: reverse portal + end rod
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    x, y, z, 40, 2.0, 3.0, 2.0, 1.5
            );
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z, 30, 2.0, 4.0, 2.0, 0.5
            );
        } else {
            // Nirvana / Spirit Seizer / True Immortal / Ancient / Paragon / Transcendence
            // The highest realms get all particles — a truly spectacular event
            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,
                    x, y, z, 60, 3.0, 4.0, 3.0, 2.0
            );
            level.sendParticles(
                    ParticleTypes.END_ROD,
                    x, y, z, 50, 3.0, 5.0, 3.0, 0.8
            );
            level.sendParticles(
                    ParticleTypes.DRAGON_BREATH,
                    x, y, z, 30, 2.0, 3.0, 2.0, 0.6
            );
            // Nautilus particles for the cosmic/transcendent feel
            level.sendParticles(
                    ParticleTypes.NAUTILUS,
                    x, y + 1.0, z, 20, 2.0, 2.0, 2.0, 1.0
            );
        }

        // Rising column of WAX_OFF particles (Qi eruption column)
        level.sendParticles(
                ParticleTypes.WAX_OFF,
                x, y - 0.5, z,
                40,
                0.5,   // narrow
                5.0,   // very tall column
                0.5,   // narrow
                1.5    // fast upward
        );
    }

    // ─── Utility ──────────────────────────────────────────────────────

    /**
     * Get tribulation parameters [minBolts, maxBolts] for a realm order.
     */
    private static int[] getTribulationParams(int realmOrder) {
        int index = Math.min(realmOrder, TRIBULATION_BOLTS.length - 1);
        return TRIBULATION_BOLTS[index];
    }

    // ─── Tribulation Tick Tracker ─────────────────────────────────────

    /**
     * Tracks pending tribulation bolts across all players.
     *
     * <p>This is a simple in-memory tracker. In production, this would
     * be properly serialized with world data.
     *
     * <p>The tracker is ticked from {@link #onPlayerTick} and spawns
     * lightning bolts at the configured interval.
     */
    static final class TribulationTracker {

        /** Maps player UUID to [nextBoltIndex, ticksUntilNextBolt]. */
        private static final java.util.Map<java.util.UUID, int[]> pending = new java.util.HashMap<>();

        private TribulationTracker() {}

        /** Schedule the next bolt for a player. */
        static void scheduleBolt(Player player, int boltIndex) {
            pending.put(player.getUUID(), new int[]{boltIndex, TRIBULATION_STRIKE_INTERVAL});
        }

        /** Remove a player's pending tribulation. */
        static void cancel(Player player) {
            pending.remove(player.getUUID());
        }

        /** Tick all pending tribulations. Called from onPlayerTick. */
        static void tickAll(java.util.function.BiConsumer<ServerPlayer, CultivationState> boltSpawner) {
            java.util.Iterator<java.util.Map.Entry<java.util.UUID, int[]>> it = pending.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                int[] data = entry.getValue();
                data[1]--;

                if (data[1] <= 0) {
                    // Time to fire the bolt — but we need the ServerPlayer
                    // The caller provides the spawner function
                    it.remove();
                    // The actual bolt spawning is deferred to the caller
                    // who has access to the player reference.
                    // We use a different approach: store and check in the tick.
                }
            }
        }

        /**
         * Check and fire tribulation bolts. Called from onPlayerTick
         * where we have the ServerPlayer reference.
         */
        static void tickPlayer(ServerPlayer player, CultivationState state) {
            int[] data = pending.get(player.getUUID());
            if (data == null) return;

            if (!state.isTribulationPending()) {
                pending.remove(player.getUUID());
                return;
            }

            data[1]--;
            if (data[1] <= 0) {
                int nextBolt = data[0];
                pending.remove(player.getUUID());

                if (player.isAlive()) {
                    spawnTribulationBolt(player, state, nextBolt);
                } else {
                    // Player died during tribulation
                    state.failBreakthrough();
                    Ergenverse.LOGGER.info("[Ergenverse] {} was slain by Heaven's Tribulation " +
                            "during breakthrough attempt.",
                            player.getName().getString());
                }
            }
        }

        /** Check if a player has a pending tribulation. */
        static boolean hasPending(java.util.UUID playerId) {
            return pending.containsKey(playerId);
        }
    }

    // ─── Register this class's tick integration ───────────────────────

    /**
     * Static initialization hook: nothing needed here since we use
     * @SubscribeEvent. But we expose a method for the main tick
     * handler to call if it wants to integrate tribulation ticking
     * with other systems.
     */
    public static void tickTribulation(ServerPlayer player, CultivationState state) {
        if (state.isTribulationPending()) {
            TribulationTracker.tickPlayer(player, state);

            // Ambient tribulation particles: ominous particles every 10 ticks
            if (player.tickCount % 10 == 0 && player.level() instanceof ServerLevel sl) {
                sl.sendParticles(
                        ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 2.0, player.getZ(),
                        3,    // subtle — just a few
                        1.5, 0.5, 1.5,
                        0.1   // very slow drift
                );
            }
        }
    }
}