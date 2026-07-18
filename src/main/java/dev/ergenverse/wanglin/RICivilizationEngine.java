package dev.ergenverse.wanglin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RI Civilization Engine — Layer 4 of the Renegade Immortal World Bible.
 *
 * <p><b>Source:</b> CANON_RI_CIVILIZATION.md (1,361 lines, 45 factions, 6 axioms).
 * Cross-referenced with:
 * <ul>
 *   <li>CANON_RI_COMPLETE_WORLD.md (3,034 lines)</li>
 *   <li>CANON_RI_COMPLETE_TECHNIQUES.md (1,793 lines)</li>
 *   <li>CANON_RI_ECOLOGY.md (2,087 lines)</li>
 * </ul>
 *
 * <p><b>The Prime Directive:</b> every faction exists objectively whether the
 * player encounters it or not. Every mortal is unknowing livestock for the
 * Joss Flame economy. Sect power is determined by the equation:
 * <pre>
 *   Power = PeakRealmTier x DiscipleCount x ResourceControl
 * </pre>
 *
 * <p><b>The 6 Civilization Axioms:</b>
 * <ol>
 *   <li>C1: The Joss Flame Economy Underlies All Civilization</li>
 *   <li>C2: Sect Power = PeakRealm x Disciples x Resources</li>
 *   <li>C3: Hierarchy Mirrors Cultivation Realms</li>
 *   <li>C4: Internal Divisions Reflect Specialization, Not Politics</li>
 *   <li>C5: Economy Flows Up the Realm Ladder</li>
 *   <li>C6: Sect Destruction Creates Ruin Ecology</li>
 * </ol>
 *
 * <p><b>Faction coverage:</b> 18 fully detailed + 27 briefs = 45 total.
 * Each entry tagged with 5-tier BridgingPolicy confidence (A/B/C/D/F).
 *
 * <p><b>Per user correction #8:</b> the true antagonist is the Seven-Colored
 * Daoist (Seven Paths Sect), NOT Allheaven. Allheaven is ISSTH-specific.
 *
 * <p><b>Per user correction #5:</b> no hardcoded realm gates. Hierarchy is
 * descriptive (maps onto realms), not prescriptive.
 */
public final class RICivilizationEngine {

    private RICivilizationEngine() {}

    // ─── Enums ──────────────────────────────────────────────────────────

    /** Faction organizational type. */
    public enum CivType {
        SECT("Cultivation sect"),
        CLAN("Cultivation clan or bloodline"),
        DYNASTY("Nation-state or dynasty"),
        ALLIANCE("Coalition or alliance"),
        TRIBE("Tribal organization"),
        RACE("Primordial race"),
        MORTAL_CLAN("Non-cultivating mortal clan"),
        COALITION("Multi-sect coalition");

        public final String description;
        CivType(String d) { this.description = d; }
    }

    /** Faction moral alignment. Per Axiom C1, alignment is irrelevant to Joss Flame. */
    public enum Alignment {
        RIGHTEOUS("Righteous / Orthodox"),
        NEUTRAL("Neutral"),
        DEMONIC("Demonic"),
        ANTAGONIST("Canon-attested antagonist"),
        BEYOND("Beyond mortal morality — Paragon-tier");

        public final String description;
        Alignment(String d) { this.description = d; }
    }

    /** Faction lifecycle state (per Part 6, Sect Lifecycle Model). */
    public enum FactionState {
        ACTIVE("Currently operating at canon-attested strength"),
        DECLINING("Lost leadership or territory, still exists"),
        DESTROYED("Canon-attested destruction"),
        HERITAGE_SITE("Destroyed but with recoverable techniques/artifacts"),
        HIDDEN("Not discoverable until specific conditions met");

        public final String description;
        FactionState(String d) { this.description = d; }
    }

    /** Canon destruction pattern (4 attested in Part 6.4). */
    public enum DestructionPattern {
        TOTAL_ANNIHILATION("Dao Devil Sect — ritual reversal"),
        LEADERSHIP_ASSASSINATION("Fighting Evil Sect — leader killed, sect released"),
        CLAN_EXTERMINATION("Teng Clan, Wang Clan — all members killed"),
        PRE_ARRIVAL("Dong Lin Sect — destroyed before Wang Lin arrived");

        public final String description;
        DestructionPattern(String d) { this.description = d; }
    }

    /** Player interaction mode (per Part 7.3). */
    public enum PlayerInteraction {
        JOIN("Player can join as a disciple"),
        SERVE("Player can serve as a retainer"),
        CONQUER("Player can destroy/weaken"),
        FOUND("Player can found a new sect"),
        INHERIT("Player can inherit destroyed faction's heritage"),
        IGNORE("Player can bypass entirely"),
        TRADE("Player can trade with"),
        RESCUE("Player can rescue from danger");

        public final String description;
        PlayerInteraction(String d) { this.description = d; }
    }

    // ─── Data Classes ───────────────────────────────────────────────────

    /** A named member at a specific rank within a faction. */
    public static final class FactionMember {
        public final String name;
        public final String nameCn;
        public final String rank;        // "Patriarch", "Elder", "Core Disciple", etc.
        public final String realm;       // "Soul Formation", "Nascent Soul", etc.
        public final String note;        // canonical role description

        public FactionMember(String name, String nameCn, String rank, String realm, String note) {
            this.name = name; this.nameCn = nameCn; this.rank = rank;
            this.realm = realm; this.note = note;
        }
    }

    /** A relationship between two factions. */
    public static final class Relationship {
        public final String targetFactionId;  // "CIV-01", "CIV-brief-01", etc.
        public final String relationType;     // "Enemy", "Ally", "Vassal", "Heritage Successor", etc.
        public final String note;
        public final int confidence;          // 1-5

        public Relationship(String targetFactionId, String relationType, String note, int confidence) {
            this.targetFactionId = targetFactionId; this.relationType = relationType;
            this.note = note; this.confidence = confidence;
        }
    }

    /** A major canon event involving this faction. */
    public static final class CanonEvent {
        public final String description;
        public final String timelineEventId;  // "E11", "E21", etc. (null if not in TIMELINE)
        public final int confidence;          // 1-5

        public CanonEvent(String description, String timelineEventId, int confidence) {
            this.description = description; this.timelineEventId = timelineEventId;
            this.confidence = confidence;
        }
    }

    /** An economy entry. */
    public static final class EconomyEntry {
        public final String resource;
        public final String detail;
        public final int confidence;

        public EconomyEntry(String resource, String detail, int confidence) {
            this.resource = resource; this.detail = detail; this.confidence = confidence;
        }
    }

    /** The complete record for one faction. */
    public static final class Faction {
        public final String id;
        public final String name;
        public final String nameCn;
        public final boolean isBrief;         // true = 27-brief entry; false = 18-detailed entry
        public final CivType type;
        public final Alignment alignment;
        public final String peakRealm;
        public final String headquarters;
        public final String specialization;
        public final FactionState state;
        public final DestructionPattern destructionPattern; // null if not destroyed
        public final int confidence;          // 1-5

        // For detailed entries (18):
        public final List<FactionMember> members;
        public final List<EconomyEntry> economy;
        public final List<Relationship> relationships;
        public final List<CanonEvent> events;
        public final List<PlayerInteraction> playerInteractions;
        public final String playerEncounterNote;

        // For brief entries (27):
        public final String briefNote;

        private Faction(String id, String name, String nameCn, boolean isBrief,
                        CivType type, Alignment alignment, String peakRealm,
                        String headquarters, String specialization, FactionState state,
                        DestructionPattern destructionPattern, int confidence,
                        List<FactionMember> members, List<EconomyEntry> economy,
                        List<Relationship> relationships, List<CanonEvent> events,
                        List<PlayerInteraction> playerInteractions, String playerEncounterNote,
                        String briefNote) {
            this.id = id; this.name = name; this.nameCn = nameCn;
            this.isBrief = isBrief; this.type = type; this.alignment = alignment;
            this.peakRealm = peakRealm; this.headquarters = headquarters;
            this.specialization = specialization; this.state = state;
            this.destructionPattern = destructionPattern; this.confidence = confidence;
            this.members = members; this.economy = economy;
            this.relationships = relationships; this.events = events;
            this.playerInteractions = playerInteractions;
            this.playerEncounterNote = playerEncounterNote; this.briefNote = briefNote;
        }

        /** Factory for a fully-detailed faction (18 entries). */
        public static Faction detailed(String id, String name, String nameCn,
                CivType type, Alignment alignment, String peakRealm,
                String headquarters, String specialization, FactionState state,
                DestructionPattern destructionPattern, int confidence,
                List<FactionMember> members, List<EconomyEntry> economy,
                List<Relationship> relationships, List<CanonEvent> events,
                List<PlayerInteraction> playerInteractions, String playerEncounterNote) {
            return new Faction(id, name, nameCn, false, type, alignment, peakRealm,
                headquarters, specialization, state, destructionPattern, confidence,
                members, economy, relationships, events, playerInteractions,
                playerEncounterNote, null);
        }

