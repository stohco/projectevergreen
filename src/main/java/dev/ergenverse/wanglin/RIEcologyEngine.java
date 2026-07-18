package dev.ergenverse.wanglin;

import dev.ergenverse.core.Ergenverse;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RI Ecology Engine — Layer 3 of the Renegade Immortal World Bible.
 *
 * <p><b>Source:</b> CANON_RI_ECOLOGY.md (2,087 lines, 10 ecotopes, 6 global laws,
 * 7 cross-region threads, ~250 ecology entries).
 * Cross-referenced with:
 * <ul>
 *   <li>CANON_RI_COMPLETE_WORLD.md (3,034 lines)</li>
 *   <li>CANON_RI_COMPLETE_ITEMS.md (2,019 lines)</li>
 *   <li>CANON_RI_COMPLETE_TECHNIQUES.md (1,793 lines)</li>
 *   <li>CANON_RI_TIMELINE.md (471 lines)</li>
 *   <li>CANON_RI_CIVILIZATION.md (1,361 lines)</li>
 * </ul>
 *
 * <p><b>The central ecological truth:</b> The Cave World is an artificial Joss Flame
 * farm. Every mortal habitat exists BECAUSE it produces Joss Flames, not despite it.
 * Mortal population density correlates with spiritual fertility (spirit vein density).
 *
 * <p><b>The Cultivation Causal Chain (derivation rule):</b>
 * <pre>
 *   Qi density → spirit veins → spirit herbs → herbivore beasts →
 *   predator beasts → apex beasts → sects → ruins → mortals → Joss Flame
 * </pre>
 * Each step's confidence is at most the minimum of the steps it derives from.
 *
 * <p><b>The 6 Global Ecology Laws:</b>
 * <ol>
 *   <li>G1: Mortal Population Density Tracks Spiritual Fertility</li>
 *   <li>G2: Beast Tier Ceiling = Local Cultivation Ceiling</li>
 *   <li>G3: Spirit Veins Are The Foundation</li>
 *   <li>G4: Ruin Ecology (ruin type × Qi density × time)</li>
 *   <li>G5: Beast Evolution Pathways (6 canon-attested)</li>
 *   <li>G6: The Mortal Cycle (mortal zones are cultivation-relevant)</li>
 * </ol>
 *
 * <p><b>Per user correction #8:</b> the world-owner is the Seven-Colored Daoist,
 * NOT Allheaven. Joss Flame siphons to Seven-Colored Daoist until ~Year 1500,
 * then to Wang Lin as new owner.
 *
 * <p><b>Per Axiom 4:</b> No beast inside the Sealed Realm can exceed tier 10
 * (Heaven Blight). The IAC is uncapped.
 */
public final class RIEcologyEngine {

    private RIEcologyEngine() {}

    // ─── Enums ──────────────────────────────────────────────────────────

    /** World-law tier for a region (from CANON_RI_COMPLETE_WORLD.md L01-L80). */
    public enum WorldLawTier {
        FRAGILE("Mortal-tier / Qi Condensation ceiling. Physical laws breakable.", 2),
        LOW("Qi Condensation to Foundation Establishment.", 4),
        MEDIUM("Foundation Establishment to Nascent Soul.", 6),
        HIGH("Nascent Soul to Soul Transformation.", 8),
        MEDIUM_HIGH("Soul Transformation to Nirvana.", 9),
        ABSOLUTE("Uncapped. Grand Empyrean and beyond.", 14);

        public final String description;
        public final int beastTierCap; // max beast tier per Law G2
        WorldLawTier(String d, int cap) { this.description = d; this.beastTierCap = cap; }
    }

    /** Sealed status of a region. */
    public enum SealedStatus {
        OPEN("Not sealed. Cultivation cap = world-law tier."),
        SEALED("Inside Realm-Sealing Grand Array. Beast cap = tier 10 (Heaven Blight). Cultivation cap = quasi-Third-Step."),
        SEALED_ANCIENT("Sealed ancient entity inside. Persists until seal breaks."),
        UNCAPPED("Outside Sealed Realm. No beast or cultivation cap.");

        public final String description;
        SealedStatus(String d) { this.description = d; }
    }

    /** Cause of a ruin, per Law G4. */
    public enum RuinCause {
        MASSACRE("Clan/nation extermination → resentful-spirit ecology"),
        BATTLE("Large-scale combat → lingering combat-energy + weapon fragments"),
        AGE("Abandoned over time → moss, scavengers, decay"),
        SEAL("Sealed entity still inside → persists until seal breaks"),
        DESTRUCTION("Sect/faction destroyed → scattered artifacts, fleeing disciples");

        public final String description;
        RuinCause(String d) { this.description = d; }
    }

    // ─── Data Classes ───────────────────────────────────────────────────

    /** A canon-attested or derived ecology entry (flora, fauna, or ruin). */
    public static final class EcologyEntry {
        public final String name;
        public final String nameCn;
        public final String category;   // "flora", "fauna", "ruin", "resource"
        public final String regionId;   // "ECO-01" through "ECO-10"
        public final String derivation; // "CANON_CONCRETE", "CANON_IMPLIED", etc.
        public final int confidence;    // 5, 4, 3, 1-2, 0
        public final String description;

        public EcologyEntry(String name, String nameCn, String category, String regionId,
                           String derivation, int confidence, String description) {
            this.name = name; this.nameCn = nameCn; this.category = category;
            this.regionId = regionId; this.derivation = derivation;
            this.confidence = confidence; this.description = description;
        }
    }

    /** A spirit vein in a region. */
    public static final class SpiritVein {
        public final String name;
        public final String aspect;       // "fire", "earth", "water", "wood", "metal", "mixed", "yin"
        public final String tier;        // "minor", "mid", "supreme", "planetary_supreme"
        public final boolean canon;       // true if canon-attested

        public SpiritVein(String name, String aspect, String tier, boolean canon) {
            this.name = name; this.aspect = aspect; this.tier = tier; this.canon = canon;
        }
    }

