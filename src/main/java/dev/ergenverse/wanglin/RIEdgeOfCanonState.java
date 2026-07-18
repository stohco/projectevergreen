package dev.ergenverse.wanglin;

import dev.ergenverse.canon.CanonEngine;

import java.util.*;

/**
 * RI Edge-of-Canon State — the runtime representation of Wang Lin's branch
 * at the edge of recorded canon.
 *
 * <p><b>The foundational framing (per user directive, CANON_RI_EDGE_OF_CANON.md):</b>
 * <pre>
 *   The player does NOT enter a "post-Wang-Lin world."
 *   The player enters Wang Lin's branch AT THE EDGE OF RECORDED CANON.
 *
 *   Wang Lin is NOT gone. His true body exists on the Immortal Astral Continent.
 *   Canonical time has paused there. His manifestation travels the lower worlds.
 *   The world is permanently shaped by Wang Lin's history, while Wang Lin
 *   himself still exists.
 * </pre>
 *
 * <p>This class encodes three systems from CANON_RI_EDGE_OF_CANON.md:
 * <ol>
 *   <li><b>Historical Consequences</b> — 30 consequence chains, each tracing
 *       a major canonical event through immediate → 100-year → 1000-year →
 *       current world state.</li>
 *   <li><b>Inheritance Registry</b> — 14 major inheritances, each classified
 *       by 12 questions (canon owner, current owner, transfer conditions,
 *       duplicable, teachable, weakens teacher, recognition, disappears,
 *       karma-tied, bloodline-tied, location-tied, recreatable) PLUS a
 *       protagonist access path (per the user's directive: the player can
 *       ALWAYS find a way).</li>
 *   <li><b>Decision Horizon Taxonomy</b> — 6 horizons (MINUTES → MILLENNIA)
 *       that classify how far ahead an NPC plans.</li>
 * </ol>
 *
 * <p><b>The Manifestation Companion</b> — Wang Lin's manifestation is a
 * permanent companion. It is NOT a quest-giver; it is a witness. It comments
 * on significant places. This class provides the {@link ManifestationComment}
 * registry for location-triggered commentary.
 *
 * <p><b>Per the Prime Directive:</b> the edge-of-canon state is OBJECTIVE.
 * The dissolved seal, the dead antagonists, the open boundary, the vacant
 * Vermilion Bird position — these are facts of the world whether the player
 * knows them or not. Cultivation reveals them; it doesn't change them.
 *
 * <p><b>Per user correction (the reframe):</b> the threats are NOT replacement
 * NPCs. The threats are the universe itself — vacuums, consequences, new
 * generations, ruined formations, altered spirit veins. The real conflict is
 * "how does a civilization rebuild after Wang Lin changed the world?"
 *
 * <p>Canon source: CANON_RI_EDGE_OF_CANON.md (the foundational framing document).
 */
public final class RIEdgeOfCanonState {

    private RIEdgeOfCanonState() {}

    // ════════════════════════════════════════════════════════════════════
    // PART 0 — THE REFRAME
    // ════════════════════════════════════════════════════════════════════

    /**
     * The correct framing of the player's entry point.
     *
     * <p>NOT "post-Wang-Lin world." IS "Wang Lin's branch at the edge of
     * recorded canon." Wang Lin exists; canonical time is paused on the IAC;
     * his manifestation travels the lower worlds.
     */
    public static final String REFRAME_STATEMENT =
        "The player enters Wang Lin's branch at the edge of recorded canon. " +
        "Wang Lin is NOT gone. His true body exists on the Immortal Astral Continent, " +
        "where canonical time has paused. His manifestation travels the lower worlds. " +
        "The world is permanently shaped by Wang Lin's history, while Wang Lin himself still exists.";

    /** The wrong framing (corrected). For reference / assertion checks. */
    public static final String WRONG_FRAMING = "The player enters a post-Wang-Lin world.";

    /**
     * The central conflict of the player's experience.
     * Per user directive: NOT "who replaces Wang Lin's enemies" — but this.
     */
    public static final String CENTRAL_CONFLICT =
        "How does a civilization rebuild after Wang Lin changed the world?";

    // ════════════════════════════════════════════════════════════════════
    // PART 1 — THE THREAT MODEL
    // ════════════════════════════════════════════════════════════════════

    /**
     * The threat categories at the edge of canon.
     *
     * <p>Per user directive: threats are NOT replacement NPCs. Threats ARE
     * the universe itself — the consequences of Wang Lin's journey.
     */
    public enum ThreatCategory {
        ABANDONED_INHERITANCES("Abandoned inheritances",
            "Tu Si's legacy (partial), Bai Fan's Collection Pavilion, the Restriction Mountain trial, the Heavenly Fate Sect's tainted techniques. Dangerous to claim — residual restrictions, guardian beasts, rival treasure hunters."),
        RUINED_SECTS("Ruined sects",
            "Teng Clan, Dao Demon Sect, Dao Devil Sect, Dong Lin Sect, Rank 9 God Sect. Political vacuums — power scrambles, territorial disputes."),
        BROKEN_FORMATIONS("Broken formations",
            "Realm-Sealing Grand Array (dissolved), the Unnamed Wheel Formation (dissolved), various battlefield arrays. Unleashed energies, altered spiritual geography."),
        POLITICAL_VACUUMS("Political vacuums",
            "Heavenly Fate Sect (masterless), Water Spirit Sect (masterless), Blood Planet (ruin). Power realignment — smaller sects expand, new alliances form."),
        DESTROYED_CIVILIZATIONS("Destroyed civilizations",
            "Multiple planets in Allheaven Star System (Wang Lin destroyed them), the Luo Tian Thunder Immortal Realm (collapsed). Lost knowledge, reclaiming beasts, void hazards."),
        ALTERED_SPIRIT_VEINS("Altered spirit veins",
            "Earth Fire main vein at Great Soul Sect (devoured by Wang Lin), battle-scarred spirit veins across the Sealed Realm. Changed cultivation geography — some depleted, some mutated."),
        TERRIFIED_FACTIONS("Terrified surviving factions",
            "The Yao Family remnants, the Scatter Thunder Clan, Outer-Realm survivors. Paranoid, hostile, defensive — may attack outsiders on sight."),
        FORGOTTEN_BATTLEGROUNDS("Forgotten battlefields",
            "The Sealed Realm boundary warzone, the Cloud Sea, the Ancient Immortal Domain. Lingering killing intent, mutated ecology, spirit fragments, treasure hunters."),
        NEW_GENERATIONS("New-generation cultivators",
            "Thousands of cultivators born every generation. Geniuses, tyrants, heroes, monsters, sect leaders. NOT replacements for canonical villains — simply the next generation.");

        public final String name;
        public final String description;
        ThreatCategory(String n, String d) { this.name = n; this.description = d; }
    }

    /** All 9 threat categories. */
    public static final List<ThreatCategory> ALL_THREATS =
        Collections.unmodifiableList(Arrays.asList(ThreatCategory.values()));

    // ════════════════════════════════════════════════════════════════════
    // PART 2 — THE MANIFESTATION COMPANION
    // ════════════════════════════════════════════════════════════════════

    /**
     * Wang Lin's manifestation — the companion that travels with the player.
     *
     * <p>Per CANON_RI_EDGE_OF_CANON.md Part 2: the manifestation is NOT a
     * quest-giver. It is a witness. It comments on significant places. It
     * reveals history gradually. It has its own emotional state.
     */
    public static final class ManifestationCompanion {

        /** The manifestation's emotional state at a given location. */
        public enum Emotion {
            NEUTRAL("No particular emotional resonance."),
            REFLECTIVE("A place of memory. The manifestation is quiet and thoughtful."),
            SORROWFUL("A place of loss. The manifestation grieves."),
            WARY("A place of danger or moral weight. The manifestation is cautious."),
            GRIM("A place of violence. The manifestation remembers the killing."),
            WARM("A place of love or friendship. The manifestation is tender."),
            PROUD("A place of triumph. The manifestation is satisfied.");

            public final String description;
            Emotion(String d) { this.description = d; }
        }

        /**
         * A location-triggered manifestation comment.
         *
         * <p>When the player visits a location with a registered comment,
         * the manifestation speaks. This is the primary vehicle for delivering
         * historical context.
         */
        public static final class ManifestationComment {
            public final String locationId;
            public final String locationName;
            public final Emotion emotion;
            public final String comment;
            public final String historicalContext;
            public final int minPlayerTierToReveal;  // the manifestation reveals more as the player grows

            public ManifestationComment(String locationId, String locationName, Emotion emotion,
                                        String comment, String historicalContext, int minPlayerTierToReveal) {
                this.locationId = locationId;
                this.locationName = locationName;
                this.emotion = emotion;
                this.comment = comment;
                this.historicalContext = historicalContext;
                this.minPlayerTierToReveal = minPlayerTierToReveal;
            }
        }

        // ── Registered comments (from CANON_RI_EDGE_OF_CANON.md chains) ──────

        /** Wang Family Village — Wang Lin's birthplace. */
        public static final ManifestationComment WANG_FAMILY_VILLAGE = new ManifestationComment(
            "wang_family_village", "Wang Family Village", Emotion.SORROWFUL,
            "This is where I was born. They called me Tie Zhu.",
            "Wang Lin was born here (E08). The Wang Clan was massacred by Teng Huayuan (E13). Wang Lin exterminated the Teng Clan in revenge (E14).",
            0  // reveal at any tier
        );

