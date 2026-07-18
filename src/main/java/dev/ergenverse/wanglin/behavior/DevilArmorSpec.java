package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * DevilArmorSpec — Wang Lin's devil armor (Scattered/Diving Devil Armour + Ancient God Leather).
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §3):</b>
 * - Scattered Devil Armour / Divine Devil Armour (Ch. 610) — pitch-black armor with a demonic feel.
 *   Took from a Scattered Devil while in the Demon Spirit Land (with help of Ancient Demon Bei Lou).
 *   In Cloud Sea Star System it's the God Sect's 'Divine Devil Armour'. Sold Ch. 1178.
 * - Ancient God Leather Armour (Ch. 758) — Origin-Soul-defensive treasure made from the skin of
 *   an 8-star Ancient God. Retained.
 */
public final class DevilArmorSpec {

    private DevilArmorSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "devil_armor",
            "Devil Armor (Scattered/Diving Devil + Ancient God Leather)", "魔甲·古神皮甲",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 610 (Scattered)", "Ch. 758 (Ancient God Leather)"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §3"),
            "Scattered Devil Armour: took from a Scattered Devil while in the Demon Spirit Land (with help of Ancient Demon Bei Lou) — Ch. 610. Ancient God Leather Armour: Ancient God inheritance — Ch. 758.",
            "Scattered Devil Armour / Divine Devil Armour: pitch-black armor with a demonic feel. Took from a Scattered Devil while in the Demon Spirit Land (with help of Ancient Demon Bei Lou). In the Cloud Sea Star System it's the God Sect's 'Divine Devil Armour'. Sold Ch. 1178. Ancient God Leather Armour: Origin-Soul-defensive treasure made from the skin of an 8-star Ancient God. Retained.",
            List.of(
                    "Scattered Devil: encounter in Demon Spirit Land + Ancient Demon Bei Lou's help",
                    "Ancient God Leather: Ancient God inheritance (Tu Si lineage)"
            ),
            ResourceCost.UNKNOWN,
            new ActivationModel(
                    ActivationModel.Trigger.PASSIVE_ALWAYS_ON,
                    "Armor is passive — worn on the body. Provides continuous defensive benefit.",
                    "Foundation Establishment+ (Scattered Devil); Core Formation+ (Ancient God Leather)",
                    List.of("The armor itself"),
                    List.of()
            ),
            new RangeModel(
                    0, 0, 0, RangeModel.Targeting.SELF_ONLY,
                    "Armor affects only the wearer."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.CULTIVATION_REALM,
                    "defense = armor_grade × user_cultivation × armor_attunement",
                    "Each realm breakthrough improves armor attunement.",
                    List.of("Ancient God body (for Ancient God Leather resonance)"),
                    "Single-state (each armor is what it is).",
                    "Higher-tier armor scales more strongly."
            ),
            List.of(
                    "Restriction-Breaking Ancient Mirror (reflects armor's demonic aura)",
                    "Stronger offensive treasures (Third-Step weapons)",
                    "Tu Si inheritance (for Ancient God Leather — exploited by Daoist Water)"
            ),
            List.of(
                    "Scattered Devil Armor is mortal-tier — destroyed by Third-Step attacks",
                    "Ancient God Leather is Ancient God-tier — vulnerable only to Ancient God-killer weapons",
                    "Scattered Devil Armor is tracking-vulnerable (demonic aura is detectable)"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Armor defense scales with the world's law strength.",
                            0.8, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.ANCIENT_GOD_BODY,
                            "Ancient God Leather resonates with Ancient God body.",
                            1.5, "RI Ch. 758")
            ),
            new VisualDescription(
                    "Scattered Devil: pitch-black plate armor with demonic carvings. Faintly smoking with demonic aura. Ancient God Leather: thick, leather armor made from 8-star Ancient God skin. Purple-tinted, dense, ancient-feeling.",
                    List.of("Scattered Devil: black metal, demonic runes",
                            "Ancient God Leather: 8-star Ancient God skin"),
                    "#1a1a1a (Scattered Devil black)", "#8e44ad (Ancient God purple)",
                    "Scattered Devil: rough, smoking. Ancient God Leather: dense, smooth, ancient.",
                    "1×2×1 (chest-piece + shoulders)",
                    "Faintly smoking (Scattered Devil) / faintly pulsing (Ancient God Leather).",
                    "Armor glows on impact (absorbing damage).",
                    "Black smoke (Scattered Devil) / purple-gold particles (Ancient God Leather).",
                    "Subtle dark glow.",
                    "A low demonic growl (Scattered Devil) / ancient hum (Ancient God Leather).",
                    "Silence.",
                    "A heavy thud on impact."
            ),
            List.of(
                    new StateDescription("Worn (Scattered Devil)", "Pitch-black armor with demonic aura.",
                            "Wang Lin equips the armor", "Wang Lin removes or sells it (Ch. 1178)",
                            "CANON_RI_COMPLETE_ITEMS.md §3"),
                    new StateDescription("Worn (Ancient God Leather)", "Ancient God skin armor, purple-tinted.",
                            "Wang Lin equips the armor (Ch. 758)", "Wang Lin removes it",
                            "CANON_RI_COMPLETE_ITEMS.md §3"),
                    new StateDescription("Damaged (Scattered Devil)", "Armor cracks; demonic aura flickers.",
                            "Armor takes damage", "Armor is repaired or destroyed",
                            "CANON_RI_COMPLETE_ITEMS.md §3 (inferred)"),
                    new StateDescription("Sold (Scattered Devil)", "Armor sold (Ch. 1178).",
                            "Wang Lin sells the armor", "Permanent",
                            "CANON_RI_COMPLETE_ITEMS.md §3")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Ancient God Leather resonates with the Ancient God body.",
                            "RI Ch. 758"),
                    new SystemInteraction(SystemInteraction.SystemType.CELESTIAL_LINEAGE,
                            SystemInteraction.InteractionType.SUPPRESSES,
                            "Scattered Devil's demonic aura suppresses celestial-tier items.",
                            "RI Ch. 610 (Demon Spirit Land)")
            ),
            "Minecraft impl: implement both as armor items (chestplate slot). Scattered Devil Armor: custom ArmorMaterial with high defense, 'demonic_aura' status effect on nearby entities (weakness). Ancient God Leather: custom ArmorMaterial with origin-soul-defense property — reduces divine-sense-attack damage. Use NBT for tracking-vulnerable flag (Scattered Devil) — nearby Ancient God entities can detect the wearer.",
            "NPC usage (canon): Wang Lin (Ch. 610 Scattered Devil; Ch. 758 Ancient God Leather). The Scattered Devil originally belonged to a Scattered Devil (defeated with Ancient Demon Bei Lou's help). The Ancient God Leather came from an 8-star Ancient God's skin (Tu Si lineage). Wang Lin sold the Scattered Devil Armor Ch. 1178; the Ancient God Leather was retained.",
            "Player usage (mod): Equip in chestplate slot. Scattered Devil Armor: passive 'demonic_aura' aura (nearby hostile mobs get Weakness I); NBT 'Ergen.Trackable' = true (Ancient God entities within 64 blocks aggro on the wearer). Ancient God Leather: reduces divine-sense-attack damage by 50%; NBT 'Ergen.TuSiLineage' = true (resonates with Ancient God body capability).",
            "AI usage (Wang Lin NPC): The NPC equips the Ancient God Leather as default chestpiece. The NPC dons the Scattered Devil Armor only when stealth is needed (demonic aura obscures identity from Celestial-tier trackers). The NPC sells the Scattered Devil Armor at the Cloud Sea Star System visit (Ch. 1178 emulation). The NPC avoids the Ancient God Leather in Ancient-God-hostile environments (where the lineage flag draws aggro)."
    );
}
