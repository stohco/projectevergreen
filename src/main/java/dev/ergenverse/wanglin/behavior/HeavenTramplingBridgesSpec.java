package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * HeavenTramplingBridgesSpec — the 9 Heaven Trampling Bridges.
 *
 * <p><b>Canon (SamsaraDao.java HeavenTramplingBridge enum, CANON_RI_COMPLETE_TECHNIQUES.md §J):</b>
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
 * <p>One spec with 9 sub-states (each bridge is a documented state of the
 * Wang Lin Heaven-Trampling journey).
 */
public final class HeavenTramplingBridgesSpec {

    private HeavenTramplingBridgesSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "heaven_trampling_bridges",
            "9 Heaven Trampling Bridges", "踏天桥",
            Provenance.explicit("Renegade Immortal", List.of("throughout late-game", "Ch. 2087 (Revealed)"), 5,
                    "SamsaraDao.java HeavenTramplingBridge enum; CANON_RI_COMPLETE_TECHNIQUES.md §J"),
            "Wang Lin walks the 9 Heaven Trampling Bridges during his late-game arc. He crosses bridges 1-7, stops at 8, and bypasses the 9th entirely by comprehending the Reincarnation Essence. Per-bridge canon tribulation names (per xian-ni.fandom.com/wiki/Half_Heaven_Trampling): 1st=Universal Law & Origin Soul Fusion; 2nd=Dao Heart Tribulation; 3rd=Inner Demon Tribulation; 4th=Unknown; 5th=Transcending Reincarnation; 6th=Unknown; 7th=Unknown; 8th=Unknown; 9th=Unknown (leads to Heaven Trampling realm).",
            "The 9 Heaven Trampling Bridges (踏天桥) are the canonical test-of-worthiness for Heaven Trampling (the 4th Step). Each bridge tests a different aspect: sturdiness of heart (1st), Heaven Trampling power glimpse (2nd), inner demons (3rd), bridge devouring the walker (4th), Heaven Trampling step vision (5th), standard crossings (6th-7th), the stopped bridge (8th), and the bypassed bridge (9th — Wang Lin achieved Heaven Trampling without stepping on it, by completing the Reincarnation Essence).",
            List.of(
                    "Comprehension of all 14 Essences (esp. the 14th, Reincarnation)",
                    "Heaven-Defying Will (to embrace inner demons at the 3rd bridge)",
                    "Reincarnation Essence (for the 9th bridge bypass)"
            ),
            ResourceCost.divineSense(100, 0,
                    "Each bridge crossing: extreme divine-sense cost. The soul nearly collapses at the 2nd bridge."),
            new ActivationModel(
                    ActivationModel.Trigger.ENVIRONMENTAL_TRIGGER,
                    "Bridges are encountered as Wang Lin walks the Heaven Trampling path. Each bridge is an environmental trigger — Wang Lin must step on it (or, in the 9th's case, NOT step on it).",
                    "Third Step+ (approaching Heaven Trampling)",
                    List.of("14 Essences (esp. Reincarnation for the 9th bypass)"),
                    List.of("Heaven Trampling path location")
            ),
            new RangeModel(
                    0, -1, 0, RangeModel.Targeting.SELF_ONLY,
                    "The bridges test the walker alone. The 2nd bridge's glimpse of Heaven Trampling power covers the entire Celestial Clan."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.ESSENCE_COMPLETION,
                    "crossing_success = (essences_comprehended / 14) × heaven_defying_will",
                    "Each essence comprehension increases crossing success probability. The 14th (Reincarnation) is the bypass for the 9th bridge.",
                    List.of("14 Essences", "Heaven-Defying Will", "Heaven-Defying Bead"),
                    "1st (Ch. ?) → 2nd (Ch. ?) → 3rd (Ch. ?) → 4th (Ch. ?) → 5th (Ch. ?) → 6th (Ch. ?) → 7th (Ch. ?) → 8th STOPPED → 9th BYPASSED via Reincarnation Essence (Ch. 2087)",
                    "More essences = higher crossing success. The 14th bypasses the 9th."
            ),
            List.of(
                    "Insufficient essence comprehension",
                    "Inner demons (at the 3rd bridge — Wang Lin embraced them instead)",
                    "Heaven-Defying Will deficit"
            ),
            List.of(
                    "The 8th bridge CANNOT be crossed by force — requires the final insight (Reincarnation Essence)",
                    "The 9th bridge CANNOT be stepped on — Wang Lin bypassed it"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "The bridges exist at the edge of the Root Dao — world law strength is absolute here.",
                            1.0, "RI: Root Dao substrate"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.REINCARNATION_POOL,
                            "At the Reincarnation Pool, the 9th bridge bypass becomes possible (Reincarnation Essence comprehension).",
                            1.5, "RI Ch. 1943+")
            ),
            new VisualDescription(
                    "Each bridge is a vast stone bridge spanning an infinite void. The void below is dark and silent. Each bridge has its own visual character: 1st (solid stone), 2nd (crackling with Heaven Trampling power), 3rd (manifested inner demons on the bridge), 4th (bridge dissolves into specks of light), 5th (vision of a man performing a Heaven Trampling step), 6th-7th (solid stone, peaceful), 8th (blocked by an invisible wall), 9th (untouched — Wang Lin stands before it but does not step on it).",
                    List.of("Ancient stone", "Void (the abyss below)", "Heaven Trampling light"),
                    "#2c3e50 (stone black)", "#f1c40f (Heaven Trampling gold)",
                    "Ancient, immovable stone. Each bridge feels older than the world.",
                    "Variable (bridge span — vast, perhaps 100+ blocks)",
                    "Silent, motionless. Faint glow at the far end.",
                    "Bridge reacts to the walker: demons manifest, specks devour, walls block.",
                    "Per-bridge: demon particles (3rd), light specks (4th), wall-shimmer (8th).",
                    "Per-bridge: red (demons), gold (Heaven Trampling), white (9th bypass).",
                    "Silence (the void is silent).",
                    "Silence.",
                    "A resonant boom when a bridge is crossed or stopped at."
            ),
            List.of(
                    new StateDescription("1st Bridge: Universal Law & Origin Soul Fusion (sturdiness of heart)", "Tests the walker's heart-sturdiness.",
                            "Walker steps on the 1st bridge", "Walker's heart proves sturdy",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_1; xian-ni.fandom.com/wiki/Half_Heaven_Trampling"),
                    new StateDescription("2nd Bridge: Dao Heart Tribulation (Heaven Trampling glimpse)", "Grants a glimpse of Heaven Trampling power; soul nearly collapses if unqualified.",
                            "Walker steps on the 2nd bridge", "Walker survives the glimpse (Wang Lin did — granted a glimpse of Heaven Trampling divine sense covering the entire Celestial Clan)",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_2; xian-ni.fandom.com/wiki/Half_Heaven_Trampling"),
                    new StateDescription("3rd Bridge: Inner Demon Tribulation", "Tests whether the walker closes mind off inner demons.",
                            "Walker steps on the 3rd bridge", "Wang Lin EMBRACED his inner demons instead of closing them out. Used his Heaven-Defying Will to cross.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_3; xian-ni.fandom.com/wiki/Half_Heaven_Trampling"),
                    new StateDescription("4th Bridge: UNKNOWN tribulation (bridge devours walker)", "The bridge turned into countless specks of light that devoured Wang Lin — he woke up.",
                            "Walker steps on the 4th bridge", "Wang Lin woke up. Crossing granted newfound cultivation; removed Ancestral curse from Celestial Ancestor's Head; absorbed the head into the sun using Lian Daozhan's soul as guide.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_4; xian-ni.fandom.com/wiki/Half_Heaven_Trampling (4th listed as 'Unknown')"),
                    new StateDescription("5th Bridge: Transcending Reincarnation (Heaven Trampling step vision)", "Crossed immediately after the 4th via a vision of a man performing a Heaven Trampling step.",
                            "Walker steps on the 5th bridge", "Wang Lin underwent his final Ancient Clan Tribulation here.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_5; xian-ni.fandom.com/wiki/Half_Heaven_Trampling"),
                    new StateDescription("6th Bridge: UNKNOWN tribulation (crossed)", "Standard crossing.",
                            "Walker steps on the 6th bridge", "Crossed.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_6; xian-ni.fandom.com/wiki/Half_Heaven_Trampling (6th listed as 'Unknown')"),
                    new StateDescription("7th Bridge: UNKNOWN tribulation (crossed)", "Standard crossing.",
                            "Walker steps on the 7th bridge", "Crossed.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_7; xian-ni.fandom.com/wiki/Half_Heaven_Trampling (7th listed as 'Unknown')"),
                    new StateDescription("8th Bridge: UNKNOWN tribulation (STOPPED)", "Wang Lin could not cross this bridge.",
                            "Walker steps on the 8th bridge", "Required the final insight (Reincarnation Essence) to bypass.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_8; xian-ni.fandom.com/wiki/Half_Heaven_Trampling (8th listed as 'Unknown')"),
                    new StateDescription("9th Bridge: UNKNOWN tribulation (BYPASSED — bridge leads to Heaven Trampling realm)", "Wang Lin achieved Heaven Trampling WITHOUT STEPPING ON this bridge.",
                            "Wang Lin comprehends the Reincarnation Essence (his 14th)", "Heaven Trampling achieved (Ch. 2087). The 9th bridge is the 'bypass' — final enlightenment transcends the bridge system.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_9; xian-ni.fandom.com/wiki/Heaven_Trampling (9th leads to Heaven Trampling realm)")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.REINCARNATION,
                            SystemInteraction.InteractionType.REQUIRES,
                            "The 9th bridge bypass requires the Reincarnation Essence (Wang Lin's 14th).",
                            "RI Ch. 2087"),
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "The 4th bridge crossing triggered Wang Lin's final Ancient Clan Tribulation.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_5"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The 2nd bridge grants a glimpse of Heaven Trampling divine sense covering the entire Celestial Clan.",
                            "SamsaraDao.java HeavenTramplingBridge.BRIDGE_2")
            ),
            "Minecraft impl: implement as a dimension (Heaven Trampling Path) with 9 sequential bridge structures. Each bridge is a structure the player must walk across. Per-bridge effects: 1st (heart-sturdiness check via a custom 'HeartSturdiness' capability), 2nd (grants 'HeavenTramplingGlimpse' effect — massive divine sense expansion for a moment), 3rd (spawns inner-demon mobs the player must NOT attack — embrace them by walking through), 4th (teleports the player to a 'devoured' sub-dimension; on wake-up grants the Celestial Ancestor head item), 5th (grants 'HeavenTramplingStep' spell), 6th-7th (standard crossings), 8th (invisible wall — player cannot proceed without the Reincarnation Essence capability), 9th (player stands before it but a 'BYPASS' action is available if Reincarnation Essence is complete).",
            "NPC usage (canon): Wang Lin only — the 9 Bridges are his Heaven Trampling journey (Ch. 2062-2087). No other cultivator in canon achieves all 9. The 4th Step ancestor (Lian Daozhen) is the only other attested crosser, and he only crossed 4. Wang Lin is the only one to BYPASS the 9th (via Reincarnation Essence completion).",
            "Player usage (mod): Accessed via a one-way portal (the Heaven Trampling Path dimension). The player walks each bridge in sequence; failing a bridge teleports the player back to bridge 1 (no permadeath — but loses progress). Bridge 9's 'BYPASS' action unlocks only if the player has the Reincarnation Essence capability (end-game achievement). Crossing (or bypassing) the 9th grants the Heaven Trampling rank (final cultivation tier).",
            "AI usage (Wang Lin NPC): The NPC's Heaven Trampling journey is a scripted end-game arc — not an open AI loop. The NPC reaches the Path at the Ch. 2062 story beat (chronological trigger). On the 3rd bridge the NPC's AI deliberately EMBRACES the inner-demon mobs (does not attack them) — canon-faithful behavior. On the 9th bridge the NPC chooses 'BYPASS' if the Reincarnation Essence capability is set; otherwise the NPC waits (does not cross)."
    );
}
