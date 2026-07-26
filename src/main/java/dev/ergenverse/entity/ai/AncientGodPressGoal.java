package dev.ergenverse.entity.ai;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

/**
 * AncientGodPressGoal — AoE ground-pound attack for Tuo Sen (8-star Ancient God).
 *
 * <p><b>CRON-COMPLETIONIST-108 — Ancient God Combat AI (point (g) extension)</b>
 *
 * <p>Closes the CRON-107 self-critique #14 documented gap: Tuo Sen reused the
 * generic {@link CultivatorCombatGoal} (melee single-target + realm-scaled damage),
 * which is tuned for standard cultivators. An 8-star Ancient God requires
 * Ancient-God-specific combat goals that reflect his canon combat style:
 * raw god-body power and AoE devastation, not mortal melee swings.
 *
 * <h2>Canon basis (web-search verified 2026-07-26)</h2>
 * <ul>
 *   <li>Baidu Baike (拓森): 8-star Ancient God born from Tu Si's (涂司) failed
 *       Ink Flow Split Soul Technique (墨流分魂术). Inherits Tu Si's <b>power</b>
 *       portion; rival to Wang Lin's <b>knowledge</b> portion.</li>
 *   <li>Sohu (2024-06-17): '时隔300年，王林在朱雀墓再遇拓森' — Wang Lin
 *       re-encounters Tuo Sen at the Suzaku Tomb after 300 years.</li>
 *   <li>163 (2025-07-29): '拓森现身朱雀墓，获得九星古神血液，抢下修星之晶'
 *       — Tuo Sen appears at the Suzaku Tomb, obtains 9-star Ancient God
 *       blood, seizes the Cultivation Planet Crystal.</li>
 * </ul>
 *
 * <h2>Mechanic — God Press (古神踏压)</h2>
 * <p>When Tuo Sen's target is in mid-range (4-16 blocks) and the goal is off
 * cooldown, he leaps into the air (visual: a sudden vertical jump via
 * {@code mob.setDeltaMovement(0, 1.2, 0)}), then crashes down 10 ticks later.
 * On landing:
 * <ul>
 *   <li>All entities within 6 blocks (excluding Tuo Sen himself) take 80
 *       base damage (an 8-star Ancient God's ground pound is devastating).</li>
 *   <li>Hit entities are knocked upward (y += 0.6) and outward (radial push).</li>
 *   <li>A massive particle burst: 30 POOF (dust cloud), 20 SQUID_INK (dark
 *       god-force visual), 10 CRIT (impact sparks).</li>
 *   <li>A loud {@code ENDER_DRAGON_GROWL} sound at 0.7 pitch (deep, ominous).</li>
 *   <li>The chunk blocks within 3 blocks of impact get a brief particle
 *       shower (no actual block changes — this is pure visual).</li>
 * </ul>
 *
 * <p>Cooldown: 8 seconds (160 ticks) — Ancient God attacks are slow but
 * devastating. The player has time to dodge between presses if they're
 * paying attention.
 *
 * <p><b>Activation gate:</b> only activates when
 * {@code mob.getCharacterId().equals("tuo_sen")} — this goal is a no-op for
 * all other cultivators. This is implemented in {@link #canUse()} so the
 * goal can be registered globally in {@link EntityCultivator#registerGoals()}
 * without polluting other NPCs' AI.
 *
 * <p><b>Self-critique:</b>
 * <ul>
 *   <li>The leap uses {@code setDeltaMovement} which is a server-side
 *       physics override — the client will interpolate. This may look
 *       slightly laggy on high-latency connections. A real implementation
 *       would use a custom leap animation packet. Acceptable for CRON-108.</li>
 *   <li>The damage bypasses ATTACK_DAMAGE attribute (direct hurt call).
 *       This is consistent with {@link CultivatorCombatGoal} but means
 *       armor enchants like Protection still apply (good).</li>
 *   <li>The particle burst is server-side ({@code sendParticles}). On
 *       single-player maximalism (Article XLIII), this is correct —
 *       every nearby player sees the burst.</li>
 *   <li><b>CRON-109: Block cratering now implemented.</b> The crash-down
 *     carves a 3-block-radius crater around the impact center via
 *     {@code runtime.world().setSimulationBlock(...)}. The center becomes
 *     coarse_dirt (the impact divot), the ring becomes cracked_stone
 *     (the shattered ground), and the outer ring becomes cobblestone
 *     (loose debris). Bedrock and air are skipped. This closes the
 *     CRON-108 self-critique #4 documented enhancement.</li>
 *   <li>The leap height (1.2 y-velocity = ~7 blocks peak) is high enough
 *       to clear most terrain but not so high that Tuo Sen flies out of
 *       the tomb chamber (the Suzaku Tomb chamber is ~10 blocks tall).</li>
 * </ul>
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.
 */
