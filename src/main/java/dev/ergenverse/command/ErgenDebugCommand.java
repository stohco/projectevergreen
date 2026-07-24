package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.ecology.CausalEcology;
import dev.ergenverse.history.CanonDivergenceRecorder;
import dev.ergenverse.history.WorldChronicle;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.npc.rumor.RumorNetwork;
import dev.ergenverse.simulation.WorldSimState;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.opportunity.OpportunityRegistry;
import dev.ergenverse.simulation.settlement.CultivatorMindRegistry;
import dev.ergenverse.world.blueprint.WorldBlueprintManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * ErgenDebugCommand — {@code /ergen debug} — the UNIFIED developer console.
 *
 * <p>Per the user's directive:
 * <pre>
 *   "I'd create /ergen debug as ONE unified developer console.
 *    Not dozens of commands. ONE."
 * </pre>
 *
 * <p>Subcommands:
 * <ul>
 *   <li>{@code /ergen debug} — show all subsystems and their status</li>
 *   <li>{@code /ergen debug actor &lt;id&gt;} — show cognition stack of an NPC</li>
 *   <li>{@code /ergen debug simulation} — show simulation-wide state</li>
 *   <li>{@code /ergen debug blueprint} — show world blueprint</li>
 *   <li>{@code /ergen debug events} — show eventbus diagnostic snapshot</li>
 *   <li>{@code /ergen debug relationships} — show NPC relationship graph</li>
 * </ul>
 *
 * <p>Constitution: Article XXV (Completed System Checklist) — debugging tools.
 */