    /** A canon-named beast with details. */
    public static final class NamedBeast {
        public final String name;
        public final String nameCn;
        public final String tier;        // "Nirvana Scryer+", "Ascendant", "Foundation Establishment", etc.
        public final String originRegion;
        public final String note;
        public final boolean isEvolved;  // evolved via Brilliant Golden Fruit or other pathway
        public final String evolutionMethod; // null if not evolved

        public NamedBeast(String name, String nameCn, String tier, String originRegion,
                         String note, boolean isEvolved, String evolutionMethod) {
            this.name = name; this.nameCn = nameCn; this.tier = tier;
            this.originRegion = originRegion; this.note = note;
            this.isEvolved = isEvolved; this.evolutionMethod = evolutionMethod;
        }
    }

    /** A heritage site the player can explore. */
    public static final class HeritageSite {
        public final String name;
        public final String location;
        public final String reward;       // what the player finds
        public final String tier;         // danger tier
        public final String canonSource;   // CANON_RI_COMPLETE_WORLD.md line reference

        public HeritageSite(String name, String location, String reward, String tier, String canonSource) {
            this.name = name; this.location = location; this.reward = reward;
            this.tier = tier; this.canonSource = canonSource;
        }
    }

    /** A cross-region ecology thread. */
    public static final class EcologyThread {
        public final String id;
        public final String name;
        public final String description;
        public final int confidence;
        public final List<String> involvedRegions;
        public final List<String> involvedFactions;

        public EcologyThread(String id, String name, String description, int confidence,
                            List<String> involvedRegions, List<String> involvedFactions) {
            this.id = id; this.name = name; this.description = description;
            this.confidence = confidence; this.involvedRegions = involvedRegions;
            this.involvedFactions = involvedFactions;
        }
    }

    /** A canon-silent entity that is FORBIDDEN. */
    public static final class CanonSilentFlag {
        public final String forbiddenName;
        public final String canonReplacement;
        public final String reason;

        public CanonSilentFlag(String forbiddenName, String canonReplacement, String reason) {
            this.forbiddenName = forbiddenName; this.canonReplacement = canonReplacement;
            this.reason = reason;
        }
    }

    /** Ruin decay timeline entry. */
    public static final class RuinDecayEntry {
        public final String ruinTier;
        public final int decayYearsMin;
        public final int decayYearsMax;
        public final String decayTo;

        public RuinDecayEntry(String ruinTier, int min, int max, String decayTo) {
            this.ruinTier = ruinTier; this.decayYearsMin = min;
            this.decayYearsMax = max; this.decayTo = decayTo;
        }
    }

    // ─── The 5 Founding Axioms ────────────────────────────────────────────

    public static final class EcologyAxiom {
        public final String id;
        public final String title;
        public final String statement;
        public final int confidence;

        EcologyAxiom(String id, String title, String statement, int confidence) {
            this.id = id; this.title = title; this.statement = statement;
            this.confidence = confidence;
        }
    }

    public static final List<EcologyAxiom> FOUNDING_AXIOMS = Arrays.asList(
        new EcologyAxiom("E1", "The Joss Flame Farm",
            "The Cave World is an artificial farm created by the Seven-Colored Daoist to harvest Joss Flames "
            + "— cultivation energy from mortal faith. Every mortal habitat exists BECAUSE it produces "
            + "Joss Flames, not despite it. The Seven-Colored Daoist siphons 30% via the seal.", 5),
        new EcologyAxiom("E2", "The Prime Directive",
            "Reality is objective. Cultivation changes a cultivator's understanding of the world, "
            + "not the world itself. The world exists independently of the player. A Qi Condensation "
            + "rat in Zhao Country has stats, behavior, a nest, and predators — whether or not "
            + "the player ever perceives it.", 5),
        new EcologyAxiom("E3", "The Cultivation Causal Chain",
            "Qi density → spirit veins → spirit herbs → herbivore beasts → predator beasts → "
            + "apex beasts → sects → ruins → mortals → Joss Flame. Each step's confidence is at most "
            + "the minimum of the steps it derives from.", 5),
        new EcologyAxiom("E4", "The Sealed Hierarchy Caps Ecology",
            "The Realm-Sealing Grand Array caps cultivation at Heaven Blight (tier 10) inside the "
            + "Sealed Realm. No beast inside the Sealed Realm can naturally exceed tier 10. "
            + "The IAC is outside the Sealed Realm and has no such cap.", 5),
        new EcologyAxiom("E5", "Beast Evolution Is Canon-Mechanical",
            "Beast evolution is a canon mechanic, not speculation. Attested pathways: "
            + "Brilliant Golden Fruit, dragon blood, Earth Fire Dragon soul extraction, "
            + "Green Devil devouring.", 5)
    );

    // ─── The 6 Global Ecology Laws ────────────────────────────────────

    public static final class GlobalLaw {
        public final String id;
        public final String title;
        public final String statement;
        public final int confidence;

        GlobalLaw(String id, String title, String statement, int confidence) {
            this.id = id; this.title = title; this.statement = statement;
            this.confidence = confidence;
        }
    }