public class AncientGodPressGoal extends Goal {

    /** Range at which the press can be initiated (squared). 4-16 blocks → 16-256. */
    private static final double MIN_RANGE_SQ = 16.0D;   // 4 blocks
    private static final double MAX_RANGE_SQ = 256.0D;  // 16 blocks

    /** AoE damage radius (in blocks, not squared). 6 blocks = devastating. */
    private static final double AOE_RADIUS = 6.0D;

    /**
     * CRON-109: Block crater radius. 3 blocks = the visually obvious
     * crater without being so large it eats the entire tomb chamber.
     * Canon: an 8-star Ancient God's ground pound would crater the
     * ground, but the Suzaku Tomb chamber is finite — we cap the crater
     * at 3 blocks to preserve playability.
     */
    private static final int CRATER_RADIUS = 3;

    /** Base damage of the press. 80 = one-shots a mortal, badly hurts a cultivator. */
    private static final float PRESS_DAMAGE = 80.0F;

    /** Cooldown in ticks (8 seconds = 160 ticks). */
    private static final int COOLDOWN_TICKS = 160;

    /** Leap duration in ticks (the air-time before the crash-down). */
    private static final int LEAP_DURATION = 10;

    /** Leap y-velocity (1.2 = ~7 blocks peak height). */
    private static final double LEAP_VELOCITY = 1.2D;

    private final Mob mob;
    private int cooldown;
    private int leapTimer;
    private boolean hasLeapt;

    public AncientGodPressGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // CRON-108: Only Tuo Sen uses this goal. Other cultivators no-op.
        if (!(mob instanceof EntityCultivator ec)) return false;
        if (!"tuo_sen".equals(ec.getCharacterId())) return false;

        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        double distSq = mob.distanceToSqr(target);
        if (distSq < MIN_RANGE_SQ || distSq > MAX_RANGE_SQ) return false;

