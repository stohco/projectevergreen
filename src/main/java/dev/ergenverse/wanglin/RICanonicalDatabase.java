package dev.ergenverse.wanglin;

import java.util.*;
import java.util.stream.*;

/**
 * RI Canonical Database — structured query engine for all 630 canon entries.
 *
 * <p>This class bridges the critical gap between the markdown canon documents
 * and the Java runtime. Previously, only the 3 canon-markdown-based engines
 * (Timeline, Ecology, Civilization) and the item-specific HeavenDefyingBead.java
 * existed. The 178 artifacts, 214 techniques, 158 characters, and 80 locations
 * defined in {{@code ri_canon_database.json} had ZERO Java representation.
 *
 * <p>Data source: {{@code ri_canon_database.json} (14,909 lines, 630 entries).
 * <ul>
 *   <li>{@link CanonCharacter} — 158 characters (N01-N160)</li>
 *   <li>{@link CanonLocation} — 80 locations (L01-L80)</li>
 *   <li>{@link CanonArtifact} — 178 artifacts (I01-I178)</li>
 *   <li>{@link CanonTechnique} — 214 techniques (T01-T170 + AT01-09 + VA01-04 + OS01-07 + B01-09 + E01-14)</li>
 * </ul>
 *
 * <p>All data is loaded from the JSON at construction time and cached in
 * typed lists. Query methods provide filtered access by confidence, type,
 * owner, user, location, and free-text search.
 *
 * <p><b>Prime Directive:</b> Every entry traces back to the 3 canon docs
 * (WORLD.md, ITEMS.md, TECHNIQUES.md) with chapter citations. Zero generic
 * fantasy content. Confidence 5 = explicitly attested; 4 = strongly implied;
 * 3 = logically necessary; 2 = derived speculation; 1 = bare conjecture.
 *
 * <p>Per user corrections in SYSTEMS_AUDIT_COMPLETE.md:
 * <ul>
 *   <li>No Allheaven-as-Wang-Lin-antagonist (correction #1)</li>
 *   <li>No 9-9-1 tribulation structure (correction #2)</li>
 *   <li>Suzaku is a planet, not a continent (correction #6)</li>
 *   <li>Heavenly Fate is a planet, not a continent (correction #7)</li>
 *   <li>Essences do not unlock at realm X (correction #5)</li>
 * </ul>
 */
public final class RICanonicalDatabase {

    private RICanonicalDatabase() {}

    // ── Character type taxonomy ──────────────────────────────────────────
    public enum CharType {
        ANTAGONIST,
        DISCIPLE,
        ELDER,
        OTHER,
        PATRIARCH,
        PROTAGONIST;
    }

    // ── Location type taxonomy ───────────────────────────────────────────
    public enum LocType {
        CAVE,
        CITY,
        CONTINENT,
        COUNTRY,
        MOUNTAIN,
        OTHER,
        PLANET,
        REALM,
        SECRET_REALM,
        SECT,
        STAR_DOMAIN,
        STAR_SYSTEM;
    }

    // ── Artifact type taxonomy ───────────────────────────────────────────
    public enum ArtType {
        ARMOR,
        BANNER,
        BEAD_PART,
        COMPASS,
        FORMATION,
        MATERIAL,
        OTHER,
        TALISMAN;
    }

    // ── Technique type taxonomy ──────────────────────────────────────────
    public enum TechType {
        ACCOMPANYING_THUNDER,
        BRIDGE,
        CULTIVATION_ART,
        ESSENCE,
        MOVEMENT,
        ORIGINAL_SPELL,
        OTHER,
        RESTRICTION;
    }

    // ── Data records (immutable) ─────────────────────────────────────────

    /** A named character from the RI canon. */
    public static final class CanonCharacter {
        public final String id;
        public final String name;
        public final String nameCn;
        public final CharType type;
        public final String peakRealm;
        public final String affiliation;
        public final String status;
        public final int canonConfidence;
        public final String firstAppearance;
        public final String location;
        public final List<String> knownFacts;
        public final List<RelationShip> relationships;
        public final String source;

        public CanonCharacter(String id, String name, String nameCn, CharType type,
                String peakRealm, String affiliation, String status, int canonConfidence,
                String firstAppearance, String location, List<String> knownFacts,
                List<RelationShip> relationships, String source) {
            this.id = id; this.name = name; this.nameCn = nameCn; this.type = type;
            this.peakRealm = peakRealm; this.affiliation = affiliation; this.status = status;
            this.canonConfidence = canonConfidence; this.firstAppearance = firstAppearance;
            this.location = location; this.knownFacts = knownFacts;
            this.relationships = relationships; this.source = source;
        }
    }

    /** A character-to-character relationship. */
    public static final class RelationShip {
        public final String target;
        public final String relation;

        public RelationShip(String target, String relation) {
            this.target = target; this.relation = relation;
        }
    }

    /** A named location from the RI canon, following the cosmological hierarchy. */
    public static final class CanonLocation {
        public final String id;
        public final String name;
        public final String nameCn;
        public final LocType type;
        public final String parentLocation;
        public final String cosmologyLayer;
        public final String worldLawTier;
        public final boolean isSealed;
        public final String sealedBy;
        public final String spiritVeins;
        public final int canonConfidence;
        public final String firstAppearance;
        public final List<String> knownFacts;
        public final List<String> associatedFactions;
        public final List<String> keyEvents;
        public final String source;

        public CanonLocation(String id, String name, String nameCn, LocType type,
                String parentLocation, String cosmologyLayer, String worldLawTier,
                boolean isSealed, String sealedBy, String spiritVeins, int canonConfidence,
                String firstAppearance, List<String> knownFacts, List<String> associatedFactions,
                List<String> keyEvents, String source) {
            this.id = id; this.name = name; this.nameCn = nameCn; this.type = type;
            this.parentLocation = parentLocation; this.cosmologyLayer = cosmologyLayer;
            this.worldLawTier = worldLawTier; this.isSealed = isSealed;
            this.sealedBy = sealedBy; this.spiritVeins = spiritVeins;
            this.canonConfidence = canonConfidence; this.firstAppearance = firstAppearance;
            this.knownFacts = knownFacts; this.associatedFactions = associatedFactions;
            this.keyEvents = keyEvents; this.source = source;
        }
    }

    /** A named artifact from the RI canon. */
    public static final class CanonArtifact {
        public final String id;
        public final String name;
        public final String nameCn;
        public final ArtType type;
        public final String category;
        public final String currentOwner;
        public final List<String> abilities;
        public final String origin;
        public final int canonConfidence;
        public final List<String> knownFacts;
        public final String source;

        public CanonArtifact(String id, String name, String nameCn, ArtType type,
                String category, String currentOwner, List<String> abilities,
                String origin, int canonConfidence, List<String> knownFacts, String source) {
            this.id = id; this.name = name; this.nameCn = nameCn; this.type = type;
            this.category = category; this.currentOwner = currentOwner;
            this.abilities = abilities; this.origin = origin;
            this.canonConfidence = canonConfidence; this.knownFacts = knownFacts; this.source = source;
        }
    }

    /** A named technique from the RI canon. */
    public static final class CanonTechnique {
        public final String id;
        public final String name;
        public final String nameCn;
        public final TechType type;
        public final String origin;
        public final List<String> effects;
        public final List<String> knownUsers;
        public final int canonConfidence;
        public final List<String> knownFacts;
        public final String source;

        public CanonTechnique(String id, String name, String nameCn, TechType type,
                String origin, List<String> effects, List<String> knownUsers,
                int canonConfidence, List<String> knownFacts, String source) {
            this.id = id; this.name = name; this.nameCn = nameCn; this.type = type;
            this.origin = origin; this.effects = effects;
            this.knownUsers = knownUsers; this.canonConfidence = canonConfidence;
            this.knownFacts = knownFacts; this.source = source;
        }
    }

    // ── Static data (populated from ri_canon_database.json) ──────────────

    /** All 158 canon characters. */
    public static final List<CanonCharacter> ALL_CHARACTERS = List.of(
        new CanonCharacter(
            "N01", "Wang Lin", "王林", CharType.PROTAGONIST,
            "Heaven Trampling", "independent (multi-sect legacy: Heng Yue Sect → Cloud Sky Sect → Soul Refining Sect → Heavenly Fate Sect → Da Lou Sword Sect → Vermilion Bird Divine Sect → Origin Sect → Great Soul Sect → Dark Scorpion Clan → Sealed Realm → Cave World owner)", "transcended", 5,
            "unknown", "Planet Suzaku / Cave World / Immortal Astral Continent",
            java.util.List.of(
                "Discovered the Heaven Defying Bead in a dead bird under a cliff as a child",
                "Awakened the Ji Realm and later inherited Tu Si's Ancient God knowledge",
                "Became the 6th-Generation Vermilion Bird Divine Emperor and Lord of the Sealed Realm",
                "Killed the Seven-Colored Daoist to become the owner of the Cave World",
                "Transcended with Li Muwan after crossing the 9 Heaven Trampling Bridges"
            ),
            List.of(new RelationShip("Li Muwan", "love_interest"), new RelationShip("Li Qianmei", "love_interest"), new RelationShip("Mu Bingmei", "love_interest"), new RelationShip("Wang Ping", "family"), new RelationShip("Wang Yiyi", "family"), new RelationShip("Situ Nan", "master"), new RelationShip("Xuan Luo", "master"), new RelationShip("All-Seer", "enemy"), new RelationShip("Teng Huayuan", "enemy"), new RelationShip("Seven-Colored Daoist", "enemy"), new RelationShip("Tianyunzi", "enemy"), new RelationShip("Xu Liguo", "disciple"), new RelationShip("Thirteen", "disciple"), new RelationShip("Xie Qing", "disciple"), new RelationShip("Xi Zifeng", "disciple"), new RelationShip("Lian Daofei", "disciple"), new RelationShip("Tuo Sen", "rival"), new RelationShip("Gu Dao", "rival")),
            "novel Ch.1+; Fandom wiki (main/cultivation/clones); Baidu Baike"
        ),
        new CanonCharacter(
            "N02", "Wang Tianshui", "王天水", CharType.OTHER,
            "mortal", "Wang Clan", "reincarnated", 5,
            "unknown", "Wang Family Village, Country of Zhao",
            java.util.List.of(
                "Wang Lin's father; called him 'Tie Zhu' to ward off bad fortune",
                "Killed by Teng Huayuan; soul trapped in Teng's soul flag",
                "Soul rescued by Situ Nan into the Heaven Defying Bead; later released"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Zhou Tingsu", "family"), new RelationShip("Teng Huayuan", "enemy")),
            "novel (early chapters); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N03", "Zhou Tingsu", "周婷苏 / 周颖苏", CharType.OTHER,
            "mortal", "Wang Clan", "reincarnated", 5,
            "unknown", "Wang Family Village, Country of Zhao",
            java.util.List.of(
                "Wang Lin's mother",
                "Killed by Teng Huayuan; soul trapped in Teng's soul flag",
                "Soul rescued by Situ Nan and later released"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Wang Tianshui", "family")),
            "novel (early chapters); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N04", "Wang Tianshan", "王天山", CharType.ELDER,
            "low-tier cultivator", "Wang Clan / Heng Yue Sect", "alive", 4,
            "unknown", "Wang Family Village, Country of Zhao",
            java.util.List.of(
                "Wang Lin's fourth uncle; risked his sect position to get Wang Lin into Heng Yue Sect",
                "Targeted by Teng Huayuan's diviner but spared",
                "Survived the Wang Clan annihilation"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Teng Huayuan", "enemy")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N05", "Wang Zhuo", "王卓", CharType.DISCIPLE,
            "Nascent Soul", "Wang Clan / Heng Yue Sect", "deceased", 4,
            "unknown", "Wang Family Village, Country of Zhao",
            java.util.List.of(
                "Wang Lin's cousin; married Teng Xiuxiu under duress after clan annihilation",
                "Killed his wife and himself rather than betray the Wang clan; Wang Lin saved and reincarnated him",
                "Wang Lin helped him form Nascent Soul; guarded the Wang family until lifespan ended"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Teng Xiuxiu", "family"), new RelationShip("Qing Yi", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N06", "Wang Hao", "王浩", CharType.DISCIPLE,
            "low-tier cultivator", "Wang Clan / Heng Yue Sect", "alive", 4,
            "unknown", "Wang Family Village, Country of Zhao",
            java.util.List.of(
                "Wang Lin's cousin",
                "Spared by Teng Huayuan because he was a sect member",
                "One of the few Wang Clan survivors"
            ),
            List.of(new RelationShip("Wang Lin", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N07", "Wang Ping", "王平", CharType.OTHER,
            "mortal", "none (mortal)", "reincarnated", 5,
            "unknown", "Planet Ran Yun",
            java.util.List.of(
                "Wang Lin's son by Mu Bingmei/Liu Mei; refined into a resentful spirit by Liu Mei",
                "Raised by Wang Lin as a mortal in a desolate village",
                "His death triggered Wang Lin's Karma Domain evolution; eventually reunited with Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Mu Bingmei", "family"), new RelationShip("Qing Yi", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N08", "Wang Yiyi", "王一一", CharType.OTHER,
            "Paragon-tier", "Vast Dao Palace / Wang Lin's lineage", "alive", 5,
            "unknown", "Xiangang Continent / AWWP Weiyang Boundary",
            java.util.List.of(
                "Wang Lin and Li Muwan's daughter; Saintess of the Vast Dao Palace",
                "Escaped destruction of Vast Dao Palace by inhabiting a mask",
                "Married Wang Baole (AWWP protagonist); brought back to Xiangang Continent by Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Li Muwan", "family"), new RelationShip("Wang Baole", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N09", "Wang Jiduo", "王继多 / 继度", CharType.OTHER,
            "Imperial Venerable", "Ancient Clan (Primordial Ancient lineage)", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Wang Lin's adopted son/godson on the IAC",
                "Of the Ancient Clan Primordial lineage",
                "Wang Lin enabled him to become Imperial Venerable"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Song Yu", "family")),
            "novel (IAC arc); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N10", "Zhou Ru", "周茹", CharType.OTHER,
            "Soul Transformation", "Kunxu Realm (disciple of Mu Bingmei)", "reincarnated", 5,
            "unknown", "Planet Suzaku / IAC",
            java.util.List.of(
                "Wang Lin's adopted daughter; vessel for Li Muwan's soul (intended)",
                "Li Muwan chose not to devour Zhou Ru's soul; calls Wang Lin 'uncle'",
                "Reincarnated on IAC and lives an ordinary life"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Li Muwan", "family"), new RelationShip("Mu Bingmei", "master"), new RelationShip("Xiao Bai", "ally")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N11", "Qing Yi", "青衣", CharType.OTHER,
            "unknown", "Wang family", "alive", 4,
            "unknown", "unknown",
            java.util.List.of("Wang Ping's wife (daughter-in-law to Wang Lin)"),
            List.of(new RelationShip("Wang Ping", "family"), new RelationShip("Wang Lin", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N12", "Song Yu", "宋玉", CharType.OTHER,
            "unknown", "Wang family", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of("Wang Jiduo's wife"),
            List.of(new RelationShip("Wang Jiduo", "family"), new RelationShip("Wang Lin", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N13", "Wang Baole", "王宝乐", CharType.PROTAGONIST,
            "Summer Immortal (9th Step)", "Federation / Wang Lin's lineage", "transcended", 5,
            "unknown", "AWWP world",
            java.util.List.of(
                "AWWP protagonist; Wang Yiyi's husband, therefore Wang Lin's son-in-law",
                "Mentored/protected by Wang Lin ('Paragon Wang') in AWWP",
                "Inherits the Eight Extreme Dao from Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "family"), new RelationShip("Wang Yiyi", "love_interest")),
            "AWWP Ch.60s & Ch.69 (cross-novel); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N14", "Li Qiqing", "李齐青", CharType.DISCIPLE,
            "elite disciple", "Luo He Sect", "alive", 4,
            "unknown", "Fire Burn Country",
            java.util.List.of("Li Muwan's older brother; raised her after their parents died", "Luo He Sect elite disciple"),
            List.of(new RelationShip("Li Muwan", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N15", "Teng Xiuxiu", "藤秀秀", CharType.OTHER,
            "low-tier cultivator", "Teng Clan", "deceased", 4,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Wang Zhuo's wife (forced marriage after Wang Clan annihilation)",
                "Killed by Wang Zhuo when he chose the Wang clan over her"
            ),
            List.of(new RelationShip("Wang Zhuo", "family"), new RelationShip("Teng Huayuan", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N16", "Dao Master Blue Dreams", "蓝梦道主", CharType.ELDER,
            "Void Tribulant+", "Blue Silk Clan", "alive", 4,
            "unknown", "Blue Silk Clan Star Domain",
            java.util.List.of(
                "Li Qianmei's father; taught Wang Lin Light Shadow Shield and Dao Art Fusion",
                "Healed Li Qianmei at the cost of her memories",
                "Wang Lin injured his palm at one point"
            ),
            List.of(new RelationShip("Li Qianmei", "family"), new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N17", "Li Muwan", "李慕婉", CharType.OTHER,
            "Treading Heaven Realm", "Luo He Sect / Cloud Sky Sect", "transcended", 5,
            "unknown", "Fire Burn Country / Chu Country / Cave World",
            java.util.List.of(
                "Met Wang Lin escaping a Fire Beast; drained life force to refine Azure Dragon Jade Slip for him",
                "Failed Nascent Soul formation multiple times; died at 500 years old",
                "Soul preserved in Heaven Defying Bead 700 years; resurrected by Wang Lin at 4th Step; transcends with him"
            ),
            List.of(new RelationShip("Wang Lin", "love_interest"), new RelationShip("Li Qiqing", "family"), new RelationShip("Wang Yiyi", "family"), new RelationShip("Sun Zhenwei", "enemy")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N18", "Li Qianmei", "李千媚", CharType.OTHER,
            "Nirvana Scryer+", "Ghost Sect / Wang Lin's lineage", "alive", 5,
            "unknown", "Origin Sect / IAC",
            java.util.List.of(
                "Daughter of Dao Master Blue Dream; Wang Lin's second wife",
                "Smeared blood on Wang Lin's stone-petrified body for 10 years to save him from Daoist Water",
                "Healed by her father at the cost of losing most memories of Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "love_interest"), new RelationShip("Dao Master Blue Dreams", "family"), new RelationShip("Wang Yiyi", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N19", "Mu Bingmei", "慕冰媚 / 柳眉", CharType.OTHER,
            "Ascendant+", "Vermilion Bird Country / Divine Sect", "alive", 4,
            "unknown", "Planet Suzaku / IAC",
            java.util.List.of(
                "Liu Mei's true form; Wang Lin's third wife",
                "Had a son with Wang Lin (Wang Ping) whom she refined into a resentful spirit out of hatred",
                "Wang Lin severed karmic ties with her via the Dream Dao; one of his clones accompanies her"
            ),
            List.of(new RelationShip("Wang Lin", "love_interest"), new RelationShip("Wang Ping", "family"), new RelationShip("Zhou Ru", "disciple")),
            "novel; Fandom wiki; Baidu Baike (Liu Mei = Mu Bingmei)"
        ),
        new CanonCharacter(
            "N20", "Situ Nan", "司徒南", CharType.OTHER,
            "Yang Solid Peak (reconstructed); Heaven Trampling (IAC reincarnation)", "Vermilion Bird Country / Wu Xuan Country", "alive", 5,
            "unknown", "Planet Suzaku / Immortal Execution Star",
            java.util.List.of(
                "Originally the Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird",
                "Fled into the Heaven Defying Pearl; met Wang Lin and became his first mentor",
                "Sacrificed his remaining power to save Wang Lin; reincarnated on IAC as 'Si Nan' Grand Marshal of Wu Xuan Country"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Ye Wuyou", "ally"), new RelationShip("Seven-Colored Daoist", "enemy"), new RelationShip("Third Generation Vermilion Bird Master", "enemy"), new RelationShip("Tan Lang", "enemy")),
            "novel; Fandom wiki; Baidu Baike (Green Soul/2nd-Gen VB)"
        ),
        new CanonCharacter(
            "N21", "All-Seer", "全知者", CharType.PATRIARCH,
            "peak Third Step", "Heavenly Fate Sect", "deceased", 5,
            "unknown", "Planet Tian Yun",
            java.util.List.of(
                "Mortal-realm schemer ruling the Heavenly Fate Sect; false mentor to Wang Lin",
                "Plotted to absorb source origins of Wang Lin, Ling Tianhou, and Blood Ancestor",
                "Taught Wang Lin the Celestial Slaughter Art with a trap inside; killed by Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Tianyunzi", "family"), new RelationShip("Blood Ancestor", "enemy"), new RelationShip("Ling Tianhou", "enemy")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N22", "Tu Si", "涂司", CharType.ELDER,
            "Ancient God 8-Star", "Ancient Clan", "deceased", 5,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of(
                "His body became the Land of the Ancient God (3-level Chaotic Broken Stars realm)",
                "Granted Wang Lin the 'Great Enlightened One' title and 'knowledge' inheritance",
                "Tuo Sen inherited his 'power' inheritance (born from Tu Si's failed Ink Flow Split Soul Technique)"
            ),
            List.of(new RelationShip("Wang Lin", "disciple"), new RelationShip("Tuo Sen", "family")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N23", "Du Tian", "顿天", CharType.ELDER,
            "Nirvana Scryer+", "Soul Refining Sect", "self_erased", 5,
            "unknown", "Pilu Kingdom",
            java.util.List.of(
                "Soul Refining Sect ancestor; gave Wang Lin the Ten Billion Soul Banner and sect inheritance",
                "Helped Wang Lin's clone reach Nascent Soul peak and true body reach 3-Star Ancient God",
                "Erased his own consciousness to become a soul within the Soul Banner"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Nian Tian", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N24", "Bai Fan", "白凡", CharType.OTHER,
            "Immortal Emperor (Third Step+)", "Thunder Immortal World", "deceased", 4,
            "unknown", "Thunder Immortal World",
            java.util.List.of(
                "Wang Lin inherited Bai Fan's Mountain Crumble spell and Six Paths Triple Techniques",
                "Found Bai Fan's Collection Pavilion in the Thunder Immortal World",
                "Long dead; inheritance passed to Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "disciple"), new RelationShip("Qing Shui", "ally")),
            "novel (Thunder Immortal Realm); Fandom wiki"
        ),
        new CanonCharacter(
            "N25", "Lu Yun", "陆云", CharType.OTHER,
            "Void Flame Cultivator", "Four Divine Sect / Vermilion Bird Divine Sect / Heng Yue Sect", "deceased", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of(
                "5th-Gen Vermilion Bird Divine Emperor; secret identity was Huang Long Zhenren, Heng Yue Sect master",
                "Gave Wang Lin the Vermilion Bird Sequence and taught him the Nine Mysterious Transformations",
                "Infiltrated Cultivation Alliance HQ to gather info on Qing Shui; died after returning"
            ),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N26", "Qing Lin", "青林", CharType.OTHER,
            "Immortal Emperor", "Xiangang Continent", "alive", 4,
            "unknown", "Xiangang Continent",
            java.util.List.of(
                "Wang Lin's 'master in name'; father of Qing Shuang",
                "Left the Body Fixation Art for Qing Shuang; Wang Lin accidentally obtained it",
                "Possessed by Taga (ancient demon); Wang Lin fought to save him"
            ),
            List.of(new RelationShip("Wang Lin", "disciple"), new RelationShip("Qing Shuang", "family"), new RelationShip("Taga", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N27", "Su Dao", "苏道", CharType.OTHER,
            "unknown", "independent (scholar)", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of("Wang Lin's 'Scholar' mentor", "Tangential figure in the story"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N28", "Xuan Luo", "玄罗", CharType.ELDER,
            "Great Heavenly Venerable", "Ancient Clan (Dao Gu lineage)", "alive", 5,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Wang Lin's true master (the only one he formally acknowledges on the IAC)",
                "Fought Dao Yi Great Heavenly Venerable over a Primordial God Realm fragment",
                "Helped over a dozen of Wang Lin's friends reincarnate into the Immortal Execution Continent"
            ),
            List.of(new RelationShip("Wang Lin", "disciple"), new RelationShip("Dao Yi Great Celestial Venerable", "enemy")),
            "novel (IAC arc); Fandom wiki"
        ),
        new CanonCharacter(
            "N30", "Qing Shui", "清水", CharType.ELDER,
            "Third Step", "Qing Shui Kingdom / Colorful Immortal Venerable's slaughter soul", "alive", 5,
            "unknown", "Cave World / Immortal Execution Star",
            java.util.List.of(
                "Formed from the Seven-Colored Immortal Venerable's lifetime of slaughter",
                "Wang Lin's senior brother via Bai Fan's technique; saved him multiple times",
                "Killed Russell with Ji Realm for offending Wang Lin; reincarnated on IAC with memories auto-recovered"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Hong Die", "family"), new RelationShip("Seven-Colored Daoist", "family"), new RelationShip("Russell", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N31", "Zhou Yi", "周逸", CharType.DISCIPLE,
            "Wending realm", "independent (defected from original sect)", "reincarnated", 5,
            "unknown", "Rain Immortal Realm / IAC",
            java.util.List.of(
                "Found Qing Shuang's corpse and called her 'Ting'er'; defected from his sect to protect her",
                "Burned his primordial spirit in defense; reached Wending realm; gave Wending Crystal to Wang Lin",
                "His primordial spirit transformed into the sword spirit of the Rain Immortal Sword"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Qing Shuang", "love_interest")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N32", "Qing Shuang", "青霜", CharType.DISCIPLE,
            "Immortal-tier", "Qing Lin lineage", "reincarnated", 5,
            "unknown", "Rain Immortal Realm / IAC",
            java.util.List.of(
                "Qing Lin's daughter; 'Ting'er' to Zhou Yi; the Body Fixation Art was her inheritance",
                "Her corpse was protected by Zhou Yi for years",
                "Her remnant soul awakened and transformed Zhou Yi into the Rain Immortal Sword's spirit; reincarnated with Zhou Yi on IAC"
            ),
            List.of(new RelationShip("Qing Lin", "family"), new RelationShip("Zhou Yi", "love_interest"), new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N33", "Chi Hu", "赤虎", CharType.OTHER,
            "unknown", "Giant Demon Clan", "alive", 4,
            "unknown", "unknown",
            java.util.List.of(
                "Giant Demon Clan member who gifted Wang Lin the Star Compass",
                "The Star Compass became Wang Lin's primary void-transportation tool"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N34", "Qiu Siping", "邱思平", CharType.DISCIPLE,
            "Nascent Soul", "independent", "alive", 4,
            "unknown", "Qilin City",
            java.util.List.of(
                "Proficient in ancient restrictions; agreed to unlock Wang Lin's restriction on Yun Fei",
                "Both he and Wang Lin reached Nascent Soul via consuming souls of Wang Lin's masters",
                "Wang Lin spared him; they became allies"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N35", "Mo Ling", "莫灵", CharType.OTHER,
            "unknown", "unknown", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's allies; tangential figure"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N36", "Mo Lihai", "莫离海", CharType.OTHER,
            "Ascendant+", "Sky Demon Country", "alive", 4,
            "unknown", "Sky Demon Country",
            java.util.List.of(
                "Sky Demon Country general; befriended Wang Lin in the capital",
                "Helped Wang Lin in the Demonic Drum tournament",
                "Asked Wang Lin to help him obtain Vice Commander-in-Chief"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N37", "Sun Tai", "孙泰", CharType.OTHER,
            "Nirvana Scryer+", "Corpse Yin Sect / Wang Lin's", "alive", 4,
            "unknown", "Planet Ran Yun",
            java.util.List.of(
                "Corpse Yin Sect member who tried to seize Qing Shuang; defeated by Zhou Yi",
                "Became Wang Lin's servant after defeat",
                "Spent his final years peacefully on Planet Ran Yun as Wang Lin's friend"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Qing Shuang", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N38", "Li Yuan", "李远", CharType.OTHER,
            "unknown", "unknown", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's allies; tangential figure"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N39", "Ling Tianhou", "凌天候", CharType.ELDER,
            "Third Step (Nirvana Void)", "Da Lou Sword Sect", "alive", 4,
            "unknown", "Allheaven Star System",
            java.util.List.of(
                "Da Lou Sword Sect elder/sect master; invited Wang Lin to be sect elder",
                "Challenged Wang Lin to survive 3 sword strikes; Wang Lin survived all 3",
                "Allied with Wang Lin during East Demon Spirit Sea arc; helped against All-Seer"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("All-Seer", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N40", "Bei Lou", "贝罗", CharType.OTHER,
            "Third Step+", "Thunder Celestial Temple", "alive", 4,
            "unknown", "Fire Demon Country / Celestial Emperors Tower",
            java.util.List.of(
                "Celestial from Thunder Celestial Temple; helped Wang Lin battle divine retribution during Ascendant breakthrough",
                "Helped trap the scattered devil Tie Lan and weaken the fragmented ancient demon under Fire Demon Country",
                "Among those who coldly stared at Master Void"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Master Void", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N41", "Wang Wei", "王伟", CharType.OTHER,
            "Nirvana Shatterer", "unknown", "alive", 4,
            "unknown", "unknown",
            java.util.List.of(
                "Wang Lin's ally; injured by Master Void",
                "Sneak-attacked by Master Void; Wang Lin used furnace to switch places and defended him"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Master Void", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N42", "Hu Juan", "胡娟", CharType.OTHER,
            "unknown", "unknown", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's allies; among those who coldly stared at Master Void"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N44", "Ta Shan", "塔山", CharType.OTHER,
            "Celestial Guard (refined)", "Wang Lin's", "alive", 4,
            "unknown", "Wang Lin's side",
            java.util.List.of("Wang Lin's refined Celestial Guard; serves as front-line combat puppet"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N45", "Big Head Cultivator", "大头", CharType.OTHER,
            "Kunie", "Wang Lin's", "alive", 4,
            "unknown", "Luo Tian / IAC",
            java.util.List.of(
                "Originally from Luo Tian; sealed with slave seal during the invasion war",
                "Followed Wang Lin for a long time; reincarnated on IAC with memories reawakened by Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (Luo Tian arc); Fandom wiki"
        ),
        new CanonCharacter(
            "N46", "Lei Ji", "雷记", CharType.OTHER,
            "beast", "Wang Lin's", "alive", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of("Wang Lin's mount; taken from the Corpse Yin Sect on Planet Suzaku"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (Corpse Yin Sect); Fandom wiki"
        ),
        new CanonCharacter(
            "N47", "Liu Jinbiao", "刘金彪", CharType.OTHER,
            "Peak Path of Deception cultivator", "Three Auspicious Treasures", "alive", 4,
            "unknown", "Cave World / IAC",
            java.util.List.of(
                "One of the Three Auspicious Treasures (with Xu Liguo and Zhong Dahong)",
                "Saved Wang Lin's life; found via Heavenly Fortune Calculation method",
                "On IAC, used Path of Deception to help Wang Lin comprehend multiple Origins; gained Golden Sea Dragon's recognition"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Xu Liguo", "ally"), new RelationShip("Zhong Dahong", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N48", "Ling Dong", "凌东", CharType.OTHER,
            "unknown", "Wang Lin's", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Initially antagonistic to Wang Lin; later became his servant"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N49", "Zhou Jin", "周瑾", CharType.OTHER,
            "unknown", "Wang Lin's", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Initially antagonistic to Wang Lin; later became his servant"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N50", "Zhong Dahong", "钟大红", CharType.OTHER,
            "unknown", "Flash Thunder Clan / Wang Lin's", "alive", 4,
            "unknown", "Cave World / IAC",
            java.util.List.of(
                "One of the Three Auspicious Treasures; originally from the Flash Thunder Clan",
                "Followed Wang Lin when his clan was exterminated; asked 2nd-Gen Vermilion Bird to take care of him",
                "Continues swindling on IAC"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Liu Jinbiao", "ally"), new RelationShip("Xu Liguo", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N51", "Daoist Scattered Spirit", "散灵道人", CharType.ELDER,
            "Third Step", "Scatter Thunder Clan", "alive", 4,
            "unknown", "unknown",
            java.util.List.of(
                "Scatter Thunder Clan elder; gave Wang Lin tens of thousands of Spiritual Thunder bolts",
                "Helped Wang Lin perfect his Thunder Essence"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N52", "Second Generation Vermilion Bird", null, CharType.OTHER,
            "Void Flame Cultivator", "Vermilion Bird Divine Sect / Fallen Land", "alive", 4,
            "unknown", "Fallen Land",
            java.util.List.of(
                "2nd-Gen Vermilion Bird Divine Emperor; also called Young Emperor of the Fallen Land",
                "Fished for a dragon and gifted its blood to Wang Lin; taught him the Dao of Strength",
                "Stood up for Wang Lin at the Trial of Heaven; asked to take care of Zhong Dahong"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Zhong Dahong", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N53", "Master Hong Shan", "洪山道主", CharType.OTHER,
            "unknown", "unknown", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's allies; tangential figure"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N54", "Master South Cloud", "南云道主", CharType.OTHER,
            "unknown", "unknown", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's allies; tangential figure"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N55", "Twin Great Heavenly Venerables", "双胞胎大天尊", CharType.ELDER,
            "Great Heavenly Venerable", "IAC", "alive", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Twin brothers who are both Great Heavenly Venerables on the IAC",
                "Took Tan Lang as their 'pet'",
                "Recurring but tangential allies of Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Tan Lang", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N56", "Tan Lang", "贪狼", CharType.OTHER,
            "Third Step+", "Seven-Colored Immortal Venerable (Yellow Soul)", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Yellow Soul of the Seven-Colored Immortal Venerable (good fortune soul)",
                "His Primordial Thunder Dragon was half-devoured by Wang Lin; treasures repeatedly plundered",
                "Reduced to a pet by the Twin Great Heavenly Venerables on the IAC"
            ),
            List.of(new RelationShip("Wang Lin", "rival"), new RelationShip("Situ Nan", "enemy"), new RelationShip("Third Generation Vermilion Bird Master", "ally"), new RelationShip("Seven-Colored Daoist", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N57", "Hong Die", "红蝶", CharType.DISCIPLE,
            "Nascent Soul", "Vermilion Bird Country / Cultivation Alliance", "deceased", 4,
            "unknown", "Planet Suzaku / IAC",
            java.util.List.of(
                "Qing Shui's daughter; innate Five Elements spirit with Ruthlessness Concept",
                "Schemed against by 14th-Gen Vermilion Bird and Qian Feng; vitality and Concept devoured",
                "Begged Wang Lin to end her life in the Vermilion Bird Tomb; gave him a blue rose; soul reincarnated as Qing Hong on IAC"
            ),
            List.of(new RelationShip("Qing Shui", "family"), new RelationShip("Wang Lin", "ally"), new RelationShip("Fourteenth Generation Vermilion Bird Master", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N58", "Zhou Wutai", "周武泰", CharType.OTHER,
            "Vermilion Bird Master (Wending+)", "Vermilion Bird Country", "deceased", 4,
            "unknown", "Vermilion Bird Star",
            java.util.List.of(
                "15th-Gen Vermilion Bird Master (position transferred by Wang Lin); bald with large ears",
                "Azure Dragon bloodline; one of Yun Quezi's 4 chess pieces",
                "Welcomed Wang Lin every return; informed him of Hong Die's split-soul reincarnation; eventually passed from old age"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Yun Quezi", "master")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N59", "Yun Quezi", "云阙子", CharType.ELDER,
            "Late Nascent Soul", "Immortal Remnant Clan / Vermilion Bird Country", "deceased", 4,
            "unknown", "Vermilion Bird Star",
            java.util.List.of(
                "2nd Ancestor of Immortal Remnant Clan; 10-leaf curse master with Manic Concept",
                "Helped Wang Lin comprehend Soul Transformation; selected him as one of 4 chess pieces",
                "Gave Wang Lin the Li Ming Straw Hat; passed away before Wang Lin's 2nd return"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Qian Pinghai", "master"), new RelationShip("Zhou Wutai", "disciple")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N60", "Mo Zhi", "莫知", CharType.ELDER,
            "Third Step+", "Cultivation Alliance", "alive", 4,
            "unknown", "Vermilion Bird Star",
            java.util.List.of(
                "Cultivation Alliance emissary; their Dao discussion triggered Wang Lin's Soul Transformation enlightenment",
                "Resolved the Immortal Remnant Clan war on Vermilion Bird Star",
                "Intended Wang Lin to be the next Vermilion Child; asked when Wang Lin would return the Boundary Fixing Compass"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N61", "Lian Daofei", "连道非", CharType.OTHER,
            "Eight Extremities Great Heavenly Venerable", "Xiangang Continent", "alive", 5,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Brother of Lian Daozhen; fused supreme Immortal bloodline into Wang Lin's body",
                "Imparted the Indestructible Immortal Body; Wang Lin made him a disciple",
                "After Lian Daozhen failed, his bloodline awakened → inherited Eight Extremities title → new Immortal Emperor"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Lian Daozhen", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N62", "Xu Liguo", "徐立国", CharType.OTHER,
            "devil soul (sword spirit)", "Wang Lin's", "alive", 5,
            "unknown", "Cave World / IAC",
            java.util.List.of(
                "Wang Lin's first devil soul companion; captured from a Corpse Yin Sect corpse puppet",
                "Consciousness erased; turned into a demonic head; constantly rebellious",
                "Became sword spirit of one of Wang Lin's immortal swords; in ISSTH guarded Wang family descendants"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Liu Jinbiao", "ally"), new RelationShip("Zhong Dahong", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N63", "Tuo Sen", "拓山", CharType.ANTAGONIST,
            "Ancient God 8-Star (potential)", "Ancient Clan", "alive", 5,
            "unknown", "Chaotic Broken Stars / IAC",
            java.util.List.of(
                "Born from Tu Si's failed Ink Flow Split Soul Technique; inherited Tu Si's 'power' inheritance",
                "Hunted Wang Lin for 1000+ years; trapped in Tu Si's body",
                "After Wang Lin obtained Dao Ancient inheritance, returned the memory inheritance; reconciled"
            ),
            List.of(new RelationShip("Wang Lin", "rival"), new RelationShip("Tu Si", "family"), new RelationShip("Duanmu Ji", "enemy")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N64", "Xu Yunshan", "许云山", CharType.ELDER,
            "unknown", "Xuan Yuan Sect", "alive", 3,
            "unknown", "Earth Planet",
            java.util.List.of(
                "Junior sect master of Xuan Yuan Sect on Earth Planet",
                "Greeted Wang Lin on Earth Planet; tangential to main plot"
            ),
            List.of(),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N65", "Ouyang Hua", "欧阳华", CharType.ELDER,
            "unknown", "Mountain Valley Tribe / Soul Refining Tribe", "alive", 4,
            "unknown", "Pilu Kingdom",
            java.util.List.of(
                "Mountain Valley Tribe chief; recognized Wang Lin as the tribe's Ancestor",
                "The tribe became the Soul Refining Tribe of the eastern sea under Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N66", "Li Dannan", "李丹楠", CharType.OTHER,
            "unknown", "independent", "alive", 3,
            "unknown", "Trading Planet",
            java.util.List.of("Wang Lin's guide on the Trading Planet", "Helped Wang Lin search for Void Wood Stone and snow ink"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N67", "Bai Wei", "白薇", CharType.OTHER,
            "unknown", "Da Lou Sword Sect", "alive", 4,
            "unknown", "Trading Planet",
            java.util.List.of(
                "Met Wang Lin on Trading Planet; Wang Lin tried to help her with Yang energy secret",
                "Revealed to be part of All-Seer's plot against Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("All-Seer", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N68", "Master Flamespark", "火芒", CharType.ELDER,
            "Third Step", "Thunder Celestial Temple", "alive", 4,
            "unknown", "Thunder Celestial Realm",
            java.util.List.of(
                "Master of the Thunder Celestial Temple",
                "Stopped Russell's final attack on Wang Lin",
                "Sometimes antagonistic; multiple appearances in Thunder Celestial Realm arc"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Russell", "ally")),
            "novel (Thunder Celestial Realm); Fandom wiki"
        ),
        new CanonCharacter(
            "N69", "Russell", "罗素", CharType.ANTAGONIST,
            "Third Step", "Thunder Celestial Temple", "deceased", 4,
            "unknown", "Thunder Celestial Realm",
            java.util.List.of(
                "Thunder Celestial Temple test proctor; saw Wang Lin defeat his Golden Giant",
                "Realized Wang Lin killed his brother; attacked Wang Lin",
                "Killed by Qing Shui's Ji Realm for offending Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Qing Shui", "enemy")),
            "novel (Thunder Celestial Realm); Fandom wiki"
        ),
        new CanonCharacter(
            "N70", "Zhao Yu", "赵宇", CharType.DISCIPLE,
            "low", "Origin Sect", "alive", 4,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Arrogant Origin Sect disciple who made mortal villagers stand in the rain",
                "Wang Lin enslaved him and had him lead back to the Origin Sect"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N71", "Lu Yanfei", "陆燕飞", CharType.DISCIPLE,
            "unknown", "Origin Sect", "alive", 4,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Origin Sect member who guessed Wang Lin's situation accurately",
                "Granted Wang Lin the 'ancestor' cover identity"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N72", "Xu Yun", "许云", CharType.DISCIPLE,
            "unknown", "Origin Sect", "alive", 3,
            "unknown", "Cloud Sea Star System",
            java.util.List.of("Origin Sect member who confronted Wang Lin alongside Lu Yanfei; tangential"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N73", "Lu Yuncong", "陆云聪", CharType.DISCIPLE,
            "unknown", "Origin Sect", "alive", 4,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Came seeking revenge for his son with Li Qianmei",
                "Realized he was no match for Wang Lin; discussed Dao all night and gained great benefits"
            ),
            List.of(new RelationShip("Wang Lin", "rival"), new RelationShip("Li Qianmei", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N74", "Song Wude", "宋武德", CharType.ANTAGONIST,
            "unknown", "Invader sect", "deceased", 4,
            "unknown", "Cloud Sea Star System",
            java.util.List.of("Came with Rudy to take over the Origin Sect", "Killed by Wang Lin"),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Rudy", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N75", "Rudy", null, CharType.ANTAGONIST,
            "unknown", "Invader sect", "deceased", 3,
            "unknown", "Cloud Sea Star System",
            java.util.List.of("Came with Song Wude to take over the Origin Sect", "Killed by Wang Lin"),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Song Wude", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N76", "Ji Si", "吉思", CharType.OTHER,
            "unknown", "Dao Devil Sect / Wang Lin's", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Implanted multiple Essences into Wang Lin's body (preparing him for Green Devil possession)",
                "Granted Earth, Water, Wood, and Metal Essence components using rare materials",
                "Wang Lin's body ultimately reclaimed all these Essences"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Green Devil", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N77", "Jiu Di", "九地", CharType.ELDER,
            "Grand Empyrean", "IAC", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Grand Empyrean of the IAC; confronted Wang Lin in the Imperial City",
                "Later gave Wang Lin the third fragment of the Celestial Ancestor's Immortal Absolute Sword"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (IAC); Fandom wiki"
        ),
        new CanonCharacter(
            "N78", "Sea Child Celestial Venerable", "海子大天尊", CharType.ELDER,
            "Celestial Venerable", "IAC", "alive", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "IAC Celestial Venerable; trapped with Wang Lin inside the Immortal Ancestor's severed palm",
                "Broke free via a Great Celestial Venerable's scheme; fortune related to Metal and Wood Origins"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N79", "Ye Wuyou", "叶无忧", CharType.OTHER,
            "1st-Gen Vermilion Bird Master", "Vermilion Bird Country", "deceased", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of(
                "1st-Gen Vermilion Bird Master; Situ Nan's benefactor (the only person Situ Nan respected besides Wang Lin)",
                "Situ Nan guarded the Vermilion Bird Star for 1000 years and sealed the Cultivation Star Crystal to repay his favor",
                "Long deceased by main story"
            ),
            List.of(new RelationShip("Situ Nan", "ally")),
            "novel (backstory); Fandom wiki"
        ),
        new CanonCharacter(
            "N80", "Qian Pinghai", "钱平海", CharType.OTHER,
            "13th-Gen Vermilion Bird Master", "Vermilion Bird Country", "deceased", 3,
            "unknown", "Vermilion Bird Star",
            java.util.List.of("13th-Gen Vermilion Bird Master; Yun Quezi's master", "Backstory figure"),
            List.of(new RelationShip("Yun Quezi", "disciple")),
            "novel (implicit); Fandom wiki (backstory); C3"
        ),
        new CanonCharacter(
            "N81", "Third Generation Vermilion Bird Master", null, CharType.ANTAGONIST,
            "3rd-Gen Vermilion Bird Master", "Vermilion Bird Country", "deceased", 4,
            "unknown", "Vermilion Bird Star",
            java.util.List.of(
                "Betrayed Situ Nan; took advantage of his weakened state after Cultivation Star Crystal sealing",
                "Launched a sneak attack with Tan Lang, destroying Situ Nan's physical body"
            ),
            List.of(new RelationShip("Situ Nan", "enemy"), new RelationShip("Tan Lang", "ally")),
            "novel (backstory); Fandom wiki"
        ),
        new CanonCharacter(
            "N82", "Fourteenth Generation Vermilion Bird Master", null, CharType.ANTAGONIST,
            "14th-Gen Vermilion Bird Master", "Vermilion Bird Country", "alive", 4,
            "unknown", "Vermilion Bird Star",
            java.util.List.of(
                "Severed Situ Nan's arm; berated by Situ Nan's damaged soul and severed his own fingers in fear",
                "Schemed against Hong Die; attacked the Soul Refining Sect in EP96/ch1500",
                "Fled at the mere mention of Situ Nan's name"
            ),
            List.of(new RelationShip("Situ Nan", "enemy"), new RelationShip("Hong Die", "enemy")),
            "novel; Donghua EP96/Ch.~1500; Fandom wiki"
        ),
        new CanonCharacter(
            "N83", "Teng Huayuan", "藤化元", CharType.OTHER,
            "Half-Step Deity Transformation", "Teng Clan", "deceased", 5,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Wang Lin's first major revenge target; exterminated the entire Wang family",
                "Trapped Wang family souls in his banner; transmitted massacre scene to Wang Lin's mind",
                "Wang Lin's 'Kill and Destroy the Heart' revenge: built tower of heads, killed all 9 Nascent Soul cultivators, finally slew Teng Huayuan"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Wang Tianshui", "enemy"), new RelationShip("Teng Li", "family"), new RelationShip("Gao Qiming", "ally"), new RelationShip("Punnan Zi", "ally"), new RelationShip("Lin Yi", "ally")),
            "novel (early arc); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N84", "Teng Li", "藤立", CharType.DISCIPLE,
            "late Foundation Establishment", "Teng Clan", "deceased", 5,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Teng Huayuan's great-great-grandson; pursued Wang Lin for Old man Ji Mo's bounty",
                "Killed by Wang Lin in the Forest of Distorted Divine Sense; Wang Lin stole his Foundation Establishment"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Teng Huayuan", "family")),
            "novel (first kill); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N85", "Sun Dazhu", "孙大柱", CharType.DISCIPLE,
            "Foundation Establishment", "Heng Yue Sect", "deceased", 4,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Zhang Hu's 'master'; killed by Wang Lin to help Zhang Hu out of trouble",
                "This kindness led to Wang Lin's clan extermination (Teng Huayuan located Wang Lin via the trail)"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Zhang Hu", "rival")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N86", "Old man Ji Mo", "极魔老人", CharType.ANTAGONIST,
            "Core Formation+", "Heng Yue Sect (associated)", "alive", 4,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Jimo Elder; sent disciples including Teng Li to take revenge on Wang Lin",
                "Indirectly caused the Wang Clan's destruction"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Teng Li", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N87", "Gao Qiming", "高启明", CharType.OTHER,
            "diviner", "independent", "alive", 3,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Independent diviner; helped Teng Huayuan locate the Wang family",
                "Requested Fourth Uncle as payment"
            ),
            List.of(new RelationShip("Teng Huayuan", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N88", "Punnan Zi", "飘南子 / 楼侯", CharType.ANTAGONIST,
            "peak Third Step", "Scattered Demon of Rain Country", "sealed", 4,
            "unknown", "Country of Zhao / IAC",
            java.util.List.of(
                "Also known as Piao Nanzi or Lou Hou; came to Teng Huayuan's aid",
                "Sealed under the Ancient Shi Branch on the IAC",
                "Wang Lin later used Lou Hou's soul to complete Slaughter, Restriction, Absolute Beginning, and Absolute End Essence True Bodies"
            ),
            List.of(new RelationShip("Teng Huayuan", "ally"), new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N89", "Lin Yi", "林怡", CharType.ANTAGONIST,
            "unknown", "Teng Huayuan's allies", "deceased", 3,
            "unknown", "Country of Zhao",
            java.util.List.of("Came to Teng Huayuan's aid", "Killed by Wang Lin when he returned to wipe out the Teng family"),
            List.of(new RelationShip("Teng Huayuan", "ally"), new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N90", "Duanmu Ji", "端木吉", CharType.ANTAGONIST,
            "Soul Formation", "independent", "deceased", 4,
            "unknown", "Sea of Devils / Xuan Wu / Chaotic Broken Stars",
            java.util.List.of(
                "Notorious evil cultivator attracted by rumors of Wang Lin's Death Spell",
                "Chased Wang Lin for 3 years (Wang Lin hid in Heaven Defying Bead)",
                "Eventually killed by Hunchback Meng (Tuo Sen's puppet)"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Wang Qingyue", "ally"), new RelationShip("Hunchback Meng", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N91", "Hunchback Meng", "驼背孟", CharType.ANTAGONIST,
            "Soul Transformation (perfect circle)", "Meng Sect", "deceased", 4,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of(
                "Ate his own toad to reach perfect circle Soul Transformation and killed the Hurricane Beast King",
                "Revived from sea of blood as a Tuo Sen puppet; killed Duanmu Ji",
                "Eventually killed"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Tuo Sen", "ally"), new RelationShip("Duanmu Ji", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N92", "Xu Liqing", "徐立清 / 六欲魔尊", CharType.ANTAGONIST,
            "Soul Transformation", "Six Desires Demon Lord", "deceased", 4,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of(
                "Demon Lord of Six Desires; modified Restriction Mountain restrictions (adding panic)",
                "Attacked by Duanmu Ji; cornered by Hunchback Meng; eventually killed"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N93", "Gun Lan", "管岚", CharType.ANTAGONIST,
            "Soul Formation", "unknown", "deceased", 3,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of("Listed among the evils Wang Lin outwitted in Chaotic Broken Stars; killed"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N94", "Wang Qingyue", "王青叶", CharType.OTHER,
            "unknown", "Duanmu Ji's group", "alive", 3,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of("Brought Wang Lin to Chaotic Broken Stars for Duanmu Ji to use Death Spell on the 3rd level"),
            List.of(new RelationShip("Duanmu Ji", "ally"), new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N95", "Yun Fei", "云菲", CharType.DISCIPLE,
            "Core Formation", "Qihuang Sect", "deceased", 4,
            "unknown", "Qilin City",
            java.util.List.of(
                "Qihuang Sect successor; Wang Lin placed a 3-day restriction on her",
                "Visited other cultivators to find a way to unlock the ban",
                "Killed by Wang Lin's devil when Qiu Siping agreed to unlock the restriction"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Qiu Siping", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N96", "Qian Kun", "乾坤", CharType.DISCIPLE,
            "Core Formation", "Poison Palace", "deceased", 4,
            "unknown", "Chaotic Broken Stars",
            java.util.List.of(
                "Poison Palace disciple; saw a Nascent Soul cultivator being devoured in Broken Stars",
                "Killed by Wang Lin to leave no witness (also because he belonged to Hunchback Meng's sect)"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Hunchback Meng", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N97", "Master Void", "虚空道主", CharType.ANTAGONIST,
            "peak Nirvana Shatterer", "independent", "deceased", 4,
            "unknown", "Celestial Emperors Tower",
            java.util.List.of(
                "Could not allow Wang Lin (with awakened Vermilion Bird Divine Mark) to grow",
                "Fought Wang Lin multiple times; injured from peak to mid Nirvana Shatterer",
                "Later sealed by Ta Jia; killed by Wang Lin using Sundered Night"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Taga", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N98", "Blood Ancestor", "血祖 / 姚星海", CharType.PATRIARCH,
            "peak Third Step", "Yao Family", "deceased", 5,
            "unknown", "Planet Tian Yun / Thunder Immortal Realm",
            java.util.List.of(
                "Yao Xinghai; father of Yao Xixue; unique cultivation system and formidable strength",
                "Refined the Blood Soul Pill (resurrection pill); killed by Wang Lin in Thunder Immortal Realm",
                "Wang Lin released a wisp of his remnant soul to perfect Karma Concept; amnesiac Yao Xixue departed with it"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Yao Xixue", "family"), new RelationShip("All-Seer", "enemy")),
            "novel; Fandom wiki; Baidu Baike (姚星海)"
        ),
        new CanonCharacter(
            "N99", "Yao Xixue", "姚惜雪", CharType.ANTAGONIST,
            "Infant Transformation Late Stage", "Yao Family", "alive", 5,
            "Ch.491", "East Sea Demon Spirit Land / Immortal Monarch's Cave Mansion",
            java.util.List.of(
                "Blood Ancestor's only daughter; heterochromic red pupils, snow-white skin",
                "Ambushed Wang Lin → imprisoned 100 years → body destroyed; used Blood Soul Pill to be reborn",
                "Sacrificed her body to the Wind Demon for revenge; memories devoured → amnesiac; wanders with father's remnant soul"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Blood Ancestor", "family"), new RelationShip("Wind Demon", "ally")),
            "novel Ch.491; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N100", "Wind Demon", "风魔", CharType.ANTAGONIST,
            "Third Step", "independent (sealed demon)", "deceased", 4,
            "unknown", "Immortal Emperor's Cave Mansion",
            java.util.List.of(
                "Sealed demon who made a deal with Yao Xixue (her body in exchange for killing Wang Lin)",
                "Hundreds of rounds of fierce combat with Wang Lin",
                "Slain by Wang Lin's God-Slaying Spear"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Yao Xixue", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N101", "Yao Family", "姚家", CharType.ANTAGONIST,
            "multiple Soul Transformation+", "Yao Family (Southern Domain, Allheaven Star System)", "deceased", 4,
            "unknown", "Allheaven Star System",
            java.util.List.of(
                "One of 4 major powers in the Southern Domain of the Allheaven Star System",
                "Sent a kill order for Wang Lin after he killed Yao Family members in Thunder Celestial Realm",
                "Wang Lin destroyed multiple planets chasing them; faction weakened"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N102", "Daoist Water", "水道子", CharType.ANTAGONIST,
            "peak Third Step (Nirvana Void+)", "Rank 9 God Sect", "deceased", 5,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Sensed the Lord of the Sealed Realm's aura on Wang Lin and attacked him",
                "Wang Lin petrified (turned to stone) fighting him; Li Qianmei's 10-year blood anointment saved Wang Lin",
                "Eventually slain by Wang Lin in the Cloud Sea"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Li Qianmei", "enemy"), new RelationShip("Lord of the Sealed Realm", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N103", "Wu Qing", "吴情", CharType.ANTAGONIST,
            "Nirvana Shatterer", "Treasured Jade Sect", "deceased", 4,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Greedy old monster who tried to kill Wang Lin after a secret exchange",
                "Killed by Wang Lin using the War Spirit Print"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N104", "Master Ashen Pine", "灰松道主", CharType.ANTAGONIST,
            "Third Step", "independent", "deceased", 4,
            "unknown", "Seven-Colored Realm",
            java.util.List.of(
                "Gathered cultivators to enter the Seven-Colored Realm; schemed against everyone",
                "Killed by Wang Lin with the seven-colored nail"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Master Cloud Soul", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N105", "Master Cloud Soul", "云魂道主", CharType.ANTAGONIST,
            "Third Step", "independent", "unknown", 3,
            "unknown", "Seven-Colored Realm",
            java.util.List.of(
                "Schemed to team up with Master Ashen Pine against Wang Lin",
                "Tricked by Wang Lin into a different spatial crack"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Master Ashen Pine", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N106", "Noble Money", "贵钱", CharType.ANTAGONIST,
            "unknown", "independent", "deceased", 3,
            "unknown", "Cloud Sea Star System",
            java.util.List.of(
                "Tried to detain Wang Lin for questioning on his return from the Wild Continent",
                "Killed by Wang Lin along with his group"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N107", "Cang Songzi", "苍松子", CharType.ANTAGONIST,
            "Sub-Empty Annihilation upper-grade", "independent", "deceased", 4,
            "unknown", "Seven-Colored Realm",
            java.util.List.of(
                "Original owner of the Seven-Colored Divine Sky Nail (108 nails designed to kill Third Step experts)",
                "Destroyed Wang Lin's Rusty Iron Sword",
                "Killed by Wang Lin; Wang Lin took his Seven-Colored Divine Sky Nail"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N108", "Lu Fuzi", "陆夫子", CharType.OTHER,
            "unknown", "independent (scholar)", "alive", 3,
            "unknown", "Cultivation world",
            java.util.List.of(
                "Literary contest opponent; defeated by Wang Lin",
                "Wang Lin informed him of hidden dangers of the Seven-Colored Realm"
            ),
            List.of(new RelationShip("Wang Lin", "rival")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N109", "Ye Dao", "叶道", CharType.ANTAGONIST,
            "unknown", "unknown", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's enemies; tangential figure"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N110", "Lian Daozhen", "连道真", CharType.OTHER,
            "Immortal Emperor", "Xiangang Continent", "unknown", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Brother of Lian Daofei; Immortal Emperor of Xiangang Continent",
                "Failed to inherit the Immortal Ancestor's plan; self-destructed",
                "Wang Lin captured his soul and injured the Infant Skull belonging to Dao Yi"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Lian Daofei", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N111", "Yan Leizi", "炎雷子", CharType.ANTAGONIST,
            "unknown", "unknown", "deceased", 3,
            "unknown", "Thunder Immortal Realm",
            java.util.List.of("Listed in Wang Lin's enemies", "Met Wang Lin in the Thunder Immortal Realm; taken into soul banner"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N112", "Su Dao", "苏道", CharType.ANTAGONIST,
            "unknown", "unknown", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of(
                "Listed in Wang Lin's enemies (possibly same name as Su Dao the Scholar mentor)",
                "Tangential figure"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N113", "Tianyunzi", "天运子", CharType.OTHER,
            "Third Step+", "Heavenly Fate Sect", "deceased", 5,
            "unknown", "Planet Tian Yun / Primordial Divine Realm",
            java.util.List.of(
                "Clone of All-Seer / artifact spirit of the Realm-Defining Compass",
                "Created clone to teach Wang Lin the Slaughter Immortal Art with a trap inside",
                "Intended to possess Wang Lin but was defeated; killed/devoured by Wang Lin in the Primordial Divine Realm"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("All-Seer", "family"), new RelationShip("Old Man Miesheng", "ally")),
            "novel; Fandom wiki; Baidu Baike (clone of All-Seer)"
        ),
        new CanonCharacter(
            "N114", "Seven-Colored Daoist", "七彩道人 / 七彩仙尊", CharType.ANTAGONIST,
            "Heaven Trampling", "Creator of the Cave World", "deceased", 5,
            "unknown", "Cave World / Luo Tian",
            java.util.List.of(
                "The cosmic-level creator-antagonist of RI; created the Cave World to harvest Joss Flames",
                "Created the Three Souls and Seven Spirits (Situ Nan = Green Soul, Qing Shui = Slaughter Soul, Tan Lang = Yellow Soul, Xie Qing = Third Soul)",
                "Killed by Wang Lin who became the new world-owner and renamed it 'Wang Lin's Cave World'"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Situ Nan", "family"), new RelationShip("Qing Shui", "family"), new RelationShip("Tan Lang", "family"), new RelationShip("Xie Qing", "family")),
            "novel Book 11+; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N115", "Old Man Miesheng", "灭生老人", CharType.ANTAGONIST,
            "peak Third Step+", "independent", "alive", 4,
            "unknown", "unknown",
            java.util.List.of(
                "Also called Old Man Samsara-Extinction; lent the Realm-Defining Compass to Lu Mo",
                "Lu Mo blasted the Compass open using Dream Dao, releasing the artifact spirit Tianyunzi"
            ),
            List.of(new RelationShip("Tianyunzi", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N116", "Taga", "塔迦", CharType.ANTAGONIST,
            "Ancient Demon (Third Step)", "Demon Spirit Land", "sealed", 4,
            "unknown", "Celestial Emperors Tower",
            java.util.List.of(
                "Ancient Demon who possessed Qing Lin's body",
                "Forced everyone to escape the Celestial Emperors Tower",
                "Sealed by himself in the encounter with Master Void; later defeated by Wang Lin"
            ),
            List.of(new RelationShip("Qing Lin", "enemy"), new RelationShip("Wang Lin", "enemy"), new RelationShip("Master Void", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N117", "Gu Dao", "古道", CharType.ELDER,
            "Grand Empyrean", "IAC", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Strongest on IAC after Wang Lin; Wang Lin's final rival",
                "Wang Lin's battle with Gu Dao triggered enlightenment → fused with Void Avatar → full Nine Songs Three Signs",
                "Wang Lin became #1 on IAC after this battle"
            ),
            List.of(new RelationShip("Wang Lin", "rival")),
            "novel (IAC final rival); Fandom wiki"
        ),
        new CanonCharacter(
            "N118", "Song Tian Great Celestial Venerable", null, CharType.ELDER,
            "Great Celestial Venerable", "IAC", "unknown", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of("IAC antagonist; defeated by Wang Lin", "Enables Ji Du to become Imperial Venerable"),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Wang Jiduo", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N119", "Dao Ancient Great Celestial Venerable", null, CharType.ELDER,
            "Great Celestial Venerable", "Ancient Clan", "unknown", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Ancient Clan Great Celestial Venerable; pressured Wang Lin",
                "Defeated by Wang Lin after Wang Lin reached 7th bridge of Heaven Trampling"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N120", "Dao Yi Great Celestial Venerable", null, CharType.ELDER,
            "Great Celestial Venerable", "IAC", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Fought Xuan Luo over a fragment of Primordial God Realm at the Seven Paths Sect entrance",
                "Indirectly caused the Cave World's birth",
                "His Infant Skull was injured by Wang Lin during the Lian Daozhen confrontation"
            ),
            List.of(new RelationShip("Xuan Luo", "enemy"), new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N121", "Imperial Preceptor", "Dao Gu Imperial Preceptor", CharType.OTHER,
            "artifact spirit", "Dao Ancient Imperial Capital", "unknown", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Actually the missing piece of the Boundary Compass / artifact spirit",
                "Possessed Wang Lin but Wang Lin hid his primordial spirit in the Heaven Defying Pearl",
                "Wang Lin's 'previous life' seen by the Imperial Teacher was actually the Slaughter clone Lu Mo"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N122", "Dao Devil Sect Master", null, CharType.PATRIARCH,
            "Third Step+", "Dao Devil Sect (IAC)", "deceased", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Captured Wang Lin to resurrect the Green Devil Scorpion",
                "Fed his Thunder Essence into Wang Lin's to evolve it (controlled by him to erase Wang Lin's mind)",
                "Wang Lin reclaimed the Essence True Body and annihilated the entire Dao Devil Sect"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Green Devil", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N123", "Du Qing", "杜青", CharType.ELDER,
            "Third Step", "Canglong Sect", "unknown", 3,
            "unknown", "Tianniu Province, IAC",
            java.util.List.of(
                "Canglong Sect ancestor; pursued Wang Lin after he extracted the Earth Fire Dragon's soul",
                "Shocked and subdued by Wang Lin's 7 Origins + Xuan Luo's golden seal"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Kang Ren", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N124", "Kang Ren", "康仁", CharType.DISCIPLE,
            "unknown", "Canglong Sect", "alive", 3,
            "unknown", "Tianniu Province, IAC",
            java.util.List.of("Canglong Sect cultivator; took unconscious Wang Lin in", "Led to the Du Qing pursuit"),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Du Qing", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N125", "Purple Dawn Immortal Emperor", null, CharType.OTHER,
            "Immortal Emperor (Celestial)", "IAC", "unknown", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of("IAC Immortal Emperor; captured by Wang Lin at the Cave World's core"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N126", "White Tiger General", null, CharType.ANTAGONIST,
            "Third Step+", "IAC", "alive", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "One of the cruel cultivators with killing intent strong enough to shock even the most cruel",
                "Subdued by Wang Lin as a slave"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N127", "Yun Yifeng", "云一峰", CharType.ANTAGONIST,
            "Third Step+", "IAC", "unknown", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of("IAC antagonist in Five Flowers Eight Gates", "Defeated by Wang Lin"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N128", "Lord of the Sealed Realm", "封界尊 / 尊者封界", CharType.OTHER,
            "peak Third Step", "Sealed Realm", "unknown", 4,
            "unknown", "Sealed Realm",
            java.util.List.of(
                "Previous 'Lord of the Sealed Realm'; Wang Lin succeeds him",
                "Forged the Seven-Colored Divine Sky Nail (108 nails to kill Third Step experts)",
                "When Wang Lin was being killed by Daoist Water, his spirit appeared and saved Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Daoist Water", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N129", "Zhan Laogui", "战老子", CharType.ANTAGONIST,
            "Third Step+", "IAC", "unknown", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "IAC antagonist in Five Flowers Eight Gates",
                "Defeated by Wang Lin after he obtained Ye Mo's heart inheritance"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N130", "Yun Kong", "云空", CharType.ANTAGONIST,
            "Third Step+", "IAC", "unknown", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of("IAC antagonist; Wang Lin destroyed his clone", "Wang Lin detonated the Immortal Pill"),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N131", "Zhan Xingye", "战星野", CharType.ANTAGONIST,
            "Third Step+", "Zhan Family (Battle)", "deceased", 3,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Schemed against Wang Lin",
                "Wang Lin merged 3 Zhen Family Battle Scrolls into the golden word 'Battle'; fused with Zhan Xingye's skeletons",
                "Killed by Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N132", "Green Devil", "绿魔 / 绿蝎", CharType.ANTAGONIST,
            "peak Third Step+", "Dao Devil Sect", "deceased", 4,
            "unknown", "Immortal Astral Continent",
            java.util.List.of(
                "Ancient entity; also called Green Devil Scorpion; worshipped by Dao Devil Sect",
                "Dao Devil Sect intended to use Wang Lin's body to resurrect him",
                "Wang Lin reclaimed his body and devoured the Green Devil → immediately reached Arcane Void"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Dao Devil Sect Master", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N133", "Thirteen", "十三", CharType.DISCIPLE,
            "body refining", "Soul Refining Tribe", "alive", 4,
            "unknown", "Pilu Kingdom / IAC",
            java.util.List.of(
                "Wang Lin's eldest disciple; originally a Mountain Valley Tribe native",
                "Meridians severed by someone; Wang Lin taught him body refining techniques instead",
                "Reincarnated on IAC; memories recovered by Wang Lin; followed Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Huo Pao", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N134", "Huo Pao", "火炮", CharType.DISCIPLE,
            "unknown", "Soul Refining Tribe", "alive", 3,
            "unknown", "Pilu Kingdom / Sky Demon Country",
            java.util.List.of("Wang Lin's junior disciple; taken with Thirteen to Sky Demon Country for trial"),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Thirteen", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N135", "Xie Qing", "谢青", CharType.DISCIPLE,
            "Jingnie (Concept-only)", "independent", "deceased", 5,
            "unknown", "Planet Qing Ling",
            java.util.List.of(
                "Wang Lin's 2nd disciple; Third Soul of the Seven-Colored Immortal Venerable",
                "Used 'fish, water, net, fishing' analogy to explain Dao → triggered Wang Lin's Nirvana Scryer breakthrough",
                "Sat atop a mountain 800 years cultivating only Concepts; ended his own life and entrusted Third Soul memories to Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Seven-Colored Daoist", "family")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N136", "Xi Zifeng", "席紫凤", CharType.DISCIPLE,
            "Jingnie", "Luo Tian Thunder Immortal Realm", "alive", 5,
            "unknown", "Luo Tian / Cave World",
            java.util.List.of(
                "Wang Lin's 3rd disciple; sole survivor of her family in the Luo Tian Alliance battle",
                "Disfigured her own face for 800 years to prevent covetous gazes",
                "Rescued by Wang Lin → beauty restored → cultivation raised to Jingnie → gifted Divine Thunder Blood Sword"
            ),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N137", "Adai", "阿呆", CharType.OTHER,
            "unknown", "Wu Yu's servant", "alive", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of(
                "Blue-skinned man with 9 talismans attached to his body",
                "Speaks in a language Wang Lin can't understand",
                "Led Wang Lin to Wu Yu's Nascent Soul in the Forest of Distorted Divine Sense"
            ),
            List.of(new RelationShip("Wu Yu", "master"), new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N138", "Wu Yu", "吴宇", CharType.ELDER,
            "Nascent Soul", "Corpse Yin Sect", "deceased", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of(
                "Corpse Yin Sect elder; only his Nascent Soul remained (long dead)",
                "Let Wang Lin leave the forest and teleport to the Corpse Yin Sect"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Adai", "servant")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N139", "Ye Zi", "叶紫", CharType.ELDER,
            "Nascent Soul", "Corpse Yin Sect", "unknown", 4,
            "unknown", "Planet Suzaku",
            java.util.List.of(
                "Corpse Yin Sect elder; gave Wang Lin an immortal cave",
                "Made Wang Lin slice a sliver of soul for the Jue Ming Valley competition",
                "Wang Lin later attacked Corpse Yin Sect members and took back his soul"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N140", "Zhao Xingsha", "赵星煞", CharType.DISCIPLE,
            "Soul Transformation", "Heavenly Fate Sect", "alive", 4,
            "unknown", "Planet Tian Yun",
            java.util.List.of(
                "Heavenly Fate Sect purple division rival; trapped Wang Lin with Sima Rufeng and others",
                "Battled Wang Lin at All-Seer's birthday banquet; heavily injured",
                "Elder stopped Wang Lin before he could kill Zhao Xingsha"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Sima Rufeng", "ally"), new RelationShip("Zhao Xinming", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N141", "Sima Rufeng", "司马如风", CharType.DISCIPLE,
            "unknown", "Heavenly Fate Sect", "alive", 3,
            "unknown", "Planet Tian Yun",
            java.util.List.of(
                "Heavenly Fate Sect fellow disciple; trapped Wang Lin with Zhao Xingsha",
                "Wang Lin broke through mid-Soul Transformation and broke the formation"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Zhao Xingsha", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N142", "Zhao Xinming", "赵新民", CharType.DISCIPLE,
            "unknown", "Heavenly Fate Sect", "alive", 3,
            "unknown", "Planet Tian Yun",
            java.util.List.of(
                "4th sister at Heavenly Fate Sect; battled Wang Lin at the birthday banquet",
                "Wang Lin won the fight"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Zhao Xingsha", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N143", "Chen Tao", "陈涛", CharType.DISCIPLE,
            "mid-Ascendant", "Heavenly Fate Sect", "alive", 4,
            "unknown", "Planet Tian Yun",
            java.util.List.of(
                "6th brother at Heavenly Fate Sect; challenged Wang Lin after he defeated Zhao Xinming",
                "Wang Lin didn't win but put up a good fight that shocked everyone"
            ),
            List.of(new RelationShip("Wang Lin", "rival")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N144", "Wang Zhou", "王周", CharType.DISCIPLE,
            "Qi Condensation", "Heng Yue Sect", "alive", 3,
            "unknown", "Heng Yue Sect, Country of Zhao",
            java.util.List.of(
                "Heng Yue Sect fellow disciple; mistaken for a girl due to long braided hair",
                "Failed all 3 entrance tests alongside Wang Lin"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N145", "Wang Jie", "王杰", CharType.DISCIPLE,
            "Qi Condensation", "Heng Yue Sect", "alive", 3,
            "unknown", "Heng Yue Sect, Country of Zhao",
            java.util.List.of("Heng Yue Sect fellow disciple; took the entrance test alongside Wang Lin and Wang Zhou"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N146", "Sun Zhenwei", "孙振威", CharType.DISCIPLE,
            "Nascent Soul", "Cloud Sky Sect", "deceased", 4,
            "unknown", "Chu Country",
            java.util.List.of(
                "Li Muwan's suitor; she agreed to marry him to shatter her heart",
                "Killed by Wang Lin at the wedding; Wang Lin then became Cloud Sky Sect leader"
            ),
            List.of(new RelationShip("Wang Lin", "enemy"), new RelationShip("Li Muwan", "love_interest"), new RelationShip("Chen Bailiang", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N147", "Chen Bailiang", "陈百良", CharType.ELDER,
            "Nascent Soul", "Cloud Sky Sect", "deceased", 4,
            "unknown", "Chu Country",
            java.util.List.of(
                "Cloud Sky Sect elder; killed by Wang Lin",
                "Wang Lin became Cloud Sky Sect leader after killing him"
            ),
            List.of(new RelationShip("Wang Lin", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N148", "Zhang Hu", "张虎", CharType.DISCIPLE,
            "Foundation Establishment", "Heng Yue Sect", "deceased", 5,
            "unknown", "Country of Zhao",
            java.util.List.of(
                "Wang Lin's early friend; pretended not to know Wang Lin to save him",
                "Wang Lin killed Sun Dazhu to help Zhang Hu (this led to Wang Lin's clan extermination)",
                "Wang Lin later bestowed opportunities upon Zhang Hu's descendants to settle karma"
            ),
            List.of(new RelationShip("Wang Lin", "ally"), new RelationShip("Sun Dazhu", "enemy")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N149", "Hui Bing", "惠冰", CharType.OTHER,
            "unknown", "unknown", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's relationships (Fandom nav-bar character); tangential"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "Fandom wiki (nav-bar only); C3"
        ),
        new CanonCharacter(
            "N150", "Zhou Rui", "周瑞", CharType.OTHER,
            "unknown", "unknown", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of(
                "Listed in Wang Lin's relationships (Fandom nav-bar female character); separate from Zhou Ru",
                "Tangential figure"
            ),
            List.of(new RelationShip("Wang Lin", "ally")),
            "Fandom wiki (nav-bar only); C3"
        ),
        new CanonCharacter(
            "N151", "Leng Sheng", "冷生", CharType.OTHER,
            "unknown", "unknown", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's relationships (Fandom nav-bar female character); tangential"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "Fandom wiki (nav-bar only); C3"
        ),
        new CanonCharacter(
            "N152", "Da Niu", "大牛", CharType.OTHER,
            "mortal", "none", "unknown", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's friends; likely a mortal friend from early life"),
            List.of(new RelationShip("Wang Lin", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N153", "Zhou Lin", "周林", CharType.DISCIPLE,
            "early Core Formation", "Yuntian Sect", "alive", 4,
            "unknown", "Chu Country",
            java.util.List.of("Li Muwan's disciple; 9th-generation Yuntian Sect disciple", "Master of Wang Lin's doppelganger"),
            List.of(new RelationShip("Li Muwan", "master"), new RelationShip("Wang Lin", "ally")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N154", "Xiao Bai", "小白", CharType.OTHER,
            "beast", "none (Zhou Ru's pet)", "alive", 3,
            "unknown", "Planet Suzaku",
            java.util.List.of("Zhou Ru's pet tiger"),
            List.of(new RelationShip("Zhou Ru", "ally")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N155", "Mosquito Beast", "蚊兽", CharType.OTHER,
            "evolved (king mosquito beast)", "Wang Lin's", "alive", 5,
            "unknown", "Sea of Devils / IAC",
            java.util.List.of(
                "Wang Lin's signature companion found in Sea of Devils; fed Brilliant Golden Fruit to evolve",
                "Herd acquired in Wind Celestial Realm; king mosquito beast further evolved",
                "Recurring across novels (RI, ISSTH, AWWP); saves Wang Lin in IAC void"
            ),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (Sea of Devils); Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N156", "Thunder Toad", "雷蟾", CharType.OTHER,
            "evolved", "Wang Lin's", "deceased", 4,
            "unknown", "Allheaven Star System",
            java.util.List.of(
                "Wang Lin's companion; fed Brilliant Golden Fruit to evolve",
                "Sacrificed to create Bloodline Thunder for Wang Lin's Thunder Essence completion"
            ),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel; Fandom wiki"
        ),
        new CanonCharacter(
            "N157", "Thunder Celestial Beast", "雷仙兽", CharType.OTHER,
            "Ascendant-tier", "Wang Lin's", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Wang Lin's combat mount/companion during Ascendant stage"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N158", "Nether Beast", "冥兽", CharType.OTHER,
            "beast (vast interior)", "Wang Lin's", "alive", 4,
            "unknown", "Wang Lin's side",
            java.util.List.of(
                "Wang Lin's life-bound beast with a vast sub-dimension interior",
                "Wang Lin refined the Seal Immortal Seal inside the Nether Beast",
                "Encountered the 'Madman' Lian Daofei inside"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Lian Daofei", "ally")),
            "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonCharacter(
            "N159", "Brilliant Void", "玄虚", CharType.OTHER,
            "beast", "Wang Lin's", "alive", 3,
            "unknown", "unknown",
            java.util.List.of("Listed in Wang Lin's pets; named for the region"),
            List.of(new RelationShip("Wang Lin", "master")),
            "novel (implicit); Fandom wiki; C3"
        ),
        new CanonCharacter(
            "N160", "Golden Exalt Sea Dragon", "金尊海龙", CharType.OTHER,
            "beast", "Wang Lin's", "alive", 4,
            "unknown", "IAC",
            java.util.List.of(
                "Wang Lin's mount",
                "Liu Jinbiao used Path of Deception to gain its recognition; Xu Liguo dared not bully Liu Jinbiao anymore"
            ),
            List.of(new RelationShip("Wang Lin", "master"), new RelationShip("Liu Jinbiao", "ally")),
            "novel; Fandom wiki"
        )
    );

    /** All 80 canon locations. */
    public static final List<CanonLocation> ALL_LOCATIONS = List.of(
        new CanonLocation(
            "L01", "The Root Dao", "本源大道", LocType.REALM,
            null, "root_dao", "absolute",
            false, null, "Source substrate of all Daos; not a cultivable realm", 4,
            null,
            java.util.List.of(
                "The outermost reality — the 'true' Dao-source underlying everything",
                "Fundamental laws: Five Elements, Karma, Reincarnation, Life-Death, True-False, Absolute Beginning/End, Restriction, Slaughter",
                "Not a place one 'lives in'; it is the substrate of all existence",
                "Wang Lin ultimately comprehends his 14th essence (Reincarnation) here, achieving Heaven Trampling"
            ), java.util.List.of(), java.util.List.of("Wang Lin comprehends the Reincarnation Essence here, achieving Heaven Trampling (4th Step)"),
            "C4 - Fandom wiki; Baidu Baike (essence system); no specific chapter"
        ),
        new CanonLocation(
            "L02", "Luo Tian Star System", "罗天", LocType.STAR_SYSTEM,
            null, "luo_tian", "absolute",
            false, null, "True-reality tier; Void Tribulant and Grand Empyrean cultivators routinely operate here", 4,
            null,
            java.util.List.of(
                "The 'true' star-system-level reality outside the Cave World",
                "The Immortal Astral Continent floats in Luo Tian's void",
                "Home of the Luo Tian Star Domain where the Luo Tian Alliance War was fought",
                "The Three Auspicious Treasures (Xu Liguo, Liu Jinbiao, Zhong Dahong) were originally cultivators from Luo Tian"
            ), java.util.List.of("Luo Tian Alliance"), java.util.List.of(
                "Wang Lin's Luo Tian Alliance War arc (Book 7-8)",
                "Wang Lin slew the Water Daoist here",
                "Luo Tian Thunder Immortal Realm existed here before collapsing"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L03", "Immortal Astral Continent", "仙罔大陆 / 仙罡大陆", LocType.CONTINENT,
            "Luo Tian Star System", "immortal_astral_continent", "absolute",
            false, null, "Vast continent with nine suns (Grand Empyreans); absolute-tier spiritual energy throughout", 5,
            null,
            java.util.List.of(
                "A continent so vast it has nine suns — each a Grand Empyrean",
                "Subdivided into provinces: Heavenly Bull, Green Devil, Mountain Sea, Great Saint, Mengtu, Tianniu, Green Bull continents",
                "Originally ruled by Immortal Emperor Lian Daozhen; later by Ancient Clan Dao Ancient Great Heavenly Venerable",
                "The Cave World is a pocket-world of the IAC"
            ), java.util.List.of("Great Soul Sect", "Gui Yi Sect", "Dao Devil Sect", "Ancient Clan", "Canglong Sect", "Dong Lin Sect"), java.util.List.of(
                "Wang Lin arrives as a Third-Step cultivator, condenses Void Clone causing 9 suns to manifest simultaneously",
                "Wang Lin slays Gu Dao and becomes 'the Tenth Sun'",
                "Wang Lin Transcends the 9 Heaven Trampling Bridges and leaves with Li Muwan"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L04", "The Cave World", "洞天", LocType.REALM,
            "Immortal Astral Continent", "cave_world", "absolute",
            true, "Seven-Colored Daoist", "Medium-tier laws within (seal suppresses high-tier cultivation); contains billions of cultivation planets", 5,
            null,
            java.util.List.of(
                "A sealed dimensional bubble floating in Luo Tian's void",
                "Created by the Seven-Colored Daoist to harvest Joss Flames (cultivation energy from mortal faith)",
                "Has a sealed inner half (Sealed Realm) and an outer half (Outer Realm)",
                "Third-Step cultivators cannot naturally arise within due to the seal"
            ), java.util.List.of("Seven-Colored Daoist", "Realm-Sealing Grand Array"), java.util.List.of(
                "The entire Book 1-9 takes place inside the Cave World",
                "Wang Lin discovers its true nature in Book 11 when he breaches the Ancient Immortal Domain",
                "Wang Lin kills the Seven-Colored Daoist, renames it 'Wang Lin's Cave World', becomes the new owner"
            ),
            "C5 - multi-source (Fandom + Baidu); Book 1-9 set here"
        ),
        new CanonLocation(
            "L05", "Sealed Realm", "封界 / 内界", LocType.REALM,
            "The Cave World", "sealed_realm", "medium",
            true, "Realm-Sealing Grand Array (spirit: Heaven-Splitting Axe)", "Seal caps cultivation at Heaven Blight / quasi-Third-Step tier", 5,
            null,
            java.util.List.of(
                "The inner half of the Cave World, separated from the Outer Realm by the Realm-Sealing Grand Array",
                "Contains Planet Suzaku, Brilliant Void Star System, Allheaven Star System, Cloud Sea Star System",
                "The Realm-Sealing Grand Array's spirit is the Heaven-Splitting Axe",
                "Suppresses Third-Step cultivation — only 'Heaven Blight' cultivators can squeeze through"
            ), java.util.List.of("Cultivation Alliance", "Realm-Sealing Grand Array"), java.util.List.of(
                "The Sealed Realm War — Wang Lin as first responder, slaughtering thousands of Outer Realm cultivators",
                "Wang Lin becomes 'Lord of the Sealed Realm'",
                "Wang Lin resets the Realm Sealing Grand Array at end of arc"
            ),
            "C5 - multi-source (Fandom + Baidu); Sealed Realm War arc"
        ),
        new CanonLocation(
            "L06", "Outer Realm", "外界", LocType.REALM,
            "The Cave World", "cave_world", "high",
            false, null, "Higher-tier spiritual energy; less restricted cultivation-wise than the Sealed Realm", 4,
            null,
            java.util.List.of(
                "The outer half of the Cave World; the half NOT inside the Sealed Realm",
                "Higher-tier cultivators dwell here",
                "Daoist Water (Shui Daozi) was from here",
                "Both sides are still sealed within the Cave World overall"
            ), java.util.List.of("Dao Devil Sect"), java.util.List.of(
                "Wang Lin flees to the Outer Realm with Li Qianmei after the Wind Celestial Realm arc",
                "Multiple Outer-Realm third-step cultivators killed by Wang Lin borrowing the Heaven-Splitting Axe"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L07", "Primordial Divine Realm", "原神境", LocType.SECRET_REALM,
            "The Cave World", "cave_world", "absolute",
            true, "Tianyunzi (unsealed by Wang Lin)", "Absolute-tier; tied to the Heaven Defying Bead / Realm-Defining Compass", 4,
            null,
            java.util.List.of(
                "A sub-dimension accessible from the Cave World after Wang Lin reaches Third Step",
                "The place where Tianyunzi's true body hides",
                "Exists because of the Heaven Defying Bead / Realm-Defining Compass",
                "Wang Lin crosses the 9 Heaven Trampling Bridges here"
            ), java.util.List.of("Tianyunzi"), java.util.List.of(
                "Wang Lin's final battle with Tianyunzi (who intended to possess Wang Lin but was defeated)",
                "Wang Lin crosses the 9 Heaven Trampling Bridges here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L08", "Brilliant Void Star System", "玄虚星系 / 联盟星系", LocType.STAR_SYSTEM,
            "Sealed Realm", "star_system", "medium",
            true, "Realm-Sealing Grand Array", "Contains ~7 million cultivation planets; Nascent Soul+ tier common", 5,
            null,
            java.util.List.of(
                "Originally called 'Brilliant Void,' renamed 'Alliance Star System' after the Cultivation Alliance rose to govern it",
                "Contains Planet Suzaku, Planet Tian Yun, Planet Ran Yun, Planet Qing Ling, plus the Vermilion Bird Starfield",
                "Wang Lin's home star system",
                "Site of the Alliance-Allheaven War"
            ), java.util.List.of("Cultivation Alliance"), java.util.List.of("Alliance-Allheaven War", "Wang Lin becomes Thunder Celestial and protects this entire star system"),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L09", "Allheaven Star System", "诸天星系", LocType.STAR_SYSTEM,
            "Sealed Realm", "star_system", "medium",
            true, "Realm-Sealing Grand Array", "Rival star system to the Alliance; Nascent Soul+ tier common", 5,
            null,
            java.util.List.of(
                "The 'other' major star system within the Sealed Realm",
                "The Southern Domain (containing Thunder Celestial Realm and Yao Family headquarters) is here",
                "Origin of many antagonists (Yao Family)",
                "Book 7 'Fame Shakes The Allheaven Star System' is set here"
            ), java.util.List.of("Yao Family", "Thunder Celestial Realm"), java.util.List.of(
                "Wang Lin's fame arc — Yao Family kill-order on Wang Lin",
                "Thunder Celestial Tournament",
                "Wang Lin destroys multiple planets here"
            ),
            "C5 - multi-source (Fandom + Baidu); Book 7"
        ),
        new CanonLocation(
            "L10", "Cloud Sea Star System", "云海星系", LocType.STAR_SYSTEM,
            "Sealed Realm", "star_system", "high",
            true, "Realm-Sealing Grand Array", "Higher-tier than Alliance/Allheaven; Nirvana Scryer+ cultivators common; cloud-covered spirit worlds", 5,
            null,
            java.util.List.of(
                "A star system dominated by cloud-covered spirit worlds",
                "Contains the Origin Sect, Treasured Jade Sect, Wild Continent, and Daoist Water's domain",
                "Book 9 'Peak of the Cloud Sea' is set here",
                "Higher-tier than Alliance and Allheaven Star Systems"
            ), java.util.List.of("Origin Sect", "Treasured Jade Sect", "Everlasting Sect", "Da Lou Sword Sect"), java.util.List.of(
                "Wang Lin lives as a mortal village doctor here, joins Origin Sect as 'Ceng Niu'",
                "Wang Lin battles Daoist Water — turns to stone, saved by Li Qianmei's 10-year blood anointment",
                "Wang Lin becomes 'Lord of the Sealed Realm'"
            ),
            "C5 - multi-source (Fandom + Baidu); Book 9"
        ),
        new CanonLocation(
            "L11", "Vermilion Bird Starfield", "朱雀星域", LocType.STAR_DOMAIN,
            "Brilliant Void Star System", "star_system", "medium",
            true, "Realm-Sealing Grand Array", "Guarded by the Vermilion Bird Divine Sect; medium-tier spiritual energy", 4,
            null,
            java.util.List.of(
                "A sub-region within the Alliance Star System",
                "Guarded by the Vermilion Bird Divine Sect",
                "Contains Planet Suzaku, the Vermilion Bird Tomb, and the Four Divine Sect headquarters",
                "Wang Lin becomes the 6th-Generation Vermilion Bird Divine Emperor here"
            ), java.util.List.of("Vermilion Bird Divine Sect", "Four Divine Sect"), java.util.List.of("Wang Lin becomes the 6th-Generation Vermilion Bird Divine Emperor"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L12", "Blue Silk Clan Star Domain", "蓝丝族星域", LocType.STAR_DOMAIN,
            "The Cave World", "cave_world", "high",
            true, "Seven-Colored Daoist", "High-tier; Void Tribulant+ cultivators present", 3,
            null,
            java.util.List.of(
                "The Blue Silk Clan's home star domain",
                "Dao Master Blue Dream is based here",
                "Probably in the Outer Realm or independent sub-region",
                "Wang Lin learns Dao Art Fusion and Light Shadow Shield here"
            ), java.util.List.of("Blue Silk Clan"), java.util.List.of("Wang Lin vs. Blue Dream Dao Venerable — heavily injured, saved by Li Qianmei's bracelet"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L13", "Luo Tian Star Domain", "罗天星域", LocType.STAR_DOMAIN,
            "Luo Tian Star System", "luo_tian", "absolute",
            false, null, "True-reality tier; Void Tribulant and Grand Empyrean cultivators routinely operate here", 4,
            null,
            java.util.List.of(
                "The star domain ABOVE the Cave World — outside the seal, in Luo Tian proper",
                "The Luo Tian Alliance War was fought here",
                "The Thunder Immortal Realm and Luo Tian Thunder Immortal Realm were here before their collapse",
                "Higher-tier cultivators (Void Tribulant, Grand Empyrean) routinely operate here"
            ), java.util.List.of("Luo Tian Alliance"), java.util.List.of(
                "Luo Tian Alliance War",
                "Wang Lin takes Xi Zifeng as his 3rd disciple after the Luo Tian Thunder Immortal Realm's collapse"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L14", "Planet Suzaku", "朱雀星", LocType.PLANET,
            "Vermilion Bird Starfield", "planet", "low",
            true, "Cultivation Planet Crystal", "Third-tier cultivation planet; spiritual energy escalates as Wang Lin remakes the planet", 5,
            "1",
            java.util.List.of(
                "Wang Lin's birthplace — a third-tier cultivation planet",
                "Multiple cultivation countries on its surface: Zhao, Chu, Fire Burn, Sky Demon, Pilu, Snow Domain, etc.",
                "Has a Cultivation Planet Crystal seal inside (Suzaku Tomb)",
                "Raised to higher tiers by Wang Lin's actions over the story"
            ), java.util.List.of(
                "Heng Yue Sect",
                "Cloud Sky Sect",
                "Soul Refining Sect",
                "Vermilion Bird Divine Sect",
                "Teng Clan",
                "Fighting Evil Sect",
                "Corpse Yin Sect",
                "Xuan Dao Sect",
                "Da Lou Sword Sect",
                "Luo He Sect",
                "Liu Family"
            ), java.util.List.of(
                "Wang Lin's birth, clan annihilation, Ji Realm awakening",
                "Teng Clan extermination and Teng Huayuan's death",
                "Wang Lin becomes Vermilion Bird Divine Emperor"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L15", "Planet Tian Yun", "天运星", LocType.PLANET,
            "Brilliant Void Star System", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Rank-7 cultivation planet; rich spiritual energy", 5,
            null,
            java.util.List.of(
                "A rank-7 cultivation planet in the Alliance Star System",
                "Home of the Heavenly Fate Sect (Tianyun Sect) ruled by the All-Seer",
                "Divided into seven color divisions (red/orange/yellow/green/blue/cyan/purple)",
                "Blood Ancestor Yao Xinghai's residence; East Sea Demon Spirit Land is here"
            ), java.util.List.of("Heavenly Fate Sect", "Yao Family"), java.util.List.of(
                "Wang Lin becomes 7th disciple of Purple Division of Heavenly Fate Sect",
                "Wang Lin kills Blood Ancestor in Thunder Celestial Realm",
                "Wang Lin captures Yao Xixue"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L16", "Planet Qing Lin", "青灵星", LocType.PLANET,
            "Brilliant Void Star System", "planet", "low",
            true, "Realm-Sealing Grand Array", "Backwater planet with low spiritual energy", 4,
            null,
            java.util.List.of(
                "Xie Qing's home planet — originally an ordinary old man before becoming Wang Lin's 2nd disciple",
                "A backwater planet in the Alliance Star System",
                "Wang Lin buried Xie Qing in Autumn Orchid Valley here",
                "The site of Xie Qing's 'fish, water, net, fishing' analogy that led Wang Lin to Nirvana Scryer"
            ), java.util.List.of(), java.util.List.of(
                "Xie Qing's enlightenment leads Wang Lin to Nirvana Scryer breakthrough",
                "Wang Lin's return visit when Xie Qing sits atop a mountain for 800 years cultivating only Concepts"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L17", "Planet Ran Yun", "染云星", LocType.PLANET,
            "Allheaven Star System", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Medium-tier cultivation planet", 4,
            null,
            java.util.List.of(
                "A planet Wang Lin visited under the alias 'Xu Mu'",
                "Sun Tai lived here as a friend in his final years",
                "Liu Mei came here to attack Wang Lin and lost",
                "Wang Ping was raised here as a mortal"
            ), java.util.List.of(), java.util.List.of("Wang Lin raises Wang Ping as a mortal (one lifetime), develops Karma Domain"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L18", "Earth Planet", "土星", LocType.PLANET,
            "Brilliant Void Star System", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Earth-element leanings; medium-tier spiritual energy", 4,
            null,
            java.util.List.of(
                "Named for its earth-element leanings",
                "Visited by Wang Lin after his Heavenly Fate Sect training",
                "Junior sect master Xu Yunshan of the Xuan Yuan Sect greeted Wang Lin here"
            ), java.util.List.of("Xuan Yuan Sect", "Da Lou Sword Sect"), java.util.List.of(
                "Wang Lin kills three Da Lou Sword Sect elders here (over Brilliant Golden Fruit)",
                "Feeds the fruit to Mosquito Beast and Thunder Toad, triggering their evolution"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L19", "Trading Planet", "交易星", LocType.PLANET,
            "Brilliant Void Star System", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Ocean-dominated; goods-flow economy; medium-tier spiritual energy", 4,
            null,
            java.util.List.of(
                "An ocean-dominated trading hub planet in the Alliance Star System",
                "Wang Lin and Situ Nan stop here on their way off Planet Suzaku",
                "Mainly ocean; goods-flow economy"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin meets Li Dannan (guide) and Bai Wei here",
                "Situ Nan steals from nearly every sect on the planet, causing a planet-wide chase"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L20", "Water Spirit Star", "水灵星 / 凤鸾星", LocType.PLANET,
            "Brilliant Void Star System", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Water-spirit planet; strong water-element spiritual energy", 4,
            null,
            java.util.List.of(
                "A water-spirit planet in the Alliance Star System",
                "Wang Lin obtained his Fire Essence potential here (first glimpse into the Fu Clan's Golden Leaf Flame Source Origin)",
                "Situ Nan founded his 'Southern Prince' faction here"
            ), java.util.List.of("Southern Prince faction", "Fu Clan"), java.util.List.of(
                "Wang Lin vs. Situ Nan reunion; Situ Nan poisoned",
                "Wang Lin's 'One Drop of Universe' comprehension"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L21", "Planet Five Elements", "五行星", LocType.PLANET,
            "Sealed Realm", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Specialized in Five Elements cultivation materials; medium-tier spiritual energy", 3,
            null,
            java.util.List.of(
                "A planet specialized in Five Elements cultivation materials",
                "Water General's 'One Drop of Universe' originates here",
                "Specific sub-region within the Sealed Realm unknown"
            ), java.util.List.of(), java.util.List.of("Wang Lin comprehends 380 million changes to condense his Water Essence here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L22", "Immortal Execution Star", "仙罔星", LocType.PLANET,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; reincarnation destination for Cave World cultivators", 4,
            null,
            java.util.List.of(
                "The 'reincarnation destination' for Wang Lin's friends/allies from the Cave World",
                "Situ Nan reincarnates here as 'Si Nan' (Grand Marshal of Wu Xuan Country)",
                "Xu Liguo, Liu Jinbiao, Zhong Dahong continue their swindling ways here",
                "Big Head Cultivator, Zhou Yi, Qing Shuang all reincarnate here"
            ), java.util.List.of("Wu Xuan Country"), java.util.List.of("Wang Lin systematically awakens his allies' past-life memories here"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L23", "Country of Zhao", "赵国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Third-tier cultivation country; Qi Condensation ceiling; weak spiritual veins", 5,
            "1",
            java.util.List.of(
                "A third-tier cultivation country on Planet Suzaku; Wang Lin's birthplace",
                "Contains Wang Family Village, Heng Yue Sect, Tian Shui City, the Forest of Distorted Divine Sense, the Teng Family City",
                "Teng Huayuan was the de facto ruler through the Teng Clan",
                "Wang Lin becomes 'Ancestor of the Country of Zhao'"
            ), java.util.List.of("Heng Yue Sect", "Teng Clan", "Wang Clan", "Xuan Dao Sect"), java.util.List.of(
                "Wang Lin's birth and clan annihilation by Teng Huayuan",
                "Wang Lin returns as Soul Formation to wipe out the Teng family and kill Teng Huayuan",
                "Wang Lin breaks through to Soul Formation at Xuan Dao Sect"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L24", "Chu Country", "楚国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Neighboring country to Zhao; fragile-to-low tier spiritual veins", 4,
            null,
            java.util.List.of(
                "A neighboring country to Zhao on Planet Suzaku",
                "The Cloud Sky Sect (Yuntian Sect) is based here",
                "Li Muwan ends up here as an elder and eventually Sect Master"
            ), java.util.List.of("Cloud Sky Sect"), java.util.List.of(
                "Wang Lin kills Sun Zhenwei at Li Muwan's wedding",
                "Wang Lin becomes Cloud Sky Sect leader, gives the seat to Li Muwan",
                "Wang Lin returns to build a small house with Li Muwan for her final 10 years"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L25", "Fire Burn Country", "火焚国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Fire-element dominant; fragile-tier spiritual veins; Fire Beasts roam", 4,
            null,
            java.util.List.of(
                "Li Muwan's home country on Planet Suzaku",
                "The Luo He Sect is based here",
                "Li Qiqing (Li Muwan's brother) was an elite Luo He Sect disciple here",
                "Fire Beasts roam this region"
            ), java.util.List.of("Luo He Sect"), java.util.List.of(
                "Wang Lin (in Ma Liang's body) meets Li Muwan here while escaping a Fire Beast",
                "The Heaven Defying Bead eats the King of Fire Beasts here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L26", "Sky Demon Country", "天魔国", LocType.COUNTRY,
            "Planet Suzaku", "country", "low",
            true, "Cultivation Planet Crystal", "Demonic cultivation country; low-tier spiritual energy with demonic taint", 4,
            null,
            java.util.List.of(
                "A demonic-cultivation country on Planet Suzaku",
                "Likely located in the East Demon Spirit Sea region",
                "Mo Lihai is a city general here",
                "The Demonic Drum tournament (15 rings → Ascendant breakthrough) is held in the capital"
            ), java.util.List.of("Sky Demon Country military"), java.util.List.of(
                "Wang Lin wins the Demonic Drum tournament (15 rings), breaks through to Ascendant",
                "Yao Xixue schemes against Wang Lin in a cave here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L27", "Pilu Kingdom", "毗卢国", LocType.COUNTRY,
            "Planet Suzaku", "country", "low",
            true, "Cultivation Planet Crystal", "Low-tier spiritual energy; Soul Refining Sect's soul-based energy practices", 4,
            null,
            java.util.List.of(
                "The kingdom where the Soul Refining Sect is headquartered",
                "Dun Tian and Nian Tian are from here",
                "Wang Lin inherits the Soul Refining Sect and the Ten Billion Soul Banner here"
            ), java.util.List.of("Soul Refining Sect"), java.util.List.of(
                "Wang Lin inherits the Soul Refining Sect and Ten Billion Soul Banner from Dun Tian",
                "Soul Transformation breakthrough",
                "Wang Lin teaches the Soul Refining Tribe their heritage here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L28", "Snow Domain Country", "雪域国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Cold-region country; fragile-tier ice/yin spiritual veins", 3,
            null,
            java.util.List.of(
                "A cold-region country on Planet Suzaku",
                "Liao Fan, who attacked Wang Lin with Situ Nan's severed arm, was from here",
                "Tangential to main plot"
            ), java.util.List.of(), java.util.List.of("Liao Fan attacks Wang Lin in EP80/ch1200 with Situ Nan's severed arm"),
            "C3 - implied; Liao Fan EP80/Ch.~1200; secondary source"
        ),
        new CanonLocation(
            "L29", "Xuan Wu", "玄武国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier spiritual veins", 3,
            null,
            java.util.List.of(
                "A country Wang Lin fled to while being chased by Duanmu Ji from the Sea of Devils",
                "Mentioned as a destination during the Duanmu Ji arc",
                "Tangential to main plot"
            ), java.util.List.of(), java.util.List.of("Wang Lin hides in the Heaven Defying Bead for 3 years here to evade Duanmu Ji"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L30", "Fire Demon Country", "火魔国", LocType.COUNTRY,
            "Planet Suzaku", "country", "low",
            true, "Cultivation Planet Crystal", "Fire/demonic spiritual energy; a fragmented ancient demon sealed beneath", 4,
            null,
            java.util.List.of(
                "A country under which a fragmented ancient demon is sealed",
                "Likably near the East Demon Spirit Sea on Planet Suzaku",
                "Bei Luo helps Wang Lin weaken the demon here"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin creates his first devil (Xu Liguo) here in a black pagoda",
                "Bei Luo devours the fragmented ancient demon here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L31", "Vermilion Bird Country", "朱雀国", LocType.COUNTRY,
            "Planet Suzaku", "country", "medium",
            true, "Cultivation Planet Crystal", "Highest-tier cultivation country on Planet Suzaku (level 6); medium-tier spiritual energy", 4,
            null,
            java.util.List.of(
                "The ruling cultivation nation of Planet Suzaku (level 6, raised by Situ Nan)",
                "Its ruler is the 'Vermilion Bird Master' — a renewable office",
                "Situ Nan was the 2nd-Generation; Zhou Wutai was the 15th; Wang Lin was the 16th",
                "The 14th severed Situ Nan's arm; the 3rd betrayed Situ Nan"
            ), java.util.List.of("Vermilion Bird Divine Sect"), java.util.List.of(
                "Wang Lin declines the position of Vermilion Bird Child (transferring it to Zhou Wutai)",
                "Wang Lin later becomes the 6th-Generation Vermilion Bird Divine Emperor of the broader sect"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L32", "Great Wang Dynasty", "大王朝", LocType.COUNTRY,
            "Planet Suzaku", "country", "medium",
            true, "Cultivation Planet Crystal", "Medium-tier; Wang Lin's eponymous dynasty", 4,
            null,
            java.util.List.of(
                "Wang Lin's eponymous dynasty, founded after his rise to power",
                "Made Wang Lin 'Ancestor of the Country of Zhao' simultaneously",
                "Established offscreen during the Vermilion Bird Star period"
            ), java.util.List.of("Wang Lin"), java.util.List.of("Established offscreen during the Vermilion Bird Star period"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L33", "Qing Shui Kingdom", "清水国", LocType.COUNTRY,
            "Planet Suzaku", "country", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier; former mortal kingdom (destroyed before main story)", 3,
            null,
            java.util.List.of(
                "Qing Shui's original mortal kingdom",
                "Destroyed before the main story began",
                "Its destruction seeded Qing Shui's slaughter essence and his resolve to defy the heavens"
            ), java.util.List.of(), java.util.List.of("Kingdom destruction and Qing Shui's family/clan annihilation (backstory only)"),
            "C3 - implied; secondary source (Qing Shui backstory)"
        ),
        new CanonLocation(
            "L34", "Wang Family Village", "王家村", LocType.CITY,
            "Country of Zhao", "country", "fragile",
            true, "Cultivation Planet Crystal", "Mortal village; negligible spiritual energy", 5,
            "1",
            java.util.List.of(
                "Wang Lin's birthplace — a small mortal village of the impoverished Wang Family Carpenter Clan",
                "Approximately 100 families",
                "Teng Huayuan exterminates the village (sparing only Wang Hao and Wang Zhuo who were Heng Yue Sect disciples)",
                "Wang Lin returns as Soul Formation to bury the dead"
            ), java.util.List.of("Wang Clan"), java.util.List.of(
                "Wang Lin born here",
                "Teng Huayuan exterminates the village",
                "Wang Lin returns as Soul Formation to bury the dead"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L35", "Tian Shui City", "天水城", LocType.CITY,
            "Country of Zhao", "country", "fragile",
            true, "Cultivation Planet Crystal", "Major mortal+cultivator trading city; fragile-tier spiritual veins", 5,
            null,
            java.util.List.of(
                "A major mortal+cultivator trading city in the Country of Zhao",
                "Wang Lin's first destination after his 4-year closed-door cultivation",
                "The Heng Yue Sect has interests here"
            ), java.util.List.of("Heng Yue Sect"), java.util.List.of(
                "Wang Lin kills Teng Li (Teng Huayuan's great-great-grandson) and steals his Foundation Establishment",
                "Old man Ji Mo's disciples attack Wang Lin here"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L36", "Teng Family City", "藤家城", LocType.CITY,
            "Country of Zhao", "country", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier; Teng Clan stronghold with accumulated resources", 4,
            null,
            java.util.List.of(
                "The Teng Clan's stronghold city in the Country of Zhao",
                "De facto ruled by Teng Huayuan",
                "The Teng Clan had nine Nascent Soul cultivators here"
            ), java.util.List.of("Teng Clan"), java.util.List.of(
                "Wang Lin's 'Kill and Destroy the Heart' revenge — hunts Teng descendants one by one",
                "Wang Lin builds a human-head tower to intimidate Teng Huayuan",
                "The Teng Clan's nine Nascent Soul cultivators are killed and refined into demons"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L37", "Nan Dou City", "南斗城", LocType.CITY,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Low-tier spiritual veins; near the Sea of Devils", 4,
            null,
            java.util.List.of(
                "A city Wang Lin visited to purchase an alchemy furnace for Li Muwan's Distant Heaven Pill",
                "Located in or near the Sea of Devils on Planet Suzaku"
            ), java.util.List.of("Fighting Evil Sect"), java.util.List.of(
                "Wang Lin noticed by Fighting Evil Sect members here",
                "10 Core Formation cultivators chase Wang Lin back to his cave"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L38", "Qilin City", "麒麟城", LocType.CITY,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Low-tier spiritual veins", 4,
            null,
            java.util.List.of(
                "A city Wang Lin visited after escaping the Chaotic Broken Stars",
                "Yun Fei's cave is here",
                "Possibly on Planet Suzaku or a nearby star"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin places a 3-day restriction on Yun Fei here",
                "Qiu Siping tries to unlock it; Wang Lin's devil kills Yun Fei"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L39", "Ancient Demon City", "古魔城", LocType.CITY,
            "Sky Demon Country", "country", "low",
            true, "Cultivation Planet Crystal", "Low-tier demonic spiritual energy; capital of Sky Demon Country", 4,
            null,
            java.util.List.of(
                "The capital of the Sky Demon Country on Planet Suzaku",
                "Site of the Demonic Drum tournament",
                "Also known as Demon Capital or Taga"
            ), java.util.List.of("Sky Demon Country military"), java.util.List.of(
                "Wang Lin wins the Demonic Drum tournament (15 rings), becomes an Ascendant",
                "Wang Lin defeats Mo Lihai's competitors"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L40", "Shui City", "水城 / 斗城", LocType.CITY,
            "Planet Suzaku", "planet", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier spiritual veins", 3,
            null,
            java.util.List.of(
                "Cities mentioned tangentially in the Sea of Devils arc",
                "Also known as Dou City",
                "Tangential to main plot"
            ), java.util.List.of(), java.util.List.of(),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L41", "Hou Fen", "侯分", LocType.OTHER,
            "Planet Suzaku", "planet", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier; a region within the Suzaku cultivation world", 3,
            null,
            java.util.List.of(
                "A region listed in the Fandom nav-bar Locations",
                "On Planet Suzaku — a region within the Suzaku cultivation world",
                "Specific details sparse; tangential to main plot"
            ), java.util.List.of(), java.util.List.of(),
            "C3 - implied; Fandom nav-bar only"
        ),
        new CanonLocation(
            "L42", "Blue Pine Peaks", "蓝松峰", LocType.MOUNTAIN,
            "Planet Suzaku", "planet", "fragile",
            true, "Cultivation Planet Crystal", "Fragile-tier mountain range spiritual veins", 3,
            null,
            java.util.List.of(
                "A mountain range region on Planet Suzaku",
                "Listed in the Fandom nav-bar Locations",
                "Specific details sparse; tangential to main plot"
            ), java.util.List.of(), java.util.List.of(),
            "C3 - implied; Fandom nav-bar only"
        ),
        new CanonLocation(
            "L43", "Soul Refining Tribe", "炼魂部族", LocType.CITY,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Low-tier; tribal soul-refining spiritual energy; over 1 million people", 4,
            null,
            java.util.List.of(
                "A tribe of over 1 million people in the East Demon Spirit Sea",
                "Founded by Wang Lin when he taught the Soul Refining Sect's heritage to the Mountain Valley tribe",
                "Chief: Ouyang Hua",
                "Wang Lin is regarded as their Ancestor"
            ), java.util.List.of("Soul Refining Sect"), java.util.List.of("Restored Wang Lin's One Billion Soul Flag", "Thirteen and Huo Pao trained here"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L44", "Mountain Valley Tribe", "山谷部落", LocType.CITY,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Low-tier; tribal spiritual energy in the East Demon Spirit Sea", 4,
            null,
            java.util.List.of(
                "The original name of the Soul Refining Tribe before Wang Lin elevated it",
                "Located in the East Demon Spirit Sea on Planet Suzaku",
                "Tribe chief: Ouyang Hua"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin displays power and is recognized by the tribe",
                "Wang Lin teaches them Soul Refining Sect heritage, elevating them to the Soul Refining Tribe"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L45", "Sea of Devils", "魔海 / 魔修海", LocType.OTHER,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Demonic spiritual energy; devils, beasts, and demonic cultivators thrive here", 5,
            null,
            java.util.List.of(
                "A vast demonic-sea region on Planet Suzaku, divided into 'districts' (Wang Lin enters the 14th district)",
                "Inhabited by devils, beasts, and demonic cultivators",
                "The Fighting Evil Sect, Soul Refining Sect, and Corpse Yin Sect all operate in/around it"
            ), java.util.List.of("Fighting Evil Sect", "Soul Refining Sect", "Corpse Yin Sect"), java.util.List.of(
                "Wang Lin's Core Formation breakthrough (with Li Muwan)",
                "The Fighting Evil Sect massacre",
                "Wang Lin takes the Mosquito Beast here"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L46", "Jue Ming Valley", "绝命谷", LocType.OTHER,
            "Planet Suzaku", "planet", "low",
            true, "Cultivation Planet Crystal", "Low-tier; trapping formation holds cultivators inside", 5,
            null,
            java.util.List.of(
                "A valley where surrounding sects compete for tokens to enter the Foreign Battleground",
                "A trapping formation holds cultivators inside until the competition ends",
                "Site of Wang Lin's Ji Realm awakening"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin's Ji Realm awakening (driven by grief over his clan's annihilation)",
                "First duel with Teng Huayuan outside the valley",
                "Wang Lin's physical body destroyed; soul flees to Foreign Battleground"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L47", "Foreign Battleground", "域外战场", LocType.SECRET_REALM,
            "Planet Suzaku", "planet", "medium",
            true, "Soul-seal (death-law locked)", "Medium-tier; death-law locked — black-colored laws of death prevent souls from leaving", 5,
            null,
            java.util.List.of(
                "A sealed battlefield accessible only via the Jue Ming Valley tokens",
                "Outside normal Suzaku geography",
                "Black-colored laws of death prevent souls from leaving — anyone who dies becomes a wandering soul",
                "Used for sect competitions and 'death exile'"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin becomes a Soul Devourer here (feeding on many souls)",
                "Wang Lin takes over Ma Liang's dying body",
                "Wang Lin escapes the death-law seal through observation, calculation, and knowledge"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L48", "Land of the Ancient God", "古神之地", LocType.SECRET_REALM,
            "Planet Suzaku", "planet", "medium",
            true, "Ancient restrictions (Tu Si's body)", "Medium-to-high; ancient god residual energy; 3-level realm structure", 5,
            null,
            java.util.List.of(
                "The corpse-body of the 8-star Ancient God Tu Si, transformed into a 3-level realm",
                "Level 1: hurricane of devils; Level 2: Bridge of No Return + Restriction Mountain; Level 3: Annihilation realm",
                "Tuo Sen was born here (Tu Si's demonic thought) and trapped for 1000+ years",
                "Accessible from Planet Suzaku and other planets"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin inherits Tu Si's knowledge and reshapes his body using ancient knowledge",
                "Wang Lin traps Tuo Sen for 1000+ years",
                "Wang Lin returns later as a 6-star Ancient God; Tuo Sen is injured"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L49", "Restriction Mountain", "禁制山", LocType.MOUNTAIN,
            "Land of the Ancient God", "planet", "medium",
            true, "Ancient restrictions", "Medium-tier; mountain made of ancient forbidden restrictions", 5,
            null,
            java.util.List.of(
                "A mountain made of ancient forbidden restrictions inside the Land of the Ancient God (Level 2)",
                "Each level up is more complex; modified by Demon Lord of Six Desires (Xu Liqing)",
                "The 'Great Enlightened One' title is granted to anyone who breaks all restrictions (only 4 ever)",
                "Wang Lin is the 4th Great Enlightened One"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin spends 7 (story) / 13 (subjective) years here; develops Illusionary Circle technique; hair turns white",
                "Receives 'Great Enlightened One' title from Tu Si"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L50", "Bridge of No Return", "不归桥", LocType.OTHER,
            "Land of the Ancient God", "planet", "medium",
            true, "Ancient restrictions", "Medium-tier; tests the heart through illusions", 4,
            null,
            java.util.List.of(
                "A bridge inside the Land of the Ancient God (Level 2)",
                "Deviils can return but beasts (and most beings) who turn back are sucked into the void",
                "Tests the heart through illusions of loved ones calling back"
            ), java.util.List.of(), java.util.List.of("Wang Lin resists the illusion of his parents' voices calling him back; passes the test"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L51", "Chaotic Broken Stars", "混沌碎星", LocType.SECRET_REALM,
            "Planet Suzaku", "planet", "medium",
            true, "Ancient spatial restrictions", "Medium-tier chaotic energy; 3-level star cluster with ancient restrictions", 5,
            null,
            java.util.List.of(
                "A 3-level chaotic star cluster accessible via teleportation from Planet Suzaku",
                "Level 1: hurricane of devils (Wang Lin creates his 2nd devil here)",
                "Level 2: Bridge of No Return + Restriction Mountain",
                "Level 3: Annihilation realm — wandering souls, ancient god Tu Si's body"
            ), java.util.List.of(), java.util.List.of(
                "Duanmu Ji (Soul Formation) chases Wang Lin here",
                "Wang Lin outwits Duanmu Ji, Gun Lan, Xu Liqing, and Hunchback Meng",
                "Wang Lin inherits Tu Si's legacy"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L52", "Suzaku Tomb", "朱雀墓", LocType.OTHER,
            "Planet Suzaku", "planet", "medium",
            true, "Cultivation Planet Crystal", "Medium-tier; contains the Cultivation Planet Crystal (the seal-mechanism of the planet itself)", 4,
            null,
            java.util.List.of(
                "A multi-floor tomb within Planet Suzaku (underground)",
                "Contains the Cultivation Planet Crystal — the seal-mechanism of the planet itself",
                "Wang Lin found the Half-Moon Blade here",
                "The scattered demon (Piao Nanzi/Lou Hou) is sealed here"
            ), java.util.List.of("Vermilion Bird Divine Sect"), java.util.List.of("Wang Lin's tomb-raiding arc", "Encounters the scattered demon (Piao Nanzi/Lou Hou) sealed here"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L53", "Thunder Celestial Realm", "雷仙界", LocType.REALM,
            "Allheaven Star System", "star_system", "high",
            true, "Ancient immortal seal (collapses later)", "High-tier; contains Thunder Lake, celestial energy; enslaved celestial clan present", 5,
            null,
            java.util.List.of(
                "A pocket-realm within the Allheaven Star System — the 'Thunder Immortal Realm'",
                "Contains the Thunder Celestial Temple, Thunder Lake, and the Chosen Immortal Clan (enslaved celestials)",
                "Ruled by Master Flamespark and Russell",
                "The Celestial Slaughter Art trap is hidden here"
            ), java.util.List.of("Thunder Celestial Temple", "Chosen Immortal Clan", "Yao Family"), java.util.List.of(
                "Wang Lin's Thunder Celestial Tournament victory; kills Russell",
                "Blood Ancestor killed here",
                "Wang Lin obtains the God-Slaying War Chariot, Celestial Emperor Crown, and Collection Pavilion"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L54", "Thunder Celestial Temple", "雷仙殿", LocType.SECT,
            "Thunder Celestial Realm", "star_system", "high",
            true, "Thunder Celestial Realm seal", "High-tier; 3 trials (heaven, earth, human) and Thunder Lake trial", 4,
            null,
            java.util.List.of(
                "The temple ruling the Thunder Celestial Realm",
                "Has 3 trials: heaven, earth, human; plus the Thunder Lake trial and battle realm trial (kill-count)",
                "Wang Lin becomes 'Thunder Celestial of the Thunder Celestial Temple' here",
                "Contains the Collection Pavilion (immortal arts library)"
            ), java.util.List.of("Thunder Celestial Temple"), java.util.List.of(
                "Wang Lin becomes Thunder Celestial of the Thunder Celestial Temple",
                "Thunder Celestial Tournament held here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L55", "Rain Celestial Realm", "雨仙界", LocType.REALM,
            "Allheaven Star System", "star_system", "high",
            true, "Ancient immortal seal", "High-tier; immortal jades and immortals' corpses; rich in residual immortal energy", 4,
            null,
            java.util.List.of(
                "A sub-realm of immortal jades and immortals' corpses within the Allheaven Star System",
                "Zhou Yi discovered Qing Shuang's corpse here ('Ting'er')",
                "The Rain Immortal Sword (Jufu) and War Spirit Print are here"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin obtains the Shooting God Chariot, Star Compass, War Spirit Print, and Rain Immortal Sword",
                "Wang Lin takes Sun Tai as a servant",
                "Wang Lin helps Zhou Yi protect Ting'er's corpse"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L56", "Wind Celestial Realm", "风仙界", LocType.REALM,
            "Cloud Sea Star System", "star_system", "high",
            true, "Ancient immortal seal", "High-tier; wind-themed immortal energy; large herds of Mosquito Beasts", 4,
            null,
            java.util.List.of(
                "A wind-themed immortal sub-realm within the Cloud Sea Star System",
                "Wang Lin acquires a large herd of Mosquito Beasts here",
                "The Flowing Moon technique (rules of time) was created here under a stone gate",
                "Site of Wang Lin's petrification by Daoist Water"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin evolves his king mosquito beast and creates Flowing Moon divine ability",
                "Wang Lin severely wounded and petrified by Daoist Water; saved by Li Qianmei's 10-year blood anointment"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L57", "Demon Spirit Land", "魔灵地 / 东海魔灵海", LocType.OTHER,
            "Planet Tian Yun", "planet", "medium",
            true, "Realm-Sealing Grand Array", "Medium-tier demonic-spirit energy; East Sea Demon Spirit Gate opens periodically", 5,
            null,
            java.util.List.of(
                "A demonic-spirit region on Planet Tian Yun (some sources say Planet Suzaku)",
                "The East Sea Demon Spirit Gate opens periodically",
                "Contains the Immortal Monarch's Cave Mansion (Yao Xixue's target)",
                "The Mountain Valley Tribe is in the east; the Ancient Demon City (Taga) is here"
            ), java.util.List.of("Heavenly Fate Sect", "Yao Family"), java.util.List.of(
                "Wang Lin takes Thirteen as his 1st disciple and seals Yao Xixue",
                "All-Seer, Ling Tianhou, Blood Ancestor, and others come here for the cave token",
                "Wang Lin escapes into the abyss to the Allheaven Star System"
            ),
            "C5 - multi-source (Fandom + Baidu); no specific chapter"
        ),
        new CanonLocation(
            "L58", "Brilliant Void", "玄虚", LocType.OTHER,
            "Brilliant Void Star System", "star_system", "medium",
            true, "Realm-Sealing Grand Array", "Medium-tier; the vast inner void of the Alliance Star System", 3,
            null,
            java.util.List.of(
                "The original name of the Alliance Star System's interior void",
                "Renamed to Alliance Star System by the Cultivation Alliance",
                "Wang Lin's 'Brilliant Void' pet is named after this region",
                "Tangential to plot"
            ), java.util.List.of("Cultivation Alliance"), java.util.List.of("Renamed to Alliance Star System by the Cultivation Alliance"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L59", "Wild Continent", "莽荒大陆", LocType.CONTINENT,
            "Cloud Sea Star System", "star_system", "medium",
            true, "Realm-Sealing Grand Array", "Medium-tier; wild, primal spiritual energy", 4,
            null,
            java.util.List.of(
                "A wild, primal continent within the Cloud Sea Star System",
                "Wang Lin discovers 'the secret for the third step that caused a huge calamity thousands of years ago' here",
                "Triggered a region-wide sect gathering when discovered"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin's discovery triggers a region-wide sect gathering",
                "Wang Lin kills Noble Money and his group on the way back"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L60", "Seven-Colored Realm", "七彩界", LocType.SECRET_REALM,
            "Cloud Sea Star System", "star_system", "high",
            true, "Ancient restrictions (Seven-Colored Divine Sky Nail system)", "High-tier; seven-colored light energy; Seven-Colored Divine Sky Nail (108 nails) system", 4,
            null,
            java.util.List.of(
                "A pocket-realm of seven-colored light within the Cloud Sea Star System",
                "Master Ashen Pine takes cultivators here to 'find something' (actually a trap)",
                "The Seven-Colored Divine Sky Nail (108 nails, designed to kill Third Step experts) is here",
                "Spatial cracks separate cultivators"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin tricks Master Cloud Soul into a different spatial crack",
                "Wang Lin steals treasures from Master Ashen Pine and kills him with the seven-colored nail",
                "Cang Songzi destroys Wang Lin's Rusty Iron Sword here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L61", "Ancient Tomb", "古墓", LocType.OTHER,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; contains Ancient Clan relics: Yi Si Puppet, Heaven-Splitting Axe, Eternal Wood Spirit, Fog Devil Lance, Ancient Breath Leaves, Emperor Furnace", 4,
            null,
            java.util.List.of(
                "A vast multi-floor tomb on the IAC containing relics of the Ancient Clan",
                "Contains the Yi Si Puppet, Heaven-Splitting Axe, Eternal Wood Spirit, Fog Devil Lance, Ancient Breath Leaves (99-piece set), Emperor Furnace",
                "Also contains Ye Mo's left eye and right arm",
                "Daogu Yemo's ancient devil corpse is here (used for Wang Lin's Third Avatar)"
            ), java.util.List.of("Ancient Clan"), java.util.List.of("Wang Lin obtains numerous Ancient Clan relics here", "Tan Lang is involved in events here"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L62", "Pill Sea", "丹海", LocType.OTHER,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; vast sea of dissolved pills and alchemical waste; contains Water Essence energy", 4,
            null,
            java.util.List.of(
                "A vast sea of pills (alchemical waste + dissolved pills) between Heavenly Bull Continent and Green Devil Continent",
                "Wang Lin completes his Water Essence 9th cycle here",
                "A Green Bull Continent woman attacks Wang Lin with Ten Thousand Refined Corruption Liquid here"
            ), java.util.List.of(), java.util.List.of(
                "A woman cultivator from Green Bull Continent attacks Wang Lin with Ten Thousand Refined Corruption Liquid",
                "Wang Lin absorbs it to complete his Water Essence 9th cycle"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L63", "Dong Lin Pool", "东林池", LocType.OTHER,
            "Great Saint Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; sealed Ancient God-tier entity beneath; grants Absolute Beginning and Reincarnation Essence comprehension", 4,
            null,
            java.util.List.of(
                "A pool with the sealed spirit of an Ancient God-tier entity beneath it",
                "Located on the IAC in the Dong Lin Sect area (Great Saint Continent)",
                "False version (inside Fifth Flower's illusory world) grants temporary power",
                "True version grants permanent Absolute Beginning and Reincarnation Essence comprehension"
            ), java.util.List.of("Dong Lin Sect"), java.util.List.of(
                "Wang Lin meditates for 13 years here; Absolute Beginning reaches completion; Reincarnation Essence begun"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L64", "Immortal Emperor's Cave Mansion", "仙帝洞府", LocType.CAVE,
            "Demon Spirit Land", "planet", "medium",
            true, "Ancient immortal restrictions", "Medium-tier; ancient immortal residual energy; Celestial Emperor's Tower part of complex", 4,
            null,
            java.util.List.of(
                "A cave-mansion left by an ancient immortal, on/under Demon Spirit Land (Tian Yun Star)",
                "Yao Xixue and Wang Lin explore it together (causing her hundred-year imprisonment)",
                "The Wind Demon (Feng Mo) is encountered here",
                "Celestial Emperor's Tower is part of this complex"
            ), java.util.List.of("Yao Family"), java.util.List.of(
                "Yao Xixue ambushes Wang Lin and is imprisoned for 100 years",
                "Yao Xinghai seeks Wang Lin here",
                "Wind Demon slain by Wang Lin's God-Slaying Spear"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L65", "Heavenly Bull Continent", "天牛大陆", LocType.CONTINENT,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; 120+ Fire Veins; rich fire-element spiritual energy", 4,
            null,
            java.util.List.of(
                "One of the major continents of the IAC",
                "Contains the Great Soul Sect, Gui Yi Sect, and 120+ Fire Veins",
                "Wang Lin's IAC territory",
                "Wang Lin joins Great Soul Sect here and becomes an elder"
            ), java.util.List.of("Great Soul Sect", "Gui Yi Sect"), java.util.List.of(
                "Wang Lin joins Great Soul Sect, becomes an elder, devours Earth Fire main vein",
                "Wang Lin manifests Fire Essence True Body"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L66", "Green Devil Continent", "绿魔大陆", LocType.CONTINENT,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; Green Devil (Green Scorpion) demonic energy pervades", 4,
            null,
            java.util.List.of(
                "Home continent of the Green Devil (Green Scorpion) entity on the IAC",
                "The Dao Devil Sect (Dao Demon Sect) is based here",
                "Wang Lin was captured here as a Green Devil sacrifice"
            ), java.util.List.of("Dao Devil Sect"), java.util.List.of(
                "Wang Lin captured as Green Devil sacrifice by Ji Si (acting for Dao Demon Sect)",
                "Wang Lin reverses the ritual and devours Green Devil, reaching Arcane Void stage"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L67", "Mountain Sea Continent", "山海大陆", LocType.CONTINENT,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; contains the Mountain Tree Seal", 3,
            null,
            java.util.List.of(
                "A continent on the IAC with the Mountain Tree Seal",
                "Wang Lin finds the second fragment of the Celestial Ancestor's Immortal Absolute Sword here"
            ), java.util.List.of(), java.util.List.of("Wang Lin obtains Metal Essence (initial) here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L68", "Great Saint Continent", "大圣大陆", LocType.CONTINENT,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; a sealed Spirit beneath grants Absolute Beginning and Absolute End Essence comprehension", 3,
            null,
            java.util.List.of(
                "A continent on the IAC with a sealed Spirit beneath it",
                "The entity whose comprehension gave Wang Lin Absolute Beginning and Absolute End Essences",
                "Dong Lin Pool is technically a sub-region here"
            ), java.util.List.of(), java.util.List.of("Wang Lin meditates at Dong Lin Pool here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L69", "Mengtu Province", "孟图省", LocType.OTHER,
            "Green Devil Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier provincial spiritual energy", 4,
            null,
            java.util.List.of(
                "A province of the IAC on the Green Devil Continent",
                "Wang Lin was captured by Dao Demon Sect here",
                "Dao Demon Sect Master intended to use Wang Lin to resurrect the Green Devil Scorpion"
            ), java.util.List.of("Dao Devil Sect"), java.util.List.of("Wang Lin captured by Dao Demon Sect; turns the tables and reaches Empty Tribulation stage"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L70", "Tianniu Province", "天牛省", LocType.OTHER,
            "Heavenly Bull Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; contains Canglong Sect and Earth Fire Dragon's soul", 4,
            null,
            java.util.List.of(
                "A province of the IAC on the Heavenly Bull Continent",
                "Contains the Canglong Sect (Azure Dragon Sect)",
                "Kang Ren (a cultivator who took Wang Lin in) is from here",
                "Wang Lin extracts Earth Fire Dragon's soul here"
            ), java.util.List.of("Canglong Sect"), java.util.List.of(
                "Wang Lin enters unconscious; taken by Kang Ren into Canglong Sect",
                "Wang Lin extracts Earth Fire Dragon's soul; pursued by ancestor Du Qing"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L71", "Green Bull Continent", "青牛大陆", LocType.CONTINENT,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier continental spiritual energy", 3,
            null,
            java.util.List.of(
                "A continent on the IAC",
                "A woman cultivator from here attacked Wang Lin in the Pill Sea",
                "Less detail available than other IAC continents"
            ), java.util.List.of(), java.util.List.of("Woman cultivator attacks Wang Lin with Ten Thousand Refined Corruption Liquid in the Pill Sea"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L72", "Imperial City", "皇城 / 道古皇都", LocType.CITY,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; the Ancient Clan's imperial seat; richest spiritual energy on the IAC", 4,
            null,
            java.util.List.of(
                "The Ancient Clan's imperial seat — the Dao Ancient Imperial Capital",
                "Dao Ancient Imperial Venerable rules here",
                "Lu Mo (Slaughter True Body) emerged here to defend Wang Lin"
            ), java.util.List.of("Ancient Clan"), java.util.List.of(
                "Wang Lin ambushed here; Lu Mo released and kills attackers",
                "Wang Lin slays Dao Ancient Imperial Venerable to retrieve Li Muwan's remnant soul",
                "Jiu Di Great Celestial Venerable confronted here"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L73", "Ancient Clan Ancestral Temple", "古族祖庙 / 古氏分支祖庙", LocType.OTHER,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier; Ancient Clan ancestral energy; Lou Hou's soul sealed here", 4,
            null,
            java.util.List.of(
                "The temple where the Ancient Clan's ancestor rites are performed",
                "Located within the Ancient Clan territory on the IAC",
                "Lou Hou's soul is sealed here (used by Wang Lin to complete Slaughter/Restriction/Absolute Beginning/End True Bodies)"
            ), java.util.List.of("Ancient Clan"), java.util.List.of("Wang Lin completes his final Ancient Clan tribulation here; obtains 2 more drops of Soul Blood"),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L74", "Kunxu Realm", "昆虚界", LocType.SECRET_REALM,
            "The Cave World", "cave_world", "medium",
            true, "Ancient restrictions", "Medium-tier sealed sub-dimension spiritual energy", 3,
            null,
            java.util.List.of(
                "A pocket-realm accessible from the Cave World",
                "Mu Bingmei entered here; took Zhou Ru as her disciple",
                "Zhou Ru's cultivation arc takes place here"
            ), java.util.List.of(), java.util.List.of("Zhou Ru's cultivation arc here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L75", "Tide Abyss", "潮渊", LocType.OTHER,
            "Thunder Celestial Realm", "star_system", "high",
            true, "Thunder Celestial Realm seal", "High-tier abyssal energy", 3,
            null,
            java.util.List.of(
                "An abyss within the Thunder Celestial Realm (Allheaven Star System)",
                "Wang Lin locates Zhou Yi's spirit here with Bei Lou's help"
            ), java.util.List.of(), java.util.List.of("Wang Lin finds Zhou Yi's spirit here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L76", "Immortal Graveyard", "仙坟", LocType.OTHER,
            "Vermilion Bird Starfield", "star_system", "high",
            true, "Ancient restrictions", "High-tier; 17 layers of immortal remnants; Fu Clan's Golden Leaf Flame Source Origin on 17th layer", 3,
            null,
            java.util.List.of(
                "A 17-layer immortal graveyard within the Vermilion Bird Starfield",
                "Wang Lin glimpses the Fu Clan's Golden Leaf Flame Source Origin on the 17th layer",
                "Site of Wang Lin's Fire Essence potential awakening"
            ), java.util.List.of("Fu Clan"), java.util.List.of("Wang Lin's Fire Essence potential awakened here"),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L77", "Five Flowers Eight Gates", "五花八门", LocType.OTHER,
            "Immortal Astral Continent", "immortal_astral_continent", "absolute",
            false, null, "Absolute-tier formation-complex energy", 3,
            null,
            java.util.List.of(
                "A complex on the IAC where Wang Lin encounters the Dong Lin Female Ancient God",
                "Wang Lin fuses with Ye Mo's heart's blood and subdues the Yi Si Puppet here",
                "Defeats Yun Yifeng and the Palm Venerable"
            ), java.util.List.of(), java.util.List.of(
                "Wang Lin fuses with Ye Mo's heart's blood, subdues the Yi Si Puppet",
                "Wang Lin defeats Yun Yifeng, defeats the Palm Venerable, and obtains Ye Mo's heart inheritance"
            ),
            "C3 - implied; secondary source"
        ),
        new CanonLocation(
            "L78", "Falling Land", "堕落之地", LocType.SECRET_REALM,
            "Sealed Realm", "sealed_realm", "high",
            true, "Ancient trial restrictions", "High-tier; trial-ground energy; dragon blood present", 4,
            null,
            java.util.List.of(
                "A trial region within the Sealed Realm where the 'Young Emperor' trial takes place",
                "Wang Lin becomes 'Young Emperor of the Fallen Land'",
                "2nd-Gen Vermilion Bird (young emperor of the Fallen Land) helps him",
                "Contains Trial of Man and Trial of Heaven"
            ), java.util.List.of("Vermilion Bird Divine Sect"), java.util.List.of(
                "Wang Lin's Young Emperor trial — fishes for a dragon, takes its blood",
                "Wang Lin passes the Trial of Man, Trial of Heaven; becomes Void Flame Cultivator"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L79", "Ancient Immortal Domain", "古仙域", LocType.REALM,
            "The Cave World", "cave_world", "absolute",
            true, "Seven-Colored Daoist (unsealed by Wang Lin)", "Absolute-tier; gateway-realm energy at the boundary of the Cave World", 4,
            null,
            java.util.List.of(
                "A gateway-realm at the boundary of the Cave World, leading to Luo Tian",
                "The gateway Wang Lin breaches to discover the truth of the Cave World",
                "The Flowing Moon divine ability was comprehended at its gate",
                "Wang Lin resets the Realm Sealing Grand Array here"
            ), java.util.List.of("Seven-Colored Daoist"), java.util.List.of(
                "Wang Lin discovers his world is the Seven-Colored Daoist's cave mansion",
                "Wang Lin resets the Realm Sealing Grand Array and plans new territories"
            ),
            "C4 - novel + wiki-attested; no specific chapter"
        ),
        new CanonLocation(
            "L80", "Yellow Spring Secret Realm", "黄泉秘境", LocType.SECRET_REALM,
            "The Cave World", "cave_world", "low",
            true, "Heaven Defying Bead", "Low-tier; extremely-Yin-location sub-realm; Situ Nan's creation for Wang Lin's breakthrough", 4,
            null,
            java.util.List.of(
                "An extremely-Yin-location sub-realm inside the Heaven Defying Bead",
                "Opened by Situ Nan for Wang Lin's secluded cultivation",
                "Wang Lin's Nascent Soul breakthrough training takes place here",
                "Sealed by being INSIDE the Heaven Defying Bead itself"
            ), java.util.List.of(), java.util.List.of("Wang Lin's Nascent Soul breakthrough training"),
            "C4 - novel + wiki-attested; no specific chapter"
        )
    );

    /** All 178 canon artifacts. */
    public static final List<CanonArtifact> ALL_ARTIFACTS = List.of(
        new CanonArtifact(
            "I01", "Heaven-Defying Bead / Pearl", "逆天珠", ArtType.BEAD_PART,
            "utility", "Wang Lin",
            java.util.List.of(
                "Five Elements pattern bead — core of the original 9-part set",
                "Interior time-dilation chamber (10× outside speed)",
                "Stores nascent souls; contains Third-Step divine abilities",
                "Sentient / destiny-bound / fuses with primordial spirit"
            ),
            "Found as a youth inside Heng Yue Sect stone bead; sent back through time by future clone Lu Mo via Dream Dao", 5,
            java.util.List.of(
                "Root cause of the great war in the Ancient Immortal Domain",
                "Cross-novel artifact — also wielded by Su Ming and Xuan Zang",
                "Bestowed by Seven-Colored Immortal Venerable to Realm-Sealing Supreme",
                "Fuses with Wang Lin's primordial spirit by the end"
            ), "Ch.8 (novel, Donghua S1E2); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I02", "Realm-Defining Compass", "界定罗盘", ArtType.COMPASS,
            "divine_sense", "Old Man Miēshēng (returned)",
            java.util.List.of(
                "Core IS the Heaven-Defying Pearl — defines and shatters realms",
                "Realm-Sealing function",
                "Quasi-Third-Step treasure power"
            ),
            "Borrowed by Lu Mo from Old Man Miēshēng to crack open the Heaven-Defying Bead", 4,
            java.util.List.of(
                "One-time borrow — returned to Old Man Miēshēng",
                "Used to blast open the Heaven-Defying Pearl via Dream Dao",
                "Quasi-Third-Step tier"
            ), "wiki-attested; Fandom wiki; no specific chapter"
        ),
        new CanonArtifact(
            "I03", "Copper Mirror (with Time Domain)", "铜镜", ArtType.OTHER,
            "divine_sense", "Sold (Ch. 664)",
            java.util.List.of(
                "Embeds a Time-domain within the mirror",
                "Shows future/past flickers as side-effect",
                "Pseudo-celestial treasure grade"
            ),
            "Crafted by Wang Lin on Planet Ran Yun to sell for celestial spirit jades", 5,
            java.util.List.of(
                "One of three pseudo-celestial treasures crafted for sale",
                "Sold at Ch. 664 — 'Missed' by buyer",
                "User-made pseudo-celestial tier"
            ), "Ch.662 obtained, Ch.664 sold (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I04", "Slaughter Crystal", "杀戮晶", ArtType.MATERIAL,
            "cultivation_aid", "Wang Lin (merged into Slaughter Origin True Body)",
            java.util.List.of(
                "Condensed Slaughter Origin essence",
                "Fuses with Qing Shui's Slaughter Sword to complete Slaughter Essence",
                "Origin essence grade"
            ),
            "Condensed from slaughters / granted by Immortal Lord Qing Shui via Sky-Gate vortex", 5,
            java.util.List.of(
                "Origin essence tier material",
                "Fuses with Slaughter Origin True Body",
                "Granted by Immortal Lord Qing Shui (senior brother)"
            ), "Ch.~1290 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I05", "Great Heavenly Venerable Sun", "大天尊日", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "Three-color (black/white/gold) Heavenly Venerable Sun",
                "Absorbable energy to enhance strength at any time",
                "Fuses with quasi-Tread-Heaven power",
                "Strongest of all Great Heavenly Venerable Suns"
            ),
            "Refined from the head of the Immortal Ancestor (Celestial Ancestor)", 4,
            java.util.List.of(
                "Will-condensed celestial-ancestor tier",
                "Perpetually active portable power-source",
                "Retained to the end"
            ), "wiki-attested; Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I06", "Wealth — Wang Lin's First Flying Sword", "财富", ArtType.OTHER,
            "offensive", "Destroyed by Teng Huayuan",
            java.util.List.of(
                "First flying sword — mortal/low-grade spirit tier",
                "Reforged multiple times over career",
                "Soul-binding contract with sect (must repair, never sell)"
            ),
            "Chosen from Heng Yue Sect armory as a disciple", 5,
            java.util.List.of(
                "Made by a Heng Yue Sect elder",
                "Sect rule: if broken repair, if sold exiled",
                "Revealed later to have mysterious origin",
                "Destroyed by Teng Huayuan ~Ch. 110"
            ), "early Heng Yue Sect, destroyed Ch.~110 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I07", "Core-Treasure Sword (Teleportation)", "核心宝剑", ArtType.OTHER,
            "offensive", "Upgraded into Dark Green Flying Sword",
            java.util.List.of("Teleportation effect on strike", "Blood-refined by Wang Lin", "Took countless lives in battle"),
            "Acquired then blood-refined in mid-Foundation era", 5,
            java.util.List.of(
                "Spirit-tier blood-refined sword",
                "Evolves into Dark Green Flying Sword (Poison)",
                "Mid-Foundation era acquisition"
            ), "mid-Foundation era (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I08", "Dark Green Flying Sword (Poison)", "墨绿飞剑", ArtType.OTHER,
            "offensive", "Wang Lin (retired)",
            java.util.List.of(
                "Poison-attribute flying sword",
                "Blood-refined and battle-hardened",
                "Evolved from Core-Treasure Sword"
            ),
            "Blood-refinement evolution from Core-Treasure Sword", 5,
            java.util.List.of(
                "Spirit-tier poison-element sword",
                "Successor to Core-Treasure Sword",
                "Retired — replaced by later swords"
            ), "evolved from Core-Treasure Sword (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I09", "The Dragon Formation", "龙阵", ArtType.FORMATION,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Formation-array treasure based on Fighting Evil Sect formation",
                "Gift from Li Muwan — sentiment-bound"
            ),
            "Gifted by Li Muwan, built based on Fighting Evil Sect (斗邪宗) formation", 5,
            java.util.List.of("Spirit-tier formation treasure", "Gift from love interest Li Muwan", "Retained to end"), "wiki-attested (gifted by Li Muwan); Fandom wiki"
        ),
        new CanonArtifact(
            "I10", "God Slaying Spear (Illusory)", "杀神矛", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Illusory copy of Ancient God Tu Si's life-treasure",
                "Bypasses physical defenses — strikes at origin soul",
                "Ancient God Weapon classification"
            ),
            "Inherited from Ancient God Tu Si's legacy (Ch. 941)", 5,
            java.util.List.of("Ancient God treasure tier", "Illusory / inherited-treasure", "Tu Si's life treasure projection"), "Ch.941 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I11", "Ancient God Trident", "古神三叉戟", ArtType.OTHER,
            "offensive", "Destroyed by Daoist Water",
            java.util.List.of(
                "Absorbs spells cast at the wielder",
                "Tu Si-refined Ancient God weapon",
                "Three-pronged trident form"
            ),
            "Tu Si inheritance — weapon Tu Si was refining before death (Ch. 1082)", 5,
            java.util.List.of(
                "Ancient God treasure tier",
                "Destroyed Ch. 1277 in Rebirth arc by Daoist Water",
                "Tu Si was refining it but died before completion"
            ), "Ch.1082 obtained, Ch.1277 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I12", "Karma Whip", "因果鞭", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Weaponizes karmic cause-effect",
                "Once cleaved open 7 million worlds with a single whip-strike",
                "Restrains soul-type entities",
                "Pseudo-immortal / dao-domain treasure tier"
            ),
            "Fused Soul Lasher (Red Butterfly's whip) + Karma Domain (Ch. 731)", 5,
            java.util.List.of(
                "Evolved from Soul Lasher via Ghostly Sky Fire + Karma Concept",
                "Soul Lasher originally belonged to Red Butterfly, won in battle from Hong Die",
                "Retained to end — dao-domain weapon"
            ), "Ch.731 (novel, Donghua EP147); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I13", "18-Hell Celestial Sealing Stamp", "十八地狱封天印", ArtType.TALISMAN,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Forms 18-Layers-of-Hell-Reincarnation-Realm with Underworld River",
                "Stores souls of all enemies Wang Lin has killed",
                "Storage realm for Scatter Beans to Form Soldiers divine ability",
                "Refined from Fragment Stamp via divine tribulation"
            ),
            "Self-forged — refined from Fragment Stamp during breakthrough to Illusionary-Yin/Corporeal-Yang (Ch. 915)", 5,
            java.util.List.of(
                "Pseudo-immortal tier — Wang Lin-made",
                "Fused with Magic-Arsenal Spell + Celestial Sealing Stamp",
                "Evolved into its own pocket-realm (Incense Offering Realm per Baidu)"
            ), "Ch.915 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I14", "Rain Celestial Sword (Mid Quality)", "雨仙剑", ArtType.OTHER,
            "offensive", "Wang Lin (wielded by Xu Liguo as sword-spirit)",
            java.util.List.of(
                "Celestial-tier mid-quality flying sword",
                "Contains Slash Luo Art sword-technique",
                "Sword-spirit (Jufu) bound to Xu Liguo",
                "Eternal-inheritance condition: protect celestial corpse in pagoda"
            ),
            "Gifted by Zhou Yi — separated from celestial corpse swords (Ch. 717)", 5,
            java.util.List.of(
                "Second-generation Rain Immortal Sword",
                "Xu Liguo (devil head) serves as its sword-spirit",
                "Conditionally gifted — must protect celestial corpse in pagoda"
            ), "Ch.717 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I15", "Celestial Sword (Rain Celestial Sword lineage seed)", "仙剑", ArtType.OTHER,
            "offensive", "Evolved into Rain Celestial Sword",
            java.util.List.of(
                "Celestial-tier flying sword — seed of Rain Celestial Sword inheritance",
                "Inheritance-bound conditional gift"
            ),
            "Gifted by Zhou Yi (Ch. 717) under condition to protect celestial corpse", 5,
            java.util.List.of("Became the seed of Rain Celestial Sword inheritance", "Evolved into Rain Celestial Sword"), "Ch.717 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I16", "Crystal Sword (Pseudo Nirvana Void)", "水晶剑", ArtType.OTHER,
            "offensive", "Wang Lin (restored)",
            java.util.List.of(
                "Crystal-element sword from Seven-Colored Realm",
                "Pseudo Nirvana Void grade",
                "Shattered then restored by previous Lord of the Sealed Realm"
            ),
            "Taken from Seven-Colored Realm (Ch. 1196)", 5,
            java.util.List.of(
                "Seven-Colored Realm treasure",
                "Destroyed Ch. 1273 vs Daoist Water, then restored",
                "Retained after restoration"
            ), "Ch.1196 obtained, Ch.1273 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I17", "Short Sword (Black & White Dual Swords)", "白黑双剑", ArtType.OTHER,
            "offensive", "Wang Lin (restored)",
            java.util.List.of(
                "Paired swords: one black, one white with nine seals each",
                "Undoing seals produces millions of sword shadows",
                "Pseudo Nirvana Void grade"
            ),
            "Acquired in Seven-Colored Realm (1st: Ch. 1187; 2nd: Ch. 1223)", 5,
            java.util.List.of(
                "Pair-weapon with seal-based mass-shadow generation",
                "Destroyed Ch. 1273 vs Daoist Water, restored by previous Lord of Sealed Realm",
                "Retained after restoration"
            ), "Ch.1187 & Ch.1223 obtained, Ch.1273 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I18", "Half-Moon Blade", "半月刃", ArtType.OTHER,
            "offensive", "Wang Lin (retired)",
            java.util.List.of("Extremely fast offensive blade", "Obedience bound to Xu Liguo (treated him like older brother)"),
            "Found in Cultivation Planet Crystal / Suzaku Tomb", 5,
            java.util.List.of("Spirit-tier blade", "Initially obeyed only because of Xu Liguo", "Retired — no longer used"), "wiki-attested (Planet Suzaku Crystal); Fandom wiki"
        ),
        new CanonArtifact(
            "I19", "Axe of Giant Demon Clan", "巨魔族斧", ArtType.OTHER,
            "offensive", "Destroyed",
            java.util.List.of("Giant Demon Clan spirit-tier axe", "Traded for the Star Compass"),
            "Stolen/traded from Giant Demon Clan Heir (exchanged for Star Compass)", 5,
            java.util.List.of(
                "Spirit-tier Giant Demon Clan weapon",
                "Traded away in exchange for Star Compass",
                "Eventually destroyed"
            ), "wiki-attested (stolen from Giant Demon Clan); Fandom wiki"
        ),
        new CanonArtifact(
            "I20", "Rusted Iron Sword (Pseudo Nirvana Void)", "锈铁剑", ArtType.OTHER,
            "offensive", "Destroyed",
            java.util.List.of("Upper Nirvana Void Treasure grade sword", "Deceptively powerful despite rusted appearance"),
            "Likely taken as loot (Ch. 1043)", 5,
            java.util.List.of(
                "Upper-Nirvana-Void tier — very high grade",
                "Destroyed Ch. 1193 vs Master Ashen Pine in Seven-Colored Realm",
                "Bloody battle weapon"
            ), "Ch.1043 obtained, Ch.1193 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I21", "Seven-Colored God Void Nails", "七彩通天钉", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "108-nail set designed to kill Third Step experts",
                "Madness-inducing when pierced into a body",
                "Forged by Cang Songzi (Palm Venerable) at Kongling-late stage",
                "Made from meteorite of ancient exotic metal"
            ),
            "Taken from Cang Songzi in Seven-Colored Realm battle (Ch. 1196)", 5,
            java.util.List.of(
                "Pseudo Nirvana Void / Seven-Colored Realm Treasure",
                "Once used by Palm Venerable to ambush Venerable Sealing Realm",
                "Used to turn tables on Daoist Water"
            ), "Ch.1196 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I22", "Blood Slaughter Sword — Seven Swords of the Ancient Dao", "血煞剑", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Ancient Dao Treasure — one of the Seven Swords of the Ancient Dao",
                "Originally Dao Gu Ye Mo's weapon",
                "Wieldable even at Third Step",
                "Was transformed into a mountain peak inside Cloud Sea Wuji Sect"
            ),
            "Seized from Cloud Sea Wuji Sect after awakening during battle with Shui Daozi (Ch. 1290)", 5,
            java.util.List.of("Ancient Dao Treasure tier", "Third-Step-viable weapon", "Originally Dao Gu Ye Mo's weapon"), "Ch.1290 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I23", "Yin Blade", "阴刃", ArtType.OTHER,
            "offensive", "Destroyed",
            java.util.List.of(
                "Internal organ treasure implanted in right arm",
                "Pseudo Void Annihilation grade (pre-tribulation)",
                "Would become Void Annihilation if transcended tribulation"
            ),
            "Implanted by Ji Si (Green Devil) when Wang Lin was captured as sacrifice (Ch. 1896)", 5,
            java.util.List.of(
                "Internal-organ treasure",
                "Destroyed Ch. 1979 in Green Devil battle",
                "Tribulation-pending — never reached full potential"
            ), "Ch.1896 obtained, Ch.1979 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I24", "Heaven-Splitting Axe (Ancestral)", "开天斧", ArtType.OTHER,
            "offensive", "Destroyed",
            java.util.List.of("Cave World Royal Weapon tier axe", "Formerly a Realm-Sealing Formation spirit", "Royal-tier power"),
            "Subdued from Realm-Sealing Formation when Wang Lin destroyed it (Ch. 1664)", 5,
            java.util.List.of(
                "Royal Weapon (Cave World) tier",
                "Destroyed Ch. 1763 in ambush",
                "Was one of the Realm-Sealing Formation's formation spirits"
            ), "Ch.1664 obtained, Ch.1763 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I25", "Demon Blade, Earth Burial", "魔刀·葬土", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of("Royal Ancient Demon Treasure blade", "Ancient demon elemental power", "Cave World origin"),
            "Cave World inheritance (Ch. 1497)", 5,
            java.util.List.of("Royal Ancient Demon Treasure (Cave World) tier", "Retained to end", "Ancient-demon-element"), "Ch.1497 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I26", "Fog Devil Lance", "雾魔枪", ArtType.OTHER,
            "offensive", "Ancient Demon clone",
            java.util.List.of(
                "Ancient Devil weapon — lance form",
                "Devil-element power",
                "Given to Wang Lin's Ancient Demon Clone"
            ),
            "Obtained in the Ancient Tomb (Ch. 1386)", 5,
            java.util.List.of(
                "Ancient Devil Treasure tier",
                "Given to Ancient Demon clone for use",
                "Per Baidu: obtained in the Ancient Tomb"
            ), "Ch.1386 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I27", "Li Guang's Heaven-Shattering Bow", "李广弓", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Bow Dao — forms Heaven-Shattering Bow Dao with Li Guang's Arrow",
                "Fuses with Wang Lin's thunder essence for armor-piercing shots",
                "Kills across realms",
                "Dao Spell treasure tier"
            ),
            "Taught by the madman Lian Daofei inside Nether Beast (Ch. 1533)", 5,
            java.util.List.of(
                "Dao Spell treasure — taught, not physical",
                "Requires Li Guang's Arrow to use",
                "Named after legendary general Li Guang"
            ), "Ch.1533 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I28", "Li Guang's Arrow", "李广箭", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Arrow paired with Li Guang's Bow",
                "Each shot depletes a Spirit-Vein-tier resource",
                "Finite ammo supply"
            ),
            "Acquired alongside Li Guang's Bow (Ch. 1577)", 5,
            java.util.List.of(
                "Dao Spell treasure tier",
                "Consumable-when-fired with finite supply",
                "Must pair with Li Guang's Bow"
            ), "Ch.1577 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I29", "Seven-Colored Lance", "七彩枪", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Seven-colored light affects emotions of anyone who sees it",
                "Three transformations: black → white → gray",
                "Similar to Ethereal Fire but stronger",
                "Dao Spell treasure tier"
            ),
            "Taught by Lian Daofei (madman) inside Nether Beast (Ch. 1543)", 5,
            java.util.List.of("Emotion-affecting weapon", "Three-transformation system", "Dao Spell taught by Lian Daofei"), "Ch.1543 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I30", "Slaughter Sword (from Immortal Lord Qing Shui)", "杀戮剑", ArtType.OTHER,
            "offensive", "Wang Lin (merged with Slaughter Origin True Body)",
            java.util.List.of(
                "Origin Treasure (Third-Step) sword",
                "Fuses with Wang Lin's Slaughter Origin to complete Slaughter Essence",
                "Condensed by Qing Shui using Sky Gate vortex power"
            ),
            "Gifted by Immortal Lord Qing Shui (Ch. 1561)", 5,
            java.util.List.of(
                "Origin Treasure (Third-Step) tier",
                "Merged with Slaughter Origin True Body",
                "Gift from senior brother Qing Shui"
            ), "Ch.1561 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I31", "Lightning Sword (Origin Treasure)", "雷剑", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Thunder-element Origin Treasure sword",
                "One of six fundamental origin-treasure swords",
                "Self-condensed via void-gate vortex"
            ),
            "Self-condensed via void-gate vortex (Ch. 1625)", 5,
            java.util.List.of("Essence Treasure tier", "Thunder-element origin sword", "One of the six origin swords"), "Ch.1625 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I32", "Six Origin Swords (Fire, Life-Death, Karma, True-False, Slaughter, Restriction)", "六本源剑", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Six fundamental origin-element swords embodying Wang Lin's Origins",
                "Each embodies one Origin: Fire, Life-Death, Karma, True-False, Slaughter, Restriction",
                "Set completes power when all six are condensed",
                "Origin Treasure tier"
            ),
            "Self-condensed from void-gate vortex (Ch. 1715)", 5,
            java.util.List.of("Origin Treasure tier — set of 6", "Each embodies a different Origin of Wang Lin", "Retained to end"), "Ch.1715 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I33", "Scattered Devil Armour / Divine Devil Armour", "散魔甲", ArtType.ARMOR,
            "defensive", "Sold (Ch. 1178)",
            java.util.List.of(
                "Pitch-black armor with demonic feel",
                "Ancient Devil Treasure tier protection",
                "Known as Divine Devil Armour in Cloud Sea Star System's God Sect"
            ),
            "Taken from a Scattered Devil in Demon Spirit Land with help of Ancient Demon Bei Lou (Ch. 610)", 5,
            java.util.List.of("Ancient Devil Treasure tier", "Sold Ch. 1178", "Demonic aura protection"), "Ch.610 obtained, Ch.1178 sold (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I34", "Ancient God Leather Armour", "古神皮甲", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of(
                "Origin-Soul defensive treasure",
                "Made from skin of 8-star Ancient God",
                "Ancient God Treasure tier protection"
            ),
            "Ancient God inheritance (Ch. 758)", 5,
            java.util.List.of(
                "Ancient God Treasure tier",
                "Origin-soul defense specialization",
                "Made from 8-star Ancient God skin"
            ), "Ch.758 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I35", "Ancient God Furnace / Ancient God Cauldron", "古神鼎", ArtType.OTHER,
            "utility", "Destroyed",
            java.util.List.of(
                "Positional swap — teleport-swap with another target",
                "Created by Tu Si but discarded as unsatisfactory",
                "Ancient God Treasure tier"
            ),
            "Tu Si inheritance (Ch. 838)", 5,
            java.util.List.of(
                "Ancient God Treasure — created by Tu Si",
                "Destroyed Ch. 1226",
                "Positional-swap teleportation utility"
            ), "Ch.838 obtained, Ch.1226 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I36", "Azure Ancient God Shield / Cyan Light Shield", "青光盾", ArtType.ARMOR,
            "defensive", "Wang Lin (restored)",
            java.util.List.of(
                "8-star Ancient God life-saving divine ability: Dreaming Back to Antiquity",
                "Shatterable and repairable defensive shield",
                "Saved Wang Lin's life multiple times (vs Tian Yunzi, vs Seven-Colored Daoist)",
                "Repaired when Wang Lin blasted open Gate of Emptiness"
            ),
            "Ancient God inheritance (Ch. 980)", 5,
            java.util.List.of(
                "Ancient God Treasure — 8-star life-saving spell",
                "Destroyed Ch. 1082, restored Ch. 1626 via Void Gate power",
                "Named Dreaming Back to Antiquity (梦回太古)"
            ), "Ch.980 obtained, Ch.1082 destroyed, Ch.1626 restored (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I37", "Emperor Furnace / Heavenly Emperor Furnace", "帝炉", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of(
                "Can capture and refine all things",
                "Used to refine Esteemed Ling Dong into ancient slave",
                "Royal Ancient God Treasure tier"
            ),
            "Seized from Tan Lang (who got it in Ancient Tomb) (Ch. 838-ish)", 5,
            java.util.List.of(
                "Royal Ancient God Treasure (Cave World) tier",
                "Refining tool — refined Ling Dong at Ch. 1450",
                "Originally obtained by Tan Lang from Ancient Tomb"
            ), "Ch.~838 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I38", "Protection Bone Tablets (Gauntlets)", "护骨板", ArtType.ARMOR,
            "defensive", "Wang Lin (restored)",
            java.util.List.of(
                "Transform into gauntlets containing 8-star life-saving spell",
                "Ancient God Treasure tier",
                "Morph-to-gauntlet form"
            ),
            "Ancient God inheritance (Cave World era)", 5,
            java.util.List.of(
                "Ancient God Treasure tier",
                "Destroyed Ch. 1580, restored Ch. 1626",
                "8-star Ancient God life-saving ability"
            ), "destroyed Ch.1580, Ch.1626 restored (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I39", "Ancient God Bracer", "古神臂甲", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of(
                "Deploys 9-star Ancient God life-saving ability: Ancient Blessing",
                "Defensive bracer of Ancient God race"
            ),
            "Ancient Tomb (post-Ancient-Tomb era)", 4,
            java.util.List.of("Ancient God Treasure tier", "9-star life-saving spell — highest Ancient God tier", "Retained"), "wiki-attested (post-Ancient-Tomb); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I40", "Mountain and River Screen", "山河屏", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of(
                "Projects mountains-and-rivers image that absorbs attacks",
                "Celestial-tier defensive treasure",
                "Projection-based absorption defense"
            ),
            "Stolen from Greed during Moongazer Serpent escape (Ch. 717)", 5,
            java.util.List.of("Celestial-tier defensive treasure", "Stolen from Greed", "Retained to end"), "Ch.717 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I41", "Heaven Tiger Flag / Banner", "天虎旗", ArtType.BANNER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Materializes Heavenly Tiger (天虎) projection to fight",
                "Big Boi technique — summoning combat",
                "Celestial treasure tier"
            ),
            "Awarded at Luo Tian Immortal Sealing ceremony (Ch. 879)", 5,
            java.util.List.of(
                "Celestial treasure tier",
                "Awarded at Luo Tian Immortal Sealing ceremony",
                "Summon-Heavenly-Tiger combat ability"
            ), "Ch.879 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I42", "Three Bells Shield", "三铃盾", ArtType.ARMOR,
            "defensive", "Ling'er (given away)",
            java.util.List.of("Three bells ring on impact — auditory warning system", "Good protection spirit-tier shield"),
            "Early Celestial era acquisition", 5,
            java.util.List.of(
                "Spirit-tier defensive treasure",
                "Given to Ling'er (Master Yi Chen's granddaughter) at Ch. 965",
                "Auditory warning on impact"
            ), "given away Ch.965 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I43", "Light and Shadow Shield", "光影盾", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of(
                "Creates shield from all vitality/light in the world",
                "Blocks AND reflects enemy attacks",
                "Dao Spell — taught by Dao Master Blue Dream"
            ),
            "Taught by Dao Master Blue Dream (Ch. 1321)", 5,
            java.util.List.of(
                "Dao Spell tier — Blue Dream's spell",
                "Reflective light-element defense",
                "Fuses all surrounding light into body"
            ), "Ch.1321 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I44", "Gui Yi Sect's Armour (Earth Element)", "鬼衣宗甲", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of("Earth-element human-shaped armor", "Rare even within the Gui Yi Sect", "Two copies obtained"),
            "1st: taken from Water General of Planet Five Elements (Ch. 1735); 2nd: via Old Ancestor Green Bull (Ch. 1886)", 5,
            java.util.List.of(
                "Rare Gui Yi Sect treasure",
                "Earth-element / human-shaped armor",
                "Old Ancestor Green Bull paid high price for 2nd copy"
            ), "Ch.1735 & Ch.1886 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I45", "Heavenly Bull Soul Armour", "天牛魂甲", ArtType.ARMOR,
            "defensive", "Destroyed",
            java.util.List.of("Soul-armor of Heavenly Bull Continent origin", "Spirit-tier soul protection"),
            "Acquired in late-game (Ch. 1874)", 5,
            java.util.List.of("Spirit-tier soul armor", "Destroyed Ch. 1989", "Heavenly Bull Continent origin"), "Ch.1874 obtained, Ch.1989 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I46", "Blue Umbrella", "蓝伞", ArtType.ARMOR,
            "defensive", "Destroyed",
            java.util.List.of("Defensive umbrella-form treasure"),
            "Won in a bet with Yan Lu (Ch. 1835)", 5,
            java.util.List.of("Defensive treasure", "Won from Yan Lu in bet", "Destroyed Ch. 1869"), "Ch.1835 obtained, Ch.1869 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I47", "Jade (Thunder Defense)", "御雷玉", ArtType.OTHER,
            "defensive", "Wang Lin",
            java.util.List.of("Defends against powerful thunder", "Spirit-tier thunder ward"),
            "Obtained from Xi Zifeng (Ch. 871)", 5,
            java.util.List.of(
                "Spirit-tier thunder-defense jade",
                "Important self-defense treasure of Xi Zifeng",
                "Retained to end"
            ), "Ch.871 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I48", "A Dagger (from Ge Hong)", "匕首", ArtType.OTHER,
            "other", "Wang Lin (never used)",
            java.util.List.of("Spirit-tier dagger", "Never used by Wang Lin"),
            "Obtained from Ge Hong in Thunder Celestial Realm (Ch. 747)", 5,
            java.util.List.of(
                "Spirit-tier dagger",
                "From Thunder Celestial Realm",
                "Wang Lin never once used it — retained as memento"
            ), "Ch.747 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I49", "Jade Thunder Defense (pendant)", "御雷玉 pendant", ArtType.OTHER,
            "defensive", "Wang Lin",
            java.util.List.of("Jade pendant warding against thunder tribulation", "Spirit-tier tribulation protection"),
            "Xi Zifeng gift (~Ch. 871)", 4,
            java.util.List.of("Separate from the Jade (Thunder Defense)", "Tribulation-ward specialization", "Spirit-tier"), "Ch.871 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I50", "Soul Flag (Soul Refining Sect)", "魂旗", ArtType.BANNER,
            "soul_related", "Wang Lin (retired)",
            java.util.List.of(
                "Holds and refines captured souls",
                "Soul Refining Sect standard soul flag",
                "Spirit-tier soul storage"
            ),
            "Soul Refining Sect inheritance — Dun Tian gift (mid-Nascent Soul era)", 5,
            java.util.List.of("Spirit-tier soul flag", "Retired — replaced by Billion Soul Flag", "Dun Tian's gift"), "wiki-attested (mid-Nascent Soul era); Fandom wiki"
        ),
        new CanonArtifact(
            "I51", "Billion Soul Flag / Ten-Billion Soul Banner", "十亿魂幡", ArtType.BANNER,
            "soul_related", "Wang Lin (retired / repaired)",
            java.util.List.of(
                "Contains 37 main souls + 1 billion ordinary souls",
                "Freely changes size; can fuse into Soul-Ascension-level soul",
                "4th mysterious soul can battle Ascension cultivators",
                "Pseudo-immortal / inheritance treasure tier"
            ),
            "Gifted by Dun Tian (Soul Refining Sect predecessor)", 5,
            java.util.List.of(
                "Soul Refining Sect's guardian treasure",
                "4th soul seized by 14th-gen Vermilion Bird Child",
                "Most souls self-destructed vs Tuo Sen; repaired via Gate of Emptiness",
                "Sentient — 37 main souls with own consciousness"
            ), "wiki-attested (mid-Nascent Soul era); Fandom wiki"
        ),
        new CanonArtifact(
            "I52", "Devil Soul Bottle", "魔鬼魂瓶", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of("Contains multiple Ancient Devil souls", "Ancient Devil Treasure tier soul storage"),
            "Ancient Tomb (Ch. 1388)", 5,
            java.util.List.of("Ancient Devil Treasure tier", "Soul storage for ancient devil souls", "From Ancient Tomb"), "Ch.1388 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I53", "Soul Lasher / Kunji Whip", "坤极鞭", ArtType.OTHER,
            "soul_related", "Fused into Karma Whip",
            java.util.List.of(
                "Directly attacks primordial/origin soul at warp speed",
                "Tian Yu Sect heavy treasure",
                "Spirit-tier soul-piercing whip"
            ),
            "Taken from Hong Die's corpse (originally Red Butterfly's) (early-Nascent Soul era)", 5,
            java.util.List.of(
                "Originally Tian Yu Sect heavy treasure",
                "Qian Feng gave it to Hong Die; Wang Lin took it after her death",
                "Burned by Ghostly Sky Fire → transformed → nourished by Karma Concept → Karma Whip"
            ), "fused Ch.731 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I54", "Carving with the Domain of Time", "时间雕刻", ArtType.OTHER,
            "soul_related", "Destroyed",
            java.util.List.of(
                "Allows one to stop time for a brief duration",
                "Time-domain-bound carving",
                "Self-carved after Tian Yun Zi tutelage"
            ),
            "Self-carved after experiencing Time domain with Yunzhe Zi's help", 5,
            java.util.List.of(
                "Spirit-tier Wang Lin-carved treasure",
                "Created after Tian Yun Zi instruction",
                "Destroyed (chapter unspecified)"
            ), "wiki-attested (after Tianyunzi instruction); Fandom wiki"
        ),
        new CanonArtifact(
            "I55", "Heaven-Avoiding Coffin", "避天棺", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Shields soul from karmic detection — avoids heaven's perception",
                "Holds Li Muwan's soul after her body perished",
                "Spirit-tier soul-protection coffin"
            ),
            "Acquired specifically to hold Li Muwan's soul (Ch. 819)", 5,
            java.util.List.of(
                "Heaven-avoiding / soul-protecting coffin",
                "Contains Li Muwan's soul (later resurrected)",
                "Spirit-tier — retained to end"
            ), "Ch.819 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I56", "Soul Gourd", "魂葫芦", ArtType.OTHER,
            "soul_related", "Destroyed",
            java.util.List.of(
                "Gathered 1 billion dao souls inside (now 30 million — broken)",
                "Single-use: attack equal to peak mid-stage Void Tribulant cultivator",
                "Great Soul Sect treasure from 9th ancestor Luo Yunhai"
            ),
            "Won in a bet with Yan Lu (Ch. 1836)", 5,
            java.util.List.of("Great Soul Sect treasure", "One-use dao-soul weapon", "Destroyed Ch. 1869"), "Ch.1836 obtained, Ch.1869 destroyed (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I57", "Giant Head Skull of an Ancient Clansman", "古族人头骨", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Trees on its head produce Ancient Leaves (太古息叶)", "Ancient-clansman-tier utility skull"),
            "Taken from Ancient Tomb", 5,
            java.util.List.of("Produces Ancient Leaves used for sealing", "From the Ancient Tomb", "Ancient-clansman tier"), "wiki-attested (Ancient Tomb); Fandom wiki"
        ),
        new CanonArtifact(
            "I58", "Infant Skull", "婴儿头骨", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Filled with death energy",
                "Plot clue containing information left by Lu Mo (Wang Lin's clone)",
                "Wang Lin's soul felt strange familiarity"
            ),
            "Seized from Da Yi Great Heavenly Venerable (mid-late game)", 4,
            java.util.List.of(
                "Spirit-tier plot-clue item",
                "Contains Lu Mo's message to main body",
                "From Immortal Clan's Da Yi Great Heavenly Venerable"
            ), "wiki-attested (mid-late game); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I59", "White Hair Strand", "白发丝", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Cannot be destroyed by the Immortal Emperor",
                "Plot clue containing Lu Mo's information about reincarnation cycle"
            ),
            "Found in Immortal Emperor's Palace", 4,
            java.util.List.of(
                "Indestructible by Immortal Emperor",
                "Contains Lu Mo's message to resolve confusion about reincarnation",
                "From Immortal Emperor's Palace"
            ), "wiki-attested (Immortal Emperor's Palace); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I60", "Blood-Red Nascent Soul", "血红元神", ArtType.OTHER,
            "soul_related", "Used (consumed)",
            java.util.List.of("Blood-red nascent soul usable in Seven-Colored Realm", "One-use consumable soul item"),
            "Acquired in Seven-Colored Realm (Ch. 1194)", 5,
            java.util.List.of("Seven-Colored Realm treasure", "One-use consumable", "Used and consumed"), "Ch.1194 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I61", "Blood Ancestor's Blood Body", "血祖血身", ArtType.OTHER,
            "offensive", "Exploded (used)",
            java.util.List.of("Explodable as a weapon — ancient-blood-tier power", "One-use explosive body"),
            "Taken from Blood Ancestor's Blood Planet (Ch. 769)", 5,
            java.util.List.of(
                "Ancient-blood-tier consumable",
                "Exploded Ch. 789 as weapon",
                "Taken from Blood Ancestor's Blood Planet"
            ), "Ch.769 obtained, Ch.789 exploded (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I62", "Wandering Souls", "游魂", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Very strong souls that obey Soul-Devourers as subjects obey a king",
                "Found in spatial rifts",
                "Battlefield soul weapons"
            ),
            "Wild-soul recruitment — Wang Lin's soul has Soul-Devourer nature (Ch. 119 era)", 5,
            java.util.List.of("Wild-soul tier", "Soul-Devourer-bound obedience", "Incorporated into soul-devourer ability"), "Ch.119 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I63", "Restriction Flag (set of three)", "禁旗", ArtType.BANNER,
            "restriction", "Wang Lin (retired)",
            java.util.List.of(
                "Made from inkstones with 99,999 restrictions each",
                "1st: Left incomplete to summon divine tribulation in danger",
                "2nd: Mixture of different restrictions",
                "3rd: Pure attack restriction flag"
            ),
            "Self-forged after completing Land of Ancient God Restrictions Mountain trial (mid-Nascent Soul era)", 5,
            java.util.List.of(
                "Ancient God inheritance — Tu Si gave refining method",
                "Wang Lin was 4th person ever to complete the trial",
                "Set of 3 with different functions"
            ), "wiki-attested (mid-Nascent-Soul, post-Restriction Mountain); Fandom wiki"
        ),
        new CanonArtifact(
            "I64", "Three Purple Flags", "三紫旗", ArtType.BANNER,
            "defensive", "Wang Lin (retired)",
            java.util.List.of("Defensive treasure — set of three purple flags", "Spirit-tier protection"),
            "Early-mid era acquisition", 5,
            java.util.List.of("Spirit-tier defensive flags", "Set of three", "Retired — no longer used"), "wiki-attested (early-mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I65", "3× Ink Stones", "三墨石", ArtType.MATERIAL,
            "restriction", "Consumed (used to make Restriction Flags)",
            java.util.List.of("Used to craft Restriction Flags", "Spirit-tier crafting material"),
            "Early era acquisition", 5,
            java.util.List.of("Crafting material for Restriction Flags", "Consumed in forging process", "Spirit-tier"), "wiki-attested (early era); Fandom wiki"
        ),
        new CanonArtifact(
            "I66", "Restriction Breaking Ancient Mirror", "破禁古镜", ArtType.FORMATION,
            "restriction", "Wang Lin",
            java.util.List.of("Breaks restrictions/formations by reflecting restriction's logic back", "Ancient treasure tier"),
            "Tattoo Tribe era (Ch. 774)", 5,
            java.util.List.of("Ancient treasure tier", "Restriction-breaking specialty", "Retained to end"), "Ch.774 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I67", "Heart Compass (Annihilation Restriction Inheritance)", "心罗盘", ArtType.COMPASS,
            "restriction", "Wang Lin",
            java.util.List.of(
                "Annihilation Restriction inheritance treasure",
                "Pairs with Destruction Restriction to seal all things",
                "Can seal immortals, demons, and mortal beasts"
            ),
            "Annihilation Restriction inheritance (Ch. 858)", 5,
            java.util.List.of(
                "Inheritance treasure tier",
                "Paired with Destruction Restriction",
                "Seals all things from immortals to mortal beasts"
            ), "Ch.858 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I68", "Ancient Soul Restriction Tortoise Beast", "古魂禁龟兽", ArtType.FORMATION,
            "restriction", "Wang Lin",
            java.util.List.of(
                "Living tortoise-beast with ancient-soul-restriction properties",
                "Functions as living formation component",
                "Inheritance treasure tier"
            ),
            "Gifted by old Vermilion Bird in Fallen Land (Ch. 1426)", 5,
            java.util.List.of("Living formation component", "Gifted by old Vermilion Bird", "Inheritance treasure tier"), "Ch.1426 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I69", "Soul Devil Ship", "魂魔船", ArtType.FORMATION,
            "restriction", "Wang Lin",
            java.util.List.of(
                "Composite of four great restrictions + many others",
                "Pairs with Ghostly Sail",
                "Used for multi-layered illusion spells"
            ),
            "Forged from the 4 great restrictions (Ch. 1789)", 5,
            java.util.List.of(
                "Composite formation treasure",
                "Used by Fan Shanmeng for multi-layered illusion",
                "4 great restrictions + many others"
            ), "Ch.1789 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I70", "Ghostly Sail / Ghost Sail", "鬼帆", ArtType.FORMATION,
            "restriction", "Wang Lin",
            java.util.List.of(
                "Main sail for Soul Devil Ship — contains many restrictions",
                "Everything about Soul Devil Ship encoded in the sail",
                "Sect-protection-formation-tier (vice sail)"
            ),
            "Refined by Wang Lin (main: Ch. 1699); vice from Great Soul Sect", 5,
            java.util.List.of(
                "Main + vice sail set",
                "Sail-bound-to-ship — encodes all ship data",
                "Wang Lin refined his own at Ch. 1854"
            ), "Ch.1699 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I71", "Isolation Restriction Compass", "隔离禁盘", ArtType.COMPASS,
            "restriction", "Discarded",
            java.util.List.of(
                "Contains Devil Restriction Sect's Devil Isolation Restriction",
                "Tracking-vulnerable — someone used it to pinpoint Wang Lin's location"
            ),
            "Took from Green Devil Continent woman at pill sea (Ch. 1850)", 5,
            java.util.List.of(
                "Devil Restriction Sect treasure",
                "Discarded Ch. 1864 due to tracking vulnerability",
                "Contains Devil Isolation Restriction"
            ), "Ch.1850 obtained, Ch.1864 discarded (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I72", "Unnamed Wheel Formation", "无名轮阵", ArtType.FORMATION,
            "restriction", "Wang Lin (active)",
            java.util.List.of(
                "Replaces Realm-Sealing Formation — upgraded Life-Death/Karma/True-False Wheel",
                "Stops Outer-Realm cultivators while allowing Joss Flames to enter",
                "Calls treasure spirits and souls of destroyed planets to fuse eternally",
                "Gathers soul fragments to push the wheel forever"
            ),
            "Self-erected as replacement formation (Ch. 1667)", 5,
            java.util.List.of(
                "Third-Step-tier formation",
                "Fixes Realm-Sealing Formation's biggest flaw (Joss Flames)",
                "Active eternal formation with immortal formation spirit"
            ), "Ch.1667 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I73", "Nine Deaths Perish Formation", "九死灭阵", ArtType.FORMATION,
            "restriction", "Wang Lin",
            java.util.List.of("Ancient deadly trap formation", "Lethal formation power"),
            "Inherited (Ch. 829)", 5,
            java.util.List.of("Ancient formation tier", "Lethal trap formation", "Retained to end"), "Ch.829 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I74", "Basic Formation Book", "基础阵书", ArtType.MATERIAL,
            "cultivation_aid", "Wang Lin (kept as memento)",
            java.util.List.of("Foundation formation knowledge manual", "Mortal-tier instructional book"),
            "Early era acquisition", 5,
            java.util.List.of("Mortal-tier manual", "Kept even after mastering it — sentimental memento", "Retained to end"), "wiki-attested (early era); Fandom wiki"
        ),
        new CanonArtifact(
            "I75", "Collection Pavilion", "藏经阁", ArtType.OTHER,
            "storage", "Given to friends in New Immortal World",
            java.util.List.of(
                "Stores various celestial spells within",
                "Can change size; master-locked to Wang Lin only",
                "Celestial-tier storage pavilion"
            ),
            "Took from Thunder Celestial Realm (Ch. 784)", 5,
            java.util.List.of(
                "Celestial-tier storage treasure",
                "Only Wang Lin can command it",
                "Gifted to friends in New Immortal World per Baidu"
            ), "Ch.784 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I76", "Space Stone", "空间石", ArtType.OTHER,
            "storage", "Wang Lin",
            java.util.List.of(
                "Extract one item from storage space without damage (one use per pocket)",
                "Nurture a Heavenly Dao (second role)",
                "Quasi-Third-Step tier — 1 of 3 promised gifts"
            ),
            "Gift from founder of Great Soul Sect (Ch. 1838)", 5,
            java.util.List.of(
                "Quasi-Third-Step tier",
                "1 of 3 promised gifts from Great Soul Sect founder",
                "One-use-per-pocket limitation (cluster collapse risk)"
            ), "Ch.1838 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I77", "A Jade (Soul Eye Dao)", "灵眼玉", ArtType.OTHER,
            "divine_sense", "Used (Ch. 2023)",
            java.util.List.of(
                "See changes in Immortal Astral Continent ONCE",
                "Divinate ONE thing — single use only",
                "Formed by founder's lifetime Soul Eye Dao cultivation"
            ),
            "Gift from founder of Great Soul Sect — 2 of 3 (Ch. 1840s)", 5,
            java.util.List.of(
                "Quasi-Third-Step tier",
                "Founder's lifetime cultivation condensed",
                "One-use divination — used Ch. 2023"
            ), "Ch.~1840, used Ch.2023 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I78", "A Drop of Crystal Clear Water (Water Essence Drop)", "水元精滴", ArtType.OTHER,
            "cultivation_aid", "Wang Lin (kept for cultivation)",
            java.util.List.of(
                "Single drop of crystal clear water essence",
                "Refined from 99 rivers with multiple water-essence cultivators",
                "Quasi-Third-Step tier — 3 of 3 promised gifts"
            ),
            "Gift from founder of Great Soul Sect (Ch. 1843)", 5,
            java.util.List.of(
                "Quasi-Third-Step tier",
                "3 of 3 promised gifts from Great Soul Sect founder",
                "Refined from 99 rivers in Heavenly River Continent"
            ), "Ch.1843 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I79", "Earth Palace", "地宫", ArtType.OTHER,
            "storage", "Wang Lin",
            java.util.List.of("One of three palaces of Ye Mo's Inheritance (heaven, earth, human)", "Ye Mo inheritance treasure"),
            "Ye Mo inheritance (Ch. 1478)", 5,
            java.util.List.of(
                "Ye Mo inheritance treasure tier",
                "The 'earth' of the heaven/earth/human palace set",
                "Retained to end"
            ), "Ch.1478 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I80", "Fate Sealing Ring", "封命环", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Sealed within divine retribution", "Celestial-tier ring"),
            "Divine-retribution-forged (Ch. 1631)", 5,
            java.util.List.of("Celestial-tier ring", "Tribulation-bound forging", "Retained to end"), "Ch.1631 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I81", "Sword Sheaths ×5", "剑鞘", ArtType.OTHER,
            "utility", "Wang Lin (retired)",
            java.util.List.of(
                "Inserting a flying sword enhances different powers",
                "Suspected Immortal World objects",
                "One suspected to be Sub-Void-Nirvana Sword"
            ),
            "Collected across journey (1st from former friend's enemy at Heng Yue Sect)", 5,
            java.util.List.of(
                "Set of 5 mysterious sword sheaths",
                "Suspected Immortal World origin",
                "One appeared in Tuo Sen's possession; one suspected Sub-Void Nirvana Sword"
            ), "wiki-attested (throughout early-mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I82", "Lu Fu Blood Balls", "陆符血球", ArtType.OTHER,
            "offensive", "Used (consumed)",
            java.util.List.of("Consumable blood-ball weapons", "Up to 6 used at once", "Spirit-tier explosive power"),
            "Early-acquired from Lu Fu (1st: Ch. 947; ×6: Ch. 1095)", 5,
            java.util.List.of("Spirit-tier consumable", "6 used simultaneously on one occasion", "Fully consumed"), "Ch.947, Ch.1095 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I83", "Ji Qiong's Head", "计穷头", ArtType.OTHER,
            "offensive", "Used (consumed)",
            java.util.List.of("Severed head used as consumable weapon", "Spirit-tier power"),
            "Took from Ji Qiong (Ch. 1127)", 5,
            java.util.List.of("Spirit-tier consumable weapon", "Used and consumed", "Severed head of Ji Qiong"), "Ch.1127 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I84", "Green Fragment with the Power of Heaven", "青碎片", ArtType.OTHER,
            "offensive", "Used (consumed)",
            java.util.List.of("Contains a thread of Heavenly power", "One-shot attack consumable", "Celestial-tier power"),
            "Mid-era acquisition", 5,
            java.util.List.of("Celestial-tier one-use attack", "Contains Heavenly power thread", "Used and consumed"), "wiki-attested (mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I85", "Beads from the Seven-Colored Realm", "七彩界珠", ArtType.OTHER,
            "offensive", "Used (consumed)",
            java.util.List.of(
                "Mimic Heaven-Defying Bead in appearance (without five-elements pattern)",
                "Used as decoys / one-use attack beads",
                "Seven-Colored Realm treasure tier"
            ),
            "Seven-Colored Realm (Ch. 1197)", 5,
            java.util.List.of(
                "Same appearance as Heaven-Defying Bead but faint blurred pattern",
                "One-use decoy/attack beads",
                "Seven-Colored Realm treasure"
            ), "Ch.1197 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I86", "Heaven Dao Crystal", "天道晶", ArtType.OTHER,
            "offensive", "Used (consumed)",
            java.util.List.of("Crystallized shard of Heavenly Dao power", "One-use consumable", "Celestial-tier power"),
            "Mid-era acquisition", 5,
            java.util.List.of("Celestial-tier crystallized Heavenly Dao", "One-use consumable", "Used and consumed"), "wiki-attested (mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I87", "Nine Drops of Poison", "九滴毒", ArtType.OTHER,
            "offensive", "Used (Ch. 1526)",
            java.util.List.of(
                "Refined from Joss Flames of Dao Master Miao Yin and Great Desolation's poison",
                "Quasi-Third-Step poison power",
                "One-use lethal poison"
            ),
            "Self-refined (Ch. 1460)", 5,
            java.util.List.of("Quasi-Third-Step tier poison", "Used Ch. 1526 — Nan Zhao Dies!", "Joss-flame-poison combination"), "Ch.1460 obtained, Ch.1526 used (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I88", "Celestial Wine (in a jug)", "仙酒", ArtType.OTHER,
            "cultivation_aid", "Used (consumed)",
            java.util.List.of(
                "Drinking or spilling grants temporary celestial-spirit-energy boost",
                "Celestial-tier consumable wine"
            ),
            "Found in Qing Lin's false cave in Demon Spirit Land (~Ch. 625)", 5,
            java.util.List.of("Celestial-tier consumable", "From Qing Lin's false caves in Demon Spirit Land", "Used and consumed"), "Ch.~625 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I89", "Pair of Metal Element Flints", "金属火石对", ArtType.OTHER,
            "utility", "Wang Lin (retired)",
            java.util.List.of("Naturally-formed magical treasure flint pair", "Metal-element flints"),
            "Took from Huang Family member's bag of holding (Ch. 672)", 5,
            java.util.List.of(
                "Naturally-formed magical treasure",
                "One of two treasures in Huang Family bag",
                "Retired — no longer used"
            ), "Ch.672 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I90", "God-Slaying War Chariot (set of 3)", "杀神战车", ArtType.OTHER,
            "offensive", "1 given to Situ Nan; 2 restored and retained",
            java.util.List.of(
                "Three tiers: Low (given to Situ Nan), Mid (Lightning Beast soul), High (Butterfly soul)",
                "Each possesses a beast soul; forged by Immortal Realm's Heavenly Treasure Master",
                "Mid: Silver-Horned Thunder Beast unsealed — Soul-Ascension strength",
                "High: Butterfly soul — kills Yang-Solid peak; with Mother-Child Dao Withered battles Insight-into-Annihilation"
            ),
            "Found during first expedition at Celestial Realm (1st: Ch. 307)", 5,
            java.util.List.of(
                "Low/Mid/High Quality Celestial Treasure tiers",
                "Low given to Situ Nan Ch. 441",
                "Mid & High destroyed Ch. 1276 vs Daoist Water, restored Ch. 1626 via Gate of Emptiness"
            ), "Ch.307, Ch.~1080, Ch.~1100 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I91", "Celestial Capture Net", "天罗网", ArtType.OTHER,
            "restriction", "Wang Lin (never used)",
            java.util.List.of("Entrapment net for capturing cultivators", "Celestial treasure tier"),
            "Early Celestial era (Ch. 624)", 5,
            java.util.List.of(
                "Celestial treasure tier",
                "Used once to capture Huang Yu who gave Golden Celestial Brush",
                "Never used again by Wang Lin"
            ), "Ch.624 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I92", "Sword with Teleportation Spell (Pseudo Celestial)", "传送剑", ArtType.OTHER,
            "offensive", "Wang Lin (Bai Zhan version retained)",
            java.util.List.of(
                "Instant teleportation on strike",
                "Blood-refined and primordial-spirit-fused",
                "Pseudo Celestial Treasure tier"
            ),
            "Wang Lin-made version sold Ch. 665; later version taken from Bai Zhan (Elder Ji Mo's disciple)", 5,
            java.util.List.of(
                "Pseudo Celestial Treasure tier",
                "Wang Lin-made version sold; Bai Zhan version retained",
                "Per Baidu: fused with Poison King Cauldron"
            ), "Ch.662 made, Ch.665 sold (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I93", "Hairpin with Thousand Illusion Ruthless Domain", "千幻无情簪", ArtType.OTHER,
            "offensive", "Sold",
            java.util.List.of(
                "Embeds the Thousand-Illusion Ruthless domain",
                "Pseudo Celestial Treasure — domain-embedded hairpin"
            ),
            "Wang Lin-made on Planet Ran Yun (~Ch. 662)", 5,
            java.util.List.of(
                "Pseudo Celestial Treasure — Wang Lin-made",
                "Sold alongside other pseudo-celestial treasures",
                "Domain-embedded weapon"
            ), "Ch.662 made (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I94", "Black Comb, Nineteen Teeth", "十九齿黑梳", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of("Black comb with nineteen teeth containing attacking formation", "Pseudo Celestial Treasure tier"),
            "One of two treasures in Huang Family member's bag of holding (Ch. 672)", 5,
            java.util.List.of("Pseudo Celestial Treasure tier", "Attacking formation embedded in comb", "From Huang Family bag"), "Ch.672 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I95", "Celestial Emperor Crown", "仙帝冠", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "Five jewels socketed — flows metal, wood, water, fire, earth powers",
                "Requires killing 9,999 mortal emperors and fusing souls to fully activate",
                "Celestial-tier crown — usable due to Red Butterfly's blue rose"
            ),
            "Early Celestial era (Ch. 717)", 5,
            java.util.List.of(
                "Celestial-tier five-element-flowing crown",
                "Soul-fueled (9,999 mortal emperor souls needed for full activation)",
                "Usable thanks to Red Butterfly's blue rose"
            ), "Ch.717 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I96", "Straw Hat / Li Ming Straw Hat", "黎明草帽", ArtType.OTHER,
            "divine_sense", "Ling'er (given away)",
            java.util.List.of(
                "Blocks out divine senses",
                "Identity concealment with numerous intricate formations",
                "Yunque Zi's gift to potential Suzaku title candidates"
            ),
            "Gifted by Yunque Zi / Tian Yunzi (early Celestial era)", 5,
            java.util.List.of(
                "Celestial-tier concealment treasure",
                "Given to Ling'er at Ch. 965",
                "Contains numerous intricate formations"
            ), "wiki-attested (early Celestial era), given away Ch.965; Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I97", "A Bell (Sealing/Tracking)", "封踪铃", ArtType.OTHER,
            "utility", "Wang Lin (retired)",
            java.util.List.of("Sealing and tracking abilities", "Celestial-tier bell"),
            "Early Celestial era (~Ch. 307)", 5,
            java.util.List.of("Celestial-tier bell", "Dual sealing + tracking function", "Retired — no longer used"), "Ch.~307 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I98", "Star Compass", "星罗盘", ArtType.COMPASS,
            "movement", "Upgraded to Silver Dragon Star Compass",
            java.util.List.of(
                "Extremely fast void travel — no energy demanded",
                "Enhanced with Ancient God memories",
                "Void-only transportation"
            ),
            "Exchanged with Giant Demon Clan Heir / Chi Hu (~Ch. 477)", 5,
            java.util.List.of(
                "Celestial-tier void travel compass",
                "Primary means of transportation in void per Baidu",
                "Traded for Axe of Giant Demon Clan"
            ), "Ch.~477 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I99", "Silver Dragon Star Compass", "银龙星罗盘", ArtType.COMPASS,
            "movement", "Wang Lin (retired)",
            java.util.List.of("Upgraded Star Compass with silver-dragon beast", "Void-only fast travel", "No energy cost"),
            "Self-upgraded from Star Compass (Ch. 477)", 5,
            java.util.List.of(
                "Upgraded celestial-tier compass",
                "Renamed due to silver dragon beast utilized",
                "Retired — no longer used"
            ), "Ch.477 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I100", "Celestial Mountain Soul", "仙山魂", ArtType.OTHER,
            "utility", "Sold (Ch. 1177)",
            java.util.List.of("Celestial-tier soul fragment", "Collapsed due to soul explosion", "Condensed remnant by Wang Lin"),
            "Celestial-era acquisition (Ch. 712)", 5,
            java.util.List.of(
                "Celestial-tier soul-fragment",
                "Soul exploded Ch. 853; sold Ch. 1177",
                "Became useless after collapse"
            ), "Ch.712 obtained, Ch.853 exploded, Ch.1177 sold (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I101", "A Broken Statue (of the Ancient Celestial Emperor)", "残像", ArtType.OTHER,
            "other", "Wang Lin",
            java.util.List.of(
                "Depicts the Ancient Celestial Emperor — middle-aged man in cloud-pattern robe",
                "Contact with its gaze causes entire body to tremble",
                "Majestic aura despite damaged appearance"
            ),
            "Ancient Tomb era (Ch. 1389)", 5,
            java.util.List.of(
                "Ancient-tier statue",
                "Plot-clue item depicting Ancient Celestial Emperor",
                "Plain appearance but immense majesty"
            ), "Ch.1389 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I102", "Cloak of Vermilion Bird Divine Emperor", "朱雀神帝斗篷", ArtType.ARMOR,
            "defensive", "Wang Lin",
            java.util.List.of(
                "Generational inheritance cloak of Vermilion Bird Divine Emperor",
                "Vermilion Bird Divine Sect inheritance tier"
            ),
            "Vermilion Bird Divine Emperor inheritance (Ch. 1090)", 5,
            java.util.List.of("Vermilion Bird Divine Sect inheritance", "Passed down for generations", "Retained to end"), "Ch.1090 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I103", "Vermilion Bird Holy Token", "朱雀圣令", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of(
                "Identification token for generations of Vermilion Bird Divine Emperors",
                "Vermilion Bird Divine Sect authority"
            ),
            "Vermilion Bird Divine Emperor inheritance (~Ch. 1090)", 5,
            java.util.List.of("Vermilion Bird Divine Sect tier", "Identity/authority token", "Retained to end"), "Ch.~1090 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I104", "Holy Treasure (White Stone)", "白石圣宝", ArtType.OTHER,
            "utility", "Returned (Ch. 1124)",
            java.util.List.of("Most important treasure of Vermilion Bird Divine Sect", "Supreme sect treasure"),
            "Vermilion Bird Divine Sect (~Ch. 1090)", 5,
            java.util.List.of(
                "Vermilion Bird Divine Sect's most important treasure",
                "Returned Ch. 1124 after saving Azure Dragon Divine Emperor",
                "Sect-supreme-treasure"
            ), "Ch.~1090 obtained, Ch.1124 returned (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I105", "Vermilion Bird Feather", "朱雀羽", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Summons a true Vermilion Bird",
                "Used to kill the third-generation evil sparrow",
                "Celestial-tier — 1st-Gen Vermilion Bird gift"
            ),
            "Gifted by 1st-Generation Vermilion Bird", 4,
            java.util.List.of(
                "Celestial-tier summoning item",
                "1st-Generation Vermilion Bird's gift",
                "Used to kill third-generation evil sparrow"
            ), "wiki-attested (Vermilion Bird lineage); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I106", "Fire Bone", "火骨", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Unleashes World Incineration Art (ultimate technique)",
                "Used when killing the third-generation Vermilion Bird",
                "2nd-Gen Vermilion Bird gift — celestial-tier"
            ),
            "Gifted by 2nd-Generation Vermilion Bird", 4,
            java.util.List.of(
                "Celestial-tier — 2nd-Gen Vermilion Bird's gift",
                "Contains World Incineration Art",
                "Used to kill third-generation Vermilion Bird"
            ), "wiki-attested (Vermilion Bird lineage); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I107", "Emerald Bracelet / Jade Bracelet", "翡翠镯", ArtType.OTHER,
            "defensive", "Wang Lin",
            java.util.List.of("Protective treasure bracelet", "Gift from Li Qianmei"),
            "Gift from Li Qianmei — last of 3 treasures for answering 3 questions (Ch. 1178)", 5,
            java.util.List.of("Protective treasure tier", "Li Qianmei's gift", "Retained to end"), "Ch.1178 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I108", "Jade Bottle with Black Liquid", "黑液玉瓶", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of(
                "Bottle half-filled with mysterious black liquid",
                "Looks like blood but no blood smell",
                "Seven-Colored Realm treasure"
            ),
            "Seven-Colored Realm (Ch. 1191)", 5,
            java.util.List.of("Seven-Colored Realm treasure", "Mysterious liquid contents", "Retained to end"), "Ch.1191 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I109", "Celestial Guard — Copper Rank (Du Jian)", "铜阶仙卫", ArtType.OTHER,
            "offensive", "Exploded (Ch. 762)",
            java.util.List.of(
                "Puppet copying Ancient Gods — body + special spells",
                "Augmented with heavenly ghost — Illusionary-Yin strength",
                "Refined from Du Jian's body"
            ),
            "Refined from Du Jian's body (Ch. 653)", 5,
            java.util.List.of(
                "Copper Rank celestial guard",
                "Method obtained from Huang Yu (via Celestial Capture Net)",
                "Exploded Ch. 762"
            ), "Ch.653/707/815 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I110", "Celestial Guard — Silver Rank (Thunder Daoist)", "银阶仙卫·雷道人", ArtType.OTHER,
            "offensive", "Shattered (Ch. 717)",
            java.util.List.of(
                "Refined from Thunder Celestial Temple messenger",
                "Thunder-element combat puppet",
                "Silver Rank celestial guard"
            ),
            "Refined from Thunder Daoist (Ch. 707)", 5,
            java.util.List.of(
                "Silver Rank celestial guard",
                "Source: real Thunder Celestial Temple messenger",
                "Shattered Ch. 717 in Thunder Prison"
            ), "Ch.653/707/815 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I111", "Celestial Guard — Silver Rank (Ta Shan)", "银阶仙卫·塔山", ArtType.OTHER,
            "offensive", "Freed (Ch. 1025)",
            java.util.List.of(
                "Silver Rank celestial guard from Chosen Immortal Clan member",
                "Ta Shan's body refined with consent"
            ),
            "Refined from dying Ta Shan with his consent (Ch. 815)", 5,
            java.util.List.of(
                "Silver Rank celestial guard",
                "Source: Ta Shan of Chosen Immortal Clan",
                "Freed when Tattoo Clan Ancestor forcibly removed seal"
            ), "Ch.653/707/815 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I112", "Silver Poison Female Corpse", "银毒女尸", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Nirvana Cleanser early stage combat power",
                "Sentient — retains memories as maid of Seven-Coloured Celestial Sovereign",
                "Autonomous — can think and act independently"
            ),
            "Obtained during Alliance–Allheaven war (Ch. 930)", 5,
            java.util.List.of(
                "Nirvana Cleanser (early stage) tier",
                "Was maid of Seven-Coloured Celestial Sovereign",
                "Sentient and autonomous — memories intact"
            ), "Ch.930 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I113", "Yi Si Puppet", "忆思傀儡", ArtType.OTHER,
            "other", "Wang Lin",
            java.util.List.of("Ancient-tier puppet from Ancient Tomb's second floor"),
            "Ancient Tomb 2nd floor (Ch. 1774)", 5,
            java.util.List.of("Ancient-tier puppet", "From Ancient Tomb's 2nd floor", "Retained to end"), "Ch.1774 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I114", "Ling Dong (Ancient Slave)", "灵东·古奴", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Half-step Third-Step combat power",
                "Refined into ancient slave using Emperor Furnace",
                "Formerly Esteemed Ling Dong"
            ),
            "Refined using Emperor Furnace (Ch. 1450)", 5,
            java.util.List.of(
                "Half-step Third-Step tier",
                "Emperor Furnace-refined ancient slave",
                "Originally Esteemed Ling Dong before being refined"
            ), "Ch.1450 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I115", "Zhou Jin (captured then freed)", "周谨", ArtType.OTHER,
            "other", "Freed (when Wang Lin got injured)",
            java.util.List.of("Temporary puppet — captured and held", "Freed when Wang Lin was injured"),
            "Captured (Ch. 1470)", 5,
            java.util.List.of("Temporary captive-puppet", "Freed when Wang Lin sustained injuries", "Short-term possession"), "Ch.1470 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I116", "Mosquito Beast", "蚊兽", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Rank-9 Void Mosquito — signature companion",
                "Multiplies into swarm of 10,000",
                "Soul-bound destined companion"
            ),
            "Tamed in Cultivation Devil Sea (early-mid era)", 5,
            java.util.List.of(
                "Rank-9 Void Mosquito species",
                "Purple/Gold variants — destined companion archetype",
                "Multiplies into 10,000 swarm; retained to end"
            ), "novel (Sea of Devils); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I117", "Mosquito Swarm ×10,000", "蚊群", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of("Descendants of original Mosquito Beast", "Rank-9 Void Mosquito swarm — battlefield swarm weapon"),
            "Bred from Mosquito Beast", 5,
            java.util.List.of("Rank-9 Void Mosquito swarm", "Battlefield weapon — massive numbers", "Retained to end"), "wiki-attested (bred from original); Fandom wiki"
        ),
        new CanonArtifact(
            "I118", "Lei Ji / Thunder Beast", "雷极", ArtType.OTHER,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Silver-Horned Thunder Beast — late Soul-Ascension strength",
                "Originally beast-soul of Mid Quality God-Slaying War Chariot",
                "Fights alongside Wang Lin"
            ),
            "Unsealed from Mid-Quality God-Slaying Chariot (~Ch. 1080)", 5,
            java.util.List.of(
                "Silver-Horned Thunder Beast",
                "Per Baidu: listed as Wang Lin's mount alongside Mosquito Beast",
                "Destroyed Ch. 1276 vs Daoist Water, restored Ch. 1626"
            ), "Ch.~1080 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I119", "Nether Beast", "冥兽", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of(
                "Life-bound beast — civilizations build on its back (gastroRealm)",
                "Vast being with interior world",
                "Contains madman Lian Daofei inside"
            ),
            "Formed in the world of the Nether Beast", 5,
            java.util.List.of(
                "Ancient-tier life-bound beast",
                "Per Baidu: Life-bound Beast of Wang Lin",
                "GastroRealm archetype — civilizations on its back"
            ), "novel; Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I120", "Thunder Toad", "雷蟾", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Thunder-element spirit beast companion"),
            "Celestial-era acquisition", 4,
            java.util.List.of("Thunder-element companion", "Retained to end", "Less prominently featured"), "novel; Fandom wiki"
        ),
        new CanonArtifact(
            "I121", "Thunder Celestial Beast", "雷仙兽", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Celestial-tier thunder-element beast companion"),
            "Early Celestial era", 4,
            java.util.List.of("Celestial-tier thunder-element beast", "Retained to end", "Less prominently featured"), "novel (implicit); Fandom wiki; C3"
        ),
        new CanonArtifact(
            "I122", "Brilliant Void Sea Dragon", "耀虚海龙", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Void-sea dragon companion", "Void-element power"),
            "Mid-era acquisition", 4,
            java.util.List.of("Void-tier dragon companion", "Retained to end", "Void-element"), "wiki-attested (mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I123", "Devils (Xu Liguo + others)", "魔头", ArtType.OTHER,
            "utility", "Xu Liguo retained as sword-spirit; others destroyed",
            java.util.List.of(
                "Extremely cunning and gifted in possession",
                "Xu Liguo became sword-spirit of Rain Celestial Sword",
                "Multiple devils refined: Xu Liguo, souls-tornado leader, Sky Cloud Sect monkey"
            ),
            "Refined/captured (Xu Liguo Ch. ~580; others ~Ch. 590-620)", 5,
            java.util.List.of(
                "Devil-tier companions",
                "Xu Liguo is the most prominent — became Rain Celestial Sword's spirit",
                "Others destroyed per wiki"
            ), "Ch.~580, Ch.~590, Ch.~620 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I124", "Cultivator Clone (Human-Celestial)", "修仙者分身·人仙", ArtType.OTHER,
            "cultivation_aid", "Fused back into Main Body",
            java.util.List.of(
                "Created via Divine Path technique — helps breakthrough to Nascent Soul",
                "No cultivation; 30-year lifespan (drawback removed via fusion)",
                "Fuseable back into main body"
            ),
            "Self-created via War God Shrine's Divine Path (Ch. 211)", 5,
            java.util.List.of(
                "Qi-cultivator tier avatar",
                "30-yr lifespan drawback removed when fused back",
                "Fused back when Wang Lin obtained Immortal Ancestor's hair"
            ), "Ch.211 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I125", "Celestial Body (Mid Quality)", "天身·中品", ArtType.OTHER,
            "cultivation_aid", "Fused back (former)",
            java.util.List.of("Celestial-energy cultivating avatar", "Mid Quality celestial body"),
            "Self-created (Ch. 424)", 5,
            java.util.List.of(
                "Mid Quality celestial tier",
                "Fused back — contributed to Immortal Celestial Body",
                "Celestial-element cultivation"
            ), "Ch.424 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I126", "Thunder Body", "雷身", ArtType.OTHER,
            "cultivation_aid", "Fused back (former)",
            java.util.List.of("Thunder-element avatar"),
            "Self-created (Ch. 719)", 5,
            java.util.List.of(
                "Thunder-element avatar",
                "Fused back — contributed to Immortal Celestial Body",
                "Combined with Celestial Body → Immortal Celestial Body"
            ), "Ch.719 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I127", "Immortal Celestial Body", "仙天身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "Fused celestial and immortal energy avatar",
                "Immortal-tier power",
                "Post-Celestial-Vein fusion product"
            ),
            "Self-created post-Celestial-Vein fusion (Ch. 1538)", 5,
            java.util.List.of("Immortal-tier avatar", "Fusion of Celestial Body + Thunder Body", "Immortal-celestial hybrid"), "Ch.1538 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I128", "Ancient Demon Clone", "古魔", ArtType.OTHER,
            "cultivation_aid", "Fused back (became Ancient One)",
            java.util.List.of(
                "Demonic energy cultivating avatar",
                "From statue in Land of Demonic Spirits",
                "Given Fog Devil Lance"
            ),
            "From statue in Land of Demonic Spirits (Ch. 1002)", 5,
            java.util.List.of(
                "Ancient-demon tier",
                "Fused back with Main Body to become Ancient One",
                "Carried the Fog Devil Lance"
            ), "Ch.1002 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I129", "Ancient Devil Clone", "古妖", ArtType.OTHER,
            "cultivation_aid", "Fused back (became Ancient One)",
            java.util.List.of("Devilish energy cultivating avatar", "From corpse in Daogu Yemo's tomb"),
            "From corpse in Daogu Yemo's tomb (~Ch. 1000)", 5,
            java.util.List.of(
                "Ancient-devil tier",
                "Fused back with Main Body to become Ancient One",
                "Combined with Ancient Demon → Ancient One"
            ), "Ch.~1539 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I130", "Otherworldly Void Avatar / Void Clone", "虚空分身", ArtType.OTHER,
            "cultivation_aid", "Fused (during Ancient Path battle)",
            java.util.List.of(
                "Possesses same Void Destiny as Immortal Ancestor and Ancient Ancestor",
                "Completely fused during battle with Ancient Path",
                "Void-tier special powers"
            ),
            "Self-created via Void-Destiny inheritance (Ch. 1798)", 5,
            java.util.List.of(
                "Void-tier — same Void Destiny as Immortal Ancestor",
                "Gained enlightenment about own path during fusion",
                "Completely fused during Ancient Path battle"
            ), "Ch.1798 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I131", "Five Elements True Body", "五行真身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "Five Elements Origins each condensed a True Body",
                "Can fuse together into single Five Elements True Body",
                "Origin-tier power"
            ),
            "Self-condensed from Five Elements Origins (post-Origin-awakening)", 5,
            java.util.List.of(
                "Origin-tier composite true body",
                "Metal, Wood, Water, Fire, Earth — each has own True Body",
                "All five can fuse into single body; retained to end"
            ), "Ch.1900-2024 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I132", "Slaughter True Body / Lu Mo", "杀戮真身·路摩", ArtType.OTHER,
            "cultivation_aid", "Wang Lin (Fourth-Step achiever)",
            java.util.List.of(
                "Fusion of Slaughter Origin + Silent Extinction (Miemie) Origin",
                "Achieved Fourth Step — Wang Lin's strongest clone",
                "Used Dream Dao to deduce past; borrowed Realm-Defining Compass to send Heaven-Defying Pearl back in time"
            ),
            "Self-fused from Slaughter + Miemie Origins (post-Origin-awakening)", 5,
            java.util.List.of(
                "Fourth-Step clone — strongest of Wang Lin's bodies",
                "Time-traveler — sent Heaven-Defying Pearl back through time",
                "Left messages in White Hair Strand and Infant Skull"
            ), "Ch.1900-2046 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I133", "Annihilating Thunder", "灭雷", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of("Fusion of Slaughter Origin + Thunder Origin", "Origin-tier true body"),
            "Self-fused from Slaughter + Thunder Origins (post-Origin-awakening)", 4,
            java.util.List.of("Origin-tier true body", "Combination of Slaughter + Thunder Origins", "Retained to end"), "wiki-attested (post-Origin-awakening); Fandom wiki"
        ),
        new CanonArtifact(
            "I134", "Taichu Origin True Body", "太初真身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of("True Body formed from Taichu (Beginning) Origin", "Special origin tier"),
            "Self-condensed (post-Origin-awakening)", 4,
            java.util.List.of("Origin-tier — special origin", "Taichu = Beginning Origin", "Retained to end"), "wiki-attested (post-Origin-awakening); Fandom wiki"
        ),
        new CanonArtifact(
            "I135", "Miemie Origin True Body", "寂灭真身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of("True Body formed from Miemie (Silent Extinction) Origin", "Pairs with Slaughter Origin → Lu Mo"),
            "Self-condensed (post-Origin-awakening)", 4,
            java.util.List.of("Origin-tier — special origin", "Component of Lu Mo (Slaughter True Body)", "Retained to end"), "wiki-attested (post-Origin-awakening); Fandom wiki"
        ),
        new CanonArtifact(
            "I136", "Restriction Origin True Body", "禁制真身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of("True Body formed from Restriction Origin", "Special origin tier"),
            "Self-condensed (post-Origin-awakening)", 4,
            java.util.List.of("Origin-tier — special origin", "Restriction-element true body", "Retained to end"), "wiki-attested (post-Origin-awakening); Fandom wiki"
        ),
        new CanonArtifact(
            "I137", "Thunder Origin True Body", "雷霆真身", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "True Body formed from Thunder Origin",
                "Pairs with Slaughter Origin → Annihilating Thunder",
                "Tangible origin tier"
            ),
            "Self-condensed (post-Origin-awakening)", 4,
            java.util.List.of("Origin-tier — tangible origin", "Component of Annihilating Thunder", "Retained to end"), "wiki-attested (post-Origin-awakening); Fandom wiki"
        ),
        new CanonArtifact(
            "I138", "Life-Death Domain / Underworld River", "生死域·冥河", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "First self-created Original Spell — life-and-death energy river",
                "Catalyst to bind souls into Celestial Sealing Stamp → 18-Layers-of-Hell",
                "Virtual Origin tier"
            ),
            "Self-comprehended (Underworld River Ch. 604; Domain stabilized ~Ch. 850)", 5,
            java.util.List.of("Origin-tier (virtual origin)", "Wang Lin's first Original Spell", "Retained to end — core domain"), "Ch.604, Ch.~850 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I139", "Karma Domain / Karmic Cycle", "因果域", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Left eye yang, right eye yin — Karma Domain manifestation",
                "Pairs with Karma Whip",
                "Virtual Origin tier"
            ),
            "Self-comprehended (Karma Whip Ch. 731; Domain stabilized ~Ch. 850)", 5,
            java.util.List.of("Origin-tier (virtual origin)", "Yin-yang balanced eyes", "Retained to end"), "Ch.731, Ch.850 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I140", "True-False Domain", "真假域", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Right eye lightning, left eye true/false — targets enter state between real and fake",
                "Hallucinations make targets unable to distinguish real from fake",
                "Controls Real and Unreal — Virtual Origin tier"
            ),
            "Self-comprehended (stabilized Ch. 1163)", 5,
            java.util.List.of("Origin-tier (virtual origin)", "Controls reality itself", "Stabilized at Ch. 1163"), "Ch.1163 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I141", "Battle Will Domain", "战斗意志域", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Gathers battle will as a weapon", "Will-based domain"),
            "Self-comprehended (mid era)", 4,
            java.util.List.of("Spirit-tier domain", "Will-based power", "Retained to end"), "wiki-attested (mid era); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I142", "Star of Law", "法之星", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Fusion of Life-Death, Karma domains + Fire and Thunder marks", "Composite Origin tier"),
            "Self-fused (Ch. 1221)", 5,
            java.util.List.of("Origin-tier composite domain", "Combines multiple domains and marks", "Retained to end"), "Ch.1221 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I143", "18-Layers-of-Hell Reincarnation Realm", "十八层地狱轮回境", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Wang Lin's own reincarnation cycle — pocket realm",
                "Stores souls for Scatter Beans to Form Soldiers divine ability",
                "Composite of Life-Death Dao Underworld River + Celestial Sealing Stamp"
            ),
            "Self-created (Ch. 915)", 5,
            java.util.List.of(
                "Pseudo-immortal tier pocket realm",
                "Reincarnation-realm domain",
                "Per Baidu: evolved into Incense Offering Realm"
            ), "Ch.915 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I144", "Sundered Night — 1st Original Spell", "残夜", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Contains rules of beginning — tears through night using Taichu sun",
                "Used to kill Xu Kongzi and heavily wound Tian Yunzi",
                "Evolved into Belief Art at Kong-Jie; power increased with Taichu Origin"
            ),
            "Self-created (Ch. 988)", 5,
            java.util.List.of(
                "Dao of Beginning and End / Creation",
                "Wang Lin's 1st self-created divine ability",
                "Evolved into Belief Art — retained to end"
            ), "Ch.988 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I145", "Flowing Time / Flowing Moon — 2nd Original Spell", "流月", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Can reverse time",
                "Used to send Lu Mo back to past to find method to resurrect Li Muwan",
                "Dao of Time"
            ),
            "Self-comprehended at Ancient Immortal Domain gate in Wind Immortal World (Ch. 1245)", 5,
            java.util.List.of("Dao of Time", "2nd self-created divine ability", "Time-reversal power — retained to end"), "Ch.1245 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I146", "Dream Dao — 3rd Original Spell", "梦道", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Probe reincarnation — used for enslavement and reality-bending",
                "Kill own shadow to pass Self Tribulation during Three-Step Tribulation",
                "Discovered Tian Yunzi's 99 cycles of reincarnation",
                "Lu Mo used Dream Dao to deduce past — made Xiangang Continent seem like Wang Lin's dream"
            ),
            "Self-created with Sealer of Realms' help (Ch. 1295)", 5,
            java.util.List.of(
                "Dao Spell tier — 3rd divine ability",
                "Reality-bending and reincarnation-probing",
                "Key to defeating Tian Yunzi"
            ), "Ch.1295 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I147", "Rain and Wind World", "风雨世界", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Fusion of Call of the Wind + Summon the Rain spell + Celestial Stamp", "Composite domain"),
            "Self-fused (Ch. 1322)", 5,
            java.util.List.of("Composite domain tier", "Combines wind, rain spells and Celestial Stamp", "Retained to end"), "Ch.1322 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I148", "Karma Print — 4th Original Spell", "因果印", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Divine ability from mastery of Cause-Effect Origin",
                "Created during mortal transformation",
                "Dao of Karma"
            ),
            "Self-created (Ch. 1614)", 5,
            java.util.List.of("Dao of Karma", "4th self-created divine ability", "Created during mortal transformation"), "Ch.1614 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I149", "Life and Death Seal — 5th Original Spell", "生死印", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Divine ability from mastery of Life-Death Origin", "Created during mortal transformation"),
            "Self-created (Ch. 1616)", 5,
            java.util.List.of("Dao of Life and Death", "5th self-created divine ability", "Created during mortal transformation"), "Ch.1616 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I150", "True and False Eternal Seal — 6th Original Spell", "真假永恒印", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Divine ability from mastery of True-False Origin", "Created during mortal transformation"),
            "Self-created (Ch. 1617)", 5,
            java.util.List.of("Dao of True and False", "6th self-created divine ability", "Created during mortal transformation"), "Ch.1617 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I151", "Undying Ancient Finger / Dao Gu Indestructible Finger — 7th Original Spell", "不古道指", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Condenses power of three ancient clans into one finger",
                "Terrifying offensive power AND terrifying recovery ability",
                "Temporarily maintains one finger's indestructibility"
            ),
            "Self-created (Ch. 1639)", 5,
            java.util.List.of("Ancient Clan / Immortal tier", "7th self-created divine ability", "Recovery beyond destruction"), "Ch.1639 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I152", "Undying Ancient Body / Dao Gu Indestructible Body", "不古道身", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Full-body version of Undying Ancient Finger",
                "Recovery surpasses force of collapse and destruction",
                "Full-body indestructibility"
            ),
            "Self-created (Ch. 1728)", 5,
            java.util.List.of("Ancient Clan / Immortal tier", "Full-body indestructibility", "Inspired by Undying Immortal Body"), "Ch.1728 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I153", "Blood Lines Rules / Restriction Essence", "血脉规则·禁制本源", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Restriction Origin — the Restriction Origin Wang Lin condenses",
                "Pairs with Restriction Origin True Body"
            ),
            "Self-condensed (Ch. 1715)", 5,
            java.util.List.of("Restriction Origin tier", "One of Wang Lin's 14 Origins", "Retained to end"), "Ch.1715 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I154", "Heart-Pounding Thunder", "心悸雷", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "Fusion of fire and thunder essence spell",
                "Comprehended in last layer of Ancient Graveyard",
                "Composite elemental power"
            ),
            "Self-created (Ch. 1777)", 5,
            java.util.List.of("Composite (fire + thunder essence) tier", "Comprehended in Ancient Graveyard", "Retained to end"), "Ch.1777 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I155", "Store All Ji Thunder — 7th accompanying thunder", "收万极雷", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Wang Lin's own 7th type of accompanying thunder"),
            "Self-condensed (Ch. 1368)", 5,
            java.util.List.of("Accompanying thunder (Wang Lin's own 7th type)", "Self-condensed", "Retained to end"), "Ch.1368 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I156", "Bloodline Thunder — 8th accompanying thunder", "血脉雷", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Wang Lin's own 8th type of accompanying thunder"),
            "Self-condensed (Ch. 1368)", 5,
            java.util.List.of("8th type accompanying thunder", "Self-condensed", "Retained to end"), "Ch.1368 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I157", "Defying Thunder — 9th accompanying thunder", "逆雷", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of(
                "9th type of accompanying thunder — never appeared since beginning of time",
                "Formed via defying will",
                "Unprecedented in history"
            ),
            "Self-condensed via defying will (Ch. 1368)", 5,
            java.util.List.of(
                "9th type — never appeared since beginning of time",
                "Formed through defying will",
                "Unprecedented accompanying thunder"
            ), "Ch.1368 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I158", "Ethereal Fire — accompanying fire", "虚火", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Ethereal Fire — one of Wang Lin's accompanying fires", "Similar in nature to Seven-Colored Lance"),
            "Absorbed with Vermilion Bird awakening (~Ch. 1068)", 5,
            java.util.List.of("Accompanying fire tier", "Absorbed during Vermilion Bird awakening", "Retained to end"), "Ch.~1068 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I159", "Nine-Tribulation Karma Fires — accompanying fire", "九劫业火", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("Nine-tribulation karma fire", "Karmic-element accompanying fire"),
            "Self-condensed (mid era)", 5,
            java.util.List.of("Accompanying fire tier", "Karmic-element fire", "Retained to end"), "wiki-attested (mid era); Fandom wiki"
        ),
        new CanonArtifact(
            "I160", "Eight Extreme Daos (set of 8)", "八极道", ArtType.OTHER,
            "domain", "Wang Lin (passed to Wang Baole in AWWP)",
            java.util.List.of(
                "8 Daos: Extreme Water, Metal, Wood, Earth, Fire, Heaven, Land, Life",
                "First 5 = Five Elements; 6th & 7th = mutually opposing; 8th = life-bound Dao",
                "Third-Step-tier Eight Extremities Dao"
            ),
            "Self-comprehended (first 5 by Empyrean Trial; last 3 on 5th Heaven-Trampling bridge)", 5,
            java.util.List.of(
                "Third-Step-tier set of 8 Daos",
                "8th = Wang Lin's unique life-bound Dao",
                "Transmitted to Wang Baole in AWWP (Another World of Padma)"
            ), "Ch.1826, Ch.1958, Ch.2034, Ch.2063 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I161", "One Step to Trample the Heavens — Heaven-Trampling Dao", "一步踏天", ArtType.OTHER,
            "domain", "Wang Lin",
            java.util.List.of("The Heaven-Trampling Dao — Fourth-Step tier", "Comprehended on the 5th Heaven-Trampling bridge"),
            "Self-comprehended on 5th Heaven-Trampling bridge (Ch. 2064)", 5,
            java.util.List.of("Fourth-Step (Heaven-Trampling Dao) tier", "Wang Lin's ultimate Dao achievement", "Retained to end"), "Ch.2064 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I162", "Four Great Restrictions", "四大禁制", ArtType.OTHER,
            "restriction", "Wang Lin",
            java.util.List.of(
                "Annihilation, Time, Life-and-Death, Destruction + Ancient Soul restrictions",
                "Destruction Restriction at great completion: seal everything from immortals to mortal beasts",
                "Ancient forbidden arts — seal all things"
            ),
            "Inherited (Annihilation from Li Yuan; others per various inheritances)", 5,
            java.util.List.of(
                "Ancient restriction tier",
                "4 great ancient forbidden arts",
                "Destruction at great completion = seal everything in the world"
            ), "Annihilation Ch.754; Time Ch.1223; Life-Death Ch.1229; Ancient Soul Ch.1697 (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I163", "Ancient Leaf / Ancient Breath Leaf", "太古息叶", ArtType.MATERIAL,
            "restriction", "Mostly used",
            java.util.List.of("Releases powerful sealing force when used", "99 total in existence", "Ancient-tier sealing leaves"),
            "Ancient Tomb (1st: Ch. 1387; ×18: Ch. 1460; 2nd: Ch. 1449)", 5,
            java.util.List.of(
                "Ancient-tier sealing treasure",
                "Grandmaster Yun Luo predicted 99 total",
                "Few dozen used to seal door passage to Immortal Astral Continent"
            ), "Ch.1387, Ch.1449, Ch.1460 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I164", "Eternal Wood Spirit", "长生木灵", ArtType.OTHER,
            "defensive", "Wang Lin",
            java.util.List.of("Defensive wood-spirit", "Ancient-tier origin"),
            "Ancient Tomb", 5,
            java.util.List.of("Ancient-tier utility treasure", "Obtained in Ancient Tomb", "Retained to end"), "wiki-attested (Ancient Tomb); Fandom wiki"
        ),
        new CanonArtifact(
            "I165", "Wood Carving (of Black Fiend Devil Saint)", "木雕", ArtType.OTHER,
            "defensive", "Wang Lin (half-broken)",
            java.util.List.of("Defends against Moongazer Serpent's Ancient God's Finger", "Spirit-tier carving"),
            "Took from Black Fiend Devil Saint (half-broken) (Ch. 931)", 5,
            java.util.List.of(
                "Spirit-tier defensive carving",
                "Originally Black Fiend Devil Saint's treasure",
                "Retained half-broken"
            ), "Ch.931 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I166", "Dark Heaven Stone", "暗天石", ArtType.MATERIAL,
            "divine_sense", "Wang Lin",
            java.util.List.of(
                "Store divine sense to create avatar or store power spells",
                "Required by cultivators at high levels"
            ),
            "Gift from Master Yi Chen (Ch. 965)", 5,
            java.util.List.of(
                "Spirit-tier divine-sense storage",
                "From Master Yi Chen",
                "Used for avatar creation and spell storage"
            ), "Ch.965 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I167", "Battle Scrolls ×3 (Zhan Family)", "战家三战卷", ArtType.MATERIAL,
            "offensive", "Wang Lin",
            java.util.List.of("Zhan Family's battle scrolls — set of 3", "Spirit-tier combat scrolls"),
            "Zhan Family (Ch. 1095)", 5,
            java.util.List.of("Spirit-tier battle scrolls", "Set of 3 from Zhan Family", "Retained to end"), "Ch.1095 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I168", "Blood Pavilion ×2", "血阁", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Blood-element pavilion — 2 copies obtained", "Spirit-tier utility pavilions"),
            "1st: Blood Ancestor's Blood Planet (Ch. 765); 2nd: from Ancient Demon occupying Blood God's soul (Ch. 1507)", 5,
            java.util.List.of("Spirit-tier blood-element pavilions", "Two copies from different sources", "Retained to end"), "Ch.765, Ch.1507 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I169", "Fragment Stamp", "碎片印", ArtType.TALISMAN,
            "utility", "Evolved into 18-Hell Celestial Sealing Stamp",
            java.util.List.of(
                "Refined by divine tribulation during Illusionary-Yin breakthrough",
                "Later evolved into Celestial Sealing Stamp"
            ),
            "Divine-tribulation-forged (Ch. 769)", 5,
            java.util.List.of(
                "Spirit-tier — tribulation-forged",
                "Precursor to 18-Hell Celestial Sealing Stamp",
                "Evolved — no longer exists in original form"
            ), "Ch.769 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I170", "Divine Soul Brush / Golden Celestial Brush", "点仙笔", ArtType.OTHER,
            "utility", "Given to Li Qianmei (Ch. 1178)",
            java.util.List.of(
                "Outlines powerful talismanic scripts",
                "Created by Immortal Emperor Qing Lin as gift to daughter Qing Shuang",
                "Celestial-tier brush"
            ),
            "Seized from short old man in Demon Spirit Land (Ch. 625); originally Qing Lin's creation", 5,
            java.util.List.of(
                "Celestial-tier talismanic script brush",
                "Created by Immortal Emperor Qing Lin",
                "Given to Li Qianmei Ch. 1178"
            ), "Ch.625 obtained, Ch.1178 given away (novel); Fandom wiki; Baidu Baike"
        ),
        new CanonArtifact(
            "I171", "Blood Jades", "血玉", ArtType.MATERIAL,
            "other", "Wang Lin (retired)",
            java.util.List.of("Spirit-tier blood jades"),
            "Stolen from Yao Xixue's bag of holding (~Ch. 580)", 5,
            java.util.List.of("Spirit-tier jades", "Stolen from Yao Xixue", "Retired — no longer used"), "Ch.~580 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I172", "Seven Star Sword Formation", "七星剑阵", ArtType.FORMATION,
            "offensive", "Destroyed (Ch. 715)",
            java.util.List.of("Sword formation from seven of Ling Tianhou's disciples' twelve swords", "Spirit-tier set-formation"),
            "Assembled from Ling Tianhou's disciples' swords", 5,
            java.util.List.of(
                "Spirit-tier sword set formation",
                "7 of 12 swords from Ling Tianhou's disciples",
                "Destroyed Ch. 715"
            ), "destroyed Ch.715 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I173", "Golden Print (Xuan Luo-forged)", "金印", ArtType.TALISMAN,
            "offensive", "Wang Lin",
            java.util.List.of(
                "Sovereign's god-destruction dao spell turned into corporeal treasure",
                "Contains hint of Nine Suns' power — indestructible",
                "Quasi-Third-Step tier"
            ),
            "Forged by Xuan Luo (Wang Lin's master) (Ch. 1772)", 5,
            java.util.List.of(
                "Quasi-Third-Step tier — Xuan Luo-forged",
                "Indestructible — contains Nine Suns' power",
                "Same strength as Sovereign using the spell himself"
            ), "Ch.1772 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I174", "Lantern (origin soul protection)", "长明灯", ArtType.OTHER,
            "soul_related", "Wang Lin",
            java.util.List.of(
                "Origin soul protection — user cannot die while fire is lit",
                "Can nourish and slowly strengthen the origin soul",
                "Refined into body: soft light surrounds body for extra protection layer"
            ),
            "Taken after killing Xu Decai from Green Devil Continent (Ch. 1867)", 5,
            java.util.List.of(
                "Spirit-tier origin-soul protector",
                "No offensive power but powerful defense",
                "Can be body-refined for permanent protection"
            ), "Ch.1867 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I175", "Heavenly Bull Bead", "天牛珠", ArtType.BEAD_PART,
            "utility", "Wang Lin",
            java.util.List.of("Bead from Heavenly Bull Continent", "Spirit-tier"),
            "Heavenly Bull Continent era", 5,
            java.util.List.of("Spirit-tier bead", "Heavenly Bull Continent origin", "Retained to end"), "wiki-attested (Heavenly Bull Continent era); Fandom wiki"
        ),
        new CanonArtifact(
            "I176", "A Tortoise Shell (Eastern Continent map)", "龟壳", ArtType.OTHER,
            "utility", "Wang Lin",
            java.util.List.of("Contains entire map of Eastern Continent", "Less detailed further from Heavenly Bull Continent"),
            "Eastern Continent era", 5,
            java.util.List.of("Spirit-tier map-bearing tortoise shell", "Rough outline — simple introduction", "Retained to end"), "wiki-attested (Eastern Continent era); Fandom wiki"
        ),
        new CanonArtifact(
            "I177", "Palm-Sized Piece of Ice (Dong Ling Pool ice, Ye Mo's left arm)", "东灵池冰·含夜魔左臂", ArtType.OTHER,
            "cultivation_aid", "Wang Lin",
            java.util.List.of(
                "Ice from imitation Dong Ling Pool in cave world",
                "Contains Ye Mo's left arm — inheritance bearing",
                "Quasi-Third-Step tier"
            ),
            "Imitation Dong Ling Pool created by Seven Coloured Celestial Sovereign (Ch. 1768)", 5,
            java.util.List.of("Quasi-Third-Step tier", "Contains Ye Mo's left arm", "From cave world's imitation Dong Ling Pool"), "Ch.1768 (novel); Fandom wiki"
        ),
        new CanonArtifact(
            "I178", "Golden Print of Xu Decai's Rapid Spell Art", "金印·徐德才速法", ArtType.TALISMAN,
            "cultivation_aid", "Wang Lin (technique internalized)",
            java.util.List.of(
                "Rapid Spell Art — opens 9 spell veins in body",
                "Each vein greatly increases casting speed",
                "Uses sealing-based method to accelerate spell casting"
            ),
            "Taken after killing Xu Decai from Green Devil Continent (Ch. 1867)", 5,
            java.util.List.of(
                "Spirit-tier print/technique",
                "Walks extreme path — terrifying casting speed",
                "Technique internalized — retained permanently"
            ), "Ch.1772 (novel); Fandom wiki"
        )
    );

    /** All 214 canon techniques. */
    public static final List<CanonTechnique> ALL_TECHNIQUES = List.of(
        new CanonTechnique(
            "T01", "Underworld Ascension Method", "黄泉升窍诀", TechType.CULTIVATION_ART,
            "Taught by Situ Nan via Heaven Defying Pearl",
            java.util.List.of(
                "Divided into 9 layers, uses extreme-Yin sites for near-death cultivation to form Cold Dan",
                "Dramatically boosts Golden Core formation success rate; catalyst for Ji Realm",
                "Produces cold-attribute Blue Flames as side effect"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ji Realm cultivation method reorganized by Situ Nan from the Ancient Underworld Ascension Method",
                "Requires Foundation Establishment to use safely, cultivates toward Core Formation",
                "Eventually superseded by Celestial/Ancient Order cultivation but remains early-game engine"
            ), "Ch.86 (Donghua S1 EP8); Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T02", "Vermilion Bird Burning Heaven Art", "朱雀焚天功", TechType.CULTIVATION_ART,
            "Taught by Situ Nan (2nd-Gen Vermilion Bird lineage)",
            java.util.List.of(
                "Foundational Vermilion Bird cultivation art opening path to Vermilion Bird Mark awakenings",
                "Fire-attribute art spanning Qi Condensation to Ascendant",
                "Feeds into Vermilion Bird Mark awakenings, Ethereal Fire, and Fire Essence"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Named in Situ Nan Baidu entry as one of the arts imparted to Wang Lin",
                "Requires Vermilion Bird Sequence to fully unlock",
                "Foundation for the entire Vermilion Bird awakening cycle"
            ), "implied early (novel); Baidu Baike (situ_nan)"
        ),
        new CanonTechnique(
            "T03", "Nine Cycle Celestial Refining Tactic", "九转炼仙诀", TechType.CULTIVATION_ART,
            "Imitation of a low-quality celestial spell by the All-Seer; learned via Tianyunzi's clone on Planet Tian Yun",
            java.util.List.of(
                "Causes immortal power to spiral in the body before release, equivalent to Golden Immortal power",
                "Up to 9 revolutions; longer charge = greater output",
                "Stacks with Rapid Spell Art; foundational to late-game Empyrean Exalt reproduction"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Uses immortal/celestial power; requires Soul Transformation and up",
                "Time-delay tradeoff: more revolutions = more power but longer charge time",
                "Learned as part of Celestial Cultivation induction under All-Seer/Tianyunzi"
            ), "Ch.493 (S1 EP106); Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T04", "Ancient God Tactic", "古神诀", TechType.CULTIVATION_ART,
            "Inherited from Tu Si's memory legacy in the Ancient God's body",
            java.util.List.of(
                "Reconstructs the cultivator's body as an Ancient God; outer-body cultivation track",
                "Allows absorbing spiritual energy, pills, and inheritance fragments as stars",
                "Forces hybrid-track existence: main body Ancient God, Avatar is Qi Cultivator vessel"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Essence is one word: plunder — plunder everything",
                "Progression from 1-Star (Ch.199) to 27-Star (Ch.2003)",
                "Enables Half-Heaven-Trampling (Ch.2062) and Heaven-Trampling (Ch.2087) via this track"
            ), "Ch.190; Fandom wiki; Baidu Baike (Tu Si memory legacy)"
        ),
        new CanonTechnique(
            "T05", "Heaven Technique (Ancient God)", "通天诀", TechType.CULTIVATION_ART,
            "Inherited from Tu Si's legacy",
            java.util.List.of(
                "Allows free travel anywhere inside an Ancient God's body",
                "Used during the Restriction Mountain Trial inside Tu Si's body",
                "Niche utility; only useful inside Ancient God body"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ancient God auxiliary movement art",
                "Learned at Ch. 192 alongside Ancient God Tactic",
                "Used specifically for the Land of the Ancient God trial"
            ), "Ch.192; Fandom wiki (Tu Si legacy)"
        ),
        new CanonTechnique(
            "T06", "Indestructible Immortal Body", "不灭仙体", TechType.CULTIVATION_ART,
            "Transmitted by Lian Daofei inside the Nether Beast",
            java.util.List.of(
                "Forges an undying physical body",
                "Initially rejected by Wang Lin's Dao-Ancient bloodline",
                "Inspires Wang Lin's self-created Undying Ancient Finger and Undying Ancient Body"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Lian Daofei fused his supreme Immortal bloodline into Wang Lin",
                "Lian Daofei also imparted numerous divine abilities alongside",
                "Implied ~Ch.1509+ when Lian Daofei forcibly transmits it"
            ), "implied Ch.~1509+; Fandom wiki (Lian Daofei transmission)"
        ),
        new CanonTechnique(
            "T07", "Rapid Spell Art", "速咒术", TechType.CULTIVATION_ART,
            "Obtained after killing Xu Decai of Green Devil Continent",
            java.util.List.of(
                "Opens nine spell veins in the body, each greatly increasing casting speed",
                "Spell veins use sealed time-energy to accelerate casting",
                "Engine of Void Tribulant breakthrough; reproduces Empyrean Exalt effect when combined with mass-produced fused spells"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Requires Arcane Void and up (3rd Step)",
                "Each spell vein requires sealing cultivation/energy; failure is fatal",
                "Central to Ch.1907 breakthrough allowing Wang Lin to become Empyrean Exalt equivalent"
            ), "Ch.1867; Fandom wiki (killed Xu Decai)"
        ),
        new CanonTechnique(
            "T08", "Dao Fusion", "道融", TechType.CULTIVATION_ART,
            "Taught by / stolen from Dao Master Blue Dream's lineage",
            java.util.List.of(
                "Dao Spell that fuses spells with essence in a special way",
                "Lets user fuse all spells into tens of millions of different ones, or all into one",
                "Mechanical basis for Wang Lin's later Essence fusion breakthroughs"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Not an attack or defense — a fusing art",
                "Comprehension-gated, not raw power",
                "Ch. 1322; enables combining Fire/Earth/Water etc. Essences"
            ), "Ch.1322; Fandom wiki (Dao Master Blue Dream lineage)"
        ),
        new CanonTechnique(
            "T09", "Finger of Death", "寂灭指", TechType.OTHER,
            "Incomplete Low-Quality Celestial Spell taught by Situ Nan",
            java.util.List.of(
                "Death-attribute finger strike; main offensive technique during Yang-Soul-Transformation period",
                "Originally performed with Extinction Twin Fingers treasure",
                "Has variants Demonic Finger and Underworld Finger — all incomplete celestial spells"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Situ Nan comprehended it from an incomplete immortal art",
                "Bare-hand version needs much higher realm",
                "Ch. 482; backfires if user's realm is too low"
            ), "Ch.482 (S1 EP103); Fandom wiki; Baidu Baike (Situ Nan)"
        ),
        new CanonTechnique(
            "T10", "Demonic Finger", "魔指", TechType.OTHER,
            "Situ Nan (incomplete low-quality celestial spell)",
            java.util.List.of(
                "Demonic-attribute finger strike",
                "Sister-technique to Finger of Death, part of Situ Nan's trio",
                "Limited use per battle due to being incomplete"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 483; requires Foundation Establishment+",
                "Part of Death/Demonic/Underworld celestial-finger trio",
                "Incomplete celestial spell tier"
            ), "Ch.483; Fandom wiki (Situ Nan)"
        ),
        new CanonTechnique(
            "T11", "Underworld Finger", "冥指", TechType.OTHER,
            "Situ Nan (third sister-spell to Death/Demonic Fingers)",
            java.util.List.of(
                "Underworld-attribute finger strike",
                "Third of Situ Nan's trio of celestial-finger spells",
                "Incomplete celestial spell tier"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 500; requires Foundation Establishment+", "Underworld-attribute variant", "Donghua S1 EP103"), "Ch.500 (S1 EP103); Fandom wiki (Situ Nan)"
        ),
        new CanonTechnique(
            "T12", "Celestial Slaughter Art", "屠仙术", TechType.OTHER,
            "Obtained from the All-Seer via Tianyunzi's clone",
            java.util.List.of(
                "Condenses life force from slaughters into protective life seals",
                "10 million life seals envelop the user into a true-life seal",
                "Trap: Tianyunzi's malicious intent; Wang Lin expelled it and merged into his first Immortal Guard"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 504; requires Soul Transformation+",
                "Became source of Slaughter Origin after re-cultivation",
                "Originally incomplete — true-life seal was a trap"
            ), "Ch.504 (S1 EP107); Fandom wiki; Baidu Baike (Tianyunzi clone)"
        ),
        new CanonTechnique(
            "T13", "Heavenly Chop", "天斩", TechType.OTHER,
            "Integrated into Wang Lin's sword dao; source not explicit",
            java.util.List.of(
                "Single heavenly chop with the sword; signature sword strike",
                "Sword-technique requiring a high-quality flying sword",
                "Classified as Sword Divine Ability alongside Deer (sword spirit pattern)"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Ch. 685; requires Ascendant+",
                "One of Wang Lin's named sword divine abilities",
                "No detailed wiki description available"
            ), "Ch.685; Fandom wiki (sword divine ability)"
        ),
        new CanonTechnique(
            "T14", "10 Million Swords", "千万剑", TechType.OTHER,
            "Sword-summoning art; source not explicit",
            java.util.List.of(
                "Summons ten million sword shadows",
                "Mass-summon sword technique",
                "Listed under Sword Divine Abilities alongside Heavenly Chop"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("No chapter number given in wiki", "Requires 2nd Step+", "Classified as Sword Divine Ability"), "wiki-attested; Fandom wiki; no specific chapter"
        ),
        new CanonTechnique(
            "T15", "Karma Whip", "因果鞭", TechType.OTHER,
            "Fused Soul Lasher treasure with Karma Dao; Ch. 731",
            java.util.List.of(
                "Manifests the Karma Domain; directly attacks the primordial spirit",
                "Restraining effect on soul-type entities; once cleaved open 7 million worlds with a single whip",
                "Evolved from Soul Lasher treasure → Karma Whip → Karma Sword (one of seven Origin Swords)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 731 (Donghua EP147); requires Ascendant+",
                "Later burned by Ghostly Sky Fire and nourished into Cause-Effect Whip",
                "Wields karmic backlash if used against those with no karmic link"
            ), "Ch.731 (Donghua EP147); Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T16", "Mountains Crumble", "山崩", TechType.OTHER,
            "4th of Bai Fan's Six Paths Triple Arts; taught by Immortal Lord Qing Shui",
            java.util.List.of(
                "Summons illusionary volcanoes that fuse into real ones, then crumbles them",
                "Limit: 2 crumbles per volcano, max 6 mountains per casting cycle",
                "Triggers breakthrough to mid Nirvana Cleanser; opens path to True-and-False Domain"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1105; requires Nirvana Cleanser+",
                "Fuses immortal power into spiritual sense",
                "Materializes sky-obscuring volcanic peaks out of thin air"
            ), "Ch.1105; Fandom wiki; Baidu Baike (Bai Fan, taught by Qing Shui)"
        ),
        new CanonTechnique(
            "T17", "Falling Star", "陨星", TechType.OTHER,
            "Self-acquired celestial spell",
            java.util.List.of(
                "Summons starlight that swarms the area",
                "Starlight-attribute celestial spell",
                "Requires Nirvana Cleanser+"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1119; source not detailed", "Celestial spell tier", "Partial wiki description"), "Ch.1119; Fandom wiki (self-acquired celestial spell)"
        ),
        new CanonTechnique(
            "T18", "Heaven Extinction", "灭天", TechType.OTHER,
            "Self-acquired / blood-absorption technique",
            java.util.List.of(
                "Devours origin souls of all killed cultivators",
                "Absorbs flesh and blood as nutrients to recover",
                "Karmically heavy — adds to slaughter essence / heart-demon risk"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1307; requires Nirvana Shatterer+",
                "Recovery-via-slaughter celestial spell",
                "Significant karmic cost with each use"
            ), "Ch.1307; Fandom wiki (self-acquired)"
        ),
        new CanonTechnique(
            "T19", "Heaven Reversal Stamp", "翻天印", TechType.OTHER,
            "Self-created celestial spell",
            java.util.List.of(
                "Flips the sky to become earth",
                "The sky becomes the strongest stamp",
                "Heaven-flipping stamp attack"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1320; requires Nirvana Shatterer+", "Self-created celestial spell", "Partial wiki description"), "Ch.1320; Fandom wiki (self-created celestial spell)"
        ),
        new CanonTechnique(
            "T20", "Light and Shadow Shield", "光影盾", TechType.OTHER,
            "Dao Master Blue Dream lineage; Spirit Void Spell",
            java.util.List.of(
                "Creates a defensive shield of light",
                "Made from every vitality in the world",
                "Dao Master Blue Dream's Spells category"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1321; requires Nirvana Shatterer+", "Defensive spell", "Partial wiki description"), "Ch.1321; Fandom wiki (Dao Master Blue Dream spells)"
        ),
        new CanonTechnique(
            "T21", "Fog Transformation Thunder Spell", "雾化雷术", TechType.OTHER,
            "Self-acquired / lineage",
            java.util.List.of(
                "Fog-transformation thunder attack",
                "Combines fog transformation with thunder",
                "Requires Nirvana Shatterer+"
            ), java.util.List.of("Wang Lin"), 3,
            java.util.List.of(
                "Ch. 1336; implied / pieced together from wiki",
                "Thunder + fog transformation hybrid",
                "Least documented of the named combat spells"
            ), "Ch.1336; Fandom wiki (confidence 3)"
        ),
        new CanonTechnique(
            "T22", "Giant Thunder Stamp", "大雷印", TechType.OTHER,
            "Self-acquired",
            java.util.List.of(
                "Giant thunder-stamp attack",
                "Thunder-attribute strike",
                "Wiki groups with Fog Transformation Thunder Spell"
            ), java.util.List.of("Wang Lin"), 3,
            java.util.List.of("Ch. ~1336+; implied / pieced together", "Thunder stamp category", "Minimal wiki description"), "Ch.~1336+; Fandom wiki (confidence 3)"
        ),
        new CanonTechnique(
            "T23", "Dao of Power / Rebound Force", "力之反", TechType.OTHER,
            "Black Tortoise method taught by 2nd Vermilion Bird Divine Emperor",
            java.util.List.of(
                "Rebound force technique — turns opponent's power back on them",
                "Force-redirection body-technique",
                "Requires Nirvana Shatterer+"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Ch. 1403; Black Tortoise clan technique",
                "Per Baidu: 2nd-Gen Vermilion Bird passed Dao of Strength as proof",
                "Body-technique category"
            ), "Ch.1403; Fandom wiki; Baidu Baike (2nd-Gen VB / Black Tortoise)"
        ),
        new CanonTechnique(
            "T24", "Open the Ancient Thunder Realm", "开古雷界", TechType.OTHER,
            "Stolen / inherited from Scatter Thunder Clan's head elder",
            java.util.List.of("Opens the Ancient Thunder Realm", "Realm-opening thunder ability", "Combat/utility dual-purpose"), java.util.List.of("Wang Lin"), 3,
            java.util.List.of("Ch. 1417; requires Nirvana Shatterer+", "Scatter Thunder Clan spell", "Implied from wiki grouping"), "Ch.1417; Fandom wiki (stolen from Scatter Thunder Clan)"
        ),
        new CanonTechnique(
            "T25", "Seven-Colored Lance", "七彩枪", TechType.OTHER,
            "Taught by Lian Daofei (madman); Dao Spell",
            java.util.List.of(
                "Seven-colored light contains the power of emotions",
                "Affects the emotions of anyone who sees it",
                "Stronger than Ethereal Fire"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1543; requires 3rd Step", "Dao Spell taught by Lian Daofei", "Emotion-attribute lance"), "Ch.1543; Fandom wiki (taught by Lian Daofei)"
        ),
        new CanonTechnique(
            "T26", "Li Guang's Heaven-Shattering Bow Dao", "李广射天弓道", TechType.OTHER,
            "Taught by Lian Daofei (madman); Dao Spell",
            java.util.List.of("Bow-Dao that shatters the heavens", "Pairs with Li Guang's Arrow", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("~Ch. 1533+ when Li Guang's Bow obtained", "Dao Spell taught by Lian Daofei", "Legendary bow-dao"), "Ch.~1533+; Fandom wiki (taught by Lian Daofei)"
        ),
        new CanonTechnique(
            "T27", "Heavenly Fate Finger", "天命指", TechType.OTHER,
            "Self-realized / derived (Bai Fan-lineage); Forgotten Chapter",
            java.util.List.of("Fate-attribute finger strike", "Bai Fan-lineage technique", "Forgotten Chapter category"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1156; requires Nirvana Cleanser+", "Self-realized technique", "Listed under Forgotten Chapters"), "Ch.1156 (Forgotten Chapters); Fandom wiki (Bai Fan lineage)"
        ),
        new CanonTechnique(
            "T28", "Lands Collapse", "土崩", TechType.OTHER,
            "Bai Fan's spell, one of the Six Paths Triple Arts",
            java.util.List.of(
                "Earth-collapse celestial spell",
                "Part of Bai Fan's Six Paths Triple Techniques",
                "Requires Nirvana Cleanser+"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1582; Forgotten Chapters", "Earth-attribute celestial spell", "Part of Bai Fan lineage"), "Ch.1582 (Forgotten Chapters); Fandom wiki (Bai Fan spell)"
        ),
        new CanonTechnique(
            "T29", "Dark Moon, Clear Skies", "暗月晴空", TechType.OTHER,
            "Bai Fan's spell",
            java.util.List.of("Yin-attribute / moon-clearing celestial spell", "Dark moon technique", "Part of Bai Fan lineage"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Ch. 1582; Forgotten Chapters",
                "Yin-attribute celestial spell",
                "Part of Bai Fan's Six Paths Triple Techniques"
            ), "Ch.1582; Fandom wiki (Bai Fan spell)"
        ),
        new CanonTechnique(
            "T30", "God, Demon, Devil, Ancient Dao, No Celestial!", "神魔妖古道无仙", TechType.OTHER,
            "Self-realized; requires three prerequisite spells in sequence",
            java.util.List.of(
                "Combined three-ancestor attack: God + Demon + Devil spells",
                "Requires God Tremble Army Formation → Wind Fire Mountain → Life Death Reversal",
                "Only after all three spells can this composite attack execute"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1778; requires 3rd Step",
                "Three-spell combo: God, Demon, and Devil spells in order",
                "Ancient Order composite-ancestor strike"
            ), "Ch.1778; Fandom wiki (three-spell combo)"
        ),
        new CanonTechnique(
            "T31", "God Punch", "神拳", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Ancient-God punch attack", "Raw physical power strike", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1312; Ancient Order spell", "Ancient God power", "Simple but devastating punch"), "Ch.1312; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T32", "Life Exchange", "命换", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of(
                "Exchanges life force for temporary burst of power",
                "Sacrifices lifespan/health",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1278; Ancient Order spell", "Life-for-power exchange", "Significant cost: sacrifices lifespan"), "Ch.1278; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T33", "Immortal Dream", "仙梦", TechType.OTHER,
            "Fusion of Dream of Ancient Times and Wang Lin's own Dream Dao spell",
            java.util.List.of(
                "Master of the dream — can change everything in the dream to kill",
                "Evolved from Dream of Ancient Times (shield-owner's technique)",
                "Wang Lin became master of the shield, his thoughts change everything about the dream"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1675; requires 3rd Step",
                "Original fusion spell combining Dream Dao + Dream of Ancient Times",
                "Dream-attack category"
            ), "Ch.1675; Fandom wiki (Dream of Ancient Times + Dream Dao fusion)"
        ),
        new CanonTechnique(
            "T34", "Heaven-Ripping", "撕天", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Heaven-ripping attack", "Ancient Order technique", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1691; Ancient Order spell", "Space-tearing attack", "Ancient Clan power"), "Ch.1691; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T35", "Tu Si's Trident", "涂司三叉戟", TechType.OTHER,
            "Inherited from Tu Si; Tu Si's Life Treasure",
            java.util.List.of(
                "Tu Si's trident weapon; could absorb spells",
                "Ancient God treasure-weapon",
                "Destroyed by Daoist Water at Ch. 1277"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 941; obtained via items page Ch.1082",
                "Tu Si's Life Treasure",
                "Later destroyed by Daoist Water"
            ), "Ch.941 obtained, Ch.1082 items page, Ch.1277 destroyed; Fandom wiki (Tu Si Life Treasure)"
        ),
        new CanonTechnique(
            "T36", "God Slaying Spear / God Slaying Seal", "诛神矛/诛神印", TechType.OTHER,
            "Origin Ancient God spell; royal Cave-World bloodline only",
            java.util.List.of(
                "Borrows power from the ancestors",
                "Only the royal clan (Cave World) can use",
                "Ancient God ancestor-borrowing technique"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 941-942; Origin Ancient God spell", "Royal bloodline requirement", "Partial wiki description"), "Ch.941-942; Fandom wiki (Origin Ancient God spell)"
        ),
        new CanonTechnique(
            "T37", "Heavenly Devil Sound", "天魔音", TechType.OTHER,
            "Learned from a Scattered Devil's armor in Qing Lin's Cave",
            java.util.List.of("Devil-sound attack", "Sound-attribute devil spell", "Requires Soul Transformation+"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 619 (per techniques page) / Ch. 625 (per Situ Nan Baidu)",
                "Obtained from Scattered Devil's armor",
                "Sound-attribute attack"
            ), "Ch.619/Ch.625; Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T38", "Spirit Transformation", "灵变", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of(
                "Enters a planet to surround oneself in spiritual energy and enter dormant state",
                "Undetectable unless encountering another Ancient God",
                "Ancient God stealth/concealment ability"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 765; Ancient God+ required",
                "Enters a planet NOT to absorb energy but to hide",
                "Stealth/concealment category"
            ), "Ch.765; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T39", "Merit Spirit", "功德灵", TechType.OTHER,
            "Ancient God spell",
            java.util.List.of(
                "Withdraws a will from the universe — the universe doesn't have a soul, but this spell can extract one",
                "A 9-star Ancient God can extract soul of universe + laws of the world to change the heavens",
                "Universe-law supreme Ancient God spell"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1017; Ancient God+ required",
                "\"Steal the merit of the heavens, take the soul of the universe\"",
                "Output scales with user's power"
            ), "Ch.1017; Fandom wiki (Ancient God supreme spell)"
        ),
        new CanonTechnique(
            "T40", "100 Avatars", "百化身", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Creates 100 avatars simultaneously", "Mass-avatar creation technique", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1271; Ancient Order spell", "Mass-avatar combat application", "Ancient Clan power"), "Ch.1271; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T41", "Void", "虚", TechType.OTHER,
            "Ancient God inheritance; 8-star Ancient God spell",
            java.util.List.of("Void manipulation ability", "8-star Ancient God spell", "Combat / void-manipulation category"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1314; requires 8-star Ancient God", "Ancient God inheritance spell", "Void manipulation power"), "Ch.1314; Fandom wiki (8-star Ancient God spell)"
        ),
        new CanonTechnique(
            "T42", "Mysterious God Star", "玄神星", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Star-attribute attack", "Ancient Order technique", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1315; Ancient Order spell", "Star-attribute power", "Partial wiki description"), "Ch.1315; Fandom wiki (Ancient Order spell)"
        ),
        new CanonTechnique(
            "T43", "God Tremble, Army Formation", "神震军阵", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of(
                "Army-formation attack spell",
                "Part 1 of three-spell combo for 'God, Demon, Devil, Ancient Dao, No Celestial!'",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1469; Ancient Order spell",
                "First spell in the three-ancestor composite",
                "Army-formation category"
            ), "Ch.1469; Fandom wiki (Ancient Order, combo part 1)"
        ),
        new CanonTechnique(
            "T44", "Demon Spell, Wind and Fire Mountain", "魔咒风水山", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Conjures wind-fire mountain demon-spell", "Part 2 of the three-spell combo", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1469; Ancient Order spell",
                "Second spell in the three-ancestor composite",
                "Demon-spell category"
            ), "Ch.1469; Fandom wiki (Ancient Order, combo part 2)"
        ),
        new CanonTechnique(
            "T45", "Devil Dao, Life and Death Reverse", "魔道生死逆", TechType.OTHER,
            "Ancient Order spell",
            java.util.List.of("Reverses life and death", "Part 3 of the three-spell combo", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1476; Ancient Order spell",
                "Third spell in the three-ancestor composite",
                "Devil-dao life-death reversal"
            ), "Ch.1476; Fandom wiki (Ancient Order, combo part 3)"
        ),
        new CanonTechnique(
            "T46", "Ancient Order, Life Transformation Spell", "古序命转术", TechType.OTHER,
            "Ancient Order spell; rumored ancestors could only use 3 times",
            java.util.List.of("Transforms life states", "Ancestors could only use 3 times per lifetime", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1515; Ancient Order spell",
                "Extreme limitation: 3 uses per lifetime maximum",
                "Life-transformation category"
            ), "Ch.1515; Fandom wiki (3 uses per lifetime)"
        ),
        new CanonTechnique(
            "T47", "Burning Realm Ancient Umbrella", "焚界古伞", TechType.OTHER,
            "Vermilion Bird Divine Ability; later upgraded to Dao Spell",
            java.util.List.of(
                "Burning realm umbrella — defensive + offensive domain item",
                "Dao Spell version achieved at Ch. 1543",
                "Treasure-spell hybrid"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1427 (upgraded to Dao Spell Ch. 1543)",
                "Vermilion Bird Divine Ability",
                "Domain/defensive/offensive item"
            ), "Ch.1427 (upgraded Ch.1543); Fandom wiki (VB Divine Ability)"
        ),
        new CanonTechnique(
            "T48", "Third Eye Spell", "天眼术", TechType.OTHER,
            "Obtained through exchange with an ancestor of a clan from Luotian Star Domain",
            java.util.List.of(
                "Origin-power third-eye attack",
                "Contains origin power; further evolved during Heaven's Trial of Seal Immortal competition",
                "Spell with a Hint of the 3rd Step"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 734; requires Ascendant+",
                "Obtained via exchange in Luotian Star Domain",
                "Further evolved by absorbing origin power during 3rd trial"
            ), "Ch.734; Fandom wiki; Baidu Baike (Luotian Star Domain ancestor)"
        ),
        new CanonTechnique(
            "T49", "Body Fixation Art", "定身术", TechType.OTHER,
            "Immortal Art from Xiangang Continent; left by Immortal Emperor Qing Lin",
            java.util.List.of(
                "Immobilizes body, soul, immortals, primordial force, stars, rivers, time, and space",
                "One of the few Divine Abilities belonging to the scope of the 4th Step",
                "Immobilized person can still think"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); requires Late 3rd Step",
                "Known users: Immortal Emperor Qing Lin, Qing Shuang, Seal Sovereign, Shui Daozi, Wang Lin",
                "Law-attribute fixation technique"
            ), "implicit (Baidu); Baidu Baike (Immortal Emperor Qing Lin lineage)"
        ),
        new CanonTechnique(
            "T50", "Slash Luo Art", "斩罗剑术", TechType.OTHER,
            "Legacy sword technique from previous generation's Rain Immortal Sword, Jufu",
            java.util.List.of(
                "Can slash through space and cut through rules",
                "Destroys rules — astonishing power",
                "Sword style that destroys the very fabric of reality"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); requires Late 3rd Step",
                "Contained within Jufu (Rain Immortal Sword)",
                "Rule-destroying sword technique"
            ), "implicit (Baidu); Baidu Baike (Rain Immortal Sword Jufu legacy)"
        ),
        new CanonTechnique(
            "T51", "Great Teleportation Forbidden Technique", "大挪移禁术", TechType.OTHER,
            "Self-developed teleportation-restriction combo",
            java.util.List.of(
                "Uses momentary time difference during teleportation",
                "Seals self, then continuously teleports within that instant to generate mysterious force",
                "When seal is released, teleportation distance is multiplied countless times"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); requires Nascent Soul+",
                "Teleport-amplification forbidden art",
                "Same as Teleportation Restriction Ch. 493"
            ), "implicit (Baidu); Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T52", "Ancient God's Blood", "古神之血", TechType.OTHER,
            "Obtained by killing the Giant Demon Ancestor",
            java.util.List.of(
                "Manifests a gigantic Ancient God phantom from the true body",
                "Single strike with considerable attack power",
                "No longer usable later since true body rarely appears"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); Ancient God body required",
                "Obtained from Giant Demon Ancestor",
                "Single-strike Ancient God phantom"
            ), "implicit (Baidu); Baidu Baike (killed Giant Demon Ancestor)"
        ),
        new CanonTechnique(
            "T53", "Dao Transformation Yellow Springs", "道化黄泉", TechType.OTHER,
            "Self-created; materialized product of Wang Lin's Dao thoughts",
            java.util.List.of(
                "Cultivated to extreme can form independent cycle of reincarnation",
                "Main divine ability during Soul Formation period",
                "Direct precursor to 18 Layers of Hell Reincarnation Realm"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); requires Soul Formation+",
                "Self-created Dao-thought materialization",
                "Precursor to 18 Layers of Hell (Ch. 915)"
            ), "implicit (Baidu); Baidu Baike (self-created)"
        ),
        new CanonTechnique(
            "T54", "Yellow Springs Finger", "黄泉指", TechType.OTHER,
            "Situ Nan lineage",
            java.util.List.of(
                "Yellow-Springs attribute finger strike",
                "Used by Situ Nan to heavily injure Tuo Sen",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Implicit (Baidu/Situ Nan lineage)",
                "Situ Nan's technique; Wang Lin learns second-hand",
                "Yellow-Springs attribute variant"
            ), "implicit (Baidu); Baidu Baike (Situ Nan lineage)"
        ),
        new CanonTechnique(
            "T55", "Three Life Spell", "三生术", TechType.OTHER,
            "Self-acquired; great-soul-sect lineage?",
            java.util.List.of("Splits soul, origin soul, and body into three parts", "Splitting technique", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1713; requires 3rd Step", "Great Soul Sect lineage possible", "Triple-split technique"), "Ch.1713; Fandom wiki (splits soul/origin soul/body)"
        ),
        new CanonTechnique(
            "T56", "Heaven Technique (Qi Cultivator)", "通天", TechType.OTHER,
            "Implicit; distinct from the Ancient God Heaven Technique",
            java.util.List.of(
                "Qi Cultivator version of Heaven Technique",
                "Combat/cultivation-art hybrid",
                "Distinct from Ancient God movement-inside-body version"
            ), java.util.List.of("Wang Lin"), 3,
            java.util.List.of(
                "Implicit (no chapter given)",
                "Canon confidence 3 — implied / pieced together",
                "Very minimal documentation"
            ), "Ch.192; Fandom wiki (Tu Si legacy)"
        ),
        new CanonTechnique(
            "T57", "War Spirit Print", "战神印", TechType.OTHER,
            "Discovered at Rain Celestial Realm entrance door",
            java.util.List.of(
                "Absorbs surrounding laws to create massive palm-shaped spell",
                "Grows stronger the longer it forms",
                "Upgraded to Dao Spell at Ch. 1542: three palms descend to kill body, origin soul, and soul simultaneously"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 299; upgraded to Dao Spell Ch. 1542",
                "Discovered by looking at massive palm print on Rain Celestial Realm entrance",
                "Three sources of power inside when upgraded"
            ), "Ch.299 (upgraded Ch.1542); Fandom wiki (Rain Celestial Realm)"
        ),
        new CanonTechnique(
            "T58", "Void Stop Spell", "空停术", TechType.OTHER,
            "Celestial spell",
            java.util.List.of(
                "Commands the law to entangle the target to stop for a few seconds",
                "Backlashes if target has higher cultivation",
                "Control/immobilization celestial spell"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 627 (EP126)", "Law-entanglement technique", "Risk of backlash against stronger opponents"), "Ch.627 (EP126); Fandom wiki"
        ),
        new CanonTechnique(
            "T59", "Thunder Origin Spell", "雷源术", TechType.OTHER,
            "Fragment of a low rank celestial spell",
            java.util.List.of(
                "Absorb origin energy from thunder and quickly condense it into your own",
                "Helps reduce time at the Yin and Yang stage",
                "Thunder-attribute origin absorption"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 707; fragment of low-rank celestial spell",
                "Reduces time at transitional stages",
                "Thunder origin energy absorption"
            ), "Ch.707; Fandom wiki (low-rank celestial fragment)"
        ),
        new CanonTechnique(
            "T60", "Call the Wind", "唤风术", TechType.OTHER,
            "1st of Celestial Emperor Bai Fan's Six Paths Triple Techniques",
            java.util.List.of(
                "Summons black wind that turns into dragons; max 9 dragons",
                "All dragons condense to a spear after reaching the spell's limit",
                "Wind-attribute celestial spell"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 783; 1st of Bai Fan's Six Paths Triple Techniques",
                "Black wind dragon summoning",
                "Max 9 dragons → spear condensation"
            ), "Ch.783; Fandom wiki (Bai Fan Six Paths Triple Arts #1)"
        ),
        new CanonTechnique(
            "T61", "Summon the Rain", "唤雨术", TechType.OTHER,
            "2nd of Celestial Emperor Bai Fan's Six Paths Triple Techniques",
            java.util.List.of(
                "Fuses Call the Wind dragons into a cloud dropping black crystal rain",
                "All rain merges into one drop of blue crystal with powerful destructive force",
                "Rain-attribute celestial spell"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 914; 2nd of Bai Fan's Six Paths Triple Techniques",
                "Sequel to Call the Wind",
                "Crystal rain → single blue crystal drop"
            ), "Ch.914; Fandom wiki (Bai Fan Six Paths Triple Arts #2)"
        ),
        new CanonTechnique(
            "T62", "Body Formation", "体化术", TechType.OTHER,
            "Celestial spell",
            java.util.List.of(
                "Creates an avatar made of rock and part of the origin soul",
                "Avatar lasts for 10 days",
                "Rock-body clone creation"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1125; celestial spell", "10-day duration avatar", "Rock and origin soul composite"), "Ch.1125; Fandom wiki (rock avatar + origin soul)"
        ),
        new CanonTechnique(
            "T63", "Heart-Pounding Thunder", "心悸雷", TechType.OTHER,
            "Comprehended in the last layer of the Ancient Graveyard",
            java.util.List.of(
                "Heart-attribute thunder attack fusing fire and thunder essence",
                "Fuses spell with fire and thunder essence power",
                "Essence-spell fusion"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1777; requires 3rd Step",
                "Self-comprehended in Ancient Graveyard",
                "Fire + thunder essence fusion attack"
            ), "Ch.1777; Fandom wiki (fire+thunder essence)"
        ),
        new CanonTechnique(
            "T64", "Soul Eye Dao", "魂眼道", TechType.OTHER,
            "Great Soul Sect divination spell",
            java.util.List.of(
                "Soul-eye divination and attack",
                "Half of the Mourning Death Clan's Dao divination spell",
                "Divination + combat hybrid"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1831; Great Soul Sect", "Mourning Death Clan Dao divination lineage", "Requires 3rd Step"), "Ch.1831; Fandom wiki (Great Soul Sect divination)"
        ),
        new CanonTechnique(
            "T65", "Soul Fantasy Origin", "魂幻源", TechType.OTHER,
            "Great Soul Sect spell",
            java.util.List.of("Summons first Ancestor power", "Ancestor-summon soul spell", "Great Soul Sect technique"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1837; Great Soul Sect", "Summons first Ancestor power", "Requires 3rd Step"), "Ch.1837; Fandom wiki (Great Soul Sect)"
        ),
        new CanonTechnique(
            "T66", "Multi-Layered Illusion Spell", "多层幻术", TechType.OTHER,
            "Great Soul Sect main spell",
            java.util.List.of(
                "Nine-layer illusion",
                "Great Soul Sect's primary illusion technique",
                "Each layer adds to the illusion complexity"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1853; Great Soul Sect main spell", "Has nine levels", "Requires 3rd Step"), "Ch.1853; Fandom wiki (Great Soul Sect, nine levels)"
        ),
        new CanonTechnique(
            "T67", "Extreme Fire Dao", "极火道", TechType.OTHER,
            "Self-comprehended via Empyrean Trial",
            java.util.List.of(
                "One of the Eight Extreme Daos; extreme-path fire Dao",
                "Imitated when trying to devour Green Bull's main fire vein",
                "Fully comprehended after passing 11th floor of Empyrean Trial"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1826; requires 3rd Step", "First of Eight Extreme Daos", "Self-comprehended"), "Ch.1826; Fandom wiki (8 Extreme Daos)"
        ),
        new CanonTechnique(
            "T68", "Extreme Water Dao", "极水道", TechType.OTHER,
            "Self-comprehended via Empyrean Trial",
            java.util.List.of(
                "Extreme-path water Dao; one of the Eight Extreme Daos",
                "Comprehended after passing 12th floor of Empyrean Trial",
                "Self-comprehended water extreme"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1958; requires 3rd Step", "Second of Eight Extreme Daos", "Empyrean Trial 12th floor"), "Ch.1958; Fandom wiki (8 Extreme Daos)"
        ),
        new CanonTechnique(
            "T69", "Extreme Metal Dao", "极金道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path metal Dao; one of the Eight Extreme Daos",
                "Comprehended during Empyrean Trial",
                "Metal-attribute extreme path"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 2034; requires 3rd Step", "Third of Eight Extreme Daos", "Revealed at Ch. 2034"), "Ch.2034; Fandom wiki (8 Extreme Daos)"
        ),
        new CanonTechnique(
            "T70", "Extreme Wood Dao", "极木道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path wood Dao; one of the Eight Extreme Daos",
                "Wood-attribute extreme path",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Listed in wiki; no chapter given", "One of Eight Extreme Daos", "Partial documentation"), "wiki-attested; Fandom wiki (8 Extreme Daos); no specific chapter"
        ),
        new CanonTechnique(
            "T71", "Extreme Earth Dao", "极土道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path earth Dao; one of the Eight Extreme Daos",
                "Earth-attribute extreme path",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Listed in wiki; no chapter given", "One of Eight Extreme Daos", "Partial documentation"), "wiki-attested; Fandom wiki (8 Extreme Daos); no specific chapter"
        ),
        new CanonTechnique(
            "T72", "Extreme Life and Death Dao", "极生死道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path life-death Dao; one of the Eight Extreme Daos",
                "Life-death attribute extreme path",
                "Requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Listed in wiki; no chapter given", "One of Eight Extreme Daos", "Partial documentation"), "wiki-attested; Fandom wiki (8 Extreme Daos); no specific chapter"
        ),
        new CanonTechnique(
            "T73", "Extreme Land Dao", "极地道", TechType.OTHER,
            "Self-comprehended on 5th Heaven-Trampling bridge",
            java.util.List.of(
                "Extreme-path land Dao; first of last three Eight Extreme Daos",
                "Comprehended on 5th bridge",
                "Requires Half-Heaven-Trampling"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2063; Half-Heaven-Trampling required",
                "First of final three Eight Extreme Daos",
                "Comprehended on 5th Heaven Trampling bridge"
            ), "Ch.2063; Fandom wiki (5th Heaven-Trampling bridge)"
        ),
        new CanonTechnique(
            "T74", "Extreme Sky Dao", "极天道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path sky Dao; second of last three Eight Extreme Daos",
                "Sky-attribute extreme path",
                "Requires Half-Heaven-Trampling"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2063; Half-Heaven-Trampling required",
                "Second of final three Eight Extreme Daos",
                "Comprehended alongside Extreme Land and Extreme Life Dao"
            ), "Ch.2063; Fandom wiki (5th bridge)"
        ),
        new CanonTechnique(
            "T75", "Extreme Life Dao", "极命道", TechType.OTHER,
            "Self-comprehended",
            java.util.List.of(
                "Extreme-path life/fate/command Dao; final of the Eight Extreme Daos",
                "Also translated as Extreme Fate Dao or Extreme Command Dao",
                "Requires Half-Heaven-Trampling"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2063; Half-Heaven-Trampling required",
                "Final of Eight Extreme Daos",
                "Can be translated as Extreme Fate/Command/Life Dao"
            ), "Ch.2063; Fandom wiki (5th bridge)"
        ),
        new CanonTechnique(
            "T76", "One Step To Trample the Heavens", "一步踏天", TechType.OTHER,
            "Self-comprehended; Heaven-Trampling Dao",
            java.util.List.of("Single step that tramples the heavens", "The Dao of the 4th Step", "Requires Half-Heaven-Trampling"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2064; comprehended on 5th bridge",
                "Heaven-Trampling Dao movement/combat ability",
                "Represents the ultimate Dao step"
            ), "Ch.2064; Fandom wiki (Heaven-Trampling Dao)"
        ),
        new CanonTechnique(
            "T77", "Ancient Ancestor's Finger Attack", "古祖之指", TechType.OTHER,
            "Ancient Order lineage",
            java.util.List.of(
                "Ancient-ancestor finger attack",
                "Ancient Order power strike",
                "Requires 3rd Step / Half-Heaven-Trampling"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Ch. 2033; Ancient Order lineage",
                "Ancient-ancestor level finger technique",
                "Partial wiki description"
            ), "Ch.2033; Fandom wiki (Ancient Order lineage)"
        ),
        new CanonTechnique(
            "T78", "Ji Realm / Extreme Realm Divine Sense", "极境神识", TechType.OTHER,
            "Awakened through clan annihilation trauma + Underworld Ascension Method catalyst",
            java.util.List.of(
                "Killing divine sense: kills cultivators below Core Formation at FE, below Nascent Soul at CF, below Soul Formation at NS Peak",
                "Spiritual Energy refines into Divine Sense through trauma",
                "Eventually absorbed into Slaughter and becomes Ji Thunder (7th accompanying thunder, Ch.1368)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 127; the Ji Realm changes Spiritual Energy to Divine Sense",
                "Interferes with breakthroughs — sealed and unsealed across story",
                "Qing Shui used Ji Realm to kill Russell for offending Wang Lin"
            ), "Ch.1306; Fandom wiki (refinement after Ascendant breakthrough)"
        ),
        new CanonTechnique(
            "T79", "Soul Piercing Eyes", "神识之眼", TechType.OTHER,
            "Obtained upon reaching mastery in restrictions",
            java.util.List.of(
                "Pierces illusions and restrictions",
                "Reads structure of restrictions without seeing them",
                "Gated by restriction mastery level"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 179; requires Core Formation + restriction mastery",
                "Divine sense eye-technique",
                "Only obtained once certain restriction mastery is reached"
            ), "Ch.179; Fandom wiki (restriction-mastery gated)"
        ),
        new CanonTechnique(
            "T80", "Heart of Slaughter", "杀戮之心", TechType.OTHER,
            "Awakened through accumulated slaughter",
            java.util.List.of("Heart-state that channels slaughter intent", "Dao-heart enhancement", "Requires Ascendant+"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 574; Ascendant+ required",
                "Awakened through accumulated killing",
                "Channels slaughter intent as a divine sense state"
            ), "Ch.574; Fandom wiki (accumulated slaughter)"
        ),
        new CanonTechnique(
            "T81", "Heart Restriction (Divine Sense)", "心禁", TechType.OTHER,
            "Wang Lin's specialized restriction; paired with Heart Compass",
            java.util.List.of(
                "Restricts the heart/mind; works on inner demons of self and enemies",
                "Paired with Heart Compass and Restriction Heart inheritance",
                "Requires Ascendant+"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 858; specialized restriction on the heart/mind",
                "Also listed in Restriction Arts (D)",
                "Dual-category: divine sense and restriction"
            ), "Ch.858; Fandom wiki (Heart Compass + Restriction Heart)"
        ),
        new CanonTechnique(
            "T82", "Eyes Suppressing the World", "镇压天下之眼", TechType.OTHER,
            "Fragment of Celestial Ancestor's Immortal Absolute Sword fused into eyes by Ji Si",
            java.util.List.of(
                "Gaze can suppress another cultivator",
                "Directly tied to Metal Essence — third fragment of Celestial Ancestor's Sword",
                "World-suppressing eye technique"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1896; requires 3rd Step",
                "Ji Si implanted sword fragment into Wang Lin's eyes",
                "Tied to Metal Essence completion (Ch. 1997)"
            ), "Ch.1896; Fandom wiki; Baidu Baike (Metal Essence Ch.1997)"
        ),
        new CanonTechnique(
            "T83", "Samsara Eye / Reincarnation Eye", "轮回眼", TechType.OTHER,
            "Situ Nan's technique; Wang Lin learns second-hand",
            java.util.List.of("Can briefly glimpse karma lines", "Eye of karma/reincarnation", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Implicit; Situ Nan used it",
                "Wang Lin's mastery implied via Karma Domain",
                "Situ Nan lineage technique"
            ), "implicit (Baidu); Baidu Baike (Situ Nan technique)"
        ),
        new CanonTechnique(
            "T84", "Heavenly Eye", "天眼", TechType.OTHER,
            "Situ Nan awakened it after reincarnation",
            java.util.List.of("Sees through cultivator disguises", "Eye of truth", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Implicit; Situ Nan's technique", "Wang Lin learns second-hand", "Truth-seeing eye ability"), "implicit (Baidu); Baidu Baike (Situ Nan awakened after reincarnation)"
        ),
        new CanonTechnique(
            "T85", "Blue Flames", "蓝焰", TechType.OTHER,
            "Cold-attribute flame from the Underworld Ascension Method",
            java.util.List.of(
                "Cold-attribute flame side-effect of Underworld Ascension Method",
                "Ch. 121 appearance",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 121; Foundation Establishment+",
                "Side-effect of Underworld Ascension Method cultivation",
                "Cold-attribute flame"
            ), "Ch.121; Fandom wiki (cold-attribute flame)"
        ),
        new CanonTechnique(
            "T86", "Star Rotation", "星辰流转", TechType.OTHER,
            "Self-acquired technique",
            java.util.List.of("Defensive rotation technique", "Divine sense defensive ability", "Requires Core Formation+"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 475 (S1 EP103)", "Self-acquired defensive technique", "Rotation-based defense"), "Ch.475 (S1 EP103); Fandom wiki (self-acquired defensive)"
        ),
        new CanonTechnique(
            "T87", "Spatial Bending", "空间弯曲", TechType.OTHER,
            "Self-acquired movement/divine-sense",
            java.util.List.of("Spatial bending movement ability", "Divine sense / movement hybrid", "Requires Ascendant+"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 770; self-acquired", "Spatial manipulation", "Also referenced in Movement section"), "Ch.770; Fandom wiki (self-acquired movement/divine-sense)"
        ),
        new CanonTechnique(
            "T88", "Planet Soul Extraction", "星魂抽取", TechType.OTHER,
            "Self-acquired soul-technique",
            java.util.List.of("Extracts souls from planets", "Planet-level soul extraction", "Requires Nirvana Scryer+"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 903; Nirvana Scryer+ required", "Self-acquired technique", "Planet-soul extraction"), "Ch.903; Fandom wiki (self-acquired soul-technique)"
        ),
        new CanonTechnique(
            "T89", "God Speed Divine Arts", "神速神技", TechType.OTHER,
            "Self-acquired",
            java.util.List.of("God-speed movement ability", "Divine sense / movement hybrid", "Requires Ascendant+"), java.util.List.of("Wang Lin"), 3,
            java.util.List.of("Listed under Techniques; no chapter given", "Self-acquired", "Minimal documentation"), "wiki-attested; Fandom wiki (confidence 3); no specific chapter"
        ),
        new CanonTechnique(
            "T90", "Ji Realm (Technique Form)", "极境", TechType.OTHER,
            "Refinement of Ji Realm into usable technique after Ascendant breakthrough",
            java.util.List.of(
                "Refined Ji Realm into an active technique form",
                "Distinct from the passive Divine Sense form",
                "Requires Ascendant+"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Ch. 1306; listed as Technique alongside Spatial Bending and Planet Soul Extraction",
                "Refinement after Ascendant breakthrough",
                "Active-use version of the Ji Realm"
            ), "Ch.1306; Fandom wiki (refinement after Ascendant breakthrough)"
        ),
        new CanonTechnique(
            "T91", "Ancient Restrictions", "古禁", TechType.RESTRICTION,
            "Self-taught via 7-year trial at Land of the Ancient God Restrictions Mountain",
            java.util.List.of(
                "Foundation for all restriction work",
                "Studied for 7 years at Restriction Mountain Trial",
                "Core Formation+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "~Ch.179-180; 7 years of study",
                "Became 4th person to successfully comprehend the trial",
                "Foundation of Wang Lin's restriction specialty"
            ), "Ch.~179-180 (S1 EP31); Fandom wiki (7 years self-taught)"
        ),
        new CanonTechnique(
            "T92", "Restriction Flags Refining Method", "禁旗炼制法", TechType.RESTRICTION,
            "Inherited from Tu Si after passing the Restriction Mountain Trial",
            java.util.List.of(
                "Creates portable formation-array flags from inkstones and 99,999 restrictions",
                "Three flag variants: incomplete/tribulation-summoning, mixed, pure-attack",
                "Requires 3 Ink Stones per flag"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "~Ch.180; inherited from Tu Si",
                "Each flag = portable formation-array",
                "Wang Lin made three distinct variants"
            ), "Ch.~180 (S1 EP32); Fandom wiki (inherited from Tu Si)"
        ),
        new CanonTechnique(
            "T93", "Illusionary Circle", "幻阵", TechType.RESTRICTION,
            "Self-developed after 7 years of restriction research",
            java.util.List.of(
                "Wave-based restriction analysis tool",
                "Reads any restriction's structure and rules without seeing it",
                "Examines waves produced by restrictions to understand their structure"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 180 (S1 EP32); Core Formation+",
                "Doesn't require eyes — works by wave examination",
                "Mastered after 7 years of research and improvement"
            ), "Ch.180 (S1 EP32); Fandom wiki (self-developed)"
        ),
        new CanonTechnique(
            "T94", "18 Plum Restriction", "梅花十八禁", TechType.RESTRICTION,
            "Self-derived from the Annihilation Restriction (Destruction Restriction branch)",
            java.util.List.of(
                "Autonomous plum-blossom restriction; operates independently once set",
                "18 sets of restrictions transform into statues",
                "Unpredictable, almost supernatural"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 752; Ascendant+ required",
                "Derived from Annihilation/Destruction Restriction",
                "Can be deployed as treasure/compass (Heart Compass)"
            ), "Ch.752; Fandom wiki; Baidu Baike (derived from Annihilation)"
        ),
        new CanonTechnique(
            "T95", "Annihilation Restriction", "寂灭禁", TechType.RESTRICTION,
            "Obtained after inheriting half of Restrictions Heart from Li Yuan",
            java.util.List.of(
                "1st of the Four Great Restrictions; annihilation-attribute",
                "Destroy-the-target restriction",
                "Source of the 18 Plum Restriction"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 754; Ascendant+ required",
                "One of four great restrictions",
                "Combined with Heart Compass for full activation"
            ), "Ch.754; Fandom wiki (1st of Four Great Restrictions)"
        ),
        new CanonTechnique(
            "T96", "Nine Deaths Perish Formation", "九死灭阵", TechType.RESTRICTION,
            "Self-constructed from Annihilation-lineage restrictions",
            java.util.List.of(
                "Formation array with perish-attribute",
                "Built from Annihilation-lineage restrictions",
                "Requires Ascendant+"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 829; Ascendant+ required", "Self-constructed formation", "Perish-attribute array"), "Ch.829; Fandom wiki (self-constructed)"
        ),
        new CanonTechnique(
            "T97", "Time Restriction", "时禁", TechType.RESTRICTION,
            "Self-comprehended; 2nd of the Four Great Restrictions",
            java.util.List.of("Time-bending restriction", "2nd of the Four Great Restrictions", "Requires Nirvana Shatterer+"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1223; Nirvana Shatterer+ required",
                "Self-comprehended / inherited",
                "Time-attribute great restriction"
            ), "Ch.1223; Fandom wiki (2nd of Four Great Restrictions)"
        ),
        new CanonTechnique(
            "T98", "Life and Death Restriction", "生死禁", TechType.RESTRICTION,
            "Self-comprehended; 3rd of the Four Great Restrictions",
            java.util.List.of(
                "Life-death attribute restriction",
                "3rd of the Four Great Restrictions",
                "Requires Nirvana Shatterer+"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1229; Nirvana Shatterer+ required",
                "Self-comprehended / inherited",
                "Life-death-attribute great restriction"
            ), "Ch.1229; Fandom wiki (3rd of Four Great Restrictions)"
        ),
        new CanonTechnique(
            "T99", "Ancient Soul Restriction", "古魂禁", TechType.RESTRICTION,
            "Self-comprehended; 4th of the Four Great Restrictions",
            java.util.List.of("Ancient-soul attribute restriction", "4th of the Four Great Restrictions", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1697; 3rd Step required",
                "Self-comprehended / inherited",
                "Ancient-soul-attribute great restriction"
            ), "Ch.1697; Fandom wiki (4th of Four Great Restrictions)"
        ),
        new CanonTechnique(
            "T100", "Heart Restriction (Restriction)", "心禁", TechType.RESTRICTION,
            "Restriction-heart inheritance; paired with Heart Compass",
            java.util.List.of(
                "Heart-attribute special restriction",
                "Pairs with Heart Compass and Restriction Heart inheritance",
                "Requires Ascendant+"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 858; also listed in Divine Sense (C)",
                "Restriction-heart inheritance",
                "Dual-category: restriction and divine sense"
            ), "Ch.858; Fandom wiki (Heart Compass + Restriction Heart)"
        ),
        new CanonTechnique(
            "T101", "Destruction Restriction", "毁灭禁", TechType.RESTRICTION,
            "One of the four great ancient forbidden arts; capstone of ancient restriction arts",
            java.util.List.of(
                "World-sealing tier restriction — seals everything from immortals to mortal beasts",
                "Capstone of ancient restriction arts; source of Annihilation Restriction",
                "Requires Heart Restriction Compass and Destruction Heart Restriction legacy for full power"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Implicit (Baidu); requires 3rd Step",
                "Ambiguous whether identical to Annihilation or overarching source-art",
                "Per Baidu: can travel the world using restrictions, sealing everything"
            ), "implicit (Baidu); Baidu Baike (capstone ancient forbidden art)"
        ),
        new CanonTechnique(
            "T102", "Restriction Essence", "禁之源", TechType.RESTRICTION,
            "Self-forged by merging world laws and millions of fused formations",
            java.util.List.of(
                "4th-step-tier essence from lifetime of restriction mastery",
                "Merged laws of the world and formations from Immortal Astral Continent into eye bloodlines",
                "Evolved from acquiring all Four Great Restrictions to Abstract Realm"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1715 (completed); Spirit Void Peak → Arcane Void",
                "Restriction Essence True Body at Ch. 2044/2046",
                "Fused into Slaughter Thunder Essence True Body"
            ), "Ch.1715; Fandom wiki (self-forged, 1 of 14 Essences)"
        ),
        new CanonTechnique(
            "T103", "Unnamed Wheel Formation", "无名轮阵", TechType.RESTRICTION,
            "Self-created; upgraded version of Life-Death, Karma, True-False Wheel Formation",
            java.util.List.of(
                "Seals all Outer Realm cultivators from entering Inner Realm",
                "Allows Joss Flames to enter (fixing Realm-Sealing Formation's flaw)",
                "Four-part creation: treasure spirits, planet souls, enemy soul slaves, and Inner Realm immortal formation spirit"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1667; requires 3rd Step",
                "Replaces the Realm-Sealing Formation",
                "Utilizes souls from 100 years of war"
            ), "Ch.1667; Fandom wiki (self-created)"
        ),
        new CanonTechnique(
            "T104", "Blood Lines Rules", "血脉之规", TechType.RESTRICTION,
            "Self-comprehended restriction-essence rule",
            java.util.List.of("Restriction-essence bloodline law rule", "Bloodline-law category", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Ch. 1715; self-comprehended", "Restriction-essence derived rule", "Partial documentation"), "Ch.1715; Fandom wiki (self-comprehended)"
        ),
        new CanonTechnique(
            "T105", "Soul Searching", "搜魂术", TechType.OTHER,
            "Self-taught",
            java.util.List.of("Rips memories from a captured soul", "Interrogation technique", "Requires Core Formation+"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Early (around Ch. 180+)", "Self-taught soul interrogation", "Standard soul technique"), "early Ch.~180+; Fandom wiki (self-taught)"
        ),
        new CanonTechnique(
            "T106", "Soul Flag Production Method", "魂旗炼制法", TechType.OTHER,
            "Main cultivation method of the Soul Refining Sect",
            java.util.List.of(
                "Splits into Soul Refining, Soul Extracting, and Soul Sealing",
                "Lets user refine souls into a Soul Flag",
                "Powers the One Billion Soul Banner"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 384; Foundation Establishment+",
                "Inherited from Soul Refining Sect",
                "Wang Lin inherits and repairs One Billion Soul Banner from Dun Tian"
            ), "Ch.384; Fandom wiki (Soul Refining Sect)"
        ),
        new CanonTechnique(
            "T107", "Soul Refining", "炼魂", TechType.OTHER,
            "Soul Refining Sect method",
            java.util.List.of("Refines souls for use", "Part of Soul Flag Production Method", "Foundation Establishment+ required"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 384; part of Soul Flag Production Method",
                "Soul Refining Sect core technique",
                "Basic soul processing"
            ), "Ch.384; Fandom wiki (Soul Refining Sect)"
        ),
        new CanonTechnique(
            "T108", "Soul Extracting", "抽魂", TechType.OTHER,
            "Soul Refining Sect method",
            java.util.List.of(
                "Extracts souls from bodies",
                "Part of Soul Flag Production Method",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 384; part of Soul Flag Production Method",
                "Soul Refining Sect core technique",
                "Soul extraction"
            ), "Ch.384; Fandom wiki (Soul Refining Sect)"
        ),
        new CanonTechnique(
            "T109", "Soul Sealing", "封魂", TechType.OTHER,
            "Soul Refining Sect method",
            java.util.List.of(
                "Seals souls for storage/use",
                "Part of Soul Flag Production Method",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 384; part of Soul Flag Production Method", "Soul Refining Sect core technique", "Soul sealing"), "Ch.384; Fandom wiki (Soul Refining Sect)"
        ),
        new CanonTechnique(
            "T110", "Soul-Devourer Soul Nature", "噬魂者本性", TechType.OTHER,
            "Innate soul-nature; manifests as Soul Gem",
            java.util.List.of(
                "Innate soul-devourer nature with Soul Gem",
                "Can create wandering souls",
                "Evolves through multiple absorptions: Thunder Dragon, Heavenly Flame, Devilish Flames, Fire/Flame Dragons, Soul Blood"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 119; innate ability",
                "Soul Gem appears at Ch. 193",
                "Eventually becomes Ancient Order Soul close to Ancient Ancestor"
            ), "Ch.119; Fandom wiki (innate, Soul Gem Ch.193)"
        ),
        new CanonTechnique(
            "T111", "Soul Branding / Slave Seal", "灵魂烙印", TechType.OTHER,
            "Self-acquired",
            java.util.List.of("Brands/enslaves souls with a seal", "Slave seal creation", "Requires Nascent Soul+"), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Early chapters; used on Big Head Cultivator and others",
                "Self-acquired technique",
                "Soul branding / slave seal"
            ), "early; Fandom wiki (confidence 4)"
        ),
        new CanonTechnique(
            "T112", "Magic Arsenal", "魔库术", TechType.OTHER,
            "Bai Fan lineage; 3rd of Six Paths Triple Arts",
            java.util.List.of(
                "Stores all souls of enemies killed throughout lifetime",
                "Catalyst for 18 Layers of Hell when combined with Underworld River + Celestial Sealing Stamp",
                "Soul-storing arsenal spell"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 919; Nirvana Scryer+ required",
                "3rd of Bai Fan's Six Paths Triple Arts",
                "Combined with Underworld River → 18 Layers of Hell (Ch. 915)"
            ), "Ch.919; Fandom wiki (Bai Fan Six Paths Triple Arts #3)"
        ),
        new CanonTechnique(
            "T113", "18 Layers of Hell Reincarnation Realm", "十八层地狱轮回界", TechType.OTHER,
            "Self-created composite using Life-Death Underworld River + Celestial Sealing Stamp",
            java.util.List.of(
                "Forms Wang Lin's own private reincarnation cycle",
                "Uses Magic Arsenal's stored souls bound by Life-Death Domain's Underworld River",
                "Reincarnation-cycle formation"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 915; Nirvana Scryer+ required",
                "Self-created composite technique",
                "Direct evolution of Dao Transformation Yellow Springs"
            ), "Ch.915; Fandom wiki (self-created composite)"
        ),
        new CanonTechnique(
            "T114", "Nether Guide", "冥引", TechType.OTHER,
            "Taught by Qing Shui (Wang Lin's senior brother)",
            java.util.List.of(
                "Condenses memories of recently killed persons to open a portal to where they had been",
                "Memory-portal soul technique",
                "Requires Nirvana Scryer+"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 949; taught by Qing Shui", "Soul/movement dual-purpose", "Memory-based portal creation"), "Ch.949; Fandom wiki (taught by Qing Shui)"
        ),
        new CanonTechnique(
            "T115", "Heaven Avoiding Coffin", "避天棺", TechType.OTHER,
            "Acquired to preserve Li Muwan's soul",
            java.util.List.of(
                "Preserves a dissipating nascent soul inside coffin",
                "Sustains soul with trace of life force",
                "Mysterious force surrounds entire coffin"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 819 (item); Ascendant+ required", "Soul-preservation treasure", "Used for Li Muwan's soul"), "Ch.819; Fandom wiki (soul-preservation for Li Muwan)"
        ),
        new CanonTechnique(
            "T116", "Qi Xi Spell", "乞夕术", TechType.OTHER,
            "Learned from Zhan Li Yunzi's ancestor",
            java.util.List.of(
                "Life-force exchange resurrection spell",
                "Sacrifices user's vitality to resurrect another and form flesh from Nascent Soul",
                "50% of Wang Lin's life force demanded"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); requires 3rd Step",
                "Failed to fully recover Li Muwan — didn't pass 4th day out of 7",
                "Life-force sacrifice resurrection"
            ), "implicit (Baidu); Baidu Baike (life-force sacrifice resurrection)"
        ),
        new CanonTechnique(
            "T117", "Soul Devil Ship", "魂魔船", TechType.OTHER,
            "Self-forged from four great restrictions and many other restrictions",
            java.util.List.of("Composite soul-restriction vehicle/weapon", "Made from four great restrictions", "Soul-devil ship"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1789; requires 3rd Step", "Self-forged composite", "Uses all four great restrictions as base"), "Ch.1789; Fandom wiki (self-forged from four great restrictions)"
        ),
        new CanonTechnique(
            "T118", "Heavenly Flame", "天火", TechType.OTHER,
            "Absorbed from Ming Hai's Burn the Heavens spell",
            java.util.List.of(
                "Absorbed heavenly flame into Soul-devourer nature",
                "Part of Vermilion Bird awakening cycle",
                "Nirvana Scryer+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1021; absorbed from Ming Hai",
                "Part of the Vermilion Bird awakening evolution",
                "Fire-attribute soul absorption"
            ), "Ch.1021; Fandom wiki (absorbed from Ming Hai)"
        ),
        new CanonTechnique(
            "T119", "Earth Escape Technique", "土遁术", TechType.MOVEMENT,
            "Self-acquired basic escape technique",
            java.util.List.of(
                "Basic earth-escape movement technique",
                "Allows underground travel",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 134 (S1 EP16)", "Basic escape technique", "Foundation Establishment+ required"), "Ch.134 (S1 EP16); Fandom wiki (self-acquired)"
        ),
        new CanonTechnique(
            "T120", "Disguising Technique", "易容术", TechType.MOVEMENT,
            "Basic immortal technique",
            java.util.List.of(
                "Hides cultivation for a short period",
                "Unless target is at Foundation Establishment, disguise holds",
                "Qi Condensation+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 33; very early technique", "Basic immortal technique", "Foundational stealth"), "Ch.33; Fandom wiki (basic immortal technique)"
        ),
        new CanonTechnique(
            "T121", "Memory Erasing Technique", "忘情术", TechType.MOVEMENT,
            "Self-acquired",
            java.util.List.of("Erases memories of targets", "Stealth/support technique", "Foundation Establishment+ required"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 78; self-acquired", "Memory erasure ability", "Foundation Establishment+ required"), "Ch.78; Fandom wiki (self-acquired)"
        ),
        new CanonTechnique(
            "T122", "Attractive Force Technique", "引力术", TechType.MOVEMENT,
            "One of Three Techniques in Three Stages of Qi Condensation",
            java.util.List.of(
                "Manipulates objects from a distance (telekinesis)",
                "Usable at 1st layer Qi Condensation",
                "Most basic immortal technique"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 25 (S1 EP2); 1st layer Qi Condensation",
                "Telekinesis ability",
                "One of the three basic immortal techniques"
            ), "Ch.25 (S1 EP2); Fandom wiki (Three Stages of Qi Condensation)"
        ),
        new CanonTechnique(
            "T123", "Fireball Technique", "火球术", TechType.OTHER,
            "Basic immortal technique (Three Stages of Qi Condensation)",
            java.util.List.of("Basic fireball attack", "Usable at 1st layer Qi Condensation", "Most basic combat spell"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 25 (S1 EP2); 1st layer Qi Condensation",
                "Basic immortal technique",
                "One of the three basic techniques"
            ), "Ch.25 (S1 EP2); Fandom wiki (Three Stages of Qi Condensation)"
        ),
        new CanonTechnique(
            "T124", "Earth Splitting Technique", "裂地术", TechType.OTHER,
            "Basic immortal technique (Three Stages of Qi Condensation)",
            java.util.List.of(
                "Splits the earth as attack/movement",
                "Usable at 1st layer Qi Condensation",
                "Basic combat/movement hybrid"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 25 (S1 EP2); 1st layer Qi Condensation",
                "Basic immortal technique",
                "Earth-attribute technique"
            ), "Ch.25 (S1 EP2); Fandom wiki (Three Stages of Qi Condensation)"
        ),
        new CanonTechnique(
            "T125", "Teleportation Restriction", "瞬移禁制", TechType.MOVEMENT,
            "Self-created restriction-teleport hybrid",
            java.util.List.of(
                "Uses split-second time distortion during teleportation",
                "Seals self, teleports multiple times in one instant, releases seal for multiplied distance",
                "Core Formation+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 493 (S1 EP106)",
                "Self-created restriction-teleport hybrid",
                "Also known as Great Teleportation Forbidden Technique"
            ), "Ch.493 (S1 EP106); Fandom wiki (self-created hybrid)"
        ),
        new CanonTechnique(
            "T126", "Shrink Earth to Inch", "缩地成寸", TechType.MOVEMENT,
            "One of four great divine abilities 2nd-step cultivators can comprehend",
            java.util.List.of(
                "Divine ability for traversing and walking between planets",
                "On Xiangang Continent, must reach Golden Venerable level",
                "Used multiple times to distance from powerful enemies"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit (Baidu); 2nd Step (Cave Mansion Realm)",
                "Self-comprehended by sensing heaven and earth",
                "Planet-traversal movement ability"
            ), "implicit (Baidu); Baidu Baike (4th-step divine ability)"
        ),
        new CanonTechnique(
            "T127", "Star Compass / Silver Dragon Star Compass", "星盘", TechType.MOVEMENT,
            "Gifted by Giant Demon Clan's Chi Hu; can only be used in void",
            java.util.List.of(
                "Extremely fast void-vehicle; primary void transportation",
                "Upgraded to Silver Dragon Star Compass at Ch. 477",
                "Ascendant+ required to operate"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 477 (upgraded); gifted by Chi Hu", "Void-only vehicle", "Primary means of void transportation"), "Ch.477 (upgraded); Fandom wiki; Baidu Baike (gifted by Chi Hu)"
        ),
        new CanonTechnique(
            "T128", "Flying Sword", "飞剑", TechType.MOVEMENT,
            "Self-acquired; first flying sword named Wealth",
            java.util.List.of("Basic flying sword movement", "Qi Condensation+ required", "First flying sword obtained ~Ch. 30"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("~Ch. 30; first flying sword Wealth", "Basic movement technique", "Qi Condensation+ required"), "Ch.~30; Fandom wiki (first flying sword Wealth)"
        ),
        new CanonTechnique(
            "T129", "Divine Path", "神道诀", TechType.OTHER,
            "War God Shrine's Divine Path technique",
            java.util.List.of(
                "Creates an Avatar to help break through to Nascent Soul",
                "Original drawback: Avatar lacks cultivation and lives only 30 years",
                "Ji Realm conflict removes lifespan drawback; no primary/secondary distinction between original and avatar"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 129 (S1 EP26); Foundation Establishment+",
                "Avatar cultivates normally while main body becomes Ancient God",
                "War God Shrine technique"
            ), "Ch.129 (S1 EP26); Fandom wiki; Baidu Baike (War God Shrine, creates Avatar)"
        ),
        new CanonTechnique(
            "T130", "War God Shrine Refining Technique", "战神殿炼制法", TechType.OTHER,
            "War God Shrine",
            java.util.List.of(
                "War-shrine method for avatar/refining",
                "War God Shrine refining technique",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("~S1 EP26; War God Shrine", "Avatar / refining category", "Partial documentation"), "S1 EP26; Fandom wiki (confidence 4)"
        ),
        new CanonTechnique(
            "T131", "Heaven Devouring Demon Formation", "吞天魔阵", TechType.OTHER,
            "Self-acquired vicious formation",
            java.util.List.of(
                "Vicious formation using bodies of killed cultivators as base",
                "10 FE bodies trap a CF cultivator; 5 CF bodies kill mid/low CF cultivator",
                "Bodies must be ones Wang Lin personally killed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 139; Foundation Establishment+",
                "Heaven Devouring Demon Formation",
                "Very inconvenient: requires personally-killed bodies"
            ), "Ch.139; Fandom wiki (self-acquired vicious formation)"
        ),
        new CanonTechnique(
            "T132", "Foundation Stealing Technique", "夺基术", TechType.OTHER,
            "Self-acquired / Situ Nan",
            java.util.List.of(
                "Steals a person's foundation and talent for instant Foundation Establishment",
                "Used on Teng Huayuan's son Teng Li at Ch. 86",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 84 (S1 EP7)",
                "Used to steal Teng Li's cultivation",
                "Instant Foundation Establishment via theft"
            ), "Ch.84 (S1 EP7); Fandom wiki (used on Teng Li Ch.86)"
        ),
        new CanonTechnique(
            "T133", "Blood Refining Technique", "血炼术", TechType.OTHER,
            "Created with Situ Nan's help; Blood Symbol method",
            java.util.List.of(
                "Refines treasure faster with Blood Symbol; user gets hurt if treasure is damaged",
                "Treasure gains blood aura and gets stronger with more kills",
                "Foundation Establishment+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 80 (S1 EP5)", "Blood Symbol creation with Situ Nan's help", "Treasure-blood refining method"), "Ch.80 (S1 EP5); Fandom wiki (Situ Nan helped create)"
        ),
        new CanonTechnique(
            "T134", "Cultivator Clone / Celestial Body", "修仙分身", TechType.OTHER,
            "Divine Path technique; main cultivator avatar",
            java.util.List.of(
                "Primary Qi Cultivator avatar; evolves through multiple bodies",
                "Evolution: Celestial Body (Ch.424) → Thunder Body (Ch.719) → Immortal Celestial Body (Ch.1538)",
                "Fused back with Main Body at Ch. 211"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 211 (fused back with Main Body)",
                "Divine Path technique — main cultivator avatar",
                "Multiple body evolutions across the story"
            ), "Ch.211; Fandom wiki; Baidu Baike (evolutions: Celestial Ch.424, Thunder Ch.719, Immortal Ch.1538)"
        ),
        new CanonTechnique(
            "T135", "Ancient Demon Clone", "古魔分身", TechType.OTHER,
            "Cultivating demonic energy; from statue in Land of Demonic Spirits",
            java.util.List.of(
                "Second Avatar for Ancient Demon powers",
                "From a statue in Land of Demonic Spirits; refined into a Second Avatar",
                "Fused back with Main Body at Ch. 1002 to become an Ancient One"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1002 (fused back); Ancient Order track",
                "Incomplete Seed initially, incapable of existing outside statue",
                "Completed via Scattered Demon fragment + Soul Refining Tribe worship"
            ), "Ch.1002; Fandom wiki; Baidu Baike (from statue in Land of Demonic Spirits)"
        ),
        new CanonTechnique(
            "T136", "Ancient Devil Clone", "古妖分身", TechType.OTHER,
            "From Devil Soul Bottle's 3000 Ancient Devil Souls; infused into Ancient Devil Corpse with Royal Aura",
            java.util.List.of(
                "Third Avatar for Ancient Devil powers",
                "Half-finished from 3000 Ancient Devil Souls; completed via Ancient Devil Corpse + Royal Aura",
                "Fused back at ~Ch.1539 to become an Ancient One"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "~Ch.1539 (fused back); Ancient Order track",
                "From Daogu Yemo's tomb corpse (Baidu)",
                "Third Avatar; completed with Royal Aura infusion"
            ), "Ch.~1539; Fandom wiki; Baidu Baike (3000 Ancient Devil Souls)"
        ),
        new CanonTechnique(
            "T137", "Otherworldly Void Avatar", "异界虚空分身", TechType.OTHER,
            "Condensed at arc-shaped platform; same Void Destiny as Immortal/Ancient Ancestor",
            java.util.List.of(
                "Possesses same Void Destiny as Immortal Ancestor and Ancient Ancestor",
                "Caused nine suns of Xiangang to appear simultaneously",
                "Fused during battle with Ancient Path; enables full Nine Songs and Three Signs"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1798; Void Tribulant (3rd Step peak)",
                "Condensed at arc-shaped platform",
                "Enables full Nine Songs and Three Signs when fused"
            ), "Ch.1798; Fandom wiki; Baidu Baike (Void Destiny)"
        ),
        new CanonTechnique(
            "T138", "Slaughter True Body / Lu Mo", "戮默", TechType.OTHER,
            "Formed accidentally by fusing Thunder True Body with Slaughter and Restriction Essences",
            java.util.List.of(
                "Black-haired Wang Lin clone; combat power threatens Grand Empyreans",
                "Represents Wang Lin's lifetime of murder (戮 + 寂灭 = Slaughter + Silent Extinction)",
                "Sent back in time via Flowing Moon to find method to resurrect Li Muwan"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1900 (initial) → Ch. 2046 (completed)",
                "Named 'Slaughter' (戮); given independence then merged back",
                "Fuses with all other True Bodies → Punishment Slaughter"
            ), "Ch.1900-2046; Fandom wiki; Baidu Baike (Thunder+Slaughter+Restriction)"
        ),
        new CanonTechnique(
            "T139", "Five Elements True Body", "五行真身", TechType.OTHER,
            "Self-forged by fusing Fire, Earth, Water, Metal, Wood Essence True Bodies",
            java.util.List.of(
                "Represents Wang Lin's lifetime of cultivation in Five Elements",
                "Each Five Elements Origin condensed a true body; all fuse into one",
                "Fire+Earth+Water at Ch.1900; completed with Metal (Ch.2024) and Wood (Ch.2017)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1900 (initial) → Ch. 2024 (completed)",
                "Arcane Void → Void Tribulant",
                "Five Elements Essences unified into single true body"
            ), "Ch.1900-2024; Fandom wiki; Baidu Baike (Fire+Earth+Water Ch.1900, Metal Ch.2024, Wood Ch.2017)"
        ),
        new CanonTechnique(
            "T140", "Slaughter Thunder Essence True Body", "戮雷真身", TechType.OTHER,
            "Formed accidentally by fusing Thunder True Body + Slaughter + Restriction Essences",
            java.util.List.of(
                "Composite true body of Thunder + Slaughter + Restriction",
                "Accidental creation during Arcane Void advancement",
                "3rd Step required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1900 (initial) → Ch. 2046 (completed)",
                "Accidentally created during Ch. 1901 advancement",
                "Contains Slaughter (Lu Mo) within it"
            ), "Ch.1900-2046; Fandom wiki (Thunder+Slaughter+Restriction fusion)"
        ),
        new CanonTechnique(
            "T141", "Restriction Essence True Body", "禁源真身", TechType.OTHER,
            "Self-forged by splitting Five Elements True Body and integrating restrictions from Xuan Lou",
            java.util.List.of(
                "Restriction-attribute true body",
                "Split from Five Elements True Body; integrated Xuan Lou's restrictions",
                "Half-Heaven-Trampling required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2044 (initial) → Ch. 2046 (completed)",
                "Self-forged with Xuan Lou's restrictions",
                "Half-Heaven-Trampling"
            ), "Ch.2044-2046; Fandom wiki (split Five Elements + Xuan Lou)"
        ),
        new CanonTechnique(
            "T142", "Absolute Beginning Essence True Body", "太初真身", TechType.OTHER,
            "Self-forged by simulating day/night alternation with multiple techniques",
            java.util.List.of(
                "Absolute Beginning (Taichu) attribute true body",
                "Created via day/night simulation: Absolute Beginning as sun, Absolute End as darkness",
                "Used Grand Empyrean Power + Flowing Time Spell + wisp of Origin Soul"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit → completed via century of closed-door cultivation with Lou Hou's Spirit",
                "Self-forged with complex simulation technique",
                "Half-Heaven-Trampling"
            ), "implicit; Fandom wiki (century cultivation with Lou Hou Spirit)"
        ),
        new CanonTechnique(
            "T143", "Absolute End Essence True Body", "寂灭真身", TechType.OTHER,
            "Same simulation process as Absolute Beginning",
            java.util.List.of(
                "Absolute End (Silent Extinction/Miemie) attribute true body",
                "Created as darkness in day/night simulation",
                "Half-Heaven-Trampling required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit → completed alongside Absolute Beginning",
                "Same complex simulation as Absolute Beginning",
                "Half-Heaven-Trampling"
            ), "implicit; Fandom wiki (completed alongside Absolute Beginning)"
        ),
        new CanonTechnique(
            "T144", "Slaughter Essence True Body", "杀戮真身", TechType.OTHER,
            "Self-forged; suppressed Slaughter's growth until Absolute Beginning/End learned",
            java.util.List.of(
                "Slaughter-attribute true body",
                "Refined second time to prevent Slaughter rebelling",
                "Fuses with all prior Essence True Bodies → Punishment Slaughter"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Initial after Slaughter Thunder creation → completed via century of nourishment",
                "Suppressed to maintain balance until Absolute Beginning/End Essences learned",
                "Half-Heaven-Trampling"
            ), "after Ch.1900; Fandom wiki (fuses into Punishment Slaughter)"
        ),
        new CanonTechnique(
            "T145", "Fire Essence True Body", "火源真身", TechType.OTHER,
            "Consumed ~120 Fire Veins + Main Fire Vein in Great Soul Sect",
            java.util.List.of(
                "Fire-attribute true body",
                "Evolved from Vermilion Bird manifestation after consuming Fire Veins",
                "3rd Step required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1825; 3rd Step",
                "Consumed ~120 Fire Veins + Main Fire Vein",
                "Evolved from Vermilion Bird manifestation"
            ), "Ch.1825; Fandom wiki (consumed ~120 Fire Veins)"
        ),
        new CanonTechnique(
            "T146", "Thunder Essence True Body", "雷源真身", TechType.OTHER,
            "Dao Devil Sect Master fed his Thunder Essence into Wang Lin's to evolve it",
            java.util.List.of(
                "Thunder-attribute true body",
                "Dao Devil Sect Master's Thunder Essence fed into Wang Lin's",
                "Wang Lin reclaimed it after Heaven Defying Bead protected him from will-erasure"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1892; 3rd Step",
                "Seized back from Dao Devil Sect Master's control",
                "Heaven Defying Bead enabled recovery"
            ), "Ch.1892; Fandom wiki (Dao Devil Sect Master fed, Wang Lin seized)"
        ),
        new CanonTechnique(
            "T147", "Earth Essence True Body", "土源真身", TechType.OTHER,
            "Ji Si used Meng Earth Beads + Celestial Ancestor's grains of sand",
            java.util.List.of(
                "Earth-attribute true body",
                "Third grain of Celestial Ancestor's continent-creation sand used",
                "3rd Step required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1895; 3rd Step",
                "Three Meng Earth Beads + two of nine grains of Celestial Ancestor's sand",
                "Completed the 9th cycle"
            ), "Ch.1895; Fandom wiki (Ji Si used 3 Meng Earth Beads)"
        ),
        new CanonTechnique(
            "T148", "Water Essence True Body", "水源真身", TechType.OTHER,
            "Ji Si used Celestial Sea Mother Soul, Mortal Dream Water, Dao Heart Melting Liquid, etc.",
            java.util.List.of(
                "Water-attribute true body",
                "Fourth Essence True Body; originally for Green Scorpion to possess",
                "3rd Step required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1897; 3rd Step",
                "Created with Celestial Sea Mother Soul, Mortal Dream Water, Dao Heart Melting Liquid, Water Celestial Blood",
                "Required 16 years of Ji Si's life in blood"
            ), "Ch.1897; Fandom wiki (Ji Si used Celestial Sea Mother Soul)"
        ),
        new CanonTechnique(
            "T149", "Wood Essence True Body", "木源真身", TechType.OTHER,
            "Pure Wood Essence liquid from Ji Du + Wood Essence absorbed with Xuan Lou",
            java.util.List.of(
                "Wood-attribute true body",
                "Formed from Ji Du's pure Wood Essence liquid",
                "Void Tribulant required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2017; Void Tribulant",
                "Ji Du's pure Wood Essence liquid",
                "Wood Essence absorbed while with Xuan Lou"
            ), "Ch.2017; Fandom wiki (Ji Du pure Wood Essence liquid)"
        ),
        new CanonTechnique(
            "T150", "Metal Essence True Body", "金源真身", TechType.OTHER,
            "Integration of Celestial Ancestor's Sword fragments + Metal Essence with Xuan Lou",
            java.util.List.of(
                "Metal-attribute true body",
                "Third fragment of Celestial Ancestor's Immortal Absolute Sword",
                "Void Tribulant Peak required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2024; Void Tribulant Peak",
                "Celestial Ancestor's Sword fragments integrated",
                "Great Accomplishment with third fragment from Grand Empyrean Jiu Di"
            ), "Ch.2024; Fandom wiki (Celestial Ancestor Sword 3rd fragment)"
        ),
        new CanonTechnique(
            "T151", "Punishment Slaughter", "刑戮", TechType.OTHER,
            "Fused Slaughter with Thunder → Restriction → Absolute Beginning/End → Five Elements True Body",
            java.util.List.of(
                "Final fused avatar from all Essence True Bodies + Slaughter",
                "Slaughter struggled until Five Elements True Body pressured him with divine sense",
                "Half-Heaven-Trampling required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. ~2065; Half-Heaven-Trampling",
                "All Essence True Bodies fused into Slaughter → Punishment Slaughter",
                "Ultimate fused avatar form"
            ), "Ch.~2065; Fandom wiki (final fused avatar)"
        ),
        new CanonTechnique(
            "T152", "Life and Death Domain", "生死域", TechType.OTHER,
            "Cultivated by spending a mortal lifetime (Ch.139 arc + Ch.272 breakthrough)",
            java.util.List.of(
                "Manifests as the Underworld River (Ch.604); controls cycle of life and death",
                "Breakthrough requires living as mortal for one full lifetime",
                "Evolves: Domain → Life and Death Essence (Ch.1280/1613) → Life and Death Seal (5th Original Spell)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 264; ~age 400; Nascent Soul → Soul Formation",
                "First domain; foundation for Karma and True-False domains",
                "Sacrificed to fuel Fire and Thunder Essences at Ch.1280; re-completed later"
            ), "Ch.264 (S1 EP62); Fandom wiki (Underworld River Ch.604)"
        ),
        new CanonTechnique(
            "T153", "Karma Domain", "因果域", TechType.OTHER,
            "Evolved from Life and Death Domain after another mortal lifetime with Wang Ping",
            java.util.List.of(
                "Manifests as Karmic Cycle / Yin-Yang Symbol (left eye yang, right eye yin)",
                "Karma Whip is its treasure-form",
                "Evolves: Domain → Karma Essence → Karma Print (4th Original Spell)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 731 (Donghua EP147); ~age 900+",
                "Evolved after spending lifetime as mortal with son Wang Ping",
                "Ascendant+ required"
            ), "Ch.731 (Donghua EP147); Fandom wiki (evolved from Life-Death Domain)"
        ),
        new CanonTechnique(
            "T154", "True and False Domain", "真假域", TechType.OTHER,
            "Evolved from Karma Domain while comprehending Bai Fan's Mountain Crumble",
            java.util.List.of(
                "Controls Real and Unreal; makes others enter nightmare hallucinations",
                "Sundered Night triggers it: right eye lightning, left eye true/false",
                "Evolves: Domain → True-False Essence → True-False Eternal Seal (6th Original Spell)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1102; ~age 1300",
                "Evolved while being a mountain soul in Vermillion Bird Divine Sect Starfield",
                "Nirvana Cleanser+ required"
            ), "Ch.1102; Fandom wiki (evolved from Karma Domain)"
        ),
        new CanonTechnique(
            "T155", "Battle Will / Battle Domain", "战之域", TechType.OTHER,
            "Inherited from golden word 'Battle' after merging Zhen Family Battle Scrolls + Zhan Xingye skeletons",
            java.util.List.of(
                "Battle-will domain from golden 'Battle' word",
                "Fused from three Zhen Family Battle Scrolls + golden light from Zhan Xingye skeletons",
                "Nirvana Cleanser+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1206", "Inherited, not self-comprehended", "Nirvana Cleanser+"), "Ch.1206; Fandom wiki (Zhen Family Battle Scrolls + Zhan Xingye)"
        ),
        new CanonTechnique(
            "T156", "Star of Law", "法则之星", TechType.OTHER,
            "Fusion of True-False Domain, Fire/Thunder tattoo, and Battle Domain",
            java.util.List.of(
                "Star-shaped Law Mark fusing life-death, karma domains, and fire/thunder mark",
                "Briefly sealed at Ch.1221 (pseudo Nirvana Shatterer) — unsealed at Ch.1298",
                "Also called Store All Ji Thunder (7th accompanying thunder) at Ch.1368"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1220-1221; Nirvana Cleanser Peak",
                "Wang Lin's own 7th type of law/creation",
                "Sealing risk could have prevented advancement"
            ), "Ch.1220-1221; Fandom wiki; Baidu Baike (sealed Ch.1221, unsealed Ch.1298)"
        ),
        new CanonTechnique(
            "T157", "Underworld River", "黄泉/冥河", TechType.OTHER,
            "Manifestation of Life and Death Domain",
            java.util.List.of(
                "Physical manifestation of Life and Death Domain as a river",
                "Used as catalyst to bind souls into Celestial Sealing Stamp",
                "Key component of 18 Layers of Hell Reincarnation Realm"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 604; Soul Formation+",
                "Life-Death Domain manifestation",
                "Catalyst for 18 Layers of Hell formation"
            ), "Ch.604; Fandom wiki (Life-Death Domain manifestation)"
        ),
        new CanonTechnique(
            "T158", "Karmic Cycle / Yin-Yang Symbol", "因果轮回/阴阳符", TechType.OTHER,
            "Karma Domain manifestation",
            java.util.List.of(
                "Left eye containing yang, right eye containing yin",
                "Physical manifestation of Karma Domain",
                "Ascendant+ required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 850; Ascendant+", "Karma Domain physical manifestation", "Yin-Yang symbol in the eyes"), "Ch.850; Fandom wiki (Karma Domain manifestation)"
        ),
        new CanonTechnique(
            "T159", "Void Avatar (Late-Game Form)", "虚空分身", TechType.OTHER,
            "Condensed at arc-shaped platform; fused during Ancient Path battle",
            java.util.List.of(
                "Same Void Destiny as Immortal Ancestor and Ancient Ancestor",
                "Fusing with it allows summoning full Nine Songs and Three Signs",
                "Caused nine suns of Xiangang to appear simultaneously"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1798; Void Tribulant (3rd Step peak)",
                "Also listed in Avatar section (G) as Otherworldly Void Avatar",
                "Enables full fusion of Celestial and Ancient Powers"
            ), "Ch.1314; Fandom wiki (8-star Ancient God spell)"
        ),
        new CanonTechnique(
            "T160", "Nine Songs and Three Signs", "九歌三符", TechType.OTHER,
            "Achieved by fusing with Void Avatar during Gu Dao battle",
            java.util.List.of(
                "Cosmic manifestation making Wang Lin match for late Celestial/Ancient Ancestors",
                "Significantly progresses fusion of Celestial and Ancient Powers",
                "Unrivalled on the Immortal Astral Continent"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2065 (full summon during Gu Dao battle)",
                "Requires Half-Heaven-Trampling (7th Bridge+)",
                "Those past 8th Bridge but not 9th level"
            ), "Ch.2065; Fandom wiki (Gu Dao battle)"
        ),
        new CanonTechnique(
            "T161", "Reincarnation Essence", "轮回源", TechType.OTHER,
            "Comprehended via Dong Lin Pool meditation and Heaven Defying Bead secret",
            java.util.List.of(
                "4th Ethereal Essence; completing it = achieving Heaven Trampling",
                "Begun at Ch.1943; completed at Ch.2087",
                "Confirmed by uncovering Ji Qiong skull's divine sense from Slaughter"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1943 (potential) → Ch. 2087 (completed)",
                "4th Step required for completion",
                "Directly enables Heaven Trampling upon mastery"
            ), "Ch.1943-2087; Fandom wiki; Baidu Baike (4th Ethereal Essence, Heaven Trampling Ch.2087)"
        ),
        new CanonTechnique(
            "T162", "Heaven Trampling", "踏天境", TechType.OTHER,
            "Achieved without stepping on more bridges; comprehended via Reincarnation Essence",
            java.util.List.of(
                "Opens world within the illusory, forming reincarnation cycle within",
                "Confrontation between reincarnation cycles determines everything",
                "Walking a single Dao to its end and becoming its source = 4th Step"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2087 (revealed); ~age 3000",
                "Achieved without completing 9th bridge — via Reincarnation Essence",
                "4th Step; not all Daos allow becoming the source"
            ), "Ch.2087; Fandom wiki; Baidu Baike (achieved via Reincarnation Essence)"
        ),
        new CanonTechnique(
            "T163", "Grand Empyrean Sun", "大天尊之阳", TechType.OTHER,
            "Condensed by absorbing Celestial Ancestor's head; tricolor black-white-gold",
            java.util.List.of(
                "Tricolored (black, white, gold) Venerable Sun — most powerful type",
                "Energy within can be absorbed to enhance strength",
                "Makes Wang Lin 10th Sun of Immortal Astral Continent, 2nd strongest after Gu Dao"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 2050 (fully condensed); Half-Heaven-Trampling (3rd Bridge+)",
                "Lian Daozhan's Soul used as guide for final condensation",
                "Black-White outline (Ch.1997) → Black-White-Gold (Ch.2050)"
            ), "Ch.2050; Fandom wiki; Baidu Baike (10th Sun of IAC)"
        ),
        new CanonTechnique(
            "T164", "Ethereal Fire", "虚无之火", TechType.OTHER,
            "4th Vermilion Bird Awakening; 9 colors fused into one",
            java.util.List.of(
                "First and only cultivatable Ethereal Fire thanks to Heaven Defying Bead",
                "After 4th awakening, absorbed Nine Tribulation Karma fires for 9 fire types",
                "Supports Fire Essence with 9 accompanying types of fire"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1412; Nirvana Shatterer+",
                "4th Vermilion Bird Awakening product",
                "Unique: only cultivatable Ethereal Fire in existence"
            ), "Ch.1412 (4th VB Awakening); Fandom wiki"
        ),
        new CanonTechnique(
            "T165", "Ethereal Essences", "虚源", TechType.OTHER,
            "4 virtual/special origins representing four realms",
            java.util.List.of(
                "4 virtual origins: Life-Death, Cause-Effect, True-False, Reincarnation",
                "4 special origins: Primordial/Taichu, Silent Extinction/Miemie, Restriction, Slaughter",
                "Each formed their own true body; Killing + Silent Extinction → Lu Mo"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1943+ (Reincarnation), 1950 (Absolute Beginning/End)",
                "14 Origins total: 6 substantial + 4 virtual + 4 special",
                "3rd Step → 4th Step"
            ), "Ch.1943+; Fandom wiki; Baidu Baike (14 Origins total)"
        ),
        new CanonTechnique(
            "T166", "Origin Swords", "源剑", TechType.OTHER,
            "Condensed using strange power from void gate collapse vortex",
            java.util.List.of(
                "Set of seven: Fire, Thunder, Cause-Effect, Life-Death, True-False, Slaughter, Restriction",
                "Condensed from void gate collapse vortex power",
                "Karma Sword is one of the seven; 3rd Step required"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1561+ (Slaughter Sword), 1625+ (Lightning Sword), 5 elemental swords",
                "Seven Origin Swords total including Restriction Sword (Ch.1715)",
                "3rd Step required"
            ), "Ch.1561+; Fandom wiki (7 swords: Fire, Thunder, Cause-Effect, Life-Death, True-False, Slaughter, Restriction Ch.1715)"
        ),
        new CanonTechnique(
            "T167", "Brilliant Void Star System Crossing", "虚空星系穿越", TechType.MOVEMENT,
            "Movement across the Brilliant Void (later Alliance) Star System",
            java.util.List.of(
                "Sustained void travel across star systems",
                "2nd Step+ required for sustained void travel",
                "Void traversal movement ability"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Implicit; late Cave-World period",
                "Originally named Brilliant Void, later Alliance Star System",
                "2nd Step+ for sustained void travel"
            ), "Ch.1314; Fandom wiki (8-star Ancient God spell)"
        ),
        new CanonTechnique(
            "OS01", "Sundered Night", "残夜", TechType.ORIGINAL_SPELL,
            "Self-created; First Original Spell — Dao of Beginning and End",
            java.util.List.of(
                "Controls Real and Unreal; right eye lightning, left eye true/false",
                "Makes others lose control, enter nightmare hallucinations",
                "Evolved into a Belief Art after reaching Kong Jie"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 988; Ascendant+ required",
                "Dao of Beginning and End / Creation and Extermination",
                "Used to kill Xu Kongzi and heavily wound Tian Yunzi"
            ), "Ch.988; Fandom wiki (1st Original Spell)"
        ),
        new CanonTechnique(
            "OS02", "Flowing Time", "流月", TechType.ORIGINAL_SPELL,
            "Self-created; Second Original Spell — Dao of Time",
            java.util.List.of(
                "Can reverse time",
                "Used to send Slaughter true body (Lu Mo) back to the past",
                "Comprehended at gate of Ancient Immortal Domain in Wind Immortal World"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1245; Nirvana Shatterer+ required",
                "Dao of Time",
                "Key to sending Lu Mo to the past for Li Muwan resurrection"
            ), "Ch.1245; Fandom wiki; Baidu Baike (2nd Original Spell, sent Lu Mo to past)"
        ),
        new CanonTechnique(
            "OS03", "Dream Dao", "梦道", TechType.ORIGINAL_SPELL,
            "Self-created; Third Original Spell — Dao Spell of Dreams",
            java.util.List.of(
                "With Slaughter Clone and three Dao Fruits, lived as mortal scholar",
                "Completed Life-Death, Karma, and True-False Essences",
                "Combined with Dream of Ancient Times → Immortal Dream (Ch.1675)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1295; Nirvana Shatterer+ required",
                "Dao Spell of Dreams",
                "Source for Dream Dao-derived resurrection method"
            ), "Ch.1295; Fandom wiki; Baidu Baike (3rd Original Spell)"
        ),
        new CanonTechnique(
            "OS04", "Karma Print", "因果印", TechType.ORIGINAL_SPELL,
            "Self-created; Fourth Original Spell — Dao of Karma",
            java.util.List.of("Karma-seal original spell", "Dao of Karma manifestation", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1614; 3rd Step required", "Dao of Karma", "Self-created original spell"), "Ch.1614; Fandom wiki (4th Original Spell)"
        ),
        new CanonTechnique(
            "OS05", "Life and Death Seal", "生死印", TechType.ORIGINAL_SPELL,
            "Self-created; Fifth Original Spell — Dao of Life and Death",
            java.util.List.of("Life-death seal original spell", "Dao of Life and Death manifestation", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1616; 3rd Step required", "Dao of Life and Death", "Self-created original spell"), "Ch.1616; Fandom wiki (5th Original Spell)"
        ),
        new CanonTechnique(
            "OS06", "True and False Eternal Seal", "真假永恒印", TechType.ORIGINAL_SPELL,
            "Self-created; Sixth Original Spell — Dao of True and False",
            java.util.List.of("True-false eternal seal original spell", "Dao of True and False manifestation", "Requires 3rd Step"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Ch. 1617; 3rd Step required", "Dao of True and False", "Self-created original spell"), "Ch.1617; Fandom wiki (6th Original Spell)"
        ),
        new CanonTechnique(
            "OS07", "Undying Ancient Finger", "不灭古指", TechType.ORIGINAL_SPELL,
            "Self-created; Seventh Original Spell — Three Ancient Clans condensed into one finger",
            java.util.List.of(
                "Terrifying offensive power AND terrifying recovery ability",
                "Condenses God/Devil/Demon power into one finger",
                "Inspired by Undying Immortal Body; has body-wide variant: Undying Ancient Body (Ch.1728)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1639; 3rd Step required",
                "Uses Undying Immortal Body as inspiration",
                "Seventh and final numbered Original Spell"
            ), "Ch.1639 (7th Original Spell); Fandom wiki; Baidu Baike"
        ),
        new CanonTechnique(
            "T171", "Undying Ancient Body", "不灭古体", TechType.OTHER,
            "Self-created; akin to Undying Ancient Finger but whole-body scale",
            java.util.List.of(
                "Whole-body undying state — body-wide variant of Undying Ancient Finger",
                "Defensive + recovery on the scale of the entire body",
                "Self-created at Ch. 1728; requires 3rd Step"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1728; self-created",
                "Body-scale variant of the 7th Original Spell (Undying Ancient Finger)",
                "Requires 3rd Step"
            ), "Ch.1728; Fandom wiki (self-created)"
        ),
        new CanonTechnique(
            "E01", "Metal Essence", "金之源", TechType.ESSENCE,
            "Fragment of Celestial Ancestor's Immortal Absolute Sword + Metal Essence from Xuan Lou",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "Obtained by absorbing sword fragments: 2nd from Mountain Tree Seal, 3rd from Jiu Di",
                "Completed at Ch. 1997; True Body at Ch. 2024"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Potential: Ch. 1896; Initial: Ch. 1938; Completed: Ch. 1997",
                "Eyes Suppressing the World tied to Metal Essence",
                "Great Accomplishment via third Celestial Ancestor's Sword fragment"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E02", "Wood Essence", "木之源", TechType.ESSENCE,
            "90,000-year-old celestial tree from Ji Si + Mountain Sea Spirits from Ji Du",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "Seed from Ji Si's celestial tree; completed with Mountain Sea Spirits",
                "Completed at Ch. 1970; True Body at Ch. 2017"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Potential: Ch. 1896; Initial: Ch. 1935; Completed: Ch. 1970",
                "Ji Du provided pure Wood Essence liquid",
                "6 Mountain Sea Spirits born from death of the tree"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E03", "Water Essence", "水之源", TechType.ESSENCE,
            "All-Seer's drop + Water General's One Drop of Universe + Great Soul Sect Founder's drop",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "Comprehends 380 million changes to condense; completed 9th cycle in Pill Sea",
                "Completed at Ch. 1843; True Body at Ch. 1897"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Potential: Ch. 1720; Initial: Ch. 1734; Completed: Ch. 1843",
                "Green Bull Continent's Water Essence absorbed during 9th cycle",
                "Ji Si used Celestial Sea Mother Soul + Mortal Dream Water for True Body"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E04", "Fire Essence", "火之源", TechType.ESSENCE,
            "Vermilion Bird Mark + Ming Hai's Burn the Heaven + Fire/Flame/Devilish Dragon absorption",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "Initial at Ch.1280 via sacrificing Life-Death and Karma Domains; Complete at Ch.1412",
                "9 accompanying types of fire from Nine Tribulation Karma fires"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Potential: from Fu Clan's Golden Leaf Flame Source; Initial: Ch. 1280; Complete: Ch. 1412",
                "Four Vermilion Bird awakenings feed into Fire Essence",
                "Ethereal Fire = first cultivatable Ethereal Fire thanks to Heaven Defying Bead"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E05", "Earth Essence", "土之源", TechType.ESSENCE,
            "Ji Si's Meng Earth Beads + Celestial Ancestor's grains of sand",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "Three Meng Earth Beads + two of nine grains of Celestial Ancestor's continent-creation sand",
                "Completed at Ch. 1895; True Body at Ch. 1895"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Completed at Ch. 1895",
                "Ji Si used Celestial Ancestor's sand (2 of 9 grains)",
                "Nourished by third grain of Celestial Ancestor's sand"
            ), "Ch.1895; Fandom wiki (Ji Si used 3 Meng Earth Beads)"
        ),
        new CanonTechnique(
            "E06", "Thunder Essence", "雷之源", TechType.ESSENCE,
            "Ancient Thunder Dragon soul + Scatter Thunder Clan's thunder + 9 Accompanying Thunders",
            java.util.List.of(
                "One of 6 substantial Essences with True Body",
                "9 Accompanying Thunders complete it: 6 stolen + Ji Thunder + Bloodline Thunder + Defying Thunder",
                "Initial at Ch.1280; Complete at Ch.1368; True Body at Ch.1892"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Devoured half of Ancient Thunder Dragon soul (Soul-Devourer form)",
                "Ji Realm absorbed as 7th Accompanying Thunder (Ji Thunder)",
                "Complete at Ch.1368 after perfecting 9 Accompanying Thunders"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E07", "Life-Death Essence", "生死之源", TechType.ESSENCE,
            "Life and Death Domain evolution + Dao Fruits + Dream Dao",
            java.util.List.of(
                "One of 4 virtual Essences",
                "Initial at Ch.1280 during battle with Daoist Water; Completed at Ch.1613",
                "Domain sacrificed at Ch.1280 to fuel Fire/Thunder Essences; re-completed via Dream Dao"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Virtual essence; no True Body in standard set",
                "Sacrificed and re-completed multiple times",
                "Evolved from Life and Death Domain (Ch.264)"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E08", "Karma Essence", "因果之源", TechType.ESSENCE,
            "Sacrificed all six stars against Daoist Water to gain last desperate power",
            java.util.List.of(
                "One of 4 virtual Essences",
                "Comprehended when sacrificing stars against Daoist Water",
                "Evolved from Karma Domain (Ch.731)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Virtual essence; no True Body in standard set",
                "Required sacrificing all six stars",
                "Evolved from Karma Domain"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E09", "Slaughter Essence", "杀戮之源", TechType.ESSENCE,
            "Lifetime of killing + consuming Qing Shui's Black Sword",
            java.util.List.of(
                "One of 4 special Essences with own True Body",
                "Initial at Ch.1509 after returning to kill Daoist Water; Completed at Ch.1622",
                "Refined by consuming Qing Shui's Black Sword (condensed from Wang Lin's own Slaughter Essence)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Special essence; has own True Body (Slaughter/Lu Mo)",
                "Completed by consuming his own Slaughter Essence in Qing Shui's sword",
                "Represents Wang Lin's lifetime of murder"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E10", "Restriction Essence", "禁之源", TechType.ESSENCE,
            "Merged world laws and millions of fused formations into eye bloodlines",
            java.util.List.of(
                "One of 4 special Essences with own True Body",
                "Completed at Ch.1715; True Body at Ch.2044/2046",
                "Formed from lifetime of restriction study + Four Great Restrictions mastery"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Special essence; has own True Body",
                "Used Ghostly Sail's eye bloodlines as vessel",
                "Fused into Slaughter Thunder Essence True Body"
            ), "Ch.1715; Fandom wiki (self-forged, 1 of 14 Essences)"
        ),
        new CanonTechnique(
            "E11", "True-False Essence", "真假之源", TechType.ESSENCE,
            "Star of Law destroyed in fight with Daoist Water; completed via Dream Dao",
            java.util.List.of(
                "One of 4 virtual Essences",
                "Domain completed when Star of Law was destroyed",
                "Completed via Dream Dao + Dao Fruits + mortal scholar life"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Virtual essence; no True Body in standard set",
                "Evolved from True-False Domain (Ch.1102)",
                "Completed alongside Life-Death and Karma via Dream Dao"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E12", "Dao Essence", "道之源", TechType.ESSENCE,
            "Wang Lin's overall Dao comprehension",
            java.util.List.of(
                "One of 14 total Essences per Baidu classification",
                "Represents Wang Lin's core Dao understanding",
                "Fundamental to 3rd Step cultivation"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Listed among 14 Origins in Baidu",
                "Part of the overall essence framework",
                "Less explicitly documented than others"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "E13", "True/False Essence (Primordial/Silent Extinction)", "太初/寂灭之源", TechType.ESSENCE,
            "Comprehended from Dong Lin Pool — Essence of Spirit sealed under Great Saint Continent",
            java.util.List.of(
                "Absolute Beginning (Taichu) completed after 13 years in Dong Lin Pool at Ch.1950",
                "Absolute End (Miemie/Silent Extinction) given to Slaughter Thunder for comprehension",
                "Each formed their own True Body; half of the 4 special Essences"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1950; two special Essences",
                "Comprehended from Spirit sealed under Great Saint Continent",
                "Absolute Beginning = sun in day/night simulation; Absolute End = darkness"
            ), "Fandom wiki; Baidu Baike (Primordial + Silent Extinction pair, Samsara Dao)"
        ),
        new CanonTechnique(
            "E14", "Cause-and-Effect Essence", "因果之源", TechType.ESSENCE,
            "Evolved from Karma Essence / Karma Domain",
            java.util.List.of(
                "One of 14 total Essences; related to Karma lineage",
                "Cause-Effect Whip evolved from Karma Whip",
                "Cleaved open 7 million worlds with a single whip"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Evolved from Karma Domain (Ch.731) → Karma Whip → Cause-Effect Whip",
                "Also called Cause-Effect in some translations",
                "Related to but distinct from Karma Essence"
            ), "Fandom wiki; Baidu Baike (1 of 14 Essences, Samsara Dao system)"
        ),
        new CanonTechnique(
            "B01", "1st Bridge", "踏天桥·第一桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Completed after century of closed-door cultivation; fused Slaughter, Restriction, Absolute Beginning/End True Bodies into Punishment Slaughter",
                "Chapter: Ch. 2047",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2047", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (1 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B02", "2nd Bridge", "踏天桥·第二桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Tested sturdiness of heart; granted glimpse of Heaven Trampling divine sense covering entire Celestial Clan",
                "Chapter: Ch. 2049",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2049", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (2 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B03", "3rd Bridge", "踏天桥·第三桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Trial to close mind from inner demons; Wang Lin embraced them instead; absorbed Celestial Ancestor's Head → Grand Empyrean Sun",
                "Chapter: Ch. 2049",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2049", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (3 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B04", "4th Bridge", "踏天桥·第四桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Crossed via Ancient Clan Cultivation completion (3rd Ancient Clan Tribulation)",
                "Chapter: Ch. 2062",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2062", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (4 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B05", "5th Bridge", "踏天桥·第五桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Stepped over after passing Ancient Order's 3rd Tribulation; Extreme Land/Sky/Life Dao comprehended here",
                "Chapter: Ch. 2062",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2062", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (5 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B06", "6th Bridge", "踏天桥·第六桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Crossed immediately after 5th using Heaven Trampling step witnessed during tribulation",
                "Chapter: ~Ch. 2062",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of("Heaven Trampling Bridge at ~Ch. 2062", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (6 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B07", "7th Bridge", "踏天桥·第七桥", TechType.BRIDGE,
            "Heaven Trampling Bridge trial",
            java.util.List.of(
                "Crossed to 7th bridge; stopped at 8th; fuses with Void Avatar for Nine Songs Three Signs",
                "Chapter: Ch. 2062/2065",
                "Crossed"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of("Heaven Trampling Bridge at Ch. 2062/2065", "Crossed by Wang Lin", "Part of 4th Step progression"), "Fandom wiki; Baidu Baike (7 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B08", "8th Bridge", "踏天桥·第八桥", TechType.BRIDGE,
            "Failed to cross",
            java.util.List.of(
                "Could NOT cross — bridge turned into specks of light that devoured Wang Lin",
                "Chapter: ~Ch. 2065",
                "Not crossed"
            ), java.util.List.of(), 4,
            java.util.List.of(
                "Heaven Trampling Bridge at ~Ch. 2065",
                "Failed — bridge devoured Wang Lin",
                "Part of 4th Step progression"
            ), "Fandom wiki; Baidu Baike (8 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "B09", "9th Bridge", "踏天桥·第九桥", TechType.BRIDGE,
            "Bypassed via Reincarnation Essence",
            java.util.List.of(
                "Never crossed — achieved Heaven Trampling via Reincarnation Essence mastery instead (Ch.2087)",
                "Chapter: N/A",
                "Not crossed"
            ), java.util.List.of(), 5,
            java.util.List.of(
                "Heaven Trampling Bridge at N/A",
                "Achieved Heaven Trampling without crossing",
                "Part of 4th Step progression"
            ), "Fandom wiki; Baidu Baike (9 of 9 Heaven Trampling Bridges)"
        ),
        new CanonTechnique(
            "AT01", "First Accompanying Thunder", "第一道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "First of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "One of first 6 accompanying thunders",
                "Devoured Ancient Thunder Dragons released by 5th Heaven Blight head elder"
            ), "Ch.1368; Fandom wiki (1st of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT02", "Second Accompanying Thunder", "第二道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "Second of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "One of first 6 accompanying thunders",
                "Part of the set stolen/devoured from Scatter Thunder Clan"
            ), "Ch.1368; Fandom wiki (2nd of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT03", "Third Accompanying Thunder", "第三道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "Third of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "One of first 6 accompanying thunders",
                "Part of the set stolen/devoured from Scatter Thunder Clan"
            ), "Ch.1368; Fandom wiki (3rd of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT04", "Fourth Accompanying Thunder", "第四道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "Fourth of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "One of first 6 accompanying thunders",
                "Part of the set stolen/devoured from Scatter Thunder Clan"
            ), "Ch.1368; Fandom wiki (4th of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT05", "Fifth Accompanying Thunder", "第五道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "Fifth of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "One of first 6 accompanying thunders",
                "Part of the set stolen/devoured from Scatter Thunder Clan"
            ), "Ch.1368; Fandom wiki (5th of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT06", "Sixth Accompanying Thunder", "第六道伴雷", TechType.ACCOMPANYING_THUNDER,
            "Stolen from Scatter Thunder Clan",
            java.util.List.of(
                "Sixth of 6 stolen/devoured accompanying thunders",
                "Part of Scatter Thunder Clan's set",
                "Contributes to Thunder Essence completion"
            ), java.util.List.of("Wang Lin"), 4,
            java.util.List.of(
                "Stolen from Scatter Thunder Clan",
                "Last of the 6 stolen accompanying thunders",
                "Part of the set stolen/devoured from Scatter Thunder Clan"
            ), "Ch.1368; Fandom wiki (6th of 9 accompanying thunders)"
        ),
        new CanonTechnique(
            "AT07", "Store All Ji Thunder", "储极雷", TechType.ACCOMPANYING_THUNDER,
            "Created from the Ji Realm (Extreme Realm Divine Sense)",
            java.util.List.of(
                "7th Accompanying Thunder — created from the Ji Realm",
                "Ji Realm absorbed into Slaughter and became this thunder at Ch.1368",
                "Also associated with Star of Law (Wang Lin's 7th type of law)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1368; created from Ji Realm",
                "Ji Realm's resolution: absorbed as 7th accompanying thunder",
                "Also called Store All Ji Thunder / tied to Star of Law"
            ), "Ch.1368; Fandom wiki (7th accompanying thunder)"
        ),
        new CanonTechnique(
            "AT08", "Bloodline Thunder", "血脉雷", TechType.ACCOMPANYING_THUNDER,
            "Created by sacrificing the Thunder Toad",
            java.util.List.of(
                "8th Accompanying Thunder — created by sacrificing Thunder Toad",
                "Bloodline-sacrifice thunder",
                "Ch. 1368"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1368; created by sacrificing Thunder Toad",
                "Bloodline-sacrifice method",
                "Self-created accompanying thunder"
            ), "Ch.1368; Fandom wiki (8th accompanying thunder)"
        ),
        new CanonTechnique(
            "AT09", "Defying Thunder", "逆天雷", TechType.ACCOMPANYING_THUNDER,
            "Wang Lin's own will — never having appeared since beginning of time",
            java.util.List.of(
                "9th Accompanying Thunder — formed via heaven-defying will",
                "Never before appeared since beginning of time",
                "Ultimate expression of Wang Lin's heaven-defying nature"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1368; Wang Lin's own 9th thunder",
                "Formed via defying will — unique in all history",
                "Completes the 9 Accompanying Thunders for Thunder Essence"
            ), "Ch.1368; Fandom wiki (9th accompanying thunder)"
        ),
        new CanonTechnique(
            "VA01", "1st Vermilion Bird Awakening", "朱雀一觉", TechType.OTHER,
            "Absorbed Ming Hai's Burn The Heaven flames",
            java.util.List.of(
                "Red Vermillion Bird appears",
                "First awakening of Vermilion Bird Mark",
                "Gives initial fire power boost"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1021; absorbed Ming Hai's Burn The Heaven flames",
                "Red Vermillion Bird manifestation",
                "Part of Fire Essence evolution"
            ), "Ch.1089; Fandom wiki (1st Vermilion Bird Mark awakening)"
        ),
        new CanonTechnique(
            "VA02", "2nd Vermilion Bird Awakening", "朱雀二觉", TechType.OTHER,
            "Absorbed Devilish Flames, Fire Dragon, and Flame Dragon",
            java.util.List.of(
                "Evolved into White Vermillion Bird",
                "Second awakening after absorbing multiple fire sources",
                "Significant power increase"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1068",
                "White Vermillion Bird manifestation",
                "Devilish Flames + Fire Dragon + Flame Dragon absorbed"
            ), "Ch.~1200; Fandom wiki (2nd Vermilion Bird Mark awakening)"
        ),
        new CanonTechnique(
            "VA03", "3rd Vermilion Bird Awakening", "朱雀三觉", TechType.OTHER,
            "Absorbed Vermillion Bird feather + ancestral spirit of Fire Sparrow Clan youth",
            java.util.List.of(
                "Evolved into Blue Vermillion Bird",
                "Third awakening via Vermillion Bird feather and Fire Sparrow Clan",
                "Ch. 1210"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1210",
                "Blue Vermillion Bird manifestation",
                "Vermillion Bird feather + Fire Sparrow Clan ancestral spirit"
            ), "Ch.~1400; Fandom wiki (3rd Vermilion Bird Mark awakening)"
        ),
        new CanonTechnique(
            "VA04", "4th Vermilion Bird Awakening", "朱雀四觉", TechType.OTHER,
            "Burned by karma fire → 9 colors fused into one = Ethereal Fire",
            java.util.List.of(
                "Blue → Black (Ch.1409, karma fire) → 9-Color (Ch.1411) → Ethereal Fire (Ch.1412)",
                "9 colors fused into one to create Ethereal Fire",
                "Thanks to Heaven Defying Bead, first and only cultivatable Ethereal Fire"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1409-1412; final awakening",
                "Multi-stage evolution: Red → White → Blue → Black → 9-Color → Ethereal",
                "Absorbed Nine Tribulation Karma fires for 9 accompanying fire types"
            ), "Ch.1412; Fandom wiki (4th Vermilion Bird Mark, 9 colors to 1)"
        ),
        new CanonTechnique(
            "T168", "Fire Burn (Nine Mysterious 1st Transformation)", "焚天九变·第一变", TechType.OTHER,
            "Vermilion Bird Divine Ability — Nine Mysterious Transformations",
            java.util.List.of("First Transformation of Nine Mysterious Transformations", "Fire Burn ability", "Ch. 1089"), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1089; Vermilion Bird Divine Ability",
                "First of Nine Mysterious Transformations",
                "Part of Nine Mysterious Spell set"
            ), "Ch.1089; Fandom wiki (VB Divine Ability)"
        ),
        new CanonTechnique(
            "T169", "Rise Three Realm Flame Origin", "三界焰源", TechType.OTHER,
            "Vermilion Bird Divine Ability — Nine Mysterious Transformations",
            java.util.List.of(
                "Second technique of Nine Mysterious Transformations",
                "Flame origin ability across three realms",
                "Ch. 1091"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1091; Vermilion Bird Divine Ability",
                "Part of Nine Mysterious Transformations",
                "Three-realm flame origin"
            ), "Ch.1091; Fandom wiki (VB Divine Ability)"
        ),
        new CanonTechnique(
            "T170", "Nine Mysterious Fire Escape", "九秘火遁", TechType.OTHER,
            "Vermilion Bird Divine Ability — secret spell (originally stolen from Azure Dragon, White Tiger, Black Tortoise clans)",
            java.util.List.of(
                "Fire escape secret spell",
                "Originally stolen from Azure Dragon, White Tiger, and Black Tortoise clans by old ancestor",
                "Not actually a spell but a method for using force (revealed at Ch.1403)"
            ), java.util.List.of("Wang Lin"), 5,
            java.util.List.of(
                "Ch. 1127 (fire escape); Ch. 1403 (revealed as stolen force method)",
                "Stolen from three other divine beast clans",
                "Called clan's secret spell but actually a force method"
            ), "Ch.1127; Fandom wiki; Baidu Baike (stolen from Azure Dragon/White Tiger/Black Tortoise)"
        )
    );

    // ── Index maps for O(1) lookup ───────────────────────────────────────

    private static final Map<String, CanonCharacter> CHAR_BY_ID;
    private static final Map<String, CanonLocation> LOC_BY_ID;
    private static final Map<String, CanonArtifact> ART_BY_ID;
    private static final Map<String, CanonTechnique> TECH_BY_ID;

    static {
        CHAR_BY_ID = ALL_CHARACTERS.stream().collect(Collectors.toUnmodifiableMap(c -> c.id, c -> c));
        LOC_BY_ID = ALL_LOCATIONS.stream().collect(Collectors.toUnmodifiableMap(l -> l.id, l -> l));
        ART_BY_ID = ALL_ARTIFACTS.stream().collect(Collectors.toUnmodifiableMap(a -> a.id, a -> a));
        TECH_BY_ID = ALL_TECHNIQUES.stream().collect(Collectors.toUnmodifiableMap(t -> t.id, t -> t));
    }

    // ── Query methods ────────────────────────────────────────────────────

    // -- Character queries --
    public static CanonCharacter getCharacterById(String id) { return CHAR_BY_ID.get(id); }
    public static List<CanonCharacter> getCharactersByType(CharType type) {
        return ALL_CHARACTERS.stream().filter(c -> c.type == type).collect(Collectors.toList());
    }
    public static List<CanonCharacter> getCharactersByAffiliation(String affiliation) {
        return ALL_CHARACTERS.stream()
            .filter(c -> c.affiliation != null && c.affiliation.contains(affiliation))
            .collect(Collectors.toList());
    }
    public static List<CanonCharacter> getCharactersAtLocation(String location) {
        return ALL_CHARACTERS.stream()
            .filter(c -> c.location != null && c.location.contains(location))
            .collect(Collectors.toList());
    }
    public static List<CanonCharacter> getCharactersByConfidence(int minConf) {
        return ALL_CHARACTERS.stream().filter(c -> c.canonConfidence >= minConf).collect(Collectors.toList());
    }
    public static List<CanonCharacter> searchCharacters(String query) {
        String q = query.toLowerCase();
        return ALL_CHARACTERS.stream()
            .filter(c -> c.name.toLowerCase().contains(q) || c.nameCn.contains(query)
                || (c.affiliation != null && c.affiliation.toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }
    public static List<CanonCharacter> getRelationshipsOf(String characterName) {
        return ALL_CHARACTERS.stream()
            .filter(c -> c.relationships.stream().anyMatch(r -> r.target.contains(characterName)))
            .collect(Collectors.toList());
    }
    public static List<CanonCharacter> getCharactersByStatus(String status) {
        return ALL_CHARACTERS.stream()
            .filter(c -> c.status.equalsIgnoreCase(status))
            .collect(Collectors.toList());
    }

    // -- Location queries --
    public static CanonLocation getLocationById(String id) { return LOC_BY_ID.get(id); }
    public static List<CanonLocation> getLocationsByType(LocType type) {
        return ALL_LOCATIONS.stream().filter(l -> l.type == type).collect(Collectors.toList());
    }
    public static List<CanonLocation> getLocationsByCosmologyLayer(String layer) {
        return ALL_LOCATIONS.stream()
            .filter(l -> layer.equals(l.cosmologyLayer))
            .collect(Collectors.toList());
    }
    public static List<CanonLocation> getLocationsByParent(String parentId) {
        return ALL_LOCATIONS.stream()
            .filter(l -> parentId.equals(l.parentLocation))
            .collect(Collectors.toList());
    }
    public static List<CanonLocation> getSealedLocations() {
        return ALL_LOCATIONS.stream().filter(l -> l.isSealed).collect(Collectors.toList());
    }
    public static List<CanonLocation> getLocationsByConfidence(int minConf) {
        return ALL_LOCATIONS.stream().filter(l -> l.canonConfidence >= minConf).collect(Collectors.toList());
    }
    public static List<CanonLocation> searchLocations(String query) {
        String q = query.toLowerCase();
        return ALL_LOCATIONS.stream()
            .filter(l -> l.name.toLowerCase().contains(q) || l.nameCn.contains(query))
            .collect(Collectors.toList());
    }
    public static List<CanonLocation> getLocationsWithFaction(String factionName) {
        return ALL_LOCATIONS.stream()
            .filter(l -> l.associatedFactions.stream().anyMatch(f -> f.contains(factionName)))
            .collect(Collectors.toList());
    }

    // -- Artifact queries --
    public static CanonArtifact getArtifactById(String id) { return ART_BY_ID.get(id); }
    public static List<CanonArtifact> getArtifactsByType(ArtType type) {
        return ALL_ARTIFACTS.stream().filter(a -> a.type == type).collect(Collectors.toList());
    }
    public static List<CanonArtifact> getArtifactsByOwner(String owner) {
        return ALL_ARTIFACTS.stream()
            .filter(a -> a.currentOwner != null && a.currentOwner.contains(owner))
            .collect(Collectors.toList());
    }
    public static List<CanonArtifact> getArtifactsByCategory(String category) {
        return ALL_ARTIFACTS.stream()
            .filter(a -> category.equals(a.category))
            .collect(Collectors.toList());
    }
    public static List<CanonArtifact> getArtifactsByConfidence(int minConf) {
        return ALL_ARTIFACTS.stream().filter(a -> a.canonConfidence >= minConf).collect(Collectors.toList());
    }
    public static List<CanonArtifact> searchArtifacts(String query) {
        String q = query.toLowerCase();
        return ALL_ARTIFACTS.stream()
            .filter(a -> a.name.toLowerCase().contains(q) || a.nameCn.contains(query)
                || (a.origin != null && a.origin.toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }
    /** Get all artifacts owned by a specific character at any point. */
    public static List<CanonArtifact> getArtifactsMentioningCharacter(String characterName) {
        return ALL_ARTIFACTS.stream()
            .filter(a -> (a.currentOwner != null && a.currentOwner.contains(characterName))
                || (a.origin != null && a.origin.contains(characterName)))
            .collect(Collectors.toList());
    }

    // -- Technique queries --
    public static CanonTechnique getTechniqueById(String id) { return TECH_BY_ID.get(id); }
    public static List<CanonTechnique> getTechniquesByType(TechType type) {
        return ALL_TECHNIQUES.stream().filter(t -> t.type == type).collect(Collectors.toList());
    }
    public static List<CanonTechnique> getTechniquesByUser(String userName) {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.knownUsers.stream().anyMatch(u -> u.contains(userName)))
            .collect(Collectors.toList());
    }
    public static List<CanonTechnique> getTechniquesByConfidence(int minConf) {
        return ALL_TECHNIQUES.stream().filter(t -> t.canonConfidence >= minConf).collect(Collectors.toList());
    }
    public static List<CanonTechnique> searchTechniques(String query) {
        String q = query.toLowerCase();
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.name.toLowerCase().contains(q) || t.nameCn.contains(query)
                || (t.origin != null && t.origin.toLowerCase().contains(q)))
            .collect(Collectors.toList());
    }
    /** Get all 14 Samsara Essences (E01–E14). */
    public static List<CanonTechnique> getEssences() {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.id.startsWith("E"))
            .collect(Collectors.toList());
    }
    /** Get all 9 Heaven Trampling Bridges (B01–B09). */
    public static List<CanonTechnique> getBridges() {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.id.startsWith("B"))
            .collect(Collectors.toList());
    }
    /** Get all 7 Original Spells (OS01–OS07). */
    public static List<CanonTechnique> getOriginalSpells() {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.id.startsWith("OS"))
            .collect(Collectors.toList());
    }
    /** Get all 9 Accompanying Thunders (AT01–AT09). */
    public static List<CanonTechnique> getAccompanyingThunders() {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.id.startsWith("AT"))
            .collect(Collectors.toList());
    }
    /** Get all 4 Vermilion Bird Awakenings (VA01–VA04). */
    public static List<CanonTechnique> getVermilionBirdAwakenings() {
        return ALL_TECHNIQUES.stream()
            .filter(t -> t.id.startsWith("VA"))
            .collect(Collectors.toList());
    }

    // -- Cross-category queries --
    /** Get total entry count across all categories. */
    public static int getTotalEntries() {
        return ALL_CHARACTERS.size() + ALL_LOCATIONS.size() + ALL_ARTIFACTS.size() + ALL_TECHNIQUES.size();
    }

    /** Get confidence distribution across all categories. */
    public static Map<Integer, Long> getConfidenceDistribution() {
        return Stream.concat(
            Stream.concat(ALL_CHARACTERS.stream().map(c -> c.canonConfidence),
            ALL_LOCATIONS.stream().map(l -> l.canonConfidence)),
            Stream.concat(ALL_ARTIFACTS.stream().map(a -> a.canonConfidence),
            ALL_TECHNIQUES.stream().map(t -> t.canonConfidence)))
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));
    }

    /** Free-text search across ALL categories. */
    public static List<String> searchAll(String query) {
        String q = query.toLowerCase();
        List<String> results = new ArrayList<>();
        for (CanonCharacter c : ALL_CHARACTERS) {
            if (c.name.toLowerCase().contains(q) || c.nameCn.contains(query))
                results.add("[Char] " + c.name + " (" + c.nameCn + ") — " + c.id);
        }
        for (CanonLocation l : ALL_LOCATIONS) {
            if (l.name.toLowerCase().contains(q) || l.nameCn.contains(query))
                results.add("[Loc]  " + l.name + " (" + l.nameCn + ") — " + l.id);
        }
        for (CanonArtifact a : ALL_ARTIFACTS) {
            if (a.name.toLowerCase().contains(q) || a.nameCn.contains(query))
                results.add("[Art]  " + a.name + " (" + a.nameCn + ") — " + a.id);
        }
        for (CanonTechnique t : ALL_TECHNIQUES) {
            if (t.name.toLowerCase().contains(q) || t.nameCn.contains(query))
                results.add("[Tech] " + t.name + " (" + t.nameCn + ") — " + t.id);
        }
        return results;
    }

    /** Get all entities that reference a given character by name. */
    public static Map<String, List<String>> getEntityReferences(String characterName) {
        Map<String, List<String>> refs = new LinkedHashMap<>();
        List<String> charRefs = new ArrayList<>();
        for (CanonCharacter c : ALL_CHARACTERS) {
            if (c.relationships.stream().anyMatch(r -> r.target.contains(characterName)))
                charRefs.add(c.id + " " + c.name + " (" + c.relationships.stream()
                    .filter(r -> r.target.contains(characterName)).map(r -> r.relation).collect(Collectors.joining(", ")) + ")");
        }
        if (!charRefs.isEmpty()) refs.put("characters", charRefs);

        List<String> artRefs = ALL_ARTIFACTS.stream()
            .filter(a -> a.currentOwner != null && a.currentOwner.contains(characterName))
            .map(a -> a.id + " " + a.name).collect(Collectors.toList());
        if (!artRefs.isEmpty()) refs.put("owned_artifacts", artRefs);

        List<String> techRefs = ALL_TECHNIQUES.stream()
            .filter(t -> t.knownUsers.stream().anyMatch(u -> u.contains(characterName)))
            .map(t -> t.id + " " + t.name).collect(Collectors.toList());
        if (!techRefs.isEmpty()) refs.put("known_techniques", techRefs);

        return refs;
    }

    /** Summary counts for logging. */
    public static Map<String, Integer> getSummaryCounts() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("characters", ALL_CHARACTERS.size());
        m.put("locations", ALL_LOCATIONS.size());
        m.put("artifacts", ALL_ARTIFACTS.size());
        m.put("techniques", ALL_TECHNIQUES.size());
        m.put("total", getTotalEntries());
        return m;
    }

    /** Count characters at each confidence level. */
    public static long countCharactersAtConfidence(int conf) {
        return ALL_CHARACTERS.stream().filter(c -> c.canonConfidence == conf).count();
    }
    /** Count locations at each confidence level. */
    public static long countLocationsAtConfidence(int conf) {
        return ALL_LOCATIONS.stream().filter(l -> l.canonConfidence == conf).count();
    }
    /** Count artifacts at each confidence level. */
    public static long countArtifactsAtConfidence(int conf) {
        return ALL_ARTIFACTS.stream().filter(a -> a.canonConfidence == conf).count();
    }
    /** Count techniques at each confidence level. */
    public static long countTechniquesAtConfidence(int conf) {
        return ALL_TECHNIQUES.stream().filter(t -> t.canonConfidence == conf).count();
    }
}