package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.ecology.CausalEcology;
import dev.ergenverse.history.CanonDivergenceRecorder;
import dev.ergenverse.history.WorldChronicle;
import dev.ergenverse.history.WorldHistory;
import dev.ergenverse.npc.rumor.RumorNetwork;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.BlockChangeDelta;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import dev.ergenverse.simulation.WorldSimState;
import dev.ergenverse.simulation.actor.Actor;
import dev.ergenverse.simulation.actor.ActorRegistry;
import dev.ergenverse.simulation.actor.ActorTickLoop;
import dev.ergenverse.simulation.cognition.Ontology;
import dev.ergenverse.simulation.cognition.perception.Interpretation;
import dev.ergenverse.simulation.cognition.perception.PerceptionSensor;
import dev.ergenverse.simulation.cognition.perception.PerceptionSnapshot;
import dev.ergenverse.simulation.intent.CultivationTask;
import dev.ergenverse.simulation.event.EnergyType;
import dev.ergenverse.simulation.event.WorldEventBus;
import dev.ergenverse.simulation.opportunity.OpportunityRegistry;
import dev.ergenverse.simulation.settlement.CultivatorMindRegistry;
import dev.ergenverse.materialization.CanonSettlementBuilder;
import dev.ergenverse.simulation.residence.WangLinHomeBuilder;
import dev.ergenverse.world.blueprint.WorldBlueprintManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
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
 * <p>This is BOTH an <b>inspector</b> and a <b>reality manipulator</b>.
 * The user's expanded directive (2026-07-24):
 * <pre>
 *   "The debug command should manipulate reality, not just inspect.
 *    /ergen debug event wolf_attack
 *    /ergen debug weather storm
 *    /ergen debug rumor spread
 *    /ergen debug relationship wang_lin player trust +20
 *    /ergen debug time 3_days
 *    /ergen debug herb mature
 *    /ergen debug cultivation breakthrough wang_lin
 *    /ergen debug simulate 24h
 *    If every system is event-driven, then every event should be injectable."
 * </pre>
 *
 * <h2>Inspection subcommands</h2>
 * <ul>
 *   <li>{@code /ergen debug} — show all subsystems and their status</li>
 *   <li>{@code /ergen debug actor <id>} — show FULL cognition stack:
 *       Perception → Interpretation → Prediction → Goal → Intent → Plan → Activity</li>
 *   <li>{@code /ergen debug simulation} — show simulation-wide state</li>
 *   <li>{@code /ergen debug blueprint} — show world blueprint</li>
 *   <li>{@code /ergen debug events} — show eventbus diagnostic snapshot</li>
 *   <li>{@code /ergen debug relationships} — show NPC relationship graph</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal} — WorldDeltaStore status
 *       (PLAYER/SIMULATION/CANON delta counts)</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal query <x> <y> <z>} —
 *       deltas at a position + live Minecraft state</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal recent [count]} —
 *       last N SIMULATION deltas (proof that beasts/weather/residence are journaling)</li>
 * </ul>
 *
 * <h2>Reality-manipulation subcommands</h2>
 * <ul>
 *   <li>{@code /ergen debug event <topic> [intensity]} — inject a WorldEvent at your position</li>
 *   <li>{@code /ergen debug relationship <a> <b> <axis> <delta>} — mutate the relationship graph</li>
 *   <li>{@code /ergen debug simulate <ticks>} — fast-forward the actor tick loop</li>
 *   <li>{@code /ergen debug breakthrough <actorId>} — force a cultivation breakthrough</li>
 *   <li>{@code /ergen debug perception <id>} — force a perception snapshot now and show it</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal sim-write <x> <y> <z> <blockId>} —
 *       manually trigger a SIMULATION write (operational proof of persistence)</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal player-write <x> <y> <z> <blockId>} —
 *       manually trigger a PLAYER write (test resolution priority)</li>
 *   <li><b>CRON-73:</b> {@code /ergen debug journal clear} — wipe the journal
 *       (simulate fresh-save state; blueprint-only)</li>
 * </ul>
 *
 * <p>Constitution: Article XXV (Completed System Checklist) — debugging tools,
 * and Article XL (Prove The Experience) — the developer must be able to
 * trigger any canon situation to verify the simulation responds correctly.
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
                    // ── Reality manipulation ──
                    .then(Commands.literal("event")
                        .then(Commands.argument("topic", StringArgumentType.string())
                            .executes(ctx -> injectEvent(ctx, 0.7f))
                            .then(Commands.argument("intensity", FloatArgumentType.floatArg(0.0f, 1.0f))
                                .executes(ctx -> injectEvent(ctx, FloatArgumentType.getFloat(ctx, "intensity"))))))
                    .then(Commands.literal("relationship")
                        .then(Commands.argument("a", StringArgumentType.word())
                            .then(Commands.argument("b", StringArgumentType.word())
                                .then(Commands.argument("axis", StringArgumentType.word())
                                    .then(Commands.argument("delta", IntegerArgumentType.integer())
                                        .executes(ErgenDebugCommand::mutateRelationship))))))
                    .then(Commands.literal("simulate")
                        .then(Commands.argument("ticks", IntegerArgumentType.integer(1, 24000))
                            .executes(ErgenDebugCommand::forceSimulate)))
                    .then(Commands.literal("breakthrough")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ErgenDebugCommand::forceBreakthrough)))
                    .then(Commands.literal("perception")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ErgenDebugCommand::forcePerception)))
                    // ── CRON-COMPLETIONIST-65: New inspection panels ──
                    .then(Commands.literal("memory")
                        .then(Commands.argument("id", StringArgumentType.word())
                            .executes(ErgenDebugCommand::showMemory)))
                    .then(Commands.literal("ecology")
                        .executes(ErgenDebugCommand::showEcology))
                    .then(Commands.literal("performance")
                        .executes(ErgenDebugCommand::showPerformance))
                    .then(Commands.literal("rumors")
                        .executes(ErgenDebugCommand::showRumors))
                    // ── CRON-COMPLETIONIST-65: New reality manipulation ──
                    .then(Commands.literal("weather")
                        .then(Commands.argument("type", StringArgumentType.word())
                            .executes(ErgenDebugCommand::forceWeather)))
                    .then(Commands.literal("time")
                        .then(Commands.argument("amount", StringArgumentType.word())
                            .executes(ErgenDebugCommand::advanceTime)))
                    .then(Commands.literal("rumor")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                            .executes(ErgenDebugCommand::spreadRumor)))
                    .then(Commands.literal("herb")
                        .executes(ErgenDebugCommand::matureHerb))
                    .then(Commands.literal("golden_save")
                        .executes(ErgenDebugCommand::goldenSave))
                    // ── CRON-COMPLETIONIST-73: Journal (WorldDeltaStore) inspection ──
                    .then(Commands.literal("journal")
                        .executes(ErgenDebugCommand::showJournalStatus)
                        .then(Commands.literal("query")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ErgenDebugCommand::journalQuery)))))
                        .then(Commands.literal("recent")
                            .executes(ctx -> journalRecent(ctx, 10))
                            .then(Commands.argument("count", IntegerArgumentType.integer(1, 100))
                                .executes(ctx -> journalRecent(ctx, IntegerArgumentType.getInteger(ctx, "count")))))
                        .then(Commands.literal("sim-write")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .then(Commands.argument("blockId", StringArgumentType.string())
                                            .executes(ErgenDebugCommand::journalSimWrite))))))
                        .then(Commands.literal("player-write")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .then(Commands.argument("blockId", StringArgumentType.string())
                                            .executes(ErgenDebugCommand::journalPlayerWrite))))))
                        .then(Commands.literal("clear")
                            .executes(ErgenDebugCommand::journalClear)))
                    // ── CRON-125: Canon structure composition (semantic Village→Building→Room→Furniture) ──
                    .then(Commands.literal("canon-build")
                        .executes(ErgenDebugCommand::canonBuildWangFamilyVillage))
                    // ── CRON-COMPLETIONIST-74: Residence simulation (WangLinHomeBuilder) ──
                    .then(Commands.literal("residence")
                        .then(Commands.literal("build")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ErgenDebugCommand::residenceBuild)))))
                        .then(Commands.literal("status")
                            .then(Commands.argument("x", IntegerArgumentType.integer())
                                .then(Commands.argument("y", IntegerArgumentType.integer())
                                    .then(Commands.argument("z", IntegerArgumentType.integer())
                                        .executes(ErgenDebugCommand::residenceStatus))))))
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
            "\u00a7eOne command. Inspect AND manipulate reality.\u00a7r"), false);
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
            "\u00a77Inspect:\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug actor <id>\u00a7r \u2014 full cognition chain"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug simulation\u00a7r \u2014 macro simulation state"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug blueprint\u00a7r \u2014 world blueprint status"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug events\u00a7r \u2014 eventbus diagnostics"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug relationships\u00a7r \u2014 NPC relationships"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug memory <id>\u00a7r \u2014 actor's cognitive memory"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug ecology\u00a7r \u2014 causal ecosystems"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug performance\u00a7r \u2014 JVM + entity stats"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug rumors\u00a7r \u2014 rumor network state"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Manipulate reality:\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug event <topic> [intensity]\u00a7r \u2014 inject a world event here"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug relationship <a> <b> <axis> <delta>\u00a7r \u2014 mutate graph"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug simulate <ticks>\u00a7r \u2014 fast-forward actor loop"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug breakthrough <id>\u00a7r \u2014 force cultivation breakthrough"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug perception <id>\u00a7r \u2014 force + show perception snapshot"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug weather <clear|rain|thunder|storm>\u00a7r \u2014 force weather"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug time <1_day|3_days|7_days|1_season>\u00a7r \u2014 advance time"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug rumor <text>\u00a7r \u2014 spread a rumor from your position"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug herb\u00a7r \u2014 mature nearest spirit herb"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug golden_save\u00a7r \u2014 save+reload persistence test"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal\u00a7r \u2014 WorldDeltaStore status + inspection"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal query <x> <y> <z>\u00a7r \u2014 deltas at a position"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal sim-write <x> <y> <z> <blockId>\u00a7r \u2014 manual SIM write"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug residence build <x> <y> <z>\u00a7r \u2014 build Wang Lin's home (SIM)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug residence status <x> <y> <z>\u00a7r \u2014 check residence + SIM delta count"), false);
        return 1;
    }

    // ── /ergen debug actor <id> — FULL cognition chain ───────────
    private static int showActor(CommandContext<CommandSourceStack> ctx) {
        String actorId = StringArgumentType.getString(ctx, "id");

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== ACTOR: " + actorId + " ===\u00a7r"), false);

        Optional<Actor> found = ActorRegistry.all().stream()
                .filter(a -> a.id.equalsIgnoreCase(actorId))
                .findFirst();

        if (found.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a78Actor '" + actorId + "' not found in ActorRegistry.\u00a7r"), false);
            return 1;
        }

        Actor actor = found.get();

        // ── Identity ──
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7b--- Identity ---\u00a7r"), false);
        sendLine(ctx, "Display Name", actor.displayName);
        sendLine(ctx, "Type", actor.type.name());
        sendLine(ctx, "Provenance", actor.provenance);
        sendLine(ctx, "Canon Confidence", actor.canonConfidence);
        if (actor.daoIdentity != null) sendLine(ctx, "Dao Identity", actor.daoIdentity.name());
        if (actor.beastTier != null) sendLine(ctx, "Beast Tier", actor.beastTier.name());
        sendLine(ctx, "Sim Level", actor.simLevel.name());
        sendLine(ctx, "Position", actor.blockX + ", " + actor.blockY + ", " + actor.blockZ);
        sendLine(ctx, "Capabilities", actor.capabilities.toString());

        // ── ARTICLE XXXV COGNITION CHAIN ──
        // Perception → Attention → Interpretation → Prediction → Goals → Intent → Plan → Activity
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7b--- Cognition Chain (Article XXXV) ---\u00a7r"), false);

        // 1. Perception (raw vs attended)
        PerceptionSnapshot rawPerception = actor.lastRawPerception;
        PerceptionSnapshot perception = actor.lastPerception;
        if (perception == null) {
            sendLine(ctx, "1. Perception", "\u00a78NONE (no cognition tick has run yet)\u00a7r");
        } else {
            // CRON-COMPLETIONIST-65: Show attention filter effect
            if (rawPerception != null && rawPerception.nearbyEntityCount() != perception.nearbyEntityCount()) {
                sendLine(ctx, "1. Perception", "\u00a7eATTENDED: " + perception.nearbyEntityCount()
                        + "/" + rawPerception.nearbyEntityCount() + " entities\u00a7r (attention filtered "
                        + (rawPerception.nearbyEntityCount() - perception.nearbyEntityCount()) + ")");
            } else {
                sendLine(ctx, "1. Perception", "\u00a7a" + perception + "\u00a7r");
            }
            sendLine(ctx, "   Radius", perception.perceptionRadiusBlocks + " blocks");
            sendLine(ctx, "   Nearby entities", perception.nearbyEntityCount()
                    + (perception.hasThreat ? " \u00a7c[THREAT]\u00a7r" : "")
                    + (perception.hasOpportunity ? " \u00a7a[OPP]\u00a7r" : "")
                    + (perception.isObserved ? " \u00a7e[OBSERVED]\u00a7r" : "")
                    + (perception.isAlone ? " \u00a77[ALONE]\u00a7r" : ""));
            for (PerceptionSnapshot.PerceivedEntity pe : perception.nearbyEntities) {
                String color = switch (pe.classification) {
                    case "hostile" -> "\u00a7c";
                    case "prey" -> "\u00a7a";
                    case "witness" -> "\u00a7e";
                    case "ally" -> "\u00a7b";
                    default -> "\u00a77";
                };
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "     " + color + "[" + pe.classification + "]\u00a7r "
                    + pe.entityType + " '" + pe.displayName + "' @ "
                    + Math.round(pe.distanceBlocks) + "b (pow " + String.format("%.2f", pe.relativePower) + ")"), false);
            }
            if (!perception.nearbyEvents.isEmpty()) {
                sendLine(ctx, "   Nearby events", String.valueOf(perception.nearbyEventCount()));
                for (PerceptionSnapshot.PerceivedEvent ev : perception.nearbyEvents) {
                    ctx.getSource().sendSuccess(() -> Component.literal(
                        "     \u00a7d" + ev.topic + "\u00a7r: " + ev.description
                        + " @" + Math.round(ev.distanceBlocks) + "b ("
                        + (ev.ageTicks / 20) + "s ago)"), false);
                }
            }
            sendLine(ctx, "   Environment", (perception.isNight ? "night " : "day ")
                    + (perception.isUnderground ? "underground " : "surface ")
                    + perception.biomeOrLocation);
            sendLine(ctx, "   Build time", (perception.buildTimeNanos / 1000) + "us");
        }

        // 2. Interpretation
        Interpretation interp = actor.lastInterpretation;
        if (interp == null) {
            sendLine(ctx, "2. Interpretation", "\u00a78NONE\u00a7r");
        } else {
            String color = switch (interp.category) {
                case THREAT_TO_LIFE -> "\u00a7c";
                case MINOR_NUISANCE -> "\u00a76";
                case PREY_DETECTED -> "\u00a7a";
                case WITNESS_RISK -> "\u00a7e";
                case SOCIAL_OPPORTUNITY -> "\u00a7b";
                case RESOURCE_OPPORTUNITY -> "\u00a7d";
                case SAFE_TO_ACT -> "\u00a7a";
                case UNEVENTFUL -> "\u00a77";
            };
            sendLine(ctx, "2. Interpretation", color + interp.category + "\u00a7r urg="
                    + String.format("%.2f", interp.urgency));
            ctx.getSource().sendSuccess(() -> Component.literal(
                "     \u00a77" + interp.summary + "\u00a7r"), false);
            if (interp.suggestedGoalOverride != null) {
                sendLine(ctx, "   Suggests goal", interp.suggestedGoalOverride.name());
            }
        }

        // 3. Prediction
        if (actor.lastPrediction != null) {
            var p = actor.lastPrediction;
            sendLine(ctx, "3. Prediction", "\u00a7d" + p.action.label + "\u00a7r "
                    + "pS=" + String.format("%.2f", p.pSuccess) + " "
                    + "pI=" + String.format("%.2f", p.pInjury) + " "
                    + "pW=" + String.format("%.2f", p.pWitnessed) + " "
                    + "EV=" + String.format("%+.2f", p.expectedValue));
        } else {
            sendLine(ctx, "3. Prediction", "\u00a78NONE\u00a7r");
        }

        // 4. Goal
        Ontology cog = actor.cognition;
        if (cog != null && cog.activeGoal != null) {
            sendLine(ctx, "4. Goal", "\u00a7b" + cog.activeGoal.category + "\u00a7r u="
                    + String.format("%.2f", cog.activeGoal.urgency) + " p="
                    + String.format("%.2f", cog.activeGoal.priority));
            ctx.getSource().sendSuccess(() -> Component.literal(
                "     \u00a77" + cog.activeGoal.description + "\u00a7r"), false);
        } else {
            sendLine(ctx, "4. Goal", "\u00a78NONE\u00a7r");
        }

        // 5. Intent — CRON-COMPLETIONIST-14: Read from Ontology field directly.
        if (cog != null && cog.activeIntent != null) {
            sendLine(ctx, "5. Intent", "\u00a7e" + cog.activeIntent.nature().label + "\u00a7r"
                    + " target=\"" + cog.activeIntent.targetId() + "\"");
            sendLine(ctx, "   Duration", cog.activeIntent.expectedDurationTicks() + " ticks ("
                    + (cog.activeIntent.expectedDurationTicks() / 20) + "s)");
        } else {
            sendLine(ctx, "5. Intent", "\u00a78NONE\u00a7r");
        }

        // 6. Tasks (CRON-COMPLETIONIST-65: show decomposed task queue)
        if (!actor.activeTasks.isEmpty()) {
            sendLine(ctx, "6. Tasks", actor.activeTasks.size() + " tasks queued");
            for (int i = 0; i < actor.activeTasks.size(); i++) {
                final CultivationTask task = actor.activeTasks.get(i);
                final int idx = i;
                final String marker = (i == actor.currentTaskIndex) ? "\u00a7a>\u00a7r" : "  ";
                ctx.getSource().sendSuccess(() -> Component.literal(
                        marker + idx + ". \u00a7b" + task.type + "\u00a7r "
                        + (task.targetPos != null ? task.targetPos.toShortString() : "")
                        + " \u00a78(" + task.status + ")\u00a7r"), false);
            }
        } else {
            sendLine(ctx, "6. Tasks", "\u00a77(none — IntentDecomposer not yet run)\u00a7r");
        }

        // 7. Activity
        if (actor.currentActivity != null) {
            sendLine(ctx, "7. Activity", "\u00a7a" + actor.currentActivity.activityType
                    + "\u00a7r [" + actor.currentActivity.state + "] "
                    + Math.round(actor.currentActivity.progress * 100) + "%");
        } else {
            sendLine(ctx, "7. Activity", "\u00a78NONE (idle)\u00a7r");
        }

        // ── Cultivator Mind (legacy motivations) ──
        var mind = CultivatorMindRegistry.get(actorId);
        if (mind != null) {
            ctx.getSource().sendSuccess(() -> Component.literal(""), false);
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

        return 1;
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

        // Cognition chain coverage — how many actors have run the full pipeline.
        int withPerception = 0, withInterpretation = 0, withPrediction = 0;
        for (Actor a : ActorRegistry.all()) {
            if (a.lastPerception != null) withPerception++;
            if (a.lastInterpretation != null) withInterpretation++;
            if (a.lastPrediction != null) withPrediction++;
        }
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77--- Article XXXV Cognition Chain Coverage ---\u00a7r"), false);
        sendLine(ctx, "Actors with perception", withPerception + "/" + ActorRegistry.all().size());
        sendLine(ctx, "Actors with interpretation", withInterpretation + "/" + ActorRegistry.all().size());
        sendLine(ctx, "Actors with prediction", withPrediction + "/" + ActorRegistry.all().size());

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
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

    // ═══════════════════════════════════════════════════════════════
    //  REALITY MANIPULATION SUBCOMMANDS
    // ═══════════════════════════════════════════════════════════════

    // ── /ergen debug event <topic> [intensity] ───────────────────
    // Inject a WorldEvent at the command sender's position. Any subscriber
    // listening on that topic prefix will fire. This lets the developer
    // trigger ANY canon situation: a wolf attack, a spirit vein weakening,
    // a rumor spreading, a sect war — by injecting the right event.
    private static int injectEvent(CommandContext<CommandSourceStack> ctx, float intensity) {
        String topic = StringArgumentType.getString(ctx, "topic");
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = ctx.getSource().isPlayer()
                ? BlockPos.containing(ctx.getSource().getPosition())
                : new BlockPos(0, 64, 0);
        long tick = level.getGameTime();

        // Pick an energy type based on the topic prefix.
        EnergyType type = EnergyType.PHYSICAL;
        if (topic.startsWith("qi.") || topic.startsWith("spirit_vein.")) type = EnergyType.QI;
        else if (topic.startsWith("rumor.") || topic.startsWith("sect.")) type = EnergyType.SOCIAL;
        else if (topic.startsWith("beast.") || topic.startsWith("npc.")) type = EnergyType.PHYSICAL;

        WorldEventBus.publish(topic, type, pos, intensity,
                "[DEBUG] Injected event: " + topic, "debug_command", tick);

        final EnergyType finalType = type;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Injected event:\u00a7r " + topic
            + " \u00a77(" + finalType + ", intensity " + intensity
            + ") @ " + pos.getX() + "," + pos.getY() + "," + pos.getZ()), false);
        return 1;
    }

    // ── /ergen debug relationship <a> <b> <axis> <delta> ────────
    // Mutate the relationship graph between two actors. Axis is one of:
    // trust, respect, fear, familiarity, debt, grievance. Delta is signed.
    private static int mutateRelationship(CommandContext<CommandSourceStack> ctx) {
        String a = StringArgumentType.getString(ctx, "a");
        String b = StringArgumentType.getString(ctx, "b");
        String axis = StringArgumentType.getString(ctx, "axis").toLowerCase();
        int delta = IntegerArgumentType.getInteger(ctx, "delta");

        ServerLevel level = ctx.getSource().getLevel();
        var store = dev.ergenverse.simulation.action.ActorRelationshipStore.get(level);
        if (store == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cActorRelationshipStore not loaded.\u00a7r"), false);
            return 0;
        }
        long tick = level.getGameTime();

        Integer trust = null, respect = null, fear = null, fam = null, debt = null, griev = null;
        switch (axis) {
            case "trust" -> trust = delta;
            case "respect" -> respect = delta;
            case "fear" -> fear = delta;
            case "familiarity", "fam" -> fam = delta;
            case "debt" -> debt = delta;
            case "grievance", "griev" -> griev = delta;
            default -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "\u00a7cUnknown axis '" + axis + "'. Use: trust, respect, fear, familiarity, debt, grievance\u00a7r"), false);
                return 0;
            }
        }
        store.recordMultiAxis(a, b, trust, respect, fear, fam, debt, griev,
                "[DEBUG] Manual relationship edit", tick);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Relationship mutated:\u00a7r " + a + " -> " + b
            + " " + axis + " " + (delta >= 0 ? "+" : "") + delta), false);
        return 1;
    }

    // ── /ergen debug simulate <ticks> ────────────────────────────
    // Fast-forward the actor tick loop by N ticks. This forces every
    // FULL_COGNITION actor to re-run the perception → prediction chain.
    private static int forceSimulate(CommandContext<CommandSourceStack> ctx) {
        int ticks = IntegerArgumentType.getInteger(ctx, "ticks");
        ServerLevel level = ctx.getSource().getLevel();
        long baseTick = level.getGameTime();

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7e[DEBUG] Simulating " + ticks + " ticks of actor cognition...\u00a7r"), false);

        int actorsRun = 0;
        for (int i = 0; i < ticks; i++) {
            long t = baseTick + i;
            // Force seasonal tick on the last iteration so DecisionEngine fires.
            if (i == ticks - 1) {
                // Make it a seasonal tick by using a multiple of SEASON_TICKS.
                t = (t / ActorTickLoop.SEASON_TICKS + 1) * ActorTickLoop.SEASON_TICKS;
            }
            for (Actor a : ActorRegistry.all()) {
                if (a.simLevel.order >= dev.ergenverse.simulation.los.SimulationLevel.ACTIVE_ACTOR.order) {
                    actorsRun++;
                }
            }
            ActorTickLoop.tick(t, level);
        }

        final int finalActors = actorsRun;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Simulated " + ticks + " ticks. ~" + finalActors
            + " actor-ticks processed.\u00a7r"), false);
        return 1;
    }

    // ── /ergen debug breakthrough <id> ───────────────────────────
    // Force a cultivation breakthrough for an actor. Advances realm order by 1.
    private static int forceBreakthrough(CommandContext<CommandSourceStack> ctx) {
        String actorId = StringArgumentType.getString(ctx, "id");
        Optional<Actor> found = ActorRegistry.all().stream()
                .filter(a -> a.id.equalsIgnoreCase(actorId))
                .findFirst();
        if (found.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cActor '" + actorId + "' not found.\u00a7r"), false);
            return 0;
        }
        Actor actor = found.get();
        if (actor.cognition == null) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cActor has no cognition state.\u00a7r"), false);
            return 0;
        }
        int oldRealm = actor.cognition.cultivation.realmOrder();
        int newRealm = oldRealm + 1;
        actor.cognition.cultivation.setRealmOrder(newRealm);
        actor.cognition.cultivation.setQiFraction(1.0);
        actor.cognition.cultivation.setBreakthroughReadiness(0.0);

        // Publish a breakthrough event so subscribers (chronicle, Wang Lin's
        // observation, rumor network) all react.
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = new BlockPos(actor.blockX, actor.blockY, actor.blockZ);
        WorldEventBus.publish("npc.breakthrough", EnergyType.SPIRITUAL, pos, 1.0f,
                "[DEBUG] " + actor.displayName + " broke through to realm " + newRealm,
                "debug_command", level.getGameTime());

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] " + actor.displayName + " broke through:\u00a7r realm "
            + oldRealm + " -> " + newRealm + ". Event published."), false);
        return 1;
    }

    // ── /ergen debug perception <id> ─────────────────────────────
    // Force a perception snapshot NOW for an actor and display it.
    // Useful for verifying the perception layer without waiting for a tick.
    private static int forcePerception(CommandContext<CommandSourceStack> ctx) {
        String actorId = StringArgumentType.getString(ctx, "id");
        Optional<Actor> found = ActorRegistry.all().stream()
                .filter(a -> a.id.equalsIgnoreCase(actorId))
                .findFirst();
        if (found.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cActor '" + actorId + "' not found.\u00a7r"), false);
            return 0;
        }
        Actor actor = found.get();
        ServerLevel level = ctx.getSource().getLevel();
        long tick = level.getGameTime();

        PerceptionSnapshot snap = PerceptionSensor.sense(actor, level, tick);
        actor.lastPerception = snap;

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== FORCED PERCEPTION: " + actorId + " ===\u00a7r"), false);
        sendLine(ctx, "Snapshot", snap.toString());
        sendLine(ctx, "Radius", snap.perceptionRadiusBlocks + " blocks");
        sendLine(ctx, "Entities", snap.nearbyEntityCount()
                + (snap.hasThreat ? " \u00a7c[THREAT]\u00a7r" : "")
                + (snap.hasOpportunity ? " \u00a7a[OPP]\u00a7r" : "")
                + (snap.isObserved ? " \u00a7e[OBSERVED]\u00a7r" : "")
                + (snap.isAlone ? " \u00a77[ALONE]\u00a7r" : ""));
        for (PerceptionSnapshot.PerceivedEntity pe : snap.nearbyEntities) {
            String color = switch (pe.classification) {
                case "hostile" -> "\u00a7c";
                case "prey" -> "\u00a7a";
                case "witness" -> "\u00a7e";
                case "ally" -> "\u00a7b";
                default -> "\u00a77";
            };
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  " + color + "[" + pe.classification + "]\u00a7r "
                + pe.entityType + " '" + pe.displayName + "' @ "
                + Math.round(pe.distanceBlocks) + "b (pow " + String.format("%.2f", pe.relativePower) + ")"), false);
        }
        if (!snap.nearbyEvents.isEmpty()) {
            sendLine(ctx, "Events", String.valueOf(snap.nearbyEventCount()));
            for (PerceptionSnapshot.PerceivedEvent ev : snap.nearbyEvents) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  \u00a7d" + ev.topic + "\u00a7r: " + ev.description
                    + " @" + Math.round(ev.distanceBlocks) + "b ("
                    + (ev.ageTicks / 20) + "s ago)"), false);
            }
        }
        sendLine(ctx, "Build time", (snap.buildTimeNanos / 1000) + "us");
        return 1;
    }

    // ── /ergen debug memory <id> ──────────────────────────────────
    // Show an actor's cognitive memory (from NpcCognitiveMemory).
    private static int showMemory(CommandContext<CommandSourceStack> ctx) {
        String actorId = StringArgumentType.getString(ctx, "id");
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== MEMORY: " + actorId + " ===\u00a7r"), false);

        var memory = dev.ergenverse.npc.memory.NpcCognitiveMemory.get(level);
        if (memory == null) {
            sendLine(ctx, "NpcCognitiveMemory", "\u00a78NOT LOADED\u00a7r");
            return 1;
        }

        // Show memory status (NpcCognitiveMemory API varies; show size only)
        sendLine(ctx, "Memory Store", "loaded for level");
        sendLine(ctx, "Note", "Detailed memory query API not yet stable");

        return 1;
    }

    // ── /ergen debug ecology ────────────────────────────────────
    private static int showEcology(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== CAUSAL ECOLOGY ===\u00a7r"), false);
        var ecosystems = CausalEcology.all();
        if (ecosystems.isEmpty()) {
            sendLine(ctx, "Ecosystems", "\u00a77(none registered)\u00a7r");
        } else {
            int i = 0;
            for (var eco : ecosystems) {
                final int idx = ++i;
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  \u00a7a#" + idx + "\u00a7r — " + eco.getClass().getSimpleName()), false);
            }
        }
        return 1;
    }

    // ── /ergen debug performance ──────────────────────────────────
    private static int showPerformance(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== PERFORMANCE ===\u00a7r"), false);

        // Entity count — use getEntityCount() if available, else skip
        sendLine(ctx, "Actors", ActorRegistry.all().size() + " registered");
        sendLine(ctx, "EventBus Subscribers", String.valueOf(WorldEventBus.subscriberCount()));
        sendLine(ctx, "Tick Rate", "(use /forge tps)");

        // Cognition chain latency — measure from the last tick
        long startNs = System.nanoTime();
        long endNs = startNs; // placeholder — real measurement needs periodic tracking
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Note: Run /ergen debug actor wang_lin to see per-actor cognition timing.\u00a7r"), false);

        // Memory estimates
        Runtime rt = Runtime.getRuntime();
        long usedMB = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMB = rt.maxMemory() / (1024 * 1024);
        sendLine(ctx, "JVM Memory", usedMB + " / " + maxMB + " MB");

        return 1;
    }

    // ── /ergen debug rumors ──────────────────────────────────────
    private static int showRumors(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== RUMOR NETWORK ===\u00a7r"), false);

        var network = RumorNetwork.get(level);
        if (network == null) {
            sendLine(ctx, "RumorNetwork", "\u00a78NOT LOADED\u00a7r");
            return 1;
        }

        int count = network.getTotalCount();
        sendLine(ctx, "Active Rumors", String.valueOf(count));
        int carriers = network.getInformedNpcCount();
        sendLine(ctx, "Carriers", String.valueOf(carriers));

        // Show most recent rumors
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Recent rumors (up to 5):\u00a7r"), false);
        List<?> allRumors = network.getRumorsKnownBy("");
        if (allRumors != null) {
            allRumors.sort((a, b) -> Long.compare(
                    ((dev.ergenverse.npc.rumor.Rumor) b).getBornTick(),
                    ((dev.ergenverse.npc.rumor.Rumor) a).getBornTick()));
            for (int i = 0; i < Math.min(5, allRumors.size()); i++) {
                var r = (dev.ergenverse.npc.rumor.Rumor) allRumors.get(i);
                ctx.getSource().sendSuccess(() -> Component.literal(
                        "  \u00a7d" + r.getRumorId() + "\u00a7r: "
                        + r.getCurrentContent().substring(0, Math.min(60, r.getCurrentContent().length()))
                        + " \u00a78(hops=" + r.getHopCount() + ")\u00a7r"), false);
            }
        }

        return 1;
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEW REALITY MANIPULATION SUBCOMMANDS
    // ═══════════════════════════════════════════════════════════════

    // ── /ergen debug weather <type> ──────────────────────────────
    private static int forceWeather(CommandContext<CommandSourceStack> ctx) {
        String type = StringArgumentType.getString(ctx, "type").toLowerCase();
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = ctx.getSource().isPlayer()
                ? BlockPos.containing(ctx.getSource().getPosition())
                : new BlockPos(0, 64, 0);
        long tick = level.getGameTime();

        String weatherType;
        switch (type) {
            case "clear" -> { level.setWeatherParameters(6000, 0, false, false); weatherType = "CLEAR"; }
            case "rain" -> { level.setWeatherParameters(0, 0, true, false); weatherType = "RAIN"; }
            case "thunder" -> { level.setWeatherParameters(0, 0, true, true); weatherType = "THUNDER"; }
            case "storm" -> { level.setWeatherParameters(0, 0, true, true); weatherType = "STORM"; }
            default -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "\u00a7cUnknown weather type. Use: clear, rain, thunder, storm\u00a7r"), false);
                return 0;
            }
        }

        WorldEventBus.publish("weather." + weatherType.toLowerCase(), EnergyType.PHYSICAL, pos, 0.8f,
                "[DEBUG] Weather forced to " + weatherType, "debug_command", tick);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Weather set to:\u00a7r " + weatherType), false);
        return 1;
    }

    // ── /ergen debug time <amount> ──────────────────────────────
    private static int advanceTime(CommandContext<CommandSourceStack> ctx) {
        String amount = StringArgumentType.getString(ctx, "amount").toLowerCase();
        ServerLevel level = ctx.getSource().getLevel();
        long currentTick = level.getGameTime();
        long advanceTicks;

        switch (amount) {
            case "1_hour" -> advanceTicks = 1000L;
            case "1_day" -> advanceTicks = 24000L;
            case "3_days" -> advanceTicks = 72000L;
            case "7_days" -> advanceTicks = 168000L;
            case "1_season" -> advanceTicks = 7L * 24000L;
            case "1_month" -> advanceTicks = 30L * 24000L;
            case "1_year" -> advanceTicks = 365L * 24000L;
            default -> {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "\u00a7cUnknown time amount. Use: 1_hour, 1_day, 3_days, 7_days, 1_season, 1_month, 1_year\u00a7r"), false);
                return 0;
            }
        }

        // Run the actor tick loop for the advanced time.
        BlockPos pos = ctx.getSource().isPlayer()
                ? BlockPos.containing(ctx.getSource().getPosition())
                : new BlockPos(0, 64, 0);

        int actorTicks = 0;
        long stepSize = Math.max(1L, advanceTicks / 100); // Batch in steps of 1%
        for (long t = currentTick; t < currentTick + advanceTicks; t += stepSize) {
            for (Actor a : ActorRegistry.all()) {
                if (a.simLevel.order >= dev.ergenverse.simulation.los.SimulationLevel.ACTIVE_ACTOR.order) {
                    actorTicks++;
                }
            }
            ActorTickLoop.tick(t, level);
        }

        final long finalAdvance = advanceTicks;
        final int finalTicks = actorTicks;
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Advanced time by " + (finalAdvance / 24000) + " days (" + finalAdvance + " ticks). ~" + finalTicks + " actor-ticks processed.\u00a7r"), false);
        return 1;
    }

    // ── /ergen debug rumor <text> ───────────────────────────────
    private static int spreadRumor(CommandContext<CommandSourceStack> ctx) {
        String text = StringArgumentType.getString(ctx, "text");
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = ctx.getSource().isPlayer()
                ? BlockPos.containing(ctx.getSource().getPosition())
                : new BlockPos(0, 64, 0);
        long tick = level.getGameTime();

        WorldEventBus.publish("rumor.spread", EnergyType.SOCIAL, pos, 0.6f,
                "[DEBUG] Rumor: " + text, "debug_command", tick);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Rumor injected:\u00a7r \"" + text + "\" @ " + pos.toShortString()), false);
        return 1;
    }

    // ── /ergen debug herb ───────────────────────────────────────
    private static int matureHerb(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        BlockPos pos = ctx.getSource().isPlayer()
                ? BlockPos.containing(ctx.getSource().getPosition())
                : new BlockPos(0, 64, 0);
        long tick = level.getGameTime();

        WorldEventBus.publish("opportunity.spirit_herb.mature", EnergyType.QI, pos, 0.7f,
                "[DEBUG] Spirit herb matured near player", "debug_command", tick);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a[DEBUG] Spirit herb matured event published near:\u00a7r " + pos.toShortString()), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Note: The OpportunitySubscriber will now assign nearby NPCs to investigate.\u00a7r"), false);
        return 1;
    }

    // ── /ergen debug golden_save ────────────────────────────────
    // The user's proposed smoke test: save → quit → reload → verify.
    // This command forces a world save and reports all subsystem states
    // so the user can verify persistence after reload.
    private static int goldenSave(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        long tick = level.getGameTime();

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== GOLDEN SAVE TEST ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Record these values. Reload the world. Run /ergen debug again. Compare.\u00a7r"), false);

        // Force-save all subsystems
        level.save(null, true, true);
        level.getServer().getPlayerList().saveAll();

        // Record subsystem states
        sendLine(ctx, "GameTime", String.valueOf(tick));
        sendLine(ctx, "DayTime", String.valueOf(level.getDayTime()));
        sendLine(ctx, "Blueprint", WorldBlueprintManager.isLoaded() ? "\u00a7aLOADED\u00a7r" : "\u00a78NOT LOADED\u00a7r");
        sendLine(ctx, "EventBus", "\u00a7a" + WorldEventBus.subscriberCount() + " subs\u00a7r");
        sendLine(ctx, "Actors", String.valueOf(ActorRegistry.all().size()));

        // Record actor-specific states
        for (Actor a : ActorRegistry.all()) {
            if (a.isCanon() && a.cognition != null) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "  \u00a7b[" + a.id + "]\u00a7r goal=" + (a.cognition.activeGoal != null ? a.cognition.activeGoal.category : "none")
                    + " realm=" + a.cognition.cultivation.realmOrder()
                    + " qi=" + String.format("%.2f", a.cognition.cultivation.qiFraction())), false);
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7eWorld saved. Now quit and reload. Run /ergen debug golden_save again.\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7eIf gameTime matches, memory persists, and goals resume → \u00a7aPASS\u00a7r\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7eIf anything resets → \u00a7cFAIL\u00a7r — the simulation isn't truly continuous.\u00a7r"), false);
        return 1;
    }

    // ── Helper ────────────────────────────────────────────────────
    private static void sendLine(CommandContext<CommandSourceStack> ctx, String key, String value) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77" + key + ":\u00a7r " + value), false);
    }

    // ══════════════════════════════════════════════════════════════════
    //  CRON-COMPLETIONIST-73: /ergen debug journal — WorldDeltaStore inspection
    // ══════════════════════════════════════════════════════════════════
    //
    //  The WorldDeltaStore is the simulation's journal of every change since
    //  day 0 — block edits (PLAYER + SIMULATION provenance), and (future) non-
    //  block deltas (actor moves, relationships, memories). Per CRON-69's
    //  "world as Git" contract: blueprint + journal = save. A fresh save
    //  starts with an empty journal; every playthrough begins from pure canon.
    //
    //  Prior to CRON-73, there was NO way for a player/admin to inspect this
    //  journal. The 3 simulation writers (SpiritBeastFeedGoal CRON-61,
    //  WeatherDamageSubscriber CRON-61, BlockPlacementEngine CRON-61) all
    //  route through WorldFacade.setSimulationBlock, but their writes were
    //  invisible — the player had to trust that the journal was being
    //  populated. This subcommand set closes that gap by exposing:
    //
    //    /ergen debug journal                  — status: counts by provenance
    //    /ergen debug journal query <x> <y> <z> — what's recorded at a pos
    //    /ergen debug journal recent [count]    — last N SIMULATION deltas
    //    /ergen debug journal sim-write <x> <y> <z> <blockId>
    //                                          — manually trigger a SIM write
    //    /ergen debug journal player-write <x> <y> <z> <blockId>
    //                                          — manually trigger a PLAYER write
    //    /ergen debug journal clear            — wipe the journal (NEW SAVE test)
    //
    //  The sim-write/player-write commands are the operational proof: run
    //  one, save+reload, run /ergen debug journal query <same pos> — if the
    //  delta is still there, the persistence loop is closed. This is the
    //  analog of /ergen debug golden_save but for the delta journal.

    /**
     * /ergen debug journal — show journal status: total deltas, breakdown by
     * provenance, journal size.
     */
    private static int showJournalStatus(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== WORLD DELTA JOURNAL ===\u00a7r"), false);

        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized. Load a world first.\u00a7r"), false);
            return 1;
        }
        WorldDeltaStore store = rt.deltaStore();

        sendLine(ctx, "Status", "\u00a7aINITIALIZED\u00a7r");
        sendLine(ctx, "Suzaku Level", rt.suzakuLevel() != null ? "\u00a7aBOUND\u00a7r" : "\u00a7cUNBOUND\u00a7r");
        sendLine(ctx, "Total Deltas (size())", String.valueOf(store.size()));
        sendLine(ctx, "PLAYER block deltas", String.valueOf(store.blockChangeCount(Provenance.PLAYER)));
        sendLine(ctx, "SIMULATION block deltas", String.valueOf(store.blockChangeCount(Provenance.SIMULATION)));
        sendLine(ctx, "CANON block deltas", String.valueOf(store.blockChangeCount(Provenance.CANON))
                + " \u00a78(should always be 0 — canon is immutable)\u00a7r");

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Journal inspection:\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal query <x> <y> <z>\u00a7r \u2014 what's recorded at a position"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal recent [count]\u00a7r \u2014 last N SIMULATION deltas"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Operational proof (persistence test):\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal sim-write <x> <y> <z> <blockId>\u00a7r \u2014 write a SIM delta"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal player-write <x> <y> <z> <blockId>\u00a7r \u2014 write a PLAYER delta"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a7e/ergen debug journal clear\u00a7r \u2014 wipe journal (test fresh-save state)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "  \u00a77Then: save+reload, run /ergen debug journal query <pos> — if the delta persists, the loop is closed.\u00a7r"), false);
        return 1;
    }

    /**
     * /ergen debug journal query <x> <y> <z> — show what's recorded at a
     * position across all provenances, plus the live Minecraft block state.
     */
    private static int journalQuery(CommandContext<CommandSourceStack> ctx) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== JOURNAL QUERY: (" + x + ", " + y + ", " + z + ") ===\u00a7r"), false);

        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized.\u00a7r"), false);
            return 1;
        }
        WorldDeltaStore store = rt.deltaStore();

        // Query each provenance
        String playerState = store.getBlock(x, y, z, Provenance.PLAYER);
        String simState = store.getBlock(x, y, z, Provenance.SIMULATION);
        String canonState = store.getBlock(x, y, z, Provenance.CANON);

        sendLine(ctx, "PLAYER delta", playerState != null
                ? "\u00a7b" + playerState + "\u00a7r" : "\u00a78(none)\u00a7r");
        sendLine(ctx, "SIMULATION delta", simState != null
                ? "\u00a7a" + simState + "\u00a7r" : "\u00a78(none)\u00a7r");
        sendLine(ctx, "CANON delta", canonState != null
                ? "\u00a7c" + canonState + "\u00a7r (unexpected — canon is immutable)" : "\u00a78(none — expected)\u00a7r");

        // Resolution priority: PLAYER > SIMULATION > CANON
        String resolved;
        String resolvedProvenance;
        if (playerState != null) {
            resolved = playerState;
            resolvedProvenance = "PLAYER";
        } else if (simState != null) {
            resolved = simState;
            resolvedProvenance = "SIMULATION";
        } else {
            resolved = "(canon — no delta)";
            resolvedProvenance = "CANON";
        }
        sendLine(ctx, "Resolved (P>S>C)", "\u00a7e" + resolved + "\u00a7r (\u00a7" + resolvedProvenance.charAt(0) + resolvedProvenance + "\u00a7r)");

        // Live Minecraft state at this position
        ServerLevel level = rt.suzakuLevel();
        if (level != null) {
            try {
                var liveState = level.getBlockState(new BlockPos(x, y, z));
                String liveBlockId = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                        .getKey(liveState.getBlock()).toString();
                sendLine(ctx, "Live Minecraft", "\u00a7d" + liveBlockId + "\u00a7r");
            } catch (Throwable t) {
                sendLine(ctx, "Live Minecraft", "\u00a7cquery failed: " + t.getMessage() + "\u00a7r");
            }
        }

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Resolution priority: PLAYER > SIMULATION > CANON. The CompositeWorldLayer asks PlayerLayer first, then SimulationLayer, then BlueprintLayer.\u00a7r"), false);
        return 1;
    }

    /**
     * /ergen debug journal recent [count] — show the last N SIMULATION deltas.
     * Useful for confirming that beast harvests / weather damage / sect wall
     * expansion are actually being journaled.
     */
    private static int journalRecent(CommandContext<CommandSourceStack> ctx, int count) {
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== LAST " + count + " SIMULATION DELTAS ===\u00a7r"), false);

        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized.\u00a7r"), false);
            return 1;
        }
        WorldDeltaStore store = rt.deltaStore();

        var simChanges = store.blockChanges(Provenance.SIMULATION);
        if (simChanges.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a78No SIMULATION deltas in the journal.\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77This means no beast has harvested, no weather has damaged, and no residence has been placed yet.\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77To test: /ergen debug journal sim-write <x> <y> <z> minecraft:air\u00a7r"), false);
            return 1;
        }

        // Sort by packed position (deterministic order) and take last N
        var sorted = simChanges.entrySet().stream()
                .sorted((a, b) -> Long.compare(a.getKey(), b.getKey()))
                .limit(count)
                .toList();

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7a" + simChanges.size() + "\u00a7r total SIMULATION deltas. Showing first " + sorted.size() + ":"), false);

        for (var entry : sorted) {
            BlockChangeDelta d = entry.getValue();
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  \u00a7a[" + d.x() + ", " + d.y() + ", " + d.z() + "]\u00a7r \u2192 \u00a7b" + d.blockState() + "\u00a7r"), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Note: deltas are indexed by packed position (latest-wins per position). The 'recent' here is by position order, not by time — the journal does not preserve temporal order for block changes (only the latest state at each position matters).\u00a7r"), false);
        return 1;
    }

    /**
     * /ergen debug journal sim-write <x> <y> <z> <blockId> — manually trigger
     * a SIMULATION write through the WorldFacade. This is the operational
     * proof: write a delta, save+reload, query — if it persists, the
     * simulation-persistence loop is closed.
     */
    private static int journalSimWrite(CommandContext<CommandSourceStack> ctx) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        String blockId = StringArgumentType.getString(ctx, "blockId");

        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized.\u00a7r"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== SIMULATION WRITE ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Position:\u00a7r (" + x + ", " + y + ", " + z + ")"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Block:\u00a7r \u00a7b" + blockId + "\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Provenance:\u00a7r \u00a7aSIMULATION\u00a7r"), false);

        // CRON-COMPLETIONIST-94: validate via BlockStateCodec.parse so the
        // command accepts full state strings like "minecraft:chest[facing=north]".
        // The old validation used new ResourceLocation(blockId) which threw on '['.
        try {
            var parsed = dev.ergenverse.runtime.delta.BlockStateCodec.parse(blockId);
            if (parsed == null) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "\u00a7cBlock '" + blockId + "' not found in registry.\u00a7r"), false);
                return 0;
            }
        } catch (Throwable t) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cInvalid blockId '" + blockId + "': " + t.getMessage() + "\u00a7r"), false);
            return 0;
        }

        // Write through the facade (records to journal + mirrors to live level)
        rt.world().setSimulationBlock(x, y, z, blockId);

        // Verify it was recorded
        String recorded = rt.deltaStore().getBlock(x, y, z, Provenance.SIMULATION);
        boolean success = blockId.equals(recorded);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7aSUCCESS\u00a7r — delta journaled under SIMULATION provenance."), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7eTo verify persistence:\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  1. Save the world (Esc → Save and Quit to Title)"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  2. Reload the world"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  3. Run: \u00a7e/ergen debug journal query " + x + " " + y + " " + z + "\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  4. If SIMULATION delta still shows \u00a7b" + blockId + "\u00a7r → \u00a7aPERSISTENCE CONFIRMED\u00a7r"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cFAILED\u00a7r — write did not record. Recorded: " + recorded), false);
        }
        return success ? 1 : 0;
    }

    /**
     * /ergen debug journal player-write <x> <y> <z> <blockId> — manually
     * trigger a PLAYER write. Same as sim-write but PLAYER provenance. Useful
     * for testing the resolution priority (PLAYER > SIMULATION > CANON).
     */
    private static int journalPlayerWrite(CommandContext<CommandSourceStack> ctx) {
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        String blockId = StringArgumentType.getString(ctx, "blockId");

        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized.\u00a7r"), false);
            return 1;
        }

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== PLAYER WRITE ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Position:\u00a7r (" + x + ", " + y + ", " + z + ")"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Block:\u00a7r \u00a7b" + blockId + "\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Provenance:\u00a7r \u00a7bPLAYER\u00a7r (takes priority over SIMULATION and CANON)"), false);

        // CRON-COMPLETIONIST-94: validate via BlockStateCodec.parse so the
        // command accepts full state strings like "minecraft:chest[facing=north]".
        try {
            var parsed = dev.ergenverse.runtime.delta.BlockStateCodec.parse(blockId);
            if (parsed == null) {
                ctx.getSource().sendSuccess(() -> Component.literal(
                    "\u00a7cBlock '" + blockId + "' not found in registry.\u00a7r"), false);
                return 0;
            }
        } catch (Throwable t) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cInvalid blockId '" + blockId + "': " + t.getMessage() + "\u00a7r"), false);
            return 0;
        }

        rt.world().setPlayerBlock(x, y, z, blockId);

        String recorded = rt.deltaStore().getBlock(x, y, z, Provenance.PLAYER);
        boolean success = blockId.equals(recorded);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        if (success) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7aSUCCESS\u00a7r — delta journaled under PLAYER provenance."), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7eTo test resolution priority:\u00a7r write a SIMULATION delta at the same position, then query — the PLAYER delta wins."), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cFAILED\u00a7r — write did not record. Recorded: " + recorded), false);
        }
        return success ? 1 : 0;
    }

    /**
     * /ergen debug journal clear — wipe the journal entirely. This simulates
     * a fresh save (blueprint + empty journal). Use with caution — all player
     * edits and simulation writes since the last save are lost.
     */
    private static int journalClear(CommandContext<CommandSourceStack> ctx) {
        WorldRuntime rt = WorldRuntime.get();
        if (!rt.isInitialized()) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWorldRuntime not initialized.\u00a7r"), false);
            return 1;
        }
        WorldDeltaStore store = rt.deltaStore();

        int before = store.size();
        int playerBefore = store.blockChangeCount(Provenance.PLAYER);
        int simBefore = store.blockChangeCount(Provenance.SIMULATION);

        store.clear();

        int after = store.size();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== JOURNAL CLEARED ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Before:\u00a7r " + before + " total (" + playerBefore + " PLAYER, " + simBefore + " SIMULATION)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77After:\u00a7r " + after + " total"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a7eThe world is now in 'fresh save' state — blueprint + empty journal. Live Minecraft blocks are NOT reverted (only the journal is cleared). To revert live blocks, reload the world.\u00a7r"), false);
        return 1;
    }

    // ── /ergen debug residence [build|status] ──────────────────────
    // CRON-COMPLETIONIST-74: Wire WangLinHomeBuilder — the 3rd simulation writer
    // (beast herb-harvest = SpiritBeastFeedGoal, weather roof-damage =
    // WeatherDamageSubscriber, residence = WangLinHomeBuilder). Prior to CRON-74
    // WangLinHomeBuilder.build() was DEAD CODE — never called from anywhere.
    // BlockPlacementEngine.placeResidence() had 23 direct level.setBlock calls
    // that bypassed the setSimulationBlock journal. Both gaps are now closed.

    /**
     * {@code /ergen debug residence build <x> <y> <z>} — materialize Wang Lin's
     * childhood home at the given origin (northwest corner of the meditation
     * room). All blocks are journaled under SIMULATION provenance via
     * BlockPlacementEngine.setBlock → WorldRuntime.world().setSimulationBlock.
     *
     * <p>This is the operational proof that the residence simulation chain
     * (ResidentProfileLoader → ResidenceManifestBuilder → BlockPlacementEngine
     * → WorldFacade → WorldDeltaStore) works end-to-end. Combined with
     * /ergen debug journal, the player can: build the residence, save+reload,
     * query the journal to confirm SIMULATION deltas persisted, and verify the
     * residence materialized on chunk reload.
     *
     * <p>The isAlreadyBuilt guard (marker block at origin.offset(0,5,0),
     * journaled under SIMULATION) prevents double-builds.
     */
    // ── CRON-125: Canon structure composition ──────────────────────────
    // ── CRON-127: World Assembly Compiler pipeline ────────────────────

    /**
     * /ergen debug canon-build — materialize Wang Family Village via the
     * CRON-127 five-layer pipeline:
     * <pre>
     *   Canon (lore) → Semantic AST (CanonSettlement tree)
     *       → WorldAssembler → VoxelInstruction IR
     *       → VoxelMaterializer → MaterialResolver → ServerLevel
     * </pre>
     * Delegates to {@link CanonSettlementBuilder#buildWangFamilyVillage}.
     */
    private static int canonBuildWangFamilyVillage(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== CANON BUILD (CRON-127 World Assembly Compiler) ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Pipeline:\u00a7r Canon \u2192 Semantic AST \u2192 WorldAssembler \u2192 VoxelInstruction IR \u2192 VoxelMaterializer \u2192 ServerLevel"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Layers:\u00a7r L1 canon/structure (no Minecraft) \u2192 L2 assembly (IR + 4 libraries) \u2192 L3 materialization (MaterialResolver seam)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Settlement:\u00a7r Wang Family Village (\u738b\u6c0f\u6751)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Canon:\u00a7r \u8d75\u56fd\u67d0\u504f\u50fb\u5c0f\u5c71\u6751 (RI Ch.1-10). 'Wang Family Village' is mod-original."), false);
        try {
            int voxels = CanonSettlementBuilder.buildWangFamilyVillage(level, null);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7aSUCCESS\u00a7r — Wang Family Village compiled + materialized (" + voxels + " voxels written)."), false);
        } catch (Throwable t) {
            ctx.getSource().sendFailure(Component.literal(
                "\u00a7cFailed: " + t.getMessage() + "\u00a7r"));
        }
        return 1;
    }

    // ── CRON-COMPLETIONIST-74: Residence simulation ────────────────────

    private static int residenceBuild(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        BlockPos origin = new BlockPos(x, y, z);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== RESIDENCE BUILD ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Origin:\u00a7r (" + x + ", " + y + ", " + z + ")"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Resident:\u00a7r \u00a7bWang Lin\u00a7r (\u00a77wang_lin\u00a7r)"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Settlement:\u00a7r \u00a7bWang Family Village\u00a7r"), false);

        if (WangLinHomeBuilder.isAlreadyBuilt(level, origin)) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7eWang Lin's home is already built at " + origin
                + ". (marker block present). Use /ergen debug journal clear"
                + " then reload the world to rebuild.\u00a7r"), false);
            return 0;
        }

        WorldRuntime rt = WorldRuntime.get();
        boolean runtimeReady = rt.isInitialized() && rt.suzakuLevel() == level;
        int before = runtimeReady ? rt.deltaStore().blockChangeCount(Provenance.SIMULATION) : -1;
        if (!runtimeReady) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cWarning: WorldRuntime not initialized or level is not Planet Suzaku."
                + " Blocks will be placed via direct level.setBlock (NOT journaled)."
                + " Persistence will not work.\u00a7r"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77SIMULATION deltas before:\u00a7r " + before), false);
        }

        // Build the residence (all blocks journaled under SIMULATION)
        WangLinHomeBuilder.build(level, origin);

        if (runtimeReady) {
            int after = rt.deltaStore().blockChangeCount(Provenance.SIMULATION);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77SIMULATION deltas after:\u00a7r " + after
                + " (\u00a7a+" + (after - before) + "\u00a7r new)"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7aSUCCESS\u00a7r — Wang Lin's home materialized at " + origin + "."), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7eTo verify persistence:\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  1. Save the world (Esc \u2192 Save and Quit to Title)"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  2. Reload the world"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  3. Run \u00a7b/ergen debug residence status " + x + " " + y + " " + z + "\u00a7r"), false);
            ctx.getSource().sendSuccess(() -> Component.literal(
                "  4. Run \u00a7b/ergen debug journal recent 20\u00a7r to see the SIMULATION deltas"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7eBlocks placed (NOT journaled — runtime not ready)."
                + " Run on Planet Suzaku for full simulation persistence.\u00a7r"), false);
        }
        return 1;
    }

    /**
     * {@code /ergen debug residence status <x> <y> <z>} — check whether Wang
     * Lin's home is built at the given origin by testing for the marker block.
     * Also reports the SIMULATION delta count in the residence's footprint
     * (13\u00d75\u00d74 = ~260 blocks) to confirm the journal was populated.
     */
    private static int residenceStatus(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        int x = IntegerArgumentType.getInteger(ctx, "x");
        int y = IntegerArgumentType.getInteger(ctx, "y");
        int z = IntegerArgumentType.getInteger(ctx, "z");
        BlockPos origin = new BlockPos(x, y, z);

        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a76\u00a7l=== RESIDENCE STATUS ===\u00a7r"), false);
        ctx.getSource().sendSuccess(() -> Component.literal(
            "\u00a77Origin:\u00a7r (" + x + ", " + y + ", " + z + ")"), false);

        boolean built = WangLinHomeBuilder.isAlreadyBuilt(level, origin);
        BlockPos markerPos = origin.offset(0, 5, 0);
        String markerBlock = level.getBlockState(markerPos)
                .getBlock().getName().getString();

        if (built) {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7aBUILT\u00a7r — marker block (CHISELED_STONE_BRICKS) present at "
                + markerPos), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a7cNOT BUILT\u00a7r — marker at " + markerPos + " is "
                + markerBlock + " (expected CHISELED_STONE_BRICKS)"), false);
        }

        // Count SIMULATION deltas in the residence footprint (13x5x4)
        WorldRuntime rt = WorldRuntime.get();
        if (rt.isInitialized()) {
            int simDeltas = 0;
            for (int dx = 0; dx < 13; dx++) {
                for (int dy = 0; dy < 6; dy++) {
                    for (int dz = 0; dz < 5; dz++) {
                        BlockPos p = origin.offset(dx, dy, dz);
                        String recorded = rt.deltaStore().getBlock(
                                p.getX(), p.getY(), p.getZ(), Provenance.SIMULATION);
                        if (recorded != null) {
                            simDeltas++;
                        }
                    }
                }
            }
            final int simDeltaCount = simDeltas;
            ctx.getSource().sendSuccess(() -> Component.literal(
                "\u00a77SIMULATION deltas in footprint:\u00a7r " + simDeltaCount
                + " / ~260 (13\u00d75\u00d74 + marker)"), false);
        }

        return 1;
    }
}
