package dev.ergenverse.runtime;

/**
 * SettlementRuntime — structures instantiated, not generated.
 *
 * <p><b>Contract:</b> Every settlement is hand-authored. Wang Family Village
 * has a fixed layout: House #1 (Wang Lin's bedroom — chest, books, bowl, bed),
 * House #2, the village well, the ancestral hall, the spirit herb garden.
 * Every block is placed by Java code at a fixed coordinate. No structure RNG.
 * Ever.
 *
 * <p>When a settlement's chunk loads, the builder places every block from
 * the blueprint. When the chunk unloads, the blocks remain (they're in the
 * save). The settlement's NPC inhabitants are managed by NPCRuntime.
 *
 * <p>The save stores only what the simulation has CHANGED (e.g. a building
 * burned down, a new wing was added). Everything else comes from the
 * blueprint.
 */
public final class SettlementRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    SettlementRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all canonical settlements. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Wire each settlement builder to its canonical coordinate.
        // WangFamilyVillageBuilder → WANG_FAMILY_VILLAGE (3842, ?, -1184)
        // HengYueSectBuilder → HENG_YUE_SECT (4200, ?, -1400)
        // TengFamilyCityBuilder → TENG_FAMILY_CITY (3500, ?, -900)
        // TianShuiCityBuilder → TIAN_SHUI_CITY (2600, ?, -2000)
        // QilinCityBuilder → QILIN_CITY (1800, ?, -2600)
        // NanDouCityBuilder → NAN_DOU_CITY (4400, ?, -2400)
        // SnowDomainCapitalBuilder → SNOW_DOMAIN_CAPITAL (2000, ?, 3200)
        // VermilionBirdImperialCityBuilder → VERMILION_BIRD_CAPITAL (0, ?, 0)
        // SoulRefiningSectBuilder → SOUL_REFINING_SECT (-1600, ?, -1800)
        // XuanDaoSectBuilder → XUAN_DAO_SECT (-2400, ?, 1400)
        // LuoHeSectBuilder → LUO_HE_SECT (3000, ?, 2400)
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
