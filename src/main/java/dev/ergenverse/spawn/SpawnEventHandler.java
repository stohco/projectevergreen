package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SpawnEventHandler — handles the player's first login into the Ergenverse.
 *
 * <p>On first join (per-player NBT flag), this handler:
 * <ol>
 *   <li>Builds the Wang Family Village at world spawn (if not already built).
 *       The village is built from custom blocks so the player immediately
 *       sees they are not in vanilla Minecraft.</li>
 *   <li>Teleports the player to the village center.</li>
 *   <li>Gives the player the tutorial book (keybinds + hints + village guide).</li>
 *   <li>Sends a welcome message.</li>
 * </ol>
 *
 * <p>The village-build is world-scoped (idempotent via
 * {@link WangFamilyVillageBuilder#isAlreadyBuilt}); the tutorial book +
 * teleport are per-player (gated by persistent NBT).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class SpawnEventHandler {

    private SpawnEventHandler() {}

    /** Per-player NBT flag: true once the tutorial book has been given. */
    private static final String NBT_TUTORIAL_GIVEN = "ergenverse.tutorial_given";

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer sp)) return;
        if (sp.server == null) return;

        ServerLevel level = sp.server.overworld();
        boolean firstJoin = !sp.getPersistentData().getBoolean(NBT_TUTORIAL_GIVEN);

        Ergenverse.LOGGER.info("[Ergenverse] PlayerLoggedInEvent fired for {} (firstJoin={})",
                sp.getName().getString(), firstJoin);

        if (!firstJoin) return; // returning player — nothing to do here

        // Mark the tutorial as given immediately so a re-login during the
        // delayed task doesn't double-give.
        sp.getPersistentData().putBoolean(NBT_TUTORIAL_GIVEN, true);

        // ── Delay the teleport/book by 40 ticks (2s) so the player
        //    has fully loaded into the world before we move them.
        sp.server.tell(new TickTask(sp.server.getTickCount() + 40, () -> {
            try {
                Ergenverse.LOGGER.info("[Ergenverse] First-spawn task executing for {}.",
                        sp.getName().getString());

                // 1. Get the Planet Suzaku dimension.
                ResourceKey<Level> suzakuKey = ResourceKey.create(Registries.DIMENSION,
                        new net.minecraft.resources.ResourceLocation(Ergenverse.MOD_ID, "planet_suzaku"));
                ServerLevel suzakuLevel = sp.server.getLevel(suzakuKey);

                if (suzakuLevel != null) {
                    // 2. Teleport the player to Planet Suzaku.
                    BlockPos spawn = suzakuLevel.getSharedSpawnPos();
                    if (spawn == BlockPos.ZERO) {
                        spawn = suzakuLevel.getHeightmapPos(
                                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                new BlockPos(0, 0, 0));
                    }
                    suzakuLevel.getChunkAt(spawn); // ensure loaded
                    sp.teleportTo(suzakuLevel, spawn.getX() + 0.5, spawn.getY() + 1.0,
                            spawn.getZ() + 0.5, 0.0F, 0.0F);
                    Ergenverse.LOGGER.info("[Ergenverse] Teleported {} to Planet Suzaku at ({}, {}, {}).",
                            sp.getName().getString(), spawn.getX(), spawn.getY(), spawn.getZ());

                    // 3. Build the Wang Family Village at the spawn point.
                    if (!WangFamilyVillageBuilder.isAlreadyBuilt(suzakuLevel)) {
                        WangFamilyVillageBuilder.build(suzakuLevel);
                        Ergenverse.LOGGER.info("[Ergenverse] Wang Family Village built on Planet Suzaku.");
                    }
                } else {
                    Ergenverse.LOGGER.error("[Ergenverse] Planet Suzaku dimension not found! Falling back to overworld village.");
                    // Fallback: build village in overworld.
                    if (!WangFamilyVillageBuilder.isAlreadyBuilt(level)) {
                        WangFamilyVillageBuilder.build(level);
                    }
                    BlockPos center = WangFamilyVillageBuilder.getVillageCenter(level);
                    BlockPos landing = center.north(3);
                    sp.moveTo(landing.getX() + 0.5, center.getY() + 1.0, landing.getZ() + 0.5,
                            0.0F, 180.0F);
                    level.getChunkAt(center);
                }

                // 4. Give the tutorial book + starter gear (no vanilla chest).
                ItemStack book = TutorialBookFactory.create();
                if (!sp.getInventory().add(book)) {
                    sp.drop(book, false);
                }
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.SPIRIT_STONE.get(), 8);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get(), 1);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get(), 4);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.MEDITATION_MAT.get(), 1);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.SPIRIT_HERB_SEED.get(), 6);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.FORMATION_FLAG_BLANK.get(), 2);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.TALISMAN_PAPER_BLANK.get(), 4);
                Ergenverse.LOGGER.info("[Ergenverse] Tutorial book + starter gear given to {}.", sp.getName().getString());

                // 5. Welcome messages.
                sp.sendSystemMessage(Component.literal("")
                        .append(Component.literal("Welcome to Planet Suzaku.")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                sp.sendSystemMessage(Component.literal("")
                        .append(Component.literal("You have been given a ")
                                .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal("Beginner's Guide")
                                .withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(". Open it to learn the keybinds.")
                                .withStyle(ChatFormatting.GRAY)));
                sp.sendSystemMessage(Component.literal("")
                        .append(Component.literal("The Wang Family Village surrounds you. The spirit vein glows at the plaza center.")
                                .withStyle(ChatFormatting.YELLOW)));
                sp.sendSystemMessage(Component.literal("")
                        .append(Component.literal("Tip: type ")
                                .withStyle(ChatFormatting.DARK_GRAY))
                        .append(Component.literal("/ergenverse status")
                                .withStyle(ChatFormatting.AQUA))
                        .append(Component.literal(" to check mod status.")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            } catch (Exception e) {
                Ergenverse.LOGGER.error("[Ergenverse] Failed to set up first-spawn for {}: {}",
                        sp.getName().getString(), e.getMessage(), e);
            }
        }));
    }

    /** Give an item stack to the player, dropping at feet if inventory is full. */
    private static void giveItem(ServerPlayer sp, net.minecraft.world.item.Item item, int count) {
        ItemStack stack = new ItemStack(item, count);
        if (!sp.getInventory().add(stack)) {
            sp.drop(stack, false);
        }
    }
}