    public static final List<GlobalLaw> GLOBAL_LAWS = Arrays.asList(
        new GlobalLaw("G1", "Mortal Population Density Tracks Spiritual Fertility",
            "Mortal population density in any region correlates (non-linearly) with that region's "
            + "spirit-vein density. Dense Qi = dense agriculture = dense mortals = more Joss Flame.", 3),
        new GlobalLaw("G2", "Beast Tier Ceiling = Local Cultivation Ceiling",
            "No naturally-occurring beast inside the Sealed Realm can exceed tier 10 (Heaven Blight). "
            + "Beasts at tier 11+ are sealed ancient entities or spoilers. IAC is uncapped.", 5),
        new GlobalLaw("G3", "Spirit Veins Are The Foundation",
            "Every region's ecology is anchored on its spirit veins. Veins determine: "
            + "(a) herb density, (b) beast tier ceiling, (c) sect presence, "
            + "(d) mortal population (via G1).", 5),
        new GlobalLaw("G4", "Ruin Ecology",
            "A ruin's ecology is determined by: (a) the original inhabitants, "
            + "(b) the cause of ruin (massacre → undead; age → scavengers; seal → sealed entity persists), "
            + "(c) the local Qi density. Ruins decay over time per the decay timeline.", 3),
        new GlobalLaw("G5", "Beast Evolution Pathways",
            "Canon-attested evolution mechanics: Brilliant Golden Fruit, dragon blood, "
            + "Earth Fire Dragon soul extraction, Green Devil devouring, "
            + "Restriction Mountain enlightenment, Soul Refining heritage.", 5),
        new GlobalLaw("G6", "The Mortal Cycle",
            "Wang Lin repeatedly descends to mortal life for breakthroughs. Mortal zones "
            + "are cultivation-relevant — they are Joss Flame sources AND trial sites. "
            + "Canon-attested: Life-Death Domain, Cloud Sea doctor village, Wang Ping village.", 5)
    );

    // ─── Beast Evolution Pathways (Law G5, Type A) ──────────────────

    public static final class EvolutionPathway {
        public final String name;
        public final String input;
        public final String output;
        public final String canonSource;

        EvolutionPathway(String name, String input, String output, String canonSource) {
            this.name = name; this.input = input; this.output = output;
            this.canonSource = canonSource;
        }
    }

    public static final List<EvolutionPathway> EVOLUTION_PATHWAYS = Arrays.asList(
        new EvolutionPathway("Brilliant Golden Fruit", "Mosquito Beast, Thunder Toad",
            "Evolved form", "N155, N156, L18"),
        new EvolutionPathway("Dragon Blood", "Beast (unspecified)",
            "Empowered form", "N52, L78 (Falling Land)"),
        new EvolutionPathway("Earth Fire Dragon Soul", "Cultivator (Wang Lin)",
            "Soul-extracted power", "L70, N123"),
        new EvolutionPathway("Green Devil Devouring", "Cultivator (Wang Lin)",
            "Arcane Void breakthrough", "N132"),
        new EvolutionPathway("Restriction Mountain Enlightenment", "Cultivator",
            "'Great Enlightened One' title", "L49"),
        new EvolutionPathway("Soul Refining Heritage", "Mountain Valley Tribe natives",
            "Soul Refining Tribe (1M+ people)", "L43, L44, F26")
    );

    // ─── Ruin Decay Timeline (Thread T6, Type C, conf 3) ──────────────

    public static final List<RuinDecayEntry> RUIN_DECAY_TIMELINE = Arrays.asList(
        new RuinDecayEntry("Qi Condensation", 10, 50, "Scavenger ecology"),
        new RuinDecayEntry("Core Formation", 50, 200, "Scavenger ecology"),
        new RuinDecayEntry("Nascent Soul", 200, 500, "Low-spirit-herb ecology"),
        new RuinDecayEntry("Soul Formation", 500, 2000, "Residual Qi wisps"),
        new RuinDecayEntry("Soul Transformation", 2000, 5000, "Faint spiritual remnants"),
        new RuinDecayEntry("Nirvana", 5000, 99999, "Effectively permanent"),
        new RuinDecayEntry("Third Step", 99999, 99999, "Permanent")
    );

    // ─── Named Beasts (cross-region roster, Type A) ───────────────────

    public static final List<NamedBeast> NAMED_BEASTS = Collections.unmodifiableList(Arrays.asList(
        new NamedBeast("Mosquito Beast", "蚊兽", "Nirvana Scryer+", "Sea of Devils",
            "Evolved with Brilliant Golden Fruit; herd in Wind Celestial Realm; saves Wang Lin in IAC void.",
            true, "Brilliant Golden Fruit"),
        new NamedBeast("Thunder Toad", "雷蟾", "Evolved", "Sea of Devils",
            "Fed Brilliant Golden Fruit → evolved. Sacrificed for Bloodline Thunder.",
            true, "Brilliant Golden Fruit"),
        new NamedBeast("Thunder Celestial Beast", "雷仙兽", "Ascendant", "Unknown",
            "Ascendant-tier combat mount.", false, null),
        new NamedBeast("Nether Beast", "冥兽", "Unknown", "Unknown",
            "Life-bound; vast interior sub-dimension.", false, null),
        new NamedBeast("Brilliant Void", "碧落", "Unknown", "Brilliant Void",
            "Wang Lin's companion beast, named for the region.", false, null),
        new NamedBeast("Golden Exalt Sea Dragon", "金翅海龙", "Void Tribulant+", "Unknown",
            "Wang Lin's mount.", false, null),
        new NamedBeast("Xiao Bai", "小白", "Low", "Cloud Sea",
            "Zhou Ru's pet tiger.", false, null),
        new NamedBeast("Lei Ji", "雷记", "Beast mount", "Corpse Yin Sect",
            "Wang Lin's mount from Corpse Yin Sect.", false, null),
        new NamedBeast("Wind Demon", "风魔", "Third Step", "Immortal Monarch's Cave Mansion (Tian Yun)",
            "Sealed demon; slain by Wang Lin.", false, null),
        new NamedBeast("Green Devil Scorpion", "Green Devil", "Peak Third Step+", "Green Devil Continent (IAC)",
            "Apex predator of Green Devil Continent. Wang Lin devours it.", false, null),
        new NamedBeast("Earth Fire Dragon", "地火龙", "Soul Transformation+", "Tianniu area (IAC)",
            "Wang Lin extracts its soul for power.", false, null)
    ));

    // ─── Heritage Sites (15 canon-attested) ──────────────────────────

