package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.HeavenDefyingBead;
import dev.ergenverse.wanglin.WangLinItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The Heaven-Defying Bead as a Minecraft {@link Item} — the most important
 * artifact in the mod and the single most important object in Wang Lin's story.
 *
 * <p><b>Canon (Renegade Immortal, Ch. 8+):</b> Found as a youth inside the Heng
 * Yue Sect stone. Contains Situ Nan's remnant soul. Houses a growing interior
 * world with 10x time dilation. Stores Li Muwan's Nascent Soul. Fuses with
 * Wang Lin's primordial spirit at Heaven Trampling. Cross-novel artifact (Su
 * Ming, Xuan Zang also wield beads — Wang Lin's is the original).
 *
 * <h2>Dual-Nature Interaction</h2>
 * <ul>
 *   <li><b>Right-click (Divine-Sense Access):</b> Opens the bead's storage menu
 *       (BeadFunctionMenu). No dimension entry needed. Available from
 *       CRACK_OPENED stage onward.</li>
 *   <li><b>Shift+Right-click (Physical Entry):</b> Teleports the player INTO the
 *       bead's interior dimension. Available from VALLEY stage onward.</li>
 * </ul>
 *
 * <h2>NBT State</h2>
 * <p>The bead's per-stack state is stored in NBT:
 * <ul>
 *   <li>{@code Ergen.Bead.Stage} — current {@link BeadInteriorStage} ordinal</li>
 *   <li>{@code Ergen.Bead.PartsAligned} — count of 9 Parts aligned (0-9)</li>
 *   <li>{@code Ergen.Bead.SpatialStability} — spatial stability 0..10000</li>
 *   <li>{@code Ergen.Bead.OwnerAuthority} — owner authority 0..10000</li>
 *   <li>{@code Ergen.Bead.InteriorGrowth} — interior growth 0..10000</li>
 *   <li>{@code Ergen.Bead.Spirit} — current {@link HeavenDefyingBead.Spirit} ordinal</li>
 *   <li>{@code Ergen.Bead.LiMuwanSoul} — whether Li Muwan's Nascent Soul is stored</li>
 *   <li>{@code Ergen.Bead.SamsaraCount} — Samsara incarnation count</li>
 *   <li>{@code Ergen.Bead.ActiveTab} — last-opened {@link BeadFunctionTab} ordinal</li>
 * </ul>
 *
 * <h2>Prime Directive Compliance</h2>
 * <p>The bead EXISTS objectively. A mortal sees a stone bead. A Foundation+
 * cultivator senses its faint pulse. A Soul Formation+ cultivator can enter
 * the interior. The bead does not change based on who looks at it —
 * what changes is the observer's ability to interact with it.
 *
 * @see HeavenDefyingBead        — canon data model (Layer 1)
 * @see BeadInteriorStage       — interior growth stages (Layer 2)
 * @see BeadCapacityModel       — capacity calculation (Layer 2)
 * @see BeadFunctionMenu        — the storage GUI (Layer 2)
 * @see BeadDimension           — the interior dimension (Layer 2)
 */
public class HeavenDefyingBeadItem extends WangLinItem {

    // ── NBT Keys ────────────────────────────────────────────────────

    public static final String NBT_STAGE = "Ergen.Bead.Stage";
    public static final String NBT_PARTS_ALIGNED = "Ergen.Bead.PartsAligned";
    public static final String NBT_SPATIAL_STABILITY = "Ergen.Bead.SpatialStability";
    public static final String NBT_OWNER_AUTHORITY = "Ergen.Bead.OwnerAuthority";
    public static final String NBT_INTERIOR_GROWTH = "Ergen.Bead.InteriorGrowth";
    public static final String NBT_SPIRIT = "Ergen.Bead.Spirit";
    public static final String NBT_LI_MUWAN_SOUL = "Ergen.Bead.LiMuwanSoul";
    public static final String NBT_SAMARA_COUNT = "Ergen.Bead.SamsaraCount";
    public static final String NBT_ACTIVE_TAB = "Ergen.Bead.ActiveTab";

    // ── Constants ───────────────────────────────────────────────────

    public HeavenDefyingBeadItem(Properties properties) {
        super(properties,
                "heaven_defying_bead",
                "Heaven-Defying Bead",
                "\u9006\u5929\u73e0",
                ArsenalCategory.ARTIFACT,
                5,   // maximum canon confidence — directly attested
                "heaven_defying_bead",
                0.0f,
                "Renegade Immortal, Ch. 8+ — Wang Lin's defining artifact");
        // Registry name is set by DeferredRegister, NOT by setRegistryName.
    }

    // ── Interaction ─────────────────────────────────────────────────

