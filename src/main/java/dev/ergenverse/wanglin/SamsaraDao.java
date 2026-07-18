package dev.ergenverse.wanglin;

import dev.ergenverse.core.Ergenverse;

import java.util.Arrays;
import java.util.List;

/**
 * Samsara Dao — Wang Lin's signature Dao. The 14 Essences + the 9 Heaven Trampling Bridges.
 *
 * <p><b>Canon (C5, CANON_RI_COMPLETE_TECHNIQUES.md):</b>
 *
 * <p><b>The 14 Essences (Origins / 本源):</b>
 * <pre>
 *   6 substantial (each forms a True Body; fuse into Five Elements True Body):
 *     1. Metal           2. Wood            3. Water
 *     4. Fire            5. Earth           6. Thunder
 *
 *   4 virtual (no True Body; conceptual laws):
 *     7.  Life-Death
 *     8.  Cause-Effect (Karma)
 *     9.  True-False
 *     10. Reincarnation (Samsara) ← Wang Lin's 14th in completion order, but 10th in taxonomy
 *
 *   4 special (each has own True Body):
 *     11. Primordial (Taichu / 太初)
 *     12. Silent Extinction (Miemie / 灭绝)
 *     13. Restriction (封印)
 *     14. Slaughter (杀戮)
 * </pre>
 *
 * <p><b>Wang Lin's completion order (canon ch. citations):</b>
 * <pre>
 *   Thunder → Fire → Karma → Life-Death → True-False → Slaughter →
 *   Water → Metal → Wood → Earth → Primordial → Silent Extinction →
 *   Restriction → Reincarnation (14th, final — achieves Heaven Trampling)
 * </pre>
 *
 * <p><b>The 9 Heaven Trampling Bridges (踏天桥):</b>
 * <pre>
 *   1st Bridge: tests sturdiness of heart
 *   2nd Bridge: glimpse of Heaven Trampling power (soul nearly collapses)
 *   3rd Bridge: close mind off inner demons (Wang Lin EMBRACED them instead)
 *   4th Bridge: bridge turned to specks of light that devoured him (woke up)
 *   5th Bridge: crossed via Heaven Trampling step vision (after 4th)
 *   6th Bridge: crossed
 *   7th Bridge: crossed
 *   8th Bridge: STOPPED (couldn't cross)
 *   9th Bridge: NOT STEPPED ON — Wang Lin achieved Heaven Trampling
 *               without crossing, the moment he fully comprehended
 *               the Reincarnation Essence (his 14th).
 * </pre>
 *
 * <p><b>Per the Prime Directive:</b> the Essences are OBJECTIVE laws of the
 * Root Dao. They exist whether the player comprehends them or not. Wang Lin
 * did not CREATE Reincarnation Essence — he comprehended it. Comprehension
 * grants access to the law; it does not make the law.
 *
 * <p><b>Per user correction #5 (NO hardcoded realms):</b> the Essences do
 * not "unlock at realm X" — they are comprehended through insight, life
 * experience, and Dao reflection. Wang Lin's completion order tracks his
 * life events (Slaughter after killing Qing Shui's father; Reincarnation
 * after the Reincarnation Pool). The realm correlations are descriptive.
 */
public final class SamsaraDao {

    private SamsaraDao() {}

    // ─── The 14 Essences ─────────────────────────────────────────────────

    public enum EssenceCategory { SUBSTANTIAL, VIRTUAL, SPECIAL }

