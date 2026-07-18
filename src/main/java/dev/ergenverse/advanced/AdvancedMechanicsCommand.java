package dev.ergenverse.advanced;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.wanglin.CaveWorldOwnership;
import dev.ergenverse.wanglin.JossFlameEconomy;
import dev.ergenverse.wanglin.RealmSealingGrandArray;
import dev.ergenverse.wanglin.SamsaraDao;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * /ergen advanced — inspect and manipulate the 4 advanced mechanics systems.
 *
 * <p>Subcommands:
 * <ul>
 *   <li><b>joss</b> — Joss Flame economy status</li>
 *   <li><b>cave</b> — Cave World ownership status</li>
 *   <li><b>seal</b> — Realm-Sealing Grand Array status</li>
 *   <li><b>essence</b> — Samsara Dao / 14 Essences status</li>
 *   <li><b>samsara</b> — Heaven Trampling check + resonance info</li>
 *   <li><b>comprehend</b> [1-14] — (debug) comprehend an Essence</li>
 *   <li><b>dissolve</b> — (debug) dissolve the Realm-Sealing Grand Array</li>
 *   <li><b>transfer</b> — (debug) transfer Cave World ownership to player</li>
 * </ul>
 */
public class AdvancedMechanicsCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("ergen")
                .then(Commands.literal("advanced")
                    // ── joss: Joss Flame economy ──
                    .then(Commands.literal("joss")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.sendSystemMessage(Component.literal(
                                AdvancedMechanicsEvents.getJossStatus(player)));
                            return 1;
                        })
                    )
                    // ── cave: Cave World ownership ──
                    .then(Commands.literal("cave")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.sendSystemMessage(Component.literal(
                                AdvancedMechanicsEvents.getCaveWorldStatus()));
                            return 1;
                        })
                    )
                    // ── seal: Realm-Sealing Grand Array ──
                    .then(Commands.literal("seal")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.sendSystemMessage(Component.literal(
                                AdvancedMechanicsEvents.getSealStatus()));
                            return 1;
                        })
                        // dissolve subcommand (debug)
                        .then(Commands.literal("dissolve")
                            .requires(src -> src.hasPermission(2))
                            .executes(ctx -> {
                                if (!RealmSealingGrandArray.isArrayActive()) {
                                    ctx.getSource().sendSuccess(
                                        () -> Component.literal("\u00A77The array is already dissolved."), false);
                                    return 1;
                                }
                                RealmSealingGrandArray.dissolve();
                                ctx.getSource().sendSuccess(
                                    () -> Component.literal(
                                        "\u00A7c\u00A7lThe Realm-Sealing Grand Array shatters!\u00A7r\n" +
                                        "\u00A7aThe Heaven-Splitting Axe's spirit fades. " +
                                        "The seal that suppressed cultivators for millennia is gone.\n" +
                                        "\u00A77Cultivation ceilings across the Sealed Realm are lifted."),
                                    true);
                                Ergenverse.LOGGER.info("[Ergenverse] Realm-Sealing Grand Array dissolved by command.");
                                return 1;
                            })
                        )
                    )
                    // ── essence: Samsara Dao / 14 Essences ──
                    .then(Commands.literal("essence")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            player.sendSystemMessage(Component.literal(
                                AdvancedMechanicsEvents.getSamsaraStatus(player)));
                            return 1;
                        })
                        // comprehend subcommand (debug: grant essence comprehension)
                        .then(Commands.literal("comprehend")
                            .requires(src -> src.hasPermission(2))
                            .then(Commands.argument("order", IntegerArgumentType.integer(1, 14))
                                .executes(ctx -> {
                                    int order = IntegerArgumentType.getInteger(ctx, "order");
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    CultivationState state = CultivationCapability.getOrThrow(player);
                                    state.comprehendEssence(order);
                                    SamsaraDao.Essence e = SamsaraDao.Essence.values()[order - 1];
                                    ctx.getSource().sendSuccess(
                                        () -> Component.literal(String.format(
                                            "\u00A7a\u00A7l\u2728 Essence Comprehended!\u00A7r\n" +
                                            "\u00A7f%d. %s \u00A77(%s)\n\u00A78%s\n" +
                                            "\u00A77Progress: \u00A7e%d/14\u00A7r",
                                            e.taxonomyOrder, e.name, e.category.name(),
                                            e.description, countEssences(state))),
                                        true);

                                    // Check Heaven Trampling
                                    if (SamsaraDao.isHeavenTramplingAchieved(state.getEssencesComprehended())) {
                                        player.sendSystemMessage(Component.literal(
                                            "\u00A7a\u00A7l\u2728\u2728 HEAVEN TRAMPLING ACHIEVED! \u00A7r\n" +
                                            "\u00A7fAll 14 Essences comprehended. You have transcended " +
                                            "the bridge system. The 9th Bridge was never needed — " +
                                            "comprehension of Reincarnation Essence is the true path."));
                                        dev.ergenverse.history.HistoryManager.onDiscovery(player,
                                                "heaven_trampling",
                                                "Achieved Heaven Trampling by comprehending all 14 Essences.",
                                                player.level().getGameTime());
                                    }

                                    Ergenverse.LOGGER.info("[Ergenverse] {} comprehended Essence {} ({})",
                                            player.getName().getString(), order, e.name);
                                    return 1;
                                })
                            )
                        )
                    )
                    // ── samsara: Heaven Trampling check + avatar gameplay ──
                    .then(Commands.literal("samsara")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            CultivationState state = CultivationCapability.getOrThrow(player);
                            boolean[] essences = state.getEssencesComprehended();
                            boolean achieved = SamsaraDao.isHeavenTramplingAchieved(essences);
                            int count = countEssences(state);

                            player.sendSystemMessage(Component.literal(
                                "\u00A75\u00A7lSamsara Dao Status\u00A7r"));
                            player.sendSystemMessage(Component.literal(String.format(
                                "  Essences: \u00A7e%d/14\u00A7r | Heaven Trampling: %s",
                                count,
                                achieved ? "\u00A7a\u00A7lACHIEVED" : "\u00A77not yet")));

                            if (!achieved) {
                                // Show next uncomprehended essence in Wang Lin's order
                                for (SamsaraDao.Essence e : SamsaraDao.WANG_LIN_COMPLETION_ORDER) {
                                    if (!essences[e.taxonomyOrder - 1]) {
                                        player.sendSystemMessage(Component.literal(String.format(
                                            "  \u00A78Next in Wang Lin's order: \u00A7f%d. %s \u00A77(%s)",
                                            e.taxonomyOrder, e.name, e.nameCn)));
                                        break;
                                    }
                                }
                            }

                            // Show Samsara track eligibility
                            boolean onTrack = SamsaraDao.isOnSamsaraTrack(
                                    state.hasHeavenDefyingBead(player.getInventory()), false);
                            player.sendSystemMessage(Component.literal(String.format(
                                "  Samsara Track: %s",
                                onTrack ? "\u00A7aEligible (soul vessel detected)" : "\u00A77Requires Heaven-Defying Bead or soul vessel artifact")));

                            // Show incarnation count
                            int active = state.countActiveIncarnations();
                            int reabsorbed = state.getSamsaraIncarnations().size() - active;
                            player.sendSystemMessage(Component.literal(String.format(
                                "  Incarnations: \u00A7a%d active\u00A7r | \u00A77%d reabsorbed",
                                active, reabsorbed)));

                            // Show resonance info
                            player.sendSystemMessage(Component.literal(
                                "  \u00A78Resonance: comprehending resonant Essence pairs grants deeper insight."));
                            player.sendSystemMessage(Component.literal(
                                "  \u00A78Use \u00A7f/ergen advanced samsara avatar <summon|merge|status>\u00A78 for incarnation gameplay."));
                            return 1;
                        })
                        // ── avatar: Samsara Incarnation gameplay ──
                        .then(Commands.literal("avatar")
                            // summon: create a new incarnation
                            .then(Commands.literal("summon")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    CultivationState state = CultivationCapability.getOrThrow(player);

                                    // Check for Heaven-Defying Bead (soul vessel)
                                    if (!state.hasHeavenDefyingBead(player.getInventory())) {
                                        player.sendSystemMessage(Component.literal(
                                            "\u00A7c\u00A7lCannot summon incarnation.\u00A7r\n" +
                                            "\u00A77You need the Heaven-Defying Bead (or a soul-vessel artifact) to act as a soul anchor.\n" +
                                            "\u00A78Canon: Wang Lin used the Bead to split his soul into a billion incarnations."));
                                        return 0;
                                    }

                                    // Check Qi cost (10% of current absolute Qi, min 1.0)
                                    double currentAbs = state.getAbsoluteQi();
                                    if (currentAbs < 1.0) {
                                        player.sendSystemMessage(Component.literal(
                                            "\u00A7cInsufficient Qi to split a soul fragment.\u00A7r\n" +
                                            "\u00A77You need at least 1.0 absolute Qi. Current: \u00A7e" +
                                                String.format("%.2f", currentAbs)));
                                        return 0;
                                    }

                                    // Summon
                                    SamsaraDao.SamsaraIncarnation inc = state.summonIncarnation(player.getInventory());
                                    if (inc == null) {
                                        player.sendSystemMessage(Component.literal(
                                            "\u00A7cIncarnation summoning failed."));
                                        return 0;
                                    }

                                    player.sendSystemMessage(Component.literal(
                                        "\u00A75\u00A7l\u2728 Samsara Incarnation Summoned!\u00A7r"));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  \u00A7fID:\u00A7r %s", inc.id)));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  \u00A7fContext:\u00A7r %s", inc.contextWorld)));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  \u00A7fIdentity:\u00A7r a %s", inc.incarnationIdentity)));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  \u00A7fLifespan:\u00A7r %d years", inc.lifespanYears)));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  \u00A7fQi cost:\u00A7r %.2f (10%% of current)",
                                        currentAbs * 0.10)));
                                    player.sendSystemMessage(Component.literal(
                                        "  \u00A78The fragment lives its own life. Reabsorb later with " +
                                        "\u00A7f/ergen advanced samsara avatar merge " + inc.id + "\u00A78."));

                                    Ergenverse.LOGGER.info("[Ergenverse] {} summoned Samsara incarnation {} ({}).",
                                            player.getName().getString(), inc.id, inc.contextWorld);
                                    return 1;
                                })
                            )
                            // merge: reabsorb an incarnation by ID
                            .then(Commands.literal("merge")
                                .then(Commands.argument("id", StringArgumentType.string())
                                    .suggests(INCARNATION_SUGGESTIONS)
                                    .executes(ctx -> {
                                        ServerPlayer player = ctx.getSource().getPlayerOrException();
                                        CultivationState state = CultivationCapability.getOrThrow(player);
                                        String id = StringArgumentType.getString(ctx, "id");

                                        SamsaraDao.SamsaraIncarnation inc = state.getIncarnation(id);
                                        if (inc == null) {
                                            player.sendSystemMessage(Component.literal(
                                                "\u00A7cNo incarnation with ID '" + id + "'.\u00A7r\n" +
                                                "\u00A77Use \u00A7f/ergen advanced samsara avatar status\u00A77 to list your incarnations."));
                                            return 0;
                                        }
                                        if (inc.reabsorbed) {
                                            player.sendSystemMessage(Component.literal(
                                                "\u00A7cIncarnation '" + id + "' is already reabsorbed.\u00A7r\n" +
                                                "\u00A77Insight gained: \u00A7f" + inc.daoInsightGained));
                                            return 0;
                                        }

                                        SamsaraDao.Essence granted = state.mergeIncarnation(id);
                                        player.sendSystemMessage(Component.literal(
                                            "\u00A75\u00A7l\u2728 Samsara Incarnation Reabsorbed!\u00A7r"));
                                        player.sendSystemMessage(Component.literal(String.format(
                                            "  \u00A7fIncarnation:\u00A7r a %s (lived %d yrs in %s)",
                                            inc.incarnationIdentity, inc.lifespanYears, inc.contextWorld)));
                                        if (granted != null) {
                                            player.sendSystemMessage(Component.literal(String.format(
                                                "  \u00A7a\u00A7lEssence Comprehended:\u00A7r %s \u00A77(%s)\n\u00A78%s",
                                                granted.name, granted.nameCn, granted.description)));
                                            int ess = countEssences(state);
                                            player.sendSystemMessage(Component.literal(String.format(
                                                "  \u00A7eProgress: %d/14\u00A7r", ess)));
                                            if (SamsaraDao.isHeavenTramplingAchieved(state.getEssencesComprehended())) {
                                                player.sendSystemMessage(Component.literal(
                                                    "\u00A7a\u00A7l\u2728\u2728 HEAVEN TRAMPLING ACHIEVED! \u00A7r\n" +
                                                    "\u00A7fAll 14 Essences comprehended. You have transcended the bridge system."));
                                                dev.ergenverse.history.HistoryManager.onDiscovery(player,
                                                        "heaven_trampling",
                                                        "Achieved Heaven Trampling by comprehending all 14 Essences via Samsara Incarnation reabsorption.",
                                                        player.level().getGameTime());
                                            }
                                        } else {
                                            player.sendSystemMessage(Component.literal(
                                                "  \u00A7aAll 14 Essences already comprehended. The lifetime became universal Dao clarity."));
                                        }

                                        Ergenverse.LOGGER.info("[Ergenverse] {} reabsorbed Samsara incarnation {} (granted: {})",
                                                player.getName().getString(), id,
                                                granted != null ? granted.name : "universal insight");
                                        return 1;
                                    })
                                )
                            )
                            // status: list all incarnations
                            .then(Commands.literal("status")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    CultivationState state = CultivationCapability.getOrThrow(player);
                                    var all = state.getSamsaraIncarnations();
                                    int active = state.countActiveIncarnations();
                                    int reabsorbed = all.size() - active;

                                    player.sendSystemMessage(Component.literal(
                                        "\u00A75\u00A7lSamsara Incarnations\u00A7r"));
                                    player.sendSystemMessage(Component.literal(String.format(
                                        "  Active: \u00A7a%d\u00A7r | Reabsorbed: \u00A77%d\u00A7r | Total: \u00A7f%d",
                                        active, reabsorbed, all.size())));

                                    if (all.isEmpty()) {
                                        player.sendSystemMessage(Component.literal(
                                            "  \u00A78None yet. Use \u00A7f/ergen advanced samsara avatar summon\u00A78 to create one."));
                                    } else {
                                        for (var inc : all) {
                                            String status = inc.reabsorbed
                                                    ? "\u00A77[reabsorbed]"
                                                    : "\u00A7a[active]";
                                            String insight = inc.daoInsightGained != null
                                                    ? " \u00A78\u2192 " + inc.daoInsightGained
                                                    : "";
                                            player.sendSystemMessage(Component.literal(String.format(
                                                "  %s \u00A7f%s\u00A7r: a %s, %d yrs in %s%s",
                                                status, inc.id, inc.incarnationIdentity,
                                                inc.lifespanYears, inc.contextWorld, insight)));
                                        }
                                    }
                                    return 1;
                                })
                            )
                        )
                    )
                    // ── transfer: Cave World ownership transfer (debug) ──
                    .then(Commands.literal("transfer")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            CultivationState state = CultivationCapability.getOrThrow(player);

                            // Dissolve the seal first (killing the owner)
                            if (RealmSealingGrandArray.isArrayActive()) {
                                RealmSealingGrandArray.dissolve();
                            }

                            CaveWorldOwnership.Ownership newOwnership =
                                CaveWorldOwnership.transferOwnership(
                                    "cave_world",
                                    player.getUUID().toString(),
                                    player.getName().getString(),
                                    state.getCurrentRealm().name().toLowerCase().replace(" ", "_"),
                                    player.serverLevel()); // persist to world save

                            ctx.getSource().sendSuccess(
                                () -> Component.literal(String.format(
                                    "\u00A7c\u00A7lCAVE WORLD OWNERSHIP TRANSFERRED!\u00A7r\n" +
                                    "\u00A7aYou are now the \u00A7f%s\u00A7a.\n" +
                                    "\u00A77The seal is dissolved. The cultivation ceiling is lifted.\n" +
                                    "\u00A78Joss Flame harvest now flows to YOU.\n" +
                                    "\u00A77You may choose to free the mortals... or continue the harvest.",
                                    newOwnership != null ? "Lord of the Cave World" : "unknown")),
                                true);

                            Ergenverse.LOGGER.info("[Ergenverse] Cave World ownership transferred to {}.",
                                    player.getName().getString());
                            return 1;
                        })
                    )
                )
        );
    }

    private static int countEssences(CultivationState state) {
        int count = 0;
        for (boolean b : state.getEssencesComprehended()) if (b) count++;
        return count;
    }

    /** Suggestion provider for active incarnation IDs (used by merge subcommand). */
    private static final SuggestionProvider<CommandSourceStack> INCARNATION_SUGGESTIONS = (ctx, builder) -> {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        try {
            CultivationState state = CultivationCapability.getOrThrow(player);
            for (var inc : state.getSamsaraIncarnations()) {
                if (!inc.reabsorbed) builder.suggest(inc.id);
            }
        } catch (Exception ignored) {}
        return SharedSuggestionProvider.suggest(new java.util.ArrayList<>(), builder);
    };
}