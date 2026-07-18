package dev.ergenverse.item;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Set;

/**
 * Jade Slip Qi-Gathering — right-click a spirit-vein or spirit-herb block while
 * holding a Jade Slip to channel Qi from the block into the cultivator's dantian.
 *
 * <p><b>Canon:</b> Jade slips are the standard Qi-channeling medium in xianxia.
 * Wang Lin uses them to absorb Qi from spirit veins throughout the Renegade
 * Immortal series. A spirit vein's Qi is finite but regenerates; a spirit herb
 * releases its stored Qi when harvested by a slip.
 *
 * <p><b>Per the Prime Directive:</b> the spirit-vein block exists objectively;
 * the Qi is objectively there. A mortal holding a jade slip would still feel
 * the Qi flow — they just wouldn't understand what it is. We require Qi
 * Condensation+ to USE the slip effectively (a mortal has no meridians to
 * channel through), but the slip itself works on any cultivator.
 *
 * <h2>Qi Yield Per Block Type</h2>
 * <ul>
 *   <li><b>Spirit vein quartz ore</b> (ergenverse:qi_gathering_grass configured_feature
 *       is replaced by ergenverse:spirit_vein_quartz_ore placed_feature): large Qi yield
 *       (50 absolute Qi). Once per block per 24000-tick MC day.</li>
 *   <li><b>Custom spirit herb blocks</b> (snow_heart_herb, soul_nourishing_lotus, etc.):
 *       medium Qi yield (20 absolute Qi). Once per block per MC day.</li>
 *   <li><b>Vanilla stand-ins</b> (sea_pickle, glow_berries, etc.) when perception-blocks
 *       are recognized: small Qi yield (10 absolute Qi). Once per block per MC day.</li>
 * </ul>
 *
 * <h2>Cooldown</h2>
 * <p>Per-block cooldown of one MC day (24000 ticks) prevents Qi farming exploits.
 * The block itself is NOT consumed — the Qi regenerates naturally (canon: spirit
 * veins replenish over time as world Qi flows through them).
 */