    /**
     * Right-click opens the bead's divine-sense storage menu.
     * Shift+right-click enters the bead's interior dimension.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(net.minecraft.world.level.Level level,
                                                   Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        BeadInteriorStage stage = getStage(stack);

        // DORMANT_STONE: nothing happens — it's just a stone.
        if (stage == BeadInteriorStage.DORMANT_STONE) {
            player.sendSystemMessage(
                    Component.literal("The bead is cold and lifeless. A faint pattern "
                            + "of five elements is barely visible on its surface.")
                            .withStyle(ChatFormatting.GRAY));
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown() && stage.hasPhysicalEntry) {
            // Physical entry into the bead dimension
            if (player instanceof ServerPlayer serverPlayer) {
                enterBeadDimension(serverPlayer, stack);
            }
            return InteractionResultHolder.success(stack);
        }

        // Divine-sense access: open the storage menu
        if (stage.hasStorageAccess) {
            if (player instanceof ServerPlayer serverPlayer) {
                openBeadMenu(serverPlayer, stack);
            }
            return InteractionResultHolder.success(stack);
        }

        // Should not happen (CRACK_OPENED and SMALL_SPACE both have storage),
        // but handle gracefully.
        player.sendSystemMessage(
                Component.literal("The bead pulses faintly but you cannot yet "
                        + "comprehend how to access it.")
                        .withStyle(ChatFormatting.DARK_GRAY));
        return InteractionResultHolder.fail(stack);
    }

    /**
     * Opens the bead's storage menu (divine-sense access).
     * The menu shows tabs based on the current interior stage.
     */
    private void openBeadMenu(ServerPlayer player, ItemStack stack) {
        BeadInteriorStage stage = getStage(stack);
        int slots = getCurrentSlots(stack);
        int activeTab = getActiveTab(stack);

        net.minecraft.world.inventory.MenuType<?> menuType = BeadFunctionMenu.TYPE.get();
        MenuProvider provider = new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("\u9006\u5929\u73e0 \u2014 Heaven-Defying Bead")
                        .withStyle(ChatFormatting.GOLD);
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(
                    int containerId, Inventory playerInv, Player player) {
                return BeadFunctionMenu.create(containerId, playerInv, stack,
                        stage, slots, activeTab);
            }
        };

        // Forge: open the menu on the server side
        net.minecraftforge.network.NetworkHooks.openScreen(player, provider, buf -> {
            buf.writeVarInt(stage.ordinal());
            buf.writeVarInt(slots);
            buf.writeVarInt(activeTab);
        });

