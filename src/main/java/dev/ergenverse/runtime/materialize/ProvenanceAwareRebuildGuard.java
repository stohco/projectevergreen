package dev.ergenverse.runtime.materialize;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.Provenance;
import dev.ergenverse.runtime.WorldRuntime;
import dev.ergenverse.runtime.delta.WorldDeltaStore;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

/**
 * ProvenanceAwareRebuildGuard — the missing piece that makes the
 * {@link WorldDeltaStore} journal actually gate structure rebuilds.
 *
 * <h2>CRON-COMPLETIONIST-69 — PROVENANCE-AWARE REBUILD GUARD (point d)</h2>
 *
 * <p><b>The bug this fixes.</b> All 7 chunk-scoped structure builders
 * ({@code WangFamilyVillageBuilder}, {@code HengYueSectBuilder},
 * {@code TengFamilyCityBuilder}, {@code TianShuiCityBuilder},
 * {@code NanDouCityBuilder}, {@code SoulRefiningSectBuilder},
 * {@code XuanDaoSectBuilder}) had an {@code isAlreadyBuilt(level, center)}
 * guard that checked only the marker block via
 * {@code level.getBlockState(...)}. If a player broke the marker block
 * (e.g., the Wang Family Village spirit-vein stone centerpiece), the guard
 * returned {@code false}, so {@code build()} proceeded and re-placed the
 * marker — undoing the player's edit. This defeated the entire purpose of
 * the WorldDeltaStore journal: the journal recorded the player's edit, but
 * the rebuild ignored it.
 *
 * <p><b>The fix.</b> This utility provides {@link #shouldSkipRebuild} — a
 * pure function that returns {@code true} if a PLAYER or SIMULATION delta
 * exists at the marker position. Each builder's {@code isAlreadyBuilt}
 * becomes:
 * <ol>
 *   <li><b>Marker present</b> → already built (canon state — existing behavior).</li>
 *   <li><b>Marker absent + player/sim delta at marker</b> → was built, then
 *       edited → don't rebuild (NEW — fixes the bug).</li>
 *   <li><b>Marker absent + no delta</b> → never built → rebuild (existing
 *       behavior).</li>
 * </ol>
 *
 * <p><b>Why check only the marker position (not the whole structure).</b>
 * The marker is the canonical "is this structure present?" signal. If the
 * player breaks a non-marker block (e.g., a wall block), the marker is
 * still present → {@code isAlreadyBuilt} returns true → no full rebuild.
 * The chunk-scoped path's per-block {@code sb()} guard (already in
 * WangFamilyVillageBuilder since CRON-63) handles non-marker blocks: it
 * checks the delta store at each block position and skips re-placing
 * blocks the player has edited. So the two guards are complementary:
 * <ul>
 *   <li>{@code isAlreadyBuilt} (full-build path): checks the marker —
 *       decides whether to trigger a full rebuild at all.</li>
 *   <li>{@code sb()} per-block guard (chunk-scoped path): checks each
 *       block — skips re-placing individual blocks the player has edited.</li>
 * </ul>
 *
 * <p><b>Why not also check the whole structure footprint.</b> That would
 * be O(structure_size) per {@code isAlreadyBuilt} call. The marker check
 * is O(1) (one HashMap lookup per provenance). The marker is sufficient
 * because the three cases above are exhaustive.
 *
 * <p><b>The creeper-explosion edge case.</b> If a creeper destroys the
 * marker, there's no player/sim delta at the marker position, so
 * {@code isAlreadyBuilt} returns false, and {@code build()} re-places the
 * marker. This is acceptable — creeper explosions are not player edits and
 * should be repaired. (If we wanted to handle this, we'd need to record
 * creeper damage as a SIMULATION delta, which is a future enhancement.)
 *
 * <p><b>Defensive behavior.</b> Returns {@code false} (proceed with
 * placement) if the WorldRuntime is not yet initialized. The
 * chunk-materializer already gates on {@code runtime.isInitialized()} so
 * this should never fire in the materializer path, but defense-in-depth
 * protects against unexpected call sites (e.g., a builder called before
 * the runtime is wired).
 *
 * <p><b>Thread safety.</b> All calls delegate to {@link WorldDeltaStore#hasBlock}
 * which is {@code synchronized}. Safe to call from any thread.
 *
 * <p><b>Performance.</b> O(1) per call — two HashMap lookups (one per
 * provenance) + one {@link dev.ergenverse.runtime.PackedPos#pack}. ~50ns.
 * Called once per {@code isAlreadyBuilt} check (rare: server-start or
 * manual command). Negligible.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class ProvenanceAwareRebuildGuard {

    private ProvenanceAwareRebuildGuard() {}

    /**
     * Returns {@code true} if a PLAYER or SIMULATION delta is recorded at
     * {@code markerPos} — meaning the structure WAS built and then edited,
     * so a full rebuild would undo the edit. The caller should treat the
     * structure as "already built" and skip the rebuild.
     *
     * <p>Defensive: returns {@code false} (no delta → proceed with rebuild)
     * if the WorldRuntime is not yet initialized.
     *
     * @param markerPos the marker block position (e.g.,
     *                  {@code center.above()} for the spirit-vein stone)
     * @return true if a PLAYER or SIMULATION delta exists at the position
     */
    public static boolean shouldSkipRebuild(BlockPos markerPos) {
        return shouldSkipRebuild(markerPos.getX(), markerPos.getY(), markerPos.getZ());
    }

    /**
     * Returns {@code true} if a PLAYER or SIMULATION delta is recorded at
     * the given coordinates. See {@link #shouldSkipRebuild(BlockPos)} for
     * the full contract.
     *
     * @param x marker block X
     * @param y marker block Y
     * @param z marker block Z
     * @return true if a PLAYER or SIMULATION delta exists at the position
     */
    public static boolean shouldSkipRebuild(int x, int y, int z) {
        try {
            WorldRuntime runtime = WorldRuntime.get();
            if (!runtime.isInitialized()) return false;
            WorldDeltaStore store = runtime.deltaStore();
            return store.hasBlock(x, y, z, Provenance.PLAYER)
                    || store.hasBlock(x, y, z, Provenance.SIMULATION);
        } catch (Throwable t) {
            // Defensive: never let a delta-store query failure block a build.
            // Log at debug (not error) to avoid log spam if this fires
            // repeatedly in a broken state.
            Ergenverse.LOGGER.debug("[Ergenverse] ProvenanceAwareRebuildGuard failed at ({}, {}, {}): {} — proceeding with rebuild.",
                    x, y, z, t.getMessage());
            return false;
        }
    }

    /**
     * Convenience: returns {@code true} if the structure should be
     * considered "already built" given the in-world marker state AND the
     * delta journal. This is the canonical replacement for the old
     * {@code isAlreadyBuilt} pattern:
     *
     * <pre>{@code
     * // OLD (buggy):
     * return level.getBlockState(markerPos).getBlock() == EXPECTED;
     *
     * // NEW (provenance-aware):
     * return ProvenanceAwareRebuildGuard.isAlreadyBuilt(
     *         level, markerPos, EXPECTED_STATE::equals);
     * }</pre>
     *
     * @param level the server level (for the in-world marker check)
     * @param markerPos the marker block position
     * @param markerPresentPredicate a predicate that returns true if the
     *        block state at {@code markerPos} matches the expected marker
     *        (e.g., {@code state -> state.getBlock() == Blocks.SMOOTH_STONE})
     * @return true if the structure is already built (marker present OR
     *         player/sim delta at marker)
     */
    public static boolean isAlreadyBuilt(
            ServerLevel level,
            BlockPos markerPos,
            java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> markerPresentPredicate) {
        // 1. Marker present → already built (canon state).
        if (markerPresentPredicate.test(level.getBlockState(markerPos))) return true;
        // 2. Player/sim delta at marker → was built, then edited → don't rebuild.
        if (shouldSkipRebuild(markerPos)) return true;
        // 3. No marker, no delta → never built → rebuild.
        return false;
    }
}
