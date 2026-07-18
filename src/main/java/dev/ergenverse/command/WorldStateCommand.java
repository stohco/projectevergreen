package dev.ergenverse.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.WorldStateEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * WorldStateCommand — /wanglin worldstate <status|exists|owns|wants|knows|why|next>
 *
 * <p>The in-game verification command for the World State Engine. Exercises the
 * 6 core queries so a server operator can confirm the engine is loading data
 * and returning real results (not empty stubs).
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /wanglin worldstate status} — loader status + tick count</li>
 *   <li>{@code /wanglin worldstate exists <locationId>} — Query 1: what exists here?</li>
 *   <li>{@code /wanglin worldstate owns <objectId>} — Query 2: who owns this?</li>
 *   <li>{@code /wanglin worldstate wants <objectId>} — Query 3: who wants this?</li>
 *   <li>{@code /wanglin worldstate knows <objectId>} — Query 4: who knows about this?</li>
 *   <li>{@code /wanglin worldstate why <objectId>} — Query 5: why is it untaken?</li>
 *   <li>{@code /wanglin worldstate next <objectId>} — Query 6: what happens next?</li>
 * </ul>
 */
public class WorldStateCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("wanglin")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("worldstate")
                    .then(Commands.literal("status")
                        .executes(WorldStateCommand::status))
                    .then(Commands.literal("exists")
                        .then(Commands.argument("locationId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryExists)))
                    .then(Commands.literal("owns")
                        .then(Commands.argument("objectId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryOwns)))
                    .then(Commands.literal("wants")
                        .then(Commands.argument("objectId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryWants)))
                    .then(Commands.literal("knows")
                        .then(Commands.argument("objectId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryKnows)))
                    .then(Commands.literal("why")
                        .then(Commands.argument("objectId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryWhy)))
                    .then(Commands.literal("next")
                        .then(Commands.argument("objectId", StringArgumentType.string())
                            .executes(WorldStateCommand::queryNext))))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /wanglin worldstate command registered (status/exists/owns/wants/knows/why/next).");
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        // Force-load so the status is current
        WorldStateDataLoader.loadOnce();
        src.sendSuccess(() -> Component.literal("§6=== World State Engine Status ==="), false);
        src.sendSuccess(() -> Component.literal("§7" + WorldStateEngine.getStatusReport()), false);
        src.sendSuccess(() -> Component.literal("§7" + WorldStateDataLoader.getStatusReport()), false);
        return 1;
    }

    private static int queryExists(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String loc = StringArgumentType.getString(ctx, "locationId");
        List<WorldStateEngine.ObjectEntry> entries = WorldStateEngine.queryWhatExists(loc);
        src.sendSuccess(() -> Component.literal(
                "§6[Query 1: What exists at §f" + loc + "§6?] §7" + entries.size() + " entries:"), false);
        for (WorldStateEngine.ObjectEntry e : entries) {
            src.sendSuccess(() -> Component.literal(
                    "§7  §f[" + e.objectType() + "] §e" + e.objectId() + "§7 — " + e.trueState()), false);
        }
        return entries.size();
    }

    private static int queryOwns(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "objectId");
        WorldStateEngine.OwnershipRecord rec = WorldStateEngine.queryWhoOwns(id);
        if (rec == null) {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 2: Who owns §f" + id + "§6?] §cNo owner found (unclaimed or unknown)."), false);
        } else {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 2: Who owns §f" + id + "§6?] §a" + rec.trueOwner() +
                    " §7(type=" + rec.ownerType() + ", claim=" + rec.claimStrength() + "/10)"), false);
        }
        return rec == null ? 0 : 1;
    }

    private static int queryWants(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "objectId");
        List<WorldStateEngine.DesireRecord> recs = WorldStateEngine.queryWhoWants(id);
        src.sendSuccess(() -> Component.literal(
                "§6[Query 3: Who wants §f" + id + "§6?] §7" + recs.size() + " desire records:"), false);
        for (WorldStateEngine.DesireRecord d : recs) {
            src.sendSuccess(() -> Component.literal(
                    "§7  §f" + d.desirerId() + " §7wants it for §b" + d.desireType() +
                    " §7(strength=" + d.desireStrength() + "/10) — " + d.desireReason()), false);
        }
        return recs.size();
    }

    private static int queryKnows(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "objectId");
        List<WorldStateEngine.KnowledgeRecord> recs = WorldStateEngine.queryWhoKnows(id);
        src.sendSuccess(() -> Component.literal(
                "§6[Query 4: Who knows about §f" + id + "§6?] §7" + recs.size() + " knowers:"), false);
        for (WorldStateEngine.KnowledgeRecord k : recs) {
            src.sendSuccess(() -> Component.literal(
                    "§7  §f" + k.knowerId() + " §7(tier=" + k.knowledgeTier() +
                    "/5, accuracy=" + k.knowledgeAccuracy() + "/10)"), false);
        }
        return recs.size();
    }

    private static int queryWhy(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "objectId");
        WorldStateEngine.UntakenReason reason = WorldStateEngine.queryWhyUntaken(id);
        if (reason == null) {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 5: Why untaken?] §c" + id + " is already taken (owned)."), false);
        } else {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 5: Why is §f" + id + "§6 untaken?] §e" + reason.reasonType() +
                    " §7(strength=" + reason.reasonStrength() + "/10)"), false);
        }
        return reason == null ? 0 : 1;
    }

    private static int queryNext(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = StringArgumentType.getString(ctx, "objectId");
        WorldStateEngine.NaturalNextEvent ev = WorldStateEngine.queryNaturalNext(id);
        if (ev == null) {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 6: What next for §f" + id + "§6?] §7No pending event."), false);
        } else {
            src.sendSuccess(() -> Component.literal(
                    "§6[Query 6: What next for §f" + id + "§6?] §e" + ev.nextEvent() +
                    " §7(prob=" + ev.probability() + ", ticks=" + ev.timelineTicks() + ")"), false);
        }
        return ev == null ? 0 : 1;
    }
}
