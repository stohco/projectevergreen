package dev.ergenverse.spawn;

import com.mojang.brigadier.CommandDispatcher;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * ErgenverseCommand — {@code /ergenverse} diagnostic command.
 *
 * <p>Provides a way for the player to verify the mod is loaded and manually
 * trigger the village build + tutorial book if the automatic first-spawn
 * handler didn't fire.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergenverse status} — Prints whether the mod is loaded, the
 *       village build status, and the player's tutorial-given flag.</li>
 *   <li>{@code /ergenverse village} — Builds the Wang Family Village at the
 *       player's current position (or world spawn if run from console).</li>
 *   <li>{@code /ergenverse book} — Gives the player the tutorial book.</li>
 *   <li>{@code /ergenverse reset} — Clears the player's tutorial-given flag
 *       so the first-spawn handler will fire again on next login.</li>
 * </ul>
 *
 * <p>No permission requirement — any player can run it. This is an alpha
 * mod for testing; the commands are intentionally open.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public final class ErgenverseCommand {

    private ErgenverseCommand() {}

    /** Per-player NBT flag — must match SpawnEventHandler.NBT_TUTORIAL_GIVEN. */
    private static final String NBT_TUTORIAL_GIVEN = "ergenverse.tutorial_given";

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> disp = event.getDispatcher();
        disp.register(Commands.literal("ergenverse")
            .then(Commands.literal("status")
                .executes(ctx -> status(ctx.getSource())))
            .then(Commands.literal("village")
                .executes(ctx -> buildVillage(ctx.getSource())))
            .then(Commands.literal("book")
                .executes(ctx -> giveBook(ctx.getSource())))
            .then(Commands.literal("reset")
                .executes(ctx -> resetFlag(ctx.getSource())))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergenverse command registered (status|village|book|reset).");
    }

    /** /ergenverse status — print mod load + village + tutorial flag state. */
    private static int status(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("=== Ergenverse Status ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        src.sendSuccess(() -> Component.literal("Mod loaded: YES")
                .withStyle(ChatFormatting.GREEN), false);

        ServerLevel level = src.getLevel();
        boolean villageBuilt = WangFamilyVillageBuilder.isAlreadyBuilt(level);
        src.sendSuccess(() -> Component.literal("Village built at spawn: "
                + (villageBuilt ? "YES" : "NO"))
                .withStyle(villageBuilt ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);

        BlockPos spawn = level.getSharedSpawnPos();
        src.sendSuccess(() -> Component.literal("World spawn: " + spawn.getX() + ", "
                + spawn.getY() + ", " + spawn.getZ()), false);

        ServerPlayer player = src.getPlayer();
        if (player != null) {
            boolean flag = player.getPersistentData().getBoolean(NBT_TUTORIAL_GIVEN);
            src.sendSuccess(() -> Component.literal("Tutorial book given: "
                    + (flag ? "YES" : "NO"))
                    .withStyle(flag ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);
        }

        src.sendSuccess(() -> Component.literal("Commands: /ergenverse <status|village|book|reset>")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    /** /ergenverse village — build the village at world spawn. */
    private static int buildVillage(CommandSourceStack src) {
        ServerLevel level = src.getLevel();
        if (WangFamilyVillageBuilder.isAlreadyBuilt(level)) {
            src.sendSuccess(() -> Component.literal("Village already built at spawn.")
                    .withStyle(ChatFormatting.YELLOW), false);
        } else {
            WangFamilyVillageBuilder.build(level);
            src.sendSuccess(() -> Component.literal("Wang Family Village built at spawn!")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        BlockPos spawn = level.getSharedSpawnPos();
        src.sendSuccess(() -> Component.literal("Center: " + spawn.getX() + ", "
                + spawn.getY() + ", " + spawn.getZ()), false);
        return 1;
    }

    /** /ergenverse book — give the tutorial book to the player. */
    private static int giveBook(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run this command as a player, not from console."));
            return 0;
        }
        net.minecraft.world.item.ItemStack book = TutorialBookFactory.create();
        if (!player.getInventory().add(book)) {
            player.drop(book, false);
        }
        player.getPersistentData().putBoolean(NBT_TUTORIAL_GIVEN, true);
        src.sendSuccess(() -> Component.literal("Tutorial book given.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    /** /ergenverse reset — clear the tutorial-given flag. */
    private static int resetFlag(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run this command as a player."));
            return 0;
        }
        player.getPersistentData().putBoolean(NBT_TUTORIAL_GIVEN, false);
        src.sendSuccess(() -> Component.literal("Tutorial flag cleared. Re-log or run /ergenverse village + /ergenverse book.")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
