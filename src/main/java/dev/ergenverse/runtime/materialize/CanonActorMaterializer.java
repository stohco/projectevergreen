package dev.ergenverse.runtime.materialize;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.entity.EntityCultivator;
import dev.ergenverse.entity.EREntityTypes;
import dev.ergenverse.runtime.CanonUUID;
import dev.ergenverse.runtime.NPCRuntime;
import dev.ergenverse.runtime.WorldRuntime;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * CanonActorMaterializer — the concrete actor materializer.
 *
 * <p><b>Architectural directive (CRON-69, milestone):</b> "Wang Lin materializes
 * from the blueprint at his canonical starting location." An actor is never
 * 'spawned' in the vanilla sense. The {@link NPCRuntime} holds the actor's
 * permanent simulation state (identity, canonical home). When the actor's chunk
 * is live, this materializer creates a Minecraft {@link EntityCultivator},
 * links it to the canon UUID (entity UUID = canon UUID), and places it at the
 * canonical location. When the chunk unloads (or on explicit dematerialize),
 * the entity is destroyed but the actor CONTINUES to exist in the simulation —
 * only the renderable body is gone.
 *
 * <p><b>Canon-faithful data (CRON-69 fact-check).</b> Each canon NPC carries:
 * <ul>
 *   <li>A {@code characterId} — the canon key used by the renderer to pick
 *       model/texture (e.g. {@code "wang_lin"}).</li>
 *   <li>A display name (with the correct characters, e.g. 藤厉 not 滕厉军).</li>
 *   <li>A sect/faction id (e.g. {@code "heng_yue_sect"}, {@code "teng_family"}).</li>
 *   <li>A cultivation realm at story start (Wang Lin is mortal at Ch.1).</li>
 * </ul>
 *
 * <p><b>Persistence of identity.</b> The entity's Minecraft persistence UUID is
 * set to the canon UUID before spawning. This means: if the chunk unloads and
 * reloads, the same canon UUID re-materializes as a fresh entity (the old one
 * was discarded on unload). Minecraft never confuses two canon actors because
 * their UUIDs are deterministic and unique.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class CanonActorMaterializer implements ActorMaterializer {

    /** Canon display data per canon UUID: characterId, displayName, sectId, realm. */
    private static final Map<UUID, CanonProfile> PROFILES = new HashMap<>();

    static {
        // CRON-69 canon fact-check applied:
        //  - Teng family uses 藤 (vine); the young antagonist is 藤厉 (Teng Li), not "Teng Lijun".
        //  - Li Muwan is from 洛河门 (Luo He Sect) in 火焚国, NOT Xuan Dao Sect.
        //  - Situ Nan is the 2nd-gen 朱雀子 of 朱雀国 (Suzaku Country), NOT Soul Refining Sect.
        //  - 曾大牛 (Zeng Da Niu) belongs to the 四派联盟 化凡 arc, NOT Wang Family Village.
        profile(CanonUUID.WANG_LIN,      "wang_lin",      "Wang Lin 王林",        "wang_family",   "mortal");
        profile(CanonUUID.OLD_CHEN,      "old_chen",      "Old Chen 陈老头",       "heng_yue_sect", "qi_condensation");
        profile(CanonUUID.DA_NIU,        "zeng_da_niu",   "Zeng Da Niu 曾大牛",    "four_sects_alliance", "mortal");
        profile(CanonUUID.LI_MUWAN,      "li_muwan",      "Li Muwan 李慕婉",       "luo_he_sect",   "foundation_establishment");
        profile(CanonUUID.WANG_ZHUO,     "wang_zhuo",     "Wang Zhuo 王卓",        "heng_yue_sect", "qi_condensation");
        profile(CanonUUID.TENG_HUAYUAN,  "teng_huayuan",  "Teng Huayuan 藤化元",   "teng_family",   "nascent_soul");
        profile(CanonUUID.TENG_LI,       "teng_li",       "Teng Li 藤厉",          "teng_family",   "foundation_establishment");
        profile(CanonUUID.SITU_NAN,      "situ_nan",      "Situ Nan 司徒南",       "suzaku_country","soul_formation");
        profile(CanonUUID.WANG_HAO,      "wang_hao",      "Wang Hao 王浩",         "wang_family",   "mortal");
    }

    private static void profile(UUID uuid, String characterId, String displayName, String sectId, String realm) {
        PROFILES.put(uuid, new CanonProfile(characterId, displayName, sectId, realm));
    }

    /** The bound server level (set by WorldRuntime on initialize). */
    private ServerLevel level;

    public void bind(ServerLevel level) { this.level = level; }

    @Override
    public int materializeActor(UUID canonUuid, WorldRuntime runtime) {
        if (level == null) return -1;
        NPCRuntime.ActorState state = runtime.npcs().getActor(canonUuid);
        if (state == null) return -1;

        // If an entity with this canon UUID already exists in the level, don't double-spawn.
        net.minecraft.world.entity.Entity existing = findEntityByUuid(canonUuid);
        if (existing != null && existing.isAlive()) {
            return existing.getId();
        }

        CanonProfile profile = PROFILES.get(canonUuid);
        try {
            EntityCultivator entity = new EntityCultivator(EREntityTypes.CULTIVATOR.get(), level);
            // Link the entity to the canon UUID (permanent identity, not a random entity UUID).
            entity.setUUID(canonUuid);

            // Resolve a safe surface position at the actor's canonical (x, z).
            int surfaceY = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(state.x, 0, state.z)).getY();
            entity.moveTo(state.x + 0.5, surfaceY + 1.0, state.z + 0.5, 0.0F, 0.0F);

            // Apply canon display data.
            if (profile != null) {
                entity.setCharacterId(profile.characterId);
                entity.setDisplayNameCn(profile.displayName);
                entity.setSectId(profile.sectId);
                entity.setCultivationRealm(profile.realm);
            }

            // Persist (don't despawn) — canon entities never disappear on their own.
            entity.setPersistenceRequired();

            boolean added = level.addFreshEntity(entity);
            if (!added) {
                Ergenverse.LOGGER.warn("[Ergenverse] CanonActorMaterializer: addFreshEntity rejected {} at ({},{},{}).",
                        profile == null ? canonUuid : profile.displayName, state.x, surfaceY, state.z);
                return -1;
            }
            Ergenverse.LOGGER.info("[Ergenverse] Materialized {} (canon UUID {}) at ({}, {}, {}).",
                    profile == null ? canonUuid : profile.displayName, canonUuid, state.x, surfaceY, state.z);
            return entity.getId();
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] CanonActorMaterializer failed for {}: {}",
                    profile == null ? canonUuid : profile.displayName, t.getMessage(), t);
            return -1;
        }
    }

    @Override
    public boolean dematerializeActor(UUID canonUuid, WorldRuntime runtime) {
        if (level == null) return false;
        net.minecraft.world.entity.Entity entity = findEntityByUuid(canonUuid);
        if (entity == null) return false;
        try {
            // Serialize the live position back into the simulation state.
            NPCRuntime.ActorState state = runtime.npcs().getActor(canonUuid);
            if (state != null) {
                state.x = entity.blockPosition().getX();
                state.z = entity.blockPosition().getZ();
            }
            entity.discard();
            Ergenverse.LOGGER.info("[Ergenverse] Dematerialized canon actor {}.", canonUuid);
            return true;
        } catch (Throwable t) {
            Ergenverse.LOGGER.error("[Ergenverse] Dematerialize failed for {}: {}", canonUuid, t.getMessage());
            return false;
        }
    }

    @Override
    public boolean isMaterialized(UUID canonUuid, WorldRuntime runtime) {
        if (level == null) return false;
        net.minecraft.world.entity.Entity e = findEntityByUuid(canonUuid);
        return e != null && e.isAlive();
    }

    // ── helpers ─────────────────────────────────────────────────────────

    private net.minecraft.world.entity.Entity findEntityByUuid(UUID uuid) {
        if (level == null) return null;
        // Scan loaded entities for the matching persistence UUID.
        // (1.20.1 ServerLevel has no direct getEntity(UUID); iteration is the
        // supported approach. Materialization is rare — one canon NPC per
        // chunk load — so the scan cost is negligible.)
        for (net.minecraft.world.entity.Entity e : level.getAllEntities()) {
            if (e.getUUID().equals(uuid)) return e;
        }
        return null;
    }

    private static final class CanonProfile {
        final String characterId;
        final String displayName;
        final String sectId;
        final String realm;

        CanonProfile(String characterId, String displayName, String sectId, String realm) {
            this.characterId = characterId;
            this.displayName = displayName;
            this.sectId = sectId;
            this.realm = realm;
        }
    }
}
