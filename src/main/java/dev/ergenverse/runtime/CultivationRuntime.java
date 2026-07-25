package dev.ergenverse.runtime;

/**
 * CultivationRuntime — realm progression. Knowledge is progression, not XP.
 *
 * <p><b>Contract (Constitution Article VII):</b> "The player does not unlock
 * recipes. The player acquires knowledge." Cultivation is the same. A realm
 * breakthrough is not a level-up; it's the consequence of accumulated
 * comprehension, refined qi, and karmic readiness.
 *
 * <p>This runtime tracks:
 * <ul>
 *   <li>Each actor's current realm (RealmId — 17 stages)</li>
 *   <li>Qi quantity and quality</li>
 *   <li>Comprehension progress toward the next realm</li>
 *   <li>Karmic bonds that help or hinder breakthrough</li>
 *   <li>Techniques known (flying sword, alchemy, formation, etc.)</li>
 * </ul>
 */
public final class CultivationRuntime {

    private final PlanetSuzakuBlueprint blueprint;
    private boolean loaded = false;

    CultivationRuntime(PlanetSuzakuBlueprint blueprint) {
        this.blueprint = blueprint;
    }

    /** Load all cultivation states. Called on WorldRuntime.initialize(). */
    void loadAll() {
        // TODO: Initialize canonical cultivation states.
        // Wang Lin: MORTAL at day 0 (canon — starts as a mortal youth).
        // Old Chen: CORE_FORMATION (canon — Heng Yue Sect elder).
        // Wang Zhuo: QI_CONDENSATION (canon — talented young cultivator).
        // Teng Huayuan: FOUNDATION (canon — Teng family patriarch).
        loaded = true;
    }

    public boolean isLoaded() { return loaded; }
    public PlanetSuzakuBlueprint blueprint() { return blueprint; }
}
