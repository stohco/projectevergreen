package dev.ergenverse.spawn;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
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

        // ── Delay the build/teleport/book by 40 ticks (2s) so the player
        //    has fully loaded into the world before we move them.
        sp.server.tell(new TickTask(sp.server.getTickCount() + 40, () -> {
            try {
                Ergenverse.LOGGER.info("[Ergenverse] First-spawn task executing for {}.",
                        sp.getName().getString());

                // 1. Build the village if not yet built (world-scoped).
                if (!WangFamilyVillageBuilder.isAlreadyBuilt(level)) {
                    Ergenverse.LOGGER.info("[Ergenverse] First player join — building Wang Family Village at spawn.");
                    WangFamilyVillageBuilder.build(level);
                } else {
                    Ergenverse.LOGGER.info("[Ergenverse] Village already built — skipping build.");
                }

                // 2. Teleport the player to a clear spot on the plaza (3
                //    blocks north of center, standing on the spirit stone
                //    plaza, facing south toward the spirit vein). This avoids
                //    spawning them inside the spirit vein stone centerpiece.
                BlockPos center = WangFamilyVillageBuilder.getVillageCenter(level);
                BlockPos landing = center.north(3);
                sp.moveTo(landing.getX() + 0.5, center.getY() + 1.0, landing.getZ() + 0.5,
                        0.0F, 180.0F);
                // Ensure the chunk stays loaded so the player doesn't fall through.
                level.getChunkAt(center);
                Ergenverse.LOGGER.info("[Ergenverse] Teleported {} to village plaza at ({}, {}, {}).",
                        sp.getName().getString(), landing.getX(), center.getY() + 1, landing.getZ());

                // 3. Give the tutorial book + starter gear (no vanilla chest).
                ItemStack book = TutorialBookFactory.create();
                if (!sp.getInventory().add(book)) {
                    sp.drop(book, false);
                }
                // Starter gear (formerly in the SW storage chest):
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.SPIRIT_STONE.get(), 8);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.JADE_SLIP.get(), 1);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.QI_GATHERING_PILL.get(), 4);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.MEDITATION_MAT.get(), 1);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.SPIRIT_HERB_SEED.get(), 6);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.FORMATION_FLAG_BLANK.get(), 2);
                giveItem(sp, dev.ergenverse.item.ErgenverseItems.TALISMAN_PAPER_BLANK.get(), 4);
                Ergenverse.LOGGER.info("[Ergenverse] Tutorial book + starter gear given to {}.", sp.getName().getString());

                // 4. Welcome messages.
                sp.sendSystemMessage(Component.literal("")
                        .append(Component.literal("Welcome to the Ergenverse.")
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
