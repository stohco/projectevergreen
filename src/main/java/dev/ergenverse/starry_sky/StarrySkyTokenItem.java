package dev.ergenverse.starry_sky;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * StarrySkyToken — a one-use teleport item that sends the player to the
 * Starry Sky dimension.
 *
 * <p><b>Canon:</b> Cultivators traveling to other planets use either a
 * flying artifact (slow, dangerous, only feasible for high-realm
 * cultivators) or a teleportation array (instant, requires an activated
 * array at both ends). This token represents a single-use teleportation
 * token — a canon-accurate means of entering the starry sky without
 * requiring the player to already be at a teleportation array.
 *
 * <p><b>Mechanics:</b>
 * <ul>
 *   <li>Right-click to teleport to the Starry Sky dimension.</li>
 *   <li>Requires Qi Condensation+ (mortals cannot survive the void's
 *       spiritual pressure).</li>
 *   <li>Consumed on successful use (single-use token).</li>
 *   <li>If the dimension is not loaded/unavailable, the token is not
 *       consumed and an error message is shown.</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
public class StarrySkyTokenItem extends Item {

    /** Minimum cultivation tier required to use the token. */
    private static final int MIN_TIER = 1; // Qi Condensation

    public StarrySkyTokenItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.success(stack);
        }

        // ── Check cultivation tier ─────────────────────────────────────
        CultivationState state = CultivationCapability.getOrThrow(serverPlayer);
        if (state.getCurrentRealm().order < MIN_TIER) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "\u00A7cThe token's spiritual pressure overwhelms you. Only a cultivator "
                    + "at Qi Condensation or above may travel to the starry sky.")
                    .withStyle(ChatFormatting.RED));
            serverPlayer.hurt(serverPlayer.level().damageSources().magic(), 2.0f);
            return InteractionResultHolder.success(stack);
        }

        // ── Get the target dimension ──────────────────────────────────
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            return InteractionResultHolder.success(stack);
        }
        ResourceKey<Level> dimKey = StarrySkySimulation.STARRY_SKY_KEY;
        ServerLevel starrySky = server.getLevel(dimKey);
        if (starrySky == null) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "\u00A7cThe starry sky cannot be reached — the dimensional anchor is unstable.")
                    .withStyle(ChatFormatting.RED));
            return InteractionResultHolder.success(stack);
        }

        // ── Find a safe spawn point ───────────────────────────────────
        BlockPos spawnPos = starrySky.getSharedSpawnPos();
        // Try heightmap to avoid spawning inside an asteroid
        BlockPos safePos = starrySky.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                spawnPos);
        if (safePos.getY() <= starrySky.getMinBuildHeight() + 1) {
            // No solid ground found — use a default mid-void position
            safePos = new BlockPos(spawnPos.getX(), 100, spawnPos.getZ());
        }

        // ── Teleport the player ───────────────────────────────────────
        serverPlayer.teleportTo(starrySky,
                safePos.getX() + 0.5, safePos.getY() + 1, safePos.getZ() + 0.5,
                java.util.Set.of(), serverPlayer.getYRot(), serverPlayer.getXRot());

        // ── Effects ───────────────────────────────────────────────────
        starrySky.playSound(null, safePos, SoundEvents.END_PORTAL_SPAWN,
                SoundSource.PLAYERS, 1.0f, 1.0f);
        starrySky.playSound(null, safePos, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS, 0.7f, 1.5f);

        serverPlayer.sendSystemMessage(Component.literal(
                "\u00A7b\u2726 The token dissolves. You feel yourself torn through the void...")
                .withStyle(ChatFormatting.AQUA));
        serverPlayer.sendSystemMessage(Component.literal(
                "\u00A77You arrive in the Starry Sky — the space between cultivation planets.")
                .withStyle(ChatFormatting.GRAY));

        // ── Consume the token ─────────────────────────────────────────
        stack.shrink(1);
        Ergenverse.LOGGER.info("[StarrySkyToken] Player {} used Starry Sky Token. "
                        + "Teleported to {} @ {} (tier {}).",
                serverPlayer.getName().getString(), dimKey.location(),
                safePos, state.getCurrentRealm().order);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                 List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u00A7bRight-click to enter the Starry Sky dimension.")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.literal("\u00A77The void between cultivation planets.")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("\u00A7cRequires: Qi Condensation+")
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("\u00A7eSingle use — consumed on teleport.")
                .withStyle(ChatFormatting.YELLOW));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
