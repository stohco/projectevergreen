package dev.ergenverse.runtime;

import dev.ergenverse.runtime.materialize.ActorMaterializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * NPCRuntime — actors load, never spawn.
 *
 * <p><b>Contract (2026-07-25 directive):</b> Every canonical NPC has a
 * permanent UUID (see {@link CanonUUID}: wang_lin, old_chen, li_muwan, etc.).
 * When a chunk loads, the NPC is materialized via
 * {@link ActorMaterializer#materializeActor} — a Minecraft entity is created
 * and linked back to the simulation state. When the chunk unloads, the NPC
 * is dematerialized via {@link ActorMaterializer#dematerializeActor} — state
 * is serialized and the entity is destroyed. The actor CONTINUES to exist
 * in the simulation; only the renderable body is gone.
 *
 * <p>This is fundamentally different from vanilla Minecraft's spawn/despawn
 * cycle. NPCs never cease to exist. They are always simulated; they are
 * only sometimes rendered.
 *
 * <p>The NPCRuntime tracks:
 * <ul>
 *   <li>All canon NPCs by UUID (the persistent simulation state)</li>
 *   <li>Which NPCs are currently materialized (have a live Minecraft entity)</li>
 *   <li>Each NPC's canonical location (which chunk they belong to)</li>
 * </ul>
 */
public final class NPCRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    /** All canon NPCs, indexed by their permanent UUID. */
    private final Map<UUID, ActorState> actors = new HashMap<>();

    /** UUIDs of actors currently materialized (have a live Minecraft entity). */
    private final Set<UUID> materialized = new HashSet<>();

    NPCRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all canonical NPCs. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // Initialize canon NPCs at their canonical locations.
        // On day 0, each NPC starts at their canon home. After that, their
        // position is loaded from the save (deltas from the canon start).
        // CRON-69 canon fact-check applied:
        //  - Li Muwan is from 洛河门 (Luo He Sect) in 火焚国, NOT Xuan Dao Sect.
        //  - Situ Nan is the 2nd-gen 朱雀子 of 朱雀国 (Vermilion Bird / Suzaku Country),
        //    NOT Soul Refining Sect.
        //  - 曾大牛 (Zeng Da Niu) belongs to the 四派联盟 化凡 arc, NOT Wang Family Village.
        //  - The young Teng antagonist is 藤厉 (Teng Li), not "Teng Lijun".
        register(CanonUUID.WANG_LIN, "Wang Lin 王林",
                PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x,
                PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z);
        register(CanonUUID.OLD_CHEN, "Old Chen 陈老头 (mod-original)",
                PlanetSuzakuBlueprint.HENG_YUE_SECT.x,
                PlanetSuzakuBlueprint.HENG_YUE_SECT.z);
        register(CanonUUID.DA_NIU, "Zeng Da Niu 曾大牛",
                PlanetSuzakuBlueprint.FOUR_SECTS_ALLIANCE.x,
                PlanetSuzakuBlueprint.FOUR_SECTS_ALLIANCE.z);
        register(CanonUUID.LI_MUWAN, "Li Muwan 李慕婉",
                PlanetSuzakuBlueprint.LUO_HE_SECT.x,
                PlanetSuzakuBlueprint.LUO_HE_SECT.z);
        // CRON-103 canon-faithful death state: in the novel, Li Muwan perishes
        // when her Nascent Soul (元婴) formation fails — she is DEAD before
        // Wang Lin's revival arc. The mod previously registered her as a
        // living NPC at Luo He Sect from day 0, contradicting canon. She is
        // now flagged deadUntilRevived=true; CanonActorMaterializer refuses
        // to materialize her until the revival event clears the flag (and
        // persists the revived state via WorldDeltaStore.markActorRevived).
        //
        // Canon sources (web-search verified 2026-07-26, multiple sources):
        //   - "李慕婉结婴失败寿尽而亡" — Li Muwan perishes when her Nascent
        //     Soul formation fails.
        //   - "王林将李慕婉的元婴收入天逆珠" — Wang Lin captures her Nascent
        //     Soul into the Heaven-Defying Bead (CRON-99 implements this).
        //   - The revival arc spans hundreds of chapters; she is NOT alive
        //     between her death and the revival event.
        ActorState liMuwanState = actors.get(CanonUUID.LI_MUWAN);
        if (liMuwanState != null) {
            liMuwanState.deadUntilRevived = true;
        }
        register(CanonUUID.WANG_ZHUO, "Wang Zhuo 王卓",
                PlanetSuzakuBlueprint.HENG_YUE_SECT.x,
                PlanetSuzakuBlueprint.HENG_YUE_SECT.z);
        register(CanonUUID.TENG_HUAYUAN, "Teng Huayuan 藤化元",
                PlanetSuzakuBlueprint.TENG_FAMILY_CITY.x,
                PlanetSuzakuBlueprint.TENG_FAMILY_CITY.z);
        register(CanonUUID.TENG_LI, "Teng Li 藤厉",
                PlanetSuzakuBlueprint.TENG_FAMILY_CITY.x,
                PlanetSuzakuBlueprint.TENG_FAMILY_CITY.z);
        register(CanonUUID.SITU_NAN, "Situ Nan 司徒南",
                PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.x,
                PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.z);
        register(CanonUUID.WANG_HAO, "Wang Hao 王浩",
                PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.x,
                PlanetSuzakuBlueprint.WANG_FAMILY_VILLAGE.z);
        // CRON-COMPLETIONIST-107: 拓森 (Tuo Sen) — Wang Lin's Ancient God rival.
        // Canon (web-search verified 2026-07-26): reappears at the 朱雀墓 (Suzaku
        // Tomb) during the 15th-gen Suzaku Son inheritance event. An 8-star
        // Ancient God; rival to Wang Lin for Tu Si's Ancient God inheritance.
        // Until the inheritance event triggers (CultivationPlanetCrystalBlock.use,
        // CRON-106), 拓森 is canonically ABSENT from the tomb — flagged
        // deadUntilRevived=true so CanonActorMaterializer refuses to spawn him
        // on chunk load. TuoSenSpawnEvent.spawnAtSuzakuTomb clears the flag
        // and materializes him at the tomb chamber when the inheritance fires.
        register(CanonUUID.TUO_SEN, "Tuo Sen 拓森",
                PlanetSuzakuBlueprint.SUZAKU_TOMB.x,
                PlanetSuzakuBlueprint.SUZAKU_TOMB.z);
        ActorState tuoSenState = actors.get(CanonUUID.TUO_SEN);
        if (tuoSenState != null) {
            tuoSenState.deadUntilRevived = true;
        }
        // CRON-COMPLETIONIST-110: 周茹 (Zhou Ru) — the mortal vessel for Li
        // Muwan's reincarnation. Canon (web-search verified 2026-07-26,
        // Baidu Baike + Fandom wiki + RICanonicalDatabase N10):
        //   - Wang Lin places Li Muwan's captured 元婴 (Nascent Soul, CRON-99)
        //     into 周茹, who becomes the vessel for Li Muwan's reincarnation.
        //   - Li Muwan chooses NOT to devour the host soul; addresses Wang Lin
        //     as 'uncle' (王林叔叔).
        //   - Eventually 周茹 becomes a Soul Transformation cultivator under
        //     慕冰梅 (Mu Bingmei) in 昆墟之境 (Kunxu Realm).
        // Placement: Vermilion Bird Capital (朱雀国) — she's a mortal girl in
        // Zhu Que Country at story start. Mod-original concrete placement
        // until a Kunxu Realm location is added (the canon Kunxu Realm is
        // her later cultivation location, not her day-0 location).
        // NOT flagged deadUntilRevived — she's present from day 0 so the player
        // can find her; the soul transfer only fires when the player holds the
        // bead with Li Muwan's soul captured (gated by the event handler).
        register(CanonUUID.ZHOU_RU, "Zhou Ru 周茹",
                PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.x,
                PlanetSuzakuBlueprint.VERMILION_BIRD_CAPITAL.z);
        // CRON-COMPLETIONIST-111: 木冰眉 (Mu Bingmei, also known as 柳眉 Liu Mei)
        // — Wang Lin's third wife and Zhou Ru's cultivation master. Canon
        // (web-search verified 2026-07-26, RICanonicalDatabase N19 + L74):
        //   - Mu Bingmei is Liu Mei's true form (木冰眉 / 柳眉). Ascendant+
        //     cultivation; Wang Lin's third wife.
        //   - Had a son with Wang Lin (Wang Ping) whom she refined into a
        //     resentful spirit out of hatred. Wang Lin severed karmic ties
        //     with her via the Dream Dao.
        //   - She entered the Kunxu Realm (昆虚界) and took Zhou Ru as her
        //     disciple. 周茹's cultivation arc takes place here.
        // Placement: Kunxu Realm (-3500, -3500) — mod-original concrete
        // placement. The canon Kunxu Realm is a pocket-realm accessible from
        // the Cave World; this mod placement is a remote overworld region so
        // Mu Bingmei can materialize there and 周茹 can travel there for
        // cultivation. NOT flagged deadUntilRevived — she's present from day 0
        // so the ZhouRuCultivationGrowthService can find her when 周茹
        // approaches.
        register(CanonUUID.MU_BINGMEI, "Mu Bingmei 木冰眉 / 柳眉",
                PlanetSuzakuBlueprint.KUNXU_REALM.x,
                PlanetSuzakuBlueprint.KUNXU_REALM.z);
        // CRON-COMPLETIONIST-116: 王平 (Wang Ping) — Wang Lin's biological son
        // by 木冰眉 / 柳眉 (Mu Bingmei's 9th avatar Liu Mei). Canon (web-search
        // verified 2026-07-27, Baidu Baike https://baike.baidu.com/item/王平/62563845
        // + Fandom wiki + newhanfu + Toutiao + 163):
        //   - Conceived in the 朱雀墓 (Suzaku Tomb) from an accidental union.
        //     BIOLOGICAL son, NOT adopted (the animation/donghua "blood-soul"
        //     framing is 魔改 — non-canon for the novel).
        //   - Refined into a 怨婴 (resentment infant) by Liu Mei for ~100 years,
        //     leaving only a wisp of resentful soul. This is effectively his
        //     death as a normal child.
        //   - Redeemed by Wang Lin: rebuilt a body from sword qi (剑气); lived
        //     a mortal life (~72 years) with Wang Lin during the 二次化凡 arc.
        //     Became a mortal emperor (帝王), married 青宜 (Qing Yi).
        //   - At ~72, voluntarily dispersed his sword-qi body; 残魂 sealed
        //     into the 天逆珠 by Wang Lin.
        //   - First appears Vol 7 Ch 680 《柳眉的特殊法宝》 as "厉儿 (Li'er)";
        //     named 王平 in Vol 7 Ch 681. (Safe citations per Baidu Baike.)
        // Placement: Suzaku Tomb (0, 0) — his conception site. Mod-original
        // concrete placement; canon does not specify a "day-0 location" for
        // Wang Ping because he is canonically a 怨婴 (resentment infant) at
        // story start, not a roaming NPC.
        // Flagged deadUntilRevived=true — CanonActorMaterializer refuses to
        // spawn him on chunk load. A future Wang Ping redemption event
        // (parallel to Li Muwan's CRON-100/102 revival arc) would clear the
        // flag and materialize him as a mortal boy at the Suzaku Tomb.
        register(CanonUUID.WANG_PING, "Wang Ping 王平",
                PlanetSuzakuBlueprint.SUZAKU_TOMB.x,
                PlanetSuzakuBlueprint.SUZAKU_TOMB.z);
        ActorState wangPingState = actors.get(CanonUUID.WANG_PING);
        if (wangPingState != null) {
            wangPingState.deadUntilRevived = true;
        }

        // CRON-COMPLETIONIST-118: 凌天侯 (Ling Tianhou) — the 剑尊 (Sword
        // Venerable), founder of 大罗剑宗 (Da Luo Sword Sect). Canon (web-search
        // verified 2026-07-27, Baidu Baike https://baike.baidu.com/item/凌天侯/65285935
        // + Sohu https://www.sohu.com/a/935257158_122415633 + Zhihu + 163):
        //   - Cultivation: 净涅后期 (Quiet Nirvana Late Stage).
        //   - True identity: an avatar (分身) of 灭生老人's servant (仆从).
        //   - Ling Tianhou PERSONALLY gave Wang Lin TWO STRANDS OF SWORD QI
        //     (两道剑气) to rebuild Wang Ping's body (CRON-117 redemption event).
        //   - Eventually consumed by 天运子 (Tian Yun Zi); the sect declined.
        // Placement: Da Luo Sword Sect (5000, 5000) — a remote overworld region
        // representing the canon 洞府界天运星. Ling Tianhou is a LIVING NPC
        // (deadUntilRevived=false) — the player can travel here and right-click
        // him to receive the Sword Qi Strand item (CRON-118).
        // NO fabricated chapter citation.
        register(CanonUUID.LING_TIANHOU, "Ling Tianhou 凌天侯",
                PlanetSuzakuBlueprint.DA_LUO_SWORD_SECT.x,
                PlanetSuzakuBlueprint.DA_LUO_SWORD_SECT.z);
        // Ling Tianhou is alive at story start — no deadUntilRevived flag.
        // (He is consumed by 天运子 later in canon, but that's a future event.)

        loaded = true;
    }

    private void register(UUID uuid, String name, int x, int z) {
        actors.put(uuid, new ActorState(uuid, name, x, z));
    }

    /**
     * Materialize an actor — create a Minecraft entity linked to the canon UUID.
     * Called by the ChunkMaterializer when a chunk containing an actor loads.
     *
     * @return the Minecraft entity ID, or -1 on failure
     */
    public int materializeActor(UUID canonUuid, WorldRuntime runtime) {
        if (!actors.containsKey(canonUuid)) return -1;
        if (materialized.contains(canonUuid)) return -1; // already materialized
        ActorMaterializer m = runtime.actorMaterializer();
        if (m == null) return -1;
        int entityId = m.materializeActor(canonUuid, runtime);
        if (entityId >= 0) {
            materialized.add(canonUuid);
        }
        return entityId;
    }

    /**
     * Dematerialize an actor — serialize state and destroy the Minecraft entity.
     * Called when a chunk unloads. The actor's simulation state persists.
     */
    public boolean dematerializeActor(UUID canonUuid, WorldRuntime runtime) {
        if (!materialized.contains(canonUuid)) return false;
        ActorMaterializer m = runtime.actorMaterializer();
        if (m == null) return false;
        boolean ok = m.dematerializeActor(canonUuid, runtime);
        if (ok) {
            materialized.remove(canonUuid);
        }
        return ok;
    }

    /** Check if an actor is currently materialized. */
    public boolean isMaterialized(UUID canonUuid) {
        return materialized.contains(canonUuid);
    }

    /**
     * Mark an actor as alive (clear the {@code deadUntilRevived} flag).
     * Called by {@link dev.ergenverse.wanglin.bead.LiMuwanRevivalEvent}
     * when the revival event fires, and by
     * {@link dev.ergenverse.runtime.WorldRuntime#initialize} when applying
     * the persisted {@link dev.ergenverse.runtime.delta.WorldDeltaStore#revivedActorUuids()}
     * set on world load.
     *
     * <p>CRON-103: this is the in-memory companion to
     * {@link dev.ergenverse.runtime.delta.WorldDeltaStore#markActorRevived}.
     * The flag is in-memory only; the persistence channel is the revived set
     * in the delta store.
     */
    public void markActorAlive(UUID canonUuid) {
        ActorState state = actors.get(canonUuid);
        if (state != null) {
            state.deadUntilRevived = false;
        }
    }

    /** Get an actor's simulation state by canon UUID. */
    public ActorState getActor(UUID canonUuid) {
        return actors.get(canonUuid);
    }

    /** All canon NPCs (immutable view). */
    public Map<UUID, ActorState> allActors() {
        return java.util.Collections.unmodifiableMap(actors);
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }

    /**
     * ActorState — the persistent simulation state of a canon NPC.
     * This survives chunk unload/reload. Only the Minecraft entity is ephemeral.
     *
     * <p>CRON-103: the {@code deadUntilRevived} flag gates materialization.
     * For Li Muwan, it is set to {@code true} at registration (she is dead
     * before the revival arc) and cleared when the revival event fires.
     * The persistence channel is
     * {@link dev.ergenverse.runtime.delta.WorldDeltaStore#markActorRevived};
     * on world load, {@link dev.ergenverse.runtime.WorldRuntime#initialize}
     * applies the revived set to clear the flag for revived actors.
     */
    public static final class ActorState {
        public final UUID canonUuid;
        public final String name;
        public int x, z; // current position (changes as the NPC moves)
        /**
         * If true, the actor is canonically dead and CanonActorMaterializer
         * will refuse to materialize them. Cleared by the revival event
         * (CRON-103) or by the persisted revived-set on world load.
         */
        public boolean deadUntilRevived = false;
        // TODO: inventory, cultivation state, memories, relationships, schedule

        ActorState(UUID canonUuid, String name, int x, int z) {
            this.canonUuid = canonUuid;
            this.name = name;
            this.x = x;
            this.z = z;
        }
    }
}