    public static final List<HeritageSite> HERITAGE_SITES = Collections.unmodifiableList(Arrays.asList(
        new HeritageSite("Heng Yue Sect cliff + dead-bird bead location",
            "Zhao Country", "Heaven-Defying Bead", "Fragile", "L11, E11"),
        new HeritageSite("Ancient Battlefield Level 3",
            "Planet Suzaku (pocket-realm)", "Ancient God Tactic / Tu Si's knowledge inheritance",
            "High", "L49"),
        new HeritageSite("Restriction Mountain",
            "Ancient Battlefield Level 2", "'Great Enlightened One' title",
            "High", "L49"),
        new HeritageSite("Suzaku Tomb (17 layers)",
            "Planet Suzaku", "Cultivation Planet Crystal + Half-Moon Blade",
            "High", "L48"),
        new HeritageSite("Immortal Graveyard 17th Layer",
            "Vermilion Bird Starfield", "Fu Clan's Golden Leaf Flame Source Origin",
            "High", "L47"),
        new HeritageSite("Falling Land",
            "Planet Suzaku", "Trial of Man, Trial of Heaven, Void Flame Cultivator",
            "High", "L78"),
        new HeritageSite("Immortal Monarch's Cave Mansion",
            "Planet Tian Yun", "Wind Demon slaying heritage",
            "High", "L64"),
        new HeritageSite("Thunder Celestial Temple 3 Trials",
            "Allheaven Star System", "Thunder Celestial title",
            "High", "L66"),
        new HeritageSite("Rain Celestial Realm",
            "Allheaven Star System", "Rain Immortal Sword, War Spirit Print, Wending Crystal",
            "High", "L68"),
        new HeritageSite("Wind Celestial Realm stone gate",
            "Cloud Sea Star System", "Flowing Moon technique",
            "High", "L69"),
        new HeritageSite("Wild Continent",
            "Cloud Sea Star System", "Secret for the Third Step",
            "High", "L73"),
        new HeritageSite("Ancient Tomb",
            "IAC", "Yi Si Puppet, Heaven-Splitting Axe, Eternal Wood Spirit, Fog Devil Lance, "
            + "Ancient Breath Leaves (99), Emperor Furnace, Ye Mo's eye+arm",
            "Absolute", "L61"),
        new HeritageSite("Dong Lin Pool",
            "IAC", "Reincarnation Essence comprehension",
            "Absolute", "L50"),
        new HeritageSite("Ancient Clan Ancestral Temple",
            "IAC", "Lou Hou's soul + Soul Blood (2 drops)",
            "Absolute", "L63"),
        new HeritageSite("Ancient Immortal Domain gate",
            "Cave World boundary", "Flowing Moon divine ability",
            "High", "L72")
    ));

    // ─── Cross-Region Ecology Threads (7 threads) ─────────────────────

    public static final List<EcologyThread> CROSS_REGION_THREADS = Collections.unmodifiableList(Arrays.asList(
        new EcologyThread("T1", "Brilliant Golden Fruit Beast-Evolution Pathway",
            "Earth Planet → Brilliant Golden Fruit → Mosquito Beast (evolved) → Mosquito herd in Wind "
            + "Celestial Realm. Thunder Toad (evolved) → sacrificed for Bloodline Thunder.",
            5,
            Arrays.asList("ECO-09", "ECO-04"),
            Arrays.asList()),
        new EcologyThread("T2", "Soul-Refining Heritage Path",
            "Soul Refining Sect (Pilu Kingdom) → Wang Lin inherits → teaches Mountain Valley Tribe → "
            + "Soul Refining Tribe (1M+ people, East Demon Spirit Sea, Planet Tian Yun) → Thirteen.",
            5,
            Arrays.asList("ECO-01", "ECO-05"),
            Arrays.asList("CIV-02", "CIV-14")),
        new EcologyThread("T3", "Reincarnation Destination Path",
            "Cave World allies → Immortal Execution Star → Wu Xuan Country → "
            + "Situ Nan reincarnates as Grand Marshal Si Nan.",
            5,
            Arrays.asList("ECO-09"),
            Arrays.asList("CIV-brief-26")),
        new EcologyThread("T4", "Heaven-Defying Pearl Cross-Novel Path",
            "Seven-Colored Daoist → Realm-Sealing Supreme → Wang Lin obtains pearl "
            + "(inside dead bird, Zhao Country cliff) → transcends. Cross-novel: "
            + "Su Ming, Xuan Zang, Paragon Wang (AWWP).",
            5,
            Arrays.asList("ECO-01"),
            Arrays.asList()),
        new EcologyThread("T5", "Joss Flame Flow",
            "All Cave World mortals → Joss Flame → 30% siphoned by seal → "
            + "Seven-Colored Daoist (until ~Year 1500) → Wang Lin (post-Year 1500). "
            + "Wang Lin dissolves the seal.",
            5,
            Arrays.asList("ECO-01", "ECO-02", "ECO-03", "ECO-04", "ECO-05", "ECO-09"),
            Arrays.asList("CIV-18")),
        new EcologyThread("T6", "Ruin-Time Decay",
            "Ruins decay over time based on: original tier, cause of ruin, local Qi density. "
            + "Qi Condensation ruins: 10-50y. Third-Step ruins: permanent.",
            3,
            Arrays.asList(),
            Arrays.asList()),
        new EcologyThread("T7", "Mortal Cycle",
            "Wang Lin's repeated descents to mortal life for breakthroughs. "
            + "Canon-attested: Life-Death Domain village, Cloud Sea doctor village, "
            + "Wang Ping village (Planet Ran Yun), Wang Family Village.",
            5,
            Arrays.asList("ECO-01", "ECO-05", "ECO-08", "ECO-09"),
            Arrays.asList())
    ));

    // ─── Canon-Silent Flags (FORBIDDEN) ────────────────────────────────

