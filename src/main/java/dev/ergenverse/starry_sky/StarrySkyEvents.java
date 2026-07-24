package dev.ergenverse.starry_sky;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.block.ErgenverseBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * StarrySkyEvents — Forge event handler for the Starry Sky dimension.
 *
 * <p>Wires the simulation into the Forge event bus:
 * <ul>
 *   <li>{@link TickEvent.ServerTickEvent} (phase=END): ticks the simulation
 *       and the void beast encounter system.</li>
 *   <li>{@link PlayerEvent.PlayerChangedDimensionEvent}: detects players
 *       entering/leaving the starry_sky dimension and notifies the
 *       simulation so it can reify/despawn cultivators accordingly.</li>
 *   <li>{@link LivingDeathEvent}: when a wandering cultivator or void
 *       beast entity dies, drops loot (spirit stones / canon items).</li>
 * </ul>
 *
 * <p>Registered on the FORGE event bus via
 * {@code MinecraftForge.EVENT_BUS.register(StarrySkyEvents.class)} in
 * {@link dev.ergenverse.core.Ergenverse}.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class StarrySkyEvents {

    private StarrySkyEvents() {}

    /**
     * Server tick — drives the starry sky simulation.
     *
     * <p>{@link StarrySkySimulation#tick} internally gates to once per 20
     * server ticks (1 simulation second), so calling it every tick is
     * cheap (an early-return when gameTime % 20 != 0).
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        if (server == null) return;

        // Tick the cultivator simulation (internally gated)
        StarrySkySimulation.tick(server);

        // Tick the void beast encounter system (only if dim is loaded)
        ServerLevel starrySky = server.getLevel(StarrySkySimulation.STARRY_SKY_KEY);
        if (starrySky != null) {
            VoidBeastEncounter.tickPlayerEncounters(starrySky);
        }
    }

    /**
     * Player changed dimension — reify/despawn cultivators when players
     * enter/leave the starry sky.
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ResourceKey<Level> to = event.getTo();
        ResourceKey<Level> from = event.getFrom();

        if (to.equals(StarrySkySimulation.STARRY_SKY_KEY)) {
            StarrySkySimulation.onPlayerEnter(player);
        } else if (from.equals(StarrySkySimulation.STARRY_SKY_KEY)) {
            StarrySkySimulation.onPlayerLeave(player);
        }
    }

    /**
     * Living death — when a wandering cultivator or void beast dies, drop
     * loot. Wandering cultivators drop spirit stones (the universal
     * cultivation currency). Void beasts drop more spirit stones (they
     * are said to have consumed many cultivators' wealth).
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        if (!(entity.level() instanceof ServerLevel serverLevel)) return;

        // ── Wandering cultivator death ────────────────────────────────
        if (entity instanceof EntityCultivator cultivator) {
            String characterId = cultivator.getCharacterId();
            if (characterId != null && characterId.startsWith("starry_sky_wandering_")) {
                // Drop 1-3 spirit stones (the cultivator's travel funds)
                int dropCount = 1 + serverLevel.random.nextInt(3);
                ItemStack stack = new ItemStack(ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().asItem(), dropCount);
                cultivator.spawnAtLocation(stack);
                // Notify killer if a player
                if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                    killer.sendSystemMessage(Component.literal(
                            "\u00A7cYou struck down a wandering cultivator. The starry sky "
                            + "claims another. (+" + dropCount + " spirit stones)"));
                }
                Ergenverse.LOGGER.info("[StarrySkyEvents] Wandering cultivator {} slain at {}. "
                                + "Dropped {} spirit stones.",
                        cultivator.getDisplayNameCn(), cultivator.blockPosition(), dropCount);
            }
            return;
        }

        // ── Void beast death ──────────────────────────────────────────
        if (entity instanceof EnderMan enderMan) {
            // Detect void beast by custom name tag
            Component name = enderMan.getCustomName();
            if (name != null && name.getString().contains("虚空兽")) {
                // Drop 3-7 spirit stones (the beast's accumulated hoard
                // from consumed cultivators)
                int dropCount = 3 + serverLevel.random.nextInt(5);
                ItemStack stack = new ItemStack(ErgenverseBlocks.SPIRIT_STONE_BLOCK.get().asItem(), dropCount);
                enderMan.spawnAtLocation(stack);
                if (event.getSource().getEntity() instanceof ServerPlayer killer) {
                    killer.sendSystemMessage(Component.literal(
                            "\u00A7d\u00A7lVoid Beast slain!\u00A7r\u00A77 "
                            + "The void releases its hoard of stolen spirit stones. "
                            + "(+" + dropCount + " spirit stones)"));
                    // Broadcast to other players in the starry sky dimension
                    ServerLevel starrySky = killer.getServer().getLevel(
                            StarrySkySimulation.STARRY_SKY_KEY);
                    if (starrySky != null) {
                        for (ServerPlayer other : starrySky.getServer().getPlayerList()
                                .getPlayers()) {
                            if (other == killer) continue;
                            if (other.level() == starrySky) {
                                other.sendSystemMessage(Component.literal(
                                        "\u00A7d\u00A7l" + killer.getName().getString()
                                                + " \u00A77has slain a Void Beast in the starry sky!"));
                            }
                        }
                    }
                }
                Ergenverse.LOGGER.info("[StarrySkyEvents] Void beast slain at {}. Dropped {} "
                                + "spirit stones.", enderMan.blockPosition(), dropCount);
            }
        }
    }
}