    public enum Essence {
        // ── 6 substantial (Five Elements + Thunder) — each forms a True Body; fuse into Five Elements True Body
        METAL   (1,  "Metal",            "金",  EssenceCategory.SUBSTANTIAL, "Obtained by Wang Lin at the Metal Spirit planet. Forms the Metal True Body."),
        WOOD    (2,  "Wood",             "木",  EssenceCategory.SUBSTANTIAL, "Obtained via the Wood Spirit inheritance. Forms the Wood True Body."),
        WATER   (3,  "Water",            "水",  EssenceCategory.SUBSTANTIAL, "Comprehended via 380 million changes at Planet Five Elements; completed 9th cycle at Pill Sea. Forms the Water True Body."),
        FIRE    (4,  "Fire",             "火",  EssenceCategory.SUBSTANTIAL, "Awakened at the Fire Spirit planet. Fuels the Fire Essence True Body. Wang Lin's first substantial essence in completion order."),
        EARTH   (5,  "Earth",            "土",  EssenceCategory.SUBSTANTIAL, "Obtained via the Earth Spirit inheritance. Forms the Earth True Body."),
        THUNDER (6,  "Thunder",          "雷",  EssenceCategory.SUBSTANTIAL, "Wang Lin's FIRST essence in completion order (Ch.127). Heart-attribute thunder fuses with fire essence. Forms the Thunder True Body."),

        // ── 4 virtual — conceptual laws; no True Body
        LIFE_DEATH      (7,  "Life-Death",      "生死",  EssenceCategory.VIRTUAL, "The cycle of mortality. Comprehended via Wang Lin's Domain (living as a mortal among mortals)."),
        KARMA           (8,  "Karma (Cause-Effect)", "因果",  EssenceCategory.VIRTUAL, "The law of karmic consequence. Wang Lin's 3rd essence in completion order. Lets him see karmic threads and manipulate them."),
        TRUE_FALSE      (9,  "True-False",      "真假",  EssenceCategory.VIRTUAL, "The distinction between reality and illusion. Resonates with Wang Lin's right-eye truth-sight."),
        REINCARNATION   (10, "Reincarnation (Samsara)", "轮回", EssenceCategory.VIRTUAL, "Wang Lin's 14th and final essence. Achieved at the Reincarnation Pool (13 years meditation). Comprehension = Heaven Trampling, without crossing the 9th bridge."),

        // ── 4 special — each has own True Body
        PRIMORDIAL         (11, "Primordial (Taichu)",      "太初",   EssenceCategory.SPECIAL, "The origin of all things. Comprehended at the False/True Reincarnation Pool. Own True Body."),
        SILENT_EXTINCTION  (12, "Silent Extinction (Miemie)", "灭绝",   EssenceCategory.SPECIAL, "The cessation of all things. The complement to Primordial. Own True Body."),
        RESTRICTION        (13, "Restriction (Sealing)",    "封印",   EssenceCategory.SPECIAL, "Wang Lin's signature Dao. Restriction-essence is the 4th-Step-tier essence. Own True Body. Resonates with the Realm-Sealing Grand Array."),
        SLAUGHTER          (14, "Slaughter",                "杀戮",   EssenceCategory.SPECIAL, "Wang Lin's first major Dao. Comprehended through a lifetime of killing (initial: slaying Daoist Water; complete: consuming Qing Shui's Black Sword). Own True Body.");

        public final int taxonomyOrder;       // 1..14 in taxonomy order
        public final String name;
        public final String nameCn;
        public final EssenceCategory category;
        public final String description;

        Essence(int taxonomyOrder, String name, String nameCn, EssenceCategory category, String description) {
            this.taxonomyOrder = taxonomyOrder;
            this.name = name; this.nameCn = nameCn;
            this.category = category; this.description = description;
        }
    }

    /**
     * Wang Lin's completion order of the 14 Essences (canon ch. citations in description).
     * This is NOT the taxonomy order — it is the order Wang Lin personally comprehended them.
     */
    public static final List<Essence> WANG_LIN_COMPLETION_ORDER = Arrays.asList(
        Essence.THUNDER,         // 1st — Ch.127
        Essence.FIRE,            // 2nd — Ch.296 (Heart-Pounding Thunder fuses fire+thunder)
        Essence.KARMA,           // 3rd — cause-effect
        Essence.LIFE_DEATH,      // 4th
        Essence.TRUE_FALSE,      // 5th
        Essence.SLAUGHTER,       // 6th — Ch.1509 initial / Ch.1622 complete
        Essence.WATER,           // 7th — Ch.1720 potential / Ch.1843 complete
        Essence.METAL,           // 8th
        Essence.WOOD,            // 9th
        Essence.EARTH,           // 10th
        Essence.PRIMORDIAL,      // 11th — at the Reincarnation Pool (False version)
        Essence.SILENT_EXTINCTION, // 12th
        Essence.RESTRICTION,     // 13th — Wang Lin's signature
        Essence.REINCARNATION    // 14th — FINAL — achieves Heaven Trampling
    );