    public static final List<CanonSilentFlag> CANON_SILENT_FLAGS = Collections.unmodifiableList(Arrays.asList(
        new CanonSilentFlag("Ten Thousand Demons Sect", "Sky Demon Country + Ancient Demon City",
            "Not attested in canon."),
        new CanonSilentFlag("Xue Yue", "Snow Domain Country (雪域国)",
            "Not attested in canon as named entity."),
        new CanonSilentFlag("Heavenly Demon City", "Ancient Demon City (古魔城)",
            "Not attested in canon."),
        new CanonSilentFlag("Suzaku Continent", "Planet Suzaku (朱雀星) — a planet, not a continent",
            "Planet Suzaku does not have continents. The IAC has continents."),
        new CanonSilentFlag("Heavenly Fate Continent", "Planet Tian Yun (天运星) — a planet, not a continent",
            "Planet Tian Yun does not have continents."),
        new CanonSilentFlag("Ancient Emperor's inheritance",
            "Immortal Monarch's Cave Mansion (L64), Ancient Tomb (L61), Heavenly Emperor Furnace",
            "'Ancient Emperor's inheritance' as a single concept is not canon.")
    ));

    // ─── Region Ecotopes (10 regions) ────────────────────────────────

    /** A regional ecotope — the core data structure for the ecology layer. */
    public static final class RegionEcotope {
        public final String id;
        public final String name;
        public final String nameCn;
        public final WorldLawTier worldLaw;
        public final SealedStatus sealed;
        public final String cosmologicalPosition;
        public final int confidence;
        public final List<SpiritVein> spiritVeins;
        public final String floraCanon;      // canon-named flora (semicolon-separated)
        public final String floraDerived;   // derived flora (semicolon-separated)
        public final String faunaCanon;      // canon-named fauna
        public final String faunaDerived;   // derived fauna
        public final String mortalPopulation;
        public final String sectPresence;
        public final String ruinEcology;
        public final String jossFlameNote;
        public final String playerEncounter;

        private RegionEcotope(String id, String name, String nameCn, WorldLawTier worldLaw,
                            SealedStatus sealed, String cosmologicalPosition, int confidence,
                            List<SpiritVein> spiritVeins,
                            String floraCanon, String floraDerived, String faunaCanon, String faunaDerived,
                            String mortalPopulation, String sectPresence, String ruinEcology,
                            String jossFlameNote, String playerEncounter) {
            this.id = id; this.name = name; this.nameCn = nameCn;
            this.worldLaw = worldLaw; this.sealed = sealed;
            this.cosmologicalPosition = cosmologicalPosition; this.confidence = confidence;
            this.spiritVeins = spiritVeins;
            this.floraCanon = floraCanon; this.floraDerived = floraDerived;
            this.faunaCanon = faunaCanon; this.faunaDerived = faunaDerived;
            this.mortalPopulation = mortalPopulation; this.sectPresence = sectPresence;
            this.ruinEcology = ruinEcology; this.jossFlameNote = jossFlameNote;
            this.playerEncounter = playerEncounter;
        }
    }

    /** Builder for RegionEcotope (keeps the constructor private). */
    public static final class EcotopeBuilder {
        private String id, name, nameCn, cosmologicalPosition, floraCanon, floraDerived;
        private String faunaCanon, faunaDerived, mortalPopulation, sectPresence, ruinEcology;
        private String jossFlameNote, playerEncounter;
        private WorldLawTier worldLaw = WorldLawTier.LOW;
        private SealedStatus sealed = SealedStatus.OPEN;
        private int confidence = 5;
        private List<SpiritVein> spiritVeins = new ArrayList<>();

        public EcotopeBuilder(String id, String name, String nameCn, String cosmologicalPosition) {
            this.id = id; this.name = name; this.nameCn = nameCn;
            this.cosmologicalPosition = cosmologicalPosition;
        }
        public EcotopeBuilder worldLaw(WorldLawTier w) { this.worldLaw = w; return this; }
        public EcotopeBuilder sealed(SealedStatus s) { this.sealed = s; return this; }
        public EcotopeBuilder confidence(int c) { this.confidence = c; return this; }
        public EcotopeBuilder vein(String name, String aspect, String tier, boolean canon) {
            this.spiritVeins.add(new SpiritVein(name, aspect, tier, canon)); return this; }
        public EcotopeBuilder floraCanon(String f) { this.floraCanon = f; return this; }
        public EcotopeBuilder floraDerived(String f) { this.floraDerived = f; return this; }
        public EcotopeBuilder faunaCanon(String f) { this.faunaCanon = f; return this; }
        public EcotopeBuilder faunaDerived(String f) { this.faunaDerived = f; return this; }
        public EcotopeBuilder mortals(String m) { this.mortalPopulation = m; return this; }
        public EcotopeBuilder sects(String s) { this.sectPresence = s; return this; }
        public EcotopeBuilder ruins(String r) { this.ruinEcology = r; return this; }
        public EcotopeBuilder jossFlame(String j) { this.jossFlameNote = j; return this; }
        public EcotopeBuilder player(String p) { this.playerEncounter = p; return this; }
        public RegionEcotope build() {
            return new RegionEcotope(id, name, nameCn, worldLaw, sealed, cosmologicalPosition,
                confidence, Collections.unmodifiableList(spiritVeins),
                floraCanon, floraDerived, faunaCanon, faunaDerived,
                mortalPopulation, sectPresence, ruinEcology, jossFlameNote, playerEncounter);
        }
    }

