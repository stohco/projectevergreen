package dev.ergenverse.block;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.wanglin.WangLinItems;
import dev.ergenverse.wanglin.bead.HeavenDefyingBeadItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;
import java.util.List;

/**
 * MysteriousStoneBlock — the stone on Heng Yue Mountain in which Wang Lin
 * found the Heaven-Defying Bead per Renegade Immortal Ch. 8+.
 *
 * <p><b>Canon:</b> 仙逆编年史 — "王林不合格被拒门外，凭借毅力独自上山遇险后
 * 发现天逆珠法宝" (Wang Lin failed the entry test, climbed the mountain
 * alone with perseverance, encountered danger, THEN discovered the bead).
 * The stone itself is mod-original (canon does not describe its appearance
 * in detail). The CRON-76 hint book describes it as "darker than the others,
 * with veins like frozen lightning" — matching the violet-crack-vein texture
 * shipped in CRON-75.
 *
 * <h2>CRON-77: Two fixes (finishing the block to a high bar)</h2>
 *
 * <ul>
 *   <li><b>Programmatic bead drop (closes CRON-75 critique #2, deferred
 *       priority d, Score 7/10).</b>
 *       Previously the loot table at
 *       {@code data/ergenverse/loot_tables/blocks/mysterious_stone.json}
 *       hardcoded the NBT values
 *       {@code {Ergen.Bead.Stage:1, Ergen.Bead.PartsAligned:1,
 *       Ergen.Bead.SpatialStability:1000, Ergen.Bead.OwnerAuthority:500,
 *       Ergen.Bead.InteriorGrowth:0, Ergen.Bead.Spirit:1}}. These values
 *       DUPLICATE the constants in
 *       {@link HeavenDefyingBeadItem#applyInitialOpening(ItemStack)}. If those
 *       constants ever change, the loot table would drift out of sync with
 *       the Java code, producing a bead with the wrong initial state.
 *       This block now overrides {@link #getDrops} to construct the bead
 *       stack programmatically and call {@code applyInitialOpening} directly.
 *       The loot table JSON is emptied (pools: []) as a safety net — if any
 *       code path bypasses the override (e.g., a third-party mod querying
 *       the loot table directly), no NBT-bearing bead is dropped; the player
 *       simply gets nothing. There is exactly ONE source of truth for the
 *       bead's initial state: {@code applyInitialOpening}.</li>
 *
 *   <li><b>Subtle mycelium particles (closes CRON-76 priority g, Score 5/10).</b>
 *       The block now emits occasional {@link ParticleTypes#MYCELIUM} particles
 *       at its center. This is a SUBTLE visual cue that the stone is unusual —
 *       matching the violet-crack-vein texture (mycelium particles are small
 *       and purple, drifting in air) and the hint book's "veins like frozen
 *       lightning" description. Spawn rate is 1/30 ticks ≈ once per 1.5
 *       seconds. The particle is small, drifts slowly, and is visible at
 *       any light level. (Note: {@code ParticleTypes.AMETHYST} does not
 *       exist in MC 1.20.1 — mycelium was chosen as the closest subtle
 *       purple ambient particle.)</li>
 * </ul>
 *
 * <h2>Light level — stays at 0 (no glow)</h2>
 * <p>The block does NOT glow. The CRON-76 hint book describes the stone as
 * "darker than the others" — a glow would directly contradict this. The
 * particle effect is the visual cue, not a light source. A glow would also
 * make the stone trivially visible at night, undermining the "discovery"
 * feel: canon demands Wang Lin finds the stone by perseverance (climbing
 * the mountain alone after rejection), not by following a beacon.
 *
 * <h2>Provenance</h2>
 * <p>The stone is placed by
 * {@link dev.ergenverse.spawn.HengYueSectBuilder#buildMysteriousStoneDiscovery}
 * via {@code sb()} (provenance-aware, chunk-filtered). If the player breaks
 * the stone, a PLAYER "air" delta is recorded at its position, and the
 * chunk-materializer will not re-place it on reload — the discovery is
 * permanent, and the bead is gone forever from this save. This is canon-
 * faithful: the bead is found ONCE, and Wang Lin's story starts from that
 * moment.
 *
 * <h2>Drop cause</h2>
 * <p>{@link #getDrops} fires regardless of cause (player break, explosion,
 * piston, etc.). This is canon-appropriate: the bead is INSIDE the stone,
 * so destroying the stone by ANY means reveals the bead. Wang Lin cracked
 * the stone open with perseverance; an explosion could do the same. In
 * single-player maximalism mode, the player IS Wang Lin — if they choose
 * TNT over perseverance, that's their story.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 *
 * @see HeavenDefyingBeadItem#applyInitialOpening(ItemStack)
 * @see dev.ergenverse.spawn.HengYueSectBuilder#buildMysteriousStoneDiscovery
 */
