package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * RestrictionFlagSpec — the behavioral specification for Wang Lin's signature Restriction Flag.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §5, CANON_RI_COMPLETE_TECHNIQUES.md §D, ri_wiki/restriction_flag.md):</b>
 * Made from inkstones with 99,999 restrictions to complete. Wang Lin made three:
 * <ol>
 *   <li>1st: intentionally left incomplete so he could summon divine tribulation in danger</li>
 *   <li>2nd: mixture of different restrictions</li>
 *   <li>3rd: pure attack restriction flag</li>
 * </ol>
 *
 * <p><b>Demonstrated uses (chapter-cited):</b>
 * <ul>
 *   <li>Ch. ~180 — first forged after passing the Restriction Mountain Trial (the 4th person ever to do so; trial took 13 years total, 7 of them studying restrictions)</li>
 *   <li>Used throughout the mid-Nascent-Soul era as Wang Lin's primary formation-array anchor</li>
 *   <li>1st flag — incomplete-by-design; deliberately kept unfinished so activating it in danger summons a divine tribulation that strikes Wang Lin's enemies (and Wang Lin himself if he is unprepared)</li>
 *   <li>2nd flag — mixed-restriction composition; flexible tool for unknown-enemy encounters</li>
 *   <li>3rd flag — pure attack restriction flag; dedicated offensive configuration</li>
 *   <li>Planted as a portable formation-array — anchors a sphere of effect around the banner; each of the 99,999 embedded restrictions is a discrete restriction art Wang Lin knew</li>
 *   <li>Refines souls (flag absorbs soul-stuff of enemies killed within its sphere — interaction with Soul Refining Sect method)</li>
 *   <li>Counters / is countered by: Restriction-Breaking Ancient Mirror (Ch. 774, reflects the restriction's logic back); Illusionary Circle (Ch. 180, reads the flag's structure); stronger restrictions (e.g. one of the Four Great Restrictions)</li>
 * </ol>
 *
 * <p><b>Acquisition:</b> Self-forged after Tu Si gave the refining method, post-Restriction
 * Mountain Trial (Ch. ~180, S1 EP32). Wang Lin became the 4th person ever to complete the trial.
 *
 * <p><b>Prerequisites:</b>
 * <ul>
 *   <li>Ancient God Tactic (Tu Si inheritance)</li>
 *   <li>Restriction Mountain Trial completion (7 years of study)</li>
 *   <li>3 Ink Stones per flag</li>
 *   <li>99,999 individual restrictions woven per flag</li>
 * </ul>
 *
 * <p><b>Cost:</b> Divine sense to weave the 99,999 restrictions; qi to activate; cooldown
 * scales with the number of restrictions activated.
 *
 * <p><b>Activation:</b> Wielded as a banner. The flag-bearer channels divine sense to
 * activate embedded restrictions. The 1st flag (incomplete) can be activated to summon
 * divine tribulation — a last-resort weapon.
 *
 * <p><b>Range:</b> The flag is a portable formation-array. Activation extends to a sphere
 * of effect around the banner. Each embedded restriction has its own range.
 *
 * <p><b>Scaling:</b> Scales with the number of restrictions completed (1 to 99,999) and
 * with the user's restriction mastery.
 *
 * <p><b>Counters:</b> Restriction-Breaking Ancient Mirror (reflects restriction logic back).
 * Illusionary Circle (analyzes the flag's structure). Stronger restrictions.
 *
 * <p><b>Weaknesses:</b> The 1st flag is intentionally incomplete — it has a stability flaw.
 * Tracking-vulnerable if the flag's restrictions are traceable.
 *
 * <p><b>Environmental:</b> World-law strength affects the flag's restriction potency. Inside
 * the Realm-Sealing Grand Array, restrictions are suppressed.
 *
 * <p><b>Visual:</b> A banner made of inkstone-material. Surface covered in densely-packed
 * restriction runes. Glows with the embedded restriction type's color.
 *
 * <p><b>States:</b>
 * <ul>
 *   <li>Idle — rolled/furled, runes dormant</li>
 *   <li>Activated — unfurled, runes lit, banners streaming in the wind</li>
 *   <li>Tribulation-summoning (1st flag only) — runes pulse lightning-black, sky darkens</li>
 *   <li>Damaged — torn, runes flicker, partial restrictions fail</li>
 * </ul>
 *
 * <p><b>Interactions:</b>
 * <ul>
 *   <li>Formations — anchors a portable formation-array</li>
 *   <li>Restrictions — IS the restriction system embodied</li>
 *   <li>Divine Sense — powered by divine sense</li>
 *   <li>Tribulation — the 1st flag summons divine tribulation</li>
 * </ul>
 */
public final class RestrictionFlagSpec {

    private RestrictionFlagSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "restriction_flag",
            "Restriction Flag (1st/2nd/3rd)", "禁旗",
            Provenance.explicit("Renegade Immortal",
                    List.of("Ch. ~180 (S1 EP32)", "Ch. ~179-180 (Restriction Mountain Trial)"),
                    5,
                    "CANON_RI_COMPLETE_ITEMS.md §5; CANON_RI_COMPLETE_TECHNIQUES.md §D (Restriction Flags Refining Method)"),
            "Self-forged after Tu Si gave the refining method, post-Restriction Mountain Trial (Ch. ~180, S1 EP32). Wang Lin became the 4th person ever to complete the trial.",
            "Made from inkstones with 99,999 restrictions to complete. Wang Lin made three flags: (1) intentionally left incomplete so he could summon divine tribulation in danger, (2) mixture of different restrictions, (3) pure attack restriction flag. Each flag is a portable formation-array.",
            List.of(
                    "Ancient God Tactic (Tu Si inheritance)",
                    "Restriction Mountain Trial completion (7 years of study)",
                    "3 Ink Stones per flag",
                    "99,999 individual restrictions woven per flag",
                    "Restriction Flags Refining Method (Tu Si inherited)"
            ),
            new ResourceCost(
                    40, 0, 50, 0, 0, 0, 100, 0,
                    "Canon: 'divine sense to weave the 99,999 restrictions; qi to activate.' Per-restriction activation: low qi cost (40 units) + moderate divine-sense cost (50 units). Cooldown: 5 sec (100 ticks) per activation. Weaving a flag from scratch: massive divine-sense expenditure over the 7-year trial."),
            new ActivationModel(
                    ActivationModel.Trigger.GESTURE_HANDSIGN,
                    "Wielded as a banner. The flag-bearer channels divine sense to activate embedded restrictions. The 1st flag (incomplete) can be activated to summon divine tribulation — a last-resort weapon.",
                    "Core Formation+ (Restriction Mountain Trial)",
                    List.of("3 Ink Stones (for forging)", "Restriction Flag Refining Method"),
                    List.of("Restriction Mountain Trial completion", "Tu Si Ancient God inheritance")
            ),
            new RangeModel(
                    1, 32, 16, RangeModel.Targeting.AOE_SPHERE,
                    "Portable formation-array — activation extends to a sphere of effect around the banner. Each embedded restriction has its own range. The 1st flag's tribulation-summon has world-scale reach (calls down lightning from the sky)."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.ESSENCE_COMPLETION,
                    "power = (restrictions_completed / 99999) × user_restriction_mastery × world_law_strength",
                    "Each new essence comprehension amplifies the flag's restriction potency. The Restriction Essence (Wang Lin's 13th) catalyzes the flag's peak form.",
                    List.of("Heart Compass (Annihilation Restriction)", "Destruction Restriction",
                            "Restriction Essence True Body", "Soul Devil Ship"),
                    "1st flag: Incomplete (deliberate) → Complete (via Tu Si Ancient God ink) → peak achievement: 99,999 restrictions woven",
                    "Wang Lin made 3 flags of differing restriction composition. Each is a separate item; the Incomplete → Complete transformation of the 1st is documented in ItemEvolutionRegistry."
            ),
            List.of(
                    "Restriction-Breaking Ancient Mirror (Ch. 774) — reflects the restriction's own logic back at it",
                    "Illusionary Circle (Ch. 180) — wave-based analysis reads the flag's structure",
                    "Stronger restrictions (e.g. one of the Four Great Restrictions)"
            ),
            List.of(
                    "1st flag is intentionally incomplete — stability flaw",
                    "Tracking-vulnerable if the flag's restrictions are traceable (cf. Isolation Restriction Compass discarded Ch. 1864)",
                    "Requires Ink Stones (consumed in forging) — material-bound"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Restriction potency scales with the world's law strength. Inside the Realm-Sealing Grand Array (Sealed Realm), restrictions are suppressed.",
                            0.5, "RI throughout (Realm-Sealing Grand Array suppresses Third-Step cultivation)"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.TRIBULATION_ACTIVE,
                            "The 1st flag's tribulation-summoning ability activates when the flag is incomplete and the user is in danger.",
                            1.0, "RI: 'Left incomplete so he could summon divine tribulation in danger'"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.ANCIENT_GOD_BODY,
                            "Inside an Ancient God's body (Tu Si's body = Land of the Ancient God), the flag's restrictions resonate with the Ancient God Tactic.",
                            1.5, "RI: Tu Si inheritance; Ancient God Tactic powers the flag")
            ),
            new VisualDescription(
                    "A banner made of inkstone-material. Surface covered in densely-packed restriction runes (99,999 in complete form). The banner pole is dark wood or bone. Three variants: 1st (incomplete, lightning-black runes), 2nd (mixed colors), 3rd (pure attack, blood-red runes).",
                    List.of("Inkstone (banner material)", "Restriction runes (99,999)",
                            "Dark wood / bone (pole)"),
                    "#1a1a2e (deep inkstone black)", "#e94560 (restriction rune red)",
                    "Stone-like, slightly translucent. Runes are inscribed in relief — feel raised to the touch.",
                    "1×2×1 (banner-pole + unfurled banner)",
                    "Banner furled around the pole; dormant runes faintly pulse with the user's divine sense.",
                    "Banner unfurls; runes light up sequentially; the activated restrictions project their effect into the surrounding sphere.",
                    "Restriction-rune particles — colored per restriction type. Dense sparks at activation point, expanding outward in a sphere.",
                    "Subtle dark glow; activated runes cast colored light on the surrounding terrain.",
                    "A deep, resonant hum — like a struck bell, but lower.",
                    "Faint whispering (the 99,999 restrictions 'speaking' inaudibly).",
                    "A sharp crack like breaking stone."
            ),
            List.of(
                    new StateDescription("Idle (furled)", "Banner rolled around the pole; runes dormant.",
                            "Item not activated", "User activates or sheathes the flag",
                            "CANON_RI_COMPLETE_ITEMS.md §5"),
                    new StateDescription("Activated (unfurled)", "Banner unfurls; runes light up; sphere of effect surrounds the flag.",
                            "User channels divine sense + activates the flag", "User ceases channeling or runs out of divine sense",
                            "CANON_RI_COMPLETE_ITEMS.md §5"),
                    new StateDescription("Tribulation-summoning (1st flag only)",
                            "Runes pulse lightning-black; sky darkens; divine tribulation descends.",
                            "1st flag (incomplete) + user in danger + user activates the tribulation-summon",
                            "Tribulation completes OR user dies",
                            "CANON_RI_COMPLETE_ITEMS.md §5: 'Left incomplete so he could summon divine tribulation in danger'"),
                    new StateDescription("Damaged", "Banner torn; runes flicker; partial restrictions fail.",
                            "Flag takes battle damage", "User repairs the flag with Ink Stones + divine sense",
                            "CANON_RI_COMPLETE_ITEMS.md §5 (inferred from material basis)"),
                    new StateDescription("Spirit Manifestation (flag-spirit awakens)",
                            "At high restriction-mastery (Wang Lin Restriction Essence True Body era), the 99,999 embedded restrictions cohere into a flag-spirit — a sentient restriction-pattern that can pilot the flag autonomously for short bursts.",
                            "User's restriction mastery crosses the Restriction Essence threshold (Wang Lin's 13th essence)",
                            "User dismisses the spirit OR spirit's autonomy-window closes",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §D (Restriction Essence True Body era, inferred from Wang Lin's restriction-mastery arc)"),
                    new StateDescription("Refining souls (sphere absorbs soul-stuff)",
                            "Enemies killed within the flag's sphere leave soul-orbs that the flag absorbs — interaction with the Soul Refining Sect method.",
                            "Flag is activated AND an enemy dies inside the sphere", "User seals the absorbed souls into a Soul Flag / Banner",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E (interaction with Soul Refining Sect method)")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.FORMATIONS,
                            SystemInteraction.InteractionType.ANCHORS,
                            "Each Restriction Flag IS a portable formation-array. Planting the flag anchors the formation in place.",
                            "RI Ch. ~180 (Restriction Flags Refining Method)"),
                    new SystemInteraction(SystemInteraction.SystemType.RESTRICTIONS,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "The flag IS the restriction system embodied. Each embedded restriction is canonically a discrete restriction art Wang Lin knew.",
                            "RI Ch. ~180"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Powered by divine sense. Weaving the 99,999 restrictions requires divine sense; activation requires divine sense.",
                            "RI Ch. ~180"),
                    new SystemInteraction(SystemInteraction.SystemType.TRIBULATION,
                            SystemInteraction.InteractionType.SUMMONS,
                            "The 1st flag (incomplete) can summon divine tribulation. This is a last-resort weapon.",
                            "RI: 'Left incomplete so he could summon divine tribulation in danger'"),
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "The Restriction Flag Refining Method was inherited from Tu Si (Ancient God).",
                            "RI Ch. ~180 (Tu Si inheritance)"),
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.COUNTERS,
                            "Countered BY the Restriction-Breaking Ancient Mirror (Ch. 774) — the mirror reflects the flag's own restriction-logic back at it. Conversely, a sufficiently complete flag counters lesser treasures by sealing them inside its sphere.",
                            "RI Ch. 774 (Restriction-Breaking Ancient Mirror)"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.STORES,
                            "The flag's sphere absorbs soul-stuff of enemies killed within it (interaction with the Soul Refining Sect method). The absorbed souls can later be sealed into a Soul Flag / Billion Soul Banner.",
                            "RI Ch. ~180 + Ch. 384 (Soul Refining Sect inheritance)"),
                    new SystemInteraction(SystemInteraction.SystemType.AVATARS_CLONES,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "When Wang Lin's Restriction Essence True Body manifests, the flag's restriction-potency is amplified (the True Body IS the Restriction Essence condensed).",
                            "RI: Restriction Essence True Body era")
            ),
            "Minecraft impl: implement as an Item that places a multiblock 'formation array' structure when right-clicked on the ground. The formation-array is a TileEntity with a 16-block-radius AOE effect. Runes are rendered via BlockState particle effects. The 1st flag has a special right-click action 'Summon Tribulation' that calls /summon lightning_bolt ~ ~ ~ with custom damage scaling. Use NBT tag 'Ergen.CanonState' (already exists in WangLinItem) for the Incomplete → Complete state. Use the existing ItemEvolutionRegistry chain 'wanglin/restriction_flag' for state transitions.",
            "NPC usage (canon): Wang Lin only — the flag is the signature treasure he forged after the Restriction Mountain Trial (Ch. ~180). Tu Si (Ancient God) held the refining method before him. No other canon NPC wields a complete Restriction Flag — the trial has only 4 completers in history. The flag-spirit (Restriction Essence True Body era) is Wang Lin-specific.",
            "Player usage (mod): Forge the flag via the Restriction Flag Refining Method item + 3 Ink Stones + a long ritual (approximated as a 7-day in-game 'Restriction Mountain Trial' advancement). Once forged, right-click the ground to plant the flag → activates a 16-block-radius AOE TileEntity. Sneak-right-click with the 1st (incomplete) flag in danger → 'Summon Tribulation' action (custom lightning_bolt with divine-sense damage scaling, 5-sec cooldown). The player must reach at least the 'Restriction Essence' capability before the flag-spirit manifests.",
            "AI usage (Wang Lin NPC): When a Wang Lin NPC encounters a group of enemies, the AI evaluates: if enemy count >= 3, plant the Restriction Flag (3rd, pure-attack variant) and engage within the sphere. If HP < 25% and the 1st (incomplete) flag is in inventory, summon tribulation as a last-resort. The NPC prefers the 2nd (mixed) flag against unknown enemies (flexible). The flag-spirit auto-pilot triggers when the NPC is crowd-controlled and the player has the Restriction Essence capability."
    );
}
