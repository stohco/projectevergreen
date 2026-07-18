package dev.ergenverse.wanglin;

import dev.ergenverse.core.Ergenverse;

/**
 * The Heaven-Defying Bead (逆天珠) — Wang Lin's defining artifact.
 *
 * <p><b>Canon (C5, CANON_RI_COMPLETE_ITEMS.md item #1):</b>
 * <pre>
 *   Chapter obtained: Ch. 8 (found as a youth inside the Heng Yue Sect stone bead)
 *   Chapter lost:     retained to end (fuses with primordial spirit)
 *   Category:         core / utility
 *   Grade:            transcendent (above celestial / pseudo-immortal)
 *   Origin:           revealed to have been sent back through time by Wang Lin's
 *                     future clone Lu Mo via Dream Dao; originally bestowed by
 *                     Seven-Colored Immortal Venerable to Realm-Sealing Supreme.
 *
 *   What it does:
 *     - Bead with the Five Elements pattern. Originally 9 parts; the bead is its core.
 *     - Interior door leads to a chamber where TIME RUNS 10× OUTSIDE.
 *     - Used to recognize-master Wang Lin after Lu Mo (his clone) blasts it open via Dream Dao.
 *     - Once Five Elements perfected, the master truly owns it.
 *     - Stores Li Muwan's Nascent Soul after her body perishes.
 *     - Reputed to contain Third-Step divine abilities inside.
 *
 *   Special properties:
 *     - Sentient / destiny-bound / fused with primordial spirit
 *     - Time dilation interior (10×)
 *     - Cross-novel artifact (also wielded by Su Ming / Xuan Zang — but Wang Lin's is the original)
 *     - Heaven-defying: allows ONE tier above the world's cultivation ceiling (the "defy heaven" loophole)
 * </pre>
 *
 * <p><b>Per the Prime Directive:</b> the bead EXISTS objectively. Its interior
 * world is real. Its time dilation is real. Mortals cannot perceive it as more
 * than a stone; cultivators at Foundation+ can sense its sentience; cultivators
 * at Soul Formation+ can enter the interior chamber; cultivators at Third-Step+
 * can access the contained divine abilities.
 *
 * <p><b>Per user correction #5 (NO hardcoded realms):</b> the bead does not
 * "unlock at realm X" — it has its own internal progression (Five Elements perfection)
 * that determines what the wielder can access. The realm correlations above are
 * descriptive (canon-observed), not prescriptive.
 */
public final class HeavenDefyingBead {

    private HeavenDefyingBead() {}

    /** The 9 parts of the bead (canon: originally 9 parts; the bead is the core). */
    public enum Part {
        CORE("The Core Bead", "The central bead itself. The 'door' to the interior chamber.", 1),
        METAL("Metal Element Pattern", "First of the Five Elements pattern fragments to align.", 2),
        WOOD("Wood Element Pattern", "Second of the Five Elements pattern fragments to align.", 3),
        WATER("Water Element Pattern", "Third of the Five Elements pattern fragments to align.", 4),
        FIRE("Fire Element Pattern", "Fourth of the Five Elements pattern fragments to align.", 5),
        EARTH("Earth Element Pattern", "Fifth of the Five Elements pattern fragments to align. When all 5 align, the master truly owns the bead.", 6),
        // The remaining 3 parts are the deeper mysteries (canon-attested but less detailed)
        DEEP_MYSTERY_1("The First Hidden Fragment", "Mentioned in canon; specific function less documented. Bridging-policy: intelligently generated as a 'karmic resonance' fragment.", 7),
        DEEP_MYSTERY_2("The Second Hidden Fragment", "Mentioned in canon; specific function less documented. Bridging-policy: intelligently generated as a 'reincarnation imprint' fragment.", 8),
        DEEP_MYSTERY_3("The Third Hidden Fragment", "Mentioned in canon; specific function less documented. Bridging-policy: intelligently generated as a 'Heaven-Trampling bridge resonance' fragment.", 9);

        public final String name;
        public final String description;
        public final int order;  // order to align (core first, then 5 elements, then 3 hidden)

        Part(String n, String d, int o) { this.name = n; this.description = d; this.order = o; }
    }

    /** The interior chamber — where time runs 10× faster outside. */
    public static final class InteriorChamber {
        public static final double TIME_DILATION = 10.0;  // 1 hour inside = 10 hours outside
        public static final int MAX_STORAGE_SLOTS = 27;   // large but finite (canon: stores Li Muwan's Nascent Soul + many items)

        public boolean containsLiMuwanNascentSoul;  // canon: Wang Lin stored her Nascent Soul here after her body perished
        public boolean thirdStepDivineAbilitiesUnlocked;  // canon: reputed to contain Third-Step divine abilities
        public int samsaraIncarnationCount;  // number of Samsara incarnations stored (Wang Lin's signature: 1 billion)

