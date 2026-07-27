package dev.ergenverse.assembly;

import dev.ergenverse.canon.structure.SemanticRole;

import java.util.HashMap;
import java.util.Map;

/**
 * AnchorRegistryService — the singleton broker that keeps compiled
 * {@link AnchorRegistry} instances addressable by settlement id at runtime.
 *
 * <p><b>CRON-129 — WIRE ANCHORREGISTRY INTO AI/NAVIGATION LAYER.</b>
 *
 * <p><b>The gap this closes.</b> CRON-127 built the World Assembly Compiler
 * pipeline: a {@link CanonSettlement} is compiled by {@link WorldAssembler}
 * into an {@link AssemblyResult} that contains BOTH the voxel instructions
 * AND a populated {@link AnchorRegistry}. The voxel side was wired through
 * {@link dev.ergenverse.materialization.VoxelMaterializer} to the live
 * {@code ServerLevel} — blocks appear in the world. But the anchor side
 * was THROWN AWAY. The {@link dev.ergenverse.materialization.CanonSettlementBuilder}
 * logged {@code "anchors=N"} and then dropped the registry on the floor.
 *
 * <p>That meant no AI code could answer the question the user kept asking:
 * <blockquote>
 *   "Find Wang Lin → Find House → Find Bedroom → Find Bed → Compiler Anchor
 *   → Navigation Target. […] AI never searches blocks."
 * </blockquote>
 *
 * <p>The meditation mat at Wang Lin's bedroom had a {@link SemanticRole#MEDITATION}
 * anchor compiled at a concrete world coordinate — but no NPC could ask
 * "where is the nearest meditation mat?" because the registry wasn't
 * reachable. {@link dev.ergenverse.entity.ai.CultivatorMeditationGoal}
 * meditated in place wherever it happened to be standing, instead of
 * walking to the cultivation spot.
 *
 * <p><b>The fix.</b> This singleton stores the latest compiled
 * {@link AnchorRegistry} per settlement id. {@link CanonSettlementBuilder}
 * calls {@link #register} after each {@link WorldAssembler#assemble}.
 * AI goals call {@link #findNearest} with the cultivator's settlement id,
 * the desired {@link SemanticRole}, and the cultivator's current position.
 *
 * <h2>Design choices</h2>
 * <ul>
 *   <li><b>Singleton, not a Forge service.</b> The compiler runs at chunk-
 *       load time, the AI runs at entity-tick time. Both are server-side.
 *       A simple {@code synchronized} singleton is the lightest correct
 *       bridge. No new bus, no new event, no new subscriber (Article XXVI
 *       compliance — this is a query index, not infrastructure).</li>
 *
 *   <li><b>Latest-wins per settlement.</b> Re-compiling a settlement (e.g.
 *       on chunk reload after a CanonSettlement composition change)
 *       overwrites the prior registry. The registry is immutable once
 *       published — readers see a consistent snapshot.</li>
 *
 *   <li><b>Settlement id from the cultivator's {@code getSectId()}.</b>
 *       For Wang Lin's village NPCs, {@code "faction": "wang_family_village"}
 *       in their JSON sets the sect id (see
 *       {@link dev.ergenverse.entity.EntityCultivator#initializeFromData}).
 *       The settlement id passed to {@link #register} is the
 *       {@link dev.ergenverse.runtime.PlanetSuzakuBlueprint.CanonLocation#id}
 *       — also {@code "wang_family_village"}. These match by canon
 *       convention: a settlement's faction name equals its canon location
 *       id. If they ever diverge, callers can pass the settlement id
 *       explicitly.</li>
 *
 *   <li><b>No BlockPos.</b> Coordinates are plain {@code int} triples.
 *       The AI layer converts to {@code BlockPos} only when it actually
 *       needs to pathfind — one of the three permitted places per the
 *       user's rule (chunk generator, renderer, navigation).</li>
 *
 *   <li><b>Thread safety.</b> All public methods are {@code synchronized}.
 *       The compiler runs on the chunk-load thread (deferred 1 tick via
 *       {@link dev.ergenverse.runtime.materialize.PlanetSuzakuChunkMaterializer});
 *       the AI runs on the main server tick thread. Synchronization
 *       prevents a torn read during a re-compile.</li>
 *
 *   <li><b>Empty by default.</b> Before any settlement is compiled,
 *       {@link #findNearest} returns {@code null}. AI goals must
 *       null-check and fall back to a reasonable default (e.g. meditate
 *       in place). This is documented on each goal.</li>
 * </ul>
 *
 * <h2>Canon fidelity</h2>
 * <p>Anchors are <b>semantic metadata</b> — they describe what a place IS
 * (a bed, a meditation mat, a bookshelf) without prescribing block
 * positions. The user's directive: "Suppose you redesign Wang Lin's house.
 * Old house: Bed x=4. New house: Bed x=11. Nothing breaks. AI still asks
 * BED. Compiler returns 11. Done." This service is the lookup table that
 * makes that contract real.
 *
 * <p>No canon citations needed here — this is infrastructure that holds
 * canon-compiled metadata. The canon fidelity lives in the
 * {@link dev.ergenverse.canon.structure.CanonFurniture} anchor definitions
 * (e.g. {@code MEDITATION_MAT → SemanticRole.MEDITATION}) which were
 * canon-vetted in CRON-125/127.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class AnchorRegistryService {

    private static final class Holder {
        static final AnchorRegistryService INSTANCE = new AnchorRegistryService();
    }

    /** Per-settlement registry index. Modified only under the instance lock. */
    private final Map<String, AnchorRegistry> registriesBySettlement = new HashMap<>();

    private AnchorRegistryService() {}

    /** The singleton instance. */
    public static AnchorRegistryService get() {
        return Holder.INSTANCE;
    }

    /**
     * Publish (or replace) the compiled {@link AnchorRegistry} for a
     * settlement. Called by {@link CanonSettlementBuilder} after each
     * successful {@link WorldAssembler#assemble}.
     *
     * @param settlementId the canon location id (e.g.
     *        {@code "wang_family_village"}). Must be non-null, non-blank.
     * @param registry the compiled registry. Must be non-null. A null
     *        registry is silently ignored (defensive — should never happen).
     */
    public synchronized void register(String settlementId, AnchorRegistry registry) {
        if (settlementId == null || settlementId.isBlank()) return;
        if (registry == null) return;
        registriesBySettlement.put(settlementId, registry);
    }

    /**
     * Get the compiled registry for a settlement, or {@code null} if no
     * composition has been compiled for that settlement yet.
     *
     * @param settlementId the canon location id (e.g.
     *        {@code "wang_family_village"})
     * @return the registry, or null if not yet compiled
     */
    public synchronized AnchorRegistry get(String settlementId) {
        if (settlementId == null) return null;
        return registriesBySettlement.get(settlementId);
    }

    /**
     * Find the nearest anchor with the given role within a settlement's
     * registry. Returns {@code null} if the settlement has no compiled
     * registry, OR the registry contains no anchor with that role.
     *
     * <p>"Nearest" is by squared Euclidean distance in (x, z) — y is
     * ignored because the cultivator can climb stairs/ladders to reach
     * an anchor on a different floor.
     *
     * @param settlementId the canon location id (e.g.
     *        {@code "wang_family_village"})
     * @param role the semantic role to find (e.g.
     *        {@link SemanticRole#MEDITATION})
     * @param x the query position X (typically the cultivator's block X)
     * @param y the query position Y (ignored for distance — see above)
     * @param z the query position Z
     * @return the nearest anchor, or null
     */
    public synchronized AnchorRegistry.ResolvedAnchor findNearest(
            String settlementId, SemanticRole role, int x, int y, int z) {
        AnchorRegistry registry = get(settlementId);
        if (registry == null) return null;
        return findNearestIn(registry, role, x, y, z);
    }

    /**
     * Find the nearest anchor with the given role across ALL registered
     * settlements. Useful when a cultivator has no settlement id (e.g.
     * an independent wanderer) or when the closest anchor happens to be
     * in a neighboring settlement.
     *
     * <p>Iterates all registered settlements; O(total_anchors_with_role).
     * Acceptable for play-scale volumes (a village has ~10-30 anchors).
     *
     * @param role the semantic role to find
     * @param x the query position X
     * @param y the query position Y (ignored for distance)
     * @param z the query position Z
     * @return the nearest anchor across all settlements, or null
     */
    public synchronized AnchorRegistry.ResolvedAnchor findNearestGlobal(
            SemanticRole role, int x, int y, int z) {
        AnchorRegistry.ResolvedAnchor best = null;
        long bestDistSq = Long.MAX_VALUE;
        for (AnchorRegistry registry : registriesBySettlement.values()) {
            AnchorRegistry.ResolvedAnchor candidate = findNearestIn(registry, role, x, y, z);
            if (candidate == null) continue;
            long dx = candidate.x() - x;
            long dz = candidate.z() - z;
            long distSq = dx * dx + dz * dz;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }
        return best;
    }

    /** Number of registered settlement registries (for diagnostics). */
    public synchronized int registeredSettlementCount() {
        return registriesBySettlement.size();
    }

    /** Total anchor count across all registered settlements (for diagnostics). */
    public synchronized int totalAnchorCount() {
        int total = 0;
        for (AnchorRegistry registry : registriesBySettlement.values()) {
            total += registry.size();
        }
        return total;
    }

    /** Immutable snapshot of all registered settlement ids (for diagnostics). */
    public synchronized java.util.Set<String> registeredSettlementIds() {
        return java.util.Collections.unmodifiableSet(new java.util.HashSet<>(registriesBySettlement.keySet()));
    }

    /**
     * Clear all registered registries. Used by tests and the
     * {@code /ergen debug anchors reset} command. AI goals that were
     * mid-navigation will fall back to in-place behavior on the next
     * tick — they re-query each canUse().
     */
    public synchronized void clear() {
        registriesBySettlement.clear();
    }

    // ── internals ──────────────────────────────────────────────────────

    private static AnchorRegistry.ResolvedAnchor findNearestIn(
            AnchorRegistry registry, SemanticRole role, int x, int y, int z) {
        AnchorRegistry.ResolvedAnchor best = null;
        long bestDistSq = Long.MAX_VALUE;
        for (AnchorRegistry.ResolvedAnchor candidate : registry.findByRole(role)) {
            long dx = candidate.x() - x;
            long dz = candidate.z() - z;
            long distSq = dx * dx + dz * dz;
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = candidate;
            }
        }
        return best;
    }
}
