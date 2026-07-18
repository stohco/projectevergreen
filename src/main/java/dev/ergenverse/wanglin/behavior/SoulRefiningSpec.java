package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * SoulRefiningSpec — the Soul Refining Sect inheritance (Soul Flag Production Method).
 *
 * <p><b>Canon (CANON_RI_COMPLETE_TECHNIQUES.md §E, CANON_RI_COMPLETE_ITEMS.md §4):</b> Main cultivation method of the
 * Soul Refining Sect — splits into three parts: Soul Refining, Soul Extracting, and
 * Soul Sealing. Lets the user refine souls into a Soul Flag. Powers the One Billion
 * Soul Banner. Inherited from Dun Tian (Ch. 384) — Soul Refining Sect ancestor who
 * erased his own consciousness to become a soul within the Soul Banner.
 *
 * <p><b>Demonstrated uses (chapter-cited):</b>
 * <ul>
 *   <li>Ch. 384 — Dun Tian (Soul Refining Sect ancestor) bequeaths the method + the Billion Soul Banner to Wang Lin; Dun Tian erases his own consciousness to become a soul within the Banner</li>
 *   <li>Ch. 390 — Wang Lin uses the Soul Refining Sect inheritance to break through to late Soul Formation with the Billion Soul Flag's help</li>
 *   <li>Three-part ritual: (1) Soul Refining (炼魂) — refine a captured soul into pure soul-stuff; (2) Soul Extracting (抽魂) — extract the soul from a body; (3) Soul Sealing (封魂) — seal the refined soul into the Soul Flag</li>
 *   <li>Powers the Billion Soul Banner / Ten-Billion Soul Banner — offensive power scales with soul-count</li>
 *   <li>End-game fusion: Soul Flag fuses with Celestial Sealing Stamp + Underworld River → 18 Layers of Hell Reincarnation Realm (Ch. 915)</li>
 *   <li>Corrupted souls (resentful spirits) can taint the Soul Flag — Wang Lin must purify them or risk backlash</li>
 * </ul>
 */
public final class SoulRefiningSpec {

