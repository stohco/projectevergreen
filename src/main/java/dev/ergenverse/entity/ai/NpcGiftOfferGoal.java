package dev.ergenverse.entity.ai;

import com.google.gson.JsonObject;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.EnumSet;
import java.util.List;

/**
 * NpcGiftOfferGoal — Article XXIV (NPCs Must Initiate Gameplay) with
 * PROTAGONIST RECOGNITION and CONTEXTUAL AWARENESS.
 *
 * <h2>What this goal does (the "smart AI" surface)</h2>
 * <p>Wang Lin's manifestation is not a vending machine. He:
 * <ol>
 *   <li><b>Recognizes the player as a protagonist even when they are a mortal.</b>
 *       Canon: Wang Lin received the Heaven-Defying Bead when he was a mortal
 *       teenager, before he knew what cultivation was. Protagonists recognize
 *       each other. The first time Wang Lin encounters the player, he walks
 *       up and offers a jade slip — a one-time recognition gift.</li>
 *   <li><b>Observes context before acting.</b> He assesses what the player is
 *       doing right now: meditating, fighting, breaking through, wounded,
 *       burdened by karma, recently advanced. He adapts his behavior and
 *       dialogue accordingly.</li>
 *   <li><b>Does not disturb the player during critical moments.</b> If the
 *       player is in a tribulation or mid-breakthrough, Wang Lin stays back
 *       and watches. He does not path into a lightning storm.</li>
 *   <li><b>Speaks contextually.</b> When the player is meditating, he speaks
 *       softly. When the player just broke through, he acknowledges it. When
 *       the player is wounded, he expresses concern. When karma is heavy,
 *       he warns. This is what makes the AI feel smart — it reads the world.</li>
 *   <li><b>Optimized for single-player.</b> The player reference is cached.
 *       No per-tick player-list iteration. The server has 0 or 1 player;
 *       we resolve once and reuse.</li>
 * </ol>
 *
 * <h2>Player context states (what Wang Lin observes)</h2>
 * <pre>
 *   NOT_PRESENT          — no player in range → goal inactive
 *   IN_TRIBULATION       — player has tribulationPending → DO NOT DISTURB
 *   BREAKING_THROUGH     — breakthroughProgress ≥ 1.0, canBreakthrough → DO NOT DISTURB
 *   IN_COMBAT            — player took damage in last 10s → observe only, speak combat line
 *   LOW_HEALTH           — health < 50% max → speak concern, offer herb pill
 *   HIGH_KARMA           — karma > 0.7 → speak karmic warning
 *   RECENT_BREAKTHROUGH  — last history entry is breakthrough, < 5 min ago → congratulate
 *   MEDITATING           — MeditationHandler.isMeditating → observe, speak softly
 *   UNRECOGNIZED_MORTAL  — mortal + not yet recognized → RECOGNIZE (one-time)
 *   NORMAL               — everything fine → normal gift pipeline
 * </pre>
 *
 * <h2>Canon-faithful gate (for the normal gift pipeline)</h2>
 * <p>After recognition, Wang Lin uses the existing {@link ManifestationGiftSystem}
 * four-question gate (affinity + trust + realm + personality) to decide what
 * to offer. He does not hand out canonical treasures to Core Formation
 * cultivators — that would violate canon. But he WILL hand a jade slip to a
 * mortal protagonist, because that is exactly what happened to him.
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new Engine/Bus/Subscriber. Reuses:
 * <ul>
 *   <li>{@link ManifestationGiftSystem} — four-question gift evaluation</li>
 *   <li>{@link WorldStateDataLoader} — NPC JSON loader</li>
 *   <li>{@link CultivationCapability} — cultivation state reader</li>
 *   <li>{@link dev.ergenverse.cultivation.MeditationHandler#isMeditating} — meditation state</li>
 *   <li>{@link dev.ergenverse.history.PlayerHistory} — recent events</li>
 *   <li>{@link dev.ergenverse.history.HistoryManager#onGiftReceived} — history recorder</li>
 *   <li>Standard Minecraft {@code player.getCombatTracker()} / {@code player.getHealth()}</li>
 * </ul>
 *
 * <h2>Data shape (NPC JSON)</h2>
 * <pre>{
 *   "offers_gifts": true,
 *   "gift_protagonist_id": "wang_lin",
 *   "protagonist_recognition_lines": [
 *     "You... You are not yet on the path. But I see it in you.",
 *     "I felt your presence before I saw you. Take this — it found me when I was nothing."
 *   ],
 *   "contextual_lines": {
 *     "breakthrough_recent": ["I felt your breakthrough. The heavens noticed. So did I."],
 *     "meditating": ["Your meditation bears fruit. Continue."],
 *     "in_combat": ["You fight. I will watch."],
 *     "low_health": ["You are wounded. Heal first. The Dao is patient."],
 *     "high_karma": ["Your karma is heavy. Be wary of breakthroughs."]
 *   },
 *   "gift_offering_lines": ["You walk the path. Hold out your hand."]
 * }</pre>
 *
 * <p><b>Provenance:</b> INFERRED from Article XXIV + canon (Wang Lin received
 * the Heaven-Defying Bead as a mortal; protagonists recognize each other).
 */
