package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

/**
 * BeadProgressionService — drives the Heaven-Defying Bead's natural growth
 * as the player cultivates. Closes the "real mechanics" gap left by CRON-77:
 * the bead had full NBT plumbing, a function menu, an interior dimension,
 * and a tooltip, but NO code-path ever advanced its four factors (Parts
 * Aligned, Spatial Stability, Owner Authority, Interior Growth) — so the
 * bead was permanently stuck at CRACK_OPENED with 1/9 parts aligned.
 *
 * <h2>Canon Basis (broad strokes — chapter numbers NOT cited to avoid
 * false precision)</h2>
 * <ul>
 *   <li>Wang Lin finds the bead as a Qi Condensation youth (novel-opening
 *       arc; the bead is initially a cold stone).</li>
 *   <li>Situ Nan's remnant soul awakens; the bead cracks open. (Canon —
 *       the "first opening" is the foundational event of Wang Lin's
 *       story.)</li>
 *   <li>As Wang Lin's cultivation advances (Foundation, Core Formation,
 *       Nascent Soul, Soul Formation...), the bead's interior stabilizes,
 *       grows, and Wang Lin gains authority over it. This is a slow,
 *       multi-decade progression in the novel.</li>
 *   <li>Aligning the Five Elements + Dao fragments (handled separately by
 *       {@link HeavenDefyingBeadItem#use}) is the active side of
 *       progression. The passive side — the bead growing as Wang Lin
 *       cultivates — is what THIS service provides.</li>
 * </ul>
 *
 * <h2>Progression Model (CRON-COMPLETIONIST-95)</h2>
 * <p>Every 100 server ticks (5 seconds) the service checks each online
 * player's inventory for a Heaven-Defying Bead. If found, it queries the
 * player's {@link CultivationState} and advances the bead's NBT factors
 * according to the player's current {@link RealmId}:
 *
 * <table>
 *   <caption>Passive progression gates by realm</caption>
 *   <tr><th>Realm (≥)</th>              <th>Factor advanced</th>          <th>Delta per 5s</th></tr>
 *   <tr><td>QI_CONDENSATION</td>        <td>InteriorGrowth</td>           <td>+1</td></tr>
 *   <tr><td>FOUNDATION</td>             <td>SpatialStability</td>         <td>+2</td></tr>
 *   <tr><td>CORE_FORMATION</td>         <td>OwnerAuthority</td>           <td>+3</td></tr>
 *   <tr><td>SOUL_FORMATION</td>         <td>InteriorGrowth (bonus)</td>   <td>+2 (stacks → +3 total)</td></tr>
 *   <tr><td>NIRVANA_SCRYER</td>         <td>All factors (bonus)</td>      <td>x2 multiplier</td></tr>
 * </table>
 *
 * <p>The gates are realm-locked because in canon:
 * <ul>
 *   <li>Qi Condensation is when Wang Lin first starts sensing and using
 *       the bead — InteriorGrowth begins here.</li>
 *   <li>Foundation Establishment is when Wang Lin first stabilizes his
 *       cultivation base — SpatialStability begins here (the bead's
 *       interior was unstable before this).</li>
 *   <li>Core Formation is when Wang Lin forms his golden core and gains
 *       real authority — OwnerAuthority begins here (Situ Nan approves of
 *       him at this stage in canon).</li>
 *   <li>Soul Formation is when Wang Lin's divine sense matures — bonus
 *       InteriorGrowth (he actively cultivates inside the bead's
 *       time-dilated interior).</li>
 *   <li>Nirvana Scryer and above is when Wang Lin's comprehension is so
 *       profound that all aspects of the bead advance faster.</li>
 * </ul>
 *
 * <h2>Cap and Re-Calculation</h2>
 * <p>Each factor is capped at 10000 by the existing setters in
 * {@link HeavenDefyingBeadItem}. Each setter calls
 * {@link HeavenDefyingBeadItem#recalculateStage}, which uses
 * {@link BeadCapacityModel#stageFor} to determine the new
 * {@link BeadInteriorStage}. When the stage changes, this service fires
 * a chat broadcast to the player ("The Heaven-Defying Bead resonates
 * with your cultivation... Interior Stage: &lt;description&gt;") — giving
 * the player immediate feedback that their cultivation is progressing the
 * bead.
 *
 * <h2>Why PlayerTickEvent, not ServerTickEvent</h2>
 * <p>{@link TickEvent.PlayerTickEvent} fires per-player per-tick, which
 * lets us cheaply scope the bead-search to that player's inventory only.
 * {@link TickEvent.ServerTickEvent} would require iterating all online
 * players each tick — same total work, but less idiomatic for Forge and
 * harder to extend with player-specific gating (e.g., future
 * meditation-only progression). The pattern mirrors
 * {@code CultivationEvents.onPlayerTick}.
 *
 * <h2>Single-Player Maximalism (Article XLIII)</h2>
 * <p>This service runs unconditionally for every online player. In
 * single-player maximalism mode (the only mode the mod supports), this is
 * exactly right: the player IS Wang Lin, and the bead's progression is
 * the central cultivation arc of the novel. There is no fairness concern
 * — there is only one player.
 *
 * <h2>Inventory Scan Strategy</h2>
 * <p>{@link #findBead(ServerPlayer)} scans main-hand → off-hand →
 * inventory (in that order) and returns the first non-empty bead stack.
 * The scan is O(36 + 2) = O(38) per player per 100 ticks — negligible.
 * The bead's NBT is mutated in place; because ItemStack is a reference
 * type stored directly in the inventory, mutations persist without
 * explicit setItem calls.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see HeavenDefyingBeadItem
 * @see BeadInteriorStage
 * @see BeadCapacityModel
 */
