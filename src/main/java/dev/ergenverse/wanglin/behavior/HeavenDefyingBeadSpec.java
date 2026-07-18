package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * HeavenDefyingBeadSpec — the behavioral specification for the Heaven-Defying Bead.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §1, HeavenDefyingBead.java):</b>
 * Bead with the Five Elements pattern. Originally 9 parts; the bead is its core.
 * Interior door leads to a chamber where time runs 10× outside. Recognizes
 * master after Lu Mo (clone) blasts it open via Dream Dao. Once Five Elements
 * perfected, the master truly owns it. Stores Li Muwan's Nascent Soul after
 * her body perishes. Reputed to contain Third-Step divine abilities inside.
 * Sentient / destiny-bound / fused with primordial spirit / time dilation
 * interior / cross-novel artifact.
 */
public final class HeavenDefyingBeadSpec {

    private HeavenDefyingBeadSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "heaven_defying_bead",
            "Heaven-Defying Bead", "逆天珠",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 8"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §1; HeavenDefyingBead.java"),
            "Found by Wang Lin as a youth inside the Heng Yue Sect stone bead (found in a dead bird under a cliff). Revealed later to have been sent back through time by his future clone Lu Mo via Dream Dao (originally bestowed by Seven-Colored Immortal Venerable to Realm-Sealing Supreme).",
            "Bead with the Five Elements pattern. Originally 9 parts; the bead is its core. Interior door leads to a chamber where TIME RUNS 10× OUTSIDE. Used to recognize-master Wang Lin after Lu Mo (his clone) blasts it open via Dream Dao. Once Five Elements perfected, the master truly owns it. Stores Li Muwan's Nascent Soul after her body perishes. Reputed to contain Third-Step divine abilities inside. Sentient / destiny-bound / fused with primordial spirit / cross-novel artifact (also wielded by Su Ming / Xuan Zang).",
            List.of(
                    "Wang Lin (the bead's destined master — recognized via Lu Mo's Dream Dao bootstrap)",
                    "Five Elements pattern alignment (6 parts: core + 5 elements)",
                    "Soul Formation+ to enter the interior chamber",
                    "Third-Step+ to access the contained divine abilities"
            ),
            ResourceCost.UNKNOWN,
            new ActivationModel(
                    ActivationModel.Trigger.PASSIVE_ALWAYS_ON,
                    "The bead is sentient and passively active. The user enters the interior chamber via will-thought. The Five Elements pattern aligns as the user attunes each element. The bead's recognition sequence was triggered by Lu Mo's Dream Dao blast.",
                    "Qi Condensation+ (to bond); Soul Formation+ (to enter interior)",
                    List.of("The bead itself (core)"),
                    List.of("Lu Mo's Dream Dao (for recognition bootstrap)",
                            "Five Elements comprehension (for ownership)")
            ),
            new RangeModel(
                    0, -1, 0, RangeModel.Targeting.SELF_ONLY,
                    "The bead affects only its master. The interior chamber is a pocket-dimension accessible only to the master."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.ESSENCE_COMPLETION,
                    "sentience_level(parts_aligned) + interior_capacity = (parts_aligned × user_cultivation)",
                    "Each Five Elements comprehension (Metal, Wood, Water, Fire, Earth) aligns a pattern fragment. All 5 aligning = true ownership. The 14th Essence (Reincarnation) triggers the final fusion with primordial spirit.",
                    List.of("Five Elements Essences (Metal, Wood, Water, Fire, Earth)",
                            "Lu Mo (Slaughter True Body)",
                            "Reincarnation Essence",
                            "Primordial Spirit (end-state fusion)"),
                    "Stone (unrecognized) → Recognized (Lu Mo's Dream Dao) → Partial ownership (1-5 elements aligned) → True ownership (5 elements aligned) → Fused with primordial spirit (at Heaven Trampling)",
                    "The bead grows with the master. More parts aligned = more sentience = more interior capacity = more divine abilities accessible."
            ),
            List.of(
                    "Killing the master (only at low cultivation before fusion)",
                    "The Heaven-Defying Bead's counterpart (per alt-wiki: 'Beads from the Seven-Colored Realm' are decoys without the pattern)",
                    "Bound to Wang Lin's destiny — counters via destiny-disruption are unknown"
            ),
            List.of(
                    "Cannot be transferred — bound to Wang Lin's destiny",
                    "Interior chamber is fragile if the master's cultivation is insufficient",
                    "Heaven-Defying nature makes the bead a target (the 'root cause of the great war in the Ancient Immortal Domain')"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.TIME_DILATION_FIELD,
                            "The interior chamber runs 10× faster than the outside (1 hour inside = 10 hours outside).",
                            10.0, "RI Ch. 8+; HeavenDefyingBead.java: TIME_DILATION = 10.0"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.REINCARNATION_POOL,
                            "At the Reincarnation Pool, the bead's resonance with Reincarnation Essence peaks.",
                            1.5, "RI Ch. 1943+")
            ),
            new VisualDescription(
                    "A small, perfectly spherical bead. Surface etched with the Five Elements pattern (Metal/Wood/Water/Fire/Earth) + a central core pattern. Initially appears as a mundane stone; reveals the pattern when activated. The interior chamber is a small pocket-space with a single door.",
                    List.of("Stone bead (material)", "Five Elements pattern (etched)",
                            "Pocket-dimension interior"),
                    "#2c3e50 (deep stone black)", "#f1c40f (Five Elements gold)",
                    "Smooth, polished stone. Slightly warm to the touch when sentient.",
                    "1×1×1 (the bead itself)",
                    "Stone-like, dormant. Faintly pulses with the user's heartbeat once bonded.",
                    "Pattern glows; the door to the interior chamber appears in front of the user.",
                    "Subtle gold particles spiraling around the bead when pattern aligns.",
                    "Soft gold glow when pattern is active; the interior chamber is dimly lit.",
                    "A faint chime like distant bells.",
                    "Silence (the bead is calm).",
                    "A crystalline ring (the bead cannot be damaged by ordinary means)."
            ),
            List.of(
                    new StateDescription("Stone (unrecognized)",
                            "Appears as a mundane stone bead. No sentience.",
                            "Initial state (Wang Lin's childhood)",
                            "Lu Mo's Dream Dao bootstrap recognizes the master",
                            "RI Ch. 8"),
                    new StateDescription("Recognized (core aligned)",
                            "Core pattern glows; sentience level 1; interior chamber barely accessible.",
                            "Lu Mo's Dream Dao bootstrap",
                            "User aligns the Five Elements pattern",
                            "RI Ch. 8+ (post-Lu-Mo bootstrap)"),
                    new StateDescription("Partial ownership (1-5 elements aligned)",
                            "Each element aligned increments sentience level (2-6).",
                            "User comprehends each of the Five Elements",
                            "All 5 elements aligned = true ownership",
                            "RI throughout (Five Elements comprehension arc)"),
                    new StateDescription("True ownership (all 5 aligned)",
                            "Sentience level 6; interior chamber fully accessible; Li Muwan's Nascent Soul can be stored.",
                            "User comprehends all 5 Five Elements",
                            "Fusion with primordial spirit at Heaven Trampling",
                            "RI: 'Once Five Elements perfected, the master truly owns it'"),
                    new StateDescription("Fused with primordial spirit (Spirit Manifestation)",
                            "The bead IS the master. The bead-spirit and the cultivator's primordial spirit are one. End-state. Sentience level 9.",
                            "Heaven Trampling achieved (Reincarnation Essence complete)",
                            "Permanent (transcendence)",
                            "RI Ch. 2087"),
                    new StateDescription("Li Muwan's Nascent Soul stored",
                            "The bead carries Wang Lin's wife's soul — his entire late-game motivation.",
                            "Li Muwan's body perishes (E28)",
                            "Wang Lin resurrects her at 4th Step",
                            "RI E28"),
                    new StateDescription("Damaged (pattern disrupted)",
                            "The Five Elements pattern flickers; interior chamber inaccessible; sentience level drops. Only achievable at low cultivation before fusion — canon implies killing the master at low cultivation is the only path.",
                            "Master is killed at low cultivation (pre-fusion) OR an anti-destiny treasure disrupts the pattern",
                            "Master's cultivation recovers OR a new alignment cycle completes",
                            "RI: 'Cannot be transferred — bound to Wang Lin's destiny' (counters via destiny-disruption are unknown)")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.SPACE_POCKET,
                            SystemInteraction.InteractionType.STORES,
                            "Interior chamber stores items, souls, and (canon) Li Muwan's Nascent Soul.",
                            "RI Ch. 8+; HeavenDefyingBead.java: MAX_STORAGE_SLOTS = 27"),
                    new SystemInteraction(SystemInteraction.SystemType.TIME,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "Time dilation: 1 hour inside = 10 hours outside. Cultivation inside is 10× faster in outside-time terms.",
                            "RI; HeavenDefyingBead.java: TIME_DILATION = 10.0"),
                    new SystemInteraction(SystemInteraction.SystemType.REINCARNATION,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "The bead is the vessel for the Samsara Incarnation technique (1 billion incarnations).",
                            "RI throughout"),
                    new SystemInteraction(SystemInteraction.SystemType.AVATARS_CLONES,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Lu Mo (Wang Lin's clone) bootstrap-recognition requires Dream Dao + the Realm-Defining Compass.",
                            "RI Ch. 1295+ (Dream Dao)"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.STORES,
                            "Stores Li Muwan's Nascent Soul for 700 years.",
                            "RI E28, end-game resurrection"),
                    new SystemInteraction(SystemInteraction.SystemType.KARMA,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "The bead is destiny-bound to Wang Lin — karmic threads converge on it. The 'root cause of the great war in the Ancient Immortal Domain' implies karmic weight beyond measurement. Karma-severing techniques (Dream Dao) cannot sever the bead's destiny-tie.",
                            "RI: 'destiny-bound / fused with primordial spirit'"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "The interior chamber is accessed via will-thought (divine sense). Surface access (storage menu) is via divine sense; deep access (physical entry into the pocket-dimension) requires Soul Formation+ divine-sense strength.",
                            "RI Ch. 8+; BeadAccessMode.java: DIVINE_SENSE vs PHYSICAL_ENTRY")
            ),
            "Minecraft impl: implement as a key-item (not craftable, not droppable). Right-click to enter the interior chamber — a small pocket-dimension (separate dimension ID, registered via Forge DeferredRegister for dimensions). Time runs 10× faster inside (use a custom WorldTickEvent modifier). The interior has 27 slot inventory (per HeavenDefyingBead.java: MAX_STORAGE_SLOTS = 27). Use NBT tags 'Ergen.BeadPartsAligned' (1-9) and 'Ergen.BeadContainsLiMuwanSoul' (boolean). The bead is bound to the player on first pickup (cannot be transferred).",
            "NPC usage (canon): Wang Lin only — the bead is destiny-bound to him (Ch. 8+). Lu Mo (Wang Lin's clone) bootstrap-recognized the bead via Dream Dao. Seven-Colored Immortal Venerable originally bestowed it to Realm-Sealing Supreme. Su Ming (Renegade Immortal-adjacent) and Xuan Zang wield bead counterparts in cross-novel canon, but those are separate artifacts. The bead-spirit (post-fusion) IS Wang Lin — not a separate wielder.",
            "Player usage (mod): The bead auto-binds on first pickup (cannot be dropped, traded, or duplicated). Right-click opens the surface storage menu (27 slots, divine-sense access). Sneak-right-click attempts physical entry into the interior dimension (requires Soul Formation+ capability; fails with backlash otherwise). Inside the dimension, time runs 10× faster — use for cultivation afk-grinds. The 'Ergen.BeadPartsAligned' NBT increments as the player comprehends each Five Elements essence (separate advancements). At 5 alignments, Li Muwan's Nascent Soul can be stored (a quest hook for the late game).",
            "AI usage (Wang Lin NPC): The NPC treats the bead as a permanent inventory fixture (never drops, never gifts). When the NPC's HP would drop to 0, the bead's destiny-binding triggers a 'last-breath' escape — teleports the NPC to the interior dimension for 10 sec to recover (cooldown: 1 in-game day). The NPC retreats into the bead to cultivate during 'downtime' ticks (simulating Wang Lin's time-dilation cultivation). At the end-game, the NPC fuses with the bead (visual transition; the NPC becomes the bead-spirit)."
    );
}
