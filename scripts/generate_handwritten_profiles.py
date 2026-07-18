#!/usr/bin/env python3
"""
Generate 15 hand-written ArtifactUsageProfiles for Wang Lin's signature treasures.
These are inserted before the bootstrap method in ArtifactUsageProfiles.java.
"""

profiles = []

# 1. Karma Whip (因果鞭) — evolved form of Soul Lasher
profiles.append("""
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
""")

# 2. Ghostly Sail (鬼帆)
profiles.append("""
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
""")

# 3. Billion Soul Flag (亿万魂幡)
profiles.append("""
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
""")

# 4. Mosquito Beast (蚊兽)
profiles.append("""
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
""")

# 5. Xu Liguo / Devil Sword (许立国 / 魔剑)
profiles.append("""
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
                                    "\\"Master, this one is too strong—\\" The sword cuts anyway. Xu Liguo always follows through."
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
""")

# 6. Annihilation Restriction (灭世禁制)
profiles.append("""
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
""")

# 7. Li Guang Heaven-Shattering Bow (李广惊天弓)
profiles.append("""
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
""")

# 8. Heaven-Avoiding Coffin (避天棺)
profiles.append("""
    // ── Heaven-Avoiding Coffin (避天棺) ─────────────────────────

    /**
     * A coffin that hides the user from heavenly detection. Used by
     * Wang Lin to avoid tribulation detection and divine sense scanning.
     * A crucial survival tool in dangerous territories.
     *
     * <p>Canon: "Even Heaven cannot find what lies within." Used during
     * Wang Lin's most dangerous infiltrations and escapes.
     */
    public static final ArtifactUsageProfile HEAVEN_AVOIDING_COFFIN = ArtifactUsageProfile.builder(
            "wanglin/heaven_avoiding_coffin", "Heaven-Avoiding Coffin", "避天棺")
            .category("Treasure / Artifact")
            .artifactRealm(RealmId.ASCENDANT)
            .provenance(Provenance.explicit("Renegade Immortal",
                    List.of("Soul Transformation era"), 4))
            .baseDamage(5.0) // can be used as a blunt weapon in desperation
            .attackSpeed(0.3) // coffins are not fast
            .passiveEffects(List.of(
                    new ArtifactUsageProfile.PassiveEffect(
                            "Heavenly Concealment", "The user is hidden from heavenly detection.",
                            0.9, RealmId.SOUL_FORMATION),
                    new ArtifactUsageProfile.PassiveEffect(
                            "Death Aura", "The coffin radiates an aura of death that deters the weak.",
                            0.4, RealmId.NASCENT_SOUL)
            ))
            .activation(new ArtifactUsageProfile.ActivationSpec(
                    0.4, 0.7, false, 0.3,
                    List.of(
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Complete Concealment", "完全隐匿",
                                    "Enter the coffin. Divine sense cannot find you.",
                                    0.3, 0.7, RealmId.SOUL_FORMATION,
                                    "The lid closes. The world forgets you exist."
                            ),
                            new ArtifactUsageProfile.ActivatableAbility(
                                    "Coffin Escape", "棺遁",
                                    "The coffin burrows through earth and space to escape.",
                                    0.6, 0.5, RealmId.SOUL_TRANSFORMATION,
                                    "The coffin drops into the ground. Space bends around it."
                            )
                    )
            ))
            .authorityRealm(RealmId.ASCENDANT)
            .spirit(null)
            .build();
""")

# 9. Earth Escape Technique (土遁术)
profiles.append("""
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
""")

# 10. Illusionary Circle (幻境圈)
profiles.append("""
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
""")

# 11. Foundation Stealing Technique (偷天换日)
profiles.append("""
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
                    "Canon name: 偷天换日, literally 'steal the sky, replace the sun'"))
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
""")

# 12. Scattered Devil Armour (散魔铠)
profiles.append("""
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
""")

# 13. Slaughter Immortal Art (屠仙大术)
profiles.append("""
    // ── Slaughter Immortal Art (屠仙大术) ──────────────────────

    /**
     * A devastating slaughter technique that Wang Lin developed/completed.
     * Channels the Dao of Slaughter into a concentrated attack capable
     * of killing immortals.
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
""")

# 14. Ancient God Tactic (古神诀)
profiles.append("""
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
""")

# 15. Seven-Colored Lance (七彩长矛)
profiles.append("""
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
""")

# Output the combined profiles
output = "\n".join(profiles)
print(output)
print(f"\n--- Total profiles: {len(profiles)}")