package dev.ergenverse.wanglin;

import dev.ergenverse.core.Ergenverse;

import java.util.*;

/**
 * RI Timeline Engine — the spine of Renegade Immortal's history.
 *
 * <p><b>Source:</b> CANON_RI_TIMELINE.md (1,123 lines, 108 events, 11 eras).
 * Every event is attested in at least one of:
 * <ul>
 *   <li>CANON_RI_COMPLETE_WORLD.md (3,034 lines)</li>
 *   <li>CANON_RI_COMPLETE_ITEMS.md (2,019 lines)</li>
 *   <li>CANON_RI_COMPLETE_TECHNIQUES.md (1,793 lines)</li>
 * </ul>
 *
 * <p><b>Era notation:</b>
 * <ul>
 *   <li>{@code -N} = N years before Wang Lin's birth (BW)</li>
 *   <li>{@code +N} = N years after Wang Lin's birth (year 0 = birth)</li>
 *   <li>Wang Lin's lifespan spans ~2,087 chapters; Heaven Trampling at ~age 2000+</li>
 * </ul>
 *
 * <p><b>Canon confidence:</b>
 * <ul>
 *   <li>C5 — explicit chapter citation in novel</li>
 *   <li>C4 — wiki-attested, no specific chapter</li>
 *   <li>C3 — strongly implied by canon context</li>
 *   <li>C2 — bridging (canon-silent; logically necessary)</li>
 * </ul>
 *
 * <p><b>Per the Prime Directive:</b> every event is OBJECTIVE. Whether the
 * player knows about it or not, it happened. "Remaining traces" tells the
 * mod what the player CAN discover in the world at any given time.
 *
 * <p><b>Per user correction #5:</b> events are NOT tied to hardcoded realm
 * gates. The era boundaries are descriptive, not prescriptive.
 */
public final class RITimelineEngine {

    private RITimelineEngine() {}

    // ─── Canon Confidence ────────────────────────────────────────────

    public enum CanonConfidence {
        C5(5, "Explicit chapter citation"),
        C4(4, "Wiki-attested, no specific chapter"),
        C3(3, "Strongly implied by canon context"),
        C2(2, "Bridging — canon-silent, logically necessary");

        public final int level;
        public final String description;

        CanonConfidence(int level, String description) {
            this.level = level; this.description = description;
        }
    }

    // ─── Era ──────────────────────────────────────────────────────────

    public enum Era {
        ANCIENT_ERA             (1,  "The Ancient Era",                   "\u53E4\u4EE3",        "before \u2212100,000 BW"),
        EARLY_LIFE              (2,  "Wang Lin\u2019s Early Life",          "\u738B\u6797\u65E9\u5E74",   "0 to +16 BW"),
        ZHAO_COUNTRY_ARC        (3,  "The Zhao Country Arc",              "\u8D75\u56FD\u7BC7",      "Year 16 to ~25"),
        SUZAKU_INHERITANCE_ARC  (4,  "The Suzaku Inheritance Arc",        "\u6731\u96C0\u4F20\u627F\u7BC7", "Year ~25 to ~100"),
        HEAVENLY_FATE_ARC       (5,  "The Heavenly Fate Sect Arc",        "\u5929\u8FD0\u5B97\u7BC7",   "Year ~100 to ~300"),
        ALLHEAVEN_ARC           (6,  "The Allheaven Star System Arc",     "\u5929\u5802\u661F\u7CFB\u7BC7", "Year ~300 to ~500"),
        CLOUD_SEA_ARC           (7,  "The Cloud Sea Star System Arc",      "\u4E91\u6D77\u661F\u7CFB\u7BC7", "Year ~500 to ~800"),
        CAVE_WORLD_REVELATION   (8,  "The Cave World Revelation",         "\u6D1E\u5929\u771F\u76F8\u7BC7", "Year ~800 to ~1500"),
        IAC_ARC                 (9,  "The Immortal Astral Continent Arc",  "\u4ED9\u7FBD\u5927\u9646\u7BC7", "Year ~1500 to ~2000"),
        TRANSCENDENCE           (10, "Transcendence",                      "\u8D85\u8131\u7BC7",      "Year ~2000+"),
        POST_RI                 (11, "Post-RI Cross-Novel",               "\u8DE8\u5C0F\u8BF4\u7BC7",   "after Year ~2000+");

        public final int number;
        public final String name;
        public final String nameCn;
        public final String timeSpan;

        Era(int number, String name, String nameCn, String timeSpan) {
            this.number = number; this.name = name; this.nameCn = nameCn;
            this.timeSpan = timeSpan;
        }
    }

    // ─── Timeline Event ───────────────────────────────────────────────

    public static final class TimelineEvent {
        public final String id;               // "E01" through "E108"
        public final String title;
        public final String titleCn;           // if available
        public final Era era;
        public final String date;             // free-form: "before time", "~\u2212100,000 BW", "+16", "~100", etc.
        public final String location;
        public final String participants;     // comma-separated character names
        public final String consequences;     // what changed
        public final String remainingTraces;  // what the player can find
        public final CanonConfidence canonConfidence;

        public TimelineEvent(String id, String title, String titleCn, Era era, String date,
                             String location, String participants, String consequences,
                             String remainingTraces, CanonConfidence canonConfidence) {
            this.id = id; this.title = title; this.titleCn = titleCn; this.era = era;
            this.date = date; this.location = location; this.participants = participants;
            this.consequences = consequences; this.remainingTraces = remainingTraces;
            this.canonConfidence = canonConfidence;
        }
    }

    // ─── All 39 Canon Events ──────────────────────────────────────────

