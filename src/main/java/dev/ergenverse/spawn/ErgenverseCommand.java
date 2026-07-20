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

    /** Per-player NBT flag — must match SpawnEventHandler.NBT_SUZAKU_TELEPORTED. */
    private static final String NBT_SUZAKU_TELEPORTED = "ergenverse.suzaku_teleported";

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
            .then(Commands.literal("gear")
                .executes(ctx -> giveGear(ctx.getSource())))
            .then(Commands.literal("reset")
                .executes(ctx -> resetFlag(ctx.getSource())))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergenverse command registered (status|village|book|gear|reset).");
    }

    /** /ergenverse status — print mod load + village + player state. */
    private static int status(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("=== Ergenverse Status ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
        src.sendSuccess(() -> Component.literal("Mod loaded: YES")
                .withStyle(ChatFormatting.GREEN), false);

        // Check the Planet Suzaku dimension for the village.
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> suzakuKey =
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
        ServerLevel suzakuLevel = src.getServer().getLevel(suzakuKey);

        if (suzakuLevel != null) {
            boolean villageBuilt = WangFamilyVillageBuilder.isAlreadyBuilt(suzakuLevel);
            src.sendSuccess(() -> Component.literal("Village at (" + WangFamilyVillageBuilder.VILLAGE_X
                    + ", ?, " + WangFamilyVillageBuilder.VILLAGE_Z + "): "
                    + (villageBuilt ? "BUILT" : "NOT BUILT"))
                    .withStyle(villageBuilt ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);
        } else {
            src.sendSuccess(() -> Component.literal("Planet Suzaku: NOT FOUND")
                    .withStyle(ChatFormatting.RED), false);
        }

        ServerPlayer player = src.getPlayer();
        if (player != null) {
            boolean teleported = player.getPersistentData().getBoolean(NBT_SUZAKU_TELEPORTED);
            src.sendSuccess(() -> Component.literal("On Planet Suzaku: "
                    + (teleported ? "YES" : "NO"))
                    .withStyle(teleported ? ChatFormatting.GREEN : ChatFormatting.YELLOW), false);

            BlockPos ppos = player.blockPosition();
            int distToVillage = (int) Math.sqrt(
                    Math.pow(ppos.getX() - WangFamilyVillageBuilder.VILLAGE_X, 2) +
                    Math.pow(ppos.getZ() - WangFamilyVillageBuilder.VILLAGE_Z, 2));
            src.sendSuccess(() -> Component.literal("Distance to village: " + distToVillage + " blocks")
                    .withStyle(ChatFormatting.AQUA), false);
        }

        src.sendSuccess(() -> Component.literal("Commands: /ergenverse <status|village|book|gear|reset>")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    /** /ergenverse village — build the village at its canonical coordinate on Planet Suzaku. */
    private static int buildVillage(CommandSourceStack src) {
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> suzakuKey =
                net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
        ServerLevel suzakuLevel = src.getServer().getLevel(suzakuKey);

        if (suzakuLevel == null) {
            src.sendFailure(Component.literal("Planet Suzaku dimension not found."));
            return 0;
        }

        if (WangFamilyVillageBuilder.isAlreadyBuilt(suzakuLevel)) {
            src.sendSuccess(() -> Component.literal("Village already built at ("
                    + WangFamilyVillageBuilder.VILLAGE_X + ", ?, " + WangFamilyVillageBuilder.VILLAGE_Z + ").")
                    .withStyle(ChatFormatting.YELLOW), false);
        } else {
            WangFamilyVillageBuilder.build(suzakuLevel);
            src.sendSuccess(() -> Component.literal("Wang Family Village built at ("
                    + WangFamilyVillageBuilder.VILLAGE_X + ", ?, " + WangFamilyVillageBuilder.VILLAGE_Z + ")!")
                    .withStyle(ChatFormatting.GREEN), false);
        }
        BlockPos center = WangFamilyVillageBuilder.getVillageCenter(suzakuLevel);
        src.sendSuccess(() -> Component.literal("Center: " + center.getX() + ", "
                + center.getY() + ", " + center.getZ()), false);
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
        player.getPersistentData().putBoolean(NBT_SUZAKU_TELEPORTED, true);
        src.sendSuccess(() -> Component.literal("Tutorial book given.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    /** /ergenverse gear — give starter gear to the player. */
    private static int giveGear(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run this command as a player."));
            return 0;
        }
        giveItem(player, dev.ergenverse.item.ErgenverseItems.SPIRIT_STONE.get(), 8);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get(), 1);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get(), 4);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.MEDITATION_MAT.get(), 1);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.SPIRIT_HERB_SEED.get(), 6);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.FORMATION_FLAG_BLANK.get(), 2);
        giveItem(player, dev.ergenverse.item.ErgenverseItems.TALISMAN_PAPER_BLANK.get(), 4);
        src.sendSuccess(() -> Component.literal("Starter gear given.")
                .withStyle(ChatFormatting.GREEN), false);
        return 1;
    }

    private static void giveItem(ServerPlayer player, net.minecraft.world.item.Item item, int count) {
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    /** /ergenverse reset — clear the tutorial-given flag. */
    private static int resetFlag(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run this command as a player."));
            return 0;
        }
        player.getPersistentData().putBoolean(NBT_SUZAKU_TELEPORTED, false);
        src.sendSuccess(() -> Component.literal("Tutorial flag cleared. Re-log or run /ergenverse village + /ergenverse book.")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
