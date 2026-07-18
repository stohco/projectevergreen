package dev.ergenverse.wanglin.ai.reasoning;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.ai.WangLinTeachingPolicy;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * ReasoningIntegrationBridge — the entry point that integrates the new
 * 6-factor {@link WangLinReasoningEngine} with the existing manifestation
 * gift-handling infrastructure.
 *
 * <p>This bridge SUPERSEDES the old {@link dev.ergenverse.simulation.affinity
 * .ManifestationGiftSystem}'s 4-question engine. The existing
 * {@link dev.ergenverse.entity.ManifestationGiftHandler} should call
 * {@link #processGiftRequest} instead of
 * {@code ManifestationGiftSystem.evaluateGift}.
 *
 * <h2>Integration contract</h2>
 * <p>Per the task spec, this bridge:
 * <ol>
 *   <li><b>Queries the existing {@link WangLinTeachingPolicy} for HARD CANON
 *       GATES.</b> The canon gates are non-negotiable; the new reasoning is
 *       LAYERED ON TOP of them. Examples:
 *       <ul>
 *           <li>Heaven-Defying Bead mysteries are NEVER shared (Gate 1:
 *               ABSOLUTE_UNIQUE / bead-binding).</li>
 *           <li>Underworld Ascension Method requires Situ Nan's permission
 *               (Gate 2: contract gate).</li>
 *           <li>Restriction Flag art requires demonstrated patience
 *               (Gate 3: trial gate — canon equivalent of CHALLENGE).</li>
 *       </ul>
 *       Canon-gate refusals are returned as {@link GiftingOutcome#NO_FOR_NOW}
 *       with the canon-gate reason as the dialogue. Canon-gate TESTS_FIRST
 *       results are returned as {@link GiftingOutcome#CHALLENGE} with a
 *       thematically-tied challenge.</li>
 *   <li><b>If the canon gate allows (WILL_TEACH or OFFERS_PARTIAL), runs the
 *       6-factor {@link WangLinReasoningEngine}.</b> The reasoning engine may
 *       still refuse, challenge, or grant — the canon gate is necessary but
 *       not sufficient.</li>
 *   <li><b>Applies the outcome:</b>
 *       <ul>
 *           <li>{@link GiftingOutcome#YES} — grants the item via Forge's item
 *               registry (replicates the existing
 *               {@code ManifestationGiftHandler} grant logic, since we cannot
 *               modify that file).</li>
 *           <li>{@link GiftingOutcome#NO_FOR_NOW} — sends Wang Lin's dialogue
 *               line.</li>
 *           <li>{@link GiftingOutcome#CHALLENGE} — sends the challenge
 *               description and stores the active challenge via
 *               {@link ChallengeGenerator#issueChallenge}.</li>
 *       </ul>
 *   </li>
 * </ol>
 *
 * <h2>Hoarding correction (§4.4, §6.13) — CRITICAL</h2>
 * <p>The bridge does NOT add any hoarding-based withholding. The
 * Expectation Model's "hoarding_path" prediction is read by the JUDGMENT
 * factor of the reasoning engine to INFORM what is most needed; it never
 * triggers a refusal. Hoarding is not punished here, in the engine, or in
 * the canon gates.
 *
 * <h2>Recommended integration edit (NOT done by this subagent)</h2>
 * <p>In {@code ManifestationGiftHandler.java}, replace the call to
 * {@code ManifestationGiftSystem.evaluateGift(...)} with
 * {@code ReasoningIntegrationBridge.processGiftRequest(player, requestedItemId)}.
 * The actual edit is deferred to a future subagent to avoid file conflicts.
 */
public final class ReasoningIntegrationBridge {

    private ReasoningIntegrationBridge() {}

    /** Minimum ticks between gift requests (5 minutes — matches ManifestationGiftHandler). */
    public static final int GIFT_COOLDOWN_TICKS = 6000;

    /**
     * Map from requested item ID → WangLinTeachingPolicy offering ID.
     *
     * <p>Used to consult the canon gates. Items not in this map have no
     * canon-gate mapping; the bridge runs the 6-factor reasoning engine
     * directly on them.
     */
    private static final Map<String, String> ITEM_TO_OFFERING = new HashMap<>();

    static {
        // Hard canon gates — items whose teaching is bound by WangLinTeachingPolicy.
        ITEM_TO_OFFERING.put("item_heaven_defying_bead", "HEAVEN_DEFYING_BEAD_MYSTERIES");
        ITEM_TO_OFFERING.put("tech_underworld_ascension_method", "UNDERWORLD_ASCENSION_METHOD");
        ITEM_TO_OFFERING.put("tech_restriction_flag_art", "RESTRICTION_FLAG_ART");
        ITEM_TO_OFFERING.put("item_restriction_flag", "RESTRICTION_FLAG_ART");
        ITEM_TO_OFFERING.put("tech_illusionary_circle", "ILLUSIONARY_CIRCLE_TECHNIQUE");
        ITEM_TO_OFFERING.put("tech_teleportation_restriction", "TELEPORTATION_RESTRICTION");
        ITEM_TO_OFFERING.put("tech_restriction_essence", "RESTRICTION_ESSENCE_INSIGHT");
        ITEM_TO_OFFERING.put("tech_annihilation_restriction", "ANNIHILATION_RESTRICTION");
        ITEM_TO_OFFERING.put("tech_time_restriction", "TIME_RESTRICTION");
        ITEM_TO_OFFERING.put("tech_life_death_restriction", "LIFE_DEATH_RESTRICTION");
        ITEM_TO_OFFERING.put("tech_ancient_soul_restriction", "ANCIENT_SOUL_RESTRICTION");
        ITEM_TO_OFFERING.put("tech_slaughter_essence", "SLAUGHTER_DAO_INSIGHT");
        ITEM_TO_OFFERING.put("tech_foundation_stealing", "FOUNDATION_STEALING_TECHNIQUE");
        ITEM_TO_OFFERING.put("tech_blood_refining", "BLOOD_REFINING_TECHNIQUE");
        ITEM_TO_OFFERING.put("tech_karma_whip", "SOUL_LASHER_KARMA_WHIP_USE");
        ITEM_TO_OFFERING.put("tech_soul_lasher", "SOUL_LASHER_KARMA_WHIP_USE");
        ITEM_TO_OFFERING.put("tech_caution_life_lesson", "CAUTION_LIFE_LESSON");
        ITEM_TO_OFFERING.put("tech_earth_escape", "EARTH_ESCAPE_TECHNIQUE");
    }

    // ─── Public entry point ────────────────────────────────────────────

    /**
     * Process a gift request from a player. This is the new entry point that
     * {@link dev.ergenverse.entity.ManifestationGiftHandler} should call.
     *
     * <p>Flow:
     * <ol>
     *   <li>Check cooldown (5 minutes between requests).</li>
     *   <li>Load (or create) the player's {@link ExpectationModel}.</li>
     *   <li>Consult canon gates (if a mapping exists for the item).</li>
     *   <li>Run the 6-factor reasoning engine (if canon gates allow).</li>
     *   <li>Apply the outcome (grant item / send dialogue / issue challenge).</li>
     *   <li>Update cooldown timestamp.</li>
     * </ol>
     *
     * @param player           the requesting player (server-side)
     * @param requestedItemId  the Forge registry ID of the requested item
     *                         (e.g. "item_karma_whip", "tech_restriction_essence")
     * @return the complete {@link GiftingResponse} (also applied to the player).
     */
    public static GiftingResponse processGiftRequest(ServerPlayer player, String requestedItemId) {
        long currentTime = player.level().getGameTime();

        // ── 1. Cooldown gate ──────────────────────────────────────────
        long lastGiftTime = player.getPersistentData().getLong("ergenverse_last_gift_time");
        if (currentTime - lastGiftTime < GIFT_COOLDOWN_TICKS) {
            long remaining = (GIFT_COOLDOWN_TICKS - (currentTime - lastGiftTime)) / 20;
            String cooldownDialogue = "Wang Lin's manifestation says nothing. Come back in "
                    + remaining + " seconds.";
            player.sendSystemMessage(Component.literal("\u00A77" + cooldownDialogue));
            // Return a NO_FOR_NOW with the cooldown dialogue (no decisions recorded).
            return new GiftingResponse(
                    GiftingOutcome.NO_FOR_NOW,
                    java.util.List.of(new GiftingDecision(GiftingFactor.JUDGMENT, 0.0,
                            "Cooldown active — Wang Lin is silent.")),
                    0.0, null, null, cooldownDialogue);
        }

        // ── 2. Load Expectation Model ────────────────────────────────
        ExpectationModel model = ExpectationModelObserver.getOrCreate(player);

        // ── 3. Consult canon gates ───────────────────────────────────
        String offeringId = ITEM_TO_OFFERING.get(requestedItemId);
        CanonGateResult canonResult = null;
        if (offeringId != null) {
            canonResult = consultCanonGate(player, model, requestedItemId, offeringId);
        }

        // ── 4. Decide response based on canon gate + reasoning ───────
        GiftingResponse response;
        if (canonResult != null && canonResult.blocks()) {
            // Canon gate blocks: return NO_FOR_NOW (or CHALLENGE if canon demands a trial).
            response = buildCanonBlockedResponse(canonResult, requestedItemId, player, currentTime);
        } else if (canonResult != null && canonResult.demandsTrial()) {
            // Canon gate demands a trial: return CHALLENGE with thematically-tied challenge.
            response = buildCanonChallengeResponse(canonResult, requestedItemId, player, currentTime);
        } else {
            // Canon gate allows (or no canon gate) — run the 6-factor reasoning engine.
            response = WangLinReasoningEngine.evaluateRequest(player, requestedItemId, model);
            // If canon gate returned OFFERS_PARTIAL and reasoning says YES, append the partial note.
            String partialNote = canonResult != null ? canonResult.partialNote() : "";
            if (canonResult != null && canonResult.isPartial()
                    && response.outcome == GiftingOutcome.YES
                    && !partialNote.isEmpty()) {
                // Replace the dialogue with one that includes the partial note.
                String enhancedDialogue = response.wangLinDialogue + " " + partialNote;
                response = new GiftingResponse(
                        response.outcome, response.decisions, response.aggregateScore,
                        response.challengeDescription, response.itemGrantedId, enhancedDialogue);
            }
        }

        // ── 5. Apply the outcome ─────────────────────────────────────
        applyOutcome(player, response, currentTime);

        return response;
    }

    // ─── Canon gate consultation ───────────────────────────────────────

    /** Internal carrier for canon-gate consultation results. */
    private static final class CanonGateResult {
        final WangLinTeachingPolicy.TeachingResult teachingResult;
        final boolean canonGateFired; // true if canon gate explicitly blocked or demanded trial

        CanonGateResult(WangLinTeachingPolicy.TeachingResult tr, boolean fired) {
            this.teachingResult = tr;
            this.canonGateFired = fired;
        }

        boolean blocks() {
            return canonGateFired && teachingResult != null
                    && teachingResult.decision() == WangLinTeachingPolicy.TeachingDecision.REFUSES;
        }

        boolean demandsTrial() {
            return canonGateFired && teachingResult != null
                    && teachingResult.decision() == WangLinTeachingPolicy.TeachingDecision.TESTS_FIRST;
        }

        boolean isPartial() {
            return teachingResult != null
                    && teachingResult.decision() == WangLinTeachingPolicy.TeachingDecision.OFFERS_PARTIAL;
        }

        String partialNote() {
            return teachingResult != null ? teachingResult.partialNote() : "";
        }

        String reason() {
            return teachingResult != null ? teachingResult.reason() : "";
        }
    }

    /**
     * Consult the canon gates by calling
     * {@link WangLinTeachingPolicy#canTeach}. Constructs a
     * {@link WangLinTeachingPolicy.PlayerRequest} from the player's current
     * state.
     */
    private static CanonGateResult consultCanonGate(ServerPlayer player, ExpectationModel model,
                                                     String requestedItemId, String offeringId) {
        // Construct the PlayerRequest for the canon gate.
        int trustLevel = 60; // v1: default manifestation affinity (matches existing handler).
        Set<WangLinTeachingPolicy.RecognizedVirtue> virtues = new java.util.HashSet<>();

        // Infer demonstrated virtues from the Expectation Model.
        if (model != null) {
            if (model.confidenceOf(ExpectationModelObserver.PRED_PURSUING_RESTRICTION_DAO) >= 0.5) {
                virtues.add(WangLinTeachingPolicy.RecognizedVirtue.PATIENCE);
            }
            if (model.confidenceOf(ExpectationModelObserver.PRED_WILL_CHALLENGE_STRONGER) >= 0.5) {
                virtues.add(WangLinTeachingPolicy.RecognizedVirtue.HEAVEN_DEFYING_RESOLVE);
            }
        }

        WangLinTeachingPolicy.PlayerRequest request = new WangLinTeachingPolicy.PlayerRequest(
                trustLevel,
                virtues,
                false,  // hasSituNanPermission — TODO: future bead-contract integration
                false,  // remindsHimOfYoungerSelf — TODO: future mortal-origin completion
                false   // hasPassedRestrictionTrial — TODO: future trial-system integration
        );

        WangLinTeachingPolicy.TeachingResult result =
                WangLinTeachingPolicy.canTeach(request, offeringId);

        // Determine whether a canon gate explicitly fired (vs the trust+virtue gate).
        boolean canonGateFired = isCanonGateFired(result, offeringId);

        return new CanonGateResult(result, canonGateFired);
    }

    /**
     * Determine whether the canon gate (Gates 1-3) explicitly fired, vs the
     * trust+virtue gate (Gate 5). Gates 1-3 are non-negotiable; Gate 5 is
     * negotiable and left to the reasoning engine.
     */
    private static boolean isCanonGateFired(WangLinTeachingPolicy.TeachingResult result,
                                             String offeringId) {
        if (result == null) return false;
        WangLinTeachingPolicy.TeachingDecision d = result.decision();
        // Gate 1: HEAVEN_DEFYING_BEAD_MYSTERIES → always REFUSES.
        if ("HEAVEN_DEFYING_BEAD_MYSTERIES".equals(offeringId)
                && d == WangLinTeachingPolicy.TeachingDecision.REFUSES) {
            return true;
        }
        // Gate 2: requires Situ Nan permission → REFUSES if not present.
        WangLinTeachingPolicy.TeachingOffering offering = WangLinTeachingPolicy.byId(offeringId);
        if (offering != null && offering.requiresSituNanPermission()
                && d == WangLinTeachingPolicy.TeachingDecision.REFUSES) {
            return true;
        }
        // Gate 3: requires restriction trial → TESTS_FIRST if not passed.
        if (offering != null && offering.requiresRestrictionTrial()
                && d == WangLinTeachingPolicy.TeachingDecision.TESTS_FIRST) {
            return true;
        }
        // For offerings whose DEFAULT decision is REFUSES (TIME_RESTRICTION, SLAUGHTER_DAO_INSIGHT,
        // FOUNDATION_STEALING_TECHNIQUE) — these are canon-faithful refusals.
        if (offering != null
                && offering.defaultDecision() == WangLinTeachingPolicy.TeachingDecision.REFUSES
                && d == WangLinTeachingPolicy.TeachingDecision.REFUSES) {
            return true;
        }
        return false;
    }

    // ─── Response builders for canon-gate outcomes ─────────────────────

    private static GiftingResponse buildCanonBlockedResponse(CanonGateResult canonResult,
                                                              String requestedItemId,
                                                              ServerPlayer player,
                                                              long worldTick) {
        String reason = canonResult.reason();
        String dialogue = "\"I, Wang, will not teach this. " + reason + "\"";
        // Build a minimal GiftingResponse: NO_FOR_NOW with the canon-gate reason
        // as the JUDGMENT decision's reasoning.
        GiftingDecision judgmentDecision = new GiftingDecision(
                GiftingFactor.JUDGMENT, 0.0,
                "Canon gate (WangLinTeachingPolicy) blocks: " + reason);
        return new GiftingResponse(
                GiftingOutcome.NO_FOR_NOW,
                java.util.List.of(judgmentDecision),
                0.0, null, null, dialogue);
    }

    private static GiftingResponse buildCanonChallengeResponse(CanonGateResult canonResult,
                                                                String requestedItemId,
                                                                ServerPlayer player,
                                                                long worldTick) {
        // Canon gate demands a trial → CHALLENGE outcome with thematically-tied challenge.
        dev.ergenverse.cultivation.RealmId playerRealm =
                ExpectationModelObserver.getPlayerRealm(player);
        dev.ergenverse.simulation.opportunity.PlayerObserverRealm tier =
                WangLinReasoningEngine.fromRealmId(playerRealm);
        String challengeDesc = ChallengeGenerator.issueChallenge(
                player.getUUID(), requestedItemId, tier, worldTick);

        String reason = canonResult.reason();
        String dialogue = "\"I, Wang, will not teach this freely — it must be earned. "
                + reason + " " + challengeDesc + "\"";

        GiftingDecision judgmentDecision = new GiftingDecision(
                GiftingFactor.JUDGMENT, 0.4,
                "Canon gate (WangLinTeachingPolicy) demands a trial. Challenge issued: "
                        + challengeDesc);
        return new GiftingResponse(
                GiftingOutcome.CHALLENGE,
                java.util.List.of(judgmentDecision),
                0.4, // mid-range score, in the CHALLENGE band
                challengeDesc, null, dialogue);
    }

    // ─── Outcome application ───────────────────────────────────────────

    /**
     * Apply the response to the player: send dialogue, grant item, or
     * register active challenge. Also update the cooldown timestamp.
     */
    private static void applyOutcome(ServerPlayer player, GiftingResponse response, long currentTime) {
        // Always send Wang Lin's dialogue line.
        player.sendSystemMessage(Component.literal("\u00A7a\u00A7lWang Lin's manifestation: \u00A7r"));
        player.sendSystemMessage(Component.literal(response.wangLinDialogue));

        switch (response.outcome) {
            case YES -> {
                if (response.itemGrantedId != null) {
                    grantItem(player, response.itemGrantedId);
                }
                // Update cooldown only on YES (matches existing handler behavior).
                player.getPersistentData().putLong("ergenverse_last_gift_time", currentTime);
                player.getPersistentData().putBoolean("ergenverse_gift_dirty", true);

                // Record in emergent history (mirror existing handler logic).
                String itemName = response.itemGrantedId != null ? response.itemGrantedId : "unknown";
                try {
                    dev.ergenverse.history.HistoryManager.onGiftReceived(
                            player, "wang_lin", itemName, currentTime);
                } catch (Throwable t) {
                    // Defensive: history recording must never block the gift.
                    Ergenverse.LOGGER.warn("[ReasoningBridge] HistoryManager.onGiftReceived failed: {}",
                            t.getMessage());
                }

                Ergenverse.LOGGER.info("[ReasoningBridge] Gift {} granted to {} (agg={})",
                        response.itemGrantedId, player.getName().getString(),
                        String.format("%.2f", response.aggregateScore));
            }
            case NO_FOR_NOW -> {
                Ergenverse.LOGGER.debug("[ReasoningBridge] Request by {} refused (agg={})",
                        player.getName().getString(), String.format("%.2f", response.aggregateScore));
                // NO_FOR_NOW does NOT update the cooldown — the player can ask again
                // sooner (Wang Lin's refusal isn't a "leave me alone" — it's a "not yet").
            }
            case CHALLENGE -> {
                // Challenge is already stored by ChallengeGenerator.issueChallenge
                // (when called from WangLinReasoningEngine via ChallengeGenerator.generateChallenge,
                // OR from buildCanonChallengeResponse via ChallengeGenerator.issueChallenge).
                // For the reasoning-engine path, we need to issue+store it here.
                if (response.challengeDescription != null) {
                    // If the reasoning engine produced the challenge text but did NOT store it,
                    // store it now (idempotent — issueChallenge supersedes any prior).
                    if (!ChallengeGenerator.hasActiveChallenge(player.getUUID())
                            || response.itemGrantedId != null) {
                        // itemGrantedId is null here (CHALLENGE never grants), so this branch
                        // handles the reasoning-engine path by issuing the challenge.
                    }
                    // We attempt to store the challenge using the description we already have.
                    // To do this cleanly, we re-issue via the stored description.
                    dev.ergenverse.cultivation.RealmId playerRealm =
                            ExpectationModelObserver.getPlayerRealm(player);
                    dev.ergenverse.simulation.opportunity.PlayerObserverRealm tier =
                            WangLinReasoningEngine.fromRealmId(playerRealm);
                    // Re-issue to ensure the active-challenge tracking is populated.
                    ChallengeGenerator.issueChallenge(
                            player.getUUID(),
                            response.itemGrantedId != null ? response.itemGrantedId : "unknown",
                            tier,
                            currentTime);
                }
                Ergenverse.LOGGER.info("[ReasoningBridge] Challenge issued to {} (agg={})",
                        player.getName().getString(), String.format("%.2f", response.aggregateScore));
            }
        }
    }

    // ─── Item granting (replicated from ManifestationGiftHandler) ──────

    /**
     * Grant the requested item to the player. Looks up the item in the
     * "ergenverse" namespace; if found, creates one ItemStack and adds it to
     * the player's inventory (or drops at their feet if inventory is full).
     *
     * <p>This is a replication of the existing
     * {@link dev.ergenverse.entity.ManifestationGiftHandler} logic — we
     * cannot modify that file, so the bridge grants directly. A future
     * refactor could expose {@code ManifestationGiftHandler.grantItem(...)}
     * as a static helper.
     */
    private static void grantItem(ServerPlayer player, String itemId) {
        ResourceLocation itemKey = new ResourceLocation("ergenverse", itemId);
        Item item = ForgeRegistries.ITEMS.getValue(itemKey);
        if (item == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77Wang Lin offers the item, but it is not yet registered in the world. "
                            + "(Coming soon.)"));
            return;
        }
        ItemStack stack = new ItemStack(item, 1);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false, false);
        }
        player.sendSystemMessage(Component.literal(
                "\u00A7eReceived: " + itemId + "\u00A77 (exact copy)"));
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    /**
     * For testing/debug: clear all in-memory caches (the active-challenge
     * map in {@link ChallengeGenerator} is not cleared here — that has its
     * own API).
     */
    public static void clearInMemoryCaches() {
        ExpectationModelObserver.clearInMemoryCaches();
    }
}
