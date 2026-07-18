package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;
import dev.ergenverse.wanglin.ai.reasoning.GiftingDecision;
import dev.ergenverse.wanglin.ai.reasoning.GiftingFactor;
import dev.ergenverse.wanglin.ai.reasoning.GiftingOutcome;
import dev.ergenverse.wanglin.ai.reasoning.GiftingResponse;
import dev.ergenverse.wanglin.ai.reasoning.ReasoningIntegrationBridge;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * WangLinAskCommand — the player-facing entry point for Wang Lin's reasoning-based
 * gifting system (PROJECT_MASTER.md §4.4).
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li><b>/ergen ask &lt;itemId&gt;</b> — Request a specific item from Wang Lin.
 *       The 6-factor reasoning engine evaluates the request and responds with
 *       YES / NO_FOR_NOW / CHALLENGE.</li>
 *   <li><b>/ergen ask recommend</b> — Ask Wang Lin to give what you need most.
 *       Iterates all known Wang Lin gifts, evaluates each with the reasoning engine,
 *       and offers the one with the highest aggregateScore that returns YES. If
 *       none returns YES, sends the dialogue from the highest-scoring response.</li>
 *   <li><b>/ergen ask list</b> — Lists all known Wang Lin gift item IDs that can
 *       be requested (for tab-completion reference).</li>
 * </ul>
 *
 * <h2>Canon faithfulness (§4.4)</h2>
 * <ul>
 *   <li>The player can ALWAYS ask. Wang Lin NEVER gets mad. Asking is always safe.</li>
 *   <li>Hoarding correction: even if the player hoards, Wang Lin looks for what
 *       they need most and gives that. Hoarding NEVER causes punitive withholding.</li>
 *   <li>Wang Lin is mentor/observer/judge, NOT a content dispenser. Low-score
 *       requests return NO_FOR_NOW (aggregateScore &lt; 0.3) — never an automatic grant.</li>
 * </ul>
 *
 * <h2>Integration</h2>
 * <p>This command SUPERSEDES the old {@code /ergen gift request} command (which uses
 * the deprecated affinity-tier ManifestationGiftSystem). The old command remains
 * functional for backward compatibility; this is the canon-faithful path per §4.4.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WangLinAskCommand {

    private WangLinAskCommand() {}

    /** Suggestion provider that lists all Wang Lin gift item IDs for tab-completion. */
    private static final SuggestionProvider<CommandSourceStack> GIFT_ID_SUGGESTIONS =
            (ctx, builder) -> SharedSuggestionProvider.suggest(getAllWangLinGiftIds(), builder);

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> disp = event.getDispatcher();

        disp.register(Commands.literal("ergen")
                .then(Commands.literal("ask")
                        // /ergen ask recommend
                        .then(Commands.literal("recommend")
                                .executes(WangLinAskCommand::askRecommend))
                        // /ergen ask list
                        .then(Commands.literal("list")
                                .executes(WangLinAskCommand::askList))
                        // /ergen ask <itemId>
                        .then(Commands.argument("itemId", StringArgumentType.string())
                                .suggests(GIFT_ID_SUGGESTIONS)
                                .executes(WangLinAskCommand::askSpecific))
                )
        );
    }

    // ─── /ergen ask <itemId> ──────────────────────────────────────────

    private static int askSpecific(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        String itemId = StringArgumentType.getString(ctx, "itemId");

        GiftingResponse response = ReasoningIntegrationBridge.processGiftRequest(player, itemId);
        sendResponseToPlayer(player, response, itemId);
        return 1;
    }

    // ─── /ergen ask recommend ─────────────────────────────────────────

    /**
     * "Wang Lin, give me what I need most."
     *
     * <p>Per the user's hoarding correction: even if the player hoards, Wang Lin
     * looks for what they need most and gives that. This command implements that
     * directive by iterating all known Wang Lin gifts, evaluating each with the
     * reasoning engine, and offering the one with the highest aggregateScore
     * that returns YES.
     *
     * <p>If no gift returns YES, sends the dialogue from the highest-scoring
     * response (which will be NO_FOR_NOW or CHALLENGE) so the player understands
     * Wang Lin's judgment.
     */
    private static int askRecommend(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        List<String> giftIds = getAllWangLinGiftIds();
        if (giftIds.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77Wang Lin's manifestation watches you in silence. " +
                    "He has nothing to offer you yet.")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        // Evaluate each gift and track the best response
        GiftingResponse bestResponse = null;
        String bestItemId = null;
        double bestScore = -1.0;

        // First pass: find any YES outcome (prefer highest score among YES)
        for (String itemId : giftIds) {
            GiftingResponse response = ReasoningIntegrationBridge.processGiftRequest(player, itemId);
            if (response == null) continue;

            if (response.outcome == GiftingOutcome.YES && response.aggregateScore > bestScore) {
                bestResponse = response;
                bestItemId = itemId;
                bestScore = response.aggregateScore;
            }
        }

        // If no YES, find the highest-scoring non-YES response (NO_FOR_NOW or CHALLENGE)
        // to show Wang Lin's judgment. This is the "look for what I need most" behavior —
        // even if he can't give right now, he communicates his reasoning.
        if (bestResponse == null) {
            bestScore = -1.0;
            for (String itemId : giftIds) {
                GiftingResponse response = ReasoningIntegrationBridge.processGiftRequest(player, itemId);
                if (response == null) continue;
                if (response.aggregateScore > bestScore) {
                    bestResponse = response;
                    bestItemId = itemId;
                    bestScore = response.aggregateScore;
                }
            }
        }

        if (bestResponse == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77Wang Lin considers your request, but says nothing.")
                    .withStyle(ChatFormatting.GRAY));
            return 0;
        }

        // Header: explain this was a "recommend" evaluation
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "\u2726 You ask Wang Lin for what you need most. \u2726")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));

        sendResponseToPlayer(player, bestResponse, bestItemId);
        return 1;
    }

    // ─── /ergen ask list ──────────────────────────────────────────────

    private static int askList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<String> giftIds = getAllWangLinGiftIds();

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal(
                "\u2726 Wang Lin's Known Gifts \u2726")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        player.sendSystemMessage(Component.literal(
                "Use /ergen ask <itemId> to request, or /ergen ask recommend for what you need most.")
                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));

        if (giftIds.isEmpty()) {
            player.sendSystemMessage(Component.literal(
                    "  (no gifts registered yet)")
                    .withStyle(ChatFormatting.DARK_GRAY));
            return 0;
        }

        for (String id : giftIds) {
            player.sendSystemMessage(Component.literal("  \u00A7e" + id)
                    .withStyle(ChatFormatting.YELLOW));
        }

        player.sendSystemMessage(Component.literal(
                "Total: " + giftIds.size() + " gifts")
                .withStyle(ChatFormatting.AQUA));
        return 1;
    }

    // ─── Response formatting ──────────────────────────────────────────

    /**
     * Sends the GiftingResponse to the player as formatted chat messages.
     * Shows Wang Lin's dialogue, the outcome, and (for transparency in v1)
     * the reasoning factors that drove the decision.
     */
    private static void sendResponseToPlayer(ServerPlayer player, GiftingResponse response,
                                              String requestedItemId) {
        if (response == null) {
            player.sendSystemMessage(Component.literal(
                    "\u00A77Wang Lin's manifestation is silent. (Internal error evaluating request.)")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        // Wang Lin's dialogue line (always shown)
        player.sendSystemMessage(Component.literal(""));
        MutableComponent header = Component.literal("\u00A7d\u00A7lWang Lin's manifestation: \u00A7r");
        player.sendSystemMessage(header);

        if (response.wangLinDialogue != null && !response.wangLinDialogue.isEmpty()) {
            player.sendSystemMessage(Component.literal("  \u00A77\"" + response.wangLinDialogue + "\"")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        // Outcome-specific messaging
        switch (response.outcome) {
            case YES:
                player.sendSystemMessage(Component.literal(
                        "  \u00A7a\u00A7l[Wang Lin grants your request.]")
                        .withStyle(ChatFormatting.GREEN));
                if (response.itemGrantedId != null) {
                    player.sendSystemMessage(Component.literal(
                            "  \u00A7eReceived: " + response.itemGrantedId + " (exact replica from true body)")
                            .withStyle(ChatFormatting.YELLOW));
                }
                break;
            case NO_FOR_NOW:
                player.sendSystemMessage(Component.literal(
                        "  \u00A7c[He shakes his head — not yet.]")
                        .withStyle(ChatFormatting.RED));
                player.sendSystemMessage(Component.literal(
                        "  \u00A77You may ask again later. He is never angry — only honest.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                break;
            case CHALLENGE:
                player.sendSystemMessage(Component.literal(
                        "  \u00A76\u00A7l[He offers you a challenge instead.]")
                        .withStyle(ChatFormatting.GOLD));
                if (response.challengeDescription != null) {
                    player.sendSystemMessage(Component.literal(
                            "  \u00A76Challenge: " + response.challengeDescription)
                            .withStyle(ChatFormatting.GOLD));
                }
                player.sendSystemMessage(Component.literal(
                        "  \u00A77Complete this challenge, and what you asked for will be yours.")
                        .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                break;
        }

        // Show the reasoning factors (v1 transparency — in v2 this could be hidden
        // and only revealed through high-affinity dialogue with Wang Lin)
        if (response.decisions != null && !response.decisions.isEmpty()) {
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(Component.literal(
                    "  \u00A78\u00A7oInternal monologue (Wang Lin's reasoning):")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            List<GiftingDecision> sorted = new ArrayList<>(response.decisions);
            sorted.sort(Comparator.comparingDouble(d -> -d.score));
            for (GiftingDecision d : sorted) {
                String color;
                if (d.score >= 0.7) color = "\u00A7a";
                else if (d.score >= 0.3) color = "\u00A7e";
                else color = "\u00A7c";
                player.sendSystemMessage(Component.literal(
                        "    " + color + d.factor.displayName + ": " + String.format("%.0f%%", d.score * 100))
                        .withStyle(ChatFormatting.GRAY));
                if (d.reasoning != null && !d.reasoning.isEmpty()) {
                    player.sendSystemMessage(Component.literal(
                            "      \u00A77" + d.reasoning)
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
            player.sendSystemMessage(Component.literal(
                    "  \u00A7fAggregate: " + String.format("%.0f%%", response.aggregateScore * 100))
                    .withStyle(ChatFormatting.WHITE));
            // Identify the dominant factor
            GiftingFactor dominant = response.dominantFactor();
            if (dominant != null) {
                player.sendSystemMessage(Component.literal(
                        "  \u00A7dDominant factor: " + dominant.displayName)
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
        }
    }

    // ─── Gift ID enumeration ──────────────────────────────────────────

    /**
     * Returns all known Wang Lin gift item IDs (canonOriginId from each GiftRecord).
     * Used for tab-completion and the /ergen ask list command.
     */
    private static List<String> getAllWangLinGiftIds() {
        List<String> ids = new ArrayList<>();
        try {
            List<ManifestationGiftSystem.GiftRecord> gifts =
                    ManifestationGiftSystem.getGiftsByProtagonist("wang_lin");
            if (gifts != null) {
                for (ManifestationGiftSystem.GiftRecord g : gifts) {
                    if (g.canonOriginId != null && !g.canonOriginId.isEmpty()) {
                        ids.add(g.canonOriginId);
                    }
                }
            }
        } catch (Exception e) {
            Ergenverse.LOGGER.warn("[WangLinAskCommand] Failed to enumerate Wang Lin gifts", e);
        }
        return ids;
    }
}
