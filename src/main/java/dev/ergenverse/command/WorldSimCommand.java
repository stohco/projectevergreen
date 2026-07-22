package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.ecology.CausalEcology;
import dev.ergenverse.simulation.WorldSimState;
import dev.ergenverse.simulation.WorldStateDataLoader;
import dev.ergenverse.simulation.WorldStateEngine;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

/**
 * WorldSimCommand — {@code /ergen worldsim} — the debug window into the macro simulation.
 *
 * <p>Shows the live state of all four WorldStateEngine subsystems:
 * <ul>
 *   <li><b>time</b> — current world tick, season, year.</li>
 *   <li><b>migrations</b> — each migration, its current waypoint + days elapsed.</li>
 *   <li><b>ecosystems</b> — each ecosystem's seasonal state + CausalEcology populations.</li>
 *   <li><b>civilizations</b> — each civ's disciple count + economy level.</li>
 *   <li><b>opportunities</b> — each opportunity's age + matured status.</li>
 * </ul>
 *
 * <p>This is the primary debugging tool for the observable ecology/migration/civ
 * systems. When you run it, you see exactly what the simulation is doing RIGHT NOW.
 *
 * <p>Also provides {@code /ergen worldsim advance <days>} to fast-forward the
 * simulation by N days (useful for testing — triggers migration arrivals,
 * seasonal shifts, and opportunity maturation without waiting).
 *
 * <p><b>Constitution compliance:</b> Article XLII (Layer 3 observability),
 * Article XLIII (single-player maximalism — the simulation is debuggable
 * because it's all on one machine).
 */