public class ErgenDebugCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> d = event.getDispatcher();
        d.register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("debug")
                    .executes(ErgenDebugCommand::showList)
                    .then(Commands.literal("list")
                        .executes(ErgenDebugCommand::showList))
                    .then(Commands.literal("actor")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ErgenDebugCommand::showActor)))
                    .then(Commands.literal("simulation")
                        .executes(ErgenDebugCommand::showSimulation))
                    .then(Commands.literal("blueprint")
                        .executes(ErgenDebugCommand::showBlueprint))
                    .then(Commands.literal("events")
                        .executes(ErgenDebugCommand::showEvents))
                    .then(Commands.literal("relationships")
                        .executes(ErgenDebugCommand::showRelationships))
                )
        );
    }

    // ── /ergen debug [list] ──────────────────────────────────────
    private static int showList(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        boolean hasLevel = level instanceof ServerLevel;

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l========== ER GEN DEBUG CONSOLE ==========\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7eOne command. Everything inspectable.\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);

        sendLine(ctx, "World Blueprint", WorldBlueprintManager.isLoaded()
                ? "\u00a7aLOADED\u00a7r" : "\u00a7cNOT LOADED\u00a7r");
        sendLine(ctx, "EventBus", WorldEventBus.currentLevel() != null
                ? "\u00a7aLIVE\u00a7r (" + WorldEventBus.subscriberCount() + " subs)" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "World History", hasLevel && WorldHistory.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "World Chronicle", hasLevel && WorldChronicle.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Canon Divergence", hasLevel && CanonDivergenceRecorder.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Ecology", CausalEcology.all().isEmpty()
                ? "\u00a78OFF\u00a7r" : "\u00a7aLIVE\u00a7r (" + CausalEcology.all().size() + " ecosystems)");
        sendLine(ctx, "World Sim State", hasLevel && WorldSimState.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Rumor Network", hasLevel && RumorNetwork.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Opportunity Registry", hasLevel && OpportunityRegistry.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Actors", ActorRegistry.all().size() + " registered");

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Commands:\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug actor <id>\u00a7r \u2014 inspect NPC cognition"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug simulation\u00a7r \u2014 macro simulation state"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug blueprint\u00a7r \u2014 world blueprint status"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug events\u00a7r \u2014 eventbus diagnostics"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug relationships\u00a7r \u2014 NPC relationships"), false);
        return 1;
    }

    // ── /ergen debug actor <id> ────────────────────────────────────
    private static int showActor(CommandContext<CommandSourceStack> ctx) {
        String actorId = StringArgumentType.getString(ctx, "id");

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== ACTOR: " + actorId + " ===\u00a7r"), false);

        // Search actor registry (case-insensitive)
        Optional<Actor> found = ActorRegistry.all().stream()
                .filter(a -> a.id.equalsIgnoreCase(actorId))
                .findFirst();

        if (found.isPresent()) {
            Actor actor = found.get();
            sendLine(ctx, "Display Name", actor.displayName);
            sendLine(ctx, "Type", actor.type.name());
            sendLine(ctx, "Provenance", actor.provenance);
            sendLine(ctx, "Canon Confidence", actor.canonConfidence);
            if (actor.daoIdentity != null) {
                sendLine(ctx, "Dao Identity", actor.daoIdentity.name());
            }
            if (actor.beastTier != null) {
                sendLine(ctx, "Beast Tier", actor.beastTier.name());
            }
            if (actor.currentActivity != null) {
                sendLine(ctx, "Current Activity", actor.currentActivity.activityType);
            }
            sendLine(ctx, "Sim Level", actor.simLevel.name());
            sendLine(ctx, "Position", actor.blockX + ", " + actor.blockY + ", " + actor.blockZ);
            sendLine(ctx, "Capabilities", actor.capabilities.toString());
            sendLine(ctx, "Goals", String.valueOf(actor.goals.size()));
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a78Actor '" + actorId + "' not found in ActorRegistry.\u00a7r"), false);
        }

        // Check CultivatorMind
        var mind = CultivatorMindRegistry.get(actorId);
        if (mind != null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77--- Cultivator Mind ---\u00a7r"), false);
            sendLine(ctx, "Display Name", mind.displayName);
            if (mind.currentActivity != null) {
                sendLine(ctx, "Current Activity", mind.currentActivity.toString());
            }
            var motivations = mind.getMotivations();
            if (!motivations.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  \u00a77Motivations:\u00a7r"), false);
                for (var entry : motivations.entrySet()) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "    \u00a7b" + entry.getKey().name() + "\u00a7r: "
                        + String.format("%.2f", entry.getValue())), false);
                }
            }
        }

        // Wang Lin specific
        if ("wang_lin".equalsIgnoreCase(actorId) || "wanglin".equalsIgnoreCase(actorId)) {
            showWangLin(ctx);
        }

        return 1;
    }

    private static void showWangLin(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7e--- Wang Lin Cognition ---\u00a7r"), false);

        // Wang Lin is the most deeply modeled NPC.
        // His reasoning engine evaluates gift requests via 6 factors.
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Reasoning Engine:\u00a7r 6-factor model (Necessity, Safety,"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "    Usefulness, Uniqueness, Current Need, Judgment)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Observation:\u00a7r Subscribed to WorldEventBus semantic events"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "    (act_of_mercy, cultivation_revealed, promise_broken, etc.)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Opinion shifts:\u00a7r Silent — no notification to player."), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "    Next gift request reflects everything Wang Lin witnessed."), false);

        var mind = CultivatorMindRegistry.get("wang_lin");
        if (mind != null) {
            sendLine(ctx, "Mind Status", "Registered: " + mind.displayName);
        }
    }

    // ── /ergen debug simulation ──────────────────────────────────
    private static int showSimulation(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== SIMULATION STATUS ===\u00a7r"), false);

        sendLine(ctx, "WorldBlueprint", WorldBlueprintManager.isLoaded()
                ? "\u00a7aYES\u00a7r" : "\u00a7cNO\u00a7r");
        sendLine(ctx, "EventBus", WorldEventBus.currentLevel() != null
                ? "\u00a7aLIVE\u00a7r (" + WorldEventBus.subscriberCount() + " subs)" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "WorldHistory", WorldHistory.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "WorldChronicle", WorldChronicle.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "CanonDivergence", CanonDivergenceRecorder.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Ecology", CausalEcology.all().isEmpty()
                ? "\u00a78OFF\u00a7r" : "\u00a7aLIVE\u00a7r");
        sendLine(ctx, "WorldSimState", WorldSimState.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Rumors", RumorNetwork.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Opportunities", OpportunityRegistry.get(level) != null
                ? "\u00a7aLIVE\u00a7r" : "\u00a78OFF\u00a7r");
        sendLine(ctx, "Actors", String.valueOf(ActorRegistry.all().size()));

        // Living Moments tracker
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77--- Living Moments ---\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a78(Living Moments status requires playtest observation)\u00a7r"), false);

        return 1;
    }

    // ── /ergen debug blueprint ───────────────────────────────────
    private static int showBlueprint(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== WORLD BLUEPRINT ===\u00a7r"), false);

        if (!WorldBlueprintManager.isLoaded()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cBLUEPRINT NOT LOADED. This is a critical error.\u00a7r"), false);
            return 1;
        }

        sendLine(ctx, "Status", "\u00a7aLOADED\u00a7r");
        sendLine(ctx, "Canon Seed", String.valueOf(WorldBlueprintManager.CANON_SEED));
        sendLine(ctx, "Countries", String.valueOf(WorldBlueprintManager.getSettlements().size()));
        sendLine(ctx, "Settlements", String.valueOf(WorldBlueprintManager.getSettlements().size()));
        sendLine(ctx, "Mountain Ranges", String.valueOf(WorldBlueprintManager.getMountainRanges().size()));
        sendLine(ctx, "Rivers", String.valueOf(WorldBlueprintManager.getRivers().size()));
        sendLine(ctx, "Spirit Veins", String.valueOf(WorldBlueprintManager.getSpiritVeins().size()));
        sendLine(ctx, "Roads", String.valueOf(WorldBlueprintManager.getRoads().size()));
        sendLine(ctx, "Restrictions", String.valueOf(WorldBlueprintManager.getRestrictions().size()));

        int[] spawn = WorldBlueprintManager.getSpawnPoint();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Spawn Point:\u00a7r " + spawn[0] + ", " + spawn[1] + ", " + spawn[2]), false);
        return 1;
    }

    // ── /ergen debug events ───────────────────────────────────────
    private static int showEvents(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== WORLD EVENT BUS ===\u00a7r"), false);

        List<String> snapshot = WorldEventBus.diagnosticSnapshot();
        for (String line : snapshot) {
            ctx.getSource().sendSuccess(() -> Component.literal(line), false);
        }
        return 1;
    }

    // ── /ergen debug relationships ───────────────────────────────
    private static int showRelationships(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== RELATIONSHIP GRAPH ===\u00a7r"), false);

        var store = dev.ergenverse.simulation.action.ActorRelationshipStore.get(level);
        if (store == null) {
            sendLine(ctx, "ActorRelationshipStore", "\u00a78NOT LOADED\u00a7r");
            return 1;
        }

        String report = store.getStatusReport();
        ctx.getSource().sendSuccess(() -> Component.literal(report), false);
        return 1;
    }

    // ── Helper ────────────────────────────────────────────────────
    private static void sendLine(CommandContext<CommandSourceStack> ctx, String key, String value) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77" + key + ":\u00a7r " + value), false);
    }
}
