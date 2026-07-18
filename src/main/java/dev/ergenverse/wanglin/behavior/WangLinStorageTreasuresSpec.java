package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinStorageTreasuresSpec — Wang Lin's storage pouches, rings, and pocket-worlds.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §6):</b>
 * - Storage Space (Pocket) — accessed via Space Stone; standard cultivator storage
 * - Space Stone (Ch. 1838) — quasi-Third-Step; 1 of 3 promised gifts from Great Soul Sect founder
 * - Collection Pavilion (Ch. 784) — celestial-tier; took from Thunder Celestial Realm
 * - Earth Palace (Ch. 1478) — Ye Mo inheritance treasure
 * - Fate Sealing Ring (Ch. 1631) — celestial-tier; sealed within divine retribution
 * - Sword Sheaths ×5 — mysterious enhancers (suspected Immortal World objects)
 */
public final class WangLinStorageTreasuresSpec {

    private WangLinStorageTreasuresSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "wang_lin_storage_treasures",
            "Wang Lin's Storage Treasures (Pouches/Rings/Pocket-Worlds)", "王林储物宝",
            Provenance.explicit("Renegade Immortal", List.of("throughout"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §6"),
            "Collected across the journey. Standard cultivator storage from the start. Space Stone: gift from Great Soul Sect founder (Ch. 1838). Collection Pavilion: took from Thunder Celestial Realm (Ch. 784). Earth Palace: Ye Mo inheritance (Ch. 1478). Fate Sealing Ring: divine-retribution-forged (Ch. 1631). Sword Sheaths ×5: collected across the journey.",
            "Wang Lin's storage treasures progress from standard spatial pockets to quasi-Third-Step pocket-worlds. The Space Stone is a one-use-per-pocket treasure (instability → cluster collapse if overused). The Collection Pavilion is a celestial-tier pavilion that can change size and stores celestial spells. The Earth Palace is part of Ye Mo's heaven/earth/human palaces inheritance. The Fate Sealing Ring was sealed within divine retribution. The Sword Sheaths ×5 are mysterious enhancers — inserting a flying sword enhances different powers.",
            List.of(
                    "Qi Condensation+ (for standard storage)",
                    "Soul Transformation+ (for Collection Pavilion)",
                    "Ye Mo inheritance (for Earth Palace)",
                    "Divine retribution survival (for Fate Sealing Ring)"
            ),
            ResourceCost.UNKNOWN,
            new ActivationModel(
                    ActivationModel.Trigger.ITEM_INTERACTION,
                    "Storage treasures are accessed via right-click (item interaction). The Collection Pavilion can change size. The Space Stone consumes one pocket of space per use.",
                    "Qi Condensation+ (for standard); varies per treasure",
                    List.of("The storage treasure itself"),
                    List.of()
            ),
            new RangeModel(
                    0, 0, 0, RangeModel.Targeting.SELF_ONLY,
                    "Storage treasures affect only the user's inventory / pocket-dimension."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.CULTIVATION_REALM,
                    "storage_capacity = treasure_grade × user_cultivation",
                    "Each realm breakthrough expands accessible storage capacity.",
                    List.of("Heaven-Defying Bead (interior chamber for premium storage)"),
                    "Standard pocket → Space Stone (one-use-per-pocket) → Collection Pavilion (celestial-tier, size-changing) → Earth Palace (Ye Mo inheritance) → Fate Sealing Ring (divine-retribution-forged)",
                    "Higher-tier treasures scale more strongly."
            ),
            List.of(
                    "Spatial-restriction treasures (that collapse pockets)",
                    "Daoist Water (Third-Step) — destroyed many of Wang Lin's items"
            ),
            List.of(
                    "Space Stone: instability → cluster collapse if overused",
                    "Standard storage is vulnerable to spatial-restriction attacks",
                    "Fate Sealing Ring is bound to divine retribution"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Storage stability scales with the world's law strength.",
                            0.8, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.SEALED_REALM,
                            "Inside the Sealed Realm, storage stability is slightly reduced.",
                            0.9, "RI throughout")
            ),
            new VisualDescription(
                    "Each storage treasure is distinct. Standard pocket: a small pouch. Space Stone: a glowing crystal stone. Collection Pavilion: a small pavilion model that expands. Earth Palace: a small palace model. Fate Sealing Ring: a ring with divine-retribution crackling. Sword Sheaths: 5 ornate sheaths of varying materials.",
                    List.of("Spirit-fabric (pouch)", "Spatial crystal (Space Stone)",
                            "Celestial wood (Collection Pavilion)", "Ye Mo palace-material (Earth Palace)",
                            "Divine retribution (Fate Sealing Ring)", "Mysterious material (Sword Sheaths)"),
                    "Variable per treasure", "Variable per treasure",
                    "Variable. Storage treasures feel 'deeper than their size'.",
                    "1×1×1 (most); Collection Pavilion can expand",
                    "Subtle spatial shimmer around the item.",
                    "Pavilion expands; ring crackles; sheath glows when a sword is inserted.",
                    "Spatial particles (purple-blue) around active storage.",
                    "Subtle spatial glow.",
                    "A soft chime on access.",
                    "Silence.",
                    "A sharp crack if a pocket collapses."
            ),
            List.of(
                    new StateDescription("Idle", "Storage treasure sits in inventory.",
                            "User possesses the treasure", "User activates it",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new StateDescription("Accessed", "User accesses the storage inventory.",
                            "User right-clicks the treasure", "User closes the inventory",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new StateDescription("Expanded (Collection Pavilion)", "Collection Pavilion changes size.",
                            "User activates the size-change", "User reverts the size",
                            "CANON_RI_COMPLETE_ITEMS.md §6: 'Can change size'"),
                    new StateDescription("Sword inserted (Sheath)", "Flying sword inserted into a Sword Sheath — enhanced.",
                            "User inserts sword into sheath", "User draws the sword",
                            "CANON_RI_COMPLETE_ITEMS.md §6: 'Inserting a flying sword enhances different powers'"),
                    new StateDescription("Pocket consumed (Space Stone)", "One pocket of space consumed.",
                            "User activates the Space Stone", "Pocket is depleted",
                            "CANON_RI_COMPLETE_ITEMS.md §6: 'At the cost of one pocket of space... cannot be used more than once per pocket'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.SPACE_POCKET,
                            SystemInteraction.InteractionType.STORES,
                            "All storage treasures store items in pocket-dimensions.",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "Sword Sheaths ×5 enhance flying swords inserted into them.",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new SystemInteraction(SystemInteraction.SystemType.CELESTIAL_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Collection Pavilion stores celestial spells; resonates with celestial-tier items.",
                            "CANON_RI_COMPLETE_ITEMS.md §6"),
                    new SystemInteraction(SystemInteraction.SystemType.TRIBULATION,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Fate Sealing Ring was sealed within divine retribution.",
                            "CANON_RI_COMPLETE_ITEMS.md §6")
            ),
            "Minecraft impl: implement standard storage as a Container item (right-click opens a chest-like GUI). Space Stone as a single-use 'extra inventory slot' item (one use, then NBT 'Ergen.PocketsUsed' increments). Collection Pavilion as a placeable block (a small pavilion structure) that opens a large inventory when right-clicked. Earth Palace as a placeable structure (Ye Mo inheritance). Fate Sealing Ring as an accessory that grants 'divine_retribution_resistance'. Sword Sheaths as items with an inventory slot for one sword (insert grants NBT 'Ergen.Enhanced').",
            "NPC usage (canon): Wang Lin (primary owner). Various treasures were acquired from enemies (loot) or inherited (Earth Palace from Ye Mo; Fate Sealing Ring from the All-Seer's lineage). Storage treasures are universal in the cultivation world — many NPCs wield lower-tier variants.",
            "Player usage (mod): Right-click a storage treasure (Storage Pouch, Space Stone, etc.) → opens container GUI. Collection Pavilion and Earth Palace are placeable structures (right-click the block after placement). Fate Sealing Ring is an accessory (curios slot) granting 'divine_retribution_resistance' (reduces tribulation lightning damage by 30%). Sword Sheaths accept one sword (insert via right-click in inventory) → sword gains 'Ergen.Enhanced' NBT (damage multiplier 1.2×).",
            "AI usage (Wang Lin NPC): The NPC auto-loots battlefield drops into the largest available storage treasure. The NPC stores the Fate Sealing Ring in the accessory slot during tribulations. The NPC keeps the Sword Sheath populated with the primary flying sword. The NPC places a Collection Pavilion at the home-cave for bulk storage (long-term loot)."
    );
}
