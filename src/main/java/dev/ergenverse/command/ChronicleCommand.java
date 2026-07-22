package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.CanonDivergenceRecorder;
import dev.ergenverse.history.WorldChronicle;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * ChronicleCommand — {@code /ergen chronicle} and {@code /ergen divergence}.
 *
 * <p>In-game commands for inspecting the two flagship single-player-maximalism
 * systems (Art XLIII):
 *
 * <h2>Chronicle subcommands</h2>
 * <ul>
 *   <li>{@code /ergen chronicle} — show the 10 most recent chronicle entries
 *       (prose, tone-colored). This is "the world's living history."</li>
 *   <li>{@code /ergen chronicle recent <n>} — show the N most recent entries.</li>
 *   <li>{@code /ergen chronicle era <era>} — show all entries for an era
 *       (ANCIENT_ERA, EARLY_LIFE, ZHAO_COUNTRY_ARC, SUZAKU_INHERITANCE_ARC,
 *       HEAVENLY_FATE_ARC, ALLHEAVEN_ARC, DIVERGENT, PRESENT).</li>
 *   <li>{@code /ergen chronicle all} — show the entire chronicle (capped).</li>
 * </ul>
 *
 * <h2>Divergence subcommands</h2>
 * <ul>
 *   <li>{@code /ergen divergence} — show the canon-divergence summary report
 *       (how many events occurred as-written, diverged, prevented, pending).</li>
 *   <li>{@code /ergen divergence forks} — list every timeline fork
 *       (DIVERGED + PREVENTED records), each showing canonical-vs-actual.</li>
 *   <li>{@code /ergen divergence <eventId>} — show one canon event's full
 *       divergence record (e.g. {@code /ergen divergence E14}).</li>
 * </ul>
 *
 * <p><b>Constitution compliance:</b> Art XLII (Layer 3 — Simulation Delta),
 * Art XLIII (saves are historical records; canon is never overwritten, only
 * deviated from).
 */
public class ChronicleCommand {

    private static final int DEFAULT_RECENT = 10;
    private static final int MAX_DISPLAY = 50;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(0))
                // ── /ergen chronicle ──
                .then(Commands.literal("chronicle")
                    .executes(ChronicleCommand::showRecentChronicle)
                    .then(Commands.literal("recent")
                        .then(Commands.argument("n", IntegerArgumentType.integer(1, MAX_DISPLAY))
                            .executes(ChronicleCommand::showRecentN)))
                    .then(Commands.literal("era")
                        .then(Commands.argument("era", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("ANCIENT_ERA");
                                builder.suggest("EARLY_LIFE");
                                builder.suggest("ZHAO_COUNTRY_ARC");
                                builder.suggest("SUZAKU_INHERITANCE_ARC");
                                builder.suggest("HEAVENLY_FATE_ARC");
                                builder.suggest("ALLHEAVEN_ARC");
                                builder.suggest("DIVERGENT");
                                builder.suggest("PRESENT");
                                return builder.buildFuture();
                            })
                            .executes(ChronicleCommand::showEra)))
                    .then(Commands.literal("all")
                        .executes(ChronicleCommand::showAll)))
                // ── /ergen divergence ──
                .then(Commands.literal("divergence")
                    .executes(ChronicleCommand::showDivergenceReport)
                    .then(Commands.literal("forks")
                        .executes(ChronicleCommand::showForks))
                    .then(Commands.argument("eventId", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            builder.suggest("E08");  // Wang Lin is Born
                            builder.suggest("E10");  // Heng Yue Recruitment
                            builder.suggest("E13");  // Teng Clan Massacre of Wang Family
                            builder.suggest("E14");  // Wang Lin's Revenge on the Teng Clan
                            builder.suggest("E15");  // Core Formation in the Sea of Devils
                            builder.suggest("E17");  // Inheriting Tu Si's Ancient God Legacy
                            builder.suggest("E18");  // Soul Formation via Life-Death Domain
                            builder.suggest("E44");  // Wang Lin Raids the Suzaku Tomb
                            builder.suggest("E46");  // Wang Lin Becomes 6th-Gen Vermilion Bird Divine Emperor
                            return builder.buildFuture();
                        })
                        .executes(ChronicleCommand::showOneDivergence)))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergen chronicle + /ergen divergence commands registered.");
    }

    // ─── Chronicle handlers ──────────────────────────────────────────

    private static int showRecentChronicle(CommandContext<CommandSourceStack> ctx) {
        return sendChronicle(ctx, DEFAULT_RECENT);
    }

    private static int showRecentN(CommandContext<CommandSourceStack> ctx) {
        int n = IntegerArgumentType.getInteger(ctx, "n");
        return sendChronicle(ctx, n);
    }

    private static int showEra(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        String era = StringArgumentType.getString(ctx, "era");
        WorldChronicle chronicle = WorldChronicle.get(level);
        Component rendered = chronicle.renderEra(era);
        ctx.getSource().sendSuccess(() -> rendered, false);
        return chronicle.forEra(era).size();
    }

    private static int showAll(CommandContext<CommandSourceStack> ctx) {
        return sendChronicle(ctx, MAX_DISPLAY);
    }

    private static int sendChronicle(CommandContext<CommandSourceStack> ctx, int n) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldChronicle chronicle = WorldChronicle.get(level);
        Component rendered = chronicle.renderRecent(n);
        ctx.getSource().sendSuccess(() -> rendered, false);
        return chronicle.size();
    }

    // ─── Divergence handlers ─────────────────────────────────────────

    private static int showDivergenceReport(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        CanonDivergenceRecorder recorder = CanonDivergenceRecorder.get(level);
        Component rendered = recorder.renderReport();
        ctx.getSource().sendSuccess(() -> rendered, false);
        return recorder.divergenceCount();
    }

    private static int showForks(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        CanonDivergenceRecorder recorder = CanonDivergenceRecorder.get(level);
        Component rendered = recorder.renderDivergences();
        ctx.getSource().sendSuccess(() -> rendered, false);
        return recorder.divergenceCount();
    }

    private static int showOneDivergence(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        String eventId = StringArgumentType.getString(ctx, "eventId");
        CanonDivergenceRecorder recorder = CanonDivergenceRecorder.get(level);
        Component rendered = recorder.renderEvent(eventId);
        ctx.getSource().sendSuccess(() -> rendered, false);
        return 1;
    }
}
