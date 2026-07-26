package dev.ergenverse.assembly;

import dev.ergenverse.canon.structure.SemanticRole;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AnchorRegistry — resolves semantic {@link dev.ergenverse.canon.structure.Anchor}s
 * to absolute world coordinates during compilation.
 *
 * <p><b>CRON-127 — WORLD ASSEMBLY COMPILER (user architectural directive)</b>
 *
 * <p>The user's vision:
 * <blockquote>
 *   AI never searches blocks. Find Wang Lin → Find House → Find Bedroom → Find
 *   Bed → Compiler Anchor → Navigation Target. […] The compiler assigns world
 *   coordinates. AI queries anchors. […] Suppose you redesign Wang Lin's house.
 *   Old house: Bed x=4. New house: Bed x=11. Nothing breaks. AI still asks BED.
 *   Compiler returns 11. Done.
 * </blockquote>
 *
 * <p>As the {@link WorldAssembler} walks the semantic tree (settlement →
 * building → room → furniture), it accumulates each object's origin and
 * registers every {@link dev.ergenverse.canon.structure.Anchor} at
 * {@code origin + anchor.offset}. The registry is then handed to AI /
 * simulation code, which can query:
 * <ul>
 *   <li>{@link #resolve(String)} — "where is the anchor named
 *       {@code wang_family_village.wang_home.wang_lin_bedroom.sleeping_mat}?"</li>
 *   <li>{@link #findByRole(SemanticRole)} — "where is the nearest BED?"</li>
 * </ul>
 *
 * <p>Coordinates are plain {@code int} triples — no {@code BlockPos}. The AI
 * layer converts to {@code BlockPos} only when it actually needs to pathfind,
 * which is one of the three permitted places (chunk generator, renderer,
 * navigation) per the user's rule.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17. No Minecraft import.</p>
 */
public final class AnchorRegistry {

    /** A resolved anchor: qualified id, role, and world coordinates. */
    public record ResolvedAnchor(String id, SemanticRole role, int x, int y, int z) {}

    private final Map<String, ResolvedAnchor> byId = new HashMap<>();
    private final Map<SemanticRole, List<ResolvedAnchor>> byRole = new HashMap<>();

    /** Registers an anchor at the given world coordinates. */
    public void register(String qualifiedId, SemanticRole role, int worldX, int worldY, int worldZ) {
        ResolvedAnchor ra = new ResolvedAnchor(qualifiedId, role, worldX, worldY, worldZ);
        byId.put(qualifiedId, ra);
        byRole.computeIfAbsent(role, r -> new ArrayList<>()).add(ra);
    }

    /** Returns the anchor with the given qualified id, or {@code null}. */
    public ResolvedAnchor resolve(String qualifiedId) {
        return byId.get(qualifiedId);
    }

    /** Returns all anchors with the given role (never {@code null}). */
    public List<ResolvedAnchor> findByRole(SemanticRole role) {
        List<ResolvedAnchor> list = byRole.get(role);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }

    /** Total number of registered anchors. */
    public int size() {
        return byId.size();
    }

    /** All registered anchors. */
    public List<ResolvedAnchor> all() {
        return List.copyOf(byId.values());
    }
}