public class NpcGiftOfferGoal extends Goal {

    // ═══════════════════════════════════════════════════════════════════
    //  Tunables
    // ═══════════════════════════════════════════════════════════════════

    /** How close a player must be for the NPC to consider engaging (blocks). */
    private static final double DETECT_RANGE = 16.0;

    /** How long the player must stay in range before the NPC commits (ticks). */
    private static final long SETTLE_TICKS = 400L; // 20 seconds (was 30 — smarter, faster)

    /** How close the NPC must get before delivering the gift (blocks). */
    private static final double GIFT_RANGE = 3.0;

    /** Per-player cooldown for NORMAL gift offers: one MC day. */
    private static final long COOLDOWN_TICKS = 24000L;

    /** Cooldown for contextual one-liners (don't spam the same observation). */
    private static final long CONTEXT_LINE_COOLDOWN = 1200L; // 60 seconds

    /** Health fraction below which Wang Lin expresses concern. */
    private static final double LOW_HEALTH_THRESHOLD = 0.5;

    /** Karma level above which Wang Lin warns. */
    private static final double HIGH_KARMA_THRESHOLD = 0.7;

    /** Recent-breakthrough window (ticks). */
    private static final long RECENT_BREAKTHROUGH_TICKS = 6000L; // 5 min

    /** Combat recency window (ticks). */
    private static final long COMBAT_RECENT_TICKS = 200L; // 10 sec

    /** Shared NBT key with ManifestationGiftHandler. */
    private static final String NBT_LAST_GIFT_TIME = "ergenverse_last_gift_time";

    /** NBT key for one-time protagonist recognition. */
    private static final String NBT_PROTAGONIST_RECOGNIZED = "ergenverse_protagonist_recognized";

    /** NBT key for last contextual line tick (per NPC). */
    private static final String NBT_LAST_CONTEXT_TICK = "ergenverse_last_context_tick";

    // ═══════════════════════════════════════════════════════════════════
    //  Player context (what Wang Lin observes)
    // ═══════════════════════════════════════════════════════════════════

    /** What Wang Lin observes about the player right now. */
    private enum PlayerContext {
        NOT_PRESENT,
        IN_TRIBULATION,       // DO NOT DISTURB
        BREAKING_THROUGH,     // DO NOT DISTURB
        IN_COMBAT,            // observe only — speak combat line once
        LOW_HEALTH,           // speak concern, offer healing
        HIGH_KARMA,           // speak karmic warning
        RECENT_BREAKTHROUGH,  // congratulate
        MEDITATING,           // observe quietly — speak soft line once
        UNRECOGNIZED_MORTAL,  // RECOGNIZE (one-time gift)
        NORMAL                // gift pipeline
    }

    // ═══════════════════════════════════════════════════════════════════
    //  State
    // ═══════════════════════════════════════════════════════════════════

    private final EntityCultivator cultivator;

    /** Protagonist ID for gift evaluation (defaults to "wang_lin"). */
    private String protagonistId = "wang_lin";

    /** Offering lines for the normal gift flow. */
    private final List<String> offeringLines = new java.util.ArrayList<>();

    /** Recognition lines for the one-time mortal encounter. */
    private final List<String> recognitionLines = new java.util.ArrayList<>();

    /** Contextual lines keyed by context name. */
    private final java.util.Map<String, List<String>> contextualLines = new java.util.HashMap<>();

