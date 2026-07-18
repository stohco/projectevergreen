package dev.ergenverse.wanglin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.core.Ergenverse;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * WangLinArsenalCommand — /wanglin arsenal <grant|list|evolve|max> [itemId] [stage]
 *
 * <p>Grants server operators access to Wang Lin's full arsenal for testing, building,
 * and creative play. This is the in-game access point for the completionist arsenal.
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /wanglin arsenal list} — lists all registered arsenal items</li>
 *   <li>{@code /wanglin arsenal grant <itemId>} — grants the base-state item to the player</li>
 *   <li>{@code /wanglin arsenal grant <itemId> max} — grants the item at peak evolution</li>
 *   <li>{@code /wanglin arsenal grant <itemId> stage <n>} — grants at a specific evolution stage</li>
 *   <li>{@code /wanglin arsenal grantall} — grants one of EVERY arsenal item (creative mode)</li>
 * </ul>
 *
 * <p>itemId is the registry name (e.g. "restriction_flag", "heaven_defying_bead").
 */
public class WangLinArsenalCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("wanglin")
                .requires(src -> src.hasPermission(2))  // op level 2+
                .then(Commands.literal("arsenal")
                    .then(Commands.literal("list")
                        .executes(WangLinArsenalCommand::listArsenal))
                    .then(Commands.literal("grantall")
                        .executes(WangLinArsenalCommand::grantAll))
                    .then(Commands.literal("grant")
                        .then(Commands.argument("itemId", StringArgumentType.string())
                            .executes(ctx -> grantItem(ctx, 0))
                            .then(Commands.literal("max")
                                .executes(ctx -> grantItem(ctx, -1)))
                            .then(Commands.literal("stage")
                                .then(Commands.argument("stage", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
                                    .executes(ctx -> grantItem(ctx,
                                            com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(ctx, "stage"))))))))
        );
        Ergenverse.LOGGER.info("[WangLin] /wanglin arsenal command registered (grant/list/grantall/max/stage).");
    }

    /** List all registered arsenal items to the player's chat. */
    private static int listArsenal(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        List<WangLinItems.ManifestEntry> entries = WangLinItems.allEntries();
        src.sendSuccess(() -> Component.literal("§6Wang Lin's Arsenal §7(" + entries.size() + " items):"), false);
        for (WangLinItems.ManifestEntry e : entries) {
            src.sendSuccess(() -> Component.literal("§7 - §f" + e.registryName()
                    + " §8(canonical=" + e.isCanonical() + ", unlock=" + e.unlockThreshold() + ")"), false);
        }
        return entries.size();
    }

    /** Grant a single arsenal item to the executing player. stage=0 base, -1 max, >0 specific. */
    private static int grantItem(CommandContext<CommandSourceStack> ctx, int stage) {
        CommandSourceStack src = ctx.getSource();
        String registryName = StringArgumentType.getString(ctx, "itemId");
        Entity executor = src.getEntity();
        if (!(executor instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("§cMust be run by a player."));
            return 0;
        }
        ResourceLocation rl = new ResourceLocation(Ergenverse.MOD_ID, registryName);
        Item item = ForgeRegistries.ITEMS.getValue(rl);
        if (item == null) {
            src.sendFailure(Component.literal("§cArsenal item not found: " + registryName
                    + ". Use /wanglin arsenal list to see all items."));
            return 0;
        }
        ItemStack stack = new ItemStack(item);
        if (item instanceof WangLinItem wli) {
            ItemEvolutionChain chain = ItemEvolutionRegistry.get(wli.evolutionChainId());
            if (chain != null) {
                int target = (stage == -1) ? chain.getMaxStage() : stage;
                chain.evolveTo(stack, target);
            }
        }
        player.getInventory().add(stack);
        String stageLabel = (stage == -1) ? "peak" : ("stage " + stage);
        src.sendSuccess(() -> Component.literal("§aGranted §f" + registryName + " §a(" + stageLabel + ")"), true);
        return 1;
    }

    /** Grant one of EVERY arsenal item to the executing player (creative mode). */
    private static int grantAll(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack src = ctx.getSource();
        Entity executor = src.getEntity();
        if (!(executor instanceof ServerPlayer player)) {
            src.sendFailure(Component.literal("§cMust be run by a player."));
            return 0;
        }
        List<WangLinItems.ManifestEntry> entries = WangLinItems.allEntries();
        int granted = 0;
        for (WangLinItems.ManifestEntry e : entries) {
            ResourceLocation rl = new ResourceLocation(Ergenverse.MOD_ID, e.registryName());
            Item item = ForgeRegistries.ITEMS.getValue(rl);
            if (item == null) continue;
            ItemStack stack = new ItemStack(item);
            player.getInventory().add(stack);
            granted++;
        }
        int finalGranted = granted;
        src.sendSuccess(() -> Component.literal("§aGranted §f" + finalGranted + " §aarsenal items (base state). Use /wanglin arsenal grant <id> max for peak state."), true);
        return granted;
    }
}