public final class BeadProgressionService {

    /** Tick interval: every N ticks the service advances the bead's factors. */
    public static final int TICK_INTERVAL = 100;  // 5 seconds at 20 TPS

    /** Nirvana Scryer+ multiplier for all factor gains. */
    public static final int NIRVANA_MULTIPLIER = 2;

    private BeadProgressionService() {}

    /**
     * Per-player per-tick hook. Fires on END phase only (after the player's
     * state has been updated this tick). Skips client-side (no inventory
     * progression on client). Runs only every {@link #TICK_INTERVAL} ticks
     * per player.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide()) return;
        if (!(event.player instanceof ServerPlayer serverPlayer)) return;
        // Per-player tick gate — each player fires on a different tickCount %
        // TICK_INTERVAL residue, naturally distributing load across the 100-tick
        // window.
        if (serverPlayer.tickCount % TICK_INTERVAL != 0) return;

        // Locate the player's bead (if any).
        ItemStack beadStack = findBead(serverPlayer);
        if (beadStack.isEmpty()) return;
        if (!(beadStack.getItem() instanceof HeavenDefyingBeadItem beadItem)) return;

        // Locate the player's cultivation state.
        CultivationState state;
        try {
            state = CultivationCapability.getOrThrow(serverPlayer);
        } catch (Throwable t) {
            // Capability missing — log once and skip. This should never happen
            // (capability is attached to every player on spawn), but defensive.
            Ergenverse.LOGGER.debug("[Ergenverse] BeadProgressionService: " +
                    "CultivationState missing for {} — skipping bead tick.",
                    serverPlayer.getName().getString());
            return;
        }

        RealmId realm = state.getCurrentRealm();
        if (realm == null || realm == RealmId.MORTAL) {
            // Mortals can't progress the bead — canon: Wang Lin only started
            // progressing the bead after entering Qi Condensation.
            return;
        }

        // Snapshot the old stage for change detection.
        BeadInteriorStage oldStage = beadItem.getStage(beadStack);

        // Apply progression rules.
        applyPassiveProgression(beadItem, beadStack, realm);

        // Check for stage change and broadcast.
        BeadInteriorStage newStage = beadItem.getStage(beadStack);
        if (newStage != oldStage) {
            broadcastStageChange(serverPlayer, oldStage, newStage);
        }
    }

    /**
     * Apply the passive realm-gated progression to the bead's three
     * time-driven factors (InteriorGrowth, SpatialStability, OwnerAuthority).
     *
     * <p>Parts Aligned is NOT advanced here — it requires the player to
     * actively absorb an Element/Dao essence via right-click (see
     * {@link HeavenDefyingBeadItem#use}).
     */
    private static void applyPassiveProgression(HeavenDefyingBeadItem beadItem,
                                                 ItemStack stack,
                                                 RealmId realm) {
        // Compute the multiplier for high-realm bonus.
        int multiplier = realm.isAtLeast(RealmId.NIRVANA_SCRYER) ? NIRVANA_MULTIPLIER : 1;

        // ── 1. InteriorGrowth (QI_CONDENSATION+): the bead's interior world
        //     slowly grows as the cultivator's qi flows through it.
        //     Bonus at SOUL_FORMATION+ (the cultivator actively cultivates
        //     inside the bead's time-dilated interior).
        if (realm.isAtLeast(RealmId.QI_CONDENSATION)) {
            int growth = beadItem.getInteriorGrowth(stack);
            int delta = 1;
            if (realm.isAtLeast(RealmId.SOUL_FORMATION)) delta += 2;
            delta *= multiplier;
            beadItem.setInteriorGrowth(stack, growth + delta);
        }

        // ── 2. SpatialStability (FOUNDATION+): the bead's spatial structure
        //     solidifies as the cultivator forms a stable foundation.
        if (realm.isAtLeast(RealmId.FOUNDATION)) {
            int stability = beadItem.getSpatialStability(stack);
            int delta = 2 * multiplier;
            beadItem.setSpatialStability(stack, stability + delta);
        }

        // ── 3. OwnerAuthority (CORE_FORMATION+): the bead recognizes the
        //     cultivator as its true master only after the golden core forms.
        //     In canon, Situ Nan explicitly approves of Wang Lin around this
        //     stage.
        if (realm.isAtLeast(RealmId.CORE_FORMATION)) {
            int authority = beadItem.getOwnerAuthority(stack);
            int delta = 3 * multiplier;
            beadItem.setOwnerAuthority(stack, authority + delta);
        }
    }

