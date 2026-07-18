package dev.ergenverse.simulation.los;

/**
 * WorldPressureEngine — 17 canonical world-pressure types that drive
 * emergent world events and force actors to react.
 *
 * <p>A "world pressure" is an exogenous force acting on an actor or region
 * that the actor must respond to (or be crushed by). Pressures come from
 * the world itself: tribulations, inheritance crises, bloodline awakenings,
 * seals weakening, soul fluctuations, Dao clashes, reality fissures, herb
 * maturations, karmic debts, demon invasions, and so on.
 *
 * <p>Canon audit — 17 pressure types (RI-canon-specific):
 * <ol>
 *   <li>TRIBULATION_PRESSURE — heavenly tribulation incoming.</li>
 *   <li>INHERITANCE_PRESSURE — an inheritance is opening; claimants gather.</li>
 *   <li>BLOODLINE_PRESSURE — bloodline awakening / instability.</li>
 *   <li>SEAL_WEAKENING — a sealed entity's seal is weakening.</li>
 *   <li>SOUL_FLUCTUATION — soul instability (deviation risk).</li>
 *   <li>DAO_CLASH — two Daos are in conflict in the same region.</li>
 *   <li>REALITY_FISSURE — reality is cracking (formation collapse, dimensional tear).</li>
 *   <li>HERB_MATURATION — a canon spirit herb is about to mature (opportunity pressure).</li>
 *   <li>KARMIC_BACKLASH — accumulated karma is demanding resolution.</li>
 *   <li>HEART_DEMAN_PRESSURE — heart demon is surfacing.</li>
 *   <li>SECT_CRISIS — sect is under threat (war / schism / resource shortfall).</li>
 *   <li>BEAST_TIDE — beast tide incoming.</li>
 *   <li>BLOOD_OATH_DEADLINE — sworn oath deadline approaching.</li>
 *   <li>REINCARNATION_TRIGGER — past-life resonance triggering.</li>
 *   <li>RESTRICTION_DECAY — a restriction array is decaying.</li>
 *   <li>ENVIRONMENTAL_QI_SURGE — regional Qi is surging (opportunity + risk).</li>
 *   <li>WORLD_WILL_ATTENTION — the world's will has noticed the actor.</li>
 * </ol>
 */
public enum WorldPressureEngine {
    TRIBULATION_PRESSURE   ("Heavenly Tribulation Pressure"),
    INHERITANCE_PRESSURE   ("Inheritance Pressure"),
    BLOODLINE_PRESSURE     ("Bloodline Pressure"),
    SEAL_WEAKENING         ("Seal Weakening"),
    SOUL_FLUCTUATION       ("Soul Fluctuation"),
    DAO_CLASH              ("Dao Clash"),
    REALITY_FISSURE        ("Reality Fissure"),
    HERB_MATURATION        ("Spirit Herb Maturation"),
    KARMIC_BACKLASH        ("Karmic Backlash"),
    HEART_DEMON_PRESSURE   ("Heart Demon Pressure"),
    SECT_CRISIS            ("Sect Crisis"),
    BEAST_TIDE             ("Beast Tide"),
    BLOOD_OATH_DEADLINE    ("Blood-Oath Deadline"),
    REINCARNATION_TRIGGER  ("Reincarnation Trigger"),
    RESTRICTION_DECAY      ("Restriction Decay"),
    ENVIRONMENTAL_QI_SURGE ("Environmental Qi Surge"),
    WORLD_WILL_ATTENTION   ("World Will Attention");

    public final String label;

    WorldPressureEngine(String label) {
        this.label = label;
    }

    /** Canonical count — canon audit requires 17. */
    public static final int CANON_COUNT = 17;
}