        // Don't press if we're already in melee range (let CultivatorCombatGoal handle that)
        if (distSq < MIN_RANGE_SQ) return false;

        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return leapTimer > 0 && mob.getTarget() != null && mob.getTarget().isAlive();
    }

    @Override
    public void start() {
        leapTimer = LEAP_DURATION;
        hasLeapt = false;
        // Face the target during the leap
        LivingEntity target = mob.getTarget();
        if (target != null) {
            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

    @Override
    public void stop() {
        leapTimer = 0;
        hasLeapt = false;
        cooldown = COOLDOWN_TICKS;
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            leapTimer = 0;
            return;
        }

        leapTimer--;

        // Face the target throughout the leap
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // On the first tick, leap into the air
        if (!hasLeapt) {
            mob.setDeltaMovement(0, LEAP_VELOCITY, 0);
            hasLeapt = true;
            // Charging particles at takeoff
            if (mob.level() instanceof ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SQUID_INK,
                        mob.getX(), mob.getY() + 0.5, mob.getZ(), 12, 0.5, 0.5, 0.5, 0.05);
            }
        }

        // When the leap timer expires, crash down
        if (leapTimer <= 0) {
            crashDown();
            cooldown = COOLDOWN_TICKS;
        }
    }

    /**
     * Execute the ground-pound impact: AoE damage + particles + knockback + sound.
     */
    private void crashDown() {
        if (!(mob.level() instanceof ServerLevel sl)) return;

        Vec3 impact = mob.position();
        AABB aoe = new AABB(
                impact.x - AOE_RADIUS, impact.y - 1.0, impact.z - AOE_RADIUS,
                impact.x + AOE_RADIUS, impact.y + 3.0, impact.z + AOE_RADIUS);

        // Find all entities in the AoE (excluding self)
        List<LivingEntity> hits = sl.getEntitiesOfClass(LivingEntity.class, aoe,
                e -> e != mob && e.isAlive());

        for (LivingEntity hit : hits) {
            // Damage
            hit.hurt(mob.damageSources().mobAttack(mob), PRESS_DAMAGE);

            // Radial knockback (away from impact center)
            Vec3 push = hit.position().subtract(impact);
            double dist = push.length();
            if (dist < 0.01) {
                // Too close — pick a random direction
                push = new Vec3(mob.getRandom().nextDouble() - 0.5, 0, mob.getRandom().nextDouble() - 0.5);
            } else {
                push = push.normalize();
            }
            double strength = 1.0 - (dist / AOE_RADIUS);  // closer = stronger
            strength = Math.max(0.3, strength);
            hit.push(push.x * strength, 0.6, push.z * strength);
        }

        // ── Particle burst ──
        // 30 POOF (dust cloud at impact)
        sl.sendParticles(ParticleTypes.POOF,
                impact.x, impact.y + 0.1, impact.z, 30, AOE_RADIUS * 0.4, 0.3, AOE_RADIUS * 0.4, 0.1);

        // 20 SQUID_INK (dark god-force visual — rising ink)
        sl.sendParticles(ParticleTypes.SQUID_INK,
                impact.x, impact.y + 0.5, impact.z, 20, AOE_RADIUS * 0.5, 0.8, AOE_RADIUS * 0.5, 0.05);

        // 10 CRIT (impact sparks)
        sl.sendParticles(ParticleTypes.CRIT,
                impact.x, impact.y + 1.0, impact.z, 10, AOE_RADIUS * 0.3, 0.5, AOE_RADIUS * 0.3, 0.3);

        // 8 END_ROD (god-body energy — bright rising sparks)
        sl.sendParticles(ParticleTypes.END_ROD,
                impact.x, impact.y + 1.5, impact.z, 8, AOE_RADIUS * 0.2, 0.4, AOE_RADIUS * 0.2, 0.1);

        // ── Sound ──
        // ENDER_DRAGON_GROWL at 0.7 pitch — deep, ominous, Ancient God roar
        mob.playSound(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, 1.5F, 0.7F);

        // Secondary sound: a heavy THUD
        mob.playSound(net.minecraft.sounds.SoundEvents.WITHER_BREAK_BLOCK, 1.0F, 0.5F);

        // ── CRON-109: Carve a crater in the ground ──
        // Canon: an 8-star Ancient God's ground pound would crater the
        // ground. The crater is carved via the WorldFacade's simulation
        // block-write API, which journals each change as SIMULATION
        // provenance — survives world reload, doesn't modify the blueprint.
        carveCrater(sl, impact);
    }

    /**
     * CRON-109: Carve a 3-block-radius crater around the impact center.
     *
     * <p>The crater has three concentric rings:
     * <ul>
     *   <li><b>Core (distance &le; 1):</b> coarse_dirt — the impact divot,
     *       where the god-body directly struck.</li>
     *   <li><b>Inner ring (1 &lt; distance &le; 2):</b> cracked_stone — the
     *       shattered ground around the impact.</li>
     *   <li><b>Outer ring (2 &lt; distance &le; 3):</b> cobblestone — loose
     *       debris where the shockwave fractured the surface.</li>
     * </ul>
     *
     * <p><b>Skip rules:</b>
     * <ul>
     *   <li><b>Air:</b> no-op (can't crater nothing).</li>
     *   <li><b>Bedrock:</b> no-op (canon: even an Ancient God can't
     *       shatter bedrock — it's the world's foundation).</li>
     *   <li><b>Already-cratered blocks:</b> the simulation layer's
     *       journal naturally handles this — re-writing the same block
     *       at the same position just appends a new delta, which is
     *       idempotent on apply.</li>
     *   <li><b>Fragile blocks (plants, snow, torches):</b> converted to
     *       air instead of cracked_stone. Canon: these would be
     *       obliterated by the press, not cracked.</li>
     * </ul>
     *
     * <p><b>Provenance:</b> all writes go through
     * {@code runtime.world().setSimulationBlock(...)} — the WorldFacade
     * routes them to BOTH the live level (immediate visibility) AND the
     * WorldDeltaStore journal (persistence + provenance = SIMULATION).
     * This is the canonical CRON-69 pattern: gameplay never touches the
     * store directly, only the facade.
     *
     * <p><b>Defensive:</b> silently no-ops if WorldRuntime is not yet
     * initialized (should not happen during combat, but defensive coding
     * in case of early-tick edge cases).
     *
     * @param level   the server level
     * @param impact  the impact position (the mob's crash-down location)
     */
    private void carveCrater(ServerLevel level, Vec3 impact) {
        WorldRuntime runtime;
        try {
            runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) {
                Ergenverse.LOGGER.warn("[Ergenverse] CRON-109: WorldRuntime not initialized — "
                        + "Tuo Sen press crater at ({}, {}, {}) will not be carved.",
                        (int) impact.x, (int) impact.y, (int) impact.z);
                return;
            }
        } catch (Throwable t) {
            Ergenverse.LOGGER.warn("[Ergenverse] CRON-109: WorldRuntime unavailable — "
                    + "Tuo Sen press crater at ({}, {}, {}) will not be carved: {}",
                    (int) impact.x, (int) impact.y, (int) impact.z, t.getMessage());
            return;
        }

        int cx = (int) Math.floor(impact.x);
        int cy = (int) Math.floor(impact.y);
        int cz = (int) Math.floor(impact.z);
        int blocksCarved = 0;

        for (int dx = -CRATER_RADIUS; dx <= CRATER_RADIUS; dx++) {
            for (int dy = -CRATER_RADIUS; dy <= CRATER_RADIUS; dy++) {
                for (int dz = -CRATER_RADIUS; dz <= CRATER_RADIUS; dz++) {
                    // Sphere mask — skip corners outside the radius
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist > CRATER_RADIUS) continue;

                    // Only carve at or below the impact Y — the crater
                    // goes DOWN (god-body presses into the ground), not
                    // up (we don't want to delete the ceiling).
                    if (dy > 0) continue;
                    // Allow dy == 0 (the impact layer) and dy < 0 (below)

                    BlockPos pos = new BlockPos(cx + dx, cy + dy, cz + dz);
                    BlockState existing = level.getBlockState(pos);

                    // Skip air
                    if (existing.isAir()) continue;
                    // Skip bedrock — canon: even Ancient Gods respect the world's foundation
                    if (existing.getBlock() == Blocks.BEDROCK) continue;

                    // Determine the crater material based on distance
                    String targetBlockId;
                    if (dist <= 1.0D) {
                        // Core: coarse_dirt (the impact divot)
                        targetBlockId = "minecraft:coarse_dirt";
                    } else if (dist <= 2.0D) {
                        // Inner ring: cracked_stone (shattered ground)
                        // BUT: if the existing block is fragile (plant,
                        // snow, torch, etc.), obliterate it to air instead.
                        if (isFragile(existing)) {
                            targetBlockId = "minecraft:air";
                        } else {
                            targetBlockId = "minecraft:cracked_stone";
                        }
                    } else {
                        // Outer ring: cobblestone (loose debris)
                        // Fragile blocks are still obliterated to air.
                        if (isFragile(existing)) {
                            targetBlockId = "minecraft:air";
                        } else {
                            targetBlockId = "minecraft:cobblestone";
                        }
                    }

                    // Skip no-ops: if the existing block is already the
                    // target block, don't write a redundant delta.
                    if (blockIdMatches(existing, targetBlockId)) continue;

                    // Write through the WorldFacade — journals as SIMULATION
                    // provenance AND mirrors to the live level.
                    try {
                        runtime.world().setSimulationBlock(
                                pos.getX(), pos.getY(), pos.getZ(), targetBlockId);
                        blocksCarved++;
                    } catch (Throwable t) {
                        Ergenverse.LOGGER.debug("[Ergenverse] CRON-109: carveCrater failed at "
                                + "({}, {}, {}): {}", pos.getX(), pos.getY(), pos.getZ(),
                                t.getMessage());
                    }
                }
            }
        }

        if (blocksCarved > 0) {
            Ergenverse.LOGGER.info("[Ergenverse] CRON-109: Tuo Sen press crater carved {} "
                    + "blocks at ({}, {}, {}) (radius={}, provenance=SIMULATION).",
                    blocksCarved, cx, cy, cz, CRATER_RADIUS);
        }
    }

    /**
     * CRON-109: Is the given block state "fragile" — should it be
     * obliterated to air rather than converted to cracked_stone?
     *
     * <p>Fragile blocks are: plants (saplings, grass, flowers), crops,
     * snow layers, torches, ladders, buttons, levers, pressure plates,
     * signs, banners, redstone dust, rails, and similar non-structural
     * blocks. An 8-star Ancient God's press would obliterate them, not
     * crack them.
     *
     * <p>Implementation: use the vanilla {@code Block.tags} system where
     * possible (DRY), with explicit block checks for edge cases.
     *
     * @param state the block state to test
     * @return {@code true} if the block should be obliterated to air
     */
    private static boolean isFragile(BlockState state) {
        // Tag-based checks (most plants, etc. are in these tags)
        if (state.is(net.minecraft.tags.BlockTags.SAPLINGS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.FLOWERS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.CROPS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.BUTTONS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.PRESSURE_PLATES)) return true;
        if (state.is(net.minecraft.tags.BlockTags.WOODEN_BUTTONS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.WOODEN_PRESSURE_PLATES)) return true;
        if (state.is(net.minecraft.tags.BlockTags.SIGNS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.STANDING_SIGNS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.WALL_SIGNS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.BANNERS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.RAILS)) return true;
        if (state.is(net.minecraft.tags.BlockTags.LEAVES)) return true;
        if (state.is(net.minecraft.tags.BlockTags.REPLACEABLE)) return true;
        if (state.is(net.minecraft.tags.BlockTags.REPLACEABLE_BY_TREES)) return true;

        // Explicit block checks for blocks not in convenient tags
        net.minecraft.world.level.block.Block block = state.getBlock();
        if (block == Blocks.TORCH) return true;
        if (block == Blocks.WALL_TORCH) return true;
        if (block == Blocks.SOUL_TORCH) return true;
        if (block == Blocks.SOUL_WALL_TORCH) return true;
        if (block == Blocks.REDSTONE_TORCH) return true;
        if (block == Blocks.REDSTONE_WALL_TORCH) return true;
        if (block == Blocks.LADDER) return true;
        if (block == Blocks.LEVER) return true;
        if (block == Blocks.SNOW) return true;          // snow layer
        if (block == Blocks.SNOW_BLOCK) return false;   // snow block is solid
        if (block == Blocks.REDSTONE_WIRE) return true;
        if (block == Blocks.REPEATER) return true;
        if (block == Blocks.COMPARATOR) return true;
        if (block == Blocks.TRIPWIRE) return true;
        if (block == Blocks.TRIPWIRE_HOOK) return true;
        if (block == Blocks.COBWEB) return true;
        if (block == Blocks.SUGAR_CANE) return true;
        if (block == Blocks.BAMBOO) return true;
        if (block == Blocks.BAMBOO_SAPLING) return true;
        if (block == Blocks.TALL_GRASS) return true;
        if (block == Blocks.LARGE_FERN) return true;
        if (block == Blocks.GRASS) return true;
        if (block == Blocks.FERN) return true;

        return false;
    }

    /**
     * CRON-109: Does the existing block state already match the target
     * block-id string? Used to skip redundant writes (no-op carving).
     *
     * @param existing      the current block state in the world
     * @param targetBlockId the target block-id string (e.g. "minecraft:coarse_dirt")
     * @return {@code true} if the existing block already matches the target
     */
    private static boolean blockIdMatches(BlockState existing, String targetBlockId) {
        var existingKey = net.minecraftforge.registries.ForgeRegistries.BLOCKS
                .getKey(existing.getBlock());
        if (existingKey == null) return false;
        String existingId = existingKey.toString();
        // For air target, also treat existing air as a match (handled by
        // the isAir() check above, but defensive here too).
        return existingId.equals(targetBlockId);
    }
}
