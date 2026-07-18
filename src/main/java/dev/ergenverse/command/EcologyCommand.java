package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.ecology.CausalEcology;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * EcologyCommand — {@code /ergen ecology <list|show|tick|seed>}
 *
 * <p>In-game command for inspecting and driving the CausalEcology system.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergen ecology list} — list all ecosystems with population summaries.</li>
 *   <li>{@code /ergen ecology show <name>} — show full detail of one ecosystem.</li>
 *   <li>{@code /ergen ecology tick} — manually advance one ecosystem tick.</li>
 *   <li>{@code /ergen ecology seed} — re-seed the canon RI ecosystems.</li>
 * </ul>
 */
public class EcologyCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> disp = event.getDispatcher();
        disp.register(Commands.literal("ergen")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("ecology")
                .then(Commands.literal("list")
                    .executes(EcologyCommand::listEcosystems))
                .then(Commands.literal("show")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(EcologyCommand::showEcosystem)))
                .then(Commands.literal("tick")
                    .executes(EcologyCommand::tickEcosystems))
                .then(Commands.literal("seed")
                    .executes(EcologyCommand::seedEcosystems))));
    }

    private static int listEcosystems(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (CausalEcology.all().isEmpty()) {
            src.sendSuccess(() -> Component.literal("No ecosystems registered.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        src.sendSuccess(() -> Component.literal("Ecosystems:")
                .withStyle(ChatFormatting.AQUA), false);
        for (CausalEcology.Ecosystem e : CausalEcology.all()) {
            final CausalEcology.Ecosystem fe = e;
            src.sendSuccess(() -> Component.literal(
                    "  " + fe.name
                    + "  F=" + (int) fe.flora
                    + " H=" + (int) fe.herbivores
                    + " P=" + (int) fe.predators
                    + " A=" + (int) fe.apex
                    + " S=" + (int) fe.sectCultivators
                    + " M=" + (int) fe.merchants
            ).withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int showEcosystem(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String name = StringArgumentType.getString(ctx, "name");
        CausalEcology.Ecosystem found = null;
        for (CausalEcology.Ecosystem e : CausalEcology.all()) {
            if (e.name.equals(name)) { found = e; break; }
        }
        if (found == null) {
            src.sendFailure(Component.literal("No ecosystem named " + name));
            return 0;
        }
        final CausalEcology.Ecosystem e = found;
        src.sendSuccess(() -> Component.literal("=== " + e.name + " ===")
                .withStyle(ChatFormatting.AQUA), false);
        src.sendSuccess(() -> Component.literal(
                "  Qi output: " + e.spiritVeinQiOutput
              + "  Flora: " + (int) e.flora
              + "  Herbivores: " + (int) e.herbivores
              + "  Predators: " + (int) e.predators
              + "  Apex: " + (int) e.apex
              + "  Sect: " + (int) e.sectCultivators
              + "  Merchants: " + (int) e.merchants
        ).withStyle(ChatFormatting.GRAY), false);
        if (!e.floraSpecies.isEmpty()) {
            src.sendSuccess(() -> Component.literal("  Flora species: " + String.join(", ", e.floraSpecies))
                    .withStyle(ChatFormatting.GRAY), false);
        }
        if (!e.recentEvents.isEmpty()) {
            src.sendSuccess(() -> Component.literal("  Recent events:")
                    .withStyle(ChatFormatting.GRAY), false);
            for (String ev : e.recentEvents) {
                final String fev = ev;
                src.sendSuccess(() -> Component.literal("    - " + fev)
                        .withStyle(ChatFormatting.DARK_GRAY), false);
            }
        }
        return 1;
    }

    private static int tickEcosystems(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        CausalEcology.tickAll();
        src.sendSuccess(() -> Component.literal("Ecosystem tick advanced.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static int seedEcosystems(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        dev.ergenverse.ecology.EcosystemSeeder.seed();
        src.sendSuccess(() -> Component.literal("Ecosystems re-seeded.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
