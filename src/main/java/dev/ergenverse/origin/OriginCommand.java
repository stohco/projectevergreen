package dev.ergenverse.origin;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public class OriginCommand {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("ergen").requires(s -> s.hasPermission(2))
            .then(Commands.literal("origin").then(Commands.literal("test").executes(OriginCommand::test))));
    }
    private static int test(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("aOrigin: stub"), false);
        return 1;
    }
}
