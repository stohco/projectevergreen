package dev.ergenverse.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.perception.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;

/**
 * PerceptionCommand — /ergen perceive [demo|tier|scan]
 *
 * <p>In-game command for testing and verifying the perception system.
 * Requires permission level 2 (op).
 *
 * <h2>Subcommands</h2>
 * <ul>
 *   <li>{@code /ergen perceive demo} — Runs the PerceptionDemo showing
 *       how the same Fifth-Rank Spirit Rabbit is perceived differently
 *       by observers at 5 cultivation tiers.</li>
 *   <li>{@code /ergen perceive tier} — Shows the player's current
 *       perception tier and divine sense stats.</li>
 *   <li>{@code /ergen perceive scan} — Server-side divine sense scan
 *       of nearby entities, printing perception results to chat.</li>
 * </ul>
 */
public class PerceptionCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> disp = event.getDispatcher();
        disp.register(Commands.literal("ergen")
            .requires(src -> src.hasPermission(2))
            .then(Commands.literal("perceive")
                .then(Commands.literal("demo")
                    .executes(PerceptionCommand::runDemo))
                .then(Commands.literal("tier")
                    .executes(PerceptionCommand::showTier))
                .then(Commands.literal("scan")
                    .executes(PerceptionCommand::runScan))
            ));
    }

    /** Helper: send a component message. */
    private static void msg(CommandSourceStack src, MutableComponent comp) {
        src.sendSuccess(() -> comp, false);
    }

    /**
     * /ergen perceive demo — prints the PerceptionDemo output to chat.
     */
    private static int runDemo(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        PerceptionDemo.printDemo(line -> {
            msg(source, Component.literal(line).withStyle(ChatFormatting.GRAY));
        });
        return 1;
    }

    /**
     * /ergen perceive tier — shows the player's perception tier and
     * divine sense stats.
     */
    private static int showTier(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        CultivationState state;
        try {
            state = CultivationCapability.getOrThrow(player);
        } catch (Exception e) {
            msg(source, Component.literal("No cultivation state. You are a mortal.")
                    .withStyle(ChatFormatting.GRAY));
            return 1;
        }

        RealmId realm = state.getCurrentRealm();
        PerceptionTier tier = PerceptionTier.fromRealm(realm);
        long sSense = DivineSense.soulPowerTotal(realm, 0.0, 0L);
        int radius = DivineSense.radius(sSense, 0, 16);

        msg(source, Component.literal("=== Perception Status ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        msg(source, Component.literal("Realm: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(realm.name).withStyle(ChatFormatting.WHITE)));
        msg(source, Component.literal("Perception Tier: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(tier.label).withStyle(ChatFormatting.YELLOW)));
        msg(source, Component.literal("Soul Power (S_sense): ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(String.valueOf(sSense)).withStyle(ChatFormatting.GREEN)));
        msg(source, Component.literal("Divine Sense Radius: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(radius + " blocks").withStyle(ChatFormatting.GREEN)));

        // Show Dao comprehension
        Map<String, Double> daoMap = state.getDaoComprehension();
        if (!daoMap.isEmpty()) {
            msg(source, Component.literal("Dao Affinities:").withStyle(ChatFormatting.GRAY));
            for (var entry : daoMap.entrySet()) {
                msg(source, Component.literal("  " + entry.getKey() + ": ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(String.format("%.0f%%", entry.getValue() * 100))
                                .withStyle(ChatFormatting.LIGHT_PURPLE)));
            }
        }

        // Show concealment thresholds
        msg(source, Component.literal("Concealment Thresholds:").withStyle(ChatFormatting.GRAY));
        for (var entry : DivineSense.CAMOUFLAGE_BY_KIND.entrySet()) {
            String status = sSense >= entry.getValue() ? "\u00a7a\u2713" : "\u00a7c\u2717";
            msg(source, Component.literal("  " + status + " " + entry.getKey())
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" (C=" + entry.getValue() + ")")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        }

        return 1;
    }

    /**
     * /ergen perceive scan — server-side scan of nearby EntityCultivator
     * NPCs, running PerceptionEngine for each and printing results.
     */
    private static int runScan(CommandContext<CommandSourceStack> ctx) {
        var source = ctx.getSource();
        var player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        CultivationState state;
        try {
            state = CultivationCapability.getOrThrow(player);
        } catch (Exception e) {
            state = null;
        }

        RealmId realm = state != null ? state.getCurrentRealm() : RealmId.MORTAL;

        // Find nearby cultivator entities within 64 blocks
        var nearby = player.level().getEntitiesOfClass(
                EntityCultivator.class,
                player.getBoundingBox().inflate(64));

        if (nearby.isEmpty()) {
            msg(source, Component.literal("No cultivator entities within 64 blocks.")
                    .withStyle(ChatFormatting.GRAY));
            return 1;
        }

        msg(source, Component.literal("=== Perception Scan (" + nearby.size() + " entities) ===")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        ObserverContext observer = state != null
                ? new ObserverContext(realm, state.getDaoComprehension(), true,
                    Set.of(), 1.0, false, false, false)
                : ObserverContext.mortal();

        for (EntityCultivator entity : nearby) {
            // Build ObjectiveNature server-side (no client classes)
            String entityName = entity.getDisplayNameCn();
            if (entityName == null || entityName.isEmpty()) entityName = entity.getCharacterId();
            RealmId entityRealm = parseRealm(entity.getCultivationRealm());
            ObjectiveNature nature = ObjectiveNature.cultivator(
                    entityName, entityName, entityRealm,
                    "", "", "", // bloodline, origin, karmic (v1)
                    dev.ergenverse.canon.CanonEngine.Confidence.NOVEL_STATEMENT,
                    "npc:" + entity.getCharacterId(),
                    "", "", ""  // daoAffinities, titles, sect (v1)
            );

            Objective objective = new Objective() {
                @Override
                public ObjectiveNature nature() { return nature; }
                @Override
                public PerceptionResult perceive(ObserverContext obs) {
                    return PerceptionEngine.perceive(this, obs);
                }
            };

            PerceptionResult result = PerceptionEngine.perceive(objective, observer);

            // Print result
            ChatFormatting nameColor = result.concealed ? ChatFormatting.DARK_GRAY
                    : (result.canInteract ? ChatFormatting.WHITE : ChatFormatting.GRAY);
            msg(source, Component.literal("[" + entity.getCharacterId() + "] ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(result.perceivedName).withStyle(nameColor)));

            // Truncate description for chat
            String desc = result.perceivedDescription;
            if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
            msg(source, Component.literal("  ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(desc)
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));

            // Show additional info at higher tiers
            if (result.recognizedRank != null) {
                msg(source, Component.literal("  Rank: " + result.recognizedRank)
                        .withStyle(ChatFormatting.YELLOW));
            }

            msg(source, Component.literal("")); // blank line separator
        }

        return 1;
    }

    /** Parse a realm string to RealmId. */
    private static RealmId parseRealm(String realmStr) {
        if (realmStr == null || realmStr.isEmpty()) return RealmId.MORTAL;
        try {
            return RealmId.valueOf(realmStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return RealmId.MORTAL;
        }
    }
}