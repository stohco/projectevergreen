package dev.ergenverse.client;

import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.network.ClientCultivationCache;
// CultivationHudOverlay is in the same package (client)
import dev.ergenverse.network.ERNetwork;
import dev.ergenverse.network.ToggleMeditationC2SPacket;
import dev.ergenverse.perception.PerceptionTier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * Client-side event handlers for the cultivation vertical slice.
 *
 * <p>This class handles:
 * <ul>
 *   <li><b>Piece 1:</b> Sneak-right-click detection on spirit vein blocks,
 *       sends {@link ToggleMeditationC2SPacket} to the server.</li>
 *   <li><b>Piece 4:</b> Perception Shift — {@link RenderNameTagEvent} translates
 *       entity names based on the player's cultivation tier.
 *       {@link RenderLivingEvent.Pre} hides spirit beasts from
 *       mortals (they're physically there but invisible).</li>
 *   <li><b>Piece 6:</b> HUD overlay via {@link RenderGuiOverlayEvent.Post}.</li>
 * </ul>
 *
 * <p>Registered on the FORGE bus (not MOD bus) because these are
 * runtime events, not mod-lifecycle events.
 */
@Mod.EventBusSubscriber(modid = "ergenverse", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class CultivationClientEvents {

    private CultivationClientEvents() {}

    /** Spirit vein blocks the player can meditate at. Must match server-side set. */
    private static final java.util.Set<Block> SPIRIT_VEIN_BLOCKS;
    static {
        java.util.Set<Block> s = new java.util.HashSet<>();
        s.add(Blocks.NETHER_QUARTZ_ORE);
        s.add(Blocks.CALCITE);
        // Add all ergenverse blocks (spirit herbs + anchor blocks)
        try {
            net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKeys().stream()
                    .filter(rl -> rl.getNamespace().equals("ergenverse"))
                    .forEach(rl -> s.add(net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(rl)));
        } catch (Exception ignored) { /* registry not yet populated in some contexts */ }
        SPIRIT_VEIN_BLOCKS = java.util.Collections.unmodifiableSet(s);
    }

    // ── Piece 1: Sneak-Right-Click Meditation Trigger ──────────────

    /**
     * Detect sneak-right-click on spirit vein blocks and send the
     * toggle meditation packet to the server.
     *
     * <p>The server validates: block type, range, and existing session.
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof LocalPlayer localPlayer)) return;
        if (event.isCanceled()) return;
        if (!localPlayer.isShiftKeyDown()) return;

        BlockPos pos = event.getPos();
        if (!SPIRIT_VEIN_BLOCKS.contains(localPlayer.level().getBlockState(pos).getBlock())) return;

        // Send toggle packet with the target position
        ERNetwork.getChannel().sendToServer(new ToggleMeditationC2SPacket(
                pos.getX(), pos.getY(), pos.getZ()));
        event.setCanceled(true);
    }

    // ── Piece 4: Perception Shift — Name Tag Translation ───────────

    /**
     * Translate entity name tags based on the player's perception tier.
     *
     * <p>Per the Prime Directive: reality is objective; cultivation changes
     * understanding, not existence. The entity IS Wang Tiangui regardless
     * of your realm. But a mortal sees "a tired old man" while a Qi
     * Condensation cultivator sees "Wang Tiangui (Faint Qi Presence)".
     *
     * <p>Uses the PerceptionBridge to run the full PerceptionEngine
     * pipeline: EntityCultivator → ObjectiveNature → PerceptionEngine →
     * PerceptionResult. Falls back to the legacy hardcoded translation
     * if the bridge is unavailable.
     *
     * <p>Only affects EntityCultivator entities (our polymorphic NPC shell).
     */
    @SubscribeEvent
    public static void onRenderNameTag(RenderNameTagEvent event) {
        if (!ClientCultivationCache.isAvailable()) return;

        Entity entity = event.getEntity();
        if (!(entity instanceof dev.ergenverse.entity.EntityCultivator cultivator)) return;

        // Use PerceptionBridge for rich, engine-driven perception
        String perceivedName = PerceptionBridge.getPerceivedName(cultivator, null);
        if (perceivedName != null) {
            PerceptionTier tier = ClientCultivationCache.getPerceptionTier();

            // Build multi-line name tag based on perception tier
            MutableComponent nameLine = Component.literal(perceivedName);

            // Show perception description as a subtitle below the name
            // (only for non-mortal observers — mortals see the mundane name only)
            if (tier.order >= PerceptionTier.QI_CONDENSATION.order) {
                String desc = PerceptionBridge.getPerceivedDescription(cultivator);
                if (desc != null && !desc.isEmpty()) {
                    // Truncate description for name tag (max 60 chars)
                    if (desc.length() > 60) desc = desc.substring(0, 57) + "...";
                    MutableComponent descLine = Component.literal(desc).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                    nameLine = nameLine.copy().append("\n").append(descLine);
                }
            }

            event.setContent(nameLine);
            return;
        }

        // Fallback: legacy hardcoded translation
        var cache = ClientCultivationCache.get();
        int realmOrder = cache.realmOrder;
        String displayName = cultivator.getDisplayName().getString();
        if (displayName == null) displayName = "Unknown";

        String translated = translateNameByPerception(displayName, realmOrder);
        if (translated != null) {
            event.setContent(Component.literal(translated));
        }
    }

    /**
     * Translate an NPC's display name based on the observer's realm.
     *
     * <p>Perception tiers:
     * <ul>
     *   <li>Mortal (0): sees mundane description, no cultivation info</li>
     *   <li>Qi Condensation (1): sees actual name + faint qi presence</li>
     *   <li>Foundation (2): sees name + realm if the entity has one</li>
     *   <li>Nascent Soul+ (3+): sees full canonical name + realm</li>
     * </ul>
     */
    @Nullable
    private static String translateNameByPerception(String displayName, int realmOrder) {
        if (realmOrder == 0) {
            return "\u00A77" + toMundaneDescription(displayName);
        }
        if (realmOrder == 1) {
            return "\u00A7a" + displayName + "\u00A77 (Faint Qi Presence)";
        }
        if (realmOrder == 2) {
            return "\u00A7e" + displayName;
        }
        // Nascent Soul and above: full information
        return "\u00A7b" + displayName;
    }

    /**
     * Generate a mundane description for a cultivator name.
     * Used when the observer is a mortal who can't sense Qi.
     *
     * <p>Simple heuristic: if the name contains known cultivation
     * terms, replace with a mundane equivalent. Otherwise just
     * show "A person" (the mortal can't sense anything special).
     */
    private static String toMundaneDescription(String name) {
        if (name.contains("Wang Tiangui")) return "A tired old man";
        if (name.contains("Teng")) return "An imposing noble";
        if (name.contains("Qiu")) return "A stern elder";
        return "A person";
    }

    // ── Piece 4: Perception Shift — Hidden Entities ───────────────

    /**
     * Hide entities from mortals based on perception tier.
     *
     * <p>Per the Prime Directive: the entity EXISTS on the server
     * regardless. A mortal standing next to a concealed cultivator
     * simply cannot perceive them. The cultivator is there, they
     * can hurt the mortal, but the mortal's eyes see nothing.
     *
     * <p>Uses PerceptionBridge to check concealment. If the
     * PerceptionResult says the entity is concealed, cancel the
     * render event — the entity becomes invisible to this observer.
     */
    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!ClientCultivationCache.isAvailable()) return;
        if (!(event.getEntity() instanceof dev.ergenverse.entity.EntityCultivator cultivator)) return;

        PerceptionTier tier = ClientCultivationCache.getPerceptionTier();

        // v2: Concealed entities (those with a concealment formation
        // or Dao that exceeds the observer's perception floor) are
        // hidden. For v1, all entities are visible but names change.
        // This hook is ready for v2 when concealment data is added.
    }

    // ── Piece 5: Perception-Filtered Item Tooltips ───────────────

    /**
     * Modify item tooltips based on the player's perception tier.
     *
     * <p>Spirit herbs, formation tools, and other cultivation items
     * reveal additional information at higher perception tiers:
     * <ul>
     *   <li><b>Mortal</b>: sees the basic item name. "Sea Pickle"</li>
     *   <li><b>Qi Condensation</b>: sees "Spirit Herb (grade unknown)"</li>
     *   <li><b>Foundation</b>: sees the herb name + grade + basic effect</li>
     *   <li><b>Nascent Soul+</b>: sees full canon description</li>
     * </ul>
     *
     * <p>Only modifies Ergenverse mod items (namespace "ergenverse").
     * Vanilla items pass through unchanged.
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!ClientCultivationCache.isAvailable()) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        Item item = stack.getItem();
        var itemKey = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(item);
        if (itemKey == null) return;

        PerceptionTier tier = ClientCultivationCache.getPerceptionTier();
        RealmId realm = ClientCultivationCache.getRealm();

        // ── Perception-block items (vanilla blocks that are spirit herbs/veins) ──
        // These vanilla items get perception-tiered tooltips when held by cultivators.
        // A mortal holding a Sea Pickle sees nothing special.
        // A Qi Condensation cultivator sees "[?] Spirit Herb".
        // A Foundation cultivator sees the true name + grade.
        String blockItemId = "minecraft:" + itemKey.getPath();
        String perceivedBlockName = dev.ergenverse.perception.AmbientPerception
                .perceiveBlockName(blockItemId, realm);
        if (perceivedBlockName != null) {
            event.getToolTip().add(Component.literal(perceivedBlockName));
            String blockDesc = dev.ergenverse.perception.AmbientPerception
                    .perceiveBlockDescription(blockItemId, realm);
            if (blockDesc != null) {
                event.getToolTip().add(Component.literal("")
                        .append(Component.literal(blockDesc)
                                .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
            }
            if (!dev.ergenverse.perception.AmbientPerception.canHarvestTrueForm(blockItemId, realm)) {
                event.getToolTip().add(Component.literal("")
                        .append(Component.literal("Your perception is insufficient to harvest its true form.")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
            return;
        }

        // Only modify ergenverse items for the rest
        if (!"ergenverse".equals(itemKey.getNamespace())) {
            return;
        }

        String itemId = itemKey.getPath();

        // Spirit herb items: reveal cultivation info at higher tiers
        if (isSpiritHerbItem(itemId)) {
            addSpiritHerbTooltip(event.getToolTip(), itemId, tier);
            return;
        }

        // Formation/restriction tools: reveal at Foundation+
        if (isFormationItem(itemId)) {
            addFormationTooltip(event.getToolTip(), itemId, tier);
            return;
        }

        // Soul-related items: reveal at Nascent Soul+
        if (isSoulItem(itemId)) {
            addSoulItemTooltip(event.getToolTip(), itemId, tier);
            return;
        }

        // All other ergenverse items: at Transcendence, show origin hint
        if (tier.order >= PerceptionTier.TRANSCENDENCE.order) {
            event.getToolTip().add(Component.literal("").append(
                    Component.literal("Dao Origin: trace the karmic web of this artifact.")
                            .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)));
        }
    }

    /** Check if an item ID corresponds to a spirit herb. */
    private static boolean isSpiritHerbItem(String id) {
        return id.contains("herb") || id.contains("lotus") || id.contains("grass")
                || id.contains("clover") || id.contains("mushroom") || id.contains("flower");
    }

    /** Check if an item ID corresponds to a formation/restriction tool. */
    private static boolean isFormationItem(String id) {
        return id.contains("restriction") || id.contains("formation") || id.contains("array")
                || id.contains("flag");
    }

    /** Check if an item ID is soul-related. */
    private static boolean isSoulItem(String id) {
        return id.contains("soul") || id.contains("banner") || id.contains("gourd")
                || id.contains("billion_soul");
    }

    /** Add perception-tiered tooltip lines for spirit herb items. */
    private static void addSpiritHerbTooltip(java.util.List<Component> tooltip, String itemId, PerceptionTier tier) {
        // Qi Condensation+: identify as spirit herb
        if (tier.order >= PerceptionTier.QI_CONDENSATION.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("[Spirit Herb]")
                            .withStyle(ChatFormatting.DARK_GREEN)));
        }
        // Foundation+: show estimated grade
        if (tier.order >= PerceptionTier.FOUNDATION.order) {
            String grade = estimateHerbGrade(itemId);
            tooltip.add(Component.literal("").append(
                    Component.literal("Estimated Grade: " + grade)
                            .withStyle(ChatFormatting.GREEN)));
        }
        // Nascent Soul+: show effect hint
        if (tier.order >= PerceptionTier.NASCENT_SOUL.order) {
            String effect = getHerbEffectHint(itemId);
            if (!effect.isEmpty()) {
                tooltip.add(Component.literal("").append(
                        Component.literal(effect)
                                .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
            }
        }
        // Soul Formation+: show origin
        if (tier.order >= PerceptionTier.SOUL_FORMATION.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("Origin: Planet Suzaku, Zhao Country region")
                            .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
        }
    }

    /** Add perception-tiered tooltip lines for formation items. */
    private static void addFormationTooltip(java.util.List<Component> tooltip, String itemId, PerceptionTier tier) {
        if (tier.order >= PerceptionTier.FOUNDATION.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("[Formation Tool]")
                            .withStyle(ChatFormatting.YELLOW)));
            tooltip.add(Component.literal("").append(
                    Component.literal("A formation artifact. Requires Foundation+ to deploy.")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
        if (tier.order >= PerceptionTier.SOUL_FORMATION.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("Restriction patterns visible to divine sense.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }
    }

    /** Add perception-tiered tooltip lines for soul items. */
    private static void addSoulItemTooltip(java.util.List<Component> tooltip, String itemId, PerceptionTier tier) {
        if (tier.order >= PerceptionTier.NASCENT_SOUL.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("[Soul Artifact]")
                            .withStyle(ChatFormatting.DARK_PURPLE)));
            tooltip.add(Component.literal("").append(
                    Component.literal("Contains bound soul remnants. Karmic weight detectable via divine sense.")
                            .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
        if (tier.order >= PerceptionTier.SOUL_FORMATION.order) {
            tooltip.add(Component.literal("").append(
                    Component.literal("Soul origin traces: the original owner's karmic imprint lingers.")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }
    }

    /** Estimate herb grade from item ID (simplified for v1). */
    private static String estimateHerbGrade(String id) {
        if (id.contains("soul_nourishing")) return "Grade 4 (Rare)";
        if (id.contains("nine_leaf")) return "Grade 3 (Uncommon)";
        if (id.contains("qi_gathering")) return "Grade 2 (Common)";
        if (id.contains("snow_heart")) return "Grade 3 (Uncommon)";
        if (id.contains("blood_forgetting")) return "Grade 5 (Legendary)";
        return "Grade 1 (Faint)";
    }

    /** Get a hint about the herb's effect based on its name. */
    private static String getHerbEffectHint(String id) {
        if (id.contains("soul_nourishing")) return "Nourishes the soul sea. Stabilizes cultivation foundation.";
        if (id.contains("nine_leaf")) return "Each leaf contains a different spiritual property. Rarely found in complete form.";
        if (id.contains("qi_gathering")) return "Accelerates Qi absorption during meditation. Common but essential.";
        if (id.contains("snow_heart")) return "Cools inner fire. Used in pills requiring cold-attribute ingredients.";
        if (id.contains("blood_forgetting")) return "Erases emotional attachments. Dangerous — may damage the Dao heart.";
        return "A spirit herb with unknown properties. Further study required.";
    }

    // ── Piece 6: HUD Overlay (cultivation + divine sense) ─────────

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        CultivationHudOverlay.render(event);
        DivineSenseHudOverlay.render(event);
    }
}