        /** Factory for a brief faction (27 entries). */
        public static Faction brief(String id, String name, String nameCn,
                CivType type, String peakRealm, String headquarters,
                String specialization, FactionState state, int confidence,
                String briefNote) {
            return new Faction(id, name, nameCn, true, type, null, peakRealm,
                headquarters, specialization, state, null, confidence,
                null, null, null, null, null, null, briefNote);
        }
    }

    // ─── The 6 Civilization Axioms ──────────────────────────────────────

    public static final class Axiom {
        public final String id;
        public final String title;
        public final String statement;
        public final int confidence;

        Axiom(String id, String title, String statement, int confidence) {
            this.id = id; this.title = title; this.statement = statement;
            this.confidence = confidence;
        }
    }

    public static final List<Axiom> CIVILIZATION_AXIOMS = Arrays.asList(
        new Axiom("C1", "The Joss Flame Economy Underlies All Civilization",
            "Every mortal in the Cave World generates Joss Flame through worship, prayer, and cultural veneration. "
            + "The Realm-Sealing Grand Array siphons ~30% of this to the Seven-Colored Daoist. "
            + "Sects, nations, and clans are all — knowingly or unknowingly — Joss Flame farms embedded within "
            + "a cosmic livestock operation. A sect's alignment is irrelevant to this structural truth.", 5),
        new Axiom("C2", "Sect Power = PeakRealm x DiscipleCount x ResourceControl",
            "A sect's strength derives from three multiplicative factors: (a) the peak cultivation realm "
            + "achievable within its territory (function of local spirit vein quality, per Ecology G3), "
            + "(b) the number of cultivators at each realm tier, and (c) the quantity and quality of "
            + "controlled resources (spirit stones, herbs, beasts, artifacts, secret realms).", 4),
        new Axiom("C3", "Hierarchy Mirrors Cultivation Realms",
            "Every attested sect follows a hierarchical structure that maps onto the realm ladder. "
            + "The exact titles vary (Patriarch/Ancestor/Sect Master/Divine Emperor), but the pattern "
            + "is universal: the strongest cultivator leads. 'Core disciple' correlates with "
            + "Foundation Establishment-Core Formation. 'Outer disciple' correlates with Qi Condensation. "
            + "This is not generic fantasy — it is the logical consequence of a society where "
            + "personal power IS political power.", 4),
        new Axiom("C4", "Internal Divisions Reflect Specialization, Not Politics",
            "Where canon attests internal subdivisions (Heavenly Fate Sect's 7 color divisions, "
            + "Vermilion Bird's generational succession), these reflect functional specialization — "
            + "different cultivation paths, element affinities, or inheritance lines — rather than "
            + "political factions. Civil wars within sects are rare and cataclysmic.", 5),
        new Axiom("C5", "Economy Flows Up the Realm Ladder",
            "Resources flow upward: mortals gather herbs -> outer disciples process them -> inner "
            + "disciples use pills for breakthrough -> core disciples control spirit veins -> elders "
            + "manage trade -> the patriarch manages strategic relationships. Spirit stones and "
            + "celestial jades are the universal currency. The Joss Flame economy operates invisibly above all.", 3),
        new Axiom("C6", "Sect Destruction Creates Ruin Ecology",
            "When a sect is destroyed, ruins follow a predictable decay pattern per Ecology Global Law G4: "
            + "resentful spirits linger at the tier of the destroyed cultivators, artifacts scatter or get "
            + "buried, surviving disciples flee carrying techniques and grudges, and the local ecology "
            + "reclaims the territory. These ruins become exploration sites for future cultivators.", 4)
    );

    // ─── Currency Hierarchy ─────────────────────────────────────────────

    public static final class CurrencyTier {
        public final String name;
        public final String usedBy;
        public final String source;

        CurrencyTier(String name, String usedBy, String source) {
            this.name = name; this.usedBy = usedBy; this.source = source;
        }
    }

    public static final List<CurrencyTier> CURRENCY_HIERARCHY = Arrays.asList(
        new CurrencyTier("Low-Grade Spirit Stones", "Mortal-tier to Qi Condensation", "Basic mining/gathering"),
        new CurrencyTier("Mid-Grade Spirit Stones", "Foundation Establishment to Core Formation", "Spirit vein refinement"),
        new CurrencyTier("High-Grade Spirit Stones", "Nascent Soul to Soul Transformation", "Major vein extraction"),
        new CurrencyTier("Celestial Jades", "Soul Transformation to Ascendant", "Rare vein deposits, theft"),
        new CurrencyTier("Immortal Jades", "Nirvana Shatterer to Third Step", "Immortal realm deposits"),
        new CurrencyTier("Origin Crystals", "Domain comprehension", "Extreme-rarity resource")
    );

    // ─── Heritage Transfer Paths (Part 6.5, Type A) ────────────────────

    public static final class HeritagePath {
        public final String description;
        public final String whatTransferred;  // technique, banner, bloodline, etc.

        HeritagePath(String description, String whatTransferred) {
            this.description = description; this.whatTransferred = whatTransferred;
        }
    }

    public static final List<HeritagePath> HERITAGE_PATHS = Arrays.asList(
        new HeritagePath("Soul Refining Sect -> Wang Lin -> Soul Refining Tribe",
            "Soul Refining techniques + Ten Billion Soul Banner"),
        new HeritagePath("Vermilion Bird Line (1st -> 2nd -> ... -> 6th/Wang Lin -> 15th/Zhou Wutai)",
            "Vermilion Bird bloodline + Nine Mysterious Transformations"),
        new HeritagePath("Great Soul Sect -> Wang Lin",
            "Ghostly Sail -> Restriction Essence bloodlines"),
        new HeritagePath("Blue Silk Clan -> Wang Lin",
            "Dao Art Fusion + Light Shadow Shield from Blue Dream")
    );

    // ─── Canon-Silent Entities (FORBIDDEN — must NOT appear as canon) ──

    public static final class ForbiddenEntity {
        public final String forbiddenName;
        public final String canonReplacement;
        public final String reason;

        ForbiddenEntity(String forbiddenName, String canonReplacement, String reason) {
            this.forbiddenName = forbiddenName; this.canonReplacement = canonReplacement;
            this.reason = reason;
        }
    }

    public static final List<ForbiddenEntity> FORBIDDEN_ENTITIES = Arrays.asList(
        new ForbiddenEntity("Ten Thousand Demons Sect", "Sky Demon Country + Ancient Demon City",
            "Not attested in canon. Do NOT invent as canon."),
        new ForbiddenEntity("Xue Yue", "Snow Domain Country",
            "Not attested in canon. Use Snow Domain Country if needed."),
        new ForbiddenEntity("Heavenly Demon City", "Ancient Demon City",
            "Not attested in canon. Use Ancient Demon City."),
        new ForbiddenEntity("Suzaku Continent", "Planet Suzaku",
            "Planet Suzaku is a PLANET, not a continent. Continent is a false classification."),
        new ForbiddenEntity("Heavenly Fate Continent", "Planet Tian Yun",
            "Planet Tian Yun is a PLANET, not a continent."),
        new ForbiddenEntity("Ancient Emperor's inheritance", "Immortal Monarch's Cave Mansion",
            "Not attested in canon. Use Immortal Monarch's Cave Mansion.")
    );

    // ─── Regional Power Structures (Part 5) ────────────────────────────

    /** The 5 regional power structure IDs for cross-referencing. */
    public static final List<String> REGIONAL_STRUCTURES = Arrays.asList(
        "Zhao Country", "Sea of Devils", "Planet Tian Yun",
        "Cloud Sea Star System", "Immortal Astral Continent"
    );

    // ─── ALL FACTIONS ───────────────────────────────────────────────────

    private static final List<FactionMember> NO_MEMBERS = Collections.emptyList();
    private static final List<EconomyEntry> NO_ECONOMY = Collections.emptyList();
    private static final List<Relationship> NO_RELATIONS = Collections.emptyList();
    private static final List<CanonEvent> NO_EVENTS = Collections.emptyList();
    private static final List<PlayerInteraction> NO_INTERACTIONS = Collections.emptyList();