public class WorldSimCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(2)) // op-only
                .then(Commands.literal("worldsim")
                    .executes(WorldSimCommand::showStatus)
                    .then(Commands.literal("advance")
                        .then(Commands.argument("days", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 365))
                            .executes(WorldSimCommand::advanceDays)))
                    .then(Commands.literal("migrations")
                        .executes(WorldSimCommand::showMigrations))
                    .then(Commands.literal("ecosystems")
                        .executes(WorldSimCommand::showEcosystems))
                    .then(Commands.literal("civilizations")
                        .executes(WorldSimCommand::showCivilizations))
                    .then(Commands.literal("opportunities")
                        .executes(WorldSimCommand::showOpportunities)))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergen worldsim command registered.");
    }

    // ─── Full status report ──────────────────────────────────────────

    private static int showStatus(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldSimState sim = WorldSimState.get(level);

        MutableComponent report = Component.literal("§6§l=== World Simulation Status ===§r\n");
        report.append(Component.literal("§eWorld Tick: §f" + WorldStateEngine.getWorldTick() + "§r\n"));
        report.append(Component.literal("§eSeason: §f" + WorldStateEngine.getCurrentSeason().name
                + " (" + WorldStateEngine.getCurrentSeason().nameCn + ")§r\n"));
        report.append(Component.literal("§eYear: §f" + (WorldStateEngine.getWorldTick() / 365) + "§r\n"));
        report.append(Component.literal("§eSim State: §7" + sim.getStatusReport() + "§r\n"));
        report.append(Component.literal("§eCausalEcology: §7" + CausalEcology.all().size() + " ecosystems active§r\n"));

        // Brief migration summary
        int migrated = sim.migrationIds().size();
        report.append(Component.literal("\n§6§lMigrations:§r §f" + migrated + " tracked§r"));
        // Brief civ summary
        int civs = sim.civilizationIds().size();
        report.append(Component.literal("\n§6§lCivilizations:§r §f" + civs + " tracked§r"));
        // Brief opportunity summary
        int opps = sim.opportunityIds().size();
        int matured = 0;
        for (String oppId : sim.opportunityIds()) {
            if (sim.isOpportunityMatured(oppId)) matured++;
        }
        report.append(Component.literal("\n§6§lOpportunities:§f " + opps + " tracked, " + matured + " matured§r"));

        report.append(Component.literal("\n\n§7Use /ergen worldsim <migrations|ecosystems|civilizations|opportunities> for details.§r"));
        report.append(Component.literal("\n§7Use /ergen worldsim advance <days> to fast-forward.§r"));

        ctx.getSource().sendSuccess(() -> report, false);
        return 1;
    }

    // ─── Migrations detail ───────────────────────────────────────────

    private static int showMigrations(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldSimState sim = WorldSimState.get(level);
        Map<String, com.google.gson.JsonObject> data = WorldStateDataLoader.getSubsystem("migrations");

        MutableComponent report = Component.literal("§6§l=== Migrations ===§r\n");
        if (data.isEmpty()) {
            report.append(Component.literal("§7No migration data loaded.§r"));
            ctx.getSource().sendSuccess(() -> report, false);
            return 0;
        }

        for (Map.Entry<String, com.google.gson.JsonObject> entry : data.entrySet()) {
            String id = entry.getKey();
            com.google.gson.JsonObject d = entry.getValue();
            String name = d.has("name") ? d.get("name").getAsString() : id;
            String species = d.has("species") ? d.get("species").getAsString() : "?";
            com.google.gson.JsonArray waypoints = d.has("waypoints") ? d.getAsJsonArray("waypoints") : null;

            int wpIdx = sim.getMigrationWaypointIndex(id);
            int days = sim.getMigrationDaysAtWaypoint(id);
            int totalWp = waypoints != null ? waypoints.size() : 0;
            String currentPoint = "?";
            int duration = 0;
            if (waypoints != null && wpIdx < waypoints.size()) {
                com.google.gson.JsonObject wp = waypoints.get(wpIdx).getAsJsonObject();
                currentPoint = wp.has("point") ? wp.get("point").getAsString() : "?";
                duration = wp.has("duration_days") ? wp.get("duration_days").getAsInt() : 30;
            }

            String progress = duration > 0 ? String.format("%d/%d days", days, duration) : days + " days";
            report.append(Component.literal(String.format("§b%s§r §7(%s)§r\n", name, species)));
            report.append(Component.literal(String.format("  §7Waypoint %d/%d: §f%s §7[%s]§r\n",
                    wpIdx + 1, totalWp, currentPoint, progress)));
        }

        ctx.getSource().sendSuccess(() -> report, false);
        return data.size();
    }

    // ─── Ecosystems detail ───────────────────────────────────────────

    private static int showEcosystems(CommandContext<CommandSourceStack> ctx) {
        MutableComponent report = Component.literal("§6§l=== Ecosystems (CausalEcology) ===§r\n");
        if (CausalEcology.all().isEmpty()) {
            report.append(Component.literal("§7No ecosystems active. Run /ergen worldsim advance 1 to seed.§r"));
            ctx.getSource().sendSuccess(() -> report, false);
            return 0;
        }

        for (CausalEcology.Ecosystem eco : CausalEcology.all()) {
            report.append(Component.literal(String.format("§b%s§r §7(qi=%.2f)§r\n", eco.name, eco.spiritVeinQiOutput)));
            report.append(Component.literal(String.format("  §aFlora:§f %.0f  §aHerb:§f %.0f  §aPred:§f %.0f  §aApex:§f %.1f§r\n",
                    eco.flora, eco.herbivores, eco.predators, eco.apex)));
            report.append(Component.literal(String.format("  §9Sect:§f %.0f  §9Merch:§f %.0f§r\n",
                    eco.sectCultivators, eco.merchants)));
            if (!eco.recentEvents.isEmpty()) {
                report.append(Component.literal("  §eLatest event:§r " + eco.recentEvents.get(0) + "§r\n"));
            }
        }

        ctx.getSource().sendSuccess(() -> report, false);
        return CausalEcology.all().size();
    }

    // ─── Civilizations detail ────────────────────────────────────────

    private static int showCivilizations(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldSimState sim = WorldSimState.get(level);
        Map<String, com.google.gson.JsonObject> data = WorldStateDataLoader.getSubsystem("civilizations");

        MutableComponent report = Component.literal("§6§l=== Civilizations ===§r\n");
        if (data.isEmpty()) {
            report.append(Component.literal("§7No civilization data loaded.§r"));
            ctx.getSource().sendSuccess(() -> report, false);
            return 0;
        }

        String[] econLabels = {"Collapsed", "Poor", "Moderate", "Rich", "Flourishing"};

        for (Map.Entry<String, com.google.gson.JsonObject> entry : data.entrySet()) {
            String id = entry.getKey();
            com.google.gson.JsonObject d = entry.getValue();
            String name = d.has("name") ? d.get("name").getAsString() : id;
            String type = d.has("type") ? d.get("type").getAsString() : "?";

            int disciples = sim.getCivilizationDisciples(id);
            int econLevel = sim.getCivilizationEconomyLevel(id);
            String econLabel = econLevel >= 0 && econLevel < econLabels.length ? econLabels[econLevel] : "?";

            report.append(Component.literal(String.format("§b%s§r §7(%s)§r\n", name, type)));
            report.append(Component.literal(String.format("  §aDisciples:§f %d  §aEconomy:§f %s§r\n",
                    disciples < 0 ? 0 : disciples, econLabel)));
        }

        ctx.getSource().sendSuccess(() -> report, false);
        return data.size();
    }

    // ─── Opportunities detail ────────────────────────────────────────

    private static int showOpportunities(CommandContext<CommandSourceStack> ctx) {
        ServerLevel level = ctx.getSource().getLevel();
        WorldSimState sim = WorldSimState.get(level);
        Map<String, com.google.gson.JsonObject> data = WorldStateDataLoader.getSubsystem("opportunities");

        MutableComponent report = Component.literal("§6§l=== Opportunities ===§r\n");
        if (data.isEmpty()) {
            report.append(Component.literal("§7No opportunity data loaded.§r"));
            ctx.getSource().sendSuccess(() -> report, false);
            return 0;
        }

        for (Map.Entry<String, com.google.gson.JsonObject> entry : data.entrySet()) {
            String id = entry.getKey();
            com.google.gson.JsonObject d = entry.getValue();
            String name = d.has("name") ? d.get("name").getAsString() : id;
            int reqAge = d.has("age_requirement_years") ? d.get("age_requirement_years").getAsInt() : 0;

            int age = sim.getOpportunityAgeYears(id);
            boolean matured = sim.isOpportunityMatured(id);

            String status = matured ? "§a§lMATURED§r" : (age >= reqAge ? "§eREADY§r" : "§7PENDING§r");
            report.append(Component.literal(String.format("§b%s§r %s\n", name, status)));
            report.append(Component.literal(String.format("  §7Age:§f %d/%d years§r\n", age, reqAge)));
        }

        ctx.getSource().sendSuccess(() -> report, false);
        return data.size();
    }

    // ─── Fast-forward ────────────────────────────────────────────────

    private static int advanceDays(CommandContext<CommandSourceStack> ctx) {
        int days = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "days");
        ServerLevel level = ctx.getSource().getLevel();

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§eFast-forwarding simulation by " + days + " days...§r"), false);

        WorldStateEngine.tick(level, days);

        ctx.getSource().sendSuccess(() -> Component.literal(
                "§aDone. World tick is now " + WorldStateEngine.getWorldTick()
                        + " (Season: " + WorldStateEngine.getCurrentSeason().name + ")§r\n"
                        + "§7Check /ergen chronicle for events that fired during the advance.§r"), false);
        return days;
    }
}
