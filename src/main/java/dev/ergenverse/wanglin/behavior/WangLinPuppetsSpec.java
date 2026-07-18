package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinPuppetsSpec — Wang Lin's puppets (celestial guards, refined corpses, ancient slaves).
 *
 * <p><b>Canon (CANON_RI_COMPLETE_ITEMS.md §9, §10):</b>
 * <ul>
 *   <li>Celestial Guard — Copper Rank (Du Jian) (Ch. 653, exploded Ch. 762)</li>
 *   <li>Celestial Guard — Silver Rank (Thunder Daoist) (Ch. 707, shattered Ch. 717)</li>
 *   <li>Celestial Guard — Silver Rank (Ta Shan) (Ch. 815, freed Ch. 1025)</li>
 *   <li>Silver Poison Female Corpse (Ch. 930) — Nirvana Cleanser (early); sentient; memories intact; autonomous (originally the maid of the Seven-Coloured Celestial Sovereign)</li>
 *   <li>Yi Si Puppet (Ch. 1774) — ancient-tier from the Ancient Tomb 2nd floor</li>
 *   <li>Ling Dong (Ancient Slave) (Ch. 1450) — half-step Third-Step refined via Emperor Furnace</li>
 *   <li>Zhou Jin (Ch. 1470) — captured then freed when Wang Lin got injured</li>
 * </ul>
 *
 * <p><b>Method provenance:</b> The Celestial Guard method came from Huang Yu (who was captured via the Celestial Capture Net). Puppets copy the Ancient Gods — fight with their body and spells specially created for them. Wang Lin added a heavenly ghost to each, making them comparable to Illusionary-Yin cultivators.
 */
public final class WangLinPuppetsSpec {

