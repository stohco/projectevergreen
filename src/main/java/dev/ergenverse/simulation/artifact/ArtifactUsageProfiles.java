package dev.ergenverse.simulation.artifact;

import dev.ergenverse.canon.Provenance;
import dev.ergenverse.cultivation.RealmId;

import java.util.List;

/**
 * Registry of per-artifact usage profiles for Wang Lin's signature treasures.
 *
 * <p>Each profile defines what the treasure can do at each of the five
 * usage layers, grounded in canon where Er Gen described it. Every
 * threshold carries a {@link Provenance} citation.
 *
 * <p>This is a SUBSET — only the most important / most detailed treasures
 * have hand-written profiles. The remaining ~300 items use a default
 * profile based on their category and artifact realm.
 *
 * <p>Per the three-layer architecture, this is Layer 2 (Simulation).
 * The profiles cite canon sources (Layer 1) but define game mechanics.
 */
public final class ArtifactUsageProfiles {

    private static boolean bootstrapped = false;

    private ArtifactUsageProfiles() {}

    // ── God-Slaying Sword (杀神剑) ─────────────────────────────────

    /**
     * Wang Lin's primary celestial weapon. Acquired in the Soul
     * Transformation era. Destroyed by Daoist Water (Ch. 1273),
     * restored later.
     *
     * <p>Canon: one of Wang Lin's most-used weapons. A Soul Formation
     * cultivator can swing it (physical), a Void Treading cultivator
     * can use its full power (authority). At Dao level, the sword
     * expresses its true killing intent.
     */
    public static final ArtifactUsageProfile GOD_SLAYING_SWORD = ArtifactUsageProfile.builder(
            "wanglin/god_slaying_sword", "God-Slaying Sword", "杀神剑")
            .category("Weapon")
            .artifactRealm(RealmId.TRANSCENDENCE)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Soul Transformation era", "Ch. 1273"), 5))
            .baseDamage(25.0)
            .attackSpeed(0.8)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Celestial Steel", "The blade is forged from celestial materials.",
                            0.6, RealmId.QI_CONDENSATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Killing Intent", "The sword radiates a faint killing aura.",
                            0.4, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.6,   // qi cost
                    0.5,   // sense cost
                    false,  // no blood refine required
                    0.3,   // min compatibility
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Sword Qi Slash", "剑气斩",
                                    "A slash of concentrated sword qi.",
                                    0.4, 0.4, RealmId.SOUL_FORMATION,
                                    "A blade of qi extends from the sword."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Divine Sense Control", "神识御剑",
                                    "Control the sword with divine sense at range.",
                                    0.5, 0.7, RealmId.SOUL_TRANSFORMATION,
                                    "The sword flies at your command."
                            )
                    )
            ))
            .authorityRealm(RealmId.TRANSCENDENCE)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The God-Slaying Sword and its master become one killing intent.",
                    "You and the sword share a single thought. One slash, one kill.",
                    Provenance.inferred("Renegade Immortal", List.of("late novel"), 4,
                            "Wang Lin's sword mastery at peak is consistently described as 'one slash, one kill'")
            ))
            .spirit(null) // no named spirit for the God-Slaying Sword
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.0, List.of(),
                    "The sword hums with residual killing intent."
            ))
            .build();

    // ── Soul Lasher / Karma Whip (打魂鞭 / 因果鞭) ──────────────────

    /**
     * Originally won from Red Butterfly. Fused with Karma Domain in
     * Ch. 731 to become the Karma Whip. Directly attacks the
     * primordial/origin soul.
     *
     * <p>Canon: "In the Outer Realm, cleaved open 7 million worlds
     * with a single whip-strike." This is one of the most devastating
     * weapons in the entire novel.
     */
    public static final ArtifactUsageProfile SOUL_LASHER = ArtifactUsageProfile.builder(
            "wanglin/soul_lasher", "Soul Lasher", "打魂鞭")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_TRANSFORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("pre-Ch. 731", "Ch. 731"), 5))
            .baseDamage(8.0)
            .attackSpeed(1.4) // whips are fast
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Disruption Aura", "Nearby souls feel uneasy.",
                            0.3, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.5, false, 0.2,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Soul Strike", "打魂",
                                    "Directly attacks the target's origin soul.",
                                    0.3, 0.5, RealmId.NASCENT_SOUL,
                                    "The whip phases through the body and strikes the soul directly."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Warp Speed Strike", "极速抽击",
                                    "The whip moves at terrifying speed.",
                                    0.4, 0.3, RealmId.SOUL_FORMATION,
                                    "The whip becomes a blur, striking before the target can react."
                            )
                    )
            ))
            .authorityRealm(RealmId.SOUL_TRANSFORMATION)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The Karma Whip weaponizes cause and effect itself.",
                    "A single whip-strike. Seven million worlds cleaved open.",
                    Provenance.explicit("Renegade Immortal", List.of("Outer Realm arc"), 5)
            ))
            .spirit(null)
            .build();

    // ── 1st Restriction Flag (第一旗) ──────────────────────────────

    /**
     * Wang Lin's signature creation. Summons divine tribulation.
     * Requires 99,999 restrictions to complete. Has a sealed spirit.
     *
     * <p>Canon: the restriction flag is Wang Lin's most iconic
     * technique-based treasure. He learned the restriction art
     * through obsessive practice, and the flag is the ultimate
     * expression of that art.
     */
    public static final ArtifactUsageProfile RESTRICTION_FLAG = ArtifactUsageProfile.builder(
            "wanglin/restriction_flag", "1st Restriction Flag", "第一旗")
            .category("Restriction Treasure")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Nascent Soul era"), 5))
            .baseDamage(5.0) // not primarily a melee weapon
            .attackSpeed(0.6) // slow to swing — it's a flag, not a sword
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Tribulation Aura", "The flag emanates pressure that attracts lightning.",
                            0.5, RealmId.CORE_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.7, 0.6, true, 0.4, // requires blood refinement
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Summon Restrictions", "禁制召唤",
                                    "Release sealed restrictions to attack.",
                                    0.5, 0.5, RealmId.NASCENT_SOUL,
                                    "Restrictions fly out from the flag toward the enemy."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Divine Tribulation Summoning", "召唤天雷",
                                    "The flag summons divine tribulation lightning.",
                                    0.8, 0.7, RealmId.SOUL_FORMATION,
                                    "Lightning crackles around the flag. Heaven's wrath descends."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(TreasureSpirit.RESTRICTION_FLAG_SPIRIT)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.3, // 30% more dangerous than average
                    List.of(BacklashType.KARMIC_DEBT),
                    "The sealed spirits within the flag churn."
            ))
            .build();

    // ── Core-Treasure Sword / Dark Green Flying Sword ─────────────

    /**
     * Wang Lin's earliest significant weapon. Blood-refined.
     * Has a teleportation effect. Later becomes the Dark Green
     * Flying Sword with poison attribute.
     *
     * <p>Canon: "Took countless lives for Wang Lin." His most-used
     * weapon in the early-mid narrative.
     */
    public static final ArtifactUsageProfile CORE_TREASURE_SWORD = ArtifactUsageProfile.builder(
            "wanglin/core_treasure_sword", "Core-Treasure Sword", "核心宝剑")
            .category("Weapon")
            .artifactRealm(RealmId.CORE_FORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Foundation era"), 5))
            .baseDamage(12.0)
            .attackSpeed(1.2)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Blood-Refined Sharpness", "The blade is supernaturally sharp.",
                            0.5, RealmId.QI_CONDENSATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.3, 0.3, true, 0.3, // requires blood refinement
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Teleportation Strike", "瞬移斩",
                                    "Teleport to the target and strike.",
                                    0.5, 0.4, RealmId.FOUNDATION,
                                    "You vanish and reappear behind the target, sword already swinging."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Flying Sword", "御剑飞行",
                                    "Ride the sword through the air.",
                                    0.3, 0.5, RealmId.CORE_FORMATION,
                                    "The sword lifts you into the air."
                            )
                    )
            ))
            .authorityRealm(RealmId.CORE_FORMATION)
            .spirit(null)
            .build();

    // ── Heaven-Defying Bead (逆天珠) ─────────────────────────────

    /**
     * The single most important artifact in the entire novel. Sent back
     * through time by Wang Lin's clone Lu Mo. Contains a time-dilation
     * interior (10x), divine-sense storage, and eventually becomes one
     * with Wang Lin's primordial spirit.
     *
     * <p>ABSOLUTE_UNIQUE per OpportunityResolver — the player can never
     * obtain the original. This profile describes what the original does,
     * so the simulation can model derivative encounters.
     *
     * <p>Canon: Ch. 8 obtained; "root cause of the great war in the
     * Ancient Immortal Domain"; "contains Third-Step divine abilities
     * inside"; fuses with primordial spirit at Transcendence.
     */
    public static final ArtifactUsageProfile HEAVEN_DEFYING_BEAD = ArtifactUsageProfile.builder(
            "wanglin/heaven_defying_bead", "Heaven-Defying Bead", "逆天珠")
            .category("Core Treasure")
            .artifactRealm(RealmId.TRANSCENDENCE)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 8", "Ch. 1715"), 5,
                    "The bead's full nature is revealed across the entire novel"))
            .baseDamage(0.0)  // not a weapon
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Five Elements Resonance", "The bead pulses with primordial five-elements energy.",
                            0.8, RealmId.QI_CONDENSATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Shelter", "The bead can store and protect souls (Li Muwan's Nascent Soul).",
                            0.9, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.2, 0.8, false, 0.9,  // extremely high compatibility needed; low qi, HIGH sense
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Divine-Sense Storage Access", "神识存取",
                                    "Open the storage dimension via divine sense.",
                                    0.1, 0.6, RealmId.SOUL_FORMATION,
                                    "Your divine sense reaches into the bead. A vast space unfolds."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Time Dilation Interior", "时空内殿",
                                    "Enter the bead's interior where time flows at 10x speed.",
                                    0.1, 0.8, RealmId.SOUL_TRANSFORMATION,
                                    "The world inside the bead shifts. Time stretches — years pass in moments."
                            )
                    )
            ))
            .authorityRealm(RealmId.TRANSCENDENCE) // "Third Step" = Transcendence in our realm ladder
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The bead and Wang Lin's primordial spirit become one. He IS the bead.",
                    "You and the bead are no longer separate. You are the heaven-defying will incarnate.",
                    Provenance.explicit("Renegade Immortal", List.of("Ch. 1715", "end of novel"), 5,
                            "Fuses with primordial spirit at Transcendence")
            ))
            .spirit(null) // the bead is sentient but not a "spirit" in the TreasureSpirit sense
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    2.0, List.of(BacklashType.KARMIC_DEBT, BacklashType.SOUL_INJURY),
                    "The bead resists. It has chosen its master across time itself."
            ))
            .build();

    // ── Celestial Emperor Crown (仙帝冠) ─────────────────────────

    /**
     * A celestial-tier crown with five jewels channeling metal, wood,
     * water, fire, earth. Requires 9,999 mortal emperor souls to fully
     * activate.
     *
     * <p>Canon: Ch. 717; "requires killing 9,999 mortal emperors and
     * fusing their souls inside the crown to activate"; "usable due to
     * the blue rose Red Butterfly left Wang Lin."
     */
    public static final ArtifactUsageProfile CELESTIAL_EMPEROR_CROWN = ArtifactUsageProfile.builder(
            "wanglin/celestial_emperor_crown", "Celestial Emperor Crown", "仙帝冠")
            .category("Defensive / Utility")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 717"), 5,
                    "Five-element crown; 9,999 mortal emperor souls required for full activation"))
            .baseDamage(3.0)
            .attackSpeed(0.5)  // crowns are not weapons
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Five Elements Flow", "Metal, wood, water, fire, earth cycle through the crown.",
                            0.5, RealmId.CORE_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Imperial Aura", "The crown projects authority over mortals.",
                            0.4, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Five Elements Shield", "五行护盾",
                                    "Channel all five elements into a defensive barrier.",
                                    0.6, 0.5, RealmId.SOUL_FORMATION,
                                    "Five colored lights erupt from the crown, forming a layered shield."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Elemental Channeling", "元素引导",
                                    "Amplify any one of the five elements for external use.",
                                    0.4, 0.6, RealmId.ASCENDANT,
                                    "The crown's jewels blaze. One element surges through your body."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();

    // ── Straw Hat / Li Ming Straw Hat (黎明草帽) ───────────────────

    /**
     * A celestial-tier concealment treasure. Blocks divine senses,
     * conceals identity. Gifted by Yunque Zi to candidates for the
     * Title of Suzaku. Given to Ling'er Ch. 965.
     *
     * <p>Canon: "Permit its user to hide his identity. A lot of
     * restrictions are placed on it." "Contains numerous intricate
     * formations within. Concealment-type treasure."
     */
    public static final ArtifactUsageProfile STRAW_HAT = ArtifactUsageProfile.builder(
            "wanglin/straw_hat", "Li Ming Straw Hat", "黎明草帽")
            .category("Defensive / Concealment")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("early Celestial era", "Ch. 965"), 5,
                    "Yunque Zi's gift; divine-sense-blocking concealment treasure"))
            .baseDamage(0.0)  // not a weapon
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Divine Sense Block", "Wearer is invisible to divine sense scanning.",
                            0.9, RealmId.NASCENT_SOUL),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Identity Concealment", "The wearer's cultivation realm appears different.",
                            0.7, RealmId.SOUL_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.3, 0.6, false, 0.5,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Full Concealment", "完全隐匿",
                                    "Activate all restriction formations for total concealment.",
                                    0.3, 0.7, RealmId.SOUL_FORMATION,
                                    "The hat's restrictions hum. You vanish from all perception."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Restriction Release", "禁制释放",
                                    "Release the hat's internal restrictions as an attack.",
                                    0.5, 0.5, RealmId.ASCENDANT,
                                    "The intricate formations within the hat unravel outward as offensive restrictions."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();

    // ── Heaven-Avoiding Coffin (避天棺) ──────────────────────────

    /**
     * A spirit-tier coffin that shields its contents from heaven's
     * perception. Wang Lin placed Li Muwan's soul inside after her
     * body perished during Nascent Soul formation.
     *
     * <p>Canon: Ch. 819; "avoids heaven's perception, shielding the
     * soul from karmic detection." Primarily a soul-protection vessel.
     */
    public static final ArtifactUsageProfile HEAVEN_AVOIDING_COFFIN = ArtifactUsageProfile.builder(
            "wanglin/heaven_avoiding_coffin", "Heaven-Avoiding Coffin", "避天棺")
            .category("Soul / Storage")
            .artifactRealm(RealmId.SOUL_TRANSFORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 819"), 5,
                    "Acquired to hold Li Muwan's soul; heaven-avoiding property"))
            .baseDamage(6.0)
            .attackSpeed(0.4)  // a coffin is not agile
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Heaven Avoidance", "The coffin and its contents are invisible to heavenly tribulation.",
                            0.9, RealmId.NASCENT_SOUL),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Preservation", "Souls inside are preserved indefinitely.",
                            0.95, RealmId.QI_CONDENSATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.3, false, 0.2,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Seal Soul", "封魂",
                                    "Place a soul inside the coffin for protection.",
                                    0.3, 0.4, RealmId.NASCENT_SOUL,
                                    "The coffin opens. A soul is drawn inside and sealed away from heaven's gaze."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Release Soul", "释魂",
                                    "Release a preserved soul from the coffin.",
                                    0.2, 0.3, RealmId.SOUL_FORMATION,
                                    "The coffin opens. A preserved soul emerges, intact."
                            )
                    )
            ))
            .authorityRealm(RealmId.SOUL_TRANSFORMATION)
            .spirit(null)
            .build();

    // ── God-Slaying War Chariot (杀神战车) ────────────────────────

    /**
     * Three chariots forged by the Immortal Realm's Heavenly Treasure
     * Master. Each contains a beast soul. The High Quality chariot
     * (Butterfly soul) is the strongest — "easily kills those at the
     * peak of Yang-Solid stage."
     *
     * <p>Canon: Ch. 307 (1st), ~1080 (2nd), ~1100 (3rd). "Each
     * chariot possesses a beast soul." Self-destruct and repair cycle.
     */
    public static final ArtifactUsageProfile GOD_SLAYING_WAR_CHARIOT = ArtifactUsageProfile.builder(
            "wanglin/god_slaying_war_chariot", "God-Slaying War Chariot (High)", "杀神战车")
            .category("Weapon / Defensive / Mount")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 307", "Ch. ~1100", "Ch. 1276", "Ch. 1626"), 5,
                    "Three chariots with beast souls; High Quality has Butterfly soul"))
            .baseDamage(20.0)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Beast Soul Pressure", "The chariot radiates the aura of its bound beast.",
                            0.6, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Celestial Fortification", "The chariot's frame provides extraordinary defense.",
                            0.7, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.6, 0.5, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Beast Soul Unsealing", "兽魂解封",
                                    "Unseal the beast soul to attack independently.",
                                    0.7, 0.6, RealmId.SOUL_FORMATION,
                                    "The chariot shudders. A beast soul materializes and attacks."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "War Chariot Charge", "战车冲锋",
                                    "Ride the chariot at devastating speed.",
                                    0.5, 0.4, RealmId.ASCENDANT,
                                    "The chariot accelerates. Mountains and rivers blur past."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Butterfly Transformation", "蝶化",
                                    "The High Quality chariot transforms into its butterfly form.",
                                    0.8, 0.7, RealmId.ASCENDANT,
                                    "The chariot dissolves into a kaleidoscope of butterflies. Each one is lethal."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The chariot and its beast soul merge with the rider's Dao.",
                    "You ride the chariot through space itself. The butterfly's wings cut reality.",
                    Provenance.inferred("Renegade Immortal", List.of("late novel", "Ch. 1626"), 4,
                            "The restored chariot at peak performance is consistently devastating")
            ))
            .spirit(null) // beast soul, not a TreasureSpirit
            .build();

    // ── Ancient God Leather Armour (古神皮甲) ────────────────────

    /**
     * Origin-Soul-defensive treasure made from the skin of an 8-star
     * Ancient God. Part of the Tu Si inheritance.
     *
     * <p>Canon: Ch. 758; "Origin-Soul-defensive treasure made from the
     * skin of an 8-star Ancient God."
     */
    public static final ArtifactUsageProfile ANCIENT_GOD_LEATHER_ARMOUR = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_leather_armour", "Ancient God Leather Armour", "古神皮甲")
            .category("Defensive / Armor")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 758"), 5,
                    "8-star Ancient God skin; Origin-Soul defense"))
            .baseDamage(2.0)
            .attackSpeed(0.0)  // armor
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Origin Soul Barrier", "Protects the wearer's origin soul from direct attacks.",
                            0.9, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Ancient God Resilience", "The armour absorbs and disperses impact.",
                            0.7, RealmId.CORE_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.3, 0.3, true, 0.2,  // blood refinement to bond with Ancient God skin
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Full Origin Soul Defense", "元神全防",
                                    "Activate the armour's full origin-soul protection.",
                                    0.4, 0.5, RealmId.SOUL_TRANSFORMATION,
                                    "The ancient god skin hums. Your origin soul is wrapped in an impenetrable layer."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();

    // ── Azure Ancient God Shield (青光盾) ────────────────────────

    /**
     * Defensive treasure of an 8-star Ancient God. Contains the
     * life-saving divine ability "Dreaming Back to Antiquity."
     * Destroyed Ch. 1082, restored Ch. 1626 via Void Gate power.
     *
     * <p>Canon: Ch. 980; "contains the 8-star Ancient God's
     * life-saving divine ability 'Dreaming Back to Antiquity'."
     */
    public static final ArtifactUsageProfile AZURE_ANCIENT_GOD_SHIELD = ArtifactUsageProfile.builder(
            "wanglin/azure_ancient_god_shield", "Azure Ancient God Shield", "青光盾")
            .category("Defensive / Shield")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 980", "Ch. 1082", "Ch. 1626"), 5,
                    "8-star Ancient God life-saving spell; destroyed and restored"))
            .baseDamage(4.0)
            .attackSpeed(0.3)  // shields are clumsy weapons
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Ancient God Shield Aura", "Cyan light provides constant passive defense.",
                            0.6, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dreaming Back to Antiquity (dormant)", "The life-saving ability stirs when death approaches.",
                            0.3, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.4, true, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Cyan Light Barrier", "青光屏障",
                                    "Project the full defensive barrier.",
                                    0.5, 0.5, RealmId.SOUL_FORMATION,
                                    "A dome of cyan light envelops you. Attacks dissolve against it."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Dreaming Back to Antiquity", "梦回太古",
                                    "The 8-star Ancient God's life-saving ability — triggers near-death.",
                                    0.8, 0.6, RealmId.TRANSCENDENCE,
                                    "Death approaches. The shield blazes with ancient light. Time reverses around you."
                            )
                    )
            ))
            .authorityRealm(RealmId.TRANSCENDENCE)
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    0.8, List.of(BacklashType.QI_DEPLETION),
                    "The ancient god power demands a heavy price."
            ))
            .build();

    // ── 18-Hell Celestial Sealing Stamp (十八地狱封天印) ──────────

    /**
     * Self-forged by Wang Lin. Forms the 18-Layers-of-Hell-
     * Reincarnation-Realm with Underworld River. Stores all souls
     * of enemies killed. The ultimate sealing treasure.
     *
     * <p>Canon: Ch. 915; "fused with Magic-Arsenal Spell + Celestial
     * Sealing Stamp"; "forms the 18-Layers-of-Hell-Reincarnation-Realm."
     */
    public static final ArtifactUsageProfile EIGHTEEN_HELL_STAMP = ArtifactUsageProfile.builder(
            "wanglin/fragment_stamp", "18-Hell Celestial Sealing Stamp", "十八地狱封天印")
            .category("Sealing / Domain")
            .artifactRealm(RealmId.ILLUSORY_YIN)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 769", "Ch. 915"), 5,
                    "Refined from Fragment Stamp; forms 18-Layers-of-Hell"))
            .baseDamage(15.0)
            .attackSpeed(0.5)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Collection", "The stamp automatically captures souls of the slain.",
                            0.7, RealmId.ASCENDANT),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Hell Pressure", "The stamp radiates the pressure of the 18 hells.",
                            0.5, RealmId.SOUL_TRANSFORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.6, 0.7, false, 0.4,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Celestial Sealing", "封天",
                                    "Seal a target with the stamp's authority.",
                                    0.6, 0.7, RealmId.ILLUSORY_YIN,
                                    "The stamp descends. Heaven and earth seal around the target."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "18-Hell Domain", "十八地狱",
                                    "Manifest the 18-Layers-of-Hell Reincarnation Realm.",
                                    0.9, 0.9, RealmId.CORPOREAL_YANG,
                                    "The ground splits. The Underworld River flows. Eighteen hells manifest around you."
                            )
                    )
            ))
            .authorityRealm(RealmId.CORPOREAL_YANG)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The 18 hells become an extension of Wang Lin's Dao. He IS the judge of reincarnation.",
                    "You stamp the ground. The cycle of reincarnation answers your call. The dead rise to serve.",
                    Provenance.explicit("Renegade Immortal", List.of("Ch. 915", "late novel"), 5)
            ))
            .spirit(null)
            .build();

    // ── Dot Immortal Pen (点仙笔) ────────────────────────────────

    /**
     * The Golden Celestial Brush (Dot Immortal Pen). A celestial-tier
     * writing instrument that can inscribe restrictions in the air.
     * Obtained from Huang Yu via the Celestial Capture Net.
     *
     * <p>Canon: "Golden Celestial Brush" — used by Wang Lin to
     * inscribe restrictions and formations in midair.
     */
    public static final ArtifactUsageProfile DOT_IMMORTAL_PEN = ArtifactUsageProfile.builder(
            "wanglin/dot_immortal_pen", "Dot Immortal Pen", "点仙笔")
            .category("Utility / Restriction Tool")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 624"), 5,
                    "Golden Celestial Brush; captures Huang Yu with the Celestial Capture Net"))
            .baseDamage(8.0)
            .attackSpeed(1.0)  // pens are light and fast
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Restriction Insight", "The pen enhances the wielder's understanding of restrictions.",
                            0.5, RealmId.SOUL_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.6, false, 0.5,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Air Inscription", "空中禁制",
                                    "Inscribe restrictions directly into the air.",
                                    0.4, 0.7, RealmId.SOUL_FORMATION,
                                    "The pen moves through the air. Glowing restriction characters hang in space."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Formation Drawing", "画阵",
                                    "Draw a complete formation in moments.",
                                    0.6, 0.8, RealmId.ASCENDANT,
                                    "The pen traces a complex formation. Lines of power connect and activate."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();

    // ══════════════════════════════════════════════════════════════
    // HAND-WRITTEN PROFILES — Batch 2 (15 additional signature items)
    // ══════════════════════════════════════════════════════════════

    // ── Karma Whip (因果鞭) ──────────────────────────────────────

    /**
     * The Soul Lasher after fusing with Karma Domain (Ch. 731).
     * Directly attacks cause-and-effect chains. One of the most
     * devastating weapons in the entire novel.
     *
     * <p>Canon: "In the Outer Realm, cleaved open 7 million worlds
     * with a single whip-strike." The Karma Whip is the Soul Lasher's
     * evolved form — not a separate item.
     */
    public static final ArtifactUsageProfile KARMA_WHIP = ArtifactUsageProfile.builder(
            "wanglin/karma_whip", "Karma Whip", "因果鞭")
            .category("Weapon")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. 731", "Outer Realm arc"), 5,
                    "Soul Lasher + Karma Domain fusion"))
            .baseDamage(18.0)
            .attackSpeed(1.4)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Karma Threads", "The whip trails threads of cause and effect.",
                            0.7, RealmId.SOUL_TRANSFORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Causal Disruption", "Nearby enemies feel their fate shifting.",
                            0.5, RealmId.NIRVANA_SCRYER)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.6, 0.7, false, 0.5,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Karma Strike", "因果一击",
                                    "The whip attacks through cause and effect — dodging is meaningless.",
                                    0.5, 0.6, RealmId.SOUL_TRANSFORMATION,
                                    "The whip doesn't follow space. It follows causality. You cannot dodge what has already happened."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Seven Million Worlds", "七百万世界",
                                    "The ultimate expression — one strike cleaves worlds.",
                                    0.9, 0.9, RealmId.NIRVANA_FRUIT,
                                    "The whip descends. Space fractures. Worlds shatter like glass."
                            )
                    )
            ))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The Karma Whip weaponizes cause and effect itself. "
                    + "At Dao level, every strike is causally inevitable — the target "
                    + "was always going to be hit.",
                    "A single whip-strike. Seven million worlds cleaved open. "
                    + "The boundary between cause and effect dissolves.",
                    Provenance.explicit("Renegade Immortal", List.of("Outer Realm arc"), 5)
            ))
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.2, List.of(BacklashType.KARMIC_DEBT),
                    "Karma reverses upon the wielder if the strike fails."
            ))
            .build();

    // ── Ghostly Sail (鬼帆) ─────────────────────────────────────

    /**
     * Wang Lin's primary long-range travel artifact. A sail woven from
     * ghostly threads that moves at extreme speed. Used extensively from
     * the Soul Formation era onward.
     *
     * <p>Canon: Wang Lin's standard mode of rapid transit before he could
     * teleport. The sail's speed increased as his cultivation grew.
     */
    public static final ArtifactUsageProfile GHOSTLY_SAIL = ArtifactUsageProfile.builder(
            "wanglin/ghostly_sail_main", "Ghostly Sail", "鬼帆")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.SOUL_TRANSFORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Soul Formation era onwards"), 4))
            .baseDamage(2.0)  // not a weapon
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Ghostly Speed", "The sail moves with unnatural swiftness.",
                            0.6, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Spectral Veil", "The sail obscures the rider from detection.",
                            0.4, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.3, 0.4, false, 0.2,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Full Sail", "全速航行",
                                    "Deploy the sail for maximum speed travel.",
                                    0.3, 0.3, RealmId.SOUL_FORMATION,
                                    "The ghostly fabric catches an unseen wind. The world blurs."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Phantom Escape", "幻影遁走",
                                    "The sail becomes intangible, passing through obstacles.",
                                    0.5, 0.6, RealmId.SOUL_TRANSFORMATION,
                                    "The sail turns translucent. Mountains pass through you like mist."
                            )
                    )
            ))
            .authorityRealm(RealmId.SOUL_TRANSFORMATION)
            .spirit(null)
            .build();

    // ── Billion Soul Flag (亿万魂幡) ─────────────────────────────

    /**
     * Wang Lin's Soul Refining Sect signature treasure. Contains the
     * souls of countless cultivators. Can attack with soul waves,
     * absorb souls, and provide army-scale spiritual suppression.
     *
     * <p>Canon: Wang Lin refined this during his time with the Soul
     * Refining Sect. It is one of his most feared treasures — enemies
     * see it and know death is certain.
     */
    public static final ArtifactUsageProfile BILLION_SOUL_FLAG = ArtifactUsageProfile.builder(
            "wanglin/billion_soul_flag", "Billion Soul Flag", "亿万魂幡")
            .category("Flag / Banner")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Soul Refining Sect era"), 4))
            .baseDamage(10.0)
            .attackSpeed(0.6)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Pressure", "The flag radiates the anguish of billions of souls.",
                            0.8, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Absorption Aura", "Nearby souls are slowly drawn toward the flag.",
                            0.5, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.6, true, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Soul Wave", "魂波冲击",
                                    "Release a devastating wave of soul energy.",
                                    0.5, 0.5, RealmId.SOUL_FORMATION,
                                    "A thousand ghostly faces scream from the flag. The wave distorts space."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Soul Army", "万魂军团",
                                    "Manifest the refined souls as an army.",
                                    0.7, 0.7, RealmId.ASCENDANT,
                                    "Spectral warriors pour from the flag. Each one was once a cultivator."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Soul Devour", "噬魂",
                                    "Absorb an enemy's soul directly.",
                                    0.8, 0.8, RealmId.NIRVANA_SCRYER,
                                    "The flag opens like a maw. The enemy's soul is ripped free and drawn inside."
                            )
                    )
            ))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.5, List.of(BacklashType.SOUL_INJURY, BacklashType.KARMIC_DEBT),
                    "The souls within may turn on an unworthy master."
            ))
            .build();

    // ── Mosquito Beast (蚊兽) ───────────────────────────────────

    /**
     * Wang Lin's most iconic beast companion. Obtained in the Sea of
     * Devils. Can devour energy, split into swarms, and evolves through
     * cultivation stages alongside Wang Lin.
     *
     * <p>Canon: one of the most loyal companions in the novel. The
     * Mosquito Beast evolves multiple times — from a single beast
     * to a swarm of 10,000.
     */
    public static final ArtifactUsageProfile MOSQUITO_BEAST = ArtifactUsageProfile.builder(
            "wanglin/mosquito_beast", "Mosquito Beast", "蚊兽")
            .category("Beast / Companion")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Sea of Devils arc"), 5))
            .baseDamage(15.0)
            .attackSpeed(1.6) // very fast
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Devouring Aura", "The beast passively absorbs ambient energy.",
                            0.6, RealmId.CORE_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Swarm Sense", "The beast can sense nearby life forms.",
                            0.4, RealmId.FOUNDATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.3, false, 0.1, // beasts don't need compatibility — loyalty is the gate
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Energy Devour", "吞噬能量",
                                    "The beast devours an enemy's qi or spiritual energy.",
                                    0.4, 0.2, RealmId.CORE_FORMATION,
                                    "The mosquito's proboscis pierces the barrier. Energy drains away."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Swarm Split", "万蚊分化",
                                    "Split into 10,000 mosquitoes for area attacks.",
                                    0.6, 0.4, RealmId.SOUL_FORMATION,
                                    "One becomes ten thousand. A dark cloud of buzzing death."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Devil Mosquito Form", "魔蚊形态",
                                    "The beast enters its full devil-mosquito battle form.",
                                    0.7, 0.5, RealmId.ASCENDANT,
                                    "The mosquito grows to the size of a building. Its wings blot out the sky."
                            )
                    )
            ))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .spirit(new TreasureSpirit(
                    "Mosquito Beast", "蚊兽",
                    "Loyal and fierce. Limited intelligence but an unbreakable bond with its master. "
                    + "Communicates through emotion and simple intent, not language. "
                    + "Will fight to the death without hesitation.",
                    "Follows Wang Lin's commands unconditionally. Evolves through cultivation "
                    + "alongside its master. Canon: one of the few companions that stays "
                    + "with Wang Lin from the Sea of Devils to the end of the novel.",
                    Provenance.explicit("Renegade Immortal", List.of("Sea of Devils arc"), 5)
            ))
            .build();

    // ── Xu Liguo — Devil Sword (许立国 — 魔剑) ─────────────────

    /**
     * The sword spirit Xu Liguo. One of the most beloved characters in RI.
     * A cowardly, talkative, secretly loyal sword spirit. Originally the
     * spirit of the Rain Celestial Sword.
     *
     * <p>Canon: Xu Liguo is arguably the most memorable side character in
     * the novel. His constant complaining, groveling, and ultimate
     * dependability make him uniquely endearing.
     */
    public static final ArtifactUsageProfile XU_LIGUO_DEVIL = ArtifactUsageProfile.builder(
            "wanglin/xu_liguo_devil", "Xu Liguo (Devil Sword)", "许立国（魔剑）")
            .category("Weapon")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Ch. ~700+"), 5,
                    "The sword spirit and the person share a name — this is the spirit"))
            .baseDamage(22.0)
            .attackSpeed(1.3)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Combat Perception", "Xu Liguo senses enemy strength and warns the wielder.",
                            0.7, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Coward's Intuition", "Xu Liguo's fear actually detects real danger.",
                            0.5, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.3, false, 0.1, // Xu Liguo's gate is loyalty, not compatibility
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Complaining Slash", "抱怨斩",
                                    "Xu Liguo slashes while loudly complaining about the enemy.",
                                    0.3, 0.2, RealmId.SOUL_FORMATION,
                                    "\"Master, this one is too strong—\" The sword cuts anyway. Xu Liguo always follows through."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Rain of Swords", "万剑雨",
                                    "The sword multiplies into a rain of blades.",
                                    0.6, 0.5, RealmId.ASCENDANT,
                                    "One sword becomes ten thousand. Xu Liguo's complaints echo from each blade."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Devil Sword Full Power", "魔剑全力",
                                    "Xu Liguo stops complaining. This is when he's truly dangerous.",
                                    0.8, 0.7, RealmId.NIRVANA_FRUIT,
                                    "The complaints stop. The sword goes silent. Even the enemy pauses."
                            )
                    )
            ))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .spirit(TreasureSpirit.XU_LIGUO)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    0.8, // actually SAFER than average — Xu Liguo is loyal
                    List.of(),
                    "Xu Liguo complains but never retaliates against Wang Lin."
            ))
            .build();

    // ── Annihilation Restriction (灭世禁制) ─────────────────────

    /**
     * The ultimate restriction technique. Destroys everything in its
     * area of effect. One of Wang Lin's most powerful attacks in the
     * late novel.
     *
     * <p>Canon: represents the pinnacle of Wang Lin's restriction art,
     * which he developed through obsessive practice from the Nascent Soul
     * era. This is NOT a learned technique — it was self-created.
     */
    public static final ArtifactUsageProfile ANNIHILATION_RESTRICTION = ArtifactUsageProfile.builder(
            "wanglin/annihilation_restriction", "Annihilation Restriction", "灭世禁制")
            .category("Restriction / Formation")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Late novel"), 4,
                    "Wang Lin's self-created ultimate restriction"))
            .baseDamage(0.0) // not a physical item
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Restriction Mastery", "Understanding of all restrictions deepens.",
                            0.9, RealmId.SOUL_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.9, 0.8, true, 0.6,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Area Annihilation", "灭世范围",
                                    "Destroy everything within a large radius.",
                                    0.8, 0.7, RealmId.NIRVANA_SCRYER,
                                    "Restriction characters fill the air. Everything they touch ceases to exist."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Targeted Annihilation", "灭世锁定",
                                    "Focus the annihilation on a single target — inescapable.",
                                    0.9, 0.9, RealmId.NIRVANA_FRUIT,
                                    "The restriction converges on one point. Existence unravels."
                            )
                    )
            ))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The Restriction Dao itself manifests as annihilation of all form.",
                    "Wang Lin raises his hand. Reality holds its breath. Then it stops holding.",
                    Provenance.explicit("Renegade Immortal", List.of("Late novel"), 4)
            ))
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    2.0, // extremely dangerous
                    List.of(BacklashType.SOUL_INJURY, BacklashType.KARMIC_DEBT, BacklashType.DIVINE_RETALIATION),
                    "The annihilation does not distinguish friend from foe."
            ))
            .build();

    // ── Li Guang Heaven-Shattering Bow (李广惊天弓) ─────────────

    /**
     * Wang Lin's primary ranged weapon. Named after the legendary
     * general Li Guang. Fires arrows of condensed energy that can
     * shatter defensive formations.
     *
     * <p>Canon: used extensively in the mid-to-late novel for long-range
     * combat. One of Wang Lin's most reliable weapons.
     */
    public static final ArtifactUsageProfile LI_GUANG_HEAVEN_SHATTERING_BOW = ArtifactUsageProfile.builder(
            "wanglin/li_guang_heaven_shattering_bow", "Li Guang Heaven-Shattering Bow", "李广惊天弓")
            .category("Weapon")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Soul Formation era"), 4))
            .baseDamage(20.0)
            .attackSpeed(0.5) // bows are slow
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Piercing Intent", "Arrows fired carry heaven-shattering intent.",
                            0.6, RealmId.CORE_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.5, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Condensed Arrow", "凝气箭",
                                    "Fire an arrow of condensed qi.",
                                    0.3, 0.3, RealmId.CORE_FORMATION,
                                    "Qi gathers at the bowstring. The arrow materializes and flies."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Heaven-Shattering Shot", "惊天一箭",
                                    "Fire an arrow that can shatter formation barriers.",
                                    0.7, 0.6, RealmId.SOUL_FORMATION,
                                    "The bowstring thrums. The arrow carries the weight of a falling sky."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Li Guang Arrow Rain", "李广箭雨",
                                    "Unleash a barrage of arrows covering a wide area.",
                                    0.8, 0.7, RealmId.ASCENDANT,
                                    "The sky darkens with arrows. Each one seeks its target."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();

    // ── Earth Escape Technique (土遁术) ─────────────────────────

    /**
     * One of Wang Lin's earliest and most-used techniques. Allows
     * travel through earth and stone. Learned early and used throughout
     * the entire novel.
     *
     * <p>Canon: Wang Lin's bread-and-butter escape technique. Used in
     * virtually every major battle sequence. Not flashy, but essential.
     */
    public static final ArtifactUsageProfile EARTH_ESCAPE_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/earth_escape_technique", "Earth Escape Technique", "土遁术")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_FORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Early chapters", "Used throughout"), 5))
            .baseDamage(0.0) // not a weapon
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Earth Affinity", "The user can sense vibrations through earth.",
                            0.4, RealmId.QI_CONDENSATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.3, 0.2, false, 0.1,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Earth Meld", "入地",
                                    "Sink into the earth and travel through it.",
                                    0.2, 0.1, RealmId.QI_CONDENSATION,
                                    "Your body becomes one with the earth. Stone flows around you."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Deep Earth Travel", "深层遁行",
                                    "Travel through earth at great speed and depth.",
                                    0.4, 0.3, RealmId.FOUNDATION,
                                    "The earth parts ahead of you. Miles pass in moments."
                            )
                    )
            ))
            .authorityRealm(RealmId.SOUL_FORMATION)
            .spirit(null)
            .build();

    // ── Illusionary Circle (幻境圈) ─────────────────────────────

    /**
     * Wang Lin's signature illusion technique. Creates a circle of
     * illusion that traps enemies in a fabricated reality. One of his
     * most versatile techniques — used for combat, escape, and concealment.
     *
     * <p>Canon: "Illusionary Devil Domain" — the illusion technique evolved
     * into a full domain at higher realms.
     */
    public static final ArtifactUsageProfile ILLUSIONARY_CIRCLE = ArtifactUsageProfile.builder(
            "wanglin/illusionary_circle", "Illusionary Circle", "幻境圈")
            .category("Technique")
            .artifactRealm(RealmId.SOUL_TRANSFORMATION)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Foundation era onwards", "Domain evolution"), 4))
            .baseDamage(0.0)
            .attackSpeed(0.0)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Illusion Sense", "The user can detect illusions cast by others.",
                            0.5, RealmId.FOUNDATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.5, 0.6, false, 0.4,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Simple Illusion", "幻术",
                                    "Create a convincing illusion of objects or people.",
                                    0.2, 0.3, RealmId.FOUNDATION,
                                    "The air shimmers. Something that isn't there appears."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Illusionary Circle", "幻境圈",
                                    "Trap enemies in a fabricated reality.",
                                    0.5, 0.5, RealmId.SOUL_FORMATION,
                                    "A circle of light expands. Inside it, the world is no longer real."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Illusionary Devil Domain", "幻境魔域",
                                    "The illusion becomes a self-sustaining domain.",
                                    0.8, 0.8, RealmId.SOUL_TRANSFORMATION,
                                    "The circle expands into a world. The real world becomes the illusion."
                            )
                    )
            ))
            .authorityRealm(RealmId.SOUL_TRANSFORMATION)
            .spirit(null)
            .build();

    // ── Foundation Stealing Technique (偷天换日) ─────────────────

    /**
     * Wang Lin's infamous technique for stealing another cultivator's
     * foundation. Transfers the victim's cultivation base to the user.
     * Extremely forbidden — causes massive karmic debt.
     *
     * <p>Canon: one of the most morally controversial techniques Wang Lin
     * uses. He only employs it against enemies who deserve no mercy.
     */
    public static final ArtifactUsageProfile FOUNDATION_STEALING_TECHNIQUE = ArtifactUsageProfile.builder(
            "wanglin/foundation_stealing_technique", "Foundation Stealing Technique", "偷天换日")
            .category("Technique")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Core Formation era"), 5,
                    "Canon name: literally 'steal the sky, replace the sun'"))
            .baseDamage(30.0) // devastating to the victim
            .attackSpeed(0.2) // slow, ritualistic
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Foundation Sense", "The user can sense the quality of others' foundations.",
                            0.4, RealmId.CORE_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.8, 0.7, true, 0.5,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Foundation Read", "探基",
                                    "Read a target's cultivation foundation in detail.",
                                    0.3, 0.5, RealmId.CORE_FORMATION,
                                    "Your divine sense pierces the target. Their entire cultivation path lays bare."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Foundation Transfer", "偷天换日",
                                    "Steal the target's foundation and absorb it.",
                                    0.9, 0.8, RealmId.ASCENDANT,
                                    "The target's cultivation drains away. Their foundation becomes yours. "
                                    + "The heavens watch in silence."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    2.5, // extremely dangerous
                    List.of(BacklashType.KARMIC_DEBT, BacklashType.HEAVENLY_KARMA, BacklashType.HEART_DEMON),
                    "Stealing a foundation stains the soul. Heaven remembers."
            ))
            .build();

    // ── Scattered Devil Armour (散魔铠) ─────────────────────────

    /**
     * A defensive armor forged from devil essence. Provides immense
     * physical and spiritual protection. Used extensively in the
     * Second Step arcs.
     *
     * <p>Canon: Wang Lin's primary defensive treasure in the mid-to-late
     * novel. The devil essence makes it dangerous to unworthy wearers.
     */
    public static final ArtifactUsageProfile SCATTERED_DEVIL_ARMOUR = ArtifactUsageProfile.builder(
            "wanglin/scattered_devil_armour", "Scattered Devil Armour", "散魔铠")
            .category("Defense / Armor")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Second Step arcs"), 4))
            .baseDamage(6.0) // can be used offensively via devil essence
            .attackSpeed(0.0) // armor has no attack speed
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Devil Essence Shield", "The armour projects a devil-essence barrier.",
                            0.8, RealmId.SOUL_TRANSFORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Soul Protection", "The armour protects the wearer's soul.",
                            0.6, RealmId.ASCENDANT)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.4, true, 0.4, // requires blood refinement
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Devil Essence Burst", "魔气爆发",
                                    "Release a burst of devil essence that damages nearby enemies.",
                                    0.5, 0.4, RealmId.SOUL_TRANSFORMATION,
                                    "Black energy erupts from the armour. The devil's remnant rage burns."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Full Devil Form", "全魔形态",
                                    "The armour fuses with the wearer. Devil essence permeates the body.",
                                    0.7, 0.6, RealmId.TRUE_IMMORTAL,
                                    "The armour melts into skin. Eyes turn black. Power surges."
                            )
                    )
            ))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.4, List.of(BacklashType.SOUL_INJURY, BacklashType.DEVIL_CORRUPTION),
                    "Devil essence may corrupt an unworthy wearer."
            ))
            .build();

    // ── Slaughter Immortal Art (屠仙大术) ──────────────────────

    /**
     * A devastating slaughter technique. Channels the Dao of Slaughter
     * into a concentrated attack capable of killing immortals.
     *
     * <p>Canon: one of Wang Lin's ultimate offensive techniques. Represents
     * the culmination of his understanding of the Slaughter Dao.
     */
    public static final ArtifactUsageProfile SLAUGHTER_IMMORTAL_ART = ArtifactUsageProfile.builder(
            "wanglin/slaughter_immortal_art", "Slaughter Immortal Art", "屠仙大术")
            .category("Technique")
            .artifactRealm(RealmId.NIRVANA_FRUIT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Second Step arcs"), 4))
            .baseDamage(35.0)
            .attackSpeed(0.3)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Slaughter Intent", "The user radiates killing intent that weakens the weak-willed.",
                            0.7, RealmId.SOUL_FORMATION)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.8, 0.7, false, 0.6,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Slaughter Palm", "屠仙掌",
                                    "A palm strike imbued with slaughter intent.",
                                    0.5, 0.5, RealmId.NIRVANA_SCRYER,
                                    "The palm descends. Where it lands, life ceases."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Immortal Slaughter", "屠仙",
                                    "The full technique — aimed at immortal-level targets.",
                                    0.9, 0.8, RealmId.NIRVANA_FRUIT,
                                    "Slaughter condenses into a point. Even immortals can die."
                            )
                    )
            ))
            .authorityRealm(RealmId.NIRVANA_FRUIT)
            .daoManifestation(new ArtifactUsageProfile.DaoManifestation(
                    "The Slaughter Dao manifests as the cessation of existence.",
                    "Wang Lin raises his hand. The target does not die — they simply stop existing.",
                    Provenance.explicit("Renegade Immortal", List.of("Second Step arcs"), 4)
            ))
            .spirit(null)
            .backlashProfile(new ArtifactUsageProfile.BacklashProfile(
                    1.6, List.of(BacklashType.KARMIC_DEBT, BacklashType.HEART_DEMON),
                    "Each use of the Slaughter Dao deepens the heart demon."
            ))
            .build();

    // ── Ancient God Tactic (古神诀) ─────────────────────────────

    /**
     * The cultivation method of the Ancient Gods. Wang Lin obtained
     * this through Tu Si's inheritance. Provides physical body
     * enhancement and the Ancient God's unique power system.
     *
     * <p>Canon: the Ancient God Tactic is separate from standard
     * cultivation — it refines the physical body through star points
     * rather than qi cultivation. Wang Lin dual-cultivates both paths.
     */
    public static final ArtifactUsageProfile ANCIENT_GOD_TACTIC = ArtifactUsageProfile.builder(
            "wanglin/ancient_god_tactic", "Ancient God Tactic", "古神诀")
            .category("Technique")
            .artifactRealm(RealmId.TRUE_IMMORTAL)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Tu Si inheritance arc"), 5,
                    "Obtained from Ancient God Tu Si's inheritance"))
            .baseDamage(20.0) // Ancient God body = physical power
            .attackSpeed(0.8)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Ancient God Body", "Physical body is enhanced beyond mortal limits.",
                            0.8, RealmId.CORE_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Star Point Resonance", "Star points in the body resonate with the cosmos.",
                            0.5, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.6, 0.4, true, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Star Point Activation", "星辰点激活",
                                    "Activate star points in the body for bursts of power.",
                                    0.3, 0.3, RealmId.NASCENT_SOUL,
                                    "A star point ignites in your body. Ancient power surges through your meridians."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Ancient God Transformation", "古神变身",
                                    "Partially transform into Ancient God form.",
                                    0.6, 0.5, RealmId.ASCENDANT,
                                    "Your body grows. Stars appear in your eyes. The blood of gods flows."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Full Ancient God", "完全古神化",
                                    "Full Ancient God transformation. Immense power, immense cost.",
                                    0.9, 0.8, RealmId.TRUE_IMMORTAL,
                                    "You ARE the Ancient God. The stars are your veins. The void is your breath."
                            )
                    )
            ))
            .authorityRealm(RealmId.TRUE_IMMORTAL)
            .spirit(null)
            .build();

    // ── Seven-Colored Lance (七彩长矛) ──────────────────────────

    /**
     * A Celestial weapon — the Seven-Colored Lance. One of Wang Lin's
     * most powerful late-game weapons. Radiates seven colors of light
     * representing different Dao essences.
     *
     * <p>Canon: used in the Immortal Astral Continent arcs. A weapon
     * of devastating power that incorporates multiple Dao essences.
     */
    public static final ArtifactUsageProfile SEVEN_COLORED_LANCE = ArtifactUsageProfile.builder(
            "wanglin/seven_colored_lance", "Seven-Colored Lance", "七彩长矛")
            .category("Weapon")
            .artifactRealm(RealmId.SPIRIT_SEIZER)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Immortal Astral Continent arc"), 4))
            .baseDamage(30.0)
            .attackSpeed(0.7)
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Seven Essence Radiance", "The lance radiates all seven essence colors.",
                            0.8, RealmId.NIRVANA_CLEANSER),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Dao Pressure", "The seven essences create overwhelming pressure.",
                            0.6, RealmId.TRUE_IMMORTAL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.7, 0.6, false, 0.5,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Essence Strike", "精华一击",
                                    "Channel one essence through the lance for a focused attack.",
                                    0.4, 0.5, RealmId.TRUE_IMMORTAL,
                                    "The lance glows with a single color. That color becomes everything."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Seven Essence Burst", "七彩爆发",
                                    "Release all seven essences simultaneously.",
                                    0.8, 0.7, RealmId.SPIRIT_SEIZER,
                                    "Seven colors spiral around the lance. Reality fractures where they meet."
                            )
                    )
            ))
            .authorityRealm(RealmId.SPIRIT_SEIZER)
            .spirit(null)
            .build();

    // ── Bootstrap ──────────────────────────────────────────────────

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        ArtifactUsageProfilesGenerated.bootstrap();
        // Hand-written profiles (20 total) take precedence over generated profiles.
        // Generated profiles cover the remaining ~289 items.
    }
}