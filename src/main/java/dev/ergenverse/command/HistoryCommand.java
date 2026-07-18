package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.history.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/**
 * HistoryCommand — /ergen history [player|world|npc|relationship|stats]
 *
 * <p>In-game command for inspecting the Layer 3 Emergent History system.
 * Players and operators can view their personal history, world events,
 * NPC memories, and relationship status.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergen history player [type]} — show player's emergent history.
 *       Optional type filter: BREAKTHROUGH, GIFT_RECEIVED, KILL, DISCOVERY,
 *       TECHNIQUE_LEARNED, KARMA_ACTION.</li>
 *   <li>{@code /ergen history world [region]} — show world history events.
 *       Optional region filter (e.g. zhao_country, heng_yue_sect).</li>
 *   <li>{@code /ergen history npc [npcId]} — show NPC memory for a specific NPC.
 *       Shows trust score + memory list. If no NPC specified, lists all known NPCs.</li>
 *   <li>{@code /ergen history relationship [protagonistId]} — show relationship history.
 *       Shows affinity score + event timeline. If no protagonist, lists all.</li>
 *   <li>{@code /ergen history stats} — show aggregate statistics for all 4 history systems.</li>
 * </ul>
 */
public class HistoryCommand {

    private static final int MAX_DISPLAY_ENTRIES = 15;

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(0)) // all players can view their own history
                .then(Commands.literal("history")
                    .executes(HistoryCommand::showPlayerSummary)
                    .then(Commands.literal("player")
                        .executes(HistoryCommand::showPlayerHistory)
                        .then(Commands.argument("type", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("BREAKTHROUGH");
                                builder.suggest("GIFT_RECEIVED");
                                builder.suggest("KILL");
                                builder.suggest("DISCOVERY");
                                builder.suggest("TECHNIQUE_LEARNED");
                                builder.suggest("KARMA_ACTION");
                                return builder.buildFuture();
                            })
                            .executes(HistoryCommand::showPlayerHistoryFiltered)))
                    .then(Commands.literal("world")
                        .executes(HistoryCommand::showWorldHistory)
                        .then(Commands.argument("region", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("zhao_country");
                                builder.suggest("heng_yue_sect");
                                builder.suggest("heavenly_fate_sect");
                                builder.suggest("pilu_kingdom");
                                builder.suggest("sea_of_devils");
                                builder.suggest("planet_suzaku");
                                builder.suggest("zhao_plains");
                                builder.suggest("zhao_mountains");
                                return builder.buildFuture();
                            })
                            .executes(HistoryCommand::showWorldHistoryRegion)))
                    .then(Commands.literal("npc")
                        .executes(HistoryCommand::showNpcList)
                        .then(Commands.argument("npcId", StringArgumentType.word())
                            .executes(HistoryCommand::showNpcMemory)))
                    .then(Commands.literal("relationship")
                        .executes(HistoryCommand::showRelationshipList)
                        .then(Commands.argument("protagonistId", StringArgumentType.word())
                            .suggests((ctx, builder) -> {
                                builder.suggest("wang_lin");
                                builder.suggest("meng_hao");
                                builder.suggest("bai_xiaochun");
                                builder.suggest("su_ming");
                                builder.suggest("xu_qing");
                                return builder.buildFuture();
                            })
                            .executes(HistoryCommand::showRelationshipHistory)))
                    .then(Commands.literal("stats")
                        .requires(src -> src.hasPermission(2))
                        .executes(HistoryCommand::showStats)))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergen history command registered.");
    }

    // ─── Player History ──────────────────────────────────────────────

    private static int showPlayerSummary(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        PlayerHistory ph = PlayerHistory.get(player);
        MutableComponent header = Component.literal("\u00A76\u00A7l=== Your Cultivation Chronicle ===");
        src.sendSuccess(() -> header, false);
        src.sendSuccess(() -> Component.literal("\u00A77" + ph.size() +
                " events recorded in your personal history."), false);

        if (ph.size() == 0) {
            src.sendSuccess(() -> Component.literal("\u00A78Your legend has not yet begun. " +
                    "Explore the world, cultivate, and make your mark."), false);
            return 1;
        }

        // Show the 5 most recent events
        List<PlayerHistory.HistoryEntry> all = ph.all();
        int start = Math.max(0, all.size() - 5);
        src.sendSuccess(() -> Component.literal("\u00A77--- Recent Events ---"), false);
        for (int i = start; i < all.size(); i++) {
            PlayerHistory.HistoryEntry e = all.get(i);
            src.sendSuccess(() -> formatPlayerEntry(e), false);
        }
        return 1;
    }

    private static int showPlayerHistory(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        PlayerHistory ph = PlayerHistory.get(player);
        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== Player History (" +
                ph.size() + " events) ==="), false);
        return displayPlayerEntries(src, ph.all());
    }

    private static int showPlayerHistoryFiltered(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String type = StringArgumentType.getString(ctx, "type");
        PlayerHistory ph = PlayerHistory.get(player);
        List<PlayerHistory.HistoryEntry> filtered = ph.forType(type);

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== Player History [" +
                type + "] (" + filtered.size() + " events) ==="), false);
        return displayPlayerEntries(src, filtered);
    }

    private static int displayPlayerEntries(CommandSourceStack src,
                                             List<PlayerHistory.HistoryEntry> entries) {
        if (entries.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78No events found for this filter."), false);
            return 0;
        }

        int start = Math.max(0, entries.size() - MAX_DISPLAY_ENTRIES);
        if (start > 0) {
            src.sendSuccess(() -> Component.literal("\u00A77... " + start +
                    " older events omitted ..."), false);
        }
        for (int i = start; i < entries.size(); i++) {
            PlayerHistory.HistoryEntry entry = entries.get(i);
            src.sendSuccess(() -> formatPlayerEntry(entry), false);
        }
        return 1;
    }

    private static MutableComponent formatPlayerEntry(PlayerHistory.HistoryEntry e) {
        String color = switch (e.eventType) {
            case "BREAKTHROUGH" -> "\u00A7a";    // green
            case "GIFT_RECEIVED" -> "\u00A7d";  // purple
            case "KILL" -> "\u00A7c";           // red
            case "DISCOVERY" -> "\u00A7e";      // yellow
            case "TECHNIQUE_LEARNED" -> "\u00A7b"; // aqua
            case "KARMA_ACTION" -> "\u00A76";   // gold
            default -> "\u00A7f";
        };
        return Component.literal(color + "[" + e.eventType + "] \u00A7f" + e.description);
    }

    // ─── World History ───────────────────────────────────────────────

    private static int showWorldHistory(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel serverLevel = src.getLevel();

        WorldHistory wh = WorldHistory.get(serverLevel);
        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== World History (" +
                wh.size() + " global events) ==="), false);

        List<WorldHistory.WorldEvent> events = wh.all();
        return displayWorldEvents(src, events);
    }

    private static int showWorldHistoryRegion(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel serverLevel = src.getLevel();

        String region = StringArgumentType.getString(ctx, "region");
        WorldHistory wh = WorldHistory.get(serverLevel);
        List<WorldHistory.WorldEvent> events = wh.forRegion(region);

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== World History [" +
                region + "] (" + events.size() + " events) ==="), false);
        return displayWorldEvents(src, events);
    }

    private static int displayWorldEvents(CommandSourceStack src,
                                           List<WorldHistory.WorldEvent> events) {
        if (events.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78No world events found."), false);
            return 0;
        }

        int start = Math.max(0, events.size() - MAX_DISPLAY_ENTRIES);
        if (start > 0) {
            src.sendSuccess(() -> Component.literal("\u00A77... " + start +
                    " older events omitted ..."), false);
        }
        for (int i = start; i < events.size(); i++) {
            WorldHistory.WorldEvent e = events.get(i);
            String typeColor = switch (e.eventType()) {
                case "CANON_CONSEQUENCE" -> "\u00A7a";
                case "PLAYER_ACTION" -> "\u00A7e";
                case "ECOLOGY_SHIFT" -> "\u00A72";
                case "FACTION_CHANGE" -> "\u00A7c";
                default -> "\u00A7f";
            };
            MutableComponent line = Component.literal(
                    typeColor + "[" + e.eventType() + "] \u00A77" + e.regionId() +
                    ": \u00A7f" + e.description());
            src.sendSuccess(() -> line, false);
        }
        return 1;
    }

    // ─── NPC Memory ──────────────────────────────────────────────────

    private static int showNpcList(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        NpcMemory mem = NpcMemory.get(player);
        List<String> known = mem.knownNpcs();

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== Known NPCs (" +
                known.size() + ") ==="), false);

        if (known.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78You have not interacted " +
                    "with any NPCs yet."), false);
            return 1;
        }

        for (String npcId : known) {
            int trust = mem.trustScore(npcId);
            List<NpcMemory.Memory> memories = mem.forNpc(npcId);
            String trustColor = trust >= 10 ? "\u00A7a" : (trust <= -10 ? "\u00A7c" : "\u00A77");
            src.sendSuccess(() -> Component.literal(
                    "  \u00A7f" + npcId + " \u00A77(" + memories.size() + " memories) " +
                    trustColor + "Trust: " + trust), false);
        }
        src.sendSuccess(() -> Component.literal("\u00A77Use /ergen history npc <id> " +
                "for details."), false);
        return 1;
    }

    private static int showNpcMemory(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String npcId = StringArgumentType.getString(ctx, "npcId");
        NpcMemory mem = NpcMemory.get(player);
        List<NpcMemory.Memory> memories = mem.forNpc(npcId);
        int trust = mem.trustScore(npcId);

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== NPC Memory: " +
                npcId + " ==="), false);
        String trustColor = trust >= 10 ? "\u00A7a" : (trust <= -10 ? "\u00A7c" : "\u00A77");
        src.sendSuccess(() -> Component.literal(trustColor + "Trust Score: " +
                trust + " \u00A77(" + memories.size() + " memories)"), false);

        if (memories.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78No memories recorded " +
                    "for this NPC."), false);
            return 1;
        }

        int start = Math.max(0, memories.size() - MAX_DISPLAY_ENTRIES);
        for (int i = start; i < memories.size(); i++) {
            NpcMemory.Memory m = memories.get(i);
            String typeColor = switch (m.memoryType) {
                case "COMBAT" -> "\u00A7c";
                case "GIFT_GRANTED" -> "\u00A7d";
                case "GIFT_RECEIVED" -> "\u00A7a";
                case "FAVOR_BROKEN" -> "\u00A74";
                case "INTERACTION" -> "\u00A7f";
                default -> "\u00A77";
            };
            String deltaStr = m.trustDelta > 0 ? "(+" + m.trustDelta + ")" :
                    ("(" + m.trustDelta + ")");
            src.sendSuccess(() -> Component.literal(
                    "  " + typeColor + "[" + m.memoryType + "] " + deltaStr +
                    " \u00A77" + m.description), false);
        }
        return 1;
    }

    // ─── Relationship History ────────────────────────────────────────

    private static int showRelationshipList(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        RelationshipHistory rh = RelationshipHistory.get(player);
        List<String> known = rh.knownProtagonists();

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== Manifestation Relationships (" +
                known.size() + ") ==="), false);

        if (known.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78You have not encountered " +
                    "any manifestation companions yet."), false);
            return 1;
        }

        for (String protagId : known) {
            int affinity = rh.affinityScore(protagId);
            String affColor = affinity >= 20 ? "\u00A7a" : (affinity < 0 ? "\u00A7c" : "\u00A77");
            src.sendSuccess(() -> Component.literal(
                    "  \u00A7f" + protagId + " " + affColor + "Affinity: " + affinity), false);
        }
        src.sendSuccess(() -> Component.literal("\u00A77Use /ergen history relationship <id> " +
                "for details."), false);
        return 1;
    }

    private static int showRelationshipHistory(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
        }

        String protagId = StringArgumentType.getString(ctx, "protagonistId");
        RelationshipHistory rh = RelationshipHistory.get(player);
        List<RelationshipHistory.RelationshipEvent> events = rh.forProtagonist(protagId);
        int affinity = rh.affinityScore(protagId);

        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== Relationship: " +
                protagId + " ==="), false);
        String affColor = affinity >= 20 ? "\u00A7a" : (affinity < 0 ? "\u00A7c" : "\u00A77");
        src.sendSuccess(() -> Component.literal(affColor + "Total Affinity: " +
                affinity + " \u00A77(" + events.size() + " events)"), false);

        if (events.isEmpty()) {
            src.sendSuccess(() -> Component.literal("\u00A78No relationship events recorded."), false);
            return 1;
        }

        int start = Math.max(0, events.size() - MAX_DISPLAY_ENTRIES);
        for (int i = start; i < events.size(); i++) {
            RelationshipHistory.RelationshipEvent e = events.get(i);
            String typeColor = switch (e.eventType()) {
                case "GIFT_RECEIVED" -> "\u00A7d";
                case "SHARED_EXPERIENCE" -> "\u00A7e";
                case "QUEST_COMPLETED" -> "\u00A7a";
                case "TRUST_BROKEN" -> "\u00A74";
                default -> "\u00A77";
            };
            String deltaStr = e.affinityDelta() > 0 ? "(+" + e.affinityDelta() + ")" :
                    ("(" + e.affinityDelta() + ")");
            src.sendSuccess(() -> Component.literal(
                    "  " + typeColor + "[" + e.eventType() + "] " + deltaStr +
                    " \u00A77" + e.description()), false);
        }
        return 1;
    }

    // ─── Stats ───────────────────────────────────────────────────────

    private static int showStats(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        ServerLevel serverLevel = src.getLevel();

        WorldHistory wh = WorldHistory.get(serverLevel);
        src.sendSuccess(() -> Component.literal("\u00A76\u00A7l=== History System Statistics ==="),
                false);
        src.sendSuccess(() -> Component.literal("\u00A77World History: \u00A7f" +
                wh.size() + " global events"), false);
        src.sendSuccess(() -> Component.literal("\u00A77  Canon Consequences: \u00A7a" +
                wh.canonConsequences().size()), false);
        src.sendSuccess(() -> Component.literal("\u00A77  Player Actions: \u00A7e" +
                wh.playerActions().size()), false);
        src.sendSuccess(() -> Component.literal("\u00A77Ecology Shifts: \u00A72" +
                wh.forType("ECOLOGY_SHIFT").size()), false);
        src.sendSuccess(() -> Component.literal("\u00A77Faction Changes: \u00A7c" +
                wh.forType("FACTION_CHANGE").size()), false);

        // Per-player stats (for the executing player)
        ServerPlayer player = ctx.getSource().getPlayer();
        if (player != null) {
            PlayerHistory ph = PlayerHistory.get(player);
            NpcMemory nm = NpcMemory.get(player);
            RelationshipHistory rh = RelationshipHistory.get(player);

            src.sendSuccess(() -> Component.literal(""), false);
            src.sendSuccess(() -> Component.literal("\u00A77Player History: \u00A7f" +
                    ph.size() + " events"), false);
            src.sendSuccess(() -> Component.literal("\u00A77NPC Memories: \u00A7f" +
                    nm.knownNpcs().size() + " NPCs known"), false);
            src.sendSuccess(() -> Component.literal("\u00A77Relationships: \u00A7f" +
                    rh.knownProtagonists().size() + " manifestations encountered"), false);
        }

        // Persistence info
        src.sendSuccess(() -> Component.literal(""), false);
        src.sendSuccess(() -> Component.literal("\u00A78All history persisted via " +
                "WorldRuntimeState (SavedData)."), false);
        return 1;
    }
}