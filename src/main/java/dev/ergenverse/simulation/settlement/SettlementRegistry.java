package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.spawn.WangFamilyVillageBuilder;

import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SettlementRegistry — the central registry of all {@link Settlement}s in the
 * simulation.
 *
 * <p>This is the <b>NEW primary index</b> for "who lives where." It replaces
 * the deprecated {@code NpcSpawnRegistry} + {@code SettlementNpcAnchors}
 * spawn-driven model. Per Article XLIV: instead of "chunk loads → spawn NPC,"
 * the model is now "Settlement owns population → actors have presence →
 * renderer materializes those intersecting loaded chunks."
 *
 * <h2>Seeding</h2>
 * <p>Settlements are seeded from canon (Layer 0/1) at server start. Each
 * settlement's residences, population, and shared presence locations are
 * authored here against the canonical coordinates from the world builders
 * (e.g. {@link WangFamilyVillageBuilder}). This is Layer 1 Blueprint data —
 * fixed geography, never randomized.
 *
 * <h2>Canon vs Simulation NPCs (Article XLIV §3)</h2>
 * <p>Each settlement's population is a mix of Canon NPCs (Wang Tianshui, Wang
 * Zhou — sourced from the novels, immutable) and Simulation NPCs (procedural
 * villagers who fill the spaces between canon). The distinction is carried on
 * the Actor provenance field, not here — but this registry is where the
 * population is declared, so the seeding methods document which NPCs are
 * canon and which are simulation filler.
 */
public final class SettlementRegistry {

    private static final Map<String, Settlement> BY_ID = new LinkedHashMap<>();

    private SettlementRegistry() {}

    /**
     * Initialize all settlements. Called once on server start (tick 1),
     * after {@code NpcSpawnRegistry.initialize()} (which remains during the
     * transition but is deprecated).
     */
    public static void initialize() {
        seedWangFamilyVillage();
        seedHengYueSect();

        int totalResidences = BY_ID.values().stream().mapToInt(s -> s.getResidences().size()).sum();
        int totalPopulation = BY_ID.values().stream().mapToInt(s -> s.getPopulation().size()).sum();
        Ergenverse.LOGGER.info("[SettlementRegistry] Registered {} settlements, {} residences, {} population (Article XLIV actor-as-source-of-truth model).",
                BY_ID.size(), totalResidences, totalPopulation);
    }

    public static Settlement get(String id) { return BY_ID.get(id); }

    public static Collection<Settlement> all() { return BY_ID.values(); }