    public static final List<RegionEcotope> ALL_REGIONS = Collections.unmodifiableList(Arrays.asList(

        // ECO-01: Zhao Country
        new EcotopeBuilder("ECO-01", "Zhao Country", "赵国",
            "Third-tier cultivation country on Planet Suzaku, inside Sealed Realm, inside Cave World")
            .worldLaw(WorldLawTier.FRAGILE).confidence(5)
            .vein("Heng Yue minor vein", "sword", "minor", false)
            .vein("Xuan Dao Sect vein", "unknown", "minor", true)
            .vein("~8-15 minor veins total (derived)", "mixed", "minor", false)
            .floraDerived("Qi-Gathering Grass; Foundation-Root Vine; Sword-Edge Moss")
            .faunaDerived("Iron-Feathered Hawk (QC tier); Stone-Backed Boar (QC herbivore); "
                + "Cloud-Walk Rabbit (non-spirit); Teng-Clan War Hound (mortal)")
            .faunaCanon("Dead bird containing Heaven-Defying Bead (Timeline E11); "
                + "Dragon Statue in Forest of Distorted Divine Sense (contains Wu Yu Nascent Soul)")
            .mortals("5-15 million (Type C)")
            .sects("Heng Yue Sect, Xuan Dao Sect, Teng Clan (DESTROYED), Wang Clan (EXTERMINATED then restored)")
            .ruins("Wang Family Village ruin (post-massacre, resentful-spirit ecology, ~10-50y to decay); "
                + "Teng Family City ruin (post-extermination, scavenger ecology, ~50-200y to decay)")
            .jossFlame("Low per-capita (fragile tier), but consistent across 100-family villages")
            .player("Starting zone. Low-tier herbs and beasts. Heng Yue Sect recruitment. "
                + "Wang Family Village massacre site. Teng Family City ruins. "
                + "Forest of Distorted Divine Sense (high-tier zone anomaly).")
            .build(),

        // ECO-02: Planet Suzaku
        new EcotopeBuilder("ECO-02", "Planet Suzaku", "朱雀星",
            "Rank-7 cultivation planet, inside Sealed Realm")
            .worldLaw(WorldLawTier.LOW).confidence(5)
            .vein("Cultivation Planet Crystal (planet's seal-mechanism)", "earth", "supreme", true)
            .floraCanon("None explicitly named for the planet as a whole")
            .floraDerived("Planet-tier herbs at higher-tier regions (Vermilion Bird Country, Pilu Kingdom)")
            .faunaCanon("Mosquito Beast (origin, pre-evolution)")
            .faunaDerived("Planet-wide beast population derived from 1-5B mortals + multiple spirit veins")
            .mortals("1-5 billion across 11 countries (Type C)")
            .sects("Vermilion Bird Country (sovereign), 11 country-level governments, "
                + "Heng Yue/Xuan Dao/Teng Clans, Soul Refining Sect, Corpse Yin Sect, Fighting Evil Sect")
            .ruins("Suzaku Tomb (sealed, permanent, 17 layers); Immortal Graveyard 17 Layers; "
                + "Falling Land (Trial grounds); Fighting Evil Sect massacre site")
            .jossFlame("Largest single Joss Flame production zone on Planet Suzaku (500M+ mortals in Vermilion Bird Country)")
            .player("Early-mid game. 11 countries to explore. Sea of Devils (lawless). "
                + "Vermilion Bird Country (capital). Pilu Kingdom (Soul Refining Sect ruins). "
                + "Suzaku Tomb, Immortal Graveyard, Falling Land as heritage sites.")
            .build(),

        // ECO-03: Ancient Battlefield
        new EcotopeBuilder("ECO-03", "The Ancient Battlefield", "古神之地",
            "Tu Si's body IS this location. Pocket-realm on Planet Suzaku.")
            .worldLaw(WorldLawTier.HIGH).sealed(SealedStatus.SEALED_ANCIENT).confidence(5)
            .vein("Tu Si's body itself IS the spirit vein", "ancient_god", "supreme", true)
            .floraDerived("Ancient-war-remnant moss; Restriction-attuned vines on Restriction Mountain")
            .faunaCanon("Tuo Sen (Tu Si's demonic thought, Ancient God tier, Level 3 — Annihilation realm)")
            .faunaDerived("Level 1: hurricane of devils; Level 2: Restriction Mountain guardian beasts")
            .mortals("None (pocket-realm)")
            .sects("None active")
            .ruins("The entire Ancient Battlefield IS a ruin — Tu Si's death created it. "
                + "Level 2: Restriction Mountain + Bridge of No Return. Level 3: Annihilation realm.")
            .jossFlame("None (pocket-realm, no mortals)")
            .player("Mid-game heritage site. 3 levels of increasing danger. "
                + "Level 1: hurricane of devils (avoid or tank). "
                + "Level 2: Restriction Mountain + Bridge (title: 'Great Enlightened One'). "
                + "Level 3: Tuo Sen encounter (Ancient God tier). Ancient God Tactic legacy.")
            .build(),

        // ECO-04: Sea of Devils
        new EcotopeBuilder("ECO-04", "The Sea of Devils", "万魔海",
            "Lawless region on Planet Suzaku, 14+ districts.")
            .worldLaw(WorldLawTier.LOW).confidence(5)
            .vein("None explicitly named (lawless region)", "none", "none", false)
            .floraDerived("Cold Dan source herbs; Sea-devil coral (Type C); "
                + "district-specific algae")
            .faunaCanon("Mosquito Beast (pre-evolution origin); Cold Dan creatures")
            .faunaDerived("14+ district-specific beast populations (Type C); "
                + "sea serpent variants; corpse-animating scavengers near Corpse Yin Sect")
            .mortals("None in Sea proper; Nan Dou City has ~50,000-100,000 (Type C)")
            .sects("Fighting Evil Sect (DESTROYED by Wang Lin), Corpse Yin Sect, "
                + "Soul Refining Sect (heritage transferred out), Nan Dou City (trade hub)")
            .ruins("Fighting Evil Sect massacre site (Core Formation-tier resentful spirits, "
                + "~50-200y to decay to scavenger ecology)")
            .jossFlame("Negligible (no mortals in Sea proper); Nan Dou City generates some via trade")
            .player("Mid-game danger zone. 14+ districts with distinct ecologies. "
                + "Nan Dou City trade hub. Corpse Yin Sect (corpse-army encounters). "
                + "Fighting Evil Sect ruins (resentful spirits). Cold Dan source.")
            .build(),

        // ECO-05: Planet Tian Yun
        new EcotopeBuilder("ECO-05", "Planet Tian Yun", "天运星",
            "Rank-7 cultivation planet, Heavenly Fate Sect headquarters.")
            .worldLaw(WorldLawTier.MEDIUM).confidence(5)
            .vein("7 supreme (one per division) + 1 planetary supreme (All-Seer's seat)", "mixed", "supreme", true)
            .vein("~50-100 mid veins", "mixed", "mid", false)
            .vein("~500-1000 minor veins", "mixed", "minor", false)
            .floraDerived("Tier-appropriate herbs for each division's vein aspect")
            .faunaCanon("Wind Demon (Immortal Monarch's Cave Mansion, sealed)")
            .faunaDerived("Division-specific guardian beasts (Type C); "
                + "East Demon Spirit Sea beast population (Soul Refining Tribe territory)")
            .mortals("5-20 billion (Type C) — largest single-planet mortal population in the Cave World")
            .sects("Heavenly Fate Sect (DESTROYED — All-Seer killed ~Year 300, "
                + "7 divisions persist as sub-faction ruins), Yao Family (DESTROYED), "
                + "Soul Refining Tribe (1M+ people, East Demon Spirit Sea)")
            .ruins("Heavenly Fate Sect 7 divisions (leadership destroyed ~Year 300, "
                + "sub-faction ruins with competing successors); Yao Family territory (planets "
                + "destroyed by Wang Lin); Immortal Monarch's Cave Mansion (Wind Demon slayed)")
            .jossFlame("Largest single Joss Flame production in the Cave World "
                + "(5-20B mortals). 30% siphoned to Seven-Colored Daoist.")
            .player("Mid-game major zone. 7 color divisions (distinct paths). "
                + "Soul Refining Tribe (allies if Wang Lin-aligned). Immortal Monarch's "
                + "Cave Mansion (Wind Demon boss). Yao Family ruins.")
            .build(),

        // ECO-06: Allheaven Star System
        new EcotopeBuilder("ECO-06", "Allheaven Star System", "天逆星系",
            "Wang Lin's home star system in the Sealed Realm.")
            .worldLaw(WorldLawTier.MEDIUM).confidence(5)
            .vein("Standard star-system vein distribution (Type C)", "mixed", "mid", false)
            .floraDerived("Star-system-appropriate herbs")
            .faunaDerived("Star-system-appropriate beasts")
            .mortals("~10-100 billion (Type C)")
            .sects("Thunder Celestial Realm (3 trials); Rain Celestial Realm (trap site); "
                + "Yao Family (Southern Domain, DESTROYED); Chosen Immortal Clan (enslaved, "
                + "freed by Wang Lin)")
            .ruins("Yao Family planetary destruction sites (multiple planets destroyed by Wang Lin)")
            .jossFlame("Significant (10-100B mortals → high Joss Flame output)")
            .player("Mid-game zone. Thunder Celestial Realm (3 trials for Thunder title). "
                + "Rain Celestial Realm (Celestial Slaughter Art trap). "
                + "Chosen Immortal Clan (rescue quest).")
            .build(),

        // ECO-07: Cloud Sea Star System
        new EcotopeBuilder("ECO-07", "Cloud Sea Star System", "云海星系",
            "Major star system in the Sealed Realm.")
            .worldLaw(WorldLawTier.MEDIUM_HIGH).confidence(5)
            .vein("Standard star-system vein distribution (Type C)", "mixed", "mid", false)
            .floraDerived("Star-system-appropriate herbs")
            .faunaDerived("Star-system-appropriate beasts")
            .mortals("~50-500 billion (Type C)")
            .sects("Da Lou Sword Sect (Ling Tianhou, Third Step); Origin Sect (rank 6); "
                + "Rank 9 God Sect (DESTROYED — Daoist Water killed by Wang Lin); "
                + "Treasured Jade Sect (auction house); Everlasting Sect (Blood Sword); Ghost Sect; "
                + "Seven-Colored Realm (108 nails)")
            .ruins("Rank 9 God Sect (Daoist Water's stronghold, DESTROYED); "
                + "Seven-Colored Realm (Seven-Colored Daoist's tools, 108 nails)")
            .jossFlame("Very high (50-500B mortals)")
            .player("Late-game zone. Da Lou Sword Sect. Origin Sect. "
                + "Seven-Colored Realm (108 nails — Seven-Colored Daoist's tools). "
                + "Treasured Jade Sect (auctions). Wild Continent ('secret for Third Step').")
            .build(),

        // ECO-08: Sealed/Outer Realm Boundary
        new EcotopeBuilder("ECO-08", "Sealed/Outer Realm Boundary", "封界/外界边界",
            "The boundary zone between Sealed Realm and Outer Realm. Scarred warzone.")
            .worldLaw(WorldLawTier.MEDIUM_HIGH).sealed(SealedStatus.SEALED).confidence(5)
            .vein("None explicitly named at the boundary", "none", "none", false)
            .floraDerived("War-scar flora: battle-remnant moss, blood-absorbing fungi")
            .faunaCanon("None named")
            .faunaDerived("War-scar beasts; boundary-patrolling creatures (Type C)")
            .mortals("None (warzone)")
            .sects("None active (warzone)")
            .ruins("Heaven-Splitting Axe is the seal spirit (sentient, dormant) — "
                + "this IS a ruin of the sealing process itself")
            .jossFlame("None (warzone)")
            .player("Transition zone between Sealed Realm and Outer Realm. "
                + "Heaven-Splitting Axe encounter. The boundary is dangerous and scarred.")
            .build(),

        // ECO-09: IAC (Immortal Astral Continent)
        new EcotopeBuilder("ECO-09", "Immortal Astral Continent (IAC)", "仙罔大陆",
            "The true reality outside the Cave World. 7 continents, 440+ spirit veins, 9 suns.")
            .worldLaw(WorldLawTier.ABSOLUTE).confidence(5)
            .vein("120+ Fire Veins on Heavenly Bull Continent", "fire", "mid", true)
            .vein("Earth Fire main vein (Great Soul Sect — devoured by Wang Lin)", "fire", "major", true)
            .vein("~320 other veins across 7 continents (Type C)", "mixed", "mixed", false)
            .floraDerived("Continental-scale flora; Mountain-Tree Sap (Mountain Tree Seal); "
                + "Pill-Sea Residue Herb (alchemical-waste biome); Dong-Lin Pool Lily")
            .faunaCanon("Earth Fire Dragon (Tianniu area, soul-extracted by Wang Lin); "
                + "Green Devil Scorpion (Green Devil Continent, peak Third Step+); "
                + "Mosquito Beast (arrives from Sea of Devils, evolved)")
            .faunaDerived("Grand Empyrean-tier beasts (9-Sun level); "
                + "continent-specific apex predators; Dong-Lin Pool Guardian (Nirvana Fruit tier)")
            .mortals("1-10 trillion (Type C) — largest Joss Flame base in the Cave World")
            .sects("Ancient Clan (Dao Gu/Dao Yi/Primordial Ancient lineages); "
                + "Celestial Clan (rival race); Great Soul Sect (Wang Lin as elder); "
                + "Canglong Sect (Earth Fire Dragon); Gui Yi Sect; "
                + "Dao Devil Sect (DESTROYED); Dong Lin Sect (DESTROYED pre-arrival); "
                + "Zhen/Zhan Family; Dark Scorpion Clan (Wang Lin as Ruler)")
            .ruins("Dao Devil Sect (annihilated by Wang Lin); Dong Lin Sect (destroyed "
                + "pre-Wang-Lin arrival); Ancient Tomb (Ancient Clan relic concentration, "
                + "intact); Dong Lin Pool (sealed Ancient God beneath); "
                + "Soul Refining Sect ruins (Pilu Kingdom, heritage transferred)")
            .jossFlame("Highest output (1-10T mortals). 30% siphoned to Seven-Colored Daoist "
                + "(until ~Year 1500, then to Wang Lin). After Year 1500, Wang Lin is "
                + "new owner — Joss Flame flow to Wang Lin, seal dissolved.")
            .player("Endgame zone. 7 continents to explore. Ancient Tomb (relics). "
                + "Dong Lin Pool (Reincarnation Essence). Ancestral Temple (Lou Hou's soul). "
                + "Pill Sea (unique biome). Great Soul Sect (elder position). "
                + "Green Devil Continent (Green Devil Scorpion boss). 9 Suns / 10th Sun ascension.")
            .build(),

        // ECO-10: Other Named Geography
        new EcotopeBuilder("ECO-10", "Other Named Geography", "其他地理",
            "Cross-region summary of canon-attested locations.")
            .worldLaw(WorldLawTier.MEDIUM).confidence(5)
            .floraCanon("Brilliant Golden Fruit (Earth Planet, beast evolution catalyst)")
            .faunaCanon("Mosquito Beast (Sea of Devils origin), Thunder Toad, Golden Exalt Sea Dragon, "
                + "Earth Fire Dragon, Green Devil Scorpion, Wind Demon")
            .mortals("Various (see individual entries)")
            .sects("Various (see individual entries)")
            .ruins("Various (see individual entries)")
            .jossFlame("Contributes to overall Cave World Joss Flame pool")
            .player("Cross-region reference. Individual locations accessed via star travel.")
            .build()
    ));