    public static final List<TimelineEvent> ALL_EVENTS = Collections.unmodifiableList(Arrays.asList(
        // ── ERA 1: The Ancient Era ──
        new TimelineEvent("E01", "The Root Dao Exists", "\u672C\u6E90\u5927\u9053\u5B58\u5728", Era.ANCIENT_ERA,
            "before time", "The Root Dao (\u672C\u6E90\u5927\u9053)",
            "none (substrate, not actor)",
            "All Daos (Five Elements, Karma, Reincarnation, Life-Death, True-False, Absolute Beginning/End, Restriction, Slaughter) exist as fundamental laws.",
            "Accessible only at Transcendence. Wang Lin comprehends his 14th Essence (Reincarnation) here.",
            CanonConfidence.C4),
        new TimelineEvent("E02", "The Seven-Colored Daoist Creates the Cave World", "\u4E03\u8272\u9053\u4EBA\u521B\u9020\u6D1E\u5929", Era.ANCIENT_ERA,
            "~\u2212100,000 BW", "The Cave World (\u6D1E\u5929)",
            "Seven-Colored Daoist (Paragon)",
            "Artificial pocket dimension created as a Joss Flame farm. All Daos seeded inside.",
            "The Cave World itself IS the remaining trace. Every mortal within generates Joss Flame for the owner. 108 Seven-Colored Divine Sky Nails embedded.",
            CanonConfidence.C4),
        new TimelineEvent("E03", "The Seven-Colored Daoist Creates the Three Souls and Seven Spirits", null, Era.ANCIENT_ERA,
            "~\u2212100,000 BW", "The Cave World",
            "Seven-Colored Daoist",
            "The Three Souls and Seven Spirits (三魂七魄) structure imposed on the Cave World's spiritual framework. Foundation of all soul-cultivation within.",
            "All soul-related techniques in the Cave World derive from this structure.",
            CanonConfidence.C4),
        new TimelineEvent("E04", "The Realm-Sealing Supreme and the Heaven-Defying Bead", null, Era.ANCIENT_ERA,
            "~\u221250,000 BW", "The Cave World / outside",
            "Realm-Sealing Supreme (sealed spirit of Heaven-Splitting Axe)",
            "The Heaven-Splitting Axe (天裂斧) used as spirit for the Realm-Sealing Grand Array. The Heaven-Defying Bead (9 parts) enters the Cave World.",
            "Heaven-Splitting Axe = spirit of the seal (canon, C5). Heaven-Defying Bead = cross-novel artifact (C4).",
            CanonConfidence.C4),
        new TimelineEvent("E05", "The Realm-Sealing Grand Array is Placed", "\u5C01\u754C\u5927\u9635\u5E03\u7F6E", Era.ANCIENT_ERA,
            "~\u221250,000 BW", "Boundary between Sealed Realm and Outer Realm",
            "Realm-Sealing Supreme (via Heaven-Splitting Axe)",
            "Cave World split into Sealed Realm (inner, Third-Step suppressed) and Outer Realm (outer, higher tiers). Caps cultivation at Heaven Blight (tier 10) inside Sealed Realm.",
            "The Array IS still active (until Wang Lin breaks it). Every cultivator in the Sealed Realm is affected whether they know it or not.",
            CanonConfidence.C5),
        new TimelineEvent("E06", "The Tu Si Ancient God Era", "\u56FE\u601D\u53E4\u795E\u65F6\u4EE3", Era.ANCIENT_ERA,
            "~\u221210,000 BW", "The Cave World (widespread)",
            "Tu Si (8-star Ancient God)",
            "Tu Si's legacy spreads across the Cave World. Ancient God Tactic = plunder. Tu Si's corpse becomes a spirit vein in the Ancient Battlefield.",
            "Tu Si's corpse IS the spirit vein in the Ancient Battlefield (C5). Tuo Sen (Tu Si's demonic thought) later awakens from the legacy.",
            CanonConfidence.C5),
        new TimelineEvent("E07", "The Bai Fan Era", "\u767D\u7E41\u65F6\u4EE3", Era.ANCIENT_ERA,
            "~\u22121,000 BW", "The Cave World",
            "Bai Fan (Six Paths Triple Arts creator)",
            "Bai Fan creates the Six Paths Triple Arts. His Collection Pavilion later discovered by Wang Lin. An era of powerful cultivators.",
            "Bai Fan's Collection Pavilion (Ch.784). Mountain Crumble spell (divine-sense mountain manipulation). Later gifted by Wang Lin to friends.",
            CanonConfidence.C4),

        // ── ERA 2: Wang Lin's Early Life ──
        new TimelineEvent("E40", "Situ Nan Is Betrayed — Physical Body Destroyed", null, Era.ANCIENT_ERA,
            "~−5000 BW",
            "Vermilion Bird Starfield",
            "Situ Nan (2nd-Gen Vermilion Bird), Third-Gen Vermilion Bird Master, Tan Lang",
            "After Situ Nan sealed himself using the Cultivation Star Crystal, the Third-Gen Vermilion Bird and Tan Lang launched a sneak attack, destroying his physical body. His remnant soul fled into the Heaven-Defying Bead. The 14th-Gen Vermilion Bird later severed three of his own fingers and fled.",
            "Situ Nan’s remnant soul is inside the Heaven-Defying Bead when Wang Lin finds it. The Vermilion Bird lineage is fractured.",
            CanonConfidence.C4),
        new TimelineEvent("E08", "Wang Lin is Born", "\u738B\u6797\u51FA\u751F", Era.EARLY_LIFE,
            "+0", "Wang Family Village, Country of Zhao, Planet Suzaku",
            "Wang Tianshui (father), Zhou Tingsu (mother)",
            "The protagonist enters the world. Mortal birth. No cultivation yet.",
            "Wang Family Village exists as a location. No special traces at this point.",
            CanonConfidence.C5),
        new TimelineEvent("E09", "Wang Lin's Childhood", null, Era.EARLY_LIFE,
            "+1 to +15", "Wang Family Village",
            "Wang Lin, Wang Tianshui, Zhou Tingsu",
            "Wang Lin grows up as a mortal carpenter's son. Shows no special talent. This normalcy is important — it grounds his Dao.",
            "The village, the carpentry tradition, the family bonds — all form the emotional foundation for Wang Lin's later Life-Death and Reincarnation Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E10", "The Heng Yue Sect Recruitment", "\u6052\u5CB3\u6D3E\u62DB\u751F", Era.EARLY_LIFE,
            "+16", "Heng Yue Sect, Country of Zhao",
            "Wang Lin, Wang Zhou, Wang Jie, Huang Long Zhenren (Lu Yun)",
            "Wang Lin fails all 3 entrance tests. Becomes a legacy disciple. Huang Long Zhenren is secretly 5th-Gen Vermilion Bird Lu Yun.",
            "Heng Yue Sect mountain exists. The Heaven-Defying Bead is found on this mountain inside a dead bird (C5).",
            CanonConfidence.C5),

        // ── ERA 3: The Zhao Country Arc ──
        new TimelineEvent("E11", "Wang Lin Discovers the Stone Bead", "\u53D1\u73B0\u78F7\u74F6", Era.ZHAO_COUNTRY_ARC,
            "~+16", "Heng Yue Sect cliff, Country of Zhao",
            "Wang Lin",
            "The Heaven-Defying Bead (1 of 9 parts) enters Wang Lin's possession. 10\u00d7 time dilation interior. Li Muwan's Nascent Soul later stored here.",
            "The bead is THE defining artifact of RI. It drives the entire plot. Interior contains 5 Elements pattern + Li Muwan's soul.",
            CanonConfidence.C5),
        new TimelineEvent("E12", "Situ Nan Awakens, Transmits the Underworld Ascension Method", null, Era.ZHAO_COUNTRY_ARC,
            "~+17", "Wang Lin's possession (inside bead?)",
            "Wang Lin, Situ Nan (2nd-Gen Vermilion Bird)",
            "Situ Nan (2nd-Gen VB, Green Soul of Seven-Colored Immortal Venerable) awakens and teaches Wang Lin the Underworld Ascension Method. Wang Lin's cultivation begins.",
            "Underworld Ascension Method = Cold Dan path to Golden Core. Situ Nan's consciousness inside the bead.",
            CanonConfidence.C5),
        new TimelineEvent("E13", "The Teng Clan Massacre of the Wang Family", "\u85E4\u65CF\u706D\u738B\u5BB6\u6751", Era.ZHAO_COUNTRY_ARC,
            "~+20", "Wang Family Village, Country of Zhao",
            "Teng Huayuan (Half-Step Deity Transformation), Wang Family, Wang Lin (not present)",
            "Teng Huayuan exterminates Wang Family Village. Wang Lin's parents and relatives killed. This is the inciting tragedy of Wang Lin's journey.",
            "Wang Family Village becomes a ruin with lingering Yin energy. Wang Lin later returns as Soul Formation to bury the dead. The emotional scar drives his Slaughter Essence.",
            CanonConfidence.C5),
        new TimelineEvent("E14", "Wang Lin's Revenge on the Teng Clan", "\u738B\u6797\u590D\u4EC7\u85E4\u65CF", Era.ZHAO_COUNTRY_ARC,
            "~+22 to ~+60", "Teng Family City, Country of Zhao",
            "Wang Lin, Teng Huayuan, Teng One-Nine, Teng Li",
            "'Kill and Destroy the Heart' revenge. Wang Lin hunts all Teng descendants. Builds human-head tower. All 9 Nascent Soul cultivators killed and refined into demon-puppets. Teng Huayuan slain.",
            "Teng Family City = depleted ruin. Core Formation-tier resentful spirits. Demon-puppet remnants. The revenge is complete — no Teng Clan survivors.",
            CanonConfidence.C5),
        new TimelineEvent("E15", "Core Formation in the Sea of Devils", "\u6D77\u9B54\u6D1E\u91D1\u4E39", Era.ZHAO_COUNTRY_ARC,
            "~+60", "Sea of Devils, Planet Suzaku",
            "Wang Lin, Fighting Evil Sect (10 Core Formation pursuers)",
            "Wang Lin breaks through to Core Formation while being chased by 10 Core Formation cultivators. Later kills the Fighting Evil Sect Leader.",
            "Fighting Evil Sect massacre site (Core Formation-tier resentful spirits). Nan Dou City (trade hub). Corpse Yin Sect territory nearby.",
            CanonConfidence.C5),

        // ── ERA 4: The Suzaku Inheritance Arc ──
        new TimelineEvent("E41", "Wang Lin Awakens the Ji Realm", "王林觉醒极境", Era.ZHAO_COUNTRY_ARC,
            "~+17",
            "Zhao Country",
            "Wang Lin",
            "Spiritual energy transforms into Ji Realm (Extreme Realm) divine sense, triggered by trauma of Wang Clan annihilation + Underworld Ascension Method. Produces Blue Flames. Later conflicts with Nascent Soul breakthrough. Eventually absorbed into Ji Thunder as 7th accompanying thunder.",
            "Ji Realm divine sense persists throughout Wang Lin’s career.",
            CanonConfidence.C5),
        new TimelineEvent("E42", "Wang Lin Discovers His Soul-Devourer Nature", null, Era.ZHAO_COUNTRY_ARC,
            "~+17",
            "Zhao Country",
            "Wang Lin",
            "Discovers innate Soul-Devourer nature. His soul contains a Soul Gem, granting command over wandering souls. Foundation of soul-based combat style and ‘Soul Devourer’ title. Soul Gem later devours Ancient Thunder Dragon’s second origin soul (Ch. 650), absorbs Heavenly Flame (Ch. 1021).",
            "Wandering souls in the Sea of Devils and beyond still respond to Soul-Devourers.",
            CanonConfidence.C5),
        new TimelineEvent("E43", "Wang Lin Passes the Restriction Mountain Trial", null, Era.ZHAO_COUNTRY_ARC,
            "~+19-22",
            "Land of the Ancient God — Restriction Mountain",
            "Wang Lin, Tu Si’s legacy",
            "Spends 7 years studying ancient restrictions. Becomes only the 4th person to complete the trial. Develops Illusionary Circle technique. Receives Restriction Flag Refining Method (99,999 restrictions per flag). Hair turns white. Foundation of lifelong restriction specialization.",
            "The Restriction Mountain still exists as a trial site. The Illusionary Circle is Wang Lin’s signature analysis technique.",
            CanonConfidence.C5),
        new TimelineEvent("E16", "The Foreign Battleground and Soul Devourer", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+60 to ~+80", "Foreign Battleground / Ancient Battlefield",
            "Wang Lin, Soul Devourer, Tu Si (corpse)",
            "Wang Lin enters the Foreign Battleground. Encounters the Soul Devourer. Tu Si's corpse IS the spirit vein of the Ancient Battlefield.",
            "Ancient Battlefield = 3 levels (hurricane of devils / Restriction Mountain + Bridge of No Return / Annihilation realm + Tuo Sen). Tu Si's legacy active.",
            CanonConfidence.C5),
        new TimelineEvent("E17", "Inheriting Tu Si's Ancient God Legacy", "\u7EE7\u627F\u56FE\u601D\u53E4\u795E\u9057\u4EA7", Era.SUZAKU_INHERITANCE_ARC,
            "~+80", "Ancient Battlefield / Tu Si's corpse",
            "Wang Lin, Tu Si (8-star Ancient God legacy), Tuo Sen (demonic thought born)",
            "Wang Lin inherits Tu Si's Ancient God legacy. Tuo Sen (Tu Si's demonic thought, born from failed Ink Flow Split Soul Technique) awakens as a recurring antagonist.",
            "Ancient God Tactic = plunder. Tuo Sen persists as an antagonist through the mid-game. Wang Lin's parallel Ancient God body cultivation path begins.",
            CanonConfidence.C5),
        new TimelineEvent("E18", "Soul Formation via the Life-Death Domain", "\u751F\u6B7B\u5883\u5143\u795E", Era.SUZAKU_INHERITANCE_ARC,
            "~+60", "Mountain near Xuan Dao Sect, Country of Zhao",
            "Wang Lin",
            "Wang Lin breaks through to Soul Formation by comprehending the Life-Death Domain. A transformative moment for his Dao.",
            "The mountain near Xuan Dao Sect becomes a potential heritage site. Life-Death Essence comprehension begins here.",
            CanonConfidence.C5),
        new TimelineEvent("E19", "The Cloud Sky Sect Era", "\u4E91\u5929\u5B97\u65F6\u671F", Era.SUZAKU_INHERITANCE_ARC,
            "~+80 to ~+100", "Cloud Sky Sect, Chu Country, Planet Suzaku",
            "Wang Lin, Li Muwan (\u674E\u6155\u5A49), Chen Bailiang, Sun Zhenwei",
            "Wang Lin intervenes at Li Muwan's forced wedding, kills Sun Zhenwei. Becomes Sect Master briefly. Hands position to Li Muwan. Li Muwan = Wang Lin's Dao companion.",
            "Cloud Sky Sect continues under Li Muwan. Li Muwan's alchemy skill is a resource for the player. Their love story echoes through the Dao.",
            CanonConfidence.C5),

        // ── ERA 5: The Heavenly Fate Sect Arc ──
        new TimelineEvent("E44", "Wang Lin Raids the Suzaku Tomb", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+25-30",
            "Suzaku Tomb / Cultivation Planet Crystal, Planet Suzaku",
            "Wang Lin, Piao Nanzi / Lou Hou",
            "Enters multi-floor tomb containing the Cultivation Planet Crystal (planet’s seal mechanism). Finds the Half-Moon Blade. Encounters scattered demon Piao Nanzi/Lou Hou sealed within. First major tomb-raiding arc; introduces planet-level cosmology.",
            "The Suzaku Tomb is a ruin the player can explore. The Cultivation Planet Crystal is Planet Suzaku’s core seal mechanism.",
            CanonConfidence.C4),
        new TimelineEvent("E45", "Wang Lin Inherits the Soul Refining Sect from Dun Tian", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+50-70",
            "Soul Refining Sect, Pilu Kingdom, Planet Suzaku",
            "Wang Lin, Dun Tian, Nian Tian",
            "Dun Tian gives 3 gifts: helps clone to Nascent Soul peak, true body to 3-Star Ancient God, bestows Ten Billion Soul Banner + Soul Refining Sect inheritance. Dun Tian erases own consciousness to become soul within the Banner. Wang Lin learns Soul Refining/Extracting/Sealing methods.",
            "The Soul Refining Sect survives. The Billion Soul Banner is Wang Lin’s signature weapon. Dun Tian exists as a soul within it.",
            CanonConfidence.C5),
        new TimelineEvent("E46", "Wang Lin Becomes the 6th-Gen Vermilion Bird Divine Emperor", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+70-90",
            "Vermilion Bird Starfield / Four Divine Sect",
            "Wang Lin, Lu Yun (5th-Gen), Azure Dragon Divine Emperor",
            "Inherits the Vermilion Bird Divine Emperor position, becoming 6th generation. Lu Yun was secretly Heng Yue’s Huang Long Zhenren. Wang Lin absorbs Vermilion Bird fire energy. Eventually returns the rein to Azure Dragon Divine Emperor.",
            "The Vermilion Bird Divine Sect survives. The Four Divine Sect recognizes Wang Lin’s legacy.",
            CanonConfidence.C5),
        new TimelineEvent("E47", "Wang Lin Kills Sun Zhenwei, Becomes Cloud Sky Sect Master", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+85-95",
            "Cloud Sky Sect, Chu Country, Planet Suzaku",
            "Wang Lin, Sun Zhenwei, Li Muwan, Chen Bailiang",
            "Li Muwan agrees to marry Sun Zhenwei to shatter her own heart (cultivation method). At the wedding, Wang Lin kills Sun Zhenwei, kills Chen Bailiang, becomes Cloud Sky Sect leader. Later hands position to Li Muwan.",
            "The Cloud Sky Sect survives. Li Muwan serves as Sect Master. The wedding site is a landmark.",
            CanonConfidence.C4),
        new TimelineEvent("E97", "Wang Lin Kills Hong Die — The Kunji Whip Changes Hands", null, Era.SUZAKU_INHERITANCE_ARC,
            "~+80-100",
            "Planet Suzaku region",
            "Wang Lin, Hong Die, Qian Feng",
            "Hong Die, who had received the Kunji Whip from Qian Feng (Tian Yu Sect heavy treasure) for her battle with Wang Lin, is killed. Wang Lin takes the Kunji Whip. Later burned by Ghostly Sky Fire, nourished by Karma Concept, becomes the Karma Whip.",
            "The Karma Whip is the evolved form of Hong Die’s Kunji Whip.",
            CanonConfidence.C5),
        new TimelineEvent("E108", "Wang Lin Creates His Second Immortal Guard — Ta Shan", null, Era.HEAVENLY_FATE_ARC,
            "~+200-400",
            "Xianxuan Tribe territory",
            "Wang Lin, Ta Shan",
            "Creates second Immortal Guard from Ta Shan (Xianxuan Tribe warrior), pushing him to peak Yang Real stage. Ta Shan is unique — the only guard to ever regain freedom when an ancestral elder shatters his slave seal. Cultivation eventually reaches Celestial Immortal realm independently.",
            "Ta Shan is alive and free, at Celestial Immortal realm.",
            CanonConfidence.C5),
        new TimelineEvent("E20", "Wang Lin Joins the Heavenly Fate Sect", "\u52A0\u5165\u5929\u8FD0\u5B97", Era.HEAVENLY_FATE_ARC,
            "~+100", "Planet Tian Yun",
            "Wang Lin, All-Seer (\u5168\u77E5\u8005), Tianyunzi (clone + artifact spirit)",
            "Wang Lin becomes 7th disciple of the Purple Division of the Heavenly Fate Sect. The sect has 7 color divisions, each with its own supreme spirit vein. Planet Tian Yun = rank-7 cultivation planet.",
            "Heavenly Fate Sect's 7 color divisions. 7+ supreme spirit veins. 5-20B mortals. After All-Seer's death (~Y300), divisions persist as ruins.",
            CanonConfidence.C5),
        new TimelineEvent("E21", "The All-Seer's Possession Plot", "\u5168\u77E5\u8005\u7684\u9644\u8EAB\u9634\u8C0B", Era.HEAVENLY_FATE_ARC,
            "~+200 to ~+300", "Planet Tian Yun",
            "All-Seer, Wang Lin",
            "All-Seer plots to absorb Wang Lin's source origin. The Celestial Slaughter Art trap is hidden in the Thunder Celestial Realm.",
            "The trap in the Thunder Celestial Realm (C5). All-Seer's schemes are layered — even his 'birthday banquet' battles were part of the plan.",
            CanonConfidence.C5),
        new TimelineEvent("E22", "Wang Lin Kills the All-Seer", "\u738B\u6797\u51FB\u6740\u5168\u77E5\u8005", Era.HEAVENLY_FATE_ARC,
            "~+300", "Planet Tian Yun",
            "Wang Lin, All-Seer",
            "Wang Lin kills the All-Seer. The Heavenly Fate Sect's 7 divisions shatter into sub-faction ruins. Tianyunzi (clone + artifact spirit) remains.",
            "Heavenly Fate Sect ruins. Tianyunzi's true body hides in the Primordial Divine Realm. The 7 division complexes become exploration sites.",
            CanonConfidence.C5),

        // ── ERA 6: The Allheaven Star System Arc ──
        new TimelineEvent("E48", "Wang Lin Creates His First Immortal Guard, Du Jian", null, Era.HEAVENLY_FATE_ARC,
            "~+120-150",
            "Heavenly Fate Sect / Alliance Star System",
            "Wang Lin, Du Jian, a Ghost King",
            "Creates first Immortal Guard by fusing Du Jian with a Ghost King’s soul, producing a hybrid entity at Yin Void level. Du Jian helps escape the Chaotic Expanse. Later protects Wang Ping for years. Eventually destroyed by the Blood Ancestor.",
            "Du Jian is dead. Wang Ping remembers the guard who protected him.",
            CanonConfidence.C5),
        new TimelineEvent("E49", "Wang Lin Learns the Celestial Slaughter Art (The Trap)", null, Era.HEAVENLY_FATE_ARC,
            "~+110",
            "Heavenly Fate Sect, Planet Tian Yun",
            "Wang Lin, All-Seer, Tianyunzi",
            "Obtains Celestial Slaughter Art from Tianyunzi’s clone. The trap: after gathering exactly 10 million life seals, they envelop the user into a ‘true-life seal’ — giving the All-Seer a backdoor to possess Wang Lin’s body. Wang Lin discovers this, expels the art into his first Immortal Guard, and re-cultivates it properly after obtaining Slaughter Origin.",
            "The Celestial Slaughter Art’s malicious legacy is a cautionary tale about cultivation techniques with hidden possession mechanisms.",
            CanonConfidence.C5),
        new TimelineEvent("E50", "Wang Lin Lives a Mortal Life with His Son Wang Ping", null, Era.HEAVENLY_FATE_ARC,
            "~+200-250",
            "A mortal village (unnamed)",
            "Wang Lin, Wang Ping, mortal wife",
            "Lives as a mortal for a full lifetime, experiencing birth, aging, sickness, and death. Raises a son, Wang Ping. After his mortal wife dies of old age, Wang Lin finally understands ‘what life means.’ Comprehension pushes him to Ascendant mid-stage. Second major mortal-life arc.",
            "Wang Ping exists as a mortal NPC. The village where Wang Lin lived is a pilgrimage site.",
            CanonConfidence.C5),
        new TimelineEvent("E51", "Wang Lin Becomes the Third Heaven-Defying Cultivator", null, Era.HEAVENLY_FATE_ARC,
            "~+250",
            "Alliance Star System",
            "Wang Lin",
            "Forms his own Dao and breaks through to Ascendant, becoming only the third Heaven-Defying Cultivator in the Cave World’s history. Incites divine retribution. Zhou Yi’s Ascendant Crystal crucial for surviving the breakthrough.",
            "The scar of divine retribution at the breakthrough site.",
            CanonConfidence.C5),
        new TimelineEvent("E100", "Wang Lin Obtains the Slash Luo Art from the Rain Celestial Sword", null, Era.ALLHEAVEN_ARC,
            "~+300-350",
            "From the Rain Celestial Sword’s sword-spirit Jufu",
            "Wang Lin, Jufu",
            "Inherits the Slash Luo Art — a legacy sword technique within the previous generation’s Rain Immortal Sword. Can slash through space and cut through rules. A technique in the scope of the late 3rd Step.",
            "The Slash Luo Art is embedded in the Rain Celestial Sword lineage.",
            CanonConfidence.C5),
        new TimelineEvent("E107", "The Tuo Sen Conflict — Ancient God vs Ancient God", null, Era.ALLHEAVEN_ARC,
            "~+400-600",
            "Multiple locations across the Sealed Realm",
            "Wang Lin, Tuo Sen",
            "Tuo Sen, a rival Ancient God, battles Wang Lin multiple times. All souls in the Billion Soul Banner except 4 condensed main souls self-destruct. Wang Lin later repairs the banner via the Gate of Emptiness. Situ Nan uses Yellow Springs Finger to heavily injure Tuo Sen.",
            "The Billion Soul Banner is repaired but diminished. Tuo Sen’s fate is a loose end.",
            CanonConfidence.C4),
        new TimelineEvent("E23", "The Demon Spirit Land and Yao Xixue", null, Era.ALLHEAVEN_ARC,
            "~+300", "Demon Spirit Land / Southern Domain",
            "Wang Lin, Yao Xixue (\u59DA\u51B0\u96EA), Yao Xinghai (Blood Ancestor)",
            "Encounter with Yao Family. Yao Xixue (amnesiac). Blood Ancestor Yao Xinghai sends kill-order on Wang Lin.",
            "Yao Family territory in Southern Domain. Blood Soul Pill mechanics. Yao Xixue's connection to the Blood Ancestor.",
            CanonConfidence.C5),
        new TimelineEvent("E24", "The Thunder Celestial Tournament", "\u96F7\u4ED9\u754C\u6BD4\u6B66", Era.ALLHEAVEN_ARC,
            "~+400 to ~+500", "Thunder Celestial Realm",
            "Wang Lin (as 'Xu Mu'), Chosen Immortal Clan, Master Flamespark, Russell",
            "Wang Lin becomes 'Thunder Celestial Xu Mu' at the tournament. Helps Chosen Immortal Clan escape enslavement. Obtains God-Slaying War Chariot, Celestial Emperor Crown.",
            "Thunder Celestial Realm (post-collapse, depleted). Chosen Immortal Clan freed. God-Slaying War Chariot in Wang Lin's possession.",
            CanonConfidence.C5),
        new TimelineEvent("E25", "Wang Lin Slays Daoist Water", "\u51FB\u6740\u6C34\u9053\u5B50", Era.ALLHEAVEN_ARC,
            "~+500 to ~+600", "Cloud Sea Star System",
            "Wang Lin, Daoist Water (Shui Daozi, from Outer Realm), Li Qianmei",
            "Rank 9 God Sect's Daoist Water attacks Wang Lin (sensed Lord of the Sealed Realm's aura). Lord of the Sealed Realm's spirit appears, severely injures Daoist Water. Wang Lin turns to stone; Li Qianmei's 10-year blood anointment saves him.",
            "The Stone Transformation site on Cloud Sea. Li Qianmei's blood anointment traces. Daoist Water killed by Wang Lin (Ch.1509).",
            CanonConfidence.C5),

        // ── ERA 7: The Cloud Sea Star System Arc ──
        new TimelineEvent("E52", "Wang Lin Creates the Karma Whip", null, Era.ALLHEAVEN_ARC,
            "~+300",
            "Demon Spirit Land / East Sea",
            "Wang Lin",
            "Soul Lasher (originally Kunji Whip from Tian Yu Sect, obtained after Hong Die’s death) is burned by Ghostly Sky Fire, then nourished by Karma Concept. Transforms into the Karma Whip — a pseudo-immortal dao-domain weapon that weaponizes karmic cause-effect. Karma Domain stabilizes. Once used to cleave open 7 million worlds.",
            "The Karma Whip is one of Wang Lin’s signature weapons to endgame. Evolves into the Karma Origin Sword.",
            CanonConfidence.C5),
        new TimelineEvent("E53", "Wang Lin Meets Zhou Yi, Receives the Rain Celestial Sword", null, Era.ALLHEAVEN_ARC,
            "~+280",
            "A pagoda (with a celestial corpse inside), Alliance Star System",
            "Wang Lin, Zhou Yi",
            "Zhou Yi separates out a mid-quality sword from the Rain Celestial Swords and gifts it to Wang Lin on the condition that Wang Lin protect the celestial corpse inside the pagoda for eternity. The sword-spirit Jufu is later passed to Xu Liguo. An eternal-inheritance pact.",
            "The pagoda with the celestial corpse is a landmark. Xu Liguo wields the Rain Celestial Sword.",
            CanonConfidence.C5),
        new TimelineEvent("E54", "Wang Lin Obtains the Heaven-Avoiding Coffin for Li Muwan’s Soul", null, Era.ALLHEAVEN_ARC,
            "~+300",
            "Alliance Star System",
            "Wang Lin, Li Muwan (soul)",
            "Acquires the Heaven-Avoiding Coffin to preserve Li Muwan’s Nascent Soul after her physical body perishes during Nascent Soul formation. The coffin avoids heaven’s perception, shielding the soul from karmic detection. Li Muwan’s soul rests here until later transferred to the Heaven-Defying Bead.",
            "The Heaven-Avoiding Coffin is retained.",
            CanonConfidence.C5),
        new TimelineEvent("E55", "Wang Lin Inherits the Annihilation Restriction from Li Yuan", null, Era.ALLHEAVEN_ARC,
            "~+320",
            "Alliance Star System",
            "Wang Lin, Li Yuan",
            "Inherits half of Li Yuan’s restrictions heart, gaining the Annihilation Restriction — the 1st of the Four Great Restrictions. Begins systematic collection: Annihilation (Ch. 754) → Time (Ch. 1223) → Life-Death (Ch. 1229) → Ancient Soul (Ch. 1697). Foundation of restriction-based combat.",
            "The Four Great Restrictions are Wang Lin’s signature restriction arts.",
            CanonConfidence.C5),
        new TimelineEvent("E56", "Wang Lin Creates the 18 Layers of Hell Reincarnation Realm", null, Era.ALLHEAVEN_ARC,
            "~+350",
            "Alliance Star System",
            "Wang Lin",
            "Uses Life-Death Dao Underworld River + Magic Arsenal celestial spell (from Bai Fan) to bind souls into the Celestial Sealing Stamp, forming a private reincarnation cycle — the 18 Layers of Hell. Stores all souls of enemies killed. Powers ‘Scatter Beans to Form Soldiers.’ Material realization of Life-Death comprehension.",
            "The 18-Layer pocket-realm persists as part of the Celestial Sealing Stamp.",
            CanonConfidence.C5),
        new TimelineEvent("E57", "Yao Xixue Ambushes Wang Lin in the Immortal Monarch’s Cave Mansion", null, Era.ALLHEAVEN_ARC,
            "~+310",
            "Immortal Monarch’s Cave Mansion, East Sea Demon Spirit Land",
            "Wang Lin, Yao Xixue, Blood Ancestor Yao Xinghai",
            "Wang Lin and Yao Xixue explore the Immortal Monarch’s Cave Mansion together. She ambushes him. Wang Lin subdues and imprisons her for 100 years, destroying her meridians. Triggers the Blood Ancestor conflict. Yao Xixue later sacrifices her body to the Wind Demon; Wang Lin kills the Wind Demon; her memories are devoured and she departs amnesiac.",
            "The Immortal Monarch’s Cave Mansion is a high-tier dungeon. Yao Xixue’s fate is a tragic plot hook.",
            CanonConfidence.C5),
        new TimelineEvent("E58", "Wang Lin Enters the Seven-Colored Realm", null, Era.ALLHEAVEN_ARC,
            "~+380",
            "Seven-Colored Realm (pocket-world within the Cave World)",
            "Wang Lin, Cang Songzi, Master Ashen Pine",
            "Enters the Seven-Colored Realm — a pocket-world tied to the Seven-Colored Daoist’s power. Obtains Crystal Sword (Pseudo Nirvana Void), paired Black-White Short Swords, 108 Seven-Colored God Void Nails from Cang Songzi. Fights Master Ashen Pine — Rusted Iron Sword destroyed. Also obtains Blood-Red Nascent Souls.",
            "The Seven-Colored Realm is a high-tier zone. The Seven-Colored Nails are retained by Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E59", "Wang Lin’s First Battle with Daoist Water", null, Era.ALLHEAVEN_ARC,
            "~+400",
            "Cloud Sea Star System",
            "Wang Lin, Daoist Water",
            "Daoist Water shatters Wang Lin’s Crystal Sword, Black-White Short Swords, and Ancient God Trident. The previous Lord of the Sealed Realm later restores the swords. This battle triggers Wang Lin’s INITIAL awakening of Slaughter Essence.",
            "Daoist Water’s remains. The restored swords. The Rank 9 God Sect survives without its master.",
            CanonConfidence.C5),
        new TimelineEvent("E60", "Wang Lin Obtains Bai Fan’s Inheritance and Learns Mountain Crumble", null, Era.ALLHEAVEN_ARC,
            "~+420",
            "Thunder Immortal World / Bai Fan’s Collection Pavilion",
            "Wang Lin, Immortal Emperor Bai Fan (inheritance), Immortal Lord Qing Shui",
            "Finds Bai Fan’s Collection Pavilion. Systematically learns the Six Paths Triple Arts: Call Wind (Ch. 783), Summon the Rain (Ch. 914), Magic Arsenal (Ch. 919), Mountain Crumble (Ch. 1105). Mountain Crumble triggers breakthrough to mid Nirvana Cleanser (Ch. 1102) and completes Karma Domain.",
            "The Collection Pavilion exists. The Six Paths Triple Arts are Wang Lin’s primary celestial spell set.",
            CanonConfidence.C5),
        new TimelineEvent("E61", "Wang Lin Creates His First Original Spell — Sundered Night", null, Era.ALLHEAVEN_ARC,
            "~+380",
            "Water Spirit Star",
            "Wang Lin",
            "Creates first self-created divine ability — Sundered Night. Cultivated via insights into the Beginning Realm. Uses the Taichu sun to tear through the night. Contains the rules of beginning. Used to kill Xu Kongzi and heavily wound Tian Yunzi. After reaching Kong-Jie, evolves into a Belief Art.",
            "Sundered Night is Wang Lin’s signature Original Spell.",
            CanonConfidence.C5),
        new TimelineEvent("E62", "Wang Lin Learns the Dream Dao", null, Era.ALLHEAVEN_ARC,
            "~+450",
            "With the Sealer of Realms",
            "Wang Lin, Sealer of Realms",
            "With the Sealer of Realms’ help, enters the Dao Realm and creates his third Original Spell — Dream Dao. Allows probing reincarnation and enslavement. Used to kill his shadow (Self Tribulation). Used to discover Tianyunzi’s 99 cycles of reincarnation. Combined with Dream of Ancient Times to become Immortal Dream (Ch. 1675).",
            "Dream Dao is a persisting ability. The revelation about Tianyunzi is plot-critical.",
            CanonConfidence.C5),
        new TimelineEvent("E66", "Wang Lin Learns Dao Fusion and Light Shadow Shield", null, Era.ALLHEAVEN_ARC,
            "~+400",
            "Blue Silk Clan Star Domain",
            "Wang Lin, Dao Master Blue Dream",
            "Learns Dao Fusion (merge different dao arts) and Light and Shadow Shield (creates defensive light from every vitality in the world). Both from Dao Master Blue Dream (Blue Silk Clan, Void Tribulant+).",
            "The Blue Silk Clan survives. Dao Fusion becomes a key mechanic for Wang Lin’s composite abilities.",
            CanonConfidence.C4),
        new TimelineEvent("E74", "Wang Lin Rescues Xi Zifeng — The Third Disciple", null, Era.ALLHEAVEN_ARC,
            "~+350-400",
            "Luo Tian Thunder Immortal Realm",
            "Wang Lin, Xi Zifeng",
            "Xi Zifeng, sole survivor of her family after the Luo Tian Alliance battle, had disfigured her own face for 800 years. Wang Lin rescues her, restores her beauty, elevates cultivation to Jingnie, accepts her as 3rd disciple, gifts Divine Thunder Blood Sword. Kills all who had humiliated her. She remains in the Cave Dwelling Realm.",
            "Xi Zifeng is alive in the Cave Dwelling Realm. The Divine Thunder Blood Sword was given to her.",
            CanonConfidence.C5),
        new TimelineEvent("E75", "Wang Lin Obtains the Fire Essence", null, Era.ALLHEAVEN_ARC,
            "~+450-550",
            "Various (Vermilion Bird Starfield, through 4 awakenings)",
            "Wang Lin",
            "Comprehends Fire Essence through four Vermilion Bird awakenings and Ethereal Fire cultivation. First of 6 substantial Essences, each forming an Essence True Body. Fire Essence True Body later fused into the Five Elements True Body.",
            "The Fire Essence True Body exists within Wang Lin’s composite bodies.",
            CanonConfidence.C5),
        new TimelineEvent("E78", "Wang Lin Obtains the Karma Essence", null, Era.ALLHEAVEN_ARC,
            "~+350",
            "Alliance Star System",
            "Wang Lin",
            "Karma Domain (Ch. 731/850) crystallizes into Karma Essence — one of 4 virtual Essences. The Karma Whip is its weapon form. Later becomes the Karma Print (Ch. 1614, 4th Original Spell).",
            "Karma Essence is one of Wang Lin’s core Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E79", "Wang Lin Obtains the True-False Essence", null, Era.ALLHEAVEN_ARC,
            "~+450",
            "Cloud Sea Star System",
            "Wang Lin, Daoist Water",
            "True-False Domain (Ch. 1102, evolved from Karma Domain after Mountain Crumble) crystallizes into True-False Essence when the Star of Law is destroyed fighting Daoist Water. Later becomes the True and False Eternal Seal (Ch. 1617, 6th Original Spell).",
            "True-False Essence is one of Wang Lin’s core Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E93", "The Yao Family Issues a Kill-Order on Wang Lin", null, Era.ALLHEAVEN_ARC,
            "~+400-500",
            "Allheaven Star System (Southern Domain)",
            "Wang Lin, Yao Family",
            "The Yao Family (connected to Blood Ancestor Yao Xinghai) issues a kill-order. Wang Lin responds by destroying multiple planets in the Allheaven Star System. The ‘Fame Shakes The Allheaven Star System’ arc (Book 7). Wang Lin earns the title ‘Thunder Celestial Xu Mu.’",
            "Destroyed planets are voids in the Allheaven Star System. The Yao Family remains hostile.",
            CanonConfidence.C5),
        new TimelineEvent("E95", "Wang Lin Obtains the Body Fixation Art", null, Era.ALLHEAVEN_ARC,
            "~+350-400",
            "Qing Lin’s Cave Dwelling / Demon Spirit Land",
            "Wang Lin",
            "Accidentally obtains the Body Fixation Art — an Immortal Art from the Xiangang Continent left by Immortal Emperor Qing Lin. Can fix the body, the soul, immortals, primordial force, the flow of stars and rivers, the passage of time, and changes in space. Only 5 people know it.",
            "The Body Fixation Art is a legendary technique known to very few.",
            CanonConfidence.C5),
        new TimelineEvent("E26", "The Sealed Realm War", "\u5C01\u754C\u5927\u6218", Era.CLOUD_SEA_ARC,
            "~+500 to ~+800", "Sealed Realm / Cave World",
            "Wang Lin, multiple Sealed Realm factions, Lord of the Sealed Realm (spirit)",
            "Major conflict within the Sealed Realm. Wang Lin's involvement escalates. The Lord of the Sealed Realm's spirit awakens briefly (during Daoist Water fight).",
            "War scars across the Sealed Realm. Multiple ruined battle sites. Lingering combat energy.",
            CanonConfidence.C4),
        new TimelineEvent("E27", "Wang Lin Flees to the Outer Realm", "\u9003\u5F80\u5916\u754C", Era.CLOUD_SEA_ARC,
            "~+800", "Boundary between Sealed Realm and Outer Realm",
            "Wang Lin, Li Qianmei",
            "Wang Lin flees the Sealed Realm to the Outer Realm with Li Qianmei. The Outer Realm has higher cultivation tiers (not suppressed by the Array).",
            "The boundary is a scarred warzone. Heaven-Splitting Axe (seal spirit) still present.",
            CanonConfidence.C5),
        new TimelineEvent("E28", "Li Muwan's Death and the Heaven-Defying Bead Storage", "\u674E\u6155\u5A49\u6B7B\u4EA1\u4E0E\u74F7\u4E2D\u5B58\u50A8", Era.CLOUD_SEA_ARC,
            "~+800", "Unknown (within Cave World)",
            "Wang Lin, Li Muwan",
            "Li Muwan dies. Wang Lin stores her Nascent Soul inside the Heaven-Defying Bead. This becomes his primary motivation for Transcendence — to resurrect her.",
            "Li Muwan's Nascent Soul inside the bead. The emotional anchor for Wang Lin's entire late-game arc. Her resurrection requires Heaven Trampling.",
            CanonConfidence.C5),

        // ── ERA 8: The Cave World Revelation ──
        new TimelineEvent("E64", "Wang Lin’s Four Vermilion Bird Awakenings", null, Era.CLOUD_SEA_ARC,
            "~+350-600",
            "Vermilion Bird Starfield / Various",
            "Wang Lin, Ming Hai, Fire Dragon, Flame Dragon, Fire Sparrow Clan ancestral spirit",
            "Four sequential awakenings: (1) Ch. 1021: Red Vermilion (Ming Hai’s Burn The Heaven flames); (2) Ch. 1068: White (Devilish Flames + Fire Dragon + Flame Dragon); (3) Ch. 1210: Blue (Fire Sparrow Clan ancestral spirit); (4) Ch. 1409-1412: Karma fire burns Blue→Black→9-Color→Ethereal Fire. First and only cultivatable Ethereal Fire.",
            "Ethereal Fire is Wang Lin’s signature fire to endgame.",
            CanonConfidence.C5),
        new TimelineEvent("E65", "Wang Lin Completes the Slaughter Essence", null, Era.CLOUD_SEA_ARC,
            "~+600",
            "Sky Gate vortex",
            "Wang Lin, Immortal Lord Qing Shui",
            "Qing Shui condenses and gifts the Slaughter Sword using the Sky Gate vortex power. Wang Lin fuses it with the Slaughter Crystal and Qing Shui’s own Slaughter Essence to COMPLETE his Slaughter Essence. Initially awakened at Ch. 1509 (slaying Daoist Water) but only fully completed here.",
            "The Slaughter Sword is retained. Slaughter Essence is one of Wang Lin’s 14 Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E67", "Wang Lin Meets the Madman Lian Daofei Inside the Nether Beast", null, Era.CLOUD_SEA_ARC,
            "~+550",
            "Inside the Nether Beast",
            "Wang Lin, Lian Daofei",
            "Encounters the madman Lian Daofei inside his own Nether Beast. Lian Daofei teaches two Dao Spells: Li Guang’s Heaven-Shattering Bow Dao (Ch. 1533) and the Seven-Colored Lance (Ch. 1543, seven-colored light with power of emotions, three transformations).",
            "Li Guang’s Bow and Seven-Colored Lance are retained to endgame.",
            CanonConfidence.C5),
        new TimelineEvent("E68", "Wang Lin Condenses His Nine Accompanying Thunders", null, Era.CLOUD_SEA_ARC,
            "~+580",
            "Scatter Thunder Clan territory",
            "Wang Lin, Scatter Thunder Clan, Thunder Toad",
            "Completes the set of 9 Accompanying Thunders: 6 stolen/devoured from Scatter Thunder Clan + Ancient Thunder Dragons; then 3 unprecedented: (7) Store All Ji Thunder (from Ji Realm); (8) Bloodline Thunder (sacrificing companion Thunder Toad); (9) Defying Thunder (never before seen). Completes Thunder Essence foundation.",
            "The Scatter Thunder Clan was devastated. The Thunder Toad is gone (sacrificed).",
            CanonConfidence.C5),
        new TimelineEvent("E69", "Wang Lin Creates Flowing Time — Second Original Spell", null, Era.CLOUD_SEA_ARC,
            "~+500",
            "Gate of the Ancient Immortal Domain, Wind Immortal World",
            "Wang Lin",
            "Comprehends second Original Spell — Flowing Time (Dao of Time) — at the gate of the Ancient Immortal Domain. Can reverse time. Later used to send Slaughter True Body Lu Mo back to the past to search for a method to resurrect Li Muwan.",
            "Flowing Time is a persisting ability.",
            CanonConfidence.C5),
        new TimelineEvent("E70", "Wang Lin Erects the Unnamed Wheel Formation", null, Era.CLOUD_SEA_ARC,
            "~+650",
            "Boundary between the Sealed Realm and the Outer Realm",
            "Wang Lin",
            "Creates a replacement for the Realm-Sealing Grand Array. Stops all Outer-Realm cultivators from entering while NOT restricting Joss Flames. Built from: (1) treasure spirits of destroyed treasures from 100 years of war, (2) souls of destroyed cultivation planets, (3) soul fragments of dead Outer-Realm cultivators, (4) souls of Inner-Realm war dead.",
            "The Wheel Formation replaces the Realm-Sealing Array as the active boundary seal.",
            CanonConfidence.C5),
        new TimelineEvent("E71", "Wang Lin Subdues the Heaven-Splitting Axe", null, Era.CLOUD_SEA_ARC,
            "~+650",
            "The boundary of the Sealed Realm",
            "Wang Lin, Heaven-Splitting Axe",
            "Subdues the Heaven-Splitting Axe — the sentient spirit of the Realm-Sealing Grand Array — when he destroys/reforms the formation. The Axe becomes his weapon. A Royal Weapon of the Cave World, later destroyed in an ambush (Ch. 1763).",
            "The Heaven-Splitting Axe is destroyed. Its former location at the Realm-Sealing boundary is a landmark.",
            CanonConfidence.C5),
        new TimelineEvent("E76", "Wang Lin Obtains the Thunder Essence", null, Era.CLOUD_SEA_ARC,
            "~+580",
            "Scatter Thunder Clan territory",
            "Wang Lin",
            "Comprehends Thunder Essence, consolidating the 9 Accompanying Thunders into a single coherent origin. Thunder Essence True Body formed, later fused into the Five Elements True Body. Thunder pairs with Slaughter to form Annihilating Thunder.",
            "Thunder Essence True Body exists within Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E77", "Wang Lin Obtains the Life-Death Essence", null, Era.CLOUD_SEA_ARC,
            "~+600-800",
            "Various (result of mortal-life comprehensions)",
            "Wang Lin",
            "Initially formed at Ch. 1280 but sacrificed to fuel Fire and Thunder Essences. Re-completed later via Dream Dao + Dao Fruits at Ch. 1613. One of 4 virtual (intangible) Essences.",
            "Life-Death Essence is one of Wang Lin’s core Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E94", "Wang Lin Kills the Blood Ancestor Yao Xinghai", null, Era.CLOUD_SEA_ARC,
            "~+500-600",
            "Blood Ancestor’s Blood Planet",
            "Wang Lin, Blood Ancestor Yao Xinghai",
            "Kills the Blood Ancestor Yao Xinghai (father of Yao Xixue). Seizes the Blood Ancestor’s Blood Body and explodes it as a weapon (Ch. 789). Takes two Blood Pavilions (Ch. 765, 1507). Concludes the Blood Ancestor conflict that began with the Yao Xixue imprisonment.",
            "The Blood Planet is a ruin. The Blood Pavilions are retained by Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E29", "The Ancient Immortal Domain and the Seven-Colored Daoist Revelation", null, Era.CAVE_WORLD_REVELATION,
            "~+800 to ~+1500", "Ancient Immortal Domain (Cave World boundary)",
            "Wang Lin, Seven-Colored Daoist (revealed as Cave World owner)",
            "Wang Lin discovers the truth: the Cave World is an artificial farm. The Seven-Colored Daoist created it to harvest Joss Flame from mortal worship. Every mortal is unknowing livestock.",
            "The Ancient Immortal Domain gateway. The revelation changes the player's understanding of the entire world. This is the TRUE antagonist — not Allheaven.",
            CanonConfidence.C5),
        new TimelineEvent("E30", "Wang Lin Kills the Seven-Colored Daoist", "\u738B\u6797\u51FB\u6740\u4E03\u8272\u9053\u4EBA", Era.CAVE_WORLD_REVELATION,
            "~+1500", "The Cave World / Seven-Colored Realm",
            "Wang Lin, Seven-Colored Daoist (Heaven Trampling)",
            "Wang Lin kills the Seven-Colored Daoist and becomes the new owner of the Cave World. Renames it 'Wang Lin's Cave World'. The Joss Flame economy now flows to him.",
            "Wang Lin's ownership of the Cave World. The 108 Seven-Colored Divine Sky Nails (designed to kill Third Step experts). The ownership transfer is complete.",
            CanonConfidence.C5),

        // ── ERA 9: The Immortal Astral Continent Arc ──
        new TimelineEvent("E72", "Wang Lin Discovers Tianyunzi Is the Boundary Compass Spirit", null, Era.CAVE_WORLD_REVELATION,
            "~+1100-1200",
            "Dong Lin Sect ruins / Dong Lin Pool",
            "Wang Lin, Tianyunzi",
            "Enters the destroyed Dong Lin Sect, finds Tianyunzi’s handwriting. Discovers that Tianyunzi — the All-Seer’s clone who had been his antagonist — is actually the treasure spirit of the Boundary Compass, one of the 9 parts of the Heaven-Defying Bead. The bead’s missing component had been manipulating Wang Lin’s life from the start.",
            "The Dong Lin Sect ruins. Tianyunzi’s handwriting. The Boundary Compass (returned to Old Man Miesheng).",
            CanonConfidence.C4),
        new TimelineEvent("E73", "Wang Lin Meets Xie Qing — The Third Soul’s 800-Year Meditation", null, Era.CAVE_WORLD_REVELATION,
            "~+400-1200",
            "Qing Ling Star, then Autumn Orchid Valley",
            "Wang Lin, Xie Qing",
            "Xie Qing sat atop a mountain for 800 years cultivating only Concepts. Used ‘fish, water, net, fishing’ analogy to explain Dao to Wang Lin, triggering Nirvana Scryer breakthrough. Xie Qing placed 3 self-seals, ended his own life, entrusted Third Soul memories to Wang Lin. Buried by Wang Lin in Autumn Orchid Valley.",
            "Autumn Orchid Valley is Xie Qing’s burial site. Xie Qing’s Third Soul memories are with Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E106", "Wang Lin Obtains the Seven Origin Swords", null, Era.IAC_ARC,
            "~+1700",
            "Void gate vortex, IAC",
            "Wang Lin, Immortal Lord Qing Shui",
            "Condenses seven Origin Swords using the strange power within the void gate collapse vortex: Fire, Thunder (Ch. 1625), Life-Death, Karma, True-False, Slaughter (Ch. 1561 from Qing Shui), and Restriction (Ch. 1715). Each embodies one of Wang Lin’s Origins. Third-Step-viable weapons.",
            "The seven Origin Swords are retained to endgame.",
            CanonConfidence.C5),
        new TimelineEvent("E31", "Wang Lin Ascends to the Immortal Astral Continent", "\u5347\u5165\u4ED9\u7FBD\u5927\u9646", Era.IAC_ARC,
            "~+1500", "Immortal Astral Continent (\u4ED9\u7FBD\u5927\u9646)",
            "Wang Lin",
            "Wang Lin breaches the Cave World boundary and enters the IAC. A continent so vast it has 9 suns (Grand Empyreans). 440+ spirit veins. 7 continents.",
            "The IAC is the 'true reality' outside the Cave World. Heavenly Bull Continent (120+ fire veins). Ancient Tomb. Ancient Clan Ancestral Temple. Pill Sea.",
            CanonConfidence.C5),
        new TimelineEvent("E32", "Wang Lin Devours the Dao Demon Sect Master", "\u541E\u5403\u9053\u9B54\u5B97\u5B97\u4E3B", Era.IAC_ARC,
            "~+1600", "Green Devil Continent, IAC",
            "Wang Lin, Dao Demon Sect Master",
            "Dao Demon Sect Master captured Wang Lin as Green Devil sacrifice. Wang Lin reversed the ritual and annihilated the entire sect. Devoured the Sect Master.",
            "Dao Demon Sect = DESTROYED. Ruin site on Green Devil Continent. No survivors.",
            CanonConfidence.C5),
        new TimelineEvent("E33", "Wang Lin Slays Gu Dao, Becomes the Tenth Sun", "\u51FB\u6740\u53E4\u9053\uFF0C\u6210\u4E3A\u7B2C\u5341\u65E5", Era.IAC_ARC,
            "~+1800 to ~+2000", "Immortal Astral Continent",
            "Wang Lin, Gu Dao (Great Heavenly Venerable), Dao Yi (Great Celestial Venerable), Xuan Luo (master)",
            "Wang Lin defeats both Gu Dao and Dao Yi to become the #1 power in the Ancient Clan. Becomes the 'Tenth Sun' of the IAC (the 9th was the strongest before him).",
            "Wang Lin as the Tenth Sun = the single most powerful entity on the IAC. The Ancient Clan recognizes him as supreme.",
            CanonConfidence.C5),

        // ── ERA 10: Transcendence ──
        new TimelineEvent("E80", "Wang Lin Obtains the Restriction Essence", null, Era.IAC_ARC,
            "~+1600",
            "Immortal Astral Continent",
            "Wang Lin",
            "Comprehends the Restriction Essence by merging the laws of the world and millions of fused formations from the IAC into the bloodlines of his eyes (from the Great Soul Sect’s Ghostly Sail). Capstone of lifelong restriction specialization. Restriction Essence True Body forms at Ch. 2044-2046.",
            "Restriction Essence is one of Wang Lin’s 4 special Essences.",
            CanonConfidence.C5),
        new TimelineEvent("E81", "Wang Lin Obtains the Taichu and Miemie Essences", null, Era.IAC_ARC,
            "~+1700",
            "Immortal Astral Continent",
            "Wang Lin",
            "Comprehends two of 4 special Essences: Taichu (Absolute Beginning) and Miemie (Silent Extinction). Each forms its own True Body. Slaughter + Miemie form Lu Mo (the Slaughter Clone, a Fourth-Step clone sent back in time).",
            "Taichu and Miemie True Bodies exist within Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E82", "Wang Lin Ascends to the IAC — The Void Avatar Condensed", null, Era.IAC_ARC,
            "~+1500-1600",
            "Arc-shaped platform, Immortal Astral Continent",
            "Wang Lin",
            "Condenses a Void Avatar at the arc-shaped platform, possessing the same Void Destiny as the Immortal Ancestor and Ancient Ancestor. Causes the nine suns of the Xiangang Continent to appear simultaneously. The Void Avatar completely fuses during his battle with the Ancient Path. Allows summoning the full Nine Songs and Three Signs (Ch. 2065).",
            "The arc-shaped platform is a landmark. The simultaneous nine-suns event is legendary on the IAC.",
            CanonConfidence.C5),
        new TimelineEvent("E83", "Wang Lin Is Captured by the Dao Devil Sect — The Green Devil Sacrifice", null, Era.IAC_ARC,
            "~+1700",
            "Dao Devil Sect, Green Devil Continent, IAC",
            "Wang Lin, Dao Devil Sect Master, Ji Si, Xu Decai",
            "The Dao Devil Sect Master captures Wang Lin to use him as a Green Devil sacrifice. Wang Lin reverses the ritual and annihilates the entire sect. Ji Si implants the Yin Blade in Wang Lin’s arm. Wang Lin later kills Xu Decai, obtaining the Origin Soul Lantern (Ch. 1867) and the Rapid Spell Art. The Yin Blade is destroyed (Ch. 1979).",
            "The Dao Devil Sect is annihilated. The Origin Soul Lantern and Rapid Spell Art are retained.",
            CanonConfidence.C5),
        new TimelineEvent("E84", "Wang Lin Meets Xuan Luo — His Master on the IAC", null, Era.IAC_ARC,
            "~+1600-1700",
            "Immortal Astral Continent",
            "Wang Lin, Xuan Luo",
            "Xuan Luo becomes Wang Lin’s master on the IAC. Forges the Golden Print for Wang Lin — the Sovereign’s god-destruction dao spell turned into a treasure, containing a hint of the Nine Suns’ power, making it indestructible.",
            "The Golden Print is retained by Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E85", "Wang Lin Enters Dong Lin Pool — A Decade of Reincarnation Meditation", null, Era.IAC_ARC,
            "~+1800",
            "Dong Lin Pool, IAC",
            "Wang Lin, Dong Lin Female Ancient God",
            "Enters the destroyed Dong Lin Sect and meditates at the Dong Lin Pool for a decade. Begins to comprehend Reincarnation as his 4th Ethereal Essence (Ch. 1943). Encounters the Dong Lin Female Ancient God sealed beneath the sect. Uncovers the Heaven-Defying Bead’s secret — the Ji Qiong skull has a divine sense left by his Slaughter telling how to revive Li Muwan.",
            "The Dong Lin Pool is a meditation site for Reincarnation/Absolute Beginning comprehension.",
            CanonConfidence.C5),
        new TimelineEvent("E86", "Wang Lin Condenses the Five Elements True Body", null, Era.IAC_ARC,
            "~+1800-1900",
            "Immortal Astral Continent",
            "Wang Lin, Ji Du, Xuan Luo",
            "Systematically condenses 6 substantial Essence True Bodies: Fire, Thunder, Metal (Ch. 1997), Wood (Ch. 2017, with Ji Du’s liquid), Water (Ch. 2020), Earth. These 5 (excluding Thunder) fuse into the Five Elements True Body.",
            "The Five Elements True Body is Wang Lin’s primary composite body.",
            CanonConfidence.C5),
        new TimelineEvent("E87", "Wang Lin Creates the Slaughter Clone Lu Mo", null, Era.IAC_ARC,
            "~+1900-2000",
            "Immortal Astral Continent",
            "Wang Lin, Lu Mo",
            "Slaughter Essence and Miemie (Silent Extinction) Essence fuse to form the Slaughter True Body — Lu Mo. An independent Fourth-Step clone. Wang Lin sends Lu Mo back to the past via the Flowing Moon technique to search for a method to resurrect Li Muwan. Lu Mo leaves clues (Infant Skull, White Hair Strand) for the main body.",
            "Lu Mo’s clues (Infant Skull, White Hair Strand) are plot-critical items.",
            CanonConfidence.C5),
        new TimelineEvent("E89", "Wang Lin Summons the Nine Songs and Three Signs", null, Era.IAC_ARC,
            "~+1900",
            "Immortal Astral Continent",
            "Wang Lin, Gu Dao",
            "By fusing with the Void Avatar, achieves the Nine Songs and Three Signs. At this point Wang Lin is a match for the late Celestial and Ancient Ancestors. Becomes unrivalled on the IAC. Achieved simultaneously with the Punishment Slaughter fusion.",
            "The Nine Songs and Three Signs is Wang Lin’s ultimate composite ability before Heaven Trampling.",
            CanonConfidence.C5),
        new TimelineEvent("E90", "Wang Lin Condenses Punishment Slaughter — Final Fused Avatar", null, Era.IAC_ARC,
            "~+1900",
            "Immortal Astral Continent",
            "Wang Lin, Slaughter Essence True Body",
            "After the Slaughter Essence True Body formed and struggled to escape, Wang Lin fuses it with all previous Essence True Bodies — Thunder, Restriction, Absolute Beginning, Absolute End simultaneously. Slaughter finally fuses with all, becoming Punishment Slaughter — the final fused avatar.",
            "Punishment Slaughter is Wang Lin’s ultimate composite avatar form.",
            CanonConfidence.C5),
        new TimelineEvent("E91", "Wang Lin Comprehends the Eight Extreme Daos", null, Era.IAC_ARC,
            "~+1800-2000",
            "Empyrean Trial / 5th Heaven-Trampling Bridge",
            "Wang Lin",
            "Comprehends the Eight Extreme Daos: Extreme Fire (Ch. 1826), Water (Ch. 1958), Metal (Ch. 2034), Wood, Earth (Five Elements); Extreme Sky, Extreme Land (mutually opposing); Extreme Life (life-bound Dao, Ch. 2063 on 5th Bridge). In AWWP, passes the Eight Extreme Daos to Wang Baole.",
            "The Eight Extreme Daos are transmittable. The Empyrean Trial floors are accessible.",
            CanonConfidence.C5),
        new TimelineEvent("E96", "Wang Lin Creates the Soul Devil Ship", null, Era.IAC_ARC,
            "~+1700",
            "Immortal Astral Continent",
            "Wang Lin, Fan Shanmeng",
            "Forges the Soul Devil Ship — a ship made from the four great restrictions and many other restrictions. Pairs with the Ghostly Sail (main + vice). Fan Shanmeng had used the Ghostly Sail to cast a multi-layered illusion on Wang Lin. Wang Lin later refines his own Ghostly Sail (Ch. 1854).",
            "The Soul Devil Ship and Ghostly Sail are retained.",
            CanonConfidence.C5),
        new TimelineEvent("E98", "Wang Lin Receives Three Gifts from the Great Soul Sect Founder", null, Era.IAC_ARC,
            "~+1700",
            "Great Soul Sect, Heavenly Bull Continent, IAC",
            "Wang Lin, Great Soul Sect founder (Luo Yunhai)",
            "Receives three promised gifts: (1) Space Stone (Ch. 1838) — take out one item from storage without damage; (2) Jade with Soul Eye Dao (Ch. 1840) — see IAC changes ONCE; (3) Drop of Crystal Clear Water (Ch. 1843) — Water Essence Drop from 99 rivers, a quasi-Third-Step treasure.",
            "The Space Stone and Water Essence Drop are retained. The Soul Eye Jade was used (Ch. 2023).",
            CanonConfidence.C5),
        new TimelineEvent("E99", "Wang Lin Devours the Earth Fire Main Vein", null, Era.IAC_ARC,
            "~+1600-1700",
            "Great Soul Sect, Heavenly Bull Continent, IAC",
            "Wang Lin",
            "As a Great Soul Sect elder, devours the Earth Fire main vein, completing his Fire Essence True Body. Receives the Three Rites of the Great Soul Sect. Obtains the Fire Element Five Elements Armor and Tianniu Pearl.",
            "The Earth Fire main vein is depleted.",
            CanonConfidence.C4),
        new TimelineEvent("E101", "Wang Lin Crosses the First Heaven-Trampling Bridge", null, Era.TRANSCENDENCE,
            "~+1800",
            "The 1st Heaven-Trampling Bridge",
            "Wang Lin",
            "The first of 9 bridges. Tests the sturdiness of one’s heart. Wang Lin crosses it, beginning his systematic progression through the bridges.",
            "The 1st Bridge is crossed; its trial is completed.",
            CanonConfidence.C5),
        new TimelineEvent("E102", "Wang Lin Crosses the Second Heaven-Trampling Bridge", null, Era.TRANSCENDENCE,
            "~+1800-1900",
            "The 2nd Heaven-Trampling Bridge",
            "Wang Lin",
            "Gets a glimpse of Heaven Trampling power. His soul nearly collapses from the strain. Upon crossing, granted a glimpse of Heaven Trampling divine sense covering the entire Celestial Clan.",
            "The 2nd Bridge is crossed.",
            CanonConfidence.C5),
        new TimelineEvent("E103", "Wang Lin Crosses the Third Heaven-Trampling Bridge", null, Era.TRANSCENDENCE,
            "~+1900",
            "The 3rd Heaven-Trampling Bridge",
            "Wang Lin",
            "The 3rd Bridge requires closing one’s mind off from inner demons. Wang Lin’s response: instead of closing, he EMBRACES his inner demons. Crosses via Heaven-Defying Will — the same principle that defines his entire cultivation path.",
            "The 3rd Bridge is crossed. The embracing-of-inner-demons method is unique to Wang Lin.",
            CanonConfidence.C5),
        new TimelineEvent("E104", "Wang Lin Crosses the Fourth Heaven-Trampling Bridge", null, Era.TRANSCENDENCE,
            "~+1900",
            "The 4th Heaven-Trampling Bridge",
            "Wang Lin",
            "The 4th Bridge turns into specks of light that devour Wang Lin — he wakes up. Upon crossing, the Ancestral curse from the Celestial Ancestor’s Head is removed, and Wang Lin becomes the 10th Sun of the IAC.",
            "The 4th Bridge is crossed. The Ancestral curse is gone.",
            CanonConfidence.C5),
        new TimelineEvent("E105", "Wang Lin is Stopped at the Eighth Heaven-Trampling Bridge", null, Era.TRANSCENDENCE,
            "~+2000",
            "The 8th Heaven-Trampling Bridge",
            "Wang Lin",
            "Cannot cross the 8th Bridge. Never steps on the 9th Bridge. Instead achieves Heaven Trampling through the Reincarnation Essence (Ch. 2087), bypassing the bridge system entirely. The 9th bridge is the ‘bypass’ — final enlightenment transcends bridges.",
            "The 8th Bridge stands uncrossed. The 9th Bridge stands untouched.",
            CanonConfidence.C5),
        new TimelineEvent("E34", "The Nine Heaven Trampling Bridges", "\u4E5D\u5EA7\u8E0F\u5929\u6865", Era.TRANSCENDENCE,
            "~+2000", "The Root Dao",
            "Wang Lin",
            "Wang Lin attempts the 9 Heaven Trampling Bridges. Crosses bridges 1-7. Stopped at bridge 8. Bridge 9 = bypassed entirely (achieved Heaven Trampling via Reincarnation Essence comprehension).",
            "The 9 bridges exist as a test structure. Bridges 1-7 crossed; bridge 8 stopped; bridge 9 never stepped on.",
            CanonConfidence.C5),
        new TimelineEvent("E35", "The 14th Essence — Reincarnation", "\u7B2C\u5341\u56DB\u672C\u6E90\u2014\u2014\u8F6E\u56DE", Era.TRANSCENDENCE,
            "~+2000", "Reincarnation Pool / The Root Dao",
            "Wang Lin",
            "Wang Lin comprehends his 14th and final Essence (Reincarnation) after 13 years of meditation at the Reincarnation Pool. This comprehension = Heaven Trampling. He did NOT cross the 9th bridge.",
            "The Reincarnation Pool. All 14 Essences comprehended. Full Samsara Dao completion.",
            CanonConfidence.C5),
        new TimelineEvent("E36", "Wang Lin Transcends with Li Muwan", "\u738B\u6797\u8D85\u8131\uFF0C\u4E0E\u674E\u6155\u5A49\u540C\u884C", Era.TRANSCENDENCE,
            "~+2000+", "Beyond the Cave World",
            "Wang Lin, Li Muwan (resurrected)",
            "Wang Lin achieves Heaven Trampling, resurrects Li Muwan from the Heaven-Defying Bead, and transcends the Cave World entirely. The ultimate goal of his ~2000-year journey is complete.",
            "The Cave World continues without its previous owner (Wang Lin has left). Li Muwan resurrected. Wang Lin moves to the Root Dao / Luo Tian.",
            CanonConfidence.C5),

        // ── ERA 11: Post-RI Cross-Novel ──
        new TimelineEvent("E63", "Wang Lin Creates the Three Seals", null, Era.TRANSCENDENCE,
            "~+550",
            "During Wang Lin’s mortal transformation period",
            "Wang Lin",
            "After achieving great mastery in the Cause-Effect, Life-Death, and True-False Origins, creates three divine abilities: Karma Print (Ch. 1614, 4th Original Spell), Life and Death Seal (Ch. 1616, 5th), and True and False Eternal Seal (Ch. 1617, 6th). Crystallization of three foundational virtual essences into usable Original Spells.",
            "The Three Seals are Wang Lin’s mid-late game signature abilities.",
            CanonConfidence.C5),
        new TimelineEvent("E88", "Wang Lin Uses the Qi Xi Spell to Attempt Li Muwan’s Resurrection", null, Era.TRANSCENDENCE,
            "~+1900-2000",
            "Via Zhan Li Yunzi’s ancestor",
            "Wang Lin, Zhan Li Yunzi, Zhan Li Yunzi’s ancestor",
            "Uses the Qi Xi Spell (life-force exchange resurrection). Sacrifices 50% of his life force to attempt to resurrect Li Muwan. The attempt fails — Li Muwan doesn’t pass the 4th day out of 7 required. Wang Lin’s most desperate and painful failure. Motivates Lu Mo’s time-travel mission and Reincarnation Essence comprehension.",
            "The failed resurrection attempt site.",
            CanonConfidence.C5),
        new TimelineEvent("E92", "Wang Lin Comprehends One Step to Trample the Heavens on the 5th Bridge", null, Era.TRANSCENDENCE,
            "~+2000",
            "The 5th Heaven-Trampling Bridge",
            "Wang Lin",
            "On the 5th Heaven-Trampling Bridge, comprehends the Heaven-Trampling Dao — a single step that tramples the heavens, the Dao of the 4th Step. A glimpse of the 4th Step achieved BEFORE the final Reincarnation Essence (Ch. 2087).",
            "The 5th Bridge bears the imprint of Wang Lin’s comprehension.",
            CanonConfidence.C5),
        new TimelineEvent("E37", "Wang Lin as 'Paragon Wang' in AWWP", "\u300A\u4E00\u4F4D\u5927\u80FD\u300B\u4E2D\u4E3A\u201C\u738B\u5927\u80FD\u201D", Era.POST_RI,
            "after ~+2000", "AWWP timeline",
            "Wang Lin (as 'Paragon Wang'), Wang Baole",
            "Wang Lin appears as 'Paragon Wang' in A Renegade Immortal's Will (AWWP). He mentors Wang Baole. Per user correction #23: they COEXIST — Wang Lin is NOT Wang Baole's reincarnation.",
            "Wang Lin's presence in AWWP as a mentor figure. The 6-protagonist framing (not 'Four Supremes').",
            CanonConfidence.C5),
        new TimelineEvent("E38", "Wang Lin as 'The God' in ISSTH", "\u300A\u6211\u6709\u8981\u5C01\u5929\u300B\u4E2D\u4E3A\u201C\u90A3\u4E2A\u4EBA\u201D", Era.POST_RI,
            "after ~+2000", "ISSTH timeline",
            "Wang Lin",
            "Wang Lin appears in I Shall Seal the Heavens as a background cosmic figure. NOT the primary antagonist (that's Allheaven/Heaven-Will, which is ISSTH-specific).",
            "Wang Lin's legacy in ISSTH as a Transcended figure in the background. Per user correction #8: Allheaven is NOT Wang Lin's antagonist.",
            CanonConfidence.C4),
        new TimelineEvent("E39", "Bald Crane Sent to Wang Lin's IAC", "\u79C3\u9E64\u88AB\u9001\u5F80\u738B\u6797\u7684\u4ED9\u7FBD\u5927\u9646", Era.POST_RI,
            "Pursuit era (Ptt timeline)", "Wang Lin's Immortal Astral Continent",
            "Su Ming (Pursuit of the Truth), Bald Crane, Wang Lin",
            "Su Ming sends Bald Crane to Wang Lin's IAC. The only direct cross-novel link between Su Ming's and Wang Lin's cosmologies.",
            "Bald Crane on the IAC. Per user correction #13: Bald Crane's illusions do NOT bypass divine sense. Divine sense pierces them.",
            CanonConfidence.C5)

    ));

