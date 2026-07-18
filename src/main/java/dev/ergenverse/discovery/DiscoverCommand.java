package dev.ergenverse.discovery;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DiscoverCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("discover")
                    .then(Commands.literal("list")
                        .executes(DiscoverCommand::list)))
        );
    }
    
    private static int list(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("aDiscovery: stub"), false);
        return 1;
    }
}
