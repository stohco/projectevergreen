package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * SituNanInheritanceSpec — Situ Nan's inheritance (the bead's spirit mentor).
 *
 * <p><b>Canon (CANON_RI_COMPLETE_TECHNIQUES.md §A):</b> Situ Nan was originally the
 * Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird.
 * Fled into the Heaven Defying Pearl; met Wang Lin and became his first mentor.
 * Sacrificed his remaining power to save Wang Lin; reincarnated on IAC as 'Si Nan'
 * Grand Marshal of Wu Xuan Country.
 *
 * <p>Transmitted to Wang Lin: Underworld Ascension Method, Vermilion Bird Burning
 * Heaven Art, Finger of Death / Demonic / Underworld trio, Yellow Springs Finger,
 * Samsara Eye, Heavenly Eye.
 */
public final class SituNanInheritanceSpec {

    private SituNanInheritanceSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "situ_nan_inheritance",
            "Situ Nan's Inheritance (Bead-Spirit Mentor)", "司徒南传承",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 8+", "Ch. 86 (Underworld Ascension)"), 5,
                    "CANON_RI_COMPLETE_TECHNIQUES.md §A; situ_nan_baidu"),
            "Situ Nan was originally the Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird. Fled into the Heaven Defying Pearl; met Wang Lin and became his first mentor. Sacrificed his remaining power to save Wang Lin; reincarnated on IAC as 'Si Nan' Grand Marshal of Wu Xuan Country.",
            "Situ Nan transmitted to Wang Lin: the Underworld Ascension Method (Ch. 86) — a Ji Realm cultivation method that requires extreme-Yin sites; the Vermilion Bird Burning Heaven Art — the foundational Vermilion Bird art; the Finger of Death / Demonic / Underworld trio (incomplete celestial spells); the Yellow Springs Finger (3rd-Step finger strike); the Samsara Eye (karma-glimpsing eye); and the Heavenly Eye (sees through disguises).",
            List.of(
                    "Heaven-Defying Bead (Situ Nan lives inside as bead-spirit)",
                    "Foundation Establishment+ (for Underworld Ascension Method)",
                    "Vermilion Bird Sequence (for Vermilion Bird Burning Heaven Art)",
                    "Extreme-Yin cultivation site (for Underworld Ascension)"
            ),
            ResourceCost.divineSense(10, 0,
                    "Transmission: low divine-sense cost. Practiced techniques have their own costs."),
            new ActivationModel(
                    ActivationModel.Trigger.RITUAL_CEREMONY,
                    "Situ Nan transmits techniques via direct soul-to-soul communication inside the bead's interior chamber. Practiced techniques have their own activation models (see Underworld Ascension Method, Vermilion Bird Burning Heaven Art, Finger of Death trio).",
                    "Foundation Establishment+",
                    List.of("Heaven-Defying Bead (with Situ Nan as spirit)"),
                    List.of("Situ Nan's permission (canon gate)")
            ),
            new RangeModel(
                    0, 0, 0, RangeModel.Targeting.SELF_ONLY,
                    "Transmission is internal (bead's interior chamber). Practiced techniques have their own ranges."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.CULTIVATION_REALM,
                    "transmission_depth = user_cultivation × situ_nan_remaining_power",
                    "Situ Nan sacrificed his remaining power to save Wang Lin — transmissions taper off as Situ Nan's power depletes. Eventually Situ Nan reincarnates as 'Si Nan' on the IAC.",
                    List.of("Underworld Ascension Method", "Vermilion Bird Burning Heaven Art",
                            "Finger of Death trio", "Yellow Springs Finger", "Samsara Eye", "Heavenly Eye"),
                    "Single-state (the inheritance is what it is).",
                    "Each transmission is discrete; the inheritance does not 'scale' as a whole."
            ),
            List.of(
                    "Situ Nan's depletion (he sacrificed his power)",
                    "Situ Nan's reincarnation (he leaves the bead)"
            ),
            List.of(
                    "Requires Situ Nan's permission (canon gate — Wang Lin cannot redistribute Situ Nan's contracted arts without his permission)",
                    "Vermilion Bird Burning Heaven Art requires the Vermilion Bird Sequence to fully unlock",
                    "Finger of Death trio is incomplete — backfires if user's realm is too low"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.EXTREME_YIN_SITE,
                            "Underworld Ascension Method requires extreme-Yin sites for cultivation.",
                            1.5, "RI Ch. 86: 'Uses places of extreme Yin as cultivation sites'"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.TIME_DILATION_FIELD,
                            "Inside the bead's interior chamber (10× time dilation), transmission is more efficient.",
                            1.2, "RI; HeavenDefyingBead.java")
            ),
            new VisualDescription(
                    "Situ Nan appears as a remnant soul inside the bead's interior chamber. Visual: a translucent humanoid figure, green-tinted (Green Soul origin). When transmitting, the figure extends a hand and pulses green light into Wang Lin.",
                    List.of("Soul-stuff (Situ Nan's remnant)", "Green light (transmission)"),
                    "#27ae60 (Green Soul green)", "#ecf0f1 (soul-stuff white)",
                    "Ethereal, translucent. Slightly fading (Situ Nan is depleting).",
                    "Variable (Situ Nan's soul-form size)",
                    "Situ Nan's soul-form drifts inside the bead's interior.",
                    "Green light pulses from Situ Nan into Wang Lin during transmission.",
                    "Green soul-particles flow from Situ Nan to Wang Lin.",
                    "Soft green glow during transmission.",
                    "A whispering voice (Situ Nan's transmission is verbal).",
                    "Faint humming from the bead (Situ Nan's presence).",
                    "A soft chime when transmission completes."
            ),
            List.of(
                    new StateDescription("Dormant", "Situ Nan's soul-form drifts inside the bead.",
                            "Initial state", "Wang Lin initiates a transmission",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §A"),
                    new StateDescription("Transmitting", "Green light pulses from Situ Nan into Wang Lin.",
                            "Wang Lin requests a transmission", "Transmission completes",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §A"),
                    new StateDescription("Sacrificed", "Situ Nan sacrifices his remaining power to save Wang Lin.",
                            "Wang Lin in mortal danger", "Situ Nan reincarnates as Si Nan on IAC",
                            "RI: 'Sacrificed his remaining power to save Wang Lin'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Transmission is via soul-to-soul communication — requires divine sense on Wang Lin's side.",
                            "RI Ch. 86+"),
                    new SystemInteraction(SystemInteraction.SystemType.VERMILION_BIRD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Situ Nan is the 2nd-Gen Vermilion Bird — transmitted the Vermilion Bird Burning Heaven Art.",
                            "RI: '2nd-Gen Vermilion Bird'"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.STORES,
                            "Situ Nan IS a remnant soul stored within the bead.",
                            "RI Ch. 8+")
            ),
            "Minecraft impl: implement as a 'Mentor dialogue' UI that opens when the player right-clicks the Heaven-Defying Bead with Situ Nan still as spirit. UI lists available transmissions (Underworld Ascension Method, Vermilion Bird Burning Heaven Art, etc.). Each transmission grants a 'Technique Learned' status effect. Situ Nan's depletion is tracked via NBT 'Ergen.SituNanPower' (decrements per transmission). When 'Ergen.SituNanPower' reaches 0, Situ Nan 'sacrifices' and reincarnates — the bead's spirit-state transitions from SITU_NAN to NONE.",
            "NPC usage (canon): Wang Lin (transmittee, Ch. 8+). Situ Nan (transmitter). Situ Nan was originally the Green Soul of the Seven-Colored Immortal Venerable; betrayed by 3rd-Gen Vermilion Bird; fled into the Heaven-Defying Pearl. After depletion he reincarnated as 'Si Nan' (Wu Xuan Country Grand Marshal on Immortal Astral Continent).",
            "Player usage (mod): Right-click the Heaven-Defying Bead (after Situ Nan binding) → opens 'Mentor' UI listing transmissions. Each transmission costs 'Ergen.SituNanPower' NBT (decrements). The Underworld Ascension Method is gated behind the Extreme-Yin Site environmental condition. When SituNanPower hits 0, Situ Nan sacrifices (canonical) — UI closes permanently; the bead's spirit-state transitions to NONE.",
            "AI usage (Wang Lin NPC): The NPC consults the Situ Nan UI during early-game cultivation (Ch. 8 - Ch. 1000). The NPC prioritizes the Underworld Ascension Method first (canonical), then the Vermilion Bird Burning Heaven Art. The NPC preserves the last 10% of SituNanPower as a sacrificial reserve (canonical: Situ Nan sacrifices to save Wang Lin at the end)."
    );
}