    // ─── Query Methods ───────────────────────────────────────────────

    /** Get all events in a specific era. */
    public static List<TimelineEvent> getEventsByEra(Era era) {
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : ALL_EVENTS) {
            if (e.era == era) result.add(e);
        }
        return result;
    }

    /** Get an event by its ID (e.g. "E13"). */
    public static TimelineEvent getEventById(String id) {
        for (TimelineEvent e : ALL_EVENTS) {
            if (e.id.equals(id)) return e;
        }
        return null;
    }

    /**
     * Get all events that have remaining traces at a given in-universe year.
     * This is the "what can the player discover?" query.
     *
     * <p>Per the Prime Directive: every event's remaining traces exist
     * objectively whether the player knows about them or not.
     *
     * @param currentYear the in-universe year (0 = Wang Lin's birth)
     * @return events whose date is before currentYear and who have remaining traces
     */
    public static List<TimelineEvent> getDiscoverableTraces(int currentYear) {
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : ALL_EVENTS) {
            // Events before the current year have traces
            if (parseApproximateYear(e.date) <= currentYear && e.remainingTraces != null) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Get all events that occurred in or near a specific location.
     * Useful for populating ruin sites and heritage locations.
     */
    public static List<TimelineEvent> getEventsAtLocation(String locationName) {
        List<TimelineEvent> result = new ArrayList<>();
        String lower = locationName.toLowerCase();
        for (TimelineEvent e : ALL_EVENTS) {
            if (e.location.toLowerCase().contains(lower)) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Get events involving a specific character.
     */
    public static List<TimelineEvent> getEventsInvolving(String characterName) {
        List<TimelineEvent> result = new ArrayList<>();
        for (TimelineEvent e : ALL_EVENTS) {
            if (e.participants.toLowerCase().contains(characterName.toLowerCase())) {
                result.add(e);
            }
        }
        return result;
    }

    /**
     * Get the two-layer antagonist events.
     * Per user correction #8: the antagonists are All-Seer (mortal scheme)
     * and Seven-Colored Daoist (cosmic farmer), NOT Allheaven.
     */
    public static List<TimelineEvent> getAntagonistEvents() {
        return Arrays.asList(
            getEventById("E21"), // All-Seer's possession plot
            getEventById("E22"), // Wang Lin kills All-Seer
            getEventById("E57"), // Yao Xixue ambush in Immortal Monarch's Cave Mansion
            getEventById("E59"), // First battle with Daoist Water (swords shattered)
            getEventById("E83"), // Dao Devil Sect captures Wang Lin (Green Devil sacrifice)
            getEventById("E29"), // Cave World revelation
            getEventById("E30"), // Wang Lin kills Seven-Colored Daoist
            getEventById("E94")  // Kills Blood Ancestor Yao Xinghai
        );
    }

    /**
     * Get events related to sect/faction destruction.
     * These create ruin ecologies per CANON_RI_ECOLOGY.md Global Law G4.
     */
    public static List<TimelineEvent> getDestructionEvents() {
        List<TimelineEvent> result = new ArrayList<>();
        String[] destructionKeywords = {"exterminat", "massacre", "destroyed", "annihilat", "slain", "kills", "ruin"};
        for (TimelineEvent e : ALL_EVENTS) {
            String lower = e.consequences.toLowerCase();
            for (String kw : destructionKeywords) {
                if (lower.contains(kw)) {
                    result.add(e);
                    break;
                }
            }
        }
        return result;
    }

    // ─── Internal ─────────────────────────────────────────────────────

    /**
     * Parse an approximate year from the free-form date string.
     * Handles: "before time" → -999999, "~\u2212100,000 BW" → -100000
     * "+16" → 16, "~+100" → 100, "~+2000+" → 2000, "after ~+2000+" → 2001.
     */
    private static int parseApproximateYear(String date) {
        if (date == null) return 0;
        date = date.trim();
        if (date.contains("before time")) return -999999;
        if (date.contains("Pursuit era") || date.contains("ISSTH") || date.contains("AWWP")) return 2001;

        // Remove common prefixes/suffixes
        String cleaned = date.replace("~", "").replace("BW", "").replace(" ", "");
        // Handle negative years (before Wang Lin's birth)
        if (cleaned.contains("\u2212") || cleaned.contains("-")) {
            cleaned = cleaned.replace("\u2212", "-");
            try {
                return Integer.parseInt(cleaned.replaceAll("[^0-9-]", ""));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        // Handle positive years
        try {
            return Integer.parseInt(cleaned.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}