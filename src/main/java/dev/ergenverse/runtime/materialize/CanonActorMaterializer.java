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
        // CRON-COMPLETIONIST-107: 拓森 (Tuo Sen) — Wang Lin's Ancient God rival.
        // Canon: 8-star Ancient God (古神), rival to Wang Lin for Tu Si's
        // Ancient God inheritance. Reappears at the Suzaku Tomb during the
        // 15th-gen Suzaku Son inheritance event (CRON-106). CharacterId
        // "tuo_sen" allows the renderer to pick a distinct model/texture.
        // SectId "ancient_god_clan" reflects his origin (Tu Si's power
        // inheritance). Realm "ancient" maps to RealmId.ANCIENT (古境).
        profile(CanonUUID.TUO_SEN,       "tuo_sen",       "Tuo Sen 拓森",          "ancient_god_clan", "ancient");
        // CRON-COMPLETIONIST-110: 周茹 (Zhou Ru) — the mortal vessel for Li
        // Muwan's reincarnation. At story start she is a mortal girl living
        // in 朱雀国 (Vermilion Bird Country); the soul transfer event
        // (ZhouRuSoulTransferEvent) fires when Wang Lin right-clicks her
        // while holding the bead with Li Muwan's soul (CRON-99 prerequisite).
        // SectId "vermilion_bird_country" reflects her mortal origin in
        // 朱雀国. Realm "mortal" reflects her pre-transfer state — the
        // Soul Transformation state in npc_zhou_ru.json describes her later
        // cultivation arc under Mu Bingmei in Kunxu Realm (a future questline).
        profile(CanonUUID.ZHOU_RU,       "zhou_ru",       "Zhou Ru 周茹",          "vermilion_bird_country", "mortal");
        // CRON-COMPLETIONIST-111: 木冰眉 (Mu Bingmei, also known as 柳眉 Liu Mei)
        // — Wang Lin's third wife and Zhou Ru's cultivation master. Canon
        // (RICanonicalDatabase N19): Ascendant+ cultivation. SectId
        // "kunxu_realm" reflects her placement at the Kunxu Realm where she
        // takes Zhou Ru as her disciple. Realm "ascendant" maps to
        // RealmId.ASCENDANT (问鼎 / WenDing, corrected in CRON-119 from the wrong
        // label "合体" which is from 凡人修仙传, NOT 仙逆) — the canon-attested
        // cultivation level. The ZhouRuCultivationGrowthService advances Zhou
        // Ru's realm when she is near Mu Bingmei — modeling the disciple-master
        // cultivation.
        profile(CanonUUID.MU_BINGMEI,    "mu_bingmei",    "Mu Bingmei 木冰眉 / 柳眉", "kunxu_realm",   "ascendant");
        // CRON-COMPLETIONIST-116: 王平 (Wang Ping) — Wang Lin's biological son
        // by 木冰眉 / 柳眉 (Mu Bingmei's 9th avatar Liu Mei). Canon (web-search
        // verified 2026-07-27, Baidu Baike + Fandom wiki + newhanfu + Toutiao
        // + 163): conceived in the 朱雀墓 (Suzaku Tomb); refined into a 怨婴
        // (resentment infant) by Liu Mei; later rebuilt by Wang Lin from sword
        // qi (剑气) and lived a mortal life (~72 years) during the 二次化凡 arc.
        // At story start he is canonically a 怨婴 — flagged deadUntilRevived=true
        // in NPCRuntime, so this materializer refuses to spawn him on chunk
        // load. A future Wang Ping redemption event would clear the flag.
        // SectId "none" reflects his mortal status (no cultivation sect).
        // Realm "mortal" reflects his state — he has NO cultivation talent,
        // cannot sense spiritual qi (canon: his sword-qi body cannot cultivate).
        // CharacterId "wang_ping" allows the renderer to pick a distinct
        // model/texture (a young boy with sword-qi visual cues).
        profile(CanonUUID.WANG_PING,     "wang_ping",     "Wang Ping 王平",         "none",          "mortal");

        // CRON-COMPLETIONIST-118: 凌天侯 (Ling Tianhou) — the 剑尊 (Sword
        // Venerable), founder of 大罗剑宗 (Da Luo Sword Sect). Canon (web-search
        // verified 2026-07-27, Baidu Baike + Sohu + Zhihu + 163):
        //   - Cultivation: 净涅后期 (Quiet Nirvana Late Stage).
        //   - True identity: an avatar of 灭生老人's servant.
        //   - Gave Wang Lin two strands of sword qi for Wang Ping's redemption.
        // SectId "da_luo_sword_sect" — founder of the sect.
        // Realm "nirvana_cleanser" — the mod's closest realm to 净涅后期
        //   (note: the mod's RealmId.NIRVANA_CLEANSER maps to 净涅, which is
        //    the canon match for Ling Tianhou's 净涅后期 realm).
        // CharacterId "ling_tianhou" allows the renderer to pick a distinct
        // model/texture (a sword cultivator with a snake-shaped flying sword).
        profile(CanonUUID.LING_TIANHOU,  "ling_tianhou",  "Ling Tianhou 凌天侯",   "da_luo_sword_sect", "nirvana_cleanser");

        // CRON-COMPLETIONIST-121: 青宜 (Qing Yi, with 宜 — NOT 青衣) — Wang
        // Ping's wife on 冉云星 (Ranyun Star). Canon (web-search verified
        // 2026-07-27, Baidu Baike https://baike.baidu.com/item/青宜/637430 +
        // Sohu + QQ News + Zhihu timeline):
        //   - Female, member of the Sun family (孙家) outer-surname affiliated
        //     clan (外姓族人) on 冉云星 in the 罗天星域 north sub-region.
        //   - First appears Vol 7 Ch 693 《青宜》 (Baidu Baike-attested).
        //   - Initial cultivation: 炼气后期 (Qi Condensation Late Stage).
        //   - Deal with Wang Lin: 60 years companionship with Wang Ping in
        //     exchange for Wang Lin elevating her to 元婴后期大圆满.
        //   - Became 皇后 (empress) of 天行帝国 during Wang Ping's 10-year reign.
        //   - At Wang Ping's voluntary dispersal at age 72 (Ch 700), 青宜
        //     followed him in death (殉情而亡); soul collected into 天逆珠.
        // SectId "sun_family_ranyun" reflects her origin as a Sun family
        // outer-surname affiliated clan member on Ranyun Star.
        // Realm "qi_condensation" reflects her initial 炼气后期 cultivation
        // (RealmId.QI_CONDENSATION). The later 元婴后期大圆满 state (after
        // Wang Lin elevates her) is described in npc_qing_yi.json as her
        // post-deal cultivation arc — a future questline state.
        // CharacterId "qing_yi" allows the renderer to pick a distinct
        // model/texture (a female cultivator in green robes — 青色 — the
        // name 青宜 literally means "green-appropriate", and her green
        // robe is the visual signature).
        profile(CanonUUID.QING_YI,      "qing_yi",       "Qing Yi 青宜",          "sun_family_ranyun", "qi_condensation");

        // CRON-COMPLETIONIST-123: 天运子 (Tian Yun Zi) — the master of 天运宗
        // (Tian Yun Sect), the #1 sect on 天运星 (Tian Yun Star). Canon (web-search
        // verified 2026-07-27, Baidu Baike https://baike.baidu.com/item/天运子/23166960
        // + Sohu https://www.sohu.com/a/961903590_568249 + Zhihu
        // https://zhuanlan.zhihu.com/p/1957927329482383516 + 163.com
        // https://www.163.com/dy/article/K98V89NE0556C06B.html):
        //   - True identity: a clone (分身) of the All-Seer; his 本体 is the
        //     artifact spirit (器灵) of the Realm-Defining Compass (定界罗盘).
        //     灭生老人 sent him into the 洞府界.
        //   - Cultivation: 天人第三衰 (Heavenly Third Tribulation) — above the
        //     Three Nirvana Realms (碎涅三境), at the transition to Third Step.
        //     He needs to weather two more 天人衰劫 to break through to Third Step.
        //   - Behavior: 天运子 needs to constantly consume other cultivators
        //     (生生吞噬) to reincarnate repeatedly. His 98th awakening target is
        //     凌天侯 (Ling Tianhou) — the LingTianhouConsumptionEvent fires when
        //     the player right-clicks 天运子 after Ling Tianhou has granted the
        //     sword qi (CRON-118).
        //   - Final fate: eventually condensed into origin (本源) by 王林 in the
        //     Primordial Divine Realm — a future event.
        // SectId "tian_yun_sect" — the master of the #1 sect on 天运星.
        // Realm "nirvana_fruit" — the mod's closest realm BELOW 天人第三衰 (the
        //   mod's enum doesn't have an explicit 天人衰 realm; 天人第三衰 is
        //   canonically ABOVE 碎涅 (NIRVANA_FRUIT, order 12) and BELOW 真仙
        //   (TRUE_IMMORTAL, order 14). The mod's SPIRIT_SEIZER (夺舍, order 13)
        //   is canonically a body-snatching TECHNIQUE rather than a realm —
        //   using it here would be canon-incorrect. We pick NIRVANA_FRUIT as
        //   the closest BELOW 天人第三衰; the Javadoc in npc_tian_yun_zi.json
        //   and CanonUUID.TIAN_YUN_ZI documents the canon 天人第三衰 realm
        //   explicitly. This is a known limitation — the worklog's NEXT
        //   PRIORITY (b) calls for restructuring the Third Step to 空之四境,
        //   which would also resolve this mapping gap.
        // CharacterId "tian_yun_zi" allows the renderer to pick a distinct
        //   model/texture (an imposing elder cultivator with an aura of cosmic
        //   calculation — 天运子 is described as calculating and predatory).
        profile(CanonUUID.TIAN_YUN_ZI,  "tian_yun_zi",   "Tian Yun Zi 天运子",    "tian_yun_sect",     "nirvana_fruit");
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

        // CRON-103: canon-faithful death gate. If the actor is flagged
        // deadUntilRevived (e.g. Li Muwan before the revival event), refuse
        // to materialize. The revival event clears the flag and persists the
        // revived state via WorldDeltaStore.markActorRevived, so on world
        // reload the persisted revived-set is applied to clear the flag.
        //
        // This is the canon-faithful behavior: Li Muwan is DEAD before the
        // revival arc (she perishes when her Nascent Soul formation fails).
        // She does NOT roam Luo He Sect as a living NPC from day 0. The
        // revival event (CRON-102) is the sole mechanism that brings her
        // back as a living EntityCultivator.
        //
        // Note: if an entity with this UUID already exists in the level
        // (from a pre-CRON-103 save where she was materialized before the
        // flag existed), we DO NOT discard it here — the entity lingers
        // until natural chunk unload, then dematerializes, and subsequent
        // materialization calls correctly refuse. This is the safest
        // migration path for existing saves.
        if (state.deadUntilRevived) {
            Ergenverse.LOGGER.debug("[Ergenverse] CanonActorMaterializer: refusing to materialize {} "
                    + "(canon UUID {}) — deadUntilRevived=true. Wait for the revival event.",
                    state.name, canonUuid);
            return -1;
        }

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
