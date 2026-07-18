package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.simulation.LocationLayerRegistry;
import dev.ergenverse.simulation.LocationLayers;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * LocationLayersCommand — {@code /ergen layers <list|show|reload>}
 *
 * <p>In-game command for inspecting the Location Layers system.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergen layers list} — list all registered locations and their
 *       populated layer count.</li>
 *   <li>{@code /ergen layers show <locationId>} — dump all 15 layers of a
 *       specific location.</li>
 *   <li>{@code /ergen layers reload} — re-seed the canon locations (mostly
 *       for development).</li>
 * </ul>
 */
public class LocationLayersCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> disp = event.getDispatcher();
        disp.register(Commands.literal("ergen")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("layers")
                .then(Commands.literal("list")
                    .executes(LocationLayersCommand::listLocations))
                .then(Commands.literal("show")
                    .then(Commands.argument("locationId", com.mojang.brigadier.arguments.StringArgumentType.string())
                        .executes(LocationLayersCommand::showLocation)))
                .then(Commands.literal("reload")
                    .executes(LocationLayersCommand::reload))));
    }

    private static int listLocations(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        if (LocationLayerRegistry.count() == 0) {
            src.sendSuccess(() -> Component.literal("No locations registered.")
                    .withStyle(ChatFormatting.YELLOW), false);
            return 0;
        }
        src.sendSuccess(() -> Component.literal("Registered locations:")
                .withStyle(ChatFormatting.AQUA), false);
        for (String id : LocationLayerRegistry.allLocationIds()) {
            LocationLayers layers = LocationLayerRegistry.get(id);
            int populated = layers == null ? 0 : layers.populatedCount();
            final String fid = id;
            final int fpop = populated;
            src.sendSuccess(() -> Component.literal("  " + fid + "  [" + fpop + "/15 layers]")
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int showLocation(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        String id = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "locationId");
        LocationLayers layers = LocationLayerRegistry.get(id);
        if (layers == null) {
            src.sendFailure(Component.literal("No such location: " + id));
            return 0;
        }
        src.sendSuccess(() -> Component.literal("=== " + id + " ===")
                .withStyle(ChatFormatting.AQUA), false);
        for (LocationLayers.LayerId layer : LocationLayers.LayerId.values()) {
            String data = layers.get(layer);
            if (data == null) data = "<unpopulated>";
            final String lname = layer.name();
            final String ldata = data;
            src.sendSuccess(() -> Component.literal("  " + lname + ": " + ldata)
                    .withStyle(ChatFormatting.GRAY), false);
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        dev.ergenverse.simulation.LocationLayerSeeder.seed();
        src.sendSuccess(() -> Component.literal("Location layers re-seeded ("
                + LocationLayerRegistry.count() + " locations).")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }
}