public class MysteriousStoneBlock extends Block {

    /** Particle spawn probability: 1/N per tick. 30 ≈ once per 1.5 seconds. */
    private static final int PARTICLE_TICK_INTERVAL = 30;

    /**
     * Construct a MysteriousStoneBlock with the given properties.
     *
     * <p>Properties should set hardness/resistance/sound (callers typically
     * pass {@code BlockBehaviour.Properties.of().strength(3.0F, 6.0F)
     * .sound(SoundType.STONE)}). Light level is NOT set here — the block
     * stays at default light level 0 (no glow), per the canon description
     * "darker than the others".
     *
     * @param properties block behavior properties
     */
    public MysteriousStoneBlock(Properties properties) {
        super(properties);
    }

    /**
     * Override getDrops to construct the bead stack programmatically.
     *
     * <p>This replaces the static-NBT loot table at
     * {@code data/ergenverse/loot_tables/blocks/mysterious_stone.json}.
     * The loot table JSON is emptied (pools: []) as a safety net — in case
     * any code path bypasses this override, no NBT-bearing bead is dropped;
     * the player simply gets nothing. There is exactly ONE source of truth
     * for the bead's initial state: {@link HeavenDefyingBeadItem#applyInitialOpening}.
     *
     * <p>The bead stack is initialized to the CRACK_OPENED stage with Situ
     * Nan's spirit via {@code applyInitialOpening}. This is the same
     * initialization Wang Lin's bead undergoes in canon (Ch. 8 — Situ Nan's
     * remnant soul blasts the stone bead open, revealing a small interior
     * chamber).
     *
     * @param state   the block state being broken
     * @param builder loot params builder (level, origin, tool, etc.) — unused
     *                because the drop is not context-dependent
     * @return a singleton list containing one initialized bead stack
     */
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        RegistryObject<Item> beadRO = WangLinItems.get("wanglin/heaven_defying_bead");
        if (beadRO == null || !beadRO.isPresent()) {
            // Bead not registered yet — return empty list. This should not
            // happen during normal play (the bead is registered during mod
            // bootstrap before any chunk loads), but defensive coding in
            // case of registry ordering issues during dev.
            Ergenverse.LOGGER.warn("[Ergenverse] MysteriousStoneBlock: heaven_defying_bead not registered; "
                    + "no drops will be generated.");
            return Collections.emptyList();
        }

        ItemStack beadStack = new ItemStack(beadRO.get());
        HeavenDefyingBeadItem.applyInitialOpening(beadStack);
        return Collections.singletonList(beadStack);
    }

    /**
     * Spawn subtle mycelium particles at the block's center.
     *
     * <p>Spawn rate: 1/{@value #PARTICLE_TICK_INTERVAL} ticks per block ≈
     * once per 1.5 seconds. The particle is {@link ParticleTypes#MYCELIUM}
     * — small, purple, drifting in air — matching the violet-crack-vein
     * texture and the hint book's "veins like frozen lightning" description.
     * ({@code ParticleTypes.AMETHYST} does not exist in MC 1.20.1; mycelium
     * was chosen as the closest subtle purple ambient particle.)
     *
     * <p>This is CLIENT-ONLY. {@code animateTick} is only called on the
     * client when the block is in view.
     *
     * <p>Light level: 0 (no glow). The particle is visible at any light
     * level — players exploring caves or night-time mountain sides will
     * see the particle without the block becoming a beacon.
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

        // Spawn at the block's center, with a small random offset so the
        // particle doesn't always appear at exactly the same point.
        double x = pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double y = pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 0.4;
        double z = pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.4;

        // MYCELIUM particles drift slowly with slight horizontal spread —
        // matches the "frozen lightning" visual of the veins. Mycelium
        // particles have built-in drift behavior, so the velocity here is
        // a small nudge rather than a strong push.
        double dx = (random.nextDouble() - 0.5) * 0.05;
        double dy = 0.02 + random.nextDouble() * 0.03;   // gentle upward drift
        double dz = (random.nextDouble() - 0.5) * 0.05;

        level.addParticle(ParticleTypes.MYCELIUM, x, y, z, dx, dy, dz);
    }
}