        /** Heng Yue Sect cliff — where Wang Lin found the bead. */
        public static final ManifestationComment HENG_YUE_CLIFF = new ManifestationComment(
            "heng_yue_cliff", "Heng Yue Sect Cliff", Emotion.REFLECTIVE,
            "I found the bead here. A dead bird. A stone. I didn't know what it was.",
            "Wang Lin found the Heaven-Defying Bead here at age 16 (E11). The bead contained Situ Nan. It would define his entire cultivation journey.",
            0
        );

        /** Cloud Sky Sect — Li Muwan's sect. */
        public static final ManifestationComment CLOUD_SKY_SECT = new ManifestationComment(
            "cloud_sky_sect", "Cloud Sky Sect", Emotion.WARM,
            "I brought her back. It took me two thousand years, but I brought her back.",
            "Wang Lin killed Sun Zhenwei here to take Li Muwan back (E47). Li Muwan became sect master. Her body perished (~Year 800, E28); Wang Lin spent 2000 years reviving her. They Transcended together.",
            2  // reveal when player reaches tier 2+
        );

        /** The Sealed Realm boundary warzone. */
        public static final ManifestationComment SEALED_REALM_BOUNDARY = new ManifestationComment(
            "sealed_realm_boundary", "Sealed Realm Boundary", Emotion.GRIM,
            "I slaughtered thousands here. The Axe chose me. I remember every soul.",
            "The Sealed Realm War (E26). Wang Lin slaughtered thousands of Outer-Realm cultivators. He borrowed the Heaven-Splitting Axe's power. He became Lord of the Sealed Realm.",
            3
        );

        /** The Ancient Immortal Domain — where Wang Lin killed the Seven-Colored Daoist. */
        public static final ManifestationComment ANCIENT_IMMORTAL_DOMAIN = new ManifestationComment(
            "ancient_immortal_domain", "Ancient Immortal Domain", Emotion.WARY,
            "I killed him here. The owner. The farmer. I am the owner now.",
            "Wang Lin killed the Seven-Colored Daoist here (E30). He became the new Cave World owner. He dissolved the Realm-Sealing Grand Array.",
            4  // reveal at high tier — this is cosmological truth
        );

        /** The mortal village where Wang Lin lived for Soul Formation. */
        public static final ManifestationComment MORTAL_VILLAGE_SOUL_FORMATION = new ManifestationComment(
            "mortal_village_soul_formation", "Mortal Village (Soul Formation)", Emotion.REFLECTIVE,
            "I lived here. Among mortals. I watched them be born, grow old, and die. That was when I understood life and death.",
            "Wang Lin lived as a mortal for Soul Formation (E18, ~Year 60-80). This comprehension became the foundation of his Life-Death Essence.",
            2
        );

        /** The mortal village where Wang Lin raised Wang Ping. */
        public static final ManifestationComment MORTAL_VILLAGE_WANG_PING = new ManifestationComment(
            "mortal_village_wang_ping", "Mortal Village (Wang Ping)", Emotion.SORROWFUL,
            "I lived here. I had a son. He grew old. I didn't.",
            "Wang Lin lived as a mortal with his son Wang Ping (E50, ~Year 200-250). He experienced birth, aging, sickness, death firsthand. This pushed him to Ascendant mid-stage.",
            2
        );

        /** Autumn Orchid Valley — Xie Qing's burial site. */
        public static final ManifestationComment AUTUMN_ORCHID_VALLEY = new ManifestationComment(
            "autumn_orchid_valley", "Autumn Orchid Valley", Emotion.SORROWFUL,
            "He taught me in parables. Fish, water, net, fishing. Then he died. I buried him here.",
            "Xie Qing (Third Soul of the Seven-Colored Daoist) meditated for 800 years, taught Wang Lin, then ended his own life (E73). Wang Lin buried him here.",
            3
        );

        /** The Cloud Sea — Daoist Water's domain. */
        public static final ManifestationComment CLOUD_SEA = new ManifestationComment(
            "cloud_sea", "Cloud Sea Star System", Emotion.REFLECTIVE,
            "Daoist Water shattered my swords here. I forged better ones. Then I shattered him.",
            "Wang Lin battled Daoist Water twice here (E59, E25). The first battle shattered Wang Lin's weapons. The final battle killed Daoist Water and awakened Wang Lin's Slaughter Essence.",
            3
        );

        /** The void-zones in the Allheaven Star System. */
        public static final ManifestationComment ALLHEAVEN_VOID_ZONES = new ManifestationComment(
            "allheaven_void_zones", "Allheaven Void-Zones", Emotion.GRIM,
            "I destroyed those planets. I don't regret it. But I remember every name.",
            "The Yao Family issued a kill-order on Wang Lin (E93). He responded by destroying multiple planets. Millions died. The void-zones are permanent.",
            3
        );

        /** The Blood Planet. */
        public static final ManifestationComment BLOOD_PLANET = new ManifestationComment(
            "blood_planet", "Blood Planet", Emotion.GRIM,
            "The Blood Ancestor died here. His blood still saturates the soil. Be careful — it will corrode you if you are not prepared.",
            "Wang Lin killed the Blood Ancestor Yao Xinghai here (E94). He took the Blood Body and Blood Pavilions. The planet is saturated with blood energy.",
            3
        );

        /** The Restriction Mountain trial. */
        public static final ManifestationComment RESTRICTION_MOUNTAIN = new ManifestationComment(
            "restriction_mountain", "Restriction Mountain", Emotion.REFLECTIVE,
            "I spent seven years here. I was only the fourth person to complete the trial. My hair turned white. It was worth it.",
            "Wang Lin passed the Restriction Mountain trial (E43, Ch. ~179-180). He developed the Illusionary Circle technique and received the Restriction Flag refining method.",
            2
        );

        /** The Heaven Trampling Bridges. */
        public static final ManifestationComment HEAVEN_TRAMPLING_BRIDGES = new ManifestationComment(
            "heaven_trampling_bridges", "Heaven Trampling Bridges", Emotion.WARY,
            "I crossed seven. I stopped at the eighth. I skipped the ninth. You will cross differently. I will not give you hints — the bridges test the self.",
            "Wang Lin crossed the 9 Heaven Trampling Bridges (E34). He crossed 7, stopped at the 8th, and achieved Heaven Trampling without stepping on the 9th.",
            5  // reveal only at the highest tier — this is endgame
        );

        /** All registered manifestation comments. */
        public static final List<ManifestationComment> ALL_COMMENTS =
            Collections.unmodifiableList(Arrays.asList(
                WANG_FAMILY_VILLAGE, HENG_YUE_CLIFF, CLOUD_SKY_SECT,
                SEALED_REALM_BOUNDARY, ANCIENT_IMMORTAL_DOMAIN,
                MORTAL_VILLAGE_SOUL_FORMATION, MORTAL_VILLAGE_WANG_PING,
                AUTUMN_ORCHID_VALLEY, CLOUD_SEA, ALLHEAVEN_VOID_ZONES,
                BLOOD_PLANET, RESTRICTION_MOUNTAIN, HEAVEN_TRAMPLING_BRIDGES
            ));

        /**
         * Get the manifestation comment for a location, if one exists and
         * the player's tier is high enough to receive it.
         *
         * @param locationId the location the player is visiting
         * @param playerTier the player's absolute cultivation tier (0 = mortal)
         * @return the comment, or null if no comment is registered or the tier is too low
         */
        public static ManifestationComment getCommentFor(String locationId, int playerTier) {
            for (ManifestationComment mc : ALL_COMMENTS) {
                if (mc.locationId.equals(locationId) && playerTier >= mc.minPlayerTierToReveal) {
                    return mc;
                }
            }
            return null;
        }

        /**
         * The manifestation does NOT give quests. It is a witness.
         * This method exists to make that design rule explicit and assertable.
         */
        public static boolean isQuestGiver() { return false; }

        /**
         * The manifestation does NOT fight for the player. It is a witness.
         */
        public static boolean isBodyguard() { return false; }

        /**
         * The manifestation is a PERMANENT companion. It cannot be dismissed.
         */
        public static boolean isPermanent() { return true; }
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 3 — HISTORICAL CONSEQUENCES
    // ════════════════════════════════════════════════════════════════════

    /**
     * A historical consequence chain — tracing a major canonical event through
     * immediate → 100-year → 1000-year → current world state.
     *
     * <p>Per CANON_RI_EDGE_OF_CANON.md Part 3. Nothing invented randomly;
     * everything follows logically from canon (Prime Directive).
     */
    public static final class ConsequenceChain {
        public final String chainId;
        public final String title;
        public final String canonEventIds;  // e.g., "E02-E05"
        public final String eventSummary;
        public final String immediateEffects;
        public final String hundredYearEffects;
        public final String thousandYearEffects;
        public final String currentWorldState;
        public final String playerImplication;
        public final CanonEngine.Confidence canonConfidence;

        public ConsequenceChain(String chainId, String title, String canonEventIds,
                                String eventSummary, String immediateEffects,
                                String hundredYearEffects, String thousandYearEffects,
                                String currentWorldState, String playerImplication,
                                CanonEngine.Confidence canonConfidence) {
            this.chainId = chainId;
            this.title = title;
            this.canonEventIds = canonEventIds;
            this.eventSummary = eventSummary;
            this.immediateEffects = immediateEffects;
            this.hundredYearEffects = hundredYearEffects;
            this.thousandYearEffects = thousandYearEffects;
            this.currentWorldState = currentWorldState;
            this.playerImplication = playerImplication;
            this.canonConfidence = canonConfidence;
        }
    }

    // ── The 30 consequence chains (from CANON_RI_EDGE_OF_CANON.md Part 3) ──
    // Each chain traces a major arc through to the present.