        Ergenverse.LOGGER.debug("[Ergenverse] Player {} opened Heaven-Defying Bead menu "
                + "(stage={}, slots={})",
                player.getName().getString(), stage, slots);
    }

    /**
     * Teleports the player into the bead's interior dimension.
     *
     * <p>Canon: Wang Lin could physically enter the bead's interior world
     * once it had grown sufficiently (valley stage and above). Inside,
     * time runs 10x faster relative to the outside world.
     */
    private void enterBeadDimension(ServerPlayer player, ItemStack stack) {
        BeadInteriorStage stage = getStage(stack);
        double timeDilation = BeadCapacityModel.timeDilationFactor(stage);

        if (BeadDimension.KEY == null) {
            player.sendSystemMessage(
                    Component.literal("The bead's interior dimension is not yet available.")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        // Teleport to the bead dimension
        var destination = player.server.getLevel(BeadDimension.KEY);
        if (destination == null) {
            Ergenverse.LOGGER.warn("[Ergenverse] Bead dimension not loaded, "
                    + "cannot teleport player {}", player.getName().getString());
            player.sendSystemMessage(
                    Component.literal("The bead's interior world is still forming...")
                            .withStyle(ChatFormatting.GRAY));
            return;
        }

        player.teleportTo(destination,
                destination.getSharedSpawnPos().getX() + 0.5,
                destination.getSharedSpawnPos().getY() + 1,
                destination.getSharedSpawnPos().getZ() + 0.5,
                player.getYRot(), player.getXRot());

        player.sendSystemMessage(
                Component.literal("You step into the Heaven-Defying Bead.")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" Time flows " + timeDilation
                                + "x faster here.")
                                .withStyle(ChatFormatting.AQUA)));

        Ergenverse.LOGGER.info("[Ergenverse] Player {} entered the Heaven-Defying Bead "
                + "interior (stage={}, timeDilation={})",
                player.getName().getString(), stage, timeDilation);
    }

    // ── Tooltip ─────────────────────────────────────────────────────

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        BeadInteriorStage stage = getStage(stack);
        int partsAligned = getPartsAligned(stack);
        int slots = getCurrentSlots(stack);

        // Interior stage description
        tooltip.add(Component.literal("")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Interior: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(stage.description)
                        .withStyle(ChatFormatting.GOLD)));

        // Five Elements / Parts alignment progress
        tooltip.add(Component.literal("Parts Aligned: ")
                .withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(partsAligned + " / 9")
                        .withStyle(partsAligned >= 6
                                ? ChatFormatting.GOLD : ChatFormatting.AQUA)));

        if (partsAligned < 6) {
            tooltip.add(Component.literal("  (Align all 5 elements to gain true ownership)")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        // Storage capacity
        if (stage.hasStorageAccess) {
            tooltip.add(Component.literal("Storage: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(slots + " slots")
                            .withStyle(ChatFormatting.GREEN)));
        }

        // Spirit
        HeavenDefyingBead.Spirit spirit = getSpirit(stack);
        if (spirit != HeavenDefyingBead.Spirit.NONE) {
            tooltip.add(Component.literal("Spirit: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(spirit.name)
                            .withStyle(ChatFormatting.LIGHT_PURPLE)));
        }

        // Time dilation
        if (stage.hasTimeDilation) {
            tooltip.add(Component.literal("Time Dilation: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("10x (1h inside = 10h outside)")
                            .withStyle(ChatFormatting.AQUA)));
        }

        // Li Muwan's Nascent Soul
        if (hasLiMuwanSoul(stack)) {
            tooltip.add(Component.literal("Contains: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal("Li Muwan's Nascent Soul")
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC)));
        }

        // Available tabs
        tooltip.add(Component.literal("")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.literal("Functions: ")
                .withStyle(ChatFormatting.DARK_GRAY));
        for (BeadFunctionTab tab : BeadFunctionTab.values()) {
            if (stage.tabAvailable(tab)) {
                tooltip.add(Component.literal("  [")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.literal(tab.label)
                                .withStyle(ChatFormatting.GREEN))
                        .append(Component.literal("]")
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
        }

        // Interaction hints
        tooltip.add(Component.literal("")
                .withStyle(ChatFormatting.DARK_GRAY));
        if (stage.hasStorageAccess) {
            tooltip.add(Component.literal("Right-click: Open storage menu")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }
        if (stage.hasPhysicalEntry) {
            tooltip.add(Component.literal("Shift+Right-click: Enter interior world")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        // Canon source
        tooltip.add(Component.literal("Renegade Immortal, Ch. 8+ — Wang Lin's defining artifact")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));

        if (flag.isAdvanced()) {
            tooltip.add(Component.literal("BeadID: heaven_defying_bead")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    // ── NBT State Accessors ─────────────────────────────────────────

    /** Get the current interior stage. */
    public BeadInteriorStage getStage(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return BeadInteriorStage.DORMANT_STONE;
        int ordinal = stack.getTag().getInt(NBT_STAGE);
        BeadInteriorStage[] values = BeadInteriorStage.values();
        if (ordinal < 0 || ordinal >= values.length) return BeadInteriorStage.DORMANT_STONE;
        return values[ordinal];
    }

    /** Set the interior stage. Called by event-driven progression. */
    public void setStage(ItemStack stack, BeadInteriorStage stage) {
        stack.getOrCreateTag().putInt(NBT_STAGE, stage.ordinal());
    }

    /** Get the number of the 9 Parts aligned (0-9). */
    public int getPartsAligned(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.min(9, Math.max(0, stack.getTag().getInt(NBT_PARTS_ALIGNED)));
    }

    /** Set parts aligned count. */
    public void setPartsAligned(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(NBT_PARTS_ALIGNED, Math.min(9, Math.max(0, count)));
        recalculateStage(stack);
    }

    /** Get spatial stability (0-10000). */
    public int getSpatialStability(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.min(10000, Math.max(0, stack.getTag().getInt(NBT_SPATIAL_STABILITY)));
    }

    /** Set spatial stability. */
    public void setSpatialStability(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(NBT_SPATIAL_STABILITY, Math.min(10000, Math.max(0, value)));
        recalculateStage(stack);
    }

    /** Get owner authority (0-10000). */
    public int getOwnerAuthority(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.min(10000, Math.max(0, stack.getTag().getInt(NBT_OWNER_AUTHORITY)));
    }

    /** Set owner authority. */
    public void setOwnerAuthority(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(NBT_OWNER_AUTHORITY, Math.min(10000, Math.max(0, value)));
        recalculateStage(stack);
    }

    /** Get interior growth (0-10000). */
    public int getInteriorGrowth(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.min(10000, Math.max(0, stack.getTag().getInt(NBT_INTERIOR_GROWTH)));
    }

    /** Set interior growth. */
    public void setInteriorGrowth(ItemStack stack, int value) {
        stack.getOrCreateTag().putInt(NBT_INTERIOR_GROWTH, Math.min(10000, Math.max(0, value)));
        recalculateStage(stack);
    }

    /** Get the bound spirit. */
    public HeavenDefyingBead.Spirit getSpirit(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return HeavenDefyingBead.Spirit.NONE;
        int ordinal = stack.getTag().getInt(NBT_SPIRIT);
        HeavenDefyingBead.Spirit[] values = HeavenDefyingBead.Spirit.values();
        if (ordinal < 0 || ordinal >= values.length) return HeavenDefyingBead.Spirit.NONE;
        return values[ordinal];
    }

    /** Set the bound spirit. */
    public void setSpirit(ItemStack stack, HeavenDefyingBead.Spirit spirit) {
        stack.getOrCreateTag().putInt(NBT_SPIRIT, spirit.ordinal());
    }

    /** Whether Li Muwan's Nascent Soul is stored in this bead. */
    public boolean hasLiMuwanSoul(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        return stack.getTag().getBoolean(NBT_LI_MUWAN_SOUL);
    }

    /** Store Li Muwan's Nascent Soul. Canon: after her body perished. */
    public void setLiMuwanSoul(ItemStack stack, boolean present) {
        stack.getOrCreateTag().putBoolean(NBT_LI_MUWAN_SOUL, present);
        if (present) {
            Ergenverse.LOGGER.info("[Ergenverse] Heaven-Defying Bead: Li Muwan's "
                    + "Nascent Soul stored. The motivation is now absolute.");
        }
    }

    /** Get the Samsara incarnation count stored in the bead. */
    public int getSamsaraCount(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.max(0, stack.getTag().getInt(NBT_SAMARA_COUNT));
    }

    /** Set the Samsara incarnation count. */
    public void setSamsaraCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(NBT_SAMARA_COUNT, Math.max(0, count));
    }

    /** Get the last-opened function tab. */
    public int getActiveTab(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return stack.getTag().getInt(NBT_ACTIVE_TAB);
    }

    /** Set the last-opened function tab. */
    public void setActiveTab(ItemStack stack, int tabOrdinal) {
        stack.getOrCreateTag().putInt(NBT_ACTIVE_TAB, tabOrdinal);
    }

    /** Get current storage slot count. */
    public int getCurrentSlots(ItemStack stack) {
        double stability = getSpatialStability(stack) / 10000.0;
        double restoration = getPartsAligned(stack) / 9.0;
        double authority = getOwnerAuthority(stack) / 10000.0;
        double interior = getInteriorGrowth(stack) / 10000.0;
        return BeadCapacityModel.storageSlots(stability, restoration, authority, interior);
    }

    /**
     * Recalculate the interior stage from the four factors and update NBT.
     * Called whenever any factor changes.
     */
    private void recalculateStage(ItemStack stack) {
        double stability = getSpatialStability(stack) / 10000.0;
        double restoration = getPartsAligned(stack) / 9.0;
        double authority = getOwnerAuthority(stack) / 10000.0;
        double interior = getInteriorGrowth(stack) / 10000.0;
        BeadInteriorStage newStage = BeadCapacityModel.stageFor(
                stability, restoration, authority, interior);
        setStage(stack, newStage);
    }

    /**
     * Initialize a fresh bead stack to CRACK_OPENED stage with Situ Nan.
     * Called when the player first obtains the bead (e.g., via command or
     * the Heng Yue Sect discovery event).
     *
     * <p>Canon: the bead starts as a stone (DORMANT_STONE). After Situ Nan
     * blasts it open, it reveals a small interior chamber (CRACK_OPENED).
     * This method simulates that first opening.
     */
    public static ItemStack createInitialBead() {
        // Create via the registered item — will be populated when
        // WangLinItems registers the bead item.
        // For now, return null and let the registration handle it.
        return ItemStack.EMPTY;
    }

    /**
     * Apply the initial opening event to an existing bead stack.
     * Transitions from DORMANT_STONE to CRACK_OPENED with Situ Nan present.
     *
     * <p>Canon: Ch. 8 — Wang Lin finds the bead as a stone. Situ Nan's
     * remnant soul is inside. The bead cracks open, revealing a small chamber.
     */
    public static void applyInitialOpening(ItemStack stack) {
        if (!(stack.getItem() instanceof HeavenDefyingBeadItem beadItem)) return;

        beadItem.setPartsAligned(stack, 1);  // CORE only
        beadItem.setSpatialStability(stack, 1000);  // unstable but present
        beadItem.setOwnerAuthority(stack, 500);  // faint recognition
        beadItem.setInteriorGrowth(stack, 0);
        beadItem.setSpirit(stack, HeavenDefyingBead.Spirit.SITU_NAN);
        // recalculateStage is called inside setPartsAligned
    }
}