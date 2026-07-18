package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * QingLinInheritanceSpec — Qing Lin's spell inheritance.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §7, §8):</b> Wang Lin encountered Qing Lin's
 * legacy in the Demon Spirit Land. Qing Lin was an ancient cultivator who left false caves
 * (decoy inheritance sites) containing celestial-tier treasures: Celestial Wine (Ch. ~625),
 * the Dragon Formation, and other inheritances. Wang Lin mined the false caves systematically.
 */
public final class QingLinInheritanceSpec {

    private QingLinInheritanceSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "qing_lin_inheritance",
            "Qing Lin's Spell Inheritance (False Caves)", "青鳞传承",
            Provenance.explicit("Renegade Immortal", List.of("Ch. ~625 (Demon Spirit Land)"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §7 (Celestial Wine), §8 (Dragon Formation); CANON_RI_COMPLETE_TECHNIQUES.md"),
            "Encountered in the Demon Spirit Land (Ch. ~625). Wang Lin mined Qing Lin's false caves (decoy inheritance sites) systematically.",
            "Qing Lin was an ancient cultivator who left false caves (decoy inheritance sites) in the Demon Spirit Land. Each false cave contained celestial-tier treasures: Celestial Wine (in a jug — Ch. ~625), the Dragon Formation (gifted by Li Muwan), and other inheritances. Wang Lin mined the false caves systematically — distinguishing real inheritances from Qing Lin's decoys using his restriction-reading skill.",
            List.of(
                    "Soul Transformation+ (to enter the Demon Spirit Land)",
                    "Restriction-reading skill (Illusionary Circle, Soul-Piercing Eyes)",
                    "Celestial-tier perception (to distinguish real from decoy)"
            ),
            ResourceCost.divineSense(20, 0,
                    "Mining a false cave: low divine-sense cost (analysis). No cooldown."),
            new ActivationModel(
                    ActivationModel.Trigger.ENVIRONMENTAL_TRIGGER,
                    "Encounter-triggered: Wang Lin enters a false cave and analyzes its restrictions to determine if it's a real inheritance or a decoy.",
                    "Soul Transformation+",
                    List.of("Restriction-reading skill"),
                    List.of("Demon Spirit Land location", "Qing Lin's false caves")
            ),
            new RangeModel(
                    0, 32, 0, RangeModel.Targeting.SINGLE_TARGET,
                    "The inheritance is fixed in place (a cave). Wang Lin must physically enter and analyze."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.ESSENCE_COMPLETION,
                    "inheritance_yield = (cave authenticity) × user_restriction_mastery × user_celestial_perception",
                    "Each essence comprehension improves Wang Lin's ability to detect real inheritances vs decoys.",
                    List.of("Illusionary Circle (restriction analysis)", "Soul-Piercing Eyes"),
                    "Single-state (the inheritance is what it is).",
                    "More essences = more accurate detection of real vs decoy caves."
            ),
            List.of(
                    "Illusionary Circle (analyzes the cave's restrictions)",
                    "Soul-Piercing Eyes (pierces illusions)",
                    "Stronger restriction-reading skill"
            ),
            List.of(
                    "Decoys (false caves look identical to real ones — restriction-reading required to distinguish)",
                    "Limited supply (Qing Lin left a finite number of false caves)"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Inheritance potency scales with the world's law strength.",
                            0.8, "RI: Demon Spirit Land")
            ),
            new VisualDescription(
                    "A cave-entrance with restriction runes covering the walls. Inside: a small chamber with celestial-tier treasures (wine jug, formation array, etc.) and (in decoys) fake or trapped versions.",
                    List.of("Cave stone", "Restriction runes", "Celestial-tier treasures"),
                    "#34495e (cave stone gray)", "#f39c12 (celestial gold)",
                    "Rough cave stone; runes inscribed in relief.",
                    "Variable (cave-sized)",
                    "Cave entrance; faint glow from inside.",
                    "Treasures illuminate; runes pulse with analysis.",
                    "Gold celestial-tier particles around real treasures.",
                    "Warm gold light from celestial-tier items.",
                    "A resonant chime when a real inheritance is detected.",
                    "Silence (the cave is dormant).",
                    "A sharp crack if a decoy trap is triggered."
            ),
            List.of(
                    new StateDescription("Undiscovered", "Cave entrance hidden; no signs of inheritance.",
                            "Initial state", "Wang Lin discovers the cave entrance",
                            "CANON_RI_COMPLETE_ITEMS.md §7"),
                    new StateDescription("Analyzing", "Wang Lin reads the restrictions to determine real vs decoy.",
                            "Wang Lin enters the cave", "Analysis complete",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §D (Illusionary Circle)"),
                    new StateDescription("Real inheritance", "Cave contains genuine celestial-tier treasures.",
                            "Analysis confirms real inheritance", "Wang Lin mines the treasures",
                            "CANON_RI_COMPLETE_ITEMS.md §7"),
                    new StateDescription("Decoy", "Cave contains traps or fake treasures.",
                            "Analysis reveals decoy", "Wang Lin avoids or triggers the trap",
                            "CANON_RI_COMPLETE_ITEMS.md §7")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.RESTRICTIONS,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Wang Lin's restriction-reading skill is required to distinguish real from decoy.",
                            "RI Ch. ~625"),
                    new SystemInteraction(SystemInteraction.SystemType.CELESTIAL_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Celestial-tier perception improves detection accuracy.",
                            "RI Ch. ~625")
            ),
            "Minecraft impl: implement as a structure (cave-entrance + small chamber) that spawns rarely in the Demon Spirit Land biome. The chamber contains a loot table that rolls either 'real inheritance' (celestial-tier item) or 'decoy' (trap block). Use a custom RestrictionReading interaction (right-click with the Illusionary Circle item) to reveal which before opening.",
            "NPC usage (canon): Wang Lin (Ch. ~625). Qing Lin was the original owner (an ancient cultivator who left false-cave decoys). Other cultivators in the Demon Spirit Land era also sought Qing Lin's inheritance; most got the decoys.",
            "Player usage (mod): Locate a Qing Lin Cave structure in the Demon Spirit Land biome. Right-click with the Illusionary Circle item to read the cave's restriction-wave — reveals 'real' vs 'decoy' before opening. Open the loot chest: real → celestial-tier item (Celestial Wine, Dragon Formation scroll, etc.); decoy → trap (spawns hostile mob or applies debuff).",
            "AI usage (Wang Lin NPC): The NPC systematically scans all Qing Lin Cave structures in a 1000-block radius using the Illusionary Circle. The NPC opens only 'real' caves, leaving decoys for rival cultivators. The NPC prioritizes the Dragon Formation scroll (canonical acquisition)."
    );
}
