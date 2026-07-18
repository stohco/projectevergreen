package dev.ergenverse.wanglin.behavior;

import dev.ergenverse.canon.Provenance;

import java.util.List;

/**
 * WangLinAncientGodPowersSpec — Wang Lin's Ancient God lineage abilities.
 *
 * <p><b>Canon (CANON_RI_COMPLETE_TECHNIQUES.md §A, SamsaraDao.java, HeavenDefyingBead.java):</b>
 * The Ancient God Tactic reconstructs the cultivator's body as an Ancient God.
 * Progression: 1★ (Ch. 199) → 27★ (Ch. 2003) → Half-Heaven-Trampling (Ch. 2062) → Heaven-Trampling (Ch. 2087).
 * Powers include: body-tempering (skin/leather/nail), Ancient God leather armor (8-star skin),
 * 'Dreaming Back to Antiquity' life-saving spell (8-star), 'Ancient Blessing of the Nine-Star Ancient God',
 * plundering (the essence of the Ancient God Tactic), absorbing spiritual energy/pills/inheritance as stars.
 */
public final class WangLinAncientGodPowersSpec {

    private WangLinAncientGodPowersSpec() {}

    public static final BehavioralSpec SPEC = new BehavioralSpec(
            "wang_lin_ancient_god_powers",
            "Wang Lin's Ancient God Lineage Powers", "王林古神之力",
            Provenance.explicit("Renegade Immortal", List.of("Ch. 190 (Ancient God Tactic)", "Ch. 199 (1★)", "Ch. 2003 (27★)"), 5,
                    "CANON_RI_COMPLETE_TECHNIQUES.md §A; SamsaraDao.java; HeavenDefyingBead.java"),
            "Inherited from Tu Si's memory legacy (Ch. 190). The Ancient God Tactic's essence: plunder — plunder everything.",
            "The Ancient God Tactic reconstructs the cultivator's body as an Ancient God. Outer-body cultivation track (main body sinks into the earth to absorb spiritual energy while the Avatar cultivates normally). Allows absorbing spiritual energy, pills, and inheritance fragments as stars. Progression: 1★ (Ch. 199) → 2★ (Ch. 285) → 3★ (Ch. 391) → 4★ (Ch. 708/940) → 5★ (Ch. 944) → 6★ (Ch. 1160) → 7★ (Ch. 1290/1472/1538 golden) → 13★ Ancient Clan (7G/6D) (Ch. 1539) → 20★ (7G/7M/6D) (Ch. 1577) → 24★ (8G/8M/8D) (Ch. 1705) → 26★ (9G/8M/9D) (Ch. 1777) → 27★ (Ch. 2003). Reaches Half-Heaven-Trampling (Ch. 2062) and Heaven-Trampling (Ch. 2087) via this track.",
            List.of(
                    "Ancient God Tactic (Tu Si inheritance)",
                    "Tu Si's 'knowledge' inheritance",
                    "Restriction Mountain Trial completion",
                    "Main body reconstruction (Ch. 199 — 1★)"
            ),
            ResourceCost.UNKNOWN,
            new ActivationModel(
                    ActivationModel.Trigger.PASSIVE_ALWAYS_ON,
                    "Ancient God powers are passive (the body is continuously reconstructed). The 'plunder' essence is reflexive — spiritual energy, pills, and inheritance fragments are absorbed as stars automatically.",
                    "1★ Ancient God (Ch. 199)",
                    List.of("Ancient God body (1★+)"),
                    List.of("Tu Si's 'knowledge' inheritance")
            ),
            new RangeModel(
                    0, -1, 0, RangeModel.Targeting.SELF_ONLY,
                    "Ancient God powers primarily affect the user. The 'plunder' essence extends to a sphere around the user (absorbing spiritual energy from the surroundings)."
            ),
            new ScalingModel(
                    ScalingModel.ScalesWith.STAR_TIER,
                    "ancient_god_power = (star_tier) × (user_cultivation) × (tu_si_inheritance_depth)",
                    "Each star tier unlocked amplifies all Ancient God powers. The 7★ golden state (Ch. 1538) is a major breakthrough catalyst. The 13★ Ancient Clan fusion (God+Demon+Devil) is another.",
                    List.of("Ancient God body (1★-27★)", "Ancient God Leather Armour",
                            "Azure Ancient God Shield", "Ancient God Trident", "Ancient God Furnace"),
                    "1★ → 7★ (golden) → 13★ Ancient Clan → 24★ → 27★ → Half-Heaven-Trampling → Heaven-Trampling",
                    "More star-tiers = more potent Ancient God powers."
            ),
            List.of(
                    "Daoist Water (Third-Step) — destroyed Ancient God Trident + Furnace",
                    "Tuo Sen (Tu Si's 'power' inheritor) — rival",
                    "Heaven-Defying Bead removal (Wang Lin's bead-bound powers would diminish)"
            ),
            List.of(
                "Hair turns white after absorbing Tu Si's legacy (visual marker)",
                "Cannot be transferred — bound to Wang Lin's body",
                "Ancient God body is vulnerable to Ancient God-killer weapons"
            ),
            List.of(
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.ANCIENT_GOD_BODY,
                            "Inside an Ancient God's body (Tu Si's body = Land of the Ancient God), all Ancient God powers resonate with peak potency.",
                            1.5, "RI Ch. 190+"),
                    new EnvironmentalEffect(EnvironmentalEffect.Factor.WORLD_LAW_STRENGTH,
                            "Ancient God power scales with the world's law strength.",
                            0.8, "RI throughout")
            ),
            new VisualDescription(
                    "The Ancient God body is a reconstructed physical form. Star-tiers manifest as glowing stars within the body (1★ = 1 star visible; 27★ = 27 stars). The 7★ golden state has a golden aura. The 13★ Ancient Clan state has God (gold), Demon (red), and Devil (purple) stars. Hair is white (post-Tu Si absorption).",
                    List.of("Ancient God bone", "Ancient God skin", "Star-tiers (visible as glowing stars)"),
                    "#8e44ad (Ancient God purple)", "#f39c12 (star gold)",
                    "Dense, ancient, otherworldly. Each star-tier is visible as a glowing point within the body.",
                    "1×2×1 (humanoid)",
                    "Star-tiers pulse with the user's cultivation.",
                    "Stars ignite; body glows with Ancient God power.",
                    "Purple-gold particles spiraling around the body.",
                    "Ancient God purple-gold glow.",
                    "A deep resonant hum — like striking ancient bronze.",
                    "Faint star-twinKle.",
                    "A heavy thud (Ancient God power impact)."
            ),
            List.of(
                    new StateDescription("1★ Ancient God", "First Ancient God reconstruction (Ch. 199).",
                            "Ancient God Tactic absorbed (Ch. 190) + main-body reconstruction (Ch. 199)", "2★ breakthrough (Ch. 285)",
                            "RI Ch. 199"),
                    new StateDescription("7★ Golden", "7★ golden state (Ch. 1538).",
                            "Pseudo Ch. 1290; confirmed Ch. 1472; golden Ch. 1538", "13★ Ancient Clan fusion (Ch. 1539)",
                            "RI Ch. 1290-1538"),
                    new StateDescription("13★ Ancient Clan (7G/6D)", "First Ancient Clan fusion (God+Demon) — Ch. 1539.",
                            "Ancient Demon clone + Ancient God main body fuse", "20★ (Ch. 1577)",
                            "RI Ch. 1539"),
                    new StateDescription("24★ Ancient Clan (8G/8M/8D)", "Three-way fusion (God+Demon+Devil) — Ch. 1705.",
                            "Ancient Devil clone fuses with the Ancient One", "26★ (Ch. 1777)",
                            "RI Ch. 1705"),
                    new StateDescription("27★ Peak", "Peak Ancient Clan state — Ch. 2003.",
                            "9 God / 8 Demon / 9 Devil stars", "Half-Heaven-Trampling (Ch. 2062)",
                            "RI Ch. 2003"),
                    new StateDescription("Half-Heaven-Trampling", "Approaching the 4th Step — Ch. 2062.",
                            "27★ peak + Reincarnation Essence comprehension begins", "Heaven Trampling (Ch. 2087)",
                            "RI Ch. 2062"),
                    new StateDescription("Heaven-Trampling (Ancient God fusion)", "The Ancient God body fuses with the primordial spirit — Ch. 2087.",
                            "Reincarnation Essence complete", "Permanent (transcendence)",
                            "RI Ch. 2087"),
                    new StateDescription("Plundering", "The Ancient God Tactic's essence: plunder — absorb spiritual energy, pills, inheritance fragments as stars.",
                            "Always active (passive)", "Permanent",
                            "RI Ch. 190: 'The essence of the art is one word: plunder'")
            ),
            List.of(
                    new SystemInteraction(SystemInteraction.SystemType.ANCIENT_GOD_LINEAGE,
                            SystemInteraction.InteractionType.RESONATES_WITH,
                            "All Ancient God powers resonate with the Ancient God body.",
                            "RI throughout"),
                    new SystemInteraction(SystemInteraction.SystemType.SOULS,
                            SystemInteraction.InteractionType.CONSUMES,
                            "The Ancient God Tactic can absorb inheritance fragments as stars.",
                            "RI: 'Allows absorbing spiritual energy, pills, and inheritance fragments as stars'"),
                    new SystemInteraction(SystemInteraction.SystemType.AVATARS_CLONES,
                            SystemInteraction.InteractionType.AMPLIFIES,
                            "The Ancient God body complements the Avatar (which cultivates normally).",
                            "RI: 'Outer-body cultivation track (the main body sinks into the earth to absorb spiritual energy while the Avatar cultivates normally)'"),
                    new SystemInteraction(SystemInteraction.SystemType.REINCARNATION,
                            SystemInteraction.InteractionType.FUSES_WITH,
                            "At Heaven Trampling, the Ancient God body fuses with the primordial spirit.",
                            "RI Ch. 2087")
            ),
            "Minecraft impl: implement as a player Capability 'AncientGodBody' with NBT 'Ergen.StarTier' (1-27) and 'Ergen.ClanComposition' (e.g. '7G/6D'). Each star-tier grants passive buffs (max health, attack damage, divine-sense radius). The 'plunder' essence is a passive aura that absorbs nearby XP orbs and converts them to 'Ergen.PlunderedEnergy' (used for breakthroughs). The 7★ golden state is a milestone (visual gold aura). The 13★ Ancient Clan fusion requires the player to also have an Ancient Demon clone AND Ancient Devil clone (cross-refs the Avatar system). At 27★ + Reincarnation Essence completion, the player can trigger Heaven Trampling (end-game transcendence event).",
            "NPC usage (canon): Wang Lin (Ch. 199 — 1-Star; Ch. 2087 — Heaven Trampling). Tu Si (the original Ancient God; Wang Lin inherited his legacy). Wang Lin is the ONLY canon cultivator to achieve 27-Star Ancient Clan. No other NPC wields this body-track.",
            "Player usage (mod): The Ancient God Body capability auto-activates when the player completes the Tu Si inheritance quest. Star-tier increments via 'plunder' (kill entities → absorb XP → tier up at thresholds). At 7★ the player unlocks 'Golden Stars' (fuse with Celestial power). At 13★ the player must complete the Ancient Clan fusion (Ancient Demon + Ancient Devil clone capabilities). At 27★ + Reincarnation Essence → unlocks Heaven Trampling (final transcendence).",
            "AI usage (Wang Lin NPC): The NPC's Ancient God Body is a passive always-on. The NPC's star-tier progression follows the canonical timeline (1-Star Ch. 199 → 27-Star Ch. 2003). The 'plunder' aura auto-absorbs XP orbs in a 32-block radius. The NPC fuses stars at the canonical chapter beats (story-driven). The NPC triggers Heaven Trampling at end-game (Reincarnation Essence completion)."
    );
}
