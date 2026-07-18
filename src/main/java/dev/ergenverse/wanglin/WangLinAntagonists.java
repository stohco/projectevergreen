package dev.ergenverse.wanglin;

import dev.ergenverse.canon.CanonEngine;

/**
 * Wang Lin's Antagonists — the two-layer antagonist structure of Renegade Immortal.
 *
 * <p><b>Canon (C5, CANON_RI_COMPLETE_WORLD.md key antagonist finding):</b>
 * <pre>
 *   The All-Seer is NOT Allheaven (the ISSTH antagonist).
 *   All-Seer is the mortal-realm schemer ruling the Heavenly Fate Sect;
 *   the true cosmic antagonist is the Seven-Colored Daoist
 *   (creator of the Cave World, the original "owner" who harvests Joss Flames).
 *   Both must be killed for Wang Lin to escape the farm.
 * </pre>
 *
 * <p>Per user correction #19 (per-novel Heaven-Will antagonists):
 * each Er Gen novel has its OWN local Heaven-Will antagonist. Wang Lin's
 * antagonists are RI-specific. Do NOT conflate with ISSTH's Allheaven.
 *
 * <p>The two-layer structure:
 * <ul>
 *   <li><b>LAYER 1 — Mortal Scheme:</b> The All-Seer plots against Wang Lin
 *       throughout the mortal-realm arcs (Heavenly Fate Sect, Planet Tian Yun).
 *       The All-Seer wants to POSSESS Wang Lin's body. Wang Lin kills him at
 *       the end of the Sealed Realm arc.</li>
 *   <li><b>LAYER 2 — Cosmic Ownership:</b> The Seven-Colored Daoist is the
 *       Cave World's owner. He doesn't care about Wang Lin personally — Wang Lin
 *       is just livestock. But Wang Lin's Third-Step breakthrough threatens the
 *       farm's stability, so the Seven-Colored Daoist must be killed for Wang Lin
 *       to escape. Wang Lin kills him at the end of RI and becomes the new owner.</li>
 * </ul>
 *
 * <p>Per the Prime Directive: the antagonists exist objectively. The All-Seer
 * plots whether the player knows it or not. The Seven-Colored Daoist harvests
 * Joss Flames whether the player knows they're livestock or not. Cultivation
 * reveals the plots; combat resolves them.
 */
public final class WangLinAntagonists {

    private WangLinAntagonists() {}

    /** The antagonist layers. */
    public enum Layer {
        LOCAL_THREAT("A regional/local antagonist whose conflict is geographically bounded. Dangerous within their domain, but not an existential threat to the world order."),
        MORTAL_SCHEME("The mortal-realm schemer. Plots against the protagonist throughout the mortal arcs."),
        COSMIC_OWNERSHIP("The cosmic owner. The world-farm owner who harvests the protagonist's realm.");

        public final String description;
        Layer(String d) { this.description = d; }
    }