    public static final List<Faction> ALL_FACTIONS = Collections.unmodifiableList(Arrays.asList(

        // ═══════════════════════════════════════════════════════════════
        // 18 FULLY DETAILED FACTIONS
        // ═══════════════════════════════════════════════════════════════

        // --- CIV-01: Heng Yue Sect ---
        Faction.detailed("CIV-01", "Heng Yue Sect", "恒岳派",
            CivType.SECT, Alignment.RIGHTEOUS,
            "Soul Formation (Huang Long Zhenren / Lu Yun)",
            "Country of Zhao, Planet Suzaku",
            "Sword arts, Qi gathering, foundation techniques",
            FactionState.DECLINING, null, 5,
            Arrays.asList(
                new FactionMember("Huang Long Zhenren", "黄龙真人", "Patriarch / Sect Master",
                    "Soul Formation", "Secretly 5th-Gen Vermilion Bird Divine Emperor Lu Yun. Died after returning from Cultivation Alliance."),
                new FactionMember("Wang Hao", "王浩", "Core Disciple", "Foundation Establishment",
                    "Spared during Teng Clan massacre of Wang Family Village."),
                new FactionMember("Wang Zhuo", "王卓", "Core Disciple", "Foundation Establishment",
                    "Spared during Teng Clan massacre."),
                new FactionMember("Wang Lin", "王林", "Outer Disciple (Legacy)",
                    "Qi Condensation (at time)", "Failed all 3 entrance tests. Became legacy disciple."),
                new FactionMember("Zhang Hu", "张虎", "Outer Disciple", "Qi Condensation",
                    "Early friend. Later became bandit (Sun Dazhu's group).")
            ),
            Arrays.asList(
                new EconomyEntry("Spirit Stones", "Low-grade only; Zhao Country is impoverished.", 3),
                new EconomyEntry("Herbs", "Qi-Gathering Grass, Foundation-Root Vine, Sword-Edge Moss.", 3),
                new EconomyEntry("Trade", "Tian Shui City is the nearby major trading hub.", 4),
                new EconomyEntry("Joss Flame", "Wang Family Village and surrounding villages generate Joss Flame via ancestor worship. Siphoned at 30%.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-11", "Hostile", "Teng Huayuan exterminated Wang Family Village (Heng Yue's recruiting ground).", 5),
                new Relationship("CIV-05", "Vassal Territory", "Heng Yue operates within Vermilion Bird Country's Zhao Country region.", 4),
                new Relationship("CIV-brief-02", "Neutral Neighbor", "Wang Lin broke through to Soul Formation on a mountain near Xuan Dao Sect.", 4)
            ),
            Arrays.asList(
                new CanonEvent("Wang Lin's entrance test — failed all 3 tests", "E11", 5),
                new CanonEvent("Teng Clan exterminates Wang Family Village — Heng Yue does not intervene (too weak)", null, 5),
                new CanonEvent("Wang Lin returns as Soul Formation to bury the dead of Wang Family Village", null, 5),
                new CanonEvent("Lu Yun dies after returning from Cultivation Alliance — Heng Yue loses its secret master", null, 5)
            ),
            Arrays.asList(PlayerInteraction.JOIN),
            "A small, struggling orthodox sect in a low-spirit-energy region. The player would encounter it early as a potential sect to join. The sect's hidden connection to the Vermilion Bird line is a late-game revelation."
        ),

        // --- CIV-02: Soul Refining Sect ---
        Faction.detailed("CIV-02", "Soul Refining Sect", "炼魂宗",
            CivType.SECT, Alignment.DEMONIC,
            "Nirvana Scryer+ (Dun Tian)",
            "Pilu Kingdom, Planet Suzaku",
            "Soul refining, soul extraction, soul sealing",
            FactionState.HERITAGE_SITE, DestructionPattern.LEADERSHIP_ASSASSINATION, 5,
            Arrays.asList(
                new FactionMember("Dun Tian", "敦天", "Ancestor / Founder", "Nirvana Scryer+",
                    "Gave 3 gifts to Wang Lin. Self-erased to become soul in Ten Billion Soul Banner."),
                new FactionMember("Nian Tian", "念天", "Senior Brother", "Nirvana Scryer",
                    "Dun Tian's senior brother."),
                new FactionMember("Wang Lin", "王林", "Inheritor", "Soul Transformation",
                    "Inherited the sect and Ten Billion Soul Banner. Became 'Ancestor of the Soul Refining Tribe.'")
            ),
            Arrays.asList(
                new EconomyEntry("Celestial Jades", "Dun Tian stole from neighbor sects.", 5),
                new EconomyEntry("Soul Cores", "Primary resource — extracted from beasts and enemy cultivators.", 5),
                new EconomyEntry("Artifacts", "Ten Billion Soul Banner — guardian treasure AND soul container.", 5)
            ),
            Arrays.asList(
                new Relationship("CIV-14", "Heritage Successor", "Wang Lin taught heritage to Mountain Valley Tribe, which became the Soul Refining Tribe.", 5),
                new Relationship("CIV-03", "Proximity", "Both operate in Sea of Devils area. No direct alliance or enmity attested.", 4)
            ),
            Arrays.asList(
                new CanonEvent("Dun Tian's 3 gifts to Wang Lin: clone->Nascent Soul, true body->3-Star Ancient God, Ten Billion Soul Banner+inheritance", null, 5),
                new CanonEvent("Dun Tian self-erases to become soul in the Banner", null, 5),
                new CanonEvent("Wang Lin's Soul Transformation breakthrough using the Billion Soul Flag", null, 5)
            ),
            Arrays.asList(PlayerInteraction.INHERIT),
            "Extinct by the time the player reaches Pilu Kingdom. Find ruins: Dun Tian's meditation chamber, residual soul fragments, damaged soul banner. LIVING heritage is the Soul Refining Tribe in East Demon Spirit Sea."
        ),

        // --- CIV-03: Corpse Yin Sect ---
        Faction.detailed("CIV-03", "Corpse Yin Sect", "尸阴宗",
            CivType.SECT, Alignment.DEMONIC,
            "Nascent Soul+ (multiple elders)",
            "Planet Suzaku (Sea of Devils area)",
            "Corpse refinement, Yin cultivation, puppet arts",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Wu Yu", "吴宇", "Elder", "Nascent Soul",
                    "Only Nascent Soul remained alive."),
                new FactionMember("Ye Zi", "叶紫", "Elder", "Nascent Soul",
                    "Gave Wang Lin immortal cave; made him slice soul sliver."),
                new FactionMember("Adai", "阿呆", "Named Servant", "Unknown",
                    "Blue-skinned man; Wu Yu's servant; 9 talismans on body."),
                new FactionMember("Sun Tai", "孙泰", "Taken by Wang Lin", "Unknown",
                    "Became Wang Lin's servant."),
                new FactionMember("Lei Ji", "雷记", "Taken by Wang Lin", "Unknown",
                    "Became Wang Lin's mount.")
            ),
            Arrays.asList(
                new EconomyEntry("Corpses", "Primary resource — harvested from battlefields and enemies, refined into puppet-soldiers.", 5),
                new EconomyEntry("Soul Slivers", "Extracted from living cultivators. Used for corpse animation.", 5),
                new EconomyEntry("Immortal Caves", "Controls at least one immortal cave system.", 5),
                new EconomyEntry("Trade", "Nan Dou City serves as the Sea of Devils trading hub.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-02", "Proximity, No Alliance", "Same Sea of Devils area. Different specializations (corpses vs. souls).", 4),
                new Relationship("CIV-04", "Proximity, Possible Rivalry", "Same Sea of Devils region. Both 'demonic' but different methods.", 3)
            ),
            Arrays.asList(
                new CanonEvent("Adai leads Wang Lin to Wu Yu's Nascent Soul in Forest of Distorted Divine Sense", null, 5),
                new CanonEvent("Wang Lin enters Corpse Yin Sect; Ye Zi gives immortal cave", null, 5),
                new CanonEvent("Wang Lin attacks members, takes back soul sliver", null, 5),
                new CanonEvent("Wang Lin kills many members when they use Wang family for resentful spirits", null, 5)
            ),
            Arrays.asList(PlayerInteraction.SERVE),
            "A feared demonic sect in the Sea of Devils controlling corpse-armies. Player could find Wu Yu's Nascent Soul, explore Ye Zi's immortal cave, and encounter refined corpse-puppets."
        ),

        // --- CIV-04: Fighting Evil Sect ---
        Faction.detailed("CIV-04", "Fighting Evil Sect", "斗邪宗",
            CivType.SECT, Alignment.DEMONIC,
            "Soul Formation (Sect Leader)",
            "Sea of Devils, Planet Suzaku",
            "Devil cultivation, soul manipulation, killing orders",
            FactionState.DESTROYED, DestructionPattern.LEADERSHIP_ASSASSINATION, 5,
            Arrays.asList(
                new FactionMember("(unnamed)", "—", "Sect Leader", "Soul Formation",
                    "Killed by Wang Lin."),
                new FactionMember("(10 Core Formation)", "—", "Core Disciples", "Core Formation",
                    "All 10 killed by Wang Lin.")
            ),
            Arrays.asList(
                new EconomyEntry("Killing Bounties", "Ten Thousand Devil Hundred Day Kill Order — paid assassination contracts.", 5),
                new EconomyEntry("Plunder", "Sea of Devils is lawless — plunder from weaker cultivators is significant.", 4),
                new EconomyEntry("Trade", "Nan Dou City — nearby trade hub.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-01", "N/A", "N/A — different regions.", 0),
                new Relationship("CIV-03", "Proximity", "Same Sea of Devils region.", 4)
            ),
            Arrays.asList(
                new CanonEvent("10 Core Formation cultivators chase Wang Lin; Wang Lin breaks through to Core Formation", null, 5),
                new CanonEvent("Wang Lin kills the Sect Leader and takes control", null, 5),
                new CanonEvent("Massacre site becomes location with Core Formation-tier resentful spirits", null, 4)
            ),
            Arrays.asList(PlayerInteraction.CONQUER),
            "A predatory organization in the Sea of Devils issuing kill-orders and hunting weaker cultivators. After Wang Lin's conquest, becomes leaderless ruins — another Sea of Devils hazard zone."
        ),

