package dev.ergenverse.wanglin.bead;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.HeavenDefyingBead;
import dev.ergenverse.wanglin.WangLinItem;
import dev.ergenverse.wanglin.WangLinItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    /**
     * CRON-COMPLETIONIST-100: The revival-attempt counter.
     *
     * <p>Canon basis (fact-checked via web-search 2026-07-26): Wang Lin
     * attempts to revive Li Muwan <b>137 times</b> across millennia — all
     * fail. The 137th attempt is the final failed attempt, depicted in
     * the novel as: "血色残阳笼罩着朱雀墓，王林怀中抱着生机尽散的李慕婉，
     * 她白发如雪的身体正在化作星芒消散，这是他第137次尝试复活失败"
     * (blood-red sun over the Vermilion Bird Tomb, Wang Lin cradles the
     * lifeless Li Muwan, her white-haired body dissolving into starlight —
     * this is his 137th failed revival attempt). Final success requires
     * the Fourth Step (第四步) + 一界本源 (origin of a world).
     *
     * <p>This counter tracks the player's revival attempts. Each attempt
     * is gated by:
     * <ul>
     *   <li>{@code hasLiMuwanSoul(stack)} must be true (CRON-99 event fired)</li>
     *   <li>Bead stage must have special functions (SMALL_WORLD+)</li>
     *   <li>Player realm must be at least SOUL_FORMATION (化神) — canon:
     *       Wang Lin attempts revival only after reaching high realms</li>
     *   <li>Cooldown of {@code REVIVAL_COOLDOWN_TICKS} between attempts</li>
     * </ul>
     *
     * <p>At 137 attempts, a special "final attempt" event fires. Actual
     * success requires Fourth Step realm (TRANSCENDENCE) AND a World Origin
     * Essence (一界本源) item in the player's inventory — separate gates
     * enforced by {@link RevivalAttemptService#attemptRevival}. The World
     * Origin Essence is consumed on success (CRON-COMPLETIONIST-101) —
     * canon: the world whose origin is extracted is irreversibly sacrificed.
     *
     * <p>NO fabricated chapter citation. The 137 number is canon-attested
     * via web-search; the exact chapter is not cited to avoid fabrication.
     */
    public static final String NBT_REVIVAL_ATTEMPTS = "Ergen.Bead.RevivalAttempts";

    /**
     * CRON-COMPLETIONIST-100: The game tick of the last revival attempt.
     * Used to enforce the cooldown between attempts. Stored as a long
     * because game time is a long.
     */
    public static final String NBT_LAST_REVIVAL_TICK = "Ergen.Bead.LastRevivalTick";

    /** Canon-attested maximum number of failed revival attempts. */
    public static final int CANON_REVIVAL_ATTEMPT_CAP = 137;

    /**
     * CRON-COMPLETIONIST-102: Flag indicating Li Muwan has been successfully
     * revived. Set to {@code true} by {@link RevivalAttemptService#doSuccessfulRevival}
     * after the World Origin Essence is consumed and Li Muwan spawns as a
     * living NPC at the player's position.
     *
     * <p>Canon basis (web-search verified 2026-07-26): "王林踏入第四步后，
     * 成功运用一界本源将之复活，此后，两人踏天同行，超越生死轮回"
     * — after Wang Lin enters the Fourth Step and uses 一界本源 to revive
     * her, they transcend together. The revived flag marks this permanent
     * state transition: Li Muwan is no longer a trapped soul — she lives
     * again as Wang Lin's companion.
     *
     * <p>Once set to {@code true}, this flag is NEVER reset. The revival
     * is irreversible — canon: the world whose origin was extracted is
     * gone forever, and Li Muwan lives beyond the cycle of life and death.
     *
     * <p>NO fabricated chapter citation. The revival is canon-attested;
     * the exact chapter is NOT cited to avoid fabrication.
     */
    public static final String NBT_LI_MUWAN_REVIVED = "Ergen.Bead.LiMuwanRevived";

    /**
     * CRON-COMPLETIONIST-106: NBT key tracking whether the player has
     * become the 15th-generation Suzaku Son (朱雀子) by inheriting the
     * Cultivation Planet Crystal (修炼星晶) at the Suzaku Tomb.
     *
     * <p>Canon: Wang Lin inherits the Cultivation Planet Crystal at the
     * Suzaku Tomb, becoming the 15th-generation Suzaku Son of Planet
     * Suzaku. The bead is the medium through which the inheritance's
     * spiritual Qi is absorbed — without the bead, the inheritance's
     * Qi would annihilate an unprepared cultivator.
     *
     * <p>Write-once: once set to {@code true}, never reset. The inheritance
     * is a one-time event per save (canon: Wang Lin inherits ONCE).
     *
     * <p>Set by {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}
     * when the inheritance event triggers. Read by future code that grants
     * Suzaku Son privileges (e.g., access to sealed chambers, recognition
     * by 拓森 when registered as a canon NPC).
     */
    public static final String NBT_SUZAKU_SON = "Ergen.Bead.SuzakuSon";

    /**
     * CRON-COMPLETIONIST-95: A bitfield tracking WHICH of the 9 Parts have
     * been aligned. Bit i corresponds to {@link HeavenDefyingBead.Part}
     * ordinal i (so bit 0 = CORE, bit 1 = METAL, ..., bit 8 = DEEP_MYSTERY_3).
     * Stored as a single int. Bits beyond 8 are reserved.
     *
     * <p>This is required to prevent a player from absorbing the same
     * essence twice (which would corrupt the parts-aligned count and
     * break the BeadCapacityModel's stage calculation). Without this
     * bitfield, the only state was a counter (0..9), and we couldn't
     * distinguish "2 parts aligned, both metal" from "2 parts aligned,
     * metal + wood".
     */
    public static final String NBT_ALIGNED_PARTS = "Ergen.Bead.AlignedParts";

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
     * Right-click interaction — dispatches to one of three behaviors based
     * on context:
     *
     * <ol>
     *   <li><b>If the off-hand holds an Element or Dao essence</b> →
     *       absorb the essence into the bead (CRON-COMPLETIONIST-95).
     *       This is the active side of bead progression — the player
     *       aligns the 9 Parts by feeding essences to the bead. Consumes
     *       one essence item per right-click.</li>
     *   <li><b>Else if shift is held and the stage allows physical entry</b> →
     *       teleport into the bead's interior dimension (existing behavior).</li>
     *   <li><b>Else if the stage allows storage access</b> →
     *       open the divine-sense storage menu (existing behavior).</li>
     * </ol>
     *
     * <p>The essence-absorption branch takes priority over menu open so
     * the player can absorb without accidental menu popups. To open the
     * menu, the player must empty their off-hand first.
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

        // CRON-COMPLETIONIST-95: Essence absorption (off-hand priority).
        // If the off-hand holds an Element or Dao essence, absorb it into
        // the bead. This takes priority over menu open and dimension entry.
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!offHand.isEmpty()) {
            EssenceType essence = identifyEssence(offHand);
            if (essence != null) {
                return tryAbsorbEssence(level, player, hand, stack, offHand, essence);
            }
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

    // ── Essence Absorption (CRON-COMPLETIONIST-95) ──────────────────────

    /**
     * Identifies which {@link HeavenDefyingBead.Part} a given off-hand
     * stack would align. Returns {@code null} if the stack is not an
     * essence used for bead alignment.
     *
     * <p><b>Canon mapping (mod-original — see HeavenDefyingBead.Part
     * javadoc for canon vs. mod-original distinction):</b>
     * <ul>
     *   <li>{@code metal_essence} → Part METAL (canonical — Wang Lin aligns the Five Elements)</li>
     *   <li>{@code wood_essence}  → Part WOOD  (canonical)</li>
     *   <li>{@code water_essence} → Part WATER (canonical)</li>
     *   <li>{@code fire_essence}  → Part FIRE  (canonical)</li>
     *   <li>{@code earth_essence} → Part EARTH (canonical)</li>
     *   <li>{@code dao_fragment}  → Part DEEP_MYSTERY_1 (mod-original — the canon mentions 3 hidden fragments but does not enumerate which Dao items map to which; this is a reasonable mod choice as dao_fragment is the most generic Dao piece)</li>
     *   <li>{@code dao_karma}     → Part DEEP_MYSTERY_2 (mod-original — karmic resonance, matches the "reincarnation imprint" bridging-policy name)</li>
     *   <li>{@code dao_life_death}→ Part DEEP_MYSTERY_3 (mod-original — life/death is the most transcendent Dao, matches the "Heaven-Trampling bridge resonance" bridging-policy name)</li>
     * </ul>
     *
     * <p>The remaining Dao items ({@code dao_slaughter}, {@code dao_time})
     * are NOT used for bead alignment — they are separate Dao items in
     * Wang Lin's arsenal but do not correspond to a bead Part. This is
     * a deliberate mod-design choice; canon does not specify how the 3
     * hidden fragments align, so we picked the three most thematically
     * resonant Dao items.
     */
    @Nullable
    private EssenceType identifyEssence(ItemStack offHand) {
        String itemId = ForgeRegistries.ITEMS.getKey(offHand.getItem()).toString();
        return switch (itemId) {
            case "ergenverse:metal_essence"     -> EssenceType.of(HeavenDefyingBead.Part.METAL,
                    "Metal essence", "金源精魄 merges with the bead. The Metal pattern flares.");
            case "ergenverse:wood_essence"      -> EssenceType.of(HeavenDefyingBead.Part.WOOD,
                    "Wood essence",  "木源精魄 merges with the bead. The Wood pattern pulses.");
            case "ergenverse:water_essence"     -> EssenceType.of(HeavenDefyingBead.Part.WATER,
                    "Water essence", "水源精魄 merges with the bead. The Water pattern ripples.");
            case "ergenverse:fire_essence"      -> EssenceType.of(HeavenDefyingBead.Part.FIRE,
                    "Fire essence",  "火源精魄 merges with the bead. The Fire pattern blazes.");
            case "ergenverse:earth_essence"     -> EssenceType.of(HeavenDefyingBead.Part.EARTH,
                    "Earth essence", "土源精魄 merges with the bead. The Earth pattern stabilizes.");
            case "ergenverse:dao_fragment"      -> EssenceType.of(HeavenDefyingBead.Part.DEEP_MYSTERY_1,
                    "Dao Fragment",  "A fragment of the Dao sinks into the bead. A hidden resonance awakens.");
            case "ergenverse:dao_karma"         -> EssenceType.of(HeavenDefyingBead.Part.DEEP_MYSTERY_2,
                    "Dao of Karma",  "Karmic Dao sinks into the bead. The reincarnation imprint stirs.");
            case "ergenverse:dao_life_death"    -> EssenceType.of(HeavenDefyingBead.Part.DEEP_MYSTERY_3,
                    "Dao of Life and Death",  "The Dao of Life and Death sinks into the bead. The Heaven-Trampling bridge resonance awakens.");
            default -> null;
        };
    }

    /**
     * Attempt to absorb one essence from the off-hand into the bead.
     *
     * <p>Behavior:
     * <ol>
     *   <li>If the corresponding Part is already aligned → fail with a
     *       chat message ("This essence has already been absorbed.").
     *       The essence is NOT consumed.</li>
     *   <li>Otherwise: align the Part (sets the bit + increments the
     *       counter), consumes one essence from the off-hand, spawns
     *       celebration particles, and sends a canon-flavored chat
     *       message describing the absorption.</li>
     * </ol>
     */
    private InteractionResultHolder<ItemStack> tryAbsorbEssence(
            net.minecraft.world.level.Level level, Player player,
            InteractionHand hand, ItemStack beadStack, ItemStack offHand,
            EssenceType essence) {

        int partBitIndex = essence.part.ordinal();

        // 1. Check if the Part is already aligned.
        if (isPartAligned(beadStack, partBitIndex)) {
            player.sendSystemMessage(
                    Component.literal("The bead rejects the " + essence.displayName
                            + " — that pattern is already aligned.")
                            .withStyle(ChatFormatting.YELLOW));
            return InteractionResultHolder.fail(beadStack);
        }

        // 2. Canon ordering check: the Five Elements must be aligned
        //    in order (Metal → Wood → Water → Fire → Earth). The 3 hidden
        //    fragments can only be aligned after all 5 Elements are done.
        //    This is a soft canon-faithful gate — the novel depicts Wang
        //    Lin aligning the Five Elements roughly in this order, and the
        //    3 hidden fragments are late-game.
        HeavenDefyingBead.Part[] order = HeavenDefyingBead.Part.values();
        for (int i = 0; i < partBitIndex; i++) {
            if (!isPartAligned(beadStack, i)) {
                HeavenDefyingBead.Part prerequisite = order[i];
                player.sendSystemMessage(
                        Component.literal("The bead cannot yet accept the "
                                + essence.displayName + ". The "
                                + prerequisite.name
                                + " must be aligned first.")
                                .withStyle(ChatFormatting.RED));
                return InteractionResultHolder.fail(beadStack);
            }
        }

        // 3. Consume one essence from the off-hand.
        if (!player.getAbilities().instabuild) {
            offHand.shrink(1);
        }

        // 4. Align the Part (sets the bit + increments the counter).
        alignPart(beadStack, partBitIndex);

        // 5. Server-side feedback: chat message + particles.
        if (player instanceof ServerPlayer serverPlayer) {
            int newCount = getPartsAligned(beadStack);
            serverPlayer.sendSystemMessage(
                    Component.literal(essence.flavorText)
                            .withStyle(ChatFormatting.GOLD));
            serverPlayer.sendSystemMessage(
                    Component.literal("  Parts Aligned: " + newCount + " / 9")
                            .withStyle(ChatFormatting.AQUA));

            // Spawn celebration particles around the player.
            spawnAbsorptionParticles(serverPlayer);

            Ergenverse.LOGGER.info("[Ergenverse] Player {} absorbed {} into the bead "
                            + "(parts aligned: {}/9, part={}).",
                    serverPlayer.getName().getString(), essence.displayName,
                    newCount, essence.part.name());
        }

        return InteractionResultHolder.success(beadStack);
    }

    /**
     * Spawn gold and dust particles around the player to celebrate the
     * absorption. Visible at any light level.
     */
    private void spawnAbsorptionParticles(ServerPlayer player) {
        if (!(player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;
        double x = player.getX();
        double y = player.getY() + 1.0;  // chest height
        double z = player.getZ();
        // 20 gold particles in a sphere
        for (int i = 0; i < 20; i++) {
            double theta = (i / 20.0) * Math.PI * 2;
            double phi = ((i * 13) % 7) / 7.0 * Math.PI;  // pseudo-random sphere
            double r = 1.2;
            double dx = Math.sin(phi) * Math.cos(theta) * r;
            double dy = Math.cos(phi) * r;
            double dz = Math.sin(phi) * Math.sin(theta) * r;
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    x + dx, y + dy, z + dz, 1,
                    0.0, 0.0, 0.0, 0.0);
        }
        // Central flash
        serverLevel.sendParticles(ParticleTypes.FIREWORK,
                x, y, z, 5, 0.5, 0.5, 0.5, 0.05);
    }

    /**
     * Returns true if the Part at {@code partIndex} (0..8) is aligned.
     */
    public boolean isPartAligned(ItemStack stack, int partIndex) {
        if (partIndex < 0 || partIndex >= 9) return false;
        if (stack.isEmpty() || !stack.hasTag()) return false;
        int bits = stack.getTag().getInt(NBT_ALIGNED_PARTS);
        return (bits & (1 << partIndex)) != 0;
    }

    /**
     * Align the Part at {@code partIndex} (0..8). Sets the bit and
     * increments the parts-aligned counter. Does NOT recompute the stage
     * (the counter setter does that).
     */
    public void alignPart(ItemStack stack, int partIndex) {
        if (partIndex < 0 || partIndex >= 9) return;
        int bits = stack.getOrCreateTag().getInt(NBT_ALIGNED_PARTS);
        if ((bits & (1 << partIndex)) != 0) return;  // already aligned
        bits |= (1 << partIndex);
        stack.getOrCreateTag().putInt(NBT_ALIGNED_PARTS, bits);
        // Increment the parts-aligned counter (which calls recalculateStage).
        setPartsAligned(stack, getPartsAligned(stack) + 1);
    }

    /**
     * Returns the raw aligned-parts bitfield. Bit i corresponds to Part
     * ordinal i. Useful for debugging or save-state inspection.
     */
    public int getAlignedPartsBits(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return stack.getTag().getInt(NBT_ALIGNED_PARTS);
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

            // CRON-COMPLETIONIST-100: Revival attempt counter.
            // Only shown when Li Muwan's soul is present (the counter is
            // meaningless without it). Displays "Revival Attempts: X / 137"
            // in GOLD for visibility — this is Wang Lin's central quest.
            int attempts = getRevivalAttempts(stack);
            tooltip.add(Component.literal("Revival Attempts: ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(attempts + " / " + CANON_REVIVAL_ATTEMPT_CAP)
                            .withStyle(ChatFormatting.GOLD)));

            // CRON-COMPLETIONIST-102: Revived flag.
            // Only shown when Li Muwan has been successfully revived — the
            // endgame state. Displays "Li Muwan: REVIVED" in GOLD+BOLD.
            if (isLiMuwanRevived(stack)) {
                tooltip.add(Component.literal("李慕婉：已复活  ·  ")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal("Li Muwan: REVIVED")
                                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
                tooltip.add(Component.literal("  两人踏天同行，超越生死轮回")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
            }
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

    // ── CRON-COMPLETIONIST-100: Revival Attempt Accessors ──────────────

    /**
     * Get the number of failed revival attempts (0..137).
     *
     * <p>Canon: Wang Lin attempts revival 137 times, all fail. The counter
     * caps at {@link #CANON_REVIVAL_ATTEMPT_CAP} (137) — additional attempts
     * beyond 137 are rejected by {@link RevivalAttemptService}.
     *
     * @param stack the bead stack
     * @return the attempt count, clamped to [0, 137]
     */
    public int getRevivalAttempts(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0;
        return Math.min(CANON_REVIVAL_ATTEMPT_CAP,
                Math.max(0, stack.getTag().getInt(NBT_REVIVAL_ATTEMPTS)));
    }

    /**
     * Set the revival attempt count. Clamped to [0, 137].
     *
     * <p>Does NOT call {@code recalculateStage} — the attempt count does
     * not affect the bead's interior stage (it's a quest tracker, not a
     * progression factor).
     *
     * @param stack the bead stack
     * @param count the new attempt count
     */
    public void setRevivalAttempts(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt(NBT_REVIVAL_ATTEMPTS,
                Math.min(CANON_REVIVAL_ATTEMPT_CAP, Math.max(0, count)));
    }

    /**
     * Get the game tick of the last revival attempt.
     *
     * <p>Used by {@link RevivalAttemptService} to enforce the cooldown.
     * Returns 0 if no attempt has been made (treated as "cooldown expired"
     * by the service).
     *
     * @param stack the bead stack
     * @return the game tick, or 0 if never attempted
     */
    public long getLastRevivalAttemptTick(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return 0L;
        return stack.getTag().getLong(NBT_LAST_REVIVAL_TICK);
    }

    /**
     * Set the game tick of the last revival attempt.
     *
     * @param stack the bead stack
     * @param tick  the game tick
     */
    public void setLastRevivalAttemptTick(ItemStack stack, long tick) {
        stack.getOrCreateTag().putLong(NBT_LAST_REVIVAL_TICK, Math.max(0L, tick));
    }

    /**
     * CRON-COMPLETIONIST-102: Check whether Li Muwan has been successfully
     * revived. Returns {@code false} for beads that have never reached the
     * success path (the vast majority of beads).
     *
     * <p>Once {@code true}, this flag is permanent — the revival is
     * irreversible (canon: the sacrificed world is gone forever).
     *
     * @param stack the bead stack
     * @return {@code true} if Li Muwan has been revived via the success path
     */
    public boolean isLiMuwanRevived(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        return stack.getTag().getBoolean(NBT_LI_MUWAN_REVIVED);
    }

    /**
     * CRON-COMPLETIONIST-102: Mark Li Muwan as revived. Called ONLY by
     * {@link RevivalAttemptService#doSuccessfulRevival} after the World
     * Origin Essence is consumed and Li Muwan spawns as a living NPC.
     *
     * <p>This flag is write-once — once set to {@code true}, it is never
     * reset. Calling this with {@code false} is a no-op if the flag is
     * already {@code true} (defensive: prevents accidental un-revival).
     *
     * @param stack the bead stack
     * @param revived {@code true} to mark Li Muwan as revived
     */
    public void setLiMuwanRevived(ItemStack stack, boolean revived) {
        if (!revived) return;  // write-once: can't un-revive
        stack.getOrCreateTag().putBoolean(NBT_LI_MUWAN_REVIVED, true);
    }

    /**
     * CRON-COMPLETIONIST-106: Check whether the player has become the
     * 15th-generation Suzaku Son by inheriting the Cultivation Planet Crystal.
     *
     * @param stack the bead stack
     * @return {@code true} if the Suzaku Son inheritance has been triggered
     *         via {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}
     */
    public boolean isSuzakuSon(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        return stack.getTag().getBoolean(NBT_SUZAKU_SON);
    }

    /**
     * CRON-COMPLETIONIST-106: Mark the player as the 15th-generation Suzaku
     * Son. Called ONLY by {@link dev.ergenverse.block.CultivationPlanetCrystalBlock#use}
     * after the inheritance event triggers (prerequisites: bead in hand,
     * realm ≥ Nascent Soul, Crystal not yet inherited).
     *
     * <p>This flag is write-once — once set to {@code true}, it is never
     * reset. Calling this with {@code false} is a no-op if the flag is
     * already {@code true} (defensive: prevents accidental un-inheritance).
     *
     * @param stack the bead stack
     * @param suzakuSon {@code true} to mark as Suzaku Son
     */
    public void setSuzakuSon(ItemStack stack, boolean suzakuSon) {
        if (!suzakuSon) return;  // write-once: can't un-inherit
        stack.getOrCreateTag().putBoolean(NBT_SUZAKU_SON, true);
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
     *
     * <p><b>CRON-COMPLETIONIST-95 FIX:</b> Previously this returned
     * {@link ItemStack#EMPTY}, leaving no programmatic way to create an
     * initialized bead. Now resolves the bead item via {@link WangLinItems}
     * and applies the opening event. Returns {@link ItemStack#EMPTY} only
     * if the bead item is not yet registered (e.g., called before
     * Forge registry event fires).
     */
    public static ItemStack createInitialBead() {
        RegistryObject<Item> beadRO = WangLinItems.get("wanglin/heaven_defying_bead");
        if (beadRO == null || !beadRO.isPresent()) {
            Ergenverse.LOGGER.warn("[Ergenverse] HeavenDefyingBeadItem.createInitialBead: "
                    + "heaven_defying_bead not registered — returning EMPTY.");
            return ItemStack.EMPTY;
        }
        ItemStack stack = new ItemStack(beadRO.get());
        applyInitialOpening(stack);
        return stack;
    }

    /**
     * Apply the initial opening event to an existing bead stack.
     * Transitions from DORMANT_STONE to CRACK_OPENED with Situ Nan present.
     *
     * <p>Canon: Ch. 8 — Wang Lin finds the bead as a stone. Situ Nan's
     * remnant soul is inside. The bead cracks open, revealing a small chamber.
     *
     * <p><b>CRON-COMPLETIONIST-95:</b> Also sets the CORE bit in the
     * AlignedParts bitfield, so the player cannot accidentally absorb a
     * "core" essence (there is none — the core is the bead itself).
     */
    public static void applyInitialOpening(ItemStack stack) {
        if (!(stack.getItem() instanceof HeavenDefyingBeadItem beadItem)) return;

        // CRON-95: align the CORE part (bit 0) BEFORE setting the counter,
        // so the bitfield and counter stay in sync.
        int bits = stack.getOrCreateTag().getInt(NBT_ALIGNED_PARTS);
        bits |= (1 << HeavenDefyingBead.Part.CORE.ordinal());
        stack.getOrCreateTag().putInt(NBT_ALIGNED_PARTS, bits);

        beadItem.setPartsAligned(stack, 1);  // CORE only
        beadItem.setSpatialStability(stack, 1000);  // unstable but present
        beadItem.setOwnerAuthority(stack, 500);  // faint recognition
        beadItem.setInteriorGrowth(stack, 0);
        beadItem.setSpirit(stack, HeavenDefyingBead.Spirit.SITU_NAN);
        // recalculateStage is called inside setPartsAligned
    }

    // ── Essence Type Helper (CRON-COMPLETIONIST-95) ────────────────────

    /**
     * A resolved essence type — the Part it aligns plus display info.
     * Used by {@link #identifyEssence} to dispatch absorption.
     */
    private static final class EssenceType {
        final HeavenDefyingBead.Part part;
        final String displayName;
        final String flavorText;

        private EssenceType(HeavenDefyingBead.Part part, String displayName, String flavorText) {
            this.part = part;
            this.displayName = displayName;
            this.flavorText = flavorText;
        }

        static EssenceType of(HeavenDefyingBead.Part part, String displayName, String flavorText) {
            return new EssenceType(part, displayName, flavorText);
        }
    }
}