    /**
     * Scan the player's main hand, off hand, and main inventory (in that
     * order) for a Heaven-Defying Bead stack. Returns the first match.
     *
     * <p>Order matters: the active-hand bead is preferred (the one the
     * player is holding). If neither hand holds one, the inventory is
     * scanned — the player doesn't need to hold the bead for it to grow.
     * This matches canon: Wang Lin often kept the bead in his storage
     * pouch, and it still grew as he cultivated.
     *
     * @return the bead stack, or {@link ItemStack#EMPTY} if none found
     */
    private static ItemStack findBead(ServerPlayer player) {
        // 1. Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof HeavenDefyingBeadItem) return mainHand;

        // 2. Off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof HeavenDefyingBeadItem) return offHand;

        // 3. Main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HeavenDefyingBeadItem) return stack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Broadcast a stage-change event to the player. Sends two chat messages:
     * <ol>
     *   <li>A golden "resonance" line indicating the bead is responding
     *       to the player's cultivation.</li>
     *   <li>An aqua line stating the new interior stage's description.</li>
     * </ol>
     *
     * <p>This is the only player-facing feedback from the passive
     * progression. Stage changes are rare (a player will see maybe 5 in
     * a full playthrough), so a chat broadcast is appropriate. Frequent
     * factor increments (every 5s) are silent — they're bookkeeping, not
     * narrative.
     *
     * @param player   the player whose bead advanced
     * @param oldStage the previous interior stage
     * @param newStage the new interior stage (always &gt; oldStage)
     */
    private static void broadcastStageChange(ServerPlayer player,
                                              BeadInteriorStage oldStage,
                                              BeadInteriorStage newStage) {
        player.sendSystemMessage(
                Component.literal("The Heaven-Defying Bead resonates with your cultivation.")
                        .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(
                Component.literal("  Interior Stage: ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(newStage.description)
                                .withStyle(ChatFormatting.AQUA)));

        Ergenverse.LOGGER.info("[Ergenverse] Bead stage advanced for {}: {} → {}",
                player.getName().getString(), oldStage.name(), newStage.name());
    }
}
