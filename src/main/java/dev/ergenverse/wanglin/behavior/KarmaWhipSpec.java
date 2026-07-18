package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * KarmaWhipSpec — bonus spec for Wang Lin's Karma Whip (fused Soul Lasher + Karma Domain).
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §2, ri_wiki):</b> Created when Wang Lin fused the Soul Lasher
 * (originally Red Butterfly's whip — attacks origin soul at warp speed) with his Karma Domain
 * (Ch. 731). Weaponizes karmic cause-effect. In the Outer Realm, Wang Lin once used this to
 * cleave open 7 million worlds with a single whip-strike.
 *
 * <p><b>Demonstrated uses (chapter-cited):</b>
 * <ul>
 *   <li>Pre-Ch. 731 — Soul Lasher (originally Red Butterfly's whip) won in battle; attacks origin soul at warp speed</li>
 *   <li>Ch. 731 (donghua Ep 147) — fused with Wang Lin's Karma Domain; burned by Ghostly Sky Fire; mysterious transformation; nourished by Wang Lin's Concept</li>
 *   <li>End-game (Outer Realm) — cleaves open 7 million worlds with a single whip-strike</li>
 *   <li>Restrains soul-type entities (karmic threads bind the target's origin soul)</li>
 *   <li>Karma Domain stabilization (Ch. 850) amplifies the whip's strike power</li>
 * </ul>
 */
public final class KarmaWhipSpec {

    private KarmaWhipSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "karma_whip",
            "Karma Whip (Soul Lasher + Karma Domain fusion)", "因果鞭",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 731 (donghua Ep 147)"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §2; CANON_RI_COMPLETE_TECHNIQUES.md §J"),
            "Fused from the Soul Lasher (originally Red Butterfly's whip, won in battle — attacks origin soul at warp speed) + Wang Lin's Karma Domain (Ch. 731, Demon Spirit Land). Burned by Ghostly Sky Fire, mysterious transformation, nourished by Wang Lin's Concept.",
            "Created when Wang Lin fused the Soul Lasher with his Karma Domain. Weaponizes karmic cause-effect. In the Outer Realm, Wang Lin once used this to cleave open 7 million worlds with a single whip-strike. Restrains soul-type entities.",
            List.of(
                    "Soul Lasher (originally Red Butterfly's, won in battle)",
                    "Karma Domain (Wang Lin's 3rd essence)",
                    "Ghostly Sky Fire (transformation catalyst)",
                    "Wang Lin's Concept (nourishment)"
            ),
            new ResourceCost(
                    0, 0, 40, 0, 0, 200, 200, 0,
                    "Per-strike: high divine-sense cost (40) + karmic-debt cost (200 — weaponizing karmic cause-effect accumulates karmic weight). Cooldown: 10 sec (200 ticks). Cross-world strike (Outer Realm only): 5× the divine-sense cost."),
            new ActivationModel(
                    ActivationModel.Trigger.WILL_THOUGHT,
                    "Whip is wielded via will-thought. The whip strikes at the target's origin soul at warp speed.",
                    "Soul Transformation+ (Karma Domain stabilized)",
                    List.of("Soul Lasher (pre-fusion)", "Karma Domain (Wang Lin's 3rd essence)"),
                    List.of("Ghostly Sky Fire (fusion catalyst)", "Wang Lin's Concept (nourishment)")
            ),
            new RangeModel(
                    1, -1, 0, RangeModel.Targeting.CROSS_WORLD,
                    "Whip strikes at the origin soul at warp speed. In the Outer Realm, can cleave open 7 million worlds with a single strike (Ch. 731+ — end-game)."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.KARMA_ACCUMULATED,
                    "whip_power = (karma_essence_depth) × (user_cultivation) × (karma_threads_visible)",
                    "Each karmic thread Wang Lin perceives amplifies the whip's strike power. Karma Domain stabilization (Ch. 850) is a major boost.",
                    List.of("Karma Domain", "Karma Essence (Wang Lin's 3rd essence)",
                            "Soul Lasher (pre-fusion base)"),
                    "Soul Lasher (pre-fusion) → Karma Whip (fused with Karma Domain, Ch. 731) → peak form (cleaves 7 million worlds)",
                    "More karmic threads perceived = stronger whip strikes."
            ),
            List.of(
                    "Heaven-Avoiding Coffin (preserves souls from whip strikes)",
                    "Stronger soul-cultivators (Third-Step+) who can resist karmic cause-effect",
                    "Karma-severing techniques (Dream Dao can sever karmic ties)"
            ),
            List.of(
                "High divine-sense cost per strike",
                "Cooldown (10 sec) — cannot be spammed",
                "Requires Karma Domain stabilization (Soul Transformation+)"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Whip potency scales with the world's law strength.",
                            0.8, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.OUTER_REALM,
                            "In the Outer Realm (outside the Realm-Sealing Array), the whip can achieve cross-world strikes (7 million worlds).",
                            2.0, "RI Ch. 731+: 'In the Outer Realm Wang Lin once used this to cleave open 7 million worlds'")
            ),
            new VisualDescription(
                    "A long whip that shimmers with karmic threads. The whip's body is green-silver (Soul Lasher origin) with karmic-gold threads woven through it (Karma Domain fusion). When activated, karmic threads become visible — connecting the target to past karma.",
                    List.of("Soul Lasher material (green-silver)", "Karmic threads (gold)",
                            "Ghostly Sky Fire (fusion catalyst residue)"),
                    "#27ae60 (Soul Lasher green)", "#f1c40f (karmic gold)",
                    "Ethereal. The whip feels 'heavy with consequence'.",
                    "1×3×1 (whip length)",
                    "Whip coiled at the user's side; karmic threads faintly visible.",
                    "Whip unfurls and strikes at the target's origin soul; karmic threads snap taut.",
                    "Gold karmic-thread particles along the whip's path.",
                    "Karmic gold glow.",
                    "A sharp crack like breaking fate.",
                    "Faint whispering (the karmic threads 'speaking').",
                    "A resonant snap on impact."
            ),
            List.of(
                    new StateDescription("Idle (coiled)", "Whip coiled at the user's side; karmic threads faintly visible.",
                            "Whip not activated", "User activates the whip",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new StateDescription("Striking (warp-speed)", "Whip unfurls and strikes at the target's origin soul at warp speed.",
                            "User wills an attack", "Whip strikes the target or is parried",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new StateDescription("Cross-world strike (Outer Realm)", "In the Outer Realm, the whip cleaves open 7 million worlds with a single strike.",
                            "User is in the Outer Realm + user wills a cross-world strike", "Strike completes",
                            "RI: 'In the Outer Realm Wang Lin once used this to cleave open 7 million worlds'"),
                    new StateDescription("Karma-bound (target)", "The target is bound by karmic threads; cannot escape.",
                            "Whip strikes the target", "Target dies or karmic tie is severed",
                            "CANON_RI_COMPLETE_ITEMS.md §2: 'restrains soul-type entities'"),
                    new StateDescription("Damaged (karmic threads frayed)", "Whip's karmic-gold threads dim; Soul-Lasher green-silver base shows through; strike power reduced.",
                            "Whip is parried by a Third-Step treasure OR the user accumulates too much karmic debt (backlash)", "User re-nourishes the whip with Concept (meditation)",
                            "CANON_RI_COMPLETE_ITEMS.md §2 (inferred from 'nourished by Wang Lin's Concept')"),
                    new StateDescription("Spirit Manifestation (Karma Whip spirit)", "After prolonged nourishment by Wang Lin's Concept, the whip develops a quasi-spirit — a karmic echo of the souls it has bound. The spirit can auto-strike karmically-bound targets the user is not consciously aware of (defensive autopilot).",
                            "User's Karma Essence deepens to the Third-Step Karma Domain stabilization (Ch. 850+)",
                            "User dismisses the spirit OR the spirit's karmic-debt reserve depletes",
                            "RI Ch. 850+ (Karma Domain stabilization), inferred from 'nourished by Wang Lin's Concept'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.KARMA,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Whip is fused with Wang Lin's Karma Domain (his 3rd essence).",
                            "RI Ch. 731"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.COUNTERS,
                            "Whip restrains soul-type entities; strikes at the origin soul.",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new SystemInteraction(SystemInteraction.SystemType.SPACE_POCKET,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "In the Outer Realm, the whip can achieve cross-world (7 million worlds) strikes.",
                            "RI: 'cleave open 7 million worlds with a single whip-strike'"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Activation requires divine sense (will-thought). The more karmic threads the user can perceive, the stronger the strike.",
                            "RI Ch. 731+"),
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.FUSES_WITH,
                            "The Karma Whip is the fusion-product of the Soul Lasher treasure + the Karma Domain essence. The Soul Lasher is consumed in the fusion; the Karma Domain persists as the whip's power-source.",
                            "RI Ch. 731"),
                    new SystemInteraction(SystemInteraction.SystemType.AVATARS_CLONES,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "When Wang Lin's Lu Mo clone (Slaughter True Body) is fused with the main body, the whip's karmic perception extends — the clone's Dream Dao can pre-mark targets with karmic threads for the whip to strike.",
                            "RI: Lu Mo + Dream Dao era (inferred)")
            ),
            "Minecraft impl: implement as a weapon item with a right-click 'Whip Strike' action. The strike targets the looked-at entity's origin-soul (bypasses armor; deals divine-sense damage). Use NBT 'Ergen.KarmaThreads' (increments as the player kills entities with karmic weight). In the Outer Realm dimension, the whip's right-click can have a 'Cross-World Strike' mode (shift+right-click) that hits entities in a 7-million-block radius (limited to loaded chunks). Cooldown: 10 sec (200 ticks). Requires the player to have the 'Karma Domain' capability.",
            "NPC usage (canon): Wang Lin only — the whip is fused with HIS Karma Domain (his 3rd essence). The Soul Lasher (pre-fusion) was originally Red Butterfly's — won in battle. No other canon NPC wields the fused Karma Whip.",
            "Player usage (mod): Craft via fusing a Soul Lasher item + a Karma Domain capability (player-side). Right-click → 'Whip Strike' (looks at entity, deals divine-sense damage bypassing armor, 10-sec cooldown). Sneak-right-click in the Outer Realm dimension → 'Cross-World Strike' (hits entities in loaded chunks; massive karmic-debt cost). The whip's damage scales with 'Ergen.KarmaThreads' NBT (increments on kill). If karmic debt exceeds a threshold, the whip backlashes (player takes divine-sense damage).",
            "AI usage (Wang Lin NPC): The NPC uses the whip as a finisher against soul-type enemies (skeletons, wither, end-game undead mobs). The NPC reserves cross-world strikes for boss-tier enemies (with high HP). The auto-pilot spirit (post-Karma Domain stabilization) auto-strikes any karmically-marked target within 32 blocks even when the NPC is crowd-controlled. The NPC avoids the whip against enemies with no karmic weight (animals, non-sapient mobs)."
    );
}