    private SoulRefiningSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "soul_refining",
            "Soul Refining (Soul Refining Sect inheritance)", "炼魂",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 384"), 5,
                    "CANON_RI_COMPLETE_TECHNIQUES.md §E; CANON_RI_COMPLETE_ITEMS.md §4"),
            "Inherited from Dun Tian (Ch. 384) — Soul Refining Sect ancestor. Dun Tian erased his own consciousness to become a soul within the Soul Banner.",
            "Main cultivation method of the Soul Refining Sect — splits into three parts: Soul Refining (炼魂), Soul Extracting (抽魂), and Soul Sealing (封魂). Lets the user refine souls into a Soul Flag. Powers the One Billion Soul Banner.",
            List.of(
                    "Foundation Establishment+",
                    "Soul Refining Sect inheritance (Dun Tian gift)",
                    "A Soul Flag / Soul Banner to receive the refined souls",
                    "Captured souls (raw material)"
            ),
            new ResourceCost(
                    0, 30, 30, 0, 0, 0, 60, 0,
                    "Per-soul refinement: soul-force cost (30 — the captured soul's resistance) + divine-sense cost (30 — to weave the refining pattern). Cooldown: 3 sec (60 ticks) per soul refined. Bulk-refining many souls (for the Billion Soul Banner) takes sustained divine-sense expenditure."),
            new ActivationModel(
                    ActivationModel.Trigger.RITUAL_CEREMONY,
                    "Three-stage ritual: (1) Soul Refining — refine a captured soul into pure soul-stuff; (2) Soul Extracting — extract the soul from a body; (3) Soul Sealing — seal the refined soul into the Soul Flag.",
                    "Foundation Establishment+",
                    List.of("Soul Flag / Billion Soul Banner", "Captured souls"),
                    List.of("Soul Refining Sect inheritance")
            ),
            new RangeModel(
                    0, 8, 0, RangeModel.Targeting.SINGLE_TARGET,
                    "Soul extraction works on a single captured entity at melee range. Once refined and sealed into the Soul Flag, the soul-stuff is contained within the banner."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.SOULS_ABSORBED,
                    "power = (souls_refined) × user_soul_cultivation × banner_quality",
                    "Soul Transformation breakthrough unlocks the Billion Soul Banner's full capacity.",
                    List.of("Billion Soul Banner / Ten-Billion Soul Banner", "Celestial Sealing Stamp",
                            "Underworld River (Life-Death Domain)", "Magic Arsenal"),
                    "Soul Flag (production) → Billion Soul Flag → fused with Celestial Sealing Stamp + Underworld River → 18 Layers of Hell Reincarnation Realm",
                    "The refined souls power Wang Lin's soul-tier attacks. The more souls, the more powerful the Soul Flag's offensive abilities."
            ),
            List.of(
                    "Soul-searching / soul-attack techniques that scatter refined souls",
                    "Stronger soul-cultivators (Soul Transformation+) who can resist extraction",
                    "Heaven-Avoiding Coffin (preserves souls from being refined)",
                    "Daoist Water (Third-Step) — destroyed many of Wang Lin's soul items"
            ),
            List.of(
                    "Slow process — cannot be performed mid-combat",
                    "Requires a captured soul (cannot extract from a willing donor without harm)",
                    "Corrupted souls (resentful spirits) can taint the Soul Flag"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Soul-refining potency scales with the world's law strength.",
                            0.7, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.SEALED_REALM,
                            "Inside the Sealed Realm, soul-refining is slightly suppressed.",
                            0.8, "RI throughout")
            ),
            new VisualDescription(
                    "A ritualistic process. The Soul Flag (banner) is unfurled; the captured soul appears as a glowing orb drawn from the target's body. The soul is then refined (compresses, purifies, glows brighter) before being sealed into the banner.",
                    List.of("Soul Flag (banner)", "Captured soul (orb)", "Divine sense (invisible thread)"),
                    "#9b59b6 (soul purple)", "#ecf0f1 (soul-stuff white)",
                    "Ethereal. The soul-orb is translucent and slightly pulsates.",
                    "1×2×1 (banner) + 1×1×1 (soul orb in front)",
                    "Banner furled; no souls being processed.",
                    "Banner unfurls; soul-orb floats in front; the orb is drawn into the banner.",
                    "Soul-stuff particles — purple-white wisps flowing from the target into the banner.",
                    "Subtle purple glow from the banner when souls are present.",
                    "A high-pitched chime as the soul is extracted.",
                    "Faint whispering from the banner (the trapped souls).",
                    "A wet tearing sound as the soul separates from the body."
            ),
            List.of(
                    new StateDescription("Idle (banner furled)", "Banner rolled; no souls being processed.",
                            "Banner not activated", "User activates the method",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E"),
                    new StateDescription("Extracting", "Soul-orb drawn from the target's body.",
                            "User activates the method on a captured target", "Extraction complete or interrupted",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E"),
                    new StateDescription("Refining", "Soul-orb compresses, purifies, glows brighter.",
                            "Extraction complete", "Refinement complete",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E"),
                    new StateDescription("Sealed", "Soul is sealed into the banner; banner's soul-count increments.",
                            "Refinement complete", "Banner is destroyed or soul is released",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E"),
                    new StateDescription("Damaged (banner torn / soul-loss)", "Banner fabric tears; trapped souls leak out as wandering spirits; soul-count decrements.",
                            "Banner takes battle damage OR a stronger soul-cultivator forces a soul-release", "User repairs the banner + re-refines the lost souls",
                            "CANON_RI_COMPLETE_TECHNIQUES.md §E (inferred; Daoist Water destroyed many of Wang Lin's soul items Ch. 1276-1277)"),
                    new StateDescription("Spirit Manifestation (Dun Tian awakens)", "Dun Tian's erased consciousness — bound into the Billion Soul Banner as a soul — can manifest briefly as the banner's spirit. He cannot speak (his consciousness was erased) but his residual intent guides the banner's autonomous defense.",
                            "Banner soul-count crosses a threshold AND the banner is in mortal danger", "Danger passes OR Dun Tian's residual intent depletes",
                            "RI Ch. 384: 'Dun Tian erased his own consciousness to become a soul within the Soul Banner'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.CONSUMES,
                            "Consumes captured souls as raw material.",
                            "RI Ch. 384"),
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Requires a Soul Flag / Billion Soul Banner to receive the refined souls.",
                            "RI Ch. 384"),
                    new SystemInteraction(SystemInteraction.SystemType.REINCARNATION,
                            SystemInteraction.InteractionType.FUSES_WITH,
                            "Fuses with Celestial Sealing Stamp + Underworld River → 18 Layers of Hell Reincarnation Realm.",
                            "RI Ch. 915"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Powered by divine sense.",
                            "RI Ch. 384"),
                    new SystemInteraction(SystemInteraction.SystemType.LIFE_DEATH,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "The Underworld River (Life-Death Domain) resonates with the refined souls — the River can purify corrupted souls before they are sealed into the Banner.",
                            "RI Ch. 915 (fusion with Underworld River)"),
                    new SystemInteraction(SystemInteraction.SystemType.FORMATIONS,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The 18 Layers of Hell Reincarnation Realm (end-game fusion) IS a formation powered by the Soul Flag + Celestial Sealing Stamp + Underworld River. Souls trapped in the Banner become the formation's energy source.",
                            "RI Ch. 915")
            ),
            "Minecraft impl: implement as a 3-stage Item right-click action on a captured entity (mob in a cage / captured NPC). Stage 1: extract soul orb (entity takes damage; if it dies, soul orb drops as item). Stage 2: refine the soul orb (right-click the orb with the Soul Flag). Stage 3: seal into the banner (banner NBT soul-count increments). The Billion Soul Banner's offensive power scales with soul-count.",
            "NPC usage (canon): Wang Lin (Ch. 384+). Dun Tian before him (Soul Refining Sect ancestor). The Soul Refining Sect itself historically. Daoist Water (Third-Step enemy) destroyed many of Wang Lin's soul items (Ch. 1276-1277) — proving the method has counters at Third-Step tier.",
            "Player usage (mod): Right-click a captured (caged) entity with the Soul Flag item → Stage 1 (extract) → soul orb drops. Right-click the soul orb with the Soul Flag → Stage 2 (refine) → orb compresses. Right-click the Banner with the refined orb → Stage 3 (seal) → banner soul-count NBT increments. The Banner's right-click attack scales damage with soul-count. Corrupted souls (mob types: zombie, skeleton variants) taint the banner — must be purified via the Underworld River block before sealing.",
            "AI usage (Wang Lin NPC): When fighting a humanoid enemy the NPC expects to kill, the NPC pre-casts the Soul Refining Method on the target's location. On target death, the soul-orb auto-extracts. The NPC seals the orb into the Banner between combats. If the Banner's soul-count exceeds 1000, the NPC switches to the Banner as primary weapon (soul-count scaling). The NPC avoids using the method on Third-Step-tier enemies (canon: Daoist Water destroyed soul items)."
    );
}