    /**
     * Find the settlement whose center is within {@code radius} blocks of the
     * given world coordinate. Used by the {@link ActorMaterializer} to decide
     * which settlements' populations to consider for materialization.
     *
     * @return the nearest settlement within range, or null.
     */
    public static Settlement settlementNear(int blockX, int blockZ, int radius) {
        long r2 = (long) radius * radius;
        for (Settlement s : BY_ID.values()) {
            long dx = blockX - s.centerX;
            long dz = blockZ - s.centerZ;
            if (dx * dx + dz * dz <= r2) return s;
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Wang Family Village (王家村) — Chapter 1 home of Wang Lin
    //  Canonical center: (3842, -1184) — from WangFamilyVillageBuilder.
    //  Canon NPCs: npc_wang_tianshui (patriarch), npc_wang_qingyue (mother),
    //              npc_wang_zhou (elder).
    //  Simulation NPCs: npc_wang_wei, npc_wang_ping, npc_wang_yiyi,
    //              npc_wang_tianshan, npc_zhou_tingsu, npc_da_niu.
    // ═══════════════════════════════════════════════════════════════════
    private static void seedWangFamilyVillage() {
        int cx = WangFamilyVillageBuilder.VILLAGE_X;
        int cz = WangFamilyVillageBuilder.VILLAGE_Z;
        Settlement s = new Settlement("wang_family_village",
                "Wang Jia Cun / 王家村", "village", cx, cz);
        s.primaryEconomy = "subsistence_farming_with_minor_spirit_herb_foraging";
        s.governance = "village_elder_council";
        s.trend = "slow_decline";
        s.addSpiritVein("spirit_vein_wang_village");

        // ── Residences (NPCs own buildings; buildings do not own NPCs) ──
        // Offsets match WangFamilyVillageBuilder + SettlementNpcAnchors.
        // Wang Family Home: 7x5 at (cx-18, cz-18). Owner: Wang Tianshui.
        s.addResidence(new Residence("wang_family_home", s.id, "Wang Family Home",
                -18, -18, -11, -13, "npc_wang_tianshui", 0L));
        s.getResidence("wang_family_home").addResident("npc_wang_qingyue");

        // Elder Home: 7x7 at (cx+10, cz-18). Owner: Wang Zhou (village elder).
        s.addResidence(new Residence("elder_home", s.id, "Elder's Home",
                10, -18, 16, -11, "npc_wang_zhou", 0L));

        // Commoner W1: 5x5 at (cx-18, cz-4). Owner: Wang Ping.
        s.addResidence(new Residence("commoner_w1", s.id, "Commoner Home W1",
                -18, -4, -13, 1, "npc_wang_ping", 0L));

        // Commoner E1: 5x5 at (cx+6, cz-4). Owner: Wang Tianshan.
        s.addResidence(new Residence("commoner_e1", s.id, "Commoner Home E1",
                6, -4, 10, 1, "npc_wang_tianshan", 0L));

        // Commoner SW2: 5x5 at (cx+2, cz+12). Owner: Zhou Tingsu.
        s.addResidence(new Residence("commoner_sw2", s.id, "Commoner Home SW2",
                2, 12, 6, 16, "npc_zhou_tingsu", 0L));

        // Commoner W2: 5x5 at (cx-18, cz+6). Owner: Wang Wei (village boy).
        s.addResidence(new Residence("commoner_w2", s.id, "Commoner Home W2",
                -18, 6, -13, 11, "npc_wang_wei", 0L));

        // Commoner NW1: 5x5 at (cx-10, cz-28). Owner: Wang Yiyi (village girl).
        s.addResidence(new Residence("commoner_nw1", s.id, "Commoner Home NW1",
                -10, -28, -5, -23, "npc_wang_yiyi", 0L));

        // Commoner SW1: 5x5 at (cx-10, cz+12). Owner: Da Niu (laborer).
        s.addResidence(new Residence("commoner_sw1", s.id, "Commoner Home SW1",
                -10, 12, -5, 17, "npc_da_niu", 0L));

        // ── Shared presence locations (communal; no single owner) ──
        s.addSharedLocation(plaza());
        s.addSharedLocation(market());
        s.addSharedLocation(herbGarden());
        s.addSharedLocation(meditationRock());
        s.addSharedLocation(northFarms());
        s.addSharedLocation(southFarms());
        s.addSharedLocation(shrine());

        // ── Population (Canon + Simulation NPCs) ──
        s.registerPopulation("npc_wang_tianshui");   // canon — patriarch
        s.registerPopulation("npc_wang_qingyue");     // canon — Wang Lin's mother
        s.registerPopulation("npc_wang_zhou");        // canon — village elder
        s.registerPopulation("npc_wang_wei");         // simulation — village boy
        s.registerPopulation("npc_wang_ping");        // simulation — village child
        s.registerPopulation("npc_wang_yiyi");        // simulation — village girl
        s.registerPopulation("npc_wang_tianshan");    // simulation — extended family
        s.registerPopulation("npc_zhou_tingsu");      // simulation — married into village
        s.registerPopulation("npc_da_niu");           // simulation — village laborer

        BY_ID.put(s.id, s);
    }

    // ── Wang Family Village shared locations (settlement-local offsets) ──

    private static PresenceLocation plaza() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.2f);
        w.put(TimeOfDay.MORNING, 0.35f);
        w.put(TimeOfDay.MIDDAY, 0.6f);
        w.put(TimeOfDay.AFTERNOON, 0.35f);
        w.put(TimeOfDay.DUSK, 0.2f);
        return new PresenceLocation("plaza", "Central Plaza", 0, 0, w);
    }