    public static final ConsequenceChain CHAIN_01_CAVE_WORLD_CREATION = new ConsequenceChain(
        "CC01", "The Cave World's Creation", "E02-E05",
        "The Seven-Colored Daoist creates the Cave World, the Three Souls and Seven Spirits, the Heaven-Defying Pearl, and the Realm-Sealing Grand Array (~−100,000 BW).",
        "The Cave World exists as an artificial farm. Mortals generate Joss Flames. The seal suppresses Third-Step cultivation in the Sealed Realm.",
        "The Joss Flame harvest loop stabilizes. A generation of stuck quasi-Third-Step cultivators (Heaven Blight tier) forms.",
        "The Cave World's civilization matures under the seal. The Outer Realm develops a different culture (Third-Step cultivators can arise but are hunted).",
        "The Seven-Colored Daoist is DEAD (killed by Wang Lin). The Cave World is now 'Wang Lin's Cave World.' The seal is DISSOLVED. Third-Step cultivators can arise naturally for the first time in ~100,000 years. The Joss Flame harvest is stopped (Wang Lin releases, not harvests).",
        "The player enters a Cave World without a seal, without an active owner-harvest, and with the ceiling lifted. The most permissive era in the Cave World's history. The player's path is limited only by talent, opportunity, and dao.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_02_TU_SI_LEGACY = new ConsequenceChain(
        "CC02", "The Tu Si Ancient God Legacy", "E06",
        "Tu Si (~−50,000 BW) leaves behind a memory legacy in the Land of the Ancient God, including the Ancient God Tactic.",
        "The Land of the Ancient God becomes a high-tier ruin. The Ancient God Tactic is sealed inside.",
        "Treasure hunters attempt the ruin. Most die. Its reputation as a death zone solidifies.",
        "The ruin becomes legend. Most cultivators treat it as myth. A few sects keep records.",
        "Wang Lin inherited Tu Si's legacy (E17). The Land of the Ancient God is partially depleted — the core is gone, but the Restriction Mountain trial still stands.",
        "The player can attempt the Restriction Mountain trial (harder now, but the structure remains). The player can learn a partial Ancient God body-cultivation track. Other Ancient God ruins may exist in unexplored regions.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_03_BAI_FAN_INHERITANCE = new ConsequenceChain(
        "CC03", "The Bai Fan Inheritance", "E07",
        "Bai Fan (~−10,000 BW) leaves behind the Six Paths Triple Arts, including Mountain Crumble.",
        "The Thunder Immortal World (where the Collection Pavilion is hidden) becomes a sought-after ruin.",
        "The Collection Pavilion's location is lost to most of the world.",
        "The Collection Pavilion's location is retained by a few ancient sects in fragmented records.",
        "Wang Lin found the Collection Pavilion (E60) and learned the Six Paths Triple Arts. The pavilion is partially depleted but the texts may remain.",
        "The player can find the Collection Pavilion in the Thunder Immortal World (dangerous but accessible). The Six Paths Triple Arts may still be learnable. One of the most accessible high-tier inheritance sites.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_04_WANG_FAMILY_MASSACRE = new ConsequenceChain(
        "CC04", "The Wang Family Village Massacre", "E13-E14",
        "Teng Huayuan slaughters the Wang family (~Year 18-20). Wang Lin exterminates the Teng Clan in revenge (~Year 20-22).",
        "The Wang Family Village and Teng Clan are both annihilated. Wang Lin's defining trauma. His Slaughter Dao begins.",
        "Both sites are ruins. Local cultivators avoid them (bad karma, haunted).",
        "Nature reclaims the ruins. Spirit beasts migrate in. The local spirit veins, untouched, accumulate energy — making the ruins rich cultivation sites.",
        "The Wang Family Village is a pilgrimage site / haunted ruin with ~2000 years of accumulated spiritual energy (prime cultivation but heart-demon risk). The Teng Clan territory is a ruin populated by scavenger sects.",
        "The player can cultivate at the Wang Family Village (high-quality but heart-demon risk from Wang Lin's trauma echoes). The Teng Clan ruins offer scavenging and a minor sect interaction.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_05_HENG_YUE_SECT = new ConsequenceChain(
        "CC05", "The Heng Yue Sect", "E10-E11",
        "Wang Lin joins the Heng Yue Sect (Year 15-16) and finds the Heaven-Defying Bead at the cliff.",
        "Wang Lin begins cultivating. The Heng Yue Sect gains a future heaven-defying cultivator (unknowingly).",
        "Wang Lin leaves. The sect continues without him. The cliff is an unremarkable landmark.",
        "The Heng Yue Sect rises and falls with Zhao Country politics. Without a heaven-defying cultivator, it cannot compete.",
        "The Heng Yue Sect's fate is open (likely diminished or absorbed during post-seal realignment). The cliff is a minor landmark with spiritual significance.",
        "The player can visit the Heng Yue Sect (or its ruins/successor). The cliff is a minor landmark. This is NOT where the player finds their own artifact.",
        CanonEngine.Confidence.IMPLICATION
    );

