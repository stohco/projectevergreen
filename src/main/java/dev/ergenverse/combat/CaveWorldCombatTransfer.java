package dev.ergenverse.combat;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.wanglin.CaveWorldOwnership;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cave World Ownership Combat Transfer — hooks into LivingDeathEvent to
 * transfer world-layer ownership when a player kills a cave-world owner NPC.
 *
 * <p><b>Canon:</b> In Renegade Immortal, Wang Lin killed the Seven-Colored
 * Daoist to gain control of the Cave World, dissolving the cultivation
 * ceiling that had suppressed all cultivators within. The Joss Flame
 * harvest that the Daoist had been collecting then flowed to Wang Lin.
 *
 * <p><b>Mechanics:</b>
 * <ul>
 *   <li>When an {@link EntityCultivator} with a characterId matching an
 *       owner in {@link CaveWorldOwnership#OWNER_NPC_TO_WORLD} dies,
 *       ownership of the associated world-layer transfers to the killer.</li>
 *   <li>The killer must be a {@link ServerPlayer} (NPCs can't claim worlds).</li>
 *   <li>The killer's cultivation realm becomes the new ceiling (tier 14,
 *       effectively unlimited — per canon, Wang Lin dissolved the seal).</li>
 *   <li>The new owner's identity, realm, and the transfer reason are
 *       recorded and persisted to the world save.</li>
 *   <li>A global message announces the transfer to all players.</li>
 * </ul>
 *
 * <p><b>MC 1.20.1 / Forge 47.4.0 / Java 17 APIs only.</b>
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CaveWorldCombatTransfer {

    private CaveWorldCombatTransfer() {}

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Only care about server-side EntityCultivator deaths
        if (!(event.getEntity() instanceof EntityCultivator cultivator)) return;
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) return;

        // Check if the killer is a player
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        // Get the cultivator's character ID
        String characterId = cultivator.getCharacterId();
        if (characterId == null || characterId.isEmpty()) return;

        // Look up which world-layer this NPC owns (via public accessor)
        String worldLayerId = CaveWorldOwnership.getOwnedWorldForNpc(characterId);
        if (worldLayerId == null) return; // Not a cave-world owner — ignore

        // Get the current ownership (may be a runtime override from a previous transfer)
        CaveWorldOwnership.Ownership currentOwnership = CaveWorldOwnership.getRuntimeOwnership(worldLayerId);
        if (currentOwnership == null) {
            Ergenverse.LOGGER.warn("[CaveWorldCombat] Entity {} (characterId={}) is mapped to world {} "
                            + "but no ownership record exists.", cultivator, characterId, worldLayerId);
            return;
        }

        // Don't transfer if the killer is ALREADY the owner
        if (killer.getStringUUID().equals(currentOwnership.ownerId)) {
            Ergenverse.LOGGER.debug("[CaveWorldCombat] {} already owns {}. No transfer needed.",
                    killer.getName().getString(), worldLayerId);
            return;
        }

        // Get killer's cultivation state
        var stateOpt = CultivationCapability.get(killer);
        String killerRealmId = "unknown";
        CultivationState killerState = stateOpt.orElse(null);
        if (killerState != null) {
            killerRealmId = killerState.getCurrentRealm().name;
        }

        // Transfer ownership — this persists to world save
        CaveWorldOwnership.Ownership newOwnership = CaveWorldOwnership.transferOwnership(
                worldLayerId,
                killer.getStringUUID(),
                killer.getName().getString(),
                killerRealmId,
                serverLevel
        );

        if (newOwnership == null) return;

        // Notify the killer
        killer.sendSystemMessage(Component.literal(
                "\u00A76\u00A7l\u2694 Cave World Ownership Transferred!\u00A7r"));
        killer.sendSystemMessage(Component.literal(String.format(
                "\u00A7aYou defeated %s and claimed control of %s.\u00A7r",
                currentOwnership.ownerName, currentOwnership.worldLayerName)));
        killer.sendSystemMessage(Component.literal(
                "\u00A77The cultivation seal has been dissolved. Joss Flame harvests now flow to you."));

        // Notify all other players on the server
        net.minecraft.server.level.ServerPlayer[] players =
                serverLevel.getServer().getPlayerList().getPlayers()
                        .toArray(new ServerPlayer[0]);
        for (ServerPlayer otherPlayer : players) {
            if (otherPlayer == killer) continue;
            otherPlayer.sendSystemMessage(Component.literal(String.format(
                    "\u00A76\u00A7l%s\u00A7r\u00A77 has defeated \u00A76%s\u00A77 and claimed \u00A7a%s\u00A77!",
                    killer.getName().getString(),
                    currentOwnership.ownerName,
                    currentOwnership.worldLayerName)));
        }

        Ergenverse.LOGGER.info("[CaveWorldCombat] {} ({}) killed {} (characterId={}) at {}. "
                        + "Ownership of '{}' transferred. New realm ceiling: tier 14 (seal dissolved).",
                killer.getName().getString(), killerRealmId,
                currentOwnership.ownerName, characterId, cultivator.blockPosition(),
                worldLayerId);
    }
}