    /** True once data has been loaded from NPC JSON. */
    private boolean dataLoaded = false;

    /** Cached single-player reference (single-player game). */
    private ServerPlayer cachedPlayer = null;

    /** Tick when the player cache was last refreshed. */
    private long playerCacheTick = 0L;

    /** The player the NPC is currently approaching (set in canUse). */
    private ServerPlayer targetPlayer = null;

    /** The gift that will be offered once the NPC reaches the player. */
    private ManifestationGiftSystem.GiftRecord pendingGift = null;

    /** The context that triggered this approach. */
    private PlayerContext activeContext = PlayerContext.NORMAL;

    /** Tick when the player first entered DETECT_RANGE (for settle check). */
    private long playerFirstSeenTick = 0L;

    /** Tick when the offer/line was delivered (for cooldown / state reset). */
    private long deliveredTick = 0L;

    /** True if this approach is a one-time recognition (vs. normal gift). */
    private boolean isRecognitionApproach = false;

    public NpcGiftOfferGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Goal lifecycle
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadDataIfNeeded()) return false;

        long now = cultivator.level().getGameTime();

        // ── Single-player optimization: get the one player (cached) ──
        ServerPlayer player = getSinglePlayer(now);
        if (player == null) return false;

        // Player must be within DETECT_RANGE
        if (player.distanceTo(cultivator) > DETECT_RANGE) {
            // Player out of range — reset settle timer
            if (targetPlayer != null && !targetPlayer.getUUID().equals(player.getUUID())) {
                targetPlayer = null;
                playerFirstSeenTick = 0L;
            } else if (targetPlayer == null) {
                playerFirstSeenTick = 0L;
            }
            return false;
        }

        // ── Assess what the player is doing right now ──
        PlayerContext context = assessContext(player, now);

        // ── SMART RULE: never disturb during tribulation or mid-breakthrough ──
        // Wang Lin would never walk into a lightning storm. He watches.
        if (context == PlayerContext.IN_TRIBULATION
                || context == PlayerContext.BREAKING_THROUGH) {
            // Reset approach state — we're observing, not approaching
            targetPlayer = null;
            playerFirstSeenTick = 0L;
            return false;
        }

        // ── Settle check: player must have been observed for SETTLE_TICKS ──
        // (Wang Lin watches first. He does not rush.)
        if (targetPlayer == null || !targetPlayer.getUUID().equals(player.getUUID())) {
            targetPlayer = player;
            playerFirstSeenTick = now;
            return false;
        }
        if (now - playerFirstSeenTick < SETTLE_TICKS) {
            return false; // still observing
        }

        // ── Decide what to do based on context ──
        activeContext = context;
        isRecognitionApproach = false;
        pendingGift = null;

        switch (context) {
            case UNRECOGNIZED_MORTAL:
                // One-time protagonist recognition — always fires
                isRecognitionApproach = true;
                return true;

            case IN_COMBAT:
                // Observe only — speak a combat line if cooldown allows, don't approach
                speakContextualLineIfReady(player, "in_combat", now);
                targetPlayer = null; // don't path toward a fighting player
                playerFirstSeenTick = 0L;
                return false;

            case MEDITATING:
                // Observe quietly — speak a soft line if cooldown allows, don't approach
                speakContextualLineIfReady(player, "meditating", now);
                targetPlayer = null;
                playerFirstSeenTick = 0L;
                return false;

            case LOW_HEALTH:
                // Approach and offer a healing pill (qi_gathering_pill)
                // Falls through to gift pipeline with a forced herb-pill fallback
                pendingGift = findHealingGift(player);
                if (pendingGift == null) {
                    // No healing gift available — just speak concern
                    speakContextualLineIfReady(player, "low_health", now);
                    targetPlayer = null;
                    playerFirstSeenTick = 0L;
                    return false;
                }
                return true;

            case HIGH_KARMA:
                // Speak warning, don't approach
                speakContextualLineIfReady(player, "high_karma", now);
                targetPlayer = null;
                playerFirstSeenTick = 0L;
                return false;

            case RECENT_BREAKTHROUGH:
                // Congratulate, then fall through to normal gift flow
                speakContextualLineIfReady(player, "breakthrough_recent", now);
                // Fall through to NORMAL gift pipeline
                activeContext = PlayerContext.NORMAL;
                // intentional fall-through

            case NORMAL:
            default:
                // Normal gift pipeline — check cooldown + four-question gate
                long lastGift = player.getPersistentData().getLong(NBT_LAST_GIFT_TIME);
                if (now - lastGift < COOLDOWN_TICKS) return false;
                pendingGift = findOfferedGift(player);
                if (pendingGift == null) return false;
                return true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (cultivator.level().isClientSide) return false;
        if (targetPlayer == null) return false;
        // For recognition, we always have a pending "gift" (the recognition slip)
        if (!isRecognitionApproach && pendingGift == null) return false;

        if (targetPlayer.hasDisconnected() || !targetPlayer.isAlive()) return false;

        double dist = targetPlayer.distanceTo(cultivator);
        if (dist > DETECT_RANGE * 1.5) return false;

        // Stop after delivery + short pause
        if (deliveredTick > 0
                && (cultivator.level().getGameTime() - deliveredTick) > 60L) {
            return false;
        }

        // SMART RULE: if the player enters tribulation/combat mid-approach, abort
        PlayerContext ctx = assessContext(targetPlayer, cultivator.level().getGameTime());
        if (ctx == PlayerContext.IN_TRIBULATION
                || ctx == PlayerContext.BREAKING_THROUGH) {
            // Player's situation changed — Wang Lin backs off
            Ergenverse.LOGGER.info("[NpcGiftOffer] {} aborting approach — player entered {}",
                    cultivator.getCharacterId(), ctx);
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (targetPlayer == null) return;
        walkTowardPlayer();
        Ergenverse.LOGGER.info("[NpcGiftOffer] {} approaching {} (context={}, recognition={})",
                cultivator.getCharacterId(), targetPlayer.getName().getString(),
                activeContext, isRecognitionApproach);
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        if (deliveredTick > 0) {
            cultivator.getNavigation().stop();
            cultivator.getLookControl().setLookAt(targetPlayer);
            return;
        }

        double dist = targetPlayer.distanceTo(cultivator);

        if (dist <= GIFT_RANGE) {
            if (isRecognitionApproach) {
                deliverRecognition();
            } else {
                deliverGift();
            }
        } else {
            if (cultivator.getNavigation().isDone() || cultivator.tickCount % 40 == 0) {
                walkTowardPlayer();
            }
            cultivator.getLookControl().setLookAt(targetPlayer);
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
        pendingGift = null;
        activeContext = PlayerContext.NORMAL;
        isRecognitionApproach = false;
        deliveredTick = 0L;
        playerFirstSeenTick = 0L;
        cultivator.getNavigation().stop();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Context assessment (the "smart AI" core)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Assess what the player is doing right now. This is what makes Wang Lin
     * feel smart — he reads the player's cultivation state, combat state,
     * health, karma, meditation, and recent history.
     */
    private PlayerContext assessContext(ServerPlayer player, long now) {
        // ── Cultivation state ──
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return PlayerContext.NOT_PRESENT;
        CultivationState state = stateOpt.resolve().get();
        RealmId realm = state.getCurrentRealm();

        // Tribulation pending — DO NOT DISTURB
        if (state.isTribulationPending()) {
            return PlayerContext.IN_TRIBULATION;
        }

        // Mid-breakthrough (progress full, can break through) — DO NOT DISTURB
        if (state.getBreakthroughProgress() >= 1.0 && state.canBreakthrough()) {
            return PlayerContext.BREAKING_THROUGH;
        }

        // ── Combat state (standard Minecraft API) ──
        // Check two signals: player was recently hurt (hurtTime > 0, lasts 10 ticks),
        // OR there are hostile mobs within 12 blocks (broader combat awareness).
        if (player.hurtTime > 0 || isPlayerNearHostiles(player)) {
            return PlayerContext.IN_COMBAT;
        }

        // ── Low health ──
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        if (maxHealth > 0 && (health / maxHealth) < LOW_HEALTH_THRESHOLD) {
            return PlayerContext.LOW_HEALTH;
        }

        // ── High karma ──
        if (state.getKarma() > HIGH_KARMA_THRESHOLD) {
            return PlayerContext.HIGH_KARMA;
        }

        // ── Recent breakthrough (check player history) ──
        try {
            var history = dev.ergenverse.history.PlayerHistory.get(player);
            if (history != null && !history.all().isEmpty()) {
                var latest = history.latest();
                if ("breakthrough".equals(latest.eventType)
                        && (now - latest.timestamp) < RECENT_BREAKTHROUGH_TICKS) {
                    return PlayerContext.RECENT_BREAKTHROUGH;
                }
            }
        } catch (Exception e) {
            // History read failure is non-fatal — fall through
        }

        // ── Meditating ──
        try {
            if (dev.ergenverse.cultivation.MeditationHandler.isMeditating(player.getUUID())) {
                return PlayerContext.MEDITATING;
            }
        } catch (Exception e) {
            // Non-fatal
        }

        // ── Unrecognized mortal → RECOGNIZE ──
        if (realm == RealmId.MORTAL) {
            boolean recognized = player.getPersistentData()
                    .getBoolean(NBT_PROTAGONIST_RECOGNIZED);
            if (!recognized) {
                return PlayerContext.UNRECOGNIZED_MORTAL;
            }
            // Already recognized mortal — Wang Lin observes quietly, doesn't approach
            return PlayerContext.MEDITATING; // treat as "observe, don't approach"
        }

        return PlayerContext.NORMAL;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Single-player optimization
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Get the single player on the server. Cached for 5 seconds (100 ticks)
     * to avoid per-tick player-list lookups.
     *
     * <p>Single-player game: the server has exactly 0 or 1 player. We resolve
     * once, cache, and reuse. This is the optimization the user requested.
     */
    private ServerPlayer getSinglePlayer(long now) {
        // Refresh cache every 100 ticks (5 seconds)
        if (cachedPlayer != null && (now - playerCacheTick) < 100L) {
            // Verify the cached player is still valid
            if (!cachedPlayer.hasDisconnected() && cachedPlayer.isAlive()
                    && cachedPlayer.level() == cultivator.level()) {
                return cachedPlayer;
            }
            cachedPlayer = null;
        }

        playerCacheTick = now;

        // Single-player: get the first (and only) player from the server
        var server = cultivator.level().getServer();
        if (server == null) {
            cachedPlayer = null;
            return null;
        }

        var players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            cachedPlayer = null;
            return null;
        }

        // Single-player game — take the first player.
        // (In a hypothetical multiplayer future, this would need to pick
        //  the nearest player in the same dimension. But per user directive,
        //  this is single-player only.)
        ServerPlayer player = players.get(0);

        // Must be in the same dimension as the NPC
        if (player.level() != cultivator.level()) {
            cachedPlayer = null;
            return null;
        }

        cachedPlayer = player;
        return player;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Recognition delivery (one-time mortal encounter)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Deliver the one-time protagonist recognition. Wang Lin walks up to a
     * mortal player, speaks a recognition line, and hands them a jade slip.
     *
     * <p>Canon: Wang Lin received the Heaven-Defying Bead as a mortal teen.
     * Protagonists recognize each other. This is that moment.
     */
    private void deliverRecognition() {
        if (targetPlayer == null) return;
        long now = cultivator.level().getGameTime();

        // Double-check: player must still be mortal and not yet recognized
        var stateOpt = CultivationCapability.get(targetPlayer);
        if (!stateOpt.isPresent()) {
            deliveredTick = now;
            return;
        }
        CultivationState state = stateOpt.resolve().get();
        if (state.getCurrentRealm() != RealmId.MORTAL) {
            // Player cultivated since we started approaching — treat as normal gift
            isRecognitionApproach = false;
            pendingGift = findOfferedGift(targetPlayer);
            if (pendingGift != null) {
                deliverGift();
            } else {
                deliveredTick = now;
            }
            return;
        }

        boolean alreadyRecognized = targetPlayer.getPersistentData()
                .getBoolean(NBT_PROTAGONIST_RECOGNIZED);
        if (alreadyRecognized) {
            deliveredTick = now;
            return;
        }

        // Speak the recognition line
        String line = recognitionLines.isEmpty()
                ? "I see it in you. The same destiny that found me."
                : recognitionLines.get(cultivator.getRandom().nextInt(recognitionLines.size()));
        targetPlayer.sendSystemMessage(Component.literal(
                "\u00A7d\u00A7l<" + cultivator.getDisplayNameCn() + ">\u00A7r \u00A7f" + line + "\u00A7r"));

        // Grant a jade slip — the canonical "first gift to a future cultivator"
        Item jadeSlip = dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get();
        ItemStack stack = new ItemStack(jadeSlip, 1);
        if (!targetPlayer.getInventory().add(stack)) {
            targetPlayer.drop(stack, false, false);
        }
        targetPlayer.sendSystemMessage(Component.literal(
                "\u00A7a\u2726 " + cultivator.getDisplayNameCn()
                        + " hands you a jade slip. It is warm to the touch.\u00A7r"));
        targetPlayer.sendSystemMessage(Component.literal(
                "\u00A7b\u00A7o\u2726 You feel that this is only the beginning.\u00A7r"));

        // Mark as recognized — one-time event, never repeats
        targetPlayer.getPersistentData().putBoolean(NBT_PROTAGONIST_RECOGNIZED, true);
        targetPlayer.getPersistentData().putLong(NBT_LAST_GIFT_TIME, now);

        // Record in emergent history
        dev.ergenverse.history.HistoryManager.onGiftReceived(
                targetPlayer, protagonistId, "Protagonist Recognition (Jade Slip)", now);

        Ergenverse.LOGGER.info("[NpcGiftOffer] {} RECOGNIZED mortal player {} as a protagonist — jade slip granted",
                cultivator.getCharacterId(), targetPlayer.getName().getString());

        deliveredTick = now;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Gift delivery (normal pipeline — reuses ManifestationGiftSystem)
    // ═══════════════════════════════════════════════════════════════════

    private void deliverGift() {
        if (targetPlayer == null || pendingGift == null) return;
        long now = cultivator.level().getGameTime();

        // Re-evaluate at delivery time — player state may have changed
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = buildSnapshot(targetPlayer);
        ManifestationGiftSystem.GiftDecision decision =
                ManifestationGiftSystem.evaluateGift(pendingGift, snapshot);

        if (decision != ManifestationGiftSystem.GiftDecision.OFFERED) {
            String refusalLine = ManifestationGiftSystem.getDialogueFor(pendingGift, decision);
            targetPlayer.sendSystemMessage(Component.literal(
                    "\u00A7e<" + cultivator.getDisplayNameCn() + "> \u00A77" + refusalLine + "\u00A7r"));
            Ergenverse.LOGGER.info("[NpcGiftOffer] {} decided NOT to offer '{}' at delivery (decision={})",
                    cultivator.getCharacterId(), pendingGift.giftId, decision);
            targetPlayer.getPersistentData().putLong(NBT_LAST_GIFT_TIME, now);
            deliveredTick = now;
            return;
        }

        // Speak offering line
        String line = offeringLines.isEmpty()
                ? "Hold out your hand."
                : offeringLines.get(cultivator.getRandom().nextInt(offeringLines.size()));
        targetPlayer.sendSystemMessage(Component.literal(
                "\u00A7d\u00A7l<" + cultivator.getDisplayNameCn() + ">\u00A7r \u00A7f" + line + "\u00A7r"));

        String offerDialogue = pendingGift.offerDialogue;
        if (offerDialogue != null && !offerDialogue.isEmpty()) {
            targetPlayer.sendSystemMessage(Component.literal("\u00A7d" + offerDialogue + "\u00A7r"));
        }

        // Grant item (three canon-faithful paths)
        boolean granted = false;
        if (pendingGift.canonOriginId != null) {
            var itemKey = new net.minecraft.resources.ResourceLocation(
                    "ergenverse", pendingGift.canonOriginId);
            Item item = ForgeRegistries.ITEMS.getValue(itemKey);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                ItemStack stack = new ItemStack(item, 1);
                if (!targetPlayer.getInventory().add(stack)) {
                    targetPlayer.drop(stack, false, false);
                }
                targetPlayer.sendSystemMessage(Component.literal(
                        "\u00A7a\u2726 Received: " + pendingGift.name + "\u00A77 (exact copy)"));
                granted = true;
            }
        }
        if (!granted) {
            Item fallbackItem = pickFallbackItem(pendingGift);
            if (fallbackItem != null) {
                ItemStack stack = new ItemStack(fallbackItem, 1);
                if (!targetPlayer.getInventory().add(stack)) {
                    targetPlayer.drop(stack, false, false);
                }
                String itemType = fallbackItem == dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get()
                        ? "a jade slip containing the teaching"
                        : "a token of " + pendingGift.name;
                targetPlayer.sendSystemMessage(Component.literal(
                        "\u00A7a\u2726 " + cultivator.getDisplayNameCn()
                                + " hands you " + itemType + ".\u00A7r"));
                if (fallbackItem == dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get()) {
                    targetPlayer.sendSystemMessage(Component.literal(
                            "\u00A7b\u2726 The jade slip is inscribed with: "
                                    + pendingGift.name + "\u00A7r"));
                }
            } else {
                targetPlayer.sendSystemMessage(Component.literal(
                        "\u00A7a\u2726 " + cultivator.getDisplayNameCn()
                                + " offers to teach you: " + pendingGift.name + "\u00A7r"));
            }
        }

        targetPlayer.getPersistentData().putLong(NBT_LAST_GIFT_TIME, now);
        targetPlayer.getPersistentData().putBoolean("ergenverse_gift_dirty", true);

        String itemName = pendingGift.name != null ? pendingGift.name : "unknown";
        dev.ergenverse.history.HistoryManager.onGiftReceived(
                targetPlayer, protagonistId, itemName, now);

        Ergenverse.LOGGER.info("[NpcGiftOffer] {} delivered gift '{}' to {} (NPC-initiated, Article XXIV)",
                cultivator.getCharacterId(), pendingGift.giftId,
                targetPlayer.getName().getString());

        deliveredTick = now;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Contextual lines (the "smart observation" surface)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Speak a contextual observation line if the per-NPC cooldown has expired.
     * This is what makes Wang Lin feel aware — he comments on what you're doing
     * without walking up to you.
     */
    private void speakContextualLineIfReady(ServerPlayer player, String contextKey, long now) {
        // Per-NPC cooldown key (so different NPCs can comment independently)
        String nbtKey = NBT_LAST_CONTEXT_TICK + "_" + cultivator.getCharacterId();
        long lastContext = player.getPersistentData().getLong(nbtKey);
        if (now - lastContext < CONTEXT_LINE_COOLDOWN) return;

        List<String> lines = contextualLines.get(contextKey);
        if (lines == null || lines.isEmpty()) return;

        String line = lines.get(cultivator.getRandom().nextInt(lines.size()));
        player.sendSystemMessage(Component.literal(
                "\u00A7e<" + cultivator.getDisplayNameCn() + "> \u00A77" + line + "\u00A7r"));

        player.getPersistentData().putLong(nbtKey, now);

        Ergenverse.LOGGER.info("[NpcGiftOffer] {} observed context '{}' for {} — spoke line",
                cultivator.getCharacterId(), contextKey, player.getName().getString());
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Find a healing-oriented gift for the LOW_HEALTH context.
     * Uses the existing gift system but filters for herb/pill category.
     */
    private ManifestationGiftSystem.GiftRecord findHealingGift(ServerPlayer player) {
        // For low health, we don't need the four-question gate — Wang Lin
        // would help a wounded cultivator regardless of trust. Use a direct
        // POST_CANON_HERBS lookup.
        for (var gift : ManifestationGiftSystem.getGiftsByProtagonist(protagonistId)) {
            if (gift.category == ManifestationGiftSystem.GiftCategory.POST_CANON_HERB) {
                return gift;
            }
        }
        return null;
    }

    private ManifestationGiftSystem.GiftRecord findOfferedGift(ServerPlayer player) {
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = buildSnapshot(player);
        List<ManifestationGiftSystem.GiftRecord> gifts =
                ManifestationGiftSystem.getGiftsByProtagonist(protagonistId);
        gifts = new java.util.ArrayList<>(gifts);
        gifts.sort((a, b) -> Integer.compare(a.affinityThreshold, b.affinityThreshold));

        for (var gift : gifts) {
            ManifestationGiftSystem.GiftDecision decision =
                    ManifestationGiftSystem.evaluateGift(gift, snapshot);
            if (decision == ManifestationGiftSystem.GiftDecision.OFFERED) {
                return gift;
            }
        }
        return null;
    }

    private ManifestationGiftSystem.PlayerStateSnapshot buildSnapshot(ServerPlayer player) {
        var stateOpt = CultivationCapability.get(player);
        final CultivationState state = stateOpt.isPresent()
                ? stateOpt.resolve().get() : null;
        final RealmId realm = state != null ? state.getCurrentRealm() : RealmId.MORTAL;
        return new ManifestationGiftSystem.PlayerStateSnapshot() {
            @Override public int getAffinity(String pId) {
                return "wang_lin".equals(pId) ? 60 : 20;
            }
            @Override public int getRealmTier() { return realm.order; }
            @Override public boolean hasDao(String daoId) {
                if (state == null) return false;
                var daoMap = state.getDaoComprehension();
                return daoMap.containsKey(daoId) && daoMap.get(daoId) > 0.1;
            }
            @Override public int getObservedActionTrust(String pId) { return getAffinity(pId); }
        };
    }

    private void walkTowardPlayer() {
        if (targetPlayer == null) return;
        cultivator.getNavigation().moveTo(
                targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), 0.9D);
    }

    /**
     * Check if there are hostile mobs within 12 blocks of the player.
     * This is Wang Lin's broader combat awareness — he won't approach a
     * player who is surrounded by monsters, even if they haven't been hit yet.
     */
    private boolean isPlayerNearHostiles(ServerPlayer player) {
        return !player.level().getEntitiesOfClass(
                net.minecraft.world.entity.monster.Monster.class,
                player.getBoundingBox().inflate(12.0)).isEmpty();
    }

    private Item pickFallbackItem(ManifestationGiftSystem.GiftRecord gift) {
        if (gift == null || gift.category == null) return null;
        switch (gift.category) {
            case CANONICAL_TECHNIQUE:
                return dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get();
            case POST_CANON_HERB:
                return dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get();
            case POST_CANON_CORE:
                return dev.ergenverse.item.ErgenverseItems.BEAST_CORE.get();
            case POST_CANON_FORGED:
                return dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get();
            case CANONICAL_TREASURE:
            default:
                return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Data loading
    // ═══════════════════════════════════════════════════════════════════

    private boolean loadDataIfNeeded() {
        if (dataLoaded) return !offeringLines.isEmpty() || !recognitionLines.isEmpty();
        dataLoaded = true;

        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return false;

        try {
            JsonObject data = WorldStateDataLoader.getEntry("npcs", characterId);
            if (data == null) return false;

            if (!data.has("offers_gifts") || !data.get("offers_gifts").isJsonPrimitive()
                    || !data.getAsJsonPrimitive("offers_gifts").getAsBoolean()) {
                return false;
            }

            if (data.has("gift_protagonist_id")
                    && data.get("gift_protagonist_id").isJsonPrimitive()) {
                protagonistId = data.getAsJsonPrimitive("gift_protagonist_id").getAsString();
            }

            // Load offering lines (normal gift flow)
            loadStringList(data, "gift_offering_lines", offeringLines);
            if (offeringLines.isEmpty()) {
                offeringLines.add("Hold out your hand.");
            }

            // Load recognition lines (one-time mortal encounter)
            loadStringList(data, "protagonist_recognition_lines", recognitionLines);
            if (recognitionLines.isEmpty()) {
                recognitionLines.add("I see it in you. The same destiny that found me.");
            }

            // Load contextual lines
            if (data.has("contextual_lines") && data.get("contextual_lines").isJsonObject()) {
                JsonObject ctx = data.getAsJsonObject("contextual_lines");
                for (String key : java.util.List.of(
                        "breakthrough_recent", "meditating", "in_combat",
                        "low_health", "high_karma")) {
                    List<String> list = new java.util.ArrayList<>();
                    loadStringList(ctx, key, list);
                    if (!list.isEmpty()) {
                        contextualLines.put(key, list);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[NpcGiftOffer] Failed to load data for {}: {}",
                    characterId, e.getMessage());
            return false;
        }
    }

    private void loadStringList(JsonObject obj, String key, List<String> target) {
        if (obj.has(key) && obj.get(key).isJsonArray()) {
            for (var elem : obj.getAsJsonArray(key)) {
                if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                    target.add(elem.getAsString());
                }
            }
        }
    }
}