    public static final ConsequenceChain CHAIN_06_SUZAKU_TOMB = new ConsequenceChain(
        "CC06", "The Suzaku Tomb and the Cultivation Planet Crystal", "E44",
        "Wang Lin raids the Suzaku Tomb (~Year 25-30), finding the Half-Moon Blade and encountering the scattered demon Piao Nanzi/Lou Hou.",
        "Wang Lin obtains the Half-Moon Blade and learns of the planet-level cosmology.",
        "The tomb's outer floors are picked clean by treasure hunters. The deeper floors remain dangerous.",
        "The tomb is a known but dangerous ruin. Most of its treasures are gone.",
        "The Suzaku Tomb is a ruin. The Cultivation Planet Crystal is still in place (Wang Lin did not remove it). The scattered demon's fate is ambiguous.",
        "The player can explore the Suzaku Tomb (dangerous — residual seals, guardian spirits, the scattered demon). The Cultivation Planet Crystal is a plot-level artifact (interacting with it affects Planet Suzaku's stability).",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_07_SOUL_REFINING_SECT = new ConsequenceChain(
        "CC07", "The Soul Refining Sect Inheritance", "E45",
        "Wang Lin inherits the Soul Refining Sect from Dun Tian (~Year 50-70). Dun Tian erases his consciousness to become a soul in the Ten Billion Soul Banner.",
        "Wang Lin becomes the inheritor. Dun Tian exists as a soul within the banner. Wang Lin promises to elevate the sect to 6th-level.",
        "The Soul Refining Sect continues. Wang Lin's promise hangs over them.",
        "The sect is affected by the broader political changes (the seal dissolving, the power realignment).",
        "The Soul Refining Sect is now a 6th-level sect (Wang Lin fulfilled his promise). They revere Wang Lin as patriarch. The Ten Billion Soul Banner is with Wang Lin; the sect retains the teaching.",
        "The player can join the Soul Refining Sect and receive the full transmission. The player can forge their own soul banner using the sect's methods. A directly accessible inheritance.",
        CanonEngine.Confidence.IMPLICATION
    );

    public static final ConsequenceChain CHAIN_08_VERMILION_BIRD_EMPEROR = new ConsequenceChain(
        "CC08", "The Vermilion Bird Divine Emperor Inheritance", "E46",
        "Wang Lin becomes the 6th-Gen Vermilion Bird Divine Emperor (~Year 70-90), inheriting from Lu Yun (5th-Gen).",
        "Wang Lin absorbs Vermilion Bird fire energy. The Four Divine Sect recognizes him.",
        "Wang Lin returns the rein to the Azure Dragon Divine Emperor. The position is vacated.",
        "The Four Divine Sect reorganizes around the vacancy.",
        "The Vermilion Bird Divine Emperor position is OPEN. The Four Divine Sect is seeking a 7th-Gen inheritor.",
        "The player can become the 7th-Gen Vermilion Bird Divine Emperor IF they have Vermilion Bird bloodline compatibility, fire-essence aptitude, and the sect's recognition. A unique position the player can claim through merit.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_09_CLOUD_SKY_SECT_LI_MUWAN = new ConsequenceChain(
        "CC09", "The Cloud Sky Sect and Li Muwan", "E47, E28",
        "Wang Lin kills Sun Zhenwei at Li Muwan's wedding (~Year 85-95). Li Muwan's body perishes (~Year 800); Wang Lin stores her soul in the bead.",
        "Wang Lin's willingness to defy sects for Li Muwan is established. Li Muwan becomes Cloud Sky Sect master.",
        "Li Muwan leads the sect. Wang Lin is often absent. Li Muwan's body is fragile.",
        "Li Muwan's body perishes. The Cloud Sky Sect loses its master. Wang Lin stores her soul.",
        "Li Muwan is ALIVE — restored and Transcended with Wang Lin. The Cloud Sky Sect likely still exists in Chu Country, protected by Wang Lin's legacy.",
        "The player can visit the Cloud Sky Sect. The wedding site is a landmark. The manifestation is warm-reflective here. This is a place with emotional weight, not a quest.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_10_HEAVENLY_FATE_SECT = new ConsequenceChain(
        "CC10", "The Heavenly Fate Sect and the All-Seer", "E20-E22, E49",
        "Wang Lin joins the Heavenly Fate Sect (~Year 100). The All-Seer transmits tainted techniques. Wang Lin kills the All-Seer (~Year 300).",
        "The All-Seer's possession plot is set in motion. The nine Cycle Celestial Refining Tactic and Celestial Slaughter Art are both traps.",
        "Wang Lin discovers the plots, expels the traps, re-cultivates properly. The seven color divisions scatter.",
        "The Heavenly Fate Sect is masterless and diminished. The seven color divisions reform as independent sects or are absorbed.",
        "The All-Seer is DEAD. The Heavenly Fate Sect is masterless and diminished. The tainted techniques still exist in the ruins (possession backdoors inert but structurally unstable).",
        "The player can explore the Heavenly Fate Sect ruins. The techniques are learnable but carry risk (the player must purify them or accept qi deviation risk). An accessible-but-dangerous inheritance.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_11_THUNDER_CELESTIAL_YAO_FAMILY = new ConsequenceChain(
        "CC11", "The Thunder Celestial Tournament and the Yao Family", "E24, E93",
        "The Yao Family issues a kill-order on Wang Lin (~Year 400-500). He destroys multiple planets in response.",
        "Multiple planets are destroyed. Millions die. The Yao Family is decimated. Wang Lin earns the title 'Thunder Celestial Xu Mu.'",
        "The destroyed planets become voids. The Yao Family survivors flee or hide.",
        "The voids become navigational hazards. Merchant routes reroute. New celestial bodies may condense (uncertain within 2000 years).",
        "The Allheaven Star System has permanent void-zones (spatial tears, void beasts, but treasure-rich). The Yao Family is effectively gone. The Thunder Celestial Realm is a high-tier ruin.",
        "The void-zones are explorable (high risk, high reward). The Thunder Celestial Realm is a thunder-cultivation site. The Yao Family's remnants are a historical curiosity.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_12_BLOOD_ANCESTOR = new ConsequenceChain(
        "CC12", "The Blood Ancestor and Yao Xixue", "E23, E57, E94",
        "Wang Lin imprisons Yao Xixue (~Year 300) and kills the Blood Ancestor (~Year 500-600).",
        "Yao Xixue is imprisoned, later sacrifices herself to the Wind Demon. The Blood Ancestor is killed. The Blood Planet is a ruin.",
        "The Blood Planet becomes a dead zone — blood energy saturates the soil. The Demon Spirit Land becomes a forbidden zone.",
        "The blood-saturated soil is both dangerous and valuable for blood-cultivation.",
        "The Blood Planet is a blood-cultivation site. The Demon Spirit Land is a forbidden zone (partially depleted). Yao Xixue departed amnesiac — fate ambiguous.",
        "The Blood Planet is a site for blood-cultivation arts (rare, morally fraught). The Demon Spirit Land is a high-tier dungeon. Yao Xixue, if found, is a tragic historical figure.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_13_DAOIST_WATER_CLOUD_SEA = new ConsequenceChain(
        "CC13", "Daoist Water and the Cloud Sea", "E59, E25",
        "Wang Lin battles Daoist Water twice (~Year 400 and ~Year 500-600), finally slaying him. Slaughter Essence awakens.",
        "Daoist Water is killed. The Water Spirit Sect loses its master. Wang Lin's Slaughter Essence begins to awaken.",
        "The Water Spirit Sect declines. Rival sects absorb its territory. The Cloud Sea reorganizes around Wang Lin.",
        "The Cloud Sea is stable under Wang Lin's implicit protection.",
        "Daoist Water is DEAD. The Water Spirit Sect is diminished or gone. The Cloud Sea is stable and high-tier.",
        "The Cloud Sea is accessible and rich. The Water Spirit Sect's remnants offer water-cultivation fragments. Daoist Water's grave may contain residual water-essence insights.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_14_SEALED_REALM_WAR = new ConsequenceChain(
        "CC14", "The Sealed Realm War", "E26, E27, E70, E71",
        "The Sealed Realm War (~Year 600-700). Wang Lin slaughters thousands of Outer-Realm cultivators, becomes Lord of the Sealed Realm, erects the Unnamed Wheel Formation, subdues the Heaven-Splitting Axe.",
        "Thousands die. The boundary becomes a scarred warzone. Wang Lin becomes Lord of the Sealed Realm. The Heaven-Splitting Axe becomes his weapon.",
        "The Unnamed Wheel Formation replaces the Realm-Sealing Grand Array. The warzone slowly recovers.",
        "The boundary warzone's spiritual scars persist.",
        "Both the Realm-Sealing Grand Array and the Unnamed Wheel Formation are DISSOLVED. The Sealed Realm boundary is OPEN — the two halves are reuniting. The warzone is a historical site. The Heaven-Splitting Axe is DESTROYED (Ch. 1763).",
        "The open boundary means free travel between the Sealed Realm and Outer Realm (a major change). The warzone is a high-tier dungeon. The political realignment is ongoing.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_15_SEVEN_COLORED_DAOIST = new ConsequenceChain(
        "CC15", "The Ancient Immortal Domain and the Seven-Colored Daoist", "E29, E30",
        "Wang Lin discovers the truth (~Year 1000-1200) and kills the Seven-Colored Daoist (~Year 1500), becoming the new owner.",
        "The Seven-Colored Daoist is dead. Ownership transfers to Wang Lin. The seal is dissolved. The Joss Flame harvest stops.",
        "The Cave World reorganizes around the new owner. The ceiling is lifted. Third-Step cultivators can arise.",
        "The mortal populations experience a slow cultivation renaissance (no longer farmed).",
        "The Seven-Colored Daoist is DEAD. Wang Lin is the benevolent owner (he releases, not harvests). The Ancient Immortal Domain is the seat of ownership (not a dungeon). The Joss Flame harvest is broken.",
        "The player lives in a Cave World without a seal, without an active harvest, and with the ceiling lifted. The most permissive era. The player's path is limited only by their dao.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_16_IAC_ASCENSION = new ConsequenceChain(
        "CC16", "The Immortal Astral Continent", "E31-E34, E80, E84",
        "Wang Lin ascends to the IAC (~Year 1500-1600), joins the Great Soul Sect, devours the Dao Demon Sect Master, slays Gu Dao, becomes the 10th Sun.",
        "The IAC's political structure shifts. The Dao Demon Sect is masterless. Wang Lin obtains the Restriction Essence and meets Xuan Luo.",
        "Wang Lin's position as the 10th Sun stabilizes. The Dao Demon Sect is absorbed or declines.",
        "The IAC's 9-Sun structure becomes 10. Canonical time pauses at Wang Lin's Transcendence.",
        "Wang Lin is the 10th Sun. His true body is here, paused at the edge of canon. The Dao Demon Sect is gone. The Great Soul Sect is intact. The IAC is accessible only to Third-Step+ cultivators.",
        "The IAC is the endgame zone. Reaching it requires Third-Step (now possible). When the player arrives, they meet Wang Lin's true body. Canonical time resumes. The player and Wang Lin coexist.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_17_HEAVEN_TRAMPLING_BRIDGES = new ConsequenceChain(
        "CC17", "The Heaven Trampling Bridges", "E34",
        "Wang Lin crosses the 9 Heaven Trampling Bridges (~Year 1800-2000): crosses 7, stops at the 8th, achieves Heaven Trampling without the 9th.",
        "Wang Lin achieves Heaven Trampling (4th Step). The bridges remain.",
        "The bridges remain as a transcendence path.",
        "The bridges are unchanged — Wang Lin crossed them, not destroyed them.",
        "The 9 Heaven Trampling Bridges are the endgame transcendence path, accessible from the IAC. They remain as a trial for future cultivators.",
        "The player can attempt the bridges after reaching the IAC. Wang Lin's path is ONE path, not the ONLY path. The manifestation will not give hints.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_18_LI_MUWAN_REVIVAL = new ConsequenceChain(
        "CC18", "Li Muwan's Death and Revival", "E28, E85",
        "Li Muwan's body perishes (~Year 800). Wang Lin stores her soul. His entire late-game is motivated by her revival. He revives her and they Transcend together.",
        "Wang Lin's motivation crystallizes. The Heaven-Avoiding Coffin and the bead's interior become her soul's shelter.",
        "Wang Lin's pursuit of revival drives his entire late-game arc.",
        "The pursuit culminates in the Root Dao's Reincarnation Essence and the Heaven Trampling step.",
        "Li Muwan is ALIVE and with Wang Lin on the IAC, paused at the edge of canon. The Heaven-Avoiding Coffin is empty.",
        "Li Muwan is not a quest target. She is Wang Lin's wife. The player meets her at the IAC. The emotional weight is part of the world's texture.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_19_NINE_THUNDERS = new ConsequenceChain(
        "CC19", "The Nine Accompanying Thunders", "E68",
        "Wang Lin condenses his 9 Accompanying Thunders (~Year 580). 6 are stolen from the Scatter Thunder Clan. 3 are unprecedented (Ji Thunder, Bloodline Thunder, Defying Thunder).",
        "The Scatter Thunder Clan is devastated. The Thunder Toad is sacrificed.",
        "The Scatter Thunder Clan struggles to survive. The Thunder Toad's lineage may persist.",
        "The Scatter Thunder Clan persists as a minor clan with generational resentment.",
        "The Scatter Thunder Clan is devastated (a minor clan now). The Thunder Toad is gone. The clan harbors resentment but cannot act (Wang Lin is the owner).",
        "The Scatter Thunder Clan territory is a thunder-cultivation site. The clan is wary of outsiders. The player can learn thunder techniques (the clan teaches, grudgingly).",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_20_FOUR_GREAT_RESTRICTIONS = new ConsequenceChain(
        "CC20", "The Four Great Restrictions", "E55, E80",
        "Wang Lin inherits the Four Great Restrictions (Annihilation, Time, Life-Death, Ancient Soul) and condenses the Restriction Essence.",
        "Wang Lin systematically collects all Four. The Restriction Essence becomes one of his 14 Essences.",
        "The original sources are partially depleted. The Restriction Mountain trial still stands.",
        "The restrictions are with Wang Lin. The trial structure remains for future cultivators.",
        "The Four Great Restrictions are with Wang Lin. The Restriction Mountain trial still exists (harder now, but the structure remains).",
        "The player can pursue the Four Great Restrictions from original sources, the Restriction Mountain trial, Wang Lin's disciples, or independent comprehension. Broadly accessible through dedication.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_21_FOURTEEN_ESSENCES = new ConsequenceChain(
        "CC21", "The 14 Essences", "E75-E80, E65",
        "Wang Lin comprehends 14 Essences over ~2000 years: 6 substantial + 4 virtual + 4 special. Each forms an Essence True Body. The 7 Origin Swords are forged.",
        "Wang Lin systematically comprehends each Essence through specific opportunities and comprehensions.",
        "The Essences accumulate. The Essence True Bodies form.",
        "All 14 Essences are fused into Wang Lin's primordial spirit.",
        "The 14 Essences are with Wang Lin (fused into his spirit). The PATH to each Essence is open — the player can comprehend the same Essences through their own cultivation. The Essences are laws, not owned.",
        "The player can pursue any or all of the 14 Essences. NOT mutually exclusive. The player can learn all 14 (as Wang Lin did). Opportunities are scattered across the world.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_22_IMMORTAL_GUARDS = new ConsequenceChain(
        "CC22", "The Immortal Guards", "E48, E108",
        "Wang Lin creates two Immortal Guards: Du Jian (destroyed by Blood Ancestor) and Ta Shan (the only guard to regain freedom, now at Celestial Immortal realm).",
        "Du Jian is created. Ta Shan is created. Both serve Wang Lin.",
        "Du Jian protects Wang Ping. Ta Shan is freed by an ancestral elder.",
        "Du Jian is destroyed. Ta Shan reaches Celestial Immortal independently.",
        "Du Jian is DEAD. Ta Shan is ALIVE and FREE at Celestial Immortal realm. He is a unique NPC — a former Immortal Guard with complex feelings about Wang Lin.",
        "Ta Shan is a potential mentor or ally. The Immortal Guard technique is learnable (Wang Lin did not keep it secret) but ethically fraught.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_23_WANG_LIN_DISCIPLES = new ConsequenceChain(
        "CC23", "Wang Lin's Disciples", "E74, E50",
        "Wang Lin takes disciples: Xi Zifeng (3rd disciple, given the Divine Thunder Blood Sword) and Wang Ping (mortal son).",
        "Xi Zifeng is rescued and elevated. Wang Ping is born and raised.",
        "Xi Zifeng remains in the Cave Dwelling Realm. Wang Ping lives a mortal life.",
        "Xi Zifeng continues cultivating. Wang Ping ages and dies (mortal).",
        "Xi Zifeng is ALIVE in the Cave Dwelling Realm (Jingnie tier, potential mentor). Wang Ping is long dead; his descendants (if any) are mortals.",
        "Xi Zifeng is a high-tier mentor NPC. Wang Ping's descendants (if found) are a historical curiosity.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_24_MORTAL_LIVES = new ConsequenceChain(
        "CC24", "Wang Lin's Mortal Lives", "E18, E50",
        "Wang Lin lives as a mortal twice: for Soul Formation (~Year 60-80) and with Wang Ping (~Year 200-250). These are the foundation of his Life-Death Essence.",
        "Wang Lin comprehends life and death through firsthand experience.",
        "The villages become locally known as places where a cultivator once lived.",
        "The villages accumulate spiritual significance over 2000 years.",
        "The mortal villages are pilgrimage sites with life-death resonance. Meditating here can trigger life-death comprehension events.",
        "These sites are prime cultivation locations for life-death dao comprehension. The emotional weight is part of the experience.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_25_KARMA_WHIP = new ConsequenceChain(
        "CC25", "The Karma Whip and the Karma Domain", "E52, E78, E97",
        "Wang Lin's Soul Lasher (from the Kunji Whip) is transformed into the Karma Whip. The Karma Domain stabilizes. Karma Essence crystallizes.",
        "The Karma Whip becomes Wang Lin's signature weapon. The Karma Domain forms.",
        "The Karma Whip evolves into the Karma Origin Sword.",
        "The Karma Whip/Origin Sword is with Wang Lin to endgame.",
        "The Karma Whip (evolved into Karma Origin Sword) is with Wang Lin. The Tian Yu Sect (original source of the Kunji Whip) is a minor sect or absorbed.",
        "The player can learn karma-attribute techniques from various sources. The Karma Domain is a law, not a unique inheritance. The player can forge their own karma-weapon.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_26_BODY_FIXATION_ART = new ConsequenceChain(
        "CC26", "The Body Fixation Art", "E95",
        "Wang Lin accidentally obtains the Body Fixation Art from Qing Lin's Cave Dwelling. Only 5 people know it (4 living).",
        "Wang Lin learns the 4th-Step Divine Ability. The circle of knowers expands by one.",
        "The technique remains rare. Wang Lin does not widely teach it.",
        "The technique remains known to only 4 living people.",
        "The Body Fixation Art is known to 4 living people (Wang Lin, Qing Shuang, the Seal Sovereign; Shui Daozi is dead). Qing Lin's Cave Dwelling may contain residual insights.",
        "The player can learn it by: finding Qing Shuang or the Seal Sovereign (social path), finding Qing Lin's Cave Dwelling (comprehension path), or reaching 4th-Step (self-reliance path). Nearly inaccessible but multiple paths exist.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_27_DREAM_DAO_TIME_TRAVEL = new ConsequenceChain(
        "CC27", "The Dream Dao and the Time-Travel Subplot", "E62, E69",
        "Wang Lin learns the Dream Dao and creates Flowing Time. He sends his Slaughter Clone Lu Mo back to send the bead to his past self (closed time loop).",
        "The Dream Dao and Flowing Time become Wang Lin's Original Spells. The time loop is set.",
        "The time loop is complete — the bead reached Wang Lin as intended.",
        "The time loop is a closed circuit. No loose ends.",
        "The Dream Dao and Flowing Time are with Wang Lin. The time loop is closed. The Sealer of Realms is a canonical figure (fate open).",
        "Time-travel is NOT available to the player (requires 4th-Step + Dream Dao + Flowing Time + Slaughter True Body — a uniquely Wang Lin configuration). The Dream Dao and Flowing Time are learnable in principle but require Dao Realm cultivation.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_28_TUO_SEN = new ConsequenceChain(
        "CC28", "The Tuo Sen Conflict", "E107",
        "Tuo Sen (rival Ancient God) battles Wang Lin multiple times (~Year 400-600). The Billion Soul Banner is damaged and repaired.",
        "The Billion Soul Banner's souls self-destruct (except 4 main souls). Tuo Sen and Wang Lin clash.",
        "Wang Lin repairs the banner via the Gate of Emptiness. Tuo Sen's fate is unresolved.",
        "Tuo Sen's fate remains a loose end.",
        "Tuo Sen's fate is ambiguous (dead, sealed, or wandering). The Billion Soul Banner is with Wang Lin (repaired, 4 main souls).",
        "Tuo Sen, if encountered, is a high-tier NPC with his own goals (rivalry with Wang Lin, not the player). NOT a replacement antagonist — a historical figure.",
        CanonEngine.Confidence.IMPLICATION
    );

    public static final ConsequenceChain CHAIN_29_XIE_QING_MENTORSHIP = new ConsequenceChain(
        "CC29", "The Xie Qing Mentorship", "E73",
        "Xie Qing (Third Soul) meditates for 800 years, teaches Wang Lin, ends his own life, entrusts his memories to Wang Lin. Wang Lin buries him in Autumn Orchid Valley.",
        "Xie Qing triggers Wang Lin's Nirvana Scryer breakthrough. He dies. Wang Lin buries him.",
        "Autumn Orchid Valley becomes a place of spiritual significance.",
        "The valley's Concept-attributed energy persists.",
        "Xie Qing is DEAD. Autumn Orchid Valley is his burial site — a high-tier cultivation site for Concept comprehension. The 'fish, water, net, fishing' analogy may be inscribed or residual.",
        "Autumn Orchid Valley is a high-tier cultivation site. The manifestation is mournful here. Concept comprehension opportunities.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final ConsequenceChain CHAIN_30_TRANSCENDENCE = new ConsequenceChain(
        "CC30", "Transcendence", "E34, E80-E85",
        "Wang Lin achieves Heaven Trampling (4th Step) by crossing the bridges and comprehending his 14th Essence (Reincarnation) in the Root Dao. He Transcends with Li Muwan.",
        "Wang Lin achieves Transcendence. Canonical time pauses.",
        "The IAC is frozen. Wang Lin's manifestation begins traveling the lower worlds.",
        "The manifestation reaches the player (pre-game). The player begins their journey.",
        "Wang Lin has Transcended. His true body is on the IAC (paused). His manifestation travels with the player. The Root Dao is accessible at Transcendence. Canonical time resumes when the player reaches the IAC.",
        "The player's endgame is to reach the IAC, meet Wang Lin, and pursue their own 4th-Step transcendence. The player's path may mirror or diverge from Wang Lin's. The manifestation witnesses.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** All 30 consequence chains. */
    public static final List<ConsequenceChain> ALL_CHAINS =
        Collections.unmodifiableList(Arrays.asList(
            CHAIN_01_CAVE_WORLD_CREATION, CHAIN_02_TU_SI_LEGACY,
            CHAIN_03_BAI_FAN_INHERITANCE, CHAIN_04_WANG_FAMILY_MASSACRE,
            CHAIN_05_HENG_YUE_SECT, CHAIN_06_SUZAKU_TOMB,
            CHAIN_07_SOUL_REFINING_SECT, CHAIN_08_VERMILION_BIRD_EMPEROR,
            CHAIN_09_CLOUD_SKY_SECT_LI_MUWAN, CHAIN_10_HEAVENLY_FATE_SECT,
            CHAIN_11_THUNDER_CELESTIAL_YAO_FAMILY, CHAIN_12_BLOOD_ANCESTOR,
            CHAIN_13_DAOIST_WATER_CLOUD_SEA, CHAIN_14_SEALED_REALM_WAR,
            CHAIN_15_SEVEN_COLORED_DAOIST, CHAIN_16_IAC_ASCENSION,
            CHAIN_17_HEAVEN_TRAMPLING_BRIDGES, CHAIN_18_LI_MUWAN_REVIVAL,
            CHAIN_19_NINE_THUNDERS, CHAIN_20_FOUR_GREAT_RESTRICTIONS,
            CHAIN_21_FOURTEEN_ESSENCES, CHAIN_22_IMMORTAL_GUARDS,
            CHAIN_23_WANG_LIN_DISCIPLES, CHAIN_24_MORTAL_LIVES,
            CHAIN_25_KARMA_WHIP, CHAIN_26_BODY_FIXATION_ART,
            CHAIN_27_DREAM_DAO_TIME_TRAVEL, CHAIN_28_TUO_SEN,
            CHAIN_29_XIE_QING_MENTORSHIP, CHAIN_30_TRANSCENDENCE
        ));

    /** Get a consequence chain by ID. */
    public static ConsequenceChain getChain(String chainId) {
        for (ConsequenceChain cc : ALL_CHAINS) {
            if (cc.chainId.equals(chainId)) return cc;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 4 — THE INHERITANCE REGISTRY
    // ════════════════════════════════════════════════════════════════════

    /**
     * An inheritance record — classified by 12 questions, per user directive.
     *
     * <p>Per the user's "protagonist finds a way" directive: every inheritance
     * has a {@code protagonistAccessPath}. "Unique" means "one person holds it
     * at a time," not "only one person can ever have it."
     */
    public static final class InheritanceRecord {
        public final String inheritanceId;
        public final String name;
        public final String nameCn;
        public final String canonOwner;
        public final String currentOwner;
        public final String transferConditions;
        public final boolean canBeDuplicated;
        public final boolean canBeTaught;
        public final boolean weakensTeacher;
        public final boolean requiresRecognition;
        public final boolean disappearsAfterInheritance;
        public final boolean karmaTied;
        public final boolean bloodlineTied;
        public final boolean locationTied;
        public final boolean canBeRecreated;
        public final String protagonistAccessPath;
        public final CanonEngine.Confidence canonConfidence;

        public InheritanceRecord(String inheritanceId, String name, String nameCn,
                                 String canonOwner, String currentOwner,
                                 String transferConditions, boolean canBeDuplicated,
                                 boolean canBeTaught, boolean weakensTeacher,
                                 boolean requiresRecognition, boolean disappearsAfterInheritance,
                                 boolean karmaTied, boolean bloodlineTied, boolean locationTied,
                                 boolean canBeRecreated, String protagonistAccessPath,
                                 CanonEngine.Confidence canonConfidence) {
            this.inheritanceId = inheritanceId;
            this.name = name;
            this.nameCn = nameCn;
            this.canonOwner = canonOwner;
            this.currentOwner = currentOwner;
            this.transferConditions = transferConditions;
            this.canBeDuplicated = canBeDuplicated;
            this.canBeTaught = canBeTaught;
            this.weakensTeacher = weakensTeacher;
            this.requiresRecognition = requiresRecognition;
            this.disappearsAfterInheritance = disappearsAfterInheritance;
            this.karmaTied = karmaTied;
            this.bloodlineTied = bloodlineTied;
            this.locationTied = locationTied;
            this.canBeRecreated = canBeRecreated;
            this.protagonistAccessPath = protagonistAccessPath;
            this.canonConfidence = canonConfidence;
        }
    }

    // ── The 14 inheritance records (from CANON_RI_EDGE_OF_CANON.md Part 4) ──

    public static final InheritanceRecord IR_01_HEAVEN_DEFYING_BEAD = new InheritanceRecord(
        "IR01", "The Heaven-Defying Bead", "逆天珠",
        "Wang Lin", "Wang Lin (fused with primordial spirit)",
        "Destiny-bound. The bead chooses its wielder via the closed time loop. Cannot be taken by force.",
        false, false, false, true, false, true, false, false, false,
        "The player CANNOT obtain the Heaven-Defying Bead. HOWEVER — the player can forge their OWN 'defying heaven' artifact through 4th-Step comprehension of the Root Dao's 'defiance' law. This is a parallel, not a duplicate.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_02_TU_SI_ANCIENT_GOD_TACTIC = new InheritanceRecord(
        "IR02", "Tu Si's Ancient God Tactic", "古神诀",
        "Tu Si → Wang Lin", "Wang Lin (fused into body-cultivation)",
        "Inherit from the Land of the Ancient God by completing the Restriction Mountain trial.",
        true, true, false, true, false, false, false, true, true,
        "The player can learn a PARTIAL Ancient God Tactic from the Restriction Mountain trial (core is gone but structure remains). OR find OTHER Ancient God ruins. OR comprehend Ancient God dao independently at high tiers.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_03_UNDERWORLD_ASCENSION_METHOD = new InheritanceRecord(
        "IR03", "The Underworld Ascension Method", "黄泉升窍诀",
        "Situ Nan → Wang Lin", "Wang Lin + the Soul Refining Sect (related methods)",
        "Transmission from Situ Nan or a holder.",
        true, true, false, false, false, false, false, false, true,
        "The player can learn it from: (a) the Soul Refining Sect archives, (b) Situ Nan himself (high-tier encounter), or (c) independent Ji Realm comprehension. Broadly accessible.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_04_SOUL_REFINING_SECT_INHERITANCE = new InheritanceRecord(
        "IR04", "The Soul Refining Sect Inheritance", "炼魂宗传承",
        "Dun Tian → Wang Lin", "The Soul Refining Sect (6th-level, teaches the methods)",
        "Join the Soul Refining Sect and receive transmission.",
        true, true, false, true, false, false, false, true, true,
        "The player can join the Soul Refining Sect (Pilu Kingdom, Planet Suzaku) and receive the full transmission. The player can forge their OWN soul banner. Directly accessible.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_05_NINE_CYCLE_CELESTIAL_REFINING = new InheritanceRecord(
        "IR05", "The Nine Cycle Celestial Refining Tactic (TAINTED)", "九转炼仙诀",
        "The All-Seer → Wang Lin (as Trojan horse)", "The Heavenly Fate Sect ruins",
        "Find the technique in the Heavenly Fate Sect ruins.",
        true, true, false, false, false, false, false, true, true,
        "The player can find it in the Heavenly Fate Sect ruins. It is TAINTED (possession backdoor). The player must purify it (high-comprehension task), accept the risk (qi deviation), or use it as reference. Accessible-but-dangerous.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_06_CELESTIAL_SLAUGHTER_ART = new InheritanceRecord(
        "IR06", "The Celestial Slaughter Art (TAINTED, re-cultivable)", "天杀诀",
        "The All-Seer/Tianyunzi → Wang Lin (re-cultivated properly)", "Wang Lin (purified version); the Heavenly Fate Sect ruins (tainted original)",
        "Find the original (ruins) OR learn the purified version from Wang Lin's lineage.",
        true, true, false, false, false, true, false, false, true,
        "The player can learn from: (a) the ruins (tainted — same risk as IR05), or (b) Wang Lin's purified version via his disciples (Xi Zifeng — earn her trust). The purified version requires the Slaughter Origin.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_07_FOUR_GREAT_RESTRICTIONS = new InheritanceRecord(
        "IR07", "The Four Great Restrictions", "四大禁制",
        "Various → Wang Lin", "Wang Lin; original source locations (partially depleted)",
        "Inherit from the original sources or from Wang Lin's lineage.",
        true, true, false, true, false, false, false, true, true,
        "The player can pursue them from: (a) original source locations, (b) the Restriction Mountain trial, (c) Wang Lin's disciples, or (d) independent restriction comprehension. Broadly accessible through dedication.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_08_SIX_PATHS_TRIPLE_ARTS = new InheritanceRecord(
        "IR08", "The Six Paths Triple Arts (Bai Fan's inheritance)", "六道三术",
        "Bai Fan → Wang Lin", "The Collection Pavilion (Thunder Immortal World — partially depleted)",
        "Find the Collection Pavilion and learn from the texts.",
        true, true, false, false, false, false, false, true, true,
        "The player can find the Collection Pavilion in the Thunder Immortal World (dangerous but accessible). The texts may still be there. One of the most accessible high-tier inheritance sites.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_09_VERMILION_BIRD_EMPEROR_POSITION = new InheritanceRecord(
        "IR09", "The Vermilion Bird Divine Emperor Position", "朱雀神君之位",
        "Lu Yun (5th-Gen) → Wang Lin (6th-Gen)", "VACANT (Wang Lin vacated it)",
        "Vermilion Bird bloodline compatibility + fire-essence aptitude + Four Divine Sect recognition.",
        false, false, false, true, false, false, true, true, false,
        "The player can become the 7th-Gen Vermilion Bird Divine Emperor IF they have bloodline compatibility (achievable), fire-essence aptitude, and the sect's recognition. A unique position the player can claim.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_10_BODY_FIXATION_ART = new InheritanceRecord(
        "IR10", "The Body Fixation Art", "定身术",
        "Qing Lin → Wang Lin (accidentally)", "Wang Lin, Qing Shuang, the Seal Sovereign (4 living knowers)",
        "Learn from one of the 4 knowers OR comprehend independently at 4th-Step.",
        true, true, false, false, false, false, false, true, true,
        "The player can learn it by: (a) finding Qing Shuang or the Seal Sovereign (social path), (b) finding Qing Lin's Cave Dwelling (comprehension path), or (c) reaching 4th-Step (self-reliance path). Nearly inaccessible but multiple paths exist.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_11_FOURTEEN_ESSENCES = new InheritanceRecord(
        "IR11", "The 14 Essences", "十四本源",
        "Wang Lin (comprehended all 14)", "The Essences are LAWS — not owned. The 7 Origin Swords are with Wang Lin.",
        "Self-comprehension. The PATH can be taught; the Essence must be self-comprehended.",
        true, true, false, false, false, false, false, false, false,
        "The player can comprehend ANY or ALL of the 14 Essences through their own cultivation. NOT mutually exclusive. The player can learn all 14 (as Wang Lin did) or specialize. Opportunities scattered across the world.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_12_HEAVEN_SPLITTING_AXE = new InheritanceRecord(
        "IR12", "The Heaven-Splitting Axe", "开天斧",
        "The Realm-Sealing Grand Array (as its spirit) → Wang Lin", "DESTROYED (Ch. 1763 ambush)",
        "N/A (destroyed).",
        false, false, false, true, true, false, false, true, false,
        "The Axe is DESTROYED. The player CANNOT obtain it. HOWEVER — the player can find its REMNANTS at the Realm-Sealing boundary (residual restriction fragments). The player can forge their OWN restriction-weapon through comprehension. A truly lost inheritance — parallel path required.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_13_SEVEN_ORIGIN_SWORDS = new InheritanceRecord(
        "IR13", "The 7 Origin Swords", "本源之剑",
        "Wang Lin (forged, one per Essence)", "Wang Lin",
        "Forged by the wielder from their own Essences.",
        false, true, false, true, false, false, false, true, true,
        "The player CANNOT take Wang Lin's swords. The player CAN forge their OWN Origin Swords by: comprehending an Essence, learning the forging method, and forging at a void gate. The player's swords will be different but functionally equivalent.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    public static final InheritanceRecord IR_14_MANIFESTATION_COMPANION = new InheritanceRecord(
        "IR14", "Wang Lin's Manifestation (as companion)", "王林化身",
        "Wang Lin (it IS Wang Lin)", "The player (as companion — NOT as possession)",
        "The manifestation chose to travel with the player (pre-game). Cannot be transferred or dismissed.",
        false, false, false, true, false, true, false, false, false,
        "The player already HAS the manifestation as a companion. This is a starting condition, not an inheritance to obtain. The manifestation's full memories unlock as the player progresses.",
        CanonEngine.Confidence.IMPLICATION
    );

    /** All 14 inheritance records. */
    public static final List<InheritanceRecord> ALL_INHERITANCES =
        Collections.unmodifiableList(Arrays.asList(
            IR_01_HEAVEN_DEFYING_BEAD, IR_02_TU_SI_ANCIENT_GOD_TACTIC,
            IR_03_UNDERWORLD_ASCENSION_METHOD, IR_04_SOUL_REFINING_SECT_INHERITANCE,
            IR_05_NINE_CYCLE_CELESTIAL_REFINING, IR_06_CELESTIAL_SLAUGHTER_ART,
            IR_07_FOUR_GREAT_RESTRICTIONS, IR_08_SIX_PATHS_TRIPLE_ARTS,
            IR_09_VERMILION_BIRD_EMPEROR_POSITION, IR_10_BODY_FIXATION_ART,
            IR_11_FOURTEEN_ESSENCES, IR_12_HEAVEN_SPLITTING_AXE,
            IR_13_SEVEN_ORIGIN_SWORDS, IR_14_MANIFESTATION_COMPANION
        ));

    /** Get an inheritance record by ID. */
    public static InheritanceRecord getInheritance(String inheritanceId) {
        for (InheritanceRecord ir : ALL_INHERITANCES) {
            if (ir.inheritanceId.equals(inheritanceId)) return ir;
        }
        return null;
    }

    /**
     * Get all inheritances that the player can potentially access (i.e., the
     * protagonistAccessPath is not "CANNOT obtain").
     *
     * <p>Per user directive: the player is a protagonist; there's gotta be a way.
     * Even "unique" inheritances have a path (parallel, self-forged, or earned).
     */
    public static List<InheritanceRecord> getAccessibleInheritances() {
        List<InheritanceRecord> result = new ArrayList<>();
        for (InheritanceRecord ir : ALL_INHERITANCES) {
            // All inheritances have a path per the user's directive.
            // Even IR01 (the bead) and IR12 (the Axe) have parallel paths.
            result.add(ir);
        }
        return result;
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 5 — THE DECISION HORIZON TAXONOMY
    // ════════════════════════════════════════════════════════════════════

    /**
     * The 6 decision horizons — how far ahead an NPC plans.
     *
     * <p>Per user directive: "Some NPCs think minutes. Others years. Others
     * millennia. The Seven-Colored Daoist doesn't make decisions like Teng
     * Huayuan. He thinks on cosmic timescales."
     */
    public enum DecisionHorizon {
        MINUTES(0, "Seconds to hours",
            "Reactive. Acts on immediate stimuli. No long-term planning. Combat reflexes, survival instinct.",
            "Most beasts; low-realm cultivators in combat; panicked NPCs"),
        DAYS(1, "Hours to weeks",
            "Tactical. Plans within an arc. Considers immediate consequences but not generational effects.",
            "Teng Huayuan (clan-scale scheming); Yao Xixue (ambush planning); most sect elders"),
        YEARS(2, "Months to decades",
            "Strategic. Plans across a cultivation phase. Considers sect-level consequences.",
            "The All-Seer (possession plot spans decades); Heng Yue Sect master; most mid-tier faction leaders"),
        DECADES(3, "Decades to centuries",
            "Generational. Plans across multiple mortal generations. Considers regional consequences.",
            "Daoist Water (sect defense across generations); high-tier sect patriarchs; the Blood Ancestor"),
        CENTURIES(4, "Centuries to millennia",
            "Civilizational. Plans across entire cultivation eras. Considers world-level consequences.",
            "Xie Qing (800-year meditation); the Seal Sovereign; high-tier IAC cultivators"),
        MILLENNIA(5, "Millennia to cosmic timescales",
            "Cosmic. Plans on the scale of the cosmology itself. Considers reality-level consequences.",
            "The Seven-Colored Daoist (100,000-year farm); Wang Lin (2000-year revival pursuit); Transcendent-tier NPCs");

        public final int tier;
        public final String timescale;
        public final String description;
        public final String exampleNpcs;

        DecisionHorizon(int tier, String timescale, String description, String exampleNpcs) {
            this.tier = tier;
            this.timescale = timescale;
            this.description = description;
            this.exampleNpcs = exampleNpcs;
        }
    }

    /**
     * The player's decision horizon expands with their realm.
     * A mortal (tier 0) cannot plan in centuries; a Third-Step cultivator can.
     *
     * @param playerAbsoluteTier the player's absolute cultivation tier (0 = mortal)
     * @return the player's current decision horizon
     */
    public static DecisionHorizon playerDecisionHorizon(int playerAbsoluteTier) {
        if (playerAbsoluteTier <= 2) return DecisionHorizon.MINUTES;   // mortal → foundation
        if (playerAbsoluteTier <= 5) return DecisionHorizon.DAYS;      // core → soul formation
        if (playerAbsoluteTier <= 8) return DecisionHorizon.YEARS;     // nascent soul → ascendant
        if (playerAbsoluteTier <= 10) return DecisionHorizon.DECADES;  // heaven blight → early third step
        if (playerAbsoluteTier <= 13) return DecisionHorizon.CENTURIES; // third step → heaven trampling
        return DecisionHorizon.MILLENNIA;                                // transcendence+
    }

    // ════════════════════════════════════════════════════════════════════
    // PART 6 — FACTIONS: ALIVE, GONE, TRANSFORMED
    // ════════════════════════════════════════════════════════════════════

    /** The status of a faction at the edge of canon. */
    public enum FactionStatus {
        ALIVE("The faction still exists and is active."),
        ALIVE_DIMINISHED("The faction exists but is weakened."),
        ALIVE_ELEVATED("The faction exists and has grown stronger."),
        GONE_DESTROYED("The faction was destroyed (canon)."),
        GONE_ABSORBED("The faction was absorbed into another."),
        GONE_MASTERLESS("The faction lost its master and dissolved."),
        TRANSFORMED("The faction has fundamentally changed."),
        VACANT("A position within the faction is open.");

        public final String description;
        FactionStatus(String d) { this.description = d; }
    }

    /** A faction's state at the edge of canon. */
    public static final class FactionState {
        public final String factionId;
        public final String factionName;
        public final FactionStatus status;
        public final String notes;

        public FactionState(String factionId, String factionName, FactionStatus status, String notes) {
            this.factionId = factionId;
            this.factionName = factionName;
            this.status = status;
            this.notes = notes;
        }
    }

    /** All faction states at the edge of canon. */
    public static final List<FactionState> ALL_FACTION_STATES =
        Collections.unmodifiableList(Arrays.asList(
            new FactionState("soul_refining_sect", "Soul Refining Sect", FactionStatus.ALIVE_ELEVATED,
                "6th-level sect (Wang Lin fulfilled his promise). Pilu Kingdom, Planet Suzaku. Reveres Wang Lin as patriarch."),
            new FactionState("cloud_sky_sect", "Cloud Sky Sect", FactionStatus.ALIVE,
                "Chu Country, Planet Suzaku. Li Muwan's former sect. Protected by Wang Lin's legacy."),
            new FactionState("four_divine_sect", "Four Divine Sect", FactionStatus.ALIVE,
                "Vermilion Bird position VACANT. Azure Dragon Divine Emperor leads. Seeking 7th-Gen Vermilion Bird."),
            new FactionState("great_soul_sect", "Great Soul Sect", FactionStatus.ALIVE,
                "IAC, Heavenly Bull Continent. Wang Lin was an elder. Thriving."),
            new FactionState("heng_yue_sect", "Heng Yue Sect", FactionStatus.ALIVE_DIMINISHED,
                "Zhao Country, Planet Suzaku. Likely diminished or absorbed during post-seal realignment."),
            new FactionState("xianxuan_tribe", "Xianxuan Tribe", FactionStatus.ALIVE,
                "Ta Shan (Wang Lin's freed Immortal Guard) is from here. Stable."),
            new FactionState("scatter_thunder_clan", "Scatter Thunder Clan", FactionStatus.ALIVE_DIMINISHED,
                "Lost 6 thunders to Wang Lin. Minor clan now. Generational resentment."),
            new FactionState("blue_silk_clan", "Blue Silk Clan", FactionStatus.ALIVE,
                "Dao Master Blue Dream's clan. Stable."),
            new FactionState("teng_clan", "Teng Clan", FactionStatus.GONE_DESTROYED,
                "Annihilated by Wang Lin (E14)."),
            new FactionState("heavenly_fate_sect", "Heavenly Fate Sect", FactionStatus.GONE_MASTERLESS,
                "All-Seer killed (E22). Sect masterless, diminished to ruin."),
            new FactionState("dao_demon_sect", "Dao Demon Sect", FactionStatus.GONE_MASTERLESS,
                "Master devoured by Wang Lin (E32). Absorbed or declined."),
            new FactionState("dao_devil_sect", "Dao Devil Sect", FactionStatus.GONE_DESTROYED,
                "Annihilated by Wang Lin (E83)."),
            new FactionState("dong_lin_sect", "Dong Lin Sect", FactionStatus.GONE_DESTROYED,
                "Destroyed before/during Wang Lin's era. Ruins remain."),
            new FactionState("water_spirit_sect", "Water Spirit Sect", FactionStatus.GONE_MASTERLESS,
                "Daoist Water killed (E25). Sect declined, likely absorbed."),
            new FactionState("yao_family", "Yao Family", FactionStatus.GONE_DESTROYED,
                "Decimated by Wang Lin's kill-order response (E93)."),
            new FactionState("rank_9_god_sect", "Rank 9 God Sect", FactionStatus.GONE_MASTERLESS,
                "Daoist Water slain. Sect declined."),
            new FactionState("blood_planet_faction", "Blood Ancestor's Faction", FactionStatus.GONE_DESTROYED,
                "Blood Ancestor killed (E94). Planet saturated with blood energy."),
            new FactionState("cave_world", "The Cave World", FactionStatus.TRANSFORMED,
                "Renamed 'Wang Lin's Cave World.' Seal dissolved. Ceiling lifted. Joss Flame harvest stopped."),
            new FactionState("sealed_realm_outer_realm_boundary", "Sealed/Outer Realm Boundary", FactionStatus.TRANSFORMED,
                "OPEN. The two halves are reuniting. Political realignment ongoing."),
            new FactionState("iac_nine_suns", "IAC Nine Suns", FactionStatus.TRANSFORMED,
                "Now 10 Suns (Wang Lin is the 10th). Political structure shifted."),
            new FactionState("vermillion_bird_emperor_position", "Vermilion Bird Divine Emperor Position", FactionStatus.VACANT,
                "Wang Lin vacated it. The Four Divine Sect is seeking a 7th-Gen inheritor. The player can claim this.")
        ));

    // ════════════════════════════════════════════════════════════════════
    // PART 7 — LEGENDARY FIGURES: ALIVE AND DEAD
    // ════════════════════════════════════════════════════════════════════

    /** A legendary figure's state at the edge of canon. */
    public static final class FigureState {
        public final String figureId;
        public final String figureName;
        public final boolean alive;
        public final String location;  // null if dead
        public final String notes;

        public FigureState(String figureId, String figureName, boolean alive, String location, String notes) {
            this.figureId = figureId;
            this.figureName = figureName;
            this.alive = alive;
            this.location = location;
            this.notes = notes;
        }
    }

    /** Figures ALIVE at the edge of canon. Do NOT resurrect the dead ones. */
    public static final List<FigureState> ALIVE_FIGURES =
        Collections.unmodifiableList(Arrays.asList(
            new FigureState("wang_lin", "Wang Lin", true,
                "IAC (true body, paused); lower worlds (manifestation)",
                "The Cave World owner. Transcendent. His manifestation travels with the player."),
            new FigureState("li_muwan", "Li Muwan", true, "IAC (with Wang Lin)",
                "Revived. Transcended with Wang Lin."),
            new FigureState("xi_zifeng", "Xi Zifeng", true, "Cave Dwelling Realm",
                "Wang Lin's 3rd disciple. Jingnie tier. Potential mentor."),
            new FigureState("ta_shan", "Ta Shan", true, "Xianxuan Tribe / wandering",
                "Wang Lin's freed Immortal Guard. Celestial Immortal tier. Complex feelings about Wang Lin."),
            new FigureState("qing_shuang", "Qing Shuang", true, "Unknown",
                "Knows the Body Fixation Art. Qing Lin's daughter."),
            new FigureState("seal_sovereign", "The Seal Sovereign", true, "Unknown",
                "Knows the Body Fixation Art. High-tier."),
            new FigureState("situn_nan", "Situ Nan", true, "With Wang Lin's manifestation",
                "Green Soul fragment. Inside the bead (fused with Wang Lin)."),
            new FigureState("azure_dragon_emperor", "Azure Dragon Divine Emperor", true, "Four Divine Sect",
                "Leading the sect. Seeking 7th-Gen Vermilion Bird."),
            new FigureState("dao_master_blue_dream", "Dao Master Blue Dream", true, "Blue Silk Clan",
                "Void Tribulant+.")
        ));

    /** Figures DEAD at the edge of canon. Do NOT resurrect these. */
    public static final List<FigureState> DEAD_FIGURES =
        Collections.unmodifiableList(Arrays.asList(
            new FigureState("seven_colored_daoist", "Seven-Colored Daoist", false, null,
                "Killed by Wang Lin (E30). Do NOT resurrect."),
            new FigureState("all_seer", "The All-Seer", false, null,
                "Killed by Wang Lin (E22). Do NOT resurrect."),
            new FigureState("teng_huayuan", "Teng Huayuan", false, null,
                "Killed by Wang Lin (E14). Do NOT resurrect."),
            new FigureState("yao_xixue", "Yao Xixue", false, null,
                "Sacrificed to Wind Demon; memories devoured; departed amnesiac. Fate ambiguous — likely dead of old age by edge of canon."),
            new FigureState("daoist_water", "Daoist Water", false, null,
                "Killed by Wang Lin (E25). Do NOT resurrect."),
            new FigureState("blood_ancestor", "Blood Ancestor (Yao Xinghai)", false, null,
                "Killed by Wang Lin (E94). Do NOT resurrect."),
            new FigureState("gu_dao", "Gu Dao", false, null,
                "Killed by Wang Lin (E33). Do NOT resurrect."),
            new FigureState("dao_demon_sect_master", "Dao Demon Sect Master", false, null,
                "Devoured by Wang Lin (E32). Do NOT resurrect."),
            new FigureState("du_jian", "Du Jian (Immortal Guard)", false, null,
                "Destroyed by Blood Ancestor."),
            new FigureState("xie_qing", "Xie Qing", false, null,
                "Ended his own life (E73). Do NOT resurrect."),
            new FigureState("dun_tian", "Dun Tian", false, null,
                "Erased his consciousness (E45). Exists as a soul in the Ten Billion Soul Banner."),
            new FigureState("heaven_splitting_axe", "The Heaven-Splitting Axe", false, null,
                "Destroyed in ambush (Ch. 1763). Do NOT resurrect."),
            new FigureState("bai_fan", "Bai Fan", false, null, "Dead before Wang Lin's era."),
            new FigureState("tu_si", "Tu Si", false, null, "Dead before Wang Lin's era.")
        ));

    /**
     * Check if a figure is alive at the edge of canon.
     * @param figureId the figure's ID
     * @return true if alive, false if dead, null if unknown
     */
    public static Boolean isFigureAlive(String figureId) {
        for (FigureState f : ALIVE_FIGURES) {
            if (f.figureId.equals(figureId)) return true;
        }
        for (FigureState f : DEAD_FIGURES) {
            if (f.figureId.equals(figureId)) return false;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════
    // SUMMARY
    // ════════════════════════════════════════════════════════════════════

    /** Get summary counts for logging. */
    public static Map<String, Integer> getSummaryCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("threatCategories", ALL_THREATS.size());
        counts.put("manifestationComments", ManifestationCompanion.ALL_COMMENTS.size());
        counts.put("consequenceChains", ALL_CHAINS.size());
        counts.put("inheritanceRecords", ALL_INHERITANCES.size());
        counts.put("decisionHorizons", DecisionHorizon.values().length);
        counts.put("factionStates", ALL_FACTION_STATES.size());
        counts.put("aliveFigures", ALIVE_FIGURES.size());
        counts.put("deadFigures", DEAD_FIGURES.size());
        return counts;
    }
}
