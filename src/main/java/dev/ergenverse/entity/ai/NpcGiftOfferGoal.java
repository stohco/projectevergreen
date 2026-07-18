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
 * NpcGiftOfferGoal — Article XXIV (NPCs Must Initiate Gameplay) taken to its
 * next step: the NPC not only SPEAKS first, it WALKS to the player and
 * offers a canon gift or a teaching.
 *
 * <h2>Why this exists</h2>
 * <p>Before this goal, all gift-giving was Player → NPC: the player had to
 * right-click the Wang Lin manifestation (ManifestationGiftHandler). That is
 * the old model Article XXIV explicitly rejects. Canon-faithful behavior:
 * <ul>
 *   <li>Wang Lin leaves a jade slip where he predicts the player will find it.</li>
 *   <li>Situ Nan asks the player to investigate a fluctuation.</li>
 *   <li>An elder invites the player to observe a lecture.</li>
 *   <li>Wang Lin, recognizing a kindred spirit, walks over and offers to
 *       share a technique.</li>
 * </ul>
 * The NPC must initiate the offering. The player accepts by standing still
 * and receiving it.
 *
 * <h2>Canon-faithful gate (very important)</h2>
 * <p>Wang Lin is canonically CAUTIOUS and RESERVED. He does not hand out
 * treasures to strangers. So this goal only fires when ALL of these hold:
 * <ul>
 *   <li>The NPC's JSON data has {@code offers_gifts: true}.</li>
 *   <li>The player has reached at least Qi Condensation — Wang Lin does not
 *       reveal himself to mortals. (Canon: cultivators hide from mortals.)</li>
 *   <li>The player has been within {@link #DETECT_RANGE} blocks for at least
 *       {@link #SETTLE_TICKS} ticks — no rushing; Wang Lin watches first.</li>
 *   <li>The per-player cooldown has expired (one offer per MC day).</li>
 *   <li>The existing {@link ManifestationGiftSystem#evaluateGift} four-question
 *       gate returns {@link ManifestationGiftSystem.GiftDecision#OFFERED} for
 *       at least one gift the protagonist can offer at this trust/realm tier.</li>
 * </ul>
 * If any gate fails, the goal never activates — Wang Lin stays silent and
 * watches. This is Article XXIV (initiation) without violating Article I
 * (canon is reality — Wang Lin would not give treasures to anyone who hasn't
 * earned the right).
 *
 * <h2>Behavior sequence</h2>
 * <ol>
 *   <li>canUse: player within DETECT_RANGE, settled for SETTLE_TICKS,
 *       cooldown expired, cultivation ≥ Qi Condensation, NPC has offers_gifts=true,
 *       and at least one gift from ManifestationGiftSystem evaluates OFFERED.</li>
 *   <li>start: pick the offered gift, walk toward the player (pathfind).</li>
 *   <li>tick: keep walking until within GIFT_RANGE (3 blocks).</li>
 *   <li>On arrival: face the player, speak the offering line, evaluate the
 *       gift via the existing ManifestationGiftSystem, and grant the item
 *       using the existing inventory-grant pattern from ManifestationGiftHandler.</li>
 *   <li>Record cooldown in player NBT (shares the same key as the right-click
 *       handler so the two paths do not double-grant).</li>
 * </ol>
 *
 * <h2>Article XXVI compliance</h2>
 * <p>NO new Engine/Bus/Subscriber. This is a single Minecraft Goal class.
 * It reuses:
 * <ul>
 *   <li>{@link ManifestationGiftSystem} — the existing four-question evaluation engine.</li>
 *   <li>{@link WorldStateDataLoader} — the existing NPC JSON loader (for
 *       {@code offers_gifts} flag and {@code gift_offering_lines}).</li>
 *   <li>{@link CultivationCapability} — the existing cultivation state reader.</li>
 *   <li>The existing inventory-grant pattern (same as ManifestationGiftHandler).</li>
 *   <li>{@link dev.ergenverse.history.HistoryManager#onGiftReceived} — the
 *       existing emergent history recorder.</li>
 * </ul>
 *
 * <h2>Data shape (NPC JSON)</h2>
 * <pre>{
 *   "offers_gifts": true,
 *   "gift_protagonist_id": "wang_lin",
 *   "gift_offering_lines": [
 *     "You walk the path. Hold out your hand.",
 *     "I have watched you cultivate. Take this — and do not waste it.",
 *     "This was mine. Now it is yours. The Dao is not hoarded."
 *   ]
 * }</pre>
 * If {@code offers_gifts} is missing or false, this goal never activates.
 *
 * <p><b>Provenance:</b> INFERRED from Article XXIV — "Wang Lin leaves a jade
 * slip", "Situ Nan asks for help", "An elder invites you to observe a lecture."
 */
public class NpcGiftOfferGoal extends Goal {

    /** How close a player must be for the NPC to consider offering (blocks). */
    private static final double DETECT_RANGE = 12.0;

    /** How long the player must stay in range before the NPC commits (ticks). */
    private static final long SETTLE_TICKS = 600L; // 30 seconds

    /** How close the NPC must get before delivering the gift (blocks). */
    private static final double GIFT_RANGE = 3.0;

    /** Per-player cooldown: one offer per MC day (24000 ticks = 20 min real). */
    private static final long COOLDOWN_TICKS = 24000L;

    /** Shared NBT key with ManifestationGiftHandler so the two paths do not double-grant. */
    private static final String NBT_LAST_GIFT_TIME = "ergenverse_last_gift_time";

    private final EntityCultivator cultivator;

    /** Protagonist ID for gift evaluation (defaults to "wang_lin"). */
    private String protagonistId = "wang_lin";

    /** Offering lines loaded from NPC JSON (empty = use a default line). */
    private final List<String> offeringLines = new java.util.ArrayList<>();

    /** True once data has been loaded from NPC JSON. */
    private boolean dataLoaded = false;

    /** The player the NPC is currently approaching. */
    private ServerPlayer targetPlayer = null;

    /** The gift that will be offered once the NPC reaches the player. */
    private ManifestationGiftSystem.GiftRecord pendingGift = null;

    /** Tick when the player first entered DETECT_RANGE (for settle check). */
    private long playerFirstSeenTick = 0L;

    /** Tick when the offer was delivered (for cooldown / state reset). */
    private long offerDeliveredTick = 0L;

    public NpcGiftOfferGoal(EntityCultivator cultivator) {
        this.cultivator = cultivator;
        // Controls MOVE + LOOK so the NPC can path to the player and face them.
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Goal lifecycle
    // ═══════════════════════════════════════════════════════════════════

    @Override
    public boolean canUse() {
        if (cultivator.level().isClientSide) return false;
        if (!loadDataIfNeeded()) return false;
        if (offeringLines.isEmpty()) return false; // no offers_gifts flag

        // Find a player who meets ALL canon-faithful gates
        ServerPlayer candidate = findEligiblePlayer();
        if (candidate == null) return false;

        // Settle check: player must have been in range for SETTLE_TICKS
        long now = cultivator.level().getGameTime();
        if (targetPlayer == null || !targetPlayer.getUUID().equals(candidate.getUUID())) {
            // New player — start the settle timer
            targetPlayer = candidate;
            playerFirstSeenTick = now;
            return false;
        }
        if (now - playerFirstSeenTick < SETTLE_TICKS) {
            return false; // still settling — keep watching
        }

        // Find a gift the existing ManifestationGiftSystem says can be offered
        pendingGift = findOfferedGift(candidate);
        if (pendingGift == null) {
            // No gift passes the four-question gate — Wang Lin stays silent.
            // Reset so we re-evaluate next cycle.
            targetPlayer = null;
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (cultivator.level().isClientSide) return false;
        if (targetPlayer == null || pendingGift == null) return false;

        // Player logged out, died, or moved too far away — abort
        if (targetPlayer.hasDisconnected() || !targetPlayer.isAlive()) return false;
        double dist = targetPlayer.distanceTo(cultivator);
        if (dist > DETECT_RANGE * 1.5) {
            // Player ran off — give up
            return false;
        }
        // Stop once the offer has been delivered + a short post-deliver pause
        if (offerDeliveredTick > 0
                && (cultivator.level().getGameTime() - offerDeliveredTick) > 60L) {
            return false;
        }
        return true;
    }

    @Override
    public void start() {
        if (targetPlayer == null) return;
        // Begin walking toward the player
        walkTowardPlayer();
        Ergenverse.LOGGER.info("[NpcGiftOffer] {} is approaching {} to offer '{}'",
                cultivator.getCharacterId(), targetPlayer.getName().getString(),
                pendingGift != null ? pendingGift.giftId : "?");
    }

    @Override
    public void tick() {
        if (targetPlayer == null) return;

        // Already delivered — just hold position briefly
        if (offerDeliveredTick > 0) {
            cultivator.getNavigation().stop();
            cultivator.getLookControl().setLookAt(targetPlayer);
            return;
        }

        double dist = targetPlayer.distanceTo(cultivator);

        if (dist <= GIFT_RANGE) {
            // Arrived — deliver the gift
            deliverGift();
        } else {
            // Keep walking; re-path every ~2 seconds to track a moving player
            if (cultivator.getNavigation().isDone()
                    || cultivator.tickCount % 40 == 0) {
                walkTowardPlayer();
            }
            // Face the player while walking
            cultivator.getLookControl().setLookAt(targetPlayer);
        }
    }

    @Override
    public void stop() {
        targetPlayer = null;
        pendingGift = null;
        offerDeliveredTick = 0L;
        playerFirstSeenTick = 0L;
        cultivator.getNavigation().stop();
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Gift delivery (reuses existing ManifestationGiftSystem)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Deliver the pending gift: speak the offering line, evaluate the gift
     * via the existing four-question engine (re-check at delivery time, in
     * case the player's state changed during approach), and grant the item
     * using the existing inventory-grant pattern.
     */
    private void deliverGift() {
        if (targetPlayer == null || pendingGift == null) return;
        long now = cultivator.level().getGameTime();

        // Re-evaluate at delivery time — the player's realm/trust may have changed
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = buildSnapshot(targetPlayer);
        ManifestationGiftSystem.GiftDecision decision =
                ManifestationGiftSystem.evaluateGift(pendingGift, snapshot);

        if (decision != ManifestationGiftSystem.GiftDecision.OFFERED) {
            // The four-question gate now refuses (player changed state).
            // Wang Lin simply turns away — canon-faithful silence.
            String refusalLine = ManifestationGiftSystem.getDialogueFor(pendingGift, decision);
            targetPlayer.sendSystemMessage(Component.literal(
                    "\u00A7e<" + cultivator.getDisplayNameCn() + "> \u00A77" + refusalLine + "\u00A7r"));
            Ergenverse.LOGGER.info("[NpcGiftOffer] {} decided NOT to offer '{}' to {} at delivery (decision={})",
                    cultivator.getCharacterId(), pendingGift.giftId,
                    targetPlayer.getName().getString(), decision);
            // Still record cooldown so we don't pester them again immediately
            targetPlayer.getPersistentData().putLong(NBT_LAST_GIFT_TIME, now);
            offerDeliveredTick = now;
            return;
        }

        // Speak the offering line
        String line = offeringLines.get(
                cultivator.getRandom().nextInt(offeringLines.size()));
        targetPlayer.sendSystemMessage(Component.literal(
                "\u00A7d\u00A7l<" + cultivator.getDisplayNameCn() + ">\u00A7r \u00A7f" + line + "\u00A7r"));

        // Offer dialogue from the gift record
        String offerDialogue = pendingGift.offerDialogue;
        if (offerDialogue != null && !offerDialogue.isEmpty()) {
            targetPlayer.sendSystemMessage(Component.literal(
                    "\u00A7d" + offerDialogue + "\u00A7r"));
        }

        // Grant the item. Three canon-faithful paths:
        //  (a) The gift's canonOriginId maps to a registered Forge item → grant it.
        //  (b) Technique gift (CANONICAL_TECHNIQUE) with no registered item →
        //      Wang Lin gives the player a JADE SLIP containing the technique.
        //      Canon: techniques are stored on jade slips in the Er Gen universe —
        //      Wang Lin finds, creates, and distributes jade slips throughout RI.
        //      This is the user's "Wang Lin would be the one in possession of
        //      the slip" correction made playable.
        //  (c) Post-canon herb/core gift with no registered item → Wang Lin
        //      gives the player a substitute physical token (qi_gathering_pill
        //      for herbs, beast_core for cores) — these are real registered items.
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
            // Fallback: pick a canon-faithful physical token by gift category
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
                // Truly no fallback — pure teaching with no physical token
                targetPlayer.sendSystemMessage(Component.literal(
                        "\u00A7a\u2726 " + cultivator.getDisplayNameCn()
                                + " offers to teach you: " + pendingGift.name + "\u00A7r"));
            }
        }

        // Record cooldown (shared key with right-click handler)
        targetPlayer.getPersistentData().putLong(NBT_LAST_GIFT_TIME, now);
        targetPlayer.getPersistentData().putBoolean("ergenverse_gift_dirty", true);

        // Record in emergent history (reuses existing HistoryManager)
        String itemName = pendingGift.name != null ? pendingGift.name : "unknown";
        dev.ergenverse.history.HistoryManager.onGiftReceived(
                targetPlayer, protagonistId, itemName, now);

        Ergenverse.LOGGER.info("[NpcGiftOffer] {} delivered gift '{}' to {} (NPC-initiated, Article XXIV)",
                cultivator.getCharacterId(), pendingGift.giftId,
                targetPlayer.getName().getString());

        offerDeliveredTick = now;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Helpers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Find a player who satisfies all the canon-faithful preconditions:
     * within DETECT_RANGE, Qi Condensation+, cooldown expired.
     */
    private ServerPlayer findEligiblePlayer() {
        long now = cultivator.level().getGameTime();
        for (ServerPlayer player : cultivator.level().getEntitiesOfClass(
                ServerPlayer.class,
                cultivator.getBoundingBox().inflate(DETECT_RANGE))) {

            // Cooldown check (shared with right-click handler)
            long lastGift = player.getPersistentData().getLong(NBT_LAST_GIFT_TIME);
            if (now - lastGift < COOLDOWN_TICKS) continue;

            // Cultivation gate: Qi Condensation+
            var stateOpt = CultivationCapability.get(player);
            if (!stateOpt.isPresent()) continue;
            CultivationState state = stateOpt.resolve().get();
            if (state.getCurrentRealm().order < RealmId.QI_CONDENSATION.order) continue;

            return player;
        }
        return null;
    }

    /**
     * Find the first Wang Lin gift (sorted by ascending affinity threshold)
     * that passes the existing four-question evaluation gate.
     */
    private ManifestationGiftSystem.GiftRecord findOfferedGift(ServerPlayer player) {
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = buildSnapshot(player);
        List<ManifestationGiftSystem.GiftRecord> gifts =
                ManifestationGiftSystem.getGiftsByProtagonist(protagonistId);
        // Sort by ascending affinity threshold (most accessible first)
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

    /**
     * Build a PlayerStateSnapshot for the existing ManifestationGiftSystem.
     * (Mirrors ManifestationGiftHandler's snapshot logic — no new infra.)
     */
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

    /** Pathfind toward the target player. */
    private void walkTowardPlayer() {
        if (targetPlayer == null) return;
        cultivator.getNavigation().moveTo(
                targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), 0.9D);
    }

    /**
     * Pick a canon-faithful fallback physical item when the gift's
     * canonOriginId is null or not yet registered as a Forge item.
     *
     * <p>Canon basis:
     * <ul>
     *   <li><b>CANONICAL_TECHNIQUE</b> → jade_slip. Techniques are stored
     *       on jade slips in the Er Gen universe — Wang Lin finds, creates,
     *       and distributes jade slips throughout Renegade Immortal.
     *       This makes "Wang Lin possesses the slip" playable.</li>
     *   <li><b>POST_CANON_HERB</b> → qi_gathering_pill (a registered,
     *       usable pill — represents Wang Lin's gathered spirit herbs
     *       condensed into pill form).</li>
     *   <li><b>POST_CANON_CORE</b> → beast_core (the registered generic
     *       beast core item — represents Wang Lin's slain beast cores).</li>
     *   <li><b>POST_CANON_FORGED</b> → jade_slip (Bai Xiaochun-style
     *       pill recipes inscribed on a slip).</li>
     *   <li><b>CANONICAL_TREASURE</b> → null (no fallback — Wang Lin would
     *       never give a fake; the canonical item must be registered).</li>
     * </ul>
     *
     * @return the fallback Item, or null if no canon-faithful fallback exists
     */
    private Item pickFallbackItem(ManifestationGiftSystem.GiftRecord gift) {
        if (gift == null || gift.category == null) return null;
        switch (gift.category) {
            case CANONICAL_TECHNIQUE:
                // Techniques live on jade slips — Wang Lin hands the player
                // a slip inscribed with the technique.
                return dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get();
            case POST_CANON_HERB:
                return dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get();
            case POST_CANON_CORE:
                return dev.ergenverse.item.ErgenverseItems.BEAST_CORE.get();
            case POST_CANON_FORGED:
                // Bai Xiaochun's pills/recipes → inscribe on a jade slip
                return dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get();
            case CANONICAL_TREASURE:
            default:
                // No fallback for canonical treasures — the canonical item
                // itself must be registered, otherwise Wang Lin stays silent.
                return null;
        }
    }

    /**
     * Lazy-load the NPC's JSON data. Returns true if data is loaded (and
     * the NPC has offers_gifts=true); false if data couldn't be loaded OR
     * the NPC doesn't offer gifts.
     */
    private boolean loadDataIfNeeded() {
        if (dataLoaded) return !offeringLines.isEmpty();
        dataLoaded = true;

        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return false;

        try {
            JsonObject data = WorldStateDataLoader.getEntry("npcs", characterId);
            if (data == null) return false;

            // Check offers_gifts flag
            if (!data.has("offers_gifts") || !data.get("offers_gifts").isJsonPrimitive()
                    || !data.getAsJsonPrimitive("offers_gifts").getAsBoolean()) {
                return false;
            }

            // Load protagonist ID (defaults to wang_lin)
            if (data.has("gift_protagonist_id")
                    && data.get("gift_protagonist_id").isJsonPrimitive()) {
                protagonistId = data.getAsJsonPrimitive("gift_protagonist_id").getAsString();
            }

            // Load offering lines
            if (data.has("gift_offering_lines")
                    && data.get("gift_offering_lines").isJsonArray()) {
                for (var elem : data.getAsJsonArray("gift_offering_lines")) {
                    if (elem.isJsonPrimitive() && elem.getAsJsonPrimitive().isString()) {
                        offeringLines.add(elem.getAsString());
                    }
                }
            }
            // Fallback: if offers_gifts=true but no lines provided, use a default
            if (offeringLines.isEmpty()) {
                offeringLines.add("Hold out your hand.");
            }
            return true;
        } catch (Exception e) {
            Ergenverse.LOGGER.debug("[NpcGiftOffer] Failed to load data for {}: {}",
                    characterId, e.getMessage());
            return false;
        }
    }
}