    // ─── Query Methods ──────────────────────────────────────────────────

    public static List<RegionEcotope> getAllRegions() { return ALL_REGIONS; }

    public static RegionEcotope getRegionById(String id) {
        for (RegionEcotope r : ALL_REGIONS) { if (r.id.equals(id)) return r; }
        return null;
    }

    public static List<RegionEcotope> getRegionsByWorldLaw(WorldLawTier tier) {
        return ALL_REGIONS.stream().filter(r -> r.worldLaw == tier).collect(Collectors.toList());
    }

    public static List<RegionEcotope> getSealedRegions() {
        return ALL_REGIONS.stream()
            .filter(r -> r.sealed != SealedStatus.OPEN)
            .collect(Collectors.toList());
    }

    public static List<RegionEcotope> getUnsealedRegions() {
        return ALL_REGIONS.stream()
            .filter(r -> r.sealed == SealedStatus.OPEN)
            .collect(Collectors.toList());
    }

    /**
     * Get the beast tier cap for a region, per Law G2.
     * Inside the Sealed Realm: tier 10 (Heaven Blight).
     * Outside: uncapped (Integer.MAX_VALUE used as sentinel).
     */
    public static int getBeastTierCap(String regionId) {
        RegionEcotope r = getRegionById(regionId);
        if (r == null) return 10;
        // Law G2: sealed regions capped at tier 10
        if (r.sealed == SealedStatus.SEALED) return 10;
        return r.worldLaw.beastTierCap;
    }

