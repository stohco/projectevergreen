package dev.ergenverse.simulation.settlement;

import dev.ergenverse.core.Ergenverse;
import dev.ergenverse.spawn.TengFamilyCityBuilder;
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
        seedTengFamilyCity();

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
        // Wang Lin lives here with his parents (canon). He is the keystone
        // HIDDEN_CULTIVATOR whose reasoning against a wolf pack produces
        // OBSERVING_THREAT — the unique reaction the golden path demands.
        s.getResidence("wang_family_home").addResident("npc_wang_lin");

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
        s.registerPopulation("npc_wang_lin");         // canon — protagonist (HIDDEN_CULTIVATOR)
        s.registerPopulation("npc_wang_tianshui");   // canon — patriarch
        s.registerPopulation("npc_wang_qingyue");     // canon — Wang Lin's mother
        s.registerPopulation("npc_wang_zhou");        // canon — village elder
        s.registerPopulation("npc_wang_wei");         // simulation — village boy
        s.registerPopulation("npc_wang_ping");        // simulation — village child
        s.registerPopulation("npc_wang_yiyi");        // simulation — village girl
        s.registerPopulation("npc_wang_tianshan");    // simulation — extended family
        s.registerPopulation("npc_zhou_tingsu");      // simulation — married into village
        s.registerPopulation("npc_da_niu");           // simulation — village laborer

        // ── Settlement personality (the place-level lens) ──
        // Per the user's directive: "Wang Family Village: Mood Peaceful, Fear
        // Teng Family taxes, Identity Poor farming village, Prosperity Low,
        // Security Weak, Cultivation Level Mortal."
        s.personality = new SettlementPersonality(
                SettlementPersonality.Mood.PEACEFUL,
                SettlementPersonality.Identity.POOR_FARMING_VILLAGE,
                SettlementPersonality.SettlementCultivationLevel.MORTAL,
                "Teng Family taxes",
                0.20f,    // prosperity — low
                0.15f,    // security — weak
                "The village was founded generations ago by the Wang family.",
                "");     // no rumor yet

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

        // ── Settlement personality ──
        // Per the user's directive: "Heng Yue Sect: Mood Competitive, Identity
        // Cultivation sect, Politics Factional, Danger Medium, Prestige High,
        // Recruitment Active."
        s.personality = new SettlementPersonality(
                SettlementPersonality.Mood.COMPETITIVE,
                SettlementPersonality.Identity.CULTIVATION_SECT,
                SettlementPersonality.SettlementCultivationLevel.QI_CONDENSATION,
                "factional politics among the elders",
                0.55f,    // prosperity — moderate (a sect has resources)
                0.50f,    // security — medium (formation-defended)
                "Heng Yue Sect has stood for centuries on the mountain.",
                "A recruiter from a larger sect was seen on the road.");

        BY_ID.put(s.id, s);
    }

    private static PresenceLocation sectPlaza() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.4f);
        w.put(TimeOfDay.MIDDAY, 0.5f);
        w.put(TimeOfDay.AFTERNOON, 0.3f);
        return new PresenceLocation("sect_plaza", "Sect Main Plaza", 0, 0, w);
    }

    // ═══════════════════════════════════════════════════════════════════
    //  Teng Family City (滕城) — Largest city in Zhao Country
    //  Canonical center: (6800, -1000) from planet_suzaku.json.
    //  Canon NPCs: npc_teng_huayuan (Half-Step Deity Transformation patriarch),
    //              npc_teng_li (Late Foundation Establishment, arrogant young master),
    //              npc_teng_xiuxiu (conflicted family member).
    //  Simulation NPCs: npc_teng_guard_captain, npc_teng_merchant, npc_teng_servant,
    //              npc_teng_temple_priest, npc_teng_tavern_keeper, npc_teng_smuggler,
    //              npc_teng_dock_foreman, npc_teng_cultivator_guard, npc_teng_beggar.
    //
    //  Per Article XXIII: Teng City must be complete before expanding to
    //  the next settlement. Per Article XXV: NPCs must initiate gameplay.
    //  Per Article XXVII: A location is not complete until it demonstrates
    //  independent life. This seeding unblocks ActorMaterializer to
    //  materialize the 12 NPCs at their daily-rhythm positions.
    //
    //  The Teng Family City builder (TengFamilyCityBuilder, 933 lines, 12
    //  districts) places blocks at this center. This settlement gives it
    //  LIFE — population, residences, shared locations, personality.
    // ═══════════════════════════════════════════════════════════════════
    private static void seedTengFamilyCity() {
        // Blueprint coordinate from planet_suzaku.json
        int cx = 6800;
        int cz = -1000;
        Settlement s = new Settlement("teng_family_city",
                "Teng Cheng / 滕城", "city", cx, cz);
        s.primaryEconomy = "mortal_commerce_with_cultivator_taxation";
        s.governance = "patriarchal_autocracy";
        s.trend = "stable_under_repression";
        s.addSpiritVein("spirit_vein_teng_city");

        // ── Residences (NPCs own buildings) ──────────────────────────
        // Offsets are relative to (cx, cz). The TengFamilyCityBuilder places
        // the keep at approximately center + (0, -20). We anchor residences
        // to the builder's district layout.

        // Governor Keep: Teng Huayuan's seat of power. Center-north of city.
        s.addResidence(new Residence("governor_keep", s.id, "Governor's Keep",
                -6, -26, 6, -14, "npc_teng_huayuan", 0L));
        s.getResidence("governor_keep").addResident("npc_teng_servant");

        // Young Master's Quarters: Teng Li, near the keep.
        s.addResidence(new Residence("young_master_quarters", s.id, "Young Master's Quarters",
                8, -26, 18, -14, "npc_teng_li", 0L));

        // Xiuxiu's Chambers: Teng Xiuxiu, near temple district.
        s.addResidence(new Residence("xiuxiu_chambers", s.id, "Xiuxiu's Chambers",
                -30, -6, -22, 6, "npc_teng_xiuxiu", 0L));

        // Guard Captain's Post: Near the city gate (south).
        s.addResidence(new Residence("guard_captain_barracks", s.id, "Guard Captain's Barracks",
                -4, 48, 4, 56, "npc_teng_guard_captain", 0L));
        s.getResidence("guard_captain_barracks").addResident("npc_teng_cultivator_guard");

        // Merchant's Stall House: In the market district (east quadrant).
        s.addResidence(new Residence("merchant_house", s.id, "Merchant's House",
                20, -6, 30, 6, "npc_teng_merchant", 0L));

        // Dock Foreman's Office: In the warehouse district (southeast).
        s.addResidence(new Residence("dock_foreman_office", s.id, "Dock Foreman's Office",
                34, 6, 46, 14, "npc_teng_dock_foreman", 0L));

        // Tavern Keeper's Inn: In the tavern district (southwest).
        s.addResidence(new Residence("tavern_inn", s.id, "Tavern Inn",
                -22, 6, -14, 14, "npc_teng_tavern_keeper", 0L));

        // Temple Priest's Quarters: In the temple district (west).
        s.addResidence(new Residence("temple_quarters", s.id, "Temple Quarters",
                -38, -6, -30, 6, "npc_teng_temple_priest", 0L));

        // Smuggler's Hideout: Underground, in the smuggler tunnel area.
        // The smuggler has no legitimate residence — his hideout is a secret.
        s.addResidence(new Residence("smuggler_hideout", s.id, "Smuggler's Hideout",
                10, -40, 20, -34, "npc_teng_smuggler", 0L));

        // Beggar's Corner: Mortal quarter (east). No real home — sleeps in
        // an alley near the mortal quarter.
        s.addResidence(new Residence("beggar_corner", s.id, "Beggar's Corner",
                28, 6, 36, 14, "npc_teng_beggar", 0L));

        // ── Shared presence locations ─────────────────────────────────
        s.addSharedLocation(tengCityGate());
        s.addSharedLocation(tengMarketPlaza());
        s.addSharedLocation(tengMainIntersection());
        s.addSharedLocation(tengKeepApproach());
        s.addSharedLocation(tengCultivatorYard());
        s.addSharedLocation(tengTempleAltar());
        s.addSharedLocation(tengTavernCommonRoom());
        s.addSharedLocation(tengWarehouseDocks());
        s.addSharedLocation(tengMortalQuarter());

        // ── Population (Canon + Simulation NPCs) ──────────────────────
        // Canon NPCs (3)
        s.registerPopulation("npc_teng_huayuan");       // canon — Half-Step Deity Transformation patriarch
        s.registerPopulation("npc_teng_li");             // canon — arrogant young master
        s.registerPopulation("npc_teng_xiuxiu");         // canon — conflicted family member

        // Simulation NPCs (9) — fill the spaces between canon
        s.registerPopulation("npc_teng_guard_captain"); // simulation — city guard commander
        s.registerPopulation("npc_teng_cultivator_guard"); // simulation — Teng family enforcer
        s.registerPopulation("npc_teng_merchant");       // simulation — shrewd trader
        s.registerPopulation("npc_teng_servant");        // simulation — obedient servant
        s.registerPopulation("npc_teng_temple_priest");  // simulation — pious priest
        s.registerPopulation("npc_teng_tavern_keeper"); // simulation — gossipy innkeeper
        s.registerPopulation("npc_teng_smuggler");      // simulation — underground dealer
        s.registerPopulation("npc_teng_dock_foreman");  // simulation — warehouse boss
        s.registerPopulation("npc_teng_beggar");        // simulation — broken beggar

        // ── Settlement personality ───────────────────────────────────
        // Teng City under Teng Huayuan: oppressive but stable.
        // The city's mood is tense — the patriarch's power keeps order,
        // but the mortal population lives in fear. This personality
        // modulates every actor's effective courage (Article XLIV).
        s.personality = new SettlementPersonality(
                SettlementPersonality.Mood.COMPETITIVE,
                SettlementPersonality.Identity.COUNTY_SEAT,
                SettlementPersonality.SettlementCultivationLevel.FOUNDATION,
                "Teng family oppression; tribute collection; Wang family grudge",
                0.65f,    // prosperity — moderate (a city of 50,000 has commerce)
                0.70f,    // security — high (Teng Huayuan's power + guard force)
                "The Teng family has ruled Zhao Country for three generations from this city.",
                "A wandering cultivator was seen entering the smuggler tunnels.");

        BY_ID.put(s.id, s);
    }

    // ── Teng Family City shared locations (settlement-local offsets) ──

    private static PresenceLocation tengCityGate() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.3f);
        w.put(TimeOfDay.MORNING, 0.5f);
        w.put(TimeOfDay.MIDDAY, 0.4f);
        w.put(TimeOfDay.AFTERNOON, 0.35f);
        w.put(TimeOfDay.DUSK, 0.3f);
        return new PresenceLocation("city_gate", "South City Gate", 0, 52, w);
    }

    private static PresenceLocation tengMarketPlaza() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.65f);
        w.put(TimeOfDay.MIDDAY, 0.7f);
        w.put(TimeOfDay.AFTERNOON, 0.5f);
        w.put(TimeOfDay.DUSK, 0.15f);
        return new PresenceLocation("market_plaza", "Market Plaza", 24, 0, w);
    }

    private static PresenceLocation tengMainIntersection() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.4f);
        w.put(TimeOfDay.MIDDAY, 0.6f);
        w.put(TimeOfDay.AFTERNOON, 0.45f);
        w.put(TimeOfDay.DUSK, 0.3f);
        return new PresenceLocation("main_intersection", "Jade Crossroads", 0, 0, w);
    }

    private static PresenceLocation tengKeepApproach() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.2f);
        w.put(TimeOfDay.MIDDAY, 0.3f);
        w.put(TimeOfDay.AFTERNOON, 0.25f);
        return new PresenceLocation("keep_approach", "Keep Approach", 0, -30, w);
    }

    private static PresenceLocation tengCultivatorYard() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.5f);
        w.put(TimeOfDay.MORNING, 0.6f);
        w.put(TimeOfDay.AFTERNOON, 0.5f);
        return new PresenceLocation("cultivator_yard", "Cultivator Training Yard", -20, -20, w);
    }

    private static PresenceLocation tengTempleAltar() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.6f);
        w.put(TimeOfDay.DUSK, 0.4f);
        w.put(TimeOfDay.NIGHT, 0.2f);
        return new PresenceLocation("temple_altar", "Temple Altar", -34, 0, w);
    }

    private static PresenceLocation tengTavernCommonRoom() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.AFTERNOON, 0.3f);
        w.put(TimeOfDay.DUSK, 0.7f);
        w.put(TimeOfDay.EVENING, 0.8f);
        w.put(TimeOfDay.NIGHT, 0.5f);
        return new PresenceLocation("tavern_common", "Tavern Common Room", -18, 10, w);
    }

    private static PresenceLocation tengWarehouseDocks() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.DAWN, 0.3f);
        w.put(TimeOfDay.MORNING, 0.5f);
        w.put(TimeOfDay.AFTERNOON, 0.4f);
        return new PresenceLocation("warehouse_docks", "Warehouse Docks", 40, 10, w);
    }

    private static PresenceLocation tengMortalQuarter() {
        Map<TimeOfDay, Float> w = new EnumMap<>(TimeOfDay.class);
        w.put(TimeOfDay.MORNING, 0.35f);
        w.put(TimeOfDay.MIDDAY, 0.3f);
        w.put(TimeOfDay.AFTERNOON, 0.25f);
        return new PresenceLocation("mortal_quarter", "Mortal Quarter Alley", 32, 0, w);
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