    private static PresenceLocation market() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.45f);
        w.put(TimeOfDay.MIDDAY, 0.55f);
        w.put(TimeOfDay.AFTERNOON, 0.2f);
        return new PresenceLocation("market", "Village Market", 6, 4, w);
    }

    private static PresenceLocation herbGarden() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.3f);
        w.put(TimeOfDay.AFTERNOON, 0.5f);
        w.put(TimeOfDay.DUSK, 0.2f);
        return new PresenceLocation("herb_garden", "Herb Garden", -26, -8, w);
    }

    private static PresenceLocation meditationRock() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.4f);
        w.put(TimeOfDay.AFTERNOON, 0.45f);
        w.put(TimeOfDay.NIGHT, 0.3f);
        return new PresenceLocation("meditation_rock", "Meditation Rock", -10, -22, w);
    }

    private static PresenceLocation northFarms() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.3f);
        w.put(TimeOfDay.MORNING, 0.55f);
        w.put(TimeOfDay.MIDDAY, 0.5f);
        w.put(TimeOfDay.AFTERNOON, 0.3f);
        return new PresenceLocation("north_farms", "North Farms", 0, -30, w);
    }

    private static PresenceLocation southFarms() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.4f);
        w.put(TimeOfDay.MIDDAY, 0.45f);
        w.put(TimeOfDay.AFTERNOON, 0.25f);
        return new PresenceLocation("south_farms", "South Farms", 0, 20, w);
    }

    private static PresenceLocation shrine() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.35f);
        w.put(TimeOfDay.DUSK, 0.3f);
        return new PresenceLocation("shrine", "Memorial Shrine", -2, -25, w);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Heng Yue Sect (恒岳派) — Wang Lin's first sect
    //  Canonical center: (5400, -1900).
    //  Canon NPCs: npc_qiu_siping, npc_wang_zhuo, npc_wang_hao, npc_sun_dazhu.
    //  This is a STUB — full residence layout pending HengYueSectBuilder v2.
    // ═══════════════════════════════════════════════════════════════════
    private static void seedHengYueSect() {
        Settlement s = new Settlement("heng_yue_sect",
                "Heng Yue Pai / 恒岳派", "sect", 5400, -1900);
        s.primaryEconomy = "sect_cultivation_economy";
        s.governance = "sect_elder_council";
        s.trend = "stable";
        s.addSpiritVein("spirit_vein_heng_yue");

        // Stub residences — one per known disciple, near the sect center.
        // These will be replaced with the real HengYueSectBuilder v2 layout.
        s.addResidence(new Residence("disciple_quarters_qiu", s.id, "Qiu Siping's Quarters",
                -6, 2, -2, 6, "npc_qiu_siping", 0L));
        s.addResidence(new Residence("disciple_quarters_zhuo", s.id, "Wang Zhuo's Quarters",
                -2, 2, 2, 6, "npc_wang_zhuo", 0L));
        s.addResidence(new Residence("disciple_quarters_hao", s.id, "Wang Hao's Quarters",
                2, 2, 6, 6, "npc_wang_hao", 0L));
        s.addResidence(new Residence("disciple_quarters_sun", s.id, "Sun Dazhu's Quarters",
                6, 2, 10, 6, "npc_sun_dazhu", 0L));

        // Shared sect locations.
        s.addSharedLocation(sectPlaza());
        s.addSharedLocation(swordTomb());
        s.addSharedLocation(alchemyCourtyard());
        s.addSharedLocation(library());
        s.addSharedLocation(meditationPlatform());

        s.registerPopulation("npc_qiu_siping");
        s.registerPopulation("npc_wang_zhuo");
        s.registerPopulation("npc_wang_hao");
        s.registerPopulation("npc_sun_dazhu");

        BY_ID.put(s.id, s);
    }

    private static PresenceLocation sectPlaza() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.4f);
        w.put(TimeOfDay.MIDDAY, 0.5f);
        w.put(TimeOfDay.AFTERNOON, 0.3f);
        return new PresenceLocation("sect_plaza", "Sect Main Plaza", 0, 0, w);
    }

    private static PresenceLocation swordTomb() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.AFTERNOON, 0.35f);
        w.put(TimeOfDay.NIGHT, 0.25f);
        return new PresenceLocation("sword_tomb", "Sword Tomb", -15, -10, w);
    }

    private static PresenceLocation alchemyCourtyard() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.3f);
        w.put(TimeOfDay.AFTERNOON, 0.4f);
        return new PresenceLocation("alchemy_courtyard", "Alchemy Courtyard", 12, -8, w);
    }

    private static PresenceLocation library() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.35f);
        w.put(TimeOfDay.AFTERNOON, 0.45f);
        w.put(TimeOfDay.EVENING, 0.3f);
        return new PresenceLocation("library", "Scripture Library", -12, 8, w);
    }

    private static PresenceLocation meditationPlatform() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.5f);
        w.put(TimeOfDay.NIGHT, 0.4f);
        return new PresenceLocation("meditation_platform", "Meditation Platform", 8, 12, w);
    }
}