        // --- CIV-05: Vermilion Bird Country ---
        Faction.detailed("CIV-05", "Vermilion Bird Country", "朱雀国",
            CivType.DYNASTY, Alignment.RIGHTEOUS,
            "Void Flame Cultivator (Divine Emperor)",
            "Planet Suzaku",
            "Ruling cultivation nation; Vermilion Bird Sequence inheritance",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Wang Lin", "王林", "6th-Gen Divine Emperor", "Void Flame Cultivator",
                    "6th-Gen Vermilion Bird. Returned rein to Azure Dragon; later transcends."),
                new FactionMember("Lu Yun", "陆云", "5th-Gen Divine Emperor", "Void Flame Cultivator",
                    "Secretly Heng Yue's Huang Long Zhenren. Died after returning from Cultivation Alliance."),
                new FactionMember("Situ Nan", "斯图南", "2nd-Gen Master", "Void Flame Cultivator",
                    "Green Soul of Seven-Colored Immortal Venerable. Physical body destroyed by 3rd-Gen betrayer."),
                new FactionMember("Hong Die", "红蝶", "Core Disciple", "Unknown",
                    "Core disciple of Vermilion Bird Country."),
                new FactionMember("Zhou Wutai", "周武泰", "15th-Gen Divine Emperor", "Unknown",
                    "Wang Lin transferred position to him.")
            ),
            Arrays.asList(
                new EconomyEntry("Mortal Population", "500M-1.5B in Vermilion Bird Country alone.", 4),
                new EconomyEntry("Spirit Veins", "Best on Planet Suzaku — the Level 6 nation controls highest-quality veins.", 4),
                new EconomyEntry("Joss Flame", "Largest single Joss Flame production zone on Planet Suzaku.", 4),
                new EconomyEntry("Vermilion Bird Sequence", "The inheritance system is an 'economy' — each generation receives accumulated bloodline power.", 5)
            ),
            Arrays.asList(
                new Relationship("CIV-01", "Vassal Territory", "Heng Yue operates within Vermilion Bird Country's Zhao Country.", 4),
                new Relationship("CIV-06", "Parent Organization", "Four Divine Sect governs all four divine beast lines.", 5),
                new Relationship("CIV-11", "Subject Clan", "Teng Clan operated within Vermilion Bird Country's territory.", 4)
            ),
            Arrays.asList(
                new CanonEvent("3rd-Gen betrayal — destroyed Situ Nan's physical body with Tan Lang", null, 5),
                new CanonEvent("Wang Lin becomes 6th-Gen Divine Emperor", null, 5),
                new CanonEvent("Wang Lin returns rein to Azure Dragon; transfers to Zhou Wutai", null, 5)
            ),
            Arrays.asList(PlayerInteraction.IGNORE),
            "The political backdrop of early-game Planet Suzaku. Player operates within its territory but rarely interacts directly. Defines the political landscape and controls best resources."
        ),

        // --- CIV-06: Four Divine Sect ---
        Faction.detailed("CIV-06", "Four Divine Sect", "四神宗",
            CivType.COALITION, Alignment.RIGHTEOUS,
            "Peak Third Step+ (multiple Divine Emperors)",
            "Vermilion Bird Starfield",
            "Divine beast cultivation, four-element arts",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Azure Dragon Divine Emperor", "青龙大帝", "Divine Emperor", "Peak Third Step+",
                    "One of four Divine Emperors."),
                new FactionMember("Vermilion Bird Divine Emperor", "朱雀大帝 (Wang Lin)", "Divine Emperor", "Void Flame Cultivator",
                    "6th-Gen. Wang Lin later returned the rein."),
                new FactionMember("White Tiger Divine Emperor", "白虎大帝", "Divine Emperor", "Peak Third Step+",
                    "One of four Divine Emperors."),
                new FactionMember("Black Tortoise Divine Emperor", "玄武大帝", "Divine Emperor", "Peak Third Step+",
                    "One of four Divine Emperors.")
            ),
            NO_ECONOMY,
            Arrays.asList(
                new Relationship("CIV-07", "Member Sub-Organization", "Vermilion Bird Divine Sect is a member.", 5)
            ),
            NO_EVENTS,
            Arrays.asList(PlayerInteraction.IGNORE),
            "Late-game entity. Player encounters after reaching Vermilion Bird Starfield. Governs divine beast inheritances."
        ),

        // --- CIV-07: Vermilion Bird Divine Sect ---
        Faction.detailed("CIV-07", "Vermilion Bird Divine Sect", "朱雀神宗",
            CivType.SECT, Alignment.RIGHTEOUS,
            "Void Flame Cultivator (Vermilion Bird Divine Emperor)",
            "Vermilion Bird Starfield",
            "Fire arts, Vermilion Bird bloodline, divine beast cultivation",
            FactionState.ACTIVE, null, 5,
            NO_MEMBERS,
            NO_ECONOMY,
            Arrays.asList(
                new Relationship("CIV-06", "Member Of", "Sub-organization of the Four Divine Sect.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Vermilion Bird Nine Mysterious Transformations taught to Wang Lin by Lu Yun", null, 5),
                new CanonEvent("Vermilion Bird Mark 4th Awakening — Ethereal Fire (cultivatable, unique to Wang Lin)", null, 5)
            ),
            Arrays.asList(PlayerInteraction.JOIN),
            "Custodian of the Vermilion Bird bloodline. Player could encounter during mid-to-late game when seeking divine beast inheritances."
        ),

        // --- CIV-08: Heavenly Fate Sect ---
        Faction.detailed("CIV-08", "Heavenly Fate Sect", "天运宗",
            CivType.SECT, Alignment.NEUTRAL,
            "Peak Third Step (All-Seer, suppressed to Heaven Blight)",
            "Planet Tian Yun",
            "Divination, celestial arts, fate manipulation",
            FactionState.DESTROYED, DestructionPattern.LEADERSHIP_ASSASSINATION, 5,
            Arrays.asList(
                new FactionMember("All-Seer", "全知者", "Leader", "Peak Third Step (suppressed to Heaven Blight)",
                    "Plots to absorb source origins of Wang Lin, Ling Tianhou, Blood Ancestor. NOT the same as Allheaven. Killed by Wang Lin ~Year 300."),
                new FactionMember("Tianyunzi", "天运子", "Clone / Artifact Spirit", "Unknown",
                    "Clone of All-Seer AND artifact spirit of the Realm-Defining Compass / Heaven Defying Bead. True body hides in Primordial Divine Realm."),
                new FactionMember("Zhao Xingsha", "赵星沙", "Purple Division Disciple", "Soul Transformation",
                    "Wang Lin's rival within the Purple Division."),
                new FactionMember("Sima Rufeng", "司马如风", "Purple Division Disciple", "Unknown", "Purple Division member."),
                new FactionMember("Zhao Xinming", "赵心明", "Purple Division (4th sister)", "Unknown", "Purple Division member."),
                new FactionMember("Chen Tao", "陈涛", "Purple Division (6th brother)", "Mid-Ascendant", "Purple Division member."),
                new FactionMember("Wang Lin", "王林", "7th Purple Division Disciple", "Soul Transformation",
                    "Recruited as 7th purple disciple. Later killed All-Seer.")
            ),
            Arrays.asList(
                new EconomyEntry("Mortal Population", "5-20 billion on Planet Tian Yun — enormous Joss Flame base.", 4),
                new EconomyEntry("Spirit Veins", "7 supreme + ~50-100 mid + ~500-1000 minor — richest single-planet vein network.", 4),
                new EconomyEntry("Celestial Jades", "Large stockpiles — Wang Lin received celestial jades for Soul Transformation breakthrough.", 5),
                new EconomyEntry("Divination Services", "Likely sells divination readings to other factions.", 3)
            ),
            Arrays.asList(
                new Relationship("CIV-09", "Co-Resident", "Blood Ancestor Yao Xinghai resides on Tian Yun.", 5),
                new Relationship("CIV-14", "Territorial Overlap", "Both in East Demon Spirit Sea area of Tian Yun.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Wang Lin joins as 7th purple disciple (~Year 100, Ch. 493)", null, 5),
                new CanonEvent("All-Seer's birthday banquet — Wang Lin battles Zhao Xingsha, Zhao Xinming, Chen Tao", null, 5),
                new CanonEvent("All-Seer's possession plot against Wang Lin (~Year 200-300)", null, 5),
                new CanonEvent("Wang Lin kills All-Seer (~Year 300)", null, 5),
                new CanonEvent("After All-Seer's death, 7 divisions persist as sub-faction ruins", null, 4)
            ),
            Arrays.asList(PlayerInteraction.JOIN),
            "One of the most important faction encounters. The 7-division structure creates 7 distinct paths. The All-Seer's scheming should be gradually discoverable. Post-All-Seer, divisions become shattered power structure."
        ),

        // --- CIV-09: Yao Family ---
        Faction.detailed("CIV-09", "Yao Family", "姚家",
            CivType.CLAN, Alignment.ANTAGONIST,
            "Third Step (Blood Ancestor Yao Xinghai)",
            "Southern Domain, Allheaven Star System",
            "Blood cultivation, Blood Soul Pill",
            FactionState.DESTROYED, DestructionPattern.CLAN_EXTERMINATION, 5,
            Arrays.asList(
                new FactionMember("Yao Xinghai", "姚星海", "Blood Ancestor / Patriarch", "Third Step",
                    "Killed by Wang Lin."),
                new FactionMember("Yao Xixue", "姚冰雪", "Key Member", "Unknown",
                    "Amnesiac; connected to the Blood Soul Pill.")
            ),
            Arrays.asList(
                new EconomyEntry("Blood Soul Pill", "Signature 'resurrection pill'. Refined by Blood Ancestor.", 5),
                new EconomyEntry("Blood Cultivation Materials", "Blood-essence from cultivators and beasts.", 4),
                new EconomyEntry("Territory", "Significant territory in the Southern Domain of Allheaven Star System.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-08", "Co-Resident on Tian Yun", "Blood Ancestor Yao Xinghai resides on Planet Tian Yun.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Yao Family sends kill-order on Wang Lin", null, 5),
                new CanonEvent("Wang Lin destroys multiple planets chasing the Yao Family", null, 5),
                new CanonEvent("Yao Xinghai killed by Wang Lin", null, 5)
            ),
            Arrays.asList(PlayerInteraction.CONQUER),
            "Major antagonist faction in Allheaven Star System arc. The player encounters kill-orders, blood-cultivation, and the Blood Soul Pill. Destruction creates power vacuum in Southern Domain."
        ),

        // --- CIV-10: Ancient Clan ---
        Faction.detailed("CIV-10", "Ancient Clan", "古族",
            CivType.RACE, Alignment.NEUTRAL,
            "Great Heavenly Venerable (Xuan Luo); Ancient Ancestor above all",
            "Immortal Astral Continent (IAC)",
            "Ancient Clan bloodline (God/Devil/Demon stars); Dao Gu, Dao Yi, Primordial lineages",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Ancient Ancestor", "古祖", "Ultimate Ancestor", "Above Great Heavenly Venerable",
                    "The clan's ultimate ancestor."),
                new FactionMember("Xuan Luo", "玄罗", "Great Heavenly Venerable", "Great Heavenly Venerable",
                    "Dao Gu lineage. Wang Lin's master."),
                new FactionMember("Dao Yi Great Celestial Venerable", "古意大天尊", "Celestial Venerable", "Celestial Venerable",
                    "Dao Yi lineage."),
                new FactionMember("Ji Du", "吉度", "Key Member", "Primordial Ancient lineage",
                    "Wang Lin's godson."),
                new FactionMember("Ye Mo", "叶墨", "Key Member", "Ancient God inheritance",
                    "Ancient God inheritor."),
                new FactionMember("Tu Si", "图思", "Deceased", "8-Star Ancient God",
                    "Body became the Land of the Ancient God. Tuo Si is his demonic thought."),
                new FactionMember("Wang Lin", "王林", "Dao Gu Lineage", "Paragon",
                    "Main God + Devil + Demon hybrid. Defeats both Gu Dao and Ancient Dao to become #1.")
            ),
            Arrays.asList(
                new EconomyEntry("Spirit Veins", "440+ on the IAC (7 continents) — richest vein network.", 4),
                new EconomyEntry("Mortal Population", "1-10 trillion on the IAC — largest Joss Flame base.", 4),
                new EconomyEntry("Ancient God Blood", "The bloodline itself IS the primary resource.", 5),
                new EconomyEntry("Earth Fire", "120+ Fire Veins on Heavenly Bull Continent alone.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-brief-18", "Rival Race", "Celestial Clan — both on IAC; both have Grand Empyreans.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Wang Lin becomes Dao Gu lineage disciple", null, 5),
                new CanonEvent("Wang Lin receives Three Rites at Great Soul Sect", null, 5),
                new CanonEvent("Wang Lin devours Earth Fire main vein -> Fire Essence True Body", null, 5),
                new CanonEvent("Wang Lin defeats both Gu Dao and Ancient Dao to become #1", null, 5)
            ),
            Arrays.asList(PlayerInteraction.JOIN),
            "Endgame faction of the Cave World arc. Player navigates three lineages (Dao Gu/Dao Yi/Primordial Ancient), explores Ancient Tomb and Ancestral Temple, witnesses Wang Lin's rise."
        ),

        // --- CIV-11: Teng Clan ---
        Faction.detailed("CIV-11", "Teng Clan", "藤族",
            CivType.CLAN, Alignment.ANTAGONIST,
            "Half-Step Deity Transformation (Teng Huayuan)",
            "Teng Family City, Country of Zhao, Planet Suzaku",
            "'Nine Great Nascent Souls' selection system",
            FactionState.DESTROYED, DestructionPattern.CLAN_EXTERMINATION, 5,
            Arrays.asList(
                new FactionMember("Teng Huayuan", "藤化元", "Patriarch", "Half-Step Deity Transformation",
                    "Elevated by Piao Nanzi. Killed by Wang Lin."),
                new FactionMember("Jimo Elder", "季莫长老", "Guest Elder", "Unknown", "Guest elder."),
                new FactionMember("Teng One-Teng Nine", "藤一至藤九", "Nine Great Nascent Souls", "Nascent Soul",
                    "All killed and refined into demon-puppets by Wang Lin."),
                new FactionMember("Teng Li", "藤丽", "Descendant", "Foundation Establishment",
                    "Great-great-granddaughter. Killed by Wang Lin at Tian Shui City.")
            ),
            Arrays.asList(
                new EconomyEntry("Territory", "Teng Family City + surrounding Zhao Country territory.", 5),
                new EconomyEntry("Mortal Population", "~50,000-150,000 in Teng Family City.", 4),
                new EconomyEntry("Pill Supply", "Access to Foundation Establishment pills.", 5)
            ),
            Arrays.asList(
                new Relationship("CIV-09", "EXTERMINATED", "Teng Huayuan exterminated Wang Family Village.", 5),
                new Relationship("CIV-01", "Dominated", "Teng Clan's power dwarfed Heng Yue's.", 4)
            ),
            Arrays.asList(
                new CanonEvent("Teng Huayuan exterminates Wang Family Village", null, 5),
                new CanonEvent("Wang Lin kills Teng Li at Tian Shui City, steals Foundation Establishment", null, 5),
                new CanonEvent("Wang Lin's 'Kill and Destroy the Heart': hunts Teng descendants, builds human-head tower", null, 5),
                new CanonEvent("All 9 Nascent Soul cultivators killed and refined into demon-puppets", null, 5),
                new CanonEvent("Teng Huayuan slain by Wang Lin", null, 5)
            ),
            Arrays.asList(PlayerInteraction.CONQUER),
            "The player's first major antagonist in Zhao Country. Teng Family City is a powerful, oppressive force. Destruction creates power vacuum and ruin site."
        ),

        // --- CIV-12: Cloud Sky Sect ---
        Faction.detailed("CIV-12", "Cloud Sky Sect", "云天宗",
            CivType.SECT, Alignment.RIGHTEOUS,
            "Nascent Soul (Li Muwan, Chen Bailiang)",
            "Chu Country, Planet Suzaku",
            "Cloud arts, flight techniques, alchemy (Li Muwan)",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Li Muwan", "李慕婉", "Sect Master", "Nascent Soul",
                    "Wang Lin's Dao companion. Wang Lin became Sect Master briefly then handed to her."),
                new FactionMember("Chen Bailiang", "陈百亮", "Elder", "Unknown", "Killed."),
                new FactionMember("Sun Zhenwei", "孙振伟", "Suitor (killed)", "Unknown",
                    "Killed by Wang Lin at Li Muwan's wedding."),
                new FactionMember("Zhou Lin", "周林", "Disciple", "Unknown", "9th-gen disciple.")
            ),
            Arrays.asList(
                new EconomyEntry("Alchemy", "Li Muwan's alchemy skill is the sect's signature asset.", 5),
                new EconomyEntry("Cloud Arts", "Flight techniques give mobility advantages.", 5)
            ),
            Arrays.asList(
                new Relationship("Wang Lin", "Love Interest's Sect",
                    "Wang Lin intervened at Li Muwan's wedding; became Sect Master briefly.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Wang Lin kills Sun Zhenwei at Li Muwan's wedding", null, 5),
                new CanonEvent("Wang Lin becomes Sect Master briefly, hands to Li Muwan", null, 5)
            ),
            Arrays.asList(PlayerInteraction.TRADE),
            "Important primarily because of Li Muwan. Player encounters during Chu Country arc. Alchemy specialization makes it a good source of pills."
        ),

        // --- CIV-13: Great Soul Sect ---
        Faction.detailed("CIV-13", "Great Soul Sect", "大魂宗",
            CivType.SECT, Alignment.NEUTRAL,
            "Third Step+",
            "Heavenly Bull Continent, Immortal Astral Continent",
            "Soul cultivation, Ghostly Sail, Fire Element, Tianniu Pearl",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Wang Lin", "王林", "Elder", "Third Step",
                    "Wang Lin becomes an elder of the Great Soul Sect.")
            ),
            Arrays.asList(
                new EconomyEntry("Earth Fire Main Vein", "At Great Soul Sect location; Wang Lin devoured it.", 5),
                new EconomyEntry("Fire Veins", "120+ on Heavenly Bull Continent.", 4)
            ),
            Arrays.asList(
                new Relationship("CIV-10", "Same Continent", "Both on Heavenly Bull Continent, IAC.", 4)
            ),
            Arrays.asList(
                new CanonEvent("Wang Lin becomes elder", null, 5),
                new CanonEvent("Wang Lin receives Three Rites", null, 5),
                new CanonEvent("Wang Lin devours Earth Fire main vein -> Fire Essence True Body", null, 5)
            ),
            Arrays.asList(PlayerInteraction.JOIN),
            "Mid-to-late game faction on IAC. Ghostly Sail (source of Restriction Essence) is a major artifact. Earth Fire vein system makes region geologically significant."
        ),

        // --- CIV-14: Soul Refining Tribe ---
        Faction.detailed("CIV-14", "Soul Refining Tribe", "炼魂部族",
            CivType.TRIBE, Alignment.RIGHTEOUS,
            "Body refining (Thirteen, Huo Pao)",
            "East Demon Spirit Sea, Planet Tian Yun",
            "Soul Refining Sect heritage; body refining",
            FactionState.ACTIVE, null, 4,
            Arrays.asList(
                new FactionMember("Ouyang Hua", "欧阳华", "Chief", "Unknown", "Tribe chief."),
                new FactionMember("Thirteen", "十三", "Disciple", "Body Refining",
                    "Originally Mountain Valley Tribe native. Wang Lin's 1st disciple."),
                new FactionMember("Huo Pao", "火炮", "Disciple", "Body Refining", "Wang Lin's disciple.")
            ),
            Arrays.asList(
                new EconomyEntry("Soul Refining Heritage", "Primary economy is cultural — Soul Refining Sect techniques from Wang Lin.", 5),
                new EconomyEntry("Population", "1M+ people — a significant tribal nation.", 5),
                new EconomyEntry("Soul Banners", "Restored Wang Lin's One Billion Soul Flag.", 5)
            ),
            Arrays.asList(
                new Relationship("CIV-02", "Heritage Successor", "Direct lineage from Dun Tian's Soul Refining Sect.", 5),
                new Relationship("CIV-08", "Territorial Proximity", "Both on Planet Tian Yun.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Mountain Valley Tribe -> Wang Lin taught Soul Refining heritage -> became Soul Refining Tribe", null, 5),
                new CanonEvent("Soul Refining Tribe restores Wang Lin's One Billion Soul Flag", null, 5)
            ),
            Arrays.asList(PlayerInteraction.TRADE),
            "A tribal nation derived from Wang Lin's heritage. Player encounters in East Demon Spirit Sea. They revere Wang Lin. Body-refining specialization and soul banner techniques make them valuable allies."
        ),

        // --- CIV-15: Origin Sect / Guiyuan Sect ---
        Faction.detailed("CIV-15", "Origin Sect", "归一宗",
            CivType.SECT, Alignment.NEUTRAL,
            "Third Step (Ling Tianhou)",
            "Cloud Sea Star System",
            "Rank 6 cultivation; Dao Spell techniques",
            FactionState.ACTIVE, null, 4,
            Arrays.asList(
                new FactionMember("Ling Tianhou", "凌天侯", "Sect Master", "Third Step",
                    "Rank 8 at time. One of the 'Three Great Cultivators'.")
            ),
            NO_ECONOMY,
            Arrays.asList(
                new Relationship("CIV-17", "Neighbor", "Both in Cloud Sea Star System.", 4)
            ),
            NO_EVENTS,
            Arrays.asList(PlayerInteraction.JOIN),
            "Wang Lin's primary faction in the Cloud Sea Star System. Player would encounter during the Cloud Sea arc."
        ),

        // --- CIV-16: Da Lou Sword Sect ---
        Faction.detailed("CIV-16", "Da Lou Sword Sect", "大楼剑宗",
            CivType.SECT, Alignment.RIGHTEOUS,
            "Third Step (Ling Tianhou connection)",
            "Cloud Sea Star System",
            "Sword cultivation",
            FactionState.ACTIVE, null, 4,
            NO_MEMBERS,
            NO_ECONOMY,
            Arrays.asList(
                new Relationship("CIV-15", "Neighbor", "Both in Cloud Sea Star System.", 4)
            ),
            NO_EVENTS,
            Arrays.asList(PlayerInteraction.JOIN),
            "Cloud Sea Star System sword cultivation faction. Major regional power."
        ),

        // --- CIV-17: Cultivation Alliance ---
        Faction.detailed("CIV-17", "Cultivation Alliance", "修真联盟",
            CivType.ALLIANCE, Alignment.RIGHTEOUS,
            "Nirvana Scryer tier (multiple elders)",
            "Planet Suzaku",
            "Inter-sect governance, law enforcement",
            FactionState.ACTIVE, null, 5,
            Arrays.asList(
                new FactionMember("Lu Yun", "陆云", "Infiltrator", "Soul Formation",
                    "Infiltrated Alliance HQ on behalf of All-Seer.")
            ),
            NO_ECONOMY,
            Arrays.asList(
                new Relationship("CIV-05", "Supra-National Authority", "Oversees multiple nation-states including Vermilion Bird Country.", 4)
            ),
            Arrays.asList(
                new CanonEvent("All-Seer infiltrated Alliance HQ via Lu Yun", null, 5)
            ),
            Arrays.asList(PlayerInteraction.IGNORE),
            "The supra-national authority on Planet Suzaku. Governs inter-sect law. Player interacts indirectly through its policies."
        ),

        // --- CIV-18: Seven Paths Sect (Seven-Colored Daoist) ---
        Faction.detailed("CIV-18", "Seven Paths Sect", "七彩道",
            CivType.SECT, Alignment.BEYOND,
            "Paragon (Seven-Colored Daoist)",
            "Seven-Colored Realm, Cloud Sea Star System / Cave World owner",
            "Cave World ownership; Joss Flame harvest; 108 nails seal",
            FactionState.HIDDEN, null, 5,
            Arrays.asList(
                new FactionMember("Seven-Colored Daoist", "七彩道人", "Owner / Daoist", "Paragon",
                    "TRUE antagonist of RI. Owns the Cave World as a Joss Flame farm. NOT Allheaven."),
                new FactionMember("Master Ashen Pine", "灰烬松", "Guardian", "Unknown",
                    "One of the Seven-Colored Realm's guardians.")
            ),
            Arrays.asList(
                new EconomyEntry("Joss Flame", "30% of ALL Cave World Joss Flame siphoned via the Realm-Sealing Grand Array.", 5),
                new EconomyEntry("108 Nails", "Control mechanism embedded across the Cave World.", 5),
                new EconomyEntry("Cave World Ownership", "The entire Cave World is this entity's farm.", 5)
            ),
            Arrays.asList(
                new Relationship("CIV-10", "Unknowing Livestock", "The Ancient Clan, despite its power, is still inside the Cave World.", 5)
            ),
            Arrays.asList(
                new CanonEvent("Seven-Colored Daoist established the Cave World as a Joss Flame farm", null, 5),
                new CanonEvent("Wang Lin defeated the Seven-Colored Daoist", null, 5)
            ),
            Arrays.asList(PlayerInteraction.CONQUER),
            "THE true antagonist structure. NOT Allheaven (which is ISSTH-specific). The Cave World owner who farms all civilizations for Joss Flame. Hidden until late-game."
        ),

        // ═══════════════════════════════════════════════════════════════
        // 27 BRIEF FACTIONS
        // ═══════════════════════════════════════════════════════════════

        Faction.brief("CIV-brief-01", "Xuan Dao Sect", "玄道宗",
            CivType.SECT, "Unknown", "Country of Zhao, Planet Suzaku",
            "Orthodox cultivation", FactionState.ACTIVE, 4,
            "Zhao Country neighbor of Heng Yue. Wang Lin broke through to Soul Formation on a mountain near this sect."),

        Faction.brief("CIV-brief-02", "Luo He Sect", "洛河宗",
            CivType.SECT, "Unknown", "Planet Suzaku",
            "Cultivation", FactionState.ACTIVE, 4,
            "Minor sect on Planet Suzaku. Limited canon detail."),

        Faction.brief("CIV-brief-03", "Tian Yu Sect", "天宇宗",
            CivType.SECT, "Unknown", "Planet Suzaku",
            "Cultivation", FactionState.ACTIVE, 4,
            "Minor sect on Planet Suzaku. Limited canon detail."),

        Faction.brief("CIV-brief-04", "Qihuang Sect", "岐黄宗",
            CivType.SECT, "Unknown", "Planet Suzaku",
            "Cultivation", FactionState.ACTIVE, 4,
            "Minor sect on Planet Suzaku. Limited canon detail."),

        Faction.brief("CIV-brief-05", "Poison Palace", "毒殿",
            CivType.SECT, "Unknown", "Planet Suzaku",
            "Poison cultivation", FactionState.ACTIVE, 4,
            "Poison cultivation specialization. Demonic alignment likely."),

        Faction.brief("CIV-brief-06", "Dao Devil Sect", "道魔宗",
            CivType.SECT, "Third Step+", "IAC (Green Devil Continent)",
            "Devil cultivation, Green Devil sacrifice", FactionState.DESTROYED, 5,
            "DESTROYED by Wang Lin — he reversed the Green Devil sacrifice ritual and annihilated them. Total Annihilation pattern."),

        Faction.brief("CIV-brief-07", "Canglong Sect", "苍龙宗",
            CivType.SECT, "Unknown", "Heavenly Bull Continent, IAC",
            "Dragon cultivation; Earth Fire Dragon", FactionState.ACTIVE, 4,
            "Neighbor of Great Soul Sect on Heavenly Bull Continent. Earth Fire Dragon connection."),

        Faction.brief("CIV-brief-08", "Dong Lin Sect", "东林宗",
            CivType.SECT, "Third Step+", "Dong Lin Pool area, IAC",
            "Cultivation (ancient god-tier entity sealed beneath)", FactionState.DESTROYED, 4,
            "DESTROYED before Wang Lin's arrival. Tianyunzi's handwriting found here. Pre-Arrival destruction pattern."),

        Faction.brief("CIV-brief-09", "Wang Clan", "王族",
            CivType.MORTAL_CLAN, "None (mortal)", "Wang Family Village, Country of Zhao",
            "Carpentry", FactionState.DESTROYED, 5,
            "EXTERMINATED by Teng Huayuan. Wang Lin's mortal origins. Key members: Wang Tianshui (father), Zhou Tingsu (mother), Fourth Uncle. Later restored through Wang Lin's influence."),

        Faction.brief("CIV-brief-10", "Tattoo Clan", "纹族",
            CivType.CLAN, "Unknown", "Unknown",
            "Tattoo-based cultivation (Beast Skin Tattoo, Tattoo Talisman Speed Boost)", FactionState.ACTIVE, 3,
            "Unique cultivation method. Beast Skin Tattoo and Tattoo Talisman Speed Boost."),

        Faction.brief("CIV-brief-11", "Forsaken Immortal Clan", "遗仙族",
            CivType.CLAN, "Late Nascent Soul (Yun Quezi)", "Vermilion Bird Star (hidden)",
            "Curse mastery, reincarnation manipulation", FactionState.HIDDEN, 4,
            "Hidden faction. Key members: Yun Quezi (2nd Ancestor), Qian Pinghai (13th-Gen Vermilion Bird). Curse/reincarnation mechanics source."),

        Faction.brief("CIV-brief-12", "Moon Devourer Clan", "吞月族",
            CivType.CLAN, "Third Step", "Unknown",
            "Moon-themed cultivation", FactionState.ACTIVE, 4,
            "Sent mysterious youth + old man to interrupt Wang Lin's Ancient God recognition. Beaten back."),

        Faction.brief("CIV-brief-13", "Scatter Thunder Clan", "散雷族",
            CivType.CLAN, "Third Step (5th Heaven Blight head elder)", "Unknown",
            "Thunder cultivation; Eternal Thunderbolt", FactionState.DESTROYED, 4,
            "DESTROYED by Wang Lin. He killed their Sect Leader + 5th Heaven Blight head elder; devoured 8 ancient thunder dragons; Eternal Thunderbolt absorbed -> Wang Lin reached Nirvana Shatterer late stage."),

        Faction.brief("CIV-brief-14", "Fire Sparrow Clan", "火雀族",
            CivType.CLAN, "Unknown", "Vermilion Bird Starfield",
            "Fire sparrow cultivation", FactionState.ACTIVE, 4,
            "Vermilion Bird Starfield sub-faction. 3rd-Gen Evil Sparrow mentioned."),

        Faction.brief("CIV-brief-15", "Giant Demon Clan", "巨魔族",
            CivType.CLAN, "Unknown", "Unknown",
            "Ancient God's Blood", FactionState.ACTIVE, 4,
            "Their Ancestor was killed for Ancient God's Blood. Chi Hu gave Wang Lin the Star Compass."),

        Faction.brief("CIV-brief-16", "Dark Scorpion Clan", "暗蝎族",
            CivType.CLAN, "Unknown", "Unknown",
            "Scorpion cultivation", FactionState.ACTIVE, 4,
            "Wang Lin is 'Ruler of the Dark Scorpion Clan.' Unique political relationship."),

        Faction.brief("CIV-brief-17", "Zhen Family / Zhan Family", "甄家/战家",
            CivType.CLAN, "Third Step (Zhan Xingye)", "IAC",
            "Battle Scroll", FactionState.ACTIVE, 4,
            "Battle Scroll (3 scrolls merged into golden 'Battle' word by Wang Lin). Zhan Xingye is a key member."),

        Faction.brief("CIV-brief-18", "Celestial Clan", "仙族",
            CivType.RACE, "Grand Empyrean (Immortal Ancestor)", "IAC",
            "Immortal/celestial cultivation; Nine Suns", FactionState.ACTIVE, 4,
            "Ancient Clan's rival race on the IAC. Key members: Immortal Ancestor, Dao Yi, Sea Child, Jiu Di (Grand Empyrean), Song Tian, Yun Yifeng, Purple Dawn. Wang Lin = '49th Ascendant Empyrean'."),

        Faction.brief("CIV-brief-19", "Great Wang Dynasty", "大王朝",
            CivType.DYNASTY, "Unknown", "Planet Suzaku",
            "Governance", FactionState.ACTIVE, 4,
            "Wang Lin's dynasty on Planet Suzaku. Player-built or Wang Lin-built political entity."),

        Faction.brief("CIV-brief-20", "Everlasting Sect", "永恒宗",
            CivType.SECT, "Third Step+ (Blood Sword)", "Cloud Sea Star System",
            "Blood-element sword cultivation", FactionState.ACTIVE, 4,
            "Cloud Sea Star System faction. Wang Lin was about to represent them in rank 8 tournament before Rank 9 God Sect canceled it."),

        Faction.brief("CIV-brief-21", "Ghost Sect", "鬼宗",
            CivType.SECT, "Unknown", "Cloud Sea Star System",
            "Ghost/spirit cultivation", FactionState.ACTIVE, 4,
            "Demonic faction. Sent Li Qianmei deep into a spatial realm with powerful beasts. Li Qianmei connection."),

        Faction.brief("CIV-brief-22", "Rank 9 God Sect", "九等神宗",
            CivType.SECT, "Third Step+ (Daoist Water / Shui Daozi)", "Cloud Sea Star System",
            "Rank 9 cultivation", FactionState.DESTROYED, 5,
            "Major antagonist on Cloud Sea. Daoist Water from the Outer Realm. Attacked Wang Lin sensing Lord of the Sealed Realm's aura. Daoist Water killed by Wang Lin (~Year 500-600, Ch. 1509)."),

        Faction.brief("CIV-brief-23", "Treasured Jade Sect", "宝玉宗",
            CivType.SECT, "Third Step+ (multiple Nirvana Shatterer old monsters)", "Cloud Sea Star System",
            "Auctions, treasure exchanges", FactionState.ACTIVE, 4,
            "Merchant/auction sect. Li Qianmei invited Wang Lin to auction. Major trade hub on Cloud Sea."),

        Faction.brief("CIV-brief-24", "Blue Silk Clan", "蓝丝族",
            CivType.CLAN, "Dao Master (Blue Dream) — Void Tribulant+", "Blue Silk Clan Star Domain",
            "Dao Art Fusion, Light Shadow Shield, Overturn Heaven Seal", FactionState.ACTIVE, 4,
            "High-tier clan. Wang Lin learned Dao Art Fusion + Light Shadow Shield from Blue Dream."),

        Faction.brief("CIV-brief-25", "Chosen Immortal Clan", "遗仙族",
            CivType.CLAN, "Unknown", "Thunder Celestial Realm",
            "Immortal cultivation", FactionState.ACTIVE, 4,
            "Enslaved in Thunder Celestial Realm. Wang Lin helped them escape. Rescue quest faction."),

        Faction.brief("CIV-brief-26", "Wu Xuan Country", "吴玄国",
            CivType.DYNASTY, "None (mortal)", "IAC",
            "Mortal governance", FactionState.ACTIVE, 4,
            "Situ Nan reincarnated here as Grand Marshal 'Si Nan' (Southern Prince). Mortal nation on the IAC."),

        Faction.brief("CIV-brief-27", "War Shrine Sect", "战神殿",
            CivType.SECT, "Unknown", "Planet Suzaku",
            "Divine Path Technique (avatar ability); war/spirit refinement", FactionState.ACTIVE, 4,
            "Source of Divine Path (avatar) technique. Wang Lin was a former Direct Disciple. Wang Lin's early background.")
    ));

    // ─── Query Methods ──────────────────────────────────────────────────

    /**
     * Get all factions.
     */
    public static List<Faction> getAllFactions() {
        return ALL_FACTIONS;
    }

    /**
     * Get the 18 fully-detailed factions.
     */
    public static List<Faction> getDetailedFactions() {
        return ALL_FACTIONS.stream()
            .filter(f -> !f.isBrief)
            .collect(Collectors.toList());
    }

    /**
     * Get the 27 brief factions.
     */
    public static List<Faction> getBriefFactions() {
        return ALL_FACTIONS.stream()
            .filter(f -> f.isBrief)
            .collect(Collectors.toList());
    }

    /**
     * Get a faction by its ID (e.g. "CIV-01", "CIV-brief-13").
     * Returns null if not found.
     */
    public static Faction getFactionById(String id) {
        for (Faction f : ALL_FACTIONS) {
            if (f.id.equals(id)) return f;
        }
        return null;
    }

    /**
     * Get all factions at a given lifecycle state.
     */
    public static List<Faction> getFactionsByState(FactionState state) {
        return ALL_FACTIONS.stream()
            .filter(f -> f.state == state)
            .collect(Collectors.toList());
    }

    /**
     * Get all destroyed factions — bridge to Ecology layer (ruin sites).
     * Per Axiom C6, every destroyed faction creates a ruin ecology.
     */
    public static List<Faction> getDestroyedFactions() {
        return getFactionsByState(FactionState.DESTROYED);
    }

    /**
     * Get all heritage sites — destroyed factions with recoverable techniques/artifacts.
     */
    public static List<Faction> getHeritageSites() {
        return getFactionsByState(FactionState.HERITAGE_SITE);
    }

    /**
     * Get factions by alignment.
     */
    public static List<Faction> getFactionsByAlignment(Alignment alignment) {
        return ALL_FACTIONS.stream()
            .filter(f -> f.alignment == alignment)
            .collect(Collectors.toList());
    }

    /**
     * Get factions by organizational type.
     */
    public static List<Faction> getFactionsByType(CivType type) {
        return ALL_FACTIONS.stream()
            .filter(f -> f.type == type)
            .collect(Collectors.toList());
    }

    /**
     * Get factions located in a specific region (headquarters contains the query string).
     * Case-insensitive substring match.
     */
    public static List<Faction> getFactionsInRegion(String region) {
        String q = region.toLowerCase();
        return ALL_FACTIONS.stream()
            .filter(f -> f.headquarters.toLowerCase().contains(q))
            .collect(Collectors.toList());
    }

    /**
     * Get the 5 regional power structures (Zhao Country, Sea of Devils,
     * Planet Tian Yun, Cloud Sea Star System, IAC) with their factions.
     * Returns a map of region name -> list of factions in that region.
     */
    public static Map<String, List<Faction>> getRegionalPowerStructures() {
        Map<String, List<Faction>> map = new LinkedHashMap<>();
        for (String region : REGIONAL_STRUCTURES) {
            map.put(region, getFactionsInRegion(region));
        }
        return map;
    }

    /**
     * Get all relationships for a faction.
     */
    public static List<Relationship> getRelationships(String factionId) {
        Faction f = getFactionById(factionId);
        if (f == null || f.relationships == null) return Collections.emptyList();
        return f.relationships;
    }

    /**
     * Get all canon events for a faction.
     */
    public static List<CanonEvent> getEvents(String factionId) {
        Faction f = getFactionById(factionId);
        if (f == null || f.events == null) return Collections.emptyList();
        return f.events;
    }

    /**
     * Get factions that have a specific relationship type with the given faction.
     */
    public static List<Relationship> getRelationshipsByType(String factionId, String relationType) {
        return getRelationships(factionId).stream()
            .filter(r -> r.relationType.equalsIgnoreCase(relationType))
            .collect(Collectors.toList());
    }

    /**
     * Get the sect power equation value for a faction (Axiom C2).
     * This is a rough estimate — the Forge mod would calculate this from
     * actual game state. Here we return a qualitative tier string.
     *
     * <p>Per Axiom C2: Power = PeakRealm x DiscipleCount x ResourceControl.
     * This method maps the peak realm to a tier multiplier as a convenience.
     */
    public static String getPowerTier(String factionId) {
        Faction f = getFactionById(factionId);
        if (f == null) return "UNKNOWN";
        String r = f.peakRealm.toLowerCase();
        if (r.contains("paragon") || r.contains("beyond")) return "S (Cave World Owner)";
        if (r.contains("third step") || r.contains("great heavenly")) return "SS (Transcendent)";
        if (r.contains("nirvana") || r.contains("ascendant")) return "S (High Star System)";
        if (r.contains("void flame") || r.contains("heaven blight")) return "S (Planet Sovereign)";
        if (r.contains("soul transformation")) return "A (Star System Power)";
        if (r.contains("nascent soul") || r.contains("deity transformation")) return "B (Regional Power)";
        if (r.contains("soul formation")) return "C (Country-Level)";
        if (r.contains("core formation")) return "D (Sect-Level)";
        if (r.contains("foundation")) return "E (Local)";
        if (r.contains("qi condensation") || r.contains("mortal")) return "F (Minor)";
        return "UNKNOWN";
    }

    /**
     * Get the trade network path for a faction (Axiom C5).
     * Returns the list of currency tiers this faction uses,
     * from lowest to highest.
     */
    public static List<CurrencyTier> getCurrencyForFaction(String factionId) {
        Faction f = getFactionById(factionId);
        if (f == null) return Collections.emptyList();
        String r = f.peakRealm.toLowerCase();
        // Economy flows up: a faction uses all currencies up to its tier
        int maxIndex = 0;
        if (r.contains("domain") || r.contains("comprehension")) maxIndex = 5;
        else if (r.contains("third step") || r.contains("nirvana")) maxIndex = 4;
        else if (r.contains("soul transformation") || r.contains("ascendant")) maxIndex = 3;
        else if (r.contains("nascent soul") || r.contains("deity")) maxIndex = 2;
        else if (r.contains("core formation")) maxIndex = 1;
        else maxIndex = 0;
        return CURRENCY_HIERARCHY.subList(0, maxIndex + 1);
    }

    /**
     * Check if a faction name is FORBIDDEN (canon-silent entity).
     * Returns the ForbiddenEntity if found, null otherwise.
     */
    public static ForbiddenEntity isForbiddenName(String name) {
        for (ForbiddenEntity fe : FORBIDDEN_ENTITIES) {
            if (fe.forbiddenName.equalsIgnoreCase(name)) return fe;
        }
        return null;
    }

    /**
     * Get the destruction pattern for a destroyed faction.
     * Returns null if the faction is not destroyed or has no known pattern.
     */
    public static DestructionPattern getDestructionPattern(String factionId) {
        Faction f = getFactionById(factionId);
        if (f == null) return null;
        return f.destructionPattern;
    }

    /**
     * Get all heritage transfer paths (Part 6.5).
     * These are the canon-attested paths where techniques/artifacts/bloodlines
     * transferred from one faction to another.
     */
    public static List<HeritagePath> getHeritagePaths() {
        return HERITAGE_PATHS;
    }

    /**
     * Get all heritage transfer paths involving a specific faction name.
     */
    public static List<HeritagePath> getHeritagePathsInvolving(String name) {
        return HERITAGE_PATHS.stream()
            .filter(h -> h.description.contains(name))
            .collect(Collectors.toList());
    }
}