    /**
     * Get ruin decay years for a given ruin tier, per Thread T6.
     */
    public static int[] getRuinDecayYears(String ruinTier) {
        for (RuinDecayEntry e : RUIN_DECAY_TIMELINE) {
            if (e.ruinTier.equals(ruinTier)) return new int[]{e.decayYearsMin, e.decayYearsMax};
        }
        return new int[]{10, 50}; // default for unknown tier
    }

    /**
     * Get all named beasts in a specific region.
     */
    public static List<NamedBeast> getBeastsInRegion(String regionId) {
        // Named beasts are cross-region; this filters by origin region
        return NAMED_BEASTS.stream()
            .filter(b -> b.originRegion.contains(regionId) ||
                b.originRegion.contains(getRegionName(regionId)))
            .collect(Collectors.toList());
    }

    private static String getRegionName(String id) {
        RegionEcotope r = getRegionById(id);
        return r != null ? r.name : id;
    }

    /**
     * Get heritage sites in a specific region (by name substring match).
     */
    public static List<HeritageSite> getHeritageSitesInRegion(String region) {
        return HERITAGE_SITES.stream()
            .filter(h -> h.location.contains(region))
            .collect(Collectors.toList());
    }

    /**
     * Get the Joss Flame owner at a given year.
     * Per Thread T5: before ~Year 1500 = Seven-Colored Daoist;
     * after = Wang Lin.
     */
    public static String getJossFlameOwnerAtYear(int year) {
        if (year < 1500) return "Seven-Colored Daoist";
        return "Wang Lin";
    }
}