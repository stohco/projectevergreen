package dev.ergenverse.runtime.materialize;

import dev.ergenverse.core.Ergenverse;
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

import java.util.HashMap;
import java.util.Map;

/**
 * StructureBuilderRegistry — maps a canon location id to the hand-authored
 * builder that materializes it.
 *
 * <p><b>Architectural directive (CRON-69, points 6 &amp; 7):</b> the
 * {@link ChunkMaterializer} is a stateless pure function: it asks each layer
 * "what intersects this chunk?" and the blueprint layer answers with canon
 * {@link PlanetSuzakuBlueprint.CanonLocation}s. To turn a location into blocks,
 * the materializer looks up its builder here and invokes it (idempotent — each
 * builder's {@code isAlreadyBuilt} guards against double-placement).
 *
 * <p>Today only Wang Family Village is fully wired (the others' builder classes
 * exist but are not yet canon-accurate enough to auto-build — see the canon
 * fact-check in CRON-69). Registering them is a one-line addition per builder
 * once each is vetted. Unregistered locations are logged and skipped, so an
 * unfinished builder never crashes chunk materialization.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class StructureBuilderRegistry {

    private StructureBuilderRegistry() {}

    /** A builder: given the Planet Suzaku level, materialize the structure (idempotent). */
    @FunctionalInterface
    public interface Builder {
        void build(ServerLevel level);
    }

    private static final Map<String, Builder> BUILDERS = new HashMap<>();

    static {
        // Wang Family Village — fully wired, canon-faithful (Wang Lin's birthplace, RI Ch.1).
        register(PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.id, WangFamilyVillageBuilder::build);

        // CRON-COMPLETIONIST-83: all 11 builders now wired. Each guards on
        // isAlreadyBuilt (idempotent). The Blueprint+Layer architecture materializes
        // these on chunk load — no vanilla random terrain dependency.
        // 6 builders take (ServerLevel, BlockPos); 5 take (ServerLevel).
        // The 2-arg builders are wrapped with lambdas that discard the BlockPos
        // (the builder resolves its own center internally).
        register(PlanetSuzakuBlueprint.HENG_YUE_SECT.id, l -> HengYueSectBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.TENG_FAMILY_CITY.id, l -> TengFamilyCityBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.TIAN_SHUI_CITY.id, l -> TianShuiCityBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.QILIN_CITY.id, QilinCityBuilder::build);
        register(PlanetSuzakuBlueprint.NAN_DOU_CITY.id, l -> NanDouCityBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.SNOW_DOMAIN_CAPITAL.id, SnowDomainCapitalBuilder::build);
        register(PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.id, VermilionBirdImperialCityBuilder::build);
        register(PlanetSuzakuBlueprint.SOUL_REFINING_SECT.id, l -> SoulRefiningSectBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.XUAN_DAO_SECT.id, l -> XuanDaoSectBuilder.build(l, net.minecraft.core.BlockPos.ZERO));
        register(PlanetSuzakuBlueprint.LUO_HE_SECT.id, LuoHeSectBuilder::build);
    }

    /** Register (or replace) the builder for a canon location id. */
    public static void register(String locationId, Builder builder) {
        BUILDERS.put(locationId, builder);
    }

    /**
     * Materialize a canon location on the given level. Idempotent: each builder
     * guards on {@code isAlreadyBuilt}. Returns true if a builder ran (or was
     * already built), false if no builder is registered for the location.
     */
    public static boolean build(String locationId, ServerLevel level) {
        Builder b = BUILDERS.get(locationId);
        if (b == null) {
            Ergenverse.LOGGER.debug("[Ergenverse] No structure builder registered for '{}'.", locationId);
            return false;
        }
        try {
            b.build(level);
            return true;
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] Structure builder for '{}' failed: {}", locationId, t.getMessage(), t);
            return false;
        }
    }
}