@Mod.EventBusSubscriber(modid = Ergenverse.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class JadeSlipQiGatheringEvent {

    private JadeSlipQiGatheringEvent() {}

    /** Qi yield per spirit vein ore block. */
    private static final double SPIRIT_VEIN_QI_YIELD = 50.0;

    /** Qi yield per custom spirit herb block. */
    private static final double SPIRIT_HERB_QI_YIELD = 20.0;

    /** Qi yield per vanilla perception-block stand-in. */
    private static final double VANILLA_PERCEPTION_BLOCK_QI_YIELD = 10.0;

    /** Cooldown per block: 1 MC day = 24000 ticks. */
    private static final long COOLDOWN_TICKS = 24000L;

    /** Tracks per-block cooldowns: key = "dim:x:y:z", value = game time of last harvest. */
    private static final java.util.Map<String, Long> BLOCK_COOLDOWNS = new java.util.concurrent.ConcurrentHashMap<>();

    /** Vanilla perception-block stand-ins (from AmbientPerception). */
    private static final Set<ResourceLocation> VANILLA_PERCEPTION_BLOCKS = new HashSet<>();
    static {
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "sea_pickle"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "glow_berries"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "orange_tulip"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "lily_of_the_valley"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "brown_mushroom"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "red_mushroom"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "nether_quartz_ore"));
        VANILLA_PERCEPTION_BLOCKS.add(new ResourceLocation("minecraft", "calcite"));
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isCanceled()) return;
        if (event.getHand() != InteractionHand.MAIN_HAND) return;

        // Must be holding the jade slip
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        ResourceLocation itemRl = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemRl == null || !itemRl.getPath().equals("jade_slip")) return;

        // Player must be at Qi Condensation+ to use the slip
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return;
        CultivationState state = stateOpt.resolve().get();
        if (state.getCurrentRealm().order < RealmId.QI_CONDENSATION.order) {
            player.sendSystemMessage(Component.literal(
                "\u00A77You hold the jade slip, but your meridians are closed.\u00A7r\n" +
                "\u00A78Cultivate to Qi Condensation before the slip can channel Qi."));
            return;
        }

        BlockPos pos = event.getPos();
        Level level = player.level();
        BlockState blockState = level.getBlockState(pos);
        Block block = blockState.getBlock();
        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(block);
        if (blockId == null) return;

        // Determine yield category
        double yield;
        String blockCategory;
        if (isCustomSpiritHerb(blockId) || isCustomSpiritVein(blockId)) {
            // Custom ergenverse herb/vein block
            yield = blockId.getPath().contains("spirit_vein") ? SPIRIT_VEIN_QI_YIELD : SPIRIT_HERB_QI_YIELD;
            blockCategory = blockId.getPath();
        } else if (VANILLA_PERCEPTION_BLOCKS.contains(blockId)) {
            yield = VANILLA_PERCEPTION_BLOCK_QI_YIELD;
            blockCategory = blockId.toString();
        } else {
            return; // not a Qi-bearing block
        }

        // Check cooldown
        String key = cooldownKey(level, pos);
        long now = level.getGameTime();
        Long last = BLOCK_COOLDOWNS.get(key);
        if (last != null && (now - last) < COOLDOWN_TICKS) {
            long remainingTicks = COOLDOWN_TICKS - (now - last);
            long remainingSeconds = remainingTicks / 20;
            player.sendSystemMessage(Component.literal(
                "\u00A77This block's Qi is still regenerating.\u00A7r\n" +
                "\u00A78Try again in ~" + remainingSeconds + "s."));
            return;
        }

        // Channel the Qi
        double before = state.getAbsoluteQi();
        state.addQi(yield);
        double after = state.getAbsoluteQi();
        double gained = after - before;
        BLOCK_COOLDOWNS.put(key, now);

        // Damage the jade slip by 1 (slips wear out with use)
        stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(event.getHand()));

        player.sendSystemMessage(Component.literal(
            String.format("\u00A7b\u2728 The jade slip channels Qi from the %s.\u00A7r\n" +
                          "\u00A7a+%.1f Qi\u00A7r (\u00A7e%.1f / %.1f\u00A7r)",
                blockCategory, gained, after, state.getMaxQi())));

        // Visual feedback: end rod particles at the block
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                12, 0.3, 0.5, 0.3, 0.05);
        }

        // ── Loot drop: Foundation+ cultivators have a chance to harvest the actual herb ──
        // Per canon: higher-realm cultivators can extract the herb's physical form
        // via Qi channeling, not just absorb its Qi.
        if (state.getCurrentRealm().order >= RealmId.FOUNDATION.order && isCustomSpiritHerb(blockId)) {
            // Drop chance scales with cultivation tier:
            // Foundation = 15%, Core Formation = 25%, Nascent Soul = 40%, Soul Formation = 60%
            double dropChance = 0.15 + (state.getCurrentRealm().order - RealmId.FOUNDATION.order) * 0.08;
            if (dropChance > 0.6) dropChance = 0.6;
            if (player.getRandom().nextDouble() < dropChance) {
                // Try to find the corresponding herb item in the ergenverse registry
                ResourceLocation herbItemId = new ResourceLocation(Ergenverse.MOD_ID, blockId.getPath());
                Item herbItem = ForgeRegistries.ITEMS.getValue(herbItemId);
                if (herbItem != null) {
                    ItemStack herbDrop = new ItemStack(herbItem, 1);
                    if (player.getInventory().add(herbDrop)) {
                        player.sendSystemMessage(Component.literal(
                            "\u00A7a\u2618 The jade slip extracts a " + blockId.getPath().replace("_", " ") + "!"));
                    } else {
                        // Inventory full — drop as entity
                        player.drop(herbDrop, false);
                        player.sendSystemMessage(Component.literal(
                            "\u00A7e\u2618 A " + blockId.getPath().replace("_", " ") + " materializes but your inventory is full!"));
                    }
                }
            }
        }

        Ergenverse.LOGGER.debug("[Ergenverse] {} channeled {} Qi from {} at {} via jade slip.",
                player.getName().getString(), String.format("%.1f", gained), blockCategory, pos);
    }

    /** Is this an ergenverse custom spirit herb block? */
    private static boolean isCustomSpiritHerb(ResourceLocation blockId) {
        if (!blockId.getNamespace().equals(Ergenverse.MOD_ID)) return false;
        String p = blockId.getPath();
        return p.equals("snow_heart_herb") || p.equals("soul_nourishing_lotus")
                || p.equals("fire_bloom_lotus") || p.equals("nine_leaf_clover")
                || p.equals("qi_gathering_grass") || p.equals("blood_forgetting_grass")
                || p.equals("void_nether_grass") || p.equals("dao_trace_vine")
                || p.equals("sword_edge_moss") || p.equals("five_color_ginseng")
                || p.equals("foundation_root_vine") || p.equals("reincarnation_lily")
                || p.equals("heart_devil_flower") || p.equals("demon_corpse_mushroom")
                || p.equals("vermilion_blood_ginseng");
    }

    /** Is this an ergenverse custom spirit vein block? (v1: same as herb stand-ins, but flagged.) */
    private static boolean isCustomSpiritVein(ResourceLocation blockId) {
        if (!blockId.getNamespace().equals(Ergenverse.MOD_ID)) return false;
        String p = blockId.getPath();
        return p.contains("spirit_vein") || p.contains("qi_vein");
    }

    /** Build a unique cooldown key for a block position in a dimension. */
    private static String cooldownKey(Level level, BlockPos pos) {
        return level.dimension().location() + ":" + pos.getX() + ":" + pos.getY() + ":" + pos.getZ();
    }

    /** Clear all cooldowns (called on server stop to free memory). */
    public static void clearCooldowns() {
        BLOCK_COOLDOWNS.clear();
    }
}
