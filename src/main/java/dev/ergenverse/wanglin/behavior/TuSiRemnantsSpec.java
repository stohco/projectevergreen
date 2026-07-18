package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * TuSiRemnantsSpec — Tu Si's Ancient God remnants.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §2, §3, §5; CANON_RI_COMPLETE_TECHNIQUES.md §A):</b>
 * Tu Si (Ancient God 8-Star) — his body became the Land of the Ancient God (3-level
 * Chaotic Broken Stars realm). Granted Wang Lin the 'Great Enlightened One' title and
 * 'knowledge' inheritance. Tuo Sen inherited the 'power' inheritance (born from Tu Si's
 * failed Ink Flow Split Soul Technique).
 *
 * <p>Tu Si's remnants include: Ancient God Tactic, Heaven Technique (movement-inside-body),
 * Restriction Flags Refining Method, Ancient God Leather Armour, Azure Ancient God Shield,
 * Ancient God Trident, Ancient God Furnace, Ancient God Bracer, God-Slaying Spear (illusory),
 * Protection Bone Tablets, etc.
 */
public final class TuSiRemnantsSpec {

    private TuSiRemnantsSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "tu_si_remnants",
            "Tu Si's Ancient God Remnants", "涂司古神遗物",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 190+"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §2, §3, §5; CANON_RI_COMPLETE_TECHNIQUES.md §A"),
            "Inherited from Tu Si's memory legacy inside the Ancient God's body (the Land of the Ancient God). Tu Si's body became the 3-level Chaotic Broken Stars realm. Wang Lin received the 'knowledge' inheritance; Tuo Sen received the 'power' inheritance.",
            "Tu Si (Ancient God 8-Star) — granted Wang Lin the 'Great Enlightened One' title and 'knowledge' inheritance. His remnants include: Ancient God Tactic (cultivation art), Heaven Technique (movement-inside-body), Restriction Flags Refining Method, Ancient God Leather Armour (Ch. 758), Azure Ancient God Shield (Ch. 980 — contains 'Dreaming Back to Antiquity' life-saving spell), Ancient God Trident (Ch. 1082 — destroyed Ch. 1277 by Daoist Water), Ancient God Furnace (Ch. 838 — destroyed Ch. 1226), Ancient God Bracer, God-Slaying spear illusory (Ch. 941), Protection Bone Tablets. The remnants also include the Land of the Ancient God itself — Tu Si's body.",
            List.of(
                    "Ancient God Tactic (Tu Si inheritance)",
                    "Core Formation+ (Restriction Mountain Trial)",
                    "Ancient God body reconstruction (1★+)",
                    "Land of the Ancient God access"
            ),
            ResourceCost.divineSense(40, 0,
                    "Using a Tu Si remnant: moderate divine-sense cost. The Ancient God Tactic itself is passive (body-reconstruction)."),
            new ActivationModel(
                    ActivationModel.Trigger.PASSIVE_ALWAYS_ON,
                    "Tu Si's remnants are passive (the Ancient God Tactic continuously reconstructs the body). Individual treasures have their own activation (e.g. Azure Shield deploys 'Dreaming Back to Antiquity' as a life-saving spell).",
                    "Core Formation+ (Restriction Mountain Trial completion)",
                    List.of("Ancient God body (1★+)"),
                    List.of("Tu Si's 'knowledge' inheritance")
            ),
            new RangeModel(
                    0, -1, 0, RangeModel.Targeting.SELF_ONLY,
                    "Tu Si's remnants primarily affect the user. The Land of the Ancient God (Tu Si's body) is a realm the user enters."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.STAR_TIER,
                    "remnant_potency = (ancient_god_star_tier) × (user_cultivation)",
                    "Each Ancient God star tier unlocked (1★ → 27★) amplifies all Tu Si remnants. The 7★ golden state (Ch. 1538) is a major breakthrough catalyst.",
                    List.of("Ancient God body reconstruction (1★-27★)",
                            "Restriction Flag system", "Ancient God Leather Armour",
                            "Azure Ancient God Shield", "Ancient God Trident"),
                    "1★ (Ch. 199) → 3★ (Ch. 391) → 7★ (Ch. 1290-1538) → 13★ Ancient Clan (Ch. 1539) → 24★ (Ch. 1705) → 27★ (Ch. 2003) → Half-Heaven-Trampling (Ch. 2062) → Heaven-Trampling (Ch. 2087)",
                    "More star-tiers = more potent remnants."
            ),
            List.of(
                    "Daoist Water (Third-Step) — destroyed Ancient God Trident (Ch. 1277) and Ancient God Furnace (Ch. 1226)",
                    "Tuo Sen (Tu Si's 'power' inheritor) — rival; contested the inheritance",
                    "Stronger Ancient God artifacts"
            ),
            List.of(
                    "Cannot be transferred — bound to Wang Lin's Ancient God body",
                    "Several remnants destroyed by Daoist Water (later restored via Void Gate Ch. 1626)",
                    "Tu Si's remnants attract Tuo Sen's rivalry"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.ANCIENT_GOD_BODY,
                            "Inside an Ancient God's body (Tu Si's body = Land of the Ancient God), all Tu Si remnants resonate with peak potency.",
                            1.5, "RI Ch. 190+"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Remnant potency scales with the world's law strength.",
                            0.7, "RI throughout")
            ),
            new VisualDescription(
                    "Tu Si's remnants are Ancient God-tier treasures — each made of Ancient God bone, skin, or refined Ancient God essence. The Land of the Ancient God itself is a 3-level Chaotic Broken Stars realm — Tu Si's body transformed into a landscape.",
                    List.of("Ancient God bone", "Ancient God skin", "Ancient God essence",
                            "Tu Si's body (the realm itself)"),
                    "#8e44ad (Ancient God purple)", "#f39c12 (Ancient God gold)",
                    "Dense, heavy, otherworldly. Each remnant feels 'older than the world'.",
                    "Variable (per remnant)",
                    "Subtle pulse with the user's Ancient God star-tier.",
                    "Treasures glow with Ancient God-tier power.",
                    "Purple-gold particles around active remnants.",
                    "Ancient God purple-gold glow.",
                    "A deep resonant hum — like striking ancient bronze.",
                    "Silence (the remnants are patient).",
                    "A heavy thud (Ancient God power impact)."
            ),
            List.of(
                    new StateDescription("Inherited", "Wang Lin has received Tu Si's 'knowledge' inheritance.",
                            "Tu Si's legacy transmission", "Permanent (bound to Wang Lin)",
                            "RI Ch. 190+"),
                    new StateDescription("Active (passive)", "The Ancient God Tactic continuously reconstructs Wang Lin's body.",
                            "Always on", "Permanent",
                            "RI Ch. 199+"),
                    new StateDescription("Destroyed (some remnants)", "Ancient God Trident + Ancient God Furnace destroyed by Daoist Water.",
                            "Daoist Water fight (Ch. 1277, 1226)", "Restored via Void Gate (Ch. 1626)",
                            "RI Ch. 1277, 1226, 1626"),
                    new StateDescription("Restored", "Destroyed remnants restored via Void Gate power.",
                            "Void Gate power (Ch. 1626)", "Permanent",
                            "RI Ch. 1626")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "All Tu Si remnants resonate with the Ancient God body.",
                            "RI throughout"),
                    new SystemInteraction(SystemInteraction.SystemType.RESTRICTIONS,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The Restriction Flags Refining Method (Tu Si inheritance) amplifies Wang Lin's restriction work.",
                            "RI Ch. ~180"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The Ancient God Tactic allows absorbing inheritance fragments as stars.",
                            "RI: 'Allows absorbing spiritual energy, pills, and inheritance fragments as stars'")
            ),
            "Minecraft impl: implement each Tu Si remnant as a separate WangLinItem. The Ancient God Tactic is a passive effect bound to the player's Ancient God star-tier (tracked via a capability). The Azure Ancient God Shield's 'Dreaming Back to Antiquity' is a life-saving proc — when the player would die, the shield triggers and rewinds time by a few seconds (a custom 'Time Rewind' event). The Land of the Ancient God is a separate dimension (Tu Si's body).",
            "NPC usage (canon): Wang Lin (inherited Tu Si's legacy in the Land of the Ancient God; Restriction Mountain Trial — 4th person ever to complete). Tu Si (the original Ancient God whose body became the Land of the Ancient God) is the source. Daoist Water later destroyed some Tu Si remnant items (Ch. 1276-1277).",
            "Player usage (mod): The Ancient God Tactic is acquired via a one-time trial in the Land of the Ancient God dimension (separate dimension, portal in the Demon Spirit Land). The Azure Ancient God Shield's 'Dreaming Back to Antiquity' is a passive proc — when player HP would hit 0, the shield rewinds 5 seconds of game-state (custom event). Cooldown: 1 in-game day. The Tu Si Trident is a weapon item with spell-absorption (active: right-click to absorb the next hostile spell cast within 16 blocks).",
            "AI usage (Wang Lin NPC): The NPC's Ancient God Tactic is a passive always-on (drives the NPC's star-tier progression). The NPC keeps the Azure Ancient God Shield equipped in the offhand for the death-proc. The Tu Si Trident is reserved for spell-caster enemies (absorbs their spells). The NPC avoids deploying Tu Si remnant items against Daoist Water-tier enemies (canon: Daoist Water destroyed these)."
    );
}