    // ─── The 9 Heaven Trampling Bridges ──────────────────────────────────

    public enum HeavenTramplingBridge {
        BRIDGE_1(1,  "The First Bridge",  "Tests the sturdiness of one's heart.", "Standard crossing.", true),
        BRIDGE_2(2,  "The Second Bridge", "Glimpse of Heaven Trampling power. Soul nearly collapses if unqualified.", "Granted Wang Lin a glimpse of Heaven Trampling divine sense covering the entire Celestial Clan.", true),
        BRIDGE_3(3,  "The Third Bridge",  "Close one's mind off from inner demons.", "Wang Lin EMBRACED his inner demons instead of closing them out. Used his Heaven-Defying Will to cross.", true),
        BRIDGE_4(4,  "The Fourth Bridge", "The bridge turned into countless specks of light that devoured Wang Lin — he woke up.", "Crossing granted newfound cultivation; removed Ancestral curse from Celestial Ancestor's Head; absorbed the head into the sun using Lian Daozhan's soul as guide.", true),
        BRIDGE_5(5,  "The Fifth Bridge",  "Crossed immediately after the 4th via a vision of a man performing a Heaven Trampling step.", "Wang Lin underwent his final Ancient Clan Tribulation here.", true),
        BRIDGE_6(6,  "The Sixth Bridge",  "Crossed.", "Continued progression toward the 9th.", true),
        BRIDGE_7(7,  "The Seventh Bridge", "Crossed.", "Continued progression toward the 9th.", true),
        BRIDGE_8(8,  "The Eighth Bridge", "STOPPED — Wang Lin could not cross this bridge.", "Stopped here. Required the final insight (Reincarnation Essence) to bypass.", false),
        BRIDGE_9(9,  "The Ninth Bridge",  "Wang Lin achieved Heaven Trampling WITHOUT STEPPING ON this bridge.", "He reached Heaven Trampling the moment he fully comprehended the Reincarnation Essence (his 14th). The 9th bridge is the 'bypass' — final enlightenment transcends the bridge system.", false);

        public final int number;
        public final String name;
        public final String testDescription;
        public final String wangLinResult;
        public final boolean wangLinCrossed;

        HeavenTramplingBridge(int n, String name, String test, String result, boolean crossed) {
            this.number = n; this.name = name; this.testDescription = test;
            this.wangLinResult = result; this.wangLinCrossed = crossed;
        }
    }

    /**
     * The "Dao completion check" — has the cultivator comprehended all 14 Essences?
     * Per canon: full comprehension of all 14 (esp. the 14th, Reincarnation) = Heaven Trampling.
     *
     * <p>This is NOT a realm gate. It is a separate Dao-completion track that
     * Wang Lin walked in parallel to his realm progression. A cultivator can
     * be at Paragon realm with 0 Essences, or at Nirvana Scryer with 14.
     *
     * <p>Per user correction #5: no hardcoded realm gates.
     */
    public static boolean isHeavenTramplingAchieved(boolean[] essencesComprehended) {
        if (essencesComprehended == null || essencesComprehended.length < 14) return false;
        for (boolean b : essencesComprehended) if (!b) return false;
        // All 14 comprehended → Heaven Trampling achieved (canon: Wang Lin
        // achieved it the moment he completed the 14th, Reincarnation Essence)
        return true;
    }

