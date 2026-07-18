package dev.ergenverse.command;

import dev.ergenverse.simulation.actor.*;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collection;

/**
 * ActorCommand — /ergen actor <list|territory|status>
 */
public class ActorCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("ergen")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("actor")
                    .then(Commands.literal("status")
                        .executes(ActorCommand::status))
                    .then(Commands.literal("list")
                        .executes(ActorCommand::list))
                )
        );
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        Collection<Actor> actors = ActorRegistry.all();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§aActor Registry: " + actors.size() + " actors"), false);
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> ctx) {
        Collection<Actor> actors = ActorRegistry.all();
        ctx.getSource().sendSuccess(() -> Component.literal(
                "§aActors (" + actors.size() + "):"), false);
        for (Actor a : actors) {
            final Actor actor = a;
            final String desc = "  §7" + actor.id + "§r §f" + actor.displayName +
                    "§r §8(" + actor.type + ")" +
                    (actor.daoIdentity != null ? " §e[" + actor.daoIdentity + "]" : "") +
                    (actor.beastTier != null ? " §d[" + actor.beastTier + "]" : "");
            ctx.getSource().sendSuccess(() -> Component.literal(desc), false);
        }
        return actors.size();
    }
}
