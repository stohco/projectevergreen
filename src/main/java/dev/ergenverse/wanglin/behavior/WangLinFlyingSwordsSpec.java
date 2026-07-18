package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinFlyingSwordsSpec — Wang Lin's flying swords, sword sheaths, and sword spirits.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §2, §6):</b> Wang Lin's flying swords include:
 * Wealth (first), Core-Treasure Sword (teleportation), Dark Green Flying Sword (poison),
 * God-Slaying Sword, Rain Celestial Sword, Origin Swords (6), etc. The Sword Sheaths ×5
 * are mysterious enhancers (suspected Immortal World objects). Sword spirits include
 * Jufu / 巨斧 (the sword-spirit of the Rain Celestial Sword, later passed to Xu Liguo).
 */
public final class WangLinFlyingSwordsSpec {

    private WangLinFlyingSwordsSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "wang_lin_flying_swords",
            "Wang Lin's Flying Swords + Sword Sheaths + Sword Spirits", "王林飞剑·剑鞘·剑灵",
            Provenance.explicit("Renegade Immortal", List.of("throughout"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §2, §6"),
            "Collected across the journey: Wealth (1st, Heng Yue Sect era), Core-Treasure Sword (mid-Foundation era), Dark Green Flying Sword (evolved from Core-Treasure), God-Slaying Sword (Soul Transformation era), Rain Celestial Sword (Ch. 717 — Zhou Yi's gift), Origin Swords (Ch. 1561+ — condensed via void-gate vortex). Sword Sheaths ×5 collected across the journey.",
            "Wang Lin's flying swords progress from mortal-tier (Wealth) through spirit-tier (Core-Treasure, Dark Green) to celestial-tier (God-Slaying, Rain Celestial) to origin-tier (6 Origin Swords: Fire, Thunder, Karma, Life-Death, True-False, Slaughter, Restriction). The Sword Sheaths ×5 are mysterious enhancers — inserting a flying sword enhances different powers. Suspected Immortal World objects. One sheath is suspected to be a Sub-Void-Nirvana Sword. Sword spirits: Jufu (巨斧) was the sword-spirit of the Rain Celestial Sword, later passed to Xu Liguo (devil head).",
            List.of(
                    "Qi Condensation+ (for Wealth)",
                    "Foundation Establishment+ (for Core-Treasure Sword blood-refinement)",
                    "Soul Transformation+ (for God-Slaying Sword)",
                    "Zhou Yi's conditional gift (for Rain Celestial Sword — must protect the celestial corpse in the pagoda)",
                    "Void-gate collapse vortex (for the 6 Origin Swords)"
            ),
            ResourceCost.qi(15,
                    "Per-sword activation: low qi cost (15 units). Each sword has its own attributes. Cooldown: 0 (flying swords are persistent once summoned)."),
            new ActivationModel(
                    ActivationModel.Trigger.WILL_THOUGHT,
                    "Flying swords are activated via will-thought (divine sense command). The sword flies out and attacks the target.",
                    "Qi Condensation+ (for basic flying swords)",
                    List.of("The flying sword itself"),
                    List.of("Sword-refining skill (for blood-refinement evolutions)")
            ),
            new RangeModel(
                    1, 64, 0, RangeModel.Targeting.SINGLE_TARGET,
                    "Flying swords have melee-to-mid range. Origin-tier swords can reach beyond the immediate battlefield."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.CULTIVATION_REALM,
                    "sword_power = sword_grade × user_cultivation × user_sword_intent",
                    "Each realm breakthrough amplifies the sword's flight speed, attack power, and divine-sense-resonance.",
                    List.of("Sword Sheaths ×5 (enhancers)", "Sword spirits (Jufu / Xu Liguo)",
                            "Origin Essences (for Origin Swords)"),
                    "Wealth (mortal) → Core-Treasure Sword (spirit, teleportation) → Dark Green Flying Sword (spirit, poison) → God-Slaying Sword (celestial) → Rain Celestial Sword (celestial, mid-quality) → 6 Origin Swords (origin-tier, void-gate-condensed)",
                    "Higher-tier swords scale more strongly with cultivation."
            ),
            List.of(
                    "Stronger defensive treasures (Ancient God Leather Armour, etc.)",
                    "Restriction-Breaking Ancient Mirror (reflects sword intent)",
                    "Heaven-Avoiding Coffin (preserves souls from sword strikes)"
            ),
            List.of(
                    "Wealth was destroyed by Teng Huayuan (cannot be re-forged — soul-binding contract)",
                    "Core-Treasure Sword evolved (cannot return to original form)",
                    "God-Slaying Sword was destroyed by Daoist Water (later restored)",
                    "Rain Celestial Sword is conditional — must protect the celestial corpse"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Sword potency scales with the world's law strength.",
                            0.8, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.SEALED_REALM,
                            "Inside the Sealed Realm, sword flight speed is slightly reduced.",
                            0.9, "RI throughout")
            ),
            new VisualDescription(
                    "Each flying sword is a distinct blade. Wealth (basic sword), Core-Treasure (glowing teleportation-effect), Dark Green (poison-green blade), God-Slaying (celestial gold), Rain Celestial (rain-blue, mid-quality celestial), Origin Swords (each colored per essence: Fire=red, Thunder=gold, Karma=green, Life-Death=black-white, True-False=shifting, Slaughter=blood-red, Restriction=inkstone-black).",
                    List.of("Spirit-metal (per sword)", "Blood (for blood-refined)", "Celestial essence",
                            "Origin essence (for Origin Swords)"),
                    "Variable per sword", "Variable per sword",
                    "Polished metal. Blood-refined swords have a faint red sheen.",
                    "1×1×0 (small blade) to 2×1×0 (longer blade)",
                    "Sword floats beside the user; faint divine-sense tether visible.",
                    "Sword flies at the target; trail of the sword's attribute color.",
                    "Per-attribute particles trailing the sword's flight path.",
                    "Per-attribute glow around the blade.",
                    "A sharp metallic ring on activation.",
                    "Faint humming while floating beside the user.",
                    "A piercing strike sound on impact."
            ),
            List.of(
                    new StateDescription("Idle (floating beside user)", "Sword hovers beside the user via divine-sense tether.",
                            "User summons the sword", "User dismisses the sword or it is destroyed",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new StateDescription("Attacking (flying at target)", "Sword flies at the target on user's command.",
                            "User wills an attack", "Sword strikes the target or is parried",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new StateDescription("In Sheath (enhanced)", "Sword inserted into a Sword Sheath — gains enhanced power.",
                            "User inserts sword into sheath", "User draws the sword",
                            "CANON_RI_COMPLETE_ITEMS.md §6: 'Inserting a flying sword enhances different powers'"),
                    new StateDescription("Spirit-manifested (Jufu / Xu Liguo)", "The Rain Celestial Sword's spirit (Jufu / 巨斧) manifests.",
                            "Sword-spirit is invoked", "Spirit is recalled",
                            "CANON_RI_COMPLETE_ITEMS.md §2: 'sword-spirit was later passed to Xu Liguo'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Sword Sheaths ×5 enhance flying swords inserted into them.",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.STORES,
                            "The Rain Celestial Sword's spirit (Jufu) was passed to Xu Liguo (devil head).",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Flying swords are commanded via divine sense.",
                            "CANON_RI_COMPLETE_ITEMS.md §2"),
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The Origin Swords were condensed via the void-gate collapse vortex — Ancient God resonance.",
                            "RI Ch. 1561+")
            ),
            "Minecraft impl: implement each flying sword as a WangLinItem with a right-click action 'Fly to target' (using a custom FlyingSwordEntity that homes on the looked-at entity). Use the Sword Sheath as an item with an inventory slot (insert a sword → sword gains an NBT 'Ergen.Enhanced' modifier). The Rain Celestial Sword has a special 'Summon Spirit' action that spawns a Jufu / Xu Liguo NPC companion.",
            "NPC usage (canon): Wang Lin (primary wielder). Xu Liguo (sword-spirit of the Rain Celestial Sword, post-Ch. 717). Zhou Yi (gifted the Rain Celestial Sword to Wang Lin). Various enemies wield flying swords Wang Lin later acquired in battle.",
            "Player usage (mod): Right-click a flying sword → launches a FlyingSwordEntity that homes on the looked-at entity (damage scales with sword tier). Insert a sword into the Sword Sheath item → applies 'Ergen.Enhanced' NBT (damage multiplier). The Rain Celestial Sword's 'Summon Spirit' action (sneak-right-click) spawns a Jufu / Xu Liguo tameable NPC companion (max 1 active).",
            "AI usage (Wang Lin NPC): The NPC cycles flying swords based on enemy type: Wealth Flying Sword for trash, Rain Celestial Sword for celestial-tier, Core Treasure Sword for boss-tier. The NPC keeps the Rain Celestial Sword's spirit (Xu Liguo) summoned during long fights. The NPC sheathes swords in the Sword Sheath between combats (to apply the Enhanced modifier)."
    );
}
