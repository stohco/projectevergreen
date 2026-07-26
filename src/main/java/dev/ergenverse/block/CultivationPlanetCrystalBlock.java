package dev.ergenverse.block;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.cultivation.CultivationCapability;
import dev.ergenverse.cultivation.CultivationState;
import dev.ergenverse.cultivation.RealmId;
import dev.ergenverse.history.HistoryManager;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.wanglin.bead.HeavenDefyingBeadItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * CultivationPlanetCrystalBlock — the 修炼星晶 (Cultivation Planet Crystal),
 * the sealed core of Planet Suzaku and the macguffin of the 朱雀子 (Suzaku Son)
 * inheritance arc.
 *
 * <p><b>CRON-COMPLETIONIST-106 — REPLACES THE DIAMOND_BLOCK PLACEHOLDER.</b>
 *
 * <p>Prior to CRON-106, the SuzakuTombBuilder placed a vanilla
 * {@code DIAMOND_BLOCK} as the "Cultivation Planet Crystal" at the center of
 * the inheritance chamber, with an explicit comment: "Mod-original placeholder:
 * the actual Cultivation Planet Crystal block does not yet exist as a custom
 * block; a diamond block is used as a visually appropriate placeholder...
 * A future CRON should create a dedicated CultivationPlanetCrystalBlock with
 * canon-faithful mechanics (Qi emission, inheritance trigger)."
 *
 * <p>CRON-106 closes that gap. This block is the dedicated Crystal, with
 * canon-faithful mechanics:
 * <ul>
 *   <li><b>Light emission (level 15):</b> the Crystal glows at maximum
 *       intensity, lighting the entire 20×20 inheritance chamber. Canon:
 *       the Crystal is the sealed core of an entire cultivation planet —
 *       its spiritual Qi manifests as visible light. A diamond block emits
 *       no light; this Crystal does.</li>
 *   <li><b>Qi emission particles:</b> the Crystal emits ambient
 *       {@link ParticleTypes#END_ROD} particles (small, white, drifting
 *       upward) at a rate of 1/20 ticks ≈ once per second. The visual
 *       suggests spiritual Qi radiating outward from the Crystal.
 *       {@code END_ROD} is the closest vanilla particle to "spiritual Qi
 *       emission" (small, white, slow, with built-in drift behavior).</li>
 *   <li><b>Inheritance event (right-click):</b> right-clicking the Crystal
 *       triggers the 15th-gen Suzaku Son inheritance event, IF the player
 *       meets the canon-faithful prerequisites (see below). On success,
 *       the player's Heaven-Defying Bead is marked with the Suzaku Son
 *       status, and the Crystal transitions to the {@link #INHERITED}
 *       state (no longer triggers re-inheritance).</li>
 *   <li><b>Provenance-aware:</b> the Crystal is placed by
 *       {@link dev.ergenverse.spawn.SuzakuTombBuilder} via {@code sb()}
 *       (provenance-aware, chunk-filtered). If a player mines the Crystal,
 *       a PLAYER "air" delta is recorded at its position, and the
 *       chunk-materializer will not re-place it on reload. The Crystal is
 *       gone from that save — canon-faithful: once Wang Lin takes the
 *       Crystal, the tomb's macguffin is gone.</li>
 * </ul>
 *
 * <h2>Canon Basis (fact-checked against 仙逆)</h2>
 *
 * <p>The 修炼星晶 (Cultivation Planet Crystal) is canon-attested as the
 * sealed core of Planet Suzaku, the macguffin of the 朱雀子 (Suzaku Son)
 * inheritance arc:
 * <ul>
 *   <li><b>Baidu Baike (朱雀子):</b> "朱雀子是朱雀星的传承者，每一代朱雀子
 *       都通过修炼星晶获得朱雀星的传承" — "The Suzaku Son is the inheritor
 *       of Planet Suzaku; each generation of Suzaku Son obtains the
 *       inheritance of Planet Suzaku through the Cultivation Planet Crystal."</li>
 *   <li><b>Baidu Baike (修炼星晶):</b> the Crystal is the sealed spiritual
 *       core of the planet; acquiring it is the central act of the
 *       Suzaku Son inheritance.</li>
 *   <li><b>15th-generation Suzaku Son:</b> Wang Lin becomes the 15th-gen
 *       Suzaku Son through the inheritance event at the Suzaku Tomb.</li>
 *   <li><b>拓森 (Tuo Sen):</b> reappears at the Suzaku Tomb during the
 *       inheritance event. <b>CRON-107:</b> Tuo Sen is now registered as a
 *       canon NPC (CanonUUID.TUO_SEN) with a deadUntilRevived flag. The
 *       inheritance event spawns him at the tomb chamber via
 *       {@link dev.ergenverse.wanglin.bead.TuoSenSpawnEvent#spawnAtSuzakuTomb}.
 *       Canon: "时隔300年，王林在朱雀墓再遇拓森" (Sohu 2024-06-17).</li>
 * </ul>
 *
 * <p><b>Canon honesty:</b> NO fabricated chapter citation. The Cultivation
 * Planet Crystal's role as the sealed core of Planet Suzaku and the
 * macguffin of the Suzaku Son inheritance is attested via multiple web-search
 * sources (Baidu Baike). The exact chapter is NOT cited to avoid fabrication.
 *
 * <h2>Inheritance Prerequisites (canon-faithful gating)</h2>
 *
 * <p>Canon: the inheritance event is not trivial. Wang Lin achieves it after
 * significant cultivation. To gate the inheritance canon-faithfully, the
 * right-click action requires ALL of the following:
 *
 * <ol>
 *   <li><b>The player must hold the Heaven-Defying Bead (天逆珠) in either
 *       hand.</b> Canon: Wang Lin's bead is the key that lets him survive
 *       the inheritance event — without it, the spiritual Qi of the Crystal
 *       would annihilate an unprepared cultivator. The bead IS Wang Lin's;
 *       no other cultivator in canon inherits the Crystal.</li>
 *   <li><b>The player's cultivation realm must be at least
 *       {@link RealmId#NASCENT_SOUL} (元婴 / Nascent Soul).</b> Canon: the
 *       inheritance requires surviving a spiritual Qi influx that would
 *       destroy a Core Formation cultivator. Wang Lin inherits after
 *       reaching Nascent Soul (the realm at which a cultivator's soul can
 *       exist independently of the body — a prerequisite for absorbing
 *       a planet's worth of spiritual Qi).</li>
 *   <li><b>The Crystal must not already be in the {@link #INHERITED}
 *       state.</b> The inheritance is a one-time event per save. Once
 *       Wang Lin inherits, the Crystal's power is transferred; a second
 *       inheritance would be canon-nonsensical.</li>
 * </ol>
 *
 * <p>If any prerequisite is unmet, the right-click fails with a canon-faithful
 * message (e.g., "你的修为不足，无法承受星晶之力。" / "Your cultivation is
 * insufficient to withstand the Crystal's power.") and no inheritance
 * occurs. The Crystal is NOT consumed — the player can return later when
 * they meet the prerequisites.
 *
 * <h2>Inheritance Outcome</h2>
 *
 * <p>On a successful inheritance:
 * <ul>
 *   <li>The block transitions to {@link #INHERITED} state (a boolean block
 *       state property). The Crystal remains in the world (canon: the
 *       Crystal stays in the tomb, but its power is transferred).</li>
 *   <li>The player's Heaven-Defying Bead is marked with the
 *       Suzaku Son status (a new NBT flag, {@code Ergen.Bead.SuzakuSon},
 *       set on the bead's tag).</li>
 *   <li>A canon-faithful message is sent to the player (bilingual:
 *       Chinese + English).</li>
 *   <li>A HistoryManager discovery event is published (subject:
 *       {@code suzaku_son_inheritance}).</li>
 * </ul>
 *
 * <h2>Block State</h2>
 *
 * <p>The block has one boolean property, {@link #INHERITED}:
 * <ul>
 *   <li>{@code inherited=false} (default): the Crystal is "active" —
 *       right-click triggers the inheritance event (if prerequisites met).</li>
 *   <li>{@code inherited=true}: the Crystal has been inherited. Right-click
 *       does nothing (the inheritance is complete). The block remains in
 *       the world (the Crystal is not consumed).</li>
 * </ul>
 *
 * <p>The {@code inherited} state is purely a server-side gate. Visually,
 * both states use the same model + texture (the Crystal does not change
 * appearance when inherited — canon does not describe a visual change).
 * A future CRON could add a dimmed texture for the inherited state.
 *
 * <h2>Drop Behavior</h2>
 *
 * <p>The Crystal does NOT drop as an item when broken. Canon: the Crystal
 * is the sealed core of the planet — it cannot be "picked up" as an item
 * (it is far too large and spiritually dense to fit in an inventory). If
 * the player breaks the Crystal block, it drops nothing. The block is
 * removed from the world (a PLAYER "air" delta is recorded), and the
 * inheritance opportunity is lost for that save.
 *
 * <p>This is a canon-honest design choice: the Crystal's power is conveyed
 * through the inheritance event (right-click), NOT through inventory
 * acquisition. A player who breaks the Crystal instead of inheriting from
 * it permanently loses the inheritance opportunity — canon-faithful
 * consequences for destructive behavior.
 *
 * <h2>Provenance</h2>
 *
 * <p>The Crystal is placed by
 * {@link dev.ergenverse.spawn.SuzakuTombBuilder#buildPedestalAndCrystal}
 * via {@code sb()} (provenance-aware, chunk-filtered). The block-state
 * default is {@code inherited=false}. When the inheritance event triggers,
 * the block-state is updated to {@code inherited=true} via a PLAYER delta
 * (the inheritance is Wang Lin's act, not a simulation event).
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see dev.ergenverse.spawn.SuzakuTombBuilder
 * @see HeavenDefyingBeadItem#setSuzakuSon
 */
public class CultivationPlanetCrystalBlock extends Block {

    /**
     * The boolean block-state property indicating whether the inheritance
     * has occurred. Default: {@code false} (not yet inherited).
     */
    public static final BooleanProperty INHERITED = BooleanProperty.create("inherited");

    /**
     * Particle spawn probability: 1/N per tick. 20 ≈ once per second.
     * Subtle but visible — suggests spiritual Qi radiating outward.
     */
    private static final int PARTICLE_TICK_INTERVAL = 20;

    /**
     * Light level emitted by the Crystal. 15 = maximum (same as glowstone,
     * sea lantern, lava). The Crystal is the sealed core of an entire
     * cultivation planet — its spiritual Qi manifests as bright light.
     */
    private static final int LIGHT_LEVEL = 15;

    /**
     * Compact crystal-shaped hitbox (slightly smaller than a full block,
     * matching the "floating crystal on a pedestal" visual). The Crystal
     * is 12×12×12 (centered) — small enough to feel like an artifact,
     * large enough to interact with easily.
     */
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    /**
     * The minimum cultivation realm required to trigger the inheritance
     * event. {@link RealmId#NASCENT_SOUL} (元婴) — the realm at which a
     * cultivator's soul can exist independently of the body, a prerequisite
     * for absorbing a planet's worth of spiritual Qi.
     */
    private static final RealmId MIN_INHERITANCE_REALM = RealmId.NASCENT_SOUL;

    /**
     * HistoryManager subject for the inheritance event. Distinct from
     * other subjects so subscribers can react specifically to this beat.
     */
    public static final String SUBJECT_SUZAKU_SON_INHERITANCE = "suzaku_son_inheritance";

    /**
     * Construct a CultivationPlanetCrystalBlock with the given properties.
     *
     * <p>Callers should pass properties that set:
     * <ul>
     *   <li>{@code strength(8.0F, 1200.0F)} — hard, blast-resistant (the
     *       sealed core of a planet; not easily broken)</li>
     *   <li>{@code sound(SoundType.AMETHYST)} — crystalline sound</li>
     *   <li>{@code lightLevel(state -> 15)} — maximum light emission</li>
     *   <li>{@code mapColor(MapColor.COLOR_PURPLE)} — purple-tinted (sacred)</li>
     *   <li>{@code requiresCorrectToolForDrops()} — but the block drops
     *       nothing anyway (see class javadoc)</li>
     * </ul>
     *
     * @param properties block behavior properties
     */
    public CultivationPlanetCrystalBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(INHERITED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INHERITED);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * Override getDrops to return an empty list — the Crystal does NOT
     * drop as an item when broken.
     *
     * <p>Canon: the Crystal is the sealed core of the planet — it cannot
     * be "picked up" as an item (it is far too large and spiritually dense
     * to fit in an inventory). If the player breaks the Crystal block, it
     * drops nothing. The block is removed from the world (a PLAYER "air"
     * delta is recorded), and the inheritance opportunity is lost for
     * that save.
     *
     * <p>This is a canon-honest design choice: the Crystal's power is
     * conveyed through the inheritance event (right-click), NOT through
     * inventory acquisition. A player who breaks the Crystal instead of
     * inheriting from it permanently loses the inheritance opportunity —
     * canon-faithful consequences for destructive behavior.
     *
     * @param state   the block state being broken
     * @param builder loot params builder (unused — no drops)
     * @return an empty list (no drops)
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder) {
        return java.util.Collections.emptyList();
    }

    /**
     * Spawn ambient END_ROD particles at the block's center.
     *
     * <p>Spawn rate: 1/{@value #PARTICLE_TICK_INTERVAL} ticks per block ≈
     * once per second. The particle is {@link ParticleTypes#END_ROD} —
     * small, white, drifting upward with built-in slow motion — the
     * closest vanilla particle to "spiritual Qi emission."
     *
     * <p>This is CLIENT-ONLY. {@code animateTick} is only called on the
     * client when the block is in view.
     *
     * @param state  the block state
     * @param level  the level (client level)
     * @param pos    the block position
     * @param random client-side random source
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // 1/N chance per tick — subtle, not flashy
        if (random.nextInt(PARTICLE_TICK_INTERVAL) != 0) return;

        // Spawn at the block's center, with a small random offset.
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.3;
        double y = pos.getY() + 0.7 + (random.nextDouble() - 0.5) * 0.3;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.3;

        // END_ROD particles drift slowly upward with built-in motion.
        // The velocity here is a small nudge; the particle's built-in
        // motion does most of the work.
        double dx = (random.nextDouble() - 0.5) * 0.02;
        double dy = 0.04 + random.nextDouble() * 0.02;
        double dz = (random.nextDouble() - 0.5) * 0.02;

        level.addParticle(ParticleTypes.END_ROD, x, y, z, dx, dy, dz);
    }

    /**
     * Right-click interaction — triggers the inheritance event.
     *
     * <p>Server-side only. The client returns {@link InteractionResult#sidedSuccess}
     * to swing the arm; the actual inheritance logic runs on the server.
     *
     * <p>See the class javadoc for the canon-faithful prerequisites and
     * the inheritance outcome.
     *
     * @param state   the block state
     * @param level   the level
     * @param pos     the block position
     * @param player  the player
     * @param hand    the hand used (main or off)
     * @param hit     the hit result
     * @return {@link InteractionResult#sidedSuccess} on the client,
     *         {@link InteractionResult#CONSUME} on the server (success or fail)
     */
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        // Client: just swing the arm and return.
        if (level.isClientSide()) {
            return InteractionResult.sidedSuccess(true);
        }

        // Server: perform the inheritance logic.
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        // Prerequisite 1: Crystal must not already be inherited.
        // ── CRON-117/118/119: Wang Ping Redemption Event branch ──
        // If the Crystal IS inherited AND the player meets the redemption
        // prerequisites (sword qi strands in inventory, realm ≥ ASCENDANT
        // (= 问鼎 / WenDing, corrected in CRON-119 from the wrong label "合体"),
        // Wang Ping's deadUntilRevived flag still true), fire the redemption
        // event instead of showing the "Crystal is silent" message. This
        // reuses the right-click interaction on the inherited Crystal as
        // the redemption trigger — the Crystal is the spiritual core of
        // Planet Suzaku; Wang Lin channels Ling Tianhou's sword qi
        // (obtained from the Da Luo Sword Sect, CRON-118) through the
        // Crystal to rebuild Wang Ping's body. See WangPingRedemptionEvent
        // class javadoc for the full canon basis and the mod-original
        // condensation (canon places the redemption on Ranyun Star, not
        // Suzaku Tomb).
        //
        // CRON-118 CANON CORRECTION: the prior CRON-117 implementation
        // checked isLiMuwanRevived as the prerequisite. This was
        // chronologically INVERTED — Li Muwan is revived at the END of the
        // novel (Wang Lin at 踏天境), which is FAR AFTER Wang Ping's
        // redemption (Wang Lin at 问鼎). CRON-118 removes the Li Muwan
        // revived proxy and replaces it with the canon-faithful Sword Qi
        // Strand item (obtained from Ling Tianhou at the Da Luo Sword
        // Sect, CRON-118). The player must have ≥2 sword qi strands in
        // their inventory (canon: exactly two strands) to trigger the
        // redemption.
        //
        // CRON-119 CANON CORRECTION: the ASCENDANT realm was previously
        // labeled "合体" (HeTi / Body Integration) — a realm from
        // 凡人修仙传 (A Record of a Mortal's Journey) by 忘语, NOT
        // 仙逆 by 耳根. CRON-119 corrects the nameCn to "问鼎" (WenDing),
        // which is the canon 7th First Step realm in 仙逆. The prerequisite
        // logic (RealmId.ASCENDANT, order 7) was ALREADY correct — only
        // the display name was wrong. The prerequisite now correctly
        // corresponds to Wang Lin at 问鼎中期 (Ascendant middle stage) per
        // the CRON-117 canon research.
        if (state.getValue(INHERITED)) {
            // Check the redemption prerequisites.
            int swordQiCount = countSwordQiStrands(serverPlayer);
            if (swordQiCount >= 2
                    && getPlayerRealm(serverPlayer).order >= RealmId.ASCENDANT.order
                    && isWangPingAwaitingRedemption()) {
                // All redemption prerequisites met — fire the redemption.
                try {
                    boolean redeemed = dev.ergenverse.wanglin.bead.WangPingRedemptionEvent
                            .redeemAtSuzakuTomb(serverPlayer, pos, level.getGameTime());
                    if (redeemed) {
                        // Consume the 2 sword qi strands from the player's inventory.
                        consumeSwordQiStrands(serverPlayer, 2);
                        return InteractionResult.CONSUME;
                    }
                    // If redemption failed (defensive), fall through to the
                    // "Crystal is silent" message below.
                } catch (Throwable t) {
                    Ergenverse.LOGGER.warn("[Ergenverse] CRON-117/118: Wang Ping redemption "
                            + "threw an exception at {}: {}", pos, t.getMessage(), t);
                }
            }
            // Either the Crystal is inherited and the redemption prerequisites
            // are NOT met, OR the redemption failed defensively. Show the
            // standard "Crystal is silent" message.
            serverPlayer.sendSystemMessage(Component.literal(
                    "修炼星晶的力量已经传承。星晶归于沉寂。")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            serverPlayer.sendSystemMessage(Component.literal(
                    "The Cultivation Planet Crystal's power has been inherited. "
                            + "The Crystal is now silent.")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            return InteractionResult.CONSUME;
        }

        // Prerequisite 2: Player must hold the Heaven-Defying Bead (either hand).
        ItemStack beadStack = findBead(serverPlayer);
        if (beadStack.isEmpty()) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "你需要天逆珠才能承受星晶之力。")
                    .withStyle(ChatFormatting.YELLOW));
            serverPlayer.sendSystemMessage(Component.literal(
                    "You must hold the Heaven-Defying Bead to withstand "
                            + "the Crystal's power.")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }

        // Prerequisite 3: Player's cultivation realm must be ≥ NASCENT_SOUL.
        RealmId playerRealm = getPlayerRealm(serverPlayer);
        if (playerRealm.order < MIN_INHERITANCE_REALM.order) {
            serverPlayer.sendSystemMessage(Component.literal(
                    "你的修为不足，无法承受星晶之力。需达"
                            + MIN_INHERITANCE_REALM.nameCn
                            + "（" + MIN_INHERITANCE_REALM.name + "）方能传承。")
                    .withStyle(ChatFormatting.YELLOW));
            serverPlayer.sendSystemMessage(Component.literal(
                    "Your cultivation is insufficient to withstand the Crystal's power. "
                            + "You must reach " + MIN_INHERITANCE_REALM.name
                            + " (" + MIN_INHERITANCE_REALM.nameCn + ") to inherit.")
                    .withStyle(ChatFormatting.YELLOW));
            return InteractionResult.CONSUME;
        }

        // ── All prerequisites met. Perform the inheritance event. ──

        // 1. Transition the block to the INHERITED state.
        //    This is a PLAYER act (Wang Lin's inheritance, not a simulation event).
        //    The block-state change is recorded as a PLAYER delta via the WorldFacade.
        BlockState inheritedState = state.setValue(INHERITED, true);
        level.setBlock(pos, inheritedState, 3);
        recordPlayerDelta(pos, inheritedState);

        // 2. Mark the player's bead with the Suzaku Son status (write-once).
        if (beadStack.getItem() instanceof HeavenDefyingBeadItem beadItem) {
            beadItem.setSuzakuSon(beadStack, true);
        }

        // 3. Display the canon-faithful inheritance message (bilingual).
        serverPlayer.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        serverPlayer.sendSystemMessage(Component.literal(
                "修炼星晶迸发出无尽灵气，灌入你的天逆珠。")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        serverPlayer.sendSystemMessage(Component.literal(
                "你成为了第十五代朱雀子。")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        serverPlayer.sendSystemMessage(Component.literal(
                "「朱雀星，我以王林之名，承此传承。」")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        serverPlayer.sendSystemMessage(Component.literal(
                "The Cultivation Planet Crystal erupts with boundless spiritual Qi, "
                        + "pouring into your Heaven-Defying Bead.")
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        serverPlayer.sendSystemMessage(Component.literal(
                "You have become the 15th-generation Suzaku Son.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        serverPlayer.sendSystemMessage(Component.literal(
                "\"Planet Suzaku, by the name of Wang Lin, I accept this inheritance.\"")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
        serverPlayer.sendSystemMessage(Component.literal(
                "─────────────────────────────────────")
                .withStyle(ChatFormatting.LIGHT_PURPLE));

        // 4. Record the inheritance in the HistoryManager.
        long currentTick = level.getGameTime();
        HistoryManager.onDiscovery(serverPlayer, SUBJECT_SUZAKU_SON_INHERITANCE,
                "Player inherited the Cultivation Planet Crystal at the Suzaku Tomb, "
                        + "becoming the 15th-generation Suzaku Son. The Crystal's "
                        + "spiritual Qi was transferred to the Heaven-Defying Bead. "
                        + "Realm at inheritance: " + playerRealm.name
                        + " (" + playerRealm.nameCn + ").",
                currentTick);

        Ergenverse.LOGGER.info("[Ergenverse] CRON-106: Suzaku Son inheritance triggered at {} "
                        + "for player {} (realm={}, bead present).",
                pos, serverPlayer.getName().getString(), playerRealm.name);

        // 5. CRON-107: Spawn Tuo Sen (拓森) at the Suzaku Tomb chamber.
        //    Canon (web-search verified 2026-07-26): 拓森 reappears at the
        //    朱雀墓 during the inheritance event — "时隔300年，王林在朱雀墓再遇拓森"
        //    (Sohu 2024-06-17). He is Wang Lin's Ancient God rival, an 8-star
        //    Ancient God, born from Tu Si's failed Ink Flow Split Soul Technique.
        //    He contests the Crystal and the inheritance.
        //
        //    This spawn is a CONSEQUENCE of the inheritance, not a prerequisite.
        //    It is DEFENSIVE: if it fails, the inheritance still succeeded (the
        //    player already has the Suzaku Son status). The spawn method logs
        //    warnings and sends the player a message on failure.
        try {
            dev.ergenverse.wanglin.bead.TuoSenSpawnEvent.spawnAtSuzakuTomb(
                    serverPlayer, pos, currentTick);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-107: Tuo Sen spawn failed at {}: {} "
                    + "(inheritance still succeeded).", pos, t.getMessage());
        }

        return InteractionResult.CONSUME;
    }

    // ── Helpers ────────────────────────────────────────────────────────

    /**
     * Find the Heaven-Defying Bead in the player's hands or inventory.
     * Mirrors the {@link dev.ergenverse.wanglin.bead.LiMuwanSoulCaptureEvent#findBead}
     * pattern (CRON-99): main hand → off hand → main inventory.
     *
     * @param player the player whose inventory to scan
     * @return the bead ItemStack, or {@link ItemStack#EMPTY} if not found
     */
    private static ItemStack findBead(ServerPlayer player) {
        // 1. Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof HeavenDefyingBeadItem) return mainHand;

        // 2. Off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof HeavenDefyingBeadItem) return offHand;

        // 3. Main inventory (slots 0-35)
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof HeavenDefyingBeadItem) return stack;
        }

        return ItemStack.EMPTY;
    }

    /**
     * Read the player's current cultivation realm via the CultivationCapability.
     * Mirrors the {@link dev.ergenverse.wanglin.ai.reasoning.ExpectationModelObserver#getPlayerRealm}
     * pattern (CRON-99). Returns {@link RealmId#MORTAL} if the capability
     * is not attached.
     *
     * @param player the server player
     * @return the player's current cultivation realm
     */
    private static RealmId getPlayerRealm(ServerPlayer player) {
        var stateOpt = CultivationCapability.get(player);
        if (!stateOpt.isPresent()) return RealmId.MORTAL;
        CultivationState state = stateOpt.resolve().orElse(null);
        if (state == null) return RealmId.MORTAL;
        return state.getCurrentRealm();
    }

    /**
     * CRON-118: Count the number of Sword Qi Strand items in the player's
     * inventory (main + off hand + main inventory). Used by the redemption
     * branch in {@link #use} to check if the player has the canon-required
     * 2 strands.
     *
     * @param player the server player
     * @return the total count of Sword Qi Strand items in the player's inventory
     */
    private static int countSwordQiStrands(ServerPlayer player) {
        int count = 0;
        // Main hand
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
            count += mainHand.getCount();
        }
        // Off hand
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
            count += offHand.getCount();
        }
        // Main inventory
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
                count += stack.getCount();
            }
        }
        return count;
    }

    /**
     * CRON-118: Consume {@code count} Sword Qi Strand items from the player's
     * inventory (main + off hand + main inventory). Called by the redemption
     * branch in {@link #use} after a successful Wang Ping redemption — the
     * two sword qi strands are "used up" to rebuild Wang Ping's body.
     *
     * <p>Consumption order: main inventory first, then off hand, then main
     * hand. This prioritizes keeping the player's hands free.
     *
     * <p>Defensive: if the player has fewer than {@code count} strands, this
     * method consumes as many as it finds (defensive — should not happen
     * because {@link #countSwordQiStrands} is checked first, but defensive
     * coding in case of race conditions).
     *
     * @param player the server player
     * @param count  the number of sword qi strands to consume
     */
    private static void consumeSwordQiStrands(ServerPlayer player, int count) {
        int remaining = count;
        // Main inventory first
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) break;
            if (stack.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
                int take = Math.min(remaining, stack.getCount());
                stack.shrink(take);
                remaining -= take;
            }
        }
        // Off hand
        if (remaining > 0) {
            ItemStack offHand = player.getOffhandItem();
            if (offHand.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
                int take = Math.min(remaining, offHand.getCount());
                offHand.shrink(take);
                remaining -= take;
            }
        }
        // Main hand (last resort)
        if (remaining > 0) {
            ItemStack mainHand = player.getMainHandItem();
            if (mainHand.getItem() instanceof dev.ergenverse.item.SwordQiStrandItem) {
                int take = Math.min(remaining, mainHand.getCount());
                mainHand.shrink(take);
                remaining -= take;
            }
        }
        if (remaining > 0) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-118: consumeSwordQiStrands could not "
                    + "consume all {} strands ({} remaining) for player {}.",
                    count, remaining, player.getName().getString());
        }
    }

    /**
     * CRON-117: Check whether Wang Ping is awaiting redemption (i.e., his
     * {@code deadUntilRevived} flag is still true). Used by the redemption
     * branch in {@link #use} to gate the redemption event.
     *
     * <p>Defensive: returns {@code false} if the WorldRuntime or NPCRuntime
     * is unavailable, or if Wang Ping's ActorState is not found. This
     * causes the right-click to fall through to the "Crystal is silent"
     * message — safer than crashing the interaction.
     *
     * @return {@code true} if Wang Ping is registered and his
     *         {@code deadUntilRevived} flag is true; {@code false} otherwise
     */
    private static boolean isWangPingAwaitingRedemption() {
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return false;
            dev.ergenverse.runtime.NPCRuntime.ActorState state =
                    runtime.npcs().getActor(dev.ergenverse.runtime.CanonUUID.WANG_PING);
            if (state == null) return false;
            return state.deadUntilRevived;
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-117: isWangPingAwaitingRedemption "
                    + "check failed: {}", t.getMessage());
            return false;
        }
    }

    /**
     * Record a PLAYER delta for the block-state change, so the
     * {@code inherited=true} state persists across chunk reloads.
     *
     * <p>The block-state change via {@code level.setBlock} is already
     * applied to the world; this method ensures the change is ALSO
     * recorded in the {@link WorldDeltaStore} journal, so the
     * chunk-materializer re-applies it on reload.
     *
     * <p>Uses the CRON-94 {@link dev.ergenverse.runtime.delta.BlockStateCodec}
     * format: {@code "ergenverse:cultivation_planet_crystal[inherited=true]"}.
     * The codec preserves the block-state property across serialization,
     * so the inherited flag survives chunk reload.
     *
     * <p>Defensive: silently no-ops if the WorldRuntime is not yet
     * initialized (should not happen during gameplay, but defensive
     * coding in case of registry ordering issues).
     *
     * @param pos   the block position
     * @param state the new block state (with {@code inherited=true})
     */
    private static void recordPlayerDelta(BlockPos pos, BlockState state) {
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) {
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-106: WorldRuntime not initialized; "
                        + "Crystal inheritance at {} will not persist via journal.", pos);
                return;
            }
            // Write the block-state change as a PLAYER delta via the WorldFacade.
            // The facade records the delta in the WorldDeltaStore journal AND
            // applies it to the world (the level.setBlock above already applied
            // it; the facade call here is for journaling only — the duplicate
            // world-write is a harmless no-op).
            //
            // The blockId string is the CRON-94 codec format:
            //   "ergenverse:cultivation_planet_crystal[inherited=true]"
            // which preserves the inherited property across serialization.
            String blockId = dev.ergenverse.runtime.delta.BlockStateCodec.serialize(state);
            runtime.world().setPlayerBlock(pos.getX(), pos.getY(), pos.getZ(), blockId);
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-106: Failed to record PLAYER delta "
                    + "for Crystal inheritance at {}: {}", pos, t.getMessage());
        }
    }
}