        public InteriorChamber() {
            this.containsLiMuwanNascentSoul = false;
            this.thirdStepDivineAbilitiesUnlocked = false;
            this.samsaraIncarnationCount = 0;
        }
    }

    /** The bead's sentient spirit — initially Situ Nan (the remnant soul inside). */
    public enum Spirit {
        NONE("No spirit bound", "The bead is dormant."),
        SITU_NAN("Situ Nan (司徒南)", "The remnant soul of the 2nd-Generation Vermilion Bird. Wang Lin's first mentor. Transmitted the Underworld Ascension Method + Vermilion Bird Burning Heaven Art. Canon: departs once Wang Lin's cultivation suffices."),
        WANG_LIN_PRIMORDIAL("Wang Lin's Primordial Spirit", "End-state: the bead fuses with Wang Lin's primordial spirit. He IS the bead. Canon: end of RI.");

        public final String name;
        public final String description;
        Spirit(String n, String d) { this.name = n; this.description = d; }
    }

    /**
     * The "defy heaven" loophole — the bead allows ONE absolute tier above the world's ceiling.
     * This is the mechanical expression of the bead's heaven-defying nature.
     *
     * <p>Canon: Wang Lin consistently broke through ceilings he shouldn't have
     * (Third-Step inside the Sealed Realm, etc.) because of the bead.
     *
     * <p>Per user correction #5: this is NOT a realm-gate unlock — it's an
     * absolute-tier delta. The bead bends the world's law by 1 tier, no more.
     */
    public static int heavenDefyingTierBonus(int worldCeiling) {
        return worldCeiling + 1;
    }

    /**
     * The bead's interior chamber — accessed by the wielder.
     * Time inside runs 10× slower than outside (1 hour inside = 10 hours outside).
     * This means: cultivation inside is 10× faster in outside-time terms.
     *
     * <p>Canon: Wang Lin used this to condense essences and cultivate
     * techniques far faster than outside cultivators could.
     */
    public static double interiorTimeDilation() {
        return InteriorChamber.TIME_DILATION;
    }

    /**
     * Store Li Muwan's Nascent Soul in the bead.
     * Canon: after Li Muwan's body perished, Wang Lin stored her Nascent Soul
     * inside the bead to preserve her until he could rebuild her a body.
     * This is the entire motivation for Wang Lin's late-game actions
     * (becoming the Cave World owner, crossing the Heaven Trampling Bridges).
     */
    public static void storeLiMuwanNascentSoul(InteriorChamber chamber) {
        chamber.containsLiMuwanNascentSoul = true;
        Ergenverse.LOGGER.info("[Ergenverse] HeavenDefyingBead: Li Muwan's Nascent Soul stored. Wang Lin's motivation is now absolute — he WILL revive her.");
    }

    /**
     * The bead's cross-novel resonance.
     * Canon: the bead (or beads like it) appears in multiple Er Gen novels
     * (Su Ming / Xuan Zang). Wang Lin's is the original. The others are echoes
     * or parallel incarnations of the same fundamental artifact.
     *
     * <p>Per user correction #11 (Wang Baole ≠ Wang Lin reincarnation) and
     * correction #19 (Pluck Stars from Vault reveals complete existence):
     * the bead is NOT a reincarnation vehicle — it is an objective artifact
     * that exists across the multiverse with its own identity.
     */
    public static final String[] CROSS_NOVEL_WIELDERS = {
        "Wang Lin (Renegade Immortal) — the original wielder; bead fuses with primordial spirit at Heaven Trampling",
        "Su Ming (Pursuit of the Truth) — wields a bead; cross-novel artifact resonance",
        "Xuan Zang (referenced in canon) — wields a bead; cross-novel artifact resonance"
    };

    /**
     * The bead's "sentience" level — grows as the wielder aligns the Five Elements.
     * At 0 (no parts aligned): the bead is a stone. At 6 (core + 5 elements):
     * the bead is fully sentient and owned. At 9 (all parts): the bead can
     * resonate with the Heaven Trampling Bridges (bridging-policy: intelligently
     * generated based on canon hint of "Third-Step divine abilities inside").
     */
    public static int sentienceLevel(int partsAligned) {
        if (partsAligned <= 0) return 0;        // stone
        if (partsAligned == 1) return 1;        // core only — faint pulse
        if (partsAligned <= 6) return 2 + (partsAligned - 2);  // 2..6 — growing sentience
        return 7 + (partsAligned - 7);          // 7..9 — full sentience + bridge resonance
    }
}
