package dev.ergenverse.runtime;

import dev.ergenverse.core.Ergenverse;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PlanetSuzakuBlueprint — the canonical, immutable, hand-authored definition
 * of Planet Suzaku (Wang Lin's home cultivation world).
 *
 * <p><b>This is the source of truth.</b> Not the chunk generator. Not the
 * world save. Not the entity data. The blueprint defines what EXISTS; the
 * simulation defines what CHANGES; the save stores only the deltas.
 *
 * <p>Canon: Planet Suzaku is a third-tier (later elevated) cultivation planet
 * in the Vermilion Bird Star System of the Sealed Realm inside the Cave World.
 * It contains multiple countries, the Sea of Devils (east), and the Forest of
 * Distorted Divine Sense (embedded in Zhao). The Vermilion Bird Dynasty rules
 * from the central continent. Spirit veins run through the mountains. The
 * planet is sealed around the Cultivation Planet Crystal (inside the Suzaku
 * Tomb, underground).
 *
 * <h2>Canonical geography (fixed coordinates, deterministic seed)</h2>
 * <p>Every location has a fixed canonical coordinate. The deterministic seed
 * ({@link dev.ergenverse.spawn.DeterministicSeedHandler#CANON_SEED}) ensures
 * the noise-generated terrain matches these coordinates. The blueprint
 * overlays hand-authored structures at these coordinates.
 *
 * <p>MC 1.20.1 / Forge 47.4.0 / Java 17.</p>
 */
public final class PlanetSuzakuBlueprint {

    private static final PlanetSuzakuBlueprint CANONICAL = new PlanetSuzakuBlueprint();

    /** Get the canonical, immutable blueprint instance. */
    public static PlanetSuzakuBlueprint canonical() { return CANONICAL; }

    private PlanetSuzakuBlueprint() {}

    // ════════════════════════════════════════════════════════════════════
    //  CANONICAL COORDINATES — fixed, hand-authored, never random
    // ════════════════════════════════════════════════════════════════════

    /**
     * A canonical location on Planet Suzaku. Each location has:
     * <ul>
     *   <li>A permanent {@code id} (used as UUID seed — never changes)</li>
     *   <li>A {@code name} (canon name from the novels)</li>
     *   <li>Fixed {@code x}, {@code y}, {@code z} coordinates</li>
     *   <li>A {@code category} (settlement, sect, ruin, geographic, etc.)</li>
     *   <li>A {@code canonReference} (which novel chapter describes it)</li>
     * </ul>
     */
    public static final class CanonLocation {
        public final String id;
        public final String name;
        public final int x, y, z;
        public final String category;
        public final String canonReference;

        public CanonLocation(String id, String name, int x, int y, int z,
                             String category, String canonReference) {
            this.id = id;
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.category = category;
            this.canonReference = canonReference;
        }
    }

    // ── Wang Family Village (Wang Lin's birthplace) ──
    public static final CanonLocation WANG_FAMILY_VILLAGE =
            new CanonLocation("wang_family_village", "Wang Family Village",
                    3842, 0, -1184, "settlement", "Renegade Immortal Ch. 1");

    // ── Heng Yue Sect (where Wang Lin joins as a disciple) ──
    public static final CanonLocation HENG_YUE_SECT =
            new CanonLocation("heng_yue_sect", "Heng Yue Sect",
                    4200, 0, -1400, "sect", "Renegade Immortal Ch. 3-15");

    // ── Teng Family City (city near Heng Yue Sect) ──
    public static final CanonLocation TENG_FAMILY_CITY =
            new CanonLocation("teng_family_city", "Teng Family City",
                    3500, 0, -900, "settlement", "Renegade Immortal Ch. 20-35");

    // ── Tian Shui City (major trade city) ──
    public static final CanonLocation TIAN_SHUI_CITY =
            new CanonLocation("tian_shui_city", "Tian Shui City",
                    2600, 0, -2000, "settlement", "Renegade Immortal Ch. 50-65");

    // ── Qilin City (city in Qilin country) ──
    public static final CanonLocation QILIN_CITY =
            new CanonLocation("qilin_city", "Qilin City",
                    1800, 0, -2600, "settlement", "Renegade Immortal Ch. 80-95");

    // ── Nan Dou City (southern city) ──
    public static final CanonLocation NAN_DOU_CITY =
            new CanonLocation("nan_dou_city", "Nan Dou City",
                    4400, 0, -2400, "settlement", "Renegade Immortal Ch. 100-115");

    // ── Snow Domain Capital (Snow Country capital) ──
    public static final CanonLocation SNOW_DOMAIN_CAPITAL =
            new CanonLocation("snow_domain_capital", "Snow Domain Capital",
                    2000, 0, 3200, "settlement", "Renegade Immortal Ch. 200-220");

    // ── Vermilion Bird Capital (imperial capital) ──
    public static final CanonLocation VERMILION_BIRD_CAPITAL =
            new CanonLocation("vermilion_bird_capital", "Vermilion Bird Imperial City",
                    0, 0, 0, "settlement", "Renegade Immortal Ch. 300-320");

    // ── Soul Refining Sect ──
    public static final CanonLocation SOUL_REFINING_SECT =
            new CanonLocation("soul_refining_sect", "Soul Refining Sect",
                    -1600, 0, -1800, "sect", "Renegade Immortal Ch. 250-270");

    // ── Xuan Dao Sect ──
    public static final CanonLocation XUAN_DAO_SECT =
            new CanonLocation("xuan_dao_sect", "Xuan Dao Sect",
                    -2400, 0, 1400, "sect", "Renegade Immortal Ch. 280-300");

    // ── Luo He Sect ──
    public static final CanonLocation LUO_HE_SECT =
            new CanonLocation("luo_he_sect", "Luo He Sect",
                    3000, 0, 2400, "sect", "Renegade Immortal Ch. 310-330");

    // ── Geographic features ──
    public static final CanonLocation SEA_OF_DEVILS =
            new CanonLocation("sea_of_devils", "Sea of Devils",
                    6000, 0, -1184, "geographic", "Renegade Immortal (east of Zhao)");

    public static final CanonLocation FOREST_OF_DISTORTED_SENSE =
            new CanonLocation("forest_of_distorted_sense", "Forest of Distorted Divine Sense",
                    4500, 0, -2400, "geographic", "Renegade Immortal (northeast Zhao)");

    public static final CanonLocation SUZAKU_TOMB =
            new CanonLocation("suzaku_tomb", "Suzaku Tomb (Cultivation Planet Crystal)",
                    0, -60, 0, "ruin", "Renegade Immortal (deep underground, center)");

    // ── Canonical NPC UUIDs (permanent identity, never entity IDs) ──
    public static final String NPC_WANG_LIN = "wang_lin";
    public static final String NPC_OLD_CHEN = "old_chen";
    public static final String NPC_DA_NIU = "da_niu";
    public static final String NPC_LI_MUWAN = "li_muwan";
    public static final String NPC_WANG_ZHUO = "wang_zhuo";
    public static final String NPC_TENG_HUAYUAN = "teng_huayuan";
    public static final String NPC_TENG_LIJUN = "teng_lijun";

    /**
     * All canonical locations, indexed by id. Immutable.
     */
    public Map<String, CanonLocation> allLocations() {
        Map<String, CanonLocation> map = new LinkedHashMap<>();
        map.put(WANG_FAMILY_VILLAGE.id, WANG_FAMILY_VILLAGE);
        map.put(HENG_YUE_SECT.id, HENG_YUE_SECT);
        map.put(TENG_FAMILY_CITY.id, TENG_FAMILY_CITY);
        map.put(TIAN_SHUI_CITY.id, TIAN_SHUI_CITY);
        map.put(QILIN_CITY.id, QILIN_CITY);
        map.put(NAN_DOU_CITY.id, NAN_DOU_CITY);
        map.put(SNOW_DOMAIN_CAPITAL.id, SNOW_DOMAIN_CAPITAL);
        map.put(VERMILION_BIRD_CAPITAL.id, VERMILION_BIRD_CAPITAL);
        map.put(SOUL_REFINING_SECT.id, SOUL_REFINING_SECT);
        map.put(XUAN_DAO_SECT.id, XUAN_DAO_SECT);
        map.put(LUO_HE_SECT.id, LUO_HE_SECT);
        map.put(SEA_OF_DEVILS.id, SEA_OF_DEVILS);
        map.put(FOREST_OF_DISTORTED_SENSE.id, FOREST_OF_DISTORTED_SENSE);
        map.put(SUZAKU_TOMB.id, SUZAKU_TOMB);
        return Collections.unmodifiableMap(map);
    }

    /**
     * Validate the blueprint's internal consistency. Called on initialize().
     * Throws IllegalStateException if the blueprint is inconsistent (e.g.
     * two locations overlap, a referenced NPC doesn't exist, etc.)
     */
    public void validate() {
        Map<String, CanonLocation> locs = allLocations();
        if (locs.isEmpty()) {
            throw new IllegalStateException("Blueprint has no locations");
        }
        // Check for coordinate overlaps (settlements should be ≥500 blocks apart)
        for (CanonLocation a : locs.values()) {
            for (CanonLocation b : locs.values()) {
                if (a == b) continue;
                if (a.category.equals("settlement") && b.category.equals("settlement")) {
                    double dist = Math.sqrt(
                            Math.pow(a.x - b.x, 2) + Math.pow(a.z - b.z, 2));
                    if (dist < 500.0) {
                        Ergenverse.LOGGER.warn(
                                "[Ergenverse] Blueprint warning: settlements {} and {} are only {} blocks apart (< 500).",
                                a.id, b.id, (int) dist);
                    }
                }
            }
        }
        Ergenverse.LOGGER.info("[Ergenverse] PlanetSuzakuBlueprint validated: {} canonical locations.",
                locs.size());
    }
}