    /**
     * The Samsara Incarnation engine — Wang Lin's signature technique.
     * Canon: "One Billion Samsara Incarnations" (一亿分身).
     *
     * <p>Each incarnation is a fragment of Wang Lin's soul that lives a complete
     * life in a different context (different world, different identity, different
     * circumstances). When the incarnation dies, its experiences return to Wang Lin
     * as Dao insight.
     *
     * <p>Per the Prime Directive: each incarnation is REAL. They are not illusions.
     * They live, suffer, love, die. When Wang Lin reabsorbs them, he gains their
     * lifetime of insight. This is why Samsara Dao is the path to Reincarnation Essence.
     *
     * <p>Per user correction #21 (Soul-Weaving = literal soul manipulation):
     * the incarnations are LITERAL soul fragments, not "spiritual mass".
     */
    public static final class SamsaraIncarnation {
        public final String id;
        public final String contextWorld;        // which world the incarnation lives in
        public final String incarnationIdentity; // the name/role the incarnation believes is real
        public final int lifespanYears;
        public final boolean reabsorbed;
        public final String daoInsightGained;    // null if not yet reabsorbed

        public SamsaraIncarnation(String id, String contextWorld, String incarnationIdentity,
                                  int lifespanYears, boolean reabsorbed, String daoInsightGained) {
            this.id = id; this.contextWorld = contextWorld; this.incarnationIdentity = incarnationIdentity;
            this.lifespanYears = lifespanYears; this.reabsorbed = reabsorbed; this.daoInsightGained = daoInsightGained;
        }
    }

    /**
     * The "is this cultivator on the Samsara Dao track?" check.
     * Canon: Wang Lin walked this track. Other cultivators may walk it too,
     * but the Samsara Incarnation technique is rare (requires the Heaven-Defying
     * Bead or equivalent soul-vessel artifact).
     */
    public static boolean isOnSamsaraTrack(boolean hasHeavenDefyingBead, boolean hasSoulVesselArtifact) {
        return hasHeavenDefyingBead || hasSoulVesselArtifact;
    }

    /**
     * The Dao resonance between Essences.
     * Per canon: certain Essences resonate with each other (Fire+Thunder = Heart-Pounding Thunder;
     * Metal+Wood+Water+Fire+Earth = Five Elements True Body; Primordial+Silent Extinction = origin-cessation pair).
     *
     * <p>This is the "every piece of knowledge interacts with every other piece" principle
     * the user emphasized. Essences are not isolated — they form a web of resonances.
     */
    public static boolean essencesResonate(Essence a, Essence b) {
        // Five Elements resonance (Metal-Wood-Water-Fire-Earth all resonate with each other)
        if (a.category == EssenceCategory.SUBSTANTIAL && b.category == EssenceCategory.SUBSTANTIAL
            && a != Essence.THUNDER && b != Essence.THUNDER) {
            return true;
        }
        // Fire-Thunder resonance (Heart-Pounding Thunder)
        if ((a == Essence.FIRE && b == Essence.THUNDER) || (a == Essence.THUNDER && b == Essence.FIRE)) {
            return true;
        }
        // Primordial-Silent Extinction resonance (origin-cessation pair)
        if ((a == Essence.PRIMORDIAL && b == Essence.SILENT_EXTINCTION)
            || (a == Essence.SILENT_EXTINCTION && b == Essence.PRIMORDIAL)) {
            return true;
        }
        // Life-Death-Reincarnation resonance (the mortality cycle)
        if ((a == Essence.LIFE_DEATH && b == Essence.REINCARNATION)
            || (a == Essence.REINCARNATION && b == Essence.LIFE_DEATH)) {
            return true;
        }
        // Karma-True-False resonance (cause-effect vs reality)
        if ((a == Essence.KARMA && b == Essence.TRUE_FALSE)
            || (a == Essence.TRUE_FALSE && b == Essence.KARMA)) {
            return true;
        }
        // Restriction-Slaughter resonance (sealing vs killing — the two 'action' essences)
        if ((a == Essence.RESTRICTION && b == Essence.SLAUGHTER)
            || (a == Essence.SLAUGHTER && b == Essence.RESTRICTION)) {
            return true;
        }
        return false;
    }
}
