package dev.ergenverse.wanglin.ai.reasoning;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;
import dev.ergenverse.simulation.opportunity.PlayerObserverRealm;
import dev.ergenverse.wanglin.ai.WangLinSpeechPatterns;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * WangLinReasoningEngine — the central 6-factor reasoning engine that governs
 * Wang Lin's gifting decisions, per PROJECT_MASTER.md §4.4.
 *
 * <p>This engine SUPERSEDES the old affinity-tier model
 * ({@link ManifestationGiftSystem}'s 4-question engine). It is queried via
 * {@link ReasoningIntegrationBridge}, which layers the new reasoning on top
 * of the existing canon gates in {@link dev.ergenverse.wanglin.ai
 * .WangLinTeachingPolicy}.
 *
 * <h2>The Six Factors (per §4.4)</h2>
 * <ol>
 *   <li><b>NECESSITY</b> — Does the player actually need this right now?</li>
 *   <li><b>SAFETY</b> — Will giving this item get the player killed?
 *       (too much power too fast)</li>
 *   <li><b>USEFULNESS</b> — Can the player actually use this at their
 *       current level?</li>
 *   <li><b>UNIQUENESS</b> — Is this item irreplaceable? Does the true body
 *       only have one?</li>
 *   <li><b>CURRENT_NEED</b> — Does Wang Lin himself need this item right
 *       now?</li>
 *   <li><b>JUDGMENT</b> — Based on the {@link ExpectationModel} of the
 *       player (§6.13), is this the right time? Weighted 1.5×.</li>
 * </ol>
 *
 * <h2>The Three Outcomes (per §4.4)</h2>
 * <ul>
 *   <li><b>YES</b> — aggregate score ≥ 0.7. Item granted.</li>
 *   <li><b>CHALLENGE</b> — 0.3 ≤ score &lt; 0.7. Thematically-tied challenge
 *       issued via {@link ChallengeGenerator}.</li>
 *   <li><b>NO_FOR_NOW</b> — score &lt; 0.3. Honest refusal. Wang Lin NEVER
 *       becomes a content dispenser (§4.4 ⚠ WARNING).</li>
 * </ul>
 *
 * <h2>Hoarding correction (§4.4, §6.13) — CRITICAL</h2>
 * <p>The JUDGMENT factor NEVER lowers the score based on hoarding behavior.
 * If the {@link ExpectationModel} holds a high-confidence "hoarding_path"
 * prediction, JUDGMENT instead looks at WHAT THE PLAYER NEEDS MOST (a
 * hoarding player probably needs a breakthrough catalyst more than another
 * treasure). Hoarding informs what is needed; it never triggers punitive
 * withholding.
 */
public final class WangLinReasoningEngine {

    private WangLinReasoningEngine() {}

    // ─── Outcome thresholds (per §4.4) ─────────────────────────────────

    /** aggregate score ≥ this → YES (grant item). */
    public static final double YES_THRESHOLD = 0.7;
    /** aggregate score < this → NO_FOR_NOW (refusal). */
    public static final double NO_FOR_NOW_THRESHOLD = 0.3;
    // Between thresholds → CHALLENGE.

    // ─── Wang Lin's currently-in-use items (for CURRENT_NEED factor) ───

    /**
     * The set of canon items Wang Lin is currently using himself — items he
     * cannot spare without compromising his own path. For v1, hardcoded per
     * the spec ("hardcode a few items"). A future subagent can wire this to
     * a runtime Wang Lin state.
     *
     * <p>Canon basis:
     * <ul>
     *   <li>{@code item_heaven_defying_bead} — the bead is permanently
     *       bound to Wang Lin (Ch. 8, E88). It cannot be spared.</li>
     *   <li>{@code item_karma_whip} — primary karmic weapon; in continuous
     *       use (Ch. 731).</li>
     *   <li>{@code item_origin_swords} — the 7 Origin Swords are Wang Lin's
     *       crystallized dao; he wields them continuously (E80).</li>
     *   <li>{@code tech_dream_dao} — the Dream Dao + Flowing Time is Wang
     *       Lin's ongoing time-loop mechanism; cannot be transmitted fully
     *       (Ch. 1433).</li>
     * </ul>
     */
    public static final Set<String> WANG_LIN_CURRENTLY_USING = Set.of(
            "item_heaven_defying_bead",
            "item_karma_whip",
            "item_origin_swords",
            "tech_dream_dao"
    );

    // ─── Public entry point ────────────────────────────────────────────

    /**
     * Evaluate a gift request and produce a {@link GiftingResponse}.
     *
     * <p>This method runs all six factors, aggregates the weighted scores,
     * decides the outcome, and generates Wang Lin's dialogue line.
     *
     * <p>This method does NOT consult the canon gates in
     * {@link dev.ergenverse.wanglin.ai.WangLinTeachingPolicy}. Canon-gate
     * consultation is the responsibility of {@link ReasoningIntegrationBridge},
     * which calls this engine AFTER (or instead of) the canon gates.
     *
     * @param player           the requesting player
     * @param requestedItemId  the Forge registry ID of the requested item
     * @param model            Wang Lin's Expectation Model of this player
     * @return the complete response (outcome + decisions + dialogue).
     */
    public static GiftingResponse evaluateRequest(ServerPlayer player,
                                                   String requestedItemId,
                                                   ExpectationModel model) {
        // Resolve player state.
        RealmId playerRealm = ExpectationModelObserver.getPlayerRealm(player);
        Map<String, Double> playerDao = ExpectationModelObserver.getPlayerDaoMap(player);
        long worldTick = player.level().getGameTime();

        // Resolve item metadata from the existing gift registry (best-effort).
        ItemMeta meta = resolveItemMeta(requestedItemId);

        // Run all six factors in canonical order.
        List<GiftingDecision> decisions = new ArrayList<>(6);
        decisions.add(evaluateNecessity(playerRealm, playerDao, meta, requestedItemId));
        decisions.add(evaluateSafety(playerRealm, meta));
        decisions.add(evaluateUsefulness(playerRealm, meta));
        decisions.add(evaluateUniqueness(meta));
        decisions.add(evaluateCurrentNeed(requestedItemId));
        decisions.add(evaluateJudgment(model, meta, requestedItemId, worldTick));

        // Aggregate: weighted average.
        double aggregateScore = aggregateWeighted(decisions);

        // Decide outcome.
        GiftingOutcome outcome;
        String challengeDescription = null;
        String itemGrantedId = null;
        if (aggregateScore >= YES_THRESHOLD) {
            outcome = GiftingOutcome.YES;
            itemGrantedId = requestedItemId;
        } else if (aggregateScore < NO_FOR_NOW_THRESHOLD) {
            outcome = GiftingOutcome.NO_FOR_NOW;
        } else {
            outcome = GiftingOutcome.CHALLENGE;
            PlayerObserverRealm tier = fromRealmId(playerRealm);
            challengeDescription = ChallengeGenerator.generateChallenge(requestedItemId, tier);
        }

        // Generate Wang Lin's dialogue line.
        GiftingFactor dominantFactor = pickDominantFactor(decisions);
        String dialogue = generateDialogue(outcome, dominantFactor, decisions, meta, challengeDescription);

        return new GiftingResponse(outcome, decisions, aggregateScore,
                challengeDescription, itemGrantedId, dialogue);
    }

    // ─── Factor evaluation ─────────────────────────────────────────────

    /**
     * NECESSITY — Does the player actually need this right now?
     *
     * <p>High if the item is the player's next-step breakthrough aid
     * (item's required realm == player's realm + 1) or if the player has
     * matching dao comprehension. Low if the item is far above the player's
     * horizon (more than 2 realms above) or already below the player's
     * current realm.
     */
    static GiftingDecision evaluateNecessity(RealmId playerRealm, Map<String, Double> playerDao,
                                              ItemMeta meta, String requestedItemId) {
        int playerTier = playerRealm.order;
        int itemTier = meta.realmGate;
        double score;
        String reasoning;

        if (itemTier == playerTier + 1) {
            // Perfect next-step breakthrough aid.
            score = 0.85;
            reasoning = "Player is at " + playerRealm.name + " (tier " + playerTier
                    + "); the requested item sits at tier " + itemTier + " — exactly the next "
                    + "threshold. This is the player's near-term need.";
        } else if (itemTier <= playerTier) {
            // Item is below the player's current realm — no longer necessary for breakthrough.
            score = 0.35;
            reasoning = "Player is at " + playerRealm.name + " (tier " + playerTier
                    + "); the requested item sits at tier " + itemTier + " — already passed. "
                    + "Necessity is low.";
        } else if (itemTier > playerTier + 2) {
            // Item is too far above to be a near-term need.
            score = 0.20;
            reasoning = "Player is at " + playerRealm.name + " (tier " + playerTier
                    + "); the requested item sits at tier " + itemTier + " — beyond the next "
                    + "two thresholds. Not a present need.";
        } else {
            // itemTier == playerTier + 2 — borderline.
            score = 0.55;
            reasoning = "Player is at " + playerRealm.name + " (tier " + playerTier
                    + "); the requested item sits at tier " + itemTier + " — two thresholds "
                    + "ahead. Possible future need.";
        }

        // Dao alignment bonus: if player has high comprehension in the item's dao, +0.10.
        if (meta.daoCompatibility != null && !meta.daoCompatibility.isEmpty()) {
            Double comprehension = playerDao.get(meta.daoCompatibility);
            if (comprehension != null && comprehension > 0.3) {
                score = Math.min(1.0, score + 0.10);
                reasoning += " Player shows " + (int) (comprehension * 100) + "% comprehension in "
                        + meta.daoCompatibility + " dao — alignment reinforces necessity.";
            }
        }
        return new GiftingDecision(GiftingFactor.NECESSITY, score, reasoning);
    }

    /**
     * SAFETY — Will giving this item get the player killed?
     *
     * <p>Low if item's required realm &gt; player's realm + 2 (too much power
     * too fast = dangerous). High (safe) otherwise.
     */
    static GiftingDecision evaluateSafety(RealmId playerRealm, ItemMeta meta) {
        int playerTier = playerRealm.order;
        int itemTier = meta.realmGate;
        double score;
        String reasoning;

        if (itemTier <= playerTier + 1) {
            score = 1.0;
            reasoning = "Item tier " + itemTier + " is at most one above player tier " + playerTier
                    + " — power transfer is safe.";
        } else if (itemTier == playerTier + 2) {
            score = 0.5;
            reasoning = "Item tier " + itemTier + " is two above player tier " + playerTier
                    + " — borderline. The player can bear it, but barely.";
        } else {
            score = 0.1;
            reasoning = "Item tier " + itemTier + " is more than two above player tier "
                    + playerTier + " — too much power too fast. The player's dao foundation "
                    + "would crack under the weight. Refusing on safety grounds.";
        }
        return new GiftingDecision(GiftingFactor.SAFETY, score, reasoning);
    }

    /**
     * USEFULNESS — Can the player actually use this at their current level?
     *
     * <p>High if item's required realm ≤ player's realm + 1.
     */
    static GiftingDecision evaluateUsefulness(RealmId playerRealm, ItemMeta meta) {
        int playerTier = playerRealm.order;
        int itemTier = meta.realmGate;
        double score;
        String reasoning;

        if (itemTier <= playerTier) {
            score = 1.0;
            reasoning = "Player tier " + playerTier + " meets/exceeds item tier " + itemTier
                    + " — fully useful.";
        } else if (itemTier == playerTier + 1) {
            score = 0.8;
            reasoning = "Item tier " + itemTier + " is one above player tier " + playerTier
                    + " — useful with effort; the player can grow into it shortly.";
        } else {
            score = 0.3;
            reasoning = "Item tier " + itemTier + " is well above player tier " + playerTier
                    + " — the player cannot yet wield this. Usefulness is low.";
        }
        return new GiftingDecision(GiftingFactor.USEFULNESS, score, reasoning);
    }

    /**
     * UNIQUENESS — Is this item irreplaceable? Does the true body only have one?
     *
     * <p>High for ABSOLUTE_UNIQUE / canonically-tied items (heaven-defying bead,
     * karma whip, origin swords). Low for transferable teachings.
     */
    static GiftingDecision evaluateUniqueness(ItemMeta meta) {
        double score;
        String reasoning;
        if (meta.canonicallyTiedToIdentity) {
            score = 0.9;
            reasoning = "Item is canonically tied to Wang Lin's identity. Replicas exist, but "
                    + "the original is singular. Wang Lin treats this with appropriate weight.";
        } else if (meta.category == ManifestationGiftSystem.GiftCategory.CANONICAL_INHERITANCE) {
            score = 0.5;
            reasoning = "Item is a canonical inheritance — unique in its original form, but the "
                    + "path can be walked again. Moderate uniqueness.";
        } else if (meta.category == ManifestationGiftSystem.GiftCategory.CANONICAL_TECHNIQUE
                || meta.category == ManifestationGiftSystem.GiftCategory.POST_CANON_TECHNIQUE) {
            score = 0.2;
            reasoning = "Item is a technique/teaching — transferable. Multiple students can "
                    + "receive the same teaching. Low uniqueness.";
        } else {
            score = 0.4;
            reasoning = "Item is replicable / generic. Low-to-moderate uniqueness.";
        }
        return new GiftingDecision(GiftingFactor.UNIQUENESS, score, reasoning);
    }

    /**
     * CURRENT_NEED — Does Wang Lin himself need this item right now?
     *
     * <p>Low if Wang Lin is currently using the item (hardcoded set).
     * High (spareable) otherwise.
     */
    static GiftingDecision evaluateCurrentNeed(String requestedItemId) {
        double score;
        String reasoning;
        if (WANG_LIN_CURRENTLY_USING.contains(requestedItemId)) {
            score = 0.1;
            reasoning = "Wang Lin is currently using this item himself. The original cannot be "
                    + "spared; only a replica may be sent, and even that costs Wang Lin a measure "
                    + "of attention. Current-need score is low.";
        } else {
            score = 0.9;
            reasoning = "Wang Lin is not currently using this item. He has it in storage and can "
                    + "spare a replica without compromise. Current-need score is high.";
        }
        return new GiftingDecision(GiftingFactor.CURRENT_NEED, score, reasoning);
    }

    /**
     * JUDGMENT — Based on the Expectation Model, is this the right time?
     *
     * <p>Integrates {@link ExpectationModel}. High if model's top predictions
     * align with the item's purpose (e.g., pursuing_restriction_dao +
     * restriction jade slip = high judgment).
     *
     * <p><b>HOARDING CORRECTION:</b> if "hoarding_path" prediction is
     * high-confidence, this factor looks at what the player NEEDS MOST (e.g.,
     * a hoarding player probably needs a breakthrough catalyst more than
     * another treasure). The hoarding prediction NEVER lowers the score — it
     * only redirects the alignment bonus.
     */
    static GiftingDecision evaluateJudgment(ExpectationModel model, ItemMeta meta,
                                             String requestedItemId, long worldTick) {
        double score = 0.5; // Neutral baseline
        StringBuilder reasoning = new StringBuilder();
        reasoning.append("Judgment integrates the Expectation Model. ");

        // Alignment bonus: top predictions aligned with item's purpose.
        boolean aligned = false;
        if (model != null) {
            // Check specific alignments.
            double restrictionDao = model.confidenceOf(ExpectationModelObserver.PRED_PURSUING_RESTRICTION_DAO);
            if (restrictionDao >= 0.3 && isRestrictionItem(requestedItemId, meta)) {
                score += 0.25;
                aligned = true;
                reasoning.append("Player pursues restriction dao (")
                        .append((int) (restrictionDao * 100))
                        .append("%) — alignment with a restriction item. ");
            }

            double challengesStronger = model.confidenceOf(ExpectationModelObserver.PRED_WILL_CHALLENGE_STRONGER);
            if (challengesStronger >= 0.3 && isCombatItem(requestedItemId, meta)) {
                score += 0.20;
                aligned = true;
                reasoning.append("Player engages stronger cultivators (")
                        .append((int) (challengesStronger * 100))
                        .append("%) — a combat aid may keep them alive. ");
            }

            // HOARDING CORRECTION: hoarding_path NEVER lowers the score.
            // It redirects the alignment bonus toward breakthrough catalysts.
            double hoardingPath = model.confidenceOf(ExpectationModelObserver.PRED_HOARDING_PATH);
            if (hoardingPath >= 0.3) {
                if (isBreakthroughCatalyst(requestedItemId, meta)) {
                    score += 0.20;
                    aligned = true;
                    reasoning.append("Player hoards (")
                            .append((int) (hoardingPath * 100))
                            .append("%) — a self-reliant path; a breakthrough catalyst serves "
                                    + "their genuine need. NOT a punishment — a redirection toward "
                                    + "what they most need. ");
                } else {
                    reasoning.append("Player hoards (")
                            .append((int) (hoardingPath * 100))
                            .append("%) but this item is not a breakthrough catalyst; hoarding "
                                    + "does NOT lower judgment. Score unaffected. ");
                }
            }

            // Survival predictions: if player has demonstrated survival of the item's
            // corresponding challenge, judgment is more favorable.
            for (Prediction p : model.allPredictions()) {
                if (p.id.startsWith("can_survive_") && p.confidence >= 0.5) {
                    if (challengeAlignsWithItem(p.id, requestedItemId)) {
                        score += 0.10;
                        aligned = true;
                        reasoning.append("Player has demonstrated survival of ")
                                .append(p.id).append(" (")
                                .append((int) (p.confidence * 100))
                                .append("%) — confidence in their readiness. ");
                    }
                }
            }

            if (!aligned) {
                reasoning.append("No strong alignment between the player's predicted path and "
                        + "this item. Score remains neutral. ");
            }

            // Top prediction context.
            List<Prediction> top = model.getTopPredictions(1);
            if (!top.isEmpty()) {
                reasoning.append("Top prediction: ").append(top.get(0).id)
                        .append(" (").append(top.get(0).confidencePercent()).append(").");
            }
        } else {
            reasoning.append("Expectation Model is empty — judgment defaults to neutral.");
        }

        // Clamp.
        if (score > 1.0) score = 1.0;
        if (score < 0.0) score = 0.0;

        return new GiftingDecision(GiftingFactor.JUDGMENT, score, reasoning.toString());
    }

    // ─── Aggregation & outcome ─────────────────────────────────────────

    /** Weighted average of all decisions' scores (using each factor's weight). */
    static double aggregateWeighted(List<GiftingDecision> decisions) {
        double weightedSum = 0.0;
        double totalWeight = 0.0;
        for (GiftingDecision d : decisions) {
            weightedSum += d.score * d.factor.weight;
            totalWeight += d.factor.weight;
        }
        if (totalWeight == 0.0) return 0.0;
        double result = weightedSum / totalWeight;
        if (result < 0.0) return 0.0;
        if (result > 1.0) return 1.0;
        return result;
    }

    /**
     * Pick the dominant factor for dialogue flavor: the factor with the
     * largest absolute deviation from 0.5 (the one that most drove the
     * aggregate toward its current value).
     */
    static GiftingFactor pickDominantFactor(List<GiftingDecision> decisions) {
        GiftingFactor dominant = GiftingFactor.JUDGMENT;
        double maxDeviation = -1.0;
        for (GiftingDecision d : decisions) {
            double dev = Math.abs(d.score - 0.5);
            if (dev > maxDeviation) {
                maxDeviation = dev;
                dominant = d.factor;
            }
        }
        return dominant;
    }

    // ─── Dialogue generation ───────────────────────────────────────────

    /**
     * Generate Wang Lin's dialogue line, using his speech patterns (terse,
     * image-rich, occasional "Wang" self-reference). The dialogue references
     * the dominant factor that drove the outcome.
     *
     * <p>Per {@link WangLinSpeechPatterns}: terse, image-rich internal
     * monologue; cold to strangers; rare but followed-through threats. The
     * dialogue here is the SPOKEN line — sparse by contrast with his
     * internal narration.
     */
    static String generateDialogue(GiftingOutcome outcome, GiftingFactor dominantFactor,
                                    List<GiftingDecision> decisions, ItemMeta meta,
                                    String challengeDescription) {
        // Find the dominant decision's reasoning snippet (one clause).
        String dominantReasoning = "";
        for (GiftingDecision d : decisions) {
            if (d.factor == dominantFactor) {
                dominantReasoning = d.reasoning;
                break;
            }
        }

        String itemName = meta.displayName != null && !meta.displayName.isEmpty()
                ? meta.displayName
                : "this item";

        return switch (outcome) {
            case YES -> {
                String flavor = yesFlavor(dominantFactor);
                yield "\"" + flavor + " " + itemName + ". Take it. Your path calls for it.\"";
            }
            case NO_FOR_NOW -> {
                String flavor = noFlavor(dominantFactor);
                yield "\"I, Wang, will not give what would harm you. " + flavor
                        + " Return when you have grown. " + shortenReasoning(dominantReasoning) + "\"";
            }
            case CHALLENGE -> {
                String flavor = challengeFlavor(dominantFactor);
                yield "\"I will consider your request for " + itemName + ". " + flavor
                        + " " + challengeDescription + "\"";
            }
        };
    }

    private static String yesFlavor(GiftingFactor dominant) {
        return switch (dominant) {
            case NECESSITY -> "You need this. I will not withhold what the path demands.";
            case SAFETY -> "You can bear it. That is enough.";
            case USEFULNESS -> "You can wield it. That is enough.";
            case UNIQUENESS -> "Even the singular may be replicated, when the need is true.";
            case CURRENT_NEED -> "I am not using this. It serves you better than my storage.";
            case JUDGMENT -> "Your path aligns. I, Wang Lin, give what I see is needed.";
        };
    }

    private static String noFlavor(GiftingFactor dominant) {
        return switch (dominant) {
            case NECESSITY -> "You do not need this yet.";
            case SAFETY -> "This would kill you. Too much, too fast.";
            case USEFULNESS -> "You cannot wield this. Your realm cannot bear it.";
            case UNIQUENESS -> "This is singular. The original is not mine to give; only "
                    + "derivatives are possible.";
            case CURRENT_NEED -> "I am using this. It is not spareable at this moment.";
            case JUDGMENT -> "Your path does not yet call for this.";
        };
    }

    private static String challengeFlavor(GiftingFactor dominant) {
        return switch (dominant) {
            case NECESSITY -> "But first, prove the need is real.";
            case SAFETY -> "But first, prove your foundation can bear the weight.";
            case USEFULNESS -> "But first, prove you can wield it.";
            case UNIQUENESS -> "But first, prove yourself worthy of the singular.";
            case CURRENT_NEED -> "But first, complete a task for me; then I will consider it.";
            case JUDGMENT -> "But first, prove your path aligns with what you ask.";
        };
    }

    /** Shorten a factor's reasoning into a single concise clause for dialogue. */
    private static String shortenReasoning(String reasoning) {
        if (reasoning == null || reasoning.isEmpty()) return "";
        // Take the first sentence; if too long, truncate.
        int period = reasoning.indexOf('.');
        String first = period > 0 ? reasoning.substring(0, period + 1) : reasoning;
        if (first.length() > 120) {
            first = first.substring(0, 117) + "...";
        }
        return first;
    }

    // ─── Item metadata resolution ──────────────────────────────────────

    /**
     * Lightweight carrier for item metadata resolved from the gift registry.
     * Defaults are conservative (low realm gate, no dao tie, not unique) so
     * unknown items fall through to the engine's normal logic.
     */
    public static final class ItemMeta {
        public final String requestedItemId;
        public final int realmGate;
        public final String daoCompatibility;
        public final boolean canonicallyTiedToIdentity;
        public final ManifestationGiftSystem.GiftCategory category;
        public final String displayName;

        public ItemMeta(String requestedItemId, int realmGate, String daoCompatibility,
                        boolean canonicallyTiedToIdentity,
                        ManifestationGiftSystem.GiftCategory category,
                        String displayName) {
            this.requestedItemId = requestedItemId;
            this.realmGate = realmGate;
            this.daoCompatibility = daoCompatibility;
            this.canonicallyTiedToIdentity = canonicallyTiedToIdentity;
            this.category = category;
            this.displayName = displayName;
        }
    }

    /**
     * Resolve an item's metadata by searching Wang Lin's gift registry. If
     * no matching gift record exists, returns a default-meta with realmGate=0
     * and category=CANONICAL_TREASURE.
     */
    static ItemMeta resolveItemMeta(String requestedItemId) {
        if (requestedItemId == null || requestedItemId.isEmpty()) {
            return new ItemMeta("", 0, null, false,
                    ManifestationGiftSystem.GiftCategory.CANONICAL_TREASURE, "");
        }
        for (ManifestationGiftSystem.GiftRecord gift :
                ManifestationGiftSystem.getGiftsByProtagonist("wang_lin")) {
            if (requestedItemId.equals(gift.canonOriginId)) {
                return new ItemMeta(
                        requestedItemId,
                        gift.realmGate,
                        gift.daoCompatibility,
                        gift.canonicallyTiedToIdentity,
                        gift.category,
                        gift.name
                );
            }
        }
        return new ItemMeta(requestedItemId, 0, null, false,
                ManifestationGiftSystem.GiftCategory.CANONICAL_TREASURE, requestedItemId);
    }

    // ─── Domain classifiers (for JUDGMENT alignment) ───────────────────

    private static boolean isRestrictionItem(String itemId, ItemMeta meta) {
        if (itemId == null) return false;
        String lower = itemId.toLowerCase();
        return lower.contains("restriction") || lower.contains("seal")
                || lower.contains("formation") || lower.contains("flag")
                || lower.contains("jade") || lower.contains("array")
                || "restriction".equals(meta.daoCompatibility);
    }

    private static boolean isCombatItem(String itemId, ItemMeta meta) {
        if (itemId == null) return false;
        String lower = itemId.toLowerCase();
        return lower.contains("whip") || lower.contains("sword") || lower.contains("blade")
                || lower.contains("spear") || lower.contains("fan") || lower.contains("axe")
                || lower.contains("guard") || lower.contains("puppet");
    }

    private static boolean isBreakthroughCatalyst(String itemId, ItemMeta meta) {
        if (itemId == null) return false;
        String lower = itemId.toLowerCase();
        // Knowledge items — essences, techniques, methods, dao teachings.
        return lower.startsWith("tech_") || lower.contains("essence")
                || lower.contains("method") || lower.contains("dao")
                || lower.contains("manual") || lower.contains("art");
    }

    private static boolean challengeAlignsWithItem(String predictionId, String itemId) {
        if (predictionId == null || itemId == null) return false;
        if (predictionId.contains("mosquito_valley")) {
            return itemId.toLowerCase().contains("soul") || itemId.toLowerCase().contains("blood")
                    || itemId.toLowerCase().contains("karma");
        }
        if (predictionId.contains("restriction_mountain")) {
            return isRestrictionItem(itemId, WangLinReasoningEngine.resolveItemMeta(itemId));
        }
        return false;
    }

    // ─── Realm mapping ─────────────────────────────────────────────────

    /**
     * Map a canonical {@link RealmId} to a {@link PlayerObserverRealm} for
     * challenge-flavor purposes. Mirrors the mapping in PA-5's
     * {@code DivineSenseAtlas.fromRealmId}.
     */
    public static PlayerObserverRealm fromRealmId(RealmId realm) {
        if (realm == null) return PlayerObserverRealm.MORTAL;
        return switch (realm) {
            case MORTAL -> PlayerObserverRealm.MORTAL;
            case QI_CONDENSATION -> PlayerObserverRealm.QI_CONDENSATION;
            case FOUNDATION -> PlayerObserverRealm.FOUNDATION;
            case CORE_FORMATION -> PlayerObserverRealm.CORE_FORMATION;
            case NASCENT_SOUL -> PlayerObserverRealm.NASCENT_SOUL;
            case SOUL_FORMATION, SOUL_TRANSFORMATION -> PlayerObserverRealm.SOUL_FORMATION;
            default -> PlayerObserverRealm.ASCENDANT_PLUS;
        };
    }
}
