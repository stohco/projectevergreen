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
            .then(Commands.literal("geography")
                .executes(ctx -> geography(ctx.getSource())))
            .then(Commands.literal("desire_list")
                .executes(ctx -> desireList(ctx.getSource())))
            .then(Commands.literal("desire_fire")
                .executes(ctx -> desireFire(ctx.getSource())))
        );
        Ergenverse.LOGGER.info("[Ergenverse] /ergenverse command registered (status|village|book|gear|reset|geography|desire_list|desire_fire).");
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

        src.sendSuccess(() -> Component.literal("Commands: /ergenverse <status|village|book|gear|reset|desire_list|desire_fire>")
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

    /**
     * /ergenverse desire_list — Art XL proof path. Lists all EntityCultivators
     * within 32 blocks and their loaded DesireState data. Proves: (a) NPCs spawn,
     * (b) Actor link works, (c) desire data loads from JSON.
     */
    private static int desireList(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run as a player."));
            return 0;
        }

        var cultivators = player.level().getEntitiesOfClass(
                dev.ergenverse.entity.EntityCultivator.class,
                player.getBoundingBox().inflate(32));

        if (cultivators.isEmpty()) {
            src.sendSuccess(() -> Component.literal("No EntityCultivators within 32 blocks.")
                    .withStyle(ChatFormatting.YELLOW), false);
            src.sendSuccess(() -> Component.literal("NPCs spawn on chunk load. Try walking around.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 1;
        }

        src.sendSuccess(() -> Component.literal("=== NPC Desires (Art XXXI) — " + cultivators.size() + " nearby ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        int totalDesires = 0;
        int activeDesires = 0;
        for (dev.ergenverse.entity.EntityCultivator npc : cultivators) {
            String charId = npc.getCharacterId();
            String name = npc.getDisplayNameCn();
            String actorId = dev.ergenverse.simulation.intent.ActorEntityLink.getActorId(npc.getUUID());
            double dist = npc.distanceTo(player);

            if (actorId == null) {
                src.sendSuccess(() -> Component.literal(name + " (" + charId + ") — "
                        + (int) dist + " blocks — NO ACTOR LINK")
                        .withStyle(ChatFormatting.RED), false);
                continue;
            }

            dev.ergenverse.simulation.actor.Actor actor =
                    dev.ergenverse.simulation.actor.ActorRegistry.get(actorId);
            if (actor == null) {
                src.sendSuccess(() -> Component.literal(name + " (" + charId + ") — "
                        + (int) dist + " blocks — ACTOR NULL")
                        .withStyle(ChatFormatting.RED), false);
                continue;
            }

            java.util.List<dev.ergenverse.simulation.cognition.DesireState> desires =
                    actor.cognition.desires;
            totalDesires += desires.size();

            long now = player.level().getGameTime();
            long activeCount = desires.stream()
                    .filter(d -> d.isActive(now) && d.urgency() >= 0.3)
                    .count();
            activeDesires += activeCount;

            ChatFormatting statusColor = desires.isEmpty() ? ChatFormatting.RED
                    : activeCount > 0 ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
            src.sendSuccess(() -> Component.literal(name + " (" + charId + ") — "
                    + (int) dist + " blocks — " + desires.size() + " desires, "
                    + activeCount + " active")
                    .withStyle(statusColor), false);

            for (dev.ergenverse.simulation.cognition.DesireState d : desires) {
                boolean active = d.isActive(now) && d.urgency() >= 0.3;
                String modeStr = d.hasLine() ? (d.requiresApproach() ? "APPROACH" : "LINE") : "SILENT";
                ChatFormatting lineColor = active ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY;
                src.sendSuccess(() -> Component.literal("  [" + d.socialEngine() + "] "
                        + d.id() + " (urgency=" + String.format("%.2f", d.urgency())
                        + ", " + modeStr + ")" + (active ? "" : " [dormant]"))
                        .withStyle(lineColor), false);
                if (d.hasLine()) {
                    src.sendSuccess(() -> Component.literal("    \"" + d.line() + "\"")
                            .withStyle(ChatFormatting.GRAY), false);
                }
            }
        }

        final int finalTotal = totalDesires;
        final int finalActive = activeDesires;
        final int npcCount = cultivators.size();
        src.sendSuccess(() -> Component.literal("Total: " + npcCount + " NPCs, "
                + finalTotal + " desires, " + finalActive + " active")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }

    /**
     * /ergenverse desire_fire — Art XL proof path. Force-fires the nearest
     * NPC's highest-urgency active desire as a system message. Bypasses the
     * 10-second settle timer so the player sees the desire system work
     * IMMEDIATELY. This is the Five Minute Rule demonstration.
     *
     * <p>What the player sees: the NPC's desire line appears in chat, exactly
     * as NpcDesireGoal.deliverLine() would produce it. Proves the full
     * pipeline: JSON → ActorEntityLink → DesireState → observable text.</p>
     */
    private static int desireFire(CommandSourceStack src) {
        ServerPlayer player = src.getPlayer();
        if (player == null) {
            src.sendFailure(Component.literal("Run as a player."));
            return 0;
        }

        var cultivators = player.level().getEntitiesOfClass(
                dev.ergenverse.entity.EntityCultivator.class,
                player.getBoundingBox().inflate(32));

        if (cultivators.isEmpty()) {
            src.sendFailure(Component.literal("No EntityCultivators within 32 blocks. NPCs spawn on chunk load."));
            return 0;
        }

        // Find the NPC with the highest-urgency active desire that has a line
        dev.ergenverse.simulation.cognition.DesireState bestDesire = null;
        dev.ergenverse.entity.EntityCultivator bestNpc = null;
        String bestName = null;

        long now = player.level().getGameTime();
        for (dev.ergenverse.entity.EntityCultivator npc : cultivators) {
            String actorId = dev.ergenverse.simulation.intent.ActorEntityLink.getActorId(npc.getUUID());
            if (actorId == null) continue;
            dev.ergenverse.simulation.actor.Actor actor =
                    dev.ergenverse.simulation.actor.ActorRegistry.get(actorId);
            if (actor == null) continue;

            for (dev.ergenverse.simulation.cognition.DesireState d : actor.cognition.desires) {
                if (!d.hasLine()) continue;
                if (!d.isActive(now)) continue;
                if (d.urgency() < 0.3) continue;
                if (bestDesire == null || d.urgency() > bestDesire.urgency()) {
                    bestDesire = d;
                    bestNpc = npc;
                    bestName = npc.getDisplayNameCn();
                }
            }
        }

        if (bestDesire == null || bestNpc == null) {
            src.sendFailure(Component.literal("No active desires with lines found among "
                    + cultivators.size() + " nearby NPCs."));
            src.sendSuccess(() -> Component.literal("All desires may be on cooldown or have no spoken lines.")
                    .withStyle(ChatFormatting.GRAY), false);
            return 0;
        }

        // Deliver the line exactly as NpcDesireGoal.deliverLine() does
        final String line = bestDesire.line();
        final String firedName = bestName;
        final String firedEngine = bestDesire.socialEngine();
        final String firedId = bestDesire.id();
        final double firedUrgency = bestDesire.urgency();
        String prefix = "\u00A77<" + firedName + "> "; // gray NPC name
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(prefix + line + "\u00A7r"), true);

        src.sendSuccess(() -> Component.literal("[DESIRE FIRED] " + firedName + " ["
                + firedEngine + "] " + firedId
                + " (urgency=" + String.format("%.2f", firedUrgency) + ")")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), false);

        Ergenverse.LOGGER.info("[DesireFire] {} -> player [{}]: \"{}\"",
                bestNpc.getCharacterId(), firedEngine, line);

        return 1;
    }

    /**
     * /ergenverse geography — shows which country/settlement the player is in,
     * based on the AUTHORED World Blueprint. This proves geography is fixed
     * (not seed-dependent). The same coordinates always yield the same country.
     */
    private static int geography(CommandSourceStack src) {
        src.sendSuccess(() -> Component.literal("=== Planet Suzaku — Authored Geography ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);

        if (!dev.ergenverse.world.blueprint.WorldBlueprintManager.isLoaded()) {
            src.sendSuccess(() -> Component.literal("World Blueprint: NOT LOADED")
                    .withStyle(ChatFormatting.RED), false);
            return 0;
        }

        src.sendSuccess(() -> Component.literal("World Blueprint: LOADED")
                .withStyle(ChatFormatting.GREEN), false);
        src.sendSuccess(() -> Component.literal("Canon Seed: -7283654891029384756 (terrain is deterministic)")
                .withStyle(ChatFormatting.DARK_GRAY), false);

        ServerPlayer player = src.getPlayer();
        if (player == null) {
            // Console: show all settlements
            src.sendSuccess(() -> Component.literal("--- Canonical Settlements ---")
                    .withStyle(ChatFormatting.YELLOW), false);
            for (com.google.gson.JsonObject s : dev.ergenverse.world.blueprint.WorldBlueprintManager.getSettlements()) {
                String name = s.has("name") ? s.get("name").getAsString() : s.get("id").getAsString();
                int x = s.get("x").getAsInt();
                int z = s.get("z").getAsInt();
                String type = s.has("type") ? s.get("type").getAsString() : "?";
                src.sendSuccess(() -> Component.literal("  " + name + " (" + type + ") at (" + x + ", " + z + ")")
                        .withStyle(ChatFormatting.WHITE), false);
            }
            return 1;
        }

        BlockPos ppos = player.blockPosition();
        int px = ppos.getX();
        int pz = ppos.getZ();

        // Show which country the player is in.
        String country = dev.ergenverse.world.blueprint.WorldBlueprintManager.getCountryAt(px, pz);
        if (country != null) {
            src.sendSuccess(() -> Component.literal("Country: " + country)
                    .withStyle(ChatFormatting.GREEN), false);
        } else {
            src.sendSuccess(() -> Component.literal("Country: Wilderness/Ocean (not in any canonical country)")
                    .withStyle(ChatFormatting.GRAY), false);
        }

        // Check if in a restriction zone.
        com.google.gson.JsonObject restriction =
                dev.ergenverse.world.blueprint.WorldBlueprintManager.getRestrictionAt(px, pz);
        if (restriction != null) {
            String rName = restriction.has("name") ? restriction.get("name").getAsString() : "Unknown Restriction";
            src.sendSuccess(() -> Component.literal("RESTRICTION ZONE: " + rName)
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD), false);
        }

        // Show nearby settlements (within 500 blocks).
        src.sendSuccess(() -> Component.literal("--- Nearby Canonical Settlements (500 blocks) ---")
                .withStyle(ChatFormatting.YELLOW), false);
        var nearby = dev.ergenverse.world.blueprint.WorldBlueprintManager.getSettlementsNear(px, pz, 500);
        if (nearby.isEmpty()) {
            src.sendSuccess(() -> Component.literal("  (none nearby)")
                    .withStyle(ChatFormatting.GRAY), false);
        } else {
            for (com.google.gson.JsonObject s : nearby) {
                String name = s.has("name") ? s.get("name").getAsString() : s.get("id").getAsString();
                int sx = s.get("x").getAsInt();
                int sz = s.get("z").getAsInt();
                int dist = (int) Math.sqrt(Math.pow(px - sx, 2) + Math.pow(pz - sz, 2));
                String type = s.has("type") ? s.get("type").getAsString() : "?";
                src.sendSuccess(() -> Component.literal("  " + name + " (" + type + ") — " + dist + " blocks away at (" + sx + ", " + sz + ")")
                        .withStyle(ChatFormatting.WHITE), false);
            }
        }

        src.sendSuccess(() -> Component.literal("Geography is AUTHORED — same every playthrough. Simulation varies per seed.")
                .withStyle(ChatFormatting.AQUA), false);
        return 1;
    }
}
