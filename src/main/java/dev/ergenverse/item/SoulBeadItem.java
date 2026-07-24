package dev.ergenverse.item;

import dev.ergenverse.simulation.action.SimulationActions;
import dev.ergenverse.simulation.event.ActionDescriptors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * SoulBeadItem — a reusable soul-power accumulator (魂珠).
 *
 * <p><b>Canon:</b> In the Renegade Immortal universe, soul beads are spiritual
 * treasures that absorb, store, and discharge soul-force. Cultivators above
 * Foundation Establishment carry one as a reserve of spiritual qi for emergencies
 * — a qi-battery that can be tapped when the cultivator's own reserves are
 * exhausted or when a sudden burst of soul-power is required for a technique.
 *
 * <p><b>Right-click mechanics:</b>
 * <ul>
 *   <li>The player channels soul-force into the bead, gaining one Soul Power charge.</li>
 *   <li>The charge is stored on the player's persistent NBT under
 *       {@link #SOUL_POWER_KEY} (max {@link #MAX_SOUL_POWER}).</li>
 *   <li>A cooldown of {@link #COOLDOWN_TICKS} ticks prevents spam-clicking.</li>
 *   <li>The item is NOT consumed — soul beads are reusable artifacts (Constitution
 *       Article III: "Design reality, not progression").</li>
 *   <li>Each channel publishes a {@code player.spell.cast} event via
 *       {@link SimulationActions#spellCast} so the WangLinReasoningEngine and
 *       CanonDivergenceRecorder can react (2026-07-23 event-sourced pivot).</li>
 * </ul>
 *
 * <p><b>Soul Power as a resource:</b> Other systems can read a player's stored
 * soul power via {@link #getSoulPower(Player)} and consume it via
 * {@link #consumeSoulPower(Player, int)}. Future subscribers (flying sword
 * upgrades, soul art casting, breakthrough assistance) hook into this resource
 * without coupling to this item directly — they read from the player's NBT.
 *
 * <p><b>Self-critique:</b> Soul power is a flat integer resource, not a
 * tiered/qi-graded one. No visual HUD yet — the player only sees the charge
 * count in the item tooltip. No particle/sound cue when the resource is
 * spent by another system. The cooldown is uniform; canon implies higher
 * cultivation allows faster soul-force channeling.
 */
public class SoulBeadItem extends Item {

    /** NBT key under the player's Forge persistent data that stores soul power. */
    public static final String SOUL_POWER_KEY = "ergenverse.SoulPower";

    /** Maximum soul-power charges a single player can bank. */
    public static final int MAX_SOUL_POWER = 10;

    /** Cooldown (in ticks) between channel activations. 200 ticks = 10 seconds. */
    public static final int COOLDOWN_TICKS = 200;

    /** Soul-power gained per channel. */
    public static final int CHARGE_PER_USE = 1;

    public SoulBeadItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(level instanceof ServerLevel sl) || !(player instanceof ServerPlayer sp)) {
            return InteractionResultHolder.success(stack);
        }

        int current = getSoulPower(player);
        if (current >= MAX_SOUL_POWER) {
            // Bead is full — play a "reject" sound and do nothing.
            player.displayClientMessage(Component.literal("\u00A7cThe soul bead is already saturated."), true);
            player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 0.6F, 0.7F);
            return InteractionResultHolder.fail(stack);
        }

        // ── Channel soul-force ──
        int newCharge = Math.min(MAX_SOUL_POWER, current + CHARGE_PER_USE);
        setSoulPower(player, newCharge);

        // Cooldown
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        // Feedback: sound + particles + message
        player.playSound(SoundEvents.SOUL_SAND_PLACE, 0.8F, 1.4F);
        player.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 0.5F, 1.6F);
        sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL,
                player.getX(), player.getY() + 1.0, player.getZ(),
                8, 0.4, 0.6, 0.4, 0.05);
        player.displayClientMessage(Component.literal(
                "\u00A7bSoul Power: " + newCharge + "/" + MAX_SOUL_POWER), true);

        // ── Event-sourced: publish spell.cast through SimulationActions ──
        // Per the 2026-07-23 directive: player is a first-class actor. Soul bead
        // channel is a qi-disturbance that the simulation must observe.
        SimulationActions.spellCast(sp, "Soul Bead Channel", "soul_art",
                1.5f, ActionDescriptors.Visibility.LOCAL);

        return InteractionResultHolder.success(stack);
    }

    // ── Soul Power NBT helpers (callable from any system) ────────────────

    /**
     * Read the player's stored soul-power charges.
     * Uses Forge's persistent-data API so the value survives death/relog.
     */
    public static int getSoulPower(Player player) {
        CompoundTag persistent = player.getPersistentData();
        return persistent.getInt(SOUL_POWER_KEY);
    }

    /**
     * Set the player's soul-power charges (clamped to [0, MAX]).
     * Caller is responsible for any cooldown/consume semantics.
     */
    public static void setSoulPower(Player player, int value) {
        int clamped = Math.max(0, Math.min(MAX_SOUL_POWER, value));
        player.getPersistentData().putInt(SOUL_POWER_KEY, clamped);
    }

    /**
     * Consume soul-power charges if available. Returns true if the consumption
     * succeeded, false if the player did not have enough stored.
     *
     * <p>Other systems (flying sword upgrades, soul art casting, breakthrough
     * assistance) call this to spend the resource — they never write directly
     * to the player's NBT, preserving the single-actor-channel principle.
     */
    public static boolean consumeSoulPower(Player player, int amount) {
        if (amount <= 0) return true;
        int current = getSoulPower(player);
        if (current < amount) return false;
        setSoulPower(player, current - amount);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("\u00A7dSoul Bead\u00A77 (\u9b42\u73e9)"));
        tooltip.add(Component.literal("\u00A77A reusable accumulator of soul-force."));
        tooltip.add(Component.literal("\u00A77Right-click to channel +1 Soul Power charge."));
        tooltip.add(Component.literal("\u00A78Cooldown: 10s \u00b7 Max charges: " + MAX_SOUL_POWER));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Always render with the enchanted-glint shimmer — soul beads glow with
        // gathered spirit-force. Purely cosmetic.
        return true;
    }
}