    /** The All-Seer — mortal-realm schemer, ruler of the Heavenly Fate Sect. */
    public static final Antagonist ALL_SEER = new Antagonist(
        "all_seer",
        "The All-Seer",
        "全知者",
        Layer.MORTAL_SCHEME,
        "Heavenly Fate Sect (Tianyun Sect) on Planet Tian Yun",
        "nirvana_fruit",  // All-Seer's cultivation peak
        "The All-Seer rules the Heavenly Fate Sect and plots to POSSESS Wang Lin's body. He divided the sect into seven color divisions (red/orange/yellow/green/blue/cyan/purple). He transmitted the Nine Cycle Celestial Refining Tactic to Wang Lin as a Trojan horse (the art creates an immortal-power spiral that the All-Seer can hijack).",
        "Possession of Wang Lin's body to escape his own karmic doom. Wang Lin's body is uniquely suitable (Ancient God + Ji Realm + Heaven-Defying Bead fusion).",
        "Wang Lin kills the All-Seer at the end of the Sealed Realm arc. The All-Seer's plot to possess Wang Lin is reversed — Wang Lin devours him instead.",
        "Direct possession attempt: the All-Seer's spirit attempts to enter Wang Lin's body. Wang Lin's Heaven-Defying Bead + Slaughter Essence + Restriction Dao make his body uninhabitable for the All-Seer's spirit. The All-Seer is devoured.",
        "Distrust of any technique transmitted by a senior cultivator (it may be a Trojan horse). Verify all transmissions against your own Dao resonance before integrating.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** The Seven-Colored Daoist — cosmic owner, creator of the Cave World. */
    public static final Antagonist SEVEN_COLORED_DAOIST = new Antagonist(
        "seven_colored_daoist",
        "The Seven-Colored Daoist",
        "七彩道人",
        Layer.COSMIC_OWNERSHIP,
        "The Cave World (he IS its owner; his seat is the Ancient Immortal Domain at its core)",
        "paragon",  // 4th-Step-adjacent
        "The Seven-Colored Daoist created the Cave World as an artificial farm to harvest Joss Flames. Every cultivator inside is, unknowingly, his livestock. He placed the Realm-Sealing Grand Array (with the Heaven-Splitting Axe as its spirit) to prevent Third-Step cultivators from rising. He does not personally care about Wang Lin — Wang Lin is one of billions of livestock — until Wang Lin's Third-Step breakthrough threatens the farm's stability.",
        "Maintain the Cave World farm. Harvest Joss Flames. Prevent any cultivator from reaching Third-Step (which would challenge his ownership).",
        "Wang Lin kills the Seven-Colored Daoist at the end of RI. Wang Lin then becomes the new Cave World owner (renames it 'Wang Lin's Cave World'), dissolves the seal, and eventually Transcends with Li Muwan.",
        "Direct combat at the Ancient Immortal Domain. The Seven-Colored Daoist has home-field advantage (the Cave World's law bends to him). Wang Lin wins by combining all 14 Essences + the Heaven-Defying Bead's full power + the Heaven Trampling step.",
        "Any 'owner' cultivator whose existence depends on farming a sealed world is structurally vulnerable to a cultivator who can break the seal. The seal is the owner's power AND the owner's weakness.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    // ── Mid-Tier Antagonists (LOCAL_THREAT layer) ────────────────────────────
    // These antagonists threaten Wang Lin within specific arcs/regions but are
    // not existential threats to the world order. Per canon:
    //   - Teng Huayuan: Zhao Country arc — destroyed the Wang Clan
    //   - Yao Xixue: Zhao Country arc — ambushed Wang Lin, wanted his Ji Realm
    //   - Daoist Water (Shui Daozi): Cloud Sea Star System — attacked Wang Lin twice
    //   - Yao Xinghai (Blood Ancestor): Allheaven/Immortal Astral Continent — major regional power

    /** Teng Huayuan — Patriarch of the Teng Clan, destroyer of the Wang Clan. */
    public static final Antagonist TENG_HUAYUAN = new Antagonist(
        "teng_huayuan",
        "Teng Huayuan",
        "藤化元",
        Layer.LOCAL_THREAT,
        "Teng Clan headquarters, Zhao Country, Planet Suzaku",
        "soul_formation",  // Soul Formation stage
        "Teng Huayuan is the Patriarch of the Teng Clan and one of the Four Great Families of Zhao Country. He orchestrated the annihilation of the Wang Clan to seize their ancestral lands and eliminate a political rival. He is the first major antagonist Wang Lin faces, driving Wang Lin's transformation from a naive disciple into a ruthless cultivator.",
        "Territorial expansion and clan dominance in Zhao Country. The Wang Clan stood in the way of Teng Clan hegemony.",
        "Wang Lin slaughters the entire Teng Clan in revenge, killing Teng Huayuan personally. This is one of Wang Lin's most defining moments — his first act of systematic clan extermination, establishing the 'Wang Lin does not forget grudges' pattern that persists throughout the novel.",
        "Direct combat. Teng Huayuan relies on clan formations and numerical superiority. Wang Lin, by this point, has Ji Realm + restriction techniques that bypass conventional defenses. The Teng Clan's collective power is useless against Wang Lin's one-man assault.",
        "Clan patriarchs who build their power on collective formations are individually vulnerable to cultivators with reality-bending abilities (Ji Realm, Restriction Dao) that ignore formation-based defenses.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** Yao Xixue — female cultivator who ambushed Wang Lin for his Ji Realm. */
    public static final Antagonist YAO_XIXUE = new Antagonist(
        "yao_xixue",
        "Yao Xixue",
        "姚夕雪",
        Layer.LOCAL_THREAT,
        "Zhao Country / surrounding wilderness, Planet Suzaku",
        "late_core_formation",  // Late Core Formation
        "Yao Xixue ambushed Wang Lin during his early cultivation years in Zhao Country, seeking to extract his Ji Realm for herself. She represents the predatory culture of Zhao Country's cultivation world — stronger cultivators prey on weaker ones for their unique abilities.",
        "Acquire Wang Lin's Ji Realm ability for her own cultivation advancement.",
        "Killed by Wang Lin during the Zhao Country arc. Her ambush backfired — Wang Lin's Ji Realm proved far more powerful than she anticipated.",
        "Ambush and extraction. She attempted to use suppression techniques to disable Wang Lin and extract his Ji Realm. Wang Lin's combat instincts and Ji Realm activation overwhelmed her.",
        "Ambush-based cultivators who rely on surprise and suppression techniques are vulnerable to opponents whose power activates automatically under threat (Ji Realm is defensive by nature).",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** Daoist Water (Shui Daozi) — Water Spirit Sect elder, attacked Wang Lin in the Cloud Sea. */
    public static final Antagonist DAOIST_WATER = new Antagonist(
        "daoist_water",
        "Daoist Water",
        "水道子",
        Layer.LOCAL_THREAT,
        "Water Spirit Sect, Cloud Sea Star System",
        "nirvana_cleanser",  // Nirvana Cleanser (late Second Step)
        "Daoist Water is an elder of the Water Spirit Sect in the Cloud Sea Star System. He attacked Wang Lin twice — first when Wang Lin arrived in the Cloud Sea, and again in a more organized assault. He represents the entrenched power structures of the Cloud Sea that resist external challengers. Per RITimelineEngine events E59 and E79.",
        "Defend Water Spirit Sect territory and interests in the Cloud Sea. Wang Lin's arrival disrupted the existing power balance.",
        "Killed by Wang Lin during the Cloud Sea arc. Wang Lin's growing power (Soul Refining + multiple Essences) made Daoist Water's attacks increasingly futile.",
        "Organized sect-based assault using Water Spirit Sect formations and multiple elders attacking in coordination. Daoist Water relies on water-attribute techniques and sect formations for power amplification.",
        "Sect elders whose power depends on home-territory formations lose most of their advantage when fighting a cultivator who can ignore or break those formations (Restriction Dao, Slaughter Essence).",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** Yao Xinghai (Blood Ancestor) — Blood Ancestor of the Immortal Astral Continent. */
    public static final Antagonist YAO_XINGHAI = new Antagonist(
        "yao_xinghai",
        "Yao Xinghai (Blood Ancestor)",
        "姚星海（血祖）",
        Layer.LOCAL_THREAT,
        "Immortal Astral Continent",
        "nirvana_fruit",  // Nirvana Fruit (peak Second Step)
        "Yao Xinghai, known as the Blood Ancestor, is a major power on the Immortal Astral Continent. He cultivates using blood-based techniques and commands significant military force. He is one of the most dangerous opponents Wang Lin faces in the IAC arc. Per RITimelineEngine event E94, Wang Lin ultimately kills him.",
        "Dominance over his territory in the Immortal Astral Continent. Wang Lin's arrival and growing power threatened his position.",
        "Killed by Wang Lin in the Immortal Astral Continent arc (E94). The Blood Ancestor's death removes a major obstacle to Wang Lin's operations in the IAC.",
        "Large-scale military assault combined with blood-cultivation techniques. The Blood Ancestor commands armies and uses blood-based divine abilities that drain life force from enemies.",
        "Blood-cultivation techniques that rely on draining life force are countered by cultivators with Life-Death Essence comprehension (Wang Lin) who control the boundary between life and death itself.",
        CanonEngine.Confidence.NOVEL_STATEMENT
    );

    /** All antagonists indexed by ID for lookup. */
    public static final java.util.Map<String, Antagonist> ALL_ANTAGONISTS;
    static {
        java.util.Map<String, Antagonist> m = new java.util.LinkedHashMap<>();
        m.put(ALL_SEER.id, ALL_SEER);
        m.put(SEVEN_COLORED_DAOIST.id, SEVEN_COLORED_DAOIST);
        m.put(TENG_HUAYUAN.id, TENG_HUAYUAN);
        m.put(YAO_XIXUE.id, YAO_XIXUE);
        m.put(DAOIST_WATER.id, DAOIST_WATER);
        m.put(YAO_XINGHAI.id, YAO_XINGHAI);
        ALL_ANTAGONISTS = java.util.Collections.unmodifiableMap(m);
    }

    /** Get all antagonists at a given layer. */
    public static java.util.List<Antagonist> getByLayer(Layer layer) {
        return ALL_ANTAGONISTS.values().stream()
            .filter(a -> a.layer == layer)
            .collect(java.util.stream.Collectors.toList());
    }

    /** Get an antagonist by ID. */
    public static Antagonist getById(String id) {
        return ALL_ANTAGONISTS.get(id);
    }

    /** An antagonist definition. */
    public static final class Antagonist {
        public final String id;
        public final String name;
        public final String nameCn;
        public final Layer layer;
        public final String seat;
        public final String cultivationPeakRealmId;
        public final String description;
        public final String motivation;
        public final String fate;
        public final String confrontationMethod;
        public final String weakness;
        public final CanonEngine.Confidence canonConfidence;

        public Antagonist(String id, String name, String nameCn, Layer layer, String seat,
                          String cultivationPeakRealmId, String description, String motivation,
                          String fate, String confrontationMethod, String weakness,
                          CanonEngine.Confidence canonConfidence) {
            this.id = id; this.name = name; this.nameCn = nameCn;
            this.layer = layer; this.seat = seat; this.cultivationPeakRealmId = cultivationPeakRealmId;
            this.description = description; this.motivation = motivation; this.fate = fate;
            this.confrontationMethod = confrontationMethod; this.weakness = weakness;
            this.canonConfidence = canonConfidence;
        }
    }

    /**
     * The "is this antagonist still alive?" runtime check.
     * Both antagonists start alive. Both are killed by Wang Lin in canon.
     */
    public static boolean isAlive(String antagonistId) {
        // Runtime state would be tracked by the save system.
        // For now, both are alive at world-gen.
        return true;
    }

    /**
     * The "what layer of antagonist is the player currently threatened by?" check.
     * Mortals are threatened by neither (livestock don't draw the owner's attention).
     * Local threats are arc-specific — they activate based on location, not just realm.
     * Cultivators at Ascendant+ draw the All-Seer's attention.
     * Cultivators at Third-Step+ draw the Seven-Colored Daoist's attention.
     *
     * <p>Per user correction #5: this is NOT a realm gate — it's a threat-tier
     * correlation. The antagonists act based on perceived threat, not realm gates.
     */
    public static Layer activeThreatLayer(String playerRealmId) {
        switch (playerRealmId) {
            case "transcendence":
            case "paragon":
            case "ancient":
            case "true_immortal":
                // Third-Step+ — threatens the Cave World farm
                return Layer.COSMIC_OWNERSHIP;
            case "spirit_seizer":
            case "nirvana_fruit":
            case "nirvana_cleanser":
            case "nirvana_scryer":
            case "corporeal_yang":
            case "illusory_yin":
            case "ascendant":
                // Late-mortal to early-Third-Step — draws the All-Seer's attention
                return Layer.MORTAL_SCHEME;
            case "soul_formation":
            case "nascent_soul":
            case "core_formation":
                // Mid-cultivation — local threats (Teng Huayuan-tier)
                return Layer.LOCAL_THREAT;
            default:
                // Mortal through Foundation Establishment — below most antagonists' notice
                // (but a LOCAL_THREAT like Yao Xixue might still ambush for abilities)
                return null;
        }
    }
}
