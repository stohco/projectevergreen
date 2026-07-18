package dev.ergenverse.wanglin.ai.reasoning;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * ExpectationModelObserver — server-side observer that updates Wang Lin's
 * Expectation Model of each player based on observed behavior, per §6.13.
 *
 * <p>Registered on the FORGE bus via {@link Mod.EventBusSubscriber}
 * (auto-registered — no edits to {@code Ergenverse.java}).
 *
 * <h2>Hooks</h2>
 * <ul>
 *   <li><b>{@link PlayerInteractEvent.RightClickBlock}</b> — if the player
 *       right-clicks a formation/restriction block, increase the
 *       "pursuing_restriction_dao" prediction confidence by 0.05 (capped at
 *       0.95 per {@link ExpectationModel#CONFIDENCE_CAP}).</li>
 *   <li><b>{@link LivingHurtEvent}</b> — if the player was hurt by an
 *       {@link EntityCultivator} (a cultivator NPC), increase the
 *       "will_challenge_stronger_cultivator" prediction confidence by 0.1.</li>
 *   <li><b>{@link PlayerEvent.PlayerRespawnEvent}</b> — if the player died
 *       (not End-conquered respawn), reduce all "can_survive_X" prediction
 *       confidences by 0.3. Wang Lin recalibrates: the player's survival
 *       ability was overestimated.</li>
 *   <li><b>{@link PlayerEvent.PlayerLoggedInEvent}</b> — load (or create) the
 *       player's ExpectationModel from persistent NBT.</li>
 *   <li><b>{@link TickEvent.PlayerTickEvent}</b> (periodic) — apply decay
 *       and detect hoarding behavior.</li>
 * </ul>
 *
 * <h2>Update throttling</h2>
 * <p>All updates are throttled to at most 1 per 100 ticks per player to
 * prevent spam (e.g. a player spamming right-click on a formation block
 * should not pin pursuing_restriction_dao to 95% in one second).
 *
 * <h2>Hoarding correction (§4.4, §6.13) — CRITICAL</h2>
 * <p>Hoarding behavior (player inventory &gt; 80% full of ingots/gems) DOES
 * update the "hoarding_path" prediction confidence by +0.02 per observation.
 * This prediction INFORMS what the player NEEDS MOST (a hoarding player may
 * need a breakthrough catalyst more than another treasure — see
 * {@link WangLinReasoningEngine}'s JUDGMENT factor).
 *
 * <p>The hoarding prediction MUST NEVER cause punitive gifting changes. It
 * is recorded here for the reasoning engine to read; it does not lower any
 * factor's score. Wang Lin responds to genuine need, not to moral judgment.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ExpectationModelObserver {

    private ExpectationModelObserver() {}

    /** Minimum ticks between any ExpectationModel update per player. */
    private static final long UPDATE_THROTTLE_TICKS = 100L;

    /** Periodic tick interval for decay + hoarding detection. */
    private static final int PERIODIC_INTERVAL_TICKS = 200; // 10s

    /** Hoarding threshold: if ≥ 80% of non-empty inventory slots are ingots/gems. */
    private static final double HOARDING_FRACTION_THRESHOLD = 0.80;

    /** Hoarding confidence increment per observation. */
    private static final double HOARDING_CONFIDENCE_INCREMENT = 0.02;

    /** Per-player last-update-tick, for throttling. */
    private static final Map<UUID, Long> LAST_UPDATE_TICK = new HashMap<>();

    /** Per-player tick counter for periodic tasks (decay + hoarding scan). */
    private static final Map<UUID, Integer> PERIODIC_TICK_COUNTER = new HashMap<>();

    // ─── Canonical prediction IDs ──────────────────────────────────────

    public static final String PRED_PURSUING_RESTRICTION_DAO = "pursuing_restriction_dao";
    public static final String PRED_WILL_CHALLENGE_STRONGER = "will_challenge_stronger_cultivator";
    public static final String PRED_CAN_SURVIVE_MOSQUITO_VALLEY = "can_survive_mosquito_valley";
    public static final String PRED_CAN_SURVIVE_RESTRICTION_MOUNTAIN = "can_survive_restriction_mountain";
    public static final String PRED_HOARDING_PATH = "hoarding_path";

    // ─── Player login: load or create model ────────────────────────────

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // Force-load the model from persistent NBT (creates an empty one if absent).
        ExpectationModel model = getOrCreate(player);
        Ergenverse.LOGGER.debug("[ExpectationModel] Loaded for {}: {} predictions",
                player.getName().getString(), model.size());
    }

    // ─── Right-click block: pursuing_restriction_dao ───────────────────

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        // Check if the clicked block is formation/restriction-related by name heuristic.
        var blockId = ForgeRegistries.BLOCKS.getKey(event.getLevel().getBlockState(event.getPos()).getBlock());
        if (blockId == null) return;
        String path = blockId.getPath();
        if (!isRestrictionOrFormationBlock(path)) return;

        long tick = player.level().getGameTime();
        if (!shouldThrottle(player, tick)) return;

        ExpectationModel model = getOrCreate(player);
        double current = model.confidenceOf(PRED_PURSUING_RESTRICTION_DAO);
        double updated = Math.min(ExpectationModel.CONFIDENCE_CAP, current + 0.05);
        model.updatePrediction(
                PRED_PURSUING_RESTRICTION_DAO,
                "Player is actively studying restrictions / formations.",
                updated, tick,
                "Right-clicked restriction/formation block: " + blockId);
        save(player, model);
    }

    // ─── Living hurt: will_challenge_stronger_cultivator ───────────────

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        Entity source = event.getSource().getEntity();
        if (!(source instanceof EntityCultivator cultivator)) return;

        long tick = player.level().getGameTime();
        if (!shouldThrottle(player, tick)) return;

        // Best-effort: if the cultivator's realm string is non-empty, we count this
        // as "challenged a stronger cultivator" (the player got hit by a cultivator NPC).
        // A future subagent can compare realms precisely.
        String cultivatorRealm = cultivator.getCultivationRealm();
        if (cultivatorRealm == null || cultivatorRealm.isBlank()) return;

        ExpectationModel model = getOrCreate(player);
        double current = model.confidenceOf(PRED_WILL_CHALLENGE_STRONGER);
        double updated = Math.min(ExpectationModel.CONFIDENCE_CAP, current + 0.10);
        model.updatePrediction(
                PRED_WILL_CHALLENGE_STRONGER,
                "Player engages cultivators of equal or higher realm.",
                updated, tick,
                "Hurt by EntityCultivator: " + cultivator.getCharacterId()
                        + " (realm: " + cultivatorRealm + ")");
        save(player, model);
    }

    // ─── Player respawn (death): reduce can_survive_X confidences ──────

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered()) return; // Not a death respawn — returning from End.
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        long tick = player.level().getGameTime();
        ExpectationModel model = getOrCreate(player);
        // Reduce every "can_survive_X" prediction by 0.3 — Wang Lin recalibrates
        // downward on observed player death.
        for (Prediction p : model.allPredictions()) {
            if (p.id.startsWith("can_survive_")) {
                double newConf = Math.max(0.0, p.confidence - 0.3);
                model.updatePrediction(p.id, p.description, newConf, tick,
                        "Player died — survival prediction recalibrated downward.");
            }
        }
        save(player, model);
        Ergenverse.LOGGER.debug("[ExpectationModel] Death respawn for {}: can_survive_X recalibrated",
                player.getName().getString());
    }

    // ─── Periodic tick: decay + hoarding detection ─────────────────────

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        UUID uuid = player.getUUID();
        int counter = PERIODIC_TICK_COUNTER.getOrDefault(uuid, 0) + 1;
        if (counter < PERIODIC_INTERVAL_TICKS) {
            PERIODIC_TICK_COUNTER.put(uuid, counter);
            return;
        }
        PERIODIC_TICK_COUNTER.put(uuid, 0);

        long tick = player.level().getGameTime();
        ExpectationModel model = getOrCreate(player);

        // Apply time-based decay.
        model.decayPredictions(tick);

        // Detect hoarding behavior.
        double hoardingFraction = computeHoardingFraction(player);
        if (hoardingFraction >= HOARDING_FRACTION_THRESHOLD) {
            double current = model.confidenceOf(PRED_HOARDING_PATH);
            double updated = Math.min(ExpectationModel.CONFIDENCE_CAP,
                    current + HOARDING_CONFIDENCE_INCREMENT);
            // HOARDING CORRECTION: this prediction INFORMS what the player needs most.
            // It NEVER triggers punitive withholding. See WangLinReasoningEngine.JUDGMENT.
            model.updatePrediction(
                    PRED_HOARDING_PATH,
                    "Player hoards resources (ingots/gems dominate inventory). "
                            + "Path: self-reliant, may face breakthrough bottlenecks.",
                    updated, tick,
                    String.format("Inventory %.0f%% ingots/gems — hoarding path inferred.",
                            hoardingFraction * 100));
        }

        save(player, model);
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    /**
     * Returns true if the throttling gate allows an update for this player
     * at this tick. If true, the caller should also update LAST_UPDATE_TICK.
     */
    private static boolean shouldThrottle(ServerPlayer player, long tick) {
        Long last = LAST_UPDATE_TICK.get(player.getUUID());
        if (last != null && (tick - last) < UPDATE_THROTTLE_TICKS) return false;
        LAST_UPDATE_TICK.put(player.getUUID(), tick);
        return true;
    }

    /** Heuristic: is this block-path a restriction/formation block? */
    private static boolean isRestrictionOrFormationBlock(String path) {
        if (path == null || path.isEmpty()) return false;
        String lower = path.toLowerCase();
        return lower.contains("restriction")
                || lower.contains("formation")
                || lower.contains("array")
                || lower.contains("seal")
                || lower.contains("altar"); // altars often have formation bindings
    }

    /**
     * Compute the fraction (0.0–1.0) of the player's non-empty inventory
     * slots that contain ingots or gems.
     */
    private static double computeHoardingFraction(ServerPlayer player) {
        int nonEmpty = 0;
        int ingotsOrGems = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.isEmpty()) continue;
            nonEmpty++;
            Item item = stack.getItem();
            ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
            if (id == null) continue;
            String path = id.getPath().toLowerCase();
            // Match vanilla + modded ingots/gems. We deliberately use a broad
            // heuristic (path contains "ingot" or "gem") so this stays
            // forward-compatible with future modded materials.
            if (path.contains("ingot") || path.contains("gem") || path.contains("crystal")) {
                // Exclude a few false positives (e.g. "ingot_mold" crafting tools).
                if (!path.contains("mold") && !path.contains("tool") && !path.contains("hammer")) {
                    ingotsOrGems++;
                }
            }
        }
        if (nonEmpty == 0) return 0.0;
        return (double) ingotsOrGems / (double) nonEmpty;
    }

    /**
     * Load (or create) the player's ExpectationModel from persistent NBT.
     * Server-side only.
     */
    public static ExpectationModel getOrCreate(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (root.contains(ExpectationModel.PERSISTENT_NBT_KEY)) {
            CompoundTag modelTag = root.getCompound(ExpectationModel.PERSISTENT_NBT_KEY);
            return ExpectationModel.deserializeNBT(modelTag);
        }
        return new ExpectationModel();
    }

    /** Persist the model back to the player's persistent NBT. Server-side only. */
    public static void save(ServerPlayer player, ExpectationModel model) {
        if (model == null) return;
        CompoundTag root = player.getPersistentData();
        root.put(ExpectationModel.PERSISTENT_NBT_KEY, model.serializeNBT());
    }

    /**
     * Convenience helper for the reasoning engine: read the player's current
     * cultivation realm. Returns {@link RealmId#MORTAL} if the cultivation
     * capability is not attached.
     */
    public static RealmId getPlayerRealm(ServerPlayer player) {
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return RealmId.MORTAL;
        CultivationState state = stateOpt.resolve().orElse(null);
        if (state == null) return RealmId.MORTAL;
        return state.getCurrentRealm();
    }

    /**
     * Convenience helper: read the player's dao comprehension map. Returns
     * an empty map if the cultivation capability is not attached.
     */
    public static java.util.Map<String, Double> getPlayerDaoMap(ServerPlayer player) {
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return java.util.Map.of();
        CultivationState state = stateOpt.resolve().orElse(null);
        if (state == null) return java.util.Map.of();
        return state.getDaoComprehension();
    }

    /** For testing/debug: clear the per-player in-memory throttle state. */
    public static void clearInMemoryCaches() {
        LAST_UPDATE_TICK.clear();
        PERIODIC_TICK_COUNTER.clear();
    }
}
