package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * BillionSoulFlagEventHandler — passive soul absorption on kill.
 *
 * <p>Listens to {@link LivingDeathEvent} and, if the killer is a player holding
 * the {@link BillionSoulFlagItem} in either hand, attempts to absorb the victim's
 * soul into the flag. This is the passive mechanic that distinguishes the flag
 * from the SoulGourdItem (which requires manual right-click on a corpse).
 *
 * <p><b>Canon:</b> The Billion Soul Flag automatically absorbs the souls of
 * those slain near it — Wang Lin doesn't need to manually capture each soul.
 * The flag is a sentient treasure that hungers for souls and draws them in.
 *
 * <p><b>Design notes:</b>
 * <ul>
 *   <li>Runs at {@link EventPriority#NORMAL} — lower than PlayerCombatBridge's
 *       HIGHEST, so combat-event publication happens first. The flag's soul
 *       absorption is a gameplay mechanic, not a simulation event.</li>
 *   <li>Handles ALL kills (vanilla mobs, beasts, cultivators) — a soul is a
 *       soul. PlayerCombatBridge only handles canon-tagged entities.</li>
 *   <li>Checks BOTH hands (main + offhand) — the player can hold the flag in
 *       either hand and still benefit from passive absorption.</li>
 *   <li>Does NOT cancel the death event — the victim still dies normally.</li>
 * </ul>
 *
 * <p>CRON-COMPLETIONIST-89: New event handler to support BillionSoulFlagItem.
 *
 * <p>Self-critique: The handler runs on EVERY entity death on the server, even
 * if no player is holding the flag. The early-return checks (isClientSide,
 * source entity is player, player is holding flag) make this cheap, but it's
 * still a per-death overhead. A more optimized approach would use a capability
 * or a per-player "has flag" tracking set, but that's premature optimization
 * for a single-player-focused mod.
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BillionSoulFlagEventHandler {

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onLivingDeath(LivingDeathEvent event) {
        // Client-side guard — should never fire on client, but double-check.
        if (event.getEntity() == null) return;
        if (event.getEntity().level().isClientSide()) return;

        LivingEntity victim = event.getEntity();

        // Only player-sourced kills trigger soul absorption.
        // Canon: the flag absorbs souls of those the cultivator slays —
        // not natural deaths, not mob-vs-mob kills.
        if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) return;

        // Don't absorb player deaths (no soul from the player themselves).
        if (victim instanceof Player) return;

        if (!(killer.level() instanceof ServerLevel sl)) return;

        // Check if the player is holding the Billion Soul Flag in either hand.
        ItemStack mainHand = killer.getMainHandItem();
        ItemStack offHand = killer.getOffhandItem();

        boolean mainIsFlag = mainHand.getItem() instanceof BillionSoulFlagItem;
        boolean offIsFlag = offHand.getItem() instanceof BillionSoulFlagItem;

        if (!mainIsFlag && !offIsFlag) return;

        // Absorb into the flag (prefer main hand if both are flags).
        ItemStack flag = mainIsFlag ? mainHand : offHand;
        BillionSoulFlagItem flagItem = (BillionSoulFlagItem) flag.getItem();

        boolean absorbed = flagItem.absorbSoul(flag, victim, sl);

        if (absorbed) {
            // Subtle feedback — the player shouldn't be spammed with messages
            // for every kill, but should know the flag is working.
            int total = flagItem.getTotalSoulCount(flag);
            boolean isMain = victim.getMaxHealth() >= 50.0F;

            sl.playSound(null, killer.blockPosition(),
                    net.minecraft.sounds.SoundEvents.SOUL_ESCAPE,
                    net.minecraft.sounds.SoundSource.PLAYERS, 0.4f, 0.7f);

            // Only send a chat message for milestones (every 10th soul, or any main soul)
            // to avoid chat spam during combat.
            if (isMain || total % 10 == 0) {
                String soulType = isMain ? "\u00A75\u2605 Main Soul" : "\u00A77soul";
                killer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "\u00A7d\u2726 The flag absorbs a " + soulType
                                + " \u00A77(" + victim.getDisplayName().getString() + ")"
                                + " \u00A7f[Total: " + total + "]"));
            }
        }
    }
}
