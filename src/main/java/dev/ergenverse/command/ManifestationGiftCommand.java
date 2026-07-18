package dev.ergenverse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.simulation.affinity.ManifestationGiftSystem;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ManifestationGiftCommand — /ergen gift [list|request|evaluate|lore]
 *
 * <p>In-game command for interacting with the Manifestation Gift System.
 * Requires permission level 2 (op).
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergen gift list} — Lists all available gifts in a formatted
 *       table grouped by protagonist.</li>
 *   <li>{@code /ergen gift request <giftId>} — Evaluates a gift request
 *       against the player's current cultivation state and shows the
 *       decision + dialogue.</li>
 *   <li>{@code /ergen gift evaluate <giftId>} — Same as request but shows
 *       the four-question breakdown (PASS/FAIL for each question).</li>
 *   <li>{@code /ergen gift lore} — Shows the philosophy statement, summary
 *       counts, and protagonist personality profiles.</li>
 * </ul>
 */
public class ManifestationGiftCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("gift")
                    .executes(ManifestationGiftCommand::listGifts)
                    .then(Commands.literal("list")
                        .executes(ManifestationGiftCommand::listGifts))
                    .then(Commands.literal("request")
                        .then(Commands.argument("giftId", StringArgumentType.string())
                            .executes(ManifestationGiftCommand::requestGift)))
                    .then(Commands.literal("evaluate")
                        .then(Commands.argument("giftId", StringArgumentType.string())
                            .executes(ManifestationGiftCommand::evaluateGift)))
                    .then(Commands.literal("lore")
                        .executes(ManifestationGiftCommand::showLore))
                )
        );
    }

    // ─── Helper ───────────────────────────────────────────────────────

    private static void msg(CommandSourceStack src, MutableComponent comp) {
        src.sendSuccess(() -> comp, false);
    }

    // ─── /ergen gift list ─────────────────────────────────────────────

    private static int listGifts(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        msg(src, Component.literal("=== Manifestation Gift Registry ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        msg(src, Component.literal(""));

        // Group gifts by protagonist
        Map<String, List<ManifestationGiftSystem.GiftRecord>> byProtagonist = new LinkedHashMap<>();
        for (ManifestationGiftSystem.GiftRecord gift : ManifestationGiftSystem.ALL_GIFTS) {
            byProtagonist.computeIfAbsent(gift.protagonistId, k -> new java.util.ArrayList<>())
                    .add(gift);
        }

        for (Map.Entry<String, List<ManifestationGiftSystem.GiftRecord>> entry : byProtagonist.entrySet()) {
            String protagId = entry.getKey();
            List<ManifestationGiftSystem.GiftRecord> gifts = entry.getValue();

            ManifestationGiftSystem.PersonalityProfile profile =
                    ManifestationGiftSystem.getProfile(protagId);
            String headerName = profile != null ? profile.protagonistName : protagId;

            msg(src, Component.literal("── " + headerName + " (" + protagId + ") ──")
                    .withStyle(ChatFormatting.GOLD));

            for (ManifestationGiftSystem.GiftRecord g : gifts) {
                // ID + name + category line
                MutableComponent line = Component.literal("  [" + g.giftId + "]")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(" " + g.name)
                                .withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(" (" + g.category.name() + ")")
                                .withStyle(ChatFormatting.GRAY));
                if (g.canonicallyTiedToIdentity) {
                    line.append(Component.literal(" [Identity-Tied]")
                            .withStyle(ChatFormatting.LIGHT_PURPLE));
                }
                msg(src, line);

                // Details line: affinity threshold, realm gate, dao
                MutableComponent details = Component.literal("    Affinity \u2265 " + g.affinityThreshold)
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(" | Realm \u2265 " + g.realmGate)
                                .withStyle(ChatFormatting.DARK_GRAY));
                if (g.daoCompatibility != null) {
                    details.append(Component.literal(" | Dao: " + g.daoCompatibility)
                            .withStyle(ChatFormatting.DARK_GRAY));
                }
                msg(src, details);
            }

            msg(src, Component.literal(""));
        }

        msg(src, Component.literal("Total: " + ManifestationGiftSystem.ALL_GIFTS.size() + " gifts registered")
                .withStyle(ChatFormatting.GRAY));

        return 1;
    }

    // ─── /ergen gift request <giftId> ─────────────────────────────────

    private static int requestGift(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        var player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String giftId = StringArgumentType.getString(ctx, "giftId");
        ManifestationGiftSystem.GiftRecord gift = ManifestationGiftSystem.getGift(giftId);
        if (gift == null) {
            src.sendFailure(Component.literal("Unknown gift ID: " + giftId));
            return 0;
        }

        // Get cultivation state
        LazyOptional<CultivationState> capOpt = CultivationCapability.get(player);
        if (!capOpt.isPresent()) {
            src.sendFailure(Component.literal("No cultivation state found. You are a mortal."));
            return 0;
        }
        CultivationState state = capOpt.resolve().get();

        // Create v1 PlayerStateSnapshot
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = createSnapshot(state);

        // Evaluate
        ManifestationGiftSystem.GiftDecision decision =
                ManifestationGiftSystem.evaluateGift(gift, snapshot);

        // Show result header
        String protagName = gift.protagonistId;
        ManifestationGiftSystem.PersonalityProfile profile =
                ManifestationGiftSystem.getProfile(gift.protagonistId);
        if (profile != null) protagName = profile.protagonistName;

        msg(src, Component.literal("=== Gift Request ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        msg(src, Component.literal("Gift: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(gift.name).withStyle(ChatFormatting.WHITE)));
        msg(src, Component.literal("From: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(protagName).withStyle(ChatFormatting.GOLD)));

        // Decision — color coded
        ChatFormatting decColor = decisionColor(decision);
        msg(src, Component.literal("Decision: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(decision.name()).withStyle(decColor)));

        // Dialogue
        String dialogue = ManifestationGiftSystem.getDialogueFor(gift, decision);
        msg(src, Component.literal("")
                .append(Component.literal(dialogue).withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW)));

        // If OFFERED and has canonOriginId, hint about item
        if (decision == ManifestationGiftSystem.GiftDecision.OFFERED && gift.canonOriginId != null) {
            boolean itemExists = ForgeRegistries.ITEMS
                    .getValue(new net.minecraft.resources.ResourceLocation("ergenverse", gift.canonOriginId)) != null;
            if (itemExists) {
                msg(src, Component.literal("")
                        .append(Component.literal("[!] This gift corresponds to a registered item — "
                                + "the manifestation could grant it to you.")
                                .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)));
            }
        }

        return 1;
    }

    // ─── /ergen gift evaluate <giftId> ────────────────────────────────

    private static int evaluateGift(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        var player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String giftId = StringArgumentType.getString(ctx, "giftId");
        ManifestationGiftSystem.GiftRecord gift = ManifestationGiftSystem.getGift(giftId);
        if (gift == null) {
            src.sendFailure(Component.literal("Unknown gift ID: " + giftId));
            return 0;
        }

        // Get cultivation state
        LazyOptional<CultivationState> capOpt = CultivationCapability.get(player);
        if (!capOpt.isPresent()) {
            src.sendFailure(Component.literal("No cultivation state found. You are a mortal."));
            return 0;
        }
        CultivationState state = capOpt.resolve().get();

        // Create snapshot
        ManifestationGiftSystem.PlayerStateSnapshot snapshot = createSnapshot(state);

        // Show header
        String protagName = gift.protagonistId;
        ManifestationGiftSystem.PersonalityProfile profile =
                ManifestationGiftSystem.getProfile(gift.protagonistId);
        if (profile != null) protagName = profile.protagonistName;

        msg(src, Component.literal("=== Gift Evaluation Breakdown ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        msg(src, Component.literal("Gift: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(gift.name).withStyle(ChatFormatting.WHITE)));
        msg(src, Component.literal("From: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(protagName).withStyle(ChatFormatting.GOLD)));
        msg(src, Component.literal(""));

        // ── Prerequisite check: affinity threshold ──
        int affinity = snapshot.getAffinity(gift.protagonistId);
        boolean preReqPass = affinity >= gift.affinityThreshold;
        msg(src, Component.literal("[PREREQ] Affinity ≥ " + gift.affinityThreshold
                + " (yours: " + affinity + ")")
                .withStyle(preReqPass ? ChatFormatting.GREEN : ChatFormatting.RED));
        if (!preReqPass) {
            msg(src, Component.literal("Result: ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal("REFUSED_NOT_TRUSTED (affinity too low)")
                            .withStyle(ChatFormatting.RED)));
            return 1;
        }

        // ── Q1: Still needed? ──
        // Identity-tied items always pass Q1 (exact copy)
        boolean q1Pass = gift.canonicallyTiedToIdentity || !gift.category.isCanonical();
        String q1Note = gift.canonicallyTiedToIdentity
                ? " (identity-tied → exact copy, always PASS)"
                : " (post-canon, not tied to identity → PASS)";
        msg(src, Component.literal("[Q1] Still needed?")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" " + (q1Pass ? "\u2713 PASS" : "\u2717 FAIL") + q1Note)
                        .withStyle(q1Pass ? ChatFormatting.GREEN : ChatFormatting.RED)));

        // ── Q2: Trust check ──
        int trustScore = affinity;
        if (profile == ManifestationGiftSystem.XU_QING) {
            int observedTrust = snapshot.getObservedActionTrust(gift.protagonistId);
            trustScore = Math.max(affinity, observedTrust);
        }
        int trustThreshold = gift.affinityThreshold + (profile != null && profile.q2Weight > 1 ? 15 : 0);
        boolean q2Pass = trustScore >= trustThreshold;
        msg(src, Component.literal("[Q2] Trusted ally?")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" " + (q2Pass ? "\u2713 PASS" : "\u2717 FAIL")
                        + " (trust: " + trustScore + " ≥ " + trustThreshold + ")")
                        .withStyle(q2Pass ? ChatFormatting.GREEN : ChatFormatting.RED)));

        // ── Q3: Realm / Dao check ──
        int realmTier = snapshot.getRealmTier();
        boolean realmOk = realmTier >= gift.realmGate;
        boolean daoOk = gift.daoCompatibility == null || snapshot.hasDao(gift.daoCompatibility);
        boolean q3Pass = realmOk && daoOk;
        StringBuilder q3Detail = new StringBuilder(" ");
        q3Detail.append(q3Pass ? "\u2713 PASS" : "\u2717 FAIL");
        q3Detail.append(" (realm ").append(realmTier).append(realmOk ? "≥" : "<")
                .append(gift.realmGate);
        if (gift.daoCompatibility != null) {
            q3Detail.append(", dao ").append(gift.daoCompatibility)
                    .append(daoOk ? " ✓" : " ✗");
        }
        q3Detail.append(")");
        msg(src, Component.literal("[Q3] Realm / Dao ready?")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(q3Detail.toString())
                        .withStyle(q3Pass ? ChatFormatting.GREEN : ChatFormatting.RED)));

        // ── Q4: Personality filter ──
        boolean q4Bypass = profile != null && profile.q4Weight == 0;
        boolean q4Bargain = profile != null && (profile.bargainBehavior == ManifestationGiftSystem.BargainBehavior.FAIR
                || profile.bargainBehavior == ManifestationGiftSystem.BargainBehavior.PREDATORY);
        boolean q4Comprehension = profile != null && profile.bargainBehavior == ManifestationGiftSystem.BargainBehavior.COMPREHENSION_TEST;
        boolean q4Pass = q4Bypass || !q4Bargain && !q4Comprehension;
        String q4Note;
        if (q4Bypass) {
            q4Note = " (personality bypasses filter)";
        } else if (q4Bargain) {
            q4Note = " → REDIRECTED_TO_BARGAIN";
        } else if (q4Comprehension) {
            q4Note = " → REDIRECTED_TO_COMPREHENSION";
        } else {
            q4Note = " (straightforward personality)";
        }
        msg(src, Component.literal("[Q4] Fits personality?")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" " + (q4Pass ? "\u2713 PASS" : "\u2717 FAIL") + q4Note)
                        .withStyle(q4Bargain || q4Comprehension ? ChatFormatting.YELLOW : (q4Pass ? ChatFormatting.GREEN : ChatFormatting.RED))));

        msg(src, Component.literal(""));

        // ── Final decision ──
        ManifestationGiftSystem.GiftDecision decision =
                ManifestationGiftSystem.evaluateGift(gift, snapshot);
        ChatFormatting decColor = decisionColor(decision);
        msg(src, Component.literal("Final Decision: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(decision.name()).withStyle(decColor, ChatFormatting.BOLD)));

        // Dialogue
        String dialogue = ManifestationGiftSystem.getDialogueFor(gift, decision);
        msg(src, Component.literal(dialogue)
                .withStyle(ChatFormatting.ITALIC, ChatFormatting.YELLOW));

        return 1;
    }

    // ─── /ergen gift lore ─────────────────────────────────────────────

    private static int showLore(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();

        msg(src, Component.literal("=== Manifestation Gift Lore ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        msg(src, Component.literal(""));

        // Philosophy
        msg(src, Component.literal("Core Philosophy:")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        msg(src, Component.literal(ManifestationGiftSystem.PHILOSOPHY_STATEMENT)
                .withStyle(ChatFormatting.WHITE, ChatFormatting.ITALIC));
        msg(src, Component.literal(""));

        // Summary counts
        msg(src, Component.literal("Registry Summary:")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        Map<String, Integer> counts = ManifestationGiftSystem.getSummaryCounts();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            msg(src, Component.literal("  " + entry.getKey() + ": ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(String.valueOf(entry.getValue()))
                            .withStyle(ChatFormatting.WHITE)));
        }
        msg(src, Component.literal(""));

        // Protagonist profiles
        msg(src, Component.literal("Protagonist Personality Profiles:")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        for (ManifestationGiftSystem.PersonalityProfile p : ManifestationGiftSystem.ALL_PROFILES) {
            msg(src, Component.literal("  " + p.protagonistName + " (" + p.protagonistNameCn + ")")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" — " + p.novel).withStyle(ChatFormatting.GRAY)));

            msg(src, Component.literal("    Volunteer Rate: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(String.format("%.0f%%", p.volunteerRate * 100))
                            .withStyle(ChatFormatting.WHITE)));
            msg(src, Component.literal("    Bargain Behavior: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(p.bargainBehavior.name())
                            .withStyle(ChatFormatting.YELLOW)));
            msg(src, Component.literal("    Q Weights: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("Q1=" + p.q1Weight + " Q2=" + p.q2Weight
                            + " Q3=" + p.q3Weight + " Q4=" + p.q4Weight)
                            .withStyle(ChatFormatting.WHITE)));
            msg(src, Component.literal("    Offer Line: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(p.signatureOfferLine)
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.ITALIC)));
            msg(src, Component.literal(""));
        }

        return 1;
    }

    // ─── PlayerStateSnapshot factory (v1) ─────────────────────────────

    /**
     * Creates a v1 PlayerStateSnapshot from the player's CultivationState.
     *
     * <p>v1 affinity model:
     * <ul>
     *   <li>wang_lin → 50 (mid affinity)</li>
     *   <li>all others → 20</li>
     * </ul>
     * Trust is the same as affinity for v1.
     */
    private static ManifestationGiftSystem.PlayerStateSnapshot createSnapshot(CultivationState state) {
        return new ManifestationGiftSystem.PlayerStateSnapshot() {
            @Override
            public int getAffinity(String protagonistId) {
                // v1: fixed affinity values
                return "wang_lin".equals(protagonistId) ? 50 : 20;
            }

            @Override
            public int getRealmTier() {
                return state.getCurrentRealm().order;
            }

            @Override
            public boolean hasDao(String daoId) {
                Map<String, Double> daoMap = state.getDaoComprehension();
                Double val = daoMap.get(daoId);
                return val != null && val > 0.1;
            }

            @Override
            public int getObservedActionTrust(String protagonistId) {
                // v1: same as affinity
                return getAffinity(protagonistId);
            }
        };
    }

    // ─── Utility ──────────────────────────────────────────────────────

    private static ChatFormatting decisionColor(ManifestationGiftSystem.GiftDecision decision) {
        if (decision == ManifestationGiftSystem.GiftDecision.OFFERED) {
            return ChatFormatting.GREEN;
        }
        if (decision.name().startsWith("REFUSED_")) {
            return ChatFormatting.RED;
        }
        if (decision.name().startsWith("REDIRECTED_")) {
            return ChatFormatting.YELLOW;
        }
        return ChatFormatting.WHITE;
    }
}