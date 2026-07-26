package dev.ergenverse.runtime.materialize;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.runtime.ChunkBounds;
import dev.ergenverse.runtime.PlanetSuzakuBlueprint;
import dev.ergenverse.spawn.HengYueSectBuilder;
import dev.ergenverse.spawn.LuoHeSectBuilder;
import dev.ergenverse.spawn.NanDouCityBuilder;
import dev.ergenverse.spawn.QilinCityBuilder;
import dev.ergenverse.spawn.SnowDomainCapitalBuilder;
import dev.ergenverse.spawn.SoulRefiningSectBuilder;
import dev.ergenverse.spawn.TengFamilyCityBuilder;
import dev.ergenverse.spawn.TianShuiCityBuilder;
import dev.ergenverse.spawn.VermilionBirdImperialCityBuilder;
import dev.ergenverse.spawn.WangFamilyVillageBuilder;
import dev.ergenverse.spawn.XuanDaoSectBuilder;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * StructureBuilderRegistry — maps a canon location id to the hand-authored
 * builder that materializes it.
 *
 * <p><b>Architectural directive (CRON-69, points 6 &amp; 7; CRON-COMPLETIONIST-62, priority c):</b>
 * the {@link ChunkMaterializer} is a stateless pure function: it asks each layer
 * "what intersects this chunk?" and the blueprint layer answers with canon
 * {@link PlanetSuzakuBlueprint.CanonLocation}s. To turn a location into blocks,
 * the materializer looks up its builder here and invokes it with the
 * {@link ChunkBounds} of the loaded chunk. The builder is responsible for
 * filtering its placements to those bounds — eliminating the prior bug where
 * every chunk load in a structure's footprint triggered a full-structure
 * rebuild that wrote blocks into unloaded neighbor chunks (cascading loads).
 *
 * <p><b>Builder interface (CRON-COMPLETIONIST-62):</b> the primary abstract
 * method is {@link Builder#buildForChunk}; the legacy {@link #build(String, ServerLevel)}
 * is preserved as a convenience default for command/login paths that want a
 * full build with no chunk filtering. A {@code null} {@link ChunkBounds}
 * signals "build everything".
 *
 * <p><b>CRON-COMPLETIONIST-72 (current state):</b> ALL 11 builders are now
 * chunk-scoped. WangFamilyVillage (CRON-62), HengYue (CRON-65),
 * TengFamily/TianShui/NanDou/SoulRefining/XuanDao (CRON-66/67), and the 4
 * formerly-ServerLevel-only builders QilinCity/SnowDomainCapital/
 * VermilionBirdImperialCity/LuoHeSect (CRON-72) all forward ChunkBounds to
 * buildForChunk and apply the sb() chunk-filter + provenance-guard helper.
 * The legacy "ignore bounds, full build" path is gone; every builder now
 * respects the chunk-materializer's incremental contract.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class StructureBuilderRegistry {

    private StructureBuilderRegistry() {}

    /**
     * A builder: given the Planet Suzaku level (and optional chunk bounds),
     * materialize the structure.
     *
     * <p><b>Chunk-scoped contract (CRON-COMPLETIONIST-62):</b> when
     * {@code bounds} is non-null, the builder MUST skip any placement whose
     * (x, z) falls outside {@link ChunkBounds#contains(int, int)}. When
     * {@code bounds} is null, the builder places ALL blocks (full build).
     *
     * <p>Builders that do not yet implement chunk-scoping can ignore
     * {@code bounds} and always do a full build — but they MUST be idempotent
     * (guard on {@code isAlreadyBuilt}) so repeated chunk-load calls are cheap.
     */
    @FunctionalInterface
    public interface Builder {
        /**
         * Materialize the structure, filtered to the given chunk bounds if non-null.
         *
         * @param level  the Planet Suzaku level
         * @param bounds optional inclusive block-coordinate rectangle; null means full build
         */
        void buildForChunk(ServerLevel level, @Nullable ChunkBounds bounds);

        /**
         * Convenience: full build (no chunk filtering). Equivalent to
         * {@code buildForChunk(level, null)}. Used by command/login paths.
         */
        default void build(ServerLevel level) {
            buildForChunk(level, null);
        }
    }

    private static final Map<String, Builder> BUILDERS = new HashMap<>();

    static {
        // Wang Family Village — fully chunk-scoped (CRON-COMPLETIONIST-62).
        // The lambda forwards the bounds to buildForChunk, which filters placements.
        register(PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.id,
                (level, bounds) -> WangFamilyVillageBuilder.buildForChunk(level, bounds));

        // CRON-COMPLETIONIST-65: Heng Yue Sect is now chunk-scoped — same pattern
        // as Wang Family Village. The registry forwards ChunkBounds to
        // buildForChunk, which filters placements to the loaded chunk and applies
        // the provenance-aware rebuild guard. The builder resolves its own center
        // from PlanetSuzakuBlueprint.HENG_YUE_SECT (fixes the prior BlockPos.ZERO
        // bug that built the sect at (0,0,0) instead of its canon coordinate).
        register(PlanetSuzakuBlueprint.HENG_YUE_SECT.id, (l, b) -> HengYueSectBuilder.buildForChunk(l, b));

        // CRON-COMPLETIONIST-66: All 5 remaining (ServerLevel, BlockPos)-taking
        // builders are now chunk-scoped — same pattern as Wang Family Village
        // and Heng Yue Sect. The registry forwards ChunkBounds to buildForChunk,
        // which filters placements to the loaded chunk and applies the provenance-
        // aware rebuild guard. Each builder resolves its own center from
        // PlanetSuzakuBlueprint (fixes the prior BlockPos.ZERO bug that built
        // structures at (0,0,0) instead of their canon coordinates).
        //
        // CRON-COMPLETIONIST-72: The 4 (ServerLevel)-only builders (QilinCity,
        // SnowDomainCapital, VermilionBirdCapital, LuoHeSect) are NOW chunk-scoped
        // too — same pattern as the other 7. Each migrated to buildForChunk with
        // the canonical sb() chunk-filter + provenance-guard helper. Each also
        // had its coordinates fixed to source from PlanetSuzakuBlueprint
        // (prior to CRON-72 they computed coords from VILLAGE_X/Z offsets,
        // placing them at wildly off-canon positions — see each builder's
        // Javadoc for the specific coordinate bug). All 11 chunk-scoped builders
        // now share the uniform architecture: blueprint → layer → journal →
        // materializer → buildForChunk → sb() → setBlock.
        register(PlanetSuzakuBlueprint.TENG_FAMILY_CITY.id, (l, b) -> TengFamilyCityBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.TIAN_SHUI_CITY.id, (l, b) -> TianShuiCityBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.QILIN_CITY.id, (l, b) -> QilinCityBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.NAN_DOU_CITY.id, (l, b) -> NanDouCityBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.SNOW_DOMAIN_CAPITAL.id, (l, b) -> SnowDomainCapitalBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.id, (l, b) -> VermilionBirdImperialCityBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.SOUL_REFINING_SECT.id, (l, b) -> SoulRefiningSectBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.XUAN_DAO_SECT.id, (l, b) -> XuanDaoSectBuilder.buildForChunk(l, b));
        register(PlanetSuzakuBlueprint.LUO_HE_SECT.id, (l, b) -> LuoHeSectBuilder.buildForChunk(l, b));
    }

    /** Register (or replace) the builder for a canon location id. */
    public static void register(String locationId, Builder builder) {
        BUILDERS.put(locationId, builder);
    }

    /**
     * Materialize a canon location on the given level — full build (no chunk
     * filtering). Idempotent: each builder guards on {@code isAlreadyBuilt}.
     * Returns true if a builder ran, false if no builder is registered.
     *
     * <p>Used by command/login paths that want a full build.
     */
    public static boolean build(String locationId, ServerLevel level) {
        return build(locationId, level, null);
    }

    /**
     * Materialize a canon location on the given level, filtered to the given
     * chunk bounds. Used by the chunk-materializer for chunk-scoped placement.
     * Returns true if a builder ran, false if no builder is registered.
     *
     * @param bounds optional inclusive block-coordinate rectangle; null means full build
     */
    public static boolean build(String locationId, ServerLevel level, @Nullable ChunkBounds bounds) {
        Builder b = BUILDERS.get(locationId);
        if (b == null) {
            Ergenverse.LOGGER.debug("[Ergenverse] No structure builder registered for '{}'.", locationId);
            return false;
        }
        try {
            b.buildForChunk(level, bounds);
            return true;
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] Structure builder for '{}' failed: {}", locationId, t.getMessage(), t);
            return false;
        }
    }
}
