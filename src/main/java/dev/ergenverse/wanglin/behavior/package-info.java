/**
 * <h1>dev.ergenverse.wanglin.behavior — Behavioral Specifications</h1>
 *
 * <p>This package decomposes Wang Lin's signature items / techniques / inheritances
 * into <b>observable mechanics</b>: how the item is acquired, its lore, its
 * prerequisites, its resource cost, its activation model, range, scaling,
 * counters, weaknesses, environmental effects, visual description, states,
 * system interactions, and Minecraft implementation notes.
 *
 * <p>The user's directive (RI-FORGE-MASTER-REGISTRY-1):
 * <blockquote>
 *   Do not stop at cataloguing Wang Lin's possessions. Reverse-engineer them.
 *   Every canonical item, technique, beast, companion, avatar, inheritance,
 *   and treasure must be decomposed into its observable mechanics, demonstrated
 *   capabilities, limitations, activation requirements, interactions with other
 *   systems, visual description, and simulation behavior. The goal is not to
 *   know that Wang Lin possessed the Restriction Flag; the goal is to know
 *   enough about it that a Forge implementation would behave recognizably like
 *   the novel version.
 * </blockquote>
 *
 * <h2>Architecture</h2>
 * <p>Every signature item is a {@link dev.ergenverse.wanglin.behavior.BehavioralSpec}
 * record. Companion records capture structured sub-data:
 * <ul>
 *   <li>{@link dev.ergenverse.wanglin.behavior.ResourceCost} — qi, soul, divine sense, joss flame, life force</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.ActivationModel} — how the item is activated</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.RangeModel} — range, area, targeting</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.ScalingModel} — how it grows with cultivation</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.EnvironmentalEffect} — weather, terrain, time-of-day, world-law</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.VisualDescription} — physical, materials, color, texture, animation, particles, lighting, sound</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.StateDescription} — idle / activated / damaged / spirit-manifested</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.SystemInteraction} — with formations, treasures, souls, beasts, avatars, karma, restrictions, divine sense</li>
 * </ul>
 *
 * <p>Concrete spec classes (one per signature item) live in this package and
 * expose a static {@code SPEC} constant. They are loaded by
 * {@link dev.ergenverse.wanglin.behavior.BehavioralSpecRegistry} at bootstrap.
 *
 * <h2>Canon sourcing</h2>
 * <p>Every spec cites its source via {@link dev.ergenverse.canon.Provenance}.
 * Where canon is silent on a detail, the field is marked UNKNOWN — never
 * invented. Per the Prime Directive.
 *
 * <h2>Items with concrete specs (currently 13)</h2>
 * <ol>
 *   <li>{@link dev.ergenverse.wanglin.behavior.RestrictionFlagSpec} — Wang Lin's signature flag (3 variants)</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.SoulRefiningSpec} — Soul Refining Sect method</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.HeavenDefyingBeadSpec} — the bead (dual nature, dimension, time)</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.QingLinInheritanceSpec} — Qing Lin's spell (Celestial Wine, false caves)</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.SituNanInheritanceSpec} — Situ Nan's inheritance (Underworld Ascension + Vermilion Bird)</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.TuSiRemnantsSpec} — Tu Si's Ancient God remnants</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.HeavenTramplingBridgesSpec} — the 9 bridges (one spec with 9 sub-states)</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.WangLinFlyingSwordsSpec} — flying swords + sword sheaths + sword spirits</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.DevilArmorSpec} — the scattered/devil armor</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.WangLinStorageTreasuresSpec} — storage pouches / rings / pocket-worlds</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.WangLinPuppetsSpec} — corpse puppets + celestial guards + heaven-reaching puppets</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.WangLinAncientGodPowersSpec} — Ancient God lineage abilities</li>
 *   <li>{@link dev.ergenverse.wanglin.behavior.KarmaWhipSpec} — bonus: the Karma Whip (fused Soul Lasher + Karma Domain)</li>
 * </ol>
 */
package dev.ergenverse.wanglin.behavior;