    private WangLinPuppetsSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "wang_lin_puppets",
            "Wang Lin's Puppets (Celestial Guards + Refined Corpses + Ancient Slaves)", "王林傀儡",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 653 (Copper)", "Ch. 707 (Silver Thunder)", "Ch. 815 (Silver Ta Shan)", "Ch. 930 (Silver Poison)", "Ch. 1450 (Ling Dong)", "Ch. 1774 (Yi Si)"), 5,
                    "CANON_RI_COMPLETE_ITEMS.md §9"),
            "Each puppet is refined from a captured or dying cultivator. Method came from Huang Yu. Celestial Guards: copper/silver rank. Silver Poison Female Corpse: Alliance-Allheaven war acquisition. Yi Si Puppet: Ancient Tomb 2nd floor. Ling Dong: refined via Emperor Furnace. Zhou Jin: temporarily captured.",
            "Puppets that copy the Ancient Gods and fight only with their body and some spells specially created for them. Wang Lin added a heavenly ghost, making them comparable to Illusionary-Yin cultivators. Silver Poison Female Corpse: sentient, memories intact, autonomous (originally the maid of the Seven-Coloured Celestial Sovereign). Ling Dong: half-step Third-Step refined into ancient slave via Emperor Furnace. Yi Si: ancient-tier puppet from the Ancient Tomb.",
            List.of(
                    "Celestial Guard method (from Huang Yu)",
                    "Captured/dying cultivator body",
                    "Heavenly ghost (for Illusionary-Yin strength augmentation)",
                    "Emperor Furnace (for ancient slaves)"
            ),
            new ResourceCost(
                    0, 50, 50, 0, 0, 0, 0, 0,
                    "Refining a puppet: moderate soul-force cost (50 — to bind the celestial-guard runes) + moderate divine-sense cost (50 — to weave the heavenly ghost). Maintaining a puppet: low ongoing cost (1 divine-sense/sec, passive). Commanding in combat: low divine-sense per command (5). No cooldown — puppets fight autonomously once commanded."),
            new ActivationModel(
                    ActivationModel.Trigger.WILL_THOUGHT,
                    "Puppets are commanded via will-thought (divine sense command). The puppet fights autonomously once commanded.",
                    "Nascent Soul+ (to refine and command)",
                    List.of("Captured/dying cultivator body", "Celestial Guard method (Huang Yu)"),
                    List.of("Heavenly ghost (for Illusionary-Yin augmentation)")
            ),
            new RangeModel(
                    1, 32, 0, RangeModel.Targeting.SINGLE_TARGET,
                    "Puppets fight alongside Wang Lin at melee-to-mid range."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.SOULS_ABSORBED,
                    "puppet_power = source_cultivation × wang_lin_divine_sense × augmentation",
                    "Each puppet retains the strength of its source (e.g. Silver Poison = Nirvana Cleanser early). Wang Lin's divine sense augments their combat performance.",
                    List.of("Heavenly ghost (augmentation)", "Emperor Furnace (for ancient slaves)"),
                    "Copper Rank (Illusionary-Yin) → Silver Rank → Ancient Slave (half-step Third-Step) → Yi Si Puppet (ancient-tier)",
                    "Higher-tier source cultivator = stronger puppet."
            ),
            List.of(
                    "Stronger offensive treasures (Third-Step weapons)",
                    "Tattoo Clan Ancestor (forcefully removed Ta Shan's celestial-guard seal — Ch. 1025)",
                    "Wang Lin's injury (frees Zhou Jin — captured puppets can be freed if Wang Lin is hurt)"
            ),
            List.of(
                "Puppets can be shattered/exploded (Celestial Guards)",
                "Celestial-guard seals can be forcefully removed (Ta Shan)",
                "Captured puppets can be freed if Wang Lin is injured (Zhou Jin)"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Puppet potency scales with the world's law strength.",
                            0.8, "RI throughout"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.ANCIENT_GOD_BODY,
                            "Puppets copy Ancient Gods — resonate inside Ancient God bodies.",
                            1.2, "RI Ch. 653+: 'Puppets that copy the Ancient Gods'")
            ),
            new VisualDescription(
                    "Each puppet is a preserved cultivator body with celestial-guard runes inscribed. Copper Rank: copper-tinted runes. Silver Rank: silver-tinted runes. Silver Poison Female Corpse: a beautiful female corpse with silver poison aura. Ling Dong: half-step Third-Step ancient slave (Emperor Furnace-refined). Yi Si: ancient-tier puppet from the Ancient Tomb.",
                    List.of("Preserved cultivator body", "Celestial-guard runes (copper/silver)",
                            "Silver poison (Silver Poison Female Corpse)", "Emperor Furnace refinement (Ling Dong)"),
                    "#b87333 (Copper Rank)", "#c0c0c0 (Silver Rank)",
                    "Preserved flesh; runes inscribed in relief.",
                    "1×2×1 (humanoid)",
                    "Puppet stands motionless; runes faintly pulse with Wang Lin's divine sense.",
                    "Puppet moves to attack; runes glow with the augmentation.",
                    "Per-rank rune particles (copper/silver).",
                    "Subtle rank-color glow.",
                    "A low resonant hum when commanded.",
                    "Silence (when idle).",
                    "A heavy thud on impact."
            ),
            List.of(
                    new StateDescription("Idle (motionless)", "Puppet stands motionless; runes dormant.",
                            "Puppet not commanded", "Wang Lin commands the puppet",
                            "CANON_RI_COMPLETE_ITEMS.md §9"),
                    new StateDescription("Commanded (fighting)", "Puppet moves to attack on Wang Lin's command.",
                            "Wang Lin commands", "Combat ends or puppet is destroyed",
                            "CANON_RI_COMPLETE_ITEMS.md §9"),
                    new StateDescription("Destroyed (Celestial Guards)", "Puppet exploded/shattered.",
                            "Puppet takes lethal damage", "Permanent (cannot be restored)",
                            "RI: Copper Du Jian exploded Ch. 762; Silver Thunder Daoist shattered Ch. 717"),
                    new StateDescription("Freed (Ta Shan, Zhou Jin)", "Puppet seal removed; puppet freed.",
                            "Tattoo Clan Ancestor removes seal (Ta Shan) / Wang Lin injured (Zhou Jin)", "Permanent",
                            "RI: Ta Shan freed Ch. 1025; Zhou Jin freed when Wang Lin got injured"),
                    new StateDescription("Autonomous (Silver Poison)", "Sentient puppet acts on her own.",
                            "Silver Poison Female Corpse retains memories", "Permanent",
                            "RI Ch. 930: 'She can think and do stuff on her own'"),
                    new StateDescription("Spirit Manifestation (heavenly ghost awakens)", "The heavenly ghost stored within the puppet can manifest as a brief autonomous spirit — piloting the puppet's body for a final attack even after the puppet's runes are disrupted. The ghost is sentient enough to follow Wang Lin's last command but cannot persist beyond the puppet's destruction.",
                            "Puppet's runes are disrupted in combat (would-be Destroyed state) AND Wang Lin has a pending command", "Puppet's body fully destroyed OR ghost's brief window closes",
                            "RI Ch. 653+: 'Wang Lin added a heavenly ghost, making it comparable to an Illusionary-Yin cultivator'"),
                    new StateDescription("Damaged (runes cracked)", "Celestial-guard runes crack; puppet's combat performance drops; heavenly ghost leaks.",
                            "Puppet takes sub-lethal damage", "Wang Lin repairs the runes with divine sense + Ink-Stone paste",
                            "CANON_RI_COMPLETE_ITEMS.md §9 (inferred)")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "Puppets copy the Ancient Gods — resonate with Ancient God body.",
                            "RI Ch. 653+"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.STORES,
                            "Heavenly ghost is stored within the puppet for augmentation.",
                            "RI Ch. 653+: 'Wang Lin added a heavenly ghost'"),
                    new SystemInteraction(SystemInteraction.SystemType.DIVINE_SENSE,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Puppets are commanded via divine sense.",
                            "RI Ch. 653+"),
                    new SystemInteraction(SystemInteraction.SystemType.TREASURES,
                            SystemInteraction.InteractionType.REQUIRES,
                            "Ling Dong (ancient slave) requires the Emperor Furnace block to refine. The Celestial Guard method itself was a treasure (the Huang Yu inheritance).",
                            "RI Ch. 1450 (Ling Dong via Emperor Furnace)"),
                    new SystemInteraction(SystemInteraction.SystemType.FORMATIONS,
                            SystemInteraction.InteractionType.ANCHORS,
                            "Multiple puppets can be arrayed as a formation (Wang Lin's battlefield tactic) — each puppet anchors a node in the formation.",
                            "RI: battlefield use throughout the mid-late eras (inferred)"),
                    new SystemInteraction(SystemInteraction.SystemType.AVATARS_CLONES,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "When Wang Lin's Restriction Essence True Body manifests, puppet-command efficiency multiplies — one divine-sense thread can coordinate many puppets simultaneously.",
                            "RI: Restriction Essence True Body era (inferred from Wang Lin's mastery arc)")
            ),
            "Minecraft impl: implement each puppet as a tameable entity (like a Wolf but humanoid). Right-click with the Celestial Guard Method item on a captured/dying mob to refine it into a puppet. Puppets have an 'Ergen.PuppetRank' NBT (Copper/Silver/Ancient/Ancient-Slave). The Silver Poison Female Corpse has 'Ergen.Autonomous' = true (acts as a follower-NPC, not a commanded puppet). Ling Dong requires the Emperor Furnace block to refine. When the player takes damage, captured puppets have a chance to be freed (Zhou Jin mechanic).",
            "NPC usage (canon): Wang Lin only. The Celestial Guard method came from Huang Yu (Ch. 653). Individual puppets: Du Jian (Copper, exploded Ch. 762), Thunder Daoist (Silver, shattered Ch. 717), Ta Shan (Silver, freed Ch. 1025), Silver Poison Female Corpse (autonomous; originally the maid of the Seven-Coloured Celestial Sovereign), Yi Si (Ancient Tomb 2nd floor), Ling Dong (half-step Third-Step, Emperor Furnace), Zhou Jin (freed when Wang Lin injured).",
            "Player usage (mod): Capture a dying mob (HP < 25%) with the Celestial Capture Net → right-click the captured mob with the Celestial Guard Method item → puppet spawns (rank = source-mob tier). Right-click the puppet to command (follow/attack/guard). Puppets have an 'Ergen.PuppetRank' NBT (Copper/Silver/Ancient). Ling Dong specifically requires the Emperor Furnace multiblock (a placeable structure). The Silver Poison Female Corpse has 'Ergen.Autonomous' = true — acts as a follower NPC, not commandable. If the player takes damage > 50% max HP, captured puppets have a 5% chance per tick to be freed (Zhou Jin mechanic).",
            "AI usage (Wang Lin NPC): The NPC maintains a pool of puppets (canonical max 7). The NPC deploys puppets based on enemy tier: Copper for trash mobs, Silver for elites, Ling Dong (Ancient Slave) for boss-tier enemies. The NPC keeps the Silver Poison Female Corpse as a permanent follower (autonomous). When the NPC's HP drops below 25%, the NPC uses the heavenly-ghost auto-pilot (puppets fight on briefly even if the NPC is incapacitated). The NPC never deploys all puppets simultaneously — keeps 2 in reserve."
    );
}
